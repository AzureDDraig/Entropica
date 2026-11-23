package ddraig.net.entropica.entity.barrow_hound;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
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
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
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
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariant;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariants;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Fauna 25 — Barrow Hound (Crypt Stalker).
 * Hostile crypt pack stalker native to 2.8 Barrow-Mound Tundra:
 * - Tomb-Bound Strength: every 30 ticks checks within 12 blocks for tomb masonry
 *   (barrow_stone_bricks, cracked_barrow_bricks, mossy_barrow_bricks),
 *   skipping the buff if near a barrow_sentinel_totem. Grants +20% melee damage.
 * - Crypt Bite: melee attacks inflict Blindness (60t = 3s) and trigger attackAnimationState (entityEvent byte 4).
 * - Pack Howl: when spotting a target (200-tick cooldown), triggers howlAnimationState (entityEvent byte 64),
 *   plays a low-pitched howl (0.65F), and alerts nearby Barrow Hounds within 20 blocks with Speed I (100t).
 */
public class BarrowHoundEntity extends Monster {

    private static final EntityDataAccessor<Boolean> TOMB_BOUND =
            SynchedEntityData.defineId(BarrowHoundEntity.class, EntityDataSerializers.BOOLEAN);

    private static final byte EVENT_CRYPT_BITE = 4;
    private static final byte EVENT_PACK_HOWL = 64;

    private static final int TOMB_CHECK_INTERVAL_TICKS = 30;
    private static final int TOMB_SEARCH_RADIUS = 12;
    private static final int PACK_HOWL_COOLDOWN_TICKS = 200;
    private static final double PACK_ALERT_RADIUS = 20.0D;

