package ddraig.net.entropica.entity.peat_mud_kelpie;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
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
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Fauna 37 — Peat-Mud Kelpie (Swamp Kelpie) (entropica:peat_mud_kelpie).
 * Passive amphibious swamp water-spirit native to 4.2 Deep Swamp Trench & 4.3 Mangrove Fens:
 * - Two natural 64x64 variants: Peat-Mud & Portal-Teal (0) and Mangrove Tannin & Wisp-Gold (1).
 * - Swims swiftly in deep swamp channels and trots across mud banks without sinking.
 * - Walks atop water blocks during storms / mirage weather (and briefly after spirit rearing),
 *   trailing water droplets and glowing portal-teal or marsh-amber hoof particles.
 * - Defensive Mud-Splash Tail Slap (attack): when hurt, whips around to splash mud in a 3.5-block
 *   radius, applying Blindness for 2 seconds (40 ticks) before darting away.
 * - Marsh Offering (rear): feeding Kelp, Seagrass, Mangrove Propagules, or Lily Pads heals the Kelpie,
 *   grants brief Water Breathing, and coaxes it to shed a Kelpie Water-Pelt on cooldown.
 */
public class PeatMudKelpieEntity extends PathfinderMob {

    public static final int VARIANT_PEAT_MUD = 0;
    public static final int VARIANT_MANGROVE = 1;

    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(PeatMudKelpieEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> WATER_WALKING =
            SynchedEntityData.defineId(PeatMudKelpieEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> REARING =
            SynchedEntityData.defineId(PeatMudKelpieEntity.class, EntityDataSerializers.BOOLEAN);

    private static final byte EVENT_MUD_SPLASH = 4;
    private static final byte EVENT_REAR = 62;

    private static final int MUD_SPLASH_DURATION_TICKS = 27; // 1.35s animation
    private static final int MUD_SPLASH_COOLDOWN_TICKS = 45;
    private static final int REAR_DURATION_TICKS = 35;       // 1.75s animation
    private static final int PELT_SHED_COOLDOWN_TICKS = 1200;

    private static final ResourceLocation KELPIE_WATER_PELT_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "kelpie_water_pelt");

    private static final DustParticleOptions PORTAL_TEAL_DUST = new DustParticleOptions(0x2DD4BF, 0.65F);
    private static final DustParticleOptions WISP_GOLD_DUST = new DustParticleOptions(0xF59E0B, 0.65F);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState swimAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState rearAnimationState = new AnimationState();

    private int mudSplashCooldown = 0;
    private int rearTicksRemaining = 0;
    private int spiritWaterWalkGraceTicks = 0;
    private int ambientRearCooldown = 260;
    private int peltShedCooldown = 0;

    public PeatMudKelpieEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 4;
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, VARIANT_PEAT_MUD);
        builder.define(WATER_WALKING, false);
        builder.define(REARING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new PanicGoal(this, 1.45D));
        this.goalSelector.addGoal(1, new TemptGoal(this, 1.15D, PeatMudKelpieEntity::isMarshOfferingItem, false));
        this.goalSelector.addGoal(2, new RandomStrollGoal(this, 1.0D, 60));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    public static boolean isMarshOfferingItem(ItemStack stack) {
        return stack.is(Items.KELP)
                || stack.is(Items.SEAGRASS)
                || stack.is(Items.MANGROVE_PROPAGULE)
                || stack.is(Items.LILY_PAD);
    }

    public int getVariant() {
        return this.entityData.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(VARIANT, variant == VARIANT_MANGROVE ? VARIANT_MANGROVE : VARIANT_PEAT_MUD);
    }

    public boolean isMangroveVariant() {
        return this.getVariant() == VARIANT_MANGROVE;
    }

    public boolean isWaterWalking() {
        return this.entityData.get(WATER_WALKING);
    }

    public void setWaterWalking(boolean waterWalking) {
        this.entityData.set(WATER_WALKING, waterWalking);
    }

    public boolean isRearing() {
        return this.entityData.get(REARING);
    }

    public void setRearing(boolean rearing) {
        this.entityData.set(REARING, rearing);
    }

    private static boolean isMangroveSurfaceOrBiome(BlockState below, Holder<Biome> biome) {
        if (below.is(Blocks.MANGROVE_ROOTS)
                || below.is(Blocks.MUDDY_MANGROVE_ROOTS)
                || below.is(Blocks.MANGROVE_LOG)) {
            return true;
        }
        String blockPath = BuiltInRegistries.BLOCK.getKey(below.getBlock()).getPath();
        if (blockPath.contains("mangrove")) {
            return true;
        }
        String biomePath = biome.unwrapKey().map(k -> k.location().getPath()).orElse("");
        return biomePath.contains("mangrove");
    }

