package com.aman.ainpc.perception;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class PerceptionBuffer {

    private final Deque<Observation> queue = new ArrayDeque<>();

    public void add(Observation observation) {
        queue.addLast(observation);
    }

    public List<Observation> drain() {
        List<Observation> result = new ArrayList<>(queue);
        queue.clear();
        return result;
    }

    public void clear() {
        queue.clear();
    }

    public int size() {
        return queue.size();
    }
}
