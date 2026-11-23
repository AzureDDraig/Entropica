package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.model.SoulSandHydraModel;
import ddraig.net.entropica.entity.soul_sand_hydra.SoulSandHydraEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class SoulSandHydraRenderer extends MobRenderer<SoulSandHydraEntity, SoulSandHydraRenderState, SoulSandHydraModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/soul_sand_hydra/soul_sand_hydra.png"
    );

    public SoulSandHydraRenderer(EntityRendererProvider.Context context) {
        super(context, new SoulSandHydraModel(context.bakeLayer(SoulSandHydraModel.LAYER_LOCATION)), 1.15F);
    }

    @Override
    public SoulSandHydraRenderState createRenderState() {
        return new SoulSandHydraRenderState();
    }

    @Override
    public void extractRenderState(SoulSandHydraEntity entity, SoulSandHydraRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.runAnimationState.copyFrom(entity.runAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.submergeAmbushAnimationState.copyFrom(entity.submergeAmbushAnimationState);
        state.witherRoarAnimationState.copyFrom(entity.witherRoarAnimationState);
        state.spellChargeAnimationState.copyFrom(entity.spellChargeAnimationState);
        state.spellReleaseAnimationState.copyFrom(entity.spellReleaseAnimationState);
        state.breathAttackAnimationState.copyFrom(entity.breathAttackAnimationState);
        state.hurtAnimationState.copyFrom(entity.hurtAnimationState);
        state.deathAnimationState.copyFrom(entity.deathAnimationState);
        state.isSubmergedAmbush = entity.isSubmergedAmbush();
        state.isChargingRun = entity.isChargingRun();
        state.isGlandGlowing = entity.isGlandGlowing();
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    @Override
    protected int getBlockLightLevel(SoulSandHydraEntity entity, BlockPos pos) {
        int mawLight;
        if (entity.isGlandGlowing()) {
            mawLight = 14;
        } else if (entity.isSubmergedAmbush()) {
            mawLight = 6;
        } else {
            mawLight = 10;
        }
        return Math.max(mawLight, super.getBlockLightLevel(entity, pos));
    }

    @Override
    public ResourceLocation getTextureLocation(SoulSandHydraRenderState state) {
        return TEXTURE;
    }
}
