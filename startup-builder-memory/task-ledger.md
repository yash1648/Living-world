# Task Ledger — Living World Engine (LWE)

## Project Status
```
Product:  Living World Engine (LWE) for Minecraft 1.20.1
Phase:    2 — Mind (COMPLETE) → 3 — Behavior (Next)
Complete: ████████████░░░░░░░░░░ 55% (est.)
```

## Phase Overview

| Phase | Name | Status | % |
|-------|------|--------|---|
| 1 | Foundation | ✅ BUILT | 100% |
| 2 | Mind | ✅ BUILT | 100% |
| 3 | Behavior | 🔜 NEXT | 0% |
| 4 | Conversation | 📋 Planned | 0% |
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

### Sprint Blockers — Fixed

| # | Task | Status | Notes |
|---|------|--------|-------|
| B1 | Fix ChatListener client-side API | ✅ DONE | Moved to ConversationHandler (ServerChatEvent server-side) |
| B2 | Realign LLM integration direction | ✅ DONE | LLM generates dialogue only; Needs system drives particles/emotions |
| B3 | Expand EventType enum | ✅ DONE (Phase 1) | 33 types, EventFactory maps ObservationType→EventType |

### Phase 2 Subsystems — Built

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

### Bonus — Architecture Fixes

| # | Fix | Status | Notes |
|---|-----|--------|-------|
| F1 | NPCInteractionHandler | ✅ DONE | Now checks `AINPCEntity` (not hardcoded name "AI_NPC") |
| F2 | ConversationManager | ✅ DONE | Server-side singleton, ConcurrentHashMap, toggle on/off |
| F3 | ConversationHandler | ✅ DONE | ServerChatEvent interceptor, AI call on background thread |
| F4 | Config.java | ✅ DONE | AI endpoint, scan interval, scan range, debug goals flag |
| F5 | AINPCEntity display name | ✅ DONE | Name set from CharacterProfile on spawn |
| F6 | Need-driven particle emotions | ✅ DONE | Safety→angry, loneliness satisfied→heart, hunger→splash |
| F7 | Occupation personality trait | ✅ DONE | CharacterProfile includes occupation, personality, dream |

**End-to-end flow:**
```
Player right-clicks AINPCEntity
→ NPCInteractionHandler starts conversation in ConversationManager
→ Player types in chat
→ ConversationHandler (ServerChatEvent) intercepts
→ Calls AI on background thread with character system prompt + needs + knowledge
→ AI returns dialogue only
→ NeedsManager determines particle effect (not LLM keyword parsing)
→ Response sent to player via sendSystemMessage
```

**Agent tick flow:**
```
AINPCEntity.tick()
→ AgentRuntime.tick()
→ NeedsManager.tick() (time-based decay)
→ PerceptionBuffer.drain() → PerceptionSnapshot
→ SignificanceEvaluator (novelty-weighted scoring, threshold=0.35)
→ ContextCorrelator (group by type+target UUID)
→ EventFactory (ObservationType→EventType mapping)
→ EventDispatcher dispatches to:
   LifeHistory (append), RelationshipManager (score updates),
   KnowledgeBase (fact extraction), NeedsManager (event-based updates)
→ DecisionEngine.decide() → current Goal
```

---

## Phase 3 📋 — Behavior (NEXT SPRINT)

### Sprint Goal
Build the **Planner → Task Queue → Action Executor** stack.
The Decision Engine already outputs Goals — Phase 3 decomposes them into executable Tasks.

| # | Task | Description | Priority | Depends On |
|---|------|-------------|----------|------------|
| 3.1 | **Planner** | Takes a Goal from DecisionEngine, decomposes into sequential Tasks. No Minecraft API access. | 🔴 HIGH | 2.7 ✅ |
| 3.2 | **Task definitions** | Walk, Mine, Talk, Eat, Sleep, Harvest, Craft, Attack, Trade. Immutable task data. | 🔴 HIGH | 3.1 |
| 3.3 | **Task Queue** | Ordered queue of tasks per NPC. Processed each tick. Cancellable. | 🔴 HIGH | 3.2 |
| 3.4 | **Action Executor** | ONLY system that touches Minecraft. Converts Tasks to Minecraft actions. | 🔴 HIGH | 3.3 |
| 3.5 | **Action primitives** | MoveTo, BreakBlock, PlaceBlock, AttackEntity, UseItem, etc. | 🟡 MED | 3.4 |
| 3.6 | **Goal-driven AI goals** | Replace generic PathfinderMob goals with ones driven by the current Goal. | 🟡 MED | 3.1 |

### Next Action
**3.1 — Build Planner** (first Phase 3 component):
- Takes a `Goal` from `DecisionEngine`
- Routes on `GoalType` to select a task sequence
- Returns a `List<Task>` without touching Minecraft
- Example: `FIND_FOOD → [Walk(nearest_food_source), Harvest(food), Eat(food)]`

---

## Phase 4 📋 — Conversation (Future)

| # | Task | Description |
|---|------|-------------|
| 4.1 | **Conversation context** | Reads Agent state (LifeHistory, Knowledge, Relationships, Needs). Formats for LLM. |
| 4.2 | **Memory-aware dialogue** | LLM references past events from LifeHistory.buildNarrativeSummary(). |
| 4.3 | **Persona system** | CharacterProfile shapes dialogue tone. |
| 4.4 | **Conversation history** | Per-conversation message history for multi-turn dialogue. |

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
