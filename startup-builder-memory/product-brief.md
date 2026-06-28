# Product Brief: Living World Engine (LWE)

## Vision Statement
> "The goal is not to create intelligent NPCs. The goal is to create a world players remember."

Living World Engine is a Minecraft AI framework where NPCs are not scripted characters or chatbots. Every NPC is an autonomous Agent that lives independently, remembers meaningful events, builds relationships, learns about the world, has dreams, has needs, makes decisions, executes plans, and changes the world over time.

The player is entering an already living world. The world should continue evolving even if the player never interacts with it.

## Ultimate Goal
A player should be able to leave a village for 100 Minecraft days and return to discover:
- New houses, new citizens, new roads
- New leaders, new stories
- NPCs remembering previous encounters
- Nothing scripted — everything happened because of simulation

## Core Philosophy

| Principle | Meaning |
|-----------|---------|
| Events, not conversations | The world remembers events, not chat logs. NPCs remember meaning, not dialogue. |
| Conversation is an interface, not intelligence | The player talks to an Agent, not an LLM. LLM generates dialogue only — never decides gameplay. |
| No global NPC brain | Every NPC is independent with its own Runtime, Memory, Knowledge, Relationships, Needs, and Dreams. |
| Event-driven architecture | Every subsystem reacts to Events. No subsystem communicates directly with another. |
| Single responsibility | Every stage in the pipeline has one job. No stage modifies another stage's data. |

## Development Phases

### Phase 1 ✅ — Foundation
- Runtime (AgentRuntime, AgentRuntimeManager, AgentState)
- Perception (Observation, PerceptionBuffer, PerceptionSnapshot)
- Event Pipeline (Event, EventFactory, EventResult)
- Event Dispatcher (EventDispatcher, EventConsumer)

### Phase 2 ⬅️ — Mind (NEXT)
- Life History (append-only event store)
- Knowledge (fact storage)
- Relationships (trust, familiarity, respect, fear, debt)
- Decision Engine (considers needs, dreams, knowledge, relationships, history)
- Needs (replace emotions: hungry, unsafe, lonely, curious, rested)

### Phase 3 — Behavior
- Planner (creates plans, never touches Minecraft)
- Tasks (Walk, Mine, Talk, Eat, Sleep, Harvest, Craft)
- Actions (only Actions touch Minecraft)

### Phase 4 — Conversation
- LLM integration (personas, memory-aware dialogue)
- **LLM generates dialogue only** — never moves entities, modifies blocks, creates memories, or controls Minecraft

### Phase 5 — Society
- Villages (jobs, resources, storage, defense, construction, population)

### Phase 6 — Civilization
- Kingdoms (trade, diplomacy, wars, taxes, economy, expansion)

## What We Are NOT Building
- AGI, self-modifying AI, unlimited autonomous agents
- GPT controlling Minecraft directly
- NPCs that magically know everything
- Chatbots — conversation is an interface, not intelligence

## Character Model

Every NPC Agent owns:
- **Character Profile** (mostly immutable): Name, Birthplace, Birthday, Personality, Values, Beliefs, Talents, Dream, Habits, Occupation, Home, Family
- **Runtime**: Lifecycle state machine
- **Perception**: Buffer of observations
- **Life History**: Append-only event log
- **Knowledge**: Evolving facts about the world
- **Relationships**: Trust, familiarity, respect, fear, debt, history
- **Needs**: Hungry, Unsafe, Lonely, Curious, Rested
- **Dreams**: Long-term ambition (rarely changes)
- **Decision Engine**: "What should I do now?"
- **Planner**: Creates Plans from goals
- **Task Queue**: Behaviors as Tasks
- **Action Executor**: Only Actions touch Minecraft
- **Conversation State**: Current dialogue context

## Technical Stack
- **Platform**: Minecraft Forge 1.20.1
- **Language**: Java 17
- **Build**: Gradle (ForgeGradle 6.0.26)
- **Package**: `com.aman.ainpc`
- **LLM Backend**: External service (no direct game control)

## Current State vs Vision

| Component | Vision Requires | Current Implementation |
|-----------|----------------|----------------------|
| Runtime | Per-NPC lifecycle, runs every tick | ✅ AgentRuntime, called via AINPCEntity.tick() |
| Perception | Observations from environment | ✅ PLAYER_SEEN via proximity scan (needs more types) |
| Event Pipeline | Events from correlated observations | ✅ EventFactory → EventDispatcher |
| Event Dispatch | Multiple consumers, no direct coupling | ✅ LifeHistory + RelationshipManager as consumers |
| Life History | Append-only event store | ⚠️ Records events but no querying/summarization |
| Knowledge | Facts about the world | ❌ Not built |
| Relationships | Trust, familiarity, respect, fear | ⚠️ Tracks UUIDs + first interaction only |
| Needs | Hungry, Unsafe, Lonely, Curious, Rested | ❌ Not built (uses emotion keywords — must replace) |
| Decision Engine | "What should I do now?" | ❌ Not built |
| Planner | Creates plans, never touches Minecraft | ❌ Not built |
| Tasks | Walk, Mine, Talk, Eat, Sleep | ❌ Not built |
| Actions | Only system that touches Minecraft | ❌ Not built |
| Conversation | LLM generates dialogue from Agent state | ⚠️ ChatListener calls LLM directly (wrong direction) |

## Known Issues / Misalignments
1. **ChatListener uses client-side API** — `Minecraft.getInstance()` blocks dedicated server support
2. **ChatListener's LLM integration wrong** — LLM currently "controls" NPC emotions; vision says LLM generates dialogue only, never decides gameplay
3. **NPCInteractionHandler checks hardcoded name** — `getName().getString().equals("AI_NPC")`
4. **Needs vs Emotions** — Current code uses emotion keywords (smile, laugh, wink); vision says replace with Needs system
5. **EventType only has UNKNOWN** — Need domain-specific event types (MET_PLAYER, BUILT_HOUSE, VILLAGE_ATTACKED, etc.)
6. **SignificanceEvaluator is pass-through** — No actual significance filtering
7. **ContextCorrelator is pass-through** — Groups each observation individually
8. **Config.java has placeholder demo values** — Not aligned with LWE configuration needs
