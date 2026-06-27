package com.aman.ainpc.client;

import com.aman.ainpc.AINPC;
import com.aman.ainpc.AINPCEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AINPC.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AINPCClient {

    public static final ModelLayerLocation AI_NPC_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AINPC.MODID, "ai_npc"), "main"
    );

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(AI_NPC_LAYER, () -> LayerDefinition.create(
                HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 64
        ));
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(AINPC.AI_NPC.get(), AINPCMobRenderer::new);
    }

    // ── Renderer ───────────────────────────────────────────────────

    private static class AINPCMobRenderer extends MobRenderer<AINPCEntity, HumanoidModel<AINPCEntity>> {

        private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/steve.png");

        public AINPCMobRenderer(EntityRendererProvider.Context context) {
            super(context, new HumanoidModel<>(context.bakeLayer(AI_NPC_LAYER)), 0.5F);
        }

        @Override
        public ResourceLocation getTextureLocation(AINPCEntity entity) {
            return TEXTURE;
        }
    }
}
