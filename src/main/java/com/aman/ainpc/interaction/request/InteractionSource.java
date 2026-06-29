package com.aman.ainpc.interaction.request;

/**
 * Identifies what external system produced an InteractionRequest.
 */
public enum InteractionSource {
    PLAYER_CHAT,
    PLAYER_INTERACT,
    PLAYER_ATTACK,
    NPC_INTERACTION,
    SETTLEMENT,
    SYSTEM,
    UNKNOWN
}
