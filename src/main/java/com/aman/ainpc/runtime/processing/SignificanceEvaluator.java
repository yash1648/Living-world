package com.aman.ainpc.runtime.processing;

import com.aman.ainpc.perception.Observation;
import com.aman.ainpc.perception.PerceptionSnapshot;

import java.util.List;

public class SignificanceEvaluator {

    public SignificanceResult evaluate(PerceptionSnapshot snapshot) {
        List<Observation> all = snapshot.getObservations();
        return new SignificanceResult(snapshot, List.copyOf(all));
    }
}
