package com.aman.ainpc.event;

/**
 * Domain event types for the Living World Engine.
 *
 * Every event in the system has a type. Subsystems (LifeHistory, Knowledge,
 * Relationships, DecisionEngine) route on event type.
 *
 * Convention:
 *   MET_*     — first or recurring encounter with an entity
 *   NPC_*     — NPC-initiated actions
 *   PLAYER_*  — player-initiated interactions
 *   ITEM_*    — item-related observations/actions
 *   BLOCK_*   — block-related observations/actions
 *   ENTITY_*  — entity-related combat/interaction
 *   DAY_*     — time/environment cycle events
 *   GOAL_*    — internal goal lifecycle (Phase 3)
 *   NEED_*    — internal need changes (Phase 2)
 */
public enum EventType {

    // ── Interaction Events ────────────────────────────────────────
    /** NPC encountered a player for the first time or again */
    MET_PLAYER,
    /** NPC encountered another NPC */
    NPC_ENCOUNTERED,
    /** NPC had a conversation with someone */
    CONVERSATION_HAD,
    /** NPC greeted someone */
    NPC_GREETED,
    /** NPC helped someone (gave item, built something, etc.) */
    NPC_HELPED,

    // ── Player Interaction Events ─────────────────────────────────
    /** Player gave an item to the NPC */
    PLAYER_GAVE_ITEM,
    /** Player took an item from the NPC */
    PLAYER_TOOK_ITEM,
    /** Player attacked or damaged the NPC */
    PLAYER_ATTACKED_NPC,

    // ── World Events ──────────────────────────────────────────────
    /** NPC placed a block */
    BLOCK_PLACED,
    /** NPC broke a block */
    BLOCK_BROKEN,
    /** A block change was observed (not necessarily by NPC) */
    BLOCK_CHANGED,
    /** NPC crafted an item */
    ITEM_CRAFTED,
    /** NPC gathered resources */
    ITEM_GATHERED,
    /** An item was observed in the world */
    ITEM_OBSERVED,

    // ── Environment Events ────────────────────────────────────────
    /** Day/night cycle changed */
    DAY_CHANGED,
    /** Weather changed (rain, thunder, clear) */
    WEATHER_CHANGED,

    // ── Combat Events ─────────────────────────────────────────────
    /** An entity took damage */
    ENTITY_DAMAGED,
    /** An entity was killed */
    ENTITY_KILLED,
    /** NPC was hurt */
    NPC_HURT,

    // ── Internal Phase 2 Events ───────────────────────────────────
    /** An NPC need changed value (hungry, lonely, etc.) */
    NEED_CHANGED,
    /** NPC achieved progress toward a dream */
    DREAM_PROGRESS,
    /** A relationship score changed */
    RELATIONSHIP_CHANGED,
    /** NPC learned a new fact */
    KNOWLEDGE_GAINED,

    // ── Goal Events (Phase 3) ─────────────────────────────────────
    /** NPC set a new goal */
    GOAL_SET,
    /** NPC completed a goal */
    GOAL_COMPLETED,
    /** NPC abandoned a goal */
    GOAL_ABANDONED,

    // ── Social Events (Phase 5+) ──────────────────────────────────
    /** Something notable happened in a settlement */
    VILLAGE_EVENT,
    /** A new NPC joined the settlement */
    NPC_JOINED,
    /** An NPC left the settlement */
    NPC_LEFT,

    // ── Fallback ──────────────────────────────────────────────────
    /** Unrecognized or unclassified event */
    UNKNOWN
}
