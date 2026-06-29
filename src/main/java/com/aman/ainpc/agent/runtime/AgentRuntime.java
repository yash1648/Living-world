package com.aman.ainpc.agent.runtime;

import com.aman.ainpc.character.CharacterProfile;
import com.aman.ainpc.decision.DecisionEngine;
import com.aman.ainpc.decision.Goal;
import com.aman.ainpc.event.EventFactory;
import com.aman.ainpc.event.EventResult;
import com.aman.ainpc.event.dispatch.EventDispatcher;
import com.aman.ainpc.knowledge.KnowledgeBase;
import com.aman.ainpc.memory.history.LifeHistory;
import com.aman.ainpc.needs.NeedsManager;
import com.aman.ainpc.perception.PerceptionBuffer;
import com.aman.ainpc.perception.PerceptionSnapshot;
import com.aman.ainpc.relationship.RelationshipManager;
import com.aman.ainpc.runtime.processing.SignificanceEvaluator;
import com.aman.ainpc.runtime.processing.SignificanceResult;
import com.aman.ainpc.runtime.processing.correlation.ContextCorrelationResult;
import com.aman.ainpc.runtime.processing.correlation.ContextCorrelator;

import java.util.UUID;

/**
 * The core agent runtime for a single NPC.
 *
 * Each tick: Perceive → Significance → Correlate → Event → Dispatch
 *   ↓ All subsystems consume dispatched events:
 *   LifeHistory, RelationshipManager, KnowledgeBase, NeedsManager
 *   ↓ DecisionEngine decides the current Goal from all subsystem state
 *
 * Phase 2 wiring complete:
 *   - KnowledgeBase (2.1) — semantic fact extraction
 *   - NeedsManager  (2.2) — drive behavior, replace hardcoded emotions
 *   - RelationshipManager enhanced (2.3) — trust/fear/friendship scores
 *   - LifeHistory querying (2.4) — query by type/UUID/time, summarize
 *   - SignificanceEvaluator real logic (2.5) — novelty scoring
 *   - ContextCorrelator real logic (2.6) — observation grouping
 *   - DecisionEngine (2.7) — goal output
 *   - CharacterProfile (2.8) — name, personality, occupation
 *   - Dream system (2.9) — long-term ambition
 */
public class AgentRuntime {

    private final AgentState state;
    private final PerceptionBuffer perceptionBuffer;
    private final SignificanceEvaluator significanceEvaluator;
    private final ContextCorrelator contextCorrelator;
    private final EventFactory eventFactory;
    private final EventDispatcher eventDispatcher;

    // ── Phase 1 subsystems ────────────────────────────────────────
    private final LifeHistory lifeHistory;
    private final RelationshipManager relationshipManager;

    // ── Phase 2 subsystems ────────────────────────────────────────
    private final KnowledgeBase knowledgeBase;
    private final NeedsManager needsManager;
    private final CharacterProfile characterProfile;
    private final DecisionEngine decisionEngine;
    private Goal currentGoal;

    public AgentRuntime(UUID agentUUID) {
        this.state = new AgentState(agentUUID);
        this.perceptionBuffer = new PerceptionBuffer();
        this.significanceEvaluator = new SignificanceEvaluator();
        this.contextCorrelator = new ContextCorrelator();
        this.eventFactory = new EventFactory();
        this.eventDispatcher = new EventDispatcher();

        // Phase 1
        this.lifeHistory = new LifeHistory();
        this.relationshipManager = new RelationshipManager();

        // Phase 2
        this.knowledgeBase = new KnowledgeBase();
        this.needsManager = new NeedsManager();
        this.characterProfile = CharacterProfile.generateFor(agentUUID);
        this.decisionEngine = new DecisionEngine();

        // Register all event consumers
        this.eventDispatcher.register(this.lifeHistory);
        this.eventDispatcher.register(this.relationshipManager);
        this.eventDispatcher.register(this.knowledgeBase);
        this.eventDispatcher.register(this.needsManager);
    }

    // ── Tick ─────────────────────────────────────────────────────

    public AgentTickResult tick() {
        switch (state.getLifecycle()) {
            case CREATED:
                state.setLifecycle(AgentState.Lifecycle.RUNNING);
                return AgentTickResult.SUCCESS;

            case RUNNING: {
                // 1. Tick needs (time-based drift every game tick)
                needsManager.tick();

                // 2. Perception pipeline
                PerceptionSnapshot snapshot = new PerceptionSnapshot(perceptionBuffer.drain());
                SignificanceResult significanceResult = significanceEvaluator.evaluate(snapshot);
                ContextCorrelationResult contextResult = contextCorrelator.correlate(significanceResult);
                EventResult eventResult = eventFactory.createEvents(contextResult);
                eventDispatcher.dispatch(eventResult.getEvents());

                // 3. Decide current goal based on all subsystem state
                currentGoal = decisionEngine.decide(
                        needsManager, knowledgeBase, relationshipManager, lifeHistory, characterProfile
                );

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

    // ── Accessors ─────────────────────────────────────────────────

    public AgentState getState() { return state; }
    public PerceptionBuffer getPerceptionBuffer() { return perceptionBuffer; }
    public LifeHistory getLifeHistory() { return lifeHistory; }
    public RelationshipManager getRelationshipManager() { return relationshipManager; }
    public KnowledgeBase getKnowledgeBase() { return knowledgeBase; }
    public NeedsManager getNeedsManager() { return needsManager; }
    public CharacterProfile getCharacterProfile() { return characterProfile; }
    public Goal getCurrentGoal() { return currentGoal; }
}
