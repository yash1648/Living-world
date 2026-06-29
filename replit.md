# AINPC - AI NPC Mod for Minecraft

A Minecraft Forge mod (1.20.1) that creates intelligent NPCs with perception, memory, relationships, and goal-oriented behaviors.

## Build System
- **Language:** Java 17 (via GraalVM 22.3)
- **Build Tool:** Gradle (via `./gradlew`)
- **Modding Platform:** Minecraft Forge 47.4.10 for Minecraft 1.20.1

## How to Build

Run the build workflow, or use the shell:

```bash
./gradlew build
```

The compiled mod `.jar` will be output to `build/libs/ainpc-1.0.0.jar`.

## Project Layout

- `src/main/java/com/aman/ainpc/` — Java source code
  - `AINPC.java` — Main mod entry point
  - `agent/` — AI agent runtime and lifecycle
  - `event/` — Internal event system
  - `memory/` — Episodic memory (LifeHistory)
  - `perception/` — NPC world observation
  - `relationship/` — NPC-player opinion tracking
  - `runtime/` — Core "brain" logic
- `src/main/resources/` — Mod metadata and assets
- `docs/` — Architecture documentation
- `gradle.properties` — Mod version and Forge/Minecraft version config

## User Preferences

- Keep existing Forge/Gradle project structure
