package ddraig.net.entropica.entity.mud_lurker_salamander;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
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
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Fauna 33 — Mud-Lurker Salamander (Amphibious Crawler) (entropica:mud_lurker_salamander).
 * Neutral thermal-mud & volcanic amphibian native to 2.14 Oasis Floes, Geothermal Mires, and Nether Magma Shores:
 * - Two 128x128 variants: OVERWORLD_MUD (0, "mud") and NETHER_MAGMA (1, "nether").
 * - Crawls along muddy/volcanic banks with a sprawled diagonal gait and tucks all four limbs sleekly
 *   against its flanks when diving into shallow oasis pools or Nether lava pools.
 * - Regenerates 1.0 HP/sec when touching water, mud, vitae_spring_stone, or lava (Nether variant).
 * - Coiled Tail-Whip Strike when provoked: 6.0 damage + strong knockback (+ 3s ignition for Nether variant).
 * - Peaceful Shedding: right-clicking an unprovoked salamander with Slime Ball, Kelp, Tropical Fish,
 *   Magma Cream, or Amber Chunk heals 4.0 HP and sheds 1x Waterproof Salamander Skin on a 2400-tick cooldown.
 */
public class MudLurkerSalamanderEntity extends PathfinderMob {

    public static final int SHED_COOLDOWN_TICKS = 2400;

    public static final int LAND_ATTACK_DURATION_TICKS = 21;  // 1.05s in mud_lurker_salamander.bbmodel
    public static final int SWIM_ATTACK_DURATION_TICKS = 19;  // 0.95s in mud_lurker_salamander.bbmodel
    public static final int ENTER_WATER_DURATION_TICKS = 20;  // 1.00s in mud_lurker_salamander.bbmodel
    public static final int EXIT_WATER_DURATION_TICKS = 22;   // 1.10s in mud_lurker_salamander.bbmodel

    private static final byte EVENT_LAND_ATTACK = 4;
    private static final byte EVENT_SWIM_ATTACK = 61;
    private static final byte EVENT_ENTER_WATER = 62;
    private static final byte EVENT_EXIT_WATER = 63;

