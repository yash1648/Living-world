# Repository Map: Living World Engine (LWE)

## Directory Structure

```
Livingworld-/
├── build.gradle                  # ForgeGradle build (Minecraft 1.20.1, Java 17)
├── gradle.properties             # ✅ Fixed: ainpc mod values
├── settings.gradle               # rootProject.name = 'ainpc'
├── gradlew / gradlew.bat         # Gradle wrapper
├── README.txt                    # Forge MDK README (template — needs update)
├── CREDITS.txt / LICENSE.txt / changelog.txt
├── src/
│   └── main/
│       ├── java/com/aman/ainpc/
│       │   ├── AINPC.java                        # @Mod entry, DeferredRegister, attributes
│       │   ├── AINPCEntity.java                   # Entity + AgentRuntime lifecycle + perception scan
│       │   ├── ChatListener.java                  # ⚠️ NEEDS FIX: client-side, wrong LLM direction
│       │   ├── Config.java                        # ⚠️ Placeholder values (not LWE-aligned)
│       │   ├── NPCCommand.java                    # /spawnnpc command
│       │   ├── NPCInteractionHandler.java         # Right-click to talk
│       │   ├── agent/
│       │   │   └── runtime/
│       │   │       ├── AgentRuntime.java           # ✅ Tick pipeline: perception→eval→correlate→event→dispatch
│       │   │       ├── AgentRuntimeManager.java    # ✅ Singleton registry
│       │   │       ├── AgentState.java             # ✅ Lifecycle: CREATED→RUNNING→PAUSED→STOPPED
│       │   │       └── AgentTickResult.java        # ✅ Tick result enum
│       │   ├── client/
│       │   │   └── AINPCClient.java               # ✅ Entity renderer (HumanoidModel)
│       │   ├── event/
│       │   │   ├── Event.java                      # ✅ Event data model
│       │   │   ├── EventFactory.java               # ✅ Creates events from context groups
│       │   │   ├── EventResult.java                # ✅ Event creation result
│       │   │   ├── EventType.java                  # ⚠️ NEEDS FIX: only UNKNOWN, needs domain types
│       │   │   └── dispatch/
│       │   │       ├── EventConsumer.java          # ✅ Functional interface
│       │   │       └── EventDispatcher.java        # ✅ Fan-out dispatch
│       │   ├── memory/
│       │   │   └── history/
│       │   │       ├── LifeHistory.java            # ✅ Append-only event store
│       │   │       └── LifeHistoryEntry.java       # ✅ Timestamped event record
│       │   ├── perception/
│       │   │   ├── Observation.java                # ✅ Perception data model
│       │   │   ├── ObservationType.java            # ✅ Enum (PLAYER_SEEN, etc.)
│       │   │   ├── PerceptionBuffer.java           # ✅ Observation queue with drain()
│       │   │   └── PerceptionSnapshot.java         # ✅ Immutable observation list
│       │   ├── relationship/
│       │   │   ├── RelationshipManager.java         # ⚠️ Basic UUID tracking, needs trust/scores
│       │   │   └── RelationshipRecord.java          # ⚠️ Basic, needs scoring fields
│       │   └── runtime/
│       │       └── processing/
│       │           ├── SignificanceEvaluator.java   # ⚠️ Pass-through stub
│       │           ├── SignificanceResult.java      # ✅ Result wrapper
│       │           └── correlation/
│       │               ├── ContextCorrelator.java   # ⚠️ Pass-through stub
│       │               └── ContextCorrelationResult.java
│       └── resources/
│           ├── META-INF/mods.toml                  # Mod metadata (ainpc, v1.0)
│           └── pack.mcmeta                         # Resource pack metadata
├── build/                                          # Build output
│   └── libs/ainpc-1.0.0.jar                        # ✅ Latest build artifact
├── .gradle/                                        # Gradle cache
└── startup-builder-memory/                         # Memory artifacts (6 files)
```

## Key Files Summary

| File | Lines | Role | Phase | Status |
|------|-------|------|-------|--------|
| AINPC.java | 54 | Mod entry + entity registration | 1 | ✅ |
| AINPCEntity.java | 117 | Entity + runtime + perception scan | 1 | ✅ |
| AgentRuntime.java | 70 | Tick pipeline orchestrator | 1 | ✅ |
| AgentRuntimeManager.java | 37 | Singleton runtime registry | 1 | ✅ |
| AgentState.java | 33 | Lifecycle state machine | 1 | ✅ |
| AgentTickResult.java | 8 | Tick result enum | 1 | ✅ |
| AINPCClient.java | 51 | Entity renderer (client) | 1 | ✅ |
| AINPCEntity.java | 117 | Entity + lifecycle + proximity scan | 1 | ✅ |
| NPCCommand.java | 39 | /spawnnpc command | 1 | ✅ |
| Observation.java | 68 | Perception data model | 1 | ✅ |
| ObservationType.java | 13 | Perception type enum | 1 | ✅ |
| PerceptionBuffer.java | 29 | Observation queue | 1 | ✅ |
| PerceptionSnapshot.java | 26 | Immutable observation list | 1 | ✅ |
| Event.java | 46 | Event data model | 1 | ✅ |
| EventFactory.java | 29 | Creates events | 1 | ✅ |
| EventResult.java | 24 | Event creation result | 1 | ✅ |
| EventType.java | 5 | Event type enum | 1 | ⚠️ Only UNKNOWN |
| EventDispatcher.java | 28 | Fan-out dispatch | 1 | ✅ |
| EventConsumer.java | 8 | Consumer interface | 1 | ✅ |
| LifeHistory.java | 37 | Append-only event store | 2 | ⚠️ Needs querying |
| LifeHistoryEntry.java | 24 | Timestamped event | 2 | ✅ |
| RelationshipManager.java | 47 | Entity relationships | 2 | ⚠️ Needs scoring |
| RelationshipRecord.java | 23 | Relationship data | 2 | ⚠️ Needs fields |
| SignificanceEvaluator.java | 14 | Observation filtering | 2 | ⚠️ Pass-through |
| SignificanceResult.java | 25 | Filter result | 2 | ✅ |
| ContextCorrelator.java | 17 | Observation grouping | 2 | ⚠️ Pass-through |
| ContextCorrelationResult.java | 25 | Correlation result | 2 | ✅ |
| ChatListener.java | 175 | Chat + LLM integration | 4 | ❌ Wrong direction |
| NPCInteractionHandler.java | 30 | Right-click handler | 1 | ❌ Hardcoded name |
| Config.java | 63 | Forge config | — | ❌ Placeholders |
