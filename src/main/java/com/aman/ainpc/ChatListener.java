package com.aman.ainpc;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side chat listener.
 *
 * Previously contained a broken server-side implementation using
 * Minecraft.getInstance() which crashes on dedicated servers (B1 fix).
 *
 * Server-side conversation logic has been moved to:
 *   → com.aman.ainpc.conversation.ConversationHandler (ServerChatEvent)
 *   → com.aman.ainpc.conversation.ConversationManager (conversation state)
 *
 * NPC interaction is now triggered by right-clicking an AINPCEntity.
 * See NPCInteractionHandler.
 *
 * This class is retained as a @Dist.CLIENT stub for any future client-side
 * conversation UI (e.g. conversation HUD, close-button overlay).
 */
@Mod.EventBusSubscriber(modid = AINPC.MODID, value = Dist.CLIENT)
public class ChatListener {
    // Client-side conversation UI hooks will go here in Phase 4.
}
