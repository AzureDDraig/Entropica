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

public class CelestialSkyRenderer {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    public static void renderSky(PoseStack poseStack, Matrix4f projectionMatrix, Camera camera, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        Player player = mc.player;
        if (level == null || player == null) return;

        // 1. Dimensional Checks
        boolean isEnd = level.dimension().equals(Level.END);
        boolean isNether = level.dimension().equals(Level.NETHER);
        if (isNether) return; // Nether bedrock roof completely blocks all celestial starlight

        float starBrightness;
        List<Constellation> visibleConstellations;

        if (isEnd) {
            // In The End: Vacuum void, all 24 constellations permanently visible at full brightness
            starBrightness = 1.0f;
            visibleConstellations = new ArrayList<>(ModConstellations.getAllConstellations());
        } else {
            // In Overworld: Day/Night cycle + Moon Phase + Weather attenuation
            if (!level.dimensionType().hasSkyLight()) return;

            starBrightness = level.getStarBrightness(partialTick);
            if (starBrightness <= 0.01f) return;

            // Weather cloud attenuation below Y=192
            float rain = level.getRainLevel(partialTick);
            if (rain > 0.01f && player.getY() < 192.0) {
                starBrightness *= (1.0f - rain * 0.85f);
                if (starBrightness <= 0.01f) return;
            }

            int moonPhase = level.getMoonPhase();
            visibleConstellations = ModConstellations.getVisibleConstellations(moonPhase);
        }

        if (visibleConstellations.isEmpty()) return;

        boolean isScoping = ddraig.net.entropica.item.AstrolabeItem.isScoping(player);
        long gameTime = level.getGameTime();
        float timeAnim = (gameTime + partialTick) * 0.05f;

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(WHITE_TEXTURE));

        int light = 15728880; // Full emissive brightness
        int overlay = OverlayTexture.NO_OVERLAY;

        poseStack.pushPose();

        // Sky rotation matching world celestial rotation
        float celestialAngle = isEnd ? ((gameTime + partialTick) * 0.02f) : (level.getTimeOfDay(partialTick) * 360.0f);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90.0F));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(celestialAngle));

        Matrix4f matrix = poseStack.last().pose();

        // Celestial sphere radius
        float skyRadius = 100.0f;
        int totalVisible = visibleConstellations.size();

        for (int i = 0; i < totalVisible; i++) {
            Constellation constellation = visibleConstellations.get(i);
            boolean isDiscovered = PlayerAstralProgress.isDiscovered(player, constellation);
            boolean canPerceive = PlayerAstralProgress.canPerceiveTier(player, constellation.getTier());

            // If player cannot perceive this tier yet, skip or render as ambient background stars only
            if (!canPerceive && !isDiscovered) {
                continue;
            }

            // Calculate spherical center (azimuth theta and altitude phi)
            float baseAzimuth = (i * (2.0f * (float) Math.PI / totalVisible));
            float baseAltitude = 0.5f + (float) Math.sin(i * 1.7) * 0.3f; // 30 to 50 degrees above horizon

            List<ConstellationStar> stars = constellation.getStars();
            float[][] star3DPos = new float[stars.size()][3];

            // Calculate 3D sphere positions for each star in this constellation
            for (int s = 0; s < stars.size(); s++) {
                ConstellationStar star = stars.get(s);
                float dAzimuth = (star.x() - 50.0f) * 0.0035f;
                float dAltitude = (50.0f - star.y()) * 0.0035f;

                float theta = baseAzimuth + dAzimuth;
                float phi = baseAltitude + dAltitude;

                float x = skyRadius * Mth.cos(phi) * Mth.cos(theta);
                float y = skyRadius * Mth.sin(phi);
                float z = skyRadius * Mth.cos(phi) * Mth.sin(theta);

                star3DPos[s][0] = x;
                star3DPos[s][1] = y;
                star3DPos[s][2] = z;
            }

            // Render Constellation Connecting Lines & Pulse Beads
            if (isDiscovered || isScoping) {
                float lineAlpha = isDiscovered ? (starBrightness * 0.95f) : (starBrightness * 0.65f);
                int rLine = isDiscovered ? 255 : 0;
                int gLine = isDiscovered ? 220 : 220;
                int bLine = isDiscovered ? 100 : 255;
                int aLine = (int) (lineAlpha * 255);

                int edgeIdx = 0;
                for (ConstellationConnection conn : constellation.getConnections()) {
                    if (conn.fromIndex() < stars.size() && conn.toIndex() < stars.size()) {
                        float[] p1 = star3DPos[conn.fromIndex()];
                        float[] p2 = star3DPos[conn.toIndex()];

                        float thickness = isDiscovered ? 0.35f : 0.22f;
                        drawLineSegment(matrix, vertexConsumer, p1[0], p1[1], p1[2], p2[0], p2[1], p2[2], thickness, rLine, gLine, bLine, aLine, light, overlay);

                        // Traveling stardust pulse beads along lines for discovered constellations
                        if (isDiscovered) {
                            float tBead = ((timeAnim * 0.4f) + (edgeIdx * 0.25f)) % 1.0f;
                            float bx = p1[0] * (1.0f - tBead) + p2[0] * tBead;
                            float by = p1[1] * (1.0f - tBead) + p2[1] * tBead;
                            float bz = p1[2] * (1.0f - tBead) + p2[2] * tBead;

                            drawStarQuad(matrix, vertexConsumer, bx, by, bz, 0.4f, 255, 255, 255, (int) (lineAlpha * 240), light, overlay);
                        }
                    }
                    edgeIdx++;
                }
            }

            // Render Star Billboards
            for (int s = 0; s < stars.size(); s++) {
                ConstellationStar star = stars.get(s);
                float[] pos = star3DPos[s];

                int rgb = star.spectralClass().getColorRgb();
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                float twinkle = 0.8f + 0.2f * (float) Math.sin(timeAnim + star.index() * 1.5f);
                float starSize = (isDiscovered ? 1.6f : (isScoping ? 1.3f : 0.8f)) * star.brightness() * twinkle;
                int alpha = (int) (starBrightness * 255);

                drawStarQuad(matrix, vertexConsumer, pos[0], pos[1], pos[2], starSize, r, g, b, alpha, light, overlay);

                // Add halo on discovered stars
                if (isDiscovered) {
                    drawStarQuad(matrix, vertexConsumer, pos[0], pos[1], pos[2], starSize * 2.0f, 255, 215, 100, (int) (alpha * 0.4f), light, overlay);
                }
            }
        }

        poseStack.popPose();
    }

    private static void drawStarQuad(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, float size, int r, int g, int b, int a, int light, int overlay) {
        consumer.addVertex(matrix, x - size, y - size, z).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix, x + size, y - size, z).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix, x + size, y + size, z).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix, x - size, y + size, z).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
    }

    private static void drawLineSegment(Matrix4f matrix, VertexConsumer consumer, float x1, float y1, float z1, float x2, float y2, float z2, float thickness, int r, int g, int b, int a, int light, int overlay) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float len = Mth.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 0.001f) return;

        float nx = -dy / len * thickness;
        float ny = dx / len * thickness;

        consumer.addVertex(matrix, x1 - nx, y1 - ny, z1).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(matrix, x1 + nx, y1 + ny, z1).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(matrix, x2 + nx, y2 + ny, z2).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(matrix, x2 - nx, y2 - ny, z2).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
    }
}
