package ddraig.net.entropica.entity.orthrus_magma_beast;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariant;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariants;
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

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * Fauna 48 — Orthrus Magma-Beast (Two-Headed Hound) (entropica:orthrus_magma_beast).
 * Hostile dire two-headed volcanic hound native to 6.1 Magma Caverns & 6.4 Ash Canyons:
 * - Trots across volcanic rock (walk) and charges players across basalt delves in a fast bounding gallop (run).
 * - Coils low and leaps forward to bite with both horned heads (attack), dealing 8.0 fire damage and igniting
 *   the target, or lashes out in close quarters with an alternating Left-then-Right twin-jaw lunge (snap_combo).
 * - Throws both heads skyward in a fiery subterranean howl (howl), and recoils snarling when struck (hurt).
 * - Immune to fire and lava, regenerating speed on magma blocks, and collapses onto its volcanic belly with
 *   splayed legs and both horned heads resting apart if slain (death).
 */
public class OrthrusMagmaBeastEntity extends Monster {

    private static final EntityDataAccessor<Boolean> CHARGING =
            SynchedEntityData.defineId(OrthrusMagmaBeastEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> MAGMA_ENERGIZED =
            SynchedEntityData.defineId(OrthrusMagmaBeastEntity.class, EntityDataSerializers.BOOLEAN);

    private static final byte EVENT_DOUBLE_BITE_LEAP = 4;
    private static final byte EVENT_SNAP_COMBO = 61;
    private static final byte EVENT_HOWL = 62;
    private static final byte EVENT_HURT_SNARL = 63;

    private static final int DOUBLE_BITE_DURATION_TICKS = 27; // 1.35s animation
    private static final int DOUBLE_BITE_LEAP_TICK = 6;
    private static final int DOUBLE_BITE_IMPACT_TICK = 13;

    private static final int SNAP_COMBO_DURATION_TICKS = 26;  // 1.30s animation
    private static final int SNAP_LEFT_TICK = 8;
    private static final int SNAP_RIGHT_TICK = 17;

    private static final int HOWL_DURATION_TICKS = 40;        // 2.00s animation
    private static final int HURT_DURATION_TICKS = 13;        // 0.65s animation
    private static final int DEATH_DURATION_TICKS = 38;       // 1.90s animation
    private static final int PELT_SHED_COOLDOWN_TICKS = 1200;

    private static final ResourceLocation MAGMA_ORTHRUS_PELT_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "magma_orthrus_pelt");

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState runAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState snapComboAnimationState = new AnimationState();
    public final AnimationState howlAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    private int doubleBiteTicks = 0;
    private int snapComboTicks = 0;
    private int howlTicks = 0;
    private int combatCooldown = 0;
    private int howlCooldown = 80;
    private boolean hadTargetLastTick = false;
    private int peltShedCooldown = 0;

    public OrthrusMagmaBeastEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 10;
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.LAVA, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.31D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.35D)
                .add(Attributes.FOLLOW_RANGE, 28.0D)
                .add(Attributes.STEP_HEIGHT, 1.2D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CHARGING, false);
        builder.define(MAGMA_ENERGIZED, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new OrthrusChargeAndBiteGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    public boolean isCharging() {
        return this.entityData.get(CHARGING);
    }

    private void setCharging(boolean charging) {
        this.entityData.set(CHARGING, charging);
    }

    public boolean isMagmaEnergized() {
        return this.entityData.get(MAGMA_ENERGIZED);
    }

    private void setMagmaEnergized(boolean energized) {
        this.entityData.set(MAGMA_ENERGIZED, energized);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean isSensitiveToWater() {
        return true;
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        if (source.is(DamageTypeTags.IS_FIRE)
                || source.is(DamageTypes.LAVA)
                || source.is(DamageTypes.HOT_FLOOR)
                || source.is(DamageTypes.IN_FIRE)
                || source.is(DamageTypes.ON_FIRE)) {
            return true;
        }
        return super.isInvulnerableTo(level, source);
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        if (fallDistance <= 6.5D) {
            return false;
        }
        return super.causeFallDamage(fallDistance - 4.5D, multiplier * 0.5F, source);
    }

    public static boolean isOrthrusSpawnSurface(BlockGetter level, BlockPos belowPos, BlockState belowState) {
        if (belowState.getFluidState().is(FluidTags.LAVA)) {
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
                || belowState.is(Blocks.DEEPSLATE)
                || path.contains("magma")
                || path.contains("basalt")
                || path.contains("ash")
                || belowState.isFaceSturdy(level, belowPos, Direction.UP);
    }

    public static boolean checkOrthrusMagmaBeastSpawnRules(
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
        BlockPos belowPos = pos.below();
        return isOrthrusSpawnSurface(level, belowPos, level.getBlockState(belowPos));
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        BlockPos belowPos = this.blockPosition().below();
        return isOrthrusSpawnSurface(level, belowPos, level.getBlockState(belowPos));
    }

    private boolean isStandingOnOrNearMagmaBlock() {
        BlockPos pos = this.blockPosition();
        for (int dy = 0; dy <= 1; dy++) {
            BlockState st = this.level().getBlockState(pos.below(dy));
            String path = BuiltInRegistries.BLOCK.getKey(st.getBlock()).getPath();
            if (st.is(Blocks.MAGMA_BLOCK) || path.contains("magma") || st.getFluidState().is(FluidTags.LAVA)) {
                return true;
            }
        }
        return false;
    }

    public void triggerSubterraneanHowl(ServerLevel serverLevel) {
        if (this.howlTicks > 0 || this.doubleBiteTicks > 0 || this.snapComboTicks > 0 || !this.isAlive()) {
            return;
        }
        this.howlTicks = HOWL_DURATION_TICKS;
        this.howlCooldown = 260 + this.random.nextInt(180);
        this.getNavigation().stop();
        serverLevel.broadcastEntityEvent(this, EVENT_HOWL);

        WolfSoundVariant sadSet = SoundEvents.WOLF_SOUNDS.get(WolfSoundVariants.SoundSet.SAD);
        SoundEvent howlSound = (sadSet != null) ? sadSet.whineSound().value() : SoundEvents.BLAZE_AMBIENT;
        serverLevel.playSound(null, this.getX(), this.getY() + 1.4D, this.getZ(),
                howlSound, SoundSource.HOSTILE, 1.15F, 0.58F);
        serverLevel.playSound(null, this.getX(), this.getY() + 1.4D, this.getZ(),
                SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 0.75F, 0.65F);
        serverLevel.sendParticles(ParticleTypes.FLAME,
                this.getX(), this.getY() + 1.75D, this.getZ(),
                22, 0.45D, 0.35D, 0.45D, 0.04D);

        // Rally nearby Orthrus packmates with bounding speed!
        List<OrthrusMagmaBeastEntity> pack = serverLevel.getEntitiesOfClass(
                OrthrusMagmaBeastEntity.class,
                this.getBoundingBox().inflate(16.0D),
                o -> o.isAlive()
        );
        LivingEntity target = this.getTarget();
        for (OrthrusMagmaBeastEntity mate : pack) {
            mate.addEffect(new MobEffectInstance(MobEffects.SPEED, 160, 0), this);
            if (target != null && mate.getTarget() == null) {
                mate.setTarget(target);
            }
        }
    }

    public void startDoubleBiteLeap(ServerLevel serverLevel) {
        this.doubleBiteTicks = DOUBLE_BITE_DURATION_TICKS;
        this.combatCooldown = DOUBLE_BITE_DURATION_TICKS + 6;
        serverLevel.broadcastEntityEvent(this, EVENT_DOUBLE_BITE_LEAP);
        serverLevel.playSound(null, this.getX(), this.getY() + 1.1D, this.getZ(),
                getWolfSoundVariant().growlSound().value(), SoundSource.HOSTILE, 1.0F, 0.65F);
    }

    private void executeDoubleBiteImpact(ServerLevel serverLevel) {
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive() || this.distanceToSqr(target) > 18.0D) {
            return;
        }
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE); // 8.0F
        target.igniteForSeconds(5.0F);
        boolean hurt = false;
        if (!target.fireImmune()) {
            hurt = target.hurtServer(serverLevel, this.damageSources().inFire(), damage);
        }
        if (!hurt) {
            target.hurtServer(serverLevel, this.damageSources().mobAttack(this), damage);
        }
        serverLevel.playSound(null, target.getX(), target.getY() + 0.7D, target.getZ(),
                SoundEvents.EVOKER_FANGS_ATTACK, SoundSource.HOSTILE, 0.90F, 0.85F);
        serverLevel.sendParticles(ParticleTypes.LAVA,
                target.getX(), target.getY() + 0.8D, target.getZ(),
                10, 0.40D, 0.30D, 0.40D, 0.04D);
    }

    public void startTwinSnapCombo(ServerLevel serverLevel) {
        this.snapComboTicks = SNAP_COMBO_DURATION_TICKS;
        this.combatCooldown = SNAP_COMBO_DURATION_TICKS + 6;
        serverLevel.broadcastEntityEvent(this, EVENT_SNAP_COMBO);
        serverLevel.playSound(null, this.getX(), this.getY() + 1.1D, this.getZ(),
                getWolfSoundVariant().growlSound().value(), SoundSource.HOSTILE, 0.95F, 0.78F);
    }

    private void executeSingleHeadSnap(ServerLevel serverLevel, boolean rightHead) {
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive() || this.distanceToSqr(target) > 14.5D) {
            return;
        }
        float halfDamage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.5F; // 4.0F + 4.0F = 8.0F
        target.igniteForSeconds(rightHead ? 4.0F : 3.0F);
        if (!target.hurtServer(serverLevel, this.damageSources().mobAttack(this), halfDamage)) {
            target.hurtServer(serverLevel, this.damageSources().inFire(), halfDamage);
        }
        serverLevel.sendParticles(ParticleTypes.FLAME,
                target.getX(), target.getY() + 0.75D, target.getZ(),
                10, 0.30D, 0.25D, 0.30D, 0.03D);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean isMagmaMeatOffering = stack.is(Items.MAGMA_CREAM)
                || stack.is(Items.COOKED_BEEF)
                || stack.is(Items.COOKED_PORKCHOP)
                || stack.is(Items.COOKED_MUTTON);
        if (isMagmaMeatOffering && this.isAlive() && this.peltShedCooldown <= 0) {
            if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                stack.consume(1, player);
                this.peltShedCooldown = PELT_SHED_COOLDOWN_TICKS;
                this.heal(5.0F);
                serverLevel.playSound(null, this.getX(), this.getY() + 1.1D, this.getZ(),
                        getWolfSoundVariant().pantSound().value(), SoundSource.HOSTILE, 0.90F, 0.75F);
                serverLevel.sendParticles(ParticleTypes.FLAME,
                        this.getX(), this.getY() + 1.1D, this.getZ(),
                        18, 0.55D, 0.35D, 0.55D, 0.03D);

                Optional<Item> peltOpt = ModItems.MAGMA_ORTHRUS_PELT.isPresent()
                        ? Optional.of(ModItems.MAGMA_ORTHRUS_PELT.get())
                        : BuiltInRegistries.ITEM.getOptional(MAGMA_ORTHRUS_PELT_ID);
                peltOpt.ifPresent(pelt ->
                        this.spawnAtLocation(serverLevel, new ItemStack(pelt, 1), 0.75F));
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(serverLevel, source, amount);
        if (hurt && this.isAlive() && this.doubleBiteTicks <= 0 && this.snapComboTicks <= 0) {
            serverLevel.broadcastEntityEvent(this, EVENT_HURT_SNARL);
        }
        return hurt;
    }

    @Override
    protected void tickDeath() {
        ++this.deathTime;
        if (this.level().isClientSide() && this.deathTime % 4 == 0) {
            this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 1.1D,
                    this.getY() + 0.55D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 1.1D,
                    0.0D, 0.03D, 0.0D);
        }
        if (this.deathTime >= DEATH_DURATION_TICKS && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_DOUBLE_BITE_LEAP) {
            this.hurtAnimationState.stop();
            this.snapComboAnimationState.stop();
            this.howlAnimationState.stop();
            this.attackAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_SNAP_COMBO) {
            this.hurtAnimationState.stop();
            this.attackAnimationState.stop();
            this.howlAnimationState.stop();
            this.snapComboAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_HOWL) {
            this.attackAnimationState.stop();
            this.snapComboAnimationState.stop();
            this.howlAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_HURT_SNARL) {
            this.hurtAnimationState.start(this.tickCount);
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            if (this.combatCooldown > 0) {
                this.combatCooldown--;
            }
            if (this.howlCooldown > 0) {
                this.howlCooldown--;
            }
            if (this.peltShedCooldown > 0) {
                this.peltShedCooldown--;
            }

            if (this.isAlive()) {
                LivingEntity target = this.getTarget();
                boolean hasTarget = target != null && target.isAlive();
                this.setCharging(hasTarget);

                // Subterranean howl when spotting a fresh target or occasionally in caverns
                if (hasTarget && !this.hadTargetLastTick && this.howlCooldown <= 0) {
                    this.triggerSubterraneanHowl(serverLevel);
                } else if (!hasTarget && this.howlCooldown <= 0 && this.random.nextInt(140) == 0) {
                    this.triggerSubterraneanHowl(serverLevel);
                }
                this.hadTargetLastTick = hasTarget;

                // Regenerates speed on magma blocks (and stays buoyant in lava)!
                boolean onMagma = this.isStandingOnOrNearMagmaBlock() || this.isInLava();
                this.setMagmaEnergized(onMagma);
                if (onMagma) {
                    if (this.tickCount % 20 == 0) {
                        this.addEffect(new MobEffectInstance(MobEffects.SPEED, 60, 1, false, false), this);
                    }
                    if (this.tickCount % 40 == 0 && this.getHealth() < this.getMaxHealth()) {
                        this.heal(1.0F);
                    }
                    if (this.isInLava() && this.getFluidHeight(FluidTags.LAVA) > 0.65D) {
                        Vec3 vel = this.getDeltaMovement();
                        this.setDeltaMovement(vel.x * 0.94D, Math.max(vel.y, 0.07D), vel.z * 0.94D);
                    }
                }

                if (this.howlTicks > 0) {
                    this.howlTicks--;
                }

                // Igniting Double-Bite Leap sequence
                if (this.doubleBiteTicks > 0) {
                    int elapsed = DOUBLE_BITE_DURATION_TICKS - this.doubleBiteTicks;
                    this.doubleBiteTicks--;
                    if (elapsed == DOUBLE_BITE_LEAP_TICK && target != null && target.isAlive()) {
                        Vec3 dir = target.position().subtract(this.position());
                        double horiz = Math.hypot(dir.x, dir.z);
                        if (horiz > 1.0E-4D) {
                            Vec3 vel = this.getDeltaMovement();
                            this.setDeltaMovement(
                                    vel.x + (dir.x / horiz) * 0.50D,
                                    Math.max(vel.y, 0.25D),
                                    vel.z + (dir.z / horiz) * 0.50D
                            );
                        }
                    } else if (elapsed == DOUBLE_BITE_IMPACT_TICK) {
                        this.executeDoubleBiteImpact(serverLevel);
                    }
                }

                // Alternating Left-then-Right Twin-Jaw Snap Combo sequence
                if (this.snapComboTicks > 0) {
                    int elapsed = SNAP_COMBO_DURATION_TICKS - this.snapComboTicks;
                    this.snapComboTicks--;
                    if (elapsed == SNAP_LEFT_TICK) {
                        this.executeSingleHeadSnap(serverLevel, false);
                    } else if (elapsed == SNAP_RIGHT_TICK) {
                        this.executeSingleHeadSnap(serverLevel, true);
                    }
                }
            } else {
                this.setCharging(false);
                this.setMagmaEnergized(false);
            }
        } else if (this.level().isClientSide()) {
            this.setupClientAnimationsAndParticles();
        }
    }

    private void setupClientAnimationsAndParticles() {
        if (!this.isAlive()) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.runAnimationState.stop();
            this.attackAnimationState.stop();
            this.snapComboAnimationState.stop();
            this.howlAnimationState.stop();
            this.hurtAnimationState.stop();
            this.deathAnimationState.startIfStopped(this.tickCount);
            return;
        }

        if (this.attackAnimationState.isStarted()
                && this.attackAnimationState.getTimeInMillis(this.tickCount) >= DOUBLE_BITE_DURATION_TICKS * 50L) {
            this.attackAnimationState.stop();
        }
        if (this.snapComboAnimationState.isStarted()
                && this.snapComboAnimationState.getTimeInMillis(this.tickCount) >= SNAP_COMBO_DURATION_TICKS * 50L) {
            this.snapComboAnimationState.stop();
        }
        if (this.howlAnimationState.isStarted()
                && this.howlAnimationState.getTimeInMillis(this.tickCount) >= HOWL_DURATION_TICKS * 50L) {
            this.howlAnimationState.stop();
        }
        if (this.hurtAnimationState.isStarted()
                && this.hurtAnimationState.getTimeInMillis(this.tickCount) >= HURT_DURATION_TICKS * 50L) {
            this.hurtAnimationState.stop();
        }

        double horizSpeedSqr = this.getDeltaMovement().horizontalDistanceSqr();
        if (horizSpeedSqr > 2.5E-4D) {
            this.idleAnimationState.stop();
            if (this.isCharging() || horizSpeedSqr > 0.022D) {
                this.walkAnimationState.stop();
                this.runAnimationState.startIfStopped(this.tickCount);
            } else {
                this.runAnimationState.stop();
                this.walkAnimationState.startIfStopped(this.tickCount);
            }
        } else {
            this.walkAnimationState.stop();
            this.runAnimationState.stop();
            this.idleAnimationState.startIfStopped(this.tickCount);
        }

        // Blazing X-nexus & magma-brush tail embers
        if (this.tickCount % (this.isMagmaEnergized() ? 3 : 8) == 0) {
            this.level().addParticle(
                    this.random.nextInt(3) == 0 ? ParticleTypes.FLAME : ParticleTypes.SMOKE,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 1.1D,
                    this.getY() + 1.15D + (this.random.nextDouble() - 0.5D) * 0.45D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 1.1D,
                    0.0D, 0.02D, 0.0D
            );
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("PeltShedCooldown", Codec.INT, this.peltShedCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.peltShedCooldown = input.read("PeltShedCooldown", Codec.INT).orElse(0);
    }

    private static WolfSoundVariant getWolfSoundVariant() {
        WolfSoundVariant big = SoundEvents.WOLF_SOUNDS.get(WolfSoundVariants.SoundSet.BIG);
        if (big != null) {
            return big;
        }
        return SoundEvents.WOLF_SOUNDS.get(WolfSoundVariants.SoundSet.CLASSIC);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return getWolfSoundVariant().growlSound().value();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return getWolfSoundVariant().hurtSound().value();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return getWolfSoundVariant().deathSound().value();
    }

    @Override
    public float getVoicePitch() {
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.12F + 0.62F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.WOLF_STEP, 0.35F, 0.72F);
    }

    private static final class OrthrusChargeAndBiteGoal extends Goal {
        private final OrthrusMagmaBeastEntity orthrus;
        private int repathTicks = 0;

        OrthrusChargeAndBiteGoal(OrthrusMagmaBeastEntity orthrus) {
            this.orthrus = orthrus;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.orthrus.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.orthrus.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }
            if (target instanceof Player p && (p.isCreative() || p.isSpectator())) {
                return false;
            }
            return this.orthrus.distanceToSqr(target) <= 900.0D;
        }

        @Override
        public void stop() {
            this.orthrus.getNavigation().stop();
        }

        @Override
        public void tick() {
            LivingEntity target = this.orthrus.getTarget();
            if (target == null || !(this.orthrus.level() instanceof ServerLevel serverLevel)) {
                return;
            }
            if (this.orthrus.howlTicks > 0) {
                return;
            }

            this.orthrus.getLookControl().setLookAt(target, 32.0F, 32.0F);
            double distSqr = this.orthrus.distanceToSqr(target);

            if (--this.repathTicks <= 0) {
                this.repathTicks = 4 + this.orthrus.getRandom().nextInt(5);
                this.orthrus.getNavigation().moveTo(target, 1.30D);
            }

            if (this.orthrus.combatCooldown > 0
                    || this.orthrus.doubleBiteTicks > 0
                    || this.orthrus.snapComboTicks > 0
                    || !this.orthrus.hasLineOfSight(target)) {
                return;
            }

            // Pounce double-bite at medium leap range; alternating Left-Right snap combo in close quarters!
            if (distSqr > 10.5D && distSqr <= 30.25D) {
                this.orthrus.startDoubleBiteLeap(serverLevel);
            } else if (distSqr <= 11.5D) {
                if (this.orthrus.getRandom().nextBoolean()) {
                    this.orthrus.startTwinSnapCombo(serverLevel);
                } else {
                    this.orthrus.startDoubleBiteLeap(serverLevel);
                }
            }
        }
    }
}
