package com.aman.ainpc.conversation;

import com.aman.ainpc.AINPC;
import com.aman.ainpc.AINPCEntity;
import com.aman.ainpc.agent.runtime.AgentRuntime;
import com.aman.ainpc.agent.runtime.AgentRuntimeManager;
import com.aman.ainpc.conversation.session.ConversationParticipant;
import com.aman.ainpc.conversation.session.ConversationSession;
import com.aman.ainpc.conversation.session.ConversationSessionManager;
import com.aman.ainpc.interaction.InteractionContext;
import com.aman.ainpc.interaction.InteractionDecision;
import com.aman.ainpc.interaction.InteractionEngine;
import com.aman.ainpc.interaction.request.InteractionRequest;
import com.aman.ainpc.interaction.request.InteractionResolver;
import com.aman.ainpc.interaction.request.InteractionSource;
import com.aman.ainpc.needs.NeedType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Server-side conversation handler.
 *
 * Player chat now travels through the full interaction architecture before
 * reaching the AI backend:
 *
 *   Player Chat
 *   ↓ InteractionRequest        (raw external event)
 *   ↓ InteractionResolver       (raw → typed context; currently always UNKNOWN)
 *   ↓ ConversationSessionManager (open a session for this exchange)
 *   ↓ InteractionEngine         (decide whether to accept; currently always accepts)
 *   ↓ ConversationContextBuilder (assemble NPC context from AgentRuntime)
 *   ↓ Existing AI backend        (unchanged — HTTP call, JSON, LLM prompt)
 *
 * The old code path that built systemPrompt / needsSummary / knowledgeSummary
 * inline is now obsolete — that logic lives in ConversationContextBuilder.
 * The old inline code is NOT deleted yet per architectural handoff policy.
 *
 * Fix for B1: uses ServerChatEvent (server-side) instead of ClientChatEvent.
 * Fix for B2: LLM generates dialogue only; game state is owned by Needs system.
 */
@Mod.EventBusSubscriber(modid = AINPC.MODID)
public class ConversationHandler {

    private static final String DEFAULT_AI_ENDPOINT = "http://127.0.0.1:5000/chat";

    // ── Architecture layer singletons ─────────────────────────────
    private static final InteractionResolver        RESOLVER        = new InteractionResolver();
    private static final ConversationSessionManager SESSION_MANAGER = new ConversationSessionManager();
    private static final InteractionEngine          ENGINE          = new InteractionEngine();
    private static final ConversationContextBuilder CONTEXT_BUILDER = new ConversationContextBuilder();

    // ── Event handler ─────────────────────────────────────────────

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player    = event.getPlayer();
        UUID         playerUUID = player.getUUID();

        if (!ConversationManager.getInstance().isInConversation(playerUUID)) return;

        // Intercept — this message is for the NPC, not the chat channel
        event.setCanceled(true);

        Optional<UUID> npcUUID = ConversationManager.getInstance().getConversationNPC(playerUUID);
        if (npcUUID.isEmpty()) return;

        String rawMessage = event.getRawText();

        // ── 1. Build the raw interaction request ──────────────────
        InteractionRequest request = new InteractionRequest(
                InteractionSource.PLAYER_CHAT,
                playerUUID,
                npcUUID.get(),
                rawMessage,
                System.currentTimeMillis()
        );

        // ── 2. Resolve request → typed InteractionContext ─────────
        //       (currently always produces InteractionType.UNKNOWN)
        InteractionContext context = RESOLVER.resolve(request);

        // ── 3. Open a ConversationSession for this exchange ───────
        ConversationSession session = SESSION_MANAGER.createSession(List.of(
                new ConversationParticipant(playerUUID),
                new ConversationParticipant(npcUUID.get())
        ));

        // ── 4. Let the InteractionEngine decide ───────────────────
        //       (currently always accepts)
        InteractionDecision decision = ENGINE.evaluate(context);
        if (!decision.isAccepted()) {
            SESSION_MANAGER.endSession(session.getSessionId());
            return;
        }

        // ── 5. Build NPC context from AgentRuntime ────────────────
        AgentRuntime runtime = AgentRuntimeManager.getInstance().getRuntime(npcUUID.get());
        ConversationContextBuilder.Result built = CONTEXT_BUILDER.build(runtime);

