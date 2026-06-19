package com.aman.ainpc;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class NPCInteractionHandler {

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.EntityInteract event) {

        if (event.getTarget() instanceof ServerPlayer npc) {

            if (npc.getName().getString().equals("AI_NPC")) {

                ChatListener.talkingToNPC = true;
                ChatListener.currentNPC = npc;

                ChatListener.freezeNPC();

                event.getEntity().sendSystemMessage(
                        Component.literal("Talking to AI Player NPC...")
                );
            }
        }
    }
}