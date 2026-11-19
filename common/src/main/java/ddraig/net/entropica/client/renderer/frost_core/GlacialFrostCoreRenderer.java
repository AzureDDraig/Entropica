package ddraig.net.entropica.client.renderer.frost_core;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.frost_core.GlacialFrostCoreEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class GlacialFrostCoreRenderer extends MobRenderer<GlacialFrostCoreEntity, GlacialFrostCoreRenderState, GlacialFrostCoreModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/frost_core/glacial_frost_core.png"
    );

    public GlacialFrostCoreRenderer(EntityRendererProvider.Context context) {
        super(context, new GlacialFrostCoreModel(context.bakeLayer(GlacialFrostCoreModel.LAYER_LOCATION)), 0.35F);
    }

    @Override
    public GlacialFrostCoreRenderState createRenderState() {
        return new GlacialFrostCoreRenderState();
    }

    @Override
    public void extractRenderState(GlacialFrostCoreEntity entity, GlacialFrostCoreRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.isAttacking = entity.isAttacking();
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
    }

    @Override
    public ResourceLocation getTextureLocation(GlacialFrostCoreRenderState state) {
        return TEXTURE;
    }

    @Override
    protected @Nullable RenderType getRenderType(GlacialFrostCoreRenderState state, boolean bodyVisible, boolean translucent, boolean glowing) {
        if (!bodyVisible && !translucent) {
            return glowing ? RenderType.outline(getTextureLocation(state)) : null;
        }
        return RenderType.entityTranslucent(getTextureLocation(state));
    }

    @Override
    protected int getBlockLightLevel(GlacialFrostCoreEntity entity, BlockPos pos) {
        return Math.max(9, super.getBlockLightLevel(entity, pos));
    }
}
