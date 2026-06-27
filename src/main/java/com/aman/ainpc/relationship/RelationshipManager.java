package com.aman.ainpc.relationship;

import com.aman.ainpc.event.Event;
import com.aman.ainpc.event.dispatch.EventConsumer;
import com.aman.ainpc.perception.Observation;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RelationshipManager implements EventConsumer {

    private final Map<UUID, RelationshipRecord> relationships = new HashMap<>();

    @Override
    public void accept(Event event) {
        Set<UUID> entityIds = new HashSet<>();
        for (Observation obs : event.getSourceObservations()) {
            if (obs.getSourceUUID() != null) {
                entityIds.add(obs.getSourceUUID());
            }
            if (obs.getTargetUUID() != null) {
                entityIds.add(obs.getTargetUUID());
            }
        }
        for (UUID entityId : entityIds) {
            ensureRecord(entityId);
        }
    }

    private void ensureRecord(UUID entityId) {
        relationships.putIfAbsent(entityId, new RelationshipRecord(entityId, Instant.now()));
    }

    public Collection<RelationshipRecord> getRelationships() {
        return Collections.unmodifiableCollection(relationships.values());
    }

    public void clear() {
        relationships.clear();
    }
}
