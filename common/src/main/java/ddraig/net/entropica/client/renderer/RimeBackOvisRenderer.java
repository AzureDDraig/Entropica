package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.ovis.RimeBackOvisEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RimeBackOvisRenderer extends MobRenderer<RimeBackOvisEntity, RimeBackOvisRenderState, RimeBackOvisModel> {
    private static final ResourceLocation GLACIAL_TEXTURE = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/ovis/rime_back_ovis.png");
    private static final ResourceLocation PATINA_TEXTURE = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/ovis/patina_back_ovis.png");

    public RimeBackOvisRenderer(EntityRendererProvider.Context context) {
        super(context, new RimeBackOvisModel(context.bakeLayer(RimeBackOvisModel.LAYER_LOCATION)), 0.6F);
    }

    @Override
    public RimeBackOvisRenderState createRenderState() {
        return new RimeBackOvisRenderState();
    }

    @Override
    public void extractRenderState(RimeBackOvisEntity entity, RimeBackOvisRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.isSheared = entity.isSheared();
        state.isBraced = entity.isBraced();
        state.isStunned = entity.isStunned();
        state.isCharging = entity.isCharging();
        state.variant = entity.getVariant();

        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.runAnimationState.copyFrom(entity.runAnimationState);
        state.grazeAnimationState.copyFrom(entity.grazeAnimationState);
        state.restAnimationState.copyFrom(entity.restAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.headbuttAnimationState.copyFrom(entity.headbuttAnimationState);
    }

    @Override
    public ResourceLocation getTextureLocation(RimeBackOvisRenderState state) {
        return state.variant == 1 ? PATINA_TEXTURE : GLACIAL_TEXTURE;
    }
}
