package ddraig.net.entropica.client.renderer.mammot;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.mammot.FrostTuskMammotEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class FrostTuskMammotRenderer extends MobRenderer<FrostTuskMammotEntity, FrostTuskMammotRenderState, FrostTuskMammotModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/mammot/frost_tusk_mammot.png"
    );
    private static final ResourceLocation SHEARED_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/mammot/frost_tusk_mammot_sheared.png"
    );

    public FrostTuskMammotRenderer(EntityRendererProvider.Context context) {
        super(context, new FrostTuskMammotModel(context.bakeLayer(FrostTuskMammotModel.LAYER_LOCATION)), 1.05F);
    }

    @Override
    public FrostTuskMammotRenderState createRenderState() {
        return new FrostTuskMammotRenderState();
    }

    @Override
    public void extractRenderState(FrostTuskMammotEntity entity, FrostTuskMammotRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.isSheared = entity.isSheared();
        state.isAttacking = entity.isAttacking();
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
    }

    @Override
    public ResourceLocation getTextureLocation(FrostTuskMammotRenderState state) {
        return state.isSheared ? SHEARED_TEXTURE : TEXTURE;
    }

    @Override
    protected @Nullable RenderType getRenderType(FrostTuskMammotRenderState state, boolean bodyVisible, boolean translucent, boolean glowing) {
        if (!bodyVisible && !translucent) {
            return glowing ? RenderType.outline(getTextureLocation(state)) : null;
        }
        return RenderType.entityTranslucent(getTextureLocation(state));
    }
}
