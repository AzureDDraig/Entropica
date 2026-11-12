package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.EssenceNodeEntity;
import ddraig.net.entropica.item.AethericVisionItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public class EssenceNodeRenderer extends EntityRenderer<EssenceNodeEntity, EssenceNodeRenderer.EssenceNodeRenderState> {

    // The 4 Layers of the Rift
    private static final ResourceLocation RIFT_BG = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/essence_rift_bg.png");
    private static final ResourceLocation RIFT_MAIN = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/essence_rift_main.png");
    private static final ResourceLocation RIFT_RING = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/essence_rift_ring.png");
    private static final ResourceLocation RIFT_CORE = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/essence_rift_core.png");

    public EssenceNodeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EssenceNodeRenderState createRenderState() {
        return new EssenceNodeRenderState();
    }

    @Override
    public void extractRenderState(EssenceNodeEntity entity, EssenceNodeRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.essenceType = entity.getEssenceType();
        state.scale = entity.getScale();
        state.time = entity.tickCount + partialTick;
        state.randomOffset = entity.getId() * 13.37f;
    }

    @Override
    public void submit(EssenceNodeRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        // ==========================================
        // AETHERIC VISION CHECK
        // ==========================================
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        if (!ddraig.net.entropica.item.ArkanistMonocleItem.isAethericVisionActiveCommon(player)) {
            return; // Invisible if not wearing the Monocle/Goggles!
        }

        // ==========================================
        // RENDER LOGIC
        // ==========================================
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.5F, 0.0F); // Center the rift slightly above its hitbox root

        // Billboarding: Ensure the 2D rift textures perfectly face the player's camera
        poseStack.mulPose(cameraState.orientation);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        // Apply dynamic scale (shrinks as the node runs out of essence or fizzles)
        float baseScale = 1.5f * state.scale;
        poseStack.scale(baseScale, baseScale, baseScale);

        // Get elemental colors
        EssenceType type = state.essenceType;
        float rRaw, gRaw, bRaw;

        if (type.isDynamic()) {
            float speed = 0.05f;
            float cycle = (state.time + state.randomOffset) * speed;
            rRaw = (float) (Math.sin(cycle) * 0.5f + 0.5f);
            gRaw = (float) (Math.sin(cycle + 2.094f) * 0.5f + 0.5f);
            bRaw = (float) (Math.sin(cycle + 4.188f) * 0.5f + 0.5f);
        } else {
            int[] rgb = type.getCurrentRGB(System.currentTimeMillis() / 50);
            rRaw = rgb[0] / 255.0F;
            gRaw = rgb[1] / 255.0F;
            bRaw = rgb[2] / 255.0F;
        }

        // Normalize colors to keep them bright and emissive
        float maxChannel = Math.max(rRaw, Math.max(gRaw, bRaw));
        final float r = (maxChannel > 0) ? rRaw / maxChannel : 0;
        final float g = (maxChannel > 0) ? gRaw / maxChannel : 0;
        final float b = (maxChannel > 0) ? bRaw / maxChannel : 0;

        float time = state.time + state.randomOffset;

        // --- LAYER 1: Background Nebula (Slow clockwise, unchanged) ---
        poseStack.pushPose();
        poseStack.scale(1.3f, 1.3f, 1.3f);
        poseStack.mulPose(Axis.ZP.rotationDegrees(time * 0.5f));
        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(RIFT_BG), (pose, consumer) -> {
            drawQuad(consumer, pose.pose(), r, g, b, 0.5f);
        });
        poseStack.popPose();

        // --- LAYER 2: Main Rift Tear (Creeping counter-clockwise, slowest) ---
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.0f, -0.01f); // Shift slightly forward to prevent Z-fighting
        poseStack.scale(1.0f, 1.0f, 1.0f);
        poseStack.mulPose(Axis.ZP.rotationDegrees(-time * 0.1f)); // Extremely slow
        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(RIFT_MAIN), (pose, consumer) -> {
            drawQuad(consumer, pose.pose(), r, g, b, 0.8f);
        });
        poseStack.popPose();

        // --- LAYER 3: Energy Ring (Slow crawl clockwise, very slow pulse) ---
        float ringPulse = (float) (Math.sin(time * 0.05f) * 0.1f + 0.85f); // Pulse barely moves
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.0f, -0.02f);
        poseStack.scale(ringPulse, ringPulse, ringPulse);
        poseStack.mulPose(Axis.ZP.rotationDegrees(time * 0.3f)); // Crawling rotation
        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(RIFT_RING), (pose, consumer) -> {
            drawQuad(consumer, pose.pose(), r, g, b, 0.9f);
        });
        poseStack.popPose();

        // --- LAYER 4: Inner Core (Slow crawl counter-clockwise, very slow bright pulse) ---
        float corePulse = (float) (Math.sin(time * 0.03f) * 0.15f + 0.55f); // Extremely slow pulse
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.0f, -0.03f);
        poseStack.scale(corePulse, corePulse, corePulse);
        poseStack.mulPose(Axis.ZP.rotationDegrees(-time * 0.2f)); // Crawling rotation
        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(RIFT_CORE), (pose, consumer) -> {
            drawQuad(consumer, pose.pose(), r, g, b, 1.0f);
        });
        poseStack.popPose();

        poseStack.popPose();
        super.submit(state, poseStack, collector, cameraState);
    }

    private void drawQuad(VertexConsumer consumer, Matrix4f matrix, float r, float g, float b, float alpha) {
        consumer.addVertex(matrix, -0.5F, -0.5F, 0.0F).setColor(r, g, b, alpha).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(255).setNormal(0.0F, 1.0F, 0.0F);
        consumer.addVertex(matrix, 0.5F, -0.5F, 0.0F).setColor(r, g, b, alpha).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(255).setNormal(0.0F, 1.0F, 0.0F);
        consumer.addVertex(matrix, 0.5F, 0.5F, 0.0F).setColor(r, g, b, alpha).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(255).setNormal(0.0F, 1.0F, 0.0F);
        consumer.addVertex(matrix, -0.5F, 0.5F, 0.0F).setColor(r, g, b, alpha).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(255).setNormal(0.0F, 1.0F, 0.0F);
    }

    public static class EssenceNodeRenderState extends EntityRenderState {
        public EssenceType essenceType;
        public float scale;
        public float time;
        public float randomOffset;
    }
}