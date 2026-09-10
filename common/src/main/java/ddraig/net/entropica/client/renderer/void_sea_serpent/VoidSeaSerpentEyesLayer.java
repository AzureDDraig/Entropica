package ddraig.net.entropica.client.renderer.void_sea_serpent;

import ddraig.net.entropica.Entropica;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public class VoidSeaSerpentEyesLayer extends EyesLayer<VoidSeaSerpentRenderState, VoidSeaSerpentModel> {
    private static final RenderType EYES = RenderType.eyes(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/void_sea_serpent/void_sea_serpent_eyes.png"));

    public VoidSeaSerpentEyesLayer(RenderLayerParent<VoidSeaSerpentRenderState, VoidSeaSerpentModel> parent) {
        super(parent);
    }

    @Override
    public RenderType renderType() {
        return EYES;
    }
}
