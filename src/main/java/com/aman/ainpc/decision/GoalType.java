package com.aman.ainpc.decision;

/**
 * Types of goals the Decision Engine can output.
 *
 * Phase 3 will have a Planner that decomposes these into Tasks.
 * For now they represent intent — what the NPC wants to do next.
 */
public enum GoalType {

    // Survival
    FIND_FOOD,      // Hunger is critical
    SEEK_SAFETY,    // Safety need is critical (flee danger)
    REST,           // Rest need is critical (sleep/idle)

    // Social
    SOCIALIZE,      // Loneliness need is high
    GREET_PLAYER,   // Player nearby and loneliness moderate

    // Curiosity / Growth
    EXPLORE,        // Curiosity need is high, no specific dream
    INVESTIGATE,    // Something interesting changed nearby

    // Dream pursuit
    PURSUE_DREAM,   // Curiosity + dream pointing in same direction

    // Occupation
    DO_WORK,        // All needs satisfied — do your job

    // Default
    IDLE            // Nothing pressing — wander/relax
}
