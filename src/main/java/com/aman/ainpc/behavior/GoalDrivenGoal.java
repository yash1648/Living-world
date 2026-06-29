package com.aman.ainpc.behavior;

import com.aman.ainpc.AINPCEntity;
import com.aman.ainpc.agent.runtime.AgentRuntime;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * A Minecraft PathfinderGoal that bridges the AI decision layer to the world.
 *
 * Each tick it asks the ActionExecutor to advance the current task.
 * When the task queue empties, it lets vanilla goals (wander, look-around)
 * run until the next planning cycle refills it.
 */
public class GoalDrivenGoal extends Goal {

    private final AINPCEntity entity;
    private final ActionExecutor executor;

    public GoalDrivenGoal(AINPCEntity entity, ActionExecutor executor) {
        this.entity   = entity;
        this.executor = executor;
        // This goal controls movement; mark MOVE so the selector knows
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        AgentRuntime runtime = entity.getAgentRuntime();
        if (runtime == null) return false;
        // Run whenever there are tasks queued
        return !runtime.getTaskQueue().isEmpty();
    }

    @Override
    public boolean canContinueToUse() {
        AgentRuntime runtime = entity.getAgentRuntime();
        if (runtime == null) return false;
        return !runtime.getTaskQueue().isEmpty() || executor.isActive();
    }

    @Override
    public void start() {
        // Nothing — ActionExecutor picks up tasks on its own first tick
    }

    @Override
    public void stop() {
        executor.reset(entity);
    }

    @Override
    public void tick() {
        AgentRuntime runtime = entity.getAgentRuntime();
        if (runtime == null) return;
        executor.tick(entity, runtime.getTaskQueue());
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
