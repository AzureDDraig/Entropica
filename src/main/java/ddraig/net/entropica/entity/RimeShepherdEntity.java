package ddraig.net.entropica.entity;

import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.mojang.serialization.Codec;

import java.util.EnumSet;
import java.util.List;

public class RimeShepherdEntity extends PathfinderMob {

    private static final EntityDataAccessor<Boolean> STAMPING = SynchedEntityData.defineId(RimeShepherdEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CHARGING = SynchedEntityData.defineId(RimeShepherdEntity.class, EntityDataSerializers.BOOLEAN);

    public final net.minecraft.world.entity.AnimationState idleAnimationState = new net.minecraft.world.entity.AnimationState();
    public final net.minecraft.world.entity.AnimationState walkAnimationState = new net.minecraft.world.entity.AnimationState();
    public final net.minecraft.world.entity.AnimationState runAnimationState = new net.minecraft.world.entity.AnimationState();
    public final net.minecraft.world.entity.AnimationState attackAnimationState = new net.minecraft.world.entity.AnimationState();

    private BlockPos patrolStart = null;
    private BlockPos patrolEnd = null;
    private boolean patrollingToEnd = true;
    private int patrolWaitTimer = 0;
    private int stampTimer = 0;

    public RimeShepherdEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 45.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ShepherdChargeGoal(this));
        this.goalSelector.addGoal(2, new ShepherdPatrolGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new ShepherdDefendHerdGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STAMPING, false);
        builder.define(CHARGING, false);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setStamping(input.read("Stamping", Codec.BOOL).orElse(false));
        this.setCharging(input.read("Charging", Codec.BOOL).orElse(false));
        if (input.read("PatrolStartX", Codec.INT).isPresent()) {
            this.patrolStart = new BlockPos(
                input.read("PatrolStartX", Codec.INT).get(),
                input.read("PatrolStartY", Codec.INT).get(),
                input.read("PatrolStartZ", Codec.INT).get()
            );
            this.patrolEnd = new BlockPos(
                input.read("PatrolEndX", Codec.INT).get(),
                input.read("PatrolEndY", Codec.INT).get(),
                input.read("PatrolEndZ", Codec.INT).get()
            );
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("Stamping", Codec.BOOL, this.isStamping());
        output.store("Charging", Codec.BOOL, this.isCharging());
        if (this.patrolStart != null) {
            output.store("PatrolStartX", Codec.INT, this.patrolStart.getX());
            output.store("PatrolStartY", Codec.INT, this.patrolStart.getY());
            output.store("PatrolStartZ", Codec.INT, this.patrolStart.getZ());
            output.store("PatrolEndX", Codec.INT, this.patrolEnd.getX());
            output.store("PatrolEndY", Codec.INT, this.patrolEnd.getY());
            output.store("PatrolEndZ", Codec.INT, this.patrolEnd.getZ());
        }
    }

    public boolean isStamping() {
        return this.entityData.get(STAMPING);
    }

    public void setStamping(boolean stamping) {
        this.entityData.set(STAMPING, stamping);
    }

    public boolean isCharging() {
        return this.entityData.get(CHARGING);
    }

    public void setCharging(boolean charging) {
        this.entityData.set(CHARGING, charging);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide()) {
            if (this.patrolStart == null) {
                this.patrolStart = this.blockPosition();
                // Find a water source or random point nearby for patrol endpoint
                this.patrolEnd = this.blockPosition().offset(this.random.nextInt(32) - 16, 0, this.random.nextInt(32) - 16);
            }

            // Herd Commanding Logic (Direct commanding nearby passive herd animals to follow)
            if (this.tickCount % 20 == 0) {
                List<Animal> nearbyAnimals = this.level().getEntitiesOfClass(Animal.class, this.getBoundingBox().inflate(16.0D));
                int followers = 0;
                for (Animal animal : nearbyAnimals) {
                    if (followers >= 12) break;
                    if (animal.getType() == EntityType.SHEEP || animal.getType() == EntityType.COW || animal.getType() == EntityType.PIG || animal.getType() == EntityType.HORSE) {
                        double distSq = animal.distanceToSqr(this);
                        if (distSq > 16.0D && distSq < 256.0D) {
                            animal.getNavigation().moveTo(this.getX(), this.getY(), this.getZ(), 1.0D);
                            animal.getLookControl().setLookAt(this, 10.0F, 10.0F);
                        }
                        followers++;
                    }
                }
            }

            // Handle stamping timer
            if (this.isStamping()) {
                this.stampTimer--;
                if (this.stampTimer <= 0) {
                    this.setStamping(false);
                }
            }
        }

