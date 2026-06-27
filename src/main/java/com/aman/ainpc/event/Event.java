package com.aman.ainpc.event;

import com.aman.ainpc.perception.Observation;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Event {

    private final UUID eventId;
    private final Instant timestamp;
    private final EventType type;
    private final List<Observation> sourceObservations;
    private final Map<String, String> metadata;

    public Event(UUID eventId, Instant timestamp, EventType type, List<Observation> sourceObservations, Map<String, String> metadata) {
        this.eventId = eventId;
        this.timestamp = timestamp;
        this.type = type;
        this.sourceObservations = List.copyOf(sourceObservations);
        this.metadata = metadata != null ? Collections.unmodifiableMap(metadata) : Map.of();
    }

    public UUID getEventId() {
        return eventId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public EventType getType() {
        return type;
    }

    public List<Observation> getSourceObservations() {
        return sourceObservations;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }
}
