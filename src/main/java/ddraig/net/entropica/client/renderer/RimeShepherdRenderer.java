package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.RimeShepherdEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RimeShepherdRenderer extends MobRenderer<RimeShepherdEntity, RimeShepherdRenderState, RimeShepherdModel> {
    private static final ResourceLocation SHEPHERD_TEXTURE = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/rime_shepherd/rime_shepherd.png");

    public RimeShepherdRenderer(EntityRendererProvider.Context context) {
        super(context, new RimeShepherdModel(context.bakeLayer(RimeShepherdModel.LAYER_LOCATION)), 0.8F);
    }

    @Override
    public RimeShepherdRenderState createRenderState() {
        return new RimeShepherdRenderState();
    }

    @Override
    public void extractRenderState(RimeShepherdEntity entity, RimeShepherdRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.runAnimationState.copyFrom(entity.runAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
    }

    @Override
    public ResourceLocation getTextureLocation(RimeShepherdRenderState state) {
        return SHEPHERD_TEXTURE;
    }
}