        // Condensation breath particle
        if (this.level().isClientSide() && this.random.nextFloat() < 0.04f) {
            Vec3 breathPos = this.position().add(this.getViewVector(1.0F).scale(0.8D)).add(0.0D, this.getEyeHeight() - 0.2D, 0.0D);
            this.level().addParticle(ParticleTypes.CLOUD, breathPos.x, breathPos.y, breathPos.z, this.getViewVector(1.0F).x * 0.05D, 0.01D, this.getViewVector(1.0F).z * 0.05D);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.idleAnimationState.animateWhen(!this.walkAnimationState.isStarted() && !this.runAnimationState.isStarted() && !this.attackAnimationState.isStarted(), this.tickCount);
            if (this.isCharging()) {
                this.runAnimationState.startIfStopped(this.tickCount);
                this.walkAnimationState.stop();
            } else if (this.getDeltaMovement().horizontalDistanceSqr() > 0.001D) {
                this.walkAnimationState.startIfStopped(this.tickCount);
                this.runAnimationState.stop();
            } else {
                this.walkAnimationState.stop();
                this.runAnimationState.stop();
            }
            if (this.isStamping()) {
                this.attackAnimationState.startIfStopped(this.tickCount);
            } else {
                this.attackAnimationState.stop();
            }
        }
    }

    public boolean isHerdAnimal(LivingEntity entity) {
        if (entity instanceof Animal animal) {
            return animal.getType() == EntityType.SHEEP || animal.getType() == EntityType.COW || animal.getType() == EntityType.PIG || animal.getType() == EntityType.HORSE;
        }
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel serverLevel, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(serverLevel, damageSource, recentlyHit);
        this.spawnAtLocation(serverLevel, new ItemStack(ModItems.RIME_ANTLER_FRAGMENT.get(), 1 + this.random.nextInt(2)));
        this.spawnAtLocation(serverLevel, new ItemStack(ModItems.SHEPHERDS_FROST.get(), 1));
    }

    // --- GOALS ---

    public static class ShepherdPatrolGoal extends Goal {
        private final RimeShepherdEntity shepherd;

        public ShepherdPatrolGoal(RimeShepherdEntity shepherd) {
            this.shepherd = shepherd;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !shepherd.isCharging() && !shepherd.isStamping() && shepherd.getTarget() == null && shepherd.patrolStart != null;
        }

        @Override
        public void tick() {
            BlockPos targetPos = shepherd.patrollingToEnd ? shepherd.patrolEnd : shepherd.patrolStart;
            shepherd.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 0.8D);

            if (shepherd.blockPosition().distSqr(targetPos) < 9.0D) {
                shepherd.patrolWaitTimer++;
                if (shepherd.patrolWaitTimer >= 200) { // 10 seconds pause
                    shepherd.patrolWaitTimer = 0;
                    shepherd.patrollingToEnd = !shepherd.patrollingToEnd;
                }
            }
        }
    }

    public static class ShepherdDefendHerdGoal extends Goal {
        private final RimeShepherdEntity shepherd;
        private LivingEntity threat;

        public ShepherdDefendHerdGoal(RimeShepherdEntity shepherd) {
            this.shepherd = shepherd;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            List<LivingEntity> entities = shepherd.level().getEntitiesOfClass(LivingEntity.class, shepherd.getBoundingBox().inflate(16.0D));
            for (LivingEntity entity : entities) {
                if (entity instanceof Mob mob && mob.getTarget() != null && shepherd.isHerdAnimal(mob.getTarget())) {
                    this.threat = entity;
                    return true;
                }
                if (entity instanceof Player player && player.getLastHurtMob() != null && shepherd.isHerdAnimal(player.getLastHurtMob())) {
                    this.threat = player;
                    return true;
                }
            }
            return false;
        }

        @Override
        public void start() {
            shepherd.setTarget(this.threat);
            shepherd.setStamping(true);
            shepherd.stampTimer = 30; // 1.5 seconds stamping
            shepherd.level().playSound(null, shepherd.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 1.0F, 0.5F);
        }
    }

    public static class ShepherdChargeGoal extends Goal {
        private final RimeShepherdEntity shepherd;
        private LivingEntity target;
        private int cooldown = 0;

        public ShepherdChargeGoal(RimeShepherdEntity shepherd) {
            this.shepherd = shepherd;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (cooldown > 0) {
                cooldown--;
                return false;
            }
            if (shepherd.isStamping()) return false;
            this.target = shepherd.getTarget();
            return this.target != null && this.target.isAlive();
        }

        @Override
        public void start() {
            shepherd.setCharging(true);
        }

        @Override
        public void stop() {
            shepherd.setCharging(false);
            this.cooldown = 100;
        }

        @Override
        public void tick() {
            if (target == null) return;
            shepherd.getLookControl().setLookAt(target, 30.0F, 30.0F);
            shepherd.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.5D);

            if (shepherd.distanceToSqr(target) <= 4.0D) {
                target.hurt(shepherd.damageSources().mobAttack(shepherd), 8.0F);
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 3)); // Freezing Slowness IV
                Vec3 kb = target.position().subtract(shepherd.position()).normalize().scale(1.4D);
                target.setDeltaMovement(kb.x, 0.35D, kb.z);
                this.stop();
            }
        }
    }
}
