package ddraig.net.entropica.entity.ovis;

import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.mojang.serialization.Codec;

import java.util.EnumSet;

public abstract class AbstractOvisEntity extends PathfinderMob {

    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(AbstractOvisEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> SHEARED = SynchedEntityData.defineId(AbstractOvisEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> BRACED = SynchedEntityData.defineId(AbstractOvisEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CHARGING = SynchedEntityData.defineId(AbstractOvisEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> STUNNED = SynchedEntityData.defineId(AbstractOvisEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> GRAZING = SynchedEntityData.defineId(AbstractOvisEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> RESTING = SynchedEntityData.defineId(AbstractOvisEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(AbstractOvisEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState runAnimationState = new AnimationState();
    public final AnimationState grazeAnimationState = new AnimationState();
    public final AnimationState restAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState headbuttAnimationState = new AnimationState();

    protected int stunTimer = 0;
    protected int braceTimer = 0;
    protected int shearCooldown = 0;
    protected int grazeTimer = 0;
    protected int attackAnimTimer = 0;

    protected AbstractOvisEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new OvisChargeGoal(this));
        this.goalSelector.addGoal(2, new PanicGoal(this, 1.25D) {
            @Override
            public boolean canUse() {
                return !AbstractOvisEntity.this.isBraced() && !AbstractOvisEntity.this.isCharging() && super.canUse();
            }
        });
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLAGS_ID, (byte) 0);
        builder.define(SHEARED, false);
        builder.define(BRACED, false);
        builder.define(CHARGING, false);
        builder.define(STUNNED, false);
        builder.define(GRAZING, false);
        builder.define(RESTING, false);
        builder.define(ATTACKING, false);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setSheared(input.read("Sheared", Codec.BOOL).orElse(false));
        this.setBraced(input.read("Braced", Codec.BOOL).orElse(false));
        this.setCharging(input.read("Charging", Codec.BOOL).orElse(false));
        this.setStunned(input.read("Stunned", Codec.BOOL).orElse(false));
        this.setGrazing(input.read("Grazing", Codec.BOOL).orElse(false));
        this.setResting(input.read("Resting", Codec.BOOL).orElse(false));
        this.stunTimer = input.read("StunTimer", Codec.INT).orElse(0);
        this.braceTimer = input.read("BraceTimer", Codec.INT).orElse(0);
        this.shearCooldown = input.read("ShearCooldown", Codec.INT).orElse(0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("Sheared", Codec.BOOL, this.isSheared());
        output.store("Braced", Codec.BOOL, this.isBraced());
        output.store("Charging", Codec.BOOL, this.isCharging());
        output.store("Stunned", Codec.BOOL, this.isStunned());
        output.store("Grazing", Codec.BOOL, this.isGrazing());
        output.store("Resting", Codec.BOOL, this.isResting());
        output.store("StunTimer", Codec.INT, this.stunTimer);
        output.store("BraceTimer", Codec.INT, this.braceTimer);
        output.store("ShearCooldown", Codec.INT, this.shearCooldown);
    }

    public boolean isSheared() {
        return this.entityData.get(SHEARED);
    }

    public void setSheared(boolean sheared) {
        this.entityData.set(SHEARED, sheared);
    }

    public boolean isBraced() {
        return this.entityData.get(BRACED);
    }

    public void setBraced(boolean braced) {
        this.entityData.set(BRACED, braced);
    }

    public boolean isCharging() {
        return this.entityData.get(CHARGING);
    }

    public void setCharging(boolean charging) {
        this.entityData.set(CHARGING, charging);
    }

    public boolean isStunned() {
        return this.entityData.get(STUNNED);
    }

    public void setStunned(boolean stunned) {
        this.entityData.set(STUNNED, stunned);
    }

    public boolean isGrazing() {
        return this.entityData.get(GRAZING);
    }

    public void setGrazing(boolean grazing) {
        this.entityData.set(GRAZING, grazing);
    }

    public boolean isResting() {
        return this.entityData.get(RESTING);
    }

    public void setResting(boolean resting) {
        this.entityData.set(RESTING, resting);
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }

    public boolean isClimbing() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    public void setClimbing(boolean climbing) {
        byte b = this.entityData.get(DATA_FLAGS_ID);
        if (climbing) {
            b = (byte) (b | 1);
        } else {
            b = (byte) (b & -2);
        }
        this.entityData.set(DATA_FLAGS_ID, b);
    }

    @Override
    public boolean onClimbable() {
        return this.isClimbing();
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            this.updateAnimationStates();
        } else {
            this.setClimbing(this.horizontalCollision);

            if (this.isBraced()) {
                this.braceTimer--;
                if (this.braceTimer <= 0) {
                    this.setBraced(false);
                }
            }

            if (this.isStunned()) {
                this.stunTimer--;
                if (this.stunTimer <= 0) {
                    this.setStunned(false);
                }
            }

            if (this.isSheared()) {
                this.shearCooldown--;
                if (this.shearCooldown <= 0) {
                    this.setSheared(false);
                }
            }

            if (this.isAttacking()) {
                this.attackAnimTimer--;
                if (this.attackAnimTimer <= 0) {
                    this.setAttacking(false);
                }
            }

            // Stun on solid horizontal collision while charging (Fauna 1 spec)
            if (this.isCharging() && this.horizontalCollision) {
                this.setStunned(true);
                this.setCharging(false);
                this.stunTimer = 40; // 2 seconds (40 ticks)
                this.getNavigation().stop();

                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.NEUTRAL, 1.5F, 0.5F);
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()), this.getX(), this.getY() + 1.0D, this.getZ(), 20, 0.5, 0.5, 0.5, 0.1);
                    // Shed 1-2 Aegis plates upon impact
                    int count = 1 + this.random.nextInt(2);
                    for (int i = 0; i < count; i++) {
                        this.spawnAtLocation(serverLevel, new ItemStack(getPlateItem()));
                    }
                }
            }
        }
    }

    private void updateAnimationStates() {
        boolean isMoving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6D;
        boolean isSprinting = this.isCharging() || this.isSprinting();

        if (this.isCharging()) {
            this.headbuttAnimationState.startIfStopped(this.tickCount);
            this.attackAnimationState.stop();
            this.walkAnimationState.stop();
            this.runAnimationState.stop();
            this.idleAnimationState.stop();
            this.grazeAnimationState.stop();
            this.restAnimationState.stop();
        } else if (this.isAttacking()) {
            this.attackAnimationState.startIfStopped(this.tickCount);
            this.headbuttAnimationState.stop();
            this.walkAnimationState.stop();
            this.runAnimationState.stop();
            this.idleAnimationState.stop();
            this.grazeAnimationState.stop();
            this.restAnimationState.stop();
        } else if (this.isResting()) {
            this.restAnimationState.startIfStopped(this.tickCount);
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.runAnimationState.stop();
            this.grazeAnimationState.stop();
        } else if (this.isGrazing()) {
            this.grazeAnimationState.startIfStopped(this.tickCount);
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.runAnimationState.stop();
        } else if (isMoving) {
            if (isSprinting) {
                this.runAnimationState.startIfStopped(this.tickCount);
                this.walkAnimationState.stop();
            } else {
                this.walkAnimationState.startIfStopped(this.tickCount);
                this.runAnimationState.stop();
            }
            this.idleAnimationState.stop();
            this.grazeAnimationState.stop();
            this.restAnimationState.stop();
        } else {
            this.idleAnimationState.startIfStopped(this.tickCount);
            this.walkAnimationState.stop();
            this.runAnimationState.stop();
            this.grazeAnimationState.stop();
            this.restAnimationState.stop();
            this.attackAnimationState.stop();
            this.headbuttAnimationState.stop();
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (!this.isBraced() && !this.isStunned()) {
            this.setBraced(true);
            this.braceTimer = 60; // 3 seconds frontal brace
        }

        if (this.isBraced() && source.getEntity() != null) {
            Vec3 viewVec = this.getViewVector(1.0F).normalize();
            Vec3 sourceVec = source.getEntity().position().subtract(this.position()).normalize();
            double dot = viewVec.dot(sourceVec);
            if (dot > 0.707) { // 90 degree frontal arc
                amount *= 0.2F; // 80% damage reduction from front
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ANVIL_PLACE, SoundSource.NEUTRAL, 1.0F, 1.5F);
            }
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem().toString().toLowerCase().contains("shears") || held.is(Items.SHEARS)) {
            if (!this.isSheared()) {
                if (!this.level().isClientSide()) {
                    this.setSheared(true);
                    this.shearCooldown = 24000; // 1 Minecraft day (24000 ticks)
                    held.hurtAndBreak(1, player, getEquipmentSlotForItem(held));
                    
                    // 50% chance to drop 1 plate
                    if (this.random.nextFloat() < 0.5F) {
                        this.spawnAtLocation((ServerLevel)this.level(), new ItemStack(getPlateItem()));
                    }
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    public abstract Item getPlateItem();

    @Override
    protected void dropCustomDeathLoot(ServerLevel serverLevel, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(serverLevel, damageSource, recentlyHit);
        this.spawnAtLocation(serverLevel, new ItemStack(Items.MUTTON, 2 + this.random.nextInt(2)));
        if (!this.isSheared() || this.random.nextFloat() < 0.5F) {
            this.spawnAtLocation(serverLevel, new ItemStack(getPlateItem()));
        }
    }

    public static class OvisChargeGoal extends Goal {
        private final AbstractOvisEntity ovis;
        private LivingEntity target;
        private int cooldown = 0;

        public OvisChargeGoal(AbstractOvisEntity ovis) {
            this.ovis = ovis;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (cooldown > 0) {
                cooldown--;
                return false;
            }
            if (ovis.isStunned()) return false;
            this.target = ovis.getTarget();
            return this.target != null && this.target.isAlive() && ovis.distanceToSqr(this.target) <= 36.0D;
        }

        @Override
        public void start() {
            ovis.setCharging(true);
            ovis.setBraced(false);
        }

        @Override
        public void stop() {
            ovis.setCharging(false);
            this.cooldown = 80; // 4 seconds cooldown
        }

        @Override
        public void tick() {
            if (target == null) return;
            ovis.getLookControl().setLookAt(target, 30.0F, 30.0F);
            ovis.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.4D);

            if (ovis.distanceToSqr(target) <= 3.0D) {
                target.hurt(ovis.damageSources().mobAttack(ovis), 8.0F);
                Vec3 kb = target.position().subtract(ovis.position()).normalize().scale(1.2D);
                target.setDeltaMovement(kb.x, 0.35D, kb.z);
                this.stop();
            }
        }
    }
}
