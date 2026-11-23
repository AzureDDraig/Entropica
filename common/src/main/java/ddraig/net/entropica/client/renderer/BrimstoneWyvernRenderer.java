package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.model.BrimstoneWyvernModel;
import ddraig.net.entropica.entity.brimstone_wyvern.BrimstoneWyvernEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class BrimstoneWyvernRenderer extends MobRenderer<BrimstoneWyvernEntity, BrimstoneWyvernRenderState, BrimstoneWyvernModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/brimstone_wyvern/brimstone_wyvern.png"
    );

    public BrimstoneWyvernRenderer(EntityRendererProvider.Context context) {
        super(context, new BrimstoneWyvernModel(context.bakeLayer(BrimstoneWyvernModel.LAYER_LOCATION)), 0.92F);
    }

    @Override
    public BrimstoneWyvernRenderState createRenderState() {
        return new BrimstoneWyvernRenderState();
    }

    @Override
    public void extractRenderState(BrimstoneWyvernEntity entity, BrimstoneWyvernRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.swimAnimationState.copyFrom(entity.swimAnimationState);
        state.flyAnimationState.copyFrom(entity.flyAnimationState);
        state.takeoffAnimationState.copyFrom(entity.takeoffAnimationState);
        state.landingAnimationState.copyFrom(entity.landingAnimationState);
        state.flyingAttackAnimationState.copyFrom(entity.flyingAttackAnimationState);
        state.flyingFireballSpitAnimationState.copyFrom(entity.flyingFireballSpitAnimationState);
        state.flyingHurtAnimationState.copyFrom(entity.flyingHurtAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.fireballSpitAnimationState.copyFrom(entity.fireballSpitAnimationState);
        state.hurtAnimationState.copyFrom(entity.hurtAnimationState);
        state.deathAnimationState.copyFrom(entity.deathAnimationState);
        state.isAirborne = (entity.getFlightMode() == BrimstoneWyvernEntity.MODE_FLYING);
        state.isMagmaCharged = entity.isMagmaCharged();
        state.isLavaSwimming = entity.isLavaSwimming();
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    @Override
    protected int getBlockLightLevel(BrimstoneWyvernEntity entity, BlockPos pos) {
        int crystalGlow = entity.isMagmaCharged() ? 13 : 9;
        return Math.max(crystalGlow, super.getBlockLightLevel(entity, pos));
    }

    @Override
    public ResourceLocation getTextureLocation(BrimstoneWyvernRenderState state) {
        return TEXTURE;
    }
}
