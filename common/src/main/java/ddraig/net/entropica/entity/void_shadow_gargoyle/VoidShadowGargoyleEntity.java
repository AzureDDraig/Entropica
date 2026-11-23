package ddraig.net.entropica.entity.void_shadow_gargoyle;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.void_rift_shade.VoidRiftShadeEntity;
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
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
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

/**
 * Fauna 51 — Void Shadow-Gargoyle (Rift Sentinel) (entropica:void_shadow_gargoyle).
 * Hostile crying-obsidian cathedral sentinel native to 6.5 Rift Flats & 6.2 Basalt Delves:
 * - Perches on obsidian blocks wrapping its wings like a cloak (idle / walk), launching into rapid
 *   diving flight (fly) to ambush prey below.
 * - Shockwave Screech & Diving Swoop (attack): screeches to deal 4 shock damage, dives with claws & talons,
 *   and blinks directly behind the target!
 * - Damage Blink (blink_out -> blink_in): blinks-out into a vertical Void portal slit and blinks-in when
 *   taking damage, leaving dense purple portal particles (suppressed near Spatial Phase Anchors).
 * - Forward Stone Collapse (death) & Obsidian Gargoyle Wing drop.
 */
public class VoidShadowGargoyleEntity extends Monster implements FlyingAnimal {

    private static final EntityDataAccessor<Boolean> AIRBORNE_FLIGHT =
            SynchedEntityData.defineId(VoidShadowGargoyleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> BLINK_PHASING =
            SynchedEntityData.defineId(VoidShadowGargoyleEntity.class, EntityDataSerializers.BOOLEAN);

    private static final byte EVENT_SCREECH_SWOOP = 4;
    private static final byte EVENT_BLINK_OUT = 61;
    private static final byte EVENT_BLINK_IN = 62;
    private static final byte EVENT_HURT_SHIELD = 63;

    private static final int SCREECH_SWOOP_DURATION_TICKS = 32; // 1.60s attack
    private static final int BLINK_OUT_DURATION_TICKS = 20;     // 1.00s blink_out hold
    private static final int BLINK_IN_DURATION_TICKS = 20;      // 1.00s blink_in once
    private static final int DEATH_DURATION_TICKS = 36;         // 1.80s death hold
    private static final int BLINK_COOLDOWN_TICKS = 90;
    private static final int WING_SHED_COOLDOWN_TICKS = 1200;

    private static final ResourceLocation OBSIDIAN_GARGOYLE_WING_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "obsidian_gargoyle_wing");

    private static final DustParticleOptions PORTAL_PURPLE_DUST = new DustParticleOptions(0x9D4EDD, 0.72F);
    private static final DustParticleOptions NEON_MAGENTA_DUST = new DustParticleOptions(0xD946EF, 0.65F);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState flyAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState blinkOutAnimationState = new AnimationState();
    public final AnimationState blinkInAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    private final MoveControl groundMoveControl;
    private final FlyingMoveControl flyingMoveControl;
    private final GroundPathNavigation groundNav;
    private final FlyingPathNavigation flyingNav;

    private int attackTicks = 0;
    private int attackCooldown = 0;
    private int blinkOutTicks = 0;
    private int blinkInTicks = 0;
    private int blinkCooldown = 0;
    private int wingShedCooldown = 0;

    @Nullable
    private Vec3 pendingBlinkDest = null;
    private float pendingBlinkYaw = 0.0F;

