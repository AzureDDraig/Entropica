package ddraig.net.entropica.entity.starlight_nautilus;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.event.ModEntityEvents;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModEntityTypes;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
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
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * Biome #17 (2.17 Astral Skerries) Signature Mount — Flying Starlight Nautilus (entropica:starlight_nautilus).
 * Buoyant amphibious & star-gliding cephalopod mount that hovers effortlessly over Starlight Ice and
 * swims through Astral Skerries channels, tamed and buffed with Biome-Exclusive Raw Star Fish,
 * and equipped with a Saddle + Mount Armor for 3D flight, underwater diving, and Spacebar Star-Jet Dashes.
 */
public class StarlightNautilusEntity extends TamableAnimal implements PlayerRideableJumping {

    public static final int BUFF_NONE = 0;
    public static final int BUFF_LIFT = 1;
    public static final int BUFF_GLIDE = 2;
    public static final int BUFF_DASH_CD = 3;
    public static final int BUFF_AURA = 4;

    public static final int DASH_DURATION_TICKS = 14;
    public static final int BASE_DASH_COOLDOWN_TICKS = 45;
    public static final int BUFFED_DASH_COOLDOWN_TICKS = 24;
    public static final int NACRE_SHED_COOLDOWN_TICKS = 1200;

    private static final ResourceLocation ARMOR_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "starlight_nautilus_armor");
    private static final ResourceLocation TOUGHNESS_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "starlight_nautilus_armor_toughness");
    private static final ResourceLocation KNOCKBACK_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "starlight_nautilus_armor_knockback_resistance");

    private static final EntityDataAccessor<Boolean> DATA_SADDLED =
            SynchedEntityData.defineId(StarlightNautilusEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ItemStack> DATA_ARMOR_ITEM =
            SynchedEntityData.defineId(StarlightNautilusEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> DATA_DASH_TICKS =
            SynchedEntityData.defineId(StarlightNautilusEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FLIGHT_BUFF_KIND =
            SynchedEntityData.defineId(StarlightNautilusEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FLIGHT_BUFF_TICKS =
            SynchedEntityData.defineId(StarlightNautilusEntity.class, EntityDataSerializers.INT);

    private int nacreCooldown = 0;
    private int dashCooldown = 0;
    private int pendingClientJumpPower = 0;

    private record StarFishProfile(
            float tameChance,
            float healAmount,
            int buffKind,
            int buffTicks,
            boolean bonusNacreRoll
    ) {}

    public StarlightNautilusEntity(EntityType<? extends StarlightNautilusEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 5;
        this.setNoGravity(true);
        this.moveControl = new StarlightNautilusMoveControl(this);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 0.0F);
        this.setPathfindingMalus(PathType.POWDER_SNOW, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_POWDER_SNOW, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 36.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.FLYING_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 28.0D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.TEMPT_RANGE, 14.0D);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        return nav;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SADDLED, false);
        builder.define(DATA_ARMOR_ITEM, ItemStack.EMPTY);
        builder.define(DATA_DASH_TICKS, 0);
        builder.define(DATA_FLIGHT_BUFF_KIND, BUFF_NONE);
        builder.define(DATA_FLIGHT_BUFF_TICKS, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new CalmSeatedHoverGoal(this));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.05D));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.15D, this::isFood, false));
        this.goalSelector.addGoal(4, new FollowOwnerStarflightGoal(this, 1.18D, 9.0F, 3.5F));
        this.goalSelector.addGoal(5, new StarryHoverAndChannelSwimGoal(this));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    // --- Synched State Getters & Setters ---

    public boolean isSaddled() {
        return this.entityData.get(DATA_SADDLED);
    }

    public void setSaddled(boolean saddled) {
        this.entityData.set(DATA_SADDLED, saddled);
    }

    public ItemStack getArmorItem() {
        return this.entityData.get(DATA_ARMOR_ITEM);
    }

    public void setArmorItem(ItemStack stack) {
        ItemStack safe = (stack == null || stack.isEmpty()) ? ItemStack.EMPTY : stack.copyWithCount(1);
        this.entityData.set(DATA_ARMOR_ITEM, safe);
        if (!this.level().isClientSide()) {
            this.applyArmorAttributeModifiers();
        }
    }

    public StarlightNautilusArmorType getArmorType() {
        return StarlightNautilusArmorType.fromItemStack(this.getArmorItem());
    }

    public int getDashTicks() {
        return this.entityData.get(DATA_DASH_TICKS);
    }

    public void setDashTicks(int ticks) {
        this.entityData.set(DATA_DASH_TICKS, Math.max(0, ticks));
    }

    public int getFlightBuffKind() {
        return this.entityData.get(DATA_FLIGHT_BUFF_KIND);
    }

    public int getFlightBuffTicks() {
        return this.entityData.get(DATA_FLIGHT_BUFF_TICKS);
    }

    public void setFlightBuff(int kind, int ticks) {
        if (kind <= BUFF_NONE || ticks <= 0) {
            this.entityData.set(DATA_FLIGHT_BUFF_KIND, BUFF_NONE);
            this.entityData.set(DATA_FLIGHT_BUFF_TICKS, 0);
        } else {
            this.entityData.set(DATA_FLIGHT_BUFF_KIND, kind);
            this.entityData.set(DATA_FLIGHT_BUFF_TICKS, ticks);
        }
    }

    public boolean hasActiveBuff(int kind) {
        return this.getFlightBuffKind() == kind && this.getFlightBuffTicks() > 0;
    }

    private void applyArmorAttributeModifiers() {
        StarlightNautilusArmorType armorType = this.getArmorType();

        AttributeInstance armorAttr = this.getAttribute(Attributes.ARMOR);
        if (armorAttr != null) {
            armorAttr.removeModifier(ARMOR_MODIFIER_ID);
            if (armorType.getArmorBonus() > 0) {
                armorAttr.addTransientModifier(new AttributeModifier(
                        ARMOR_MODIFIER_ID,
                        armorType.getArmorBonus(),
                        AttributeModifier.Operation.ADD_VALUE
                ));
            }
        }

        AttributeInstance toughnessAttr = this.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (toughnessAttr != null) {
            toughnessAttr.removeModifier(TOUGHNESS_MODIFIER_ID);
            if (armorType.getToughnessBonus() > 0.0D) {
                toughnessAttr.addTransientModifier(new AttributeModifier(
                        TOUGHNESS_MODIFIER_ID,
                        armorType.getToughnessBonus(),
                        AttributeModifier.Operation.ADD_VALUE
                ));
            }
        }

        AttributeInstance knockbackAttr = this.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (knockbackAttr != null) {
            knockbackAttr.removeModifier(KNOCKBACK_MODIFIER_ID);
            if (armorType.getKnockbackResistanceBonus() > 0.0D) {
                knockbackAttr.addTransientModifier(new AttributeModifier(
                        KNOCKBACK_MODIFIER_ID,
                        armorType.getKnockbackResistanceBonus(),
                        AttributeModifier.Operation.ADD_VALUE
                ));
            }
        }
    }

    // --- Serialization ---

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("Saddled", Codec.BOOL, this.isSaddled());
        ItemStack armor = this.getArmorItem();
        if (!armor.isEmpty()) {
            output.store("ArmorItem", ItemStack.CODEC, armor);
        }
        output.store("NacreCooldown", Codec.INT, this.nacreCooldown);
        output.store("FlightBuffKind", Codec.INT, this.getFlightBuffKind());
        output.store("FlightBuffTicks", Codec.INT, this.getFlightBuffTicks());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setSaddled(input.read("Saddled", Codec.BOOL).orElse(false));
        this.setArmorItem(input.read("ArmorItem", ItemStack.CODEC).orElse(ItemStack.EMPTY));
        this.nacreCooldown = input.read("NacreCooldown", Codec.INT).orElse(0);
        int kind = input.read("FlightBuffKind", Codec.INT).orElse(BUFF_NONE);
        int ticks = input.read("FlightBuffTicks", Codec.INT).orElse(0);
        this.setFlightBuff(kind, ticks);
    }

    // --- Environmental & Amphibious Immunities ---

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    protected int decreaseAirSupply(int currentAir) {
        return currentAir;
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (this.getAirSupply() < this.getMaxAirSupply()) {
            this.setAirSupply(this.getMaxAirSupply());
        }
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean dismountsUnderwater() {
        return false;
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        this.resetFallDistance();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        // Silent starlight hoverer and buoyant swimmer
    }

    @Override
    public void makeStuckInBlock(BlockState state, Vec3 motionMultiplier) {
        if (state.is(Blocks.POWDER_SNOW) || state.is(Blocks.SNOW) || state.getBlock() instanceof SnowLayerBlock) {
            return;
        }
        super.makeStuckInBlock(state, motionMultiplier);
    }

    // --- Spawn Rules ---

    public static boolean checkStarlightNautilusSpawnRules(
            EntityType<StarlightNautilusEntity> type,
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
        if (level.getFluidState(pos).is(FluidTags.WATER) || level.getFluidState(pos.below()).is(FluidTags.WATER)) {
            return true;
        }
        for (int dy = 1; dy <= 8; dy++) {
            BlockPos belowPos = pos.below(dy);
            BlockState below = level.getBlockState(belowPos);
            if (below.isAir() || below.is(Blocks.SNOW) || below.getBlock() instanceof SnowLayerBlock) {
                continue;
            }
            if (below.getFluidState().is(FluidTags.WATER)) {
                return true;
            }
            String path = BuiltInRegistries.BLOCK.getKey(below.getBlock()).getPath();
            return "starlight_ice".equals(path)
                    || "astral_impactite".equals(path)
                    || below.is(Blocks.BLUE_ICE)
                    || below.is(Blocks.PACKED_ICE)
                    || below.is(Blocks.ICE)
                    || below.is(BlockTags.ICE)
                    || below.is(Blocks.STONE)
                    || "coarse_gravel".equals(path)
                    || ModEntityEvents.isColdOrSkerriesSpawnSurface(level, belowPos, below);
        }
        return false;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        BlockPos pos = this.blockPosition();
        if (level.getFluidState(pos).is(FluidTags.WATER) || level.getFluidState(pos.below()).is(FluidTags.WATER)) {
            return true;
        }
        for (int dy = 1; dy <= 8; dy++) {
            BlockPos belowPos = pos.below(dy);
            BlockState below = level.getBlockState(belowPos);
            if (below.isAir()) {
                continue;
            }
            if (below.getFluidState().is(FluidTags.WATER) || ModEntityEvents.isColdOrSkerriesSpawnSurface(level, belowPos, below)) {
                return true;
            }
        }
        return super.checkSpawnRules(level, spawnReason);
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader level) {
        return level.isUnobstructed(this);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        if (level.getFluidState(pos).is(FluidTags.WATER)) {
            return 10.0F;
        }
        for (int dy = 1; dy <= 6; dy++) {
            BlockState below = level.getBlockState(pos.below(dy));
            if (below.getFluidState().is(FluidTags.WATER)
                    || below.is(BlockTags.ICE)
                    || (ModBlocks.STARLIGHT_ICE.isPresent() && below.is(ModBlocks.STARLIGHT_ICE.get()))) {
                return 9.0F;
            }
        }
        return 2.0F;
    }

    // --- Star-Fish Diet, Taming, Breeding & Equipment Interaction ---

    private static @Nullable StarFishProfile getStarFishProfile(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        Item item = stack.getItem();
        if (ModItems.ASTRAL_LANTERNFISH.isPresent() && item == ModItems.ASTRAL_LANTERNFISH.get()) {
            return new StarFishProfile(1.00F, 8.0F, BUFF_AURA, 4800, false);
        }
        if (ModItems.SUPERNOVA_PUFFER.isPresent() && item == ModItems.SUPERNOVA_PUFFER.get()) {
            return new StarFishProfile(0.75F, 10.0F, BUFF_DASH_CD, 3600, true);
        }
        if (ModItems.STARFALL_RAY.isPresent() && item == ModItems.STARFALL_RAY.get()) {
            return new StarFishProfile(0.50F, 6.0F, BUFF_GLIDE, 3600, false);
        }
        if (ModItems.ZENITH_MOONFISH.isPresent() && item == ModItems.ZENITH_MOONFISH.get()) {
            return new StarFishProfile(0.50F, 6.0F, BUFF_NONE, 0, false);
        }
        if (ModItems.STARWING_FLYING_FISH.isPresent() && item == ModItems.STARWING_FLYING_FISH.get()) {
            return new StarFishProfile(0.40F, 5.0F, BUFF_LIFT, 3600, false);
        }
        if (ModItems.COMET_RIBBONFISH.isPresent() && item == ModItems.COMET_RIBBONFISH.get()) {
            return new StarFishProfile(0.40F, 5.0F, BUFF_DASH_CD, 3600, false);
        }
        if (ModItems.NEBULA_DARTER.isPresent() && item == ModItems.NEBULA_DARTER.get()) {
            return new StarFishProfile(0.33F, 4.0F, BUFF_NONE, 0, false);
        }

        String path = BuiltInRegistries.ITEM.getKey(item).getPath();
        return switch (path) {
            case "astral_lanternfish" -> new StarFishProfile(1.00F, 8.0F, BUFF_AURA, 4800, false);
            case "supernova_puffer" -> new StarFishProfile(0.75F, 10.0F, BUFF_DASH_CD, 3600, true);
            case "starfall_ray" -> new StarFishProfile(0.50F, 6.0F, BUFF_GLIDE, 3600, false);
            case "zenith_moonfish" -> new StarFishProfile(0.50F, 6.0F, BUFF_NONE, 0, false);
            case "starwing_flying_fish" -> new StarFishProfile(0.40F, 5.0F, BUFF_LIFT, 3600, false);
            case "comet_ribbonfish" -> new StarFishProfile(0.40F, 5.0F, BUFF_DASH_CD, 3600, false);
            case "nebula_darter" -> new StarFishProfile(0.33F, 4.0F, BUFF_NONE, 0, false);
            default -> null;
        };
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return getStarFishProfile(stack) != null;
    }

    private boolean isNacreOffering(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.is(Items.AMETHYST_SHARD)) {
            return true;
        }
        if (ModItems.STARDUST_FRAGMENT.isPresent() && stack.is(ModItems.STARDUST_FRAGMENT.get())) {
            return true;
        }
        return "stardust_fragment".equals(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath());
    }

    private void spawnAstralNacre(ServerLevel serverLevel) {
        Optional<Item> nacreOpt = ModItems.ASTRAL_NAUTILUS_NACRE.isPresent()
                ? Optional.of(ModItems.ASTRAL_NAUTILUS_NACRE.get())
                : BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "astral_nautilus_nacre"));
        nacreOpt.ifPresent(nacre -> this.spawnAtLocation(serverLevel, new ItemStack(nacre, 1), 0.35F));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        StarFishProfile fishProfile = getStarFishProfile(stack);

        // 1. Untamed Star-Fish Feeding & Taming
        if (!this.isTame()) {
            if (fishProfile != null) {
                if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                    stack.consume(1, player);
                    this.heal(fishProfile.healAmount());
                    if (fishProfile.buffKind() != BUFF_NONE) {
                        this.setFlightBuff(fishProfile.buffKind(), fishProfile.buffTicks());
                    }
                    if (fishProfile.bonusNacreRoll() && this.random.nextFloat() < 0.50F) {
                        this.spawnAstralNacre(serverLevel);
                    }
                    this.playSound(SoundEvents.CAT_EAT, 0.85F, 1.25F);
                    this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 0.75F, 1.35F);

                    if (this.random.nextFloat() < fishProfile.tameChance()) {
                        this.tame(player);
                        this.navigation.stop();
                        this.setOrderedToSit(false);
                        this.level().broadcastEntityEvent(this, (byte) 7);
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6);
                    }
                }
                return InteractionResult.SUCCESS;
            }
            return super.mobInteract(player, hand);
        }

        // 2. Tamed Star-Fish Feeding (Heal + Flight Buff + Breeding / Baby Growth)
        if (fishProfile != null) {
            boolean needsHeal = this.getHealth() < this.getMaxHealth();
            boolean canBreed = !this.isBaby() && this.getAge() == 0 && this.canFallInLove();
            boolean wantsBuffRefresh = fishProfile.buffKind() != BUFF_NONE
                    && (this.getFlightBuffKind() != fishProfile.buffKind()
                            || this.getFlightBuffTicks() < fishProfile.buffTicks() - 200);

            if (this.isBaby() || needsHeal || canBreed || wantsBuffRefresh) {
                if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                    stack.consume(1, player);
                    this.heal(fishProfile.healAmount());
                    if (fishProfile.buffKind() != BUFF_NONE) {
                        this.setFlightBuff(fishProfile.buffKind(), fishProfile.buffTicks());
                    }
                    if (fishProfile.bonusNacreRoll() && this.random.nextFloat() < 0.50F) {
                        this.spawnAstralNacre(serverLevel);
                    }
                    if (this.isBaby()) {
                        this.ageUp(getSpeedUpSecondsWhenFeeding(-this.getAge()), true);
                    } else if (this.getAge() == 0 && this.canFallInLove()) {
                        this.setInLove(player);
                    }
                    this.playSound(SoundEvents.CAT_EAT, 0.85F, 1.25F);
                    this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 0.85F, 1.35F);
                    serverLevel.sendParticles(
                            ParticleTypes.END_ROD,
                            this.getX(),
                            this.getY() + 0.55D,
                            this.getZ(),
                            10,
                            0.35D,
                            0.30D,
                            0.35D,
                            0.04D
                    );
                }
                return InteractionResult.SUCCESS;
            }
        }

        // 3. Tamed Adult Interactions (Nacre Offering, Shears, Mount Armor, Saddle, Riding & Sit Toggle)
        if (!this.isBaby()) {
            // Amethyst Shard / Stardust Fragment -> Astral Nautilus Nacre shedding (60s cooldown)
            if (this.isNacreOffering(stack) && this.nacreCooldown <= 0) {
                if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                    stack.consume(1, player);
                    this.nacreCooldown = NACRE_SHED_COOLDOWN_TICKS;
                    this.spawnAstralNacre(serverLevel);
                    serverLevel.playSound(
                            null,
                            this.getX(),
                            this.getY() + 0.5D,
                            this.getZ(),
                            SoundEvents.AMETHYST_BLOCK_CHIME,
                            SoundSource.NEUTRAL,
                            1.0F,
                            1.30F
                    );
                    serverLevel.sendParticles(
                            ParticleTypes.END_ROD,
                            this.getX(),
                            this.getY() + 0.55D,
                            this.getZ(),
                            18,
                            0.40D,
                            0.35D,
                            0.40D,
                            0.05D
                    );
                }
                return InteractionResult.SUCCESS;
            }

            // Shears -> remove Mount Armor first, else remove Saddle
            if (stack.is(Items.SHEARS)) {
                if (!this.getArmorItem().isEmpty()) {
                    if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                        ItemStack removedArmor = this.getArmorItem().copy();
                        this.setArmorItem(ItemStack.EMPTY);
                        if (!player.addItem(removedArmor)) {
                            this.spawnAtLocation(serverLevel, removedArmor, 0.35F);
                        }
                        this.playSound(SoundEvents.SHEARS_SNIP, 1.0F, 1.0F);
                        stack.hurtAndBreak(1, player, getEquipmentSlotForItem(stack));
                    }
                    return InteractionResult.SUCCESS;
                } else if (this.isSaddled() && !this.isVehicle()) {
                    if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                        this.setSaddled(false);
                        ItemStack removedSaddle = new ItemStack(Items.SADDLE);
                        if (!player.addItem(removedSaddle)) {
                            this.spawnAtLocation(serverLevel, removedSaddle, 0.35F);
                        }
                        this.playSound(SoundEvents.SHEARS_SNIP, 1.0F, 1.0F);
                        stack.hurtAndBreak(1, player, getEquipmentSlotForItem(stack));
                    }
                    return InteractionResult.SUCCESS;
                }
            }

            // Mount Armor -> equip or swap into DATA_ARMOR_ITEM
            StarlightNautilusArmorType incomingArmor = StarlightNautilusArmorType.fromItemStack(stack);
            if (incomingArmor != StarlightNautilusArmorType.NONE) {
                if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                    ItemStack previousArmor = this.getArmorItem();
                    ItemStack equippedCopy = stack.copyWithCount(1);
                    stack.consume(1, player);
                    if (!previousArmor.isEmpty()) {
                        if (!player.addItem(previousArmor.copy())) {
                            this.spawnAtLocation(serverLevel, previousArmor.copy(), 0.35F);
                        }
                    }
                    this.setArmorItem(equippedCopy);
                    this.playSound(SoundEvents.ARMOR_EQUIP_DIAMOND.value(), 1.0F, 1.10F);
                }
                return InteractionResult.SUCCESS;
            }

            // Saddle -> equip Saddle
            if (stack.is(Items.SADDLE) && !this.isSaddled()) {
                if (!this.level().isClientSide()) {
                    stack.consume(1, player);
                    this.setSaddled(true);
                    this.playSound(SoundEvents.HORSE_SADDLE.value(), 0.90F, 1.05F);
                }
                return InteractionResult.SUCCESS;
            }

            // Sneaking with empty hand (or owner sneaking) -> toggle calm seated hover
            if (player.isSecondaryUseActive() && this.isOwnedBy(player)) {
                if (!this.level().isClientSide()) {
                    this.setOrderedToSit(!this.isOrderedToSit());
                    this.navigation.stop();
                    this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 0.65F, this.isOrderedToSit() ? 0.95F : 1.25F);
                }
                return InteractionResult.SUCCESS;
            }

            // Mount when saddled and not sneaking
            if (this.isSaddled() && !player.isSecondaryUseActive() && !this.isVehicle()) {
                if (!this.level().isClientSide()) {
                    this.setOrderedToSit(false);
                    this.navigation.stop();
                    player.setYRot(this.getYRot());
                    player.setXRot(this.getXRot());
                    player.startRiding(this);
                }
                return InteractionResult.SUCCESS;
            }
        }

        // Fallback owner sit toggle for unsaddled or baby Nautilus
        if (this.isOwnedBy(player) && !this.isVehicle()) {
            if (!this.level().isClientSide()) {
                this.setOrderedToSit(!this.isOrderedToSit());
                this.navigation.stop();
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        StarlightNautilusEntity baby = ModEntityTypes.STARLIGHT_NAUTILUS.get().create(level, EntitySpawnReason.BREEDING);
        if (baby != null && this.isTame()) {
            LivingEntity owner = this.getOwner();
            if (owner instanceof Player player) {
                baby.tame(player);
            }
        }
        this.spawnAstralNacre(level);
        level.playSound(
                null,
                this.getX(),
                this.getY() + 0.5D,
                this.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.NEUTRAL,
                0.95F,
                1.35F
        );
        return baby;
    }

    @Override
    protected void dropEquipment(ServerLevel serverLevel) {
        super.dropEquipment(serverLevel);
        if (this.isSaddled()) {
            this.spawnAtLocation(serverLevel, new ItemStack(Items.SADDLE));
            this.setSaddled(false);
        }
        ItemStack armor = this.getArmorItem();
        if (!armor.isEmpty()) {
            this.spawnAtLocation(serverLevel, armor.copy());
            this.setArmorItem(ItemStack.EMPTY);
        }
    }

    // --- Ridden 3D Air Flight, Underwater Swim & Spacebar Star-Jet Dash ---

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        if (this.isSaddled() && this.getFirstPassenger() instanceof Player player) {
            return player;
        }
        return super.getControllingPassenger();
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float partialTick) {
        return new Vec3(0.0D, dimensions.height() * 0.88D, 0.05D);
    }

    @Override
    public boolean canJump() {
        return this.isSaddled() && this.dashCooldown <= 0;
    }

    @Override
    public int getJumpCooldown() {
        return this.dashCooldown;
    }

    @Override
    public void onPlayerJump(int power) {
        if (!this.isSaddled() || this.dashCooldown > 0 || power <= 0) {
            return;
        }
        int clampedPower = Mth.clamp(power, 1, 100);
        this.pendingClientJumpPower = clampedPower;
        this.dashCooldown = this.hasActiveBuff(BUFF_DASH_CD)
                ? BUFFED_DASH_COOLDOWN_TICKS
                : BASE_DASH_COOLDOWN_TICKS;
    }

    @Override
    public void handleStartJump(int power) {
        if (!this.isSaddled() || power <= 0) {
            return;
        }
        int clampedPower = Mth.clamp(power, 1, 100);
        this.dashCooldown = this.hasActiveBuff(BUFF_DASH_CD)
                ? BUFFED_DASH_COOLDOWN_TICKS
                : BASE_DASH_COOLDOWN_TICKS;
        this.setDashTicks(DASH_DURATION_TICKS);
        this.gameEvent(GameEvent.ENTITY_ACTION);

        if (!this.isClientAuthoritative() && this.getControllingPassenger() instanceof Player rider) {
            double boost = 0.85D + 0.95D * (clampedPower / 100.0D);
            Vec3 look = rider.getLookAngle();
            this.setDeltaMovement(this.getDeltaMovement().scale(0.35D).add(look.scale(boost)));
            this.hasImpulse = true;
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(
                    null,
                    this.getX(),
                    this.getY() + 0.5D,
                    this.getZ(),
                    SoundEvents.TRIDENT_RIPTIDE_1.value(),
                    SoundSource.NEUTRAL,
                    0.95F,
                    1.20F
            );
            serverLevel.playSound(
                    null,
                    this.getX(),
                    this.getY() + 0.5D,
                    this.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.NEUTRAL,
                    1.0F,
                    1.32F
            );
            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    this.getX(),
                    this.getY() + 0.45D,
                    this.getZ(),
                    18,
                    0.35D,
                    0.30D,
                    0.35D,
                    0.08D
            );
        }
    }

    @Override
    public void handleStopJump() {
    }

    @Override
    protected void tickRidden(Player player, Vec3 travelVector) {
        super.tickRidden(player, travelVector);
        float targetYaw = player.getYRot();
        float targetPitch = player.getXRot() * 0.5F;
        this.setYRot(Mth.rotLerp(0.38F, this.getYRot(), targetYaw));
        this.setXRot(Mth.lerp(0.35F, this.getXRot(), targetPitch));
        this.yRotO = this.getYRot();
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();

        if (this.pendingClientJumpPower > 0) {
            double boost = 0.85D + 0.95D * (this.pendingClientJumpPower / 100.0D);
            Vec3 look = player.getLookAngle();
            double liftFactor = (this.hasActiveBuff(BUFF_LIFT) && look.y > 0.0D) ? 1.30D : 1.0D;
            Vec3 dashVec = new Vec3(look.x * boost, look.y * boost * liftFactor, look.z * boost);
            this.setDeltaMovement(this.getDeltaMovement().scale(0.35D).add(dashVec));
            this.hasImpulse = true;
            this.pendingClientJumpPower = 0;
        }
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 travelVector) {
        float xxa = player.xxa * 0.65F;
        float zza = player.zza;
        if (zza < 0.0F) {
            zza *= 0.55F;
        }
        float yInput = 0.0F;
        if (Math.abs(zza) > 1.0E-4F) {
            yInput = -Mth.sin(player.getXRot() * Mth.DEG_TO_RAD) * zza;
            if (this.hasActiveBuff(BUFF_LIFT)) {
                yInput *= 1.30F;
            }
        }
        return new Vec3(xxa, yInput, zza);
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        float base = (float) this.getAttributeValue(Attributes.FLYING_SPEED);
        if (this.hasActiveBuff(BUFF_GLIDE)) {
            base *= 1.25F;
        }
        return base;
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isDeadOrDying()) {
            super.travel(travelVector);
            return;
        }

        if (this.isVehicle() && this.getControllingPassenger() instanceof Player) {
            float speed = (float) this.getAttributeValue(Attributes.FLYING_SPEED);
            float horizMult = this.hasActiveBuff(BUFF_GLIDE) ? 1.25F : 1.0F;
            float horizAccel = speed * (this.isInWater() ? 0.25F : 0.28F) * horizMult;
            double vertAccel = speed * (this.isInWater() ? 0.22D : 0.25D);

            if (travelVector.horizontalDistanceSqr() > 1.0E-6D || Math.abs(travelVector.y) > 1.0E-6D) {
                this.moveRelative(horizAccel, new Vec3(travelVector.x, 0.0D, travelVector.z));
                Vec3 cur = this.getDeltaMovement();
                this.setDeltaMovement(cur.x, cur.y + travelVector.y * vertAccel, cur.z);
            }

            this.move(MoverType.SELF, this.getDeltaMovement());

            double damping = this.getDashTicks() > 0 ? 0.90D : 0.85D;
            this.setDeltaMovement(this.getDeltaMovement().scale(damping));
            this.calculateEntityAnimation(false);
            return;
        }

        if (this.isEffectiveAi()) {
            float accel = this.isInWater() ? 0.045F : 0.05F;
            double damp = this.isInWater() ? 0.88D : 0.86D;
            this.moveRelative(accel, travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(damp));
            this.calculateEntityAnimation(false);
            return;
        }

        super.travel(travelVector);
    }

    // --- Tick & Aura / Hover Maintenance ---

    @Override
    public void tick() {
        if (this.getTicksFrozen() > 0) {
            this.setTicksFrozen(0);
        }
        this.setNoGravity(this.isAlive());

        if (this.dashCooldown > 0) {
            this.dashCooldown--;
        }

        super.tick();

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            if (this.nacreCooldown > 0) {
                this.nacreCooldown--;
            }

            int curDash = this.getDashTicks();
            if (curDash > 0) {
                this.setDashTicks(curDash - 1);
                Vec3 back = this.getLookAngle().scale(-0.55D);
                serverLevel.sendParticles(
                        ParticleTypes.END_ROD,
                        this.getX() + back.x,
                        this.getY() + 0.45D + back.y,
                        this.getZ() + back.z,
                        3,
                        0.22D,
                        0.18D,
                        0.22D,
                        0.02D
                );
            }

            int curBuffTicks = this.getFlightBuffTicks();
            if (curBuffTicks > 0) {
                int nextTicks = curBuffTicks - 1;
                if (nextTicks <= 0) {
                    this.setFlightBuff(BUFF_NONE, 0);
                } else {
                    this.entityData.set(DATA_FLIGHT_BUFF_TICKS, nextTicks);
                }
            }

            if (this.isAlive()) {
                // Astral Slate Armor passive night regeneration (+1.0F HP every 80 ticks at night)
                if (this.getArmorType() == StarlightNautilusArmorType.ASTRAL_SLATE
                        && serverLevel.isDarkOutside()
                        && this.tickCount % 80 == 0
                        && this.getHealth() < this.getMaxHealth()) {
                    this.heal(1.0F);
                }

                // Breath of the Starlight Nautilus: refresh Water Breathing while ridden in water
                if (this.getFirstPassenger() instanceof Player rider
                        && (this.isInWater() || rider.isEyeInFluid(FluidTags.WATER))) {
                    rider.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 60, 0, true, false, true), this);
                }

                // Astral Lanternfish 16-block Starlight Grace Aura pulse
                if (this.hasActiveBuff(BUFF_AURA) && (this.tickCount + this.getId()) % 40 == 0) {
                    if (ModEffects.STARLIGHT_GRACE.isBound()) {
                        List<Player> nearbyPlayers = serverLevel.getEntitiesOfClass(
                                Player.class,
                                this.getBoundingBox().inflate(16.0D),
                                p -> p.isAlive() && !p.isSpectator() && this.distanceToSqr(p) <= 256.0D
                        );
                        for (Player p : nearbyPlayers) {
                            p.addEffect(new MobEffectInstance(ModEffects.STARLIGHT_GRACE, 120, 0, true, false, true), this);
                        }
                    }
                    serverLevel.sendParticles(
                            ParticleTypes.END_ROD,
                            this.getX(),
                            this.getY() + 0.55D,
                            this.getZ(),
                            6,
                            0.55D,
                            0.35D,
                            0.55D,
                            0.02D
                    );
                }

                // Gentle unmounted starry hover altitude regulation
                if (!this.isVehicle() && !this.isInWater() && !this.isOrderedToSit()) {
                    this.maintainUnmountedStarryHover();
                }
            }
        } else if (this.level().isClientSide() && this.isAlive()) {
            int interval = (this.getDashTicks() > 0) ? 1 : (this.hasActiveBuff(BUFF_AURA) ? 5 : 14);
            if (this.tickCount % interval == 0) {
                this.level().addParticle(
                        ParticleTypes.END_ROD,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.75D,
                        this.getY() + 0.25D + this.random.nextDouble() * 0.55D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.75D,
                        0.0D,
                        -0.005D,
                        0.0D
                );
            }
        }
    }

    double findSurfaceBelow(int maxDepth) {
        BlockPos origin = this.blockPosition();
        for (int dy = 0; dy <= maxDepth; dy++) {
            BlockPos check = origin.below(dy);
            BlockState state = this.level().getBlockState(check);
            if (state.isAir() || state.is(Blocks.SNOW) || state.getBlock() instanceof SnowLayerBlock) {
                continue;
            }
            if (!state.getFluidState().isEmpty() || state.blocksMotion()) {
                return check.getY() + 1.0D;
            }
        }
        return Double.NaN;
    }

    private void maintainUnmountedStarryHover() {
        double surfaceY = this.findSurfaceBelow(14);
        Vec3 vel = this.getDeltaMovement();
        if (Double.isNaN(surfaceY)) {
            this.setDeltaMovement(vel.x, Mth.clamp(vel.y - 0.02D, -0.08D, 0.04D), vel.z);
            return;
        }
        double heightAbove = this.getY() - surfaceY;
        double bob = Math.sin(this.tickCount * 0.06D + this.getId() * 0.7D) * 0.25D;
        if (heightAbove < 1.5D) {
            double lift = Mth.clamp((1.85D + bob - heightAbove) * 0.09D, 0.02D, 0.075D);
            this.setDeltaMovement(vel.x, Mth.lerp(0.25D, vel.y, lift), vel.z);
        } else if (heightAbove > 6.2D) {
            double sink = Mth.clamp((5.2D + bob - heightAbove) * 0.06D, -0.07D, -0.015D);
            this.setDeltaMovement(vel.x, Mth.lerp(0.22D, vel.y, sink), vel.z);
        }
    }

    // --- Soundscape ---

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.isInWater() ? SoundEvents.GLOW_SQUID_AMBIENT : SoundEvents.AMETHYST_BLOCK_CHIME;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.GLOW_SQUID_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GLOW_SQUID_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.65F;
    }

    @Override
    public float getVoicePitch() {
        return (this.isBaby() ? 1.45F : 1.15F) + (this.random.nextFloat() - 0.5F) * 0.16F;
    }

    // --- Custom 3D MoveControl & AI Goals ---

    static class StarlightNautilusMoveControl extends MoveControl {
        private final StarlightNautilusEntity nautilus;

        StarlightNautilusMoveControl(StarlightNautilusEntity nautilus) {
            super(nautilus);
            this.nautilus = nautilus;
        }

        @Override
        public void tick() {
            if (this.operation != Operation.MOVE_TO || this.nautilus.isVehicle() || this.nautilus.isDeadOrDying()) {
                return;
            }
            this.operation = Operation.WAIT;

            Vec3 diff = new Vec3(
                    this.wantedX - this.nautilus.getX(),
                    this.wantedY - this.nautilus.getY(),
                    this.wantedZ - this.nautilus.getZ()
            );
            double dist = diff.length();
            if (dist < 0.08D) {
                return;
            }

            double horizDist = Math.hypot(diff.x, diff.z);
            if (horizDist > 1.0E-4D) {
                float targetYaw = (float) (Mth.atan2(diff.z, diff.x) * (180.0D / Math.PI)) - 90.0F;
                this.nautilus.setYRot(this.rotlerp(this.nautilus.getYRot(), targetYaw, 10.0F));
                this.nautilus.yBodyRot = this.nautilus.getYRot();
            }

            double baseSpeed = (this.nautilus.isInWater()
                    ? this.nautilus.getAttributeValue(Attributes.MOVEMENT_SPEED)
                    : this.nautilus.getAttributeValue(Attributes.FLYING_SPEED)) * this.speedModifier;
            Vec3 dir = diff.scale(1.0D / dist);
            Vec3 next = this.nautilus.getDeltaMovement().add(dir.scale(baseSpeed * 0.18D));
            double maxSpd = baseSpeed * 0.95D;
            if (next.lengthSqr() > maxSpd * maxSpd) {
                next = next.normalize().scale(maxSpd);
            }
            this.nautilus.setDeltaMovement(next);
        }
    }

    /**
     * When ordered to sit, hovers calmly in place ~0.8 blocks above the surface below.
     */
    static class CalmSeatedHoverGoal extends Goal {
        private final StarlightNautilusEntity nautilus;

        CalmSeatedHoverGoal(StarlightNautilusEntity nautilus) {
            this.nautilus = nautilus;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return this.nautilus.isTame()
                    && !this.nautilus.isVehicle()
                    && this.nautilus.isOrderedToSit();
        }

        @Override
        public void start() {
            this.nautilus.getNavigation().stop();
        }

        @Override
        public void tick() {
            this.nautilus.getNavigation().stop();
            Vec3 vel = this.nautilus.getDeltaMovement();
            double surfaceY = this.nautilus.findSurfaceBelow(12);
            double nextVy = vel.y * 0.75D;
            if (!Double.isNaN(surfaceY)) {
                double desiredY = surfaceY + 0.8D + Math.sin(this.nautilus.tickCount * 0.05D) * 0.06D;
                double diff = desiredY - this.nautilus.getY();
                nextVy = Mth.clamp(diff * 0.12D, -0.06D, 0.06D);
            }
            this.nautilus.setDeltaMovement(vel.x * 0.78D, nextVy, vel.z * 0.78D);
        }
    }

    /**
     * Gentle 3D following behavior for tamed Starlight Nautilus when unmounted and not ordered to sit.
     */
    static class FollowOwnerStarflightGoal extends Goal {
        private final StarlightNautilusEntity nautilus;
        private final double speedModifier;
        private final float startDist;
        private final float stopDist;
        private @Nullable LivingEntity owner;
        private int recalcTicker = 0;

        FollowOwnerStarflightGoal(StarlightNautilusEntity nautilus, double speedModifier, float startDist, float stopDist) {
            this.nautilus = nautilus;
            this.speedModifier = speedModifier;
            this.startDist = startDist;
            this.stopDist = stopDist;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.nautilus.isTame() || this.nautilus.isOrderedToSit() || this.nautilus.isVehicle() || this.nautilus.isLeashed()) {
                return false;
            }
            LivingEntity candidate = this.nautilus.getOwner();
            if (candidate == null || candidate.isSpectator()) {
                return false;
            }
            if (this.nautilus.distanceToSqr(candidate) < (double) (this.startDist * this.startDist)) {
                return false;
            }
            this.owner = candidate;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.owner == null || !this.owner.isAlive() || this.nautilus.isOrderedToSit() || this.nautilus.isVehicle()) {
                return false;
            }
            return this.nautilus.distanceToSqr(this.owner) > (double) (this.stopDist * this.stopDist);
        }

        @Override
        public void start() {
            this.recalcTicker = 0;
        }

        @Override
        public void stop() {
            this.owner = null;
            this.nautilus.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.owner == null) {
                return;
            }
            this.nautilus.getLookControl().setLookAt(this.owner, 10.0F, (float) this.nautilus.getMaxHeadXRot());
            if (--this.recalcTicker > 0) {
                return;
            }
            this.recalcTicker = 10;
            double distSqr = this.nautilus.distanceToSqr(this.owner);
            if (distSqr >= 784.0D && this.owner.level() == this.nautilus.level()) {
                BlockPos targetBase = this.owner.blockPosition().above(2);
                if (this.nautilus.level().isEmptyBlock(targetBase) && this.nautilus.level().isEmptyBlock(targetBase.above())) {
                    this.nautilus.snapTo(
                            targetBase.getX() + 0.5D,
                            targetBase.getY(),
                            targetBase.getZ() + 0.5D,
                            this.nautilus.getYRot(),
                            this.nautilus.getXRot()
                    );
                    this.nautilus.getNavigation().stop();
                    return;
                }
            }
            double targetY = this.owner.isInWater() ? this.owner.getY() + 0.5D : this.owner.getY() + 1.8D;
            this.nautilus.getMoveControl().setWantedPosition(this.owner.getX(), targetY, this.owner.getZ(), this.speedModifier);
        }
    }

    /**
     * Gentle 3D starry hovering (1.5–6.0 blocks above Starlight Ice, ground, or water)
     * and graceful underwater swimming in Astral Skerries channels.
     */
    static class StarryHoverAndChannelSwimGoal extends Goal {
        private final StarlightNautilusEntity nautilus;
        private @Nullable Vec3 waypoint;
        private int driftTicks = 0;

        StarryHoverAndChannelSwimGoal(StarlightNautilusEntity nautilus) {
            this.nautilus = nautilus;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.nautilus.isVehicle() || this.nautilus.isOrderedToSit()) {
                return false;
            }
            if (this.nautilus.getRandom().nextInt(28) != 0) {
                return false;
            }
            this.waypoint = this.pickWaypoint();
            return this.waypoint != null;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.nautilus.isVehicle()
                    && !this.nautilus.isOrderedToSit()
                    && this.waypoint != null
                    && this.driftTicks > 0
                    && this.nautilus.position().distanceToSqr(this.waypoint) > 1.25D;
        }

        @Override
        public void start() {
            this.driftTicks = 90 + this.nautilus.getRandom().nextInt(80);
        }

        @Override
        public void stop() {
            this.waypoint = null;
            this.driftTicks = 0;
        }

        @Override
        public void tick() {
            this.driftTicks--;
            if (this.waypoint != null) {
                this.nautilus.getMoveControl().setWantedPosition(this.waypoint.x, this.waypoint.y, this.waypoint.z, 0.85D);
            }
        }

        private @Nullable Vec3 pickWaypoint() {
            RandomSource rand = this.nautilus.getRandom();
            Level level = this.nautilus.level();
            boolean trySwim = this.nautilus.isInWater() ? rand.nextFloat() < 0.65F : rand.nextFloat() < 0.25F;

            for (int attempt = 0; attempt < 10; attempt++) {
                double dx = (rand.nextDouble() - 0.5D) * 16.0D;
                double dz = (rand.nextDouble() - 0.5D) * 16.0D;
                int tx = Mth.floor(this.nautilus.getX() + dx);
                int tz = Mth.floor(this.nautilus.getZ() + dz);

                if (trySwim) {
                    int ty = Mth.floor(this.nautilus.getY() + (rand.nextDouble() - 0.5D) * 6.0D);
                    BlockPos waterCheck = new BlockPos(tx, ty, tz);
                    if (level.getFluidState(waterCheck).is(FluidTags.WATER)) {
                        return new Vec3(tx + 0.5D, ty + 0.5D, tz + 0.5D);
                    }
                }

                // Find surface near (tx, currentY, tz) and hover 1.5–6.0 blocks above it
                int startY = Mth.floor(this.nautilus.getY()) + 4;
                for (int dy = 0; dy <= 16; dy++) {
                    BlockPos check = new BlockPos(tx, startY - dy, tz);
                    BlockState state = level.getBlockState(check);
                    if (state.isAir() || state.is(Blocks.SNOW) || state.getBlock() instanceof SnowLayerBlock) {
                        continue;
                    }
                    if (!state.getFluidState().isEmpty() || state.blocksMotion()) {
                        double hoverAlt = 1.5D + rand.nextDouble() * 4.5D;
                        double targetY = check.getY() + 1.0D + hoverAlt;
                        BlockPos airPos = BlockPos.containing(tx + 0.5D, targetY, tz + 0.5D);
                        if (level.isEmptyBlock(airPos)) {
                            return new Vec3(tx + 0.5D, targetY, tz + 0.5D);
                        }
                        break;
                    }
                }
            }
            return null;
        }
    }
}
