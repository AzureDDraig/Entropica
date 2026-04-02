package ddraig.net.entropica.entity.ashen_stalker;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.grot.GrotEntity;
import ddraig.net.entropica.entity.veil_fox.VeilFoxEntity;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class AshenStalkerEntity extends PathfinderMob {

    // --- STATE TRACKING ---
    // 0 = Patrol, 1 = Stalking, 2 = Sprinting/Killing, 3 = Feeding
    private static final EntityDataAccessor<Integer> HUNT_PHASE = SynchedEntityData.defineId(AshenStalkerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_SCENTING = SynchedEntityData.defineId(AshenStalkerEntity.class, EntityDataSerializers.BOOLEAN);

    public int feedTimer = 0;
    public BlockPos killPos = null;
    public BlockPos territoryCenter = null;

    // Player Stalking Memory
    public BlockPos lastKnownPlayerPos = null;
    public UUID rememberedPlayerUUID = null;
    public boolean isGrudgeTriggered = false;
    public boolean forceScent = false;

    // --- ANIMATION STATES ---
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState stalkAnimationState = new AnimationState();
    public final AnimationState sprintAnimationState = new AnimationState();
    public final AnimationState feedAnimationState = new AnimationState();
    public final AnimationState scentAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState swipeAnimationState = new AnimationState();

    public AshenStalkerEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 55.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0D) // Added to give the bite some force!
                .add(Attributes.FOLLOW_RANGE, 32.0D) // Toned down to 32 to complement the 24 block tracking radius
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HUNT_PHASE, 0);
        builder.define(IS_SCENTING, false);
    }

    public int getHuntPhase() { return this.entityData.get(HUNT_PHASE); }
    public void setHuntPhase(int phase) { this.entityData.set(HUNT_PHASE, phase); }

    public boolean isScenting() { return this.entityData.get(IS_SCENTING); }
    public void setScenting(boolean scenting) { this.entityData.set(IS_SCENTING, scenting); }

    @Override
    public int getMaxHeadYRot() {
        return 0; // Completely locks horizontal head rotation to the body!
    }

    @Override
    public int getMaxHeadXRot() {
        return 0; // Completely locks vertical head rotation to the body!
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("HuntPhase", Codec.INT, this.getHuntPhase());
        output.store("FeedTimer", Codec.INT, this.feedTimer);
        output.store("GrudgeTriggered", Codec.BOOL, this.isGrudgeTriggered);
        if (this.killPos != null) {
            output.store("KillPosX", Codec.INT, this.killPos.getX());
            output.store("KillPosY", Codec.INT, this.killPos.getY());
            output.store("KillPosZ", Codec.INT, this.killPos.getZ());
        }
        if (this.territoryCenter != null) {
            output.store("TerritoryX", Codec.INT, this.territoryCenter.getX());
            output.store("TerritoryY", Codec.INT, this.territoryCenter.getY());
            output.store("TerritoryZ", Codec.INT, this.territoryCenter.getZ());
        }
        if (this.rememberedPlayerUUID != null) {
            output.store("RememberedPlayer", Codec.STRING, this.rememberedPlayerUUID.toString());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setHuntPhase(input.read("HuntPhase", Codec.INT).orElse(0));
        this.feedTimer = input.read("FeedTimer", Codec.INT).orElse(0);
        this.isGrudgeTriggered = input.read("GrudgeTriggered", Codec.BOOL).orElse(false);

        int kX = input.read("KillPosX", Codec.INT).orElse(0);
        int kY = input.read("KillPosY", Codec.INT).orElse(0);
        int kZ = input.read("KillPosZ", Codec.INT).orElse(0);
        if (kY != 0) this.killPos = new BlockPos(kX, kY, kZ);

        int tX = input.read("TerritoryX", Codec.INT).orElse(0);
        int tY = input.read("TerritoryY", Codec.INT).orElse(0);
        int tZ = input.read("TerritoryZ", Codec.INT).orElse(0);
        if (tY != 0) this.territoryCenter = new BlockPos(tX, tY, tZ);

        String uuidStr = input.read("RememberedPlayer", Codec.STRING).orElse("");
        if (!uuidStr.isEmpty()) {
            try {
                this.rememberedPlayerUUID = UUID.fromString(uuidStr);
            } catch (Exception e) {}
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnType, @Nullable SpawnGroupData spawnGroupData) {
        this.territoryCenter = this.blockPosition(); // Claim territory upon spawning
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // Custom Hunt & Feed Behaviors
        this.goalSelector.addGoal(1, new StalkerTheftAggroGoal());
        this.goalSelector.addGoal(2, new StalkerFeedGoal());
        this.goalSelector.addGoal(3, new StalkerHuntGoal());
        this.goalSelector.addGoal(4, new StalkerScentingGoal());
        this.goalSelector.addGoal(5, new StalkerInvestigateGoal());

        // Territorial Patrol
        this.goalSelector.addGoal(6, new StalkerTerritoryPatrolGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        // Targeting
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());

        // Territorial Exclusion (Custom method bypassing NearestAttackableTargetGoal entirely)
        this.targetSelector.addGoal(2, new StalkerTerritoryTargetGoal());

        // Prey Targeting (Custom method bypassing NearestAttackableTargetGoal entirely)
        this.targetSelector.addGoal(3, new StalkerPreyTargetGoal());

        // Scent Detection Target (Tracks sprinting players blindly)
        this.targetSelector.addGoal(4, new StalkerBlindScentTargetGoal());

        // Intraspecies Aggression
        this.targetSelector.addGoal(5, new StalkerClashTargetGoal());
    }

    @Override
    public void tick() {
        // --- TARGET INTERFERENCE & KILL STEAL MECHANIC ---
        // Runs before super.tick() so it catches the interference even if the target dies this exact tick
        if (!this.level().isClientSide() && (this.getHuntPhase() == 1 || this.getHuntPhase() == 2)) {
            LivingEntity target = this.getTarget();
            if (target != null) {
                LivingEntity attacker = target.getLastHurtByMob();
                // If someone else hurts our prey, switch target to them! (Unless it's a packmate)
                if (attacker != null && attacker != this && !(attacker instanceof AshenStalkerEntity) && attacker.isAlive() && target.tickCount - target.getLastHurtByMobTimestamp() < 10) {
                    this.setTarget(attacker);
                    if (attacker instanceof Player p) {
                        this.rememberedPlayerUUID = p.getUUID();
                        this.isGrudgeTriggered = true;
                    }
                    this.setHuntPhase(2);
                    this.playSound(SoundEvents.ENDER_DRAGON_GROWL, 1.0F, 1.5F); // Angry Roar
                }
            }
        }

        super.tick();

        if (this.level().isClientSide()) {
            handleClientAnimations();
        } else {
            // Failsafe: claim territory if loaded without one
            if (this.territoryCenter == null) {
                this.territoryCenter = this.blockPosition();
            }
        }
    }

    private void handleClientAnimations() {
        boolean isMoving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6;
        int phase = this.getHuntPhase();

        this.idleAnimationState.animateWhen(!isMoving && phase == 0 && !this.isScenting(), this.tickCount);
        this.walkAnimationState.animateWhen(isMoving && phase == 0, this.tickCount);

        this.stalkAnimationState.animateWhen(isMoving && phase == 1, this.tickCount);
        this.sprintAnimationState.animateWhen(isMoving && phase == 2, this.tickCount);

        this.feedAnimationState.animateWhen(phase == 3, this.tickCount);

        if (this.isScenting() && !this.scentAnimationState.isStarted()) {
            this.scentAnimationState.start(this.tickCount);
        } else if (!this.isScenting()) {
            this.scentAnimationState.stop();
        }

        if (this.isScenting() && this.tickCount % 5 == 0) {
            this.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, this.getRandomX(0.5D), this.getEyeY(), this.getRandomZ(0.5D), 0, 0.05, 0);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 4) {
            this.attackAnimationState.start(this.tickCount);
        } else if (id == 5) {
            this.swipeAnimationState.start(this.tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        // CRITICAL FIX: Wipe the target's pre-existing Invulnerability Frames (I-Frames).
        // Without this, the Vanilla engine will completely reject the attack if the player
        // took any damage (like fall damage or a previous hit) within the last 20 ticks!
        target.invulnerableTime = 0;

        // Let the Native Vanilla combat engine handle armor checks, shields, enchantments, and knockback natively!
        boolean attackSuccess = super.doHurtTarget(level, target);

        if (attackSuccess) {
            this.playSound(SoundEvents.FOX_BITE, 1.0F, 0.5F); // Give audio feedback

            // OVERRIDE AGAIN: Clear the newly-created I-frames so the Stalker's furious 12-tick bite flurry actually lands!
            target.invulnerableTime = 0;
        }

        return attackSuccess;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        // If attacked while feeding, immediately cancel feeding and retaliate wildly!
        if (this.getHuntPhase() == 3) {
            this.setHuntPhase(2);
            this.feedTimer = 0;
            this.playSound(SoundEvents.ENDER_DRAGON_GROWL, 1.0F, 1.5F); // Raspy Hiss placeholder
            if (source.getEntity() instanceof LivingEntity living) {
                this.setTarget(living);
                if (living instanceof Player p) {
                    this.rememberedPlayerUUID = p.getUUID(); // Remember who hit them
                    this.isGrudgeTriggered = true; // Instantly trigger grudge since they clearly saw us
                }
            }
        } else if (source.getEntity() instanceof Player p) {
            this.rememberedPlayerUUID = p.getUUID(); // Hold a grudge against the attacker
            this.isGrudgeTriggered = true;
        }

        return super.hurtServer(level, source, amount);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);

        // 1. Nether Essence (Strong)
        ItemStack netherEssence = new ItemStack(ModItems.STRONG_ESSENCE.get(), 1 + this.random.nextInt(2));
        EssenceItem.setEssenceType(netherEssence, EssenceType.NETHER);
        this.spawnAtLocation(level, netherEssence);

        // 2. Arid Essence (Average)
        ItemStack aridEssence = new ItemStack(ModItems.AVERAGE_ESSENCE.get(), 1 + this.random.nextInt(2));
        EssenceItem.setEssenceType(aridEssence, EssenceType.ARID);
        this.spawnAtLocation(level, aridEssence);

        // 3. Ashen Hide Placeholder (Using Leather for now until specific item is ready)
        this.spawnAtLocation(level, new ItemStack(Items.LEATHER, 1 + this.random.nextInt(2)));

        // 4. Rare Ember Claw
        if (this.random.nextFloat() < 0.15f) {
            this.spawnAtLocation(level, new ItemStack(Items.BLAZE_POWDER, 1)); // Placeholder for Ember Claw
        }
    }

    // ==========================================
    // CUSTOM AI GOALS
    // ==========================================

    class StalkerInvestigateGoal extends Goal {
        public StalkerInvestigateGoal() { this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)); }

        @Override
        public boolean canUse() {
            return AshenStalkerEntity.this.getTarget() == null
                    && AshenStalkerEntity.this.lastKnownPlayerPos != null
                    && AshenStalkerEntity.this.getHuntPhase() == 0;
        }

        @Override
        public void start() {
            AshenStalkerEntity.this.getNavigation().moveTo(
                    AshenStalkerEntity.this.lastKnownPlayerPos.getX(),
                    AshenStalkerEntity.this.lastKnownPlayerPos.getY(),
                    AshenStalkerEntity.this.lastKnownPlayerPos.getZ(),
                    1.0D);
        }

        @Override
        public void tick() {
            if (AshenStalkerEntity.this.lastKnownPlayerPos != null) {
                if (AshenStalkerEntity.this.distanceToSqr(Vec3.atCenterOf(AshenStalkerEntity.this.lastKnownPlayerPos)) < 16.0D || AshenStalkerEntity.this.getNavigation().isDone()) {
                    AshenStalkerEntity.this.lastKnownPlayerPos = null;
                    AshenStalkerEntity.this.forceScent = true; // Instantly trigger scent check when arriving!
                }
            }
        }
    }

    class StalkerScentingGoal extends Goal {
        private int scentTimer = 0;

        public StalkerScentingGoal() { this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)); }

        @Override
        public boolean canUse() {
            if (AshenStalkerEntity.this.getHuntPhase() != 0) return false;
            if (AshenStalkerEntity.this.getTarget() != null) return false;

            boolean triggered = AshenStalkerEntity.this.forceScent || AshenStalkerEntity.this.getRandom().nextInt(200) == 0;
            if (!triggered) return false;

            // Check if there are prey animals nearby before casually stopping to scent.
            // If forceScent is true (due to losing a player), ignore this and sniff anyway.
            if (!AshenStalkerEntity.this.forceScent) {
                AABB searchBox = AshenStalkerEntity.this.getBoundingBox().inflate(24.0D);
                List<LivingEntity> prey = AshenStalkerEntity.this.level().getEntitiesOfClass(LivingEntity.class, searchBox, a ->
                        (a instanceof Pig || a instanceof Sheep || a instanceof Cow || a instanceof Horse || a instanceof VeilFoxEntity || a instanceof GrotEntity ||
                                (a instanceof Player p && !p.isCreative() && !p.isSpectator() && !p.isCrouching())) && AshenStalkerEntity.this.hasLineOfSight(a)
                );
                if (!prey.isEmpty()) return false;
            }

            return true;
        }

        @Override
        public void start() {
            scentTimer = 100; // 5 seconds of stopping to sniff the air (matches idle_search animation)
            AshenStalkerEntity.this.setScenting(true);
            AshenStalkerEntity.this.getNavigation().stop();
            AshenStalkerEntity.this.forceScent = false;
        }

        @Override
        public boolean canContinueToUse() {
            return scentTimer > 0 && AshenStalkerEntity.this.getTarget() == null && AshenStalkerEntity.this.getHuntPhase() == 0;
        }

        @Override
        public void tick() {
            scentTimer--;
            AshenStalkerEntity.this.getNavigation().stop();
            AshenStalkerEntity.this.setDeltaMovement(0, AshenStalkerEntity.this.getDeltaMovement().y, 0); // Lock completely in place
        }

        @Override
        public void stop() {
            AshenStalkerEntity.this.setScenting(false);

            // --- SNEAKING PLAYER DETECTION ---
            // Post-sniff: actively sweep for ANY players within 24 blocks, EVEN SNEAKING ONES!
            AABB searchBox = AshenStalkerEntity.this.getBoundingBox().inflate(24.0D);
            List<Player> hiddenPlayers = AshenStalkerEntity.this.level().getEntitiesOfClass(Player.class, searchBox, p -> !p.isCreative() && !p.isSpectator());
            if (!hiddenPlayers.isEmpty()) {
                hiddenPlayers.sort(java.util.Comparator.comparingDouble(AshenStalkerEntity.this::distanceToSqr));

                Player spottedPlayer = hiddenPlayers.get(0);
                AshenStalkerEntity.this.setTarget(spottedPlayer);

                // Always start with Phase 1 (Stalking). StalkerHuntGoal will handle the
                // transition to Sprinting if the player makes eye contact!
                AshenStalkerEntity.this.setHuntPhase(1);
                AshenStalkerEntity.this.playSound(SoundEvents.FOX_AGGRO, 1.0F, 0.6F); // Creepy recognition noise
            }
        }
    }

    class StalkerTerritoryPatrolGoal extends WaterAvoidingRandomStrollGoal {
        public StalkerTerritoryPatrolGoal(PathfinderMob mob, double speed) {
            super(mob, speed);
        }

        @Override
        public boolean canUse() {
            return AshenStalkerEntity.this.getHuntPhase() == 0
                    && !AshenStalkerEntity.this.isScenting()
                    && super.canUse();
        }

        @Nullable
        @Override
        protected Vec3 getPosition() {
            if (AshenStalkerEntity.this.territoryCenter != null) {
                // Ensure patrol stays within 50 blocks of the territory center
                Vec3 randomPos = super.getPosition();
                if (randomPos != null && randomPos.distanceToSqr(Vec3.atCenterOf(AshenStalkerEntity.this.territoryCenter)) > 2500.0D) {
                    // Pathfind back towards center if drifting too far
                    return Vec3.atCenterOf(AshenStalkerEntity.this.territoryCenter).add(
                            (AshenStalkerEntity.this.random.nextDouble() - 0.5) * 20,
                            0,
                            (AshenStalkerEntity.this.random.nextDouble() - 0.5) * 20);
                }
                return randomPos;
            }
            return super.getPosition();
        }
    }

    class StalkerHuntGoal extends Goal {
        private int attackTick = 0;
        private int pathUpdateTimer = 0;

        public StalkerHuntGoal() { this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)); }

        @Override
        public boolean canUse() {
            return AshenStalkerEntity.this.getTarget() != null
                    && AshenStalkerEntity.this.getHuntPhase() != 3; // Not feeding
        }

        @Override
        public void start() {
            AshenStalkerEntity.this.setScenting(false);
            this.pathUpdateTimer = 0;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = AshenStalkerEntity.this.getTarget();
            if (target == null || !target.isAlive() || AshenStalkerEntity.this.getHuntPhase() == 3) {
                return false;
            }
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = AshenStalkerEntity.this.getTarget();
            if (target == null) return;

            AshenStalkerEntity.this.getLookControl().setLookAt(target, 30.0F, 30.0F);

            // Raw center-to-center distance squared (used for memory/drop-off)
            double distSq = AshenStalkerEntity.this.distanceToSqr(target);

            // Clean edge-to-edge distance (Math.max prevents negative values if they completely overlap)
            double edgeDistance = Math.max(0.0D, AshenStalkerEntity.this.distanceTo(target) - (AshenStalkerEntity.this.getBbWidth() / 2.0F) - (target.getBbWidth() / 2.0F));

            boolean isPlayer = target instanceof Player;
            boolean isSprintingPlayer = isPlayer && ((Player) target).isSprinting();
            boolean isKnownPlayer = isPlayer && target.getUUID().equals(AshenStalkerEntity.this.rememberedPlayerUUID);

            // Precise manual Raytrace to guarantee absolute Line of Sight, bypassing weird vanilla caching
            net.minecraft.world.phys.HitResult rayTraceHit = AshenStalkerEntity.this.level().clip(new net.minecraft.world.level.ClipContext(
                    AshenStalkerEntity.this.getEyePosition(),
                    target.getEyePosition(),
                    net.minecraft.world.level.ClipContext.Block.COLLIDER,
                    net.minecraft.world.level.ClipContext.Fluid.NONE,
                    AshenStalkerEntity.this
            ));
            boolean hasLoS = rayTraceHit.getType() == net.minecraft.world.phys.HitResult.Type.MISS;

            // --- RED LIGHT, GREEN LIGHT EYE CONTACT MECHANIC ---
            if (isKnownPlayer && !AshenStalkerEntity.this.isGrudgeTriggered) {
                Vec3 vecToStalker = AshenStalkerEntity.this.getEyePosition().subtract(target.getEyePosition()).normalize();
                Vec3 playerLook = target.getLookAngle().normalize();

                // Dot product > 0.85 equals roughly a 60-degree field of view cone from the player's crosshair
                if (vecToStalker.dot(playerLook) > 0.85D && hasLoS) {
                    AshenStalkerEntity.this.isGrudgeTriggered = true; // They saw us!
                    AshenStalkerEntity.this.playSound(SoundEvents.ENDER_DRAGON_GROWL, 1.0F, 1.5F); // Raspy Hiss
                }
            }

            // Player Tracking Memory: Lost track of them
            double maxTrackDistSq = 576.0D;

            if (isPlayer && distSq > maxTrackDistSq && !isSprintingPlayer && !AshenStalkerEntity.this.isGrudgeTriggered) {
                AshenStalkerEntity.this.lastKnownPlayerPos = target.blockPosition();
                AshenStalkerEntity.this.rememberedPlayerUUID = target.getUUID(); // Remember them for next time!
                AshenStalkerEntity.this.isGrudgeTriggered = false; // Reset grudge state
                AshenStalkerEntity.this.setTarget(null);
                AshenStalkerEntity.this.setHuntPhase(0);
                return;
            }

            // Normal animal tracking (Drops target if too far)
            if (!isPlayer && distSq > maxTrackDistSq) {
                AshenStalkerEntity.this.setTarget(null);
                AshenStalkerEntity.this.setHuntPhase(0);
                return;
            }

            // --- PRECISE DISTANCE LOGIC ---
            // Gap of 2.1D keeps the stalker at a very terrifying standoff distance!
            boolean inStopRange = AshenStalkerEntity.this.getBoundingBox().inflate(2.1D).intersects(target.getBoundingBox());
            boolean inAttackRange = AshenStalkerEntity.this.getBoundingBox().inflate(2.2D).intersects(target.getBoundingBox());

            this.pathUpdateTimer--;

            if (this.pathUpdateTimer <= 0) {
                this.pathUpdateTimer = 10; // Only recalculate path twice a second to preserve momentum!

                // Flanking Logic
                boolean isFlanking = false;
                Vec3 moveTarget = target.position();
                List<AshenStalkerEntity> pack = AshenStalkerEntity.this.level().getEntitiesOfClass(AshenStalkerEntity.class, AshenStalkerEntity.this.getBoundingBox().inflate(24.0D), s -> s != AshenStalkerEntity.this && s.getTarget() == target);
                if (!pack.isEmpty()) {
                    AshenStalkerEntity buddy = pack.get(0);
                    if (buddy.distanceToSqr(target) < distSq) { // Buddy is closer, we should flank!
                        Vec3 targetToBuddy = buddy.position().subtract(target.position()).normalize();
                        moveTarget = target.position().subtract(targetToBuddy.scale(5.0)); // Move to opposite side
                        isFlanking = true;
                    }
                }

                // --- HUNTING MOVEMENT ---
                if (isPlayer) {
                    // If they run, get too close, OR they looked directly at us, Hunt them instantly!
                    if (isSprintingPlayer || distSq < 64.0D || AshenStalkerEntity.this.isGrudgeTriggered) {
                        // PHASE 2: Sprinting Hunt
                        AshenStalkerEntity.this.setHuntPhase(2);

                        if (inStopRange) {
                            AshenStalkerEntity.this.getNavigation().stop();
                        } else if (isFlanking) {
                            AshenStalkerEntity.this.getNavigation().moveTo(moveTarget.x, moveTarget.y, moveTarget.z, 1.6D);
                        } else {
                            AshenStalkerEntity.this.getNavigation().moveTo(target, 1.6D);
                        }
                    } else {
                        // PHASE 1: Creepy Stalking
                        AshenStalkerEntity.this.setHuntPhase(1);

                        Vec3 vecToStalker = AshenStalkerEntity.this.position().subtract(target.position()).normalize();
                        Vec3 playerLook = target.getLookAngle().normalize();

                        // If the player is actively looking in the stalker's general direction (but didn't directly spot it)
                        if (vecToStalker.dot(playerLook) > 0.3) {
                            // Try to move laterally to hide!
                            Vec3 sidestep = playerLook.cross(new Vec3(0, 1, 0)).normalize().scale(8.0);
                            if (AshenStalkerEntity.this.tickCount % 100 > 50) sidestep = sidestep.scale(-1); // Switch direction occasionally
                            Vec3 hidePos = AshenStalkerEntity.this.position().add(sidestep);
                            AshenStalkerEntity.this.getNavigation().moveTo(hidePos.x, hidePos.y, hidePos.z, 0.7D);
                        } else {
                            // Player is looking away, creep up slowly
                            if (inStopRange) {
                                AshenStalkerEntity.this.getNavigation().stop();
                            } else {
                                AshenStalkerEntity.this.getNavigation().moveTo(target, 0.5D);
                            }
                        }
                    }
                } else {
                    // Normal Animal Hunting
                    double speed = distSq > 256.0D ? 0.5D : 1.6D;
                    AshenStalkerEntity.this.setHuntPhase(distSq > 256.0D ? 1 : 2);

                    if (inStopRange) {
                        AshenStalkerEntity.this.getNavigation().stop();
                    } else if (isFlanking) {
                        AshenStalkerEntity.this.getNavigation().moveTo(moveTarget.x, moveTarget.y, moveTarget.z, speed);
                    } else {
                        AshenStalkerEntity.this.getNavigation().moveTo(target, speed);
                    }
                }
            }

            // Attack Execution (Happens every tick independently of the pathfinding update)
            if (attackTick > 0) attackTick--;

            if (attackTick <= 0 && inAttackRange && hasLoS) {
                attackTick = 12; // 0.6 seconds! Guarantees the player's 10-tick invulnerability window is cleared before the next bite!

                AshenStalkerEntity.this.level().broadcastEntityEvent(AshenStalkerEntity.this, (byte) 4); // Bite exclusively

                if (AshenStalkerEntity.this.level() instanceof ServerLevel sl) {
                    // Uses the fully native vanilla method now located at AshenStalkerEntity.doHurtTarget()!
                    boolean attackSuccess = AshenStalkerEntity.this.doHurtTarget(sl, target);

                    if (attackSuccess && !target.isAlive()) {
                        // PREY KILLED! Transition to Feeding!
                        AshenStalkerEntity.this.isGrudgeTriggered = false; // Reset Grudge on kill
                        AshenStalkerEntity.this.setTarget(null);
                        AshenStalkerEntity.this.setHuntPhase(3);
                        AshenStalkerEntity.this.feedTimer = 200; // 10 seconds!
                        AshenStalkerEntity.this.killPos = target.blockPosition();
                    }
                }
            }
        }

        @Override
        public void stop() {
            AshenStalkerEntity.this.isGrudgeTriggered = false; // Ensure grudge is cleared if the goal is stopped
        }
    }

    class StalkerFeedGoal extends Goal {
        public StalkerFeedGoal() { this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)); }

        @Override
        public boolean canUse() {
            return AshenStalkerEntity.this.getHuntPhase() == 3 && AshenStalkerEntity.this.feedTimer > 0;
        }

        @Override
        public boolean canContinueToUse() {
            // Keep feeding even if a target sneaks into range (unless it is explicitly cleared by taking damage or theft)
            return AshenStalkerEntity.this.feedTimer > 0 && AshenStalkerEntity.this.getHuntPhase() == 3;
        }

        @Override
        public void tick() {
            AshenStalkerEntity.this.feedTimer--;

            if (AshenStalkerEntity.this.killPos != null) {
                if (AshenStalkerEntity.this.distanceToSqr(Vec3.atCenterOf(AshenStalkerEntity.this.killPos)) > 4.0D) {
                    AshenStalkerEntity.this.getNavigation().moveTo(AshenStalkerEntity.this.killPos.getX(), AshenStalkerEntity.this.killPos.getY(), AshenStalkerEntity.this.killPos.getZ(), 1.0D);
                } else {
                    AshenStalkerEntity.this.getNavigation().stop();
                }
            }

            if (AshenStalkerEntity.this.feedTimer <= 0) {
                AshenStalkerEntity.this.setHuntPhase(0); // Return to patrol
                AshenStalkerEntity.this.killPos = null;
            }
        }
    }

    class StalkerTheftAggroGoal extends Goal {
        // High priority goal that interrupts feeding if a player steals the kill
        public StalkerTheftAggroGoal() { this.setFlags(EnumSet.of(Goal.Flag.TARGET)); }

        @Override
        public boolean canUse() {
            if (AshenStalkerEntity.this.getHuntPhase() != 3 || AshenStalkerEntity.this.killPos == null) return false;

            // Scan for players within 5 blocks of the kill site who aren't sneaking
            List<Player> thieves = AshenStalkerEntity.this.level().getEntitiesOfClass(Player.class, new AABB(AshenStalkerEntity.this.killPos).inflate(5.0D),
                    p -> !p.isCreative() && !p.isSpectator() && !p.isCrouching());

            if (!thieves.isEmpty()) {
                Player thief = thieves.get(0);
                AshenStalkerEntity.this.setTarget(thief);
                AshenStalkerEntity.this.rememberedPlayerUUID = thief.getUUID(); // Remember the thief!
                AshenStalkerEntity.this.isGrudgeTriggered = true; // Instant Aggro
                AshenStalkerEntity.this.setHuntPhase(2); // Instantly enter furious sprint phase!
                AshenStalkerEntity.this.feedTimer = 0;
                AshenStalkerEntity.this.playSound(SoundEvents.ENDER_DRAGON_GROWL, 1.0F, 1.5F); // Raspy Hiss placeholder
                return true;
            }
            return false;
        }
    }

    class StalkerBlindScentTargetGoal extends Goal {
        private Player scentTarget;

        public StalkerBlindScentTargetGoal() { this.setFlags(EnumSet.of(Goal.Flag.TARGET)); }

        @Override
        public boolean canUse() {
            if (AshenStalkerEntity.this.getHuntPhase() == 3) return false; // Ignore scent while feeding

            LivingEntity currentTarget = AshenStalkerEntity.this.getTarget();
            if (currentTarget instanceof Player) return false; // Already hunting a player, don't interrupt

            // Scan for sprinting players up to 24 blocks away, IGNORING line of sight!
            List<Player> runners = AshenStalkerEntity.this.level().getEntitiesOfClass(Player.class, AshenStalkerEntity.this.getBoundingBox().inflate(24.0D),
                    p -> !p.isCreative() && !p.isSpectator() && p.isSprinting());

            if (!runners.isEmpty()) {
                scentTarget = runners.get(0);
                return true;
            }
            return false;
        }

        @Override
        public void start() {
            AshenStalkerEntity.this.setTarget(scentTarget);

            // If we hold a grudge, attack immediately!
            if (AshenStalkerEntity.this.rememberedPlayerUUID != null && scentTarget.getUUID().equals(AshenStalkerEntity.this.rememberedPlayerUUID)) {
                AshenStalkerEntity.this.isGrudgeTriggered = true;
            }

            // Standard start (StalkerHuntGoal will seamlessly transition this to sprint since they are sprinting!)
            AshenStalkerEntity.this.setHuntPhase(1);
            AshenStalkerEntity.this.playSound(SoundEvents.FOX_AGGRO, 1.0F, 0.5F); // Pitched down raspy hiss
        }
    }

    // --- REPLACEMENT TARGET GOALS (Bypassing NearestAttackableTargetGoal Constructors) ---
    class StalkerTerritoryTargetGoal extends net.minecraft.world.entity.ai.goal.target.TargetGoal {
        private Monster targetMonster;

        public StalkerTerritoryTargetGoal() {
            super(AshenStalkerEntity.this, true, false);
        }

        @Override
        public boolean canUse() {
            if (AshenStalkerEntity.this.territoryCenter == null) return false;
            if (AshenStalkerEntity.this.getHuntPhase() == 3) return false; // Ignore intruders while feeding
            if (AshenStalkerEntity.this.getRandom().nextInt(10) != 0) return false; // Optimization

            AABB searchBox = AshenStalkerEntity.this.getBoundingBox().inflate(24.0D);
            List<Monster> monsters = AshenStalkerEntity.this.level().getEntitiesOfClass(Monster.class, searchBox, m ->
                    m.distanceToSqr(AshenStalkerEntity.this.territoryCenter.getX(), AshenStalkerEntity.this.territoryCenter.getY(), AshenStalkerEntity.this.territoryCenter.getZ()) < 2500.0D
                            && AshenStalkerEntity.this.hasLineOfSight(m)
            );

            if (monsters.isEmpty()) return false;

            // Get closest
            monsters.sort(java.util.Comparator.comparingDouble(AshenStalkerEntity.this::distanceToSqr));
            this.targetMonster = monsters.get(0);
            return true;
        }

        @Override
        public void start() {
            AshenStalkerEntity.this.setTarget(this.targetMonster);
            super.start();
        }

        @Override
        public void stop() {
            this.targetMonster = null;
            super.stop();
        }
    }

    class StalkerClashTargetGoal extends net.minecraft.world.entity.ai.goal.target.TargetGoal {
        private AshenStalkerEntity rivalStalker;

        public StalkerClashTargetGoal() {
            super(AshenStalkerEntity.this, true, false);
        }

        @Override
        public boolean canUse() {
            if (AshenStalkerEntity.this.getHuntPhase() == 3) return false; // Don't fight while feeding
            if (AshenStalkerEntity.this.getRandom().nextInt(50) != 0) return false; // Optimization

            AABB searchBox = AshenStalkerEntity.this.getBoundingBox().inflate(24.0D);
            List<AshenStalkerEntity> stalkers = AshenStalkerEntity.this.level().getEntitiesOfClass(AshenStalkerEntity.class, searchBox, s ->
                    s != AshenStalkerEntity.this && s.isAlive() && AshenStalkerEntity.this.hasLineOfSight(s)
            );

            if (stalkers.isEmpty()) return false;

            // 25% chance to instigate a fight upon spotting another stalker
            if (AshenStalkerEntity.this.getRandom().nextInt(4) == 0) {
                stalkers.sort(java.util.Comparator.comparingDouble(AshenStalkerEntity.this::distanceToSqr));
                this.rivalStalker = stalkers.get(0);
                return true;
            }

            return false;
        }

        @Override
        public void start() {
            AshenStalkerEntity.this.setTarget(this.rivalStalker);
            AshenStalkerEntity.this.playSound(SoundEvents.FOX_AGGRO, 1.0F, 0.5F); // Angry growl
            super.start();
        }

        @Override
        public void stop() {
            this.rivalStalker = null;
            super.stop();
        }
    }

    class StalkerPreyTargetGoal extends net.minecraft.world.entity.ai.goal.target.TargetGoal {
        private LivingEntity targetPrey;

        public StalkerPreyTargetGoal() {
            super(AshenStalkerEntity.this, true, false);
        }

        @Override
        public boolean canUse() {
            if (AshenStalkerEntity.this.getHuntPhase() == 3) return false; // Don't acquire new prey while feeding
            if (AshenStalkerEntity.this.getRandom().nextInt(10) != 0) return false; // Optimization

            AABB searchBox = AshenStalkerEntity.this.getBoundingBox().inflate(24.0D); // Toned down to 24 blocks

            List<LivingEntity> prey = AshenStalkerEntity.this.level().getEntitiesOfClass(LivingEntity.class, searchBox, a -> {
                if (!AshenStalkerEntity.this.hasLineOfSight(a)) return false;

                double distSq = AshenStalkerEntity.this.distanceToSqr(a);

                if (a instanceof Player p) {
                    if (p.isCreative() || p.isSpectator()) return false;
                    boolean isKnown = AshenStalkerEntity.this.rememberedPlayerUUID != null && p.getUUID().equals(AshenStalkerEntity.this.rememberedPlayerUUID);

                    if (isKnown) {
                        Vec3 vecToStalker = AshenStalkerEntity.this.getEyePosition().subtract(p.getEyePosition()).normalize();
                        Vec3 playerLook = p.getLookAngle().normalize();
                        boolean playerLooking = vecToStalker.dot(playerLook) > 0.85D;

                        if (playerLooking || AshenStalkerEntity.this.isGrudgeTriggered) {
                            return distSq <= 576.0D; // 24 block grudge detection, ignores crouching completely!
                        }
                    }

                    if (p.isCrouching()) return false;
                    return distSq <= 576.0D; // 24 block standard detection
                } else if (a instanceof Pig || a instanceof Sheep || a instanceof Cow || a instanceof Horse || a instanceof VeilFoxEntity || a instanceof GrotEntity) {
                    return distSq <= 576.0D; // 24 block animal detection
                }

                return false;
            });

            if (prey.isEmpty()) return false;

            // Get closest
            prey.sort(java.util.Comparator.comparingDouble(AshenStalkerEntity.this::distanceToSqr));
            this.targetPrey = prey.get(0);
            return true;
        }

        @Override
        public void start() {
            AshenStalkerEntity.this.setTarget(this.targetPrey);
            super.start();
        }

        @Override
        public void stop() {
            this.targetPrey = null;
            super.stop();
        }
    }
}