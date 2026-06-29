package com.aman.ainpc.conversation;

import com.aman.ainpc.agent.runtime.AgentRuntime;

/**
 * Assembles the context fields needed to call the AI backend for a conversation.
 *
 * Extracts data from the NPC's AgentRuntime (character profile, needs, knowledge)
 * and returns them as an immutable Result. Returns safe defaults when no runtime
 * is available.
 *
 * Currently returns the same values the old ConversationHandler built inline.
 * Future: enrich with LifeHistory narrative summary, relationship scores, and
 * active goal — all read from the AgentRuntime without touching Minecraft.
 */
public class ConversationContextBuilder {

    /**
     * Immutable snapshot of everything the AI backend needs for one exchange.
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
     * Build the AI context from the NPC's runtime state.
     *
     * @param runtime the NPC's AgentRuntime, or null if unavailable
     * @return a fully-populated Result (falls back to safe defaults if runtime is null)
     */
    public Result build(AgentRuntime runtime) {
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

        return new Result(systemPrompt, npcName, needsSummary, knowledgeSummary);
    }
}
