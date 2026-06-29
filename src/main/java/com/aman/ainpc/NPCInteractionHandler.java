package com.aman.ainpc;

import com.aman.ainpc.conversation.ConversationManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/**
 * Handles player right-clicking on an AINPCEntity to start a conversation.
 *
 * Fix for B1/B3:
 *   - Checks target instanceof AINPCEntity (not hardcoded "AI_NPC" name)
 *   - Runs server-side only (guarded by isClientSide check)
 *   - Delegates conversation state to ConversationManager
 *   - Player types normally in chat; ConversationHandler intercepts on server
 */
@Mod.EventBusSubscriber(modid = AINPC.MODID)
public class NPCInteractionHandler {

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.EntityInteract event) {
        // Server-side only
        if (event.getLevel().isClientSide()) return;

        // Only handle right-clicks on AINPCEntity
        if (!(event.getTarget() instanceof AINPCEntity npc)) return;

        // Only handle by real server players
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        UUID playerUUID = player.getUUID();
        UUID npcUUID = npc.getUUID();

        ConversationManager cm = ConversationManager.getInstance();

        if (cm.isInConversation(playerUUID) &&
                cm.getConversationNPC(playerUUID).map(id -> id.equals(npcUUID)).orElse(false)) {
            // Right-clicking the same NPC again ends the conversation
            cm.endConversation(playerUUID);
            String npcName = npc.getCharacterName();
            player.sendSystemMessage(Component.literal("§7[You step away from " + npcName + ".]§r"));
        } else {
            // Start a new conversation (ends any previous one automatically)
            cm.startConversation(playerUUID, npcUUID);
            String npcName = npc.getCharacterName();
            player.sendSystemMessage(Component.literal(
                    "§a[You are now talking to " + npcName +
                    ". Type in chat to speak. Right-click again to leave.]§r"
            ));
        }

        event.setCanceled(true);
    }
}
