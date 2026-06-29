package com.aman.ainpc.interaction;

/**
 * Decides how an NPC will react to an incoming interaction.
 *
 * The Interaction Engine is the single entry point for all NPC interactions —
 * greetings, questions, trades, commands, and every future interaction type.
 * It evaluates the InteractionContext and returns an InteractionDecision before
 * any dialogue is generated or any game action is taken.
 *
 * WHY THIS EXISTS SEPARATELY FROM CONVERSATION:
 *
 * Conversation is one output channel — it produces dialogue text.
 * The Interaction Engine is upstream of that: it decides IF and HOW the NPC
 * responds at all. Future interactions (trading, recruitment, threats) may
 * never produce dialogue; they produce game-state changes instead. Keeping
 * this layer separate means the LLM never receives raw player input directly,
 * and every interaction type routes through a consistent decision point that
 * can check relationship state, NPC mood, goals, and context before anything
 * downstream is invoked.
 *
 * Current behaviour: accept every interaction unconditionally.
 * Future: route on InteractionType, check relationship/needs/goals, and
 * return differentiated decisions (refuse, defer, counter-offer, etc.).
 */
public class InteractionEngine {

    /**
     * Evaluate an interaction and return the NPC's decision.
     *
     * @param context the fully-populated interaction context
     * @return an InteractionDecision describing whether the NPC accepts and why
     */
    public InteractionDecision evaluate(InteractionContext context) {
        return new InteractionDecision(true, "Accepted");
    }
}
