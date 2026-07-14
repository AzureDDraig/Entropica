package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.model.VeilFoxModel;
import ddraig.net.entropica.entity.veil_fox.VeilFoxAfterimageEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class VeilFoxAfterimageRenderer extends MobRenderer<VeilFoxAfterimageEntity, VeilFoxRenderState, VeilFoxModel> {
    private static final ResourceLocation FOX_TEXTURE = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/veil_fox/veil_fox.png");

    public VeilFoxAfterimageRenderer(EntityRendererProvider.Context context) {
        super(context, new VeilFoxModel(context.bakeLayer(VeilFoxModel.LAYER_LOCATION)), 0.0F);
    }

    @Override
    public VeilFoxRenderState createRenderState() {
        return new VeilFoxRenderState();
    }

    @Override
    public void extractRenderState(VeilFoxAfterimageEntity entity, VeilFoxRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);

        state.isSitting = entity.isSitting();

        state.xRot = entity.getXRot();
        state.yRot = entity.getYHeadRot();
        state.bodyRot = entity.getYBodyRot();

        state.strobeAlpha = Math.max(0.0f, 1.0f - ((entity.tickCount + partialTick) / 20.0f));

        state.walkAnimationSpeed = 0.0f;
        state.walkAnimationPos = 0.0f;
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