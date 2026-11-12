package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.SporeDrifterEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SporeDrifterRenderer extends MobRenderer<SporeDrifterEntity, SporeDrifterRenderState, SporeDrifterModel> {
    private static final ResourceLocation DRIFTER_TEXTURE = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/spore_drifter.png");

    public SporeDrifterRenderer(EntityRendererProvider.Context context) {
        super(context, new SporeDrifterModel(context.bakeLayer(SporeDrifterModel.LAYER_LOCATION)), 0.5F);
        this.addLayer(new SporeDrifterGlowLayer(this));
    }

    @Override
    public SporeDrifterRenderState createRenderState() {
        return new SporeDrifterRenderState();
    }

    @Override
    public void extractRenderState(SporeDrifterEntity entity, SporeDrifterRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
    }

    @Override
    public ResourceLocation getTextureLocation(SporeDrifterRenderState state) {
        return DRIFTER_TEXTURE;
    }

    @Override
    protected RenderType getRenderType(SporeDrifterRenderState state, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderType.entityTranslucent(getTextureLocation(state));
    }
}
