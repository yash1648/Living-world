package com.aman.ainpc.conversation;

import com.aman.ainpc.AINPC;
import com.aman.ainpc.AINPCEntity;
import com.aman.ainpc.agent.runtime.AgentRuntime;
import com.aman.ainpc.agent.runtime.AgentRuntimeManager;
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
import java.util.Optional;
import java.util.UUID;

/**
 * Server-side conversation handler.
 *
 * When a player is in conversation with an NPC (tracked by ConversationManager),
 * their chat messages are intercepted here — NOT broadcast to all players.
 * The message is sent to the AI backend. The LLM returns DIALOGUE ONLY.
 * Emotion/particle effects are driven by the NPC's Needs system, not the LLM.
 *
 * Fix for B1: uses ServerChatEvent (server-side) instead of ClientChatEvent.
 * Fix for B2: LLM generates dialogue only; game state is owned by Needs system.
 */
@Mod.EventBusSubscriber(modid = AINPC.MODID)
public class ConversationHandler {

    private static final String DEFAULT_AI_ENDPOINT = "http://127.0.0.1:5000/chat";

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        UUID playerUUID = player.getUUID();

        if (!ConversationManager.getInstance().isInConversation(playerUUID)) return;

        // Intercept — this message is for the NPC, not the chat channel
        event.setCanceled(true);

        Optional<UUID> npcUUID = ConversationManager.getInstance().getConversationNPC(playerUUID);
        if (npcUUID.isEmpty()) return;

        String playerMessage = event.getRawText();

        // Resolve the NPC entity and runtime
        AgentRuntime runtime = AgentRuntimeManager.getInstance().getRuntime(npcUUID.get());

        // Build NPC system context
        String systemPrompt = runtime != null
                ? runtime.getCharacterProfile().toSystemPrompt()
                : "You are an NPC villager. Respond naturally in character.";
        String npcName = runtime != null
                ? runtime.getCharacterProfile().getName()
                : "NPC";
        String needsSummary = runtime != null
                ? runtime.getNeedsManager().summarize()
                : "";
        String knowledgeSummary = runtime != null
                ? runtime.getKnowledgeBase().summarize(8)
                : "";

        // Tell player NPC is thinking
        player.sendSystemMessage(Component.literal("§7[" + npcName + " is thinking...]§r"));

        // Run AI call on background thread (never block the server tick)
        Thread aiThread = new Thread(() -> {
            String response = callAI(DEFAULT_AI_ENDPOINT, systemPrompt, playerMessage, needsSummary, knowledgeSummary);

            // Schedule response back on the main server thread
            player.getServer().execute(() -> {
                // Display the NPC's response
                player.sendSystemMessage(Component.literal("§e[" + npcName + "]§r " + response));

                // Spawn emotion particles based on NPC's current Needs (not LLM keywords)
                if (runtime != null) {
                    spawnNeedsParticles(runtime, player, npcUUID.get());
                }
            });
        });
        aiThread.setDaemon(true);
        aiThread.setName("ainpc-ai-" + npcName);
        aiThread.start();
    }

    // ── AI Call ───────────────────────────────────────────────────

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
            String safeSystem = systemPrompt.replace("\"", "'");
            String safeMessage = playerMessage.replace("\"", "'");
            String safeNeeds = needsSummary.replace("\"", "'");
            String safeKnowledge = knowledgeSummary.replace("\"", "'");

            String json = "{" +
                    "\"system\":\"" + safeSystem + "\"," +
                    "\"needs\":\"" + safeNeeds + "\"," +
                    "\"knowledge\":\"" + safeKnowledge + "\"," +
                    "\"message\":\"" + safeMessage + "\"" +
                    "}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes("UTF-8"));
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
            }

            // Parse simple {"reply":"..."} response
            String body = sb.toString().trim();
            if (body.contains("\"reply\"")) {
                int start = body.indexOf("\"reply\"") + 9;
                int valueStart = body.indexOf("\"", start) + 1;
                int valueEnd = body.lastIndexOf("\"");
                if (valueStart > 0 && valueEnd > valueStart) {
                    return body.substring(valueStart, valueEnd);
                }
            }
            return body;

        } catch (Exception e) {
            return "(The NPC seems lost in thought and doesn't respond right now.)";
        }
    }

    // ── Need-driven Particle Emotions ─────────────────────────────

    private static void spawnNeedsParticles(AgentRuntime runtime, ServerPlayer player, UUID npcUUID) {
        Entity npcEntity = player.serverLevel().getEntity(npcUUID);
        if (!(npcEntity instanceof AINPCEntity npc)) return;
        if (!(npc.level() instanceof ServerLevel serverLevel)) return;

        double x = npc.getX();
        double y = npc.getY() + 2.2;
        double z = npc.getZ();

        float safety = runtime.getNeedsManager().getNeed(NeedType.SAFETY).getValue();
        float loneliness = runtime.getNeedsManager().getNeed(NeedType.LONELINESS).getValue();
        float hunger = runtime.getNeedsManager().getNeed(NeedType.HUNGER).getValue();

        if (safety > 0.5f) {
            // NPC is scared — angry particles
            serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER, x, y, z, 3, 0.3, 0.2, 0.3, 0);
        } else if (loneliness < 0.2f) {
            // NPC is happy to have company — heart particles
            serverLevel.sendParticles(ParticleTypes.HEART, x, y, z, 3, 0.3, 0.2, 0.3, 0);
        } else if (hunger > 0.6f) {
            // NPC is hungry — splash particles
            serverLevel.sendParticles(ParticleTypes.SPLASH, x, y, z, 3, 0.3, 0.2, 0.3, 0.1);
        } else {
            // Content — happy villager
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 3, 0.3, 0.2, 0.3, 0);
        }
    }
}
