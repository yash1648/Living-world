package com.aman.ainpc.event;

import com.aman.ainpc.runtime.processing.correlation.ContextCorrelationResult;

import java.util.List;

public class EventResult {

    private final ContextCorrelationResult originalContextResult;
    private final List<Event> events;

    public EventResult(ContextCorrelationResult originalContextResult, List<Event> events) {
        this.originalContextResult = originalContextResult;
        this.events = List.copyOf(events);
    }

    public ContextCorrelationResult getOriginalContextResult() {
        return originalContextResult;
    }

    public List<Event> getEvents() {
        return events;
    }
}
