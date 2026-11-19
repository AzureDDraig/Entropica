package ddraig.net.entropica.entity.mammot;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.entity.frost_core.GlacialFrostCoreEntity;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Locale;

/**
 * Fauna 20 — Frost-Tusk Mammot (Glacial Excavator).
 * Heavy woolly quadruped native to 2.1 Blue-Glacier Shelf that clears snow layers ahead
 * while walking and delivers an upward-launching tusk sweep lunge when provoked.
 */
public class FrostTuskMammotEntity extends PathfinderMob {

    private static final EntityDataAccessor<Boolean> SHEARED =
            SynchedEntityData.defineId(FrostTuskMammotEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(FrostTuskMammotEntity.class, EntityDataSerializers.BOOLEAN);

    private static final byte EVENT_TUSK_SWEEP = 4;
    private static final int SHEAR_COOLDOWN_TICKS = 24000;

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();

    private int shearCooldown = 0;
    private int attackAnimTimer = 0;

    public FrostTuskMammotEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 45.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.21D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D)
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.STEP_HEIGHT, 1.1D);
    }

    public static boolean checkBlueGlacierSpawnRules(
            EntityType<? extends PathfinderMob> type,
            ServerLevelAccessor level,
            EntitySpawnReason spawnReason,
            BlockPos pos,
            RandomSource random
    ) {
        if (EntitySpawnReason.isSpawner(spawnReason) || spawnReason == EntitySpawnReason.SPAWN_ITEM_USE || spawnReason == EntitySpawnReason.COMMAND) {
            return true;
        }
        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        if (below.is(Blocks.SNOW) && level.getBlockState(belowPos.below()).blocksMotion()) {
            belowPos = belowPos.below();
            below = level.getBlockState(belowPos);
        }
        return GlacialFrostCoreEntity.isGlacialSpawnSurface(level, belowPos, below);
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        BlockPos belowPos = this.blockPosition().below();
        BlockState belowState = level.getBlockState(belowPos);
        if (GlacialFrostCoreEntity.isGlacialSpawnSurface(level, belowPos, belowState)) {
            return true;
        }
        return super.checkSpawnRules(level, spawnReason);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        BlockState below = level.getBlockState(pos.below());
        if (below.is(Blocks.BLUE_ICE) || below.is(Blocks.PACKED_ICE) || below.is(Blocks.SNOW_BLOCK) || below.is(Blocks.ICE)) {
            return 10.0F;
        }
        return 0.0F;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SHEARED, false);
        builder.define(ATTACKING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MammotTuskSweepGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 7.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public boolean isSheared() {
        return this.entityData.get(SHEARED);
    }

    public void setSheared(boolean sheared) {
        this.entityData.set(SHEARED, sheared);
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SNIFFER_IDLE;
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
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.RAVAGER_STEP, 0.35F, 0.85F);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_TUSK_SWEEP) {
            this.attackAnimationState.start(this.tickCount);
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
            if (moving) {
                this.walkAnimationState.startIfStopped(this.tickCount);
                this.idleAnimationState.stop();
            } else {
                this.walkAnimationState.stop();
                this.idleAnimationState.startIfStopped(this.tickCount);
            }

            if (this.isAttacking()) {
                this.attackAnimationState.startIfStopped(this.tickCount);
            }
        } else if (this.level() instanceof ServerLevel serverLevel) {
            if (this.isSheared()) {
                this.shearCooldown--;
                if (this.shearCooldown <= 0) {
                    this.setSheared(false);
                }
            }

            if (this.attackAnimTimer > 0) {
                this.attackAnimTimer--;
                if (this.attackAnimTimer <= 0) {
                    this.setAttacking(false);
                }
            }

            // Clear snow layers directly ahead of feet while walking when mobGriefing is enabled
            if (this.onGround()
                    && this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D
                    && serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                clearSnowLayersAhead(serverLevel);
            }
        }
    }

    private void clearSnowLayersAhead(ServerLevel serverLevel) {
        Vec3 forward = Vec3.directionFromRotation(0.0F, this.getYRot());
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);

        double baseX = this.getX() + forward.x * 1.35D;
        double baseY = this.getY();
        double baseZ = this.getZ() + forward.z * 1.35D;

        BlockPos[] probePositions = new BlockPos[]{
                this.blockPosition(),
                BlockPos.containing(baseX, baseY, baseZ),
                BlockPos.containing(baseX + right.x * 0.55D, baseY, baseZ + right.z * 0.55D),
                BlockPos.containing(baseX - right.x * 0.55D, baseY, baseZ - right.z * 0.55D)
        };

        for (BlockPos snowPos : probePositions) {
            BlockState state = serverLevel.getBlockState(snowPos);
            if (state.is(Blocks.SNOW) || state.getBlock() instanceof SnowLayerBlock) {
                serverLevel.removeBlock(snowPos, false);
                serverLevel.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, state),
                        snowPos.getX() + 0.5D,
                        snowPos.getY() + 0.2D,
                        snowPos.getZ() + 0.5D,
                        10,
                        0.35D,
                        0.15D,
                        0.35D,
                        0.08D
                );
                serverLevel.sendParticles(
                        ParticleTypes.SNOWFLAKE,
                        snowPos.getX() + 0.5D,
                        snowPos.getY() + 0.25D,
                        snowPos.getZ() + 0.5D,
                        5,
                        0.3D,
                        0.15D,
                        0.3D,
                        0.02D
                );
                serverLevel.playSound(
                        null,
                        snowPos,
                        SoundEvents.SNOW_BREAK,
                        SoundSource.NEUTRAL,
                        0.6F,
                        0.95F
                );
            }
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        String itemId = BuiltInRegistries.ITEM.getKey(held.getItem()).getPath().toLowerCase(Locale.ROOT);
        if (held.is(Items.SHEARS) || itemId.contains("shears")) {
            if (!this.isSheared()) {
                if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                    this.setSheared(true);
                    this.shearCooldown = SHEAR_COOLDOWN_TICKS;
                    held.hurtAndBreak(1, player, getEquipmentSlotForItem(held));

                    int woolCount = 1 + this.random.nextInt(2);
                    this.spawnAtLocation(serverLevel, new ItemStack(ModItems.INSULATED_GLACIAL_WOOL.get(), woolCount), 1.0F);
                    serverLevel.playSound(
                            null,
                            this.getX(),
                            this.getY(),
                            this.getZ(),
                            SoundEvents.SHEEP_SHEAR,
                            SoundSource.PLAYERS,
                            1.0F,
                            0.9F
                    );
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel serverLevel, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(serverLevel, damageSource, recentlyHit);
        int woolCount = 2 + this.random.nextInt(2);
        this.spawnAtLocation(serverLevel, new ItemStack(ModItems.INSULATED_GLACIAL_WOOL.get(), woolCount));

        int meatCount = 2 + this.random.nextInt(3);
        Item meatItem = this.isOnFire() ? ModItems.COOKED_MAMMOT_MEAT.get() : ModItems.RAW_MAMMOT_MEAT.get();
        this.spawnAtLocation(serverLevel, new ItemStack(meatItem, meatCount));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setSheared(input.read("Sheared", Codec.BOOL).orElse(false));
        this.shearCooldown = input.read("ShearCooldown", Codec.INT).orElse(0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("Sheared", Codec.BOOL, this.isSheared());
        output.store("ShearCooldown", Codec.INT, this.shearCooldown);
    }

    static class MammotTuskSweepGoal extends Goal {
        private final FrostTuskMammotEntity mammot;
        private int attackCooldown = 0;

        MammotTuskSweepGoal(FrostTuskMammotEntity mammot) {
            this.mammot = mammot;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.attackCooldown > 0) {
                this.attackCooldown--;
            }
            LivingEntity target = this.mammot.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.mammot.getTarget();
            return target != null && target.isAlive() && !target.isSpectator();
        }

        @Override
        public void stop() {
            this.mammot.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.attackCooldown > 0) {
                this.attackCooldown--;
            }
            LivingEntity target = this.mammot.getTarget();
            if (target == null) {
                return;
            }

            this.mammot.getLookControl().setLookAt(target, 30.0F, 30.0F);
            this.mammot.getNavigation().moveTo(target, 1.38D);

            double reachSqr = 6.76D; // ~2.6 blocks from center of 1.8-wide Mammot
            if (this.attackCooldown <= 0 && (this.mammot.distanceToSqr(target) <= reachSqr || this.mammot.isWithinMeleeAttackRange(target))) {
                this.attackCooldown = 32;
                this.mammot.attackAnimTimer = 18;
                this.mammot.setAttacking(true);

                if (this.mammot.level() instanceof ServerLevel serverLevel) {
                    serverLevel.broadcastEntityEvent(this.mammot, EVENT_TUSK_SWEEP);
                    float damage = (float) this.mammot.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    boolean hit = target.hurtServer(serverLevel, this.mammot.damageSources().mobAttack(this.mammot), damage);
                    if (hit) {
                        Vec3 pushDir = target.position().subtract(this.mammot.position());
                        Vec3 horiz = new Vec3(pushDir.x, 0.0D, pushDir.z);
                        if (horiz.lengthSqr() > 1.0E-4D) {
                            horiz = horiz.normalize().scale(0.35D);
                        }
                        Vec3 cur = target.getDeltaMovement();
                        // Launch target ~4 blocks upward (deltaMovement.y = 0.78D)
                        target.setDeltaMovement(cur.x * 0.2D + horiz.x, 0.78D, cur.z * 0.2D + horiz.z);
                        target.hurtMarked = true;
                        target.hasImpulse = true;

                        serverLevel.playSound(
                                null,
                                this.mammot.getX(),
                                this.mammot.getY(),
                                this.mammot.getZ(),
                                SoundEvents.RAVAGER_ATTACK,
                                SoundSource.HOSTILE,
                                1.1F,
                                0.9F
                        );
                        serverLevel.sendParticles(
                                ParticleTypes.SNOWFLAKE,
                                target.getX(),
                                target.getY() + 0.5D,
                                target.getZ(),
                                18,
                                0.5D,
                                0.3D,
                                0.5D,
                                0.08D
                        );
                    }
                }
            }
        }
    }
}
