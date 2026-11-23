package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.model.SiltSphinxModel;
import ddraig.net.entropica.entity.silt_sphinx.SiltSphinxEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class SiltSphinxRenderer extends MobRenderer<SiltSphinxEntity, SiltSphinxRenderState, SiltSphinxModel> {
    private static final ResourceLocation TEXTURE_SILT_STONE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/silt_sphinx/silt_sphinx.png"
    );
    private static final ResourceLocation TEXTURE_PHARAOH = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/silt_sphinx/silt_sphinx_pharaoh.png"
    );

    public SiltSphinxRenderer(EntityRendererProvider.Context context) {
        super(context, new SiltSphinxModel(context.bakeLayer(SiltSphinxModel.LAYER_LOCATION)), 0.80F);
    }

    @Override
    public SiltSphinxRenderState createRenderState() {
        return new SiltSphinxRenderState();
    }

    @Override
    public void extractRenderState(SiltSphinxEntity entity, SiltSphinxRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.dormantAnimationState.copyFrom(entity.dormantAnimationState);
        state.awakenAnimationState.copyFrom(entity.awakenAnimationState);
        state.returnToDormantAnimationState.copyFrom(entity.returnToDormantAnimationState);
        state.isPharaohVariant = entity.isPharaohVariant();
        state.monumentState = entity.getMonumentState();
    }

    @Override
    protected int getBlockLightLevel(SiltSphinxEntity entity, BlockPos pos) {
        if (entity.isPharaohVariant() && entity.getMonumentState() == SiltSphinxEntity.STATE_ACTIVE) {
            return Math.max(5, super.getBlockLightLevel(entity, pos));
        }
        return super.getBlockLightLevel(entity, pos);
    }

    @Override
    public ResourceLocation getTextureLocation(SiltSphinxRenderState state) {
        return state.isPharaohVariant ? TEXTURE_PHARAOH : TEXTURE_SILT_STONE;
    }
}
