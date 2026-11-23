package ddraig.net.entropica.entity.void_rift_shade;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.item.biome2.CamouflageCloakItem;
import ddraig.net.entropica.item.biome6.VaporShroudCloakItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Fauna 42 — Void-Rift Shade (entropica:void_rift_shade).
 * Tall, neutral dimensional rift-lurker native to 2.9 Null-Fringe Bay:
 * - Neutral Rift-Lurker: stalks slowly along Void Sand shores and floating obsidian shards;
 *   becomes hostile if attacked OR if an uncloaked survival player (without Null-Weave Mantle)
 *   lingers within 3.0 blocks (or stares closely within 3.5 blocks) for > 3 seconds.
 * - Upward Rift Claw Slash (attack): deals 8.0F melee damage, triggers attackAnimationState,
 *   and pops the target upward (+0.58D Y velocity) with REVERSE_PORTAL particles.
 * - Dimensional Rift Blink (blink_out -> blink_in): when hurt by a ranged attack or repositioning
 *   in combat (120-tick cooldown), UNLESS within 24 blocks of a spatial_phase_anchor,
 *   blinks 4–8 blocks onto dry solid ground near its target with PORTAL / REVERSE_PORTAL particles
 *   and SoundEvents.ENDERMAN_TELEPORT.
 */
public class VoidRiftShadeEntity extends PathfinderMob implements Enemy {

