package ddraig.net.entropica.client.worldgen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.config.EntropicaConfig;
import ddraig.net.entropica.worldgen.biome.SkyPhenomenonType;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/**
 * 100% Sodium & Iris/Oculus Shader-Compatible Procedural Sky Phenomenon Renderer.
 *
 * <p>Uses standard {@link MultiBufferSource} + {@link RenderType#entityTranslucentEmissive}
 * without raw GL state mutations or custom core GLSL shaders, allowing Iris deferred G-buffers
 * (Complementary, BSL, Photon, Bliss) to apply native bloom and water reflections cleanly.</p>
 */
public final class BiomeSkyPhenomenaRenderer {
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private static final int FULL_LIGHT = 15728880;
    private static final int NO_OVERLAY = OverlayTexture.NO_OVERLAY;

    private BiomeSkyPhenomenaRenderer() {}

    public static void renderSkyPhenomena(PoseStack poseStack, Matrix4f projOrModelView, Camera camera, float deltaTicks) {
        if (!EntropicaConfig.ENABLE_BIOME_CUSTOM_SKY.get()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || mc.player == null) {
            return;
        }

        // Obtain true [0..1] sub-tick partialTick (callers pass getGameTimeDeltaTicks() as 4th arg)
        float partialTick = mc.getDeltaTracker() != null
                ? Mth.clamp(mc.getDeltaTracker().getGameTimeDeltaPartialTick(false), 0.0F, 1.0F)
                : Mth.clamp(deltaTicks, 0.0F, 1.0F);

        SkyPhenomenonType fadeOutType = BiomeAtmosphereClientManager.getFadingOutPhenomenon();
        float fadeOutAlpha = BiomeAtmosphereClientManager.getFadingOutPhenomenonAlpha(partialTick);
        boolean renderFadeOut = fadeOutType != null && fadeOutType != SkyPhenomenonType.NONE && fadeOutAlpha > 0.005F;

        SkyPhenomenonType activeType = BiomeAtmosphereClientManager.getActivePhenomenon();
        float activeAlpha = BiomeAtmosphereClientManager.getRenderPhenomenonAlpha(partialTick);
        boolean renderActive = activeType != null && activeType != SkyPhenomenonType.NONE && activeAlpha > 0.005F;

        if (!renderFadeOut && !renderActive) {
            return;
        }

        double time = (mc.level.getGameTime() + (double) partialTick) * 0.035D;

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        RenderType emissiveType = RenderType.entityTranslucentEmissive(WHITE_TEXTURE);
        VertexConsumer vc = bufferSource.getBuffer(emissiveType);

        poseStack.pushPose();
        // Centered on camera in world-orientation (translation 0,0,0 = camera eye position)
        Matrix4f mat = poseStack.last().pose();

        // Cross-fade outgoing phenomenon and incoming active phenomenon in one emissive batch
        if (renderFadeOut) {
            renderPhenomenonLayer(
                    mat, vc, fadeOutType, time, fadeOutAlpha,
                    BiomeAtmosphereClientManager.getFadeOutPrimR(),
                    BiomeAtmosphereClientManager.getFadeOutPrimG(),
                    BiomeAtmosphereClientManager.getFadeOutPrimB(),
                    BiomeAtmosphereClientManager.getFadeOutSecR(),
                    BiomeAtmosphereClientManager.getFadeOutSecG(),
                    BiomeAtmosphereClientManager.getFadeOutSecB()
            );
        }

        if (renderActive) {
            renderPhenomenonLayer(
                    mat, vc, activeType, time, activeAlpha,
                    BiomeAtmosphereClientManager.getPrimR(),
                    BiomeAtmosphereClientManager.getPrimG(),
                    BiomeAtmosphereClientManager.getPrimB(),
                    BiomeAtmosphereClientManager.getSecR(),
                    BiomeAtmosphereClientManager.getSecG(),
                    BiomeAtmosphereClientManager.getSecB()
            );
        }

        poseStack.popPose();
        bufferSource.endBatch(emissiveType);
    }

