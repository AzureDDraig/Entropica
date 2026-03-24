package ddraig.net.entropica.entity.grot;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.fumes.VisFumeStack;
import ddraig.net.entropica.block.VisFumePipeBlock;
import ddraig.net.entropica.block.entity.VisFumePipeBlockEntity;
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
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
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

public class GrotEntity extends Slime {

    private static final EntityDataAccessor<String> ESSENCE_TYPE = SynchedEntityData.defineId(GrotEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> IS_STERILE = SynchedEntityData.defineId(GrotEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FROM_SPLIT = SynchedEntityData.defineId(GrotEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CAN_RANGED = SynchedEntityData.defineId(GrotEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> IS_FUSING = SynchedEntityData.defineId(GrotEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> FUSION_TARGET_ID = SynchedEntityData.defineId(GrotEntity.class, EntityDataSerializers.INT);

    private int contactTicks = 0;
    private int fusionTicks = 0;
    private boolean lastTickOnGround = false;

    public int healTime = 0;

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
        float s = 0.51F * (float)Math.max(1, this.getSize());
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
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));

        this.goalSelector.addGoal(2, new Goal() {
            private Player player;
            @Override public boolean canUse() {
                if (GrotEntity.this.getHealth() < GrotEntity.this.getMaxHealth() * 0.9f) return false;
                List<Player> players = GrotEntity.this.level().getEntitiesOfClass(Player.class, GrotEntity.this.getBoundingBox().inflate(8.0D));
                if (players.isEmpty()) return false;
                player = players.get(0);
                if (player.isCreative() || player.isSpectator()) return false;
                return true;
            }
            @Override public boolean canContinueToUse() {
                return player != null && player.isAlive() && GrotEntity.this.getHealth() >= GrotEntity.this.getMaxHealth() * 0.9f;
            }
            @Override public void tick() {
                if (player != null) {
                    Vec3 dir = GrotEntity.this.position().subtract(player.position()).normalize().scale(5);
                    GrotEntity.this.getNavigation().moveTo(GrotEntity.this.getX() + dir.x, GrotEntity.this.getY() + dir.y, GrotEntity.this.getZ() + dir.z, 0.8D);
                }
            }
        });

        this.goalSelector.addGoal(3, new Goal() {
            private int jumpDelay = 0;
            private int rangedCooldown = 0;

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

                    // FIX: If Ranged capable, inside 12 blocks, and outside melee reach... plant feet and shoot!
                    if (GrotEntity.this.canRanged() && distSq < 144.0 && distSq > reach * reach) {
                        GrotEntity.this.getNavigation().stop();
                    } else {
                        // Otherwise, slide into position (either to get in range, or dive into melee)
                        GrotEntity.this.getNavigation().moveTo(target, 1.2D);
                    }

                    if (jumpDelay > 0) jumpDelay--;
                    if (rangedCooldown > 0) rangedCooldown--;

                    // RANGED ATTACK LOGIC
                    if (GrotEntity.this.canRanged() && rangedCooldown <= 0 && distSq > reach * reach && distSq < 144.0) { // Max 12 blocks
                        rangedCooldown = 60 + GrotEntity.this.getRandom().nextInt(40);
                        EssenceType t = GrotEntity.this.getEssenceType();
                        Vec3 dir = target.position().subtract(GrotEntity.this.position()).normalize();

                        if (t == EssenceType.AIR || t == EssenceType.STORM || t == EssenceType.LIGHTNING || t == EssenceType.VAPOR) {
                            target.setDeltaMovement(target.getDeltaMovement().add(dir.x * 1.5, 0.5, dir.z * 1.5));
                            if (!GrotEntity.this.level().isClientSide()) {
                                target.hurtServer((ServerLevel)GrotEntity.this.level(), GrotEntity.this.damageSources().mobAttack(GrotEntity.this), 2.0F);
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
                    if (distSq <= reach * reach) {
                        if (GrotEntity.this.level() instanceof ServerLevel serverLevel) {
                            GrotEntity.this.doHurtTarget(serverLevel, target);
                        }
                    }
                }
            }
        });

        this.goalSelector.addGoal(4, new Goal() {
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
                if (GrotEntity.this.level().getBlockState(pos).getBlock() instanceof VisFumePipeBlock) {
                    if (GrotEntity.this.level().getBlockEntity(pos) instanceof VisFumePipeBlockEntity pipe) {
                        return !pipe.getFumeInTank().isEmpty();
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
                        if (GrotEntity.this.level().getBlockEntity(targetPipe) instanceof VisFumePipeBlockEntity pipe) {
                            if (!pipe.getFumeInTank().isEmpty()) {
                                VisFumeStack drained = pipe.drain(10, false);

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

        this.goalSelector.addGoal(5, new Goal() {
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

        this.goalSelector.addGoal(6, new Goal() {
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

        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new Goal() {
            @Override public boolean canUse() {
                return GrotEntity.this.getLastHurtByMob() != null && GrotEntity.this.getTarget() == null;
            }
            @Override public void start() {
                LivingEntity attacker = GrotEntity.this.getLastHurtByMob();
                GrotEntity.this.setTarget(attacker);

                double alertRange = 20.0D;
                AABB alertBox = GrotEntity.this.getBoundingBox().inflate(alertRange);
                List<GrotEntity> friends = GrotEntity.this.level().getEntitiesOfClass(GrotEntity.class, alertBox);
                for (GrotEntity friend : friends) {
                    if (friend != GrotEntity.this && friend.getTarget() == null && friend.isAlive()) {
                        friend.setTarget(attacker);
                    }
                }
            }
        });

        this.targetSelector.addGoal(2, new Goal() {
            private Player targetPlayer;
            @Override public boolean canUse() {
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
    }

    public EssenceType getEssenceType() {
        try {
            return EssenceType.valueOf(this.entityData.get(ESSENCE_TYPE));
        } catch (IllegalArgumentException e) {
            return EssenceType.EARTH;
        }
    }

    public void setEssenceType(EssenceType type) {
        this.entityData.set(ESSENCE_TYPE, type.name());
    }

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

    @Override
    public void setSize(int size, boolean resetHealth) {
        super.setSize(size, resetHealth);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double)(size * 4));

        if (resetHealth) {
            this.setHealth(this.getMaxHealth());
        }
        this.refreshDimensions();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("EssenceType", Codec.STRING, this.entityData.get(ESSENCE_TYPE));
        output.store("IsSterile", Codec.BOOL, isSterile());
        output.store("FromSplit", Codec.BOOL, isFromSplit());
        output.store("CanRanged", Codec.BOOL, canRanged());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(ESSENCE_TYPE, input.read("EssenceType", Codec.STRING).orElse("EARTH"));
        setSterile(input.read("IsSterile", Codec.BOOL).orElse(false));
        setFromSplit(input.read("FromSplit", Codec.BOOL).orElse(false));
        setCanRanged(input.read("CanRanged", Codec.BOOL).orElse(false));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnType, @Nullable SpawnGroupData spawnGroupData) {
        EssenceType type = GrotSpawnRegistry.rollEssenceForBiome(level.getBiome(this.blockPosition()), this.getRandom());
        this.setEssenceType(type);

        int size = 1;
        if (this.getRandom().nextFloat() < 0.001f) {
            size = 8;
        } else {
            size = 1 << this.getRandom().nextInt(3);
        }
        this.setSize(size, true);

        this.setCanRanged(this.getRandom().nextFloat() < 0.10f);

        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
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
                    child.setSize(currentSize / 2, true);
                    child.setPos(this.getX() + (double)fX, this.getY() + 0.5D, this.getZ() + (double)fZ);
                    child.setYRot(this.getRandom().nextFloat() * 360.0F);
                    this.level().addFreshEntity(child);
                }
            }
        }

        this.setSize(0, false);
        super.remove(reason);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);

        int amount = 1 + this.getRandom().nextInt(3);
        for (int i = 0; i < amount; i++) {
            ItemStack essenceDrop = new ItemStack(ModItems.WEAK_ESSENCE.get());
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
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = target.hurtServer(level, this.damageSources().mobAttack(this), 2.0F);

        if (hit) {
            target.hurtServer(level, this.damageSources().magic(), 2.0F);

            if (target instanceof LivingEntity living) {
                if (this.getRandom().nextFloat() < 0.25F) {
                    living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1));
                    living.addEffect(new MobEffectInstance(ModEffects.VIS_TOXICITY, 200, 0));
                    living.setData(ModAttachments.TOXICITY_SOURCE, this.getEssenceType().name());
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

        EssenceType type = this.getEssenceType();

        float baseSpeed = 0.2F + 0.02F * (float)this.getSize();
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
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.1, 1.1, 1.1));
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

        // ==========================================
        // CONTINUOUS SERVER-SIDE AURAS
        // ==========================================
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
            this.fusionTicks++;
            this.setDeltaMovement(Vec3.ZERO);

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

        AABB scanBox = this.getBoundingBox().inflate(0.2);
        List<GrotEntity> others = this.level().getEntitiesOfClass(GrotEntity.class, scanBox, e ->
                e != this && e.isAlive() && !e.isFusing() && e.getSize() == this.getSize()
        );

        if (!others.isEmpty()) {
            GrotEntity partner = others.get(0);
            this.contactTicks++;

            if (this.contactTicks >= 100 || this.getRandom().nextFloat() < 0.04f) {
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