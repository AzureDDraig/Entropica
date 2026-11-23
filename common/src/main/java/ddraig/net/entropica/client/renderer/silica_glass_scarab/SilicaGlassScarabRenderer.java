package ddraig.net.entropica.client.renderer.silica_glass_scarab;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.silica_glass_scarab.SilicaGlassScarabEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class SilicaGlassScarabRenderer extends MobRenderer<SilicaGlassScarabEntity, SilicaGlassScarabRenderState, SilicaGlassScarabModel> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/silica_glass_scarab/silica_glass_scarab.png"
    );

    public SilicaGlassScarabRenderer(EntityRendererProvider.Context context) {
        super(context, new SilicaGlassScarabModel(context.bakeLayer(SilicaGlassScarabModel.LAYER_LOCATION)), 0.45F);
        this.addLayer(new SilicaGlassScarabGlowLayer(this));
    }

    @Override
    public SilicaGlassScarabRenderState createRenderState() {
        return new SilicaGlassScarabRenderState();
    }

    @Override
    public void extractRenderState(SilicaGlassScarabEntity entity, SilicaGlassScarabRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.solarFlashTicks = entity.getSolarFlashTicks();
    }

    @Override
    public ResourceLocation getTextureLocation(SilicaGlassScarabRenderState state) {
        return TEXTURE;
    }

    @Override
    protected @Nullable RenderType getRenderType(SilicaGlassScarabRenderState state, boolean bodyVisible, boolean translucent, boolean glowing) {
        if (!bodyVisible && !translucent) {
            return glowing ? RenderType.outline(getTextureLocation(state)) : null;
        }
        return RenderType.entityTranslucent(getTextureLocation(state));
    }

    @Override
    protected int getBlockLightLevel(SilicaGlassScarabEntity entity, BlockPos pos) {
        int minLight = entity.getSolarFlashTicks() > 0 ? 15 : 8;
        return Math.max(minLight, super.getBlockLightLevel(entity, pos));
    }
}
