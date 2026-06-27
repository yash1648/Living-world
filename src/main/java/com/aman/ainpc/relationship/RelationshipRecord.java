package com.aman.ainpc.relationship;

import java.time.Instant;
import java.util.UUID;

public class RelationshipRecord {

    private final UUID entityId;
    private final Instant createdAt;

    public RelationshipRecord(UUID entityId, Instant createdAt) {
        this.entityId = entityId;
        this.createdAt = createdAt;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
