package com.aman.ainpc.event.dispatch;

import com.aman.ainpc.event.Event;

@FunctionalInterface
public interface EventConsumer {
    void accept(Event event);
}
