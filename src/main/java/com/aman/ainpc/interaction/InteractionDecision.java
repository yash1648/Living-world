package com.aman.ainpc.interaction;

/**
 * Immutable result of the InteractionEngine evaluating an InteractionContext.
 *
 * Records whether the NPC accepts the interaction and why.
 * No gameplay logic, no dialogue, no Minecraft API.
 */
public final class InteractionDecision {

    private final boolean accepted;
    private final String reason;

    public InteractionDecision(boolean accepted, String reason) {
        this.accepted = accepted;
        this.reason   = reason;
    }

    public boolean isAccepted() { return accepted; }
    public String getReason()   { return reason; }

    @Override
    public String toString() {
        return String.format("InteractionDecision[accepted=%b, reason='%s']", accepted, reason);
    }
}
