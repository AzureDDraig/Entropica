package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.model.MagmaCherufeModel;
import ddraig.net.entropica.entity.magma_cherufe.MagmaCherufeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class MagmaCherufeRenderer extends MobRenderer<MagmaCherufeEntity, MagmaCherufeRenderState, MagmaCherufeModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/magma_cherufe/magma_cherufe.png"
    );

    public MagmaCherufeRenderer(EntityRendererProvider.Context context) {
        super(context, new MagmaCherufeModel(context.bakeLayer(MagmaCherufeModel.LAYER_LOCATION)), 0.95F);
    }

    @Override
    public MagmaCherufeRenderState createRenderState() {
        return new MagmaCherufeRenderState();
    }

    @Override
    public void extractRenderState(MagmaCherufeEntity entity, MagmaCherufeRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.climbAnimationState.copyFrom(entity.climbAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.tailWhipAnimationState.copyFrom(entity.tailWhipAnimationState);
        state.hurtAnimationState.copyFrom(entity.hurtAnimationState);
        state.deathAnimationState.copyFrom(entity.deathAnimationState);
        state.isClimbing = entity.isClimbing();
        state.isLavaBathing = entity.isLavaBathing();
    }

    @Override
    protected float getFlipDegrees() {
        // Custom belly-settle death animation keeps the model upright rather than tipping 90 degrees sideways
        return 0.0F;
    }

    @Override
    protected int getBlockLightLevel(MagmaCherufeEntity entity, BlockPos pos) {
        int ventGlow = entity.isAlive() ? (entity.isLavaBathing() ? 13 : 9) : 4;
        return Math.max(ventGlow, super.getBlockLightLevel(entity, pos));
    }

    @Override
    public ResourceLocation getTextureLocation(MagmaCherufeRenderState state) {
        return TEXTURE;
    }
}
