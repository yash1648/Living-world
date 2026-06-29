package com.aman.ainpc;

import com.aman.ainpc.agent.runtime.AgentRuntime;
import com.aman.ainpc.agent.runtime.AgentRuntimeManager;
import com.aman.ainpc.agent.runtime.AgentTickResult;
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

public class AINPCEntity extends PathfinderMob {

    private static final double DEFAULT_SCAN_RANGE = 16.0;

    private AgentRuntime agentRuntime;
    private int scanCooldown = 0;
    private int nameUpdateCooldown = 0;

    public AINPCEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
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
        this.goalSelector.addGoal(1, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }

    // ── Lifecycle ──────────────────────────────────────────────────

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        this.agentRuntime = AgentRuntimeManager.getInstance().register(this.getUUID());

        // Set display name from generated character profile (server-side)
        if (!this.level().isClientSide() && this.agentRuntime != null) {
            String name = this.agentRuntime.getCharacterProfile().getName();
            this.setCustomName(Component.literal(name));
            this.setCustomNameVisible(true);
        }
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        if (this.agentRuntime != null) {
            AgentRuntimeManager.getInstance().unregister(this.getUUID());
            this.agentRuntime = null;
        }
    }

    // ── Tick ───────────────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();

        // Ensure runtime is registered (defensive)
        if (this.agentRuntime == null) {
            this.agentRuntime = AgentRuntimeManager.getInstance().register(this.getUUID());
        }

        if (!this.level().isClientSide()) {
            // Scan surroundings every N ticks
            int scanInterval = Config.scanIntervalTicks > 0 ? Config.scanIntervalTicks : 60;
            if (--scanCooldown <= 0) {
                scanCooldown = scanInterval;
                scanSurroundings();
            }

            // Execute the perception → event → decision pipeline
            AgentTickResult result = this.agentRuntime.tick();

            // Optionally update nametag with current goal (debug mode)
            if (--nameUpdateCooldown <= 0) {
                nameUpdateCooldown = 100; // ~5 seconds
                updateNameTag();
            }
        }
    }

    // ── Perception ─────────────────────────────────────────────────

    private void scanSurroundings() {
        Level level = this.level();
        if (level == null) return;

        double range = Config.scanRange > 0 ? Config.scanRange : DEFAULT_SCAN_RANGE;
        AABB searchBox = this.getBoundingBox().inflate(range);

        // Detect nearby players
        List<Player> players = level.getEntitiesOfClass(Player.class, searchBox,
                p -> p != null && p.isAlive() && !p.isSpectator());

        for (Player player : players) {
            Observation observation = new Observation(
                    System.currentTimeMillis(),
                    ObservationType.PLAYER_SEEN,
                    this.getUUID(),
                    player.getUUID(),
                    new Observation.Position(player.getX(), player.getY(), player.getZ()),
                    Map.of("name", player.getName().getString())
            );
            this.agentRuntime.getPerceptionBuffer().add(observation);
        }
    }

    // ── Nametag ────────────────────────────────────────────────────

    private void updateNameTag() {
        if (this.agentRuntime == null) return;

        String baseName = this.agentRuntime.getCharacterProfile().getName();

        if (Config.showDebugGoals) {
            Goal goal = this.agentRuntime.getCurrentGoal();
            String goalLabel = goal != null ? " §7[" + goal.getType().name() + "]§r" : "";
            this.setCustomName(Component.literal(baseName + goalLabel));
        } else {
            this.setCustomName(Component.literal(baseName));
        }
    }

    // ── Public Helpers ─────────────────────────────────────────────

    /** Returns the NPC's generated character name. */
    public String getCharacterName() {
        if (agentRuntime != null) {
            return agentRuntime.getCharacterProfile().getName();
        }
        return "NPC";
    }

    /** Returns the NPC's agent runtime (server-side only). */
    public AgentRuntime getAgentRuntime() {
        return agentRuntime;
    }
}
