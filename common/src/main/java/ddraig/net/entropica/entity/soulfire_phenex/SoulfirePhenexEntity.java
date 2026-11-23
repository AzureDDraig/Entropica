package ddraig.net.entropica.entity.soulfire_phenex;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
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
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
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
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * Fauna 45 — Soulfire Phenex (Goetic Flyer) (entropica:soulfire_phenex).
 * Hostile demonic skeletal bird-of-prey native to 6.3 Soulfire Fjords & 6.4 Ash Canyons:
 * - Perch & Flight Lifecycle: hops agilely on skeletal talons (walk / idle), launches from ledges in a
 *   burst of blue flame (takeoff), flies through cavern vaults trailing blue soulfire (fly), and flares
 *   its bone wings and 5-feather tail fan to airbrake onto perches (landing).
 * - 3-Block Soulfire Breath Cone (attack grounded / fly_attack aerial swoop): breathes blue soulfire in
 *   a 3-block cone dealing 6.0 damage and applying Soul Wither (Wither + Soulfire ignition).
 * - Soul Sand Storm & Windy Nether Surge: flies significantly faster during soul sand storms and in windy
 *   nether/soul/ash biomes.
 * - Hollow Ribcage Soulfire Wisp: houses a dynamic cyan-blue soulfire wisp inside its open 360-degree
 *   ribcage that flares once and gutters out completely as the bleached skeleton collapses upon defeat (death).
 */
public class SoulfirePhenexEntity extends Monster implements FlyingAnimal {

    public static final int MODE_PERCHED = 0;
    public static final int MODE_TAKING_OFF = 1;
    public static final int MODE_SOARING = 2;
    public static final int MODE_LANDING = 3;

