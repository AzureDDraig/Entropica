package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.network.SpellVfxEventPayload;
import ddraig.net.entropica.registry.AestheticGlassRegistry;
import ddraig.net.entropica.spell.vfx.SpellParticleHelper;
import ddraig.net.entropica.spell.vfx.SpellWorldVfxManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

/**
 * Custom 3D procedural world-space spell VFX renderer.
 * Renders multi-layered emissive beams, chain-lightning arcs, shockwave domes,
 * rotating arcane seals, crescent slashes, vortex cones, orbital skyfall pillars,
 * dimensional blink rifts, ward shells, and summoning ritual columns at full frame rate.
 */
public final class SpellWorldVfxRenderer {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private static final int FULL_LIGHT = 15728880;
    private static final int NO_OVERLAY = OverlayTexture.NO_OVERLAY;

    private SpellWorldVfxRenderer() {
    }

    public static void renderWorldVfx(PoseStack poseStack, Matrix4f projOrModelView, Camera camera, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }

        List<SpellWorldVfxManager.ActiveVfxInstance> events = SpellWorldVfxManager.snapshotClientEvents();
        if (events.isEmpty()) {
            return;
        }

        Vec3 camPos = camera.getPosition();
        double gameTime = mc.level.getGameTime() + partialTick;

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        RenderType emissiveType = RenderType.entityTranslucentEmissive(WHITE_TEXTURE);
        RenderType solidBlackType = RenderType.entitySolid(WHITE_TEXTURE);

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
        Matrix4f matrix = poseStack.last().pose();

        // Pass 1: Pitch-black event-horizon cores for Void / Eschaton / Singularity beams & rifts
        boolean drewSolid = false;
        for (SpellWorldVfxManager.ActiveVfxInstance inst : events) {
            if (isDarkSingularityMateria(inst.essence)) {
                float progress = Mth.clamp((inst.ageTicks + partialTick) / (float) inst.maxTicks, 0.0F, 1.0F);
                if (inst.vfxType == SpellVfxEventPayload.VFX_BEAM) {
                    VertexConsumer solidVc = bufferSource.getBuffer(solidBlackType);
                    float fade = computeEnvelope(progress, 0.12F, 0.55F);
                    renderTube(matrix, solidVc, inst.start, inst.endOrDir, inst.radiusOrScale * 0.42F * fade, 8,
                            0.02F, 0.01F, 0.03F, 1.0F, 0);
                    drewSolid = true;
                } else if (inst.vfxType == SpellVfxEventPayload.VFX_BLINK_RIFT) {
                    VertexConsumer solidVc = bufferSource.getBuffer(solidBlackType);
                    float fade = computeEnvelope(progress, 0.15F, 0.50F);
                    renderVerticalDiamondPortalFill(matrix, solidVc, inst.start.add(0.0D, 1.0D, 0.0D),
                            camera, 0.34F * fade, 0.95F * fade, 0.01F, 0.0F, 0.02F, 1.0F, 0);
                    renderVerticalDiamondPortalFill(matrix, solidVc, inst.endOrDir.add(0.0D, 1.0D, 0.0D),
                            camera, 0.34F * fade, 0.95F * fade, 0.01F, 0.0F, 0.02F, 1.0F, 0);
                    drewSolid = true;
                }
            }
        }
        if (drewSolid) {
            bufferSource.endBatch(solidBlackType);
        }

        // Pass 2: Translucent-Emissive 3D Procedural Geometry
        VertexConsumer vc = bufferSource.getBuffer(emissiveType);
        for (SpellWorldVfxManager.ActiveVfxInstance inst : events) {
            float progress = Mth.clamp((inst.ageTicks + partialTick) / (float) inst.maxTicks, 0.0F, 1.0F);
            int pRgb = inst.essence.isDynamic()
                    ? SpellParticleHelper.resolveVibrantPrimaryRgb(inst.essence, gameTime + inst.seed * 5)
                    : inst.primaryRgb;
            int aRgb = inst.essence.isDynamic()
                    ? SpellParticleHelper.resolveVibrantAccentRgb(inst.essence, null, gameTime + inst.seed * 5)
                    : inst.accentRgb;

            float pr = ((pRgb >> 16) & 0xFF) / 255.0F;
            float pg = ((pRgb >> 8) & 0xFF) / 255.0F;
            float pb = (pRgb & 0xFF) / 255.0F;

            float ar = ((aRgb >> 16) & 0xFF) / 255.0F;
            float ag = ((aRgb >> 8) & 0xFF) / 255.0F;
            float ab = (aRgb & 0xFF) / 255.0F;

            switch (inst.vfxType) {
                case SpellVfxEventPayload.VFX_BEAM ->
                        renderBeam(matrix, vc, inst, progress, (float) gameTime, pr, pg, pb, ar, ag, ab);
                case SpellVfxEventPayload.VFX_CHAIN_ARC ->
                        renderChainArc(matrix, vc, inst, progress, (float) gameTime, pr, pg, pb, ar, ag, ab);
                case SpellVfxEventPayload.VFX_RADIAL_NOVA ->
                        renderRadialNova(matrix, vc, inst, progress, (float) gameTime, pr, pg, pb, ar, ag, ab);
                case SpellVfxEventPayload.VFX_PROJECTILE_IMPACT ->
                        renderProjectileImpact(matrix, vc, inst, progress, pr, pg, pb, ar, ag, ab);
                case SpellVfxEventPayload.VFX_ARCANE_SEAL ->
                        renderArcaneSeal(matrix, vc, inst, progress, (float) gameTime, pr, pg, pb, ar, ag, ab);
                case SpellVfxEventPayload.VFX_CRESCENT_SLASH ->
                        renderCrescentSlash(matrix, vc, inst, progress, pr, pg, pb, ar, ag, ab);
                case SpellVfxEventPayload.VFX_VORTEX_CONE ->
                        renderVortexCone(matrix, vc, inst, progress, (float) gameTime, pr, pg, pb, ar, ag, ab);
                case SpellVfxEventPayload.VFX_SKYFALL_PILLAR ->
                        renderSkyfallPillar(matrix, vc, inst, progress, (float) gameTime, pr, pg, pb, ar, ag, ab);
                case SpellVfxEventPayload.VFX_BLINK_RIFT ->
                        renderBlinkRift(matrix, vc, camera, inst, progress, pr, pg, pb, ar, ag, ab);
                case SpellVfxEventPayload.VFX_WARD_SHELL ->
                        renderWardShell(matrix, vc, inst, progress, (float) gameTime, pr, pg, pb, ar, ag, ab);
                case SpellVfxEventPayload.VFX_SUMMON_PILLAR ->
                        renderSummonPillar(matrix, vc, inst, progress, (float) gameTime, pr, pg, pb, ar, ag, ab);
                default -> {
                }
            }
        }

