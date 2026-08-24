package ddraig.net.entropica.entity;

import ddraig.net.entropica.registry.ModEntityTypes;
import ddraig.net.entropica.registry.ModItems;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class BloomCrawlerEntity extends Animal {
    private static final EntityDataAccessor<Integer> SHELL_AGE = SynchedEntityData.defineId(BloomCrawlerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_GRAZING = SynchedEntityData.defineId(BloomCrawlerEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState runAnimationState = new AnimationState();
    public final AnimationState grazeAnimationState = new AnimationState();

    private int grazeTimer = 0;
    private int grazeCooldown = 100;
    private int fleeTimer = 0;

    public BloomCrawlerEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.TEMPT_RANGE, 8.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BloomCrawlerFleeGoal(this));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.0D, Ingredient.of(Items.BONE_MEAL), false));
        this.goalSelector.addGoal(4, new BloomCrawlerGrazeGoal(this));
        this.goalSelector.addGoal(5, new BloomCrawlerHerdGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, net.minecraft.world.entity.player.Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SHELL_AGE, 0);
        builder.define(IS_GRAZING, false);
    }

    public int getShellAge() {
        return this.entityData.get(SHELL_AGE);
    }

    public void setShellAge(int age) {
        this.entityData.set(SHELL_AGE, Math.max(0, Math.min(2, age)));
    }

    public boolean isGrazing() {
        return this.entityData.get(IS_GRAZING);
    }

    public void setGrazing(boolean grazing) {
        this.entityData.set(IS_GRAZING, grazing);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnType, @Nullable SpawnGroupData spawnGroupData) {
        this.setShellAge(this.random.nextInt(3)); // 0 = Young, 1 = Medium, 2 = Old
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setShellAge(input.read("ShellAge", Codec.INT).orElse(0));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("ShellAge", Codec.INT, this.getShellAge());
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.BONE_MEAL);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob parent) {
        BloomCrawlerEntity offspring = ModEntityTypes.BLOOM_CRAWLER.get().create(level, EntitySpawnReason.BREEDING);
        if (offspring != null) {
            // Offspring shell age is average of parents or slightly randomized
            int parentAge = this.getShellAge();
            if (parent instanceof BloomCrawlerEntity otherParent) {
                parentAge = (parentAge + otherParent.getShellAge()) / 2;
            }
            offspring.setShellAge(Math.max(0, parentAge - 1)); // Starts younger
        }
        return offspring;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide()) {
            // Particle effect when raining
            if (this.level().isRaining() && this.random.nextFloat() < 0.2f) {
                this.level().addParticle(ParticleTypes.DRIPPING_WATER,
                        this.getX() + (this.random.nextDouble() - 0.5D) * this.getBbWidth(),
                        this.getY() + this.getBbHeight() * 0.8D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * this.getBbWidth(),
                        0.0D, 0.0D, 0.0D);
            }
            return;
        }

        // Flee timer squeaking
        if (this.fleeTimer > 0) {
            this.fleeTimer--;
            if (this.fleeTimer % 15 == 0) {
                this.level().playSound(null, this.blockPosition(), SoundEvents.RABBIT_HURT, SoundSource.NEUTRAL, 0.8F, 1.6F + this.random.nextFloat() * 0.4F);
            }
        }

        // Graze cooldown
        if (this.grazeCooldown > 0) {
            this.grazeCooldown--;
        }

        // Handle active grazing timer on server
        if (this.isGrazing()) {
            this.grazeTimer--;
            if (this.grazeTimer <= 0) {
                this.setGrazing(false);
            }
        }

        // Rain behavior speed boost
        if (this.level().isRaining()) {
            this.setSpeed(0.26F); // 1.2x speed boost
        } else {
            this.setSpeed(0.22F);
        }

        // Passive Growth Aura: Turn nearby dirt to grass
        if (this.tickCount % 60 == 0) {
            int radius = 2;
            int attempts = this.level().isRaining() ? 6 : 3;
            BlockPos currentPos = this.blockPosition();
            for (int i = 0; i < attempts; i++) {
                BlockPos target = currentPos.offset(
                        this.random.nextInt(radius * 2 + 1) - radius,
                        this.random.nextInt(2) - 1,
                        this.random.nextInt(radius * 2 + 1) - radius
                );
                if (this.level().getBlockState(target).is(Blocks.DIRT)) {
                    this.level().setBlockAndUpdate(target, Blocks.GRASS_BLOCK.defaultBlockState());
                    if (this.random.nextFloat() < 0.15f) {
                        ((ServerLevel) this.level()).sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                target.getX() + 0.5D, target.getY() + 1.1D, target.getZ() + 0.5D,
                                3, 0.2D, 0.2D, 0.2D, 0.0D);
                    }
                }
            }
        }

        // Growth Trail: apply bone meal / grow plants when scuttling
        if (this.getDeltaMovement().horizontalDistanceSqr() > 0.001D && this.tickCount % 20 == 0) {
            float growthChance = this.level().isRaining() ? 0.30f : 0.15f;
            if (this.random.nextFloat() < growthChance) {
                BlockPos pos = this.blockPosition();
                BlockPos below = pos.below();
                BlockState stateBelow = this.level().getBlockState(below);

                // Grow crops / plants
                if (this.level().getBlockState(pos).isAir() && stateBelow.is(Blocks.GRASS_BLOCK)) {
                    // Random grass or flower spread
                    BlockState flowerState = getRandomFlora();
                    if (flowerState != null) {
                        this.level().setBlockAndUpdate(pos, flowerState);
                        ((ServerLevel) this.level()).sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                pos.getX() + 0.5D, pos.getY() + 0.3D, pos.getZ() + 0.5D,
                                5, 0.2D, 0.2D, 0.2D, 0.05D);
                    }
                } else {
                    // Try to apply bonemeal to crops or saplings
                    BlockState posState = this.level().getBlockState(pos);
                    if (posState.is(Blocks.OAK_SAPLING) || posState.is(Blocks.SPRUCE_SAPLING) || posState.is(Blocks.BIRCH_SAPLING) ||
                            posState.is(Blocks.JUNGLE_SAPLING) || posState.is(Blocks.ACACIA_SAPLING) || posState.is(Blocks.DARK_OAK_SAPLING)) {
                        net.minecraft.world.item.BoneMealItem.growWaterPlant(new ItemStack(Items.BONE_MEAL), this.level(), pos, null);
                        ((ServerLevel) this.level()).sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                                8, 0.3D, 0.3D, 0.3D, 0.05D);
                    }
                }
            }
        }
    }

    private BlockState getRandomFlora() {
        float r = this.random.nextFloat();
        if (r < 0.60f) {
            return Blocks.SHORT_GRASS.defaultBlockState();
        } else if (r < 0.75f) {
            return Blocks.FERN.defaultBlockState();
        } else if (r < 0.85f) {
            return Blocks.DANDELION.defaultBlockState();
        } else if (r < 0.95f) {
            return Blocks.POPPY.defaultBlockState();
        } else {
            // Rare flower
            int shellAge = this.getShellAge();
            if (shellAge == 2) {
                return Blocks.BLUE_ORCHID.defaultBlockState();
            } else if (shellAge == 1) {
                return Blocks.ALLIUM.defaultBlockState();
            } else {
                return Blocks.OXEYE_DAISY.defaultBlockState();
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.idleAnimationState.animateWhen(!this.walkAnimationState.isStarted() && !this.runAnimationState.isStarted() && !this.grazeAnimationState.isStarted(), this.tickCount);
            if (this.isGrazing()) {
                this.grazeAnimationState.startIfStopped(this.tickCount);
                this.walkAnimationState.stop();
                this.runAnimationState.stop();
            } else {
                this.grazeAnimationState.stop();
                if (this.getDeltaMovement().horizontalDistanceSqr() > 0.01D) {
                    if (this.isSprinting() || this.fleeTimer > 0) {
                        this.runAnimationState.startIfStopped(this.tickCount);
                        this.walkAnimationState.stop();
                    } else {
                        this.walkAnimationState.startIfStopped(this.tickCount);
                        this.runAnimationState.stop();
                    }
                } else {
                    this.walkAnimationState.stop();
                    this.runAnimationState.stop();
                }
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel serverLevel, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(serverLevel, damageSource, recentlyHit);
        
        int shellAge = this.getShellAge();
        
        // Bone meal drops
        int boneMealCount = this.random.nextInt(2) + shellAge;
        if (boneMealCount > 0) {
            this.spawnAtLocation(serverLevel, new ItemStack(Items.BONE_MEAL, boneMealCount));
        }

        // Shell fragment drops
        if (shellAge >= 1) {
            int fragmentCount = this.random.nextFloat() < 0.5f ? shellAge : shellAge - 1;
            if (fragmentCount > 0) {
                this.spawnAtLocation(serverLevel, new ItemStack(ModItems.CRAWLER_SHELL_FRAGMENT.get(), fragmentCount));
            }
        }

        // Flower drops based on age
        if (shellAge == 1) {
            this.spawnAtLocation(serverLevel, new ItemStack(this.random.nextBoolean() ? Items.DANDELION : Items.POPPY, 1));
        } else if (shellAge == 2) {
            Item flower = Items.BLUE_ORCHID;
            float roll = this.random.nextFloat();
            if (roll < 0.33f) flower = Items.LILY_OF_THE_VALLEY;
            else if (roll < 0.66f) flower = Items.SPORE_BLOSSOM;
            this.spawnAtLocation(serverLevel, new ItemStack(flower, 1));
        }
    }

    public void triggerFlee() {
        this.fleeTimer = 60;
    }

    // --- CUSTOM GOALS ---

    public static class BloomCrawlerFleeGoal extends Goal {
        private final BloomCrawlerEntity crawler;
        private LivingEntity threat;

        public BloomCrawlerFleeGoal(BloomCrawlerEntity crawler) {
            this.crawler = crawler;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            // Detect hostile threat
            // Emberwings and Ashen Stalkers at 8 blocks range, players/hostiles at 4 blocks
            List<LivingEntity> potentialThreats = crawler.level().getEntitiesOfClass(LivingEntity.class, crawler.getBoundingBox().inflate(8.0D));
            for (LivingEntity entity : potentialThreats) {
                if (entity.isAlive() && entity != crawler) {
                    double distSq = entity.distanceToSqr(crawler);
                    boolean isPredator = entity.getType().toString().contains("emberwing") || entity.getType().toString().contains("ashen_stalker");
                    
                    if (isPredator && distSq <= 64.0D) { // 8 blocks
                        this.threat = entity;
                        return true;
                    }
                    if ((entity instanceof net.minecraft.world.entity.monster.Monster || entity instanceof net.minecraft.world.entity.player.Player) && distSq <= 16.0D) { // 4 blocks
                        this.threat = entity;
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void start() {
            crawler.triggerFlee();
        }

        @Override
        public void tick() {
            if (threat != null) {
                Vec3 fleeDir = crawler.position().subtract(threat.position()).normalize().scale(5.0D);
                Vec3 targetPos = crawler.position().add(fleeDir);
                crawler.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, 1.4D);
            }
        }

        @Override
        public boolean canContinueToUse() {
            return threat != null && threat.isAlive() && crawler.fleeTimer > 0;
        }
    }

    public static class BloomCrawlerGrazeGoal extends Goal {
        private final BloomCrawlerEntity crawler;
        private BlockPos targetBlock;

        public BloomCrawlerGrazeGoal(BloomCrawlerEntity crawler) {
            this.crawler = crawler;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (crawler.grazeCooldown > 0 || crawler.isGrazing() || crawler.fleeTimer > 0) {
                return false;
            }

            int radius = 4;
            BlockPos center = crawler.blockPosition();
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -1, -radius), center.offset(radius, 1, radius))) {
                BlockState state = crawler.level().getBlockState(pos);
                if (state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS) || state.is(Blocks.FERN) || state.is(Blocks.DANDELION) ||
                        state.is(Blocks.POPPY) || state.is(Blocks.OXEYE_DAISY) || state.is(Blocks.BROWN_MUSHROOM) || state.is(Blocks.RED_MUSHROOM) ||
                        state.is(Blocks.GRASS_BLOCK)) {
                    this.targetBlock = pos.immutable();
                    return true;
                }
            }
            return false;
        }

        @Override
        public void start() {
            crawler.getNavigation().moveTo(targetBlock.getX() + 0.5D, targetBlock.getY(), targetBlock.getZ() + 0.5D, 1.0D);
        }

        @Override
        public void tick() {
            if (targetBlock == null) return;
            crawler.getLookControl().setLookAt(targetBlock.getX() + 0.5D, targetBlock.getY() + 0.5D, targetBlock.getZ() + 0.5D, 30.0F, 30.0F);

            if (crawler.blockPosition().distSqr(targetBlock) <= 2.25D) {
                crawler.setGrazing(true);
                crawler.grazeTimer = 60; // 3 seconds graze animation
                crawler.grazeCooldown = crawler.level().isRaining() ? 100 : 200; // Halved cooldown in rain

                // Eat the block
                BlockState state = crawler.level().getBlockState(targetBlock);
                if (state.is(Blocks.GRASS_BLOCK)) {
                    crawler.level().setBlockAndUpdate(targetBlock, Blocks.DIRT.defaultBlockState());
                } else {
                    crawler.level().destroyBlock(targetBlock, false);
                    BlockPos below = targetBlock.below();
                    if (crawler.level().getBlockState(below).is(Blocks.GRASS_BLOCK)) {
                        crawler.level().setBlockAndUpdate(below, Blocks.DIRT.defaultBlockState());
                    }
                }
                crawler.level().playSound(null, targetBlock, SoundEvents.SHEEP_SHEAR, SoundSource.NEUTRAL, 1.0F, 1.0F);
                this.targetBlock = null;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return targetBlock != null && !crawler.isGrazing() && crawler.fleeTimer <= 0;
        }
    }

    public static class BloomCrawlerHerdGoal extends Goal {
        private final BloomCrawlerEntity crawler;
        private BloomCrawlerEntity friend;

        public BloomCrawlerHerdGoal(BloomCrawlerEntity crawler) {
            this.crawler = crawler;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (crawler.fleeTimer > 0) return false;
            List<BloomCrawlerEntity> group = crawler.level().getEntitiesOfClass(BloomCrawlerEntity.class, crawler.getBoundingBox().inflate(16.0D));
            for (BloomCrawlerEntity other : group) {
                if (other != crawler && other.isAlive()) {
                    double distSq = crawler.distanceToSqr(other);
                    if (distSq > 16.0D) { // More than 4 blocks away
                        this.friend = other;
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void tick() {
            if (friend != null) {
                crawler.getNavigation().moveTo(friend.getX(), friend.getY(), friend.getZ(), 0.8D);
            }
        }

        @Override
        public boolean canContinueToUse() {
            return friend != null && friend.isAlive() && crawler.distanceToSqr(friend) > 9.0D && crawler.fleeTimer <= 0;
        }
    }
}