    private static final EntityDataAccessor<Boolean> PROVOKED =
            SynchedEntityData.defineId(VoidRiftShadeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> RIFT_STABILIZED =
            SynchedEntityData.defineId(VoidRiftShadeEntity.class, EntityDataSerializers.BOOLEAN);

    private static final byte EVENT_RIFT_SLASH = 4;
    private static final byte EVENT_BLINK_OUT = 61;
    private static final byte EVENT_BLINK_IN = 62;
    private static final byte EVENT_AGGRO = 64;
    private static final byte EVENT_HURT_FLINCH = 65;
    private static final byte EVENT_HURT_STAGGER = 66;
    private static final byte EVENT_DEATH_COLLAPSE = 67;

    public static final int BLINK_COOLDOWN_TICKS = 120;
    public static final double STABILIZER_SUPPRESSION_RADIUS = 24.0D;
    private static final int BLINK_OUT_PHASE_TICKS = 10;
    private static final int STABILIZER_SCAN_INTERVAL_TICKS = 20;
    private static final int AGGRO_COOLDOWN_TICKS = 140;
    private static final int STAGGER_COOLDOWN_TICKS = 80;
    private static final int PROVOCATION_LINGER_THRESHOLD_TICKS = 60;

    private static final ResourceLocation VOID_SAND_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "void_sand");
    private static final ResourceLocation VOID_SAND_TURF_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "void_sand_turf");
    private static final ResourceLocation VOID_STONE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "void_stone");
    private static final ResourceLocation OBSIDIAN_BRICKS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "obsidian_bricks");
    private static final ResourceLocation VOID_CRYSTAL_BLOCK_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "void_crystal_block");
    private static final ResourceLocation NULL_FRINGE_TUSSOCK_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "null_fringe_tussock");
    private static final ResourceLocation SPATIAL_PHASE_ANCHOR_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spatial_phase_anchor");
    private static final ResourceLocation NULL_WEAVE_MANTLE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "null_weave_mantle");

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState blinkOutAnimationState = new AnimationState();
    public final AnimationState blinkInAnimationState = new AnimationState();
    public final AnimationState aggroAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState hurtStaggerAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    private int blinkCooldown = 0;
    private int pendingBlinkTicks = 0;
    @Nullable
    private Vec3 pendingBlinkPos = null;
    private int aggroAnimCooldown = 0;
    private int staggerCooldown = 0;

    public VoidRiftShadeEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 12;
        this.setPathfindingMalus(PathType.WATER, 8.0F);
        this.setPathfindingMalus(PathType.POWDER_SNOW, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_POWDER_SNOW, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 225.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    private static boolean matchesBlock(BlockState state, ResourceLocation id) {
        if (state.isAir()) {
            return false;
        }
        Optional<Block> opt = BuiltInRegistries.BLOCK.getOptional(id);
        return opt.isPresent() && state.is(opt.get());
    }

    private static boolean isThinNullFringeCover(BlockState state) {
        if (state.is(Blocks.SNOW)) {
            return true;
        }
        if (matchesBlock(state, NULL_FRINGE_TUSSOCK_ID)) {
            return true;
        }
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return "null_fringe_tussock".equals(path)
                || "blue_frozen_moss_carpet".equals(path)
                || "grave_moss".equals(path);
    }

    public static boolean isVoidRiftShadeSpawnSurface(BlockGetter level, BlockPos belowPos, BlockState belowState) {
        if (belowState.isAir()) {
            return false;
        }
        return matchesBlock(belowState, VOID_SAND_ID)
                || matchesBlock(belowState, VOID_SAND_TURF_ID)
                || matchesBlock(belowState, VOID_STONE_ID)
                || matchesBlock(belowState, OBSIDIAN_BRICKS_ID)
                || matchesBlock(belowState, VOID_CRYSTAL_BLOCK_ID)
                || belowState.is(Blocks.OBSIDIAN)
                || belowState.is(Blocks.CRYING_OBSIDIAN)
                || belowState.is(Blocks.BLUE_ICE)
                || belowState.is(Blocks.PACKED_ICE)
                || belowState.is(Blocks.SNOW_BLOCK);
    }

    public static boolean checkVoidRiftShadeSpawnRules(
            EntityType<VoidRiftShadeEntity> type,
            ServerLevelAccessor level,
            EntitySpawnReason reason,
            BlockPos pos,
            RandomSource random
    ) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        if (EntitySpawnReason.isSpawner(reason)
                || reason == EntitySpawnReason.SPAWN_ITEM_USE
                || reason == EntitySpawnReason.COMMAND) {
            return true;
        }

        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        if (isThinNullFringeCover(below) && level.getBlockState(belowPos.below()).blocksMotion()) {
            belowPos = belowPos.below();
            below = level.getBlockState(belowPos);
        }

        return isVoidRiftShadeSpawnSurface(level, belowPos, below);
    }

    /**
     * Checks whether {@code pos} lies within {@code radius} blocks of a placed
     * {@code entropica:spatial_phase_anchor} using chunk-section palette skipping.
     */
    public static boolean isWithinStabilizedRiftField(ServerLevel level, BlockPos pos, double radius) {
        if (level == null || pos == null || radius <= 0.0D) {
            return false;
        }
        Optional<Block> anchorOpt = BuiltInRegistries.BLOCK.getOptional(SPATIAL_PHASE_ANCHOR_ID);
        if (anchorOpt.isEmpty()) {
            return false;
        }
        Block anchorBlock = anchorOpt.get();
        double maxDistSq = radius * radius;
        int r = Mth.ceil(radius);

        int minChunkX = (pos.getX() - r) >> 4;
        int maxChunkX = (pos.getX() + r) >> 4;
        int minChunkZ = (pos.getZ() - r) >> 4;
        int maxChunkZ = (pos.getZ() + r) >> 4;
        int minY = Math.max(level.getMinY(), pos.getY() - r);
        int maxY = Math.min(level.getMaxY() - 1, pos.getY() + r);
        if (minY > maxY) {
            return false;
        }

        int minSecIdx = level.getSectionIndex(minY);
        int maxSecIdx = level.getSectionIndex(maxY);

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                if (!level.hasChunk(cx, cz)) {
                    continue;
                }
                LevelChunk chunk = level.getChunk(cx, cz);
                LevelChunkSection[] sections = chunk.getSections();
                for (int secIdx = Math.max(0, minSecIdx); secIdx <= Math.min(sections.length - 1, maxSecIdx); secIdx++) {
                    LevelChunkSection section = sections[secIdx];
                    if (section == null || section.hasOnlyAir()
                            || !section.maybeHas(s -> s.is(anchorBlock))) {
                        continue;
                    }
                    int baseX = cx << 4;
                    int baseY = level.getSectionYFromSectionIndex(secIdx) << 4;
                    int baseZ = cz << 4;
                    for (int ly = 0; ly < 16; ly++) {
                        int wy = baseY + ly;
                        int dy = wy - pos.getY();
                        if (dy * dy > maxDistSq) {
                            continue;
                        }
                        for (int lx = 0; lx < 16; lx++) {
                            int wx = baseX + lx;
                            int dx = wx - pos.getX();
                            if (dx * dx + dy * dy > maxDistSq) {
                                continue;
                            }
                            for (int lz = 0; lz < 16; lz++) {
                                BlockState s = section.getBlockState(lx, ly, lz);
                                if (s.is(anchorBlock)) {
                                    int wz = baseZ + lz;
                                    int dz = wz - pos.getZ();
                                    if (dx * dx + dy * dy + dz * dz <= maxDistSq) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean isWearingNullWeaveMantle(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        Optional<Item> mantleOpt = BuiltInRegistries.ITEM.getOptional(NULL_WEAVE_MANTLE_ID);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (mantleOpt.isPresent() && stack.is(mantleOpt.get())) {
                return true;
            }
            if ("null_weave_mantle".equals(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPlayerCloakedFromShade(Player player) {
        if (player == null || !player.isAlive() || player.isCreative() || player.isSpectator()) {
            return true;
        }
        return isWearingNullWeaveMantle(player)
                || CamouflageCloakItem.hasCamouflageCloak(player)
                || VaporShroudCloakItem.isWearingVaporShroudCloak(player)
                || player.isInvisible();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PROVOKED, false);
        builder.define(RIFT_STABILIZED, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new DimensionalRiftBlinkCombatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.18D, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.82D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new CloseProximityProvocationGoal(this));
    }

    public boolean isProvoked() {
        return this.entityData.get(PROVOKED);
    }

    public void setProvoked(boolean provoked) {
        this.entityData.set(PROVOKED, provoked);
    }

    public boolean isRiftStabilized() {
        return this.entityData.get(RIFT_STABILIZED);
    }

    public void setRiftStabilized(boolean stabilized) {
        this.entityData.set(RIFT_STABILIZED, stabilized);
    }

    @Override
    public SoundSource getSoundSource() {
        return this.isProvoked() ? SoundSource.HOSTILE : SoundSource.NEUTRAL;
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        // Immune to fall damage across jagged obsidian shards and void cliffs
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        if (source.is(DamageTypeTags.IS_FREEZING)
                || source.is(DamageTypes.FREEZE)
                || source.is(DamageTypeTags.IS_FALL)
                || source.is(DamageTypes.FALL)) {
            return true;
        }
        return super.isInvulnerableTo(level, source);
    }

    @Override
    public void makeStuckInBlock(BlockState state, Vec3 motionMultiplier) {
        if (state.is(Blocks.POWDER_SNOW)
                || state.is(Blocks.SNOW)
                || state.getBlock() instanceof SnowLayerBlock
                || matchesBlock(state, NULL_FRINGE_TUSSOCK_ID)) {
            return;
        }
        super.makeStuckInBlock(state, motionMultiplier);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        BlockState below = level.getBlockState(pos.below());
        if (matchesBlock(below, VOID_SAND_ID)
                || matchesBlock(below, VOID_SAND_TURF_ID)
                || matchesBlock(below, OBSIDIAN_BRICKS_ID)
                || below.is(Blocks.OBSIDIAN)) {
            return 10.0F;
        }
        return 0.0F;
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        LivingEntity previousTarget = this.getTarget();
        super.setTarget(target);
        if (!this.level().isClientSide()) {
            this.setProvoked(target != null && target.isAlive());
            if (this.level() instanceof ServerLevel serverLevel
                    && target != null
                    && target.isAlive()
                    && previousTarget == null
                    && this.aggroAnimCooldown <= 0) {
                this.aggroAnimCooldown = AGGRO_COOLDOWN_TICKS;
                serverLevel.broadcastEntityEvent(this, EVENT_AGGRO);
                serverLevel.playSound(
                        null,
                        this.getX(),
                        this.getY() + 2.4D,
                        this.getZ(),
                        SoundEvents.ENDERMAN_SCREAM,
                        SoundSource.HOSTILE,
                        1.05F,
                        0.6F
                );
                serverLevel.sendParticles(
                        ParticleTypes.REVERSE_PORTAL,
                        this.getX(),
                        this.getY() + 2.2D,
                        this.getZ(),
                        16,
                        0.45D,
                        0.65D,
                        0.45D,
                        0.06D
                );
            }
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        level.broadcastEntityEvent(this, EVENT_RIFT_SLASH);

        if (hit) {
            double knockResist = (target instanceof LivingEntity living)
                    ? Mth.clamp(living.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.0D, 0.85D)
                    : 0.0D;
            double upwardVel = 0.58D * (1.0D - knockResist * 0.5D);
            Vec3 cur = target.getDeltaMovement();
            target.setDeltaMovement(cur.x * 0.65D, Math.max(cur.y + 0.25D, upwardVel), cur.z * 0.65D);
            target.hasImpulse = true;
            target.hurtMarked = true;

            level.playSound(
                    null,
                    this.getX(),
                    this.getY() + 1.8D,
                    this.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP,
                    SoundSource.HOSTILE,
                    1.0F,
                    0.55F
            );
            level.playSound(
                    null,
                    target.getX(),
                    target.getY(),
                    target.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.HOSTILE,
                    0.75F,
                    1.35F
            );
            level.sendParticles(
                    ParticleTypes.REVERSE_PORTAL,
                    target.getX(),
                    target.getY() + target.getBbHeight() * 0.5D,
                    target.getZ(),
                    22,
                    0.38D,
                    0.45D,
                    0.38D,
                    0.12D
            );
        }
        return hit;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.isInvulnerableTo(level, source)) {
            return false;
        }

        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && this.isAlive()) {
            boolean rangedOrIndirect = source.is(DamageTypeTags.IS_PROJECTILE)
                    || (source.getEntity() != null && source.getDirectEntity() != source.getEntity())
                    || (source.getEntity() instanceof LivingEntity attacker && this.distanceToSqr(attacker) > 30.25D);

            boolean anchored = isWithinStabilizedRiftField(level, this.blockPosition(), STABILIZER_SUPPRESSION_RADIUS);
            this.setRiftStabilized(anchored);

            LivingEntity blinkRef = this.getTarget();
            if (blinkRef == null && source.getEntity() instanceof LivingEntity srcLiving && srcLiving.isAlive()) {
                if (!(srcLiving instanceof Player p && (p.isCreative() || p.isSpectator()))) {
                    this.setTarget(srcLiving);
                    blinkRef = srcLiving;
                }
            }

            if (rangedOrIndirect
                    && !anchored
                    && this.blinkCooldown <= 0
                    && this.pendingBlinkTicks <= 0
                    && this.tryInitiateRiftBlink(level, blinkRef)) {
                return true;
            }

            if ((amount >= 10.0F || (rangedOrIndirect && anchored)) && this.staggerCooldown <= 0) {
                this.staggerCooldown = STAGGER_COOLDOWN_TICKS;
                level.broadcastEntityEvent(this, EVENT_HURT_STAGGER);
            } else {
                level.broadcastEntityEvent(this, EVENT_HURT_FLINCH);
            }
        }
        return hurt;
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.broadcastEntityEvent(this, EVENT_DEATH_COLLAPSE);
            serverLevel.sendParticles(
                    ParticleTypes.PORTAL,
                    this.getX(),
                    this.getY() + 1.8D,
                    this.getZ(),
                    28,
                    0.5D,
                    0.9D,
                    0.5D,
                    0.25D
            );
        }
        super.die(damageSource);
    }

    @Override
    protected void tickDeath() {
        ++this.deathTime;
        if (this.deathTime >= 95 && !this.level().isClientSide() && !this.isRemoved()) {
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.REVERSE_PORTAL,
                        this.getX(),
                        this.getY() + 1.8D,
                        this.getZ(),
                        24,
                        0.35D,
                        0.45D,
                        0.35D,
                        0.08D
                );
            }
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    /**
     * Initiates a Dimensional Rift Blink (blink_out -> teleport -> blink_in) 4–8 blocks onto dry solid ground
     * near {@code referenceTarget} (or relative to self if null), unless suppressed by a nearby Spatial Phase Anchor.
     */
    public boolean tryInitiateRiftBlink(ServerLevel level, @Nullable LivingEntity referenceTarget) {
        if (this.blinkCooldown > 0 || this.pendingBlinkTicks > 0 || !this.isAlive()) {
            return false;
        }
        if (isWithinStabilizedRiftField(level, this.blockPosition(), STABILIZER_SUPPRESSION_RADIUS)) {
            this.setRiftStabilized(true);
            return false;
        }

        Vec3 dest = findDryBlinkDestination(level, referenceTarget);
        if (dest == null) {
            return false;
        }

        this.blinkCooldown = BLINK_COOLDOWN_TICKS;
        this.pendingBlinkPos = dest;
        this.pendingBlinkTicks = BLINK_OUT_PHASE_TICKS;
        this.getNavigation().stop();

        level.broadcastEntityEvent(this, EVENT_BLINK_OUT);
        level.playSound(
                null,
                this.getX(),
                this.getY() + 1.6D,
                this.getZ(),
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.HOSTILE,
                1.0F,
                0.78F
        );
        level.sendParticles(
                ParticleTypes.PORTAL,
                this.getX(),
                this.getY() + 1.8D,
                this.getZ(),
                26,
                0.45D,
                0.95D,
                0.45D,
                0.3D
        );
        level.sendParticles(
                ParticleTypes.REVERSE_PORTAL,
                this.getX(),
                this.getY() + 1.8D,
                this.getZ(),
                18,
                0.35D,
                0.75D,
                0.35D,
                0.08D
        );
        return true;
    }

    private void completePendingRiftBlink(ServerLevel level) {
        Vec3 dest = this.pendingBlinkPos;
        this.pendingBlinkPos = null;
        if (dest == null || !this.isAlive()) {
            return;
        }
        if (isWithinStabilizedRiftField(level, this.blockPosition(), STABILIZER_SUPPRESSION_RADIUS)) {
            this.setRiftStabilized(true);
            return;
        }

        if (!isValidDryStandPosition(level, dest)) {
            dest = findDryBlinkDestination(level, this.getTarget());
            if (dest == null) {
                return;
            }
        }

        double oldX = this.getX();
        double oldY = this.getY();
        double oldZ = this.getZ();

        this.teleportTo(dest.x, dest.y, dest.z);
        this.resetFallDistance();
        this.getNavigation().stop();

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            double dx = target.getX() - dest.x;
            double dz = target.getZ() - dest.z;
            float yaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
            this.setYRot(yaw);
            this.yBodyRot = yaw;
            this.yHeadRot = yaw;
        }

        level.sendParticles(
                ParticleTypes.REVERSE_PORTAL,
                oldX,
                oldY + 1.6D,
                oldZ,
                16,
                0.35D,
                0.7D,
                0.35D,
                0.06D
        );
        level.sendParticles(
                ParticleTypes.PORTAL,
                dest.x,
                dest.y + 1.8D,
                dest.z,
                30,
                0.48D,
                0.95D,
                0.48D,
                0.32D
        );
        level.sendParticles(
                ParticleTypes.REVERSE_PORTAL,
                dest.x,
                dest.y + 1.8D,
                dest.z,
                20,
                0.4D,
                0.8D,
                0.4D,
                0.1D
        );
        level.playSound(
                null,
                dest.x,
                dest.y + 1.6D,
                dest.z,
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.HOSTILE,
                1.1F,
                1.12F
        );
        level.broadcastEntityEvent(this, EVENT_BLINK_IN);
    }

    @Nullable
    private Vec3 findDryBlinkDestination(ServerLevel level, @Nullable LivingEntity target) {
        Vec3 anchor = (target != null && target.isAlive()) ? target.position() : this.position();
        for (int attempt = 0; attempt < 28; attempt++) {
            double dist = 4.0D + this.random.nextDouble() * 4.0D; // 4.0 - 8.0 blocks
            double angle = this.random.nextDouble() * (Math.PI * 2.0D);

            // First 18 attempts position around target (at least 2.2 blocks from target & 4.0-8.0 blocks from self or target)
            Vec3 base = (attempt < 20 && target != null) ? anchor : this.position();
            double cx = base.x + Math.cos(angle) * dist;
            double cz = base.z + Math.sin(angle) * dist;

            // Ensure actual blink distance from current Shade position is within ~3.5-9.5 blocks (or 4-8 when blinking from self)
            double selfHorizDistSq = (cx - this.getX()) * (cx - this.getX()) + (cz - this.getZ()) * (cz - this.getZ());
            if (selfHorizDistSq < 12.25D) {
                continue;
            }
            if (target != null) {
                double targetDistSq = (cx - target.getX()) * (cx - target.getX()) + (cz - target.getZ()) * (cz - target.getZ());
                if (targetDistSq < 4.0D || targetDistSq > 68.0D) {
                    continue;
                }
            }

            int bx = Mth.floor(cx);
            int bz = Mth.floor(cz);
            int refY = Mth.floor(base.y);

            for (int dy = 4; dy >= -6; dy--) {
                BlockPos feetPos = new BlockPos(bx, refY + dy, bz);
                if (!level.isLoaded(feetPos)) {
                    continue;
                }
                Vec3 candidate = new Vec3(bx + 0.5D, feetPos.getY(), bz + 0.5D);
                if (isValidDryStandPosition(level, candidate)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private boolean isValidDryStandPosition(ServerLevel level, Vec3 candidate) {
        BlockPos feetPos = BlockPos.containing(candidate.x, candidate.y, candidate.z);
        BlockPos belowPos = feetPos.below();
        BlockState belowState = level.getBlockState(belowPos);
        if (!belowState.blocksMotion()
                || !belowState.getFluidState().isEmpty()
                || !belowState.isFaceSturdy(level, belowPos, Direction.UP)) {
            return false;
        }
        for (int h = 0; h <= 3; h++) {
            BlockPos check = feetPos.above(h);
            BlockState s = level.getBlockState(check);
            if (!s.getFluidState().isEmpty()) {
                return false;
            }
        }
        AABB box = this.getType().getSpawnAABB(candidate.x, candidate.y, candidate.z);
        return level.getWorldBorder().isWithinBounds(box) && level.noCollision(this, box);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_RIFT_SLASH) {
            this.hurtAnimationState.stop();
            this.attackAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_BLINK_OUT) {
            this.attackAnimationState.stop();
            this.aggroAnimationState.stop();
            this.hurtAnimationState.stop();
            this.blinkInAnimationState.stop();
            this.blinkOutAnimationState.start(this.tickCount);
            spawnClientRiftParticles(ParticleTypes.REVERSE_PORTAL, 14);
            return;
        }
        if (id == EVENT_BLINK_IN) {
            this.blinkOutAnimationState.stop();
            this.blinkInAnimationState.start(this.tickCount);
            spawnClientRiftParticles(ParticleTypes.PORTAL, 18);
            return;
        }
        if (id == EVENT_AGGRO) {
            this.hurtAnimationState.stop();
            this.aggroAnimationState.start(this.tickCount);
            spawnClientRiftParticles(ParticleTypes.REVERSE_PORTAL, 12);
            return;
        }
        if (id == EVENT_HURT_FLINCH) {
            if (!this.attackAnimationState.isStarted()
                    && !this.blinkOutAnimationState.isStarted()
                    && !this.blinkInAnimationState.isStarted()
                    && !this.hurtStaggerAnimationState.isStarted()) {
                this.hurtAnimationState.start(this.tickCount);
            }
            return;
        }
        if (id == EVENT_HURT_STAGGER) {
            this.attackAnimationState.stop();
            this.hurtAnimationState.stop();
            this.hurtStaggerAnimationState.start(this.tickCount);
            spawnClientRiftParticles(ParticleTypes.PORTAL, 10);
            return;
        }
        if (id == EVENT_DEATH_COLLAPSE) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.attackAnimationState.stop();
            this.blinkOutAnimationState.stop();
            this.blinkInAnimationState.stop();
            this.aggroAnimationState.stop();
            this.hurtAnimationState.stop();
            this.hurtStaggerAnimationState.stop();
            this.deathAnimationState.start(this.tickCount);
            return;
        }
        super.handleEntityEvent(id);
    }

    private void spawnClientRiftParticles(net.minecraft.core.particles.SimpleParticleType type, int count) {
        for (int i = 0; i < count; i++) {
            this.level().addParticle(
                    type,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 1.1D,
                    this.getY() + 0.6D + this.random.nextDouble() * 2.6D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 1.1D,
                    (this.random.nextDouble() - 0.5D) * 0.15D,
                    (this.random.nextDouble() - 0.5D) * 0.12D,
                    (this.random.nextDouble() - 0.5D) * 0.15D
            );
        }
    }

    @Override
    public void aiStep() {
        if (this.getTicksFrozen() > 0) {
            this.setTicksFrozen(0);
        }

        super.aiStep();

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            if (this.blinkCooldown > 0) {
                this.blinkCooldown--;
            }
            if (this.aggroAnimCooldown > 0) {
                this.aggroAnimCooldown--;
            }
            if (this.staggerCooldown > 0) {
                this.staggerCooldown--;
            }

            if (this.pendingBlinkTicks > 0) {
                this.pendingBlinkTicks--;
                this.getNavigation().stop();
                if (this.pendingBlinkTicks == 0) {
                    this.completePendingRiftBlink(serverLevel);
                }
            }

            if (this.tickCount <= 1 || (this.tickCount + this.getId()) % STABILIZER_SCAN_INTERVAL_TICKS == 0) {
                boolean stabilized = isWithinStabilizedRiftField(serverLevel, this.blockPosition(), STABILIZER_SUPPRESSION_RADIUS);
                if (stabilized != this.isRiftStabilized()) {
                    this.setRiftStabilized(stabilized);
                }
            }

            LivingEntity target = this.getTarget();
            boolean activeTarget = target != null && target.isAlive()
                    && !(target instanceof Player p && (p.isCreative() || p.isSpectator()));
            if (!activeTarget && target != null) {
                this.setTarget(null);
            } else if (activeTarget != this.isProvoked()) {
                this.setProvoked(activeTarget);
            }
        } else if (this.level().isClientSide()) {
            this.setupClientAnimationsAndParticles();
        }
    }

    private void setupClientAnimationsAndParticles() {
        if (this.isDeadOrDying() || this.deathTime > 0) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.deathAnimationState.startIfStopped(this.tickCount);
            if (this.deathTime < 85 && this.tickCount % 3 == 0) {
                this.level().addParticle(
                        ParticleTypes.REVERSE_PORTAL,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.8D,
                        this.getY() + 1.2D + this.random.nextDouble() * 1.6D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.8D,
                        0.0D,
                        0.03D,
                        0.0D
                );
            }
            return;
        }

        if (this.attackAnimationState.isStarted() && this.attackAnimationState.getTimeInMillis(this.tickCount) >= 1500L) {
            this.attackAnimationState.stop();
        }
        if (this.blinkOutAnimationState.isStarted() && this.blinkOutAnimationState.getTimeInMillis(this.tickCount) >= 1000L) {
            this.blinkOutAnimationState.stop();
        }
        if (this.blinkInAnimationState.isStarted() && this.blinkInAnimationState.getTimeInMillis(this.tickCount) >= 1000L) {
            this.blinkInAnimationState.stop();
        }
        if (this.aggroAnimationState.isStarted() && this.aggroAnimationState.getTimeInMillis(this.tickCount) >= 1600L) {
            this.aggroAnimationState.stop();
        }
        if (this.hurtAnimationState.isStarted() && this.hurtAnimationState.getTimeInMillis(this.tickCount) >= 650L) {
            this.hurtAnimationState.stop();
        }
        if (this.hurtStaggerAnimationState.isStarted() && this.hurtStaggerAnimationState.getTimeInMillis(this.tickCount) >= 2400L) {
            this.hurtStaggerAnimationState.stop();
        }

        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
        if (moving) {
            this.idleAnimationState.stop();
            this.walkAnimationState.startIfStopped(this.tickCount);
        } else {
            this.walkAnimationState.stop();
            this.idleAnimationState.startIfStopped(this.tickCount);
        }

        if (this.random.nextFloat() < (this.isProvoked() ? 0.35F : 0.16F)) {
            this.level().addParticle(
                    this.isRiftStabilized() ? ParticleTypes.ASH : ParticleTypes.PORTAL,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.9D,
                    this.getY() + 0.8D + this.random.nextDouble() * 2.5D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.9D,
                    (this.random.nextDouble() - 0.5D) * 0.12D,
                    -0.04D,
                    (this.random.nextDouble() - 0.5D) * 0.12D
            );
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("BlinkCooldown", Codec.INT, this.blinkCooldown);
        output.store("Provoked", Codec.BOOL, this.isProvoked());
        output.store("RiftStabilized", Codec.BOOL, this.isRiftStabilized());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.blinkCooldown = input.read("BlinkCooldown", Codec.INT).orElse(0);
        this.setProvoked(input.read("Provoked", Codec.BOOL).orElse(false));
        this.setRiftStabilized(input.read("RiftStabilized", Codec.BOOL).orElse(false));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isProvoked() ? SoundEvents.ENDERMAN_STARE : SoundEvents.ENDERMAN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ENDERMAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDERMAN_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.12F + 0.62F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.WARDEN_TENDRIL_CLICKS, 0.18F, 0.75F);
    }

    // --- CUSTOM GOALS ---

    /**
     * Provokes the neutral Void-Rift Shade if an uncloaked survival player (without Null-Weave Mantle)
     * lingers within 3.0 blocks (or approaches/stares within 3.5 blocks) for > 3 seconds (60 ticks).
     */
    static class CloseProximityProvocationGoal extends Goal {
        private final VoidRiftShadeEntity shade;
        private final Map<UUID, Integer> lingerTicksByPlayer = new HashMap<>();
        @Nullable
        private Player provokedTarget;

        public CloseProximityProvocationGoal(VoidRiftShadeEntity shade) {
            this.shade = shade;
            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (this.shade.getTarget() != null && this.shade.getTarget().isAlive()) {
                return false;
            }
            if (!(this.shade.level() instanceof ServerLevel serverLevel)
                    || serverLevel.getDifficulty() == Difficulty.PEACEFUL) {
                this.lingerTicksByPlayer.clear();
                return false;
            }

            List<Player> nearbyPlayers = serverLevel.getEntitiesOfClass(
                    Player.class,
                    this.shade.getBoundingBox().inflate(4.0D),
                    p -> p.isAlive() && !p.isCreative() && !p.isSpectator()
            );

            if (nearbyPlayers.isEmpty()) {
                if (!this.lingerTicksByPlayer.isEmpty()) {
                    decayAllCounters(null);
                }
                return false;
            }

            Player triggered = null;
            for (Player player : nearbyPlayers) {
                if (isPlayerCloakedFromShade(player)) {
                    this.lingerTicksByPlayer.remove(player.getUUID());
                    continue;
                }

                double distSq = this.shade.distanceToSqr(player);
                boolean withinThreeBlocks = distSq <= 9.0D; // <= 3.0 blocks
                boolean staringWithinThreePointFive = distSq <= 12.25D && isPlayerLookingAtShade(player);

                if (withinThreeBlocks || staringWithinThreePointFive) {
                    int next = this.lingerTicksByPlayer.getOrDefault(player.getUUID(), 0) + 1;
                    this.lingerTicksByPlayer.put(player.getUUID(), next);

                    if (next == 25 || next == 45) {
                        serverLevel.sendParticles(
                                ParticleTypes.REVERSE_PORTAL,
                                this.shade.getX(),
                                this.shade.getY() + 2.5D,
                                this.shade.getZ(),
                                5,
                                0.25D,
                                0.3D,
                                0.25D,
                                0.03D
                        );
                    }

                    if (next > PROVOCATION_LINGER_THRESHOLD_TICKS) {
                        triggered = player;
                        break;
                    }
                } else {
                    int prev = this.lingerTicksByPlayer.getOrDefault(player.getUUID(), 0);
                    if (prev <= 2) {
                        this.lingerTicksByPlayer.remove(player.getUUID());
                    } else {
                        this.lingerTicksByPlayer.put(player.getUUID(), prev - 2);
                    }
                }
            }

            decayAllCounters(nearbyPlayers);

            if (triggered != null) {
                this.provokedTarget = triggered;
                return true;
            }
            return false;
        }

        private boolean isPlayerLookingAtShade(Player player) {
            Vec3 look = player.getViewVector(1.0F).normalize();
            Vec3 toShadeHead = new Vec3(
                    this.shade.getX() - player.getX(),
                    (this.shade.getY() + 2.8D) - player.getEyeY(),
                    this.shade.getZ() - player.getZ()
            );
            double len = toShadeHead.length();
            if (len < 1.0E-4D) {
                return true;
            }
            return look.dot(toShadeHead.scale(1.0D / len)) > 0.82D && this.shade.hasLineOfSight(player);
        }

        private void decayAllCounters(@Nullable List<Player> activePlayers) {
            Iterator<Map.Entry<UUID, Integer>> it = this.lingerTicksByPlayer.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, Integer> entry = it.next();
                boolean present = false;
                if (activePlayers != null) {
                    for (Player p : activePlayers) {
                        if (p.getUUID().equals(entry.getKey())) {
                            present = true;
                            break;
                        }
                    }
                }
                if (!present) {
                    int next = entry.getValue() - 2;
                    if (next <= 0) {
                        it.remove();
                    } else {
                        entry.setValue(next);
                    }
                }
            }
        }

        @Override
        public void start() {
            if (this.provokedTarget != null && this.provokedTarget.isAlive()) {
                this.lingerTicksByPlayer.remove(this.provokedTarget.getUUID());
                this.shade.setTarget(this.provokedTarget);
            }
            this.provokedTarget = null;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }
    }

    /**
     * Executes combat repositioning blinks (120-tick cooldown) onto dry ground 4–8 blocks near its target,
     * unless suppressed within 24 blocks of a Spatial Phase Anchor.
     */
    static class DimensionalRiftBlinkCombatGoal extends Goal {
        private final VoidRiftShadeEntity shade;

        public DimensionalRiftBlinkCombatGoal(VoidRiftShadeEntity shade) {
            this.shade = shade;
        }

        @Override
        public boolean canUse() {
            if (!(this.shade.level() instanceof ServerLevel serverLevel)) {
                return false;
            }
            if (this.shade.blinkCooldown > 0 || this.shade.pendingBlinkTicks > 0 || this.shade.isRiftStabilized()) {
                return false;
            }
            LivingEntity target = this.shade.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }
            double distSq = this.shade.distanceToSqr(target);
            boolean gapCloseOrWater = distSq > 42.25D || this.shade.isInWater();
            boolean flankReposition = distSq >= 12.25D && this.shade.getRandom().nextInt(18) == 0;
            return gapCloseOrWater || flankReposition;
        }

        @Override
        public void start() {
            if (this.shade.level() instanceof ServerLevel serverLevel) {
                this.shade.tryInitiateRiftBlink(serverLevel, this.shade.getTarget());
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.shade.pendingBlinkTicks > 0;
        }
    }
}
