package ddraig.net.entropica.client.renderer.starlight_nautilus;

import com.mojang.blaze3d.vertex.PoseStack;
import ddraig.net.entropica.Entropica;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class StarlightNautilusSaddleLayer extends RenderLayer<StarlightNautilusRenderState, StarlightNautilusModel> {

    private static final ResourceLocation SADDLE_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/starlight_nautilus/starlight_nautilus_saddle.png"
    );

    private final StarlightNautilusModel saddleModel;

    public StarlightNautilusSaddleLayer(
            RenderLayerParent<StarlightNautilusRenderState, StarlightNautilusModel> renderer,
            EntityModelSet modelSet
    ) {
        super(renderer);
        this.saddleModel = new StarlightNautilusModel(modelSet.bakeLayer(StarlightNautilusModel.SADDLE_LAYER_LOCATION));
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
        if (!state.isSaddled || state.isBaby) {
            return;
        }
        coloredCutoutModelCopyLayerRender(
                this.saddleModel,
                SADDLE_TEXTURE,
                poseStack,
                collector,
                packedLight,
                state,
                -1,
                1
        );
    }
}
