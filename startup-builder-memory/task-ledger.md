# Task Ledger — Living World Engine (LWE)

## Project Status
```
Product:  Living World Engine (LWE) for Minecraft 1.20.1
Phase:    3 — Behavior (COMPLETE) → 4 — Conversation (Next)
Complete: ████████████████░░░░░░ 70% (est.)
```

## Phase Overview

| Phase | Name | Status | % |
|-------|------|--------|---|
| 1 | Foundation | ✅ BUILT | 100% |
| 2 | Mind | ✅ BUILT | 100% |
| 3 | Behavior | ✅ BUILT | 100% |
| 4 | Conversation | 🔜 NEXT | 0% |
| 5 | Society | 📋 Planned | 0% |
| 6 | Civilization | 📋 Planned | 0% |

---

## Phase 1 ✅ — Foundation (COMPLETE)

| Component | Status | Files |
|-----------|--------|-------|
| Runtime | ✅ | AgentRuntime, AgentRuntimeManager, AgentState, AgentTickResult |
| Perception | ✅ | Observation, ObservationType, PerceptionBuffer, PerceptionSnapshot |
| Event Pipeline | ✅ | Event, EventType, EventFactory, EventResult |
| Event Dispatcher | ✅ | EventDispatcher, EventConsumer |
| Entity Integration | ✅ | AINPCEntity (tick + lifecycle + proximity scan) |
| Mod Entry | ✅ | AINPC (@Mod, DeferredRegister, attributes) |
| Renderer | ✅ | AINPCClient (HumanoidModel) |
| Spawn Command | ✅ | /spawnnpc |

---

## Phase 2 ✅ — Mind (COMPLETE)

| # | Task | Status | Files |
|---|------|--------|-------|
| 2.1 | Knowledge System | ✅ DONE | Fact, FactExtractor, KnowledgeBase |
| 2.2 | Needs System | ✅ DONE | NeedType, Need, NeedsManager |
| 2.3 | Relationship Enhancement | ✅ DONE | RelationshipRecord (scores), RelationshipManager (event-driven) |
| 2.4 | LifeHistory Querying | ✅ DONE | queryByType, queryByEntityUUID, queryByTimeRange, summarize, buildNarrativeSummary |
| 2.5 | SignificanceEvaluator | ✅ DONE | Novelty-weighted scoring with per-type thresholds |
| 2.6 | ContextCorrelator | ✅ DONE | Groups by type+targetUUID, one group per unique world event |
| 2.7 | Decision Engine | ✅ DONE | GoalType, Goal, DecisionEngine (priority cascade) |
| 2.8 | Character Profile | ✅ DONE | CharacterProfile (deterministic generation from UUID) |
| 2.9 | Dreams System | ✅ DONE | DreamType, Dream (progress tracking) |

---

## Phase 3 ✅ — Behavior (COMPLETE)

**Full Planner → TaskQueue → ActionExecutor → GoalDrivenGoal stack built.**

| # | Task | Status | Files |
|---|------|--------|-------|
| 3.1 | **Planner** | ✅ DONE | `behavior/Planner.java` — Goal→List<Task>, pure Java |
| 3.2 | **Task definitions** | ✅ DONE | `behavior/task/TaskType.java`, `behavior/task/Task.java` |
| 3.3 | **Task Queue** | ✅ DONE | `behavior/TaskQueue.java` — ordered queue, loaded by Planner |
| 3.4 | **Action Executor** | ✅ DONE | `behavior/ActionExecutor.java` — only system touching Minecraft |
| 3.5 | **Action primitives** | ✅ DONE | Walk, Explore, Investigate, Greet, Socialize, Eat, Sleep, Work, Idle |
| 3.6 | **Goal-driven AI goal** | ✅ DONE | `behavior/GoalDrivenGoal.java` — PathfinderGoal at priority 1 |

### Architecture

```
AgentRuntime.tick()
→ DecisionEngine.decide() → Goal
→ if goal changed or queue empty: taskQueue.load(planner.plan(goal))

AINPCEntity.registerGoals()
→ Priority 0: RandomLookAroundGoal     (fallback look)
→ Priority 1: GoalDrivenGoal           ← NEW (reads taskQueue, drives ActionExecutor)
→ Priority 2: RandomStrollGoal         (fallback wander when queue empty)
→ Priority 3: LookAtPlayerGoal         (fallback look at players)

GoalDrivenGoal.tick()
→ ActionExecutor.tick(entity, taskQueue)
→ executes head task: WALK_TO, EXPLORE, INVESTIGATE, GREET, SOCIALIZE, EAT, SLEEP, WORK, IDLE
→ resolves positions from world state at task start
→ completes when nav.isDone() or maxDurationTicks elapsed
```

### Task → Goal mapping

| GoalType | Task Sequence |
|----------|---------------|
| FIND_FOOD | EXPLORE → EAT |
| SEEK_SAFETY | EXPLORE |
| REST | SLEEP |
| SOCIALIZE | SOCIALIZE |
| GREET_PLAYER | GREET |
| EXPLORE | EXPLORE |
| INVESTIGATE | INVESTIGATE |
| PURSUE_DREAM | EXPLORE → WORK |
| DO_WORK | WORK |
| IDLE | IDLE |

---

## Phase 4 📋 — Conversation (NEXT SPRINT)

| # | Task | Description |
|---|------|-------------|
| 4.1 | **Conversation context** | Reads Agent state (LifeHistory, Knowledge, Relationships, Needs). Formats for LLM. |
| 4.2 | **Memory-aware dialogue** | LLM references past events from LifeHistory.buildNarrativeSummary(). |
| 4.3 | **Persona system** | CharacterProfile shapes dialogue tone. |
| 4.4 | **Conversation history** | Per-conversation message history for multi-turn dialogue. |

### Next Action
**4.1 — Build Conversation Context formatter:**
- Reads `AgentRuntime` state: NeedsManager, KnowledgeBase, RelationshipManager, LifeHistory, CharacterProfile
- Builds a structured system prompt string for the LLM
- No direct LLM call here — just the context formatter that ConversationHandler uses

---

## Phase 5 📋 — Society (Future)

| # | Task | Description |
|---|------|-------------|
| 5.1 | Village model | Collection of Agents. Jobs, resources, storage. |
| 5.2 | Job system | Farmers, builders, guards, traders, leaders. |
| 5.3 | Resource management | Shared storage, crafting, consumption, production. |
| 5.4 | Defense | Village guards, walls, alarm systems. |
| 5.5 | Construction | NPCs build new houses, roads, walls over time. |
| 5.6 | Population | NPCs arrive, settle, form families, have children. |

---

## Phase 6 📋 — Civilization (Future)

| # | Task | Description |
|---|------|-------------|
| 6.1 | Kingdom model | Collection of villages/settlements. |
| 6.2 | Trade routes | Between villages, resource exchange. |
| 6.3 | Diplomacy | Alliances, treaties, conflicts. |
| 6.4 | Wars | Village vs village, NPC armies. |
| 6.5 | Economy | Currency, prices, supply/demand. |
| 6.6 | Expansion | Villages grow, new settlements founded. |
| 6.7 | Leadership | Chiefs, kings, elections, succession. |
