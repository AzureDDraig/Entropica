package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.cryo_stalker.CryoStalkerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CryoStalkerRenderer extends MobRenderer<CryoStalkerEntity, CryoStalkerRenderState, CryoStalkerModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/cryo_stalker/cryo_stalker.png");

    public CryoStalkerRenderer(EntityRendererProvider.Context context) {
        super(context, new CryoStalkerModel(context.bakeLayer(CryoStalkerModel.LAYER_LOCATION)), 0.7F);
    }

    @Override
    public CryoStalkerRenderState createRenderState() {
        return new CryoStalkerRenderState();
    }

    @Override
    public void extractRenderState(CryoStalkerEntity entity, CryoStalkerRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.runAnimationState.copyFrom(entity.runAnimationState);
        state.jumpAnimationState.copyFrom(entity.jumpAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.swipeAnimationState.copyFrom(entity.swipeAnimationState);
        state.biteAnimationState.copyFrom(entity.biteAnimationState);
        state.isStalking = entity.isStalking();
        state.isAttacking = entity.isAttackingAnim();
        state.isLeaping = entity.isLeaping();
    }

    @Override
    public ResourceLocation getTextureLocation(CryoStalkerRenderState state) {
        return TEXTURE;
    }
}
