package com.aman.ainpc.runtime.processing;

import com.aman.ainpc.perception.Observation;
import com.aman.ainpc.perception.ObservationType;
import com.aman.ainpc.perception.PerceptionSnapshot;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Filters observations by significance.
 *
 * Phase 2.5 — real filtering logic replaces the pass-through stub.
 *
 * Scoring:
 *   - Base score assigned by ObservationType (combat > chat > player > item > env)
 *   - Novelty multiplier: repeated observation types are scored lower
 *   - Observations scoring below THRESHOLD are discarded
 */
public class SignificanceEvaluator {

    private static final float THRESHOLD = 0.35f;

    /** Base significance score per observation type. */
    private static final Map<ObservationType, Float> BASE_SCORES = new EnumMap<>(ObservationType.class);

    static {
        BASE_SCORES.put(ObservationType.ENTITY_DAMAGED,  1.00f); // highest — combat
        BASE_SCORES.put(ObservationType.CHAT_HEARD,      0.90f); // communication
        BASE_SCORES.put(ObservationType.NIGHT_STARTED,   0.55f); // night is more dangerous
        BASE_SCORES.put(ObservationType.PLAYER_SEEN,     0.60f); // players are always relevant
        BASE_SCORES.put(ObservationType.NPC_SEEN,        0.50f); // other NPCs
        BASE_SCORES.put(ObservationType.BLOCK_CHANGED,   0.30f); // environmental
        BASE_SCORES.put(ObservationType.ITEM_SEEN,       0.25f); // items
        BASE_SCORES.put(ObservationType.DAY_STARTED,     0.20f); // routine cycle
        BASE_SCORES.put(ObservationType.UNKNOWN,         0.00f); // always discard
    }

    /**
     * Recent observation type counts used for novelty reduction.
     * Common types are scored lower to focus on what's new.
     */
    private final Map<ObservationType, Integer> recentCounts = new EnumMap<>(ObservationType.class);

    public SignificanceResult evaluate(PerceptionSnapshot snapshot) {
        List<Observation> significant = new ArrayList<>();

        for (Observation obs : snapshot.getObservations()) {
            float score = computeScore(obs);
            if (score >= THRESHOLD) {
                significant.add(obs);
            }
        }

        // Update novelty counters (decay all, then add current batch)
        decayCounts();
        for (Observation obs : snapshot.getObservations()) {
            recentCounts.merge(obs.getType(), 1, Integer::sum);
        }

        return new SignificanceResult(snapshot, List.copyOf(significant));
    }

    private float computeScore(Observation obs) {
        float base = BASE_SCORES.getOrDefault(obs.getType(), 0.0f);
        if (base == 0.0f) return 0.0f;

        // Novelty multiplier: the more we've seen this type recently, the lower the score
        int count = recentCounts.getOrDefault(obs.getType(), 0);
        float novelty = 1.0f / (1.0f + count * 0.4f);

        return base * novelty;
    }

    private void decayCounts() {
        recentCounts.replaceAll((k, v) -> Math.max(0, v - 1));
    }
}