    private static final ResourceLocation WATERPROOF_SALAMANDER_SKIN_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "waterproof_salamander_skin");
    private static final ResourceLocation AMBER_CHUNK_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "amber_chunk");
    private static final ResourceLocation VITAE_SPRING_STONE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vitae_spring_stone");
    private static final ResourceLocation STEAMING_SPRING_STONE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "steaming_spring_stone");

    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(MudLurkerSalamanderEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SHED_COOLDOWN =
            SynchedEntityData.defineId(MudLurkerSalamanderEntity.class, EntityDataSerializers.INT);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState enterWaterAnimationState = new AnimationState();
    public final AnimationState swimIdleAnimationState = new AnimationState();
    public final AnimationState swimAnimationState = new AnimationState();
    public final AnimationState swimAttackAnimationState = new AnimationState();
    public final AnimationState exitWaterAnimationState = new AnimationState();

    private boolean wasInFluid = false;
    private boolean fluidStateInitialized = false;
    private boolean clientWasInFluid = false;
    private boolean clientFluidInitialized = false;

    public MudLurkerSalamanderEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 4;
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 0.0F);
        this.setPathfindingMalus(PathType.POWDER_SNOW, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_POWDER_SNOW, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 36.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 20.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, MudLurkerSalamanderVariant.OVERWORLD_MUD.getId());
        builder.define(SHED_COOLDOWN, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.32D, true));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.15D, MudLurkerSalamanderEntity::isSheddingTreat, false));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 1.0D, 50));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static boolean isSheddingTreat(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.is(Items.SLIME_BALL)
                || stack.is(Items.KELP)
                || stack.is(Items.TROPICAL_FISH)
                || stack.is(Items.MAGMA_CREAM)) {
            return true;
        }
        return BuiltInRegistries.ITEM.getOptional(AMBER_CHUNK_ID)
                .map(stack::is)
                .orElse(false);
    }

    public MudLurkerSalamanderVariant getVariant() {
        return MudLurkerSalamanderVariant.byId(this.entityData.get(VARIANT));
    }

    public void setVariant(MudLurkerSalamanderVariant variant) {
        MudLurkerSalamanderVariant safe = variant != null ? variant : MudLurkerSalamanderVariant.OVERWORLD_MUD;
        this.entityData.set(VARIANT, safe.getId());
        this.refreshVariantPathfinding(safe);
    }

    private void refreshVariantPathfinding(MudLurkerSalamanderVariant variant) {
        if (variant == MudLurkerSalamanderVariant.NETHER_MAGMA) {
            this.setPathfindingMalus(PathType.LAVA, 0.0F);
            this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
            this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
        } else {
            this.setPathfindingMalus(PathType.LAVA, 8.0F);
            this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
            this.setPathfindingMalus(PathType.DAMAGE_FIRE, 8.0F);
        }
    }

    public boolean isNetherVariant() {
        return this.getVariant() == MudLurkerSalamanderVariant.NETHER_MAGMA;
    }

    public int getShedCooldown() {
        return this.entityData.get(SHED_COOLDOWN);
    }

    public void setShedCooldown(int ticks) {
        this.entityData.set(SHED_COOLDOWN, Math.max(0, ticks));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("Variant", Codec.INT, this.getVariant().getId());
        output.store("ShedCooldown", Codec.INT, this.getShedCooldown());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setVariant(MudLurkerSalamanderVariant.byId(input.read("Variant", Codec.INT).orElse(0)));
        this.setShedCooldown(input.read("ShedCooldown", Codec.INT).orElse(0));
    }

    private static boolean shouldSelectNetherVariant(ServerLevelAccessor level, BlockPos pos) {
        if (level.getLevel().dimension() == Level.NETHER) {
            return true;
        }
        Holder<Biome> biomeHolder = level.getBiome(pos);
        if (biomeHolder.is(BiomeTags.IS_NETHER)) {
            return true;
        }
        String biomePath = biomeHolder.unwrapKey().map(k -> k.location().getPath()).orElse("");
        return biomePath.contains("nether")
                || biomePath.contains("basalt_delta")
                || biomePath.contains("soul_sand")
                || biomePath.contains("crimson")
                || biomePath.contains("warped")
                || biomePath.contains("brimstone")
                || biomePath.contains("magma_cavern");
    }

    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason spawnType,
            @Nullable SpawnGroupData spawnGroupData
    ) {
        if (shouldSelectNetherVariant(level, this.blockPosition())) {
            this.setVariant(MudLurkerSalamanderVariant.NETHER_MAGMA);
        } else {
            this.setVariant(MudLurkerSalamanderVariant.OVERWORLD_MUD);
        }
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    public boolean fireImmune() {
        return this.isNetherVariant() || super.fireImmune();
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        if (source.is(DamageTypes.HOT_FLOOR)) {
            return true;
        }
        if (this.isNetherVariant() && (source.is(DamageTypeTags.IS_FIRE)
                || source.is(DamageTypes.LAVA)
                || source.is(DamageTypes.IN_FIRE)
                || source.is(DamageTypes.ON_FIRE))) {
            return true;
        }
        return super.isInvulnerableTo(level, source);
    }

    @Override
    public float getPathfindingMalus(PathType pathType) {
        if (pathType == PathType.WATER || pathType == PathType.WATER_BORDER) {
            return 0.0F;
        }
        if (this.isNetherVariant() && (pathType == PathType.LAVA
                || pathType == PathType.DANGER_FIRE
                || pathType == PathType.DAMAGE_FIRE)) {
            return 0.0F;
        }
        return super.getPathfindingMalus(pathType);
    }

    @Override
    protected float getBlockSpeedFactor() {
        return Math.max(1.0F, super.getBlockSpeedFactor());
    }

    public boolean isInFluidMedium() {
        return this.isInWater() || (this.isNetherVariant() && this.isInLava());
    }

    private static boolean isVitaeOrSteamingSpringStone(BlockState state) {
        if (state.isAir()) {
            return false;
        }
        return BuiltInRegistries.BLOCK.getOptional(VITAE_SPRING_STONE_ID).map(state::is).orElse(false)
                || BuiltInRegistries.BLOCK.getOptional(STEAMING_SPRING_STONE_ID).map(state::is).orElse(false);
    }

    private static boolean isVitaeSpringStone(BlockState state) {
        if (state.isAir()) {
            return false;
        }
        return BuiltInRegistries.BLOCK.getOptional(VITAE_SPRING_STONE_ID).map(state::is).orElse(false);
    }

    public boolean isTouchingRestorativeMedium() {
        if (this.isInWaterOrRain()) {
            return true;
        }
        if (this.isNetherVariant() && this.isInLava()) {
            return true;
        }
        BlockPos pos = this.blockPosition();
        BlockState at = this.level().getBlockState(pos);
        BlockState below = this.level().getBlockState(pos.below());
        return at.is(Blocks.MUD)
                || at.is(Blocks.MUDDY_MANGROVE_ROOTS)
                || below.is(Blocks.MUD)
                || below.is(Blocks.MUDDY_MANGROVE_ROOTS)
                || isVitaeSpringStone(at)
                || isVitaeSpringStone(below);
    }

    public static boolean isValidSalamanderSubstrate(BlockGetter level, BlockPos belowPos, BlockState belowState) {
        FluidState belowFluid = belowState.getFluidState();
        if (belowFluid.is(FluidTags.WATER)) {
            // Shallow water check: solid floor within 2 blocks beneath
            return level.getBlockState(belowPos.below()).blocksMotion()
                    || level.getBlockState(belowPos.below(2)).blocksMotion()
                    || !level.getFluidState(belowPos.above()).is(FluidTags.WATER);
        }
        if (belowState.isAir()) {
            return false;
        }
        return belowState.is(Blocks.GRASS_BLOCK)
                || belowState.is(Blocks.MUD)
                || belowState.is(Blocks.MUDDY_MANGROVE_ROOTS)
                || belowState.is(Blocks.CLAY)
                || belowState.is(Blocks.SAND)
                || belowState.is(Blocks.RED_SAND)
                || belowState.is(Blocks.GRAVEL)
                || belowState.is(Blocks.SNOW_BLOCK)
                || belowState.is(Blocks.STONE)
                || belowState.is(Blocks.DEEPSLATE)
                || belowState.is(Blocks.ANDESITE)
                || belowState.is(Blocks.TUFF)
                || belowState.is(Blocks.BASALT)
                || belowState.is(Blocks.SMOOTH_BASALT)
                || belowState.is(Blocks.POLISHED_BASALT)
                || belowState.is(Blocks.BLACKSTONE)
                || belowState.is(Blocks.GILDED_BLACKSTONE)
                || belowState.is(Blocks.MAGMA_BLOCK)
                || belowState.is(Blocks.NETHERRACK)
                || belowState.is(Blocks.SOUL_SAND)
                || belowState.is(Blocks.SOUL_SOIL)
                || belowState.is(BlockTags.ANIMALS_SPAWNABLE_ON)
                || isVitaeOrSteamingSpringStone(belowState);
    }

    public static boolean checkMudLurkerSalamanderSpawnRules(
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

        BlockState atState = level.getBlockState(pos);
        if (atState.blocksMotion()) {
            return false;
        }

        FluidState atFluid = level.getFluidState(pos);
        if (atFluid.is(FluidTags.WATER)) {
            // Allow spawning in shallow water pools where surface above is open or within 2 blocks
            boolean shallow = !level.getFluidState(pos.above()).is(FluidTags.WATER)
                    || !level.getFluidState(pos.above(2)).is(FluidTags.WATER);
            return shallow && (level.getBlockState(pos.below()).blocksMotion()
                    || isValidSalamanderSubstrate(level, pos.below(), level.getBlockState(pos.below())));
        }

        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        if ((belowState.is(Blocks.SNOW) || belowState.getBlock() instanceof SnowLayerBlock)
                && level.getBlockState(belowPos.below()).blocksMotion()) {
            belowPos = belowPos.below();
            belowState = level.getBlockState(belowPos);
        }

        return isValidSalamanderSubstrate(level, belowPos, belowState);
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        BlockPos pos = this.blockPosition();
        if (level.getFluidState(pos).is(FluidTags.WATER)) {
            return true;
        }
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        if ((belowState.is(Blocks.SNOW) || belowState.getBlock() instanceof SnowLayerBlock)
                && level.getBlockState(belowPos.below()).blocksMotion()) {
            belowPos = belowPos.below();
            belowState = level.getBlockState(belowPos);
        }
        if (isValidSalamanderSubstrate(level, belowPos, belowState)) {
            return true;
        }
        return super.checkSpawnRules(level, spawnReason);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        FluidState fluid = level.getFluidState(pos);
        if (fluid.is(FluidTags.WATER)) {
            return 10.0F;
        }
        if (this.isNetherVariant() && fluid.is(FluidTags.LAVA)) {
            return 10.0F;
        }
        BlockState below = level.getBlockState(pos.below());
        if (below.is(Blocks.MUD)
                || below.is(Blocks.MUDDY_MANGROVE_ROOTS)
                || below.is(Blocks.CLAY)
                || isVitaeOrSteamingSpringStone(below)) {
            return 9.0F;
        }
        if (this.isNetherVariant() && (below.is(Blocks.MAGMA_BLOCK)
                || below.is(Blocks.BLACKSTONE)
                || below.is(Blocks.BASALT))) {
            return 8.5F;
        }
        if (below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.SNOW_BLOCK)) {
            return 6.0F;
        }
        return super.getWalkTargetValue(pos, level);
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isEffectiveAi() && this.isInFluidMedium()) {
            this.moveRelative(0.055F, travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            Vec3 next = this.getDeltaMovement().scale(this.isInLava() ? 0.84D : 0.88D);
            LivingEntity target = this.getTarget();
            if (this.horizontalCollision) {
                next = new Vec3(next.x, 0.24D, next.z);
            } else if (target != null && target.isAlive()) {
                double dy = Mth.clamp((target.getY() - this.getY()) * 0.07D, -0.14D, 0.16D);
                next = new Vec3(next.x, Mth.clamp(next.y + dy * 0.35D, -0.15D, 0.18D), next.z);
            } else if (this.isInLava() && this.getFluidHeight(FluidTags.LAVA) > 0.45D) {
                next = new Vec3(next.x, Math.max(next.y, 0.04D), next.z);
            } else if (travelVector.z > 0.001D && this.level().getFluidState(this.blockPosition().above()).is(FluidTags.WATER)) {
                next = new Vec3(next.x, Mth.clamp(next.y + 0.012D, -0.12D, 0.14D), next.z);
            }
            this.setDeltaMovement(next);
            this.calculateEntityAnimation(false);
            return;
        }
        super.travel(travelVector);
    }

    @Override
    public boolean doHurtTarget(ServerLevel serverLevel, Entity target) {
        boolean inFluid = this.isInFluidMedium();
        serverLevel.broadcastEntityEvent(this, inFluid ? EVENT_SWIM_ATTACK : EVENT_LAND_ATTACK);

        boolean hurt = super.doHurtTarget(serverLevel, target);
        if (hurt) {
            double dx = this.getX() - target.getX();
            double dz = this.getZ() - target.getZ();
            if (target instanceof LivingEntity living) {
                living.knockback(0.95D, dx, dz);
            } else {
                double dist = Math.max(0.001D, Math.hypot(dx, dz));
                target.push((-dx / dist) * 0.75D, 0.22D, (-dz / dist) * 0.75D);
            }
            target.hurtMarked = true;

            if (this.isNetherVariant()) {
                target.igniteForSeconds(3.0F);
                serverLevel.playSound(null, this.getX(), this.getY() + 0.35D, this.getZ(),
                        SoundEvents.BLAZE_SHOOT, SoundSource.NEUTRAL, 0.75F, 1.15F);
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        target.getX(), target.getY() + 0.5D, target.getZ(),
                        14, 0.35D, 0.25D, 0.35D, 0.04D);
                serverLevel.sendParticles(ParticleTypes.LAVA,
                        target.getX(), target.getY() + 0.45D, target.getZ(),
                        5, 0.30D, 0.20D, 0.30D, 0.02D);
            } else {
                serverLevel.playSound(null, this.getX(), this.getY() + 0.35D, this.getZ(),
                        SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.NEUTRAL, 0.85F, 0.90F);
                serverLevel.sendParticles(ParticleTypes.SPLASH,
                        target.getX(), target.getY() + 0.45D, target.getZ(),
                        12, 0.35D, 0.20D, 0.35D, 0.08D);
            }
        }
        return hurt;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.isAlive() && this.getTarget() == null && isSheddingTreat(stack)) {
            boolean canShed = this.getShedCooldown() <= 0;
            boolean needsHeal = this.getHealth() < this.getMaxHealth();
            if (canShed || needsHeal) {
                if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                    stack.consume(1, player);
                    this.heal(4.0F);

                    serverLevel.playSound(null, this.getX(), this.getY() + 0.35D, this.getZ(),
                            SoundEvents.AXOLOTL_IDLE_AIR, SoundSource.NEUTRAL, 0.90F, 1.05F);
                    serverLevel.playSound(null, this.getX(), this.getY() + 0.35D, this.getZ(),
                            SoundEvents.SLIME_SQUISH, SoundSource.NEUTRAL, 0.75F, 1.15F);
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            this.getX(), this.getY() + 0.45D, this.getZ(),
                            10, 0.45D, 0.25D, 0.45D, 0.02D);

                    if (canShed) {
                        this.setShedCooldown(SHED_COOLDOWN_TICKS);
                        Optional<Item> skinOpt = BuiltInRegistries.ITEM.getOptional(WATERPROOF_SALAMANDER_SKIN_ID);
                        skinOpt.ifPresent(skin ->
                                this.spawnAtLocation(serverLevel, new ItemStack(skin, 1), 0.35F));
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_LAND_ATTACK) {
            this.swimAttackAnimationState.stop();
            this.enterWaterAnimationState.stop();
            this.exitWaterAnimationState.stop();
            this.attackAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_SWIM_ATTACK) {
            this.attackAnimationState.stop();
            this.enterWaterAnimationState.stop();
            this.exitWaterAnimationState.stop();
            this.swimAttackAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_ENTER_WATER) {
            this.exitWaterAnimationState.stop();
            this.enterWaterAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_EXIT_WATER) {
            this.enterWaterAnimationState.stop();
            this.exitWaterAnimationState.start(this.tickCount);
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public void tick() {
        if (this.getTicksFrozen() > 0) {
            this.setTicksFrozen(0);
        }

        super.tick();

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            int cooldown = this.getShedCooldown();
            if (cooldown > 0) {
                this.setShedCooldown(cooldown - 1);
            }

            if (this.isAlive()) {
                boolean inFluid = this.isInFluidMedium();
                if (!this.fluidStateInitialized) {
                    this.wasInFluid = inFluid;
                    this.fluidStateInitialized = true;
                } else if (inFluid != this.wasInFluid) {
                    this.wasInFluid = inFluid;
                    serverLevel.broadcastEntityEvent(this, inFluid ? EVENT_ENTER_WATER : EVENT_EXIT_WATER);
                    if (inFluid) {
                        serverLevel.playSound(null, this.getX(), this.getY() + 0.25D, this.getZ(),
                                this.isInLava() ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.AXOLOTL_SPLASH,
                                SoundSource.NEUTRAL, 0.65F, 1.05F);
                    } else {
                        serverLevel.playSound(null, this.getX(), this.getY() + 0.25D, this.getZ(),
                                SoundEvents.MUD_STEP, SoundSource.NEUTRAL, 0.70F, 0.95F);
                        if (!this.isNetherVariant()) {
                            serverLevel.sendParticles(
                                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MUD.defaultBlockState()),
                                    this.getX(), this.getY() + 0.35D, this.getZ(),
                                    8, 0.35D, 0.15D, 0.35D, 0.06D);
                        }
                    }
                }

                // Regenerate 1.0 HP every 20 ticks in water, mud, vitae_spring_stone, or lava (Nether variant)
                if (this.tickCount % 20 == 0
                        && this.getHealth() < this.getMaxHealth()
                        && this.isTouchingRestorativeMedium()) {
                    this.heal(1.0F);
                }
            }
        } else if (this.level().isClientSide()) {
            this.setupClientAnimationsAndParticles();
        }
    }

    private void setupClientAnimationsAndParticles() {
        if (!this.isAlive()) {
            return;
        }

        if (this.attackAnimationState.isStarted()
                && this.attackAnimationState.getTimeInMillis(this.tickCount) >= LAND_ATTACK_DURATION_TICKS * 50L) {
            this.attackAnimationState.stop();
        }
        if (this.swimAttackAnimationState.isStarted()
                && this.swimAttackAnimationState.getTimeInMillis(this.tickCount) >= SWIM_ATTACK_DURATION_TICKS * 50L) {
            this.swimAttackAnimationState.stop();
        }
        if (this.enterWaterAnimationState.isStarted()
                && this.enterWaterAnimationState.getTimeInMillis(this.tickCount) >= ENTER_WATER_DURATION_TICKS * 50L) {
            this.enterWaterAnimationState.stop();
        }
        if (this.exitWaterAnimationState.isStarted()
                && this.exitWaterAnimationState.getTimeInMillis(this.tickCount) >= EXIT_WATER_DURATION_TICKS * 50L) {
            this.exitWaterAnimationState.stop();
        }

        boolean inFluid = this.isInFluidMedium();
        if (!this.clientFluidInitialized) {
            this.clientWasInFluid = inFluid;
            this.clientFluidInitialized = true;
        } else if (inFluid != this.clientWasInFluid) {
            this.clientWasInFluid = inFluid;
            if (inFluid) {
                this.exitWaterAnimationState.stop();
                this.enterWaterAnimationState.startIfStopped(this.tickCount);
            } else {
                this.enterWaterAnimationState.stop();
                this.exitWaterAnimationState.startIfStopped(this.tickCount);
            }
        }

        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.5E-4D;
        if (inFluid) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            if (moving) {
                this.swimIdleAnimationState.stop();
                this.swimAnimationState.startIfStopped(this.tickCount);
            } else {
                this.swimAnimationState.stop();
                this.swimIdleAnimationState.startIfStopped(this.tickCount);
            }
        } else {
            this.swimIdleAnimationState.stop();
            this.swimAnimationState.stop();
            if (moving) {
                this.idleAnimationState.stop();
                this.walkAnimationState.startIfStopped(this.tickCount);
            } else {
                this.walkAnimationState.stop();
                this.idleAnimationState.startIfStopped(this.tickCount);
            }
        }

        // Subtle variant-specific ambient particles
        if (this.isNetherVariant()) {
            if (this.tickCount % 10 == 0) {
                double px = this.getX() + (this.random.nextDouble() - 0.5D) * 0.70D;
                double py = this.getY() + 0.42D + this.random.nextDouble() * 0.15D;
                double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * 0.70D;
                this.level().addParticle(
                        this.random.nextInt(3) == 0 ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SMOKE,
                        px, py, pz,
                        0.0D, 0.015D, 0.0D
                );
            }
        } else if (inFluid && moving && this.tickCount % 5 == 0) {
            this.level().addParticle(
                    ParticleTypes.BUBBLE,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.55D,
                    this.getY() + 0.22D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.55D,
                    0.0D, 0.02D, 0.0D
            );
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel serverLevel, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(serverLevel, damageSource, recentlyHit);
        if (this.isNetherVariant()) {
            ItemStack pyreEssence = new ItemStack(ddraig.net.entropica.registry.ModItems.AVERAGE_ESSENCE.get(), 1 + this.random.nextInt(2));
            ddraig.net.entropica.item.EssenceItem.setEssenceType(pyreEssence, ddraig.net.entropica.api.EssenceType.PYRE);
            this.spawnAtLocation(serverLevel, pyreEssence);

            ItemStack soulfireEssence = new ItemStack(ddraig.net.entropica.registry.ModItems.AVERAGE_ESSENCE.get(), 1);
            ddraig.net.entropica.item.EssenceItem.setEssenceType(soulfireEssence, ddraig.net.entropica.api.EssenceType.SOULFIRE);
            this.spawnAtLocation(serverLevel, soulfireEssence);
        } else {
            ItemStack vaporEssence = new ItemStack(ddraig.net.entropica.registry.ModItems.AVERAGE_ESSENCE.get(), 2 + this.random.nextInt(2));
            ddraig.net.entropica.item.EssenceItem.setEssenceType(vaporEssence, ddraig.net.entropica.api.EssenceType.VAPOR);
            this.spawnAtLocation(serverLevel, vaporEssence);
        }
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        if (this.isInWater()) {
            return SoundEvents.AXOLOTL_IDLE_WATER;
        }
        return SoundEvents.AXOLOTL_IDLE_AIR;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.AXOLOTL_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.AXOLOTL_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        if (this.isNetherVariant()) {
            this.playSound(SoundEvents.BASALT_STEP, 0.30F, 0.90F);
        } else {
            this.playSound(SoundEvents.MUD_STEP, 0.30F, 1.00F);
        }
    }
}
