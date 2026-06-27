# Task Ledger — Living World Engine (LWE)

## Project Status
```
Product:  Living World Engine (LWE) for Minecraft 1.20.1
Phase:    1 — Foundation (Built) → 2 — Mind (Starting)
Complete: ██████░░░░░░░░░░░░░░░░ 30% (est.)
```

## Phase Overview

| Phase | Name | Status | % |
|-------|------|--------|---|
| 1 | Foundation | ✅ BUILT | 100% |
| 2 | Mind | 🔜 STARTING | 0% |
| 3 | Behavior | 📋 Planned | 0% |
| 4 | Conversation | 📋 Planned | 0% |
| 5 | Society | 📋 Planned | 0% |
| 6 | Civilization | 📋 Planned | 0% |

---

## Phase 1 ✅ — Foundation (COMPLETE)

The runtime pipeline is fully connected and operational.

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

**What works end-to-end:**
```
Entity spawns → onAddedToWorld() → Runtime registered
→ tick() → scanSurroundings() → PLAYER_SEEN observations
→ AgentRuntime.tick() → pipeline → LifeHistory + RelationshipManager
→ onRemovedFromWorld() → Runtime unregistered
```

---

## Phase 2 🟡 — Mind (CURRENT SPRINT)

### 🔴 Sprint Blockers (Fix Before Phase 2 Work)

| # | Task | Priority | Effort |
|---|------|----------|--------|
| B1 | **Fix ChatListener client-side API** — `Minecraft.getInstance()` blocks dedicated server. Move server logic to server handler, use packets. | 🔴 HIGH | Medium |
| B2 | **Fix LLM integration direction** — LLM should never decide gameplay. Move emotion→Needs. Conversation reads Agent state, LLM generates dialogue only. | 🔴 HIGH | Medium |
| B3 | **Expand EventType enum** — Need domain events: MET_PLAYER, BUILT_HOUSE, VILLAGE_ATTACKED, SAVED_BY_PLAYER, etc. Instead of generic UNKNOWN. | ✅ DONE | 33 types, factory maps ObservationType→EventType |

### 🟡 Phase 2 Subsystems (Build Order)

| # | Task | Description | Priority | Depends On |
|---|------|-------------|----------|------------|
| 2.1 | **Knowledge system** | EventConsumer that extracts facts from events. Fact: {subject, predicate, object, confidence, timestamp}. Immutable history, evolving knowledge. | 🔴 HIGH | B3 |
| 2.2 | **Needs system** | Replace emotion keywords with Needs: Hungry, Unsafe, Lonely, Curious, Rested. Needs drive decisions, emotions emerge naturally. | 🔴 HIGH | B2 |
| 2.3 | **Relationship enhancement** | Add trust, familiarity, respect, fear, friendship, debt scores. Positive events increment, negative decrement, decay over time. | 🔴 HIGH | 2.1 |
| 2.4 | **LifeHistory querying** | Query events by player UUID, by type, by time range. Summarization for conversation context. | 🟡 MED | B3 |
| 2.5 | **SignificanceEvaluator** | Real filtering — not all observations are significant. Rate by type, source, novelty, repetition. | 🟡 MED | 2.1 |
| 2.6 | **ContextCorrelator** | Real grouping — observations in same time/location/participants form context groups. | 🟡 MED | 2.5 |
| 2.7 | **Decision Engine** | "What should I do now?" Considers: Needs, Dreams, Knowledge, Relationships, LifeHistory, CurrentGoal. Outputs a Goal. | 🟡 MED | 2.1, 2.2, 2.3, 2.4 |
| 2.8 | **Character Profile** | Name, Birthplace, Birthday, Personality, Values, Beliefs, Talents, Dream, Habits, Occupation, Home, Family. | 🟢 LOW | 2.7 |
| 2.9 | **Dreams system** | Long-term ambition: Become Chief, Protect Village, Build Town, Get Rich. Rarely changes. Influences Decision Engine. | 🟢 LOW | 2.8 |

