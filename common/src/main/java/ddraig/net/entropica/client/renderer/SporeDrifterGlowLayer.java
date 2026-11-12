package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public class SporeDrifterGlowLayer extends EyesLayer<SporeDrifterRenderState, SporeDrifterModel> {
    private static final RenderType GLOW = RenderType.eyes(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/spore_drifter_glow.png"));

    public SporeDrifterGlowLayer(RenderLayerParent<SporeDrifterRenderState, SporeDrifterModel> parent) {
        super(parent);
    }

    @Override
    public RenderType renderType() {
        return GLOW;
    }
}
