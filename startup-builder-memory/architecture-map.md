# Architecture Map: Living World Engine

## Runtime Pipeline (Vision)

Every tick follows this pipeline. Each stage has one responsibility. No stage modifies another stage.

```
Observation
    ↓
Significance Evaluation
    ↓
Context Correlation
    ↓
Event Creation
    ↓
Event Dispatch
    ↓
┌──────────────────→ Life History (append-only)
│──────────────────→ Knowledge (fact extraction)
│──────────────────→ Relationships (trust, familiarity, etc.)
│──────────────────→ [Future: Settlements, Kingdoms]
    ↓
Decision Engine (What should I do now?)
    ↓
Planning (Create Plan from Goal)
    ↓
Task Execution (Queue tasks)
    ↓
Action Execution (Only Action touches Minecraft)
    ↓
End Tick
```

## Current Implementation vs Vision

### Phase 1 ✅ — Foundation (FULLY BUILT)

```
                          ┌─ PathfinderMob.tick()
                          │
                          ▼
AINPCEntity.tick() ───→ scanSurroundings()
(server only)              │
                          ▼
                   PerceptionBuffer.add(Observation)
                          │
                          ▼  AgentRuntime.tick()
                   ┌──────────────────────────────┐
                   │ PerceptionBuffer.drain()     │
                   │ → PerceptionSnapshot         │
                   │ → SignificanceEvaluator      │ ⚠️  pass-through stub
                   │ → ContextCorrelator          │ ⚠️  pass-through stub
                   │ → EventFactory               │
                   │ → EventDispatcher            │
                   │   ├── LifeHistory            │ ✅ recording events
                   │   └── RelationshipManager    │ ⚠️  basic UUID tracking
                   └──────────────────────────────┘
```

**Implemented files:**
| File | Role | Status |
|------|------|--------|
| `AgentRuntime.java` | Per-NPC tick loop | ✅ Holds all subsystems, orchestrates pipeline |
| `AgentRuntimeManager.java` | Singleton registry (ConcurrentHashMap) | ✅ register/unregister/getRuntime |
| `AgentState.java` | Lifecycle: CREATED→RUNNING→PAUSED→STOPPED | ✅ |
| `AgentTickResult.java` | Tick result enum | ✅ |
| `Observation.java` | Perception data (timestamp, type, source, target, position, metadata) | ✅ |
| `ObservationType.java` | Enum: PLAYER_SEEN, NPC_SEEN, ITEM_SEEN, DAY_STARTED, etc. | ✅ |
| `PerceptionBuffer.java` | Deque-based observation queue with drain() | ✅ |
| `PerceptionSnapshot.java` | Immutable point-in-time observation list | ✅ |
| `Event.java` | UUID + Instant + EventType + source observations + metadata | ✅ |
| `EventType.java` | Domain event types | ⚠️ Currently only UNKNOWN |
| `EventFactory.java` | Creates events from context correlation groups | ✅ |
| `EventResult.java` | Wraps context result + created events | ✅ |
| `EventDispatcher.java` | Fan-out to registered EventConsumers | ✅ |
| `EventConsumer.java` | @FunctionalInterface: accept(Event) | ✅ |
| `LifeHistory.java` | Append-only event store | ✅ Stores Events as LifeHistoryEntries |
| `LifeHistoryEntry.java` | Timestamped event record | ✅ |
| `RelationshipManager.java` | Tracks entity relationships | ⚠️ Only UUID + first interaction |
| `RelationshipRecord.java` | Per-entity relationship record | ⚠️ Needs trust/familiarity/respect/fear |
| `SignificanceEvaluator.java` | Filters significant observations | ⚠️ Pass-through stub |
| `SignificanceResult.java` | Wraps snapshot + significant observations | ✅ |
| `ContextCorrelator.java` | Groups related observations | ⚠️ Pass-through stub (each obs solo) |
| `ContextCorrelationResult.java` | Wraps significance result + context groups | ✅ |
| `AINPCEntity.java` | Entity class with runtime integration | ✅ tick() + lifecycle + proximity scan |
| `AINPC.java` | @Mod entry point, entity registration | ✅ |
| `AINPCClient.java` | Client renderer (HumanoidModel) | ✅ |

