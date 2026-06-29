---
name: ForgeGradle Java 17 Toolchain
description: How to resolve ForgeGradle 6's hardcoded Java 17 toolchain requirement in a Replit/Nix environment running GraalVM 22.3 (Java 19).
---

## The Rule
ForgeGradle 6.0.x internally requests a Java 17 toolchain for its MCP decompilation/renaming steps (`ExecuteFunction`, `MCPRuntime`). GraalVM 22.3 (Java 19) is not accepted — the Gradle toolchain scanner detects the version from the `release` file.

**Why:** ForgeGradle hardcodes `JavaLanguageVersion.of(17)` in its toolchain spec internally. No `build.gradle` setting overrides this.

**How to apply:** When a Replit environment only has GraalVM 22.3 (Java 19) on PATH:
1. After `installSystemDependencies({ packages: ["jdk17"] })`, Replit installs a real JDK 17 at a Nix store path (available to workflows).
2. Add to `gradle.properties`:
   ```properties
   org.gradle.java.installations.auto-detect=true
   org.gradle.java.installations.paths=/home/runner/.jdks/temurin-17
   ```
3. Gradle's auto-detect of `~/.jdks/` works natively — place the JDK or a symlink directory there with a valid `release` file (key field: `JAVA_VERSION="17.x.x"`).

**Outcome:** After this fix, `./gradlew build` completes in ~4 minutes on first run (Minecraft decompile), ~17 seconds incremental. Java reported as `17.0.15+6-nixos`.

**Note:** `ModContainer.registerConfig()` does NOT exist in Forge 47.x. Keep `FMLJavaModLoadingContext.get().registerConfig()` despite the deprecation warning — it compiles and works at runtime. The deprecation is informational only in Forge 47.4.10.
