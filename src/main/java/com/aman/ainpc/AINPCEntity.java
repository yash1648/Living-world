package com.aman.ainpc;

import com.aman.ainpc.agent.runtime.AgentRuntime;
import com.aman.ainpc.agent.runtime.AgentRuntimeManager;
import com.aman.ainpc.agent.runtime.AgentTickResult;
import com.aman.ainpc.behavior.ActionExecutor;
import com.aman.ainpc.behavior.GoalDrivenGoal;
import com.aman.ainpc.decision.Goal;
import com.aman.ainpc.perception.Observation;
import com.aman.ainpc.perception.ObservationType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Map;

/**
 * The Minecraft entity representing an AI NPC.
 *
 * Runtime ownership:
 *   Each AINPCEntity permanently owns exactly one AgentRuntime — its brain.
 *   The runtime is created once in onAddedToWorld() (where the entity UUID is
 *   guaranteed stable) and is never nulled out or recreated. Removing the entity
 *   from the world does not destroy the runtime; re-adding it (chunk unload/reload,
 *   dimension change) reuses the same runtime via the null-guard.
 *   AgentRuntimeManager is still notified on add/remove for backward compatibility.
 */
public class AINPCEntity extends PathfinderMob {

    private static final double DEFAULT_SCAN_RANGE = 16.0;

    /** The NPC's brain. Created once; never null after onAddedToWorld(). */
    private AgentRuntime agentRuntime;
    private final ActionExecutor actionExecutor = new ActionExecutor();
    private int scanCooldown    = 0;
    private int nameUpdateCooldown = 0;

    public AINPCEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        // Runtime is NOT created here — the entity UUID may still be overwritten
        // by NBT load after construction. Create it in onAddedToWorld() instead.
    }

    // ── Attributes ─────────────────────────────────────────────────

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ARMOR, 2.0D);
    }

    // ── AI Goals ───────────────────────────────────────────────────

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(1, new GoalDrivenGoal(this, actionExecutor));
        this.goalSelector.addGoal(2, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }

    // ── Lifecycle ──────────────────────────────────────────────────

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();

        // Create the runtime exactly once. If the entity is removed and re-added
        // (chunk reload, dimension travel), the existing runtime is reused.
        if (agentRuntime == null) {
            agentRuntime = new AgentRuntime(this.getUUID());
        }

        // Also register with AgentRuntimeManager for backward compatibility
        // (other systems that still do UUID-based lookup).
        AgentRuntimeManager.getInstance().register(this.getUUID());

        // Set display name from generated character profile (server-side only)
        if (!this.level().isClientSide()) {
            String name = agentRuntime.getCharacterProfile().getName();
            this.setCustomName(Component.literal(name));
            this.setCustomNameVisible(true);
        }
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        // Do NOT null agentRuntime — the entity permanently owns it.
        // Unregister from the manager so UUID lookups no longer resolve here.
        AgentRuntimeManager.getInstance().unregister(this.getUUID());
    }

    // ── Tick ───────────────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();

        // agentRuntime is guaranteed non-null after onAddedToWorld().
        // No defensive re-creation needed here.
        if (agentRuntime == null || this.level().isClientSide()) return;

        // Scan surroundings every N ticks
        int scanInterval = Config.scanIntervalTicks > 0 ? Config.scanIntervalTicks : 60;
        if (--scanCooldown <= 0) {
            scanCooldown = scanInterval;
            scanSurroundings();
        }

        // Execute the perception → event → decision → plan pipeline
        AgentTickResult result = agentRuntime.tick();

        // Optionally update nametag with current goal (debug mode)
        if (--nameUpdateCooldown <= 0) {
            nameUpdateCooldown = 100;
            updateNameTag();
        }
    }

    // ── Perception ─────────────────────────────────────────────────

    private void scanSurroundings() {
        Level level = this.level();
        if (level == null) return;

        double range = Config.scanRange > 0 ? Config.scanRange : DEFAULT_SCAN_RANGE;
        AABB searchBox = this.getBoundingBox().inflate(range);

        List<Player> players = level.getEntitiesOfClass(Player.class, searchBox,
                p -> p != null && p.isAlive() && !p.isSpectator());

        for (Player player : players) {
            agentRuntime.getPerceptionBuffer().add(new Observation(
                    System.currentTimeMillis(),
                    ObservationType.PLAYER_SEEN,
                    this.getUUID(),
                    player.getUUID(),
                    new Observation.Position(player.getX(), player.getY(), player.getZ()),
                    Map.of("name", player.getName().getString())
            ));
        }
    }

    // ── Nametag ────────────────────────────────────────────────────

    private void updateNameTag() {
        if (agentRuntime == null) return;

        String baseName = agentRuntime.getCharacterProfile().getName();

        if (Config.showDebugGoals) {
            Goal goal = agentRuntime.getCurrentGoal();
            String goalLabel = goal != null ? " §7[" + goal.getType().name() + "]§r" : "";
            this.setCustomName(Component.literal(baseName + goalLabel));
        } else {
            this.setCustomName(Component.literal(baseName));
        }
    }

    // ── Public API ─────────────────────────────────────────────────

    /** The NPC's generated character name. */
    public String getCharacterName() {
        return agentRuntime != null ? agentRuntime.getCharacterProfile().getName() : "NPC";
    }

    /**
     * The NPC's agent runtime — its brain.
     * Non-null after onAddedToWorld(). Server-side only.
     */
    public AgentRuntime getAgentRuntime() {
        return agentRuntime;
    }
}
