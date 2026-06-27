package com.aman.ainpc.memory.history;

import com.aman.ainpc.event.Event;
import com.aman.ainpc.event.dispatch.EventConsumer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LifeHistory implements EventConsumer {

    private final List<LifeHistoryEntry> entries = new ArrayList<>();

    @Override
    public void accept(Event event) {
        append(event);
    }

    public void append(Event event) {
        entries.add(new LifeHistoryEntry(event, Instant.now()));
    }

    public void appendAll(List<Event> events) {
        for (Event event : events) {
            append(event);
        }
    }

    public List<LifeHistoryEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public void clear() {
        entries.clear();
    }
}
