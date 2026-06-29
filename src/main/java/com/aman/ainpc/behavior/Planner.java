package com.aman.ainpc.behavior;

import com.aman.ainpc.behavior.task.Task;
import com.aman.ainpc.behavior.task.TaskType;
import com.aman.ainpc.decision.Goal;

import java.util.List;

/**
 * Decomposes a Goal into a sequential list of Tasks.
 *
 * Pure logic — no Minecraft API access.
 * The ActionExecutor is responsible for resolving positions and entity targets
 * at execution time.
 */
public class Planner {

    /**
     * Produces the task sequence for the given goal.
     * Returns an IDLE task list when goal is null.
     */
    public List<Task> plan(Goal goal) {
        if (goal == null) {
            return List.of(Task.of(TaskType.IDLE));
        }

        return switch (goal.getType()) {
            // Survival
            case FIND_FOOD   -> List.of(Task.of(TaskType.EXPLORE), Task.of(TaskType.EAT));
            case SEEK_SAFETY -> List.of(Task.of(TaskType.EXPLORE));
            case REST        -> List.of(Task.of(TaskType.SLEEP));

            // Social
            case SOCIALIZE    -> List.of(Task.of(TaskType.SOCIALIZE));
            case GREET_PLAYER -> List.of(Task.of(TaskType.GREET));

            // Curiosity / Growth
            case EXPLORE      -> List.of(Task.of(TaskType.EXPLORE));
            case INVESTIGATE  -> List.of(Task.of(TaskType.INVESTIGATE));

            // Dream pursuit — wander and then work toward ambition
            case PURSUE_DREAM -> List.of(Task.of(TaskType.EXPLORE), Task.of(TaskType.WORK));

            // Occupation
            case DO_WORK -> List.of(Task.of(TaskType.WORK));

            // Default
            case IDLE -> List.of(Task.of(TaskType.IDLE));
        };
    }
}
