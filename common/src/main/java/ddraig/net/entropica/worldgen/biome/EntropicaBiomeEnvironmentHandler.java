package ddraig.net.entropica.worldgen.biome;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.config.EntropicaConfig;
import ddraig.net.entropica.worldgen.structure.AmberWillowBathhouseStructure;
import ddraig.net.entropica.worldgen.structure.AuroralWellspringStructure;
import ddraig.net.entropica.worldgen.structure.RadiantSpringPavilionStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Applies gameplay environmental physics and regional Materia-infused biome effects on both server and client,
 * with continuous 2D spatial neighborhood blending across borders between non-effect biomes and custom-effect biomes:
 * <ul>
 *   <li><b>Aegis / Regional Descent Velocity</b> (e.g. Biome 1 {@code Blue-Glacier Shelf} +15% faster
 *       descent velocity via spatially blended, Hermite-eased linear gravity scaling while preserving
 *       normal block-based fall damage).</li>
 *   <li><b>Low-Gravity Anomalies</b> (Jump Boost in Void/Zephyr biomes once spatial presence reaches threshold,
 *       preventing single-block corner HUD flicker).</li>
 *   <li><b>Sanctuary Vitae Regeneration</b> (slow passive health recovery in Radiant sanctuary biomes).</li>
 *   <li><b>Custom Per-Biome Server Hazards</b>.</li>
 * </ul>
 */
public final class EntropicaBiomeEnvironmentHandler {
    /**
     * Minimum 2D spatial neighborhood weight inside an effect biome required before ambient buffs,
     * sanctuary regeneration, or custom server hazards activate (prevents 1-block corner flickering).
     */
    public static final float BUFF_PRESENCE_THRESHOLD = 0.35F;

    /**
     * World-aligned 4x4 uniform cubic B-spline lattice spacing (6 blocks -> exact 9-block transition radius
     * around boundaries, symmetric across x=0 / z=0 via +3 block cell offset).
     */
    private static final double SPATIAL_GRID_STEP = 6.0D;
    private static final double SPATIAL_GRID_OFFSET = 3.0D;

    /**
     * Dyadic Hermite smoothstep blend factor (3/8 = 0.375, exact in IEEE-754 binary floating point)
     * applied on top of the C2 cubic B-spline presence weight for ultra-smooth border easing (<0.025x/block).
     */
    private static final double HERMITE_EASE_BLEND = 0.375D;

