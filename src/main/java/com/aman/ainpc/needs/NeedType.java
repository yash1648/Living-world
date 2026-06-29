package com.aman.ainpc.needs;

/**
 * The types of needs an NPC can have.
 *
 * Each need drives different behaviors:
 * - HUNGER      → find food, hunt, harvest
 * - SAFETY      → flee danger, seek shelter
 * - LONELINESS  → talk to players/NPCs, socialize
 * - CURIOSITY   → explore, investigate changes
 * - REST        → sleep, idle, recover
 */
public enum NeedType {
    HUNGER,
    SAFETY,
    LONELINESS,
    CURIOSITY,
    REST
}
