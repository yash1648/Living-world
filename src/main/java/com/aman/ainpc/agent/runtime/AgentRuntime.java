package com.aman.ainpc.agent.runtime;

import com.aman.ainpc.event.EventFactory;
import com.aman.ainpc.event.EventResult;
import com.aman.ainpc.event.dispatch.EventDispatcher;
import com.aman.ainpc.memory.history.LifeHistory;
import com.aman.ainpc.perception.PerceptionBuffer;
import com.aman.ainpc.perception.PerceptionSnapshot;
import com.aman.ainpc.relationship.RelationshipManager;
import com.aman.ainpc.runtime.processing.SignificanceEvaluator;
import com.aman.ainpc.runtime.processing.SignificanceResult;
import com.aman.ainpc.runtime.processing.correlation.ContextCorrelator;
import com.aman.ainpc.runtime.processing.correlation.ContextCorrelationResult;

import java.util.UUID;

public class AgentRuntime {

    private final AgentState state;
    private final PerceptionBuffer perceptionBuffer;
    private final SignificanceEvaluator significanceEvaluator;
    private final ContextCorrelator contextCorrelator;
    private final EventFactory eventFactory;
    private final EventDispatcher eventDispatcher;
    private final LifeHistory lifeHistory;
    private final RelationshipManager relationshipManager;

    public AgentRuntime(UUID agentUUID) {
        this.state = new AgentState(agentUUID);
        this.perceptionBuffer = new PerceptionBuffer();
        this.significanceEvaluator = new SignificanceEvaluator();
        this.contextCorrelator = new ContextCorrelator();
        this.eventFactory = new EventFactory();
        this.eventDispatcher = new EventDispatcher();
        this.lifeHistory = new LifeHistory();
        this.relationshipManager = new RelationshipManager();
        this.eventDispatcher.register(this.lifeHistory);
        this.eventDispatcher.register(this.relationshipManager);
    }

    public AgentState getState() {
        return state;
    }

    public PerceptionBuffer getPerceptionBuffer() {
        return perceptionBuffer;
    }

    public AgentTickResult tick() {
        switch (state.getLifecycle()) {
            case CREATED:
                state.setLifecycle(AgentState.Lifecycle.RUNNING);
                return AgentTickResult.SUCCESS;
            case RUNNING: {
                PerceptionSnapshot snapshot = new PerceptionSnapshot(perceptionBuffer.drain());
                SignificanceResult significanceResult = significanceEvaluator.evaluate(snapshot);
                ContextCorrelationResult contextResult = contextCorrelator.correlate(significanceResult);
                EventResult eventResult = eventFactory.createEvents(contextResult);
                eventDispatcher.dispatch(eventResult.getEvents());
                return AgentTickResult.SUCCESS;
            }
            case PAUSED:
                return AgentTickResult.PAUSED;
            case STOPPED:
                return AgentTickResult.STOPPED;
            default:
                return AgentTickResult.SKIPPED;
        }
    }
}