        // Tell player NPC is thinking
        player.sendSystemMessage(Component.literal("§7[" + built.npcName + " is thinking...]§r"));

        // ── 6. Call existing AI backend on background thread ──────
        //       (networking, HTTP, JSON, LLM prompt — all unchanged)
        UUID sessionId = session.getSessionId();
        Thread aiThread = new Thread(() -> {
            String response = callAI(
                    DEFAULT_AI_ENDPOINT,
                    built.systemPrompt,
                    rawMessage,
                    built.needsSummary,
                    built.knowledgeSummary
            );

            player.getServer().execute(() -> {
                player.sendSystemMessage(
                        Component.literal("§e[" + built.npcName + "]§r " + response));

                // Spawn emotion particles based on NPC's current Needs (not LLM keywords)
                if (runtime != null) {
                    spawnNeedsParticles(runtime, player, npcUUID.get());
                }

                // Close the session when the exchange completes
                SESSION_MANAGER.endSession(sessionId);
            });
        });
        aiThread.setDaemon(true);
        aiThread.setName("ainpc-ai-" + built.npcName);
        aiThread.start();
    }

    // ── AI Call (unchanged) ───────────────────────────────────────

    private static String callAI(String endpoint, String systemPrompt, String playerMessage,
                                  String needsSummary, String knowledgeSummary) {
        try {
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(15000);
            conn.setDoOutput(true);

            // Escape quotes in strings for JSON
            String safeSystem    = systemPrompt.replace("\"", "'");
            String safeMessage   = playerMessage.replace("\"", "'");
            String safeNeeds     = needsSummary.replace("\"", "'");
            String safeKnowledge = knowledgeSummary.replace("\"", "'");

            String json = "{" +
                    "\"system\":\""    + safeSystem    + "\"," +
                    "\"needs\":\""     + safeNeeds     + "\"," +
                    "\"knowledge\":\"" + safeKnowledge + "\"," +
                    "\"message\":\""   + safeMessage   + "\"" +
                    "}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes("UTF-8"));
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
            }

            // Parse simple {"reply":"..."} response
            String body = sb.toString().trim();
            if (body.contains("\"reply\"")) {
                int start      = body.indexOf("\"reply\"") + 9;
                int valueStart = body.indexOf("\"", start) + 1;
                int valueEnd   = body.lastIndexOf("\"");
                if (valueStart > 0 && valueEnd > valueStart) {
                    return body.substring(valueStart, valueEnd);
                }
            }
            return body;

        } catch (Exception e) {
            return "(The NPC seems lost in thought and doesn't respond right now.)";
        }
    }

    // ── Need-driven Particle Emotions (unchanged) ─────────────────

    private static void spawnNeedsParticles(AgentRuntime runtime, ServerPlayer player, UUID npcUUID) {
        Entity npcEntity = player.serverLevel().getEntity(npcUUID);
        if (!(npcEntity instanceof AINPCEntity npc)) return;
        if (!(npc.level() instanceof ServerLevel serverLevel)) return;

        double x = npc.getX();
        double y = npc.getY() + 2.2;
        double z = npc.getZ();

        float safety    = runtime.getNeedsManager().getNeed(NeedType.SAFETY).getValue();
        float loneliness = runtime.getNeedsManager().getNeed(NeedType.LONELINESS).getValue();
        float hunger    = runtime.getNeedsManager().getNeed(NeedType.HUNGER).getValue();

        if (safety > 0.5f) {
            serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER,  x, y, z, 3, 0.3, 0.2, 0.3, 0);
        } else if (loneliness < 0.2f) {
            serverLevel.sendParticles(ParticleTypes.HEART,           x, y, z, 3, 0.3, 0.2, 0.3, 0);
        } else if (hunger > 0.6f) {
            serverLevel.sendParticles(ParticleTypes.SPLASH,          x, y, z, 3, 0.3, 0.2, 0.3, 0.1);
        } else {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,  x, y, z, 3, 0.3, 0.2, 0.3, 0);
        }
    }
}
