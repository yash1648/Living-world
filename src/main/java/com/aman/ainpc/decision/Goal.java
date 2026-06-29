package com.aman.ainpc.decision;

import java.time.Instant;

/**
 * The current goal chosen by the Decision Engine.
 *
 * A goal is an intent — what the NPC wants to do right now.
 * Phase 3 (Behavior) will decompose goals into executable Tasks.
 */
public class Goal {

    private final GoalType type;
    private final String reason;
    private final float priority;   // 0.0 (lowest) to 1.0 (highest)
    private final Instant decidedAt;

    public Goal(GoalType type, String reason, float priority) {
        this.type = type;
        this.reason = reason;
        this.priority = Math.max(0.0f, Math.min(1.0f, priority));
        this.decidedAt = Instant.now();
    }

    public GoalType getType() { return type; }
    public String getReason() { return reason; }
    public float getPriority() { return priority; }
    public Instant getDecidedAt() { return decidedAt; }

    @Override
    public String toString() {
        return type.name() + " (p=" + String.format("%.2f", priority) + "): " + reason;
    }
}