### Phase 2 ❌ — Mind (NOT BUILT)

```
Event Dispatch
    ↓
┌──────────────────→ Knowledge (EventConsumer)
│   Stores facts: "Player owns horse", "Village has blacksmith"
│   Fact format: {subject, predicate, object, confidence, timestamp}
│   Knowledge evolves. History never changes.
│
├──────────────────→ LifeHistory enhancements
│   Query: "What events happened involving this player?"
│   Summarization for conversation context
│
├──────────────────→ Relationships enhancement
│   Add: trust, familiarity, respect, fear, friendship, debt
│   Scoring: increment on positive interactions, decay over time
│
├──────────────────→ Needs subsystem
│   Values: Hungry, Unsafe, Lonely, Curious, Rested
│   Influence decision making, emerge naturally from events
│
├──────────────────→ Decision Engine
│   Input: Needs + Dreams + Knowledge + Relationships + LifeHistory
│   Output: "I should gather wood because I need to build a house"
```

### Phase 3 ❌ — Behavior (NOT STARTED)

```
Decision Engine Output (Goal)
    ↓
Planner ───→ Creates Plan (sequence of Tasks)
             Never touches Minecraft. Never modifies world.
    ↓
Task Queue ───→ Walk, Mine, Talk, Eat, Sleep, Harvest, Craft
    ↓
Action Executor ───→ Only Action touches Minecraft blocks/entities
```

### Phase 4 ❌ — Conversation (WRONG DIRECTION)

**Current (WRONG):**
```
Player chat → ClientChatEvent → HTTP POST to LLM → LLM returns reply + emotion
→ Display message + particles
→ LLM indirectly controls NPC via "emotion" keywords
```

**Vision (CORRECT):**
```
Player chat → Conversation interface reads Agent state:
              Life History + Knowledge + Relationships + Needs
              → LLM generates natural language from this state
              → Display dialogue
              → LLM NEVER decides gameplay, NEVER moves entities,
                NEVER modifies blocks, NEVER creates memories
```

### Phase 5 ❌ — Society (NOT STARTED)
- Villages (jobs, resources, storage, defense, construction, population, leadership)
- Collection of Agents with shared goals

### Phase 6 ❌ — Civilization (NOT STARTED)
- Kingdoms (collections of settlements)
- Trade, roads, diplomacy, wars, taxes, economy, expansion

## Coding Rules (from Vision)

- Every subsystem has **single responsibility**
- **No hidden side effects**
- **Immutable inputs**
- **Immutable outputs where possible**
- **No global mutable state** (AgentRuntimeManager is the exception)
- **Every object has exactly one owner**

## Critical Gaps (Priority Order)

### 🔴 Must Fix Before Phase 2
1. **ChatListener uses client-side API** — `Minecraft.getInstance()` blocks dedicated server
2. **LLM integration wrong direction** — LLM should never decide gameplay/emotions
3. **EventType enum too limited** — Need domain events (MET_PLAYER, BUILT_HOUSE, etc.)

### 🟡 Phase 2 Build
4. **Knowledge system** — EventConsumer that stores facts from events
5. **Needs system** — Replace emotion keywords with Hungry/Unsafe/Lonely/Curious/Rested
6. **Decision Engine** — "What should I do now?" considering needs + dreams + knowledge + relationships + history
7. **Relationships enhancement** — Trust, familiarity, respect, fear, debt, history
8. **LifeHistory querying** — Query events by player, by type, by time range
9. **SignificanceEvaluator** — Real filtering logic
10. **ContextCorrelator** — Real grouping logic

### 🟢 Phase 3+
11. **Planner** — Goal decomposition into task sequences
12. **Tasks** — Walk, Mine, Talk, Eat, Sleep, Harvest, Craft, Attack, Trade
13. **Actions** — Only system that touches Minecraft
14. **Character Profile** — Name, personality, values, beliefs, talents, dream
15. **Dreams** — Long-term ambition
16. **Settlements** — Jobs, resources, storage, defense
17. **Kingdoms** — Trade, diplomacy, wars
18. **World persistence** — Save/load NPC states across restarts
19. **Tests** — Zero coverage
