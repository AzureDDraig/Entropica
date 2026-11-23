package ddraig.net.entropica.entity.dust_core_golem;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import net.minecraft.core.BlockPos;
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
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Optional;

/**
 * Fauna 14 — Dust-Core Golem (Silt Excavator) (entropica:dust_core_golem).
 * Neutral hovering silt-and-ash elemental sentinel native to 2.15 Dust-Crusted Bergs & arid silt canyons:
 * - Two 64x64 regional variants: Polar Ash-Berg (0) and Red Canyon Silt (1).
 * - Blinding Dust Blast: retaliates when hurt from ~4.5..7.5 blocks (or close quarters) on a 40-tick cooldown,
 *   firing a jet of ash & smoke that deals 6.0 damage + knockback and applies Blindness (3s) unless protected by
 *   Mirage Veil Goggles, Spore-Filter Mask, or Zephyr Weight.
 * - Sand-Absorption Self-Repair: scans a 5x3x5 neighborhood every 80 ticks when injured to absorb loose sand/grey sand
 *   (skipping sand supporting chests/containers/machines) and restore +6.0 HP.
 * - Player Sand Feeding: right-clicking with Sand, Red Sand, or Grey Sand heals +6.0 HP when injured, or compacts
 *   at full health on a 240-tick cooldown with a 35% chance to pop out 1x Dust-Core Shard.
 */
public class DustCoreGolemEntity extends PathfinderMob {

    public static final int VARIANT_POLAR_ASH_BERG = 0;
    public static final int VARIANT_RED_CANYON_SILT = 1;

    public static final int ACTION_NONE = 0;
    public static final int ACTION_ATTACKING = 1;
    public static final int ACTION_REPAIRING = 2;

    public static final int ATTACK_COOLDOWN_TICKS = 40;
    public static final int ATTACK_ANIMATION_TICKS = 20;
    public static final int REPAIR_SCAN_INTERVAL_TICKS = 80;
    public static final int REPAIR_ANIMATION_TICKS = 30;
    public static final int REPAIR_COOLDOWN_TICKS = 140;
    public static final int SHED_COOLDOWN_TICKS = 240;

    private static final byte EVENT_ATTACK = 4;
    private static final byte EVENT_REPAIR = 61;

