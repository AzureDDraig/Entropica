package ddraig.net.entropica.entity.obsidian_golem;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TridentItem;
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
 * Fauna 55 — Obsidian Golem (Nether Sentinel) (entropica:obsidian_golem).
 * Hostile towering four-armed Nether sentinel colossus native to 6.5 Rift Flats & 6.2 Basalt Delves:
 * - Powered by a swirling purple Nether-portal singularity core inside its hollow 12x12x12 obsidian chest cage.
 * - Slow, thunderous heavy stone stride guarding ancient portal ruins (idle / walk).
 * - Crushing Quad-Fist Crying-Obsidian Column Slam (attack): raises all four arms overhead and smashes the
 *   ground for 15.0 damage and heavy knockback.
 * - Open-Chest Spatial Portal Shockwave Burst (spatial_wave): compresses its chest singularity then flings
 *   all four arms into a wide X-spread to unleash a 12.0-damage spatial shockwave.
 * - Impenetrable 4-Arm Projectile Shield Wall (shield_block): locks all four crying-obsidian forearms across
 *   its chest to block and deflect incoming projectiles!
 * - Drops Obsidian Core (entropica:obsidian_core) and 2-4 Obsidian blocks.
 */
public class ObsidianGolemEntity extends Monster {

    private static final EntityDataAccessor<Boolean> SHIELD_WALL_LOCKED =
            SynchedEntityData.defineId(ObsidianGolemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> PORTAL_SURGING =
            SynchedEntityData.defineId(ObsidianGolemEntity.class, EntityDataSerializers.BOOLEAN);

    private static final byte EVENT_QUAD_SLAM = 4;
    private static final byte EVENT_SPATIAL_WAVE = 61;
    private static final byte EVENT_SHIELD_BLOCK = 62;
    private static final byte EVENT_HURT_FLINCH = 63;

    private static final int QUAD_SLAM_DURATION_TICKS = 36;    // 1.80s attack
    private static final int SPATIAL_WAVE_DURATION_TICKS = 36; // 1.80s spatial_wave
    private static final int SHIELD_BLOCK_DURATION_TICKS = 26; // 1.30s shield_block
    private static final int DEATH_DURATION_TICKS = 36;        // 1.80s death hold
    private static final int CORE_SHED_COOLDOWN_TICKS = 1200;

    private static final ResourceLocation OBSIDIAN_CORE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "obsidian_core");

    private static final DustParticleOptions PORTAL_VIOLET_DUST = new DustParticleOptions(0xA855F7, 0.85F);
    private static final DustParticleOptions SINGULARITY_WHITE_DUST = new DustParticleOptions(0xF8ECFF, 0.70F);
    private static final DustParticleOptions ARCANE_CYAN_DUST = new DustParticleOptions(0x06B6D4, 0.65F);
    private static final BlockParticleOption CRYING_OBSIDIAN_DEBRIS =
            new BlockParticleOption(ParticleTypes.BLOCK, Blocks.CRYING_OBSIDIAN.defaultBlockState());

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState spatialWaveAnimationState = new AnimationState();
    public final AnimationState shieldBlockAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    private int quadSlamTicks = 0;
    private int spatialWaveTicks = 0;
    private int shieldBlockTicks = 0;
    private int combatCooldown = 0;
    private int spatialWaveCooldown = 70;
    private int coreShedCooldown = 0;

