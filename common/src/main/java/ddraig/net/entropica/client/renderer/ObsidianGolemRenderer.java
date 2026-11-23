package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.model.ObsidianGolemModel;
import ddraig.net.entropica.entity.obsidian_golem.ObsidianGolemEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class ObsidianGolemRenderer extends MobRenderer<ObsidianGolemEntity, ObsidianGolemRenderState, ObsidianGolemModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/obsidian_golem/obsidian_golem.png"
    );

    public ObsidianGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new ObsidianGolemModel(context.bakeLayer(ObsidianGolemModel.LAYER_LOCATION)), 0.88F);
    }

    @Override
    public ObsidianGolemRenderState createRenderState() {
        return new ObsidianGolemRenderState();
    }

    @Override
    public void extractRenderState(ObsidianGolemEntity entity, ObsidianGolemRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.spatialWaveAnimationState.copyFrom(entity.spatialWaveAnimationState);
        state.shieldBlockAnimationState.copyFrom(entity.shieldBlockAnimationState);
        state.hurtAnimationState.copyFrom(entity.hurtAnimationState);
        state.deathAnimationState.copyFrom(entity.deathAnimationState);
        state.isShieldWallLocked = entity.isShieldWallLocked();
        state.isPortalSurging = entity.isPortalSurging();
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    @Override
    protected int getBlockLightLevel(ObsidianGolemEntity entity, BlockPos pos) {
        if (entity.isDeadOrDying()) {
            return super.getBlockLightLevel(entity, pos);
        }
        int portalGlow = entity.isPortalSurging() ? 15 : 13;
        return Math.max(portalGlow, super.getBlockLightLevel(entity, pos));
    }

    @Override
    public ResourceLocation getTextureLocation(ObsidianGolemRenderState state) {
        return TEXTURE;
    }
}
