package ddraig.net.entropica.entity.barrow_wight_golem;

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
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
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
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

/**
 * Fauna 52 — Barrow Wight Golem (Crypt Sentinel) (entropica:barrow_wight_golem).
 * Hostile towering crypt-stone sentinel native to 6.3 Soulfire Fjords & 6.4 Ash Canyons:
 * - Powered by a glowing cyan-blue soul-stone core inside its hollow crypt-stone rib vault.
 * - Crypt-Stone Block Disguise (dormant_enter -> dormant -> awaken): folds its head, limbs, and
 *   runic column arms tightly inward to stand dormant as a compact crypt-stone block until a player
 *   enters its 5-block range!
 * - Crushing Double-Fist Stone Column Slam (attack): overhead windup into a 12-damage slam + heavy knockback.
 * - Open-Chest Soulfire Shockwave Burst (soulfire_burst): compresses chest core then flings stone arms
 *   wide to unleash a 10-damage soulfire wave.
 * - Core Extinction & Collapse (death): core extinguishes as the stone sentinel collapses flat onto Y = 0.
 */
public class BarrowWightGolemEntity extends Monster {

    public static final int STATE_DORMANT = 0;
    public static final int STATE_AWAKENING = 1;
    public static final int STATE_ACTIVE = 2;
    public static final int STATE_ENTERING_DORMANT = 3;

    private static final EntityDataAccessor<Integer> SENTINEL_STATE =
            SynchedEntityData.defineId(BarrowWightGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> BURSTING_SOULFIRE =
            SynchedEntityData.defineId(BarrowWightGolemEntity.class, EntityDataSerializers.BOOLEAN);

    private static final byte EVENT_COLUMN_SLAM = 4;
    private static final byte EVENT_SOULFIRE_BURST = 61;
    private static final byte EVENT_AWAKEN = 62;
    private static final byte EVENT_DORMANT_ENTER = 63;
    private static final byte EVENT_HURT_FLINCH = 64;

    private static final int COLUMN_SLAM_DURATION_TICKS = 36;    // 1.80s attack
    private static final int SOULFIRE_BURST_DURATION_TICKS = 40; // 2.00s soulfire_burst
    private static final int DORMANT_ENTER_DURATION_TICKS = 24;  // 1.20s dormant_enter
    private static final int AWAKEN_DURATION_TICKS = 28;         // 1.40s awaken
    private static final int DEATH_DURATION_TICKS = 36;          // 1.80s death hold
    private static final int CALM_RETURN_TO_DORMANT_TICKS = 240; // 12.0s idle before folding back
    private static final int STONE_SHED_COOLDOWN_TICKS = 1200;

    private static final ResourceLocation RUNIC_CRYPT_STONE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "runic_crypt_stone");

    private static final DustParticleOptions SOUL_STONE_CYAN_DUST = new DustParticleOptions(0x4EC2F2, 0.80F);
    private static final DustParticleOptions SOUL_CORE_WHITE_DUST = new DustParticleOptions(0xEAFAFF, 0.65F);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState soulfireBurstAnimationState = new AnimationState();
    public final AnimationState dormantEnterAnimationState = new AnimationState();
    public final AnimationState dormantAnimationState = new AnimationState();
    public final AnimationState awakenAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    private int transitionTicks = 0;
    private int columnSlamTicks = 0;
    private int soulfireBurstTicks = 0;
    private int combatCooldown = 0;
    private int burstCooldown = 60;
    private int calmIdleTicks = 0;
    private int stoneShedCooldown = 0;