    private static final ResourceLocation BARROW_STONE_BRICKS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "barrow_stone_bricks");
    private static final ResourceLocation CRACKED_BARROW_BRICKS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "cracked_barrow_bricks");
    private static final ResourceLocation MOSSY_BARROW_BRICKS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mossy_barrow_bricks");
    private static final ResourceLocation BARROW_SENTINEL_TOTEM_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "barrow_sentinel_totem");
    private static final ResourceLocation NECROTIC_SOIL_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "necrotic_soil");
    private static final ResourceLocation BARROW_MOSS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "barrow_moss");

    private static final ResourceLocation WOLF_HOWL_SOUND_ID =
            ResourceLocation.withDefaultNamespace("entity.wolf.howl");

    private static final ResourceLocation TOMB_BOUND_DAMAGE_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "barrow_hound_tomb_bound_strength");
    private static final AttributeModifier TOMB_BOUND_DAMAGE_MODIFIER = new AttributeModifier(
            TOMB_BOUND_DAMAGE_MODIFIER_ID,
            0.20D,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    );

    private static final DustParticleOptions BARROW_MOTE_DUST = new DustParticleOptions(0x5B7A67, 0.65F);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState howlAnimationState = new AnimationState();

    private int howlCooldown = 0;

    public BarrowHoundEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(PathType.POWDER_SNOW, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_POWDER_SNOW, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.31D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    private static boolean matchesBlock(BlockState state, ResourceLocation id) {
        if (state.isAir()) {
            return false;
        }
        Optional<Block> opt = BuiltInRegistries.BLOCK.getOptional(id);
        return opt.isPresent() && state.is(opt.get());
    }

    public static boolean isPurifiedWardBlock(BlockState state) {
        for (Property<?> prop : state.getProperties()) {
            String propName = prop.getName().toLowerCase(Locale.ROOT);
            if ((propName.equals("purified")
                    || propName.equals("sanctified")
                    || propName.equals("cleansed")
                    || propName.equals("blessed")
                    || propName.equals("warded"))
                    && prop instanceof BooleanProperty boolProp) {
                if (Boolean.TRUE.equals(state.getValue(boolProp))) {
                    return true;
                }
            }
            String valStr = String.valueOf(state.getValue(prop)).toLowerCase(Locale.ROOT);
            if (valStr.contains("purified") || valStr.contains("sanctified") || valStr.contains("cleansed")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isTombMasonryBlock(BlockState state) {
        if (state.isAir()) {
            return false;
        }
        return matchesBlock(state, BARROW_STONE_BRICKS_ID)
                || matchesBlock(state, CRACKED_BARROW_BRICKS_ID)
                || matchesBlock(state, MOSSY_BARROW_BRICKS_ID);
    }

    private static boolean isThinBarrowCover(BlockState state) {
        if (state.is(Blocks.SNOW)) {
            return true;
        }
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return "grave_moss".equals(path)
                || "crypt_grave_root".equals(path)
                || "rime_cap_mushroom".equals(path)
                || "barrow_moss_carpet".equals(path)
                || "blue_frozen_moss_carpet".equals(path)
                || "cryo_static_shrub".equals(path);
    }

    public static boolean isBarrowHoundSpawnSurface(BlockGetter level, BlockPos belowPos, BlockState belowState) {
        if (belowState.isAir()) {
            return false;
        }
        if (matchesBlock(belowState, NECROTIC_SOIL_ID)
                || matchesBlock(belowState, BARROW_STONE_BRICKS_ID)
                || matchesBlock(belowState, CRACKED_BARROW_BRICKS_ID)
                || matchesBlock(belowState, MOSSY_BARROW_BRICKS_ID)
                || matchesBlock(belowState, BARROW_MOSS_ID)
                || belowState.is(Blocks.SNOW_BLOCK)
                || belowState.is(Blocks.SNOW)
                || belowState.is(Blocks.ICE)
                || belowState.is(Blocks.PACKED_ICE)
                || belowState.is(Blocks.BLUE_ICE)
                || belowState.is(Blocks.COARSE_DIRT)
                || belowState.is(Blocks.PODZOL)
                || belowState.is(Blocks.GRASS_BLOCK)
                || belowState.is(Blocks.GRAVEL)
                || belowState.is(Blocks.STONE)
                || belowState.is(BlockTags.STONE_BRICKS)
                || belowState.is(BlockTags.BASE_STONE_OVERWORLD)
                || belowState.is(BlockTags.ANIMALS_SPAWNABLE_ON)) {
            return true;
        }
        String path = BuiltInRegistries.BLOCK.getKey(belowState.getBlock()).getPath();
        return "coarse_gravel".equals(path)
                || "blue_frozen_moss".equals(path)
                || (belowState.getFluidState().isEmpty() && belowState.isFaceSturdy(level, belowPos, Direction.UP));
    }

    public static boolean checkBarrowHoundSpawnRules(
            EntityType<BarrowHoundEntity> type,
            ServerLevelAccessor level,
            EntitySpawnReason spawnReason,
            BlockPos pos,
            RandomSource random
    ) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        if (EntitySpawnReason.isSpawner(spawnReason)
                || spawnReason == EntitySpawnReason.SPAWN_ITEM_USE
                || spawnReason == EntitySpawnReason.COMMAND) {
            return true;
        }

        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
        int skyLight = level.getBrightness(LightLayer.SKY, pos);
        int internalBrightness = level.getRawBrightness(pos, 0);

        boolean isBarrowTundra = level.getBiome(pos).unwrapKey()
                .map(k -> "barrow_mound_tundra".equals(k.location().getPath()))
                .orElse(false);

        boolean darkOrMistyBarrow = Monster.isDarkEnoughToSpawn(level, pos, random)
                || (blockLight <= 9 && (skyLight <= 9
                || internalBrightness <= 9
                || (isBarrowTundra && (level.getLevel().isRaining() || level.getLevel().isThundering() || level.getLevel().isDarkOutside()))));
        if (!darkOrMistyBarrow) {
            return false;
        }

        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        if (isThinBarrowCover(below) && level.getBlockState(belowPos.below()).blocksMotion()) {
            belowPos = belowPos.below();
            below = level.getBlockState(belowPos);
        }

        return isBarrowHoundSpawnSurface(level, belowPos, below);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TOMB_BOUND, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LeapAtTargetGoal(this, 0.42F));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean isTombBound() {
        return this.entityData.get(TOMB_BOUND);
    }

    public void setTombBound(boolean tombBound) {
        this.entityData.set(TOMB_BOUND, tombBound);
        if (!this.level().isClientSide()) {
            this.updateTombBoundModifier(tombBound);
        }
    }

    private void updateTombBoundModifier(boolean tombBound) {
        AttributeInstance attackAttr = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr == null) {
            return;
        }
        if (tombBound) {
            if (!attackAttr.hasModifier(TOMB_BOUND_DAMAGE_MODIFIER_ID)) {
                attackAttr.addTransientModifier(TOMB_BOUND_DAMAGE_MODIFIER);
            }
        } else {
            attackAttr.removeModifier(TOMB_BOUND_DAMAGE_MODIFIER_ID);
        }
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        if (source.is(DamageTypeTags.IS_FREEZING) || source.is(DamageTypes.FREEZE)) {
            return true;
        }
        return super.isInvulnerableTo(level, source);
    }

    @Override
    public void makeStuckInBlock(BlockState state, Vec3 motionMultiplier) {
        if (state.is(Blocks.POWDER_SNOW) || state.is(Blocks.SNOW) || state.getBlock() instanceof SnowLayerBlock) {
            return;
        }
        super.makeStuckInBlock(state, motionMultiplier);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        BlockState below = level.getBlockState(pos.below());
        if (isTombMasonryBlock(below) || matchesBlock(below, NECROTIC_SOIL_ID)) {
            return 10.0F;
        }
        return super.getWalkTargetValue(pos, level);
    }

    private boolean scanNearbyTombMasonry() {
        Level level = this.level();
        BlockPos origin = this.blockPosition();
        int r = TOMB_SEARCH_RADIUS;
        int rSq = r * r;
        boolean foundTombMasonry = false;

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = -8; dy <= 8; dy++) {
            int dySq = dy * dy;
            for (int dx = -r; dx <= r; dx++) {
                int dxySq = dx * dx + dySq;
                if (dxySq > rSq) {
                    continue;
                }
                for (int dz = -r; dz <= r; dz++) {
                    if (dxySq + dz * dz > rSq) {
                        continue;
                    }
                    cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    BlockState state = level.getBlockState(cursor);
                    if (state.isAir()) {
                        continue;
                    }
                    if (matchesBlock(state, BARROW_SENTINEL_TOTEM_ID) || isPurifiedWardBlock(state)) {
                        // Near a Barrow Sentinel Totem or purified ward: suppress Tomb-Bound Strength
                        return false;
                    }
                    if (!foundTombMasonry
                            && (matchesBlock(state, BARROW_STONE_BRICKS_ID)
                            || matchesBlock(state, CRACKED_BARROW_BRICKS_ID)
                            || matchesBlock(state, MOSSY_BARROW_BRICKS_ID))) {
                        foundTombMasonry = true;
                    }
                }
            }
        }
        return foundTombMasonry;
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        LivingEntity previousTarget = this.getTarget();
        super.setTarget(target);
        if (!this.level().isClientSide()
                && this.level() instanceof ServerLevel serverLevel
                && target != null
                && target.isAlive()
                && target != previousTarget
                && this.howlCooldown <= 0) {
            this.triggerPackHowl(serverLevel, target);
        }
    }

    private void triggerPackHowl(ServerLevel level, LivingEntity target) {
        this.howlCooldown = PACK_HOWL_COOLDOWN_TICKS;
        level.broadcastEntityEvent(this, EVENT_PACK_HOWL);

        SoundEvent howlSound = BuiltInRegistries.SOUND_EVENT.getOptional(WOLF_HOWL_SOUND_ID)
                .orElseGet(() -> {
                    WolfSoundVariant sadSet = SoundEvents.WOLF_SOUNDS.get(WolfSoundVariants.SoundSet.SAD);
                    if (sadSet != null) {
                        return sadSet.whineSound().value();
                    }
                    return SoundEvents.FOX_SCREECH;
                });

        level.playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                howlSound,
                SoundSource.HOSTILE,
                1.25F,
                0.65F
        );

        this.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 0), this);

        List<BarrowHoundEntity> packmates = level.getEntitiesOfClass(
                BarrowHoundEntity.class,
                this.getBoundingBox().inflate(PACK_ALERT_RADIUS),
                hound -> hound.isAlive() && this.distanceToSqr(hound) <= PACK_ALERT_RADIUS * PACK_ALERT_RADIUS
        );
        for (BarrowHoundEntity ally : packmates) {
            ally.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 0), this);
            if (ally != this) {
                ally.howlCooldown = Math.max(ally.howlCooldown, 80);
                if (ally.getTarget() == null) {
                    ally.setTarget(target);
                }
            }
        }

        level.sendParticles(
                ParticleTypes.SOUL,
                this.getX(),
                this.getY() + 0.85D,
                this.getZ(),
                10,
                0.35D,
                0.25D,
                0.35D,
                0.03D
        );
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        // Ensure Tomb-Bound +20% melee modifier is active when tomb-bound
        this.updateTombBoundModifier(this.isTombBound());

        boolean hit = super.doHurtTarget(level, target);
        level.broadcastEntityEvent(this, EVENT_CRYPT_BITE);

        if (hit && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0), this);

            SoundEvent snarl = getWolfSoundVariant().growlSound().value();
            level.playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    snarl,
                    SoundSource.HOSTILE,
                    0.95F,
                    0.7F
            );
            level.sendParticles(
                    ParticleTypes.SOUL,
                    living.getX(),
                    living.getY() + living.getBbHeight() * 0.5D,
                    living.getZ(),
                    8,
                    0.28D,
                    0.3D,
                    0.28D,
                    0.03D
            );
        }
        return hit;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_CRYPT_BITE) {
            this.attackAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_PACK_HOWL) {
            this.howlAnimationState.start(this.tickCount);
            for (int i = 0; i < 8; i++) {
                this.level().addParticle(
                        ParticleTypes.SOUL,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.6D,
                        this.getY() + 0.75D + this.random.nextDouble() * 0.35D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.6D,
                        0.0D,
                        0.04D,
                        0.0D
                );
            }
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public void aiStep() {
        if (this.getTicksFrozen() > 0) {
            this.setTicksFrozen(0);
        }

        super.aiStep();

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            if (this.howlCooldown > 0) {
                this.howlCooldown--;
            }

            if (this.tickCount <= 1 || (this.tickCount + this.getId()) % TOMB_CHECK_INTERVAL_TICKS == 0) {
                boolean nearTomb = this.scanNearbyTombMasonry();
                if (nearTomb != this.isTombBound()) {
                    this.setTombBound(nearTomb);
                } else {
                    this.updateTombBoundModifier(nearTomb);
                }
            }
        } else if (this.level().isClientSide()) {
            this.setupClientAnimationsAndParticles();
        }
    }

    private void setupClientAnimationsAndParticles() {
        if (this.attackAnimationState.isStarted() && this.attackAnimationState.getTimeInMillis(this.tickCount) >= 1500L) {
            this.attackAnimationState.stop();
        }
        if (this.howlAnimationState.isStarted() && this.howlAnimationState.getTimeInMillis(this.tickCount) >= 2400L) {
            this.howlAnimationState.stop();
        }

        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
        if (moving) {
            this.idleAnimationState.stop();
            this.walkAnimationState.startIfStopped(this.tickCount);
        } else {
            this.walkAnimationState.stop();
            this.idleAnimationState.startIfStopped(this.tickCount);
        }

        if (this.isTombBound() && this.tickCount % 10 == 0) {
            this.level().addParticle(
                    BARROW_MOTE_DUST,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.7D,
                    this.getY() + 0.35D + this.random.nextDouble() * 0.55D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.7D,
                    0.0D,
                    0.015D,
                    0.0D
            );
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("HowlCooldown", Codec.INT, this.howlCooldown);
        output.store("TombBound", Codec.BOOL, this.isTombBound());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.howlCooldown = input.read("HowlCooldown", Codec.INT).orElse(0);
        this.setTombBound(input.read("TombBound", Codec.BOOL).orElse(false));
    }

    private static WolfSoundVariant getWolfSoundVariant() {
        WolfSoundVariant big = SoundEvents.WOLF_SOUNDS.get(WolfSoundVariants.SoundSet.BIG);
        if (big != null) {
            return big;
        }
        return SoundEvents.WOLF_SOUNDS.get(WolfSoundVariants.SoundSet.CLASSIC);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return getWolfSoundVariant().growlSound().value();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return getWolfSoundVariant().hurtSound().value();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return getWolfSoundVariant().deathSound().value();
    }

    @Override
    public float getVoicePitch() {
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.12F + 0.68F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.WOLF_STEP, 0.2F, 0.85F);
    }
}
