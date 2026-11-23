package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.model.ObsidianScorpioidModel;
import ddraig.net.entropica.entity.obsidian_scorpioid.ObsidianScorpioidEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class ObsidianScorpioidRenderer extends MobRenderer<ObsidianScorpioidEntity, ObsidianScorpioidRenderState, ObsidianScorpioidModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/obsidian_scorpioid/obsidian_scorpioid.png"
    );

    public ObsidianScorpioidRenderer(EntityRendererProvider.Context context) {
        super(context, new ObsidianScorpioidModel(context.bakeLayer(ObsidianScorpioidModel.LAYER_LOCATION)), 0.92F);
    }

    @Override
    public ObsidianScorpioidRenderState createRenderState() {
        return new ObsidianScorpioidRenderState();
    }

    @Override
    public void extractRenderState(ObsidianScorpioidEntity entity, ObsidianScorpioidRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.pincerClampAnimationState.copyFrom(entity.pincerClampAnimationState);
        state.hurtAnimationState.copyFrom(entity.hurtAnimationState);
        state.deathAnimationState.copyFrom(entity.deathAnimationState);
        state.isClimbing = entity.isClimbing();
        state.isStingerFlaring = entity.isStingerFlaring();
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    @Override
    protected int getBlockLightLevel(ObsidianScorpioidEntity entity, BlockPos pos) {
        int stingerLight = entity.isStingerFlaring() ? 14 : 10;
        return Math.max(stingerLight, super.getBlockLightLevel(entity, pos));
    }

    @Override
    public ResourceLocation getTextureLocation(ObsidianScorpioidRenderState state) {
        return TEXTURE;
    }
}
