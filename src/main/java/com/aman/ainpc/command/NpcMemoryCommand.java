package com.aman.ainpc.command;

import com.aman.ainpc.AINPCEntity;
import com.aman.ainpc.agent.runtime.AgentRuntime;
import com.aman.ainpc.event.Event;
import com.aman.ainpc.memory.history.LifeHistory;
import com.aman.ainpc.memory.history.LifeHistoryEntry;
import com.aman.ainpc.perception.Observation;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Developer command: /npcmemory <target>
 *
 * Permission level 2 (operator-only).
 *
 * Reads the NPC's LifeHistory and prints:
 *   - NPC name
 *   - Total number of recorded events
 *   - The 10 most recent events (timestamp, EventType, short summary)
 *
 * Nothing is written, generated, or modified. Only existing data is exposed.
 * Intended for verifying the perception → event → memory pipeline during
 * development. Has no effect on gameplay.
 */
public class NpcMemoryCommand {

    /** HH:mm:ss in the server's local timezone — readable in the chat window. */
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("npcmemory")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("target", EntityArgument.entity())
                                .executes(ctx -> {
                                    Entity entity = EntityArgument.getEntity(ctx, "target");

                                    if (!(entity instanceof AINPCEntity npc)) {
                                        ctx.getSource().sendFailure(
                                                Component.literal("Target is not an AI NPC."));
                                        return 0;
                                    }

                                    AgentRuntime runtime = npc.getAgentRuntime();
                                    if (runtime == null) {
                                        ctx.getSource().sendFailure(
                                                Component.literal("NPC has no active runtime (client-side?)."));
                                        return 0;
                                    }

                                    LifeHistory history = runtime.getLifeHistory();
                                    String      npcName = npc.getCharacterName();
                                    int         total   = history.size();

                                    // ── Header ───────────────────────────────────────
                                    if (total == 0) {
                                        ctx.getSource().sendSuccess(
                                                () -> Component.literal(
                                                        "§e[" + npcName + "]§r No recorded history."),
                                                false);
                                        return 1;
                                    }

                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal(
                                                    "§e[" + npcName + "]§r — §7"
                                                    + total + " event(s) total, showing last "
                                                    + Math.min(total, 10) + ":§r"),
                                            false);

                                    // ── 10 most recent entries ───────────────────────
                                    List<LifeHistoryEntry> recent = history.recent(10);
                                    for (LifeHistoryEntry entry : recent) {
                                        Event  event   = entry.getEvent();
                                        String time    = TIME_FMT.format(entry.getRecordedAt());
                                        String type    = event.getType().name();
                                        String summary = buildSummary(event);

                                        ctx.getSource().sendSuccess(
                                                () -> Component.literal(
                                                        "  §7" + time + "§r  §6" + type + "§r  " + summary),
                                                false);
                                    }

                                    return 1;
                                })
                        )
        );
    }

    // ── Helpers ───────────────────────────────────────────────────

    /**
     * Produces a one-line human-readable description of an event using only
     * the data already stored on it — metadata map and source observations.
     * No new data is generated. Falls back to "(no details)" when the event
     * carries no useful metadata.
     */
    private static String buildSummary(Event event) {
        // 1. Try metadata fields common to several event types
        Map<String, String> meta = event.getMetadata();
        if (!meta.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            meta.forEach((k, v) -> sb.append(k).append("=").append(v).append(" "));
            return sb.toString().trim();
        }

        // 2. Try the "name" field from source observations (e.g. PLAYER_SEEN)
        for (Observation obs : event.getSourceObservations()) {
            String name = obs.getMetadata().get("name");
            if (name != null) return "player=" + name;
        }

        // 3. Nothing useful available yet
        return "(no details)";
    }
}
