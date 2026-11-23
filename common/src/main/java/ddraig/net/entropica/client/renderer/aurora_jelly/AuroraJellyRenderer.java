package ddraig.net.entropica.client.renderer.aurora_jelly;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.aurora_jelly.AuroraJellyEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class AuroraJellyRenderer extends MobRenderer<AuroraJellyEntity, AuroraJellyRenderState, AuroraJellyModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/aurora_jelly/aurora_jelly.png"
    );

    public AuroraJellyRenderer(EntityRendererProvider.Context context) {
        super(context, new AuroraJellyModel(context.bakeLayer(AuroraJellyModel.LAYER_LOCATION)), 0.35F);
    }

    @Override
    public AuroraJellyRenderState createRenderState() {
        return new AuroraJellyRenderState();
    }

    @Override
    public void extractRenderState(AuroraJellyEntity entity, AuroraJellyRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.isStartled = entity.isStartled();
        state.isMagnetizing = entity.isMagnetizing();
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.flyAnimationState.copyFrom(entity.flyAnimationState);
        state.magnetizeAnimationState.copyFrom(entity.magnetizeAnimationState);
        state.startledFlashAnimationState.copyFrom(entity.startledFlashAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.deathAnimationState.copyFrom(entity.deathAnimationState);
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(AuroraJellyRenderState state) {
        return TEXTURE;
    }

    @Override
    protected @Nullable RenderType getRenderType(AuroraJellyRenderState state, boolean bodyVisible, boolean translucent, boolean glowing) {
        if (!bodyVisible && !translucent) {
            return glowing ? RenderType.outline(getTextureLocation(state)) : null;
        }
        return RenderType.entityTranslucent(getTextureLocation(state));
    }

    @Override
    protected int getBlockLightLevel(AuroraJellyEntity entity, BlockPos pos) {
        int minGlow = entity.isStartled() ? 15 : (entity.isMagnetizing() ? 13 : 11);
        return Math.max(minGlow, super.getBlockLightLevel(entity, pos));
    }
}
