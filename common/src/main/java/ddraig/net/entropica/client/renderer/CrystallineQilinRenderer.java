package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.model.CrystallineQilinModel;
import ddraig.net.entropica.entity.crystalline_qilin.CrystallineQilinEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class CrystallineQilinRenderer extends MobRenderer<CrystallineQilinEntity, CrystallineQilinRenderState, CrystallineQilinModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/crystalline_qilin/crystalline_qilin.png"
    );

    public CrystallineQilinRenderer(EntityRendererProvider.Context context) {
        super(context, new CrystallineQilinModel(context.bakeLayer(CrystallineQilinModel.LAYER_LOCATION)), 0.75F);
    }

    @Override
    public CrystallineQilinRenderState createRenderState() {
        return new CrystallineQilinRenderState();
    }

    @Override
    public void extractRenderState(CrystallineQilinEntity entity, CrystallineQilinRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.runAnimationState.copyFrom(entity.runAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.lightRefractAnimationState.copyFrom(entity.lightRefractAnimationState);
        state.hurtAnimationState.copyFrom(entity.hurtAnimationState);
        state.deathAnimationState.copyFrom(entity.deathAnimationState);
        state.isRefracting = entity.isRefracting();
        state.isProvoked = entity.isProvoked();
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    @Override
    protected int getBlockLightLevel(CrystallineQilinEntity entity, BlockPos pos) {
        int minGlow = entity.isRefracting() ? 15 : 11;
        return Math.max(minGlow, super.getBlockLightLevel(entity, pos));
    }

    @Override
    public ResourceLocation getTextureLocation(CrystallineQilinRenderState state) {
        return TEXTURE;
    }
}