    private static final EntityDataAccessor<Integer> FLIGHT_MODE =
            SynchedEntityData.defineId(SoulfirePhenexEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SOUL_STORM_BOOST =
            SynchedEntityData.defineId(SoulfirePhenexEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> BREATHING_SOULFIRE =
            SynchedEntityData.defineId(SoulfirePhenexEntity.class, EntityDataSerializers.BOOLEAN);

    private static final byte EVENT_GROUND_BREATH = 4;
    private static final byte EVENT_FLY_BREATH = 61;
    private static final byte EVENT_TAKEOFF = 62;
    private static final byte EVENT_LANDING = 63;
    private static final byte EVENT_HURT_RATTLE = 64;

    private static final int TAKEOFF_DURATION_TICKS = 26;       // 1.30s animation
    private static final int LANDING_DURATION_TICKS = 27;       // 1.35s animation
    private static final int FLY_BREATH_DURATION_TICKS = 30;    // 1.50s animation
    private static final int GROUND_BREATH_DURATION_TICKS = 31; // 1.55s animation
    private static final int BREATH_IMPACT_TICK = 12;
    private static final int HURT_DURATION_TICKS = 13;          // 0.65s animation
    public static final int DEATH_DURATION_TICKS = 44;          // 2.20s animation
    private static final int FEATHER_SHED_COOLDOWN_TICKS = 1200;

    private static final ResourceLocation PHENEX_EMBER_FEATHER_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "phenex_ember_feather");

    private static final DustParticleOptions SOULFIRE_CYAN_DUST = new DustParticleOptions(0x06B6D4, 0.65F);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState flyAnimationState = new AnimationState();
    public final AnimationState takeoffAnimationState = new AnimationState();
    public final AnimationState landingAnimationState = new AnimationState();
    public final AnimationState flyAttackAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    private final PathNavigation groundNav;
    private final PathNavigation flyingNav;
    private final MoveControl groundMoveControl;
    private final MoveControl flyingMoveControl;

    private int modeTransitionTicks = 0;
    private int phaseTimer = 140;
    private int breathTicks = 0;
    private int breathCooldown = 0;
    private int featherShedCooldown = 0;

    public SoulfirePhenexEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 8;
        this.groundMoveControl = new MoveControl(this);
        this.flyingMoveControl = new FlyingMoveControl(this, 20, true);
        this.groundNav = new GroundPathNavigation(this, level);
        FlyingPathNavigation fNav = new FlyingPathNavigation(this, level);
        fNav.setCanOpenDoors(false);
        fNav.setCanFloat(true);
        this.flyingNav = fNav;

        this.moveControl = this.groundMoveControl;
        this.navigation = this.groundNav;
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.LAVA, -1.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 44.0D)
                .add(Attributes.FLYING_SPEED, 0.56D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 28.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLIGHT_MODE, MODE_PERCHED);
        builder.define(SOUL_STORM_BOOST, false);
        builder.define(BREATHING_SOULFIRE, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PhenexAerialAndPerchCombatGoal(this));
        this.goalSelector.addGoal(2, new PhenexCavernSoarGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.95D) {
            @Override
            public boolean canUse() {
                return SoulfirePhenexEntity.this.getFlightMode() == MODE_PERCHED && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return SoulfirePhenexEntity.this.getFlightMode() == MODE_PERCHED && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

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
        boolean airborne = (mode != MODE_PERCHED);
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

    public boolean isSoulStormBoosted() {
        return this.entityData.get(SOUL_STORM_BOOST);
    }

    private void setSoulStormBoosted(boolean boosted) {
        this.entityData.set(SOUL_STORM_BOOST, boosted);
    }

    public boolean isBreathingSoulfire() {
        return this.entityData.get(BREATHING_SOULFIRE);
    }

    private void setBreathingSoulfire(boolean breathing) {
        this.entityData.set(BREATHING_SOULFIRE, breathing);
    }

    @Override
    public boolean isFlying() {
        return this.getFlightMode() != MODE_PERCHED;
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
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        // Goetic flyer never takes fall damage
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        if (source.is(DamageTypeTags.IS_FALL)
                || source.is(DamageTypeTags.IS_FIRE)
                || source.is(DamageTypes.WITHER)) {
            return true;
        }
        return super.isInvulnerableTo(level, source);
    }

    public static boolean isSoulfireSpawnSurface(BlockGetter level, BlockPos belowPos, BlockState belowState) {
        if (belowState.isAir()) {
            return false;
        }
        String path = BuiltInRegistries.BLOCK.getKey(belowState.getBlock()).getPath();
        return belowState.is(Blocks.SOUL_SAND)
                || belowState.is(Blocks.SOUL_SOIL)
                || belowState.is(Blocks.BASALT)
                || belowState.is(Blocks.SMOOTH_BASALT)
                || belowState.is(Blocks.BLACKSTONE)
                || belowState.is(Blocks.NETHERRACK)
                || belowState.is(BlockTags.SOUL_FIRE_BASE_BLOCKS)
                || path.contains("soul")
                || path.contains("ash")
                || path.contains("basalt")
                || belowState.isFaceSturdy(level, belowPos, Direction.UP);
    }

    public static boolean checkSoulfirePhenexSpawnRules(
            EntityType<? extends Monster> type,
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
        for (int dy = 1; dy <= 3; dy++) {
            BlockPos belowPos = pos.below(dy);
            BlockState belowState = level.getBlockState(belowPos);
            if (belowState.isAir()) {
                continue;
            }
            return isSoulfireSpawnSurface(level, belowPos, belowState);
        }
        return false;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        for (int dy = 1; dy <= 3; dy++) {
            BlockPos belowPos = this.blockPosition().below(dy);
            BlockState belowState = level.getBlockState(belowPos);
            if (belowState.isAir()) {
                continue;
            }
            return isSoulfireSpawnSurface(level, belowPos, belowState);
        }
        return super.checkSpawnRules(level, spawnReason);
    }

    private boolean computeSoulStormOrWindyNetherCondition(ServerLevel serverLevel) {
        if (serverLevel.isRaining() || serverLevel.isThundering()) {
            return true;
        }
        Holder<Biome> biomeHolder = serverLevel.getBiome(this.blockPosition());
        String biomePath = biomeHolder.unwrapKey()
                .map(k -> k.location().getPath())
                .orElse("");
        if (serverLevel.dimension() == Level.NETHER
                || biomePath.contains("soul")
                || biomePath.contains("ash")
                || biomePath.contains("wind")
                || biomePath.contains("canyon")
                || biomePath.contains("fjord")
                || biomePath.contains("valley")
                || biomePath.contains("wastes")) {
            return true;
        }
        BlockPos origin = this.blockPosition();
        for (int dy = 0; dy <= 5; dy++) {
            BlockState st = serverLevel.getBlockState(origin.below(dy));
            if (st.is(BlockTags.SOUL_FIRE_BASE_BLOCKS)
                    || st.is(Blocks.SOUL_FIRE)
                    || st.is(Blocks.SOUL_CAMPFIRE)
                    || st.is(Blocks.SOUL_LANTERN)
                    || st.is(Blocks.SOUL_TORCH)) {
                return true;
            }
        }
        return false;
    }

    public double getEffectiveFlightSpeedMultiplier() {
        return this.isSoulStormBoosted() ? 1.42D : 1.0D;
    }

    public void beginTakeoff(ServerLevel serverLevel) {
        if (this.getFlightMode() != MODE_PERCHED || !this.isAlive()) {
            return;
        }
        this.setFlightMode(MODE_TAKING_OFF);
        this.modeTransitionTicks = TAKEOFF_DURATION_TICKS;
        this.phaseTimer = 220 + this.random.nextInt(180);
        serverLevel.broadcastEntityEvent(this, EVENT_TAKEOFF);

        // Launches from ledges in a burst of blue flame!
        Vec3 vel = this.getDeltaMovement();
        this.setDeltaMovement(vel.x, Math.max(vel.y, 0.42D), vel.z);
        serverLevel.playSound(null, this.getX(), this.getY() + 0.6D, this.getZ(),
                SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 0.85F, 1.30F);
        serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                this.getX(), this.getY() + 0.35D, this.getZ(),
                26, 0.55D, 0.25D, 0.55D, 0.05D);
        serverLevel.sendParticles(ParticleTypes.SOUL,
                this.getX(), this.getY() + 0.35D, this.getZ(),
                8, 0.45D, 0.20D, 0.45D, 0.02D);
    }

    public void beginLanding(ServerLevel serverLevel) {
        if (this.getFlightMode() != MODE_SOARING || !this.isAlive()) {
            return;
        }
        this.setFlightMode(MODE_LANDING);
        this.modeTransitionTicks = LANDING_DURATION_TICKS;
        this.phaseTimer = 140 + this.random.nextInt(140);
        serverLevel.broadcastEntityEvent(this, EVENT_LANDING);
    }

    public void startSoulfireBreath(ServerLevel serverLevel, boolean aerial) {
        int duration = aerial ? FLY_BREATH_DURATION_TICKS : GROUND_BREATH_DURATION_TICKS;
        this.breathTicks = duration;
        this.breathCooldown = duration + 18;
        this.setBreathingSoulfire(true);
        serverLevel.broadcastEntityEvent(this, aerial ? EVENT_FLY_BREATH : EVENT_GROUND_BREATH);
        serverLevel.playSound(null, this.getX(), this.getY() + 0.9D, this.getZ(),
                SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 0.90F, 1.25F);
    }

    private void emitSoulfireBreathConeParticles(ServerLevel serverLevel) {
        Vec3 look = Vec3.directionFromRotation(this.getXRot(), this.getYRot()).normalize();
        Vec3 origin = new Vec3(this.getX() + look.x * 0.45D, this.getY() + 0.95D + look.y * 0.25D, this.getZ() + look.z * 0.45D);
        for (int i = 1; i <= 6; i++) {
            double dist = i * 0.52D;
            double spread = 0.08D * i;
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    origin.x + look.x * dist,
                    origin.y + look.y * dist,
                    origin.z + look.z * dist,
                    3, spread, spread * 0.65D, spread, 0.015D);
        }
        serverLevel.sendParticles(SOULFIRE_CYAN_DUST,
                origin.x + look.x * 1.4D,
                origin.y + look.y * 1.4D,
                origin.z + look.z * 1.4D,
                5, 0.35D, 0.25D, 0.35D, 0.02D);
    }

