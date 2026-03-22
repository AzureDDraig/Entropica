package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.entity.EidolicShadowEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix4f;

public class EidolicShadowRenderer extends EntityRenderer<EidolicShadowEntity, EidolicShadowRenderer.ShadowRenderState> {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private final Font font;
    private final ItemModelResolver itemModelResolver;

    public EidolicShadowRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.font = context.getFont();
        this.itemModelResolver = context.getItemModelResolver();
    }

    public static class ShadowRenderState extends EntityRenderState {
        public final ItemStackRenderState itemRenderState = new ItemStackRenderState();
        public boolean hasItem = false;
        public String[] runes = new String[4];
        public float yRot = 0;
        public float bobbing = 0;
        public long time = 0;
    }

    @Override
    public ShadowRenderState createRenderState() {
        return new ShadowRenderState();
    }

    @Override
    public void extractRenderState(EidolicShadowEntity entity, ShadowRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);

        state.hasItem = !entity.getEntityData().get(EidolicShadowEntity.CARRIED_ITEM).isEmpty();
        if (state.hasItem) {
            this.itemModelResolver.updateForTopItem(state.itemRenderState, entity.getEntityData().get(EidolicShadowEntity.CARRIED_ITEM), ItemDisplayContext.GROUND, entity.level(), entity, 0);
        }

        state.runes[0] = entity.getEntityData().get(EidolicShadowEntity.RUNE_1);
        state.runes[1] = entity.getEntityData().get(EidolicShadowEntity.RUNE_2);
        state.runes[2] = entity.getEntityData().get(EidolicShadowEntity.RUNE_3);
        state.runes[3] = entity.getEntityData().get(EidolicShadowEntity.RUNE_4);

        state.yRot = entity.getViewYRot(partialTick);
        state.bobbing = (float) Math.sin((entity.tickCount + partialTick) / 10.0f) * 0.1f;
        state.time = System.currentTimeMillis();
    }

    @Override
    public void submit(ShadowRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        int light = 15728880;

        // Spectral Body Colors (Dark Grey, Transparent)
        int bR = 30, bG = 30, bB = 30, bA = 180;

        // Gauntlet Colors (Light Grey, Solid)
        int gR = 140, gG = 140, gB = 140, gA = 255;
        // Trim Gauntlet Colors (Brighter Grey, Solid)
        int tR = 190, tG = 190, tB = 190;

        poseStack.pushPose();

        // Base entity rotation and floating bob
        poseStack.mulPose(Axis.YP.rotationDegrees(180f - state.yRot));
        poseStack.translate(0, state.bobbing + 0.4f, 0);

        float timeF = state.time / 50.0f;

        // ==========================================
        // 1. TORSO (Translucent)
        // ==========================================
        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucent(WHITE_TEXTURE), (pose, cons) -> {
            drawCube(cons, pose.pose(), 0.5f, 0.75f, 0.25f, 0, 1.125f, 0, bR, bG, bB, bA, light);
        });

        // ==========================================
        // 2. HEAD & RUNES (Translucent + Font)
        // ==========================================
        poseStack.pushPose();
        poseStack.translate(0, 1.5f, 0); // Neck Pivot
        poseStack.mulPose(Axis.XP.rotationDegrees((float) Math.sin(timeF * 0.05f) * 5.0f));
        poseStack.mulPose(Axis.YP.rotationDegrees((float) Math.cos(timeF * 0.03f) * 5.0f));

        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucent(WHITE_TEXTURE), (pose, cons) -> {
            drawCube(cons, pose.pose(), 0.5f, 0.5f, 0.5f, 0, 0.25f, 0, bR, bG, bB, bA, light);
        });

        float textScale = 0.015f;
        poseStack.translate(0, 0.25f, 0); // Center of Head
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        for (int face = 0; face < 4; face++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(face * 90f));
            poseStack.translate(0, 0, 0.251f);
            poseStack.scale(textScale, -textScale, textScale);

            drawRuneText(this.font, poseStack, bufferSource, state.runes[0], -6, -6, light);
            drawRuneText(this.font, poseStack, bufferSource, state.runes[1], 6, -6, light);
            drawRuneText(this.font, poseStack, bufferSource, state.runes[2], -6, 6, light);
            drawRuneText(this.font, poseStack, bufferSource, state.runes[3], 6, 6, light);

            poseStack.popPose();
        }
        bufferSource.endBatch(); // Flush runes instantly
        poseStack.popPose(); // Pop Head

        // ==========================================
        // 3. LEGS (Translucent)
        // ==========================================
        float legSwing = (float) Math.sin(timeF * 0.1f) * 10.0f;

        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucent(WHITE_TEXTURE), (pose, cons) -> {
            Matrix4f basePose = pose.pose();
            PoseStack localStack = new PoseStack();

            // Right Leg
            localStack.pushPose();
            localStack.translate(-0.125f, 0.75f, 0);
            localStack.mulPose(Axis.XP.rotationDegrees(legSwing + 5.0f));
            drawCube(cons, new Matrix4f(basePose).mul(localStack.last().pose()), 0.25f, 0.75f, 0.25f, 0, -0.375f, 0, bR, bG, bB, bA, light);
            localStack.popPose();

            // Left Leg
            localStack.pushPose();
            localStack.translate(0.125f, 0.75f, 0);
            localStack.mulPose(Axis.XP.rotationDegrees(-legSwing + 5.0f));
            drawCube(cons, new Matrix4f(basePose).mul(localStack.last().pose()), 0.25f, 0.75f, 0.25f, 0, -0.375f, 0, bR, bG, bB, bA, light);
            localStack.popPose();
        });

        // ==========================================
        // 4. HIGHLY DETAILED KINEMATIC ARMS
        // ==========================================
        // Translucent Pass (Upper Arms)
        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucent(WHITE_TEXTURE), (pose, cons) -> {
            Matrix4f basePose = pose.pose();
            renderUpperArm(basePose, cons, true, state.hasItem, timeF, light, bR, bG, bB, bA);
            renderUpperArm(basePose, cons, false, state.hasItem, timeF, light, bR, bG, bB, bA);
        });

        // Solid Pass (Gauntlets & Fingers)
        collector.submitCustomGeometry(poseStack, RenderType.entitySolid(WHITE_TEXTURE), (pose, cons) -> {
            Matrix4f basePose = pose.pose();
            renderGauntlet(basePose, cons, true, state.hasItem, timeF, light, gR, gG, gB, gA, tR, tG, tB);
            renderGauntlet(basePose, cons, false, state.hasItem, timeF, light, gR, gG, gB, gA, tR, tG, tB);
        });

        // ==========================================
        // 5. CARRIED ACTUAL ITEM
        // ==========================================
        if (state.hasItem) {
            poseStack.pushPose();
            poseStack.translate(0, 1.35f, 0.65f);
            poseStack.scale(0.6f, 0.6f, 0.6f);

            float spin = state.time % 360000L / 10.0f;
            poseStack.mulPose(Axis.YP.rotationDegrees(spin));

            // Synchronous submission here is safe as it immediately snapshots the fully translated poseStack
            state.itemRenderState.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        poseStack.popPose();

        super.submit(state, poseStack, collector, cameraState);
    }

    private void renderUpperArm(Matrix4f basePose, VertexConsumer cons, boolean isRight, boolean hasItem, float timeF, int light, int bR, int bG, int bB, int bA) {
        PoseStack localStack = new PoseStack();
        float sign = isRight ? -1.0f : 1.0f;
        float armPitch = hasItem ? -55.0f : (float) Math.sin(timeF * 0.1f) * 15.0f * sign;
        float armYaw = hasItem ? 25.0f * sign : 0.0f;

        localStack.translate(sign * 0.375f, 1.375f, 0); // Shoulder Pivot
        localStack.mulPose(Axis.YP.rotationDegrees(armYaw));
        localStack.mulPose(Axis.XP.rotationDegrees(armPitch));

        drawCube(cons, new Matrix4f(basePose).mul(localStack.last().pose()), 0.25f, 0.35f, 0.25f, 0, -0.175f, 0, bR, bG, bB, bA, light);
    }

    private void renderGauntlet(Matrix4f basePose, VertexConsumer cons, boolean isRight, boolean hasItem, float timeF, int light, int gR, int gG, int gB, int gA, int tR, int tG, int tB) {
        PoseStack localStack = new PoseStack();
        float sign = isRight ? -1.0f : 1.0f;
        float armPitch = hasItem ? -55.0f : (float) Math.sin(timeF * 0.1f) * 15.0f * sign;
        float armYaw = hasItem ? 25.0f * sign : 0.0f;

        localStack.translate(sign * 0.375f, 1.375f, 0); // Shoulder Pivot
        localStack.mulPose(Axis.YP.rotationDegrees(armYaw));
        localStack.mulPose(Axis.XP.rotationDegrees(armPitch));

        // Elbow Pivot
        localStack.translate(0, -0.35f, 0);
        float elbowPitch = hasItem ? -65.0f : -5.0f + (float) Math.sin(timeF * 0.15f) * 5.0f;
        localStack.mulPose(Axis.XP.rotationDegrees(elbowPitch));

        drawCube(cons, new Matrix4f(basePose).mul(localStack.last().pose()), 0.26f, 0.35f, 0.26f, 0, -0.175f, 0, gR, gG, gB, gA, light); // Base
        drawCube(cons, new Matrix4f(basePose).mul(localStack.last().pose()), 0.28f, 0.15f, 0.28f, 0, -0.075f, 0, tR, tG, tB, gA, light); // Flare
        drawCube(cons, new Matrix4f(basePose).mul(localStack.last().pose()), 0.1f, 0.1f, 0.1f, 0, 0.05f, 0.15f, tR, tG, tB, gA, light); // Elbow Guard

        // Wrist Pivot
        localStack.translate(0, -0.35f, 0);
        float wristYaw = hasItem ? -30.0f * sign : 0.0f;
        float wristRoll = hasItem ? 45.0f * sign : 0.0f;
        localStack.mulPose(Axis.YP.rotationDegrees(wristYaw));
        localStack.mulPose(Axis.ZP.rotationDegrees(wristRoll));

        drawCube(cons, new Matrix4f(basePose).mul(localStack.last().pose()), 0.22f, 0.05f, 0.22f, 0, -0.025f, 0, tR, tG, tB, gA, light); // Band
        drawCube(cons, new Matrix4f(basePose).mul(localStack.last().pose()), 0.20f, 0.12f, 0.08f, 0, -0.08f, 0, gR, gG, gB, gA, light); // Palm
        drawCube(cons, new Matrix4f(basePose).mul(localStack.last().pose()), 0.21f, 0.04f, 0.04f, 0, -0.13f, 0.04f, tR, tG, tB, gA, light); // Knuckle

        // 4 Individual Fingers
        for (int f = 0; f < 4; f++) {
            localStack.pushPose();
            float fingerX = (f - 1.5f) * 0.05f;
            localStack.translate(fingerX, -0.14f, 0);
            float curl = hasItem ? -70.0f : -10.0f + (float)Math.sin(timeF * 0.2f + f) * 10f;
            localStack.mulPose(Axis.XP.rotationDegrees(curl));

            drawCube(cons, new Matrix4f(basePose).mul(localStack.last().pose()), 0.04f, 0.08f, 0.04f, 0, -0.04f, 0, tR, tG, tB, gA, light); // Proximal

            localStack.translate(0, -0.08f, 0);
            localStack.mulPose(Axis.XP.rotationDegrees(curl * 0.8f));
            drawCube(cons, new Matrix4f(basePose).mul(localStack.last().pose()), 0.035f, 0.08f, 0.035f, 0, -0.04f, 0, gR, gG, gB, gA, light); // Distal

            localStack.popPose();
        }

        // Opposable Thumb
        localStack.pushPose();
        localStack.translate(sign * 0.1f, -0.06f, 0);
        float thumbCurl = hasItem ? -50.0f : -5.0f;
        localStack.mulPose(Axis.ZP.rotationDegrees(sign * 40f));
        localStack.mulPose(Axis.YP.rotationDegrees(sign * -30f));
        localStack.mulPose(Axis.XP.rotationDegrees(thumbCurl));

        drawCube(cons, new Matrix4f(basePose).mul(localStack.last().pose()), 0.04f, 0.06f, 0.04f, 0, -0.03f, 0, tR, tG, tB, gA, light);

        localStack.translate(0, -0.06f, 0);
        localStack.mulPose(Axis.XP.rotationDegrees(thumbCurl * 0.5f));
        drawCube(cons, new Matrix4f(basePose).mul(localStack.last().pose()), 0.035f, 0.06f, 0.035f, 0, -0.03f, 0, gR, gG, gB, gA, light);

        localStack.popPose();
    }

    private void drawRuneText(Font font, PoseStack poseStack, MultiBufferSource bufferSource, String rune, float x, float y, int light) {
        if (rune == null || rune.isEmpty()) return;
        float width = font.width(rune);
        font.drawInBatch(rune, x - (width / 2f), y - (font.lineHeight / 2f), 0x00FFFF, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, light);
    }

    private void drawCube(VertexConsumer consumer, Matrix4f pose, float width, float height, float depth, float offsetX, float offsetY, float offsetZ, int r, int g, int b, int a, int light) {
        float hw = width / 2f;
        float hd = depth / 2f;
        float x0 = offsetX - hw, x1 = offsetX + hw;
        float y0 = offsetY - height/2f, y1 = offsetY + height/2f;
        float z0 = offsetZ - hd, z1 = offsetZ + hd;

        drawQuad(consumer, pose, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, r, g, b, a, light, 0, 0, 1);
        drawQuad(consumer, pose, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0, r, g, b, a, light, 0, 0, -1);
        drawQuad(consumer, pose, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0, r, g, b, a, light, 0, 1, 0);
        drawQuad(consumer, pose, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, r, g, b, a, light, 0, -1, 0);
        drawQuad(consumer, pose, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, r, g, b, a, light, -1, 0, 0);
        drawQuad(consumer, pose, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1, r, g, b, a, light, 1, 0, 0);
    }

    private void drawQuad(VertexConsumer consumer, Matrix4f pose, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, int r, int g, int b, int a, int light, float nx, float ny, float nz) {
        consumer.addVertex(pose, x0, y0, z0).setColor(r, g, b, a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(nx, ny, nz);
        consumer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(nx, ny, nz);
        consumer.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(nx, ny, nz);
        consumer.addVertex(pose, x3, y3, z3).setColor(r, g, b, a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(nx, ny, nz);
    }
}