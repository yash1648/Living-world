# Handoff — Living World Engine (LWE)

## Session Summary

### What Was Done This Cycle
**Phase 3 — Behavior: Full Planner → TaskQueue → ActionExecutor stack**

| File | Action | Description |
|------|--------|-------------|
| `behavior/task/TaskType.java` | ✅ Created | 9 task types: WALK_TO, EXPLORE, INVESTIGATE, GREET, SOCIALIZE, EAT, SLEEP, WORK, IDLE |
| `behavior/task/Task.java` | ✅ Created | Immutable task data. Factory methods: `of(type)`, `walkTo(x,y,z)`, `ofWithDuration(type, ticks)` |
| `behavior/TaskQueue.java` | ✅ Created | Deque-backed ordered queue. `load()`, `peek()`, `poll()`, `clear()` |
| `behavior/Planner.java` | ✅ Created | Pure Java. Routes GoalType → List<Task>. No Minecraft access. |
| `behavior/ActionExecutor.java` | ✅ Created | Only Minecraft-touching class. Resolves positions, drives navigation, spawns particles. |
| `behavior/GoalDrivenGoal.java` | ✅ Created | PathfinderGoal at priority 1. Bridges AgentRuntime.taskQueue ↔ ActionExecutor. |
| `agent/runtime/AgentRuntime.java` | ✅ Updated | Added Planner, TaskQueue, `lastPlannedGoalType`. Replans when goal type changes or queue empties. |
| `AINPCEntity.java` | ✅ Updated | Added GoalDrivenGoal (priority 1), ActionExecutor. Kept RandomStrollGoal (priority 2) as fallback. |

### Build Status
```
BUILD SUCCESSFUL in 25s
1 deprecation warning (FMLJavaModLoadingContext.get() — pre-existing, not introduced here)
```

### End-to-End Tick Flow (Phase 3 complete)
```
AINPCEntity.tick()
→ scanSurroundings() → PerceptionBuffer
→ AgentRuntime.tick()
   → NeedsManager.tick()
   → Perception pipeline → EventDispatcher → all subsystems
   → DecisionEngine.decide() → Goal
   → if goal changed or queue empty: taskQueue.load(planner.plan(goal))
→ GoalDrivenGoal.tick() [via Minecraft goal selector]
   → ActionExecutor.tick(entity, taskQueue)
   → head task executed: navigate, look, idle, etc.
   → task completes → next task starts automatically
```

### Project Context
- **Package**: `com.aman.ainpc`
- **Mod ID**: `ainpc`
- **Minecraft**: 1.20.1 / Forge 47.4.10
- **Java**: 17 (JDK at `/nix/store/xad649j61kwkh0id5wvyiab5rliprp4d-openjdk-17.0.15+6/lib/openjdk`)
- **Build**: `./gradlew build`

### Next Action
**4.1 — Build Conversation Context formatter:**
- Class: `conversation/ConversationContext.java`
- Method: `String buildSystemPrompt(AgentRuntime runtime, UUID playerUUID)`
- Reads: CharacterProfile (name, personality, occupation), NeedsManager (current needs), KnowledgeBase (facts), RelationshipManager (player relationship), LifeHistory.buildNarrativeSummary()
- Output: structured String that ConversationHandler passes to the AI call as the system prompt
- No LLM call here — just the formatter. ConversationHandler already makes the call.
