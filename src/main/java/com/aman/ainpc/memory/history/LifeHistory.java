package com.aman.ainpc.memory.history;

import com.aman.ainpc.event.Event;
import com.aman.ainpc.event.EventType;
import com.aman.ainpc.event.dispatch.EventConsumer;
import com.aman.ainpc.perception.Observation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Append-only log of every event this NPC has lived through.
 *
 * Phase 2.4 — added querying by type, entity UUID, and time range,
 * plus summarization for LLM conversation context.
 */
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

    // ── Queries ───────────────────────────────────────────────────

    public List<LifeHistoryEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /** Returns all entries of a specific event type. */
    public List<LifeHistoryEntry> queryByType(EventType type) {
        return entries.stream()
                .filter(e -> e.getEvent().getType() == type)
                .collect(Collectors.toList());
    }

    /**
     * Returns entries where the given entity UUID appears as a source or target
     * in any source observation.
     */
    public List<LifeHistoryEntry> queryByEntityUUID(UUID entityUUID) {
        return entries.stream()
                .filter(e -> e.getEvent().getSourceObservations().stream()
                        .anyMatch(o -> entityUUID.equals(o.getSourceUUID())
                                    || entityUUID.equals(o.getTargetUUID())))
                .collect(Collectors.toList());
    }

    /** Returns entries recorded within the given time range. */
    public List<LifeHistoryEntry> queryByTimeRange(Instant from, Instant to) {
        return entries.stream()
                .filter(e -> !e.getRecordedAt().isBefore(from) && !e.getRecordedAt().isAfter(to))
                .collect(Collectors.toList());
    }

    /** Returns the N most recent entries. */
    public List<LifeHistoryEntry> recent(int n) {
        int start = Math.max(0, entries.size() - n);
        return Collections.unmodifiableList(entries.subList(start, entries.size()));
    }

    /**
     * Returns a short comma-separated text summary of recent event types,
     * suitable for including as LLM context during conversation.
     */
    public String summarize(int maxEntries) {
        if (entries.isEmpty()) return "No history yet.";
        return recent(maxEntries).stream()
                .map(e -> e.getEvent().getType().name().toLowerCase().replace("_", " "))
                .collect(Collectors.joining(", "));
    }

    /**
     * Builds a rich narrative summary for LLM conversation context.
     * Uses observation metadata where available.
     */
    public String buildNarrativeSummary(int maxEntries) {
        if (entries.isEmpty()) return "I have not experienced much yet.";
        return recent(maxEntries).stream()
                .map(e -> describeEvent(e.getEvent()))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(". "));
    }

    public int size() { return entries.size(); }

    public void clear() { entries.clear(); }

    // ── Helpers ───────────────────────────────────────────────────

    private String describeEvent(Event event) {
        String playerName = firstPlayerName(event);
        return switch (event.getType()) {
            case MET_PLAYER       -> "I met a player" + (playerName != null ? " named " + playerName : "");
            case PLAYER_ATTACKED_NPC -> "I was attacked" + (playerName != null ? " by " + playerName : "");
            case PLAYER_GAVE_ITEM -> "Someone gave me " + event.getMetadata().getOrDefault("item", "something");
            case CONVERSATION_HAD -> "I had a conversation";
            case ITEM_OBSERVED    -> "I saw " + event.getMetadata().getOrDefault("item", "an item");
            case BLOCK_CHANGED    -> "I noticed a block change nearby";
            case ENTITY_DAMAGED   -> "I witnessed something getting hurt";
            case DAY_CHANGED      -> "A new day began";
            default               -> "";
        };
    }

    private String firstPlayerName(Event event) {
        for (Observation obs : event.getSourceObservations()) {
            String name = obs.getMetadata().get("name");
            if (name != null) return name;
        }
        return null;
    }
}
