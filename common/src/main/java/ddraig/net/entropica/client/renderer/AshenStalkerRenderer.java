package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.model.AshenStalkerModel;
import ddraig.net.entropica.entity.ashen_stalker.AshenStalkerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class AshenStalkerRenderer extends MobRenderer<AshenStalkerEntity, AshenStalkerRenderState, AshenStalkerModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/ashen_stalker/ashen_stalker.png");

    public AshenStalkerRenderer(EntityRendererProvider.Context context) {
        super(context, new AshenStalkerModel(context.bakeLayer(AshenStalkerModel.LAYER_LOCATION)), 0.6F);
    }

    @Override
    public AshenStalkerRenderState createRenderState() {
        return new AshenStalkerRenderState();
    }

    @Override
    public void extractRenderState(AshenStalkerEntity entity, AshenStalkerRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);

        state.xRot = entity.getXRot();
        state.yRot = entity.yHeadRot;

        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.stalkAnimationState.copyFrom(entity.stalkAnimationState);
        state.sprintAnimationState.copyFrom(entity.sprintAnimationState);
        state.feedAnimationState.copyFrom(entity.feedAnimationState);
        state.scentAnimationState.copyFrom(entity.scentAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
    }

    @Override
    public ResourceLocation getTextureLocation(AshenStalkerRenderState state) {
        return TEXTURE;
    }
}