    public VoidShadowGargoyleEntity(EntityType<? extends VoidShadowGargoyleEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 10;
        this.groundMoveControl = new MoveControl(this);
        this.flyingMoveControl = new FlyingMoveControl(this, 22, true);
        this.groundNav = new GroundPathNavigation(this, level);
        FlyingPathNavigation fNav = new FlyingPathNavigation(this, level);
        fNav.setCanOpenDoors(false);
        fNav.setCanFloat(true);
        this.flyingNav = fNav;

        this.moveControl = this.groundMoveControl;
        this.navigation = this.groundNav;
        this.setPathfindingMalus(PathType.LAVA, -1.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.45D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FLYING_SPEED, 0.62D)
                .add(Attributes.FOLLOW_RANGE, 28.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(AIRBORNE_FLIGHT, false);
        builder.define(BLINK_PHASING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new GargoyleDiveAndScreechCombatGoal(this));
        this.goalSelector.addGoal(2, new ObsidianPerchGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.85D) {
            @Override
            public boolean canUse() {
                return !VoidShadowGargoyleEntity.this.isAirborneFlight()
                        && !VoidShadowGargoyleEntity.this.isPerchedOnObsidian()
                        && super.canUse();
            }
        });
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean isAirborneFlight() {
        return this.entityData.get(AIRBORNE_FLIGHT);
    }

    public void setAirborneFlight(boolean airborne) {
        this.entityData.set(AIRBORNE_FLIGHT, airborne);
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

    public boolean isBlinkPhasing() {
        return this.entityData.get(BLINK_PHASING);
    }

    private void setBlinkPhasing(boolean value) {
        this.entityData.set(BLINK_PHASING, value);
    }

    @Override
    public boolean isFlying() {
        return this.isAirborneFlight() || !this.onGround();
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
        // Winged obsidian sentinel never takes fall damage
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        if (source.is(DamageTypeTags.IS_FIRE)
                || source.is(DamageTypeTags.IS_FALL)
                || source.is(DamageTypes.HOT_FLOOR)
                || source.is(DamageTypes.LAVA)
                || source.is(DamageTypes.DRAGON_BREATH)) {
            return true;
        }
        // Evades projectiles while collapsed inside its vertical Void portal slit
        if (source.is(DamageTypeTags.IS_PROJECTILE)
                && (this.blinkOutTicks > 0 && this.blinkOutTicks <= 8 || this.blinkInTicks >= 15)) {
            return true;
        }
        return super.isInvulnerableTo(level, source);
    }

    public boolean isPerchedOnObsidian() {
        if (!this.onGround()) {
            return false;
        }
        return isObsidianPerchBlock(this.level().getBlockState(this.blockPosition().below()));
    }

    public static boolean isObsidianPerchBlock(BlockState state) {
        if (state.isAir()) {
            return false;
        }
        if (state.is(Blocks.OBSIDIAN) || state.is(Blocks.CRYING_OBSIDIAN)) {
            return true;
        }
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return path.contains("obsidian") || path.contains("void_stone") || path.contains("void_crystal");
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            this.updateClientAnimations();
            this.spawnClientPortalParticles();
            return;
        }

        if (!this.isAlive()) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();

        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }
        if (this.blinkCooldown > 0) {
            this.blinkCooldown--;
        }
        if (this.wingShedCooldown > 0) {
            this.wingShedCooldown--;
        }

        this.tickVoidBlinkSequence(serverLevel);
        this.tickScreechAndSwoopAttack(serverLevel);
    }

    private void updateClientAnimations() {
        if (this.isDeadOrDying()) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.flyAnimationState.stop();
            this.attackAnimationState.stop();
            this.blinkOutAnimationState.stop();
            this.blinkInAnimationState.stop();
            this.hurtAnimationState.stop();
            if (!this.deathAnimationState.isStarted()) {
                this.deathAnimationState.start(this.tickCount);
            }
            return;
        }

        if (this.isAirborneFlight() || !this.onGround()) {
            this.flyAnimationState.startIfStopped(this.tickCount);
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            return;
        }

