package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.model.ZephyrSimurghModel;
import ddraig.net.entropica.entity.zephyr_simurgh.ZephyrSimurghEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class ZephyrSimurghRenderer extends MobRenderer<ZephyrSimurghEntity, ZephyrSimurghRenderState, ZephyrSimurghModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/zephyr_simurgh/zephyr_simurgh.png"
    );

    public ZephyrSimurghRenderer(EntityRendererProvider.Context context) {
        super(context, new ZephyrSimurghModel(context.bakeLayer(ZephyrSimurghModel.LAYER_LOCATION)), 0.70F);
    }

    @Override
    public ZephyrSimurghRenderState createRenderState() {
        return new ZephyrSimurghRenderState();
    }

    @Override
    public void extractRenderState(ZephyrSimurghEntity entity, ZephyrSimurghRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.flyAnimationState.copyFrom(entity.flyAnimationState);
        state.takeoffAnimationState.copyFrom(entity.takeoffAnimationState);
        state.landingAnimationState.copyFrom(entity.landingAnimationState);
        state.flyAttackAnimationState.copyFrom(entity.flyAttackAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.screechAnimationState.copyFrom(entity.screechAnimationState);
        state.flightMode = entity.getFlightMode();
        state.isStormCharged = entity.isStormCharged();
    }

    @Override
    protected int getBlockLightLevel(ZephyrSimurghEntity entity, BlockPos pos) {
        int copperGlow = entity.isStormCharged() ? 7 : 4;
        return Math.max(copperGlow, super.getBlockLightLevel(entity, pos));
    }

    @Override
    public ResourceLocation getTextureLocation(ZephyrSimurghRenderState state) {
        return TEXTURE;
    }
}
