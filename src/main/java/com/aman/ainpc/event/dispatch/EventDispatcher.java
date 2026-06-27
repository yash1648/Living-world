package com.aman.ainpc.event.dispatch;

import com.aman.ainpc.event.Event;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

public class EventDispatcher {

    private final List<EventConsumer> consumers = new ArrayList<>();

    public void register(EventConsumer consumer) {
        consumers.add(consumer);
    }

    public void unregister(EventConsumer consumer) {
        consumers.remove(consumer);
    }

    public void dispatch(Collection<Event> events) {
        for (Event event : events) {
            for (EventConsumer consumer : consumers) {
                consumer.accept(event);
            }
        }
    }
}
