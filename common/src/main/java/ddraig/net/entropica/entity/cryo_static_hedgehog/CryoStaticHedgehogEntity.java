package ddraig.net.entropica.entity.cryo_static_hedgehog;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Optional;

/**
 * Fauna 21 — Cryo-Static Hedgehog (Needle-Quill Rodent).
 * Passive polar skitterer native to 2.7 Static Ice-Plains & 3.21 Shaded Ridge:
 * - Fast ground skittering, seeking shelter under nearby cryo_static_shrub bushes.
 * - Defensive Static Curl: when touched by a hostile mob or non-sneaking player or hurt,
 *   curls into a spiky static ball for 60 ticks (3s), gaining +8 armor & 50% damage reduction
 *   and discharging 3.0F lightning damage + +100 freeze ticks + Slowness II (40t) + ELECTRIC_SPARK burst.
 * - Snowstorm / Weather Static Charge: crackles with cyan ELECTRIC_SPARK particles during rain/snowfall/thunder.
 * - Quill Shedding & Drops: colliding horizontally with a solid block while curled sheds 1x static_quill
 *   (2400-tick cooldown); drops 1-2x static_quill (+ looting) on death.
 */
public class CryoStaticHedgehogEntity extends PathfinderMob {

    private static final EntityDataAccessor<Boolean> CURLED =
            SynchedEntityData.defineId(CryoStaticHedgehogEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> STORM_CHARGED =
            SynchedEntityData.defineId(CryoStaticHedgehogEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SHELTERING =
            SynchedEntityData.defineId(CryoStaticHedgehogEntity.class, EntityDataSerializers.BOOLEAN);

    private static final byte EVENT_STATIC_CURL = 4;
    private static final byte EVENT_STATIC_CURL_REFRESH = 5;
    private static final byte EVENT_SNEEZE = 6;
    private static final byte EVENT_DIG_IN = 7;
    private static final byte EVENT_DIG_OUT = 8;

    private static final int CURL_DURATION_TICKS = 60;
    private static final int DISCHARGE_COOLDOWN_TICKS = 15;
    private static final int QUILL_SHED_COOLDOWN_TICKS = 2400;

    private static final ResourceLocation CRYO_STATIC_SHRUB_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "cryo_static_shrub");
    private static final ResourceLocation FULGURITE_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "fulgurite_ice");
    private static final ResourceLocation STATIC_QUILL_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "static_quill");

    private static final ResourceLocation CURL_ARMOR_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "cryo_static_hedgehog_curl_armor");
    private static final AttributeModifier CURL_ARMOR_MODIFIER = new AttributeModifier(
            CURL_ARMOR_MODIFIER_ID,
            8.0D,
            AttributeModifier.Operation.ADD_VALUE
    );

    private static final TagKey<Biome> C_IS_SNOWY =
            TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("c", "is_snowy"));
    private static final TagKey<Biome> C_IS_ICY =
            TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("c", "is_icy"));
    private static final TagKey<Biome> C_IS_COLD =
            TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("c", "is_cold"));

    private static final DustParticleOptions CYAN_STATIC_DUST = new DustParticleOptions(0x06B6D4, 0.55F);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState curlAnimationState = new AnimationState();
    public final AnimationState sneezeAnimationState = new AnimationState();
    public final AnimationState digInAnimationState = new AnimationState();
    public final AnimationState digHoldAnimationState = new AnimationState();
    public final AnimationState digOutAnimationState = new AnimationState();

    private int curlTicks = 0;
    private int dischargeCooldown = 0;
    private int quillShedCooldown = 0;
    private int lightningBoostTicks = 0;
    private int ambientSneezeTimer = 240;
    private boolean clientPrevSheltering = false;

    public CryoStaticHedgehogEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(PathType.POWDER_SNOW, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_POWDER_SNOW, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.STEP_HEIGHT, 1.0D)
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    public static boolean isCryoStaticShrub(BlockState state) {
        if (state.isAir()) {
            return false;
        }
        Optional<Block> shrub = BuiltInRegistries.BLOCK.getOptional(CRYO_STATIC_SHRUB_ID);
        return shrub.isPresent() && state.is(shrub.get());
    }

    public static boolean isCryoStaticHedgehogSpawnSurface(BlockState state) {
        if (state.isAir()) {
            return false;
        }
        if (state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.SNOW)
                || state.is(Blocks.ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.COARSE_DIRT)) {
            return true;
        }
        Optional<Block> fulguriteIce = BuiltInRegistries.BLOCK.getOptional(FULGURITE_ICE_ID);
        return fulguriteIce.isPresent() && state.is(fulguriteIce.get());
    }

    private static boolean isPolarBiome(ServerLevelAccessor level, BlockPos pos) {
        Holder<Biome> biomeHolder = level.getBiome(pos);
        if (biomeHolder.is(C_IS_SNOWY) || biomeHolder.is(C_IS_ICY) || biomeHolder.is(C_IS_COLD)) {
            return true;
        }
        Biome biome = biomeHolder.value();
        if (biome.coldEnoughToSnow(pos, level.getSeaLevel()) || biome.getBaseTemperature() < 0.25F) {
            return true;
        }
        return biomeHolder.unwrapKey()
                .map(key -> {
                    String path = key.location().getPath().toLowerCase(Locale.ROOT);
                    return path.contains("ice")
                            || path.contains("glacier")
                            || path.contains("frost")
                            || path.contains("skerries")
                            || path.contains("fjord")
                            || path.contains("polar")
                            || path.contains("snow")
                            || "shaded_ridge".equals(path);
                })
                .orElse(false);
    }

    public static boolean checkCryoStaticHedgehogSpawnRules(
            EntityType<? extends PathfinderMob> type,
            ServerLevelAccessor level,
            EntitySpawnReason spawnReason,
            BlockPos pos,
            RandomSource random
    ) {
        if (EntitySpawnReason.isSpawner(spawnReason)
                || spawnReason == EntitySpawnReason.SPAWN_ITEM_USE
                || spawnReason == EntitySpawnReason.COMMAND) {
            return true;
        }

        if (!isPolarBiome(level, pos) && level.getRawBrightness(pos, 0) < 7) {
            return false;
        }

        BlockState atPos = level.getBlockState(pos);
        if (atPos.is(Blocks.SNOW) && isCryoStaticHedgehogSpawnSurface(level.getBlockState(pos.below()))) {
            return true;
        }

        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        if (isCryoStaticHedgehogSpawnSurface(below)) {
            return true;
        }
        if ((isCryoStaticShrub(below) || !below.blocksMotion())
                && isCryoStaticHedgehogSpawnSurface(level.getBlockState(belowPos.below()))) {
            return true;
        }
        return false;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        BlockPos belowPos = this.blockPosition().below();
        if (isCryoStaticHedgehogSpawnSurface(level.getBlockState(belowPos))
                || isCryoStaticHedgehogSpawnSurface(level.getBlockState(this.blockPosition()))) {
            return true;
        }
        return super.checkSpawnRules(level, spawnReason);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (isCryoStaticShrub(level.getBlockState(pos.offset(dx, dy, dz)))) {
                        return 12.0F;
                    }
                }
            }
        }
        if (isCryoStaticHedgehogSpawnSurface(level.getBlockState(pos.below()))) {
            return 4.0F;
        }
        return 0.0F;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CURLED, false);
        builder.define(STORM_CHARGED, false);
        builder.define(SHELTERING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.35D));
        this.goalSelector.addGoal(2, new SeekCryoStaticShrubGoal(this, 1.08D, 12));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D, 0.004F));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 7.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    public boolean isCurled() {
        return this.entityData.get(CURLED);
    }

    public void setCurled(boolean curled) {
        this.entityData.set(CURLED, curled);
        if (!this.level().isClientSide()) {
            this.updateCurlArmorModifier(curled);
        }
    }

    public boolean isStormCharged() {
        return this.entityData.get(STORM_CHARGED);
    }

    public void setStormCharged(boolean charged) {
        this.entityData.set(STORM_CHARGED, charged);
    }

    public boolean isSheltering() {
        return this.entityData.get(SHELTERING);
    }

    public void setSheltering(boolean sheltering) {
        this.entityData.set(SHELTERING, sheltering);
    }

    private void updateCurlArmorModifier(boolean curled) {
        AttributeInstance armorAttr = this.getAttribute(Attributes.ARMOR);
        if (armorAttr == null) {
            return;
        }
        if (curled) {
            if (!armorAttr.hasModifier(CURL_ARMOR_MODIFIER_ID)) {
                armorAttr.addTransientModifier(CURL_ARMOR_MODIFIER);
            }
        } else {
            armorAttr.removeModifier(CURL_ARMOR_MODIFIER_ID);
        }
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        if (source.is(DamageTypeTags.IS_FREEZING)
                || source.is(DamageTypes.FREEZE)
                || source.is(DamageTypeTags.IS_LIGHTNING)
                || source.is(DamageTypes.LIGHTNING_BOLT)) {
            return true;
        }
        return super.isInvulnerableTo(level, source);
    }

    @Override
    public void thunderHit(ServerLevel level, LightningBolt lightning) {
        this.clearFire();
        this.lightningBoostTicks = 200;
        this.setStormCharged(true);
        level.playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.NEUTRAL,
                1.0F,
                1.35F
        );
        level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                this.getX(),
                this.getY() + 0.35D,
                this.getZ(),
                20,
                0.35D,
                0.25D,
                0.35D,
                0.18D
        );
    }

    @Override
    public void makeStuckInBlock(BlockState state, Vec3 motionMultiplier) {
        if (state.is(Blocks.POWDER_SNOW)
                || state.is(Blocks.SNOW)
                || state.getBlock() instanceof SnowLayerBlock
                || isCryoStaticShrub(state)) {
            return;
        }
        super.makeStuckInBlock(state, motionMultiplier);
    }

    @Override
    protected float getBlockSpeedFactor() {
        BlockState state = this.level().getBlockState(this.blockPosition());
        if (state.is(Blocks.POWDER_SNOW) || state.is(Blocks.SNOW) || state.getBlock() instanceof SnowLayerBlock) {
            return 1.0F;
        }
        return super.getBlockSpeedFactor();
    }

    private boolean isThreateningToucher(Entity other) {
        if (other == this || !other.isAlive() || other instanceof CryoStaticHedgehogEntity) {
            return false;
        }
        if (other instanceof Player player) {
            return !player.isCreative()
                    && !player.isSpectator()
                    && !player.isShiftKeyDown()
                    && !player.isCrouching();
        }
        if (other instanceof Enemy) {
            return true;
        }
        return other instanceof Mob mob && mob.getTarget() == this;
    }

    private void triggerDefensiveCurlAndDischarge(ServerLevel level, @Nullable LivingEntity target, boolean forceDischarge) {
        boolean alreadyCurled = this.isCurled();
        this.curlTicks = CURL_DURATION_TICKS;
        this.setSheltering(false);
        this.setCurled(true);

        level.broadcastEntityEvent(this, alreadyCurled ? EVENT_STATIC_CURL_REFRESH : EVENT_STATIC_CURL);

        // Give a crisp skitter-roll recoil away from the threat so curled hedgehogs can roll into nearby blocks
        if (target != null && target != this) {
            Vec3 away = this.position().subtract(target.position());
            Vec3 horiz = new Vec3(away.x, 0.0D, away.z);
            if (horiz.lengthSqr() > 1.0E-4D) {
                Vec3 dir = horiz.normalize();
                Vec3 curVel = this.getDeltaMovement();
                this.setDeltaMovement(
                        curVel.x * 0.4D + dir.x * 0.32D,
                        Math.max(curVel.y, this.onGround() ? 0.14D : curVel.y),
                        curVel.z * 0.4D + dir.z * 0.32D
                );
                this.hasImpulse = true;
            }
        }

        if (target != null
                && target.isAlive()
                && !(target instanceof CryoStaticHedgehogEntity)
                && !(target instanceof Player p && (p.isCreative() || p.isSpectator()))
                && (forceDischarge || this.dischargeCooldown <= 0)) {
            this.dischargeCooldown = DISCHARGE_COOLDOWN_TICKS;
            dischargeStaticQuills(level, target);
        } else if (!alreadyCurled) {
            level.playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.NEUTRAL,
                    0.85F,
                    1.45F
            );
            level.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    this.getX(),
                    this.getY() + 0.35D,
                    this.getZ(),
                    16,
                    0.35D,
                    0.25D,
                    0.35D,
                    0.15D
            );
        }
    }

    private void dischargeStaticQuills(ServerLevel level, LivingEntity target) {
        target.hurtServer(level, this.damageSources().lightningBolt(), 3.0F);
        if (target.canFreeze()) {
            int maxFreeze = Math.max(target.getTicksRequiredToFreeze() + 60, 240);
            target.setTicksFrozen(Math.min(maxFreeze, target.getTicksFrozen() + 100));
        }
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1));

        level.playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.PLAYER_HURT_FREEZE,
                SoundSource.NEUTRAL,
                0.9F,
                1.35F
        );
        level.playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.AMETHYST_CLUSTER_HIT,
                SoundSource.NEUTRAL,
                0.85F,
                1.5F
        );
        level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                this.getX(),
                this.getY() + 0.35D,
                this.getZ(),
                24,
                0.4D,
                0.3D,
                0.4D,
                0.2D
        );
        level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                target.getX(),
                target.getY() + target.getBbHeight() * 0.5D,
                target.getZ(),
                12,
                0.3D,
                0.35D,
                0.3D,
                0.15D
        );
        level.sendParticles(
                ParticleTypes.SNOWFLAKE,
                target.getX(),
                target.getY() + target.getBbHeight() * 0.5D,
                target.getZ(),
                10,
                0.3D,
                0.3D,
                0.3D,
                0.05D
        );
    }

    @Override
    public void playerTouch(Player player) {
        super.playerTouch(player);
        if (!this.level().isClientSide()
                && this.level() instanceof ServerLevel serverLevel
                && isThreateningToucher(player)) {
            triggerDefensiveCurlAndDischarge(serverLevel, player, false);
        }
    }

    @Override
    public void push(Entity other) {
        super.push(other);
        if (!this.level().isClientSide()
                && this.level() instanceof ServerLevel serverLevel
                && other instanceof LivingEntity living
                && isThreateningToucher(living)) {
            triggerDefensiveCurlAndDischarge(serverLevel, living, false);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.isInvulnerableTo(level, source)) {
            return false;
        }

        if (this.isCurled() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            amount *= 0.5F;
        }

        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && this.isAlive()) {
            LivingEntity attacker = null;
            if (source.getDirectEntity() instanceof LivingEntity directLiving) {
                attacker = directLiving;
            } else if (source.getEntity() instanceof LivingEntity sourceLiving
                    && this.distanceToSqr(sourceLiving) <= 16.0D) {
                attacker = sourceLiving;
            }
            triggerDefensiveCurlAndDischarge(level, attacker, true);
        }
        return hurt;
    }

    private boolean isCollidingWithSolidBlock() {
        AABB expanded = this.getBoundingBox().inflate(0.08D, 0.0D, 0.08D);
        int minX = Mth.floor(expanded.minX);
        int maxX = Mth.floor(expanded.maxX);
        int minY = Mth.floor(expanded.minY + 0.05D);
        int maxY = Mth.floor(expanded.maxY);
        int minZ = Mth.floor(expanded.minZ);
        int maxZ = Mth.floor(expanded.maxZ);

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    cursor.set(x, y, z);
                    BlockState state = this.level().getBlockState(cursor);
                    if (!state.isAir()
                            && state.blocksMotion()
                            && !(state.getBlock() instanceof SnowLayerBlock)
                            && !state.is(Blocks.POWDER_SNOW)) {
                        return true;
                    }
                }
            }
        }
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockState adj = this.level().getBlockState(this.blockPosition().relative(dir));
            if (!adj.isAir() && adj.blocksMotion() && !(adj.getBlock() instanceof SnowLayerBlock)) {
                return true;
            }
        }
        return false;
    }

    private void tryShedStaticQuill(ServerLevel level) {
        this.quillShedCooldown = QUILL_SHED_COOLDOWN_TICKS;
        Optional<Item> quillItem = BuiltInRegistries.ITEM.getOptional(STATIC_QUILL_ID);
        quillItem.ifPresent(item -> this.spawnAtLocation(level, new ItemStack(item, 1), 0.25F));

        level.playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.BUNDLE_DROP_CONTENTS,
                SoundSource.NEUTRAL,
                0.95F,
                1.15F
        );
        level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                this.getX(),
                this.getY() + 0.35D,
                this.getZ(),
                10,
                0.25D,
                0.2D,
                0.25D,
                0.12D
        );
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_STATIC_CURL) {
            this.sneezeAnimationState.stop();
            this.digInAnimationState.stop();
            this.digHoldAnimationState.stop();
            this.digOutAnimationState.stop();
            this.curlAnimationState.start(this.tickCount);
            spawnClientStaticBurst(16);
            return;
        }
        if (id == EVENT_STATIC_CURL_REFRESH) {
            this.curlAnimationState.start(Math.max(0, this.tickCount - 8));
            spawnClientStaticBurst(12);
            return;
        }
        if (id == EVENT_SNEEZE) {
            this.sneezeAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_DIG_IN) {
            this.digOutAnimationState.stop();
            this.digInAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_DIG_OUT) {
            this.digInAnimationState.stop();
            this.digHoldAnimationState.stop();
            this.digOutAnimationState.start(this.tickCount);
            return;
        }
        super.handleEntityEvent(id);
    }

    private void spawnClientStaticBurst(int count) {
        for (int i = 0; i < count; i++) {
            double vx = (this.random.nextDouble() - 0.5D) * 0.35D;
            double vy = 0.05D + this.random.nextDouble() * 0.2D;
            double vz = (this.random.nextDouble() - 0.5D) * 0.35D;
            this.level().addParticle(
                    ParticleTypes.ELECTRIC_SPARK,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.55D,
                    this.getY() + 0.25D + this.random.nextDouble() * 0.35D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.55D,
                    vx,
                    vy,
                    vz
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
            if (this.dischargeCooldown > 0) {
                this.dischargeCooldown--;
            }
            if (this.quillShedCooldown > 0) {
                this.quillShedCooldown--;
            }
            if (this.lightningBoostTicks > 0) {
                this.lightningBoostTicks--;
            }

            if (this.curlTicks > 0) {
                this.curlTicks--;
                if (this.curlTicks <= 0) {
                    this.setCurled(false);
                }
            } else if (this.isCurled()) {
                this.setCurled(false);
            }

            // Weather / snowstorm static charge update
            boolean weatherCharged = serverLevel.isRaining() || serverLevel.isThundering() || this.lightningBoostTicks > 0;
            if (weatherCharged != this.isStormCharged()) {
                this.setStormCharged(weatherCharged);
            }

            if (weatherCharged && this.tickCount % 8 == 0) {
                serverLevel.sendParticles(
                        ParticleTypes.ELECTRIC_SPARK,
                        this.getX(),
                        this.getY() + 0.42D,
                        this.getZ(),
                        2,
                        0.26D,
                        0.16D,
                        0.26D,
                        0.04D
                );
            }

            // Shed 1x static_quill when curled and colliding horizontally with a solid block
            if (this.isCurled()
                    && this.quillShedCooldown <= 0
                    && this.horizontalCollision
                    && this.isCollidingWithSolidBlock()) {
                this.tryShedStaticQuill(serverLevel);
            }

            // Occasional cute icy sneeze when idle & uncurled
            if (!this.isCurled()
                    && !this.isSheltering()
                    && this.getDeltaMovement().horizontalDistanceSqr() <= 1.0E-4D) {
                this.ambientSneezeTimer--;
                if (this.ambientSneezeTimer <= 0) {
                    this.ambientSneezeTimer = 260 + this.random.nextInt(280);
                    serverLevel.broadcastEntityEvent(this, EVENT_SNEEZE);
                }
            }
        } else if (this.level().isClientSide()) {
            this.setupClientAnimationsAndParticles();
        }
    }

    private void setupClientAnimationsAndParticles() {
        boolean curled = this.isCurled();
        boolean sheltering = this.isSheltering();
        boolean stormCharged = this.isStormCharged() || this.level().isRaining() || this.level().isThundering();
        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;

        if (sheltering && !this.clientPrevSheltering && !curled) {
            this.digOutAnimationState.stop();
            this.digInAnimationState.start(this.tickCount);
        } else if (!sheltering && this.clientPrevSheltering && !curled) {
            this.digInAnimationState.stop();
            this.digHoldAnimationState.stop();
            this.digOutAnimationState.start(this.tickCount);
        }
        this.clientPrevSheltering = sheltering;

        if (curled) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.sneezeAnimationState.stop();
            this.digInAnimationState.stop();
            this.digHoldAnimationState.stop();
            this.digOutAnimationState.stop();
            this.curlAnimationState.startIfStopped(this.tickCount);
        } else {
            if (this.curlAnimationState.getTimeInMillis(this.tickCount) >= 3000L) {
                this.curlAnimationState.stop();
            }
            if (this.sneezeAnimationState.getTimeInMillis(this.tickCount) >= 1400L) {
                this.sneezeAnimationState.stop();
            }
            if (this.digOutAnimationState.getTimeInMillis(this.tickCount) >= 1200L) {
                this.digOutAnimationState.stop();
            }

            if (sheltering) {
                this.idleAnimationState.stop();
                this.walkAnimationState.stop();
                if (this.digInAnimationState.getTimeInMillis(this.tickCount) >= 1300L) {
                    this.digInAnimationState.stop();
                    this.digHoldAnimationState.startIfStopped(this.tickCount);
                } else if (!this.digInAnimationState.isStarted()) {
                    this.digHoldAnimationState.startIfStopped(this.tickCount);
                }
            } else if (moving) {
                this.idleAnimationState.stop();
                this.digHoldAnimationState.stop();
                this.walkAnimationState.startIfStopped(this.tickCount);
            } else {
                this.walkAnimationState.stop();
                this.digHoldAnimationState.stop();
                this.idleAnimationState.startIfStopped(this.tickCount);
            }
        }

        // Cyan ELECTRIC_SPARK quill crackle during snowstorms/rain/thunder or while curled
        if (stormCharged || curled) {
            int interval = curled ? 2 : (this.level().isThundering() ? 2 : 4);
            if (this.tickCount % interval == 0) {
                double qx = this.getX() + (this.random.nextDouble() - 0.5D) * 0.55D;
                double qy = this.getY() + 0.36D + this.random.nextDouble() * 0.34D;
                double qz = this.getZ() + (this.random.nextDouble() - 0.5D) * 0.65D;
                this.level().addParticle(
                        ParticleTypes.ELECTRIC_SPARK,
                        qx,
                        qy,
                        qz,
                        (this.random.nextDouble() - 0.5D) * 0.06D,
                        0.02D + this.random.nextDouble() * 0.05D,
                        (this.random.nextDouble() - 0.5D) * 0.06D
                );
                if (stormCharged && this.random.nextBoolean()) {
                    this.level().addParticle(CYAN_STATIC_DUST, qx, qy, qz, 0.0D, 0.01D, 0.0D);
                }
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        int looting = 0;
        if (damageSource.getEntity() instanceof LivingEntity attacker) {
            var enchantmentLookup = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            var lootingHolder = enchantmentLookup.get(Enchantments.LOOTING);
            if (lootingHolder.isPresent()) {
                looting = EnchantmentHelper.getItemEnchantmentLevel(lootingHolder.get(), attacker.getMainHandItem());
            }
        }
        int count = 1 + this.random.nextInt(2) + (looting > 0 ? this.random.nextInt(looting + 1) : 0);
        BuiltInRegistries.ITEM.getOptional(STATIC_QUILL_ID)
                .ifPresent(item -> this.spawnAtLocation(level, new ItemStack(item, count)));
        int essenceCount = 2 + this.random.nextInt(2) + (looting > 0 ? this.random.nextInt(looting + 1) : 0);
        ItemStack staticEssence = new ItemStack(ddraig.net.entropica.registry.ModItems.AVERAGE_ESSENCE.get(), essenceCount);
        ddraig.net.entropica.item.EssenceItem.setEssenceType(staticEssence, ddraig.net.entropica.api.EssenceType.STATIC);
        this.spawnAtLocation(level, staticEssence);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("CurlTicks", Codec.INT, this.curlTicks);
        output.store("QuillShedCooldown", Codec.INT, this.quillShedCooldown);
        output.store("Curled", Codec.BOOL, this.isCurled());
        output.store("StormCharged", Codec.BOOL, this.isStormCharged());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.curlTicks = input.read("CurlTicks", Codec.INT).orElse(0);
        this.quillShedCooldown = input.read("QuillShedCooldown", Codec.INT).orElse(0);
        this.setCurled(input.read("Curled", Codec.BOOL).orElse(this.curlTicks > 0));
        this.setStormCharged(input.read("StormCharged", Codec.BOOL).orElse(false));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.FOX_SNIFF;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.RABBIT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RABBIT_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.RABBIT_JUMP, 0.15F, 1.45F);
    }

    /**
     * Seeks out nearby `entropica:cryo_static_shrub` blocks to skitter under and shelter beneath.
     */
    static class SeekCryoStaticShrubGoal extends Goal {
        private final CryoStaticHedgehogEntity hedgehog;
        private final double speedModifier;
        private final int searchRadius;
        private @Nullable BlockPos targetShrubPos;
        private int tryTicks;
        private int shelterTicks;
        private int searchCooldown;

        SeekCryoStaticShrubGoal(CryoStaticHedgehogEntity hedgehog, double speedModifier, int searchRadius) {
            this.hedgehog = hedgehog;
            this.speedModifier = speedModifier;
            this.searchRadius = searchRadius;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.hedgehog.isCurled() || this.hedgehog.getLastHurtByMob() != null) {
                return false;
            }
            if (this.searchCooldown > 0) {
                this.searchCooldown--;
                return false;
            }
            this.searchCooldown = 50 + this.hedgehog.getRandom().nextInt(60);
            if (BuiltInRegistries.BLOCK.getOptional(CRYO_STATIC_SHRUB_ID).isEmpty()) {
                return false;
            }
            this.targetShrubPos = this.findNearbyShrub();
            return this.targetShrubPos != null;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.hedgehog.isCurled() || this.hedgehog.getLastHurtByMob() != null || this.targetShrubPos == null) {
                return false;
            }
            if (!isCryoStaticShrub(this.hedgehog.level().getBlockState(this.targetShrubPos))) {
                return false;
            }
            return this.tryTicks < 200 && this.shelterTicks > 0;
        }

        @Override
        public void start() {
            this.tryTicks = 0;
            this.shelterTicks = 100 + this.hedgehog.getRandom().nextInt(80);
            this.hedgehog.setSheltering(false);
            if (this.targetShrubPos != null) {
                this.hedgehog.getNavigation().moveTo(
                        this.targetShrubPos.getX() + 0.5D,
                        this.targetShrubPos.getY(),
                        this.targetShrubPos.getZ() + 0.5D,
                        this.speedModifier
                );
            }
        }

        @Override
        public void stop() {
            if (this.hedgehog.isSheltering()) {
                this.hedgehog.setSheltering(false);
                if (this.hedgehog.level() instanceof ServerLevel serverLevel && !this.hedgehog.isCurled()) {
                    serverLevel.broadcastEntityEvent(this.hedgehog, EVENT_DIG_OUT);
                }
            }
            this.targetShrubPos = null;
            this.hedgehog.getNavigation().stop();
            this.searchCooldown = 120 + this.hedgehog.getRandom().nextInt(120);
        }

        @Override
        public void tick() {
            if (this.targetShrubPos == null) {
                return;
            }
            Vec3 shrubCenter = Vec3.atBottomCenterOf(this.targetShrubPos);
            double distSqr = this.hedgehog.position().distanceToSqr(shrubCenter);
            if (distSqr <= 2.1D) {
                this.hedgehog.getNavigation().stop();
                if (!this.hedgehog.isSheltering()) {
                    this.hedgehog.setSheltering(true);
                    if (this.hedgehog.level() instanceof ServerLevel serverLevel) {
                        serverLevel.broadcastEntityEvent(this.hedgehog, EVENT_DIG_IN);
                    }
                }
                this.shelterTicks--;
            } else {
                this.tryTicks++;
                if (this.tryTicks % 20 == 0) {
                    this.hedgehog.getNavigation().moveTo(
                            shrubCenter.x,
                            shrubCenter.y,
                            shrubCenter.z,
                            this.speedModifier
                    );
                }
            }
        }

        private @Nullable BlockPos findNearbyShrub() {
            BlockPos origin = this.hedgehog.blockPosition();
            Level level = this.hedgehog.level();
            BlockPos closest = null;
            double closestDistSqr = Double.MAX_VALUE;
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

            for (int dy = -2; dy <= 2; dy++) {
                for (int dx = -this.searchRadius; dx <= this.searchRadius; dx++) {
                    for (int dz = -this.searchRadius; dz <= this.searchRadius; dz++) {
                        cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                        if (isCryoStaticShrub(level.getBlockState(cursor))) {
                            double d2 = origin.distSqr(cursor);
                            if (d2 < closestDistSqr) {
                                closestDistSqr = d2;
                                closest = cursor.immutable();
                            }
                        }
                    }
                }
            }
            return closest;
        }
    }
}
