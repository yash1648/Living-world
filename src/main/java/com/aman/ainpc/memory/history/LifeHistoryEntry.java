package com.aman.ainpc.memory.history;

import com.aman.ainpc.event.Event;

import java.time.Instant;

public class LifeHistoryEntry {

    private final Event event;
    private final Instant recordedAt;

    public LifeHistoryEntry(Event event, Instant recordedAt) {
        this.event = event;
        this.recordedAt = recordedAt;
    }

    public Event getEvent() {
        return event;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }
}
