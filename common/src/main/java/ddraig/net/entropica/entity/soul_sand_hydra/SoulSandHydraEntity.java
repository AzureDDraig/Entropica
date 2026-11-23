package ddraig.net.entropica.entity.soul_sand_hydra;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

/**
 * Fauna 50 — Soul-Sand Hydra (Multi-Headed Serpentine) (entropica:soul_sand_hydra).
 * Hostile 3-headed draconic Hydra native to 6.3 Soulfire Fjords & 6.4 Ash Canyons:
 * - Stalks across soul-sand basins without Soul Sand slowness (idle / walk / run).
 * - Soul-Sand Submerge Ambush (submerge_ambush): crouches low and submerges into soft soul sand to hide,
 *   erupting upward in a shower of soul sand and soulfire when players approach!
 * - Cascading Triple-Head Wither Bite (attack): Left (0.55s) -> Right (0.75s) -> Center (1.00s) jaws snap
 *   in sequence for 3 + 3 + 4 = 10 total damage + Wither.
 * - Reared Soulfire Roar (wither_roar), Sweeping Triple-Maw Soulfire Breath Cone (breath_attack),
 *   and Seamless Two-Stage Soulfire Spell Volley (spell_charge -> spell_release).
 * - Splayed 3-Neck Collapse (death) & Hydra Soul-Gland harvest.
 */
public class SoulSandHydraEntity extends Monster {

    private static final EntityDataAccessor<Boolean> SUBMERGED_AMBUSH =
            SynchedEntityData.defineId(SoulSandHydraEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CHARGING_RUN =
            SynchedEntityData.defineId(SoulSandHydraEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> GLAND_GLOWING =
            SynchedEntityData.defineId(SoulSandHydraEntity.class, EntityDataSerializers.BOOLEAN);

    private static final byte EVENT_TRIPLE_BITE = 4;
    private static final byte EVENT_SUBMERGE_CROUCH = 61;
    private static final byte EVENT_AMBUSH_ERUPT = 62;
    private static final byte EVENT_WITHER_ROAR = 63;
    private static final byte EVENT_SPELL_CHARGE = 64;
    private static final byte EVENT_SPELL_RELEASE = 65;
    private static final byte EVENT_BREATH_ATTACK = 66;
    private static final byte EVENT_HURT_RECOIL = 67;

    private static final int TRIPLE_BITE_DURATION_TICKS = 32;     // 1.60s attack
    private static final int SUBMERGE_AMBUSH_DURATION_TICKS = 52; // 2.60s submerge_ambush
    private static final int AMBUSH_ERUPT_DURATION_TICKS = 27;    // 1.35s eruption half (1.25s -> 2.60s)
    private static final int WITHER_ROAR_DURATION_TICKS = 40;     // 2.00s wither_roar
    private static final int SPELL_CHARGE_DURATION_TICKS = 24;    // 1.20s spell_charge hold
    private static final int SPELL_RELEASE_DURATION_TICKS = 28;   // 1.40s spell_release
    private static final int BREATH_ATTACK_DURATION_TICKS = 52;   // 2.60s breath_attack
    private static final int DEATH_DURATION_TICKS = 40;           // 2.00s death hold
    private static final int GLAND_SHED_COOLDOWN_TICKS = 1200;

    private static final ResourceLocation HYDRA_SOUL_GLAND_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "hydra_soul_gland");

    private static final DustParticleOptions SOULFIRE_CYAN_DUST = new DustParticleOptions(0x38BDF8, 0.78F);
    private static final DustParticleOptions SOULFIRE_ICE_DUST = new DustParticleOptions(0xBAE6FD, 0.62F);
    private static final DustParticleOptions SOULFIRE_DEEP_DUST = new DustParticleOptions(0x0369A1, 0.85F);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState runAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState submergeAmbushAnimationState = new AnimationState();
    public final AnimationState witherRoarAnimationState = new AnimationState();
    public final AnimationState spellChargeAnimationState = new AnimationState();
    public final AnimationState spellReleaseAnimationState = new AnimationState();
    public final AnimationState breathAttackAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    private int tripleBiteTicks = 0;
    private int ambushLungeTicks = 0;
    private int witherRoarTicks = 0;
    private int spellChargeTicks = 0;
    private int spellReleaseTicks = 0;
    private int breathAttackTicks = 0;

    private int combatCooldown = 0;
    private int specialCooldown = 80;
    private int roarCooldown = 0;
    private int submergeHideCooldown = 60;
    private int glandShedCooldown = 0;

    private final List<SoulfireBolt> activeSoulfireBolts = new ArrayList<>();

    public SoulSandHydraEntity(EntityType<? extends SoulSandHydraEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 18;
        this.setPathfindingMalus(PathType.LAVA, 0.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 105.0D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.60D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.FOLLOW_RANGE, 28.0D)
                .add(Attributes.STEP_HEIGHT, 1.1D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SUBMERGED_AMBUSH, false);
        builder.define(CHARGING_RUN, false);
        builder.define(GLAND_GLOWING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SoulSandAmbushHideGoal(this));
        this.goalSelector.addGoal(2, new HydraMultiHeadCombatGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                this,
                Player.class,
                10,
                true,
                false,
                (entity, serverLevel) -> {
                    if (!(entity instanceof Player player)) {
                        return false;
                    }
                    // When crouched in soul-sand ambush, waits patiently until prey steps within 9.5 blocks!
                    if (this.isSubmergedAmbush()) {
                        return this.distanceToSqr(player) <= 90.25D;
                    }
                    return true;
                }
        ));
    }