        poseStack.popPose();
        bufferSource.endBatch(emissiveType);
    }

    // =========================================================================
    // 1. VOLUMETRIC 3D BEAM
    // =========================================================================

    private static void renderBeam(
            Matrix4f matrix,
            VertexConsumer vc,
            SpellWorldVfxManager.ActiveVfxInstance inst,
            float progress,
            float gameTime,
            float pr, float pg, float pb,
            float ar, float ag, float ab
    ) {
        Vec3 span = inst.endOrDir.subtract(inst.start);
        double len = span.length();
        if (len < 0.08D) return;

        float fade = computeEnvelope(progress, 0.12F, 0.45F);
        float pulse = 1.0F + 0.14F * Mth.sin(gameTime * 0.85F + inst.seed);
        float baseR = inst.radiusOrScale * pulse;

        boolean darkCore = isDarkSingularityMateria(inst.essence);

        // Inner white-hot (or photon-ring) core cylinder
        if (!darkCore) {
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.38F * fade, 8,
                    0.98F, 0.99F, 1.0F, 0.92F * fade, FULL_LIGHT);
        } else {
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.56F * fade, 8,
                    ar, ag, ab, 0.88F * fade, FULL_LIGHT);
        }

        // Outer Materia plasma sheath
        renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.15F * fade, 10,
                pr, pg, pb, 0.46F * fade, FULL_LIGHT);

        // Dual counter-rotating 3D helical energy ribbons spiraling down the beam
        Vec3 fwd = span.normalize();
        Vec3 right = buildPerpendicularRight(fwd);
        Vec3 up = right.cross(fwd).normalize();

        int strandCount = (inst.essence == EssenceType.PRISMATIC || inst.essence == EssenceType.ENTROPICA) ? 3 : 2;
        int steps = Math.clamp((int) (len * 3.5D), 6, 64);
        float helixRadius = baseR * 1.65F * fade;
        float spinPhase = -gameTime * 0.45F + inst.seed;

        for (int s = 0; s < strandCount; s++) {
            float strandOffset = (float) (Math.PI * 2.0D * s / strandCount);
            float dirSign = (s % 2 == 0) ? 1.0F : -1.0F;

            float sr = (s == 0) ? pr : ar;
            float sg = (s == 0) ? pg : ag;
            float sb = (s == 0) ? pb : ab;
            if (inst.essence == EssenceType.PRISMATIC) {
                int rgb = AestheticGlassRegistry.hsbToRgb(((s / 3.0F) + gameTime * 0.03F) % 1.0F, 0.78F, 1.0F);
                sr = ((rgb >> 16) & 0xFF) / 255.0F;
                sg = ((rgb >> 8) & 0xFF) / 255.0F;
                sb = (rgb & 0xFF) / 255.0F;
            }

            Vec3 prevPt = null;
            for (int i = 0; i <= steps; i++) {
                float t = (float) i / steps;
                double dist = t * len;
                float ang = dirSign * ((float) dist * 1.85F + spinPhase) + strandOffset;
                Vec3 radial = right.scale(Mth.cos(ang) * helixRadius).add(up.scale(Mth.sin(ang) * helixRadius));
                Vec3 pt = inst.start.add(fwd.scale(dist)).add(radial);
                if (prevPt != null) {
                    renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.24F * fade, sr, sg, sb, 0.78F * fade);
                }
                prevPt = pt;
            }
        }

        // Muzzle arcane ring & impact shock-ring perpendicular to beam
        float muzzleRot = gameTime * 0.12F;
        renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(0.08D)), right, up,
                baseR * 1.8F, baseR * 2.5F, 16, ar, ag, ab, 0.80F * fade);
        renderOrientedStarPolygon(matrix, vc, inst.start.add(fwd.scale(0.09D)), right, up,
                baseR * 2.2F, 6, muzzleRot, 1.0F, 1.0F, 1.0F, 0.72F * fade);

        float impactExpand = 1.0F + progress * 1.6F;
        renderOrientedRing(matrix, vc, inst.endOrDir.subtract(fwd.scale(0.05D)), right, up,
                baseR * 1.4F * impactExpand, baseR * 2.4F * impactExpand, 18,
                pr, pg, pb, 0.82F * (1.0F - progress));
    }

    // =========================================================================
    // 2. JAGGED 3D CHAIN ARC / LIGHTNING TETHER
    // =========================================================================

    private static void renderChainArc(
            Matrix4f matrix,
            VertexConsumer vc,
            SpellWorldVfxManager.ActiveVfxInstance inst,
            float progress,
            float gameTime,
            float pr, float pg, float pb,
            float ar, float ag, float ab
    ) {
        Vec3 span = inst.endOrDir.subtract(inst.start);
        double len = span.length();
        if (len < 0.1D) return;

        float fade = 1.0F - progress * progress;
        Vec3 fwd = span.normalize();
        Vec3 right = buildPerpendicularRight(fwd);
        Vec3 up = right.cross(fwd).normalize();

        int segs = Math.clamp((int) (len * 2.4D), 6, 18);
        int flickerFrame = (int) (gameTime * 0.75F) + inst.seed * 17;
        float maxJitter = (float) Math.min(0.52D, len * 0.11D);

        Vec3 prev = inst.start;
        for (int i = 1; i <= segs; i++) {
            float t = (float) i / segs;
            Vec3 base = inst.start.add(span.scale(t));
            Vec3 curr;
            if (i == segs) {
                curr = inst.endOrDir;
            } else {
                float envelope = Mth.sin(t * (float) Math.PI);
                float jR = (hashFloat(flickerFrame, i * 3) - 0.5F) * 2.0F * maxJitter * envelope;
                float jU = (hashFloat(flickerFrame, i * 3 + 1) - 0.5F) * 2.0F * maxJitter * envelope;
                curr = base.add(right.scale(jR)).add(up.scale(jU));
            }

            // Outer colored plasma bloom + inner white-hot filament
            renderCrossSegment(matrix, vc, prev, curr, 0.11F * fade, pr, pg, pb, 0.62F * fade);
            renderCrossSegment(matrix, vc, prev, curr, 0.042F * fade, 1.0F, 1.0F, 1.0F, 0.95F * fade);

            // Secondary fork branch from midpoint
            if (i == segs / 2 && len > 1.8D) {
                float fR = (hashFloat(flickerFrame, 99) - 0.5F) * maxJitter * 2.2F;
                float fU = (hashFloat(flickerFrame, 101) - 0.5F) * maxJitter * 2.2F;
                Vec3 forkTip = curr.add(fwd.scale(len * 0.22D)).add(right.scale(fR)).add(up.scale(fU));
                renderCrossSegment(matrix, vc, curr, forkTip, 0.065F * fade, ar, ag, ab, 0.55F * fade);
            }

            prev = curr;
        }
    }

    // =========================================================================
    // 3. EXPANDING 3D RADIAL NOVA SHOCKWAVE DOME & GROUND RING
    // =========================================================================

    private static void renderRadialNova(
            Matrix4f matrix,
            VertexConsumer vc,
            SpellWorldVfxManager.ActiveVfxInstance inst,
            float progress,
            float gameTime,
            float pr, float pg, float pb,
            float ar, float ag, float ab
    ) {
        float eased = 1.0F - (1.0F - progress) * (1.0F - progress) * (1.0F - progress);
        float fade = (1.0F - progress) * (1.0F - progress * 0.35F);
        float curRadius = inst.radiusOrScale * (0.18F + 0.85F * eased);

        // 1. Expanding 3D hemispherical energy dome
        renderHemisphereShell(matrix, vc, inst.start, curRadius, 8, 22, pr, pg, pb, 0.36F * fade);

        // 2. Crisp expanding double shock-ring near ground level
        Vec3 ground = inst.start.add(0.0D, 0.08D, 0.0D);
        Vec3 right = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 forward = new Vec3(0.0D, 0.0D, 1.0D);
        float ringThick = Math.max(0.18F, curRadius * 0.12F);
        renderOrientedRing(matrix, vc, ground, right, forward,
                Math.max(0.05F, curRadius - ringThick), curRadius + ringThick * 0.35F, 28,
                ar, ag, ab, 0.84F * fade);
        renderOrientedRing(matrix, vc, ground.add(0.0D, 0.02D, 0.0D), right, forward,
                Math.max(0.02F, curRadius - ringThick * 0.45F), curRadius, 28,
                1.0F, 1.0F, 1.0F, 0.72F * fade);

        // 3. Rotating inscribed star seal inside the blast
        renderOrientedStarPolygon(matrix, vc, ground.add(0.0D, 0.03D, 0.0D), right, forward,
                curRadius * 0.78F, 6, gameTime * 0.08F + inst.seed, pr, pg, pb, 0.62F * fade);
    }

    // =========================================================================
    // 4. PROJECTILE IMPACT SHOCK-RING & STARBURST
    // =========================================================================

    private static void renderProjectileImpact(
            Matrix4f matrix,
            VertexConsumer vc,
            SpellWorldVfxManager.ActiveVfxInstance inst,
            float progress,
            float pr, float pg, float pb,
            float ar, float ag, float ab
    ) {
        float eased = 1.0F - (1.0F - progress) * (1.0F - progress);
        float fade = 1.0F - progress;
        float maxR = inst.radiusOrScale * (0.95F + inst.tier * 0.08F);
        float curR = maxR * (0.15F + 0.85F * eased);

        Vec3 normal = inst.endOrDir.lengthSqr() > 1.0E-4D ? inst.endOrDir.normalize() : new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = buildPerpendicularRight(normal);
        Vec3 up = right.cross(normal).normalize();

        // Primary oriented shockwave ring
        renderOrientedRing(matrix, vc, inst.start.add(normal.scale(0.05D)), right, up,
                curR * 0.70F, curR, 20, pr, pg, pb, 0.85F * fade);
        renderOrientedRing(matrix, vc, inst.start.add(normal.scale(0.06D)), right, up,
                curR * 0.80F, curR * 0.94F, 20, 1.0F, 1.0F, 1.0F, 0.78F * fade);

        // Flash core octahedron during first half of impact
        if (progress < 0.65F) {
            float flashFade = 1.0F - (progress / 0.65F);
            renderOctahedron(matrix, vc, inst.start, maxR * 0.34F * flashFade, 1.0F, 1.0F, 1.0F, 0.92F * flashFade);
            renderOctahedron(matrix, vc, inst.start, maxR * 0.52F * flashFade, ar, ag, ab, 0.55F * flashFade);
        }

        // 6 outward starburst rays
        for (int i = 0; i < 6; i++) {
            float ang = (float) (Math.PI * 2.0D * i / 6.0D) + inst.seed;
            Vec3 rayDir = right.scale(Mth.cos(ang)).add(up.scale(Mth.sin(ang))).add(normal.scale(0.32D)).normalize();
            Vec3 p0 = inst.start.add(rayDir.scale(curR * 0.20D));
            Vec3 p1 = inst.start.add(rayDir.scale(curR * 1.18D));
            renderCrossSegment(matrix, vc, p0, p1, 0.045F * fade, ar, ag, ab, 0.80F * fade);
        }
    }

    // =========================================================================
    // 5. ROTATING 3D ARCANE MAGIC SEAL (GLYPHS & LINGERING ZONES)
    // =========================================================================

    private static void renderArcaneSeal(
            Matrix4f matrix,
            VertexConsumer vc,
            SpellWorldVfxManager.ActiveVfxInstance inst,
            float progress,
            float gameTime,
            float pr, float pg, float pb,
            float ar, float ag, float ab
    ) {
        float fade = computeEnvelope(progress, 0.10F, 0.18F);
        float r = inst.radiusOrScale;
        Vec3 base = inst.start.add(0.0D, 0.05D, 0.0D);
        Vec3 right = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 forward = new Vec3(0.0D, 0.0D, 1.0D);

        float rotOuter = gameTime * 0.035F + inst.seed;
        float rotInner = -gameTime * 0.055F - inst.seed;
        float pulse = 0.85F + 0.15F * Mth.sin(gameTime * 0.18F + inst.seed);

        // Outer & Inner Runic Rings
        renderOrientedRing(matrix, vc, base, right, forward,
                r * 0.93F, r, 32, pr, pg, pb, 0.72F * fade * pulse);
        renderOrientedRing(matrix, vc, base.add(0.0D, 0.01D, 0.0D), right, forward,
                r * 0.66F, r * 0.71F, 28, ar, ag, ab, 0.68F * fade * pulse);

        // Counter-rotating Hexagram / Octagram Star Geometry
        int points = inst.tier >= 6 ? 8 : 6;
        renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.015D, 0.0D), right, forward,
                r * 0.93F, points, rotOuter, ar, ag, ab, 0.65F * fade);
        renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.02D, 0.0D), right, forward,
                r * 0.67F, points, rotInner, 1.0F, 1.0F, 1.0F, 0.55F * fade);

        // Soft vertical rising mana curtain around perimeter
        renderVerticalAuraCurtain(matrix, vc, base, r * 0.97F, 0.48F + 0.12F * pulse, 24,
                pr, pg, pb, 0.28F * fade);
    }

    // =========================================================================
    // 6. SWEEPING 3D CRESCENT SLASH (TOUCH / STRIKE)
    // =========================================================================

    private static void renderCrescentSlash(
            Matrix4f matrix,
            VertexConsumer vc,
            SpellWorldVfxManager.ActiveVfxInstance inst,
            float progress,
            float pr, float pg, float pb,
            float ar, float ag, float ab
    ) {
        Vec3 fwd = inst.endOrDir.lengthSqr() > 1.0E-4D ? inst.endOrDir.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
        Vec3 flatRight = buildPerpendicularRight(fwd);
        Vec3 flatUp = flatRight.cross(fwd).normalize();

        // Diagonal 24-degree cleave plane
        float tilt = (float) Math.toRadians((inst.seed % 2 == 0) ? 24.0D : -24.0D);
        Vec3 planeRight = flatRight.scale(Mth.cos(tilt)).add(flatUp.scale(Mth.sin(tilt))).normalize();

        float fade = 1.0F - progress * progress;
        float reach = inst.radiusOrScale * (0.82F + 0.25F * progress);
        int segs = 18;
        float totalArc = (float) Math.toRadians(145.0D);
        float sweepShift = (progress - 0.5F) * 0.55F;

        for (int i = 0; i < segs; i++) {
            float t0 = (float) i / segs;
            float t1 = (float) (i + 1) / segs;
            float a0 = (t0 - 0.5F) * totalArc + sweepShift;
            float a1 = (t1 - 0.5F) * totalArc + sweepShift;

            float prof0 = Mth.sin(t0 * (float) Math.PI);
            float prof1 = Mth.sin(t1 * (float) Math.PI);

            Vec3 dir0 = fwd.scale(Mth.cos(a0)).add(planeRight.scale(Mth.sin(a0))).normalize();
            Vec3 dir1 = fwd.scale(Mth.cos(a1)).add(planeRight.scale(Mth.sin(a1))).normalize();

            float inner0 = reach * (1.0F - 0.42F * prof0);
            float inner1 = reach * (1.0F - 0.42F * prof1);

            Vec3 pIn0 = inst.start.add(dir0.scale(inner0));
            Vec3 pOut0 = inst.start.add(dir0.scale(reach));
            Vec3 pOut1 = inst.start.add(dir1.scale(reach));
            Vec3 pIn1 = inst.start.add(dir1.scale(inner1));

            emitDoubleSidedQuad(matrix, vc,
                    pIn0, pOut0, pOut1, pIn1,
                    pr, pg, pb, 0.15F * fade * prof0,
                    ar, ag, ab, 0.88F * fade * prof0,
                    ar, ag, ab, 0.88F * fade * prof1,
                    pr, pg, pb, 0.15F * fade * prof1);

            // White-hot razor cutting rim
            Vec3 pRim0 = inst.start.add(dir0.scale(reach * (1.0F - 0.08F * prof0)));
            Vec3 pRim1 = inst.start.add(dir1.scale(reach * (1.0F - 0.08F * prof1)));
            emitDoubleSidedQuad(matrix, vc,
                    pRim0, pOut0, pOut1, pRim1,
                    1.0F, 1.0F, 1.0F, 0.85F * fade * prof0,
                    1.0F, 1.0F, 1.0F, 0.96F * fade * prof0,
                    1.0F, 1.0F, 1.0F, 0.96F * fade * prof1,
                    1.0F, 1.0F, 1.0F, 0.85F * fade * prof1);
        }
    }

    // =========================================================================
    // 7. VOLUMETRIC 3D VORTEX CONE
    // =========================================================================

    private static void renderVortexCone(
            Matrix4f matrix,
            VertexConsumer vc,
            SpellWorldVfxManager.ActiveVfxInstance inst,
            float progress,
            float gameTime,
            float pr, float pg, float pb,
            float ar, float ag, float ab
    ) {
        Vec3 fwd = inst.endOrDir.lengthSqr() > 1.0E-4D ? inst.endOrDir.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
        Vec3 right = buildPerpendicularRight(fwd);
        Vec3 up = right.cross(fwd).normalize();

        float fade = computeEnvelope(progress, 0.15F, 0.45F);
        float maxRange = inst.radiusOrScale;
        float coneTan = 0.56F; // ~29-degree half-angle

        // 5 forward-surging vortex hoops expanding along the cone
        int ringCount = 5;
        for (int rIdx = 0; rIdx < ringCount; rIdx++) {
            float ringT = ((rIdx / (float) ringCount) + progress * 1.15F) % 1.0F;
            float dist = 0.45F + ringT * maxRange;
            float ringRad = dist * coneTan;
            float ringFade = fade * Mth.sin(ringT * (float) Math.PI);
            Vec3 ringCenter = inst.start.add(fwd.scale(dist));

            renderOrientedRing(matrix, vc, ringCenter, right, up,
                    ringRad * 0.88F, ringRad, 20, pr, pg, pb, 0.68F * ringFade);
        }

        // 4 spiraling vortex ribbons sweeping from muzzle to cone mouth
        int spirals = 4;
        int steps = 14;
        float activeReach = maxRange * Mth.clamp(0.35F + progress * 0.85F, 0.35F, 1.0F);
        for (int s = 0; s < spirals; s++) {
            float baseAng = (float) (Math.PI * 2.0D * s / spirals) - gameTime * 0.35F + inst.seed;
            Vec3 prev = null;
            for (int i = 0; i <= steps; i++) {
                float t = (float) i / steps;
                float dist = 0.35F + t * activeReach;
                float rad = dist * coneTan * 0.92F;
                float ang = baseAng + t * 3.4F;
                Vec3 pt = inst.start.add(fwd.scale(dist))
                        .add(right.scale(Mth.cos(ang) * rad))
                        .add(up.scale(Mth.sin(ang) * rad));
                if (prev != null) {
                    float segFade = fade * (1.0F - t * 0.45F);
                    renderCrossSegment(matrix, vc, prev, pt, 0.065F, ar, ag, ab, 0.72F * segFade);
                }
                prev = pt;
            }
        }
    }

    // =========================================================================
    // 8. ORBITAL SKYFALL PILLAR
    // =========================================================================

    private static void renderSkyfallPillar(
            Matrix4f matrix,
            VertexConsumer vc,
            SpellWorldVfxManager.ActiveVfxInstance inst,
            float progress,
            float gameTime,
            float pr, float pg, float pb,
            float ar, float ag, float ab
    ) {
        Vec3 ground = inst.start;
        Vec3 skyTop = inst.endOrDir;
        float fade = computeEnvelope(progress, 0.10F, 0.35F);
        float radius = inst.radiusOrScale;

        // Descent phase (first 28% of animation) slams downward from skyTop to ground
        float strikeT = Mth.clamp(progress / 0.28F, 0.0F, 1.0F);
        Vec3 currentBottom = new Vec3(
                Mth.lerp(strikeT, skyTop.x, ground.x),
                Mth.lerp(strikeT, skyTop.y, ground.y),
                Mth.lerp(strikeT, skyTop.z, ground.z)
        );

        float beamWidth = Math.max(0.24F, radius * 0.28F) * fade;
        renderTube(matrix, vc, currentBottom, skyTop, beamWidth * 0.45F, 10, 1.0F, 1.0F, 1.0F, 0.94F * fade, FULL_LIGHT);
        renderTube(matrix, vc, currentBottom, skyTop, beamWidth * 1.15F, 12, pr, pg, pb, 0.50F * fade, FULL_LIGHT);

        Vec3 right = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 forward = new Vec3(0.0D, 0.0D, 1.0D);

        // Descending sky halo collars
        for (int c = 0; c < 3; c++) {
            float collarT = ((progress * 1.8F + c * 0.33F) % 1.0F);
            double y = Mth.lerp(collarT, skyTop.y, ground.y + 0.2D);
            float cFade = fade * Mth.sin(collarT * (float) Math.PI);
            renderOrientedRing(matrix, vc, new Vec3(ground.x, y, ground.z), right, forward,
                    beamWidth * 1.4F, beamWidth * 2.2F, 20, ar, ag, ab, 0.72F * cFade);
        }

        // Ground impact arcane seal & expanding shockwave once beam touches down
        if (progress >= 0.22F) {
            float impT = (progress - 0.22F) / 0.78F;
            float impFade = 1.0F - impT;
            float shockR = radius * (0.25F + 0.95F * (1.0F - (1.0F - impT) * (1.0F - impT)));
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.06D, 0.0D), right, forward,
                    shockR * 0.78F, shockR, 26, ar, ag, ab, 0.85F * impFade);
            renderHemisphereShell(matrix, vc, ground, shockR * 0.75F, 6, 18, pr, pg, pb, 0.32F * impFade);
        }
    }

    // =========================================================================
    // 9. DIMENSIONAL BLINK RIFT & WARP STREAK
    // =========================================================================

    private static void renderBlinkRift(
            Matrix4f matrix,
            VertexConsumer vc,
            Camera camera,
            SpellWorldVfxManager.ActiveVfxInstance inst,
            float progress,
            float pr, float pg, float pb,
            float ar, float ag, float ab
    ) {
        float fade = computeEnvelope(progress, 0.15F, 0.45F);
        Vec3 depCenter = inst.start.add(0.0D, 1.0D, 0.0D);
        Vec3 arrCenter = inst.endOrDir.add(0.0D, 1.0D, 0.0D);

        renderVerticalDiamondPortalRim(matrix, vc, depCenter, camera, 0.46F * fade, 1.15F * fade, pr, pg, pb, ar, ag, ab, fade);
        renderVerticalDiamondPortalRim(matrix, vc, arrCenter, camera, 0.52F * fade, 1.25F * fade, pr, pg, pb, ar, ag, ab, fade);

        // Sleek warp-streak ribbon connecting departure & arrival
        if (depCenter.distanceToSqr(arrCenter) > 0.25D) {
            renderCrossSegment(matrix, vc, depCenter, arrCenter, 0.08F * fade, pr, pg, pb, 0.48F * fade);
            renderCrossSegment(matrix, vc, depCenter, arrCenter, 0.03F * fade, 1.0F, 1.0F, 1.0F, 0.82F * fade);
        }
    }

    // =========================================================================
    // 10. SHIMMERING 3D WARD / AURA BARRIER SHELL
    // =========================================================================

    private static void renderWardShell(
            Matrix4f matrix,
            VertexConsumer vc,
            SpellWorldVfxManager.ActiveVfxInstance inst,
            float progress,
            float gameTime,
            float pr, float pg, float pb,
            float ar, float ag, float ab
    ) {
        float fade = computeEnvelope(progress, 0.15F, 0.35F);
        float r = inst.radiusOrScale;
        Vec3 center = inst.start.add(0.0D, 0.9D, 0.0D);

        float rot = gameTime * 0.06F + inst.seed;
        Vec3 rx = new Vec3(Mth.cos(rot), 0.0D, Mth.sin(rot));
        Vec3 rz = new Vec3(-Mth.sin(rot), 0.0D, Mth.cos(rot));
        Vec3 ry = new Vec3(0.0D, 1.0D, 0.0D);

        // 3 orthogonal rotating runic meridian hoops
        renderOrientedRing(matrix, vc, center, rx, rz, r * 0.94F, r, 24, pr, pg, pb, 0.65F * fade);
        renderOrientedRing(matrix, vc, center, rx, ry, r * 0.94F, r, 24, ar, ag, ab, 0.58F * fade);
        renderOrientedRing(matrix, vc, center, rz, ry, r * 0.94F, r, 24, 1.0F, 1.0F, 1.0F, 0.48F * fade);

        // Soft upper + lower dome shimmer
        renderHemisphereShell(matrix, vc, center, r * 0.96F, 5, 16, pr, pg, pb, 0.18F * fade);
    }

    // =========================================================================
    // 11. SUMMONING RITUAL CIRCLE & ASCENDING HALO PILLAR
    // =========================================================================

    private static void renderSummonPillar(
            Matrix4f matrix,
            VertexConsumer vc,
            SpellWorldVfxManager.ActiveVfxInstance inst,
            float progress,
            float gameTime,
            float pr, float pg, float pb,
            float ar, float ag, float ab
    ) {
        float fade = computeEnvelope(progress, 0.12F, 0.30F);
        float r = inst.radiusOrScale;
        Vec3 base = inst.start.add(0.0D, 0.05D, 0.0D);
        Vec3 right = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 forward = new Vec3(0.0D, 0.0D, 1.0D);

        // Ground ritual circle + inscribed star
        renderOrientedRing(matrix, vc, base, right, forward, r * 0.90F, r, 28, pr, pg, pb, 0.82F * fade);
        renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.015D, 0.0D), right, forward,
                r * 0.90F, 6, gameTime * 0.07F + inst.seed, ar, ag, ab, 0.78F * fade);

        // Rising ritual column
        Vec3 top = base.add(0.0D, 3.6D * (0.35F + 0.65F * progress), 0.0D);
        renderTube(matrix, vc, base, top, r * 0.32F * fade, 12, pr, pg, pb, 0.36F * fade, FULL_LIGHT);
        renderTube(matrix, vc, base, top, r * 0.12F * fade, 8, 1.0F, 1.0F, 1.0F, 0.78F * fade, FULL_LIGHT);

        // Two ascending halo rings lifting up the summon column
        for (int h = 0; h < 2; h++) {
            float ht = ((progress * 1.4F + h * 0.5F) % 1.0F);
            Vec3 hPos = base.add(0.0D, ht * 3.0D, 0.0D);
            float hFade = fade * Mth.sin(ht * (float) Math.PI);
            renderOrientedRing(matrix, vc, hPos, right, forward,
                    r * 0.65F, r * 0.82F, 22, ar, ag, ab, 0.76F * hFade);
        }
    }

    // =========================================================================
    // LOW-LEVEL 3D GEOMETRY PRIMITIVES (DOUBLE-SIDED EMISSIVE MESHES)
    // =========================================================================

    private static void renderTube(
            Matrix4f matrix,
            VertexConsumer vc,
            Vec3 p0,
            Vec3 p1,
            float radius,
            int sides,
            float r, float g, float b, float a,
            int light
    ) {
        Vec3 span = p1.subtract(p0);
        if (span.lengthSqr() < 1.0E-5D || radius <= 0.001F || a <= 0.005F) return;

        Vec3 fwd = span.normalize();
        Vec3 right = buildPerpendicularRight(fwd);
        Vec3 up = right.cross(fwd).normalize();

        for (int i = 0; i < sides; i++) {
            float a0 = (float) (Math.PI * 2.0D * i / sides);
            float a1 = (float) (Math.PI * 2.0D * (i + 1) / sides);

            Vec3 off0 = right.scale(Mth.cos(a0) * radius).add(up.scale(Mth.sin(a0) * radius));
            Vec3 off1 = right.scale(Mth.cos(a1) * radius).add(up.scale(Mth.sin(a1) * radius));

            Vec3 v00 = p0.add(off0);
            Vec3 v01 = p0.add(off1);
            Vec3 v11 = p1.add(off1);
            Vec3 v10 = p1.add(off0);

            emitQuadWithLight(matrix, vc, v00, v01, v11, v10, r, g, b, a, light);
            emitQuadWithLight(matrix, vc, v10, v11, v01, v00, r, g, b, a, light);
        }
    }

    private static void renderCrossSegment(
            Matrix4f matrix,
            VertexConsumer vc,
            Vec3 p0,
            Vec3 p1,
            float halfWidth,
            float r, float g, float b, float a
    ) {
        Vec3 span = p1.subtract(p0);
        if (span.lengthSqr() < 1.0E-6D || halfWidth <= 0.001F || a <= 0.005F) return;

        Vec3 fwd = span.normalize();
        Vec3 right = buildPerpendicularRight(fwd).scale(halfWidth);
        Vec3 up = right.normalize().cross(fwd).normalize().scale(halfWidth);

        emitDoubleSidedFlatQuad(matrix, vc,
                p0.subtract(right), p0.add(right), p1.add(right), p1.subtract(right),
                r, g, b, a);
        emitDoubleSidedFlatQuad(matrix, vc,
                p0.subtract(up), p0.add(up), p1.add(up), p1.subtract(up),
                r, g, b, a);
    }

    private static void renderOrientedRing(
            Matrix4f matrix,
            VertexConsumer vc,
            Vec3 center,
            Vec3 axisU,
            Vec3 axisV,
            float rIn,
            float rOut,
            int segments,
            float r, float g, float b, float a
    ) {
        if (rOut <= 0.002F || a <= 0.005F) return;
        for (int i = 0; i < segments; i++) {
            float a0 = (float) (Math.PI * 2.0D * i / segments);
            float a1 = (float) (Math.PI * 2.0D * (i + 1) / segments);

            Vec3 dir0 = axisU.scale(Mth.cos(a0)).add(axisV.scale(Mth.sin(a0)));
            Vec3 dir1 = axisU.scale(Mth.cos(a1)).add(axisV.scale(Mth.sin(a1)));

            Vec3 in0 = center.add(dir0.scale(rIn));
            Vec3 out0 = center.add(dir0.scale(rOut));
            Vec3 out1 = center.add(dir1.scale(rOut));
            Vec3 in1 = center.add(dir1.scale(rIn));

            emitDoubleSidedFlatQuad(matrix, vc, in0, out0, out1, in1, r, g, b, a);
        }
    }

    private static void renderOrientedStarPolygon(
            Matrix4f matrix,
            VertexConsumer vc,
            Vec3 center,
            Vec3 axisU,
            Vec3 axisV,
            float radius,
            int points,
            float rotRad,
            float r, float g, float b, float a
    ) {
        if (radius <= 0.02F || a <= 0.005F) return;
        float lineHalfW = Math.max(0.025F, radius * 0.028F);

        // Connect every 2nd vertex to form interlocking Hexagram (6) or Octagram (8)
        int step = 2;
        for (int i = 0; i < points; i++) {
            float a0 = rotRad + (float) (Math.PI * 2.0D * i / points);
            float a1 = rotRad + (float) (Math.PI * 2.0D * ((i + step) % points) / points);

            Vec3 p0 = center.add(axisU.scale(Mth.cos(a0) * radius)).add(axisV.scale(Mth.sin(a0) * radius));
            Vec3 p1 = center.add(axisU.scale(Mth.cos(a1) * radius)).add(axisV.scale(Mth.sin(a1) * radius));
            renderCrossSegment(matrix, vc, p0, p1, lineHalfW, r, g, b, a);
        }
    }

    private static void renderHemisphereShell(
            Matrix4f matrix,
            VertexConsumer vc,
            Vec3 center,
            float radius,
            int latBands,
            int lonSegs,
            float r, float g, float b, float a
    ) {
        if (radius <= 0.02F || a <= 0.005F) return;
        for (int lat = 0; lat < latBands; lat++) {
            float phi0 = (float) (Math.PI * 0.5D * lat / latBands);
            float phi1 = (float) (Math.PI * 0.5D * (lat + 1) / latBands);

            float y0 = Mth.sin(phi0) * radius;
            float r0 = Mth.cos(phi0) * radius;
            float y1 = Mth.sin(phi1) * radius;
            float r1 = Mth.cos(phi1) * radius;

            // Rim brighter near horizon, softer toward zenith
            float a0 = a * (1.15F - 0.55F * ((float) lat / latBands));
            float a1 = a * (1.15F - 0.55F * ((float) (lat + 1) / latBands));

            for (int lon = 0; lon < lonSegs; lon++) {
                float th0 = (float) (Math.PI * 2.0D * lon / lonSegs);
                float th1 = (float) (Math.PI * 2.0D * (lon + 1) / lonSegs);

                Vec3 v00 = center.add(Mth.cos(th0) * r0, y0, Mth.sin(th0) * r0);
                Vec3 v01 = center.add(Mth.cos(th1) * r0, y0, Mth.sin(th1) * r0);
                Vec3 v11 = center.add(Mth.cos(th1) * r1, y1, Mth.sin(th1) * r1);
                Vec3 v10 = center.add(Mth.cos(th0) * r1, y1, Mth.sin(th0) * r1);

                emitDoubleSidedQuad(matrix, vc, v00, v01, v11, v10,
                        r, g, b, a0,
                        r, g, b, a0,
                        r, g, b, a1,
                        r, g, b, a1);
            }
        }
    }

    private static void renderVerticalAuraCurtain(
            Matrix4f matrix,
            VertexConsumer vc,
            Vec3 baseCenter,
            float radius,
            float height,
            int segments,
            float r, float g, float b, float baseAlpha
    ) {
        if (radius <= 0.02F || baseAlpha <= 0.005F) return;
        for (int i = 0; i < segments; i++) {
            float a0 = (float) (Math.PI * 2.0D * i / segments);
            float a1 = (float) (Math.PI * 2.0D * (i + 1) / segments);

            Vec3 b0 = baseCenter.add(Mth.cos(a0) * radius, 0.0D, Mth.sin(a0) * radius);
            Vec3 b1 = baseCenter.add(Mth.cos(a1) * radius, 0.0D, Mth.sin(a1) * radius);
            Vec3 t1 = b1.add(0.0D, height, 0.0D);
            Vec3 t0 = b0.add(0.0D, height, 0.0D);

            emitDoubleSidedQuad(matrix, vc, b0, b1, t1, t0,
                    r, g, b, baseAlpha,
                    r, g, b, baseAlpha,
                    r, g, b, 0.0F,
                    r, g, b, 0.0F);
        }
    }

    private static void renderOctahedron(
            Matrix4f matrix,
            VertexConsumer vc,
            Vec3 c,
            float rad,
            float r, float g, float b, float a
    ) {
        if (rad <= 0.005F || a <= 0.005F) return;
        Vec3 top = c.add(0.0D, rad, 0.0D);
        Vec3 bot = c.add(0.0D, -rad, 0.0D);
        Vec3 px = c.add(rad, 0.0D, 0.0D);
        Vec3 nx = c.add(-rad, 0.0D, 0.0D);
        Vec3 pz = c.add(0.0D, 0.0D, rad);
        Vec3 nz = c.add(0.0D, 0.0D, -rad);

        emitDoubleSidedFlatQuad(matrix, vc, top, px, pz, top, r, g, b, a);
        emitDoubleSidedFlatQuad(matrix, vc, top, pz, nx, top, r, g, b, a);
        emitDoubleSidedFlatQuad(matrix, vc, top, nx, nz, top, r, g, b, a);
        emitDoubleSidedFlatQuad(matrix, vc, top, nz, px, top, r, g, b, a);

        emitDoubleSidedFlatQuad(matrix, vc, bot, pz, px, bot, r, g, b, a);
        emitDoubleSidedFlatQuad(matrix, vc, bot, nx, pz, bot, r, g, b, a);
        emitDoubleSidedFlatQuad(matrix, vc, bot, nz, nx, bot, r, g, b, a);
        emitDoubleSidedFlatQuad(matrix, vc, bot, px, nz, bot, r, g, b, a);
    }

    private static void renderVerticalDiamondPortalFill(
            Matrix4f matrix,
            VertexConsumer vc,
            Vec3 center,
            Camera camera,
            float halfW,
            float halfH,
            float r, float g, float b, float a,
            int light
    ) {
        Vec3 toCam = camera.getPosition().subtract(center);
        Vec3 horiz = new Vec3(toCam.x, 0.0D, toCam.z);
        Vec3 right = horiz.lengthSqr() > 1.0E-4D ? new Vec3(-horiz.z, 0.0D, horiz.x).normalize() : new Vec3(1.0D, 0.0D, 0.0D);

        Vec3 top = center.add(0.0D, halfH, 0.0D);
        Vec3 bot = center.add(0.0D, -halfH, 0.0D);
        Vec3 rgt = center.add(right.scale(halfW));
        Vec3 lft = center.subtract(right.scale(halfW));

        emitQuadWithLight(matrix, vc, top, rgt, bot, lft, r, g, b, a, light);
        emitQuadWithLight(matrix, vc, lft, bot, rgt, top, r, g, b, a, light);
    }

    private static void renderVerticalDiamondPortalRim(
            Matrix4f matrix,
            VertexConsumer vc,
            Vec3 center,
            Camera camera,
            float halfW,
            float halfH,
            float pr, float pg, float pb,
            float ar, float ag, float ab,
            float fade
    ) {
        Vec3 toCam = camera.getPosition().subtract(center);
        Vec3 horiz = new Vec3(toCam.x, 0.0D, toCam.z);
        Vec3 right = horiz.lengthSqr() > 1.0E-4D ? new Vec3(-horiz.z, 0.0D, horiz.x).normalize() : new Vec3(1.0D, 0.0D, 0.0D);

        Vec3 top = center.add(0.0D, halfH, 0.0D);
        Vec3 bot = center.add(0.0D, -halfH, 0.0D);
        Vec3 rgt = center.add(right.scale(halfW));
        Vec3 lft = center.subtract(right.scale(halfW));

        // Translucent glowing interior membrane
        emitDoubleSidedFlatQuad(matrix, vc, top, rgt, bot, lft, pr, pg, pb, 0.34F * fade);

        // Blazing diamond tear edges
        renderCrossSegment(matrix, vc, top, rgt, 0.055F, ar, ag, ab, 0.92F * fade);
        renderCrossSegment(matrix, vc, rgt, bot, 0.055F, ar, ag, ab, 0.92F * fade);
        renderCrossSegment(matrix, vc, bot, lft, 0.055F, ar, ag, ab, 0.92F * fade);
        renderCrossSegment(matrix, vc, lft, top, 0.055F, ar, ag, ab, 0.92F * fade);
    }

    private static void emitDoubleSidedFlatQuad(
            Matrix4f matrix,
            VertexConsumer vc,
            Vec3 v0, Vec3 v1, Vec3 v2, Vec3 v3,
            float r, float g, float b, float a
    ) {
        emitQuadWithLight(matrix, vc, v0, v1, v2, v3, r, g, b, a, FULL_LIGHT);
        emitQuadWithLight(matrix, vc, v3, v2, v1, v0, r, g, b, a, FULL_LIGHT);
    }

    private static void emitQuadWithLight(
            Matrix4f matrix,
            VertexConsumer vc,
            Vec3 v0, Vec3 v1, Vec3 v2, Vec3 v3,
            float r, float g, float b, float a,
            int light
    ) {
        vc.addVertex(matrix, (float) v0.x, (float) v0.y, (float) v0.z)
                .setColor(r, g, b, a).setUv(0.0F, 0.0F).setOverlay(NO_OVERLAY).setLight(light).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(matrix, (float) v1.x, (float) v1.y, (float) v1.z)
                .setColor(r, g, b, a).setUv(0.0F, 1.0F).setOverlay(NO_OVERLAY).setLight(light).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(matrix, (float) v2.x, (float) v2.y, (float) v2.z)
                .setColor(r, g, b, a).setUv(1.0F, 1.0F).setOverlay(NO_OVERLAY).setLight(light).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(matrix, (float) v3.x, (float) v3.y, (float) v3.z)
                .setColor(r, g, b, a).setUv(1.0F, 0.0F).setOverlay(NO_OVERLAY).setLight(light).setNormal(0.0F, 1.0F, 0.0F);
    }

    private static void emitDoubleSidedQuad(
            Matrix4f matrix,
            VertexConsumer vc,
            Vec3 v0, Vec3 v1, Vec3 v2, Vec3 v3,
            float r0, float g0, float b0, float a0,
            float r1, float g1, float b1, float a1,
            float r2, float g2, float b2, float a2,
            float r3, float g3, float b3, float a3
    ) {
        vc.addVertex(matrix, (float) v0.x, (float) v0.y, (float) v0.z)
                .setColor(r0, g0, b0, a0).setUv(0.0F, 0.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(matrix, (float) v1.x, (float) v1.y, (float) v1.z)
                .setColor(r1, g1, b1, a1).setUv(0.0F, 1.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(matrix, (float) v2.x, (float) v2.y, (float) v2.z)
                .setColor(r2, g2, b2, a2).setUv(1.0F, 1.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(matrix, (float) v3.x, (float) v3.y, (float) v3.z)
                .setColor(r3, g3, b3, a3).setUv(1.0F, 0.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);

        vc.addVertex(matrix, (float) v3.x, (float) v3.y, (float) v3.z)
                .setColor(r3, g3, b3, a3).setUv(1.0F, 0.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(matrix, (float) v2.x, (float) v2.y, (float) v2.z)
                .setColor(r2, g2, b2, a2).setUv(1.0F, 1.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(matrix, (float) v1.x, (float) v1.y, (float) v1.z)
                .setColor(r1, g1, b1, a1).setUv(0.0F, 1.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(matrix, (float) v0.x, (float) v0.y, (float) v0.z)
                .setColor(r0, g0, b0, a0).setUv(0.0F, 0.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
    }

    private static Vec3 buildPerpendicularRight(Vec3 fwd) {
        Vec3 ref = Math.abs(fwd.y) > 0.92D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
        return fwd.cross(ref).normalize();
    }

    private static float computeEnvelope(float progress, float fadeInEnd, float fadeOutStart) {
        if (progress < fadeInEnd && fadeInEnd > 0.001F) {
            return Mth.clamp(progress / fadeInEnd, 0.0F, 1.0F);
        }
        if (progress > fadeOutStart && fadeOutStart < 0.999F) {
            return Mth.clamp(1.0F - (progress - fadeOutStart) / (1.0F - fadeOutStart), 0.0F, 1.0F);
        }
        return 1.0F;
    }

    private static float hashFloat(int a, int b) {
        int h = a * 374761393 + b * 668265263;
        h = (h ^ (h >> 13)) * 1274126177;
        h = h ^ (h >> 16);
        return (h & 0x7FFFFFFF) / (float) 0x7FFFFFFF;
    }

    private static boolean isDarkSingularityMateria(EssenceType type) {
        return switch (type) {
            case VOID, NULL_U, OBLIVION, ESCHATON, SINGULARITY, VACUUM, OBLIVIATE -> true;
            default -> false;
        };
    }
}