    /**
     * Smooth Hermite C1 easing {@code t^2 * (3 - 2t)} on {@code [0, 1]} so sky phenomena
     * ease softly out of invisible upper-atmosphere mist with zero pop-in slope discontinuity.
     */
    public static float hermiteEase(float alpha) {
        float t = Mth.clamp(alpha, 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    /**
     * Continuous vertical/radial emergence factor on {@code [0, 1]} that gently unfurls
     * sky curtains, halos, veils, and rifts as {@code alpha} rises from {@code 0.0 -> 1.0},
     * reaching near-full structural proportions across standard biome intensity levels ({@code 0.65+}).
     */
    public static float computeEmergenceFactor(float alpha) {
        float clamped = Mth.clamp(alpha, 0.0F, 1.0F);
        return 0.65F * hermiteEase(clamped / 0.65F) + 0.35F * hermiteEase(clamped);
    }

    private static void renderPhenomenonLayer(
            Matrix4f mat, VertexConsumer vc, SkyPhenomenonType type,
            double time, float rawAlpha,
            float pr, float pg, float pb, float sr, float sg, float sb) {
        float easedAlpha = hermiteEase(rawAlpha);
        if (easedAlpha <= 1.0E-5F) {
            return;
        }
        float emerge = computeEmergenceFactor(rawAlpha);

        switch (type) {
            case GLACIAL_ICE_HALO -> renderGlacialIceHalo(mat, vc, time, easedAlpha, emerge, pr, pg, pb, sr, sg, sb);
            case AURORA_CURTAINS -> renderAuroraCurtains(mat, vc, time, easedAlpha, emerge, pr, pg, pb, sr, sg, sb);
            case ASTRAL_STARDUST_VEIL -> renderAstralStardustVeil(mat, vc, time, easedAlpha, emerge, pr, pg, pb, sr, sg, sb);
            case ECLIPSE_CORONA -> renderEclipseCorona(mat, vc, time, easedAlpha, emerge, pr, pg, pb, sr, sg, sb);
            case VOID_RIFT_FRACTURES -> renderVoidRiftFractures(mat, vc, time, easedAlpha, emerge, pr, pg, pb, sr, sg, sb);
            case MIRAGE_HEAT_SHIMMER -> renderMirageHeatShimmer(mat, vc, time, easedAlpha, emerge, pr, pg, pb, sr, sg, sb);
            default -> {}
        }
    }

    /**
     * Crystalline polar parhelion ("sun-dog" / lunar ice halo) ring & 4 cardinal prism glints
     * overhead for Biome 1 (Blue-Glacier Shelf) and Biome 4 (Crystalline Pack-Ice).
     */
    private static void renderGlacialIceHalo(
            Matrix4f mat, VertexConsumer vc, double time, float alpha, float emerge,
            float pr, float pg, float pb, float sr, float sg, float sb) {
        int segments = 48;
        float y = 78.0F + (1.0F - emerge) * 8.0F;
        float radialScale = Mth.lerp(emerge, 0.86F, 1.0F);
        float innerR = 38.0F * radialScale;
        float featherWidth = Mth.lerp(emerge, 7.5F, 4.5F) * radialScale;
        float outerR = innerR + featherWidth;
        float innerMistR = Math.max(0.0F, innerR - Mth.lerp(emerge, 4.0F, 1.8F) * radialScale);
        float ringAlpha = alpha * 0.28F;

        for (int i = 0; i < segments; i++) {
            float a0 = (float) (i * Math.PI * 2.0 / segments);
            float a1 = (float) ((i + 1) * Math.PI * 2.0 / segments);
            float shimmer = 0.75F + 0.25F * Mth.sin((float) (time * 1.4D + a0 * 4.0F));
            float mix = 0.5F + 0.5F * Mth.cos(a0 * 2.0F);
            float r = Mth.lerp(mix, pr, sr);
            float g = Mth.lerp(mix, pg, sg);
            float b = Mth.lerp(mix, pb, sb);

            float c0 = Mth.cos(a0), s0 = Mth.sin(a0);
            float c1 = Mth.cos(a1), s1 = Mth.sin(a1);
            float crestA = ringAlpha * shimmer;

            // Soft inner feather so the ring emerges from diffuse mist without a sharp inner seam
            emitDoubleSidedQuad(mat, vc,
                    c0 * innerMistR, y + 0.5F, s0 * innerMistR, r, g, b, 0.0F,
                    c1 * innerMistR, y + 0.5F, s1 * innerMistR, r, g, b, 0.0F,
                    c1 * innerR, y, s1 * innerR, r, g, b, crestA,
                    c0 * innerR, y, s0 * innerR, r, g, b, crestA);
            // Outer parhelion halo feather
            emitDoubleSidedQuad(mat, vc,
                    c0 * innerR, y, s0 * innerR, r, g, b, crestA,
                    c1 * innerR, y, s1 * innerR, r, g, b, crestA,
                    c1 * outerR, y + 1.5F, s1 * outerR, r, g, b, 0.0F,
                    c0 * outerR, y + 1.5F, s0 * outerR, r, g, b, 0.0F);
        }

        // 4 Parhelion diamond glints crystallizing smoothly around the ice halo
        float glintR = 40.0F * radialScale;
        float glintHalfH = 4.5F * Mth.lerp(emerge, 0.45F, 1.0F);
        float glintHalfW = 3.2F * Mth.lerp(emerge, 0.45F, 1.0F);
        float coreR = Mth.lerp(emerge, pr, 1.0F);
        float coreG = Mth.lerp(emerge, pg, 1.0F);
        float coreB = Mth.lerp(emerge, pb, 1.0F);

        for (int k = 0; k < 4; k++) {
            float ang = (float) (k * Math.PI * 0.5D + time * 0.04D);
            float gx = Mth.cos(ang) * glintR;
            float gz = Mth.sin(ang) * glintR;
            float pulse = (0.65F + 0.35F * Mth.sin((float) (time * 2.2D + k))) * alpha * emerge * 0.48F;
            float tx = -Mth.sin(ang) * glintHalfW;
            float tz = Mth.cos(ang) * glintHalfW;

            emitDoubleSidedQuad(mat, vc,
                    gx, y + glintHalfH, gz, coreR, coreG, coreB, pulse,
                    gx + tx, y, gz + tz, pr, pg, pb, pulse * 0.55F,
                    gx, y - glintHalfH, gz, sr, sg, sb, pulse * 0.35F,
                    gx - tx, y, gz - tz, pr, pg, pb, pulse * 0.55F);
        }
    }

    /**
     * Undulating 3D polar northern-light curtains rippling smoothly across the upper sky dome.
     */
    private static void renderAuroraCurtains(
            Matrix4f mat, VertexConsumer vc, double time, float alpha, float emerge,
            float pr, float pg, float pb, float sr, float sg, float sb) {
        int ribbons = 3;
        int segments = 40;
        float altitudeLift = (1.0F - emerge) * 12.0F;
        float curtainHeight = 24.0F * Mth.lerp(emerge, 0.35F, 1.0F);
        float lowerFeather = 3.5F * Mth.lerp(emerge, 1.35F, 1.0F);
        float halfSpan = 115.0F * Mth.lerp(emerge, 0.72F, 1.0F);
        float waveScale = Mth.lerp(emerge, 0.65F, 1.0F);

        for (int band = 0; band < ribbons; band++) {
            float baseZOffset = (band - 1) * 34.0F * Mth.lerp(emerge, 0.85F, 1.0F);
            float baseY = 68.0F + band * 8.0F + altitudeLift;
            double phase = band * 2.1D;

            for (int i = 0; i < segments; i++) {
                float t0 = (i / (float) segments) * 2.0F - 1.0F;
                float t1 = ((i + 1) / (float) segments) * 2.0F - 1.0F;
                float edgeFade0 = 1.0F - t0 * t0;
                float edgeFade1 = 1.0F - t1 * t1;

                float x0 = t0 * halfSpan;
                float x1 = t1 * halfSpan;
                float z0 = baseZOffset + (Mth.sin((float) (t0 * 4.5F + time * 0.7D + phase)) * 16.0F
                        + Mth.cos((float) (t0 * 9.0F - time * 0.45D)) * 6.0F) * waveScale;
                float z1 = baseZOffset + (Mth.sin((float) (t1 * 4.5F + time * 0.7D + phase)) * 16.0F
                        + Mth.cos((float) (t1 * 9.0F - time * 0.45D)) * 6.0F) * waveScale;

                float a0 = alpha * 0.40F * edgeFade0 * (0.7F + 0.3F * Mth.sin((float) (t0 * 7.0F + time * 1.5D + phase)));
                float a1 = alpha * 0.40F * edgeFade1 * (0.7F + 0.3F * Mth.sin((float) (t1 * 7.0F + time * 1.5D + phase)));

                // Soft lower fringe feather so curtains unfurl without a hard bottom polygon edge
                emitDoubleSidedQuad(mat, vc,
                        x0, baseY - lowerFeather, z0, pr, pg, pb, 0.0F,
                        x1, baseY - lowerFeather, z1, pr, pg, pb, 0.0F,
                        x1, baseY, z1, pr, pg, pb, a1,
                        x0, baseY, z0, pr, pg, pb, a0);
                // Main upward-fading aurora curtain
                emitDoubleSidedQuad(mat, vc,
                        x0, baseY, z0, pr, pg, pb, a0,
                        x1, baseY, z1, pr, pg, pb, a1,
                        x1, baseY + curtainHeight, z1, sr, sg, sb, 0.0F,
                        x0, baseY + curtainHeight, z0, sr, sg, sb, 0.0F);
            }
        }
    }

    /**
     * Celestial nebula arc & twinkling starlight glints overhead for Astral biomes.
     */
    private static void renderAstralStardustVeil(
            Matrix4f mat, VertexConsumer vc, double time, float alpha, float emerge,
            float pr, float pg, float pb, float sr, float sg, float sb) {
        int segments = 36;
        float radius = 92.0F * Mth.lerp(emerge, 0.88F, 1.0F);
        float domeHeight = 76.0F + (1.0F - emerge) * 8.0F;
        float arcSpan = 2.4F * Mth.lerp(emerge, 0.58F, 1.0F);
        float halfWidth = 9.0F * Mth.lerp(emerge, 0.50F, 1.0F);
        float crestLift = 2.0F * emerge;
        float waveAmp = 22.0F * Mth.lerp(emerge, 0.70F, 1.0F);

        for (int i = 0; i < segments; i++) {
            float u0 = (i / (float) segments) - 0.5F;
            float u1 = ((i + 1) / (float) segments) - 0.5F;
            float ang0 = u0 * arcSpan;
            float ang1 = u1 * arcSpan;
            float fade0 = Mth.cos(u0 * (float) Math.PI);
            float fade1 = Mth.cos(u1 * (float) Math.PI);

            float x0 = Mth.sin(ang0) * radius;
            float y0 = Mth.cos(ang0) * domeHeight;
            float z0 = Mth.sin(ang0 * 2.0F + (float) (time * 0.2D)) * waveAmp;

            float x1 = Mth.sin(ang1) * radius;
            float y1 = Mth.cos(ang1) * domeHeight;
            float z1 = Mth.sin(ang1 * 2.0F + (float) (time * 0.2D)) * waveAmp;

            float spineA0 = alpha * 0.34F * fade0;
            float spineA1 = alpha * 0.34F * fade1;

            emitDoubleSidedQuad(mat, vc,
                    x0, y0, z0 - halfWidth, pr, pg, pb, 0.0F,
                    x1, y1, z1 - halfWidth, pr, pg, pb, 0.0F,
                    x1, y1 + crestLift, z1, sr, sg, sb, spineA1,
                    x0, y0 + crestLift, z0, sr, sg, sb, spineA0);
            emitDoubleSidedQuad(mat, vc,
                    x0, y0 + crestLift, z0, sr, sg, sb, spineA0,
                    x1, y1 + crestLift, z1, sr, sg, sb, spineA1,
                    x1, y1, z1 + halfWidth, pr, pg, pb, 0.0F,
                    x0, y0, z0 + halfWidth, pr, pg, pb, 0.0F);
        }
    }

    /**
     * Glowing coronal eclipse halo high overhead for Eclipse / Penumbra / Silent Fjord biomes.
     */
    private static void renderEclipseCorona(
            Matrix4f mat, VertexConsumer vc, double time, float alpha, float emerge,
            float pr, float pg, float pb, float sr, float sg, float sb) {
        int segments = 40;
        float y = 82.0F + (1.0F - emerge) * 7.0F;
        float radialScale = Mth.lerp(emerge, 0.68F, 1.0F);
        float innerR = 14.0F * radialScale;
        float innerMistR = Math.max(0.0F, innerR - 2.5F * radialScale);
        float outerR = Mth.lerp(emerge, innerR + 5.5F, 26.0F);
        float flareAmp = 4.5F * emerge;
        float coronaAlpha = alpha * 0.55F;

        for (int i = 0; i < segments; i++) {
            float a0 = (float) (i * Math.PI * 2.0 / segments);
            float a1 = (float) ((i + 1) * Math.PI * 2.0 / segments);
            float flare0 = outerR + flareAmp * Mth.sin((float) (a0 * 7.0F + time * 1.2D));
            float flare1 = outerR + flareAmp * Mth.sin((float) (a1 * 7.0F + time * 1.2D));

            float c0 = Mth.cos(a0), s0 = Mth.sin(a0);
            float c1 = Mth.cos(a1), s1 = Mth.sin(a1);

            // Soft inner coronal rim feather around the umbral disc
            emitDoubleSidedQuad(mat, vc,
                    c0 * innerMistR, y + 0.3F, s0 * innerMistR, sr, sg, sb, 0.0F,
                    c1 * innerMistR, y + 0.3F, s1 * innerMistR, sr, sg, sb, 0.0F,
                    c1 * innerR, y, s1 * innerR, sr, sg, sb, coronaAlpha,
                    c0 * innerR, y, s0 * innerR, sr, sg, sb, coronaAlpha);
            // Outer solar/lunar coronal flare
            emitDoubleSidedQuad(mat, vc,
                    c0 * innerR, y, s0 * innerR, sr, sg, sb, coronaAlpha,
                    c1 * innerR, y, s1 * innerR, sr, sg, sb, coronaAlpha,
                    c1 * flare1, y - 1.0F, s1 * flare1, pr, pg, pb, 0.0F,
                    c0 * flare0, y - 1.0F, s0 * flare0, pr, pg, pb, 0.0F);
        }
    }

    /**
     * Jagged high-altitude dimensional violet rift seams for Null-U / Void-Cracked biomes.
     */
    private static void renderVoidRiftFractures(
            Matrix4f mat, VertexConsumer vc, double time, float alpha, float emerge,
            float pr, float pg, float pb, float sr, float sg, float sb) {
        float altitudeLift = (1.0F - emerge) * 8.0F;
        float halfLen = 24.0F * Mth.lerp(emerge, 0.28F, 1.0F);
        float maxHalfWidth = 2.2F * Mth.lerp(emerge, 0.35F, 1.0F);
        float jagScale = Mth.lerp(emerge, 0.45F, 1.0F);

        for (int crack = 0; crack < 3; crack++) {
            float baseAng = (float) (crack * 2.09D + 0.4D);
            float cx = Mth.cos(baseAng) * 52.0F;
            float cz = Mth.sin(baseAng) * 52.0F;
            float dirX = -Mth.sin(baseAng);
            float dirZ = Mth.cos(baseAng);
            float pulse = (0.65F + 0.35F * Mth.sin((float) (time * 1.8D + crack * 1.7D))) * alpha * 0.48F;
            float y = 74.0F + crack * 4.0F + altitudeLift;

            int segs = 10;
            for (int s = 0; s < segs; s++) {
                float u0 = (s / (float) segs) * 2.0F - 1.0F;
                float u1 = ((s + 1) / (float) segs) * 2.0F - 1.0F;
                float w0 = (1.0F - u0 * u0) * maxHalfWidth;
                float w1 = (1.0F - u1 * u1) * maxHalfWidth;
                float jag0 = Mth.sin(s * 2.3F + crack) * 2.5F * jagScale;
                float jag1 = Mth.sin((s + 1) * 2.3F + crack) * 2.5F * jagScale;

                float x0 = cx + dirX * (u0 * halfLen) + dirZ * jag0;
                float z0 = cz + dirZ * (u0 * halfLen) - dirX * jag0;
                float x1 = cx + dirX * (u1 * halfLen) + dirZ * jag1;
                float z1 = cz + dirZ * (u1 * halfLen) - dirX * jag1;

                float tipFade0 = 1.0F - u0 * u0;
                float tipFade1 = 1.0F - u1 * u1;

                emitDoubleSidedQuad(mat, vc,
                        x0, y - w0, z0, pr, pg, pb, 0.0F,
                        x1, y - w1, z1, pr, pg, pb, 0.0F,
                        x1, y, z1, sr, sg, sb, pulse * tipFade1,
                        x0, y, z0, sr, sg, sb, pulse * tipFade0);
                emitDoubleSidedQuad(mat, vc,
                        x0, y, z0, sr, sg, sb, pulse * tipFade0,
                        x1, y, z1, sr, sg, sb, pulse * tipFade1,
                        x1, y + w1, z1, pr, pg, pb, 0.0F,
                        x0, y + w0, z0, pr, pg, pb, 0.0F);
            }
        }
    }

    /**
     * Shimmering warm horizon heat-haze ribbons for Mirage biomes.
     */
    private static void renderMirageHeatShimmer(
            Matrix4f mat, VertexConsumer vc, double time, float alpha, float emerge,
            float pr, float pg, float pb, float sr, float sg, float sb) {
        int segments = 48;
        float radius = 84.0F * Mth.lerp(emerge, 0.94F, 1.0F);
        float baseY = Mth.lerp(emerge, 3.5F, 6.0F);
        float waveAmp = 2.2F * Mth.lerp(emerge, 0.35F, 1.0F);
        float halfH = 3.8F * Mth.lerp(emerge, 0.45F, 1.0F);

        for (int i = 0; i < segments; i++) {
            float a0 = (float) (i * Math.PI * 2.0 / segments);
            float a1 = (float) ((i + 1) * Math.PI * 2.0 / segments);
            float y0 = baseY + waveAmp * Mth.sin((float) (a0 * 8.0F + time * 2.4D));
            float y1 = baseY + waveAmp * Mth.sin((float) (a1 * 8.0F + time * 2.4D));
            float shimmer0 = alpha * 0.16F * (0.65F + 0.35F * Mth.cos((float) (a0 * 5.0F - time * 1.9D)));
            float shimmer1 = alpha * 0.16F * (0.65F + 0.35F * Mth.cos((float) (a1 * 5.0F - time * 1.9D)));

            float x0 = Mth.cos(a0) * radius, z0 = Mth.sin(a0) * radius;
            float x1 = Mth.cos(a1) * radius, z1 = Mth.sin(a1) * radius;

            // Lower heat-haze feather rising from horizon
            emitDoubleSidedQuad(mat, vc,
                    x0, y0 - halfH, z0, sr, sg, sb, 0.0F,
                    x1, y1 - halfH, z1, sr, sg, sb, 0.0F,
                    x1, y1, z1, pr, pg, pb, shimmer1,
                    x0, y0, z0, pr, pg, pb, shimmer0);
            // Upper heat-haze feather dissolving smoothly into sky
            emitDoubleSidedQuad(mat, vc,
                    x0, y0, z0, pr, pg, pb, shimmer0,
                    x1, y1, z1, pr, pg, pb, shimmer1,
                    x1, y1 + halfH, z1, sr, sg, sb, 0.0F,
                    x0, y0 + halfH, z0, sr, sg, sb, 0.0F);
        }
    }

    private static void emitDoubleSidedQuad(
            Matrix4f mat, VertexConsumer vc,
            float x0, float y0, float z0, float r0, float g0, float b0, float a0,
            float x1, float y1, float z1, float r1, float g1, float b1, float a1,
            float x2, float y2, float z2, float r2, float g2, float b2, float a2,
            float x3, float y3, float z3, float r3, float g3, float b3, float a3) {
        // Front face
        vertex(mat, vc, x0, y0, z0, r0, g0, b0, a0, 0.0F, 0.0F);
        vertex(mat, vc, x1, y1, z1, r1, g1, b1, a1, 1.0F, 0.0F);
        vertex(mat, vc, x2, y2, z2, r2, g2, b2, a2, 1.0F, 1.0F);
        vertex(mat, vc, x3, y3, z3, r3, g3, b3, a3, 0.0F, 1.0F);
        // Back face (visible looking upward from below)
        vertex(mat, vc, x3, y3, z3, r3, g3, b3, a3, 0.0F, 1.0F);
        vertex(mat, vc, x2, y2, z2, r2, g2, b2, a2, 1.0F, 1.0F);
        vertex(mat, vc, x1, y1, z1, r1, g1, b1, a1, 1.0F, 0.0F);
        vertex(mat, vc, x0, y0, z0, r0, g0, b0, a0, 0.0F, 0.0F);
    }

    private static void vertex(
            Matrix4f mat, VertexConsumer vc,
            float x, float y, float z,
            float r, float g, float b, float a,
            float u, float v) {
        vc.addVertex(mat, x, y, z)
                .setColor(Mth.clamp(r, 0.0F, 1.0F), Mth.clamp(g, 0.0F, 1.0F), Mth.clamp(b, 0.0F, 1.0F), Mth.clamp(a, 0.0F, 1.0F))
                .setUv(u, v)
                .setOverlay(NO_OVERLAY)
                .setLight(FULL_LIGHT)
                .setNormal(0.0F, -1.0F, 0.0F);
    }
}
