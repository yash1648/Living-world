package com.aman.ainpc.behavior;

import com.aman.ainpc.AINPCEntity;
import com.aman.ainpc.behavior.task.Task;
import com.aman.ainpc.behavior.task.TaskType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Random;

/**
 * The ONLY system allowed to call Minecraft APIs to move or animate an NPC.
 *
 * Consumes tasks from the TaskQueue one at a time, resolving positions and
 * entity targets from the world at the moment each task begins.
 *
 * Not thread-safe — always called from the server tick thread.
 */
public class ActionExecutor {

    private static final Random RANDOM = new Random();

    private Task currentTask;
    private int taskTicksElapsed;

    // Resolved world position for movement tasks
    private double resolvedX;
    private double resolvedY;
    private double resolvedZ;
    private boolean positionResolved;

    // ── Main tick ─────────────────────────────────────────────────

    /**
     * Advance one server tick. Returns true if a task is actively running.
     *
     * @param entity the NPC entity (Minecraft access)
     * @param queue  the shared TaskQueue from AgentRuntime
     */
    public boolean tick(AINPCEntity entity, TaskQueue queue) {
        // Pull the next task when idle
        if (currentTask == null) {
            if (queue.isEmpty()) return false;
            startTask(entity, queue.poll());
        }

        taskTicksElapsed++;

        boolean done = execute(entity, currentTask);

        if (done || taskTicksElapsed >= currentTask.getMaxDurationTicks()) {
            finishTask(entity);
        }

        return true;
    }

    /** True while a task is currently being executed. */
    public boolean isActive() {
        return currentTask != null;
    }

    /** Cancel whatever is running and clear state. */
    public void reset(AINPCEntity entity) {
        if (entity != null && entity.getNavigation() != null) {
            entity.getNavigation().stop();
        }
        currentTask = null;
        taskTicksElapsed = 0;
        positionResolved = false;
    }

    // ── Task lifecycle ────────────────────────────────────────────

    private void startTask(AINPCEntity entity, Task task) {
        currentTask = task;
        taskTicksElapsed = 0;
        positionResolved = false;
    }

    private void finishTask(AINPCEntity entity) {
        currentTask = null;
        taskTicksElapsed = 0;
        positionResolved = false;
    }

    // ── Dispatch ──────────────────────────────────────────────────

    private boolean execute(AINPCEntity entity, Task task) {
        return switch (task.getType()) {
            case WALK_TO     -> doWalkTo(entity, task);
            case EXPLORE     -> doExplore(entity);
            case INVESTIGATE -> doInvestigate(entity, task);
            case GREET       -> doGreet(entity);
            case SOCIALIZE   -> doSocialize(entity);
            case EAT         -> doEat(entity);
            case SLEEP       -> doSleep(entity);
            case WORK        -> doWork(entity);
            case IDLE        -> true; // done immediately — let vanilla goals run
        };
    }

    // ── Movement helpers ──────────────────────────────────────────

    /** Walk to the task's fixed position. */
    private boolean doWalkTo(AINPCEntity entity, Task task) {
        if (!positionResolved) {
            resolvedX = task.getTargetX();
            resolvedY = task.getTargetY();
            resolvedZ = task.getTargetZ();
            entity.getNavigation().moveTo(resolvedX, resolvedY, resolvedZ, 1.0);
            positionResolved = true;
        }
        return entity.getNavigation().isDone();
    }

    /** Pick a random position nearby and walk there. */
    private boolean doExplore(AINPCEntity entity) {
        if (!positionResolved) {
            double angle    = RANDOM.nextDouble() * 2 * Math.PI;
            double distance = 6.0 + RANDOM.nextDouble() * 10.0;
            resolvedX = entity.getX() + Math.cos(angle) * distance;
            resolvedY = entity.getY();
            resolvedZ = entity.getZ() + Math.sin(angle) * distance;
            entity.getNavigation().moveTo(resolvedX, resolvedY, resolvedZ, 1.0);
            positionResolved = true;
        }
        return entity.getNavigation().isDone();
    }

