package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.ovis.OvergrowthOvisEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class OvergrowthOvisRenderer extends MobRenderer<OvergrowthOvisEntity, RimeBackOvisRenderState, RimeBackOvisModel> {
    private static final ResourceLocation MOSSY_TEXTURE = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/ovis/mossy_back_ovis.png");

    public OvergrowthOvisRenderer(EntityRendererProvider.Context context) {
        super(context, new RimeBackOvisModel(context.bakeLayer(RimeBackOvisModel.LAYER_LOCATION)), 0.6F);
    }

    @Override
    public RimeBackOvisRenderState createRenderState() {
        return new RimeBackOvisRenderState();
    }

    @Override
    public void extractRenderState(OvergrowthOvisEntity entity, RimeBackOvisRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.isSheared = entity.isSheared();
        state.isBraced = entity.isBraced();
        state.isStunned = entity.isStunned();
        state.variant = 0; // Mossy is single variant
    }

    @Override
    public ResourceLocation getTextureLocation(RimeBackOvisRenderState state) {
        return MOSSY_TEXTURE;
    }
}
