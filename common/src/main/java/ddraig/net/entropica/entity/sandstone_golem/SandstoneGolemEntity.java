package ddraig.net.entropica.entity.sandstone_golem;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.grot.GrotEntity;
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
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Fauna 39 — Sandstone Golem (Desert Elemental) (entropica:sandstone_golem).
 * Neutral hovering sandstone sentinel native to 5.3 Red Silt-Wastes & 5.4 Dusty Canyons:
 * - Two natural masonry variants: Pale Desert Sandstone (0) and Red Canyon Sandstone (1).
 * - Blinding Sandstorm Cyclone & Double-Hammer Slam (attack): deals 10.0 damage and applies
 *   Blindness (3s) to hostile targets within a 3.0-block radius.
 * - Magnetic Sand-Vortex Self-Repair (repair): when damaged near sand/sandstone (or fed sand/sandstone
 *   by hand), unlocks its 4 floating limbs into an orbiting compass vortex to absorb sand and restore HP,
 *   shedding Hard Sandstone Shards on a cooldown when repaired by players.
 */
public class SandstoneGolemEntity extends PathfinderMob {

    public static final int VARIANT_PALE = 0;
    public static final int VARIANT_RED = 1;

    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(SandstoneGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> PROVOKED =
            SynchedEntityData.defineId(SandstoneGolemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> REPAIRING =
            SynchedEntityData.defineId(SandstoneGolemEntity.class, EntityDataSerializers.BOOLEAN);

    private static final byte EVENT_ATTACK = 4;
    private static final byte EVENT_REPAIR = 61;

    private static final int REPAIR_DURATION_TICKS = 48; // 2.4s animation
    private static final int AUTO_REPAIR_COOLDOWN_TICKS = 240;
    private static final int SHARD_SHED_COOLDOWN_TICKS = 1200;

    private static final ResourceLocation HARD_SANDSTONE_SHARD_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "hard_sandstone_shard");

    private static final DustParticleOptions REDSTONE_HEART_DUST = new DustParticleOptions(0xEF4444, 0.70F);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState repairAnimationState = new AnimationState();

    private int repairTicksRemaining = 0;
    private int repairCooldown = 0;
    private int shardShedCooldown = 0;

    public SandstoneGolemEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 135.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 2.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.75D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, VARIANT_PALE);
        builder.define(PROVOKED, false);
        builder.define(REPAIRING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.18D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                this,
                Mob.class,
                10,
                true,
                false,
                SandstoneGolemEntity::isHostileRuinRaider
        ));
    }

    public static boolean isHostileRuinRaider(LivingEntity candidate, ServerLevel level) {
        if (candidate == null || !candidate.isAlive() || candidate instanceof GrotEntity || candidate instanceof Creeper) {
            return false;
        }
        return candidate.getType().is(EntityTypeTags.UNDEAD)
                || candidate.getType().is(EntityTypeTags.RAIDERS)
                || candidate instanceof Monster;
    }

    public int getVariant() {
        return this.entityData.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(VARIANT, variant == VARIANT_RED ? VARIANT_RED : VARIANT_PALE);
    }

    public boolean isRedVariant() {
        return this.getVariant() == VARIANT_RED;
    }

    public boolean isProvoked() {
        return this.entityData.get(PROVOKED);
    }

    public void setProvoked(boolean provoked) {
        this.entityData.set(PROVOKED, provoked);
    }

    public boolean isRepairing() {
        return this.entityData.get(REPAIRING);
    }

    public void setRepairing(boolean repairing) {
        this.entityData.set(REPAIRING, repairing);
    }

    private static boolean isRedSandOrCanyonBlock(BlockState state) {
        if (state.isAir()) {
            return false;
        }
        if (state.is(Blocks.RED_SAND)
                || state.is(Blocks.RED_SANDSTONE)
                || state.is(Blocks.CUT_RED_SANDSTONE)
                || state.is(Blocks.CHISELED_RED_SANDSTONE)
                || state.is(Blocks.SMOOTH_RED_SANDSTONE)
                || state.is(BlockTags.TERRACOTTA)) {
            return true;
        }
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return path.contains("red_silt") || path.contains("red_sand") || path.contains("canyon");
    }

    private static boolean isPaleSandOrSandstoneBlock(BlockState state) {
        if (state.isAir()) {
            return false;
        }
        if (state.is(Blocks.SAND)
                || state.is(Blocks.SANDSTONE)
                || state.is(Blocks.CUT_SANDSTONE)
                || state.is(Blocks.CHISELED_SANDSTONE)
                || state.is(Blocks.SMOOTH_SANDSTONE)) {
            return true;
        }
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return path.contains("sand") || path.contains("silt") || path.contains("dune");
    }

    private static boolean isAnySandOrSandstoneBlock(BlockState state) {
        return isRedSandOrCanyonBlock(state) || isPaleSandOrSandstoneBlock(state);
    }

    private static boolean isRedSandRepairItem(ItemStack stack) {
        return stack.is(Items.RED_SAND)
                || stack.is(Items.RED_SANDSTONE)
                || stack.is(Items.CUT_RED_SANDSTONE)
                || stack.is(Items.CHISELED_RED_SANDSTONE)
                || stack.is(Items.SMOOTH_RED_SANDSTONE);
    }

    private static boolean isPaleSandRepairItem(ItemStack stack) {
        return stack.is(Items.SAND)
                || stack.is(Items.SANDSTONE)
                || stack.is(Items.CUT_SANDSTONE)
                || stack.is(Items.CHISELED_SANDSTONE)
                || stack.is(Items.SMOOTH_SANDSTONE);
    }

    public static boolean isSandstoneSpawnSurface(BlockGetter level, BlockPos belowPos, BlockState belowState) {
        if (belowState.isAir()) {
            return false;
        }
        return isAnySandOrSandstoneBlock(belowState)
                || belowState.is(BlockTags.TERRACOTTA)
                || belowState.is(BlockTags.ANIMALS_SPAWNABLE_ON)
                || (belowState.getFluidState().isEmpty() && belowState.isFaceSturdy(level, belowPos, Direction.UP));
    }

    public static boolean checkSandstoneGolemSpawnRules(
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
        return isSandstoneSpawnSurface(level, belowPos, level.getBlockState(belowPos));
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        BlockPos belowPos = this.blockPosition().below();
        if (isSandstoneSpawnSurface(level, belowPos, level.getBlockState(belowPos))) {
            return true;
        }
        return super.checkSpawnRules(level, spawnReason);
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
        String biomePath = biome.unwrapKey().map(k -> k.location().getPath()).orElse("");

        if (isRedSandOrCanyonBlock(below)
                || biome.is(BiomeTags.IS_BADLANDS)
                || biomePath.contains("red_silt")
                || biomePath.contains("dusty_canyon")) {
            this.setVariant(VARIANT_RED);
        } else if (isPaleSandOrSandstoneBlock(below)) {
            this.setVariant(VARIANT_PALE);
        } else {
            this.setVariant(this.random.nextFloat() < 0.25F ? VARIANT_RED : VARIANT_PALE);
        }
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        if (source.is(DamageTypes.CACTUS) || source.is(DamageTypes.IN_WALL) || source.is(DamageTypeTags.IS_FALL)) {
            return true;
        }
        return super.isInvulnerableTo(level, source);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        if (!this.level().isClientSide()) {
            this.setProvoked(target != null && target.isAlive());
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        level.broadcastEntityEvent(this, EVENT_ATTACK);

        BlockState dustBlock = this.isRedVariant() ? Blocks.RED_SANDSTONE.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
        BlockState sandBlock = this.isRedVariant() ? Blocks.RED_SAND.defaultBlockState() : Blocks.SAND.defaultBlockState();

        // 3.0-block Sandstorm Cyclone & Double-Hammer Ground Slam shockwave (Blindness for 3s)
        List<LivingEntity> victims = level.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(3.0D),
                e -> e != this && e.isAlive() && !(e instanceof SandstoneGolemEntity) && this.distanceToSqr(e) <= 9.0D
        );
        for (LivingEntity victim : victims) {
            boolean shouldBlind = (victim == target)
                    || (victim == this.getLastHurtByMob())
                    || (victim instanceof Mob mob && (mob.getTarget() == this || isHostileRuinRaider(mob, level)));
            if (shouldBlind) {
                victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0), this);
                victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0), this);
                if (victim != target) {
                    double dx = this.getX() - victim.getX();
                    double dz = this.getZ() - victim.getZ();
                    victim.knockback(0.45D, dx, dz);
                }
            }
        }

        if (hit && target instanceof LivingEntity living) {
            double dx = this.getX() - living.getX();
            double dz = this.getZ() - living.getZ();
            living.knockback(0.70D, dx, dz);
            Vec3 cur = living.getDeltaMovement();
            living.setDeltaMovement(cur.x, Math.max(cur.y, 0.28D), cur.z);
            living.hasImpulse = true;
            living.hurtMarked = true;
        }

        level.playSound(null, this.getX(), this.getY() + 0.8D, this.getZ(),
                SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 0.55F, 0.65F);
        level.playSound(null, this.getX(), this.getY() + 0.8D, this.getZ(),
                SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), SoundSource.HOSTILE, 0.85F, 0.80F);

        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, dustBlock),
                this.getX(), this.getY() + 0.35D, this.getZ(),
                32, 1.35D, 0.25D, 1.35D, 0.14D);
        level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, sandBlock),
                this.getX(), this.getY() + 1.0D, this.getZ(),
                28, 1.5D, 0.65D, 1.5D, 0.05D);

        return hit;
    }

    @Nullable
    private BlockPos findNearbySandSource(ServerLevel level) {
        BlockPos origin = this.blockPosition();
        BlockPos belowFeet = origin.below();
        BlockPos fallbackSandstone = null;
        for (BlockPos check : BlockPos.betweenClosed(origin.offset(-4, -2, -4), origin.offset(4, 2, 4))) {
            BlockState state = level.getBlockState(check);
            if ((state.is(Blocks.SAND) || state.is(Blocks.RED_SAND))
                    && !check.equals(belowFeet)
                    && level.getBlockState(check.above()).isAir()) {
                return check.immutable();
            }
            if (fallbackSandstone == null && isAnySandOrSandstoneBlock(state)) {
                fallbackSandstone = check.immutable();
            }
        }
        return fallbackSandstone;
    }

    private void startSandVortexRepair(ServerLevel level, @Nullable BlockPos sandPos) {
        this.repairCooldown = AUTO_REPAIR_COOLDOWN_TICKS;
        this.repairTicksRemaining = REPAIR_DURATION_TICKS;
        this.setRepairing(true);
        this.getNavigation().stop();
        level.broadcastEntityEvent(this, EVENT_REPAIR);

        if (sandPos != null) {
            BlockState srcState = level.getBlockState(sandPos);
            if (isRedSandOrCanyonBlock(srcState)) {
                this.setVariant(VARIANT_RED);
            } else if (isPaleSandOrSandstoneBlock(srcState)) {
                this.setVariant(VARIANT_PALE);
            }
            // Vacuum loose surface sand into the compass vortex when mobGriefing is enabled
            if ((srcState.is(Blocks.SAND) || srcState.is(Blocks.RED_SAND))
                    && level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, srcState),
                        sandPos.getX() + 0.5D, sandPos.getY() + 0.5D, sandPos.getZ() + 0.5D,
                        18, 0.35D, 0.35D, 0.35D, 0.08D);
                level.removeBlock(sandPos, false);
            }
        }

        level.playSound(null, this.getX(), this.getY() + 1.0D, this.getZ(),
                SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.NEUTRAL, 0.70F, 1.25F);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean redOffer = isRedSandRepairItem(stack);
        boolean paleOffer = isPaleSandRepairItem(stack);
        if ((redOffer || paleOffer) && this.isAlive()) {
            boolean needsHeal = this.getHealth() < this.getMaxHealth();
            boolean canShed = this.shardShedCooldown <= 0;
            if (needsHeal || canShed || redOffer != this.isRedVariant()) {
                if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                    stack.consume(1, player);
                    this.setVariant(redOffer ? VARIANT_RED : VARIANT_PALE);
                    this.heal(8.0F);

                    if (this.getTarget() == player || this.getLastHurtByMob() == player) {
                        this.setTarget(null);
                        this.setLastHurtByMob(null);
                    }

                    this.startSandVortexRepair(serverLevel, null);

                    if (canShed) {
                        this.shardShedCooldown = SHARD_SHED_COOLDOWN_TICKS;
                        Optional<Item> shardOpt = ModItems.HARD_SANDSTONE_SHARD.isPresent()
                                ? Optional.of(ModItems.HARD_SANDSTONE_SHARD.get())
                                : BuiltInRegistries.ITEM.getOptional(HARD_SANDSTONE_SHARD_ID);
                        shardOpt.ifPresent(shard ->
                                this.spawnAtLocation(serverLevel, new ItemStack(shard, 1), 0.85F));
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_ATTACK) {
            this.repairAnimationState.stop();
            this.attackAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_REPAIR) {
            this.attackAnimationState.stop();
            this.repairAnimationState.start(this.tickCount);
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            if (this.repairCooldown > 0) {
                this.repairCooldown--;
            }
            if (this.shardShedCooldown > 0) {
                this.shardShedCooldown--;
            }

            if (this.repairTicksRemaining > 0) {
                this.repairTicksRemaining--;
                this.getNavigation().stop();

                if (this.repairTicksRemaining % 6 == 0) {
                    BlockState sandBlock = this.isRedVariant() ? Blocks.RED_SAND.defaultBlockState() : Blocks.SAND.defaultBlockState();
                    serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, sandBlock),
                            this.getX(), this.getY() + 0.95D, this.getZ(),
                            10, 0.85D, 0.45D, 0.85D, 0.04D);
                    serverLevel.sendParticles(REDSTONE_HEART_DUST,
                            this.getX(), this.getY() + 0.95D, this.getZ(),
                            4, 0.18D, 0.18D, 0.18D, 0.01D);
                }

                if (this.repairTicksRemaining == 0) {
                    this.setRepairing(false);
                    if (this.isAlive()) {
                        this.heal(10.0F);
                        serverLevel.playSound(null, this.getX(), this.getY() + 1.0D, this.getZ(),
                                SoundEvents.SAND_PLACE, SoundSource.NEUTRAL, 1.0F, 0.85F);
                        serverLevel.playSound(null, this.getX(), this.getY() + 1.0D, this.getZ(),
                                SoundEvents.IRON_GOLEM_REPAIR, SoundSource.NEUTRAL, 0.65F, 1.15F);
                    }
                }
            } else if (this.isAlive()
                    && this.getHealth() <= this.getMaxHealth() * 0.72F
                    && this.repairCooldown <= 0) {
                BlockPos sandSource = this.findNearbySandSource(serverLevel);
                if (sandSource != null) {
                    this.startSandVortexRepair(serverLevel, sandSource);
                } else {
                    this.repairCooldown = 60;
                }
            }
        } else if (this.level().isClientSide()) {
            this.setupClientAnimationsAndParticles();
        }
    }

    private void setupClientAnimationsAndParticles() {
        if (!this.isAlive()) {
            return;
        }

        if (this.attackAnimationState.isStarted() && this.attackAnimationState.getTimeInMillis(this.tickCount) >= 1800L) {
            this.attackAnimationState.stop();
        }
        if (this.repairAnimationState.isStarted() && this.repairAnimationState.getTimeInMillis(this.tickCount) >= 2400L) {
            this.repairAnimationState.stop();
        }

        double horizSpeedSqr = this.getDeltaMovement().horizontalDistanceSqr();
        boolean moving = !this.isRepairing() && horizSpeedSqr > 1.0E-4D;

        if (moving) {
            this.idleAnimationState.stop();
            this.walkAnimationState.startIfStopped(this.tickCount);
        } else {
            this.walkAnimationState.stop();
            this.idleAnimationState.startIfStopped(this.tickCount);
        }

        // Trailing hovering sand dust & crimson redstone core sparks
        BlockState sandBlock = this.isRedVariant() ? Blocks.RED_SAND.defaultBlockState() : Blocks.SAND.defaultBlockState();
        if (moving && this.tickCount % 4 == 0) {
            this.level().addParticle(
                    new BlockParticleOption(ParticleTypes.FALLING_DUST, sandBlock),
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.85D,
                    this.getY() + 0.18D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.85D,
                    0.0D, 0.0D, 0.0D
            );
        }
        if (this.tickCount % (this.isRepairing() ? 3 : 12) == 0) {
            this.level().addParticle(
                    REDSTONE_HEART_DUST,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.30D,
                    this.getY() + 0.94D + (this.random.nextDouble() - 0.5D) * 0.25D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.30D,
                    0.0D, 0.01D, 0.0D
            );
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("Variant", Codec.INT, this.getVariant());
        output.store("RepairCooldown", Codec.INT, this.repairCooldown);
        output.store("ShardShedCooldown", Codec.INT, this.shardShedCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setVariant(input.read("Variant", Codec.INT).orElse(VARIANT_PALE));
        this.repairCooldown = input.read("RepairCooldown", Codec.INT).orElse(0);
        this.shardShedCooldown = input.read("ShardShedCooldown", Codec.INT).orElse(0);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.SAND_STEP;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.DEEPSLATE_BRICKS_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.DEEPSLATE_BRICKS_BREAK;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SAND_STEP, 0.35F, 0.80F);
    }
}
