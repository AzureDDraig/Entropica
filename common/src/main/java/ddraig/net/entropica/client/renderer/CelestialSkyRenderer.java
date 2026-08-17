package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.astral.Constellation;
import ddraig.net.entropica.astral.ConstellationConnection;
import ddraig.net.entropica.astral.ConstellationStar;
import ddraig.net.entropica.astral.ModConstellations;
import ddraig.net.entropica.astral.PlayerAstralProgress;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CelestialSkyRenderer {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private static final ResourceLocation STAR_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/star.png");

    // Ambient background stars cache for skybox
    private static final List<float[]> SKY_STARS = new ArrayList<>();
    static {
        Random rng = new Random(987654L);
        for (int i = 0; i < 280; i++) {
            float theta = rng.nextFloat() * 360.0f;
            float phi = -5.0f + rng.nextFloat() * 95.0f;
            float size = 0.8f + rng.nextFloat() * 1.6f;
            float r = 0.8f + rng.nextFloat() * 0.2f;
            float g = 0.85f + rng.nextFloat() * 0.15f;
            float b = 0.9f + rng.nextFloat() * 0.1f;
            SKY_STARS.add(new float[]{theta, phi, size, r, g, b});
        }
    }

    public static void renderSky(PoseStack poseStack, Matrix4f projectionMatrix, Camera camera, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        Player player = mc.player;
        if (level == null || player == null) return;

        // 1. Dimensional Checks
        boolean isEnd = level.dimension().equals(Level.END);
        boolean isNether = level.dimension().equals(Level.NETHER);
        if (isNether) return; // Nether bedrock roof blocks celestial starlight

        float starBrightness;
        List<Constellation> visibleConstellations;

        if (isEnd) {
            starBrightness = 1.0f;
            visibleConstellations = new ArrayList<>(ModConstellations.getAllConstellations());
        } else {
            if (!level.dimensionType().hasSkyLight()) return;

            starBrightness = level.getStarBrightness(partialTick);
            if (starBrightness <= 0.01f) return;

            float rain = level.getRainLevel(partialTick);
            if (rain > 0.01f && player.getY() < 192.0) {
                starBrightness *= (1.0f - rain * 0.85f);
                if (starBrightness <= 0.01f) return;
            }

            int moonPhase = level.getMoonPhase();
            visibleConstellations = ModConstellations.getVisibleConstellations(moonPhase);
        }

        long gameTime = level.getGameTime();
        float timeAnim = (gameTime + partialTick) * 0.05f;

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        RenderType renderType = RenderType.entityTranslucentEmissive(STAR_TEXTURE);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

        int light = 15728880;
        int overlay = OverlayTexture.NO_OVERLAY;

        poseStack.pushPose();

        float celestialAngle = isEnd ? ((gameTime + partialTick) * 0.02f) : (level.getTimeOfDay(partialTick) * 360.0f);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90.0F));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(celestialAngle));

        Matrix4f matrix = poseStack.last().pose();
        float skyRadius = 100.0f;

        // A. Render Ambient Stars in Skybox
        for (float[] s : SKY_STARS) {
            float theta = (float) Math.toRadians(s[0]);
            float phi = (float) Math.toRadians(s[1]);
            float sSize = s[2];
            float sr = s[3];
            float sg = s[4];
            float sb = s[5];

            float x = skyRadius * Mth.cos(phi) * Mth.sin(theta);
            float y = skyRadius * Mth.sin(phi);
            float z = skyRadius * Mth.cos(phi) * Mth.cos(theta);

            float twinkle = 0.7f + 0.3f * Mth.sin(timeAnim + s[0]);
            float a = starBrightness * twinkle * 0.9f;

            renderBillboardQuad(vertexConsumer, matrix, x, y, z, sSize, sr, sg, sb, a, light, overlay);
        }

        // B. Render Nebulae Clouds in Skybox
        renderNebulaCloud(vertexConsumer, matrix, skyRadius, 45.0f, 30.0f, 35.0f, 0.0f, 0.5f, 1.0f, starBrightness * 0.25f, light, overlay);
        renderNebulaCloud(vertexConsumer, matrix, skyRadius, 180.0f, 60.0f, 40.0f, 0.6f, 0.1f, 0.9f, starBrightness * 0.22f, light, overlay);
        renderNebulaCloud(vertexConsumer, matrix, skyRadius, 270.0f, 40.0f, 30.0f, 1.0f, 0.5f, 0.1f, starBrightness * 0.18f, light, overlay);

        // C. Render Constellations and Lines
        int totalVisible = visibleConstellations.size();

        for (int i = 0; i < totalVisible; i++) {
            Constellation constellation = visibleConstellations.get(i);
            boolean isDiscovered = PlayerAstralProgress.isDiscovered(player, constellation);

            float baseAzimuth = (i * (2.0f * (float) Math.PI / Math.max(1, totalVisible)));
            float baseAltitude = 0.55f + (float) Math.sin(i * 1.7) * 0.35f;

            List<ConstellationStar> stars = constellation.getStars();
            float[][] starWorldPos = new float[stars.size()][3];

            for (int s = 0; s < stars.size(); s++) {
                ConstellationStar star = stars.get(s);
                float starAzimuth = baseAzimuth + (star.x() - 50.0f) * 0.0055f;
                float starAltitude = baseAltitude + (50.0f - star.y()) * 0.0055f;

                float x = skyRadius * Mth.cos(starAltitude) * Mth.sin(starAzimuth);
                float y = skyRadius * Mth.sin(starAltitude);
                float z = skyRadius * Mth.cos(starAltitude) * Mth.cos(starAzimuth);

                starWorldPos[s][0] = x;
                starWorldPos[s][1] = y;
                starWorldPos[s][2] = z;

                int rgb = star.spectralClass().getColorRgb();
                float r = ((rgb >> 16) & 0xFF) / 255.0f;
                float g = ((rgb >> 8) & 0xFF) / 255.0f;
                float b = (rgb & 0xFF) / 255.0f;

                float starSize = star.brightness() * 1.8f;
                float twinkle = 0.85f + 0.15f * Mth.sin(timeAnim * 2.0f + s * 1.5f);
                float alpha = starBrightness * twinkle;

                renderBillboardQuad(vertexConsumer, matrix, x, y, z, starSize, r, g, b, alpha, light, overlay);
            }

            // Render Connecting Lines for Discovered Constellations
            if (isDiscovered) {
                int edgeIndex = 0;
                for (ConstellationConnection conn : constellation.getConnections()) {
                    if (conn.fromIndex() < stars.size() && conn.toIndex() < stars.size()) {
                        float x1 = starWorldPos[conn.fromIndex()][0];
                        float y1 = starWorldPos[conn.fromIndex()][1];
                        float z1 = starWorldPos[conn.fromIndex()][2];

                        float x2 = starWorldPos[conn.toIndex()][0];
                        float y2 = starWorldPos[conn.toIndex()][1];
                        float z2 = starWorldPos[conn.toIndex()][2];

                        renderLineSegment(vertexConsumer, matrix, x1, y1, z1, x2, y2, z2, 0.25f, 1.0f, 0.85f, 0.2f, starBrightness * 0.8f, light, overlay);

                        // Pulse bead traveling along line
                        float tBead = ((gameTime + partialTick) * 0.03f + edgeIndex * 0.35f) % 1.0f;
                        float bx = x1 * (1.0f - tBead) + x2 * tBead;
                        float by = y1 * (1.0f - tBead) + y2 * tBead;
                        float bz = z1 * (1.0f - tBead) + z2 * tBead;

                        renderBillboardQuad(vertexConsumer, matrix, bx, by, bz, 0.8f, 1.0f, 1.0f, 1.0f, starBrightness * 0.95f, light, overlay);
                    }
                    edgeIndex++;
                }
            }
        }

        poseStack.popPose();

        // Flush and immediately draw all batched vertices to screen!
        bufferSource.endBatch(renderType);
    }

    private static void renderNebulaCloud(VertexConsumer builder, Matrix4f matrix, float radius, float azimDeg, float altDeg, float size, float r, float g, float b, float a, int light, int overlay) {
        float azim = (float) Math.toRadians(azimDeg);
        float alt = (float) Math.toRadians(altDeg);

        float x = radius * 0.95f * Mth.cos(alt) * Mth.sin(azim);
        float y = radius * 0.95f * Mth.sin(alt);
        float z = radius * 0.95f * Mth.cos(alt) * Mth.cos(azim);

        renderBillboardQuad(builder, matrix, x, y, z, size, r, g, b, a, light, overlay);
    }

    private static void renderBillboardQuad(VertexConsumer builder, Matrix4f matrix, float x, float y, float z, float size, float r, float g, float b, float a, int light, int overlay) {
        float hs = size * 0.5f;

        builder.addVertex(matrix, x - hs, y - hs, z).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        builder.addVertex(matrix, x + hs, y - hs, z).setColor(r, g, b, a).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        builder.addVertex(matrix, x + hs, y + hs, z).setColor(r, g, b, a).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        builder.addVertex(matrix, x - hs, y + hs, z).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
    }

    private static void renderLineSegment(VertexConsumer builder, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float width, float r, float g, float b, float a, int light, int overlay) {
        float hw = width * 0.5f;

        builder.addVertex(matrix, x1 - hw, y1 - hw, z1).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        builder.addVertex(matrix, x2 - hw, y2 - hw, z2).setColor(r, g, b, a).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        builder.addVertex(matrix, x2 + hw, y2 + hw, z2).setColor(r, g, b, a).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        builder.addVertex(matrix, x1 + hw, y1 + hw, z1).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
    }
}
