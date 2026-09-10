package ddraig.net.entropica.client.renderer.glacial_hydra;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.glacial_hydra.GlacialHydraEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GlacialHydraRenderer extends MobRenderer<GlacialHydraEntity, GlacialHydraRenderState, GlacialHydraModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/glacial_hydra/glacial_hydra.png");

    public GlacialHydraRenderer(EntityRendererProvider.Context context) {
        super(context, new GlacialHydraModel(context.bakeLayer(GlacialHydraModel.LAYER_LOCATION)), 0.9F);
    }

    @Override
    public GlacialHydraRenderState createRenderState() {
        return new GlacialHydraRenderState();
    }

    @Override
    public void extractRenderState(GlacialHydraEntity entity, GlacialHydraRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
    }

    @Override
    public ResourceLocation getTextureLocation(GlacialHydraRenderState state) {
        return TEXTURE;
    }
}