    /** Walk toward the task's target or, if absent, explore. */
    private boolean doInvestigate(AINPCEntity entity, Task task) {
        if (task.hasPosition()) {
            return doWalkTo(entity, task);
        }
        return doExplore(entity);
    }

    /** Approach and face the nearest player. */
    private boolean doGreet(AINPCEntity entity) {
        Player nearest = nearestPlayer(entity, 20.0);
        if (nearest == null) return true; // no player in range — done

        double dist = entity.distanceTo(nearest);
        if (dist > 3.0) {
            entity.getNavigation().moveTo(nearest, 1.0);
            return false;
        }
        // Close enough — look at player
        entity.getLookControl().setLookAt(nearest, 30.0F, 30.0F);
        entity.getNavigation().stop();
        return taskTicksElapsed >= 60; // hold gaze for 3 s
    }

    /** Walk toward the nearest living entity that is not the NPC itself. */
    private boolean doSocialize(AINPCEntity entity) {
        LivingEntity target = nearestLivingEntity(entity, 16.0);
        if (target == null) return doExplore(entity);

        double dist = entity.distanceTo(target);
        if (dist > 3.0) {
            entity.getNavigation().moveTo(target, 0.9);
            return false;
        }
        entity.getLookControl().setLookAt(target, 30.0F, 30.0F);
        entity.getNavigation().stop();
        return taskTicksElapsed >= 80;
    }

    /** Stand still and emit eating particles. */
    private boolean doEat(AINPCEntity entity) {
        entity.getNavigation().stop();
        if (entity.level() instanceof ServerLevel serverLevel) {
            if (taskTicksElapsed % 10 == 0) {
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        entity.getX(), entity.getEyeY(), entity.getZ(),
                        3, 0.3, 0.3, 0.3, 0.05);
            }
        }
        return taskTicksElapsed >= 80;
    }

    /** Stand still for a rest duration. */
    private boolean doSleep(AINPCEntity entity) {
        entity.getNavigation().stop();
        return taskTicksElapsed >= 240;
    }

    /** Walk to a nearby "work" position, then stand for a duration. */
    private boolean doWork(AINPCEntity entity) {
        if (!positionResolved) {
            double angle    = RANDOM.nextDouble() * 2 * Math.PI;
            double distance = 4.0 + RANDOM.nextDouble() * 8.0;
            resolvedX = entity.getX() + Math.cos(angle) * distance;
            resolvedY = entity.getY();
            resolvedZ = entity.getZ() + Math.sin(angle) * distance;
            entity.getNavigation().moveTo(resolvedX, resolvedY, resolvedZ, 0.85);
            positionResolved = true;
        }

        boolean arrived = entity.getNavigation().isDone();
        if (arrived) {
            entity.getNavigation().stop();
            // Stand and "work" for the remainder of the duration
            return taskTicksElapsed >= currentTask.getMaxDurationTicks() - 20;
        }
        return false;
    }

    // ── World queries ─────────────────────────────────────────────

    private Player nearestPlayer(AINPCEntity entity, double range) {
        AABB box = entity.getBoundingBox().inflate(range);
        List<Player> players = entity.level().getEntitiesOfClass(
                Player.class, box,
                p -> p.isAlive() && !p.isSpectator());
        if (players.isEmpty()) return null;
        Player nearest = null;
        double best = Double.MAX_VALUE;
        for (Player p : players) {
            double d = entity.distanceToSqr(p);
            if (d < best) { best = d; nearest = p; }
        }
        return nearest;
    }

    private LivingEntity nearestLivingEntity(AINPCEntity entity, double range) {
        AABB box = entity.getBoundingBox().inflate(range);
        List<LivingEntity> entities = entity.level().getEntitiesOfClass(
                LivingEntity.class, box,
                e -> e != entity && e.isAlive());
        if (entities.isEmpty()) return null;
        LivingEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (LivingEntity e : entities) {
            double d = entity.distanceToSqr(e);
            if (d < best) { best = d; nearest = e; }
        }
        return nearest;
    }
}
