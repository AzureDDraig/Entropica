package ddraig.net.entropica.entity.aurorafowl;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class AurorafowlEntity extends Animal implements FlyingAnimal {

    private static final EntityDataAccessor<Boolean> IS_FLYING = SynchedEntityData.defineId(AurorafowlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_SHEARED = SynchedEntityData.defineId(AurorafowlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_ATTACKING = SynchedEntityData.defineId(AurorafowlEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState idleGroundAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState idleFlightAnimationState = new AnimationState();
    public final AnimationState flyingAnimationState = new AnimationState();
    public final AnimationState takingOffAnimationState = new AnimationState();
    public final AnimationState landingAnimationState = new AnimationState();
    public final AnimationState attackGroundAnimationState = new AnimationState();
    public final AnimationState attackFlyingAnimationState = new AnimationState();

    private final FlyingPathNavigation flyingNavigation;
    private final GroundPathNavigation groundNavigation;
    public int flightTimer = 0;
    private int shearCooldown = 0;
    private int attackTimer = 0;

    public AurorafowlEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.flyingNavigation = new FlyingPathNavigation(this, level);
        this.groundNavigation = new GroundPathNavigation(this, level);
        this.moveControl = new FlyingMoveControl(this, 10, false);
        this.setPathfindingMalus(PathType.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 16.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.FLYING_SPEED, 0.40D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.TEMPT_RANGE, 16.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_FLYING, false);
        builder.define(IS_SHEARED, false);
        builder.define(IS_ATTACKING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4D) {
            @Override
            public void start() {
                super.start();
                AurorafowlEntity.this.setFlying(true);
            }
        });
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.1D, stack -> stack.is(ModItems.AURORAFOWL_FEATHER.get()) || stack.is(Items.WHEAT_SEEDS), false));
        this.goalSelector.addGoal(4, new AurorafowlFlightGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    public boolean isFlying() {
        return this.entityData.get(IS_FLYING);
    }

    public void setFlying(boolean flying) {
        this.entityData.set(IS_FLYING, flying);
        this.navigation = flying ? this.flyingNavigation : this.groundNavigation;
        this.setNoGravity(flying);
    }

    public boolean isSheared() {
        return this.entityData.get(IS_SHEARED);
    }

    public void setSheared(boolean sheared) {
        this.entityData.set(IS_SHEARED, sheared);
    }

    public boolean isAttacking() {
        return this.entityData.get(IS_ATTACKING);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(IS_ATTACKING, attacking);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.WHEAT_SEEDS) || stack.is(Items.MELON_SEEDS) || stack.is(Items.PUMPKIN_SEEDS);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return ddraig.net.entropica.registry.ModEntityTypes.AURORAFOWL.get().create(level, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.shearCooldown > 0) {
            this.shearCooldown--;
            if (this.shearCooldown == 0) {
                this.setSheared(false);
            }
        }

        if (this.attackTimer > 0) {
            this.attackTimer--;
            if (this.attackTimer == 0) {
                this.setAttacking(false);
            }
        }

        if (this.isFlying()) {
            if (this.onGround() && this.flightTimer > 100) {
                this.setFlying(false);
                this.flightTimer = 0;
            } else {
                this.flightTimer++;
                Vec3 motion = this.getDeltaMovement();
                if (!this.isNoGravity()) {
                    this.setNoGravity(true);
                }
                if (motion.y < -0.05) {
                    this.setDeltaMovement(motion.x, motion.y * 0.6D, motion.z);
                }
            }
        } else {
            if (this.isNoGravity()) {
                this.setNoGravity(false);
            }
        }

        if (this.level().isClientSide()) {
            this.setupAnimationStates();
        }
    }

    private void setupAnimationStates() {
        boolean flying = this.isFlying() || !this.onGround();
        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;

        if (flying) {
            this.idleGroundAnimationState.stop();
            this.walkAnimationState.stop();
            if (moving) {
                this.idleFlightAnimationState.stop();
                this.flyingAnimationState.startIfStopped(this.tickCount);
            } else {
                this.flyingAnimationState.stop();
                this.idleFlightAnimationState.startIfStopped(this.tickCount);
            }
        } else {
            this.idleFlightAnimationState.stop();
            this.flyingAnimationState.stop();
            if (moving) {
                this.idleGroundAnimationState.stop();
                this.walkAnimationState.startIfStopped(this.tickCount);
            } else {
                this.walkAnimationState.stop();
                this.idleGroundAnimationState.startIfStopped(this.tickCount);
            }
        }

        if (this.isAttacking()) {
            if (flying) {
                this.attackFlyingAnimationState.startIfStopped(this.tickCount);
            } else {
                this.attackGroundAnimationState.startIfStopped(this.tickCount);
            }
        } else {
            this.attackGroundAnimationState.stop();
            this.attackFlyingAnimationState.stop();
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if ((stack.is(Items.SHEARS) || stack.getItem().toString().toLowerCase().contains("shears")) && !this.isSheared() && !this.isBaby()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
            this.setSheared(true);
            this.shearCooldown = 12000;
            if (this.level() instanceof ServerLevel serverLevel) {
                stack.hurtAndBreak(1, player, getEquipmentSlotForItem(stack));
                int count = 1 + this.random.nextInt(3);
                for (int i = 0; i < count; i++) {
                    this.spawnAtLocation(serverLevel, new ItemStack(ModItems.AURORAFOWL_FEATHER.get()));
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        int feathers = 1 + this.random.nextInt(3);
        this.spawnAtLocation(level, new ItemStack(ModItems.AURORAFOWL_FEATHER.get(), feathers));

        int meat = 1 + this.random.nextInt(2);
        if (this.isOnFire()) {
            this.spawnAtLocation(level, new ItemStack(ModItems.COOKED_AURORAFOWL.get(), meat));
        } else {
            this.spawnAtLocation(level, new ItemStack(ModItems.RAW_AURORAFOWL.get(), meat));
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("Flying", Codec.BOOL, this.isFlying());
        output.store("Sheared", Codec.BOOL, this.isSheared());
        output.store("ShearCooldown", Codec.INT, this.shearCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setFlying(input.read("Flying", Codec.BOOL).orElse(false));
        this.setSheared(input.read("Sheared", Codec.BOOL).orElse(false));
        this.shearCooldown = input.read("ShearCooldown", Codec.INT).orElse(0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
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
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.CHICKEN_STEP, 0.15F, 1.0F);
    }

    static class AurorafowlFlightGoal extends Goal {
        private final AurorafowlEntity fowl;

        public AurorafowlFlightGoal(AurorafowlEntity fowl) {
            this.fowl = fowl;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return this.fowl.getRandom().nextInt(100) == 0 && !this.fowl.isFlying() && !this.fowl.isInWater();
        }

        @Override
        public boolean canContinueToUse() {
            return this.fowl.isFlying() && this.fowl.flightTimer < 300;
        }

        @Override
        public void start() {
            this.fowl.setFlying(true);
            RandomSource rand = this.fowl.getRandom();
            double dx = this.fowl.getX() + (rand.nextDouble() * 32.0D - 16.0D);
            double dy = this.fowl.getY() + (rand.nextDouble() * 16.0D + 6.0D);
            double dz = this.fowl.getZ() + (rand.nextDouble() * 32.0D - 16.0D);
            this.fowl.getNavigation().moveTo(dx, dy, dz, 1.2D);
        }
    }
}
