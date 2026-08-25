package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.ovis.PatinaGalvanicOvisEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class PatinaOvisRenderer extends MobRenderer<PatinaGalvanicOvisEntity, RimeBackOvisRenderState, RimeBackOvisModel> {
    private static final ResourceLocation PATINA_TEXTURE = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/ovis/patina_galvanic_ovis.png");

    public PatinaOvisRenderer(EntityRendererProvider.Context context) {
        super(context, new RimeBackOvisModel(context.bakeLayer(RimeBackOvisModel.LAYER_LOCATION)), 0.6F);
    }

    @Override
    public RimeBackOvisRenderState createRenderState() {
        return new RimeBackOvisRenderState();
    }

    @Override
    public void extractRenderState(PatinaGalvanicOvisEntity entity, RimeBackOvisRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.isSheared = entity.isSheared();
        state.isBraced = entity.isBraced();
        state.isStunned = entity.isStunned();
        state.isCharging = entity.isCharging();
        state.variant = 1;

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
        return PATINA_TEXTURE;
    }
}
