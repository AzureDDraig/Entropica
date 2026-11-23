package ddraig.net.entropica.entity.magma_cherufe;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.ThrownSplashPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
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
import java.util.List;
import java.util.Optional;

/**
 * Fauna 44 — Magma Cherufe (Lava Lizard) (entropica:magma_cherufe).
 * Hostile armored volcanic reptile native to 6.1 Magma Caverns & 6.2 Basalt Delves:
 * - Crawls across volcanic rock (walk) and effortlessly climbs vertical basalt cliffs near lava rivers (climb).
 * - Molten Jaw Clamp (attack): lunges forward to clamp prey in interlocking fangs for 8.0 damage + tearing shake.
 * - Igniting Basalt Tail Whip (tail_whip): spins to lash foes with its 5-segment basalt tail for 4.0 fire damage + ignition.
 * - Fire & Lava Immunity + Lava Bath Regeneration: takes 0 fire/lava damage and regenerates 2.0 HP/sec in lava.
 * - Collapses flat onto its cooling basalt belly when slain (death).
 */
public class MagmaCherufeEntity extends Monster {

    private static final EntityDataAccessor<Boolean> CLIMBING =
            SynchedEntityData.defineId(MagmaCherufeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> LAVA_BATHING =
            SynchedEntityData.defineId(MagmaCherufeEntity.class, EntityDataSerializers.BOOLEAN);

    private static final byte EVENT_JAW_CLAMP = 4;
    private static final byte EVENT_TAIL_WHIP = 61;
    private static final byte EVENT_HURT_SHUDDER = 62;

    private static final int JAW_CLAMP_DURATION_TICKS = 29; // 1.45s animation
    private static final int JAW_LUNGE_TICK = 6;
    private static final int JAW_IMPACT_TICK = 12;

    private static final int TAIL_WHIP_DURATION_TICKS = 31; // 1.55s animation
    private static final int TAIL_WHIP_IMPACT_TICK = 13;

    private static final int HURT_DURATION_TICKS = 14;      // 0.70s animation
    private static final int DEATH_DURATION_TICKS = 48;     // 2.40s animation
    private static final int CRUST_SHED_COOLDOWN_TICKS = 1200;

    private static final ResourceLocation CHERUFE_BASALT_CRUST_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "cherufe_basalt_crust");

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState climbAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState tailWhipAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    private int jawClampTicks = 0;
    private int tailWhipTicks = 0;
    private int combatCooldown = 0;
    private int tailWhipCooldown = 0;
    private int strikeCounter = 0;
    private int crustShedCooldown = 0;

