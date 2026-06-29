package com.aman.ainpc.conversation.session;

import java.util.UUID;

/**
 * Immutable identity of one entity taking part in a ConversationSession.
 */
public final class ConversationParticipant {

    private final UUID entityId;

    public ConversationParticipant(UUID entityId) {
        this.entityId = entityId;
    }

    public UUID getEntityId() { return entityId; }
}
