package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.model.BarrowCryptGlowOwlModel;
import ddraig.net.entropica.entity.barrow_crypt_glow_owl.BarrowCryptGlowOwlEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BarrowCryptGlowOwlRenderer extends MobRenderer<BarrowCryptGlowOwlEntity, BarrowCryptGlowOwlRenderState, BarrowCryptGlowOwlModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/barrow_crypt_glow_owl/barrow_crypt_glow_owl.png"
    );

    public BarrowCryptGlowOwlRenderer(EntityRendererProvider.Context context) {
        super(context, new BarrowCryptGlowOwlModel(context.bakeLayer(BarrowCryptGlowOwlModel.MODEL_LOCATION)), 0.38F);
    }

    @Override
    public BarrowCryptGlowOwlRenderState createRenderState() {
        return new BarrowCryptGlowOwlRenderState();
    }

    @Override
    public void extractRenderState(BarrowCryptGlowOwlEntity entity, BarrowCryptGlowOwlRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.flyAnimationState.copyFrom(entity.flyAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.isFlying = entity.isFlying();
    }

    @Override
    public ResourceLocation getTextureLocation(BarrowCryptGlowOwlRenderState state) {
        return TEXTURE;
    }
}
