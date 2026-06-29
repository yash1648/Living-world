package com.aman.ainpc.runtime.processing.correlation;

import com.aman.ainpc.perception.Observation;
import com.aman.ainpc.perception.ObservationType;
import com.aman.ainpc.runtime.processing.SignificanceResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Groups related significant observations into context groups.
 *
 * Phase 2.6 — real grouping logic replaces the pass-through stub.
 *
 * Grouping strategy:
 *   Observations are grouped when they share the same ObservationType AND
 *   the same target entity UUID (if any). This collapses e.g. "player seen"
 *   observations from different ticks for the same player into one event.
 *
 *   Observations with no target UUID are each placed in their own group
 *   (they represent unique world events: block changes, day/night, etc.)
 */
public class ContextCorrelator {

    public ContextCorrelationResult correlate(SignificanceResult significanceResult) {
        List<Observation> observations = significanceResult.getSignificant();

        // Key = ObservationType + "_" + targetUUID (or unique index for target-less observations)
        Map<String, List<Observation>> groupMap = new LinkedHashMap<>();
        int uniqueIdx = 0;

        for (Observation obs : observations) {
            String key = buildKey(obs, uniqueIdx);
            if (obs.getTargetUUID() == null) {
                // Each target-less observation is unique (don't merge them)
                uniqueIdx++;
            }
            groupMap.computeIfAbsent(key, k -> new ArrayList<>()).add(obs);
        }

        List<List<Observation>> groups = new ArrayList<>(groupMap.values());
        return new ContextCorrelationResult(significanceResult, groups);
    }

    private static String buildKey(Observation obs, int uniqueIdx) {
        String typeKey = obs.getType().name();

        if (obs.getTargetUUID() != null) {
            // Group by type + target entity — merges "player X seen twice" into one event
            return typeKey + "_" + obs.getTargetUUID();
        }

        // Environmental / world events with no target → each is its own group
        return typeKey + "_unique_" + uniqueIdx;
    }
}
