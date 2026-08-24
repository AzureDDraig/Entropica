package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.BloomCrawlerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BloomCrawlerRenderer extends MobRenderer<BloomCrawlerEntity, BloomCrawlerRenderState, BloomCrawlerModel> {
    private static final ResourceLocation CRAWLER_TEXTURE = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/bloom_crawler/bloom_crawler.png");

    public BloomCrawlerRenderer(EntityRendererProvider.Context context) {
        super(context, new BloomCrawlerModel(context.bakeLayer(BloomCrawlerModel.LAYER_LOCATION)), 0.4F);
    }

    @Override
    public BloomCrawlerRenderState createRenderState() {
        return new BloomCrawlerRenderState();
    }

    @Override
    public void extractRenderState(BloomCrawlerEntity entity, BloomCrawlerRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.runAnimationState.copyFrom(entity.runAnimationState);
        state.grazeAnimationState.copyFrom(entity.grazeAnimationState);
        state.shellAge = entity.getShellAge();
    }

    @Override
    public ResourceLocation getTextureLocation(BloomCrawlerRenderState state) {
        return CRAWLER_TEXTURE;
    }
}