    private void executeSoulfireBreathConeDamage(ServerLevel serverLevel) {
        this.emitSoulfireBreathConeParticles(serverLevel);

        Vec3 look = Vec3.directionFromRotation(this.getXRot(), this.getYRot()).normalize();
        Vec3 beakPos = new Vec3(this.getX(), this.getY() + 0.95D, this.getZ());
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE); // 6.0F

        // 3-block directional cone check
        List<LivingEntity> candidates = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(3.4D, 2.2D, 3.4D),
                e -> e != this && e.isAlive() && !(e instanceof SoulfirePhenexEntity)
        );
        LivingEntity primaryTarget = this.getTarget();
        for (LivingEntity victim : candidates) {
            if (victim instanceof Player p && (p.isCreative() || p.isSpectator())) {
                continue;
            }
            Vec3 toVictim = victim.getBoundingBox().getCenter().subtract(beakPos);
            double dist = toVictim.length();
            if (dist > 3.35D) {
                continue;
            }
            double dot = dist > 1.0E-4D ? toVictim.scale(1.0D / dist).dot(look) : 1.0D;
            if (dot < 0.42D && victim != primaryTarget) {
                continue;
            }
            if (dot < 0.20D) {
                continue;
            }

            // Deal 6.0 damage + apply Soul Wither (Wither I for 5s + 4s fire ignition)
            boolean hurt = victim.hurtServer(serverLevel, this.damageSources().mobAttack(this), damage);
            if (!hurt) {
                victim.hurtServer(serverLevel, this.damageSources().magic(), damage);
            }
            victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0), this);
            victim.igniteForSeconds(4.0F);
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean isSoulOffering = stack.is(Items.SOUL_SAND)
                || stack.is(Items.SOUL_SOIL)
                || stack.is(Items.SOUL_TORCH)
                || stack.is(Items.BLAZE_POWDER);
        if (isSoulOffering && this.isAlive() && this.featherShedCooldown <= 0) {
            if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                stack.consume(1, player);
                this.featherShedCooldown = FEATHER_SHED_COOLDOWN_TICKS;
                this.heal(4.0F);
                serverLevel.playSound(null, this.getX(), this.getY() + 0.8D, this.getZ(),
                        SoundEvents.SOUL_ESCAPE.value(), SoundSource.HOSTILE, 0.95F, 1.15F);
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX(), this.getY() + 0.75D, this.getZ(),
                        20, 0.45D, 0.35D, 0.45D, 0.03D);

                Optional<Item> featherOpt = ModItems.PHENEX_EMBER_FEATHER.isPresent()
                        ? Optional.of(ModItems.PHENEX_EMBER_FEATHER.get())
                        : BuiltInRegistries.ITEM.getOptional(PHENEX_EMBER_FEATHER_ID);
                featherOpt.ifPresent(feather ->
                        this.spawnAtLocation(serverLevel, new ItemStack(feather, 1), 0.65F));
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(serverLevel, source, amount);
        if (hurt && this.isAlive()) {
            serverLevel.broadcastEntityEvent(this, EVENT_HURT_RATTLE);
            if (this.getFlightMode() == MODE_PERCHED && this.modeTransitionTicks <= 0) {
                this.beginTakeoff(serverLevel);
            }
        }
        return hurt;
    }

    @Override
    protected void tickDeath() {
        this.setNoGravity(false);
        ++this.deathTime;
        if (this.level().isClientSide()) {
            if (this.deathTime == 2) {
                // Ribcage soulfire wisp flares brightly once before guttering out!
                for (int i = 0; i < 18; i++) {
                    this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                            this.getX() + (this.random.nextDouble() - 0.5D) * 0.45D,
                            this.getY() + 0.75D + (this.random.nextDouble() - 0.5D) * 0.25D,
                            this.getZ() + (this.random.nextDouble() - 0.5D) * 0.45D,
                            (this.random.nextDouble() - 0.5D) * 0.08D,
                            0.06D + this.random.nextDouble() * 0.05D,
                            (this.random.nextDouble() - 0.5D) * 0.08D);
                }
                this.level().addParticle(ParticleTypes.SOUL,
                        this.getX(), this.getY() + 0.80D, this.getZ(),
                        0.0D, 0.07D, 0.0D);
            } else if (this.deathTime >= 10 && this.deathTime <= 28 && this.deathTime % 3 == 0) {
                // Cold smoke rises from the extinguished hollow ribcage as the bleached skeleton collapses
                this.level().addParticle(ParticleTypes.SMOKE,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.35D,
                        this.getY() + 0.55D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.35D,
                        0.0D, 0.025D, 0.0D);
            }
        }
        if (this.deathTime >= DEATH_DURATION_TICKS && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_GROUND_BREATH) {
            this.hurtAnimationState.stop();
            this.attackAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_FLY_BREATH) {
            this.hurtAnimationState.stop();
            this.flyAttackAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_TAKEOFF) {
            this.landingAnimationState.stop();
            this.takeoffAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_LANDING) {
            this.takeoffAnimationState.stop();
            this.landingAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_HURT_RATTLE) {
            this.hurtAnimationState.start(this.tickCount);
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public void tick() {
        this.syncNavigationForMode(this.getFlightMode());
        super.tick();

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            if (this.breathCooldown > 0) {
                this.breathCooldown--;
            }
            if (this.featherShedCooldown > 0) {
                this.featherShedCooldown--;
            }

            if (this.isAlive()) {
                if (this.tickCount % 15 == 0) {
                    this.setSoulStormBoosted(this.computeSoulStormOrWindyNetherCondition(serverLevel));
                }

                // Update active soulfire breath cone
                if (this.breathTicks > 0) {
                    int total = (this.getFlightMode() == MODE_PERCHED)
                            ? GROUND_BREATH_DURATION_TICKS
                            : FLY_BREATH_DURATION_TICKS;
                    int elapsed = total - this.breathTicks;
                    this.breathTicks--;
                    if (elapsed >= 8 && elapsed <= 20 && elapsed % 3 == 0) {
                        this.emitSoulfireBreathConeParticles(serverLevel);
                    }
                    if (elapsed == BREATH_IMPACT_TICK) {
                        this.executeSoulfireBreathConeDamage(serverLevel);
                    }
                    if (this.breathTicks == 0) {
                        this.setBreathingSoulfire(false);
                    }
                }

                // Update flight mode transitions
                int mode = this.getFlightMode();
                if (mode == MODE_TAKING_OFF) {
                    Vec3 vel = this.getDeltaMovement();
                    if (vel.y < 0.22D) {
                        this.setDeltaMovement(vel.x, 0.24D, vel.z);
                    }
                    if (--this.modeTransitionTicks <= 0) {
                        this.setFlightMode(MODE_SOARING);
                    }
                } else if (mode == MODE_LANDING) {
                    Vec3 vel = this.getDeltaMovement();
                    this.setDeltaMovement(vel.x * 0.85D, -0.16D, vel.z * 0.85D);
                    if (this.onGround() || --this.modeTransitionTicks <= 0) {
                        this.setFlightMode(MODE_PERCHED);
                    }
                } else if (mode == MODE_PERCHED) {
                    if (this.getTarget() != null && this.getTarget().distanceToSqr(this) > 20.0D) {
                        this.beginTakeoff(serverLevel);
                    } else if (--this.phaseTimer <= 0) {
                        this.beginTakeoff(serverLevel);
                    }
                } else if (mode == MODE_SOARING) {
                    if (this.isSoulStormBoosted()) {
                        Vec3 vel = this.getDeltaMovement();
                        double hSqr = vel.horizontalDistanceSqr();
                        if (hSqr > 1.0E-4D && hSqr < 0.36D) {
                            this.setDeltaMovement(vel.x * 1.08D, vel.y, vel.z * 1.08D);
                        }
                    }
                    if (this.getTarget() == null && --this.phaseTimer <= 0 && this.hasPerchBelow(serverLevel)) {
                        this.beginLanding(serverLevel);
                    }
                }
            } else {
                this.setBreathingSoulfire(false);
            }
        } else if (this.level().isClientSide()) {
            this.setupClientAnimationsAndParticles();
        }
    }

    private boolean hasPerchBelow(ServerLevel level) {
        BlockPos pos = this.blockPosition();
        for (int dy = 1; dy <= 6; dy++) {
            BlockPos check = pos.below(dy);
            BlockState st = level.getBlockState(check);
            if (!st.isAir() && st.isFaceSturdy(level, check, Direction.UP)) {
                return true;
            }
        }
        return false;
    }

    private void setupClientAnimationsAndParticles() {
        if (!this.isAlive()) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.flyAnimationState.stop();
            this.takeoffAnimationState.stop();
            this.landingAnimationState.stop();
            this.flyAttackAnimationState.stop();
            this.attackAnimationState.stop();
            this.hurtAnimationState.stop();
            this.deathAnimationState.startIfStopped(this.tickCount);
            return;
        }

        if (this.takeoffAnimationState.isStarted()
                && this.takeoffAnimationState.getTimeInMillis(this.tickCount) >= TAKEOFF_DURATION_TICKS * 50L) {
            this.takeoffAnimationState.stop();
        }
        if (this.landingAnimationState.isStarted()
                && this.landingAnimationState.getTimeInMillis(this.tickCount) >= LANDING_DURATION_TICKS * 50L) {
            this.landingAnimationState.stop();
        }
        if (this.flyAttackAnimationState.isStarted()
                && this.flyAttackAnimationState.getTimeInMillis(this.tickCount) >= FLY_BREATH_DURATION_TICKS * 50L) {
            this.flyAttackAnimationState.stop();
        }
        if (this.attackAnimationState.isStarted()
                && this.attackAnimationState.getTimeInMillis(this.tickCount) >= GROUND_BREATH_DURATION_TICKS * 50L) {
            this.attackAnimationState.stop();
        }
        if (this.hurtAnimationState.isStarted()
                && this.hurtAnimationState.getTimeInMillis(this.tickCount) >= HURT_DURATION_TICKS * 50L) {
            this.hurtAnimationState.stop();
        }

        int mode = this.getFlightMode();
        if (mode != MODE_PERCHED) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.flyAnimationState.startIfStopped(this.tickCount);

            // Flies through cavern vaults leaving blue soulfire particles in its wake!
            int wakeInterval = this.isSoulStormBoosted() ? 1 : 2;
            if (this.tickCount % wakeInterval == 0) {
                this.level().addParticle(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.65D,
                        this.getY() + 0.65D + (this.random.nextDouble() - 0.5D) * 0.25D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.65D,
                        -this.getDeltaMovement().x * 0.25D,
                        0.01D,
                        -this.getDeltaMovement().z * 0.25D
                );
            }
        } else {
            this.flyAnimationState.stop();
            boolean hopping = this.getDeltaMovement().horizontalDistanceSqr() > 2.5E-4D;
            if (hopping) {
                this.idleAnimationState.stop();
                this.walkAnimationState.startIfStopped(this.tickCount);
            } else {
                this.walkAnimationState.stop();
                this.idleAnimationState.startIfStopped(this.tickCount);
            }

            if (this.tickCount % 7 == 0) {
                this.level().addParticle(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.28D,
                        this.getY() + 0.78D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.28D,
                        0.0D, 0.015D, 0.0D
                );
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("FeatherShedCooldown", Codec.INT, this.featherShedCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.featherShedCooldown = input.read("FeatherShedCooldown", Codec.INT).orElse(0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_SKELETON_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SKELETON_STEP, 0.30F, 1.25F);
    }

    private static final class PhenexAerialAndPerchCombatGoal extends Goal {
        private final SoulfirePhenexEntity phenex;
        private int repathTicks = 0;

        PhenexAerialAndPerchCombatGoal(SoulfirePhenexEntity phenex) {
            this.phenex = phenex;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.phenex.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.phenex.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }
            if (target instanceof Player p && (p.isCreative() || p.isSpectator())) {
                return false;
            }
            return this.phenex.distanceToSqr(target) <= 900.0D;
        }

        @Override
        public void tick() {
            LivingEntity target = this.phenex.getTarget();
            if (target == null || !(this.phenex.level() instanceof ServerLevel serverLevel)) {
                return;
            }

            this.phenex.getLookControl().setLookAt(target, 35.0F, 35.0F);
            double distSqr = this.phenex.distanceToSqr(target);
            double speedMult = 1.20D * this.phenex.getEffectiveFlightSpeedMultiplier();

            if (--this.repathTicks <= 0) {
                this.repathTicks = 4 + this.phenex.getRandom().nextInt(5);
                if (this.phenex.getFlightMode() != MODE_PERCHED) {
                    this.phenex.getMoveControl().setWantedPosition(
                            target.getX(),
                            target.getY() + 1.15D,
                            target.getZ(),
                            speedMult
                    );
                } else {
                    this.phenex.getNavigation().moveTo(target, 1.15D);
                }
            }

            if (this.phenex.breathCooldown <= 0
                    && this.phenex.breathTicks <= 0
                    && distSqr <= 10.5D
                    && this.phenex.hasLineOfSight(target)) {
                boolean aerial = this.phenex.getFlightMode() != MODE_PERCHED;
                if (aerial) {
                    Vec3 dive = target.position().add(0.0D, 0.8D, 0.0D).subtract(this.phenex.position()).normalize();
                    this.phenex.setDeltaMovement(this.phenex.getDeltaMovement().add(dive.scale(0.26D)));
                }
                this.phenex.startSoulfireBreath(serverLevel, aerial);
            }
        }
    }

    private static final class PhenexCavernSoarGoal extends Goal {
        private final SoulfirePhenexEntity phenex;

        PhenexCavernSoarGoal(SoulfirePhenexEntity phenex) {
            this.phenex = phenex;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return this.phenex.getFlightMode() == MODE_SOARING
                    && this.phenex.getTarget() == null
                    && !this.phenex.getNavigation().isInProgress()
                    && this.phenex.getRandom().nextInt(16) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.phenex.getFlightMode() == MODE_SOARING
                    && this.phenex.getTarget() == null
                    && this.phenex.getNavigation().isInProgress();
        }

        @Override
        public void start() {
            Vec3 pos = this.phenex.position();
            double tx = pos.x + (this.phenex.getRandom().nextDouble() - 0.5D) * 20.0D;
            double tz = pos.z + (this.phenex.getRandom().nextDouble() - 0.5D) * 20.0D;
            double ty = pos.y + (this.phenex.getRandom().nextDouble() - 0.45D) * 6.0D;
            this.phenex.getNavigation().moveTo(tx, ty, tz, this.phenex.getEffectiveFlightSpeedMultiplier());
        }
    }
}
