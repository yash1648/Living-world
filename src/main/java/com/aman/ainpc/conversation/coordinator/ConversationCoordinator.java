package com.aman.ainpc.conversation.coordinator;

import com.aman.ainpc.agent.runtime.AgentRuntime;
import com.aman.ainpc.conversation.ConversationContextBuilder;
import com.aman.ainpc.conversation.session.ConversationParticipant;
import com.aman.ainpc.conversation.session.ConversationSession;
import com.aman.ainpc.conversation.session.ConversationSessionManager;
import com.aman.ainpc.interaction.InteractionContext;
import com.aman.ainpc.interaction.InteractionDecision;
import com.aman.ainpc.interaction.InteractionEngine;
import com.aman.ainpc.interaction.request.InteractionRequest;
import com.aman.ainpc.interaction.request.InteractionResolver;
import net.minecraft.server.MinecraftServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Owns the complete NPC conversation pipeline.
 *
 * Responsibilities moved here from ConversationHandler:
 *   - All architecture layer singletons (resolver, session manager, engine, builder)
 *   - Session lifecycle (create on entry, close after reply)
 *   - AgentSnapshot creation
 *   - ConversationContext assembly
 *   - Background thread management for the AI call
 *   - The AI HTTP call itself (networking, JSON, response parsing)
 *
 * ConversationHandler is left as Minecraft glue only:
 * Forge event subscription, InteractionRequest construction, calling this class,
 * and sending the returned reply to the player.
 *
 * Pipeline:
 *   InteractionRequest
 *   ↓ InteractionResolver       (raw → typed context)
 *   ↓ ConversationSessionManager (open session)
 *   ↓ InteractionEngine         (accept / reject)
 *   ↓ AgentRuntime.createSnapshot()
 *   ↓ ConversationContextBuilder (assemble AI context)
 *   ↓ callAI()                  (HTTP — unchanged)
 *   ↓ ConversationSessionManager (close session)
 *   ↓ onComplete callback       (back on server thread)
 */
public class ConversationCoordinator {

    private static final String AI_ENDPOINT = "http://127.0.0.1:5000/chat";

    /**
     * Immutable result returned to ConversationHandler after the AI replies.
     */
    public static final class ConversationResult {
        public final String npcName;
        public final String reply;

        ConversationResult(String npcName, String reply) {
            this.npcName = npcName;
            this.reply   = reply;
        }
    }

    // ── Pipeline singletons ───────────────────────────────────────

    private final InteractionResolver        resolver        = new InteractionResolver();
    private final ConversationSessionManager sessionManager  = new ConversationSessionManager();
    private final InteractionEngine          engine          = new InteractionEngine();
    private final ConversationContextBuilder contextBuilder  = new ConversationContextBuilder();

    // ── Public entry point ────────────────────────────────────────

    /**
     * Run the full conversation pipeline for one player message.
     *
     * Steps 1–5 run synchronously on the calling thread (server tick thread).
     * The AI call (step 6) runs on a daemon background thread.
     * {@code onComplete} is scheduled back onto the server thread via
     * {@code server.execute()} so Minecraft state can be safely accessed.
     *
     * @param request      the raw interaction created by ConversationHandler
     * @param runtime      the NPC's live runtime (may be null)
     * @param server       the Minecraft server (used to schedule the reply callback)
     * @param onThinking   called immediately (server thread) with the NPC name
     * @param onComplete   called after the AI replies (server thread) with the result
     */
    public void handle(InteractionRequest request,
                       AgentRuntime runtime,
                       MinecraftServer server,
                       Consumer<String> onThinking,
                       Consumer<ConversationResult> onComplete) {

        UUID playerUUID = request.getInitiatorId();
        UUID npcUUID    = request.getTargetId();

        // ── 1. Resolve raw request → typed context ────────────────
        InteractionContext context = resolver.resolve(request);

        // ── 2. Open a ConversationSession ─────────────────────────
        ConversationSession session = sessionManager.createSession(List.of(
                new ConversationParticipant(playerUUID),
                new ConversationParticipant(npcUUID)
        ));

        // ── 3. Evaluate — accept or reject ────────────────────────
        InteractionDecision decision = engine.evaluate(context);
        if (!decision.isAccepted()) {
            sessionManager.endSession(session.getSessionId());
            return;
        }

        // ── 4. Snapshot NPC state ─────────────────────────────────
        // ── 5. Build conversation context ─────────────────────────
        ConversationContextBuilder.Result built = contextBuilder.build(
                runtime != null ? runtime.createSnapshot() : null);

        // Notify caller that the NPC is "thinking" before going async
        onThinking.accept(built.npcName);

        // ── 6. Call AI on background thread ───────────────────────
        UUID sessionId = session.getSessionId();
        Thread aiThread = new Thread(() -> {
            String reply = callAI(
                    AI_ENDPOINT,
                    built.systemPrompt,
                    request.getPayload(),
                    built.needsSummary,
                    built.knowledgeSummary
            );

            // ── 7. Close session and deliver reply on server thread ─
            server.execute(() -> {
                sessionManager.endSession(sessionId);
                onComplete.accept(new ConversationResult(built.npcName, reply));
            });
        });
        aiThread.setDaemon(true);
        aiThread.setName("ainpc-ai-" + built.npcName);
        aiThread.start();
    }

    // ── AI call (unchanged from ConversationHandler) ──────────────

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
}