        this.flyAnimationState.stop();
        double hSpeedSqr = this.getDeltaMovement().horizontalDistanceSqr();
        if (hSpeedSqr > 4.0E-4D || this.walkAnimation.speed() > 0.08F) {
            this.walkAnimationState.startIfStopped(this.tickCount);
            this.idleAnimationState.stop();
        } else {
            this.idleAnimationState.startIfStopped(this.tickCount);
            this.walkAnimationState.stop();
        }
    }

    private void spawnClientPortalParticles() {
        if (this.isDeadOrDying()) {
            return;
        }
        int chance = this.isBlinkPhasing() ? 1 : 5;
        if (this.random.nextInt(chance) == 0) {
            this.level().addParticle(ParticleTypes.PORTAL,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.9D,
                    this.getY() + 0.95D + (this.random.nextDouble() - 0.5D) * 0.65D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.9D,
                    (this.random.nextDouble() - 0.5D) * 0.15D,
                    (this.random.nextDouble() - 0.5D) * 0.15D,
                    (this.random.nextDouble() - 0.5D) * 0.15D);
        }
    }

    /**
     * Shockwave Screech (4 shock damage at 0.45s) -> Diving Claw Swoop (0.85s) -> Blink Behind Target (1.55s).
     */
    public void startScreechAndDiveAttack(ServerLevel serverLevel, LivingEntity target) {
        if (this.attackTicks > 0 || this.blinkOutTicks > 0 || this.blinkInTicks > 0) {
            return;
        }
        this.attackTicks = SCREECH_SWOOP_DURATION_TICKS;
        this.attackCooldown = 42;
        serverLevel.broadcastEntityEvent(this, EVENT_SCREECH_SWOOP);
        serverLevel.playSound(null, this.getX(), this.getY() + 1.1D, this.getZ(),
                SoundEvents.PHANTOM_SWOOP, SoundSource.HOSTILE, 0.95F, 0.78F);

        // Rapid downward/forward swoop impulse toward target!
        Vec3 diff = target.position().add(0.0D, 0.5D, 0.0D).subtract(this.position());
        if (diff.lengthSqr() > 1.0E-4D) {
            Vec3 dive = diff.normalize().scale(0.62D);
            this.setDeltaMovement(dive.x, Mth.clamp(dive.y, -0.55D, 0.25D), dive.z);
        }
    }

    private void tickScreechAndSwoopAttack(ServerLevel serverLevel) {
        if (this.attackTicks <= 0) {
            return;
        }
        this.attackTicks--;

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            this.getLookControl().setLookAt(target, 32.0F, 32.0F);
        }

        // 1. t = 0.45s (remaining == 23): Wide-jaw Shockwave Screech deals 4 shock damage!
        if (this.attackTicks == 23) {
            this.emitShockwaveScreech(serverLevel);
        }

        // 2. t = 0.85s (remaining == 15): Diving double-claw & talon swoop impact!
        if (this.attackTicks == 15 && target != null && target.isAlive()) {
            if (this.distanceToSqr(target) <= 13.5D) {
                target.invulnerableTime = 0;
                this.doHurtTarget(serverLevel, target);
                serverLevel.sendParticles(NEON_MAGENTA_DUST,
                        target.getX(), target.getY() + 0.9D, target.getZ(),
                        12, 0.3D, 0.3D, 0.3D, 0.03D);
            }
        }

        // 3. End of screech-swoop: blink directly behind the target!
        if (this.attackTicks == 1 && target != null && target.isAlive()) {
            this.tryStartVoidBlink(serverLevel, target, true);
        }
    }

    private void emitShockwaveScreech(ServerLevel serverLevel) {
        Vec3 mawPos = new Vec3(this.getX(), this.getY() + 1.25D, this.getZ());
        Vec3 look = this.getLookAngle();

        serverLevel.playSound(null, mawPos.x, mawPos.y, mawPos.z,
                SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.HOSTILE, 0.85F, 1.55F);
        serverLevel.playSound(null, mawPos.x, mawPos.y, mawPos.z,
                SoundEvents.PHANTOM_BITE, SoundSource.HOSTILE, 1.0F, 0.65F);

        for (int step = 1; step <= 5; step++) {
            Vec3 ringCenter = mawPos.add(look.scale(step * 1.15D));
            double spread = 0.25D + step * 0.16D;
            serverLevel.sendParticles(NEON_MAGENTA_DUST,
                    ringCenter.x, ringCenter.y, ringCenter.z, 6, spread, spread * 0.7D, spread, 0.02D);
            serverLevel.sendParticles(PORTAL_PURPLE_DUST,
                    ringCenter.x, ringCenter.y, ringCenter.z, 5, spread, spread * 0.7D, spread, 0.02D);
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    ringCenter.x, ringCenter.y, ringCenter.z, 4, spread * 0.6D, spread * 0.5D, spread * 0.6D, 0.04D);
        }

        for (LivingEntity victim : serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(6.8D, 3.5D, 6.8D),
                e -> e.isAlive() && e != this && !(e instanceof VoidShadowGargoyleEntity))) {
            if (victim instanceof Player p && (p.isCreative() || p.isSpectator())) {
                continue;
            }
            Vec3 toVictim = victim.getEyePosition().subtract(mawPos);
            double dist = toVictim.length();
            if (dist <= 6.8D && dist > 0.05D && toVictim.normalize().dot(look) >= 0.50D) {
                if (victim.hurtServer(serverLevel, this.damageSources().sonicBoom(this), 4.0F)) {
                    victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0), this);
                }
            }
        }
    }

    /**
     * Blinks-out into a vertical Void portal slit and blinks-in behind the target (or away from danger),
     * leaving purple portal particles at both departure and arrival.
     */
    public boolean tryStartVoidBlink(ServerLevel serverLevel, @Nullable LivingEntity refTarget, boolean preferBehindTarget) {
        if (this.blinkCooldown > 0
                || this.blinkOutTicks > 0
                || this.blinkInTicks > 0
                || !this.isAlive()) {
            return false;
        }
        // Spatial Phase Anchor stabilizes local space and prevents rift-blinking!
        if (VoidRiftShadeEntity.isWithinStabilizedRiftField(serverLevel, this.blockPosition(), 16.0D)) {
            return false;
        }

        Vec3 dest = this.findBlinkDestination(serverLevel, refTarget, preferBehindTarget);
        if (dest == null) {
            return false;
        }

        this.blinkCooldown = BLINK_COOLDOWN_TICKS;
        this.pendingBlinkDest = dest;
        if (refTarget != null) {
            double dx = refTarget.getX() - dest.x;
            double dz = refTarget.getZ() - dest.z;
            this.pendingBlinkYaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
        } else {
            this.pendingBlinkYaw = this.getYRot();
        }

        this.blinkOutTicks = BLINK_OUT_DURATION_TICKS;
        this.attackTicks = 0;
        this.setBlinkPhasing(true);
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);

        serverLevel.broadcastEntityEvent(this, EVENT_BLINK_OUT);
        serverLevel.playSound(null, this.getX(), this.getY() + 0.95D, this.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.95F, 1.15F);
        this.spawnPortalSlitParticles(serverLevel, this.position());
        return true;
    }

    private void tickVoidBlinkSequence(ServerLevel serverLevel) {
        if (this.blinkOutTicks > 0) {
            this.blinkOutTicks--;
            this.setDeltaMovement(Vec3.ZERO);
            if (this.blinkOutTicks % 4 == 0) {
                serverLevel.sendParticles(ParticleTypes.PORTAL,
                        this.getX(), this.getY() + 1.0D, this.getZ(),
                        10, 0.22D, 0.65D, 0.22D, 0.14D);
            }
            if (this.blinkOutTicks == 0) {
                Vec3 oldPos = this.position();
                if (this.pendingBlinkDest != null) {
                    this.teleportTo(this.pendingBlinkDest.x, this.pendingBlinkDest.y, this.pendingBlinkDest.z);
                    this.setYRot(this.pendingBlinkYaw);
                    this.setYHeadRot(this.pendingBlinkYaw);
                    this.yBodyRot = this.pendingBlinkYaw;
                    this.pendingBlinkDest = null;
                }
                this.spawnPortalSlitParticles(serverLevel, oldPos);
                this.spawnPortalSlitParticles(serverLevel, this.position());
                serverLevel.playSound(null, this.getX(), this.getY() + 0.95D, this.getZ(),
                        SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.95F, 0.92F);

                this.blinkInTicks = BLINK_IN_DURATION_TICKS;
                serverLevel.broadcastEntityEvent(this, EVENT_BLINK_IN);
            }
            return;
        }

        if (this.blinkInTicks > 0) {
            this.blinkInTicks--;
            if (this.blinkInTicks == 0) {
                this.setBlinkPhasing(false);
            }
        }
    }

    private void spawnPortalSlitParticles(ServerLevel serverLevel, Vec3 pos) {
        serverLevel.sendParticles(ParticleTypes.PORTAL,
                pos.x, pos.y + 1.0D, pos.z, 34, 0.30D, 0.75D, 0.30D, 0.22D);
        serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                pos.x, pos.y + 1.0D, pos.z, 18, 0.16D, 0.65D, 0.16D, 0.06D);
        serverLevel.sendParticles(PORTAL_PURPLE_DUST,
                pos.x, pos.y + 1.0D, pos.z, 14, 0.24D, 0.60D, 0.24D, 0.02D);
        serverLevel.sendParticles(NEON_MAGENTA_DUST,
                pos.x, pos.y + 1.0D, pos.z, 10, 0.18D, 0.55D, 0.18D, 0.02D);
    }

    private @Nullable Vec3 findBlinkDestination(
            ServerLevel serverLevel,
            @Nullable LivingEntity refTarget,
            boolean preferBehindTarget) {
        // 1. First try blinking directly behind the target's back!
        if (preferBehindTarget && refTarget != null && refTarget.isAlive()) {
            Vec3 targetLookHoriz = new Vec3(refTarget.getLookAngle().x, 0.0D, refTarget.getLookAngle().z);
            if (targetLookHoriz.lengthSqr() < 1.0E-4D) {
                float rYaw = refTarget.getYRot() * Mth.DEG_TO_RAD;
                targetLookHoriz = new Vec3(-Mth.sin(rYaw), 0.0D, Mth.cos(rYaw));
            } else {
                targetLookHoriz = targetLookHoriz.normalize();
            }

            for (double dist : new double[]{2.2D, 2.9D, 1.7D}) {
                Vec3 behindBase = refTarget.position().subtract(targetLookHoriz.scale(dist));
                for (int dy = 1; dy >= -2; dy--) {
                    Vec3 candidate = new Vec3(behindBase.x, Mth.floor(refTarget.getY()) + dy, behindBase.z);
                    if (this.isClearBlinkSpot(serverLevel, candidate)) {
                        return candidate;
                    }
                }
            }
        }

        // 2. Fallback: blink to a nearby flanking or elevated obsidian perch spot!
        Vec3 center = (refTarget != null && refTarget.isAlive()) ? refTarget.position() : this.position();
        for (int attempt = 0; attempt < 16; attempt++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double dist = 3.0D + this.random.nextDouble() * 4.0D;
            int cx = Mth.floor(center.x + Math.cos(angle) * dist);
            int cz = Mth.floor(center.z + Math.sin(angle) * dist);
            int baseY = Mth.floor(center.y);

            for (int dy = 3; dy >= -3; dy--) {
                BlockPos standPos = new BlockPos(cx, baseY + dy, cz);
                BlockPos floorPos = standPos.below();
                BlockState floor = serverLevel.getBlockState(floorPos);
                if (floor.isAir() || !floor.blocksMotion() || !floor.getFluidState().isEmpty()) {
                    continue;
                }
                Vec3 candidate = new Vec3(cx + 0.5D, standPos.getY(), cz + 0.5D);
                if (this.isClearBlinkSpot(serverLevel, candidate)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private boolean isClearBlinkSpot(ServerLevel serverLevel, Vec3 candidate) {
        BlockPos pos = BlockPos.containing(candidate);
        if (!serverLevel.getBlockState(pos).getFluidState().isEmpty()) {
            return false;
        }
        return serverLevel.noCollision(this, this.getDimensions(this.getPose()).makeBoundingBox(candidate));
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(serverLevel, source, amount);
        if (hurt && this.isAlive()) {
            LivingEntity attacker = source.getEntity() instanceof LivingEntity le ? le : this.getTarget();
            // Blinks-out and blinks-in when taking damage, leaving purple portal particles!
            if (!this.tryStartVoidBlink(serverLevel, attacker, attacker != null)) {
                serverLevel.broadcastEntityEvent(this, EVENT_HURT_SHIELD);
                serverLevel.sendParticles(ParticleTypes.PORTAL,
                        this.getX(), this.getY() + 1.0D, this.getZ(),
                        16, 0.35D, 0.45D, 0.35D, 0.12D);
            }
        }
        return hurt;
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_SCREECH_SWOOP -> this.attackAnimationState.start(this.tickCount);
            case EVENT_BLINK_OUT -> {
                this.blinkInAnimationState.stop();
                this.blinkOutAnimationState.start(this.tickCount);
            }
            case EVENT_BLINK_IN -> {
                this.blinkOutAnimationState.stop();
                this.blinkInAnimationState.start(this.tickCount);
            }
            case EVENT_HURT_SHIELD -> this.hurtAnimationState.start(this.tickCount);
            default -> super.handleEntityEvent(id);
        }
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (!this.level().isClientSide() && this.deathTime == 1 && this.level() instanceof ServerLevel serverLevel) {
            this.setAirborneFlight(false);
            this.setBlinkPhasing(false);
            this.spawnPortalSlitParticles(serverLevel, this.position());
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

        // Sneaking offering of Crying Obsidian or Ender Pearl to a perched sentinel sheds a spatial Gargoyle Wing
        if (player.isCrouching() && (stack.is(Items.CRYING_OBSIDIAN) || stack.is(Items.ENDER_PEARL))) {
            if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.heal(5.0F);
                serverLevel.broadcastEntityEvent(this, EVENT_BLINK_IN);
                this.spawnPortalSlitParticles(serverLevel, this.position());
                if (this.wingShedCooldown <= 0) {
                    this.wingShedCooldown = WING_SHED_COOLDOWN_TICKS;
                    Item wingItem = BuiltInRegistries.ITEM.getOptional(OBSIDIAN_GARGOYLE_WING_ID)
                            .orElse(ModItems.OBSIDIAN_GARGOYLE_WING.get());
                    this.spawnAtLocation(serverLevel, new ItemStack(wingItem));
                }
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    public static boolean checkVoidShadowGargoyleSpawnRules(
            EntityType<VoidShadowGargoyleEntity> type,
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
        boolean validFloor = isObsidianPerchBlock(below)
                || below.is(Blocks.BASALT)
                || below.is(Blocks.SMOOTH_BASALT)
                || below.is(Blocks.BLACKSTONE)
                || below.is(Blocks.DEEPSLATE)
                || belowPath.contains("basalt")
                || belowPath.contains("void")
                || belowPath.contains("rift");
        return validFloor && level.getBlockState(pos).getFluidState().isEmpty();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("WingShedCooldown", Codec.INT, this.wingShedCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.wingShedCooldown = input.read("WingShedCooldown", Codec.INT).orElse(0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PHANTOM_AMBIENT;
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
    public float getVoicePitch() {
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.14F + 0.72F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.DEEPSLATE_BRICKS_STEP, 0.40F, 0.85F);
    }

    /**
     * Seeks out elevated Obsidian / Crying Obsidian blocks to perch on like a gothic cathedral sentinel,
     * folding its wings until prey steps into view.
     */
    private static final class ObsidianPerchGoal extends Goal {
        private final VoidShadowGargoyleEntity gargoyle;
        @Nullable
        private BlockPos perchStandPos;
        private int searchCooldown = 0;
        private int flightTimeout = 0;

        ObsidianPerchGoal(VoidShadowGargoyleEntity gargoyle) {
            this.gargoyle = gargoyle;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.gargoyle.getTarget() != null || this.gargoyle.isBlinkPhasing()) {
                return false;
            }
            if (this.gargoyle.isPerchedOnObsidian()) {
                if (this.gargoyle.isAirborneFlight()) {
                    this.gargoyle.setAirborneFlight(false);
                }
                return false;
            }
            if (this.searchCooldown > 0) {
                this.searchCooldown--;
                return false;
            }
            this.searchCooldown = 40 + this.gargoyle.getRandom().nextInt(40);
            this.perchStandPos = this.findNearbyObsidianPerch();
            return this.perchStandPos != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.perchStandPos != null
                    && this.gargoyle.getTarget() == null
                    && !this.gargoyle.isBlinkPhasing()
                    && this.flightTimeout < 140
                    && isObsidianPerchBlock(this.gargoyle.level().getBlockState(this.perchStandPos.below()));
        }

        @Override
        public void start() {
            this.flightTimeout = 0;
            this.gargoyle.setAirborneFlight(true);
        }

        @Override
        public void tick() {
            if (this.perchStandPos == null) {
                return;
            }
            this.flightTimeout++;
            Vec3 goal = new Vec3(
                    this.perchStandPos.getX() + 0.5D,
                    this.perchStandPos.getY() + 0.05D,
                    this.perchStandPos.getZ() + 0.5D
            );
            if (this.gargoyle.distanceToSqr(goal) <= 1.15D) {
                this.gargoyle.setPos(goal.x, this.perchStandPos.getY(), goal.z);
                this.gargoyle.setDeltaMovement(Vec3.ZERO);
                this.gargoyle.setAirborneFlight(false);
                this.perchStandPos = null;
                return;
            }
            this.gargoyle.getMoveControl().setWantedPosition(goal.x, goal.y + 0.35D, goal.z, 1.10D);
        }

        @Override
        public void stop() {
            this.perchStandPos = null;
            if (this.gargoyle.onGround() && this.gargoyle.getTarget() == null) {
                this.gargoyle.setAirborneFlight(false);
            }
        }

        private @Nullable BlockPos findNearbyObsidianPerch() {
            BlockPos origin = this.gargoyle.blockPosition();
            Level level = this.gargoyle.level();
            BlockPos best = null;
            double bestScore = -Double.MAX_VALUE;
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

            for (int dx = -12; dx <= 12; dx += 2) {
                for (int dy = -3; dy <= 8; dy++) {
                    for (int dz = -12; dz <= 12; dz += 2) {
                        cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                        if (!isObsidianPerchBlock(level.getBlockState(cursor))) {
                            continue;
                        }
                        BlockPos stand = cursor.above();
                        if (!level.getBlockState(stand).isAir() || !level.getBlockState(stand.above()).isAir()) {
                            continue;
                        }
                        // Prefer higher vantage perches within close horizontal reach
                        double score = stand.getY() * 2.5D - Math.sqrt(origin.distSqr(stand)) * 0.4D;
                        if (score > bestScore) {
                            bestScore = score;
                            best = stand.immutable();
                        }
                    }
                }
            }
            return best;
        }
    }

    /**
     * Launches off obsidian perches to fly rapidly down onto prey, screeching for 4 shock damage,
     * swooping with claws/talons, and blinking behind the target!
     */
    private static final class GargoyleDiveAndScreechCombatGoal extends Goal {
        private final VoidShadowGargoyleEntity gargoyle;

        GargoyleDiveAndScreechCombatGoal(VoidShadowGargoyleEntity gargoyle) {
            this.gargoyle = gargoyle;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.gargoyle.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.gargoyle.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }
            if (target instanceof Player p && (p.isCreative() || p.isSpectator())) {
                return false;
            }
            return this.gargoyle.distanceToSqr(target) <= 1024.0D;
        }

        @Override
        public void start() {
            this.gargoyle.setAirborneFlight(true);
            if (this.gargoyle.onGround()) {
                this.gargoyle.setDeltaMovement(this.gargoyle.getDeltaMovement().add(0.0D, 0.36D, 0.0D));
            }
        }

        @Override
        public void stop() {
            this.gargoyle.getNavigation().stop();
            if (this.gargoyle.onGround()) {
                this.gargoyle.setAirborneFlight(false);
            }
        }

        @Override
        public void tick() {
            LivingEntity target = this.gargoyle.getTarget();
            if (target == null || !(this.gargoyle.level() instanceof ServerLevel serverLevel)) {
                return;
            }
            if (this.gargoyle.blinkOutTicks > 0 || this.gargoyle.blinkInTicks > 0) {
                return;
            }

            if (!this.gargoyle.isAirborneFlight()) {
                this.gargoyle.setAirborneFlight(true);
            }

            this.gargoyle.getLookControl().setLookAt(target, 32.0F, 32.0F);
            double distSqr = this.gargoyle.distanceToSqr(target);

            // Rapid downward flight dive toward target!
            Vec3 diveGoal = new Vec3(target.getX(), target.getY() + 0.9D, target.getZ());
            this.gargoyle.getMoveControl().setWantedPosition(diveGoal.x, diveGoal.y, diveGoal.z, 1.45D);

            if (this.gargoyle.attackCooldown <= 0
                    && this.gargoyle.attackTicks <= 0
                    && distSqr <= 36.0D
                    && this.gargoyle.hasLineOfSight(target)) {
                this.gargoyle.startScreechAndDiveAttack(serverLevel, target);
            }
        }
    }
}
