package ddraig.net.entropica.entity.mammot;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.entity.frost_core.GlacialFrostCoreEntity;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Locale;

/**
 * Fauna 20 — Frost-Tusk Mammot (Glacial Excavator).
 * Large elephant-esque woolly quadruped native to 2.1 Blue-Glacier Shelf and 2.2 Auroral Skerries.
 * Clears snow layers ahead while walking, kneels onto the ice to rest peacefully when undisturbed,
 * stampede-charges attackers with lowered glacier tusks, and launches targets 4 blocks upward with
 * its tusk sweep. Drops 3–4 Raw Mammot Meat (Cooked when on fire), 2–3 Insulated Glacial Wool,
 * and 3–5 Strong Glacial Essence via its entity loot table.
 */
public class FrostTuskMammotEntity extends PathfinderMob {

    public static final int PHASE_STANDING = 0;
    public static final int PHASE_LAYING_DOWN = 1;
    public static final int PHASE_RESTING = 2;
    public static final int PHASE_STANDING_UP = 3;

    public static final int LAYDOWN_DURATION_TICKS = 36;   // 1.80s
    public static final int STAND_UP_DURATION_TICKS = 36;  // 1.80s
    public static final int ATTACK_DURATION_TICKS = 25;    // 1.25s
    public static final int ATTACK_STRIKE_TICK = 11;       // ~0.55s upward tusk launch peak
    public static final int DEATH_DURATION_TICKS = 36;     // 1.80s

