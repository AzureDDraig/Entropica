package ddraig.net.entropica.client.renderer.starlight_nautilus;

import com.mojang.blaze3d.vertex.PoseStack;
import ddraig.net.entropica.entity.starlight_nautilus.StarlightNautilusArmorType;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class StarlightNautilusArmorLayer extends RenderLayer<StarlightNautilusRenderState, StarlightNautilusModel> {

    private final StarlightNautilusModel armorModel;

    public StarlightNautilusArmorLayer(
            RenderLayerParent<StarlightNautilusRenderState, StarlightNautilusModel> renderer,
            EntityModelSet modelSet
    ) {
        super(renderer);
        this.armorModel = new StarlightNautilusModel(modelSet.bakeLayer(StarlightNautilusModel.ARMOR_LAYER_LOCATION));
    }

    @Override
    public void submit(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight,
            StarlightNautilusRenderState state,
            float yRot,
            float xRot
    ) {
        if (state.isBaby || state.armorType == null || state.armorType == StarlightNautilusArmorType.NONE) {
            return;
        }
        ResourceLocation texture = state.armorType.getTextureLocation();
        if (texture == null) {
            return;
        }
        coloredCutoutModelCopyLayerRender(
                this.armorModel,
                texture,
                poseStack,
                collector,
                packedLight,
                state,
                -1,
                2
        );
    }
}
