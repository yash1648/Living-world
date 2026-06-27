package com.aman.ainpc;

import com.aman.ainpc.agent.runtime.AgentRuntime;
import com.aman.ainpc.agent.runtime.AgentRuntimeManager;
import com.aman.ainpc.agent.runtime.AgentTickResult;
import com.aman.ainpc.perception.Observation;
import com.aman.ainpc.perception.ObservationType;
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

    private static final int SCAN_INTERVAL = 60; // ticks (~3 seconds) between proximity scans
    private static final double SCAN_RANGE = 16.0;

    private AgentRuntime agentRuntime;
    private int scanCooldown = 0;

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

        // Ensure runtime is registered (defensive — onAddedToWorld should handle it)
        if (this.agentRuntime == null) {
            this.agentRuntime = AgentRuntimeManager.getInstance().register(this.getUUID());
        }

        // Run agent runtime only on server side
        if (!this.level().isClientSide()) {
            // Scan surroundings for observations
            scanSurroundings();

            // Execute the perception → event pipeline
            AgentTickResult result = this.agentRuntime.tick();
        }
    }

    // ── Perception ─────────────────────────────────────────────────

    private void scanSurroundings() {
        if (--scanCooldown > 0) return;
        scanCooldown = SCAN_INTERVAL;

        Level level = this.level();
        if (level == null) return;

        // Detect nearby players
        AABB searchBox = this.getBoundingBox().inflate(SCAN_RANGE);
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
}