    private static final ThreadLocal<BlockPos.MutableBlockPos> SCRATCH_POS =
            ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);

    private static final ResourceLocation MIRAGE_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mirage_ice");
    private static final ResourceLocation MIRAGE_VEIL_GOGGLES_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mirage_veil_goggles");
    private static final ResourceLocation MIRAGE_PRISM_LANTERN_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mirage_prism_lantern");
    private static final ResourceLocation DOUBLE_VISION_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "double_vision");

    private static final Map<UUID, Long> LAST_MIRAGE_ICE_EYE_TICK = new ConcurrentHashMap<>();

    private EntropicaBiomeEnvironmentHandler() {}

    public static void tickLivingEntity(LivingEntity entity) {
        if (entity == null || !entity.isAlive() || entity.isSpectator()) {
            return;
        }
        if (!EntropicaConfig.ENABLE_BIOME_ENVIRONMENTAL_EFFECTS.get()) {
            return;
        }

        Level level = entity.level();
        boolean isServer = !level.isClientSide() && level instanceof ServerLevel;
        boolean needsGravity = isEligibleForGravityScaling(entity) && entity.getDeltaMovement().y < -0.005D;
        boolean isServerPlayer = isServer && entity instanceof Player;

        // Fast-path 1: Client entities that are not actively descending require zero biome lookups.
        if (!needsGravity && !isServer) {
            return;
        }

        if (isServer) {
            tickMirageIceHeadHazard((ServerLevel) level, entity);
            if (entity.getTicksFrozen() > 0 && EntropicaBiomeAtmospheres.hasRadiantFreezeImmunity(entity)) {
                entity.setTicksFrozen(0);
            }
            if (entity instanceof ServerPlayer serverPlayer) {
                AuroralWellspringStructure.tickWellspringThermalEffects(serverPlayer);
                RadiantSpringPavilionStructure.tickSpringPavilionEffects(serverPlayer);
                AmberWillowBathhouseStructure.tickBathhouseThermalEffects(serverPlayer);
            }
        }

        BlockPos.MutableBlockPos scratchPos = SCRATCH_POS.get();
        int blockY = entity.getBlockY();

        // Fast-path 2: Grounded non-player server mobs only ever run customServerTick if present.
        if (!needsGravity && !isServerPlayer) {
            BiomeAtmosphereProfile centerProfile = sampleProfileAt(
                    level, scratchPos, entity.getBlockX(), blockY, entity.getBlockZ());
            if (centerProfile == null || centerProfile.getCustomServerTick() == null) {
                return;
            }
        }

        double entityX = entity.getX();
        double entityZ = entity.getZ();
        if (!Double.isFinite(entityX) || !Double.isFinite(entityZ)) {
            return;
        }

        double ux = (entityX - SPATIAL_GRID_OFFSET) / SPATIAL_GRID_STEP;
        double uz = (entityZ - SPATIAL_GRID_OFFSET) / SPATIAL_GRID_STEP;
        int ix0 = (int) Math.floor(ux);
        int iz0 = (int) Math.floor(uz);

        int bx0 = gridBlockCoord(ix0 - 1);
        int bx1 = gridBlockCoord(ix0);
        int bx2 = gridBlockCoord(ix0 + 1);
        int bx3 = gridBlockCoord(ix0 + 2);

        int bz0 = gridBlockCoord(iz0 - 1);
        int bz1 = gridBlockCoord(iz0);
        int bz2 = gridBlockCoord(iz0 + 1);
        int bz3 = gridBlockCoord(iz0 + 2);

        // Fast-path 3: Probe the 4 outer corners + 2 interior cells (6 points).
        // By convexity over [bx0..bx3] x [bz0..bz3], every straight/angled/quadrant border hits at least one corner,
        // and the 2 interior cells catch compact islands — keeping deep vanilla plains/oceans super fast!
        BiomeAtmosphereProfile p00 = sampleProfileAt(level, scratchPos, bx0, blockY, bz0);
        BiomeAtmosphereProfile p30 = sampleProfileAt(level, scratchPos, bx3, blockY, bz0);
        BiomeAtmosphereProfile p03 = sampleProfileAt(level, scratchPos, bx0, blockY, bz3);
        BiomeAtmosphereProfile p33 = sampleProfileAt(level, scratchPos, bx3, blockY, bz3);
        BiomeAtmosphereProfile p11 = sampleProfileAt(level, scratchPos, bx1, blockY, bz1);
        BiomeAtmosphereProfile p22 = sampleProfileAt(level, scratchPos, bx2, blockY, bz2);

        if (p00 == null && p30 == null && p03 == null && p33 == null && p11 == null && p22 == null) {
            return;
        }

        // Sample the remaining 10 cells of the 4x4 neighborhood (zero duplicate lookups).
        BiomeAtmosphereProfile p10 = sampleProfileAt(level, scratchPos, bx1, blockY, bz0);
        BiomeAtmosphereProfile p20 = sampleProfileAt(level, scratchPos, bx2, blockY, bz0);
        BiomeAtmosphereProfile p01 = sampleProfileAt(level, scratchPos, bx0, blockY, bz1);
        BiomeAtmosphereProfile p21 = sampleProfileAt(level, scratchPos, bx2, blockY, bz1);
        BiomeAtmosphereProfile p31 = sampleProfileAt(level, scratchPos, bx3, blockY, bz1);
        BiomeAtmosphereProfile p02 = sampleProfileAt(level, scratchPos, bx0, blockY, bz2);
        BiomeAtmosphereProfile p12 = sampleProfileAt(level, scratchPos, bx1, blockY, bz2);
        BiomeAtmosphereProfile p32 = sampleProfileAt(level, scratchPos, bx3, blockY, bz2);
        BiomeAtmosphereProfile p13 = sampleProfileAt(level, scratchPos, bx1, blockY, bz3);
        BiomeAtmosphereProfile p23 = sampleProfileAt(level, scratchPos, bx2, blockY, bz3);

        double fx = clamp01(ux - ix0);
        double fz = clamp01(uz - iz0);

        double wx0 = bspline3Weight0(fx);
        double wx1 = bspline3Weight1(fx);
        double wx2 = bspline3Weight2(fx);
        double wx3 = bspline3Weight3(fx);

        double wz0 = bspline3Weight0(fz);
        double wz1 = bspline3Weight1(fz);
        double wz2 = bspline3Weight2(fz);
        double wz3 = bspline3Weight3(fz);

        // 1. Smooth Spatial Descent Velocity Scaling (runs identically on Client & Server for zero rubberbanding!)
        if (needsGravity) {
            float descentMult = computeBlendedDescentFromGrid(
                    p00, p10, p20, p30,
                    p01, p11, p21, p31,
                    p02, p12, p22, p32,
                    p03, p13, p23, p33,
                    wx0, wx1, wx2, wx3,
                    wz0, wz1, wz2, wz3
            );
            if (Math.abs(descentMult - 1.0F) > 1.0E-4F) {
                Vec3 vel = entity.getDeltaMovement();
                double baseGravity = entity.getAttributeValue(Attributes.GRAVITY);
                if (baseGravity > 0.0D) {
                    // Linear recurrence delta: v_{t+1} = 0.98 * (v_t - g * mult) => smooth scaled descent, normal block fall damage!
                    double extraDownward = -((double) descentMult - 1.0D) * baseGravity * 0.98D;
                    entity.setDeltaMovement(vel.x, vel.y + extraDownward, vel.z);
                }
            }
        }

        // 2. Server-Authoritative Ambient Buffs & Hazards (gated by spatial presence >= BUFF_PRESENCE_THRESHOLD)
        if (isServer && level instanceof ServerLevel serverLevel) {
            if (isServerPlayer) {
                if (entity.tickCount % 20 == 0) {
                    double jumpPresence = accumulateJumpPresence(
                            p00, p10, p20, p30, p01, p11, p21, p31,
                            p02, p12, p22, p32, p03, p13, p23, p33,
                            wx0, wx1, wx2, wx3, wz0, wz1, wz2, wz3);
                    if (jumpPresence >= BUFF_PRESENCE_THRESHOLD) {
                        double weightedAmp = accumulateWeightedJumpAmplifier(
                                p00, p10, p20, p30, p01, p11, p21, p31,
                                p02, p12, p22, p32, p03, p13, p23, p33,
                                wx0, wx1, wx2, wx3, wz0, wz1, wz2, wz3);
                        int effectiveJumpAmp = Math.max(0, (int) Math.round(weightedAmp / jumpPresence));
                        entity.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 55, effectiveJumpAmp, true, false, true));
                    }
                }

                double regenPresence = accumulateRegenPresence(
                        p00, p10, p20, p30, p01, p11, p21, p31,
                        p02, p12, p22, p32, p03, p13, p23, p33,
                        wx0, wx1, wx2, wx3, wz0, wz1, wz2, wz3);
                if (regenPresence >= BUFF_PRESENCE_THRESHOLD) {
                    if (entity.getTicksFrozen() > 0) {
                        entity.setTicksFrozen(0);
                    }
                    double weightedInterval = accumulateWeightedRegenInterval(
                            p00, p10, p20, p30, p01, p11, p21, p31,
                            p02, p12, p22, p32, p03, p13, p23, p33,
                            wx0, wx1, wx2, wx3, wz0, wz1, wz2, wz3);
                    int effectiveInterval = Math.max(1, (int) Math.round(weightedInterval / regenPresence));
                    boolean notStarving = !(entity instanceof Player p) || p.getFoodData().getFoodLevel() > 0;
                    if (entity.tickCount % effectiveInterval == 0 && notStarving && entity.getHealth() < entity.getMaxHealth()) {
                        entity.heal(1.0F);
                        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 0, true, false, true));
                    }
                }
            }

            BiomeAtmosphereProfile centerProfile = sampleProfileAt(
                    level, scratchPos, entity.getBlockX(), blockY, entity.getBlockZ());
            if (centerProfile != null) {
                BiConsumer<ServerLevel, LivingEntity> customTick = centerProfile.getCustomServerTick();
                if (customTick != null) {
                    double hazardPresence = accumulateCustomTickPresence(
                            p00, p10, p20, p30, p01, p11, p21, p31,
                            p02, p12, p22, p32, p03, p13, p23, p33,
                            wx0, wx1, wx2, wx3, wz0, wz1, wz2, wz3);
                    if (hazardPresence >= BUFF_PRESENCE_THRESHOLD) {
                        customTick.accept(serverLevel, entity);
                    }
                }
            }
        }
    }

    /**
     * Pure, deterministic, allocation-free helper computing the spatially blended and Hermite-eased
     * descent speed multiplier at continuous world coordinates {@code (entityX, entityZ)}.
     * <p>
     * Any sampled block coordinate returning {@code null} (vanilla biomes or Entropica biomes without
     * a registered {@link BiomeAtmosphereProfile}) contributes the baseline {@code 1.0F} multiplier.
     */
    public static float computeBlendedDescentMultiplier(
            BiFunction<Integer, Integer, BiomeAtmosphereProfile> blockSampler,
            double entityX,
            double entityZ
    ) {
        if (blockSampler == null || !Double.isFinite(entityX) || !Double.isFinite(entityZ)) {
            return 1.0F;
        }

        double ux = (entityX - SPATIAL_GRID_OFFSET) / SPATIAL_GRID_STEP;
        double uz = (entityZ - SPATIAL_GRID_OFFSET) / SPATIAL_GRID_STEP;
        int ix0 = (int) Math.floor(ux);
        int iz0 = (int) Math.floor(uz);

        int bx0 = gridBlockCoord(ix0 - 1);
        int bx1 = gridBlockCoord(ix0);
        int bx2 = gridBlockCoord(ix0 + 1);
        int bx3 = gridBlockCoord(ix0 + 2);

        int bz0 = gridBlockCoord(iz0 - 1);
        int bz1 = gridBlockCoord(iz0);
        int bz2 = gridBlockCoord(iz0 + 1);
        int bz3 = gridBlockCoord(iz0 + 2);

        BiomeAtmosphereProfile p00 = blockSampler.apply(bx0, bz0);
        BiomeAtmosphereProfile p30 = blockSampler.apply(bx3, bz0);
        BiomeAtmosphereProfile p03 = blockSampler.apply(bx0, bz3);
        BiomeAtmosphereProfile p33 = blockSampler.apply(bx3, bz3);
        BiomeAtmosphereProfile p11 = blockSampler.apply(bx1, bz1);
        BiomeAtmosphereProfile p22 = blockSampler.apply(bx2, bz2);

        if (p00 == null && p30 == null && p03 == null && p33 == null && p11 == null && p22 == null) {
            return 1.0F;
        }

        BiomeAtmosphereProfile p10 = blockSampler.apply(bx1, bz0);
        BiomeAtmosphereProfile p20 = blockSampler.apply(bx2, bz0);
        BiomeAtmosphereProfile p01 = blockSampler.apply(bx0, bz1);
        BiomeAtmosphereProfile p21 = blockSampler.apply(bx2, bz1);
        BiomeAtmosphereProfile p31 = blockSampler.apply(bx3, bz1);
        BiomeAtmosphereProfile p02 = blockSampler.apply(bx0, bz2);
        BiomeAtmosphereProfile p12 = blockSampler.apply(bx1, bz2);
        BiomeAtmosphereProfile p32 = blockSampler.apply(bx3, bz2);
        BiomeAtmosphereProfile p13 = blockSampler.apply(bx1, bz3);
        BiomeAtmosphereProfile p23 = blockSampler.apply(bx2, bz3);

        double fx = clamp01(ux - ix0);
        double fz = clamp01(uz - iz0);

        return computeBlendedDescentFromGrid(
                p00, p10, p20, p30,
                p01, p11, p21, p31,
                p02, p12, p22, p32,
                p03, p13, p23, p33,
                bspline3Weight0(fx), bspline3Weight1(fx), bspline3Weight2(fx), bspline3Weight3(fx),
                bspline3Weight0(fz), bspline3Weight1(fz), bspline3Weight2(fz), bspline3Weight3(fz)
        );
    }

    /**
     * Pure helper returning the continuous `[0..1]` neighborhood presence weight of Jump Boost biomes
     * around {@code (entityX, entityZ)} (used against {@link #BUFF_PRESENCE_THRESHOLD}).
     */
    public static float computeJumpBoostPresenceWeight(
            BiFunction<Integer, Integer, BiomeAtmosphereProfile> blockSampler,
            double entityX,
            double entityZ
    ) {
        if (blockSampler == null || !Double.isFinite(entityX) || !Double.isFinite(entityZ)) {
            return 0.0F;
        }

        double ux = (entityX - SPATIAL_GRID_OFFSET) / SPATIAL_GRID_STEP;
        double uz = (entityZ - SPATIAL_GRID_OFFSET) / SPATIAL_GRID_STEP;
        int ix0 = (int) Math.floor(ux);
        int iz0 = (int) Math.floor(uz);

        int bx0 = gridBlockCoord(ix0 - 1);
        int bx1 = gridBlockCoord(ix0);
        int bx2 = gridBlockCoord(ix0 + 1);
        int bx3 = gridBlockCoord(ix0 + 2);

        int bz0 = gridBlockCoord(iz0 - 1);
        int bz1 = gridBlockCoord(iz0);
        int bz2 = gridBlockCoord(iz0 + 1);
        int bz3 = gridBlockCoord(iz0 + 2);

        BiomeAtmosphereProfile p00 = blockSampler.apply(bx0, bz0);
        BiomeAtmosphereProfile p30 = blockSampler.apply(bx3, bz0);
        BiomeAtmosphereProfile p03 = blockSampler.apply(bx0, bz3);
        BiomeAtmosphereProfile p33 = blockSampler.apply(bx3, bz3);
        BiomeAtmosphereProfile p11 = blockSampler.apply(bx1, bz1);
        BiomeAtmosphereProfile p22 = blockSampler.apply(bx2, bz2);

        if (p00 == null && p30 == null && p03 == null && p33 == null && p11 == null && p22 == null) {
            return 0.0F;
        }

        double fx = clamp01(ux - ix0);
        double fz = clamp01(uz - iz0);

        double presence = accumulateJumpPresence(
                p00, blockSampler.apply(bx1, bz0), blockSampler.apply(bx2, bz0), p30,
                blockSampler.apply(bx0, bz1), p11, blockSampler.apply(bx2, bz1), blockSampler.apply(bx3, bz1),
                blockSampler.apply(bx0, bz2), blockSampler.apply(bx1, bz2), p22, blockSampler.apply(bx3, bz2),
                p03, blockSampler.apply(bx1, bz3), blockSampler.apply(bx2, bz3), p33,
                bspline3Weight0(fx), bspline3Weight1(fx), bspline3Weight2(fx), bspline3Weight3(fx),
                bspline3Weight0(fz), bspline3Weight1(fz), bspline3Weight2(fz), bspline3Weight3(fz)
        );
        return (float) clamp01(presence);
    }

    /**
     * Pure helper returning the continuous `[0..1]` neighborhood presence weight of passive sanctuary
     * regeneration biomes around {@code (entityX, entityZ)} (used against {@link #BUFF_PRESENCE_THRESHOLD}).
     */
    public static float computeSanctuaryRegenPresenceWeight(
            BiFunction<Integer, Integer, BiomeAtmosphereProfile> blockSampler,
            double entityX,
            double entityZ
    ) {
        if (blockSampler == null || !Double.isFinite(entityX) || !Double.isFinite(entityZ)) {
            return 0.0F;
        }

        double ux = (entityX - SPATIAL_GRID_OFFSET) / SPATIAL_GRID_STEP;
        double uz = (entityZ - SPATIAL_GRID_OFFSET) / SPATIAL_GRID_STEP;
        int ix0 = (int) Math.floor(ux);
        int iz0 = (int) Math.floor(uz);

        int bx0 = gridBlockCoord(ix0 - 1);
        int bx1 = gridBlockCoord(ix0);
        int bx2 = gridBlockCoord(ix0 + 1);
        int bx3 = gridBlockCoord(ix0 + 2);

        int bz0 = gridBlockCoord(iz0 - 1);
        int bz1 = gridBlockCoord(iz0);
        int bz2 = gridBlockCoord(iz0 + 1);
        int bz3 = gridBlockCoord(iz0 + 2);

        BiomeAtmosphereProfile p00 = blockSampler.apply(bx0, bz0);
        BiomeAtmosphereProfile p30 = blockSampler.apply(bx3, bz0);
        BiomeAtmosphereProfile p03 = blockSampler.apply(bx0, bz3);
        BiomeAtmosphereProfile p33 = blockSampler.apply(bx3, bz3);
        BiomeAtmosphereProfile p11 = blockSampler.apply(bx1, bz1);
        BiomeAtmosphereProfile p22 = blockSampler.apply(bx2, bz2);

        if (p00 == null && p30 == null && p03 == null && p33 == null && p11 == null && p22 == null) {
            return 0.0F;
        }

        double fx = clamp01(ux - ix0);
        double fz = clamp01(uz - iz0);

        double presence = accumulateRegenPresence(
                p00, blockSampler.apply(bx1, bz0), blockSampler.apply(bx2, bz0), p30,
                blockSampler.apply(bx0, bz1), p11, blockSampler.apply(bx2, bz1), blockSampler.apply(bx3, bz1),
                blockSampler.apply(bx0, bz2), blockSampler.apply(bx1, bz2), p22, blockSampler.apply(bx3, bz2),
                p03, blockSampler.apply(bx1, bz3), blockSampler.apply(bx2, bz3), p33,
                bspline3Weight0(fx), bspline3Weight1(fx), bspline3Weight2(fx), bspline3Weight3(fx),
                bspline3Weight0(fz), bspline3Weight1(fz), bspline3Weight2(fz), bspline3Weight3(fz)
        );
        return (float) clamp01(presence);
    }

    /**
     * Pure cubic Hermite smoothstep {@code 3t^2 - 2t^3} on {@code [0, 1]}.
     */
    public static double hermiteSmoothstep(double t) {
        double c = clamp01(t);
        return c * c * (3.0D - 2.0D * c);
    }

    private static float computeBlendedDescentFromGrid(
            BiomeAtmosphereProfile p00, BiomeAtmosphereProfile p10, BiomeAtmosphereProfile p20, BiomeAtmosphereProfile p30,
            BiomeAtmosphereProfile p01, BiomeAtmosphereProfile p11, BiomeAtmosphereProfile p21, BiomeAtmosphereProfile p31,
            BiomeAtmosphereProfile p02, BiomeAtmosphereProfile p12, BiomeAtmosphereProfile p22, BiomeAtmosphereProfile p32,
            BiomeAtmosphereProfile p03, BiomeAtmosphereProfile p13, BiomeAtmosphereProfile p23, BiomeAtmosphereProfile p33,
            double wx0, double wx1, double wx2, double wx3,
            double wz0, double wz1, double wz2, double wz3
    ) {
        double rowPresence0 = gravityFlag(p00) * wx0 + gravityFlag(p10) * wx1 + gravityFlag(p20) * wx2 + gravityFlag(p30) * wx3;
        double rowPresence1 = gravityFlag(p01) * wx0 + gravityFlag(p11) * wx1 + gravityFlag(p21) * wx2 + gravityFlag(p31) * wx3;
        double rowPresence2 = gravityFlag(p02) * wx0 + gravityFlag(p12) * wx1 + gravityFlag(p22) * wx2 + gravityFlag(p32) * wx3;
        double rowPresence3 = gravityFlag(p03) * wx0 + gravityFlag(p13) * wx1 + gravityFlag(p23) * wx2 + gravityFlag(p33) * wx3;

        double gravityPresence = rowPresence0 * wz0 + rowPresence1 * wz1 + rowPresence2 * wz2 + rowPresence3 * wz3;
        if (gravityPresence <= 1.0E-9D) {
            return 1.0F;
        }

        double rowDelta0 = gravityDelta(p00) * wx0 + gravityDelta(p10) * wx1 + gravityDelta(p20) * wx2 + gravityDelta(p30) * wx3;
        double rowDelta1 = gravityDelta(p01) * wx0 + gravityDelta(p11) * wx1 + gravityDelta(p21) * wx2 + gravityDelta(p31) * wx3;
        double rowDelta2 = gravityDelta(p02) * wx0 + gravityDelta(p12) * wx1 + gravityDelta(p22) * wx2 + gravityDelta(p32) * wx3;
        double rowDelta3 = gravityDelta(p03) * wx0 + gravityDelta(p13) * wx1 + gravityDelta(p23) * wx2 + gravityDelta(p33) * wx3;

        double weightedDelta = rowDelta0 * wz0 + rowDelta1 * wz1 + rowDelta2 * wz2 + rowDelta3 * wz3;
        if (Math.abs(weightedDelta) <= 1.0E-9D) {
            return 1.0F;
        }

        double clampedPresence = clamp01(gravityPresence);
        // Equivalent to easing spatial presence weight via (1 - B) * e + B * hermiteSmoothstep(e):
        // Dividing by e algebraically yields (1 - B) + B * e * (3 - 2e) with zero division-by-zero risk!
        double smoothScale = (1.0D - HERMITE_EASE_BLEND)
                + HERMITE_EASE_BLEND * clampedPresence * (3.0D - 2.0D * clampedPresence);
        return (float) (1.0D + weightedDelta * smoothScale);
    }

    private static double accumulateJumpPresence(
            BiomeAtmosphereProfile p00, BiomeAtmosphereProfile p10, BiomeAtmosphereProfile p20, BiomeAtmosphereProfile p30,
            BiomeAtmosphereProfile p01, BiomeAtmosphereProfile p11, BiomeAtmosphereProfile p21, BiomeAtmosphereProfile p31,
            BiomeAtmosphereProfile p02, BiomeAtmosphereProfile p12, BiomeAtmosphereProfile p22, BiomeAtmosphereProfile p32,
            BiomeAtmosphereProfile p03, BiomeAtmosphereProfile p13, BiomeAtmosphereProfile p23, BiomeAtmosphereProfile p33,
            double wx0, double wx1, double wx2, double wx3,
            double wz0, double wz1, double wz2, double wz3
    ) {
        double r0 = jumpFlag(p00) * wx0 + jumpFlag(p10) * wx1 + jumpFlag(p20) * wx2 + jumpFlag(p30) * wx3;
        double r1 = jumpFlag(p01) * wx0 + jumpFlag(p11) * wx1 + jumpFlag(p21) * wx2 + jumpFlag(p31) * wx3;
        double r2 = jumpFlag(p02) * wx0 + jumpFlag(p12) * wx1 + jumpFlag(p22) * wx2 + jumpFlag(p32) * wx3;
        double r3 = jumpFlag(p03) * wx0 + jumpFlag(p13) * wx1 + jumpFlag(p23) * wx2 + jumpFlag(p33) * wx3;
        return r0 * wz0 + r1 * wz1 + r2 * wz2 + r3 * wz3;
    }

    private static double accumulateWeightedJumpAmplifier(
            BiomeAtmosphereProfile p00, BiomeAtmosphereProfile p10, BiomeAtmosphereProfile p20, BiomeAtmosphereProfile p30,
            BiomeAtmosphereProfile p01, BiomeAtmosphereProfile p11, BiomeAtmosphereProfile p21, BiomeAtmosphereProfile p31,
            BiomeAtmosphereProfile p02, BiomeAtmosphereProfile p12, BiomeAtmosphereProfile p22, BiomeAtmosphereProfile p32,
            BiomeAtmosphereProfile p03, BiomeAtmosphereProfile p13, BiomeAtmosphereProfile p23, BiomeAtmosphereProfile p33,
            double wx0, double wx1, double wx2, double wx3,
            double wz0, double wz1, double wz2, double wz3
    ) {
        double r0 = jumpAmp(p00) * wx0 + jumpAmp(p10) * wx1 + jumpAmp(p20) * wx2 + jumpAmp(p30) * wx3;
        double r1 = jumpAmp(p01) * wx0 + jumpAmp(p11) * wx1 + jumpAmp(p21) * wx2 + jumpAmp(p31) * wx3;
        double r2 = jumpAmp(p02) * wx0 + jumpAmp(p12) * wx1 + jumpAmp(p22) * wx2 + jumpAmp(p32) * wx3;
        double r3 = jumpAmp(p03) * wx0 + jumpAmp(p13) * wx1 + jumpAmp(p23) * wx2 + jumpAmp(p33) * wx3;
        return r0 * wz0 + r1 * wz1 + r2 * wz2 + r3 * wz3;
    }

    private static double accumulateRegenPresence(
            BiomeAtmosphereProfile p00, BiomeAtmosphereProfile p10, BiomeAtmosphereProfile p20, BiomeAtmosphereProfile p30,
            BiomeAtmosphereProfile p01, BiomeAtmosphereProfile p11, BiomeAtmosphereProfile p21, BiomeAtmosphereProfile p31,
            BiomeAtmosphereProfile p02, BiomeAtmosphereProfile p12, BiomeAtmosphereProfile p22, BiomeAtmosphereProfile p32,
            BiomeAtmosphereProfile p03, BiomeAtmosphereProfile p13, BiomeAtmosphereProfile p23, BiomeAtmosphereProfile p33,
            double wx0, double wx1, double wx2, double wx3,
            double wz0, double wz1, double wz2, double wz3
    ) {
        double r0 = regenFlag(p00) * wx0 + regenFlag(p10) * wx1 + regenFlag(p20) * wx2 + regenFlag(p30) * wx3;
        double r1 = regenFlag(p01) * wx0 + regenFlag(p11) * wx1 + regenFlag(p21) * wx2 + regenFlag(p31) * wx3;
        double r2 = regenFlag(p02) * wx0 + regenFlag(p12) * wx1 + regenFlag(p22) * wx2 + regenFlag(p32) * wx3;
        double r3 = regenFlag(p03) * wx0 + regenFlag(p13) * wx1 + regenFlag(p23) * wx2 + regenFlag(p33) * wx3;
        return r0 * wz0 + r1 * wz1 + r2 * wz2 + r3 * wz3;
    }

    private static double accumulateWeightedRegenInterval(
            BiomeAtmosphereProfile p00, BiomeAtmosphereProfile p10, BiomeAtmosphereProfile p20, BiomeAtmosphereProfile p30,
            BiomeAtmosphereProfile p01, BiomeAtmosphereProfile p11, BiomeAtmosphereProfile p21, BiomeAtmosphereProfile p31,
            BiomeAtmosphereProfile p02, BiomeAtmosphereProfile p12, BiomeAtmosphereProfile p22, BiomeAtmosphereProfile p32,
            BiomeAtmosphereProfile p03, BiomeAtmosphereProfile p13, BiomeAtmosphereProfile p23, BiomeAtmosphereProfile p33,
            double wx0, double wx1, double wx2, double wx3,
            double wz0, double wz1, double wz2, double wz3
    ) {
        double r0 = regenInterval(p00) * wx0 + regenInterval(p10) * wx1 + regenInterval(p20) * wx2 + regenInterval(p30) * wx3;
        double r1 = regenInterval(p01) * wx0 + regenInterval(p11) * wx1 + regenInterval(p21) * wx2 + regenInterval(p31) * wx3;
        double r2 = regenInterval(p02) * wx0 + regenInterval(p12) * wx1 + regenInterval(p22) * wx2 + regenInterval(p32) * wx3;
        double r3 = regenInterval(p03) * wx0 + regenInterval(p13) * wx1 + regenInterval(p23) * wx2 + regenInterval(p33) * wx3;
        return r0 * wz0 + r1 * wz1 + r2 * wz2 + r3 * wz3;
    }

    private static double accumulateCustomTickPresence(
            BiomeAtmosphereProfile p00, BiomeAtmosphereProfile p10, BiomeAtmosphereProfile p20, BiomeAtmosphereProfile p30,
            BiomeAtmosphereProfile p01, BiomeAtmosphereProfile p11, BiomeAtmosphereProfile p21, BiomeAtmosphereProfile p31,
            BiomeAtmosphereProfile p02, BiomeAtmosphereProfile p12, BiomeAtmosphereProfile p22, BiomeAtmosphereProfile p32,
            BiomeAtmosphereProfile p03, BiomeAtmosphereProfile p13, BiomeAtmosphereProfile p23, BiomeAtmosphereProfile p33,
            double wx0, double wx1, double wx2, double wx3,
            double wz0, double wz1, double wz2, double wz3
    ) {
        double r0 = customTickFlag(p00) * wx0 + customTickFlag(p10) * wx1 + customTickFlag(p20) * wx2 + customTickFlag(p30) * wx3;
        double r1 = customTickFlag(p01) * wx0 + customTickFlag(p11) * wx1 + customTickFlag(p21) * wx2 + customTickFlag(p31) * wx3;
        double r2 = customTickFlag(p02) * wx0 + customTickFlag(p12) * wx1 + customTickFlag(p22) * wx2 + customTickFlag(p32) * wx3;
        double r3 = customTickFlag(p03) * wx0 + customTickFlag(p13) * wx1 + customTickFlag(p23) * wx2 + customTickFlag(p33) * wx3;
        return r0 * wz0 + r1 * wz1 + r2 * wz2 + r3 * wz3;
    }

    private static BiomeAtmosphereProfile sampleProfileAt(
            Level level, BlockPos.MutableBlockPos scratchPos, int x, int y, int z) {
        Holder<Biome> holder = level.getBiome(scratchPos.set(x, y, z));
        return EntropicaBiomeAtmospheres.getForHolder(holder);
    }

    private static int gridBlockCoord(int gridIndex) {
        return gridIndex * 6 + 3;
    }

    private static double clamp01(double v) {
        return v < 0.0D ? 0.0D : (v > 1.0D ? 1.0D : v);
    }

    private static double bspline3Weight0(double f) {
        double inv = 1.0D - f;
        return (inv * inv * inv) / 6.0D;
    }

    private static double bspline3Weight1(double f) {
        double f2 = f * f;
        return (3.0D * f2 * f - 6.0D * f2 + 4.0D) / 6.0D;
    }

    private static double bspline3Weight2(double f) {
        double f2 = f * f;
        return (-3.0D * f2 * f + 3.0D * f2 + 3.0D * f + 1.0D) / 6.0D;
    }

    private static double bspline3Weight3(double f) {
        return (f * f * f) / 6.0D;
    }

    private static double gravityFlag(BiomeAtmosphereProfile p) {
        return (p != null && Math.abs(p.getDescentSpeedMultiplier() - 1.0F) > 1.0E-4F) ? 1.0D : 0.0D;
    }

    private static double gravityDelta(BiomeAtmosphereProfile p) {
        if (p == null) {
            return 0.0D;
        }
        float mult = p.getDescentSpeedMultiplier();
        return Math.abs(mult - 1.0F) > 1.0E-4F ? ((double) mult - 1.0D) : 0.0D;
    }

    private static double jumpFlag(BiomeAtmosphereProfile p) {
        return (p != null && p.getJumpBoostAmplifier() >= 0) ? 1.0D : 0.0D;
    }

    private static double jumpAmp(BiomeAtmosphereProfile p) {
        return (p != null && p.getJumpBoostAmplifier() >= 0) ? (double) p.getJumpBoostAmplifier() : 0.0D;
    }

    private static double regenFlag(BiomeAtmosphereProfile p) {
        return (p != null && p.getPassiveRegenIntervalTicks() > 0) ? 1.0D : 0.0D;
    }

    private static double regenInterval(BiomeAtmosphereProfile p) {
        return (p != null && p.getPassiveRegenIntervalTicks() > 0) ? (double) p.getPassiveRegenIntervalTicks() : 0.0D;
    }

    private static double customTickFlag(BiomeAtmosphereProfile p) {
        return (p != null && p.getCustomServerTick() != null) ? 1.0D : 0.0D;
    }

    private static boolean isEligibleForGravityScaling(LivingEntity entity) {
        if (entity.onGround() || entity.isInWater() || entity.isInLava() || entity.onClimbable() || entity.isNoGravity()) {
            return false;
        }
        if (entity.isFallFlying() || entity.hasEffect(MobEffects.SLOW_FALLING) || entity.hasEffect(MobEffects.LEVITATION)) {
            return false;
        }
        if (entity instanceof Player player && player.getAbilities().flying) {
            return false;
        }
        return true;
    }

    /**
     * Universal server-side head-in-{@code entropica:mirage_ice} check (active across ALL biomes):
     * <ul>
     *   <li>If {@code !phased} (solid {@code Mirage Ice} enclosing the entity's eye position), applies
     *       immediate {@code inWall} suffocation ({@code 2.0F}) with zero air-supply wait.</li>
     *   <li>In both solid and phased states, unless protected by {@code entropica:mirage_veil_goggles}
     *       on {@link EquipmentSlot#HEAD} or a {@code entropica:mirage_prism_lantern} within {@code 12}
     *       blocks (checked on initial entry and every {@code 10} ticks), applies
     *       {@code entropica:double_vision} ({@code 200} ticks, amplifier {@code 0}).</li>
     * </ul>
     */
    private static void tickMirageIceHeadHazard(ServerLevel level, LivingEntity entity) {
        BlockPos eyePos = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
        BlockState eyeState = level.getBlockState(eyePos);
        if (eyeState.isAir() || !MIRAGE_ICE_ID.equals(BuiltInRegistries.BLOCK.getKey(eyeState.getBlock()))) {
            if (!LAST_MIRAGE_ICE_EYE_TICK.isEmpty()) {
                LAST_MIRAGE_ICE_EYE_TICK.remove(entity.getUUID());
            }
            return;
        }

        boolean phased = isMirageIcePhased(eyeState);
        if (!phased) {
            entity.hurtServer(level, level.damageSources().inWall(), 2.0F);
        }

        long gameTime = level.getGameTime();
        Long prevTick = LAST_MIRAGE_ICE_EYE_TICK.put(entity.getUUID(), gameTime);
        boolean initialEntry = (prevTick == null || gameTime - prevTick > 1L);

        if (isWearingMirageVeilGoggles(entity)) {
            return;
        }

        Optional<MobEffect> doubleVisionOpt = BuiltInRegistries.MOB_EFFECT.getOptional(DOUBLE_VISION_ID);
        if (doubleVisionOpt.isEmpty()) {
            return;
        }

        if (initialEntry || entity.tickCount % 10 == 0) {
            if (!isNearMiragePrismLantern(level, eyePos, 12)) {
                entity.addEffect(new MobEffectInstance(
                        BuiltInRegistries.MOB_EFFECT.wrapAsHolder(doubleVisionOpt.get()),
                        200,
                        0,
                        true,
                        true,
                        true
                ));
            }
        }
    }

    private static boolean isMirageIcePhased(BlockState state) {
        for (Property<?> prop : state.getProperties()) {
            if ("phased".equals(prop.getName()) && prop instanceof BooleanProperty boolProp) {
                return Boolean.TRUE.equals(state.getValue(boolProp));
            }
        }
        return false;
    }

    public static boolean isWearingMirageVeilGoggles(LivingEntity entity) {
        ItemStack headStack = entity.getItemBySlot(EquipmentSlot.HEAD);
        return !headStack.isEmpty()
                && MIRAGE_VEIL_GOGGLES_ID.equals(BuiltInRegistries.ITEM.getKey(headStack.getItem()));
    }

    public static boolean isNearMiragePrismLantern(ServerLevel level, BlockPos center, int radius) {
        Optional<Block> lanternOpt = BuiltInRegistries.BLOCK.getOptional(MIRAGE_PRISM_LANTERN_ID);
        if (lanternOpt.isEmpty()) {
            return false;
        }
        Block lanternBlock = lanternOpt.get();
        int radiusSqr = radius * radius;
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();
        int minY = Math.max(level.getMinY(), cy - radius);
        int maxY = Math.min(level.getMaxY() - 1, cy + radius);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int dx = -radius; dx <= radius; dx++) {
            int dx2 = dx * dx;
            int bx = cx + dx;
            for (int dz = -radius; dz <= radius; dz++) {
                int horizSqr = dx2 + dz * dz;
                if (horizSqr > radiusSqr) {
                    continue;
                }
                int bz = cz + dz;
                if (!level.hasChunk(bx >> 4, bz >> 4)) {
                    continue;
                }
                int maxDy = (int) Math.floor(Math.sqrt(radiusSqr - horizSqr));
                int y0 = Math.max(minY, cy - maxDy);
                int y1 = Math.min(maxY, cy + maxDy);
                for (int by = y0; by <= y1; by++) {
                    cursor.set(bx, by, bz);
                    if (level.getBlockState(cursor).is(lanternBlock)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
