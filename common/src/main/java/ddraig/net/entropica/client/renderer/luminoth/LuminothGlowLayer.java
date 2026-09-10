package ddraig.net.entropica.client.renderer.luminoth;

import ddraig.net.entropica.Entropica;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public class LuminothGlowLayer extends EyesLayer<LuminothRenderState, LuminothModel> {
    private static final RenderType GLOW = RenderType.eyes(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/luminoth/luminoth_wings_glow.png"));

    public LuminothGlowLayer(RenderLayerParent<LuminothRenderState, LuminothModel> parent) {
        super(parent);
    }

    @Override
    public RenderType renderType() {
        return GLOW;
    }
}
