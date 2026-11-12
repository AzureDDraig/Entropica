package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.registry.AestheticGlassRegistry;
import ddraig.net.entropica.spell.vfx.SpellParticleHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom 3D procedural emissive renderer for {@link SpellProjectileEntity}.
 * Replaces flat item-sprite rendering with multi-layered 3D crystalline lances,
 * gravitational-lensed black hole singularities, spinning prismatic chakrams,
 * celestial armillary stars, crackling voltaic javelins, double-helix pods,
 * and smooth 3D velocity-aligned plasma ribbon trails across all 58+ Materia types.
 */
public class SpellProjectileRenderer extends EntityRenderer<SpellProjectileEntity, SpellProjectileRenderer.SpellProjectileRenderState> {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private static final int FULL_LIGHT = 15728880;
    private static final int NO_OVERLAY = OverlayTexture.NO_OVERLAY;

    public static class SpellProjectileRenderState extends EntityRenderState {
        public float ageTicks;
        public Vec3 velocity = new Vec3(0.0D, 0.0D, 1.0D);
        public EssenceType essence = EssenceType.REGULAR;
        public int primaryRgb = 0x62E8FF;
        public int accentRgb = 0xDF78FF;
        public byte archetype = SpellParticleHelper.ARCHETYPE_ARCANE_BOLT;
        public int tier = 1;
        public byte flags = 0;
        public int entitySeed = 0;
        public float halfHeight = 0.125F;
        public final List<Vec3> relativeTrail = new ArrayList<>(15);
    }

    public SpellProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected int getBlockLightLevel(SpellProjectileEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public SpellProjectileRenderState createRenderState() {
        return new SpellProjectileRenderState();
    }

    @Override
    public void extractRenderState(SpellProjectileEntity entity, SpellProjectileRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.ageTicks = entity.tickCount + partialTick;
        Vec3 vel = entity.getDeltaMovement();
        state.velocity = vel.lengthSqr() > 1.0E-5D ? vel : Vec3.directionFromRotation(entity.getXRot(), entity.getYRot());
        state.essence = entity.getVisualEssenceType();
        state.archetype = entity.getVisualArchetype();
        state.tier = entity.getVisualPotencyTier();
        state.flags = entity.getVisualFlags();
        state.entitySeed = entity.getId();
        state.halfHeight = entity.getBbHeight() * 0.5F;

        double animTime = (entity.level() != null ? entity.level().getGameTime() : 0L) + partialTick;
        if (state.essence.isDynamic()) {
            state.primaryRgb = SpellParticleHelper.resolveVibrantPrimaryRgb(state.essence, animTime + state.entitySeed * 3);
            state.accentRgb = SpellParticleHelper.resolveVibrantAccentRgb(state.essence, entity.getVisualSecondaryEssenceType(), animTime + state.entitySeed * 3);
        } else {
            state.primaryRgb = entity.getColorTint();
            state.accentRgb = entity.getAccentColor();
        }

        state.relativeTrail.clear();
        Vec3 interpPos = entity.getPosition(partialTick).add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
        state.relativeTrail.add(new Vec3(0.0D, entity.getBbHeight() * 0.5D, 0.0D));
        for (Vec3 worldPt : entity.getClientTrailPoints()) {
            Vec3 rel = worldPt.add(0.0D, entity.getBbHeight() * 0.5D, 0.0D).subtract(interpPos)
                    .add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
            state.relativeTrail.add(rel);
        }
    }

    @Override
    public void submit(SpellProjectileRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        float tierScale = 0.85F + (state.tier - 1) * 0.07F;
        if ((state.flags & SpellProjectileEntity.VFLAG_SPLIT_CHILD) != 0) {
            tierScale *= 0.68F;
        } else if ((state.flags & SpellProjectileEntity.VFLAG_ORBITAL) != 0) {
            tierScale *= 0.80F;
        }
        final float scale = tierScale;

        float pr = ((state.primaryRgb >> 16) & 0xFF) / 255.0F;
        float pg = ((state.primaryRgb >> 8) & 0xFF) / 255.0F;
        float pb = (state.primaryRgb & 0xFF) / 255.0F;

        float ar = ((state.accentRgb >> 16) & 0xFF) / 255.0F;
        float ag = ((state.accentRgb >> 8) & 0xFF) / 255.0F;
        float ab = (state.accentRgb & 0xFF) / 255.0F;

        // 1. World-aligned 3D tapered plasma ribbon wake (before rotating local head!)
        if (state.relativeTrail.size() >= 2) {
            List<Vec3> trailSnap = new ArrayList<>(state.relativeTrail);
            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, vc) -> {
                renderRibbonTrail(pose.pose(), vc, trailSnap, scale, state.essence, state.ageTicks, pr, pg, pb, ar, ag, ab);
            });
        }

