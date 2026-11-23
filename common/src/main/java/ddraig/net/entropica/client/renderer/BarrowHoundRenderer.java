package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.model.BarrowHoundModel;
import ddraig.net.entropica.entity.barrow_hound.BarrowHoundEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BarrowHoundRenderer extends MobRenderer<BarrowHoundEntity, BarrowHoundRenderState, BarrowHoundModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/barrow_hound/barrow_hound.png"
    );

    public BarrowHoundRenderer(EntityRendererProvider.Context context) {
        super(context, new BarrowHoundModel(context.bakeLayer(BarrowHoundModel.MODEL_LOCATION)), 0.5F);
    }

    @Override
    public BarrowHoundRenderState createRenderState() {
        return new BarrowHoundRenderState();
    }

    @Override
    public void extractRenderState(BarrowHoundEntity entity, BarrowHoundRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.howlAnimationState.copyFrom(entity.howlAnimationState);
        state.isTombBound = entity.isTombBound();
    }

    @Override
    public ResourceLocation getTextureLocation(BarrowHoundRenderState state) {
        return TEXTURE;
    }
}
