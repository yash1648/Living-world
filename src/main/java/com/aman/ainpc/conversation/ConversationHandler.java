package com.aman.ainpc.conversation;

import com.aman.ainpc.AINPC;
import com.aman.ainpc.AINPCEntity;
import com.aman.ainpc.agent.runtime.AgentRuntime;
import com.aman.ainpc.conversation.coordinator.ConversationCoordinator;
import com.aman.ainpc.interaction.request.InteractionRequest;
import com.aman.ainpc.interaction.request.InteractionSource;
import com.aman.ainpc.needs.NeedType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;
import java.util.UUID;

/**
 * Minecraft glue for NPC conversations.
 *
 * Responsibilities (Minecraft-specific only):
 *   - Forge event subscription
 *   - Checking whether the player is in an active conversation
 *   - Cancelling the chat event so the message is not broadcast
 *   - Resolving the NPC entity from the world (entity is the runtime's owner)
 *   - Creating the InteractionRequest and calling ConversationCoordinator
 *   - Sending "thinking" and reply messages to the player
 *   - Spawning need-driven emotion particles on the NPC
 *
 * All pipeline logic lives in ConversationCoordinator.
 *
 * Fix for B1: uses ServerChatEvent (server-side) instead of ClientChatEvent.
 * Fix for B2: LLM generates dialogue only; game state is owned by Needs system.
 */
@Mod.EventBusSubscriber(modid = AINPC.MODID)
public class ConversationHandler {

    private static final ConversationCoordinator COORDINATOR = new ConversationCoordinator();

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player     = event.getPlayer();
        UUID         playerUUID = player.getUUID();

        if (!ConversationManager.getInstance().isInConversation(playerUUID)) return;

        // Intercept — this message is for the NPC, not the chat channel
        event.setCanceled(true);

        Optional<UUID> npcUUID = ConversationManager.getInstance().getConversationNPC(playerUUID);
        if (npcUUID.isEmpty()) return;

        // Resolve the NPC entity — it is the authoritative owner of its runtime
        Entity entity = player.serverLevel().getEntity(npcUUID.get());
        AINPCEntity npc = entity instanceof AINPCEntity a ? a : null;

        // Build the raw interaction request and hand the full pipeline to the coordinator
        InteractionRequest request = new InteractionRequest(
                InteractionSource.PLAYER_CHAT,
                playerUUID,
                npcUUID.get(),
                event.getRawText(),
                System.currentTimeMillis()
        );

        COORDINATOR.handle(
                request,
                npc,
                player.getServer(),
                // onThinking — synchronous, before the AI thread starts
                npcName -> player.sendSystemMessage(
                        Component.literal("§7[" + npcName + " is thinking...]§r")),
                // onComplete — back on server thread after AI replies
                result -> {
                    player.sendSystemMessage(
                            Component.literal("§e[" + result.npcName + "]§r " + result.reply));
                    if (npc != null) {
                        spawnNeedsParticles(npc.getAgentRuntime(), player, npcUUID.get());
                    }
                }
        );
    }

    // ── Need-driven Particle Emotions (Minecraft-specific, stays here) ─

    private static void spawnNeedsParticles(AgentRuntime runtime, ServerPlayer player, UUID npcUUID) {
        if (runtime == null) return;
        Entity npcEntity = player.serverLevel().getEntity(npcUUID);
        if (!(npcEntity instanceof AINPCEntity npc)) return;
        if (!(npc.level() instanceof ServerLevel serverLevel)) return;

        double x = npc.getX();
        double y = npc.getY() + 2.2;
        double z = npc.getZ();

        float safety     = runtime.getNeedsManager().getNeed(NeedType.SAFETY).getValue();
        float loneliness = runtime.getNeedsManager().getNeed(NeedType.LONELINESS).getValue();
        float hunger     = runtime.getNeedsManager().getNeed(NeedType.HUNGER).getValue();

        if (safety > 0.5f) {
            serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER,  x, y, z, 3, 0.3, 0.2, 0.3, 0);
        } else if (loneliness < 0.2f) {
            serverLevel.sendParticles(ParticleTypes.HEART,           x, y, z, 3, 0.3, 0.2, 0.3, 0);
        } else if (hunger > 0.6f) {
            serverLevel.sendParticles(ParticleTypes.SPLASH,          x, y, z, 3, 0.3, 0.2, 0.3, 0.1);
        } else {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,  x, y, z, 3, 0.3, 0.2, 0.3, 0);
        }
    }
}
