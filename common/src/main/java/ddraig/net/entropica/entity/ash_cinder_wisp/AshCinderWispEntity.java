package ddraig.net.entropica.entity.ash_cinder_wisp;

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
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import java.util.List;

/**
 * Fauna 49 — Ash-Cinder Wisp (Will-o'-Wisp) (entropica:ash_cinder_wisp).
 * Passive frameless hovering magma-core wisp native to 6.3 Soulfire Fjords & 6.4 Ash Canyons:
 * - Floats slowly near magma pools and ash fissures with three orbiting charcoal-and-ember flakes
 *   and counter-orbiting diamond cinder sparks (idle / walk).
 * - Light Attraction & Spiraling Hover Dance (flutter_light): curiously drawn to torches, lanterns,
 *   and handheld lights, fluttering in a moth-like spiral around them.
 * - Ash Puff Blindness Spray (attack): passive, never deals damage, but fans ash dust when disturbed
 *   or hurt to blind nearby players/attackers for 2 seconds (40 ticks) before floating away.
 * - Eternal Spark Burnout (death): shudders and loses lift as its 3 charcoal flakes tumble flat onto
 *   the ground around a shrunken smoldering Cinder Wisp Core.
 */
public class AshCinderWispEntity extends PathfinderMob implements FlyingAnimal {

    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(AshCinderWispEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ASH_PUFFING =
            SynchedEntityData.defineId(AshCinderWispEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FLUTTERING_LIGHT =
            SynchedEntityData.defineId(AshCinderWispEntity.class, EntityDataSerializers.BOOLEAN);

    private static final byte EVENT_ASH_PUFF = 4;
    private static final byte EVENT_FLUTTER_LIGHT = 61;
    private static final byte EVENT_HURT_JOLT = 62;

    private static final int ASH_PUFF_DURATION_TICKS = 30;      // 1.50s attack animation
    private static final int ASH_PUFF_BURST_TICK = 19;          // 0.55s into animation: rapid flake spin burst
    private static final int ASH_BLINDNESS_TICKS = 40;          // 2.00s Blindness per Fauna 49 spec
    private static final int FLUTTER_LIGHT_DURATION_TICKS = 48; // 2.40s flutter_light animation
    private static final int HURT_DURATION_TICKS = 13;          // 0.65s hurt animation
    private static final int DEATH_DURATION_TICKS = 36;         // 1.80s death hold animation
    private static final int CORE_SHED_COOLDOWN_TICKS = 1200;

    private static final ResourceLocation CINDER_WISP_CORE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "cinder_wisp_core");

    private static final DustParticleOptions EMBER_SPARK_DUST = new DustParticleOptions(0xF97316, 0.58F);
    private static final DustParticleOptions ETERNAL_GOLD_DUST = new DustParticleOptions(0xFEF08A, 0.52F);
    private static final DustParticleOptions CHARCOAL_ASH_DUST = new DustParticleOptions(0x344054, 0.68F);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState flutterLightAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    private int ashPuffTicks = 0;
    private int ashPuffCooldown = 0;
    private int flutterLightTicks = 0;
    private int flutterLightCooldown = 0;
    private int hurtAnimTicks = 0;
    private int coreShedCooldown = 0;

    public AshCinderWispEntity(EntityType<? extends AshCinderWispEntity> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 16, true);
        this.setNoGravity(true);
        this.xpReward = 3;
        this.setPathfindingMalus(PathType.LAVA, 0.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
        this.setPathfindingMalus(PathType.WATER, 8.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.20D)
                .add(Attributes.FLYING_SPEED, 0.34D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 20.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, 0);
        builder.define(ASH_PUFFING, false);
        builder.define(FLUTTERING_LIGHT, false);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new PanicGoal(this, 1.30D));
        this.goalSelector.addGoal(1, new DisturbedAshPuffGoal(this));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.05D, AshCinderWispEntity::isCalmingLightOrFuel, false));
        this.goalSelector.addGoal(3, new LightSourceFlutterGoal(this));
        this.goalSelector.addGoal(4, new MagmaAndAshHoverGoal(this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason spawnReason,
            @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
        String biomePath = level.getBiome(this.blockPosition()).unwrapKey()
                .map(k -> k.location().getPath())
                .orElse("");
        if (biomePath.contains("soul") || biomePath.contains("fjord")) {
            this.setVariant(1);
        } else if (biomePath.contains("ash") || biomePath.contains("canyon")) {
            this.setVariant(2);
        } else {
            this.setVariant(level.getRandom().nextInt(3));
        }
        return data;
    }

    public int getVariant() {
        return this.entityData.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(VARIANT, Mth.clamp(variant, 0, 2));
    }

    public boolean isAshPuffing() {
        return this.entityData.get(ASH_PUFFING);
    }

    private void setAshPuffing(boolean value) {
        this.entityData.set(ASH_PUFFING, value);
    }

    public boolean isFlutteringLight() {
        return this.entityData.get(FLUTTERING_LIGHT);
    }

    private void setFlutteringLight(boolean value) {
        this.entityData.set(FLUTTERING_LIGHT, value);
    }

    @Override
    public boolean isFlying() {
        return !this.onGround();
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
        // Hovering Will-o'-Wisp never suffers fall impacts
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        if (source.is(DamageTypeTags.IS_FIRE)
                || source.is(DamageTypeTags.IS_FALL)
                || source.is(DamageTypes.HOT_FLOOR)
                || source.is(DamageTypes.LAVA)
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
            this.spawnClientAmbientSparks();
            return;
        }

        if (!this.isAlive()) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();

        if (this.ashPuffCooldown > 0) {
            this.ashPuffCooldown--;
        }
        if (this.flutterLightCooldown > 0) {
            this.flutterLightCooldown--;
        }
        if (this.hurtAnimTicks > 0) {
            this.hurtAnimTicks--;
        }
        if (this.coreShedCooldown > 0) {
            this.coreShedCooldown--;
        }

        // Gentle buoyant lift over lava pools & magma fissures so the wisp never sinks into lava
        BlockState feetState = serverLevel.getBlockState(this.blockPosition());
        BlockState belowState = serverLevel.getBlockState(this.blockPosition().below());
        if (feetState.getFluidState().is(FluidTags.LAVA) || belowState.getFluidState().is(FluidTags.LAVA)) {
            Vec3 vel = this.getDeltaMovement();
            if (vel.y < 0.08D) {
                this.setDeltaMovement(vel.x * 0.92D, 0.09D, vel.z * 0.92D);
            }
            if (this.tickCount % 30 == 0 && this.getHealth() < this.getMaxHealth()) {
                this.heal(1.0F);
            }
        } else if (belowState.is(Blocks.MAGMA_BLOCK) && this.tickCount % 40 == 0 && this.getHealth() < this.getMaxHealth()) {
            this.heal(0.5F);
        }

        // Tick Ash Puff Blindness Spray (1.50s = 30 ticks; burst lands at ashPuffTicks == 19)
        if (this.ashPuffTicks > 0) {
            this.ashPuffTicks--;
            if (this.ashPuffTicks == ASH_PUFF_BURST_TICK) {
                this.releaseAshBlindnessPuff(serverLevel);
            }
            if (this.ashPuffTicks <= 0) {
                this.setAshPuffing(false);
            }
        }

        // Tick curious torch/lantern flutter dance (2.40s = 48 ticks)
        if (this.flutterLightTicks > 0) {
            this.flutterLightTicks--;
            if (this.tickCount % 6 == 0) {
                serverLevel.sendParticles(ETERNAL_GOLD_DUST,
                        this.getX(), this.getY() + 0.95D, this.getZ(),
                        3, 0.28D, 0.22D, 0.28D, 0.01D);
            }
            if (this.flutterLightTicks <= 0) {
                this.setFlutteringLight(false);
            }
        }
    }

    private void updateClientAnimations() {
        if (this.isDeadOrDying()) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.attackAnimationState.stop();
            this.flutterLightAnimationState.stop();
            this.hurtAnimationState.stop();
            if (!this.deathAnimationState.isStarted()) {
                this.deathAnimationState.start(this.tickCount);
            }
            return;
        }

        double horizontalSpeedSqr = this.getDeltaMovement().horizontalDistanceSqr();
        boolean drifting = horizontalSpeedSqr > 1.8E-4D || this.walkAnimation.speed() > 0.06F;
        if (drifting) {
            this.walkAnimationState.startIfStopped(this.tickCount);
            this.idleAnimationState.stop();
        } else {
            this.idleAnimationState.startIfStopped(this.tickCount);
            this.walkAnimationState.stop();
        }
    }

    private void spawnClientAmbientSparks() {
        if (this.isDeadOrDying()) {
            if (this.random.nextInt(3) == 0) {
                this.level().addParticle(ParticleTypes.WHITE_ASH,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.55D,
                        this.getY() + 0.25D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.55D,
                        0.0D, 0.02D, 0.0D);
            }
            return;
        }

        if (this.random.nextInt(4) == 0) {
            double angle = (this.tickCount * 0.16D) + this.random.nextDouble() * Math.PI * 2.0D;
            double radius = 0.45D + this.random.nextDouble() * 0.22D;
            double px = this.getX() + Math.cos(angle) * radius;
            double py = this.getY() + 0.85D + (this.random.nextDouble() - 0.5D) * 0.30D;
            double pz = this.getZ() + Math.sin(angle) * radius;
            this.level().addParticle(ParticleTypes.WHITE_ASH, px, py, pz, 0.0D, -0.01D, 0.0D);
            if (this.random.nextInt(3) == 0) {
                this.level().addParticle(
                        this.getVariant() == 1 ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SMALL_FLAME,
                        px, py, pz, 0.0D, 0.012D, 0.0D);
            }
        }

        if (this.isFlutteringLight() && this.random.nextInt(2) == 0) {
            this.level().addParticle(ETERNAL_GOLD_DUST,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.65D,
                    this.getY() + 0.95D + (this.random.nextDouble() - 0.5D) * 0.35D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.65D,
                    0.0D, 0.02D, 0.0D);
        }
    }

    public void startAshPuffSpray(ServerLevel serverLevel) {
        if (this.ashPuffTicks > 0) {
            return;
        }
        this.ashPuffTicks = ASH_PUFF_DURATION_TICKS;
        this.ashPuffCooldown = 110;
        this.flutterLightTicks = 0;
        this.setFlutteringLight(false);
        this.setAshPuffing(true);
        serverLevel.broadcastEntityEvent(this, EVENT_ASH_PUFF);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.CAMPFIRE_CRACKLE, SoundSource.NEUTRAL, 0.95F, 1.45F);
    }

    private void releaseAshBlindnessPuff(ServerLevel serverLevel) {
        double cx = this.getX();
        double cy = this.getY() + 0.95D;
        double cz = this.getZ();

        serverLevel.playSound(null, cx, cy, cz,
                SoundEvents.FIRECHARGE_USE, SoundSource.NEUTRAL, 0.65F, 1.55F);
        serverLevel.playSound(null, cx, cy, cz,
                SoundEvents.BREEZE_IDLE_AIR, SoundSource.NEUTRAL, 0.75F, 1.35F);

        serverLevel.sendParticles(ParticleTypes.WHITE_ASH, cx, cy, cz, 42, 1.65D, 0.75D, 1.65D, 0.06D);
        serverLevel.sendParticles(ParticleTypes.ASH, cx, cy, cz, 32, 1.55D, 0.65D, 1.55D, 0.05D);
        serverLevel.sendParticles(CHARCOAL_ASH_DUST, cx, cy, cz, 22, 1.35D, 0.55D, 1.35D, 0.04D);
        serverLevel.sendParticles(EMBER_SPARK_DUST, cx, cy, cz, 14, 1.05D, 0.45D, 1.05D, 0.03D);

        // Passive defensive puff: 0 damage, blinds nearby disturbed players & hostile mobs for 2 seconds (40 ticks)
        List<LivingEntity> nearby = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(4.2D, 2.6D, 4.2D),
                e -> e.isAlive() && e != this && !(e instanceof AshCinderWispEntity)
        );

        Vec3 awayAccum = Vec3.ZERO;
        for (LivingEntity target : nearby) {
            if (target instanceof Player player) {
                if (player.isCreative() || player.isSpectator()) {
                    continue;
                }
                // Respect peaceful sneaking observers holding a lantern or torch unless they just struck the wisp
                if (player.isCrouching()
                        && this.getLastHurtByMob() != player
                        && (isCalmingLightOrFuel(player.getMainHandItem()) || isCalmingLightOrFuel(player.getOffhandItem()))) {
                    continue;
                }
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, ASH_BLINDNESS_TICKS, 0, false, true, true));
                awayAccum = awayAccum.add(this.position().subtract(player.position()));
            } else if (target instanceof Monster || target == this.getLastHurtByMob()) {
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, ASH_BLINDNESS_TICKS, 0, false, true, true));
                if (target instanceof Monster monster && monster.getTarget() == this) {
                    monster.setTarget(null);
                }
                awayAccum = awayAccum.add(this.position().subtract(target.position()));
            }
        }

        // Gently float upward and away after fanning the blinding ash burst
        if (awayAccum.lengthSqr() > 1.0E-4D) {
            Vec3 retreat = awayAccum.normalize().scale(0.32D);
            this.setDeltaMovement(retreat.x, 0.14D, retreat.z);
        } else {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.12D, 0.0D));
        }
    }

    public void startFlutterLightDance(ServerLevel serverLevel) {
        if (this.ashPuffTicks > 0 || this.flutterLightTicks > 0) {
            return;
        }
        this.flutterLightTicks = FLUTTER_LIGHT_DURATION_TICKS;
        this.flutterLightCooldown = 140 + this.random.nextInt(80);
        this.setFlutteringLight(true);
        serverLevel.broadcastEntityEvent(this, EVENT_FLUTTER_LIGHT);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 0.45F, 1.55F);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.CAMPFIRE_CRACKLE, SoundSource.NEUTRAL, 0.65F, 1.35F);
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(serverLevel, source, amount);
        if (hurt && this.isAlive()) {
            this.hurtAnimTicks = HURT_DURATION_TICKS;
            serverLevel.broadcastEntityEvent(this, EVENT_HURT_JOLT);
            serverLevel.sendParticles(ParticleTypes.WHITE_ASH,
                    this.getX(), this.getY() + 0.9D, this.getZ(),
                    14, 0.35D, 0.35D, 0.35D, 0.04D);

            // Immediately fan defensive blinding ash dust when struck!
            if (this.ashPuffTicks <= 0) {
                this.startAshPuffSpray(serverLevel);
            }
        }
        return hurt;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_ASH_PUFF) {
            this.flutterLightAnimationState.stop();
            this.attackAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_FLUTTER_LIGHT) {
            this.flutterLightAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_HURT_JOLT) {
            this.hurtAnimationState.start(this.tickCount);
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (!this.level().isClientSide() && this.deathTime == 1 && this.level() instanceof ServerLevel serverLevel) {
            this.setAshPuffing(false);
            this.setFlutteringLight(false);
            serverLevel.sendParticles(ParticleTypes.WHITE_ASH,
                    this.getX(), this.getY() + 0.85D, this.getZ(),
                    20, 0.45D, 0.35D, 0.45D, 0.03D);
            serverLevel.sendParticles(EMBER_SPARK_DUST,
                    this.getX(), this.getY() + 0.75D, this.getZ(),
                    12, 0.30D, 0.25D, 0.30D, 0.02D);
        }
        // Hold ground collapse so the 3 charcoal flakes rest flat around the shrunken smoldering core
        if (this.deathTime >= DEATH_DURATION_TICKS && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty() || !this.isAlive()) {
            return super.mobInteract(player, hand);
        }

        // Holding up a Torch or Lantern delights the wisp into a curious moth-like spiral dance without consuming it
        if (isHandheldTorchOrLantern(stack)) {
            if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                this.startFlutterLightDance(serverLevel);
                player.removeEffect(MobEffects.DARKNESS);
                player.removeEffect(MobEffects.BLINDNESS);
            }
            return InteractionResult.SUCCESS;
        }

        // Stoking its eternal spark with Coal, Charcoal, or Blaze Powder heals it and sheds a glowing cinder gift
        if (stack.is(Items.COAL) || stack.is(Items.CHARCOAL) || stack.is(Items.BLAZE_POWDER)) {
            if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.heal(4.0F);
                this.startFlutterLightDance(serverLevel);
                player.removeEffect(MobEffects.BLINDNESS);
                player.removeEffect(MobEffects.DARKNESS);
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 240, 0, true, true, true));

                serverLevel.sendParticles(ETERNAL_GOLD_DUST,
                        this.getX(), this.getY() + 0.95D, this.getZ(),
                        16, 0.35D, 0.30D, 0.35D, 0.03D);

                if (this.coreShedCooldown <= 0) {
                    this.coreShedCooldown = CORE_SHED_COOLDOWN_TICKS;
                    Item coreItem = BuiltInRegistries.ITEM.getOptional(CINDER_WISP_CORE_ID)
                            .orElse(ModItems.CINDER_WISP_CORE.get());
                    this.spawnAtLocation(serverLevel, new ItemStack(coreItem));
                    serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.FIRECHARGE_USE, SoundSource.NEUTRAL, 0.55F, 1.35F);
                }
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    public static boolean isHandheldTorchOrLantern(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.is(Items.TORCH)
                || stack.is(Items.SOUL_TORCH)
                || stack.is(Items.REDSTONE_TORCH)
                || stack.is(Items.LANTERN)
                || stack.is(Items.SOUL_LANTERN)) {
            return true;
        }
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.contains("torch") || path.contains("lantern") || path.contains("brazier");
    }

    public static boolean isCalmingLightOrFuel(ItemStack stack) {
        return isHandheldTorchOrLantern(stack)
                || stack.is(Items.COAL)
                || stack.is(Items.CHARCOAL)
                || stack.is(Items.BLAZE_POWDER);
    }

    public static boolean isAttractiveLightBlock(BlockState state) {
        if (state.isAir()) {
            return false;
        }
        if (state.is(Blocks.TORCH)
                || state.is(Blocks.WALL_TORCH)
                || state.is(Blocks.SOUL_TORCH)
                || state.is(Blocks.SOUL_WALL_TORCH)
                || state.is(Blocks.REDSTONE_TORCH)
                || state.is(Blocks.REDSTONE_WALL_TORCH)
                || state.is(Blocks.LANTERN)
                || state.is(Blocks.SOUL_LANTERN)
                || state.is(BlockTags.CAMPFIRES)
                || state.is(Blocks.SHROOMLIGHT)) {
            return true;
        }
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return path.contains("torch") || path.contains("lantern") || path.contains("brazier");
    }

    public static boolean checkAshCinderWispSpawnRules(
            EntityType<AshCinderWispEntity> type,
            LevelAccessor level,
            EntitySpawnReason spawnReason,
            BlockPos pos,
            RandomSource random) {
        if (spawnReason == EntitySpawnReason.SPAWNER || spawnReason == EntitySpawnReason.TRIAL_SPAWNER) {
            return true;
        }
        BlockState below = level.getBlockState(pos.below());
        String belowPath = BuiltInRegistries.BLOCK.getKey(below.getBlock()).getPath();
        boolean volcanicOrAshFloor = below.is(Blocks.MAGMA_BLOCK)
                || below.is(Blocks.BASALT)
                || below.is(Blocks.SMOOTH_BASALT)
                || below.is(Blocks.BLACKSTONE)
                || below.is(Blocks.SOUL_SAND)
                || below.is(Blocks.SOUL_SOIL)
                || below.is(Blocks.NETHERRACK)
                || below.is(Blocks.GRAVEL)
                || below.is(Blocks.TUFF)
                || below.getFluidState().is(FluidTags.LAVA)
                || belowPath.contains("ash")
                || belowPath.contains("basalt")
                || belowPath.contains("soul")
                || belowPath.contains("magma")
                || belowPath.contains("cinder");
        return volcanicOrAshFloor && level.getBlockState(pos).isAir();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("Variant", Codec.INT, this.getVariant());
        output.store("CoreShedCooldown", Codec.INT, this.coreShedCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setVariant(input.read("Variant", Codec.INT).orElse(0));
        this.coreShedCooldown = input.read("CoreShedCooldown", Codec.INT).orElse(0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.CAMPFIRE_CRACKLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.BLAZE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.FIRE_EXTINGUISH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.45F;
    }

    @Override
    public float getVoicePitch() {
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.15F + 1.38F;
    }

    /**
     * Passive self-defense when disturbed: if a non-sneaking player without a calming torch/lantern
     * rushes or crowds right into the wisp, it flutters ash dust to blind them for 2 seconds and floats back.
     */
    private static final class DisturbedAshPuffGoal extends Goal {
        private final AshCinderWispEntity wisp;

        DisturbedAshPuffGoal(AshCinderWispEntity wisp) {
            this.wisp = wisp;
        }

        @Override
        public boolean canUse() {
            if (this.wisp.ashPuffTicks > 0 || this.wisp.ashPuffCooldown > 0) {
                return false;
            }
            if (!(this.wisp.level() instanceof ServerLevel serverLevel)) {
                return false;
            }
            List<Player> crowding = serverLevel.getEntitiesOfClass(
                    Player.class,
                    this.wisp.getBoundingBox().inflate(2.1D, 1.6D, 2.1D),
                    p -> p.isAlive()
                            && !p.isCreative()
                            && !p.isSpectator()
                            && !p.isCrouching()
                            && p.isSprinting()
                            && !isCalmingLightOrFuel(p.getMainHandItem())
                            && !isCalmingLightOrFuel(p.getOffhandItem())
            );
            return !crowding.isEmpty();
        }

        @Override
        public void start() {
            if (this.wisp.level() instanceof ServerLevel serverLevel) {
                this.wisp.startAshPuffSpray(serverLevel);
            }
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }
    }

    /**
     * Curious moth-like attraction to placed Torches, Lanterns, and Braziers:
     * approaches within 1.8 blocks and spirals gently around the light while playing flutter_light.
     */
    private static final class LightSourceFlutterGoal extends Goal {
        private final AshCinderWispEntity wisp;
        @Nullable
        private BlockPos targetLightPos;
        private int orbitTicks = 0;
        private double orbitAngle = 0.0D;
        private int scanCooldown = 0;

        LightSourceFlutterGoal(AshCinderWispEntity wisp) {
            this.wisp = wisp;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.wisp.ashPuffTicks > 0 || this.wisp.isPanicking()) {
                return false;
            }
            if (this.scanCooldown > 0) {
                this.scanCooldown--;
                return false;
            }
            this.scanCooldown = 35 + this.wisp.getRandom().nextInt(35);
            this.targetLightPos = this.findNearbyTorchOrLantern();
            return this.targetLightPos != null;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.targetLightPos == null || this.wisp.ashPuffTicks > 0 || this.wisp.isPanicking()) {
                return false;
            }
            if (this.orbitTicks >= 110) {
                return false;
            }
            return isAttractiveLightBlock(this.wisp.level().getBlockState(this.targetLightPos));
        }

        @Override
        public void start() {
            this.orbitTicks = 0;
            this.orbitAngle = this.wisp.getRandom().nextDouble() * Math.PI * 2.0D;
        }

        @Override
        public void stop() {
            this.targetLightPos = null;
            this.wisp.getNavigation().stop();
            this.scanCooldown = 90 + this.wisp.getRandom().nextInt(70);
        }

        @Override
        public void tick() {
            if (this.targetLightPos == null) {
                return;
            }
            Vec3 lightCenter = Vec3.atCenterOf(this.targetLightPos);
            this.wisp.getLookControl().setLookAt(lightCenter.x, lightCenter.y, lightCenter.z, 24.0F, 24.0F);

            double distSqr = this.wisp.distanceToSqr(lightCenter);
            this.orbitTicks++;

            if (distSqr > 6.25D) {
                this.wisp.getNavigation().moveTo(lightCenter.x, lightCenter.y + 0.35D, lightCenter.z, 1.0D);
                return;
            }

            // Spiraling hover dance around the torch or lantern!
            this.orbitAngle += 0.11D;
            double radius = 1.45D + Math.sin(this.orbitTicks * 0.12D) * 0.25D;
            double destX = lightCenter.x + Math.cos(this.orbitAngle) * radius;
            double destY = lightCenter.y + 0.25D + Math.sin(this.orbitTicks * 0.18D) * 0.30D;
            double destZ = lightCenter.z + Math.sin(this.orbitAngle) * radius;
            this.wisp.getMoveControl().setWantedPosition(destX, destY, destZ, 0.95D);

            if (this.wisp.flutterLightCooldown <= 0
                    && this.wisp.flutterLightTicks <= 0
                    && this.wisp.level() instanceof ServerLevel serverLevel) {
                this.wisp.startFlutterLightDance(serverLevel);
            }
        }

        private @Nullable BlockPos findNearbyTorchOrLantern() {
            BlockPos origin = this.wisp.blockPosition();
            Level level = this.wisp.level();
            BlockPos best = null;
            double bestDistSqr = Double.MAX_VALUE;
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

            for (int dx = -10; dx <= 10; dx++) {
                for (int dy = -4; dy <= 5; dy++) {
                    for (int dz = -10; dz <= 10; dz++) {
                        cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                        if (!isAttractiveLightBlock(level.getBlockState(cursor))) {
                            continue;
                        }
                        double d2 = origin.distSqr(cursor);
                        if (d2 < bestDistSqr) {
                            bestDistSqr = d2;
                            best = cursor.immutable();
                        }
                    }
                }
            }
            return best;
        }
    }

    /**
     * Gentle floating drift near magma pools, lava seams, and ash fissures (1.0 - 2.6 blocks above surfaces).
     */
    private static final class MagmaAndAshHoverGoal extends Goal {
        private final AshCinderWispEntity wisp;
        private double targetX;
        private double targetY;
        private double targetZ;

        MagmaAndAshHoverGoal(AshCinderWispEntity wisp) {
            this.wisp = wisp;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.wisp.getNavigation().isInProgress() && this.wisp.getRandom().nextInt(10) != 0) {
                return false;
            }
            if (this.wisp.getRandom().nextInt(18) != 0) {
                return false;
            }
            Vec3 dest = this.pickHoverDestination();
            if (dest == null) {
                return false;
            }
            this.targetX = dest.x;
            this.targetY = dest.y;
            this.targetZ = dest.z;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.wisp.getNavigation().isInProgress()
                    && this.wisp.distanceToSqr(this.targetX, this.targetY, this.targetZ) > 1.2D;
        }

        @Override
        public void start() {
            this.wisp.getNavigation().moveTo(this.targetX, this.targetY, this.targetZ, 0.85D);
        }

        private @Nullable Vec3 pickHoverDestination() {
            RandomSource rng = this.wisp.getRandom();
            Level level = this.wisp.level();
            BlockPos origin = this.wisp.blockPosition();
            Vec3 fallback = null;

            for (int attempt = 0; attempt < 10; attempt++) {
                int dx = rng.nextInt(13) - 6;
                int dz = rng.nextInt(13) - 6;
                int dy = rng.nextInt(7) - 3;
                BlockPos candidate = origin.offset(dx, dy, dz);
                if (!level.getBlockState(candidate).isAir()) {
                    continue;
                }

                // Prefer hovering 1-3 blocks above magma blocks, lava pools, or solid ash/stone ground
                BlockPos surface = candidate;
                boolean foundSurface = false;
                boolean warmMagmaPool = false;
                for (int down = 1; down <= 4; down++) {
                    BlockPos check = candidate.below(down);
                    BlockState st = level.getBlockState(check);
                    if (st.is(Blocks.MAGMA_BLOCK) || st.getFluidState().is(FluidTags.LAVA)) {
                        surface = check;
                        foundSurface = true;
                        warmMagmaPool = true;
                        break;
                    }
                    if (!st.isAir()) {
                        surface = check;
                        foundSurface = true;
                        break;
                    }
                }

                if (!foundSurface) {
                    continue;
                }

                Vec3 hoverPos = new Vec3(
                        surface.getX() + 0.5D,
                        surface.getY() + 1.35D + rng.nextDouble() * 1.15D,
                        surface.getZ() + 0.5D
                );
                if (level.getBlockState(BlockPos.containing(hoverPos)).isAir()) {
                    if (warmMagmaPool) {
                        return hoverPos;
                    }
                    if (fallback == null) {
                        fallback = hoverPos;
                    }
                }
            }
            return fallback;
        }
    }
}
