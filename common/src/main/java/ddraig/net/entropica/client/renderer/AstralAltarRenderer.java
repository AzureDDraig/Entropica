package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.block.entity.AstralAltarCoreBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class AstralAltarRenderer implements BlockEntityRenderer<AstralAltarCoreBlockEntity, AstralAltarRenderer.AltarRenderState> {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private final ItemModelResolver itemModelResolver;

    public AstralAltarRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    public AABB getRenderBoundingBox(AstralAltarCoreBlockEntity blockEntity) {
        return new AABB(-30000000.0, -30000000.0, -30000000.0, 30000000.0, 30000000.0, 30000000.0);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRender(AstralAltarCoreBlockEntity blockEntity, Vec3 cameraPos) {
        return true;
    }

    @Override
    public AltarRenderState createRenderState() {
        return new AltarRenderState();
    }

    @Override
    public void extractRenderState(AstralAltarCoreBlockEntity be, AltarRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.isStructureValid = be.isStructureValid();
        state.isStarlightActive = be.isStarlightActive();
        state.structureTier = be.getStructureTier();
        state.incomingBeamCount = be.getIncomingBeamCount();
        state.rotation = (be.clientRotation + partialTick * 0.02f) * 57.2958f; // rad to deg

        state.hasHeldItem = !be.getHeldItem().isEmpty();
        if (state.hasHeldItem && be.getLevel() != null) {
            int seed = be.getBlockPos().hashCode();
            this.itemModelResolver.updateForTopItem(state.heldItemState, be.getHeldItem(), ItemDisplayContext.GROUND, be.getLevel(), null, seed);
        } else {
            state.heldItemState.clear();
        }

        state.hasStarChart = !be.getStarChart().isEmpty();
        if (state.hasStarChart && be.getLevel() != null) {
            int seed = be.getBlockPos().hashCode() + 1;
            this.itemModelResolver.updateForTopItem(state.starChartState, be.getStarChart(), ItemDisplayContext.GROUND, be.getLevel(), null, seed);
        } else {
            state.starChartState.clear();
        }
    }

    @Override
    public void submit(AltarRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        int light = 15728880;
        int overlay = OverlayTexture.NO_OVERLAY;
        double gameTime = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0;
        double bob = Math.sin(gameTime * 0.05) * 0.05;

        // 1. Runic / Starlight Circle & Focal Beams on Altar Core Surface when formed
        // 1. Runic / Starlight Circle & Focal Beams on Altar Core Surface when formed
        if (state.isStructureValid) {
            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, consumer) -> {
                float r = state.isStarlightActive ? 0.35f : 0.2f;
                float g = state.isStarlightActive ? 0.85f : 0.5f;
                float b = state.isStarlightActive ? 1.0f : 0.7f;
                float a = state.isStarlightActive ? 0.90f : 0.45f;
                float radius = state.structureTier == 3 ? 0.85f : 0.52f;
                float discY = (float) (1.25 + bob);
                renderCircleDisc(pose.pose(), consumer, 0.5f, discY, 0.5f, radius, r, g, b, a, state.rotation, light, overlay);

                if (state.isStarlightActive) {
                    if (state.structureTier == 3) {
                        // Master Astral Altar: 4 Corner Lenses routing into Overhead Central Apex
                        renderBeamLine(pose.pose(), consumer, 5.5f, 5.5f, 5.5f, 0.5f, 5.0f, 0.5f, 0.85f, 0.95f, 1.0f, 0.75f, light, overlay);
                        renderBeamLine(pose.pose(), consumer, 5.5f, 5.5f, -4.5f, 0.5f, 5.0f, 0.5f, 0.85f, 0.95f, 1.0f, 0.75f, light, overlay);
                        renderBeamLine(pose.pose(), consumer, -4.5f, 5.5f, 5.5f, 0.5f, 5.0f, 0.5f, 0.85f, 0.95f, 1.0f, 0.75f, light, overlay);
                        renderBeamLine(pose.pose(), consumer, -4.5f, 5.5f, -4.5f, 0.5f, 5.0f, 0.5f, 0.85f, 0.95f, 1.0f, 0.75f, light, overlay);

                        // Multi-Beam Spectral Refraction Cascade (Overhead Apex -> Altar Core & Pedestal circle)
                        renderBeamLine(pose.pose(), consumer, 0.5f, 5.0f, 0.5f, 0.5f, discY, 0.5f, 1.0f, 1.0f, 1.0f, 0.90f, light, overlay); // Center White-Blue
                        renderBeamLine(pose.pose(), consumer, 0.5f, 5.0f, 0.5f, 2.5f, 1.15f, 0.5f, 0.9f, 0.1f, 0.2f, 0.80f, light, overlay); // Red
                        renderBeamLine(pose.pose(), consumer, 0.5f, 5.0f, 0.5f, -1.5f, 1.15f, 0.5f, 0.1f, 0.9f, 0.9f, 0.80f, light, overlay); // Cyan
                        renderBeamLine(pose.pose(), consumer, 0.5f, 5.0f, 0.5f, 0.5f, 1.15f, 2.5f, 0.9f, 0.1f, 0.8f, 0.80f, light, overlay); // Magenta
                        renderBeamLine(pose.pose(), consumer, 0.5f, 5.0f, 0.5f, 0.5f, 1.15f, -1.5f, 0.2f, 0.9f, 0.3f, 0.80f, light, overlay); // Green
                        renderBeamLine(pose.pose(), consumer, 0.5f, 5.0f, 0.5f, 1.9f, 1.15f, 1.9f, 1.0f, 0.85f, 0.2f, 0.80f, light, overlay); // Gold
                        renderBeamLine(pose.pose(), consumer, 0.5f, 5.0f, 0.5f, -0.9f, 1.15f, -0.9f, 0.3f, 0.4f, 1.0f, 0.80f, light, overlay); // Deep Blue
                    } else {
                        // Standard Tier 2 Altar: Overhead Starlight Focal Ray (From Focal Lens Mount at Y=+5 down to Altar Core)
                        renderBeamLine(pose.pose(), consumer, 0.5f, 5.0f, 0.5f, 0.5f, discY, 0.5f, 0.8f, 0.95f, 1.0f, 0.85f, light, overlay);
                    }
                }
            });
        }

        // 2. Render Floating Held Item
        if (state.hasHeldItem) {
            poseStack.pushPose();
            poseStack.translate(0.5, 1.32 + bob, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.rotation));
            poseStack.scale(0.6f, 0.6f, 0.6f);
            state.heldItemState.submit(poseStack, collector, light, overlay, 0);
            poseStack.popPose();
        }

        // 3. Render Floating Star Chart
        if (state.hasStarChart) {
            poseStack.pushPose();
            poseStack.translate(0.5, (state.hasHeldItem ? 1.7 : 1.25) - bob, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(-state.rotation * 0.8f));
            poseStack.mulPose(Axis.XP.rotationDegrees(25.0f));
            poseStack.scale(0.5f, 0.5f, 0.5f);
            state.starChartState.submit(poseStack, collector, light, overlay, 0);
            poseStack.popPose();
        }
    }

    private static void renderCircleDisc(Matrix4f pose, VertexConsumer consumer, float cx, float cy, float cz, float radius, float r, float g, float b, float a, float rotDeg, int light, int overlay) {
        int segments = 24;
        float innerRadius = radius * 0.65f;
        double rotRad = Math.toRadians(rotDeg);

        // Outer rotating runic ring (rendered double-sided)
        for (int i = 0; i < segments; i++) {
            double a1 = (i * 2.0 * Math.PI / segments) + rotRad;
            double a2 = ((i + 1) * 2.0 * Math.PI / segments) + rotRad;

            float cos1 = (float) Math.cos(a1);
            float sin1 = (float) Math.sin(a1);
            float cos2 = (float) Math.cos(a2);
            float sin2 = (float) Math.sin(a2);

            float x1_out = cx + cos1 * radius;
            float z1_out = cz + sin1 * radius;
            float x2_out = cx + cos2 * radius;
            float z2_out = cz + sin2 * radius;

            float x1_in = cx + cos1 * innerRadius;
            float z1_in = cz + sin1 * innerRadius;
            float x2_in = cx + cos2 * innerRadius;
            float z2_in = cz + sin2 * innerRadius;

            // Top Face (visible looking down from above)
            consumer.addVertex(pose, x1_in, cy, z1_in).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
            consumer.addVertex(pose, x1_out, cy, z1_out).setColor(r, g, b, a).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
            consumer.addVertex(pose, x2_out, cy, z2_out).setColor(r, g, b, a).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
            consumer.addVertex(pose, x2_in, cy, z2_in).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);

            // Bottom Face
            consumer.addVertex(pose, x2_in, cy, z2_in).setColor(r, g, b, a * 0.7f).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
            consumer.addVertex(pose, x2_out, cy, z2_out).setColor(r, g, b, a * 0.7f).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
            consumer.addVertex(pose, x1_out, cy, z1_out).setColor(r, g, b, a * 0.7f).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
            consumer.addVertex(pose, x1_in, cy, z1_in).setColor(r, g, b, a * 0.7f).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
        }

        // Inner glowing translucent core disk (counter-rotating)
        for (int i = 0; i < segments; i++) {
            double a1 = (i * 2.0 * Math.PI / segments) - rotRad * 0.6;
            double a2 = ((i + 1) * 2.0 * Math.PI / segments) - rotRad * 0.6;

            float x1 = cx + (float) Math.cos(a1) * innerRadius;
            float z1 = cz + (float) Math.sin(a1) * innerRadius;
            float x2 = cx + (float) Math.cos(a2) * innerRadius;
            float z2 = cz + (float) Math.sin(a2) * innerRadius;

            // Center glow fan
            consumer.addVertex(pose, cx, cy + 0.003f, cz).setColor(r * 1.3f, g * 1.3f, b * 1.3f, a * 0.6f).setUv(0.5f, 0.5f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
            consumer.addVertex(pose, x1, cy + 0.003f, z1).setColor(r, g, b, a * 0.35f).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
            consumer.addVertex(pose, x2, cy + 0.003f, z2).setColor(r, g, b, a * 0.35f).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
            consumer.addVertex(pose, cx, cy + 0.003f, cz).setColor(r * 1.3f, g * 1.3f, b * 1.3f, a * 0.6f).setUv(0.5f, 0.5f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        }
    }

    private static void renderBeamLine(Matrix4f pose, VertexConsumer consumer, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a, int light, int overlay) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 0.001f) return;

        float ux = dx / len;
        float uy = dy / len;
        float uz = dz / len;

        float px, py, pz;
        if (Math.abs(uy) < 0.95f) {
            px = -uz;
            py = 0;
            pz = ux;
        } else {
            px = 0;
            py = -uz;
            pz = uy;
        }
        float pLen = (float) Math.sqrt(px * px + py * py + pz * pz);
        if (pLen > 0.0001f) {
            px /= pLen;
            py /= pLen;
            pz /= pLen;
        }

        float qx = uy * pz - uz * py;
        float qy = uz * px - ux * pz;
        float qz = ux * py - uy * px;

        float thickness = 0.045f;
        int segments = 8;
        for (int i = 0; i < segments; i++) {
            double a1 = (i * 2.0 * Math.PI) / segments;
            double a2 = ((i + 1) * 2.0 * Math.PI) / segments;

            float cos1 = (float) Math.cos(a1) * thickness;
            float sin1 = (float) Math.sin(a1) * thickness;
            float cos2 = (float) Math.cos(a2) * thickness;
            float sin2 = (float) Math.sin(a2) * thickness;

            float ox1 = px * cos1 + qx * sin1;
            float oy1 = py * cos1 + qy * sin1;
            float oz1 = pz * cos1 + qz * sin1;

            float ox2 = px * cos2 + qx * sin2;
            float oy2 = py * cos2 + qy * sin2;
            float oz2 = pz * cos2 + qz * sin2;

            consumer.addVertex(pose, x1 + ox1, y1 + oy1, z1 + oz1).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(ox1, oy1, oz1);
            consumer.addVertex(pose, x1 + ox2, y1 + oy2, z1 + oz2).setColor(r, g, b, a).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(ox2, oy2, oz2);
            consumer.addVertex(pose, x2 + ox2, y2 + oy2, z2 + oz2).setColor(r, g, b, a).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(ox2, oy2, oz2);
            consumer.addVertex(pose, x2 + ox1, y2 + oy1, z2 + oz1).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(ox1, oy1, oz1);
        }
    }

    public static class AltarRenderState extends BlockEntityRenderState {
        public boolean isStructureValid = false;
        public boolean isStarlightActive = false;
        public int structureTier = 0;
        public int incomingBeamCount = 0;
        public float rotation = 0.0f;
        public boolean hasHeldItem = false;
        public boolean hasStarChart = false;
        public final ItemStackRenderState heldItemState = new ItemStackRenderState();
        public final ItemStackRenderState starChartState = new ItemStackRenderState();
    }
}
