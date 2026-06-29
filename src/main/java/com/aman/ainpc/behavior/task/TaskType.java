package com.aman.ainpc.behavior.task;

/**
 * Types of atomic tasks a Planner can queue for an NPC.
 *
 * ActionExecutor is the only system that maps these to Minecraft actions.
 */
public enum TaskType {

    // Movement
    WALK_TO,      // Walk to a specific (x, y, z) — position must be set on the Task
    EXPLORE,      // Walk to a random nearby position
    INVESTIGATE,  // Walk toward a point of interest

    // Social
    GREET,        // Approach and face the nearest player
    SOCIALIZE,    // Walk toward the nearest living entity

    // Survival
    EAT,          // Stand still; emit food particles; satisfy hunger visually
    SLEEP,        // Stand still and rest for a duration

    // Occupation
    WORK,         // Walk to a nearby work position and stand for a duration

    // Default
    IDLE          // Do nothing; let vanilla wandering goals handle movement
}
