package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.network.SpellVfxEventPayload;
import ddraig.net.entropica.registry.AestheticGlassRegistry;
import ddraig.net.entropica.spell.vfx.SpellParticleHelper;
import ddraig.net.entropica.spell.vfx.SpellWardClientManager;
import ddraig.net.entropica.spell.vfx.SpellWorldVfxManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
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
        List<SpellWardClientManager.WardInstance> activeWards = SpellWardClientManager.snapshotActiveWards();
        if (events.isEmpty() && activeWards.isEmpty()) {
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

        // Pass 1.5: Skyfall 3D Pseudoblock Monoliths & Barricades (VFX_SKYFALL_PSEUDOBLOCK)
        // Rendered via BlockRenderDispatcher BEFORE acquiring emissiveType VertexConsumer so block sheet buffers flush cleanly!
        boolean drewPseudoblocks = false;
        var blockRenderer = mc.getBlockRenderer();
        for (SpellWorldVfxManager.ActiveVfxInstance inst : events) {
            if (inst.vfxType != SpellVfxEventPayload.VFX_SKYFALL_PSEUDOBLOCK) continue;
            net.minecraft.world.level.block.state.BlockState blockState =
                    net.minecraft.world.level.block.Block.stateById(inst.primaryRgb);
            if (blockState.isAir()) {
                blockState = net.minecraft.world.level.block.Blocks.PACKED_ICE.defaultBlockState();
            }
            int fallTicks = Math.max(1, inst.accentRgb);
            float elapsed = inst.ageTicks + partialTick;
            float fallT = Mth.clamp(elapsed / (float) fallTicks, 0.0F, 1.0F);
            float accel = fallT * fallT;

            double curX = Mth.lerp(accel, inst.start.x - 0.5D, inst.endOrDir.x);
            double curY = Mth.lerp(accel, inst.start.y, inst.endOrDir.y);
            double curZ = Mth.lerp(accel, inst.start.z - 0.5D, inst.endOrDir.z);
            int colHeight = Math.clamp(Math.round(inst.radiusOrScale), 1, 20);

            for (int y = 0; y < colHeight; y++) {
                poseStack.pushPose();
                poseStack.translate(curX, curY + y, curZ);
                // Slight mystical scale-in during first 6 ticks of emergence from the overhead rift
                if (elapsed < 6.0F) {
                    float s = Mth.lerp(elapsed / 6.0F, 0.35F, 1.0F);
                    poseStack.translate(0.5D * (1.0D - s), 0.5D * (1.0D - s), 0.5D * (1.0D - s));
                    poseStack.scale(s, s, s);
                }
                blockRenderer.renderSingleBlock(blockState, poseStack, bufferSource, FULL_LIGHT, NO_OVERLAY);
                poseStack.popPose();
            }
            drewPseudoblocks = true;
        }
        if (drewPseudoblocks) {
            bufferSource.endBatch();
        }

        // Pass 2: Translucent-Emissive 3D Procedural Geometry
        VertexConsumer vc = bufferSource.getBuffer(emissiveType);
        for (SpellWorldVfxManager.ActiveVfxInstance inst : events) {
            float progress = Mth.clamp((inst.ageTicks + partialTick) / (float) inst.maxTicks, 0.0F, 1.0F);
            boolean isPseudoblock = inst.vfxType == SpellVfxEventPayload.VFX_SKYFALL_PSEUDOBLOCK;
            int pRgb = (isPseudoblock || inst.essence.isDynamic())
                    ? SpellParticleHelper.resolveVibrantPrimaryRgb(inst.essence, gameTime + inst.seed * 5)
                    : inst.primaryRgb;
            int aRgb = (isPseudoblock || inst.essence.isDynamic())
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
                case SpellVfxEventPayload.VFX_TORNADO_VORTEX ->
                        renderTornadoVortex(matrix, vc, inst, progress, (float) gameTime, pr, pg, pb, ar, ag, ab);
                case SpellVfxEventPayload.VFX_GEYSER_PLUME ->
                        renderGeyserPlume(matrix, vc, inst, progress, (float) gameTime, pr, pg, pb, ar, ag, ab);
                case SpellVfxEventPayload.VFX_VACUUM_SPHERE ->
                        renderVacuumSphere(matrix, vc, inst, progress, (float) gameTime, pr, pg, pb, ar, ag, ab);
                case SpellVfxEventPayload.VFX_SKYFALL_PSEUDOBLOCK ->
                        renderSkyfallPseudoblockAura(matrix, vc, inst, partialTick, (float) gameTime, pr, pg, pb, ar, ag, ab);
                default -> {
                }
            }
        }

        // Pass 3: Persistent Entity-Tracked 3D Ward & Aegis Barrier Shells (with dynamic Hit-Flare & Shatter!)
        for (SpellWardClientManager.WardInstance ward : activeWards) {
            renderPersistentEntityWard(mc, matrix, vc, ward, partialTick, (float) gameTime);
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

        boolean channeledSlice = inst.maxTicks <= 4;
        float fade = channeledSlice ? (1.0F - progress * 0.25F) : computeEnvelope(progress, 0.06F, 0.58F);
        float pulse = 1.0F + 0.14F * Mth.sin(gameTime * 0.85F + (channeledSlice ? 0.0F : inst.seed));
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
        float spinPhase = -gameTime * 0.45F + (channeledSlice ? 0.0F : inst.seed);

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
                baseR * 2.2F, isAerodynamicMateria(inst.essence) ? 8 : 6, muzzleRot, 1.0F, 1.0F, 1.0F, 0.72F * fade);

        // Air / Aura Jetstream Propulsion: supersonic Mach shock-diamonds, flared slipstream funnel & racing wind collars!
        if (isAerodynamicMateria(inst.essence)) {
            int diamondCount = Math.clamp((int) (len * 1.35D), 3, 18);
            for (int d = 1; d <= diamondCount; d++) {
                double dist = (len * d) / (diamondCount + 1.0D);
                float dPulse = 0.80F + 0.25F * Mth.sin(gameTime * 1.15F - d * 0.9F);
                Vec3 dPos = inst.start.add(fwd.scale(dist));
                renderOctahedron(matrix, vc, dPos, baseR * 0.72F * dPulse * fade, 1.0F, 1.0F, 1.0F, 0.88F * fade);
            }

            int collarCount = 6;
            for (int c = 0; c < collarCount; c++) {
                float ct = (((gameTime * 0.14F) + (c / (float) collarCount)) % 1.0F);
                double dist = ct * len;
                float cFlare = baseR * (1.75F + 1.15F * Mth.sin(ct * (float) Math.PI));
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                Vec3 cCenter = inst.start.add(fwd.scale(dist));
                renderOrientedRing(matrix, vc, cCenter, right, up,
                        cFlare * 0.82F, cFlare, 20, ar, ag, ab, 0.80F * cAlpha);
                renderOrientedFrustumBand(matrix, vc,
                        cCenter.add(fwd.scale(0.18D)), cCenter.subtract(fwd.scale(0.38D)),
                        right, up, cFlare * 0.52F, cFlare * 1.18F, 18,
                        pr, pg, pb, 0.36F * cAlpha, 0.04F * cAlpha);
            }

            float endFade = channeledSlice ? (0.92F - progress * 0.20F) : (1.0F - progress);
            renderOrientedStarPolygon(matrix, vc, inst.endOrDir.subtract(fwd.scale(0.06D)), right, up,
                    baseR * 2.8F * (1.0F + (channeledSlice ? 0.15F : progress * 0.8F)), 8, -gameTime * 0.22F, ar, ag, ab, 0.82F * endFade);
        } else if (isHydroMateria(inst.essence)) {
            // Section 6.3 High-Pressure Hydro-Cutter: dense deep-azure hydraulic jacket, racing whitewater pressure collars & splash spray!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.78F * fade, 10,
                    0.133F, 0.361F, 0.486F, 0.68F * fade, FULL_LIGHT);

            int collarCount = 5;
            for (int c = 0; c < collarCount; c++) {
                float ct = (((gameTime * 0.18F) + (c / (float) collarCount)) % 1.0F);
                double dist = ct * len;
                float cFlare = baseR * (1.55F + 0.95F * Mth.sin(ct * (float) Math.PI));
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                Vec3 cCenter = inst.start.add(fwd.scale(dist));
                renderOrientedRing(matrix, vc, cCenter, right, up,
                        cFlare * 0.78F, cFlare, 18, 0.88F, 0.98F, 1.00F, 0.84F * cAlpha);
                renderOrientedFrustumBand(matrix, vc,
                        cCenter.add(fwd.scale(0.14D)), cCenter.subtract(fwd.scale(0.34D)),
                        right, up, cFlare * 0.50F, cFlare * 1.15F, 16,
                        pr, pg, pb, 0.44F * cAlpha, 0.05F * cAlpha);
            }

            // Foaming whitewater back-spray crown at cutter impact point
            Vec3 impBase = inst.endOrDir.subtract(fwd.scale(0.04D));
            Vec3 impBack = inst.endOrDir.subtract(fwd.scale(baseR * 3.2D));
            renderOrientedFrustumBand(matrix, vc, impBase, impBack, right, up,
                    baseR * 0.85F, baseR * 3.4F, 18,
                    0.88F, 0.98F, 1.00F, 0.76F * fade, 0.06F * fade);
        } else if (isBotanicalMateria(inst.essence)) {
            // Section 6.4 Verdant Growth Ray: living emerald beam wrapped in coiling thorny ivy creepers, sprouting leaves & racing blossoms!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.82F * fade, 10,
                    0.102F, 0.349F, 0.137F, 0.64F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.46F * fade, 8,
                    0.71F, 1.00F, 0.47F, 0.88F * fade, FULL_LIGHT);

            int vineSteps = Math.clamp((int) (len * 4.2D), 8, 56);
            float ivyRadius = baseR * 1.48F;
            for (int v = 0; v < 3; v++) {
                float vOff = (float) (Math.PI * 2.0D * v / 3.0D);
                Vec3 prevVine = null;
                for (int i = 0; i <= vineSteps; i++) {
                    float t = (float) i / vineSteps;
                    double dist = t * len;
                    float ang = (float) dist * 2.35F - gameTime * 0.34F + vOff;
                    Vec3 radDir = right.scale(Mth.cos(ang)).add(up.scale(Mth.sin(ang)));
                    Vec3 tangDir = right.scale(-Mth.sin(ang)).add(up.scale(Mth.cos(ang)));
                    Vec3 pt = inst.start.add(fwd.scale(dist)).add(radDir.scale(ivyRadius));
                    if (prevVine != null) {
                        renderCrossSegment(matrix, vc, prevVine, pt, baseR * 0.22F * fade,
                                (v == 0) ? 0.36F : 0.102F, (v == 0) ? 0.23F : 0.48F, (v == 0) ? 0.12F : 0.18F, 0.90F * fade);
                    }
                    // Sprouting 3D emerald leaf blades along the coiling ivy creepers
                    if (i > 0 && i < vineSteps && (i + v) % 3 == 0) {
                        double leafS = baseR * 1.15D * fade;
                        Vec3 lBase = pt;
                        Vec3 lLeft = pt.add(radDir.scale(leafS * 0.55D)).add(tangDir.scale(leafS * 0.32D)).add(fwd.scale(leafS * 0.25D));
                        Vec3 lTip = pt.add(radDir.scale(leafS * 1.15D)).add(fwd.scale(leafS * 0.55D));
                        Vec3 lRight = pt.add(radDir.scale(leafS * 0.55D)).subtract(tangDir.scale(leafS * 0.32D)).add(fwd.scale(leafS * 0.25D));
                        emitDoubleSidedFlatQuad(matrix, vc, lBase, lLeft, lTip, lRight,
                                0.17F, 0.90F, 0.41F, 0.86F * fade);
                    }
                    prevVine = pt;
                }
            }

            // 5 racing woodland flower collars blooming down the Verdant Growth Ray
            int blossomCount = 5;
            for (int c = 0; c < blossomCount; c++) {
                float ct = (((gameTime * 0.12F) + (c / (float) blossomCount)) % 1.0F);
                double dist = ct * len;
                float cScale = baseR * (2.1F + 0.95F * Mth.sin(ct * (float) Math.PI));
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                Vec3 cCenter = inst.start.add(fwd.scale(dist));
                boolean pinkBlossom = (c % 2 == 0);
                renderBotanicalBlossom(matrix, vc, cCenter, right, up, cScale, 6,
                        gameTime * 0.16F + c,
                        pinkBlossom ? 1.00F : 0.71F, pinkBlossom ? 0.58F : 1.00F, pinkBlossom ? 0.76F : 0.47F,
                        0.85F * cAlpha);
            }

            // Blooming floral wreath at beam endpoint
            float endAlpha = channeledSlice ? (0.90F - progress * 0.20F) : (1.0F - progress);
            renderBotanicalBlossom(matrix, vc, inst.endOrDir.subtract(fwd.scale(0.04D)), right, up,
                    baseR * 2.9F, 6, -gameTime * 0.14F, 1.00F, 0.58F, 0.76F, 0.88F * endAlpha);
        } else if (isTectonicMateria(inst.essence)) {
            // Section 6.5 Magnetite Tractor Beam: lodestone-iron flux core, 6 INWARD-pulling octagonal rings & reeled ore shards!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.72F * fade, 8,
                    0.353F, 0.322F, 0.298F, 0.72F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.34F * fade, 6,
                    1.00F, 0.76F, 0.47F, 0.94F * fade, FULL_LIGHT);

            int pullRings = 6;
            for (int c = 0; c < pullRings; c++) {
                float rawT = (((gameTime * 0.16F) + (c / (float) pullRings)) % 1.0F);
                float ct = 1.0F - rawT; // Races INWARD from endpoint (1.0) back to caster (0.0)!
                double dist = ct * len;
                float cFlare = baseR * (1.45F + 1.15F * ct);
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                Vec3 cCenter = inst.start.add(fwd.scale(dist));
                renderOrientedRing(matrix, vc, cCenter, right, up,
                        cFlare * 0.80F, cFlare, 8,
                        (c % 2 == 0) ? 1.00F : 0.467F, (c % 2 == 0) ? 0.76F : 0.196F, (c % 2 == 0) ? 0.47F : 0.078F,
                        0.88F * cAlpha);
                renderOrientedFrustumBand(matrix, vc,
                        cCenter.add(fwd.scale(0.32D)), cCenter.subtract(fwd.scale(0.16D)),
                        right, up, cFlare * 1.16F, cFlare * 0.54F, 8,
                        0.353F, 0.322F, 0.298F, 0.38F * cAlpha, 0.10F * cAlpha);
            }

            // 10 magnetically reeled 3D iron/magnetite ore chunks streaking back toward the caster!
            for (int m = 0; m < 10; m++) {
                float mRaw = (((gameTime * 0.13F) + (m / 10.0F)) % 1.0F);
                float mt = 1.0F - mRaw;
                double dist = mt * len;
                float mAng = m * 2.39996F - gameTime * 0.28F;
                float orbitR = baseR * (0.65F + 1.25F * mt);
                Vec3 shardPos = inst.start.add(fwd.scale(dist))
                        .add(right.scale(Mth.cos(mAng) * orbitR))
                        .add(up.scale(Mth.sin(mAng) * orbitR));
                float mAlpha = fade * Mth.sin(mt * (float) Math.PI);
                boolean amberOre = (m % 3 == 0);
                renderOctahedron(matrix, vc, shardPos, baseR * 0.48F * (0.75F + 0.25F * mAlpha),
                        amberOre ? 1.00F : 0.52F, amberOre ? 0.76F : 0.50F, amberOre ? 0.47F : 0.48F,
                        0.92F * mAlpha);
            }
        } else if (isGlacialMateria(inst.essence)) {
            // Section 6.6 Absolute-Zero Cryo-Ray: permafrost-blue (#2C82C9) sub-zero sheath, 6 racing 3D snowflakes & spiraling hoarfrost crystals!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.76F * fade, 8,
                    0.173F, 0.510F, 0.788F, 0.68F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.36F * fade, 6,
                    0.95F, 0.99F, 1.00F, 0.96F * fade, FULL_LIGHT);

            int flakeCollars = 6;
            for (int c = 0; c < flakeCollars; c++) {
                float ct = (((gameTime * 0.14F) + (c / (float) flakeCollars)) % 1.0F);
                double dist = ct * len;
                float cScale = baseR * (1.85F + 1.05F * Mth.sin(ct * (float) Math.PI));
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                Vec3 cCenter = inst.start.add(fwd.scale(dist));
                renderSnowflakeCrystal(matrix, vc, cCenter, right, up, cScale,
                        gameTime * 0.18F + c * 0.6F, 0.92F * cAlpha);
            }

            // 10 spiraling sub-zero ice shards hurtling down the beam
            for (int s = 0; s < 10; s++) {
                float st = (((gameTime * 0.16F) + (s / 10.0F)) % 1.0F);
                double dist = st * len;
                float sAng = s * 2.39996F + gameTime * 0.32F;
                float orbitR = baseR * (1.10F + 0.45F * Mth.sin(st * (float) Math.PI));
                Vec3 shardPos = inst.start.add(fwd.scale(dist))
                        .add(right.scale(Mth.cos(sAng) * orbitR))
                        .add(up.scale(Mth.sin(sAng) * orbitR));
                float sAlpha = fade * Mth.sin(st * (float) Math.PI);
                boolean diamond = (s % 2 == 0);
                renderOctahedron(matrix, vc, shardPos, baseR * 0.42F * (0.75F + 0.25F * sAlpha),
                        diamond ? 0.94F : 0.596F, diamond ? 0.99F : 0.863F, 1.00F, 0.92F * sAlpha);
            }

            float endAlpha = channeledSlice ? (0.90F - progress * 0.20F) : (1.0F - progress);
            renderSnowflakeCrystal(matrix, vc, inst.endOrDir.subtract(fwd.scale(0.04D)), right, up,
                    baseR * 2.8F, -gameTime * 0.16F, 0.90F * endAlpha);
        } else if (isInfernalMateria(inst.essence)) {
            // Section 6.7 Nether Tendril Tether: burning braided chains of hellfire latching onto the target,
            // with inward-flowing thermal energy siphon rings & glowing embers draining back into the caster!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.78F * fade, 8,
                    0.722F, 0.235F, 0.031F, 0.72F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.34F * fade, 6,
                    1.00F, 0.820F, 0.361F, 0.96F * fade, FULL_LIGHT);

            // Twin coiling 3D hellfire chain strands latching tightly around the tether
            int chainSteps = Math.clamp((int) (len * 4.5D), 12, 42);
            for (int ch = 0; ch < 2; ch++) {
                float chOff = ch * (float) Math.PI - gameTime * 0.28F;
                Vec3 prevCh = null;
                for (int i = 0; i <= chainSteps; i++) {
                    float t = i / (float) chainSteps;
                    double dist = t * len;
                    float coilR = baseR * (1.28F + 0.35F * Mth.sin(t * (float) Math.PI));
                    float ang = chOff + t * (float) (len * 2.6D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * coilR))
                            .add(up.scale(Mth.sin(ang) * coilR));
                    if (prevCh != null) {
                        boolean goldLink = ((i + ch) % 2 == 0);
                        renderCrossSegment(matrix, vc, prevCh, pt, baseR * 0.36F * fade,
                                goldLink ? 1.00F : 0.722F,
                                goldLink ? 0.820F : 0.235F,
                                goldLink ? 0.361F : 0.031F,
                                0.92F * fade);
                    }
                    prevCh = pt;
                }
            }

            // 6 INWARD-RACING thermal life-siphon rings & 10 glowing ember orbs flowing from target (1.0) back to caster (0.0)!
            int siphonRings = 6;
            for (int c = 0; c < siphonRings; c++) {
                float rawT = (((gameTime * 0.15F) + (c / (float) siphonRings)) % 1.0F);
                float ct = 1.0F - rawT;
                double dist = ct * len;
                float cFlare = baseR * (1.45F + 0.95F * Mth.sin(ct * (float) Math.PI));
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                Vec3 cCenter = inst.start.add(fwd.scale(dist));
                renderOrientedRing(matrix, vc, cCenter, right, up,
                        cFlare * 0.78F, cFlare, 12,
                        (c % 2 == 0) ? 1.00F : 0.722F,
                        (c % 2 == 0) ? 0.820F : 0.235F,
                        (c % 2 == 0) ? 0.361F : 0.031F,
                        0.90F * cAlpha);
            }
            for (int e = 0; e < 10; e++) {
                float rawE = (((gameTime * 0.14F) + (e / 10.0F)) % 1.0F);
                float et = 1.0F - rawE;
                double dist = et * len;
                float eAng = e * 2.39996F - gameTime * 0.30F;
                float orbitR = baseR * (0.75F + 0.85F * et);
                Vec3 emberPos = inst.start.add(fwd.scale(dist))
                        .add(right.scale(Mth.cos(eAng) * orbitR))
                        .add(up.scale(Mth.sin(eAng) * orbitR));
                float eAlpha = fade * Mth.sin(et * (float) Math.PI);
                renderOctahedron(matrix, vc, emberPos, baseR * 0.42F * (0.75F + 0.25F * eAlpha),
                        1.00F, 0.820F, 0.361F, 0.94F * eAlpha);
            }
        } else if (isSolarRadiantMateria(inst.essence)) {
            // Section 6.8 Solar Thermal Lance: focused bloom daylight beam (NO chunk lightmap updates!),
            // 8-ray muzzle solar lens, 6 forward-racing golden corona rings & 8-ray impact starburst!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.52F * fade, 10,
                    1.00F, 0.995F, 0.95F, 0.98F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.48F * fade, 12,
                    1.00F, 0.886F, 0.471F, 0.44F * fade, FULL_LIGHT);

            Vec3 lensOrigin = inst.start.add(fwd.scale(0.10D));
            for (int rIdx = 0; rIdx < 8; rIdx++) {
                float rAng = gameTime * 0.14F + (float) (Math.PI * 2.0D * rIdx / 8.0D);
                Vec3 rDir = right.scale(Mth.cos(rAng)).add(up.scale(Mth.sin(rAng))).normalize();
                Vec3 rTang = fwd.cross(rDir).normalize();
                renderSolarStarburstRay(matrix, vc, lensOrigin, rDir, rTang, fwd,
                        baseR * 0.28F * fade, baseR * ((rIdx % 2 == 0) ? 2.85F : 1.95F) * fade, fade);
            }

            int coronaRings = 6;
            for (int c = 0; c < coronaRings; c++) {
                float ct = (((gameTime * 0.16F) + (c / (float) coronaRings)) % 1.0F);
                double dist = ct * len;
                float cFlare = baseR * (1.55F + 1.05F * Mth.sin(ct * (float) Math.PI));
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                Vec3 cCenter = inst.start.add(fwd.scale(dist));
                renderOrientedRing(matrix, vc, cCenter, right, up,
                        cFlare * 0.80F, cFlare, 20, 1.00F, 0.886F, 0.471F, 0.90F * cAlpha);
                renderOrientedStarPolygon(matrix, vc, cCenter, right, up,
                        cFlare * 0.94F, 8, -gameTime * 0.18F + c * 0.4F, 1.00F, 0.99F, 0.92F, 0.72F * cAlpha);
            }

            Vec3 impactCenter = inst.endOrDir.subtract(fwd.scale(0.06D));
            for (int rIdx = 0; rIdx < 8; rIdx++) {
                float rAng = -gameTime * 0.20F + (float) (Math.PI * 2.0D * rIdx / 8.0D);
                Vec3 rDir = right.scale(Mth.cos(rAng)).add(up.scale(Mth.sin(rAng))).subtract(fwd.scale(0.22D)).normalize();
                Vec3 rTang = fwd.cross(rDir).normalize();
                renderSolarStarburstRay(matrix, vc, impactCenter, rDir, rTang, fwd,
                        baseR * 0.30F * fade, baseR * ((rIdx % 2 == 0) ? 3.20F : 2.15F) * fade, fade);
            }
        } else if (isUmbralShadowMateria(inst.essence)) {
            // Section 6.9 Abyssal Shadow Lance: light-absorbing smoky black beam (#211A21) wrapped in swirling
            // umbral smoke helices & 6 inward-draining twilight eclipse rings (#8F5CFF) draining 30% attack strength!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.62F * fade, 10,
                    0.071F, 0.051F, 0.086F, 0.98F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.38F * fade, 12,
                    0.129F, 0.102F, 0.129F, 0.72F * fade, FULL_LIGHT);

            int smokeSteps = Math.clamp((int) (len * 4.0D), 10, 44);
            for (int sh = 0; sh < 3; sh++) {
                float shOff = (float) (Math.PI * 2.0D * sh / 3.0D) - gameTime * 0.24F;
                Vec3 prevSh = null;
                for (int i = 0; i <= smokeSteps; i++) {
                    float t = i / (float) smokeSteps;
                    double dist = t * len;
                    float coilR = baseR * (1.35F + 0.45F * Mth.sin(t * (float) Math.PI * 2.0F + sh));
                    float ang = shOff + t * (float) (len * 2.1D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * coilR))
                            .add(up.scale(Mth.sin(ang) * coilR));
                    if (prevSh != null) {
                        boolean violetRim = (sh == 2);
                        renderCrossSegment(matrix, vc, prevSh, pt, baseR * 0.34F * fade,
                                violetRim ? 0.56F : 0.129F,
                                violetRim ? 0.36F : 0.102F,
                                violetRim ? 1.00F : 0.129F,
                                (violetRim ? 0.82F : 0.92F) * fade);
                    }
                    prevSh = pt;
                }
            }

            int eclipseRings = 6;
            for (int c = 0; c < eclipseRings; c++) {
                float rawT = (((gameTime * 0.13F) + (c / (float) eclipseRings)) % 1.0F);
                float ct = 1.0F - rawT; // Inward strength-draining siphon!
                double dist = ct * len;
                float cFlare = baseR * (1.50F + 0.95F * Mth.sin(ct * (float) Math.PI));
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                Vec3 cCenter = inst.start.add(fwd.scale(dist));
                renderOrientedRing(matrix, vc, cCenter, right, up,
                        cFlare * 0.72F, cFlare, 18, 0.129F, 0.102F, 0.129F, 0.94F * cAlpha);
                renderOrientedRing(matrix, vc, cCenter, right, up,
                        cFlare * 0.96F, cFlare * 1.08F, 18, 0.56F, 0.36F, 1.00F, 0.80F * cAlpha);
            }
        } else if (isNecroticUndeadMateria(inst.essence)) {
            // Section 6.10 Siphon Life / Necrotic Drain: sustained dark shadowy necrotic tether (#4D6453),
            // coiling #E6DFCF spinal vertebrae rings, and 8 inward-streaming #5EE088 stolen life & Materia fuel orbs!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.58F * fade, 10,
                    0.125F, 0.175F, 0.140F, 0.96F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.36F * fade, 12,
                    0.302F, 0.392F, 0.325F, 0.64F * fade, FULL_LIGHT);

            int spineCount = Math.clamp((int) (len * 1.8D), 5, 18);
            for (int v = 0; v < spineCount; v++) {
                float vt = (v + 0.5F) / spineCount;
                double dist = vt * len;
                float sway = 0.06F * Mth.sin(gameTime * 0.24F + v * 0.9F);
                Vec3 vPos = inst.start.add(fwd.scale(dist)).add(right.scale(sway)).add(up.scale(sway * 0.5F));
                float vRad = baseR * (1.28F + 0.24F * Mth.sin(vt * (float) Math.PI));
                renderOrientedRing(matrix, vc, vPos, right, up,
                        vRad * 0.68F, vRad, 6, 0.902F, 0.875F, 0.812F, 0.86F * fade);
            }

            int soulOrbs = 8;
            for (int s = 0; s < soulOrbs; s++) {
                float rawT = (((gameTime * 0.16F) + (s / (float) soulOrbs)) % 1.0F);
                float st = 1.0F - rawT; // Streams INWARD from victim back into caster's health & Orbis Cell!
                double dist = st * len;
                float sAng = s * 2.39996F - gameTime * 0.34F;
                float orbitR = baseR * (0.45F + 1.15F * Mth.sin(st * (float) Math.PI));
                Vec3 soulPos = inst.start.add(fwd.scale(dist))
                        .add(right.scale(Mth.cos(sAng) * orbitR))
                        .add(up.scale(Mth.sin(sAng) * orbitR));
                float sAlpha = fade * Mth.sin(st * (float) Math.PI);
                renderOctahedron(matrix, vc, soulPos, baseR * 0.48F * (0.75F + 0.25F * sAlpha),
                        0.369F, 0.878F, 0.533F, 0.96F * sAlpha);
                renderCrossSegment(matrix, vc, soulPos, soulPos.add(fwd.scale(0.38D)),
                        baseR * 0.20F, 0.369F, 0.878F, 0.533F, 0.78F * sAlpha);
            }
        } else if (inst.essence == EssenceType.VOID) {
            // Section 6.11 Gravitational Tractor Beam (#41304E): stone-piercing deep cosmic violet ray,
            // pitch-black event-horizon filament core, 6 INWARD-pulling #C77DFF gravitational lensing rings,
            // and a hovering 3D mini-singularity suspension anchor at the beam endpoint!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.56F * fade, 10,
                    0.055F, 0.030F, 0.085F, 0.98F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.42F * fade, 12,
                    0.255F, 0.188F, 0.306F, 0.68F * fade, FULL_LIGHT);

            int helixSteps = Math.clamp((int) (len * 3.8D), 8, 44);
            for (int h = 0; h < 2; h++) {
                float hOff = (float) (Math.PI * h) + gameTime * 0.28F;
                Vec3 prevH = null;
                for (int i = 0; i <= helixSteps; i++) {
                    float t = i / (float) helixSteps;
                    double dist = t * len;
                    float coilR = baseR * (1.28F + 0.42F * Mth.sin(t * (float) Math.PI));
                    float ang = hOff - t * (float) (len * 2.2D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * coilR))
                            .add(up.scale(Mth.sin(ang) * coilR));
                    if (prevH != null) {
                        renderCrossSegment(matrix, vc, prevH, pt, baseR * 0.24F * fade,
                                (h == 0) ? 0.780F : 0.255F,
                                (h == 0) ? 0.490F : 0.188F,
                                (h == 0) ? 1.000F : 0.306F,
                                0.88F * fade);
                    }
                    prevH = pt;
                }
            }

            int tractorRings = 6;
            for (int c = 0; c < tractorRings; c++) {
                float rawT = (((gameTime * 0.16F) + (c / (float) tractorRings)) % 1.0F);
                float ct = 1.0F - rawT; // Races INWARD from suspended target back toward caster!
                double dist = ct * len;
                float cFlare = baseR * (1.35F + 1.25F * ct);
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                Vec3 cCenter = inst.start.add(fwd.scale(dist));
                renderOrientedRing(matrix, vc, cCenter, right, up,
                        cFlare * 0.76F, cFlare, 20, 0.780F, 0.490F, 1.000F, 0.90F * cAlpha);
                renderOrientedRing(matrix, vc, cCenter, right, up,
                        cFlare * 0.42F, cFlare * 0.62F, 18, 0.255F, 0.188F, 0.306F, 0.85F * cAlpha);
            }

            Vec3 endAnchor = inst.endOrDir.subtract(fwd.scale(0.05D));
            renderOctahedron(matrix, vc, endAnchor, baseR * 1.15F * fade, 0.055F, 0.030F, 0.085F, 0.98F * fade);
            renderOrientedRing(matrix, vc, endAnchor, right, up,
                    baseR * 1.45F * fade, baseR * 2.15F * fade, 22, 0.780F, 0.490F, 1.000F, 0.90F * fade);
        } else if (isAridDesertMateria(inst.essence)) {
            // Section 6.12 Sirocco Sandstream (#C2B28F): high-pressure jet of abrasive desert sand,
            // 3 fast-spiraling dune-wind helices, and 6 outward-racing sandstone grit shock-cones!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.62F * fade, 10,
                    0.961F, 0.894F, 0.745F, 0.96F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.48F * fade, 12,
                    0.761F, 0.698F, 0.561F, 0.68F * fade, FULL_LIGHT);

            int helixSteps = Math.clamp((int) (len * 4.0D), 8, 46);
            for (int h = 0; h < 3; h++) {
                float hOff = (float) (Math.PI * 2.0D * h / 3.0D) - gameTime * 0.46F;
                Vec3 prevH = null;
                for (int i = 0; i <= helixSteps; i++) {
                    float t = i / (float) helixSteps;
                    double dist = t * len;
                    float coilR = baseR * (1.24F + 0.52F * t);
                    float ang = hOff + t * (float) (len * 2.6D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * coilR))
                            .add(up.scale(Mth.sin(ang) * coilR));
                    if (prevH != null) {
                        boolean sunlit = (h == 0);
                        renderCrossSegment(matrix, vc, prevH, pt, baseR * 0.25F * fade,
                                sunlit ? 0.961F : 0.612F,
                                sunlit ? 0.894F : 0.478F,
                                sunlit ? 0.745F : 0.298F,
                                0.88F * fade);
                    }
                    prevH = pt;
                }
            }

            int gritCones = 6;
            for (int c = 0; c < gritCones; c++) {
                float ct = (((gameTime * 0.22F) + (c / (float) gritCones)) % 1.0F);
                double dist0 = ct * Math.max(0.2D, len - 0.35D);
                double dist1 = Math.min(len, dist0 + 0.42D);
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                renderOrientedFrustumBand(matrix, vc,
                        inst.start.add(fwd.scale(dist0)), inst.start.add(fwd.scale(dist1)),
                        right, up, baseR * 0.92F, baseR * (1.85F + 0.85F * ct), 18,
                        0.761F, 0.698F, 0.561F, 0.76F * cAlpha, 0.22F * cAlpha);
            }
        } else if (isGalvanicLightningMateria(inst.essence)) {
            // Section 6.13 Continuous Tesla Arc (#FFE600): blinding white-gold plasma filament,
            // 3 snapping jagged 3D lightning strands dancing along the beam & electric corona rings!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.48F * fade, 10,
                    1.00F, 1.00F, 0.94F, 0.99F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.18F * fade, 12,
                    1.00F, 0.902F, 0.00F, 0.72F * fade, FULL_LIGHT);

            int arcSteps = Math.clamp((int) (len * 3.6D), 8, 42);
            int frame = (int) (gameTime * 1.25F) + inst.seed * 13;
            for (int strand = 0; strand < 3; strand++) {
                Vec3 prevA = inst.start;
                for (int i = 1; i <= arcSteps; i++) {
                    float t = i / (float) arcSteps;
                    Vec3 pt;
                    if (i == arcSteps) {
                        pt = inst.endOrDir;
                    } else {
                        float env = Mth.sin(t * (float) Math.PI);
                        float jR = (hashFloat(frame + strand * 19, i * 5) - 0.5F) * baseR * 6.5F * env;
                        float jU = (hashFloat(frame + strand * 19, i * 5 + 2) - 0.5F) * baseR * 6.5F * env;
                        pt = inst.start.add(fwd.scale(t * len)).add(right.scale(jR)).add(up.scale(jU));
                    }
                    boolean cyan = (strand == 2);
                    renderCrossSegment(matrix, vc, prevA, pt, baseR * (strand == 0 ? 0.34F : 0.24F) * fade,
                            cyan ? 0.722F : 1.00F,
                            cyan ? 0.957F : (strand == 0 ? 0.99F : 0.902F),
                            cyan ? 1.00F : (strand == 0 ? 0.92F : 0.00F),
                            0.94F * fade);
                    prevA = pt;
                }
            }
        } else if (isMagmaCalderaMateria(inst.essence)) {
            // Section 6.14 Pyroclastic Lavastream (#B83C08): sustained torrent of viscous liquid magma,
            // incandescent #FFD15C lava core & tumbling dark obsidian/pumice slag globules boring forward!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.72F * fade, 12,
                    1.00F, 0.820F, 0.361F, 0.98F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.48F * fade, 14,
                    0.722F, 0.235F, 0.031F, 0.88F * fade, FULL_LIGHT);

            int globCount = Math.clamp((int) (len * 1.6D), 6, 26);
            for (int g = 0; g < globCount; g++) {
                float gt = ((g / (float) globCount) + (gameTime * 0.16F) % 1.0F) % 1.0F;
                double dist = gt * len;
                float gAng = gt * 11.0F + g * 1.9F;
                Vec3 gPos = inst.start.add(fwd.scale(dist))
                        .add(right.scale(Mth.cos(gAng) * baseR * 1.25F))
                        .add(up.scale(Mth.sin(gAng) * baseR * 1.25F));
                boolean crust = (g % 2 == 0);
                renderOctahedron(matrix, vc, gPos, baseR * (crust ? 0.62F : 0.48F) * fade,
                        crust ? 0.165F : 1.00F,
                        crust ? 0.090F : 0.820F,
                        crust ? 0.071F : 0.361F,
                        (crust ? 0.86F : 0.96F) * fade);
            }
        } else if (isTempestStormMateria(inst.essence)) {
            // Section 6.15 Gale-Cutter Torrent Beam (#429BA3): sustained high-pressure torrent of howling wind & cutting water droplets!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.62F * fade, 12,
                    0.847F, 0.980F, 1.000F, 0.98F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.38F * fade, 14,
                    0.259F, 0.608F, 0.639F, 0.84F * fade, FULL_LIGHT);

            int ringCount = Math.clamp((int) (len * 1.4D), 5, 22);
            for (int c = 0; c < ringCount; c++) {
                float ct = ((c / (float) ringCount) + (gameTime * 0.32F) % 1.0F) % 1.0F;
                double dist0 = ct * len;
                double dist1 = Math.min(len, dist0 + 0.38D);
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                renderOrientedFrustumBand(matrix, vc,
                        inst.start.add(fwd.scale(dist0)), inst.start.add(fwd.scale(dist1)),
                        right, up, baseR * 0.88F, baseR * (1.85F + 0.70F * ct), 18,
                        0.847F, 0.980F, 1.000F, 0.82F * cAlpha, 0.24F * cAlpha);
            }
        } else if (isPermafrostGlacialMateria(inst.essence)) {
            // Section 6.16 Absolute-Zero Cryo-Torrent Beam (#98DCF2): sustained pressurized stream of sub-zero liquid frost & ice slurry!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.58F * fade, 12,
                    0.88F, 1.00F, 1.00F, 0.98F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.34F * fade, 14,
                    0.596F, 0.863F, 0.949F, 0.82F * fade, FULL_LIGHT);

            int slurryBands = Math.clamp((int) (len * 1.3D), 5, 20);
            for (int b = 0; b < slurryBands; b++) {
                float bt = ((b / (float) slurryBands) + (gameTime * 0.24F) % 1.0F) % 1.0F;
                double dist0 = bt * len;
                double dist1 = Math.min(len, dist0 + 0.36D);
                float bAlpha = fade * Mth.sin(bt * (float) Math.PI);
                renderOrientedFrustumBand(matrix, vc,
                        inst.start.add(fwd.scale(dist0)), inst.start.add(fwd.scale(dist1)),
                        right, up, baseR * 0.85F, baseR * (1.72F + 0.55F * bt), 12,
                        0.173F, 0.510F, 0.788F, 0.80F * bAlpha, 0.22F * bAlpha);
            }
            for (int s = 0; s < 10; s++) {
                float st = (((gameTime * 0.20F) + (s / 10.0F)) % 1.0F);
                double dist = st * len;
                float sAng = s * 2.39996F - gameTime * 0.30F;
                float orbitR = baseR * (1.08F + 0.42F * Mth.sin(st * (float) Math.PI));
                Vec3 chunkPos = inst.start.add(fwd.scale(dist))
                        .add(right.scale(Mth.cos(sAng) * orbitR))
                        .add(up.scale(Mth.sin(sAng) * orbitR));
                float sAlpha = fade * Mth.sin(st * (float) Math.PI);
                boolean pale = (s % 2 == 0);
                renderOctahedron(matrix, vc, chunkPos, baseR * 0.46F * (0.75F + 0.25F * sAlpha),
                        pale ? 0.596F : 0.173F, pale ? 0.863F : 0.510F, pale ? 0.949F : 0.788F, 0.94F * sAlpha);
            }
            float endAlpha = channeledSlice ? (0.90F - progress * 0.20F) : (1.0F - progress);
            renderSnowflakeCrystal(matrix, vc, inst.endOrDir.subtract(fwd.scale(0.04D)), right, up,
                    baseR * 2.9F, gameTime * 0.18F, 0.92F * endAlpha);
        } else if (isOvergrowthRampartMateria(inst.essence)) {
            // Section 6.17 Verdant Growth Tether Ray (#1A5923): sustained emerald beam wrapped in 3 clinging earthen root tendrils!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.46F * fade, 10,
                    0.706F, 1.000F, 0.471F, 0.96F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.12F * fade, 12,
                    0.102F, 0.349F, 0.137F, 0.80F * fade, FULL_LIGHT);

            int rootSteps = Math.clamp((int) (len * 3.8D), 10, 38);
            for (int rIdx = 0; rIdx < 3; rIdx++) {
                float off = (float) (Math.PI * 2.0D * rIdx / 3.0D) - gameTime * 0.22F;
                Vec3 prevPt = null;
                for (int i = 0; i <= rootSteps; i++) {
                    float t = i / (float) rootSteps;
                    double dist = t * len;
                    float coilR = baseR * (1.35F + 0.35F * Mth.sin(t * (float) Math.PI));
                    float ang = off + t * (float) (len * 2.2D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * coilR))
                            .add(up.scale(Mth.sin(ang) * coilR));
                    if (prevPt != null) {
                        boolean woody = (rIdx == 0);
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.38F * fade,
                                woody ? 0.467F : 0.102F,
                                woody ? 0.196F : 0.349F,
                                woody ? 0.078F : 0.137F,
                                0.92F * fade);
                    }
                    prevPt = pt;
                }
            }
        } else if (isAbrasiveDustMateria(inst.essence)) {
            // Section 6.18 Abrasive Sandblasting Jet (#C2B28F): high-velocity mineral dust stream,
            // 3 pressurized chalk-sandstone grit helices & 6 forward-racing pneumatic wind-pressure collars!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.50F * fade, 10,
                    0.949F, 0.925F, 0.882F, 0.96F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.24F * fade, 12,
                    0.761F, 0.698F, 0.561F, 0.72F * fade, FULL_LIGHT);

            int gritSteps = Math.clamp((int) (len * 4.0D), 10, 42);
            for (int gIdx = 0; gIdx < 3; gIdx++) {
                float off = (float) (Math.PI * 2.0D * gIdx / 3.0D) - gameTime * 0.48F;
                Vec3 prevPt = null;
                for (int i = 0; i <= gritSteps; i++) {
                    float t = i / (float) gritSteps;
                    double dist = t * len;
                    float coilR = baseR * (1.18F + 0.36F * Mth.sin(t * (float) Math.PI));
                    float ang = off + t * (float) (len * 2.6D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * coilR))
                            .add(up.scale(Mth.sin(ang) * coilR));
                    if (prevPt != null) {
                        boolean chalkWhite = (gIdx == 0);
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.34F * fade,
                                chalkWhite ? 0.949F : 0.761F,
                                chalkWhite ? 0.925F : 0.698F,
                                chalkWhite ? 0.882F : 0.561F,
                                0.90F * fade);
                    }
                    prevPt = pt;
                }
            }

            for (int c = 0; c < 6; c++) {
                float ct = (((gameTime * 0.22F) + (c / 6.0F)) % 1.0F);
                double dist = ct * len;
                float cFlare = baseR * (1.35F + 0.75F * ct);
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        cFlare * 0.80F, cFlare, 18, 0.949F, 0.925F, 0.882F, 0.88F * cAlpha);
            }
        } else if (isSymbioticSporeMateria(inst.essence)) {
            // Section 6.19 Mycelial Inoculation Stream (#1A5923 / #A88654): living mycelial spore stream,
            // dual-tone green/gold hyphal tendrils & pulsing spore-cap inoculation rings!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.52F * fade, 10,
                    0.910F, 0.784F, 0.510F, 0.95F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.22F * fade, 12,
                    0.102F, 0.349F, 0.137F, 0.70F * fade, FULL_LIGHT);

            int hyphaSteps = Math.clamp((int) (len * 4.0D), 10, 42);
            for (int hIdx = 0; hIdx < 3; hIdx++) {
                float off = (float) (Math.PI * 2.0D * hIdx / 3.0D) - gameTime * 0.34F;
                Vec3 prevPt = null;
                for (int i = 0; i <= hyphaSteps; i++) {
                    float t = i / (float) hyphaSteps;
                    double dist = t * len;
                    float coilR = baseR * (1.16F + 0.38F * Mth.sin(t * (float) Math.PI * 2.0F));
                    float ang = off + t * (float) (len * 2.2D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * coilR))
                            .add(up.scale(Mth.sin(ang) * coilR));
                    if (prevPt != null) {
                        boolean goldHypha = (hIdx == 0);
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.32F * fade,
                                goldHypha ? 0.659F : 0.173F,
                                goldHypha ? 0.525F : 0.902F,
                                goldHypha ? 0.329F : 0.408F,
                                0.90F * fade);
                    }
                    prevPt = pt;
                }
            }
            for (int c = 0; c < 5; c++) {
                float ct = (((gameTime * 0.16F) + (c / 5.0F)) % 1.0F);
                double dist = ct * len;
                float cFlare = baseR * (1.30F + 0.65F * ct);
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        cFlare * 0.78F, cFlare, 18, 0.910F, 0.784F, 0.510F, 0.86F * cAlpha);
            }
        } else if (isBorealTaigaMateria(inst.essence)) {
            // Section 6.20 Glacial Amber Leeching Beam (#1A5923 / #98DCF2): sticky coniferous amber-sap core,
            // sub-zero frost-cyan helical tendrils & inward-siphoning rings pulling warmth back toward caster!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.50F * fade, 10,
                    0.920F, 0.710F, 0.260F, 0.96F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.18F * fade, 12,
                    0.102F, 0.349F, 0.137F, 0.72F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.48F * fade, 12,
                    0.596F, 0.863F, 0.949F, 0.42F * fade, FULL_LIGHT);

            int frostSteps = Math.clamp((int) (len * 4.0D), 10, 44);
            for (int hIdx = 0; hIdx < 3; hIdx++) {
                float off = (float) (Math.PI * 2.0D * hIdx / 3.0D) + gameTime * 0.36F;
                Vec3 prevPt = null;
                for (int i = 0; i <= frostSteps; i++) {
                    float t = i / (float) frostSteps;
                    double dist = t * len;
                    float coilR = baseR * (1.20F + 0.34F * Mth.sin(t * (float) Math.PI * 2.0F));
                    float ang = off + t * (float) (len * 2.1D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * coilR))
                            .add(up.scale(Mth.sin(ang) * coilR));
                    if (prevPt != null) {
                        boolean frostStrand = (hIdx % 2 == 0);
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.30F * fade,
                                frostStrand ? 0.596F : 0.102F,
                                frostStrand ? 0.863F : 0.349F,
                                frostStrand ? 0.949F : 0.137F,
                                0.92F * fade);
                    }
                    prevPt = pt;
                }
            }
            // Inward-siphoning rings flowing from target back into caster!
            for (int c = 0; c < 5; c++) {
                float ct = 1.0F - (((gameTime * 0.18F) + (c / 5.0F)) % 1.0F);
                double dist = ct * len;
                float cFlare = baseR * (1.25F + 0.60F * ct);
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        cFlare * 0.80F, cFlare, 18, 0.596F, 0.863F, 0.949F, 0.90F * cAlpha);
            }
        } else if (isMiracleOasisMateria(inst.essence)) {
            // Section 6.21 Subterranean Aquifer Stream (#1A5923 / #C2B28F / #38C6F4): pressurized jet of
            // sparkling desert springwater, succulent emerald vine helices & sunlit pollen rings + 15% life-siphon return!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.56F * fade, 12,
                    0.220F, 0.776F, 0.957F, 0.96F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.16F * fade, 12,
                    0.102F, 0.349F, 0.137F, 0.66F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.46F * fade, 12,
                    0.761F, 0.698F, 0.561F, 0.38F * fade, FULL_LIGHT);

            int vineSteps = Math.clamp((int) (len * 4.0D), 10, 44);
            for (int hIdx = 0; hIdx < 3; hIdx++) {
                float off = (float) (Math.PI * 2.0D * hIdx / 3.0D) - gameTime * 0.38F;
                Vec3 prevPt = null;
                for (int i = 0; i <= vineSteps; i++) {
                    float t = i / (float) vineSteps;
                    double dist = t * len;
                    float coilR = baseR * (1.18F + 0.30F * Mth.sin(t * (float) Math.PI * 2.0F));
                    float ang = off + t * (float) (len * 2.0D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * coilR))
                            .add(up.scale(Mth.sin(ang) * coilR));
                    if (prevPt != null) {
                        boolean pollenStrand = (hIdx == 2);
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.28F * fade,
                                pollenStrand ? 0.871F : 0.133F,
                                pollenStrand ? 0.788F : 0.471F,
                                pollenStrand ? 0.604F : 0.188F,
                                0.92F * fade);
                    }
                    prevPt = pt;
                }
            }
            // Outward springwater surge rings + inward emerald 15% life-mending rings!
            for (int c = 0; c < 6; c++) {
                boolean siphonReturn = (c % 2 == 1);
                float raw = ((gameTime * 0.20F) + (c / 6.0F)) % 1.0F;
                float ct = siphonReturn ? (1.0F - raw) : raw;
                double dist = ct * len;
                float cFlare = baseR * (1.22F + 0.55F * ct);
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        cFlare * 0.80F, cFlare, 18,
                        siphonReturn ? 0.133F : 0.871F,
                        siphonReturn ? 0.840F : 0.788F,
                        siphonReturn ? 0.360F : 0.604F,
                        0.90F * cAlpha);
            }
        } else if (isCellularVitaeMateria(inst.essence)) {
            // Section 6.22 Cardiovascular Life-Tether (#FF6B9D / #FFD1E3): pulsing heartbeat beam,
            // intertwined vascular double-helix & 25% life-siphon return rings!
            float beat = 0.88F + 0.20F * Mth.abs(Mth.sin(gameTime * 0.32F));
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.56F * beat * fade, 12,
                    1.000F, 0.820F, 0.890F, 0.96F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.18F * beat * fade, 12,
                    1.000F, 0.420F, 0.616F, 0.72F * fade, FULL_LIGHT);

            int helixSteps = Math.clamp((int) (len * 4.2D), 10, 46);
            for (int hIdx = 0; hIdx < 2; hIdx++) {
                float off = hIdx * (float) Math.PI - gameTime * 0.42F;
                Vec3 prevPt = null;
                for (int i = 0; i <= helixSteps; i++) {
                    float t = i / (float) helixSteps;
                    double dist = t * len;
                    float coilR = baseR * (1.24F + 0.26F * Mth.sin(t * (float) Math.PI * 2.0F));
                    float ang = off + t * (float) (len * 2.2D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * coilR))
                            .add(up.scale(Mth.sin(ang) * coilR));
                    if (prevPt != null) {
                        boolean pearl = (hIdx == 1);
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.28F * fade,
                                1.000F, pearl ? 0.820F : 0.420F, pearl ? 0.890F : 0.616F, 0.94F * fade);
                    }
                    prevPt = pt;
                }
            }
            for (int c = 0; c < 6; c++) {
                boolean siphonInward = (c % 2 == 0);
                float raw = ((gameTime * 0.22F) + (c / 6.0F)) % 1.0F;
                float ct = siphonInward ? (1.0F - raw) : raw;
                double dist = ct * len;
                float cFlare = baseR * (1.28F + 0.62F * ct) * beat;
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        cFlare * 0.80F, cFlare, 18,
                        1.000F, siphonInward ? 0.420F : 0.820F, siphonInward ? 0.616F : 0.890F, 0.92F * cAlpha);
            }
        } else if (isSanguineBloodMateria(inst.essence)) {
            // Section 6.23 Sanguine Exsanguination Tether (#8A0303 / #FF384C): writhing crimson blood stream,
            // 3 capillary tendrils & inward-flowing 35% life-siphon rings!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.54F * fade, 10,
                    1.000F, 0.220F, 0.298F, 0.95F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.18F * fade, 12,
                    0.541F, 0.012F, 0.012F, 0.82F * fade, FULL_LIGHT);

            int tendrilSteps = Math.clamp((int) (len * 4.4D), 10, 48);
            for (int tIdx = 0; tIdx < 3; tIdx++) {
                float off = (float) (Math.PI * 2.0D * tIdx / 3.0D) + gameTime * 0.44F;
                Vec3 prevPt = null;
                for (int i = 0; i <= tendrilSteps; i++) {
                    float t = i / (float) tendrilSteps;
                    double dist = t * len;
                    float coilR = baseR * (1.20F + 0.36F * Mth.sin(t * (float) Math.PI * 2.5F + gameTime * 0.2F));
                    float ang = off + t * (float) (len * 2.3D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * coilR))
                            .add(up.scale(Mth.sin(ang) * coilR));
                    if (prevPt != null) {
                        boolean arterial = (tIdx == 0);
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.30F * fade,
                                arterial ? 1.000F : 0.541F,
                                arterial ? 0.220F : 0.012F,
                                arterial ? 0.298F : 0.012F,
                                0.94F * fade);
                    }
                    prevPt = pt;
                }
            }
            for (int c = 0; c < 6; c++) {
                float ct = 1.0F - (((gameTime * 0.22F) + (c / 6.0F)) % 1.0F);
                double dist = ct * len;
                float cFlare = baseR * (1.24F + 0.60F * ct);
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        cFlare * 0.80F, cFlare, 18,
                        0.541F, 0.012F, 0.012F, 0.92F * cAlpha);
            }
        } else if (isFossilAmberMateria(inst.essence)) {
            // Section 6.25 Fossilizing Stasis Torrent (#FF6B9D / #773214 / #F29B2E): sustained thick stream of
            // molten golden tree resin & crystallizing stasis rings!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.62F * fade, 10,
                    1.000F, 0.420F, 0.616F, 0.94F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.28F * fade, 12,
                    0.949F, 0.608F, 0.180F, 0.72F * fade, FULL_LIGHT);

            int resinSteps = Math.clamp((int) (len * 4.0D), 10, 44);
            for (int rIdx = 0; rIdx < 3; rIdx++) {
                float off = (float) (Math.PI * 2.0D * rIdx / 3.0D) - gameTime * 0.28F;
                Vec3 prevPt = null;
                for (int i = 0; i <= resinSteps; i++) {
                    float t = i / (float) resinSteps;
                    double dist = t * len;
                    float coilR = baseR * (1.22F + 0.30F * Mth.sin(t * (float) Math.PI * 2.0F + gameTime * 0.16F));
                    float ang = off + t * (float) (len * 1.9D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * coilR))
                            .add(up.scale(Mth.sin(ang) * coilR));
                    if (prevPt != null) {
                        boolean rose = (rIdx == 0);
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.32F * fade,
                                rose ? 1.000F : 0.467F,
                                rose ? 0.420F : 0.196F,
                                rose ? 0.616F : 0.078F,
                                0.92F * fade);
                    }
                    prevPt = pt;
                }
            }
            for (int c = 0; c < 6; c++) {
                float ct = ((gameTime * 0.16F) + (c / 6.0F)) % 1.0F;
                double dist = ct * len;
                float cFlare = baseR * (1.28F + 0.56F * ct);
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        cFlare * 0.80F, cFlare, 6,
                        0.949F, 0.608F, 0.180F, 0.90F * cAlpha);
            }
        } else if (isParadoxEclipseMateria(inst.essence)) {
            // Section 6.26 Umbral Inversion Jet (#FFEA78 / #211A21): stream of paradox shadow-light with a
            // pitch-black core & blazing golden coronal rings!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.68F * fade, 10,
                    0.085F, 0.065F, 0.090F, 0.97F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.18F * fade, 12,
                    1.000F, 0.918F, 0.471F, 0.64F * fade, FULL_LIGHT);

            int eclipseSteps = Math.clamp((int) (len * 4.4D), 10, 48);
            for (int eIdx = 0; eIdx < 3; eIdx++) {
                float off = (float) (Math.PI * 2.0D * eIdx / 3.0D) + gameTime * 0.52F;
                Vec3 prevPt = null;
                for (int i = 0; i <= eclipseSteps; i++) {
                    float t = i / (float) eclipseSteps;
                    double dist = t * len;
                    float coilR = baseR * (1.26F + 0.34F * Mth.sin(t * (float) Math.PI * 2.5F - gameTime * 0.22F));
                    float ang = off + t * (float) (len * 2.3D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * coilR))
                            .add(up.scale(Mth.sin(ang) * coilR));
                    if (prevPt != null) {
                        boolean corona = (eIdx != 1);
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.28F * fade,
                                corona ? 1.000F : 0.129F,
                                corona ? 0.918F : 0.102F,
                                corona ? 0.471F : 0.129F,
                                0.95F * fade);
                    }
                    prevPt = pt;
                }
            }
            for (int c = 0; c < 6; c++) {
                float ct = ((gameTime * 0.22F) + (c / 6.0F)) % 1.0F;
                double dist = ct * len;
                float cFlare = baseR * (1.30F + 0.62F * ct);
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        cFlare * 0.80F, cFlare, 20,
                        1.000F, 0.918F, 0.471F, 0.94F * cAlpha);
            }
        } else if (isCadaverousBlightMateria(inst.essence)) {
            // Section 6.27 Necrotic Bramble Tether Beam (#4D6453 / #34CC48): writhing tendril of dark thorns,
            // rotting mold & inward-pulling bramble rings!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.56F * fade, 10,
                    0.204F, 0.800F, 0.282F, 0.94F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.18F * fade, 12,
                    0.302F, 0.392F, 0.325F, 0.78F * fade, FULL_LIGHT);

            int brambleSteps = Math.clamp((int) (len * 4.4D), 10, 48);
            for (int bIdx = 0; bIdx < 3; bIdx++) {
                float off = (float) (Math.PI * 2.0D * bIdx / 3.0D) - gameTime * 0.38F;
                Vec3 prevPt = null;
                for (int i = 0; i <= brambleSteps; i++) {
                    float t = i / (float) brambleSteps;
                    double dist = t * len;
                    float coilR = baseR * (1.24F + 0.32F * Mth.sin(t * (float) Math.PI * 2.5F + gameTime * 0.18F));
                    float ang = off + t * (float) (len * 2.4D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * coilR))
                            .add(up.scale(Mth.sin(ang) * coilR));
                    if (prevPt != null) {
                        boolean bone = (bIdx == 0);
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.32F * fade,
                                bone ? 0.902F : 0.204F,
                                bone ? 0.875F : 0.800F,
                                bone ? 0.812F : 0.282F,
                                0.94F * fade);
                    }
                    prevPt = pt;
                }
            }
            for (int c = 0; c < 6; c++) {
                float ct = 1.0F - (((gameTime * 0.20F) + (c / 6.0F)) % 1.0F);
                double dist = ct * len;
                float cFlare = baseR * (1.24F + 0.56F * ct);
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        cFlare * 0.80F, cFlare, 16,
                        0.204F, 0.800F, 0.282F, 0.90F * cAlpha);
            }
        } else if (isCosmicAstralMateria(inst.essence)) {
            // Section 6.28 Pulsar Tractor Ray (#41304E / #FFE600): crackling cosmic violet & electric gold tractor beam
            // with inward-pulling gravity rings!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.54F * fade, 10,
                    1.000F, 0.902F, 0.000F, 0.96F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.22F * fade, 12,
                    0.255F, 0.188F, 0.306F, 0.82F * fade, FULL_LIGHT);

            int arcSteps = Math.clamp((int) (len * 4.2D), 10, 46);
            for (int aIdx = 0; aIdx < 3; aIdx++) {
                float off = (float) (Math.PI * 2.0D * aIdx / 3.0D) + gameTime * 0.55F;
                Vec3 prevPt = null;
                for (int i = 0; i <= arcSteps; i++) {
                    float t = i / (float) arcSteps;
                    double dist = t * len;
                    float jag = ((i & 1) == 0 ? 1.35F : 0.85F) * baseR;
                    float ang = off + t * (float) (len * 2.6D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * jag))
                            .add(up.scale(Mth.sin(ang) * jag));
                    if (prevPt != null) {
                        boolean gold = (aIdx != 2);
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.30F * fade,
                                gold ? 1.000F : 0.255F,
                                gold ? 0.902F : 0.188F,
                                gold ? 0.000F : 0.306F,
                                0.96F * fade);
                    }
                    prevPt = pt;
                }
            }
            for (int c = 0; c < 6; c++) {
                float ct = 1.0F - (((gameTime * 0.24F) + (c / 6.0F)) % 1.0F);
                double dist = ct * len;
                float cFlare = baseR * (1.28F + 0.62F * ct);
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        cFlare * 0.82F, cFlare, 20,
                        1.000F, 0.902F, 0.000F, 0.94F * cAlpha);
            }
        } else if (isPhantasmalSoulfireMateria(inst.essence)) {
            // Section 6.29 Phantasmal Soul-Torch Jet (#1DF0E5 / #0D6469): roaring stream of cyan soulflames & siphoning soul wisps!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.56F * fade, 10,
                    0.88F, 1.00F, 0.99F, 0.96F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.24F * fade, 12,
                    0.114F, 0.941F, 0.898F, 0.84F * fade, FULL_LIGHT);

            int flameSteps = Math.clamp((int) (len * 4.2D), 10, 46);
            for (int fIdx = 0; fIdx < 3; fIdx++) {
                float off = (float) (Math.PI * 2.0D * fIdx / 3.0D) - gameTime * 0.48F;
                Vec3 prevPt = null;
                for (int i = 0; i <= flameSteps; i++) {
                    float t = i / (float) flameSteps;
                    double dist = t * len;
                    float coilR = baseR * (1.20F + 0.36F * Mth.sin(t * (float) Math.PI * 2.5F - gameTime * 0.24F));
                    float ang = off + t * (float) (len * 2.3D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * coilR))
                            .add(up.scale(Mth.sin(ang) * coilR));
                    if (prevPt != null) {
                        boolean cyan = (fIdx != 2);
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.34F * fade,
                                cyan ? 0.114F : 0.051F,
                                cyan ? 0.941F : 0.392F,
                                cyan ? 0.898F : 0.412F,
                                0.94F * fade);
                    }
                    prevPt = pt;
                }
            }
            for (int c = 0; c < 6; c++) {
                float ct = 1.0F - (((gameTime * 0.22F) + (c / 6.0F)) % 1.0F);
                double dist = ct * len;
                float cFlare = baseR * (1.26F + 0.58F * ct);
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        cFlare * 0.80F, cFlare, 18,
                        0.114F, 0.941F, 0.898F, 0.92F * cAlpha);
            }
        } else if (isScaldingVaporMateria(inst.essence)) {
            // Section 6.30 Scalding Steam Jetstream (#E0F7FA / #30A9B0): high-pressure superheated boiling steam jet!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.58F * fade, 10,
                    0.878F, 0.969F, 0.980F, 0.96F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.28F * fade, 12,
                    0.188F, 0.663F, 0.690F, 0.78F * fade, FULL_LIGHT);

            int steamSteps = Math.clamp((int) (len * 4.0D), 10, 44);
            for (int sIdx = 0; sIdx < 3; sIdx++) {
                float off = (float) (Math.PI * 2.0D * sIdx / 3.0D) + gameTime * 0.52F;
                Vec3 prevPt = null;
                for (int i = 0; i <= steamSteps; i++) {
                    float t = i / (float) steamSteps;
                    double dist = t * len;
                    float coilR = baseR * (1.18F + 0.42F * t);
                    float ang = off + t * (float) (len * 2.4D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * coilR))
                            .add(up.scale(Mth.sin(ang) * coilR));
                    if (prevPt != null) {
                        boolean white = (sIdx != 2);
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.34F * fade,
                                white ? 0.878F : 0.188F,
                                white ? 0.969F : 0.663F,
                                white ? 0.980F : 0.690F,
                                0.92F * fade);
                    }
                    prevPt = pt;
                }
            }
            for (int c = 0; c < 6; c++) {
                float ct = ((gameTime * 0.25F) + (c / 6.0F)) % 1.0F;
                double dist = ct * len;
                float cFlare = baseR * (1.22F + 0.68F * ct);
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        cFlare * 0.82F, cFlare, 20,
                        0.878F, 0.969F, 0.980F, 0.90F * cAlpha);
            }
        } else if (isAntimatterNullRMateria(inst.essence)) {
            // Section 6.31 Event Horizon Tractor Beam (#FFFFFF / #41304E): blinding white anti-light beam with inward-pulling violet tractor hoops!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.62F * fade, 10,
                    1.000F, 1.000F, 1.000F, 0.99F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.24F * fade, 12,
                    0.255F, 0.188F, 0.306F, 0.82F * fade, FULL_LIGHT);

            int tractorSteps = Math.clamp((int) (len * 4.0D), 10, 44);
            for (int hIdx = 0; hIdx < 3; hIdx++) {
                float off = (float) (Math.PI * 2.0D * hIdx / 3.0D) - gameTime * 0.56F;
                Vec3 prevPt = null;
                for (int i = 0; i <= tractorSteps; i++) {
                    float t = i / (float) tractorSteps;
                    double dist = t * len;
                    float coilR = baseR * (1.08F + 0.45F * (1.0F - t));
                    float ang = off + t * (float) (len * 2.3D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * coilR))
                            .add(up.scale(Mth.sin(ang) * coilR));
                    if (prevPt != null) {
                        boolean white = (hIdx != 2);
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.34F * fade,
                                white ? 1.000F : 0.255F,
                                white ? 1.000F : 0.188F,
                                white ? 1.000F : 0.306F,
                                0.95F * fade);
                    }
                    prevPt = pt;
                }
            }
            for (int c = 0; c < 6; c++) {
                float ct = 1.0F - (((gameTime * 0.28F) + (c / 6.0F)) % 1.0F);
                double dist = ct * len;
                float cFlare = baseR * (1.18F + 0.72F * ct);
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                boolean whiteRing = (c % 2 == 0);
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        cFlare * 0.80F, cFlare, 22,
                        whiteRing ? 1.000F : 0.255F,
                        whiteRing ? 1.000F : 0.188F,
                        whiteRing ? 1.000F : 0.306F,
                        0.94F * cAlpha);
            }
        } else if (isAbyssalNullUMateria(inst.essence)) {
            // Section 6.32 Void-Umbral Disintegration Stream (#110B17 / #41304E): pitch-black disintegrating ray with inward-pulling violet rings!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.64F * fade, 10,
                    0.067F, 0.043F, 0.090F, 0.99F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.26F * fade, 12,
                    0.255F, 0.188F, 0.306F, 0.84F * fade, FULL_LIGHT);

            int streamSteps = Math.clamp((int) (len * 4.0D), 10, 44);
            for (int hIdx = 0; hIdx < 3; hIdx++) {
                float off = (float) (Math.PI * 2.0D * hIdx / 3.0D) - gameTime * 0.52F;
                Vec3 prevPt = null;
                for (int i = 0; i <= streamSteps; i++) {
                    float t = i / (float) streamSteps;
                    double dist = t * len;
                    float coilR = baseR * (1.10F + 0.44F * (1.0F - t));
                    float ang = off + t * (float) (len * 2.3D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * coilR))
                            .add(up.scale(Mth.sin(ang) * coilR));
                    if (prevPt != null) {
                        boolean dark = (hIdx != 2);
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.34F * fade,
                                dark ? 0.067F : 0.255F,
                                dark ? 0.043F : 0.188F,
                                dark ? 0.090F : 0.306F,
                                0.96F * fade);
                    }
                    prevPt = pt;
                }
            }
            for (int c = 0; c < 6; c++) {
                float ct = 1.0F - (((gameTime * 0.26F) + (c / 6.0F)) % 1.0F);
                double dist = ct * len;
                float cFlare = baseR * (1.16F + 0.70F * ct);
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                boolean darkRing = (c % 2 == 0);
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        cFlare * 0.80F, cFlare, 22,
                        darkRing ? 0.067F : 0.255F,
                        darkRing ? 0.043F : 0.188F,
                        darkRing ? 0.090F : 0.306F,
                        0.95F * cAlpha);
            }
        } else if (isDaybreakDawnMateria(inst.essence)) {
            // Section 6.33 Pranic Solar Lance (#FFEA78 / #A88654): concentrated sunrise lance wrapped in atmospheric gale slipstreams!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.60F * fade, 10,
                    1.000F, 0.918F, 0.471F, 0.99F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.22F * fade, 12,
                    0.659F, 0.525F, 0.329F, 0.78F * fade, FULL_LIGHT);

            int lanceSteps = Math.clamp((int) (len * 4.0D), 10, 44);
            for (int hIdx = 0; hIdx < 3; hIdx++) {
                float off = (float) (Math.PI * 2.0D * hIdx / 3.0D) + gameTime * 0.56F;
                Vec3 prevPt = null;
                for (int i = 0; i <= lanceSteps; i++) {
                    float t = i / (float) lanceSteps;
                    double dist = t * len;
                    float coilR = baseR * (1.14F + 0.40F * t);
                    float ang = off + t * (float) (len * 2.4D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * coilR))
                            .add(up.scale(Mth.sin(ang) * coilR));
                    if (prevPt != null) {
                        boolean sunrise = (hIdx != 2);
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.34F * fade,
                                sunrise ? 1.000F : 0.659F,
                                sunrise ? 0.918F : 0.525F,
                                sunrise ? 0.471F : 0.329F,
                                0.95F * fade);
                    }
                    prevPt = pt;
                }
            }
            for (int c = 0; c < 6; c++) {
                float ct = ((gameTime * 0.28F) + (c / 6.0F)) % 1.0F;
                double dist = ct * len;
                float cFlare = baseR * (1.20F + 0.68F * ct);
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                boolean sunriseRing = (c % 2 == 0);
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        cFlare * 0.80F, cFlare, 22,
                        sunriseRing ? 1.000F : 0.659F,
                        sunriseRing ? 0.918F : 0.525F,
                        sunriseRing ? 0.471F : 0.329F,
                        0.94F * cAlpha);
            }
        } else if (isHadopelagicAbyssMateria(inst.essence)) {
            // Section 6.34 Hydrostatic Trench Torrent Beam (#211A21 / #225C7C): pressurized ink-black oceanic stream & trench spirals!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.66F * fade, 10,
                    0.129F, 0.102F, 0.129F, 0.99F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.28F * fade, 12,
                    0.133F, 0.361F, 0.486F, 0.80F * fade, FULL_LIGHT);

            int trenchSteps = Math.clamp((int) (len * 4.0D), 10, 44);
            for (int hIdx = 0; hIdx < 3; hIdx++) {
                float off = (float) (Math.PI * 2.0D * hIdx / 3.0D) - gameTime * 0.50F;
                Vec3 prevPt = null;
                for (int i = 0; i <= trenchSteps; i++) {
                    float t = i / (float) trenchSteps;
                    double dist = t * len;
                    float coilR = baseR * (1.22F + 0.35F * (1.0F - t));
                    float ang = off + t * (float) (len * 2.6D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * coilR))
                            .add(up.scale(Mth.sin(ang) * coilR));
                    if (prevPt != null) {
                        boolean ink = (hIdx != 2);
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.36F * fade,
                                ink ? 0.129F : 0.133F,
                                ink ? 0.102F : 0.361F,
                                ink ? 0.129F : 0.486F,
                                0.96F * fade);
                    }
                    prevPt = pt;
                }
            }
            for (int c = 0; c < 6; c++) {
                float ct = ((gameTime * 0.24F) + (c / 6.0F)) % 1.0F;
                double dist = ct * len;
                float cFlare = baseR * (1.26F + 0.60F * ct);
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                boolean inkRing = (c % 2 == 0);
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        cFlare * 0.80F, cFlare, 22,
                        inkRing ? 0.129F : 0.133F,
                        inkRing ? 0.102F : 0.361F,
                        inkRing ? 0.129F : 0.486F,
                        0.95F * cAlpha);
            }
        } else if (isGildedAegisMateria(inst.essence)) {
            // Section 6.35 Citadel Bastion Tether Beam (#FFEA78 / #773214): radiant-earthen forcefield beam & octagonal bastion rings!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.64F * fade, 8,
                    1.000F, 0.918F, 0.471F, 0.99F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.26F * fade, 8,
                    0.467F, 0.196F, 0.078F, 0.82F * fade, FULL_LIGHT);

            for (int c = 0; c < 7; c++) {
                float ct = ((gameTime * 0.22F) + (c / 7.0F)) % 1.0F;
                double dist = ct * len;
                float cFlare = baseR * (1.35F + 0.65F * ct);
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                boolean goldRing = (c % 2 == 0);
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        cFlare * 0.78F, cFlare, 8,
                        goldRing ? 1.000F : 0.467F,
                        goldRing ? 0.918F : 0.196F,
                        goldRing ? 0.471F : 0.078F,
                        0.96F * cAlpha);
            }
        } else if (isBorealisAuroraMateria(inst.essence)) {
            // Section 6.36 Borealis Prismatic Spectrum Ray (#FFEA78 / #98DCF2): dancing iridescent northern-light ribbons & freezing air!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.56F * fade, 10,
                    1.000F, 0.918F, 0.471F, 0.98F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.18F * fade, 12,
                    0.596F, 0.863F, 0.949F, 0.78F * fade, FULL_LIGHT);

            int ribbonSteps = Math.clamp((int) (len * 4.2D), 10, 48);
            for (int rIdx = 0; rIdx < 3; rIdx++) {
                float off = (float) (Math.PI * 2.0D * rIdx / 3.0D) + gameTime * 0.48F;
                boolean gold = (rIdx != 2);
                Vec3 prevPt = null;
                for (int i = 0; i <= ribbonSteps; i++) {
                    float t = i / (float) ribbonSteps;
                    double dist = t * len;
                    float waveR = baseR * (1.16F + 0.44F * Mth.sin(t * 9.0F - gameTime * 0.35F + rIdx));
                    float ang = off + t * (float) (len * 1.8D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * waveR))
                            .add(up.scale(Mth.sin(ang) * waveR));
                    if (prevPt != null) {
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.36F * fade,
                                gold ? 1.000F : 0.596F,
                                gold ? 0.918F : 0.863F,
                                gold ? 0.471F : 0.949F,
                                0.94F * fade);
                    }
                    prevPt = pt;
                }
            }
            for (int c = 0; c < 6; c++) {
                float ct = ((gameTime * 0.24F) + (c / 6.0F)) % 1.0F;
                double dist = ct * len;
                float cFlare = baseR * (1.25F + 0.65F * ct);
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                boolean goldRing = (c % 2 == 0);
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        cFlare * 0.80F, cFlare, 6,
                        goldRing ? 1.000F : 0.596F,
                        goldRing ? 0.918F : 0.863F,
                        goldRing ? 0.471F : 0.949F,
                        0.95F * cAlpha);
            }
        } else if (isPhantasmMirageMateria(inst.essence)) {
            // Section 6.37 Phantasm Mirage Tether Beam (#FFEA78 / #C2B28F): shimmering golden tether of heat-haze distortion & 50%-redirect mirror hoops!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.54F * fade, 10,
                    1.000F, 0.918F, 0.471F, 0.96F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.22F * fade, 12,
                    0.761F, 0.698F, 0.561F, 0.68F * fade, FULL_LIGHT);

            int hazeSteps = Math.clamp((int) (len * 4.0D), 10, 44);
            for (int hIdx = 0; hIdx < 2; hIdx++) {
                float off = hIdx * (float) Math.PI + gameTime * 0.42F;
                boolean gold = (hIdx == 0);
                Vec3 prevPt = null;
                for (int i = 0; i <= hazeSteps; i++) {
                    float t = i / (float) hazeSteps;
                    double dist = t * len;
                    float waveR = baseR * (1.18F + 0.40F * Mth.sin(t * 10.0F - gameTime * 0.40F + hIdx));
                    float ang = off + t * (float) (len * 2.1D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * waveR))
                            .add(up.scale(Mth.sin(ang) * waveR));
                    if (prevPt != null) {
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.34F * fade,
                                gold ? 1.000F : 0.761F,
                                gold ? 0.918F : 0.698F,
                                gold ? 0.471F : 0.561F,
                                0.90F * fade);
                    }
                    prevPt = pt;
                }
            }
            for (int c = 0; c < 6; c++) {
                float ct = ((gameTime * 0.25F) + (c / 6.0F)) % 1.0F;
                double dist = ct * len;
                float cFlare = baseR * (1.28F + 0.62F * ct);
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                boolean goldRing = (c % 2 == 0);
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        cFlare * 0.80F, cFlare, 20,
                        goldRing ? 1.000F : 0.761F,
                        goldRing ? 0.918F : 0.698F,
                        goldRing ? 0.471F : 0.561F,
                        0.92F * cAlpha);
            }
        } else if (isIncorporealWraithMateria(inst.essence)) {
            // Section 6.38 Incorporeal Soul-Siphon Tether Beam (#41304E / #4D6453): wall-phasing ethereal tether of spectral tendrils & void mist!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.52F * fade, 10,
                    0.302F, 0.392F, 0.325F, 0.96F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.24F * fade, 12,
                    0.255F, 0.188F, 0.306F, 0.74F * fade, FULL_LIGHT);

            int tendrilSteps = Math.clamp((int) (len * 4.0D), 10, 44);
            for (int tIdx = 0; tIdx < 2; tIdx++) {
                float off = tIdx * (float) Math.PI - gameTime * 0.38F;
                boolean moss = (tIdx == 0);
                Vec3 prevPt = null;
                for (int i = 0; i <= tendrilSteps; i++) {
                    float t = i / (float) tendrilSteps;
                    double dist = t * len;
                    float waveR = baseR * (1.22F + 0.38F * Mth.sin(t * 9.5F + gameTime * 0.45F + tIdx));
                    float ang = off + t * (float) (len * 2.2D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * waveR))
                            .add(up.scale(Mth.sin(ang) * waveR));
                    if (prevPt != null) {
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.36F * fade,
                                moss ? 0.302F : 0.255F,
                                moss ? 0.392F : 0.188F,
                                moss ? 0.325F : 0.306F,
                                0.90F * fade);
                    }
                    prevPt = pt;
                }
            }
            // Inward-flowing soul-siphon rings traveling from victim back toward caster!
            for (int c = 0; c < 6; c++) {
                float ct = 1.0F - (((gameTime * 0.24F) + (c / 6.0F)) % 1.0F);
                double dist = ct * len;
                float cFlare = baseR * (1.15F + 0.55F * ct);
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                boolean mossRing = (c % 2 == 0);
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        cFlare * 0.78F, cFlare, 18,
                        mossRing ? 0.302F : 0.255F,
                        mossRing ? 0.392F : 0.188F,
                        mossRing ? 0.325F : 0.306F,
                        0.92F * cAlpha);
            }
        } else if (isSepulchralBarrowMateria(inst.essence)) {
            // Section 6.39 Barrow-Tomb Necrotic Tether Beam (#773214 / #4D6453 + #E6DFCF bone): grasping skeletal roots & churning grave dirt!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.56F * fade, 8,
                    0.467F, 0.196F, 0.078F, 0.96F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.24F * fade, 10,
                    0.302F, 0.392F, 0.325F, 0.72F * fade, FULL_LIGHT);

            int rootSteps = Math.clamp((int) (len * 4.0D), 10, 44);
            for (int rIdx = 0; rIdx < 2; rIdx++) {
                float off = rIdx * (float) Math.PI + gameTime * 0.34F;
                boolean bone = (rIdx == 0);
                Vec3 prevPt = null;
                for (int i = 0; i <= rootSteps; i++) {
                    float t = i / (float) rootSteps;
                    double dist = t * len;
                    float waveR = baseR * (1.20F + 0.36F * Mth.sin(t * 9.0F - gameTime * 0.36F + rIdx));
                    float ang = off + t * (float) (len * 2.1D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * waveR))
                            .add(up.scale(Mth.sin(ang) * waveR));
                    if (prevPt != null) {
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.38F * fade,
                                bone ? 0.902F : 0.302F,
                                bone ? 0.875F : 0.392F,
                                bone ? 0.812F : 0.325F,
                                0.92F * fade);
                    }
                    prevPt = pt;
                }
            }
            for (int c = 0; c < 6; c++) {
                float ct = ((gameTime * 0.22F) + (c / 6.0F)) % 1.0F;
                double dist = ct * len;
                float cFlare = baseR * (1.26F + 0.54F * ct);
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                boolean granite = (c % 2 == 0);
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        cFlare * 0.78F, cFlare, 8,
                        granite ? 0.467F : 0.302F,
                        granite ? 0.196F : 0.392F,
                        granite ? 0.078F : 0.325F,
                        0.92F * cAlpha);
            }
        } else if (isCrematingPyreMateria(inst.essence)) {
            // Section 6.40 Cremating Wildfire Jetstream Beam (#773214 / #FF4500): roaring coal-fire jet & spiraling charred ember ribbons!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.58F * fade, 8,
                    1.000F, 0.820F, 0.361F, 0.98F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.28F * fade, 10,
                    1.000F, 0.271F, 0.000F, 0.82F * fade, FULL_LIGHT);

            int jetSteps = Math.clamp((int) (len * 4.0D), 10, 44);
            for (int rIdx = 0; rIdx < 2; rIdx++) {
                float off = rIdx * (float) Math.PI + gameTime * 0.48F;
                boolean coal = (rIdx == 1);
                Vec3 prevPt = null;
                for (int i = 0; i <= jetSteps; i++) {
                    float t = i / (float) jetSteps;
                    double dist = t * len;
                    float waveR = baseR * (1.18F + 0.38F * Mth.sin(t * 10.0F - gameTime * 0.52F + rIdx));
                    float ang = off + t * (float) (len * 2.4D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * waveR))
                            .add(up.scale(Mth.sin(ang) * waveR));
                    if (prevPt != null) {
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.38F * fade,
                                coal ? 0.467F : 1.000F,
                                coal ? 0.196F : 0.271F,
                                coal ? 0.078F : 0.000F,
                                0.95F * fade);
                    }
                    prevPt = pt;
                }
            }
            for (int c = 0; c < 6; c++) {
                float ct = ((gameTime * 0.30F) + (c / 6.0F)) % 1.0F;
                double dist = ct * len;
                float cFlare = baseR * (1.28F + 0.62F * ct);
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                boolean flame = (c % 2 == 0);
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        cFlare * 0.78F, cFlare, 10,
                        flame ? 1.000F : 0.467F,
                        flame ? 0.271F : 0.196F,
                        flame ? 0.000F : 0.078F,
                        0.94F * cAlpha);
            }
        } else if (isTwilightPenumbraMateria(inst.essence)) {
            // Section 6.41 Penumbral Event-Lensing Ray (#FFEA78 / #281E3C): light-bending twilight core & high-contrast coronal lensing rings!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.56F * fade, 8,
                    0.157F, 0.118F, 0.235F, 0.98F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.25F * fade, 10,
                    1.000F, 0.918F, 0.471F, 0.80F * fade, FULL_LIGHT);

            int lensSteps = Math.clamp((int) (len * 4.0D), 10, 44);
            for (int rIdx = 0; rIdx < 2; rIdx++) {
                float off = rIdx * (float) Math.PI + gameTime * 0.44F;
                boolean corona = (rIdx == 0);
                Vec3 prevPt = null;
                for (int i = 0; i <= lensSteps; i++) {
                    float t = i / (float) lensSteps;
                    double dist = t * len;
                    float waveR = baseR * (1.20F + 0.34F * Mth.sin(t * 9.5F - gameTime * 0.46F + rIdx));
                    float ang = off + t * (float) (len * 2.2D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * waveR))
                            .add(up.scale(Mth.sin(ang) * waveR));
                    if (prevPt != null) {
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.36F * fade,
                                corona ? 1.000F : 0.157F,
                                corona ? 0.918F : 0.118F,
                                corona ? 0.471F : 0.235F,
                                0.95F * fade);
                    }
                    prevPt = pt;
                }
            }
            for (int c = 0; c < 6; c++) {
                float ct = ((gameTime * 0.28F) + (c / 6.0F)) % 1.0F;
                double dist = ct * len;
                float cFlare = baseR * (1.30F + 0.58F * ct);
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                boolean corona = (c % 2 == 0);
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        cFlare * 0.80F, cFlare, 16,
                        corona ? 1.000F : 0.157F,
                        corona ? 0.918F : 0.118F,
                        corona ? 0.471F : 0.235F,
                        0.94F * cAlpha);
            }
        } else if (isHoarfrostRimeMateria(inst.essence)) {
            // Section 6.42 Glacial Absolute-Zero Jetstream Beam (#6395B1 / #DCF0FF): howling sub-zero needle-ice gale & hoarfrost crystals!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.58F * fade, 8,
                    0.863F, 0.941F, 1.000F, 0.98F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.26F * fade, 10,
                    0.388F, 0.584F, 0.694F, 0.80F * fade, FULL_LIGHT);

            int rimeSteps = Math.clamp((int) (len * 4.0D), 10, 44);
            for (int rIdx = 0; rIdx < 2; rIdx++) {
                float off = rIdx * (float) Math.PI + gameTime * 0.46F;
                boolean white = (rIdx == 0);
                Vec3 prevPt = null;
                for (int i = 0; i <= rimeSteps; i++) {
                    float t = i / (float) rimeSteps;
                    double dist = t * len;
                    float waveR = baseR * (1.20F + 0.34F * Mth.sin(t * 9.5F - gameTime * 0.48F + rIdx));
                    float ang = off + t * (float) (len * 2.3D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * waveR))
                            .add(up.scale(Mth.sin(ang) * waveR));
                    if (prevPt != null) {
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.36F * fade,
                                white ? 0.863F : 0.388F,
                                white ? 0.941F : 0.584F,
                                white ? 1.000F : 0.694F,
                                0.95F * fade);
                    }
                    prevPt = pt;
                }
            }
            for (int c = 0; c < 6; c++) {
                float ct = ((gameTime * 0.30F) + (c / 6.0F)) % 1.0F;
                double dist = ct * len;
                float cFlare = baseR * (1.28F + 0.58F * ct);
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                boolean white = (c % 2 == 0);
                renderOrientedStarPolygon(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        cFlare, 6, ct * 4.0F,
                        white ? 0.863F : 0.388F,
                        white ? 0.941F : 0.584F,
                        white ? 1.000F : 0.694F,
                        0.94F * cAlpha);
            }
        } else if (isVernalSpringMateria(inst.essence)) {
            // Section 6.43 Photosynthetic Life-Tether (#FF6B9D / #64C8FF): dual-stream tether of floral blossom vines & pressurized springwater!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.56F * fade, 8,
                    0.392F, 0.784F, 1.000F, 0.96F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.22F * fade, 10,
                    1.000F, 0.420F, 0.616F, 0.76F * fade, FULL_LIGHT);

            int springSteps = Math.clamp((int) (len * 4.0D), 10, 44);
            for (int sIdx = 0; sIdx < 2; sIdx++) {
                float off = sIdx * (float) Math.PI + gameTime * 0.42F;
                boolean blossom = (sIdx == 0);
                Vec3 prevPt = null;
                for (int i = 0; i <= springSteps; i++) {
                    float t = i / (float) springSteps;
                    double dist = t * len;
                    float waveR = baseR * (1.22F + 0.32F * Mth.sin(t * 9.0F - gameTime * 0.44F + sIdx));
                    float ang = off + t * (float) (len * 2.2D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * waveR))
                            .add(up.scale(Mth.sin(ang) * waveR));
                    if (prevPt != null) {
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.36F * fade,
                                blossom ? 1.000F : 0.392F,
                                blossom ? 0.420F : 0.784F,
                                blossom ? 0.616F : 1.000F,
                                0.95F * fade);
                    }
                    prevPt = pt;
                }
            }
            for (int c = 0; c < 6; c++) {
                float ct = ((gameTime * 0.28F) + (c / 6.0F)) % 1.0F;
                double dist = ct * len;
                float cFlare = baseR * (1.26F + 0.54F * ct);
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                boolean blossom = (c % 2 == 0);
                renderOrientedStarPolygon(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        cFlare, 5, ct * 4.2F,
                        blossom ? 1.000F : 0.392F,
                        blossom ? 0.420F : 0.784F,
                        blossom ? 0.616F : 1.000F,
                        0.94F * cAlpha);
            }
        } else if (isGalvanicStaticMateria(inst.essence)) {
            // Section 6.44 Dielectric Breakdown Arc-Stream Beam (#FFE600 / #878DA2): high-voltage electrical arc stream & ionizing dust slipstream!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.50F * fade, 8,
                    1.000F, 0.902F, 0.000F, 0.99F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.24F * fade, 10,
                    0.529F, 0.553F, 0.635F, 0.66F * fade, FULL_LIGHT);

            int arcSteps = Math.clamp((int) (len * 3.8D), 8, 42);
            int frame = (int) (gameTime * 1.35F) + inst.seed * 17;
            for (int strand = 0; strand < 3; strand++) {
                Vec3 prevPt = inst.start;
                boolean yellow = (strand < 2);
                for (int i = 1; i <= arcSteps; i++) {
                    float t = i / (float) arcSteps;
                    Vec3 pt;
                    if (i == arcSteps) {
                        pt = inst.endOrDir;
                    } else {
                        float env = Mth.sin(t * (float) Math.PI);
                        float jR = (hashFloat(frame + strand * 29, i * 5) - 0.5F) * 2.0F * baseR * 2.15F * env;
                        float jU = (hashFloat(frame + strand * 29, i * 5 + 1) - 0.5F) * 2.0F * baseR * 2.15F * env;
                        pt = inst.start.add(fwd.scale(t * len)).add(right.scale(jR)).add(up.scale(jU));
                    }
                    renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.34F * fade,
                            yellow ? 1.000F : 0.529F,
                            yellow ? 0.902F : 0.553F,
                            yellow ? 0.000F : 0.635F,
                            0.96F * fade);
                    prevPt = pt;
                }
            }
            for (int c = 0; c < 6; c++) {
                float ct = ((gameTime * 0.34F) + (c / 6.0F)) % 1.0F;
                double dist = ct * len;
                float cFlare = baseR * (1.28F + 0.58F * ct);
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                boolean yellow = (c % 2 == 0);
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        cFlare * 0.80F, cFlare, 8,
                        yellow ? 1.000F : 0.529F,
                        yellow ? 0.902F : 0.553F,
                        yellow ? 0.000F : 0.635F,
                        0.94F * cAlpha);
            }
        } else if (isPrimevalSylvanMateria(inst.essence)) {
            // Section 6.45 Canopy Solar-Root Tether Beam (#1A5923 / #773214 / #FFEA78): golden solar core & twisting living ironwood roots!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.54F * fade, 8,
                    1.000F, 0.918F, 0.471F, 0.98F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.18F * fade, 10,
                    0.102F, 0.349F, 0.137F, 0.72F * fade, FULL_LIGHT);

            int rootSteps = Math.clamp((int) (len * 4.0D), 10, 44);
            for (int rIdx = 0; rIdx < 2; rIdx++) {
                float off = rIdx * (float) Math.PI - gameTime * 0.36F;
                boolean ironwood = (rIdx == 0);
                Vec3 prevPt = null;
                for (int i = 0; i <= rootSteps; i++) {
                    float t = i / (float) rootSteps;
                    double dist = t * len;
                    float waveR = baseR * (1.24F + 0.30F * Mth.sin(t * 8.5F + gameTime * 0.38F + rIdx));
                    float ang = off + t * (float) (len * 2.1D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * waveR))
                            .add(up.scale(Mth.sin(ang) * waveR));
                    if (prevPt != null) {
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.38F * fade,
                                ironwood ? 0.467F : 0.102F,
                                ironwood ? 0.196F : 0.349F,
                                ironwood ? 0.078F : 0.137F,
                                0.96F * fade);
                    }
                    prevPt = pt;
                }
            }
            for (int c = 0; c < 6; c++) {
                float ct = ((gameTime * 0.26F) + (c / 6.0F)) % 1.0F;
                double dist = ct * len;
                float cFlare = baseR * (1.26F + 0.54F * ct);
                float cAlpha = fade * Mth.sin(ct * (float) Math.PI);
                boolean gold = (c % 2 == 0);
                renderOrientedStarPolygon(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        cFlare, 6, ct * 3.8F,
                        gold ? 1.000F : 0.102F,
                        gold ? 0.918F : 0.349F,
                        gold ? 0.471F : 0.137F,
                        0.95F * cAlpha);
            }
        } else if (isPestilentMiasmaMateria(inst.essence)) {
            // Section 6.46 Decomposing Quagmire Siphon Ray & Sinkhole (#1A5923 / #4D6453 / #225C7C): murky toxic teal siphon core & inward-dragging swamp bile tendrils!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.56F * fade, 8,
                    0.133F, 0.361F, 0.486F, 0.96F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.22F * fade, 10,
                    0.302F, 0.392F, 0.325F, 0.68F * fade, FULL_LIGHT);

            int bileSteps = Math.clamp((int) (len * 4.0D), 10, 44);
            for (int bIdx = 0; bIdx < 2; bIdx++) {
                float off = bIdx * (float) Math.PI + gameTime * 0.42F; // inward-pulling phase!
                boolean green = (bIdx == 0);
                Vec3 prevPt = null;
                for (int i = 0; i <= bileSteps; i++) {
                    float t = i / (float) bileSteps;
                    double dist = t * len;
                    float waveR = baseR * (1.28F + 0.32F * Mth.sin(t * 8.0F + gameTime * 0.40F + bIdx));
                    float ang = off + t * (float) (len * 2.2D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * waveR))
                            .add(up.scale(Mth.sin(ang) * waveR));
                    if (prevPt != null) {
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.40F * fade,
                                green ? 0.102F : 0.133F,
                                green ? 0.349F : 0.361F,
                                green ? 0.137F : 0.486F,
                                0.95F * fade);
                    }
                    prevPt = pt;
                }
            }
            for (int s = 0; s < 6; s++) {
                float st = (1.0F - (((gameTime * 0.28F) + (s / 6.0F)) % 1.0F));
                double dist = st * len;
                float sFlare = baseR * (1.22F + 0.50F * st);
                float sAlpha = fade * Mth.sin(st * (float) Math.PI);
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        sFlare * 0.72F, sFlare, 14,
                        0.102F, 0.349F, 0.137F, 0.92F * sAlpha);
            }
        } else if (isPrimordialGenesisMateria(inst.essence)) {
            // Section 6.47 Primordial Genesis Creation Ray & Rebirth Pulse (#FF6B9D / #FFEA78 / #225C7C): dual-helix torrent of radiant gold & living water!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.56F * fade, 8,
                    1.000F, 0.918F, 0.471F, 0.99F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.18F * fade, 10,
                    0.133F, 0.361F, 0.486F, 0.72F * fade, FULL_LIGHT);

            int helixSteps = Math.clamp((int) (len * 4.2D), 12, 46);
            for (int hIdx = 0; hIdx < 2; hIdx++) {
                float off = hIdx * (float) Math.PI - gameTime * 0.44F;
                boolean pink = (hIdx == 0);
                Vec3 prevPt = null;
                for (int i = 0; i <= helixSteps; i++) {
                    float t = i / (float) helixSteps;
                    double dist = t * len;
                    float waveR = baseR * (1.26F + 0.28F * Mth.sin(t * 9.0F + gameTime * 0.40F + hIdx));
                    float ang = off + t * (float) (len * 2.4D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * waveR))
                            .add(up.scale(Mth.sin(ang) * waveR));
                    if (prevPt != null) {
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.38F * fade,
                                1.000F,
                                pink ? 0.420F : 0.918F,
                                pink ? 0.616F : 0.471F,
                                0.97F * fade);
                    }
                    prevPt = pt;
                }
            }
            for (int l = 0; l < 6; l++) {
                float lt = ((gameTime * 0.28F) + (l / 6.0F)) % 1.0F;
                double dist = lt * len;
                float lFlare = baseR * (1.24F + 0.52F * lt);
                float lAlpha = fade * Mth.sin(lt * (float) Math.PI);
                renderOrientedStarPolygon(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        lFlare, 6, lt * 4.0F, 1.000F, 0.420F, 0.616F, 0.94F * lAlpha);
            }
        } else if (isStygianOblivionMateria(inst.essence)) {
            // Section 6.48 Stygian Extinction Erasure Beam (#B83C08 / #211A21 / #4D6453): concentrated obsidian hellfire & necrotic decay coils!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.60F * fade, 8,
                    0.722F, 0.235F, 0.031F, 0.99F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.22F * fade, 10,
                    0.129F, 0.102F, 0.129F, 0.88F * fade, FULL_LIGHT);

            int coilSteps = Math.clamp((int) (len * 4.2D), 12, 46);
            for (int cIdx = 0; cIdx < 2; cIdx++) {
                float off = cIdx * (float) Math.PI + gameTime * 0.48F;
                boolean rust = (cIdx == 0);
                Vec3 prevPt = null;
                for (int i = 0; i <= coilSteps; i++) {
                    float t = i / (float) coilSteps;
                    double dist = t * len;
                    float waveR = baseR * (1.30F + 0.28F * Mth.sin(t * 9.5F - gameTime * 0.42F + cIdx));
                    float ang = off + t * (float) (len * 2.5D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * waveR))
                            .add(up.scale(Mth.sin(ang) * waveR));
                    if (prevPt != null) {
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.38F * fade,
                                rust ? 0.722F : 0.302F,
                                rust ? 0.235F : 0.392F,
                                rust ? 0.031F : 0.325F,
                                0.97F * fade);
                    }
                    prevPt = pt;
                }
            }
            for (int rIdx = 0; rIdx < 6; rIdx++) {
                float rt = ((gameTime * 0.26F) + (rIdx / 6.0F)) % 1.0F;
                double dist = rt * len;
                float rFlare = baseR * (1.28F + 0.56F * rt);
                float rAlpha = fade * Mth.sin(rt * (float) Math.PI);
                renderOrientedStarPolygon(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        rFlare, 6, -rt * 4.2F, 0.722F, 0.235F, 0.031F, 0.95F * rAlpha);
            }
        } else if (isQuintessentialAetherMateria(inst.essence)) {
            // Section 6.50 Quintessential Firmament Ray (#A88654 / #773214 / #225C7C / #B83C08): 4-band atmospheric beam & halo rings!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.62F * fade, 8,
                    0.659F, 0.525F, 0.329F, 0.99F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.24F * fade, 12,
                    0.133F, 0.361F, 0.486F, 0.74F * fade, FULL_LIGHT);

            float[][] quadRgb = {
                    {0.659F, 0.525F, 0.329F},
                    {0.467F, 0.196F, 0.078F},
                    {0.133F, 0.361F, 0.486F},
                    {0.722F, 0.235F, 0.031F}
            };
            int bandSteps = Math.clamp((int) (len * 4.0D), 12, 46);
            for (int qIdx = 0; qIdx < 4; qIdx++) {
                float off = (float) (Math.PI * 0.5D * qIdx) - gameTime * 0.46F;
                float[] c = quadRgb[qIdx];
                Vec3 prevPt = null;
                for (int i = 0; i <= bandSteps; i++) {
                    float t = i / (float) bandSteps;
                    double dist = t * len;
                    float waveR = baseR * (1.32F + 0.28F * Mth.sin(t * 9.0F + gameTime * 0.38F + qIdx));
                    float ang = off + t * (float) (len * 2.2D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * waveR))
                            .add(up.scale(Mth.sin(ang) * waveR));
                    if (prevPt != null) {
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.34F * fade, c[0], c[1], c[2], 0.96F * fade);
                    }
                    prevPt = pt;
                }
            }
            for (int hIdx = 0; hIdx < 6; hIdx++) {
                float ht = ((gameTime * 0.28F) + (hIdx / 6.0F)) % 1.0F;
                double dist = ht * len;
                float hFlare = baseR * (1.34F + 0.60F * ht);
                float hAlpha = fade * Mth.sin(ht * (float) Math.PI);
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        hFlare * 0.80F, hFlare, 22, 0.659F, 0.525F, 0.329F, 0.95F * hAlpha);
            }
        } else if (isAbyssalEntropicMateria(inst.essence)) {
            // Section 6.51 Entropic Oblivion-Siphon Ray (#41304E / #211A21 / #4D6453 / #8A0303): light-devouring void darkness & crimson blood mist!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.64F * fade, 8,
                    0.129F, 0.102F, 0.129F, 0.99F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.26F * fade, 12,
                    0.255F, 0.188F, 0.306F, 0.84F * fade, FULL_LIGHT);

            float[][] entRgb = {
                    {0.255F, 0.188F, 0.306F},
                    {0.129F, 0.102F, 0.129F},
                    {0.302F, 0.392F, 0.325F},
                    {0.541F, 0.012F, 0.012F}
            };
            int siphonSteps = Math.clamp((int) (len * 4.0D), 12, 46);
            for (int eIdx = 0; eIdx < 4; eIdx++) {
                float off = (float) (Math.PI * 0.5D * eIdx) + gameTime * 0.48F;
                float[] c = entRgb[eIdx];
                Vec3 prevPt = null;
                for (int i = 0; i <= siphonSteps; i++) {
                    float t = i / (float) siphonSteps;
                    double dist = t * len;
                    float waveR = baseR * (1.30F + 0.30F * Mth.sin(t * 9.5F - gameTime * 0.44F + eIdx));
                    float ang = off + t * (float) (len * 2.4D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * waveR))
                            .add(up.scale(Mth.sin(ang) * waveR));
                    if (prevPt != null) {
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.36F * fade, c[0], c[1], c[2], 0.96F * fade);
                    }
                    prevPt = pt;
                }
            }
            for (int rIdx = 0; rIdx < 6; rIdx++) {
                float rt = ((gameTime * 0.26F) + (rIdx / 6.0F)) % 1.0F;
                double dist = (1.0F - rt) * len; // Inward-siphoning rings toward caster!
                float rFlare = baseR * (1.32F + 0.58F * rt);
                float rAlpha = fade * Mth.sin(rt * (float) Math.PI);
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        rFlare * 0.80F, rFlare, 20, 0.541F, 0.012F, 0.012F, 0.96F * rAlpha);
            }
        } else if (isHolyCelestialMateria(inst.essence)) {
            // Section 6.52 Holy Pranic Fusion Beam (#FFEA78 / #FF6B9D / #1A5923 / #A88654): white-gold starlight & photosynthetic plasma!
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 0.58F * fade, 8,
                    1.000F, 1.000F, 1.000F, 0.99F * fade, FULL_LIGHT);
            renderTube(matrix, vc, inst.start, inst.endOrDir, baseR * 1.24F * fade, 12,
                    1.000F, 0.918F, 0.471F, 0.78F * fade, FULL_LIGHT);

            float[][] celRgb = {
                    {1.000F, 0.918F, 0.471F},
                    {1.000F, 0.420F, 0.616F},
                    {0.102F, 0.349F, 0.137F},
                    {0.659F, 0.525F, 0.329F}
            };
            int beamSteps = Math.clamp((int) (len * 4.0D), 12, 46);
            for (int cIdx = 0; cIdx < 4; cIdx++) {
                float off = (float) (Math.PI * 0.5D * cIdx) - gameTime * 0.46F;
                float[] c = celRgb[cIdx];
                Vec3 prevPt = null;
                for (int i = 0; i <= beamSteps; i++) {
                    float t = i / (float) beamSteps;
                    double dist = t * len;
                    float waveR = baseR * (1.28F + 0.28F * Mth.sin(t * 9.0F - gameTime * 0.42F + cIdx));
                    float ang = off + t * (float) (len * 2.4D);
                    Vec3 pt = inst.start.add(fwd.scale(dist))
                            .add(right.scale(Mth.cos(ang) * waveR))
                            .add(up.scale(Mth.sin(ang) * waveR));
                    if (prevPt != null) {
                        renderCrossSegment(matrix, vc, prevPt, pt, baseR * 0.35F * fade, c[0], c[1], c[2], 0.96F * fade);
                    }
                    prevPt = pt;
                }
            }
            for (int rIdx = 0; rIdx < 6; rIdx++) {
                float rt = ((gameTime * 0.26F) + (rIdx / 6.0F)) % 1.0F;
                double dist = rt * len;
                float rFlare = baseR * (1.30F + 0.56F * rt);
                float rAlpha = fade * Mth.sin(rt * (float) Math.PI);
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(dist)), right, up,
                        rFlare * 0.80F, rFlare, 22, 1.000F, 0.420F, 0.616F, 0.95F * rAlpha);
            }
        }

        float impactExpand = channeledSlice ? (1.15F + 0.12F * Mth.sin(gameTime * 0.9F)) : (1.0F + progress * 1.6F);
        float impactAlpha = channeledSlice ? (0.86F - progress * 0.22F) : (0.82F * (1.0F - progress));
        renderOrientedRing(matrix, vc, inst.endOrDir.subtract(fwd.scale(0.05D)), right, up,
                baseR * 1.4F * impactExpand, baseR * 2.4F * impactExpand, 18,
                pr, pg, pb, impactAlpha);
    }

    // =========================================================================
    // 2. JAGGED 3D CHAIN ARC / CATENARY SONIC WIND-BRIDGE
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

        float fade = computeEnvelope(progress, 0.05F, 0.52F);
        Vec3 fwd = span.normalize();
        Vec3 right = buildPerpendicularRight(fwd);
        Vec3 up = right.cross(fwd).normalize();

        // Air / Aura Sonic Leap Chains: graceful upward-arching catenary Vacuum Wind-Bridge (Guide Sec 3.9 & 6.2)!
        if (isAerodynamicMateria(inst.essence)) {
            int steps = Math.clamp((int) (len * 4.0D), 12, 36);
            double archHeight = Math.min(2.2D, 0.35D + len * 0.18D);
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);

            for (int strand = 0; strand < 3; strand++) {
                float phase0 = -gameTime * 0.55F + (float) (Math.PI * 2.0D * strand / 3.0D);
                boolean ivory = (strand == 0);
                float sr = ivory ? 1.0F : (strand == 1 ? ar : pr);
                float sg = ivory ? 1.0F : (strand == 1 ? ag : pg);
                float sb = ivory ? 1.0F : (strand == 1 ? ab : pb);

                Vec3 prevPt = null;
                Vec3 prevCenter = null;
                for (int i = 0; i <= steps; i++) {
                    float t = i / (float) steps;
                    float arch = Mth.sin(t * (float) Math.PI);
                    Vec3 center = inst.start.add(span.scale(t)).add(worldUp.scale(archHeight * arch));
                    float helixR = (0.06F + 0.16F * arch) * fade;
                    float ang = phase0 + t * 10.5F;
                    Vec3 pt = center.add(right.scale(Mth.cos(ang) * helixR)).add(up.scale(Mth.sin(ang) * helixR));

                    if (prevPt != null) {
                        renderCrossSegment(matrix, vc, prevPt, pt, (ivory ? 0.045F : 0.065F) * fade, sr, sg, sb, 0.88F * fade);
                        if (strand == 0 && prevCenter != null) {
                            renderCrossSegment(matrix, vc, prevCenter, center, 0.14F * fade * (0.45F + 0.55F * arch),
                                    pr, pg, pb, 0.32F * fade);
                        }
                    }
                    prevPt = pt;
                    prevCenter = center;
                }
            }

            // Twin inward-rushing sonic compression rings pulling both chained targets toward their midpoint!
            float pullT = Mth.clamp(progress * 1.15F, 0.0F, 0.5F);
            for (int side = 0; side < 2; side++) {
                float t = (side == 0) ? pullT : (1.0F - pullT);
                Vec3 ringPos = inst.start.add(span.scale(t)).add(worldUp.scale(archHeight * Mth.sin(t * (float) Math.PI)));
                float rSize = (0.42F - 0.36F * pullT) * (0.85F + 0.15F * fade);
                renderOrientedRing(matrix, vc, ringPos, right, up, rSize * 0.76F, rSize, 18, ar, ag, ab, 0.86F * fade);
            }
            return;
        }

        // Water (#2) Fluid Dissolution Droplet Streak, Aqueous Agent Water Tendril Lash, & Riptide Undertow Chains!
        if (isHydroMateria(inst.essence)) {
            int steps = Math.clamp((int) (len * 4.0D), 12, 36);
            double archHeight = Math.min(0.85D, 0.12D + len * 0.06D);
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);

            Vec3 prevCenter = null;
            for (int i = 0; i <= steps; i++) {
                float t = i / (float) steps;
                float arch = Mth.sin(t * (float) Math.PI);
                float waveR = 0.08F * arch * Mth.sin(t * 9.0F - gameTime * 0.65F);
                float waveU = 0.06F * arch * Mth.cos(t * 9.0F - gameTime * 0.65F);
                Vec3 center = inst.start.add(span.scale(t))
                        .add(worldUp.scale(archHeight * arch))
                        .add(right.scale(waveR))
                        .add(up.scale(waveU));
                if (prevCenter != null) {
                    float wProfile = (0.55F + 0.45F * arch) * fade;
                    renderCrossSegment(matrix, vc, prevCenter, center, 0.13F * wProfile, 0.133F, 0.361F, 0.486F, 0.62F * fade);
                    renderCrossSegment(matrix, vc, prevCenter, center, 0.075F * wProfile, pr, pg, pb, 0.84F * fade);
                    renderCrossSegment(matrix, vc, prevCenter, center, 0.032F * wProfile, 0.90F, 0.99F, 1.00F, 0.94F * fade);
                }
                prevCenter = center;
            }

            // Twin coiling whitewater foam ribbons around the pressurized stream
            for (int strand = 0; strand < 2; strand++) {
                float phase0 = -gameTime * 0.60F + strand * (float) Math.PI;
                Vec3 prevPt = null;
                for (int i = 0; i <= steps; i++) {
                    float t = i / (float) steps;
                    float arch = Mth.sin(t * (float) Math.PI);
                    Vec3 center = inst.start.add(span.scale(t)).add(worldUp.scale(archHeight * arch));
                    float helixR = (0.05F + 0.12F * arch) * fade;
                    float ang = phase0 + t * 11.0F;
                    Vec3 pt = center.add(right.scale(Mth.cos(ang) * helixR)).add(up.scale(Mth.sin(ang) * helixR));
                    if (prevPt != null) {
                        renderCrossSegment(matrix, vc, prevPt, pt, 0.036F * fade,
                                strand == 0 ? 0.90F : ar, strand == 0 ? 0.99F : ag, strand == 0 ? 1.00F : ab, 0.82F * fade);
                    }
                    prevPt = pt;
                }
            }

            // High-speed 3D water droplet head streaking along the path (Fluid Dissolution & Tendril tip!)
            float headT = Mth.clamp(progress * 2.35F, 0.0F, 1.0F);
            Vec3 headPos = inst.start.add(span.scale(headT))
                    .add(worldUp.scale(archHeight * Mth.sin(headT * (float) Math.PI)));
            renderOctahedron(matrix, vc, headPos, 0.24F * fade, 0.133F, 0.361F, 0.486F, 0.86F * fade);
            renderOctahedron(matrix, vc, headPos, 0.14F * fade, 0.92F, 0.99F, 1.00F, 0.96F * fade);
            renderOrientedRing(matrix, vc, headPos, right, up, 0.18F * fade, 0.28F * fade, 16, 0.88F, 0.98F, 1.00F, 0.85F * fade);
            return;
        }

        // Nature (#3) & Overgrowth (#16) Entangling Briar Chains!
        if (isBotanicalMateria(inst.essence) || isOvergrowthRampartMateria(inst.essence)) {
            int steps = Math.clamp((int) (len * 4.2D), 12, 36);
            double sagHeight = Math.min(0.65D, 0.10D + len * 0.05D);
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);

            Vec3 prevStem = null;
            for (int i = 0; i <= steps; i++) {
                float t = i / (float) steps;
                float arch = Mth.sin(t * (float) Math.PI);
                float swayR = 0.07F * arch * Mth.sin(t * 7.5F - gameTime * 0.28F + inst.seed);
                Vec3 stem = inst.start.add(span.scale(t))
                        .add(worldUp.scale(sagHeight * arch))
                        .add(right.scale(swayR));
                if (prevStem != null) {
                    // Woody briar bark stem (#5C3A1E) + lush forest-green (#1A5923) inner sap vein
                    renderCrossSegment(matrix, vc, prevStem, stem, 0.085F * fade, 0.36F, 0.23F, 0.12F, 0.92F * fade);
                    renderCrossSegment(matrix, vc, prevStem, stem, 0.048F * fade, 0.102F, 0.349F, 0.137F, 0.95F * fade);
                }
                // Protruding 3D razor thorn spikes & emerald leaves along the briar chain
                if (i > 0 && i < steps && i % 2 == 0) {
                    float thornAng = i * 2.1F + inst.seed;
                    Vec3 tDir = right.scale(Mth.cos(thornAng)).add(up.scale(Mth.sin(thornAng)));
                    Vec3 thornTip = stem.add(tDir.scale(0.22D * fade)).add(fwd.scale((i % 4 == 0 ? 0.08D : -0.08D) * fade));
                    renderCrossSegment(matrix, vc, stem, thornTip, 0.028F * fade, 0.71F, 1.00F, 0.47F, 0.92F * fade);
                }
                prevStem = stem;
            }

            // Twin coiling thorny creeper vines wrapping the briar tether
            for (int v = 0; v < 2; v++) {
                float phase0 = -gameTime * 0.36F + v * (float) Math.PI;
                Vec3 prevVine = null;
                for (int i = 0; i <= steps; i++) {
                    float t = i / (float) steps;
                    float arch = Mth.sin(t * (float) Math.PI);
                    Vec3 center = inst.start.add(span.scale(t)).add(worldUp.scale(sagHeight * arch));
                    float helixR = (0.06F + 0.11F * arch) * fade;
                    float ang = phase0 + t * 12.0F;
                    Vec3 pt = center.add(right.scale(Mth.cos(ang) * helixR)).add(up.scale(Mth.sin(ang) * helixR));
                    if (prevVine != null) {
                        renderCrossSegment(matrix, vc, prevVine, pt, 0.036F * fade,
                                v == 0 ? 0.17F : 0.71F, v == 0 ? 0.90F : 1.00F, v == 0 ? 0.41F : 0.47F, 0.88F * fade);
                    }
                    prevVine = pt;
                }
            }

            // Coiling thorny snare collars wrapping both tethered victims!
            Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
            Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
            renderOrientedRing(matrix, vc, inst.start, flatX, flatZ, 0.34F * fade, 0.46F * fade, 16,
                    0.102F, 0.349F, 0.137F, 0.86F * fade);
            renderOrientedRing(matrix, vc, inst.endOrDir, flatX, flatZ, 0.34F * fade, 0.46F * fade, 16,
                    0.17F, 0.90F, 0.41F, 0.86F * fade);
            return;
        }

        // Earth (#4) Tectonic Fault Fissure & Ground-Punch Seismic Crack!
        if (isTectonicMateria(inst.essence)) {
            int steps = Math.clamp((int) (len * 3.5D), 8, 28);
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
            Vec3 prevCrack = null;
            for (int i = 0; i <= steps; i++) {
                float t = i / (float) steps;
                float env = Mth.sin(t * (float) Math.PI);
                float zig = ((i % 2 == 0) ? 1.0F : -1.0F) * 0.18F * env;
                Vec3 pt = inst.start.add(span.scale(t)).add(right.scale(zig));
                if (prevCrack != null) {
                    renderCrossSegment(matrix, vc, prevCrack, pt, 0.13F * fade, 0.467F, 0.196F, 0.078F, 0.92F * fade);
                    renderCrossSegment(matrix, vc, prevCrack, pt, 0.055F * fade, 1.00F, 0.76F, 0.47F, 0.96F * fade);
                }
                if (i > 0 && i < steps && i % 4 == 0) {
                    renderFacetedStoneSpire(matrix, vc, pt, worldUp, right, fwd,
                            0.11F * fade, (0.38F + 0.24F * env) * fade, 5, i * 0.9F + inst.seed, fade);
                }
                prevCrack = pt;
            }
            return;
        }

        // Nether (#6) Arcing Fire-Whip Chains: roaring braided 3D flame whips with serrated fire barbs!
        if (isInfernalMateria(inst.essence)) {
            int steps = Math.clamp((int) (len * 4.2D), 12, 36);
            double archHeight = Math.min(1.05D, 0.18D + len * 0.08D);
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);

            for (int whip = 0; whip < 2; whip++) {
                float wPhase = whip * (float) Math.PI - gameTime * 0.48F;
                Vec3 prevWhip = null;
                for (int i = 0; i <= steps; i++) {
                    float t = i / (float) steps;
                    float arch = Mth.sin(t * (float) Math.PI);
                    Vec3 center = inst.start.add(span.scale(t)).add(worldUp.scale(archHeight * arch));
                    float coilR = (0.05F + 0.15F * arch) * fade;
                    float ang = wPhase + t * 11.5F;
                    Vec3 pt = center.add(right.scale(Mth.cos(ang) * coilR)).add(up.scale(Mth.sin(ang) * coilR));
                    if (prevWhip != null) {
                        boolean gold = (whip == 0);
                        renderCrossSegment(matrix, vc, prevWhip, pt, (gold ? 0.052F : 0.082F) * fade,
                                gold ? 1.00F : 0.722F,
                                gold ? 0.820F : 0.235F,
                                gold ? 0.361F : 0.031F,
                                0.92F * fade);
                    }
                    if (whip == 0 && i > 1 && i < steps - 1 && i % 3 == 0) {
                        renderHellfireFlameTongue(matrix, vc, pt, worldUp.add(right.scale((i % 2 == 0 ? 0.35D : -0.35D))).normalize(),
                                right, fwd, 0.12F * fade, (0.34F + 0.22F * arch) * fade, fade);
                    }
                    prevWhip = pt;
                }
            }
            return;
        }

        // Radiant (#7) Prismatic Starlight Chains: blinding straight-refracted daylight beams with prism dispersion & target halos!
        if (isSolarRadiantMateria(inst.essence)) {
            Vec3 midPrism = inst.start.add(span.scale(0.5D)).add(0.0D, Math.min(0.45D, len * 0.06D), 0.0D);
            // Blinding white-gold refracted daylight core + #FFE278 corona sheath along the two straight optical legs
            renderCrossSegment(matrix, vc, inst.start, midPrism, 0.12F * fade, 1.00F, 0.886F, 0.471F, 0.65F * fade);
            renderCrossSegment(matrix, vc, midPrism, inst.endOrDir, 0.12F * fade, 1.00F, 0.886F, 0.471F, 0.65F * fade);
            renderCrossSegment(matrix, vc, inst.start, midPrism, 0.052F * fade, 1.00F, 0.995F, 0.96F, 0.98F * fade);
            renderCrossSegment(matrix, vc, midPrism, inst.endOrDir, 0.052F * fade, 1.00F, 0.995F, 0.96F, 0.98F * fade);

            // Subtle refracted prismatic dispersion rays flanking the main starlight beam
            for (int disp = 0; disp < 3; disp++) {
                float dAng = gameTime * 0.22F + (float) (Math.PI * 2.0D * disp / 3.0D);
                Vec3 offset = right.scale(Mth.cos(dAng) * 0.14D * fade).add(up.scale(Mth.sin(dAng) * 0.14D * fade));
                float dr = disp == 0 ? 1.00F : (disp == 1 ? 0.62F : 1.00F);
                float dg = disp == 0 ? 0.78F : (disp == 1 ? 0.95F : 0.88F);
                float db = disp == 0 ? 0.42F : (disp == 1 ? 1.00F : 0.96F);
                renderCrossSegment(matrix, vc, inst.start, midPrism.add(offset), 0.026F * fade, dr, dg, db, 0.78F * fade);
                renderCrossSegment(matrix, vc, midPrism.add(offset), inst.endOrDir, 0.026F * fade, dr, dg, db, 0.78F * fade);
            }

            // Faceted crystal prism node at refraction vertex + illuminating golden halo rings above both victims!
            renderOctahedron(matrix, vc, midPrism, 0.18F * fade, 1.00F, 0.99F, 0.92F, 0.95F * fade);
            Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
            Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
            renderOrientedRing(matrix, vc, inst.start.add(0.0D, 0.72D, 0.0D), flatX, flatZ,
                    0.34F, 0.44F, 20, 1.00F, 0.886F, 0.471F, 0.90F * fade);
            renderOrientedRing(matrix, vc, inst.endOrDir.add(0.0D, 0.72D, 0.0D), flatX, flatZ,
                    0.38F, 0.50F, 20, 1.00F, 0.886F, 0.471F, 0.94F * fade);
            renderOrientedStarPolygon(matrix, vc, inst.endOrDir.add(0.0D, 0.73D, 0.0D), flatX, flatZ,
                    0.46F, 8, gameTime * 0.12F, 1.00F, 0.99F, 0.94F, 0.84F * fade);
            return;
        }

        // Arid (#11) Sirocco Leaping Chains: swirling #C2B28F desert dust threads & blinding mini-sand plumes!
        if (isAridDesertMateria(inst.essence)) {
            int steps = Math.clamp((int) (len * 4.2D), 12, 36);
            double archHeight = Math.min(1.15D, 0.20D + len * 0.10D);
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);

            for (int strand = 0; strand < 3; strand++) {
                float phase0 = -gameTime * 0.56F + (float) (Math.PI * 2.0D * strand / 3.0D);
                Vec3 prevPt = null;
                for (int i = 0; i <= steps; i++) {
                    float t = i / (float) steps;
                    float arch = Mth.sin(t * (float) Math.PI);
                    Vec3 center = inst.start.add(span.scale(t)).add(worldUp.scale(archHeight * arch));
                    float helixR = (0.06F + 0.16F * arch) * fade;
                    float ang = phase0 + t * 12.0F;
                    Vec3 pt = center.add(right.scale(Mth.cos(ang) * helixR)).add(up.scale(Mth.sin(ang) * helixR));
                    if (prevPt != null) {
                        boolean sunlit = (strand == 0);
                        boolean grit = (strand == 2);
                        renderCrossSegment(matrix, vc, prevPt, pt, (sunlit ? 0.046F : 0.068F) * fade,
                                sunlit ? 0.961F : (grit ? 0.612F : 0.761F),
                                sunlit ? 0.894F : (grit ? 0.478F : 0.698F),
                                sunlit ? 0.745F : (grit ? 0.298F : 0.561F),
                                0.88F * fade);
                    }
                    prevPt = pt;
                }
            }

            // Blinding mini-sand plume funnels erupting around chained victims!
            Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
            Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
            renderOrientedFrustumBand(matrix, vc, inst.endOrDir.add(0.0D, -0.35D, 0.0D), inst.endOrDir.add(0.0D, 0.65D, 0.0D),
                    flatX, flatZ, 0.20F * fade, 0.64F * fade, 18,
                    0.761F, 0.698F, 0.561F, 0.82F * fade, 0.24F * fade);
            renderOrientedRing(matrix, vc, inst.endOrDir.add(0.0D, 0.25D, 0.0D), flatX, flatZ,
                    0.34F * fade, 0.52F * fade, 18, 0.961F, 0.894F, 0.745F, 0.88F * fade);
            return;
        }

        // Dust (#17) Abrasive Sandblaster Chains: swirling #C2B28F / #F2ECE1 chalk-dust filaments & mini-powder cloud burst!
        if (isAbrasiveDustMateria(inst.essence)) {
            int steps = Math.clamp((int) (len * 4.2D), 12, 36);
            double archHeight = Math.min(1.05D, 0.18D + len * 0.09D);
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);

            for (int strand = 0; strand < 3; strand++) {
                float phase0 = -gameTime * 0.62F + (float) (Math.PI * 2.0D * strand / 3.0D);
                Vec3 prevPt = null;
                for (int i = 0; i <= steps; i++) {
                    float t = i / (float) steps;
                    float arch = Mth.sin(t * (float) Math.PI);
                    Vec3 center = inst.start.add(span.scale(t)).add(worldUp.scale(archHeight * arch));
                    float helixR = (0.05F + 0.15F * arch) * fade;
                    float ang = phase0 + t * 13.0F;
                    Vec3 pt = center.add(right.scale(Mth.cos(ang) * helixR)).add(up.scale(Mth.sin(ang) * helixR));
                    if (prevPt != null) {
                        boolean chalk = (strand == 0);
                        renderCrossSegment(matrix, vc, prevPt, pt, (chalk ? 0.050F : 0.068F) * fade,
                                chalk ? 0.949F : 0.761F,
                                chalk ? 0.925F : 0.698F,
                                chalk ? 0.882F : 0.561F,
                                0.90F * fade);
                    }
                    prevPt = pt;
                }
            }

            Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
            Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
            renderHemisphereShell(matrix, vc, inst.endOrDir.add(0.0D, -0.15D, 0.0D), 0.68F * fade, 4, 16,
                    0.949F, 0.925F, 0.882F, 0.52F * fade);
            renderOrientedRing(matrix, vc, inst.endOrDir.add(0.0D, 0.15D, 0.0D), flatX, flatZ,
                    0.38F * fade, 0.62F * fade, 20, 0.761F, 0.698F, 0.561F, 0.90F * fade);
            return;
        }

        // Lightning (#12) & Static (#43) Supercharged Forked Chains: multi-strand #FFE600 high-voltage lightning bolts with side forks!
        if (isGalvanicLightningMateria(inst.essence) || isGalvanicStaticMateria(inst.essence)) {
            boolean staticMat = isGalvanicStaticMateria(inst.essence);
            int segs = Math.clamp((int) (len * 3.2D), 8, 26);
            int flickerFrame = (int) (gameTime * 1.15F) + inst.seed * 19;
            float maxJitter = (float) Math.min(0.58D, len * 0.13D);

            for (int bolt = 0; bolt < 2; bolt++) {
                Vec3 prev = inst.start;
                for (int i = 1; i <= segs; i++) {
                    float t = (float) i / segs;
                    Vec3 curr;
                    if (i == segs) {
                        curr = inst.endOrDir;
                    } else {
                        float envelope = Mth.sin(t * (float) Math.PI);
                        float jR = (hashFloat(flickerFrame + bolt * 31, i * 3) - 0.5F) * 2.0F * maxJitter * envelope;
                        float jU = (hashFloat(flickerFrame + bolt * 31, i * 3 + 1) - 0.5F) * 2.0F * maxJitter * envelope;
                        curr = inst.start.add(span.scale(t)).add(right.scale(jR)).add(up.scale(jU));
                    }
                    renderCrossSegment(matrix, vc, prev, curr, (bolt == 0 ? 0.12F : 0.075F) * fade,
                            1.00F, 0.902F, 0.00F, 0.84F * fade);
                    renderCrossSegment(matrix, vc, prev, curr, (bolt == 0 ? 0.050F : 0.030F) * fade,
                            staticMat ? 0.529F : 1.00F,
                            staticMat ? 0.553F : 1.00F,
                            staticMat ? 0.635F : 0.95F,
                            0.98F * fade);
                    if (bolt == 0 && i % 4 == 0 && len > 1.5D) {
                        float fR = (hashFloat(flickerFrame, i * 11) - 0.5F) * maxJitter * 2.4F;
                        float fU = (hashFloat(flickerFrame, i * 13) - 0.5F) * maxJitter * 2.4F;
                        Vec3 forkTip = curr.add(fwd.scale(len * 0.18D)).add(right.scale(fR)).add(up.scale(fU));
                        renderCrossSegment(matrix, vc, curr, forkTip, 0.055F * fade,
                                staticMat ? 0.529F : 0.722F,
                                staticMat ? 0.553F : 0.957F,
                                staticMat ? 0.635F : 1.00F,
                                0.84F * fade);
                    }
                    prev = curr;
                }
            }
            return;
        }

        // Blood (#22) Sanguine Capillary Chains: writhing #8A0303 / #FF384C crimson blood tendrils pulling chained victims closer!
        if (isSanguineBloodMateria(inst.essence)) {
            int steps = Math.clamp((int) (len * 4.4D), 12, 36);
            double archHeight = Math.min(0.85D, 0.14D + len * 0.07D);
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);

            for (int strand = 0; strand < 3; strand++) {
                float phase0 = -gameTime * 0.58F + (float) (Math.PI * 2.0D * strand / 3.0D);
                Vec3 prevPt = null;
                for (int i = 0; i <= steps; i++) {
                    float t = i / (float) steps;
                    float arch = Mth.sin(t * (float) Math.PI);
                    Vec3 center = inst.start.add(span.scale(t)).add(worldUp.scale(archHeight * arch));
                    float helixR = (0.045F + 0.14F * arch) * fade;
                    float ang = phase0 + t * 11.5F;
                    Vec3 pt = center.add(right.scale(Mth.cos(ang) * helixR)).add(up.scale(Mth.sin(ang) * helixR));
                    if (prevPt != null) {
                        boolean arterial = (strand == 0);
                        renderCrossSegment(matrix, vc, prevPt, pt, (arterial ? 0.048F : 0.072F) * fade,
                                arterial ? 1.000F : 0.541F,
                                arterial ? 0.220F : 0.012F,
                                arterial ? 0.298F : 0.012F,
                                0.94F * fade);
                    }
                    prevPt = pt;
                }
            }
            Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
            Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
            renderOrientedRing(matrix, vc, inst.endOrDir, flatX, flatZ,
                    0.26F * fade, 0.48F * fade, 18, 0.541F, 0.012F, 0.012F, 0.92F * fade);
            return;
        }

        // Blight (#26) Cadaverous Contagion Chains: leaping threads of thorny #4D6453 / #34CC48 bone vines!
        if (isCadaverousBlightMateria(inst.essence)) {
            int steps = Math.clamp((int) (len * 4.2D), 12, 36);
            double archHeight = Math.min(0.90D, 0.16D + len * 0.075D);
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);

            for (int strand = 0; strand < 3; strand++) {
                float phase0 = gameTime * 0.48F + (float) (Math.PI * 2.0D * strand / 3.0D);
                Vec3 prevPt = null;
                for (int i = 0; i <= steps; i++) {
                    float t = i / (float) steps;
                    float arch = Mth.sin(t * (float) Math.PI);
                    Vec3 center = inst.start.add(span.scale(t)).add(worldUp.scale(archHeight * arch));
                    float helixR = (0.045F + 0.14F * arch) * fade;
                    float ang = phase0 + t * 11.0F;
                    Vec3 pt = center.add(right.scale(Mth.cos(ang) * helixR)).add(up.scale(Mth.sin(ang) * helixR));
                    if (prevPt != null) {
                        boolean bone = (strand == 0);
                        renderCrossSegment(matrix, vc, prevPt, pt, (bone ? 0.048F : 0.068F) * fade,
                                bone ? 0.902F : 0.204F,
                                bone ? 0.875F : 0.800F,
                                bone ? 0.812F : 0.282F,
                                0.94F * fade);
                    }
                    prevPt = pt;
                }
            }
            Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
            Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
            renderOrientedRing(matrix, vc, inst.endOrDir, flatX, flatZ,
                    0.28F * fade, 0.50F * fade, 16, 0.204F, 0.800F, 0.282F, 0.92F * fade);
            return;
        }

        // Astral (#27) Supercharged Void-Warp Chains: micro-wormhole rings at both endpoints + forked #FFE600 / #41304E cosmic lightning!
        if (isCosmicAstralMateria(inst.essence)) {
            int steps = Math.clamp((int) (len * 4.0D), 10, 34);
            renderOrientedRing(matrix, vc, inst.start, right, up,
                    0.22F * fade, 0.42F * fade, 18, 0.255F, 0.188F, 0.306F, 0.92F * fade);
            renderOrientedRing(matrix, vc, inst.endOrDir, right, up,
                    0.24F * fade, 0.46F * fade, 18, 1.000F, 0.902F, 0.000F, 0.95F * fade);

            for (int strand = 0; strand < 2; strand++) {
                Vec3 prevPt = inst.start;
                for (int i = 1; i <= steps; i++) {
                    float t = i / (float) steps;
                    float env = Mth.sin(t * (float) Math.PI);
                    float jx = (hashFloat(i + strand * 19, inst.seed + (int) gameTime) - 0.5F) * 0.42F * env;
                    float jy = (hashFloat(i + strand * 37, inst.seed + 77 + (int) gameTime) - 0.5F) * 0.42F * env;
                    Vec3 pt = (i == steps) ? inst.endOrDir
                            : inst.start.add(span.scale(t)).add(right.scale(jx)).add(up.scale(jy));
                    boolean gold = (strand == 0);
                    renderCrossSegment(matrix, vc, prevPt, pt, (gold ? 0.058F : 0.078F) * fade,
                            gold ? 1.000F : 0.255F,
                            gold ? 0.902F : 0.188F,
                            gold ? 0.000F : 0.306F,
                            0.96F * fade);
                    prevPt = pt;
                }
            }
            return;
        }

        // Soulfire (#28) Phantasmal Soul-Chain Flow: leaping filaments of howling #1DF0E5 / #0D6469 cyan soulfire!
        if (isPhantasmalSoulfireMateria(inst.essence)) {
            int steps = Math.clamp((int) (len * 4.2D), 12, 36);
            double archHeight = Math.min(0.95D, 0.18D + len * 0.08D);
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);

            for (int strand = 0; strand < 3; strand++) {
                float phase0 = -gameTime * 0.52F + (float) (Math.PI * 2.0D * strand / 3.0D);
                Vec3 prevPt = null;
                for (int i = 0; i <= steps; i++) {
                    float t = i / (float) steps;
                    float arch = Mth.sin(t * (float) Math.PI);
                    Vec3 center = inst.start.add(span.scale(t)).add(worldUp.scale(archHeight * arch));
                    float helixR = (0.05F + 0.15F * arch) * fade;
                    float ang = phase0 + t * 11.5F;
                    Vec3 pt = center.add(right.scale(Mth.cos(ang) * helixR)).add(up.scale(Mth.sin(ang) * helixR));
                    if (prevPt != null) {
                        boolean cyan = (strand != 2);
                        renderCrossSegment(matrix, vc, prevPt, pt, (cyan ? 0.062F : 0.074F) * fade,
                                cyan ? 0.114F : 0.051F,
                                cyan ? 0.941F : 0.392F,
                                cyan ? 0.898F : 0.412F,
                                0.95F * fade);
                    }
                    prevPt = pt;
                }
            }
            Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
            Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
            renderOrientedRing(matrix, vc, inst.endOrDir, flatX, flatZ,
                    0.26F * fade, 0.48F * fade, 18, 0.114F, 0.941F, 0.898F, 0.94F * fade);
            return;
        }

        // Section 6.46 Virulent Contagion-Chain Flow (#1A5923 / #4D6453 / #225C7C): caustic tendrils of stagnant swamp bile!
        if (isPestilentMiasmaMateria(inst.essence)) {
            int steps = Math.clamp((int) (len * 4.0D), 12, 34);
            double archHeight = Math.min(0.75D, 0.12D + len * 0.06D);
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
            Vec3 prevCenter = null;
            for (int i = 0; i <= steps; i++) {
                float t = i / (float) steps;
                float arch = Mth.sin(t * (float) Math.PI);
                float swayR = 0.09F * arch * Mth.sin(t * 8.5F - gameTime * 0.45F + inst.seed);
                Vec3 center = inst.start.add(span.scale(t))
                        .add(worldUp.scale(archHeight * arch))
                        .add(right.scale(swayR));
                if (prevCenter != null) {
                    renderCrossSegment(matrix, vc, prevCenter, center, 0.10F * fade, 0.302F, 0.392F, 0.325F, 0.85F * fade);
                    renderCrossSegment(matrix, vc, prevCenter, center, 0.055F * fade, 0.133F, 0.361F, 0.486F, 0.95F * fade);
                    renderCrossSegment(matrix, vc, prevCenter, center, 0.028F * fade, 0.102F, 0.349F, 0.137F, 0.98F * fade);
                }
                prevCenter = center;
            }
            Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
            Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
            renderOrientedRing(matrix, vc, inst.endOrDir, flatX, flatZ,
                    0.24F * fade, 0.46F * fade, 16, 0.102F, 0.349F, 0.137F, 0.92F * fade);
            return;
        }

        // Section 6.48 Entropic Ash-Chain Flow (#B83C08 / #211A21 / #4D6453): leaping black soulfire & blazing magma-ash tendrils!
        if (isStygianOblivionMateria(inst.essence)) {
            int steps = Math.clamp((int) (len * 4.2D), 12, 36);
            double archHeight = Math.min(0.85D, 0.14D + len * 0.07D);
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
            Vec3 prevCenter = null;
            for (int i = 0; i <= steps; i++) {
                float t = i / (float) steps;
                float arch = Mth.sin(t * (float) Math.PI);
                float whipR = 0.10F * arch * Mth.sin(t * 9.5F - gameTime * 0.55F + inst.seed);
                Vec3 center = inst.start.add(span.scale(t))
                        .add(worldUp.scale(archHeight * arch))
                        .add(right.scale(whipR));
                if (prevCenter != null) {
                    renderCrossSegment(matrix, vc, prevCenter, center, 0.11F * fade, 0.129F, 0.102F, 0.129F, 0.94F * fade);
                    renderCrossSegment(matrix, vc, prevCenter, center, 0.060F * fade, 0.302F, 0.392F, 0.325F, 0.92F * fade);
                    renderCrossSegment(matrix, vc, prevCenter, center, 0.032F * fade, 0.722F, 0.235F, 0.031F, 0.99F * fade);
                }
                prevCenter = center;
            }
            Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
            Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
            renderOrientedRing(matrix, vc, inst.endOrDir, flatX, flatZ,
                    0.26F * fade, 0.50F * fade, 16, 0.722F, 0.235F, 0.031F, 0.95F * fade);
            return;
        }

        // Section 6.50 Quintessential Quad-Harmonic Homing Spike / Orbit Dart (#A88654 / #773214 / #225C7C / #B83C08):
        if (isQuintessentialAetherMateria(inst.essence)) {
            int steps = Math.clamp((int) (len * 4.2D), 12, 36);
            double archHeight = Math.min(0.95D, 0.16D + len * 0.07D);
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
            Vec3 prevCenter = null;
            for (int i = 0; i <= steps; i++) {
                float t = i / (float) steps;
                float arch = Mth.sin(t * (float) Math.PI);
                float spiralR = 0.09F * arch * Mth.sin(t * 10.0F - gameTime * 0.50F + inst.seed);
                Vec3 center = inst.start.add(span.scale(t))
                        .add(worldUp.scale(archHeight * arch))
                        .add(right.scale(spiralR));
                if (prevCenter != null) {
                    renderCrossSegment(matrix, vc, prevCenter, center, 0.11F * fade, 0.133F, 0.361F, 0.486F, 0.88F * fade);
                    renderCrossSegment(matrix, vc, prevCenter, center, 0.062F * fade, 0.659F, 0.525F, 0.329F, 0.98F * fade);
                    renderCrossSegment(matrix, vc, prevCenter, center, 0.030F * fade, ar, ag, ab, 0.99F * fade);
                }
                prevCenter = center;
            }
            Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
            Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
            renderOrientedRing(matrix, vc, inst.endOrDir, flatX, flatZ,
                    0.28F * fade, 0.52F * fade, 20, 0.659F, 0.525F, 0.329F, 0.96F * fade);
            return;
        }

        // Section 6.51 Abyssal Sanguine-Void Chain Flow (#41304E / #211A21 / #4D6453 / #8A0303): wall-phasing dark matter & boiling blood tendrils!
        if (isAbyssalEntropicMateria(inst.essence)) {
            int steps = Math.clamp((int) (len * 4.2D), 12, 36);
            double archHeight = Math.min(0.95D, 0.16D + len * 0.07D);
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
            Vec3 prevCenter = null;
            for (int i = 0; i <= steps; i++) {
                float t = i / (float) steps;
                float arch = Mth.sin(t * (float) Math.PI);
                float whipR = 0.10F * arch * Mth.sin(t * 10.5F - gameTime * 0.55F + inst.seed);
                Vec3 center = inst.start.add(span.scale(t))
                        .add(worldUp.scale(archHeight * arch))
                        .add(right.scale(whipR));
                if (prevCenter != null) {
                    renderCrossSegment(matrix, vc, prevCenter, center, 0.12F * fade, 0.129F, 0.102F, 0.129F, 0.95F * fade);
                    renderCrossSegment(matrix, vc, prevCenter, center, 0.068F * fade, 0.255F, 0.188F, 0.306F, 0.96F * fade);
                    renderCrossSegment(matrix, vc, prevCenter, center, 0.034F * fade, 0.541F, 0.012F, 0.012F, 0.99F * fade);
                }
                prevCenter = center;
            }
            Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
            Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
            renderOrientedRing(matrix, vc, inst.endOrDir, flatX, flatZ,
                    0.28F * fade, 0.54F * fade, 18, 0.541F, 0.012F, 0.012F, 0.96F * fade);
            return;
        }

        // Section 6.52 Holy White-Hole Star-Orbit Flow (#FFEA78 / #FF6B9D / #1A5923 / #A88654): retaliatory starlight & pranic dart arc!
        if (isHolyCelestialMateria(inst.essence)) {
            int steps = Math.clamp((int) (len * 4.2D), 12, 36);
            double archHeight = Math.min(0.90D, 0.14D + len * 0.06D);
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
            Vec3 prevCenter = null;
            for (int i = 0; i <= steps; i++) {
                float t = i / (float) steps;
                float arch = Mth.sin(t * (float) Math.PI);
                Vec3 center = inst.start.add(span.scale(t)).add(worldUp.scale(archHeight * arch));
                if (prevCenter != null) {
                    renderCrossSegment(matrix, vc, prevCenter, center, 0.11F * fade, 1.000F, 0.918F, 0.471F, 0.94F * fade);
                    renderCrossSegment(matrix, vc, prevCenter, center, 0.062F * fade, 1.000F, 0.420F, 0.616F, 0.96F * fade);
                    renderCrossSegment(matrix, vc, prevCenter, center, 0.032F * fade, 1.000F, 1.000F, 1.000F, 0.99F * fade);
                }
                prevCenter = center;
            }
            Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
            Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
            renderOrientedRing(matrix, vc, inst.endOrDir, flatX, flatZ,
                    0.28F * fade, 0.54F * fade, 20, 1.000F, 0.918F, 0.471F, 0.97F * fade);
            return;
        }

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
        float expandProg = Mth.clamp(progress / 0.38F, 0.0F, 1.0F);
        float eased = 1.0F - (1.0F - expandProg) * (1.0F - expandProg) * (1.0F - expandProg);
        float fade = computeEnvelope(progress, 0.04F, 0.54F);
        float curRadius = inst.radiusOrScale * (0.16F + 0.76F * eased + 0.14F * progress);

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
                curRadius * 0.78F, (isAerodynamicMateria(inst.essence) || isTectonicMateria(inst.essence)) ? 8 : 6,
                gameTime * 0.08F + inst.seed, pr, pg, pb, 0.62F * fade);

        if (isAerodynamicMateria(inst.essence)) {
            for (int b = 0; b < 6; b++) {
                float ang = gameTime * 0.25F + (float) (Math.PI * 2.0D * b / 6.0D);
                renderSweptWindCrescent(matrix, vc, ground.add(0.0D, 0.35D + 0.18D * (b % 2), 0.0D),
                        right, forward, curRadius * 0.94F, ang, 0.95F, 0.22F, ar, ag, ab, 0.86F * fade);
            }
        } else if (isHydroMateria(inst.essence)) {
            // Section 6.3 Tidal Nova: 360-degree 3D Curling Tsunami Wave Wall with deep-azure swell & foaming whitewater crest!
            float waveH = Math.max(0.85F, inst.radiusOrScale * 0.28F)
                    * (0.86F + 0.22F * Mth.sin(expandProg * (float) Math.PI) - progress * 0.18F);
            Vec3 midSwell = ground.add(0.0D, waveH * 0.55D, 0.0D);
            Vec3 crestTop = ground.add(0.0D, waveH, 0.0D);
            Vec3 curlLip = ground.add(0.0D, waveH * 0.84D, 0.0D);

            // Deep azure (#225C7C) lower tsunami swell
            renderOrientedFrustumBand(matrix, vc, ground, midSwell, right, forward,
                    Math.max(0.1F, curRadius * 0.82F), curRadius * 0.98F, 32,
                    0.133F, 0.361F, 0.486F, 0.68F * fade, 0.58F * fade);
            // Cerulean rising wave face
            renderOrientedFrustumBand(matrix, vc, midSwell, crestTop, right, forward,
                    curRadius * 0.98F, curRadius * 1.07F, 32,
                    pr, pg, pb, 0.62F * fade, 0.78F * fade);
            // Curling whitewater foam lip overhanging the outer wave front!
            renderOrientedFrustumBand(matrix, vc, crestTop, curlLip, right, forward,
                    curRadius * 1.07F, curRadius * 1.14F, 32,
                    0.90F, 0.99F, 1.00F, 0.92F * fade, 0.45F * fade);
            renderOrientedRing(matrix, vc, crestTop, right, forward,
                    curRadius * 1.02F, curRadius * 1.10F, 32, 0.94F, 0.99F, 1.00F, 0.90F * fade);

            // Trailing concentric surf ripples behind the advancing tsunami wall
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.04D, 0.0D), right, forward,
                    curRadius * 0.56F, curRadius * 0.64F, 28, ar, ag, ab, 0.55F * fade);
        } else if (isBotanicalMateria(inst.essence)) {
            // Section 6.4 Bramble Bloom Explosion: rapid ring of 18 jagged 3D wooden spikes & arching thorny briars!
            int spikeCount = 18;
            for (int s = 0; s < spikeCount; s++) {
                float sAng = (float) (Math.PI * 2.0D * s / spikeCount) + inst.seed * 0.13F;
                Vec3 rad = new Vec3(Mth.cos(sAng), 0.0D, Mth.sin(sAng));
                Vec3 tang = new Vec3(-Mth.sin(sAng), 0.0D, Mth.cos(sAng));

                boolean tallSpike = (s % 2 == 0);
                double baseDist = curRadius * (tallSpike ? 0.78D : 0.62D);
                double tipDist = curRadius * (tallSpike ? 1.08D : 0.94D);
                double spikeH = Math.max(0.75D, inst.radiusOrScale * (tallSpike ? 0.34D : 0.24D))
                        * (0.55D + 0.45D * Mth.sin(Math.min(1.0F, expandProg * 1.15F) * (float) Math.PI * 0.5F));

                Vec3 rootCenter = ground.add(rad.scale(baseDist));
                Vec3 spikeTip = ground.add(rad.scale(tipDist)).add(0.0D, spikeH, 0.0D);
                double w = Math.max(0.10D, curRadius * 0.042D);
                Vec3 b0 = rootCenter.add(tang.scale(w));
                Vec3 b1 = rootCenter.add(rad.scale(w * 0.85D));
                Vec3 b2 = rootCenter.subtract(tang.scale(w));
                Vec3 b3 = rootCenter.subtract(rad.scale(w * 0.85D));

                // Dark wooden bark base (#5C3A1E) -> razor sprout-lime / emerald tip
                emitDoubleSidedQuad(matrix, vc, b0, b1, spikeTip, spikeTip,
                        0.36F, 0.23F, 0.12F, 0.94F * fade, 0.36F, 0.23F, 0.12F, 0.94F * fade,
                        0.71F, 1.00F, 0.47F, 0.92F * fade, 0.71F, 1.00F, 0.47F, 0.92F * fade);
                emitDoubleSidedQuad(matrix, vc, b1, b2, spikeTip, spikeTip,
                        0.36F, 0.23F, 0.12F, 0.94F * fade, 0.36F, 0.23F, 0.12F, 0.94F * fade,
                        0.17F, 0.90F, 0.41F, 0.92F * fade, 0.17F, 0.90F, 0.41F, 0.92F * fade);
                emitDoubleSidedQuad(matrix, vc, b2, b3, spikeTip, spikeTip,
                        0.102F, 0.349F, 0.137F, 0.94F * fade, 0.102F, 0.349F, 0.137F, 0.94F * fade,
                        0.71F, 1.00F, 0.47F, 0.92F * fade, 0.71F, 1.00F, 0.47F, 0.92F * fade);
                emitDoubleSidedQuad(matrix, vc, b3, b0, spikeTip, spikeTip,
                        0.102F, 0.349F, 0.137F, 0.94F * fade, 0.102F, 0.349F, 0.137F, 0.94F * fade,
                        0.17F, 0.90F, 0.41F, 0.92F * fade, 0.17F, 0.90F, 0.41F, 0.92F * fade);
            }

            // 6 overarching thorny briar arches curling outward across the blast zone
            for (int a = 0; a < 6; a++) {
                float aAng = (float) (Math.PI * 2.0D * a / 6.0D) + 0.26F;
                Vec3 rad = new Vec3(Mth.cos(aAng), 0.0D, Mth.sin(aAng));
                Vec3 prevArch = null;
                for (int seg = 0; seg <= 8; seg++) {
                    float u = seg / 8.0F;
                    double d = curRadius * (0.18D + 0.80D * u);
                    double h = Math.sin(u * Math.PI) * Math.max(0.65D, curRadius * 0.26D);
                    Vec3 pt = ground.add(rad.scale(d)).add(0.0D, h, 0.0D);
                    if (prevArch != null) {
                        renderCrossSegment(matrix, vc, prevArch, pt, 0.075F * fade,
                                0.102F, 0.349F, 0.137F, 0.88F * fade);
                    }
                    prevArch = pt;
                }
            }
        } else if (isTectonicMateria(inst.essence)) {
            // Section 6.5 Stone Ring Rampart & Granite Shatter Nova: 16 erupting 3D granite/terracotta monoliths & octagonal parapet!
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
            int pillarCount = 16;
            for (int p = 0; p < pillarCount; p++) {
                float pAng = (float) (Math.PI * 2.0D * p / pillarCount) + inst.seed * 0.09F;
                Vec3 rad = new Vec3(Mth.cos(pAng), 0.0D, Mth.sin(pAng));
                Vec3 tang = new Vec3(-Mth.sin(pAng), 0.0D, Mth.cos(pAng));
                boolean merlon = (p % 2 == 0);
                float hMult = merlon ? 0.44F : 0.30F;
                float spireH = Math.max(0.85F, inst.radiusOrScale * hMult)
                        * (0.45F + 0.55F * Mth.sin(Math.min(1.0F, expandProg * 1.25F) * (float) Math.PI * 0.5F));
                Vec3 basePos = ground.add(rad.scale(curRadius * 0.94D));
                Vec3 tiltAxis = worldUp.add(rad.scale(0.18D)).normalize();
                renderFacetedStoneSpire(matrix, vc, basePos, tiltAxis, rad, tang,
                        Math.max(0.14F, curRadius * 0.068F), spireH, 6, pAng, fade);
            }
            renderOrientedRing(matrix, vc, ground.add(0.0D, Math.max(0.35D, inst.radiusOrScale * 0.16D), 0.0D),
                    right, forward, curRadius * 0.88F, curRadius * 0.98F, 8,
                    0.467F, 0.196F, 0.078F, 0.86F * fade);
        } else if (isGlacialMateria(inst.essence)) {
            // Section 6.6 Frost Nova Burst: expanding 6-pointed snowflake ground seal & 18 erupting jagged 3D glacial ice spikes!
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
            renderSnowflakeCrystal(matrix, vc, ground.add(0.0D, 0.04D, 0.0D), right, forward,
                    curRadius * 0.92F, gameTime * 0.05F + inst.seed, 0.86F * fade);

            int spikeCount = 18;
            for (int s = 0; s < spikeCount; s++) {
                float sAng = (float) (Math.PI * 2.0D * s / spikeCount) + inst.seed * 0.11F;
                Vec3 rad = new Vec3(Mth.cos(sAng), 0.0D, Mth.sin(sAng));
                Vec3 tang = new Vec3(-Mth.sin(sAng), 0.0D, Mth.cos(sAng));
                boolean tallSpike = (s % 2 == 0);
                float hMult = tallSpike ? 0.42F : 0.28F;
                float spireH = Math.max(0.85F, inst.radiusOrScale * hMult)
                        * (0.42F + 0.58F * Mth.sin(Math.min(1.0F, expandProg * 1.25F) * (float) Math.PI * 0.5F));
                Vec3 basePos = ground.add(rad.scale(curRadius * (tallSpike ? 0.92D : 0.76D)));
                Vec3 tiltAxis = worldUp.add(rad.scale(0.38D)).normalize();
                renderFacetedIceSpire(matrix, vc, basePos, tiltAxis, rad, tang,
                        Math.max(0.13F, curRadius * 0.058F), spireH, 6, sAng, fade);
            }
        } else if (isInfernalMateria(inst.essence)) {
            // Section 6.7 Hellfire Ring Shockwave: 6-block roaring 360° wave of 20 erupting 3D flame tongues & molten caldera ring!
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    curRadius * 0.84F, curRadius * 1.02F, 28,
                    0.722F, 0.235F, 0.031F, 0.88F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.07D, 0.0D), right, forward,
                    curRadius * 0.90F, curRadius * 0.97F, 28,
                    1.00F, 0.820F, 0.361F, 0.94F * fade);

            int flameCount = 20;
            for (int f = 0; f < flameCount; f++) {
                float fAng = (float) (Math.PI * 2.0D * f / flameCount) + inst.seed * 0.13F;
                Vec3 rad = new Vec3(Mth.cos(fAng), 0.0D, Mth.sin(fAng));
                Vec3 tang = new Vec3(-Mth.sin(fAng), 0.0D, Mth.cos(fAng));
                boolean tallFlame = (f % 2 == 0);
                float flicker = 0.84F + 0.22F * Mth.sin(gameTime * 0.36F + f * 1.1F);
                float flameH = Math.max(0.95F, inst.radiusOrScale * (tallFlame ? 0.44F : 0.30F)) * flicker
                        * (0.40F + 0.60F * Mth.sin(Math.min(1.0F, expandProg * 1.25F) * (float) Math.PI * 0.5F));
                Vec3 basePos = ground.add(rad.scale(curRadius * (tallFlame ? 0.94D : 0.80D)));
                Vec3 tiltAxis = worldUp.add(rad.scale(0.32D)).normalize();
                renderHellfireFlameTongue(matrix, vc, basePos, tiltAxis, rad, tang,
                        Math.max(0.18F, curRadius * 0.075F), flameH, fade);
            }
        } else if (isSolarRadiantMateria(inst.essence)) {
            // Section 6.8 Supernova Flashbang: brilliant full 3D golden solar photosphere, 16 spearing sun-rays & twin celestial halos!
            Vec3 sunCenter = inst.start.add(0.0D, 0.85D, 0.0D);
            renderHemisphereShell(matrix, vc, sunCenter, curRadius * 0.72F, 7, 22,
                    1.00F, 0.96F, 0.76F, 0.42F * fade);
            renderLowerHemisphereShell(matrix, vc, sunCenter, curRadius * 0.72F, 6, 22,
                    1.00F, 0.886F, 0.471F, 0.34F * fade);
            if (progress < 0.55F) {
                float coreFlash = 1.0F - (progress / 0.55F);
                renderOctahedron(matrix, vc, sunCenter, Math.max(0.45F, curRadius * 0.38F * coreFlash),
                        1.00F, 0.995F, 0.96F, 0.96F * coreFlash);
            }

            // Twin tilted expanding golden celestial shockwave halos
            float tiltSpin = gameTime * 0.08F + inst.seed;
            Vec3 h1U = new Vec3(Mth.cos(tiltSpin), 0.28D, Mth.sin(tiltSpin)).normalize();
            Vec3 h1V = new Vec3(-Mth.sin(tiltSpin), 0.28D, Mth.cos(tiltSpin)).normalize();
            renderOrientedRing(matrix, vc, sunCenter, h1U, h1V, curRadius * 0.88F, curRadius * 1.02F, 30,
                    1.00F, 0.886F, 0.471F, 0.92F * fade);
            renderOrientedStarPolygon(matrix, vc, ground.add(0.0D, 0.06D, 0.0D), right, forward,
                    curRadius * 0.98F, 12, -tiltSpin * 0.7F, 1.00F, 0.98F, 0.88F, 0.85F * fade);

            // 16 spearing 3D golden-white sun-rays radiating in 3D from the Supernova core
            int rayCount = 16;
            for (int i = 0; i < rayCount; i++) {
                float phi = (float) (Math.PI * 2.0D * i / rayCount) + inst.seed * 0.17F;
                double elev = ((i % 4) - 1.2D) * 0.34D;
                Vec3 rDir = new Vec3(Mth.cos(phi), elev, Mth.sin(phi)).normalize();
                Vec3 rTang = buildPerpendicularRight(rDir);
                Vec3 rUp = rDir.cross(rTang).normalize();
                float rayLen = curRadius * ((i % 2 == 0) ? 1.18F : 0.88F);
                renderSolarStarburstRay(matrix, vc, sunCenter.add(rDir.scale(curRadius * 0.16D)),
                        rDir, rTang, rUp, Math.max(0.11F, curRadius * 0.048F), rayLen, fade);
            }
        } else if (isUmbralShadowMateria(inst.essence)) {
            // Section 6.9 Smoke Veil Shockwave & Void-Blind Mine: silent, expanding cloud of dense black umbral mist!
            Vec3 mistBase = ground.add(0.0D, 0.06D, 0.0D);
            renderHemisphereShell(matrix, vc, mistBase, curRadius * 0.92F, 7, 24,
                    0.095F, 0.072F, 0.110F, 0.68F * fade);
            renderHemisphereShell(matrix, vc, mistBase, curRadius * 0.68F, 6, 20,
                    0.129F, 0.102F, 0.129F, 0.76F * fade);
            renderOrientedRing(matrix, vc, mistBase.add(0.0D, 0.04D, 0.0D), right, forward,
                    curRadius * 0.80F, curRadius * 1.03F, 30, 0.129F, 0.102F, 0.129F, 0.92F * fade);
            renderOrientedRing(matrix, vc, mistBase.add(0.0D, 0.07D, 0.0D), right, forward,
                    curRadius * 0.98F, curRadius * 1.06F, 30, 0.56F, 0.36F, 1.00F, 0.78F * fade);

            // 16 creeping ground-hugging umbral smoke tendrils sweeping across the shockwave radius
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
            for (int t = 0; t < 16; t++) {
                float tAng = (float) (Math.PI * 2.0D * t / 16.0D) + inst.seed * 0.15F;
                Vec3 rad = new Vec3(Mth.cos(tAng), 0.0D, Mth.sin(tAng));
                Vec3 tang = new Vec3(-Mth.sin(tAng), 0.0D, Mth.cos(tAng));
                Vec3 tilt = worldUp.scale(0.38D).add(rad.scale(0.92D)).normalize();
                renderUmbralShadowNeedle(matrix, vc, mistBase.add(rad.scale(curRadius * 0.68D)),
                        tilt, rad, tang, Math.max(0.14F, curRadius * 0.055F), curRadius * 0.46F, fade);
            }
        } else if (isNecroticUndeadMateria(inst.essence)) {
            // Section 6.10 Corpse Explosion / Bone Nova: violent 360-degree eruption of 18 jagged curved 3D ribcage spikes
            // in a 6-block radius out of #4D6453 grave-earth & #5EE088 necrotic soul rings!
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    curRadius * 0.76F, curRadius * 1.02F, 28, 0.302F, 0.392F, 0.325F, 0.88F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.08D, 0.0D), right, forward,
                    curRadius * 0.90F, curRadius * 0.98F, 28, 0.369F, 0.878F, 0.533F, 0.92F * fade);

            int ribCount = 18;
            for (int s = 0; s < ribCount; s++) {
                float sAng = (float) (Math.PI * 2.0D * s / ribCount) + inst.seed * 0.11F;
                Vec3 rad = new Vec3(Mth.cos(sAng), 0.0D, Mth.sin(sAng));
                Vec3 tang = new Vec3(-Mth.sin(sAng), 0.0D, Mth.cos(sAng));
                boolean outerRib = (s % 2 == 0);
                float ribLen = Math.max(1.05F, inst.radiusOrScale * (outerRib ? 0.46F : 0.33F))
                        * (0.38F + 0.62F * Mth.sin(Math.min(1.0F, expandProg * 1.25F) * (float) Math.PI * 0.5F));
                Vec3 ribBase = ground.add(rad.scale(curRadius * (outerRib ? 0.88D : 0.68D)));
                Vec3 axisDir = worldUp.scale(0.76D).add(rad.scale(0.65D)).normalize();
                Vec3 bendDir = rad.scale(-0.72D).add(worldUp.scale(0.35D)).normalize();
                renderSkeletalRibSpike(matrix, vc, ribBase, axisDir, bendDir, tang,
                        Math.max(0.11F, curRadius * 0.048F), ribLen, fade);
            }
        } else if (isAridDesertMateria(inst.essence)) {
            // Section 6.12 Haboob Sandstorm Nova: explosive 360-degree desert dust wall (6-block radius)
            // with swirling #C2B28F sandstorm curtain, 8 desert wind crescents & 16 tumbling sandstone chunks!
            float wallH = Math.max(1.15F, inst.radiusOrScale * 0.36F)
                    * (0.75F + 0.35F * Mth.sin(expandProg * (float) Math.PI));
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, wallH, 0.0D), right, forward,
                    Math.max(0.15F, curRadius * 0.84F), curRadius * 1.06F, 30,
                    0.761F, 0.698F, 0.561F, 0.72F * fade, 0.26F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, wallH * 0.62D, 0.0D), right, forward,
                    Math.max(0.12F, curRadius * 0.76F), curRadius * 0.96F, 28,
                    0.612F, 0.478F, 0.298F, 0.62F * fade, 0.22F * fade);

            for (int b = 0; b < 8; b++) {
                float ang = gameTime * 0.28F + (float) (Math.PI * 2.0D * b / 8.0D);
                renderSweptWindCrescent(matrix, vc, ground.add(0.0D, 0.28D + 0.22D * (b % 3), 0.0D),
                        right, forward, curRadius * 0.98F, ang, 0.95F, 0.24F,
                        0.961F, 0.894F, 0.745F, 0.86F * fade);
            }
            for (int c = 0; c < 16; c++) {
                float cAng = (float) (Math.PI * 2.0D * c / 16.0D) + inst.seed * 0.13F;
                Vec3 cPos = ground.add(Mth.cos(cAng) * curRadius * 0.92D,
                        0.18D + (c % 3) * 0.26D, Mth.sin(cAng) * curRadius * 0.92D);
                boolean sunlit = (c % 2 == 0);
                renderOctahedron(matrix, vc, cPos, Math.max(0.10F, curRadius * 0.036F) * fade,
                        sunlit ? 0.961F : 0.612F, sunlit ? 0.894F : 0.478F, sunlit ? 0.745F : 0.298F, 0.92F * fade);
            }
        } else if (isGalvanicLightningMateria(inst.essence)) {
            // Section 6.13 Static EMP Nova: brilliant 360-degree #FFE600 electric dome & 16 jagged ground-skimming lightning arcs!
            int flicker = (int) (gameTime * 1.2F) + inst.seed * 7;
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    curRadius * 0.86F, curRadius * 1.04F, 30, 1.00F, 0.902F, 0.00F, 0.94F * fade);
            for (int aIdx = 0; aIdx < 16; aIdx++) {
                float baseAng = (float) (Math.PI * 2.0D * aIdx / 16.0D);
                Vec3 prev = ground.add(0.0D, 0.22D, 0.0D);
                for (int s = 1; s <= 4; s++) {
                    float st = s / 4.0F;
                    float rStep = curRadius * st;
                    float jAng = baseAng + (hashFloat(flicker + aIdx, s * 7) - 0.5F) * 0.28F;
                    double yStep = 0.14D + (hashFloat(flicker + aIdx, s * 11) * 0.45D * (1.0F - st * 0.5F));
                    Vec3 next = ground.add(Mth.cos(jAng) * rStep, yStep, Mth.sin(jAng) * rStep);
                    renderCrossSegment(matrix, vc, prev, next, 0.085F * fade, 1.00F, 0.902F, 0.00F, 0.90F * fade);
                    renderCrossSegment(matrix, vc, prev, next, 0.036F * fade, 1.00F, 1.00F, 0.96F, 0.98F * fade);
                    prev = next;
                }
            }
        } else if (isMagmaCalderaMateria(inst.essence)) {
            // Section 6.14 Caldera Shockwave Nova: 360-degree volcanic eruption ring, 16 molten lava plumes & dark pumice boulders!
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.045D, 0.0D), right, forward,
                    curRadius * 0.72F, curRadius * 1.06F, 30, 0.165F, 0.090F, 0.071F, 0.88F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.065D, 0.0D), right, forward,
                    curRadius * 0.82F, curRadius * 0.98F, 30, 0.722F, 0.235F, 0.031F, 0.94F * fade);
            for (int f = 0; f < 16; f++) {
                float fAng = (float) (Math.PI * 2.0D * f / 16.0D) + inst.seed * 0.11F;
                Vec3 rad = new Vec3(Mth.cos(fAng), 0.0D, Mth.sin(fAng));
                Vec3 tang = new Vec3(-Mth.sin(fAng), 0.0D, Mth.cos(fAng));
                Vec3 plumeBase = ground.add(rad.scale(curRadius * 0.90D));
                renderHellfireFlameTongue(matrix, vc, plumeBase, worldUp.add(rad.scale(0.32D)).normalize(), rad, tang,
                        Math.max(0.18F, curRadius * 0.065F), Math.max(0.68F, curRadius * (f % 2 == 0 ? 0.30F : 0.20F)), fade);
                Vec3 boulderPos = plumeBase.add(0.0D, 0.32D + (f % 3) * 0.24D, 0.0D);
                renderOctahedron(matrix, vc, boulderPos, Math.max(0.12F, curRadius * 0.042F) * fade,
                        0.165F, 0.090F, 0.071F, 0.90F * fade);
            }
        } else if (isTempestStormMateria(inst.essence)) {
            // Section 6.15 Tempest Nova Burst: 360-degree #429BA3 hurricane squall wall & 6 sweeping seafoam gale-crescents!
            renderOrientedFrustumBand(matrix, vc,
                    ground.add(0.0D, 0.04D, 0.0D), ground.add(0.0D, Math.min(2.6D, curRadius * 0.38D), 0.0D),
                    right, forward, curRadius * 0.84F, curRadius * 1.06F, 30,
                    0.259F, 0.608F, 0.639F, 0.82F * fade, 0.16F * fade);
            for (int c = 0; c < 6; c++) {
                float cAng = progress * 5.2F + (float) (Math.PI * 2.0D * c / 6.0D);
                renderSweptWindCrescent(matrix, vc, ground.add(0.0D, 0.28D + (c % 3) * 0.44D, 0.0D),
                        right, forward, curRadius * 0.96F, cAng, 1.25F, Math.max(0.24F, curRadius * 0.085F),
                        0.847F, 0.980F, 1.000F, 0.92F * fade);
            }
        } else if (isPermafrostGlacialMateria(inst.essence)) {
            // Section 6.16 Hoarfrost Blizzard Shockwave: expanding #98DCF2 cryogenic fog wall, 6-pointed snowflake seal & 12 rime spires!
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
            renderSnowflakeCrystal(matrix, vc, ground.add(0.0D, 0.055D, 0.0D), right, forward,
                    curRadius * 0.98F, progress * 1.6F, 0.86F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    ground.add(0.0D, 0.04D, 0.0D), ground.add(0.0D, Math.min(2.4D, curRadius * 0.36D), 0.0D),
                    right, forward, curRadius * 0.82F, curRadius * 1.05F, 28,
                    0.596F, 0.863F, 0.949F, 0.78F * fade, 0.14F * fade);
            for (int s = 0; s < 12; s++) {
                float sAng = (float) (Math.PI * 2.0D * s / 12.0D) + inst.seed * 0.13F;
                Vec3 rad = new Vec3(Mth.cos(sAng), 0.0D, Mth.sin(sAng));
                Vec3 tang = new Vec3(-Mth.sin(sAng), 0.0D, Mth.cos(sAng));
                Vec3 spireBase = ground.add(rad.scale(curRadius * 0.92D));
                renderFacetedIceSpire(matrix, vc, spireBase, worldUp.add(rad.scale(0.42D)).normalize(), rad, tang,
                        Math.max(0.14F, curRadius * 0.048F), Math.max(0.56F, curRadius * (s % 2 == 0 ? 0.24F : 0.17F)),
                        5, sAng, fade * 0.92F);
            }
        } else if (isOvergrowthRampartMateria(inst.essence)) {
            // Section 6.17 Bramble Rampart Bloom: 360-degree eruption of 14 mossy stone spires & thorny emerald bramble arches!
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    curRadius * 0.78F, curRadius * 1.05F, 28, 0.102F, 0.349F, 0.137F, 0.88F * fade);
            for (int s = 0; s < 14; s++) {
                float sAng = (float) (Math.PI * 2.0D * s / 14.0D) + inst.seed * 0.11F;
                Vec3 rad = new Vec3(Mth.cos(sAng), 0.0D, Mth.sin(sAng));
                Vec3 tang = new Vec3(-Mth.sin(sAng), 0.0D, Mth.cos(sAng));
                Vec3 spireBase = ground.add(rad.scale(curRadius * 0.90D));
                renderFacetedStoneSpire(matrix, vc, spireBase, worldUp.add(rad.scale(0.32D)).normalize(), rad, tang,
                        Math.max(0.18F, curRadius * 0.060F), Math.max(0.75F, curRadius * (s % 2 == 0 ? 0.32F : 0.22F)),
                        5, sAng, fade);
                renderCrossSegment(matrix, vc, spireBase, spireBase.add(0.0D, 0.85D, 0.0D).add(tang.scale(0.35D)),
                        0.055F * fade, 0.102F, 0.349F, 0.137F, 0.94F * fade);
            }
        } else if (isAbrasiveDustMateria(inst.essence)) {
            // Section 6.18 Whiteout Powder Nova: explosive 360-degree #C2B28F / #F2ECE1 chalky dust cloud (6m radius),
            // dense whiteout powder curtain, 8 sweeping pneumatic grit crescents & 16 mineral powder clusters!
            float cloudH = Math.max(1.35F, inst.radiusOrScale * 0.42F)
                    * (0.78F + 0.32F * Mth.sin(expandProg * (float) Math.PI));
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, cloudH, 0.0D), right, forward,
                    Math.max(0.16F, curRadius * 0.82F), curRadius * 1.08F, 32,
                    0.949F, 0.925F, 0.882F, 0.74F * fade, 0.22F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, cloudH * 0.65D, 0.0D), right, forward,
                    Math.max(0.14F, curRadius * 0.74F), curRadius * 0.96F, 28,
                    0.761F, 0.698F, 0.561F, 0.68F * fade, 0.24F * fade);

            for (int c = 0; c < 8; c++) {
                float cAng = gameTime * 0.34F + (float) (Math.PI * 2.0D * c / 8.0D);
                renderSweptWindCrescent(matrix, vc, ground.add(0.0D, 0.26D + 0.24D * (c % 3), 0.0D),
                        right, forward, curRadius * 0.98F, cAng, 1.15F, 0.26F,
                        0.949F, 0.925F, 0.882F, 0.90F * fade);
            }
            for (int g = 0; g < 16; g++) {
                float gAng = (float) (Math.PI * 2.0D * g / 16.0D) + inst.seed * 0.13F;
                Vec3 gPos = ground.add(Mth.cos(gAng) * curRadius * 0.92D,
                        0.20D + (g % 3) * 0.28D, Mth.sin(gAng) * curRadius * 0.92D);
                boolean chalk = (g % 2 == 0);
                renderOctahedron(matrix, vc, gPos, Math.max(0.11F, curRadius * 0.038F) * fade,
                        chalk ? 0.949F : 0.761F, chalk ? 0.925F : 0.698F, chalk ? 0.882F : 0.561F, 0.92F * fade);
            }
        } else if (isSymbioticSporeMateria(inst.essence)) {
            // Section 6.19 Fungal Bloom Nova: outward 360-degree #1A5923 / #A88654 spore ring with wind-pressure crescents
            // & 12 blooming 3D mushroom puffballs!
            float sporeH = Math.max(1.25F, inst.radiusOrScale * 0.38F)
                    * (0.76F + 0.30F * Mth.sin(expandProg * (float) Math.PI));
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, sporeH, 0.0D), right, forward,
                    Math.max(0.16F, curRadius * 0.80F), curRadius * 1.06F, 30,
                    0.102F, 0.349F, 0.137F, 0.72F * fade, 0.22F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, sporeH * 0.62D, 0.0D), right, forward,
                    Math.max(0.14F, curRadius * 0.72F), curRadius * 0.94F, 26,
                    0.659F, 0.525F, 0.329F, 0.68F * fade, 0.24F * fade);

            for (int c = 0; c < 6; c++) {
                float cAng = gameTime * 0.28F + (float) (Math.PI * 2.0D * c / 6.0D);
                renderSweptWindCrescent(matrix, vc, ground.add(0.0D, 0.24D + 0.22D * (c % 3), 0.0D),
                        right, forward, curRadius * 0.96F, cAng, 1.10F, 0.24F,
                        0.910F, 0.784F, 0.510F, 0.88F * fade);
            }
            for (int m = 0; m < 12; m++) {
                float mAng = (float) (Math.PI * 2.0D * m / 12.0D) + inst.seed * 0.15F;
                Vec3 mPos = ground.add(Mth.cos(mAng) * curRadius * 0.88D,
                        0.22D + (m % 3) * 0.22D, Mth.sin(mAng) * curRadius * 0.88D);
                boolean goldCap = (m % 2 == 0);
                renderOctahedron(matrix, vc, mPos, Math.max(0.13F, curRadius * 0.042F) * fade,
                        goldCap ? 0.659F : 0.173F, goldCap ? 0.525F : 0.902F, goldCap ? 0.329F : 0.408F, 0.94F * fade);
            }
        } else if (isBorealTaigaMateria(inst.essence)) {
            // Section 6.20 Boreal Blizzard Nova: explosive 360-degree #1A5923 / #98DCF2 frosted pine needles & ice shards!
            float blizzardH = Math.max(1.30F, inst.radiusOrScale * 0.38F)
                    * (0.78F + 0.28F * Mth.sin(expandProg * (float) Math.PI));
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, blizzardH, 0.0D), right, forward,
                    Math.max(0.16F, curRadius * 0.82F), curRadius * 1.06F, 30,
                    0.102F, 0.349F, 0.137F, 0.74F * fade, 0.22F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, blizzardH * 0.66D, 0.0D), right, forward,
                    Math.max(0.14F, curRadius * 0.74F), curRadius * 0.96F, 28,
                    0.596F, 0.863F, 0.949F, 0.72F * fade, 0.24F * fade);
            renderSnowflakeCrystal(matrix, vc, ground.add(0.0D, 0.06D, 0.0D), right, forward,
                    curRadius * 0.96F, gameTime * 0.08F + inst.seed, 0.84F * fade);
            for (int c = 0; c < 6; c++) {
                float cAng = gameTime * 0.34F + (float) (Math.PI * 2.0D * c / 6.0D);
                renderSweptWindCrescent(matrix, vc, ground.add(0.0D, 0.22D + 0.24D * (c % 3), 0.0D),
                        right, forward, curRadius * 0.96F, cAng, 1.18F, 0.24F,
                        (c % 2 == 0) ? 0.596F : 0.102F,
                        (c % 2 == 0) ? 0.863F : 0.349F,
                        (c % 2 == 0) ? 0.949F : 0.137F,
                        0.90F * fade);
            }
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
            for (int s = 0; s < 12; s++) {
                float sAng = (float) (Math.PI * 2.0D * s / 12.0D) + inst.seed * 0.13F;
                Vec3 rad = new Vec3(Mth.cos(sAng), 0.0D, Mth.sin(sAng));
                Vec3 tang = new Vec3(-Mth.sin(sAng), 0.0D, Mth.cos(sAng));
                Vec3 tilt = worldUp.add(rad.scale(0.55D)).normalize();
                renderFacetedIceSpire(matrix, vc, ground.add(rad.scale(curRadius * 0.90D)), tilt, rad, tang,
                        Math.max(0.10F, curRadius * 0.036F), Math.max(0.45F, curRadius * 0.14F) * fade, 5, sAng, fade * 0.92F);
            }
        } else if (isMiracleOasisMateria(inst.essence)) {
            // Section 6.21 Miracle Oasis Bloom Nova: explosive 360-degree #38C6F4 springwater surge,
            // #1A5923 lush desert-lotus mandala & #C2B28F sunlit desert flowers!
            float surgeH = Math.max(1.15F, inst.radiusOrScale * 0.34F)
                    * (0.76F + 0.28F * Mth.sin(expandProg * (float) Math.PI));
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, surgeH, 0.0D), right, forward,
                    Math.max(0.16F, curRadius * 0.82F), curRadius * 1.06F, 30,
                    0.220F, 0.776F, 0.957F, 0.72F * fade, 0.18F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, surgeH * 0.62D, 0.0D), right, forward,
                    Math.max(0.14F, curRadius * 0.74F), curRadius * 0.96F, 28,
                    0.102F, 0.349F, 0.137F, 0.74F * fade, 0.22F * fade);
            renderBotanicalBlossom(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    curRadius * 0.96F, 8, gameTime * 0.06F + inst.seed, 0.102F, 0.349F, 0.137F, 0.82F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.07D, 0.0D), right, forward,
                    curRadius * 0.82F, curRadius * 0.98F, 30, 0.761F, 0.698F, 0.561F, 0.88F * fade);
            for (int b = 0; b < 12; b++) {
                float bAng = (float) (Math.PI * 2.0D * b / 12.0D) + inst.seed * 0.15F;
                Vec3 bPos = ground.add(Mth.cos(bAng) * curRadius * 0.88D,
                        0.20D + (b % 3) * 0.22D, Mth.sin(bAng) * curRadius * 0.88D);
                boolean goldPetal = (b % 2 == 0);
                renderOctahedron(matrix, vc, bPos, Math.max(0.14F, curRadius * 0.045F) * fade,
                        goldPetal ? 0.871F : 0.133F,
                        goldPetal ? 0.788F : 0.471F,
                        goldPetal ? 0.604F : 0.188F,
                        0.95F * fade);
            }
        } else if (isCellularVitaeMateria(inst.essence)) {
            // Section 6.22 Cellular Overcharge Bloom (#FF6B9D): outward 6m biological shockwave,
            // rose blossom mandala & 12 glowing life-essence orbs!
            float bioH = Math.max(1.15F, inst.radiusOrScale * 0.34F)
                    * (0.76F + 0.28F * Mth.sin(expandProg * (float) Math.PI));
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, bioH, 0.0D), right, forward,
                    Math.max(0.16F, curRadius * 0.82F), curRadius * 1.06F, 30,
                    1.000F, 0.420F, 0.616F, 0.74F * fade, 0.20F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, bioH * 0.60D, 0.0D), right, forward,
                    Math.max(0.14F, curRadius * 0.74F), curRadius * 0.96F, 28,
                    1.000F, 0.820F, 0.890F, 0.68F * fade, 0.18F * fade);
            renderBotanicalBlossom(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    curRadius * 0.96F, 8, gameTime * 0.07F + inst.seed, 1.000F, 0.420F, 0.616F, 0.84F * fade);
            for (int o = 0; o < 12; o++) {
                float oAng = (float) (Math.PI * 2.0D * o / 12.0D) + inst.seed * 0.16F;
                Vec3 oPos = ground.add(Mth.cos(oAng) * curRadius * 0.88D,
                        0.22D + (o % 3) * 0.22D, Mth.sin(oAng) * curRadius * 0.88D);
                boolean pearl = (o % 2 == 0);
                renderOctahedron(matrix, vc, oPos, Math.max(0.14F, curRadius * 0.046F) * fade,
                        1.000F, pearl ? 0.820F : 0.420F, pearl ? 0.890F : 0.616F, 0.96F * fade);
            }
        } else if (isSanguineBloodMateria(inst.essence)) {
            // Section 6.23 Boiling Blood Mist Detonation (#8A0303): 6m expanding cloud of boiling crimson vapor,
            // arrow-melting heat shroud & 14 coagulated blood-droplet shards!
            float mistH = Math.max(1.35F, inst.radiusOrScale * 0.38F)
                    * (0.80F + 0.28F * Mth.sin(expandProg * (float) Math.PI));
            renderHemisphereShell(matrix, vc, ground, curRadius * 1.02F, 6, 26,
                    0.541F, 0.012F, 0.012F, 0.52F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, mistH, 0.0D), right, forward,
                    Math.max(0.16F, curRadius * 0.78F), curRadius * 1.08F, 30,
                    0.541F, 0.012F, 0.012F, 0.82F * fade, 0.24F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, mistH * 0.55D, 0.0D), right, forward,
                    Math.max(0.14F, curRadius * 0.70F), curRadius * 0.95F, 28,
                    1.000F, 0.220F, 0.298F, 0.68F * fade, 0.18F * fade);
            for (int d = 0; d < 14; d++) {
                float dAng = (float) (Math.PI * 2.0D * d / 14.0D) + inst.seed * 0.15F;
                Vec3 dPos = ground.add(Mth.cos(dAng) * curRadius * 0.90D,
                        0.26D + (d % 3) * 0.28D, Mth.sin(dAng) * curRadius * 0.90D);
                boolean arterial = (d % 3 == 0);
                renderOctahedron(matrix, vc, dPos, Math.max(0.14F, curRadius * 0.048F) * fade,
                        arterial ? 1.000F : 0.541F,
                        arterial ? 0.220F : 0.012F,
                        arterial ? 0.298F : 0.012F,
                        0.95F * fade);
            }
        } else if (isFossilAmberMateria(inst.essence)) {
            // Section 6.25 Amber Crystallization Nova (#FF6B9D / #773214 / #F29B2E): 6m explosive shockwave of
            // golden resin & 14 jagged amber crystal shards!
            float resinH = Math.max(1.25F, inst.radiusOrScale * 0.34F)
                    * (0.78F + 0.26F * Mth.sin(expandProg * (float) Math.PI));
            renderHemisphereShell(matrix, vc, ground, curRadius * 1.01F, 6, 24,
                    0.949F, 0.608F, 0.180F, 0.44F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, resinH, 0.0D), right, forward,
                    Math.max(0.18F, curRadius * 0.80F), curRadius * 1.06F, 24,
                    0.467F, 0.196F, 0.078F, 0.84F * fade, 0.22F * fade);
            for (int s = 0; s < 14; s++) {
                float sAng = (float) (Math.PI * 2.0D * s / 14.0D) + inst.seed * 0.13F;
                Vec3 sPos = ground.add(Mth.cos(sAng) * curRadius * 0.92D,
                        0.24D + (s % 3) * 0.26D, Mth.sin(sAng) * curRadius * 0.92D);
                boolean rose = (s % 2 == 0);
                renderOctahedron(matrix, vc, sPos, Math.max(0.16F, curRadius * 0.052F) * fade,
                        rose ? 1.000F : 0.949F,
                        rose ? 0.420F : 0.608F,
                        rose ? 0.616F : 0.180F,
                        0.96F * fade);
            }
        } else if (isParadoxEclipseMateria(inst.essence)) {
            // Section 6.26 Total Eclipse Nova (#FFEA78 / #211A21): 6m blast of pitch-black darkness encircled by
            // blazing coronal gold & 16 coronal flare spikes!
            float coronaH = Math.max(1.40F, inst.radiusOrScale * 0.38F)
                    * (0.82F + 0.26F * Mth.sin(expandProg * (float) Math.PI));
            renderHemisphereShell(matrix, vc, ground, curRadius * 0.96F, 6, 26,
                    0.090F, 0.070F, 0.095F, 0.68F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.09D, 0.0D), right, forward,
                    curRadius * 0.88F, curRadius * 1.12F, 32,
                    1.000F, 0.918F, 0.471F, 0.96F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, coronaH, 0.0D), right, forward,
                    Math.max(0.18F, curRadius * 0.82F), curRadius * 1.10F, 30,
                    1.000F, 0.918F, 0.471F, 0.88F * fade, 0.20F * fade);
            for (int f = 0; f < 16; f++) {
                float fAng = (float) (Math.PI * 2.0D * f / 16.0D) + inst.seed * 0.11F;
                Vec3 fBase = ground.add(Mth.cos(fAng) * curRadius * 0.92D, 0.12D, Mth.sin(fAng) * curRadius * 0.92D);
                Vec3 fTip = fBase.add(Mth.cos(fAng) * 0.65D, 0.42D + (f % 2) * 0.28D, Mth.sin(fAng) * 0.65D);
                renderCrossSegment(matrix, vc, fBase, fTip, 0.065F * fade,
                        1.000F, 0.918F, 0.471F, 0.96F * fade);
            }
        } else if (isCadaverousBlightMateria(inst.essence)) {
            // Section 6.27 Cadaverous Briar Nova (#4D6453 / #34CC48): 6m ring of rotting bone thorns & toxic miasma spores!
            float miasmaH = Math.max(1.30F, inst.radiusOrScale * 0.36F)
                    * (0.80F + 0.26F * Mth.sin(expandProg * (float) Math.PI));
            renderHemisphereShell(matrix, vc, ground, curRadius * 1.00F, 6, 24,
                    0.204F, 0.800F, 0.282F, 0.42F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, miasmaH, 0.0D), right, forward,
                    Math.max(0.18F, curRadius * 0.80F), curRadius * 1.06F, 26,
                    0.302F, 0.392F, 0.325F, 0.84F * fade, 0.22F * fade);
            for (int t = 0; t < 16; t++) {
                float tAng = (float) (Math.PI * 2.0D * t / 16.0D) + inst.seed * 0.13F;
                Vec3 tBase = ground.add(Mth.cos(tAng) * curRadius * 0.90D, 0.06D, Mth.sin(tAng) * curRadius * 0.90D);
                Vec3 tTip = tBase.add(Mth.cos(tAng) * 0.48D, 0.56D + (t % 2) * 0.24D, Mth.sin(tAng) * 0.48D);
                boolean bone = (t % 2 == 0);
                renderCrossSegment(matrix, vc, tBase, tTip, 0.062F * fade,
                        bone ? 0.902F : 0.204F,
                        bone ? 0.875F : 0.800F,
                        bone ? 0.812F : 0.282F,
                        0.96F * fade);
            }
        } else if (isCosmicAstralMateria(inst.essence)) {
            // Section 6.28 Supernova Gravitational Wave (#41304E / #FFE600): 6m 360° cosmic plasma shockwave & electric arcs!
            float plasmaH = Math.max(1.35F, inst.radiusOrScale * 0.38F)
                    * (0.82F + 0.25F * Mth.sin(expandProg * (float) Math.PI));
            renderHemisphereShell(matrix, vc, ground, curRadius * 1.02F, 6, 26,
                    0.255F, 0.188F, 0.306F, 0.46F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, plasmaH, 0.0D), right, forward,
                    Math.max(0.20F, curRadius * 0.82F), curRadius * 1.08F, 28,
                    1.000F, 0.902F, 0.000F, 0.88F * fade, 0.20F * fade);
            for (int r = 0; r < 16; r++) {
                float rAng = (float) (Math.PI * 2.0D * r / 16.0D) + inst.seed * 0.15F;
                Vec3 rBase = ground.add(Mth.cos(rAng) * curRadius * 0.92D, 0.10D, Mth.sin(rAng) * curRadius * 0.92D);
                Vec3 rTip = rBase.add(Mth.cos(rAng) * 0.62D, 0.52D + (r % 2) * 0.28D, Mth.sin(rAng) * 0.62D);
                renderCrossSegment(matrix, vc, rBase, rTip, 0.064F * fade,
                        1.000F, 0.902F, 0.000F, 0.96F * fade);
            }
        } else if (isPhantasmalSoulfireMateria(inst.essence)) {
            // Section 6.29 Phantasmal Soul Nova (#1DF0E5 / #0D6469): 6m 360° ring of howling cyan soulflames!
            float soulH = Math.max(1.40F, inst.radiusOrScale * 0.40F)
                    * (0.82F + 0.26F * Mth.sin(expandProg * (float) Math.PI));
            renderHemisphereShell(matrix, vc, ground, curRadius * 1.00F, 6, 26,
                    0.051F, 0.392F, 0.412F, 0.44F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, soulH, 0.0D), right, forward,
                    Math.max(0.20F, curRadius * 0.82F), curRadius * 1.08F, 28,
                    0.114F, 0.941F, 0.898F, 0.88F * fade, 0.18F * fade);
            for (int f = 0; f < 16; f++) {
                float fAng = (float) (Math.PI * 2.0D * f / 16.0D) + inst.seed * 0.17F;
                Vec3 fBase = ground.add(Mth.cos(fAng) * curRadius * 0.92D, 0.06D, Mth.sin(fAng) * curRadius * 0.92D);
                Vec3 fTip = fBase.add(Mth.cos(fAng) * 0.42D, 0.68D + (f % 2) * 0.32D, Mth.sin(fAng) * 0.42D);
                renderCrossSegment(matrix, vc, fBase, fTip, 0.068F * fade,
                        0.114F, 0.941F, 0.898F, 0.96F * fade);
            }
        } else if (isScaldingVaporMateria(inst.essence)) {
            // Section 6.30 Scalding Pressure Blast (#E0F7FA / #30A9B0): 6m pneumatic blast of compressed boiling steam!
            float steamH = Math.max(1.45F, inst.radiusOrScale * 0.42F)
                    * (0.84F + 0.26F * Mth.sin(expandProg * (float) Math.PI));
            renderHemisphereShell(matrix, vc, ground, curRadius * 1.02F, 6, 26,
                    0.878F, 0.969F, 0.980F, 0.46F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, steamH, 0.0D), right, forward,
                    Math.max(0.20F, curRadius * 0.80F), curRadius * 1.10F, 28,
                    0.188F, 0.663F, 0.690F, 0.84F * fade, 0.20F * fade);
            for (int j = 0; j < 16; j++) {
                float jAng = (float) (Math.PI * 2.0D * j / 16.0D) + inst.seed * 0.15F;
                Vec3 jBase = ground.add(Mth.cos(jAng) * curRadius * 0.90D, 0.08D, Mth.sin(jAng) * curRadius * 0.90D);
                Vec3 jTip = jBase.add(Mth.cos(jAng) * 0.56D, 0.72D + (j % 2) * 0.30D, Mth.sin(jAng) * 0.56D);
                renderCrossSegment(matrix, vc, jBase, jTip, 0.066F * fade,
                        0.878F, 0.969F, 0.980F, 0.94F * fade);
            }
        } else if (isAntimatterNullRMateria(inst.essence)) {
            // Section 6.31 White Hole Antimatter Nova (#FFFFFF / #41304E): 6m+ white-hole dome & cosmic violet event-horizon shockwave!
            float novaH = Math.max(1.55F, inst.radiusOrScale * 0.44F)
                    * (0.85F + 0.28F * Mth.sin(expandProg * (float) Math.PI));
            renderHemisphereShell(matrix, vc, ground, curRadius * 1.02F, 6, 28,
                    1.000F, 1.000F, 1.000F, 0.52F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, novaH, 0.0D), right, forward,
                    Math.max(0.22F, curRadius * 0.82F), curRadius * 1.10F, 28,
                    0.255F, 0.188F, 0.306F, 0.88F * fade, 0.22F * fade);
            for (int rIdx = 0; rIdx < 16; rIdx++) {
                float rAng = (float) (Math.PI * 2.0D * rIdx / 16.0D) + inst.seed * 0.13F;
                Vec3 rBase = ground.add(Mth.cos(rAng) * curRadius * 0.88D, 0.08D, Mth.sin(rAng) * curRadius * 0.88D);
                Vec3 rTip = rBase.add(Mth.cos(rAng) * 0.64D, 0.78D + (rIdx % 2) * 0.34D, Mth.sin(rAng) * 0.64D);
                boolean white = (rIdx % 2 == 0);
                renderCrossSegment(matrix, vc, rBase, rTip, 0.068F * fade,
                        white ? 1.000F : 0.255F,
                        white ? 1.000F : 0.188F,
                        white ? 1.000F : 0.306F,
                        0.96F * fade);
            }
        } else if (isAbyssalNullUMateria(inst.essence)) {
            // Section 6.32 Abyssal Gravity Shockwave (#110B17 / #41304E): silent 6m+ dark gravity dome & cosmic violet shock-band!
            float gravH = Math.max(1.50F, inst.radiusOrScale * 0.42F)
                    * (0.84F + 0.28F * Mth.sin(expandProg * (float) Math.PI));
            renderHemisphereShell(matrix, vc, ground, curRadius * 1.02F, 6, 28,
                    0.067F, 0.043F, 0.090F, 0.68F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, gravH, 0.0D), right, forward,
                    Math.max(0.22F, curRadius * 0.82F), curRadius * 1.10F, 28,
                    0.255F, 0.188F, 0.306F, 0.90F * fade, 0.24F * fade);
            for (int rIdx = 0; rIdx < 16; rIdx++) {
                float rAng = (float) (Math.PI * 2.0D * rIdx / 16.0D) + inst.seed * 0.15F;
                Vec3 rBase = ground.add(Mth.cos(rAng) * curRadius * 0.88D, 0.08D, Mth.sin(rAng) * curRadius * 0.88D);
                Vec3 rTip = rBase.add(Mth.cos(rAng) * 0.60D, 0.72D + (rIdx % 2) * 0.32D, Mth.sin(rAng) * 0.60D);
                boolean dark = (rIdx % 2 == 0);
                renderCrossSegment(matrix, vc, rBase, rTip, 0.068F * fade,
                        dark ? 0.067F : 0.255F,
                        dark ? 0.043F : 0.188F,
                        dark ? 0.090F : 0.306F,
                        0.96F * fade);
            }
        } else if (isDaybreakDawnMateria(inst.essence)) {
            // Section 6.33 Sunrise Horizon Nova (#FFEA78 / #A88654): golden sunrise shockwave & howling gale wind band!
            float novaH = Math.max(1.55F, inst.radiusOrScale * 0.44F)
                    * (0.85F + 0.28F * Mth.sin(expandProg * (float) Math.PI));
            renderHemisphereShell(matrix, vc, ground, curRadius * 1.02F, 6, 28,
                    1.000F, 0.918F, 0.471F, 0.54F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, novaH, 0.0D), right, forward,
                    Math.max(0.22F, curRadius * 0.82F), curRadius * 1.12F, 28,
                    0.659F, 0.525F, 0.329F, 0.88F * fade, 0.22F * fade);
            for (int rIdx = 0; rIdx < 16; rIdx++) {
                float rAng = (float) (Math.PI * 2.0D * rIdx / 16.0D) + inst.seed * 0.14F;
                Vec3 rBase = ground.add(Mth.cos(rAng) * curRadius * 0.88D, 0.08D, Mth.sin(rAng) * curRadius * 0.88D);
                Vec3 rTip = rBase.add(Mth.cos(rAng) * 0.66D, 0.80D + (rIdx % 2) * 0.34D, Mth.sin(rAng) * 0.66D);
                boolean sunrise = (rIdx % 2 == 0);
                renderCrossSegment(matrix, vc, rBase, rTip, 0.068F * fade,
                        sunrise ? 1.000F : 0.659F,
                        sunrise ? 0.918F : 0.525F,
                        sunrise ? 0.471F : 0.329F,
                        0.96F * fade);
            }
        } else if (isHadopelagicAbyssMateria(inst.essence)) {
            // Section 6.34 Benthic Inversion Shockwave (#211A21 / #225C7C): silent eruption of crushing abyssal water & blinding black ink!
            float inkH = Math.max(1.50F, inst.radiusOrScale * 0.42F)
                    * (0.86F + 0.26F * Mth.sin(expandProg * (float) Math.PI));
            renderHemisphereShell(matrix, vc, ground, curRadius * 1.00F, 6, 28,
                    0.129F, 0.102F, 0.129F, 0.64F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, inkH, 0.0D), right, forward,
                    Math.max(0.22F, curRadius * 0.82F), curRadius * 1.12F, 28,
                    0.133F, 0.361F, 0.486F, 0.90F * fade, 0.24F * fade);
            for (int rIdx = 0; rIdx < 16; rIdx++) {
                float rAng = (float) (Math.PI * 2.0D * rIdx / 16.0D) + inst.seed * 0.14F;
                Vec3 rBase = ground.add(Mth.cos(rAng) * curRadius * 0.88D, 0.08D, Mth.sin(rAng) * curRadius * 0.88D);
                Vec3 rTip = rBase.add(Mth.cos(rAng) * 0.62D, 0.72D + (rIdx % 2) * 0.30D, Mth.sin(rAng) * 0.62D);
                boolean ink = (rIdx % 2 == 0);
                renderCrossSegment(matrix, vc, rBase, rTip, 0.070F * fade,
                        ink ? 0.129F : 0.133F,
                        ink ? 0.102F : 0.361F,
                        ink ? 0.129F : 0.486F,
                        0.97F * fade);
            }
        } else if (isGildedAegisMateria(inst.essence)) {
            // Section 6.35 Gilded Bulwark Rampart Nova (#FFEA78 / #773214): holy shockwave & rising golden-granite rampart spikes!
            float rampartH = Math.max(1.60F, inst.radiusOrScale * 0.44F)
                    * (0.88F + 0.25F * Mth.sin(expandProg * (float) Math.PI));
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, rampartH, 0.0D), right, forward,
                    Math.max(0.24F, curRadius * 0.84F), curRadius * 1.08F, 12,
                    0.467F, 0.196F, 0.078F, 0.92F * fade, 0.30F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.08D, 0.0D), right, forward,
                    curRadius * 0.86F, curRadius * 1.06F, 16, 1.000F, 0.918F, 0.471F, 0.96F * fade);
            for (int rIdx = 0; rIdx < 12; rIdx++) {
                float rAng = (float) (Math.PI * 2.0D * rIdx / 12.0D) + inst.seed * 0.12F;
                Vec3 rBase = ground.add(Mth.cos(rAng) * curRadius * 0.90D, 0.04D, Mth.sin(rAng) * curRadius * 0.90D);
                Vec3 rTip = rBase.add(Mth.cos(rAng) * 0.35D, 1.15D + (rIdx % 2) * 0.35D, Mth.sin(rAng) * 0.35D);
                boolean gold = (rIdx % 2 == 0);
                renderCrossSegment(matrix, vc, rBase, rTip, 0.085F * fade,
                        gold ? 1.000F : 0.467F,
                        gold ? 0.918F : 0.196F,
                        gold ? 0.471F : 0.078F,
                        0.98F * fade);
            }
        } else if (isBorealisAuroraMateria(inst.essence)) {
            // Section 6.36 Borealis Prismatic Curtain Nova (#FFEA78 / #98DCF2): 360° shimmering aurora curtain & sub-zero hoarfrost spires!
            float curtainH = Math.max(1.75F, inst.radiusOrScale * 0.48F)
                    * (0.88F + 0.26F * Mth.sin(expandProg * (float) Math.PI));
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, curtainH, 0.0D), right, forward,
                    Math.max(0.24F, curRadius * 0.84F), curRadius * 1.08F, 28,
                    1.000F, 0.918F, 0.471F, 0.86F * fade, 0.24F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.07D, 0.0D), right, forward,
                    curRadius * 0.86F, curRadius * 1.05F, 28, 0.596F, 0.863F, 0.949F, 0.96F * fade);
            for (int rIdx = 0; rIdx < 16; rIdx++) {
                float rAng = (float) (Math.PI * 2.0D * rIdx / 16.0D) + inst.seed * 0.13F;
                Vec3 rBase = ground.add(Mth.cos(rAng) * curRadius * 0.90D, 0.06D, Mth.sin(rAng) * curRadius * 0.90D);
                Vec3 rTip = rBase.add(Mth.cos(rAng) * 0.42D, 1.05D + (rIdx % 2) * 0.40D, Mth.sin(rAng) * 0.42D);
                boolean gold = (rIdx % 2 == 0);
                renderCrossSegment(matrix, vc, rBase, rTip, 0.070F * fade,
                        gold ? 1.000F : 0.596F,
                        gold ? 0.918F : 0.863F,
                        gold ? 0.471F : 0.949F,
                        0.97F * fade);
            }
        } else if (isPhantasmMirageMateria(inst.essence)) {
            // Section 6.37 Shimmering Fata Morgana Shockwave (#FFEA78 / #C2B28F): 360° blinding heat-distortion eruption & swirling desert sand!
            float mirageH = Math.max(1.65F, inst.radiusOrScale * 0.44F)
                    * (0.86F + 0.26F * Mth.sin(expandProg * (float) Math.PI));
            renderHemisphereShell(matrix, vc, ground, curRadius * 1.02F, 6, 28,
                    1.000F, 0.918F, 0.471F, 0.48F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, mirageH, 0.0D), right, forward,
                    Math.max(0.24F, curRadius * 0.82F), curRadius * 1.10F, 28,
                    0.761F, 0.698F, 0.561F, 0.86F * fade, 0.22F * fade);
            for (int rIdx = 0; rIdx < 16; rIdx++) {
                float rAng = (float) (Math.PI * 2.0D * rIdx / 16.0D) + inst.seed * 0.14F;
                Vec3 rBase = ground.add(Mth.cos(rAng) * curRadius * 0.88D, 0.06D, Mth.sin(rAng) * curRadius * 0.88D);
                Vec3 rTip = rBase.add(Mth.cos(rAng + 0.25F) * 0.62D, 0.82D + (rIdx % 2) * 0.32D, Mth.sin(rAng + 0.25F) * 0.62D);
                boolean gold = (rIdx % 2 == 0);
                renderCrossSegment(matrix, vc, rBase, rTip, 0.068F * fade,
                        gold ? 1.000F : 0.761F,
                        gold ? 0.918F : 0.698F,
                        gold ? 0.471F : 0.561F,
                        0.95F * fade);
            }
        } else if (isIncorporealWraithMateria(inst.essence)) {
            // Section 6.38 Incorporeal Ghost-Storm Shockwave (#41304E / #4D6453): icy spectral shockwave of howling void spirits!
            float wraithH = Math.max(1.75F, inst.radiusOrScale * 0.46F)
                    * (0.86F + 0.26F * Mth.sin(expandProg * (float) Math.PI));
            renderHemisphereShell(matrix, vc, ground, curRadius * 1.02F, 6, 28,
                    0.255F, 0.188F, 0.306F, 0.52F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, wraithH, 0.0D), right, forward,
                    Math.max(0.24F, curRadius * 0.80F), curRadius * 1.12F, 28,
                    0.302F, 0.392F, 0.325F, 0.88F * fade, 0.22F * fade);
            for (int wIdx = 0; wIdx < 16; wIdx++) {
                float wAng = (float) (Math.PI * 2.0D * wIdx / 16.0D) + inst.seed * 0.16F;
                Vec3 wBase = ground.add(Mth.cos(wAng) * curRadius * 0.88D, 0.08D, Mth.sin(wAng) * curRadius * 0.88D);
                Vec3 wTip = wBase.add(Mth.cos(wAng - 0.30F) * 0.70D, 0.95D + (wIdx % 2) * 0.35D, Mth.sin(wAng - 0.30F) * 0.70D);
                boolean moss = (wIdx % 2 == 0);
                renderCrossSegment(matrix, vc, wBase, wTip, 0.072F * fade,
                        moss ? 0.302F : 0.255F,
                        moss ? 0.392F : 0.188F,
                        moss ? 0.325F : 0.306F,
                        0.95F * fade);
            }
        } else if (isSepulchralBarrowMateria(inst.essence)) {
            // Section 6.39 Catacomb Miasma Eruption (#773214 / #4D6453 + #E6DFCF bone): toxic burial gas cloud & jagged petrified bone shards!
            float barrowH = Math.max(1.65F, inst.radiusOrScale * 0.44F)
                    * (0.86F + 0.26F * Mth.sin(expandProg * (float) Math.PI));
            renderHemisphereShell(matrix, vc, ground, curRadius * 1.02F, 6, 26,
                    0.302F, 0.392F, 0.325F, 0.54F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, barrowH, 0.0D), right, forward,
                    Math.max(0.24F, curRadius * 0.80F), curRadius * 1.10F, 16,
                    0.467F, 0.196F, 0.078F, 0.88F * fade, 0.24F * fade);
            for (int bIdx = 0; bIdx < 16; bIdx++) {
                float bAng = (float) (Math.PI * 2.0D * bIdx / 16.0D) + inst.seed * 0.15F;
                Vec3 bBase = ground.add(Mth.cos(bAng) * curRadius * 0.86D, 0.06D, Mth.sin(bAng) * curRadius * 0.86D);
                Vec3 bTip = bBase.add(Mth.cos(bAng) * 0.68D, 0.88D + (bIdx % 2) * 0.34D, Mth.sin(bAng) * 0.68D);
                boolean bone = (bIdx % 2 == 0);
                renderCrossSegment(matrix, vc, bBase, bTip, 0.076F * fade,
                        bone ? 0.902F : 0.467F,
                        bone ? 0.875F : 0.196F,
                        bone ? 0.812F : 0.078F,
                        0.96F * fade);
            }
        } else if (isCrematingPyreMateria(inst.essence)) {
            // Section 6.40 Raging Wildfire Pyre Nova (#773214 / #FF4500): roaring wildfire shockwave & blazing cedar/coal shards!
            float pyreH = Math.max(1.75F, inst.radiusOrScale * 0.46F)
                    * (0.88F + 0.28F * Mth.sin(expandProg * (float) Math.PI));
            renderHemisphereShell(matrix, vc, ground, curRadius * 1.02F, 6, 26,
                    1.000F, 0.271F, 0.000F, 0.56F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, pyreH, 0.0D), right, forward,
                    Math.max(0.24F, curRadius * 0.80F), curRadius * 1.12F, 18,
                    0.467F, 0.196F, 0.078F, 0.90F * fade, 0.26F * fade);
            for (int pIdx = 0; pIdx < 16; pIdx++) {
                float pAng = (float) (Math.PI * 2.0D * pIdx / 16.0D) + inst.seed * 0.17F;
                Vec3 pBase = ground.add(Mth.cos(pAng) * curRadius * 0.85D, 0.06D, Mth.sin(pAng) * curRadius * 0.85D);
                Vec3 pTip = pBase.add(Mth.cos(pAng) * 0.72D, 0.92D + (pIdx % 2) * 0.36D, Mth.sin(pAng) * 0.72D);
                boolean flame = (pIdx % 2 == 0);
                renderCrossSegment(matrix, vc, pBase, pTip, 0.078F * fade,
                        flame ? 1.000F : 0.467F,
                        flame ? 0.271F : 0.196F,
                        flame ? 0.000F : 0.078F,
                        0.97F * fade);
            }
        } else if (isTwilightPenumbraMateria(inst.essence)) {
            // Section 6.41 Twilight Corona Nova (#FFEA78 / #281E3C): blazing solar corona flares shrouded in deep indigo twilight!
            float penH = Math.max(1.70F, inst.radiusOrScale * 0.45F)
                    * (0.88F + 0.26F * Mth.sin(expandProg * (float) Math.PI));
            renderHemisphereShell(matrix, vc, ground, curRadius * 1.02F, 6, 28,
                    0.157F, 0.118F, 0.235F, 0.60F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, penH, 0.0D), right, forward,
                    Math.max(0.24F, curRadius * 0.82F), curRadius * 1.12F, 20,
                    1.000F, 0.918F, 0.471F, 0.92F * fade, 0.24F * fade);
            for (int rIdx = 0; rIdx < 16; rIdx++) {
                float rAng = (float) (Math.PI * 2.0D * rIdx / 16.0D) + inst.seed * 0.13F;
                Vec3 rBase = ground.add(Mth.cos(rAng) * curRadius * 0.84D, 0.08D, Mth.sin(rAng) * curRadius * 0.84D);
                Vec3 rTip = rBase.add(Mth.cos(rAng) * 0.78D, 0.90D + (rIdx % 2) * 0.34D, Mth.sin(rAng) * 0.78D);
                boolean corona = (rIdx % 2 == 0);
                renderCrossSegment(matrix, vc, rBase, rTip, 0.074F * fade,
                        corona ? 1.000F : 0.157F,
                        corona ? 0.918F : 0.118F,
                        corona ? 0.471F : 0.235F,
                        0.97F * fade);
            }
        } else if (isHoarfrostRimeMateria(inst.essence)) {
            // Section 6.42 Flash-Freeze Rime Frost-Nova (#6395B1 / #DCF0FF): radial blizzard shockwave & hoarfrost spires!
            float rimeH = Math.max(1.70F, inst.radiusOrScale * 0.45F)
                    * (0.88F + 0.26F * Mth.sin(expandProg * (float) Math.PI));
            renderHemisphereShell(matrix, vc, ground, curRadius * 1.02F, 6, 26,
                    0.388F, 0.584F, 0.694F, 0.54F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, rimeH, 0.0D), right, forward,
                    Math.max(0.24F, curRadius * 0.82F), curRadius * 1.12F, 18,
                    0.863F, 0.941F, 1.000F, 0.90F * fade, 0.24F * fade);
            for (int rIdx = 0; rIdx < 16; rIdx++) {
                float rAng = (float) (Math.PI * 2.0D * rIdx / 16.0D) + inst.seed * 0.15F;
                Vec3 rBase = ground.add(Mth.cos(rAng) * curRadius * 0.85D, 0.06D, Mth.sin(rAng) * curRadius * 0.85D);
                Vec3 rTip = rBase.add(Mth.cos(rAng) * 0.72D, 0.92D + (rIdx % 2) * 0.36D, Mth.sin(rAng) * 0.72D);
                boolean white = (rIdx % 2 == 0);
                renderCrossSegment(matrix, vc, rBase, rTip, 0.074F * fade,
                        white ? 0.863F : 0.388F,
                        white ? 0.941F : 0.584F,
                        white ? 1.000F : 0.694F,
                        0.97F * fade);
            }
        } else if (isVernalSpringMateria(inst.essence)) {
            // Section 6.43 Vernal Rebirth Blossom-Nova (#FF6B9D / #64C8FF): crystalline springwater & blooming rose petals!
            float springH = Math.max(1.65F, inst.radiusOrScale * 0.44F)
                    * (0.88F + 0.26F * Mth.sin(expandProg * (float) Math.PI));
            renderHemisphereShell(matrix, vc, ground, curRadius * 1.02F, 6, 26,
                    0.392F, 0.784F, 1.000F, 0.52F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, springH, 0.0D), right, forward,
                    Math.max(0.24F, curRadius * 0.82F), curRadius * 1.12F, 22,
                    1.000F, 0.420F, 0.616F, 0.88F * fade, 0.24F * fade);
            for (int pIdx = 0; pIdx < 16; pIdx++) {
                float pAng = (float) (Math.PI * 2.0D * pIdx / 16.0D) + inst.seed * 0.15F;
                Vec3 pBase = ground.add(Mth.cos(pAng) * curRadius * 0.86D, 0.08D, Mth.sin(pAng) * curRadius * 0.86D);
                Vec3 pTip = pBase.add(Mth.cos(pAng) * 0.72D, 0.86D + (pIdx % 2) * 0.34D, Mth.sin(pAng) * 0.72D);
                boolean blossom = (pIdx % 2 == 0);
                renderCrossSegment(matrix, vc, pBase, pTip, 0.072F * fade,
                        blossom ? 1.000F : 0.392F,
                        blossom ? 0.420F : 0.784F,
                        blossom ? 0.616F : 1.000F,
                        0.96F * fade);
            }
        } else if (isGalvanicStaticMateria(inst.essence)) {
            // Section 6.44 Ground-Discharge EMP Shockwave (#FFE600 / #878DA2): electric ground ripple & polarized dielectric dome!
            float empH = Math.max(1.55F, inst.radiusOrScale * 0.42F)
                    * (0.88F + 0.24F * Mth.sin(expandProg * (float) Math.PI));
            renderHemisphereShell(matrix, vc, ground, curRadius * 1.02F, 6, 20,
                    0.529F, 0.553F, 0.635F, 0.48F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, empH, 0.0D), right, forward,
                    Math.max(0.24F, curRadius * 0.84F), curRadius * 1.10F, 16,
                    1.000F, 0.902F, 0.000F, 0.92F * fade, 0.20F * fade);
            for (int aIdx = 0; aIdx < 16; aIdx++) {
                float aAng = (float) (Math.PI * 2.0D * aIdx / 16.0D) + inst.seed * 0.21F;
                Vec3 aBase = ground.add(Mth.cos(aAng) * curRadius * 0.80D, 0.06D, Mth.sin(aAng) * curRadius * 0.80D);
                Vec3 aTip = aBase.add(Mth.cos(aAng) * 0.82D, 0.64D + (aIdx % 2) * 0.42D, Mth.sin(aAng) * 0.82D);
                boolean yellow = (aIdx % 2 == 0);
                renderCrossSegment(matrix, vc, aBase, aTip, 0.075F * fade,
                        yellow ? 1.000F : 0.529F,
                        yellow ? 0.902F : 0.553F,
                        yellow ? 0.000F : 0.635F,
                        0.98F * fade);
            }
        } else if (isPrimevalSylvanMateria(inst.essence)) {
            // Section 6.45 Primeval Sylvan Canopy Nova (#1A5923 / #773214 / #FFEA78): golden sun-rays, ironwood branches & emerald leaves!
            float sylH = Math.max(1.65F, inst.radiusOrScale * 0.44F)
                    * (0.88F + 0.25F * Mth.sin(expandProg * (float) Math.PI));
            renderHemisphereShell(matrix, vc, ground, curRadius * 1.02F, 6, 24,
                    0.102F, 0.349F, 0.137F, 0.52F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, sylH, 0.0D), right, forward,
                    Math.max(0.24F, curRadius * 0.82F), curRadius * 1.12F, 20,
                    1.000F, 0.918F, 0.471F, 0.90F * fade, 0.22F * fade);
            for (int bIdx = 0; bIdx < 16; bIdx++) {
                float bAng = (float) (Math.PI * 2.0D * bIdx / 16.0D) + inst.seed * 0.17F;
                Vec3 bBase = ground.add(Mth.cos(bAng) * curRadius * 0.84D, 0.08D, Mth.sin(bAng) * curRadius * 0.84D);
                Vec3 bTip = bBase.add(Mth.cos(bAng) * 0.78D, 0.88D + (bIdx % 2) * 0.36D, Mth.sin(bAng) * 0.78D);
                boolean gold = (bIdx % 2 == 0);
                renderCrossSegment(matrix, vc, bBase, bTip, 0.076F * fade,
                        gold ? 1.000F : 0.467F,
                        gold ? 0.918F : 0.196F,
                        gold ? 0.471F : 0.078F,
                        0.97F * fade);
            }
        } else if (isPestilentMiasmaMateria(inst.essence)) {
            // Section 6.46 Putrescent Swamp-Blight Nova (#1A5923 / #4D6453 / #225C7C): sickly green & necrotic teal swamp vapor shockwave!
            float miaH = Math.max(1.50F, inst.radiusOrScale * 0.42F)
                    * (0.86F + 0.24F * Mth.sin(expandProg * (float) Math.PI));
            renderHemisphereShell(matrix, vc, ground, curRadius * 1.02F, 6, 24,
                    0.302F, 0.392F, 0.325F, 0.54F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, miaH, 0.0D), right, forward,
                    Math.max(0.24F, curRadius * 0.82F), curRadius * 1.12F, 20,
                    0.133F, 0.361F, 0.486F, 0.88F * fade, 0.24F * fade);
            for (int gIdx = 0; gIdx < 16; gIdx++) {
                float gAng = (float) (Math.PI * 2.0D * gIdx / 16.0D) + inst.seed * 0.19F;
                Vec3 gBase = ground.add(Mth.cos(gAng) * curRadius * 0.84D, 0.08D, Mth.sin(gAng) * curRadius * 0.84D);
                Vec3 gTip = gBase.add(Mth.cos(gAng) * 0.72D, 0.84D + (gIdx % 2) * 0.34D, Mth.sin(gAng) * 0.72D);
                boolean green = (gIdx % 2 == 0);
                renderCrossSegment(matrix, vc, gBase, gTip, 0.078F * fade,
                        green ? 0.102F : 0.133F,
                        green ? 0.349F : 0.361F,
                        green ? 0.137F : 0.486F,
                        0.96F * fade);
            }
        } else if (isPrimordialGenesisMateria(inst.essence)) {
            // Section 6.47 Dawn-Tide Rebirth Nova (#FF6B9D / #FFEA78 / #225C7C): swirling golden-pink solar liquid & sacred water rings!
            float genH = Math.max(1.65F, inst.radiusOrScale * 0.44F)
                    * (0.88F + 0.24F * Mth.sin(expandProg * (float) Math.PI));
            renderHemisphereShell(matrix, vc, ground, curRadius * 1.02F, 6, 24,
                    0.133F, 0.361F, 0.486F, 0.52F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, genH, 0.0D), right, forward,
                    Math.max(0.24F, curRadius * 0.82F), curRadius * 1.12F, 22,
                    1.000F, 0.918F, 0.471F, 0.90F * fade, 0.24F * fade);
            for (int pIdx = 0; pIdx < 16; pIdx++) {
                float pAng = (float) (Math.PI * 2.0D * pIdx / 16.0D) + inst.seed * 0.17F;
                Vec3 pBase = ground.add(Mth.cos(pAng) * curRadius * 0.84D, 0.08D, Mth.sin(pAng) * curRadius * 0.84D);
                Vec3 pTip = pBase.add(Mth.cos(pAng) * 0.78D, 0.88D + (pIdx % 2) * 0.36D, Mth.sin(pAng) * 0.78D);
                boolean pink = (pIdx % 2 == 0);
                renderCrossSegment(matrix, vc, pBase, pTip, 0.076F * fade,
                        1.000F,
                        pink ? 0.420F : 0.918F,
                        pink ? 0.616F : 0.471F,
                        0.98F * fade);
            }
        } else if (isStygianOblivionMateria(inst.essence)) {
            // Section 6.48 Stygian Extinction Nova (#B83C08 / #211A21 / #4D6453): explosive black hellfire dome, obsidian ash wall & necrotic bone lances!
            float oblH = Math.max(1.75F, inst.radiusOrScale * 0.46F)
                    * (0.88F + 0.26F * Mth.sin(expandProg * (float) Math.PI));
            renderHemisphereShell(matrix, vc, ground, curRadius * 1.02F, 6, 22,
                    0.129F, 0.102F, 0.129F, 0.74F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, oblH, 0.0D), right, forward,
                    Math.max(0.26F, curRadius * 0.84F), curRadius * 1.14F, 20,
                    0.722F, 0.235F, 0.031F, 0.94F * fade, 0.26F * fade);
            for (int sIdx = 0; sIdx < 16; sIdx++) {
                float sAng = (float) (Math.PI * 2.0D * sIdx / 16.0D) + inst.seed * 0.19F;
                Vec3 sBase = ground.add(Mth.cos(sAng) * curRadius * 0.84D, 0.08D, Mth.sin(sAng) * curRadius * 0.84D);
                Vec3 sTip = sBase.add(Mth.cos(sAng) * 0.84D, 0.94D + (sIdx % 2) * 0.38D, Mth.sin(sAng) * 0.84D);
                boolean rust = (sIdx % 2 == 0);
                renderCrossSegment(matrix, vc, sBase, sTip, 0.080F * fade,
                        rust ? 0.722F : 0.302F,
                        rust ? 0.235F : 0.392F,
                        rust ? 0.031F : 0.325F,
                        0.98F * fade);
            }
        } else if (isQuintessentialAetherMateria(inst.essence)) {
            // Section 6.50 Quintessential Firmament Resonance Nova (#A88654 / #773214 / #225C7C / #B83C08): luminous halo rings & quad-elemental bands!
            float aethH = Math.max(1.90F, inst.radiusOrScale * 0.46F)
                    * (0.90F + 0.26F * Mth.sin(expandProg * (float) Math.PI));
            renderHemisphereShell(matrix, vc, ground, curRadius * 1.03F, 6, 26,
                    0.133F, 0.361F, 0.486F, 0.54F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, aethH, 0.0D), right, forward,
                    Math.max(0.28F, curRadius * 0.86F), curRadius * 1.16F, 24,
                    0.659F, 0.525F, 0.329F, 0.94F * fade, 0.26F * fade);
            float[][] quadRgb = {
                    {0.659F, 0.525F, 0.329F},
                    {0.467F, 0.196F, 0.078F},
                    {0.133F, 0.361F, 0.486F},
                    {0.722F, 0.235F, 0.031F}
            };
            for (int qIdx = 0; qIdx < 16; qIdx++) {
                float qAng = (float) (Math.PI * 2.0D * qIdx / 16.0D) + inst.seed * 0.17F;
                float[] c = quadRgb[qIdx & 3];
                Vec3 qBase = ground.add(Mth.cos(qAng) * curRadius * 0.84D, 0.08D, Mth.sin(qAng) * curRadius * 0.84D);
                Vec3 qTip = qBase.add(Mth.cos(qAng) * 0.88D, 1.02D + (qIdx % 2) * 0.40D, Mth.sin(qAng) * 0.88D);
                renderCrossSegment(matrix, vc, qBase, qTip, 0.082F * fade, c[0], c[1], c[2], 0.98F * fade);
            }
        } else if (isAbyssalEntropicMateria(inst.essence)) {
            // Section 6.51 Entropic Singularity Implosion Nova (#41304E / #211A21 / #4D6453 / #8A0303): light-devouring darkness & crimson blood rings!
            float entH = Math.max(1.90F, inst.radiusOrScale * 0.46F)
                    * (0.90F + 0.26F * Mth.sin(expandProg * (float) Math.PI));
            renderHemisphereShell(matrix, vc, ground, curRadius * 1.03F, 6, 24,
                    0.129F, 0.102F, 0.129F, 0.80F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, entH, 0.0D), right, forward,
                    Math.max(0.28F, curRadius * 0.86F), curRadius * 1.16F, 22,
                    0.541F, 0.012F, 0.012F, 0.95F * fade, 0.28F * fade);
            float[][] entRgb = {
                    {0.255F, 0.188F, 0.306F},
                    {0.129F, 0.102F, 0.129F},
                    {0.302F, 0.392F, 0.325F},
                    {0.541F, 0.012F, 0.012F}
            };
            for (int eIdx = 0; eIdx < 16; eIdx++) {
                float eAng = (float) (Math.PI * 2.0D * eIdx / 16.0D) + inst.seed * 0.19F;
                float[] c = entRgb[eIdx & 3];
                Vec3 eBase = ground.add(Mth.cos(eAng) * curRadius * 0.84D, 0.08D, Mth.sin(eAng) * curRadius * 0.84D);
                Vec3 eTip = eBase.add(Mth.cos(eAng) * 0.88D, 1.02D + (eIdx % 2) * 0.40D, Mth.sin(eAng) * eBase.length() * 0.0D + Mth.sin(eAng) * 0.88D);
                renderCrossSegment(matrix, vc, eBase, eTip, 0.082F * fade, c[0], c[1], c[2], 0.98F * fade);
            }
        } else if (isHolyCelestialMateria(inst.essence)) {
            // Section 6.52 Holy Supernova Dawn-Shockwave (#FFEA78 / #FF6B9D / #1A5923 / #A88654): stellar dawnfire, rose-prana & emerald life-waves!
            float celH = Math.max(1.90F, inst.radiusOrScale * 0.46F)
                    * (0.90F + 0.26F * Mth.sin(expandProg * (float) Math.PI));
            renderHemisphereShell(matrix, vc, ground, curRadius * 1.03F, 6, 24,
                    1.000F, 0.918F, 0.471F, 0.62F * fade);
            renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, celH, 0.0D), right, forward,
                    Math.max(0.28F, curRadius * 0.86F), curRadius * 1.16F, 24,
                    1.000F, 0.420F, 0.616F, 0.95F * fade, 0.28F * fade);
            float[][] celRgb = {
                    {1.000F, 0.918F, 0.471F},
                    {1.000F, 0.420F, 0.616F},
                    {0.102F, 0.349F, 0.137F},
                    {0.659F, 0.525F, 0.329F}
            };
            for (int cIdx = 0; cIdx < 16; cIdx++) {
                float cAng = (float) (Math.PI * 2.0D * cIdx / 16.0D) + inst.seed * 0.19F;
                float[] c = celRgb[cIdx & 3];
                Vec3 cBase = ground.add(Mth.cos(cAng) * curRadius * 0.84D, 0.08D, Mth.sin(cAng) * curRadius * 0.84D);
                Vec3 cTip = cBase.add(Mth.cos(cAng) * 0.90D, 1.05D + (cIdx % 2) * 0.42D, Mth.sin(cAng) * 0.90D);
                renderCrossSegment(matrix, vc, cBase, cTip, 0.082F * fade, c[0], c[1], c[2], 0.98F * fade);
            }
        }
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
        float surgeProg = Mth.clamp(progress / 0.36F, 0.0F, 1.0F);
        float eased = 1.0F - (1.0F - surgeProg) * (1.0F - surgeProg) * (1.0F - surgeProg);
        float fade = computeEnvelope(progress, 0.04F, 0.52F);
        float maxR = inst.radiusOrScale * (0.95F + inst.tier * 0.08F);
        float curR = maxR * (0.15F + 0.76F * eased + 0.14F * progress);

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

        if (isAerodynamicMateria(inst.essence)) {
            // Sweeping pinwheel wind-blade crescents & supersonic impact funnel!
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(normal.scale(0.04D)),
                    inst.start.add(normal.scale(curR * 0.55D)),
                    right, up, curR * 0.35F, curR * 1.12F, 20,
                    pr, pg, pb, 0.45F * fade, 0.05F * fade);
            for (int i = 0; i < 6; i++) {
                float ang = progress * 2.4F + (float) (Math.PI * 2.0D * i / 6.0D) + inst.seed;
                renderSweptWindCrescent(matrix, vc, inst.start.add(normal.scale(0.10D)),
                        right, up, curR * 1.06F, ang, 0.92F, 0.16F * maxR, ar, ag, ab, 0.88F * fade);
            }
            return;
        }

        if (isHydroMateria(inst.essence)) {
            // Section 6.3 3D Liquid Crown-Splash (Hydro-Cannon Slug 3m splash, Fluid Dissolution reform, Crashing Breaker & Aqueous Decoy)!
            float crownProfile = Mth.sin(progress * (float) Math.PI);
            float crownH = maxR * (0.16F + 0.44F * crownProfile);
            Vec3 crownBase = inst.start.add(normal.scale(0.03D));
            Vec3 crownRim = inst.start.add(normal.scale(crownH));

            // Rising azure-cerulean liquid splash crown wall
            renderOrientedFrustumBand(matrix, vc, crownBase, crownRim, right, up,
                    curR * 0.34F, curR * 0.72F, 24,
                    0.133F, 0.361F, 0.486F, 0.62F * fade, 0.38F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(normal.scale(crownH * 0.68D)), crownRim, right, up,
                    curR * 0.60F, curR * 0.76F, 24,
                    0.88F, 0.98F, 1.00F, 0.78F * fade, 0.90F * fade);

            // Inner concentric surface ripple rings
            renderOrientedRing(matrix, vc, crownBase, right, up,
                    curR * 0.42F, curR * 0.50F, 22, ar, ag, ab, 0.72F * fade);

            // 10 arcing 3D splash droplets leaping off the crown rim!
            for (int d = 0; d < 10; d++) {
                float ang = (float) (Math.PI * 2.0D * d / 10.0D) + inst.seed * 0.3F;
                Vec3 radial = right.scale(Mth.cos(ang)).add(up.scale(Mth.sin(ang)));
                double dropDist = curR * (0.42D + 0.62D * progress);
                double dropLift = maxR * (1.15D * progress - 1.08D * progress * progress) + 0.06D;
                Vec3 dropPos = inst.start.add(radial.scale(dropDist)).add(normal.scale(Math.max(0.02D, dropLift)));
                renderOctahedron(matrix, vc, dropPos, Math.max(0.045F, maxR * 0.045F) * fade,
                        (d % 2 == 0) ? 0.90F : pr, (d % 2 == 0) ? 0.99F : pg, 1.00F, 0.88F * fade);
            }
            return;
        }

        if (isBotanicalMateria(inst.essence) || isOvergrowthRampartMateria(inst.essence)) {
            // Section 6.4 Thorned Briar Seed & Section 6.17 Verdant Briar-Spike Seed Impact: thorny vines erupt upward!
            float erupt = Mth.clamp(progress / 0.25F, 0.0F, 1.0F);
            double snareH = Math.max(1.15D, maxR * 1.35D) * (0.30D + 0.70D * erupt);
            int vineCount = 5;
            for (int v = 0; v < vineCount; v++) {
                float baseAng = (float) (Math.PI * 2.0D * v / vineCount) + inst.seed * 0.4F;
                Vec3 prevV = null;
                for (int s = 0; s <= 10; s++) {
                    float u = s / 10.0F;
                    float coilAng = baseAng + u * 3.4F;
                    float coilR = maxR * (0.58F - 0.20F * u) * (0.65F + 0.35F * Mth.sin(u * (float) Math.PI));
                    Vec3 radial = right.scale(Mth.cos(coilAng) * coilR).add(up.scale(Mth.sin(coilAng) * coilR));
                    Vec3 pt = inst.start.add(radial).add(normal.scale(u * snareH));
                    if (prevV != null) {
                        renderCrossSegment(matrix, vc, prevV, pt, 0.052F * fade * (1.0F - u * 0.30F),
                                (v % 2 == 0) ? 0.102F : 0.36F, (v % 2 == 0) ? 0.349F : 0.23F, (v % 2 == 0) ? 0.137F : 0.12F,
                                0.92F * fade);
                    }
                    if (s > 0 && s < 10 && s % 3 == 0) {
                        Vec3 barbTip = pt.add(radial.normalize().scale(0.22D * maxR)).add(normal.scale(0.08D));
                        renderCrossSegment(matrix, vc, pt, barbTip, 0.026F * fade, 0.71F, 1.00F, 0.47F, 0.90F * fade);
                    }
                    prevV = pt;
                }
            }
            renderOrientedRing(matrix, vc, inst.start.add(normal.scale(snareH * 0.45D)), right, up,
                    maxR * 0.42F, maxR * 0.52F, 18, 0.17F, 0.90F, 0.41F, 0.82F * fade);
            return;
        }

        if (isTectonicMateria(inst.essence)) {
            // Section 6.5 Quake Spire & Tectonic Impalement Spike: colossal 3D sharp stone stalagmite cluster erupting upward!
            float erupt = Mth.clamp(progress / 0.18F, 0.0F, 1.0F);
            float easeOut = 1.0F - (1.0F - erupt) * (1.0F - erupt) * (1.0F - erupt);
            float mainH = Math.max(1.55F, maxR * 1.68F) * (0.20F + 0.80F * easeOut);
            float mainW = Math.max(0.24F, maxR * 0.28F);

            // Central colossal 6-sided granite & terracotta stalagmite spire
            renderFacetedStoneSpire(matrix, vc, inst.start, normal, right, up,
                    mainW, mainH, 6, inst.seed * 0.37F, fade);

            // 5 angled flanking stalagmite stone spikes bursting out around the crater rim!
            for (int s = 0; s < 5; s++) {
                float sAng = (float) (Math.PI * 2.0D * s / 5.0D) + inst.seed * 0.51F;
                Vec3 sRad = right.scale(Mth.cos(sAng)).add(up.scale(Mth.sin(sAng))).normalize();
                Vec3 sTang = normal.cross(sRad).normalize();
                Vec3 tiltAxis = normal.add(sRad.scale(0.42D)).normalize();
                Vec3 sBase = inst.start.add(sRad.scale(mainW * 0.95D));
                float subH = mainH * (s % 2 == 0 ? 0.62F : 0.48F);
                renderFacetedStoneSpire(matrix, vc, sBase, tiltAxis, sRad, sTang,
                        mainW * 0.56F, subH, 5, sAng, fade);
            }

            // Glowing octagonal amber-quartz seismic fault crater ring
            renderOrientedRing(matrix, vc, inst.start.add(normal.scale(0.05D)), right, up,
                    curR * 0.54F, curR * 0.72F, 8, 1.00F, 0.76F, 0.47F, 0.90F * fade);
            return;
        }

        if (isGlacialMateria(inst.essence)) {
            // Section 6.6 Glacial Ice-Shard Shatter: 8 radial 3D crystalline frost shards & 6-pointed snowflake shock-seal!
            float erupt = Mth.clamp(progress / 0.20F, 0.0F, 1.0F);
            float easeOut = 1.0F - (1.0F - erupt) * (1.0F - erupt) * (1.0F - erupt);
            renderSnowflakeCrystal(matrix, vc, inst.start.add(normal.scale(0.06D)), right, up,
                    curR * 0.94F, inst.seed * 0.45F, 0.88F * fade);
            for (int s = 0; s < 8; s++) {
                float sAng = (float) (Math.PI * 2.0D * s / 8.0D) + inst.seed * 0.31F;
                Vec3 sRad = right.scale(Mth.cos(sAng)).add(up.scale(Mth.sin(sAng))).normalize();
                Vec3 sTang = normal.cross(sRad).normalize();
                Vec3 tiltAxis = normal.scale(0.62D).add(sRad.scale(0.78D)).normalize();
                Vec3 sBase = inst.start.add(sRad.scale(curR * 0.22D));
                float shardH = Math.max(0.65F, maxR * (s % 2 == 0 ? 0.92F : 0.68F)) * (0.25F + 0.75F * easeOut);
                renderFacetedIceSpire(matrix, vc, sBase, tiltAxis, sRad, sTang,
                        Math.max(0.09F, maxR * 0.11F), shardH, 5, sAng, fade);
            }
            return;
        } else if (isInfernalMateria(inst.essence) || isMagmaCalderaMateria(inst.essence)) {
            // Section 6.7 Netherfire Comet & Section 6.14 Molten Magma Boulder Impact: erupting molten magma crown & 8 outward-arcing 3D lava splash plumes!
            float erupt = Mth.clamp(progress / 0.28F, 0.0F, 1.0F);
            float easeOut = 1.0F - (1.0F - erupt) * (1.0F - erupt);
            renderOrientedRing(matrix, vc, inst.start.add(normal.scale(0.04D)), right, up,
                    curR * 0.55F, curR * 1.04F, 18, 0.722F, 0.235F, 0.031F, 0.88F * fade);
            renderOrientedRing(matrix, vc, inst.start.add(normal.scale(0.06D)), right, up,
                    curR * 0.72F, curR * 0.92F, 18, 1.00F, 0.820F, 0.361F, 0.94F * fade);
            for (int f = 0; f < 8; f++) {
                float fAng = (float) (Math.PI * 2.0D * f / 8.0D) + inst.seed * 0.19F;
                Vec3 fRad = right.scale(Mth.cos(fAng)).add(up.scale(Mth.sin(fAng))).normalize();
                Vec3 fTang = normal.cross(fRad).normalize();
                Vec3 tiltAxis = normal.scale(0.68D).add(fRad.scale(0.73D)).normalize();
                Vec3 fBase = inst.start.add(fRad.scale(curR * 0.20D));
                float plumeH = Math.max(0.75F, maxR * (f % 2 == 0 ? 0.95F : 0.70F)) * (0.28F + 0.72F * easeOut);
                renderHellfireFlameTongue(matrix, vc, fBase, tiltAxis, fRad, fTang,
                        Math.max(0.14F, maxR * 0.14F), plumeH, fade);
                if (isMagmaCalderaMateria(inst.essence)) {
                    Vec3 crustShard = fBase.add(tiltAxis.scale(plumeH * 0.72D));
                    renderOctahedron(matrix, vc, crustShard, Math.max(0.095F, maxR * 0.085F) * fade,
                            0.165F, 0.090F, 0.071F, 0.88F * fade);
                }
            }
            return;
        } else if (isSolarRadiantMateria(inst.essence)) {
            // Section 6.8 Homing Dawn Star & Luminous Cleanse Flash: 8-point 3D golden sunburst crown & celestial halo!
            float erupt = Mth.clamp(progress / 0.24F, 0.0F, 1.0F);
            float easeOut = 1.0F - (1.0F - erupt) * (1.0F - erupt);
            renderOrientedRing(matrix, vc, inst.start.add(normal.scale(0.05D)), right, up,
                    curR * 0.62F, curR * 1.04F, 24, 1.00F, 0.886F, 0.471F, 0.92F * fade);
            renderOrientedStarPolygon(matrix, vc, inst.start.add(normal.scale(0.07D)), right, up,
                    curR * 1.06F, 8, inst.seed * 0.27F + progress * 0.9F, 1.00F, 0.99F, 0.92F, 0.90F * fade);
            for (int s = 0; s < 8; s++) {
                float sAng = (float) (Math.PI * 2.0D * s / 8.0D) + inst.seed * 0.21F;
                Vec3 sRad = right.scale(Mth.cos(sAng)).add(up.scale(Mth.sin(sAng))).normalize();
                Vec3 sTang = normal.cross(sRad).normalize();
                Vec3 tiltAxis = normal.scale(0.55D).add(sRad.scale(0.83D)).normalize();
                float rayLen = Math.max(0.85F, maxR * (s % 2 == 0 ? 1.12F : 0.82F)) * (0.28F + 0.72F * easeOut);
                renderSolarStarburstRay(matrix, vc, inst.start.add(sRad.scale(curR * 0.16D)),
                        tiltAxis, sRad, sTang, Math.max(0.10F, maxR * 0.11F), rayLen, fade);
            }
            return;
        } else if (isUmbralShadowMateria(inst.essence)) {
            // Section 6.9 Shadow Snare Missile Floor-Tether: pitch-black shadow pool & 6 obsidian stakes pinning target's shadow!
            float erupt = Mth.clamp(progress / 0.18F, 0.0F, 1.0F);
            float easeOut = 1.0F - (1.0F - erupt) * (1.0F - erupt);
            renderOrientedRing(matrix, vc, inst.start.add(normal.scale(0.03D)), right, up,
                    0.0F, curR * 0.96F, 22, 0.085F, 0.058F, 0.095F, 0.88F * fade);
            renderOrientedRing(matrix, vc, inst.start.add(normal.scale(0.05D)), right, up,
                    curR * 0.86F, curR * 1.04F, 22, 0.56F, 0.36F, 1.00F, 0.86F * fade);
            for (int k = 0; k < 6; k++) {
                float kAng = (float) (Math.PI * 2.0D * k / 6.0D) + inst.seed * 0.23F;
                Vec3 kRad = right.scale(Mth.cos(kAng)).add(up.scale(Mth.sin(kAng))).normalize();
                Vec3 kTang = normal.cross(kRad).normalize();
                Vec3 stakeBase = inst.start.add(kRad.scale(curR * 0.78D)).add(normal.scale(0.02D));
                Vec3 stakeAxis = normal.scale(0.84D).subtract(kRad.scale(0.54D)).normalize();
                float stakeLen = Math.max(0.72F, maxR * 0.82F) * (0.25F + 0.75F * easeOut);
                renderUmbralShadowNeedle(matrix, vc, stakeBase, stakeAxis, kRad, kTang,
                        Math.max(0.09F, maxR * 0.10F), stakeLen, fade);
                renderCrossSegment(matrix, vc, stakeBase.add(stakeAxis.scale(stakeLen * 0.55D)),
                        inst.start.add(normal.scale(0.18D)), 0.036F * fade, 0.129F, 0.102F, 0.129F, 0.92F * fade);
            }
            return;
        } else if (isNecroticUndeadMateria(inst.essence)) {
            // Section 6.10 Necrotic Bone-Shard Shatter & Corpse Bloom Mine Skull Explosion:
            // exploding 3D cursed skull flash + 10 flying bleached bone shrapnel splinters & #5EE088 necrotic shock-ring!
            float erupt = Mth.clamp(progress / 0.22F, 0.0F, 1.0F);
            float easeOut = 1.0F - (1.0F - erupt) * (1.0F - erupt);
            renderOrientedRing(matrix, vc, inst.start.add(normal.scale(0.04D)), right, up,
                    curR * 0.66F, curR * 1.04F, 22, 0.302F, 0.392F, 0.325F, 0.90F * fade);
            renderOrientedRing(matrix, vc, inst.start.add(normal.scale(0.06D)), right, up,
                    curR * 0.82F, curR * 0.96F, 22, 0.369F, 0.878F, 0.533F, 0.94F * fade);
            if (progress < 0.72F) {
                float skullFade = (1.0F - progress / 0.72F) * fade;
                float skullScale = Math.max(0.28F, maxR * (0.24F + 0.22F * easeOut));
                renderSpectralSkullHead(matrix, vc, inst.start.add(normal.scale(0.28D + 0.36D * easeOut)),
                        right, up, normal, skullScale, skullFade);
            }
            for (int b = 0; b < 10; b++) {
                float bAng = (float) (Math.PI * 2.0D * b / 10.0D) + inst.seed * 0.29F;
                Vec3 bRad = right.scale(Mth.cos(bAng)).add(up.scale(Mth.sin(bAng))).normalize();
                Vec3 bTang = normal.cross(bRad).normalize();
                Vec3 shrapnelDir = normal.scale(0.55D).add(bRad.scale(0.84D)).normalize();
                Vec3 sBase = inst.start.add(shrapnelDir.scale(curR * (0.24D + 0.48D * progress)));
                float splinterLen = Math.max(0.45F, maxR * (b % 2 == 0 ? 0.68F : 0.48F)) * (0.30F + 0.70F * easeOut);
                renderSkeletalRibSpike(matrix, vc, sBase, shrapnelDir, normal, bTang,
                        Math.max(0.065F, maxR * 0.075F), splinterLen, fade);
            }
            return;
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
        float inFrac = Math.min(0.12F, 8.0F / inst.maxTicks);
        float outFrac = Math.max(0.76F, 1.0F - (12.0F / inst.maxTicks));
        float fade = computeEnvelope(progress, inFrac, outFrac);
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

        // Counter-rotating Hexagram / Octagram Star Geometry (8-pointed Celestial Wind-Rose for Air & Octagonal Bedrock Compass for Earth!)
        int points = (isAerodynamicMateria(inst.essence) || isTectonicMateria(inst.essence) || inst.tier >= 6) ? 8 : 6;
        renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.015D, 0.0D), right, forward,
                r * 0.93F, points, rotOuter, ar, ag, ab, 0.65F * fade);
        renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.02D, 0.0D), right, forward,
                r * 0.67F, points, rotInner, 1.0F, 1.0F, 1.0F, 0.55F * fade);

        // Soft vertical rising mana curtain around perimeter
        renderVerticalAuraCurtain(matrix, vc, base, r * 0.97F, 0.48F + 0.12F * pulse, 24,
                pr, pg, pb, 0.28F * fade);

        // Air / Aura Pressurized Updraft Seal: inner eye ring + 4 gently curling helical wind wisps!
        if (isAerodynamicMateria(inst.essence)) {
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.025D, 0.0D), right, forward,
                    r * 0.28F, r * 0.34F, 22, 1.0F, 1.0F, 1.0F, 0.74F * fade * pulse);
            for (int w = 0; w < 4; w++) {
                float wPhase = gameTime * 0.16F + (float) (Math.PI * 0.5D * w);
                for (int s = 0; s < 8; s++) {
                    float t0 = s / 8.0F;
                    float t1 = (s + 1) / 8.0F;
                    float wr0 = r * (0.30F + 0.48F * t0);
                    float wr1 = r * (0.30F + 0.48F * t1);
                    float a0 = wPhase + t0 * 3.2F;
                    float a1 = wPhase + t1 * 3.2F;
                    Vec3 p0 = base.add(Mth.cos(a0) * wr0, 0.04D + t0 * 0.85D, Mth.sin(a0) * wr0);
                    Vec3 p1 = base.add(Mth.cos(a1) * wr1, 0.04D + t1 * 0.85D, Mth.sin(a1) * wr1);
                    renderCrossSegment(matrix, vc, p0, p1, 0.032F, ar, ag, ab, 0.65F * fade * (1.0F - t0 * 0.65F));
                }
            }
        } else if (isHydroMateria(inst.essence)) {
            // Section 6.3 Whirlpool Geyser & Fluid Dissolution Pool: swirling inward whirlpool arms & rising 3D bubbles!
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.025D, 0.0D), right, forward,
                    r * 0.24F, r * 0.36F, 24, 0.90F, 0.99F, 1.00F, 0.80F * fade * pulse);
            for (int arm = 0; arm < 6; arm++) {
                float armBase = -gameTime * 0.22F + (float) (Math.PI * 2.0D * arm / 6.0D);
                for (int s = 0; s < 8; s++) {
                    float u0 = s / 8.0F;
                    float u1 = (s + 1) / 8.0F;
                    float gr0 = Mth.lerp(u0, r * 0.94F, r * 0.20F);
                    float gr1 = Mth.lerp(u1, r * 0.94F, r * 0.20F);
                    float ga0 = armBase + u0 * 2.8F;
                    float ga1 = armBase + u1 * 2.8F;
                    Vec3 gp0 = base.add(Mth.cos(ga0) * gr0, 0.03D + u0 * 0.14D, Mth.sin(ga0) * gr0);
                    Vec3 gp1 = base.add(Mth.cos(ga1) * gr1, 0.03D + u1 * 0.14D, Mth.sin(ga1) * gr1);
                    renderCrossSegment(matrix, vc, gp0, gp1, 0.042F,
                            (arm % 2 == 0) ? 0.88F : pr, (arm % 2 == 0) ? 0.98F : pg, 1.00F, 0.72F * fade);
                }
            }
        } else if (isBotanicalMateria(inst.essence)) {
            // Section 6.4 Overgrowth Snare & Ground Briars: woven woodland briar wreath, central flower & creeping wooden roots!
            renderBotanicalBlossom(matrix, vc, base.add(0.0D, 0.03D, 0.0D), right, forward,
                    Math.min(0.85F, r * 0.34F), 6, rotOuter * 1.4F,
                    1.00F, 0.58F, 0.76F, 0.84F * fade * pulse);
            for (int root = 0; root < 6; root++) {
                float rootBase = rotInner * 0.35F + (float) (Math.PI * 2.0D * root / 6.0D);
                for (int s = 0; s < 8; s++) {
                    float u0 = s / 8.0F;
                    float u1 = (s + 1) / 8.0F;
                    float rr0 = Mth.lerp(u0, r * 0.96F, r * 0.24F);
                    float rr1 = Mth.lerp(u1, r * 0.96F, r * 0.24F);
                    float ra0 = rootBase + Mth.sin(u0 * 5.0F + root) * 0.32F;
                    float ra1 = rootBase + Mth.sin(u1 * 5.0F + root) * 0.32F;
                    Vec3 rp0 = base.add(Mth.cos(ra0) * rr0, 0.03D + 0.06D * Mth.sin(u0 * (float) Math.PI), Mth.sin(ra0) * rr0);
                    Vec3 rp1 = base.add(Mth.cos(ra1) * rr1, 0.03D + 0.06D * Mth.sin(u1 * (float) Math.PI), Mth.sin(ra1) * rr1);
                    renderCrossSegment(matrix, vc, rp0, rp1, 0.052F,
                            (root % 2 == 0) ? 0.36F : 0.102F, (root % 2 == 0) ? 0.23F : 0.349F, (root % 2 == 0) ? 0.12F : 0.137F,
                            0.84F * fade);
                }
            }
        } else if (isTectonicMateria(inst.essence)) {
            // Section 6.5 Tectonic Impalement Spike Seal & Burrow Step Fissure: octagonal bedrock seal, 8 fault-lines & perimeter stone teeth!
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.025D, 0.0D), right, forward,
                    r * 0.24F, r * 0.36F, 8, 1.00F, 0.76F, 0.47F, 0.88F * fade * pulse);
            for (int f = 0; f < 8; f++) {
                float fAng = rotOuter * 0.25F + (float) (Math.PI * 2.0D * f / 8.0D);
                Vec3 rad = new Vec3(Mth.cos(fAng), 0.0D, Mth.sin(fAng));
                Vec3 tang = new Vec3(-Mth.sin(fAng), 0.0D, Mth.cos(fAng));
                Vec3 pIn = base.add(rad.scale(r * 0.28D)).add(0.0D, 0.03D, 0.0D);
                Vec3 pOut = base.add(rad.scale(r * 0.94D)).add(0.0D, 0.03D, 0.0D);
                renderCrossSegment(matrix, vc, pIn, pOut, 0.048F, 0.467F, 0.196F, 0.078F, 0.88F * fade);
                renderCrossSegment(matrix, vc, pIn, pOut, 0.022F, 1.00F, 0.76F, 0.47F, 0.92F * fade * pulse);
                renderFacetedStoneSpire(matrix, vc, pOut, worldUp, rad, tang,
                        Math.min(0.14F, r * 0.085F), Math.min(0.42F, r * 0.24F) * pulse, 5, fAng, fade);
            }
        } else if (isGlacialMateria(inst.essence)) {
            // Section 6.6 Rime Pitfall: translucent hollow frosted-ice hex crust, hairline fracture veins, 6-pointed snowflake & rime teeth!
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
            renderFacetedHexPlate(
                    matrix, vc, base.add(0.0D, 0.018D, 0.0D), right, forward,
                    r * 0.95F, rotOuter * 0.12F,
                    0.596F, 0.863F, 0.949F, 0.28F * fade,
                    0.922F, 0.976F, 1.000F, 0.72F * fade * pulse
            );
            renderSnowflakeCrystal(matrix, vc, base.add(0.0D, 0.03D, 0.0D), right, forward,
                    r * 0.96F, rotOuter * 0.35F, 0.90F * fade * pulse);
            for (int f = 0; f < 6; f++) {
                float fAng = rotOuter * 0.35F + (float) (Math.PI * 2.0D * f / 6.0D);
                Vec3 rad = new Vec3(Mth.cos(fAng), 0.0D, Mth.sin(fAng));
                Vec3 tang = new Vec3(-Mth.sin(fAng), 0.0D, Mth.cos(fAng));
                Vec3 pMid = base.add(rad.scale(r * 0.46D)).add(tang.scale(r * ((f % 2 == 0) ? 0.14D : -0.14D))).add(0.0D, 0.032D, 0.0D);
                Vec3 pOut = base.add(rad.scale(r * 0.94D)).add(0.0D, 0.03D, 0.0D);
                // Jagged hairline rime-fracture lines across the hollow ice plate
                renderCrossSegment(matrix, vc, base.add(0.0D, 0.032D, 0.0D), pMid, 0.018F, 0.922F, 0.976F, 1.000F, 0.85F * fade);
                renderCrossSegment(matrix, vc, pMid, pOut, 0.015F, 0.922F, 0.976F, 1.000F, 0.78F * fade);
                renderFacetedIceSpire(matrix, vc, pOut, worldUp, rad, tang,
                        Math.min(0.13F, r * 0.08F), Math.min(0.40F, r * 0.22F) * pulse, 5, fAng, fade);
            }
        } else if (isInfernalMateria(inst.essence)) {
            // Section 6.7 Magma Puddle Trap: volcanic basalt-caldera floor seal, molten crust disc, 8 radial magma fissures & flame tongues!
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.018D, 0.0D), right, forward,
                    0.0F, r * 0.88F, 18, 0.722F, 0.235F, 0.031F, 0.34F * fade * pulse);
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.024D, 0.0D), right, forward,
                    r * 0.84F, r * 1.03F, 18, 0.165F, 0.090F, 0.071F, 0.92F * fade);
            for (int f = 0; f < 8; f++) {
                float fAng = rotOuter * 0.30F + (float) (Math.PI * 2.0D * f / 8.0D);
                Vec3 rad = new Vec3(Mth.cos(fAng), 0.0D, Mth.sin(fAng));
                Vec3 tang = new Vec3(-Mth.sin(fAng), 0.0D, Mth.cos(fAng));
                Vec3 pIn = base.add(rad.scale(r * 0.14D)).add(0.0D, 0.036D, 0.0D);
                Vec3 pMid = base.add(rad.scale(r * 0.54D)).add(tang.scale((f % 2 == 0 ? 1 : -1) * r * 0.08D)).add(0.0D, 0.036D, 0.0D);
                Vec3 pOut = base.add(rad.scale(r * 0.94D)).add(0.0D, 0.036D, 0.0D);
                renderCrossSegment(matrix, vc, pIn, pMid, 0.042F, 1.00F, 0.820F, 0.361F, 0.94F * fade * pulse);
                renderCrossSegment(matrix, vc, pMid, pOut, 0.035F, 1.00F, 0.820F, 0.361F, 0.90F * fade * pulse);
                renderHellfireFlameTongue(matrix, vc, pOut, worldUp, rad, tang,
                        Math.min(0.14F, r * 0.085F), Math.min(0.46F, r * (f % 2 == 0 ? 0.26F : 0.18F)) * pulse, fade);
            }
        } else if (isSolarRadiantMateria(inst.essence)) {
            // Section 6.8 Sunburst Confinement Sigil: 12-ray solar rose-compass glyph & golden light beacons!
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.03D, 0.0D), right, forward,
                    r * 0.98F, 12, rotOuter * 0.45F, 1.00F, 0.886F, 0.471F, 0.88F * fade * pulse);
            for (int f = 0; f < 8; f++) {
                float fAng = rotOuter * 0.45F + (float) (Math.PI * 2.0D * f / 8.0D);
                Vec3 rad = new Vec3(Mth.cos(fAng), 0.0D, Mth.sin(fAng));
                Vec3 tang = new Vec3(-Mth.sin(fAng), 0.0D, Mth.cos(fAng));
                Vec3 pOut = base.add(rad.scale(r * 0.94D)).add(0.0D, 0.03D, 0.0D);
                renderSolarStarburstRay(matrix, vc, pOut, worldUp, rad, tang,
                        Math.min(0.10F, r * 0.065F), Math.min(0.48F, r * 0.26F) * pulse, fade);
            }
        } else if (isUmbralShadowMateria(inst.essence)) {
            // Section 6.9 Void-Blind Mine: dark #211A21 eclipse compass sigil & 8 obsidian shadow-spikes!
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.02D, 0.0D), right, forward,
                    0.0F, r * 0.88F, 22, 0.085F, 0.058F, 0.095F, 0.78F * fade);
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.035D, 0.0D), right, forward,
                    r * 0.96F, 8, -rotOuter * 0.40F, 0.56F, 0.36F, 1.00F, 0.86F * fade * pulse);
            for (int f = 0; f < 8; f++) {
                float fAng = -rotOuter * 0.40F + (float) (Math.PI * 2.0D * f / 8.0D);
                Vec3 rad = new Vec3(Mth.cos(fAng), 0.0D, Mth.sin(fAng));
                Vec3 tang = new Vec3(-Mth.sin(fAng), 0.0D, Mth.cos(fAng));
                Vec3 pOut = base.add(rad.scale(r * 0.92D)).add(0.0D, 0.03D, 0.0D);
                renderUmbralShadowNeedle(matrix, vc, pOut, worldUp, rad, tang,
                        Math.min(0.10F, r * 0.07F), Math.min(0.42F, r * 0.24F) * pulse, fade);
            }
        } else if (isNecroticUndeadMateria(inst.essence)) {
            // Section 6.10 Undead Ossuary Seal (Grave-Phasing & Haunt Zone; Corpse Bloom Mine stays invisible until triggered!):
            // 8 radial bleached-bone spokes, #4D6453 tombstone ossuary ring & 8 inward-curving perimeter rib teeth!
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.02D, 0.0D), right, forward,
                    r * 0.84F, r * 1.02F, 24, 0.302F, 0.392F, 0.325F, 0.88F * fade);
            for (int f = 0; f < 8; f++) {
                float fAng = rotOuter * 0.30F + (float) (Math.PI * 2.0D * f / 8.0D);
                Vec3 rad = new Vec3(Mth.cos(fAng), 0.0D, Mth.sin(fAng));
                Vec3 tang = new Vec3(-Mth.sin(fAng), 0.0D, Mth.cos(fAng));
                Vec3 pIn = base.add(rad.scale(r * 0.22D)).add(0.0D, 0.03D, 0.0D);
                Vec3 pOut = base.add(rad.scale(r * 0.94D)).add(0.0D, 0.03D, 0.0D);
                renderCrossSegment(matrix, vc, pIn, pOut, 0.042F, 0.902F, 0.875F, 0.812F, 0.86F * fade);
                renderSkeletalRibSpike(matrix, vc, pOut, worldUp, rad.scale(-1.0D), tang,
                        Math.min(0.095F, r * 0.068F), Math.min(0.46F, r * 0.26F) * pulse, fade);
            }
        } else if (isAridDesertMateria(inst.essence)) {
            // Section 6.12 Desiccation Siphon Sigil: parched #C2B28F sandstone sun-compass, 8 cracked desert fissures & 8 obelisk teeth!
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.03D, 0.0D), right, forward,
                    r * 0.96F, 8, rotOuter * 0.35F, 0.961F, 0.894F, 0.745F, 0.88F * fade * pulse);
            for (int f = 0; f < 8; f++) {
                float fAng = rotOuter * 0.35F + (float) (Math.PI * 2.0D * f / 8.0D);
                Vec3 rad = new Vec3(Mth.cos(fAng), 0.0D, Mth.sin(fAng));
                Vec3 tang = new Vec3(-Mth.sin(fAng), 0.0D, Mth.cos(fAng));
                Vec3 pIn = base.add(rad.scale(r * 0.22D)).add(0.0D, 0.032D, 0.0D);
                Vec3 pOut = base.add(rad.scale(r * 0.94D)).add(0.0D, 0.032D, 0.0D);
                renderCrossSegment(matrix, vc, pIn, pOut, 0.044F, 0.612F, 0.478F, 0.298F, 0.88F * fade);
                renderFacetedStoneSpire(matrix, vc, pOut, worldUp, rad, tang,
                        Math.min(0.12F, r * 0.075F), Math.min(0.44F, r * 0.25F) * pulse, 4, fAng, fade);
            }
        } else if (isGalvanicLightningMateria(inst.essence)) {
            // Section 6.13 Tesla Arc Mine: #FFE600 8-node copper/gold Tesla coil sigil & crackling perimeter electrodes!
            int flicker = (int) (gameTime * 0.95F) + inst.seed;
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.03D, 0.0D), right, forward,
                    r * 0.96F, 8, rotOuter * 0.65F, 1.00F, 0.902F, 0.00F, 0.92F * fade * pulse);
            for (int f = 0; f < 8; f++) {
                float fAng = rotOuter * 0.45F + (float) (Math.PI * 2.0D * f / 8.0D);
                Vec3 pOut = base.add(Mth.cos(fAng) * r * 0.92D, 0.04D, Mth.sin(fAng) * r * 0.92D);
                Vec3 pTop = pOut.add(0.0D, Math.min(0.38D, r * 0.22D) * pulse, 0.0D);
                renderCrossSegment(matrix, vc, pOut, pTop, 0.052F, 0.85F, 0.48F, 0.22F, 0.94F * fade);
                renderOctahedron(matrix, vc, pTop, 0.075F, 1.00F, 0.902F, 0.00F, 0.96F * fade);
                if (((flicker + f) & 1) == 0) {
                    float nextAng = rotOuter * 0.45F + (float) (Math.PI * 2.0D * ((f + 1) % 8) / 8.0D);
                    Vec3 nextTop = base.add(Mth.cos(nextAng) * r * 0.92D, 0.04D + Math.min(0.38D, r * 0.22D) * pulse, Mth.sin(nextAng) * r * 0.92D);
                    renderCrossSegment(matrix, vc, pTop, nextTop, 0.028F, 1.00F, 1.00F, 0.94F, 0.90F * fade);
                }
            }
        } else if (isMagmaCalderaMateria(inst.essence)) {
            // Section 6.14 Volcanic Vent Geyser Trap: dark basalt-pumice caldera vent rim, boiling #B83C08 lava basin & 8 glowing magma fissures!
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.020D, 0.0D), right, forward,
                    r * 0.78F, r * 1.06F, 24, 0.165F, 0.090F, 0.071F, 0.92F * fade);
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.028D, 0.0D), right, forward,
                    0.0F, r * 0.80F, 22, 0.722F, 0.235F, 0.031F, 0.68F * fade * pulse);
            for (int f = 0; f < 8; f++) {
                float fAng = rotOuter * 0.30F + (float) (Math.PI * 2.0D * f / 8.0D);
                Vec3 rad = new Vec3(Mth.cos(fAng), 0.0D, Mth.sin(fAng));
                Vec3 tang = new Vec3(-Mth.sin(fAng), 0.0D, Mth.cos(fAng));
                Vec3 pIn = base.add(rad.scale(r * 0.22D)).add(0.0D, 0.034D, 0.0D);
                Vec3 pOut = base.add(rad.scale(r * 0.94D)).add(0.0D, 0.034D, 0.0D);
                renderCrossSegment(matrix, vc, pIn, pOut, 0.052F, 1.00F, 0.820F, 0.361F, 0.94F * fade * pulse);
                renderFacetedStoneSpire(matrix, vc, pOut, worldUp, rad, tang,
                        Math.min(0.13F, r * 0.08F), Math.min(0.42F, r * 0.24F) * pulse, 5, fAng, fade);
            }
        } else if (isTempestStormMateria(inst.essence)) {
            // Section 6.15 Atmospheric Downburst Trap: #429BA3 barometric cyclone floor seal & 6 inward-spiraling wind crescents!
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.024D, 0.0D), right, forward,
                    r * 0.82F, r * 1.04F, 26, 0.259F, 0.608F, 0.639F, 0.92F * fade);
            for (int c = 0; c < 6; c++) {
                float cAng = -rotOuter * 0.85F + (float) (Math.PI * 2.0D * c / 6.0D);
                renderSweptWindCrescent(matrix, vc, base.add(0.0D, 0.045D, 0.0D),
                        right, forward, r * 0.78F * pulse, cAng, 0.95F, r * 0.12F,
                        0.847F, 0.980F, 1.000F, 0.88F * fade);
            }
        } else if (isPermafrostGlacialMateria(inst.essence)) {
            // Section 6.16 Powder Snow Pitfall Trap / Ice Statue Blink: 6-pointed #98DCF2 snowflake floor rune & hexagonal rime rim!
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.024D, 0.0D), right, forward,
                    r * 0.84F, r * 1.05F, 6, 0.173F, 0.510F, 0.788F, 0.90F * fade);
            renderSnowflakeCrystal(matrix, vc, base.add(0.0D, 0.036D, 0.0D), right, forward,
                    r * 0.92F * pulse, rotOuter * 0.35F, 0.92F * fade);
        } else if (isOvergrowthRampartMateria(inst.essence)) {
            // Section 6.17 Strangler Briar Cage Mine / Botanical Ground Slam: mossy stone rim, woodland blossom & 6 earthen root spurs!
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.024D, 0.0D), right, forward,
                    r * 0.80F, r * 1.04F, 18, 0.467F, 0.196F, 0.078F, 0.90F * fade);
            renderBotanicalBlossom(matrix, vc, base.add(0.0D, 0.036D, 0.0D), right, forward,
                    r * 0.86F * pulse, 6, rotOuter * 0.25F, 0.173F, 0.902F, 0.408F, 0.84F * fade);
            for (int sp = 0; sp < 6; sp++) {
                float spAng = rotOuter * 0.20F + (float) (Math.PI * 2.0D * sp / 6.0D);
                Vec3 rad = new Vec3(Mth.cos(spAng), 0.0D, Mth.sin(spAng));
                Vec3 tang = new Vec3(-Mth.sin(spAng), 0.0D, Mth.cos(spAng));
                renderFacetedStoneSpire(matrix, vc, base.add(rad.scale(r * 0.92D)), worldUp, rad, tang,
                        Math.min(0.13F, r * 0.075F), Math.min(0.40F, r * 0.22F) * pulse, 5, spAng, fade);
            }
        } else if (isAbrasiveDustMateria(inst.essence)) {
            // Section 6.18 Choking Powder Pitfall: sunken sedimentary sandstone rim, inward-spiraling suffocating chalk dust & 8 grit spurs!
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.020D, 0.0D), right, forward,
                    0.0F, r * 0.88F, 22, 0.761F, 0.698F, 0.561F, 0.34F * fade * pulse);
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.028D, 0.0D), right, forward,
                    r * 0.82F, r * 1.04F, 24, 0.949F, 0.925F, 0.882F, 0.88F * fade);
            for (int sp = 0; sp < 8; sp++) {
                float spAng = -rotOuter * 0.30F + (float) (Math.PI * 2.0D * sp / 8.0D);
                Vec3 rad = new Vec3(Mth.cos(spAng), 0.0D, Mth.sin(spAng));
                Vec3 tang = new Vec3(-Mth.sin(spAng), 0.0D, Mth.cos(spAng));
                renderFacetedStoneSpire(matrix, vc, base.add(rad.scale(r * 0.92D)), worldUp, rad, tang,
                        Math.min(0.12F, r * 0.070F), Math.min(0.36F, r * 0.20F) * pulse, 4, spAng, fade * 0.90F);
            }
        } else if (isSymbioticSporeMateria(inst.essence)) {
            // Section 6.19 Silencing Mycelium Web: sticky #1A5923 / #A88654 hyphal fungal tendrils & 8 glowing mushroom nodes!
            renderBotanicalBlossom(matrix, vc, base.add(0.0D, 0.025D, 0.0D), right, forward,
                    r * 0.96F, 8, rotOuter * 0.25F, 0.102F, 0.349F, 0.137F, 0.72F * fade);
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.030D, 0.0D), right, forward,
                    r * 0.76F, r * 0.98F, 24, 0.659F, 0.525F, 0.329F, 0.86F * fade);
            for (int w = 0; w < 8; w++) {
                float wAng = (float) (Math.PI * 2.0D * w / 8.0D) + rotInner * 0.18F;
                Vec3 outerNode = base.add(Mth.cos(wAng) * r * 0.92D, 0.06D, Mth.sin(wAng) * r * 0.92D);
                renderCrossSegment(matrix, vc, base.add(0.0D, 0.04D, 0.0D), outerNode, 0.042F * fade,
                        0.910F, 0.784F, 0.510F, 0.88F * fade);
                renderOctahedron(matrix, vc, outerNode.add(0.0D, 0.08D, 0.0D), Math.min(0.14F, r * 0.065F) * pulse,
                        0.173F, 0.902F, 0.408F, 0.94F * fade);
            }
        } else if (isBorealTaigaMateria(inst.essence)) {
            // Section 6.20 Frosted Conifer Briar Trap & Frost-Howler Spire Aura (#1A5923 / #98DCF2):
            // 6-pointed boreal snowflake rune intertwined with evergreen thorny spruce briar spurs!
            renderSnowflakeCrystal(matrix, vc, base.add(0.0D, 0.028D, 0.0D), right, forward,
                    r * 0.96F, rotOuter * 0.30F, 0.88F * fade);
            renderBotanicalBlossom(matrix, vc, base.add(0.0D, 0.022D, 0.0D), right, forward,
                    r * 0.90F, 6, -rotInner * 0.25F, 0.102F, 0.349F, 0.137F, 0.78F * fade);
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
            for (int b = 0; b < 8; b++) {
                float bAng = (float) (Math.PI * 2.0D * b / 8.0D) + rotOuter * 0.12F;
                Vec3 rad = new Vec3(Mth.cos(bAng), 0.0D, Mth.sin(bAng));
                Vec3 tang = new Vec3(-Mth.sin(bAng), 0.0D, Mth.cos(bAng));
                renderFacetedIceSpire(matrix, vc, base.add(rad.scale(r * 0.92D)), worldUp, rad, tang,
                        Math.min(0.12F, r * 0.065F), Math.min(0.42F, r * 0.22F) * pulse, 5, bAng, fade * 0.92F);
            }
        } else if (isMiracleOasisMateria(inst.essence)) {
            // Section 6.21 Aquifer Geyser Mine & Aquifer Spring Spire Aura (#1A5923 / #C2B28F / #38C6F4):
            // 8-petaled succulent desert-lotus mandala & concentric springwater aquifer rings!
            renderBotanicalBlossom(matrix, vc, base.add(0.0D, 0.024D, 0.0D), right, forward,
                    r * 0.96F, 8, rotOuter * 0.24F, 0.102F, 0.349F, 0.137F, 0.80F * fade);
            renderBotanicalBlossom(matrix, vc, base.add(0.0D, 0.030D, 0.0D), right, forward,
                    r * 0.68F, 8, -rotInner * 0.28F, 0.761F, 0.698F, 0.561F, 0.76F * fade);
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.036D, 0.0D), right, forward,
                    r * 0.82F, r * 0.98F, 28, 0.220F, 0.776F, 0.957F, 0.86F * fade);
            for (int p = 0; p < 8; p++) {
                float pAng = (float) (Math.PI * 2.0D * p / 8.0D) + rotOuter * 0.16F;
                Vec3 node = base.add(Mth.cos(pAng) * r * 0.90D, 0.10D, Mth.sin(pAng) * r * 0.90D);
                renderOctahedron(matrix, vc, node, Math.min(0.13F, r * 0.062F) * pulse,
                        (p % 2 == 0) ? 0.871F : 0.220F,
                        (p % 2 == 0) ? 0.788F : 0.776F,
                        (p % 2 == 0) ? 0.604F : 0.957F,
                        0.94F * fade);
            }
        } else if (isCellularVitaeMateria(inst.essence)) {
            // Section 6.22 Cellular Overcharge Mine & Cardiovascular Spire Aura (#FF6B9D / #FFD1E3):
            // 8-petaled biological rose heartbeat rune & pulsing life-essence nodes!
            float beat = 0.90F + 0.14F * Mth.abs(Mth.sin(gameTime * 0.24F));
            renderBotanicalBlossom(matrix, vc, base.add(0.0D, 0.024D, 0.0D), right, forward,
                    r * 0.96F * beat, 8, rotOuter * 0.24F, 1.000F, 0.420F, 0.616F, 0.82F * fade);
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.032D, 0.0D), right, forward,
                    r * 0.80F * beat, r * 0.98F * beat, 28, 1.000F, 0.820F, 0.890F, 0.88F * fade);
            for (int p = 0; p < 8; p++) {
                float pAng = (float) (Math.PI * 2.0D * p / 8.0D) + rotInner * 0.18F;
                Vec3 node = base.add(Mth.cos(pAng) * r * 0.90D, 0.10D, Mth.sin(pAng) * r * 0.90D);
                boolean pearl = (p % 2 == 0);
                renderOctahedron(matrix, vc, node, Math.min(0.13F, r * 0.065F) * beat,
                        1.000F, pearl ? 0.820F : 0.420F, pearl ? 0.890F : 0.616F, 0.95F * fade);
            }
        } else if (isSanguineBloodMateria(inst.essence)) {
            // Section 6.23 Sanguine Spike Snare (#8A0303): crimson hemomantic snare rune & 8 hardened blood spikes!
            Vec3 upAxis = new Vec3(0.0D, 1.0D, 0.0D);
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.025D, 0.0D), right, forward,
                    r * 0.94F, 8, rotOuter * 0.30F, 0.541F, 0.012F, 0.012F, 0.90F * fade);
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.032D, 0.0D), right, forward,
                    r * 0.78F, r * 0.98F, 28, 1.000F, 0.220F, 0.298F, 0.84F * fade * pulse);
            for (int s = 0; s < 8; s++) {
                float sAng = (float) (Math.PI * 2.0D * s / 8.0D) + rotInner * 0.14F;
                Vec3 spikeBase = base.add(Mth.cos(sAng) * r * 0.88D, 0.02D, Mth.sin(sAng) * r * 0.88D);
                Vec3 spikeTip = spikeBase.add(Mth.cos(sAng) * 0.12D, 0.46D * pulse, Mth.sin(sAng) * 0.12D);
                renderCrossSegment(matrix, vc, spikeBase, spikeTip, 0.052F,
                        0.541F, 0.012F, 0.012F, 0.95F * fade);
                renderOctahedron(matrix, vc, spikeBase.add(upAxis.scale(0.14D)), Math.min(0.12F, r * 0.055F),
                        1.000F, 0.220F, 0.298F, 0.92F * fade);
            }
        } else if (isFossilAmberMateria(inst.essence)) {
            // Section 6.25 Amber Preservation Vault Glyph (#FF6B9D / #773214 / #F29B2E): hexagonal golden amber
            // safekeeping rune & 6 hovering protective resin crystal facets!
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.025D, 0.0D), right, forward,
                    r * 0.80F, r * 1.00F, 6, 0.467F, 0.196F, 0.078F, 0.92F * fade);
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.032D, 0.0D), right, forward,
                    r * 0.90F, 6, rotOuter * 0.18F, 0.949F, 0.608F, 0.180F, 0.88F * fade * pulse);
            for (int g = 0; g < 6; g++) {
                float gAng = (float) (Math.PI * 2.0D * g / 6.0D) + rotInner * 0.10F;
                Vec3 gem = base.add(Mth.cos(gAng) * r * 0.86D, 0.24D + 0.06D * Mth.sin(gameTime * 0.18F + g), Mth.sin(gAng) * r * 0.86D);
                boolean rose = (g % 2 == 0);
                renderOctahedron(matrix, vc, gem, Math.min(0.14F, r * 0.065F),
                        rose ? 1.000F : 0.949F,
                        rose ? 0.420F : 0.608F,
                        rose ? 0.616F : 0.180F,
                        0.96F * fade);
            }
        } else if (isParadoxEclipseMateria(inst.essence)) {
            // Section 6.26 Total Eclipse Singularity Mine (#FFEA78 / #211A21): 4m pitch-black event horizon disc
            // encircled by a blazing golden coronal starburst ring!
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.022D, 0.0D), right, forward,
                    0.0F, r * 0.78F, 28, 0.085F, 0.065F, 0.090F, 0.88F * fade);
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.032D, 0.0D), right, forward,
                    r * 0.78F, r * 1.02F, 32, 1.000F, 0.918F, 0.471F, 0.96F * fade * pulse);
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.040D, 0.0D), right, forward,
                    r * 0.98F, 8, -rotOuter * 0.35F, 1.000F, 0.975F, 0.820F, 0.90F * fade);
        } else if (isCadaverousBlightMateria(inst.essence)) {
            // Section 6.27 Strangling Rot-Briar Snare (#4D6453 / #34CC48): 4m cage of grasping necrotic vines & bone spikes!
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.025D, 0.0D), right, forward,
                    r * 0.78F, r * 1.00F, 18, 0.302F, 0.392F, 0.325F, 0.92F * fade);
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.032D, 0.0D), right, forward,
                    r * 0.94F, 7, rotOuter * 0.24F, 0.204F, 0.800F, 0.282F, 0.88F * fade * pulse);
            for (int v = 0; v < 8; v++) {
                float vAng = (float) (Math.PI * 2.0D * v / 8.0D) + rotInner * 0.12F;
                Vec3 vBase = base.add(Mth.cos(vAng) * r * 0.88D, 0.02D, Mth.sin(vAng) * r * 0.88D);
                Vec3 vTip = vBase.add(-Mth.cos(vAng) * 0.18D, 0.52D * pulse, -Mth.sin(vAng) * 0.18D);
                boolean bone = (v % 2 == 0);
                renderCrossSegment(matrix, vc, vBase, vTip, 0.052F,
                        bone ? 0.902F : 0.204F,
                        bone ? 0.875F : 0.800F,
                        bone ? 0.812F : 0.282F,
                        0.94F * fade);
            }
        } else if (isCosmicAstralMateria(inst.essence)) {
            // Section 6.28 Cosmic Vortex Singularity Mine (#41304E / #FFE600): starlight rune & golden pulsar geometry!
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.025D, 0.0D), right, forward,
                    r * 0.78F, r * 1.02F, 28, 0.255F, 0.188F, 0.306F, 0.94F * fade);
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.034D, 0.0D), right, forward,
                    r * 0.96F, 8, -rotOuter * 0.32F, 1.000F, 0.902F, 0.000F, 0.94F * fade * pulse);
        } else if (isPhantasmalSoulfireMateria(inst.essence)) {
            // Section 6.29 Soulflame Pyre Trap (#1DF0E5 / #0D6469): ethereal cyan pyre rune & grasping soul-flame tongues!
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.025D, 0.0D), right, forward,
                    r * 0.78F, r * 1.02F, 24, 0.051F, 0.392F, 0.412F, 0.94F * fade);
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.034D, 0.0D), right, forward,
                    r * 0.94F, 8, rotOuter * 0.28F, 0.114F, 0.941F, 0.898F, 0.94F * fade * pulse);
            for (int f = 0; f < 8; f++) {
                float fAng = (float) (Math.PI * 2.0D * f / 8.0D) + rotInner * 0.15F;
                Vec3 fBase = base.add(Mth.cos(fAng) * r * 0.86D, 0.02D, Mth.sin(fAng) * r * 0.86D);
                Vec3 fTip = fBase.add(0.0D, 0.48D * pulse, 0.0D);
                renderCrossSegment(matrix, vc, fBase, fTip, 0.052F, 0.114F, 0.941F, 0.898F, 0.92F * fade);
            }
        } else if (isScaldingVaporMateria(inst.essence)) {
            // Section 6.30 Scalding Geyser Pitfall Trap (#E0F7FA / #30A9B0): condensation pressure rune & hissing vents!
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.025D, 0.0D), right, forward,
                    r * 0.78F, r * 1.02F, 26, 0.188F, 0.663F, 0.690F, 0.92F * fade);
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.034D, 0.0D), right, forward,
                    r * 0.94F, 8, -rotOuter * 0.30F, 0.878F, 0.969F, 0.980F, 0.94F * fade * pulse);
        } else if (isAntimatterNullRMateria(inst.essence)) {
            // Section 6.31 White Hole Singularity Trap (#FFFFFF / #41304E): cosmic event-horizon ring, white-hole star sigil & zero-g filaments!
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.025D, 0.0D), right, forward,
                    r * 0.76F, r * 1.02F, 28, 0.255F, 0.188F, 0.306F, 0.95F * fade);
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.036D, 0.0D), right, forward,
                    r * 0.96F, 8, rotOuter * 0.32F, 1.000F, 1.000F, 1.000F, 0.98F * fade * pulse);
            for (int f = 0; f < 6; f++) {
                float fAng = (float) (Math.PI * 2.0D * f / 6.0D) - rotInner * 0.18F;
                Vec3 fBase = base.add(Mth.cos(fAng) * r * 0.84D, 0.03D, Mth.sin(fAng) * r * 0.84D);
                Vec3 fTip = fBase.add(0.0D, 0.56D * pulse, 0.0D);
                boolean white = (f % 2 == 0);
                renderCrossSegment(matrix, vc, fBase, fTip, 0.048F,
                        white ? 1.000F : 0.255F,
                        white ? 1.000F : 0.188F,
                        white ? 1.000F : 0.306F,
                        0.94F * fade);
            }
        } else if (isAbyssalNullUMateria(inst.essence)) {
            // Section 6.32 Shadow-Snare Singularity Mine (#110B17 / #41304E): pitch-black containment ring, cosmic violet octagram & snare spikes!
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.025D, 0.0D), right, forward,
                    r * 0.76F, r * 1.02F, 28, 0.067F, 0.043F, 0.090F, 0.98F * fade);
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.036D, 0.0D), right, forward,
                    r * 0.96F, 8, -rotOuter * 0.30F, 0.255F, 0.188F, 0.306F, 0.96F * fade * pulse);
            for (int f = 0; f < 6; f++) {
                float fAng = (float) (Math.PI * 2.0D * f / 6.0D) + rotInner * 0.16F;
                Vec3 fBase = base.add(Mth.cos(fAng) * r * 0.86D, 0.03D, Mth.sin(fAng) * r * 0.86D);
                Vec3 fTip = base.add(Mth.cos(fAng) * r * 0.38D, 0.42D * pulse, Mth.sin(fAng) * r * 0.38D);
                boolean dark = (f % 2 == 0);
                renderCrossSegment(matrix, vc, fBase, fTip, 0.048F,
                        dark ? 0.067F : 0.255F,
                        dark ? 0.043F : 0.188F,
                        dark ? 0.090F : 0.306F,
                        0.95F * fade);
            }
        } else if (isDaybreakDawnMateria(inst.essence)) {
            // Section 6.33 Pranic Sunrise Well (#FFEA78 / #A88654): sunlit horizon ring, 8-pointed daybreak star & updraft plumes!
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.025D, 0.0D), right, forward,
                    r * 0.76F, r * 1.02F, 28, 0.659F, 0.525F, 0.329F, 0.94F * fade);
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.036D, 0.0D), right, forward,
                    r * 0.96F, 8, rotOuter * 0.32F, 1.000F, 0.918F, 0.471F, 0.98F * fade * pulse);
            for (int f = 0; f < 6; f++) {
                float fAng = (float) (Math.PI * 2.0D * f / 6.0D) + rotInner * 0.18F;
                Vec3 fBase = base.add(Mth.cos(fAng) * r * 0.84D, 0.03D, Mth.sin(fAng) * r * 0.84D);
                Vec3 fTip = fBase.add(0.0D, 0.54D * pulse, 0.0D);
                boolean sunrise = (f % 2 == 0);
                renderCrossSegment(matrix, vc, fBase, fTip, 0.048F,
                        sunrise ? 1.000F : 0.659F,
                        sunrise ? 0.918F : 0.525F,
                        sunrise ? 0.471F : 0.329F,
                        0.95F * fade);
            }
        } else if (isHadopelagicAbyssMateria(inst.essence)) {
            // Section 6.34 Benthic Maelstrom Pitfall (#211A21 / #225C7C): swirling 4m whirlpool vortex & inward ink currents!
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.025D, 0.0D), right, forward,
                    r * 0.68F, r * 1.02F, 28, 0.129F, 0.102F, 0.129F, 0.96F * fade);
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.040D, 0.0D), right, forward,
                    r * 0.34F, r * 0.62F, 24, 0.133F, 0.361F, 0.486F, 0.94F * fade * pulse);
            for (int f = 0; f < 8; f++) {
                float fAng = (float) (Math.PI * 2.0D * f / 8.0D) - rotOuter * 0.36F;
                Vec3 fOuter = base.add(Mth.cos(fAng) * r * 0.95D, 0.08D, Mth.sin(fAng) * r * 0.95D);
                Vec3 fInner = base.add(Mth.cos(fAng + 0.72F) * r * 0.18D, 0.02D, Mth.sin(fAng + 0.72F) * r * 0.18D);
                boolean ink = (f % 2 == 0);
                renderCrossSegment(matrix, vc, fOuter, fInner, 0.052F,
                        ink ? 0.129F : 0.133F,
                        ink ? 0.102F : 0.361F,
                        ink ? 0.129F : 0.486F,
                        0.96F * fade);
            }
        } else if (isGildedAegisMateria(inst.essence)) {
            // Section 6.35 Citadel Rampart Vault Mine (#FFEA78 / #773214): octagonal golden rune & granite bastion posts!
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.025D, 0.0D), right, forward,
                    r * 0.78F, r * 1.02F, 8, 0.467F, 0.196F, 0.078F, 0.96F * fade);
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.038D, 0.0D), right, forward,
                    r * 0.94F, 8, rotOuter * 0.24F, 1.000F, 0.918F, 0.471F, 0.98F * fade * pulse);
            for (int f = 0; f < 8; f++) {
                float fAng = (float) (Math.PI * 2.0D * f / 8.0D);
                Vec3 fBase = base.add(Mth.cos(fAng) * r * 0.92D, 0.03D, Mth.sin(fAng) * r * 0.92D);
                Vec3 fTip = fBase.add(0.0D, 0.48D * pulse, 0.0D);
                boolean gold = (f % 2 == 0);
                renderCrossSegment(matrix, vc, fBase, fTip, 0.056F,
                        gold ? 1.000F : 0.467F,
                        gold ? 0.918F : 0.196F,
                        gold ? 0.471F : 0.078F,
                        0.97F * fade);
            }
        } else if (isBorealisAuroraMateria(inst.essence)) {
            // Section 6.36 Cryo-Refractive Mirror Trap (#FFEA78 / #98DCF2): mirrored hexagonal polar glyph & cryo-prism facets!
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.020D, 0.0D), right, forward,
                    r * 0.82F, r * 1.04F, 6, 0.596F, 0.863F, 0.949F, 0.96F * fade);
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.032D, 0.0D), right, forward,
                    r * 0.92F, 6, rotOuter * 0.45F, 1.000F, 0.918F, 0.471F, 0.98F * fade);
            for (int f = 0; f < 6; f++) {
                float fAng = -rotOuter * 0.35F + (float) (Math.PI * 2.0D * f / 6.0D);
                Vec3 fBase = base.add(Mth.cos(fAng) * r * 0.90D, 0.04D, Mth.sin(fAng) * r * 0.90D);
                Vec3 fTip = fBase.add(0.0D, 0.54D * pulse, 0.0D);
                boolean gold = (f % 2 == 0);
                renderCrossSegment(matrix, vc, fBase, fTip, 0.054F,
                        gold ? 1.000F : 0.596F,
                        gold ? 0.918F : 0.863F,
                        gold ? 0.471F : 0.949F,
                        0.97F * fade);
            }
        } else if (isPhantasmMirageMateria(inst.essence)) {
            // Section 6.37 False Oasis Lure Glyph (#FFEA78 / #C2B28F + illusory #38C6F4 oasis pool & #2CE668 fronds):
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.020D, 0.0D), right, forward,
                    r * 0.82F, r * 1.04F, 24, 0.761F, 0.698F, 0.561F, 0.94F * fade);
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.030D, 0.0D), right, forward,
                    0.0F, r * 0.76F, 24, 0.220F, 0.776F, 0.957F, 0.58F * fade * pulse);
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.042D, 0.0D), right, forward,
                    r * 0.90F, 8, rotOuter * 0.35F, 1.000F, 0.918F, 0.471F, 0.92F * fade);
            for (int f = 0; f < 6; f++) {
                float fAng = (float) (Math.PI * 2.0D * f / 6.0D) + rotOuter * 0.15F;
                Vec3 fBase = base.add(Mth.cos(fAng) * r * 0.82D, 0.04D, Mth.sin(fAng) * r * 0.82D);
                Vec3 fTip = fBase.add(Mth.cos(fAng) * 0.36D, 0.52D * pulse, Mth.sin(fAng) * 0.36D);
                renderCrossSegment(matrix, vc, fBase, fTip, 0.056F, 0.173F, 0.902F, 0.408F, 0.78F * fade);
            }
        } else if (isIncorporealWraithMateria(inst.essence)) {
            // Section 6.38 Incorporeal Grave-Grip Pitfall Glyph (#41304E / #4D6453): shadowy grave sigil & grasping ghostly skeletal arms!
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.022D, 0.0D), right, forward,
                    r * 0.78F, r * 1.04F, 24, 0.255F, 0.188F, 0.306F, 0.94F * fade);
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.036D, 0.0D), right, forward,
                    r * 0.92F, 8, -rotOuter * 0.40F, 0.302F, 0.392F, 0.325F, 0.90F * fade);
            for (int a = 0; a < 8; a++) {
                float aAng = (float) (Math.PI * 2.0D * a / 8.0D) + rotOuter * 0.22F;
                Vec3 wrist = base.add(Mth.cos(aAng) * r * 0.74D, 0.03D, Mth.sin(aAng) * r * 0.74D);
                Vec3 elbow = wrist.add(-Mth.cos(aAng) * 0.22D, 0.42D * pulse, -Mth.sin(aAng) * 0.22D);
                Vec3 claw = elbow.add(-Mth.cos(aAng) * 0.18D, 0.24D * pulse, -Mth.sin(aAng) * 0.18D);
                boolean moss = (a % 2 == 0);
                renderCrossSegment(matrix, vc, wrist, elbow, 0.058F,
                        moss ? 0.302F : 0.255F,
                        moss ? 0.392F : 0.188F,
                        moss ? 0.325F : 0.306F,
                        0.88F * fade);
                renderCrossSegment(matrix, vc, elbow, claw, 0.042F, 0.620F, 0.840F, 0.680F, 0.92F * fade);
            }
        } else if (isSepulchralBarrowMateria(inst.essence)) {
            // Section 6.39 Cairn Collapse Mine (#773214 / #4D6453): hidden pile of cursed megalithic cairn stones & mossy grave rune!
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.022D, 0.0D), right, forward,
                    r * 0.78F, r * 1.04F, 8, 0.467F, 0.196F, 0.078F, 0.95F * fade);
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.036D, 0.0D), right, forward,
                    r * 0.90F, 6, rotOuter * 0.25F, 0.302F, 0.392F, 0.325F, 0.90F * fade);
            for (int c = 0; c < 6; c++) {
                float cAng = (float) (Math.PI * 2.0D * c / 6.0D) + rotOuter * 0.12F;
                Vec3 stonePos = base.add(Mth.cos(cAng) * r * 0.46D, 0.16D, Mth.sin(cAng) * r * 0.46D);
                boolean moss = (c % 2 == 0);
                renderOctahedron(matrix, vc, stonePos, Math.max(0.18F, r * 0.14F),
                        moss ? 0.302F : 0.467F,
                        moss ? 0.392F : 0.196F,
                        moss ? 0.325F : 0.078F,
                        0.92F * fade);
            }
        } else if (isCrematingPyreMateria(inst.essence)) {
            // Section 6.40 Cremation Pyre-Pitfall Glyph (#773214 / #FF4500): smoldering coal-ember sigil & cracked cedar hearth logs!
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.022D, 0.0D), right, forward,
                    r * 0.78F, r * 1.04F, 12, 0.467F, 0.196F, 0.078F, 0.95F * fade);
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.036D, 0.0D), right, forward,
                    r * 0.90F, 6, rotOuter * 0.35F, 1.000F, 0.271F, 0.000F, 0.94F * fade);
            for (int c = 0; c < 6; c++) {
                float cAng = (float) (Math.PI * 2.0D * c / 6.0D) + rotOuter * 0.18F;
                Vec3 coalPos = base.add(Mth.cos(cAng) * r * 0.46D, 0.15D, Mth.sin(cAng) * r * 0.46D);
                boolean ember = (c % 2 == 0);
                renderOctahedron(matrix, vc, coalPos, Math.max(0.16F, r * 0.12F),
                        ember ? 1.000F : 0.467F,
                        ember ? 0.271F : 0.196F,
                        ember ? 0.000F : 0.078F,
                        0.94F * fade);
            }
        } else if (isTwilightPenumbraMateria(inst.essence)) {
            // Section 6.41 Penumbral Twilight Trap Glyph (#FFEA78 / #281E3C): shifting coronal light & deep indigo gravity sigil!
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.022D, 0.0D), right, forward,
                    r * 0.78F, r * 1.04F, 24, 0.157F, 0.118F, 0.235F, 0.96F * fade);
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.036D, 0.0D), right, forward,
                    r * 0.92F, 8, rotOuter * 0.40F, 1.000F, 0.918F, 0.471F, 0.94F * fade);
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.044D, 0.0D), right, forward,
                    r * 0.62F, 8, -rotOuter * 0.55F, 0.157F, 0.118F, 0.235F, 0.92F * fade);
        } else if (isHoarfrostRimeMateria(inst.essence)) {
            // Section 6.42 Flash-Freeze Rime Pitfall Glyph (#6395B1 / #DCF0FF): crystalline 6-pointed frost rune & jagged rime spikes!
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.022D, 0.0D), right, forward,
                    r * 0.78F, r * 1.04F, 6, 0.388F, 0.584F, 0.694F, 0.95F * fade);
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.036D, 0.0D), right, forward,
                    r * 0.94F, 6, rotOuter * 0.32F, 0.863F, 0.941F, 1.000F, 0.96F * fade);
            for (int c = 0; c < 6; c++) {
                float cAng = (float) (Math.PI * 2.0D * c / 6.0D) + rotOuter * 0.15F;
                Vec3 icePos = base.add(Mth.cos(cAng) * r * 0.48D, 0.16D, Mth.sin(cAng) * r * 0.48D);
                boolean white = (c % 2 == 0);
                renderOctahedron(matrix, vc, icePos, Math.max(0.16F, r * 0.12F),
                        white ? 0.863F : 0.388F,
                        white ? 0.941F : 0.584F,
                        white ? 1.000F : 0.694F,
                        0.94F * fade);
            }
        } else if (isVernalSpringMateria(inst.essence)) {
            // Section 6.43 Bubbling Vernal Aquifer Well Glyph (#FF6B9D / #64C8FF): glowing cerulean spring runes & 5-petal cherry blossoms!
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.022D, 0.0D), right, forward,
                    r * 0.78F, r * 1.04F, 24, 0.392F, 0.784F, 1.000F, 0.94F * fade);
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.036D, 0.0D), right, forward,
                    r * 0.92F, 5, rotOuter * 0.35F, 1.000F, 0.420F, 0.616F, 0.96F * fade);
            for (int p = 0; p < 5; p++) {
                float pAng = (float) (Math.PI * 2.0D * p / 5.0D) + rotOuter * 0.18F;
                Vec3 petalPos = base.add(Mth.cos(pAng) * r * 0.50D, 0.14D, Mth.sin(pAng) * r * 0.50D);
                boolean blossom = (p % 2 == 0);
                renderOctahedron(matrix, vc, petalPos, Math.max(0.15F, r * 0.11F),
                        blossom ? 1.000F : 0.392F,
                        blossom ? 0.420F : 0.784F,
                        blossom ? 0.616F : 1.000F,
                        0.94F * fade);
            }
        } else if (isGalvanicStaticMateria(inst.essence)) {
            // Section 6.44 Galvanic Arc-Snare Sigil (#FFE600 / #878DA2): crackling yellow lightning runes & dielectric slate capacitor nodes!
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.022D, 0.0D), right, forward,
                    r * 0.80F, r * 1.04F, 8, 0.529F, 0.553F, 0.635F, 0.95F * fade);
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.036D, 0.0D), right, forward,
                    r * 0.94F, 8, rotOuter * 0.42F, 1.000F, 0.902F, 0.000F, 0.98F * fade);
            for (int n = 0; n < 6; n++) {
                float nAng = (float) (Math.PI * 2.0D * n / 6.0D) - rotOuter * 0.24F;
                Vec3 nodePos = base.add(Mth.cos(nAng) * r * 0.58D, 0.14D, Mth.sin(nAng) * r * 0.58D);
                boolean yellow = (n % 2 == 0);
                renderOctahedron(matrix, vc, nodePos, Math.max(0.14F, r * 0.10F),
                        yellow ? 1.000F : 0.529F,
                        yellow ? 0.902F : 0.553F,
                        yellow ? 0.000F : 0.635F,
                        0.96F * fade);
            }
        } else if (isPrimevalSylvanMateria(inst.essence)) {
            // Section 6.45 Canopy Solar-Mine (#1A5923 / #773214 / #FFEA78): floating solar leaf-prism mine & ancient sylvan runes!
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.022D, 0.0D), right, forward,
                    r * 0.78F, r * 1.04F, 12, 0.102F, 0.349F, 0.137F, 0.94F * fade);
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.036D, 0.0D), right, forward,
                    r * 0.92F, 6, rotOuter * 0.36F, 1.000F, 0.918F, 0.471F, 0.98F * fade);
            renderOctahedron(matrix, vc, base.add(0.0D, 0.24D, 0.0D), Math.max(0.22F, r * 0.14F),
                    1.000F, 0.918F, 0.471F, 0.98F * fade);
            for (int l = 0; l < 6; l++) {
                float lAng = (float) (Math.PI * 2.0D * l / 6.0D) + rotOuter * 0.20F;
                Vec3 leafPos = base.add(Mth.cos(lAng) * r * 0.54D, 0.18D, Mth.sin(lAng) * r * 0.54D);
                boolean bark = (l % 2 == 0);
                renderOctahedron(matrix, vc, leafPos, Math.max(0.14F, r * 0.10F),
                        bark ? 0.467F : 0.102F,
                        bark ? 0.196F : 0.349F,
                        bark ? 0.078F : 0.137F,
                        0.95F * fade);
            }
        } else if (isPestilentMiasmaMateria(inst.essence)) {
            // Section 6.46 Pressurized Septic Gas-Geyser (#1A5923 / #4D6453 / #225C7C): necrotic swamp pressure rune & bubbling geyser vents!
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.022D, 0.0D), right, forward,
                    r * 0.76F, r * 1.04F, 14, 0.302F, 0.392F, 0.325F, 0.94F * fade);
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.036D, 0.0D), right, forward,
                    r * 0.90F, 6, rotOuter * 0.34F, 0.133F, 0.361F, 0.486F, 0.96F * fade);
            for (int v = 0; v < 6; v++) {
                float vAng = (float) (Math.PI * 2.0D * v / 6.0D) + rotOuter * 0.22F;
                Vec3 ventPos = base.add(Mth.cos(vAng) * r * 0.52D, 0.14D, Mth.sin(vAng) * r * 0.52D);
                boolean green = (v % 2 == 0);
                renderOctahedron(matrix, vc, ventPos, Math.max(0.14F, r * 0.10F),
                        green ? 0.102F : 0.133F,
                        green ? 0.349F : 0.361F,
                        green ? 0.137F : 0.486F,
                        0.95F * fade);
            }
        } else if (isPrimordialGenesisMateria(inst.essence)) {
            // Section 6.47 Well of Creation & Cleansing Geyser (#FF6B9D / #FFEA78 / #225C7C): radiant water & vitae lotus shrine runes!
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.022D, 0.0D), right, forward,
                    r * 0.78F, r * 1.04F, 18, 0.133F, 0.361F, 0.486F, 0.94F * fade);
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.036D, 0.0D), right, forward,
                    r * 0.92F, 6, rotOuter * 0.36F, 1.000F, 0.918F, 0.471F, 0.98F * fade);
            for (int l = 0; l < 6; l++) {
                float lAng = (float) (Math.PI * 2.0D * l / 6.0D) + rotOuter * 0.22F;
                Vec3 lotusPos = base.add(Mth.cos(lAng) * r * 0.54D, 0.16D, Mth.sin(lAng) * r * 0.54D);
                boolean pink = (l % 2 == 0);
                renderOctahedron(matrix, vc, lotusPos, Math.max(0.14F, r * 0.10F),
                        1.000F,
                        pink ? 0.420F : 0.918F,
                        pink ? 0.616F : 0.471F,
                        0.97F * fade);
            }
        } else if (isStygianOblivionMateria(inst.essence)) {
            // Section 6.48 Oblivion Singularity Mine (#B83C08 / #211A21 / #4D6453): stygian obsidian event-horizon ring & blazing magma sigils!
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.022D, 0.0D), right, forward,
                    r * 0.76F, r * 1.05F, 18, 0.129F, 0.102F, 0.129F, 0.96F * fade);
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.038D, 0.0D), right, forward,
                    r * 0.92F, 6, -rotOuter * 0.42F, 0.722F, 0.235F, 0.031F, 0.98F * fade);
            for (int k = 0; k < 6; k++) {
                float kAng = (float) (Math.PI * 2.0D * k / 6.0D) - rotOuter * 0.28F;
                Vec3 skullNode = base.add(Mth.cos(kAng) * r * 0.56D, 0.18D, Mth.sin(kAng) * r * 0.56D);
                boolean rust = (k % 2 == 0);
                renderOctahedron(matrix, vc, skullNode, Math.max(0.15F, r * 0.11F),
                        rust ? 0.722F : 0.302F,
                        rust ? 0.235F : 0.392F,
                        rust ? 0.031F : 0.325F,
                        0.98F * fade);
            }
        } else if (isQuintessentialAetherMateria(inst.essence)) {
            // Section 6.50 Quintessential Firmament Inversion Vortex Sigil (#A88654 / #773214 / #225C7C / #B83C08): radiant quad-elemental mandala!
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.022D, 0.0D), right, forward,
                    r * 0.78F, r * 1.06F, 22, 0.659F, 0.525F, 0.329F, 0.96F * fade);
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.038D, 0.0D), right, forward,
                    r * 0.94F, 8, rotOuter * 0.40F, 0.133F, 0.361F, 0.486F, 0.98F * fade);
            float[][] quadRgb = {
                    {0.659F, 0.525F, 0.329F},
                    {0.467F, 0.196F, 0.078F},
                    {0.133F, 0.361F, 0.486F},
                    {0.722F, 0.235F, 0.031F}
            };
            for (int q = 0; q < 8; q++) {
                float qAng = (float) (Math.PI * 2.0D * q / 8.0D) + rotOuter * 0.26F;
                float[] c = quadRgb[q & 3];
                Vec3 nodePos = base.add(Mth.cos(qAng) * r * 0.58D, 0.18D, Mth.sin(qAng) * r * 0.58D);
                renderOctahedron(matrix, vc, nodePos, Math.max(0.15F, r * 0.11F), c[0], c[1], c[2], 0.98F * fade);
            }
        } else if (isAbyssalEntropicMateria(inst.essence)) {
            // Section 6.51 Entropic Singularity Collapse Sigil (#41304E / #211A21 / #4D6453 / #8A0303): void-shadow mandala!
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.022D, 0.0D), right, forward,
                    r * 0.78F, r * 1.06F, 20, 0.129F, 0.102F, 0.129F, 0.97F * fade);
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.038D, 0.0D), right, forward,
                    r * 0.94F, 8, -rotOuter * 0.42F, 0.541F, 0.012F, 0.012F, 0.98F * fade);
            float[][] entRgb = {
                    {0.255F, 0.188F, 0.306F},
                    {0.129F, 0.102F, 0.129F},
                    {0.302F, 0.392F, 0.325F},
                    {0.541F, 0.012F, 0.012F}
            };
            for (int e = 0; e < 8; e++) {
                float eAng = (float) (Math.PI * 2.0D * e / 8.0D) - rotOuter * 0.28F;
                float[] c = entRgb[e & 3];
                Vec3 nodePos = base.add(Mth.cos(eAng) * r * 0.58D, 0.18D, Mth.sin(eAng) * r * 0.58D);
                renderOctahedron(matrix, vc, nodePos, Math.max(0.15F, r * 0.11F), c[0], c[1], c[2], 0.98F * fade);
            }
        } else if (isHolyCelestialMateria(inst.essence)) {
            // Section 6.52 Holy White-Hole Sigil (#FFEA78 / #FF6B9D / #1A5923 / #A88654): radiant white-gold star-mandala!
            renderOrientedRing(matrix, vc, base.add(0.0D, 0.022D, 0.0D), right, forward,
                    r * 0.78F, r * 1.06F, 24, 1.000F, 0.918F, 0.471F, 0.97F * fade);
            renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.038D, 0.0D), right, forward,
                    r * 0.94F, 8, rotOuter * 0.42F, 1.000F, 0.420F, 0.616F, 0.98F * fade);
            float[][] celRgb = {
                    {1.000F, 0.918F, 0.471F},
                    {1.000F, 0.420F, 0.616F},
                    {0.102F, 0.349F, 0.137F},
                    {0.659F, 0.525F, 0.329F}
            };
            for (int c = 0; c < 8; c++) {
                float cAng = (float) (Math.PI * 2.0D * c / 8.0D) + rotOuter * 0.28F;
                float[] col = celRgb[c & 3];
                Vec3 nodePos = base.add(Mth.cos(cAng) * r * 0.58D, 0.18D, Mth.sin(cAng) * r * 0.58D);
                renderOctahedron(matrix, vc, nodePos, Math.max(0.15F, r * 0.11F), col[0], col[1], col[2], 0.99F * fade);
            }
        }
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

        boolean airBlade = isAerodynamicMateria(inst.essence) || isTempestStormMateria(inst.essence);
        boolean hydroWave = isHydroMateria(inst.essence) || isMiracleOasisMateria(inst.essence);
        boolean botanicalWhip = isBotanicalMateria(inst.essence) || isOvergrowthRampartMateria(inst.essence) || isSymbioticSporeMateria(inst.essence) || isCadaverousBlightMateria(inst.essence);
        boolean tectonicCleave = isTectonicMateria(inst.essence) || isAbrasiveDustMateria(inst.essence);
        boolean glacialCleave = isGlacialMateria(inst.essence) || isPermafrostGlacialMateria(inst.essence) || isBorealTaigaMateria(inst.essence);
        boolean infernalCleave = isInfernalMateria(inst.essence) || isMagmaCalderaMateria(inst.essence);
        boolean solarCleave = isSolarRadiantMateria(inst.essence) || isCellularVitaeMateria(inst.essence);
        boolean sanguineCleave = isSanguineBloodMateria(inst.essence);
        boolean fossilAmberCleave = isFossilAmberMateria(inst.essence);
        boolean paradoxEclipseCleave = isParadoxEclipseMateria(inst.essence);
        boolean cosmicAstralCleave = isCosmicAstralMateria(inst.essence);
        boolean phantasmalSoulfireCleave = isPhantasmalSoulfireMateria(inst.essence);
        boolean scaldingVaporCleave = isScaldingVaporMateria(inst.essence);
        boolean antimatterNullRCleave = isAntimatterNullRMateria(inst.essence);
        boolean abyssalNullUCleave = isAbyssalNullUMateria(inst.essence);
        boolean daybreakDawnCleave = isDaybreakDawnMateria(inst.essence);
        boolean hadopelagicAbyssCleave = isHadopelagicAbyssMateria(inst.essence);
        boolean gildedAegisCleave = isGildedAegisMateria(inst.essence);
        boolean borealisAuroraCleave = isBorealisAuroraMateria(inst.essence);
        boolean phantasmMirageCleave = isPhantasmMirageMateria(inst.essence);
        boolean incorporealWraithCleave = isIncorporealWraithMateria(inst.essence);
        boolean sepulchralBarrowCleave = isSepulchralBarrowMateria(inst.essence);
        boolean crematingPyreCleave = isCrematingPyreMateria(inst.essence);
        boolean twilightPenumbraCleave = isTwilightPenumbraMateria(inst.essence);
        boolean hoarfrostRimeCleave = isHoarfrostRimeMateria(inst.essence);
        boolean vernalSpringCleave = isVernalSpringMateria(inst.essence);
        boolean galvanicStaticCleave = isGalvanicStaticMateria(inst.essence);
        boolean primevalSylvanCleave = isPrimevalSylvanMateria(inst.essence);
        boolean pestilentMiasmaCleave = isPestilentMiasmaMateria(inst.essence);
        boolean primordialGenesisCleave = isPrimordialGenesisMateria(inst.essence);
        boolean stygianOblivionCleave = isStygianOblivionMateria(inst.essence);
        boolean quintessentialAetherCleave = isQuintessentialAetherMateria(inst.essence);
        boolean abyssalEntropicCleave = isAbyssalEntropicMateria(inst.essence);
        boolean holyCelestialCleave = isHolyCelestialMateria(inst.essence);
        boolean umbralCleave = isUmbralShadowMateria(inst.essence);
        boolean necroticCleave = isNecroticUndeadMateria(inst.essence);
        int bladeLayers = airBlade ? 3 : ((hydroWave || botanicalWhip || tectonicCleave || glacialCleave || infernalCleave || solarCleave || sanguineCleave || fossilAmberCleave || paradoxEclipseCleave || cosmicAstralCleave || phantasmalSoulfireCleave || scaldingVaporCleave || antimatterNullRCleave || abyssalNullUCleave || daybreakDawnCleave || hadopelagicAbyssCleave || gildedAegisCleave || borealisAuroraCleave || phantasmMirageCleave || incorporealWraithCleave || sepulchralBarrowCleave || crematingPyreCleave || twilightPenumbraCleave || hoarfrostRimeCleave || vernalSpringCleave || galvanicStaticCleave || primevalSylvanCleave || pestilentMiasmaCleave || primordialGenesisCleave || stygianOblivionCleave || quintessentialAetherCleave || abyssalEntropicCleave || holyCelestialCleave || umbralCleave || necroticCleave) ? 2 : 1);

        for (int layer = 0; layer < bladeLayers; layer++) {
            float baseTiltDeg = (inst.seed % 2 == 0) ? 24.0F : -24.0F;
            if (layer == 1) baseTiltDeg = hydroWave ? (baseTiltDeg * 0.45F) : ((botanicalWhip || tectonicCleave || glacialCleave || infernalCleave || solarCleave || sanguineCleave || fossilAmberCleave || paradoxEclipseCleave || umbralCleave || necroticCleave) ? (-baseTiltDeg * 0.55F) : (-baseTiltDeg * 1.15F));
            else if (layer == 2) baseTiltDeg = 6.0F;

            float tilt = (float) Math.toRadians(baseTiltDeg);
            Vec3 planeRight = flatRight.scale(Mth.cos(tilt)).add(flatUp.scale(Mth.sin(tilt))).normalize();
            Vec3 planeNormal = fwd.cross(planeRight).normalize();

            float fade = computeEnvelope(progress, 0.04F, 0.54F) * (layer == 0 ? 1.0F : 0.78F);
            float sweepProg = Mth.clamp(progress / 0.32F, 0.0F, 1.0F);
            float scaleLayer = layer == 0 ? 1.0F : (layer == 1 ? (hydroWave ? 1.10F : 0.88F) : 1.14F);
            float reach = inst.radiusOrScale * scaleLayer * (0.86F + 0.14F * sweepProg + 0.16F * progress);
            int segs = 20;
            float totalArc = (float) Math.toRadians(airBlade ? 168.0D : (botanicalWhip ? 162.0D : ((hydroWave || infernalCleave || solarCleave || umbralCleave || necroticCleave) ? 158.0D : ((tectonicCleave || glacialCleave) ? 154.0D : 145.0D))));
            float sweepShift = ((sweepProg * 0.82F + progress * 0.18F) - 0.5F) * (layer == 1 && !hydroWave ? -0.65F : 0.65F);
            Vec3 origin = inst.start
                    .add(fwd.scale(0.55D * progress))
                    .add(planeNormal.scale((layer - 1) * (airBlade ? 0.14D : ((hydroWave || botanicalWhip || tectonicCleave || glacialCleave || infernalCleave || solarCleave || umbralCleave || necroticCleave) ? 0.10D : 0.0D))));

            for (int i = 0; i < segs; i++) {
                float t0 = (float) i / segs;
                float t1 = (float) (i + 1) / segs;
                float a0 = (t0 - 0.5F) * totalArc + sweepShift;
                float a1 = (t1 - 0.5F) * totalArc + sweepShift;

                float prof0 = Mth.sin(t0 * (float) Math.PI);
                float prof1 = Mth.sin(t1 * (float) Math.PI);

                Vec3 dir0 = fwd.scale(Mth.cos(a0)).add(planeRight.scale(Mth.sin(a0))).normalize();
                Vec3 dir1 = fwd.scale(Mth.cos(a1)).add(planeRight.scale(Mth.sin(a1))).normalize();

                float widthFrac = hydroWave ? 0.56F : ((tectonicCleave || infernalCleave) ? 0.52F : (glacialCleave ? 0.48F : (botanicalWhip ? 0.38F : 0.44F)));
                float inner0 = reach * (1.0F - widthFrac * prof0);
                float inner1 = reach * (1.0F - widthFrac * prof1);

                Vec3 pIn0 = origin.add(dir0.scale(inner0));
                Vec3 pOut0 = origin.add(dir0.scale(reach));
                Vec3 pOut1 = origin.add(dir1.scale(reach));
                Vec3 pIn1 = origin.add(dir1.scale(inner1));

                float bodyR = hydroWave && layer == 0 ? 0.133F : (botanicalWhip ? 0.102F : (tectonicCleave ? 0.353F : (glacialCleave ? 0.173F : (infernalCleave ? 0.722F : (umbralCleave ? 0.085F : (necroticCleave ? 0.302F : pr))))));
                float bodyG = hydroWave && layer == 0 ? 0.361F : (botanicalWhip ? 0.349F : (tectonicCleave ? 0.322F : (glacialCleave ? 0.510F : (infernalCleave ? 0.235F : (umbralCleave ? 0.058F : (necroticCleave ? 0.392F : pg))))));
                float bodyB = hydroWave && layer == 0 ? 0.486F : (botanicalWhip ? 0.137F : (tectonicCleave ? 0.298F : (glacialCleave ? 0.788F : (infernalCleave ? 0.031F : (umbralCleave ? 0.095F : (necroticCleave ? 0.325F : pb))))));
                float edgeR = botanicalWhip ? 0.54F : (tectonicCleave ? 0.467F : (glacialCleave ? 0.596F : (infernalCleave ? 1.00F : (umbralCleave ? 0.22F : (necroticCleave ? 0.369F : ar)))));
                float edgeG = botanicalWhip ? 1.00F : (tectonicCleave ? 0.196F : (glacialCleave ? 0.863F : (infernalCleave ? 0.820F : (umbralCleave ? 0.14F : (necroticCleave ? 0.878F : ag)))));
                float edgeB = botanicalWhip ? 0.23F : (tectonicCleave ? 0.078F : (glacialCleave ? 0.949F : (infernalCleave ? 0.361F : (umbralCleave ? 0.34F : (necroticCleave ? 0.533F : ab)))));

                emitDoubleSidedQuad(matrix, vc,
                        pIn0, pOut0, pOut1, pIn1,
                        bodyR, bodyG, bodyB, ((hydroWave || botanicalWhip || tectonicCleave || glacialCleave || infernalCleave || umbralCleave || necroticCleave) ? 0.72F : 0.18F) * fade * prof0,
                        edgeR, edgeG, edgeB, 0.92F * fade * prof0,
                        edgeR, edgeG, edgeB, 0.92F * fade * prof1,
                        bodyR, bodyG, bodyB, ((hydroWave || botanicalWhip || tectonicCleave || glacialCleave || infernalCleave || umbralCleave || necroticCleave) ? 0.72F : 0.18F) * fade * prof1);

                // White-hot razor cutting rim / foaming whitewater lip / virulent Poison Ivy sap edge / amber fault seam / twilight shadow rim / bleached bone rim
                float rimFrac = hydroWave ? 0.14F : ((tectonicCleave || glacialCleave || infernalCleave || umbralCleave || necroticCleave) ? 0.13F : (botanicalWhip ? 0.11F : 0.085F));
                Vec3 pRim0 = origin.add(dir0.scale(reach * (1.0F - rimFrac * prof0)));
                Vec3 pRim1 = origin.add(dir1.scale(reach * (1.0F - rimFrac * prof1)));
                float tipR = botanicalWhip ? 0.78F : (glacialCleave ? 0.94F : (umbralCleave ? 0.56F : (necroticCleave ? 0.902F : 1.0F)));
                float tipG = tectonicCleave ? 0.78F : (infernalCleave ? 0.94F : (umbralCleave ? 0.36F : (necroticCleave ? 0.875F : 1.0F)));
                float tipB = botanicalWhip ? 0.42F : (tectonicCleave ? 0.48F : (infernalCleave ? 0.62F : (necroticCleave ? 0.812F : 1.0F)));
                emitDoubleSidedQuad(matrix, vc,
                        pRim0, pOut0, pOut1, pRim1,
                        hydroWave ? 0.88F : tipR, hydroWave ? 0.98F : tipG, botanicalWhip ? 0.28F : tipB, 0.88F * fade * prof0,
                        tipR, tipG, tipB, 0.98F * fade * prof0,
                        tipR, tipG, tipB, 0.98F * fade * prof1,
                        hydroWave ? 0.88F : tipR, hydroWave ? 0.98F : tipG, botanicalWhip ? 0.28F : tipB, 0.88F * fade * prof1);

                // Section 6.4 Poison Ivy Slash: jagged 3D thorn barbs jutting outward off the thorny crescent cleave!
                if (isMiracleOasisMateria(inst.essence) && layer == 0 && i > 1 && i < segs - 1 && i % 3 == 0) {
                    // Section 6.21 Desert Blessing Touch: blooming #C2B28F / #1A5923 desert lotus blossoms along the springwater wave!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    boolean goldPetal = ((i / 3) % 2 == 0);
                    renderOctahedron(matrix, vc, midOut, 0.115F * reach * prof0,
                            goldPetal ? 0.871F : 0.133F,
                            goldPetal ? 0.788F : 0.471F,
                            goldPetal ? 0.604F : 0.188F,
                            0.95F * fade * prof0);
                } else if (botanicalWhip && layer == 0 && i > 1 && i < segs - 1 && i % 2 == 0) {
                    Vec3 barbTip = pOut0.add(dir0.scale(0.24D * reach * prof0))
                            .add(pOut1.subtract(pOut0).scale(0.65D));
                    emitDoubleSidedFlatQuad(matrix, vc, pOut0, pOut1, barbTip, barbTip,
                            0.54F, 1.00F, 0.23F, 0.92F * fade * prof0);
                } else if (tectonicCleave && layer == 0 && i > 1 && i < segs - 1 && i % 3 == 0) {
                    // Section 6.5 Seismic Impact: heavy 3D chiseled granite & amber stone teeth along the cleave!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    Vec3 tang = pOut1.subtract(pOut0).normalize();
                    renderFacetedStoneSpire(matrix, vc, midOut, dir0, tang, planeNormal,
                            0.095F * reach * prof0, 0.34F * reach * prof0, 4, i * 0.7F, fade * prof0);
                } else if (glacialCleave && layer == 0 && i > 1 && i < segs - 1 && i % 2 == 0) {
                    // Section 6.6 Flash-Freeze Stasis: razor 3D crystalline icicle teeth jutting off the frost cleave!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    Vec3 tang = pOut1.subtract(pOut0).normalize();
                    renderFacetedIceSpire(matrix, vc, midOut, dir0, tang, planeNormal,
                            0.082F * reach * prof0, 0.36F * reach * prof0, 5, i * 0.65F, fade * prof0);
                } else if (infernalCleave && layer == 0 && i > 1 && i < segs - 1 && i % 2 == 0) {
                    // Section 6.7 Cauterizing Cleave: serrated 3D roaring hellfire flame tongues jutting off the molten arc!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    Vec3 tang = pOut1.subtract(pOut0).normalize();
                    renderHellfireFlameTongue(matrix, vc, midOut, dir0, tang, planeNormal,
                            0.115F * reach * prof0, 0.42F * reach * prof0, fade * prof0);
                } else if (isCellularVitaeMateria(inst.essence) && layer == 0 && i > 1 && i < segs - 1 && i % 3 == 0) {
                    // Section 6.22 Cellular Overcharge Strike: pulsating #FF6B9D / #FFD1E3 rose vital-force nodes along the strike!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    boolean pearl = ((i / 3) % 2 == 0);
                    renderOctahedron(matrix, vc, midOut, 0.12F * reach * prof0,
                            1.000F, pearl ? 0.820F : 0.420F, pearl ? 0.890F : 0.616F, 0.96F * fade * prof0);
                } else if (sanguineCleave && layer == 0 && i > 1 && i < segs - 1 && i % 2 == 0) {
                    // Section 6.23 Vampiric Exsanguination Strike: jagged #8A0303 crystallized blood teeth along the strike!
                    Vec3 barbTip = pOut0.add(dir0.scale(0.32D * reach * prof0))
                            .add(pOut1.subtract(pOut0).scale(0.55D));
                    emitDoubleSidedFlatQuad(matrix, vc, pOut0, pOut1, barbTip, barbTip,
                            0.541F, 0.012F, 0.012F, 0.96F * fade * prof0);
                } else if (fossilAmberCleave && layer == 0 && i > 1 && i < segs - 1 && i % 2 == 0) {
                    // Section 6.25 Fossilizing Stasis Strike: petrified #773214 / #F29B2E / #FF6B9D amber crystal teeth along the cleave!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    boolean rose = ((i / 2) % 2 == 0);
                    renderOctahedron(matrix, vc, midOut, 0.125F * reach * prof0,
                            rose ? 1.000F : 0.949F,
                            rose ? 0.420F : 0.608F,
                            rose ? 0.616F : 0.180F,
                            0.96F * fade * prof0);
                } else if (paradoxEclipseCleave && layer == 0 && i > 0 && i < segs && i % 2 == 0) {
                    // Section 6.26 Umbral Siphon Palm: alternating #FFEA78 coronal rays & #211A21 umbral daggers!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    Vec3 tang = pOut1.subtract(pOut0).normalize();
                    if ((i / 2) % 2 == 0) {
                        renderSolarStarburstRay(matrix, vc, midOut, dir0, tang, planeNormal,
                                0.088F * reach * prof0, 0.46F * reach * prof0, fade * prof0);
                    } else {
                        renderUmbralShadowNeedle(matrix, vc, midOut, dir0, tang, planeNormal,
                                0.085F * reach * prof0, 0.44F * reach * prof0, fade * prof0);
                    }
                } else if (cosmicAstralCleave && layer == 0 && i > 0 && i < segs && i % 2 == 0) {
                    // Section 6.28 Cosmic Overcharge Strike: crackling #FFE600 pulsar rays & #41304E void star spikes!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    Vec3 tipOut = midOut.add(dir0.scale(0.46D * reach * prof0));
                    boolean gold = ((i / 2) % 2 == 0);
                    renderCrossSegment(matrix, vc, midOut, tipOut, 0.052F * fade * prof0,
                            gold ? 1.000F : 0.255F,
                            gold ? 0.902F : 0.188F,
                            gold ? 0.000F : 0.306F,
                            0.96F * fade);
                } else if (phantasmalSoulfireCleave && layer == 0 && i > 0 && i < segs && i % 2 == 0) {
                    // Section 6.29 Phantasmal Soul Cleave: roaring #1DF0E5 / #0D6469 cyan soulflame tongues!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    Vec3 tipOut = midOut.add(dir0.scale(0.44D * reach * prof0)).add(0.0D, 0.16D * prof0, 0.0D);
                    boolean cyan = ((i / 2) % 2 == 0);
                    renderCrossSegment(matrix, vc, midOut, tipOut, 0.056F * fade * prof0,
                            cyan ? 0.114F : 0.051F,
                            cyan ? 0.941F : 0.392F,
                            cyan ? 0.898F : 0.412F,
                            0.96F * fade);
                } else if (scaldingVaporCleave && layer == 0 && i > 0 && i < segs && i % 2 == 0) {
                    // Section 6.30 Superheated Hydro-Gouge: pressurized #E0F7FA steam jets & #30A9B0 boiling water spikes!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    Vec3 tipOut = midOut.add(dir0.scale(0.46D * reach * prof0));
                    boolean white = ((i / 2) % 2 == 0);
                    renderCrossSegment(matrix, vc, midOut, tipOut, 0.054F * fade * prof0,
                            white ? 0.878F : 0.188F,
                            white ? 0.969F : 0.663F,
                            white ? 0.980F : 0.690F,
                            0.95F * fade);
                } else if (antimatterNullRCleave && layer == 0 && i > 0 && i < segs && i % 2 == 0) {
                    // Section 6.31 Event Horizon Palm: incandescent #FFFFFF antimatter rays & #41304E spacetime fracture spikes!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    Vec3 tipOut = midOut.add(dir0.scale(0.50D * reach * prof0));
                    boolean white = ((i / 2) % 2 == 0);
                    renderCrossSegment(matrix, vc, midOut, tipOut, 0.058F * fade * prof0,
                            white ? 1.000F : 0.255F,
                            white ? 1.000F : 0.188F,
                            white ? 1.000F : 0.306F,
                            0.97F * fade);
                } else if (abyssalNullUCleave && layer == 0 && i > 0 && i < segs && i % 2 == 0) {
                    // Section 6.32 Spacetime Oblivion Palm: pitch-black #110B17 shadow-gouge claws & #41304E void fractures!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    Vec3 tipOut = midOut.add(dir0.scale(0.50D * reach * prof0));
                    boolean dark = ((i / 2) % 2 == 0);
                    renderCrossSegment(matrix, vc, midOut, tipOut, 0.058F * fade * prof0,
                            dark ? 0.067F : 0.255F,
                            dark ? 0.043F : 0.188F,
                            dark ? 0.090F : 0.306F,
                            0.98F * fade);
                } else if (daybreakDawnCleave && layer == 0 && i > 0 && i < segs && i % 2 == 0) {
                    // Section 6.33 Aero-Solar Cleave: blazing #FFEA78 sunrise rays & #A88654 gale wind-feather spikes!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    Vec3 tipOut = midOut.add(dir0.scale(0.52D * reach * prof0));
                    boolean sunrise = ((i / 2) % 2 == 0);
                    renderCrossSegment(matrix, vc, midOut, tipOut, 0.056F * fade * prof0,
                            sunrise ? 1.000F : 0.659F,
                            sunrise ? 0.918F : 0.525F,
                            sunrise ? 0.471F : 0.329F,
                            0.97F * fade);
                } else if (hadopelagicAbyssCleave && layer == 0 && i > 0 && i < segs && i % 2 == 0) {
                    // Section 6.34 Abyssal Drowning Cleave: sweeping #211A21 dark-ink blades & #225C7C trench surf spikes!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    Vec3 tipOut = midOut.add(dir0.scale(0.50D * reach * prof0));
                    boolean ink = ((i / 2) % 2 == 0);
                    renderCrossSegment(matrix, vc, midOut, tipOut, 0.058F * fade * prof0,
                            ink ? 0.129F : 0.133F,
                            ink ? 0.102F : 0.361F,
                            ink ? 0.129F : 0.486F,
                            0.98F * fade);
                } else if (gildedAegisCleave && layer == 0 && i > 0 && i < segs && i % 2 == 0) {
                    // Section 6.35 Gilded Shield-Bash Strike: #FFEA78 holy quartz spikes & #773214 granite shield-bash teeth!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    Vec3 tipOut = midOut.add(dir0.scale(0.48D * reach * prof0));
                    boolean gold = ((i / 2) % 2 == 0);
                    renderCrossSegment(matrix, vc, midOut, tipOut, 0.064F * fade * prof0,
                            gold ? 1.000F : 0.467F,
                            gold ? 0.918F : 0.196F,
                            gold ? 0.471F : 0.078F,
                            0.98F * fade);
                } else if (borealisAuroraCleave && layer == 0 && i > 0 && i < segs && i % 2 == 0) {
                    // Section 6.36 Prismatic Aurora Polar Cleave: #FFEA78 northern-light rays & #98DCF2 crystalline icicle teeth!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    Vec3 tipOut = midOut.add(dir0.scale(0.50D * reach * prof0));
                    boolean gold = ((i / 2) % 2 == 0);
                    renderCrossSegment(matrix, vc, midOut, tipOut, 0.060F * fade * prof0,
                            gold ? 1.000F : 0.596F,
                            gold ? 0.918F : 0.863F,
                            gold ? 0.471F : 0.949F,
                            0.98F * fade);
                } else if (phantasmMirageCleave && layer == 0 && i > 0 && i < segs && i % 2 == 0) {
                    // Section 6.37 Phantasmal Paranoia & Traitor Strike: #FFEA78 mirage mirror shards & #C2B28F heat-haze blades!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    Vec3 tipOut = midOut.add(dir0.scale(0.48D * reach * prof0));
                    boolean gold = ((i / 2) % 2 == 0);
                    renderCrossSegment(matrix, vc, midOut, tipOut, 0.058F * fade * prof0,
                            gold ? 1.000F : 0.761F,
                            gold ? 0.918F : 0.698F,
                            gold ? 0.471F : 0.561F,
                            0.96F * fade);
                } else if (incorporealWraithCleave && layer == 0 && i > 0 && i < segs && i % 2 == 0) {
                    // Section 6.38 Spectral Phasing Palm-Gouge: #41304E / #4D6453 shield-phasing ghostly claw prongs!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    Vec3 tipOut = midOut.add(dir0.scale(0.52D * reach * prof0));
                    boolean moss = ((i / 2) % 2 == 0);
                    renderCrossSegment(matrix, vc, midOut, tipOut, 0.062F * fade * prof0,
                            moss ? 0.302F : 0.255F,
                            moss ? 0.392F : 0.188F,
                            moss ? 0.325F : 0.306F,
                            0.96F * fade);
                } else if (sepulchralBarrowCleave && layer == 0 && i > 0 && i < segs && i % 2 == 0) {
                    // Section 6.39 Barrow-Cairn Tombstone Slam: #773214 / #4D6453 megalithic headstone teeth & petrified bone spikes!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    Vec3 tipOut = midOut.add(dir0.scale(0.52D * reach * prof0));
                    boolean granite = ((i / 2) % 2 == 0);
                    renderCrossSegment(matrix, vc, midOut, tipOut, 0.072F * fade * prof0,
                            granite ? 0.467F : 0.302F,
                            granite ? 0.196F : 0.392F,
                            granite ? 0.078F : 0.325F,
                            0.98F * fade);
                } else if (crematingPyreCleave && layer == 0 && i > 0 && i < segs && i % 2 == 0) {
                    // Section 6.40 Cremating Timber-Pyre Cleave: #773214 / #FF4500 blazing petrified wood & roaring coal-flame teeth!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    Vec3 tipOut = midOut.add(dir0.scale(0.56D * reach * prof0));
                    boolean flame = ((i / 2) % 2 == 0);
                    renderCrossSegment(matrix, vc, midOut, tipOut, 0.074F * fade * prof0,
                            flame ? 1.000F : 0.467F,
                            flame ? 0.271F : 0.196F,
                            flame ? 0.000F : 0.078F,
                            0.98F * fade);
                } else if (twilightPenumbraCleave && layer == 0 && i > 0 && i < segs && i % 2 == 0) {
                    // Section 6.41 Penumbral Eclipse Cleave: #FFEA78 / #281E3C lensed coronal sunlight & twilight shadow scythe teeth!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    Vec3 tipOut = midOut.add(dir0.scale(0.58D * reach * prof0));
                    boolean corona = ((i / 2) % 2 == 0);
                    renderCrossSegment(matrix, vc, midOut, tipOut, 0.070F * fade * prof0,
                            corona ? 1.000F : 0.157F,
                            corona ? 0.918F : 0.118F,
                            corona ? 0.471F : 0.235F,
                            0.98F * fade);
                } else if (hoarfrostRimeCleave && layer == 0 && i > 0 && i < segs && i % 2 == 0) {
                    // Section 6.42 Permafrost Rime-Cleave: #6395B1 / #DCF0FF jagged hoarfrost icicle teeth!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    Vec3 tipOut = midOut.add(dir0.scale(0.58D * reach * prof0));
                    boolean white = ((i / 2) % 2 == 0);
                    renderCrossSegment(matrix, vc, midOut, tipOut, 0.072F * fade * prof0,
                            white ? 0.863F : 0.388F,
                            white ? 0.941F : 0.584F,
                            white ? 1.000F : 0.694F,
                            0.98F * fade);
                } else if (vernalSpringCleave && layer == 0 && i > 0 && i < segs && i % 2 == 0) {
                    // Section 6.43 Vernal Blossom-Cleave: #FF6B9D / #64C8FF surging aquifer water & thorned cherry blossom vines!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    Vec3 tipOut = midOut.add(dir0.scale(0.54D * reach * prof0));
                    boolean blossom = ((i / 2) % 2 == 0);
                    renderCrossSegment(matrix, vc, midOut, tipOut, 0.068F * fade * prof0,
                            blossom ? 1.000F : 0.392F,
                            blossom ? 0.420F : 0.784F,
                            blossom ? 0.616F : 1.000F,
                            0.97F * fade);
                } else if (galvanicStaticCleave && layer == 0 && i > 0 && i < segs && i % 2 == 0) {
                    // Section 6.44 Dielectric Palm Overload: #FFE600 / #878DA2 snapping high-voltage static arc teeth!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    Vec3 tipOut = midOut.add(dir0.scale(0.60D * reach * prof0));
                    boolean yellow = ((i / 2) % 2 == 0);
                    renderCrossSegment(matrix, vc, midOut, tipOut, 0.072F * fade * prof0,
                            yellow ? 1.000F : 0.529F,
                            yellow ? 0.902F : 0.553F,
                            yellow ? 0.000F : 0.635F,
                            0.99F * fade);
                } else if (primevalSylvanCleave && layer == 0 && i > 0 && i < segs && i % 2 == 0) {
                    // Section 6.45 Elderwood Canopy-Cleave: #1A5923 / #773214 / #FFEA78 petrified oak thorns & golden sun-rays!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    Vec3 tipOut = midOut.add(dir0.scale(0.56D * reach * prof0));
                    boolean gold = ((i / 2) % 2 == 0);
                    renderCrossSegment(matrix, vc, midOut, tipOut, 0.074F * fade * prof0,
                            gold ? 1.000F : 0.467F,
                            gold ? 0.918F : 0.196F,
                            gold ? 0.471F : 0.078F,
                            0.98F * fade);
                } else if (pestilentMiasmaCleave && layer == 0 && i > 0 && i < segs && i % 2 == 0) {
                    // Section 6.46 Septic Quagmire Palm-Gouge: #1A5923 / #4D6453 / #225C7C decomposing swamp muck & necrotic bile claws!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    Vec3 tipOut = midOut.add(dir0.scale(0.54D * reach * prof0));
                    boolean teal = ((i / 2) % 2 == 0);
                    renderCrossSegment(matrix, vc, midOut, tipOut, 0.078F * fade * prof0,
                            teal ? 0.133F : 0.102F,
                            teal ? 0.361F : 0.349F,
                            teal ? 0.486F : 0.137F,
                            0.97F * fade);
                } else if (primordialGenesisCleave && layer == 0 && i > 0 && i < segs && i % 2 == 0) {
                    // Section 6.47 Primordial Inception Palm-Strike: #FF6B9D / #FFEA78 / #225C7C concentrated dawn vitae & sacred tidal crest!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    Vec3 tipOut = midOut.add(dir0.scale(0.58D * reach * prof0));
                    boolean gold = ((i / 2) % 2 == 0);
                    renderCrossSegment(matrix, vc, midOut, tipOut, 0.076F * fade * prof0,
                            1.000F,
                            gold ? 0.918F : 0.420F,
                            gold ? 0.471F : 0.616F,
                            0.99F * fade);
                } else if (stygianOblivionCleave && layer == 0 && i > 0 && i < segs && i % 2 == 0) {
                    // Section 6.48 Stygian Extinction Palm-Cleave: #B83C08 / #211A21 / #4D6453 blazing magma rust & obsidian ash claws!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    Vec3 tipOut = midOut.add(dir0.scale(0.62D * reach * prof0));
                    boolean rust = ((i / 2) % 2 == 0);
                    renderCrossSegment(matrix, vc, midOut, tipOut, 0.082F * fade * prof0,
                            rust ? 0.722F : 0.129F,
                            rust ? 0.235F : 0.102F,
                            rust ? 0.031F : 0.129F,
                            0.99F * fade);
                } else if (quintessentialAetherCleave && layer == 0 && i > 0 && i < segs && i % 2 == 0) {
                    // Section 6.50 Quintessential Firmament Sunder: #A88654 / #773214 / #225C7C / #B83C08 quad-elemental firmament rays!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    Vec3 tipOut = midOut.add(dir0.scale(0.66D * reach * prof0));
                    int qIdx = (i / 2) & 3;
                    float cr = qIdx == 0 ? 0.659F : (qIdx == 1 ? 0.467F : (qIdx == 2 ? 0.133F : 0.722F));
                    float cg = qIdx == 0 ? 0.525F : (qIdx == 1 ? 0.196F : (qIdx == 2 ? 0.361F : 0.235F));
                    float cb = qIdx == 0 ? 0.329F : (qIdx == 1 ? 0.078F : (qIdx == 2 ? 0.486F : 0.031F));
                    renderCrossSegment(matrix, vc, midOut, tipOut, 0.084F * fade * prof0, cr, cg, cb, 0.99F * fade);
                } else if (abyssalEntropicCleave && layer == 0 && i > 0 && i < segs && i % 2 == 0) {
                    // Section 6.51 Entropic Singularity Cleave: #41304E / #211A21 / #4D6453 / #8A0303 event-horizon & blood scythe teeth!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    Vec3 tipOut = midOut.add(dir0.scale(0.66D * reach * prof0));
                    int eIdx = (i / 2) & 3;
                    float cr = eIdx == 0 ? 0.255F : (eIdx == 1 ? 0.129F : (eIdx == 2 ? 0.302F : 0.541F));
                    float cg = eIdx == 0 ? 0.188F : (eIdx == 1 ? 0.102F : (eIdx == 2 ? 0.392F : 0.012F));
                    float cb = eIdx == 0 ? 0.306F : (eIdx == 1 ? 0.129F : (eIdx == 2 ? 0.325F : 0.012F));
                    renderCrossSegment(matrix, vc, midOut, tipOut, 0.084F * fade * prof0, cr, cg, cb, 0.99F * fade);
                } else if (holyCelestialCleave && layer == 0 && i > 0 && i < segs && i % 2 == 0) {
                    // Section 6.52 Holy Supernova Cleave / White-Hole Touch: #FFEA78 / #FF6B9D / #1A5923 / #A88654 solar corona & living briar rays!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    Vec3 tipOut = midOut.add(dir0.scale(0.68D * reach * prof0));
                    int cIdx = (i / 2) & 3;
                    float cr = cIdx == 0 ? 1.000F : (cIdx == 1 ? 1.000F : (cIdx == 2 ? 0.102F : 0.659F));
                    float cg = cIdx == 0 ? 0.918F : (cIdx == 1 ? 0.420F : (cIdx == 2 ? 0.349F : 0.525F));
                    float cb = cIdx == 0 ? 0.471F : (cIdx == 1 ? 0.616F : (cIdx == 2 ? 0.137F : 0.329F));
                    renderCrossSegment(matrix, vc, midOut, tipOut, 0.086F * fade * prof0, cr, cg, cb, 0.99F * fade);
                } else if (solarCleave && layer == 0 && i > 0 && i < segs && i % 2 == 0) {
                    // Section 6.8 Holy Smite & Daylight Ignition: 9 radiant 3D golden-white sun-ray lance teeth along the solar crescent!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    Vec3 tang = pOut1.subtract(pOut0).normalize();
                    renderSolarStarburstRay(matrix, vc, midOut, dir0, tang, planeNormal,
                            0.088F * reach * prof0, 0.48F * reach * prof0, fade * prof0);
                } else if (umbralCleave && layer == 0 && i > 0 && i < segs && i % 2 == 0) {
                    // Section 6.9 Umbral Shroud Cloaking: 9 obsidian shadow-dagger teeth trailing off the smoky crescent!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    Vec3 tang = pOut1.subtract(pOut0).normalize();
                    renderUmbralShadowNeedle(matrix, vc, midOut, dir0, tang, planeNormal,
                            0.085F * reach * prof0, 0.44F * reach * prof0, fade * prof0);
                } else if (necroticCleave && layer == 0 && i > 0 && i < segs && i % 2 == 0) {
                    // Section 6.10 Vampiric Touch & Cryptguard Agent: 9 curved 3D bleached withering bone-claw talons!
                    Vec3 midOut = pOut0.add(pOut1).scale(0.5D);
                    Vec3 tang = pOut1.subtract(pOut0).normalize();
                    renderSkeletalRibSpike(matrix, vc, midOut, dir0, tang, planeNormal,
                            0.085F * reach * prof0, 0.46F * reach * prof0, fade * prof0);
                }
            }
        }
    }

    // =========================================================================
    // 7. VOLUMETRIC 3D VORTEX CONE (HOWLING GALE & SUPERSONIC RAMMING CONE)
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

        float fade = computeEnvelope(progress, 0.07F, 0.56F);
        float maxRange = inst.radiusOrScale;
        float coneTan = 0.56F; // ~29-degree half-angle (60-degree full cone)
        float coneSurge = Mth.clamp(progress / 0.26F, 0.0F, 1.0F);
        float activeReach = maxRange * (0.42F + 0.58F * (1.0F - (1.0F - coneSurge) * (1.0F - coneSurge)));

        // 1. Continuous Volumetric Hurricane Funnel Sheath (multi-band translucent cone wall!)
        int wallBands = 6;
        for (int b = 0; b < wallBands; b++) {
            float t0 = b / (float) wallBands;
            float t1 = (b + 1) / (float) wallBands;
            float d0 = 0.30F + t0 * activeReach;
            float d1 = 0.30F + t1 * activeReach;
            float r0 = d0 * coneTan * 0.94F;
            float r1 = d1 * coneTan * 0.94F;
            float a0 = fade * (0.36F - 0.24F * t0);
            float a1 = fade * (0.36F - 0.24F * t1);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(d0)), inst.start.add(fwd.scale(d1)),
                    right, up, r0, r1, 22,
                    (b % 2 == 0) ? pr : ar, (b % 2 == 0) ? pg : ag, (b % 2 == 0) ? pb : ab,
                    a0, Math.max(0.03F * fade, a1));
        }

        // 2. Spinning muzzle Wind-Rose seal at hand / origin
        Vec3 muzzle = inst.start.add(fwd.scale(0.32D));
        renderOrientedRing(matrix, vc, muzzle, right, up, 0.16F, 0.24F, 18, ar, ag, ab, 0.86F * fade);
        renderOrientedStarPolygon(matrix, vc, muzzle, right, up, 0.23F, 8, -gameTime * 0.24F, 1.0F, 1.0F, 1.0F, 0.78F * fade);

        // 3. 7 forward-surging sonic compression hoops expanding continuously along the cone
        int ringCount = 7;
        for (int rIdx = 0; rIdx < ringCount; rIdx++) {
            float ringT = ((rIdx / (float) ringCount) + gameTime * 0.065F) % 1.0F;
            float dist = 0.40F + ringT * activeReach;
            float ringRad = dist * coneTan;
            float ringFade = fade * Mth.sin(ringT * (float) Math.PI);
            Vec3 ringCenter = inst.start.add(fwd.scale(dist));

            renderOrientedRing(matrix, vc, ringCenter, right, up,
                    ringRad * 0.87F, ringRad, 24, (rIdx % 2 == 0) ? ar : pr,
                    (rIdx % 2 == 0) ? ag : pg, (rIdx % 2 == 0) ? ab : pb, 0.78F * ringFade);
        }

        // 4. 6 spiraling hurricane gale-blade ribbons + sweeping razor crescents at cone mouth
        int spirals = 6;
        int steps = 16;
        for (int s = 0; s < spirals; s++) {
            float baseAng = (float) (Math.PI * 2.0D * s / spirals) - gameTime * 0.44F + inst.seed;
            boolean ivory = (s % 2 == 0);
            Vec3 prev = null;
            for (int i = 0; i <= steps; i++) {
                float t = (float) i / steps;
                float dist = 0.32F + t * activeReach;
                float rad = dist * coneTan * 0.95F;
                float ang = baseAng + t * 4.2F;
                Vec3 pt = inst.start.add(fwd.scale(dist))
                        .add(right.scale(Mth.cos(ang) * rad))
                        .add(up.scale(Mth.sin(ang) * rad));
                if (prev != null) {
                    float segFade = fade * (1.0F - t * 0.38F);
                    renderCrossSegment(matrix, vc, prev, pt, ivory ? 0.055F : 0.075F,
                            ivory ? 1.0F : ar, ivory ? 1.0F : ag, ivory ? 1.0F : ab, 0.82F * segFade);
                }
                prev = pt;
            }
        }

        if (isAerodynamicMateria(inst.essence)) {
            Vec3 mouth = inst.start.add(fwd.scale(activeReach * 0.78D));
            float mouthRad = (float) (activeReach * 0.78D * coneTan);
            for (int c = 0; c < 4; c++) {
                float cAng = -gameTime * 0.42F + (float) (Math.PI * 0.5D * c);
                renderSweptWindCrescent(matrix, vc, mouth, right, up, mouthRad * 1.04F, cAng, 1.05F,
                        Math.max(0.14F, mouthRad * 0.14F), 1.0F, 1.0F, 1.0F, 0.85F * fade);
            }
        } else if (isHydroMateria(inst.essence)) {
            // Section 6.3 Deluge Torrent: 12-block foaming surge with deep-azure flood core & rolling whitewater surf crests!
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.35D)), inst.start.add(fwd.scale(activeReach * 0.92D)),
                    right, up, 0.14F, activeReach * 0.92F * coneTan * 0.68F, 20,
                    0.133F, 0.361F, 0.486F, 0.55F * fade, 0.14F * fade);

            for (int w = 0; w < 5; w++) {
                float wt = ((gameTime * 0.075F + (w / 5.0F)) % 1.0F);
                float dist = 0.60F + wt * activeReach;
                float surfRad = dist * coneTan * 1.02F;
                float surfFade = fade * Mth.sin(wt * (float) Math.PI);
                Vec3 surfPos = inst.start.add(fwd.scale(dist));
                renderOrientedRing(matrix, vc, surfPos, right, up,
                        surfRad * 0.82F, surfRad * 1.03F, 24, 0.90F, 0.99F, 1.00F, 0.84F * surfFade);
            }
        } else if (isBotanicalMateria(inst.essence)) {
            // Section 6.4 Toxic Pollen Cone: dense billowing cloud of golden-yellow botanical pollen & swirling spore clusters!
            renderBotanicalBlossom(matrix, vc, muzzle, right, up, 0.34F, 6, gameTime * 0.22F,
                    1.00F, 0.86F, 0.24F, 0.92F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.32D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.16F, activeReach * 0.96F * coneTan * 0.88F, 22,
                    1.00F, 0.85F, 0.22F, 0.56F * fade, 0.14F * fade);

            for (int p = 0; p < 14; p++) {
                float pt = ((gameTime * 0.065F + (p / 14.0F)) % 1.0F);
                float dist = 0.55F + pt * (activeReach - 0.55F);
                float maxOff = dist * coneTan * 0.82F;
                float rFrac = 0.25F + 0.72F * hashFloat(p, 41);
                float pAng = p * 2.39996F + gameTime * 0.18F;
                Vec3 sporePos = inst.start.add(fwd.scale(dist))
                        .add(right.scale(Mth.cos(pAng) * maxOff * rFrac))
                        .add(up.scale(Mth.sin(pAng) * maxOff * rFrac));
                float sFade = fade * Mth.sin(pt * (float) Math.PI);
                boolean golden = (p % 3 != 0);
                renderOctahedron(matrix, vc, sporePos, (0.08F + 0.06F * pt) * (0.75F + 0.25F * sFade),
                        golden ? 1.00F : 0.72F, golden ? 0.86F : 1.00F, golden ? 0.22F : 0.36F, 0.86F * sFade);
            }
        } else if (isTectonicMateria(inst.essence)) {
            // Section 6.5 Sand & Gravel Flurry: scouring sandstone-ochre dust funnel & 22 high-velocity tumbling 3D gravel/flint chunks!
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.32D)), inst.start.add(fwd.scale(activeReach * 0.98D)),
                    right, up, 0.18F, activeReach * 0.98F * coneTan * 0.90F, 16,
                    0.76F, 0.61F, 0.38F, 0.54F * fade, 0.16F * fade);

            for (int g = 0; g < 22; g++) {
                float gt = ((gameTime * 0.135F + (g / 22.0F)) % 1.0F);
                float dist = 0.45F + gt * (activeReach - 0.45F);
                float maxOff = dist * coneTan * 0.88F;
                float rFrac = 0.15F + 0.82F * hashFloat(g, 53);
                float gAng = g * 2.39996F - gameTime * 0.28F;
                Vec3 rockPos = inst.start.add(fwd.scale(dist))
                        .add(right.scale(Mth.cos(gAng) * maxOff * rFrac))
                        .add(up.scale(Mth.sin(gAng) * maxOff * rFrac));
                float rFade = fade * Mth.sin(gt * (float) Math.PI);
                int kind = g % 3;
                float cr = kind == 0 ? 0.467F : (kind == 1 ? 0.42F : 0.82F);
                float cg = kind == 0 ? 0.196F : (kind == 1 ? 0.40F : 0.66F);
                float cb = kind == 0 ? 0.078F : (kind == 1 ? 0.38F : 0.40F);
                renderOctahedron(matrix, vc, rockPos, (0.095F + 0.085F * gt) * (0.75F + 0.25F * rFade),
                        cr, cg, cb, 0.94F * rFade);
            }
        } else if (isGlacialMateria(inst.essence)) {
            // Section 6.6 Hoarfrost Flash-Freeze: sub-zero permafrost cone & 18 tumbling 3D 6-pointed snowflakes & ice needles!
            renderSnowflakeCrystal(matrix, vc, muzzle, right, up, 0.36F, gameTime * 0.20F, 0.94F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.32D)), inst.start.add(fwd.scale(activeReach * 0.98D)),
                    right, up, 0.16F, activeReach * 0.98F * coneTan * 0.88F, 18,
                    0.596F, 0.863F, 0.949F, 0.52F * fade, 0.14F * fade);

            for (int f = 0; f < 18; f++) {
                float ft = ((gameTime * 0.115F + (f / 18.0F)) % 1.0F);
                float dist = 0.45F + ft * (activeReach - 0.45F);
                float maxOff = dist * coneTan * 0.86F;
                float rFrac = 0.15F + 0.80F * hashFloat(f, 67);
                float fAng = f * 2.39996F + gameTime * 0.24F;
                Vec3 frostPos = inst.start.add(fwd.scale(dist))
                        .add(right.scale(Mth.cos(fAng) * maxOff * rFrac))
                        .add(up.scale(Mth.sin(fAng) * maxOff * rFrac));
                float fFade = fade * Mth.sin(ft * (float) Math.PI);
                if (f % 2 == 0) {
                    renderSnowflakeCrystal(matrix, vc, frostPos, right, up,
                            0.18F + 0.14F * ft, gameTime * 0.25F + f, 0.92F * fFade);
                } else {
                    renderFacetedIceSpire(matrix, vc, frostPos.subtract(fwd.scale(0.18D)), fwd, right, up,
                            0.065F, 0.38F, 5, fAng, fFade);
                }
            }
        } else if (isInfernalMateria(inst.essence)) {
            // Section 6.7 Combustion Backdraft: explosive muzzle backdraft blast ring, roaring hellfire cone & 18 molten cinder chunks!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.22F, 0.56F, 20,
                    1.00F, 0.820F, 0.361F, 0.95F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.30D)), inst.start.add(fwd.scale(activeReach * 0.98D)),
                    right, up, 0.20F, activeReach * 0.98F * coneTan * 0.92F, 18,
                    0.722F, 0.235F, 0.031F, 0.58F * fade, 0.16F * fade);

            for (int c = 0; c < 18; c++) {
                float ct = ((gameTime * 0.14F + (c / 18.0F)) % 1.0F);
                float dist = 0.42F + ct * (activeReach - 0.42F);
                float maxOff = dist * coneTan * 0.88F;
                float rFrac = 0.16F + 0.80F * hashFloat(c, 73);
                float cAng = c * 2.39996F - gameTime * 0.26F;
                Vec3 cinderPos = inst.start.add(fwd.scale(dist))
                        .add(right.scale(Mth.cos(cAng) * maxOff * rFrac))
                        .add(up.scale(Mth.sin(cAng) * maxOff * rFrac));
                float cFade = fade * Mth.sin(ct * (float) Math.PI);
                if (c % 2 == 0) {
                    renderHellfireFlameTongue(matrix, vc, cinderPos.subtract(fwd.scale(0.22D)), fwd, right, up,
                            0.11F + 0.06F * ct, 0.44F + 0.16F * ct, cFade);
                } else {
                    renderOctahedron(matrix, vc, cinderPos, (0.095F + 0.065F * ct) * (0.75F + 0.25F * cFade),
                            1.00F, 0.820F, 0.361F, 0.95F * cFade);
                }
            }
        } else if (isSolarRadiantMateria(inst.essence)) {
            // Section 6.8 Cleansing Mist Cone: warm golden-ivory sunbeam mist curtain, 6 soothing halo rings & 16 drifting 3D starlight motes!
            renderOrientedStarPolygon(matrix, vc, muzzle, right, up, 0.42F, 8, gameTime * 0.16F,
                    1.00F, 0.99F, 0.92F, 0.94F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.30D)), inst.start.add(fwd.scale(activeReach * 0.98D)),
                    right, up, 0.18F, activeReach * 0.98F * coneTan * 0.94F, 24,
                    1.00F, 0.886F, 0.471F, 0.48F * fade, 0.12F * fade);

            for (int h = 0; h < 6; h++) {
                float ht = ((gameTime * 0.075F + (h / 6.0F)) % 1.0F);
                float dist = 0.55F + ht * (activeReach - 0.55F);
                float hRad = dist * coneTan * 0.98F;
                float hFade = fade * Mth.sin(ht * (float) Math.PI);
                Vec3 hPos = inst.start.add(fwd.scale(dist));
                renderOrientedRing(matrix, vc, hPos, right, up, hRad * 0.85F, hRad, 26,
                        1.00F, 0.96F, 0.76F, 0.82F * hFade);
            }

            for (int m = 0; m < 16; m++) {
                float mt = ((gameTime * 0.085F + (m / 16.0F)) % 1.0F);
                float dist = 0.48F + mt * (activeReach - 0.48F);
                float maxOff = dist * coneTan * 0.85F;
                float rFrac = 0.18F + 0.78F * hashFloat(m, 83);
                float mAng = m * 2.39996F + gameTime * 0.14F;
                Vec3 motePos = inst.start.add(fwd.scale(dist))
                        .add(right.scale(Mth.cos(mAng) * maxOff * rFrac))
                        .add(up.scale(Mth.sin(mAng) * maxOff * rFrac));
                float mFade = fade * Mth.sin(mt * (float) Math.PI);
                renderOctahedron(matrix, vc, motePos, (0.085F + 0.055F * mt) * (0.75F + 0.25F * mFade),
                        1.00F, (m % 2 == 0) ? 0.995F : 0.886F, (m % 2 == 0) ? 0.95F : 0.471F, 0.92F * mFade);
            }
        } else if (isUmbralShadowMateria(inst.essence)) {
            // Section 6.9 Shadow Dart Fan: sweeping arc of 20 physical 3D #211A21 obsidian-violet dark needles fanning outward!
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.28D)), inst.start.add(fwd.scale(activeReach * 0.94D)),
                    right, up, 0.16F, activeReach * 0.94F * coneTan * 0.88F, 20,
                    0.129F, 0.102F, 0.129F, 0.62F * fade, 0.12F * fade);

            int dartCount = 20;
            for (int d = 0; d < dartCount; d++) {
                float u = (d / (float) (dartCount - 1)) - 0.5F;
                float pitchOff = (((d % 5) - 2.0F) * 0.11F);
                Vec3 dartDir = fwd.add(right.scale(u * coneTan * 2.15D)).add(up.scale(pitchOff)).normalize();
                Vec3 dRight = buildPerpendicularRight(dartDir);
                Vec3 dUp = dartDir.cross(dRight).normalize();

                float volleyCycle = ((gameTime * 0.12F + (d % 4) * 0.23F) % 1.0F);
                float dist = 0.35F + volleyCycle * (activeReach - 0.55F);
                float dFade = fade * Mth.sin(volleyCycle * (float) Math.PI);
                Vec3 dartTail = inst.start.add(dartDir.scale(dist));
                renderUmbralShadowNeedle(matrix, vc, dartTail, dartDir, dRight, dUp,
                        0.072F + 0.018F * volleyCycle, 0.68F + 0.22F * volleyCycle, dFade);
            }
        } else if (isNecroticUndeadMateria(inst.essence)) {
            // Section 6.10 Screaming Skull Swarm (Haunting Banshee Wave): conical spray of 14 screaming 3D ethereal skulls
            // rushing forward with #5EE088 banshee sonic shock-rings & ectoplasm trails!
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.28D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.16F, activeReach * 0.96F * coneTan * 0.90F, 20,
                    0.302F, 0.392F, 0.325F, 0.56F * fade, 0.14F * fade);

            int skullCount = 14;
            for (int k = 0; k < skullCount; k++) {
                float kt = ((gameTime * 0.115F + (k / (float) skullCount)) % 1.0F);
                float dist = 0.45F + kt * (activeReach - 0.45F);
                float maxOff = dist * coneTan * 0.85F;
                float rFrac = 0.18F + 0.78F * hashFloat(k, 91);
                float kAng = k * 2.39996F + gameTime * 0.20F;
                Vec3 skullPos = inst.start.add(fwd.scale(dist))
                        .add(right.scale(Mth.cos(kAng) * maxOff * rFrac))
                        .add(up.scale(Mth.sin(kAng) * maxOff * rFrac));
                float kFade = fade * Mth.sin(kt * (float) Math.PI);
                float skullR = 0.14F + 0.07F * kt;
                renderSpectralSkullHead(matrix, vc, skullPos, fwd, right, up, skullR, kFade);
                renderCrossSegment(matrix, vc, skullPos.subtract(fwd.scale(skullR * 0.7D)),
                        skullPos.subtract(fwd.scale(skullR * 3.8D)), skullR * 0.34F,
                        0.369F, 0.878F, 0.533F, 0.82F * kFade);
            }
        } else if (isAridDesertMateria(inst.essence)) {
            // Section 6.12 Sandblast Blunderbuss Wave: wide 60-degree cone of coarse abrasive #C2B28F sand & 24 sharp sandstone shrapnel wedges!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.20F, 0.52F, 20,
                    0.961F, 0.894F, 0.745F, 0.94F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.28D)), inst.start.add(fwd.scale(activeReach * 0.98D)),
                    right, up, 0.18F, activeReach * 0.98F * coneTan * 0.95F, 20,
                    0.761F, 0.698F, 0.561F, 0.62F * fade, 0.18F * fade);

            int shrapnelCount = 24;
            for (int s = 0; s < shrapnelCount; s++) {
                float st = ((gameTime * 0.15F + (s / (float) shrapnelCount)) % 1.0F);
                float dist = 0.42F + st * (activeReach - 0.42F);
                float maxOff = dist * coneTan * 0.90F;
                float rFrac = 0.14F + 0.84F * hashFloat(s, 83);
                float sAng = s * 2.39996F - gameTime * 0.32F;
                Vec3 shrapPos = inst.start.add(fwd.scale(dist))
                        .add(right.scale(Mth.cos(sAng) * maxOff * rFrac))
                        .add(up.scale(Mth.sin(sAng) * maxOff * rFrac));
                float sFade = fade * Mth.sin(st * (float) Math.PI);
                if (s % 2 == 0) {
                    renderFacetedStoneSpire(matrix, vc, shrapPos.subtract(fwd.scale(0.20D)), fwd, right, up,
                            0.068F, 0.36F, 4, sAng, sFade);
                } else {
                    renderOctahedron(matrix, vc, shrapPos, (0.085F + 0.065F * st) * (0.75F + 0.25F * sFade),
                            0.961F, 0.894F, 0.745F, 0.94F * sFade);
                }
            }
        } else if (isGalvanicLightningMateria(inst.essence)) {
            // Section 6.13 Forked Arc Fan (Tesla Shotgun): wide 60-degree spread of 14 branching #FFE600 electrical arcs!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.18F, 0.50F, 20,
                    1.00F, 0.902F, 0.00F, 0.96F * fade);
            int flicker = (int) (gameTime * 1.35F) + inst.seed * 11;
            int arcCount = 14;
            for (int a = 0; a < arcCount; a++) {
                float aAng = (float) (Math.PI * 2.0D * a / arcCount) + inst.seed * 0.2F;
                float rFrac = 0.22F + 0.76F * hashFloat(a, 97);
                Vec3 prev = muzzle;
                for (int s = 1; s <= 4; s++) {
                    float st = s / 4.0F;
                    float dist = st * activeReach * 0.96F;
                    float spread = dist * coneTan * rFrac;
                    float jR = (hashFloat(flicker + a * 7, s * 3) - 0.5F) * 0.42F;
                    float jU = (hashFloat(flicker + a * 7, s * 3 + 1) - 0.5F) * 0.42F;
                    Vec3 next = inst.start.add(fwd.scale(0.28D + dist))
                            .add(right.scale(Mth.cos(aAng) * spread + jR))
                            .add(up.scale(Mth.sin(aAng) * spread + jU));
                    renderCrossSegment(matrix, vc, prev, next, 0.072F * fade * (1.0F - st * 0.25F),
                            1.00F, 0.902F, 0.00F, 0.88F * fade);
                    renderCrossSegment(matrix, vc, prev, next, 0.030F * fade * (1.0F - st * 0.25F),
                            1.00F, 1.00F, 0.95F, 0.98F * fade);
                    prev = next;
                }
            }
        } else if (isMagmaCalderaMateria(inst.essence)) {
            // Section 6.14 Pyroclastic Surge: wide 60-degree cone of 20 burning 3D pumice boulders & #B83C08 molten lava splatter!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.18F, 0.52F, 20,
                    0.722F, 0.235F, 0.031F, 0.96F * fade);
            int pumiceCount = 20;
            for (int p = 0; p < pumiceCount; p++) {
                float pAng = (float) (Math.PI * 2.0D * p / pumiceCount) + inst.seed * 0.17F;
                float rFrac = 0.20F + 0.78F * hashFloat(p, 83);
                float st = ((progress * 1.45F + hashFloat(p, 41)) % 1.0F);
                float dist = st * activeReach * 0.96F;
                float spread = dist * coneTan * rFrac;
                Vec3 pos = inst.start.add(fwd.scale(0.28D + dist))
                        .add(right.scale(Mth.cos(pAng) * spread))
                        .add(up.scale(Mth.sin(pAng) * spread));
                float pFade = fade * (1.0F - st * 0.35F);
                boolean lavaGlob = (p % 2 == 0);
                renderOctahedron(matrix, vc, pos, (0.095F + 0.065F * st),
                        lavaGlob ? 0.722F : 0.165F,
                        lavaGlob ? 0.235F : 0.090F,
                        lavaGlob ? 0.031F : 0.071F,
                        0.94F * pFade);
                if (lavaGlob) {
                    renderOctahedron(matrix, vc, pos, (0.055F + 0.035F * st),
                            1.00F, 0.820F, 0.361F, 0.98F * pFade);
                }
            }
        } else if (isTempestStormMateria(inst.essence)) {
            // Section 6.15 Deluge Squall Cone: wide 60-degree torrential deluge of 22 driving #429BA3 rain streaks & gale wind-rings!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.20F, 0.56F, 22,
                    0.259F, 0.608F, 0.639F, 0.94F * fade);
            for (int rIdx = 0; rIdx < 3; rIdx++) {
                float rt = (rIdx + 1) / 3.0F;
                float dist = rt * activeReach * 0.92F;
                float ringRad = dist * coneTan;
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(0.28D + dist)), right, up,
                        ringRad * 0.88F, ringRad, 24, 0.847F, 0.980F, 1.000F, 0.78F * fade * (1.0F - rt * 0.25F));
            }
            for (int d = 0; d < 22; d++) {
                float dAng = (float) (Math.PI * 2.0D * d / 22.0D) + inst.seed * 0.15F;
                float rFrac = 0.16F + 0.80F * hashFloat(d, 71);
                float st = ((progress * 1.85F + hashFloat(d, 37)) % 1.0F);
                float dist0 = st * activeReach * 0.88F;
                float dist1 = Math.min(activeReach, dist0 + 0.68F);
                Vec3 p0 = inst.start.add(fwd.scale(0.28D + dist0))
                        .add(right.scale(Mth.cos(dAng) * dist0 * coneTan * rFrac))
                        .add(up.scale(Mth.sin(dAng) * dist0 * coneTan * rFrac));
                Vec3 p1 = inst.start.add(fwd.scale(0.28D + dist1))
                        .add(right.scale(Mth.cos(dAng) * dist1 * coneTan * rFrac))
                        .add(up.scale(Mth.sin(dAng) * dist1 * coneTan * rFrac));
                boolean foam = (d % 2 == 0);
                renderCrossSegment(matrix, vc, p0, p1, 0.036F * fade,
                        foam ? 0.847F : 0.259F, foam ? 0.980F : 0.608F, foam ? 1.000F : 0.639F, 0.90F * fade);
            }
        } else if (isPermafrostGlacialMateria(inst.essence)) {
            // Section 6.16 Blizzard Shrapnel Fan: 60-degree cone blasting 24 razor-sharp 3D #98DCF2 / #E0FFFF icicles & rime rings!
            renderSnowflakeCrystal(matrix, vc, muzzle, right, up, 0.56F, progress * 2.4F, 0.94F * fade);
            for (int rIdx = 0; rIdx < 3; rIdx++) {
                float rt = (rIdx + 1) / 3.0F;
                float dist = rt * activeReach * 0.90F;
                float ringRad = dist * coneTan;
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(0.28D + dist)), right, up,
                        ringRad * 0.88F, ringRad, 18, 0.596F, 0.863F, 0.949F, 0.76F * fade * (1.0F - rt * 0.25F));
            }
            for (int ic = 0; ic < 24; ic++) {
                float icAng = (float) (Math.PI * 2.0D * ic / 24.0D) + inst.seed * 0.19F;
                float rFrac = 0.14F + 0.82F * hashFloat(ic, 89);
                float st = ((progress * 1.90F + hashFloat(ic, 43)) % 1.0F);
                float dist0 = st * activeReach * 0.86F;
                float dist1 = Math.min(activeReach, dist0 + 0.76F);
                Vec3 p0 = inst.start.add(fwd.scale(0.28D + dist0))
                        .add(right.scale(Mth.cos(icAng) * dist0 * coneTan * rFrac))
                        .add(up.scale(Mth.sin(icAng) * dist0 * coneTan * rFrac));
                Vec3 p1 = inst.start.add(fwd.scale(0.28D + dist1))
                        .add(right.scale(Mth.cos(icAng) * dist1 * coneTan * rFrac))
                        .add(up.scale(Mth.sin(icAng) * dist1 * coneTan * rFrac));
                boolean diamond = (ic % 2 == 0);
                renderCrossSegment(matrix, vc, p0, p1, 0.042F * fade,
                        diamond ? 0.88F : 0.173F, diamond ? 1.00F : 0.510F, diamond ? 1.00F : 0.788F, 0.94F * fade);
                renderOctahedron(matrix, vc, p1, 0.068F * fade,
                        0.596F, 0.863F, 0.949F, 0.96F * fade);
            }
        } else if (isOvergrowthRampartMateria(inst.essence)) {
            // Section 6.17 Bramble Flurry & Phyto-Wave: 60-degree cone of 24 sharp wooden thorns, fertile earthen clods & emerald rings!
            renderBotanicalBlossom(matrix, vc, muzzle, right, up, 0.54F, 6, progress * 2.2F,
                    0.173F, 0.902F, 0.408F, 0.92F * fade);
            for (int rIdx = 0; rIdx < 3; rIdx++) {
                float rt = (rIdx + 1) / 3.0F;
                float dist = rt * activeReach * 0.90F;
                float ringRad = dist * coneTan;
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(0.28D + dist)), right, up,
                        ringRad * 0.88F, ringRad, 18, 0.102F, 0.349F, 0.137F, 0.78F * fade * (1.0F - rt * 0.25F));
            }
            for (int th = 0; th < 24; th++) {
                float thAng = (float) (Math.PI * 2.0D * th / 24.0D) + inst.seed * 0.17F;
                float rFrac = 0.15F + 0.82F * hashFloat(th, 97);
                float st = ((progress * 1.85F + hashFloat(th, 53)) % 1.0F);
                float dist0 = st * activeReach * 0.86F;
                float dist1 = Math.min(activeReach, dist0 + 0.68F);
                Vec3 p0 = inst.start.add(fwd.scale(0.28D + dist0))
                        .add(right.scale(Mth.cos(thAng) * dist0 * coneTan * rFrac))
                        .add(up.scale(Mth.sin(thAng) * dist0 * coneTan * rFrac));
                Vec3 p1 = inst.start.add(fwd.scale(0.28D + dist1))
                        .add(right.scale(Mth.cos(thAng) * dist1 * coneTan * rFrac))
                        .add(up.scale(Mth.sin(thAng) * dist1 * coneTan * rFrac));
                boolean woody = (th % 2 == 0);
                renderCrossSegment(matrix, vc, p0, p1, 0.044F * fade,
                        woody ? 0.467F : 0.102F, woody ? 0.196F : 0.349F, woody ? 0.078F : 0.137F, 0.94F * fade);
                renderOctahedron(matrix, vc, p1, 0.070F * fade,
                        woody ? 0.706F : 0.467F, woody ? 1.000F : 0.196F, woody ? 0.471F : 0.078F, 0.95F * fade);
            }
        } else if (isAbrasiveDustMateria(inst.essence)) {
            // Section 6.18 Abrasive Sandblasting Deluge: wide 60-degree cone of high-pressure #C2B28F / #F2ECE1 mineral grit!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.20F, 0.56F, 22,
                    0.949F, 0.925F, 0.882F, 0.94F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.28D)), inst.start.add(fwd.scale(activeReach * 0.98D)),
                    right, up, 0.18F, activeReach * 0.98F * coneTan * 0.96F, 22,
                    0.761F, 0.698F, 0.561F, 0.66F * fade, 0.18F * fade);

            for (int rIdx = 0; rIdx < 3; rIdx++) {
                float rt = (rIdx + 1) / 3.0F;
                float dist = rt * activeReach * 0.92F;
                float ringRad = dist * coneTan;
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(0.28D + dist)), right, up,
                        ringRad * 0.88F, ringRad, 22, 0.949F, 0.925F, 0.882F, 0.80F * fade * (1.0F - rt * 0.25F));
            }
            for (int g = 0; g < 26; g++) {
                float gAng = (float) (Math.PI * 2.0D * g / 26.0D) + inst.seed * 0.19F;
                float rFrac = 0.14F + 0.84F * hashFloat(g, 91);
                float st = ((progress * 1.95F + hashFloat(g, 47)) % 1.0F);
                float dist0 = st * activeReach * 0.88F;
                float dist1 = Math.min(activeReach, dist0 + 0.64F);
                Vec3 p0 = inst.start.add(fwd.scale(0.28D + dist0))
                        .add(right.scale(Mth.cos(gAng) * dist0 * coneTan * rFrac))
                        .add(up.scale(Mth.sin(gAng) * dist0 * coneTan * rFrac));
                Vec3 p1 = inst.start.add(fwd.scale(0.28D + dist1))
                        .add(right.scale(Mth.cos(gAng) * dist1 * coneTan * rFrac))
                        .add(up.scale(Mth.sin(gAng) * dist1 * coneTan * rFrac));
                boolean chalk = (g % 2 == 0);
                renderCrossSegment(matrix, vc, p0, p1, 0.040F * fade,
                        chalk ? 0.949F : 0.761F, chalk ? 0.925F : 0.698F, chalk ? 0.882F : 0.561F, 0.92F * fade);
                renderOctahedron(matrix, vc, p1, 0.068F * fade,
                        0.761F, 0.698F, 0.561F, 0.94F * fade);
            }
        } else if (isSymbioticSporeMateria(inst.essence)) {
            // Section 6.19 Billowing Windborne Spore Blast: wide 60-degree wind-borne #1A5923 / #A88654 fungal spore cone!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.20F, 0.54F, 22,
                    0.910F, 0.784F, 0.510F, 0.92F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.28D)), inst.start.add(fwd.scale(activeReach * 0.98D)),
                    right, up, 0.18F, activeReach * 0.98F * coneTan * 0.96F, 22,
                    0.102F, 0.349F, 0.137F, 0.64F * fade, 0.18F * fade);

            for (int rIdx = 0; rIdx < 3; rIdx++) {
                float rt = (rIdx + 1) / 3.0F;
                float dist = rt * activeReach * 0.90F;
                float ringRad = dist * coneTan;
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(0.28D + dist)), right, up,
                        ringRad * 0.86F, ringRad, 22, 0.659F, 0.525F, 0.329F, 0.80F * fade * (1.0F - rt * 0.22F));
            }
            for (int p = 0; p < 22; p++) {
                float pAng = (float) (Math.PI * 2.0D * p / 22.0D) + inst.seed * 0.17F;
                float rFrac = 0.15F + 0.82F * hashFloat(p, 83);
                float st = ((progress * 1.65F + hashFloat(p, 41)) % 1.0F);
                float dist = st * activeReach * 0.90F;
                Vec3 puffPos = inst.start.add(fwd.scale(0.28D + dist))
                        .add(right.scale(Mth.cos(pAng) * dist * coneTan * rFrac))
                        .add(up.scale(Mth.sin(pAng) * dist * coneTan * rFrac));
                boolean gold = (p % 2 == 0);
                renderOctahedron(matrix, vc, puffPos, 0.095F * fade,
                        gold ? 0.910F : 0.173F, gold ? 0.784F : 0.902F, gold ? 0.510F : 0.408F, 0.94F * fade);
            }
        } else if (isBorealTaigaMateria(inst.essence)) {
            // Section 6.20 Frozen Sap Splatter Cone: shotgun-like spread of sticky sub-zero pine sap globs & #98DCF2 ice splinters!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.20F, 0.54F, 22,
                    0.596F, 0.863F, 0.949F, 0.92F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.26D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.18F, activeReach * 0.96F * coneTan * 0.95F, 22,
                    0.102F, 0.349F, 0.137F, 0.66F * fade, 0.20F * fade);
            for (int p = 0; p < 24; p++) {
                float pAng = (float) (Math.PI * 2.0D * p / 24.0D) + inst.seed * 0.19F;
                float rFrac = 0.12F + 0.84F * hashFloat(p, 97);
                float st = ((progress * 1.85F + hashFloat(p, 53)) % 1.0F);
                float dist = st * activeReach * 0.92F;
                Vec3 sapPos = inst.start.add(fwd.scale(0.26D + dist))
                        .add(right.scale(Mth.cos(pAng) * dist * coneTan * rFrac))
                        .add(up.scale(Mth.sin(pAng) * dist * coneTan * rFrac));
                Vec3 tailPos = sapPos.subtract(fwd.scale(0.45D));
                boolean frostShard = (p % 2 == 0);
                renderCrossSegment(matrix, vc, tailPos, sapPos, 0.042F * fade,
                        frostShard ? 0.596F : 0.860F,
                        frostShard ? 0.863F : 0.640F,
                        frostShard ? 0.949F : 0.240F,
                        0.92F * fade);
                renderOctahedron(matrix, vc, sapPos, 0.088F * fade,
                        frostShard ? 0.596F : 0.102F,
                        frostShard ? 0.863F : 0.349F,
                        frostShard ? 0.949F : 0.137F,
                        0.96F * fade);
            }
        } else if (isMiracleOasisMateria(inst.essence)) {
            // Section 6.21 Desert Greening Mist: gentle warm botanical mist (#1A5923 / #C2B28F) & sparkling #38C6F4 droplets!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.20F, 0.54F, 22,
                    0.761F, 0.698F, 0.561F, 0.90F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.26D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.20F, activeReach * 0.96F * coneTan * 0.96F, 24,
                    0.102F, 0.349F, 0.137F, 0.58F * fade, 0.16F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.32D)), inst.start.add(fwd.scale(activeReach * 0.88D)),
                    right, up, 0.16F, activeReach * 0.88F * coneTan * 0.84F, 22,
                    0.220F, 0.776F, 0.957F, 0.44F * fade, 0.12F * fade);
            for (int p = 0; p < 22; p++) {
                float pAng = (float) (Math.PI * 2.0D * p / 22.0D) + inst.seed * 0.17F;
                float rFrac = 0.14F + 0.82F * hashFloat(p, 89);
                float st = ((progress * 1.45F + hashFloat(p, 47)) % 1.0F);
                float dist = st * activeReach * 0.90F;
                Vec3 mistPos = inst.start.add(fwd.scale(0.28D + dist))
                        .add(right.scale(Mth.cos(pAng) * dist * coneTan * rFrac))
                        .add(up.scale(Mth.sin(pAng) * dist * coneTan * rFrac));
                int kind = p % 3;
                renderOctahedron(matrix, vc, mistPos, 0.088F * fade,
                        kind == 0 ? 0.871F : (kind == 1 ? 0.220F : 0.133F),
                        kind == 0 ? 0.788F : (kind == 1 ? 0.776F : 0.471F),
                        kind == 0 ? 0.604F : (kind == 1 ? 0.957F : 0.188F),
                        0.92F * fade);
            }
        } else if (isCellularVitaeMateria(inst.essence)) {
            // Section 6.22 Cellular Rejuvenation Spray (#FF6B9D / #FFD1E3): wide 60° sparkling rose biological mist!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.20F, 0.56F, 22,
                    1.000F, 0.820F, 0.890F, 0.92F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.26D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.20F, activeReach * 0.96F * coneTan * 0.96F, 24,
                    1.000F, 0.420F, 0.616F, 0.58F * fade, 0.18F * fade);
            for (int p = 0; p < 22; p++) {
                float pAng = (float) (Math.PI * 2.0D * p / 22.0D) + inst.seed * 0.17F;
                float rFrac = 0.14F + 0.82F * hashFloat(p, 97);
                float st = ((progress * 1.55F + hashFloat(p, 43)) % 1.0F);
                float dist = st * activeReach * 0.90F;
                Vec3 mistPos = inst.start.add(fwd.scale(0.28D + dist))
                        .add(right.scale(Mth.cos(pAng) * dist * coneTan * rFrac))
                        .add(up.scale(Mth.sin(pAng) * dist * coneTan * rFrac));
                boolean pearl = (p % 2 == 0);
                renderOctahedron(matrix, vc, mistPos, 0.090F * fade,
                        1.000F, pearl ? 0.820F : 0.420F, pearl ? 0.890F : 0.616F, 0.95F * fade);
            }
        } else if (isSanguineBloodMateria(inst.essence)) {
            // Section 6.23 Sanguine Shrapnel Deluge (#8A0303 / #FF384C): wide 60° cone of high-velocity
            // crystallized blood needles & crimson mist!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.20F, 0.56F, 22,
                    0.541F, 0.012F, 0.012F, 0.94F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.26D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.20F, activeReach * 0.96F * coneTan * 0.96F, 24,
                    0.541F, 0.012F, 0.012F, 0.60F * fade, 0.18F * fade);
            for (int n = 0; n < 24; n++) {
                float nAng = (float) (Math.PI * 2.0D * n / 24.0D) + inst.seed * 0.19F;
                float rFrac = 0.12F + 0.84F * hashFloat(n, 91);
                float st = ((progress * 1.85F + hashFloat(n, 47)) % 1.0F);
                float dist = st * activeReach * 0.92F;
                Vec3 needleTail = inst.start.add(fwd.scale(0.24D + dist))
                        .add(right.scale(Mth.cos(nAng) * dist * coneTan * rFrac))
                        .add(up.scale(Mth.sin(nAng) * dist * coneTan * rFrac));
                Vec3 needleTip = needleTail.add(fwd.scale(0.55D));
                boolean arterial = (n % 3 == 0);
                renderCrossSegment(matrix, vc, needleTail, needleTip, 0.042F * fade,
                        arterial ? 1.000F : 0.541F,
                        arterial ? 0.220F : 0.012F,
                        arterial ? 0.298F : 0.012F,
                        0.96F * fade);
            }
        } else if (isFossilAmberMateria(inst.essence)) {
            // Section 6.25 Molten Resin Deluge (#FF6B9D / #773214 / #F29B2E): wide 60° cone of molten golden resin
            // & 24 flying amber grit globs!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.20F, 0.56F, 6,
                    0.467F, 0.196F, 0.078F, 0.94F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.26D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.20F, activeReach * 0.96F * coneTan * 0.96F, 24,
                    0.949F, 0.608F, 0.180F, 0.56F * fade, 0.18F * fade);
            for (int g = 0; g < 24; g++) {
                float gAng = (float) (Math.PI * 2.0D * g / 24.0D) + inst.seed * 0.17F;
                float rFrac = 0.14F + 0.82F * hashFloat(g, 97);
                float st = ((progress * 1.55F + hashFloat(g, 53)) % 1.0F);
                float dist = st * activeReach * 0.90F;
                Vec3 globPos = inst.start.add(fwd.scale(0.26D + dist))
                        .add(right.scale(Mth.cos(gAng) * dist * coneTan * rFrac))
                        .add(up.scale(Mth.sin(gAng) * dist * coneTan * rFrac));
                boolean rose = (g % 3 == 0);
                renderOctahedron(matrix, vc, globPos, 0.105F * fade,
                        rose ? 1.000F : 0.467F,
                        rose ? 0.420F : 0.196F,
                        rose ? 0.616F : 0.078F,
                        0.95F * fade);
            }
        } else if (isParadoxEclipseMateria(inst.essence)) {
            // Section 6.26 Coronal Flare Deluge (#FFEA78 / #211A21): wide 60° cone of paradox shadow-flame &
            // blazing golden coronal flare streaks!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.0F, 0.24F, 18,
                    0.085F, 0.065F, 0.090F, 0.95F * fade);
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.24F, 0.56F, 24,
                    1.000F, 0.918F, 0.471F, 0.96F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.26D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.20F, activeReach * 0.96F * coneTan * 0.96F, 24,
                    0.129F, 0.102F, 0.129F, 0.66F * fade, 0.20F * fade);
            for (int f = 0; f < 24; f++) {
                float fAng = (float) (Math.PI * 2.0D * f / 24.0D) + inst.seed * 0.17F;
                float rFrac = 0.14F + 0.84F * hashFloat(f, 101);
                float st = ((progress * 1.90F + hashFloat(f, 59)) % 1.0F);
                float dist = st * activeReach * 0.92F;
                Vec3 tail = inst.start.add(fwd.scale(0.24D + dist))
                        .add(right.scale(Mth.cos(fAng) * dist * coneTan * rFrac))
                        .add(up.scale(Mth.sin(fAng) * dist * coneTan * rFrac));
                Vec3 tip = tail.add(fwd.scale(0.62D));
                boolean corona = (f % 2 == 0);
                renderCrossSegment(matrix, vc, tail, tip, (corona ? 0.045F : 0.055F) * fade,
                        corona ? 1.000F : 0.129F,
                        corona ? 0.918F : 0.102F,
                        corona ? 0.471F : 0.129F,
                        0.96F * fade);
            }
        } else if (isCadaverousBlightMateria(inst.essence)) {
            // Section 6.27 Virulent Necro-Spore Flurry (#4D6453 / #34CC48): wide 60° cone of rotting bone splinters & toxic spores!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.18F, 0.54F, 18,
                    0.302F, 0.392F, 0.325F, 0.94F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.26D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.20F, activeReach * 0.96F * coneTan * 0.96F, 24,
                    0.204F, 0.800F, 0.282F, 0.56F * fade, 0.18F * fade);
            for (int b = 0; b < 24; b++) {
                float bAng = (float) (Math.PI * 2.0D * b / 24.0D) + inst.seed * 0.17F;
                float rFrac = 0.14F + 0.84F * hashFloat(b, 107);
                float st = ((progress * 1.80F + hashFloat(b, 61)) % 1.0F);
                float dist = st * activeReach * 0.92F;
                Vec3 tail = inst.start.add(fwd.scale(0.24D + dist))
                        .add(right.scale(Mth.cos(bAng) * dist * coneTan * rFrac))
                        .add(up.scale(Mth.sin(bAng) * dist * coneTan * rFrac));
                Vec3 tip = tail.add(fwd.scale(0.54D));
                boolean bone = (b % 2 == 0);
                renderCrossSegment(matrix, vc, tail, tip, 0.044F * fade,
                        bone ? 0.902F : 0.204F,
                        bone ? 0.875F : 0.800F,
                        bone ? 0.812F : 0.282F,
                        0.95F * fade);
            }
        } else if (isCosmicAstralMateria(inst.essence)) {
            // Section 6.28 Pulsar Arc Spray (Cosmic Tesla Shotgun, #41304E / #FFE600): 60° branching cosmic lightning & void distortion!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.18F, 0.56F, 22,
                    1.000F, 0.902F, 0.000F, 0.96F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.26D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.20F, activeReach * 0.96F * coneTan * 0.96F, 24,
                    0.255F, 0.188F, 0.306F, 0.62F * fade, 0.18F * fade);
            for (int a = 0; a < 22; a++) {
                float aAng = (float) (Math.PI * 2.0D * a / 22.0D) + inst.seed * 0.19F;
                float rFrac = 0.14F + 0.84F * hashFloat(a, 113);
                float st = ((progress * 1.95F + hashFloat(a, 67)) % 1.0F);
                float dist0 = st * activeReach * 0.88F;
                float dist1 = Math.min(activeReach, dist0 + 0.78F);
                Vec3 p0 = inst.start.add(fwd.scale(0.24D + dist0))
                        .add(right.scale(Mth.cos(aAng) * dist0 * coneTan * rFrac))
                        .add(up.scale(Mth.sin(aAng) * dist0 * coneTan * rFrac));
                Vec3 p1 = inst.start.add(fwd.scale(0.24D + dist1))
                        .add(right.scale(Mth.cos(aAng + 0.18F) * dist1 * coneTan * rFrac))
                        .add(up.scale(Mth.sin(aAng + 0.18F) * dist1 * coneTan * rFrac));
                boolean gold = (a % 3 != 0);
                renderCrossSegment(matrix, vc, p0, p1, 0.046F * fade,
                        gold ? 1.000F : 0.255F,
                        gold ? 0.902F : 0.188F,
                        gold ? 0.000F : 0.306F,
                        0.96F * fade);
            }
        } else if (isPhantasmalSoulfireMateria(inst.essence)) {
            // Section 6.29 Phantasmal Soul-Torch Wave (#1DF0E5 / #0D6469): 60° roaring cyan soulflames!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.18F, 0.56F, 20,
                    0.114F, 0.941F, 0.898F, 0.96F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.26D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.20F, activeReach * 0.96F * coneTan * 0.96F, 24,
                    0.114F, 0.941F, 0.898F, 0.64F * fade, 0.18F * fade);
            for (int f = 0; f < 24; f++) {
                float fAng = (float) (Math.PI * 2.0D * f / 24.0D) + inst.seed * 0.17F;
                float rFrac = 0.14F + 0.84F * hashFloat(f, 119);
                float st = ((progress * 1.90F + hashFloat(f, 71)) % 1.0F);
                float dist = st * activeReach * 0.90F;
                Vec3 tail = inst.start.add(fwd.scale(0.24D + dist))
                        .add(right.scale(Mth.cos(fAng) * dist * coneTan * rFrac))
                        .add(up.scale(Mth.sin(fAng) * dist * coneTan * rFrac));
                Vec3 tip = tail.add(fwd.scale(0.62D)).add(up.scale(0.10D));
                boolean cyan = (f % 3 != 0);
                renderCrossSegment(matrix, vc, tail, tip, 0.048F * fade,
                        cyan ? 0.114F : 0.051F,
                        cyan ? 0.941F : 0.392F,
                        cyan ? 0.898F : 0.412F,
                        0.95F * fade);
            }
        } else if (isScaldingVaporMateria(inst.essence)) {
            // Section 6.30 Scalding Steam-Blast Deluge (#E0F7FA / #30A9B0): 60° superheated steam & boiling droplets!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.18F, 0.56F, 22,
                    0.878F, 0.969F, 0.980F, 0.95F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.26D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.20F, activeReach * 0.96F * coneTan * 0.96F, 24,
                    0.878F, 0.969F, 0.980F, 0.58F * fade, 0.16F * fade);
            for (int d = 0; d < 24; d++) {
                float dAng = (float) (Math.PI * 2.0D * d / 24.0D) + inst.seed * 0.19F;
                float rFrac = 0.14F + 0.84F * hashFloat(d, 131);
                float st = ((progress * 1.92F + hashFloat(d, 79)) % 1.0F);
                float dist = st * activeReach * 0.90F;
                Vec3 tail = inst.start.add(fwd.scale(0.24D + dist))
                        .add(right.scale(Mth.cos(dAng) * dist * coneTan * rFrac))
                        .add(up.scale(Mth.sin(dAng) * dist * coneTan * rFrac));
                Vec3 tip = tail.add(fwd.scale(0.64D));
                boolean steam = (d % 2 == 0);
                renderCrossSegment(matrix, vc, tail, tip, 0.046F * fade,
                        steam ? 0.878F : 0.188F,
                        steam ? 0.969F : 0.663F,
                        steam ? 0.980F : 0.690F,
                        0.94F * fade);
            }
        } else if (isAntimatterNullRMateria(inst.essence)) {
            // Section 6.31 Blinding Antimatter Flare Cone (#FFFFFF / #41304E): 60° blinding white-violet antimatter radiation spray!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.18F, 0.58F, 24,
                    1.000F, 1.000F, 1.000F, 0.98F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.24D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.20F, activeReach * 0.96F * coneTan * 0.96F, 26,
                    0.255F, 0.188F, 0.306F, 0.64F * fade, 0.18F * fade);
            for (int d = 0; d < 26; d++) {
                float dAng = (float) (Math.PI * 2.0D * d / 26.0D) + inst.seed * 0.17F;
                float rFrac = 0.12F + 0.86F * hashFloat(d, 149);
                float st = ((progress * 2.05F + hashFloat(d, 83)) % 1.0F);
                float dist = st * activeReach * 0.92F;
                Vec3 tail = inst.start.add(fwd.scale(0.22D + dist))
                        .add(right.scale(Mth.cos(dAng) * dist * coneTan * rFrac))
                        .add(up.scale(Mth.sin(dAng) * dist * coneTan * rFrac));
                Vec3 tip = tail.add(fwd.scale(0.72D));
                boolean white = (d % 2 == 0);
                renderCrossSegment(matrix, vc, tail, tip, 0.048F * fade,
                        white ? 1.000F : 0.255F,
                        white ? 1.000F : 0.188F,
                        white ? 1.000F : 0.306F,
                        0.97F * fade);
            }
        } else if (isAbyssalNullUMateria(inst.essence)) {
            // Section 6.32 Void-Umbral Disintegration Wave (#110B17 / #41304E): 60° pitch-black antimatter needles & light-devouring void mist!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.18F, 0.58F, 24,
                    0.067F, 0.043F, 0.090F, 0.98F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.24D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.20F, activeReach * 0.96F * coneTan * 0.96F, 26,
                    0.255F, 0.188F, 0.306F, 0.68F * fade, 0.20F * fade);
            for (int d = 0; d < 26; d++) {
                float dAng = (float) (Math.PI * 2.0D * d / 26.0D) + inst.seed * 0.17F;
                float rFrac = 0.12F + 0.86F * hashFloat(d, 157);
                float st = ((progress * 2.05F + hashFloat(d, 89)) % 1.0F);
                float dist = st * activeReach * 0.92F;
                Vec3 tail = inst.start.add(fwd.scale(0.22D + dist))
                        .add(right.scale(Mth.cos(dAng) * dist * coneTan * rFrac))
                        .add(up.scale(Mth.sin(dAng) * dist * coneTan * rFrac));
                Vec3 tip = tail.add(fwd.scale(0.74D));
                boolean dark = (d % 2 == 0);
                renderCrossSegment(matrix, vc, tail, tip, 0.048F * fade,
                        dark ? 0.067F : 0.255F,
                        dark ? 0.043F : 0.188F,
                        dark ? 0.090F : 0.306F,
                        0.98F * fade);
            }
        } else if (isDaybreakDawnMateria(inst.essence)) {
            // Section 6.33 Solar Flare Breath (#FFEA78 / #A88654): 60° fan of dawn light & superheated gale air!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.18F, 0.58F, 24,
                    1.000F, 0.918F, 0.471F, 0.98F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.24D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.20F, activeReach * 0.96F * coneTan * 0.96F, 26,
                    0.659F, 0.525F, 0.329F, 0.62F * fade, 0.18F * fade);
            for (int d = 0; d < 26; d++) {
                float dAng = (float) (Math.PI * 2.0D * d / 26.0D) + inst.seed * 0.17F;
                float rFrac = 0.12F + 0.86F * hashFloat(d, 167);
                float st = ((progress * 2.10F + hashFloat(d, 97)) % 1.0F);
                float dist = st * activeReach * 0.92F;
                Vec3 tail = inst.start.add(fwd.scale(0.22D + dist))
                        .add(right.scale(Mth.cos(dAng) * dist * coneTan * rFrac))
                        .add(up.scale(Mth.sin(dAng) * dist * coneTan * rFrac));
                Vec3 tip = tail.add(fwd.scale(0.76D));
                boolean sunrise = (d % 2 == 0);
                renderCrossSegment(matrix, vc, tail, tip, 0.048F * fade,
                        sunrise ? 1.000F : 0.659F,
                        sunrise ? 0.918F : 0.525F,
                        sunrise ? 0.471F : 0.329F,
                        0.97F * fade);
            }
        } else if (isHadopelagicAbyssMateria(inst.essence)) {
            // Section 6.34 Abyssal Ink-Blast Deluge (#211A21 / #225C7C): 60° cone of pressurized black ocean water & cephalopod ink!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.18F, 0.60F, 24,
                    0.129F, 0.102F, 0.129F, 0.98F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.24D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.20F, activeReach * 0.96F * coneTan * 0.96F, 26,
                    0.133F, 0.361F, 0.486F, 0.66F * fade, 0.20F * fade);
            for (int d = 0; d < 26; d++) {
                float dAng = (float) (Math.PI * 2.0D * d / 26.0D) + inst.seed * 0.17F;
                float rFrac = 0.12F + 0.86F * hashFloat(d, 173);
                float st = ((progress * 2.05F + hashFloat(d, 101)) % 1.0F);
                float dist = st * activeReach * 0.92F;
                Vec3 tail = inst.start.add(fwd.scale(0.22D + dist))
                        .add(right.scale(Mth.cos(dAng) * dist * coneTan * rFrac))
                        .add(up.scale(Mth.sin(dAng) * dist * coneTan * rFrac));
                Vec3 tip = tail.add(fwd.scale(0.72D));
                boolean ink = (d % 2 == 0);
                renderCrossSegment(matrix, vc, tail, tip, 0.052F * fade,
                        ink ? 0.129F : 0.133F,
                        ink ? 0.102F : 0.361F,
                        ink ? 0.129F : 0.486F,
                        0.98F * fade);
            }
        } else if (isGildedAegisMateria(inst.essence)) {
            // Section 6.35 Gilded Granite Shrapnel Deluge (#FFEA78 / #773214): 60° cone of jagged quartz shards & granite shrapnel!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.18F, 0.60F, 8,
                    1.000F, 0.918F, 0.471F, 0.98F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.24D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.20F, activeReach * 0.96F * coneTan * 0.96F, 12,
                    0.467F, 0.196F, 0.078F, 0.60F * fade, 0.18F * fade);
            for (int d = 0; d < 26; d++) {
                float dAng = (float) (Math.PI * 2.0D * d / 26.0D) + inst.seed * 0.17F;
                float rFrac = 0.12F + 0.86F * hashFloat(d, 179);
                float st = ((progress * 2.15F + hashFloat(d, 107)) % 1.0F);
                float dist = st * activeReach * 0.92F;
                Vec3 tail = inst.start.add(fwd.scale(0.22D + dist))
                        .add(right.scale(Mth.cos(dAng) * dist * coneTan * rFrac))
                        .add(up.scale(Mth.sin(dAng) * dist * coneTan * rFrac));
                Vec3 tip = tail.add(fwd.scale(0.74D));
                boolean gold = (d % 2 == 0);
                renderCrossSegment(matrix, vc, tail, tip, 0.056F * fade,
                        gold ? 1.000F : 0.467F,
                        gold ? 0.918F : 0.196F,
                        gold ? 0.471F : 0.078F,
                        0.98F * fade);
            }
        } else if (isBorealisAuroraMateria(inst.essence)) {
            // Section 6.36 Borealis Prismatic Fan Deluge (#FFEA78 / #98DCF2): 60° cone of sparkling sub-zero light ribbons & ice needles!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.18F, 0.58F, 6,
                    0.596F, 0.863F, 0.949F, 0.98F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.24D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.20F, activeReach * 0.96F * coneTan * 0.96F, 26,
                    1.000F, 0.918F, 0.471F, 0.62F * fade, 0.18F * fade);
            for (int d = 0; d < 26; d++) {
                float dAng = (float) (Math.PI * 2.0D * d / 26.0D) + inst.seed * 0.17F;
                float rFrac = 0.12F + 0.86F * hashFloat(d, 191);
                float st = ((progress * 2.15F + hashFloat(d, 113)) % 1.0F);
                float dist = st * activeReach * 0.92F;
                Vec3 tail = inst.start.add(fwd.scale(0.22D + dist))
                        .add(right.scale(Mth.cos(dAng) * dist * coneTan * rFrac))
                        .add(up.scale(Mth.sin(dAng) * dist * coneTan * rFrac));
                Vec3 tip = tail.add(fwd.scale(0.78D));
                boolean gold = (d % 2 == 0);
                renderCrossSegment(matrix, vc, tail, tip, 0.050F * fade,
                        gold ? 1.000F : 0.596F,
                        gold ? 0.918F : 0.863F,
                        gold ? 0.471F : 0.949F,
                        0.98F * fade);
            }
        } else if (isPhantasmMirageMateria(inst.essence)) {
            // Section 6.37 Hallucinatory Mirage Shimmer Deluge (#FFEA78 / #C2B28F): 60° cone of shimmering heat-haze waves & golden sand!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.18F, 0.60F, 8,
                    1.000F, 0.918F, 0.471F, 0.96F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.24D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.20F, activeReach * 0.96F * coneTan * 0.96F, 24,
                    0.761F, 0.698F, 0.561F, 0.58F * fade, 0.16F * fade);
            for (int d = 0; d < 26; d++) {
                float dAng = (float) (Math.PI * 2.0D * d / 26.0D) + inst.seed * 0.19F;
                float rFrac = 0.12F + 0.86F * hashFloat(d, 197);
                float st = ((progress * 2.10F + hashFloat(d, 127)) % 1.0F);
                float dist = st * activeReach * 0.92F;
                double shimmer = 0.08D * Mth.sin(gameTime * 0.30F + d * 0.9F);
                Vec3 tail = inst.start.add(fwd.scale(0.22D + dist))
                        .add(right.scale(Mth.cos(dAng) * dist * coneTan * rFrac + shimmer))
                        .add(up.scale(Mth.sin(dAng) * dist * coneTan * rFrac));
                Vec3 tip = tail.add(fwd.scale(0.76D));
                boolean gold = (d % 2 == 0);
                renderCrossSegment(matrix, vc, tail, tip, 0.052F * fade,
                        gold ? 1.000F : 0.761F,
                        gold ? 0.918F : 0.698F,
                        gold ? 0.471F : 0.561F,
                        0.96F * fade);
            }
        } else if (isIncorporealWraithMateria(inst.essence)) {
            // Section 6.38 Banshee Wail Ectoplasm Deluge (#41304E / #4D6453): 60° cone of ultrasonic spectral wail rings & corrosive void ectoplasm!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.18F, 0.62F, 18,
                    0.302F, 0.392F, 0.325F, 0.98F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.24D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.20F, activeReach * 0.96F * coneTan * 0.96F, 24,
                    0.255F, 0.188F, 0.306F, 0.64F * fade, 0.18F * fade);
            for (int r = 1; r <= 4; r++) {
                float rt = ((progress * 1.85F + r * 0.25F) % 1.0F);
                float rDist = rt * activeReach * 0.94F;
                float rRad = Math.max(0.25F, rDist * coneTan * 0.92F);
                renderOrientedRing(matrix, vc, inst.start.add(fwd.scale(0.24D + rDist)), right, up,
                        rRad * 0.84F, rRad, 22, 0.302F, 0.392F, 0.325F, 0.88F * (1.0F - rt * 0.65F) * fade);
            }
            for (int d = 0; d < 24; d++) {
                float dAng = (float) (Math.PI * 2.0D * d / 24.0D) + inst.seed * 0.17F;
                float rFrac = 0.12F + 0.86F * hashFloat(d, 211);
                float st = ((progress * 2.15F + hashFloat(d, 137)) % 1.0F);
                float dist = st * activeReach * 0.92F;
                Vec3 tail = inst.start.add(fwd.scale(0.22D + dist))
                        .add(right.scale(Mth.cos(dAng) * dist * coneTan * rFrac))
                        .add(up.scale(Mth.sin(dAng) * dist * coneTan * rFrac));
                Vec3 tip = tail.add(fwd.scale(0.76D));
                boolean moss = (d % 2 == 0);
                renderCrossSegment(matrix, vc, tail, tip, 0.054F * fade,
                        moss ? 0.302F : 0.255F,
                        moss ? 0.392F : 0.188F,
                        moss ? 0.325F : 0.306F,
                        0.95F * fade);
            }
        } else if (isSepulchralBarrowMateria(inst.essence)) {
            // Section 6.39 Ossuary Bone-Grit & Tomb-Dust Deluge (#773214 / #4D6453 + #E6DFCF bone): 60° cone of petrified bone shrapnel & grave dirt!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.18F, 0.62F, 8,
                    0.467F, 0.196F, 0.078F, 0.98F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.24D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.20F, activeReach * 0.96F * coneTan * 0.96F, 16,
                    0.302F, 0.392F, 0.325F, 0.62F * fade, 0.18F * fade);
            for (int d = 0; d < 26; d++) {
                float dAng = (float) (Math.PI * 2.0D * d / 26.0D) + inst.seed * 0.19F;
                float rFrac = 0.12F + 0.86F * hashFloat(d, 223);
                float st = ((progress * 2.15F + hashFloat(d, 149)) % 1.0F);
                float dist = st * activeReach * 0.92F;
                Vec3 tail = inst.start.add(fwd.scale(0.22D + dist))
                        .add(right.scale(Mth.cos(dAng) * dist * coneTan * rFrac))
                        .add(up.scale(Mth.sin(dAng) * dist * coneTan * rFrac));
                Vec3 tip = tail.add(fwd.scale(0.76D));
                boolean bone = (d % 2 == 0);
                renderCrossSegment(matrix, vc, tail, tip, 0.058F * fade,
                        bone ? 0.902F : 0.467F,
                        bone ? 0.875F : 0.196F,
                        bone ? 0.812F : 0.078F,
                        0.97F * fade);
            }
        } else if (isCrematingPyreMateria(inst.essence)) {
            // Section 6.40 Combustion Hearth Flame-Thrower (#773214 / #FF4500): roaring flamethrower cone & glowing coal cinders!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.18F, 0.64F, 12,
                    1.000F, 0.271F, 0.000F, 0.98F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.24D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.22F, activeReach * 0.96F * coneTan * 0.98F, 18,
                    1.000F, 0.271F, 0.000F, 0.68F * fade, 0.22F * fade);
            for (int d = 0; d < 26; d++) {
                float dAng = (float) (Math.PI * 2.0D * d / 26.0D) + inst.seed * 0.21F;
                float rFrac = 0.12F + 0.86F * hashFloat(d, 227);
                float st = ((progress * 2.40F + hashFloat(d, 151)) % 1.0F);
                float dist = st * activeReach * 0.92F;
                Vec3 tail = inst.start.add(fwd.scale(0.22D + dist))
                        .add(right.scale(Mth.cos(dAng) * dist * coneTan * rFrac))
                        .add(up.scale(Mth.sin(dAng) * dist * coneTan * rFrac));
                Vec3 tip = tail.add(fwd.scale(0.82D));
                boolean flame = (d % 2 == 0);
                renderCrossSegment(matrix, vc, tail, tip, 0.060F * fade,
                        flame ? 1.000F : 0.467F,
                        flame ? 0.271F : 0.196F,
                        flame ? 0.000F : 0.078F,
                        0.98F * fade);
            }
        } else if (isTwilightPenumbraMateria(inst.essence)) {
            // Section 6.41 Penumbral Corona-Twilight Fan Deluge (#FFEA78 / #281E3C): 60° cone of solar coronal rays & deep indigo twilight ribbons!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.18F, 0.64F, 18,
                    1.000F, 0.918F, 0.471F, 0.98F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.24D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.20F, activeReach * 0.96F * coneTan * 0.96F, 20,
                    0.157F, 0.118F, 0.235F, 0.72F * fade, 0.20F * fade);
            for (int d = 0; d < 26; d++) {
                float dAng = (float) (Math.PI * 2.0D * d / 26.0D) + inst.seed * 0.23F;
                float rFrac = 0.12F + 0.86F * hashFloat(d, 233);
                float st = ((progress * 2.50F + hashFloat(d, 157)) % 1.0F);
                float dist = st * activeReach * 0.92F;
                Vec3 tail = inst.start.add(fwd.scale(0.22D + dist))
                        .add(right.scale(Mth.cos(dAng) * dist * coneTan * rFrac))
                        .add(up.scale(Mth.sin(dAng) * dist * coneTan * rFrac));
                Vec3 tip = tail.add(fwd.scale(0.86D));
                boolean corona = (d % 2 == 0);
                renderCrossSegment(matrix, vc, tail, tip, 0.056F * fade,
                        corona ? 1.000F : 0.157F,
                        corona ? 0.918F : 0.118F,
                        corona ? 0.471F : 0.235F,
                        0.98F * fade);
            }
        } else if (isHoarfrostRimeMateria(inst.essence)) {
            // Section 6.42 Sub-Zero Blizzarding Rime Cone (#6395B1 / #DCF0FF): 60° blizzard cone of razor-sharp needle-ice!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.18F, 0.64F, 6,
                    0.863F, 0.941F, 1.000F, 0.98F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.24D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.20F, activeReach * 0.96F * coneTan * 0.96F, 18,
                    0.388F, 0.584F, 0.694F, 0.66F * fade, 0.20F * fade);
            for (int d = 0; d < 26; d++) {
                float dAng = (float) (Math.PI * 2.0D * d / 26.0D) + inst.seed * 0.21F;
                float rFrac = 0.12F + 0.86F * hashFloat(d, 239);
                float st = ((progress * 2.55F + hashFloat(d, 163)) % 1.0F);
                float dist = st * activeReach * 0.92F;
                Vec3 tail = inst.start.add(fwd.scale(0.22D + dist))
                        .add(right.scale(Mth.cos(dAng) * dist * coneTan * rFrac))
                        .add(up.scale(Mth.sin(dAng) * dist * coneTan * rFrac));
                Vec3 tip = tail.add(fwd.scale(0.88D));
                boolean white = (d % 2 == 0);
                renderCrossSegment(matrix, vc, tail, tip, 0.056F * fade,
                        white ? 0.863F : 0.388F,
                        white ? 0.941F : 0.584F,
                        white ? 1.000F : 0.694F,
                        0.98F * fade);
            }
        } else if (isVernalSpringMateria(inst.essence)) {
            // Section 6.43 Vernal Rejuvenation Torrent Cone (#FF6B9D / #64C8FF): 60° spray of pressurized aquifer springwater & cherry petals!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.18F, 0.62F, 20,
                    0.392F, 0.784F, 1.000F, 0.96F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.24D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.20F, activeReach * 0.96F * coneTan * 0.96F, 24,
                    0.392F, 0.784F, 1.000F, 0.62F * fade, 0.18F * fade);
            for (int p = 0; p < 24; p++) {
                float pAng = (float) (Math.PI * 2.0D * p / 24.0D) + inst.seed * 0.19F;
                float rFrac = 0.14F + 0.84F * hashFloat(p, 251);
                float st = ((progress * 2.35F + hashFloat(p, 173)) % 1.0F);
                float dist = st * activeReach * 0.92F;
                Vec3 tail = inst.start.add(fwd.scale(0.22D + dist))
                        .add(right.scale(Mth.cos(pAng) * dist * coneTan * rFrac))
                        .add(up.scale(Mth.sin(pAng) * dist * coneTan * rFrac));
                Vec3 tip = tail.add(fwd.scale(0.76D));
                boolean blossom = (p % 2 == 0);
                renderCrossSegment(matrix, vc, tail, tip, 0.056F * fade,
                        blossom ? 1.000F : 0.392F,
                        blossom ? 0.420F : 0.784F,
                        blossom ? 0.616F : 1.000F,
                        0.96F * fade);
            }
        } else if (isGalvanicStaticMateria(inst.essence)) {
            // Section 6.44 High-Voltage Static Arc-Fan Deluge (#FFE600 / #878DA2): 60° cone of high-voltage static sparks & electrified desert dust!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.18F, 0.64F, 8,
                    1.000F, 0.902F, 0.000F, 0.98F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.24D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.20F, activeReach * 0.96F * coneTan * 0.96F, 16,
                    0.529F, 0.553F, 0.635F, 0.58F * fade, 0.16F * fade);
            for (int a = 0; a < 24; a++) {
                float aAng = (float) (Math.PI * 2.0D * a / 24.0D) + inst.seed * 0.23F;
                float rFrac = 0.14F + 0.84F * hashFloat(a, 257);
                float st = ((progress * 2.65F + hashFloat(a, 179)) % 1.0F);
                float dist = st * activeReach * 0.92F;
                Vec3 tail = inst.start.add(fwd.scale(0.22D + dist))
                        .add(right.scale(Mth.cos(aAng) * dist * coneTan * rFrac))
                        .add(up.scale(Mth.sin(aAng) * dist * coneTan * rFrac));
                Vec3 tip = tail.add(fwd.scale(0.82D));
                boolean yellow = (a % 2 == 0);
                renderCrossSegment(matrix, vc, tail, tip, 0.058F * fade,
                        yellow ? 1.000F : 0.529F,
                        yellow ? 0.902F : 0.553F,
                        yellow ? 0.000F : 0.635F,
                        0.98F * fade);
            }
        } else if (isPrimevalSylvanMateria(inst.essence)) {
            // Section 6.45 Canopy Solar-Flare Breath (#1A5923 / #773214 / #FFEA78): 60° cone of sun-dappled green solar fire & living embers!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.18F, 0.64F, 18,
                    1.000F, 0.918F, 0.471F, 0.98F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.24D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.20F, activeReach * 0.96F * coneTan * 0.96F, 22,
                    0.102F, 0.349F, 0.137F, 0.62F * fade, 0.18F * fade);
            for (int e = 0; e < 24; e++) {
                float eAng = (float) (Math.PI * 2.0D * e / 24.0D) + inst.seed * 0.21F;
                float rFrac = 0.14F + 0.84F * hashFloat(e, 263);
                float st = ((progress * 2.40F + hashFloat(e, 181)) % 1.0F);
                float dist = st * activeReach * 0.92F;
                Vec3 tail = inst.start.add(fwd.scale(0.22D + dist))
                        .add(right.scale(Mth.cos(eAng) * dist * coneTan * rFrac))
                        .add(up.scale(Mth.sin(eAng) * dist * coneTan * rFrac));
                Vec3 tip = tail.add(fwd.scale(0.78D));
                boolean gold = (e % 2 == 0);
                renderCrossSegment(matrix, vc, tail, tip, 0.058F * fade,
                        gold ? 1.000F : 0.102F,
                        gold ? 0.918F : 0.349F,
                        gold ? 0.471F : 0.137F,
                        0.97F * fade);
            }
        } else if (isPestilentMiasmaMateria(inst.essence)) {
            // Section 6.46 Choking Bog-Gas Fan (#1A5923 / #4D6453 / #225C7C): thick smothering fan of decomposing swamp gas & toxic acid!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.18F, 0.66F, 18,
                    0.133F, 0.361F, 0.486F, 0.95F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.24D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.22F, activeReach * 0.96F * coneTan * 0.98F, 22,
                    0.302F, 0.392F, 0.325F, 0.64F * fade, 0.22F * fade);
            for (int g = 0; g < 24; g++) {
                float gAng = (float) (Math.PI * 2.0D * g / 24.0D) + inst.seed * 0.23F;
                float rFrac = 0.14F + 0.84F * hashFloat(g, 271);
                float st = ((progress * 2.20F + hashFloat(g, 193)) % 1.0F);
                float dist = st * activeReach * 0.92F;
                Vec3 tail = inst.start.add(fwd.scale(0.22D + dist))
                        .add(right.scale(Mth.cos(gAng) * dist * coneTan * rFrac))
                        .add(up.scale(Mth.sin(gAng) * dist * coneTan * rFrac));
                Vec3 tip = tail.add(fwd.scale(0.74D));
                boolean green = (g % 2 == 0);
                renderCrossSegment(matrix, vc, tail, tip, 0.062F * fade,
                        green ? 0.102F : 0.133F,
                        green ? 0.349F : 0.361F,
                        green ? 0.137F : 0.486F,
                        0.96F * fade);
            }
        } else if (isPrimordialGenesisMateria(inst.essence)) {
            // Section 6.47 Dawn-Tide Rejuvenation Deluge (#FF6B9D / #FFEA78 / #225C7C): pressurized holy dawn water & radiant vitae spray!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.18F, 0.66F, 20,
                    1.000F, 0.918F, 0.471F, 0.98F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.24D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.22F, activeReach * 0.96F * coneTan * 0.98F, 22,
                    0.133F, 0.361F, 0.486F, 0.62F * fade, 0.20F * fade);
            for (int d = 0; d < 24; d++) {
                float dAng = (float) (Math.PI * 2.0D * d / 24.0D) + inst.seed * 0.23F;
                float rFrac = 0.14F + 0.84F * hashFloat(d, 277);
                float st = ((progress * 2.35F + hashFloat(d, 197)) % 1.0F);
                float dist = st * activeReach * 0.92F;
                Vec3 tail = inst.start.add(fwd.scale(0.22D + dist))
                        .add(right.scale(Mth.cos(dAng) * dist * coneTan * rFrac))
                        .add(up.scale(Mth.sin(dAng) * dist * coneTan * rFrac));
                Vec3 tip = tail.add(fwd.scale(0.78D));
                boolean pink = (d % 2 == 0);
                renderCrossSegment(matrix, vc, tail, tip, 0.060F * fade,
                        1.000F,
                        pink ? 0.420F : 0.918F,
                        pink ? 0.616F : 0.471F,
                        0.98F * fade);
            }
        } else if (isStygianOblivionMateria(inst.essence)) {
            // Section 6.48 Crematorium Shadow-Breath Cone (#B83C08 / #211A21 / #4D6453): roaring black stygian flame & bone-ash embers!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.20F, 0.68F, 18,
                    0.722F, 0.235F, 0.031F, 0.98F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.24D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.24F, activeReach * 0.96F * coneTan * 0.98F, 20,
                    0.129F, 0.102F, 0.129F, 0.82F * fade, 0.24F * fade);
            for (int e = 0; e < 24; e++) {
                float eAng = (float) (Math.PI * 2.0D * e / 24.0D) + inst.seed * 0.25F;
                float rFrac = 0.14F + 0.84F * hashFloat(e, 283);
                float st = ((progress * 2.45F + hashFloat(e, 199)) % 1.0F);
                float dist = st * activeReach * 0.92F;
                Vec3 tail = inst.start.add(fwd.scale(0.22D + dist))
                        .add(right.scale(Mth.cos(eAng) * dist * coneTan * rFrac))
                        .add(up.scale(Mth.sin(eAng) * dist * coneTan * rFrac));
                Vec3 tip = tail.add(fwd.scale(0.84D));
                boolean rust = (e % 2 == 0);
                renderCrossSegment(matrix, vc, tail, tip, 0.064F * fade,
                        rust ? 0.722F : 0.302F,
                        rust ? 0.235F : 0.392F,
                        rust ? 0.031F : 0.325F,
                        0.98F * fade);
            }
        } else if (isQuintessentialAetherMateria(inst.essence)) {
            // Section 6.50 Quintessential Firmament Deluge (#A88654 / #773214 / #225C7C / #B83C08): pressurized ether & quad-elemental ribbons!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.22F, 0.72F, 22,
                    0.659F, 0.525F, 0.329F, 0.98F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.24D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.24F, activeReach * 0.96F * coneTan * 0.98F, 24,
                    0.133F, 0.361F, 0.486F, 0.64F * fade, 0.22F * fade);
            float[][] quadRgb = {
                    {0.659F, 0.525F, 0.329F},
                    {0.467F, 0.196F, 0.078F},
                    {0.133F, 0.361F, 0.486F},
                    {0.722F, 0.235F, 0.031F}
            };
            for (int r = 0; r < 24; r++) {
                float rAng = (float) (Math.PI * 2.0D * r / 24.0D) + inst.seed * 0.23F;
                float rFrac = 0.14F + 0.84F * hashFloat(r, 293);
                float st = ((progress * 2.50F + hashFloat(r, 211)) % 1.0F);
                float dist = st * activeReach * 0.92F;
                float[] c = quadRgb[r & 3];
                Vec3 tail = inst.start.add(fwd.scale(0.22D + dist))
                        .add(right.scale(Mth.cos(rAng) * dist * coneTan * rFrac))
                        .add(up.scale(Mth.sin(rAng) * dist * coneTan * rFrac));
                Vec3 tip = tail.add(fwd.scale(0.88D));
                renderCrossSegment(matrix, vc, tail, tip, 0.066F * fade, c[0], c[1], c[2], 0.98F * fade);
            }
        } else if (isAbyssalEntropicMateria(inst.essence)) {
            // Section 6.51 Entropic Oblivion-Flame Deluge (#41304E / #211A21 / #4D6453 / #8A0303): void fire, bone-grit & boiling sanguine mist!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.22F, 0.72F, 20,
                    0.541F, 0.012F, 0.012F, 0.98F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.24D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.24F, activeReach * 0.96F * coneTan * 0.98F, 22,
                    0.129F, 0.102F, 0.129F, 0.84F * fade, 0.26F * fade);
            float[][] entRgb = {
                    {0.255F, 0.188F, 0.306F},
                    {0.129F, 0.102F, 0.129F},
                    {0.302F, 0.392F, 0.325F},
                    {0.541F, 0.012F, 0.012F}
            };
            for (int r = 0; r < 24; r++) {
                float rAng = (float) (Math.PI * 2.0D * r / 24.0D) + inst.seed * 0.25F;
                float rFrac = 0.14F + 0.84F * hashFloat(r, 307);
                float st = ((progress * 2.50F + hashFloat(r, 223)) % 1.0F);
                float dist = st * activeReach * 0.92F;
                float[] c = entRgb[r & 3];
                Vec3 tail = inst.start.add(fwd.scale(0.22D + dist))
                        .add(right.scale(Mth.cos(rAng) * dist * coneTan * rFrac))
                        .add(up.scale(Mth.sin(rAng) * dist * coneTan * rFrac));
                Vec3 tip = tail.add(fwd.scale(0.88D));
                renderCrossSegment(matrix, vc, tail, tip, 0.066F * fade, c[0], c[1], c[2], 0.98F * fade);
            }
        } else if (isHolyCelestialMateria(inst.essence)) {
            // Section 6.52 Holy Dawn-Deluge Cone (#FFEA78 / #FF6B9D / #1A5923 / #A88654): blinding white-gold starlight & pranic solar winds!
            renderOrientedRing(matrix, vc, muzzle, right, up, 0.22F, 0.74F, 22,
                    1.000F, 0.918F, 0.471F, 0.99F * fade);
            renderOrientedFrustumBand(matrix, vc,
                    inst.start.add(fwd.scale(0.24D)), inst.start.add(fwd.scale(activeReach * 0.96D)),
                    right, up, 0.24F, activeReach * 0.96F * coneTan * 0.98F, 24,
                    1.000F, 0.420F, 0.616F, 0.72F * fade, 0.24F * fade);
            float[][] celRgb = {
                    {1.000F, 0.918F, 0.471F},
                    {1.000F, 0.420F, 0.616F},
                    {0.102F, 0.349F, 0.137F},
                    {0.659F, 0.525F, 0.329F}
            };
            for (int r = 0; r < 24; r++) {
                float rAng = (float) (Math.PI * 2.0D * r / 24.0D) + inst.seed * 0.25F;
                float rFrac = 0.14F + 0.84F * hashFloat(r, 311);
                float st = ((progress * 2.50F + hashFloat(r, 227)) % 1.0F);
                float dist = st * activeReach * 0.92F;
                float[] c = celRgb[r & 3];
                Vec3 tail = inst.start.add(fwd.scale(0.22D + dist))
                        .add(right.scale(Mth.cos(rAng) * dist * coneTan * rFrac))
                        .add(up.scale(Mth.sin(rAng) * dist * coneTan * rFrac));
                Vec3 tip = tail.add(fwd.scale(0.90D));
                renderCrossSegment(matrix, vc, tail, tip, 0.066F * fade, c[0], c[1], c[2], 0.98F * fade);
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
        float inFrac = Math.min(0.10F, 10.0F / Math.max(1, inst.maxTicks));
        float outFrac = Math.max(0.74F, 1.0F - (20.0F / Math.max(1, inst.maxTicks)));
        float fade = computeEnvelope(progress, inFrac, outFrac);
        float radius = inst.radiusOrScale;

        Vec3 right = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 forward = new Vec3(0.0D, 0.0D, 1.0D);

        // Section 6.3 Monsoon Cloudburst: dense tier-scaled 3D downpour (overhead dark-azure monsoon canopy, angled rain sheets & puddle ripples)!
        if (isHydroMateria(inst.essence)) {
            double canopyH = resolveTierScaledSkyfallHeight(ground, inst.tier, 8.8D);
            Vec3 canopyCenter = ground.add(0.0D, canopyH, 0.0D);
            float mFade = fade;

            // 1. Swirling dark-azure (#225C7C) & slate storm-cloud canopy discs overhead
            renderOrientedRing(matrix, vc, canopyCenter, right, forward, 0.0F, radius * 1.04F, 32,
                    0.11F, 0.30F, 0.44F, 0.56F * mFade);
            renderOrientedRing(matrix, vc, canopyCenter.add(0.0D, -0.22D, 0.0D), right, forward, radius * 0.32F, radius * 0.92F, 28,
                    pr * 0.75F, pg * 0.75F, pb * 0.85F, 0.42F * mFade);
            renderLowerHemisphereShell(matrix, vc, canopyCenter, radius * 0.96F, 3, 24,
                    0.133F, 0.361F, 0.486F, 0.28F * mFade);

            // 2. Tier-scaled animated 3D torrential rain-sheet streaks falling continuously across the full storm cylinder!
            int rainStreaks = 24 + inst.tier * 3;
            for (int i = 0; i < rainStreaks; i++) {
                float rFrac = (float) Math.sqrt((i + 0.5D) / rainStreaks) * 0.94F;
                float theta = i * 2.39996F + inst.seed * 0.17F; // Golden-angle uniform disc distribution
                double rx = Mth.cos(theta) * radius * rFrac;
                double rz = Mth.sin(theta) * radius * rFrac;

                float fallCycle = ((gameTime * 0.14F + hashFloat(i, 77)) % 1.0F);
                double topY = canopyH * (1.0D - fallCycle * 0.78D);
                double botY = Math.max(0.05D, topY - 2.35D);
                Vec3 sTop = ground.add(rx + 0.22D, topY, rz);
                Vec3 sBot = ground.add(rx, botY, rz);
                boolean foamStreak = (i % 3 == 0);
                renderCrossSegment(matrix, vc, sTop, sBot, foamStreak ? 0.038F : 0.048F,
                        foamStreak ? 0.88F : pr, foamStreak ? 0.98F : pg, 1.00F, 0.74F * mFade);

                // Ground splash ripple when rain sheet nears the ground
                float splashCycle = ((gameTime * 0.09F + hashFloat(i, 131)) % 1.0F);
                if (i % 3 == 0) {
                    float pRad = (0.18F + 0.75F * splashCycle);
                    float pAlpha = (1.0F - splashCycle) * 0.72F * mFade;
                    renderOrientedRing(matrix, vc, ground.add(rx, 0.04D, rz), right, forward,
                            pRad * 0.74F, pRad, 14, 0.88F, 0.98F, 1.00F, pAlpha);
                }
            }

            // Outer drenched monsoon perimeter ring on ground
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    radius * 0.95F, radius * 1.01F, 36, pr, pg, pb, 0.52F * mFade);
            return;
        }

        // Section 6.4 Spore Tempest: overhead bioluminescent mycelial spore-canopy, drifting fungal spore streams & poison moss patches!
        if (isBotanicalMateria(inst.essence)) {
            double canopyH = resolveTierScaledSkyfallHeight(ground, inst.tier, 8.5D);
            Vec3 canopyCenter = ground.add(0.0D, canopyH, 0.0D);

            // 1. Domed bioluminescent forest-emerald & golden-ochre mycelial mushroom canopy overhead
            renderHemisphereShell(matrix, vc, canopyCenter.add(0.0D, -0.35D, 0.0D), radius * 0.98F, 4, 24,
                    0.102F, 0.349F, 0.137F, 0.44F * fade);
            renderOrientedRing(matrix, vc, canopyCenter.add(0.0D, -0.30D, 0.0D), right, forward,
                    radius * 0.20F, radius * 1.02F, 30, 0.78F, 0.86F, 0.22F, 0.36F * fade);
            // 12 radial glowing fungal gill ribs underneath the spore canopy
            for (int g = 0; g < 12; g++) {
                float gAng = gameTime * 0.018F + (float) (Math.PI * 2.0D * g / 12.0D);
                Vec3 gIn = canopyCenter.add(Mth.cos(gAng) * radius * 0.16D, -0.18D, Mth.sin(gAng) * radius * 0.16D);
                Vec3 gOut = canopyCenter.add(Mth.cos(gAng) * radius * 0.96D, -0.34D, Mth.sin(gAng) * radius * 0.96D);
                renderCrossSegment(matrix, vc, gIn, gOut, 0.065F, 0.76F, 1.00F, 0.34F, 0.62F * fade);
            }

            // 2. 26 drifting helical streams of toxic yellow-green fungal spores descending to carpet the floor
            int sporeStreams = 26;
            for (int i = 0; i < sporeStreams; i++) {
                float rFrac = (float) Math.sqrt((i + 0.5D) / sporeStreams) * 0.92F;
                float theta = i * 2.39996F + inst.seed * 0.19F;
                float fallCycle = ((gameTime * 0.065F + hashFloat(i, 91)) % 1.0F);
                double swayX = Math.sin(gameTime * 0.09D + i) * 0.38D;
                double swayZ = Math.cos(gameTime * 0.08D + i * 1.3D) * 0.38D;
                double sx = Mth.cos(theta) * radius * rFrac + swayX;
                double sz = Mth.sin(theta) * radius * rFrac + swayZ;
                double sy = Math.max(0.12D, (canopyH - 0.4D) * (1.0D - fallCycle));

                Vec3 sporePos = ground.add(sx, sy, sz);
                float sAlpha = fade * Mth.sin(fallCycle * (float) Math.PI);
                boolean goldenSpore = (i % 2 == 0);
                renderOctahedron(matrix, vc, sporePos, 0.11F + 0.04F * (i % 3),
                        goldenSpore ? 0.96F : 0.66F, goldenSpore ? 0.88F : 1.00F, goldenSpore ? 0.24F : 0.32F,
                        0.84F * sAlpha);

                if (i % 3 == 0) {
                    float mossPulse = 0.70F + 0.30F * Mth.sin(gameTime * 0.12F + i);
                    renderOrientedRing(matrix, vc, ground.add(Mth.cos(theta) * radius * rFrac, 0.04D, Mth.sin(theta) * radius * rFrac),
                            right, forward, 0.18F, 0.52F * mossPulse, 14,
                            0.48F, 0.92F, 0.26F, 0.52F * fade);
                }
            }

            // Outer poison-moss spore ring on the ground
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    radius * 0.94F, radius * 1.01F, 36, 0.102F, 0.349F, 0.137F, 0.72F * fade);
            return;
        }

        // Section 6.5 Meteorite Shrapnel Barrage: cracked stone cavern-rift overhead raining plummeting 3D stone meteorite shards!
        if (isTectonicMateria(inst.essence)) {
            double riftH = resolveTierScaledSkyfallHeight(ground, inst.tier, 9.0D);
            Vec3 riftCenter = ground.add(0.0D, riftH, 0.0D);
            Vec3 downAxis = new Vec3(0.0D, -1.0D, 0.0D);

            // 1. Fractured octagonal granite & terracotta cavern-rift disc overhead laced with amber tectonic fissures
            renderOrientedRing(matrix, vc, riftCenter, right, forward, 0.0F, radius * 1.02F, 8,
                    0.353F, 0.322F, 0.298F, 0.62F * fade);
            renderOrientedRing(matrix, vc, riftCenter.add(0.0D, -0.10D, 0.0D), right, forward,
                    radius * 0.88F, radius * 1.04F, 8, 0.467F, 0.196F, 0.078F, 0.84F * fade);
            for (int f = 0; f < 8; f++) {
                float fAng = gameTime * 0.015F + (float) (Math.PI * 2.0D * f / 8.0D);
                Vec3 fIn = riftCenter.add(Mth.cos(fAng) * radius * 0.12D, -0.12D, Mth.sin(fAng) * radius * 0.12D);
                Vec3 fOut = riftCenter.add(Mth.cos(fAng) * radius * 0.94D, -0.12D, Mth.sin(fAng) * radius * 0.94D);
                renderCrossSegment(matrix, vc, fIn, fOut, 0.075F, 1.00F, 0.76F, 0.47F, 0.86F * fade);
            }

            // 2. Tier-scaled heavy 3D stone meteorite shards plummeting straight down & shattering into amber ground shock-rings!
            int meteorCount = 12 + inst.tier * 2;
            for (int i = 0; i < meteorCount; i++) {
                float rFrac = (float) Math.sqrt((i + 0.5D) / meteorCount) * 0.90F;
                float theta = i * 2.39996F + inst.seed * 0.23F;
                double mx = Mth.cos(theta) * radius * rFrac;
                double mz = Mth.sin(theta) * radius * rFrac;

                float fallCycle = ((gameTime * 0.095F + hashFloat(i, 107)) % 1.0F);
                double my = riftH * (1.0D - fallCycle * fallCycle); // Accelerating gravitational plummet!
                Vec3 mBase = ground.add(mx, Math.max(0.45D, my + 0.65D), mz);
                float mAlpha = fade * (fallCycle < 0.94F ? 1.0F : (1.0F - (fallCycle - 0.94F) / 0.06F));

                renderFacetedStoneSpire(matrix, vc, mBase, downAxis, right, forward,
                        0.20F + 0.04F * (i % 3), 0.78F + 0.18F * (i % 2), 5, gameTime * 0.18F + i, mAlpha);
                renderCrossSegment(matrix, vc, mBase, mBase.add(0.0D, 1.65D, 0.0D), 0.055F,
                        1.00F, 0.76F, 0.47F, 0.78F * mAlpha);

                if (fallCycle > 0.72F) {
                    float impT = (fallCycle - 0.72F) / 0.28F;
                    float impR = 0.25F + 1.15F * impT;
                    renderOrientedRing(matrix, vc, ground.add(mx, 0.05D, mz), right, forward,
                            impR * 0.74F, impR, 8, 1.00F, 0.76F, 0.47F, (1.0F - impT) * 0.88F * fade);
                }
            }

            // Octagonal tectonic bombardment perimeter ring on the ground
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    radius * 0.94F, radius * 1.02F, 16, 0.467F, 0.196F, 0.078F, 0.82F * fade);
            return;
        }

        // Section 6.6 Colossal Glacial Monolith & Section 6.16 Glacial Monolith Cataclysm:
        // summons a SINGLE gigantic 3D spire of Blue Ice that hovers/descends over 2.0s (40t),
        // crashes into the impact zone, and radiates sub-zero frost shockwaves around the permanent ice monument (100% cave-viable)!
        if (isGlacialMateria(inst.essence) || isPermafrostGlacialMateria(inst.essence)) {
            double riftH = resolveTierScaledSkyfallHeight(ground, inst.tier, 10.5D);
            Vec3 riftCenter = ground.add(0.0D, riftH, 0.0D);
            Vec3 downAxis = new Vec3(0.0D, -1.0D, 0.0D);
            Vec3 upAxis = new Vec3(0.0D, 1.0D, 0.0D);
            float elapsedTicks = progress * Math.max(1, inst.maxTicks);

            // 1. Overhead giant 6-pointed Crystalline Snowflake Sky-Rift Portal
            renderOrientedRing(matrix, vc, riftCenter, right, forward, radius * 0.72F, radius * 1.04F, 24,
                    0.173F, 0.510F, 0.788F, 0.76F * fade);
            renderSnowflakeCrystal(matrix, vc, riftCenter.add(0.0D, -0.08D, 0.0D), right, forward,
                    radius * 1.02F, gameTime * 0.035F + inst.seed, 0.90F * fade);

            // Ground warning / frost-crater snowflake target sigil
            renderSnowflakeCrystal(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    radius * 0.96F, -gameTime * 0.04F, 0.84F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.045D, 0.0D), right, forward,
                    radius * 0.92F, radius * 1.02F, 24, 0.596F, 0.863F, 0.949F, 0.80F * fade);

            float tierSpireMax = 4.5F + inst.tier * 0.95F;
            if (elapsedTicks < 40.0F) {
                // Phase 1 (0..40 ticks = 2.0 seconds): Materializes high overhead (0..14t), then accelerates catastrophically downward (14..40t)!
                float formT = Mth.clamp(elapsedTicks / 14.0F, 0.0F, 1.0F);
                float fallT = elapsedTicks <= 14.0F ? 0.0F : Mth.clamp((elapsedTicks - 14.0F) / 26.0F, 0.0F, 1.0F);
                float accelFall = fallT * fallT * fallT; // Dramatic heavy gravitational drop!
                float maxSpireLen = Math.min(tierSpireMax, (float) (riftH * 0.52D));
                double startTipY = Math.max(0.65D, riftH - maxSpireLen * 0.82D);
                double tipY = Mth.lerp(accelFall, startTipY, 0.10D);
                float monolithScale = 0.45F + 0.55F * formT;
                float spireLen = maxSpireLen * monolithScale;
                float spireRad = Math.max(0.88F, radius * 0.24F) * monolithScale;

                Vec3 spireBase = ground.add(0.0D, tipY + spireLen, 0.0D);
                renderFacetedIceSpire(matrix, vc, spireBase, downAxis, right, forward,
                        spireRad, spireLen, 6, gameTime * 0.06F, fade * 0.72F);
                // Crown of 6 secondary blue-ice fins flanking the colossal plummeting pseudoblock monolith!
                for (int f = 0; f < 6; f++) {
                    float fAng = gameTime * 0.06F + (float) (Math.PI * 2.0D * f / 6.0D);
                    Vec3 fRad = new Vec3(Mth.cos(fAng), 0.0D, Mth.sin(fAng));
                    Vec3 fTang = new Vec3(-Mth.sin(fAng), 0.0D, Mth.cos(fAng));
                    renderFacetedIceSpire(matrix, vc, spireBase.add(fRad.scale(spireRad * 0.72D)).add(0.0D, -0.8D, 0.0D),
                            downAxis, fRad, fTang, spireRad * 0.44F, spireLen * 0.62F, 5, fAng, fade * 0.68F);
                }
            } else {
                // Phase 2 (40+ ticks): Impact crash! Translucent frost aura sheath around the standing 3D Pseudoblock Monolith + expanding Frost Nova shockwave!
                float postTicks = elapsedTicks - 40.0F;
                float shockProg = Mth.clamp(postTicks / 24.0F, 0.0F, 1.0F);
                float shockR = radius * (0.25F + 1.05F * (1.0F - (1.0F - shockProg) * (1.0F - shockProg)));
                if (shockProg < 1.0F) {
                    renderOrientedRing(matrix, vc, ground.add(0.0D, 0.08D, 0.0D), right, forward,
                            shockR * 0.80F, shockR, 28, 0.94F, 0.99F, 1.00F, (1.0F - shockProg) * 0.95F * fade);
                }
                float auraSpireH = Math.min(tierSpireMax, (float) (riftH - 0.8D));
                renderFacetedIceSpire(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), upAxis, right, forward,
                        Math.max(0.95F, radius * 0.22F), auraSpireH, 6, 0.25F, fade * 0.36F);
            }
            return;
        }

        // Section 6.7 Brimstone Meteor Rain & Section 6.14 Volcanic Meteorite Bombardment:
        // Overhead roaring volcanic sky-rift raining hurtling 3D molten magma boulders / brimstone meteors!
        // Dynamically adapts rift height inside underground caves, dungeons, and Nether Fortresses (Section 4.8)!
        if (isInfernalMateria(inst.essence) || isMagmaCalderaMateria(inst.essence)) {
            double riftH = resolveTierScaledSkyfallHeight(ground, inst.tier, 9.6D);
            Vec3 riftCenter = ground.add(0.0D, riftH, 0.0D);
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);

            // 1. Overhead Volcanic Basalt-Slag & Molten Hellfire Sky-Rift Portal
            renderOrientedRing(matrix, vc, riftCenter, right, forward, radius * 0.78F, radius * 1.06F, 24,
                    0.165F, 0.090F, 0.071F, 0.88F * fade);
            renderOrientedRing(matrix, vc, riftCenter.add(0.0D, -0.08D, 0.0D), right, forward, 0.0F, radius * 0.94F, 24,
                    0.722F, 0.235F, 0.031F, 0.58F * fade);
            renderOrientedStarPolygon(matrix, vc, riftCenter.add(0.0D, -0.12D, 0.0D), right, forward,
                    radius * 0.92F, 8, gameTime * 0.035F + inst.seed, 1.00F, 0.820F, 0.361F, 0.90F * fade);

            // 2. Tier-scaled hurtling 3D molten brimstone meteors crashing diagonally down & exploding into hellfire ground shock-rings!
            int meteorCount = 14 + inst.tier * 2;
            for (int i = 0; i < meteorCount; i++) {
                float rFrac = (float) Math.sqrt((i + 0.5D) / meteorCount) * 0.90F;
                float theta = i * 2.39996F + inst.seed * 0.29F;
                double hitX = Mth.cos(theta) * radius * rFrac;
                double hitZ = Mth.sin(theta) * radius * rFrac;

                float fallCycle = ((gameTime * 0.090F + hashFloat(i, 127)) % 1.0F);
                double remFrac = 1.0D - (fallCycle * fallCycle);
                double my = riftH * remFrac;
                double mx = hitX - (1.25D * (riftH / 9.6D)) * remFrac;
                double mz = hitZ - (0.60D * (riftH / 9.6D)) * remFrac;

                Vec3 meteorPos = ground.add(mx, Math.max(0.28D, my), mz);
                float mAlpha = fade * (fallCycle < 0.93F ? 1.0F : (1.0F - (fallCycle - 0.93F) / 0.07F));
                float mSize = 0.22F + 0.06F * (i % 3);

                // Molten gold core + volcanic red-orange magma shell + basalt-slag crust + trailing flame comet wake!
                renderOctahedron(matrix, vc, meteorPos, mSize * 0.72F, 1.00F, 0.88F, 0.45F, 0.98F * mAlpha);
                renderOctahedron(matrix, vc, meteorPos, mSize * 1.12F, 0.722F, 0.235F, 0.031F, 0.88F * mAlpha);
                renderOctahedron(matrix, vc, meteorPos.add(0.04D, -0.02D, 0.03D), mSize * 1.24F, 0.165F, 0.090F, 0.071F, 0.62F * mAlpha);
                Vec3 tailTip = meteorPos.add(-0.52D * (riftH / 9.6D), Math.min(1.85D, riftH * 0.28D), -0.24D * (riftH / 9.6D));
                renderCrossSegment(matrix, vc, meteorPos, tailTip, mSize * 0.52F,
                        0.722F, 0.235F, 0.031F, 0.82F * mAlpha);
                renderCrossSegment(matrix, vc, meteorPos, tailTip, mSize * 0.24F,
                        1.00F, 0.820F, 0.361F, 0.94F * mAlpha);

                if (fallCycle > 0.70F) {
                    float impT = (fallCycle - 0.70F) / 0.30F;
                    float impR = 0.30F + 1.40F * impT;
                    Vec3 impBase = ground.add(hitX, 0.05D, hitZ);
                    renderOrientedRing(matrix, vc, impBase, right, forward,
                            impR * 0.74F, impR, 16, 1.00F, 0.820F, 0.361F, (1.0F - impT) * 0.92F * fade);
                    renderHellfireFlameTongue(matrix, vc, impBase, worldUp, right, forward,
                            0.16F * (1.0F - impT * 0.4F), 0.78F * Mth.sin(impT * (float) Math.PI), fade);
                }
            }

            // Blazing brimstone caldera perimeter ring on the ground
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    radius * 0.94F, radius * 1.02F, 28, 0.722F, 0.235F, 0.031F, 0.86F * fade);
            return;
        }

        // Section 6.8 Daylight Megalith Pillar: single colossal 8-faceted pillar of white-hot celestial quartz
        // crashing down after 2.0 seconds (40 ticks) and emitting expanding 3D holy shockwaves (100% cave-viable)!
        if (isSolarRadiantMateria(inst.essence)) {
            float elapsedTicks = progress * Math.max(1, inst.maxTicks);
            double skyGateH = resolveTierScaledSkyfallHeight(ground, inst.tier, 10.5D);
            Vec3 gateCenter = ground.add(0.0D, skyGateH, 0.0D);

            // 1. Overhead 12-Ray Solar Gate & Ground Targeting Sun-Compass Sigil
            renderOrientedRing(matrix, vc, gateCenter, right, forward, radius * 0.72F, radius * 0.96F, 28,
                    1.00F, 0.886F, 0.471F, 0.88F * fade);
            renderOrientedStarPolygon(matrix, vc, gateCenter.add(0.0D, -0.08D, 0.0D), right, forward,
                    radius * 0.94F, 12, gameTime * 0.045F + inst.seed, 1.00F, 0.995F, 0.95F, 0.92F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.045D, 0.0D), right, forward,
                    radius * 0.90F, radius * 1.00F, 32, 1.00F, 0.886F, 0.471F, 0.82F * fade);
            renderOrientedStarPolygon(matrix, vc, ground.add(0.0D, 0.055D, 0.0D), right, forward,
                    radius * 0.94F, 12, -gameTime * 0.045F, 1.00F, 0.98F, 0.88F, 0.78F * fade);

            // Guiding daylight sunbeam column before/after impact
            renderTube(matrix, vc, ground, gateCenter, Math.max(0.28F, radius * 0.20F), 10,
                    1.00F, 0.96F, 0.78F, 0.34F * fade, FULL_LIGHT);

            // 2. Tier-scaled Colossal 8-Faceted White-Hot Celestial Quartz Monolith descending over 40 ticks (2.0s)!
            float fallT = Mth.clamp(elapsedTicks / 40.0F, 0.0F, 1.0F);
            float accelFall = fallT * fallT * fallT; // Dramatic accelerating orbital drop!
            double tipY = Mth.lerp(accelFall, Math.max(1.2D, skyGateH - 1.2D), 0.0D);
            double pillarBodyH = Math.min(5.2D + inst.tier * 0.95D, Math.max(2.0D, skyGateH - 2.35D));
            double crownH = Math.min(1.35D, Math.max(0.65D, skyGateH * 0.14D));
            float pillarR = Math.max(1.05F, radius * 0.34F);
            Vec3 tipPos = ground.add(0.0D, tipY, 0.0D);
            Vec3 shoulderBot = ground.add(0.0D, tipY + 0.95D, 0.0D);
            Vec3 shoulderTop = ground.add(0.0D, Math.min(skyGateH - 0.15D, tipY + 0.95D + pillarBodyH), 0.0D);
            Vec3 crownApex = ground.add(0.0D, Math.min(skyGateH, shoulderTop.y - ground.y + crownH), 0.0D);

            for (int i = 0; i < 8; i++) {
                float a0 = (float) (Math.PI * 2.0D * i / 8.0D) + 0.19635F;
                float a1 = (float) (Math.PI * 2.0D * (i + 1) / 8.0D) + 0.19635F;
                Vec3 d0 = new Vec3(Mth.cos(a0) * pillarR, 0.0D, Mth.sin(a0) * pillarR);
                Vec3 d1 = new Vec3(Mth.cos(a1) * pillarR, 0.0D, Mth.sin(a1) * pillarR);
                Vec3 b0 = shoulderBot.add(d0);
                Vec3 b1 = shoulderBot.add(d1);
                Vec3 t0 = shoulderTop.add(d0.scale(0.88D));
                Vec3 t1 = shoulderTop.add(d1.scale(0.88D));

                boolean ivoryQuartz = (i % 2 == 0);
                float qr = 1.00F;
                float qg = ivoryQuartz ? 0.992F : 0.935F;
                float qb = ivoryQuartz ? 0.955F : 0.680F;
                emitDoubleSidedFlatQuad(matrix, vc, b0, b1, t1, t0, qr, qg, qb, 0.88F * fade);
                emitDoubleSidedFlatQuad(matrix, vc, tipPos, b1, b0, tipPos, 1.00F, 0.995F, 0.96F, 0.95F * fade);
                emitDoubleSidedFlatQuad(matrix, vc, t0, t1, crownApex, crownApex, 1.00F, 0.96F, 0.76F, 0.90F * fade);
                // Glowing golden #FFE278 celestial quartz inlay seams along all 8 monolith ribs!
                renderCrossSegment(matrix, vc, b0, t0, 0.045F, 1.00F, 0.886F, 0.471F, 0.96F * fade);
            }

            // 3. Repeating Holy Shockwave Rings & 12 Ground Sunburst Rays once the monolith crashes down at 40t!
            if (elapsedTicks >= 40.0F) {
                float postImpact = elapsedTicks - 40.0F;
                for (int w = 0; w < 3; w++) {
                    float waveLocal = (postImpact - w * 14.0F) / 28.0F;
                    if (waveLocal >= 0.0F && waveLocal <= 1.0F) {
                        float easeW = 1.0F - (1.0F - waveLocal) * (1.0F - waveLocal);
                        float shockR = radius * (0.24F + 0.92F * easeW);
                        float wAlpha = (1.0F - waveLocal) * 0.94F * fade;
                        renderOrientedRing(matrix, vc, ground.add(0.0D, 0.08D, 0.0D), right, forward,
                                shockR * 0.82F, shockR, 32, 1.00F, 0.886F, 0.471F, wAlpha);
                        renderOrientedRing(matrix, vc, ground.add(0.0D, 0.12D, 0.0D), right, forward,
                                shockR * 0.88F, shockR * 0.96F, 32, 1.00F, 0.995F, 0.96F, wAlpha * 0.90F);
                    }
                }
                Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
                for (int rIdx = 0; rIdx < 12; rIdx++) {
                    float rAng = (float) (Math.PI * 2.0D * rIdx / 12.0D);
                    Vec3 rad = new Vec3(Mth.cos(rAng), 0.0D, Mth.sin(rAng));
                    Vec3 tang = new Vec3(-Mth.sin(rAng), 0.0D, Mth.cos(rAng));
                    Vec3 tilt = worldUp.scale(0.35D).add(rad.scale(0.94D)).normalize();
                    renderSolarStarburstRay(matrix, vc, ground.add(rad.scale(pillarR * 0.92D)).add(0.0D, 0.10D, 0.0D),
                            tilt, rad, tang, 0.14F, (rIdx % 2 == 0 ? 1.85F : 1.25F), fade * 0.90F);
                }
            }
            return;
        } else if (isUmbralShadowMateria(inst.essence)) {
            float cloudRad = Math.max(4.0F, radius);
            double canopyClearance = resolveTierScaledSkyfallHeight(ground, inst.tier, 8.6D);
            Vec3 cloudCenter = new Vec3(ground.x, ground.y + canopyClearance, ground.z);
            renderHemisphereShell(matrix, vc, cloudCenter, cloudRad * 0.96F, 6, 26,
                    0.075F, 0.052F, 0.090F, 0.72F * fade);
            renderLowerHemisphereShell(matrix, vc, cloudCenter, cloudRad * 0.92F, 4, 24,
                    0.129F, 0.102F, 0.129F, 0.78F * fade);
            renderOrientedRing(matrix, vc, cloudCenter.add(0.0D, -0.18D, 0.0D), right, forward,
                    cloudRad * 0.78F, cloudRad * 1.02F, 32, 0.56F, 0.36F, 1.00F, 0.74F * fade);

            // Ground obfuscation mist ring & dark ripple splashes across the 16-block zone
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.06D, 0.0D), right, forward,
                    cloudRad * 0.90F, cloudRad * 1.00F, 34, 0.129F, 0.102F, 0.129F, 0.84F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.08D, 0.0D), right, forward,
                    cloudRad * 0.98F, cloudRad * 1.03F, 34, 0.56F, 0.36F, 1.00F, 0.68F * fade);

            Vec3 downAxis = new Vec3(0.0D, -1.0D, 0.0D);
            int dropCount = 32;
            for (int d = 0; d < dropCount; d++) {
                float dropPhase = ((gameTime * (0.048F + 0.014F * (d % 3)) + hashFloat(d, 41)) % 1.0F);
                float rFrac = (float) Math.sqrt(hashFloat(d, 97)) * 0.94F;
                float dAng = hashFloat(d, 151) * (float) (Math.PI * 2.0D);
                double dx = ground.x + Mth.cos(dAng) * cloudRad * rFrac;
                double dz = ground.z + Mth.sin(dAng) * cloudRad * rFrac;
                double dy = Mth.lerp(dropPhase, cloudCenter.y - 0.35D, ground.y + 0.12D);

                Vec3 dropTop = new Vec3(dx, dy + 0.75D, dz);
                renderUmbralShadowNeedle(matrix, vc, dropTop, downAxis, right, forward,
                        0.082F, 0.85F, fade * (0.55F + 0.45F * Mth.sin(dropPhase * (float) Math.PI)));

                if (dropPhase > 0.84F) {
                    float splashT = (dropPhase - 0.84F) / 0.16F;
                    float sRad = 0.22F + splashT * 0.85F;
                    renderOrientedRing(matrix, vc, new Vec3(dx, ground.y + 0.05D, dz), right, forward,
                            sRad * 0.76F, sRad, 14, 0.56F, 0.36F, 1.00F, (1.0F - splashT) * 0.82F * fade);
                }
            }
            return;
        } else if (isNecroticUndeadMateria(inst.essence)) {
            float hauntRad = Math.max(4.0F, radius);
            double riftH = resolveTierScaledSkyfallHeight(ground, inst.tier, 9.0D);
            Vec3 riftCenter = new Vec3(ground.x, ground.y + riftH, ground.z);

            // 1. Ghostly aerial ossuary & soul-ectoplasm rift overhead
            renderOrientedRing(matrix, vc, riftCenter, right, forward, hauntRad * 0.72F, hauntRad * 1.02F, 28,
                    0.302F, 0.392F, 0.325F, 0.82F * fade);
            renderOrientedRing(matrix, vc, riftCenter.add(0.0D, -0.10D, 0.0D), right, forward, 0.0F, hauntRad * 0.90F, 28,
                    0.369F, 0.878F, 0.533F, 0.42F * fade);
            renderOrientedStarPolygon(matrix, vc, riftCenter.add(0.0D, -0.14D, 0.0D), right, forward,
                    hauntRad * 0.92F, 8, -gameTime * 0.04F + inst.seed, 0.902F, 0.875F, 0.812F, 0.80F * fade);

            // Ground grave-haunt perimeter ring
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    hauntRad * 0.92F, hauntRad * 1.01F, 34, 0.302F, 0.392F, 0.325F, 0.80F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.07D, 0.0D), right, forward,
                    hauntRad * 0.96F, hauntRad * 1.02F, 34, 0.369F, 0.878F, 0.533F, 0.72F * fade);

            // 2. 14 swooping 3D hooded spectral wraiths dive-bombing along helical arcs!
            int wraithCount = 14;
            for (int w = 0; w < wraithCount; w++) {
                float diveCycle = ((gameTime * (0.055F + 0.012F * (w % 3)) + hashFloat(w, 61)) % 1.0F);
                float rFrac = (float) Math.sqrt((w + 0.5D) / wraithCount) * 0.88F;
                float baseAng = w * 2.39996F + inst.seed * 0.21F;
                float swoopAng = baseAng + diveCycle * 2.15F * ((w % 2 == 0) ? 1.0F : -1.0F);
                double wx = ground.x + Mth.cos(swoopAng) * hauntRad * rFrac;
                double wz = ground.z + Mth.sin(swoopAng) * hauntRad * rFrac;
                double wy = Mth.lerp(diveCycle * diveCycle, riftCenter.y - 0.35D, ground.y + 0.38D);

                Vec3 wraithPos = new Vec3(wx, wy, wz);
                Vec3 diveDir = new Vec3(-Mth.sin(swoopAng) * 0.45D, -0.88D, Mth.cos(swoopAng) * 0.45D).normalize();
                Vec3 wRight = buildPerpendicularRight(diveDir);
                Vec3 wUp = diveDir.cross(wRight).normalize();
                float wAlpha = fade * (diveCycle < 0.92F ? 1.0F : (1.0F - (diveCycle - 0.92F) / 0.08F));

                renderSpectralSkullHead(matrix, vc, wraithPos, diveDir, wRight, wUp, 0.20F, wAlpha);
                // Tattered #4D6453 wraith shroud & streaming #5EE088 soul-ectoplasm tail behind the swooping skull!
                Vec3 shroudMid = wraithPos.subtract(diveDir.scale(0.65D));
                Vec3 shroudTail = wraithPos.subtract(diveDir.scale(1.65D)).add(wRight.scale(0.14D * Mth.sin(gameTime * 0.3F + w)));
                renderCrossSegment(matrix, vc, wraithPos, shroudMid, 0.22F, 0.302F, 0.392F, 0.325F, 0.86F * wAlpha);
                renderCrossSegment(matrix, vc, shroudMid, shroudTail, 0.13F, 0.369F, 0.878F, 0.533F, 0.82F * wAlpha);

                if (diveCycle > 0.78F) {
                    float impT = (diveCycle - 0.78F) / 0.22F;
                    float impR = 0.25F + impT * 1.05F;
                    renderOrientedRing(matrix, vc, new Vec3(wx, ground.y + 0.05D, wz), right, forward,
                            impR * 0.76F, impR, 14, 0.369F, 0.878F, 0.533F, (1.0F - impT) * 0.88F * fade);
                }
            }
            return;
        } else if (inst.essence == EssenceType.VOID) {
            float vortexRad = Math.max(4.5F, radius);
            double riftH = resolveTierScaledSkyfallHeight(ground, inst.tier, 9.35D);
            Vec3 skyEye = new Vec3(ground.x, ground.y + riftH, ground.z);

            // 1. Overhead Pitch-Black Event Horizon Dome + #41304E / #C77DFF Spinning Accretion Disk!
            renderHemisphereShell(matrix, vc, skyEye.add(0.0D, -0.25D, 0.0D), vortexRad * 0.34F, 6, 22,
                    0.045F, 0.025F, 0.070F, 0.96F * fade);
            renderLowerHemisphereShell(matrix, vc, skyEye.add(0.0D, -0.25D, 0.0D), vortexRad * 0.34F, 5, 22,
                    0.045F, 0.025F, 0.070F, 0.96F * fade);
            renderOrientedRing(matrix, vc, skyEye.add(0.0D, -0.22D, 0.0D), right, forward,
                    vortexRad * 0.36F, vortexRad * 0.96F, 34, 0.255F, 0.188F, 0.306F, 0.82F * fade);
            renderOrientedRing(matrix, vc, skyEye.add(0.0D, -0.18D, 0.0D), right, forward,
                    vortexRad * 0.42F, vortexRad * 0.68F, 32, 0.780F, 0.490F, 1.000F, 0.88F * fade);
            renderOrientedStarPolygon(matrix, vc, skyEye.add(0.0D, -0.28D, 0.0D), right, forward,
                    vortexRad * 0.92F, 8, -gameTime * 0.06F + inst.seed, 0.780F, 0.490F, 1.000F, 0.84F * fade);

            // Ground 12m+ upward-gravity perimeter rings
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    vortexRad * 0.90F, vortexRad * 1.00F, 36, 0.255F, 0.188F, 0.306F, 0.86F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.07D, 0.0D), right, forward,
                    vortexRad * 0.96F, vortexRad * 1.03F, 36, 0.780F, 0.490F, 1.000F, 0.78F * fade);

            // 2. 14 UPWARD-spiraling 3D cosmic suction streams & levitating singularity shards pulled from floor into skyEye!
            int streamCount = 14;
            for (int s = 0; s < streamCount; s++) {
                float liftCycle = ((gameTime * (0.048F + 0.010F * (s % 3)) + hashFloat(s, 73)) % 1.0F);
                float baseR = vortexRad * (0.28F + 0.68F * hashFloat(s, 131));
                float curR = Mth.lerp(liftCycle, baseR, vortexRad * 0.10F);
                float spinAng = s * 2.39996F - gameTime * 0.08F + liftCycle * 2.65F;
                double sx = ground.x + Mth.cos(spinAng) * curR;
                double sz = ground.z + Mth.sin(spinAng) * curR;
                double sy = Mth.lerp(liftCycle * liftCycle, ground.y + 0.15D, skyEye.y - 0.30D);

                Vec3 shardPos = new Vec3(sx, sy, sz);
                float prevLift = Math.max(0.0F, liftCycle - 0.12F);
                float prevR = Mth.lerp(prevLift, baseR, vortexRad * 0.10F);
                float prevAng = s * 2.39996F - gameTime * 0.08F + prevLift * 2.65F;
                Vec3 tailPos = new Vec3(
                        ground.x + Mth.cos(prevAng) * prevR,
                        Mth.lerp(prevLift * prevLift, ground.y + 0.12D, skyEye.y - 0.30D),
                        ground.z + Mth.sin(prevAng) * prevR
                );
                float sAlpha = fade * Mth.sin(liftCycle * (float) Math.PI);
                boolean brightAccent = (s % 2 == 0);
                renderCrossSegment(matrix, vc, tailPos, shardPos, 0.11F,
                        brightAccent ? 0.780F : 0.255F,
                        brightAccent ? 0.490F : 0.188F,
                        brightAccent ? 1.000F : 0.306F,
                        0.88F * sAlpha);
                renderOctahedron(matrix, vc, shardPos, 0.14F,
                        0.055F, 0.030F, 0.085F, 0.96F * sAlpha);
                renderOctahedron(matrix, vc, shardPos, 0.19F,
                        0.780F, 0.490F, 1.000F, 0.58F * sAlpha);
            }
            return;
        } else if (isAridDesertMateria(inst.essence)) {
            float stormRad = Math.max(6.0F, radius);
            double stormH = resolveTierScaledSkyfallHeight(ground, inst.tier, 8.6D);
            Vec3 canopyCenter = new Vec3(ground.x, ground.y + stormH, ground.z);

            // 1. Swirling desert Haboob dust-cloud canopy overhead + scoured sandstorm perimeter ring underfoot
            renderOrientedRing(matrix, vc, canopyCenter, right, forward, 0.0F, stormRad * 1.04F, 34,
                    0.612F, 0.478F, 0.298F, 0.58F * fade);
            renderOrientedRing(matrix, vc, canopyCenter.add(0.0D, -0.22D, 0.0D), right, forward,
                    stormRad * 0.28F, stormRad * 0.96F, 32, 0.761F, 0.698F, 0.561F, 0.64F * fade);
            renderLowerHemisphereShell(matrix, vc, canopyCenter, stormRad * 0.96F, 3, 24,
                    0.761F, 0.698F, 0.561F, 0.30F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    stormRad * 0.88F, stormRad * 1.02F, 36, 0.761F, 0.698F, 0.561F, 0.82F * fade);

            // 2. Three sweeping cyclone dust-wall bands swirling through the 12-block storm cylinder
            for (int band = 0; band < 3; band++) {
                double by0 = ground.y + 0.15D + (band / 3.0D) * (stormH - 1.1D);
                double by1 = by0 + Math.min(1.45D, stormH * 0.22D);
                float bR0 = stormRad * (0.48F + band * 0.20F);
                float bR1 = bR0 * 1.08F;
                renderOrientedFrustumBand(matrix, vc,
                        new Vec3(ground.x, by0, ground.z), new Vec3(ground.x, by1, ground.z),
                        right, forward, bR0, bR1, 28,
                        (band == 1) ? 0.961F : 0.761F,
                        (band == 1) ? 0.894F : 0.698F,
                        (band == 1) ? 0.745F : 0.561F,
                        0.32F * fade, 0.14F * fade);
            }

            // 3. 28 diagonal wind-whipped sandstone grit streaks pelting the storm zone!
            int gritStreaks = 28;
            for (int i = 0; i < gritStreaks; i++) {
                float cycle = ((gameTime * (0.075F + 0.015F * (i % 3)) + hashFloat(i, 61)) % 1.0F);
                float rDist = stormRad * (0.14F + 0.82F * hashFloat(i, 109));
                float swirlAng = i * 2.39996F + gameTime * 0.095F + cycle * 1.85F;
                double gx = ground.x + Mth.cos(swirlAng) * rDist;
                double gz = ground.z + Mth.sin(swirlAng) * rDist;
                double gy = Mth.lerp(cycle, canopyCenter.y - 0.25D, ground.y + 0.12D);

                Vec3 windTail = new Vec3(
                        gx - Mth.sin(swirlAng) * 0.85D,
                        Math.min(canopyCenter.y - 0.15D, gy + 0.95D),
                        gz + Mth.cos(swirlAng) * 0.85D
                );
                Vec3 gritHead = new Vec3(gx, gy, gz);
                float gAlpha = fade * Mth.sin(cycle * (float) Math.PI);
                boolean sunlit = (i % 2 == 0);
                renderCrossSegment(matrix, vc, windTail, gritHead, 0.075F,
                        sunlit ? 0.961F : 0.761F,
                        sunlit ? 0.894F : 0.698F,
                        sunlit ? 0.745F : 0.561F,
                        0.86F * gAlpha);
            }
            return;
        } else if (isGalvanicLightningMateria(inst.essence)) {
            float stormRad = Math.max(6.0F, radius);
            double cloudH = resolveTierScaledSkyfallHeight(ground, inst.tier, 9.2D);
            Vec3 cloudCenter = new Vec3(ground.x, ground.y + cloudH, ground.z);
            int stormFrame = (int) (gameTime * 0.45F) + inst.seed * 13;
            float sheetFlash = ((stormFrame % 7) == 0) ? 0.95F : 0.45F;

            // 1. Brooding dark thunderhead stormcloud canopy + internal #FFE600 sheet-lightning flash!
            renderOrientedRing(matrix, vc, cloudCenter, right, forward, 0.0F, stormRad * 1.06F, 34,
                    0.14F, 0.17F, 0.23F, 0.78F * fade);
            renderOrientedRing(matrix, vc, cloudCenter.add(0.0D, -0.18D, 0.0D), right, forward,
                    stormRad * 0.22F, stormRad * 0.88F, 30, 1.00F, 0.902F, 0.00F, sheetFlash * 0.62F * fade);
            renderLowerHemisphereShell(matrix, vc, cloudCenter, stormRad * 0.96F, 3, 24,
                    0.16F, 0.19F, 0.26F, 0.46F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    stormRad * 0.92F, stormRad * 1.02F, 36, 1.00F, 0.902F, 0.00F, 0.80F * fade);

            // 2. Five animated violent 3D jagged lightning bolts slamming from the thundercloud to the floor!
            for (int b = 0; b < 5; b++) {
                int boltEpoch = (int) ((gameTime + b * 7.0F) / 9.0F);
                float boltPhase = ((gameTime + b * 7.0F) % 9.0F) / 9.0F;
                if (boltPhase > 0.62F) continue;
                float bAlpha = fade * (1.0F - boltPhase / 0.62F);

                float bDist = stormRad * (0.12F + 0.82F * hashFloat(boltEpoch * 17 + b, 43));
                float bAng = hashFloat(boltEpoch * 23 + b, 79) * (float) (Math.PI * 2.0D);
                Vec3 boltTop = new Vec3(ground.x + Mth.cos(bAng) * bDist * 0.75D, cloudCenter.y - 0.15D, ground.z + Mth.sin(bAng) * bDist * 0.75D);
                Vec3 boltGround = new Vec3(ground.x + Mth.cos(bAng) * bDist, ground.y + 0.08D, ground.z + Mth.sin(bAng) * bDist);

                Vec3 prev = boltTop;
                int segs = 7;
                for (int s = 1; s <= segs; s++) {
                    float st = s / (float) segs;
                    Vec3 next;
                    if (s == segs) {
                        next = boltGround;
                    } else {
                        float jx = (hashFloat(boltEpoch + b * 11, s * 5) - 0.5F) * 1.35F;
                        float jz = (hashFloat(boltEpoch + b * 11, s * 5 + 1) - 0.5F) * 1.35F;
                        next = boltTop.lerp(boltGround, st).add(jx, 0.0D, jz);
                    }
                    renderCrossSegment(matrix, vc, prev, next, 0.16F, 1.00F, 0.902F, 0.00F, 0.90F * bAlpha);
                    renderCrossSegment(matrix, vc, prev, next, 0.068F, 1.00F, 1.00F, 0.96F, 0.99F * bAlpha);
                    prev = next;
                }
                renderOrientedRing(matrix, vc, boltGround, right, forward,
                        0.28F, 0.72F + 0.85F * boltPhase, 18, 1.00F, 0.902F, 0.00F, 0.92F * bAlpha);
            }
            return;
        } else if (isTempestStormMateria(inst.essence)) {
            float monsoonRad = Math.max(8.0F, radius);
            double cloudH = resolveTierScaledSkyfallHeight(ground, inst.tier, 9.2D);
            Vec3 cloudCenter = new Vec3(ground.x, ground.y + cloudH, ground.z);

            renderOrientedRing(matrix, vc, cloudCenter, right, forward, 0.0F, monsoonRad * 1.04F, 36,
                    0.16F, 0.34F, 0.38F, 0.76F * fade);
            renderLowerHemisphereShell(matrix, vc, cloudCenter, monsoonRad * 0.96F, 3, 24,
                    0.259F, 0.608F, 0.639F, 0.44F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    monsoonRad * 0.92F, monsoonRad * 1.02F, 36, 0.259F, 0.608F, 0.639F, 0.84F * fade);

            for (int band = 0; band < 3; band++) {
                float bAng = gameTime * (0.24F + band * 0.06F) + band * 2.1F;
                double by = ground.y + 0.45D + band * (cloudH * 0.26D);
                renderSweptWindCrescent(matrix, vc, new Vec3(ground.x, by, ground.z),
                        right, forward, monsoonRad * (0.55F + band * 0.18F), bAng, 1.45F, 0.34F,
                        0.847F, 0.980F, 1.000F, 0.76F * fade);
            }

            for (int i = 0; i < 32; i++) {
                float rDist = monsoonRad * (0.10F + 0.86F * hashFloat(i, 113));
                float rAng = hashFloat(i, 61) * (float) (Math.PI * 2.0D) + gameTime * 0.05F;
                float fall = ((gameTime * 0.16F + hashFloat(i, 19)) % 1.0F);
                double topY = cloudCenter.y - fall * (cloudH - 0.25D);
                double botY = Math.max(ground.y + 0.08D, topY - 1.15D);
                Vec3 rTop = new Vec3(ground.x + Mth.cos(rAng) * rDist, topY, ground.z + Mth.sin(rAng) * rDist);
                Vec3 rBot = new Vec3(ground.x + Mth.cos(rAng + 0.06F) * (rDist + 0.22F), botY,
                        ground.z + Mth.sin(rAng + 0.06F) * (rDist + 0.22F));
                boolean foam = (i % 2 == 0);
                renderCrossSegment(matrix, vc, rTop, rBot, 0.032F,
                        foam ? 0.847F : 0.259F, foam ? 0.980F : 0.608F, foam ? 1.000F : 0.639F, 0.88F * fade);
            }
            return;
        } else if (isOvergrowthRampartMateria(inst.essence)) {
            float canopyRad = Math.max(8.0F, radius);
            double cloudH = resolveTierScaledSkyfallHeight(ground, inst.tier, 8.8D);
            Vec3 cloudCenter = new Vec3(ground.x, ground.y + cloudH, ground.z);

            renderOrientedRing(matrix, vc, cloudCenter, right, forward, 0.0F, canopyRad * 1.02F, 34,
                    0.102F, 0.349F, 0.137F, 0.72F * fade);
            renderLowerHemisphereShell(matrix, vc, cloudCenter, canopyRad * 0.94F, 3, 24,
                    0.173F, 0.902F, 0.408F, 0.40F * fade);
            renderBotanicalBlossom(matrix, vc, cloudCenter.add(0.0D, -0.10D, 0.0D), right, forward,
                    canopyRad * 0.88F, 6, gameTime * 0.035F, 0.706F, 1.000F, 0.471F, 0.68F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    canopyRad * 0.92F, canopyRad * 1.02F, 34, 0.173F, 0.902F, 0.408F, 0.84F * fade);

            for (int i = 0; i < 28; i++) {
                float rDist = canopyRad * (0.10F + 0.86F * hashFloat(i, 107));
                float rAng = hashFloat(i, 59) * (float) (Math.PI * 2.0D) + gameTime * 0.025F;
                float fall = ((gameTime * 0.085F + hashFloat(i, 23)) % 1.0F);
                double py = cloudCenter.y - fall * (cloudH - 0.25D);
                Vec3 motePos = new Vec3(ground.x + Mth.cos(rAng) * rDist, py, ground.z + Mth.sin(rAng) * rDist);
                boolean gold = (i % 3 == 0);
                renderOctahedron(matrix, vc, motePos, 0.11F * fade,
                        gold ? 1.000F : 0.173F, gold ? 0.847F : 0.902F, gold ? 0.239F : 0.408F, 0.90F * fade);
            }
            return;
        } else if (isAbrasiveDustMateria(inst.essence)) {
            float stormRad = Math.max(7.0F, radius);
            double stormH = resolveTierScaledSkyfallHeight(ground, inst.tier, 8.8D);
            Vec3 canopyCenter = new Vec3(ground.x, ground.y + stormH, ground.z);

            renderOrientedRing(matrix, vc, canopyCenter, right, forward, 0.0F, stormRad * 1.04F, 36,
                    0.761F, 0.698F, 0.561F, 0.68F * fade);
            renderLowerHemisphereShell(matrix, vc, canopyCenter, stormRad * 0.96F, 3, 26,
                    0.949F, 0.925F, 0.882F, 0.36F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    stormRad * 0.90F, stormRad * 1.02F, 36, 0.949F, 0.925F, 0.882F, 0.86F * fade);

            for (int band = 0; band < 3; band++) {
                double by0 = ground.y + 0.18D + (band / 3.0D) * (stormH - 1.1D);
                double by1 = by0 + Math.min(1.50D, stormH * 0.24D);
                float bR0 = stormRad * (0.50F + band * 0.20F);
                renderOrientedFrustumBand(matrix, vc,
                        new Vec3(ground.x, by0, ground.z), new Vec3(ground.x, by1, ground.z),
                        right, forward, bR0, bR0 * 1.08F, 30,
                        (band == 1) ? 0.949F : 0.761F,
                        (band == 1) ? 0.925F : 0.698F,
                        (band == 1) ? 0.882F : 0.561F,
                        0.36F * fade, 0.16F * fade);
            }

            for (int i = 0; i < 32; i++) {
                float cycle = ((gameTime * (0.080F + 0.015F * (i % 3)) + hashFloat(i, 67)) % 1.0F);
                float rDist = stormRad * (0.12F + 0.84F * hashFloat(i, 113));
                float swirlAng = i * 2.39996F + gameTime * 0.11F + cycle * 2.05F;
                double gx = ground.x + Mth.cos(swirlAng) * rDist;
                double gz = ground.z + Mth.sin(swirlAng) * rDist;
                double gy = Mth.lerp(cycle, canopyCenter.y - 0.25D, ground.y + 0.12D);
                Vec3 windTail = new Vec3(
                        gx - Mth.sin(swirlAng) * 0.92D,
                        Math.min(canopyCenter.y - 0.15D, gy + 0.95D),
                        gz + Mth.cos(swirlAng) * 0.92D
                );
                float gAlpha = fade * Mth.sin(cycle * (float) Math.PI);
                boolean chalk = (i % 2 == 0);
                renderCrossSegment(matrix, vc, windTail, new Vec3(gx, gy, gz), 0.072F,
                        chalk ? 0.949F : 0.761F, chalk ? 0.925F : 0.698F, chalk ? 0.882F : 0.561F,
                        0.88F * gAlpha);
            }
            return;
        } else if (isSymbioticSporeMateria(inst.essence)) {
            float fogRad = Math.max(7.0F, radius);
            double fogH = resolveTierScaledSkyfallHeight(ground, inst.tier, 8.6D);
            Vec3 canopyCenter = new Vec3(ground.x, ground.y + fogH, ground.z);

            renderOrientedRing(matrix, vc, canopyCenter, right, forward, 0.0F, fogRad * 1.02F, 34,
                    0.102F, 0.349F, 0.137F, 0.70F * fade);
            renderLowerHemisphereShell(matrix, vc, canopyCenter, fogRad * 0.95F, 3, 24,
                    0.659F, 0.525F, 0.329F, 0.38F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    fogRad * 0.88F, fogRad * 1.02F, 34, 0.173F, 0.902F, 0.408F, 0.82F * fade);

            for (int band = 0; band < 3; band++) {
                double by0 = ground.y + 0.14D + (band / 3.0D) * (fogH - 1.2D);
                double by1 = by0 + Math.min(1.45D, fogH * 0.22D);
                float bR0 = fogRad * (0.55F + band * 0.18F);
                renderOrientedFrustumBand(matrix, vc,
                        new Vec3(ground.x, by0, ground.z), new Vec3(ground.x, by1, ground.z),
                        right, forward, bR0, bR0 * 1.06F, 28,
                        (band == 1) ? 0.659F : 0.102F,
                        (band == 1) ? 0.525F : 0.349F,
                        (band == 1) ? 0.329F : 0.137F,
                        0.34F * fade, 0.15F * fade);
            }
            for (int i = 0; i < 30; i++) {
                float cycle = ((gameTime * 0.055F + hashFloat(i, 71)) % 1.0F);
                float rDist = fogRad * (0.10F + 0.86F * hashFloat(i, 109));
                float driftAng = i * 2.39996F + gameTime * 0.035F;
                double px = ground.x + Mth.cos(driftAng) * rDist;
                double pz = ground.z + Mth.sin(driftAng) * rDist;
                double py = Mth.lerp(cycle, canopyCenter.y - 0.25D, ground.y + 0.14D);
                boolean gold = (i % 2 == 0);
                renderOctahedron(matrix, vc, new Vec3(px, py, pz), 0.12F * fade * Mth.sin(cycle * (float) Math.PI),
                        gold ? 0.910F : 0.173F, gold ? 0.784F : 0.902F, gold ? 0.510F : 0.408F, 0.90F * fade);
            }
            return;
        } else if (isBorealTaigaMateria(inst.essence)) {
            // Section 6.20 Falling Glacial Timber Cataclysm: tier-scaled cave-safe Boreal Blizzard Sky-Rift (#1A5923 / #98DCF2)
            // accompanying the falling 3D Spruce-Log & Packed/Blue-Ice Pseudoblock monoliths!
            float borealRad = Math.max(6.8F, radius);
            double riftH = resolveTierScaledSkyfallHeight(ground, inst.tier, 9.5D);
            Vec3 riftCenter = new Vec3(ground.x, ground.y + riftH, ground.z);

            renderOrientedRing(matrix, vc, riftCenter, right, forward, 0.0F, borealRad * 1.04F, 34,
                    0.102F, 0.349F, 0.137F, 0.72F * fade);
            renderLowerHemisphereShell(matrix, vc, riftCenter, borealRad * 0.96F, 3, 24,
                    0.596F, 0.863F, 0.949F, 0.36F * fade);
            renderSnowflakeCrystal(matrix, vc, riftCenter.add(0.0D, -0.10D, 0.0D), right, forward,
                    borealRad * 0.96F, gameTime * 0.045F + inst.seed, 0.88F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    borealRad * 0.90F, borealRad * 1.02F, 36, 0.596F, 0.863F, 0.949F, 0.84F * fade);

            int streakCount = 24 + inst.tier * 2;
            for (int i = 0; i < streakCount; i++) {
                float cycle = ((gameTime * (0.078F + 0.014F * (i % 3)) + hashFloat(i, 89)) % 1.0F);
                float rDist = borealRad * (0.10F + 0.86F * hashFloat(i, 127));
                float swirlAng = i * 2.39996F + gameTime * 0.06F + cycle * 1.35F;
                double sx = ground.x + Mth.cos(swirlAng) * rDist;
                double sz = ground.z + Mth.sin(swirlAng) * rDist;
                double sy = Mth.lerp(cycle * cycle, riftCenter.y - 0.25D, ground.y + 0.12D);
                Vec3 tail = new Vec3(sx - Mth.sin(swirlAng) * 0.55D, Math.min(riftCenter.y - 0.10D, sy + 1.15D), sz + Mth.cos(swirlAng) * 0.55D);
                boolean frostIcicle = (i % 2 == 0);
                renderCrossSegment(matrix, vc, tail, new Vec3(sx, sy, sz), 0.058F,
                        frostIcicle ? 0.596F : 0.102F,
                        frostIcicle ? 0.863F : 0.349F,
                        frostIcicle ? 0.949F : 0.137F,
                        0.88F * fade * Mth.sin(cycle * (float) Math.PI));
            }
            return;
        }

        // Section 6.21 Miracle Desert Downpour: tier-scaled restorative rainstorm of sparkling #38C6F4 springwater & #C2B28F golden pollen!
        if (isMiracleOasisMateria(inst.essence)) {
            double canopyH = resolveTierScaledSkyfallHeight(ground, inst.tier, 9.0D);
            Vec3 canopyCenter = ground.add(0.0D, canopyH, 0.0D);
            float oasisRad = Math.max(6.8F, radius);

            // 1. Sunlit Desert-Mirage & Lush Oasis Sky-Canopy overhead
            renderHemisphereShell(matrix, vc, canopyCenter.add(0.0D, -0.30D, 0.0D), oasisRad * 0.98F, 4, 26,
                    0.102F, 0.349F, 0.137F, 0.40F * fade);
            renderLowerHemisphereShell(matrix, vc, canopyCenter, oasisRad * 0.95F, 3, 24,
                    0.220F, 0.776F, 0.957F, 0.32F * fade);
            renderBotanicalBlossom(matrix, vc, canopyCenter.add(0.0D, -0.18D, 0.0D), right, forward,
                    oasisRad * 0.92F, 8, gameTime * 0.03F + inst.seed, 0.871F, 0.788F, 0.604F, 0.64F * fade);
            renderOrientedRing(matrix, vc, canopyCenter.add(0.0D, -0.12D, 0.0D), right, forward,
                    oasisRad * 0.84F, oasisRad * 1.02F, 34, 0.761F, 0.698F, 0.561F, 0.72F * fade);

            // 2. Tier-scaled sparkling springwater rain sheets & drifting sunlit golden pollen blossoms!
            int streams = 26 + inst.tier * 3;
            for (int i = 0; i < streams; i++) {
                float rFrac = (float) Math.sqrt((i + 0.5D) / streams) * 0.94F;
                float theta = i * 2.39996F + inst.seed * 0.19F;
                double rx = Mth.cos(theta) * oasisRad * rFrac;
                double rz = Mth.sin(theta) * oasisRad * rFrac;

                float fallCycle = ((gameTime * 0.125F + hashFloat(i, 79)) % 1.0F);
                double topY = canopyH * (1.0D - fallCycle * 0.80D);
                double botY = Math.max(0.06D, topY - 2.20D);
                boolean pollenStream = (i % 3 == 0);
                if (pollenStream) {
                    double swayX = Math.sin(gameTime * 0.08D + i) * 0.32D;
                    double swayZ = Math.cos(gameTime * 0.07D + i * 1.2D) * 0.32D;
                    Vec3 pPos = ground.add(rx + swayX, Math.max(0.14D, canopyH * (1.0D - fallCycle)), rz + swayZ);
                    float pAlpha = fade * Mth.sin(fallCycle * (float) Math.PI);
                    renderOctahedron(matrix, vc, pPos, 0.105F + 0.03F * (i % 2),
                            0.871F, 0.788F, 0.604F, 0.88F * pAlpha);
                } else {
                    renderCrossSegment(matrix, vc,
                            ground.add(rx + 0.14D, topY, rz),
                            ground.add(rx, botY, rz),
                            0.044F, 0.220F, 0.776F, 0.957F, 0.78F * fade);
                }

                // Sparkling springwater & oasis-green splash ripples across the hydrated desert floor
                if (i % 3 == 1) {
                    float splashCycle = ((gameTime * 0.085F + hashFloat(i, 137)) % 1.0F);
                    float pRad = 0.20F + 0.80F * splashCycle;
                    float pAlpha = (1.0F - splashCycle) * 0.72F * fade;
                    renderOrientedRing(matrix, vc, ground.add(rx, 0.04D, rz), right, forward,
                            pRad * 0.74F, pRad, 14, 0.220F, 0.776F, 0.957F, pAlpha);
                }
            }

            // Lush Oasis perimeter ring on the ground
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    oasisRad * 0.95F, oasisRad * 1.01F, 36, 0.133F, 0.471F, 0.188F, 0.66F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.06D, 0.0D), right, forward,
                    oasisRad * 0.89F, oasisRad * 0.94F, 34, 0.761F, 0.698F, 0.561F, 0.58F * fade);
            return;
        }

        // Section 6.22 Font of Vitality Downpour (#FF6B9D / #FFD1E3): tier-scaled shimmering rain of rose life essence!
        if (isCellularVitaeMateria(inst.essence)) {
            double canopyH = resolveTierScaledSkyfallHeight(ground, inst.tier, 9.0D);
            Vec3 canopyCenter = ground.add(0.0D, canopyH, 0.0D);
            float vitaeRad = Math.max(6.8F, radius);
            float beat = 0.92F + 0.10F * Mth.abs(Mth.sin(gameTime * 0.24F));

            renderHemisphereShell(matrix, vc, canopyCenter.add(0.0D, -0.30D, 0.0D), vitaeRad * 0.98F, 4, 26,
                    1.000F, 0.420F, 0.616F, 0.38F * fade);
            renderBotanicalBlossom(matrix, vc, canopyCenter.add(0.0D, -0.18D, 0.0D), right, forward,
                    vitaeRad * 0.92F * beat, 8, gameTime * 0.035F + inst.seed, 1.000F, 0.820F, 0.890F, 0.68F * fade);
            renderOrientedRing(matrix, vc, canopyCenter.add(0.0D, -0.12D, 0.0D), right, forward,
                    vitaeRad * 0.84F, vitaeRad * 1.02F, 34, 1.000F, 0.420F, 0.616F, 0.76F * fade);

            int streams = 26 + inst.tier * 3;
            for (int i = 0; i < streams; i++) {
                float rFrac = (float) Math.sqrt((i + 0.5D) / streams) * 0.94F;
                float theta = i * 2.39996F + inst.seed * 0.19F;
                double rx = Mth.cos(theta) * vitaeRad * rFrac;
                double rz = Mth.sin(theta) * vitaeRad * rFrac;

                float fallCycle = ((gameTime * 0.115F + hashFloat(i, 83)) % 1.0F);
                double topY = canopyH * (1.0D - fallCycle * 0.80D);
                double botY = Math.max(0.06D, topY - 2.10D);
                if (i % 3 == 0) {
                    Vec3 orbPos = ground.add(rx, Math.max(0.14D, canopyH * (1.0D - fallCycle)), rz);
                    float oAlpha = fade * Mth.sin(fallCycle * (float) Math.PI);
                    renderOctahedron(matrix, vc, orbPos, 0.11F + 0.03F * (i % 2),
                            1.000F, (i % 2 == 0) ? 0.820F : 0.420F, (i % 2 == 0) ? 0.890F : 0.616F, 0.92F * oAlpha);
                } else {
                    renderCrossSegment(matrix, vc,
                            ground.add(rx + 0.08D, topY, rz),
                            ground.add(rx, botY, rz),
                            0.044F, 1.000F, 0.420F, 0.616F, 0.78F * fade);
                }

                if (i % 3 == 1) {
                    float splashCycle = ((gameTime * 0.085F + hashFloat(i, 139)) % 1.0F);
                    float pRad = 0.22F + 0.82F * splashCycle;
                    float pAlpha = (1.0F - splashCycle) * 0.72F * fade;
                    renderOrientedRing(matrix, vc, ground.add(rx, 0.04D, rz), right, forward,
                            pRad * 0.75F, pRad, 14, 1.000F, 0.820F, 0.890F, pAlpha);
                }
            }

            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    vitaeRad * 0.94F, vitaeRad * 1.01F, 36, 1.000F, 0.420F, 0.616F, 0.68F * fade);
            return;
        }

        // Section 6.23 Sanguine Deluge Downpour (#8A0303 / #FF384C): tier-scaled 16m+ crimson bloodstorm & floor blood slicks!
        if (isSanguineBloodMateria(inst.essence)) {
            double canopyH = resolveTierScaledSkyfallHeight(ground, inst.tier, 9.2D);
            Vec3 canopyCenter = ground.add(0.0D, canopyH, 0.0D);
            float bloodRad = Math.max(6.8F, radius);

            renderHemisphereShell(matrix, vc, canopyCenter.add(0.0D, -0.32D, 0.0D), bloodRad * 0.98F, 4, 26,
                    0.541F, 0.012F, 0.012F, 0.52F * fade);
            renderOrientedStarPolygon(matrix, vc, canopyCenter.add(0.0D, -0.18D, 0.0D), right, forward,
                    bloodRad * 0.92F, 8, gameTime * 0.032F + inst.seed, 1.000F, 0.220F, 0.298F, 0.72F * fade);
            renderOrientedRing(matrix, vc, canopyCenter.add(0.0D, -0.12D, 0.0D), right, forward,
                    bloodRad * 0.84F, bloodRad * 1.02F, 34, 0.541F, 0.012F, 0.012F, 0.84F * fade);

            int streams = 28 + inst.tier * 3;
            for (int i = 0; i < streams; i++) {
                float rFrac = (float) Math.sqrt((i + 0.5D) / streams) * 0.94F;
                float theta = i * 2.39996F + inst.seed * 0.19F;
                double rx = Mth.cos(theta) * bloodRad * rFrac;
                double rz = Mth.sin(theta) * bloodRad * rFrac;

                float fallCycle = ((gameTime * 0.145F + hashFloat(i, 89)) % 1.0F);
                double topY = canopyH * (1.0D - fallCycle * 0.82D);
                double botY = Math.max(0.05D, topY - 2.35D);
                boolean arterial = (i % 4 == 0);
                renderCrossSegment(matrix, vc,
                        ground.add(rx + 0.06D, topY, rz),
                        ground.add(rx, botY, rz),
                        0.046F,
                        arterial ? 1.000F : 0.541F,
                        arterial ? 0.220F : 0.012F,
                        arterial ? 0.298F : 0.012F,
                        0.88F * fade);

                if (i % 3 == 0) {
                    float slickCycle = ((gameTime * 0.075F + hashFloat(i, 149)) % 1.0F);
                    float pRad = 0.30F + 1.05F * slickCycle;
                    float pAlpha = (1.0F - slickCycle) * 0.82F * fade;
                    renderOrientedRing(matrix, vc, ground.add(rx, 0.04D, rz), right, forward,
                            0.0F, pRad, 14, 0.541F, 0.012F, 0.012F, pAlpha * 0.48F);
                    renderOrientedRing(matrix, vc, ground.add(rx, 0.045D, rz), right, forward,
                            pRad * 0.78F, pRad, 14, 1.000F, 0.220F, 0.298F, pAlpha);
                }
            }

            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    bloodRad * 0.94F, bloodRad * 1.01F, 36, 0.541F, 0.012F, 0.012F, 0.78F * fade);
            return;
        }

        // Section 6.24 Spiritual Gale Barrage (#FF6B9D / #A88654): tier-scaled aerial rift dropping compressed
        // atmospheric wind torpedoes that erupt into 3-block updraft geysers!
        if (isPranicAuraMateria(inst.essence)) {
            double riftH = resolveTierScaledSkyfallHeight(ground, inst.tier, 9.5D);
            Vec3 riftCenter = ground.add(0.0D, riftH, 0.0D);
            float auraRad = Math.max(6.8F, radius);

            renderOrientedRing(matrix, vc, riftCenter, right, forward, auraRad * 0.80F, auraRad * 1.02F, 32,
                    0.839F, 0.706F, 0.471F, 0.84F * fade);
            renderBotanicalBlossom(matrix, vc, riftCenter.add(0.0D, -0.14D, 0.0D), right, forward,
                    auraRad * 0.92F, 8, gameTime * 0.045F + inst.seed, 1.000F, 0.420F, 0.616F, 0.74F * fade);
            renderOrientedStarPolygon(matrix, vc, riftCenter.add(0.0D, -0.08D, 0.0D), right, forward,
                    auraRad * 0.94F, 8, -gameTime * 0.06F, 1.000F, 0.950F, 0.860F, 0.80F * fade);

            int torpedoStreams = 10 + inst.tier * 2;
            for (int i = 0; i < torpedoStreams; i++) {
                float rFrac = (float) Math.sqrt((i + 0.5D) / torpedoStreams) * 0.88F;
                float theta = i * 2.39996F + inst.seed * 0.21F;
                double tx = Mth.cos(theta) * auraRad * rFrac;
                double tz = Mth.sin(theta) * auraRad * rFrac;

                float fallCycle = ((gameTime * 0.085F + hashFloat(i, 103)) % 1.0F);
                double ty = riftH * (1.0D - fallCycle * fallCycle);
                Vec3 torpedoHead = ground.add(tx, Math.max(0.25D, ty), tz);
                Vec3 torpedoTail = torpedoHead.add(0.0D, Math.min(2.6D, riftH - ty + 0.4D), 0.0D);
                boolean pink = (i % 2 == 0);
                renderCrossSegment(matrix, vc, torpedoHead, torpedoTail, 0.085F,
                        pink ? 1.000F : 0.839F,
                        pink ? 0.420F : 0.706F,
                        pink ? 0.616F : 0.471F,
                        0.92F * fade);
                renderOctahedron(matrix, vc, torpedoHead, 0.18F,
                        1.000F, 0.960F, 0.900F, 0.96F * fade);

                if (fallCycle > 0.74F) {
                    float impT = (fallCycle - 0.74F) / 0.26F;
                    float geyserR = 0.35F + 2.45F * impT;
                    renderOrientedRing(matrix, vc, ground.add(tx, 0.05D + impT * 1.4D, tz), right, forward,
                            geyserR * 0.78F, geyserR, 18,
                            pink ? 1.000F : 0.839F,
                            pink ? 0.420F : 0.706F,
                            pink ? 0.616F : 0.471F,
                            (1.0F - impT) * 0.90F * fade);
                }
            }

            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    auraRad * 0.94F, auraRad * 1.01F, 36, 0.839F, 0.706F, 0.471F, 0.76F * fade);
            return;
        }

        // Section 6.25 Prehistoric Resinite Monsoon (#FF6B9D / #773214 / #F29B2E): tier-scaled cave-safe
        // scalding liquid sap downpour & fossilizing amber splash rings!
        if (isFossilAmberMateria(inst.essence)) {
            double canopyH = resolveTierScaledSkyfallHeight(ground, inst.tier, 9.2D);
            Vec3 canopyCenter = ground.add(0.0D, canopyH, 0.0D);
            float amberRad = Math.max(6.8F, radius);

            renderOrientedRing(matrix, vc, canopyCenter, right, forward, 0.0F, amberRad * 1.02F, 24,
                    0.467F, 0.196F, 0.078F, 0.62F * fade);
            renderOrientedRing(matrix, vc, canopyCenter.add(0.0D, -0.18D, 0.0D), right, forward,
                    amberRad * 0.34F, amberRad * 0.94F, 24, 0.949F, 0.608F, 0.180F, 0.54F * fade);
            renderLowerHemisphereShell(matrix, vc, canopyCenter, amberRad * 0.95F, 3, 22,
                    0.949F, 0.608F, 0.180F, 0.30F * fade);

            int sapStreams = 30 + inst.tier * 4;
            for (int i = 0; i < sapStreams; i++) {
                float rFrac = (float) Math.sqrt((i + 0.5D) / sapStreams) * 0.95F;
                float theta = i * 2.39996F + inst.seed * 0.15F;
                double sx = Mth.cos(theta) * amberRad * rFrac;
                double sz = Mth.sin(theta) * amberRad * rFrac;

                float fallCycle = ((gameTime * 0.070F + hashFloat(i, 109)) % 1.0F);
                double sy = canopyH * (1.0D - fallCycle);
                Vec3 dropBot = ground.add(sx, Math.max(0.10D, sy), sz);
                Vec3 dropTop = dropBot.add(0.0D, Math.min(2.2D, canopyH - sy + 0.3D), 0.0D);
                boolean rose = (i % 4 == 0);
                renderCrossSegment(matrix, vc, dropBot, dropTop, 0.068F,
                        rose ? 1.000F : 0.949F,
                        rose ? 0.420F : 0.608F,
                        rose ? 0.616F : 0.180F,
                        0.88F * fade);
                if (fallCycle > 0.78F) {
                    float spT = (fallCycle - 0.78F) / 0.22F;
                    float spR = 0.20F + 0.95F * spT;
                    renderOrientedRing(matrix, vc, ground.add(sx, 0.04D, sz), right, forward,
                            spR * 0.75F, spR, 6, 0.949F, 0.608F, 0.180F, (1.0F - spT) * 0.82F * fade);
                }
            }

            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    amberRad * 0.94F, amberRad * 1.01F, 32, 0.949F, 0.608F, 0.180F, 0.80F * fade);
            return;
        }

        // Section 6.26 Penumbral Shadow Monsoon (#FFEA78 / #211A21): tier-scaled cave-safe overhead Total Eclipse
        // Black Sun ringed in blazing coronal gold, raining dark solar radiation!
        if (isParadoxEclipseMateria(inst.essence)) {
            double eclipseH = resolveTierScaledSkyfallHeight(ground, inst.tier, 9.6D);
            Vec3 sunCenter = ground.add(0.0D, eclipseH, 0.0D);
            float ecRad = Math.max(6.8F, radius);

            // Overhead pitch-black Moon-occluded Sun disc encircled by a blazing golden solar corona!
            renderOrientedRing(matrix, vc, sunCenter, right, forward, 0.0F, ecRad * 0.78F, 32,
                    0.075F, 0.058F, 0.082F, 0.92F * fade);
            renderOrientedRing(matrix, vc, sunCenter.add(0.0D, -0.06D, 0.0D), right, forward,
                    ecRad * 0.76F, ecRad * 1.06F, 36, 1.000F, 0.918F, 0.471F, 0.96F * fade);
            renderOrientedStarPolygon(matrix, vc, sunCenter.add(0.0D, -0.10D, 0.0D), right, forward,
                    ecRad * 1.04F, 12, gameTime * 0.035F + inst.seed, 1.000F, 0.975F, 0.840F, 0.84F * fade);

            int radStreams = 28 + inst.tier * 4;
            for (int i = 0; i < radStreams; i++) {
                float rFrac = (float) Math.sqrt((i + 0.5D) / radStreams) * 0.95F;
                float theta = i * 2.39996F + inst.seed * 0.19F;
                double rx = Mth.cos(theta) * ecRad * rFrac;
                double rz = Mth.sin(theta) * ecRad * rFrac;

                float fallCycle = ((gameTime * 0.088F + hashFloat(i, 113)) % 1.0F);
                double ry = eclipseH * (1.0D - fallCycle);
                Vec3 rayBot = ground.add(rx, Math.max(0.10D, ry), rz);
                Vec3 rayTop = rayBot.add(0.0D, Math.min(2.8D, eclipseH - ry + 0.35D), 0.0D);
                boolean corona = (i % 3 == 0);
                renderCrossSegment(matrix, vc, rayBot, rayTop, corona ? 0.055F : 0.075F,
                        corona ? 1.000F : 0.129F,
                        corona ? 0.918F : 0.102F,
                        corona ? 0.471F : 0.129F,
                        0.92F * fade);
            }

            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    ecRad * 0.92F, ecRad * 1.02F, 36, 1.000F, 0.918F, 0.471F, 0.88F * fade);
            return;
        }

        // Section 6.27 Cadaverous Rot Tempest (#4D6453 / #34CC48): weeping aerial blight-rift (14m+ tier-scaled)
        // raining putrid fungal bile & skeletal thorns!
        if (isCadaverousBlightMateria(inst.essence)) {
            double blightH = resolveTierScaledSkyfallHeight(ground, inst.tier, 9.4D);
            Vec3 riftCenter = ground.add(0.0D, blightH, 0.0D);
            float blightRad = Math.max(8.0F, radius);

            renderOrientedRing(matrix, vc, riftCenter, right, forward, 0.0F, blightRad * 1.02F, 28,
                    0.302F, 0.392F, 0.325F, 0.68F * fade);
            renderOrientedRing(matrix, vc, riftCenter.add(0.0D, -0.16D, 0.0D), right, forward,
                    blightRad * 0.36F, blightRad * 0.96F, 28, 0.204F, 0.800F, 0.282F, 0.62F * fade);
            renderLowerHemisphereShell(matrix, vc, riftCenter, blightRad * 0.96F, 3, 24,
                    0.204F, 0.800F, 0.282F, 0.28F * fade);

            int bileStreams = 32 + inst.tier * 4;
            for (int i = 0; i < bileStreams; i++) {
                float rFrac = (float) Math.sqrt((i + 0.5D) / bileStreams) * 0.95F;
                float theta = i * 2.39996F + inst.seed * 0.17F;
                double bx = Mth.cos(theta) * blightRad * rFrac;
                double bz = Mth.sin(theta) * blightRad * rFrac;

                float fallCycle = ((gameTime * 0.082F + hashFloat(i, 127)) % 1.0F);
                double by = blightH * (1.0D - fallCycle);
                Vec3 dropBot = ground.add(bx, Math.max(0.10D, by), bz);
                Vec3 dropTop = dropBot.add(0.0D, Math.min(2.2D, blightH - by + 0.35D), 0.0D);
                boolean boneThorn = (i % 3 == 0);
                renderCrossSegment(matrix, vc, dropBot, dropTop, boneThorn ? 0.052F : 0.068F,
                        boneThorn ? 0.902F : 0.204F,
                        boneThorn ? 0.875F : 0.800F,
                        boneThorn ? 0.812F : 0.282F,
                        0.92F * fade);
            }

            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    blightRad * 0.94F, blightRad * 1.01F, 36, 0.204F, 0.800F, 0.282F, 0.80F * fade);
            return;
        }

        // Section 6.28 Celestial Meteor-Storm Cataclysm (#41304E / #FFE600): crackling 14m+ cosmic starlight rift
        // raining falling star comets and cosmic lightning bolts!
        if (isCosmicAstralMateria(inst.essence)) {
            double astralH = resolveTierScaledSkyfallHeight(ground, inst.tier, 10.6D);
            Vec3 riftCenter = ground.add(0.0D, astralH, 0.0D);
            float astralRad = Math.max(14.0F, radius);

            renderOrientedRing(matrix, vc, riftCenter, right, forward, 0.0F, astralRad * 1.02F, 32,
                    0.255F, 0.188F, 0.306F, 0.72F * fade);
            renderOrientedRing(matrix, vc, riftCenter.add(0.0D, -0.14D, 0.0D), right, forward,
                    astralRad * 0.42F, astralRad * 0.98F, 32, 1.000F, 0.902F, 0.000F, 0.68F * fade);
            renderOrientedStarPolygon(matrix, vc, riftCenter.add(0.0D, -0.08D, 0.0D), right, forward,
                    astralRad * 0.94F, 8, gameTime * 0.04F + inst.seed, 1.000F, 0.902F, 0.000F, 0.76F * fade);
            renderLowerHemisphereShell(matrix, vc, riftCenter, astralRad * 0.96F, 3, 28,
                    0.255F, 0.188F, 0.306F, 0.30F * fade);

            int cometStreams = 34 + inst.tier * 4;
            for (int i = 0; i < cometStreams; i++) {
                float rFrac = (float) Math.sqrt((i + 0.5D) / cometStreams) * 0.95F;
                float theta = i * 2.39996F + inst.seed * 0.19F;
                double cx = Mth.cos(theta) * astralRad * rFrac;
                double cz = Mth.sin(theta) * astralRad * rFrac;

                float fallCycle = ((gameTime * 0.095F + hashFloat(i, 139)) % 1.0F);
                double cy = astralH * (1.0D - fallCycle);
                Vec3 cometHead = ground.add(cx, Math.max(0.14D, cy), cz);
                Vec3 cometTail = cometHead.add(0.28D, Math.min(2.6D, astralH - cy + 0.45D), 0.18D);
                boolean bolt = (i % 2 == 0);
                renderCrossSegment(matrix, vc, cometHead, cometTail, bolt ? 0.056F : 0.078F,
                        bolt ? 1.000F : 0.255F,
                        bolt ? 0.902F : 0.188F,
                        bolt ? 0.000F : 0.306F,
                        0.95F * fade);
                if (!bolt) {
                    renderOctahedron(matrix, vc, cometHead, 0.14F * fade, 1.000F, 0.902F, 0.000F, 0.98F * fade);
                }
            }

            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    astralRad * 0.95F, astralRad * 1.01F, 40, 1.000F, 0.902F, 0.000F, 0.84F * fade);
            return;
        }

        // Section 6.29 Phantasmal Soulfire Tempest (#1DF0E5 / #0D6469): howling 14m+ spectral rift
        // raining cyan soul-embers and blue fireballs!
        if (isPhantasmalSoulfireMateria(inst.essence)) {
            double soulH = resolveTierScaledSkyfallHeight(ground, inst.tier, 10.2D);
            Vec3 riftCenter = ground.add(0.0D, soulH, 0.0D);
            float soulRad = Math.max(14.0F, radius);

            renderOrientedRing(matrix, vc, riftCenter, right, forward, 0.0F, soulRad * 1.02F, 30,
                    0.051F, 0.392F, 0.412F, 0.70F * fade);
            renderOrientedRing(matrix, vc, riftCenter.add(0.0D, -0.14D, 0.0D), right, forward,
                    soulRad * 0.38F, soulRad * 0.96F, 30, 0.114F, 0.941F, 0.898F, 0.66F * fade);
            renderLowerHemisphereShell(matrix, vc, riftCenter, soulRad * 0.96F, 3, 26,
                    0.114F, 0.941F, 0.898F, 0.28F * fade);

            int fireballStreams = 32 + inst.tier * 4;
            for (int i = 0; i < fireballStreams; i++) {
                float rFrac = (float) Math.sqrt((i + 0.5D) / fireballStreams) * 0.95F;
                float theta = i * 2.39996F + inst.seed * 0.19F;
                double fx = Mth.cos(theta) * soulRad * rFrac;
                double fz = Mth.sin(theta) * soulRad * rFrac;

                float fallCycle = ((gameTime * 0.088F + hashFloat(i, 151)) % 1.0F);
                double fy = soulH * (1.0D - fallCycle);
                Vec3 fbHead = ground.add(fx, Math.max(0.14D, fy), fz);
                Vec3 fbTail = fbHead.add(0.0D, Math.min(2.4D, soulH - fy + 0.40D), 0.0D);
                boolean fireball = (i % 2 == 0);
                renderCrossSegment(matrix, vc, fbHead, fbTail, fireball ? 0.072F : 0.052F,
                        fireball ? 0.114F : 0.051F,
                        fireball ? 0.941F : 0.392F,
                        fireball ? 0.898F : 0.412F,
                        0.94F * fade);
                if (fireball) {
                    renderOctahedron(matrix, vc, fbHead, 0.13F * fade, 0.88F, 1.00F, 0.99F, 0.98F * fade);
                }
            }

            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    soulRad * 0.94F, soulRad * 1.01F, 38, 0.114F, 0.941F, 0.898F, 0.84F * fade);
            return;
        }

        // Section 6.30 Thermal Condensation Storm (#E0F7FA / #30A9B0): 14m+ scalding steam cloud canopy
        // raining boiling aquatic drops for 12s!
        if (isScaldingVaporMateria(inst.essence)) {
            double vaporH = resolveTierScaledSkyfallHeight(ground, inst.tier, 10.0D);
            Vec3 cloudCenter = ground.add(0.0D, vaporH, 0.0D);
            float vaporRad = Math.max(14.0F, radius);

            renderOrientedRing(matrix, vc, cloudCenter, right, forward, 0.0F, vaporRad * 1.02F, 30,
                    0.878F, 0.969F, 0.980F, 0.68F * fade);
            renderOrientedRing(matrix, vc, cloudCenter.add(0.0D, -0.16D, 0.0D), right, forward,
                    vaporRad * 0.36F, vaporRad * 0.96F, 30, 0.188F, 0.663F, 0.690F, 0.60F * fade);
            renderLowerHemisphereShell(matrix, vc, cloudCenter, vaporRad * 0.96F, 3, 26,
                    0.878F, 0.969F, 0.980F, 0.30F * fade);

            int rainStreams = 36 + inst.tier * 4;
            for (int i = 0; i < rainStreams; i++) {
                float rFrac = (float) Math.sqrt((i + 0.5D) / rainStreams) * 0.95F;
                float theta = i * 2.39996F + inst.seed * 0.17F;
                double rx = Mth.cos(theta) * vaporRad * rFrac;
                double rz = Mth.sin(theta) * vaporRad * rFrac;

                float fallCycle = ((gameTime * 0.105F + hashFloat(i, 163)) % 1.0F);
                double ry = vaporH * (1.0D - fallCycle);
                Vec3 dropBot = ground.add(rx, Math.max(0.10D, ry), rz);
                Vec3 dropTop = dropBot.add(0.0D, Math.min(2.0D, vaporH - ry + 0.30D), 0.0D);
                boolean steam = (i % 3 == 0);
                renderCrossSegment(matrix, vc, dropBot, dropTop, steam ? 0.062F : 0.048F,
                        steam ? 0.878F : 0.188F,
                        steam ? 0.969F : 0.663F,
                        steam ? 0.980F : 0.690F,
                        0.92F * fade);
            }

            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    vaporRad * 0.94F, vaporRad * 1.01F, 38, 0.188F, 0.663F, 0.690F, 0.82F * fade);
            return;
        }

        // Section 6.31 White Hole Singularity Tempest (#FFFFFF / #41304E): 14m+ blinding aerial rift raining antimatter stars for 10s!
        if (isAntimatterNullRMateria(inst.essence)) {
            double riftH = resolveTierScaledSkyfallHeight(ground, inst.tier, 10.5D);
            Vec3 riftCenter = ground.add(0.0D, riftH, 0.0D);
            float riftRad = Math.max(14.0F, radius);

            renderOrientedRing(matrix, vc, riftCenter, right, forward, 0.0F, riftRad * 1.02F, 32,
                    1.000F, 1.000F, 1.000F, 0.72F * fade);
            renderOrientedRing(matrix, vc, riftCenter.add(0.0D, -0.16D, 0.0D), right, forward,
                    riftRad * 0.34F, riftRad * 0.98F, 32, 0.255F, 0.188F, 0.306F, 0.78F * fade);
            renderOrientedStarPolygon(matrix, vc, riftCenter.add(0.0D, -0.26D, 0.0D), right, forward,
                    riftRad * 0.88F, 8, gameTime * 0.04F, 1.000F, 1.000F, 1.000F, 0.88F * fade);
            renderLowerHemisphereShell(matrix, vc, riftCenter, riftRad * 0.96F, 3, 28,
                    0.255F, 0.188F, 0.306F, 0.34F * fade);

            int starCount = 34 + inst.tier * 4;
            for (int i = 0; i < starCount; i++) {
                float rFrac = (float) Math.sqrt((i + 0.5D) / starCount) * 0.95F;
                float theta = i * 2.39996F + inst.seed * 0.19F;
                double rx = Mth.cos(theta) * riftRad * rFrac;
                double rz = Mth.sin(theta) * riftRad * rFrac;

                float fallCycle = ((gameTime * 0.095F + hashFloat(i, 179)) % 1.0F);
                double ry = riftH * (1.0D - fallCycle);
                Vec3 starHead = ground.add(rx, Math.max(0.16D, ry), rz);
                Vec3 starTail = starHead.add(0.0D, Math.min(2.1D, riftH - ry + 0.25D), 0.0D);
                boolean white = (i % 2 == 0);
                renderCrossSegment(matrix, vc, starHead, starTail, white ? 0.064F : 0.052F,
                        white ? 1.000F : 0.255F,
                        white ? 1.000F : 0.188F,
                        white ? 1.000F : 0.306F,
                        0.96F * fade);
                if (i % 3 == 0) {
                    renderOctahedron(matrix, vc, starHead, 0.12F * fade, 1.000F, 1.000F, 1.000F, 0.99F * fade);
                }
            }

            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    riftRad * 0.94F, riftRad * 1.01F, 38, 1.000F, 1.000F, 1.000F, 0.88F * fade);
            return;
        }

        // Section 6.32 Black Hole Oblivion Tempest (#110B17 / #41304E): 14m+ abyssal aerial rift raining dark singularity micro-orbs for 10s!
        if (isAbyssalNullUMateria(inst.essence)) {
            double riftH = resolveTierScaledSkyfallHeight(ground, inst.tier, 10.5D);
            Vec3 riftCenter = ground.add(0.0D, riftH, 0.0D);
            float riftRad = Math.max(14.0F, radius);

            renderOrientedRing(matrix, vc, riftCenter, right, forward, 0.0F, riftRad * 1.02F, 32,
                    0.067F, 0.043F, 0.090F, 0.82F * fade);
            renderOrientedRing(matrix, vc, riftCenter.add(0.0D, -0.16D, 0.0D), right, forward,
                    riftRad * 0.34F, riftRad * 0.98F, 32, 0.255F, 0.188F, 0.306F, 0.80F * fade);
            renderOrientedStarPolygon(matrix, vc, riftCenter.add(0.0D, -0.26D, 0.0D), right, forward,
                    riftRad * 0.88F, 8, -gameTime * 0.04F, 0.255F, 0.188F, 0.306F, 0.90F * fade);
            renderLowerHemisphereShell(matrix, vc, riftCenter, riftRad * 0.96F, 3, 28,
                    0.067F, 0.043F, 0.090F, 0.44F * fade);

            int orbCount = 34 + inst.tier * 4;
            for (int i = 0; i < orbCount; i++) {
                float rFrac = (float) Math.sqrt((i + 0.5D) / orbCount) * 0.95F;
                float theta = i * 2.39996F + inst.seed * 0.19F;
                double rx = Mth.cos(theta) * riftRad * rFrac;
                double rz = Mth.sin(theta) * riftRad * rFrac;

                float fallCycle = ((gameTime * 0.092F + hashFloat(i, 191)) % 1.0F);
                double ry = riftH * (1.0D - fallCycle);
                Vec3 orbHead = ground.add(rx, Math.max(0.16D, ry), rz);
                Vec3 orbTail = orbHead.add(0.0D, Math.min(2.0D, riftH - ry + 0.25D), 0.0D);
                boolean dark = (i % 2 == 0);
                renderCrossSegment(matrix, vc, orbHead, orbTail, dark ? 0.064F : 0.052F,
                        dark ? 0.067F : 0.255F,
                        dark ? 0.043F : 0.188F,
                        dark ? 0.090F : 0.306F,
                        0.96F * fade);
                if (i % 3 == 0) {
                    renderOctahedron(matrix, vc, orbHead, 0.13F * fade, 0.067F, 0.043F, 0.090F, 0.99F * fade);
                }
            }

            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    riftRad * 0.94F, riftRad * 1.01F, 38, 0.255F, 0.188F, 0.306F, 0.90F * fade);
            return;
        }

        // Section 6.33 Sunrise Daybreak Tempest (#FFEA78 / #A88654): 14m+ celestial sunrise rift raining sunlight lances & howling updrafts!
        if (isDaybreakDawnMateria(inst.essence)) {
            double riftH = resolveTierScaledSkyfallHeight(ground, inst.tier, 10.5D);
            Vec3 riftCenter = ground.add(0.0D, riftH, 0.0D);
            float riftRad = Math.max(14.0F, radius);

            renderOrientedRing(matrix, vc, riftCenter, right, forward, 0.0F, riftRad * 1.02F, 32,
                    1.000F, 0.918F, 0.471F, 0.76F * fade);
            renderOrientedRing(matrix, vc, riftCenter.add(0.0D, -0.16D, 0.0D), right, forward,
                    riftRad * 0.36F, riftRad * 0.98F, 32, 0.659F, 0.525F, 0.329F, 0.72F * fade);
            renderOrientedStarPolygon(matrix, vc, riftCenter.add(0.0D, -0.26D, 0.0D), right, forward,
                    riftRad * 0.90F, 8, gameTime * 0.045F, 1.000F, 0.918F, 0.471F, 0.92F * fade);
            renderLowerHemisphereShell(matrix, vc, riftCenter, riftRad * 0.96F, 3, 28,
                    1.000F, 0.918F, 0.471F, 0.32F * fade);

            int lanceCount = 36 + inst.tier * 4;
            for (int i = 0; i < lanceCount; i++) {
                float rFrac = (float) Math.sqrt((i + 0.5D) / lanceCount) * 0.95F;
                float theta = i * 2.39996F + inst.seed * 0.17F;
                double rx = Mth.cos(theta) * riftRad * rFrac;
                double rz = Mth.sin(theta) * riftRad * rFrac;

                float fallCycle = ((gameTime * 0.110F + hashFloat(i, 211)) % 1.0F);
                double ry = riftH * (1.0D - fallCycle);
                Vec3 lanceHead = ground.add(rx, Math.max(0.14D, ry), rz);
                Vec3 lanceTail = lanceHead.add(0.0D, Math.min(2.4D, riftH - ry + 0.30D), 0.0D);
                boolean sunrise = (i % 3 != 0);
                renderCrossSegment(matrix, vc, lanceHead, lanceTail, sunrise ? 0.064F : 0.052F,
                        sunrise ? 1.000F : 0.659F,
                        sunrise ? 0.918F : 0.525F,
                        sunrise ? 0.471F : 0.329F,
                        0.96F * fade);
            }

            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    riftRad * 0.94F, riftRad * 1.01F, 38, 1.000F, 0.918F, 0.471F, 0.88F * fade);
            return;
        }

        // Section 6.34 Crushing Trench Monolith (#211A21 / #225C7C): 2s telegraph -> colossal abyssal black-ice monolith & 10m black tidal wave!
        if (isHadopelagicAbyssMateria(inst.essence)) {
            double skyH = resolveTierScaledSkyfallHeight(ground, inst.tier, 10.5D);
            Vec3 riftCenter = ground.add(0.0D, skyH, 0.0D);
            float trenchRad = Math.max(8.0F, radius);
            float elapsed = progress * inst.maxTicks;

            renderOrientedRing(matrix, vc, riftCenter, right, forward, 0.0F, trenchRad * 0.86F, 28,
                    0.129F, 0.102F, 0.129F, 0.86F * fade);
            renderOrientedRing(matrix, vc, riftCenter.add(0.0D, -0.16D, 0.0D), right, forward,
                    trenchRad * 0.34F, trenchRad * 0.92F, 28, 0.133F, 0.361F, 0.486F, 0.78F * fade);

            if (elapsed < 40.0F) {
                float teleProg = Mth.clamp(elapsed / 40.0F, 0.0F, 1.0F);
                float sealR = trenchRad * (1.0F - 0.55F * teleProg);
                renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                        sealR * 0.82F, sealR, 30, 0.133F, 0.361F, 0.486F, 0.92F * fade);
                renderOrientedRing(matrix, vc, ground.add(0.0D, 0.04D, 0.0D), right, forward,
                        0.85F, 1.65F, 22, 0.129F, 0.102F, 0.129F, 0.96F * fade);
            } else {
                float postProg = Mth.clamp((elapsed - 40.0F) / 36.0F, 0.0F, 1.0F);
                float waveR = 1.4F + postProg * (trenchRad * 1.15F);
                renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, 1.65D * (1.0F - postProg * 0.55F), 0.0D),
                        right, forward, Math.max(0.4F, waveR * 0.84F), waveR, 32,
                        0.129F, 0.102F, 0.129F, 0.94F * (1.0F - postProg * 0.75F) * fade, 0.24F * fade);
                renderOrientedRing(matrix, vc, ground.add(0.0D, 0.06D, 0.0D), right, forward,
                        waveR * 0.88F, waveR * 1.04F, 34, 0.133F, 0.361F, 0.486F, 0.92F * (1.0F - postProg * 0.65F) * fade);

                double colTop = Math.min(skyH, 5.2D + inst.tier * 0.35D);
                renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, colTop, 0.0D), right, forward,
                        1.42F, 1.08F, 8, 0.129F, 0.102F, 0.129F, 0.92F * fade, 0.72F * fade);
                renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, colTop * 0.92D, 0.0D), right, forward,
                        1.58F, 1.22F, 8, 0.133F, 0.361F, 0.486F, 0.64F * fade, 0.32F * fade);
            }
            return;
        }

        // Section 6.35 Gilded Citadel Megalith Cataclysm (#FFEA78 / #773214): 2s telegraph -> monolithic golden granite pillar & 14m shockwave!
        if (isGildedAegisMateria(inst.essence)) {
            double skyH = resolveTierScaledSkyfallHeight(ground, inst.tier, 11.0D);
            Vec3 riftCenter = ground.add(0.0D, skyH, 0.0D);
            float shockRad = Math.max(14.0F, radius);
            float elapsed = progress * inst.maxTicks;

            renderOrientedRing(matrix, vc, riftCenter, right, forward, 0.0F, shockRad * 0.72F, 8,
                    0.467F, 0.196F, 0.078F, 0.84F * fade);
            renderOrientedStarPolygon(matrix, vc, riftCenter.add(0.0D, -0.18D, 0.0D), right, forward,
                    shockRad * 0.78F, 8, gameTime * 0.035F, 1.000F, 0.918F, 0.471F, 0.94F * fade);

            if (elapsed < 40.0F) {
                float teleProg = Mth.clamp(elapsed / 40.0F, 0.0F, 1.0F);
                float sealR = shockRad * (1.0F - 0.58F * teleProg);
                renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                        sealR * 0.86F, sealR, 16, 1.000F, 0.918F, 0.471F, 0.95F * fade);
                renderOrientedRing(matrix, vc, ground.add(0.0D, 0.04D, 0.0D), right, forward,
                        1.00F, 1.85F, 8, 0.467F, 0.196F, 0.078F, 0.96F * fade);
            } else {
                float postProg = Mth.clamp((elapsed - 40.0F) / 34.0F, 0.0F, 1.0F);
                float waveR = 1.6F + postProg * shockRad;
                renderOrientedRing(matrix, vc, ground.add(0.0D, 0.07D, 0.0D), right, forward,
                        waveR * 0.88F, waveR * 1.03F, 24, 1.000F, 0.918F, 0.471F, 0.96F * (1.0F - postProg * 0.65F) * fade);

                double colTop = Math.min(skyH, 5.8D + inst.tier * 0.38D);
                renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, colTop, 0.0D), right, forward,
                        1.48F, 1.24F, 8, 0.467F, 0.196F, 0.078F, 0.96F * fade, 0.82F * fade);
                renderOrientedFrustumBand(matrix, vc, ground, ground.add(0.0D, colTop * 0.96D, 0.0D), right, forward,
                        1.64F, 1.36F, 8, 1.000F, 0.918F, 0.471F, 0.68F * fade, 0.36F * fade);
            }
            return;
        }

        // Section 6.36 Cryo-Solar Comet Barrage (#FFEA78 / #98DCF2): shimmering borealis sky-curtain canopy & sub-zero comet seal!
        if (isBorealisAuroraMateria(inst.essence)) {
            double skyH = resolveTierScaledSkyfallHeight(ground, inst.tier, 10.6D);
            Vec3 borealisTop = ground.add(0.0D, skyH, 0.0D);
            float fieldRad = Math.max(8.0F, radius);

            renderOrientedRing(matrix, vc, borealisTop, right, forward, 0.0F, fieldRad * 0.96F, 28,
                    0.596F, 0.863F, 0.949F, 0.72F * fade);
            renderOrientedStarPolygon(matrix, vc, borealisTop.add(0.0D, -0.15D, 0.0D), right, forward,
                    fieldRad * 0.90F, 6, gameTime * 0.040F, 1.000F, 0.918F, 0.471F, 0.92F * fade);
            renderOrientedFrustumBand(matrix, vc, borealisTop.add(0.0D, -1.8D, 0.0D), borealisTop, right, forward,
                    fieldRad * 0.86F, fieldRad * 1.02F, 28,
                    1.000F, 0.918F, 0.471F, 0.34F * fade, 0.76F * fade);

            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    fieldRad * 0.90F, fieldRad * 1.02F, 28, 0.596F, 0.863F, 0.949F, 0.90F * fade);
            renderOrientedStarPolygon(matrix, vc, ground.add(0.0D, 0.06D, 0.0D), right, forward,
                    fieldRad * 0.88F, 6, -gameTime * 0.035F, 1.000F, 0.918F, 0.471F, 0.84F * fade);
            return;
        }

        // Section 6.37 Mirage Sand-Storm Tempest (#FFEA78 / #C2B28F): 10s swirling golden sand-cloud canopy, blinding refractive light & desert vortex!
        if (isPhantasmMirageMateria(inst.essence)) {
            double skyH = resolveTierScaledSkyfallHeight(ground, inst.tier, 9.6D);
            Vec3 mirageTop = ground.add(0.0D, skyH, 0.0D);
            float fieldRad = Math.max(8.0F, radius);

            renderOrientedRing(matrix, vc, mirageTop, right, forward, 0.0F, fieldRad * 1.02F, 28,
                    0.761F, 0.698F, 0.561F, 0.68F * fade);
            renderLowerHemisphereShell(matrix, vc, mirageTop, fieldRad * 0.94F, 3, 24,
                    0.761F, 0.698F, 0.561F, 0.30F * fade);
            renderOrientedStarPolygon(matrix, vc, mirageTop.add(0.0D, -0.14D, 0.0D), right, forward,
                    fieldRad * 0.88F, 8, gameTime * 0.045F, 1.000F, 0.918F, 0.471F, 0.92F * fade);

            for (int b = 0; b < 3; b++) {
                double bandY = skyH * (0.20D + b * 0.26D);
                float bandR = fieldRad * (0.76F + 0.08F * Mth.sin(gameTime * 0.18F + b * 1.4F));
                renderOrientedFrustumBand(matrix, vc,
                        ground.add(0.0D, bandY, 0.0D),
                        ground.add(0.0D, bandY + 1.35D, 0.0D),
                        right, forward, bandR * 0.94F, bandR * 1.04F, 24,
                        (b == 1) ? 1.000F : 0.761F,
                        (b == 1) ? 0.918F : 0.698F,
                        (b == 1) ? 0.471F : 0.561F,
                        0.38F * fade, 0.16F * fade);
            }
            int shafts = 12 + inst.tier * 2;
            for (int s = 0; s < shafts; s++) {
                float theta = s * 2.39996F + gameTime * 0.055F + inst.seed * 0.15F;
                float rFrac = (float) Math.sqrt((s + 0.5D) / shafts) * 0.90F;
                double sx = Mth.cos(theta) * fieldRad * rFrac;
                double sz = Mth.sin(theta) * fieldRad * rFrac;
                float fallT = ((gameTime * 0.095F + hashFloat(s, 131)) % 1.0F);
                double topY = skyH * (1.0D - fallT * 0.72D);
                double botY = Math.max(0.08D, topY - 2.6D);
                boolean gold = (s % 2 == 0);
                renderCrossSegment(matrix, vc,
                        ground.add(sx + 0.35D, topY, sz + 0.18D),
                        ground.add(sx, botY, sz),
                        0.055F * fade,
                        gold ? 1.000F : 0.761F,
                        gold ? 0.918F : 0.698F,
                        gold ? 0.471F : 0.561F,
                        0.82F * fade);
            }

            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    fieldRad * 0.90F, fieldRad * 1.02F, 28, 0.761F, 0.698F, 0.561F, 0.86F * fade);
            renderOrientedStarPolygon(matrix, vc, ground.add(0.0D, 0.06D, 0.0D), right, forward,
                    fieldRad * 0.86F, 8, -gameTime * 0.040F, 1.000F, 0.918F, 0.471F, 0.82F * fade);
            return;
        }

        // Section 6.38 Spectral Rift Skull-Vortex Cataclysm (#41304E / #4D6453): dark ceiling rift & 12 swirling 3D spectral skulls across 12m!
        if (isIncorporealWraithMateria(inst.essence)) {
            double skyH = resolveTierScaledSkyfallHeight(ground, inst.tier, 9.8D);
            Vec3 riftTop = ground.add(0.0D, skyH, 0.0D);
            float fieldRad = Math.max(12.0F, radius);

            renderOrientedRing(matrix, vc, riftTop, right, forward, 0.0F, fieldRad * 0.88F, 28,
                    0.255F, 0.188F, 0.306F, 0.78F * fade);
            renderOrientedStarPolygon(matrix, vc, riftTop.add(0.0D, -0.14D, 0.0D), right, forward,
                    fieldRad * 0.82F, 8, -gameTime * 0.045F, 0.302F, 0.392F, 0.325F, 0.92F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    fieldRad * 0.90F, fieldRad * 1.02F, 32, 0.255F, 0.188F, 0.306F, 0.88F * fade);

            int skullCount = 12;
            for (int s = 0; s < skullCount; s++) {
                float orbitR = 2.2F + ((s * 2.1F + (gameTime * 0.12F)) % Math.max(2.5F, fieldRad - 2.4F));
                float ang = (gameTime * (0.085F + (s % 3) * 0.018F)) + (float) (Math.PI * 2.0D * s / skullCount);
                double h = 0.65D + ((s * 0.48D + 0.35D * Mth.sin(gameTime * 0.16F + s)) % Math.max(1.8D, Math.min(5.2D, skyH - 1.2D)));
                Vec3 skullPos = ground.add(Mth.cos(ang) * orbitR, h, Mth.sin(ang) * orbitR);
                Vec3 tangDir = new Vec3(-Mth.sin(ang), 0.0D, Mth.cos(ang));
                Vec3 outDir = new Vec3(Mth.cos(ang), 0.0D, Mth.sin(ang));
                Vec3 upVec = new Vec3(0.0D, 1.0D, 0.0D);

                renderSpectralSkullHead(matrix, vc, skullPos, tangDir, outDir, upVec, 0.22F, fade);
                Vec3 tailMid = skullPos.subtract(tangDir.scale(0.85D));
                Vec3 tailEnd = skullPos.subtract(tangDir.scale(1.95D)).add(outDir.scale(0.16D * Mth.sin(gameTime * 0.32F + s)));
                renderCrossSegment(matrix, vc, skullPos, tailMid, 0.20F, 0.255F, 0.188F, 0.306F, 0.86F * fade);
                renderCrossSegment(matrix, vc, tailMid, tailEnd, 0.10F, 0.302F, 0.392F, 0.325F, 0.72F * fade);
            }
            return;
        }

        // Section 6.39 Megalithic Ossuary Cataclysm Downpour (#773214 / #4D6453): tomb-cloud sky canopy & 10m catacomb sepulcher seal!
        if (isSepulchralBarrowMateria(inst.essence)) {
            double skyH = resolveTierScaledSkyfallHeight(ground, inst.tier, 10.2D);
            Vec3 barrowTop = ground.add(0.0D, skyH, 0.0D);
            float fieldRad = Math.max(10.0F, radius);

            renderOrientedRing(matrix, vc, barrowTop, right, forward, 0.0F, fieldRad * 0.96F, 24,
                    0.467F, 0.196F, 0.078F, 0.74F * fade);
            renderOrientedStarPolygon(matrix, vc, barrowTop.add(0.0D, -0.15D, 0.0D), right, forward,
                    fieldRad * 0.88F, 6, gameTime * 0.035F, 0.302F, 0.392F, 0.325F, 0.92F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    fieldRad * 0.90F, fieldRad * 1.02F, 28, 0.467F, 0.196F, 0.078F, 0.90F * fade);
            renderOrientedStarPolygon(matrix, vc, ground.add(0.0D, 0.06D, 0.0D), right, forward,
                    fieldRad * 0.86F, 6, -gameTime * 0.030F, 0.302F, 0.392F, 0.325F, 0.86F * fade);
            return;
        }
        // Section 6.40 Cremating Pyre Cataclysm Downpour (#773214 / #FF4500): volcanic wildfire canopy & 8m blazing hearth seal!
        if (isCrematingPyreMateria(inst.essence)) {
            double skyH = resolveTierScaledSkyfallHeight(ground, inst.tier, 10.5D);
            Vec3 pyreTop = ground.add(0.0D, skyH, 0.0D);
            float fieldRad = Math.max(8.0F, radius);

            renderOrientedRing(matrix, vc, pyreTop, right, forward, 0.0F, fieldRad * 0.96F, 24,
                    0.467F, 0.196F, 0.078F, 0.78F * fade);
            renderOrientedStarPolygon(matrix, vc, pyreTop.add(0.0D, -0.15D, 0.0D), right, forward,
                    fieldRad * 0.88F, 6, gameTime * 0.045F, 1.000F, 0.271F, 0.000F, 0.95F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    fieldRad * 0.90F, fieldRad * 1.02F, 28, 1.000F, 0.271F, 0.000F, 0.92F * fade);
            renderOrientedStarPolygon(matrix, vc, ground.add(0.0D, 0.06D, 0.0D), right, forward,
                    fieldRad * 0.86F, 6, -gameTime * 0.040F, 0.467F, 0.196F, 0.078F, 0.88F * fade);
            return;
        }
        // Section 6.41 Penumbral Cataclysmic Eclipse Tempest (#FFEA78 / #281E3C): 12m eclipsed corona canopy & twilight event horizon!
        if (isTwilightPenumbraMateria(inst.essence)) {
            double skyH = resolveTierScaledSkyfallHeight(ground, inst.tier, 11.2D);
            Vec3 penTop = ground.add(0.0D, skyH, 0.0D);
            float fieldRad = Math.max(12.0F, radius);

            renderOrientedRing(matrix, vc, penTop, right, forward, 0.0F, fieldRad * 0.76F, 32,
                    0.157F, 0.118F, 0.235F, 0.92F * fade);
            renderOrientedRing(matrix, vc, penTop, right, forward, fieldRad * 0.74F, fieldRad * 0.98F, 32,
                    1.000F, 0.918F, 0.471F, 0.95F * fade);
            renderOrientedStarPolygon(matrix, vc, penTop.add(0.0D, -0.15D, 0.0D), right, forward,
                    fieldRad * 0.90F, 8, gameTime * 0.045F, 1.000F, 0.918F, 0.471F, 0.92F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    fieldRad * 0.88F, fieldRad * 1.02F, 32, 1.000F, 0.918F, 0.471F, 0.92F * fade);
            renderOrientedStarPolygon(matrix, vc, ground.add(0.0D, 0.06D, 0.0D), right, forward,
                    fieldRad * 0.86F, 8, -gameTime * 0.040F, 0.157F, 0.118F, 0.235F, 0.94F * fade);
            return;
        }
        // Section 6.42 Glacial Rime Cataclysmic Blizzard (#6395B1 / #DCF0FF): 12m arctic blizzard canopy & permafrost snowflake seal!
        if (isHoarfrostRimeMateria(inst.essence)) {
            double skyH = resolveTierScaledSkyfallHeight(ground, inst.tier, 11.2D);
            Vec3 rimeTop = ground.add(0.0D, skyH, 0.0D);
            float fieldRad = Math.max(12.0F, radius);

            renderOrientedRing(matrix, vc, rimeTop, right, forward, 0.0F, fieldRad * 0.96F, 24,
                    0.388F, 0.584F, 0.694F, 0.82F * fade);
            renderOrientedStarPolygon(matrix, vc, rimeTop.add(0.0D, -0.15D, 0.0D), right, forward,
                    fieldRad * 0.90F, 6, gameTime * 0.045F, 0.863F, 0.941F, 1.000F, 0.95F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    fieldRad * 0.88F, fieldRad * 1.02F, 28, 0.863F, 0.941F, 1.000F, 0.92F * fade);
            renderOrientedStarPolygon(matrix, vc, ground.add(0.0D, 0.06D, 0.0D), right, forward,
                    fieldRad * 0.86F, 6, -gameTime * 0.040F, 0.388F, 0.584F, 0.694F, 0.90F * fade);
            return;
        }
        // Section 6.43 Vernal Miracle Downpour (#FF6B9D / #64C8FF): 12m springwater canopy & blooming lotus sanctuary seal!
        if (isVernalSpringMateria(inst.essence)) {
            double skyH = resolveTierScaledSkyfallHeight(ground, inst.tier, 11.2D);
            Vec3 springTop = ground.add(0.0D, skyH, 0.0D);
            float fieldRad = Math.max(12.0F, radius);

            renderOrientedRing(matrix, vc, springTop, right, forward, 0.0F, fieldRad * 0.96F, 28,
                    0.392F, 0.784F, 1.000F, 0.80F * fade);
            renderOrientedStarPolygon(matrix, vc, springTop.add(0.0D, -0.15D, 0.0D), right, forward,
                    fieldRad * 0.88F, 5, gameTime * 0.045F, 1.000F, 0.420F, 0.616F, 0.95F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    fieldRad * 0.88F, fieldRad * 1.02F, 32, 0.392F, 0.784F, 1.000F, 0.90F * fade);
            renderOrientedStarPolygon(matrix, vc, ground.add(0.0D, 0.06D, 0.0D), right, forward,
                    fieldRad * 0.85F, 5, -gameTime * 0.040F, 1.000F, 0.420F, 0.616F, 0.90F * fade);
            return;
        }
        // Section 6.44 Galvanic Tesla Cataclysmic Tempest (#FFE600 / #878DA2): 12m electrical storm canopy, lightning pillars & ionized field!
        if (isGalvanicStaticMateria(inst.essence)) {
            double skyH = resolveTierScaledSkyfallHeight(ground, inst.tier, 11.8D);
            Vec3 staticTop = ground.add(0.0D, skyH, 0.0D);
            float fieldRad = Math.max(12.0F, radius);

            renderOrientedRing(matrix, vc, staticTop, right, forward, 0.0F, fieldRad * 0.96F, 24,
                    0.529F, 0.553F, 0.635F, 0.82F * fade);
            renderOrientedStarPolygon(matrix, vc, staticTop.add(0.0D, -0.15D, 0.0D), right, forward,
                    fieldRad * 0.90F, 8, gameTime * 0.065F, 1.000F, 0.902F, 0.000F, 0.98F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    fieldRad * 0.88F, fieldRad * 1.02F, 24, 0.529F, 0.553F, 0.635F, 0.92F * fade);
            renderOrientedStarPolygon(matrix, vc, ground.add(0.0D, 0.06D, 0.0D), right, forward,
                    fieldRad * 0.86F, 8, -gameTime * 0.055F, 1.000F, 0.902F, 0.000F, 0.95F * fade);

            int frame = (int) (gameTime * 0.90F) + inst.seed * 23;
            for (int p = 0; p < 4; p++) {
                float pAng = (float) (Math.PI * 0.5D * p) + hashFloat(frame, p * 7) * 1.2F;
                float pDist = (0.20F + 0.65F * hashFloat(frame, p * 11 + 3)) * fieldRad;
                Vec3 topPt = staticTop.add(Mth.cos(pAng) * pDist, 0.0D, Mth.sin(pAng) * pDist);
                Vec3 midPt = ground.add(Mth.cos(pAng + 0.14F) * pDist, skyH * 0.50D, Mth.sin(pAng + 0.14F) * pDist);
                Vec3 botPt = ground.add(Mth.cos(pAng) * pDist, 0.05D, Mth.sin(pAng) * pDist);
                renderCrossSegment(matrix, vc, topPt, midPt, 0.18F * fade, 1.000F, 0.902F, 0.000F, 0.96F * fade);
                renderCrossSegment(matrix, vc, midPt, botPt, 0.18F * fade, 1.000F, 0.902F, 0.000F, 0.96F * fade);
            }
            return;
        }
        // Section 6.45 Sun-Canopy Orbital Meteor Barrage (#1A5923 / #773214 / #FFEA78): 10m solar canopy rift & entangled ironwood root seal!
        if (isPrimevalSylvanMateria(inst.essence)) {
            double skyH = resolveTierScaledSkyfallHeight(ground, inst.tier, 11.4D);
            Vec3 sylvanTop = ground.add(0.0D, skyH, 0.0D);
            float fieldRad = Math.max(10.0F, radius);

            renderOrientedRing(matrix, vc, sylvanTop, right, forward, 0.0F, fieldRad * 0.96F, 24,
                    0.102F, 0.349F, 0.137F, 0.82F * fade);
            renderOrientedStarPolygon(matrix, vc, sylvanTop.add(0.0D, -0.15D, 0.0D), right, forward,
                    fieldRad * 0.90F, 6, gameTime * 0.045F, 1.000F, 0.918F, 0.471F, 0.98F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    fieldRad * 0.88F, fieldRad * 1.02F, 24, 0.467F, 0.196F, 0.078F, 0.94F * fade);
            renderOrientedStarPolygon(matrix, vc, ground.add(0.0D, 0.06D, 0.0D), right, forward,
                    fieldRad * 0.86F, 6, -gameTime * 0.040F, 0.102F, 0.349F, 0.137F, 0.95F * fade);
            return;
        }
        // Section 6.46 Cataclysmic Miasmic Bog-Downpour (#1A5923 / #4D6453 / #225C7C): 12m virulent swamp-smog canopy & bubbling quagmire seal!
        if (isPestilentMiasmaMateria(inst.essence)) {
            double skyH = resolveTierScaledSkyfallHeight(ground, inst.tier, 11.6D);
            Vec3 miasmaTop = ground.add(0.0D, skyH, 0.0D);
            float fieldRad = Math.max(12.0F, radius);

            renderOrientedRing(matrix, vc, miasmaTop, right, forward, 0.0F, fieldRad * 0.96F, 24,
                    0.302F, 0.392F, 0.325F, 0.84F * fade);
            renderOrientedStarPolygon(matrix, vc, miasmaTop.add(0.0D, -0.15D, 0.0D), right, forward,
                    fieldRad * 0.88F, 6, gameTime * 0.042F, 0.133F, 0.361F, 0.486F, 0.96F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    fieldRad * 0.88F, fieldRad * 1.02F, 24, 0.133F, 0.361F, 0.486F, 0.94F * fade);
            renderOrientedStarPolygon(matrix, vc, ground.add(0.0D, 0.06D, 0.0D), right, forward,
                    fieldRad * 0.84F, 6, -gameTime * 0.038F, 0.102F, 0.349F, 0.137F, 0.95F * fade);
            return;
        }
        // Section 6.47 Cataclysmic Waters of Creation Downpour (#FF6B9D / #FFEA78 / #225C7C): 12m radiant dawn rift & shimmering sanctuary pool!
        if (isPrimordialGenesisMateria(inst.essence)) {
            double skyH = resolveTierScaledSkyfallHeight(ground, inst.tier, 11.6D);
            Vec3 genTop = ground.add(0.0D, skyH, 0.0D);
            float fieldRad = Math.max(12.0F, radius);

            renderOrientedRing(matrix, vc, genTop, right, forward, 0.0F, fieldRad * 0.96F, 24,
                    0.133F, 0.361F, 0.486F, 0.82F * fade);
            renderOrientedStarPolygon(matrix, vc, genTop.add(0.0D, -0.15D, 0.0D), right, forward,
                    fieldRad * 0.90F, 6, gameTime * 0.045F, 1.000F, 0.918F, 0.471F, 0.99F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    fieldRad * 0.88F, fieldRad * 1.02F, 24, 0.133F, 0.361F, 0.486F, 0.94F * fade);
            renderOrientedStarPolygon(matrix, vc, ground.add(0.0D, 0.06D, 0.0D), right, forward,
                    fieldRad * 0.86F, 6, -gameTime * 0.042F, 1.000F, 0.420F, 0.616F, 0.96F * fade);
            return;
        }
        // Section 6.48 Cataclysmic Stygian Meteor Downpour (#B83C08 / #211A21 / #4D6453): 12m abyssal hell-rift & smoldering wasteland seal!
        if (isStygianOblivionMateria(inst.essence)) {
            double skyH = resolveTierScaledSkyfallHeight(ground, inst.tier, 11.8D);
            Vec3 oblTop = ground.add(0.0D, skyH, 0.0D);
            float fieldRad = Math.max(12.0F, radius);

            renderOrientedRing(matrix, vc, oblTop, right, forward, 0.0F, fieldRad * 0.96F, 22,
                    0.129F, 0.102F, 0.129F, 0.92F * fade);
            renderOrientedStarPolygon(matrix, vc, oblTop.add(0.0D, -0.16D, 0.0D), right, forward,
                    fieldRad * 0.90F, 6, -gameTime * 0.048F, 0.722F, 0.235F, 0.031F, 0.99F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    fieldRad * 0.88F, fieldRad * 1.02F, 22, 0.129F, 0.102F, 0.129F, 0.96F * fade);
            renderOrientedStarPolygon(matrix, vc, ground.add(0.0D, 0.06D, 0.0D), right, forward,
                    fieldRad * 0.86F, 6, gameTime * 0.044F, 0.722F, 0.235F, 0.031F, 0.97F * fade);
            return;
        }
        // Section 6.50 Cataclysmic Firmament Skyfall (#A88654 / #773214 / #225C7C / #B83C08): 14m celestial firmament rift & inverted pressure seal!
        if (isQuintessentialAetherMateria(inst.essence)) {
            double skyH = resolveTierScaledSkyfallHeight(ground, inst.tier, 13.0D);
            Vec3 aethTop = ground.add(0.0D, skyH, 0.0D);
            float fieldRad = Math.max(14.0F, radius);

            renderOrientedRing(matrix, vc, aethTop, right, forward, 0.0F, fieldRad * 0.96F, 26,
                    0.133F, 0.361F, 0.486F, 0.82F * fade);
            renderOrientedStarPolygon(matrix, vc, aethTop.add(0.0D, -0.16D, 0.0D), right, forward,
                    fieldRad * 0.92F, 8, gameTime * 0.050F, 0.659F, 0.525F, 0.329F, 0.99F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    fieldRad * 0.88F, fieldRad * 1.02F, 26, 0.659F, 0.525F, 0.329F, 0.96F * fade);
            renderOrientedStarPolygon(matrix, vc, ground.add(0.0D, 0.06D, 0.0D), right, forward,
                    fieldRad * 0.86F, 8, -gameTime * 0.046F, 0.133F, 0.361F, 0.486F, 0.97F * fade);
            return;
        }
        // Section 6.51 Cataclysmic Entropic Skyfall (#41304E / #211A21 / #4D6453 / #8A0303): 14m gaping abyssal void tear & blood-slag crater!
        if (isAbyssalEntropicMateria(inst.essence)) {
            double skyH = resolveTierScaledSkyfallHeight(ground, inst.tier, 13.0D);
            Vec3 entTop = ground.add(0.0D, skyH, 0.0D);
            float fieldRad = Math.max(14.0F, radius);

            renderOrientedRing(matrix, vc, entTop, right, forward, 0.0F, fieldRad * 0.96F, 24,
                    0.129F, 0.102F, 0.129F, 0.94F * fade);
            renderOrientedStarPolygon(matrix, vc, entTop.add(0.0D, -0.16D, 0.0D), right, forward,
                    fieldRad * 0.92F, 8, -gameTime * 0.052F, 0.541F, 0.012F, 0.012F, 0.99F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    fieldRad * 0.88F, fieldRad * 1.02F, 24, 0.255F, 0.188F, 0.306F, 0.96F * fade);
            renderOrientedStarPolygon(matrix, vc, ground.add(0.0D, 0.06D, 0.0D), right, forward,
                    fieldRad * 0.86F, 8, gameTime * 0.046F, 0.541F, 0.012F, 0.012F, 0.97F * fade);
            return;
        }
        // Section 6.52 Cataclysmic Supernova Starfall (#FFEA78 / #FF6B9D / #1A5923 / #A88654): 14m consecrated white-hole firmament rift!
        if (isHolyCelestialMateria(inst.essence)) {
            double skyH = resolveTierScaledSkyfallHeight(ground, inst.tier, 13.0D);
            Vec3 celTop = ground.add(0.0D, skyH, 0.0D);
            float fieldRad = Math.max(14.0F, radius);

            renderOrientedRing(matrix, vc, celTop, right, forward, 0.0F, fieldRad * 0.96F, 24,
                    1.000F, 0.918F, 0.471F, 0.78F * fade);
            renderOrientedStarPolygon(matrix, vc, celTop.add(0.0D, -0.16D, 0.0D), right, forward,
                    fieldRad * 0.92F, 8, gameTime * 0.052F, 1.000F, 0.420F, 0.616F, 0.99F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    fieldRad * 0.88F, fieldRad * 1.02F, 24, 1.000F, 0.918F, 0.471F, 0.96F * fade);
            renderOrientedStarPolygon(matrix, vc, ground.add(0.0D, 0.06D, 0.0D), right, forward,
                    fieldRad * 0.86F, 8, -gameTime * 0.046F, 0.102F, 0.349F, 0.137F, 0.97F * fade);
            return;
        }

        // Crisp 8-tick (0.4s) orbital descent slams downward from skyTop to ground, then sustains full sky column!
        float elapsedTicks = progress * inst.maxTicks;
        float strikeT = Mth.clamp(elapsedTicks / 8.0F, 0.0F, 1.0F);
        Vec3 currentBottom = new Vec3(
                Mth.lerp(strikeT, skyTop.x, ground.x),
                Mth.lerp(strikeT, skyTop.y, ground.y),
                Mth.lerp(strikeT, skyTop.z, ground.z)
        );

        float pulse = 0.92F + 0.08F * Mth.sin(gameTime * 0.32F + inst.seed);
        float beamWidth = Math.max(0.26F, radius * 0.28F) * pulse * (0.35F + 0.65F * fade);

        // Overhead Celestial Sky-Rift Crown at skyTop so looking up reveals the open heavens!
        float crownRad = Math.max(beamWidth * 2.8F, radius * 0.88F);
        renderOrientedRing(matrix, vc, skyTop, right, forward, crownRad * 0.86F, crownRad, 30, pr, pg, pb, 0.80F * fade);
        renderOrientedRing(matrix, vc, skyTop.add(0.0D, -0.12D, 0.0D), right, forward,
                crownRad * 0.45F, crownRad * 0.56F, 24, ar, ag, ab, 0.75F * fade);
        renderOrientedStarPolygon(matrix, vc, skyTop.add(0.0D, -0.06D, 0.0D), right, forward,
                crownRad * 0.86F, 8, gameTime * 0.05F + inst.seed, 1.0F, 1.0F, 1.0F, 0.72F * fade);

        renderTube(matrix, vc, currentBottom, skyTop, beamWidth * 0.46F, 10, 1.0F, 1.0F, 1.0F, 0.94F * fade, FULL_LIGHT);
        renderTube(matrix, vc, currentBottom, skyTop, beamWidth * 1.15F, 12, pr, pg, pb, 0.52F * fade, FULL_LIGHT);

        // 4 continuously cascading sky halo collars descending the orbital beam
        for (int c = 0; c < 4; c++) {
            float collarT = ((gameTime * 0.055F + c * 0.25F) % 1.0F);
            double y = Mth.lerp(collarT, skyTop.y, ground.y + 0.2D);
            float cFade = fade * Mth.sin(collarT * (float) Math.PI);
            renderOrientedRing(matrix, vc, new Vec3(ground.x, y, ground.z), right, forward,
                    beamWidth * 1.45F, beamWidth * 2.25F, 22, ar, ag, ab, 0.74F * cFade);
        }

        // Ground impact dome & repeating shockwave rings once beam touches down
        if (elapsedTicks >= 6.0F) {
            renderHemisphereShell(matrix, vc, ground, radius * 0.68F * pulse, 6, 20, pr, pg, pb, 0.26F * fade);
            renderOrientedRing(matrix, vc, ground.add(0.0D, 0.05D, 0.0D), right, forward,
                    radius * 0.92F, radius * 1.00F, 30, pr, pg, pb, 0.66F * fade);

            for (int w = 0; w < 2; w++) {
                float wt = ((gameTime * 0.045F + w * 0.5F) % 1.0F);
                float shockR = radius * (0.22F + 0.82F * (1.0F - (1.0F - wt) * (1.0F - wt)));
                float wAlpha = (1.0F - wt) * 0.82F * fade;
                renderOrientedRing(matrix, vc, ground.add(0.0D, 0.06D, 0.0D), right, forward,
                        shockR * 0.82F, shockR, 28, ar, ag, ab, wAlpha);
            }
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
        boolean persistentTwoWayWormhole = inst.essence == EssenceType.VOID && inst.maxTicks >= 80;
        float fade = persistentTwoWayWormhole
                ? computeEnvelope(progress, 0.06F, 0.88F)
                : computeEnvelope(progress, 0.08F, 0.56F);
        float openScale = persistentTwoWayWormhole
                ? (0.72F + 0.28F * fade)
                : (0.35F + 0.65F * fade);
        Vec3 depCenter = inst.start.add(0.0D, 1.05D, 0.0D);
        Vec3 arrCenter = inst.endOrDir.add(0.0D, 1.05D, 0.0D);

        if (persistentTwoWayWormhole) {
            // Section 6.11 Wormhole Transit: twin full-height 5-second two-way cosmic wormhole doorways (#41304E)!
            renderVerticalDiamondPortalRim(matrix, vc, depCenter, camera, 0.72F * openScale, 1.52F * openScale,
                    0.255F, 0.188F, 0.306F, 0.780F, 0.490F, 1.000F, fade);
            renderVerticalDiamondPortalRim(matrix, vc, arrCenter, camera, 0.72F * openScale, 1.52F * openScale,
                    0.255F, 0.188F, 0.306F, 0.780F, 0.490F, 1.000F, fade);
            // Dark event-horizon portal interiors
            renderOctahedron(matrix, vc, depCenter, 0.32F * openScale, 0.045F, 0.025F, 0.070F, 0.94F * fade);
            renderOctahedron(matrix, vc, arrCenter, 0.32F * openScale, 0.045F, 0.025F, 0.070F, 0.94F * fade);
            if (depCenter.distanceToSqr(arrCenter) > 0.25D) {
                renderCrossSegment(matrix, vc, depCenter, arrCenter, 0.045F * openScale, 0.255F, 0.188F, 0.306F, 0.45F * fade);
                renderCrossSegment(matrix, vc, depCenter, arrCenter, 0.018F * openScale, 0.780F, 0.490F, 1.000F, 0.72F * fade);
            }
            return;
        }

        renderVerticalDiamondPortalRim(matrix, vc, depCenter, camera, 0.48F * openScale, 1.18F * openScale, pr, pg, pb, ar, ag, ab, fade);
        renderVerticalDiamondPortalRim(matrix, vc, arrCenter, camera, 0.54F * openScale, 1.28F * openScale, pr, pg, pb, ar, ag, ab, fade);

        // Sleek warp-streak ribbon connecting departure & arrival
        if (depCenter.distanceToSqr(arrCenter) > 0.25D) {
            renderCrossSegment(matrix, vc, depCenter, arrCenter, 0.08F * openScale, pr, pg, pb, 0.52F * fade);
            renderCrossSegment(matrix, vc, depCenter, arrCenter, 0.03F * openScale, 1.0F, 1.0F, 1.0F, 0.86F * fade);
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
        float inFrac = Math.min(0.12F, 10.0F / Math.max(1, inst.maxTicks));
        float outFrac = Math.max(0.62F, 1.0F - (18.0F / Math.max(1, inst.maxTicks)));
        float fade = computeEnvelope(progress, inFrac, outFrac);
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
        renderHemisphereShell(matrix, vc, center, r * 0.96F, 5, 16, pr, pg, pb, 0.20F * fade);
        renderLowerHemisphereShell(matrix, vc, center, r * 0.96F, 4, 16, pr, pg, pb, 0.14F * fade);

        // Air / Aura Slipstream Wind-Wall: 4 orbiting razor wind-blade crescents & ground Wind-Rose!
        if (isAerodynamicMateria(inst.essence)) {
            Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
            Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
            renderOrientedStarPolygon(matrix, vc, inst.start.add(0.0D, 0.05D, 0.0D), flatX, flatZ,
                    r * 1.05F, 8, -rot * 1.6F, ar, ag, ab, 0.72F * fade);
            for (int c = 0; c < 4; c++) {
                float cAng = gameTime * 0.24F + (float) (Math.PI * 0.5D * c);
                double yOff = (c % 2 == 0) ? 0.18D : -0.18D;
                renderSweptWindCrescent(matrix, vc, center.add(0.0D, yOff, 0.0D),
                        flatX, flatZ, r * 1.06F, cAng, 1.10F, 0.13F * r, ar, ag, ab, 0.88F * fade);
            }
        } else if (isHydroMateria(inst.essence)) {
            Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
            Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
            renderOrientedRing(matrix, vc, inst.start.add(0.0D, 0.05D, 0.0D), flatX, flatZ,
                    r * 0.88F, r * 1.12F, 26, 0.88F, 0.98F, 1.00F, 0.80F * fade);
            for (int b = 0; b < 3; b++) {
                double by = center.y - 0.35D + b * 0.35D + 0.06D * Mth.sin(gameTime * 0.20F + b);
                renderOrientedRing(matrix, vc, new Vec3(center.x, by, center.z), flatX, flatZ,
                        r * 0.96F, r * 1.05F, 24, 0.141F, 0.714F, 1.000F, 0.68F * fade);
            }
        } else if (isBotanicalMateria(inst.essence) || isOvergrowthRampartMateria(inst.essence)) {
            // Section 6.4 Overgrowth Snare & Section 6.17 Strangler Briar Cage: 12 dense gnarled 3D wooden roots erupt into an entangled dome cage!
            Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
            Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
            float erupt = Mth.clamp((progress * Math.max(1, inst.maxTicks)) / 8.0F, 0.0F, 1.0F);
            double cageH = Math.max(2.6D, r * 0.86D) * (0.28D + 0.72D * erupt);
            Vec3 floor = inst.start.add(0.0D, 0.04D, 0.0D);

            int rootBars = 12;
            for (int bar = 0; bar < rootBars; bar++) {
                float baseAng = (float) (Math.PI * 2.0D * bar / rootBars) + inst.seed * 0.11F;
                float twistDir = (bar % 2 == 0) ? 0.36F : -0.36F;
                Vec3 prevPt = null;
                for (int s = 0; s <= 10; s++) {
                    float u = s / 10.0F;
                    // Dome arch profile from perimeter (r) up to interlocking root crown!
                    float phi = u * (float) (Math.PI * 0.48D);
                    double rad = r * Math.cos(phi);
                    double y = cageH * Math.sin(phi);
                    float ang = baseAng + twistDir * u + 0.05F * Mth.sin(u * 9.0F + bar);
                    Vec3 pt = floor.add(Mth.cos(ang) * rad, y, Mth.sin(ang) * rad);
                    if (prevPt != null) {
                        float thick = (0.145F - 0.055F * u) * (0.5F + 0.5F * fade);
                        renderCrossSegment(matrix, vc, prevPt, pt, thick,
                                0.36F, 0.23F, 0.12F, 0.94F * fade);
                        renderCrossSegment(matrix, vc, prevPt, pt, thick * 0.52F,
                                0.102F, 0.349F, 0.137F, 0.88F * fade);
                    }
                    // Inward-pointing strangling briar thorns inside the cage!
                    if (s > 1 && s < 9 && s % 2 == 0) {
                        Vec3 inDir = new Vec3(-Mth.cos(ang), -0.18D, -Mth.sin(ang)).normalize();
                        renderCrossSegment(matrix, vc, pt, pt.add(inDir.scale(0.32D)), 0.034F,
                                0.71F, 1.00F, 0.47F, 0.90F * fade);
                    }
                    prevPt = pt;
                }
            }

            // 3 horizontal thorny briar hoops binding the wooden root bars together
            for (int h = 0; h < 3; h++) {
                float phi = (0.18F + h * 0.24F) * (float) (Math.PI * 0.5D);
                float hoopR = r * Mth.cos(phi);
                double hoopY = cageH * Mth.sin(phi);
                renderOrientedRing(matrix, vc, floor.add(0.0D, hoopY, 0.0D), flatX, flatZ,
                        hoopR * 0.93F, hoopR * 1.03F, 26,
                        (h == 1) ? 0.17F : 0.102F, (h == 1) ? 0.90F : 0.349F, (h == 1) ? 0.41F : 0.137F,
                        0.86F * fade);
            }
        } else if (isTectonicMateria(inst.essence)) {
            Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
            Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
            renderOrientedStarPolygon(matrix, vc, inst.start.add(0.0D, 0.05D, 0.0D), flatX, flatZ,
                    r * 1.04F, 8, -rot * 1.2F, 1.00F, 0.76F, 0.47F, 0.80F * fade);
            for (int s = 0; s < 6; s++) {
                float sAng = gameTime * 0.09F + (float) (Math.PI * 2.0D * s / 6.0D);
                Vec3 outN = new Vec3(Mth.cos(sAng), 0.0D, Mth.sin(sAng));
                Vec3 tU = new Vec3(-Mth.sin(sAng), 0.0D, Mth.cos(sAng));
                Vec3 tV = new Vec3(0.0D, 1.0D, 0.0D);
                renderFacetedHexPlate(matrix, vc, center.add(outN.scale(r * 1.04D)), tU, tV, r * 0.30F, (float) (Math.PI / 6.0D),
                        (s % 2 == 0) ? 0.467F : 0.353F, (s % 2 == 0) ? 0.196F : 0.322F, (s % 2 == 0) ? 0.078F : 0.298F,
                        0.72F * fade, 1.00F, 0.76F, 0.47F, 0.94F * fade);
            }
        } else if (isGlacialMateria(inst.essence)) {
            // Section 6.6 Flash-Freeze Stasis & Frostwalker Stasis: 6-sided translucent solid Crystalline Ice Block Prism!
            Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
            Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
            Vec3 floor = inst.start.add(0.0D, 0.03D, 0.0D);
            double prismH = Math.max(2.05D, r * 1.75D);
            float prismR = Math.max(0.68F, r * 0.82F);
            renderSnowflakeCrystal(matrix, vc, floor.add(0.0D, 0.02D, 0.0D), flatX, flatZ,
                    prismR * 1.28F, 0.0F, 0.88F * fade);
            for (int i = 0; i < 6; i++) {
                float a0 = (float) (Math.PI * 2.0D * i / 6.0D);
                float a1 = (float) (Math.PI * 2.0D * (i + 1) / 6.0D);
                Vec3 b0 = floor.add(Mth.cos(a0) * prismR, 0.0D, Mth.sin(a0) * prismR);
                Vec3 b1 = floor.add(Mth.cos(a1) * prismR, 0.0D, Mth.sin(a1) * prismR);
                Vec3 t1 = floor.add(Mth.cos(a1) * prismR * 0.90D, prismH, Mth.sin(a1) * prismR * 0.90D);
                Vec3 t0 = floor.add(Mth.cos(a0) * prismR * 0.90D, prismH, Mth.sin(a0) * prismR * 0.90D);
                Vec3 apex = floor.add(0.0D, prismH + 0.38D, 0.0D);
                boolean cyanFace = (i % 2 == 0);
                float cr = cyanFace ? 0.596F : 0.173F;
                float cg = cyanFace ? 0.863F : 0.510F;
                float cb = cyanFace ? 0.949F : 0.788F;
                emitDoubleSidedFlatQuad(matrix, vc, b0, b1, t1, t0, cr, cg, cb, 0.44F * fade);
                emitDoubleSidedFlatQuad(matrix, vc, t0, t1, apex, apex, 0.88F, 0.97F, 1.00F, 0.52F * fade);
                renderCrossSegment(matrix, vc, b0, t0, 0.024F, 0.94F, 0.99F, 1.00F, 0.92F * fade);
            }
        } else if (isSolarRadiantMateria(inst.essence)) {
            // Section 6.8 Sunburst Confinement Sigil: 3-second blinding cage of 12 vertical radiant light beams & 3 binding solar halo hoops!
            Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
            Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
            Vec3 floor = inst.start.add(0.0D, 0.03D, 0.0D);
            double cageH = Math.max(2.45D, r * 1.45D);
            float cageR = Math.max(0.88F, r * 0.92F);
            renderOrientedStarPolygon(matrix, vc, floor.add(0.0D, 0.02D, 0.0D), flatX, flatZ,
                    cageR * 1.12F, 12, gameTime * 0.04F, 1.00F, 0.886F, 0.471F, 0.92F * fade);

            for (int h = 0; h < 3; h++) {
                double hy = (h / 2.0D) * (cageH - 0.18D) + 0.08D;
                renderOrientedRing(matrix, vc, floor.add(0.0D, hy, 0.0D), flatX, flatZ,
                        cageR * 0.92F, cageR * 1.03F, 24, 1.00F, 0.886F, 0.471F, 0.92F * fade);
            }

            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
            for (int b = 0; b < 12; b++) {
                float bAng = (float) (Math.PI * 2.0D * b / 12.0D);
                Vec3 rad = new Vec3(Mth.cos(bAng), 0.0D, Mth.sin(bAng));
                Vec3 tang = new Vec3(-Mth.sin(bAng), 0.0D, Mth.cos(bAng));
                Vec3 barBot = floor.add(rad.scale(cageR));
                Vec3 barTop = barBot.add(0.0D, cageH, 0.0D);
                renderCrossSegment(matrix, vc, barBot, barTop, 0.072F, 1.00F, 0.886F, 0.471F, 0.72F * fade);
                renderCrossSegment(matrix, vc, barBot, barTop, 0.032F, 1.00F, 0.995F, 0.96F, 0.98F * fade);
                renderSolarStarburstRay(matrix, vc, barTop, worldUp, rad, tang, 0.085F, 0.42F, fade);
            }
        } else if (isNecroticUndeadMateria(inst.essence)) {
            // Section 6.10 Grave-Phasing Departure Ribcage Root: 12 curved 3D skeletal ribs erupting from the earth
            // and interlocking overhead into a clutching ossuary ribcage prison bound by #5EE088 necrotic soul rings!
            Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
            Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
            Vec3 floor = inst.start.add(0.0D, 0.03D, 0.0D);
            float erupt = Mth.clamp((progress * Math.max(1, inst.maxTicks)) / 7.0F, 0.0F, 1.0F);
            float cageR = Math.max(0.95F, r * 0.96F);
            float ribLen = Math.max(2.15F, r * 1.28F) * (0.28F + 0.72F * erupt);
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);

            renderOrientedRing(matrix, vc, floor.add(0.0D, 0.02D, 0.0D), flatX, flatZ,
                    cageR * 0.84F, cageR * 1.05F, 26, 0.302F, 0.392F, 0.325F, 0.90F * fade);
            renderOrientedRing(matrix, vc, floor.add(0.0D, ribLen * 0.48D, 0.0D), flatX, flatZ,
                    cageR * 0.88F, cageR * 0.98F, 24, 0.369F, 0.878F, 0.533F, 0.84F * fade);

            int ribBars = 12;
            for (int bar = 0; bar < ribBars; bar++) {
                float bAng = (float) (Math.PI * 2.0D * bar / ribBars);
                Vec3 rad = new Vec3(Mth.cos(bAng), 0.0D, Mth.sin(bAng));
                Vec3 tang = new Vec3(-Mth.sin(bAng), 0.0D, Mth.cos(bAng));
                Vec3 ribBase = floor.add(rad.scale(cageR * 0.96D));
                Vec3 axisDir = worldUp.scale(0.88D).add(rad.scale(0.18D)).normalize();
                Vec3 bendInward = rad.scale(-0.92D).add(worldUp.scale(0.25D)).normalize();
                renderSkeletalRibSpike(matrix, vc, ribBase, axisDir, bendInward, tang,
                        Math.max(0.095F, cageR * 0.068F), ribLen, fade);
            }
        } else if (isAridDesertMateria(inst.essence)) {
            // Section 6.12 Mirage Step & Heat Mirage Decoy Dust Swirl: whirling #C2B28F desert dust-devil funnel & sand crescents!
            Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
            Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
            Vec3 floor = inst.start.add(0.0D, 0.04D, 0.0D);
            double funnelH = Math.max(2.15D, r * 1.45D);
            renderOrientedFrustumBand(matrix, vc, floor, floor.add(0.0D, funnelH, 0.0D),
                    flatX, flatZ, Math.max(0.28F, r * 0.42F), Math.max(0.85F, r * 1.12F), 24,
                    0.761F, 0.698F, 0.561F, 0.64F * fade, 0.24F * fade);
            for (int c = 0; c < 6; c++) {
                float cAng = gameTime * 0.34F + (float) (Math.PI * 2.0D * c / 6.0D);
                double cy = 0.25D + (c / 5.0D) * (funnelH - 0.35D);
                float cR = Mth.lerp(c / 5.0F, Math.max(0.35F, r * 0.48F), Math.max(0.88F, r * 1.08F));
                renderSweptWindCrescent(matrix, vc, floor.add(0.0D, cy, 0.0D),
                        flatX, flatZ, cR, cAng, 1.05F, 0.18F,
                        0.961F, 0.894F, 0.745F, 0.88F * fade);
            }
        }
    }

    /**
     * Renders a continuous, entity-tracked 3D Ward or Aegis Barrier Shell that stays visibly active
     * for the full duration of the shield/mantle, surges dramatically in opacity and directional
     * shockwave ripples when absorbing hits, and bursts outward cleanly when shattered.
     */
    private static void renderPersistentEntityWard(
            Minecraft mc,
            Matrix4f matrix,
            VertexConsumer vc,
            SpellWardClientManager.WardInstance ward,
            float partialTick,
            float gameTime
    ) {
        if (mc.level == null) return;
        Entity entity = mc.level.getEntity(ward.entityId);
        if (entity == null || !entity.isAlive() || entity.isRemoved()) {
            return;
        }

        float env = ward.getEnvelope(partialTick);
        if (env <= 0.005F) {
            return;
        }

        Vec3 feetPos = entity.getPosition(partialTick);
        float entityHeight = Math.max(0.90F, entity.getBbHeight());
        float entityWidth = Math.max(0.60F, entity.getBbWidth());
        Vec3 center = feetPos.add(0.0D, entityHeight * 0.52D, 0.0D);
        float baseRadius = Math.max(0.92F, Math.max(entityWidth * 0.95F, entityHeight * 0.58F));

        float flare = ward.getFlare(partialTick);
        float flare01 = Mth.clamp(flare / 1.15F, 0.0F, 1.0F);
        float shieldRatio = ward.getShieldRatio(partialTick);
        float lowShieldPulse = shieldRatio < 0.35F
                ? (1.0F + 0.24F * Mth.sin(gameTime * 0.58F + ward.entityId))
                : 1.0F;

        float r;
        float shatterOutward = 0.0F;
        if (ward.isShattering()) {
            float sProg = ward.getShatterProgress(partialTick);
            shatterOutward = 1.0F - (1.0F - sProg) * (1.0F - sProg);
            r = baseRadius * (1.0F + 0.62F * shatterOutward);
        } else {
            float breath = 1.0F + 0.018F * Mth.sin(gameTime * 0.09F + ward.entityId);
            float impactRecoil = 0.065F * Mth.sin(flare01 * (float) Math.PI);
            r = baseRadius * (0.96F + 0.04F * env) * breath + impactRecoil;
        }

        int pRgb = SpellParticleHelper.resolveVibrantPrimaryRgb(ward.primaryMateria, gameTime + ward.entityId * 7);
        int aRgb = SpellParticleHelper.resolveVibrantAccentRgb(ward.primaryMateria, ward.secondaryMateria, gameTime + ward.entityId * 7);

        float pr = ((pRgb >> 16) & 0xFF) / 255.0F;
        float pg = ((pRgb >> 8) & 0xFF) / 255.0F;
        float pb = (pRgb & 0xFF) / 255.0F;

        float ar = ((aRgb >> 16) & 0xFF) / 255.0F;
        float ag = ((aRgb >> 8) & 0xFF) / 255.0F;
        float ab = (aRgb & 0xFF) / 255.0F;

        // Blend dome tint toward incandescent accent/white as hit-flare peaks!
        float sr = Mth.lerp(flare01 * 0.68F, pr, ar * 0.58F + 0.42F);
        float sg = Mth.lerp(flare01 * 0.68F, pg, ag * 0.58F + 0.42F);
        float sb = Mth.lerp(flare01 * 0.68F, pb, ab * 0.58F + 0.42F);

        boolean localFirstPerson = (entity == mc.player && mc.options.getCameraType().isFirstPerson());

        float upperShellAlpha;
        float lowerShellAlpha;
        float ringAlpha;
        float groundSealAlpha;

        if (localFirstPerson) {
            // Keep resting 1st-person crosshair crystal-clear while still showing peripheral barrier & surging brightly on hit!
            upperShellAlpha = Mth.clamp(Mth.lerp(flare01, 0.042F, 0.44F) * env * lowShieldPulse, 0.0F, 0.85F);
            lowerShellAlpha = Mth.clamp(Mth.lerp(flare01, 0.065F, 0.48F) * env * lowShieldPulse, 0.0F, 0.85F);
            ringAlpha = Mth.clamp(Mth.lerp(flare01, 0.36F, 0.94F) * env * lowShieldPulse, 0.0F, 0.98F);
            groundSealAlpha = Mth.clamp(Mth.lerp(flare01, 0.46F, 0.92F) * env, 0.0F, 0.98F);
        } else {
            // Distinct, unmistakable persistent 3D barrier shell in 3rd-person & on allies/Eidolons/foes, surging >4x on hit!
            upperShellAlpha = Mth.clamp(Mth.lerp(flare01, 0.165F, 0.72F) * env * lowShieldPulse, 0.0F, 0.92F);
            lowerShellAlpha = Mth.clamp(Mth.lerp(flare01, 0.125F, 0.56F) * env * lowShieldPulse, 0.0F, 0.90F);
            ringAlpha = Mth.clamp(Mth.lerp(flare01, 0.54F, 0.98F) * env * lowShieldPulse, 0.0F, 0.99F);
            groundSealAlpha = Mth.clamp(Mth.lerp(flare01, 0.46F, 0.94F) * env, 0.0F, 0.98F);
        }

        // Special Full-Body 3D Solid Crystalline Ice Block Encasement (#5 Frozen Stasis — Flash-Freeze, Frost Nova, Cryo-Ray, Glacial Eidolon)!
        // Stationary chiseled hexagonal Glacial Cyan (#98DCF2) & Permafrost Blue (#2C82C9) crystal prism encasing the frozen victim!
        if (ward.primaryMateria == EssenceType.FROZEN && ward.secondaryMateria == EssenceType.STASIS) {
            float fixedYaw = (ward.entityId * 0.618F) % ((float) Math.PI * 2.0F);
            Vec3 iceU = new Vec3(Mth.cos(fixedYaw), 0.0D, Mth.sin(fixedYaw));
            Vec3 iceV = new Vec3(-Mth.sin(fixedYaw), 0.0D, Mth.cos(fixedYaw));
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);

            float expand = 1.0F + 0.55F * shatterOutward;
            float blockRad = Math.max(0.56F, entity.getBbWidth() * 0.74F) * expand;
            double blockH = Math.max(1.25D, entityHeight + 0.24D) * (1.0D + 0.12D * shatterOutward);
            Vec3 floor = feetPos.add(0.0D, 0.03D, 0.0D);
            Vec3 mid = floor.add(0.0D, blockH * 0.52D, 0.0D);
            Vec3 top = floor.add(0.0D, blockH, 0.0D);

            float wallAlpha = Mth.clamp(Mth.lerp(flare01, localFirstPerson ? 0.16F : 0.46F, 0.78F) * env, 0.0F, 0.92F);
            float coreAlpha = Mth.clamp(Mth.lerp(flare01, localFirstPerson ? 0.10F : 0.30F, 0.56F) * env, 0.0F, 0.85F);
            float edgeAlpha = Mth.clamp(Mth.lerp(flare01, localFirstPerson ? 0.42F : 0.86F, 0.99F) * env, 0.0F, 0.99F);

            // 1. Underfoot 6-pointed Snowflake Seal anchoring the ice block to the ground
            renderSnowflakeCrystal(matrix, vc, floor, iceU, iceV, blockRad * 1.24F, fixedYaw, edgeAlpha * 0.88F);

            // 2. Inner Permafrost Blue (#2C82C9) refrigerated core + Outer Glacial Cyan (#98DCF2) 6-sided crystal prism walls
            renderOrientedFrustumBand(matrix, vc, floor.add(0.0D, 0.04D, 0.0D), top.add(0.0D, -0.08D, 0.0D), iceU, iceV,
                    blockRad * 0.84F, blockRad * 0.78F, 6, 0.173F, 0.510F, 0.788F, coreAlpha, coreAlpha * 0.86F);
            renderOrientedFrustumBand(matrix, vc, floor, mid, iceU, iceV,
                    blockRad * 1.04F, blockRad, 6, 0.596F, 0.863F, 0.949F, wallAlpha * 1.08F, wallAlpha);
            renderOrientedFrustumBand(matrix, vc, mid, top, iceU, iceV,
                    blockRad, blockRad * 0.92F, 6, 0.596F, 0.863F, 0.949F, wallAlpha, wallAlpha * 0.92F);

            // 3. Pyramidal crystalline ice crown cap sealing the top of the prism
            if (!localFirstPerson || flare01 > 0.18F) {
                renderFacetedIceSpire(matrix, vc, top, worldUp, iceU, iceV,
                        blockRad * 0.92F, Math.max(0.32F, blockRad * 0.52F), 6, 0.0F, wallAlpha * 1.12F);
            }

            // 4. Specular Diamond-Frost (#EBF9FF) vertical corner ribs, bevel rims & internal diagonal frost-fracture veins
            renderOrientedRing(matrix, vc, floor.add(0.0D, 0.02D, 0.0D), iceU, iceV, blockRad * 0.96F, blockRad * 1.06F, 6,
                    0.922F, 0.976F, 1.000F, edgeAlpha);
            renderOrientedRing(matrix, vc, top, iceU, iceV, blockRad * 0.85F, blockRad * 0.94F, 6,
                    0.922F, 0.976F, 1.000F, edgeAlpha * 0.92F);

            if (!localFirstPerson || flare01 > 0.12F) {
                for (int i = 0; i < 6; i++) {
                    float a0 = (float) (Math.PI * 2.0D * i / 6.0D);
                    float a1 = (float) (Math.PI * 2.0D * ((i + 1) % 6) / 6.0D);
                    Vec3 dir0 = iceU.scale(Mth.cos(a0)).add(iceV.scale(Mth.sin(a0)));
                    Vec3 dir1 = iceU.scale(Mth.cos(a1)).add(iceV.scale(Mth.sin(a1)));
                    Vec3 b0 = floor.add(dir0.scale(blockRad * 1.04D));
                    Vec3 t0 = top.add(dir0.scale(blockRad * 0.92D));
                    Vec3 m1 = mid.add(dir1.scale(blockRad));
                    renderCrossSegment(matrix, vc, b0, t0, 0.024F, 0.922F, 0.976F, 1.000F, edgeAlpha);
                    if (i % 2 == 0) {
                        renderCrossSegment(matrix, vc, b0.add(0.0D, blockH * 0.16D, 0.0D), m1,
                                0.016F + 0.014F * flare01, 0.922F, 0.976F, 1.000F, edgeAlpha * (0.62F + 0.36F * flare01));
                    }
                    // Basal rime anchor spikes gripping the ground around the solid ice block!
                    Vec3 tang = iceU.scale(-Mth.sin(a0)).add(iceV.scale(Mth.cos(a0)));
                    Vec3 spikeTilt = worldUp.add(dir0.scale(0.62D)).normalize();
                    renderFacetedIceSpire(matrix, vc, floor.add(dir0.scale(blockRad * 0.95D)), spikeTilt, dir0, tang,
                            blockRad * 0.18F, blockRad * 0.56F, 5, a0, edgeAlpha * 0.88F);
                }
            }
            return;
        }

        // Special Full-Body 3D Invulnerable Cryo-Cocoon Chrysalis (#15 Glacial — Cryo-Cocoon Mending, Section 6.16)!
        // Faceted octagonal Pale Cryo-Blue (#98DCF2) & Diamond-Frost (#E0FFFF) regenerating ice cocoon!
        if (ward.primaryMateria == EssenceType.GLACIAL && ward.secondaryMateria == EssenceType.FROZEN) {
            float cocoonYaw = gameTime * 0.035F + (ward.entityId * 0.37F);
            Vec3 cocU = new Vec3(Mth.cos(cocoonYaw), 0.0D, Mth.sin(cocoonYaw));
            Vec3 cocV = new Vec3(-Mth.sin(cocoonYaw), 0.0D, Mth.cos(cocoonYaw));
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);

            float expand = 1.0F + 0.38F * shatterOutward;
            float cocRad = Math.max(0.58F, entity.getBbWidth() * 0.78F) * expand;
            double cocH = Math.max(1.35D, entityHeight + 0.30D);
            Vec3 floor = feetPos.add(0.0D, 0.03D, 0.0D);
            Vec3 waist = floor.add(0.0D, cocH * 0.48D, 0.0D);
            Vec3 shoulder = floor.add(0.0D, cocH * 0.84D, 0.0D);

            float shellAlpha = Mth.clamp(Mth.lerp(flare01, localFirstPerson ? 0.14F : 0.48F, 0.82F) * env, 0.0F, 0.94F);
            float haloAlpha = Mth.clamp(Mth.lerp(flare01, localFirstPerson ? 0.36F : 0.86F, 0.99F) * env, 0.0F, 0.99F);

            renderSnowflakeCrystal(matrix, vc, floor, cocU, cocV, cocRad * 1.28F, cocoonYaw, haloAlpha * 0.90F);
            renderOrientedFrustumBand(matrix, vc, floor, waist, cocU, cocV,
                    cocRad * 0.72F, cocRad * 1.06F, 8, 0.596F, 0.863F, 0.949F, shellAlpha * 0.95F, shellAlpha);
            renderOrientedFrustumBand(matrix, vc, waist, shoulder, cocU, cocV,
                    cocRad * 1.06F, cocRad * 0.74F, 8, 0.596F, 0.863F, 0.949F, shellAlpha, shellAlpha * 0.92F);
            if (!localFirstPerson || flare01 > 0.16F) {
                renderFacetedIceSpire(matrix, vc, shoulder, worldUp, cocU, cocV,
                        cocRad * 0.74F, Math.max(0.36F, cocRad * 0.56F), 8, cocoonYaw, shellAlpha * 1.10F);
            }
            renderOrientedRing(matrix, vc, waist, cocU, cocV, cocRad * 0.98F, cocRad * 1.10F, 16,
                    0.88F, 1.00F, 1.00F, haloAlpha);
            renderOrientedRing(matrix, vc, shoulder, cocU, cocV, cocRad * 0.68F, cocRad * 0.78F, 16,
                    0.50F, 1.00F, 0.90F, haloAlpha * 0.88F);
            return;
        }

        // 1. Continuous 3D Upper + Lower Barrier Dome Shell
        renderHemisphereShell(matrix, vc, center, r * 0.97F, 5, 18, sr, sg, sb, upperShellAlpha);
        renderLowerHemisphereShell(matrix, vc, center, r * 0.97F, 4, 18, sr, sg, sb, lowerShellAlpha);

        // Inner incandescent core flash dome when absorbing a hit or shattering
        if (flare01 > 0.06F) {
            float coreFlashAlpha = (localFirstPerson ? 0.22F : 0.38F) * flare01 * env;
            renderHemisphereShell(matrix, vc, center, r * 0.90F, 4, 14,
                    ar * 0.45F + 0.55F, ag * 0.45F + 0.55F, ab * 0.45F + 0.55F, coreFlashAlpha);
        }

        // 2. Rotating Runic Meridian Hoops (thicken & accelerate slightly on impact!)
        float rot = gameTime * 0.055F + ward.entityId * 1.37F + flare01 * 0.28F;
        Vec3 rx = new Vec3(Mth.cos(rot), 0.0D, Mth.sin(rot));
        Vec3 rz = new Vec3(-Mth.sin(rot), 0.0D, Mth.cos(rot));
        Vec3 ry = new Vec3(0.0D, 1.0D, 0.0D);

        float hoopThickness = r * (0.048F + 0.032F * flare01);
        float hoopIn = Math.max(0.05F, r - hoopThickness);

        // Equatorial waist hoop (always visible)
        renderOrientedRing(matrix, vc, center, rx, rz, hoopIn, r, 28, pr, pg, pb, ringAlpha);

        if (!localFirstPerson || flare01 > 0.12F) {
            float vertFactor = localFirstPerson ? flare01 : 1.0F;
            Vec3 tiltA = rx.scale(0.866D).add(0.0D, 0.5D, 0.0D).normalize();
            Vec3 tiltB = rz.scale(0.866D).add(0.0D, -0.5D, 0.0D).normalize();
            renderOrientedRing(matrix, vc, center, tiltA, rz, hoopIn, r, 24, ar, ag, ab, ringAlpha * 0.88F * vertFactor);
            renderOrientedRing(matrix, vc, center, rx, tiltB, hoopIn, r, 24,
                    ar * 0.5F + 0.5F, ag * 0.5F + 0.5F, ab * 0.5F + 0.5F, ringAlpha * 0.76F * vertFactor);
        }

        // 3. Entity-Tracked Ground Ward / Aegis / Wind-Rose Seal
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        Vec3 sealPos = feetPos.add(0.0D, 0.04D, 0.0D);
        boolean aerodynamic = isAerodynamicMateria(ward.primaryMateria) || ward.secondaryMateria == EssenceType.AIR;
        boolean aegisThemed = ward.primaryMateria == EssenceType.AEGIS || ward.secondaryMateria == EssenceType.AEGIS;

        renderOrientedRing(matrix, vc, sealPos, flatX, flatZ, baseRadius * 0.94F, baseRadius * 1.02F, 24,
                pr, pg, pb, groundSealAlpha * 0.88F);
        renderOrientedStarPolygon(matrix, vc, sealPos, flatX, flatZ,
                baseRadius * 0.92F, aerodynamic ? 8 : 6, -rot * 1.35F, ar, ag, ab, groundSealAlpha);

        // 4. Orbiting Faceted Hexagonal Bastion Plates (prominent golden plates for Aegis, crystalline facets for Wards)
        int plateCount = aegisThemed ? 6 : 4;
        float plateFillAlpha = (aegisThemed
                ? Mth.lerp(flare01, localFirstPerson ? 0.10F : 0.26F, localFirstPerson ? 0.45F : 0.74F)
                : Mth.lerp(flare01, localFirstPerson ? 0.05F : 0.13F, localFirstPerson ? 0.38F : 0.58F)) * env;
        float plateRimAlpha = (aegisThemed
                ? Mth.lerp(flare01, localFirstPerson ? 0.35F : 0.66F, 0.98F)
                : Mth.lerp(flare01, localFirstPerson ? 0.22F : 0.42F, 0.94F)) * env;
        float plateRad = r * (aegisThemed ? 0.30F : 0.23F) * (1.0F + 0.12F * flare01);

        for (int p = 0; p < plateCount; p++) {
            float pAng = rot * (aegisThemed ? 0.95F : 1.15F) + (float) (Math.PI * 2.0D * p / plateCount);
            float bobLat = 0.16F * Mth.sin(gameTime * 0.08F + p * 1.7F) + shatterOutward * ((p % 2 == 0) ? 0.28F : -0.28F);
            Vec3 normal = new Vec3(
                    Mth.cos(pAng) * Mth.cos(bobLat),
                    Mth.sin(bobLat),
                    Mth.sin(pAng) * Mth.cos(bobLat)
            ).normalize();
            Vec3 pTanU = buildPerpendicularRight(normal);
            Vec3 pTanV = normal.cross(pTanU).normalize();
            Vec3 plateCenter = center.add(normal.scale(r * (1.01D + 0.25D * shatterOutward)));
            renderFacetedHexPlate(
                    matrix, vc, plateCenter, pTanU, pTanV, plateRad,
                    shatterOutward * 1.4F + p * 0.52F,
                    sr, sg, sb, plateFillAlpha,
                    ar, ag, ab, plateRimAlpha
            );
        }

        // Air / Aura Slipstream Wind-Wall: 4 orbiting razor wind-blade crescents!
        if (aerodynamic) {
            float bladeAlpha = Mth.lerp(flare01, localFirstPerson ? 0.34F : 0.62F, 0.96F) * env;
            for (int c = 0; c < 4; c++) {
                float cAng = gameTime * (0.22F + 0.10F * flare01) + (float) (Math.PI * 0.5D * c);
                double yOff = (c % 2 == 0) ? 0.20D : -0.20D;
                renderSweptWindCrescent(matrix, vc, center.add(0.0D, yOff, 0.0D),
                        flatX, flatZ, r * 1.05F, cAng, 1.05F, 0.12F * r, ar, ag, ab, bladeAlpha);
            }
        } else if (isHydroMateria(ward.primaryMateria) || ward.secondaryMateria == EssenceType.WATER) {
            // Section 6.3 Oceanic Shroud & Tidal Bulwark: undulating azure ocean-current bands & rising 3D water bubbles!
            float tideAlpha = Mth.lerp(flare01, localFirstPerson ? 0.28F : 0.58F, 0.95F) * env;
            for (int band = -1; band <= 1; band += 2) {
                double yOff = band * (r * 0.28D) + 0.05D * Mth.sin(gameTime * 0.15F + band);
                float bandR = (float) Math.sqrt(Math.max(0.08D, r * r - yOff * yOff)) * 1.03F;
                renderOrientedRing(matrix, vc, center.add(0.0D, yOff, 0.0D), flatX, flatZ,
                        bandR * 0.93F, bandR, 26, 0.88F, 0.98F, 1.00F, tideAlpha * 0.78F);
            }
            if (!localFirstPerson || flare01 > 0.15F) {
                for (int b = 0; b < 6; b++) {
                    float bCycle = ((gameTime * 0.035F + b / 6.0F) % 1.0F);
                    float bAng = gameTime * 0.12F + b * 1.047F;
                    double by = -r * 0.55D + bCycle * (r * 1.15D);
                    float br = (float) Math.sqrt(Math.max(0.06D, r * r - by * by)) * 1.04F;
                    Vec3 bPos = center.add(Mth.cos(bAng) * br, by, Mth.sin(bAng) * br);
                    renderOctahedron(matrix, vc, bPos, 0.055F * r * (0.7F + 0.3F * Mth.sin(bCycle * (float) Math.PI)),
                            0.88F, 0.98F, 1.00F, tideAlpha * Mth.sin(bCycle * (float) Math.PI));
                }
            }
        } else if (isBotanicalMateria(ward.primaryMateria) || ward.secondaryMateria == EssenceType.NATURE) {
            // Section 6.4 Sylvan Mantle: twining 3D leafy-vine & blossom garlands, underfoot woodland flower seal & golden sunlight motes!
            float sylvanAlpha = Mth.lerp(flare01, localFirstPerson ? 0.26F : 0.72F, 0.96F) * env;
            Vec3 sylvanFeet = new Vec3(center.x, entity.getY() + 0.05D, center.z);
            renderOrientedRing(matrix, vc, sylvanFeet, flatX, flatZ, r * 0.78F, r * 0.92F, 24,
                    0.102F, 0.349F, 0.137F, sylvanAlpha * 0.85F);
            renderBotanicalBlossom(matrix, vc, sylvanFeet.add(0.0D, 0.015D, 0.0D), flatX, flatZ, r * 0.86F, 6,
                    gameTime * 0.045F, 1.00F, 0.58F, 0.76F, sylvanAlpha * 0.68F);

            if (!localFirstPerson || flare01 > 0.15F) {
                for (int garland = 0; garland < 2; garland++) {
                    float gOff = garland * (float) Math.PI + gameTime * 0.08F;
                    Vec3 prevG = null;
                    for (int s = 0; s <= 14; s++) {
                        float u = s / 14.0F;
                        double gy = -r * 0.62D + u * (r * 1.24D);
                        float gr = (float) Math.sqrt(Math.max(0.10D, r * r - gy * gy)) * 1.03F;
                        float ga = gOff + u * 5.2F;
                        Vec3 gpt = center.add(Mth.cos(ga) * gr, gy, Mth.sin(ga) * gr);
                        if (prevG != null) {
                            renderCrossSegment(matrix, vc, prevG, gpt, 0.036F * r,
                                    0.102F, 0.349F, 0.137F, sylvanAlpha * 0.90F);
                        }
                        if (s > 0 && s < 14 && s % 3 == 0) {
                            Vec3 radOut = new Vec3(Mth.cos(ga), 0.22D, Mth.sin(ga)).normalize();
                            Vec3 tang = new Vec3(-Mth.sin(ga), 0.0D, Mth.cos(ga));
                            renderBotanicalBlossom(matrix, vc, gpt.add(radOut.scale(0.04D)), tang, new Vec3(0.0D, 1.0D, 0.0D),
                                    0.14F * r, 5, ga,
                                    (s % 2 == 0) ? 1.00F : 0.71F, (s % 2 == 0) ? 0.58F : 1.00F, (s % 2 == 0) ? 0.76F : 0.47F,
                                    sylvanAlpha * 0.88F);
                        }
                        prevG = gpt;
                    }
                }
            }
        } else if (isTectonicMateria(ward.primaryMateria) || ward.secondaryMateria == EssenceType.EARTH) {
            // Section 6.5 Earth Defensive Attunement: 6 orbiting 3D hexagonal granite/terracotta bastion shields & octagonal seal!
            float bastionAlpha = Mth.lerp(flare01, localFirstPerson ? 0.28F : 0.80F, 0.98F) * env;
            Vec3 bastionFeet = new Vec3(center.x, entity.getY() + 0.05D, center.z);
            renderOrientedRing(matrix, vc, bastionFeet, flatX, flatZ, r * 0.80F, r * 0.96F, 8,
                    0.467F, 0.196F, 0.078F, bastionAlpha * 0.88F);
            renderOrientedStarPolygon(matrix, vc, bastionFeet.add(0.0D, 0.015D, 0.0D), flatX, flatZ,
                    r * 0.92F, 8, -gameTime * 0.035F, 1.00F, 0.76F, 0.47F, bastionAlpha * 0.74F);

            if (!localFirstPerson || flare01 > 0.15F) {
                int shieldCount = 6;
                for (int s = 0; s < shieldCount; s++) {
                    float sAng = gameTime * 0.075F + (float) (Math.PI * 2.0D * s / shieldCount);
                    double bob = 0.08D * Mth.sin(gameTime * 0.14F + s * 1.1F);
                    Vec3 outNormal = new Vec3(Mth.cos(sAng), 0.0D, Mth.sin(sAng));
                    Vec3 tangU = new Vec3(-Mth.sin(sAng), 0.0D, Mth.cos(sAng));
                    Vec3 tangV = new Vec3(0.0D, 1.0D, 0.0D);
                    Vec3 platePos = center.add(outNormal.scale(r * 1.04D)).add(0.0D, bob, 0.0D);
                    boolean terracottaPlate = (s % 2 == 0);
                    renderFacetedHexPlate(
                            matrix, vc, platePos, tangU, tangV, r * 0.30F, (float) (Math.PI / 6.0D),
                            terracottaPlate ? 0.467F : 0.353F,
                            terracottaPlate ? 0.196F : 0.322F,
                            terracottaPlate ? 0.078F : 0.298F,
                            bastionAlpha * 0.74F,
                            1.00F, 0.76F, 0.47F, bastionAlpha * 0.95F
                    );
                }
            }
        } else if (isGlacialMateria(ward.primaryMateria)) {
            // Section 6.6 Glacial Spikes Retaliation: underfoot 6-pointed snowflake seal & 8 outward-bristling 3D reactive ice spikes!
            float spikeAlpha = Mth.lerp(flare01, localFirstPerson ? 0.26F : 0.82F, 0.98F) * env;
            Vec3 frostFeet = new Vec3(center.x, entity.getY() + 0.05D, center.z);
            renderSnowflakeCrystal(matrix, vc, frostFeet, flatX, flatZ,
                    r * 1.02F, gameTime * 0.04F, spikeAlpha * 0.86F);

            if (!localFirstPerson || flare01 > 0.15F) {
                int spikeCount = 8;
                float spikeSurge = 1.0F + 0.42F * flare01;
                for (int s = 0; s < spikeCount; s++) {
                    float sAng = -gameTime * 0.065F + (float) (Math.PI * 2.0D * s / spikeCount);
                    double yOff = (s % 2 == 0) ? 0.16D : -0.18D;
                    Vec3 rad = new Vec3(Mth.cos(sAng), 0.24D * (s % 2 == 0 ? 1.0D : -0.4D), Mth.sin(sAng)).normalize();
                    Vec3 tang = new Vec3(-Mth.sin(sAng), 0.0D, Mth.cos(sAng));
                    Vec3 upPerp = rad.cross(tang).normalize();
                    Vec3 spikeBase = center.add(0.0D, yOff, 0.0D).add(rad.scale(r * 0.78D));
                    renderFacetedIceSpire(matrix, vc, spikeBase, rad, tang, upPerp,
                            r * 0.11F, r * 0.56F * spikeSurge, 5, sAng, spikeAlpha);
                }
            }
        } else if (isInfernalMateria(ward.primaryMateria)) {
            // Section 6.7 Immolation Shroud: volcanic caldera seal underfoot, 8 rising flickering 3D hellfire flame tongues & orbiting embers!
            float blazeAlpha = Mth.lerp(flare01, localFirstPerson ? 0.26F : 0.82F, 0.98F) * env;
            Vec3 blazeFeet = new Vec3(center.x, entity.getY() + 0.05D, center.z);
            renderOrientedRing(matrix, vc, blazeFeet, flatX, flatZ, r * 0.82F, r * 1.04F, 24,
                    0.722F, 0.235F, 0.031F, blazeAlpha * 0.88F);
            renderOrientedStarPolygon(matrix, vc, blazeFeet.add(0.0D, 0.015D, 0.0D), flatX, flatZ,
                    r * 0.96F, 8, gameTime * 0.06F, 1.00F, 0.820F, 0.361F, blazeAlpha * 0.82F);

            if (!localFirstPerson || flare01 > 0.15F) {
                Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
                int flameCount = 8;
                float flameSurge = 1.0F + 0.45F * flare01;
                for (int f = 0; f < flameCount; f++) {
                    float fAng = gameTime * 0.085F + (float) (Math.PI * 2.0D * f / flameCount);
                    float flicker = 0.82F + 0.22F * Mth.sin(gameTime * 0.32F + f * 1.3F);
                    Vec3 rad = new Vec3(Mth.cos(fAng), 0.0D, Mth.sin(fAng));
                    Vec3 tang = new Vec3(-Mth.sin(fAng), 0.0D, Mth.cos(fAng));
                    Vec3 flameBase = blazeFeet.add(rad.scale(r * 0.94D));
                    Vec3 tiltAxis = worldUp.add(rad.scale(0.14D)).normalize();
                    renderHellfireFlameTongue(matrix, vc, flameBase, tiltAxis, rad, tang,
                            r * 0.18F, r * (f % 2 == 0 ? 0.96F : 0.68F) * flicker * flameSurge, blazeAlpha);
                }
                // 6 orbiting 3D basalt-slag & molten-gold magma cinder shards swirling around the shrouded caster
                for (int c = 0; c < 6; c++) {
                    float cAng = -gameTime * 0.11F + (float) (Math.PI * 2.0D * c / 6.0D);
                    double bob = 0.22D * Mth.sin(gameTime * 0.18F + c * 1.05F);
                    Vec3 cinderPos = center.add(Mth.cos(cAng) * r * 1.04D, bob, Mth.sin(cAng) * r * 1.04D);
                    renderOctahedron(matrix, vc, cinderPos, r * 0.085F * flameSurge, 1.00F, 0.820F, 0.361F, blazeAlpha * 0.96F);
                    renderOctahedron(matrix, vc, cinderPos, r * 0.125F * flameSurge, 0.165F, 0.090F, 0.071F, blazeAlpha * 0.76F);
                }
            }
        } else if (isSolarRadiantMateria(ward.primaryMateria)) {
            // Section 6.8 Pacification Shroud: underfoot 12-ray sun-compass seal, floating golden Seraphic Halo & 6 orbiting 3D Dawn Stars!
            float radiantAlpha = Mth.lerp(flare01, localFirstPerson ? 0.24F : 0.82F, 0.98F) * env;
            Vec3 sunFeet = new Vec3(center.x, entity.getY() + 0.05D, center.z);
            renderOrientedRing(matrix, vc, sunFeet, flatX, flatZ, r * 0.84F, r * 1.02F, 28,
                    1.00F, 0.886F, 0.471F, radiantAlpha * 0.88F);
            renderOrientedStarPolygon(matrix, vc, sunFeet.add(0.0D, 0.015D, 0.0D), flatX, flatZ,
                    r * 0.98F, 12, gameTime * 0.045F, 1.00F, 0.99F, 0.92F, radiantAlpha * 0.84F);

            // Overhead floating Seraphic Halo calming Enderman eye contact & neutral wildlife
            Vec3 haloCenter = new Vec3(center.x, entity.getY() + entityHeight + 0.36D + 0.04D * Mth.sin(gameTime * 0.12F), center.z);
            renderOrientedRing(matrix, vc, haloCenter, flatX, flatZ, r * 0.36F, r * 0.48F, 24,
                    1.00F, 0.886F, 0.471F, radiantAlpha * 0.94F);
            renderOrientedRing(matrix, vc, haloCenter.add(0.0D, 0.01D, 0.0D), flatX, flatZ, r * 0.39F, r * 0.44F, 24,
                    1.00F, 0.995F, 0.96F, radiantAlpha * 0.96F);

            if (!localFirstPerson || flare01 > 0.15F) {
                Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
                for (int s = 0; s < 6; s++) {
                    float sAng = gameTime * 0.065F + (float) (Math.PI * 2.0D * s / 6.0D);
                    double bob = 0.14D * Mth.sin(gameTime * 0.15F + s * 1.05F);
                    Vec3 rad = new Vec3(Mth.cos(sAng), 0.0D, Mth.sin(sAng));
                    Vec3 tang = new Vec3(-Mth.sin(sAng), 0.0D, Mth.cos(sAng));
                    Vec3 starPos = center.add(rad.scale(r * 1.02D)).add(0.0D, bob, 0.0D);
                    renderOctahedron(matrix, vc, starPos, r * 0.10F, 1.00F, 0.995F, 0.96F, radiantAlpha * 0.96F);
                    renderSolarStarburstRay(matrix, vc, starPos, worldUp, rad, tang, r * 0.042F, r * 0.24F, radiantAlpha);
                    renderSolarStarburstRay(matrix, vc, starPos, worldUp.scale(-1.0D), rad, tang, r * 0.042F, r * 0.24F, radiantAlpha);
                }
            }
        } else if (isUmbralShadowMateria(ward.primaryMateria)) {
            // Section 6.9 Shroud of Silence & Stealth: 3-block obscuring shadow-mist veil, underfoot silence ring,
            // and 6 orbiting obsidian shadow-daggers concealing the caster's exact silhouette!
            float umbralAlpha = localFirstPerson ? (env * 0.22F) : (env * 0.88F);
            Vec3 feet = new Vec3(center.x, entity.getY() + 0.04D, center.z);

            renderOrientedRing(matrix, vc, feet, flatX, flatZ, 0.25F, 2.95F, 30,
                    0.085F, 0.058F, 0.095F, umbralAlpha * 0.42F);
            renderOrientedRing(matrix, vc, feet.add(0.0D, 0.02D, 0.0D), flatX, flatZ, 2.82F, 3.02F, 32,
                    0.56F, 0.36F, 1.00F, umbralAlpha * 0.72F);

            if (!localFirstPerson || flare01 > 0.15F) {
                renderVerticalAuraCurtain(matrix, vc, feet, r * 1.15F, (float) (entityHeight + 0.28D),
                        18, 0.095F, 0.070F, 0.115F, umbralAlpha * 0.56F);
                Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
                for (int s = 0; s < 6; s++) {
                    float sAng = -gameTime * 0.085F + (float) (Math.PI * 2.0D * s / 6.0D);
                    double bob = 0.16D * Mth.sin(gameTime * 0.16F + s * 1.1F);
                    Vec3 rad = new Vec3(Mth.cos(sAng), 0.0D, Mth.sin(sAng));
                    Vec3 tang = new Vec3(-Mth.sin(sAng), 0.0D, Mth.cos(sAng));
                    Vec3 orbPos = center.add(rad.scale(r * 1.06D)).add(0.0D, bob, 0.0D);
                    renderUmbralShadowNeedle(matrix, vc, orbPos.add(0.0D, -0.22D, 0.0D), worldUp, rad, tang,
                            r * 0.068F, r * 0.44F, umbralAlpha);
                }
            }
        } else if (isNecroticUndeadMateria(ward.primaryMateria)) {
            // Section 6.10 Necrotic Shroud: 3-block hostile life-drain grave aura underfoot, #4D6453 tombstone ossuary seal,
            // and 5 orbiting 3D #E6DFCF / #5EE088 cursed skulls & soul wisps!
            float undeadAlpha = Mth.lerp(flare01, localFirstPerson ? 0.24F : 0.84F, 0.98F) * env;
            Vec3 feet = new Vec3(center.x, entity.getY() + 0.045D, center.z);

            // 3-block life-drain perimeter ring + inner tombstone ossuary seal
            renderOrientedRing(matrix, vc, feet, flatX, flatZ, 2.82F, 3.02F, 32,
                    0.369F, 0.878F, 0.533F, undeadAlpha * 0.72F);
            renderOrientedRing(matrix, vc, feet.add(0.0D, 0.01D, 0.0D), flatX, flatZ, r * 0.80F, r * 1.02F, 24,
                    0.302F, 0.392F, 0.325F, undeadAlpha * 0.88F);
            renderOrientedStarPolygon(matrix, vc, feet.add(0.0D, 0.02D, 0.0D), flatX, flatZ,
                    r * 0.94F, 8, -gameTime * 0.045F, 0.902F, 0.875F, 0.812F, undeadAlpha * 0.82F);

            if (!localFirstPerson || flare01 > 0.15F) {
                Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
                for (int s = 0; s < 5; s++) {
                    float sAng = gameTime * 0.075F + (float) (Math.PI * 2.0D * s / 5.0D);
                    double bob = 0.16D * Mth.sin(gameTime * 0.15F + s * 1.25F);
                    Vec3 rad = new Vec3(Mth.cos(sAng), 0.0D, Mth.sin(sAng));
                    Vec3 tang = new Vec3(-Mth.sin(sAng), 0.0D, Mth.cos(sAng));
                    Vec3 skullPos = center.add(rad.scale(r * 1.06D)).add(0.0D, bob, 0.0D);
                    renderSpectralSkullHead(matrix, vc, skullPos, tang, rad, worldUp, r * 0.145F, undeadAlpha);
                }
            }
        } else if (isAridDesertMateria(ward.primaryMateria)) {
            // Section 6.12 Sandstorm Veil: swirling #C2B28F desert dust cloud cloaking caster, arrow-deflecting sand crescents
            // & dry sandstone crust seal underfoot!
            float aridAlpha = Mth.lerp(flare01, localFirstPerson ? 0.22F : 0.84F, 0.98F) * env;
            Vec3 feet = new Vec3(center.x, entity.getY() + 0.045D, center.z);

            renderOrientedRing(matrix, vc, feet, flatX, flatZ, r * 0.76F, r * 1.18F, 26,
                    0.761F, 0.698F, 0.561F, aridAlpha * 0.86F);
            renderOrientedStarPolygon(matrix, vc, feet.add(0.0D, 0.015D, 0.0D), flatX, flatZ,
                    r * 1.04F, 8, gameTime * 0.06F, 0.961F, 0.894F, 0.745F, aridAlpha * 0.78F);

            if (!localFirstPerson || flare01 > 0.15F) {
                renderVerticalAuraCurtain(matrix, vc, feet, r * 1.14F, (float) (entityHeight + 0.24D),
                        20, 0.761F, 0.698F, 0.561F, aridAlpha * 0.46F);
                for (int c = 0; c < 4; c++) {
                    float cAng = gameTime * 0.26F + (float) (Math.PI * 0.5D * c);
                    double cy = 0.28D + (c * 0.36D);
                    renderSweptWindCrescent(matrix, vc, feet.add(0.0D, cy, 0.0D),
                            flatX, flatZ, r * (1.06F + 0.05F * c), cAng, 1.05F, r * 0.14F,
                            0.961F, 0.894F, 0.745F, aridAlpha * 0.88F);
                }
                for (int g = 0; g < 6; g++) {
                    float gAng = -gameTime * 0.14F + (float) (Math.PI * 2.0D * g / 6.0D);
                    double bob = 0.18D * Mth.sin(gameTime * 0.18F + g * 1.15F);
                    Vec3 grainPos = center.add(Mth.cos(gAng) * r * 1.10D, bob, Mth.sin(gAng) * r * 1.10D);
                    renderOctahedron(matrix, vc, grainPos, r * 0.075F,
                            0.612F, 0.478F, 0.298F, aridAlpha * 0.92F);
                }
            }
        } else if (isGalvanicLightningMateria(ward.primaryMateria)) {
            // Section 6.13 Static Shroud (Living Lightning Rod): crackling #FFE600 Faraday rings,
            // overhead Living Lightning Rod conductor spire & 6 snapping 3D electric arcs!
            float voltAlpha = Mth.lerp(flare01, localFirstPerson ? 0.22F : 0.86F, 0.99F) * env;
            Vec3 feet = new Vec3(center.x, entity.getY() + 0.045D, center.z);

            renderOrientedRing(matrix, vc, feet, flatX, flatZ, r * 0.84F, r * 1.04F, 26,
                    1.00F, 0.902F, 0.00F, voltAlpha * 0.92F);
            renderOrientedStarPolygon(matrix, vc, feet.add(0.0D, 0.015D, 0.0D), flatX, flatZ,
                    r * 0.98F, 8, gameTime * 0.14F, 1.00F, 1.00F, 0.94F, voltAlpha * 0.88F);

            // Overhead Living Lightning Rod copper/gold conductor spire above caster's head!
            Vec3 rodBase = new Vec3(center.x, entity.getY() + entityHeight + 0.22D, center.z);
            Vec3 rodTip = rodBase.add(0.0D, 0.48D, 0.0D);
            renderCrossSegment(matrix, vc, rodBase, rodTip, 0.055F, 0.85F, 0.48F, 0.22F, voltAlpha * 0.95F);
            renderOctahedron(matrix, vc, rodTip, 0.11F, 1.00F, 0.902F, 0.00F, voltAlpha * 0.98F);

            if (!localFirstPerson || flare01 > 0.15F) {
                int flicker = (int) (gameTime * 1.25F) + ward.entityId * 11;
                for (int a = 0; a < 6; a++) {
                    float aAng = (float) (Math.PI * 2.0D * a / 6.0D) + gameTime * 0.10F;
                    Vec3 p0 = feet.add(Mth.cos(aAng) * r * 0.98D, 0.12D, Mth.sin(aAng) * r * 0.98D);
                    Vec3 p1 = feet.add(
                            Mth.cos(aAng + 0.25F) * r * (1.04D + 0.14D * (hashFloat(flicker, a * 3) - 0.5F)),
                            entityHeight * 0.52D,
                            Mth.sin(aAng + 0.25F) * r * (1.04D + 0.14D * (hashFloat(flicker, a * 3 + 1) - 0.5F))
                    );
                    Vec3 p2 = rodTip;
                    renderCrossSegment(matrix, vc, p0, p1, 0.048F, 1.00F, 0.902F, 0.00F, voltAlpha * 0.88F);
                    renderCrossSegment(matrix, vc, p1, p2, 0.032F, 1.00F, 1.00F, 0.96F, voltAlpha * 0.94F);
                }
            }
        } else if (isMagmaCalderaMateria(ward.primaryMateria)) {
            // Section 6.14 Igneous Armor Mantle: 4-block volcanic heat-haze perimeter ring underfoot,
            // plus 6 thick 3D dark obsidian/pumice hexagonal armor plates laced with glowing #B83C08 / #FFD15C lava seams!
            float magmaAlpha = Mth.lerp(flare01, localFirstPerson ? 0.20F : 0.88F, 0.99F) * env;
            Vec3 feet = new Vec3(center.x, entity.getY() + 0.045D, center.z);

            // 4-block snow/ice-melting & water-vaporizing volcanic heat perimeter ring + caldera star seal
            renderOrientedRing(matrix, vc, feet, flatX, flatZ, 3.82F, 4.02F, 36,
                    0.722F, 0.235F, 0.031F, magmaAlpha * 0.68F);
            renderOrientedRing(matrix, vc, feet.add(0.0D, 0.012D, 0.0D), flatX, flatZ, r * 0.82F, r * 1.06F, 24,
                    0.165F, 0.090F, 0.071F, magmaAlpha * 0.92F);
            renderOrientedStarPolygon(matrix, vc, feet.add(0.0D, 0.020D, 0.0D), flatX, flatZ,
                    r * 0.96F, 6, gameTime * 0.04F, 1.00F, 0.820F, 0.361F, magmaAlpha * 0.86F);

            if (!localFirstPerson || flare01 > 0.15F) {
                Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
                float plateSurge = 1.0F + 0.25F * flare01;
                for (int p = 0; p < 6; p++) {
                    float pAng = gameTime * 0.045F + (float) (Math.PI * 2.0D * p / 6.0D);
                    double bob = 0.08D * Mth.sin(gameTime * 0.14F + p * 1.05F);
                    Vec3 rad = new Vec3(Mth.cos(pAng), 0.0D, Mth.sin(pAng));
                    Vec3 tang = new Vec3(-Mth.sin(pAng), 0.0D, Mth.cos(pAng));
                    Vec3 plateCenter = center.add(rad.scale(r * 0.96D)).add(0.0D, bob, 0.0D);
                    renderFacetedHexPlate(
                            matrix, vc, plateCenter, tang, worldUp, r * 0.28F * plateSurge, 0.0F,
                            0.165F, 0.090F, 0.071F, magmaAlpha * 0.88F,
                            0.722F, 0.235F, 0.031F, magmaAlpha * 0.96F
                    );
                    renderOctahedron(matrix, vc, plateCenter.add(rad.scale(0.035D)), r * 0.065F * plateSurge,
                            1.00F, 0.820F, 0.361F, magmaAlpha * 0.98F);
                }
            }
        } else if (isTempestStormMateria(ward.primaryMateria)) {
            // Section 6.15 Squall-Rider Shroud: swirling #429BA3 / #D8FAFF updraft slipstream rings & swept 3D gale glider crescents!
            float stormAlpha = Mth.lerp(flare01, localFirstPerson ? 0.20F : 0.84F, 0.98F) * env;
            Vec3 feet = new Vec3(center.x, entity.getY() + 0.045D, center.z);

            renderOrientedRing(matrix, vc, feet, flatX, flatZ, r * 0.80F, r * 1.08F, 26,
                    0.259F, 0.608F, 0.639F, stormAlpha * 0.90F);
            if (!localFirstPerson || flare01 > 0.15F) {
                for (int c = 0; c < 4; c++) {
                    float cAng = gameTime * 0.28F + (float) (Math.PI * 0.5D * c);
                    double cy = 0.22D + c * 0.36D;
                    renderSweptWindCrescent(matrix, vc, feet.add(0.0D, cy, 0.0D),
                            flatX, flatZ, r * (1.02F + 0.08F * c), cAng, 1.18F, r * 0.14F,
                            (c % 2 == 0) ? 0.847F : 0.259F,
                            (c % 2 == 0) ? 0.980F : 0.608F,
                            (c % 2 == 0) ? 1.000F : 0.639F,
                            stormAlpha * 0.88F);
                }
            }
        } else if (isPermafrostGlacialMateria(ward.primaryMateria)) {
            // Section 6.16 Permafrost Cryo-Shroud: Frost-Walker underfoot snowflake seal, #98DCF2 cryo-mist bands & 6 orbiting permafrost plates!
            float cryoAlpha = Mth.lerp(flare01, localFirstPerson ? 0.20F : 0.82F, 0.98F) * env;
            Vec3 feet = new Vec3(center.x, entity.getY() + 0.045D, center.z);
            renderSnowflakeCrystal(matrix, vc, feet, flatX, flatZ,
                    r * 1.08F, gameTime * 0.045F, cryoAlpha * 0.88F);
            if (!localFirstPerson || flare01 > 0.15F) {
                Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
                float plateSurge = 1.0F + 0.26F * flare01;
                for (int p = 0; p < 6; p++) {
                    float pAng = -gameTime * 0.055F + (float) (Math.PI * 2.0D * p / 6.0D);
                    double bob = 0.07D * Mth.sin(gameTime * 0.15F + p * 1.05F);
                    Vec3 rad = new Vec3(Mth.cos(pAng), 0.0D, Mth.sin(pAng));
                    Vec3 tang = new Vec3(-Mth.sin(pAng), 0.0D, Mth.cos(pAng));
                    Vec3 platePos = center.add(rad.scale(r * 0.98D)).add(0.0D, bob, 0.0D);
                    renderFacetedHexPlate(
                            matrix, vc, platePos, tang, worldUp, r * 0.25F * plateSurge, 0.0F,
                            0.173F, 0.510F, 0.788F, cryoAlpha * 0.82F,
                            0.88F, 1.00F, 1.00F, cryoAlpha * 0.96F
                    );
                }
            }
        } else if (isOvergrowthRampartMateria(ward.primaryMateria)) {
            // Section 6.17 Earth-Root Bastion: 6 mossy stone bastion shields & 6 living wooden root anchors gripping the earth!
            float rootAlpha = Mth.lerp(flare01, localFirstPerson ? 0.22F : 0.84F, 0.98F) * env;
            Vec3 feet = new Vec3(center.x, entity.getY() + 0.045D, center.z);
            renderOrientedRing(matrix, vc, feet, flatX, flatZ, r * 0.80F, r * 1.06F, 18,
                    0.467F, 0.196F, 0.078F, rootAlpha * 0.90F);
            renderBotanicalBlossom(matrix, vc, feet.add(0.0D, 0.015D, 0.0D), flatX, flatZ,
                    r * 0.96F, 6, gameTime * 0.03F, 0.173F, 0.902F, 0.408F, rootAlpha * 0.80F);
            if (!localFirstPerson || flare01 > 0.15F) {
                Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
                float plateSurge = 1.0F + 0.25F * flare01;
                for (int p = 0; p < 6; p++) {
                    float pAng = gameTime * 0.038F + (float) (Math.PI * 2.0D * p / 6.0D);
                    Vec3 rad = new Vec3(Mth.cos(pAng), 0.0D, Mth.sin(pAng));
                    Vec3 tang = new Vec3(-Mth.sin(pAng), 0.0D, Mth.cos(pAng));
                    Vec3 platePos = center.add(rad.scale(r * 0.96D));
                    renderFacetedHexPlate(
                            matrix, vc, platePos, tang, worldUp, r * 0.28F * plateSurge, 0.0F,
                            0.467F, 0.196F, 0.078F, rootAlpha * 0.86F,
                            0.173F, 0.902F, 0.408F, rootAlpha * 0.96F
                    );
                    renderFacetedStoneSpire(matrix, vc, feet.add(rad.scale(r * 0.92D)),
                            worldUp.add(rad.scale(0.35D)).normalize(), rad, tang,
                            r * 0.10F, r * 0.36F * plateSurge, 5, pAng, rootAlpha);
                }
            } else if (ward.primaryMateria == EssenceType.DUST) {
                // Section 6.18 Obscuring Chalk Veil (#C2B28F): dense swirling chalk-powder veil & 4 arrow-deflecting dust crescents!
                Vec3 dustFeet = feetPos.add(0.0D, 0.04D, 0.0D);
                float veilAlpha = localFirstPerson ? (ringAlpha * 0.50F) : (ringAlpha * 0.92F);
                renderOrientedRing(matrix, vc, dustFeet, flatX, flatZ, r * 0.80F, r * 1.18F, 26,
                        0.761F, 0.698F, 0.561F, veilAlpha * 0.85F);
                for (int c = 0; c < 4; c++) {
                    float cAng = gameTime * 0.32F + (float) (Math.PI * 0.5D * c);
                    double cy = dustFeet.y + 0.32D + c * 0.38D;
                    renderSweptWindCrescent(matrix, vc, new Vec3(dustFeet.x, cy, dustFeet.z),
                            flatX, flatZ, r * (0.94F + 0.06F * (c % 2)), cAng, 1.20F, Math.max(0.14F, r * 0.12F),
                            (c % 2 == 0) ? 0.949F : 0.761F,
                            (c % 2 == 0) ? 0.925F : 0.698F,
                            (c % 2 == 0) ? 0.882F : 0.561F,
                            veilAlpha * 0.92F);
                }
            } else if (ward.primaryMateria == EssenceType.SPORE) {
                // Section 6.19 Mycelial Spore-Cloud Mantle & Spore Carapace (#1A5923 / #A88654):
                // protective swirling green-gold spore cloud + chitinous fungal carapace plates!
                Vec3 sporeFeet = feetPos.add(0.0D, 0.04D, 0.0D);
                float mantleAlpha = localFirstPerson ? (ringAlpha * 0.52F) : (ringAlpha * 0.92F);
                renderOrientedRing(matrix, vc, sporeFeet, flatX, flatZ, r * 0.80F, r * 1.16F, 24,
                        0.102F, 0.349F, 0.137F, mantleAlpha * 0.86F);
                for (int c = 0; c < 4; c++) {
                    float cAng = gameTime * 0.24F + (float) (Math.PI * 0.5D * c);
                    double cy = sporeFeet.y + 0.30D + c * 0.38D;
                    renderSweptWindCrescent(matrix, vc, new Vec3(sporeFeet.x, cy, sporeFeet.z),
                            flatX, flatZ, r * (0.92F + 0.06F * (c % 2)), cAng, 1.15F, Math.max(0.13F, r * 0.11F),
                            (c % 2 == 0) ? 0.173F : 0.910F,
                            (c % 2 == 0) ? 0.902F : 0.784F,
                            (c % 2 == 0) ? 0.408F : 0.510F,
                            mantleAlpha * 0.90F);
                }
                if (ward.secondaryMateria == EssenceType.AEGIS) {
                    Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
                    for (int p = 0; p < 6; p++) {
                        float pAng = gameTime * 0.06F + (float) (Math.PI * 2.0D * p / 6.0D);
                        Vec3 rad = new Vec3(Mth.cos(pAng), 0.0D, Mth.sin(pAng));
                        Vec3 tang = new Vec3(-Mth.sin(pAng), 0.0D, Mth.cos(pAng));
                        renderFacetedHexPlate(
                                matrix, vc, center.add(rad.scale(r * 0.96D)), tang, worldUp, r * 0.26F, 0.0F,
                                0.102F, 0.349F, 0.137F, mantleAlpha * 0.86F,
                                0.910F, 0.784F, 0.510F, mantleAlpha * 0.95F
                        );
                    }
                }
            } else if (ward.primaryMateria == EssenceType.TAIGA) {
                // Section 6.20 Winter Solstice Shroud (#1A5923 / #98DCF2): sub-zero blizzard mist & whirling
                // frosted pine-needle crescents deflecting 50% of incoming arrows!
                Vec3 taigaFeet = feetPos.add(0.0D, 0.04D, 0.0D);
                float shroudAlpha = localFirstPerson ? (ringAlpha * 0.48F) : (ringAlpha * 0.92F);
                renderOrientedRing(matrix, vc, taigaFeet, flatX, flatZ, r * 0.80F, r * 1.16F, 24,
                        0.596F, 0.863F, 0.949F, shroudAlpha * 0.86F);
                renderSnowflakeCrystal(matrix, vc, taigaFeet.add(0.0D, 0.012D, 0.0D), flatX, flatZ,
                        r * 1.05F, gameTime * 0.06F, shroudAlpha * 0.82F);
                for (int c = 0; c < 4; c++) {
                    float cAng = gameTime * 0.30F + (float) (Math.PI * 0.5D * c);
                    double cy = taigaFeet.y + 0.30D + c * 0.38D;
                    renderSweptWindCrescent(matrix, vc, new Vec3(taigaFeet.x, cy, taigaFeet.z),
                            flatX, flatZ, r * (0.92F + 0.06F * (c % 2)), cAng, 1.22F, Math.max(0.13F, r * 0.11F),
                            (c % 2 == 0) ? 0.596F : 0.102F,
                            (c % 2 == 0) ? 0.863F : 0.349F,
                            (c % 2 == 0) ? 0.949F : 0.137F,
                            shroudAlpha * 0.92F);
                }
            } else if (ward.primaryMateria == EssenceType.OASIS) {
                // Section 6.21 Succulent Thorn Carapace (#1A5923 / #C2B28F): thick succulent cactus-flesh hex plates
                // bristling with sharp sunlit desert-gold thorn spines (-20% physical damage + Oasis thorn retaliation)!
                Vec3 oasisFeet = feetPos.add(0.0D, 0.04D, 0.0D);
                float carapaceAlpha = localFirstPerson ? (ringAlpha * 0.46F) : (ringAlpha * 0.92F);
                renderOrientedRing(matrix, vc, oasisFeet, flatX, flatZ, r * 0.82F, r * 1.14F, 24,
                        0.220F, 0.776F, 0.957F, carapaceAlpha * 0.80F);
                renderBotanicalBlossom(matrix, vc, oasisFeet.add(0.0D, 0.015D, 0.0D), flatX, flatZ,
                        r * 1.04F, 8, gameTime * 0.05F, 0.102F, 0.349F, 0.137F, carapaceAlpha * 0.82F);
                for (int p = 0; p < 6; p++) {
                    float pAng = gameTime * 0.09F + (float) (Math.PI * 2.0D * p / 6.0D);
                    Vec3 outDir = new Vec3(Mth.cos(pAng), 0.10D * ((p % 2 == 0) ? 1 : -1), Mth.sin(pAng)).normalize();
                    Vec3 tanU = new Vec3(-Mth.sin(pAng), 0.0D, Mth.cos(pAng));
                    Vec3 tanV = outDir.cross(tanU).normalize();
                    Vec3 plateCenter = center.add(outDir.scale(r * 0.94D));
                    renderFacetedHexPlate(
                            matrix, vc, plateCenter, tanU, tanV, r * 0.28F, pAng * 0.5F,
                            0.102F, 0.349F, 0.137F, carapaceAlpha * 0.72F,
                            0.761F, 0.698F, 0.561F, carapaceAlpha * 0.94F
                    );
                    // Sharp outward sunlit cactus thorn spine jutting from each succulent plate!
                    renderCrossSegment(
                            matrix, vc, plateCenter, plateCenter.add(outDir.scale(Math.max(0.24D, r * 0.28D))),
                            0.032F, 0.871F, 0.788F, 0.604F, carapaceAlpha * 0.96F
                    );
                }
            } else if (ward.primaryMateria == EssenceType.VITAE) {
                // Section 6.22 Cellular Overdrive Shroud (#FF6B9D / #FFD1E3): crackling biological double-helix
                // & heartbeat overdrive rings!
                Vec3 vitaeFeet = feetPos.add(0.0D, 0.04D, 0.0D);
                float shroudAlpha = localFirstPerson ? (ringAlpha * 0.44F) : (ringAlpha * 0.92F);
                float beat = 0.90F + 0.14F * Mth.abs(Mth.sin(gameTime * 0.28F));
                renderOrientedRing(matrix, vc, vitaeFeet, flatX, flatZ, r * 0.82F * beat, r * 1.14F * beat, 26,
                        1.000F, 0.420F, 0.616F, shroudAlpha * 0.86F);
                for (int h = 0; h < 2; h++) {
                    float off = h * (float) Math.PI + gameTime * 0.16F;
                    Vec3 prev = null;
                    for (int s = 0; s <= 12; s++) {
                        float t = s / 12.0F;
                        float ang = off + t * 4.8F;
                        float hr = r * (0.86F + 0.10F * Mth.sin(t * (float) Math.PI));
                        Vec3 pt = vitaeFeet.add(Mth.cos(ang) * hr, 0.10D + t * 1.65D, Mth.sin(ang) * hr);
                        if (prev != null) {
                            boolean pearl = (h == 1);
                            renderCrossSegment(matrix, vc, prev, pt, 0.032F,
                                    1.000F, pearl ? 0.820F : 0.420F, pearl ? 0.890F : 0.616F, shroudAlpha * 0.92F);
                        }
                        prev = pt;
                    }
                }
            } else if (ward.primaryMateria == EssenceType.BLOOD) {
                // Section 6.23 Coagulated Crimson Carapace (#8A0303 / #FF384C): hardened armor shell of 6 orbiting
                // solidified crimson blood-crystal hex plates!
                Vec3 bloodFeet = feetPos.add(0.0D, 0.04D, 0.0D);
                float carapaceAlpha = localFirstPerson ? (ringAlpha * 0.42F) : (ringAlpha * 0.94F);
                renderOrientedRing(matrix, vc, bloodFeet, flatX, flatZ, r * 0.84F, r * 1.12F, 24,
                        0.541F, 0.012F, 0.012F, carapaceAlpha * 0.88F);
                for (int p = 0; p < 6; p++) {
                    float pAng = gameTime * 0.065F + (float) (Math.PI * 2.0D * p / 6.0D);
                    Vec3 outDir = new Vec3(Mth.cos(pAng), 0.0D, Mth.sin(pAng));
                    Vec3 tanU = new Vec3(-Mth.sin(pAng), 0.0D, Mth.cos(pAng));
                    Vec3 tanV = new Vec3(0.0D, 1.0D, 0.0D);
                    Vec3 plateCenter = center.add(outDir.scale(r * 0.96D)).add(0.0D, (p % 2 == 0 ? 0.08D : -0.08D), 0.0D);
                    renderFacetedHexPlate(
                            matrix, vc, plateCenter, tanU, tanV, Math.max(0.22F, r * 0.34F), 0.0F,
                            0.541F, 0.012F, 0.012F, carapaceAlpha * 0.64F,
                            1.000F, 0.220F, 0.298F, carapaceAlpha * 0.96F
                    );
                }
            } else if (ward.primaryMateria == EssenceType.AURA) {
                // Section 6.24 Chakra Respiration Shroud (#FF6B9D / #A88654): meditative 8-petal golden-pink chakra lotus
                // & orbiting pranic respiration prayer beads!
                Vec3 auraFeet = feetPos.add(0.0D, 0.04D, 0.0D);
                float shroudAlpha = localFirstPerson ? (ringAlpha * 0.40F) : (ringAlpha * 0.92F);
                renderBotanicalBlossom(matrix, vc, auraFeet, flatX, flatZ, r * 1.08F, 8,
                        gameTime * 0.045F, 1.000F, 0.420F, 0.616F, shroudAlpha * 0.82F);
                renderOrientedRing(matrix, vc, auraFeet.add(0.0D, 0.015D, 0.0D), flatX, flatZ,
                        r * 0.86F, r * 1.12F, 26, 0.839F, 0.706F, 0.471F, shroudAlpha * 0.88F);
                if (!localFirstPerson || flare01 > 0.15F) {
                    for (int b = 0; b < 6; b++) {
                        float bAng = -gameTime * 0.09F + (float) (Math.PI * 2.0D * b / 6.0D);
                        Vec3 bead = center.add(Mth.cos(bAng) * r * 1.02D, 0.14D * Mth.sin(gameTime * 0.16F + b), Mth.sin(bAng) * r * 1.02D);
                        boolean pink = (b % 2 == 0);
                        renderOctahedron(matrix, vc, bead, Math.max(0.08F, r * 0.085F),
                                pink ? 1.000F : 0.839F,
                                pink ? 0.420F : 0.706F,
                                pink ? 0.616F : 0.471F,
                                shroudAlpha * 0.95F);
                    }
                }
            } else if (ward.primaryMateria == EssenceType.AMBER) {
                // Section 6.25 Petrified Amber Bastion Mantle (#FF6B9D / #773214 / #F29B2E): translucent carapace
                // of 6 orbiting hardened golden-amber hex plates & fossilized rose rim!
                Vec3 amberFeet = feetPos.add(0.0D, 0.04D, 0.0D);
                float carapaceAlpha = localFirstPerson ? (ringAlpha * 0.38F) : (ringAlpha * 0.92F);
                renderOrientedRing(matrix, vc, amberFeet, flatX, flatZ, r * 0.86F, r * 1.12F, 6,
                        0.467F, 0.196F, 0.078F, carapaceAlpha * 0.90F);
                for (int p = 0; p < 6; p++) {
                    float pAng = gameTime * 0.048F + (float) (Math.PI * 2.0D * p / 6.0D);
                    Vec3 outDir = new Vec3(Mth.cos(pAng), 0.0D, Mth.sin(pAng));
                    Vec3 tanU = new Vec3(-Mth.sin(pAng), 0.0D, Mth.cos(pAng));
                    Vec3 tanV = new Vec3(0.0D, 1.0D, 0.0D);
                    Vec3 plateCenter = center.add(outDir.scale(r * 0.98D)).add(0.0D, (p % 2 == 0 ? 0.06D : -0.06D), 0.0D);
                    renderFacetedHexPlate(
                            matrix, vc, plateCenter, tanU, tanV, Math.max(0.24F, r * 0.36F), 0.0F,
                            0.949F, 0.608F, 0.180F, carapaceAlpha * 0.56F,
                            1.000F, 0.420F, 0.616F, carapaceAlpha * 0.94F
                    );
                }
            } else if (ward.primaryMateria == EssenceType.ECLIPSE) {
                // Section 6.26 Solar Corona Bastion (#FFEA78 / #211A21): radiant golden coronal flares orbiting an
                // abyssal eclipse event-horizon band!
                Vec3 eclipseFeet = feetPos.add(0.0D, 0.04D, 0.0D);
                float coronaAlpha = localFirstPerson ? (ringAlpha * 0.40F) : (ringAlpha * 0.95F);
                renderOrientedRing(matrix, vc, eclipseFeet, flatX, flatZ, r * 0.80F, r * 1.04F, 26,
                        0.129F, 0.102F, 0.129F, coronaAlpha * 0.92F);
                renderOrientedRing(matrix, vc, eclipseFeet.add(0.0D, 0.015D, 0.0D), flatX, flatZ, r * 1.02F, r * 1.18F, 28,
                        1.000F, 0.918F, 0.471F, coronaAlpha * 0.96F);
                if (!localFirstPerson || flare01 > 0.15F) {
                    for (int c = 0; c < 6; c++) {
                        float cAng = gameTime * 0.11F + (float) (Math.PI * 2.0D * c / 6.0D);
                        double yBand = ((c % 3) - 1) * (r * 0.24D);
                        renderSweptWindCrescent(matrix, vc, center.add(0.0D, yBand, 0.0D),
                                flatX, flatZ, r * 1.06F, cAng, 1.00F, Math.max(0.11F, r * 0.10F),
                                (c % 2 == 0) ? 1.000F : 0.129F,
                                (c % 2 == 0) ? 0.918F : 0.102F,
                                (c % 2 == 0) ? 0.471F : 0.129F,
                                coronaAlpha * 0.94F);
                    }
                }
            } else if (ward.primaryMateria == EssenceType.BLIGHT) {
                // Section 6.27 Cadaverous Briar-Carapace (#4D6453 / #34CC48): rotting bone-bark plates & thorny vines!
                Vec3 blightFeet = feetPos.add(0.0D, 0.04D, 0.0D);
                float carapaceAlpha = localFirstPerson ? (ringAlpha * 0.38F) : (ringAlpha * 0.92F);
                renderOrientedRing(matrix, vc, blightFeet, flatX, flatZ, r * 0.84F, r * 1.12F, 18,
                        0.302F, 0.392F, 0.325F, carapaceAlpha * 0.90F);
                for (int p = 0; p < 6; p++) {
                    float pAng = -gameTime * 0.055F + (float) (Math.PI * 2.0D * p / 6.0D);
                    Vec3 outDir = new Vec3(Mth.cos(pAng), 0.0D, Mth.sin(pAng));
                    Vec3 tanU = new Vec3(-Mth.sin(pAng), 0.0D, Mth.cos(pAng));
                    Vec3 tanV = new Vec3(0.0D, 1.0D, 0.0D);
                    Vec3 plateCenter = center.add(outDir.scale(r * 0.98D)).add(0.0D, (p % 2 == 0 ? 0.06D : -0.06D), 0.0D);
                    renderFacetedHexPlate(
                            matrix, vc, plateCenter, tanU, tanV, Math.max(0.23F, r * 0.35F), 0.0F,
                            0.302F, 0.392F, 0.325F, carapaceAlpha * 0.64F,
                            0.204F, 0.800F, 0.282F, carapaceAlpha * 0.94F
                    );
                }
            } else if (ward.primaryMateria == EssenceType.ASTRAL) {
                // Section 6.28 Cosmic Starlight Shroud (#41304E / #FFE600): gyroscopic pulsar rings & orbiting star-sparks!
                Vec3 astralFeet = feetPos.add(0.0D, 0.04D, 0.0D);
                float shroudAlpha = localFirstPerson ? (ringAlpha * 0.38F) : (ringAlpha * 0.92F);
                renderOrientedRing(matrix, vc, astralFeet, flatX, flatZ, r * 0.84F, r * 1.12F, 24,
                        0.255F, 0.188F, 0.306F, shroudAlpha * 0.90F);
                float spin = gameTime * 0.085F;
                Vec3 gyroU = new Vec3(Mth.cos(spin), 0.36D, Mth.sin(spin)).normalize();
                Vec3 gyroV = new Vec3(-Mth.sin(spin), 0.84D, Mth.cos(spin)).normalize();
                renderOrientedRing(matrix, vc, center, gyroU, gyroV, r * 0.92F, r * 1.04F, 24,
                        1.000F, 0.902F, 0.000F, shroudAlpha * 0.92F);
                for (int s = 0; s < 6; s++) {
                    float sAng = -spin * 1.35F + (float) (Math.PI * 2.0D * s / 6.0D);
                    Vec3 starPos = center.add(Mth.cos(sAng) * r * 1.02D, 0.22D * Mth.sin(gameTime * 0.14F + s), Mth.sin(sAng) * r * 1.02D);
                    boolean gold = (s % 2 == 0);
                    renderOctahedron(matrix, vc, starPos, 0.075F,
                            gold ? 1.000F : 0.255F,
                            gold ? 0.902F : 0.188F,
                            gold ? 0.000F : 0.306F,
                            shroudAlpha * 0.96F);
                }
            } else if (ward.primaryMateria == EssenceType.SOULFIRE) {
                // Section 6.29 Phantasmal Soulfire Shroud (#1DF0E5 / #0D6469): roaring cyan soulflames & orbiting soul wisps!
                Vec3 soulFeet = feetPos.add(0.0D, 0.04D, 0.0D);
                float shroudAlpha = localFirstPerson ? (ringAlpha * 0.38F) : (ringAlpha * 0.92F);
                renderOrientedRing(matrix, vc, soulFeet, flatX, flatZ, r * 0.84F, r * 1.14F, 22,
                        0.114F, 0.941F, 0.898F, shroudAlpha * 0.92F);
                for (int f = 0; f < 6; f++) {
                    float fAng = gameTime * 0.075F + (float) (Math.PI * 2.0D * f / 6.0D);
                    Vec3 fBase = soulFeet.add(Mth.cos(fAng) * r * 0.96D, 0.12D, Mth.sin(fAng) * r * 0.96D);
                    Vec3 fTip = fBase.add(0.0D, 0.85D + 0.22D * Mth.sin(gameTime * 0.18F + f), 0.0D);
                    boolean cyan = (f % 2 == 0);
                    renderCrossSegment(matrix, vc, fBase, fTip, 0.058F,
                            cyan ? 0.114F : 0.051F,
                            cyan ? 0.941F : 0.392F,
                            cyan ? 0.898F : 0.412F,
                            shroudAlpha * 0.94F);
                }
            } else if (ward.primaryMateria == EssenceType.VAPOR) {
                // Section 6.30 Mist-Walker Shroud (#E0F7FA / #30A9B0): thick swirling fog bands & aquatic mist hoops!
                Vec3 vaporFeet = feetPos.add(0.0D, 0.04D, 0.0D);
                float shroudAlpha = localFirstPerson ? (ringAlpha * 0.34F) : (ringAlpha * 0.88F);
                renderOrientedRing(matrix, vc, vaporFeet, flatX, flatZ, r * 0.82F, r * 1.18F, 24,
                        0.878F, 0.969F, 0.980F, shroudAlpha * 0.88F);
                renderOrientedRing(matrix, vc, center, flatX, flatZ, r * 0.90F, r * 1.08F, 24,
                        0.188F, 0.663F, 0.690F, shroudAlpha * 0.78F);
            } else if (ward.primaryMateria == EssenceType.NULL_R) {
                // Section 6.31 Event Horizon Bastion (#FFFFFF / #41304E): radiant white light corona with cosmic violet gravity bands!
                Vec3 bastionFeet = feetPos.add(0.0D, 0.045D, 0.0D);
                float coronaAlpha = localFirstPerson ? (ringAlpha * 0.36F) : (ringAlpha * 0.92F);
                renderOrientedRing(matrix, vc, bastionFeet, flatX, flatZ, r * 0.84F, r * 1.18F, 28,
                        1.000F, 1.000F, 1.000F, coronaAlpha * 0.94F);
                float gSpin = gameTime * 0.08F;
                Vec3 gu1 = new Vec3(Mth.cos(gSpin), 0.32D, Mth.sin(gSpin)).normalize();
                Vec3 gv1 = new Vec3(-Mth.sin(gSpin), 0.86D, Mth.cos(gSpin)).normalize();
                renderOrientedRing(matrix, vc, center, gu1, gv1, r * 0.94F, r * 1.10F, 26,
                        0.255F, 0.188F, 0.306F, coronaAlpha * 0.92F);
                Vec3 gu2 = new Vec3(Mth.cos(-gSpin * 0.85F), -0.34D, Mth.sin(-gSpin * 0.85F)).normalize();
                Vec3 gv2 = new Vec3(-Mth.sin(-gSpin * 0.85F), 0.84D, Mth.cos(-gSpin * 0.85F)).normalize();
                renderOrientedRing(matrix, vc, center, gu2, gv2, r * 0.98F, r * 1.14F, 26,
                        1.000F, 1.000F, 1.000F, coronaAlpha * 0.88F);
                for (int n = 0; n < 4; n++) {
                    float nAng = -gSpin * 1.25F + (float) (Math.PI * 0.5D * n);
                    Vec3 node = center.add(Mth.cos(nAng) * r * 1.08D, 0.14D * Mth.sin(gameTime * 0.18F + n), Mth.sin(nAng) * r * 1.08D);
                    boolean white = (n % 2 == 0);
                    renderOctahedron(matrix, vc, node, 0.085F * r,
                            white ? 1.000F : 0.255F,
                            white ? 1.000F : 0.188F,
                            white ? 1.000F : 0.306F,
                            coronaAlpha * 0.96F);
                }
            } else if (ward.primaryMateria == EssenceType.NULL_U) {
                // Section 6.32 Abyssal Zero-Acoustic Veil (#110B17 / #41304E): silent abyssal shroud & cosmic violet event-horizon hoops!
                Vec3 veilFeet = feetPos.add(0.0D, 0.045D, 0.0D);
                float veilAlpha = localFirstPerson ? (ringAlpha * 0.36F) : (ringAlpha * 0.94F);
                renderOrientedRing(matrix, vc, veilFeet, flatX, flatZ, r * 0.84F, r * 1.18F, 28,
                        0.067F, 0.043F, 0.090F, veilAlpha * 0.96F);
                float vSpin = -gameTime * 0.075F;
                Vec3 vu1 = new Vec3(Mth.cos(vSpin), 0.30D, Mth.sin(vSpin)).normalize();
                Vec3 vv1 = new Vec3(-Mth.sin(vSpin), 0.88D, Mth.cos(vSpin)).normalize();
                renderOrientedRing(matrix, vc, center, vu1, vv1, r * 0.94F, r * 1.10F, 26,
                        0.255F, 0.188F, 0.306F, veilAlpha * 0.92F);
                Vec3 vu2 = new Vec3(Mth.cos(-vSpin * 0.82F), -0.32D, Mth.sin(-vSpin * 0.82F)).normalize();
                Vec3 vv2 = new Vec3(-Mth.sin(-vSpin * 0.82F), 0.85D, Mth.cos(-vSpin * 0.82F)).normalize();
                renderOrientedRing(matrix, vc, center, vu2, vv2, r * 0.98F, r * 1.14F, 26,
                        0.067F, 0.043F, 0.090F, veilAlpha * 0.94F);
                for (int n = 0; n < 4; n++) {
                    float nAng = vSpin * 1.20F + (float) (Math.PI * 0.5D * n);
                    Vec3 node = center.add(Mth.cos(nAng) * r * 1.08D, 0.14D * Mth.sin(gameTime * 0.16F + n), Mth.sin(nAng) * r * 1.08D);
                    boolean dark = (n % 2 == 0);
                    renderOctahedron(matrix, vc, node, 0.085F * r,
                            dark ? 0.067F : 0.255F,
                            dark ? 0.043F : 0.188F,
                            dark ? 0.090F : 0.306F,
                            veilAlpha * 0.96F);
                }
            } else if (ward.primaryMateria == EssenceType.DAWN) {
                // Section 6.33 Dawn-Bringer Solar Shroud (#FFEA78 / #A88654): blazing sunrise radiance & swirling morning air hoops!
                Vec3 dawnFeet = feetPos.add(0.0D, 0.045D, 0.0D);
                float shroudAlpha = localFirstPerson ? (ringAlpha * 0.36F) : (ringAlpha * 0.92F);
                renderOrientedRing(matrix, vc, dawnFeet, flatX, flatZ, r * 0.84F, r * 1.18F, 28,
                        0.659F, 0.525F, 0.329F, shroudAlpha * 0.90F);
                float dSpin = gameTime * 0.09F;
                Vec3 du1 = new Vec3(Mth.cos(dSpin), 0.28D, Mth.sin(dSpin)).normalize();
                Vec3 dv1 = new Vec3(-Mth.sin(dSpin), 0.88D, Mth.cos(dSpin)).normalize();
                renderOrientedRing(matrix, vc, center, du1, dv1, r * 0.94F, r * 1.10F, 26,
                        1.000F, 0.918F, 0.471F, shroudAlpha * 0.94F);
                Vec3 du2 = new Vec3(Mth.cos(-dSpin * 0.85F), -0.30D, Mth.sin(-dSpin * 0.85F)).normalize();
                Vec3 dv2 = new Vec3(-Mth.sin(-dSpin * 0.85F), 0.86D, Mth.cos(-dSpin * 0.85F)).normalize();
                renderOrientedRing(matrix, vc, center, du2, dv2, r * 0.98F, r * 1.14F, 26,
                        0.659F, 0.525F, 0.329F, shroudAlpha * 0.88F);
                for (int n = 0; n < 4; n++) {
                    float nAng = dSpin * 1.25F + (float) (Math.PI * 0.5D * n);
                    Vec3 node = center.add(Mth.cos(nAng) * r * 1.08D, 0.14D * Mth.sin(gameTime * 0.20F + n), Mth.sin(nAng) * r * 1.08D);
                    boolean sunrise = (n % 2 == 0);
                    renderOctahedron(matrix, vc, node, 0.085F * r,
                            sunrise ? 1.000F : 0.659F,
                            sunrise ? 0.918F : 0.525F,
                            sunrise ? 0.471F : 0.329F,
                            shroudAlpha * 0.96F);
                }
            } else if (ward.primaryMateria == EssenceType.ABYSS) {
                // Section 6.34 Hadopelagic Trench Shroud (#211A21 / #225C7C): silent pitch-black deep-sea water & ink cloak!
                Vec3 abyssFeet = feetPos.add(0.0D, 0.045D, 0.0D);
                float shroudAlpha = localFirstPerson ? (ringAlpha * 0.34F) : (ringAlpha * 0.94F);
                renderOrientedRing(matrix, vc, abyssFeet, flatX, flatZ, r * 0.84F, r * 1.18F, 28,
                        0.129F, 0.102F, 0.129F, shroudAlpha * 0.95F);
                float aSpin = -gameTime * 0.08F;
                Vec3 au1 = new Vec3(Mth.cos(aSpin), 0.26D, Mth.sin(aSpin)).normalize();
                Vec3 av1 = new Vec3(-Mth.sin(aSpin), 0.88D, Mth.cos(aSpin)).normalize();
                renderOrientedRing(matrix, vc, center, au1, av1, r * 0.94F, r * 1.10F, 26,
                        0.133F, 0.361F, 0.486F, shroudAlpha * 0.92F);
                Vec3 au2 = new Vec3(Mth.cos(-aSpin * 0.85F), -0.28D, Mth.sin(-aSpin * 0.85F)).normalize();
                Vec3 av2 = new Vec3(-Mth.sin(-aSpin * 0.85F), 0.86D, Mth.cos(-aSpin * 0.85F)).normalize();
                renderOrientedRing(matrix, vc, center, au2, av2, r * 0.98F, r * 1.14F, 26,
                        0.129F, 0.102F, 0.129F, shroudAlpha * 0.94F);
                for (int n = 0; n < 4; n++) {
                    float nAng = aSpin * 1.25F + (float) (Math.PI * 0.5D * n);
                    Vec3 node = center.add(Mth.cos(nAng) * r * 1.08D, 0.14D * Mth.sin(gameTime * 0.18F + n), Mth.sin(nAng) * r * 1.08D);
                    boolean ink = (n % 2 == 0);
                    renderOctahedron(matrix, vc, node, 0.085F * r,
                            ink ? 0.129F : 0.133F,
                            ink ? 0.102F : 0.361F,
                            ink ? 0.129F : 0.486F,
                            shroudAlpha * 0.96F);
                }
            } else if (ward.primaryMateria == EssenceType.AEGIS && ward.secondaryMateria == EssenceType.RADIANT) {
                // Section 6.35 Gilded Bastion Citadel Mantle (#FFEA78 / #773214): interlocking golden granite & radiant quartz armor plates!
                Vec3 aegisFeet = feetPos.add(0.0D, 0.045D, 0.0D);
                float mantleAlpha = localFirstPerson ? (ringAlpha * 0.38F) : (ringAlpha * 0.96F);
                renderOrientedRing(matrix, vc, aegisFeet, flatX, flatZ, r * 0.86F, r * 1.22F, 8,
                        0.467F, 0.196F, 0.078F, mantleAlpha * 0.95F);
                renderOrientedRing(matrix, vc, center.add(0.0D, r * 0.82D, 0.0D), flatX, flatZ, r * 0.42F, r * 0.64F, 24,
                        1.000F, 0.918F, 0.471F, mantleAlpha * 0.96F);
                for (int p = 0; p < 6; p++) {
                    float pAng = gameTime * 0.055F + (float) (Math.PI * 2.0D * p / 6.0D);
                    Vec3 outDir = new Vec3(Mth.cos(pAng), 0.0D, Mth.sin(pAng));
                    Vec3 tanU = new Vec3(-Mth.sin(pAng), 0.0D, Mth.cos(pAng));
                    Vec3 tanV = new Vec3(0.0D, 1.0D, 0.0D);
                    Vec3 plateCenter = center.add(outDir.scale(r * 1.02D));
                    renderFacetedHexPlate(
                            matrix, vc, plateCenter, tanU, tanV, Math.max(0.24F, r * 0.36F), 0.0F,
                            0.467F, 0.196F, 0.078F, mantleAlpha * 0.82F,
                            1.000F, 0.918F, 0.471F, mantleAlpha * 0.99F
                    );
                }
            } else if (ward.primaryMateria == EssenceType.AURORA) {
                // Section 6.36 Borealis Prismatic Veil Mantle (#FFEA78 / #98DCF2): dancing polar light curtains & hoarfrost crystal seal!
                Vec3 auroraFeet = feetPos.add(0.0D, 0.045D, 0.0D);
                float veilAlpha = localFirstPerson ? (ringAlpha * 0.36F) : (ringAlpha * 0.95F);
                renderSnowflakeCrystal(matrix, vc, auroraFeet, flatX, flatZ, r * 1.12F, gameTime * 0.04F, veilAlpha * 0.92F);
                renderOrientedRing(matrix, vc, center.add(0.0D, r * 0.84D, 0.0D), flatX, flatZ, r * 0.42F, r * 0.62F, 24,
                        1.000F, 0.918F, 0.471F, veilAlpha * 0.96F);
                for (int c = 0; c < 6; c++) {
                    float cAng = gameTime * 0.065F + (float) (Math.PI * 2.0D * c / 6.0D);
                    double wave = 0.08D * Mth.sin(gameTime * 0.18F + c * 1.1F);
                    Vec3 topPt = center.add(Mth.cos(cAng) * r * 0.94D, r * 0.68D + wave, Mth.sin(cAng) * r * 0.94D);
                    Vec3 botPt = center.add(Mth.cos(cAng + 0.18F) * r * 1.02D, -r * 0.62D + wave, Mth.sin(cAng + 0.18F) * r * 1.02D);
                    boolean gold = (c % 2 == 0);
                    renderCrossSegment(matrix, vc, topPt, botPt, 0.056F,
                            gold ? 1.000F : 0.596F,
                            gold ? 0.918F : 0.863F,
                            gold ? 0.471F : 0.949F,
                            veilAlpha * 0.90F);
                }
            } else if (ward.primaryMateria == EssenceType.MIRAGE) {
                // Section 6.37 Spatial Displacement Glamour Shroud (#FFEA78 / #C2B28F): heat-haze distortion hoops & 3-block displaced phantom double!
                Vec3 mirageFeet = feetPos.add(0.0D, 0.045D, 0.0D);
                float glamourAlpha = localFirstPerson ? (ringAlpha * 0.34F) : (ringAlpha * 0.94F);
                renderOrientedStarPolygon(matrix, vc, mirageFeet, flatX, flatZ, r * 1.10F, 8, gameTime * 0.045F,
                        1.000F, 0.918F, 0.471F, glamourAlpha * 0.90F);
                renderOrientedRing(matrix, vc, center.add(0.0D, r * 0.78D, 0.0D), flatX, flatZ, r * 0.42F, r * 0.62F, 20,
                        0.761F, 0.698F, 0.561F, glamourAlpha * 0.92F);
                for (int h = 0; h < 4; h++) {
                    double waveY = (h - 1.5D) * r * 0.36D + 0.05D * Mth.sin(gameTime * 0.24F + h * 1.3F);
                    float wr = r * (0.94F + 0.08F * Mth.sin(gameTime * 0.20F + h));
                    boolean gold = (h % 2 == 0);
                    renderOrientedRing(matrix, vc, center.add(0.0D, waveY, 0.0D), flatX, flatZ, wr * 0.88F, wr, 20,
                            gold ? 1.000F : 0.761F,
                            gold ? 0.918F : 0.698F,
                            gold ? 0.471F : 0.561F,
                            glamourAlpha * 0.76F);
                }
                // Flickering 3-block spatially displaced 3D phantom double!
                float dispAng = gameTime * 0.032F;
                float flicker = 0.55F + 0.40F * Mth.sin(gameTime * 0.36F);
                Vec3 phantomCenter = center.add(Mth.cos(dispAng) * 3.0D, 0.0D, Mth.sin(dispAng) * 3.0D);
                Vec3 phantomFeet = mirageFeet.add(Mth.cos(dispAng) * 3.0D, 0.0D, Mth.sin(dispAng) * 3.0D);
                renderOrientedRing(matrix, vc, phantomFeet, flatX, flatZ, 0.36F, 0.58F, 16,
                        0.761F, 0.698F, 0.561F, glamourAlpha * 0.78F * flicker);
                renderOctahedron(matrix, vc, phantomCenter, Math.max(0.32F, r * 0.42F),
                        1.000F, 0.918F, 0.471F, glamourAlpha * 0.72F * flicker);
                renderCrossSegment(matrix, vc, phantomFeet, phantomCenter.add(0.0D, r * 0.78D, 0.0D), 0.14F,
                        0.761F, 0.698F, 0.561F, glamourAlpha * 0.64F * flicker);
            } else if (ward.primaryMateria == EssenceType.WRAITH) {
                // Section 6.38 Specter Ectoplasm Armor (#41304E / #4D6453): heavy translucent void-ectoplasm plates & sculk-silent wraith rings!
                Vec3 wraithFeet = feetPos.add(0.0D, 0.045D, 0.0D);
                float ectoAlpha = localFirstPerson ? (ringAlpha * 0.36F) : (ringAlpha * 0.94F);
                renderOrientedStarPolygon(matrix, vc, wraithFeet, flatX, flatZ, r * 1.14F, 8, -gameTime * 0.040F,
                        0.255F, 0.188F, 0.306F, ectoAlpha * 0.94F);
                renderOrientedRing(matrix, vc, center.add(0.0D, r * 0.80D, 0.0D), flatX, flatZ, r * 0.44F, r * 0.64F, 20,
                        0.302F, 0.392F, 0.325F, ectoAlpha * 0.92F);
                for (int p = 0; p < 6; p++) {
                    float pAng = -gameTime * 0.050F + (float) (Math.PI * 2.0D * p / 6.0D);
                    Vec3 outDir = new Vec3(Mth.cos(pAng), 0.0D, Mth.sin(pAng));
                    Vec3 tanU = new Vec3(-Mth.sin(pAng), 0.0D, Mth.cos(pAng));
                    Vec3 tanV = new Vec3(0.0D, 1.0D, 0.0D);
                    Vec3 plateCenter = center.add(outDir.scale(r * 1.00D));
                    renderFacetedHexPlate(
                            matrix, vc, plateCenter, tanU, tanV, Math.max(0.24F, r * 0.35F), 0.0F,
                            0.255F, 0.188F, 0.306F, ectoAlpha * 0.78F,
                            0.302F, 0.392F, 0.325F, ectoAlpha * 0.96F
                    );
                }
            } else if (ward.primaryMateria == EssenceType.BARROW) {
                // Section 6.39 Catacomb Miasma Shroud (#773214 / #4D6453): dense ancient tomb-fog rings & mossy gravestone ward slabs!
                Vec3 barrowFeet = feetPos.add(0.0D, 0.045D, 0.0D);
                float tombAlpha = localFirstPerson ? (ringAlpha * 0.36F) : (ringAlpha * 0.94F);
                renderOrientedRing(matrix, vc, barrowFeet, flatX, flatZ, r * 0.82F, r * 1.24F, 12,
                        0.467F, 0.196F, 0.078F, tombAlpha * 0.94F);
                renderOrientedStarPolygon(matrix, vc, barrowFeet.add(0.0D, 0.015D, 0.0D), flatX, flatZ, r * 1.12F, 6, gameTime * 0.035F,
                        0.302F, 0.392F, 0.325F, tombAlpha * 0.90F);
                for (int f = 0; f < 4; f++) {
                    double fogY = (f - 1.5D) * r * 0.34D;
                    float fr = r * (0.98F + 0.08F * Mth.sin(gameTime * 0.18F + f));
                    boolean moss = (f % 2 == 0);
                    renderOrientedRing(matrix, vc, center.add(0.0D, fogY, 0.0D), flatX, flatZ, fr * 0.86F, fr, 20,
                            moss ? 0.302F : 0.467F,
                            moss ? 0.392F : 0.196F,
                            moss ? 0.325F : 0.078F,
                            tombAlpha * 0.78F);
                }
            } else if (ward.primaryMateria == EssenceType.PYRE) {
                // Section 6.40 Pyre Hearth Hearthstone Shroud (#773214 / #FF4500): heavy glowing coal & burning cedar armor plates!
                Vec3 pyreFeet = feetPos.add(0.0D, 0.045D, 0.0D);
                float hearthAlpha = localFirstPerson ? (ringAlpha * 0.36F) : (ringAlpha * 0.95F);
                renderOrientedRing(matrix, vc, pyreFeet, flatX, flatZ, r * 0.82F, r * 1.25F, 12,
                        0.467F, 0.196F, 0.078F, hearthAlpha * 0.96F);
                renderOrientedStarPolygon(matrix, vc, pyreFeet.add(0.0D, 0.015D, 0.0D), flatX, flatZ, r * 1.14F, 6, gameTime * 0.045F,
                        1.000F, 0.271F, 0.000F, hearthAlpha * 0.94F);
                for (int h = 0; h < 4; h++) {
                    double bandY = (h - 1.5D) * r * 0.34D;
                    float hr = r * (0.98F + 0.06F * Mth.sin(gameTime * 0.24F + h));
                    boolean flame = (h % 2 == 0);
                    renderOrientedRing(matrix, vc, center.add(0.0D, bandY, 0.0D), flatX, flatZ, hr * 0.85F, hr, 16,
                            flame ? 1.000F : 0.467F,
                            flame ? 0.271F : 0.196F,
                            flame ? 0.000F : 0.078F,
                            hearthAlpha * 0.82F);
                }
            } else if (ward.primaryMateria == EssenceType.PENUMBRA) {
                // Section 6.41 Penumbral Corona Mantle (#FFEA78 / #281E3C): oscillating coronal flares & deep twilight shadow rings!
                Vec3 penFeet = feetPos.add(0.0D, 0.045D, 0.0D);
                float penAlpha = localFirstPerson ? (ringAlpha * 0.36F) : (ringAlpha * 0.95F);
                renderOrientedRing(matrix, vc, penFeet, flatX, flatZ, r * 0.82F, r * 1.25F, 24,
                        0.157F, 0.118F, 0.235F, penAlpha * 0.96F);
                renderOrientedStarPolygon(matrix, vc, penFeet.add(0.0D, 0.015D, 0.0D), flatX, flatZ, r * 1.15F, 8, gameTime * 0.045F,
                        1.000F, 0.918F, 0.471F, penAlpha * 0.94F);
                for (int p = 0; p < 4; p++) {
                    double py = (p - 1.5D) * r * 0.34D;
                    float prRad = r * (0.98F + 0.07F * Mth.sin(gameTime * 0.22F + p));
                    boolean corona = (p % 2 == 0);
                    renderOrientedRing(matrix, vc, center.add(0.0D, py, 0.0D), flatX, flatZ, prRad * 0.86F, prRad, 22,
                            corona ? 1.000F : 0.157F,
                            corona ? 0.918F : 0.118F,
                            corona ? 0.471F : 0.235F,
                            penAlpha * 0.82F);
                }
            } else if (ward.primaryMateria == EssenceType.RIME) {
                // Section 6.42 Hoarfrost Glacial Aegis Mantle (#6395B1 / #DCF0FF): crystalline hoarfrost plates & howling sub-zero updrafts!
                Vec3 rimeFeet = feetPos.add(0.0D, 0.045D, 0.0D);
                float rimeAlpha = localFirstPerson ? (ringAlpha * 0.36F) : (ringAlpha * 0.95F);
                renderOrientedRing(matrix, vc, rimeFeet, flatX, flatZ, r * 0.82F, r * 1.25F, 6,
                        0.388F, 0.584F, 0.694F, rimeAlpha * 0.96F);
                renderOrientedStarPolygon(matrix, vc, rimeFeet.add(0.0D, 0.015D, 0.0D), flatX, flatZ, r * 1.16F, 6, gameTime * 0.045F,
                        0.863F, 0.941F, 1.000F, rimeAlpha * 0.95F);
                for (int h = 0; h < 4; h++) {
                    double hy = (h - 1.5D) * r * 0.34D;
                    float hr = r * (0.98F + 0.07F * Mth.sin(gameTime * 0.22F + h));
                    boolean white = (h % 2 == 0);
                    renderOrientedRing(matrix, vc, center.add(0.0D, hy, 0.0D), flatX, flatZ, hr * 0.85F, hr, 12,
                            white ? 0.863F : 0.388F,
                            white ? 0.941F : 0.584F,
                            white ? 1.000F : 0.694F,
                            rimeAlpha * 0.82F);
                }
            } else if (ward.primaryMateria == EssenceType.SPRING) {
                // Section 6.43 Vernal Fountainhead Mantle (#FF6B9D / #64C8FF): bubbling aquifer springwater cloak & 5m restorative blossom aura!
                Vec3 springFeet = feetPos.add(0.0D, 0.045D, 0.0D);
                float springAlpha = localFirstPerson ? (ringAlpha * 0.36F) : (ringAlpha * 0.94F);
                renderOrientedRing(matrix, vc, springFeet, flatX, flatZ, r * 0.82F, r * 1.24F, 24,
                        0.392F, 0.784F, 1.000F, springAlpha * 0.95F);
                renderOrientedStarPolygon(matrix, vc, springFeet.add(0.0D, 0.015D, 0.0D), flatX, flatZ, r * 1.16F, 5, gameTime * 0.05F,
                        1.000F, 0.420F, 0.616F, springAlpha * 0.95F);
                for (int h = 0; h < 4; h++) {
                    double hy = (h - 1.5D) * r * 0.34D;
                    float hr = r * (0.98F + 0.07F * Mth.sin(gameTime * 0.24F + h));
                    boolean blossom = (h % 2 == 0);
                    renderOrientedRing(matrix, vc, center.add(0.0D, hy, 0.0D), flatX, flatZ, hr * 0.86F, hr, 20,
                            blossom ? 1.000F : 0.392F,
                            blossom ? 0.420F : 0.784F,
                            blossom ? 0.616F : 1.000F,
                            springAlpha * 0.82F);
                }
            } else if (ward.primaryMateria == EssenceType.STATIC) {
                // Section 6.44 Dielectric Faraday Shroud (#FFE600 / #878DA2): shimmering Faraday mesh cage of static sparks & dielectric slate bands!
                Vec3 staticFeet = feetPos.add(0.0D, 0.045D, 0.0D);
                float staticAlpha = localFirstPerson ? (ringAlpha * 0.36F) : (ringAlpha * 0.95F);
                renderOrientedRing(matrix, vc, staticFeet, flatX, flatZ, r * 0.82F, r * 1.24F, 8,
                        0.529F, 0.553F, 0.635F, staticAlpha * 0.96F);
                renderOrientedStarPolygon(matrix, vc, staticFeet.add(0.0D, 0.015D, 0.0D), flatX, flatZ, r * 1.18F, 8, gameTime * 0.065F,
                        1.000F, 0.902F, 0.000F, staticAlpha * 0.98F);
                for (int h = 0; h < 4; h++) {
                    double hy = (h - 1.5D) * r * 0.34D;
                    float hr = r * (0.98F + 0.07F * Mth.sin(gameTime * 0.30F + h));
                    boolean yellow = (h % 2 == 0);
                    renderOrientedRing(matrix, vc, center.add(0.0D, hy, 0.0D), flatX, flatZ, hr * 0.86F, hr, 12,
                            yellow ? 1.000F : 0.529F,
                            yellow ? 0.902F : 0.553F,
                            yellow ? 0.000F : 0.635F,
                            staticAlpha * 0.84F);
                }
            } else if (ward.primaryMateria == EssenceType.SYLVAN) {
                // Section 6.45 Primeval Photosynthetic Aegis (#1A5923 / #773214 / #FFEA78): petrified ironwood plates & solar leaves!
                Vec3 sylvanFeet = feetPos.add(0.0D, 0.045D, 0.0D);
                float sylAlpha = localFirstPerson ? (ringAlpha * 0.36F) : (ringAlpha * 0.95F);
                renderOrientedRing(matrix, vc, sylvanFeet, flatX, flatZ, r * 0.82F, r * 1.24F, 12,
                        0.467F, 0.196F, 0.078F, sylAlpha * 0.96F);
                renderOrientedStarPolygon(matrix, vc, sylvanFeet.add(0.0D, 0.015D, 0.0D), flatX, flatZ, r * 1.18F, 6, gameTime * 0.05F,
                        1.000F, 0.918F, 0.471F, sylAlpha * 0.97F);
                for (int h = 0; h < 4; h++) {
                    double hy = (h - 1.5D) * r * 0.34D;
                    float hr = r * (0.98F + 0.07F * Mth.sin(gameTime * 0.22F + h));
                    boolean gold = (h % 2 == 0);
                    renderOrientedRing(matrix, vc, center.add(0.0D, hy, 0.0D), flatX, flatZ, hr * 0.86F, hr, 16,
                            gold ? 1.000F : 0.102F,
                            gold ? 0.918F : 0.349F,
                            gold ? 0.471F : 0.137F,
                            sylAlpha * 0.84F);
                }
            } else if (ward.primaryMateria == EssenceType.MIASMA) {
                // Section 6.46 Putrescent Mire-Carapace (#1A5923 / #4D6453 / #225C7C): petrified bog peat, necrotic algae & virulent vapor!
                Vec3 miasmaFeet = feetPos.add(0.0D, 0.045D, 0.0D);
                float miaAlpha = localFirstPerson ? (ringAlpha * 0.36F) : (ringAlpha * 0.95F);
                renderOrientedRing(matrix, vc, miasmaFeet, flatX, flatZ, r * 0.80F, r * 1.24F, 14,
                        0.302F, 0.392F, 0.325F, miaAlpha * 0.95F);
                renderOrientedStarPolygon(matrix, vc, miasmaFeet.add(0.0D, 0.015D, 0.0D), flatX, flatZ, r * 1.16F, 6, gameTime * 0.045F,
                        0.133F, 0.361F, 0.486F, miaAlpha * 0.96F);
                for (int h = 0; h < 4; h++) {
                    double hy = (h - 1.5D) * r * 0.34D;
                    float hr = r * (0.98F + 0.08F * Mth.sin(gameTime * 0.24F + h));
                    boolean green = (h % 2 == 0);
                    renderOrientedRing(matrix, vc, center.add(0.0D, hy, 0.0D), flatX, flatZ, hr * 0.86F, hr, 16,
                            green ? 0.102F : 0.133F,
                            green ? 0.349F : 0.361F,
                            green ? 0.137F : 0.486F,
                            miaAlpha * 0.84F);
                }
            } else if (ward.primaryMateria == EssenceType.GENESIS) {
                // Section 6.47 Amniotic Genesis Shroud (#FF6B9D / #FFEA78 / #225C7C): radiant dawn sunlight, pink vitae currents & sacred oceanic water!
                Vec3 genFeet = feetPos.add(0.0D, 0.045D, 0.0D);
                float genAlpha = localFirstPerson ? (ringAlpha * 0.36F) : (ringAlpha * 0.96F);
                renderOrientedRing(matrix, vc, genFeet, flatX, flatZ, r * 0.82F, r * 1.24F, 18,
                        0.133F, 0.361F, 0.486F, genAlpha * 0.95F);
                renderOrientedStarPolygon(matrix, vc, genFeet.add(0.0D, 0.015D, 0.0D), flatX, flatZ, r * 1.18F, 6, gameTime * 0.048F,
                        1.000F, 0.918F, 0.471F, genAlpha * 0.98F);
                for (int h = 0; h < 4; h++) {
                    double hy = (h - 1.5D) * r * 0.34D;
                    float hr = r * (0.98F + 0.08F * Mth.sin(gameTime * 0.25F + h));
                    boolean pink = (h % 2 == 0);
                    renderOrientedRing(matrix, vc, center.add(0.0D, hy, 0.0D), flatX, flatZ, hr * 0.86F, hr, 18,
                            1.000F,
                            pink ? 0.420F : 0.918F,
                            pink ? 0.616F : 0.471F,
                            genAlpha * 0.86F);
                }
            } else if (ward.primaryMateria == EssenceType.OBLIVION) {
                // Section 6.48 Stygian Extinction Armor & Oblivion Void-Shroud (#B83C08 / #211A21 / #4D6453): obsidian plates & magma ash rings!
                Vec3 oblFeet = feetPos.add(0.0D, 0.045D, 0.0D);
                float oblAlpha = localFirstPerson ? (ringAlpha * 0.36F) : (ringAlpha * 0.96F);
                renderOrientedRing(matrix, vc, oblFeet, flatX, flatZ, r * 0.82F, r * 1.26F, 16,
                        0.129F, 0.102F, 0.129F, oblAlpha * 0.96F);
                renderOrientedStarPolygon(matrix, vc, oblFeet.add(0.0D, 0.015D, 0.0D), flatX, flatZ, r * 1.18F, 6, -gameTime * 0.050F,
                        0.722F, 0.235F, 0.031F, oblAlpha * 0.98F);
                for (int h = 0; h < 4; h++) {
                    double hy = (h - 1.5D) * r * 0.34D;
                    float hr = r * (0.98F + 0.08F * Mth.sin(gameTime * 0.26F + h));
                    boolean rust = (h % 2 == 0);
                    renderOrientedRing(matrix, vc, center.add(0.0D, hy, 0.0D), flatX, flatZ, hr * 0.86F, hr, 16,
                            rust ? 0.722F : 0.302F,
                            rust ? 0.235F : 0.392F,
                            rust ? 0.031F : 0.325F,
                            oblAlpha * 0.88F);
                }
            } else if (ward.primaryMateria == EssenceType.AETHER) {
                // Section 6.50 Transcendent Firmament Quintessence Shroud & Aegis (#A88654 / #773214 / #225C7C / #B83C08):
                Vec3 aethFeet = feetPos.add(0.0D, 0.045D, 0.0D);
                float aethAlpha = localFirstPerson ? (ringAlpha * 0.36F) : (ringAlpha * 0.96F);
                renderOrientedRing(matrix, vc, aethFeet, flatX, flatZ, r * 0.84F, r * 1.28F, 22,
                        0.659F, 0.525F, 0.329F, aethAlpha * 0.96F);
                renderOrientedStarPolygon(matrix, vc, aethFeet.add(0.0D, 0.015D, 0.0D), flatX, flatZ, r * 1.20F, 8, gameTime * 0.052F,
                        0.133F, 0.361F, 0.486F, aethAlpha * 0.98F);
                float[][] quadRgb = {
                        {0.659F, 0.525F, 0.329F},
                        {0.467F, 0.196F, 0.078F},
                        {0.133F, 0.361F, 0.486F},
                        {0.722F, 0.235F, 0.031F}
                };
                for (int h = 0; h < 4; h++) {
                    double hy = (h - 1.5D) * r * 0.34D;
                    float hr = r * (0.98F + 0.08F * Mth.sin(gameTime * 0.26F + h));
                    float[] c = quadRgb[h];
                    renderOrientedRing(matrix, vc, center.add(0.0D, hy, 0.0D), flatX, flatZ, hr * 0.86F, hr, 20,
                            c[0], c[1], c[2], aethAlpha * 0.90F);
                }
            } else if (ward.primaryMateria == EssenceType.ENTROPIC) {
                // Section 6.51 Abyssal Event Horizon Mantle & Apex Entropic Shroud (#41304E / #211A21 / #4D6453 / #8A0303):
                Vec3 entFeet = feetPos.add(0.0D, 0.045D, 0.0D);
                float entAlpha = localFirstPerson ? (ringAlpha * 0.36F) : (ringAlpha * 0.96F);
                renderOrientedRing(matrix, vc, entFeet, flatX, flatZ, r * 0.84F, r * 1.28F, 20,
                        0.129F, 0.102F, 0.129F, entAlpha * 0.97F);
                renderOrientedStarPolygon(matrix, vc, entFeet.add(0.0D, 0.015D, 0.0D), flatX, flatZ, r * 1.20F, 8, -gameTime * 0.052F,
                        0.541F, 0.012F, 0.012F, entAlpha * 0.98F);
                float[][] entRgb = {
                        {0.255F, 0.188F, 0.306F},
                        {0.129F, 0.102F, 0.129F},
                        {0.302F, 0.392F, 0.325F},
                        {0.541F, 0.012F, 0.012F}
                };
                for (int h = 0; h < 4; h++) {
                    double hy = (h - 1.5D) * r * 0.34D;
                    float hr = r * (0.98F + 0.08F * Mth.sin(gameTime * 0.26F + h));
                    float[] c = entRgb[h];
                    renderOrientedRing(matrix, vc, center.add(0.0D, hy, 0.0D), flatX, flatZ, hr * 0.86F, hr, 20,
                            c[0], c[1], c[2], entAlpha * 0.90F);
                }
            } else if (ward.primaryMateria == EssenceType.CELESTIAL) {
                // Section 6.52 Holy Celestial Supernova Shroud & Ascendance Shroud (#FFEA78 / #FF6B9D / #1A5923 / #A88654):
                Vec3 celFeet = feetPos.add(0.0D, 0.045D, 0.0D);
                float celAlpha = localFirstPerson ? (ringAlpha * 0.36F) : (ringAlpha * 0.96F);
                renderOrientedRing(matrix, vc, celFeet, flatX, flatZ, r * 0.84F, r * 1.28F, 22,
                        1.000F, 0.918F, 0.471F, celAlpha * 0.97F);
                renderOrientedStarPolygon(matrix, vc, celFeet.add(0.0D, 0.015D, 0.0D), flatX, flatZ, r * 1.20F, 8, gameTime * 0.052F,
                        1.000F, 0.420F, 0.616F, celAlpha * 0.98F);
                float[][] celRgb = {
                        {1.000F, 0.918F, 0.471F},
                        {1.000F, 0.420F, 0.616F},
                        {0.102F, 0.349F, 0.137F},
                        {0.659F, 0.525F, 0.329F}
                };
                for (int h = 0; h < 4; h++) {
                    double hy = (h - 1.5D) * r * 0.34D;
                    float hr = r * (0.98F + 0.08F * Mth.sin(gameTime * 0.26F + h));
                    float[] c = celRgb[h];
                    renderOrientedRing(matrix, vc, center.add(0.0D, hy, 0.0D), flatX, flatZ, hr * 0.86F, hr, 20,
                            c[0], c[1], c[2], celAlpha * 0.90F);
                }
            }
        }

        // 5. Directional Hit-Impact Hex-Shield Flash & Dual Spherical Shockwave Ripples!
        if (ward.hasActiveRipple(partialTick)) {
            float rProg = ward.getRippleProgress(partialTick);
            float rFade = (1.0F - rProg) * (1.0F - rProg * 0.45F) * env;
            Vec3 hitDir = new Vec3(ward.hitDirX, ward.hitDirY, ward.hitDirZ).normalize();
            Vec3 tanU = buildPerpendicularRight(hitDir);
            Vec3 tanV = hitDir.cross(tanU).normalize();

            // Incandescent hexagonal bastion flare right at point of impact
            Vec3 impactSurface = center.add(hitDir.scale(r * 0.99D));
            float impactHexRad = r * (0.34F + 0.18F * rProg);
            renderFacetedHexPlate(
                    matrix, vc, impactSurface, tanU, tanV, impactHexRad, rProg * 0.35F,
                    ar * 0.45F + 0.55F, ag * 0.45F + 0.55F, ab * 0.45F + 0.55F, 0.66F * rFade,
                    1.0F, 1.0F, 1.0F, 0.98F * rFade
            );

            // Outer + inner spherical shockwave rings sweeping outward across the curvature of the dome!
            float theta1 = 0.14F + rProg * 1.18F;
            float ringR1 = r * 0.985F * Mth.sin(theta1);
            Vec3 chord1 = center.add(hitDir.scale(r * 0.985D * Mth.cos(theta1)));
            renderOrientedRing(matrix, vc, chord1, tanU, tanV,
                    Math.max(0.02F, ringR1 - 0.065F * r), ringR1, 24,
                    1.0F, 1.0F, 1.0F, 0.92F * rFade);

            if (rProg > 0.12F) {
                float theta2 = 0.10F + (rProg - 0.12F) * 0.92F;
                float ringR2 = r * 0.985F * Mth.sin(theta2);
                Vec3 chord2 = center.add(hitDir.scale(r * 0.985D * Mth.cos(theta2)));
                renderOrientedRing(matrix, vc, chord2, tanU, tanV,
                        Math.max(0.02F, ringR2 - 0.048F * r), ringR2, 20,
                        ar, ag, ab, 0.78F * rFade);
            }
        }
    }

    private static void renderFacetedHexPlate(
            Matrix4f matrix,
            VertexConsumer vc,
            Vec3 center,
            Vec3 axisU,
            Vec3 axisV,
            float radius,
            float rotRad,
            float fr, float fg, float fb, float fillAlpha,
            float rr, float rg, float rb, float rimAlpha
    ) {
        if (radius <= 0.015F || (fillAlpha <= 0.004F && rimAlpha <= 0.004F)) return;
        Vec3[] pts = new Vec3[6];
        for (int i = 0; i < 6; i++) {
            float ang = rotRad + (float) (Math.PI * 2.0D * i / 6.0D);
            pts[i] = center.add(axisU.scale(Mth.cos(ang) * radius)).add(axisV.scale(Mth.sin(ang) * radius));
        }

        if (fillAlpha > 0.004F) {
            // Two quads cleanly cover a convex regular hexagon: (0,1,2,3) and (3,4,5,0)
            emitDoubleSidedFlatQuad(matrix, vc, pts[0], pts[1], pts[2], pts[3], fr, fg, fb, fillAlpha);
            emitDoubleSidedFlatQuad(matrix, vc, pts[3], pts[4], pts[5], pts[0], fr, fg, fb, fillAlpha);
        }

        if (rimAlpha > 0.004F) {
            float rimHalfW = Math.max(0.016F, radius * 0.065F);
            for (int i = 0; i < 6; i++) {
                renderCrossSegment(matrix, vc, pts[i], pts[(i + 1) % 6], rimHalfW, rr, rg, rb, rimAlpha);
            }
        }
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
        float fade = computeEnvelope(progress, 0.08F, 0.58F);
        float r = inst.radiusOrScale;
        Vec3 base = inst.start.add(0.0D, 0.05D, 0.0D);
        Vec3 right = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 forward = new Vec3(0.0D, 0.0D, 0.0D).add(0.0D, 0.0D, 1.0D);

        // Ground ritual circle + inscribed star
        renderOrientedRing(matrix, vc, base, right, forward, r * 0.90F, r, 28, pr, pg, pb, 0.82F * fade);
        renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.015D, 0.0D), right, forward,
                r * 0.90F, isAerodynamicMateria(inst.essence) ? 8 : 6, gameTime * 0.07F + inst.seed, ar, ag, ab, 0.78F * fade);

        // Rising ritual column (reaches full height crisply, then holds luminous sanctuary column)
        float colSurge = Mth.clamp(progress / 0.28F, 0.0F, 1.0F);
        double colH = 3.8D * (0.35F + 0.65F * (1.0F - (1.0F - colSurge) * (1.0F - colSurge)));
        Vec3 top = base.add(0.0D, colH, 0.0D);
        float colW = 0.35F + 0.65F * fade;
        renderTube(matrix, vc, base, top, r * 0.32F * colW, 12, pr, pg, pb, 0.38F * fade, FULL_LIGHT);
        renderTube(matrix, vc, base, top, r * 0.12F * colW, 8, 1.0F, 1.0F, 1.0F, 0.82F * fade, FULL_LIGHT);

        // Three continuously ascending halo rings lifting up the summon column
        for (int h = 0; h < 3; h++) {
            float ht = ((gameTime * 0.06F + h / 3.0F) % 1.0F);
            Vec3 hPos = base.add(0.0D, ht * colH, 0.0D);
            float hFade = fade * Mth.sin(ht * (float) Math.PI);
            renderOrientedRing(matrix, vc, hPos, right, forward,
                    r * 0.65F, r * 0.84F, 22, ar, ag, ab, 0.78F * hFade);
        }
    }

    /**
     * VFX 11: Towering 14-tier Volumetric Supercell Tornado Vortex with a continuous translucent swaying funnel wall,
     * counter-spinning luminous inner eye-wall column, 6 wide helical slipstream ribbons, 8 orbiting razor wind-crescents,
     * logarithmic ground suction spiral, and flared upper storm-anvil crown.
     */
    private static void renderTornadoVortex(
            Matrix4f matrix,
            VertexConsumer vc,
            SpellWorldVfxManager.ActiveVfxInstance inst,
            float progress,
            float gameTime,
            float pr, float pg, float pb,
            float ar, float ag, float ab
    ) {
        float fade = computeEnvelope(progress, 0.08F, 0.85F);
        Vec3 base = inst.start.add(0.0D, 0.06D, 0.0D);
        double totalH = Math.max(5.5D, inst.endOrDir.y - inst.start.y);
        float maxR = Math.max(1.8F, inst.radiusOrScale);

        Vec3 right = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 forward = new Vec3(0.0D, 0.0D, 1.0D);

        float baseEyeR = maxR * 0.26F;

        // 1. Ground Wind-Rose eye + 4 logarithmic inward suction spirals across the floor
        renderOrientedRing(matrix, vc, base, right, forward, baseEyeR * 0.58F, baseEyeR * 1.12F, 26, ar, ag, ab, 0.88F * fade);
        renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.02D, 0.0D), right, forward,
                maxR * 0.62F, 8, gameTime * 0.18F + inst.seed, pr, pg, pb, 0.66F * fade);
        renderOrientedRing(matrix, vc, base.add(0.0D, 0.03D, 0.0D), right, forward, maxR * 0.88F, maxR * 1.00F, 32, pr, pg, pb, 0.50F * fade);

        for (int arm = 0; arm < 4; arm++) {
            float armBase = gameTime * 0.28F + (float) (Math.PI * 0.5D * arm);
            for (int s = 0; s < 10; s++) {
                float u0 = s / 10.0F;
                float u1 = (s + 1) / 10.0F;
                float gr0 = Mth.lerp(u0, maxR * 0.98F, baseEyeR * 0.72F);
                float gr1 = Mth.lerp(u1, maxR * 0.98F, baseEyeR * 0.72F);
                float ga0 = armBase + u0 * 2.8F;
                float ga1 = armBase + u1 * 2.8F;
                Vec3 gp0 = base.add(Mth.cos(ga0) * gr0, 0.04D + u0 * 0.18D, Mth.sin(ga0) * gr0);
                Vec3 gp1 = base.add(Mth.cos(ga1) * gr1, 0.04D + u1 * 0.18D, Mth.sin(ga1) * gr1);
                renderCrossSegment(matrix, vc, gp0, gp1, 0.055F, ar, ag, ab, 0.72F * fade * u1);
            }
        }

        // Precompute swaying spine centers & radii across 14 vertical tiers
        int tiers = 14;
        Vec3[] centers = new Vec3[tiers];
        float[] outerRadii = new float[tiers];
        float[] innerRadii = new float[tiers];
        for (int b = 0; b < tiers; b++) {
            float t = b / (float) (tiers - 1);
            double y = t * totalH;
            double swayAmp = 0.30D * t * maxR * 0.36D;
            double swayX = Math.sin(gameTime * 0.16D + t * 3.2D + inst.seed) * swayAmp;
            double swayZ = Math.cos(gameTime * 0.14D + t * 2.8D + inst.seed) * swayAmp;
            centers[b] = base.add(swayX, y, swayZ);
            float curve = t * t * 0.68F + t * 0.32F;
            outerRadii[b] = Mth.lerp(curve, baseEyeR, maxR * 1.08F);
            innerRadii[b] = outerRadii[b] * 0.42F;
        }

        // 2. Continuous Volumetric Outer Cyclone Wall + Luminous Inner Eye-Wall Core!
        for (int b = 0; b < tiers - 1; b++) {
            float t0 = b / (float) (tiers - 1);
            float t1 = (b + 1) / (float) (tiers - 1);
            float outerA0 = (0.34F - 0.18F * t0) * fade;
            float outerA1 = (0.34F - 0.18F * t1) * fade;
            renderOrientedFrustumBand(matrix, vc, centers[b], centers[b + 1], right, forward,
                    outerRadii[b], outerRadii[b + 1], 24,
                    pr, pg, pb, outerA0, outerA1);

            float innerA0 = (0.48F - 0.26F * t0) * fade;
            float innerA1 = (0.48F - 0.26F * t1) * fade;
            renderOrientedFrustumBand(matrix, vc, centers[b], centers[b + 1], right, forward,
                    innerRadii[b], innerRadii[b + 1], 18,
                    1.0F, 1.0F, 1.0F, innerA0, innerA1);
        }

        // 3. Tilted counter-rotating storm hoops & flared upper Storm-Anvil Crown
        for (int b = 0; b < tiers; b++) {
            float t = b / (float) (tiers - 1);
            float funnelR = outerRadii[b];
            float spin = gameTime * (0.40F - 0.14F * t) + b * 0.65F;
            Vec3 ux = new Vec3(Mth.cos(spin), 0.09D * Mth.sin(spin), Mth.sin(spin)).normalize();
            Vec3 uz = new Vec3(-Mth.sin(spin), 0.09D * Mth.cos(spin), Mth.cos(spin)).normalize();

            boolean accent = (b % 2 == 1);
            renderOrientedRing(matrix, vc, centers[b], ux, uz,
                    funnelR * 0.87F, funnelR * 1.02F, 24,
                    accent ? ar : pr, accent ? ag : pg, accent ? ab : pb,
                    (0.78F - 0.24F * t) * fade);
        }

        Vec3 crownCenter = centers[tiers - 1];
        float crownR = outerRadii[tiers - 1];
        renderOrientedRing(matrix, vc, crownCenter, right, forward,
                crownR * 0.92F, crownR * 1.26F, 30, ar, ag, ab, 0.56F * fade);

        // 4. 6 wide helical slipstream ribbons spiraling up the swaying funnel wall
        int segs = 24;
        for (int strand = 0; strand < 6; strand++) {
            float phase0 = gameTime * 0.42F + (float) (Math.PI * 2.0D * strand / 6.0D);
            boolean ivory = (strand % 3 == 0);
            boolean accent = (strand % 3 == 1);
            float sr = ivory ? 1.0F : (accent ? ar : pr);
            float sg = ivory ? 1.0F : (accent ? ag : pg);
            float sb = ivory ? 1.0F : (accent ? ab : pb);

            for (int s = 0; s < segs; s++) {
                float t0 = s / (float) segs;
                float t1 = (s + 1) / (float) segs;
                double swayAmp0 = 0.30D * t0 * maxR * 0.36D;
                double swayAmp1 = 0.30D * t1 * maxR * 0.36D;
                Vec3 c0 = base.add(
                        Math.sin(gameTime * 0.16D + t0 * 3.2D + inst.seed) * swayAmp0,
                        t0 * totalH,
                        Math.cos(gameTime * 0.14D + t0 * 2.8D + inst.seed) * swayAmp0);
                Vec3 c1 = base.add(
                        Math.sin(gameTime * 0.16D + t1 * 3.2D + inst.seed) * swayAmp1,
                        t1 * totalH,
                        Math.cos(gameTime * 0.14D + t1 * 2.8D + inst.seed) * swayAmp1);

                float r0 = Mth.lerp(t0 * t0 * 0.68F + t0 * 0.32F, baseEyeR, maxR * 1.06F);
                float r1 = Mth.lerp(t1 * t1 * 0.68F + t1 * 0.32F, baseEyeR, maxR * 1.06F);
                float a0 = phase0 + t0 * 12.5F;
                float a1 = phase0 + t1 * 12.5F;

                Vec3 p0 = c0.add(Mth.cos(a0) * r0, 0.0D, Mth.sin(a0) * r0);
                Vec3 p1 = c1.add(Mth.cos(a1) * r1, 0.0D, Mth.sin(a1) * r1);
                renderCrossSegment(matrix, vc, p0, p1, 0.075F + 0.045F * t0, sr, sg, sb, 0.84F * fade);
            }
        }

        // 5. 8 orbiting razor wind-shear crescents whipping around the cyclone!
        for (int c = 0; c < 8; c++) {
            float hFrac = 0.14F + 0.105F * c;
            int idx = Math.clamp(Math.round(hFrac * (tiers - 1)), 0, tiers - 1);
            float bladeAng = gameTime * (0.48F - 0.02F * c) + c * 1.35F;
            float bladeR = outerRadii[idx] * 1.14F;
            renderSweptWindCrescent(matrix, vc, centers[idx], right, forward,
                    bladeR, bladeAng, 0.95F, Math.max(0.14F, bladeR * 0.10F),
                    (c % 2 == 0) ? 1.0F : ar, (c % 2 == 0) ? 1.0F : ag, (c % 2 == 0) ? 1.0F : ab,
                    0.86F * fade);
        }
    }

    /**
     * VFX 12: Explosive Flared Cyclone Geyser Plume (14-block Air Geyser Mine & 8-block Concussive Uppercut).
     */
    private static void renderGeyserPlume(
            Matrix4f matrix,
            VertexConsumer vc,
            SpellWorldVfxManager.ActiveVfxInstance inst,
            float progress,
            float gameTime,
            float pr, float pg, float pb,
            float ar, float ag, float ab
    ) {
        float inFrac = Math.min(0.08F, 6.0F / Math.max(1, inst.maxTicks));
        float outFrac = Math.max(0.65F, 1.0F - (16.0F / Math.max(1, inst.maxTicks)));
        float fade = computeEnvelope(progress, inFrac, outFrac);
        float eruptProg = Mth.clamp((progress * inst.maxTicks) / 9.0F, 0.0F, 1.0F);
        Vec3 base = inst.start.add(0.0D, 0.05D, 0.0D);
        double maxH = Math.max(3.0D, inst.endOrDir.y - inst.start.y);
        double currentH = maxH * (0.22F + 0.78F * (1.0F - (1.0F - eruptProg) * (1.0F - eruptProg)));
        Vec3 top = base.add(0.0D, currentH, 0.0D);
        float r = Math.max(0.75F, inst.radiusOrScale);

        Vec3 right = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 forward = new Vec3(0.0D, 0.0D, 1.0D);

        // 1. Ground Wind-Rose eruption seal + double expanding shockwave rings
        float groundExpand = r * (0.62F + 0.42F * eruptProg);
        renderOrientedRing(matrix, vc, base, right, forward, groundExpand * 0.80F, groundExpand, 28, pr, pg, pb, 0.88F * fade);
        renderOrientedRing(matrix, vc, base.add(0.0D, 0.02D, 0.0D), right, forward,
                groundExpand * 0.50F, groundExpand * 0.62F, 24, ar, ag, ab, 0.82F * fade);
        renderOrientedStarPolygon(matrix, vc, base.add(0.0D, 0.03D, 0.0D), right, forward,
                groundExpand * 0.88F, 8, gameTime * 0.22F + inst.seed, 1.0F, 1.0F, 1.0F, 0.78F * fade);

        // 2. Flared volumetric cyclone updraft funnel + blazing ivory-white inner jet
        int funnelBands = 6;
        for (int b = 0; b < funnelBands; b++) {
            float t0 = b / (float) funnelBands;
            float t1 = (b + 1) / (float) funnelBands;
            Vec3 c0 = base.add(0.0D, t0 * currentH, 0.0D);
            Vec3 c1 = base.add(0.0D, t1 * currentH, 0.0D);
            float fr0 = r * (0.34F + 0.52F * t0 * t0);
            float fr1 = r * (0.34F + 0.52F * t1 * t1);
            renderOrientedFrustumBand(matrix, vc, c0, c1, right, forward, fr0, fr1, 20,
                    pr, pg, pb, (0.46F - 0.24F * t0) * fade, (0.46F - 0.24F * t1) * fade);
        }
        renderTube(matrix, vc, base, top, r * 0.16F * (0.35F + 0.65F * fade), 10, 1.0F, 1.0F, 1.0F, 0.94F * fade, FULL_LIGHT);

        // 3. 4 fast-rising helical slipstream ribbons spiraling up the geyser
        int steps = 16;
        for (int strand = 0; strand < 4; strand++) {
            float phase = -gameTime * 0.55F + (float) (Math.PI * 0.5D * strand);
            boolean ivory = (strand % 2 == 0);
            for (int s = 0; s < steps; s++) {
                float t0 = s / (float) steps;
                float t1 = (s + 1) / (float) steps;
                float hr0 = r * (0.38F + 0.54F * t0 * t0);
                float hr1 = r * (0.38F + 0.54F * t1 * t1);
                float a0 = phase + t0 * 9.5F;
                float a1 = phase + t1 * 9.5F;
                Vec3 p0 = base.add(Mth.cos(a0) * hr0, t0 * currentH, Mth.sin(a0) * hr0);
                Vec3 p1 = base.add(Mth.cos(a1) * hr1, t1 * currentH, Mth.sin(a1) * hr1);
                renderCrossSegment(matrix, vc, p0, p1, 0.055F,
                        ivory ? 1.0F : ar, ivory ? 1.0F : ag, ivory ? 1.0F : ab,
                        0.86F * fade * (1.0F - t0 * 0.30F));
            }
        }

        // 4. 6 continuously ascending sonic shock-collar rings rocketing up the plume
        for (int i = 0; i < 6; i++) {
            float rt = ((gameTime * 0.11F + i / 6.0F) % 1.0F);
            Vec3 ringPos = base.add(0.0D, rt * currentH, 0.0D);
            float flare = r * (0.44F + 0.56F * rt);
            float rAlpha = fade * Mth.sin(rt * (float) Math.PI);
            renderOrientedRing(matrix, vc, ringPos, right, forward, flare * 0.80F, flare, 22, ar, ag, ab, 0.88F * rAlpha);
        }

        if (isHydroMateria(inst.essence)) {
            // Section 6.3 Whirlpool Geyser: deep-azure drowning water vortex wall, foaming geyser crown & spinning bubbles!
            int vBands = 7;
            for (int b = 0; b < vBands; b++) {
                float t0 = b / (float) vBands;
                float t1 = (b + 1) / (float) vBands;
                // Classic whirlpool hourglass-funnel profile: wide suction base, tight spinning waist, flared geyser top!
                float wr0 = r * (0.58F - 0.22F * Mth.sin(t0 * (float) Math.PI) + 0.42F * t0 * t0);
                float wr1 = r * (0.58F - 0.22F * Mth.sin(t1 * (float) Math.PI) + 0.42F * t1 * t1);
                renderOrientedFrustumBand(matrix, vc,
                        base.add(0.0D, t0 * currentH, 0.0D), base.add(0.0D, t1 * currentH, 0.0D),
                        right, forward, wr0, wr1, 24,
                        0.133F, 0.361F, 0.486F, 0.52F * fade, 0.42F * fade);
            }
            // Foaming whitewater geyser crest at top of vortex
            renderOrientedFrustumBand(matrix, vc,
                    top.add(0.0D, -0.35D, 0.0D), top.add(0.0D, 0.22D, 0.0D),
                    right, forward, r * 0.78F, r * 1.12F, 24,
                    0.90F, 0.99F, 1.00F, 0.82F * fade, 0.16F * fade);
        } else if (isInfernalMateria(inst.essence) || isMagmaCalderaMateria(inst.essence)) {
            // Section 6.7 Thermal Pop & Section 6.14 Volcanic Vent Geyser / Volcanic Eruption Blast:
            // Roaring vertical column of 8 rising 3D lava/hellfire flame tongues & erupting pumice boulders!
            Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
            for (int f = 0; f < 8; f++) {
                float fAng = -gameTime * 0.14F + (float) (Math.PI * 2.0D * f / 8.0D);
                Vec3 rad = new Vec3(Mth.cos(fAng), 0.0D, Mth.sin(fAng));
                Vec3 tang = new Vec3(-Mth.sin(fAng), 0.0D, Mth.cos(fAng));
                float flicker = 0.84F + 0.22F * Mth.sin(gameTime * 0.38F + f);
                renderHellfireFlameTongue(matrix, vc, base.add(rad.scale(r * 0.55D)),
                        worldUp.add(rad.scale(0.12D)).normalize(), rad, tang,
                        r * 0.24F, (float) (currentH * (f % 2 == 0 ? 0.72D : 0.48D)) * flicker, fade);
                if (isMagmaCalderaMateria(inst.essence) && f % 2 == 0) {
                    float rise = ((gameTime * 0.12F + f * 0.25F) % 1.0F);
                    Vec3 pumice = base.add(rad.scale(r * 0.42D)).add(0.0D, currentH * rise, 0.0D);
                    renderOctahedron(matrix, vc, pumice, Math.max(0.12F, r * 0.11F) * fade,
                            0.165F, 0.090F, 0.071F, 0.90F * fade);
                }
            }
        } else if (isMiracleOasisMateria(inst.essence)) {
            // Section 6.21 Aquifer Geyser Mine: pressurized #38C6F4 groundwater spout + rising #1A5923 / #C2B28F desert blossoms!
            int vBands = 7;
            for (int b = 0; b < vBands; b++) {
                float t0 = b / (float) vBands;
                float t1 = (b + 1) / (float) vBands;
                float wr0 = r * (0.54F - 0.18F * Mth.sin(t0 * (float) Math.PI) + 0.44F * t0 * t0);
                float wr1 = r * (0.54F - 0.18F * Mth.sin(t1 * (float) Math.PI) + 0.44F * t1 * t1);
                renderOrientedFrustumBand(matrix, vc,
                        base.add(0.0D, t0 * currentH, 0.0D), base.add(0.0D, t1 * currentH, 0.0D),
                        right, forward, wr0, wr1, 24,
                        0.220F, 0.776F, 0.957F, 0.56F * fade, 0.40F * fade);
            }
            renderOrientedFrustumBand(matrix, vc,
                    top.add(0.0D, -0.35D, 0.0D), top.add(0.0D, 0.24D, 0.0D),
                    right, forward, r * 0.76F, r * 1.14F, 24,
                    0.871F, 0.788F, 0.604F, 0.84F * fade, 0.18F * fade);
            for (int f = 0; f < 8; f++) {
                float rise = ((gameTime * 0.13F + f * 0.125F) % 1.0F);
                float fAng = gameTime * 0.20F + (float) (Math.PI * 2.0D * f / 8.0D);
                Vec3 bPos = base.add(Mth.cos(fAng) * r * (0.35D + 0.35D * rise), currentH * rise, Mth.sin(fAng) * r * (0.35D + 0.35D * rise));
                boolean goldPetal = (f % 2 == 0);
                renderOctahedron(matrix, vc, bPos, Math.max(0.11F, r * 0.10F) * fade,
                        goldPetal ? 0.871F : 0.133F,
                        goldPetal ? 0.788F : 0.471F,
                        goldPetal ? 0.604F : 0.188F,
                        0.94F * fade);
            }
        }
    }

    /**
     * VFX 13: 3D Dual-Hemisphere Gust Shield & Logarithmic Vacuum Implosion Sphere.
     * When {@code inst.endOrDir.x < 0}, collapses rapidly inward with 12 spiraling suction ribbons and a sonic core flash;
     * otherwise expands outward as a full spherical Gust Shield wrapped in orbiting razor gale-crescents.
     */
    private static void renderVacuumSphere(
            Matrix4f matrix,
            VertexConsumer vc,
            SpellWorldVfxManager.ActiveVfxInstance inst,
            float progress,
            float gameTime,
            float pr, float pg, float pb,
            float ar, float ag, float ab
    ) {
        boolean inward = inst.endOrDir.x < 0.0D;
        float fade = inward
                ? computeEnvelope(progress, 0.05F, 0.78F)
                : computeEnvelope(progress, 0.06F, 0.62F);
        float maxR = Math.max(1.2F, inst.radiusOrScale);
        float outSurge = Mth.clamp(progress / 0.28F, 0.0F, 1.0F);
        float curR = inward
                ? maxR * Math.max(0.08F, 1.0F - (progress * progress * 0.92F))
                : maxR * (0.28F + 0.72F * (1.0F - (1.0F - outSurge) * (1.0F - outSurge)));

        Vec3 center = inst.start.add(0.0D, inward ? 0.9D : 0.35D, 0.0D);
        // Full 360-degree spherical wind shell (upper + lower hemispheres)
        renderHemisphereShell(matrix, vc, center, curR, 6, 22, pr, pg, pb, 0.36F * fade);
        renderLowerHemisphereShell(matrix, vc, center, curR, 5, 22, pr, pg, pb, 0.24F * fade);

        // 3 orthogonal swirling slipstream rings around the sphere
        float spin = gameTime * (inward ? -0.52F : 0.36F) + inst.seed;
        Vec3 ux1 = new Vec3(Mth.cos(spin), 0.18D, Mth.sin(spin)).normalize();
        Vec3 uz1 = new Vec3(-Mth.sin(spin), 0.18D, Mth.cos(spin)).normalize();
        renderOrientedRing(matrix, vc, center.add(0.0D, curR * 0.22D, 0.0D), ux1, uz1,
                curR * 0.90F, curR * 1.02F, 26, ar, ag, ab, 0.86F * fade);

        Vec3 ux2 = new Vec3(Mth.cos(-spin), 0.75D, Mth.sin(-spin)).normalize();
        Vec3 uz2 = ux2.cross(new Vec3(0.0D, 1.0D, 0.0D)).normalize();
        renderOrientedRing(matrix, vc, center, ux2, uz2,
                curR * 0.88F, curR * 0.98F, 24, pr, pg, pb, 0.74F * fade);

        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);

        if (inward) {
            // 12 logarithmic 3D inward-spiraling vacuum ribbons twisting into a blazing singularity flash!
            for (int i = 0; i < 12; i++) {
                float baseTheta = (float) (Math.PI * 2.0D * i / 12.0D) + spin * 0.45F;
                float elev = ((i % 3) - 1) * 0.38F;
                boolean ivory = (i % 2 == 0);
                for (int s = 0; s < 8; s++) {
                    float t0 = s / 8.0F;
                    float t1 = (s + 1) / 8.0F;
                    float sr0 = curR * Mth.lerp(t0, 1.28F, 0.14F);
                    float sr1 = curR * Mth.lerp(t1, 1.28F, 0.14F);
                    float th0 = baseTheta + t0 * 2.4F;
                    float th1 = baseTheta + t1 * 2.4F;
                    float ph0 = elev * (1.0F - t0 * 0.65F);
                    float ph1 = elev * (1.0F - t1 * 0.65F);
                    Vec3 p0 = center.add(Mth.cos(th0) * Mth.cos(ph0) * sr0, Mth.sin(ph0) * sr0, Mth.sin(th0) * Mth.cos(ph0) * sr0);
                    Vec3 p1 = center.add(Mth.cos(th1) * Mth.cos(ph1) * sr1, Mth.sin(ph1) * sr1, Mth.sin(th1) * Mth.cos(ph1) * sr1);
                    renderCrossSegment(matrix, vc, p0, p1, 0.048F,
                            ivory ? 1.0F : ar, ivory ? 1.0F : ag, ivory ? 1.0F : ab, 0.88F * fade);
                }
            }
            // 4 inward-sweeping razor wind crescents rushing into the collapsing vacuum core
            for (int c = 0; c < 4; c++) {
                float cAng = -spin * 1.55F + (float) (Math.PI * 0.5D * c);
                renderSweptWindCrescent(matrix, vc, center, flatX, flatZ,
                        Math.max(0.22F, curR * 0.95F), cAng, 1.15F, Math.max(0.12F, curR * 0.11F),
                        (c & 1) == 0 ? 1.0F : ar, (c & 1) == 0 ? 1.0F : ag, (c & 1) == 0 ? 1.0F : ab,
                        0.82F * fade);
            }

            float corePulse = 0.20F + 0.38F * progress;
            boolean voidBlackHole = inst.essence == EssenceType.VOID;
            renderOctahedron(matrix, vc, center, maxR * corePulse * fade,
                    voidBlackHole ? 0.045F : 1.0F,
                    voidBlackHole ? 0.025F : 1.0F,
                    voidBlackHole ? 0.070F : 1.0F,
                    0.96F * fade);
            renderOctahedron(matrix, vc, center, maxR * corePulse * 1.45F * fade, ar, ag, ab, 0.66F * fade);

            // Final Vacuum Pop pressure-clap ring when the collapsing air pocket snaps shut!
            if (progress >= 0.68F) {
                float popT = (progress - 0.68F) / 0.32F;
                float popFade = 1.0F - popT;
                float popR = maxR * (0.14F + 0.48F * popT);
                renderOrientedRing(matrix, vc, center, flatX, flatZ, popR * 0.82F, popR, 28,
                        1.0F, 1.0F, 1.0F, 0.92F * popFade);
                renderOrientedRing(matrix, vc, center.add(0.0D, -0.55D, 0.0D), flatX, flatZ, popR * 0.90F, popR * 1.08F, 26,
                        ar, ag, ab, 0.78F * popFade);
            }
        } else {
            // Outward Gust Shield: 6 curved sweeping gale-blade crescents orbiting the protective wind bubble!
            for (int c = 0; c < 6; c++) {
                float cAng = spin * 1.35F + (float) (Math.PI * 2.0D * c / 6.0D);
                double yBand = ((c % 3) - 1) * (curR * 0.28D);
                float bandR = (float) Math.sqrt(Math.max(0.08D, curR * curR - yBand * yBand)) * 1.05F;
                renderSweptWindCrescent(matrix, vc, center.add(0.0D, yBand, 0.0D),
                        flatX, flatZ, bandR, cAng, 1.05F, Math.max(0.14F, curR * 0.10F),
                        (c % 2 == 0) ? 1.0F : ar, (c % 2 == 0) ? 1.0F : ag, (c % 2 == 0) ? 1.0F : ab,
                        0.88F * fade);
            }
        }
    }

    private static boolean isAerodynamicMateria(EssenceType type) {
        return type == EssenceType.AIR || type == EssenceType.AURA;
    }

    private static boolean isHydroMateria(EssenceType type) {
        return type == EssenceType.WATER || type == EssenceType.FLOW || type == EssenceType.UMBRAL_WATER;
    }

    private static boolean isBotanicalMateria(EssenceType type) {
        return type == EssenceType.NATURE;
    }

    private static boolean isTectonicMateria(EssenceType type) {
        return type == EssenceType.EARTH;
    }

    private static boolean isGlacialMateria(EssenceType type) {
        return type == EssenceType.FROZEN;
    }

    private static boolean isInfernalMateria(EssenceType type) {
        return type == EssenceType.NETHER;
    }

    private static boolean isSolarRadiantMateria(EssenceType type) {
        return type == EssenceType.RADIANT;
    }

    private static boolean isUmbralShadowMateria(EssenceType type) {
        return type == EssenceType.UMBRAL;
    }

    private static boolean isNecroticUndeadMateria(EssenceType type) {
        return type == EssenceType.UNDEAD;
    }

    private static boolean isAridDesertMateria(EssenceType type) {
        return type == EssenceType.ARID;
    }

    private static boolean isGalvanicLightningMateria(EssenceType type) {
        return type == EssenceType.LIGHTNING;
    }

    private static boolean isMagmaCalderaMateria(EssenceType type) {
        return type == EssenceType.MAGMA;
    }

    private static boolean isTempestStormMateria(EssenceType type) {
        return type == EssenceType.STORM;
    }

    private static boolean isPermafrostGlacialMateria(EssenceType type) {
        return type == EssenceType.GLACIAL;
    }

    private static boolean isOvergrowthRampartMateria(EssenceType type) {
        return type == EssenceType.OVERGROWTH;
    }

    private static boolean isAbrasiveDustMateria(EssenceType type) {
        return type == EssenceType.DUST;
    }

    private static boolean isSymbioticSporeMateria(EssenceType type) {
        return type == EssenceType.SPORE;
    }

    private static boolean isBorealTaigaMateria(EssenceType type) {
        return type == EssenceType.TAIGA;
    }

    private static boolean isMiracleOasisMateria(EssenceType type) {
        return type == EssenceType.OASIS;
    }

    private static boolean isCellularVitaeMateria(EssenceType type) {
        return type == EssenceType.VITAE;
    }

    private static boolean isSanguineBloodMateria(EssenceType type) {
        return type == EssenceType.BLOOD;
    }

    private static boolean isPranicAuraMateria(EssenceType type) {
        return type == EssenceType.AURA;
    }

    private static boolean isFossilAmberMateria(EssenceType type) {
        return type == EssenceType.AMBER;
    }

    private static boolean isParadoxEclipseMateria(EssenceType type) {
        return type == EssenceType.ECLIPSE;
    }

    private static boolean isCadaverousBlightMateria(EssenceType type) {
        return type == EssenceType.BLIGHT;
    }

    private static boolean isCosmicAstralMateria(EssenceType type) {
        return type == EssenceType.ASTRAL;
    }

    private static boolean isPhantasmalSoulfireMateria(EssenceType type) {
        return type == EssenceType.SOULFIRE;
    }

    private static boolean isScaldingVaporMateria(EssenceType type) {
        return type == EssenceType.VAPOR;
    }

    private static boolean isAntimatterNullRMateria(EssenceType type) {
        return type == EssenceType.NULL_R;
    }

    private static boolean isAbyssalNullUMateria(EssenceType type) {
        return type == EssenceType.NULL_U;
    }

    private static boolean isDaybreakDawnMateria(EssenceType type) {
        return type == EssenceType.DAWN;
    }

    private static boolean isHadopelagicAbyssMateria(EssenceType type) {
        return type == EssenceType.ABYSS;
    }

    private static boolean isGildedAegisMateria(EssenceType type) {
        return type == EssenceType.AEGIS;
    }

    private static boolean isBorealisAuroraMateria(EssenceType type) {
        return type == EssenceType.AURORA;
    }

    private static boolean isPhantasmMirageMateria(EssenceType type) {
        return type == EssenceType.MIRAGE;
    }

    private static boolean isIncorporealWraithMateria(EssenceType type) {
        return type == EssenceType.WRAITH;
    }

    private static boolean isSepulchralBarrowMateria(EssenceType type) {
        return type == EssenceType.BARROW;
    }

    private static boolean isCrematingPyreMateria(EssenceType type) {
        return type == EssenceType.PYRE;
    }

    private static boolean isTwilightPenumbraMateria(EssenceType type) {
        return type == EssenceType.PENUMBRA;
    }

    private static boolean isHoarfrostRimeMateria(EssenceType type) {
        return type == EssenceType.RIME;
    }

    private static boolean isVernalSpringMateria(EssenceType type) {
        return type == EssenceType.SPRING;
    }

    private static boolean isGalvanicStaticMateria(EssenceType type) {
        return type == EssenceType.STATIC;
    }

    private static boolean isPrimevalSylvanMateria(EssenceType type) {
        return type == EssenceType.SYLVAN;
    }

    private static boolean isPestilentMiasmaMateria(EssenceType type) {
        return type == EssenceType.MIASMA;
    }

    private static boolean isPrimordialGenesisMateria(EssenceType type) {
        return type == EssenceType.GENESIS;
    }

    private static boolean isStygianOblivionMateria(EssenceType type) {
        return type == EssenceType.OBLIVION;
    }

    private static boolean isQuintessentialAetherMateria(EssenceType type) {
        return type == EssenceType.AETHER;
    }

    private static boolean isAbyssalEntropicMateria(EssenceType type) {
        return type == EssenceType.ENTROPIC;
    }

    private static boolean isHolyCelestialMateria(EssenceType type) {
        return type == EssenceType.CELESTIAL;
    }

    /**
     * Scales Skyfall rift/canopy height dynamically by Potency Tier ({@code +2.2m} per tier up to ~30m outdoors at Tier 10!)
     * while scanning upward for cave/dungeon ceilings so indoor Skyfalls always fit cleanly underneath!
     */
    private static double resolveTierScaledSkyfallHeight(Vec3 ground, int tier, double tier1BaseHeight) {
        double desired = tier1BaseHeight + (Math.clamp(tier, 1, 10) - 1) * 2.2D;
        var clientLvl = Minecraft.getInstance().level;
        if (clientLvl != null) {
            net.minecraft.core.BlockPos baseBlock = net.minecraft.core.BlockPos.containing(ground.x, ground.y + 0.2D, ground.z);
            int maxScan = (int) Math.ceil(desired) + 1;
            for (int dy = 4; dy <= maxScan; dy++) {
                net.minecraft.core.BlockPos check = baseBlock.above(dy);
                var bs = clientLvl.getBlockState(check);
                if (!bs.isAir() && !bs.getCollisionShape(clientLvl, check).isEmpty()) {
                    return Math.max(3.6D, dy - 0.50D);
                }
            }
        }
        return desired;
    }

    /**
     * Renders the translucent-emissive Materia aura shell, falling comet wake, and landing shock-ring
     * around a 3D Skyfall Pseudoblock column ({@link SpellVfxEventPayload#VFX_SKYFALL_PSEUDOBLOCK}).
     */
    private static void renderSkyfallPseudoblockAura(
            Matrix4f matrix,
            VertexConsumer vc,
            SpellWorldVfxManager.ActiveVfxInstance inst,
            float partialTick,
            float gameTime,
            float pr, float pg, float pb,
            float ar, float ag, float ab
    ) {
        int fallTicks = Math.max(1, inst.accentRgb);
        float elapsed = inst.ageTicks + partialTick;
        float fallT = Mth.clamp(elapsed / (float) fallTicks, 0.0F, 1.0F);
        float accel = fallT * fallT;

        double curX = Mth.lerp(accel, inst.start.x - 0.5D, inst.endOrDir.x);
        double curY = Mth.lerp(accel, inst.start.y, inst.endOrDir.y);
        double curZ = Mth.lerp(accel, inst.start.z - 0.5D, inst.endOrDir.z);
        int colH = Math.clamp(Math.round(inst.radiusOrScale), 1, 20);

        float pulse = 0.72F + 0.28F * Mth.sin(gameTime * 0.24F + inst.seed);
        float remTicks = Math.max(0.0F, inst.maxTicks - elapsed);
        float solidifyFlare = remTicks <= 8.0F ? (1.0F + (1.0F - remTicks / 8.0F) * 0.45F) : 1.0F;

        // 4 glowing vertical corner seams & top/bottom framing edges signifying an active magical Pseudoblock
        double pad = 0.025D;
        double[][] corners = {{-pad, -pad}, {1.0D + pad, -pad}, {1.0D + pad, 1.0D + pad}, {-pad, 1.0D + pad}};
        for (int i = 0; i < 4; i++) {
            Vec3 b0 = new Vec3(curX + corners[i][0], curY - pad, curZ + corners[i][1]);
            Vec3 t0 = new Vec3(curX + corners[i][0], curY + colH + pad, curZ + corners[i][1]);
            Vec3 b1 = new Vec3(curX + corners[(i + 1) % 4][0], curY - pad, curZ + corners[(i + 1) % 4][1]);
            Vec3 t1 = new Vec3(curX + corners[(i + 1) % 4][0], curY + colH + pad, curZ + corners[(i + 1) % 4][1]);
            renderCrossSegment(matrix, vc, b0, t0, 0.026F * solidifyFlare, ar, ag, ab, 0.68F * pulse);
            renderCrossSegment(matrix, vc, b0, b1, 0.022F, pr, pg, pb, 0.58F * pulse);
            renderCrossSegment(matrix, vc, t0, t1, 0.022F, ar, ag, ab, 0.62F * pulse);
        }

        // Falling comet wake while descending, or brief impact ring right when the pseudoblock column slams into the ground!
        Vec3 right = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 forward = new Vec3(0.0D, 0.0D, 1.0D);
        if (fallT < 1.0F) {
            Vec3 colTopCenter = new Vec3(curX + 0.5D, curY + colH, curZ + 0.5D);
            Vec3 wakeTip = new Vec3(curX + 0.5D, Math.min(inst.start.y + colH, curY + colH + 2.4D), curZ + 0.5D);
            renderCrossSegment(matrix, vc, colTopCenter, wakeTip, 0.18F, pr, pg, pb, 0.62F);
            renderCrossSegment(matrix, vc, colTopCenter, wakeTip, 0.08F, ar, ag, ab, 0.88F);
            renderOrientedRing(matrix, vc, new Vec3(inst.endOrDir.x + 0.5D, inst.endOrDir.y + 0.04D, inst.endOrDir.z + 0.5D),
                    right, forward, 0.42F, 0.62F, 12, ar, ag, ab, 0.54F * fallT);
        } else if (elapsed - fallTicks < 14.0F) {
            float impT = (elapsed - fallTicks) / 14.0F;
            float impR = 0.55F + 1.10F * impT;
            renderOrientedRing(matrix, vc, new Vec3(inst.endOrDir.x + 0.5D, inst.endOrDir.y + 0.05D, inst.endOrDir.z + 0.5D),
                    right, forward, impR * 0.76F, impR, 16, ar, ag, ab, (1.0F - impT) * 0.88F);
        }
    }

    private static void renderSkeletalRibSpike(
            Matrix4f matrix,
            VertexConsumer vc,
            Vec3 baseCenter,
            Vec3 axisDir,
            Vec3 bendDir,
            Vec3 perpTang,
            float baseHalfWidth,
            float length,
            float alpha
    ) {
        if (baseHalfWidth <= 0.008F || length <= 0.02F || alpha <= 0.004F) return;
        Vec3 midCenter = baseCenter
                .add(axisDir.scale(length * 0.52D))
                .add(bendDir.scale(length * 0.14D));
        Vec3 tip = baseCenter
                .add(axisDir.scale(length * 0.92D))
                .add(bendDir.scale(length * 0.36D));
        Vec3 perpB = axisDir.cross(perpTang).normalize();

        Vec3[] bRing = new Vec3[]{
                baseCenter.add(perpTang.scale(baseHalfWidth)),
                baseCenter.add(perpB.scale(baseHalfWidth)),
                baseCenter.subtract(perpTang.scale(baseHalfWidth)),
                baseCenter.subtract(perpB.scale(baseHalfWidth))
        };
        float midW = baseHalfWidth * 0.68F;
        Vec3[] mRing = new Vec3[]{
                midCenter.add(perpTang.scale(midW)),
                midCenter.add(perpB.scale(midW)),
                midCenter.subtract(perpTang.scale(midW)),
                midCenter.subtract(perpB.scale(midW))
        };

        for (int i = 0; i < 4; i++) {
            int next = (i + 1) % 4;
            boolean boneFace = (i % 2 == 0);
            float mr = boneFace ? 0.902F : 0.78F;
            float mg = boneFace ? 0.875F : 0.75F;
            float mb = boneFace ? 0.812F : 0.68F;
            emitDoubleSidedQuad(matrix, vc, bRing[i], bRing[next], mRing[next], mRing[i],
                    0.302F, 0.392F, 0.325F, 0.95F * alpha,
                    0.302F, 0.392F, 0.325F, 0.95F * alpha,
                    mr, mg, mb, 0.96F * alpha,
                    mr, mg, mb, 0.96F * alpha);
            emitDoubleSidedQuad(matrix, vc, mRing[i], mRing[next], tip, tip,
                    mr, mg, mb, 0.96F * alpha,
                    mr, mg, mb, 0.96F * alpha,
                    0.369F, 0.878F, 0.533F, 0.94F * alpha,
                    0.369F, 0.878F, 0.533F, 0.94F * alpha);
        }
    }

    private static void renderSpectralSkullHead(
            Matrix4f matrix,
            VertexConsumer vc,
            Vec3 center,
            Vec3 fwdDir,
            Vec3 rightDir,
            Vec3 upDir,
            float radius,
            float alpha
    ) {
        if (radius <= 0.015F || alpha <= 0.004F) return;
        Vec3 craniumTop = center.add(upDir.scale(radius * 0.95D));
        Vec3 craniumBase = center.subtract(upDir.scale(radius * 0.38D));
        Vec3 brow = center.add(fwdDir.scale(radius * 0.96D)).add(upDir.scale(radius * 0.16D));
        Vec3 occiput = center.subtract(fwdDir.scale(radius * 0.88D)).add(upDir.scale(radius * 0.12D));
        Vec3 cheekR = center.add(rightDir.scale(radius * 0.86D));
        Vec3 cheekL = center.subtract(rightDir.scale(radius * 0.86D));

        // Bleached Ossuary Bone Cranium (#E6DFCF) + Desiccated Tombstone Occiput (#4D6453)
        emitDoubleSidedFlatQuad(matrix, vc, craniumTop, cheekR, brow, craniumTop, 0.902F, 0.875F, 0.812F, 0.95F * alpha);
        emitDoubleSidedFlatQuad(matrix, vc, craniumTop, brow, cheekL, craniumTop, 0.902F, 0.875F, 0.812F, 0.95F * alpha);
        emitDoubleSidedFlatQuad(matrix, vc, craniumTop, occiput, cheekR, craniumTop, 0.302F, 0.392F, 0.325F, 0.92F * alpha);
        emitDoubleSidedFlatQuad(matrix, vc, craniumTop, cheekL, occiput, craniumTop, 0.302F, 0.392F, 0.325F, 0.92F * alpha);
        emitDoubleSidedFlatQuad(matrix, vc, craniumBase, brow, cheekR, craniumBase, 0.84F, 0.81F, 0.74F, 0.92F * alpha);
        emitDoubleSidedFlatQuad(matrix, vc, craniumBase, cheekL, brow, craniumBase, 0.84F, 0.81F, 0.74F, 0.92F * alpha);

        // Screaming Lower Jawbone & Twin Blazing #5EE088 Necrotic Soul Eye Sockets
        Vec3 chin = center.add(fwdDir.scale(radius * 0.82D)).subtract(upDir.scale(radius * 0.82D));
        renderCrossSegment(matrix, vc, cheekL.subtract(upDir.scale(radius * 0.22D)), chin, radius * 0.16F,
                0.902F, 0.875F, 0.812F, 0.94F * alpha);
        renderCrossSegment(matrix, vc, cheekR.subtract(upDir.scale(radius * 0.22D)), chin, radius * 0.16F,
                0.902F, 0.875F, 0.812F, 0.94F * alpha);
        for (int side = -1; side <= 1; side += 2) {
            Vec3 eyePos = center.add(fwdDir.scale(radius * 0.88D))
                    .add(rightDir.scale(side * radius * 0.36D))
                    .add(upDir.scale(radius * 0.14D));
            renderOrientedRing(matrix, vc, eyePos, rightDir, upDir, 0.0F, radius * 0.22F, 6,
                    0.369F, 0.878F, 0.533F, 0.98F * alpha);
        }
    }

    private static void renderUmbralShadowNeedle(
            Matrix4f matrix,
            VertexConsumer vc,
            Vec3 baseCenter,
            Vec3 axisDir,
            Vec3 perpU,
            Vec3 perpV,
            float baseHalfWidth,
            float length,
            float alpha
    ) {
        if (baseHalfWidth <= 0.008F || length <= 0.02F || alpha <= 0.004F) return;
        Vec3 waist = baseCenter.add(axisDir.scale(length * 0.20D));
        Vec3 tip = baseCenter.add(axisDir.scale(length));
        Vec3[] ring = new Vec3[]{
                waist.add(perpU.scale(baseHalfWidth)),
                waist.add(perpV.scale(baseHalfWidth)),
                waist.subtract(perpU.scale(baseHalfWidth)),
                waist.subtract(perpV.scale(baseHalfWidth))
        };
        for (int i = 0; i < 4; i++) {
            Vec3 w0 = ring[i];
            Vec3 w1 = ring[(i + 1) % 4];
            boolean violetEdge = (i % 2 == 0);
            float r = violetEdge ? 0.34F : 0.115F;
            float g = violetEdge ? 0.20F : 0.085F;
            float b = violetEdge ? 0.56F : 0.130F;
            emitDoubleSidedFlatQuad(matrix, vc, baseCenter, w0, w1, baseCenter, 0.085F, 0.058F, 0.095F, 0.94F * alpha);
            emitDoubleSidedFlatQuad(matrix, vc, w0, w1, tip, tip, r, g, b, 0.92F * alpha);
        }
    }

    private static void renderSolarStarburstRay(
            Matrix4f matrix,
            VertexConsumer vc,
            Vec3 baseCenter,
            Vec3 axisDir,
            Vec3 perpU,
            Vec3 perpV,
            float baseHalfWidth,
            float length,
            float alpha
    ) {
        if (baseHalfWidth <= 0.008F || length <= 0.02F || alpha <= 0.004F) return;
        Vec3 waist = baseCenter.add(axisDir.scale(length * 0.16D));
        Vec3 tip = baseCenter.add(axisDir.scale(length));
        Vec3[] ring = new Vec3[]{
                waist.add(perpU.scale(baseHalfWidth)),
                waist.add(perpV.scale(baseHalfWidth * 0.85D)),
                waist.subtract(perpU.scale(baseHalfWidth)),
                waist.subtract(perpV.scale(baseHalfWidth * 0.85D))
        };
        for (int i = 0; i < 4; i++) {
            Vec3 w0 = ring[i];
            Vec3 w1 = ring[(i + 1) % 4];
            emitDoubleSidedQuad(matrix, vc, baseCenter, w0, w1, baseCenter,
                    1.00F, 0.995F, 0.96F, 0.98F * alpha,
                    1.00F, 0.93F, 0.62F, 0.92F * alpha,
                    1.00F, 0.93F, 0.62F, 0.92F * alpha,
                    1.00F, 0.995F, 0.96F, 0.98F * alpha);
            emitDoubleSidedQuad(matrix, vc, w0, w1, tip, tip,
                    1.00F, 0.93F, 0.62F, 0.92F * alpha,
                    1.00F, 0.93F, 0.62F, 0.92F * alpha,
                    1.00F, 0.886F, 0.471F, 0.72F * alpha,
                    1.00F, 0.886F, 0.471F, 0.72F * alpha);
        }
    }

    private static void renderHellfireFlameTongue(
            Matrix4f matrix,
            VertexConsumer vc,
            Vec3 baseCenter,
            Vec3 axisDir,
            Vec3 perpU,
            Vec3 perpV,
            float baseHalfWidth,
            float height,
            float alpha
    ) {
        if (baseHalfWidth <= 0.01F || height <= 0.02F || alpha <= 0.004F) return;
        Vec3 tip = baseCenter.add(axisDir.scale(height));
        Vec3 mid = baseCenter.add(axisDir.scale(height * 0.42D));
        // Crossed dual-plane 3D flame tongue: incandescent molten-gold (#FFD15C) base tapering into roaring volcanic red-orange (#B83C08) tip!
        Vec3 u0 = baseCenter.subtract(perpU.scale(baseHalfWidth));
        Vec3 u1 = baseCenter.add(perpU.scale(baseHalfWidth));
        Vec3 mU0 = mid.subtract(perpU.scale(baseHalfWidth * 0.68D));
        Vec3 mU1 = mid.add(perpU.scale(baseHalfWidth * 0.68D));
        emitDoubleSidedQuad(matrix, vc, u0, u1, mU1, mU0,
                1.00F, 0.90F, 0.50F, 0.96F * alpha,
                1.00F, 0.90F, 0.50F, 0.96F * alpha,
                0.88F, 0.36F, 0.06F, 0.90F * alpha,
                0.88F, 0.36F, 0.06F, 0.90F * alpha);
        emitDoubleSidedQuad(matrix, vc, mU0, mU1, tip, tip,
                0.88F, 0.36F, 0.06F, 0.90F * alpha,
                0.88F, 0.36F, 0.06F, 0.90F * alpha,
                0.722F, 0.235F, 0.031F, 0.78F * alpha,
                0.722F, 0.235F, 0.031F, 0.78F * alpha);

        Vec3 v0 = baseCenter.subtract(perpV.scale(baseHalfWidth * 0.85D));
        Vec3 v1 = baseCenter.add(perpV.scale(baseHalfWidth * 0.85D));
        Vec3 mV0 = mid.subtract(perpV.scale(baseHalfWidth * 0.55D));
        Vec3 mV1 = mid.add(perpV.scale(baseHalfWidth * 0.55D));
        emitDoubleSidedQuad(matrix, vc, v0, v1, mV1, mV0,
                1.00F, 0.820F, 0.361F, 0.92F * alpha,
                1.00F, 0.820F, 0.361F, 0.92F * alpha,
                0.722F, 0.235F, 0.031F, 0.86F * alpha,
                0.722F, 0.235F, 0.031F, 0.86F * alpha);
        emitDoubleSidedQuad(matrix, vc, mV0, mV1, tip, tip,
                0.722F, 0.235F, 0.031F, 0.86F * alpha,
                0.722F, 0.235F, 0.031F, 0.86F * alpha,
                0.58F, 0.14F, 0.02F, 0.72F * alpha,
                0.58F, 0.14F, 0.02F, 0.72F * alpha);
    }

    private static void renderSnowflakeCrystal(
            Matrix4f matrix,
            VertexConsumer vc,
            Vec3 center,
            Vec3 axisU,
            Vec3 axisV,
            float radius,
            float rotRad,
            float alpha
    ) {
        if (radius <= 0.02F || alpha <= 0.004F) return;
        float spokeW = Math.max(0.014F, radius * 0.042F);
        renderOrientedRing(matrix, vc, center, axisU, axisV, radius * 0.28F, radius * 0.38F, 6,
                0.596F, 0.863F, 0.949F, 0.88F * alpha);
        for (int arm = 0; arm < 6; arm++) {
            float ang = rotRad + (float) (Math.PI * 2.0D * arm / 6.0D);
            Vec3 dir = axisU.scale(Mth.cos(ang)).add(axisV.scale(Mth.sin(ang)));
            Vec3 tip = center.add(dir.scale(radius));
            renderCrossSegment(matrix, vc, center, tip, spokeW, 0.92F, 0.98F, 1.00F, 0.94F * alpha);

            // Dendritic 60-degree crystalline snowflake chevrons along each arm
            Vec3 branchBase = center.add(dir.scale(radius * 0.58D));
            for (int side = -1; side <= 1; side += 2) {
                float bAng = ang + side * (float) (Math.PI / 3.0D);
                Vec3 bDir = axisU.scale(Mth.cos(bAng)).add(axisV.scale(Mth.sin(bAng)));
                renderCrossSegment(matrix, vc, branchBase, branchBase.add(bDir.scale(radius * 0.32D)),
                        spokeW * 0.76F, 0.596F, 0.863F, 0.949F, 0.88F * alpha);
            }
        }
    }

    private static void renderFacetedIceSpire(
            Matrix4f matrix,
            VertexConsumer vc,
            Vec3 baseCenter,
            Vec3 axisDir,
            Vec3 perpU,
            Vec3 perpV,
            float baseRadius,
            float height,
            int sides,
            float rotRad,
            float alpha
    ) {
        if (baseRadius <= 0.01F || height <= 0.02F || alpha <= 0.004F) return;
        Vec3 tip = baseCenter.add(axisDir.scale(height));
        for (int i = 0; i < sides; i++) {
            float a0 = rotRad + (float) (Math.PI * 2.0D * i / sides);
            float a1 = rotRad + (float) (Math.PI * 2.0D * (i + 1) / sides);
            Vec3 b0 = baseCenter.add(perpU.scale(Mth.cos(a0) * baseRadius)).add(perpV.scale(Mth.sin(a0) * baseRadius));
            Vec3 b1 = baseCenter.add(perpU.scale(Mth.cos(a1) * baseRadius)).add(perpV.scale(Mth.sin(a1) * baseRadius));

            boolean cyanFacet = (i % 2 == 0);
            float br = cyanFacet ? 0.596F : 0.173F;
            float bg = cyanFacet ? 0.863F : 0.510F;
            float bb = cyanFacet ? 0.949F : 0.788F;
            emitDoubleSidedQuad(matrix, vc, b0, b1, tip, tip,
                    br, bg, bb, 0.84F * alpha,
                    br, bg, bb, 0.84F * alpha,
                    0.95F, 0.99F, 1.00F, 0.96F * alpha,
                    0.95F, 0.99F, 1.00F, 0.96F * alpha);

            if (cyanFacet) {
                renderCrossSegment(matrix, vc, b0, tip, Math.max(0.011F, baseRadius * 0.075F),
                        0.95F, 0.99F, 1.00F, 0.94F * alpha);
            }
        }
    }

    private static void renderFacetedStoneSpire(
            Matrix4f matrix,
            VertexConsumer vc,
            Vec3 baseCenter,
            Vec3 axisDir,
            Vec3 perpU,
            Vec3 perpV,
            float baseRadius,
            float height,
            int sides,
            float rotRad,
            float alpha
    ) {
        if (baseRadius <= 0.01F || height <= 0.02F || alpha <= 0.004F) return;
        Vec3 tip = baseCenter.add(axisDir.scale(height));
        for (int i = 0; i < sides; i++) {
            float a0 = rotRad + (float) (Math.PI * 2.0D * i / sides);
            float a1 = rotRad + (float) (Math.PI * 2.0D * (i + 1) / sides);
            Vec3 b0 = baseCenter.add(perpU.scale(Mth.cos(a0) * baseRadius)).add(perpV.scale(Mth.sin(a0) * baseRadius));
            Vec3 b1 = baseCenter.add(perpU.scale(Mth.cos(a1) * baseRadius)).add(perpV.scale(Mth.sin(a1) * baseRadius));

            boolean graniteFacet = (i % 2 == 0);
            float br = graniteFacet ? 0.353F : 0.467F;
            float bg = graniteFacet ? 0.322F : 0.196F;
            float bb = graniteFacet ? 0.298F : 0.078F;
            emitDoubleSidedQuad(matrix, vc, b0, b1, tip, tip,
                    br, bg, bb, 0.95F * alpha,
                    br, bg, bb, 0.95F * alpha,
                    0.96F, 0.72F, 0.42F, 0.96F * alpha,
                    0.96F, 0.72F, 0.42F, 0.96F * alpha);

            if (graniteFacet) {
                renderCrossSegment(matrix, vc, b0, tip, Math.max(0.012F, baseRadius * 0.085F),
                        1.00F, 0.78F, 0.48F, 0.92F * alpha);
            }
        }
    }

    private static void renderBotanicalBlossom(
            Matrix4f matrix,
            VertexConsumer vc,
            Vec3 center,
            Vec3 axisU,
            Vec3 axisV,
            float radius,
            int petals,
            float rotRad,
            float pr, float pg, float pb,
            float alpha
    ) {
        if (radius <= 0.015F || alpha <= 0.004F) return;
        // Golden pollen core (#FFD84D)
        renderOrientedRing(matrix, vc, center, axisU, axisV, 0.0F, radius * 0.28F, 12,
                1.00F, 0.85F, 0.30F, alpha * 0.96F);
        for (int p = 0; p < petals; p++) {
            float ang = rotRad + (float) (Math.PI * 2.0D * p / petals);
            Vec3 dir = axisU.scale(Mth.cos(ang)).add(axisV.scale(Mth.sin(ang)));
            Vec3 tang = axisU.scale(-Mth.sin(ang)).add(axisV.scale(Mth.cos(ang)));
            Vec3 base = center.add(dir.scale(radius * 0.20D));
            Vec3 left = center.add(dir.scale(radius * 0.62D)).add(tang.scale(radius * 0.24D));
            Vec3 tip = center.add(dir.scale(radius));
            Vec3 right = center.add(dir.scale(radius * 0.62D)).subtract(tang.scale(radius * 0.24D));
            emitDoubleSidedQuad(matrix, vc, base, left, tip, right,
                    1.00F, 0.90F, 0.45F, alpha * 0.92F,
                    pr, pg, pb, alpha * 0.88F,
                    pr, pg, pb, alpha * 0.94F,
                    pr, pg, pb, alpha * 0.88F);
        }
    }

    private static void renderOrientedFrustumBand(
            Matrix4f matrix,
            VertexConsumer vc,
            Vec3 c0,
            Vec3 c1,
            Vec3 axisU,
            Vec3 axisV,
            float r0,
            float r1,
            int segments,
            float r, float g, float b,
            float a0, float a1
    ) {
        if ((r0 <= 0.002F && r1 <= 0.002F) || (a0 <= 0.004F && a1 <= 0.004F)) return;
        for (int i = 0; i < segments; i++) {
            float ang0 = (float) (Math.PI * 2.0D * i / segments);
            float ang1 = (float) (Math.PI * 2.0D * (i + 1) / segments);
            Vec3 d0 = axisU.scale(Mth.cos(ang0)).add(axisV.scale(Mth.sin(ang0)));
            Vec3 d1 = axisU.scale(Mth.cos(ang1)).add(axisV.scale(Mth.sin(ang1)));

            Vec3 v00 = c0.add(d0.scale(r0));
            Vec3 v01 = c0.add(d1.scale(r0));
            Vec3 v11 = c1.add(d1.scale(r1));
            Vec3 v10 = c1.add(d0.scale(r1));

            emitDoubleSidedQuad(matrix, vc, v00, v01, v11, v10,
                    r, g, b, a0,
                    r, g, b, a0,
                    r, g, b, a1,
                    r, g, b, a1);
        }
    }

    private static void renderSweptWindCrescent(
            Matrix4f matrix,
            VertexConsumer vc,
            Vec3 center,
            Vec3 axisU,
            Vec3 axisV,
            float radius,
            float startAng,
            float arcSpan,
            float maxWidth,
            float r, float g, float b, float a
    ) {
        if (radius <= 0.02F || a <= 0.005F) return;
        int segs = 10;
        for (int i = 0; i < segs; i++) {
            float t0 = i / (float) segs;
            float t1 = (i + 1) / (float) segs;
            float a0 = startAng + t0 * arcSpan;
            float a1 = startAng + t1 * arcSpan;
            float prof0 = Mth.sin(t0 * (float) Math.PI);
            float prof1 = Mth.sin(t1 * (float) Math.PI);
            float w0 = maxWidth * prof0;
            float w1 = maxWidth * prof1;

            Vec3 d0 = axisU.scale(Mth.cos(a0)).add(axisV.scale(Mth.sin(a0)));
            Vec3 d1 = axisU.scale(Mth.cos(a1)).add(axisV.scale(Mth.sin(a1)));

            Vec3 in0 = center.add(d0.scale(Math.max(0.01F, radius - w0)));
            Vec3 out0 = center.add(d0.scale(radius + w0 * 0.25F));
            Vec3 out1 = center.add(d1.scale(radius + w1 * 0.25F));
            Vec3 in1 = center.add(d1.scale(Math.max(0.01F, radius - w1)));

            emitDoubleSidedQuad(matrix, vc, in0, out0, out1, in1,
                    r, g, b, a * 0.25F * prof0,
                    1.0F, 1.0F, 1.0F, a * prof0,
                    1.0F, 1.0F, 1.0F, a * prof1,
                    r, g, b, a * 0.25F * prof1);
        }
    }

    private static void renderLowerHemisphereShell(
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

            float y0 = -Mth.sin(phi0) * radius;
            float r0 = Mth.cos(phi0) * radius;
            float y1 = -Mth.sin(phi1) * radius;
            float r1 = Mth.cos(phi1) * radius;

            float a0 = a * (1.10F - 0.65F * ((float) lat / latBands));
            float a1 = a * (1.10F - 0.65F * ((float) (lat + 1) / latBands));

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
            case VOID, UMBRAL, NULL_U, OBLIVION, ESCHATON, SINGULARITY, VACUUM, OBLIVIATE -> true;
            default -> false;
        };
    }
}
