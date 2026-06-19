package com.aman.ainpc;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(AINPC.MODID)
public class AINPC {

    public static final String MODID = "ainpc";  // ✅ ADD THIS

    public AINPC() {
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        NPCCommand.register(event.getDispatcher());
    }
}