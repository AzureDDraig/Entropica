package ddraig.net.entropica.entity.obsidian_wendigo;

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
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
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
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * Fauna 59 — Obsidian Wendigo (Forest Fiend) (entropica:obsidian_wendigo).
 * Hostile towering skeletal forest fiend native to 3.9 Blighted Copse & 5.23 Petrified Wood:
 * - Creeps in a deeply hunched, marionette-like forest stalk (walk_start -> walk -> walk_stop),
 *   lunges into a feral forward-pitched claw-reaching sprint (run_start -> run -> run_stop),
 *   and scrambles continuously hand-over-hand up tall petrified or blighted tree trunks
 *   (climb_start -> climb -> climb_stop).
 * - Rakes targets with long obsidian bone claws (attack, 8.0 damage) and unleashes a bone-chilling
 *   fear howl (screech) that freezes nearby targets in terror (Slowness V for 2 seconds).
 * - Stalks from shadows, becoming completely invisible when light level is below 4.
 * - Drops 1 Wendigo Skull-Plate (entropica:wendigo_skull_plate) when slain.
 */
public class ObsidianWendigoEntity extends Monster {

    private static final EntityDataAccessor<Boolean> SPRINTING_RUN =
            SynchedEntityData.defineId(ObsidianWendigoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CLIMBING_TRUNK =
            SynchedEntityData.defineId(ObsidianWendigoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SCREECHING =
            SynchedEntityData.defineId(ObsidianWendigoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SHADOW_CLOAKED =
            SynchedEntityData.defineId(ObsidianWendigoEntity.class, EntityDataSerializers.BOOLEAN);

    private static final byte EVENT_ATTACK = 4;
    private static final byte EVENT_SCREECH = 61;
    private static final byte EVENT_HURT = 62;

    // Exact animation lengths from obsidian_wendigo_animations.java (20 TPS)
    private static final int WALK_START_TICKS = 15;   // 0.75s
    private static final int WALK_STOP_TICKS = 16;    // 0.80s
    private static final int RUN_START_TICKS = 14;    // 0.70s
    private static final int RUN_STOP_TICKS = 17;     // 0.85s
    private static final int CLIMB_START_TICKS = 15;  // 0.75s
    private static final int CLIMB_STOP_TICKS = 16;   // 0.80s

    private static final int ATTACK_DURATION_TICKS = 27;   // 1.35s
    private static final int ATTACK_PRIMARY_HIT_TICK = 11; // 0.54s right-claw rip
    private static final int ATTACK_CROSS_HIT_TICK = 16;   // 0.80s left-claw cross-rake & jaw snap

    private static final int SCREECH_DURATION_TICKS = 40;  // 2.00s
    private static final int SCREECH_HOWL_TICK = 13;       // 0.65s backward spine arch terror howl
    private static final int SCREECH_COOLDOWN_TICKS = 160; // 8.0s between terror howls
    private static final int TERROR_FREEZE_TICKS = 40;     // 2.0s Slowness V (amplifier 4)
    private static final double TERROR_HOWL_RADIUS = 9.0D;

    private static final int HURT_DURATION_TICKS = 13;     // 0.65s
    private static final int DEATH_DURATION_TICKS = 37;    // 1.85s hold
    private static final int SHED_COOLDOWN_TICKS = 1200;

    private static final int SHADOW_LIGHT_THRESHOLD = 4;   // Completely invisible when light < 4

    private static final DustParticleOptions FEAR_RUNE_PURPLE = new DustParticleOptions(0x7C3AED, 0.90F);
    private static final DustParticleOptions FEAR_RUNE_MAGENTA = new DustParticleOptions(0xD946EF, 0.75F);
    private static final DustParticleOptions SHADOW_OBSIDIAN_DUST = new DustParticleOptions(0x180E29, 0.85F);

    private static final ResourceLocation SKULL_PLATE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "wendigo_skull_plate");

    private enum LocoPhase {
        IDLE,
        WALK_START,
        WALK,
        WALK_STOP,
        RUN_START,
        RUN,
        RUN_STOP,
        CLIMB_START,
        CLIMB,
        CLIMB_STOP
    }

    // All 14 Blockbench animations
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkStartAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState walkStopAnimationState = new AnimationState();
    public final AnimationState runStartAnimationState = new AnimationState();
    public final AnimationState runAnimationState = new AnimationState();
    public final AnimationState runStopAnimationState = new AnimationState();
    public final AnimationState climbStartAnimationState = new AnimationState();
    public final AnimationState climbAnimationState = new AnimationState();
    public final AnimationState climbStopAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState screechAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    // Server combat & offering timers
    private int attackAnimTicks = 0;
    private boolean attackDealtDamage = false;
    private int attackCooldown = 0;
    private int screechAnimTicks = 0;
    private int screechCooldown = 30;
    private int shedCooldown = 0;

    // Client locomotion state machine
    private LocoPhase clientLocoPhase = LocoPhase.IDLE;
    private int clientLocoPhaseTicks = 0;
    private int clientHurtTicks = 0;

    public ObsidianWendigoEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 14;
        this.setPathfindingMalus(PathType.WATER, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 105.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.35D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.STEP_HEIGHT, 1.1D);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WallClimberNavigation(this, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SPRINTING_RUN, false);
        builder.define(CLIMBING_TRUNK, false);
        builder.define(SCREECHING, false);
        builder.define(SHADOW_CLOAKED, false);
    }

    public boolean isSprintingRun() {
        return this.entityData.get(SPRINTING_RUN);
    }

    public void setSprintingRun(boolean sprinting) {
        this.entityData.set(SPRINTING_RUN, sprinting);
    }

    public boolean isClimbingTrunk() {
        return this.entityData.get(CLIMBING_TRUNK);
    }

    public void setClimbingTrunk(boolean climbing) {
        this.entityData.set(CLIMBING_TRUNK, climbing);
    }

    public boolean isScreeching() {
        return this.entityData.get(SCREECHING);
    }

    private void setScreeching(boolean screeching) {
        this.entityData.set(SCREECHING, screeching);
    }

    public boolean isShadowCloaked() {
        return this.entityData.get(SHADOW_CLOAKED);
    }

    private void setShadowCloaked(boolean cloaked) {
        this.entityData.set(SHADOW_CLOAKED, cloaked);
    }

    @Override
    public boolean onClimbable() {
        return this.isClimbingTrunk() || super.onClimbable();
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageMultiplier, DamageSource damageSource) {
        // Long digitigrade legs and trunk-scrambling claws cushion drops from tall trees
        if (fallDistance <= 12.0D) {
            return false;
        }
        return super.causeFallDamage(fallDistance - 8.0D, damageMultiplier * 0.4F, damageSource);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WendigoHuntAndHowlGoal(this));
        this.goalSelector.addGoal(2, new WendigoTrunkScrambleGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    public static boolean checkObsidianWendigoSpawnRules(
            EntityType<ObsidianWendigoEntity> type,
            ServerLevelAccessor level,
            EntitySpawnReason spawnReason,
            BlockPos pos,
            RandomSource random
    ) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        if (EntitySpawnReason.isSpawner(spawnReason)
                || spawnReason == EntitySpawnReason.SPAWN_ITEM_USE
                || spawnReason == EntitySpawnReason.COMMAND
                || spawnReason == EntitySpawnReason.EVENT) {
            return true;
        }
        BlockState below = level.getBlockState(pos.below());
        if (!below.blocksMotion()) {
            return false;
        }
        return Monster.isDarkEnoughToSpawn(level, pos, random) || isWendigoForestSurface(below);
    }

    private static boolean isWendigoForestSurface(BlockState state) {
        if (state.is(BlockTags.DIRT) || state.is(BlockTags.LOGS) || state.is(Blocks.PODZOL)
                || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.MYCELIUM) || state.is(Blocks.BASALT)) {
            return true;
        }
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return path.contains("blight") || path.contains("petrified") || path.contains("necrotic") || path.contains("barrow");
    }