    public BarrowWightGolemEntity(EntityType<? extends BarrowWightGolemEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 20;
        this.setPathfindingMalus(PathType.LAVA, 0.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 120.0D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D)
                .add(Attributes.MOVEMENT_SPEED, 0.21D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SENTINEL_STATE, STATE_DORMANT);
        builder.define(BURSTING_SOULFIRE, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new BarrowWightCombatGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.85D) {
            @Override
            public boolean canUse() {
                return BarrowWightGolemEntity.this.getSentinelState() == STATE_ACTIVE
                        && !BarrowWightGolemEntity.this.isPerformingAttack()
                        && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return BarrowWightGolemEntity.this.getSentinelState() == STATE_ACTIVE
                        && !BarrowWightGolemEntity.this.isPerformingAttack()
                        && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F) {
            @Override
            public boolean canUse() {
                return BarrowWightGolemEntity.this.getSentinelState() == STATE_ACTIVE && super.canUse();
            }
        });
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this) {
            @Override
            public boolean canUse() {
                return BarrowWightGolemEntity.this.getSentinelState() == STATE_ACTIVE && super.canUse();
            }
        });

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
                    // When folded as a dormant crypt-stone block, stands completely still until a player enters its 5-block range!
                    if (this.getSentinelState() == STATE_DORMANT || this.getSentinelState() == STATE_ENTERING_DORMANT) {
                        return this.distanceToSqr(player) <= 25.0D;
                    }
                    return true;
                }
        ));
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason spawnReason,
            @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
        this.setSentinelState(STATE_DORMANT);
        return data;
    }

    public int getSentinelState() {
        return this.entityData.get(SENTINEL_STATE);
    }

    public void setSentinelState(int state) {
        this.entityData.set(SENTINEL_STATE, state);
    }

    public boolean isBurstingSoulfire() {
        return this.entityData.get(BURSTING_SOULFIRE);
    }

    private void setBurstingSoulfire(boolean value) {
        this.entityData.set(BURSTING_SOULFIRE, value);
    }

    public boolean isPerformingAttack() {
        return this.columnSlamTicks > 0 || this.soulfireBurstTicks > 0;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    protected int decreaseAirSupply(int air) {
        return air;
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effectInstance) {
        if (effectInstance.is(MobEffects.WITHER) || effectInstance.is(MobEffects.POISON)) {
            return false;
        }
        return super.canBeAffected(effectInstance);
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        if (source.is(DamageTypeTags.IS_FIRE)
                || source.is(DamageTypeTags.IS_FALL)
                || source.is(DamageTypes.HOT_FLOOR)
                || source.is(DamageTypes.LAVA)
                || source.is(DamageTypes.DROWN)
                || source.is(DamageTypes.WITHER)
                || source.is(DamageTypes.CACTUS)) {
            return true;
        }
        return super.isInvulnerableTo(level, source);
    }

    @Override
    public boolean isPushable() {
        return this.getSentinelState() == STATE_ACTIVE && super.isPushable();
    }

    @Override
    public void knockback(double strength, double x, double z) {
        if (this.getSentinelState() == STATE_DORMANT || this.getSentinelState() == STATE_ENTERING_DORMANT) {
            return;
        }
        super.knockback(strength, x, z);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            this.updateClientAnimations();
            this.spawnClientCoreParticles();
            return;
        }

        if (!this.isAlive()) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();

        if (this.combatCooldown > 0) {
            this.combatCooldown--;
        }
        if (this.burstCooldown > 0) {
            this.burstCooldown--;
        }
        if (this.stoneShedCooldown > 0) {
            this.stoneShedCooldown--;
        }

        int state = this.getSentinelState();

        // 1. Dormant Crypt-Stone Block Watch: awakens the instant a survival/adventure player enters its 5-block range!
        if (state == STATE_DORMANT) {
            this.getNavigation().stop();
            this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
            Player intruder = this.findPlayerWithinFiveBlocks(serverLevel);
            if (intruder != null) {
                this.setTarget(intruder);
                this.triggerAwaken(serverLevel);
            } else if (this.getTarget() != null && this.getTarget().isAlive()) {
                this.triggerAwaken(serverLevel);
            }
            return;
        }

        // 2. Unsealing Awaken Transition (1.40s = 28 ticks)
        if (state == STATE_AWAKENING) {
            this.getNavigation().stop();
            if (--this.transitionTicks <= 0) {
                this.setSentinelState(STATE_ACTIVE);
                this.calmIdleTicks = 0;
            }
            return;
        }

        // 3. Telescoping Fold Back into Dormant Crypt-Stone Block (1.20s = 24 ticks)
        if (state == STATE_ENTERING_DORMANT) {
            this.getNavigation().stop();
            if (this.findPlayerWithinFiveBlocks(serverLevel) != null || (this.getTarget() != null && this.getTarget().isAlive())) {
                this.triggerAwaken(serverLevel);
                return;
            }
            if (--this.transitionTicks <= 0) {
                this.setSentinelState(STATE_DORMANT);
            }
            return;
        }

        // 4. Active Standing Crypt Sentinel
        this.tickColumnSlam(serverLevel);
        this.tickSoulfireBurst(serverLevel);
        this.setBurstingSoulfire(this.soulfireBurstTicks > 0);

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            List<Player> nearbyPlayers = serverLevel.getEntitiesOfClass(
                    Player.class,
                    this.getBoundingBox().inflate(9.0D, 5.0D, 9.0D),
                    p -> p.isAlive() && !p.isSpectator()
            );
            if (nearbyPlayers.isEmpty() && !this.isPerformingAttack()) {
                this.calmIdleTicks++;
                if (this.calmIdleTicks >= CALM_RETURN_TO_DORMANT_TICKS) {
                    this.triggerEnterDormant(serverLevel);
                }
            } else {
                this.calmIdleTicks = 0;
            }
        } else {
            this.calmIdleTicks = 0;
        }
    }

    private @Nullable Player findPlayerWithinFiveBlocks(ServerLevel serverLevel) {
        List<Player> nearby = serverLevel.getEntitiesOfClass(
                Player.class,
                this.getBoundingBox().inflate(5.0D, 3.5D, 5.0D),
                p -> p.isAlive()
                        && !p.isCreative()
                        && !p.isSpectator()
                        && this.distanceToSqr(p) <= 25.0D
        );
        return nearby.isEmpty() ? null : nearby.get(0);
    }

    public void triggerAwaken(ServerLevel serverLevel) {
        if (this.getSentinelState() == STATE_AWAKENING || this.getSentinelState() == STATE_ACTIVE) {
            return;
        }
        this.setSentinelState(STATE_AWAKENING);
        this.transitionTicks = AWAKEN_DURATION_TICKS;
        this.calmIdleTicks = 0;
        serverLevel.broadcastEntityEvent(this, EVENT_AWAKEN);
        serverLevel.playSound(null, this.getX(), this.getY() + 1.0D, this.getZ(),
                SoundEvents.IRON_GOLEM_REPAIR, SoundSource.HOSTILE, 0.95F, 0.62F);
        serverLevel.playSound(null, this.getX(), this.getY() + 1.2D, this.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 0.85F, 1.25F);
        serverLevel.sendParticles(SOUL_STONE_CYAN_DUST,
                this.getX(), this.getY() + 1.15D, this.getZ(),
                22, 0.55D, 0.55D, 0.55D, 0.03D);
    }

    public void triggerEnterDormant(ServerLevel serverLevel) {
        if (this.getSentinelState() != STATE_ACTIVE) {
            return;
        }
        this.setSentinelState(STATE_ENTERING_DORMANT);
        this.transitionTicks = DORMANT_ENTER_DURATION_TICKS;
        this.calmIdleTicks = 0;
        this.getNavigation().stop();
        serverLevel.broadcastEntityEvent(this, EVENT_DORMANT_ENTER);
        serverLevel.playSound(null, this.getX(), this.getY() + 0.8D, this.getZ(),
                SoundEvents.CHISELED_BOOKSHELF_INSERT_ENCHANTED, SoundSource.HOSTILE, 1.0F, 0.65F);
        serverLevel.playSound(null, this.getX(), this.getY() + 0.6D, this.getZ(),
                SoundEvents.DEEPSLATE_BRICKS_PLACE, SoundSource.HOSTILE, 0.95F, 0.72F);
    }

    private void updateClientAnimations() {
        if (this.isDeadOrDying()) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.attackAnimationState.stop();
            this.soulfireBurstAnimationState.stop();
            this.dormantEnterAnimationState.stop();
            this.dormantAnimationState.stop();
            this.awakenAnimationState.stop();
            this.hurtAnimationState.stop();
            if (!this.deathAnimationState.isStarted()) {
                this.deathAnimationState.start(this.tickCount);
            }
            return;
        }

        int state = this.getSentinelState();
        if (state == STATE_DORMANT) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.awakenAnimationState.stop();
            this.dormantEnterAnimationState.stop();
            this.dormantAnimationState.startIfStopped(this.tickCount);
            return;
        }

        if (state == STATE_AWAKENING || state == STATE_ENTERING_DORMANT) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.dormantAnimationState.stop();
            return;
        }

        this.dormantAnimationState.stop();
        this.dormantEnterAnimationState.stop();

        double hSpeedSqr = this.getDeltaMovement().horizontalDistanceSqr();
        if (hSpeedSqr > 3.5E-4D || this.walkAnimation.speed() > 0.06F) {
            this.walkAnimationState.startIfStopped(this.tickCount);
            this.idleAnimationState.stop();
        } else {
            this.idleAnimationState.startIfStopped(this.tickCount);
            this.walkAnimationState.stop();
        }
    }

    private void spawnClientCoreParticles() {
        if (this.isDeadOrDying()) {
            return;
        }
        int state = this.getSentinelState();
        if (state == STATE_DORMANT) {
            if (this.random.nextInt(14) == 0) {
                this.level().addParticle(SOUL_STONE_CYAN_DUST,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.65D,
                        this.getY() + 0.55D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.65D,
                        0.0D, 0.01D, 0.0D);
            }
            return;
        }

        if (this.random.nextInt(4) == 0) {
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.45D,
                    this.getY() + 1.32D + (this.random.nextDouble() - 0.5D) * 0.25D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.45D,
                    0.0D, 0.012D, 0.0D);
        }
    }

    /**
     * Crushing Double-Fist Stone Column Slam (1.80s = 36 ticks; impact at t = 0.90s -> remaining == 18):
     * deals 12 damage and heavy knockback!
     */
    public void startColumnSlam(ServerLevel serverLevel) {
        if (this.isPerformingAttack() || this.getSentinelState() != STATE_ACTIVE) {
            return;
        }
        this.columnSlamTicks = COLUMN_SLAM_DURATION_TICKS;
        this.combatCooldown = 44;
        this.getNavigation().stop();
        serverLevel.broadcastEntityEvent(this, EVENT_COLUMN_SLAM);
        serverLevel.playSound(null, this.getX(), this.getY() + 1.2D, this.getZ(),
                SoundEvents.IRON_GOLEM_ATTACK, SoundSource.HOSTILE, 1.0F, 0.68F);
    }

    private void tickColumnSlam(ServerLevel serverLevel) {
        if (this.columnSlamTicks <= 0) {
            return;
        }
        this.columnSlamTicks--;

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && this.columnSlamTicks > 18) {
            this.getLookControl().setLookAt(target, 28.0F, 28.0F);
        }

        if (this.columnSlamTicks == 18) {
            float yawRad = this.getYRot() * Mth.DEG_TO_RAD;
            Vec3 forward = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
            Vec3 impactCenter = this.position().add(forward.scale(1.45D));

            serverLevel.playSound(null, impactCenter.x, impactCenter.y, impactCenter.z,
                    SoundEvents.MACE_SMASH_GROUND_HEAVY, SoundSource.HOSTILE, 0.95F, 0.78F);
            serverLevel.playSound(null, impactCenter.x, impactCenter.y, impactCenter.z,
                    SoundEvents.DEEPSLATE_BRICKS_BREAK, SoundSource.HOSTILE, 1.0F, 0.68F);

            BlockParticleOption rubble = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE_BRICKS.defaultBlockState());
            serverLevel.sendParticles(rubble,
                    impactCenter.x, impactCenter.y + 0.15D, impactCenter.z,
                    42, 1.25D, 0.25D, 1.25D, 0.16D);
            serverLevel.sendParticles(SOUL_STONE_CYAN_DUST,
                    impactCenter.x, impactCenter.y + 0.35D, impactCenter.z,
                    18, 1.15D, 0.25D, 1.15D, 0.03D);

            for (LivingEntity victim : serverLevel.getEntitiesOfClass(
                    LivingEntity.class,
                    this.getBoundingBox().inflate(3.6D, 2.0D, 3.6D),
                    e -> e.isAlive() && e != this && !(e instanceof BarrowWightGolemEntity))) {
                if (victim instanceof Player p && (p.isCreative() || p.isSpectator())) {
                    continue;
                }
                if (victim.distanceToSqr(impactCenter) <= 10.25D) {
                    if (victim.hurtServer(serverLevel, this.damageSources().mobAttack(this), 12.0F)) {
                        Vec3 push = victim.position().subtract(this.position());
                        double len = Math.hypot(push.x, push.z);
                        if (len > 1.0E-4D) {
                            victim.knockback(1.15D, -push.x / len, -push.z / len);
                        }
                        victim.push(0.0D, 0.28D, 0.0D);
                        victim.hurtMarked = true;
                    }
                }
            }
        }
    }

    /**
     * Open-Chest Soulfire Shockwave Burst (2.00s = 40 ticks; wave erupts at t = 0.92s -> remaining == 22):
     * deals 10 damage in a 5.5-block radial soulfire shockwave!
     */
    public void startSoulfireBurst(ServerLevel serverLevel) {
        if (this.isPerformingAttack() || this.getSentinelState() != STATE_ACTIVE) {
            return;
        }
        this.soulfireBurstTicks = SOULFIRE_BURST_DURATION_TICKS;
        this.burstCooldown = 140;
        this.combatCooldown = 46;
        this.setBurstingSoulfire(true);
        this.getNavigation().stop();
        serverLevel.broadcastEntityEvent(this, EVENT_SOULFIRE_BURST);
        serverLevel.playSound(null, this.getX(), this.getY() + 1.3D, this.getZ(),
                SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.HOSTILE, 0.85F, 1.15F);
    }

    private void tickSoulfireBurst(ServerLevel serverLevel) {
        if (this.soulfireBurstTicks <= 0) {
            return;
        }
        this.soulfireBurstTicks--;

        if (this.soulfireBurstTicks > 22 && this.soulfireBurstTicks % 3 == 0) {
            serverLevel.sendParticles(SOUL_CORE_WHITE_DUST,
                    this.getX(), this.getY() + 1.32D, this.getZ(),
                    8, 0.25D, 0.25D, 0.25D, 0.02D);
        }

        if (this.soulfireBurstTicks == 22) {
            double cx = this.getX();
            double cy = this.getY() + 1.25D;
            double cz = this.getZ();

            serverLevel.playSound(null, cx, cy, cz,
                    SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 0.75F, 1.35F);
            serverLevel.playSound(null, cx, cy, cz,
                    SoundEvents.SOUL_ESCAPE.value(), SoundSource.HOSTILE, 1.2F, 0.78F);

            for (int i = 0; i < 36; i++) {
                double angle = (Math.PI * 2.0D * i) / 36.0D;
                for (double r : new double[]{1.8D, 3.4D, 5.0D}) {
                    serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                            cx + Math.cos(angle) * r, cy - 0.55D, cz + Math.sin(angle) * r,
                            1, 0.08D, 0.12D, 0.08D, 0.02D);
                }
            }
            serverLevel.sendParticles(SOUL_STONE_CYAN_DUST, cx, cy, cz, 36, 2.2D, 0.65D, 2.2D, 0.06D);
            serverLevel.sendParticles(ParticleTypes.SCULK_SOUL, cx, cy, cz, 20, 1.8D, 0.55D, 1.8D, 0.06D);

            for (LivingEntity victim : serverLevel.getEntitiesOfClass(
                    LivingEntity.class,
                    this.getBoundingBox().inflate(5.5D, 2.8D, 5.5D),
                    e -> e.isAlive() && e != this && !(e instanceof BarrowWightGolemEntity))) {
                if (victim instanceof Player p && (p.isCreative() || p.isSpectator())) {
                    continue;
                }
                if (this.distanceToSqr(victim) <= 30.25D) {
                    if (victim.hurtServer(serverLevel, this.damageSources().mobAttack(this), 10.0F)) {
                        victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0), this);
                        Vec3 push = victim.position().subtract(this.position());
                        double len = Math.hypot(push.x, push.z);
                        if (len > 1.0E-4D) {
                            victim.knockback(0.95D, -push.x / len, -push.z / len);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        boolean sealed = this.getSentinelState() == STATE_DORMANT || this.getSentinelState() == STATE_ENTERING_DORMANT;
        float adjusted = sealed ? amount * 0.35F : amount;
        boolean hurt = super.hurtServer(serverLevel, source, adjusted);
        if (hurt && this.isAlive()) {
            if (sealed) {
                if (source.getEntity() instanceof LivingEntity attacker) {
                    this.setTarget(attacker);
                }
                this.triggerAwaken(serverLevel);
            } else if (!this.isPerformingAttack()) {
                serverLevel.broadcastEntityEvent(this, EVENT_HURT_FLINCH);
            }
        }
        return hurt;
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_COLUMN_SLAM -> this.attackAnimationState.start(this.tickCount);
            case EVENT_SOULFIRE_BURST -> this.soulfireBurstAnimationState.start(this.tickCount);
            case EVENT_AWAKEN -> {
                this.dormantAnimationState.stop();
                this.dormantEnterAnimationState.stop();
                this.awakenAnimationState.start(this.tickCount);
            }
            case EVENT_DORMANT_ENTER -> {
                this.idleAnimationState.stop();
                this.walkAnimationState.stop();
                this.awakenAnimationState.stop();
                this.dormantEnterAnimationState.start(this.tickCount);
            }
            case EVENT_HURT_FLINCH -> this.hurtAnimationState.start(this.tickCount);
            default -> super.handleEntityEvent(id);
        }
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (!this.level().isClientSide() && this.deathTime == 1 && this.level() instanceof ServerLevel serverLevel) {
            this.setBurstingSoulfire(false);
            serverLevel.playSound(null, this.getX(), this.getY() + 1.0D, this.getZ(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 0.95F, 0.82F);
            serverLevel.sendParticles(ParticleTypes.SOUL,
                    this.getX(), this.getY() + 1.25D, this.getZ(),
                    18, 0.55D, 0.45D, 0.55D, 0.04D);
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

        // Sneaking masonry repair / soul offering on a dormant or unaggroed Crypt Sentinel
        if (player.isCrouching() && (stack.is(Items.STONE_BRICKS)
                || stack.is(Items.MOSSY_STONE_BRICKS)
                || stack.is(Items.CHISELED_STONE_BRICKS)
                || stack.is(Items.SOUL_SAND))) {
            if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.heal(8.0F);
                serverLevel.playSound(null, this.getX(), this.getY() + 1.0D, this.getZ(),
                        SoundEvents.IRON_GOLEM_REPAIR, SoundSource.NEUTRAL, 0.85F, 0.95F);
                serverLevel.sendParticles(SOUL_STONE_CYAN_DUST,
                        this.getX(), this.getY() + 1.2D, this.getZ(),
                        16, 0.45D, 0.45D, 0.45D, 0.03D);
                if (this.stoneShedCooldown <= 0) {
                    this.stoneShedCooldown = STONE_SHED_COOLDOWN_TICKS;
                    Item stoneItem = BuiltInRegistries.ITEM.getOptional(RUNIC_CRYPT_STONE_ID)
                            .orElse(ModItems.RUNIC_CRYPT_STONE.get());
                    this.spawnAtLocation(serverLevel, new ItemStack(stoneItem));
                }
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    public static boolean checkBarrowWightGolemSpawnRules(
            EntityType<BarrowWightGolemEntity> type,
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
        boolean validFloor = below.is(Blocks.STONE_BRICKS)
                || below.is(Blocks.MOSSY_STONE_BRICKS)
                || below.is(Blocks.CRACKED_STONE_BRICKS)
                || below.is(Blocks.DEEPSLATE_BRICKS)
                || below.is(Blocks.SOUL_SAND)
                || below.is(Blocks.SOUL_SOIL)
                || below.is(Blocks.BASALT)
                || below.is(Blocks.BLACKSTONE)
                || belowPath.contains("barrow")
                || belowPath.contains("crypt")
                || belowPath.contains("soul")
                || belowPath.contains("ash");
        return validFloor && level.getBlockState(pos).getFluidState().isEmpty();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        int persistedState = (this.getSentinelState() == STATE_ACTIVE || this.getSentinelState() == STATE_AWAKENING)
                ? STATE_ACTIVE
                : STATE_DORMANT;
        output.store("SentinelState", Codec.INT, persistedState);
        output.store("StoneShedCooldown", Codec.INT, this.stoneShedCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setSentinelState(input.read("SentinelState", Codec.INT).orElse(STATE_DORMANT));
        this.stoneShedCooldown = input.read("StoneShedCooldown", Codec.INT).orElse(0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.getSentinelState() == STATE_DORMANT
                ? SoundEvents.BEACON_AMBIENT
                : SoundEvents.WITHER_SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.10F + 0.64F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.IRON_GOLEM_STEP, 0.85F, 0.68F);
    }

    private static final class BarrowWightCombatGoal extends Goal {
        private final BarrowWightGolemEntity golem;
        private int repathTicks = 0;

        BarrowWightCombatGoal(BarrowWightGolemEntity golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.golem.getTarget();
            return target != null && target.isAlive() && this.golem.getSentinelState() == STATE_ACTIVE;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.golem.getTarget();
            if (target == null || !target.isAlive() || this.golem.getSentinelState() != STATE_ACTIVE) {
                return false;
            }
            if (target instanceof Player p && (p.isCreative() || p.isSpectator())) {
                return false;
            }
            return this.golem.distanceToSqr(target) <= 676.0D;
        }

        @Override
        public void stop() {
            this.golem.getNavigation().stop();
        }

        @Override
        public void tick() {
            LivingEntity target = this.golem.getTarget();
            if (target == null || !(this.golem.level() instanceof ServerLevel serverLevel)) {
                return;
            }

            this.golem.getLookControl().setLookAt(target, 26.0F, 26.0F);
            if (this.golem.isPerformingAttack()) {
                this.golem.getNavigation().stop();
                return;
            }

            if (--this.repathTicks <= 0) {
                this.repathTicks = 6 + this.golem.getRandom().nextInt(5);
                this.golem.getNavigation().moveTo(target, 1.05D);
            }

            if (this.golem.combatCooldown > 0 || !this.golem.hasLineOfSight(target)) {
                return;
            }

            double distSqr = this.golem.distanceToSqr(target);

            // Open-chest 10-damage Soulfire Shockwave Burst when off cooldown at 2.8 - 5.2 blocks (or 35% mix in melee)!
            if (this.golem.burstCooldown <= 0 && distSqr <= 27.0D && (distSqr > 9.0D || this.golem.getRandom().nextInt(3) == 0)) {
                this.golem.startSoulfireBurst(serverLevel);
                return;
            }

            // Crushing 12-damage Double-Fist Stone Column Slam in melee range (<= 3.2 blocks)!
            if (distSqr <= 10.25D) {
                this.golem.startColumnSlam(serverLevel);
            }
        }
    }
}
