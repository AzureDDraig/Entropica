package ddraig.net.entropica.client.renderer.silica_glass_scarab;

import ddraig.net.entropica.Entropica;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public class SilicaGlassScarabGlowLayer extends EyesLayer<SilicaGlassScarabRenderState, SilicaGlassScarabModel> {

    private static final RenderType GLOW = RenderType.eyes(
            ResourceLocation.fromNamespaceAndPath(
                    Entropica.MODID,
                    "textures/entity/silica_glass_scarab/silica_glass_scarab_glow.png"
            )
    );

    public SilicaGlassScarabGlowLayer(RenderLayerParent<SilicaGlassScarabRenderState, SilicaGlassScarabModel> parent) {
        super(parent);
    }

    @Override
    public RenderType renderType() {
        return GLOW;
    }
}
