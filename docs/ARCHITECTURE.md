# Living World Vision

A Minecraft world where NPCs create believable stories through memories, relationships, jobs, settlements, and history rather than scripted quests.

# Core Design Principles

1. The world remembers events, not conversations.
2. Every NPC is an Agent.
3. Conversation is an interface, not intelligence.
4. Knowledge and Memory are different systems.
5. Planning and Execution are different systems.
6. Everything an NPC does is represented as a Task.
7. The player is always the highest authority.

# High Level Modules

**Agent** — The core loop that ties every system together for a single NPC. Each NPC runs one Agent. The Agent decides what to do next based on its goals, memories, relationships, and perceived environment.

**Conversation** — Manages dialogue state, NPC persona, emotion expression, and the interface between the player and the Agent's underlying systems. Does not contain AI logic itself.

**Memory** — Stores episodic records of what happened (events, conversations, observations). Append-only, time-stamped, decayable. Answers "what happened when."

**Knowledge** — Stores semantic facts the NPC has learned (locations, recipes, names, relationships). Queryable, updatable, shareable between NPCs. Answers "what does the NPC know."

**Perception** — Monitors the Minecraft world around the NPC and converts raw game events into meaningful observations that feed into Memory and Knowledge.

**Planner** — Generates sequences of Tasks for an Agent to achieve a goal. Operates on a separate tick from execution. May be resource-intensive and run asynchronously.

**Task** — A single, executable unit of work. Every action an NPC takes (walk to position, pick up item, talk to player, sleep) is a Task. Tasks can be composed sequentially or conditionally.

**Action** — The lowest-level primitive that directly interfaces with the Minecraft game API. A Task decomposes into one or more Actions. Actions are the atom of NPC behavior.

**Relationship** — Tracks the opinion, familiarity, and history between two NPCs or between an NPC and the player. Influences conversation tone, trading prices, and willingness to cooperate.

**Settlement** — A group of NPCs that share a location, resource pool, and collective goals. Manages job assignments, building projects, and defense.

**Persistence** — Handles saving and loading all NPC state (Memory, Knowledge, Relationships, Tasks, Settlements) across world save/load cycles.

**Networking** — Manages communication between the mod and the external AI backend (or any external service). Handles request queuing, timeouts, retries, and backpressure.

# Long Term Development Order

**Phase 0 Foundation** — Project setup, entity registration, basic command infrastructure, config system, and the core Agent loop skeleton.

**Phase 1 Conversation** — Working player-to-NPC chat with AI backend integration, emotion expression, and persona management.

**Phase 2 Memory** — Episodic memory store: NPCs remember past conversations, observed events, and player interactions. Memories decay and are prioritized.

**Phase 3 Relationships** — Relationship system between NPCs and players. Past interactions affect future conversation and behavior.

**Phase 4 Tasks** — NPCs can perform autonomous actions beyond standing and talking: walking to points of interest, picking up items, following schedules.

**Phase 5 Planning** — NPCs can create and execute multi-step plans to achieve goals: gather resources, build structures, explore.

**Phase 6 Villages** — Multiple NPCs coordinate as a settlement. Shared memory, job roles, resource pooling, and collective building projects.

**Phase 7 Kingdoms** — Multiple settlements interact. Trade, diplomacy, territorial claims, wars, and large-scale emergent history.

# Non Goals

The project is NOT trying to create AGI.

The project is NOT trying to create self-modifying AI.

The project is NOT trying to replace Minecraft gameplay.

The project exists to make Minecraft worlds feel alive.
