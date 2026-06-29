package com.aman.ainpc.interaction.request;

import com.aman.ainpc.interaction.InteractionContext;
import com.aman.ainpc.interaction.InteractionType;

/**
 * Translates a raw InteractionRequest into a typed InteractionContext.
 *
 * This is the bridge between the external world (Minecraft events, settlement
 * orders, NPC-to-NPC interactions) and the internal interaction system.
 *
 * Current behaviour: always produces an InteractionContext with
 * InteractionType.UNKNOWN, preserving all other fields unchanged.
 *
 * Future: route on InteractionSource and payload content to assign the
 * correct InteractionType (GREETING, TRADE, COMMAND, etc.) without the
 * InteractionEngine ever seeing unclassified input.
 */
public class InteractionResolver {

    /**
     * Convert a raw request into a typed interaction context.
     *
     * @param request the raw external event
     * @return a fully-formed InteractionContext ready for the InteractionEngine
     */
    public InteractionContext resolve(InteractionRequest request) {
        return new InteractionContext(
                InteractionType.UNKNOWN,
                request.getInitiatorId(),
                request.getTargetId(),
                request.getPayload(),
                request.getTimestamp()
        );
    }
}
