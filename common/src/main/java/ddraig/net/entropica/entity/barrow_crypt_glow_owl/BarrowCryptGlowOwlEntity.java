package ddraig.net.entropica.entity.barrow_crypt_glow_owl;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Optional;

/**
 * Fauna 9 — Barrow Crypt-Glow Owl (Silent Crypt Flyer).
 * Neutral nocturnal/twilight flyer native to 2.8 Barrow-Mound Tundra:
 * - Glides silently between barrows and dead spruce trees, occasionally perching on logs or stone bricks.
 * - Provoked Swoop: if attacked, swoops down to strike for 6.0F damage and inflicts Blindness (60t = 3s)
 *   while triggering attackAnimationState.
 * - Grave-Glow Shedding: every 2400-4800 ticks when alive and near barrow_stone_bricks, necrotic_soil,
 *   or grave_moss, sheds 1x entropica:grave_glow_feather with soft amethyst/soul particles.
 */
public class BarrowCryptGlowOwlEntity extends PathfinderMob implements FlyingAnimal {

    private static final byte EVENT_SWOOP_ATTACK = 4;

    private static final int MIN_SHED_INTERVAL_TICKS = 2400;
    private static final int MAX_SHED_RANDOM_EXTRA_TICKS = 2401;

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
    private static final ResourceLocation GRAVE_MOSS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "grave_moss");
    private static final ResourceLocation BARROW_MOSS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "barrow_moss");
    private static final ResourceLocation GRAVE_GLOW_FEATHER_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "grave_glow_feather");

    private static final DustParticleOptions AMETHYST_GLOW_DUST = new DustParticleOptions(0xB388FF, 0.55F);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState flyAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();

    private int featherShedTimer;
    private int perchRestTicks = 0;

    public BarrowCryptGlowOwlEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 18, false);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(PathType.POWDER_SNOW, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_POWDER_SNOW, 0.0F);
        this.featherShedTimer = MIN_SHED_INTERVAL_TICKS + this.random.nextInt(MAX_SHED_RANDOM_EXTRA_TICKS);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.FLYING_SPEED, 0.42D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 20.0D);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        return navigation;
    }

    private static boolean matchesBlock(BlockState state, ResourceLocation id) {
        if (state.isAir()) {
            return false;
        }
        Optional<Block> opt = BuiltInRegistries.BLOCK.getOptional(id);
        return opt.isPresent() && state.is(opt.get());
    }

    public static boolean isOwlPerchBlock(BlockState state) {
        if (state.isAir()) {
            return false;
        }
        return state.is(Blocks.SPRUCE_LOG)
                || state.is(Blocks.STRIPPED_SPRUCE_LOG)
                || state.is(Blocks.SPRUCE_WOOD)
                || state.is(BlockTags.LOGS)
                || state.is(BlockTags.STONE_BRICKS)
                || matchesBlock(state, BARROW_STONE_BRICKS_ID)
                || matchesBlock(state, CRACKED_BARROW_BRICKS_ID)
                || matchesBlock(state, MOSSY_BARROW_BRICKS_ID)
                || matchesBlock(state, BARROW_SENTINEL_TOTEM_ID);
    }

    private static boolean isGraveGlowSubstrate(BlockState state) {
        if (state.isAir()) {
            return false;
        }
        return matchesBlock(state, BARROW_STONE_BRICKS_ID)
                || matchesBlock(state, CRACKED_BARROW_BRICKS_ID)
                || matchesBlock(state, MOSSY_BARROW_BRICKS_ID)
                || matchesBlock(state, NECROTIC_SOIL_ID)
                || matchesBlock(state, GRAVE_MOSS_ID);
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

    public static boolean isBarrowOwlSpawnSurface(BlockGetter level, BlockPos belowPos, BlockState belowState) {
        if (belowState.isAir()) {
            return false;
        }
        if (isOwlPerchBlock(belowState)
                || matchesBlock(belowState, NECROTIC_SOIL_ID)
                || matchesBlock(belowState, BARROW_MOSS_ID)
                || belowState.is(Blocks.SNOW_BLOCK)
                || belowState.is(Blocks.SNOW)
                || belowState.is(Blocks.ICE)
                || belowState.is(Blocks.PACKED_ICE)
                || belowState.is(Blocks.BLUE_ICE)
                || belowState.is(Blocks.COARSE_DIRT)
                || belowState.is(Blocks.PODZOL)
                || belowState.is(Blocks.GRASS_BLOCK)
                || belowState.is(Blocks.STONE)
                || belowState.is(Blocks.GRAVEL)
                || belowState.is(BlockTags.LEAVES)
                || belowState.is(BlockTags.ANIMALS_SPAWNABLE_ON)
                || belowState.is(BlockTags.BASE_STONE_OVERWORLD)) {
            return true;
        }
        String path = BuiltInRegistries.BLOCK.getKey(belowState.getBlock()).getPath();
        return "coarse_gravel".equals(path)
                || "blue_frozen_moss".equals(path)
                || (belowState.getFluidState().isEmpty() && belowState.isFaceSturdy(level, belowPos, Direction.UP));
    }

    public static boolean checkBarrowOwlSpawnRules(
            EntityType<? extends PathfinderMob> type,
            ServerLevelAccessor level,
            EntitySpawnReason spawnReason,
            BlockPos pos,
            RandomSource random
    ) {
        if (EntitySpawnReason.isSpawner(spawnReason)
                || spawnReason == EntitySpawnReason.SPAWN_ITEM_USE
                || spawnReason == EntitySpawnReason.COMMAND) {
            return true;
        }

        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        if (isThinBarrowCover(below) && level.getBlockState(belowPos.below()).blocksMotion()) {
            belowPos = belowPos.below();
            below = level.getBlockState(belowPos);
        }
        return isBarrowOwlSpawnSurface(level, belowPos, below);
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        BlockPos belowPos = this.blockPosition().below();
        BlockState below = level.getBlockState(belowPos);
        if (isThinBarrowCover(below) && level.getBlockState(belowPos.below()).blocksMotion()) {
            belowPos = belowPos.below();
            below = level.getBlockState(belowPos);
        }
        if (isBarrowOwlSpawnSurface(level, belowPos, below)) {
            return true;
        }
        return super.checkSpawnRules(level, spawnReason);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        if (isOwlPerchBlock(level.getBlockState(pos.below()))) {
            return 12.0F;
        }
        if (level.getBlockState(pos).isAir()) {
            return 6.0F;
        }
        return 0.0F;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new OwlProvokedSwoopGoal(this, 1.35D));
        this.goalSelector.addGoal(2, new OwlBarrowPerchAndGlideGoal(this, 1.05D));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.85D, 0.01F));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        // Silent crypt flyer — immune to fall damage accumulation
    }

    @Override
    protected boolean isFlapping() {
        // Silent owl wingbeats
        return false;
    }

    @Override
    protected void onFlap() {
        // Silent owl wingbeats
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
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        level.broadcastEntityEvent(this, EVENT_SWOOP_ATTACK);

        if (hit && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0), this);

            level.playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.PHANTOM_SWOOP,
                    SoundSource.NEUTRAL,
                    0.75F,
                    1.15F
            );
            level.sendParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    living.getX(),
                    living.getY() + living.getBbHeight() * 0.55D,
                    living.getZ(),
                    10,
                    0.3D,
                    0.3D,
                    0.3D,
                    0.025D
            );
            level.sendParticles(
                    ParticleTypes.WITCH,
                    living.getX(),
                    living.getY() + living.getBbHeight() * 0.55D,
                    living.getZ(),
                    8,
                    0.28D,
                    0.28D,
                    0.28D,
                    0.02D
            );
        }
        return hit;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_SWOOP_ATTACK) {
            this.attackAnimationState.start(this.tickCount);
            return;
        }
        super.handleEntityEvent(id);
    }

    private boolean isNearGraveGlowSubstrate(int radius) {
        Level level = this.level();
        BlockPos origin = this.blockPosition();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = -4; dy <= 4; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    if (isGraveGlowSubstrate(level.getBlockState(cursor))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void tryShedGraveGlowFeather(ServerLevel level) {
        if (!this.isNearGraveGlowSubstrate(8)) {
            this.featherShedTimer = 100 + this.random.nextInt(80);
            return;
        }

        this.featherShedTimer = MIN_SHED_INTERVAL_TICKS + this.random.nextInt(MAX_SHED_RANDOM_EXTRA_TICKS);
        Optional<Item> featherOpt = BuiltInRegistries.ITEM.getOptional(GRAVE_GLOW_FEATHER_ID);
        if (featherOpt.isEmpty()) {
            return;
        }

        this.spawnAtLocation(level, new ItemStack(featherOpt.get(), 1), 0.25F);
        level.playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.NEUTRAL,
                0.55F,
                1.2F
        );
        level.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                this.getX(),
                this.getY() + 0.45D,
                this.getZ(),
                8,
                0.28D,
                0.22D,
                0.28D,
                0.015D
        );
        level.sendParticles(
                ParticleTypes.WITCH,
                this.getX(),
                this.getY() + 0.45D,
                this.getZ(),
                8,
                0.28D,
                0.22D,
                0.28D,
                0.02D
        );
    }

    @Override
    public void aiStep() {
        if (this.getTicksFrozen() > 0) {
            this.setTicksFrozen(0);
        }

        super.aiStep();

        // Soft owl feather-glide descent when airborne and not actively diving
        Vec3 vel = this.getDeltaMovement();
        if (!this.onGround() && vel.y < 0.0D && this.getTarget() == null) {
            this.setDeltaMovement(vel.multiply(1.0D, 0.65D, 1.0D));
        }

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            if (this.perchRestTicks > 0) {
                this.perchRestTicks--;
            }

            if (this.isAlive()) {
                this.featherShedTimer--;
                if (this.featherShedTimer <= 0) {
                    this.tryShedGraveGlowFeather(serverLevel);
                }
            }
        } else if (this.level().isClientSide()) {
            this.setupClientAnimationsAndParticles();
        }
    }

    private void setupClientAnimationsAndParticles() {
        if (this.attackAnimationState.isStarted() && this.attackAnimationState.getTimeInMillis(this.tickCount) >= 1600L) {
            this.attackAnimationState.stop();
        }

        if (this.isFlying()) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.flyAnimationState.startIfStopped(this.tickCount);
        } else {
            this.flyAnimationState.stop();
            boolean walking = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
            if (walking) {
                this.idleAnimationState.stop();
                this.walkAnimationState.startIfStopped(this.tickCount);
            } else {
                this.walkAnimationState.stop();
                this.idleAnimationState.startIfStopped(this.tickCount);
            }
        }

        // Subtle grave-glow motes trailing from luminescent wingtips
        if (this.tickCount % (this.isFlying() ? 6 : 16) == 0) {
            this.level().addParticle(
                    AMETHYST_GLOW_DUST,
                    this.getX() + (this.random.nextDouble() - 0.5D) * (this.isFlying() ? 0.95D : 0.45D),
                    this.getY() + 0.35D + this.random.nextDouble() * 0.4D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * (this.isFlying() ? 0.95D : 0.45D),
                    0.0D,
                    -0.01D,
                    0.0D
            );
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("FeatherShedTimer", Codec.INT, this.featherShedTimer);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.featherShedTimer = input.read("FeatherShedTimer", Codec.INT)
                .orElse(MIN_SHED_INTERVAL_TICKS + this.random.nextInt(MAX_SHED_RANDOM_EXTRA_TICKS));
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.random.nextBoolean()
                ? SoundEvents.PARROT_IMITATE_PHANTOM
                : SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PHANTOM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PHANTOM_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.5F;
    }

    @Override
    public float getVoicePitch() {
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.1F + 0.58F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        // Silent talon step
    }

    /**
     * Swoops directly at a provoking attacker with silent wingbeats and strikes in melee.
     */
    static class OwlProvokedSwoopGoal extends MeleeAttackGoal {
        private final BarrowCryptGlowOwlEntity owl;

        OwlProvokedSwoopGoal(BarrowCryptGlowOwlEntity owl, double speedModifier) {
            super(owl, speedModifier, true);
            this.owl = owl;
        }

        @Override
        public void start() {
            super.start();
            this.owl.perchRestTicks = 0;
            if (this.owl.onGround()) {
                this.owl.setDeltaMovement(this.owl.getDeltaMovement().add(0.0D, 0.36D, 0.0D));
            }
        }

        @Override
        public void tick() {
            super.tick();
            LivingEntity target = this.owl.getTarget();
            if (target != null && target.isAlive()) {
                this.owl.getMoveControl().setWantedPosition(
                        target.getX(),
                        target.getY() + target.getBbHeight() * 0.55D,
                        target.getZ(),
                        1.35D
                );
            }
        }
    }

    /**
     * Glides silently between barrow stone masonry and dead spruce logs, perching quietly atop them.
     */
    static class OwlBarrowPerchAndGlideGoal extends Goal {
        private final BarrowCryptGlowOwlEntity owl;
        private final double speedModifier;
        private @Nullable Vec3 destination;
        private boolean destinationIsPerch;
        private int flightTicks;

        OwlBarrowPerchAndGlideGoal(BarrowCryptGlowOwlEntity owl, double speedModifier) {
            this.owl = owl;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.owl.getTarget() != null || this.owl.perchRestTicks > 0) {
                return false;
            }
            if (this.owl.getNavigation().isInProgress() || this.owl.getRandom().nextInt(25) != 0) {
                return false;
            }
            this.destination = this.findPerchOrGlideTarget();
            return this.destination != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.owl.getTarget() == null
                    && this.destination != null
                    && !this.owl.getNavigation().isDone()
                    && this.flightTicks < 160;
        }

        @Override
        public void start() {
            this.flightTicks = 0;
            if (this.destination != null) {
                if (this.owl.onGround()) {
                    this.owl.setDeltaMovement(this.owl.getDeltaMovement().add(0.0D, 0.3D, 0.0D));
                }
                this.owl.getNavigation().moveTo(
                        this.destination.x,
                        this.destination.y,
                        this.destination.z,
                        this.speedModifier
                );
            }
        }

        @Override
        public void tick() {
            this.flightTicks++;
            if (this.destination != null && this.owl.position().distanceToSqr(this.destination) <= 1.75D) {
                this.owl.getNavigation().stop();
                if (this.destinationIsPerch) {
                    this.owl.perchRestTicks = 100 + this.owl.getRandom().nextInt(140);
                }
                this.destination = null;
            }
        }

        @Override
        public void stop() {
            this.destination = null;
        }

        private @Nullable Vec3 findPerchOrGlideTarget() {
            Level level = this.owl.level();
            BlockPos origin = this.owl.blockPosition();
            RandomSource rand = this.owl.getRandom();

            // 65% chance to seek an elevated spruce log or barrow brick perch within 14 blocks
            if (rand.nextFloat() < 0.65F) {
                BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
                for (int attempt = 0; attempt < 24; attempt++) {
                    int dx = rand.nextInt(29) - 14;
                    int dy = rand.nextInt(11) - 3;
                    int dz = rand.nextInt(29) - 14;
                    cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    if (cursor.distSqr(origin) < 9.0D) {
                        continue;
                    }
                    BlockState state = level.getBlockState(cursor);
                    if (isOwlPerchBlock(state)
                            && level.getBlockState(cursor.above()).isAir()
                            && level.getBlockState(cursor.above(2)).isAir()) {
                        this.destinationIsPerch = true;
                        return new Vec3(cursor.getX() + 0.5D, cursor.getY() + 1.05D, cursor.getZ() + 0.5D);
                    }
                }
            }

            // Otherwise glide silently at low twilight altitude (3-7 blocks above barrows)
            double gx = this.owl.getX() + (rand.nextDouble() - 0.5D) * 20.0D;
            double gz = this.owl.getZ() + (rand.nextDouble() - 0.5D) * 20.0D;
            int surfaceY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, (int) gx, (int) gz);
            double gy = Math.min(surfaceY + 3.0D + rand.nextDouble() * 4.5D, level.getMaxY() - 8);
            BlockPos airCheck = BlockPos.containing(gx, gy, gz);
            if (level.getBlockState(airCheck).isAir()) {
                this.destinationIsPerch = false;
                return new Vec3(gx, gy, gz);
            }
            return null;
        }
    }
}
