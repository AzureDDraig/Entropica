package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.model.VoidArachneModel;
import ddraig.net.entropica.entity.void_arachne.VoidArachneEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class VoidArachneRenderer extends MobRenderer<VoidArachneEntity, VoidArachneRenderState, VoidArachneModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/void_arachne/void_arachne.png"
    );

    public VoidArachneRenderer(EntityRendererProvider.Context context) {
        super(context, new VoidArachneModel(context.bakeLayer(VoidArachneModel.LAYER_LOCATION)), 0.85F);
    }

    @Override
    public VoidArachneRenderState createRenderState() {
        return new VoidArachneRenderState();
    }

    @Override
    public void extractRenderState(VoidArachneEntity entity, VoidArachneRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.biteAnimationState.copyFrom(entity.biteAnimationState);
        state.blinkOutAnimationState.copyFrom(entity.blinkOutAnimationState);
        state.blinkInAnimationState.copyFrom(entity.blinkInAnimationState);
        state.hurtAnimationState.copyFrom(entity.hurtAnimationState);
        state.deathAnimationState.copyFrom(entity.deathAnimationState);
        state.isClimbing = entity.isClimbing();
        state.isBlinkPhasing = entity.isBlinkPhasing();
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    @Override
    protected int getBlockLightLevel(VoidArachneEntity entity, BlockPos pos) {
        int runeGlow = entity.isAlive() ? (entity.isBlinkPhasing() ? 13 : 8) : 3;
        return Math.max(runeGlow, super.getBlockLightLevel(entity, pos));
    }

    @Override
    public ResourceLocation getTextureLocation(VoidArachneRenderState state) {
        return TEXTURE;
    }
}
