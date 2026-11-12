package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.spell.vfx.ArcaneWingsClientManager;
import ddraig.net.entropica.spell.vfx.SpellParticleHelper;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/**
 * Renders articulated 3D Materia-attuned Arcane Wings on players granted flight via {@code FLIGHT} spells
 * or Apex/Singularity Flight Shrouds. Wing coloration adapts dynamically to all 58 Materia types
 * (including dual-Materia primary + accent blends and prismatic/cosmic shifting essences).
 */
public class ArcaneWingsRenderLayer extends RenderLayer<AvatarRenderState, PlayerModel> {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private static final int FULL_LIGHT = 15728880;
    private static final int NO_OVERLAY = OverlayTexture.NO_OVERLAY;

    public ArcaneWingsRenderLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
        super(renderer);
    }

    @Override
    public void submit(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight,
            AvatarRenderState state,
            float yRot,
            float xRot
    ) {
        if (state.isInvisible || state.isSpectator) {
            return;
        }
        ArcaneWingsClientManager.WingInstance wing = ArcaneWingsClientManager.getActiveWingState(state.id);
        if (wing == null) {
            return;
        }

        float deploy = wing.getDeployProgress(Mth.frac(state.ageInTicks));
        if (deploy <= 0.01F) {
            return;
        }

        EssenceType primaryMat = wing.primaryMateria;
        EssenceType secondaryMat = wing.secondaryMateria;
        int tier = wing.potencyTier;

        double animTime = state.ageInTicks + (state.id * 5.0D);
        int primaryRgb = SpellParticleHelper.resolveVibrantPrimaryRgb(primaryMat, animTime);
        int accentRgb = SpellParticleHelper.resolveVibrantAccentRgb(primaryMat, secondaryMat, animTime);

        float pr = ((primaryRgb >> 16) & 0xFF) / 255.0F;
        float pg = ((primaryRgb >> 8) & 0xFF) / 255.0F;
        float pb = (primaryRgb & 0xFF) / 255.0F;

        float ar = ((accentRgb >> 16) & 0xFF) / 255.0F;
        float ag = ((accentRgb >> 8) & 0xFF) / 255.0F;
        float ab = (accentRgb & 0xFF) / 255.0F;

        // Motion reactivity from AvatarRenderState (capeLean rises smoothly with forward/flight velocity)
        float flightIntensity = Mth.clamp((state.capeLean / 55.0F) + (Math.abs(state.capeFlap) / 35.0F), 0.0F, 1.0F);
        if (state.isFallFlying) {
            flightIntensity = 1.0F;
        } else if (state.walkAnimationSpeed > 0.15F) {
            flightIntensity = Math.max(flightIntensity, Mth.clamp(state.walkAnimationSpeed * 0.65F, 0.0F, 0.75F));
        }

        float flapSpeed = Mth.lerp(flightIntensity, 0.11F, 0.34F);
        float flapWave = Mth.sin(state.ageInTicks * flapSpeed);
        float wristWave = Mth.sin(state.ageInTicks * flapSpeed - 0.65F);

        // Sweep angle back from shoulder (+Z is out the player's back; 0 = straight out to sides, higher = folded back)
        float baseSweepDeg = Mth.lerp(flightIntensity, 36.0F, 16.0F) + (1.0F - deploy) * 42.0F;
        float flapSwingDeg = Mth.lerp(flightIntensity, 7.5F, 26.0F) * flapWave;
        float shoulderSweepDeg = baseSweepDeg + flapSwingDeg;
        float dihedralRollDeg = Mth.lerp(flightIntensity, -10.0F, -18.0F) + (6.0F * flapWave);
        float wristSweepDeg = Mth.lerp(flightIntensity, 14.0F, 20.0F * wristWave + 6.0F);

        float wingSpanScale = (0.88F + (tier - 1) * 0.055F) * (0.25F + 0.75F * deploy);
        float alphaScale = Mth.clamp(deploy * 1.10F, 0.0F, 1.0F);
        boolean wearingChestArmor = state.chestEquipment != null && !state.chestEquipment.isEmpty();

        poseStack.pushPose();
        this.getParentModel().setupAnim(state);
        this.getParentModel().body.translateAndRotate(poseStack);

        // Anchor cleanly onto upper shoulder blades (+Y is down torso, +Z is out back)
        float backZ = wearingChestArmor ? 0.195F : 0.135F;
        poseStack.translate(0.0F, 0.155F, backZ);

        // 1. Central Dorsal Arcane Crest Ring between the shoulder blades
        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, vc) -> {
            renderDorsalSigilCrest(pose.pose(), vc, wingSpanScale, state.ageInTicks, pr, pg, pb, ar, ag, ab, alphaScale);
        });

        // 2. Articulated Left & Right Materia Wings
        for (int side = -1; side <= 1; side += 2) {
            final float s = (float) side;
            poseStack.pushPose();
            poseStack.translate(s * 0.075F, 0.0F, 0.015F);
            // Yaw outward to the side and sweep slightly backward toward +Z
            poseStack.mulPose(Axis.YP.rotationDegrees(-s * shoulderSweepDeg));
            // Dihedral upward tilt (-Y is up in HumanoidModel space)
            poseStack.mulPose(Axis.ZP.rotationDegrees(s * dihedralRollDeg));

            final float innerLen = 0.56F * wingSpanScale;
            final float outerLen = 0.74F * wingSpanScale;

            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, vc) -> {
                renderInnerWingSegment(pose.pose(), vc, s, innerLen, wingSpanScale, tier, state.ageInTicks,
                        pr, pg, pb, ar, ag, ab, alphaScale);
            });

            // Translate out to the mid-wing wrist joint & articulate outer primary flight pinions!
            poseStack.translate(s * innerLen, -0.10F * wingSpanScale, 0.03F * wingSpanScale);
            poseStack.mulPose(Axis.YP.rotationDegrees(-s * wristSweepDeg));
            poseStack.mulPose(Axis.ZP.rotationDegrees(s * (5.0F * wristWave)));

            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, vc) -> {
                renderOuterWingSegment(pose.pose(), vc, s, outerLen, wingSpanScale, tier, state.ageInTicks,
                        pr, pg, pb, ar, ag, ab, alphaScale);
            });

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static void renderDorsalSigilCrest(
            Matrix4f mat,
            VertexConsumer vc,
            float scale,
            float ageTicks,
            float pr, float pg, float pb,
            float ar, float ag, float ab,
            float alpha
    ) {
        float pulse = 0.92F + 0.08F * Mth.sin(ageTicks * 0.18F);
        float rOuter = 0.14F * scale * pulse;
        float rInner = 0.095F * scale * pulse;
        int segs = 12;
        float spin = ageTicks * 0.04F;

        for (int i = 0; i < segs; i++) {
            float a0 = spin + (Mth.TWO_PI * i / segs);
            float a1 = spin + (Mth.TWO_PI * (i + 1) / segs);
            float c0 = Mth.cos(a0), s0 = Mth.sin(a0);
            float c1 = Mth.cos(a1), s1 = Mth.sin(a1);

            emitDoubleQuad(mat, vc,
                    c0 * rInner, s0 * rInner, 0.01F,
                    c1 * rInner, s1 * rInner, 0.01F,
                    c1 * rOuter, s1 * rOuter, 0.015F,
                    c0 * rOuter, s0 * rOuter, 0.015F,
                    ar, ag, ab, 0.78F * alpha);
        }

        // Center diamond core gem
        float d = 0.065F * scale;
        emitDoubleQuad(mat, vc,
                0.0F, -d * 1.35F, 0.025F,
                d, 0.0F, 0.025F,
                0.0F, d * 1.35F, 0.025F,
                -d, 0.0F, 0.025F,
                Mth.lerp(0.45F, pr, 1.0F), Mth.lerp(0.45F, pg, 1.0F), Mth.lerp(0.45F, pb, 1.0F), 0.92F * alpha);
    }

    private static void renderInnerWingSegment(
            Matrix4f mat,
            VertexConsumer vc,
            float side,
            float innerLen,
            float scale,
            int tier,
            float ageTicks,
            float pr, float pg, float pb,
            float ar, float ag, float ab,
            float alpha
    ) {
        float endX = side * innerLen;
        float endY = -0.10F * scale;
        float endZ = 0.03F * scale;

        // 1. Upper crystalline leading spar (shoulder -> wrist)
        draw3DSpar(mat, vc,
                0.0F, 0.0F, 0.0F,
                endX, endY, endZ,
                0.028F * scale, 0.022F * scale,
                Mth.lerp(0.35F, ar, 1.0F), Mth.lerp(0.35F, ag, 1.0F), Mth.lerp(0.35F, ab, 1.0F), 0.92F * alpha);

        // 2. Soft gossamer arcane membrane veil under the inner spar
        emitDoubleQuad(mat, vc,
                0.0F, 0.02F * scale, 0.0F,
                endX, endY + 0.02F * scale, endZ,
                endX * 0.92F, endY + 0.34F * scale, endZ + 0.04F * scale,
                side * 0.05F * scale, 0.30F * scale, 0.03F * scale,
                pr, pg, pb, 0.34F * alpha);

        // 3. Five layered inner secondary feathers fanning downward (+Y is down!)
        int featherCount = 5;
        for (int i = 0; i < featherCount; i++) {
            float t = (i + 0.5F) / featherCount;
            float rootX = Mth.lerp(t, side * 0.04F * scale, endX * 0.96F);
            float rootY = Mth.lerp(t, 0.02F * scale, endY + 0.02F * scale);
            float rootZ = Mth.lerp(t, 0.0F, endZ) + (i % 2 == 0 ? 0.006F : -0.006F);

            float featherLen = (0.36F + t * 0.22F) * scale;
            float tipX = rootX + side * (0.08F + t * 0.12F) * scale;
            float tipY = rootY + featherLen;
            float tipZ = rootZ + 0.05F * scale;

            drawFeatherBlade(mat, vc,
                    rootX, rootY, rootZ,
                    tipX, tipY, tipZ,
                    0.048F * scale,
                    pr, pg, pb,
                    ar, ag, ab,
                    alpha);
        }

        // High-tier dorsal shoulder plume (Tier 5+)
        if (tier >= 5) {
            drawFeatherBlade(mat, vc,
                    side * 0.06F * scale, -0.02F * scale, 0.02F * scale,
                    side * 0.32F * scale, -0.22F * scale, 0.08F * scale,
                    0.042F * scale,
                    ar, ag, ab,
                    1.0F, 1.0F, 1.0F,
                    0.85F * alpha);
        }
    }

    private static void renderOuterWingSegment(
            Matrix4f mat,
            VertexConsumer vc,
            float side,
            float outerLen,
            float scale,
            int tier,
            float ageTicks,
            float pr, float pg, float pb,
            float ar, float ag, float ab,
            float alpha
    ) {
        // Wrist claw / star node
        float c = 0.045F * scale;
        emitDoubleQuad(mat, vc,
                0.0F, -c, 0.01F,
                side * c * 1.4F, 0.0F, 0.01F,
                0.0F, c, 0.01F,
                -side * c * 0.7F, 0.0F, 0.01F,
                1.0F, 1.0F, 1.0F, 0.90F * alpha);

        float tipSparX = side * outerLen;
        float tipSparY = -0.14F * scale;
        float tipSparZ = 0.05F * scale;

        // 1. Outer primary leading spar
        draw3DSpar(mat, vc,
                0.0F, 0.0F, 0.0F,
                tipSparX, tipSparY, tipSparZ,
                0.022F * scale, 0.008F * scale,
                Mth.lerp(0.45F, ar, 1.0F), Mth.lerp(0.45F, ag, 1.0F), Mth.lerp(0.45F, ab, 1.0F), 0.90F * alpha);

        // 2. Outer translucent energy membrane
        emitDoubleQuad(mat, vc,
                0.0F, 0.015F * scale, 0.0F,
                tipSparX * 0.92F, tipSparY + 0.02F * scale, tipSparZ,
                side * outerLen * 0.62F, 0.36F * scale, 0.08F * scale,
                0.0F, 0.32F * scale, 0.04F * scale,
                pr, pg, pb, 0.30F * alpha);

        // 3. Swept Primary Flight Pinions (5 to 7 long radiant blades fanning from horizontal wingtip down to mid-trailing edge)
        int pinionCount = tier >= 7 ? 7 : (tier >= 4 ? 6 : 5);
        for (int i = 0; i < pinionCount; i++) {
            float f = i / (float) (pinionCount - 1); // 0 = topmost longest wingtip pinion, 1 = innermost wrist pinion
            float rootX = Mth.lerp(1.0F - f, 0.02F * side * scale, tipSparX * 0.68F);
            float rootY = Mth.lerp(1.0F - f, 0.02F * scale, tipSparY * 0.68F);
            float rootZ = Mth.lerp(1.0F - f, 0.01F * scale, tipSparZ * 0.68F) + ((i & 1) == 0 ? 0.005F : -0.005F);

            float reachOut = Mth.lerp(f, outerLen * 1.18F, outerLen * 0.42F);
            float dropDown = Mth.lerp(f, -0.08F * scale, 0.56F * scale);
            float sweepBack = Mth.lerp(f, 0.09F * scale, 0.06F * scale);

            float flutter = 0.018F * scale * Mth.sin(ageTicks * 0.28F + i * 0.9F);
            float tipX = side * reachOut;
            float tipY = dropDown;
            float tipZ = sweepBack + flutter;

            drawFeatherBlade(mat, vc,
                    rootX, rootY, rootZ,
                    tipX, tipY, tipZ,
                    (0.056F - f * 0.010F) * scale,
                    pr, pg, pb,
                    ar, ag, ab,
                    alpha);
        }

        // Tier 6+ Ethereal Floating Halo Pinions trailing behind the wingtips
        if (tier >= 6) {
            int haloCount = tier >= 9 ? 3 : 2;
            for (int h = 0; h < haloCount; h++) {
                float hf = h / (float) Math.max(1, haloCount - 1);
                float hx0 = side * (outerLen * (0.55F + hf * 0.22F));
                float hy0 = (-0.02F + hf * 0.20F) * scale;
                float hz0 = 0.09F * scale;
                float hx1 = hx0 + side * 0.34F * scale;
                float hy1 = hy0 + (0.06F + hf * 0.14F) * scale;
                float hz1 = hz0 + 0.06F * scale;

                drawFeatherBlade(mat, vc,
                        hx0, hy0, hz0,
                        hx1, hy1, hz1,
                        0.036F * scale,
                        ar, ag, ab,
                        1.0F, 1.0F, 1.0F,
                        0.72F * alpha);
            }
        }
    }

    /**
     * Draws a 3D double-sided crystalline feather blade with a bright central rachis spine,
     * primary Materia inner vane, and luminous accent Materia tip.
     */
    private static void drawFeatherBlade(
            Matrix4f mat,
            VertexConsumer vc,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float halfWidth,
            float pr, float pg, float pb,
            float ar, float ag, float ab,
            float alpha
    ) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        float dz = z1 - z0;
        float len = Mth.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1.0E-4F) {
            return;
        }

        // Perpendicular width axis in the X-Y wing plane
        float px = -dy / len;
        float py = dx / len;
        float pLen = Mth.sqrt(px * px + py * py);
        if (pLen < 1.0E-4F) {
            px = 1.0F;
            py = 0.0F;
        } else {
            px /= pLen;
            py /= pLen;
        }

        // Midpoint at 68% along blade where the feather reaches peak width before tapering to a sharp point
        float mx = Mth.lerp(0.68F, x0, x1);
        float my = Mth.lerp(0.68F, y0, y1);
        float mz = Mth.lerp(0.68F, z0, z1);

        float rootW = halfWidth * 0.42F;
        float midW = halfWidth;

        // Outer translucent Materia feather vane (root -> widest 68% -> sharp accent tip)
        emitDoubleQuad(mat, vc,
                x0 - px * rootW, y0 - py * rootW, z0,
                x0 + px * rootW, y0 + py * rootW, z0,
                mx + px * midW, my + py * midW, mz,
                mx - px * midW, my - py * midW, mz,
                pr, pg, pb, 0.72F * alpha);

        emitDoubleQuad(mat, vc,
                mx - px * midW, my - py * midW, mz,
                mx + px * midW, my + py * midW, mz,
                x1, y1, z1,
                x1, y1, z1,
                ar, ag, ab, 0.86F * alpha);

        // Luminous narrow central rachis quill spine (slight Z thickness so wings read in 3D from side angles too!)
        float coreW = halfWidth * 0.22F;
        float coreR = Mth.lerp(0.55F, ar, 1.0F);
        float coreG = Mth.lerp(0.55F, ag, 1.0F);
        float coreB = Mth.lerp(0.55F, ab, 1.0F);
        emitDoubleQuad(mat, vc,
                x0 - px * coreW, y0 - py * coreW, z0,
                x0 + px * coreW, y0 + py * coreW, z0,
                x1, y1, z1,
                x1, y1, z1,
                coreR, coreG, coreB, 0.94F * alpha);

        // Perpendicular Z-depth fin along the rachis so viewing the player strictly from profile still shows 3D glowing volume!
        float depthZ = halfWidth * 0.28F;
        emitDoubleQuad(mat, vc,
                x0, y0, z0 - depthZ,
                x0, y0, z0 + depthZ,
                x1, y1, z1,
                x1, y1, z1,
                ar, ag, ab, 0.65F * alpha);
    }

    private static void draw3DSpar(
            Matrix4f mat,
            VertexConsumer vc,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float r0, float r1,
            float cr, float cg, float cb, float ca
    ) {
        // Crossed XY + XZ quads form a crisp 3D beveled beam spar from every camera angle
        emitDoubleQuad(mat, vc,
                x0, y0 - r0, z0,
                x0, y0 + r0, z0,
                x1, y1 + r1, z1,
                x1, y1 - r1, z1,
                cr, cg, cb, ca);
        emitDoubleQuad(mat, vc,
                x0, y0, z0 - r0,
                x0, y0, z0 + r0,
                x1, y1, z1 + r1,
                x1, y1, z1 - r1,
                cr, cg, cb, ca * 0.88F);
    }

    private static void emitDoubleQuad(
            Matrix4f mat,
            VertexConsumer vc,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float r, float g, float b, float a
    ) {
        if (a <= 0.005F) return;
        // Front face
        emitVertex(mat, vc, x0, y0, z0, 0.0F, 0.0F, r, g, b, a, 0.0F, 0.0F, 1.0F);
        emitVertex(mat, vc, x1, y1, z1, 1.0F, 0.0F, r, g, b, a, 0.0F, 0.0F, 1.0F);
        emitVertex(mat, vc, x2, y2, z2, 1.0F, 1.0F, r, g, b, a, 0.0F, 0.0F, 1.0F);
        emitVertex(mat, vc, x3, y3, z3, 0.0F, 1.0F, r, g, b, a, 0.0F, 0.0F, 1.0F);
        // Back face (reverse winding so wings shine brightly from both front & behind!)
        emitVertex(mat, vc, x3, y3, z3, 0.0F, 1.0F, r, g, b, a, 0.0F, 0.0F, -1.0F);
        emitVertex(mat, vc, x2, y2, z2, 1.0F, 1.0F, r, g, b, a, 0.0F, 0.0F, -1.0F);
        emitVertex(mat, vc, x1, y1, z1, 1.0F, 0.0F, r, g, b, a, 0.0F, 0.0F, -1.0F);
        emitVertex(mat, vc, x0, y0, z0, 0.0F, 0.0F, r, g, b, a, 0.0F, 0.0F, -1.0F);
    }

    private static void emitVertex(
            Matrix4f mat,
            VertexConsumer vc,
            float x, float y, float z,
            float u, float v,
            float r, float g, float b, float a,
            float nx, float ny, float nz
    ) {
        vc.addVertex(mat, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(NO_OVERLAY)
                .setLight(FULL_LIGHT)
                .setNormal(nx, ny, nz);
    }
}
