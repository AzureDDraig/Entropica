package ddraig.net.entropica.entity.zephyr_simurgh;

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
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
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
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * Fauna 41 — Zephyr-Simurgh (Wind Lord) (entropica:zephyr_simurgh).
 * Neutral legendary sky-raptor native to 5.1 Silica Dunes & 5.6 Windy Ridge:
 * - Seamless 8-animation flight & perch lifecycle: launches from rocky ridges (takeoff), soars high
 *   riding thermal wind streams (fly), flares copper-tipped wings to airbrake onto perches (landing),
 *   and hops agilely on bronze raptor talons (walk / idle).
 * - Gale-Force Windstorm Clap (attack grounded / fly_attack airborne): snaps both giant copper-tipped
 *   wings forward to hurl attackers 6 blocks backward (+static shock bonus in storms).
 * - Weather Sentinel (screech): throws its hooked beak skyward, flares its 3-plume copper crown crest,
 *   and mantles its giant wings when rain/thunderstorms gather (or when offered Rabbit / Copper Ingots
 *   / Phantom Membranes), granting nearby peaceful travelers a tailwind blessing and shedding Simurgh Feathers.
 */
public class ZephyrSimurghEntity extends PathfinderMob implements FlyingAnimal {

    public static final int MODE_PERCHED = 0;
    public static final int MODE_TAKING_OFF = 1;
    public static final int MODE_SOARING = 2;
    public static final int MODE_LANDING = 3;

    private static final EntityDataAccessor<Integer> FLIGHT_MODE =
            SynchedEntityData.defineId(ZephyrSimurghEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> STORM_CHARGED =
            SynchedEntityData.defineId(ZephyrSimurghEntity.class, EntityDataSerializers.BOOLEAN);

    private static final byte EVENT_GROUND_ATTACK = 4;
    private static final byte EVENT_FLY_ATTACK = 61;
    private static final byte EVENT_TAKEOFF = 62;
    private static final byte EVENT_LANDING = 63;
    private static final byte EVENT_SCREECH = 64;

    private static final int TAKEOFF_DURATION_TICKS = 27;       // 1.35s animation
    private static final int LANDING_DURATION_TICKS = 28;       // 1.40s animation
    private static final int FLY_ATTACK_DURATION_TICKS = 27;    // 1.35s animation
    private static final int GROUND_ATTACK_DURATION_TICKS = 33; // 1.65s animation
    private static final int SCREECH_DURATION_TICKS = 42;       // 2.10s animation
    private static final int FEATHER_SHED_COOLDOWN_TICKS = 1200;

    private static final ResourceLocation SIMURGH_FEATHER_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "simurgh_feather");

    private static final DustParticleOptions COPPER_SPARK_DUST = new DustParticleOptions(0xF59E0B, 0.65F);
    private static final DustParticleOptions ZEPHYR_SKY_DUST = new DustParticleOptions(0x38BDF8, 0.60F);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState flyAnimationState = new AnimationState();
    public final AnimationState takeoffAnimationState = new AnimationState();
    public final AnimationState landingAnimationState = new AnimationState();
    public final AnimationState flyAttackAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState screechAnimationState = new AnimationState();

    private final PathNavigation groundNav;
    private final PathNavigation flyingNav;
    private final MoveControl groundMoveControl;
    private final MoveControl flyingMoveControl;

    private int modeTransitionTicks = 0;
    private int screechTicks = 0;
    private int phaseTimer = 200;
    private int swoopCooldown = 0;
    private int featherShedCooldown = 0;
    private int naturalStormShedTimer = 3600;
    private boolean wasWeatherStormy = false;
    private int preStormWarningCooldown = 0;

    public ZephyrSimurghEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 10;
        this.groundMoveControl = new MoveControl(this);
        this.flyingMoveControl = new FlyingMoveControl(this, 18, true);
        this.groundNav = new GroundPathNavigation(this, level);
        FlyingPathNavigation fNav = new FlyingPathNavigation(this, level);
        fNav.setCanOpenDoors(false);
        fNav.setCanFloat(true);
        this.flyingNav = fNav;

