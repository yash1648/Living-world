package com.aman.ainpc.interaction;

import java.util.UUID;

/**
 * Immutable snapshot of a single interaction event.
 *
 * Carries everything the InteractionEngine needs to make a decision:
 * who initiated, who is targeted, what type of interaction it is,
 * the raw player message, and when it occurred.
 *
 * No parsing, no networking, no Minecraft API.
 */
public final class InteractionContext {

    private final InteractionType interactionType;
    private final UUID initiatorId;
    private final UUID targetId;
    private final String rawMessage;
    private final long timestamp;

    public InteractionContext(InteractionType interactionType,
                              UUID initiatorId,
                              UUID targetId,
                              String rawMessage,
                              long timestamp) {
        this.interactionType = interactionType;
        this.initiatorId     = initiatorId;
        this.targetId        = targetId;
        this.rawMessage      = rawMessage;
        this.timestamp       = timestamp;
    }

    public InteractionType getInteractionType() { return interactionType; }
    public UUID getInitiatorId()                { return initiatorId; }
    public UUID getTargetId()                   { return targetId; }
    public String getRawMessage()               { return rawMessage; }
    public long getTimestamp()                  { return timestamp; }
}