    public MagmaCherufeEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 10;
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.LAVA, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 105.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.45D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.STEP_HEIGHT, 1.1D);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WallClimberNavigation(this, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CLIMBING, false);
        builder.define(LAVA_BATHING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new CherufeCombatGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.90D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    public boolean isClimbing() {
        return this.entityData.get(CLIMBING);
    }

    public void setClimbing(boolean climbing) {
        this.entityData.set(CLIMBING, climbing);
    }

    public boolean isLavaBathing() {
        return this.entityData.get(LAVA_BATHING);
    }

    public void setLavaBathing(boolean bathing) {
        this.entityData.set(LAVA_BATHING, bathing);
    }

    @Override
    public boolean onClimbable() {
        return this.isClimbing();
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
        if (fallDistance <= 6.0D || this.isClimbing()) {
            return false;
        }
        return super.causeFallDamage(fallDistance - 4.0D, multiplier * 0.5F, source);
    }

    public static boolean isVolcanicSpawnSurface(BlockGetter level, BlockPos belowPos, BlockState belowState) {
        if (belowState.getFluidState().is(FluidTags.LAVA)) {
            return true;
        }
        if (belowState.isAir()) {
            return false;
        }
        String path = BuiltInRegistries.BLOCK.getKey(belowState.getBlock()).getPath();
        return belowState.is(Blocks.BASALT)
                || belowState.is(Blocks.SMOOTH_BASALT)
                || belowState.is(Blocks.POLISHED_BASALT)
                || belowState.is(Blocks.BLACKSTONE)
                || belowState.is(Blocks.MAGMA_BLOCK)
                || belowState.is(Blocks.NETHERRACK)
                || belowState.is(Blocks.DEEPSLATE)
                || belowState.is(Blocks.TUFF)
                || belowState.is(Blocks.OBSIDIAN)
                || path.contains("basalt")
                || path.contains("magma")
                || path.contains("volcanic")
                || belowState.isFaceSturdy(level, belowPos, Direction.UP);
    }

    public static boolean checkMagmaCherufeSpawnRules(
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
        return isVolcanicSpawnSurface(level, belowPos, level.getBlockState(belowPos));
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        BlockPos belowPos = this.blockPosition().below();
        return isVolcanicSpawnSurface(level, belowPos, level.getBlockState(belowPos));
    }

    private boolean canClimbAdjacentSurface() {
        BlockPos base = this.blockPosition();
        for (int dy = 0; dy <= 1; dy++) {
            BlockPos center = base.above(dy);
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos adj = center.relative(dir);
                BlockState st = this.level().getBlockState(adj);
                if (!st.isAir() && (st.blocksMotion() || st.isFaceSturdy(this.level(), adj, dir.getOpposite()))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void tryThermalQuenchShedCrust(ServerLevel serverLevel) {
        if (this.crustShedCooldown > 0 || !this.isAlive()) {
            return;
        }
        this.crustShedCooldown = CRUST_SHED_COOLDOWN_TICKS;
        serverLevel.playSound(null, this.getX(), this.getY() + 0.6D, this.getZ(),
                SoundEvents.LAVA_EXTINGUISH, SoundSource.HOSTILE, 0.90F, 0.95F);
        serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                this.getX(), this.getY() + 0.65D, this.getZ(),
                16, 0.55D, 0.25D, 0.55D, 0.03D);

        Optional<Item> crustOpt = ModItems.CHERUFE_BASALT_CRUST.isPresent()
                ? Optional.of(ModItems.CHERUFE_BASALT_CRUST.get())
                : BuiltInRegistries.ITEM.getOptional(CHERUFE_BASALT_CRUST_ID);
        crustOpt.ifPresent(crust ->
                this.spawnAtLocation(serverLevel, new ItemStack(crust, 1), 0.55F));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean isColdQuenchItem = stack.is(Items.ICE)
                || stack.is(Items.PACKED_ICE)
                || stack.is(Items.BLUE_ICE)
                || stack.is(Items.SNOWBALL);
        if (isColdQuenchItem && this.isAlive() && this.crustShedCooldown <= 0) {
            if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                stack.consume(1, player);
                this.tryThermalQuenchShedCrust(serverLevel);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(serverLevel, source, amount);
        if (hurt && this.isAlive()) {
            if (this.jawClampTicks <= 0 && this.tailWhipTicks <= 0) {
                serverLevel.broadcastEntityEvent(this, EVENT_HURT_SHUDDER);
            }
            Entity direct = source.getDirectEntity();
            boolean thermalShock = source.is(DamageTypeTags.IS_FREEZING)
                    || source.is(DamageTypes.DROWN)
                    || direct instanceof Snowball
                    || isWaterSplash(direct);
            if (thermalShock) {
                this.tryThermalQuenchShedCrust(serverLevel);
            }
        }
        return hurt;
    }

    private static boolean isWaterSplash(@Nullable Entity direct) {
        if (direct instanceof ThrownSplashPotion potion) {
            PotionContents contents = potion.getItem().get(DataComponents.POTION_CONTENTS);
            return contents != null && contents.is(Potions.WATER);
        }
        return false;
    }

    private void startJawClamp(ServerLevel serverLevel) {
        this.jawClampTicks = JAW_CLAMP_DURATION_TICKS;
        this.combatCooldown = JAW_CLAMP_DURATION_TICKS + 5;
        this.strikeCounter++;
        serverLevel.broadcastEntityEvent(this, EVENT_JAW_CLAMP);
        serverLevel.playSound(null, this.getX(), this.getY() + 0.6D, this.getZ(),
                SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 0.70F, 1.35F);
    }

    private void executeJawClampImpact(ServerLevel serverLevel) {
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        if (this.distanceToSqr(target) <= 17.5D && this.hasLineOfSight(target)) {
            boolean hit = this.doHurtTarget(serverLevel, target);
            if (hit) {
                // Clamp & tearing jaw shake briefly pins the target while scattering embers
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 1), this);
                serverLevel.playSound(null, target.getX(), target.getY() + 0.6D, target.getZ(),
                        SoundEvents.EVOKER_FANGS_ATTACK, SoundSource.HOSTILE, 0.85F, 0.85F);
                serverLevel.sendParticles(ParticleTypes.LAVA,
                        target.getX(), target.getY() + 0.7D, target.getZ(),
                        8, 0.35D, 0.25D, 0.35D, 0.04D);
            }
        }
    }

    private void startTailWhip(ServerLevel serverLevel) {
        this.tailWhipTicks = TAIL_WHIP_DURATION_TICKS;
        this.tailWhipCooldown = 70;
        this.combatCooldown = TAIL_WHIP_DURATION_TICKS + 5;
        this.strikeCounter++;
        serverLevel.broadcastEntityEvent(this, EVENT_TAIL_WHIP);
        serverLevel.playSound(null, this.getX(), this.getY() + 0.5D, this.getZ(),
                SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 0.80F, 0.75F);
    }

    private void executeTailWhipImpact(ServerLevel serverLevel) {
        serverLevel.sendParticles(ParticleTypes.FLAME,
                this.getX(), this.getY() + 0.55D, this.getZ(),
                26, 1.45D, 0.25D, 1.45D, 0.04D);
        serverLevel.sendParticles(ParticleTypes.LAVA,
                this.getX(), this.getY() + 0.55D, this.getZ(),
                8, 1.10D, 0.20D, 1.10D, 0.03D);

        List<LivingEntity> victims = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(2.6D, 1.1D, 2.6D),
                e -> e != this && e.isAlive() && !(e instanceof MagmaCherufeEntity)
        );
        LivingEntity primaryTarget = this.getTarget();
        for (LivingEntity victim : victims) {
            if (victim instanceof Player p && (p.isCreative() || p.isSpectator())) {
                continue;
            }
            boolean validTarget = (victim == primaryTarget)
                    || (victim instanceof Player)
                    || (victim == this.getLastHurtByMob());
            if (!validTarget || this.distanceToSqr(victim) > 15.0D) {
                continue;
            }

            // 4.0 fire damage + ignite target for 5 seconds + heavy basalt tail knockback
            victim.igniteForSeconds(5.0F);
            if (!victim.fireImmune()) {
                if (!victim.hurtServer(serverLevel, this.damageSources().inFire(), 4.0F)) {
                    victim.hurtServer(serverLevel, this.damageSources().mobAttack(this), 4.0F);
                }
            } else {
                victim.hurtServer(serverLevel, this.damageSources().mobAttack(this), 4.0F);
            }

            double dx = victim.getX() - this.getX();
            double dz = victim.getZ() - this.getZ();
            double dist = Math.max(0.001D, Math.hypot(dx, dz));
            victim.push((dx / dist) * 0.65D, 0.24D, (dz / dist) * 0.65D);
            victim.hurtMarked = true;
        }
    }

    @Override
    protected void tickDeath() {
        ++this.deathTime;
        if (this.level().isClientSide() && this.deathTime % 4 == 0) {
            this.level().addParticle(ParticleTypes.SMOKE,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 1.1D,
                    this.getY() + 0.45D,
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
        if (id == EVENT_JAW_CLAMP) {
            this.hurtAnimationState.stop();
            this.tailWhipAnimationState.stop();
            this.attackAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_TAIL_WHIP) {
            this.hurtAnimationState.stop();
            this.attackAnimationState.stop();
            this.tailWhipAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_HURT_SHUDDER) {
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
            if (this.tailWhipCooldown > 0) {
                this.tailWhipCooldown--;
            }
            if (this.crustShedCooldown > 0) {
                this.crustShedCooldown--;
            }

            if (this.isAlive()) {
                // Vertical basalt cliff climbing update
                boolean climbingNow = this.horizontalCollision && this.canClimbAdjacentSurface();
                this.setClimbing(climbingNow);

                // Lava submersion: regenerate 2 HP (1 heart) per second & stay buoyant in lava rivers
                boolean inLava = this.isInLava();
                this.setLavaBathing(inLava);
                if (inLava) {
                    if (this.tickCount % 20 == 0 && this.getHealth() < this.getMaxHealth()) {
                        this.heal(2.0F);
                        serverLevel.sendParticles(ParticleTypes.LAVA,
                                this.getX(), this.getY() + 0.7D, this.getZ(),
                                4, 0.45D, 0.20D, 0.45D, 0.02D);
                    }
                    if (this.getFluidHeight(FluidTags.LAVA) > 0.55D) {
                        Vec3 vel = this.getDeltaMovement();
                        this.setDeltaMovement(vel.x * 0.92D, Math.max(vel.y, 0.06D), vel.z * 0.92D);
                    }
                }

                // Active Molten Jaw Clamp sequence
                if (this.jawClampTicks > 0) {
                    int elapsed = JAW_CLAMP_DURATION_TICKS - this.jawClampTicks;
                    this.jawClampTicks--;
                    LivingEntity target = this.getTarget();
                    if (elapsed == JAW_LUNGE_TICK && target != null && target.isAlive()) {
                        Vec3 toTarget = target.position().subtract(this.position());
                        double horiz = Math.hypot(toTarget.x, toTarget.z);
                        if (horiz > 1.0E-4D) {
                            Vec3 vel = this.getDeltaMovement();
                            this.setDeltaMovement(
                                    vel.x + (toTarget.x / horiz) * 0.36D,
                                    Math.max(vel.y, 0.14D),
                                    vel.z + (toTarget.z / horiz) * 0.36D
                            );
                        }
                    } else if (elapsed == JAW_IMPACT_TICK) {
                        this.executeJawClampImpact(serverLevel);
                    }
                }

                // Active Igniting Basalt Tail Whip sequence
                if (this.tailWhipTicks > 0) {
                    int elapsed = TAIL_WHIP_DURATION_TICKS - this.tailWhipTicks;
                    this.tailWhipTicks--;
                    if (elapsed == TAIL_WHIP_IMPACT_TICK) {
                        this.executeTailWhipImpact(serverLevel);
                    }
                }
            } else {
                this.setClimbing(false);
                this.setLavaBathing(false);
            }
        } else if (this.level().isClientSide()) {
            this.setupClientAnimationsAndParticles();
        }
    }

    private void setupClientAnimationsAndParticles() {
        if (!this.isAlive()) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.climbAnimationState.stop();
            this.attackAnimationState.stop();
            this.tailWhipAnimationState.stop();
            this.hurtAnimationState.stop();
            this.deathAnimationState.startIfStopped(this.tickCount);
            return;
        }

        if (this.attackAnimationState.isStarted()
                && this.attackAnimationState.getTimeInMillis(this.tickCount) >= JAW_CLAMP_DURATION_TICKS * 50L) {
            this.attackAnimationState.stop();
        }
        if (this.tailWhipAnimationState.isStarted()
                && this.tailWhipAnimationState.getTimeInMillis(this.tickCount) >= TAIL_WHIP_DURATION_TICKS * 50L) {
            this.tailWhipAnimationState.stop();
        }
        if (this.hurtAnimationState.isStarted()
                && this.hurtAnimationState.getTimeInMillis(this.tickCount) >= HURT_DURATION_TICKS * 50L) {
            this.hurtAnimationState.stop();
        }

        if (this.isClimbing()) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.climbAnimationState.startIfStopped(this.tickCount);
        } else {
            this.climbAnimationState.stop();
            boolean movingHoriz = this.getDeltaMovement().horizontalDistanceSqr() > 2.5E-4D;
            if (movingHoriz) {
                this.idleAnimationState.stop();
                this.walkAnimationState.startIfStopped(this.tickCount);
            } else {
                this.walkAnimationState.stop();
                this.idleAnimationState.startIfStopped(this.tickCount);
            }
        }

        // Ambient dorsal magma-vent embers & smoke wisps
        if (this.tickCount % (this.isLavaBathing() ? 4 : 11) == 0) {
            double px = this.getX() + (this.random.nextDouble() - 0.5D) * 0.85D;
            double py = this.getY() + 0.85D + this.random.nextDouble() * 0.20D;
            double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * 0.85D;
            this.level().addParticle(
                    this.random.nextInt(3) == 0 ? ParticleTypes.FLAME : ParticleTypes.SMOKE,
                    px, py, pz,
                    0.0D, 0.02D, 0.0D
            );
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("CrustShedCooldown", Codec.INT, this.crustShedCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.crustShedCooldown = input.read("CrustShedCooldown", Codec.INT).orElse(0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BLAZE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.BLAZE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BLAZE_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.BASALT_STEP, 0.45F, 0.75F);
    }

    private static final class CherufeCombatGoal extends Goal {
        private final MagmaCherufeEntity cherufe;
        private int repathTicks = 0;

        CherufeCombatGoal(MagmaCherufeEntity cherufe) {
            this.cherufe = cherufe;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.cherufe.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.cherufe.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }
            if (target instanceof Player p && (p.isCreative() || p.isSpectator())) {
                return false;
            }
            return this.cherufe.distanceToSqr(target) <= 676.0D;
        }

        @Override
        public void start() {
            this.repathTicks = 0;
        }

        @Override
        public void stop() {
            this.cherufe.getNavigation().stop();
        }

        @Override
        public void tick() {
            LivingEntity target = this.cherufe.getTarget();
            if (target == null || !(this.cherufe.level() instanceof ServerLevel serverLevel)) {
                return;
            }

            this.cherufe.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double distSqr = this.cherufe.distanceToSqr(target);

            if (--this.repathTicks <= 0) {
                this.repathTicks = 4 + this.cherufe.getRandom().nextInt(6);
                this.cherufe.getNavigation().moveTo(target, 1.18D);
            }

            if (this.cherufe.combatCooldown <= 0
                    && this.cherufe.jawClampTicks <= 0
                    && this.cherufe.tailWhipTicks <= 0
                    && distSqr <= 12.25D
                    && this.cherufe.hasLineOfSight(target)) {
                // Check if target is flanking/behind OR alternate into tail_whip when off cooldown
                double yawRad = this.cherufe.getYRot() * Mth.DEG_TO_RAD;
                double fwdX = -Math.sin(yawRad);
                double fwdZ = Math.cos(yawRad);
                Vec3 dir = target.position().subtract(this.cherufe.position()).normalize();
                double facingDot = fwdX * dir.x + fwdZ * dir.z;

                boolean useTailWhip = this.cherufe.tailWhipCooldown <= 0
                        && (facingDot < 0.25D || (this.cherufe.strikeCounter % 2 == 1));
                if (useTailWhip) {
                    this.cherufe.startTailWhip(serverLevel);
                } else {
                    this.cherufe.startJawClamp(serverLevel);
                }
            }
        }
    }
}