    private static final EntityDataAccessor<Boolean> SHEARED =
            SynchedEntityData.defineId(FrostTuskMammotEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CHARGING =
            SynchedEntityData.defineId(FrostTuskMammotEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(FrostTuskMammotEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> REST_PHASE =
            SynchedEntityData.defineId(FrostTuskMammotEntity.class, EntityDataSerializers.INT);

    private static final byte EVENT_TUSK_SWEEP = 4;
    private static final int SHEAR_COOLDOWN_TICKS = 24000;

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState chargeAnimationState = new AnimationState();
    public final AnimationState laydownAnimationState = new AnimationState();
    public final AnimationState layAnimationState = new AnimationState();
    public final AnimationState standUpAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    private int shearCooldown = 0;
    private int restPhaseTimer = 0;
    private int restCooldown = 500;

    public FrostTuskMammotEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 135.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.21D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.65D)
                .add(Attributes.FOLLOW_RANGE, 22.0D)
                .add(Attributes.STEP_HEIGHT, 1.1D);
    }

    public static boolean checkBlueGlacierSpawnRules(
            EntityType<? extends PathfinderMob> type,
            ServerLevelAccessor level,
            EntitySpawnReason spawnReason,
            BlockPos pos,
            RandomSource random
    ) {
        if (EntitySpawnReason.isSpawner(spawnReason) || spawnReason == EntitySpawnReason.SPAWN_ITEM_USE || spawnReason == EntitySpawnReason.COMMAND) {
            return true;
        }
        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        if (below.is(Blocks.SNOW) && level.getBlockState(belowPos.below()).blocksMotion()) {
            belowPos = belowPos.below();
            below = level.getBlockState(belowPos);
        }
        return GlacialFrostCoreEntity.isGlacialSpawnSurface(level, belowPos, below);
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        BlockPos belowPos = this.blockPosition().below();
        BlockState belowState = level.getBlockState(belowPos);
        if (GlacialFrostCoreEntity.isGlacialSpawnSurface(level, belowPos, belowState)) {
            return true;
        }
        return super.checkSpawnRules(level, spawnReason);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        BlockState below = level.getBlockState(pos.below());
        if (below.is(Blocks.BLUE_ICE) || below.is(Blocks.PACKED_ICE) || below.is(Blocks.SNOW_BLOCK) || below.is(Blocks.ICE)) {
            return 10.0F;
        }
        return 0.0F;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SHEARED, false);
        builder.define(CHARGING, false);
        builder.define(ATTACKING, false);
        builder.define(REST_PHASE, PHASE_STANDING);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MammotTuskSweepGoal(this));
        this.goalSelector.addGoal(2, new MammotRestOnIceGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 7.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public boolean isSheared() {
        return this.entityData.get(SHEARED);
    }

    public void setSheared(boolean sheared) {
        this.entityData.set(SHEARED, sheared);
    }

    public boolean isCharging() {
        return this.entityData.get(CHARGING);
    }

    public void setCharging(boolean charging) {
        this.entityData.set(CHARGING, charging);
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }

    public int getRestPhase() {
        return this.entityData.get(REST_PHASE);
    }

    public void setRestPhase(int phase) {
        this.entityData.set(REST_PHASE, phase);
    }

    public boolean isRestingOrTransitioning() {
        return this.getRestPhase() != PHASE_STANDING;
    }

    public void triggerStandUp() {
        int phase = this.getRestPhase();
        if (phase == PHASE_LAYING_DOWN || phase == PHASE_RESTING) {
            this.setRestPhase(PHASE_STANDING_UP);
            this.restPhaseTimer = STAND_UP_DURATION_TICKS;
            this.restCooldown = 600 + this.random.nextInt(600);
        }
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SNIFFER_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        float vol = this.isCharging() ? 0.55F : 0.35F;
        float pitch = this.isCharging() ? 0.78F : 0.85F;
        this.playSound(SoundEvents.RAVAGER_STEP, vol, pitch);
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(serverLevel, source, amount);
        if (hurt && this.isAlive()) {
            this.triggerStandUp();
        }
        return hurt;
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        this.setCharging(false);
        this.setAttacking(false);

        Vec3 vel = this.getDeltaMovement();
        this.setDeltaMovement(vel.x * 0.75D, vel.y, vel.z * 0.75D);

        if (!this.level().isClientSide() && this.deathTime == 22 && this.level() instanceof ServerLevel serverLevel) {
            // Heavy snow puff when the massive woolly flank slams onto the ice
            serverLevel.sendParticles(
                    ParticleTypes.SNOWFLAKE,
                    this.getX(),
                    this.getY() + 0.35D,
                    this.getZ(),
                    22,
                    0.9D,
                    0.2D,
                    0.9D,
                    0.04D
            );
            serverLevel.playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.RAVAGER_STEP,
                    SoundSource.NEUTRAL,
                    0.9F,
                    0.65F
            );
        }

        if (this.deathTime >= DEATH_DURATION_TICKS && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_TUSK_SWEEP) {
            this.attackAnimationState.start(this.tickCount);
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            this.updateClientAnimations();
        } else if (this.level() instanceof ServerLevel serverLevel) {
            if (this.isSheared()) {
                this.shearCooldown--;
                if (this.shearCooldown <= 0) {
                    this.setSheared(false);
                }
            }

            if (this.restCooldown > 0 && this.getRestPhase() == PHASE_STANDING) {
                this.restCooldown--;
            }

            // Advance STANDING_UP completion even if woken by combat
            if (this.getRestPhase() == PHASE_STANDING_UP) {
                this.getNavigation().stop();
                this.restPhaseTimer--;
                if (this.restPhaseTimer <= 0) {
                    this.setRestPhase(PHASE_STANDING);
                }
            } else if (this.getTarget() != null && this.getTarget().isAlive() && this.isRestingOrTransitioning()) {
                this.triggerStandUp();
            }

            // Clear snow layers directly ahead of feet while walking or charging when mobGriefing is enabled
            if (this.onGround()
                    && !this.isRestingOrTransitioning()
                    && this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D
                    && serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                clearSnowLayersAhead(serverLevel);
            }

            // Kick up frost spray underfoot while stampede-charging
            if (this.isCharging() && this.onGround() && this.tickCount % 3 == 0) {
                serverLevel.sendParticles(
                        ParticleTypes.SNOWFLAKE,
                        this.getX(),
                        this.getY() + 0.15D,
                        this.getZ(),
                        5,
                        0.65D,
                        0.12D,
                        0.65D,
                        0.04D
                );
            }
        }
    }

    private void updateClientAnimations() {
        if (this.isDeadOrDying()) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.chargeAnimationState.stop();
            this.laydownAnimationState.stop();
            this.layAnimationState.stop();
            this.standUpAnimationState.stop();
            this.attackAnimationState.stop();
            this.deathAnimationState.startIfStopped(this.tickCount);
            return;
        }

