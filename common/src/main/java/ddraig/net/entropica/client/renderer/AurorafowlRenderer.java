package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.aurorafowl.AurorafowlEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class AurorafowlRenderer extends MobRenderer<AurorafowlEntity, AurorafowlRenderState, AurorafowlModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/aurorafowl/aurorafowl.png");

    public AurorafowlRenderer(EntityRendererProvider.Context context) {
        super(context, new AurorafowlModel(context.bakeLayer(AurorafowlModel.LAYER_LOCATION)), 0.6F);
    }

    @Override
    public AurorafowlRenderState createRenderState() {
        return new AurorafowlRenderState();
    }

    @Override
    public void extractRenderState(AurorafowlEntity entity, AurorafowlRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleGroundAnimationState.copyFrom(entity.idleGroundAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.idleFlightAnimationState.copyFrom(entity.idleFlightAnimationState);
        state.flyingAnimationState.copyFrom(entity.flyingAnimationState);
        state.takingOffAnimationState.copyFrom(entity.takingOffAnimationState);
        state.landingAnimationState.copyFrom(entity.landingAnimationState);
        state.attackGroundAnimationState.copyFrom(entity.attackGroundAnimationState);
        state.attackFlyingAnimationState.copyFrom(entity.attackFlyingAnimationState);
        state.isFlying = entity.isFlying();
        state.isSheared = entity.isSheared();
    }

    @Override
    public ResourceLocation getTextureLocation(AurorafowlRenderState state) {
        return TEXTURE;
    }
}
