package ddraig.net.entropica.client.renderer.storm_kite;

import ddraig.net.entropica.Entropica;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public class StormKiteGlowLayer extends EyesLayer<StormKiteRenderState, StormKiteModel> {
    private static final RenderType GLOW = RenderType.eyes(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/storm_kite/storm_kite_glow.png"));

    public StormKiteGlowLayer(RenderLayerParent<StormKiteRenderState, StormKiteModel> parent) {
        super(parent);
    }

    @Override
    public RenderType renderType() {
        return GLOW;
    }
}
