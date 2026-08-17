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

    public static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    public static final ResourceLocation STAR_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/star.png");
    public static final ResourceLocation NEBULA_PUFF_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/environment/nebula_puff.png");

    public record NebulaPuff(float dAzim, float dAlt, float size, float r, float g, float b, float a) {}

    public static class NebulaComplex {
        public final float baseAzim;
        public final float baseAlt;
        public final String name;
        public final List<NebulaPuff> puffs;

        public NebulaComplex(float baseAzim, float baseAlt, String name, List<NebulaPuff> puffs) {
            this.baseAzim = baseAzim;
            this.baseAlt = baseAlt;
            this.name = name;
            this.puffs = puffs;
        }
    }

    public static final List<NebulaComplex> NEBULA_COMPLEXES = new ArrayList<>();
    static {
        // 1. The Great Azure Lagoon Veil (Cyan, Teal, Sapphire)
        List<NebulaPuff> lagoon = new ArrayList<>();
        lagoon.add(new NebulaPuff(0.0f, 0.0f, 42.0f, 0.0f, 0.65f, 1.0f, 0.32f));
        lagoon.add(new NebulaPuff(-8.0f, 4.0f, 32.0f, 0.1f, 0.85f, 0.9f, 0.28f));
        lagoon.add(new NebulaPuff(9.0f, -5.0f, 36.0f, 0.0f, 0.45f, 0.95f, 0.25f));
        lagoon.add(new NebulaPuff(-5.0f, -6.0f, 26.0f, 0.2f, 0.95f, 1.0f, 0.22f));
        lagoon.add(new NebulaPuff(6.0f, 7.0f, 28.0f, 0.05f, 0.55f, 0.85f, 0.24f));
        lagoon.add(new NebulaPuff(-12.0f, -2.0f, 20.0f, 0.15f, 0.75f, 1.0f, 0.18f));
        lagoon.add(new NebulaPuff(13.0f, 3.0f, 22.0f, 0.0f, 0.4f, 0.8f, 0.19f));
        NEBULA_COMPLEXES.add(new NebulaComplex(45.0f, 30.0f, "Lagoon Veil", lagoon));

        // 2. The Amethyst Supernova Remnant (Violet, Rose, Magenta)
        List<NebulaPuff> amethyst = new ArrayList<>();
        amethyst.add(new NebulaPuff(0.0f, 0.0f, 46.0f, 0.65f, 0.15f, 0.95f, 0.30f));
        amethyst.add(new NebulaPuff(-10.0f, 6.0f, 34.0f, 0.85f, 0.2f, 0.75f, 0.26f));
        amethyst.add(new NebulaPuff(11.0f, -7.0f, 38.0f, 0.45f, 0.1f, 0.9f, 0.25f));
        amethyst.add(new NebulaPuff(-6.0f, -8.0f, 28.0f, 0.95f, 0.25f, 0.65f, 0.20f));
        amethyst.add(new NebulaPuff(8.0f, 8.0f, 30.0f, 0.55f, 0.1f, 0.85f, 0.22f));
        amethyst.add(new NebulaPuff(14.0f, 2.0f, 24.0f, 0.75f, 0.2f, 0.8f, 0.18f));
        amethyst.add(new NebulaPuff(-15.0f, -3.0f, 22.0f, 0.4f, 0.05f, 0.7f, 0.16f));
        NEBULA_COMPLEXES.add(new NebulaComplex(175.0f, 58.0f, "Amethyst Remnant", amethyst));

        // 3. The Amber Solar Nursery (Golden Amber, Crimson, Solar Orange)
        List<NebulaPuff> amber = new ArrayList<>();
        amber.add(new NebulaPuff(0.0f, 0.0f, 44.0f, 1.0f, 0.65f, 0.1f, 0.32f));
        amber.add(new NebulaPuff(-9.0f, -5.0f, 35.0f, 1.0f, 0.4f, 0.05f, 0.28f));
        amber.add(new NebulaPuff(8.0f, 6.0f, 36.0f, 1.0f, 0.85f, 0.2f, 0.26f));
        amber.add(new NebulaPuff(-6.0f, 7.0f, 26.0f, 0.95f, 0.55f, 0.1f, 0.22f));
        amber.add(new NebulaPuff(7.0f, -7.0f, 28.0f, 1.0f, 0.3f, 0.0f, 0.24f));
        amber.add(new NebulaPuff(13.0f, -2.0f, 22.0f, 0.9f, 0.75f, 0.15f, 0.18f));
        NEBULA_COMPLEXES.add(new NebulaComplex(265.0f, 38.0f, "Amber Nursery", amber));

        // 4. The Obsidian Emerald Shroud (Deep Emerald, Viridian, Indigo)
        List<NebulaPuff> emerald = new ArrayList<>();
        emerald.add(new NebulaPuff(0.0f, 0.0f, 40.0f, 0.05f, 0.85f, 0.55f, 0.28f));
        emerald.add(new NebulaPuff(-8.0f, 5.0f, 32.0f, 0.1f, 0.7f, 0.75f, 0.24f));
        emerald.add(new NebulaPuff(9.0f, -6.0f, 34.0f, 0.0f, 0.95f, 0.45f, 0.22f));
        emerald.add(new NebulaPuff(-7.0f, -6.0f, 24.0f, 0.15f, 0.55f, 0.85f, 0.19f));
        emerald.add(new NebulaPuff(7.0f, 7.0f, 26.0f, 0.05f, 0.8f, 0.6f, 0.20f));
        NEBULA_COMPLEXES.add(new NebulaComplex(330.0f, 48.0f, "Emerald Shroud", emerald));
    }

    // Ambient background stars cache for skybox
    private static final List<float[]> SKY_STARS = new ArrayList<>();
    static {
        Random rng = new Random(987654L);
        for (int i = 0; i < 280; i++) {
            float theta = rng.nextFloat() * 360.0f;
            float phi = -10.0f + rng.nextFloat() * 105.0f;
            float size = 0.9f + rng.nextFloat() * 1.8f;
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
        if (isNether) return;

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
        RenderType starRenderType = RenderType.entityTranslucentEmissive(STAR_TEXTURE);
        RenderType nebulaRenderType = RenderType.entityTranslucentEmissive(NEBULA_PUFF_TEXTURE);
        RenderType lineRenderType = RenderType.entityTranslucentEmissive(WHITE_TEXTURE);

        int light = 15728880;
        int overlay = OverlayTexture.NO_OVERLAY;

        poseStack.pushPose();

        float celestialAngle = isEnd ? ((gameTime + partialTick) * 0.02f) : (level.getTimeOfDay(partialTick) * 360.0f);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90.0F));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(celestialAngle));

        Matrix4f matrix = poseStack.last().pose();
        float skyRadius = 100.0f;

        // ----------------------------------------------------
        // PASS 1: Organic Nebulae Clouds (Using NEBULA_PUFF_TEXTURE)
        // ----------------------------------------------------
        VertexConsumer nebulaConsumer = bufferSource.getBuffer(nebulaRenderType);
        for (NebulaComplex complex : NEBULA_COMPLEXES) {
            for (NebulaPuff puff : complex.puffs) {
                float azimDeg = complex.baseAzim + puff.dAzim();
                float altDeg = complex.baseAlt + puff.dAlt();
                float azim = (float) Math.toRadians(azimDeg);
                float alt = (float) Math.toRadians(altDeg);

                float x = skyRadius * 0.94f * Mth.cos(alt) * Mth.sin(azim);
                float y = skyRadius * 0.94f * Mth.sin(alt);
                float z = skyRadius * 0.94f * Mth.cos(alt) * Mth.cos(azim);

                float a = starBrightness * puff.a();
                renderBillboardQuad(nebulaConsumer, matrix, x, y, z, puff.size(), puff.r(), puff.g(), puff.b(), a, light, overlay);
            }
        }
        bufferSource.endBatch(nebulaRenderType);

        // ----------------------------------------------------
        // PASS 2: Ambient Stars & Constellation Nodes (Using STAR_TEXTURE)
        // ----------------------------------------------------
        VertexConsumer starConsumer = bufferSource.getBuffer(starRenderType);

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

            renderBillboardQuad(starConsumer, matrix, x, y, z, sSize, sr, sg, sb, a, light, overlay);
        }

        // B. Render Constellation Star Vertices
        int totalVisible = visibleConstellations.size();
        List<float[][]> allConstellationWorldPositions = new ArrayList<>();

        for (int i = 0; i < totalVisible; i++) {
            Constellation constellation = visibleConstellations.get(i);

            float baseAzimuth = (float) Math.toRadians(i * (360.0f / Math.max(1, totalVisible)));
            float baseAltitude = (float) Math.toRadians(35.0f + (float) Math.sin(i * 1.7) * 22.0f);

            List<ConstellationStar> stars = constellation.getStars();
            float[][] starWorldPos = new float[stars.size()][3];

            for (int s = 0; s < stars.size(); s++) {
                ConstellationStar star = stars.get(s);
                float starAzimuth = baseAzimuth + (float) Math.toRadians((50.0f - star.x()) * 0.28f);
                float starAltitude = baseAltitude + (float) Math.toRadians((star.y() - 50.0f) * 0.28f);

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

                float starSize = Math.max(2.2f, star.brightness() * 2.6f);
                float twinkle = 0.85f + 0.15f * Mth.sin(timeAnim * 2.0f + s * 1.5f);
                float alpha = starBrightness * twinkle;

                renderBillboardQuad(starConsumer, matrix, x, y, z, starSize, r, g, b, alpha, light, overlay);
            }

            allConstellationWorldPositions.add(starWorldPos);
        }

        // Render traveling stardust pulse sparks on discovered constellation lines
        for (int i = 0; i < totalVisible; i++) {
            Constellation constellation = visibleConstellations.get(i);
            boolean isDiscovered = PlayerAstralProgress.isDiscovered(player, constellation);
            if (isDiscovered) {
                float[][] starWorldPos = allConstellationWorldPositions.get(i);
                List<ConstellationStar> stars = constellation.getStars();
                int edgeIndex = 0;
                for (ConstellationConnection conn : constellation.getConnections()) {
                    if (conn.fromIndex() < stars.size() && conn.toIndex() < stars.size()) {
                        float x1 = starWorldPos[conn.fromIndex()][0];
                        float y1 = starWorldPos[conn.fromIndex()][1];
                        float z1 = starWorldPos[conn.fromIndex()][2];

                        float x2 = starWorldPos[conn.toIndex()][0];
                        float y2 = starWorldPos[conn.toIndex()][1];
                        float z2 = starWorldPos[conn.toIndex()][2];

                        float tBead = ((gameTime + partialTick) * 0.035f + edgeIndex * 0.35f) % 1.0f;
                        float bx = x1 * (1.0f - tBead) + x2 * tBead;
                        float by = y1 * (1.0f - tBead) + y2 * tBead;
                        float bz = z1 * (1.0f - tBead) + z2 * tBead;

                        renderBillboardQuad(starConsumer, matrix, bx, by, bz, 1.6f, 1.0f, 1.0f, 1.0f, starBrightness * 0.98f, light, overlay);
                    }
                    edgeIndex++;
                }
            }
        }

        bufferSource.endBatch(starRenderType);

        // ----------------------------------------------------
        // PASS 3: Glowing Constellation Starlight Lines (Using WHITE_TEXTURE)
        // ----------------------------------------------------
        VertexConsumer lineConsumer = bufferSource.getBuffer(lineRenderType);

        for (int i = 0; i < totalVisible; i++) {
            Constellation constellation = visibleConstellations.get(i);
            boolean isDiscovered = PlayerAstralProgress.isDiscovered(player, constellation);

            if (isDiscovered) {
                float[][] starWorldPos = allConstellationWorldPositions.get(i);
                List<ConstellationStar> stars = constellation.getStars();

                for (ConstellationConnection conn : constellation.getConnections()) {
                    if (conn.fromIndex() < stars.size() && conn.toIndex() < stars.size()) {
                        float x1 = starWorldPos[conn.fromIndex()][0];
                        float y1 = starWorldPos[conn.fromIndex()][1];
                        float z1 = starWorldPos[conn.fromIndex()][2];

                        float x2 = starWorldPos[conn.toIndex()][0];
                        float y2 = starWorldPos[conn.toIndex()][1];
                        float z2 = starWorldPos[conn.toIndex()][2];

                        // Outer Glowing Starlight Halo (Width 1.5f, soft amber-gold)
                        renderLineSegment(lineConsumer, matrix, x1, y1, z1, x2, y2, z2, 1.5f, 1.0f, 0.75f, 0.1f, starBrightness * 0.45f, light, overlay);

                        // Inner Brilliant Starlight Beam (Width 0.7f, luminous gold-white)
                        renderLineSegment(lineConsumer, matrix, x1, y1, z1, x2, y2, z2, 0.7f, 1.0f, 0.95f, 0.65f, starBrightness * 0.95f, light, overlay);
                    }
                }
            }
        }

        bufferSource.endBatch(lineRenderType);

        poseStack.popPose();
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
