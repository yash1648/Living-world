package com.aman.ainpc.needs;

import com.aman.ainpc.event.Event;
import com.aman.ainpc.event.dispatch.EventConsumer;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manages all NPC needs and their changes over time.
 *
 * Implements EventConsumer — events update needs immediately.
 * tick() is called each game tick to apply time-based decay/drift.
 */
public class NeedsManager implements EventConsumer {

    private static final float TICK_RATE = 1.0f / 20.0f; // ~1 second per 20 ticks

    private final Map<NeedType, Need> needs = new EnumMap<>(NeedType.class);

    public NeedsManager() {
        for (NeedType type : NeedType.values()) {
            needs.put(type, new Need(type));
        }
    }

    // ── EventConsumer ─────────────────────────────────────────────

    @Override
    public void accept(Event event) {
        switch (event.getType()) {
            // Meeting someone reduces loneliness
            case MET_PLAYER, NPC_ENCOUNTERED, CONVERSATION_HAD, NPC_GREETED ->
                    needs.get(NeedType.LONELINESS).decrease(0.15f);

            // Danger and combat increase fear, reduce safety
            case PLAYER_ATTACKED_NPC, NPC_HURT, ENTITY_DAMAGED, ENTITY_KILLED ->
                    needs.get(NeedType.SAFETY).increase(0.25f);

            // Interesting things reduce curiosity need
            case ITEM_OBSERVED, BLOCK_CHANGED ->
                    needs.get(NeedType.CURIOSITY).decrease(0.08f);

            // Receiving items helps hunger slightly
            case PLAYER_GAVE_ITEM ->
                    needs.get(NeedType.HUNGER).decrease(0.1f);

            // Day/night cycles affect rest
            case DAY_CHANGED -> {
                needs.get(NeedType.REST).increase(0.1f);   // daytime is energizing
                needs.get(NeedType.CURIOSITY).increase(0.05f);
            }

            // Helping someone or achieving goals reduces loneliness and boosts purpose
            case NPC_HELPED -> {
                needs.get(NeedType.LONELINESS).decrease(0.2f);
                needs.get(NeedType.REST).decrease(0.05f); // helping is tiring
            }

            default -> {}
        }
    }

    // ── Time-based decay ──────────────────────────────────────────

    /**
     * Called every game tick (from AgentRuntime). Slowly drifts needs toward
     * their natural state (hunger grows, loneliness grows, etc.).
     */
    public void tick() {
        // Hunger grows over time
        needs.get(NeedType.HUNGER).increase(0.0005f);

        // Loneliness grows when not interacting
        needs.get(NeedType.LONELINESS).increase(0.0003f);

        // Safety gradually recovers when not in danger
        needs.get(NeedType.SAFETY).decrease(0.0004f);

        // Curiosity slowly builds up
        needs.get(NeedType.CURIOSITY).increase(0.0002f);

        // Rest depletes slowly while active
        needs.get(NeedType.REST).decrease(0.0003f);
    }

    // ── Queries ───────────────────────────────────────────────────

    public Need getNeed(NeedType type) {
        return needs.get(type);
    }

    public Map<NeedType, Need> getAllNeeds() {
        return Collections.unmodifiableMap(needs);
    }

    /**
     * Returns the most urgent need (highest urgency value, adjusted for REST inversion).
     */
    public Need getMostUrgent() {
        return needs.values().stream()
                .max(Comparator.comparingDouble(need -> switch (need.getType()) {
                    case REST -> 1.0f - need.getValue(); // low rest = high urgency
                    default   -> need.getValue();
                }))
                .orElse(needs.get(NeedType.HUNGER));
    }

    public List<Need> getUrgentNeeds() {
        return needs.values().stream()
                .filter(Need::isUrgent)
                .collect(Collectors.toList());
    }

    public String summarize() {
        return Arrays.stream(NeedType.values())
                .map(t -> needs.get(t).toString())
                .collect(Collectors.joining(", "));
    }
}
