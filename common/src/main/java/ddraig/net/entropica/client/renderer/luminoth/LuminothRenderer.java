package ddraig.net.entropica.client.renderer.luminoth;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.luminoth.LuminothEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class LuminothRenderer extends MobRenderer<LuminothEntity, LuminothRenderState, LuminothModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/luminoth/luminoth.png");

    public LuminothRenderer(EntityRendererProvider.Context context) {
        super(context, new LuminothModel(context.bakeLayer(LuminothModel.LAYER_LOCATION)), 0.4F);
        this.addLayer(new LuminothGlowLayer(this));
    }

    @Override
    public LuminothRenderState createRenderState() {
        return new LuminothRenderState();
    }

    @Override
    public void extractRenderState(LuminothEntity entity, LuminothRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.flyAnimationState.copyFrom(entity.flyAnimationState);
    }

    @Override
    public ResourceLocation getTextureLocation(LuminothRenderState state) {
        return TEXTURE;
    }
}
