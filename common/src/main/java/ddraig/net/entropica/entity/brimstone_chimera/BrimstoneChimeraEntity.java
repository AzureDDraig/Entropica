package ddraig.net.entropica.entity.brimstone_chimera;

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
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
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

import java.util.EnumSet;

/**
 * Fauna 58 — Brimstone Chimera (Lava Beast) (entropica:brimstone_chimera).
 * Hostile mythical three-headed volcanic beast (central royal lion, shoulder volcanic goat,
 * and overhead 7-segment viper tail) native to 6.1 Magma Caverns & 6.4 Ash Canyons:
 * - Fast quadrupedal walker (walk) and bounding galloper (run) patrolling near lava lakes & ash canyons.
 * - Triple-Headed Claw, Bite & Viper Strike (attack): slashes with its foreclaws while its lion, goat,
 *   and overhead viper heads bite in tandem for 12.0 total fire and venom damage (4 + 4 + 4).
 * - Sweeping Twin-Maw Magma-Ember Breath (magma_breath): braces wide and sweeps both gaping molten
 *   lion_jaw_grp and goat_jaw_grp maws across a wide arc, emitting dynamic billowing magma-ember fire
 *   streams dealing 12.0 fire and venom damage total!
 * - Immune to fire and lava, recharging its magma breath in lava pools (and on magma blocks).
 * - Drops Chimera Venom-Gland (entropica:chimera_venom_gland).
 */
public class BrimstoneChimeraEntity extends Monster {