    private static final ResourceLocation GREY_SAND_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "grey_sand");
    private static final ResourceLocation DUST_CRUSTED_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "dust_crusted_ice");
    private static final ResourceLocation COARSE_GRAVEL_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "coarse_gravel");
    private static final ResourceLocation DUST_CORE_SHARD_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "dust_core_shard");
    private static final ResourceLocation MIRAGE_VEIL_GOGGLES_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mirage_veil_goggles");
    private static final ResourceLocation SPORE_FILTER_MASK_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spore_filter_mask");
    private static final ResourceLocation ZEPHYR_WEIGHT_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "zephyr_weight");

    private static final DustParticleOptions CORE_EMBER_DUST = new DustParticleOptions(0xF97316, 0.60F);

    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(DustCoreGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ACTION_STATE =
            SynchedEntityData.defineId(DustCoreGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SHED_COOLDOWN =
            SynchedEntityData.defineId(DustCoreGolemEntity.class, EntityDataSerializers.INT);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState repairAnimationState = new AnimationState();

    private int attackCooldown = 0;
    private int repairCooldown = 0;
    private int actionTicks = 0;
    private int clientLastActionState = ACTION_NONE;

    public DustCoreGolemEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 6;
        this.setPathfindingMalus(PathType.POWDER_SNOW, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_POWDER_SNOW, 0.0F);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.45D)
                .add(Attributes.STEP_HEIGHT, 1.2D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, VARIANT_POLAR_ASH_BERG);
        builder.define(ACTION_STATE, ACTION_NONE);
        builder.define(SHED_COOLDOWN, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new DustBlastAttackGoal(this));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.05D, DustCoreGolemEntity::isFeedableSandItem, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.90D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public int getVariant() {
        return this.entityData.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(VARIANT, variant == VARIANT_RED_CANYON_SILT ? VARIANT_RED_CANYON_SILT : VARIANT_POLAR_ASH_BERG);
    }

    public boolean isRedCanyonVariant() {
        return this.getVariant() == VARIANT_RED_CANYON_SILT;
    }

    public int getActionState() {
        return this.entityData.get(ACTION_STATE);
    }

    public void setActionState(int state) {
        int clamped = (state == ACTION_ATTACKING || state == ACTION_REPAIRING) ? state : ACTION_NONE;
        this.entityData.set(ACTION_STATE, clamped);
    }

    public boolean isAttacking() {
        return this.getActionState() == ACTION_ATTACKING;
    }

    public boolean isRepairing() {
        return this.getActionState() == ACTION_REPAIRING;
    }

    public int getRepairCooldown() {
        return this.repairCooldown;
    }

    public void setRepairCooldown(int ticks) {
        this.repairCooldown = Math.max(0, ticks);
    }

    public int getShedCooldown() {
        return this.entityData.get(SHED_COOLDOWN);
    }

    public void setShedCooldown(int ticks) {
        this.entityData.set(SHED_COOLDOWN, Math.max(0, ticks));
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        // Hovering silt-core construct never accumulates fall damage
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effectInstance) {
        if (effectInstance.is(MobEffects.BLINDNESS)) {
            return false;
        }
        return super.canBeAffected(effectInstance);
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        if (source.is(DamageTypeTags.IS_FALL)
                || source.is(DamageTypeTags.IS_FREEZING)
                || source.is(DamageTypes.FREEZE)
                || source.is(DamageTypes.CACTUS)
                || source.is(DamageTypes.IN_WALL)) {
            return true;
        }
        return super.isInvulnerableTo(level, source);
    }

    @Override
    public void makeStuckInBlock(BlockState state, Vec3 motionMultiplier) {
        if (state.is(Blocks.POWDER_SNOW) || state.is(Blocks.SNOW) || state.getBlock() instanceof SnowLayerBlock) {
            return;
        }
        super.makeStuckInBlock(state, motionMultiplier);
    }

    public static boolean isLooseRepairSandBlock(BlockState state) {
        if (state == null || state.isAir()) {
            return false;
        }
        if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
            return true;
        }
        return BuiltInRegistries.BLOCK.getOptional(GREY_SAND_ID)
                .map(state::is)
                .orElse(false);
    }

    public static boolean isFeedableSandItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (stack.is(Items.SAND) || stack.is(Items.RED_SAND)) {
            return true;
        }
        return BuiltInRegistries.ITEM.getOptional(GREY_SAND_ID)
                .map(stack::is)
                .orElse(false);
    }

    public static boolean isProtectedFromDustBlindness(LivingEntity target) {
        ItemStack headStack = target.getItemBySlot(EquipmentSlot.HEAD);
        if (!headStack.isEmpty()) {
            if (BuiltInRegistries.ITEM.getOptional(MIRAGE_VEIL_GOGGLES_ID).map(headStack::is).orElse(false)
                    || BuiltInRegistries.ITEM.getOptional(SPORE_FILTER_MASK_ID).map(headStack::is).orElse(false)) {
                return true;
            }
            ResourceLocation headKey = BuiltInRegistries.ITEM.getKey(headStack.getItem());
            if (MIRAGE_VEIL_GOGGLES_ID.equals(headKey) || SPORE_FILTER_MASK_ID.equals(headKey)) {
                return true;
            }
        }

        Optional<Holder.Reference<MobEffect>> zephyrHolder = BuiltInRegistries.MOB_EFFECT.get(ZEPHYR_WEIGHT_ID);
        if (zephyrHolder.isPresent() && target.hasEffect(zephyrHolder.get())) {
            return true;
        }
        for (MobEffectInstance active : target.getActiveEffects()) {
            if (active.getEffect().unwrapKey().map(k -> ZEPHYR_WEIGHT_ID.equals(k.location())).orElse(false)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDustCoreGolemSpawnSurface(BlockState state) {
        if (state == null || state.isAir()) {
            return false;
        }
        if (state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.SAND)
                || state.is(Blocks.RED_SAND)
                || state.is(Blocks.SANDSTONE)
                || state.is(Blocks.RED_SANDSTONE)
                || state.is(BlockTags.ANIMALS_SPAWNABLE_ON)
                || state.is(BlockTags.SAND)) {
            return true;
        }
        return BuiltInRegistries.BLOCK.getOptional(GREY_SAND_ID).map(state::is).orElse(false)
                || BuiltInRegistries.BLOCK.getOptional(DUST_CRUSTED_ICE_ID).map(state::is).orElse(false)
                || BuiltInRegistries.BLOCK.getOptional(COARSE_GRAVEL_ID).map(state::is).orElse(false);
    }

    private static boolean isThinSurfaceCover(BlockState state) {
        if (state.is(Blocks.SNOW) || state.getBlock() instanceof SnowLayerBlock) {
            return true;
        }
        return !state.blocksMotion() && state.getFluidState().isEmpty() && !state.isAir();
    }

    public static boolean checkDustCoreGolemSpawnRules(
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
        BlockState below = level.getBlockState(belowPos);
        if (isThinSurfaceCover(below) && level.getBlockState(belowPos.below()).blocksMotion()) {
            below = level.getBlockState(belowPos.below());
        }
        return isDustCoreGolemSpawnSurface(below);
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        BlockPos belowPos = this.blockPosition().below();
        BlockState below = level.getBlockState(belowPos);
        if (isThinSurfaceCover(below) && level.getBlockState(belowPos.below()).blocksMotion()) {
            below = level.getBlockState(belowPos.below());
        }
        if (isDustCoreGolemSpawnSurface(below)) {
            return true;
        }
        return super.checkSpawnRules(level, spawnReason);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        BlockState below = level.getBlockState(pos.below());
        if (BuiltInRegistries.BLOCK.getOptional(GREY_SAND_ID).map(below::is).orElse(false)
                || BuiltInRegistries.BLOCK.getOptional(DUST_CRUSTED_ICE_ID).map(below::is).orElse(false)) {
            return 10.0F;
        }
        if (isDustCoreGolemSpawnSurface(below)) {
            return 5.0F;
        }
        return 0.0F;
    }

    private static boolean isNearRedSandOrSandstone(ServerLevelAccessor level, BlockPos origin) {
        for (BlockPos check : BlockPos.betweenClosed(origin.offset(-2, -2, -2), origin.offset(2, 1, 2))) {
            BlockState st = level.getBlockState(check);
            if (st.is(Blocks.RED_SAND)
                    || st.is(Blocks.RED_SANDSTONE)
                    || st.is(Blocks.CUT_RED_SANDSTONE)
                    || st.is(Blocks.CHISELED_RED_SANDSTONE)
                    || st.is(Blocks.SMOOTH_RED_SANDSTONE)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason spawnType,
            @Nullable SpawnGroupData spawnGroupData
    ) {
        BlockPos origin = this.blockPosition();
        Holder<Biome> biomeHolder = level.getBiome(origin);
        float baseTemp = biomeHolder.value().getBaseTemperature();

        if (baseTemp >= 0.5F || biomeHolder.is(BiomeTags.IS_BADLANDS) || isNearRedSandOrSandstone(level, origin)) {
            this.setVariant(VARIANT_RED_CANYON_SILT);
        } else {
            this.setVariant(VARIANT_POLAR_ASH_BERG);
        }
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    private void triggerAttackAction(ServerLevel serverLevel) {
        this.actionTicks = ATTACK_ANIMATION_TICKS;
        this.setActionState(ACTION_ATTACKING);
        serverLevel.broadcastEntityEvent(this, EVENT_ATTACK);
    }

    private void triggerRepairAction(ServerLevel serverLevel) {
        this.actionTicks = REPAIR_ANIMATION_TICKS;
        this.setActionState(ACTION_REPAIRING);
        serverLevel.broadcastEntityEvent(this, EVENT_REPAIR);
    }

    public void unleashBlindingDustBlast(ServerLevel serverLevel, LivingEntity target) {
        this.attackCooldown = ATTACK_COOLDOWN_TICKS;
        this.triggerAttackAction(serverLevel);

        serverLevel.playSound(
                null,
                this.getX(),
                this.getY() + 0.75D,
                this.getZ(),
                SoundEvents.BREEZE_SHOOT,
                SoundSource.HOSTILE,
                0.95F,
                0.90F
        );
        serverLevel.playSound(
                null,
                this.getX(),
                this.getY() + 0.75D,
                this.getZ(),
                SoundEvents.SAND_BREAK,
                SoundSource.HOSTILE,
                1.0F,
                0.85F
        );

        Vec3 start = new Vec3(this.getX(), this.getY() + 0.85D, this.getZ());
        Vec3 end = new Vec3(target.getX(), target.getY() + target.getBbHeight() * 0.60D, target.getZ());
        Vec3 delta = end.subtract(start);
        int steps = 12;
        for (int i = 1; i <= steps; i++) {
            double t = (double) i / (double) steps;
            double px = start.x + delta.x * t;
            double py = start.y + delta.y * t;
            double pz = start.z + delta.z * t;
            serverLevel.sendParticles(ParticleTypes.ASH, px, py, pz, 5, 0.14D, 0.14D, 0.14D, 0.03D);
            if (i % 2 == 0) {
                serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, px, py, pz, 1, 0.08D, 0.08D, 0.08D, 0.012D);
            }
        }

        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        target.hurtServer(serverLevel, this.damageSources().mobAttack(this), damage);

        double dx = this.getX() - target.getX();
        double dz = this.getZ() - target.getZ();
        target.knockback(0.65D, dx, dz);
        target.hurtMarked = true;

        if (!isProtectedFromDustBlindness(target)) {
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0), this);
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel serverLevel, Entity target) {
        if (target instanceof LivingEntity living) {
            this.unleashBlindingDustBlast(serverLevel, living);
            return true;
        }
        return super.doHurtTarget(serverLevel, target);
    }

    private static boolean hasProtectedContainerOrMachineAbove(ServerLevel level, BlockPos sandPos) {
        BlockPos abovePos = sandPos.above();
        BlockState aboveState = level.getBlockState(abovePos);
        if (aboveState.isAir()) {
            return false;
        }
        if (aboveState.hasBlockEntity() || aboveState.getBlock() instanceof EntityBlock) {
            return true;
        }
        BlockEntity be = level.getBlockEntity(abovePos);
        if (be != null || be instanceof Container) {
            return true;
        }
        String abovePath = BuiltInRegistries.BLOCK.getKey(aboveState.getBlock()).getPath();
        return abovePath.contains("chest")
                || abovePath.contains("barrel")
                || abovePath.contains("hopper")
                || abovePath.contains("furnace")
                || abovePath.contains("kiln")
                || abovePath.contains("crucible")
                || abovePath.contains("pedestal")
                || abovePath.contains("machine")
                || abovePath.contains("chamber")
                || abovePath.contains("condenser")
                || abovePath.contains("extractor")
                || abovePath.contains("synthesizer");
    }

    public boolean tryAbsorbNearbySandForRepair(ServerLevel serverLevel) {
        if (!serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return false;
        }
        BlockPos origin = this.blockPosition();
        BlockPos directUnder = origin.below();
        BlockPos bestPos = null;
        double bestDistSqr = Double.MAX_VALUE;
        BlockPos fallbackUnderPos = null;

        // Scan 5x3x5 neighborhood around the golem for loose sand / red sand / grey sand
        for (BlockPos check : BlockPos.betweenClosed(origin.offset(-2, -1, -2), origin.offset(2, 1, 2))) {
            BlockState state = serverLevel.getBlockState(check);
            if (!isLooseRepairSandBlock(state) || hasProtectedContainerOrMachineAbove(serverLevel, check)) {
                continue;
            }
            if (check.equals(directUnder)) {
                fallbackUnderPos = check.immutable();
                continue;
            }
            double d = origin.distSqr(check);
            if (d < bestDistSqr) {
                bestDistSqr = d;
                bestPos = check.immutable();
            }
        }

        BlockPos targetSandPos = bestPos != null ? bestPos : fallbackUnderPos;
        if (targetSandPos == null) {
            return false;
        }

        BlockState sandState = serverLevel.getBlockState(targetSandPos);
        serverLevel.levelEvent(2001, targetSandPos, Block.getId(sandState));
        serverLevel.removeBlock(targetSandPos, false);

        this.triggerRepairAction(serverLevel);
        this.heal(6.0F);
        this.repairCooldown = REPAIR_COOLDOWN_TICKS;

        serverLevel.playSound(
                null,
                this.getX(),
                this.getY() + 0.75D,
                this.getZ(),
                SoundEvents.SAND_PLACE,
                SoundSource.NEUTRAL,
                0.95F,
                0.95F
        );
        serverLevel.playSound(
                null,
                this.getX(),
                this.getY() + 0.75D,
                this.getZ(),
                SoundEvents.AMETHYST_BLOCK_RESONATE,
                SoundSource.NEUTRAL,
                0.90F,
                1.15F
        );
        serverLevel.sendParticles(
                new BlockParticleOption(ParticleTypes.FALLING_DUST, sandState),
                this.getX(),
                this.getY() + 0.85D,
                this.getZ(),
                18,
                0.55D,
                0.35D,
                0.55D,
                0.04D
        );
        return true;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.isAlive() && isFeedableSandItem(stack)) {
            boolean injured = this.getHealth() < this.getMaxHealth();
            boolean canShed = !injured && this.getShedCooldown() <= 0;
            if (injured || canShed) {
                if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                    stack.consume(1, player);
                    this.triggerRepairAction(serverLevel);

                    if (this.getTarget() == player || this.getLastHurtByMob() == player) {
                        this.setTarget(null);
                        this.setLastHurtByMob(null);
                    }

                    serverLevel.playSound(
                            null,
                            this.getX(),
                            this.getY() + 0.75D,
                            this.getZ(),
                            SoundEvents.SAND_PLACE,
                            SoundSource.NEUTRAL,
                            0.95F,
                            1.05F
                    );
                    serverLevel.playSound(
                            null,
                            this.getX(),
                            this.getY() + 0.75D,
                            this.getZ(),
                            SoundEvents.AMETHYST_BLOCK_RESONATE,
                            SoundSource.NEUTRAL,
                            0.85F,
                            1.25F
                    );
                    serverLevel.sendParticles(
                            ParticleTypes.ASH,
                            this.getX(),
                            this.getY() + 0.85D,
                            this.getZ(),
                            16,
                            0.45D,
                            0.30D,
                            0.45D,
                            0.04D
                    );

                    if (injured) {
                        this.heal(6.0F);
                        serverLevel.sendParticles(
                                ParticleTypes.HAPPY_VILLAGER,
                                this.getX(),
                                this.getY() + 0.90D,
                                this.getZ(),
                                8,
                                0.40D,
                                0.25D,
                                0.40D,
                                0.02D
                        );
                    } else {
                        this.setShedCooldown(SHED_COOLDOWN_TICKS);
                        if (this.random.nextFloat() < 0.35F) {
                            Optional<Item> shardOpt = BuiltInRegistries.ITEM.getOptional(DUST_CORE_SHARD_ID);
                            if (shardOpt.isPresent()) {
                                ItemEntity drop = this.spawnAtLocation(serverLevel, new ItemStack(shardOpt.get(), 1), 0.70F);
                                if (drop != null) {
                                    drop.setDeltaMovement(drop.getDeltaMovement().add(
                                            (this.random.nextFloat() - this.random.nextFloat()) * 0.08D,
                                            0.05D + this.random.nextFloat() * 0.05D,
                                            (this.random.nextFloat() - this.random.nextFloat()) * 0.08D
                                    ));
                                }
                                serverLevel.playSound(
                                        null,
                                        this.getX(),
                                        this.getY() + 0.75D,
                                        this.getZ(),
                                        SoundEvents.AMETHYST_CLUSTER_BREAK,
                                        SoundSource.NEUTRAL,
                                        0.85F,
                                        1.30F
                                );
                            }
                        }
                    }

                    this.gameEvent(GameEvent.ENTITY_INTERACT, player);
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
    public void tick() {
        if (this.getTicksFrozen() > 0) {
            this.setTicksFrozen(0);
        }

        super.tick();

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            if (this.attackCooldown > 0) {
                this.attackCooldown--;
            }
            if (this.repairCooldown > 0) {
                this.repairCooldown--;
            }
            int shedCd = this.getShedCooldown();
            if (shedCd > 0) {
                this.setShedCooldown(shedCd - 1);
            }
            if (this.actionTicks > 0) {
                this.actionTicks--;
                if (this.actionTicks <= 0) {
                    this.setActionState(ACTION_NONE);
                }
            }

            if (this.isAlive()
                    && this.tickCount % REPAIR_SCAN_INTERVAL_TICKS == 0
                    && this.getHealth() < this.getMaxHealth()
                    && this.repairCooldown <= 0) {
                this.tryAbsorbNearbySandForRepair(serverLevel);
            }
        } else if (this.level().isClientSide()) {
            this.setupClientAnimationsAndParticles();
        }
    }

    private void setupClientAnimationsAndParticles() {
        if (!this.isAlive()) {
            return;
        }

        int currentAction = this.getActionState();
        if (currentAction != this.clientLastActionState) {
            if (currentAction == ACTION_ATTACKING) {
                this.repairAnimationState.stop();
                this.attackAnimationState.startIfStopped(this.tickCount);
            } else if (currentAction == ACTION_REPAIRING) {
                this.attackAnimationState.stop();
                this.repairAnimationState.startIfStopped(this.tickCount);
            }
            this.clientLastActionState = currentAction;
        }

        if (this.attackAnimationState.isStarted()
                && this.attackAnimationState.getTimeInMillis(this.tickCount) >= ATTACK_ANIMATION_TICKS * 50L
                && currentAction != ACTION_ATTACKING) {
            this.attackAnimationState.stop();
        }
        if (this.repairAnimationState.isStarted()
                && this.repairAnimationState.getTimeInMillis(this.tickCount) >= REPAIR_ANIMATION_TICKS * 50L
                && currentAction != ACTION_REPAIRING) {
            this.repairAnimationState.stop();
        }

        this.idleAnimationState.startIfStopped(this.tickCount);

        boolean moving = currentAction != ACTION_REPAIRING && this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
        if (moving) {
            this.walkAnimationState.startIfStopped(this.tickCount);
        } else {
            this.walkAnimationState.stop();
        }

        if (this.tickCount % 8 == 0) {
            this.level().addParticle(
                    ParticleTypes.ASH,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.70D,
                    this.getY() + 0.55D + this.random.nextDouble() * 0.50D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.70D,
                    0.0D,
                    -0.01D,
                    0.0D
            );
        }
        if (this.tickCount % (currentAction == ACTION_REPAIRING ? 4 : 16) == 0) {
            this.level().addParticle(
                    CORE_EMBER_DUST,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.28D,
                    this.getY() + 0.80D + (this.random.nextDouble() - 0.5D) * 0.22D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.28D,
                    0.0D,
                    0.01D,
                    0.0D
            );
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("Variant", Codec.INT, this.getVariant());
        output.store("RepairCooldown", Codec.INT, this.repairCooldown);
        output.store("ShedCooldown", Codec.INT, this.getShedCooldown());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setVariant(input.read("Variant", Codec.INT).orElse(VARIANT_POLAR_ASH_BERG));
        this.repairCooldown = input.read("RepairCooldown", Codec.INT).orElse(0);
        this.setShedCooldown(input.read("ShedCooldown", Codec.INT).orElse(0));
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.SAND_STEP;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.DEEPSLATE_TILES_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.DEEPSLATE_TILES_BREAK;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        // Hovering silt core glides quietly with faint drifting grit
        if (this.random.nextInt(3) == 0) {
            this.playSound(SoundEvents.SAND_STEP, 0.18F, 0.90F);
        }
    }

    static final class DustBlastAttackGoal extends Goal {
        private static final double MIN_HOLD_DIST_SQR = 4.5D * 4.5D; // 20.25
        private static final double MAX_BLAST_DIST_SQR = 7.5D * 7.5D; // 56.25
        private static final double APPROACH_STOP_DIST_SQR = 6.2D * 6.2D; // 38.44

        private final DustCoreGolemEntity golem;

        DustBlastAttackGoal(DustCoreGolemEntity golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.golem.getTarget();
            return target != null
                    && target.isAlive()
                    && !(target instanceof Player p && (p.isCreative() || p.isSpectator()));
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.golem.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }
            if (target instanceof Player p && (p.isCreative() || p.isSpectator())) {
                return false;
            }
            double maxFollow = this.golem.getAttributeValue(Attributes.FOLLOW_RANGE);
            return this.golem.distanceToSqr(target) <= (maxFollow * maxFollow) * 1.25D;
        }

        @Override
        public void stop() {
            this.golem.getNavigation().stop();
        }

        @Override
        public void tick() {
            LivingEntity target = this.golem.getTarget();
            if (target == null || !target.isAlive()) {
                return;
            }

            this.golem.getLookControl().setLookAt(target, 30.0F, 30.0F);

            if (this.golem.isRepairing()) {
                this.golem.getNavigation().stop();
                return;
            }

            double distSqr = this.golem.distanceToSqr(target);
            boolean hasSight = this.golem.getSensing().hasLineOfSight(target);

            if (!hasSight || distSqr > APPROACH_STOP_DIST_SQR) {
                this.golem.getNavigation().moveTo(target, 1.12D);
            } else if (distSqr >= MIN_HOLD_DIST_SQR) {
                this.golem.getNavigation().stop();
            } else {
                // Target closed inside 4.5 blocks: hold ground and face target for point-blank blast
                this.golem.getNavigation().stop();
            }

            if (hasSight
                    && distSqr <= MAX_BLAST_DIST_SQR
                    && this.golem.attackCooldown <= 0
                    && this.golem.level() instanceof ServerLevel serverLevel) {
                this.golem.unleashBlindingDustBlast(serverLevel, target);
            }
        }
    }
}
