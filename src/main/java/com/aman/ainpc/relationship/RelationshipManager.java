package com.aman.ainpc.relationship;

import com.aman.ainpc.event.Event;
import com.aman.ainpc.event.dispatch.EventConsumer;
import com.aman.ainpc.perception.Observation;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages all of this NPC's relationships with other entities.
 *
 * Implements EventConsumer — relationship scores update automatically
 * as events are dispatched through the pipeline.
 */
public class RelationshipManager implements EventConsumer {

    private final Map<UUID, RelationshipRecord> relationships = new HashMap<>();

    @Override
    public void accept(Event event) {
        switch (event.getType()) {

            case MET_PLAYER, NPC_ENCOUNTERED -> {
                for (Observation obs : event.getSourceObservations()) {
                    if (obs.getTargetUUID() != null) {
                        ensureRecord(obs.getTargetUUID()).onEncountered();
                    }
                }
            }

            case PLAYER_ATTACKED_NPC, NPC_HURT -> {
                for (Observation obs : event.getSourceObservations()) {
                    // The attacker is the source
                    if (obs.getSourceUUID() != null) {
                        ensureRecord(obs.getSourceUUID()).onAttackedBy();
                    }
                }
            }

            case ENTITY_DAMAGED -> {
                for (Observation obs : event.getSourceObservations()) {
                    // Source attacked target — from the NPC's perspective, source is scary
                    if (obs.getSourceUUID() != null) {
                        ensureRecord(obs.getSourceUUID()).onAttackedBy();
                    }
                }
            }

            case PLAYER_GAVE_ITEM -> {
                for (Observation obs : event.getSourceObservations()) {
                    if (obs.getSourceUUID() != null) {
                        ensureRecord(obs.getSourceUUID()).onGaveItem();
                    }
                }
            }

            case PLAYER_TOOK_ITEM -> {
                for (Observation obs : event.getSourceObservations()) {
                    if (obs.getSourceUUID() != null) {
                        ensureRecord(obs.getSourceUUID()).onTookItem();
                    }
                }
            }

            case CONVERSATION_HAD -> {
                for (Observation obs : event.getSourceObservations()) {
                    if (obs.getTargetUUID() != null) {
                        ensureRecord(obs.getTargetUUID()).onConversation();
                    }
                }
            }

            case NPC_HELPED -> {
                for (Observation obs : event.getSourceObservations()) {
                    if (obs.getTargetUUID() != null) {
                        ensureRecord(obs.getTargetUUID()).onHelped();
                    }
                }
            }

            default -> {
                // For any other event, still track unique entities observed
                for (Observation obs : event.getSourceObservations()) {
                    if (obs.getTargetUUID() != null) {
                        ensureRecord(obs.getTargetUUID());
                    }
                }
            }
        }
    }

    // ── Queries ───────────────────────────────────────────────────

    public Collection<RelationshipRecord> getRelationships() {
        return Collections.unmodifiableCollection(relationships.values());
    }

    public Optional<RelationshipRecord> getRelationship(UUID entityId) {
        return Optional.ofNullable(relationships.get(entityId));
    }

    public boolean knows(UUID entityId) {
        return relationships.containsKey(entityId);
    }

    public String summarize() {
        if (relationships.isEmpty()) return "No known entities.";
        StringBuilder sb = new StringBuilder();
        for (RelationshipRecord r : relationships.values()) {
            sb.append(r.getEntityId().toString(), 0, 8)
              .append("=").append(r.getRelationshipLabel()).append("; ");
        }
        return sb.toString();
    }

    public void clear() {
        relationships.clear();
    }

    // ── Internal ──────────────────────────────────────────────────

    private RelationshipRecord ensureRecord(UUID entityId) {
        return relationships.computeIfAbsent(entityId,
                id -> new RelationshipRecord(id, Instant.now()));
    }
}
