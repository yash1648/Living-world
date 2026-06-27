# Architecture Decisions — Living World Engine

## Active Decisions

### ADR-001: Event-Driven Agent Architecture
- **Status**: ✅ Confirmed — Live in production
- **Context**: NPCs need to process perceptions, maintain memory, and track relationships without direct coupling
- **Decision**: Event-driven pipeline: Observation → Significance → Correlation → Event → Dispatch → Consumers
- **Rationale**: Every subsystem reacts to Events. No subsystem knows another exists. LifeHistory and RelationshipManager both observe the same events without knowing about each other.
- **Consequences**: Pipeline is extendable — adding Knowledge just means registering another EventConsumer

### ADR-002: No Global NPC Brain
- **Status**: ✅ Confirmed — Already implemented
- **Context**: The vision explicitly forbids a global NPC brain
- **Decision**: Every NPC has its own AgentRuntime with independent subsystems
- **Rationale**: "Every NPC is independent. No global NPC brain exists."
- **Consequences**: AgentRuntimeManager is a registry, not a controller. Each runtime is fully self-contained.

### ADR-003: Events, Not Conversations (Phase 4)
- **Status**: ⏳ Deferred to Phase 4
- **Context**: The world remembers events, not chat logs. NPCs remember meaning, not dialogue.
- **Decision**: LifeHistory stores Events (typed, structured). Conversation interface reads LifeHistory + Knowledge + Relationships + Needs to generate dialogue. LLM generates natural language only.
- **Rationale**: LLM never decides gameplay. LLM never modifies game state. LLM only generates dialogue based on the Agent's internal state.
- **Consequences**: Current ChatListener approach (LLM controls emotions) needs to be replaced. This is a Phase 4 task.

### ADR-004: Needs Replace Emotions
- **Status**: 🔜 Planned for Phase 2
- **Context**: Vision specifies: "Needs replace emotions. Emotions emerge naturally."
- **Decision**: Replace the current emotion keyword system (smile/laugh/wink in ChatListener) with a Needs system: Hungry, Unsafe, Lonely, Curious, Rested
- **Rationale**: Needs are intrinsic, measurable, and drive decisions. Emotions are outputs, not inputs.
- **Consequences**: ChatListener's extractEmotion() must be removed. Needs system feeds into Decision Engine.

### ADR-005: Planner Never Touches Minecraft (Phase 3)
- **Status**: 📋 Planned for Phase 3
- **Context**: The vision establishes strict separation: Planner, Tasks, and Actions are separate layers
- **Decision**: Planner creates Plans (sequences of Tasks). Tasks describe behaviors. Only Actions execute Minecraft operations.
- **Rationale**: Planner never modifies the world. Tasks never modify the world. Actions do.
- **Consequences**: Clean separation of concerns. Planner can be tested without Minecraft runtime.

### ADR-006: Immutable Where Possible
- **Status**: ✅ Confirmed — Partially implemented
- **Context**: Vision requires: Immutable inputs, immutable outputs where possible, no hidden side effects
- **Decision**: Event, Observation, and result objects are immutable (List.copyOf, unmodifiableMap). Mutating state is explicit (PerceptionBuffer, LifeHistory, RelationshipManager).
- **Rationale**: Reduces bugs, makes data flow easier to trace, aligns with event sourcing patterns
- **Consequences**: Need to maintain discipline for new subsystems

## Pending Decisions

### ADR-007: Minecraft Entity Integration Strategy
- **Status**: ✅ Implemented — Current approach
- **Decision**: Each NPC is a Minecraft `Entity` (PathfinderMob subtype) with an AgentRuntime bound to the entity's UUID
- **Alternatives considered**: Capability system on existing entities, standalone data tracker
- **Rationale**: Natural Minecraft integration, entity lifecycle handles spawn/despawn, works with vanilla AI goals

### ADR-008: Knowledge Fact Model
- **Status**: ⏳ Pending implementation (Phase 2)
- **Options**:
  1. Triple store: {subject, predicate, object, confidence, timestamp}
  2. Property map per entity: Map<UUID, Map<String, String>>
  3. Graph database (overkill for Minecraft mod)
- **Recommendation**: Option 1 — triple store. Clean, queryable, extensible.

### ADR-009: Network Protocol for Chat
- **Status**: ⏳ Pending (blocker B1)
- **Context**: ChatListener uses client-side API, blocks dedicated server
- **Options**:
  1. Custom Forge packets (client → server → server handler → LLM)
  2. Server-only via ServerChatEvent
  3. Currently: Client-only via ClientChatEvent (WRONG)
- **Recommendation**: Option 1 — custom packets. Proper client→server→response flow.

## Anti-Patterns (Learned — Must Avoid)

1. **LLM deciding gameplay** — Current ChatListener lets LLM output determine NPC emotions/particles. **Must fix**: LLM should generate dialogue only.
2. **Client-side server logic** — `Minecraft.getInstance()` in server context will crash on dedicated servers. **Must fix**: Server logic on server side.
3. **Static mutable state** — `ChatListener.talkingToNPC`, `currentNPC` are static fields. Breaks when multiple NPCs exist. **Must fix**: Per-entity conversation state.
4. **Fragile string parsing** — `json.replace("{\"reply\":\"", "").replace("\"}", "")` breaks on any JSON variation. **Must fix**: Proper JSON parsing.
5. **Hardcoded name check** — `npc.getName().getString().equals("AI_NPC")` breaks if NPC is renamed. **Must fix**: Check entity type instead.
6. **Stub implementations** — SignificanceEvaluator and ContextCorrelator are pass-through. **Must fix**: Real logic in Phase 2.
