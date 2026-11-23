package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.model.AshCinderWispModel;
import ddraig.net.entropica.entity.ash_cinder_wisp.AshCinderWispEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class AshCinderWispRenderer extends MobRenderer<AshCinderWispEntity, AshCinderWispRenderState, AshCinderWispModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/ash_cinder_wisp/ash_cinder_wisp.png"
    );

    public AshCinderWispRenderer(EntityRendererProvider.Context context) {
        super(context, new AshCinderWispModel(context.bakeLayer(AshCinderWispModel.LAYER_LOCATION)), 0.42F);
    }

    @Override
    public AshCinderWispRenderState createRenderState() {
        return new AshCinderWispRenderState();
    }

    @Override
    public void extractRenderState(AshCinderWispEntity entity, AshCinderWispRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.flutterLightAnimationState.copyFrom(entity.flutterLightAnimationState);
        state.hurtAnimationState.copyFrom(entity.hurtAnimationState);
        state.deathAnimationState.copyFrom(entity.deathAnimationState);
        state.variant = entity.getVariant();
        state.isAshPuffing = entity.isAshPuffing();
        state.isFlutteringLight = entity.isFlutteringLight();
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    @Override
    protected int getBlockLightLevel(AshCinderWispEntity entity, BlockPos pos) {
        int coreLight;
        if (entity.isDeadOrDying()) {
            coreLight = 8;
        } else if (entity.isFlutteringLight() || entity.isAshPuffing()) {
            coreLight = 15;
        } else {
            coreLight = 13;
        }
        return Math.max(coreLight, super.getBlockLightLevel(entity, pos));
    }

    @Override
    public ResourceLocation getTextureLocation(AshCinderWispRenderState state) {
        return TEXTURE;
    }
}
