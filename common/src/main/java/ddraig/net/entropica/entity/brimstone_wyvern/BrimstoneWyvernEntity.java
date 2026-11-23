package ddraig.net.entropica.entity.brimstone_wyvern;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
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
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

/**
 * Fauna 54 — Brimstone Wyvern (Lava Drake) (entropica:brimstone_wyvern).
 * Hostile bipedal volcanic wyvern native to 6.1 Magma Caverns & 6.4 Ash Canyons:
 * - Swims rapidly in subterranean lava rivers & lakes (swim), stalks on digitigrade talons (walk / idle),
 *   and launches (takeoff) to soar through cavern vaults (fly -> landing).
 * - Magma Jaw Bite (attack grounded / flying_attack airborne): bites for 8.0 damage (+ recharged magma sear).
 * - Explosive Brimstone Fireball Spit (fireball_spit grounded / flying_fireball_spit airborne): hurls a
 *   sulfur-yellow brimstone fireball that explodes on contact.
 * - Immune to fire, lava, and fall damage, recharging and amplifying its fire damage when bathing in lava
 *   or standing on magma blocks.
 * - Drops fireproof Wyvern Scale (entropica:wyvern_scale).
 */
public class BrimstoneWyvernEntity extends Monster implements FlyingAnimal {

    public static final int MODE_GROUNDED = 0;
    public static final int MODE_TAKING_OFF = 1;
    public static final int MODE_FLYING = 2;
    public static final int MODE_LANDING = 3;