---

## Phase 3 📋 — Behavior (Future)

| # | Task | Description |
|---|------|-------------|
| 3.1 | **Planner** | Takes a Goal, decomposes into sequential Tasks. Never touches Minecraft. |
| 3.2 | **Task definitions** | Walk, Mine, Talk, Eat, Sleep, Harvest, Craft, Attack, Trade. Immutable task data. |
| 3.3 | **Task Queue** | Ordered queue of tasks per NPC. Processed each tick. |
| 3.4 | **Action Executor** | Only system that touches Minecraft. Converts tasks to Minecraft actions. |
| 3.5 | **Action primitives** | MoveTo, BreakBlock, PlaceBlock, AttackEntity, UseItem, etc. |

---

## Phase 4 📋 — Conversation (Future)

| # | Task | Description |
|---|------|-------------|
| 4.1 | **Conversation interface** | Reads Agent state (LifeHistory, Knowledge, Relationships, Needs). Formats for LLM context. |
| 4.2 | **LLM integration** | HTTP client to LLM backend. Sends structured state, receives dialogue only. |
| 4.3 | **Persona system** | Character profile + current state shapes dialogue tone and content. |
| 4.4 | **Memory-aware dialogue** | LLM references past events from LifeHistory and Knowledge. |

---

## Phase 5 📋 — Society (Future)

| # | Task | Description |
|---|------|-------------|
| 5.1 | **Village model** | Collection of Agents. Jobs, resources, storage. |
| 5.2 | **Job system** | Farmers, builders, guards, traders, leaders. |
| 5.3 | **Resource management** | Shared storage, crafting, consumption, production. |
| 5.4 | **Defense** | Village guards, walls, alarm systems. |
| 5.5 | **Construction** | NPCs build new houses, roads, walls over time. |
| 5.6 | **Population** | NPCs arrive, settle, form families, have children. |

---

## Phase 6 📋 — Civilization (Future)

| # | Task | Description |
|---|------|-------------|
| 6.1 | **Kingdom model** | Collection of villages/settlements. |
| 6.2 | **Trade routes** | Between villages, resource exchange. |
| 6.3 | **Diplomacy** | Alliances, treaties, conflicts. |
| 6.4 | **Wars** | Village vs village, NPC armies. |
| 6.5 | **Economy** | Currency, prices, supply/demand. |
| 6.6 | **Expansion** | Villages grow, new settlements founded. |
| 6.7 | **Leadership** | Chiefs, kings, elections, succession. |

---

## Current Sprint: Phase 2 Kickoff

### Sprint Goal
Build the **Knowledge subsystem** — the first Phase 2 component. It's the natural next step because:
1. LifeHistory already listens to events → Knowledge will too
2. Knowledge is required by everything below it (Decision Engine, Conversation)
3. It's a clean EventConsumer with no dependencies on other Phase 2 systems

### Sprint Tasks
| # | Task | Status | Est. Effort |
|---|------|--------|-------------|
| B1 | Fix ChatListener client/server | TODO | Medium |
| B2 | Realign LLM integration direction | TODO | Medium |
| B3 | Expand EventType enum | ✅ DONE | 33 types, factory maps ObservationType→EventType, metadata carries through |
| **2.1** | **Build Knowledge system** | **NEXT** | Medium |
| 2.2 | Build Needs system | TODO | Medium |
| 2.3 | Enhance Relationships | TODO | Medium |

### Next Action
**2.1 — Build Knowledge system** (first Phase 2 subsystem):
- EventConsumer that extracts facts from typed events
- Fact format: {subject, predicate, object, confidence, timestamp}
- Routes on EventType: MET_PLAYER, ITEM_OBSERVED, BLOCK_CHANGED, etc.
- Depends on: B3 (done), EventType enum (done)
