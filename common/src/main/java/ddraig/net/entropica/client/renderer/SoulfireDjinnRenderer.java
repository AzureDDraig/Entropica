package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.model.SoulfireDjinnModel;
import ddraig.net.entropica.entity.soulfire_djinn.SoulfireDjinnEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class SoulfireDjinnRenderer extends MobRenderer<SoulfireDjinnEntity, SoulfireDjinnRenderState, SoulfireDjinnModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/soulfire_djinn/soulfire_djinn.png"
    );

    public SoulfireDjinnRenderer(EntityRendererProvider.Context context) {
        super(context, new SoulfireDjinnModel(context.bakeLayer(SoulfireDjinnModel.LAYER_LOCATION)), 0.52F);
    }

    @Override
    public SoulfireDjinnRenderState createRenderState() {
        return new SoulfireDjinnRenderState();
    }

    @Override
    public void extractRenderState(SoulfireDjinnEntity entity, SoulfireDjinnRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.glideTakeoffAnimationState.copyFrom(entity.glideTakeoffAnimationState);
        state.glideAnimationState.copyFrom(entity.glideAnimationState);
        state.glideLandingAnimationState.copyFrom(entity.glideLandingAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.phaseGlideAnimationState.copyFrom(entity.phaseGlideAnimationState);
        state.hurtAnimationState.copyFrom(entity.hurtAnimationState);
        state.deathAnimationState.copyFrom(entity.deathAnimationState);
        state.isGliding = (entity.getGlideMode() == SoulfireDjinnEntity.MODE_GLIDING);
        state.isConjuringOrb = entity.isConjuringOrb();
        state.isPhasingBars = entity.isPhasingBars();
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    @Override
    protected int getBlockLightLevel(SoulfireDjinnEntity entity, BlockPos pos) {
        if (entity.isDeadOrDying()) {
            return super.getBlockLightLevel(entity, pos);
        }
        int soulfireGlow = entity.isConjuringOrb() ? 15 : 13;
        return Math.max(soulfireGlow, super.getBlockLightLevel(entity, pos));
    }

    @Override
    public ResourceLocation getTextureLocation(SoulfireDjinnRenderState state) {
        return TEXTURE;
    }
}