    public ObsidianGolemEntity(EntityType<? extends ObsidianGolemEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 25;
        this.setPathfindingMalus(PathType.LAVA, 0.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 180.0D)
                .add(Attributes.ATTACK_DAMAGE, 15.0D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.90D)
                .add(Attributes.MOVEMENT_SPEED, 0.21D)
                .add(Attributes.FOLLOW_RANGE, 28.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SHIELD_WALL_LOCKED, false);
        builder.define(PORTAL_SURGING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new ObsidianGolemCombatGoal(this));
        this.goalSelector.addGoal(2, new PortalRuinPatrolGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.85D) {
            @Override
            public boolean canUse() {
                return !ObsidianGolemEntity.this.isBusyInAction() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !ObsidianGolemEntity.this.isBusyInAction() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean isShieldWallLocked() {
        return this.entityData.get(SHIELD_WALL_LOCKED);
    }

    private void setShieldWallLocked(boolean locked) {
        this.entityData.set(SHIELD_WALL_LOCKED, locked);
    }

    public boolean isPortalSurging() {
        return this.entityData.get(PORTAL_SURGING);
    }

    private void setPortalSurging(boolean surging) {
        this.entityData.set(PORTAL_SURGING, surging);
    }

    public boolean isBusyInAction() {
        return this.quadSlamTicks > 0 || this.spatialWaveTicks > 0 || this.shieldBlockTicks > 0;
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
        // Colossus stone construct does not take fall damage
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        if (source.is(DamageTypeTags.IS_FIRE)
                || source.is(DamageTypeTags.IS_FALL)
                || source.is(DamageTypes.LAVA)
                || source.is(DamageTypes.HOT_FLOOR)
                || source.is(DamageTypes.IN_WALL)) {
            return true;
        }
        return super.isInvulnerableTo(level, source);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            this.updateClientAnimations();
            this.spawnClientSingularityParticles();
            return;
        }

        if (!this.isAlive()) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();

        if (this.combatCooldown > 0) {
            this.combatCooldown--;
        }
        if (this.spatialWaveCooldown > 0) {
            this.spatialWaveCooldown--;
        }
        if (this.coreShedCooldown > 0) {
            this.coreShedCooldown--;
        }

        // Thunderous stone footsteps
        if (this.onGround()
                && this.getDeltaMovement().horizontalDistanceSqr() > 8.0E-4D
                && this.tickCount % 16 == 0) {
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.IRON_GOLEM_STEP, SoundSource.HOSTILE, 0.90F, 0.58F);
        }

        if (this.shieldBlockTicks > 0) {
            this.shieldBlockTicks--;
            if (this.shieldBlockTicks == 0) {
                this.setShieldWallLocked(false);
            }
        }

        this.tickQuadFistSlam(serverLevel);
        this.tickSpatialWave(serverLevel);
        this.setPortalSurging(this.spatialWaveTicks > 0);
    }

    private void updateClientAnimations() {
        if (this.isDeadOrDying()) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.attackAnimationState.stop();
            this.spatialWaveAnimationState.stop();
            this.shieldBlockAnimationState.stop();
            this.hurtAnimationState.stop();
            if (!this.deathAnimationState.isStarted()) {
                this.deathAnimationState.start(this.tickCount);
            }
            return;
        }

        double hSpeedSqr = this.getDeltaMovement().horizontalDistanceSqr();
        if (hSpeedSqr > 3.5E-4D || this.walkAnimation.speed() > 0.06F) {
            this.walkAnimationState.startIfStopped(this.tickCount);
            this.idleAnimationState.stop();
        } else {
            this.idleAnimationState.startIfStopped(this.tickCount);
            this.walkAnimationState.stop();
        }
    }

    private void spawnClientSingularityParticles() {
        if (this.isDeadOrDying()) {
            return;
        }
        if (this.random.nextInt(3) == 0) {
            this.level().addParticle(ParticleTypes.PORTAL,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.70D,
                    this.getY() + 1.45D + (this.random.nextDouble() - 0.5D) * 0.45D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.70D,
                    (this.random.nextDouble() - 0.5D) * 0.14D,
                    (this.random.nextDouble() - 0.5D) * 0.14D,
                    (this.random.nextDouble() - 0.5D) * 0.14D);
        }
        if (this.random.nextInt(8) == 0) {
            this.level().addParticle(ARCANE_CYAN_DUST,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 1.15D,
                    this.getY() + 1.35D + (this.random.nextDouble() - 0.5D) * 0.65D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 1.15D,
                    0.0D, 0.01D, 0.0D);
        }
    }

    /**
     * Crushing Quad-Fist Crying-Obsidian Column Slam (1.80s = 36 ticks; ground impact at t = 0.80s -> remaining == 20):
     * Deals 15.0 damage and heavy knockback!
     */
    public void startQuadFistSlam(ServerLevel serverLevel) {
        if (this.isBusyInAction()) {
            return;
        }
        this.quadSlamTicks = QUAD_SLAM_DURATION_TICKS;
        this.combatCooldown = 42;
        this.getNavigation().stop();
        serverLevel.broadcastEntityEvent(this, EVENT_QUAD_SLAM);
        serverLevel.playSound(null, this.getX(), this.getY() + 1.6D, this.getZ(),
                SoundEvents.IRON_GOLEM_ATTACK, SoundSource.HOSTILE, 1.10F, 0.58F);
    }

