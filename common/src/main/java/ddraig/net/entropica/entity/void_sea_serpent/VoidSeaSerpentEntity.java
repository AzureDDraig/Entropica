package ddraig.net.entropica.entity.void_sea_serpent;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class VoidSeaSerpentEntity extends WaterAnimal implements Enemy {

    private static final EntityDataAccessor<Boolean> ROARING = SynchedEntityData.defineId(VoidSeaSerpentEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> BITING = SynchedEntityData.defineId(VoidSeaSerpentEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState swimAnimationState = new AnimationState();
    public final AnimationState biteAnimationState = new AnimationState();
    public final AnimationState roarAnimationState = new AnimationState();

    private int roarCooldown = 100;
    private int attackCooldown = 0;
    private static final double[] SEGMENT_DISTANCES = new double[]{2.8D, 5.6D, 8.4D, 11.2D, 14.0D};
    private static final float[][] SEGMENT_SIZES = new float[][]{
            {2.0F, 1.8F}, // Fore body / Pectoral wings
            {2.2F, 2.0F}, // Grand Sail / Upper mid
            {1.8F, 1.8F}, // Mid body
            {1.6F, 1.6F}, // Pelvic wings / Lower body
            {1.4F, 1.4F}  // Tail section
    };

    private VoidSeaSerpentSegmentEntity[] segments;
    private final java.util.List<TrailPoint> history = new java.util.ArrayList<>();

    public record TrailPoint(Vec3 pos, float yRot, float xRot) {}

    public VoidSeaSerpentEntity(EntityType<? extends WaterAnimal> type, Level level) {
        super(type, level);
        this.moveControl = new VoidSerpentMoveControl(this);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return WaterAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 140.0D)
                .add(Attributes.MOVEMENT_SPEED, 1.35D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ROARING, false);
        builder.define(BITING, false);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new VoidSerpentRoarGoal(this));
        this.goalSelector.addGoal(2, new VoidSerpentAttackGoal(this));
        this.goalSelector.addGoal(4, new VoidSerpentRandomSwimGoal(this));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    private void ensureSegmentsInitialized() {
        if (this.segments == null && !this.level().isClientSide()) {
            this.segments = new VoidSeaSerpentSegmentEntity[SEGMENT_DISTANCES.length];
            for (int i = 0; i < SEGMENT_DISTANCES.length; i++) {
                this.segments[i] = new VoidSeaSerpentSegmentEntity(this, SEGMENT_SIZES[i][0], SEGMENT_SIZES[i][1], i);
                this.level().addFreshEntity(this.segments[i]);
            }
        }
    }

    private TrailPoint getPointAtDistance(double targetDistance) {
        if (this.history.isEmpty()) {
            Vec3 dir = this.calculateViewVector(this.getXRot(), this.getYRot());
            return new TrailPoint(this.position().subtract(dir.scale(targetDistance)), this.getYRot(), this.getXRot());
        }

        double accumulatedDistance = 0.0D;
        Vec3 prevPos = this.position();

        for (int i = 0; i < this.history.size(); i++) {
            TrailPoint pt = this.history.get(i);
            double segLen = prevPos.distanceTo(pt.pos());
            if (accumulatedDistance + segLen >= targetDistance) {
                double remaining = targetDistance - accumulatedDistance;
                double fraction = segLen > 0.0001D ? remaining / segLen : 0.0D;
                Vec3 interpPos = prevPos.lerp(pt.pos(), fraction);
                float interpYaw = net.minecraft.util.Mth.rotLerp((float) fraction, this.getYRot(), pt.yRot());
                float interpPitch = net.minecraft.util.Mth.rotLerp((float) fraction, this.getXRot(), pt.xRot());
                return new TrailPoint(interpPos, interpYaw, interpPitch);
            }
            accumulatedDistance += segLen;
            prevPos = pt.pos();
        }

        TrailPoint last = this.history.get(this.history.size() - 1);
        Vec3 dir = this.calculateViewVector(last.xRot(), last.yRot());
        double extra = targetDistance - accumulatedDistance;
        return new TrailPoint(last.pos().subtract(dir.scale(extra)), last.yRot(), last.xRot());
    }

    private void updateSegments() {
        if (this.level().isClientSide()) return;
        ensureSegmentsInitialized();
        if (this.segments == null) return;

        // Record history
        this.history.add(0, new TrailPoint(this.position(), this.getYRot(), this.getXRot()));
        if (this.history.size() > 120) {
            this.history.remove(this.history.size() - 1);
        }

        // Update each physical segment position and rotation
        for (int i = 0; i < this.segments.length; i++) {
            VoidSeaSerpentSegmentEntity seg = this.segments[i];
            if (seg != null && seg.isAlive()) {
                TrailPoint pt = getPointAtDistance(SEGMENT_DISTANCES[i]);
                seg.setPos(pt.pos().x, pt.pos().y, pt.pos().z);
                seg.setYRot(pt.yRot());
                seg.setXRot(pt.xRot());
                seg.yRotO = pt.yRot();
                seg.xRotO = pt.xRot();
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.updateSegments();

        if (this.level().isClientSide()) {
            if (this.isRoaring()) {
                this.swimAnimationState.stop();
                this.biteAnimationState.stop();
                this.idleAnimationState.stop();
                this.roarAnimationState.startIfStopped(this.tickCount);
            } else if (this.isBiting()) {
                this.roarAnimationState.stop();
                this.swimAnimationState.stop();
                this.idleAnimationState.stop();
                this.biteAnimationState.startIfStopped(this.tickCount);
            } else if (this.getDeltaMovement().horizontalDistanceSqr() > 0.002D || Math.abs(this.getDeltaMovement().y) > 0.002D) {
                this.roarAnimationState.stop();
                this.biteAnimationState.stop();
                this.idleAnimationState.stop();
                this.swimAnimationState.startIfStopped(this.tickCount);
            } else {
                this.roarAnimationState.stop();
                this.biteAnimationState.stop();
                this.swimAnimationState.stop();
                this.idleAnimationState.startIfStopped(this.tickCount);
            }

            // Emissive void ambient motes
            if (this.random.nextFloat() < 0.25F) {
                this.level().addParticle(ParticleTypes.PORTAL,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 1.5D,
                        this.getY() + this.random.nextDouble() * 1.0D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 1.5D,
                        (this.random.nextDouble() - 0.5D) * 0.2D,
                        -0.1D,
                        (this.random.nextDouble() - 0.5D) * 0.2D);
            }
        } else {
            if (this.roarCooldown > 0) this.roarCooldown--;
            if (this.attackCooldown > 0) this.attackCooldown--;
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if (this.segments != null) {
            for (VoidSeaSerpentSegmentEntity seg : this.segments) {
                if (seg != null) {
                    seg.discard();
                }
            }
        }
    }

    @Override
    public void die(net.minecraft.world.damagesource.DamageSource damageSource) {
        super.die(damageSource);
        if (this.segments != null) {
            for (VoidSeaSerpentSegmentEntity seg : this.segments) {
                if (seg != null) {
                    seg.discard();
                }
            }
        }
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(this.getSpeed(), travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
        } else {
            super.travel(travelVector);
        }
    }

    public boolean isRoaring() {
        return this.entityData.get(ROARING);
    }

    public void setRoaring(boolean roaring) {
        this.entityData.set(ROARING, roaring);
    }

    public boolean isBiting() {
        return this.entityData.get(BITING);
    }

    public void setBiting(boolean biting) {
        this.entityData.set(BITING, biting);
    }

    // --- GOALS & CONTROLLERS ---

    static class VoidSerpentMoveControl extends MoveControl {
        private final VoidSeaSerpentEntity serpent;

        public VoidSerpentMoveControl(VoidSeaSerpentEntity serpent) {
            super(serpent);
            this.serpent = serpent;
        }

        @Override
        public void tick() {
            if (this.operation == Operation.MOVE_TO && !this.serpent.getNavigation().isDone()) {
                Vec3 vec = new Vec3(this.wantedX - this.serpent.getX(), this.wantedY - this.serpent.getY(), this.wantedZ - this.serpent.getZ());
                double dist = vec.length();
                if (dist < 0.001D) return;

                vec = vec.scale(this.speedModifier * 0.05D / dist);
                this.serpent.setDeltaMovement(this.serpent.getDeltaMovement().add(vec));

                float targetYaw = (float) (Math.atan2(vec.z, vec.x) * (180F / (float) Math.PI)) - 90.0F;
                this.serpent.setYRot(this.rotlerp(this.serpent.getYRot(), targetYaw, 10.0F));
                this.serpent.yBodyRot = this.serpent.getYRot();
            }
        }
    }

    static class VoidSerpentRandomSwimGoal extends Goal {
        private final VoidSeaSerpentEntity serpent;

        public VoidSerpentRandomSwimGoal(VoidSeaSerpentEntity serpent) {
            this.serpent = serpent;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !this.serpent.getNavigation().isInProgress() && this.serpent.getRandom().nextInt(30) == 0;
        }

        @Override
        public void start() {
            Vec3 pos = this.serpent.position();
            double targetX = pos.x + (this.serpent.getRandom().nextDouble() - 0.5D) * 20.0D;
            double targetY = Math.max(this.serpent.level().getMinY() + 10, pos.y + (this.serpent.getRandom().nextDouble() - 0.5D) * 8.0D);
            double targetZ = pos.z + (this.serpent.getRandom().nextDouble() - 0.5D) * 20.0D;
            this.serpent.getNavigation().moveTo(targetX, targetY, targetZ, 1.0D);
        }
    }

    static class VoidSerpentAttackGoal extends Goal {
        private final VoidSeaSerpentEntity serpent;
        private int attackTick = 0;

        public VoidSerpentAttackGoal(VoidSeaSerpentEntity serpent) {
            this.serpent = serpent;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.serpent.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public void start() {
            this.attackTick = 0;
        }

        @Override
        public void tick() {
            LivingEntity target = this.serpent.getTarget();
            if (target == null) return;

            this.serpent.getLookControl().setLookAt(target, 30.0F, 30.0F);
            this.serpent.getNavigation().moveTo(target, 1.4D);

            double distSq = this.serpent.distanceToSqr(target);
            if (distSq < 16.0D) {
                if (this.attackTick == 0) {
                    this.serpent.setBiting(true);
                    this.attackTick = 20;
                } else if (this.attackTick == 10) {
                    if (this.serpent.distanceToSqr(target) < 18.0D) {
                        target.hurt(this.serpent.damageSources().mobAttack(this.serpent), (float) this.serpent.getAttributeValue(Attributes.ATTACK_DAMAGE));
                        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0), this.serpent);
                        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1), this.serpent);
                        this.serpent.level().playSound(null, this.serpent.getX(), this.serpent.getY(), this.serpent.getZ(), SoundEvents.PHANTOM_BITE, SoundSource.HOSTILE, 1.2F, 0.7F);
                    }
                }
            }

            if (this.attackTick > 0) {
                this.attackTick--;
                if (this.attackTick == 0) {
                    this.serpent.setBiting(false);
                }
            }
        }

        @Override
        public void stop() {
            this.serpent.setBiting(false);
        }
    }

    static class VoidSerpentRoarGoal extends Goal {
        private final VoidSeaSerpentEntity serpent;
        private int roarTimer = 0;

        public VoidSerpentRoarGoal(VoidSeaSerpentEntity serpent) {
            this.serpent = serpent;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return this.serpent.getTarget() != null && this.serpent.roarCooldown <= 0 && this.serpent.distanceToSqr(this.serpent.getTarget()) < 144.0D;
        }

        @Override
        public void start() {
            this.roarTimer = 40;
            this.serpent.setRoaring(true);
            this.serpent.roarCooldown = 200;
            this.serpent.getNavigation().stop();
            this.serpent.level().playSound(null, this.serpent.getX(), this.serpent.getY(), this.serpent.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 1.6F, 0.6F);
        }

        @Override
        public void tick() {
            this.roarTimer--;
            LivingEntity target = this.serpent.getTarget();
            if (target != null) {
                this.serpent.getLookControl().setLookAt(target, 30.0F, 30.0F);
                if (this.roarTimer == 20) {
                    // Sonic void shockwave
                    double dist = Math.sqrt(this.serpent.distanceToSqr(target));
                    if (dist < 12.0D) {
                        Vec3 push = target.position().subtract(this.serpent.position()).normalize().scale(1.2D);
                        target.setDeltaMovement(target.getDeltaMovement().add(push));
                        target.hurt(this.serpent.damageSources().magic(), 6.0F);
                        target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0), this.serpent);
                    }
                }
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.roarTimer > 0;
        }

        @Override
        public void stop() {
            this.serpent.setRoaring(false);
        }
    }
}
