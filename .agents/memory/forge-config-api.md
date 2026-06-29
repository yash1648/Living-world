---
name: Forge 47.x Config Registration API
description: The correct (non-broken) way to register ForgeConfigSpec in Forge 47.4.x (Minecraft 1.20.1).
---

## The Rule
Use `FMLJavaModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC)` in the `@Mod` constructor.

**Why:** `ModContainer` (injected via constructor parameter) does NOT have a `registerConfig()` method in Forge 47.x — it will fail with `cannot find symbol`. The `FMLJavaModLoadingContext` approach emits a deprecation warning (`[removal]`) but compiles and functions correctly at runtime.

**How to apply:**
```java
@Mod(AINPC.MODID)
public class AINPC {
    public AINPC(IEventBus modEventBus) {
        // ... other registration ...
        FMLJavaModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
```

Do NOT try `modContainer.registerConfig(...)` — the `ModContainer` parameter injection works, but the method does not exist in Forge 47.4.10's API.
