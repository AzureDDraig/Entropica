package ddraig.net.entropica.client.renderer.void_sea_serpent;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.void_sea_serpent.VoidSeaSerpentEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class VoidSeaSerpentRenderer extends MobRenderer<VoidSeaSerpentEntity, VoidSeaSerpentRenderState, VoidSeaSerpentModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/void_sea_serpent/void_sea_serpent.png");

    public VoidSeaSerpentRenderer(EntityRendererProvider.Context context) {
        super(context, new VoidSeaSerpentModel(context.bakeLayer(VoidSeaSerpentModel.LAYER_LOCATION)), 1.2F);
        this.addLayer(new VoidSeaSerpentEyesLayer(this));
    }

    @Override
    public VoidSeaSerpentRenderState createRenderState() {
        return new VoidSeaSerpentRenderState();
    }

    @Override
    public void extractRenderState(VoidSeaSerpentEntity entity, VoidSeaSerpentRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.swimAnimationState.copyFrom(entity.swimAnimationState);
        state.biteAnimationState.copyFrom(entity.biteAnimationState);
        state.roarAnimationState.copyFrom(entity.roarAnimationState);
    }

    @Override
    public ResourceLocation getTextureLocation(VoidSeaSerpentRenderState state) {
        return TEXTURE;
    }
}