        this.moveControl = this.groundMoveControl;
        this.navigation = this.groundNav;
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 105.0D)
                .add(Attributes.FLYING_SPEED, 0.55D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLIGHT_MODE, MODE_PERCHED);
        builder.define(STORM_CHARGED, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SimurghAerialSwoopGoal(this));
        this.goalSelector.addGoal(2, new SimurghPerchedMeleeGoal(this, 1.20D));
        this.goalSelector.addGoal(3, new SimurghSoarThermalGoal(this));
        this.goalSelector.addGoal(4, new SimurghPerchedStrollGoal(this, 0.90D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public int getFlightMode() {
        return this.entityData.get(FLIGHT_MODE);
    }

    public void setFlightMode(int mode) {
        this.entityData.set(FLIGHT_MODE, mode);
        this.syncNavigationForMode(mode);
    }

    private void syncNavigationForMode(int mode) {
        boolean airborneControl = (mode != MODE_PERCHED);
        if (airborneControl) {
            if (this.moveControl != this.flyingMoveControl) {
                this.moveControl = this.flyingMoveControl;
                this.navigation = this.flyingNav;
            }
            this.setNoGravity(mode == MODE_SOARING || mode == MODE_TAKING_OFF);
        } else {
            if (this.moveControl != this.groundMoveControl) {
                this.moveControl = this.groundMoveControl;
                this.navigation = this.groundNav;
            }
            this.setNoGravity(false);
        }
    }

    public boolean isStormCharged() {
        return this.entityData.get(STORM_CHARGED);
    }

    public void setStormCharged(boolean charged) {
        this.entityData.set(STORM_CHARGED, charged);
    }

    @Override
    public boolean isFlying() {
        return this.getFlightMode() != MODE_PERCHED || !this.onGround();
    }

    public static boolean isRidgeOrDuneSpawnSurface(BlockGetter level, BlockPos belowPos, BlockState belowState) {
        if (belowState.isAir()) {
            return false;
        }
        String path = BuiltInRegistries.BLOCK.getKey(belowState.getBlock()).getPath();
        return belowState.is(Blocks.SAND)
                || belowState.is(Blocks.SANDSTONE)
                || belowState.is(Blocks.RED_SAND)
                || belowState.is(Blocks.RED_SANDSTONE)
                || belowState.is(Blocks.STONE)
                || belowState.is(Blocks.ANDESITE)
                || belowState.is(Blocks.GRANITE)
                || belowState.is(BlockTags.TERRACOTTA)
                || belowState.is(BlockTags.ANIMALS_SPAWNABLE_ON)
                || path.contains("silica")
                || path.contains("dune")
                || path.contains("ridge")
                || (belowState.getFluidState().isEmpty() && belowState.isFaceSturdy(level, belowPos, Direction.UP));
    }

    public static boolean checkZephyrSimurghSpawnRules(
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
        return isRidgeOrDuneSpawnSurface(level, belowPos, level.getBlockState(belowPos));
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        BlockPos belowPos = this.blockPosition().below();
        if (isRidgeOrDuneSpawnSurface(level, belowPos, level.getBlockState(belowPos))) {
            return true;
        }
        return super.checkSpawnRules(level, spawnReason);
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        // Immune to fall damage accumulation
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        if (source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypeTags.IS_LIGHTNING)) {
            return true;
        }
        return super.isInvulnerableTo(level, source);
    }

    @Override
    public void thunderHit(ServerLevel level, LightningBolt lightning) {
        this.setStormCharged(true);
        this.heal(8.0F);
        this.triggerWeatherSentinelScreech(level, true);
    }

    public void beginTakeoff(ServerLevel serverLevel) {
        if (this.getFlightMode() != MODE_PERCHED) {
            return;
        }
        this.screechTicks = 0;
        this.modeTransitionTicks = TAKEOFF_DURATION_TICKS;
        this.phaseTimer = 260 + this.random.nextInt(240);
        this.setFlightMode(MODE_TAKING_OFF);
        this.getNavigation().stop();
        Vec3 cur = this.getDeltaMovement();
        this.setDeltaMovement(cur.x, Math.max(cur.y, 0.42D), cur.z);
        this.hasImpulse = true;

        serverLevel.broadcastEntityEvent(this, EVENT_TAKEOFF);
        serverLevel.playSound(null, this.getX(), this.getY() + 0.8D, this.getZ(),
                SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), SoundSource.NEUTRAL, 0.85F, 1.05F);
        serverLevel.sendParticles(ParticleTypes.CLOUD,
                this.getX(), this.getY() + 0.3D, this.getZ(),
                18, 0.90D, 0.20D, 0.90D, 0.08D);
    }

    public void beginLanding(ServerLevel serverLevel) {
        if (this.getFlightMode() != MODE_SOARING) {
            return;
        }
        this.modeTransitionTicks = LANDING_DURATION_TICKS;
        this.phaseTimer = 220 + this.random.nextInt(220);
        this.setFlightMode(MODE_LANDING);
        this.setNoGravity(false);
        serverLevel.broadcastEntityEvent(this, EVENT_LANDING);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.PHANTOM_FLAP, SoundSource.NEUTRAL, 0.80F, 1.15F);
    }

    public void triggerWeatherSentinelScreech(ServerLevel serverLevel, boolean tryShedFeather) {
        this.screechTicks = SCREECH_DURATION_TICKS;
        if (this.getFlightMode() == MODE_PERCHED) {
            this.getNavigation().stop();
        }
        serverLevel.broadcastEntityEvent(this, EVENT_SCREECH);
        serverLevel.playSound(null, this.getX(), this.getY() + 1.1D, this.getZ(),
                SoundEvents.PARROT_IMITATE_PHANTOM, SoundSource.NEUTRAL, 1.2F, 0.70F);
        serverLevel.playSound(null, this.getX(), this.getY() + 1.1D, this.getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.NEUTRAL, 0.35F, 1.65F);

        serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                this.getX(), this.getY() + 1.05D, this.getZ(),
                22, 0.85D, 0.55D, 0.85D, 0.08D);
        serverLevel.sendParticles(COPPER_SPARK_DUST,
                this.getX(), this.getY() + 1.10D, this.getZ(),
                14, 0.75D, 0.45D, 0.75D, 0.02D);

        // Grant tailwind blessing to peaceful nearby players
        List<Player> travelers = serverLevel.getEntitiesOfClass(
                Player.class,
                this.getBoundingBox().inflate(10.0D),
                p -> p.isAlive() && !p.isSpectator() && p != this.getTarget()
        );
        for (Player p : travelers) {
            p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 400, 0), this);
            p.addEffect(new MobEffectInstance(MobEffects.SPEED, 300, 0), this);
        }

        if (tryShedFeather && this.featherShedCooldown <= 0) {
            this.featherShedCooldown = FEATHER_SHED_COOLDOWN_TICKS;
            this.dropOneSimurghFeather(serverLevel);
        }
    }

    private void dropOneSimurghFeather(ServerLevel serverLevel) {
        Optional<Item> featherOpt = ModItems.SIMURGH_FEATHER.isPresent()
                ? Optional.of(ModItems.SIMURGH_FEATHER.get())
                : BuiltInRegistries.ITEM.getOptional(SIMURGH_FEATHER_ID);
        featherOpt.ifPresent(feather ->
                this.spawnAtLocation(serverLevel, new ItemStack(feather, 1), 0.75F));
    }

    public void unleashGaleWingClap(ServerLevel serverLevel, @Nullable LivingEntity primaryTarget, boolean airborne) {
        serverLevel.broadcastEntityEvent(this, airborne ? EVENT_FLY_ATTACK : EVENT_GROUND_ATTACK);

        float baseDamage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (this.isStormCharged() || serverLevel.isThundering()) {
            baseDamage += 3.0F;
        }

        if (primaryTarget != null && primaryTarget.isAlive()) {
            primaryTarget.hurtServer(serverLevel, this.damageSources().mobAttack(this), baseDamage);
        }

        // 6-block Gale-Force Windstorm Clap knockback wave
        List<LivingEntity> pushed = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(6.0D),
                e -> e != this && e.isAlive() && !(e instanceof ZephyrSimurghEntity) && this.distanceToSqr(e) <= 36.0D
        );
        for (LivingEntity entity : pushed) {
            if (entity == primaryTarget || entity == this.getLastHurtByMob() || (entity instanceof Mob m && m.getTarget() == this)) {
                double dx = this.getX() - entity.getX();
                double dz = this.getZ() - entity.getZ();
                entity.knockback(2.35D, dx, dz);
                Vec3 v = entity.getDeltaMovement();
                entity.setDeltaMovement(v.x, Math.max(v.y, 0.38D), v.z);
                entity.hasImpulse = true;
                entity.hurtMarked = true;
            }
        }

        serverLevel.playSound(null, this.getX(), this.getY() + 0.9D, this.getZ(),
                SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), SoundSource.HOSTILE, 1.15F, 0.85F);
        serverLevel.sendParticles(ParticleTypes.CLOUD,
                this.getX(), this.getY() + 0.85D, this.getZ(),
                32, 1.65D, 0.45D, 1.65D, 0.18D);
        serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                this.getX(), this.getY() + 0.95D, this.getZ(),
                18, 1.35D, 0.45D, 1.35D, 0.12D);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        LivingEntity liv = target instanceof LivingEntity l ? l : null;
        this.unleashGaleWingClap(level, liv, this.getFlightMode() != MODE_PERCHED);
        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(serverLevel, source, amount);
        if (hurt && this.isAlive() && this.getFlightMode() == MODE_PERCHED && source.getEntity() instanceof LivingEntity attacker) {
            if (this.distanceToSqr(attacker) > 25.0D) {
                this.beginTakeoff(serverLevel);
            }
        }
        return hurt;
    }

    public static boolean isSimurghTributeItem(ItemStack stack) {
        return stack.is(Items.RABBIT)
                || stack.is(Items.COOKED_RABBIT)
                || stack.is(Items.COPPER_INGOT)
                || stack.is(Items.PHANTOM_MEMBRANE);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isSimurghTributeItem(stack) && this.isAlive() && this.getTarget() != player) {
            boolean needsHeal = this.getHealth() < this.getMaxHealth();
            boolean canShed = this.featherShedCooldown <= 0;
            if (needsHeal || canShed || this.screechTicks <= 0) {
                if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                    boolean copperOffer = stack.is(Items.COPPER_INGOT);
                    stack.consume(1, player);
                    if (copperOffer) {
                        this.setStormCharged(true);
                    }
                    this.heal(6.0F);
                    this.triggerWeatherSentinelScreech(serverLevel, canShed);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_GROUND_ATTACK) {
            this.screechAnimationState.stop();
            this.attackAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_FLY_ATTACK) {
            this.flyAttackAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_TAKEOFF) {
            this.landingAnimationState.stop();
            this.screechAnimationState.stop();
            this.takeoffAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_LANDING) {
            this.takeoffAnimationState.stop();
            this.landingAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_SCREECH) {
            this.screechAnimationState.start(this.tickCount);
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public void aiStep() {
        this.syncNavigationForMode(this.getFlightMode());
        super.aiStep();

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            if (this.swoopCooldown > 0) {
                this.swoopCooldown--;
            }
            if (this.featherShedCooldown > 0) {
                this.featherShedCooldown--;
            }
            if (this.screechTicks > 0) {
                this.screechTicks--;
            }
            if (this.preStormWarningCooldown > 0) {
                this.preStormWarningCooldown--;
            }

            boolean stormyNow = serverLevel.isRaining() || serverLevel.isThundering();
            if (stormyNow != this.isStormCharged() && serverLevel.isThundering()) {
                this.setStormCharged(true);
            } else if (!stormyNow && this.isStormCharged() && this.tickCount % 400 == 0) {
                this.setStormCharged(false);
            }

            // Pre-Thunderstorm Weather Sentinel: screech 30-60s BEFORE an incoming thunderstorm or rainstorm arrives!
            if (!stormyNow
                    && this.preStormWarningCooldown <= 0
                    && this.screechTicks <= 0
                    && serverLevel.getLevelData() instanceof ServerLevelData weatherData) {
                int thunderCountdown = weatherData.getThunderTime();
                int rainCountdown = weatherData.getRainTime();
                int clearCountdown = weatherData.getClearWeatherTime();
                boolean stormApproaching = (clearCountdown == 0)
                        && ((thunderCountdown > 0 && thunderCountdown <= 1200)
                            || (rainCountdown > 0 && rainCountdown <= 1200));
                if (stormApproaching) {
                    this.preStormWarningCooldown = 1400;
                    this.setStormCharged(true);
                    this.triggerWeatherSentinelScreech(serverLevel, false);
                }
            }

            // Also call out as the storm breaks overhead
            if (stormyNow && !this.wasWeatherStormy && this.screechTicks <= 0) {
                this.triggerWeatherSentinelScreech(serverLevel, true);
            }
            this.wasWeatherStormy = stormyNow;

            if (stormyNow && --this.naturalStormShedTimer <= 0) {
                this.naturalStormShedTimer = 3600 + this.random.nextInt(2400);
                this.dropOneSimurghFeather(serverLevel);
            }

            int mode = this.getFlightMode();
            if (mode == MODE_TAKING_OFF) {
                Vec3 v = this.getDeltaMovement();
                this.setDeltaMovement(v.x, Math.max(v.y, 0.24D), v.z);
                if (--this.modeTransitionTicks <= 0) {
                    this.setFlightMode(MODE_SOARING);
                }
            } else if (mode == MODE_SOARING) {
                int groundY = serverLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.blockPosition().getX(), this.blockPosition().getZ());
                if (this.getTarget() == null && this.getY() < groundY + 6.0D) {
                    Vec3 v = this.getDeltaMovement();
                    this.setDeltaMovement(v.x, Math.max(v.y + 0.05D, 0.16D), v.z);
                }
                if (this.getTarget() == null && --this.phaseTimer <= 0) {
                    if (this.getY() - groundY <= 6.5D) {
                        this.beginLanding(serverLevel);
                    } else {
                        this.getNavigation().moveTo(this.getX(), groundY + 2.0D, this.getZ(), 1.05D);
                        this.phaseTimer = 40;
                    }
                }
            } else if (mode == MODE_LANDING) {
                Vec3 v = this.getDeltaMovement();
                this.setDeltaMovement(v.x * 0.85D, Mth.clamp(v.y, -0.18D, 0.05D), v.z * 0.85D);
                if (this.onGround() || --this.modeTransitionTicks <= 0) {
                    this.setFlightMode(MODE_PERCHED);
                }
            } else {
                // MODE_PERCHED
                if (!this.onGround() && this.fallDistance > 2.5F) {
                    this.setFlightMode(MODE_SOARING);
                } else if (this.getTarget() != null && this.distanceToSqr(this.getTarget()) > 36.0D) {
                    this.beginTakeoff(serverLevel);
                } else if (--this.phaseTimer <= 0) {
                    if (stormyNow && this.random.nextFloat() < 0.45F && this.screechTicks <= 0) {
                        this.phaseTimer = 180 + this.random.nextInt(160);
                        this.triggerWeatherSentinelScreech(serverLevel, false);
                    } else {
                        this.beginTakeoff(serverLevel);
                    }
                }
            }
        } else if (this.level().isClientSide()) {
            this.setupClientAnimationsAndParticles();
        }
    }

    private void setupClientAnimationsAndParticles() {
        if (!this.isAlive()) {
            return;
        }

        if (this.attackAnimationState.isStarted()
                && this.attackAnimationState.getTimeInMillis(this.tickCount) >= GROUND_ATTACK_DURATION_TICKS * 50L) {
            this.attackAnimationState.stop();
        }
        if (this.flyAttackAnimationState.isStarted()
                && this.flyAttackAnimationState.getTimeInMillis(this.tickCount) >= FLY_ATTACK_DURATION_TICKS * 50L) {
            this.flyAttackAnimationState.stop();
        }
        if (this.takeoffAnimationState.isStarted()
                && this.takeoffAnimationState.getTimeInMillis(this.tickCount) >= TAKEOFF_DURATION_TICKS * 50L) {
            this.takeoffAnimationState.stop();
        }
        if (this.landingAnimationState.isStarted()
                && this.landingAnimationState.getTimeInMillis(this.tickCount) >= LANDING_DURATION_TICKS * 50L) {
            this.landingAnimationState.stop();
        }
        if (this.screechAnimationState.isStarted()
                && this.screechAnimationState.getTimeInMillis(this.tickCount) >= SCREECH_DURATION_TICKS * 50L) {
            this.screechAnimationState.stop();
        }

        int mode = this.getFlightMode();
        if (mode == MODE_SOARING || mode == MODE_TAKING_OFF || !this.onGround()) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.flyAnimationState.startIfStopped(this.tickCount);
        } else {
            this.flyAnimationState.stop();
            boolean hopping = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
            if (hopping) {
                this.idleAnimationState.stop();
                this.walkAnimationState.startIfStopped(this.tickCount);
            } else {
                this.walkAnimationState.stop();
                this.idleAnimationState.startIfStopped(this.tickCount);
            }
        }

        boolean charged = this.isStormCharged() || this.level().isRaining();
        if (mode == MODE_SOARING && this.tickCount % 4 == 0) {
            this.level().addParticle(
                    ZEPHYR_SKY_DUST,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 1.4D,
                    this.getY() + 0.55D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 1.4D,
                    0.0D, -0.01D, 0.0D
            );
        }
        if (charged && this.tickCount % 6 == 0) {
            this.level().addParticle(
                    ParticleTypes.ELECTRIC_SPARK,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 1.3D,
                    this.getY() + 0.4D + this.random.nextDouble() * 0.9D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 1.3D,
                    0.0D, 0.01D, 0.0D
            );
        } else if (this.tickCount % 18 == 0) {
            this.level().addParticle(
                    COPPER_SPARK_DUST,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.9D,
                    this.getY() + 0.45D + this.random.nextDouble() * 0.7D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.9D,
                    0.0D, 0.01D, 0.0D
            );
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("StormCharged", Codec.BOOL, this.isStormCharged());
        output.store("FeatherShedCooldown", Codec.INT, this.featherShedCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setStormCharged(input.read("StormCharged", Codec.BOOL).orElse(false));
        this.featherShedCooldown = input.read("FeatherShedCooldown", Codec.INT).orElse(0);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.PARROT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PARROT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PARROT_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 0.65F + (this.random.nextFloat() - 0.5F) * 0.15F;
    }

    private static final class SimurghSoarThermalGoal extends Goal {
        private final ZephyrSimurghEntity simurgh;

        SimurghSoarThermalGoal(ZephyrSimurghEntity simurgh) {
            this.simurgh = simurgh;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return this.simurgh.getFlightMode() == MODE_SOARING
                    && this.simurgh.getTarget() == null
                    && !this.simurgh.getNavigation().isInProgress()
                    && this.simurgh.getRandom().nextInt(12) == 0;
        }

        @Override
        public void start() {
            Vec3 pos = this.simurgh.position();
            double tx = pos.x + (this.simurgh.getRandom().nextDouble() - 0.5D) * 32.0D;
            double tz = pos.z + (this.simurgh.getRandom().nextDouble() - 0.5D) * 32.0D;
            int groundY = this.simurgh.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(tx), Mth.floor(tz));
            double ty = Math.min(groundY + 10.0D + this.simurgh.getRandom().nextDouble() * 10.0D, this.simurgh.level().getMaxY() - 12);
            this.simurgh.getNavigation().moveTo(tx, ty, tz, 1.15D);
        }
    }

    private static final class SimurghAerialSwoopGoal extends Goal {
        private final ZephyrSimurghEntity simurgh;

        SimurghAerialSwoopGoal(ZephyrSimurghEntity simurgh) {
            this.simurgh = simurgh;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.simurgh.getTarget();
            return this.simurgh.getFlightMode() == MODE_SOARING && target != null && target.isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = this.simurgh.getTarget();
            if (target == null || !target.isAlive()) {
                return;
            }
            this.simurgh.getLookControl().setLookAt(target, 35.0F, 35.0F);
            if (this.simurgh.swoopCooldown <= 0) {
                this.simurgh.getNavigation().moveTo(target.getX(), target.getY() + 1.0D, target.getZ(), 1.45D);
                if (this.simurgh.distanceToSqr(target) <= 16.0D && this.simurgh.level() instanceof ServerLevel serverLevel) {
                    this.simurgh.unleashGaleWingClap(serverLevel, target, true);
                    this.simurgh.swoopCooldown = 55;
                }
            } else {
                this.simurgh.getNavigation().moveTo(target.getX(), target.getY() + 10.0D, target.getZ(), 1.25D);
            }
        }
    }

    private static final class SimurghPerchedMeleeGoal extends MeleeAttackGoal {
        private final ZephyrSimurghEntity simurgh;

        SimurghPerchedMeleeGoal(ZephyrSimurghEntity simurgh, double speedModifier) {
            super(simurgh, speedModifier, true);
            this.simurgh = simurgh;
        }

        @Override
        public boolean canUse() {
            return this.simurgh.getFlightMode() == MODE_PERCHED && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return this.simurgh.getFlightMode() == MODE_PERCHED && super.canContinueToUse();
        }
    }

    private static final class SimurghPerchedStrollGoal extends WaterAvoidingRandomStrollGoal {
        private final ZephyrSimurghEntity simurgh;

        SimurghPerchedStrollGoal(ZephyrSimurghEntity simurgh, double speedModifier) {
            super(simurgh, speedModifier);
            this.simurgh = simurgh;
        }

        @Override
        public boolean canUse() {
            return this.simurgh.getFlightMode() == MODE_PERCHED
                    && this.simurgh.screechTicks <= 0
                    && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return this.simurgh.getFlightMode() == MODE_PERCHED
                    && this.simurgh.screechTicks <= 0
                    && super.canContinueToUse();
        }
    }
}
