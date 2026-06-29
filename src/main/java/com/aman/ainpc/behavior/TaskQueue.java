package com.aman.ainpc.behavior;

import com.aman.ainpc.behavior.task.Task;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Ordered queue of tasks for one NPC.
 *
 * Loaded by the Planner, consumed tick-by-tick by the ActionExecutor.
 * Thread-safe not required — always accessed from the server tick thread.
 */
public class TaskQueue {

    private final Deque<Task> queue = new ArrayDeque<>();

    /** Replace the current queue with a fresh task list. */
    public void load(List<Task> tasks) {
        queue.clear();
        if (tasks != null) {
            queue.addAll(tasks);
        }
    }

    /** Peek at the current head without removing it. */
    public Task peek() {
        return queue.peek();
    }

    /** Remove and return the current head task, or null if empty. */
    public Task poll() {
        return queue.poll();
    }

    /** True when there are no tasks pending. */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /** Cancel all pending tasks. */
    public void clear() {
        queue.clear();
    }

    /** Number of tasks currently queued. */
    public int size() {
        return queue.size();
    }
}
