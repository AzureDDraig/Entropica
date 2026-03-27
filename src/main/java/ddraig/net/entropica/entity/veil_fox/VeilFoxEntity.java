package ddraig.net.entropica.entity.veil_fox;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.registry.ModEntityTypes;
import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class VeilFoxEntity extends TamableAnimal {

    private static final EntityDataAccessor<String> ESSENCE_TYPE = SynchedEntityData.defineId(VeilFoxEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> TAMING_PHASE = SynchedEntityData.defineId(VeilFoxEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DETECTING_TARGET_ID = SynchedEntityData.defineId(VeilFoxEntity.class, EntityDataSerializers.INT);

    private int blinkCooldown = 0;
    private int chainBlinksRemaining = 0;

    private final int[] recentTeleportTicks = new int[]{-200, -200, -200, -200};
    private int teleportIndex = 0;

    private int teleportDelay = 0;
    private Vec3 pendingTeleportTarget = null;
    private boolean pendingTeleportSitting = false;

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState blinkOutAnimationState = new AnimationState();
    public final AnimationState blinkInAnimationState = new AnimationState();
    public final AnimationState sleepAnimationState = new AnimationState();
    public final AnimationState sleepInAnimationState = new AnimationState();
    public final AnimationState sleepOutAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState rolloverAnimationState = new AnimationState();
    public final AnimationState digAnimationState = new AnimationState();

    public VeilFoxEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 14.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ESSENCE_TYPE, EssenceType.VOID.name());
        builder.define(TAMING_PHASE, 0);
        builder.define(DETECTING_TARGET_ID, -1);
    }

    public EssenceType getEssenceType() {
        try {
            return EssenceType.valueOf(this.entityData.get(ESSENCE_TYPE));
        } catch (IllegalArgumentException e) {
            return EssenceType.VOID;
        }
    }

    public void setEssenceType(EssenceType type) {
        this.entityData.set(ESSENCE_TYPE, type.name());
    }

    public int getTamingPhase() { return this.entityData.get(TAMING_PHASE); }
    public void setTamingPhase(int phase) { this.entityData.set(TAMING_PHASE, phase); }

    public int getDetectingTargetId() { return this.entityData.get(DETECTING_TARGET_ID); }
    public void setDetectingTargetId(int id) { this.entityData.set(DETECTING_TARGET_ID, id); }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnType, @Nullable SpawnGroupData spawnGroupData) {
        this.setEssenceType(this.getRandom().nextBoolean() ? EssenceType.VOID : EssenceType.UMBRAL);
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new StartledBlinkGoal());
        this.goalSelector.addGoal(3, new ProgressiveTameGoal());
        this.goalSelector.addGoal(4, new SocialBlinkGoal());
        this.goalSelector.addGoal(5, new DetectHiddenTargetGoal());
        this.goalSelector.addGoal(6, new FollowOwnerBlinkingGoal(this, 1.0D, 10.0F, 2.0F));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            boolean isMoving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6;
            if (isMoving || this.blinkOutAnimationState.isStarted() || this.blinkInAnimationState.isStarted() ||
                    this.sleepAnimationState.isStarted() || this.sleepInAnimationState.isStarted() ||
                    this.sleepOutAnimationState.isStarted() || this.attackAnimationState.isStarted() ||
                    this.rolloverAnimationState.isStarted() || this.digAnimationState.isStarted()) {
                this.idleAnimationState.stop();
            } else {
                this.idleAnimationState.startIfStopped(this.tickCount);
            }
        } else {
            if (this.teleportDelay > 0) {
                this.teleportDelay--;
                if (this.teleportDelay == 0 && this.pendingTeleportTarget != null) {
                    this.executeTeleport();
                }
            }
        }

        if (this.blinkCooldown > 0) {
            this.blinkCooldown--;
        }

        // Chained startled blinks explicitly bypass the 4-per-10-seconds limit
        if (!this.level().isClientSide() && this.chainBlinksRemaining > 0 && this.blinkCooldown <= 0) {
            if (this.teleportRandomly(8, true)) {
                this.chainBlinksRemaining--;
                this.blinkCooldown = 10; // Rapid half-second succession
            } else {
                this.chainBlinksRemaining = 0; // Cancel if stuck
            }
        }

        if (this.level().isClientSide() && this.tickCount % 2 == 0) {
            this.level().addParticle(ParticleTypes.PORTAL,
                    this.getRandomX(0.5D), this.getRandomY() + 0.2D, this.getRandomZ(0.5D),
                    (this.random.nextDouble() - 0.5D) * 0.5D, -this.random.nextDouble() * 0.2D, (this.random.nextDouble() - 0.5D) * 0.5D);
        }
    }

    public void scheduleTeleport(Vec3 target) {
        if (!this.level().isClientSide() && this.teleportDelay <= 0) {
            this.pendingTeleportTarget = target;
            this.pendingTeleportSitting = this.isOrderedToSit();
            this.teleportDelay = 2;
            this.level().broadcastEntityEvent(this, (byte) 60);
        }
    }

    private void executeTeleport() {
        double oldX = this.getX();
        double oldY = this.getY();
        double oldZ = this.getZ();
        float oldYBodyRot = this.yBodyRot;
        float oldYHeadRot = this.yHeadRot;
        float oldXRot = this.getXRot();

        if (this.randomTeleport(this.pendingTeleportTarget.x, this.pendingTeleportTarget.y, this.pendingTeleportTarget.z, true)) {
            this.playSound(ModSounds.VEIL_FOX_BLINK.get(), 1.0F, 1.0F);
            this.level().gameEvent(GameEvent.TELEPORT, this.position(), GameEvent.Context.of(this));

            VeilFoxAfterimageEntity ghost = ModEntityTypes.VEIL_FOX_AFTERIMAGE.get().create(this.level(), EntitySpawnReason.TRIGGERED);
            if (ghost != null) {
                ghost.snapTo(oldX, oldY, oldZ, this.getYRot(), this.getXRot());
                ghost.setPoses(oldYBodyRot, oldYHeadRot, oldXRot, this.pendingTeleportSitting);
                this.level().addFreshEntity(ghost);
            }

            this.level().broadcastEntityEvent(this, (byte) 61);
        } else {
            this.level().broadcastEntityEvent(this, (byte) 61);
        }
        this.pendingTeleportTarget = null;
    }

    // New signature includes bypassLimit flag for startled bursts
    public boolean teleportRandomly(int radius, boolean bypassLimit) {
        if (this.level().isClientSide() || this.teleportDelay > 0) {
            return false;
        }

        if (!bypassLimit && this.tickCount - this.recentTeleportTicks[this.teleportIndex] < 200) {
            return false; // Hit the 4-per-10-seconds limit
        }

        Player nearestPlayer = this.level().getNearestPlayer(this, 24.0D);

        for (int i = 0; i < 32; i++) {
            double targetX = this.getX() + (this.random.nextDouble() - 0.5D) * radius * 2;
            double targetY = Mth.clamp(this.getY() + (this.random.nextInt(radius) - radius / 2), this.level().getMinY(), this.level().getMaxY() - 1);
            double targetZ = this.getZ() + (this.random.nextDouble() - 0.5D) * radius * 2;

            Vec3 targetVec = new Vec3(targetX, targetY, targetZ);
            boolean outOfSight = true;

            if (nearestPlayer != null) {
                HitResult hit = this.level().clip(new ClipContext(
                        nearestPlayer.getEyePosition(),
                        targetVec.add(0, this.getEyeHeight(), 0),
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        this
                ));
                outOfSight = hit.getType() != HitResult.Type.MISS;
            }

            if (outOfSight || i > 24) {
                BlockPos targetPos = BlockPos.containing(targetX, targetY, targetZ);
                if (this.level().getBlockState(targetPos).isAir() || i > 28) {
                    if (!bypassLimit) {
                        this.recentTeleportTicks[this.teleportIndex] = this.tickCount;
                        this.teleportIndex = (this.teleportIndex + 1) % 4;
                    }
                    this.scheduleTeleport(targetVec);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 60) {
            this.blinkOutAnimationState.start(this.tickCount);
            this.blinkInAnimationState.stop();
            this.idleAnimationState.stop();
        } else if (id == 61) {
            this.blinkInAnimationState.start(this.tickCount);
            this.blinkOutAnimationState.stop();
            this.idleAnimationState.stop();
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (!this.isTame() && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            if (this.random.nextFloat() < 0.90f) {
                this.teleportRandomly(8, true); // Burst teleport to dodge
                this.chainBlinksRemaining = this.random.nextInt(3);
                this.blinkCooldown = 15;
                return false;
            }
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (this.level().isClientSide()) {
            return super.mobInteract(player, hand);
        }

        if (this.isTame()) {
            if (this.isOwnedBy(player)) {
                this.setOrderedToSit(!this.isOrderedToSit());
                this.jumping = false;
                this.navigation.stop();
                return InteractionResult.SUCCESS;
            }
        } else {
            // Only allow taming interaction if the full 5-phase progressive taming session is complete!
            if (this.getTamingPhase() >= 5) {
                boolean isTamingItem = itemstack.is(ModItems.VEIL_SHARD.get()) ||
                        (itemstack.getItem() instanceof EssenceItem &&
                                (EssenceItem.getEssenceType(itemstack) == EssenceType.VOID || EssenceItem.getEssenceType(itemstack) == EssenceType.UMBRAL));

                if (isTamingItem) {
                    if (!player.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }
                    this.tame(player);
                    this.navigation.stop();
                    this.setTarget(null);
                    this.setOrderedToSit(true);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        this.spawnAtLocation(level, new ItemStack(ModItems.VEIL_SHARD.get(), 1));

        ItemStack essenceDrop = new ItemStack(ModItems.AVERAGE_ESSENCE.get(), 1 + this.random.nextInt(2));
        EssenceItem.setEssenceType(essenceDrop, this.getEssenceType());
        this.spawnAtLocation(level, essenceDrop);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        VeilFoxEntity baby = ModEntityTypes.VEIL_FOX.get().create(level, EntitySpawnReason.BREEDING);
        if (baby != null && this.isTame()) {
            LivingEntity owner = this.getOwner();
            if (owner instanceof Player player) {
                baby.tame(player);
            }
        }
        return baby;
    }

    // ==========================================
    // ADVANCED AI GOALS
    // ==========================================

    class StartledBlinkGoal extends Goal {
        public StartledBlinkGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (VeilFoxEntity.this.isTame() || VeilFoxEntity.this.blinkCooldown > 0 || VeilFoxEntity.this.teleportDelay > 0) return false;

            // Check for sudden stimulus within 8 blocks
            List<Player> nearbyPlayers = VeilFoxEntity.this.level().getEntitiesOfClass(Player.class, VeilFoxEntity.this.getBoundingBox().inflate(8.0D));
            for (Player p : nearbyPlayers) {
                double speedSqr = p.getDeltaMovement().horizontalDistanceSqr();

                // If the player is moving faster than a crouch OR swinging their arm (block place/break/attack)
                if ((speedSqr > 0.002D && !p.isCrouching()) || p.swinging) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void start() {
            VeilFoxEntity.this.teleportRandomly(12, true); // Burst teleport!
            VeilFoxEntity.this.chainBlinksRemaining = 1 + VeilFoxEntity.this.random.nextInt(2); // 2 or 3 total blinks
            VeilFoxEntity.this.blinkCooldown = 15;
            VeilFoxEntity.this.setTamingPhase(0); // Resets taming entirely
        }
    }

    class ProgressiveTameGoal extends Goal {
        private Player tamingTarget;
        private int patienceTimer = 0;

        // 15 blocks -> 10 blocks -> 8 blocks -> 5 blocks -> 2 blocks
        private final int[] phaseDistances = {15, 10, 8, 5, 2};

        // Phase 0 requires 30s (600 ticks). Phases 1-4 require 3 minutes (3600 ticks)
        private final int[] phaseTimes = {600, 3600, 3600, 3600, 3600};

        public ProgressiveTameGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (VeilFoxEntity.this.isTame()) return false;

            List<Player> players = VeilFoxEntity.this.level().getEntitiesOfClass(Player.class, VeilFoxEntity.this.getBoundingBox().inflate(16.0D));
            if (players.isEmpty()) return false;

            Player closest = null;
            double minDist = Double.MAX_VALUE;
            for(Player p : players) {
                double dist = VeilFoxEntity.this.distanceToSqr(p);
                if(dist < minDist) {
                    minDist = dist;
                    closest = p;
                }
            }
            tamingTarget = closest;
            return tamingTarget != null && tamingTarget.isCrouching();
        }

        @Override
        public void tick() {
            if (tamingTarget == null || !tamingTarget.isAlive()) {
                resetTaming();
                return;
            }

            double speedSqr = tamingTarget.getDeltaMovement().horizontalDistanceSqr();

            // Any movement faster than a slow walk (> 0.002D) OR standing up resets it entirely
            if (!tamingTarget.isCrouching() || speedSqr > 0.002D) {
                if (VeilFoxEntity.this.getTamingPhase() > 0) {
                    VeilFoxEntity.this.teleportRandomly(12, true); // Flee!
                }
                resetTaming();
                return;
            }

            int phase = VeilFoxEntity.this.getTamingPhase();

            // Phase 5 means taming is complete, fox approaches
            if (phase >= 5) {
                VeilFoxEntity.this.getNavigation().moveTo(tamingTarget, 0.5D);
                VeilFoxEntity.this.getLookControl().setLookAt(tamingTarget, 10.0F, VeilFoxEntity.this.getMaxHeadXRot());
                return;
            }

            double dist = VeilFoxEntity.this.distanceTo(tamingTarget);
            int requiredDist = phaseDistances[phase];

            // Must be within required range AND exhibiting "crouching stillness" to progress the timer
            if (dist <= requiredDist && speedSqr < 0.0001D) {
                patienceTimer++;
                VeilFoxEntity.this.getLookControl().setLookAt(tamingTarget, 10.0F, VeilFoxEntity.this.getMaxHeadXRot());

                if (patienceTimer >= phaseTimes[phase]) {
                    VeilFoxEntity.this.setTamingPhase(phase + 1);
                    patienceTimer = 0;
                    VeilFoxEntity.this.playSound(SoundEvents.FOX_AGGRO, 0.5F, 1.5F); // Curious chirp
                    VeilFoxEntity.this.level().broadcastEntityEvent(VeilFoxEntity.this, (byte) 7);
                }
            }
        }

        private void resetTaming() {
            VeilFoxEntity.this.setTamingPhase(0);
            patienceTimer = 0;
        }

        @Override
        public void stop() {
            tamingTarget = null;
        }
    }

    class FollowOwnerBlinkingGoal extends Goal {
        private final TamableAnimal tamable;
        private LivingEntity owner;
        private final double maxDist;
        private final double minDist;

        public FollowOwnerBlinkingGoal(TamableAnimal tamable, double speedModifier, float maxDist, float minDist) {
            this.tamable = tamable;
            this.maxDist = maxDist;
            this.minDist = minDist;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity livingentity = this.tamable.getOwner();
            if (livingentity == null || livingentity.isSpectator() || this.tamable.isOrderedToSit() || this.tamable.distanceToSqr(livingentity) < (double)(this.minDist * this.minDist)) {
                return false;
            }
            this.owner = livingentity;
            return true;
        }

        @Override
        public void tick() {
            this.tamable.getLookControl().setLookAt(this.owner, 10.0F, (float)this.tamable.getMaxHeadXRot());

            if (this.tamable.distanceToSqr(this.owner) >= (double)(this.maxDist * this.maxDist)) {
                if (((VeilFoxEntity)this.tamable).blinkCooldown <= 0 && ((VeilFoxEntity)this.tamable).teleportDelay <= 0) {
                    this.blinkToOwner();
                }
            } else {
                this.tamable.getNavigation().moveTo(this.owner, 1.0D);
            }
        }

        private void blinkToOwner() {
            BlockPos targetPos = this.owner.blockPosition().offset(
                    this.tamable.getRandom().nextInt(3) - 1,
                    this.tamable.getRandom().nextInt(2),
                    this.tamable.getRandom().nextInt(3) - 1
            );

            if (this.tamable.level().getBlockState(targetPos).isAir() && this.tamable.level().getBlockState(targetPos.above()).isAir()) {
                ((VeilFoxEntity)this.tamable).scheduleTeleport(new Vec3(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5));
                ((VeilFoxEntity)this.tamable).blinkCooldown = 40;
            }
        }
    }

    class DetectHiddenTargetGoal extends Goal {
        private LivingEntity detectedTarget;

        public DetectHiddenTargetGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!VeilFoxEntity.this.isTame()) return false;
            if (VeilFoxEntity.this.tickCount % 20 != 0) return false;

            List<Monster> hostiles = VeilFoxEntity.this.level().getEntitiesOfClass(Monster.class, VeilFoxEntity.this.getBoundingBox().inflate(16.0D));
            for (Monster m : hostiles) {
                if (m.isInvisible() || !VeilFoxEntity.this.hasLineOfSight(m)) {
                    detectedTarget = m;
                    VeilFoxEntity.this.setDetectingTargetId(m.getId());
                    return true;
                }
            }
            VeilFoxEntity.this.setDetectingTargetId(-1);
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return detectedTarget != null && detectedTarget.isAlive() && VeilFoxEntity.this.distanceToSqr(detectedTarget) < 400.0D;
        }

        @Override
        public void tick() {
            VeilFoxEntity.this.getLookControl().setLookAt(detectedTarget, 30.0F, 30.0F);
        }

        @Override
        public void stop() {
            detectedTarget = null;
            VeilFoxEntity.this.setDetectingTargetId(-1);
        }
    }

    class SocialBlinkGoal extends Goal {
        public SocialBlinkGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (VeilFoxEntity.this.isTame() || VeilFoxEntity.this.level().getDayTime() % 24000L < 12000L) return false;
            if (VeilFoxEntity.this.getRandom().nextInt(200) != 0) return false;

            List<VeilFoxEntity> friends = VeilFoxEntity.this.level().getEntitiesOfClass(VeilFoxEntity.class, VeilFoxEntity.this.getBoundingBox().inflate(20.0D));
            return friends.size() > 1;
        }

        @Override
        public void start() {
            VeilFoxEntity.this.setOrderedToSit(true);
        }

        @Override
        public void tick() {
            if (VeilFoxEntity.this.getRandom().nextInt(60) == 0 && VeilFoxEntity.this.blinkCooldown <= 0) {
                VeilFoxEntity.this.teleportRandomly(4, false); // Standard limit
                VeilFoxEntity.this.blinkCooldown = 100;
                VeilFoxEntity.this.setOrderedToSit(true);
            }
        }

        @Override
        public void stop() {
            VeilFoxEntity.this.setOrderedToSit(false);
        }
    }
}