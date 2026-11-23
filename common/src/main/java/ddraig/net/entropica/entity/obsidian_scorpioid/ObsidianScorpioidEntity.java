package ddraig.net.entropica.entity.obsidian_scorpioid;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Fauna 53 — Obsidian Scorpioid (Lava Beast) (entropica:obsidian_scorpioid).
 * Hostile stepped crying-obsidian & columnar-basalt scorpion beast native to 6.1 Magma Caverns & 6.2 Basalt Delves:
 * - Crawls in a true alternating hexapod tripod gait and climbs vertical basalt & obsidian surfaces (idle / walk).
 * - Pincer Clamp (pincer_clamp): sweeps both serrated crying-obsidian claws wide and slams them shut for 6 damage,
 *   briefly clamping prey in place for its overhead stinger follow-up!
 * - Molten Tail Sting (attack): whips its 6-segment tail overhead like a spear, dealing 4 fire damage and igniting
 *   the target for 5 seconds.
 * - Immune to fire, lava, and hot magma floors, thriving near lava oceans.
 */
public class ObsidianScorpioidEntity extends Monster {

    private static final EntityDataAccessor<Boolean> CLIMBING =
            SynchedEntityData.defineId(ObsidianScorpioidEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> STINGER_FLARING =
            SynchedEntityData.defineId(ObsidianScorpioidEntity.class, EntityDataSerializers.BOOLEAN);

    private static final byte EVENT_TAIL_STING = 4;
    private static final byte EVENT_PINCER_CLAMP = 61;
    private static final byte EVENT_HURT_BLOCK = 62;

    private static final int TAIL_STING_DURATION_TICKS = 32;   // 1.60s attack
    private static final int PINCER_CLAMP_DURATION_TICKS = 28; // 1.40s pincer_clamp
    private static final int DEATH_DURATION_TICKS = 36;        // 1.80s death hold
    private static final int SPIKE_SHED_COOLDOWN_TICKS = 1200;

    private static final ResourceLocation SCORPIOID_TAIL_SPIKE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "scorpioid_tail_spike");

    private static final DustParticleOptions MAGMA_STINGER_DUST = new DustParticleOptions(0xF97316, 0.72F);
    private static final DustParticleOptions CRYING_VIOLET_DUST = new DustParticleOptions(0xA855F7, 0.58F);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState pincerClampAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    private int tailStingTicks = 0;
    private int pincerClampTicks = 0;
    private int combatCooldown = 0;
    private boolean queueStingFollowup = false;
    private int spikeShedCooldown = 0;