    private void tickQuadFistSlam(ServerLevel serverLevel) {
        if (this.quadSlamTicks <= 0) {
            return;
        }
        this.quadSlamTicks--;

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && this.quadSlamTicks > 20) {
            this.getLookControl().setLookAt(target, 28.0F, 28.0F);
        }

        if (this.quadSlamTicks == 20) {
            float yawRad = this.getYRot() * Mth.DEG_TO_RAD;
            Vec3 slamPos = this.position().add(-Mth.sin(yawRad) * 1.45D, 0.15D, Mth.cos(yawRad) * 1.45D);

            serverLevel.playSound(null, slamPos.x, slamPos.y, slamPos.z,
                    SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 0.65F, 0.72F);
            serverLevel.playSound(null, slamPos.x, slamPos.y, slamPos.z,
                    SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.HOSTILE, 0.85F, 0.80F);
            serverLevel.sendParticles(CRYING_OBSIDIAN_DEBRIS,
                    slamPos.x, slamPos.y + 0.2D, slamPos.z,
                    32, 1.35D, 0.25D, 1.35D, 0.14D);
            serverLevel.sendParticles(PORTAL_VIOLET_DUST,
                    slamPos.x, slamPos.y + 0.35D, slamPos.z,
                    22, 1.30D, 0.30D, 1.30D, 0.04D);

            float slamDamage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE); // 15.0F
            for (LivingEntity victim : serverLevel.getEntitiesOfClass(
                    LivingEntity.class,
                    this.getBoundingBox().inflate(3.4D, 1.8D, 3.4D),
                    e -> e.isAlive() && e != this && !(e instanceof ObsidianGolemEntity))) {
                if (victim instanceof Player p && (p.isCreative() || p.isSpectator())) {
                    continue;
                }
                if (victim.position().distanceToSqr(slamPos) <= 12.25D || this.distanceToSqr(victim) <= 13.5D) {
                    if (victim.hurtServer(serverLevel, this.damageSources().mobAttack(this), slamDamage)) {
                        double dx = this.getX() - victim.getX();
                        double dz = this.getZ() - victim.getZ();
                        if (dx * dx + dz * dz < 1.0E-4D) {
                            dx = Mth.sin(yawRad);
                            dz = -Mth.cos(yawRad);
                        }
                        victim.knockback(1.65D, dx, dz);
                        victim.setDeltaMovement(victim.getDeltaMovement().add(0.0D, 0.34D, 0.0D));
                        victim.hurtMarked = true;
                    }
                }
            }
        }
    }

    /**
     * Open-Chest Spatial Portal Shockwave Burst (1.80s = 36 ticks; singularity release at t = 0.80s -> remaining == 20):
     * Deals 12.0 damage across a 5.5-block radius around its hollow obsidian chest cage!
     */
    public void startSpatialWave(ServerLevel serverLevel) {
        if (this.isBusyInAction()) {
            return;
        }
        this.spatialWaveTicks = SPATIAL_WAVE_DURATION_TICKS;
        this.spatialWaveCooldown = 130;
        this.combatCooldown = 44;
        this.setPortalSurging(true);
        this.getNavigation().stop();
        serverLevel.broadcastEntityEvent(this, EVENT_SPATIAL_WAVE);
        serverLevel.playSound(null, this.getX(), this.getY() + 1.5D, this.getZ(),
                SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.HOSTILE, 1.0F, 0.72F);
    }

    private void tickSpatialWave(ServerLevel serverLevel) {
        if (this.spatialWaveTicks <= 0) {
            return;
        }
        this.spatialWaveTicks--;

        Vec3 coreCenter = new Vec3(this.getX(), this.getY() + 1.48D, this.getZ());

        // Inward singularity compression particles (0.0s -> 0.65s)
        if (this.spatialWaveTicks > 20 && this.spatialWaveTicks % 2 == 0) {
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    coreCenter.x, coreCenter.y, coreCenter.z,
                    10, 0.55D, 0.55D, 0.55D, 0.08D);
            serverLevel.sendParticles(SINGULARITY_WHITE_DUST,
                    coreCenter.x, coreCenter.y, coreCenter.z,
                    5, 0.30D, 0.30D, 0.30D, 0.02D);
        }

        // Radial 12-damage spatial portal shockwave release at t = 0.80s (remaining == 20)
        if (this.spatialWaveTicks == 20) {
            serverLevel.playSound(null, coreCenter.x, coreCenter.y, coreCenter.z,
                    SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 0.75F, 0.82F);
            serverLevel.playSound(null, coreCenter.x, coreCenter.y, coreCenter.z,
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.1F, 0.65F);

            for (int i = 0; i < 28; i++) {
                double ang = (Math.PI * 2.0D * i) / 28.0D;
                double rx = coreCenter.x + Math.cos(ang) * 3.8D;
                double rz = coreCenter.z + Math.sin(ang) * 3.8D;
                serverLevel.sendParticles(PORTAL_VIOLET_DUST, rx, this.getY() + 1.1D, rz, 2, 0.20D, 0.20D, 0.20D, 0.03D);
                serverLevel.sendParticles(SINGULARITY_WHITE_DUST, rx, this.getY() + 1.2D, rz, 1, 0.12D, 0.12D, 0.12D, 0.02D);
            }
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    coreCenter.x, coreCenter.y, coreCenter.z,
                    48, 2.2D, 0.85D, 2.2D, 0.20D);

            for (LivingEntity victim : serverLevel.getEntitiesOfClass(
                    LivingEntity.class,
                    this.getBoundingBox().inflate(5.5D, 2.8D, 5.5D),
                    e -> e.isAlive() && e != this && !(e instanceof ObsidianGolemEntity))) {
                if (victim instanceof Player p && (p.isCreative() || p.isSpectator())) {
                    continue;
                }
                if (this.distanceToSqr(victim) <= 30.25D) {
                    if (victim.hurtServer(serverLevel, this.damageSources().magic(), 12.0F)) {
                        double dx = this.getX() - victim.getX();
                        double dz = this.getZ() - victim.getZ();
                        if (dx * dx + dz * dz > 1.0E-4D) {
                            victim.knockback(1.25D, dx, dz);
                        }
                        victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 0), this);
                    }
                }
            }
        }
    }

    /**
     * Locks all four crying-obsidian forearms across its chest as an impenetrable shield wall
     * to block and deflect incoming projectiles!
     */
    public void triggerFourArmShieldWall(ServerLevel serverLevel, @Nullable Entity incomingProjectile) {
        this.shieldBlockTicks = SHIELD_BLOCK_DURATION_TICKS;
        this.setShieldWallLocked(true);
        this.getNavigation().stop();
        serverLevel.broadcastEntityEvent(this, EVENT_SHIELD_BLOCK);
        serverLevel.playSound(null, this.getX(), this.getY() + 1.45D, this.getZ(),
                SoundEvents.SHIELD_BLOCK.value(), SoundSource.HOSTILE, 1.15F, 0.72F);
        serverLevel.playSound(null, this.getX(), this.getY() + 1.45D, this.getZ(),
                SoundEvents.ANVIL_PLACE, SoundSource.HOSTILE, 0.45F, 1.45F);
        serverLevel.sendParticles(PORTAL_VIOLET_DUST,
                this.getX(), this.getY() + 1.45D, this.getZ(),
                16, 0.55D, 0.45D, 0.55D, 0.04D);
        serverLevel.sendParticles(ARCANE_CYAN_DUST,
                this.getX(), this.getY() + 1.45D, this.getZ(),
                10, 0.45D, 0.40D, 0.45D, 0.03D);

        if (incomingProjectile instanceof Projectile proj && proj.isAlive()) {
            Vec3 vel = proj.getDeltaMovement();
            proj.setDeltaMovement(vel.scale(-0.65D).add(0.0D, 0.16D, 0.0D));
            proj.setYRot(proj.getYRot() + 180.0F);
            proj.hurtMarked = true;
        }
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        // Deflect incoming projectiles off its 4-forearm crying-obsidian shield wall!
        if (this.isAlive()
                && (source.is(DamageTypeTags.IS_PROJECTILE) || source.getDirectEntity() instanceof Projectile)
                && this.quadSlamTicks <= 0
                && this.spatialWaveTicks <= 0) {
            this.triggerFourArmShieldWall(serverLevel, source.getDirectEntity());
            return false;
        }

        boolean hurt = super.hurtServer(serverLevel, source, amount);
        if (hurt && this.isAlive() && !this.isBusyInAction()) {
            serverLevel.broadcastEntityEvent(this, EVENT_HURT_FLINCH);
        }
        return hurt;
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_QUAD_SLAM -> this.attackAnimationState.start(this.tickCount);
            case EVENT_SPATIAL_WAVE -> this.spatialWaveAnimationState.start(this.tickCount);
            case EVENT_SHIELD_BLOCK -> this.shieldBlockAnimationState.start(this.tickCount);
            case EVENT_HURT_FLINCH -> this.hurtAnimationState.start(this.tickCount);
            default -> super.handleEntityEvent(id);
        }
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (this.deathTime == 1 && !this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            this.setShieldWallLocked(false);
            this.setPortalSurging(false);
            serverLevel.playSound(null, this.getX(), this.getY() + 1.4D, this.getZ(),
                    SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.HOSTILE, 1.1F, 0.60F);
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    this.getX(), this.getY() + 1.4D, this.getZ(),
                    36, 0.55D, 0.55D, 0.55D, 0.12D);
            serverLevel.sendParticles(CRYING_OBSIDIAN_DEBRIS,
                    this.getX(), this.getY() + 1.0D, this.getZ(),
                    24, 0.65D, 0.55D, 0.65D, 0.10D);
        }
        if (this.deathTime >= DEATH_DURATION_TICKS && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (player.isShiftKeyDown()
                && (held.is(Items.CRYING_OBSIDIAN) || held.is(Items.OBSIDIAN))
                && this.getTarget() == null
                && this.isAlive()) {
            if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                this.heal(10.0F);
                serverLevel.playSound(null, this.getX(), this.getY() + 1.4D, this.getZ(),
                        SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.NEUTRAL, 0.85F, 1.05F);
                serverLevel.sendParticles(PORTAL_VIOLET_DUST,
                        this.getX(), this.getY() + 1.45D, this.getZ(),
                        14, 0.45D, 0.45D, 0.45D, 0.03D);
                if (this.coreShedCooldown <= 0) {
                    this.coreShedCooldown = CORE_SHED_COOLDOWN_TICKS;
                    Item coreItem = BuiltInRegistries.ITEM.getOptional(OBSIDIAN_CORE_ID).orElse(Items.CRYING_OBSIDIAN);
                    this.spawnAtLocation(serverLevel, new ItemStack(coreItem));
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    public static boolean isPortalRuinBlock(BlockState state) {
        if (state.isAir()) {
            return false;
        }
        if (state.is(Blocks.OBSIDIAN)
                || state.is(Blocks.CRYING_OBSIDIAN)
                || state.is(Blocks.NETHER_PORTAL)
                || state.is(Blocks.RESPAWN_ANCHOR)) {
            return true;
        }
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return path.contains("obsidian") || path.contains("void_stone") || path.contains("portal");
    }

    public static boolean isObsidianGolemSpawnSurface(BlockGetter level, BlockPos belowPos, BlockState belowState) {
        if (belowState.isAir()) {
            return false;
        }
        String path = BuiltInRegistries.BLOCK.getKey(belowState.getBlock()).getPath();
        return belowState.is(Blocks.OBSIDIAN)
                || belowState.is(Blocks.CRYING_OBSIDIAN)
                || belowState.is(Blocks.BASALT)
                || belowState.is(Blocks.SMOOTH_BASALT)
                || belowState.is(Blocks.POLISHED_BASALT)
                || belowState.is(Blocks.BLACKSTONE)
                || belowState.is(Blocks.DEEPSLATE)
                || path.contains("obsidian")
                || path.contains("basalt")
                || path.contains("void")
                || belowState.isFaceSturdy(level, belowPos, Direction.UP);
    }

    public static boolean checkObsidianGolemSpawnRules(
            EntityType<? extends ObsidianGolemEntity> type,
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
        return isObsidianGolemSpawnSurface(level, below, level.getBlockState(below));
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        BlockPos below = this.blockPosition().below();
        return isObsidianGolemSpawnSurface(level, below, level.getBlockState(below))
                && super.checkSpawnRules(level, spawnReason);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("CoreShedCooldown", Codec.INT, this.coreShedCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.coreShedCooldown = input.read("CoreShedCooldown", Codec.INT).orElse(0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PORTAL_AMBIENT;
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
    protected float getSoundVolume() {
        return 0.65F;
    }

    @Override
    public float getVoicePitch() {
        return 0.68F + this.random.nextFloat() * 0.14F;
    }

    // =========================================================================
    // AI GOALS: 4-Arm Colossus Combat, Ranged Guard & Portal Ruin Guarding
    // =========================================================================

    private static final class ObsidianGolemCombatGoal extends Goal {
        private final ObsidianGolemEntity golem;

        ObsidianGolemCombatGoal(ObsidianGolemEntity golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.golem.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.golem.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = this.golem.getTarget();
            if (target == null || !(this.golem.level() instanceof ServerLevel serverLevel)) {
                return;
            }

            this.golem.getLookControl().setLookAt(target, 28.0F, 28.0F);
            if (this.golem.isBusyInAction()) {
                return;
            }

            double distSq = this.golem.distanceToSqr(target);

            // Proactive 4-arm shield guard when a distant archer/crossbowman aims at the golem!
            if (distSq > 25.0D && isAimingRangedWeapon(target) && this.golem.random.nextInt(18) == 0) {
                this.golem.triggerFourArmShieldWall(serverLevel, null);
                return;
            }

            // Open-Chest Spatial Portal Shockwave Burst (12.0 damage) at medium-close range
            if (distSq <= 26.0D && distSq >= 6.25D
                    && this.golem.spatialWaveCooldown <= 0
                    && this.golem.combatCooldown <= 0) {
                this.golem.startSpatialWave(serverLevel);
                return;
            }

            // Crushing Quad-Fist Crying-Obsidian Column Slam (15.0 damage + heavy knockback)
            if (distSq <= 11.5D && this.golem.combatCooldown <= 0) {
                this.golem.startQuadFistSlam(serverLevel);
                return;
            }

            this.golem.getNavigation().moveTo(target, 1.0D);
        }

        private static boolean isAimingRangedWeapon(LivingEntity entity) {
            if (!entity.isUsingItem()) {
                return false;
            }
            Item used = entity.getUseItem().getItem();
            return used instanceof BowItem || used instanceof CrossbowItem || used instanceof TridentItem;
        }
    }

    private static final class PortalRuinPatrolGoal extends Goal {
        private final ObsidianGolemEntity golem;
        @Nullable
        private BlockPos ruinPos;

        PortalRuinPatrolGoal(ObsidianGolemEntity golem) {
            this.golem = golem;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.golem.getTarget() != null || this.golem.isBusyInAction() || this.golem.getRandom().nextInt(80) != 0) {
                return false;
            }
            this.ruinPos = this.findNearbyPortalRuin();
            return this.ruinPos != null && this.golem.blockPosition().distSqr(this.ruinPos) > 16.0D;
        }

        @Override
        public boolean canContinueToUse() {
            return this.golem.getTarget() == null
                    && !this.golem.isBusyInAction()
                    && this.ruinPos != null
                    && !this.golem.getNavigation().isDone();
        }

        @Override
        public void start() {
            if (this.ruinPos != null) {
                this.golem.getNavigation().moveTo(
                        this.ruinPos.getX() + 0.5D,
                        this.ruinPos.getY() + 1.0D,
                        this.ruinPos.getZ() + 0.5D,
                        0.85D
                );
            }
        }

        @Nullable
        private BlockPos findNearbyPortalRuin() {
            BlockPos origin = this.golem.blockPosition();
            Level level = this.golem.level();
            for (int attempt = 0; attempt < 18; attempt++) {
                BlockPos check = origin.offset(
                        this.golem.getRandom().nextInt(21) - 10,
                        this.golem.getRandom().nextInt(7) - 3,
                        this.golem.getRandom().nextInt(21) - 10
                );
                if (isPortalRuinBlock(level.getBlockState(check))) {
                    return check;
                }
            }
            return null;
        }
    }
}
