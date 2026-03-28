package ddraig.net.entropica.entity.veil_fox;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.registry.ModEntityTypes;
import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class VeilFoxEntity extends TamableAnimal {

    private static final EntityDataAccessor<String> ESSENCE_TYPE = SynchedEntityData.defineId(VeilFoxEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> TAMING_PHASE = SynchedEntityData.defineId(VeilFoxEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DETECTING_TARGET_ID = SynchedEntityData.defineId(VeilFoxEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_SLEEPING = SynchedEntityData.defineId(VeilFoxEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Integer> FRIENDLINESS = SynchedEntityData.defineId(VeilFoxEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WILD_FRIENDLINESS = SynchedEntityData.defineId(VeilFoxEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> INTELLIGENCE = SynchedEntityData.defineId(VeilFoxEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TRICKSTER = SynchedEntityData.defineId(VeilFoxEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_AGGRESSIVE = SynchedEntityData.defineId(VeilFoxEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HEARD_MUSIC = SynchedEntityData.defineId(VeilFoxEntity.class, EntityDataSerializers.BOOLEAN);

    private int blinkCooldown = 0;
    private int chainBlinksRemaining = 0;
    private final int[] recentTeleportTicks = new int[]{-200, -200, -200, -200};
    private int teleportIndex = 0;
    private int teleportDelay = 0;
    private Vec3 pendingTeleportTarget = null;
    private boolean pendingTeleportSitting = false;
    private boolean pendingTeleportLeaveAfterimage = false;
    private int sleepTicksClient = 0;

    public LivingEntity tagTarget = null;
    public int tagTimer = 0;
    public int tagCooldown = 0;
    public boolean isSeeker = false;
    public boolean isHider = false;
    public int hideSeekTimer = 0;
    public VeilFoxEntity seekTarget = null;
    public boolean isPlayingTagWithOwner = false;

    public String forcedPackGame = "";
    public int forcedMelody = -1;

    public boolean isPlayingPackGame = false;

    public ItemStack voidPocket = ItemStack.EMPTY;
    public ItemStack fetchedItem = ItemStack.EMPTY;

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
        builder.define(IS_SLEEPING, false);
        builder.define(FRIENDLINESS, 30);
        builder.define(WILD_FRIENDLINESS, 0);
        builder.define(INTELLIGENCE, 5);
        builder.define(TRICKSTER, 0);
        builder.define(IS_AGGRESSIVE, false);
        builder.define(HEARD_MUSIC, false);
    }

    public EssenceType getEssenceType() { try { return EssenceType.valueOf(this.entityData.get(ESSENCE_TYPE)); } catch (IllegalArgumentException e) { return EssenceType.VOID; } }
    public void setEssenceType(EssenceType type) { this.entityData.set(ESSENCE_TYPE, type.name()); }
    public int getTamingPhase() { return this.entityData.get(TAMING_PHASE); }
    public void setTamingPhase(int phase) { this.entityData.set(TAMING_PHASE, phase); }
    public int getDetectingTargetId() { return this.entityData.get(DETECTING_TARGET_ID); }
    public void setDetectingTargetId(int id) { this.entityData.set(DETECTING_TARGET_ID, id); }
    public boolean isSleeping() { return this.entityData.get(IS_SLEEPING); }
    public void setSleeping(boolean sleeping) { this.entityData.set(IS_SLEEPING, sleeping); }
    public int getFriendliness() { return this.entityData.get(FRIENDLINESS); }
    public void setFriendliness(int f) { this.entityData.set(FRIENDLINESS, Mth.clamp(f, 0, 255)); }
    public int getWildFriendliness() { return this.entityData.get(WILD_FRIENDLINESS); }
    public void setWildFriendliness(int f) { this.entityData.set(WILD_FRIENDLINESS, Mth.clamp(f, 0, 255)); }
    public int getIntelligence() { return this.entityData.get(INTELLIGENCE); }
    public void setIntelligence(int i) { this.entityData.set(INTELLIGENCE, Mth.clamp(i, 1, 20)); }
    public int getTrickster() { return this.entityData.get(TRICKSTER); }
    public void setTrickster(int t) { this.entityData.set(TRICKSTER, Mth.clamp(t, 0, 31)); }
    public boolean isAggressive() { return this.entityData.get(IS_AGGRESSIVE); }
    public void setAggressive(boolean a) { this.entityData.set(IS_AGGRESSIVE, a); }
    public boolean hasHeardMusic() { return this.entityData.get(HEARD_MUSIC); }
    public void setHeardMusic(boolean m) { this.entityData.set(HEARD_MUSIC, m); }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("IsSleeping", Codec.BOOL, this.isSleeping());
        output.store("TamingPhase", Codec.INT, this.getTamingPhase());
        output.store("Friendliness", Codec.INT, this.getFriendliness());
        output.store("WildFriendliness", Codec.INT, this.getWildFriendliness());
        output.store("Intelligence", Codec.INT, this.getIntelligence());
        output.store("Trickster", Codec.INT, this.getTrickster());
        output.store("IsAggressive", Codec.BOOL, this.isAggressive());
        output.store("HeardMusic", Codec.BOOL, this.hasHeardMusic());
        if (!this.voidPocket.isEmpty()) {
            output.store("VoidPocket", ItemStack.OPTIONAL_CODEC, this.voidPocket);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setSleeping(input.read("IsSleeping", Codec.BOOL).orElse(false));
        this.setTamingPhase(input.read("TamingPhase", Codec.INT).orElse(0));
        this.setFriendliness(input.read("Friendliness", Codec.INT).orElse(30));
        this.setWildFriendliness(input.read("WildFriendliness", Codec.INT).orElse(0));
        this.setIntelligence(input.read("Intelligence", Codec.INT).orElse(5));
        this.setTrickster(input.read("Trickster", Codec.INT).orElse(0));
        this.setAggressive(input.read("IsAggressive", Codec.BOOL).orElse(false));
        this.setHeardMusic(input.read("HeardMusic", Codec.BOOL).orElse(false));
        this.voidPocket = input.read("VoidPocket", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    public boolean isFood(ItemStack stack) { return false; }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnType, @Nullable SpawnGroupData spawnGroupData) {
        this.setEssenceType(this.getRandom().nextBoolean() ? EssenceType.VOID : EssenceType.UMBRAL);
        this.setFriendliness(30);
        this.setWildFriendliness(this.getRandom().nextInt(121));
        this.setIntelligence(5 + this.getRandom().nextInt(8));
        this.setTrickster(this.getRandom().nextInt(32));
        this.setAggressive(this.getRandom().nextFloat() < 0.15f);
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));

        this.goalSelector.addGoal(2, new FoxMeleeAttackGoal(1.3D, false));
        this.goalSelector.addGoal(3, new AuraCleansingGoal());
        this.goalSelector.addGoal(4, new DefensiveDistractionGoal());
        this.goalSelector.addGoal(5, new StartledBlinkGoal());

        this.goalSelector.addGoal(6, new VoidFetchGoal());
        this.goalSelector.addGoal(7, new FoxSleepGoal());
        this.goalSelector.addGoal(8, new CombatFetchGoal());
        this.goalSelector.addGoal(9, new GuidingLightGoal());
        this.goalSelector.addGoal(10, new BringGiftGoal());
        this.goalSelector.addGoal(11, new EmpathicResonanceGoal());
        this.goalSelector.addGoal(12, new ProgressiveTameGoal());
        this.goalSelector.addGoal(13, new CuriousItemInspectorGoal());
        this.goalSelector.addGoal(14, new FollowOwnerBlinkingGoal(this, 6.0F, 2.0F));

        this.goalSelector.addGoal(15, new MirrorMatchGoal());
        this.goalSelector.addGoal(16, new EssenceHotPotatoGoal());
        this.goalSelector.addGoal(17, new PhantomHerdingGoal());
        this.goalSelector.addGoal(18, new BlinkTagGoal());
        this.goalSelector.addGoal(19, new HideAndSeekGoal());
        this.goalSelector.addGoal(20, new HideAndSeekHiderGoal());
        this.goalSelector.addGoal(21, new ArcaneKingOfTheHillGoal());
        this.goalSelector.addGoal(22, new VoidLeapfrogGoal());
        this.goalSelector.addGoal(23, new AmbushPracticeGoal());
        this.goalSelector.addGoal(24, new HarmonicHowlingGoal());
        this.goalSelector.addGoal(25, new EssenceSharingGoal());
        this.goalSelector.addGoal(26, new NightGatheringGoal());

        this.goalSelector.addGoal(27, new PerchOnElevationGoal());
        this.goalSelector.addGoal(28, new DetectHiddenTargetGoal());

        this.goalSelector.addGoal(29, new WaterAvoidingRandomStrollGoal(this, 1.0D) {
            @Override public boolean canUse() { return !VeilFoxEntity.this.isTame() && !VeilFoxEntity.this.isPlayingPackGame && super.canUse(); }
        });

        this.goalSelector.addGoal(30, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(31, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getTarget() != null) {
            this.isPlayingPackGame = false;
        }

        if (this.level().isClientSide()) {
            boolean isMoving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6;

            if (this.isSleeping()) {
                this.idleAnimationState.stop();
                this.blinkOutAnimationState.stop();
                this.blinkInAnimationState.stop();
                this.sleepOutAnimationState.stop();
                this.attackAnimationState.stop();
                this.rolloverAnimationState.stop();
                this.digAnimationState.stop();

                this.sleepTicksClient++;
                if (this.sleepTicksClient == 1) {
                    this.sleepInAnimationState.start(this.tickCount);
                } else if (this.sleepTicksClient >= 36) {
                    this.sleepInAnimationState.stop();
                    this.sleepAnimationState.startIfStopped(this.tickCount);
                }
            } else {
                if (this.sleepTicksClient > 0) {
                    this.sleepInAnimationState.stop();
                    this.sleepAnimationState.stop();
                    this.sleepOutAnimationState.start(this.tickCount);
                    this.sleepTicksClient = 0;
                }

                if (isMoving || this.blinkOutAnimationState.isStarted() || this.blinkInAnimationState.isStarted() ||
                        this.attackAnimationState.isStarted() || this.rolloverAnimationState.isStarted() || this.digAnimationState.isStarted()) {
                    this.idleAnimationState.stop();
                    this.sleepOutAnimationState.stop();
                } else {
                    if (!this.sleepOutAnimationState.isStarted()) {
                        this.idleAnimationState.startIfStopped(this.tickCount);
                    }
                }
            }
        } else {
            if (this.teleportDelay > 0) {
                this.teleportDelay--;
                if (this.teleportDelay == 0 && this.pendingTeleportTarget != null) {
                    this.executeTeleport();
                }
            }

            if (this.isPassenger() && this.getVehicle() instanceof Player p) {
                p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 40, 0, false, false, true));
            }

            if (this.tickCount % 40 == 0 && !this.hasHeardMusic()) {
                BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();
                for (int x = -10; x <= 10; x++) {
                    for (int y = -5; y <= 5; y++) {
                        for (int z = -10; z <= 10; z++) {
                            mPos.set(this.getX() + x, this.getY() + y, this.getZ() + z);
                            if (this.level().getBlockState(mPos).is(Blocks.JUKEBOX)) {
                                if (this.level().getBlockState(mPos).getValue(JukeboxBlock.HAS_RECORD)) {
                                    this.setHeardMusic(true);
                                }
                            }
                        }
                    }
                }
            }

            if (this.tickCount % 100 == 0) {
                List<VeilFoxEntity> friends = this.level().getEntitiesOfClass(VeilFoxEntity.class, this.getBoundingBox().inflate(16.0D), f -> f != this);
                if (!friends.isEmpty()) {
                    int boost = 1 + (this.getFriendliness() / 50);
                    this.setWildFriendliness(this.getWildFriendliness() + boost);
                }

                LivingEntity owner = this.getOwner();
                if (owner instanceof Player p && this.getDetectingTargetId() != -1) {
                    Entity detected = this.level().getEntity(this.getDetectingTargetId());
                    if (detected == null || !detected.isAlive()) {
                        if (p.getLastHurtMob() != null && !p.getLastHurtMob().isAlive()) {
                            this.setFriendliness(this.getFriendliness() + 10);
                        }
                        this.setDetectingTargetId(-1);
                    }
                }
            }

            if (this.tagCooldown > 0) this.tagCooldown--;
        }

        if (this.blinkCooldown > 0) this.blinkCooldown--;

        if (!this.level().isClientSide() && this.chainBlinksRemaining > 0 && this.blinkCooldown <= 0) {
            if (this.teleportRandomly(8, true, true)) {
                this.chainBlinksRemaining--;
                this.blinkCooldown = 10;
            } else {
                this.chainBlinksRemaining = 0;
            }
        }

        if (this.level().isClientSide() && this.tickCount % 2 == 0) {
            if (this.isPlayingTagWithOwner) {
                this.level().addParticle(ParticleTypes.ENCHANTED_HIT, this.getRandomX(0.5D), this.getY() + 1.2, this.getRandomZ(0.5D), 0, 0, 0);
            } else {
                this.level().addParticle(ParticleTypes.PORTAL, this.getRandomX(0.5D), this.getRandomY() + 0.2D, this.getRandomZ(0.5D), (this.random.nextDouble() - 0.5D) * 0.5D, -this.random.nextDouble() * 0.2D, (this.random.nextDouble() - 0.5D) * 0.5D);
            }
        }
    }

    public void scheduleTeleport(Vec3 target, boolean leaveAfterimage) {
        if (!this.level().isClientSide() && this.teleportDelay <= 0) {
            this.pendingTeleportTarget = target;
            this.pendingTeleportSitting = this.isOrderedToSit();
            this.pendingTeleportLeaveAfterimage = leaveAfterimage;
            this.teleportDelay = 2;
            this.level().broadcastEntityEvent(this, (byte) 100);
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

            if (this.pendingTeleportLeaveAfterimage) {
                VeilFoxAfterimageEntity ghost = ModEntityTypes.VEIL_FOX_AFTERIMAGE.get().create(this.level(), EntitySpawnReason.TRIGGERED);
                if (ghost != null) {
                    ghost.snapTo(oldX, oldY, oldZ, this.getYRot(), this.getXRot());
                    ghost.setPoses(oldYBodyRot, oldYHeadRot, oldXRot, this.pendingTeleportSitting);
                    this.level().addFreshEntity(ghost);
                }
            }
            this.level().broadcastEntityEvent(this, (byte) 101);
        } else {
            this.level().broadcastEntityEvent(this, (byte) 101);
        }
        this.pendingTeleportTarget = null;
    }

    public boolean teleportRandomly(int radius, boolean hideFromPlayer, boolean leaveAfterimage) {
        if (this.level().isClientSide() || this.teleportDelay > 0) return false;
        if (!hideFromPlayer && this.tickCount - this.recentTeleportTicks[this.teleportIndex] < 200) return false;

        Player nearestPlayer = hideFromPlayer ? this.level().getNearestPlayer(this, 24.0D) : null;

        for (int i = 0; i < 32; i++) {
            double targetX = this.getX() + (this.random.nextDouble() - 0.5D) * radius * 2;
            double targetY = Mth.clamp(this.getY() + (this.random.nextInt(radius) - radius / 2), this.level().getMinY(), this.level().getMaxY() - 1);
            double targetZ = this.getZ() + (this.random.nextDouble() - 0.5D) * radius * 2;
            Vec3 targetVec = new Vec3(targetX, targetY, targetZ);
            boolean outOfSight = true;

            if (nearestPlayer != null) {
                HitResult hit = this.level().clip(new ClipContext(nearestPlayer.getEyePosition(), targetVec.add(0, this.getEyeHeight(), 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                outOfSight = hit.getType() != HitResult.Type.MISS;
            }

            if (!hideFromPlayer || outOfSight || i > 24) {
                BlockPos targetPos = BlockPos.containing(targetX, targetY, targetZ);
                if (this.level().getBlockState(targetPos).isAir() || i > 28) {
                    if (!hideFromPlayer) {
                        this.recentTeleportTicks[this.teleportIndex] = this.tickCount;
                        this.teleportIndex = (this.teleportIndex + 1) % 4;
                    }
                    this.scheduleTeleport(targetVec, leaveAfterimage);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 100) {
            this.blinkOutAnimationState.start(this.tickCount);
            this.blinkInAnimationState.stop();
            this.idleAnimationState.stop();
            this.attackAnimationState.stop();
        } else if (id == 101) {
            this.blinkInAnimationState.start(this.tickCount);
            this.blinkOutAnimationState.stop();
            this.idleAnimationState.stop();
            this.attackAnimationState.stop();
        } else if (id == 102) {
            this.attackAnimationState.start(this.tickCount);
            this.idleAnimationState.stop();
        } else if (id == 103) {
            this.rolloverAnimationState.start(this.tickCount);
            this.idleAnimationState.stop();
        } else if (id == 104) {
            this.digAnimationState.start(this.tickCount);
            this.idleAnimationState.stop();
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        this.setSleeping(false);
        this.isPlayingPackGame = false;

        if (this.isTame() && this.getFriendliness() > 150 && !this.isAggressive() && source.getEntity() == this.getOwner()) {
            Player p = (Player)source.getEntity();
            if (p.getMainHandItem().isEmpty() && !this.isPlayingTagWithOwner) {
                this.isPlayingTagWithOwner = true;
                this.setTarget(p);
                this.playSound(SoundEvents.FOX_AGGRO, 1.0f, 1.5f);
                return false;
            }
        }

        if (!this.isTame() && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            if (this.isAggressive() && source.getEntity() instanceof LivingEntity attacker) {
                this.setTarget(attacker);
                this.teleportRandomly(5, false, true);
                this.playSound(SoundEvents.FOX_BITE, 1.0F, 1.0F);
            } else if (this.random.nextFloat() < 0.90f) {
                this.teleportRandomly(8, true, true);
                this.chainBlinksRemaining = this.random.nextInt(3);
                this.blinkCooldown = 15;

                if (this.getWildFriendliness() > 40) {
                    List<VeilFoxEntity> pack = this.level().getEntitiesOfClass(VeilFoxEntity.class, this.getBoundingBox().inflate(16.0D));
                    for(VeilFoxEntity friend : pack) {
                        if(friend != this && friend.blinkCooldown <= 0) {
                            friend.isPlayingPackGame = false;
                            friend.teleportRandomly(8, true, true);
                            friend.blinkCooldown = 15;
                        }
                    }
                }
                return false;
            }
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (this.level().isClientSide()) {
            boolean isTamingItem = itemstack.is(ModItems.VEIL_SHARD.get()) || itemstack.getItem() instanceof EssenceItem;
            boolean interacts = (this.isTame() && this.isOwnedBy(player)) || isTamingItem;
            return interacts ? InteractionResult.CONSUME : super.mobInteract(player, hand);
        }

        if (hand == InteractionHand.MAIN_HAND) {
            if (this.isTame()) {
                if (this.isOwnedBy(player)) {
                    if (player.isCrouching() && itemstack.isEmpty() && this.getFriendliness() >= 180) {
                        this.startRiding(player);
                        return InteractionResult.SUCCESS;
                    }

                    if (this.getFriendliness() == 255 && !player.isCrouching()) {
                        boolean isTamingItem = itemstack.is(ModItems.VEIL_SHARD.get()) || itemstack.getItem() instanceof EssenceItem;

                        if (itemstack.isEmpty() && !this.voidPocket.isEmpty()) {
                            if (this.level() instanceof ServerLevel sl) {
                                this.spawnAtLocation(sl, this.voidPocket);
                            }
                            this.voidPocket = ItemStack.EMPTY;
                            this.playSound(SoundEvents.SHULKER_BOX_OPEN, 1.0f, 1.5f);
                            return InteractionResult.SUCCESS;
                        } else if (!itemstack.isEmpty() && this.voidPocket.isEmpty() && !isTamingItem) {
                            this.voidPocket = itemstack.copy();
                            itemstack.shrink(itemstack.getCount());
                            this.playSound(SoundEvents.SHULKER_BOX_CLOSE, 1.0f, 1.5f);
                            return InteractionResult.SUCCESS;
                        }
                    }

                    this.setSleeping(false);
                    this.setOrderedToSit(!this.isOrderedToSit());
                    this.jumping = false;
                    this.navigation.stop();
                    return InteractionResult.SUCCESS;
                }
            } else {
                if (this.getTamingPhase() >= 5) {
                    boolean isTamingItem = itemstack.is(ModItems.VEIL_SHARD.get()) || itemstack.is(ModItems.WEAK_ESSENCE.get()) || itemstack.is(ModItems.AVERAGE_ESSENCE.get()) || itemstack.is(ModItems.STRONG_ESSENCE.get());
                    if (isTamingItem) {
                        if (this.getRandom().nextInt(32) < this.getTrickster()) {
                            if (!player.getAbilities().instabuild) {
                                ItemStack stolen = itemstack.copyWithCount(1);
                                itemstack.shrink(1);
                                if (!this.voidPocket.isEmpty() && this.level() instanceof ServerLevel sl) {
                                    this.spawnAtLocation(sl, this.voidPocket);
                                }
                                this.voidPocket = stolen;
                            }
                            this.setTrickster(Math.max(0, this.getTrickster() - 10));
                            this.setTamingPhase(0);

                            if (this.isAggressive()) {
                                this.setTarget(player);
                                this.teleportRandomly(4, false, true);
                                this.playSound(SoundEvents.FOX_BITE, 1.0F, 1.0F);
                            } else {
                                this.teleportRandomly(12, true, false);
                                this.playSound(SoundEvents.FOX_SCREECH, 1.0F, 1.5F);
                            }
                            return InteractionResult.SUCCESS;
                        }

                        if (!player.getAbilities().instabuild) itemstack.shrink(1);
                        this.setFriendliness(255);
                        this.tame(player);
                        this.navigation.stop();
                        this.setTarget(null);
                        this.setOrderedToSit(true);
                        this.level().broadcastEntityEvent(this, (byte) 7);
                        return InteractionResult.SUCCESS;
                    }
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

        if (!this.voidPocket.isEmpty()) {
            this.spawnAtLocation(level, this.voidPocket);
        }
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        VeilFoxEntity baby = ModEntityTypes.VEIL_FOX.get().create(level, EntitySpawnReason.BREEDING);
        if (baby != null && this.isTame()) {
            LivingEntity owner = this.getOwner();
            if (owner instanceof Player player) baby.tame(player);
        }
        return baby;
    }

    // ==========================================
    // AI GOALS
    // ==========================================

    class FoxMeleeAttackGoal extends MeleeAttackGoal {
        public FoxMeleeAttackGoal(double speed, boolean useLongMemory) {
            super(VeilFoxEntity.this, speed, useLongMemory);
        }
        @Override
        protected void checkAndPerformAttack(LivingEntity target) {
            if (VeilFoxEntity.this.isWithinMeleeAttackRange(target) && this.isTimeToAttack()) {
                this.resetAttackCooldown();
                VeilFoxEntity.this.level().broadcastEntityEvent(VeilFoxEntity.this, (byte) 102);

                if (VeilFoxEntity.this.isPlayingTagWithOwner && target == VeilFoxEntity.this.getOwner()) {
                    VeilFoxEntity.this.isPlayingTagWithOwner = false;
                    VeilFoxEntity.this.setTarget(null);
                    VeilFoxEntity.this.playSound(SoundEvents.FOX_AGGRO, 1.0f, 2.0f);
                    if (VeilFoxEntity.this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.HEART, target.getX(), target.getY() + 1, target.getZ(), 3, 0.2, 0.2, 0.2, 0);
                    }
                    return;
                }

                if (VeilFoxEntity.this.level() instanceof ServerLevel sl) {
                    VeilFoxEntity.this.doHurtTarget(sl, target);
                }
            }
        }
    }

    class VoidFetchGoal extends Goal {
        private ItemEntity targetToy;
        private int timeout = 0;
        public VoidFetchGoal() { this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)); }

        @Override
        public boolean canUse() {
            boolean forced = VeilFoxEntity.this.forcedPackGame.equals("void_fetch");
            if (!forced) {
                if (!VeilFoxEntity.this.isTame() || VeilFoxEntity.this.getFriendliness() <= 100 || VeilFoxEntity.this.getIntelligence() <= 5 || VeilFoxEntity.this.isOrderedToSit() || VeilFoxEntity.this.teleportDelay > 0) return false;
            }

            if (!VeilFoxEntity.this.fetchedItem.isEmpty()) return true;

            List<ItemEntity> items = VeilFoxEntity.this.level().getEntitiesOfClass(ItemEntity.class, VeilFoxEntity.this.getBoundingBox().inflate(16.0D));
            for (ItemEntity item : items) {
                Item i = item.getItem().getItem();
                if (forced || ((i == Items.SLIME_BALL || i == Items.STICK || i == Items.BONE) && Math.abs(item.getDeltaMovement().y) > 0.1)) {
                    targetToy = item;
                    return true;
                }
            }
            return false;
        }

        @Override
        public void start() {
            VeilFoxEntity.this.forcedPackGame = "";
            timeout = 0;
            if (targetToy != null && VeilFoxEntity.this.fetchedItem.isEmpty()) {
                Vec3 intercept = targetToy.position().add(targetToy.getDeltaMovement().scale(2.0));
                VeilFoxEntity.this.scheduleTeleport(intercept, false);
                VeilFoxEntity.this.setNoGravity(true);
            }
        }

        @Override
        public boolean canContinueToUse() {
            return (!VeilFoxEntity.this.fetchedItem.isEmpty()) || (targetToy != null && targetToy.isAlive() && timeout < 100);
        }

        @Override
        public void tick() {
            timeout++;

            if (!VeilFoxEntity.this.fetchedItem.isEmpty()) {
                VeilFoxEntity.this.setNoGravity(false);
                LivingEntity owner = VeilFoxEntity.this.getOwner();
                if (owner != null) {
                    VeilFoxEntity.this.getLookControl().setLookAt(owner, 30.0F, 30.0F);
                    if (VeilFoxEntity.this.distanceToSqr(owner) > 6.0D) {
                        if (timeout % 10 == 0 && VeilFoxEntity.this.teleportDelay <= 0) {
                            VeilFoxEntity.this.scheduleTeleport(owner.position().add((VeilFoxEntity.this.random.nextDouble() - 0.5), 0, (VeilFoxEntity.this.random.nextDouble() - 0.5)), false);
                        }
                    } else {
                        if (VeilFoxEntity.this.level() instanceof ServerLevel sl) {
                            VeilFoxEntity.this.spawnAtLocation(sl, VeilFoxEntity.this.fetchedItem);
                        }
                        VeilFoxEntity.this.fetchedItem = ItemStack.EMPTY;
                        VeilFoxEntity.this.playSound(SoundEvents.FOX_AGGRO, 1.0F, 1.5F);
                    }
                }
            } else if (targetToy != null && targetToy.isAlive()) {
                VeilFoxEntity.this.getLookControl().setLookAt(targetToy, 30.0F, 30.0F);
                if (VeilFoxEntity.this.distanceToSqr(targetToy) < 9.0D || VeilFoxEntity.this.getBoundingBox().inflate(1.0D).intersects(targetToy.getBoundingBox())) {
                    VeilFoxEntity.this.fetchedItem = targetToy.getItem().copy();
                    targetToy.discard();
                    targetToy = null;
                    VeilFoxEntity.this.setNoGravity(false);
                } else if (timeout % 10 == 0 && VeilFoxEntity.this.teleportDelay <= 0) {
                    Vec3 intercept = targetToy.position().add(targetToy.getDeltaMovement().scale(3.0));
                    VeilFoxEntity.this.scheduleTeleport(intercept, false);
                }
            }
        }

        @Override
        public void stop() {
            targetToy = null;
            VeilFoxEntity.this.setNoGravity(false);
        }
    }

    class MirrorMatchGoal extends Goal {
        private VeilFoxEntity partner;
        private int matchTimer = 0;
        private int animId = 0;

        public MirrorMatchGoal() { this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)); }

        @Override
        public boolean canUse() {
            boolean forced = VeilFoxEntity.this.forcedPackGame.equals("mirror_match");
            if (!forced) {
                if (VeilFoxEntity.this.isTame() || VeilFoxEntity.this.getWildFriendliness() <= 180 || VeilFoxEntity.this.getIntelligence() <= 16 || VeilFoxEntity.this.isPlayingPackGame) return false;
                if (VeilFoxEntity.this.getRandom().nextInt(400) != 0) return false;
            }

            List<VeilFoxEntity> friends = VeilFoxEntity.this.level().getEntitiesOfClass(VeilFoxEntity.class, VeilFoxEntity.this.getBoundingBox().inflate(10.0D), f -> f != VeilFoxEntity.this && !f.isTame() && !f.isPlayingPackGame);
            if (!friends.isEmpty()) {
                partner = friends.get(0);
                return true;
            }
            return false;
        }

        @Override
        public void start() {
            VeilFoxEntity.this.forcedPackGame = "";
            matchTimer = 60;
            animId = VeilFoxEntity.this.getRandom().nextBoolean() ? 103 : 104;
            VeilFoxEntity.this.level().broadcastEntityEvent(VeilFoxEntity.this, (byte) animId);
            VeilFoxEntity.this.isPlayingPackGame = true;
            partner.isPlayingPackGame = true;
        }

        @Override
        public boolean canContinueToUse() { return matchTimer > 0 && partner != null && partner.isAlive(); }

        @Override
        public void tick() {
            matchTimer--;
            partner.getLookControl().setLookAt(VeilFoxEntity.this, 30.0F, 30.0F);
            if (matchTimer == 20) {
                partner.level().broadcastEntityEvent(partner, (byte) animId);
            }
            if (matchTimer == 1) {
                if (VeilFoxEntity.this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.HEART, VeilFoxEntity.this.getX(), VeilFoxEntity.this.getY()+1, VeilFoxEntity.this.getZ(), 3, 0.2, 0.2, 0.2, 0);
                    sl.sendParticles(ParticleTypes.HEART, partner.getX(), partner.getY()+1, partner.getZ(), 3, 0.2, 0.2, 0.2, 0);
                }
                VeilFoxEntity.this.setIntelligence(VeilFoxEntity.this.getIntelligence() + 1);
                partner.setIntelligence(partner.getIntelligence() + 1);
            }
        }
        @Override public void stop() {
            VeilFoxEntity.this.isPlayingPackGame = false;
            if (partner != null) partner.isPlayingPackGame = false;
            partner = null;
        }
    }

    class EssenceHotPotatoGoal extends Goal {
        private VeilFoxEntity partner;
        private ItemEntity targetItem;
        private List<VeilFoxEntity> participants;

        public EssenceHotPotatoGoal() { this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)); }

        @Override
        public boolean canUse() {
            boolean forced = VeilFoxEntity.this.forcedPackGame.equals("hot_potato");
            if (!forced) {
                if (VeilFoxEntity.this.isTame() || VeilFoxEntity.this.getWildFriendliness() <= 160 || VeilFoxEntity.this.getIntelligence() <= 14 || VeilFoxEntity.this.isPlayingPackGame) return false;
            }

            List<ItemEntity> items = VeilFoxEntity.this.level().getEntitiesOfClass(ItemEntity.class, VeilFoxEntity.this.getBoundingBox().inflate(12.0D));
            for (ItemEntity item : items) {
                if (item.getItem().is(ModItems.VEIL_SHARD.get()) || item.getItem().getItem() instanceof EssenceItem) {
                    List<VeilFoxEntity> friends = VeilFoxEntity.this.level().getEntitiesOfClass(VeilFoxEntity.class, VeilFoxEntity.this.getBoundingBox().inflate(12.0D), f -> f != VeilFoxEntity.this && !f.isPlayingPackGame);
                    if (!friends.isEmpty()) {
                        targetItem = item;
                        partner = friends.get(0);
                        participants = friends;
                        return true;
                    }
                }
            }

            if (forced) {
                List<VeilFoxEntity> friends = VeilFoxEntity.this.level().getEntitiesOfClass(VeilFoxEntity.class, VeilFoxEntity.this.getBoundingBox().inflate(12.0D), f -> f != VeilFoxEntity.this && !f.isPlayingPackGame);
                if (!friends.isEmpty()) {
                    targetItem = null;
                    VeilFoxEntity.this.fetchedItem = new ItemStack(ModItems.VEIL_SHARD.get());
                    partner = friends.get(0);
                    participants = friends;
                    return true;
                }
            }
            return false;
        }

        @Override
        public void start() {
            VeilFoxEntity.this.forcedPackGame = "";
            VeilFoxEntity.this.isPlayingPackGame = true;
            for(VeilFoxEntity f : participants) f.isPlayingPackGame = true;
        }

        @Override
        public void tick() {
            if (targetItem != null && targetItem.isAlive() && VeilFoxEntity.this.fetchedItem.isEmpty()) {
                VeilFoxEntity.this.scheduleTeleport(targetItem.position(), false);
                if (VeilFoxEntity.this.distanceToSqr(targetItem) < 4.0D) {
                    VeilFoxEntity.this.fetchedItem = targetItem.getItem().copy();
                    targetItem.discard();
                }
            } else if (!VeilFoxEntity.this.fetchedItem.isEmpty() && partner != null && partner.isAlive()) {
                VeilFoxEntity.this.getLookControl().setLookAt(partner, 30.0F, 30.0F);
                ItemEntity dropped = new ItemEntity(VeilFoxEntity.this.level(), VeilFoxEntity.this.getX(), VeilFoxEntity.this.getY()+0.5, VeilFoxEntity.this.getZ(), VeilFoxEntity.this.fetchedItem);
                Vec3 throwDir = partner.position().subtract(VeilFoxEntity.this.position()).normalize().scale(0.5);
                dropped.setDeltaMovement(throwDir.x, 0.3, throwDir.z);
                VeilFoxEntity.this.level().addFreshEntity(dropped);
                VeilFoxEntity.this.fetchedItem = ItemStack.EMPTY;
                VeilFoxEntity.this.playSound(SoundEvents.FOX_AGGRO, 1.0F, 1.5F);
            }
        }

        @Override
        public void stop() {
            VeilFoxEntity.this.isPlayingPackGame = false;
            if (participants != null) {
                for(VeilFoxEntity f : participants) f.isPlayingPackGame = false;
            }
        }
    }

    class ArcaneKingOfTheHillGoal extends Goal {
        private BlockPos hillPos;
        public ArcaneKingOfTheHillGoal() { this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)); }

        @Override
        public boolean canUse() {
            boolean forced = VeilFoxEntity.this.forcedPackGame.equals("king_of_the_hill");
            if (!forced) {
                if (VeilFoxEntity.this.isTame() || VeilFoxEntity.this.getWildFriendliness() <= 100 || VeilFoxEntity.this.getIntelligence() <= 8 || VeilFoxEntity.this.teleportDelay > 0 || VeilFoxEntity.this.isPlayingPackGame) return false;
                if (VeilFoxEntity.this.getRandom().nextInt(200) != 0) return false;
            }

            List<VeilFoxEntity> friends = VeilFoxEntity.this.level().getEntitiesOfClass(VeilFoxEntity.class, VeilFoxEntity.this.getBoundingBox().inflate(16.0D), f -> f != VeilFoxEntity.this);
            if (friends.size() < 2 && !forced) return false;

            for (int i = 0; i < 10; i++) {
                BlockPos p = VeilFoxEntity.this.blockPosition().offset(VeilFoxEntity.this.getRandom().nextInt(16)-8, VeilFoxEntity.this.getRandom().nextInt(3)+1, VeilFoxEntity.this.getRandom().nextInt(16)-8);
                if (VeilFoxEntity.this.level().getBlockState(p).isAir() && VeilFoxEntity.this.level().getBlockState(p.below()).isSolidRender()) {
                    hillPos = p;
                    return true;
                }
            }
            return false;
        }
        @Override
        public void start() {
            VeilFoxEntity.this.forcedPackGame = "";
            if (hillPos != null) VeilFoxEntity.this.scheduleTeleport(new Vec3(hillPos.getX()+0.5, hillPos.getY(), hillPos.getZ()+0.5), false);
        }
    }

    class VoidLeapfrogGoal extends Goal {
        private VeilFoxEntity leader;
        public VoidLeapfrogGoal() { this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)); }

        @Override
        public boolean canUse() {
            boolean forced = VeilFoxEntity.this.forcedPackGame.equals("leapfrog");
            if (!forced) {
                if (VeilFoxEntity.this.isTame() || VeilFoxEntity.this.getWildFriendliness() <= 120 || VeilFoxEntity.this.getIntelligence() <= 10 || VeilFoxEntity.this.teleportDelay > 0 || VeilFoxEntity.this.isPlayingPackGame) return false;
            }

            List<VeilFoxEntity> friends = VeilFoxEntity.this.level().getEntitiesOfClass(VeilFoxEntity.class, VeilFoxEntity.this.getBoundingBox().inflate(10.0D), f -> f != VeilFoxEntity.this && !f.isPlayingPackGame && (forced || f.getDeltaMovement().horizontalDistanceSqr() > 0.001));
            if (!friends.isEmpty()) {
                leader = friends.get(0);
                return true;
            }
            return false;
        }
        @Override
        public void start() {
            VeilFoxEntity.this.forcedPackGame = "";
            if (leader != null) {
                Vec3 jumpPos = leader.position().add(leader.getDeltaMovement().normalize().scale(3.0));
                VeilFoxEntity.this.scheduleTeleport(jumpPos, false);
                VeilFoxEntity.this.playSound(SoundEvents.FOX_AGGRO, 0.5f, 1.8f);
            }
        }
    }

    class AmbushPracticeGoal extends Goal {
        private VeilFoxEntity victim;
        public AmbushPracticeGoal() { this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)); }

        @Override
        public boolean canUse() {
            boolean forced = VeilFoxEntity.this.forcedPackGame.equals("ambush");
            if (!forced) {
                if (VeilFoxEntity.this.isTame() || VeilFoxEntity.this.getWildFriendliness() <= 60 || VeilFoxEntity.this.getTrickster() <= 15 || VeilFoxEntity.this.teleportDelay > 0 || VeilFoxEntity.this.isPlayingPackGame) return false;
                if (VeilFoxEntity.this.getRandom().nextInt(200) != 0) return false;
            }

            List<VeilFoxEntity> friends = VeilFoxEntity.this.level().getEntitiesOfClass(VeilFoxEntity.class, VeilFoxEntity.this.getBoundingBox().inflate(16.0D), f -> f != VeilFoxEntity.this && !f.isPlayingPackGame && (forced || f.isSleeping() || f.isOrderedToSit()));
            if (!friends.isEmpty()) {
                victim = friends.get(0);
                return true;
            }
            return false;
        }
        @Override
        public void start() {
            VeilFoxEntity.this.forcedPackGame = "";
            VeilFoxEntity.this.level().broadcastEntityEvent(VeilFoxEntity.this, (byte) 104);
            Vec3 ambushPos = victim.position().subtract(victim.getLookAngle().normalize().scale(1.5));
            VeilFoxEntity.this.scheduleTeleport(ambushPos, false);
        }
        @Override
        public void tick() {
            if (VeilFoxEntity.this.distanceToSqr(victim) < 4.0D && VeilFoxEntity.this.teleportDelay <= 0) {
                VeilFoxEntity.this.playSound(SoundEvents.FOX_SCREECH, 1.0F, 1.0F);
                victim.setSleeping(false);
                victim.teleportRandomly(6, true, false);
                victim = null;
            }
        }
    }

    class HarmonicHowlingGoal extends Goal {
        private int howlTimer = 0;
        private int currentMelodyId = -1;
        private int noteIndex = 0;
        private int noteDelay = 10;
        private int songTick = 0;
        private List<VeilFoxEntity> participants;

        private float p(int semitonesFromE4) {
            return (float) Math.pow(2.0D, (semitonesFromE4 - 2) / 12.0D);
        }

        public HarmonicHowlingGoal() { this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)); }

        @Override
        public boolean canUse() {
            boolean forced = VeilFoxEntity.this.forcedPackGame.equals("howling");
            if (!forced) {
                if (VeilFoxEntity.this.isTame() || VeilFoxEntity.this.getWildFriendliness() <= 200 || VeilFoxEntity.this.getIntelligence() <= 15 || !VeilFoxEntity.this.hasHeardMusic() || VeilFoxEntity.this.isPlayingPackGame) return false;
                boolean isNight = !VeilFoxEntity.this.level().dimensionType().hasFixedTime() && (VeilFoxEntity.this.level().getDayTime() % 24000L >= 13000L);
                if (!isNight || VeilFoxEntity.this.getRandom().nextInt(400) != 0) return false;
            }

            participants = VeilFoxEntity.this.level().getEntitiesOfClass(VeilFoxEntity.class, VeilFoxEntity.this.getBoundingBox().inflate(16.0D), f -> f != VeilFoxEntity.this && !f.isPlayingPackGame);
            if (participants.size() >= 1 || forced) {
                return true;
            }
            return false;
        }

        @Override
        public void start() {
            VeilFoxEntity.this.getNavigation().stop();
            VeilFoxEntity.this.isPlayingPackGame = true;
            if (participants != null) {
                for(VeilFoxEntity f : participants) {
                    f.isPlayingPackGame = true;
                    f.getNavigation().stop();
                }
            }

            howlTimer = 240;
            noteIndex = 0;
            songTick = 0;

            boolean forced = VeilFoxEntity.this.forcedPackGame.equals("howling");

            if (forced) {
                if (VeilFoxEntity.this.forcedMelody >= 0 && VeilFoxEntity.this.forcedMelody <= 8) {
                    currentMelodyId = VeilFoxEntity.this.forcedMelody;
                    noteDelay = 10;
                } else if (VeilFoxEntity.this.forcedMelody == -2) {
                    currentMelodyId = -1;
                    noteDelay = 30;
                } else {
                    currentMelodyId = VeilFoxEntity.this.getRandom().nextInt(9);
                    noteDelay = 10;
                }
                VeilFoxEntity.this.forcedPackGame = "";
                VeilFoxEntity.this.forcedMelody = -1;
            } else {
                if (VeilFoxEntity.this.getRandom().nextFloat() < 0.2f) {
                    currentMelodyId = VeilFoxEntity.this.getRandom().nextInt(9);
                    noteDelay = 10;
                } else {
                    currentMelodyId = -1;
                    noteDelay = 30;
                }
            }
        }

        @Override
        public boolean canContinueToUse() { return howlTimer > 0; }

        @Override
        public void tick() {
            howlTimer--;

            if (songTick % noteDelay == 0) {
                float pitch = 1.0f;
                boolean play = false;
                boolean noteDone = false;

                if (currentMelodyId == -1) {
                    pitch = 0.5f + (VeilFoxEntity.this.getRandom().nextFloat() * 1.5f);
                    play = true;
                    if (songTick > 100) howlTimer = 0;
                } else {
                    switch (currentMelodyId) {
                        case 0:
                            switch (noteIndex) {
                                case 0: pitch = p(2); play = true; break;
                                case 1: play = false; break;
                                case 2: pitch = p(5); play = true; break;
                                case 3: play = false; break;
                                case 4: pitch = p(-2); play = true; break;
                                case 5: play = false; break;
                                case 6: play = false; break;
                                case 7: pitch = p(2); play = true; break;
                                case 8: play = false; break;
                                case 9: pitch = p(5); play = true; break;
                                case 10: play = false; break;
                                case 11: pitch = p(-2); play = true; break;
                                default: noteDone = true; break;
                            }
                            break;
                        case 1:
                            switch (noteIndex) {
                                case 0: pitch = p(0); play = true; break;
                                case 1: play = false; break;
                                case 2: pitch = p(3); play = true; break;
                                case 3: play = false; break;
                                case 4: pitch = p(5); play = true; break;
                                case 5: play = false; break;
                                case 6: pitch = p(3); play = true; break;
                                case 7: play = false; break;
                                case 8: pitch = p(0); play = true; break;
                                default: noteDone = true; break;
                            }
                            break;
                        case 2:
                            switch (noteIndex) {
                                case 0: pitch = p(0); play = true; break;
                                case 1: pitch = p(0); play = true; break;
                                case 2: play = false; break;
                                case 3: pitch = p(0); play = true; break;
                                case 4: play = false; break;
                                case 5: pitch = p(-4); play = true; break;
                                case 6: pitch = p(0); play = true; break;
                                case 7: play = false; break;
                                case 8: pitch = p(3); play = true; break;
                                default: noteDone = true; break;
                            }
                            break;
                        case 3:
                            switch (noteIndex) {
                                case 0: pitch = p(3); play = true; break;
                                case 1: play = false; break;
                                case 2: pitch = p(0); play = true; break;
                                case 3: play = false; break;
                                case 4: pitch = p(-4); play = true; break;
                                case 5: play = false; break;
                                case 6: pitch = p(-2); play = true; break;
                                case 7: play = false; break;
                                case 8: pitch = p(0); play = true; break;
                                default: noteDone = true; break;
                            }
                            break;
                        case 4:
                            switch (noteIndex) {
                                case 0: pitch = p(-4); play = true; break;
                                case 1: play = false; break;
                                case 2: pitch = p(0); play = true; break;
                                case 3: play = false; break;
                                case 4: pitch = p(3); play = true; break;
                                case 5: play = false; break;
                                case 6: pitch = p(5); play = true; break;
                                case 7: play = false; break;
                                case 8: pitch = p(3); play = true; break;
                                default: noteDone = true; break;
                            }
                            break;
                        case 5:
                            switch (noteIndex) {
                                case 0: pitch = p(0); play = true; break;
                                case 1: pitch = p(0); play = true; break;
                                case 2: play = false; break;
                                case 3: pitch = p(-2); play = true; break;
                                case 4: pitch = p(0); play = true; break;
                                case 5: play = false; break;
                                case 6: pitch = p(3); play = true; break;
                                case 7: pitch = p(0); play = true; break;
                                default: noteDone = true; break;
                            }
                            break;
                        case 6:
                            switch (noteIndex) {
                                case 0: pitch = p(0); play = true; break;
                                case 1: pitch = p(0); play = true; break;
                                case 2: pitch = p(12); play = true; break;
                                case 3: play = false; break;
                                case 4: pitch = p(7); play = true; break;
                                case 5: play = false; break;
                                case 6: pitch = p(6); play = true; break;
                                case 7: pitch = p(5); play = true; break;
                                case 8: pitch = p(3); play = true; break;
                                case 9: pitch = p(0); play = true; break;
                                case 10: pitch = p(3); play = true; break;
                                case 11: pitch = p(5); play = true; break;
                                default: noteDone = true; break;
                            }
                            break;
                        case 7:
                            switch (noteIndex) {
                                case 0: pitch = p(-5); play = true; break;
                                case 1: pitch = p(7); play = true; break;
                                case 2: pitch = p(2); play = true; break;
                                case 3: pitch = p(-1); play = true; break;
                                case 4: play = false; break;
                                case 5: pitch = p(7); play = true; break;
                                case 6: pitch = p(2); play = true; break;
                                case 7: pitch = p(-1); play = true; break;
                                default: noteDone = true; break;
                            }
                            break;
                        case 8:
                            switch (noteIndex) {
                                case 0: pitch = p(0); play = true; break;
                                case 1: pitch = p(0); play = true; break;
                                case 2: pitch = p(0); play = true; break;
                                case 3: pitch = p(0); play = true; break;
                                case 4: pitch = p(-4); play = true; break;
                                case 5: pitch = p(-2); play = true; break;
                                case 6: pitch = p(0); play = true; break;
                                case 7: play = false; break;
                                case 8: pitch = p(-2); play = true; break;
                                case 9: pitch = p(0); play = true; break;
                                default: noteDone = true; break;
                            }
                            break;
                        default:
                            noteDone = true; break;
                    }

                    noteIndex++;
                    if (noteDone) {
                        play = false;
                        howlTimer = 0; // End the song!
                    }
                }

                if (play) {
                    pitch = Mth.clamp(pitch, 0.5f, 2.0f);

                    VeilFoxEntity.this.playSound(ModSounds.VEIL_FOX_CHORUS.get(), 1.0f, pitch);
                    if (VeilFoxEntity.this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.NOTE, VeilFoxEntity.this.getX(), VeilFoxEntity.this.getY()+1, VeilFoxEntity.this.getZ(), 1, 0.2, 0.2, 0.2, 0);
                    }

                    // Orchestrate the pack so they all sing the note perfectly in sync!
                    if (participants != null) {
                        for (VeilFoxEntity f : participants) {
                            if (f.isAlive()) {
                                // Add a very tiny pitch variation for a natural chorus effect
                                float chorusPitch = Mth.clamp(pitch + (f.getRandom().nextFloat() * 0.04f - 0.02f), 0.5f, 2.0f);
                                f.playSound(ModSounds.VEIL_FOX_CHORUS.get(), 0.8f, chorusPitch);
                                if (f.level() instanceof ServerLevel sl) {
                                    sl.sendParticles(ParticleTypes.NOTE, f.getX(), f.getY()+1, f.getZ(), 1, 0.2, 0.2, 0.2, 0);
                                }
                            }
                        }
                    }
                }
            }
            songTick++;
        }

        @Override
        public void stop() {
            VeilFoxEntity.this.isPlayingPackGame = false;
            if (participants != null) {
                for(VeilFoxEntity f : participants) { f.isPlayingPackGame = false; }
            }
        }
    }

    class FoxSleepGoal extends Goal {
        private VeilFoxEntity leader;
        public FoxSleepGoal() { this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP)); }

        @Override
        public boolean canUse() {
            if (VeilFoxEntity.this.getTarget() != null || VeilFoxEntity.this.isInWater() || VeilFoxEntity.this.teleportDelay > 0 || VeilFoxEntity.this.isPlayingPackGame) return false;
            if (VeilFoxEntity.this.isTame()) {
                LivingEntity owner = VeilFoxEntity.this.getOwner();
                return owner != null && owner.isSleeping();
            } else {
                boolean isDay = !VeilFoxEntity.this.level().dimensionType().hasFixedTime() && (VeilFoxEntity.this.level().getDayTime() % 24000L < 12000L);
                int light = VeilFoxEntity.this.level().getMaxLocalRawBrightness(VeilFoxEntity.this.blockPosition());
                if (isDay && light < 10 && VeilFoxEntity.this.getRandom().nextInt(100) == 0) {
                    List<VeilFoxEntity> pack = VeilFoxEntity.this.level().getEntitiesOfClass(VeilFoxEntity.class, VeilFoxEntity.this.getBoundingBox().inflate(16.0D), f -> f.getWildFriendliness() >= VeilFoxEntity.this.getWildFriendliness() && f.isSleeping());
                    if (!pack.isEmpty()) leader = pack.get(0);
                    return true;
                }
                return false;
            }
        }

        @Override
        public boolean canContinueToUse() {
            if (VeilFoxEntity.this.getTarget() != null || VeilFoxEntity.this.isInWater() || VeilFoxEntity.this.isPlayingPackGame) return false;
            if (VeilFoxEntity.this.isTame()) {
                LivingEntity owner = VeilFoxEntity.this.getOwner();
                return owner != null && owner.isSleeping();
            } else {
                return !VeilFoxEntity.this.level().dimensionType().hasFixedTime() && (VeilFoxEntity.this.level().getDayTime() % 24000L < 12000L);
            }
        }

        @Override
        public void start() {
            VeilFoxEntity.this.setSleeping(true);
            VeilFoxEntity.this.getNavigation().stop();
            if (VeilFoxEntity.this.isTame() && VeilFoxEntity.this.getOwner() != null) {
                Vec3 bedPos = VeilFoxEntity.this.getOwner().position().add((VeilFoxEntity.this.getRandom().nextDouble() - 0.5), 0, (VeilFoxEntity.this.getRandom().nextDouble() - 0.5));
                VeilFoxEntity.this.scheduleTeleport(bedPos, false);
            } else if (leader != null) {
                Vec3 cuddlePos = leader.position().add((VeilFoxEntity.this.getRandom().nextDouble() - 0.5) * 0.5, 0, (VeilFoxEntity.this.getRandom().nextDouble() - 0.5) * 0.5);
                VeilFoxEntity.this.scheduleTeleport(cuddlePos, false);
            }
        }
        @Override public void stop() { VeilFoxEntity.this.setSleeping(false); leader = null; }
    }

    class DefensiveDistractionGoal extends Goal {
        private LivingEntity threat;
        private int distractTimer = 0;
        public DefensiveDistractionGoal() { this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)); }

        @Override
        public boolean canUse() {
            if (VeilFoxEntity.this.isOrderedToSit() || VeilFoxEntity.this.isSleeping() || VeilFoxEntity.this.isPlayingPackGame) return false;
            LivingEntity owner = VeilFoxEntity.this.getOwner();
            if (owner != null && owner.getLastHurtByMob() != null && owner.getLastHurtByMob().isAlive() && owner.tickCount - owner.getLastHurtByMobTimestamp() < 100) {
                threat = owner.getLastHurtByMob();
                return threat.distanceToSqr(owner) < 256.0D;
            }
            if (VeilFoxEntity.this.getWildFriendliness() > 0) {
                List<VeilFoxEntity> pack = VeilFoxEntity.this.level().getEntitiesOfClass(VeilFoxEntity.class, VeilFoxEntity.this.getBoundingBox().inflate(16.0D));
                for (VeilFoxEntity f : pack) {
                    if (f != VeilFoxEntity.this && f.getLastHurtByMob() != null && f.getLastHurtByMob().isAlive() && f.tickCount - f.getLastHurtByMobTimestamp() < 60) {
                        threat = f.getLastHurtByMob();
                        return true;
                    }
                }
            }
            return false;
        }

        @Override public void start() { distractTimer = 40; }
        @Override public boolean canContinueToUse() { return distractTimer > 0 && threat != null && threat.isAlive(); }

        @Override
        public void tick() {
            distractTimer--;
            if (distractTimer % 10 == 0 && VeilFoxEntity.this.teleportDelay <= 0) {
                Vec3 blinkPos = threat.position().add((VeilFoxEntity.this.getRandom().nextDouble() - 0.5) * 4, VeilFoxEntity.this.getRandom().nextDouble() * 2, (VeilFoxEntity.this.getRandom().nextDouble() - 0.5) * 4);
                VeilFoxEntity.this.scheduleTeleport(blinkPos, true);
            }
            if (distractTimer == 10) {
                threat.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                threat.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0));
                VeilFoxEntity.this.playSound(ModSounds.VEIL_FOX_HURT.get(), 1.0F, 0.5F);
            }
        }
        @Override
        public void stop() {
            LivingEntity owner = VeilFoxEntity.this.getOwner();
            if (owner != null && VeilFoxEntity.this.teleportDelay <= 0) {
                Vec3 targetPos = owner.position().add((VeilFoxEntity.this.getRandom().nextDouble() - 0.5) * 2, 0, (VeilFoxEntity.this.getRandom().nextDouble() - 0.5) * 2);
                VeilFoxEntity.this.scheduleTeleport(targetPos, true);
            }
            threat = null;
        }
    }

    class BringGiftGoal extends Goal {
        private int cooldown = 6000;
        private boolean isAway = false;
        private int returnTimer = 0;
        public BringGiftGoal() { this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)); }

        @Override
        public boolean canUse() {
            if (!VeilFoxEntity.this.isTame() || VeilFoxEntity.this.isOrderedToSit() || VeilFoxEntity.this.isSleeping() || VeilFoxEntity.this.teleportDelay > 0 || VeilFoxEntity.this.isPlayingPackGame) return false;
            if (cooldown > 0) { cooldown--; return false; }
            LivingEntity owner = VeilFoxEntity.this.getOwner();
            return owner != null && VeilFoxEntity.this.getRandom().nextInt(300) == 0;
        }

        @Override
        public void start() {
            isAway = true; returnTimer = 100;
            Vec3 fetchPos = VeilFoxEntity.this.position().add(0, 200, 0);
            VeilFoxEntity.this.scheduleTeleport(fetchPos, false);
            VeilFoxEntity.this.setNoGravity(true);
        }

        @Override public boolean canContinueToUse() { return isAway; }

        @Override
        public void tick() {
            returnTimer--;
            if (returnTimer <= 0) {
                LivingEntity owner = VeilFoxEntity.this.getOwner();
                if (owner != null) {
                    Vec3 targetPos = owner.position().add(owner.getLookAngle().normalize().scale(2.0));
                    VeilFoxEntity.this.setNoGravity(false);
                    VeilFoxEntity.this.scheduleTeleport(targetPos, false);

                    Item[] gifts = {Items.RABBIT_HIDE, Items.FEATHER, ModItems.WEAK_ESSENCE.get(), Items.EMERALD, Items.GLOW_BERRIES};
                    ItemStack gift = new ItemStack(gifts[VeilFoxEntity.this.getRandom().nextInt(gifts.length)]);
                    if (gift.getItem() instanceof EssenceItem) EssenceItem.setEssenceType(gift, VeilFoxEntity.this.getEssenceType());

                    if (VeilFoxEntity.this.level() instanceof ServerLevel sl) {
                        VeilFoxEntity.this.spawnAtLocation(sl, gift);
                    }
                    VeilFoxEntity.this.playSound(SoundEvents.FOX_AGGRO, 0.5F, 1.5F);
                }
                isAway = false;
                cooldown = 6000 + VeilFoxEntity.this.getRandom().nextInt(6000);
            }
        }
    }

    class CuriousItemInspectorGoal extends Goal {
        private ItemEntity targetItem;
        public CuriousItemInspectorGoal() { this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)); }

        @Override
        public boolean canUse() {
            if (VeilFoxEntity.this.isTame() || VeilFoxEntity.this.isSleeping() || VeilFoxEntity.this.teleportDelay > 0 || VeilFoxEntity.this.isPlayingPackGame) return false;
            if (VeilFoxEntity.this.getRandom().nextInt(40) != 0) return false;

            List<ItemEntity> items = VeilFoxEntity.this.level().getEntitiesOfClass(ItemEntity.class, VeilFoxEntity.this.getBoundingBox().inflate(12.0D));
            for (ItemEntity item : items) {
                if (item.getItem().is(ModItems.VEIL_SHARD.get()) || item.getItem().getItem() instanceof EssenceItem || item.getItem().is(ModItems.SPELL_GEM.get())) {
                    targetItem = item;
                    return true;
                }
            }
            return false;
        }

        @Override
        public void tick() {
            if (targetItem != null && targetItem.isAlive()) {
                VeilFoxEntity.this.getLookControl().setLookAt(targetItem, 30.0F, 30.0F);
                if (VeilFoxEntity.this.distanceToSqr(targetItem) > 4.0D) {
                    if (VeilFoxEntity.this.teleportDelay <= 0 && VeilFoxEntity.this.getRandom().nextInt(20) == 0) {
                        Vec3 targetPos = targetItem.position().add((VeilFoxEntity.this.random.nextDouble() - 0.5) * 3, 0, (VeilFoxEntity.this.random.nextDouble() - 0.5) * 3);
                        VeilFoxEntity.this.scheduleTeleport(targetPos, false);
                    } else {
                        VeilFoxEntity.this.getNavigation().moveTo(targetItem, 0.6D);
                    }
                } else {
                    VeilFoxEntity.this.getNavigation().stop();
                    if (VeilFoxEntity.this.getRandom().nextInt(40) == 0) {
                        VeilFoxEntity.this.playSound(SoundEvents.FOX_SNIFF, 1.0F, 1.0F);
                        VeilFoxEntity.this.setFriendliness(VeilFoxEntity.this.getFriendliness() + 5);
                    }
                }
            }
        }
    }

    class GuidingLightGoal extends Goal {
        private BlockPos guideTarget;
        public GuidingLightGoal() { this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)); }

        @Override
        public boolean canUse() {
            if (!VeilFoxEntity.this.isTame() || VeilFoxEntity.this.getFriendliness() <= 150 || VeilFoxEntity.this.isPlayingPackGame) return false;
            LivingEntity owner = VeilFoxEntity.this.getOwner();
            if (owner == null) return false;

            boolean lowHealth = owner.getHealth() < owner.getMaxHealth() * 0.3f;
            boolean inDark = VeilFoxEntity.this.level().getMaxLocalRawBrightness(owner.blockPosition()) == 0;

            if (lowHealth || inDark) {
                for(int i=0; i<20; i++) {
                    BlockPos p = owner.blockPosition().offset(VeilFoxEntity.this.getRandom().nextInt(20)-10, VeilFoxEntity.this.getRandom().nextInt(5), VeilFoxEntity.this.getRandom().nextInt(20)-10);
                    if (VeilFoxEntity.this.level().getBlockState(p).isAir() && VeilFoxEntity.this.level().getBlockState(p.below()).isSolidRender()) {
                        if (VeilFoxEntity.this.level().getMaxLocalRawBrightness(p) > 0 || p.getY() > owner.getY() + 2) {
                            guideTarget = p;
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public void start() {
            if (guideTarget != null) VeilFoxEntity.this.scheduleTeleport(new Vec3(guideTarget.getX()+0.5, guideTarget.getY(), guideTarget.getZ()+0.5), false);
        }

        @Override
        public void tick() {
            LivingEntity owner = VeilFoxEntity.this.getOwner();
            if (owner != null) VeilFoxEntity.this.getLookControl().setLookAt(owner, 30.0F, 30.0F);
        }
    }

    class EmpathicResonanceGoal extends Goal {
        private int resonanceTimer = 0;
        private int cooldown = 0;

        public EmpathicResonanceGoal() { this.setFlags(EnumSet.of(Goal.Flag.LOOK)); }

        @Override
        public boolean canUse() {
            if (!VeilFoxEntity.this.isTame() || VeilFoxEntity.this.getFriendliness() <= 200 || !VeilFoxEntity.this.isOrderedToSit() || VeilFoxEntity.this.isPlayingPackGame) return false;
            if (cooldown > 0) { cooldown--; return false; }
            LivingEntity owner = VeilFoxEntity.this.getOwner();
            return owner != null && owner.isCrouching() && VeilFoxEntity.this.distanceToSqr(owner) < 4.0D;
        }

        @Override
        public void tick() {
            resonanceTimer++;
            LivingEntity owner = VeilFoxEntity.this.getOwner();
            if (owner != null) {
                VeilFoxEntity.this.getLookControl().setLookAt(owner, 30.0F, 30.0F);
                if (resonanceTimer % 10 == 0 && VeilFoxEntity.this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.ENCHANT, owner.getX(), owner.getY() + 1, owner.getZ(), 2, 0.2, 0.2, 0.2, 0);
                }
                if (resonanceTimer >= 200) {
                    owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 2400, 0));
                    VeilFoxEntity.this.playSound(SoundEvents.AMETHYST_BLOCK_RESONATE, 1.0F, 1.0F);
                    cooldown = 6000;
                    resonanceTimer = 0;
                }
            }
        }
        @Override public void stop() { resonanceTimer = 0; }
    }

    class CombatFetchGoal extends Goal {
        private ItemEntity targetItem;
        public CombatFetchGoal() { this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)); }

        @Override
        public boolean canUse() {
            if (!VeilFoxEntity.this.isTame() || VeilFoxEntity.this.getFriendliness() <= 100 || VeilFoxEntity.this.isOrderedToSit() || VeilFoxEntity.this.isPlayingPackGame) return false;

            if (!VeilFoxEntity.this.fetchedItem.isEmpty()) return true;

            LivingEntity owner = VeilFoxEntity.this.getOwner();
            if (owner == null || (owner.getLastHurtByMob() == null && owner.getLastHurtMob() == null)) return false;

            List<ItemEntity> items = VeilFoxEntity.this.level().getEntitiesOfClass(ItemEntity.class, VeilFoxEntity.this.getBoundingBox().inflate(16.0D));
            if (!items.isEmpty()) {
                targetItem = items.get(0);
                return true;
            }
            return false;
        }

        @Override
        public void tick() {
            if (!VeilFoxEntity.this.fetchedItem.isEmpty()) {
                LivingEntity owner = VeilFoxEntity.this.getOwner();
                if (owner != null) {
                    if (VeilFoxEntity.this.distanceToSqr(owner) > 4.0D) {
                        if (VeilFoxEntity.this.teleportDelay <= 0 && VeilFoxEntity.this.getRandom().nextInt(10) == 0) {
                            VeilFoxEntity.this.scheduleTeleport(owner.position().add((VeilFoxEntity.this.random.nextDouble() - 0.5), 0, (VeilFoxEntity.this.random.nextDouble() - 0.5)), false);
                        }
                    } else {
                        if (VeilFoxEntity.this.level() instanceof ServerLevel sl) {
                            VeilFoxEntity.this.spawnAtLocation(sl, VeilFoxEntity.this.fetchedItem);
                        }
                        VeilFoxEntity.this.fetchedItem = ItemStack.EMPTY;
                    }
                }
            } else if (targetItem != null && targetItem.isAlive()) {
                if (VeilFoxEntity.this.distanceToSqr(targetItem) > 4.0D) {
                    if (VeilFoxEntity.this.teleportDelay <= 0 && VeilFoxEntity.this.getRandom().nextInt(10) == 0) {
                        VeilFoxEntity.this.scheduleTeleport(targetItem.position(), false);
                    }
                } else {
                    VeilFoxEntity.this.fetchedItem = targetItem.getItem().copy();
                    targetItem.discard();
                    targetItem = null;
                }
            }
        }
    }

    class EssenceSharingGoal extends Goal {
        private VeilFoxEntity targetFox;
        private ItemEntity targetItem;
        public EssenceSharingGoal() { this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)); }

        @Override
        public boolean canUse() {
            if (VeilFoxEntity.this.getWildFriendliness() <= 60 || VeilFoxEntity.this.isTame() || VeilFoxEntity.this.isPlayingPackGame) return false;

            if (!VeilFoxEntity.this.fetchedItem.isEmpty() && targetFox != null && targetFox.isAlive()) return true;

            List<VeilFoxEntity> friends = VeilFoxEntity.this.level().getEntitiesOfClass(VeilFoxEntity.class, VeilFoxEntity.this.getBoundingBox().inflate(16.0D), f -> f != VeilFoxEntity.this && f.getHealth() < f.getMaxHealth() * 0.5f);
            if (friends.isEmpty()) return false;

            List<ItemEntity> items = VeilFoxEntity.this.level().getEntitiesOfClass(ItemEntity.class, VeilFoxEntity.this.getBoundingBox().inflate(16.0D), i -> i.getItem().getItem() instanceof EssenceItem);
            if (items.isEmpty()) return false;

            targetFox = friends.get(0);
            targetItem = items.get(0);
            return true;
        }

        @Override
        public void tick() {
            if (!VeilFoxEntity.this.fetchedItem.isEmpty() && targetFox != null && targetFox.isAlive()) {
                if (VeilFoxEntity.this.distanceToSqr(targetFox) > 4.0D) {
                    if (VeilFoxEntity.this.teleportDelay <= 0) VeilFoxEntity.this.scheduleTeleport(targetFox.position(), false);
                } else {
                    if (VeilFoxEntity.this.level() instanceof ServerLevel sl) {
                        VeilFoxEntity.this.spawnAtLocation(sl, VeilFoxEntity.this.fetchedItem);
                    }
                    VeilFoxEntity.this.fetchedItem = ItemStack.EMPTY;
                }
            } else if (targetItem != null && targetItem.isAlive()) {
                if (VeilFoxEntity.this.distanceToSqr(targetItem) > 4.0D) {
                    if (VeilFoxEntity.this.teleportDelay <= 0) VeilFoxEntity.this.scheduleTeleport(targetItem.position(), false);
                } else {
                    VeilFoxEntity.this.fetchedItem = targetItem.getItem().copy();
                    targetItem.discard();
                    targetItem = null;
                }
            }
        }
    }

    class AuraCleansingGoal extends Goal {
        private VeilFoxEntity targetFox;
        public AuraCleansingGoal() { this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)); }

        @Override
        public boolean canUse() {
            if (VeilFoxEntity.this.getWildFriendliness() <= 160 || VeilFoxEntity.this.isTame() || VeilFoxEntity.this.teleportDelay > 0 || VeilFoxEntity.this.isPlayingPackGame) return false;

            List<VeilFoxEntity> friends = VeilFoxEntity.this.level().getEntitiesOfClass(VeilFoxEntity.class, VeilFoxEntity.this.getBoundingBox().inflate(16.0D));
            for (VeilFoxEntity f : friends) {
                if (f != VeilFoxEntity.this && f.getActiveEffects().stream().anyMatch(inst -> !inst.getEffect().value().isBeneficial())) {
                    targetFox = f;
                    return true;
                }
            }
            return false;
        }

        @Override
        public void start() {
            if (targetFox != null) VeilFoxEntity.this.scheduleTeleport(targetFox.position(), false);
        }

        @Override
        public void tick() {
            if (targetFox != null && VeilFoxEntity.this.distanceToSqr(targetFox) < 2.0D) {
                targetFox.removeAllEffects();
                if (VeilFoxEntity.this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.ENCHANT, targetFox.getX(), targetFox.getY() + 1, targetFox.getZ(), 10, 0.2, 0.2, 0.2, 0.1);
                }
                targetFox = null;
            }
        }
    }

    class BlinkTagGoal extends Goal {
        private List<VeilFoxEntity> participants;

        @Override
        public boolean canUse() {
            boolean forced = VeilFoxEntity.this.forcedPackGame.equals("blink_tag");
            if (!forced) {
                if (VeilFoxEntity.this.isTame() || VeilFoxEntity.this.getWildFriendliness() <= 80 || VeilFoxEntity.this.tagCooldown > 0 || VeilFoxEntity.this.isPlayingPackGame) return false;
                if (VeilFoxEntity.this.tagTarget != null && VeilFoxEntity.this.tagTarget.isAlive()) return true;
                if (VeilFoxEntity.this.getRandom().nextInt(200) != 0) return false;
            } else {
                if (VeilFoxEntity.this.tagTarget != null && VeilFoxEntity.this.tagTarget.isAlive()) return true;
            }

            participants = VeilFoxEntity.this.level().getEntitiesOfClass(VeilFoxEntity.class, VeilFoxEntity.this.getBoundingBox().inflate(16.0D),
                    e -> e != VeilFoxEntity.this && e.isAlive() && !e.isTame() && e.tagCooldown <= 0 && !e.isPlayingPackGame);

            if (!participants.isEmpty()) {
                VeilFoxEntity.this.tagTarget = participants.get(VeilFoxEntity.this.getRandom().nextInt(participants.size()));
                VeilFoxEntity.this.tagTimer = 200;
                return true;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() { return VeilFoxEntity.this.tagTarget != null && VeilFoxEntity.this.tagTarget.isAlive() && VeilFoxEntity.this.tagTimer > 0; }

        @Override
        public void start() {
            VeilFoxEntity.this.forcedPackGame = "";
            VeilFoxEntity.this.isPlayingPackGame = true;
            if (participants != null) {
                for(VeilFoxEntity f : participants) f.isPlayingPackGame = true;
            }
        }

        @Override
        public void tick() {
            VeilFoxEntity.this.tagTimer--;
            if (VeilFoxEntity.this.teleportDelay <= 0 && VeilFoxEntity.this.getRandom().nextInt(10) == 0) {
                VeilFoxEntity.this.scheduleTeleport(VeilFoxEntity.this.tagTarget.position(), false);
            }

            if (VeilFoxEntity.this.distanceToSqr(VeilFoxEntity.this.tagTarget) < 4.0D) {
                VeilFoxEntity.this.playSound(SoundEvents.FOX_AGGRO, 1.0F, 2.0F);
                if (VeilFoxEntity.this.tagTarget instanceof VeilFoxEntity other) {
                    other.tagTarget = VeilFoxEntity.this;
                    other.tagTimer = 200;
                    other.tagCooldown = 40;
                }
                VeilFoxEntity.this.tagTarget = null;
                VeilFoxEntity.this.tagCooldown = 40;
            }
        }
        @Override
        public void stop() {
            VeilFoxEntity.this.tagTarget = null;
            VeilFoxEntity.this.isPlayingPackGame = false;
            if (participants != null) {
                for(VeilFoxEntity f : participants) f.isPlayingPackGame = false;
            }
        }
    }

    class HideAndSeekGoal extends Goal {
        private List<VeilFoxEntity> hiders;

        @Override
        public boolean canUse() {
            boolean forced = VeilFoxEntity.this.forcedPackGame.equals("hide_and_seek");
            if (!forced) {
                if (VeilFoxEntity.this.isTame() || VeilFoxEntity.this.getWildFriendliness() <= 150) return false;
                if (VeilFoxEntity.this.isSeeker) return true;
                if (VeilFoxEntity.this.getRandom().nextInt(400) != 0) return false;
            } else {
                if (VeilFoxEntity.this.isSeeker) return true;
            }

            hiders = VeilFoxEntity.this.level().getEntitiesOfClass(VeilFoxEntity.class, VeilFoxEntity.this.getBoundingBox().inflate(16.0D),
                    e -> e != VeilFoxEntity.this && !e.isSeeker && !e.isHider && !e.isTame() && !e.isPlayingPackGame);

            if (hiders.size() >= 1) {
                VeilFoxEntity.this.isSeeker = true;
                VeilFoxEntity.this.hideSeekTimer = 100;
                return true;
            }
            return false;
        }

        @Override
        public void start() {
            VeilFoxEntity.this.forcedPackGame = "";
            VeilFoxEntity.this.setSleeping(true);
            VeilFoxEntity.this.isPlayingPackGame = true;
            for (VeilFoxEntity friend : hiders) {
                friend.isHider = true;
                friend.hideSeekTimer = 400;
                friend.seekTarget = VeilFoxEntity.this;
                friend.isPlayingPackGame = true;
            }
        }

        @Override
        public void tick() {
            VeilFoxEntity.this.hideSeekTimer--;
            if (VeilFoxEntity.this.hideSeekTimer <= 0) {
                VeilFoxEntity.this.setSleeping(false);
                List<VeilFoxEntity> activeHiders = VeilFoxEntity.this.level().getEntitiesOfClass(VeilFoxEntity.class, VeilFoxEntity.this.getBoundingBox().inflate(24.0D), e -> e.isHider);

                for (VeilFoxEntity hider : activeHiders) {
                    if (VeilFoxEntity.this.hasLineOfSight(hider)) {
                        VeilFoxEntity.this.scheduleTeleport(hider.position(), false);
                        hider.isHider = false;
                        VeilFoxEntity.this.playSound(SoundEvents.FOX_AGGRO, 1.0F, 2.0F);
                        VeilFoxEntity.this.setIntelligence(VeilFoxEntity.this.getIntelligence() + 1);
                    }
                }
            }
        }
        @Override
        public void stop() {
            VeilFoxEntity.this.isSeeker = false;
            VeilFoxEntity.this.setSleeping(false);
            VeilFoxEntity.this.isPlayingPackGame = false;
            if (hiders != null) {
                for (VeilFoxEntity f : hiders) f.isPlayingPackGame = false;
            }
        }
    }

    class HideAndSeekHiderGoal extends Goal {
        @Override public boolean canUse() { return VeilFoxEntity.this.isHider && VeilFoxEntity.this.getTarget() == null && VeilFoxEntity.this.hideSeekTimer > 0; }

        @Override
        public void tick() {
            VeilFoxEntity.this.hideSeekTimer--;
            if (VeilFoxEntity.this.seekTarget != null && VeilFoxEntity.this.seekTarget.isSleeping() && VeilFoxEntity.this.teleportDelay <= 0) {
                VeilFoxEntity.this.teleportRandomly(12, true, false);
            } else {
                VeilFoxEntity.this.setOrderedToSit(true);
            }
        }
        @Override
        public void stop() {
            VeilFoxEntity.this.isHider = false;
            VeilFoxEntity.this.seekTarget = null;
            VeilFoxEntity.this.setOrderedToSit(false);
            VeilFoxEntity.this.isPlayingPackGame = false;
        }
    }

    class PhantomHerdingGoal extends Goal {
        private Animal prey;
        public PhantomHerdingGoal() { this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)); }

        @Override
        public boolean canUse() {
            if (VeilFoxEntity.this.isTame() || VeilFoxEntity.this.getWildFriendliness() <= 90 || VeilFoxEntity.this.isPlayingPackGame) return false;
            if (VeilFoxEntity.this.getRandom().nextInt(200) != 0) return false;

            List<Animal> animals = VeilFoxEntity.this.level().getEntitiesOfClass(Animal.class, VeilFoxEntity.this.getBoundingBox().inflate(16.0D), a -> !(a instanceof VeilFoxEntity));
            if (!animals.isEmpty()) {
                prey = animals.get(0);
                return true;
            }
            return false;
        }

        @Override
        public void tick() {
            if (prey != null && prey.isAlive() && VeilFoxEntity.this.teleportDelay <= 0) {
                if (VeilFoxEntity.this.getRandom().nextInt(15) == 0) {
                    Vec3 surroundPos = prey.position().add((VeilFoxEntity.this.getRandom().nextDouble() - 0.5) * 6, 0, (VeilFoxEntity.this.getRandom().nextDouble() - 0.5) * 6);
                    VeilFoxEntity.this.scheduleTeleport(surroundPos, false);
                }
            }
        }
    }

    class StartledBlinkGoal extends Goal {
        public StartledBlinkGoal() { this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)); }

        @Override
        public boolean canUse() {
            if (VeilFoxEntity.this.isTame() || VeilFoxEntity.this.blinkCooldown > 0 || VeilFoxEntity.this.teleportDelay > 0 || VeilFoxEntity.this.isPlayingPackGame) return false;
            if (VeilFoxEntity.this.getTarget() != null && VeilFoxEntity.this.isAggressive()) return false;

            List<Player> nearbyPlayers = VeilFoxEntity.this.level().getEntitiesOfClass(Player.class, VeilFoxEntity.this.getBoundingBox().inflate(8.0D));
            for (Player p : nearbyPlayers) {
                if (p.isCreative() || p.isSpectator()) continue;

                double speedSqr = p.getDeltaMovement().horizontalDistanceSqr();

                if ((speedSqr > 0.002D && !p.isCrouching()) || p.swinging) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void start() {
            VeilFoxEntity.this.setSleeping(false);

            if (VeilFoxEntity.this.isAggressive()) {
                List<Player> nearbyPlayers = VeilFoxEntity.this.level().getEntitiesOfClass(Player.class, VeilFoxEntity.this.getBoundingBox().inflate(8.0D));
                if (!nearbyPlayers.isEmpty()) {
                    VeilFoxEntity.this.setTarget(nearbyPlayers.get(0));
                    VeilFoxEntity.this.teleportRandomly(5, false, true);
                    VeilFoxEntity.this.blinkCooldown = 15;
                    VeilFoxEntity.this.setTamingPhase(0);
                    return;
                }
            }

            VeilFoxEntity.this.teleportRandomly(12, true, true);
            VeilFoxEntity.this.chainBlinksRemaining = 1 + VeilFoxEntity.this.random.nextInt(2);
            VeilFoxEntity.this.blinkCooldown = 15;
            VeilFoxEntity.this.setTamingPhase(0);
        }
    }

    class ProgressiveTameGoal extends Goal {
        private Player tamingTarget;
        private int patienceTimer = 0;
        private final int[] phaseDistances = {15, 10, 8, 5, 2};
        private final int[] phaseTimes = {600, 600, 600, 600, 600};

        public ProgressiveTameGoal() { this.setFlags(EnumSet.of(Goal.Flag.LOOK)); }

        @Override
        public boolean canUse() {
            if (VeilFoxEntity.this.isTame() || VeilFoxEntity.this.isSleeping() || VeilFoxEntity.this.isPlayingPackGame) return false;
            List<Player> players = VeilFoxEntity.this.level().getEntitiesOfClass(Player.class, VeilFoxEntity.this.getBoundingBox().inflate(16.0D));
            if (players.isEmpty()) return false;

            Player closest = null; double minDist = Double.MAX_VALUE;
            for(Player p : players) {
                if(VeilFoxEntity.this.distanceToSqr(p) < minDist) { minDist = VeilFoxEntity.this.distanceToSqr(p); closest = p; }
            }
            tamingTarget = closest;
            return tamingTarget != null && tamingTarget.isCrouching();
        }

        @Override
        public void tick() {
            if (tamingTarget == null || !tamingTarget.isAlive()) { resetTaming(); return; }

            double speedSqr = tamingTarget.getDeltaMovement().horizontalDistanceSqr();
            if (!tamingTarget.isCrouching() || speedSqr > 0.002D) {
                if (VeilFoxEntity.this.getTamingPhase() > 0) VeilFoxEntity.this.teleportRandomly(12, true, true);
                resetTaming();
                return;
            }

            int phase = VeilFoxEntity.this.getTamingPhase();
            if (phase >= 5) {
                VeilFoxEntity.this.getNavigation().moveTo(tamingTarget, 0.5D);
                VeilFoxEntity.this.getLookControl().setLookAt(tamingTarget, 10.0F, VeilFoxEntity.this.getMaxHeadXRot());
                return;
            }

            if (VeilFoxEntity.this.distanceTo(tamingTarget) <= phaseDistances[phase] && speedSqr < 0.0001D) {
                patienceTimer++;
                VeilFoxEntity.this.getLookControl().setLookAt(tamingTarget, 10.0F, VeilFoxEntity.this.getMaxHeadXRot());
                if (patienceTimer >= phaseTimes[phase]) {
                    VeilFoxEntity.this.setTamingPhase(phase + 1);
                    VeilFoxEntity.this.setFriendliness(VeilFoxEntity.this.getFriendliness() + 10);
                    patienceTimer = 0;
                    VeilFoxEntity.this.playSound(SoundEvents.FOX_AGGRO, 0.5F, 1.5F);
                    VeilFoxEntity.this.level().broadcastEntityEvent(VeilFoxEntity.this, (byte) 7);
                }
            }
        }

        private void resetTaming() { VeilFoxEntity.this.setTamingPhase(0); patienceTimer = 0; }
    }

    class FollowOwnerBlinkingGoal extends Goal {
        private final TamableAnimal tamable;
        private LivingEntity owner;
        private final float maxDist;
        private final float minDist;

        public FollowOwnerBlinkingGoal(TamableAnimal tamable, float maxDist, float minDist) {
            this.tamable = tamable; this.maxDist = maxDist; this.minDist = minDist;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity livingentity = this.tamable.getOwner();
            if (livingentity == null || livingentity.isSpectator() || this.tamable.isOrderedToSit() || ((VeilFoxEntity)this.tamable).isSleeping() || ((VeilFoxEntity)this.tamable).isPlayingTagWithOwner || this.tamable.distanceTo(livingentity) < this.minDist) return false;
            this.owner = livingentity; return true;
        }

        @Override
        public void tick() {
            this.tamable.getLookControl().setLookAt(this.owner, 10.0F, (float)this.tamable.getMaxHeadXRot());
            if (this.tamable.distanceTo(this.owner) >= this.maxDist && ((VeilFoxEntity)this.tamable).teleportDelay <= 0) {
                for(int i = 0; i < 10; i++) {
                    BlockPos targetPos = this.owner.blockPosition().offset(this.tamable.getRandom().nextInt(5) - 2, this.tamable.getRandom().nextInt(3) - 1, this.tamable.getRandom().nextInt(5) - 2);
                    if (this.tamable.level().getBlockState(targetPos).isAir() && this.tamable.level().getBlockState(targetPos.above()).isAir() && this.tamable.level().getBlockState(targetPos.below()).isSolidRender()) {
                        ((VeilFoxEntity)this.tamable).scheduleTeleport(new Vec3(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5), false);
                        break;
                    }
                }
            }
        }
    }

    class PerchOnElevationGoal extends Goal {
        public PerchOnElevationGoal() { this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)); }

        @Override
        public boolean canUse() {
            if (!VeilFoxEntity.this.isTame() || VeilFoxEntity.this.isOrderedToSit() || VeilFoxEntity.this.isSleeping() || VeilFoxEntity.this.teleportDelay > 0 || VeilFoxEntity.this.isPlayingPackGame) return false;
            if (VeilFoxEntity.this.getRandom().nextInt(80) != 0) return false;
            LivingEntity owner = VeilFoxEntity.this.getOwner();
            return owner != null && VeilFoxEntity.this.distanceToSqr(owner) <= 144;
        }

        @Override
        public void start() {
            for (int i = 0; i < 15; i++) {
                BlockPos pos = BlockPos.containing(VeilFoxEntity.this.getX() + (VeilFoxEntity.this.random.nextDouble() - 0.5D) * 16, VeilFoxEntity.this.getY() + 2 + VeilFoxEntity.this.random.nextInt(4), VeilFoxEntity.this.getZ() + (VeilFoxEntity.this.random.nextDouble() - 0.5D) * 16);
                if (VeilFoxEntity.this.level().getBlockState(pos.below()).isSolidRender() && VeilFoxEntity.this.level().getBlockState(pos).isAir() && VeilFoxEntity.this.level().getBlockState(pos.above()).isAir()) {
                    VeilFoxEntity.this.scheduleTeleport(new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5), false);
                    break;
                }
            }
        }
    }

    class DetectHiddenTargetGoal extends Goal {
        private LivingEntity detectedTarget;
        public DetectHiddenTargetGoal() { this.setFlags(EnumSet.of(Goal.Flag.LOOK)); }

        @Override
        public boolean canUse() {
            if (!VeilFoxEntity.this.isTame() || VeilFoxEntity.this.isSleeping() || VeilFoxEntity.this.tickCount % 10 != 0 || VeilFoxEntity.this.isPlayingPackGame) return false;
            if (!(VeilFoxEntity.this.getOwner() instanceof Player owner)) return false;

            for (Monster m : VeilFoxEntity.this.level().getEntitiesOfClass(Monster.class, VeilFoxEntity.this.getBoundingBox().inflate(16.0D))) {
                if (!(owner.hasLineOfSight(m) && !m.isInvisible())) {
                    detectedTarget = m;
                    VeilFoxEntity.this.setDetectingTargetId(m.getId());
                    return true;
                }
            }
            VeilFoxEntity.this.setDetectingTargetId(-1);
            return false;
        }

        @Override
        public boolean canContinueToUse() { return detectedTarget != null && detectedTarget.isAlive() && VeilFoxEntity.this.distanceToSqr(detectedTarget) < 400.0D; }
        @Override
        public void tick() { VeilFoxEntity.this.getLookControl().setLookAt(detectedTarget, 30.0F, 30.0F); }
        @Override
        public void stop() { detectedTarget = null; VeilFoxEntity.this.setDetectingTargetId(-1); }
    }

    class NightGatheringGoal extends Goal {
        private BlockPos gatheringCenter;
        public NightGatheringGoal() { this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)); }

        @Override
        public boolean canUse() {
            boolean forced = VeilFoxEntity.this.forcedPackGame.equals("night_gathering");
            if (!forced) {
                if (VeilFoxEntity.this.isTame() || VeilFoxEntity.this.isSleeping()) return false;
                boolean isNight = !VeilFoxEntity.this.level().dimensionType().hasFixedTime() && (VeilFoxEntity.this.level().getDayTime() % 24000L >= 13000L);
                if (!isNight || VeilFoxEntity.this.getRandom().nextInt(100) != 0) return false;
            }

            List<VeilFoxEntity> friends = VeilFoxEntity.this.level().getEntitiesOfClass(VeilFoxEntity.class, VeilFoxEntity.this.getBoundingBox().inflate(32.0D), f -> !f.isTame());
            if (friends.isEmpty() && !forced) return false;

            int avgX = (int)VeilFoxEntity.this.getX(), avgY = (int)VeilFoxEntity.this.getY(), avgZ = (int)VeilFoxEntity.this.getZ();
            if (!friends.isEmpty()) {
                for (VeilFoxEntity f : friends) { avgX += f.getX(); avgY += f.getY(); avgZ += f.getZ(); }
                avgX /= (friends.size() + 1);
                avgY /= (friends.size() + 1);
                avgZ /= (friends.size() + 1);
            }
            gatheringCenter = new BlockPos(avgX, avgY, avgZ);
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (VeilFoxEntity.this.forcedPackGame.equals("night_gathering")) return true;
            long time = VeilFoxEntity.this.level().getDayTime() % 24000L;
            return !VeilFoxEntity.this.isTame() && !VeilFoxEntity.this.isSleeping() && time >= 13000L && time <= 23000L;
        }

        @Override
        public void start() {
            VeilFoxEntity.this.forcedPackGame = "";
            VeilFoxEntity.this.setOrderedToSit(false);
        }

        @Override
        public void tick() {
            if (gatheringCenter == null) return;
            if (VeilFoxEntity.this.distanceToSqr(gatheringCenter.getX(), gatheringCenter.getY(), gatheringCenter.getZ()) > 64.0D) {
                VeilFoxEntity.this.setOrderedToSit(false);
                VeilFoxEntity.this.getNavigation().moveTo(gatheringCenter.getX(), gatheringCenter.getY(), gatheringCenter.getZ(), 0.6D);
            } else {
                VeilFoxEntity.this.setOrderedToSit(true);
                long syncTime = VeilFoxEntity.this.level().getGameTime();
                if ((syncTime % 100 == 0 || syncTime % 100 == 15) && VeilFoxEntity.this.getRandom().nextFloat() < 0.6f) {
                    VeilFoxEntity.this.teleportRandomly(5, false, false);
                    VeilFoxEntity.this.setOrderedToSit(true);
                }
            }
        }
        @Override
        public void stop() { VeilFoxEntity.this.setOrderedToSit(false); gatheringCenter = null; }
    }
}