    private static final EntityDataAccessor<Boolean> GALLOPING_RUN =
            SynchedEntityData.defineId(BrimstoneChimeraEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> BREATHING_MAGMA =
            SynchedEntityData.defineId(BrimstoneChimeraEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> MAGMA_BREATH_CHARGES =
            SynchedEntityData.defineId(BrimstoneChimeraEntity.class, EntityDataSerializers.INT);

    private static final byte EVENT_TRIPLE_STRIKE = 4;
    private static final byte EVENT_MAGMA_BREATH = 61;
    private static final byte EVENT_HURT_SNARL = 62;

    private static final int TRIPLE_STRIKE_DURATION_TICKS = 32; // 1.60s attack
    private static final int MAGMA_BREATH_DURATION_TICKS = 36;  // 1.80s magma_breath
    private static final int DEATH_DURATION_TICKS = 34;         // 1.70s death hold
    private static final int MAX_BREATH_CHARGES = 3;
    private static final int GLAND_SHED_COOLDOWN_TICKS = 1200;

    private static final ResourceLocation CHIMERA_VENOM_GLAND_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "chimera_venom_gland");

    private static final DustParticleOptions MAGMA_EMBER_DUST = new DustParticleOptions(0xE85D12, 0.80F);
    private static final DustParticleOptions SULFUR_GOLD_DUST = new DustParticleOptions(0xFEE252, 0.68F);
    private static final DustParticleOptions ACID_VENOM_DUST = new DustParticleOptions(0x98AE64, 0.65F);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState runAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState magmaBreathAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    private int tripleStrikeTicks = 0;
    private int magmaBreathTicks = 0;
    private int combatCooldown = 0;
    private int breathCooldown = 40;
    private int glandShedCooldown = 0;
    private int clientBreathTicks = 0;

    public BrimstoneChimeraEntity(EntityType<? extends BrimstoneChimeraEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 20;
        this.setPathfindingMalus(PathType.LAVA, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
        this.setPathfindingMalus(PathType.WATER, 8.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 135.0D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.50D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.FOLLOW_RANGE, 30.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(GALLOPING_RUN, false);
        builder.define(BREATHING_MAGMA, false);
        builder.define(MAGMA_BREATH_CHARGES, MAX_BREATH_CHARGES);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new ChimeraCombatGoal(this));
        this.goalSelector.addGoal(2, new ChimeraLavaPoolRechargeGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.90D) {
            @Override
            public boolean canUse() {
                return !BrimstoneChimeraEntity.this.isBusyAttacking() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !BrimstoneChimeraEntity.this.isBusyAttacking() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    public boolean isGallopingRun() {
        return this.entityData.get(GALLOPING_RUN);
    }

    public void setGallopingRun(boolean running) {
        this.entityData.set(GALLOPING_RUN, running);
    }

    public boolean isBreathingMagma() {
        return this.entityData.get(BREATHING_MAGMA);
    }

    private void setBreathingMagma(boolean breathing) {
        this.entityData.set(BREATHING_MAGMA, breathing);
    }

    public int getMagmaBreathCharges() {
        return this.entityData.get(MAGMA_BREATH_CHARGES);
    }

    private void setMagmaBreathCharges(int charges) {
        this.entityData.set(MAGMA_BREATH_CHARGES, Mth.clamp(charges, 0, MAX_BREATH_CHARGES));
    }

    public boolean isBusyAttacking() {
        return this.tripleStrikeTicks > 0 || this.magmaBreathTicks > 0;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effectInstance) {
        if (effectInstance.is(MobEffects.POISON) || effectInstance.is(MobEffects.WITHER)) {
            return false;
        }
        return super.canBeAffected(effectInstance);
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        if (source.is(DamageTypeTags.IS_FIRE)
                || source.is(DamageTypes.LAVA)
                || source.is(DamageTypes.HOT_FLOOR)
                || source.is(DamageTypes.IN_FIRE)
                || source.is(DamageTypes.ON_FIRE)) {
            this.rechargeMagmaBreath(level, false);
            return true;
        }
        return super.isInvulnerableTo(level, source);
    }

    public boolean isInOrOnLavaMagma() {
        if (this.isInLava() || this.isOnFire()) {
            return true;
        }
        BlockState below = this.level().getBlockState(this.blockPosition().below());
        BlockState feet = this.level().getBlockState(this.blockPosition());
        return isLavaOrMagmaBlock(below) || isLavaOrMagmaBlock(feet);
    }

    public static boolean isLavaOrMagmaBlock(BlockState state) {
        if (state.isAir()) {
            return false;
        }
        if (state.is(Blocks.LAVA) || state.is(Blocks.MAGMA_BLOCK)) {
            return true;
        }
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return path.contains("magma") || path.contains("lava") || path.contains("brimstone");
    }

    public void rechargeMagmaBreath(ServerLevel serverLevel, boolean loud) {
        boolean needed = this.getMagmaBreathCharges() < MAX_BREATH_CHARGES;
        this.setMagmaBreathCharges(MAX_BREATH_CHARGES);
        if ((needed || loud) && serverLevel != null) {
            serverLevel.playSound(null, this.getX(), this.getY() + 1.0D, this.getZ(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 0.80F, 0.90F);
            serverLevel.sendParticles(ParticleTypes.LAVA,
                    this.getX(), this.getY() + 1.1D, this.getZ(),
                    8, 0.55D, 0.35D, 0.55D, 0.03D);
            serverLevel.sendParticles(MAGMA_EMBER_DUST,
                    this.getX(), this.getY() + 1.1D, this.getZ(),
                    12, 0.55D, 0.35D, 0.55D, 0.03D);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            this.updateClientAnimations();
            this.spawnClientDualJawBreathAndEmbers();
            return;
        }

        if (!this.isAlive()) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();

        if (this.combatCooldown > 0) {
            this.combatCooldown--;
        }
        if (this.breathCooldown > 0) {
            this.breathCooldown--;
        }
        if (this.glandShedCooldown > 0) {
            this.glandShedCooldown--;
        }

        // Buoyant wading & magma-breath recharge in lava pools!
        if (this.isInLava()) {
            Vec3 vel = this.getDeltaMovement();
            this.setDeltaMovement(vel.x * 1.06D, Math.max(vel.y, 0.07D), vel.z * 1.06D);
        }
        if (this.isInOrOnLavaMagma()) {
            this.rechargeMagmaBreath(serverLevel, false);
            if (this.tickCount % 40 == 0 && this.getHealth() < this.getMaxHealth()) {
                this.heal(1.0F);
            }
        }

        this.tickTripleStrike(serverLevel);
        this.tickMagmaBreath(serverLevel);
        this.setBreathingMagma(this.magmaBreathTicks > 0);
    }

    private void updateClientAnimations() {
        if (this.isDeadOrDying()) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.runAnimationState.stop();
            this.attackAnimationState.stop();
            this.magmaBreathAnimationState.stop();
            this.hurtAnimationState.stop();
            if (!this.deathAnimationState.isStarted()) {
                this.deathAnimationState.start(this.tickCount);
            }
            return;
        }

        if (this.clientBreathTicks > 0) {
            this.clientBreathTicks--;
        }

        double hSpeedSqr = this.getDeltaMovement().horizontalDistanceSqr();
        boolean moving = hSpeedSqr > 4.0E-4D || this.walkAnimation.speed() > 0.07F;
        if (moving) {
            this.idleAnimationState.stop();
            if (this.isGallopingRun()) {
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

    /**
     * Dynamic particle emitters attached to {@code lion_jaw_grp} (center maw) and {@code goat_jaw_grp}
     * (shoulder maw) so billowing magma-ember breath streams sweep directly from both jaws!
     */
    private void spawnClientDualJawBreathAndEmbers() {
        if (this.isDeadOrDying()) {
            return;
        }

        float bodyYawRad = this.yBodyRot * Mth.DEG_TO_RAD;
        Vec3 forward = new Vec3(-Mth.sin(bodyYawRad), 0.0D, Mth.cos(bodyYawRad));
        Vec3 right = new Vec3(-Mth.cos(bodyYawRad), 0.0D, -Mth.sin(bodyYawRad));

        // Model offsets: lion_jaw_grp at center front (~1.05 fwd, Y~1.26); goat_jaw_grp on +X shoulder (~0.32 side, ~0.88 fwd, Y~1.48)
        Vec3 lionJaw = this.position().add(forward.scale(1.05D)).add(0.0D, 1.24D, 0.0D);
        Vec3 goatJaw = this.position().add(forward.scale(0.88D)).subtract(right.scale(0.32D)).add(0.0D, 1.46D, 0.0D);

        if (this.isBreathingMagma() && this.clientBreathTicks <= 24 && this.clientBreathTicks >= 8) {
            float progress = (24 - this.clientBreathTicks) / 16.0F; // 0.0 (left) -> 1.0 (right)
            float sweepDeg = Mth.lerp(progress, 24.0F, -24.0F);
            float streamYawRad = (this.yBodyRot - sweepDeg) * Mth.DEG_TO_RAD;
            Vec3 streamDir = new Vec3(-Mth.sin(streamYawRad), -0.06D, Mth.cos(streamYawRad)).normalize();

            for (int step = 1; step <= 5; step++) {
                double dist = step * 0.85D;
                Vec3 lPt = lionJaw.add(streamDir.scale(dist));
                Vec3 gPt = goatJaw.add(streamDir.scale(dist));
                this.level().addParticle(ParticleTypes.FLAME,
                        lPt.x + (this.random.nextDouble() - 0.5D) * 0.18D,
                        lPt.y + (this.random.nextDouble() - 0.5D) * 0.14D,
                        lPt.z + (this.random.nextDouble() - 0.5D) * 0.18D,
                        streamDir.x * 0.16D, streamDir.y * 0.05D, streamDir.z * 0.16D);
                this.level().addParticle(ParticleTypes.FLAME,
                        gPt.x + (this.random.nextDouble() - 0.5D) * 0.18D,
                        gPt.y + (this.random.nextDouble() - 0.5D) * 0.14D,
                        gPt.z + (this.random.nextDouble() - 0.5D) * 0.18D,
                        streamDir.x * 0.16D, streamDir.y * 0.05D, streamDir.z * 0.16D);
                if (step % 2 == 0) {
                    this.level().addParticle(MAGMA_EMBER_DUST, lPt.x, lPt.y, lPt.z, 0.0D, 0.01D, 0.0D);
                    this.level().addParticle(SULFUR_GOLD_DUST, gPt.x, gPt.y, gPt.z, 0.0D, 0.01D, 0.0D);
                }
            }
            return;
        }

        // Ambient furnace-gullet embers from lion_jaw_grp & goat_jaw_grp when charged
        if (this.getMagmaBreathCharges() > 0 && this.random.nextInt(5) == 0) {
            Vec3 jaw = this.random.nextBoolean() ? lionJaw : goatJaw;
            this.level().addParticle(ParticleTypes.SMALL_FLAME, jaw.x, jaw.y, jaw.z, 0.0D, 0.01D, 0.0D);
        }
    }

    /**
     * Triple-Headed Claw, Lion/Goat Bite & Overhead Viper Strike (1.60s = 32 ticks):
     * - t = 0.62s (remaining == 20): Foreclaw slash (4.0 physical damage)
     * - t = 0.80s (remaining == 16): Lion + Goat molten bite (4.0 fire damage + ignition) +
     *   Viper tail strike (4.0 venom damage + Poison + Wither) = 12.0 total damage!
     */
    public void startTripleStrike(ServerLevel serverLevel) {
        if (this.isBusyAttacking()) {
            return;
        }
        this.tripleStrikeTicks = TRIPLE_STRIKE_DURATION_TICKS;
        this.combatCooldown = 36;
        this.setGallopingRun(false);
        this.getNavigation().stop();
        serverLevel.broadcastEntityEvent(this, EVENT_TRIPLE_STRIKE);
        serverLevel.playSound(null, this.getX(), this.getY() + 1.2D, this.getZ(),
                SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 0.75F, 1.18F);
    }

    private void tickTripleStrike(ServerLevel serverLevel) {
        if (this.tripleStrikeTicks <= 0) {
            return;
        }
        this.tripleStrikeTicks--;

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        // 1. Foreclaw slash at t = 0.62s (remaining == 20): 4.0 damage
        if (this.tripleStrikeTicks == 20 && target != null && target.isAlive() && this.distanceToSqr(target) <= 14.5D) {
            target.hurtServer(serverLevel, this.damageSources().mobAttack(this), 4.0F);
            serverLevel.playSound(null, target.getX(), target.getY() + 0.9D, target.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 0.90F, 0.78F);
        }

        // 2. Lion + Goat molten bite (4.0 fire) + Overhead Viper strike (4.0 venom) at t = 0.80s (remaining == 16)
        if (this.tripleStrikeTicks == 16 && target != null && target.isAlive() && this.distanceToSqr(target) <= 16.0D) {
            target.invulnerableTime = 0;
            if (target.hurtServer(serverLevel, this.damageSources().mobAttack(this), 8.0F)) {
                target.igniteForSeconds(5.0F);
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0), this);
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0), this);
                serverLevel.sendParticles(MAGMA_EMBER_DUST,
                        target.getX(), target.getY() + 1.0D, target.getZ(),
                        12, 0.30D, 0.30D, 0.30D, 0.03D);
                serverLevel.sendParticles(ACID_VENOM_DUST,
                        target.getX(), target.getY() + 1.1D, target.getZ(),
                        12, 0.30D, 0.30D, 0.30D, 0.03D);
            }
        }
    }

    /**
     * Sweeping Twin-Maw Magma-Ember Breath (1.80s = 36 ticks; sweep from t = 0.62s -> 1.34s):
     * Unleashes sweeping torrents of magma-ember fire from both lion_jaw_grp and goat_jaw_grp
     * dealing 12.0 total fire and venom damage (3 pulses x 4.0)!
     */
    public void startMagmaBreath(ServerLevel serverLevel) {
        if (this.isBusyAttacking() || this.getMagmaBreathCharges() <= 0) {
            return;
        }
        this.setMagmaBreathCharges(this.getMagmaBreathCharges() - 1);
        this.magmaBreathTicks = MAGMA_BREATH_DURATION_TICKS;
        this.breathCooldown = 85;
        this.combatCooldown = 42;
        this.setGallopingRun(false);
        this.setBreathingMagma(true);
        this.getNavigation().stop();
        serverLevel.broadcastEntityEvent(this, EVENT_MAGMA_BREATH);
        serverLevel.playSound(null, this.getX(), this.getY() + 1.3D, this.getZ(),
                SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.05F, 0.72F);
    }

    private void tickMagmaBreath(ServerLevel serverLevel) {
        if (this.magmaBreathTicks <= 0) {
            return;
        }
        this.magmaBreathTicks--;

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && this.magmaBreathTicks > 24) {
            this.getLookControl().setLookAt(target, 28.0F, 28.0F);
        }

        // Sweep damage & server ember cone at remaining == 22, 16, 10 (3 x 4.0 = 12.0 total!)
        if (this.magmaBreathTicks == 22 || this.magmaBreathTicks == 16 || this.magmaBreathTicks == 10) {
            float progress = (24 - this.magmaBreathTicks) / 16.0F;
            float sweepDeg = Mth.lerp(progress, 24.0F, -24.0F);
            float yawRad = (this.getYRot() - sweepDeg) * Mth.DEG_TO_RAD;
            Vec3 dir = new Vec3(-Mth.sin(yawRad), -0.05D, Mth.cos(yawRad)).normalize();
            Vec3 mawOrigin = new Vec3(this.getX() + dir.x * 0.95D, this.getY() + 1.32D, this.getZ() + dir.z * 0.95D);

            serverLevel.playSound(null, mawOrigin.x, mawOrigin.y, mawOrigin.z,
                    SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 0.85F, 0.85F);
            for (int step = 1; step <= 6; step++) {
                Vec3 pt = mawOrigin.add(dir.scale(step * 0.95D));
                double spread = 0.18D + step * 0.12D;
                serverLevel.sendParticles(ParticleTypes.FLAME, pt.x, pt.y, pt.z, 4, spread, spread * 0.6D, spread, 0.02D);
                serverLevel.sendParticles(MAGMA_EMBER_DUST, pt.x, pt.y, pt.z, 3, spread, spread * 0.6D, spread, 0.01D);
                serverLevel.sendParticles(ACID_VENOM_DUST, pt.x, pt.y, pt.z, 2, spread * 0.8D, spread * 0.5D, spread * 0.8D, 0.01D);
            }

            for (LivingEntity victim : serverLevel.getEntitiesOfClass(
                    LivingEntity.class,
                    this.getBoundingBox().inflate(6.5D, 2.5D, 6.5D),
                    e -> e.isAlive() && e != this && !(e instanceof BrimstoneChimeraEntity))) {
                if (victim instanceof Player p && (p.isCreative() || p.isSpectator())) {
                    continue;
                }
                Vec3 toVictim = victim.getEyePosition().subtract(mawOrigin);
                double dist = toVictim.length();
                if (dist <= 6.5D && dist > 0.05D && toVictim.normalize().dot(dir) >= 0.62D) {
                    victim.invulnerableTime = 0;
                    if (victim.hurtServer(serverLevel, this.damageSources().mobAttack(this), 4.0F)) {
                        victim.igniteForSeconds(5.0F);
                        victim.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0), this);
                    }
                }
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(serverLevel, source, amount);
        if (hurt && this.isAlive() && !this.isBusyAttacking()) {
            serverLevel.broadcastEntityEvent(this, EVENT_HURT_SNARL);
        }
        return hurt;
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_TRIPLE_STRIKE -> this.attackAnimationState.start(this.tickCount);
            case EVENT_MAGMA_BREATH -> {
                this.clientBreathTicks = MAGMA_BREATH_DURATION_TICKS;
                this.magmaBreathAnimationState.start(this.tickCount);
            }
            case EVENT_HURT_SNARL -> this.hurtAnimationState.start(this.tickCount);
            default -> super.handleEntityEvent(id);
        }
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (this.deathTime == 1 && !this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            this.setGallopingRun(false);
            this.setBreathingMagma(false);
            serverLevel.sendParticles(MAGMA_EMBER_DUST,
                    this.getX(), this.getY() + 0.9D, this.getZ(),
                    16, 0.55D, 0.35D, 0.55D, 0.03D);
            serverLevel.sendParticles(ACID_VENOM_DUST,
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
        boolean isOffering = held.is(Items.MAGMA_CREAM) || held.is(Items.SPIDER_EYE) || held.is(Items.MUTTON) || held.is(Items.BEEF);
        if (player.isShiftKeyDown() && isOffering && this.getTarget() == null && this.isAlive()) {
            if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                this.heal(8.0F);
                this.rechargeMagmaBreath(serverLevel, true);
                if (this.glandShedCooldown <= 0) {
                    this.glandShedCooldown = GLAND_SHED_COOLDOWN_TICKS;
                    Item gland = BuiltInRegistries.ITEM.getOptional(CHIMERA_VENOM_GLAND_ID).orElse(Items.FERMENTED_SPIDER_EYE);
                    this.spawnAtLocation(serverLevel, new ItemStack(gland));
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    public static boolean isChimeraSpawnSurface(BlockGetter level, BlockPos belowPos, BlockState belowState) {
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
                || path.contains("basalt")
                || path.contains("magma")
                || path.contains("ash")
                || belowState.isFaceSturdy(level, belowPos, Direction.UP);
    }

    public static boolean checkBrimstoneChimeraSpawnRules(
            EntityType<? extends BrimstoneChimeraEntity> type,
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
        BlockPos below = pos.below();
        return isChimeraSpawnSurface(level, below, level.getBlockState(below));
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        BlockPos below = this.blockPosition().below();
        return isChimeraSpawnSurface(level, below, level.getBlockState(below))
                && super.checkSpawnRules(level, spawnReason);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("MagmaBreathCharges", Codec.INT, this.getMagmaBreathCharges());
        output.store("GlandShedCooldown", Codec.INT, this.glandShedCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setMagmaBreathCharges(input.read("MagmaBreathCharges", Codec.INT).orElse(MAX_BREATH_CHARGES));
        this.glandShedCooldown = input.read("GlandShedCooldown", Codec.INT).orElse(0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.RAVAGER_AMBIENT;
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
        return 0.78F;
    }

    @Override
    public float getVoicePitch() {
        return 0.92F + this.random.nextFloat() * 0.16F;
    }

    // =========================================================================
    // AI GOALS: Bounding Gallop Combat & Lava-Pool Magma Breath Recharging
    // =========================================================================

    private static final class ChimeraCombatGoal extends Goal {
        private final BrimstoneChimeraEntity chimera;

        ChimeraCombatGoal(BrimstoneChimeraEntity chimera) {
            this.chimera = chimera;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.chimera.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.chimera.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public void stop() {
            this.chimera.setGallopingRun(false);
        }

        @Override
        public void tick() {
            LivingEntity target = this.chimera.getTarget();
            if (target == null || !(this.chimera.level() instanceof ServerLevel serverLevel)) {
                return;
            }

            this.chimera.getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (this.chimera.isBusyAttacking()) {
                return;
            }

            // If magma breath charges are completely spent, briefly bound into nearby lava/magma to recharge!
            if (this.chimera.getMagmaBreathCharges() == 0 && this.chimera.breathCooldown <= 0) {
                BlockPos lavaPos = findNearbyLavaOrMagma(serverLevel, this.chimera.blockPosition(), 9);
                if (lavaPos != null) {
                    this.chimera.setGallopingRun(true);
                    this.chimera.getNavigation().moveTo(
                            lavaPos.getX() + 0.5D, lavaPos.getY() + 0.5D, lavaPos.getZ() + 0.5D, 1.35D);
                    return;
                }
            }

            double distSq = this.chimera.distanceToSqr(target);

            // Sweeping Twin-Maw Magma-Ember Breath (3.0 - 6.2 blocks)
            if (distSq >= 9.0D && distSq <= 38.5D
                    && this.chimera.getMagmaBreathCharges() > 0
                    && this.chimera.breathCooldown <= 0
                    && this.chimera.combatCooldown <= 0
                    && this.chimera.hasLineOfSight(target)) {
                this.chimera.startMagmaBreath(serverLevel);
                return;
            }

            // Triple-Headed Claw, Lion/Goat Bite & Viper Strike (close melee)
            if (distSq <= 12.25D && this.chimera.combatCooldown <= 0) {
                this.chimera.startTripleStrike(serverLevel);
                return;
            }

            boolean gallop = distSq > 25.0D;
            this.chimera.setGallopingRun(gallop);
            this.chimera.getNavigation().moveTo(target, gallop ? 1.36D : 1.05D);
        }
    }

    private static final class ChimeraLavaPoolRechargeGoal extends Goal {
        private final BrimstoneChimeraEntity chimera;
        @Nullable
        private BlockPos lavaPos;

        ChimeraLavaPoolRechargeGoal(BrimstoneChimeraEntity chimera) {
            this.chimera = chimera;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.chimera.getTarget() != null || this.chimera.isBusyAttacking()) {
                return false;
            }
            if (this.chimera.getMagmaBreathCharges() >= MAX_BREATH_CHARGES && this.chimera.random.nextInt(90) != 0) {
                return false;
            }
            this.lavaPos = findNearbyLavaOrMagma(this.chimera.level(), this.chimera.blockPosition(), 10);
            return this.lavaPos != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.chimera.getTarget() == null
                    && this.lavaPos != null
                    && !this.chimera.getNavigation().isDone();
        }

        @Override
        public void start() {
            if (this.lavaPos != null) {
                this.chimera.getNavigation().moveTo(
                        this.lavaPos.getX() + 0.5D,
                        this.lavaPos.getY() + 0.5D,
                        this.lavaPos.getZ() + 0.5D,
                        1.0D
                );
            }
        }
    }

    @Nullable
    static BlockPos findNearbyLavaOrMagma(Level level, BlockPos origin, int radius) {
        BlockPos best = null;
        double bestDistSq = Double.MAX_VALUE;
        for (BlockPos check : BlockPos.betweenClosed(
                origin.offset(-radius, -3, -radius),
                origin.offset(radius, 2, radius))) {
            if (isLavaOrMagmaBlock(level.getBlockState(check))) {
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
