package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.model.VeilFoxModel;
import ddraig.net.entropica.entity.veil_fox.VeilFoxEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class VeilFoxRenderer extends MobRenderer<VeilFoxEntity, VeilFoxRenderState, VeilFoxModel> {

    private static final ResourceLocation FOX_TEXTURE = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/veil_fox/veil_fox.png");

    public VeilFoxRenderer(EntityRendererProvider.Context context) {
        super(context, new VeilFoxModel(context.bakeLayer(VeilFoxModel.LAYER_LOCATION)), 0.4F);
        this.addLayer(new VeilFoxEyesLayer(this));
    }

    @Override
    public VeilFoxRenderState createRenderState() {
        return new VeilFoxRenderState();
    }

    @Override
    public void extractRenderState(VeilFoxEntity entity, VeilFoxRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.essenceType = entity.getEssenceType();
        state.isSitting = entity.isOrderedToSit();

        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.blinkOutAnimationState.copyFrom(entity.blinkOutAnimationState);
        state.blinkInAnimationState.copyFrom(entity.blinkInAnimationState);
        state.sleepAnimationState.copyFrom(entity.sleepAnimationState);
        state.sleepInAnimationState.copyFrom(entity.sleepInAnimationState);
        state.sleepOutAnimationState.copyFrom(entity.sleepOutAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.rolloverAnimationState.copyFrom(entity.rolloverAnimationState);
        state.digAnimationState.copyFrom(entity.digAnimationState);

        if (entity.getDeltaMovement().horizontalDistanceSqr() < 0.0001D && !entity.isTame()) {
            state.strobeAlpha = 0.3f + 0.7f * (float)Math.abs(Math.sin((entity.tickCount + partialTick) * 0.1f));
        } else {
            state.strobeAlpha = 1.0f;
        }
    }

    @Override
    public ResourceLocation getTextureLocation(VeilFoxRenderState state) {
        return FOX_TEXTURE;
    }

    @Override
    protected RenderType getRenderType(VeilFoxRenderState state, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderType.entityTranslucent(getTextureLocation(state));
    }
}