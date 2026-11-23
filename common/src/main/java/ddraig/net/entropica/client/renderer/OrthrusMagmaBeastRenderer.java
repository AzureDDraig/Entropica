package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.model.OrthrusMagmaBeastModel;
import ddraig.net.entropica.entity.orthrus_magma_beast.OrthrusMagmaBeastEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class OrthrusMagmaBeastRenderer extends MobRenderer<OrthrusMagmaBeastEntity, OrthrusMagmaBeastRenderState, OrthrusMagmaBeastModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/orthrus_magma_beast/orthrus_magma_beast.png"
    );

    public OrthrusMagmaBeastRenderer(EntityRendererProvider.Context context) {
        super(context, new OrthrusMagmaBeastModel(context.bakeLayer(OrthrusMagmaBeastModel.LAYER_LOCATION)), 1.05F);
    }

    @Override
    public OrthrusMagmaBeastRenderState createRenderState() {
        return new OrthrusMagmaBeastRenderState();
    }

    @Override
    public void extractRenderState(OrthrusMagmaBeastEntity entity, OrthrusMagmaBeastRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.runAnimationState.copyFrom(entity.runAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.snapComboAnimationState.copyFrom(entity.snapComboAnimationState);
        state.howlAnimationState.copyFrom(entity.howlAnimationState);
        state.hurtAnimationState.copyFrom(entity.hurtAnimationState);
        state.deathAnimationState.copyFrom(entity.deathAnimationState);
        state.isCharging = entity.isCharging();
        state.isMagmaEnergized = entity.isMagmaEnergized();
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    @Override
    protected int getBlockLightLevel(OrthrusMagmaBeastEntity entity, BlockPos pos) {
        int nexusLight = entity.isAlive() ? (entity.isMagmaEnergized() ? 14 : 10) : 4;
        return Math.max(nexusLight, super.getBlockLightLevel(entity, pos));
    }

    @Override
    public ResourceLocation getTextureLocation(OrthrusMagmaBeastRenderState state) {
        return TEXTURE;
    }
}
