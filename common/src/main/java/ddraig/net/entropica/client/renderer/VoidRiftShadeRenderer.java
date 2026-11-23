package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.model.VoidRiftShadeModel;
import ddraig.net.entropica.entity.void_rift_shade.VoidRiftShadeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class VoidRiftShadeRenderer extends MobRenderer<VoidRiftShadeEntity, VoidRiftShadeRenderState, VoidRiftShadeModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/void_rift_shade/void_rift_shade.png"
    );

    public VoidRiftShadeRenderer(EntityRendererProvider.Context context) {
        super(context, new VoidRiftShadeModel(context.bakeLayer(VoidRiftShadeModel.MODEL_LOCATION)), 0.65F);
    }

    @Override
    public VoidRiftShadeRenderState createRenderState() {
        return new VoidRiftShadeRenderState();
    }

    @Override
    public void extractRenderState(VoidRiftShadeEntity entity, VoidRiftShadeRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.blinkOutAnimationState.copyFrom(entity.blinkOutAnimationState);
        state.blinkInAnimationState.copyFrom(entity.blinkInAnimationState);
        state.aggroAnimationState.copyFrom(entity.aggroAnimationState);
        state.hurtAnimationState.copyFrom(entity.hurtAnimationState);
        state.hurtStaggerAnimationState.copyFrom(entity.hurtStaggerAnimationState);
        state.deathAnimationState.copyFrom(entity.deathAnimationState);
        state.isProvoked = entity.isProvoked();
        state.isRiftStabilized = entity.isRiftStabilized();
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(VoidRiftShadeRenderState state) {
        return TEXTURE;
    }
}
