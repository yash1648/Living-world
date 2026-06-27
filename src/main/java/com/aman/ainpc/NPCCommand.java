package com.aman.ainpc;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;

public class NPCCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("spawnnpc")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> {
                            ServerLevel level = context.getSource().getLevel();

                            AINPCEntity npc = new AINPCEntity(
                                    AINPC.AI_NPC.get(),
                                    level
                            );

                            npc.moveTo(
                                    context.getSource().getPosition().x,
                                    context.getSource().getPosition().y,
                                    context.getSource().getPosition().z
                            );

                            level.addFreshEntity(npc);

                            context.getSource().sendSuccess(
                                    () -> net.minecraft.network.chat.Component.literal("NPC spawned"),
                                    true
                            );

                            return 1;
                        })
        );
    }
}