    public ObsidianScorpioidEntity(EntityType<? extends ObsidianScorpioidEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 14;
        this.setPathfindingMalus(PathType.LAVA, 0.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
        this.setPathfindingMalus(PathType.WATER, 8.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.55D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WallClimberNavigation(this, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CLIMBING, false);
        builder.define(STINGER_FLARING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new ScorpioidClampAndStingGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.90D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    public boolean isClimbing() {
        return this.entityData.get(CLIMBING);
    }

    private void setClimbing(boolean climbing) {
        this.entityData.set(CLIMBING, climbing);
    }

    public boolean isStingerFlaring() {
        return this.entityData.get(STINGER_FLARING);
    }

    private void setStingerFlaring(boolean flaring) {
        this.entityData.set(STINGER_FLARING, flaring);
    }

    @Override
    public boolean onClimbable() {
        return this.isClimbing();
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        if (source.is(DamageTypeTags.IS_FIRE)
                || source.is(DamageTypes.LAVA)
                || source.is(DamageTypes.HOT_FLOOR)
                || source.is(DamageTypes.IN_FIRE)
                || source.is(DamageTypes.ON_FIRE)) {
            return true;
        }
        return super.isInvulnerableTo(level, source);
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        if (fallDistance <= 6.5D || this.isClimbing()) {
            return false;
        }
        return super.causeFallDamage(fallDistance - 4.5D, multiplier * 0.5F, source);
    }

    private boolean canScaleAdjacentWall() {
        BlockPos base = this.blockPosition();
        for (int dy = 0; dy <= 1; dy++) {
            BlockPos center = base.above(dy);
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos adj = center.relative(dir);
                BlockState st = this.level().getBlockState(adj);
                if (!st.isAir() && (st.blocksMotion() || st.isFaceSturdy(this.level(), adj, dir.getOpposite()))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            this.updateClientAnimations();
            this.spawnClientStingerParticles();
            return;
        }

        if (!this.isAlive()) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();

        this.setClimbing(this.horizontalCollision && this.canScaleAdjacentWall());

        if (this.combatCooldown > 0) {
            this.combatCooldown--;
        }
        if (this.spikeShedCooldown > 0) {
            this.spikeShedCooldown--;
        }

        // Buoyant wading & thermal mend in lava pools
        if (this.isInLava()) {
            Vec3 vel = this.getDeltaMovement();
            this.setDeltaMovement(vel.x * 1.04D, Math.max(vel.y, 0.06D), vel.z * 1.04D);
            if (this.tickCount % 40 == 0 && this.getHealth() < this.getMaxHealth()) {
                this.heal(1.0F);
            }
        }

        this.tickPincerClamp(serverLevel);
        this.tickTailSting(serverLevel);
        this.setStingerFlaring(this.tailStingTicks > 0);
    }

    private void updateClientAnimations() {
        if (this.isDeadOrDying()) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.attackAnimationState.stop();
            this.pincerClampAnimationState.stop();
            this.hurtAnimationState.stop();
            if (!this.deathAnimationState.isStarted()) {
                this.deathAnimationState.start(this.tickCount);
            }
            return;
        }

        double hSpeedSqr = this.getDeltaMovement().horizontalDistanceSqr();
        boolean crawling = hSpeedSqr > 3.5E-4D || this.isClimbing() || this.walkAnimation.speed() > 0.06F;
        if (crawling) {
            this.walkAnimationState.startIfStopped(this.tickCount);
            this.idleAnimationState.stop();
        } else {
            this.idleAnimationState.startIfStopped(this.tickCount);
            this.walkAnimationState.stop();
        }
    }

    private void spawnClientStingerParticles() {
        if (this.isDeadOrDying()) {
            return;
        }
        if (this.random.nextInt(4) == 0) {
            this.level().addParticle(ParticleTypes.SMALL_FLAME,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.55D,
                    this.getY() + 1.45D + (this.random.nextDouble() - 0.5D) * 0.25D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.55D,
                    0.0D, 0.01D, 0.0D);
        }
        if (this.random.nextInt(7) == 0) {
            this.level().addParticle(CRYING_VIOLET_DUST,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 1.1D,
                    this.getY() + 0.55D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 1.1D,
                    0.0D, 0.01D, 0.0D);
        }
    }

    /**
     * Serrated Crying-Obsidian Pincer Clamp (1.40s = 28 ticks; clamp impact at t = 0.54s -> remaining == 17):
     * deals 6 damage and clamps prey briefly so the overhead molten tail stinger can follow up!
     */
    public void startPincerClamp(ServerLevel serverLevel) {
        if (this.pincerClampTicks > 0 || this.tailStingTicks > 0) {
            return;
        }
        this.pincerClampTicks = PINCER_CLAMP_DURATION_TICKS;
        this.combatCooldown = 30;
        this.queueStingFollowup = false;
        this.getNavigation().stop();
        serverLevel.broadcastEntityEvent(this, EVENT_PINCER_CLAMP);
        serverLevel.playSound(null, this.getX(), this.getY() + 0.6D, this.getZ(),
                SoundEvents.SPIDER_AMBIENT, SoundSource.HOSTILE, 0.9F, 0.68F);
    }

    private void tickPincerClamp(ServerLevel serverLevel) {
        if (this.pincerClampTicks <= 0) {
            return;
        }
        this.pincerClampTicks--;

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        if (this.pincerClampTicks == 17 && target != null && target.isAlive()) {
            if (this.distanceToSqr(target) <= 13.0D) {
                serverLevel.playSound(null, target.getX(), target.getY() + 0.6D, target.getZ(),
                        SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 0.40F, 1.55F);
                if (target.hurtServer(serverLevel, this.damageSources().mobAttack(this), 6.0F)) {
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 26, 3), this);
                    this.queueStingFollowup = true;
                }
            }
        }

        // Immediately chain clamped prey into the overhead molten tail sting!
        if (this.pincerClampTicks == 0 && this.queueStingFollowup && target != null && target.isAlive() && this.distanceToSqr(target) <= 18.5D) {
            this.queueStingFollowup = false;
            this.startMoltenTailSting(serverLevel);
        }
    }

    /**
     * Overhead 6-Segment Molten Magma Tail Sting (1.60s = 32 ticks; spear strike at t = 0.60s -> remaining == 20):
     * deals 4 fire damage and ignites the target for 5 seconds!
     */
    public void startMoltenTailSting(ServerLevel serverLevel) {
        if (this.tailStingTicks > 0 || this.pincerClampTicks > 0) {
            return;
        }
        this.tailStingTicks = TAIL_STING_DURATION_TICKS;
        this.combatCooldown = 36;
        this.setStingerFlaring(true);
        this.getNavigation().stop();
        serverLevel.broadcastEntityEvent(this, EVENT_TAIL_STING);
        serverLevel.playSound(null, this.getX(), this.getY() + 1.1D, this.getZ(),
                SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 0.75F, 1.25F);
    }

    private void tickTailSting(ServerLevel serverLevel) {
        if (this.tailStingTicks <= 0) {
            return;
        }
        this.tailStingTicks--;

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && this.tailStingTicks > 20) {
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        if (this.tailStingTicks == 20 && target != null && target.isAlive()) {
            if (this.distanceToSqr(target) <= 18.5D) {
                float yawRad = this.getYRot() * Mth.DEG_TO_RAD;
                Vec3 tipPos = this.position().add(-Mth.sin(yawRad) * 1.55D, 1.05D, Mth.cos(yawRad) * 1.55D);

                serverLevel.playSound(null, tipPos.x, tipPos.y, tipPos.z,
                        SoundEvents.TRIDENT_HIT, SoundSource.HOSTILE, 0.95F, 1.18F);
                serverLevel.sendParticles(ParticleTypes.LAVA,
                        tipPos.x, tipPos.y, tipPos.z, 8, 0.25D, 0.25D, 0.25D, 0.04D);
                serverLevel.sendParticles(MAGMA_STINGER_DUST,
                        tipPos.x, tipPos.y, tipPos.z, 14, 0.30D, 0.30D, 0.30D, 0.03D);

                target.invulnerableTime = 0;
                if (target.hurtServer(serverLevel, this.damageSources().mobAttack(this), 4.0F)) {
                    target.igniteForSeconds(5.0F);
                }
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(serverLevel, source, amount);
        if (hurt && this.isAlive() && this.tailStingTicks <= 0 && this.pincerClampTicks <= 0) {
            serverLevel.broadcastEntityEvent(this, EVENT_HURT_BLOCK);
        }
        return hurt;
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_TAIL_STING -> this.attackAnimationState.start(this.tickCount);
            case EVENT_PINCER_CLAMP -> this.pincerClampAnimationState.start(this.tickCount);
            case EVENT_HURT_BLOCK -> this.hurtAnimationState.start(this.tickCount);
            default -> super.handleEntityEvent(id);
        }
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (!this.level().isClientSide() && this.deathTime == 1 && this.level() instanceof ServerLevel serverLevel) {
            this.setClimbing(false);
            this.setStingerFlaring(false);
            serverLevel.sendParticles(ParticleTypes.SMOKE,
                    this.getX(), this.getY() + 0.65D, this.getZ(),
                    16, 0.55D, 0.35D, 0.55D, 0.03D);
        }
        if (this.deathTime >= DEATH_DURATION_TICKS && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty() || !this.isAlive() || this.getTarget() != null) {
            return super.mobInteract(player, hand);
        }

        // Sneaking offering of Magma Cream or Crying Obsidian when unaggroed sheds a Scorpioid Tail-Spike
        if (player.isCrouching() && (stack.is(Items.MAGMA_CREAM) || stack.is(Items.CRYING_OBSIDIAN))) {
            if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.heal(6.0F);
                serverLevel.sendParticles(MAGMA_STINGER_DUST,
                        this.getX(), this.getY() + 1.25D, this.getZ(),
                        16, 0.35D, 0.35D, 0.35D, 0.03D);
                if (this.spikeShedCooldown <= 0) {
                    this.spikeShedCooldown = SPIKE_SHED_COOLDOWN_TICKS;
                    Item spikeItem = BuiltInRegistries.ITEM.getOptional(SCORPIOID_TAIL_SPIKE_ID)
                            .orElse(ModItems.SCORPIOID_TAIL_SPIKE.get());
                    this.spawnAtLocation(serverLevel, new ItemStack(spikeItem));
                }
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    public static boolean checkObsidianScorpioidSpawnRules(
            EntityType<ObsidianScorpioidEntity> type,
            ServerLevelAccessor level,
            EntitySpawnReason spawnReason,
            BlockPos pos,
            RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        if (spawnReason == EntitySpawnReason.SPAWNER || spawnReason == EntitySpawnReason.TRIAL_SPAWNER) {
            return true;
        }
        BlockState below = level.getBlockState(pos.below());
        String belowPath = BuiltInRegistries.BLOCK.getKey(below.getBlock()).getPath();
        boolean validFloor = below.is(Blocks.BASALT)
                || below.is(Blocks.SMOOTH_BASALT)
                || below.is(Blocks.POLISHED_BASALT)
                || below.is(Blocks.BLACKSTONE)
                || below.is(Blocks.OBSIDIAN)
                || below.is(Blocks.CRYING_OBSIDIAN)
                || below.is(Blocks.MAGMA_BLOCK)
                || below.is(Blocks.NETHERRACK)
                || below.getFluidState().is(FluidTags.LAVA)
                || belowPath.contains("basalt")
                || belowPath.contains("obsidian")
                || belowPath.contains("magma");
        return validFloor && level.getBlockState(pos).isAir();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("SpikeShedCooldown", Codec.INT, this.spikeShedCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.spikeShedCooldown = input.read("SpikeShedCooldown", Codec.INT).orElse(0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SPIDER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.DEEPSLATE_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SPIDER_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.12F + 0.65F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SPIDER_STEP, 0.35F, 0.72F);
    }

    private static final class ScorpioidClampAndStingGoal extends Goal {
        private final ObsidianScorpioidEntity scorpioid;
        private int repathTicks = 0;

        ScorpioidClampAndStingGoal(ObsidianScorpioidEntity scorpioid) {
            this.scorpioid = scorpioid;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.scorpioid.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.scorpioid.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }
            if (target instanceof Player p && (p.isCreative() || p.isSpectator())) {
                return false;
            }
            return this.scorpioid.distanceToSqr(target) <= 676.0D;
        }

        @Override
        public void stop() {
            this.scorpioid.getNavigation().stop();
        }

        @Override
        public void tick() {
            LivingEntity target = this.scorpioid.getTarget();
            if (target == null || !(this.scorpioid.level() instanceof ServerLevel serverLevel)) {
                return;
            }

            this.scorpioid.getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (this.scorpioid.tailStingTicks > 0 || this.scorpioid.pincerClampTicks > 0) {
                return;
            }

            if (--this.repathTicks <= 0) {
                this.repathTicks = 4 + this.scorpioid.getRandom().nextInt(5);
                this.scorpioid.getNavigation().moveTo(target, 1.18D);
            }

            if (this.scorpioid.combatCooldown > 0 || !this.scorpioid.hasLineOfSight(target)) {
                return;
            }

            double distSqr = this.scorpioid.distanceToSqr(target);
            if (distSqr <= 10.5D) {
                // Close quarters: 6-damage Pincer Clamp (chains directly into 4-fire-damage Molten Tail Sting!)
                if (this.scorpioid.getRandom().nextInt(3) != 0) {
                    this.scorpioid.startPincerClamp(serverLevel);
                } else {
                    this.scorpioid.startMoltenTailSting(serverLevel);
                }
            } else if (distSqr <= 17.5D) {
                // Extended stinger spear reach!
                this.scorpioid.startMoltenTailSting(serverLevel);
            }
        }
    }
}
