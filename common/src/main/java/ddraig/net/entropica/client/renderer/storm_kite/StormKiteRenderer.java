package ddraig.net.entropica.client.renderer.storm_kite;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.storm_kite.StormKiteEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class StormKiteRenderer extends MobRenderer<StormKiteEntity, StormKiteRenderState, StormKiteModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/storm_kite/storm_kite.png");

    public StormKiteRenderer(EntityRendererProvider.Context context) {
        super(context, new StormKiteModel(context.bakeLayer(StormKiteModel.LAYER_LOCATION)), 1.1F);
        this.addLayer(new StormKiteGlowLayer(this));
    }

    @Override
    public StormKiteRenderState createRenderState() {
        return new StormKiteRenderState();
    }

    @Override
    public void extractRenderState(StormKiteEntity entity, StormKiteRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.moveAnimationState.copyFrom(entity.moveAnimationState);
        state.flapAnimationState.copyFrom(entity.flapAnimationState);
        state.diveAnimationState.copyFrom(entity.diveAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
    }

    @Override
    public ResourceLocation getTextureLocation(StormKiteRenderState state) {
        return TEXTURE;
    }
}
