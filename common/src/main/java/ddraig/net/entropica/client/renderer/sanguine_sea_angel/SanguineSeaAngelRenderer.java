package ddraig.net.entropica.client.renderer.sanguine_sea_angel;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.sanguine_sea_angel.SanguineSeaAngelEntity;
import ddraig.net.entropica.entity.sanguine_sea_angel.SanguineSeaAngelVariant;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class SanguineSeaAngelRenderer extends MobRenderer<SanguineSeaAngelEntity, SanguineSeaAngelRenderState, SanguineSeaAngelModel> {

    private static final ResourceLocation TEXTURE_POLAR = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/sanguine_sea_angel/sanguine_sea_angel.png"
    );
    private static final ResourceLocation TEXTURE_ABYSSAL = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/sanguine_sea_angel/sanguine_sea_angel_abyssal.png"
    );

    public SanguineSeaAngelRenderer(EntityRendererProvider.Context context) {
        super(context, new SanguineSeaAngelModel(context.bakeLayer(SanguineSeaAngelModel.LAYER_LOCATION)), 0.32F);
    }

    @Override
    public SanguineSeaAngelRenderState createRenderState() {
        return new SanguineSeaAngelRenderState();
    }

    @Override
    public void extractRenderState(SanguineSeaAngelEntity entity, SanguineSeaAngelRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.swimAnimationState.copyFrom(entity.swimAnimationState);
        state.iceHoverAnimationState.copyFrom(entity.iceHoverAnimationState);
        state.iceGlideAnimationState.copyFrom(entity.iceGlideAnimationState);
        state.spitDartAnimationState.copyFrom(entity.spitDartAnimationState);
        state.lungeAttackAnimationState.copyFrom(entity.lungeAttackAnimationState);
        state.damagedAnimationState.copyFrom(entity.damagedAnimationState);
        state.deathAnimationState.copyFrom(entity.deathAnimationState);
        state.variant = entity.getVariant();
        state.inWater = entity.isInWater();
        state.hunting = entity.isHunting();
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(SanguineSeaAngelRenderState state) {
        return state.variant == SanguineSeaAngelVariant.ABYSSAL_GARNET ? TEXTURE_ABYSSAL : TEXTURE_POLAR;
    }

    @Override
    protected @Nullable RenderType getRenderType(SanguineSeaAngelRenderState state, boolean bodyVisible, boolean translucent, boolean glowing) {
        if (!bodyVisible && !translucent) {
            return glowing ? RenderType.outline(getTextureLocation(state)) : null;
        }
        return RenderType.entityTranslucent(getTextureLocation(state));
    }

    @Override
    protected int getBlockLightLevel(SanguineSeaAngelEntity entity, BlockPos pos) {
        return Math.max(entity.isHunting() ? 14 : 11, super.getBlockLightLevel(entity, pos));
    }
}
