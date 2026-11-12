package ddraig.net.entropica.entity.geyser_wiggle_worm;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class GeyserWiggleWormEntity extends PathfinderMob {

    private static final EntityDataAccessor<Boolean> IS_VENTING_STEAM = SynchedEntityData.defineId(GeyserWiggleWormEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_REARING_UP = SynchedEntityData.defineId(GeyserWiggleWormEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_ATTACKING = SynchedEntityData.defineId(GeyserWiggleWormEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState slitherAnimationState = new AnimationState();
    public final AnimationState rearUpAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState ventSteamAnimationState = new AnimationState();

    private int ventTimer = 0;
    private int harvestCooldown = 0;
    private int attackAnimTimer = 0;

    public GeyserWiggleWormEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(PathType.LAVA, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_VENTING_STEAM, false);
        builder.define(IS_REARING_UP, false);
        builder.define(IS_ATTACKING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
    }

    public boolean isVentingSteam() {
        return this.entityData.get(IS_VENTING_STEAM);
    }

    public void setVentingSteam(boolean venting) {
        this.entityData.set(IS_VENTING_STEAM, venting);
    }

    public boolean isRearingUp() {
        return this.entityData.get(IS_REARING_UP);
    }

    public void setRearingUp(boolean rearing) {
        this.entityData.set(IS_REARING_UP, rearing);
    }

    public boolean isAttacking() {
        return this.entityData.get(IS_ATTACKING);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(IS_ATTACKING, attacking);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource damageSource, float amount) {
        boolean result = super.hurtServer(level, damageSource, amount);
        if (result && !this.isVentingSteam()) {
            this.triggerSteamVent();
        }
        return result;
    }

    public void triggerSteamVent() {
        this.setVentingSteam(true);
        this.ventTimer = 40; // 2 seconds
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 1.5F, 0.8F);

        // Create thermal updraft pushing nearby entities upward
        AABB area = this.getBoundingBox().inflate(2.5D, 6.0D, 2.5D);
        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, area);
        for (LivingEntity entity : nearby) {
            if (entity != this) {
                Vec3 vel = entity.getDeltaMovement();
                entity.setDeltaMovement(vel.x * 0.5D, Math.max(vel.y, 0.85D), vel.z * 0.5D);
                entity.hurtMarked = true;
            }
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.harvestCooldown > 0) {
            this.harvestCooldown--;
        }

        if (this.ventTimer > 0) {
            this.ventTimer--;
            if (this.level().isClientSide()) {
                for (int i = 0; i < 4; i++) {
                    double px = this.getX() + (this.random.nextDouble() - 0.5D) * 0.8D;
                    double py = this.getY() + 0.6D + this.random.nextDouble() * 0.5D;
                    double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * 0.8D;
                    this.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, px, py, pz, 0.0D, 0.15D, 0.0D);
                    this.level().addParticle(ParticleTypes.LAVA, px, py, pz, 0.0D, 0.05D, 0.0D);
                }
            }
            if (this.ventTimer == 0) {
                this.setVentingSteam(false);
            }
        }

        if (this.attackAnimTimer > 0) {
            this.attackAnimTimer--;
            if (this.attackAnimTimer == 0) {
                this.setAttacking(false);
            }
        }

        if (this.level().isClientSide()) {
            this.setupAnimationStates();
        }
    }

    private void setupAnimationStates() {
        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;

        if (this.isVentingSteam()) {
            this.ventSteamAnimationState.startIfStopped(this.tickCount);
        } else {
            this.ventSteamAnimationState.stop();
        }

        if (this.isAttacking()) {
            this.attackAnimationState.startIfStopped(this.tickCount);
        } else {
            this.attackAnimationState.stop();
        }

        if (this.isRearingUp()) {
            this.rearUpAnimationState.startIfStopped(this.tickCount);
        } else {
            this.rearUpAnimationState.stop();
        }

        if (moving) {
            this.idleAnimationState.stop();
            this.slitherAnimationState.startIfStopped(this.tickCount);
        } else {
            this.slitherAnimationState.stop();
            this.idleAnimationState.startIfStopped(this.tickCount);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if ((stack.is(Items.SHEARS) || stack.getItem().toString().toLowerCase().contains("shears")) && this.harvestCooldown == 0) {
            this.triggerSteamVent();
            this.harvestCooldown = 6000;
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
            if (this.level() instanceof ServerLevel serverLevel) {
                stack.hurtAndBreak(1, player, getEquipmentSlotForItem(stack));
                this.spawnAtLocation(serverLevel, new ItemStack(ModItems.VAPOR_VENT_MEMBRANE.get(), 1));
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        this.spawnAtLocation(level, new ItemStack(ModItems.VAPOR_VENT_MEMBRANE.get(), 1));
        int sulfur = 2 + this.random.nextInt(2);
        this.spawnAtLocation(level, new ItemStack(ModItems.SULFUR_CLOD.get(), sulfur));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("HarvestCooldown", Codec.INT, this.harvestCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.harvestCooldown = input.read("HarvestCooldown", Codec.INT).orElse(0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.MAGMA_CUBE_SQUISH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.MAGMA_CUBE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.MAGMA_CUBE_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SLIME_SQUISH_SMALL, 0.15F, 1.0F);
    }
}
