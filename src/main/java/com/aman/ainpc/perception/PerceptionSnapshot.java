package com.aman.ainpc.perception;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PerceptionSnapshot {

    private final List<Observation> observations;

    public PerceptionSnapshot(Collection<Observation> observations) {
        this.observations = List.copyOf(observations);
    }

    public List<Observation> getObservations() {
        return observations;
    }

    public boolean isEmpty() {
        return observations.isEmpty();
    }

    public int size() {
        return observations.size();
    }
}
