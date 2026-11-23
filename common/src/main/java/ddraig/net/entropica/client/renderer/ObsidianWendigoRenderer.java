package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.model.ObsidianWendigoModel;
import ddraig.net.entropica.entity.obsidian_wendigo.ObsidianWendigoEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ObsidianWendigoRenderer extends MobRenderer<ObsidianWendigoEntity, ObsidianWendigoRenderState, ObsidianWendigoModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/obsidian_wendigo/obsidian_wendigo.png"
    );

    public ObsidianWendigoRenderer(EntityRendererProvider.Context context) {
        super(context, new ObsidianWendigoModel(context.bakeLayer(ObsidianWendigoModel.LAYER_LOCATION)), 0.65F);
    }

    @Override
    public ObsidianWendigoRenderState createRenderState() {
        return new ObsidianWendigoRenderState();
    }

    @Override
    public void extractRenderState(ObsidianWendigoEntity entity, ObsidianWendigoRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkStartAnimationState.copyFrom(entity.walkStartAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.walkStopAnimationState.copyFrom(entity.walkStopAnimationState);
        state.runStartAnimationState.copyFrom(entity.runStartAnimationState);
        state.runAnimationState.copyFrom(entity.runAnimationState);
        state.runStopAnimationState.copyFrom(entity.runStopAnimationState);
        state.climbStartAnimationState.copyFrom(entity.climbStartAnimationState);
        state.climbAnimationState.copyFrom(entity.climbAnimationState);
        state.climbStopAnimationState.copyFrom(entity.climbStopAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.screechAnimationState.copyFrom(entity.screechAnimationState);
        state.hurtAnimationState.copyFrom(entity.hurtAnimationState);
        state.deathAnimationState.copyFrom(entity.deathAnimationState);
        state.isSprintingRun = entity.isSprintingRun();
        state.isClimbingTrunk = entity.isClimbingTrunk();
        state.isScreeching = entity.isScreeching();
        state.isShadowCloaked = entity.isShadowCloaked();
        if (state.isShadowCloaked || state.isInvisible) {
            state.shadowRadius = 0.0F;
        }
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(ObsidianWendigoRenderState state) {
        return TEXTURE;
    }
}