    private static final EntityDataAccessor<Integer> FLIGHT_MODE =
            SynchedEntityData.defineId(BrimstoneWyvernEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> MAGMA_CHARGED =
            SynchedEntityData.defineId(BrimstoneWyvernEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> LAVA_SWIMMING =
            SynchedEntityData.defineId(BrimstoneWyvernEntity.class, EntityDataSerializers.BOOLEAN);

    private static final byte EVENT_GROUND_BITE = 4;
    private static final byte EVENT_FLYING_BITE = 61;
    private static final byte EVENT_GROUND_FIREBALL = 62;
    private static final byte EVENT_FLYING_FIREBALL = 63;
    private static final byte EVENT_TAKEOFF = 64;
    private static final byte EVENT_LANDING = 65;
    private static final byte EVENT_GROUND_HURT = 66;
    private static final byte EVENT_FLYING_HURT = 67;

    private static final int TAKEOFF_DURATION_TICKS = 30;        // 1.50s takeoff
    private static final int LANDING_DURATION_TICKS = 30;        // 1.50s landing
    private static final int BITE_DURATION_TICKS = 26;           // 1.30s attack / flying_attack
    private static final int FIREBALL_SPIT_DURATION_TICKS = 32;  // 1.60s fireball_spit / flying_fireball_spit
    private static final int GROUND_HURT_DURATION_TICKS = 12;    // 0.60s hurt
    private static final int FLYING_HURT_DURATION_TICKS = 13;    // 0.65s flying_hurt
    private static final int DEATH_DURATION_TICKS = 36;          // 1.80s death hold
    private static final int MAGMA_FULL_CHARGE_TICKS = 320;      // 16.0s of recharged fire damage
    private static final int SCALE_SHED_COOLDOWN_TICKS = 1200;

    private static final ResourceLocation WYVERN_SCALE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "wyvern_scale");

    private static final DustParticleOptions BRIMSTONE_SULFUR_DUST = new DustParticleOptions(0xF8EE2A, 0.78F);
    private static final DustParticleOptions MOLTEN_PALATE_DUST = new DustParticleOptions(0xF97316, 0.72F);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState swimAnimationState = new AnimationState();
    public final AnimationState flyAnimationState = new AnimationState();
    public final AnimationState takeoffAnimationState = new AnimationState();
    public final AnimationState landingAnimationState = new AnimationState();
    public final AnimationState flyingAttackAnimationState = new AnimationState();
    public final AnimationState flyingFireballSpitAnimationState = new AnimationState();
    public final AnimationState flyingHurtAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState fireballSpitAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    private final PathNavigation groundNav;
    private final PathNavigation flyingNav;
    private final MoveControl groundMoveControl;
    private final MoveControl flyingMoveControl;

    private int modeTransitionTicks = 0;
    private int phaseTimer = 140;
    private int biteTicks = 0;
    private boolean biteAerial = false;
    private int fireballSpitTicks = 0;
    private boolean fireballAerial = false;
    private int combatCooldown = 0;
    private int fireballCooldown = 45;
    private int magmaChargeTicks = MAGMA_FULL_CHARGE_TICKS;
    private int scaleShedCooldown = 0;

    // Client-side countdowns so absolute-pose aerial one-shots yield cleanly back to fly at t=0
    private int clientTakeoffTicks = 0;
    private int clientLandingTicks = 0;
    private int clientFlyingBiteTicks = 0;
    private int clientFlyingFireballTicks = 0;
    private int clientFlyingHurtTicks = 0;

    private final List<TrackedBrimstoneFireball> activeFireballs = new ArrayList<>();

    private static final class TrackedBrimstoneFireball {
        final SmallFireball projectile;
        final boolean supercharged;
        Vec3 lastPos;
        int age;

        TrackedBrimstoneFireball(SmallFireball projectile, boolean supercharged) {
            this.projectile = projectile;
            this.supercharged = supercharged;
            this.lastPos = projectile.position();
            this.age = 0;
        }
    }

    public BrimstoneWyvernEntity(EntityType<? extends BrimstoneWyvernEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 18;
        this.groundMoveControl = new MoveControl(this);
        this.flyingMoveControl = new FlyingMoveControl(this, 20, true);
        this.groundNav = new GroundPathNavigation(this, level);
        FlyingPathNavigation fNav = new FlyingPathNavigation(this, level);
        fNav.setCanOpenDoors(false);
        fNav.setCanFloat(true);
        this.flyingNav = fNav;

        this.moveControl = this.groundMoveControl;
        this.navigation = this.groundNav;
        this.setPathfindingMalus(PathType.LAVA, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
        this.setPathfindingMalus(PathType.WATER, 8.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 135.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.50D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.FLYING_SPEED, 0.62D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLIGHT_MODE, MODE_GROUNDED);
        builder.define(MAGMA_CHARGED, true);
        builder.define(LAVA_SWIMMING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new WyvernCombatGoal(this));
        this.goalSelector.addGoal(2, new WyvernMagmaBathGoal(this));
        this.goalSelector.addGoal(3, new WyvernCavernSoarGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.90D) {
            @Override
            public boolean canUse() {
                return BrimstoneWyvernEntity.this.getFlightMode() == MODE_GROUNDED
                        && !BrimstoneWyvernEntity.this.isInLava()
                        && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return BrimstoneWyvernEntity.this.getFlightMode() == MODE_GROUNDED
                        && !BrimstoneWyvernEntity.this.isInLava()
                        && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    public int getFlightMode() {
        return this.entityData.get(FLIGHT_MODE);
    }

    private void setFlightMode(int mode) {
        this.entityData.set(FLIGHT_MODE, mode);
        this.syncNavigationForMode(mode);
    }

    private void syncNavigationForMode(int mode) {
        boolean airborne = (mode != MODE_GROUNDED);
        this.setNoGravity(airborne);
        if (airborne) {
            if (this.navigation != this.flyingNav) {
                this.groundNav.stop();
                this.navigation = this.flyingNav;
                this.moveControl = this.flyingMoveControl;
            }
        } else {
            if (this.navigation != this.groundNav) {
                this.flyingNav.stop();
                this.navigation = this.groundNav;
                this.moveControl = this.groundMoveControl;
            }
        }
    }

    public boolean isMagmaCharged() {
        return this.entityData.get(MAGMA_CHARGED);
    }

    private void setMagmaCharged(boolean charged) {
        this.entityData.set(MAGMA_CHARGED, charged);
    }

    public boolean isLavaSwimming() {
        return this.entityData.get(LAVA_SWIMMING);
    }

    private void setLavaSwimming(boolean swimming) {
        this.entityData.set(LAVA_SWIMMING, swimming);
    }

    @Override
    public boolean isFlying() {
        return this.getFlightMode() != MODE_GROUNDED;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        // Immune to fall damage
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        if (source.is(DamageTypeTags.IS_FIRE)
                || source.is(DamageTypeTags.IS_FALL)
                || source.is(DamageTypes.LAVA)
                || source.is(DamageTypes.HOT_FLOOR)
                || source.is(DamageTypes.IN_FIRE)
                || source.is(DamageTypes.ON_FIRE)) {
            this.rechargeFireInMagma(level, false);
            return true;
        }
        return super.isInvulnerableTo(level, source);
    }

    public boolean isStandingOnOrInMagma() {
        if (this.isInLava() || this.isOnFire()) {
            return true;
        }
        BlockState below = this.level().getBlockState(this.blockPosition().below());
        BlockState feet = this.level().getBlockState(this.blockPosition());
        return isMagmaRechargeBlock(below) || isMagmaRechargeBlock(feet);
    }

    public static boolean isMagmaRechargeBlock(BlockState state) {
        if (state.isAir()) {
            return false;
        }
        if (state.is(Blocks.LAVA) || state.is(Blocks.MAGMA_BLOCK) || state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)) {
            return true;
        }
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return path.contains("magma") || path.contains("brimstone") || path.contains("lava");
    }

    public void rechargeFireInMagma(ServerLevel serverLevel, boolean playBurst) {
        boolean wasDry = !this.isMagmaCharged();
        this.magmaChargeTicks = MAGMA_FULL_CHARGE_TICKS;
        this.setMagmaCharged(true);
        if ((wasDry || playBurst) && serverLevel != null) {
            serverLevel.playSound(null, this.getX(), this.getY() + 0.9D, this.getZ(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 0.70F, 0.85F);
            serverLevel.sendParticles(BRIMSTONE_SULFUR_DUST,
                    this.getX(), this.getY() + 1.0D, this.getZ(),
                    14, 0.55D, 0.40D, 0.55D, 0.03D);
            serverLevel.sendParticles(ParticleTypes.LAVA,
                    this.getX(), this.getY() + 0.8D, this.getZ(),
                    6, 0.45D, 0.30D, 0.45D, 0.02D);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            this.updateClientAnimations();
            this.spawnClientBrimstoneParticles();
            return;
        }

        if (!this.isAlive()) {
            this.clearTrackedFireballs();
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();

        if (this.combatCooldown > 0) {
            this.combatCooldown--;
        }
        if (this.fireballCooldown > 0) {
            this.fireballCooldown--;
        }
        if (this.scaleShedCooldown > 0) {
            this.scaleShedCooldown--;
        }

        // Lava swimming propulsion & Magma fire-damage recharge
        boolean inLavaNow = this.isInLava();
        if (inLavaNow) {
            if (this.getFlightMode() == MODE_LANDING) {
                this.setFlightMode(MODE_GROUNDED);
                this.modeTransitionTicks = 0;
            }
            if (this.getFlightMode() == MODE_GROUNDED) {
                Vec3 vel = this.getDeltaMovement();
                double lift = this.getFluidHeight(net.minecraft.tags.FluidTags.LAVA) > 0.55D ? 0.075D : 0.015D;
                this.setDeltaMovement(vel.x * 1.12D, Math.max(vel.y * 0.85D, lift), vel.z * 1.12D);
            }
        }
        this.setLavaSwimming(inLavaNow && this.getFlightMode() == MODE_GROUNDED);

        if (this.isStandingOnOrInMagma()) {
            this.rechargeFireInMagma(serverLevel, false);
            if (this.tickCount % 40 == 0 && this.getHealth() < this.getMaxHealth()) {
                this.heal(1.0F);
            }
        } else if (this.magmaChargeTicks > 0) {
            this.magmaChargeTicks--;
            if (this.magmaChargeTicks == 0) {
                this.setMagmaCharged(false);
            }
        }

        this.tickFlightLifecycle(serverLevel);
        this.tickBiteAttack(serverLevel);
        this.tickFireballSpit(serverLevel);
        this.tickTrackedFireballs(serverLevel);
    }

    private void tickFlightLifecycle(ServerLevel serverLevel) {
        int mode = this.getFlightMode();
        if (mode == MODE_TAKING_OFF) {
            if (this.modeTransitionTicks > 0) {
                this.modeTransitionTicks--;
                // Explosive downstroke launch at t = 0.42s (remaining == 22)
                if (this.modeTransitionTicks == 22) {
                    Vec3 vel = this.getDeltaMovement();
                    this.setDeltaMovement(vel.x, Math.max(vel.y, 0.48D), vel.z);
                    serverLevel.playSound(null, this.getX(), this.getY() + 0.8D, this.getZ(),
                            SoundEvents.ENDER_DRAGON_FLAP, SoundSource.HOSTILE, 0.90F, 0.92F);
                    serverLevel.sendParticles(ParticleTypes.FLAME,
                            this.getX(), this.getY() + 0.3D, this.getZ(),
                            18, 0.65D, 0.25D, 0.65D, 0.04D);
                    serverLevel.sendParticles(BRIMSTONE_SULFUR_DUST,
                            this.getX(), this.getY() + 0.4D, this.getZ(),
                            12, 0.60D, 0.25D, 0.60D, 0.02D);
                }
            }
            if (this.modeTransitionTicks <= 0) {
                this.setFlightMode(MODE_FLYING);
            }
            return;
        }

        if (mode == MODE_LANDING) {
            Vec3 vel = this.getDeltaMovement();
            this.setDeltaMovement(vel.x * 0.86D, -0.18D, vel.z * 0.86D);
            if (this.modeTransitionTicks > 0) {
                this.modeTransitionTicks--;
            }
            if (this.onGround() || this.isInLava() || this.modeTransitionTicks <= 0) {
                this.setFlightMode(MODE_GROUNDED);
                this.modeTransitionTicks = 0;
            }
            return;
        }

        if (this.phaseTimer > 0) {
            this.phaseTimer--;
        }

        LivingEntity target = this.getTarget();
        if (mode == MODE_GROUNDED) {
            boolean shouldLaunch = false;
            if (target != null && target.isAlive()) {
                double dy = target.getY() - this.getY();
                double distSq = this.distanceToSqr(target);
                if ((dy > 2.8D || distSq > 64.0D) && this.hasHeadroomForTakeoff(serverLevel)) {
                    shouldLaunch = true;
                }
            } else if (this.phaseTimer <= 0 && !this.isInLava() && this.random.nextInt(45) == 0 && this.hasHeadroomForTakeoff(serverLevel)) {
                shouldLaunch = true;
            }
            if (shouldLaunch && this.biteTicks <= 0 && this.fireballSpitTicks <= 0) {
                this.beginTakeoff(serverLevel);
            }
        } else if (mode == MODE_FLYING) {
            if (target == null && this.phaseTimer <= 0 && this.biteTicks <= 0 && this.fireballSpitTicks <= 0) {
                if (this.isNearLandingSurface(serverLevel, 6)) {
                    this.beginLanding(serverLevel);
                } else {
                    this.phaseTimer = 80;
                }
            }
        }
    }

    private boolean hasHeadroomForTakeoff(ServerLevel level) {
        BlockPos pos = this.blockPosition();
        for (int dy = 2; dy <= 5; dy++) {
            if (!level.getBlockState(pos.above(dy)).getCollisionShape(level, pos.above(dy)).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean isNearLandingSurface(ServerLevel level, int maxDrop) {
        BlockPos pos = this.blockPosition();
        for (int dy = 1; dy <= maxDrop; dy++) {
            BlockPos below = pos.below(dy);
            BlockState st = level.getBlockState(below);
            if (st.is(Blocks.LAVA) || st.isFaceSturdy(level, below, Direction.UP)) {
                return true;
            }
        }
        return false;
    }

    public void beginTakeoff(ServerLevel serverLevel) {
        if (this.getFlightMode() != MODE_GROUNDED || !this.isAlive()) {
            return;
        }
        this.setLavaSwimming(false);
        this.setFlightMode(MODE_TAKING_OFF);
        this.modeTransitionTicks = TAKEOFF_DURATION_TICKS;
        this.phaseTimer = 220 + this.random.nextInt(160);
        serverLevel.broadcastEntityEvent(this, EVENT_TAKEOFF);
    }

    public void beginLanding(ServerLevel serverLevel) {
        if (this.getFlightMode() != MODE_FLYING || !this.isAlive()) {
            return;
        }
        this.setFlightMode(MODE_LANDING);
        this.modeTransitionTicks = LANDING_DURATION_TICKS;
        this.phaseTimer = 150 + this.random.nextInt(140);
        serverLevel.broadcastEntityEvent(this, EVENT_LANDING);
    }

    private void updateClientAnimations() {
        if (this.isDeadOrDying()) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.swimAnimationState.stop();
            this.flyAnimationState.stop();
            this.takeoffAnimationState.stop();
            this.landingAnimationState.stop();
            this.flyingAttackAnimationState.stop();
            this.flyingFireballSpitAnimationState.stop();
            this.flyingHurtAnimationState.stop();
            this.attackAnimationState.stop();
            this.fireballSpitAnimationState.stop();
            this.hurtAnimationState.stop();
            if (!this.deathAnimationState.isStarted()) {
                this.deathAnimationState.start(this.tickCount);
            }
            return;
        }

        if (this.clientTakeoffTicks > 0) {
            this.clientTakeoffTicks--;
            if (this.clientTakeoffTicks == 0) {
                this.takeoffAnimationState.stop();
                this.flyAnimationState.start(this.tickCount);
            }
        }
        if (this.clientLandingTicks > 0) {
            this.clientLandingTicks--;
            if (this.clientLandingTicks == 0) {
                this.landingAnimationState.stop();
            }
        }
        if (this.clientFlyingBiteTicks > 0) {
            this.clientFlyingBiteTicks--;
            if (this.clientFlyingBiteTicks == 0) {
                this.flyingAttackAnimationState.stop();
                this.flyAnimationState.start(this.tickCount);
            }
        }
        if (this.clientFlyingFireballTicks > 0) {
            this.clientFlyingFireballTicks--;
            if (this.clientFlyingFireballTicks == 0) {
                this.flyingFireballSpitAnimationState.stop();
                this.flyAnimationState.start(this.tickCount);
            }
        }
        if (this.clientFlyingHurtTicks > 0) {
            this.clientFlyingHurtTicks--;
            if (this.clientFlyingHurtTicks == 0) {
                this.flyingHurtAnimationState.stop();
                this.flyAnimationState.start(this.tickCount);
            }
        }

        int mode = this.getFlightMode();
        if (mode == MODE_TAKING_OFF || this.clientTakeoffTicks > 0) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.swimAnimationState.stop();
            this.flyAnimationState.stop();
            return;
        }
        if (mode == MODE_LANDING || this.clientLandingTicks > 0) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.swimAnimationState.stop();
            this.flyAnimationState.stop();
            return;
        }
        if (mode == MODE_FLYING) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.swimAnimationState.stop();
            this.flyAnimationState.startIfStopped(this.tickCount);
            return;
        }

        this.flyAnimationState.stop();
        this.flyingAttackAnimationState.stop();
        this.flyingFireballSpitAnimationState.stop();
        this.flyingHurtAnimationState.stop();

        if (this.isLavaSwimming() || this.isInLava()) {
            this.swimAnimationState.startIfStopped(this.tickCount);
            this.walkAnimationState.stop();
            this.idleAnimationState.stop();
            return;
        }

        this.swimAnimationState.stop();
        double hSpeedSqr = this.getDeltaMovement().horizontalDistanceSqr();
        if (hSpeedSqr > 4.0E-4D || this.walkAnimation.speed() > 0.08F) {
            this.walkAnimationState.startIfStopped(this.tickCount);
            this.idleAnimationState.stop();
        } else {
            this.idleAnimationState.startIfStopped(this.tickCount);
            this.walkAnimationState.stop();
        }
    }

    private void spawnClientBrimstoneParticles() {
        if (this.isDeadOrDying()) {
            return;
        }
        int chance = this.isMagmaCharged() ? 3 : 8;
        if (this.random.nextInt(chance) == 0) {
            this.level().addParticle(BRIMSTONE_SULFUR_DUST,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.95D,
                    this.getY() + 1.15D + (this.random.nextDouble() - 0.5D) * 0.45D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.95D,
                    0.0D, 0.015D, 0.0D);
        }
        if (this.isMagmaCharged() && this.random.nextInt(5) == 0) {
            this.level().addParticle(ParticleTypes.SMALL_FLAME,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.70D,
                    this.getY() + 1.25D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.70D,
                    0.0D, 0.01D, 0.0D);
        }
    }

    /**
     * Magma Jaw Bite (1.30s = 26 ticks; jaw snap at t = 0.65s -> remaining == 13):
     * Bites for 8.0 damage (+ bonus molten fire sear when recharged in magma!).
     */
    public void startBiteAttack(ServerLevel serverLevel, LivingEntity target) {
        if (this.biteTicks > 0 || this.fireballSpitTicks > 0 || this.modeTransitionTicks > 0) {
            return;
        }
        this.biteAerial = (this.getFlightMode() == MODE_FLYING);
        this.biteTicks = BITE_DURATION_TICKS;
        this.combatCooldown = 30;
        if (!this.biteAerial && !this.isInLava()) {
            this.getNavigation().stop();
        } else if (this.biteAerial && target != null) {
            Vec3 diff = target.position().add(0.0D, 0.55D, 0.0D).subtract(this.position());
            if (diff.lengthSqr() > 1.0E-4D) {
                Vec3 swoop = diff.normalize().scale(0.58D);
                this.setDeltaMovement(swoop.x, Mth.clamp(swoop.y, -0.45D, 0.25D), swoop.z);
            }
        }
        serverLevel.broadcastEntityEvent(this, this.biteAerial ? EVENT_FLYING_BITE : EVENT_GROUND_BITE);
        serverLevel.playSound(null, this.getX(), this.getY() + 1.1D, this.getZ(),
                SoundEvents.PHANTOM_BITE, SoundSource.HOSTILE, 1.0F, 0.62F);
    }

    private void tickBiteAttack(ServerLevel serverLevel) {
        if (this.biteTicks <= 0) {
            return;
        }
        this.biteTicks--;

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            this.getLookControl().setLookAt(target, 32.0F, 32.0F);
        }

        if (this.biteTicks == 13 && target != null && target.isAlive()) {
            double reachSq = this.biteAerial ? 18.5D : 15.5D;
            if (this.distanceToSqr(target) <= reachSq) {
                float baseBite = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE); // 8.0F
                boolean charged = this.isMagmaCharged();
                if (target.hurtServer(serverLevel, this.damageSources().mobAttack(this), baseBite)) {
                    target.igniteForSeconds(charged ? 6.0F : 3.0F);
                    if (charged) {
                        target.invulnerableTime = 0;
                        target.hurtServer(serverLevel, this.damageSources().onFire(), 3.0F);
                        this.magmaChargeTicks = Math.max(0, this.magmaChargeTicks - 70);
                        if (this.magmaChargeTicks == 0) {
                            this.setMagmaCharged(false);
                        }
                    }
                    serverLevel.playSound(null, target.getX(), target.getY() + 0.8D, target.getZ(),
                            SoundEvents.GENERIC_BURN, SoundSource.HOSTILE, 0.75F, 0.90F);
                    serverLevel.sendParticles(MOLTEN_PALATE_DUST,
                            target.getX(), target.getY() + 0.9D, target.getZ(),
                            12, 0.30D, 0.30D, 0.30D, 0.03D);
                    serverLevel.sendParticles(BRIMSTONE_SULFUR_DUST,
                            target.getX(), target.getY() + 0.9D, target.getZ(),
                            8, 0.25D, 0.25D, 0.25D, 0.02D);
                }
            }
        }
    }

    /**
     * Explosive Brimstone Fireball Spit (1.60s = 32 ticks; hurl at t = 0.68s -> remaining == 18):
     * Spits a sulfur-yellow brimstone fireball that explodes on contact!
     */
    public void startFireballSpit(ServerLevel serverLevel) {
        if (this.fireballSpitTicks > 0 || this.biteTicks > 0 || this.modeTransitionTicks > 0) {
            return;
        }
        this.fireballAerial = (this.getFlightMode() == MODE_FLYING);
        this.fireballSpitTicks = FIREBALL_SPIT_DURATION_TICKS;
        int baseCd = this.isMagmaCharged() ? 52 : 76;
        this.fireballCooldown = baseCd;
        this.combatCooldown = 36;
        if (!this.fireballAerial && !this.isInLava()) {
            this.getNavigation().stop();
        }
        serverLevel.broadcastEntityEvent(this, this.fireballAerial ? EVENT_FLYING_FIREBALL : EVENT_GROUND_FIREBALL);
        serverLevel.playSound(null, this.getX(), this.getY() + 1.2D, this.getZ(),
                SoundEvents.BLAZE_AMBIENT, SoundSource.HOSTILE, 0.95F, 0.72F);
    }

    private void tickFireballSpit(ServerLevel serverLevel) {
        if (this.fireballSpitTicks <= 0) {
            return;
        }
        this.fireballSpitTicks--;

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            this.getLookControl().setLookAt(target, 32.0F, 32.0F);
        }

        // Furnace inhale glow (0.0s -> 0.55s)
        if (this.fireballSpitTicks > 18 && this.fireballSpitTicks % 2 == 0) {
            Vec3 look = this.getLookAngle();
            Vec3 maw = new Vec3(this.getX() + look.x * 0.95D, this.getY() + 1.30D, this.getZ() + look.z * 0.95D);
            serverLevel.sendParticles(BRIMSTONE_SULFUR_DUST, maw.x, maw.y, maw.z, 4, 0.18D, 0.18D, 0.18D, 0.02D);
            serverLevel.sendParticles(ParticleTypes.FLAME, maw.x, maw.y, maw.z, 2, 0.12D, 0.12D, 0.12D, 0.01D);
        }

        // Launch explosive brimstone fireball at t = 0.68s (remaining == 18)
        if (this.fireballSpitTicks == 18) {
            this.launchBrimstoneFireball(serverLevel, target);
        }
    }

    private void launchBrimstoneFireball(ServerLevel serverLevel, @Nullable LivingEntity target) {
        Vec3 look = this.getLookAngle();
        Vec3 muzzle = new Vec3(this.getX() + look.x * 1.15D, this.getY() + 1.28D, this.getZ() + look.z * 1.15D);
        Vec3 aimDir = look;
        if (target != null && target.isAlive()) {
            Vec3 toTarget = target.getBoundingBox().getCenter().subtract(muzzle);
            if (toTarget.lengthSqr() > 1.0E-4D) {
                aimDir = toTarget.normalize();
            }
        }

        boolean supercharged = this.isMagmaCharged();
        if (supercharged) {
            this.magmaChargeTicks = Math.max(0, this.magmaChargeTicks - 85);
            if (this.magmaChargeTicks == 0) {
                this.setMagmaCharged(false);
            }
        }

        SmallFireball fireball = EntityType.SMALL_FIREBALL.create(serverLevel, EntitySpawnReason.TRIGGERED);
        if (fireball != null) {
            fireball.setOwner(this);
            fireball.setPos(muzzle.x, muzzle.y, muzzle.z);
            fireball.shoot(aimDir.x, aimDir.y, aimDir.z, supercharged ? 1.55F : 1.30F, 0.8F);
            serverLevel.addFreshEntity(fireball);
            this.activeFireballs.add(new TrackedBrimstoneFireball(fireball, supercharged));
        }

        serverLevel.playSound(null, muzzle.x, muzzle.y, muzzle.z,
                SoundEvents.GHAST_SHOOT, SoundSource.HOSTILE, 1.0F, 0.88F);
        serverLevel.sendParticles(ParticleTypes.FLAME, muzzle.x, muzzle.y, muzzle.z, 14, 0.22D, 0.22D, 0.22D, 0.05D);
        serverLevel.sendParticles(BRIMSTONE_SULFUR_DUST, muzzle.x, muzzle.y, muzzle.z, 12, 0.25D, 0.25D, 0.25D, 0.03D);
    }

    private void tickTrackedFireballs(ServerLevel serverLevel) {
        if (this.activeFireballs.isEmpty()) {
            return;
        }
        Iterator<TrackedBrimstoneFireball> it = this.activeFireballs.iterator();
        while (it.hasNext()) {
            TrackedBrimstoneFireball tracked = it.next();
            tracked.age++;
            if (tracked.projectile.isAlive() && !tracked.projectile.isRemoved()) {
                tracked.lastPos = tracked.projectile.position();
                serverLevel.sendParticles(BRIMSTONE_SULFUR_DUST,
                        tracked.lastPos.x, tracked.lastPos.y, tracked.lastPos.z,
                        3, 0.12D, 0.12D, 0.12D, 0.01D);
                if (tracked.supercharged && tracked.age % 2 == 0) {
                    serverLevel.sendParticles(ParticleTypes.LAVA,
                            tracked.lastPos.x, tracked.lastPos.y, tracked.lastPos.z,
                            1, 0.08D, 0.08D, 0.08D, 0.01D);
                }
                if (tracked.age > 80) {
                    tracked.projectile.discard();
                    it.remove();
                }
            } else {
                // Explodes on contact (terrain-safe fiery blast + sulfur-yellow shockwave)!
                Vec3 hitPos = tracked.projectile.position();
                if (hitPos.lengthSqr() < 1.0E-4D) {
                    hitPos = tracked.lastPos;
                }
                float blastRadius = tracked.supercharged ? 2.05F : 1.55F;
                serverLevel.explode(
                        this,
                        null,
                        null,
                        hitPos.x,
                        hitPos.y,
                        hitPos.z,
                        blastRadius,
                        false,
                        Level.ExplosionInteraction.NONE
                );
                serverLevel.sendParticles(BRIMSTONE_SULFUR_DUST,
                        hitPos.x, hitPos.y, hitPos.z,
                        24, 0.75D, 0.55D, 0.75D, 0.05D);
                serverLevel.sendParticles(ParticleTypes.LAVA,
                        hitPos.x, hitPos.y, hitPos.z,
                        10, 0.45D, 0.35D, 0.45D, 0.04D);

                double igniteRange = tracked.supercharged ? 3.2D : 2.4D;
                for (LivingEntity victim : serverLevel.getEntitiesOfClass(
                        LivingEntity.class,
                        new net.minecraft.world.phys.AABB(
                                hitPos.x - igniteRange, hitPos.y - igniteRange, hitPos.z - igniteRange,
                                hitPos.x + igniteRange, hitPos.y + igniteRange, hitPos.z + igniteRange),
                        e -> e.isAlive() && e != this && !(e instanceof BrimstoneWyvernEntity))) {
                    victim.igniteForSeconds(tracked.supercharged ? 6.0F : 4.0F);
                }
                it.remove();
            }
        }
    }

    private void clearTrackedFireballs() {
        this.activeFireballs.clear();
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(serverLevel, source, amount);
        if (hurt && this.isAlive() && this.biteTicks <= 0 && this.fireballSpitTicks <= 0 && this.modeTransitionTicks <= 0) {
            byte ev = (this.getFlightMode() == MODE_FLYING) ? EVENT_FLYING_HURT : EVENT_GROUND_HURT;
            serverLevel.broadcastEntityEvent(this, ev);
        }
        return hurt;
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_GROUND_BITE -> this.attackAnimationState.start(this.tickCount);
            case EVENT_FLYING_BITE -> {
                this.clientFlyingBiteTicks = BITE_DURATION_TICKS;
                this.flyingAttackAnimationState.start(this.tickCount);
            }
            case EVENT_GROUND_FIREBALL -> this.fireballSpitAnimationState.start(this.tickCount);
            case EVENT_FLYING_FIREBALL -> {
                this.clientFlyingFireballTicks = FIREBALL_SPIT_DURATION_TICKS;
                this.flyingFireballSpitAnimationState.start(this.tickCount);
            }
            case EVENT_TAKEOFF -> {
                this.clientTakeoffTicks = TAKEOFF_DURATION_TICKS;
                this.takeoffAnimationState.start(this.tickCount);
            }
            case EVENT_LANDING -> {
                this.clientLandingTicks = LANDING_DURATION_TICKS;
                this.landingAnimationState.start(this.tickCount);
            }
            case EVENT_GROUND_HURT -> this.hurtAnimationState.start(this.tickCount);
            case EVENT_FLYING_HURT -> {
                this.clientFlyingHurtTicks = FLYING_HURT_DURATION_TICKS;
                this.flyingHurtAnimationState.start(this.tickCount);
            }
            default -> super.handleEntityEvent(id);
        }
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (this.deathTime == 1 && !this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            this.setFlightMode(MODE_GROUNDED);
            this.setLavaSwimming(false);
            this.clearTrackedFireballs();
            serverLevel.sendParticles(BRIMSTONE_SULFUR_DUST,
                    this.getX(), this.getY() + 0.9D, this.getZ(),
                    20, 0.65D, 0.45D, 0.65D, 0.04D);
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                    this.getX(), this.getY() + 0.8D, this.getZ(),
                    14, 0.55D, 0.35D, 0.55D, 0.03D);
        }
        if (this.deathTime >= DEATH_DURATION_TICKS && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        boolean isLavaBucket = held.is(Items.LAVA_BUCKET);
        boolean isMagmaTribute = held.is(Items.MAGMA_CREAM) || held.is(Items.BLAZE_POWDER) || isLavaBucket;
        if (player.isShiftKeyDown() && isMagmaTribute && this.getTarget() == null && this.isAlive()) {
            if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                if (!player.getAbilities().instabuild) {
                    if (isLavaBucket) {
                        player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                    } else {
                        held.shrink(1);
                    }
                }
                this.heal(8.0F);
                this.rechargeFireInMagma(serverLevel, true);
                if (this.scaleShedCooldown <= 0) {
                    this.scaleShedCooldown = SCALE_SHED_COOLDOWN_TICKS;
                    Item scaleItem = BuiltInRegistries.ITEM.getOptional(WYVERN_SCALE_ID).orElse(Items.BLAZE_POWDER);
                    this.spawnAtLocation(serverLevel, new ItemStack(scaleItem));
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    public static boolean isVolcanicWyvernSpawnSurface(BlockGetter level, BlockPos belowPos, BlockState belowState) {
        if (belowState.is(Blocks.LAVA)) {
            return true;
        }
        if (belowState.isAir()) {
            return false;
        }
        String path = BuiltInRegistries.BLOCK.getKey(belowState.getBlock()).getPath();
        return belowState.is(Blocks.MAGMA_BLOCK)
                || belowState.is(Blocks.BASALT)
                || belowState.is(Blocks.SMOOTH_BASALT)
                || belowState.is(Blocks.POLISHED_BASALT)
                || belowState.is(Blocks.BLACKSTONE)
                || belowState.is(Blocks.NETHERRACK)
                || belowState.is(Blocks.OBSIDIAN)
                || path.contains("basalt")
                || path.contains("magma")
                || path.contains("ash")
                || path.contains("brimstone")
                || belowState.isFaceSturdy(level, belowPos, Direction.UP);
    }

    public static boolean checkBrimstoneWyvernSpawnRules(
            EntityType<? extends BrimstoneWyvernEntity> type,
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
                || spawnReason == EntitySpawnReason.COMMAND) {
            return true;
        }
        if (level.getBlockState(pos).is(Blocks.LAVA)) {
            return true;
        }
        for (int dy = 1; dy <= 3; dy++) {
            BlockPos belowPos = pos.below(dy);
            BlockState belowState = level.getBlockState(belowPos);
            if (belowState.isAir()) {
                continue;
            }
            return isVolcanicWyvernSpawnSurface(level, belowPos, belowState);
        }
        return false;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        if (level.getBlockState(this.blockPosition()).is(Blocks.LAVA)) {
            return true;
        }
        for (int dy = 1; dy <= 3; dy++) {
            BlockPos belowPos = this.blockPosition().below(dy);
            BlockState belowState = level.getBlockState(belowPos);
            if (belowState.isAir()) {
                continue;
            }
            return isVolcanicWyvernSpawnSurface(level, belowPos, belowState);
        }
        return super.checkSpawnRules(level, spawnReason);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("FlightMode", Codec.INT, this.getFlightMode());
        output.store("MagmaChargeTicks", Codec.INT, this.magmaChargeTicks);
        output.store("ScaleShedCooldown", Codec.INT, this.scaleShedCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        int savedMode = input.read("FlightMode", Codec.INT).orElse(MODE_GROUNDED);
        this.setFlightMode(savedMode == MODE_FLYING ? MODE_FLYING : MODE_GROUNDED);
        this.magmaChargeTicks = input.read("MagmaChargeTicks", Codec.INT).orElse(MAGMA_FULL_CHARGE_TICKS);
        this.setMagmaCharged(this.magmaChargeTicks > 0);
        this.scaleShedCooldown = input.read("ScaleShedCooldown", Codec.INT).orElse(0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENDER_DRAGON_AMBIENT;
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
    protected float getSoundVolume() {
        return 0.72F;
    }

    @Override
    public float getVoicePitch() {
        return 0.85F + this.random.nextFloat() * 0.18F;
    }

    // =========================================================================
    // AI GOALS: Ground/Lava/Aerial Combat, Magma Recharging & Cavern Vault Flight
    // =========================================================================

    private static final class WyvernCombatGoal extends Goal {
        private final BrimstoneWyvernEntity wyvern;
        private int strafeTicks = 0;
        private boolean orbitClockwise = true;

        WyvernCombatGoal(BrimstoneWyvernEntity wyvern) {
            this.wyvern = wyvern;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.wyvern.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.wyvern.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = this.wyvern.getTarget();
            if (target == null || !(this.wyvern.level() instanceof ServerLevel serverLevel)) {
                return;
            }

            this.wyvern.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double distSq = this.wyvern.distanceToSqr(target);

            // If fire damage is depleted during combat and nearby magma/lava is close, dip in to recharge!
            if (!this.wyvern.isMagmaCharged() && this.wyvern.tickCount % 20 == 0) {
                BlockPos magmaPos = findNearbyMagmaOrLava(serverLevel, this.wyvern.blockPosition(), 9);
                if (magmaPos != null && this.wyvern.biteTicks <= 0 && this.wyvern.fireballSpitTicks <= 0) {
                    if (this.wyvern.getFlightMode() == MODE_FLYING) {
                        this.wyvern.getMoveControl().setWantedPosition(
                                magmaPos.getX() + 0.5D, magmaPos.getY() + 0.6D, magmaPos.getZ() + 0.5D, 1.25D);
                    } else {
                        this.wyvern.getNavigation().moveTo(
                                magmaPos.getX() + 0.5D, magmaPos.getY() + 0.5D, magmaPos.getZ() + 0.5D, 1.15D);
                    }
                    return;
                }
            }

            if (this.wyvern.biteTicks > 0 || this.wyvern.fireballSpitTicks > 0 || this.wyvern.modeTransitionTicks > 0) {
                return;
            }

            // Close-range Magma Jaw Bite (8.0 damage)
            if (distSq <= 12.5D && this.wyvern.combatCooldown <= 0) {
                this.wyvern.startBiteAttack(serverLevel, target);
                return;
            }

            // Mid/Long-range Explosive Brimstone Fireball Spit
            if (distSq >= 20.0D && distSq <= 576.0D
                    && this.wyvern.fireballCooldown <= 0
                    && this.wyvern.combatCooldown <= 0
                    && this.wyvern.hasLineOfSight(target)) {
                this.wyvern.startFireballSpit(serverLevel);
                return;
            }

            int mode = this.wyvern.getFlightMode();
            if (mode == MODE_FLYING) {
                this.strafeTicks++;
                if (this.strafeTicks % 60 == 0) {
                    this.orbitClockwise = !this.orbitClockwise;
                }
                if (distSq > 49.0D && this.wyvern.fireballCooldown > 15) {
                    // Dive in toward prey for a swooping bite
                    this.wyvern.getMoveControl().setWantedPosition(
                            target.getX(), target.getY() + 0.9D, target.getZ(), 1.25D);
                } else {
                    // Circle cavern vault above target
                    double angle = (this.wyvern.tickCount * 0.06D) * (this.orbitClockwise ? 1.0D : -1.0D);
                    double radius = 5.5D;
                    double gx = target.getX() + Math.cos(angle) * radius;
                    double gz = target.getZ() + Math.sin(angle) * radius;
                    double gy = target.getY() + 2.8D;
                    this.wyvern.getMoveControl().setWantedPosition(gx, gy, gz, 1.12D);
                }
            } else if (this.wyvern.isInLava()) {
                Vec3 toTarget = target.position().subtract(this.wyvern.position());
                Vec3 horiz = new Vec3(toTarget.x, 0.0D, toTarget.z);
                if (horiz.lengthSqr() > 1.0E-4D) {
                    Vec3 swimStep = horiz.normalize().scale(0.18D);
                    Vec3 cur = this.wyvern.getDeltaMovement();
                    this.wyvern.setDeltaMovement(
                            Mth.clamp(cur.x + swimStep.x, -0.36D, 0.36D),
                            cur.y,
                            Mth.clamp(cur.z + swimStep.z, -0.36D, 0.36D)
                    );
                }
            } else {
                this.wyvern.getNavigation().moveTo(target, 1.12D);
            }
        }
    }

    private static final class WyvernMagmaBathGoal extends Goal {
        private final BrimstoneWyvernEntity wyvern;
        @Nullable
        private BlockPos targetMagmaPos;
        private int bathTicks = 0;

        WyvernMagmaBathGoal(BrimstoneWyvernEntity wyvern) {
            this.wyvern = wyvern;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.wyvern.getTarget() != null || this.wyvern.getFlightMode() != MODE_GROUNDED) {
                return false;
            }
            if (this.wyvern.isMagmaCharged() && this.wyvern.random.nextInt(90) != 0) {
                return false;
            }
            this.targetMagmaPos = findNearbyMagmaOrLava(this.wyvern.level(), this.wyvern.blockPosition(), 10);
            return this.targetMagmaPos != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.wyvern.getTarget() == null
                    && this.wyvern.getFlightMode() == MODE_GROUNDED
                    && this.targetMagmaPos != null
                    && this.bathTicks < 100;
        }

        @Override
        public void start() {
            this.bathTicks = 0;
            if (this.targetMagmaPos != null) {
                this.wyvern.getNavigation().moveTo(
                        this.targetMagmaPos.getX() + 0.5D,
                        this.targetMagmaPos.getY() + 0.5D,
                        this.targetMagmaPos.getZ() + 0.5D,
                        1.0D
                );
            }
        }

        @Override
        public void tick() {
            this.bathTicks++;
            if (this.targetMagmaPos != null && this.wyvern.isInLava()) {
                Vec3 toPool = Vec3.atCenterOf(this.targetMagmaPos).subtract(this.wyvern.position());
                Vec3 horiz = new Vec3(toPool.x, 0.0D, toPool.z);
                if (horiz.lengthSqr() > 0.6D) {
                    Vec3 step = horiz.normalize().scale(0.08D);
                    this.wyvern.setDeltaMovement(this.wyvern.getDeltaMovement().add(step.x, 0.0D, step.z));
                }
            }
        }
    }

    private static final class WyvernCavernSoarGoal extends Goal {
        private final BrimstoneWyvernEntity wyvern;
        @Nullable
        private Vec3 waypoint;
        private int ticksLeft = 0;

        WyvernCavernSoarGoal(BrimstoneWyvernEntity wyvern) {
            this.wyvern = wyvern;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.wyvern.getFlightMode() != MODE_FLYING || this.wyvern.getTarget() != null) {
                return false;
            }
            this.waypoint = this.pickCavernVaultWaypoint();
            return this.waypoint != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.wyvern.getFlightMode() == MODE_FLYING
                    && this.wyvern.getTarget() == null
                    && this.waypoint != null
                    && this.ticksLeft > 0
                    && this.wyvern.position().distanceToSqr(this.waypoint) > 2.5D;
        }

        @Override
        public void start() {
            this.ticksLeft = 90;
            if (this.waypoint != null) {
                this.wyvern.getMoveControl().setWantedPosition(this.waypoint.x, this.waypoint.y, this.waypoint.z, 1.0D);
            }
        }

        @Override
        public void tick() {
            this.ticksLeft--;
            if (this.waypoint != null) {
                Vec3 dir = this.waypoint.subtract(this.wyvern.position());
                if (dir.horizontalDistanceSqr() > 1.0E-3D) {
                    float yaw = (float) (Mth.atan2(dir.z, dir.x) * Mth.RAD_TO_DEG) - 90.0F;
                    this.wyvern.setYRot(Mth.rotLerp(0.12F, this.wyvern.getYRot(), yaw));
                    this.wyvern.yBodyRot = this.wyvern.getYRot();
                }
            }
        }

        @Nullable
        private Vec3 pickCavernVaultWaypoint() {
            RandomSource rng = this.wyvern.getRandom();
            Level level = this.wyvern.level();
            for (int attempt = 0; attempt < 8; attempt++) {
                double dx = (rng.nextDouble() - 0.5D) * 18.0D;
                double dy = (rng.nextDouble() - 0.35D) * 7.0D;
                double dz = (rng.nextDouble() - 0.5D) * 18.0D;
                BlockPos candidate = BlockPos.containing(this.wyvern.getX() + dx, this.wyvern.getY() + dy, this.wyvern.getZ() + dz);
                if (level.isEmptyBlock(candidate) && level.isEmptyBlock(candidate.above())) {
                    return Vec3.atCenterOf(candidate);
                }
            }
            return null;
        }
    }

    @Nullable
    static BlockPos findNearbyMagmaOrLava(Level level, BlockPos origin, int radius) {
        BlockPos best = null;
        double bestDistSq = Double.MAX_VALUE;
        for (BlockPos check : BlockPos.betweenClosed(
                origin.offset(-radius, -3, -radius),
                origin.offset(radius, 2, radius))) {
            BlockState st = level.getBlockState(check);
            if (isMagmaRechargeBlock(st)) {
                double d = check.distSqr(origin);
                if (d < bestDistSq) {
                    bestDistSq = d;
                    best = check.immutable();
                }
            }
        }
        return best;
    }
}
