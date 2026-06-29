package com.aman.ainpc.interaction;

/**
 * Classifies the intent of an incoming interaction.
 *
 * Every interaction that reaches an NPC — whether from a player, another NPC,
 * or a future game system — is labelled with one of these types before the
 * InteractionEngine decides how the NPC will respond.
 */
public enum InteractionType {
    GREETING,
    QUESTION,
    REQUEST,
    COMMAND,
    TRADE,
    FAREWELL,
    UNKNOWN
}
