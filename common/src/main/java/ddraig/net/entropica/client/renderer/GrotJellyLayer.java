package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.model.GrotModel;
import ddraig.net.entropica.client.model.ModModelLayers;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class GrotJellyLayer extends RenderLayer<GrotRenderState, GrotModel> {

    private static final ResourceLocation GROT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/grot/grot.png");
    private final GrotModel model;

    public GrotJellyLayer(RenderLayerParent<GrotRenderState, GrotModel> parent, EntityModelSet modelSet) {
        super(parent);
        this.model = new GrotModel(modelSet.bakeLayer(ModModelLayers.GROT));
    }

    private int blendColor(int baseColor, int targetColor, float ratio) {
        int r1 = (baseColor >> 16) & 0xFF;
        int g1 = (baseColor >> 8) & 0xFF;
        int b1 = baseColor & 0xFF;

        int r2 = (targetColor >> 16) & 0xFF;
        int g2 = (targetColor >> 8) & 0xFF;
        int b2 = targetColor & 0xFF;

        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);

        return (r << 16) | (g << 8) | b;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector buffer, int packedLight, GrotRenderState state, float yaw, float pitch) {
        this.model.setupAnim(state);

        int color = state.essenceType != null ? state.essenceType.getColorInt() : 0xFFFFFF;

        if (state.healTime > 0) {
            float ratio = state.healTime / 10.0f;
            color = blendColor(color, 0x00FF00, ratio);
        }

        int packedOverlay = OverlayTexture.pack(OverlayTexture.u(0.0F), OverlayTexture.v(state.hurtTime > 0 || state.grotDeathTime > 0));

        // ==========================================
        // 1. RENDER THE CORE + EYES + MOUTH (Opaque, Tinted, Glowing)
        // ==========================================
        int coreArgb = (255 << 24) | (color & 0xFFFFFF);
        int glowingLight = 15728880;

        buffer.submitCustomGeometry(poseStack, RenderType.entityCutoutNoCull(GROT_TEXTURE), (pose, vertexConsumer) -> {
            PoseStack safeStack = new PoseStack();
            safeStack.last().pose().set(pose.pose());
            safeStack.last().normal().set(pose.normal());

            this.model.getInnerBody().visible = true;
            this.model.getOuterBody().visible = false;
            this.model.renderToBuffer(safeStack, vertexConsumer, glowingLight, packedOverlay, coreArgb);
        });

        // ==========================================
        // 2. RENDER THE JELLY (Translucent, Melting Top-Down, Normal Light)
        // ==========================================
        poseStack.pushPose();

        if (state.grotDeathTime > 0.0f) {
            float meltScale = Math.max(0.0f, 1.0f - (state.grotDeathTime / 20.0f));

            // FIX: Translate the pose pivot directly to the floor (Y=1.5), scale it, then move back!
            // This forces the geometry to "puddle" downwards instead of shrinking upwards into the air.
            poseStack.translate(0.0f, 1.5f, 0.0f);
            poseStack.scale(1.0f, meltScale, 1.0f);
            poseStack.translate(0.0f, -1.5f, 0.0f);
        }

        // FIX: Force the alpha channel to 160. This guarantees you will always see the core/face,
        // even if your image editor accidentally stripped the transparency out of the .png file!
        int jellyArgb = (160 << 24) | (color & 0xFFFFFF);

        buffer.submitCustomGeometry(poseStack, RenderType.entityTranslucent(GROT_TEXTURE), (pose, vertexConsumer) -> {
            PoseStack safeStack = new PoseStack();
            safeStack.last().pose().set(pose.pose());
            safeStack.last().normal().set(pose.normal());

            this.model.getInnerBody().visible = false;
            this.model.getOuterBody().visible = true;
            this.model.renderToBuffer(safeStack, vertexConsumer, packedLight, packedOverlay, jellyArgb);
        });

        poseStack.popPose();
    }
}