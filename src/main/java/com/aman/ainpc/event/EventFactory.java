package com.aman.ainpc.event;

import com.aman.ainpc.perception.Observation;
import com.aman.ainpc.perception.ObservationType;
import com.aman.ainpc.runtime.processing.correlation.ContextCorrelationResult;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Creates domain Events from context-correlated observation groups.
 *
 * Each group of observations is classified into an EventType based on
 * the types of observations it contains. Observation metadata is
 * carried through to the Event so downstream consumers (LifeHistory,
 * Knowledge, Relationships) can access it.
 */
public class EventFactory {

    /**
     * Converts context-correlated observation groups into typed Events.
     */
    public EventResult createEvents(ContextCorrelationResult contextResult) {
        List<Event> events = contextResult.getContextGroups()
                .stream()
                .map(this::buildEvent)
                .toList();
        return new EventResult(contextResult, events);
    }

    /**
     * Builds a single Event from a group of correlated observations.
     */
    private Event buildEvent(List<Observation> group) {
        return new Event(
                UUID.randomUUID(),
                Instant.now(),
                classifyEventType(group),
                group,
                mergeMetadata(group)
        );
    }

    // ── Event Type Classification ──────────────────────────────────

    /**
     * Determines the dominant EventType for a group of observations.
     *
     * Uses the first observation's type as the primary classifier.
     * If the group is empty, returns UNKNOWN.
     */
    private static EventType classifyEventType(List<Observation> group) {
        if (group == null || group.isEmpty()) {
            return EventType.UNKNOWN;
        }
        ObservationType obsType = group.get(0).getType();
        EventType eventType = OBSERVATION_TO_EVENT.get(obsType);
        return eventType != null ? eventType : EventType.UNKNOWN;
    }

    private static final Map<ObservationType, EventType> OBSERVATION_TO_EVENT = new HashMap<>();

    static {
        // Entity observations
        OBSERVATION_TO_EVENT.put(ObservationType.PLAYER_SEEN, EventType.MET_PLAYER);
        OBSERVATION_TO_EVENT.put(ObservationType.NPC_SEEN, EventType.NPC_ENCOUNTERED);
        OBSERVATION_TO_EVENT.put(ObservationType.ITEM_SEEN, EventType.ITEM_OBSERVED);

        // Environment observations
        OBSERVATION_TO_EVENT.put(ObservationType.DAY_STARTED, EventType.DAY_CHANGED);
        OBSERVATION_TO_EVENT.put(ObservationType.NIGHT_STARTED, EventType.DAY_CHANGED);

        // World observations
        OBSERVATION_TO_EVENT.put(ObservationType.BLOCK_CHANGED, EventType.BLOCK_CHANGED);

        // Combat observations
        OBSERVATION_TO_EVENT.put(ObservationType.ENTITY_DAMAGED, EventType.ENTITY_DAMAGED);

        // Communication observations
        OBSERVATION_TO_EVENT.put(ObservationType.CHAT_HEARD, EventType.CONVERSATION_HAD);

        // Fallback
        OBSERVATION_TO_EVENT.put(ObservationType.UNKNOWN, EventType.UNKNOWN);
    }

    // ── Metadata Merging ───────────────────────────────────────────

    /**
     * Merges metadata from all observations in the group into a single map.
     *
     * Later observations with the same key overwrite earlier ones.
     * Adds an event-level metadata key "event_observation_count".
     */
    private static Map<String, String> mergeMetadata(List<Observation> group) {
        if (group == null || group.isEmpty()) {
            return Map.of("event_observation_count", "0");
        }
        Map<String, String> merged = new HashMap<>();
        merged.put("event_observation_count", String.valueOf(group.size()));
        for (Observation obs : group) {
            if (obs.getMetadata() != null) {
                merged.putAll(obs.getMetadata());
            }
        }
        return Map.copyOf(merged);
    }
}
