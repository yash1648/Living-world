package com.aman.ainpc.runtime.processing.correlation;

import com.aman.ainpc.perception.Observation;
import com.aman.ainpc.runtime.processing.SignificanceResult;

import java.util.List;

public class ContextCorrelationResult {

    private final SignificanceResult originalSignificanceResult;
    private final List<List<Observation>> contextGroups;

    public ContextCorrelationResult(SignificanceResult originalSignificanceResult, List<List<Observation>> contextGroups) {
        this.originalSignificanceResult = originalSignificanceResult;
        this.contextGroups = contextGroups.stream().map(List::copyOf).toList();
    }

    public SignificanceResult getOriginalSignificanceResult() {
        return originalSignificanceResult;
    }

    public List<List<Observation>> getContextGroups() {
        return contextGroups;
    }
}