    public boolean isSubmergedAmbush() {
        return this.entityData.get(SUBMERGED_AMBUSH);
    }

    private void setSubmergedAmbush(boolean value) {
        this.entityData.set(SUBMERGED_AMBUSH, value);
    }

    public boolean isChargingRun() {
        return this.entityData.get(CHARGING_RUN);
    }

    private void setChargingRun(boolean value) {
        this.entityData.set(CHARGING_RUN, value);
    }

    public boolean isGlandGlowing() {
        return this.entityData.get(GLAND_GLOWING);
    }

    private void setGlandGlowing(boolean value) {
        this.entityData.set(GLAND_GLOWING, value);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effectInstance) {
        if (effectInstance.is(MobEffects.WITHER)) {
            return false;
        }
        return super.canBeAffected(effectInstance);
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        if (source.is(DamageTypeTags.IS_FIRE)
                || source.is(DamageTypes.LAVA)
                || source.is(DamageTypes.HOT_FLOOR)
                || source.is(DamageTypes.WITHER)) {
            return true;
        }
        return super.isInvulnerableTo(level, source);
    }

    @Override
    protected float getBlockSpeedFactor() {
        BlockState below = this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement());
        if (isSoulSandBasinBlock(below)) {
            // Native soul-sand predator glides swiftly over soft Soul Sand instead of sinking
            return 1.12F;
        }
        return super.getBlockSpeedFactor();
    }

    public static boolean isSoulSandBasinBlock(BlockState state) {
        if (state.isAir()) {
            return false;
        }
        if (state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL)) {
            return true;
        }
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return path.contains("soul") || path.contains("ash");
    }

    public boolean isBusyWithAction() {
        return this.tripleBiteTicks > 0
                || this.ambushLungeTicks > 0
                || this.witherRoarTicks > 0
                || this.spellChargeTicks > 0
                || this.spellReleaseTicks > 0
                || this.breathAttackTicks > 0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            this.updateClientAnimations();
            this.spawnClientAmbientParticles();
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();
        this.tickActiveSoulfireBolts(serverLevel);

        if (!this.isAlive()) {
            return;
        }

        if (this.combatCooldown > 0) {
            this.combatCooldown--;
        }
        if (this.specialCooldown > 0) {
            this.specialCooldown--;
        }
        if (this.roarCooldown > 0) {
            this.roarCooldown--;
        }
        if (this.submergeHideCooldown > 0) {
            this.submergeHideCooldown--;
        }
        if (this.glandShedCooldown > 0) {
            this.glandShedCooldown--;
        }

        LivingEntity target = this.getTarget();
        boolean activelyChasing = target != null
                && target.isAlive()
                && !this.isSubmergedAmbush()
                && !this.isBusyWithAction()
                && this.getDeltaMovement().horizontalDistanceSqr() > 0.006D;
        this.setChargingRun(activelyChasing);

        // If crouched in soul-sand ambush and a target is acquired within range, erupt upward immediately!
        if (this.isSubmergedAmbush() && target != null && target.isAlive()) {
            this.eruptFromSoulSandAmbush(serverLevel, target);
        }

        this.tickTripleHeadBite(serverLevel);
        this.tickAmbushEruption(serverLevel);
        this.tickWitherRoar(serverLevel);
        this.tickSpellVolley(serverLevel);
        this.tickBreathAttack(serverLevel);

        this.setGlandGlowing(this.spellChargeTicks > 0 || this.spellReleaseTicks > 0 || this.breathAttackTicks > 0 || this.witherRoarTicks > 0);
    }

    private void updateClientAnimations() {
        if (this.isDeadOrDying()) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.runAnimationState.stop();
            this.attackAnimationState.stop();
            this.submergeAmbushAnimationState.stop();
            this.witherRoarAnimationState.stop();
            this.spellChargeAnimationState.stop();
            this.spellReleaseAnimationState.stop();
            this.breathAttackAnimationState.stop();
            this.hurtAnimationState.stop();
            if (!this.deathAnimationState.isStarted()) {
                this.deathAnimationState.start(this.tickCount);
            }
            return;
        }

        if (this.isSubmergedAmbush()) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.runAnimationState.stop();
            return;
        }

        double hSpeedSqr = this.getDeltaMovement().horizontalDistanceSqr();
        boolean moving = hSpeedSqr > 4.0E-4D || this.walkAnimation.speed() > 0.08F;
        if (moving) {
            this.idleAnimationState.stop();
            if (this.isChargingRun()) {
                this.runAnimationState.startIfStopped(this.tickCount);
                this.walkAnimationState.stop();
            } else {
                this.walkAnimationState.startIfStopped(this.tickCount);
                this.runAnimationState.stop();
            }
        } else {
            this.idleAnimationState.startIfStopped(this.tickCount);
            this.walkAnimationState.stop();
            this.runAnimationState.stop();
        }
    }

    private void spawnClientAmbientParticles() {
        if (this.isDeadOrDying()) {
            return;
        }
        if (this.isSubmergedAmbush()) {
            if (this.random.nextInt(5) == 0) {
                this.level().addParticle(ParticleTypes.SOUL,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 1.6D,
                        this.getY() + 0.25D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 1.6D,
                        0.0D, 0.025D, 0.0D);
            }
            return;
        }

        if (this.random.nextInt(5) == 0) {
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 1.4D,
                    this.getY() + 1.75D + (this.random.nextDouble() - 0.5D) * 0.45D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 1.4D,
                    0.0D, 0.015D, 0.0D);
        }
    }

    /**
     * Cascading Left (0.55s) -> Right (0.75s) -> Center (1.00s) triple-head Wither bite (3 + 3 + 4 = 10 total damage).
     */
    public void startCascadingTripleBite(ServerLevel serverLevel) {
        if (this.isBusyWithAction()) {
            return;
        }
        this.tripleBiteTicks = TRIPLE_BITE_DURATION_TICKS;
        this.combatCooldown = 38;
        this.getNavigation().stop();
        serverLevel.broadcastEntityEvent(this, EVENT_TRIPLE_BITE);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 0.65F, 1.35F);
    }

    private void tickTripleHeadBite(ServerLevel serverLevel) {
        if (this.tripleBiteTicks <= 0) {
            return;
        }
        this.tripleBiteTicks--;

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        this.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (this.tripleBiteTicks == 21) {
            // 1. Left Head Snap (3.0 damage + 4s Wither)
            this.applySingleHeadSnap(serverLevel, target, 3.0F, 80, -0.35D, 1.42F);
        } else if (this.tripleBiteTicks == 17) {
            // 2. Right Head Snap (3.0 damage + 5s Wither)
            this.applySingleHeadSnap(serverLevel, target, 3.0F, 100, 0.35D, 1.32F);
        } else if (this.tripleBiteTicks == 12) {
            // 3. Center Head Snap (4.0 damage + 6s Wither; total = 10.0 damage!)
            this.applySingleHeadSnap(serverLevel, target, 4.0F, 120, 0.0D, 1.18F);
        }
    }

    private void applySingleHeadSnap(
            ServerLevel serverLevel,
            LivingEntity target,
            float damage,
            int witherTicks,
            double lateralOffset,
            float pitch) {
        double reachSqr = 18.5D; // ~4.3 blocks reach for outstretched 4-segment S-necks
        if (this.distanceToSqr(target) > reachSqr) {
            return;
        }

        float yawRad = this.getYRot() * Mth.DEG_TO_RAD;
        Vec3 forward = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        Vec3 right = new Vec3(forward.z, 0.0D, -forward.x);
        Vec3 mawPos = this.position()
                .add(0.0D, 1.75D, 0.0D)
                .add(forward.scale(1.65D))
                .add(right.scale(lateralOffset));

        serverLevel.playSound(null, mawPos.x, mawPos.y, mawPos.z,
                SoundEvents.RAVAGER_ATTACK, SoundSource.HOSTILE, 0.75F, pitch);
        serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                mawPos.x, mawPos.y, mawPos.z, 10, 0.25D, 0.22D, 0.25D, 0.04D);
        serverLevel.sendParticles(SOULFIRE_CYAN_DUST,
                mawPos.x, mawPos.y, mawPos.z, 8, 0.22D, 0.22D, 0.22D, 0.02D);

        // Clear invulnerability frames between the 3 cascading head bites so all 3 heads connect!
        target.invulnerableTime = 0;
        DamageSource biteSource = this.damageSources().mobAttack(this);
        if (target.hurtServer(serverLevel, biteSource, damage)) {
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, witherTicks, 0), this);
            if (damage >= 4.0F) {
                target.knockback(0.45D, -forward.x, -forward.z);
            }
        }
    }

    /**
     * Erupts upward from crouched Soul-Sand Ambush (or initiates a mid-combat soul-sand ambush pounce).
     */
    public void eruptFromSoulSandAmbush(ServerLevel serverLevel, @Nullable LivingEntity target) {
        this.setSubmergedAmbush(false);
        this.submergeHideCooldown = 260;
        this.ambushLungeTicks = AMBUSH_ERUPT_DURATION_TICKS;
        this.combatCooldown = 30;

        serverLevel.broadcastEntityEvent(this, EVENT_AMBUSH_ERUPT);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.WARDEN_EMERGE, SoundSource.HOSTILE, 0.75F, 1.35F);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.SOUL_ESCAPE.value(), SoundSource.HOSTILE, 1.0F, 0.9F);

        BlockParticleOption sandSpray = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SOUL_SAND.defaultBlockState());
        serverLevel.sendParticles(sandSpray,
                this.getX(), this.getY() + 0.35D, this.getZ(),
                45, 1.25D, 0.45D, 1.25D, 0.16D);
        serverLevel.sendParticles(ParticleTypes.SOUL,
                this.getX(), this.getY() + 0.65D, this.getZ(),
                18, 1.05D, 0.55D, 1.05D, 0.05D);
        serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                this.getX(), this.getY() + 0.85D, this.getZ(),
                22, 1.05D, 0.55D, 1.05D, 0.06D);

        if (target != null) {
            Vec3 toward = target.position().subtract(this.position());
            Vec3 horiz = new Vec3(toward.x, 0.0D, toward.z);
            if (horiz.lengthSqr() > 1.0E-4D) {
                Vec3 leap = horiz.normalize().scale(0.58D);
                this.setDeltaMovement(leap.x, 0.36D, leap.z);
            }
        }

        // Shifting soul-sand eruption slows nearby prey
        for (LivingEntity nearby : serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(4.5D, 2.5D, 4.5D),
                e -> e.isAlive() && e != this && !(e instanceof SoulSandHydraEntity))) {
            if (nearby instanceof Player p && (p.isCreative() || p.isSpectator())) {
                continue;
            }
            nearby.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1), this);
        }
    }

    public void startMidCombatSubmergePounce(ServerLevel serverLevel) {
        if (this.isBusyWithAction()) {
            return;
        }
        this.ambushLungeTicks = SUBMERGE_AMBUSH_DURATION_TICKS;
        this.specialCooldown = 140;
        this.combatCooldown = 56;
        this.getNavigation().stop();
        serverLevel.broadcastEntityEvent(this, EVENT_SUBMERGE_CROUCH);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.SOUL_SAND_BREAK, SoundSource.HOSTILE, 1.0F, 0.8F);
    }

    private void tickAmbushEruption(ServerLevel serverLevel) {
        if (this.ambushLungeTicks <= 0) {
            return;
        }
        this.ambushLungeTicks--;

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        // At t = 1.58s of submerge_ambush (remaining == 20), lunges upward out of the soul sand!
        if (this.ambushLungeTicks == 20) {
            BlockParticleOption sandSpray = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SOUL_SAND.defaultBlockState());
            serverLevel.sendParticles(sandSpray,
                    this.getX(), this.getY() + 0.35D, this.getZ(),
                    32, 1.1D, 0.4D, 1.1D, 0.14D);
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX(), this.getY() + 0.8D, this.getZ(),
                    16, 0.9D, 0.4D, 0.9D, 0.05D);
            if (target != null && target.isAlive()) {
                Vec3 horiz = new Vec3(target.getX() - this.getX(), 0.0D, target.getZ() - this.getZ());
                if (horiz.lengthSqr() > 1.0E-4D) {
                    Vec3 leap = horiz.normalize().scale(0.62D);
                    this.setDeltaMovement(leap.x, 0.34D, leap.z);
                }
                if (this.distanceToSqr(target) <= 20.25D) {
                    if (target.hurtServer(serverLevel, this.damageSources().mobAttack(this), 8.0F)) {
                        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0), this);
                    }
                }
            }
        }
    }

    public void startWitherRoar(ServerLevel serverLevel) {
        if (this.isBusyWithAction()) {
            return;
        }
        this.witherRoarTicks = WITHER_ROAR_DURATION_TICKS;
        this.roarCooldown = 280;
        this.combatCooldown = 42;
        this.getNavigation().stop();
        serverLevel.broadcastEntityEvent(this, EVENT_WITHER_ROAR);
    }

    private void tickWitherRoar(ServerLevel serverLevel) {
        if (this.witherRoarTicks <= 0) {
            return;
        }
        this.witherRoarTicks--;
        if (this.witherRoarTicks == 26) {
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 1.0F, 0.78F);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.WITHER_AMBIENT, SoundSource.HOSTILE, 0.85F, 0.72F);
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX(), this.getY() + 2.1D, this.getZ(),
                    28, 1.2D, 0.55D, 1.2D, 0.08D);
            serverLevel.sendParticles(ParticleTypes.SCULK_SOUL,
                    this.getX(), this.getY() + 1.9D, this.getZ(),
                    16, 1.0D, 0.45D, 1.0D, 0.05D);

            this.addEffect(new MobEffectInstance(MobEffects.SPEED, 120, 0, false, false));
            for (LivingEntity nearby : serverLevel.getEntitiesOfClass(
                    LivingEntity.class,
                    this.getBoundingBox().inflate(6.5D, 3.0D, 6.5D),
                    e -> e.isAlive() && e != this && !(e instanceof SoulSandHydraEntity))) {
                if (nearby instanceof Player p && (p.isCreative() || p.isSpectator())) {
                    continue;
                }
                nearby.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 0), this);
            }
        }
    }

    /**
     * Seamless Two-Stage Soulfire Spell Volley: spell_charge (1.2s) -> spell_release (1.4s).
     */
    public void startSoulfireSpellVolley(ServerLevel serverLevel) {
        if (this.isBusyWithAction()) {
            return;
        }
        this.spellChargeTicks = SPELL_CHARGE_DURATION_TICKS;
        this.specialCooldown = 120;
        this.combatCooldown = 54;
        this.getNavigation().stop();
        serverLevel.broadcastEntityEvent(this, EVENT_SPELL_CHARGE);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 0.75F, 1.45F);
    }

    private void tickSpellVolley(ServerLevel serverLevel) {
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && (this.spellChargeTicks > 0 || this.spellReleaseTicks > 0)) {
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        if (this.spellChargeTicks > 0) {
            this.spellChargeTicks--;
            if (this.spellChargeTicks % 4 == 0) {
                serverLevel.sendParticles(SOULFIRE_ICE_DUST,
                        this.getX(), this.getY() + 2.15D, this.getZ(),
                        10, 0.65D, 0.35D, 0.65D, 0.03D);
            }
            if (this.spellChargeTicks == 0) {
                this.spellReleaseTicks = SPELL_RELEASE_DURATION_TICKS;
                serverLevel.broadcastEntityEvent(this, EVENT_SPELL_RELEASE);
            }
            return;
        }

        if (this.spellReleaseTicks > 0) {
            this.spellReleaseTicks--;
            // At t = 0.20s of spell_release (remaining == 24), hurls all 3 heads forward to fire 3 Soulfire Bolts!
            if (this.spellReleaseTicks == 24) {
                this.launchTripleSoulfireBolts(serverLevel, target);
            }
        }
    }

    private void launchTripleSoulfireBolts(ServerLevel serverLevel, @Nullable LivingEntity target) {
        Vec3 origin = new Vec3(this.getX(), this.getY() + 1.95D, this.getZ());
        Vec3 baseDir;
        if (target != null && target.isAlive()) {
            Vec3 aim = target.getEyePosition().subtract(origin);
            baseDir = aim.lengthSqr() > 1.0E-4D ? aim.normalize() : this.getLookAngle();
        } else {
            baseDir = this.getLookAngle();
        }

        serverLevel.playSound(null, origin.x, origin.y, origin.z,
                SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 0.85F, 1.28F);

        float[] fanAnglesDeg = new float[]{-13.0F, 0.0F, 13.0F};
        for (float deg : fanAnglesDeg) {
            Vec3 rotated = baseDir.yRot(deg * Mth.DEG_TO_RAD).normalize().scale(0.82D);
            Vec3 spawnPos = origin.add(rotated.scale(1.2D));
            this.activeSoulfireBolts.add(new SoulfireBolt(spawnPos, rotated, 34));
        }
    }

    private void tickActiveSoulfireBolts(ServerLevel serverLevel) {
        if (this.activeSoulfireBolts.isEmpty()) {
            return;
        }
        Iterator<SoulfireBolt> it = this.activeSoulfireBolts.iterator();
        while (it.hasNext()) {
            SoulfireBolt bolt = it.next();
            bolt.ticksLeft--;
            if (bolt.ticksLeft <= 0) {
                it.remove();
                continue;
            }

            Vec3 nextPos = bolt.pos.add(bolt.vel);
            BlockPos blockPos = BlockPos.containing(nextPos);
            if (!serverLevel.getBlockState(blockPos).getCollisionShape(serverLevel, blockPos).isEmpty()) {
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        bolt.pos.x, bolt.pos.y, bolt.pos.z, 8, 0.18D, 0.18D, 0.18D, 0.04D);
                it.remove();
                continue;
            }

            bolt.pos = nextPos;
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    bolt.pos.x, bolt.pos.y, bolt.pos.z, 2, 0.08D, 0.08D, 0.08D, 0.01D);
            serverLevel.sendParticles(SOULFIRE_CYAN_DUST,
                    bolt.pos.x, bolt.pos.y, bolt.pos.z, 2, 0.06D, 0.06D, 0.06D, 0.0D);

            AABB hitBox = new AABB(bolt.pos, bolt.pos).inflate(0.55D);
            List<LivingEntity> hits = serverLevel.getEntitiesOfClass(
                    LivingEntity.class,
                    hitBox,
                    e -> e.isAlive() && e != this && !(e instanceof SoulSandHydraEntity)
            );
            if (!hits.isEmpty()) {
                LivingEntity victim = hits.get(0);
                if (victim.hurtServer(serverLevel, this.damageSources().mobAttack(this), 5.0F)) {
                    victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0), this);
                }
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        bolt.pos.x, bolt.pos.y, bolt.pos.z, 12, 0.25D, 0.25D, 0.25D, 0.05D);
                it.remove();
            }
        }
    }

    /**
     * Sweeping Left-to-Right Triple-Maw Soulfire Breath Cone (breath_attack: 2.60s = 52 ticks).
     */
    public void startSoulfireBreathSweep(ServerLevel serverLevel) {
        if (this.isBusyWithAction()) {
            return;
        }
        this.breathAttackTicks = BREATH_ATTACK_DURATION_TICKS;
        this.specialCooldown = 130;
        this.combatCooldown = 54;
        this.getNavigation().stop();
        serverLevel.broadcastEntityEvent(this, EVENT_BREATH_ATTACK);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 0.8F, 1.15F);
    }

    private void tickBreathAttack(ServerLevel serverLevel) {
        if (this.breathAttackTicks <= 0) {
            return;
        }
        this.breathAttackTicks--;

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && this.breathAttackTicks > 38) {
            this.getLookControl().setLookAt(target, 26.0F, 26.0F);
        }

        // Sweep window: 0.72s to 1.78s (remaining ticks 38 down to 16)
        if (this.breathAttackTicks <= 38 && this.breathAttackTicks >= 16 && this.breathAttackTicks % 3 == 0) {
            float progress = (38 - this.breathAttackTicks) / 22.0F; // 0.0 (left) -> 1.0 (right)
            float sweepOffsetDeg = Mth.lerp(progress, 18.0F, -18.0F);
            float yawRad = (this.getYRot() - sweepOffsetDeg) * Mth.DEG_TO_RAD;
            Vec3 dir = new Vec3(-Mth.sin(yawRad), -0.08D, Mth.cos(yawRad)).normalize();
            Vec3 mawOrigin = new Vec3(this.getX(), this.getY() + 1.85D, this.getZ()).add(dir.scale(1.35D));

            if (this.breathAttackTicks == 36 || this.breathAttackTicks == 24) {
                serverLevel.playSound(null, mawOrigin.x, mawOrigin.y, mawOrigin.z,
                        SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 0.75F, 0.68F);
            }

            for (int step = 1; step <= 6; step++) {
                Vec3 pt = mawOrigin.add(dir.scale(step * 0.95D));
                double spread = 0.16D + step * 0.11D;
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        pt.x, pt.y, pt.z, 4, spread, spread * 0.65D, spread, 0.02D);
                serverLevel.sendParticles(SOULFIRE_CYAN_DUST,
                        pt.x, pt.y, pt.z, 3, spread, spread * 0.65D, spread, 0.01D);
                serverLevel.sendParticles(SOULFIRE_DEEP_DUST,
                        pt.x, pt.y, pt.z, 2, spread, spread * 0.55D, spread, 0.01D);
            }

            for (LivingEntity victim : serverLevel.getEntitiesOfClass(
                    LivingEntity.class,
                    this.getBoundingBox().inflate(6.8D, 2.8D, 6.8D),
                    e -> e.isAlive() && e != this && !(e instanceof SoulSandHydraEntity))) {
                if (victim instanceof Player p && (p.isCreative() || p.isSpectator())) {
                    continue;
                }
                Vec3 toVictim = victim.getEyePosition().subtract(mawOrigin);
                double dist = toVictim.length();
                if (dist <= 6.8D && dist > 0.05D) {
                    double dot = toVictim.normalize().dot(dir);
                    if (dot >= 0.72D) {
                        if (victim.hurtServer(serverLevel, this.damageSources().mobAttack(this), 3.5F)) {
                            victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0), this);
                            victim.igniteForSeconds(3.0F);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        // Crouched in soul-sand masonry ambush halves incoming ambush-breaking damage before erupting
        float adjustedAmount = this.isSubmergedAmbush() ? amount * 0.45F : amount;
        boolean hurt = super.hurtServer(serverLevel, source, adjustedAmount);
        if (hurt && this.isAlive()) {
            if (this.isSubmergedAmbush()) {
                LivingEntity attacker = source.getEntity() instanceof LivingEntity le ? le : null;
                this.eruptFromSoulSandAmbush(serverLevel, attacker);
            } else if (!this.isBusyWithAction()) {
                serverLevel.broadcastEntityEvent(this, EVENT_HURT_RECOIL);
            }
        }
        return hurt;
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_TRIPLE_BITE -> this.attackAnimationState.start(this.tickCount);
            case EVENT_SUBMERGE_CROUCH -> this.submergeAmbushAnimationState.start(this.tickCount);
            case EVENT_AMBUSH_ERUPT -> {
                // Start submerge_ambush at t = 1.25s (25 ticks in) for seamless upward eruption out of soul sand!
                this.submergeAmbushAnimationState.start(this.tickCount - 25);
            }
            case EVENT_WITHER_ROAR -> this.witherRoarAnimationState.start(this.tickCount);
            case EVENT_SPELL_CHARGE -> {
                this.spellReleaseAnimationState.stop();
                this.spellChargeAnimationState.start(this.tickCount);
            }
            case EVENT_SPELL_RELEASE -> {
                this.spellChargeAnimationState.stop();
                this.spellReleaseAnimationState.start(this.tickCount);
            }
            case EVENT_BREATH_ATTACK -> this.breathAttackAnimationState.start(this.tickCount);
            case EVENT_HURT_RECOIL -> this.hurtAnimationState.start(this.tickCount);
            default -> super.handleEntityEvent(id);
        }
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (!this.level().isClientSide() && this.deathTime == 1 && this.level() instanceof ServerLevel serverLevel) {
            this.setSubmergedAmbush(false);
            this.setChargingRun(false);
            this.setGlandGlowing(false);
            this.activeSoulfireBolts.clear();
            serverLevel.sendParticles(ParticleTypes.SOUL,
                    this.getX(), this.getY() + 1.2D, this.getZ(),
                    20, 0.9D, 0.5D, 0.9D, 0.04D);
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX(), this.getY() + 1.0D, this.getZ(),
                    24, 0.9D, 0.45D, 0.9D, 0.04D);
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

        // Peaceful soul tribute when unaggroed & sneaking: offering Soul Sand, Soul Soil, or Ghast Tear
        if (player.isCrouching() && (stack.is(Items.SOUL_SAND) || stack.is(Items.SOUL_SOIL) || stack.is(Items.GHAST_TEAR))) {
            if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.heal(6.0F);
                this.startWitherRoar(serverLevel);
                if (this.glandShedCooldown <= 0) {
                    this.glandShedCooldown = GLAND_SHED_COOLDOWN_TICKS;
                    Item glandItem = BuiltInRegistries.ITEM.getOptional(HYDRA_SOUL_GLAND_ID)
                            .orElse(ModItems.HYDRA_SOUL_GLAND.get());
                    this.spawnAtLocation(serverLevel, new ItemStack(glandItem));
                }
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    public static boolean checkSoulSandHydraSpawnRules(
            EntityType<SoulSandHydraEntity> type,
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
        boolean validFloor = below.is(Blocks.SOUL_SAND)
                || below.is(Blocks.SOUL_SOIL)
                || below.is(Blocks.BASALT)
                || below.is(Blocks.BLACKSTONE)
                || below.is(Blocks.NETHERRACK)
                || belowPath.contains("soul")
                || belowPath.contains("ash")
                || belowPath.contains("basalt");
        return validFloor && level.getBlockState(pos).getFluidState().isEmpty();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("SubmergedAmbush", Codec.BOOL, this.isSubmergedAmbush());
        output.store("GlandShedCooldown", Codec.INT, this.glandShedCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setSubmergedAmbush(input.read("SubmergedAmbush", Codec.BOOL).orElse(false));
        this.glandShedCooldown = input.read("GlandShedCooldown", Codec.INT).orElse(0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_SKELETON_AMBIENT;
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
    public float getVoicePitch() {
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.12F + 0.68F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.RAVAGER_STEP, 0.45F, 0.82F);
    }

    private static final class SoulfireBolt {
        Vec3 pos;
        final Vec3 vel;
        int ticksLeft;

        SoulfireBolt(Vec3 pos, Vec3 vel, int ticksLeft) {
            this.pos = pos;
            this.vel = vel;
            this.ticksLeft = ticksLeft;
        }
    }

    /**
     * Crouches low and submerges into soft Soul Sand / Soul Soil basins when unaggroed, waiting to erupt!
     */
    private static final class SoulSandAmbushHideGoal extends Goal {
        private final SoulSandHydraEntity hydra;
        private int hideDuration = 0;

        SoulSandAmbushHideGoal(SoulSandHydraEntity hydra) {
            this.hydra = hydra;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            if (this.hydra.getTarget() != null || this.hydra.isBusyWithAction()) {
                return false;
            }
            if (this.hydra.isSubmergedAmbush()) {
                return true;
            }
            if (this.hydra.submergeHideCooldown > 0 || !this.hydra.onGround()) {
                return false;
            }
            if (this.hydra.getRandom().nextInt(35) != 0) {
                return false;
            }
            BlockState below = this.hydra.level().getBlockState(this.hydra.blockPosition().below());
            BlockState atFeet = this.hydra.level().getBlockState(this.hydra.blockPosition());
            return isSoulSandBasinBlock(below) || isSoulSandBasinBlock(atFeet);
        }

        @Override
        public boolean canContinueToUse() {
            return this.hydra.isSubmergedAmbush()
                    && this.hydra.getTarget() == null
                    && this.hideDuration < 400;
        }

        @Override
        public void start() {
            this.hideDuration = 0;
            this.hydra.getNavigation().stop();
            this.hydra.setSubmergedAmbush(true);
            if (this.hydra.level() instanceof ServerLevel serverLevel) {
                BlockParticleOption sand = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SOUL_SAND.defaultBlockState());
                serverLevel.sendParticles(sand,
                        this.hydra.getX(), this.hydra.getY() + 0.25D, this.hydra.getZ(),
                        24, 1.1D, 0.25D, 1.1D, 0.08D);
                serverLevel.playSound(null, this.hydra.getX(), this.hydra.hydraBlockY(), this.hydra.getZ(),
                        SoundEvents.SOUL_SAND_FALL, SoundSource.HOSTILE, 0.9F, 0.75F);
            }
        }

        @Override
        public void tick() {
            this.hideDuration++;
            this.hydra.getNavigation().stop();
        }

        @Override
        public void stop() {
            if (this.hydra.isSubmergedAmbush() && this.hydra.getTarget() == null) {
                this.hydra.setSubmergedAmbush(false);
                this.hydra.submergeHideCooldown = 220;
            }
        }
    }

    private double hydraBlockY() {
        return this.getY();
    }

    /**
     * Full multi-headed combat controller coordinating stalking runs, cascading 10-damage triple Wither bites,
     * soul-sand ambush pounces, reared soulfire roars, sweeping breath cones, and 2-stage soulfire spell volleys.
     */
    private static final class HydraMultiHeadCombatGoal extends Goal {
        private final SoulSandHydraEntity hydra;
        private int repathTicks = 0;

        HydraMultiHeadCombatGoal(SoulSandHydraEntity hydra) {
            this.hydra = hydra;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.hydra.getTarget();
            return target != null && target.isAlive() && !this.hydra.isSubmergedAmbush();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.hydra.getTarget();
            if (target == null || !target.isAlive() || this.hydra.isSubmergedAmbush()) {
                return false;
            }
            if (target instanceof Player p && (p.isCreative() || p.isSpectator())) {
                return false;
            }
            return this.hydra.distanceToSqr(target) <= 1024.0D;
        }

        @Override
        public void stop() {
            this.hydra.getNavigation().stop();
            this.hydra.setChargingRun(false);
        }

        @Override
        public void tick() {
            LivingEntity target = this.hydra.getTarget();
            if (target == null || !(this.hydra.level() instanceof ServerLevel serverLevel)) {
                return;
            }

            this.hydra.getLookControl().setLookAt(target, 28.0F, 28.0F);
            if (this.hydra.isBusyWithAction()) {
                this.hydra.getNavigation().stop();
                return;
            }

            double distSqr = this.hydra.distanceToSqr(target);

            if (--this.repathTicks <= 0) {
                this.repathTicks = 4 + this.hydra.getRandom().nextInt(5);
                this.hydra.getNavigation().moveTo(target, 1.32D);
            }

            if (!this.hydra.hasLineOfSight(target) || this.hydra.combatCooldown > 0) {
                return;
            }

            // 1. Opening Reared Soulfire Roar when engaging prey
            if (this.hydra.roarCooldown <= 0 && distSqr <= 144.0D && this.hydra.getRandom().nextInt(4) == 0) {
                this.hydra.startWitherRoar(serverLevel);
                return;
            }

            // 2. Close Quarters (<= 4.1 blocks): Cascading Left-Right-Center 10-damage Wither Bite!
            if (distSqr <= 16.8D) {
                this.hydra.startCascadingTripleBite(serverLevel);
                return;
            }

            // 3. Mid-Range Specials when off cooldown
            if (this.hydra.specialCooldown <= 0) {
                BlockState below = this.hydra.level().getBlockState(this.hydra.blockPosition().below());
                if (distSqr <= 42.25D) {
                    // 4.1 - 6.5 blocks: Sweeping Triple-Maw Soulfire Breath Cone or Soul-Sand Ambush Pounce!
                    if (isSoulSandBasinBlock(below) && this.hydra.getRandom().nextBoolean()) {
                        this.hydra.startMidCombatSubmergePounce(serverLevel);
                    } else {
                        this.hydra.startSoulfireBreathSweep(serverLevel);
                    }
                    return;
                }
                if (distSqr <= 324.0D) {
                    // 6.5 - 18.0 blocks: Two-stage seamless Soulfire Spell Volley (spell_charge -> spell_release)!
                    this.hydra.startSoulfireSpellVolley(serverLevel);
                }
            }
        }
    }
}