    public boolean isClimbableTrunkBlock(BlockState state) {
        if (state.isAir()) {
            return false;
        }
        if (state.is(BlockTags.LOGS) || state.is(Blocks.BASALT) || state.is(Blocks.POLISHED_BASALT)
                || state.is(Blocks.SMOOTH_BASALT) || state.is(Blocks.OBSIDIAN) || state.is(Blocks.CRYING_OBSIDIAN)) {
            return true;
        }
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return path.contains("log")
                || path.contains("wood")
                || path.contains("stem")
                || path.contains("trunk")
                || path.contains("bark")
                || path.contains("petrified")
                || path.contains("blight")
                || path.contains("pillar");
    }

    private boolean hasAdjacentClimbableSurface() {
        BlockPos base = this.blockPosition();
        for (int dy = 0; dy <= 2; dy++) {
            BlockPos checkCenter = base.above(dy);
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockState neighbor = this.level().getBlockState(checkCenter.relative(dir));
                if (isClimbableTrunkBlock(neighbor) || (this.getTarget() != null && neighbor.blocksMotion())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            if (this.attackCooldown > 0) this.attackCooldown--;
            if (this.screechCooldown > 0) this.screechCooldown--;
            if (this.shedCooldown > 0) this.shedCooldown--;

            updateShadowStalkingInvisibility();
            updateServerClimbingState();
            tickServerAttack();
            tickServerScreech();
        } else {
            tickClientAnimations();
            tickClientRuneParticles();
        }
    }

    private void updateShadowStalkingInvisibility() {
        if (this.isDeadOrDying()) {
            if (this.isShadowCloaked()) {
                this.setShadowCloaked(false);
            }
            if (!this.hasEffect(MobEffects.INVISIBILITY)) {
                this.setInvisible(false);
            }
            return;
        }

        int light = this.level().getMaxLocalRawBrightness(this.blockPosition());
        boolean shouldCloak = light < SHADOW_LIGHT_THRESHOLD;
        boolean wasCloaked = this.isShadowCloaked();

        if (shouldCloak != wasCloaked) {
            this.setShadowCloaked(shouldCloak);
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(SHADOW_OBSIDIAN_DUST,
                        this.getX(), this.getY() + 1.5D, this.getZ(),
                        12, 0.35D, 0.85D, 0.35D, 0.02D);
                serverLevel.sendParticles(FEAR_RUNE_PURPLE,
                        this.getX(), this.getY() + 2.4D, this.getZ(),
                        5, 0.30D, 0.30D, 0.30D, 0.01D);
            }
        }
        this.setInvisible(shouldCloak || this.hasEffect(MobEffects.INVISIBILITY));
    }

