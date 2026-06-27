package com.aman.ainpc.runtime.processing.correlation;

import com.aman.ainpc.perception.Observation;
import com.aman.ainpc.runtime.processing.SignificanceResult;

import java.util.List;

public class ContextCorrelator {

    public ContextCorrelationResult correlate(SignificanceResult significanceResult) {
        List<List<Observation>> groups = significanceResult.getSignificant()
                .stream()
                .map(List::of)
                .toList();
        return new ContextCorrelationResult(significanceResult, groups);
    }
}