        if (this.attackAnimationState.isStarted()
                && this.attackAnimationState.getTimeInMillis(this.tickCount) >= ATTACK_DURATION_TICKS * 50L) {
            this.attackAnimationState.stop();
        }

        int phase = this.getRestPhase();
        if (phase == PHASE_LAYING_DOWN) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.chargeAnimationState.stop();
            this.layAnimationState.stop();
            this.standUpAnimationState.stop();
            this.laydownAnimationState.startIfStopped(this.tickCount);
            return;
        }
        if (phase == PHASE_RESTING) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.chargeAnimationState.stop();
            this.laydownAnimationState.stop();
            this.standUpAnimationState.stop();
            this.layAnimationState.startIfStopped(this.tickCount);
            return;
        }
        if (phase == PHASE_STANDING_UP) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.chargeAnimationState.stop();
            this.laydownAnimationState.stop();
            this.layAnimationState.stop();
            this.standUpAnimationState.startIfStopped(this.tickCount);
            return;
        }

        this.laydownAnimationState.stop();
        this.layAnimationState.stop();
        this.standUpAnimationState.stop();

        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D || this.walkAnimation.speed() > 0.05F;
        if (moving) {
            this.idleAnimationState.stop();
            if (this.isCharging()) {
                this.walkAnimationState.stop();
                this.chargeAnimationState.startIfStopped(this.tickCount);
            } else {
                this.chargeAnimationState.stop();
                this.walkAnimationState.startIfStopped(this.tickCount);
            }
        } else {
            this.walkAnimationState.stop();
            this.chargeAnimationState.stop();
            this.idleAnimationState.startIfStopped(this.tickCount);
        }
    }

    private void clearSnowLayersAhead(ServerLevel serverLevel) {
        Vec3 forward = Vec3.directionFromRotation(0.0F, this.getYRot());
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);

        double baseX = this.getX() + forward.x * 1.45D;
        double baseY = this.getY();
        double baseZ = this.getZ() + forward.z * 1.45D;

        BlockPos[] probePositions = new BlockPos[]{
                this.blockPosition(),
                BlockPos.containing(baseX, baseY, baseZ),
                BlockPos.containing(baseX + right.x * 0.65D, baseY, baseZ + right.z * 0.65D),
                BlockPos.containing(baseX - right.x * 0.65D, baseY, baseZ - right.z * 0.65D)
        };

        for (BlockPos snowPos : probePositions) {
            BlockState state = serverLevel.getBlockState(snowPos);
            if (state.is(Blocks.SNOW) || state.getBlock() instanceof SnowLayerBlock) {
                serverLevel.removeBlock(snowPos, false);
                serverLevel.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, state),
                        snowPos.getX() + 0.5D,
                        snowPos.getY() + 0.2D,
                        snowPos.getZ() + 0.5D,
                        10,
                        0.35D,
                        0.15D,
                        0.35D,
                        0.08D
                );
                serverLevel.sendParticles(
                        ParticleTypes.SNOWFLAKE,
                        snowPos.getX() + 0.5D,
                        snowPos.getY() + 0.25D,
                        snowPos.getZ() + 0.5D,
                        5,
                        0.3D,
                        0.15D,
                        0.3D,
                        0.02D
                );
                serverLevel.playSound(
                        null,
                        snowPos,
                        SoundEvents.SNOW_BREAK,
                        SoundSource.NEUTRAL,
                        0.6F,
                        0.95F
                );
            }
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        String itemId = BuiltInRegistries.ITEM.getKey(held.getItem()).getPath().toLowerCase(Locale.ROOT);
        if (held.is(Items.SHEARS) || itemId.contains("shears")) {
            if (!this.isSheared()) {
                if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                    this.setSheared(true);
                    this.shearCooldown = SHEAR_COOLDOWN_TICKS;
                    this.triggerStandUp();
                    held.hurtAndBreak(1, player, getEquipmentSlotForItem(held));

                    int woolCount = 1 + this.random.nextInt(2);
                    this.spawnAtLocation(serverLevel, new ItemStack(ModItems.INSULATED_GLACIAL_WOOL.get(), woolCount), 1.0F);
                    serverLevel.playSound(
                            null,
                            this.getX(),
                            this.getY(),
                            this.getZ(),
                            SoundEvents.SHEEP_SHEAR,
                            SoundSource.PLAYERS,
                            1.0F,
                            0.9F
                    );
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setSheared(input.read("Sheared", Codec.BOOL).orElse(false));
        this.shearCooldown = input.read("ShearCooldown", Codec.INT).orElse(0);
        this.setRestPhase(input.read("RestPhase", Codec.INT).orElse(PHASE_STANDING));
        this.restPhaseTimer = input.read("RestPhaseTimer", Codec.INT).orElse(0);
        this.restCooldown = input.read("RestCooldown", Codec.INT).orElse(400);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("Sheared", Codec.BOOL, this.isSheared());
        output.store("ShearCooldown", Codec.INT, this.shearCooldown);
        output.store("RestPhase", Codec.INT, this.getRestPhase());
        output.store("RestPhaseTimer", Codec.INT, this.restPhaseTimer);
        output.store("RestCooldown", Codec.INT, this.restCooldown);
    }

    /**
     * Peaceful glacial resting behavior: kneels onto front knees first (laydown),
     * rests peacefully on the snow (lay), and rises back onto its feet (stand_up).
     */
    static class MammotRestOnIceGoal extends Goal {
        private final FrostTuskMammotEntity mammot;

        MammotRestOnIceGoal(FrostTuskMammotEntity mammot) {
            this.mammot = mammot;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.mammot.getTarget() != null || this.mammot.isInWater() || !this.mammot.onGround()) {
                return false;
            }
            int phase = this.mammot.getRestPhase();
            if (phase == PHASE_LAYING_DOWN || phase == PHASE_RESTING || phase == PHASE_STANDING_UP) {
                return true;
            }
            if (this.mammot.restCooldown > 0) {
                return false;
            }
            return this.mammot.getRandom().nextInt(90) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.mammot.isInWater()) {
                this.mammot.setRestPhase(PHASE_STANDING);
                return false;
            }
            return this.mammot.getRestPhase() != PHASE_STANDING;
        }

        @Override
        public void start() {
            this.mammot.getNavigation().stop();
            if (this.mammot.getRestPhase() == PHASE_STANDING) {
                this.mammot.setRestPhase(PHASE_LAYING_DOWN);
                this.mammot.restPhaseTimer = LAYDOWN_DURATION_TICKS;
            }
        }

        @Override
        public void tick() {
            this.mammot.getNavigation().stop();
            int phase = this.mammot.getRestPhase();

            if (this.mammot.getTarget() != null && this.mammot.getTarget().isAlive()) {
                this.mammot.triggerStandUp();
                return;
            }

            if (phase == PHASE_LAYING_DOWN) {
                this.mammot.restPhaseTimer--;
                if (this.mammot.restPhaseTimer <= 0) {
                    this.mammot.setRestPhase(PHASE_RESTING);
                    this.mammot.restPhaseTimer = 240 + this.mammot.getRandom().nextInt(280);
                }
            } else if (phase == PHASE_RESTING) {
                this.mammot.restPhaseTimer--;
                if (this.mammot.restPhaseTimer <= 0) {
                    this.mammot.setRestPhase(PHASE_STANDING_UP);
                    this.mammot.restPhaseTimer = STAND_UP_DURATION_TICKS;
                }
            }
        }

        @Override
        public void stop() {
            if (this.mammot.getRestPhase() == PHASE_STANDING) {
                this.mammot.restCooldown = 700 + this.mammot.getRandom().nextInt(700);
            }
        }
    }

    /**
     * Bull-mammot stampede charge & timed upward-launching glacier-tusk sweep.
     */
    static class MammotTuskSweepGoal extends Goal {
        private final FrostTuskMammotEntity mammot;
        private int attackCooldown = 0;
        private int sweepTick = -1;

        MammotTuskSweepGoal(FrostTuskMammotEntity mammot) {
            this.mammot = mammot;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.attackCooldown > 0) {
                this.attackCooldown--;
            }
            LivingEntity target = this.mammot.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            if (this.sweepTick >= 0) {
                return this.mammot.isAlive();
            }
            LivingEntity target = this.mammot.getTarget();
            return target != null && target.isAlive() && !target.isSpectator();
        }

        @Override
        public void start() {
            if (this.mammot.isRestingOrTransitioning()) {
                this.mammot.triggerStandUp();
            }
        }

        @Override
        public void stop() {
            this.sweepTick = -1;
            this.mammot.setCharging(false);
            this.mammot.setAttacking(false);
            this.mammot.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.attackCooldown > 0) {
                this.attackCooldown--;
            }

            // Wait cleanly for stand_up animation before charging
            if (this.mammot.isRestingOrTransitioning()) {
                this.mammot.setCharging(false);
                this.mammot.getNavigation().stop();
                return;
            }

            LivingEntity target = this.mammot.getTarget();

            // Progress active tusk-sweep animation & execute upward toss on launch frame
            if (this.sweepTick >= 0) {
                this.sweepTick++;
                this.mammot.setCharging(false);
                this.mammot.getNavigation().stop();
                if (target != null) {
                    this.mammot.getLookControl().setLookAt(target, 30.0F, 30.0F);
                }

                if (this.sweepTick == ATTACK_STRIKE_TICK && target != null && target.isAlive()
                        && this.mammot.level() instanceof ServerLevel serverLevel) {
                    double strikeReachSqr = 10.24D; // ~3.2 blocks from center of 1.95-wide Mammot
                    if (this.mammot.distanceToSqr(target) <= strikeReachSqr || this.mammot.isWithinMeleeAttackRange(target)) {
                        float damage = (float) this.mammot.getAttributeValue(Attributes.ATTACK_DAMAGE);
                        boolean hit = target.hurtServer(serverLevel, this.mammot.damageSources().mobAttack(this.mammot), damage);
                        if (hit) {
                            Vec3 pushDir = target.position().subtract(this.mammot.position());
                            Vec3 horiz = new Vec3(pushDir.x, 0.0D, pushDir.z);
                            if (horiz.lengthSqr() > 1.0E-4D) {
                                horiz = horiz.normalize().scale(0.38D);
                            }
                            Vec3 cur = target.getDeltaMovement();
                            // Launch target ~4 blocks upward (deltaMovement.y = 0.78D)
                            target.setDeltaMovement(cur.x * 0.2D + horiz.x, 0.78D, cur.z * 0.2D + horiz.z);
                            target.hurtMarked = true;
                            target.hasImpulse = true;

                            serverLevel.playSound(
                                    null,
                                    this.mammot.getX(),
                                    this.mammot.getY(),
                                    this.mammot.getZ(),
                                    SoundEvents.RAVAGER_ATTACK,
                                    SoundSource.HOSTILE,
                                    1.1F,
                                    0.9F
                            );
                            serverLevel.sendParticles(
                                    ParticleTypes.SNOWFLAKE,
                                    target.getX(),
                                    target.getY() + 0.5D,
                                    target.getZ(),
                                    18,
                                    0.5D,
                                    0.3D,
                                    0.5D,
                                    0.08D
                            );
                        }
                    }
                }

                if (this.sweepTick >= ATTACK_DURATION_TICKS) {
                    this.sweepTick = -1;
                    this.mammot.setAttacking(false);
                }
                return;
            }

            if (target == null) {
                this.mammot.setCharging(false);
                return;
            }

            this.mammot.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double distSqr = this.mammot.distanceToSqr(target);
            double triggerReachSqr = 8.41D; // ~2.9 blocks from center

            if (distSqr > triggerReachSqr) {
                this.mammot.setCharging(true);
                this.mammot.getNavigation().moveTo(target, 1.48D);
            } else {
                this.mammot.setCharging(false);
                this.mammot.getNavigation().moveTo(target, 1.15D);
            }

            if (this.attackCooldown <= 0 && (distSqr <= triggerReachSqr || this.mammot.isWithinMeleeAttackRange(target))) {
                this.attackCooldown = 36;
                this.sweepTick = 0;
                this.mammot.setCharging(false);
                this.mammot.setAttacking(true);
                if (this.mammot.level() instanceof ServerLevel serverLevel) {
                    serverLevel.broadcastEntityEvent(this.mammot, EVENT_TUSK_SWEEP);
                }
            }
        }
    }
}