        // 2. Center & orient local pose along velocity vector (+Z forward in local space)
        poseStack.pushPose();
        poseStack.translate(0.0F, state.halfHeight, 0.0F);

        // Singularity archetype renders true astrophysical black hole + Materia accretion halo!
        if (state.archetype == SpellParticleHelper.ARCHETYPE_SINGULARITY_ORB) {
            GravitationalLensingRenderer.renderBlackHole(
                    poseStack,
                    collector,
                    0.13F * scale,
                    state.ageTicks * 1.6F,
                    FULL_LIGHT,
                    NO_OVERLAY
            );
            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, vc) -> {
                Matrix4f mat = pose.pose();
                float rot = state.ageTicks * 0.14F;
                Vec3 ux = new Vec3(Mth.cos(rot), 0.25D, Mth.sin(rot)).normalize();
                Vec3 uz = new Vec3(-Mth.sin(rot), 0.25D, Mth.cos(rot)).normalize();
                renderLocalRing(mat, vc, Vec3.ZERO, ux, uz, 0.26F * scale, 0.38F * scale, 22, pr, pg, pb, 0.78F);
            });
            poseStack.popPose();
            return;
        }

        Vec3 fwd = state.velocity.normalize();
        float yawRad = (float) Mth.atan2(fwd.x, fwd.z);
        float pitchRad = (float) -Math.asin(Mth.clamp(fwd.y, -1.0D, 1.0D));

        poseStack.mulPose(Axis.YP.rotation(yawRad));
        poseStack.mulPose(Axis.XP.rotation(pitchRad));

        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, vc) -> {
            Matrix4f mat = pose.pose();
            switch (state.archetype) {
                case SpellParticleHelper.ARCHETYPE_CRYSTALLINE_LANCE ->
                        renderCrystallineLance(mat, vc, scale, state.ageTicks, pr, pg, pb, ar, ag, ab);
                case SpellParticleHelper.ARCHETYPE_PRISMATIC_CHAKRAM ->
                        renderPrismaticChakram(mat, vc, scale, state.ageTicks);
                case SpellParticleHelper.ARCHETYPE_SOLAR_CROWN ->
                        renderSolarCrown(mat, vc, scale, state.ageTicks, pr, pg, pb, ar, ag, ab);
                case SpellParticleHelper.ARCHETYPE_MOLTEN_METEOR ->
                        renderMoltenMeteor(mat, vc, scale, state.ageTicks, pr, pg, pb, ar, ag, ab);
                case SpellParticleHelper.ARCHETYPE_BIO_HELIX ->
                        renderBioHelix(mat, vc, scale, state.ageTicks, pr, pg, pb, ar, ag, ab);
                case SpellParticleHelper.ARCHETYPE_VOLTAIC_JAVELIN ->
                        renderVoltaicJavelin(mat, vc, scale, state.ageTicks, state.entitySeed, pr, pg, pb, ar, ag, ab);
                default ->
                        renderArcaneBolt(mat, vc, scale, state.ageTicks, pr, pg, pb, ar, ag, ab);
            }

            // Homing seeker ring cue ahead of nose
            if ((state.flags & SpellProjectileEntity.VFLAG_HOMING) != 0) {
                Vec3 rx = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 ry = new Vec3(0.0D, 1.0D, 0.0D);
                renderLocalRing(mat, vc, new Vec3(0.0D, 0.0D, 0.34D * scale), rx, ry,
                        0.15F * scale, 0.19F * scale, 16, 1.0F, 1.0F, 1.0F, 0.72F);
            }

            // Wall-phasing spectral outer aura ring
            if ((state.flags & SpellProjectileEntity.VFLAG_WALL_PHASING) != 0) {
                float pulse = 0.85F + 0.15F * Mth.sin(state.ageTicks * 0.35F);
                renderElongatedBipyramid(mat, vc, 0.28F * scale * pulse, 0.42F * scale * pulse, 0.38F * scale * pulse,
                        6, 0.0F, ar, ag, ab, 0.24F);
            }
        });

        poseStack.popPose();
    }

    // =========================================================================
    // ARCHETYPE 0: ARCANE BOLT (GYROSCOPIC PLASMA ORB)
    // =========================================================================

    private static void renderArcaneBolt(
            Matrix4f mat, VertexConsumer vc, float scale, float age,
            float pr, float pg, float pb, float ar, float ag, float ab
    ) {
        float spin = age * 0.22F;
        // White-hot inner kernel
        renderElongatedBipyramid(mat, vc, 0.11F * scale, 0.22F * scale, 0.18F * scale, 6, spin,
                1.0F, 1.0F, 1.0F, 0.96F);
        // Materia plasma shell
        renderElongatedBipyramid(mat, vc, 0.19F * scale, 0.34F * scale, 0.30F * scale, 6, -spin * 0.7F,
                pr, pg, pb, 0.68F);

        // Two counter-rotating gyroscopic arcane rings
        Vec3 rx = new Vec3(Mth.cos(spin), Mth.sin(spin) * 0.45D, 0.0D).normalize();
        Vec3 rz = new Vec3(0.0D, Mth.cos(spin) * 0.45D, 1.0D).normalize();
        renderLocalRing(mat, vc, Vec3.ZERO, rx, rz, 0.24F * scale, 0.30F * scale, 18, ar, ag, ab, 0.82F);

        Vec3 rx2 = new Vec3(Mth.cos(-spin), 0.0D, Mth.sin(-spin)).normalize();
        Vec3 ry2 = new Vec3(-Mth.sin(-spin) * 0.5D, 1.0D, Mth.cos(-spin) * 0.5D).normalize();
        renderLocalRing(mat, vc, Vec3.ZERO, rx2, ry2, 0.22F * scale, 0.27F * scale, 18, pr, pg, pb, 0.72F);
    }

    // =========================================================================
    // ARCHETYPE 1: CRYSTALLINE / RAILGUN LANCE
    // =========================================================================

    private static void renderCrystallineLance(
            Matrix4f mat, VertexConsumer vc, float scale, float age,
            float pr, float pg, float pb, float ar, float ag, float ab
    ) {
        float spin = age * 0.32F;
        // Razor needle white-hot core
        renderElongatedBipyramid(mat, vc, 0.075F * scale, 0.68F * scale, 0.42F * scale, 6, spin,
                1.0F, 1.0F, 1.0F, 0.96F);
        // Faceted outer crystal spearhead
        renderElongatedBipyramid(mat, vc, 0.155F * scale, 0.82F * scale, 0.52F * scale, 6, spin,
                pr, pg, pb, 0.74F);

        // Twin counter-rotating shock collars along shaft
        Vec3 rx = new Vec3(Mth.cos(spin), Mth.sin(spin), 0.0D);
        Vec3 ry = new Vec3(-Mth.sin(spin), Mth.cos(spin), 0.0D);
        renderLocalRing(mat, vc, new Vec3(0.0D, 0.0D, 0.08D * scale), rx, ry,
                0.18F * scale, 0.26F * scale, 6, ar, ag, ab, 0.88F);
        renderLocalRing(mat, vc, new Vec3(0.0D, 0.0D, -0.22D * scale), ry, rx,
                0.12F * scale, 0.19F * scale, 6, 1.0F, 1.0F, 1.0F, 0.76F);
    }

    // =========================================================================
    // ARCHETYPE 2: PRISMATIC OSCILLATING CHAKRAM DISC
    // =========================================================================

    private static void renderPrismaticChakram(Matrix4f mat, VertexConsumer vc, float scale, float age) {
        float spin = age * 0.55F;
        int blades = 12;
        float rIn = 0.14F * scale;
        float rMid = 0.38F * scale;
        float rTip = 0.50F * scale;

        // White-hot central prism hub
        renderElongatedBipyramid(mat, vc, 0.13F * scale, 0.12F * scale, 0.12F * scale, 8, -spin,
                1.0F, 1.0F, 1.0F, 0.95F);

        // Horizontal rainbow refractive chakram ring + sawtooth outer star tips
        for (int i = 0; i < blades; i++) {
            float a0 = spin + (float) (Math.PI * 2.0D * i / blades);
            float a1 = spin + (float) (Math.PI * 2.0D * (i + 1) / blades);
            float aMid = (a0 + a1) * 0.5F;

            int rgb = AestheticGlassRegistry.hsbToRgb(((i / (float) blades) + age * 0.03F) % 1.0F, 0.78F, 1.0F);
            float cr = ((rgb >> 16) & 0xFF) / 255.0F;
            float cg = ((rgb >> 8) & 0xFF) / 255.0F;
            float cb = (rgb & 0xFF) / 255.0F;

            Vec3 in0 = new Vec3(Mth.cos(a0) * rIn, 0.0D, Mth.sin(a0) * rIn);
            Vec3 out0 = new Vec3(Mth.cos(a0) * rMid, 0.0D, Mth.sin(a0) * rMid);
            Vec3 out1 = new Vec3(Mth.cos(a1) * rMid, 0.0D, Mth.sin(a1) * rMid);
            Vec3 in1 = new Vec3(Mth.cos(a1) * rIn, 0.0D, Mth.sin(a1) * rIn);
            emitDoubleSidedQuad(mat, vc, in0, out0, out1, in1, cr, cg, cb, 0.86F);

            Vec3 tip = new Vec3(Mth.cos(aMid) * rTip, 0.0D, Mth.sin(aMid) * rTip);
            emitDoubleSidedQuad(mat, vc, out0, tip, tip, out1, 1.0F, 1.0F, 1.0F, 0.92F);
        }
    }

    // =========================================================================
    // ARCHETYPE 4: SOLAR / CELESTIAL ARMILLARY STAR
    // =========================================================================

    private static void renderSolarCrown(
            Matrix4f mat, VertexConsumer vc, float scale, float age,
            float pr, float pg, float pb, float ar, float ag, float ab
    ) {
        float spin = age * 0.18F;
        // 3 orthogonal stellar spikes forming a 6-ray blazing star
        renderElongatedBipyramid(mat, vc, 0.09F * scale, 0.42F * scale, 0.42F * scale, 4, spin,
                1.0F, 1.0F, 1.0F, 0.96F);
        renderTransverseSpikes(mat, vc, 0.40F * scale, 0.085F * scale, spin, ar, ag, ab, 0.90F);

        // Outer golden/celestial corona shell
        renderElongatedBipyramid(mat, vc, 0.20F * scale, 0.24F * scale, 0.24F * scale, 8, -spin,
                pr, pg, pb, 0.58F);

        // Tilted celestial armillary halo
        Vec3 rx = new Vec3(Mth.cos(spin), 0.35D, Mth.sin(spin)).normalize();
        Vec3 rz = new Vec3(-Mth.sin(spin), 0.35D, Mth.cos(spin)).normalize();
        renderLocalRing(mat, vc, Vec3.ZERO, rx, rz, 0.30F * scale, 0.37F * scale, 22, pr, pg, pb, 0.84F);
    }

    // =========================================================================
    // ARCHETYPE 5: MOLTEN METEOR / IGNEOUS BOULDER
    // =========================================================================

    private static void renderMoltenMeteor(
            Matrix4f mat, VertexConsumer vc, float scale, float age,
            float pr, float pg, float pb, float ar, float ag, float ab
    ) {
        float tumble = age * 0.28F;
        // White-yellow incandescent magma heart
        renderElongatedBipyramid(mat, vc, 0.16F * scale, 0.20F * scale, 0.20F * scale, 5, tumble,
                1.0F, 0.96F, 0.78F, 0.96F);
        // Counter-tumbling jagged igneous outer shells
        renderElongatedBipyramid(mat, vc, 0.24F * scale, 0.28F * scale, 0.26F * scale, 5, -tumble * 1.3F,
                pr, pg, pb, 0.80F);
        renderElongatedBipyramid(mat, vc, 0.27F * scale, 0.22F * scale, 0.32F * scale, 4, tumble * 0.7F + 1.1F,
                ar, ag, ab, 0.52F);
    }

    // =========================================================================
    // ARCHETYPE 6: BIO-POD & DOUBLE HELIX
    // =========================================================================

    private static void renderBioHelix(
            Matrix4f mat, VertexConsumer vc, float scale, float age,
            float pr, float pg, float pb, float ar, float ag, float ab
    ) {
        float pulse = 1.0F + 0.10F * Mth.sin(age * 0.35F);
        renderElongatedBipyramid(mat, vc, 0.11F * scale * pulse, 0.26F * scale, 0.20F * scale, 6, age * 0.15F,
                1.0F, 1.0F, 1.0F, 0.92F);
        renderElongatedBipyramid(mat, vc, 0.18F * scale * pulse, 0.34F * scale, 0.28F * scale, 6, -age * 0.15F,
                pr, pg, pb, 0.68F);

        // Twin helical strands wrapping around the bolt
        int steps = 10;
        float helixR = 0.22F * scale;
        for (int s = 0; s < 2; s++) {
            float phase = (float) (s * Math.PI) - age * 0.35F;
            Vec3 prev = null;
            for (int i = 0; i <= steps; i++) {
                float t = (float) i / steps;
                float z = Mth.lerp(t, 0.32F * scale, -0.45F * scale);
                float env = Mth.sin(t * (float) Math.PI);
                float ang = phase + t * 4.2F;
                Vec3 pt = new Vec3(Mth.cos(ang) * helixR * env, Mth.sin(ang) * helixR * env, z);
                if (prev != null) {
                    renderLocalSegment(mat, vc, prev, pt, 0.038F * scale,
                            s == 0 ? pr : ar, s == 0 ? pg : ag, s == 0 ? pb : ab, 0.85F);
                }
                prev = pt;
            }
        }
    }

    // =========================================================================
    // ARCHETYPE 7: VOLTAIC LIGHTNING JAVELIN
    // =========================================================================

    private static void renderVoltaicJavelin(
            Matrix4f mat, VertexConsumer vc, float scale, float age, int seed,
            float pr, float pg, float pb, float ar, float ag, float ab
    ) {
        // Blazing lightning spear core
        renderElongatedBipyramid(mat, vc, 0.08F * scale, 0.64F * scale, 0.44F * scale, 4, age * 0.4F,
                1.0F, 1.0F, 1.0F, 0.96F);
        renderElongatedBipyramid(mat, vc, 0.14F * scale, 0.74F * scale, 0.50F * scale, 4, -age * 0.4F,
                pr, pg, pb, 0.72F);

        // 3 crackling 3D jagged arcs dancing from nose to tail
        int flicker = (int) (age * 0.85F) + seed * 13;
        for (int a = 0; a < 3; a++) {
            Vec3 prev = new Vec3(0.0D, 0.0D, 0.65D * scale);
            int segs = 5;
            for (int i = 1; i <= segs; i++) {
                float t = (float) i / segs;
                float z = Mth.lerp(t, 0.65F * scale, -0.52F * scale);
                Vec3 curr;
                if (i == segs) {
                    curr = new Vec3(0.0D, 0.0D, z);
                } else {
                    float jx = (hash01(flicker + a * 31, i * 7) - 0.5F) * 0.42F * scale;
                    float jy = (hash01(flicker + a * 31, i * 7 + 3) - 0.5F) * 0.42F * scale;
                    curr = new Vec3(jx, jy, z);
                }
                renderLocalSegment(mat, vc, prev, curr, 0.036F * scale, ar, ag, ab, 0.88F);
                renderLocalSegment(mat, vc, prev, curr, 0.016F * scale, 1.0F, 1.0F, 1.0F, 0.95F);
                prev = curr;
            }
        }
    }

    // =========================================================================
    // 3D VELOCITY-ALIGNED RIBBON WAKE
    // =========================================================================

    private static void renderRibbonTrail(
            Matrix4f mat,
            VertexConsumer vc,
            List<Vec3> pts,
            float scale,
            EssenceType essence,
            float ageTicks,
            float pr, float pg, float pb,
            float ar, float ag, float ab
    ) {
        int count = pts.size();
        float maxHalfW = 0.155F * scale;

        for (int i = 0; i < count - 1; i++) {
            Vec3 p0 = pts.get(i);
            Vec3 p1 = pts.get(i + 1);
            Vec3 seg = p1.subtract(p0);
            if (seg.lengthSqr() < 1.0E-6D) continue;

            float t0 = (float) i / (count - 1);
            float t1 = (float) (i + 1) / (count - 1);

            float w0 = maxHalfW * (1.0F - t0);
            float w1 = maxHalfW * (1.0F - t1);

            float a0 = 0.76F * (1.0F - t0) * (1.0F - t0 * 0.4F);
            float a1 = 0.76F * (1.0F - t1) * (1.0F - t1 * 0.4F);

            float r0 = Mth.lerp(t0, pr, ar);
            float g0 = Mth.lerp(t0, pg, ag);
            float b0 = Mth.lerp(t0, pb, ab);

            float r1 = Mth.lerp(t1, pr, ar);
            float g1 = Mth.lerp(t1, pg, ag);
            float b1 = Mth.lerp(t1, pb, ab);

            if (essence == EssenceType.PRISMATIC || essence == EssenceType.ENTROPICA) {
                int c0 = AestheticGlassRegistry.hsbToRgb((t0 + ageTicks * 0.04F) % 1.0F, 0.75F, 1.0F);
                int c1 = AestheticGlassRegistry.hsbToRgb((t1 + ageTicks * 0.04F) % 1.0F, 0.75F, 1.0F);
                r0 = ((c0 >> 16) & 0xFF) / 255.0F; g0 = ((c0 >> 8) & 0xFF) / 255.0F; b0 = (c0 & 0xFF) / 255.0F;
                r1 = ((c1 >> 16) & 0xFF) / 255.0F; g1 = ((c1 >> 8) & 0xFF) / 255.0F; b1 = (c1 & 0xFF) / 255.0F;
            }

            Vec3 fwd = seg.normalize();
            Vec3 ref = Math.abs(fwd.y) > 0.92D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
            Vec3 right = fwd.cross(ref).normalize();
            Vec3 up = right.cross(fwd).normalize();

            // Crossed horizontal + vertical ribbon planes so wake looks full from every angle
            emitGradientDoubleSidedQuad(mat, vc,
                    p0.subtract(right.scale(w0)), p0.add(right.scale(w0)),
                    p1.add(right.scale(w1)), p1.subtract(right.scale(w1)),
                    r0, g0, b0, a0, r1, g1, b1, a1);
            emitGradientDoubleSidedQuad(mat, vc,
                    p0.subtract(up.scale(w0)), p0.add(up.scale(w0)),
                    p1.add(up.scale(w1)), p1.subtract(up.scale(w1)),
                    r0, g0, b0, a0, r1, g1, b1, a1);

            // Bright thin inner core filament along first half of trail
            if (t0 < 0.55F) {
                float cw0 = w0 * 0.35F;
                float cw1 = w1 * 0.35F;
                emitGradientDoubleSidedQuad(mat, vc,
                        p0.subtract(right.scale(cw0)), p0.add(right.scale(cw0)),
                        p1.add(right.scale(cw1)), p1.subtract(right.scale(cw1)),
                        1.0F, 1.0F, 1.0F, a0 * 0.9F, 1.0F, 1.0F, 1.0F, a1 * 0.5F);
            }
        }
    }

    // =========================================================================
    // LOCAL 3D PRIMITIVE HELPERS
    // =========================================================================

    private static void renderElongatedBipyramid(
            Matrix4f mat,
            VertexConsumer vc,
            float waistRadius,
            float noseLength,
            float tailLength,
            int sides,
            float rollRad,
            float r, float g, float b, float a
    ) {
        Vec3 nose = new Vec3(0.0D, 0.0D, noseLength);
        Vec3 tail = new Vec3(0.0D, 0.0D, -tailLength);

        for (int i = 0; i < sides; i++) {
            float a0 = rollRad + (float) (Math.PI * 2.0D * i / sides);
            float a1 = rollRad + (float) (Math.PI * 2.0D * (i + 1) / sides);

            Vec3 w0 = new Vec3(Mth.cos(a0) * waistRadius, Mth.sin(a0) * waistRadius, 0.0D);
            Vec3 w1 = new Vec3(Mth.cos(a1) * waistRadius, Mth.sin(a1) * waistRadius, 0.0D);

            emitDoubleSidedQuad(mat, vc, nose, w0, w1, nose, r, g, b, a);
            emitDoubleSidedQuad(mat, vc, tail, w1, w0, tail, r, g, b, a * 0.88F);
        }
    }

    private static void renderTransverseSpikes(
            Matrix4f mat,
            VertexConsumer vc,
            float tipLen,
            float waist,
            float roll,
            float r, float g, float b, float a
    ) {
        Vec3 rx = new Vec3(Mth.cos(roll), Mth.sin(roll), 0.0D);
        Vec3 ry = new Vec3(-Mth.sin(roll), Mth.cos(roll), 0.0D);
        Vec3 rz = new Vec3(0.0D, 0.0D, 1.0D);

        renderSpikeAlongAxis(mat, vc, rx, ry, rz, tipLen, waist, r, g, b, a);
        renderSpikeAlongAxis(mat, vc, ry, rx, rz, tipLen, waist, r, g, b, a);
    }

    private static void renderSpikeAlongAxis(
            Matrix4f mat, VertexConsumer vc,
            Vec3 mainAxis, Vec3 perpA, Vec3 perpB,
            float tipLen, float waist,
            float r, float g, float b, float a
    ) {
        Vec3 posTip = mainAxis.scale(tipLen);
        Vec3 negTip = mainAxis.scale(-tipLen);
        Vec3[] ring = new Vec3[]{
                perpA.scale(waist),
                perpB.scale(waist),
                perpA.scale(-waist),
                perpB.scale(-waist)
        };
        for (int i = 0; i < 4; i++) {
            Vec3 w0 = ring[i];
            Vec3 w1 = ring[(i + 1) % 4];
            emitDoubleSidedQuad(mat, vc, posTip, w0, w1, posTip, r, g, b, a);
            emitDoubleSidedQuad(mat, vc, negTip, w1, w0, negTip, r, g, b, a);
        }
    }

    private static void renderLocalRing(
            Matrix4f mat, VertexConsumer vc,
            Vec3 center, Vec3 u, Vec3 v,
            float rIn, float rOut, int segs,
            float r, float g, float b, float a
    ) {
        for (int i = 0; i < segs; i++) {
            float a0 = (float) (Math.PI * 2.0D * i / segs);
            float a1 = (float) (Math.PI * 2.0D * (i + 1) / segs);
            Vec3 d0 = u.scale(Mth.cos(a0)).add(v.scale(Mth.sin(a0)));
            Vec3 d1 = u.scale(Mth.cos(a1)).add(v.scale(Mth.sin(a1)));
            emitDoubleSidedQuad(mat, vc,
                    center.add(d0.scale(rIn)), center.add(d0.scale(rOut)),
                    center.add(d1.scale(rOut)), center.add(d1.scale(rIn)),
                    r, g, b, a);
        }
    }

    private static void renderLocalSegment(
            Matrix4f mat, VertexConsumer vc,
            Vec3 p0, Vec3 p1, float halfW,
            float r, float g, float b, float a
    ) {
        Vec3 span = p1.subtract(p0);
        if (span.lengthSqr() < 1.0E-6D) return;
        Vec3 fwd = span.normalize();
        Vec3 ref = Math.abs(fwd.y) > 0.92D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = fwd.cross(ref).normalize().scale(halfW);
        Vec3 up = right.normalize().cross(fwd).normalize().scale(halfW);

        emitDoubleSidedQuad(mat, vc, p0.subtract(right), p0.add(right), p1.add(right), p1.subtract(right), r, g, b, a);
        emitDoubleSidedQuad(mat, vc, p0.subtract(up), p0.add(up), p1.add(up), p1.subtract(up), r, g, b, a);
    }

    private static void emitDoubleSidedQuad(
            Matrix4f mat, VertexConsumer vc,
            Vec3 v0, Vec3 v1, Vec3 v2, Vec3 v3,
            float r, float g, float b, float a
    ) {
        vc.addVertex(mat, (float) v0.x, (float) v0.y, (float) v0.z)
                .setColor(r, g, b, a).setUv(0.0F, 0.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(mat, (float) v1.x, (float) v1.y, (float) v1.z)
                .setColor(r, g, b, a).setUv(0.0F, 1.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(mat, (float) v2.x, (float) v2.y, (float) v2.z)
                .setColor(r, g, b, a).setUv(1.0F, 1.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(mat, (float) v3.x, (float) v3.y, (float) v3.z)
                .setColor(r, g, b, a).setUv(1.0F, 0.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);

        vc.addVertex(mat, (float) v3.x, (float) v3.y, (float) v3.z)
                .setColor(r, g, b, a).setUv(1.0F, 0.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(mat, (float) v2.x, (float) v2.y, (float) v2.z)
                .setColor(r, g, b, a).setUv(1.0F, 1.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(mat, (float) v1.x, (float) v1.y, (float) v1.z)
                .setColor(r, g, b, a).setUv(0.0F, 1.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(mat, (float) v0.x, (float) v0.y, (float) v0.z)
                .setColor(r, g, b, a).setUv(0.0F, 0.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
    }

    private static void emitGradientDoubleSidedQuad(
            Matrix4f mat, VertexConsumer vc,
            Vec3 v0, Vec3 v1, Vec3 v2, Vec3 v3,
            float r0, float g0, float b0, float a0,
            float r1, float g1, float b1, float a1
    ) {
        vc.addVertex(mat, (float) v0.x, (float) v0.y, (float) v0.z)
                .setColor(r0, g0, b0, a0).setUv(0.0F, 0.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(mat, (float) v1.x, (float) v1.y, (float) v1.z)
                .setColor(r0, g0, b0, a0).setUv(0.0F, 1.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(mat, (float) v2.x, (float) v2.y, (float) v2.z)
                .setColor(r1, g1, b1, a1).setUv(1.0F, 1.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(mat, (float) v3.x, (float) v3.y, (float) v3.z)
                .setColor(r1, g1, b1, a1).setUv(1.0F, 0.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);

        vc.addVertex(mat, (float) v3.x, (float) v3.y, (float) v3.z)
                .setColor(r1, g1, b1, a1).setUv(1.0F, 0.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(mat, (float) v2.x, (float) v2.y, (float) v2.z)
                .setColor(r1, g1, b1, a1).setUv(1.0F, 1.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(mat, (float) v1.x, (float) v1.y, (float) v1.z)
                .setColor(r0, g0, b0, a0).setUv(0.0F, 1.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(mat, (float) v0.x, (float) v0.y, (float) v0.z)
                .setColor(r0, g0, b0, a0).setUv(0.0F, 0.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
    }

    private static float hash01(int a, int b) {
        int h = a * 374761393 + b * 668265263;
        h = (h ^ (h >> 13)) * 1274126177;
        h = h ^ (h >> 16);
        return (h & 0x7FFFFFFF) / (float) 0x7FFFFFFF;
    }
}
