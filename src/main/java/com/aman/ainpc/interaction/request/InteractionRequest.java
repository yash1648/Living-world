package com.aman.ainpc.interaction.request;

import java.util.UUID;

/**
 * Immutable record of a raw external event directed at an NPC.
 *
 * Contains only what physically happened — who did it, from which source,
 * the raw payload, and when. No intent, no classification, no parsing.
 *
 * An InteractionResolver translates this into an InteractionContext.
 */
public final class InteractionRequest {

    private final InteractionSource source;
    private final UUID initiatorId;
    private final UUID targetId;
    private final String payload;
    private final long timestamp;

    public InteractionRequest(InteractionSource source,
                              UUID initiatorId,
                              UUID targetId,
                              String payload,
                              long timestamp) {
        this.source      = source;
        this.initiatorId = initiatorId;
        this.targetId    = targetId;
        this.payload     = payload;
        this.timestamp   = timestamp;
    }

    public InteractionSource getSource()  { return source; }
    public UUID getInitiatorId()          { return initiatorId; }
    public UUID getTargetId()             { return targetId; }
    public String getPayload()            { return payload; }
    public long getTimestamp()            { return timestamp; }
}
