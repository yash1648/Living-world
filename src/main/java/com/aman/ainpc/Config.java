package com.aman.ainpc;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/**
 * Mod configuration.
 *
 * Values are loaded from the ainpc-common.toml config file.
 * Forge handles reading/writing automatically.
 */
@Mod.EventBusSubscriber(modid = AINPC.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // ── AI Backend ────────────────────────────────────────────────

    public static final ForgeConfigSpec.ConfigValue<String> AI_ENDPOINT = BUILDER
            .comment("URL of the AI backend chat endpoint.",
                     "The server POSTs player messages here and expects a JSON {\"reply\":\"...\"} response.",
                     "Default assumes a local Python server on port 5000.")
            .define("aiEndpoint", "http://127.0.0.1:5000/chat");

    // ── NPC Behaviour ─────────────────────────────────────────────

    public static final ForgeConfigSpec.IntValue SCAN_INTERVAL_TICKS = BUILDER
            .comment("How often (in game ticks) an NPC scans its surroundings.",
                     "20 ticks = 1 second. Default 60 = ~3 seconds.")
            .defineInRange("scanIntervalTicks", 60, 10, 200);

    public static final ForgeConfigSpec.DoubleValue SCAN_RANGE = BUILDER
            .comment("Radius (in blocks) of the NPC's perception scan.")
            .defineInRange("scanRange", 10.0, 4.0, 64.0);

    public static final ForgeConfigSpec.BooleanValue SHOW_DEBUG_GOALS = BUILDER
            .comment("If true, the NPC's current goal is shown above its head as a nametag suffix.")
            .define("showDebugGoals", false);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    // ── Loaded values (updated on config load/reload) ─────────────

    public static String aiEndpoint;
    public static int scanIntervalTicks;
    public static double scanRange;
    public static boolean showDebugGoals;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        aiEndpoint = AI_ENDPOINT.get();
        scanIntervalTicks = SCAN_INTERVAL_TICKS.get();
        scanRange = SCAN_RANGE.get();
        showDebugGoals = SHOW_DEBUG_GOALS.get();
    }
}