    public static boolean isSwampOrFenSpawnSurface(BlockGetter level, BlockPos belowPos, BlockState belowState) {
        if (!belowState.getFluidState().isEmpty() && belowState.getFluidState().is(FluidTags.WATER)) {
            return true;
        }
        if (belowState.isAir()) {
            return false;
        }
        String path = BuiltInRegistries.BLOCK.getKey(belowState.getBlock()).getPath();
        return belowState.is(Blocks.MUD)
                || belowState.is(Blocks.MUDDY_MANGROVE_ROOTS)
                || belowState.is(Blocks.MANGROVE_ROOTS)
                || belowState.is(Blocks.CLAY)
                || belowState.is(Blocks.GRASS_BLOCK)
                || belowState.is(Blocks.PODZOL)
                || belowState.is(Blocks.COARSE_DIRT)
                || belowState.is(Blocks.MOSS_BLOCK)
                || path.contains("peat")
                || path.contains("mud")
                || path.contains("silt")
                || belowState.is(BlockTags.ANIMALS_SPAWNABLE_ON)
                || (belowState.getFluidState().isEmpty() && belowState.isFaceSturdy(level, belowPos, Direction.UP));
    }

    public static boolean checkPeatMudKelpieSpawnRules(
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
        BlockPos belowPos = pos.below();
        return isSwampOrFenSpawnSurface(level, belowPos, level.getBlockState(belowPos));
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        BlockPos belowPos = this.blockPosition().below();
        if (isSwampOrFenSpawnSurface(level, belowPos, level.getBlockState(belowPos))) {
            return true;
        }
        return super.checkSpawnRules(level, spawnReason);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        FluidState fluid = level.getFluidState(pos);
        FluidState belowFluid = level.getFluidState(pos.below());
        if (fluid.is(FluidTags.WATER) || belowFluid.is(FluidTags.WATER)) {
            return 10.0F;
        }
        BlockState below = level.getBlockState(pos.below());
        if (below.is(Blocks.MUD) || below.is(Blocks.MUDDY_MANGROVE_ROOTS) || below.is(Blocks.CLAY)) {
            return 8.5F;
        }
        return super.getWalkTargetValue(pos, level);
    }

    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason spawnType,
            @Nullable SpawnGroupData spawnGroupData
    ) {
        BlockState below = level.getBlockState(this.blockPosition().below());
        Holder<Biome> biome = level.getBiome(this.blockPosition());
        if (isMangroveSurfaceOrBiome(below, biome)) {
            this.setVariant(VARIANT_MANGROVE);
        } else if (biome.is(BiomeTags.HAS_SWAMP_HUT)) {
            this.setVariant(this.random.nextFloat() < 0.20F ? VARIANT_MANGROVE : VARIANT_PEAT_MUD);
        } else {
            this.setVariant(this.random.nextFloat() < 0.30F ? VARIANT_MANGROVE : VARIANT_PEAT_MUD);
        }
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    public boolean shouldWalkOnWaterNow() {
        if (this.spiritWaterWalkGraceTicks > 0) {
            return true;
        }
        Level lvl = this.level();
        if (lvl.isRaining() || lvl.isThundering()) {
            return true;
        }
        Holder<Biome> biome = lvl.getBiome(this.blockPosition());
        String biomePath = biome.unwrapKey().map(k -> k.location().getPath()).orElse("");
        return biomePath.contains("mirage") || biomePath.contains("storm");
    }

    @Override
    public boolean canStandOnFluid(FluidState fluidState) {
        return this.isWaterWalking() && fluidState.is(FluidTags.WATER);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected float getBlockSpeedFactor() {
        return Math.max(1.0F, super.getBlockSpeedFactor());
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isEffectiveAi() && this.isInWater() && !this.isWaterWalking()) {
            this.moveRelative(0.055F, travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            Vec3 next = this.getDeltaMovement().scale(0.88D);
            if (this.horizontalCollision) {
                next = new Vec3(next.x, 0.22D, next.z);
            } else if (travelVector.z > 0.001D && this.level().getFluidState(this.blockPosition().above()).is(FluidTags.WATER)) {
                next = new Vec3(next.x, Mth.clamp(next.y + 0.015D, -0.15D, 0.16D), next.z);
            }
            this.setDeltaMovement(next);
            return;
        }
        super.travel(travelVector);
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float amount) {
        boolean hurt = super.hurtServer(serverLevel, damageSource, amount);
        if (hurt && this.isAlive() && this.mudSplashCooldown <= 0) {
            this.triggerDefensiveMudSplash(serverLevel, damageSource.getEntity());
        }
        return hurt;
    }

    private void triggerDefensiveMudSplash(ServerLevel serverLevel, @Nullable Entity attacker) {
        this.mudSplashCooldown = MUD_SPLASH_COOLDOWN_TICKS;
        this.rearTicksRemaining = 0;
        this.setRearing(false);
        serverLevel.broadcastEntityEvent(this, EVENT_MUD_SPLASH);

        BlockState mudState = this.isMangroveVariant()
                ? Blocks.MUDDY_MANGROVE_ROOTS.defaultBlockState()
                : Blocks.MUD.defaultBlockState();

        List<LivingEntity> nearby = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(3.5D),
                e -> e != this && e.isAlive() && !(e instanceof PeatMudKelpieEntity) && this.distanceToSqr(e) <= 12.25D
        );
        for (LivingEntity target : nearby) {
            if (target == attacker || (target instanceof Mob mob && mob.getTarget() == this)) {
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0), this);
                double dx = this.getX() - target.getX();
                double dz = this.getZ() - target.getZ();
                target.knockback(0.45D, dx, dz);
            }
        }

        serverLevel.playSound(null, this.getX(), this.getY() + 0.6D, this.getZ(),
                SoundEvents.GENERIC_SPLASH, SoundSource.NEUTRAL, 0.90F, 0.85F);
        serverLevel.playSound(null, this.getX(), this.getY() + 0.6D, this.getZ(),
                SoundEvents.MUD_BREAK, SoundSource.NEUTRAL, 1.0F, 0.90F);

        serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, mudState),
                this.getX(), this.getY() + 0.55D, this.getZ(),
                30, 1.25D, 0.35D, 1.25D, 0.15D);
        serverLevel.sendParticles(ParticleTypes.SPLASH,
                this.getX(), this.getY() + 0.65D, this.getZ(),
                24, 1.20D, 0.30D, 1.20D, 0.12D);
    }

    private void startSpiritRear(ServerLevel serverLevel) {
        this.rearTicksRemaining = REAR_DURATION_TICKS;
        this.spiritWaterWalkGraceTicks = Math.max(this.spiritWaterWalkGraceTicks, 160);
        this.setRearing(true);
        this.getNavigation().stop();
        serverLevel.broadcastEntityEvent(this, EVENT_REAR);
        serverLevel.playSound(null, this.getX(), this.getY() + 1.0D, this.getZ(),
                SoundEvents.HORSE_BREATHE, SoundSource.NEUTRAL, 0.75F, 1.25F);
        serverLevel.playSound(null, this.getX(), this.getY() + 1.0D, this.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 0.65F, 1.15F);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isMarshOfferingItem(stack) && this.isAlive()) {
            boolean needsHeal = this.getHealth() < this.getMaxHealth();
            boolean canShed = this.peltShedCooldown <= 0;
            if (needsHeal || canShed || !this.isRearing()) {
                if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                    boolean mangroveFood = stack.is(Items.MANGROVE_PROPAGULE);
                    stack.consume(1, player);
                    if (mangroveFood) {
                        this.setVariant(VARIANT_MANGROVE);
                    }
                    this.heal(4.0F);
                    player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 300, 0), this);
                    this.startSpiritRear(serverLevel);

                    if (canShed) {
                        this.peltShedCooldown = PELT_SHED_COOLDOWN_TICKS;
                        Optional<Item> peltOpt = ModItems.KELPIE_WATER_PELT.isPresent()
                                ? Optional.of(ModItems.KELPIE_WATER_PELT.get())
                                : BuiltInRegistries.ITEM.getOptional(KELPIE_WATER_PELT_ID);
                        peltOpt.ifPresent(pelt ->
                                this.spawnAtLocation(serverLevel, new ItemStack(pelt, 1), 0.75F));
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_MUD_SPLASH) {
            this.rearAnimationState.stop();
            this.attackAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_REAR) {
            this.attackAnimationState.stop();
            this.rearAnimationState.start(this.tickCount);
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public void aiStep() {
        if (!this.level().isClientSide()) {
            boolean waterWalkNow = this.shouldWalkOnWaterNow();
            if (waterWalkNow != this.isWaterWalking()) {
                this.setWaterWalking(waterWalkNow);
            }
        }

        // Lift smoothly onto the top of water blocks when mirage/storm water-walking is active
        if (this.isWaterWalking() && this.isInWater()) {
            BlockPos pos = this.blockPosition();
            if (this.level().getFluidState(pos).is(FluidTags.WATER)
                    && this.level().getFluidState(pos.above()).isEmpty()) {
                Vec3 vel = this.getDeltaMovement();
                this.setDeltaMovement(vel.x, Math.max(vel.y, 0.12D), vel.z);
            }
        }

        super.aiStep();

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            if (this.mudSplashCooldown > 0) {
                this.mudSplashCooldown--;
            }
            if (this.spiritWaterWalkGraceTicks > 0) {
                this.spiritWaterWalkGraceTicks--;
            }
            if (this.peltShedCooldown > 0) {
                this.peltShedCooldown--;
            }
            if (this.rearTicksRemaining > 0) {
                this.rearTicksRemaining--;
                this.getNavigation().stop();
                if (this.rearTicksRemaining == 0) {
                    this.setRearing(false);
                }
            } else if (this.ambientRearCooldown > 0) {
                this.ambientRearCooldown--;
            } else if (this.onGround()
                    && this.getDeltaMovement().horizontalDistanceSqr() < 1.0E-4D
                    && this.random.nextInt(140) == 0) {
                this.ambientRearCooldown = 360 + this.random.nextInt(280);
                this.startSpiritRear(serverLevel);
            }
        } else if (this.level().isClientSide()) {
            this.setupClientAnimationsAndParticles();
        }
    }

    private void setupClientAnimationsAndParticles() {
        if (!this.isAlive()) {
            return;
        }

        if (this.attackAnimationState.isStarted()
                && this.attackAnimationState.getTimeInMillis(this.tickCount) >= MUD_SPLASH_DURATION_TICKS * 50L) {
            this.attackAnimationState.stop();
        }
        if (this.rearAnimationState.isStarted()
                && this.rearAnimationState.getTimeInMillis(this.tickCount) >= REAR_DURATION_TICKS * 50L) {
            this.rearAnimationState.stop();
        }

        double horizSpeedSqr = this.getDeltaMovement().horizontalDistanceSqr();
        boolean inChannelSwim = this.isInWater() && !this.isWaterWalking();
        boolean moving = !this.isRearing() && horizSpeedSqr > 1.0E-4D;

        if (inChannelSwim) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.swimAnimationState.startIfStopped(this.tickCount);
        } else if (moving) {
            this.idleAnimationState.stop();
            this.swimAnimationState.stop();
            this.walkAnimationState.startIfStopped(this.tickCount);
        } else {
            this.walkAnimationState.stop();
            this.swimAnimationState.stop();
            this.idleAnimationState.startIfStopped(this.tickCount);
        }

        DustParticleOptions spiritDust = this.isMangroveVariant() ? WISP_GOLD_DUST : PORTAL_TEAL_DUST;
        boolean overWater = this.isInWater()
                || this.level().getFluidState(this.blockPosition().below()).is(FluidTags.WATER);

        if ((this.isWaterWalking() || overWater) && moving && this.tickCount % 3 == 0) {
            this.level().addParticle(
                    ParticleTypes.SPLASH,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.75D,
                    this.getY() + 0.08D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.75D,
                    0.0D, 0.04D, 0.0D
            );
            this.level().addParticle(
                    spiritDust,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.65D,
                    this.getY() + 0.12D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.65D,
                    0.0D, 0.01D, 0.0D
            );
        } else if (this.tickCount % 16 == 0) {
            this.level().addParticle(
                    spiritDust,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.55D,
                    this.getY() + 0.25D + this.random.nextDouble() * 0.90D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.55D,
                    0.0D, 0.01D, 0.0D
            );
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("Variant", Codec.INT, this.getVariant());
        output.store("PeltShedCooldown", Codec.INT, this.peltShedCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setVariant(input.read("Variant", Codec.INT).orElse(VARIANT_PEAT_MUD));
        this.peltShedCooldown = input.read("PeltShedCooldown", Codec.INT).orElse(0);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.HORSE_BREATHE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.HORSE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.HORSE_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        if (this.isWaterWalking() || state.getFluidState().is(FluidTags.WATER)) {
            this.playSound(SoundEvents.GENERIC_SWIM, 0.22F, 1.20F);
        } else {
            this.playSound(SoundEvents.MUD_STEP, 0.30F, 1.00F);
        }
    }
}
