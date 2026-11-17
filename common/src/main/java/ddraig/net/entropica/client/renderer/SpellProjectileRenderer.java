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
        if ((state.flags & SpellProjectileEntity.VFLAG_HEAVY_FLUID) != 0) {
            tierScale *= 1.38F;
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
            final byte archSnap = state.archetype;
            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, vc) -> {
                renderRibbonTrail(pose.pose(), vc, trailSnap, scale, state.essence, archSnap, state.ageTicks, pr, pg, pb, ar, ag, ab);
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
                case SpellParticleHelper.ARCHETYPE_WIND_SPIRAL ->
                        renderWindSpiral(mat, vc, scale, state.ageTicks, pr, pg, pb, ar, ag, ab);
                case SpellParticleHelper.ARCHETYPE_WIND_SLASH ->
                        renderFlyingWindSlash(mat, vc, scale, state.ageTicks, state.entitySeed, pr, pg, pb, ar, ag, ab);
                case SpellParticleHelper.ARCHETYPE_HYDRO_ORB ->
                        renderHydroOrb(mat, vc, scale, state.ageTicks, state.entitySeed,
                                (state.flags & SpellProjectileEntity.VFLAG_HEAVY_FLUID) != 0, pr, pg, pb, ar, ag, ab);
                case SpellParticleHelper.ARCHETYPE_BRIAR_SEED ->
                        renderBriarSeed(mat, vc, scale, state.ageTicks, state.entitySeed, pr, pg, pb, ar, ag, ab);
                case SpellParticleHelper.ARCHETYPE_QUAKE_SPIRE ->
                        renderQuakeSpire(mat, vc, scale, state.ageTicks, state.entitySeed,
                                (state.flags & SpellProjectileEntity.VFLAG_HEAVY_FLUID) != 0, pr, pg, pb, ar, ag, ab);
                case SpellParticleHelper.ARCHETYPE_GLACIAL_SHARD ->
                        renderGlacialShard(mat, vc, scale, state.ageTicks, state.entitySeed,
                                (state.flags & SpellProjectileEntity.VFLAG_HEAVY_FLUID) != 0, pr, pg, pb, ar, ag, ab);
                case SpellParticleHelper.ARCHETYPE_NETHERFIRE_COMET ->
                        renderNetherfireComet(mat, vc, scale, state.ageTicks, state.entitySeed, pr, pg, pb, ar, ag, ab);
                case SpellParticleHelper.ARCHETYPE_DAWN_STAR ->
                        renderDawnStar(mat, vc, scale, state.ageTicks, state.entitySeed, pr, pg, pb, ar, ag, ab);
                case SpellParticleHelper.ARCHETYPE_UMBRAL_SNARE ->
                        renderShadowSnareMissile(mat, vc, scale, state.ageTicks, state.entitySeed, pr, pg, pb, ar, ag, ab);
                case SpellParticleHelper.ARCHETYPE_UNDEAD_SKULL ->
                        renderUndeadSkullMissile(mat, vc, scale, state.ageTicks, state.entitySeed,
                                (state.flags & SpellProjectileEntity.VFLAG_HEAVY_FLUID) != 0, pr, pg, pb, ar, ag, ab);
                case SpellParticleHelper.ARCHETYPE_DUST_DEVIL ->
                        renderDustDevilMissile(mat, vc, scale, state.ageTicks, state.entitySeed, pr, pg, pb, ar, ag, ab);
                case SpellParticleHelper.ARCHETYPE_BALL_LIGHTNING ->
                        renderBallLightningOrb(mat, vc, scale, state.ageTicks, state.entitySeed, pr, pg, pb, ar, ag, ab);
                case SpellParticleHelper.ARCHETYPE_SQUALL_VORTEX ->
                        renderSquallVortexSlug(mat, vc, scale, state.ageTicks, state.entitySeed, pr, pg, pb, ar, ag, ab);
                case SpellParticleHelper.ARCHETYPE_GLACIAL_ICE_LANCE ->
                        renderGlacialIceLance(mat, vc, scale, state.ageTicks, state.entitySeed,
                                (state.flags & SpellProjectileEntity.VFLAG_HEAVY_FLUID) != 0, pr, pg, pb, ar, ag, ab);
                case SpellParticleHelper.ARCHETYPE_OVERGROWTH_BRIAR_POD ->
                        renderOvergrowthBriarPod(mat, vc, scale, state.ageTicks, state.entitySeed, pr, pg, pb, ar, ag, ab);
                case SpellParticleHelper.ARCHETYPE_CHOKING_DUST_POD ->
                        renderChokingDustPod(mat, vc, scale, state.ageTicks, state.entitySeed, pr, pg, pb, ar, ag, ab);
                case SpellParticleHelper.ARCHETYPE_FUNGAL_PUFFBALL ->
                        renderFungalPuffball(mat, vc, scale, state.ageTicks, state.entitySeed, pr, pg, pb, ar, ag, ab);
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
        // 1. White-hot / molten-gold (#FFD15C) incandescent magma heart
        renderElongatedBipyramid(mat, vc, 0.17F * scale, 0.21F * scale, 0.21F * scale, 6, tumble,
                1.0F, 0.94F, 0.68F, 0.98F);
        // 2. Boiling volcanic red-orange (#B83C08) liquid magma mantle
        renderElongatedBipyramid(mat, vc, 0.25F * scale, 0.28F * scale, 0.27F * scale, 6, -tumble * 1.25F,
                pr, pg, pb, 0.88F);
        // 3. Fractured dark obsidian-pumice boulder crust plates tumbling over glowing lava seams!
        renderElongatedBipyramid(mat, vc, 0.29F * scale, 0.24F * scale, 0.30F * scale, 5, tumble * 0.75F + 0.9F,
                0.165F, 0.090F, 0.071F, 0.82F);
        for (int p = 0; p < 6; p++) {
            float pAng = tumble * 0.9F + (float) (Math.PI * 2.0D * p / 6.0D);
            Vec3 crustPos = new Vec3(
                    Mth.cos(pAng) * 0.22D * scale,
                    Mth.sin(pAng * 1.5F) * 0.14D * scale,
                    -0.06D * scale + Mth.sin(pAng) * 0.18D * scale
            );
            booleanslag: {
                boolean glowing = (p % 2 == 0);
                renderLocalSegment(mat, vc,
                        crustPos.subtract(0.0D, 0.0D, 0.08D * scale),
                        crustPos.add(0.0D, 0.0D, 0.08D * scale),
                        0.058F * scale,
                        glowing ? ar : 0.165F,
                        glowing ? ag : 0.090F,
                        glowing ? ab : 0.071F,
                        glowing ? 0.94F : 0.86F);
                break booleanslag;
            }
        }
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
            byte archetype,
            float ageTicks,
            float pr, float pg, float pb,
            float ar, float ag, float ab
    ) {
        int count = pts.size();
        boolean windSlash = (archetype == SpellParticleHelper.ARCHETYPE_WIND_SLASH);
        boolean umbralSnare = (archetype == SpellParticleHelper.ARCHETYPE_UMBRAL_SNARE);
        float maxHalfW = (windSlash ? 0.34F : (umbralSnare ? 0.185F : 0.155F)) * scale;

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
            float vScale = windSlash ? 0.38F : 1.0F;
            emitGradientDoubleSidedQuad(mat, vc,
                    p0.subtract(up.scale(w0 * vScale)), p0.add(up.scale(w0 * vScale)),
                    p1.add(up.scale(w1 * vScale)), p1.subtract(up.scale(w1 * vScale)),
                    r0, g0, b0, a0, r1, g1, b1, a1);

            if (windSlash) {
                // Twin razor wingtip contrails streaming from the outer horns of the flying Crescent Wind-Blade!
                double tipSpan0 = 0.72D * scale * (1.0F - t0 * 0.35F);
                double tipSpan1 = 0.72D * scale * (1.0F - t1 * 0.35F);
                for (int side = -1; side <= 1; side += 2) {
                    Vec3 tip0 = p0.add(right.scale(side * tipSpan0)).add(up.scale(side * 0.18D * scale));
                    Vec3 tip1 = p1.add(right.scale(side * tipSpan1)).add(up.scale(side * 0.18D * scale));
                    renderLocalSegment(mat, vc, tip0, tip1, 0.026F * scale * (1.0F - t0 * 0.6F),
                            1.0F, 0.99F, 0.96F, 0.85F * (1.0F - t0));
                }
            }

            // Bright thin inner core filament along first half of trail (or light-drinking obsidian core for Shadow Snare!)
            if (t0 < 0.55F) {
                float cw0 = w0 * (umbralSnare ? 0.48F : 0.35F);
                float cw1 = w1 * (umbralSnare ? 0.48F : 0.35F);
                float cr = umbralSnare ? 0.08F : 1.0F;
                float cg = umbralSnare ? 0.05F : 1.0F;
                float cb = umbralSnare ? 0.10F : 1.0F;
                emitGradientDoubleSidedQuad(mat, vc,
                        p0.subtract(right.scale(cw0)), p0.add(right.scale(cw0)),
                        p1.add(right.scale(cw1)), p1.subtract(right.scale(cw1)),
                        cr, cg, cb, a0 * 0.94F, cr, cg, cb, a1 * 0.55F);
            }

            // Twin 3D helical corkscrew slipstream filaments wrapped around Air / Aura wakes!
            if (essence == EssenceType.AIR || essence == EssenceType.AURA) {
                float helixR0 = (0.24F * scale) * (1.0F - t0 * 0.72F);
                float helixR1 = (0.24F * scale) * (1.0F - t1 * 0.72F);
                float phase0 = -ageTicks * 0.65F + t0 * 14.0F;
                float phase1 = -ageTicks * 0.65F + t1 * 14.0F;
                for (int h = 0; h < 2; h++) {
                    float off = h * (float) Math.PI;
                    Vec3 hp0 = p0.add(right.scale(Mth.cos(phase0 + off) * helixR0))
                            .add(up.scale(Mth.sin(phase0 + off) * helixR0));
                    Vec3 hp1 = p1.add(right.scale(Mth.cos(phase1 + off) * helixR1))
                            .add(up.scale(Mth.sin(phase1 + off) * helixR1));
                    float sr = (h == 0) ? 1.0F : pr;
                    float sg = (h == 0) ? 0.99F : pg;
                    float sb = (h == 0) ? 0.96F : pb;
                    renderLocalSegment(mat, vc, hp0, hp1, 0.028F * scale * (1.0F - t0 * 0.65F),
                            sr, sg, sb, 0.84F * (1.0F - t0));
                }
            } else if (archetype == SpellParticleHelper.ARCHETYPE_HYDRO_ORB) {
                // Twin undulating whitewater & cerulean liquid wake streams for Hydro-Cannon Slug & Viscous Fluid Mass!
                float waveR0 = (0.20F * scale) * (1.0F - t0 * 0.68F);
                float waveR1 = (0.20F * scale) * (1.0F - t1 * 0.68F);
                float phase0 = -ageTicks * 0.48F + t0 * 10.5F;
                float phase1 = -ageTicks * 0.48F + t1 * 10.5F;
                for (int h = 0; h < 2; h++) {
                    float off = h * (float) Math.PI;
                    Vec3 hp0 = p0.add(right.scale(Mth.cos(phase0 + off) * waveR0))
                            .add(up.scale(Mth.sin(phase0 + off) * waveR0 * 0.75D));
                    Vec3 hp1 = p1.add(right.scale(Mth.cos(phase1 + off) * waveR1))
                            .add(up.scale(Mth.sin(phase1 + off) * waveR1 * 0.75D));
                    float sr = (h == 0) ? 0.88F : 0.14F;
                    float sg = (h == 0) ? 0.98F : 0.70F;
                    float sb = (h == 0) ? 1.00F : 1.00F;
                    renderLocalSegment(mat, vc, hp0, hp1, 0.032F * scale * (1.0F - t0 * 0.60F),
                            sr, sg, sb, 0.82F * (1.0F - t0));
                }
            } else if (archetype == SpellParticleHelper.ARCHETYPE_BRIAR_SEED) {
                // Twin coiling 3D thorny vine creeper strands (#1A5923 forest green & #B4FF78 sprout-lime)
                float vineR0 = (0.19F * scale) * (1.0F - t0 * 0.65F);
                float vineR1 = (0.19F * scale) * (1.0F - t1 * 0.65F);
                float phase0 = -ageTicks * 0.52F + t0 * 12.0F;
                float phase1 = -ageTicks * 0.52F + t1 * 12.0F;
                for (int v = 0; v < 2; v++) {
                    float off = v * (float) Math.PI;
                    Vec3 vp0 = p0.add(right.scale(Mth.cos(phase0 + off) * vineR0))
                            .add(up.scale(Mth.sin(phase0 + off) * vineR0));
                    Vec3 vp1 = p1.add(right.scale(Mth.cos(phase1 + off) * vineR1))
                            .add(up.scale(Mth.sin(phase1 + off) * vineR1));
                    float vr = (v == 0) ? 0.10F : 0.71F;
                    float vg = (v == 0) ? 0.35F : 1.00F;
                    float vb = (v == 0) ? 0.14F : 0.47F;
                    renderLocalSegment(mat, vc, vp0, vp1, 0.028F * scale * (1.0F - t0 * 0.55F),
                            vr, vg, vb, 0.86F * (1.0F - t0));
                }
            } else if (archetype == SpellParticleHelper.ARCHETYPE_QUAKE_SPIRE) {
                // Section 6.5 Quake Spire: twin jagged subterranean seismic fault-seam fissures (#773214 & #FFC278)!
                float seamSpread0 = (0.22F * scale) * (1.0F - t0 * 0.42F);
                float seamSpread1 = (0.22F * scale) * (1.0F - t1 * 0.42F);
                for (int f = 0; f < 2; f++) {
                    float side = (f == 0) ? -1.0F : 1.0F;
                    float zig0 = ((i + f) % 2 == 0 ? 1.0F : -1.0F) * 0.38F * seamSpread0;
                    float zig1 = ((i + 1 + f) % 2 == 0 ? 1.0F : -1.0F) * 0.38F * seamSpread1;
                    Vec3 fp0 = p0.add(right.scale(side * seamSpread0 + zig0)).add(up.scale(-0.22D * seamSpread0));
                    Vec3 fp1 = p1.add(right.scale(side * seamSpread1 + zig1)).add(up.scale(-0.22D * seamSpread1));
                    renderLocalSegment(mat, vc, fp0, fp1, 0.034F * scale * (1.0F - t0 * 0.50F),
                            0.467F, 0.196F, 0.078F, 0.92F * (1.0F - t0));
                    renderLocalSegment(mat, vc, fp0, fp1, 0.016F * scale * (1.0F - t0 * 0.50F),
                            1.00F, 0.76F, 0.47F, 0.96F * (1.0F - t0));
                }
            } else if (archetype == SpellParticleHelper.ARCHETYPE_GLACIAL_SHARD) {
                // Section 6.6 Glacial Ice-Shard: twin spiraling crystalline hoarfrost & permafrost-blue (#2C82C9 / #98DCF2) rime wakes!
                float frostR0 = (0.19F * scale) * (1.0F - t0 * 0.62F);
                float frostR1 = (0.19F * scale) * (1.0F - t1 * 0.62F);
                float phase0 = -ageTicks * 0.46F + t0 * 11.5F;
                float phase1 = -ageTicks * 0.46F + t1 * 11.5F;
                for (int f = 0; f < 2; f++) {
                    float off = f * (float) Math.PI;
                    Vec3 fp0 = p0.add(right.scale(Mth.cos(phase0 + off) * frostR0))
                            .add(up.scale(Mth.sin(phase0 + off) * frostR0));
                    Vec3 fp1 = p1.add(right.scale(Mth.cos(phase1 + off) * frostR1))
                            .add(up.scale(Mth.sin(phase1 + off) * frostR1));
                    float cr = (f == 0) ? 0.92F : 0.173F;
                    float cg = (f == 0) ? 0.98F : 0.510F;
                    float cb = (f == 0) ? 1.00F : 0.788F;
                    renderLocalSegment(mat, vc, fp0, fp1, 0.028F * scale * (1.0F - t0 * 0.58F),
                            cr, cg, cb, 0.88F * (1.0F - t0));
                }
            } else if (archetype == SpellParticleHelper.ARCHETYPE_NETHERFIRE_COMET) {
                // Section 6.7 Netherfire Comet: roaring braided hellfire flame tongues (#B83C08 & #FFD15C) & molten slag wake!
                float blazeR0 = (0.24F * scale) * (1.0F - t0 * 0.45F);
                float blazeR1 = (0.24F * scale) * (1.0F - t1 * 0.45F);
                float phase0 = -ageTicks * 0.52F + t0 * 9.5F;
                float phase1 = -ageTicks * 0.52F + t1 * 9.5F;
                for (int f = 0; f < 3; f++) {
                    float off = (float) (Math.PI * 2.0D * f / 3.0D);
                    Vec3 fp0 = p0.add(right.scale(Mth.cos(phase0 + off) * blazeR0))
                            .add(up.scale(Mth.sin(phase0 + off) * blazeR0));
                    Vec3 fp1 = p1.add(right.scale(Mth.cos(phase1 + off) * blazeR1))
                            .add(up.scale(Mth.sin(phase1 + off) * blazeR1));
                    boolean goldCore = (f == 0);
                    float cr = goldCore ? 1.00F : 0.722F;
                    float cg = goldCore ? 0.820F : 0.235F;
                    float cb = goldCore ? 0.361F : 0.031F;
                    renderLocalSegment(mat, vc, fp0, fp1, (goldCore ? 0.034F : 0.044F) * scale * (1.0F - t0 * 0.50F),
                            cr, cg, cb, 0.92F * (1.0F - t0));
                }
            } else if (archetype == SpellParticleHelper.ARCHETYPE_DAWN_STAR) {
                // Section 6.8 Homing Dawn Star: twin luminous solar-gold (#FFE278) & incandescent ivory-white (#FFFDF0) starlight corona wakes!
                float sunR0 = (0.21F * scale) * (1.0F - t0 * 0.58F);
                float sunR1 = (0.21F * scale) * (1.0F - t1 * 0.58F);
                float phase0 = -ageTicks * 0.44F + t0 * 10.0F;
                float phase1 = -ageTicks * 0.44F + t1 * 10.0F;
                for (int s = 0; s < 2; s++) {
                    float off = s * (float) Math.PI;
                    Vec3 sp0 = p0.add(right.scale(Mth.cos(phase0 + off) * sunR0))
                            .add(up.scale(Mth.sin(phase0 + off) * sunR0));
                    Vec3 sp1 = p1.add(right.scale(Mth.cos(phase1 + off) * sunR1))
                            .add(up.scale(Mth.sin(phase1 + off) * sunR1));
                    boolean ivory = (s == 0);
                    float cr = 1.00F;
                    float cg = ivory ? 0.992F : 0.886F;
                    float cb = ivory ? 0.941F : 0.471F;
                    renderLocalSegment(mat, vc, sp0, sp1, (ivory ? 0.028F : 0.036F) * scale * (1.0F - t0 * 0.52F),
                            cr, cg, cb, 0.92F * (1.0F - t0));
                }
            } else if (archetype == SpellParticleHelper.ARCHETYPE_DUST_DEVIL) {
                // Section 6.12 Dust Devil Missile: twin swirling #C2B28F dune sandstone & #F5E4BE desert ivory vortex trails!
                float funnelR0 = (0.24F * scale) * (1.0F - t0 * 0.50F);
                float funnelR1 = (0.24F * scale) * (1.0F - t1 * 0.50F);
                float phase0 = -ageTicks * 0.62F + t0 * 12.5F;
                float phase1 = -ageTicks * 0.62F + t1 * 12.5F;
                for (int d = 0; d < 2; d++) {
                    float off = d * (float) Math.PI;
                    Vec3 dp0 = p0.add(right.scale(Mth.cos(phase0 + off) * funnelR0))
                            .add(up.scale(Mth.sin(phase0 + off) * funnelR0));
                    Vec3 dp1 = p1.add(right.scale(Mth.cos(phase1 + off) * funnelR1))
                            .add(up.scale(Mth.sin(phase1 + off) * funnelR1));
                    boolean sunlit = (d == 0);
                    float cr = sunlit ? 0.961F : 0.761F;
                    float cg = sunlit ? 0.894F : 0.698F;
                    float cb = sunlit ? 0.745F : 0.561F;
                    renderLocalSegment(mat, vc, dp0, dp1, (sunlit ? 0.032F : 0.044F) * scale * (1.0F - t0 * 0.45F),
                            cr, cg, cb, 0.90F * (1.0F - t0));
                }
            } else if (archetype == SpellParticleHelper.ARCHETYPE_BALL_LIGHTNING) {
                // Section 6.13 Plasma Bolt (Ball Lightning): jagged crackling #FFE600 & #FFFDF0 electric plasma arcs!
                int frame = (int) (ageTicks * 1.35F);
                float j0 = Mth.sin((i + frame) * 2.9F) * 0.16F * scale * (1.0F - t0 * 0.45F);
                float j1 = Mth.sin((i + 1 + frame) * 2.9F) * 0.16F * scale * (1.0F - t1 * 0.45F);
                float k0 = Mth.cos((i + frame) * 3.7F) * 0.16F * scale * (1.0F - t0 * 0.45F);
                float k1 = Mth.cos((i + 1 + frame) * 3.7F) * 0.16F * scale * (1.0F - t1 * 0.45F);
                Vec3 zp0 = p0.add(right.scale(j0)).add(up.scale(k0));
                Vec3 zp1 = p1.add(right.scale(j1)).add(up.scale(k1));
                renderLocalSegment(mat, vc, zp0, zp1, 0.048F * scale * (1.0F - t0 * 0.40F),
                        1.00F, 0.902F, 0.00F, 0.90F * (1.0F - t0));
                renderLocalSegment(mat, vc, zp0, zp1, 0.022F * scale * (1.0F - t0 * 0.40F),
                        1.00F, 1.00F, 0.96F, 0.98F * (1.0F - t0));
            } else if (archetype == SpellParticleHelper.ARCHETYPE_MOLTEN_METEOR) {
                // Section 6.14 Molten Magma Boulder: viscous #B83C08 liquid lava wake & tumbling dark pumice slag droplets!
                float sag0 = t0 * t0 * 0.14F * scale;
                float sag1 = t1 * t1 * 0.14F * scale;
                Vec3 mp0 = p0.subtract(up.scale(sag0));
                Vec3 mp1 = p1.subtract(up.scale(sag1));
                renderLocalSegment(mat, vc, mp0, mp1, 0.062F * scale * (1.0F - t0 * 0.55F),
                        0.722F, 0.235F, 0.031F, 0.88F * (1.0F - t0));
                renderLocalSegment(mat, vc, mp0, mp1, 0.028F * scale * (1.0F - t0 * 0.55F),
                        1.00F, 0.820F, 0.361F, 0.96F * (1.0F - t0));
            } else if (archetype == SpellParticleHelper.ARCHETYPE_SQUALL_VORTEX) {
                // Section 6.15 Squall Tempest Slug: twin swirling #429BA3 tempest-teal rain & #D8FAFF gale-wind slipstream spirals!
                float sr0 = (0.24F * scale) * (1.0F - t0 * 0.55F);
                float sr1 = (0.24F * scale) * (1.0F - t1 * 0.55F);
                float ph0 = -ageTicks * 0.78F + t0 * 13.5F;
                float ph1 = -ageTicks * 0.78F + t1 * 13.5F;
                for (int h = 0; h < 2; h++) {
                    float off = h * (float) Math.PI;
                    Vec3 sp0 = p0.add(right.scale(Mth.cos(ph0 + off) * sr0)).add(up.scale(Mth.sin(ph0 + off) * sr0));
                    Vec3 sp1 = p1.add(right.scale(Mth.cos(ph1 + off) * sr1)).add(up.scale(Mth.sin(ph1 + off) * sr1));
                    boolean foam = (h == 1);
                    renderLocalSegment(mat, vc, sp0, sp1, (foam ? 0.030F : 0.045F) * scale * (1.0F - t0 * 0.45F),
                            foam ? 0.847F : 0.259F,
                            foam ? 0.980F : 0.608F,
                            foam ? 1.000F : 0.639F,
                            0.92F * (1.0F - t0));
                }
            } else if (archetype == SpellParticleHelper.ARCHETYPE_GLACIAL_ICE_LANCE) {
                // Section 6.16 Glacial Ice-Lance: heavy blue-ice (#2C82C9) & pale cryo-blue (#98DCF2) piercing wake!
                float gr0 = (0.21F * scale) * (1.0F - t0 * 0.60F);
                float gr1 = (0.21F * scale) * (1.0F - t1 * 0.60F);
                float gph0 = -ageTicks * 0.48F + t0 * 10.5F;
                float gph1 = -ageTicks * 0.48F + t1 * 10.5F;
                for (int h = 0; h < 2; h++) {
                    float off = h * (float) Math.PI;
                    Vec3 gp0 = p0.add(right.scale(Mth.cos(gph0 + off) * gr0)).add(up.scale(Mth.sin(gph0 + off) * gr0));
                    Vec3 gp1 = p1.add(right.scale(Mth.cos(gph1 + off) * gr1)).add(up.scale(Mth.sin(gph1 + off) * gr1));
                    boolean pale = (h == 0);
                    renderLocalSegment(mat, vc, gp0, gp1, (pale ? 0.040F : 0.032F) * scale * (1.0F - t0 * 0.48F),
                            pale ? 0.596F : 0.173F,
                            pale ? 0.863F : 0.510F,
                            pale ? 0.949F : 0.788F,
                            0.90F * (1.0F - t0));
                }
            } else if (archetype == SpellParticleHelper.ARCHETYPE_OVERGROWTH_BRIAR_POD) {
                // Section 6.17 Verdant Briar-Spike Seed: twining #1A5923 jungle vine & #773214 earthen root trail!
                float br0 = (0.20F * scale) * (1.0F - t0 * 0.55F);
                float br1 = (0.20F * scale) * (1.0F - t1 * 0.55F);
                float bph0 = ageTicks * 0.52F + t0 * 11.0F;
                float bph1 = ageTicks * 0.52F + t1 * 11.0F;
                for (int h = 0; h < 2; h++) {
                    float off = h * (float) Math.PI;
                    Vec3 bp0 = p0.add(right.scale(Mth.cos(bph0 + off) * br0)).add(up.scale(Mth.sin(bph0 + off) * br0));
                    Vec3 bp1 = p1.add(right.scale(Mth.cos(bph1 + off) * br1)).add(up.scale(Mth.sin(bph1 + off) * br1));
                    boolean vine = (h == 0);
                    renderLocalSegment(mat, vc, bp0, bp1, (vine ? 0.042F : 0.034F) * scale * (1.0F - t0 * 0.48F),
                            vine ? 0.173F : 0.467F,
                            vine ? 0.902F : 0.196F,
                            vine ? 0.408F : 0.078F,
                            0.90F * (1.0F - t0));
                }
            }
        }
    }

    // =========================================================================
    // ARCHETYPE 8: AERODYNAMIC WIND SPIRAL (MAJESTIC VALKYRIE GALE-LANCE)
    // =========================================================================

    private static void renderWindSpiral(
            Matrix4f mat, VertexConsumer vc, float scale, float age,
            float pr, float pg, float pb, float ar, float ag, float ab
    ) {
        float spin = age * 0.58F;
        // 1. Gleaming ivory-white supersonic needle core + golden-buff compressed wind spearhead
        renderElongatedBipyramid(mat, vc, 0.082F * scale, 0.74F * scale, 0.52F * scale, 6, spin,
                1.0F, 1.0F, 1.0F, 0.97F);
        renderElongatedBipyramid(mat, vc, 0.155F * scale, 0.84F * scale, 0.58F * scale, 6, -spin * 0.65F,
                pr, pg, pb, 0.76F);

        // 2. Four swept Valkyrie wind-blade fins curving gracefully backward from nose to tail
        for (int fin = 0; fin < 4; fin++) {
            float finAng = spin * 0.85F + (float) (Math.PI * 0.5D * fin);
            Vec3 radial = new Vec3(Mth.cos(finAng), Mth.sin(finAng), 0.0D);
            Vec3 rootFront = radial.scale(0.08D * scale).add(0.0D, 0.0D, 0.42D * scale);
            Vec3 tipOuter = radial.scale(0.42D * scale).add(0.0D, 0.0D, -0.08D * scale);
            Vec3 tipSweep = radial.scale(0.34D * scale).add(0.0D, 0.0D, -0.46D * scale);
            Vec3 rootBack = radial.scale(0.06D * scale).add(0.0D, 0.0D, -0.32D * scale);

            boolean brightFin = (fin % 2 == 0);
            emitDoubleSidedQuad(mat, vc, rootFront, tipOuter, tipSweep, rootBack,
                    brightFin ? ar : pr, brightFin ? ag : pg, brightFin ? ab : pb, 0.82F);
            renderLocalSegment(mat, vc, rootFront, tipOuter, 0.018F * scale, 1.0F, 1.0F, 1.0F, 0.94F);
        }

        // 3. Twin conical supersonic Prandtl-Glauert Mach vapor cones around nose & mid-body
        renderLocalConicalSkirt(mat, vc, 0.46F * scale, 0.06F * scale, 0.08F * scale, 0.34F * scale,
                20, spin, ar, ag, ab, 0.56F, 0.08F);
        renderLocalConicalSkirt(mat, vc, 0.10F * scale, -0.34F * scale, 0.14F * scale, 0.46F * scale,
                22, -spin * 0.8F, pr, pg, pb, 0.46F, 0.05F);

        // 4. Quadruple counter-spiraling corkscrew slipstream ribbons coiling around the flight axis
        int steps = 16;
        for (int strand = 0; strand < 4; strand++) {
            float dirSign = (strand < 2) ? 1.0F : -1.0F;
            float phaseBase = dirSign * spin * 1.25F + (float) (Math.PI * (strand % 2));
            boolean accentStrand = (strand % 2 == 0);
            float sr = accentStrand ? ar : pr;
            float sg = accentStrand ? ag : pg;
            float sb = accentStrand ? ab : pb;

            for (int s = 0; s < steps; s++) {
                float t0 = s / (float) steps;
                float t1 = (s + 1) / (float) steps;
                double z0 = (0.58D - t0 * 1.38D) * scale;
                double z1 = (0.58D - t1 * 1.38D) * scale;
                float r0 = (0.11F + 0.28F * Mth.sin(t0 * (float) Math.PI)) * scale;
                float r1 = (0.11F + 0.28F * Mth.sin(t1 * (float) Math.PI)) * scale;
                float a0 = phaseBase - dirSign * t0 * 11.5F;
                float a1 = phaseBase - dirSign * t1 * 11.5F;

                Vec3 p0 = new Vec3(Mth.cos(a0) * r0, Mth.sin(a0) * r0, z0);
                Vec3 p1 = new Vec3(Mth.cos(a1) * r1, Mth.sin(a1) * r1, z1);
                renderLocalSegment(mat, vc, p0, p1, 0.035F * scale * (1.0F - t0 * 0.45F), sr, sg, sb, 0.88F * (1.0F - t0 * 0.50F));
            }
        }

        // 5. Counter-spinning aerodynamic deflection halos ahead of nose & mid-body
        Vec3 rx = new Vec3(Mth.cos(spin), Mth.sin(spin), 0.0D);
        Vec3 ry = new Vec3(-Mth.sin(spin), Mth.cos(spin), 0.0D);
        renderLocalRing(mat, vc, new Vec3(0.0D, 0.0D, 0.22D * scale), rx, ry,
                0.24F * scale, 0.32F * scale, 20, ar, ag, ab, 0.88F);
        renderLocalRing(mat, vc, new Vec3(0.0D, 0.0D, -0.20D * scale), ry, rx,
                0.32F * scale, 0.40F * scale, 20, pr, pg, pb, 0.72F);
    }

    // =========================================================================
    // ARCHETYPE 9: FLYING 3D CRESCENT WIND-BLADE SLASH (AIR HARM PROJECTILE)
    // =========================================================================

    /**
     * Flying 3D form of {@code SpellWorldVfxRenderer.renderCrescentSlash} (used by Air {@code TOUCH_STRIKE}):
     * renders triple intersecting razor-sharp 168° swept Crescent Wind-Blades slicing forward along {@code +Z}
     * with golden-buff trailing concavity, radiant silver-ivory bevels, and blazing white-hot cutting rims!
     */
    private static void renderFlyingWindSlash(
            Matrix4f mat, VertexConsumer vc, float scale, float age, int seed,
            float pr, float pg, float pb, float ar, float ag, float ab
    ) {
        Vec3 fwd = new Vec3(0.0D, 0.0D, 1.0D);
        Vec3 flatRight = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatUp = new Vec3(0.0D, 1.0D, 0.0D);

        float flutter = Mth.sin(age * 0.45F + seed) * 5.0F;
        float breathe = 1.0F + 0.06F * Mth.sin(age * 0.65F);

        for (int layer = 0; layer < 3; layer++) {
            float baseTiltDeg = (seed % 2 == 0) ? 24.0F : -24.0F;
            if (layer == 1) {
                baseTiltDeg = -baseTiltDeg * 1.15F - flutter * 0.8F;
            } else if (layer == 2) {
                baseTiltDeg = 6.0F + flutter * 0.5F;
            } else {
                baseTiltDeg += flutter;
            }

            float tilt = (float) Math.toRadians(baseTiltDeg);
            Vec3 planeRight = flatRight.scale(Mth.cos(tilt)).add(flatUp.scale(Mth.sin(tilt))).normalize();
            Vec3 planeNormal = fwd.cross(planeRight).normalize();

            float fade = (layer == 0) ? 1.0F : 0.82F;
            float scaleLayer = (layer == 0) ? 1.0F : (layer == 1 ? 0.88F : 1.14F);
            float reach = 0.96F * scale * scaleLayer * breathe;
            int segs = 22;
            float totalArc = (float) Math.toRadians(168.0D);
            float sweepShift = Mth.sin(age * 0.35F + layer * 1.7F) * 0.08F;

            // Offset arc center slightly aft so the convex cutting edge sits right at the projectile nose (+0.45*scale)
            Vec3 origin = new Vec3(0.0D, 0.0D, -0.44D * scale)
                    .add(planeNormal.scale((layer - 1) * 0.085D * scale));

            for (int i = 0; i < segs; i++) {
                float t0 = (float) i / segs;
                float t1 = (float) (i + 1) / segs;
                float a0 = (t0 - 0.5F) * totalArc + sweepShift;
                float a1 = (t1 - 0.5F) * totalArc + sweepShift;

                float prof0 = Mth.sin(t0 * (float) Math.PI);
                float prof1 = Mth.sin(t1 * (float) Math.PI);

                Vec3 dir0 = fwd.scale(Mth.cos(a0)).add(planeRight.scale(Mth.sin(a0))).normalize();
                Vec3 dir1 = fwd.scale(Mth.cos(a1)).add(planeRight.scale(Mth.sin(a1))).normalize();

                float inner0 = reach * (1.0F - 0.44F * prof0);
                float inner1 = reach * (1.0F - 0.44F * prof1);

                Vec3 pIn0 = origin.add(dir0.scale(inner0));
                Vec3 pOut0 = origin.add(dir0.scale(reach));
                Vec3 pOut1 = origin.add(dir1.scale(reach));
                Vec3 pIn1 = origin.add(dir1.scale(inner1));

                // Upper & lower 3D beveled crescent blade faces meeting at the razor cutting edge (pOut0..pOut1)
                double halfThick0 = 0.028D * scale * prof0;
                double halfThick1 = 0.028D * scale * prof1;
                Vec3 nOff0 = planeNormal.scale(halfThick0);
                Vec3 nOff1 = planeNormal.scale(halfThick1);

                emitPerVertexDoubleSidedQuad(mat, vc,
                        pIn0.add(nOff0), pOut0, pOut1, pIn1.add(nOff1),
                        pr, pg, pb, 0.22F * fade * prof0,
                        ar, ag, ab, 0.92F * fade * prof0,
                        ar, ag, ab, 0.92F * fade * prof1,
                        pr, pg, pb, 0.22F * fade * prof1);

                emitPerVertexDoubleSidedQuad(mat, vc,
                        pIn0.subtract(nOff0), pOut0, pOut1, pIn1.subtract(nOff1),
                        pr, pg, pb, 0.18F * fade * prof0,
                        ar, ag, ab, 0.88F * fade * prof0,
                        ar, ag, ab, 0.88F * fade * prof1,
                        pr, pg, pb, 0.18F * fade * prof1);

                // Blazing white-hot razor cutting rim (identical to Touch/Strike renderCrescentSlash!)
                Vec3 pRim0 = origin.add(dir0.scale(reach * (1.0F - 0.09F * prof0)));
                Vec3 pRim1 = origin.add(dir1.scale(reach * (1.0F - 0.09F * prof1)));
                emitPerVertexDoubleSidedQuad(mat, vc,
                        pRim0, pOut0, pOut1, pRim1,
                        1.0F, 1.0F, 1.0F, 0.88F * fade * prof0,
                        1.0F, 1.0F, 1.0F, 0.99F * fade * prof0,
                        1.0F, 1.0F, 1.0F, 0.99F * fade * prof1,
                        1.0F, 1.0F, 1.0F, 0.88F * fade * prof1);
            }
        }
    }

    // =========================================================================
    // ARCHETYPE 10: FLUID-DENSE 3D HYDRO-CANNON ORB & VISCOUS FLUID MASS (#2 WATER)
    // =========================================================================

    private static void renderHydroOrb(
            Matrix4f mat, VertexConsumer vc, float scale, float age, int seed, boolean heavyFluid,
            float pr, float pg, float pb, float ar, float ag, float ab
    ) {
        float slosh = age * (heavyFluid ? 0.28F : 0.42F) + seed * 0.7F;
        float zStretch = heavyFluid ? 1.10F : 1.26F;

        // 1. Gleaming pressurized white-seafoam hydrostatic core
        renderUndulatingFluidSphere(mat, vc, Vec3.ZERO, 0.125F * scale, 1.08F, 6, 10, slosh * 1.3F, 0.04F,
                0.90F, 0.99F, 1.00F, 0.95F);

        // 2. Dense deep-azure (#225C7C) pressurized ocean fluid body
        renderUndulatingFluidSphere(mat, vc, Vec3.ZERO, 0.225F * scale, zStretch, 8, 14, slosh, heavyFluid ? 0.11F : 0.075F,
                0.133F, 0.361F, 0.486F, 0.86F);

        // 3. Translucent cerulean surface-tension meniscus shell undulating in counter-phase
        renderUndulatingFluidSphere(mat, vc, new Vec3(0.0D, 0.0D, -0.02D * scale), 0.295F * scale, zStretch * 1.05F,
                8, 14, -slosh * 0.85F, heavyFluid ? 0.13F : 0.09F,
                pr, pg, pb, 0.52F);

        // 4. Foaming whitewater bow-wave cap parting around the nose (+Z)
        float bowFront = 0.31F * scale * zStretch;
        renderLocalConicalSkirt(mat, vc, bowFront, -0.04F * scale, 0.07F * scale, 0.33F * scale,
                18, slosh * 0.6F, 0.88F, 0.98F, 1.00F, 0.74F, 0.10F);

        // 5. Twin counter-rotating internal whirlpool current bands
        Vec3 ux1 = new Vec3(Mth.cos(slosh), Mth.sin(slosh) * 0.42D, 0.18D).normalize();
        Vec3 uz1 = new Vec3(-Mth.sin(slosh) * 0.35D, Mth.cos(slosh), 0.35D).normalize();
        renderLocalRing(mat, vc, Vec3.ZERO, ux1, uz1, 0.24F * scale, 0.32F * scale, 18, ar, ag, ab, 0.78F);

        Vec3 ux2 = new Vec3(Mth.cos(-slosh * 0.8F), 0.28D, Mth.sin(-slosh * 0.8F)).normalize();
        Vec3 uz2 = new Vec3(-Mth.sin(-slosh * 0.8F), 0.28D, Mth.cos(-slosh * 0.8F)).normalize();
        renderLocalRing(mat, vc, Vec3.ZERO, ux2, uz2, 0.26F * scale, 0.34F * scale, 18, 0.86F, 0.98F, 1.00F, 0.64F);

        // 6. Trailing & sloshing 3D satellite water droplets (extra droplets for Viscous Fluid Mass!)
        int dropletCount = heavyFluid ? 5 : 3;
        for (int d = 0; d < dropletCount; d++) {
            float dPhase = slosh * 0.95F + d * ((float) (Math.PI * 2.0D) / dropletCount);
            float orbitR = (heavyFluid ? 0.28F : 0.20F) * scale * (0.75F + 0.25F * Mth.sin(age * 0.3F + d));
            double dz = (-0.28D - 0.16D * d + 0.06D * Mth.sin(dPhase * 1.4F)) * scale;
            Vec3 dropCenter = new Vec3(Mth.cos(dPhase) * orbitR, Mth.sin(dPhase) * orbitR, dz);
            float dropRad = (0.065F - d * 0.007F) * scale;
            renderUndulatingFluidSphere(mat, vc, dropCenter, Math.max(0.03F * scale, dropRad), 1.35F,
                    4, 8, dPhase, 0.04F,
                    (d % 2 == 0) ? pr : 0.86F, (d % 2 == 0) ? pg : 0.98F, 1.00F, 0.78F);
        }
    }

    // =========================================================================
    // ARCHETYPE 11: THORNED BRIAR SEED (SPIKED BOTANICAL SEED POD & VINES)
    // =========================================================================

    private static void renderBriarSeed(
            Matrix4f mat, VertexConsumer vc, float scale, float age, int entitySeed,
            float pr, float pg, float pb, float ar, float ag, float ab
    ) {
        float spin = age * 0.34F + (entitySeed & 31) * 0.17F;
        float pulse = 0.94F + 0.06F * Mth.sin(age * 0.40F);

        // 1. Glowing sprout-lime botanical embryo core (#B4FF78)
        renderElongatedBipyramid(mat, vc, 0.125F * scale * pulse, 0.34F * scale, 0.24F * scale, 6, spin,
                0.71F, 1.00F, 0.47F, 0.96F);

        // 2. Ribbed forest-green (#1A5923) & vibrant emerald (#2CE668) botanical seed pod husk
        renderElongatedBipyramid(mat, vc, 0.235F * scale * pulse, 0.48F * scale, 0.34F * scale, 8, -spin * 0.55F,
                0.10F, 0.35F, 0.14F, 0.88F);
        renderElongatedBipyramid(mat, vc, 0.205F * scale, 0.52F * scale, 0.30F * scale, 6, spin * 0.40F,
                0.17F, 0.90F, 0.41F, 0.62F);

        // 3. Four swept emerald calyx sepal leaves cupping the rear of the seed pod
        for (int s = 0; s < 4; s++) {
            float sAng = -spin * 0.65F + (float) (Math.PI * 0.5D * s);
            Vec3 rad = new Vec3(Mth.cos(sAng), Mth.sin(sAng), 0.0D);
            Vec3 tang = new Vec3(-Mth.sin(sAng), Mth.cos(sAng), 0.0D);
            Vec3 base = rad.scale(0.08D * scale).add(0.0D, 0.0D, -0.14D * scale);
            Vec3 left = rad.scale(0.24D * scale).add(tang.scale(0.085D * scale)).add(0.0D, 0.0D, -0.26D * scale);
            Vec3 tip = rad.scale(0.32D * scale).add(0.0D, 0.0D, -0.44D * scale);
            Vec3 right = rad.scale(0.24D * scale).subtract(tang.scale(0.085D * scale)).add(0.0D, 0.0D, -0.26D * scale);
            emitGradientDoubleSidedQuad(mat, vc, base, left, tip, right,
                    0.10F, 0.35F, 0.14F, 0.92F,
                    0.17F, 0.90F, 0.41F, 0.80F);
        }

        // 4. Eight protruding 3D bark-to-lime razor thorn barbs (#5C3A1E base -> #B4FF78 razor tip)
        for (int t = 0; t < 8; t++) {
            boolean frontTier = (t < 4);
            float tierZ = (frontTier ? 0.14F : -0.06F) * scale;
            float tiltZ = (frontTier ? 0.18F : -0.14F) * scale;
            float tAng = spin + (frontTier ? 0.0F : (float) (Math.PI * 0.25D))
                    + (float) (Math.PI * 0.5D * (t % 4));
            Vec3 rad = new Vec3(Mth.cos(tAng), Mth.sin(tAng), 0.0D);
            Vec3 tang = new Vec3(-Mth.sin(tAng), Mth.cos(tAng), 0.0D);

            Vec3 rootCenter = rad.scale(0.17D * scale).add(0.0D, 0.0D, tierZ);
            Vec3 barbTip = rad.scale(0.46D * scale).add(0.0D, 0.0D, tierZ + tiltZ);
            double w = 0.048D * scale;
            Vec3 b0 = rootCenter.add(tang.scale(w));
            Vec3 b1 = rootCenter.add(0.0D, 0.0D, w);
            Vec3 b2 = rootCenter.subtract(tang.scale(w));
            Vec3 b3 = rootCenter.subtract(0.0D, 0.0D, w);

            emitGradientDoubleSidedQuad(mat, vc, b0, b1, barbTip, barbTip,
                    0.36F, 0.23F, 0.12F, 0.95F, 0.71F, 1.00F, 0.47F, 0.96F);
            emitGradientDoubleSidedQuad(mat, vc, b1, b2, barbTip, barbTip,
                    0.36F, 0.23F, 0.12F, 0.95F, 0.71F, 1.00F, 0.47F, 0.96F);
            emitGradientDoubleSidedQuad(mat, vc, b2, b3, barbTip, barbTip,
                    0.36F, 0.23F, 0.12F, 0.95F, 0.71F, 1.00F, 0.47F, 0.96F);
            emitGradientDoubleSidedQuad(mat, vc, b3, b0, barbTip, barbTip,
                    0.36F, 0.23F, 0.12F, 0.95F, 0.71F, 1.00F, 0.47F, 0.96F);
        }

        // 5. Nose puncture thorn spearpoint
        Vec3 noseBaseL = new Vec3(-0.045D * scale, 0.0D, 0.42D * scale);
        Vec3 noseBaseR = new Vec3(0.045D * scale, 0.0D, 0.42D * scale);
        Vec3 noseBaseU = new Vec3(0.0D, 0.045D * scale, 0.42D * scale);
        Vec3 noseBaseD = new Vec3(0.0D, -0.045D * scale, 0.42D * scale);
        Vec3 noseTip = new Vec3(0.0D, 0.0D, 0.72D * scale);
        emitGradientDoubleSidedQuad(mat, vc, noseBaseL, noseBaseR, noseTip, noseTip,
                0.17F, 0.90F, 0.41F, 0.94F, 0.85F, 1.00F, 0.62F, 0.98F);
        emitGradientDoubleSidedQuad(mat, vc, noseBaseU, noseBaseD, noseTip, noseTip,
                0.17F, 0.90F, 0.41F, 0.94F, 0.85F, 1.00F, 0.62F, 0.98F);

        // 6. Two spiraling thorny vine tendrils trailing behind the seed
        int vineSegs = 10;
        for (int v = 0; v < 2; v++) {
            float vOff = v * (float) Math.PI;
            for (int s = 0; s < vineSegs; s++) {
                float t0 = (float) s / vineSegs;
                float t1 = (float) (s + 1) / vineSegs;
                float a0 = -spin * 1.4F + vOff + t0 * 5.2F;
                float a1 = -spin * 1.4F + vOff + t1 * 5.2F;
                float r0 = (0.10F + 0.14F * t0) * scale * (1.0F - t0 * 0.35F);
                float r1 = (0.10F + 0.14F * t1) * scale * (1.0F - t1 * 0.35F);
                Vec3 p0 = new Vec3(Mth.cos(a0) * r0, Mth.sin(a0) * r0, (-0.22D - 0.72D * t0) * scale);
                Vec3 p1 = new Vec3(Mth.cos(a1) * r1, Mth.sin(a1) * r1, (-0.22D - 0.72D * t1) * scale);
                renderLocalSegment(mat, vc, p0, p1, 0.022F * scale * (1.0F - t0 * 0.55F),
                        (v == 0) ? 0.10F : 0.17F, (v == 0) ? 0.35F : 0.90F, (v == 0) ? 0.14F : 0.41F,
                        0.88F * (1.0F - t0 * 0.70F));
            }
        }
    }

    private static void renderUndulatingFluidSphere(
            Matrix4f mat, VertexConsumer vc,
            Vec3 center, float radius, float zStretch,
            int latBands, int lonSegs, float wavePhase, float waveAmp,
            float r, float g, float b, float a
    ) {
        for (int lat = 0; lat < latBands; lat++) {
            float v0 = (float) lat / latBands;
            float v1 = (float) (lat + 1) / latBands;
            float phi0 = (v0 - 0.5F) * (float) Math.PI;
            float phi1 = (v1 - 0.5F) * (float) Math.PI;

            float cp0 = Mth.cos(phi0);
            float sp0 = Mth.sin(phi0);
            float cp1 = Mth.cos(phi1);
            float sp1 = Mth.sin(phi1);

            for (int lon = 0; lon < lonSegs; lon++) {
                float th0 = (float) (Math.PI * 2.0D * lon / lonSegs);
                float th1 = (float) (Math.PI * 2.0D * (lon + 1) / lonSegs);

                float w00 = 1.0F + waveAmp * Mth.sin(th0 * 2.0F + phi0 * 3.0F + wavePhase);
                float w10 = 1.0F + waveAmp * Mth.sin(th1 * 2.0F + phi0 * 3.0F + wavePhase);
                float w11 = 1.0F + waveAmp * Mth.sin(th1 * 2.0F + phi1 * 3.0F + wavePhase);
                float w01 = 1.0F + waveAmp * Mth.sin(th0 * 2.0F + phi1 * 3.0F + wavePhase);

                Vec3 p00 = center.add(cp0 * Mth.cos(th0) * radius * w00, cp0 * Mth.sin(th0) * radius * w00, sp0 * radius * zStretch * w00);
                Vec3 p10 = center.add(cp0 * Mth.cos(th1) * radius * w10, cp0 * Mth.sin(th1) * radius * w10, sp0 * radius * zStretch * w10);
                Vec3 p11 = center.add(cp1 * Mth.cos(th1) * radius * w11, cp1 * Mth.sin(th1) * radius * w11, sp1 * radius * zStretch * w11);
                Vec3 p01 = center.add(cp1 * Mth.cos(th0) * radius * w01, cp1 * Mth.sin(th0) * radius * w01, sp1 * radius * zStretch * w01);

                emitDoubleSidedQuad(mat, vc, p00, p10, p11, p01, r, g, b, a);
            }
        }
    }

    private static void renderLocalConicalSkirt(
            Matrix4f mat, VertexConsumer vc,
            float zFront, float zBack, float rFront, float rBack,
            int segs, float rotOffset,
            float r, float g, float b, float aFront, float aBack
    ) {
        for (int i = 0; i < segs; i++) {
            float a0 = rotOffset + (float) (Math.PI * 2.0D * i / segs);
            float a1 = rotOffset + (float) (Math.PI * 2.0D * (i + 1) / segs);
            Vec3 f0 = new Vec3(Mth.cos(a0) * rFront, Mth.sin(a0) * rFront, zFront);
            Vec3 f1 = new Vec3(Mth.cos(a1) * rFront, Mth.sin(a1) * rFront, zFront);
            Vec3 b1 = new Vec3(Mth.cos(a1) * rBack, Mth.sin(a1) * rBack, zBack);
            Vec3 b0 = new Vec3(Mth.cos(a0) * rBack, Mth.sin(a0) * rBack, zBack);
            emitGradientDoubleSidedQuad(mat, vc, f0, f1, b1, b0, r, g, b, aFront, r, g, b, aBack);
        }
    }

    // =========================================================================
    // ARCHETYPE 12: TECTONIC QUAKE SPIRE (CHISELED GRANITE & TERRACOTTA MONOLITH)
    // =========================================================================

    private static void renderQuakeSpire(
            Matrix4f mat, VertexConsumer vc, float scale, float age, int seed, boolean heavyTectonic,
            float pr, float pg, float pb, float ar, float ag, float ab
    ) {
        float s = heavyTectonic ? (scale * 1.32F) : scale;
        float spin = age * (heavyTectonic ? 0.14F : 0.24F) + (seed & 0xFF) * 0.03F;

        // 1. Glowing amber-quartz (#FFC278) seismic core crystal inside the fractured stone monolith
        renderElongatedBipyramid(mat, vc, 0.12F * s, 0.72F * s, 0.44F * s, 6, spin * 1.4F,
                1.00F, 0.80F, 0.50F, 0.96F);

        // 2. Heavy 6-sided chiseled granite (#5A524C) & burnt earthen terracotta (#773214) spearhead monolith
        Vec3 nose = new Vec3(0.0D, 0.0D, 0.86D * s);
        Vec3 tail = new Vec3(0.0D, 0.0D, -0.52D * s);
        float waistR = 0.24F * s;
        for (int i = 0; i < 6; i++) {
            float a0 = spin + (float) (Math.PI * 2.0D * i / 6.0D);
            float a1 = spin + (float) (Math.PI * 2.0D * (i + 1) / 6.0D);
            Vec3 w0 = new Vec3(Mth.cos(a0) * waistR, Mth.sin(a0) * waistR, -0.04D * s);
            Vec3 w1 = new Vec3(Mth.cos(a1) * waistR, Mth.sin(a1) * waistR, -0.04D * s);

            boolean graniteFace = (i % 2 == 0);
            float fr = graniteFace ? 0.353F : 0.467F;
            float fg = graniteFace ? 0.322F : 0.196F;
            float fb = graniteFace ? 0.298F : 0.078F;
            emitDoubleSidedQuad(mat, vc, nose, w0, w1, nose, fr, fg, fb, 0.94F);
            emitDoubleSidedQuad(mat, vc, tail, w1, w0, tail, pr * 0.85F, pg * 0.85F, pb * 0.85F, 0.90F);

            // Glowing amber fault-line seam along every alternate granite ridge!
            if (graniteFace) {
                renderLocalSegment(mat, vc, w0, nose, 0.017F * s, ar, ag, ab, 0.95F);
            }
        }

        // 3. Four jagged stalagmite stone buttress shards jutting outward & backward around the waist
        for (int sp = 0; sp < 4; sp++) {
            float spAng = -spin * 0.65F + (float) (Math.PI * 0.5D * sp);
            Vec3 rad = new Vec3(Mth.cos(spAng), Mth.sin(spAng), 0.0D);
            Vec3 tang = new Vec3(-Mth.sin(spAng), Mth.cos(spAng), 0.0D);
            Vec3 root = rad.scale(0.14D * s).add(0.0D, 0.0D, 0.12D * s);
            Vec3 tip = rad.scale(0.42D * s).add(0.0D, 0.0D, -0.34D * s);
            Vec3 sideA = root.add(tang.scale(0.075D * s));
            Vec3 sideB = root.subtract(tang.scale(0.075D * s));
            emitDoubleSidedQuad(mat, vc, sideA, sideB, tip, tip, 0.467F, 0.196F, 0.078F, 0.92F);
            renderLocalSegment(mat, vc, root, tip, 0.018F * s, 1.00F, 0.76F, 0.47F, 0.90F);
        }

        // 4. Octagonal seismic fault collar ring
        Vec3 rx = new Vec3(Mth.cos(spin), Mth.sin(spin), 0.0D);
        Vec3 ry = new Vec3(-Mth.sin(spin), Mth.cos(spin), 0.0D);
        renderLocalRing(mat, vc, new Vec3(0.0D, 0.0D, -0.06D * s), rx, ry,
                0.28F * s, 0.36F * s, 8, ar, ag, ab, 0.85F);

        // 5. Dense Tectonic Mass (Slow/Heavy Flow Mutation): 3 heavy chiseled boulders orbiting the spire!
        if (heavyTectonic) {
            for (int b = 0; b < 3; b++) {
                float bAng = -age * 0.22F + (float) (Math.PI * 2.0D * b / 3.0D);
                Vec3 bCenter = new Vec3(Mth.cos(bAng) * 0.52D * scale, Mth.sin(bAng) * 0.52D * scale, -0.10D * scale);
                Vec3 bx = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 by = new Vec3(0.0D, 1.0D, 0.0D);
                Vec3 bz = new Vec3(0.0D, 0.0D, 1.0D);
                float br = 0.14F * scale;
                for (int face = 0; face < 4; face++) {
                    float fa0 = bAng + (float) (Math.PI * 0.5D * face);
                    float fa1 = bAng + (float) (Math.PI * 0.5D * (face + 1));
                    Vec3 c0 = bCenter.add(bx.scale(Mth.cos(fa0) * br)).add(by.scale(Mth.sin(fa0) * br));
                    Vec3 c1 = bCenter.add(bx.scale(Mth.cos(fa1) * br)).add(by.scale(Mth.sin(fa1) * br));
                    Vec3 top = bCenter.add(bz.scale(br * 1.15D));
                    Vec3 bot = bCenter.subtract(bz.scale(br * 1.15D));
                    emitDoubleSidedQuad(mat, vc, top, c0, c1, top, 0.353F, 0.322F, 0.298F, 0.94F);
                    emitDoubleSidedQuad(mat, vc, bot, c1, c0, bot, 0.467F, 0.196F, 0.078F, 0.92F);
                }
            }
        }
    }

    // =========================================================================
    // ARCHETYPE 13: GLACIAL ICE-SHARD (CRYSTALLINE FROST NEEDLE & RIME PLATES)
    // =========================================================================

    private static void renderGlacialShard(
            Matrix4f mat, VertexConsumer vc, float scale, float age, int seed, boolean heavyPermafrost,
            float pr, float pg, float pb, float ar, float ag, float ab
    ) {
        float s = heavyPermafrost ? (scale * 1.28F) : scale;
        float spin = age * (heavyPermafrost ? 0.18F : 0.34F) + (seed & 0x7F) * 0.04F;

        // 1. Razor diamond-white (#EBF9FF) inner cryogenic needle core
        renderElongatedBipyramid(mat, vc, 0.085F * s, 0.84F * s, 0.52F * s, 6, spin,
                0.95F, 0.99F, 1.00F, 0.98F);

        // 2. Aerodynamic 6-sided faceted Glacial Cyan (#98DCF2) & Permafrost Blue (#2C82C9) crystal spearhead
        Vec3 nose = new Vec3(0.0D, 0.0D, 0.96D * s);
        Vec3 tail = new Vec3(0.0D, 0.0D, -0.60D * s);
        float waistR = 0.185F * s;
        for (int i = 0; i < 6; i++) {
            float a0 = spin + (float) (Math.PI * 2.0D * i / 6.0D);
            float a1 = spin + (float) (Math.PI * 2.0D * (i + 1) / 6.0D);
            Vec3 w0 = new Vec3(Mth.cos(a0) * waistR, Mth.sin(a0) * waistR, 0.02D * s);
            Vec3 w1 = new Vec3(Mth.cos(a1) * waistR, Mth.sin(a1) * waistR, 0.02D * s);

            boolean cyanFacet = (i % 2 == 0);
            float fr = cyanFacet ? 0.596F : 0.173F;
            float fg = cyanFacet ? 0.863F : 0.510F;
            float fb = cyanFacet ? 0.949F : 0.788F;
            emitDoubleSidedQuad(mat, vc, nose, w0, w1, nose, fr, fg, fb, 0.82F);
            emitDoubleSidedQuad(mat, vc, tail, w1, w0, tail, fr * 0.92F, fg * 0.95F, fb, 0.76F);

            if (cyanFacet) {
                renderLocalSegment(mat, vc, w0, nose, 0.015F * s, 0.94F, 0.99F, 1.00F, 0.96F);
            }
        }

        // 3. Six swept-back razor icicle barbs jutting outward from the crystalline waist
        for (int b = 0; b < 6; b++) {
            float bAng = -spin * 0.72F + (float) (Math.PI * 2.0D * b / 6.0D);
            Vec3 rad = new Vec3(Mth.cos(bAng), Mth.sin(bAng), 0.0D);
            Vec3 tang = new Vec3(-Mth.sin(bAng), Mth.cos(bAng), 0.0D);
            Vec3 root = rad.scale(0.11D * s).add(0.0D, 0.0D, 0.16D * s);
            Vec3 tip = rad.scale(0.38D * s).add(0.0D, 0.0D, -0.32D * s);
            Vec3 sideA = root.add(tang.scale(0.052D * s));
            Vec3 sideB = root.subtract(tang.scale(0.052D * s));
            boolean brightBarb = (b % 2 == 0);
            emitDoubleSidedQuad(mat, vc, sideA, sideB, tip, tip,
                    brightBarb ? 0.92F : 0.596F, brightBarb ? 0.98F : 0.863F, 1.00F, 0.88F);
        }

        // 4. Counter-rotating 6-pointed crystalline snowflake collar ring
        Vec3 rx = new Vec3(Mth.cos(-spin * 0.85F), Mth.sin(-spin * 0.85F), 0.0D);
        Vec3 ry = new Vec3(-Mth.sin(-spin * 0.85F), Mth.cos(-spin * 0.85F), 0.0D);
        renderLocalRing(mat, vc, new Vec3(0.0D, 0.0D, -0.04D * s), rx, ry,
                0.22F * s, 0.30F * s, 6, 0.92F, 0.98F, 1.00F, 0.90F);
        for (int p = 0; p < 6; p++) {
            float pAng = (float) (Math.PI * 2.0D * p / 6.0D);
            Vec3 spokeDir = rx.scale(Mth.cos(pAng)).add(ry.scale(Mth.sin(pAng)));
            renderLocalSegment(mat, vc,
                    spokeDir.scale(0.18D * s).add(0.0D, 0.0D, -0.04D * s),
                    spokeDir.scale(0.42D * s).add(0.0D, 0.0D, -0.10D * s),
                    0.018F * s, 0.94F, 0.99F, 1.00F, 0.92F);
        }

        // 5. Permafrost Creep (Slow/Heavy Flow Mutation): 3 orbiting Blue-Ice permafrost rime crystals!
        if (heavyPermafrost) {
            for (int rIdx = 0; rIdx < 3; rIdx++) {
                float rAng = -age * 0.24F + (float) (Math.PI * 2.0D * rIdx / 3.0D);
                Vec3 cPos = new Vec3(Mth.cos(rAng) * 0.48D * scale, Mth.sin(rAng) * 0.48D * scale, -0.12D * scale);
                Vec3 bx = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 by = new Vec3(0.0D, 1.0D, 0.0D);
                Vec3 bz = new Vec3(0.0D, 0.0D, 1.0D);
                float cr = 0.13F * scale;
                for (int face = 0; face < 6; face++) {
                    float fa0 = rAng + (float) (Math.PI * 2.0D * face / 6.0D);
                    float fa1 = rAng + (float) (Math.PI * 2.0D * (face + 1) / 6.0D);
                    Vec3 c0 = cPos.add(bx.scale(Mth.cos(fa0) * cr)).add(by.scale(Mth.sin(fa0) * cr));
                    Vec3 c1 = cPos.add(bx.scale(Mth.cos(fa1) * cr)).add(by.scale(Mth.sin(fa1) * cr));
                    Vec3 top = cPos.add(bz.scale(cr * 1.45D));
                    Vec3 bot = cPos.subtract(bz.scale(cr * 1.45D));
                    emitDoubleSidedQuad(mat, vc, top, c0, c1, top, 0.173F, 0.510F, 0.788F, 0.90F);
                    emitDoubleSidedQuad(mat, vc, bot, c1, c0, bot, 0.596F, 0.863F, 0.949F, 0.88F);
                }
            }
        }
    }

    // =========================================================================
    // ARCHETYPE 14: NETHERFIRE COMET (MOLTEN MAGMA ORB, SLAG PLATES & FLAME CORONA)
    // =========================================================================

    private static void renderNetherfireComet(
            Matrix4f mat, VertexConsumer vc, float scale, float age, int seed,
            float pr, float pg, float pb, float ar, float ag, float ab
    ) {
        float pulse = 0.94F + 0.08F * Mth.sin(age * 0.42F + (seed & 0x1F));
        float spin = age * 0.28F + (seed & 0x3F) * 0.05F;
        float s = scale * pulse;

        // 1. White-hot / molten-gold (#FFD15C) incandescent magma heart
        renderElongatedBipyramid(mat, vc, 0.22F * s, 0.26F * s, 0.26F * s, 8, spin,
                1.00F, 0.90F, 0.52F, 0.98F);

        // 2. Roaring Volcanic Red-Orange (#B83C08) molten magma orb shell laced with glowing lava seams
        renderElongatedBipyramid(mat, vc, 0.31F * s, 0.36F * s, 0.38F * s, 8, -spin * 0.65F,
                0.722F, 0.235F, 0.031F, 0.88F);
        for (int seam = 0; seam < 4; seam++) {
            float sAng = -spin * 0.65F + (float) (Math.PI * 0.5D * seam);
            Vec3 u = new Vec3(Mth.cos(sAng), Mth.sin(sAng), 0.0D);
            Vec3 v = new Vec3(0.0D, 0.0D, 1.0D);
            renderLocalRing(mat, vc, Vec3.ZERO, u, v, 0.29F * s, 0.335F * s, 14,
                    1.00F, 0.820F, 0.361F, 0.95F);
        }

        // 3. Six fractured dark basalt-slag crust plates tumbling across the boiling magma surface
        for (int c = 0; c < 6; c++) {
            float cAng = spin * 0.85F + (float) (Math.PI * 2.0D * c / 6.0D);
            float zBob = 0.14F * Mth.sin(age * 0.24F + c * 1.1F);
            Vec3 rad = new Vec3(Mth.cos(cAng), Mth.sin(cAng), zBob).normalize();
            Vec3 tang = new Vec3(-Mth.sin(cAng), Mth.cos(cAng), 0.0D);
            Vec3 zVec = rad.cross(tang).normalize();
            Vec3 center = rad.scale(0.335D * s);
            Vec3 p0 = center.add(tang.scale(0.11D * s)).add(zVec.scale(0.09D * s));
            Vec3 p1 = center.subtract(tang.scale(0.11D * s)).add(zVec.scale(0.08D * s));
            Vec3 p2 = center.subtract(tang.scale(0.09D * s)).subtract(zVec.scale(0.10D * s));
            Vec3 p3 = center.add(tang.scale(0.10D * s)).subtract(zVec.scale(0.09D * s));
            emitDoubleSidedQuad(mat, vc, p0, p1, p2, p3, 0.165F, 0.090F, 0.071F, 0.94F);
            renderLocalSegment(mat, vc, p0, p2, 0.015F * s, 1.00F, 0.820F, 0.361F, 0.96F);
        }

        // 4. Six roaring 3D swept-back comet flame tongues trailing behind the magma orb
        for (int f = 0; f < 6; f++) {
            float fAng = -spin * 1.15F + (float) (Math.PI * 2.0D * f / 6.0D);
            float flicker = 0.82F + 0.24F * Mth.sin(age * 0.55F + f * 1.3F);
            Vec3 rad = new Vec3(Mth.cos(fAng), Mth.sin(fAng), 0.0D);
            Vec3 tang = new Vec3(-Mth.sin(fAng), Mth.cos(fAng), 0.0D);
            Vec3 root = rad.scale(0.26D * s).add(0.0D, 0.0D, 0.06D * s);
            Vec3 tip = rad.scale(0.12D * s).add(0.0D, 0.0D, -0.88D * s * flicker);
            Vec3 sideA = root.add(tang.scale(0.095D * s));
            Vec3 sideB = root.subtract(tang.scale(0.095D * s));
            boolean goldTongue = (f % 2 == 0);
            emitDoubleSidedQuad(mat, vc, sideA, sideB, tip, tip,
                    goldTongue ? 1.00F : 0.88F,
                    goldTongue ? 0.820F : 0.29F,
                    goldTongue ? 0.361F : 0.05F,
                    0.86F);
        }
    }

    // =========================================================================
    // ARCHETYPE 15: HOMING DAWN STAR (SECTION 6.8 RADIANT MATERIA)
    // =========================================================================

    private static void renderDawnStar(
            Matrix4f mat, VertexConsumer vc, float scale, float age, int seed,
            float pr, float pg, float pb, float ar, float ag, float ab
    ) {
        float pulse = 0.92F + 0.08F * Mth.sin(age * 0.32F + seed);
        float s = scale * pulse;
        float spin = age * 0.16F + (seed % 11) * 0.27F;

        // 1. Incandescent white-gold photosphere core (#FFFDF0) + faceted #FFE278 concentrated sunlight orb
        renderElongatedBipyramid(mat, vc, 0.21F * s, 0.22F * s, 0.22F * s, 8, spin,
                1.00F, 0.995F, 0.95F, 0.99F);
        renderElongatedBipyramid(mat, vc, 0.31F * s, 0.33F * s, 0.33F * s, 8, -spin * 0.75F,
                1.00F, 0.886F, 0.471F, 0.82F);

        // 2. 8-point 3D compass-rose sunburst rays radiating from the floating sunlight orb
        renderTransverseSpikes(mat, vc, 0.68F * s, 0.078F * s, spin * 0.65F,
                1.00F, 0.99F, 0.92F, 0.96F);
        renderTransverseSpikes(mat, vc, 0.50F * s, 0.062F * s, spin * 0.65F + (float) (Math.PI * 0.25D),
                1.00F, 0.886F, 0.471F, 0.90F);
        Vec3 zAxis = new Vec3(0.0D, 0.0D, 1.0D);
        Vec3 xAxis = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 yAxis = new Vec3(0.0D, 1.0D, 0.0D);
        renderSpikeAlongAxis(mat, vc, zAxis, xAxis, yAxis, 0.64F * s, 0.072F * s,
                1.00F, 0.96F, 0.78F, 0.94F);

        // 3. Twin counter-rotating tilted golden armillary halos
        Vec3 h1U = new Vec3(Mth.cos(spin), 0.36D, Mth.sin(spin)).normalize();
        Vec3 h1V = new Vec3(-Mth.sin(spin), 0.36D, Mth.cos(spin)).normalize();
        renderLocalRing(mat, vc, Vec3.ZERO, h1U, h1V, 0.42F * s, 0.49F * s, 24,
                1.00F, 0.886F, 0.471F, 0.88F);

        Vec3 h2U = new Vec3(Mth.cos(-spin * 1.15F), -0.40D, Mth.sin(-spin * 1.15F)).normalize();
        Vec3 h2V = new Vec3(-Mth.sin(-spin * 1.15F), -0.40D, Mth.cos(-spin * 1.15F)).normalize();
        renderLocalRing(mat, vc, Vec3.ZERO, h2U, h2V, 0.50F * s, 0.55F * s, 24,
                1.00F, 0.99F, 0.92F, 0.80F);

        // 4. 4 orbiting starlight satellite motes circling the Dawn Star
        for (int m = 0; m < 4; m++) {
            float mAng = spin * 1.45F + (float) (Math.PI * 0.5D * m);
            double zOff = 0.14D * Mth.sin(age * 0.25F + m * 1.57F) * s;
            Vec3 motePos = new Vec3(Mth.cos(mAng) * 0.58D * s, Mth.sin(mAng) * 0.58D * s, zOff);
            Vec3 mr = new Vec3(1.0D, 0.0D, 0.0D);
            Vec3 mu = new Vec3(0.0D, 1.0D, 0.0D);
            renderLocalRing(mat, vc, motePos, mr, mu, 0.0F, 0.058F * s, 6,
                    1.00F, (m % 2 == 0) ? 0.99F : 0.886F, (m % 2 == 0) ? 0.94F : 0.471F, 0.95F);
        }
    }

    // =========================================================================
    // ARCHETYPE 16: SHADOW SNARE MISSILE (UMBRAL BASE #8 — SECTION 6.9)
    // =========================================================================

    private static void renderShadowSnareMissile(
            Matrix4f mat, VertexConsumer vc, float scale, float age, int seed,
            float pr, float pg, float pb, float ar, float ag, float ab
    ) {
        float spin = age * 0.24F + (seed & 7) * 0.41F;
        float pulse = 0.94F + 0.06F * Mth.sin(age * 0.32F + seed);
        float s = scale * pulse;

        // 1. Light-absorbing #211A21 obsidian shadow-needle core + smoky twilight-violet sheath
        renderElongatedBipyramid(mat, vc, 0.11F * s, 0.72F * s, 0.48F * s, 6, spin,
                0.085F, 0.058F, 0.095F, 0.97F);
        renderElongatedBipyramid(mat, vc, 0.21F * s, 0.84F * s, 0.56F * s, 6, -spin * 0.65F,
                0.180F, 0.125F, 0.220F, 0.84F);
        renderElongatedBipyramid(mat, vc, 0.26F * s, 0.62F * s, 0.40F * s, 6, spin * 0.50F,
                0.48F, 0.28F, 0.78F, 0.42F);

        // 2. Twin counter-rotating inverted eclipse corona collars drinking ambient light
        Vec3 e1U = new Vec3(Mth.cos(spin), Mth.sin(spin), 0.18D).normalize();
        Vec3 e1V = new Vec3(-Mth.sin(spin), Mth.cos(spin), 0.18D).normalize();
        renderLocalRing(mat, vc, new Vec3(0.0D, 0.0D, 0.10D * s), e1U, e1V, 0.22F * s, 0.31F * s, 18,
                0.129F, 0.102F, 0.129F, 0.92F);
        renderLocalRing(mat, vc, new Vec3(0.0D, 0.0D, 0.10D * s), e1U, e1V, 0.30F * s, 0.34F * s, 18,
                0.56F, 0.36F, 1.00F, 0.82F);

        Vec3 e2U = new Vec3(Mth.cos(-spin * 1.25F), -0.32D, Mth.sin(-spin * 1.25F)).normalize();
        Vec3 e2V = new Vec3(-Mth.sin(-spin * 1.25F), -0.32D, Mth.cos(-spin * 1.25F)).normalize();
        renderLocalRing(mat, vc, new Vec3(0.0D, 0.0D, -0.18D * s), e2U, e2V, 0.18F * s, 0.25F * s, 16,
                0.129F, 0.102F, 0.129F, 0.88F);

        // 3. 4 swept barbed shadow-tether tendrils streaming behind the piercing snare needle
        for (int t = 0; t < 4; t++) {
            float tAng = spin * 0.85F + (float) (Math.PI * 0.5D * t);
            Vec3 base = new Vec3(Mth.cos(tAng) * 0.15D * s, Mth.sin(tAng) * 0.15D * s, 0.06D * s);
            Vec3 mid = new Vec3(
                    Mth.cos(tAng + 0.45F) * (0.34D + 0.04D * Mth.sin(age * 0.38F + t)) * s,
                    Mth.sin(tAng + 0.45F) * (0.34D + 0.04D * Mth.sin(age * 0.38F + t)) * s,
                    -0.38D * s
            );
            Vec3 tail = new Vec3(Mth.cos(tAng + 0.92F) * 0.22D * s, Mth.sin(tAng + 0.92F) * 0.22D * s, -0.86D * s);
            renderLocalSegment(mat, vc, base, mid, 0.042F * s, 0.129F, 0.102F, 0.129F, 0.92F);
            renderLocalSegment(mat, vc, mid, tail, 0.028F * s, 0.52F, 0.32F, 0.88F, 0.78F);
        }
    }

    // =========================================================================
    // ARCHETYPE 17: NECROTIC BONE-SHARD / SKULL MISSILE (UNDEAD BASE #9 — SECTION 6.10)
    // =========================================================================

    private static void renderUndeadSkullMissile(
            Matrix4f mat, VertexConsumer vc, float scale, float age, int seed, boolean creepingTombDust,
            float pr, float pg, float pb, float ar, float ag, float ab
    ) {
        float pulse = 0.95F + 0.05F * Mth.sin(age * 0.30F + seed);
        float s = scale * pulse * (creepingTombDust ? 1.24F : 1.0F);
        Vec3 right = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 up = new Vec3(0.0D, 1.0D, 0.0D);

        // 1. Sharp serrated Bleached Bone-Shard Spear Nose (#E6DFCF) + Desiccated Tombstone Ossuary Core (#4D6453)
        renderElongatedBipyramid(mat, vc, 0.16F * s, 0.58F * s, 0.34F * s, 6, 0.0F,
                0.902F, 0.875F, 0.812F, 0.96F);
        // Sculpted Cranium Vault Dome & Zygomatic Cheekbone Plates
        for (int i = 0; i < 6; i++) {
            float a0 = (float) (Math.PI * 2.0D * i / 6.0D);
            float a1 = (float) (Math.PI * 2.0D * (i + 1) / 6.0D);
            Vec3 w0 = new Vec3(Mth.cos(a0) * 0.25D * s, 0.06D * s + Mth.sin(a0) * 0.21D * s, 0.04D * s);
            Vec3 w1 = new Vec3(Mth.cos(a1) * 0.25D * s, 0.06D * s + Mth.sin(a1) * 0.21D * s, 0.04D * s);
            Vec3 brow = new Vec3(0.0D, 0.10D * s, 0.30D * s);
            Vec3 occiput = new Vec3(0.0D, 0.05D * s, -0.24D * s);
            boolean tombTint = (i == 1 || i == 2);
            float cr = tombTint ? 0.302F : 0.902F;
            float cg = tombTint ? 0.392F : 0.875F;
            float cb = tombTint ? 0.325F : 0.812F;
            emitDoubleSidedQuad(mat, vc, brow, w0, w1, brow, cr, cg, cb, 0.95F);
            emitDoubleSidedQuad(mat, vc, occiput, w1, w0, occiput, 0.302F, 0.392F, 0.325F, 0.92F);
        }

        // 2. Twin Blazing Necrotic Soul-Green (#5EE088) Eye Sockets on the Skull Visage
        for (int side = -1; side <= 1; side += 2) {
            Vec3 eyePos = new Vec3(side * 0.105D * s, 0.065D * s, 0.27D * s);
            Vec3 eyePlaneU = right.add(new Vec3(0.0D, 0.0D, -side * 0.28D)).normalize();
            renderLocalRing(mat, vc, eyePos, eyePlaneU, up, 0.038F * s, 0.072F * s, 8,
                    0.16F, 0.21F, 0.18F, 0.96F);
            renderLocalRing(mat, vc, eyePos.add(0.0D, 0.0D, 0.01D * s), eyePlaneU, up, 0.0F, 0.046F * s, 8,
                    0.369F, 0.878F, 0.533F, 0.98F);
        }

        // 3. Chattering Articulated Mandible Jaw & 4 Bleached Fang Teeth
        double jawDrop = (0.045D + 0.032D * Math.abs(Mth.sin(age * 0.38F + seed))) * s;
        Vec3 jawHingeL = new Vec3(-0.15D * s, -0.04D * s, 0.06D * s);
        Vec3 jawHingeR = new Vec3(0.15D * s, -0.04D * s, 0.06D * s);
        Vec3 chinTip = new Vec3(0.0D, -0.09D * s - jawDrop, 0.28D * s);
        renderLocalSegment(mat, vc, jawHingeL, chinTip, 0.036F * s, 0.86F, 0.83F, 0.76F, 0.95F);
        renderLocalSegment(mat, vc, jawHingeR, chinTip, 0.036F * s, 0.86F, 0.83F, 0.76F, 0.95F);
        for (int tooth = 0; tooth < 4; tooth++) {
            double tx = (-0.09D + 0.06D * tooth) * s;
            Vec3 gum = new Vec3(tx, -0.02D * s, 0.25D * s);
            Vec3 fangTip = new Vec3(tx * 0.92D, -0.085D * s - jawDrop * 0.35D, 0.27D * s);
            renderLocalSegment(mat, vc, gum, fangTip, 0.018F * s, 0.94F, 0.92F, 0.86F, 0.96F);
        }

        // 4. Articulated Spinal Vertebrae Wake & Streaming #5EE088 Necrotic Ectoplasm Soul-Wisp Ribbons
        for (int v = 0; v < 4; v++) {
            double vz = (-0.18D - v * 0.15D) * s;
            double swayX = 0.045D * Mth.sin(age * 0.34F - v * 0.85F + seed) * s;
            double swayY = 0.035D * Mth.cos(age * 0.28F - v * 0.85F + seed) * s;
            float vr = (0.14F - v * 0.024F) * s;
            Vec3 vPos = new Vec3(swayX, swayY, vz);
            renderLocalRing(mat, vc, vPos, right, up, vr * 0.52F, vr, 6,
                    (v % 2 == 0) ? 0.902F : 0.302F,
                    (v % 2 == 0) ? 0.875F : 0.392F,
                    (v % 2 == 0) ? 0.812F : 0.325F,
                    0.90F - v * 0.12F);
        }
        for (int w = 0; w < 3; w++) {
            float wAng = age * 0.26F + (float) (Math.PI * 2.0D * w / 3.0D);
            Vec3 wStart = new Vec3(Mth.cos(wAng) * 0.12D * s, Mth.sin(wAng) * 0.12D * s, -0.08D * s);
            Vec3 wMid = new Vec3(Mth.cos(wAng + 0.7F) * 0.24D * s, Mth.sin(wAng + 0.7F) * 0.24D * s, -0.46D * s);
            Vec3 wEnd = new Vec3(Mth.cos(wAng + 1.4F) * 0.14D * s, Mth.sin(wAng + 1.4F) * 0.14D * s, -0.92D * s);
            renderLocalSegment(mat, vc, wStart, wMid, 0.042F * s, 0.369F, 0.878F, 0.533F, 0.84F);
            renderLocalSegment(mat, vc, wMid, wEnd, 0.024F * s, 0.302F, 0.392F, 0.325F, 0.68F);
        }

        // 5. Section 6.10 Creeping Tomb-Dust (Flow Slow): heavy, sluggish creeping fog of #4D6453 grave dust!
        if (creepingTombDust) {
            float fogSpin = age * 0.11F;
            for (int f = 0; f < 3; f++) {
                float fAng = fogSpin * (f % 2 == 0 ? 1.0F : -1.15F) + f * 1.05F;
                Vec3 fu = new Vec3(Mth.cos(fAng), 0.35D * (f - 1), Mth.sin(fAng)).normalize();
                Vec3 fv = new Vec3(-Mth.sin(fAng), 0.55D, Mth.cos(fAng)).normalize();
                renderLocalRing(mat, vc, new Vec3(0.0D, -0.06D * s, (-0.12D * f) * s), fu, fv,
                        (0.32F + 0.10F * f) * s, (0.56F + 0.14F * f) * s, 18,
                        0.302F, 0.392F, 0.325F, 0.58F - f * 0.10F);
            }
            renderElongatedBipyramid(mat, vc, 0.48F * s, 0.52F * s, 0.78F * s, 8, -fogSpin * 0.6F,
                    0.302F, 0.392F, 0.325F, 0.34F);
        }
    }

    // =========================================================================
    // ARCHETYPE 18: DUST DEVIL MISSILE (ARID #11 MINIATURE DESERT VORTEX)
    // =========================================================================

    private static void renderDustDevilMissile(
            Matrix4f mat, VertexConsumer vc, float scale, float age, int seed,
            float pr, float pg, float pb, float ar, float ag, float ab
    ) {
        float spin = age * 0.68F + (seed & 31) * 0.21F;
        float s = scale * 1.06F;

        // 1. Compressed terracotta-sandstone (#9C7A4C) inner eye & conical #C2B28F vortex funnel body
        renderElongatedBipyramid(mat, vc, 0.11F * s, 0.46F * s, 0.44F * s, 6, spin * 1.15F,
                0.612F, 0.478F, 0.298F, 0.94F);
        renderLocalConicalSkirt(mat, vc, 0.38F * s, -0.42F * s, 0.42F * s, 0.08F * s,
                20, spin, pr, pg, pb, 0.68F, 0.07F);
        renderLocalConicalSkirt(mat, vc, 0.34F * s, -0.36F * s, 0.30F * s, 0.05F * s,
                16, -spin * 1.25F, ar, ag, ab, 0.54F, 0.05F);

        // 2. Five stacked wobbling desert funnel-cloud rings widening from tail tip to leading vortex mouth
        for (int rIdx = 0; rIdx < 5; rIdx++) {
            float frac = rIdx / 4.0F;
            float rz = Mth.lerp(frac, -0.38F, 0.36F) * s;
            float outerR = Mth.lerp(frac, 0.11F, 0.44F) * s;
            float innerR = outerR * 0.64F;
            float wobbleX = 0.045F * Mth.sin(spin * 0.85F + rIdx * 1.15F) * s;
            float wobbleY = 0.045F * Mth.cos(spin * 0.85F + rIdx * 1.15F) * s;
            float tilt = 0.20F * Mth.sin(spin + rIdx * 0.9F);
            Vec3 ru = new Vec3(1.0D, tilt, 0.14D * Mth.cos(spin + rIdx)).normalize();
            Vec3 rv = new Vec3(-tilt, 1.0D, 0.14D * Mth.sin(spin + rIdx)).normalize();
            boolean sunlit = (rIdx % 2 == 0);
            renderLocalRing(mat, vc, new Vec3(wobbleX, wobbleY, rz), ru, rv,
                    innerR, outerR, 18,
                    sunlit ? ar : pr, sunlit ? ag : pg, sunlit ? ab : pb,
                    0.86F - (1.0F - frac) * 0.18F);
        }

        // 3. Six orbiting 3D sharp sandstone shrapnel grains whirling around the dust devil rim
        for (int g = 0; g < 6; g++) {
            float gAng = spin * 1.35F + (float) (Math.PI * 2.0D * g / 6.0D);
            float gz = (-0.22F + (g % 3) * 0.22F) * s;
            float gr = (0.24F + (g % 3) * 0.10F) * s;
            Vec3 grainCenter = new Vec3(Mth.cos(gAng) * gr, Mth.sin(gAng) * gr, gz);
            Vec3 tangent = new Vec3(-Mth.sin(gAng), Mth.cos(gAng), 0.35D).normalize();
            Vec3 grainA = grainCenter.subtract(tangent.scale(0.065D * s));
            Vec3 grainB = grainCenter.add(tangent.scale(0.065D * s));
            boolean terracotta = (g % 2 == 0);
            renderLocalSegment(mat, vc, grainA, grainB, 0.028F * s,
                    terracotta ? 0.612F : 0.961F,
                    terracotta ? 0.478F : 0.894F,
                    terracotta ? 0.298F : 0.745F,
                    0.95F);
        }
    }

    // =========================================================================
    // ARCHETYPE 19: PLASMA BOLT / BALL LIGHTNING (LIGHTNING #12 SPHERE)
    // =========================================================================

    private static void renderBallLightningOrb(
            Matrix4f mat, VertexConsumer vc, float scale, float age, int seed,
            float pr, float pg, float pb, float ar, float ag, float ab
    ) {
        float spin = age * 0.78F + (seed & 31) * 0.19F;
        float pulse = 0.92F + 0.10F * Mth.sin(age * 0.95F + seed);
        float s = scale * pulse;

        // 1. White-hot plasma core sphere + crackling #FFE600 Ball-Lightning outer plasma shell
        renderElongatedBipyramid(mat, vc, 0.20F * s, 0.20F * s, 0.20F * s, 8, spin,
                1.00F, 0.99F, 0.92F, 0.99F);
        renderElongatedBipyramid(mat, vc, 0.31F * s, 0.31F * s, 0.31F * s, 10, -spin * 0.85F,
                1.00F, 0.902F, 0.00F, 0.74F);

        // 2. Three fast-tumbling gyroscopic Faraday plasma rings around the Ball-Lightning sphere
        for (int c = 0; c < 3; c++) {
            float cAng = spin * (c % 2 == 0 ? 1.15F : -1.30F) + c * 1.047F;
            Vec3 ru = new Vec3(Mth.cos(cAng), Mth.sin(cAng) * 0.65D, 0.45D * (c - 1)).normalize();
            Vec3 rv = new Vec3(-Mth.sin(cAng), Mth.cos(cAng), 0.35D).normalize();
            boolean cyanArc = (c == 2);
            renderLocalRing(mat, vc, Vec3.ZERO, ru, rv, 0.33F * s, 0.40F * s, 18,
                    cyanArc ? ar : 1.00F, cyanArc ? ag : 0.902F, cyanArc ? ab : 0.00F, 0.92F);
        }

        // 3. Eight snapping 3D jagged corona lightning arcs leaping off the Ball-Lightning core!
        int flicker = (int) (age * 1.4F) + (seed & 63);
        for (int aIdx = 0; aIdx < 8; aIdx++) {
            float phi = (flicker * 0.93F) + (float) (Math.PI * 2.0D * aIdx / 8.0D);
            float elev = Mth.sin((flicker + aIdx * 3) * 1.7F) * 0.72F;
            Vec3 dir = new Vec3(Mth.cos(phi), elev, Mth.sin(phi)).normalize();
            Vec3 perp = new Vec3(-dir.z, 0.35D, dir.x).normalize();
            Vec3 p0 = dir.scale(0.18D * s);
            Vec3 p1 = dir.scale(0.34D * s).add(perp.scale(0.09D * s * ((aIdx & 1) == 0 ? 1.0D : -1.0D)));
            Vec3 p2 = dir.scale(0.50D * s).subtract(perp.scale(0.06D * s));
            renderLocalSegment(mat, vc, p0, p1, 0.026F * s, 1.00F, 0.902F, 0.00F, 0.96F);
            renderLocalSegment(mat, vc, p1, p2, 0.018F * s, 1.00F, 1.00F, 0.94F, 0.98F);
        }
    }

    // =========================================================================
    // ARCHETYPE 20: SQUALL TEMPEST SLUG (STORM #14 GALE-WIND & RAIN VORTEX)
    // =========================================================================

    private static void renderSquallVortexSlug(
            Matrix4f mat, VertexConsumer vc, float scale, float age, int seed,
            float pr, float pg, float pb, float ar, float ag, float ab
    ) {
        float spin = age * 0.82F + (seed & 31) * 0.17F;
        float s = scale * (0.94F + 0.08F * Mth.sin(age * 0.65F + seed));
        Vec3 rx = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 ry = new Vec3(0.0D, 1.0D, 0.0D);

        // 1. High-pressure #429BA3 tempest-teal water slug core + #D8FAFF seafoam slipstream spearhead
        renderElongatedBipyramid(mat, vc, 0.14F * s, 0.44F * s, 0.34F * s, 8, spin,
                0.847F, 0.980F, 1.000F, 0.98F);
        renderElongatedBipyramid(mat, vc, 0.22F * s, 0.52F * s, 0.42F * s, 8, -spin * 0.9F,
                0.259F, 0.608F, 0.639F, 0.84F);

        // 2. Four spinning hurricane squall funnel rings tapering from nose to tail
        for (int ring = 0; ring < 4; ring++) {
            float rt = ring / 3.0F;
            double rz = Mth.lerp(rt, 0.26D * s, -0.34D * s);
            float rad = Mth.lerp(rt, 0.18F * s, 0.38F * s);
            boolean foam = (ring % 2 == 0);
            renderLocalRing(mat, vc, new Vec3(0.0D, 0.0D, rz), rx, ry, rad * 0.80F, rad, 18,
                    foam ? 0.847F : 0.259F,
                    foam ? 0.980F : 0.608F,
                    foam ? 1.000F : 0.639F,
                    0.88F * (1.0F - rt * 0.25F));
        }

        // 3. Six driving rain-droplet streaks whipping around the turbulent gale vortex!
        for (int d = 0; d < 6; d++) {
            float dAng = -spin * 1.35F + (float) (Math.PI * 2.0D * d / 6.0D);
            double dr = (0.28D + 0.06D * (d % 2)) * s;
            double dz = (0.14D - 0.10D * (d % 3)) * s;
            Vec3 dropNose = new Vec3(Mth.cos(dAng) * dr, Mth.sin(dAng) * dr, dz + 0.12D * s);
            Vec3 dropTail = new Vec3(Mth.cos(dAng - 0.35F) * dr, Mth.sin(dAng - 0.35F) * dr, dz - 0.14D * s);
            renderLocalSegment(mat, vc, dropTail, dropNose, 0.026F * s, 0.847F, 0.980F, 1.000F, 0.95F);
        }
    }

    // =========================================================================
    // ARCHETYPE 21: GLACIAL ICE-LANCE (GLACIAL #15 HEAVY BLUE-ICE SPEAR)
    // =========================================================================

    private static void renderGlacialIceLance(
            Matrix4f mat, VertexConsumer vc, float scale, float age, int seed, boolean heavyPermafrost,
            float pr, float pg, float pb, float ar, float ag, float ab
    ) {
        float s = heavyPermafrost ? (scale * 1.32F) : (scale * 1.12F);
        float spin = age * (heavyPermafrost ? 0.16F : 0.28F) + (seed & 0x3F) * 0.05F;
        Vec3 rx = new Vec3(Mth.cos(spin), Mth.sin(spin), 0.0D);
        Vec3 ry = new Vec3(-Mth.sin(spin), Mth.cos(spin), 0.0D);

        // 1. Long diamond-white (#E0FFFF) sub-zero core shaft + heavy 8-faceted Blue-Ice (#2C82C9 / #98DCF2) spearhead
        renderElongatedBipyramid(mat, vc, 0.105F * s, 1.08F * s, 0.72F * s, 8, spin,
                0.88F, 1.00F, 1.00F, 0.98F);
        renderElongatedBipyramid(mat, vc, 0.245F * s, 1.16F * s, 0.68F * s, 8, spin,
                0.173F, 0.510F, 0.788F, 0.78F);
        renderElongatedBipyramid(mat, vc, 0.195F * s, 0.98F * s, 0.58F * s, 6, -spin * 0.65F,
                0.596F, 0.863F, 0.949F, 0.86F);

        // 2. Four broad swept-back aerodynamic Blue-Ice spear fins for 2-target piercing stability
        for (int f = 0; f < 4; f++) {
            float fAng = spin + (float) (Math.PI * 0.5D * f);
            Vec3 rad = new Vec3(Mth.cos(fAng), Mth.sin(fAng), 0.0D);
            Vec3 tang = new Vec3(-Mth.sin(fAng), Mth.cos(fAng), 0.0D);
            Vec3 lead = rad.scale(0.14D * s).add(0.0D, 0.0D, 0.34D * s);
            Vec3 outerTip = rad.scale(0.46D * s).add(0.0D, 0.0D, -0.36D * s);
            Vec3 rearRoot = rad.scale(0.12D * s).add(0.0D, 0.0D, -0.48D * s);
            boolean paleFin = (f % 2 == 0);
            emitDoubleSidedQuad(mat, vc,
                    lead.add(tang.scale(0.025D * s)), rearRoot.subtract(tang.scale(0.025D * s)),
                    outerTip, outerTip,
                    paleFin ? 0.596F : 0.173F,
                    paleFin ? 0.863F : 0.510F,
                    paleFin ? 0.949F : 0.788F,
                    0.90F);
            renderLocalSegment(mat, vc, lead, outerTip, 0.018F * s, 0.90F, 0.99F, 1.00F, 0.96F);
        }

        // 3. Dual counter-rotating cryo-slurry compression rings along the lance shaft
        renderLocalRing(mat, vc, new Vec3(0.0D, 0.0D, 0.08D * s), rx, ry,
                0.24F * s, 0.32F * s, 16, 0.596F, 0.863F, 0.949F, 0.90F);
        renderLocalRing(mat, vc, new Vec3(0.0D, 0.0D, -0.26D * s), rx, ry,
                0.18F * s, 0.25F * s, 16, 0.88F, 1.00F, 1.00F, 0.86F);

        // 4. Permafrost Creep (Slow Flow Modifier): 3 orbiting solid Blue-Ice monolith crystals!
        if (heavyPermafrost) {
            for (int c = 0; c < 3; c++) {
                float cAng = -age * 0.22F + (float) (Math.PI * 2.0D * c / 3.0D);
                Vec3 cPos = new Vec3(Mth.cos(cAng) * 0.52D * scale, Mth.sin(cAng) * 0.52D * scale, -0.10D * scale);
                Vec3 bx = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 by = new Vec3(0.0D, 1.0D, 0.0D);
                Vec3 bz = new Vec3(0.0D, 0.0D, 1.0D);
                float cr = 0.14F * scale;
                for (int face = 0; face < 6; face++) {
                    float fa0 = cAng + (float) (Math.PI * 2.0D * face / 6.0D);
                    float fa1 = cAng + (float) (Math.PI * 2.0D * (face + 1) / 6.0D);
                    Vec3 p0 = cPos.add(bx.scale(Mth.cos(fa0) * cr)).add(by.scale(Mth.sin(fa0) * cr));
                    Vec3 p1 = cPos.add(bx.scale(Mth.cos(fa1) * cr)).add(by.scale(Mth.sin(fa1) * cr));
                    Vec3 top = cPos.add(bz.scale(cr * 1.55D));
                    Vec3 bot = cPos.subtract(bz.scale(cr * 1.55D));
                    emitDoubleSidedQuad(mat, vc, top, p0, p1, top, 0.173F, 0.510F, 0.788F, 0.92F);
                    emitDoubleSidedQuad(mat, vc, bot, p1, p0, bot, 0.596F, 0.863F, 0.949F, 0.90F);
                }
            }
        }
    }

    // =========================================================================
    // ARCHETYPE 22: VERDANT BRIAR-SPIKE SEED (OVERGROWTH #16 SEED POD & EARTHEN THORNS)
    // =========================================================================

    private static void renderOvergrowthBriarPod(
            Matrix4f mat, VertexConsumer vc, float scale, float age, int seed,
            float pr, float pg, float pb, float ar, float ag, float ab
    ) {
        float spin = age * 0.42F + (seed & 31) * 0.13F;
        float s = scale * (1.02F + 0.06F * Mth.sin(age * 0.55F + seed));
        Vec3 rx = new Vec3(Mth.cos(spin), Mth.sin(spin), 0.0D);
        Vec3 ry = new Vec3(-Mth.sin(spin), Mth.cos(spin), 0.0D);

        // 1. Glowing emerald seed pod core (#2CE668 / #1A5923)
        renderElongatedBipyramid(mat, vc, 0.16F * s, 0.42F * s, 0.34F * s, 6, spin,
                0.706F, 1.000F, 0.471F, 0.96F);
        renderElongatedBipyramid(mat, vc, 0.25F * s, 0.50F * s, 0.40F * s, 6, -spin * 0.7F,
                0.102F, 0.349F, 0.137F, 0.88F);

        // 2. Six jagged earthen stone & wooden thorns (#773214 / #5A3A1E) wrapping the explosive seed pod
        for (int t = 0; t < 6; t++) {
            float tAng = spin + (float) (Math.PI * 2.0D * t / 6.0D);
            Vec3 rad = new Vec3(Mth.cos(tAng), Mth.sin(tAng), 0.0D);
            Vec3 tang = new Vec3(-Mth.sin(tAng), Mth.cos(tAng), 0.0D);
            Vec3 root = rad.scale(0.16D * s).add(0.0D, 0.0D, 0.14D * s);
            Vec3 tip = rad.scale(0.44D * s).add(0.0D, 0.0D, (t % 2 == 0 ? 0.18D : -0.22D) * s);
            boolean stoneThorn = (t % 2 == 0);
            emitDoubleSidedQuad(mat, vc,
                    root.add(tang.scale(0.055D * s)), root.subtract(tang.scale(0.055D * s)),
                    tip, tip,
                    stoneThorn ? 0.467F : 0.102F,
                    stoneThorn ? 0.196F : 0.349F,
                    stoneThorn ? 0.078F : 0.137F,
                    0.94F);
            renderLocalSegment(mat, vc, root, tip, 0.020F * s, 0.706F, 1.000F, 0.471F, 0.92F);
        }

        // 3. Intertwined mossy-root collar ring
        renderLocalRing(mat, vc, Vec3.ZERO, rx, ry, 0.24F * s, 0.32F * s, 14,
                0.467F, 0.196F, 0.078F, 0.90F);
    }

    // =========================================================================
    // ARCHETYPE 23: CHOKING DUST-POD (DUST #17 COMPACTED MINERAL POWDER SPHERE)
    // =========================================================================

    private static void renderChokingDustPod(
            Matrix4f mat, VertexConsumer vc, float scale, float age, int seed,
            float pr, float pg, float pb, float ar, float ag, float ab
    ) {
        float spin = age * 0.48F + (seed & 31) * 0.17F;
        float s = scale * (1.02F + 0.05F * Mth.sin(age * 0.62F + seed));

        // 1. Compacted sphere of fine mineral powder (#C2B28F sandstone dun + #F2ECE1 chalk-white core)
        renderElongatedBipyramid(mat, vc, 0.19F * s, 0.22F * s, 0.22F * s, 8, spin,
                0.949F, 0.925F, 0.882F, 0.96F);
        renderElongatedBipyramid(mat, vc, 0.28F * s, 0.31F * s, 0.29F * s, 8, -spin * 0.85F,
                0.761F, 0.698F, 0.561F, 0.88F);

        // 2. Twin counter-rotating abrasive mineral sandpaper rings
        Vec3 rx1 = new Vec3(Mth.cos(spin), 0.38D, Mth.sin(spin)).normalize();
        Vec3 rz1 = new Vec3(-Mth.sin(spin), 0.38D, Mth.cos(spin)).normalize();
        renderLocalRing(mat, vc, Vec3.ZERO, rx1, rz1, 0.28F * s, 0.37F * s, 18,
                0.949F, 0.925F, 0.882F, 0.90F);

        Vec3 rx2 = new Vec3(Mth.cos(-spin * 1.2F), -0.42D, Mth.sin(-spin * 1.2F)).normalize();
        Vec3 rz2 = new Vec3(-Mth.sin(-spin * 1.2F), -0.42D, Mth.cos(-spin * 1.2F)).normalize();
        renderLocalRing(mat, vc, Vec3.ZERO, rx2, rz2, 0.25F * s, 0.33F * s, 16,
                0.549F, 0.490F, 0.396F, 0.86F);

        // 3. Six orbiting fine mineral grit grains trailing behind the compacted pod
        for (int g = 0; g < 6; g++) {
            float gAng = -spin * 1.4F + (float) (Math.PI * 2.0D * g / 6.0D);
            Vec3 gPos = new Vec3(Mth.cos(gAng) * 0.34D * s, Mth.sin(gAng) * 0.34D * s, (-0.12D - (g % 2) * 0.14D) * s);
            boolean chalkGrain = (g % 2 == 0);
            Vec3 gx = new Vec3(0.06F * s, 0.0D, 0.0D);
            Vec3 gy = new Vec3(0.0D, 0.06F * s, 0.0D);
            emitDoubleSidedQuad(mat, vc,
                    gPos.subtract(gx).subtract(gy), gPos.add(gx).subtract(gy),
                    gPos.add(gx).add(gy), gPos.subtract(gx).add(gy),
                    chalkGrain ? 0.949F : 0.761F,
                    chalkGrain ? 0.925F : 0.698F,
                    chalkGrain ? 0.882F : 0.561F,
                    0.92F);
        }
    }

    // =========================================================================
    // ARCHETYPE 24: AERODYNAMIC FUNGAL PUFFBALL (#18 SPORE — #1A5923 / #A88654)
    // =========================================================================

    private static void renderFungalPuffball(
            Matrix4f mat, VertexConsumer vc, float scale, float age, int seed,
            float pr, float pg, float pb, float ar, float ag, float ab
    ) {
        float breathe = 0.92F + 0.12F * Mth.sin(age * 0.24F + seed * 0.1F);
        float spin = age * 0.16F;

        // 1. Inner glowing buff-air-gold (#A88654 / #E8C882) spore-sac kernel
        renderElongatedBipyramid(mat, vc, 0.18F * scale * breathe, 0.20F * scale * breathe, 0.18F * scale * breathe,
                8, spin, 0.910F, 0.784F, 0.510F, 0.95F);

        // 2. Bulbous #1A5923 Nature-Green fungal puffball cap dome & soft stem base
        renderElongatedBipyramid(mat, vc, 0.28F * scale * breathe, 0.30F * scale * breathe, 0.22F * scale * breathe,
                10, -spin * 0.5F, 0.102F, 0.349F, 0.137F, 0.86F);

        // 3. Golden gill collar ring & drifting windborne slipstream ring
        Vec3 rx = new Vec3(Mth.cos(spin), Mth.sin(spin) * 0.25D, 0.0D).normalize();
        Vec3 ry = new Vec3(-Mth.sin(spin) * 0.25D, 1.0D, Mth.cos(spin) * 0.25D).normalize();
        renderLocalRing(mat, vc, new Vec3(0.0D, 0.0D, -0.04D * scale), rx, ry,
                0.24F * scale * breathe, 0.36F * scale * breathe, 18, 0.659F, 0.525F, 0.329F, 0.90F);

        Vec3 rx2 = new Vec3(Mth.cos(-spin * 1.4F), 0.0D, Mth.sin(-spin * 1.4F)).normalize();
        Vec3 rz2 = new Vec3(-Mth.sin(-spin * 1.4F) * 0.35D, 1.0D, Mth.cos(-spin * 1.4F) * 0.35D).normalize();
        renderLocalRing(mat, vc, Vec3.ZERO, rx2, rz2,
                0.30F * scale, 0.38F * scale, 16, 0.173F, 0.902F, 0.408F, 0.78F);

        // 4. 5 buoyant orbiting dual-tone (#1A5923 / #A88654) fungal spore motes
        for (int i = 0; i < 5; i++) {
            float mAng = spin * 1.65F + (float) (Math.PI * 2.0D * i / 5.0D);
            float mRad = (0.35F + 0.06F * Mth.sin(age * 0.30F + i)) * scale;
            double mx = Mth.cos(mAng) * mRad;
            double my = Mth.sin(mAng) * mRad;
            double mz = 0.10D * scale * Mth.cos(age * 0.22F + i * 1.3F);
            Vec3 p0 = new Vec3(mx, my, mz - 0.055D * scale);
            Vec3 p1 = new Vec3(mx, my, mz + 0.055D * scale);
            boolean goldMote = (i % 2 == 0);
            renderLocalSegment(mat, vc, p0, p1, 0.048F * scale,
                    goldMote ? 0.910F : 0.173F,
                    goldMote ? 0.784F : 0.902F,
                    goldMote ? 0.510F : 0.408F,
                    0.94F);
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

    private static void emitPerVertexDoubleSidedQuad(
            Matrix4f mat, VertexConsumer vc,
            Vec3 v0, Vec3 v1, Vec3 v2, Vec3 v3,
            float r0, float g0, float b0, float a0,
            float r1, float g1, float b1, float a1,
            float r2, float g2, float b2, float a2,
            float r3, float g3, float b3, float a3
    ) {
        vc.addVertex(mat, (float) v0.x, (float) v0.y, (float) v0.z)
                .setColor(r0, g0, b0, a0).setUv(0.0F, 0.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(mat, (float) v1.x, (float) v1.y, (float) v1.z)
                .setColor(r1, g1, b1, a1).setUv(0.0F, 1.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(mat, (float) v2.x, (float) v2.y, (float) v2.z)
                .setColor(r2, g2, b2, a2).setUv(1.0F, 1.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(mat, (float) v3.x, (float) v3.y, (float) v3.z)
                .setColor(r3, g3, b3, a3).setUv(1.0F, 0.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);

        vc.addVertex(mat, (float) v3.x, (float) v3.y, (float) v3.z)
                .setColor(r3, g3, b3, a3).setUv(1.0F, 0.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(mat, (float) v2.x, (float) v2.y, (float) v2.z)
                .setColor(r2, g2, b2, a2).setUv(1.0F, 1.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(mat, (float) v1.x, (float) v1.y, (float) v1.z)
                .setColor(r1, g1, b1, a1).setUv(0.0F, 1.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
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
