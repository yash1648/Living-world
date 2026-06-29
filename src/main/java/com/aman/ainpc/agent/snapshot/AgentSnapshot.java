package com.aman.ainpc.agent.snapshot;

import java.util.UUID;

/**
 * Immutable, read-only snapshot of an NPC's visible state at a point in time.
 *
 * AgentRuntime creates this via createSnapshot(). Downstream systems —
 * ConversationContextBuilder and any future consumer — read from this snapshot
 * rather than holding a reference to the live runtime.
 *
 * No behavior. No references to AgentRuntime. No mutable collections.
 */
public final class AgentSnapshot {

    private final UUID   npcId;
    private final String npcName;
    private final String systemPrompt;
    private final String needsSummary;
    private final String knowledgeSummary;

    public AgentSnapshot(UUID npcId,
                         String npcName,
                         String systemPrompt,
                         String needsSummary,
                         String knowledgeSummary) {
        this.npcId            = npcId;
        this.npcName          = npcName;
        this.systemPrompt     = systemPrompt;
        this.needsSummary     = needsSummary;
        this.knowledgeSummary = knowledgeSummary;
    }

    public UUID   getNpcId()            { return npcId; }
    public String getNpcName()          { return npcName; }
    public String getSystemPrompt()     { return systemPrompt; }
    public String getNeedsSummary()     { return needsSummary; }
    public String getKnowledgeSummary() { return knowledgeSummary; }
}
