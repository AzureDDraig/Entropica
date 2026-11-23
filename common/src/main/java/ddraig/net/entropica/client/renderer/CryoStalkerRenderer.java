package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.cryo_stalker.CryoStalkerEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class CryoStalkerRenderer extends MobRenderer<CryoStalkerEntity, CryoStalkerRenderState, CryoStalkerModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/cryo_stalker/cryo_stalker.png"
    );

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
        state.isSwiping = entity.isSwiping();
        state.stealthAlpha = entity.getStealthAlpha(partialTick);
    }

    @Override
    public ResourceLocation getTextureLocation(CryoStalkerRenderState state) {
        return TEXTURE;
    }

    @Override
    protected @Nullable RenderType getRenderType(CryoStalkerRenderState state, boolean bodyVisible, boolean translucent, boolean glowing) {
        if (!bodyVisible && !translucent) {
            return glowing ? RenderType.outline(getTextureLocation(state)) : null;
        }
        if (state.isStalking || state.stealthAlpha < 0.995F || translucent) {
            return RenderType.entityTranslucent(getTextureLocation(state));
        }
        return super.getRenderType(state, bodyVisible, false, glowing);
    }

    @Override
    protected int getModelTint(CryoStalkerRenderState state) {
        if (state.isStalking || state.stealthAlpha < 0.995F) {
            float alpha = Mth.clamp(state.stealthAlpha, 0.0F, 1.0F);
            float stealthBlend = 1.0F - Mth.clamp((alpha - CryoStalkerEntity.STEALTH_ALPHA) / (1.0F - CryoStalkerEntity.STEALTH_ALPHA), 0.0F, 1.0F);
            float r = Mth.lerp(stealthBlend, 1.0F, 0.78F);
            float g = Mth.lerp(stealthBlend, 1.0F, 0.94F);
            float b = 1.0F;
            return ARGB.colorFromFloat(alpha, r, g, b);
        }
        return 0xFFFFFFFF;
    }

    @Override
    protected float getShadowRadius(CryoStalkerRenderState state) {
        float base = super.getShadowRadius(state);
        if (state.isStalking) {
            return base * 0.14F;
        }
        return base * Mth.lerp(Mth.clamp(state.stealthAlpha, 0.0F, 1.0F), 0.14F, 1.0F);
    }
}
