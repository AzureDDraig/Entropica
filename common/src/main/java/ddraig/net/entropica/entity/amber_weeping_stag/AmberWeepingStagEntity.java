package ddraig.net.entropica.entity.amber_weeping_stag;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AmberWeepingStagEntity extends Animal {

    public static final ResourceKey<Biome> RADIANT_GLAZE_PLAINS = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "radiant_glaze_plains")
    );
    public static final ResourceKey<Biome> OASIS_FLOES = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "oasis_floes")
    );
    private static final ResourceLocation LIQUID_VITAE_RESIN_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "liquid_vitae_resin");

    public static final int RESIN_SIPHON_COOLDOWN_TICKS = 2400;

    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(AmberWeepingStagEntity.class, EntityDataSerializers.INT);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();

    private int resinCooldown = 0;

    public AmberWeepingStagEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.POWDER_SNOW, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_POWDER_SNOW, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 96.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, AmberStagVariant.AMBER.getId());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new PanicGoal(this, 1.4D));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.15D, stack -> stack.is(Items.GLOW_BERRIES) || stack.is(Items.GOLDEN_CARROT) || stack.is(Items.AMETHYST_SHARD), false));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.GLOW_BERRIES) || stack.is(Items.GOLDEN_CARROT) || stack.is(Items.AMETHYST_SHARD);
    }

    public AmberStagVariant getVariant() {
        return AmberStagVariant.byId(this.entityData.get(VARIANT));
    }

    public void setVariant(AmberStagVariant variant) {
        this.entityData.set(VARIANT, variant.getId());
    }

    public int getResinCooldown() {
        return this.resinCooldown;
    }

    public void setResinCooldown(int ticks) {
        this.resinCooldown = Math.max(0, ticks);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("Variant", Codec.INT, this.getVariant().getId());
        output.store("ResinCooldown", Codec.INT, this.resinCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setVariant(AmberStagVariant.byId(input.read("Variant", Codec.INT).orElse(0)));
        this.resinCooldown = input.read("ResinCooldown", Codec.INT).orElse(0);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnType, @Nullable SpawnGroupData spawnGroupData) {
        Holder<Biome> biomeHolder = level.getBiome(this.blockPosition());
        if (biomeHolder.is(RADIANT_GLAZE_PLAINS)) {
            this.setVariant(this.random.nextBoolean() ? AmberStagVariant.SOLAR : AmberStagVariant.FROST);
        } else if (biomeHolder.is(OASIS_FLOES)) {
            this.setVariant(this.random.nextFloat() < 0.70F ? AmberStagVariant.AMBER : AmberStagVariant.FROST);
        } else {
            float temp = biomeHolder.value().getBaseTemperature();
            if (temp < 0.2F) {
                this.setVariant(AmberStagVariant.FROST);
            } else if (temp > 1.0F) {
                this.setVariant(AmberStagVariant.SOLAR);
            } else {
                this.setVariant(AmberStagVariant.AMBER);
            }
        }
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    public static boolean isAmberStagSpawnSurface(BlockState below) {
        if (below.isAir()) {
            return false;
        }
        String path = BuiltInRegistries.BLOCK.getKey(below.getBlock()).getPath();
        return below.is(Blocks.GRASS_BLOCK)
                || below.is(Blocks.SNOW_BLOCK)
                || below.is(Blocks.MUD)
                || below.is(Blocks.MUDDY_MANGROVE_ROOTS)
                || below.is(Blocks.MOSS_BLOCK)
                || below.is(Blocks.DIRT)
                || below.is(Blocks.COARSE_DIRT)
                || below.is(Blocks.SAND)
                || below.is(Blocks.GRAVEL)
                || below.is(Blocks.ICE)
                || below.is(Blocks.PACKED_ICE)
                || below.is(Blocks.BLUE_ICE)
                || below.is(BlockTags.ANIMALS_SPAWNABLE_ON)
                || "radiant_snow_block".equals(path)
                || "vitae_spring_stone".equals(path)
                || "steaming_spring_stone".equals(path)
                || "radiant_glaze_ice".equals(path)
                || "amber_resin_block".equals(path);
    }

    public static boolean checkAmberWeepingStagSpawnRules(
            EntityType<? extends AmberWeepingStagEntity> type,
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
        if (level.getBlockState(pos).blocksMotion()) {
            return false;
        }
        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        if ((below.is(Blocks.SNOW) || below.getBlock() instanceof SnowLayerBlock)
                && level.getBlockState(belowPos.below()).blocksMotion()) {
            belowPos = belowPos.below();
            below = level.getBlockState(belowPos);
        }
        return isAmberStagSpawnSurface(below);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        BlockState below = level.getBlockState(pos.below());
        String path = BuiltInRegistries.BLOCK.getKey(below.getBlock()).getPath();
        if (below.is(Blocks.GRASS_BLOCK)
                || below.is(Blocks.SNOW_BLOCK)
                || below.is(Blocks.MUD)
                || below.is(Blocks.MOSS_BLOCK)
                || "radiant_snow_block".equals(path)
                || "vitae_spring_stone".equals(path)
                || "steaming_spring_stone".equals(path)
                || "radiant_glaze_ice".equals(path)) {
            return 10.0F;
        }
        return super.getWalkTargetValue(pos, level);
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        BlockPos belowPos = this.blockPosition().below();
        BlockState below = level.getBlockState(belowPos);
        if ((below.is(Blocks.SNOW) || below.getBlock() instanceof SnowLayerBlock)
                && level.getBlockState(belowPos.below()).blocksMotion()) {
            belowPos = belowPos.below();
            below = level.getBlockState(belowPos);
        }
        if (isAmberStagSpawnSurface(below)) {
            return true;
        }
        return super.checkSpawnRules(level, spawnReason);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.GLASS_BOTTLE) && !this.isBaby() && this.isAlive()) {
            if (this.resinCooldown <= 0) {
                Optional<Item> resinOpt = ModItems.LIQUID_VITAE_RESIN.isPresent()
                        ? Optional.of(ModItems.LIQUID_VITAE_RESIN.get())
                        : BuiltInRegistries.ITEM.getOptional(LIQUID_VITAE_RESIN_ID);
                if (resinOpt.isPresent()) {
                    ItemStack filled = ItemUtils.createFilledResult(stack, player, new ItemStack(resinOpt.get()));
                    player.setItemInHand(hand, filled);
                    this.resinCooldown = RESIN_SIPHON_COOLDOWN_TICKS;

                    this.level().playSound(
                            null,
                            this.getX(),
                            this.getY() + 1.1D,
                            this.getZ(),
                            SoundEvents.BOTTLE_FILL,
                            SoundSource.PLAYERS,
                            1.0F,
                            1.0F
                    );
                    if (this.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(
                                ParticleTypes.END_ROD,
                                this.getX(),
                                this.getY() + 1.25D,
                                this.getZ(),
                                14,
                                0.35D,
                                0.35D,
                                0.35D,
                                0.03D
                        );
                    }
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void tick() {
        if (this.getTicksFrozen() > 0) {
            this.setTicksFrozen(0);
        }

        super.tick();

        if (!this.level().isClientSide()) {
            if (this.resinCooldown > 0) {
                this.resinCooldown--;
            }
        } else {
            if (this.getDeltaMovement().horizontalDistanceSqr() > 0.0001D) {
                this.idleAnimationState.stop();
                this.walkAnimationState.startIfStopped(this.tickCount);
            } else {
                this.walkAnimationState.stop();
                this.idleAnimationState.startIfStopped(this.tickCount);
            }

            // Weeping tear particles based on variant
            if (this.random.nextFloat() < 0.25F) {
                AmberStagVariant var = this.getVariant();
                double ox = this.getX() + (this.random.nextDouble() - 0.5D) * 0.8D;
                double oy = this.getY() + 1.2D + (this.random.nextDouble() - 0.5D) * 0.4D;
                double oz = this.getZ() + (this.random.nextDouble() - 0.5D) * 0.8D;

                if (var == AmberStagVariant.FROST) {
                    this.level().addParticle(ParticleTypes.SNOWFLAKE, ox, oy, oz, 0, -0.04D, 0);
                } else if (var == AmberStagVariant.SOLAR) {
                    this.level().addParticle(ParticleTypes.WAX_OFF, ox, oy, oz, 0, -0.02D, 0);
                } else {
                    this.level().addParticle(ParticleTypes.DRIPPING_HONEY, ox, oy, oz, 0, -0.03D, 0);
                }
            }
        }
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        AmberWeepingStagEntity child = new AmberWeepingStagEntity((EntityType<? extends Animal>) this.getType(), level);
        if (otherParent instanceof AmberWeepingStagEntity otherStag) {
            child.setVariant(this.random.nextBoolean() ? this.getVariant() : otherStag.getVariant());
        }
        return child;
    }
}
