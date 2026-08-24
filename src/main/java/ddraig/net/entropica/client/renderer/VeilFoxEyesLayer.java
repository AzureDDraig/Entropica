package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.model.VeilFoxModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class VeilFoxEyesLayer extends EyesLayer<VeilFoxRenderState, VeilFoxModel> {

    // The texture that determines which pixels actually glow in the dark
    private static final RenderType EYES = RenderType.eyes(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/veil_fox/veil_fox_eyes.png"));

    public VeilFoxEyesLayer(RenderLayerParent<VeilFoxRenderState, VeilFoxModel> parent) {
        super(parent);
    }

    @Override
    public RenderType renderType() {
        return EYES;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector buffer, int packedLight, VeilFoxRenderState state, float yaw, float pitch) {
        // We inject the same strobe alpha math so the glowing eyes fade out seamlessly with the body
        int alpha = (int)(state.strobeAlpha * 255.0f);
        int color = 0x00FFFFFF | (alpha << 24);

        // Use the native 1.21.10 SubmitNodeCollector syntax, replacing the default -1 color with our strobe color!
        buffer.order(1).submitModel(
                this.getParentModel(),
                state,
                poseStack,
                this.renderType(),
                packedLight,
                OverlayTexture.NO_OVERLAY,
                color,
                null,
                state.outlineColor,
                null
        );
    }
}