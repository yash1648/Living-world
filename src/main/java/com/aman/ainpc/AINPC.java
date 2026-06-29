package com.aman.ainpc;

import com.aman.ainpc.client.AINPCClient;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

@Mod(AINPC.MODID)
public class AINPC {

    public static final String MODID = "ainpc";

    // ── Entity Registration ────────────────────────────────────────
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    public static final Supplier<EntityType<AINPCEntity>> AI_NPC =
            ENTITY_TYPES.register("ai_npc",
                    () -> EntityType.Builder.of(AINPCEntity::new, MobCategory.CREATURE)
                            .sized(0.6f, 1.8f)
                            .clientTrackingRange(10)
                            .build("ai_npc")
            );

    // ── Constructor ────────────────────────────────────────────────
    public AINPC(IEventBus modEventBus) {
        // Register entity types
        ENTITY_TYPES.register(modEventBus);

        // Register entity attributes
        modEventBus.addListener(this::onEntityAttributeCreation);

        // Register game event listeners (commands, etc.)
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);

        // Register mod config (FMLJavaModLoadingContext is Forge 47.x's stable approach)
        FMLJavaModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    // ── Event Handlers ─────────────────────────────────────────────

    private void onRegisterCommands(RegisterCommandsEvent event) {
        NPCCommand.register(event.getDispatcher());
    }

    private void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(AI_NPC.get(), AINPCEntity.createAttributes().build());
    }
}
