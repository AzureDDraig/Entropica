package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.model.BrimstoneChimeraModel;
import ddraig.net.entropica.entity.brimstone_chimera.BrimstoneChimeraEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class BrimstoneChimeraRenderer extends MobRenderer<BrimstoneChimeraEntity, BrimstoneChimeraRenderState, BrimstoneChimeraModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/brimstone_chimera/brimstone_chimera.png"
    );

    public BrimstoneChimeraRenderer(EntityRendererProvider.Context context) {
        super(context, new BrimstoneChimeraModel(context.bakeLayer(BrimstoneChimeraModel.LAYER_LOCATION)), 0.85F);
    }

    @Override
    public BrimstoneChimeraRenderState createRenderState() {
        return new BrimstoneChimeraRenderState();
    }

    @Override
    public void extractRenderState(BrimstoneChimeraEntity entity, BrimstoneChimeraRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.runAnimationState.copyFrom(entity.runAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.magmaBreathAnimationState.copyFrom(entity.magmaBreathAnimationState);
        state.hurtAnimationState.copyFrom(entity.hurtAnimationState);
        state.deathAnimationState.copyFrom(entity.deathAnimationState);
        state.isGallopingRun = entity.isGallopingRun();
        state.isBreathingMagma = entity.isBreathingMagma();
        state.magmaBreathCharges = entity.getMagmaBreathCharges();
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    @Override
    protected int getBlockLightLevel(BrimstoneChimeraEntity entity, BlockPos pos) {
        if (entity.isDeadOrDying()) {
            return super.getBlockLightLevel(entity, pos);
        }
        int gulletGlow = entity.isBreathingMagma() ? 14 : (entity.getMagmaBreathCharges() > 0 ? 9 : 6);
        return Math.max(gulletGlow, super.getBlockLightLevel(entity, pos));
    }

    @Override
    public ResourceLocation getTextureLocation(BrimstoneChimeraRenderState state) {
        return TEXTURE;
    }
}
