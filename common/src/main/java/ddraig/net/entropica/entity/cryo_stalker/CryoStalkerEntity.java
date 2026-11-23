package ddraig.net.entropica.entity.cryo_stalker;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.item.biome3.CryoInsulatedArmorItem;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Fauna 2 — Cryo-Stalker (Frost-Veined Prowler).
 * Hostile apex ambush predator native to 2.3 Frost-Bite Floes and polar ice packs:
 * - 90% Snow/Ice Camouflage (IS_STALKING + translucent icy tint) when on or adjacent to snow/ice in low block light (<= 10)
 * - Fire & Bright-Light Weakness (> 10 block light or on fire): loses stealth and suffers -30% movement speed
 * - 8-block Pounce Leap, 4-second Freezing Bite hypothermia, and Rear Tail-Swipe Reflex when struck from behind
 */
public class CryoStalkerEntity extends Monster {

    public static final float STEALTH_ALPHA = 0.14F;

    private static final EntityDataAccessor<Boolean> IS_STALKING =
            SynchedEntityData.defineId(CryoStalkerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_ATTACKING =
            SynchedEntityData.defineId(CryoStalkerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_LEAPING =
            SynchedEntityData.defineId(CryoStalkerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_SWIPING =
            SynchedEntityData.defineId(CryoStalkerEntity.class, EntityDataSerializers.BOOLEAN);

    private static final byte EVENT_TAIL_SWIPE = 4;

    private static final ResourceLocation LIGHT_FIRE_WEAKNESS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "cryo_stalker_light_fire_weakness");
    private static final AttributeModifier LIGHT_FIRE_WEAKNESS_MODIFIER = new AttributeModifier(
            LIGHT_FIRE_WEAKNESS_ID,
            -0.30D,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    );

    private static final ResourceLocation GLIMMER_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "glimmer_ice");
    private static final ResourceLocation ROTTEN_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "rotten_ice");
    private static final ResourceLocation BLIGHTED_PACKED_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "blighted_packed_ice");
    private static final ResourceLocation RUNIC_BLUE_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "runic_blue_ice");
    private static final ResourceLocation COARSE_GRAVEL_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "coarse_gravel");
    private static final ResourceLocation NECROTIC_SLIME_PATCH_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "necrotic_slime_patch");
    private static final ResourceLocation AURORAL_FEATHER_MOSS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "auroral_feather_moss");

    private static final DustParticleOptions FROST_VEIN_DUST = new DustParticleOptions(0x00F0FF, 0.55F);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState runAnimationState = new AnimationState();
    public final AnimationState jumpAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState swipeAnimationState = new AnimationState();
    public final AnimationState biteAnimationState = new AnimationState();

    public int leapCooldown = 0;
    public int tailSwipeCooldown = 0;
    private int attackAnimTimer = 0;
    private int tailSwipeTimer = 0;

    private float prevStealthAlpha = 1.0F;
    private float currentStealthAlpha = 1.0F;

    public CryoStalkerEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(PathType.POWDER_SNOW, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_POWDER_SNOW, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 48.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    private static boolean isRegisteredBlock(BlockState state, ResourceLocation id) {
        if (BuiltInRegistries.BLOCK.containsKey(id)) {
            Block block = BuiltInRegistries.BLOCK.getValue(id);
            return block != Blocks.AIR && state.is(block);
        }
        return false;
    }

    public static boolean isSnowOrIceCamouflageBlock(BlockState state) {
        if (state.isAir()) {
            return false;
        }
        return state.is(Blocks.SNOW)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.POWDER_SNOW)
                || state.is(Blocks.ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.FROSTED_ICE)
                || state.is(BlockTags.SNOW)
                || state.is(BlockTags.ICE)
                || isRegisteredBlock(state, GLIMMER_ICE_ID)
                || isRegisteredBlock(state, ROTTEN_ICE_ID)
                || isRegisteredBlock(state, BLIGHTED_PACKED_ICE_ID)
                || isRegisteredBlock(state, RUNIC_BLUE_ICE_ID);
    }

    public static boolean isCryoStalkerSpawnSurface(BlockGetter level, BlockPos belowPos, BlockState belowState) {
        return belowState.is(Blocks.ICE)
                || belowState.is(Blocks.PACKED_ICE)
                || belowState.is(Blocks.BLUE_ICE)
                || belowState.is(Blocks.FROSTED_ICE)
                || belowState.is(BlockTags.ICE)
                || isRegisteredBlock(belowState, ROTTEN_ICE_ID)
                || isRegisteredBlock(belowState, BLIGHTED_PACKED_ICE_ID)
                || isRegisteredBlock(belowState, GLIMMER_ICE_ID)
                || isRegisteredBlock(belowState, RUNIC_BLUE_ICE_ID)
                || belowState.is(Blocks.SNOW_BLOCK)
                || belowState.is(Blocks.SNOW)
                || belowState.is(Blocks.GRAVEL)
                || isRegisteredBlock(belowState, COARSE_GRAVEL_ID)
                || belowState.is(Blocks.GRASS_BLOCK)
                || belowState.is(BlockTags.ANIMALS_SPAWNABLE_ON)
                || belowState.is(BlockTags.BASE_STONE_OVERWORLD)
                || (belowState.getFluidState().isEmpty() && belowState.isFaceSturdy(level, belowPos, Direction.UP));
    }

    public static boolean checkCryoStalkerSpawnRules(
            EntityType<CryoStalkerEntity> type,
            ServerLevelAccessor level,
            EntitySpawnReason reason,
            BlockPos pos,
            RandomSource random
    ) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        if (EntitySpawnReason.isSpawner(reason)
                || reason == EntitySpawnReason.SPAWN_ITEM_USE
                || reason == EntitySpawnReason.COMMAND) {
            return true;
        }

        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
        if (blockLight > 10) {
            return false;
        }

        boolean darkOrNight = Monster.isDarkEnoughToSpawn(level, pos, random)
                || ((level.getLevel().isDarkOutside() || level.getLevel().isThundering())
                && level.getBrightness(LightLayer.SKY, pos) <= random.nextInt(32));
        if (!darkOrNight) {
            return false;
        }

        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        if ((below.is(Blocks.SNOW)
                || isRegisteredBlock(below, NECROTIC_SLIME_PATCH_ID)
                || isRegisteredBlock(below, AURORAL_FEATHER_MOSS_ID))
                && level.getBlockState(belowPos.below()).blocksMotion()) {
            belowPos = belowPos.below();
            below = level.getBlockState(belowPos);
        }

        return isCryoStalkerSpawnSurface(level, belowPos, below);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_STALKING, false);
        builder.define(IS_ATTACKING, false);
        builder.define(IS_LEAPING, false);
        builder.define(IS_SWIPING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new CryoStalkerLeapGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25D, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean isStalking() {
        return this.entityData.get(IS_STALKING);
    }

    public void setStalking(boolean stalking) {
        this.entityData.set(IS_STALKING, stalking);
    }

    public boolean isAttackingAnim() {
        return this.entityData.get(IS_ATTACKING);
    }

    public void setAttackingAnim(boolean attacking) {
        this.entityData.set(IS_ATTACKING, attacking);
    }

    public boolean isLeaping() {
        return this.entityData.get(IS_LEAPING);
    }

    public void setLeaping(boolean leaping) {
        this.entityData.set(IS_LEAPING, leaping);
    }

    public boolean isSwiping() {
        return this.entityData.get(IS_SWIPING);
    }

    public void setSwiping(boolean swiping) {
        this.entityData.set(IS_SWIPING, swiping);
    }

    public float getStealthAlpha(float partialTick) {
        return Mth.lerp(partialTick, this.prevStealthAlpha, this.currentStealthAlpha);
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    public boolean isSteppingCarefully() {
        return this.isStalking() || super.isSteppingCarefully();
    }

    @Override
    public void makeStuckInBlock(BlockState state, Vec3 motionMultiplier) {
        if (state.is(Blocks.POWDER_SNOW) || state.is(Blocks.SNOW) || state.getBlock() instanceof SnowLayerBlock) {
            return;
        }
        super.makeStuckInBlock(state, motionMultiplier);
    }

    @Override
    public double getVisibilityPercent(@Nullable Entity lookingEntity) {
        double base = super.getVisibilityPercent(lookingEntity);
        return this.isStalking() ? base * 0.10D : base;
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        if (level.getBrightness(LightLayer.BLOCK, pos) > 10) {
            return -10.0F;
        }
        if (isSnowOrIceCamouflageBlock(level.getBlockState(pos.below()))
                || isSnowOrIceCamouflageBlock(level.getBlockState(pos))) {
            return 10.0F;
        }
        return super.getWalkTargetValue(pos, level);
    }

    public boolean isStandingOnOrAdjacentToSnowOrIce() {
        Level level = this.level();
        BlockPos center = this.blockPosition();
        if (isSnowOrIceCamouflageBlock(level.getBlockState(center))) {
            return true;
        }
        BlockPos onPos = this.getOnPos();
        if (!onPos.equals(center) && isSnowOrIceCamouflageBlock(level.getBlockState(onPos))) {
            return true;
        }
        for (Direction dir : Direction.values()) {
            if (isSnowOrIceCamouflageBlock(level.getBlockState(center.relative(dir)))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean alreadyChilled = target instanceof LivingEntity preCheck
                && (preCheck.getTicksFrozen() > 0 || preCheck.hasEffect(MobEffects.SLOWNESS));

        boolean success = super.doHurtTarget(level, target);
        if (success && target instanceof LivingEntity living) {
            this.setAttackingAnim(true);
            this.attackAnimTimer = 15;
            this.setStalking(false);

            if (!CryoInsulatedArmorItem.hasFullCryoInsulation(living)) {
                living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1), this);
                living.setTicksFrozen(Math.min(living.getTicksRequiredToFreeze() + 100, living.getTicksFrozen() + 100));

                if (alreadyChilled && living.isAlive()) {
                    int prevInvulnerable = living.invulnerableTime;
                    living.invulnerableTime = 0;
                    living.hurtServer(level, this.damageSources().freeze(), 1.0F);
                    living.invulnerableTime = prevInvulnerable;
                }

                level.playSound(
                        null,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        SoundEvents.PLAYER_HURT_FREEZE,
                        SoundSource.HOSTILE,
                        1.0F,
                        0.9F
                );
                level.sendParticles(
                        ParticleTypes.SNOWFLAKE,
                        living.getX(),
                        living.getY() + living.getBbHeight() * 0.5D,
                        living.getZ(),
                        12,
                        0.3D,
                        0.35D,
                        0.3D,
                        0.04D
                );
            }
        }
        return success;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && this.isAlive() && this.tailSwipeCooldown == 0) {
            Entity srcEntity = source.getEntity();
            if (!(srcEntity instanceof LivingEntity) && source.getDirectEntity() instanceof LivingEntity directLiving) {
                srcEntity = directLiving;
            }

            if (srcEntity instanceof LivingEntity attacker
                    && attacker != this
                    && attacker.isAlive()
                    && !(attacker instanceof CryoStalkerEntity)
                    && this.distanceToSqr(attacker) <= 3.5D * 3.5D) {
                Vec3 diff = attacker.position().subtract(this.position());
                if (diff.lengthSqr() > 1.0E-6D) {
                    Vec3 toAttacker = diff.normalize();
                    if (this.getLookAngle().dot(toAttacker) < -0.15D) {
                        triggerRearTailSwipe(level, attacker, toAttacker);
                    }
                }
            }
        }
        return hurt;
    }

    private void triggerRearTailSwipe(ServerLevel level, LivingEntity attacker, Vec3 toAttacker) {
        this.tailSwipeCooldown = 40;
        this.tailSwipeTimer = 15;
        this.setSwiping(true);
        this.setStalking(false);
        level.broadcastEntityEvent(this, EVENT_TAIL_SWIPE);

        boolean fullInsulation = CryoInsulatedArmorItem.hasFullCryoInsulation(attacker);
        int prevInvuln = attacker.invulnerableTime;
        attacker.invulnerableTime = 0;
        if (fullInsulation || !attacker.hurtServer(level, this.damageSources().freeze(), 4.0F)) {
            attacker.hurtServer(level, this.damageSources().mobAttack(this), fullInsulation ? 2.5F : 4.0F);
        }
        attacker.invulnerableTime = prevInvuln;

        double dx = this.getX() - attacker.getX();
        double dz = this.getZ() - attacker.getZ();
        if (dx * dx + dz * dz < 1.0E-4D) {
            dx = -toAttacker.x;
            dz = -toAttacker.z;
        }
        attacker.knockback(0.75D, dx, dz);
        attacker.hurtMarked = true;

        if (!fullInsulation) {
            attacker.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 0), this);
            attacker.setTicksFrozen(Math.min(attacker.getTicksRequiredToFreeze() + 60, attacker.getTicksFrozen() + 60));
        }

        level.playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP,
                SoundSource.HOSTILE,
                1.0F,
                0.85F
        );
        level.playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.GLASS_HIT,
                SoundSource.HOSTILE,
                0.85F,
                1.25F
        );
        level.sendParticles(
                ParticleTypes.SNOWFLAKE,
                attacker.getX(),
                attacker.getY() + attacker.getBbHeight() * 0.5D,
                attacker.getZ(),
                16,
                0.45D,
                0.35D,
                0.45D,
                0.08D
        );
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_TAIL_SWIPE) {
            this.swipeAnimationState.start(this.tickCount);
            for (int i = 0; i < 14; i++) {
                double angle = (Math.PI * 2.0D * i) / 14.0D;
                this.level().addParticle(
                        ParticleTypes.SNOWFLAKE,
                        this.getX() + Math.cos(angle) * 0.8D,
                        this.getY() + 0.55D,
                        this.getZ() + Math.sin(angle) * 0.8D,
                        Math.cos(angle) * 0.16D,
                        0.02D,
                        Math.sin(angle) * 0.16D
                );
            }
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.leapCooldown > 0) {
            this.leapCooldown--;
        }

        if (this.tailSwipeCooldown > 0) {
            this.tailSwipeCooldown--;
        }

        if (this.attackAnimTimer > 0) {
            this.attackAnimTimer--;
            if (this.attackAnimTimer == 0) {
                this.setAttackingAnim(false);
            }
        }

        if (this.tailSwipeTimer > 0) {
            this.tailSwipeTimer--;
            if (this.tailSwipeTimer == 0) {
                this.setSwiping(false);
            }
        }

        if (!this.level().isClientSide()) {
            int blockLight = this.level().getBrightness(LightLayer.BLOCK, this.blockPosition());
            boolean exposedToFireOrLight = this.isOnFire() || blockLight > 10;

            AttributeInstance speedAttr = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (exposedToFireOrLight) {
                this.setStalking(false);
                if (speedAttr != null && !speedAttr.hasModifier(LIGHT_FIRE_WEAKNESS_ID)) {
                    speedAttr.addTransientModifier(LIGHT_FIRE_WEAKNESS_MODIFIER);
                }
            } else {
                if (speedAttr != null && speedAttr.hasModifier(LIGHT_FIRE_WEAKNESS_ID)) {
                    speedAttr.removeModifier(LIGHT_FIRE_WEAKNESS_ID);
                }
                boolean shouldStalk = this.attackAnimTimer == 0
                        && this.tailSwipeTimer == 0
                        && this.isStandingOnOrAdjacentToSnowOrIce();
                this.setStalking(shouldStalk);
            }
        } else {
            float targetAlpha = this.isStalking() ? STEALTH_ALPHA : 1.0F;
            if (this.tickCount <= 1) {
                this.prevStealthAlpha = targetAlpha;
                this.currentStealthAlpha = targetAlpha;
            } else {
                this.prevStealthAlpha = this.currentStealthAlpha;
                this.currentStealthAlpha = Mth.lerp(0.28F, this.currentStealthAlpha, targetAlpha);
                if (Math.abs(this.currentStealthAlpha - targetAlpha) < 0.004F) {
                    this.currentStealthAlpha = targetAlpha;
                }
            }

            if (this.isStalking()) {
                // Faint glowing cyan frost-vein motes while 90% camouflaged
                if (this.random.nextInt(6) == 0) {
                    double px = this.getX() + (this.random.nextDouble() - 0.5D) * 0.75D;
                    double py = this.getY() + 0.25D + this.random.nextDouble() * 0.65D;
                    double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * 0.75D;
                    this.level().addParticle(FROST_VEIN_DUST, px, py, pz, 0.0D, 0.005D, 0.0D);
                }
            } else if (this.random.nextInt(3) == 0) {
                double px = this.getX() + (this.random.nextDouble() - 0.5D) * 0.8D;
                double py = this.getY() + 0.3D + this.random.nextDouble() * 0.6D;
                double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * 0.8D;
                this.level().addParticle(ParticleTypes.SNOWFLAKE, px, py, pz, 0.0D, -0.02D, 0.0D);
            }

            this.setupAnimationStates();
        }
    }

    private void setupAnimationStates() {
        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
        boolean sprinting = this.isSprinting() || (moving && this.getTarget() != null);

        if (this.isLeaping()) {
            this.jumpAnimationState.startIfStopped(this.tickCount);
        } else {
            this.jumpAnimationState.stop();
        }

        if (this.isAttackingAnim()) {
            this.attackAnimationState.startIfStopped(this.tickCount);
            this.biteAnimationState.startIfStopped(this.tickCount);
        } else {
            this.attackAnimationState.stop();
            this.biteAnimationState.stop();
        }

        if (this.isSwiping()) {
            this.swipeAnimationState.startIfStopped(this.tickCount);
        } else {
            this.swipeAnimationState.stop();
        }

        if (sprinting) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.runAnimationState.startIfStopped(this.tickCount);
        } else if (moving) {
            this.idleAnimationState.stop();
            this.runAnimationState.stop();
            this.walkAnimationState.startIfStopped(this.tickCount);
        } else {
            this.walkAnimationState.stop();
            this.runAnimationState.stop();
            this.idleAnimationState.startIfStopped(this.tickCount);
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        if (ModItems.CRYO_CRYSTALLINE_FANG.isPresent()) {
            this.spawnAtLocation(level, new ItemStack(ModItems.CRYO_CRYSTALLINE_FANG.get(), 1));
        }
        if (ModItems.FROST_VEINED_HIDE.isPresent()) {
            this.spawnAtLocation(level, new ItemStack(ModItems.FROST_VEINED_HIDE.get(), 1));
        }
        ItemStack glacialEssence = new ItemStack(ModItems.AVERAGE_ESSENCE.get(), 1 + this.random.nextInt(2));
        ddraig.net.entropica.item.EssenceItem.setEssenceType(glacialEssence, ddraig.net.entropica.api.EssenceType.GLACIAL);
        this.spawnAtLocation(level, glacialEssence);

        ItemStack vaporEssence = new ItemStack(ModItems.AVERAGE_ESSENCE.get(), 1 + this.random.nextInt(2));
        ddraig.net.entropica.item.EssenceItem.setEssenceType(vaporEssence, ddraig.net.entropica.api.EssenceType.VAPOR);
        this.spawnAtLocation(level, vaporEssence);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.isStalking() ? null : SoundEvents.POLAR_BEAR_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.POLAR_BEAR_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.POLAR_BEAR_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        if (!this.isStalking()) {
            this.playSound(SoundEvents.POWDER_SNOW_STEP, 0.15F, 1.0F);
        }
    }

    static class CryoStalkerLeapGoal extends Goal {
        private final CryoStalkerEntity stalker;
        private LivingEntity target;

        public CryoStalkerLeapGoal(CryoStalkerEntity stalker) {
            this.stalker = stalker;
        }

        @Override
        public boolean canUse() {
            this.target = this.stalker.getTarget();
            if (this.target == null || !this.target.isAlive() || this.stalker.leapCooldown > 0) {
                return false;
            }
            double distSq = this.stalker.distanceToSqr(this.target);
            return distSq >= 16.0D && distSq <= 64.0D && this.stalker.onGround() && this.stalker.getRandom().nextInt(5) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.stalker.onGround();
        }

        @Override
        public void start() {
            Vec3 targetPos = this.target.position();
            Vec3 stalkerPos = this.stalker.position();
            Vec3 diff = targetPos.subtract(stalkerPos).normalize().scale(1.2D);

            this.stalker.setDeltaMovement(diff.x, 0.45D, diff.z);
            this.stalker.setLeaping(true);
            this.stalker.leapCooldown = 80;
            this.stalker.level().playSound(
                    null,
                    this.stalker.getX(),
                    this.stalker.getY(),
                    this.stalker.getZ(),
                    SoundEvents.POLAR_BEAR_WARNING,
                    SoundSource.HOSTILE,
                    1.0F,
                    1.2F
            );
        }

        @Override
        public void stop() {
            this.stalker.setLeaping(false);
        }
    }
}