    private void updateServerClimbingState() {
        if (this.isDeadOrDying() || this.screechAnimTicks > 0) {
            if (this.isClimbingTrunk()) {
                this.setClimbingTrunk(false);
            }
            return;
        }
        boolean climbingNow = this.horizontalCollision && this.hasAdjacentClimbableSurface();
        if (climbingNow != this.isClimbingTrunk()) {
            this.setClimbingTrunk(climbingNow);
        }
        if (climbingNow) {
            Vec3 dm = this.getDeltaMovement();
            if (dm.y < 0.22D) {
                this.setDeltaMovement(dm.x * 0.85D, 0.22D, dm.z * 0.85D);
            }
            if (this.tickCount % 7 == 0) {
                this.playSound(SoundEvents.WOOD_STEP, 0.55F, 0.65F + this.random.nextFloat() * 0.2F);
            }
        }
    }

    public void startClawAttack() {
        if (this.attackAnimTicks > 0 || this.screechAnimTicks > 0) {
            return;
        }
        this.attackAnimTicks = ATTACK_DURATION_TICKS;
        this.attackDealtDamage = false;
        this.attackCooldown = 28;
        this.level().broadcastEntityEvent(this, EVENT_ATTACK);
        this.playSound(SoundEvents.SKELETON_STEP, 0.9F, 0.55F);
    }

