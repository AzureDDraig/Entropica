package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.model.MirageFataSpriteModel;
import ddraig.net.entropica.entity.mirage_fata_sprite.MirageFataSpriteEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class MirageFataSpriteRenderer<T extends MirageFataSpriteEntity> extends MobRenderer<T, MirageFataSpriteRenderState, MirageFataSpriteModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/mirage_fata_sprite/mirage_fata_sprite.png"
    );

    public MirageFataSpriteRenderer(EntityRendererProvider.Context context) {
        super(context, new MirageFataSpriteModel(context.bakeLayer(MirageFataSpriteModel.LAYER_LOCATION)), 0.45F);
    }

    @Override
    public MirageFataSpriteRenderState createRenderState() {
        return new MirageFataSpriteRenderState();
    }

    @Override
    public void extractRenderState(T entity, MirageFataSpriteRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.mirageIllusionAnimationState.copyFrom(entity.mirageIllusionAnimationState);
        state.hurtAnimationState.copyFrom(entity.hurtAnimationState);
        state.deathAnimationState.copyFrom(entity.deathAnimationState);
        state.isIllusionActive = entity.isIllusionActive();
        state.isSolarFocusing = entity.isSolarFocusing();
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    @Override
    protected int getBlockLightLevel(T entity, BlockPos pos) {
        int coreLight = (entity.isSolarFocusing() || entity.isIllusionActive()) ? 15 : 12;
        return Math.max(coreLight, super.getBlockLightLevel(entity, pos));
    }

    @Override
    public ResourceLocation getTextureLocation(MirageFataSpriteRenderState state) {
        return TEXTURE;
    }
}
