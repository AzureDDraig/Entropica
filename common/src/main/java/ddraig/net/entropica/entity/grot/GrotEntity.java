package ddraig.net.entropica.entity.grot;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.materia.MateriaFumusStack;
import ddraig.net.entropica.api.materia.MateriaStack;
import ddraig.net.entropica.block.VaporPneumaticPipeBlock;
import ddraig.net.entropica.block.entity.VaporPneumaticPipeBlockEntity;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.registry.ModAttachments;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GrotEntity extends Slime {

    private static final EntityDataAccessor<String> ESSENCE_TYPE = SynchedEntityData.defineId(GrotEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> IS_STERILE = SynchedEntityData.defineId(GrotEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FROM_SPLIT = SynchedEntityData.defineId(GrotEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CAN_RANGED = SynchedEntityData.defineId(GrotEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_FUSING = SynchedEntityData.defineId(GrotEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> FUSION_TARGET_ID = SynchedEntityData.defineId(GrotEntity.class, EntityDataSerializers.INT);

    // --- GENETICS DATA ACCESSORS ---
    private static final EntityDataAccessor<Float> SCALE_FACTOR = SynchedEntityData.defineId(GrotEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> STRENGTH_FACTOR = SynchedEntityData.defineId(GrotEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> VIS_FACTOR = SynchedEntityData.defineId(GrotEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SPEED_FACTOR = SynchedEntityData.defineId(GrotEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WATER_SPEED_FACTOR = SynchedEntityData.defineId(GrotEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> INTELLIGENCE = SynchedEntityData.defineId(GrotEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FRIENDLINESS = SynchedEntityData.defineId(GrotEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_TAMED = SynchedEntityData.defineId(GrotEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_RIDEABLE = SynchedEntityData.defineId(GrotEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> OWNER_UUID = SynchedEntityData.defineId(GrotEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> PURITY = SynchedEntityData.defineId(GrotEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FERTILITY = SynchedEntityData.defineId(GrotEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_COHESIVE = SynchedEntityData.defineId(GrotEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> REBEL_CHANCE = SynchedEntityData.defineId(GrotEntity.class, EntityDataSerializers.INT);

    private int contactTicks = 0;
    private int fusionTicks = 0;
    private boolean lastTickOnGround = false;

    public int healTime = 0;

    // Breeding Tracking
    public int inLove = 0;
    public int loveCooldown = 0;

    // --- PLAYFUL STATE TRACKING (Transient) ---
    public LivingEntity tagTarget = null;
    public int tagTimer = 0;
    public boolean isSeeker = false;
    public boolean isHider = false;
    public int hideSeekTimer = 0;
    public GrotEntity seekTarget = null;

    public int tagCooldown = 0;
    public int happyJumps = 0;

    public GrotEntity(EntityType<? extends Slime> type, Level level) {
        super(type, level);
        this.moveControl = new MoveControl(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        float s = 0.51F * (float)Math.max(1, this.getSize()) * this.getScaleFactor();
        return EntityDimensions.fixed(s, s);
    }

    @Override
    public void heal(float amount) {
        float oldHealth = this.getHealth();
        super.heal(amount);
        if (this.getHealth() > oldHealth) {
            this.healTime = 10;
        }
    }

    @Override
    public boolean canBeLeashed() {
        return true;
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        if (this.isRideable() && this.getIntelligence() > 8) {
            Entity first = this.getFirstPassenger();
            if (first instanceof Player player) {
                return player;
            }
        }
        return super.getControllingPassenger();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.isTamed() && source.getEntity() instanceof Player player) {
            if (!player.isCrouching()) {
                return false;
            }

            Optional<UUID> ownerId = this.getOwnerUUID();
            if (ownerId.isPresent() && player.getUUID().equals(ownerId.get())) {
                this.setFriendliness(Math.max(0, this.getFriendliness() - 15));
                this.setRebelChance(Math.min(100, this.getRebelChance() + 15));

                if (this.getRandom().nextInt(100) < this.getRebelChance()) {
                    this.setTamed(false);
                    this.setOwnerUUID(null);
                    this.setRebelChance(0);

                    this.setTarget(player);
                    this.playSound(SoundEvents.ENDER_DRAGON_GROWL, 1.0f, 1.5f);

                    double alertRange = 20.0D;
                    AABB alertBox = this.getBoundingBox().inflate(alertRange);
                    List<GrotEntity> friends = this.level().getEntitiesOfClass(GrotEntity.class, alertBox);
                    for (GrotEntity friend : friends) {
                        if (friend != this && friend.getTarget() == null && friend.isAlive() && !friend.isTamed()) {
                            friend.setTarget(player);
                        }
                    }
                }
            }
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (this.isRideable() && stack.isEmpty() && !player.isSecondaryUseActive()) {
            if (!this.level().isClientSide()) {
                player.startRiding(this);
            }
            return InteractionResult.SUCCESS;
        }

        if (stack.getItem() instanceof EssenceItem eItem) {
            EssenceType t = EssenceItem.getEssenceType(stack);

            if (t == this.getEssenceType()) {
                if (!player.isCreative()) stack.shrink(1);

                if (!this.level().isClientSide()) {
                    if (this.getFriendliness() < 255) {
                        this.setFriendliness(Math.min(255, this.getFriendliness() + 10));
                    }

                    if (!this.isTamed() && this.getFriendliness() > 50 && this.getRandom().nextFloat() < 0.2f) {
                        this.setTamed(true);
                        this.setOwnerUUID(player.getUUID());
                        this.level().broadcastEntityEvent(this, (byte) 7);
                    }
                    else if (!this.isSterile() && this.inLove <= 0 && this.loveCooldown <= 0) {
                        this.inLove = 600;
                        this.level().broadcastEntityEvent(this, (byte) 18);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 18 || id == 7) {
            for(int i = 0; i < 7; ++i) {
                double d0 = this.getRandom().nextGaussian() * 0.02D;
                double d1 = this.getRandom().nextGaussian() * 0.02D;
                double d2 = this.getRandom().nextGaussian() * 0.02D;
                this.level().addParticle(ParticleTypes.HEART, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d0, d1, d2);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));

        // --- NEW: TACTICAL FUSION (Emergency Heal) ---
        this.goalSelector.addGoal(2, new Goal() {
            private GrotEntity targetMate;
            @Override public boolean canUse() {
                // Ensure they aren't breeding (inLove) and aren't tamed.
                if (GrotEntity.this.getIntelligence() < 15 || GrotEntity.this.isTamed() || GrotEntity.this.inLove > 0) return false;
                if (GrotEntity.this.getHealth() > GrotEntity.this.getMaxHealth() * 0.3f) return false;
                if (GrotEntity.this.getSize() >= 8 || GrotEntity.this.isFusing()) return false;

                List<GrotEntity> list = GrotEntity.this.level().getEntitiesOfClass(GrotEntity.class, GrotEntity.this.getBoundingBox().inflate(16.0D));
                for (GrotEntity other : list) {
                    if (other != GrotEntity.this && other.getSize() == GrotEntity.this.getSize() && !other.isFusing() && other.isAlive() && !other.isTamed() && other.inLove <= 0) {
                        targetMate = other;
                        return true;
                    }
                }
                return false;
            }
            @Override public void start() {
                GrotEntity.this.setTarget(null); // Abort combat!
            }
            @Override public boolean canContinueToUse() {
                return targetMate != null && targetMate.isAlive() && !GrotEntity.this.isFusing();
            }
            @Override public void tick() {
                if (targetMate != null && targetMate.isAlive()) {
                    GrotEntity.this.getNavigation().moveTo(targetMate, 1.5D);
                    if (GrotEntity.this.distanceToSqr(targetMate) < 3.0D) {
                        GrotEntity.this.setFusing(true);
                        targetMate.setFusing(true);
                        GrotEntity.this.setFusionTarget(targetMate.getId());
                        targetMate.setFusionTarget(GrotEntity.this.getId());
                        GrotEntity.this.contactTicks = 100;
                        targetMate.contactTicks = 100;
                        GrotEntity.this.fusionTicks = 25; // Skip almost the entire struggle
                    }
                }
            }
        });

        // --- NEW: LOOT DENIAL (Greedy Grot) ---
        this.goalSelector.addGoal(3, new Goal() {
            private ItemEntity targetItem;
            @Override public boolean canUse() {
                if (GrotEntity.this.getIntelligence() < 8 || GrotEntity.this.isTamed()) return false;
                List<ItemEntity> items = GrotEntity.this.level().getEntitiesOfClass(ItemEntity.class, GrotEntity.this.getBoundingBox().inflate(12.0D));
                for (ItemEntity item : items) {
                    Item i = item.getItem().getItem();
                    if (i == ModItems.AVERAGE_ESSENCE.get() || i == ModItems.STRONG_ESSENCE.get()) {
                        targetItem = item;
                        return true;
                    }
                }
                return false;
            }
            @Override public void start() { GrotEntity.this.setTarget(null); }
            @Override public boolean canContinueToUse() {
                return targetItem != null && targetItem.isAlive();
            }
            @Override public void tick() {
                if (targetItem != null && targetItem.isAlive()) {
                    GrotEntity.this.getNavigation().moveTo(targetItem, 1.4D);
                    if (GrotEntity.this.distanceToSqr(targetItem) < 2.0D) {
                        targetItem.discard();
                        GrotEntity.this.playSound(SoundEvents.GENERIC_EAT.value(), 1.0F, 1.0F);
                        GrotEntity.this.heal(GrotEntity.this.getMaxHealth());
                        GrotEntity.this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 1));
                        GrotEntity.this.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 600, 1));
                        if (GrotEntity.this.getRandom().nextBoolean() && GrotEntity.this.getPurity() < 28) {
                            GrotEntity.this.setPurity(GrotEntity.this.getPurity() + 1);
                        }
                        targetItem = null;
                    }
                }
            }
        });

        // BREEDING GOAL
        this.goalSelector.addGoal(4, new Goal() {
            private GrotEntity mate;

            @Override public boolean canUse() {
                if (GrotEntity.this.inLove <= 0 || GrotEntity.this.isSterile()) return false;
                List<GrotEntity> list = GrotEntity.this.level().getEntitiesOfClass(GrotEntity.class, GrotEntity.this.getBoundingBox().inflate(8.0D));
                for (GrotEntity other : list) {
                    if (other != GrotEntity.this && other.inLove > 0 && !other.isSterile()) {
                        mate = other;
                        return true;
                    }
                }
                return false;
            }

            @Override public boolean canContinueToUse() {
                return mate != null && mate.isAlive() && mate.inLove > 0 && GrotEntity.this.inLove > 0;
            }

            @Override public void tick() {
                GrotEntity.this.getLookControl().setLookAt(mate, 10.0F, 30.0F);
                GrotEntity.this.getNavigation().moveTo(mate, 1.0D);

                if (GrotEntity.this.distanceToSqr(mate) < 3.0D) {
                    GrotEntity.this.inLove = 0;
                    mate.inLove = 0;

                    int cooldown = Math.max(1200, 6000 - (GrotEntity.this.getFertility() * 200));
                    GrotEntity.this.loveCooldown = cooldown;
                    mate.loveCooldown = cooldown;

                    if (!GrotEntity.this.level().isClientSide()) {
                        int numBabies = 1;
                        float extraChance = (GrotEntity.this.getFertility() + mate.getFertility()) * 0.025f;
                        if (GrotEntity.this.getRandom().nextFloat() < extraChance) numBabies++;
                        if (GrotEntity.this.getRandom().nextFloat() < extraChance - 1.0f) numBabies++;

                        for (int b = 0; b < numBabies; b++) {
                            GrotEntity baby = (GrotEntity) GrotEntity.this.getType().create(GrotEntity.this.level(), EntitySpawnReason.BREEDING);
                            if (baby != null) {
                                EssenceType t1 = GrotEntity.this.getEssenceType();
                                EssenceType t2 = mate.getEssenceType();
                                EssenceType fusion = GrotFusionHelper.getFusion(t1, t2);

                                if (fusion != null && GrotEntity.this.getRandom().nextFloat() < 0.75f) {
                                    baby.setEssenceType(fusion);
                                } else {
                                    baby.setEssenceType(GrotEntity.this.getRandom().nextBoolean() ? t1 : t2);
                                }

                                baby.setScaleFactor(GrotEntity.this.mutate((GrotEntity.this.getScaleFactor() + mate.getScaleFactor()) / 2f, 0.75f, 1.25f));
                                baby.setStrengthFactor(GrotEntity.this.mutate((GrotEntity.this.getStrengthFactor() + mate.getStrengthFactor()) / 2f, 0.5f, 1.5f));
                                baby.setVisFactor(GrotEntity.this.mutate((GrotEntity.this.getVisFactor() + mate.getVisFactor()) / 2f, 0.5f, 1.5f));
                                baby.setSpeedFactor(GrotEntity.this.mutate((GrotEntity.this.getSpeedFactor() + mate.getSpeedFactor()) / 2f, 0.8f, 1.2f));
                                baby.setWaterSpeedFactor(GrotEntity.this.mutate((GrotEntity.this.getWaterSpeedFactor() + mate.getWaterSpeedFactor()) / 2f, 0.65f, 1.35f));

                                baby.setIntelligence((GrotEntity.this.getIntelligence() + mate.getIntelligence()) / 2);
                                baby.setFriendliness((GrotEntity.this.getFriendliness() + mate.getFriendliness()) / 2);

                                baby.setPurity(Mth.clamp((GrotEntity.this.getPurity() + mate.getPurity()) / 2 + (GrotEntity.this.getRandom().nextInt(3) - 1), 1, 28));
                                baby.setFertility(Mth.clamp((GrotEntity.this.getFertility() + mate.getFertility()) / 2 + (GrotEntity.this.getRandom().nextInt(3) - 1), 1, 20));
                                baby.setCohesive(GrotEntity.this.isCohesive() || mate.isCohesive() ? GrotEntity.this.getRandom().nextFloat() < 0.8f : GrotEntity.this.getRandom().nextFloat() < 0.05f);

                                float rangedChance = 0.05f;
                                if (GrotEntity.this.canRanged() && mate.canRanged()) rangedChance = 0.80f;
                                else if (GrotEntity.this.canRanged() || mate.canRanged()) rangedChance = 0.40f;
                                baby.setCanRanged(GrotEntity.this.getRandom().nextFloat() < rangedChance);

                                baby.setSize(1, true);
                                baby.setPos(GrotEntity.this.getX(), GrotEntity.this.getY(), GrotEntity.this.getZ());
                                GrotEntity.this.level().addFreshEntity(baby);
                            }
                        }

                        ((ServerLevel)GrotEntity.this.level()).sendParticles(ParticleTypes.HEART, GrotEntity.this.getX(), GrotEntity.this.getY() + 0.5, GrotEntity.this.getZ(), 7, 0.3, 0.3, 0.3, 0.0);
                        GrotEntity.this.playSound(SoundEvents.SLIME_SQUISH, 1.0F, 1.5F);
                    }
                }
            }
        });

        // --- NEW: CONDUCTOR WATER SEEKING ---
        this.goalSelector.addGoal(5, new Goal() {
            private BlockPos waterPos;
            @Override public boolean canUse() {
                if (GrotEntity.this.getIntelligence() < 10 || GrotEntity.this.isTamed()) return false;
                if (GrotEntity.this.getTarget() == null || GrotEntity.this.isInWater()) return false;

                EssenceType t = GrotEntity.this.getEssenceType();
                boolean isConductor = (t == EssenceType.LIGHTNING || t == EssenceType.STORM || t == EssenceType.STATIC);
                boolean isPuddle = (t == EssenceType.WATER || t == EssenceType.VAPOR || t == EssenceType.FLOW);
                if (!isConductor && !isPuddle) return false;

                BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();
                for (int x = -8; x <= 8; x++) {
                    for (int y = -3; y <= 3; y++) {
                        for (int z = -8; z <= 8; z++) {
                            mPos.set(GrotEntity.this.getX() + x, GrotEntity.this.getY() + y, GrotEntity.this.getZ() + z);
                            if (GrotEntity.this.level().getFluidState(mPos).is(FluidTags.WATER)) {
                                waterPos = mPos.immutable();
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
            @Override public boolean canContinueToUse() {
                return waterPos != null && !GrotEntity.this.isInWater() && GrotEntity.this.getTarget() != null;
            }
            @Override public void tick() {
                GrotEntity.this.getNavigation().moveTo(waterPos.getX(), waterPos.getY(), waterPos.getZ(), 1.3D);
            }
        });

        // --- NEW: FALSE FLEE (Baiting) ---
        this.goalSelector.addGoal(6, new Goal() {
            private Vec3 fleeDir;
            @Override public boolean canUse() {
                if (GrotEntity.this.getIntelligence() < 14 || GrotEntity.this.isTamed()) return false;
                LivingEntity target = GrotEntity.this.getTarget();
                if (target == null) return false;

                if (GrotEntity.this.getHealth() > GrotEntity.this.getMaxHealth() * 0.8f) return false;

                List<GrotEntity> friends = GrotEntity.this.level().getEntitiesOfClass(GrotEntity.class, GrotEntity.this.getBoundingBox().inflate(15.0D), e -> e != GrotEntity.this && e.getTarget() == target);
                if (friends.size() < 2) return false;

                Vec3 packCenter = Vec3.ZERO;
                for (GrotEntity f : friends) packCenter = packCenter.add(f.position());
                packCenter = packCenter.scale(1.0 / friends.size());

                fleeDir = GrotEntity.this.position().subtract(packCenter).normalize().scale(10);
                return true;
            }
            @Override public void tick() {
                if (fleeDir != null) {
                    GrotEntity.this.getNavigation().moveTo(GrotEntity.this.getX() + fleeDir.x, GrotEntity.this.getY(), GrotEntity.this.getZ() + fleeDir.z, 1.4D);
                }
            }
        });

        // FLEE GOAL (Spooked Avoidance)
        this.goalSelector.addGoal(7, new Goal() {
            private Player player;
            @Override public boolean canUse() {
                if (GrotEntity.this.getTarget() != null) return false;

                boolean isSmartAndScared = GrotEntity.this.getIntelligence() >= 10 && !GrotEntity.this.isTamed();
                boolean isHealthyFlee = GrotEntity.this.getHealth() >= GrotEntity.this.getMaxHealth() * 0.9f;

                if (!isSmartAndScared && !isHealthyFlee) return false;

                List<Player> players = GrotEntity.this.level().getEntitiesOfClass(Player.class, GrotEntity.this.getBoundingBox().inflate(12.0D));
                if (players.isEmpty()) return false;
                player = players.get(0);
                if (player.isCreative() || player.isSpectator()) return false;

                if (isSmartAndScared) {
                    List<GrotEntity> friends = GrotEntity.this.level().getEntitiesOfClass(GrotEntity.class, GrotEntity.this.getBoundingBox().inflate(20.0D));
                    for (GrotEntity friend : friends) {
                        if (friend != GrotEntity.this && friend.getTarget() == null && friend.isAlive() && !friend.isTamed()) {
                            friend.setTarget(player);
                        }
                    }
                }
                return true;
            }
            @Override public boolean canContinueToUse() {
                return player != null && player.isAlive() && GrotEntity.this.getTarget() == null;
            }
            @Override public void tick() {
                if (player != null) {
                    Vec3 dir = GrotEntity.this.position().subtract(player.position()).normalize().scale(5);
                    GrotEntity.this.getNavigation().moveTo(GrotEntity.this.getX() + dir.x, GrotEntity.this.getY() + dir.y, GrotEntity.this.getZ() + dir.z, 0.8D);
                }
            }
        });

        // PRIMARY COMBAT GOAL (Now includes Medics and Conductor Traps)
        this.goalSelector.addGoal(8, new Goal() {
            private int jumpDelay = 0;
            private int rangedCooldown = 0;
            private int attackCooldown = 0;

            @Override public boolean canUse() {
                return GrotEntity.this.getTarget() != null && GrotEntity.this.getHealth() < GrotEntity.this.getMaxHealth() * 0.9f;
            }
            @Override public boolean canContinueToUse() {
                return GrotEntity.this.getTarget() != null && GrotEntity.this.getTarget().isAlive() && GrotEntity.this.getHealth() < GrotEntity.this.getMaxHealth() * 0.9f;
            }
            @Override public void tick() {
                LivingEntity target = GrotEntity.this.getTarget();
                if (target != null) {
                    GrotEntity.this.getLookControl().setLookAt(target, 30.0F, 30.0F);

                    double distSq = GrotEntity.this.distanceToSqr(target);
                    double reach = 1.0 + (GrotEntity.this.getBbWidth() / 2.0);

                    // --- COMBAT MEDIC LOGIC ---
                    if (target instanceof GrotEntity ally) {
                        if (distSq > reach * reach * 4.0D) {
                            GrotEntity.this.getNavigation().moveTo(ally, 1.2D);
                        } else {
                            GrotEntity.this.getNavigation().stop();
                            if (attackCooldown <= 0) {
                                ally.heal(4.0f);
                                ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
                                GrotEntity.this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.5F);
                                if (GrotEntity.this.level() instanceof ServerLevel sl) {
                                    sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, ally.getX(), ally.getY() + ally.getBbHeight()/2, ally.getZ(), 5, 0.3, 0.3, 0.3, 0.0);
                                }
                                attackCooldown = 40;
                            }
                        }
                        if (jumpDelay > 0) jumpDelay--;
                        if (attackCooldown > 0) attackCooldown--;
                        return; // Skip normal attacking
                    }

                    EssenceType t = GrotEntity.this.getEssenceType();

                    // --- CONDUCTOR TRAP LOGIC ---
                    boolean isConductor = (t == EssenceType.LIGHTNING || t == EssenceType.STORM || t == EssenceType.STATIC);
                    if (GrotEntity.this.getIntelligence() >= 10 && isConductor && GrotEntity.this.isInWater()) {
                        GrotEntity.this.getNavigation().stop(); // Hold ground in the water
                        if (rangedCooldown <= 0 && target.isInWater() && distSq < 100.0D) {
                            rangedCooldown = 40;
                            List<LivingEntity> inWater = GrotEntity.this.level().getEntitiesOfClass(LivingEntity.class, GrotEntity.this.getBoundingBox().inflate(10.0D), e -> e.isInWater() && e != GrotEntity.this && !(e instanceof GrotEntity));
                            for (LivingEntity e : inWater) {
                                e.hurtServer((ServerLevel)GrotEntity.this.level(), GrotEntity.this.damageSources().magic(), 4.0F * GrotEntity.this.getVisFactor());
                                e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 2));
                                if (GrotEntity.this.level() instanceof ServerLevel sl) {
                                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, e.getX(), e.getY() + 0.5, e.getZ(), 10, 0.5, 0.5, 0.5, 0.1);
                                }
                            }
                            GrotEntity.this.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 0.5F, 2.0F);
                        }
                        if (rangedCooldown > 0) rangedCooldown--;
                        return; // Skip normal attacking
                    }

                    // Standard Movement
                    if (GrotEntity.this.canRanged() && distSq < 144.0 && distSq > reach * reach) {
                        GrotEntity.this.getNavigation().stop();
                    } else {
                        GrotEntity.this.getNavigation().moveTo(target, 1.2D);
                    }

                    if (jumpDelay > 0) jumpDelay--;
                    if (rangedCooldown > 0) rangedCooldown--;
                    if (attackCooldown > 0) attackCooldown--;

                    // RANGED ATTACK LOGIC
                    if (GrotEntity.this.canRanged() && rangedCooldown <= 0 && distSq > reach * reach && distSq < 144.0) {
                        rangedCooldown = 60 + GrotEntity.this.getRandom().nextInt(40);
                        Vec3 dir = target.position().subtract(GrotEntity.this.position()).normalize();

                        if (t == EssenceType.AIR || t == EssenceType.STORM || t == EssenceType.LIGHTNING || t == EssenceType.VAPOR) {
                            target.setDeltaMovement(target.getDeltaMovement().add(dir.x * 1.5, 0.5, dir.z * 1.5));
                            if (!GrotEntity.this.level().isClientSide()) {
                                GrotEntity.this.doHurtTarget((ServerLevel)GrotEntity.this.level(), target);
                                ((ServerLevel)GrotEntity.this.level()).sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY() + target.getBbHeight()/2, target.getZ(), 5, 0.2, 0.2, 0.2, 0.2);
                            }
                            GrotEntity.this.playSound(SoundEvents.WIND_CHARGE_BURST.value(), 1.0F, 1.0F);

                        } else if (t == EssenceType.NETHER || t == EssenceType.MAGMA || t == EssenceType.PYRE) {
                            SmallFireball fireball = EntityType.SMALL_FIREBALL.create(GrotEntity.this.level(), EntitySpawnReason.TRIGGERED);
                            if (fireball != null) {
                                fireball.setOwner(GrotEntity.this);
                                fireball.setPos(GrotEntity.this.getX(), GrotEntity.this.getY() + GrotEntity.this.getBbHeight(), GrotEntity.this.getZ());
                                fireball.shoot(dir.x, dir.y, dir.z, 1.5F, 1.0F);
                                GrotEntity.this.level().addFreshEntity(fireball);
                                GrotEntity.this.playSound(SoundEvents.BLAZE_SHOOT, 1.0F, 1.0F);
                            }

                        } else if (t == EssenceType.VOID || t == EssenceType.ASTRAL || t == EssenceType.UMBRAL || t == EssenceType.NULL_R || t == EssenceType.NULL_U) {
                            GrotEntity.this.randomTeleport(target.getX(), target.getY() + 1.0, target.getZ(), true);
                            GrotEntity.this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                            if (GrotEntity.this.level() instanceof ServerLevel sl) {
                                GrotEntity.this.doHurtTarget(sl, target);
                            }
                        }
                    }

                    // MELEE LEAP LOGIC
                    if (distSq <= reach * reach * 3.0 && jumpDelay <= 0 && GrotEntity.this.onGround()) {
                        Vec3 dir = target.position().subtract(GrotEntity.this.position()).normalize();
                        GrotEntity.this.setDeltaMovement(dir.x * 0.5, 0.4, dir.z * 0.5);
                        GrotEntity.this.hasImpulse = true;
                        jumpDelay = 20 + GrotEntity.this.getRandom().nextInt(20);
                        GrotEntity.this.playSound(SoundEvents.SLIME_JUMP, 1.0F, 1.0F);
                    }

                    // TRUE MELEE DAMAGE
                    if (distSq <= reach * reach && attackCooldown <= 0) {
                        if (GrotEntity.this.level() instanceof ServerLevel serverLevel) {
                            boolean hit = GrotEntity.this.doHurtTarget(serverLevel, target);
                            if (hit) {
                                GrotEntity.this.playSound(SoundEvents.SLIME_ATTACK, 1.0F, 1.0F);
                                attackCooldown = 20;
                            }
                        }
                    }
                }
            }
        });

        // FRIENDLY JUMP GOAL
        this.goalSelector.addGoal(9, new Goal() {
            private Player targetPlayer;
            private int jumpDelay = 0;

            @Override public boolean canUse() {
                if (GrotEntity.this.getFriendliness() <= 120 || GrotEntity.this.getTarget() != null || GrotEntity.this.isFusing()) return false;
                if (GrotEntity.this.getRandom().nextInt(40) != 0) return false;

                List<Player> players = GrotEntity.this.level().getEntitiesOfClass(Player.class, GrotEntity.this.getBoundingBox().inflate(8.0D));
                if (players.isEmpty()) return false;
                targetPlayer = players.get(0);
                return true;
            }

            @Override public boolean canContinueToUse() {
                return targetPlayer != null && targetPlayer.isAlive() && GrotEntity.this.getTarget() == null && GrotEntity.this.distanceToSqr(targetPlayer) < 100.0D;
            }

            @Override public void tick() {
                GrotEntity.this.getLookControl().setLookAt(targetPlayer, 30.0F, 30.0F);
                double distSq = GrotEntity.this.distanceToSqr(targetPlayer);

                if (jumpDelay > 0) jumpDelay--;

                if (distSq > 4.0D && jumpDelay <= 0 && GrotEntity.this.onGround()) {
                    Vec3 dir = targetPlayer.position().subtract(GrotEntity.this.position()).normalize();
                    GrotEntity.this.setDeltaMovement(dir.x * 0.4, 0.3, dir.z * 0.4);
                    GrotEntity.this.hasImpulse = true;
                    jumpDelay = 30 + GrotEntity.this.getRandom().nextInt(20);
                    GrotEntity.this.playSound(SoundEvents.SLIME_JUMP, 0.5F, 1.5F);

                    if (GrotEntity.this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, GrotEntity.this.getX(), GrotEntity.this.getY() + GrotEntity.this.getBbHeight(), GrotEntity.this.getZ(), 3, 0.2, 0.2, 0.2, 0.0);
                    }
                }
            }
        });

        this.goalSelector.addGoal(10, new Goal() {
            private BlockPos targetPipe = null;
            private int drainCooldown = 0;

            @Override public boolean canUse() {
                if (GrotEntity.this.getTarget() != null) return false;
                if (targetPipe != null && isValidPipe(targetPipe)) return true;
                if (GrotEntity.this.tickCount % 40 != 0) return false;

                targetPipe = findVisPipe();
                return targetPipe != null;
            }

            @Override public boolean canContinueToUse() {
                return GrotEntity.this.getTarget() == null && targetPipe != null && isValidPipe(targetPipe);
            }

            private boolean isValidPipe(BlockPos pos) {
                if (GrotEntity.this.level().getBlockState(pos).getBlock() instanceof VaporPneumaticPipeBlock) {
                    if (GrotEntity.this.level().getBlockEntity(pos) instanceof VaporPneumaticPipeBlockEntity pipe) {
                        return !pipe.getMateriaInTank().isEmpty();
                    }
                }
                return false;
            }

            private BlockPos findVisPipe() {
                BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();
                int radius = 10;
                for (int x = -radius; x <= radius; x++) {
                    for (int y = -4; y <= 4; y++) {
                        for (int z = -radius; z <= radius; z++) {
                            mPos.set(GrotEntity.this.getX() + x, GrotEntity.this.getY() + y, GrotEntity.this.getZ() + z);
                            if (isValidPipe(mPos)) {
                                return mPos.immutable();
                            }
                        }
                    }
                }
                return null;
            }

            @Override public void tick() {
                if (targetPipe != null) {
                    GrotEntity.this.getLookControl().setLookAt(targetPipe.getX() + 0.5, targetPipe.getY() + 0.5, targetPipe.getZ() + 0.5, 30.0F, 30.0F);
                    GrotEntity.this.getNavigation().moveTo(targetPipe.getX(), targetPipe.getY(), targetPipe.getZ(), 1.0D);

                    if (drainCooldown > 0) drainCooldown--;

                    Vec3 pipeCenter = Vec3.atCenterOf(targetPipe);
                    double distSq = GrotEntity.this.position().distanceToSqr(pipeCenter);
                    double reach = 1.5 + (GrotEntity.this.getBbWidth() / 2.0);

                    if (distSq <= reach * reach && drainCooldown <= 0) {
                        if (GrotEntity.this.level().getBlockEntity(targetPipe) instanceof VaporPneumaticPipeBlockEntity pipe) {
                            if (!pipe.getMateriaInTank().isEmpty()) {
                                MateriaStack drained = pipe.drain(10, false);

                                if (!drained.isEmpty()) {
                                    GrotEntity.this.playSound(SoundEvents.BREWING_STAND_BREW, 1.0f, 1.5f);
                                    GrotEntity.this.heal(2.0f);

                                    if (GrotEntity.this.getRandom().nextFloat() < 0.05f && GrotEntity.this.getSize() < 8) {
                                        GrotEntity.this.setSize(GrotEntity.this.getSize() * 2, false);
                                    }
                                    drainCooldown = 40;

                                    if (GrotEntity.this.level() instanceof ServerLevel sl) {
                                        sl.sendParticles(ParticleTypes.ENCHANT, targetPipe.getX() + 0.5, targetPipe.getY() + 0.5, targetPipe.getZ() + 0.5, 10, 0.2, 0.2, 0.2, 0.05);
                                    }
                                }
                            } else {
                                targetPipe = null;
                            }
                        } else {
                            targetPipe = null;
                        }
                    }
                }
            }
        });

        this.goalSelector.addGoal(11, new Goal() {
            private ItemEntity targetItem;
            @Override public boolean canUse() {
                EssenceType t = GrotEntity.this.getEssenceType();
                if (t != EssenceType.EARTH && t != EssenceType.DUST) return false;
                if (GrotEntity.this.getTarget() != null) return false;
                if (targetItem != null && targetItem.isAlive()) return true;
                if (GrotEntity.this.tickCount % 20 != 0) return false;

                List<ItemEntity> items = GrotEntity.this.level().getEntitiesOfClass(ItemEntity.class, GrotEntity.this.getBoundingBox().inflate(6.0D));
                for (ItemEntity item : items) {
                    Item i = item.getItem().getItem();
                    if (i == Items.REDSTONE || i == Items.QUARTZ || i == Items.LAPIS_LAZULI || i == Items.COPPER_INGOT) {
                        targetItem = item;
                        return true;
                    }
                }
                return false;
            }
            @Override public boolean canContinueToUse() {
                return targetItem != null && targetItem.isAlive() && GrotEntity.this.getTarget() == null;
            }
            @Override public void tick() {
                if (targetItem != null && targetItem.isAlive()) {
                    GrotEntity.this.getLookControl().setLookAt(targetItem, 30.0F, 30.0F);
                    GrotEntity.this.getNavigation().moveTo(targetItem, 1.0D);

                    double reach = 1.0 + (GrotEntity.this.getBbWidth() / 2.0);
                    if (GrotEntity.this.distanceToSqr(targetItem) < reach * reach) {
                        ItemStack stack = targetItem.getItem();
                        Item i = stack.getItem();
                        Item result = null;

                        if (i == Items.REDSTONE) result = Items.QUARTZ;
                        else if (i == Items.QUARTZ) result = Items.REDSTONE;
                        else if (i == Items.LAPIS_LAZULI) result = Items.COPPER_INGOT;
                        else if (i == Items.COPPER_INGOT) result = Items.LAPIS_LAZULI;

                        if (result != null) {
                            targetItem.setItem(new ItemStack(result, stack.getCount()));
                            GrotEntity.this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.0F);
                            targetItem = null;
                        }
                    }
                }
            }
        });

        this.goalSelector.addGoal(12, new Goal() {
            private double tx, ty, tz;
            @Override public boolean canUse() {
                if (GrotEntity.this.getTarget() != null) return false;
                if (GrotEntity.this.tickCount % 80 != 0) return false;
                tx = GrotEntity.this.getX() + (GrotEntity.this.getRandom().nextInt(11) - 5);
                ty = GrotEntity.this.getY();
                tz = GrotEntity.this.getZ() + (GrotEntity.this.getRandom().nextInt(11) - 5);
                return true;
            }
            @Override public boolean canContinueToUse() {
                return !GrotEntity.this.getNavigation().isDone() && GrotEntity.this.getTarget() == null;
            }
            @Override public void start() {
                GrotEntity.this.getNavigation().moveTo(tx, ty, tz, 0.8D);
            }
        });

        this.goalSelector.addGoal(13, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(14, new RandomLookAroundGoal(this));

        // --- NEW: PLAY TAG (CHASER) ---
        this.goalSelector.addGoal(15, new Goal() {
            @Override public boolean canUse() {
                if (GrotEntity.this.getTarget() != null || GrotEntity.this.isFusing() || GrotEntity.this.tagCooldown > 0) return false;
                if (GrotEntity.this.tagTarget != null && GrotEntity.this.tagTarget.isAlive()) return true;
                if (GrotEntity.this.getFriendliness() > 150 && GrotEntity.this.getRandom().nextInt(200) == 0) {
                    List<LivingEntity> potentials = GrotEntity.this.level().getEntitiesOfClass(LivingEntity.class, GrotEntity.this.getBoundingBox().inflate(10.0D),
                            e -> e != GrotEntity.this && e.isAlive() &&
                                    ((e instanceof Player p && !p.isCreative() && !p.isSpectator()) ||
                                            (e instanceof GrotEntity g && !g.isFusing() && g.getTarget() == null && !g.isSeeker && !g.isHider && g.tagCooldown <= 0)));

                    if (!potentials.isEmpty()) {
                        GrotEntity.this.tagTarget = potentials.get(GrotEntity.this.getRandom().nextInt(potentials.size()));
                        GrotEntity.this.tagTimer = 200; // 10 seconds to tag
                        return true;
                    }
                }
                return false;
            }
            @Override public boolean canContinueToUse() {
                return GrotEntity.this.tagTarget != null && GrotEntity.this.tagTarget.isAlive() && GrotEntity.this.tagTimer > 0 && GrotEntity.this.getTarget() == null;
            }
            @Override public void start() {
                GrotEntity.this.playSound(SoundEvents.SLIME_JUMP, 1.0F, 1.5F);
                if (GrotEntity.this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, GrotEntity.this.getX(), GrotEntity.this.getY() + 1, GrotEntity.this.getZ(), 5, 0.2, 0.2, 0.2, 0);
                }
            }
            @Override public void tick() {
                GrotEntity.this.tagTimer--;
                GrotEntity.this.getLookControl().setLookAt(GrotEntity.this.tagTarget, 30.0F, 30.0F);
                GrotEntity.this.getNavigation().moveTo(GrotEntity.this.tagTarget, 1.3D);

                if (GrotEntity.this.distanceToSqr(GrotEntity.this.tagTarget) < 4.0D) {
                    // TAG! You're it!
                    GrotEntity.this.playSound(SoundEvents.SLIME_SQUISH, 1.0F, 2.0F);
                    GrotEntity.this.happyJumps = 2; // Perform queued happy jumps

                    if (GrotEntity.this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.HEART, GrotEntity.this.tagTarget.getX(), GrotEntity.this.tagTarget.getY() + 1, GrotEntity.this.tagTarget.getZ(), 3, 0.2, 0.2, 0.2, 0);
                    }
                    if (GrotEntity.this.tagTarget instanceof GrotEntity otherGrot) {
                        otherGrot.tagTarget = GrotEntity.this; // Make the other grot the chaser
                        otherGrot.tagTimer = 200;
                        otherGrot.tagCooldown = 40; // 2 seconds before tagging back (prevents infinite jump loops)
                    }
                    GrotEntity.this.tagTarget = null;
                    GrotEntity.this.tagCooldown = 40;
                }
            }
            @Override public void stop() { GrotEntity.this.tagTarget = null; }
        });

        // --- NEW: PLAY TAG (FLEE) ---
        this.goalSelector.addGoal(16, new Goal() {
            private LivingEntity chaser;
            @Override public boolean canUse() {
                if (GrotEntity.this.getTarget() != null) return false;
                List<GrotEntity> grots = GrotEntity.this.level().getEntitiesOfClass(GrotEntity.class, GrotEntity.this.getBoundingBox().inflate(10.0D),
                        e -> e.tagTarget == GrotEntity.this);
                if (!grots.isEmpty()) {
                    chaser = grots.get(0);
                    return true;
                }
                return false;
            }
            @Override public boolean canContinueToUse() {
                return chaser != null && chaser.isAlive() && ((GrotEntity)chaser).tagTarget == GrotEntity.this && GrotEntity.this.getTarget() == null;
            }
            @Override public void tick() {
                Vec3 dir = GrotEntity.this.position().subtract(chaser.position()).normalize().scale(5);
                GrotEntity.this.getNavigation().moveTo(GrotEntity.this.getX() + dir.x, GrotEntity.this.getY(), GrotEntity.this.getZ() + dir.z, 1.2D);
            }
        });

        // --- NEW: HIDE AND SEEK (SEEKER) ---
        this.goalSelector.addGoal(17, new Goal() {
            @Override public boolean canUse() {
                if (GrotEntity.this.getTarget() != null || GrotEntity.this.isFusing()) return false;
                if (GrotEntity.this.isSeeker) return true;
                if (GrotEntity.this.getIntelligence() > 10 && GrotEntity.this.getFriendliness() > 120 && GrotEntity.this.getRandom().nextInt(400) == 0) {
                    List<GrotEntity> friends = GrotEntity.this.level().getEntitiesOfClass(GrotEntity.class, GrotEntity.this.getBoundingBox().inflate(10.0D),
                            e -> e != GrotEntity.this && !e.isSeeker && !e.isHider && e.getTarget() == null && e.tagTarget == null);

                    if (friends.size() >= 1) {
                        GrotEntity.this.isSeeker = true;
                        GrotEntity.this.hideSeekTimer = 100; // Counting for 5 seconds
                        for (GrotEntity friend : friends) {
                            friend.isHider = true;
                            friend.hideSeekTimer = 400; // 20 seconds max to hide and be found
                            friend.seekTarget = GrotEntity.this; // Know who to run from
                        }
                        return true;
                    }
                }
                return false;
            }
            @Override public boolean canContinueToUse() {
                return GrotEntity.this.isSeeker && GrotEntity.this.getTarget() == null && GrotEntity.this.hideSeekTimer > -400; // Give up after 20 secs of seeking
            }
            @Override public void start() {
                GrotEntity.this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.5F); // Happy starting sound
            }
            @Override public void tick() {
                GrotEntity.this.hideSeekTimer--;
                if (GrotEntity.this.hideSeekTimer > 0) {
                    // Counting! Spin around looking down.
                    GrotEntity.this.getNavigation().stop();
                    GrotEntity.this.setYRot(GrotEntity.this.getYRot() + 10.0F);
                    GrotEntity.this.setXRot(40.0F);
                } else {
                    // Seeking!
                    List<GrotEntity> hiders = GrotEntity.this.level().getEntitiesOfClass(GrotEntity.class, GrotEntity.this.getBoundingBox().inflate(15.0D), e -> e.isHider);
                    if (!hiders.isEmpty()) {
                        GrotEntity targetHider = hiders.get(0);
                        GrotEntity.this.getLookControl().setLookAt(targetHider, 30.0F, 30.0F);
                        GrotEntity.this.getNavigation().moveTo(targetHider, 1.1D);

                        if (GrotEntity.this.distanceToSqr(targetHider) < 5.0D) {
                            // Found them!
                            targetHider.isHider = false;
                            targetHider.happyJumps = 2; // Queue happy jump
                            GrotEntity.this.happyJumps = 2; // Queue happy jump
                            GrotEntity.this.playSound(SoundEvents.SLIME_SQUISH, 1.0F, 2.0F);

                            if (GrotEntity.this.level() instanceof ServerLevel sl) {
                                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, targetHider.getX(), targetHider.getY() + 1, targetHider.getZ(), 5, 0.2, 0.2, 0.2, 0);
                            }
                        }
                    } else {
                        // Wander randomly searching
                        if (GrotEntity.this.getNavigation().isDone()) {
                            double tx = GrotEntity.this.getX() + (GrotEntity.this.getRandom().nextInt(15) - 7);
                            double tz = GrotEntity.this.getZ() + (GrotEntity.this.getRandom().nextInt(15) - 7);
                            GrotEntity.this.getNavigation().moveTo(tx, GrotEntity.this.getY(), tz, 1.0D);
                        }
                    }
                }
            }
            @Override public void stop() { GrotEntity.this.isSeeker = false; }
        });

        // --- NEW: HIDE AND SEEK (HIDER) ---
        this.goalSelector.addGoal(18, new Goal() {
            @Override public boolean canUse() {
                return GrotEntity.this.isHider && GrotEntity.this.getTarget() == null && GrotEntity.this.hideSeekTimer > 0;
            }
            @Override public boolean canContinueToUse() {
                return GrotEntity.this.isHider && GrotEntity.this.getTarget() == null && GrotEntity.this.hideSeekTimer > 0;
            }
            @Override public void tick() {
                GrotEntity.this.hideSeekTimer--;
                if (GrotEntity.this.seekTarget != null && GrotEntity.this.seekTarget.hideSeekTimer > 0) {
                    // Seeker is counting, run away!
                    Vec3 dir = GrotEntity.this.position().subtract(GrotEntity.this.seekTarget.position()).normalize().scale(8);
                    GrotEntity.this.getNavigation().moveTo(GrotEntity.this.getX() + dir.x, GrotEntity.this.getY(), GrotEntity.this.getZ() + dir.z, 1.2D);
                } else {
                    // Hide! Stay still, occasionally peek
                    GrotEntity.this.getNavigation().stop();
                    if (GrotEntity.this.getRandom().nextInt(40) == 0 && GrotEntity.this.seekTarget != null) {
                        GrotEntity.this.getLookControl().setLookAt(GrotEntity.this.seekTarget, 30.0F, 30.0F);
                    }
                }
            }
            @Override public void stop() {
                GrotEntity.this.isHider = false;
                GrotEntity.this.seekTarget = null;
            }
        });

        this.targetSelector.addGoal(1, new Goal() {
            @Override public boolean canUse() {
                if (GrotEntity.this.isTamed()) return false;
                return GrotEntity.this.getLastHurtByMob() != null && GrotEntity.this.getTarget() == null;
            }
            @Override public void start() {
                LivingEntity attacker = GrotEntity.this.getLastHurtByMob();
                GrotEntity.this.setTarget(attacker);

                double alertRange = 20.0D;
                AABB alertBox = GrotEntity.this.getBoundingBox().inflate(alertRange);
                List<GrotEntity> friends = GrotEntity.this.level().getEntitiesOfClass(GrotEntity.class, alertBox);
                for (GrotEntity friend : friends) {
                    if (friend != GrotEntity.this && friend.getTarget() == null && friend.isAlive() && !friend.isTamed()) {
                        friend.setTarget(attacker);
                    }
                }
            }
        });

        // --- NEW: COMBAT MEDIC TARGETING ---
        this.targetSelector.addGoal(2, new Goal() {
            private GrotEntity patient;
            @Override public boolean canUse() {
                if (GrotEntity.this.isTamed()) return false;
                if (GrotEntity.this.getIntelligence() < 12) return false;
                EssenceType t = GrotEntity.this.getEssenceType();
                if (t != EssenceType.NATURE && t != EssenceType.OVERGROWTH && t != EssenceType.SPORE) return false;

                List<GrotEntity> list = GrotEntity.this.level().getEntitiesOfClass(GrotEntity.class, GrotEntity.this.getBoundingBox().inflate(16.0D));
                for (GrotEntity other : list) {
                    if (other != GrotEntity.this && other.getHealth() < other.getMaxHealth() * 0.5f && other.isAlive()) {
                        patient = other;
                        return true;
                    }
                }
                return false;
            }
            @Override public void start() { GrotEntity.this.setTarget(patient); }
        });

        this.targetSelector.addGoal(3, new Goal() {
            private Player targetPlayer;
            @Override public boolean canUse() {
                if (GrotEntity.this.isTamed()) return false;
                if (GrotEntity.this.getHealth() >= GrotEntity.this.getMaxHealth() * 0.9f || GrotEntity.this.tickCount % 20 != 0) return false;
                List<Player> players = GrotEntity.this.level().getEntitiesOfClass(Player.class, GrotEntity.this.getBoundingBox().inflate(10.0D));
                if (players.isEmpty()) return false;
                targetPlayer = players.get(0);
                return true;
            }
            @Override public void start() { GrotEntity.this.setTarget(targetPlayer); }
        });
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ESSENCE_TYPE, EssenceType.EARTH.name());
        builder.define(IS_STERILE, false);
        builder.define(FROM_SPLIT, false);
        builder.define(CAN_RANGED, false);
        builder.define(IS_FUSING, false);
        builder.define(FUSION_TARGET_ID, -1);

        builder.define(SCALE_FACTOR, 1.0f);
        builder.define(STRENGTH_FACTOR, 1.0f);
        builder.define(VIS_FACTOR, 1.0f);
        builder.define(SPEED_FACTOR, 1.0f);
        builder.define(WATER_SPEED_FACTOR, 1.0f);
        builder.define(INTELLIGENCE, 1);
        builder.define(FRIENDLINESS, 10);
        builder.define(IS_TAMED, false);
        builder.define(IS_RIDEABLE, false);
        builder.define(OWNER_UUID, "");

        builder.define(PURITY, 10);
        builder.define(FERTILITY, 5);
        builder.define(IS_COHESIVE, false);
        builder.define(REBEL_CHANCE, 0);
    }

    public EssenceType getEssenceType() {
        try {
            return EssenceType.valueOf(this.entityData.get(ESSENCE_TYPE));
        } catch (IllegalArgumentException e) {
            return EssenceType.EARTH;
        }
    }

    public void setEssenceType(EssenceType type) { this.entityData.set(ESSENCE_TYPE, type.name()); }

    public boolean isFlightCapable() {
        EssenceType type = this.getEssenceType();
        return type == EssenceType.AIR || type == EssenceType.STORM || type == EssenceType.VOID || type == EssenceType.LIGHTNING || type == EssenceType.VAPOR;
    }

    public boolean isSterile() { return this.entityData.get(IS_STERILE); }
    public void setSterile(boolean sterile) { this.entityData.set(IS_STERILE, sterile); }

    public boolean isFromSplit() { return this.entityData.get(FROM_SPLIT); }
    public void setFromSplit(boolean split) { this.entityData.set(FROM_SPLIT, split); }

    public boolean canRanged() { return this.entityData.get(CAN_RANGED); }
    public void setCanRanged(boolean ranged) { this.entityData.set(CAN_RANGED, ranged); }

    public boolean isFusing() { return this.entityData.get(IS_FUSING); }
    public void setFusing(boolean fusing) { this.entityData.set(IS_FUSING, fusing); }

    public int getFusionTarget() { return this.entityData.get(FUSION_TARGET_ID); }
    public void setFusionTarget(int id) { this.entityData.set(FUSION_TARGET_ID, id); }

    public float getScaleFactor() { return this.entityData.get(SCALE_FACTOR); }
    public void setScaleFactor(float f) { this.entityData.set(SCALE_FACTOR, f); }

    public float getStrengthFactor() { return this.entityData.get(STRENGTH_FACTOR); }
    public void setStrengthFactor(float f) { this.entityData.set(STRENGTH_FACTOR, f); }

    public float getVisFactor() { return this.entityData.get(VIS_FACTOR); }
    public void setVisFactor(float f) { this.entityData.set(VIS_FACTOR, f); }

    public float getSpeedFactor() { return this.entityData.get(SPEED_FACTOR); }
    public void setSpeedFactor(float f) { this.entityData.set(SPEED_FACTOR, f); }

    public float getWaterSpeedFactor() { return this.entityData.get(WATER_SPEED_FACTOR); }
    public void setWaterSpeedFactor(float f) { this.entityData.set(WATER_SPEED_FACTOR, f); }

    public int getIntelligence() { return this.entityData.get(INTELLIGENCE); }
    public void setIntelligence(int i) { this.entityData.set(INTELLIGENCE, Mth.clamp(i, 1, 20)); }

    public int getFriendliness() { return this.entityData.get(FRIENDLINESS); }
    public void setFriendliness(int i) { this.entityData.set(FRIENDLINESS, Mth.clamp(i, 0, 255)); }

    public boolean isTamed() { return this.entityData.get(IS_TAMED); }
    public void setTamed(boolean t) { this.entityData.set(IS_TAMED, t); }

    public boolean isRideable() { return this.entityData.get(IS_RIDEABLE); }
    public void setRideable(boolean r) { this.entityData.set(IS_RIDEABLE, r); }

    public Optional<UUID> getOwnerUUID() {
        String s = this.entityData.get(OWNER_UUID);
        if (s.isEmpty()) return Optional.empty();
        try {
            return Optional.of(UUID.fromString(s));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
    public void setOwnerUUID(UUID uuid) { this.entityData.set(OWNER_UUID, uuid == null ? "" : uuid.toString()); }

    public int getPurity() { return this.entityData.get(PURITY); }
    public void setPurity(int p) { this.entityData.set(PURITY, Mth.clamp(p, 1, 28)); }

    public int getFertility() { return this.entityData.get(FERTILITY); }
    public void setFertility(int f) { this.entityData.set(FERTILITY, Mth.clamp(f, 1, 20)); }

    public boolean isCohesive() { return this.entityData.get(IS_COHESIVE); }
    public void setCohesive(boolean c) { this.entityData.set(IS_COHESIVE, c); }

    public int getRebelChance() { return this.entityData.get(REBEL_CHANCE); }
    public void setRebelChance(int r) { this.entityData.set(REBEL_CHANCE, Mth.clamp(r, 0, 100)); }

    @Override
    public void setSize(int size, boolean resetHealth) {
        super.setSize(size, resetHealth);

        double health = (double)(size * 4) + (this.getPurity() * 0.5);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);

        if (resetHealth) {
            this.setHealth(this.getMaxHealth());
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("EssenceType", Codec.STRING, this.entityData.get(ESSENCE_TYPE));
        output.store("IsSterile", Codec.BOOL, isSterile());
        output.store("FromSplit", Codec.BOOL, isFromSplit());
        output.store("CanRanged", Codec.BOOL, canRanged());
        output.store("InLove", Codec.INT, this.inLove);
        output.store("LoveCooldown", Codec.INT, this.loveCooldown);

        output.store("ScaleFactor", Codec.FLOAT, getScaleFactor());
        output.store("StrengthFactor", Codec.FLOAT, getStrengthFactor());
        output.store("VisFactor", Codec.FLOAT, getVisFactor());
        output.store("SpeedFactor", Codec.FLOAT, getSpeedFactor());
        output.store("WaterSpeedFactor", Codec.FLOAT, getWaterSpeedFactor());
        output.store("Intelligence", Codec.INT, getIntelligence());
        output.store("Friendliness", Codec.INT, getFriendliness());
        output.store("IsTamed", Codec.BOOL, isTamed());
        output.store("IsRideable", Codec.BOOL, isRideable());
        getOwnerUUID().ifPresent(uuid -> output.store("OwnerUUID", Codec.STRING, uuid.toString()));

        output.store("Purity", Codec.INT, getPurity());
        output.store("Fertility", Codec.INT, getFertility());
        output.store("IsCohesive", Codec.BOOL, isCohesive());
        output.store("RebelChance", Codec.INT, getRebelChance());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(ESSENCE_TYPE, input.read("EssenceType", Codec.STRING).orElse("EARTH"));
        setSterile(input.read("IsSterile", Codec.BOOL).orElse(false));
        setFromSplit(input.read("FromSplit", Codec.BOOL).orElse(false));
        setCanRanged(input.read("CanRanged", Codec.BOOL).orElse(false));
        this.inLove = input.read("InLove", Codec.INT).orElse(0);
        this.loveCooldown = input.read("LoveCooldown", Codec.INT).orElse(0);

        setScaleFactor(input.read("ScaleFactor", Codec.FLOAT).orElse(1.0f));
        setStrengthFactor(input.read("StrengthFactor", Codec.FLOAT).orElse(1.0f));
        setVisFactor(input.read("VisFactor", Codec.FLOAT).orElse(1.0f));
        setSpeedFactor(input.read("SpeedFactor", Codec.FLOAT).orElse(1.0f));
        setWaterSpeedFactor(input.read("WaterSpeedFactor", Codec.FLOAT).orElse(1.0f));
        setIntelligence(input.read("Intelligence", Codec.INT).orElse(1));
        setFriendliness(input.read("Friendliness", Codec.INT).orElse(10));
        setTamed(input.read("IsTamed", Codec.BOOL).orElse(false));
        setRideable(input.read("IsRideable", Codec.BOOL).orElse(false));

        String uuidStr = input.read("OwnerUUID", Codec.STRING).orElse("");
        if (!uuidStr.isEmpty()) {
            setOwnerUUID(UUID.fromString(uuidStr));
        }

        setPurity(input.read("Purity", Codec.INT).orElse(10));
        setFertility(input.read("Fertility", Codec.INT).orElse(5));
        setCohesive(input.read("IsCohesive", Codec.BOOL).orElse(false));
        setRebelChance(input.read("RebelChance", Codec.INT).orElse(0));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnType, @Nullable SpawnGroupData spawnGroupData) {
        EssenceType type = GrotSpawnRegistry.rollEssenceForBiome(level.getBiome(this.blockPosition()), this.getRandom());
        this.setEssenceType(type);

        float pRoll = this.getRandom().nextFloat();
        int purity;
        if (pRoll < 0.625f) purity = 1 + this.getRandom().nextInt(9);
        else if (pRoll < 0.865f) purity = 10 + this.getRandom().nextInt(9);
        else if (pRoll < 0.980f) purity = 19 + this.getRandom().nextInt(9);
        else purity = 28;
        this.setPurity(purity);

        this.setFertility(1 + this.getRandom().nextInt(10));
        this.setCohesive(this.getRandom().nextFloat() < 0.05f);

        int size = 1;
        if (this.getRandom().nextFloat() < 0.001f) {
            size = 8;
        } else {
            size = 1 << this.getRandom().nextInt(3);
        }

        this.setScaleFactor(0.75f + this.getRandom().nextFloat() * 0.5f);

        float strBase = (type == EssenceType.EARTH || type == EssenceType.DUST) ? 0.75f : 0.5f;
        float strVar = (type == EssenceType.EARTH || type == EssenceType.DUST) ? 0.75f : 1.0f;
        this.setStrengthFactor(strBase + this.getRandom().nextFloat() * strVar);

        this.setVisFactor(0.5f + this.getRandom().nextFloat() * 1.0f);
        this.setSpeedFactor(0.8f + this.getRandom().nextFloat() * 0.4f);

        float waterBase = (type == EssenceType.WATER || type == EssenceType.FLOW) ? 0.85f : 0.65f;
        float waterVar = (type == EssenceType.WATER || type == EssenceType.FLOW) ? 0.5f : 0.7f;
        this.setWaterSpeedFactor(waterBase + this.getRandom().nextFloat() * waterVar);

        this.setIntelligence(1 + this.getRandom().nextInt(20));
        this.setFriendliness(5 + this.getRandom().nextInt(11));
        this.setCanRanged(this.getRandom().nextFloat() < 0.10f);

        this.setSize(size, true);
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    public float mutate(float val, float min, float max) {
        float mutated = val + (this.getRandom().nextFloat() * 0.1f - 0.05f);
        return Mth.clamp(mutated, min, max);
    }

    @Override
    public void remove(RemovalReason reason) {
        int currentSize = this.getSize();

        if (reason == RemovalReason.KILLED && !this.isSterile() && currentSize > 1 && !this.level().isClientSide()) {
            int count = 2 + this.getRandom().nextInt(3);
            for (int i = 0; i < count; i++) {
                float fX = ((float)(i % 2) - 0.5F) * (float)currentSize / 4.0F;
                float fZ = ((float)(i / 2) - 0.5F) * (float)currentSize / 4.0F;

                GrotEntity child = (GrotEntity) this.getType().create(this.level(), EntitySpawnReason.TRIGGERED);
                if (child != null) {
                    child.setEssenceType(this.getEssenceType());
                    child.setFromSplit(true);

                    child.setCanRanged(this.canRanged());
                    child.setScaleFactor(this.getScaleFactor());
                    child.setStrengthFactor(this.getStrengthFactor());
                    child.setVisFactor(this.getVisFactor());
                    child.setSpeedFactor(this.getSpeedFactor());
                    child.setWaterSpeedFactor(this.getWaterSpeedFactor());
                    child.setIntelligence(this.getIntelligence());
                    child.setFriendliness(this.getFriendliness());
                    child.setTamed(this.isTamed());
                    child.setRideable(this.isRideable());
                    child.setPurity(this.getPurity());
                    child.setFertility(this.getFertility());
                    child.setCohesive(this.isCohesive());
                    child.setRebelChance(this.getRebelChance());
                    this.getOwnerUUID().ifPresent(child::setOwnerUUID);

                    child.setSize(currentSize / 2, true);
                    child.setPos(this.getX() + (double)fX, this.getY() + 0.5D, this.getZ() + (double)fZ);
                    child.setYRot(this.getRandom().nextFloat() * 360.0F);
                    this.level().addFreshEntity(child);
                }
            }
        }

        super.remove(reason);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);

        int purity = this.getPurity();
        Item essenceItem;
        int amount = 1 + this.getRandom().nextInt(3);

        if (purity == 28) {
            essenceItem = ModItems.STRONG_ESSENCE.get();
            amount = 2;
        } else if (purity >= 19) {
            essenceItem = ModItems.STRONG_ESSENCE.get();
        } else if (purity >= 10) {
            essenceItem = ModItems.AVERAGE_ESSENCE.get();
        } else {
            essenceItem = ModItems.WEAK_ESSENCE.get();
        }

        for (int i = 0; i < amount; i++) {
            ItemStack essenceDrop = new ItemStack(essenceItem);
            EssenceItem.setEssenceType(essenceDrop, this.getEssenceType());
            this.spawnAtLocation((ServerLevel) this.level(), essenceDrop);
        }

        if (this.getSize() == 1) {
            int slimeballAmount = this.getRandom().nextInt(3);
            for (int i = 0; i < slimeballAmount; i++) {
                this.spawnAtLocation((ServerLevel) this.level(), new ItemStack(Items.SLIME_BALL));
            }
        }
    }

    @Override
    protected void dealDamage(LivingEntity target) {
        if (this.isAlive() && this.level() instanceof ServerLevel sl) {
            double reach = (this.getBbWidth() / 2.0) + (target.getBbWidth() / 2.0) + 0.2;
            if (this.distanceToSqr(target) < reach * reach && this.hasLineOfSight(target)) {
                boolean hit = this.doHurtTarget(sl, target);
                if (hit) {
                    this.playSound(SoundEvents.SLIME_ATTACK, 1.0F, (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.2F + 1.0F);
                }
            }
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        float physDamage = 2.0F * this.getStrengthFactor();
        boolean hit = target.hurtServer(level, this.damageSources().mobAttack(this), physDamage);

        if (hit) {
            target.invulnerableTime = 0;

            float magicDamage = (2.0F * this.getSize() * this.getVisFactor()) + (0.04F * this.getPurity());
            target.hurtServer(level, this.damageSources().magic(), magicDamage);

            if (target instanceof LivingEntity living) {
                if (this.getRandom().nextFloat() < 0.25F) {
                    living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1));
                    living.addEffect(new MobEffectInstance(ModEffects.MATERIA_TOXICITY, 200, 0));
                    ModAttachments.setToxicitySource(living, this.getEssenceType().name());
                }

                if (this.getEssenceType() == EssenceType.LIGHTNING || this.getEssenceType() == EssenceType.STATIC) {
                    if (this.getRandom().nextFloat() < 0.20f) {
                        living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 255));
                    }
                }

                if (this.getEssenceType() == EssenceType.UMBRAL || this.getEssenceType() == EssenceType.UNDEAD) {
                    living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                }

                if (this.getEssenceType() == EssenceType.FROZEN || this.getEssenceType() == EssenceType.GLACIAL || this.getEssenceType() == EssenceType.TAIGA) {
                    living.setTicksFrozen(living.getTicksFrozen() + 100);
                }

                if (this.getEssenceType() == EssenceType.NETHER || this.getEssenceType() == EssenceType.MAGMA || this.getEssenceType() == EssenceType.PYRE) {
                    living.igniteForSeconds(4);
                }
            }

            if (this.getEssenceType() == EssenceType.VOID || this.getEssenceType() == EssenceType.ASTRAL) {
                this.randomTeleport(this.getX() + (this.getRandom().nextDouble() - 0.5) * 16, this.getY() + (this.getRandom().nextInt(8) - 4), this.getZ() + (this.getRandom().nextDouble() - 0.5) * 16, true);
            }
        }
        return hit;
    }

    @Override
    public boolean fireImmune() {
        EssenceType t = getEssenceType();
        return t == EssenceType.NETHER || t == EssenceType.MAGMA || t == EssenceType.PYRE || super.fireImmune();
    }

    @Override
    public void tick() {
        super.tick();

        if (this.healTime > 0) {
            this.healTime--;
        }

        if (this.loveCooldown > 0) {
            this.loveCooldown--;
        }

        if (this.inLove > 0) {
            this.inLove--;
        }

        if (this.tagCooldown > 0) {
            this.tagCooldown--;
        }

        // Safely perform queued happy jumps without overriding horizontal movement
        if (this.happyJumps > 0 && this.onGround() && this.lastTickOnGround) {
            this.setDeltaMovement(0.0D, 0.4D, 0.0D);
            this.hasImpulse = true;
            this.playSound(SoundEvents.SLIME_JUMP, 1.0F, 1.5F);
            this.happyJumps--;
            this.lastTickOnGround = false; // Prevents triggering multiple jumps instantly
        }

        EssenceType type = this.getEssenceType();

        // --- PUDDLE AMBUSH INVISIBILITY ---
        if (this.getIntelligence() >= 10 && (type == EssenceType.WATER || type == EssenceType.VAPOR || type == EssenceType.FLOW)) {
            if (this.isInWater() && this.getTarget() != null) {
                if (this.distanceToSqr(this.getTarget()) > 9.0D) {
                    this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, false, false));
                } else {
                    this.removeEffect(MobEffects.INVISIBILITY);
                    this.addEffect(new MobEffectInstance(MobEffects.SPEED, 60, 1, false, false));
                }
            }
        }

        float baseSpeed = 0.2F * this.getSpeedFactor();
        if (this.isFlightCapable()) {
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(baseSpeed * 1.2F);
        } else {
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(baseSpeed);
        }

        if (this.getDeltaMovement().y < 0 && !this.onGround() && this.isFlightCapable()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6, 1.0));
            this.resetFallDistance();
        }

        if (this.isInWater() && (type == EssenceType.WATER || type == EssenceType.FLOW)) {
            float waterMult = 1.1F * this.getWaterSpeedFactor();
            this.setDeltaMovement(this.getDeltaMovement().multiply(waterMult, waterMult, waterMult));
            this.resetFallDistance();
        }

        boolean isDay = !this.level().dimensionType().hasFixedTime() && (this.level().getDayTime() % 24000L < 12000L);
        if (type == EssenceType.NATURE || type == EssenceType.SPORE || type == EssenceType.OVERGROWTH) {
            if (this.tickCount % 40 == 0 && isDay && this.level().canSeeSky(this.blockPosition())) {
                this.heal(1.0f);
            }
        }

        if (!this.level().isClientSide() && (type == EssenceType.VOID || type == EssenceType.NULL_R || type == EssenceType.NULL_U)) {
            if (this.getRandom().nextFloat() < 0.002f) {
                this.randomTeleport(this.getX() + (this.getRandom().nextDouble() - 0.5) * 8, this.getY() + (this.getRandom().nextInt(4) - 2), this.getZ() + (this.getRandom().nextDouble() - 0.5) * 8, true);
            }
        }

        if (!this.level().isClientSide() && this.tickCount % 100 == 0) {
            if (this.getFriendliness() < 255) {
                int hayCount = 0;
                BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();
                for(int x = -1; x <= 1; x++) {
                    for(int y = -1; y <= 1; y++) {
                        for(int z = -1; z <= 1; z++) {
                            if (this.level().getBlockState(mPos.set(this.getX() + x, this.getY() + y, this.getZ() + z)).is(Blocks.HAY_BLOCK)) {
                                hayCount++;
                            }
                        }
                    }
                }
                if (hayCount >= 9 && this.getRandom().nextFloat() < 0.2f) {
                    this.setFriendliness(this.getFriendliness() + 1);
                }
            }

            if (this.getIntelligence() < 20) {
                List<Player> nearby = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(5.0));
                boolean hasMagic = false;
                for (Player p : nearby) {
                    if (p.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).isEnchanted() ||
                            p.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).isEnchanted() ||
                            p.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS).isEnchanted() ||
                            p.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET).isEnchanted()) {
                        hasMagic = true;
                    }
                    if (p.getMainHandItem().getItem() instanceof ddraig.net.entropica.item.DynamicVisWeaponItem) hasMagic = true;
                }
                if (hasMagic && this.getRandom().nextFloat() < 0.05f) {
                    this.setIntelligence(this.getIntelligence() + 1);
                }
            }

            if (this.getFriendliness() > 100 && !this.isRideable()) {
                this.setRideable(true);
            }
        }

        if (!this.level().isClientSide() && this.tickCount % 5 == 0) {
            int radius = Math.min(8, 1 + this.getSize());
            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

            if (type == EssenceType.FROZEN || type == EssenceType.GLACIAL || type == EssenceType.TAIGA) {
                if (this.onGround()) {
                    BlockPos pos = this.blockPosition();
                    if (this.level().getBlockState(pos).isAir() && Blocks.SNOW.defaultBlockState().canSurvive(this.level(), pos)) {
                        this.level().setBlockAndUpdate(pos, Blocks.SNOW.defaultBlockState());
                    }
                }
                for(BlockPos offsetPos : BlockPos.betweenClosed(this.blockPosition().offset(-radius, -1, -radius), this.blockPosition().offset(radius, -1, radius))) {
                    if (offsetPos.closerToCenterThan(this.position(), radius)) {
                        mutablePos.set(offsetPos.getX(), offsetPos.getY() + 1, offsetPos.getZ());
                        if (this.level().getBlockState(mutablePos).isAir()) {
                            BlockState waterState = this.level().getBlockState(offsetPos);
                            if (waterState.is(Blocks.WATER) && waterState.getFluidState().isSource()) {
                                this.level().setBlockAndUpdate(offsetPos, Blocks.FROSTED_ICE.defaultBlockState());
                                this.level().scheduleTick(offsetPos, Blocks.FROSTED_ICE, Mth.nextInt(this.getRandom(), 60, 120));
                            }
                        }
                    }
                }
            }
            else if (type == EssenceType.NETHER || type == EssenceType.MAGMA || type == EssenceType.PYRE) {
                for(BlockPos offsetPos : BlockPos.betweenClosed(this.blockPosition().offset(-radius, -1, -radius), this.blockPosition().offset(radius, 1, radius))) {
                    if (offsetPos.closerToCenterThan(this.position(), radius)) {
                        BlockState state = this.level().getBlockState(offsetPos);
                        if (state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.ICE) || state.is(Blocks.FROSTED_ICE)) {
                            if (this.getRandom().nextInt(10) == 0) {
                                if (state.is(Blocks.ICE) || state.is(Blocks.FROSTED_ICE)) {
                                    this.level().setBlockAndUpdate(offsetPos, Blocks.WATER.defaultBlockState());
                                } else {
                                    this.level().removeBlock(offsetPos, false);
                                }
                            }
                        }
                    }
                }
            }
            else if (type == EssenceType.EARTH || type == EssenceType.DUST) {
                if (this.onGround() && this.getRandom().nextFloat() < 0.05f) {
                    BlockPos pos = this.blockPosition().below();
                    BlockState state = this.level().getBlockState(pos);
                    if (state.is(Blocks.COBBLESTONE)) {
                        this.level().setBlockAndUpdate(pos, Blocks.GRAVEL.defaultBlockState());
                    } else if (state.is(Blocks.GRAVEL)) {
                        this.level().setBlockAndUpdate(pos, Blocks.SAND.defaultBlockState());
                    }
                }
            }
        }

        if (this.onGround() && !this.lastTickOnGround) {
            BlockPos landingPos = this.blockPosition().below();
            BlockState landingState = this.level().getBlockState(landingPos);

            if (!this.level().isClientSide()) {
                if ((type == EssenceType.NATURE || type == EssenceType.SPORE) && landingState.is(Blocks.DIRT) && this.getRandom().nextFloat() < 0.1f) {
                    this.level().setBlockAndUpdate(landingPos, Blocks.GRASS_BLOCK.defaultBlockState());
                } else if ((type == EssenceType.EARTH || type == EssenceType.DUST) && this.getRandom().nextFloat() < 0.05f) {
                    if (landingState.is(Blocks.COBBLESTONE)) {
                        this.level().setBlockAndUpdate(landingPos, Blocks.GRAVEL.defaultBlockState());
                    } else if (landingState.is(Blocks.GRAVEL)) {
                        this.level().setBlockAndUpdate(landingPos, Blocks.SAND.defaultBlockState());
                    }
                }
            }
        }

        if (this.level().isClientSide() && this.getRandom().nextFloat() < 0.2f) {
            int color = type.getColorInt();

            if (type == EssenceType.VOID || type == EssenceType.ASTRAL) {
                this.level().addParticle(ParticleTypes.PORTAL, this.getRandomX(0.5D), this.getRandomY() - 0.25D, this.getRandomZ(0.5D), (this.getRandom().nextDouble() - 0.5D) * 2.0D, -this.getRandom().nextDouble(), (this.getRandom().nextDouble() - 0.5D) * 2.0D);
            } else if (type == EssenceType.LIGHTNING || type == EssenceType.STATIC) {
                this.level().addParticle(ParticleTypes.ELECTRIC_SPARK, this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), 0, 0, 0);
            } else if (type == EssenceType.NETHER || type == EssenceType.MAGMA) {
                this.level().addParticle(ParticleTypes.FLAME, this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), 0, 0.05, 0);
            } else {
                this.level().addParticle(new DustParticleOptions(color, 1.0f), this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), 0, 0, 0);
            }
        }

        handleFusionLogic();
        this.lastTickOnGround = this.onGround();
    }

    @Override
    public void makeStuckInBlock(BlockState state, Vec3 motionMultiplier) {
        EssenceType type = this.getEssenceType();
        if (state.is(BlockTags.LEAVES) && (type == EssenceType.NATURE || type == EssenceType.EARTH || type == EssenceType.OVERGROWTH || type == EssenceType.SPORE || type == EssenceType.DUST)) {
            return;
        }
        super.makeStuckInBlock(state, motionMultiplier);
    }

    private void handleFusionLogic() {
        if (this.level().isClientSide() || !this.isAlive() || this.getSize() >= 8) return;

        if (this.isFusing()) {
            Entity rawPartner = this.level().getEntity(this.getFusionTarget());

            if (!(rawPartner instanceof GrotEntity partner) || !partner.isAlive()) {
                this.setFusing(false);
                this.fusionTicks = 0;
                this.contactTicks = 0;
                return;
            }

            double reach = (this.getBbWidth() / 2.0) + (partner.getBbWidth() / 2.0) + 1.0;
            if (this.distanceToSqr(partner) > reach * reach) {
                this.setFusing(false);
                this.fusionTicks = 0;
                this.contactTicks = 0;
                return;
            }

            this.fusionTicks++;

            Vec3 pull = partner.position().subtract(this.position()).normalize().scale(0.05);
            this.setDeltaMovement(pull);

            if (this.fusionTicks % 5 == 0) {
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + this.getBbHeight()/2f, this.getZ(), 5, 0.2, 0.2, 0.2, 0.1);
                }
                this.playSound(SoundEvents.MINECART_RIDING, 0.5f, 1.0f + (this.fusionTicks / 30.0f));
            }

            if (this.fusionTicks >= 30) {
                executeFusion();
            }
            return;
        }

        // Breeding is not agreeing to merge, and taming prevents merges (unless rebelling, where isTamed is false)
        if (this.inLove > 0 || this.isTamed()) {
            this.contactTicks = Math.max(0, this.contactTicks - 2);
            return;
        }

        AABB scanBox = this.getBoundingBox().inflate(0.2);
        List<GrotEntity> others = this.level().getEntitiesOfClass(GrotEntity.class, scanBox, e ->
                e != this && e.isAlive() && !e.isFusing() && e.getSize() == this.getSize() && e.inLove <= 0 && !e.isTamed()
        );

        if (!others.isEmpty()) {
            GrotEntity partner = others.get(0);

            // Merging should only happen if at least one is in combat
            boolean eitherInCombat = this.getTarget() != null || partner.getTarget() != null;
            if (!eitherInCombat) {
                this.contactTicks = Math.max(0, this.contactTicks - 2);
                return;
            }

            this.contactTicks++;

            // They "agree" to merge by maintaining contact for 100 ticks (removed random 4% instant-fusion chance to stop accidents)
            if (this.contactTicks >= 100) {
                this.setFusing(true);
                partner.setFusing(true);
                this.setFusionTarget(partner.getId());
                partner.setFusionTarget(this.getId());
                this.contactTicks = 0;
                partner.contactTicks = 0;
            }
        } else {
            this.contactTicks = Math.max(0, this.contactTicks - 2);
        }
    }

    private void executeFusion() {
        Entity rawPartner = this.level().getEntity(this.getFusionTarget());
        if (!(rawPartner instanceof GrotEntity partner) || !partner.isAlive()) {
            this.setFusing(false);
            this.fusionTicks = 0;
            return;
        }

        if (this.getUUID().compareTo(partner.getUUID()) > 0) {
            return;
        }

        EssenceType t1 = this.getEssenceType();
        EssenceType t2 = partner.getEssenceType();

        EssenceType resultType = (t1 == t2) ? t1 : GrotFusionHelper.getFusion(t1, t2);

        boolean inheritedSterility = this.isFromSplit() || partner.isFromSplit() || this.isSterile() || partner.isSterile();
        boolean inheritedRanged = this.canRanged() || partner.canRanged();

        if (this.isCohesive() || partner.isCohesive()) {
            inheritedSterility = false;
        }

        if (resultType != null) {
            this.setEssenceType(resultType);
            this.setSize(this.getSize() * 2, true);
            this.heal(this.getMaxHealth());
            this.setCanRanged(inheritedRanged);

            if (resultType != t1 && resultType != t2) {
                this.setSterile(false);
            } else {
                this.setSterile(inheritedSterility);
            }

            this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.HOSTILE, 1.0f, 0.5f);
        } else {
            if (this.getRandom().nextBoolean()) {
                this.level().explode(this, null, null, this.getX(), this.getY(), this.getZ(), 2.0f, false, Level.ExplosionInteraction.NONE);
                this.discard();
                partner.discard();
                return;
            } else {
                this.setEssenceType(this.getRandom().nextBoolean() ? t1 : t2);
                this.setSize(this.getSize() * 2, true);
                this.heal(this.getMaxHealth());
                this.setCanRanged(inheritedRanged);
                this.setSterile(inheritedSterility);
                this.level().playSound(null, this.blockPosition(), SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 1.0f, 0.5f);
            }
        }

        partner.discard();
        this.setFusing(false);
        this.fusionTicks = 0;
    }
}