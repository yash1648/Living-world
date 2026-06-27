package com.aman.ainpc.event;

import com.aman.ainpc.perception.Observation;
import com.aman.ainpc.runtime.processing.correlation.ContextCorrelationResult;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class EventFactory {

    public EventResult createEvents(ContextCorrelationResult contextResult) {
        List<Event> events = contextResult.getContextGroups()
                .stream()
                .map(this::buildEvent)
                .toList();
        return new EventResult(contextResult, events);
    }

    private Event buildEvent(List<Observation> group) {
        return new Event(
                UUID.randomUUID(),
                Instant.now(),
                EventType.UNKNOWN,
                group,
                null
        );
    }
}
