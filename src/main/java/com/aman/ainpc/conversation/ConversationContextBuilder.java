package com.aman.ainpc.conversation;

import com.aman.ainpc.agent.snapshot.AgentSnapshot;

/**
 * Assembles the context fields needed to call the AI backend for a conversation.
 *
 * Accepts an immutable AgentSnapshot — never reads AgentRuntime directly.
 * Returns safe defaults when no snapshot is available.
 *
 * Future: enrich with LifeHistory narrative summary, relationship scores, and
 * active goal by adding those fields to AgentSnapshot and reading them here.
 */
public class ConversationContextBuilder {

    /**
     * Immutable result containing everything the AI backend needs for one exchange.
     */
    public static final class Result {
        public final String systemPrompt;
        public final String npcName;
        public final String needsSummary;
        public final String knowledgeSummary;

        Result(String systemPrompt, String npcName,
               String needsSummary, String knowledgeSummary) {
            this.systemPrompt     = systemPrompt;
            this.npcName          = npcName;
            this.needsSummary     = needsSummary;
            this.knowledgeSummary = knowledgeSummary;
        }
    }

    /**
     * Build the AI context from an immutable NPC snapshot.
     *
     * @param snapshot the NPC's AgentSnapshot, or null if unavailable
     * @return a fully-populated Result (falls back to safe defaults if snapshot is null)
     */
    public Result build(AgentSnapshot snapshot) {
        String systemPrompt = snapshot != null
                ? snapshot.getSystemPrompt()
                : "You are an NPC villager. Respond naturally in character.";
        String npcName = snapshot != null
                ? snapshot.getNpcName()
                : "NPC";
        String needsSummary = snapshot != null
                ? snapshot.getNeedsSummary()
                : "";
        String knowledgeSummary = snapshot != null
                ? snapshot.getKnowledgeSummary()
                : "";

        return new Result(systemPrompt, npcName, needsSummary, knowledgeSummary);
    }
}
