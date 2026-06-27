package com.aman.ainpc.runtime.processing;

import com.aman.ainpc.perception.Observation;
import com.aman.ainpc.perception.PerceptionSnapshot;

import java.util.List;

public class SignificanceResult {

    private final PerceptionSnapshot originalSnapshot;
    private final List<Observation> significant;

    public SignificanceResult(PerceptionSnapshot originalSnapshot, List<Observation> significant) {
        this.originalSnapshot = originalSnapshot;
        this.significant = List.copyOf(significant);
    }

    public PerceptionSnapshot getOriginalSnapshot() {
        return originalSnapshot;
    }

    public List<Observation> getSignificant() {
        return significant;
    }
}
