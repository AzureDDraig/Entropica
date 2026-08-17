package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.api.EssenceType;
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
        NEBULA_COMPLEXES.add(new NebulaComplex(45.0f, 25.0f, "Lagoon Veil", lagoon));

        // 2. The Amethyst Supernova Remnant (Violet, Rose, Magenta)
        List<NebulaPuff> amethyst = new ArrayList<>();
        amethyst.add(new NebulaPuff(0.0f, 0.0f, 46.0f, 0.65f, 0.15f, 0.95f, 0.30f));
        amethyst.add(new NebulaPuff(-10.0f, 6.0f, 34.0f, 0.85f, 0.2f, 0.75f, 0.26f));
        amethyst.add(new NebulaPuff(11.0f, -7.0f, 38.0f, 0.45f, 0.1f, 0.9f, 0.25f));
        amethyst.add(new NebulaPuff(-6.0f, -8.0f, 28.0f, 0.95f, 0.25f, 0.65f, 0.20f));
        amethyst.add(new NebulaPuff(8.0f, 8.0f, 30.0f, 0.55f, 0.1f, 0.85f, 0.22f));
        amethyst.add(new NebulaPuff(14.0f, 2.0f, 24.0f, 0.75f, 0.2f, 0.8f, 0.18f));
        amethyst.add(new NebulaPuff(-15.0f, -3.0f, 22.0f, 0.4f, 0.05f, 0.7f, 0.16f));
        NEBULA_COMPLEXES.add(new NebulaComplex(175.0f, 55.0f, "Amethyst Remnant", amethyst));

        // 3. The Amber Solar Nursery (Golden Amber, Crimson, Solar Orange)
        List<NebulaPuff> amber = new ArrayList<>();
        amber.add(new NebulaPuff(0.0f, 0.0f, 44.0f, 1.0f, 0.65f, 0.1f, 0.32f));
        amber.add(new NebulaPuff(-9.0f, -5.0f, 35.0f, 1.0f, 0.4f, 0.05f, 0.28f));
        amber.add(new NebulaPuff(8.0f, 6.0f, 36.0f, 1.0f, 0.85f, 0.2f, 0.26f));
        amber.add(new NebulaPuff(-6.0f, 7.0f, 26.0f, 0.95f, 0.55f, 0.1f, 0.22f));
        amber.add(new NebulaPuff(7.0f, -7.0f, 28.0f, 1.0f, 0.3f, 0.0f, 0.24f));
        amber.add(new NebulaPuff(13.0f, -2.0f, 22.0f, 0.9f, 0.75f, 0.15f, 0.18f));
        NEBULA_COMPLEXES.add(new NebulaComplex(265.0f, 28.0f, "Amber Nursery", amber));

        // 4. The Obsidian Emerald Shroud (Deep Emerald, Viridian, Indigo)
        List<NebulaPuff> emerald = new ArrayList<>();
        emerald.add(new NebulaPuff(0.0f, 0.0f, 40.0f, 0.05f, 0.85f, 0.55f, 0.28f));
        emerald.add(new NebulaPuff(-8.0f, 5.0f, 32.0f, 0.1f, 0.7f, 0.75f, 0.24f));
        emerald.add(new NebulaPuff(9.0f, -6.0f, 34.0f, 0.0f, 0.95f, 0.45f, 0.22f));
        emerald.add(new NebulaPuff(-7.0f, -6.0f, 24.0f, 0.15f, 0.55f, 0.85f, 0.19f));
        emerald.add(new NebulaPuff(7.0f, 7.0f, 26.0f, 0.05f, 0.8f, 0.6f, 0.20f));
        NEBULA_COMPLEXES.add(new NebulaComplex(330.0f, 45.0f, "Emerald Shroud", emerald));
    }

    // Ambient background stars cache with deterministic Essence typings
    private static final List<float[]> SKY_STARS = new ArrayList<>();
    static {
        Random rng = new Random(987654L);
        EssenceType[] essences = EssenceType.values();
        for (int i = 0; i < 320; i++) {
            float theta = rng.nextFloat() * 360.0f;
            float phi = -25.0f + rng.nextFloat() * 115.0f; // -25 to +90 degrees for rich sky depth
            float size = 0.85f + rng.nextFloat() * 1.6f;
            EssenceType ess = essences[rng.nextInt(essences.length)];
            float er = ess.getR() / 255.0f;
            float eg = ess.getG() / 255.0f;
            float eb = ess.getB() / 255.0f;
            float r = Mth.lerp(0.55f, 0.9f, er);
            float g = Mth.lerp(0.55f, 0.9f, eg);
            float b = Mth.lerp(0.55f, 0.9f, eb);
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

            float twinkle = 0.75f + 0.25f * Mth.sin(timeAnim + s[0]);
            float a = starBrightness * twinkle * 0.85f;

            renderBillboardQuad(starConsumer, matrix, x, y, z, sSize, sr, sg, sb, a, light, overlay);
        }

        // B. Render Constellation Star Vertices (Elevated high in circumpolar sky: 48° to 72°)
        int totalVisible = visibleConstellations.size();
        List<float[][]> allConstellationWorldPositions = new ArrayList<>();

        for (int i = 0; i < totalVisible; i++) {
            Constellation constellation = visibleConstellations.get(i);

            float baseAzimuth = (float) Math.toRadians(i * (360.0f / Math.max(1, totalVisible)));
            float baseAltitude = (float) Math.toRadians(48.0f + ((i * 17) % 24));

            List<ConstellationStar> stars = constellation.getStars();
            float[][] starWorldPos = new float[stars.size()][3];
            boolean isDiscovered = PlayerAstralProgress.isDiscovered(player, constellation);

            float er = constellation.getEssenceType().getR() / 255.0f;
            float eg = constellation.getEssenceType().getG() / 255.0f;
            float eb = constellation.getEssenceType().getB() / 255.0f;

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
                float specR = ((rgb >> 16) & 0xFF) / 255.0f;
                float specG = ((rgb >> 8) & 0xFF) / 255.0f;
                float specB = (rgb & 0xFF) / 255.0f;

                float r = isDiscovered ? er : Mth.lerp(0.45f, specR, er);
                float g = isDiscovered ? eg : Mth.lerp(0.45f, specG, eg);
                float b = isDiscovered ? eb : Mth.lerp(0.45f, specB, eb);

                float starSize = Math.max(2.0f, star.brightness() * 2.4f);
                float twinkle = 0.85f + 0.15f * Mth.sin(timeAnim * 2.0f + s * 1.5f);
                float alpha = starBrightness * twinkle;

                renderBillboardQuad(starConsumer, matrix, x, y, z, starSize, r, g, b, alpha, light, overlay);
            }

            allConstellationWorldPositions.add(starWorldPos);
        }

        bufferSource.endBatch(starRenderType);

        // ----------------------------------------------------
        // PASS 3: Single Clean, Tangent-Aligned Constellation Starlight Lines
        // ----------------------------------------------------
        VertexConsumer lineConsumer = bufferSource.getBuffer(lineRenderType);

        for (int i = 0; i < totalVisible; i++) {
            Constellation constellation = visibleConstellations.get(i);
            boolean isDiscovered = PlayerAstralProgress.isDiscovered(player, constellation);

            if (isDiscovered) {
                float er = constellation.getEssenceType().getR() / 255.0f;
                float eg = constellation.getEssenceType().getG() / 255.0f;
                float eb = constellation.getEssenceType().getB() / 255.0f;

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

                        // Single crisp, spherical-tangent starlight beam
                        renderSphericalLineSegment(lineConsumer, matrix, x1, y1, z1, x2, y2, z2, 0.55f, er, eg, eb, starBrightness * 0.85f, light, overlay);
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

    private static void renderSphericalLineSegment(VertexConsumer builder, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float width, float r, float g, float b, float a, int light, int overlay) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;

        float mx = (x1 + x2) * 0.5f;
        float my = (y1 + y2) * 0.5f;
        float mz = (z1 + z2) * 0.5f;

        // Cross product of line vector D and midpoint normal M yields the spherical tangent vector
        float wx = dy * mz - dz * my;
        float wy = dz * mx - dx * mz;
        float wz = dx * my - dy * mx;
        float wLen = (float) Math.sqrt(wx * wx + wy * wy + wz * wz);
        if (wLen > 1e-4f) {
            wx /= wLen;
            wy /= wLen;
            wz /= wLen;
        } else {
            wx = 0; wy = 1; wz = 0;
        }

        float hw = width * 0.5f;
        float offX = wx * hw;
        float offY = wy * hw;
        float offZ = wz * hw;

        builder.addVertex(matrix, x1 - offX, y1 - offY, z1 - offZ).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        builder.addVertex(matrix, x2 - offX, y2 - offY, z2 - offZ).setColor(r, g, b, a).setUv(1.0f, 0.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        builder.addVertex(matrix, x2 + offX, y2 + offY, z2 + offZ).setColor(r, g, b, a).setUv(1.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        builder.addVertex(matrix, x1 + offX, y1 + offY, z1 + offZ).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
    }
}
