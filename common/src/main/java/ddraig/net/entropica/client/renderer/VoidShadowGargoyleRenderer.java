package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.model.VoidShadowGargoyleModel;
import ddraig.net.entropica.entity.void_shadow_gargoyle.VoidShadowGargoyleEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class VoidShadowGargoyleRenderer extends MobRenderer<VoidShadowGargoyleEntity, VoidShadowGargoyleRenderState, VoidShadowGargoyleModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/void_shadow_gargoyle/void_shadow_gargoyle.png"
    );

    public VoidShadowGargoyleRenderer(EntityRendererProvider.Context context) {
        super(context, new VoidShadowGargoyleModel(context.bakeLayer(VoidShadowGargoyleModel.LAYER_LOCATION)), 0.72F);
    }

    @Override
    public VoidShadowGargoyleRenderState createRenderState() {
        return new VoidShadowGargoyleRenderState();
    }

    @Override
    public void extractRenderState(VoidShadowGargoyleEntity entity, VoidShadowGargoyleRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.flyAnimationState.copyFrom(entity.flyAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.blinkOutAnimationState.copyFrom(entity.blinkOutAnimationState);
        state.blinkInAnimationState.copyFrom(entity.blinkInAnimationState);
        state.hurtAnimationState.copyFrom(entity.hurtAnimationState);
        state.deathAnimationState.copyFrom(entity.deathAnimationState);
        state.isAirborneFlight = entity.isAirborneFlight();
        state.isBlinkPhasing = entity.isBlinkPhasing();
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    @Override
    protected int getBlockLightLevel(VoidShadowGargoyleEntity entity, BlockPos pos) {
        int veinLight = entity.isBlinkPhasing() ? 14 : 9;
        return Math.max(veinLight, super.getBlockLightLevel(entity, pos));
    }

    @Override
    public ResourceLocation getTextureLocation(VoidShadowGargoyleRenderState state) {
        return TEXTURE;
    }
}