    private void tickServerAttack() {
        if (this.attackAnimTicks <= 0) {
            return;
        }
        int elapsed = ATTACK_DURATION_TICKS - this.attackAnimTicks;
        this.attackAnimTicks--;

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        if (!this.attackDealtDamage && (elapsed == ATTACK_PRIMARY_HIT_TICK || elapsed == ATTACK_CROSS_HIT_TICK)) {
            this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 0.65F + this.random.nextFloat() * 0.15F);
            if (target != null && target.isAlive() && this.level() instanceof ServerLevel serverLevel) {
                double distSq = this.distanceToSqr(target);
                if (distSq <= 12.25D && this.hasLineOfSight(target)) {
                    float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE); // 8.0F
                    if (target.hurtServer(serverLevel, this.damageSources().mobAttack(this), damage)) {
                        this.attackDealtDamage = true;
                        Vec3 knock = target.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D).normalize();
                        target.push(knock.x * 0.45D, 0.20D, knock.z * 0.45D);
                        serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                                target.getX(), target.getY(0.55D), target.getZ(),
                                1, 0.0D, 0.0D, 0.0D, 0.0D);
                        serverLevel.sendParticles(FEAR_RUNE_PURPLE,
                                target.getX(), target.getY(0.6D), target.getZ(),
                                8, 0.25D, 0.35D, 0.25D, 0.02D);
                    }
                }
            }
        }
    }

    public void startFearScreech() {
        if (this.screechAnimTicks > 0 || this.attackAnimTicks > 0) {
            return;
        }
        this.screechAnimTicks = SCREECH_DURATION_TICKS;
        this.screechCooldown = SCREECH_COOLDOWN_TICKS;
        this.setScreeching(true);
        this.setSprintingRun(false);
        this.getNavigation().stop();
        this.level().broadcastEntityEvent(this, EVENT_SCREECH);
        this.playSound(SoundEvents.SKELETON_AMBIENT, 1.1F, 0.45F);
    }

    private void tickServerScreech() {
        if (this.screechAnimTicks <= 0) {
            if (this.isScreeching()) {
                this.setScreeching(false);
            }
            return;
        }
        int elapsed = SCREECH_DURATION_TICKS - this.screechAnimTicks;
        this.screechAnimTicks--;
        this.getNavigation().stop();

        LivingEntity focus = this.getTarget();
        if (focus != null && focus.isAlive()) {
            this.getLookControl().setLookAt(focus, 25.0F, 25.0F);
        }

        if (elapsed == SCREECH_HOWL_TICK && this.level() instanceof ServerLevel serverLevel) {
            // Bone-chilling terror howl: freezes nearby targets in terror (Slowness V for 2 seconds)
            serverLevel.playSound(null, this.getX(), this.getY() + 2.2D, this.getZ(),
                    SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 1.35F, 1.65F);
            serverLevel.playSound(null, this.getX(), this.getY() + 2.2D, this.getZ(),
                    SoundEvents.SKELETON_HORSE_DEATH, SoundSource.HOSTILE, 1.15F, 0.55F);

            serverLevel.sendParticles(FEAR_RUNE_PURPLE,
                    this.getX(), this.getY() + 2.6D, this.getZ(),
                    28, 0.85D, 0.55D, 0.85D, 0.06D);
            serverLevel.sendParticles(FEAR_RUNE_MAGENTA,
                    this.getX(), this.getY() + 2.6D, this.getZ(),
                    18, 0.65D, 0.45D, 0.65D, 0.05D);

            List<LivingEntity> victims = serverLevel.getEntitiesOfClass(
                    LivingEntity.class,
                    this.getBoundingBox().inflate(TERROR_HOWL_RADIUS),
                    e -> e != this
                            && e.isAlive()
                            && !(e instanceof ObsidianWendigoEntity)
                            && (!(e instanceof Player p) || (!p.isCreative() && !p.isSpectator()))
            );
            for (LivingEntity victim : victims) {
                // Slowness V (amplifier 4) for 2 seconds (40 ticks)
                victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, TERROR_FREEZE_TICKS, 4, false, true, true));
                serverLevel.sendParticles(FEAR_RUNE_PURPLE,
                        victim.getX(), victim.getY() + victim.getBbHeight() * 0.6D, victim.getZ(),
                        12, 0.3D, 0.4D, 0.3D, 0.02D);
                serverLevel.sendParticles(FEAR_RUNE_MAGENTA,
                        victim.getX(), victim.getY() + victim.getBbHeight() * 0.8D, victim.getZ(),
                        6, 0.25D, 0.3D, 0.25D, 0.01D);
            }
        }

        if (this.screechAnimTicks <= 0) {
            this.setScreeching(false);
        }
    }

    private void stopAllClientLocomotionStates() {
        this.idleAnimationState.stop();
        this.walkStartAnimationState.stop();
        this.walkAnimationState.stop();
        this.walkStopAnimationState.stop();
        this.runStartAnimationState.stop();
        this.runAnimationState.stop();
        this.runStopAnimationState.stop();
        this.climbStartAnimationState.stop();
        this.climbAnimationState.stop();
        this.climbStopAnimationState.stop();
    }

    private void enterClientLocoPhase(LocoPhase next, int durationTicks) {
        this.stopAllClientLocomotionStates();
        this.clientLocoPhase = next;
        this.clientLocoPhaseTicks = durationTicks;
        switch (next) {
            case IDLE -> this.idleAnimationState.start(this.tickCount);
            case WALK_START -> this.walkStartAnimationState.start(this.tickCount);
            case WALK -> this.walkAnimationState.start(this.tickCount);
            case WALK_STOP -> this.walkStopAnimationState.start(this.tickCount);
            case RUN_START -> this.runStartAnimationState.start(this.tickCount);
            case RUN -> this.runAnimationState.start(this.tickCount);
            case RUN_STOP -> this.runStopAnimationState.start(this.tickCount);
            case CLIMB_START -> this.climbStartAnimationState.start(this.tickCount);
            case CLIMB -> this.climbAnimationState.start(this.tickCount);
            case CLIMB_STOP -> this.climbStopAnimationState.start(this.tickCount);
        }
    }

    private void tickClientAnimations() {
        if (this.isDeadOrDying()) {
            this.stopAllClientLocomotionStates();
            this.attackAnimationState.stop();
            this.screechAnimationState.stop();
            this.hurtAnimationState.stop();
            this.deathAnimationState.startIfStopped(this.tickCount);
            return;
        }

        if (this.clientHurtTicks > 0) {
            this.clientHurtTicks--;
            if (this.clientHurtTicks == 0) {
                this.hurtAnimationState.stop();
            }
        }

        Vec3 dm = this.getDeltaMovement();
        double horizSpeedSq = dm.x * dm.x + dm.z * dm.z;
        boolean moving = horizSpeedSq > 0.0009D || this.walkAnimation.speed() > 0.06F;
        boolean screeching = this.isScreeching();
        boolean climbing = !screeching && this.isClimbingTrunk();
        boolean running = !screeching && !climbing && moving && this.isSprintingRun();
        boolean walking = !screeching && !climbing && moving && !running;

        if (this.clientLocoPhaseTicks > 0) {
            this.clientLocoPhaseTicks--;
        }

        switch (this.clientLocoPhase) {
            case IDLE -> {
                if (climbing) {
                    enterClientLocoPhase(LocoPhase.CLIMB_START, CLIMB_START_TICKS);
                } else if (running) {
                    enterClientLocoPhase(LocoPhase.RUN_START, RUN_START_TICKS);
                } else if (walking) {
                    enterClientLocoPhase(LocoPhase.WALK_START, WALK_START_TICKS);
                } else {
                    this.idleAnimationState.startIfStopped(this.tickCount);
                }
            }
            case WALK_START -> {
                if (climbing) {
                    enterClientLocoPhase(LocoPhase.CLIMB_START, CLIMB_START_TICKS);
                } else if (running) {
                    enterClientLocoPhase(LocoPhase.RUN_START, RUN_START_TICKS);
                } else if (this.clientLocoPhaseTicks <= 0) {
                    if (walking) {
                        enterClientLocoPhase(LocoPhase.WALK, 0);
                    } else {
                        enterClientLocoPhase(LocoPhase.WALK_STOP, WALK_STOP_TICKS);
                    }
                }
            }
            case WALK -> {
                if (climbing) {
                    enterClientLocoPhase(LocoPhase.CLIMB_START, CLIMB_START_TICKS);
                } else if (running) {
                    enterClientLocoPhase(LocoPhase.RUN_START, RUN_START_TICKS);
                } else if (!walking) {
                    enterClientLocoPhase(LocoPhase.WALK_STOP, WALK_STOP_TICKS);
                } else {
                    this.walkAnimationState.startIfStopped(this.tickCount);
                }
            }
            case WALK_STOP -> {
                if (climbing) {
                    enterClientLocoPhase(LocoPhase.CLIMB_START, CLIMB_START_TICKS);
                } else if (running) {
                    enterClientLocoPhase(LocoPhase.RUN_START, RUN_START_TICKS);
                } else if (this.clientLocoPhaseTicks <= 0) {
                    if (walking) {
                        enterClientLocoPhase(LocoPhase.WALK_START, WALK_START_TICKS);
                    } else {
                        enterClientLocoPhase(LocoPhase.IDLE, 0);
                    }
                }
            }
            case RUN_START -> {
                if (climbing) {
                    enterClientLocoPhase(LocoPhase.CLIMB_START, CLIMB_START_TICKS);
                } else if (this.clientLocoPhaseTicks <= 0) {
                    if (running) {
                        enterClientLocoPhase(LocoPhase.RUN, 0);
                    } else {
                        enterClientLocoPhase(LocoPhase.RUN_STOP, RUN_STOP_TICKS);
                    }
                }
            }
            case RUN -> {
                if (climbing) {
                    enterClientLocoPhase(LocoPhase.CLIMB_START, CLIMB_START_TICKS);
                } else if (!running) {
                    enterClientLocoPhase(LocoPhase.RUN_STOP, RUN_STOP_TICKS);
                } else {
                    this.runAnimationState.startIfStopped(this.tickCount);
                }
            }
            case RUN_STOP -> {
                if (climbing) {
                    enterClientLocoPhase(LocoPhase.CLIMB_START, CLIMB_START_TICKS);
                } else if (this.clientLocoPhaseTicks <= 0) {
                    if (running) {
                        enterClientLocoPhase(LocoPhase.RUN_START, RUN_START_TICKS);
                    } else if (walking) {
                        enterClientLocoPhase(LocoPhase.WALK_START, WALK_START_TICKS);
                    } else {
                        enterClientLocoPhase(LocoPhase.IDLE, 0);
                    }
                }
            }
            case CLIMB_START -> {
                if (this.clientLocoPhaseTicks <= 0) {
                    if (climbing) {
                        enterClientLocoPhase(LocoPhase.CLIMB, 0);
                    } else {
                        enterClientLocoPhase(LocoPhase.CLIMB_STOP, CLIMB_STOP_TICKS);
                    }
                }
            }
            case CLIMB -> {
                if (!climbing) {
                    enterClientLocoPhase(LocoPhase.CLIMB_STOP, CLIMB_STOP_TICKS);
                } else {
                    this.climbAnimationState.startIfStopped(this.tickCount);
                }
            }
            case CLIMB_STOP -> {
                if (this.clientLocoPhaseTicks <= 0) {
                    if (climbing) {
                        enterClientLocoPhase(LocoPhase.CLIMB_START, CLIMB_START_TICKS);
                    } else if (running) {
                        enterClientLocoPhase(LocoPhase.RUN_START, RUN_START_TICKS);
                    } else if (walking) {
                        enterClientLocoPhase(LocoPhase.WALK_START, WALK_START_TICKS);
                    } else {
                        enterClientLocoPhase(LocoPhase.IDLE, 0);
                    }
                }
            }
        }
    }

    private void tickClientRuneParticles() {
        if (this.isDeadOrDying() || this.isShadowCloaked()) {
            return;
        }
        if (this.isScreeching() || this.random.nextInt(14) == 0) {
            double ox = (this.random.nextDouble() - 0.5D) * 0.9D;
            double oy = 2.55D + this.random.nextDouble() * 0.65D;
            double oz = (this.random.nextDouble() - 0.5D) * 0.9D;
            this.level().addParticle(
                    this.random.nextBoolean() ? FEAR_RUNE_PURPLE : FEAR_RUNE_MAGENTA,
                    this.getX() + ox, this.getY() + oy, this.getZ() + oz,
                    0.0D, 0.01D, 0.0D
            );
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_ATTACK) {
            this.attackAnimationState.start(this.tickCount);
        } else if (id == EVENT_SCREECH) {
            this.screechAnimationState.start(this.tickCount);
        } else if (id == EVENT_HURT) {
            this.hurtAnimationState.start(this.tickCount);
            this.clientHurtTicks = HURT_DURATION_TICKS;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && this.isAlive()) {
            level.broadcastEntityEvent(this, EVENT_HURT);
        }
        return hurt;
    }

    @Override
    protected void tickDeath() {
        ++this.deathTime;
        if (this.deathTime >= DEATH_DURATION_TICKS && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()
                && this.getTarget() == null
                && this.shedCooldown <= 0
                && isWendigoOffering(stack)) {
            if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.heal(5.0F);
                this.shedCooldown = SHED_COOLDOWN_TICKS;
                Optional<Item> plateOpt = BuiltInRegistries.ITEM.getOptional(SKULL_PLATE_ID);
                Item dropItem = plateOpt.orElseGet(ModItems.WENDIGO_SKULL_PLATE::get);
                this.spawnAtLocation(serverLevel, new ItemStack(dropItem));
                serverLevel.playSound(null, this.getX(), this.getY() + 2.0D, this.getZ(),
                        SoundEvents.BONE_BLOCK_BREAK, SoundSource.NEUTRAL, 0.9F, 0.7F);
                serverLevel.sendParticles(FEAR_RUNE_PURPLE,
                        this.getX(), this.getY() + 2.4D, this.getZ(),
                        14, 0.35D, 0.35D, 0.35D, 0.02D);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    private static boolean isWendigoOffering(ItemStack stack) {
        return stack.is(Items.BONE)
                || stack.is(Items.BONE_BLOCK)
                || stack.is(Items.ROTTEN_FLESH)
                || stack.is(Items.CRYING_OBSIDIAN)
                || stack.is(Items.OBSIDIAN);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("ShedCooldown", Codec.INT, this.shedCooldown);
        output.store("ScreechCooldown", Codec.INT, this.screechCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.shedCooldown = input.read("ShedCooldown", Codec.INT).orElse(0);
        this.screechCooldown = input.read("ScreechCooldown", Codec.INT).orElse(0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.WITHER_SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_SKELETON_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 0.58F + this.random.nextFloat() * 0.14F;
    }

    /**
     * Hunts targets with feral claw-reaching sprints (run_start -> run -> run_stop),
     * bone-chilling Slowness V terror howls (screech), and 8-damage obsidian claw rakes (attack).
     */
    private static class WendigoHuntAndHowlGoal extends Goal {
        private final ObsidianWendigoEntity wendigo;
        private int repathTicker = 0;

        WendigoHuntAndHowlGoal(ObsidianWendigoEntity wendigo) {
            this.wendigo = wendigo;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.wendigo.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.wendigo.getTarget();
            return target != null && target.isAlive()
                    && (this.wendigo.distanceToSqr(target) <= 1296.0D || this.wendigo.screechAnimTicks > 0);
        }

        @Override
        public void start() {
            this.repathTicker = 0;
        }

        @Override
        public void stop() {
            this.wendigo.setSprintingRun(false);
            this.wendigo.getNavigation().stop();
        }

        @Override
        public void tick() {
            LivingEntity target = this.wendigo.getTarget();
            if (target == null) {
                return;
            }

            this.wendigo.getLookControl().setLookAt(target, 30.0F, 30.0F);

            if (this.wendigo.screechAnimTicks > 0) {
                this.wendigo.setSprintingRun(false);
                this.wendigo.getNavigation().stop();
                return;
            }

            double distSq = this.wendigo.distanceToSqr(target);

            // Unleash terror howl to freeze target in Slowness V for 2s before closing in
            if (this.wendigo.screechCooldown <= 0
                    && this.wendigo.attackAnimTicks <= 0
                    && distSq >= 10.0D
                    && distSq <= 90.0D
                    && this.wendigo.hasLineOfSight(target)) {
                this.wendigo.startFearScreech();
                return;
            }

            // Creeping stalk at long shadow range (> 18 blocks while unseen in dark), feral sprint once closing in!
            boolean creepyShadowStalk = this.wendigo.isShadowCloaked() && distSq > 324.0D;
            boolean sprintNow = !creepyShadowStalk && distSq > 4.0D;
            this.wendigo.setSprintingRun(sprintNow);
            double moveSpeed = sprintNow ? 1.58D : 0.88D;

            if (--this.repathTicker <= 0) {
                this.repathTicker = 4 + this.wendigo.random.nextInt(4);
                this.wendigo.getNavigation().moveTo(target, moveSpeed);
            }

            if (distSq <= 10.5D && this.wendigo.attackCooldown <= 0 && this.wendigo.attackAnimTicks <= 0) {
                this.wendigo.startClawAttack();
            }
        }
    }

    /**
     * Periodically approaches a tall petrified, blighted, or wooden tree trunk when unaggroed
     * and scrambles continuously hand-over-hand up the bark (climb_start -> climb -> climb_stop).
     */
    private static class WendigoTrunkScrambleGoal extends Goal {
        private final ObsidianWendigoEntity wendigo;
        private @Nullable BlockPos trunkBasePos;
        private Direction climbFace = Direction.NORTH;
        private int climbTicks = 0;
        private int searchCooldown = 80;

        WendigoTrunkScrambleGoal(ObsidianWendigoEntity wendigo) {
            this.wendigo = wendigo;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.wendigo.getTarget() != null || this.wendigo.screechAnimTicks > 0) {
                return false;
            }
            if (this.searchCooldown > 0) {
                this.searchCooldown--;
                return false;
            }
            this.searchCooldown = 140 + this.wendigo.random.nextInt(140);
            BlockPos origin = this.wendigo.blockPosition();
            for (int i = 0; i < 14; i++) {
                BlockPos candidate = origin.offset(
                        this.wendigo.random.nextInt(15) - 7,
                        this.wendigo.random.nextInt(5) - 2,
                        this.wendigo.random.nextInt(15) - 7
                );
                if (isTallClimbableTrunk(candidate)) {
                    for (Direction dir : Direction.Plane.HORIZONTAL) {
                        BlockPos stand = candidate.relative(dir);
                        if (this.wendigo.level().getBlockState(stand).isAir()
                                && this.wendigo.level().getBlockState(stand.above()).isAir()) {
                            this.trunkBasePos = candidate;
                            this.climbFace = dir;
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        private boolean isTallClimbableTrunk(BlockPos pos) {
            for (int dy = 0; dy < 4; dy++) {
                if (!this.wendigo.isClimbableTrunkBlock(this.wendigo.level().getBlockState(pos.above(dy)))) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.wendigo.getTarget() == null
                    && this.trunkBasePos != null
                    && this.climbTicks < 110;
        }

        @Override
        public void start() {
            this.climbTicks = 0;
            this.wendigo.setSprintingRun(false);
        }

        @Override
        public void stop() {
            this.trunkBasePos = null;
            this.wendigo.setClimbingTrunk(false);
        }

        @Override
        public void tick() {
            if (this.trunkBasePos == null) {
                return;
            }
            this.climbTicks++;
            BlockPos standPos = this.trunkBasePos.relative(this.climbFace);
            double tx = standPos.getX() + 0.5D - this.climbFace.getStepX() * 0.28D;
            double tz = standPos.getZ() + 0.5D - this.climbFace.getStepZ() * 0.28D;

            this.wendigo.getLookControl().setLookAt(
                    this.trunkBasePos.getX() + 0.5D,
                    this.wendigo.getY() + 3.0D,
                    this.trunkBasePos.getZ() + 0.5D,
                    25.0F, 30.0F
            );

            double dx = tx - this.wendigo.getX();
            double dz = tz - this.wendigo.getZ();
            if (dx * dx + dz * dz > 1.35D) {
                this.wendigo.getNavigation().moveTo(tx, standPos.getY(), tz, 0.95D);
            } else {
                BlockPos trunkCheck = this.trunkBasePos.above(Mth.floor(this.wendigo.getY() - this.trunkBasePos.getY()) + 1);
                if (this.wendigo.isClimbableTrunkBlock(this.wendigo.level().getBlockState(trunkCheck))) {
                    this.wendigo.setClimbingTrunk(true);
                    float faceYaw = this.climbFace.getOpposite().toYRot();
                    this.wendigo.setYRot(faceYaw);
                    this.wendigo.yBodyRot = faceYaw;
                    this.wendigo.setDeltaMovement(
                            -this.climbFace.getStepX() * 0.08D,
                            0.20D,
                            -this.climbFace.getStepZ() * 0.08D
                    );
                } else {
                    this.climbTicks = 110;
                }
            }
        }
    }
}
