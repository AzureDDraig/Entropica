package ddraig.net.entropica.client.renderer.mycelial_spore_weaver;

import ddraig.net.entropica.Entropica;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public class MycelialSporeWeaverGlowLayer extends EyesLayer<MycelialSporeWeaverRenderState, MycelialSporeWeaverModel> {

    private static final RenderType GLOW = RenderType.eyes(
            ResourceLocation.fromNamespaceAndPath(
                    Entropica.MODID,
                    "textures/entity/mycelial_spore_weaver/mycelial_spore_weaver_glow.png"
            )
    );

    public MycelialSporeWeaverGlowLayer(RenderLayerParent<MycelialSporeWeaverRenderState, MycelialSporeWeaverModel> parent) {
        super(parent);
    }

    @Override
    public RenderType renderType() {
        return GLOW;
    }
}
