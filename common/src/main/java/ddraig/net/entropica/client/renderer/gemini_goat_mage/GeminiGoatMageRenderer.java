package ddraig.net.entropica.client.renderer.gemini_goat_mage;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.gemini_goat_mage.GeminiGoatMageEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GeminiGoatMageRenderer extends MobRenderer<GeminiGoatMageEntity, GeminiGoatMageRenderState, GeminiGoatMageModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/gemini_goat_mage/gemini_goat_mage.png");

    public GeminiGoatMageRenderer(EntityRendererProvider.Context context) {
        super(context, new GeminiGoatMageModel(context.bakeLayer(GeminiGoatMageModel.LAYER_LOCATION)), 0.6F);
    }

    @Override
    public GeminiGoatMageRenderState createRenderState() {
        return new GeminiGoatMageRenderState();
    }

    @Override
    public void extractRenderState(GeminiGoatMageEntity entity, GeminiGoatMageRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.castSolarAnimationState.copyFrom(entity.castSolarAnimationState);
        state.castUmbralAnimationState.copyFrom(entity.castUmbralAnimationState);
        state.castDualAnimationState.copyFrom(entity.castDualAnimationState);
        state.castingPhase = entity.getCastingPhase();
        state.isWitnessingAltar = entity.isWitnessingAltar();
    }

    @Override
    public ResourceLocation getTextureLocation(GeminiGoatMageRenderState state) {
        return TEXTURE;
    }
}
