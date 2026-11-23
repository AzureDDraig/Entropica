package ddraig.net.entropica.entity.mycelial_spore_weaver;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
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
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Fauna 23 — Mycelial Spore-Weaver:
 * Gentle frost-adapted fungal-bark quadruped native to 2.12 Spore-Glazed Pack.
 * - Grows a multi-tiered golden-orange glazed mushroom cap that can be harvested with Shears
 *   for 1-2x Mycelial Spore Cap and regrows over time (twice as fast on Frozen Spore-Moss, Moss, or Mycelium).
 * - Gently weaves Frozen Spore-Moss onto Snow Blocks or Dirt when grazing with a full cap.
 * - Releases a defensive spore puff when hurt, inflicting Nausea + Slowness on nearby attackers
 *   unless protected by a Spore-Filter Mask or Spore Clarity effect.
 */
public class MycelialSporeWeaverEntity extends PathfinderMob implements Shearable {

    private static final EntityDataAccessor<Boolean> HAS_CAP =
            SynchedEntityData.defineId(MycelialSporeWeaverEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> PUFF_TICKS =
            SynchedEntityData.defineId(MycelialSporeWeaverEntity.class, EntityDataSerializers.INT);

    public static final int DEFAULT_REGROW_COOLDOWN_TICKS = 3000;

    private static final ResourceLocation FROZEN_SPORE_MOSS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "frozen_spore_moss");
    private static final ResourceLocation FROZEN_SPORE_MOSS_CARPET_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "frozen_spore_moss_carpet");
    private static final ResourceLocation SPORE_GLAZED_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spore_glazed_ice");
    private static final ResourceLocation MYCELIAL_SPORE_CAP_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mycelial_spore_cap");
    private static final ResourceLocation SPORE_FILTER_MASK_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spore_filter_mask");
    private static final ResourceLocation SPORE_CLARITY_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spore_clarity");
    private static final ResourceLocation SPORE_PUFF_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spore_puff");

    private int regrowCooldown = 0;

    public MycelialSporeWeaverEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.xpReward = 4;
        this.setPathfindingMalus(PathType.POWDER_SNOW, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_POWDER_SNOW, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    public static boolean isTemptFood(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.is(Items.BROWN_MUSHROOM) || stack.is(Items.RED_MUSHROOM)) {
            return true;
        }
        if (ModItems.SPORE_PUFF.isPresent() && stack.is(ModItems.SPORE_PUFF.get())) {
            return true;
        }
        return SPORE_PUFF_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    public static boolean hasSporeProtection(LivingEntity entity) {
        ItemStack headStack = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (!headStack.isEmpty() && SPORE_FILTER_MASK_ID.equals(BuiltInRegistries.ITEM.getKey(headStack.getItem()))) {
            return true;
        }
        Optional<Holder.Reference<MobEffect>> clarityHolder = BuiltInRegistries.MOB_EFFECT.get(SPORE_CLARITY_ID);
        if (clarityHolder.isPresent() && entity.hasEffect(clarityHolder.get())) {
            return true;
        }
        for (MobEffectInstance active : entity.getActiveEffects()) {
            if (active.getEffect().unwrapKey().map(k -> SPORE_CLARITY_ID.equals(k.location())).orElse(false)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isThinCoverBlock(BlockState state) {
        if (state.is(Blocks.SNOW) || state.getBlock() instanceof SnowLayerBlock) {
            return true;
        }
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return FROZEN_SPORE_MOSS_CARPET_ID.equals(key)
                || (!state.blocksMotion() && state.getFluidState().isEmpty() && !state.isAir());
    }

    public static boolean isWeaverSpawnSurface(BlockState state) {
        if (state == null || state.isAir()) {
            return false;
        }
        if (state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.MYCELIUM)
                || state.is(Blocks.MOSS_BLOCK)) {
            return true;
        }
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return FROZEN_SPORE_MOSS_ID.equals(key) || SPORE_GLAZED_ICE_ID.equals(key);
    }

    public static boolean checkMycelialSporeWeaverSpawnRules(
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
        if (isThinCoverBlock(below) && level.getBlockState(belowPos.below()).blocksMotion()) {
            below = level.getBlockState(belowPos.below());
        }
        return isWeaverSpawnSurface(below);
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        BlockPos belowPos = this.blockPosition().below();
        BlockState below = level.getBlockState(belowPos);
        if (isThinCoverBlock(below) && level.getBlockState(belowPos.below()).blocksMotion()) {
            below = level.getBlockState(belowPos.below());
        }
        if (isWeaverSpawnSurface(below)) {
            return true;
        }
        return super.checkSpawnRules(level, spawnReason);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        BlockState below = level.getBlockState(pos.below());
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(below.getBlock());
        if (FROZEN_SPORE_MOSS_ID.equals(key) || below.is(Blocks.MYCELIUM) || below.is(Blocks.MOSS_BLOCK)) {
            return 10.0F;
        }
        if (isWeaverSpawnSurface(below)) {
            return 4.0F;
        }
        return 0.0F;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HAS_CAP, true);
        builder.define(PUFF_TICKS, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.35D));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.1D, MycelialSporeWeaverEntity::isTemptFood, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 7.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    public boolean hasCap() {
        return this.entityData.get(HAS_CAP);
    }

    public void setHasCap(boolean hasCap) {
        this.entityData.set(HAS_CAP, hasCap);
    }

    public int getPuffTicks() {
        return this.entityData.get(PUFF_TICKS);
    }

    public void setPuffTicks(int ticks) {
        this.entityData.set(PUFF_TICKS, Math.max(0, ticks));
    }

    public int getRegrowCooldown() {
        return this.regrowCooldown;
    }

    public void setRegrowCooldown(int regrowCooldown) {
        this.regrowCooldown = Math.max(0, regrowCooldown);
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
    public boolean readyForShearing() {
        return this.isAlive() && !this.isBaby() && this.hasCap();
    }

    @Override
    public void shear(ServerLevel level, SoundSource soundSource, ItemStack shears) {
        level.playSound(null, this, SoundEvents.SHEEP_SHEAR, soundSource, 1.0F, 1.0F);
        this.setHasCap(false);
        this.regrowCooldown = DEFAULT_REGROW_COOLDOWN_TICKS;

        int dropCount = 1 + this.random.nextInt(2);
        Optional<Item> capItemOpt = BuiltInRegistries.ITEM.getOptional(MYCELIAL_SPORE_CAP_ID);
        if (capItemOpt.isPresent()) {
            Item capItem = capItemOpt.get();
            for (int i = 0; i < dropCount; i++) {
                ItemEntity drop = this.spawnAtLocation(level, new ItemStack(capItem, 1), 0.85F);
                if (drop != null) {
                    drop.setDeltaMovement(drop.getDeltaMovement().add(
                            (this.random.nextFloat() - this.random.nextFloat()) * 0.08D,
                            this.random.nextFloat() * 0.05D,
                            (this.random.nextFloat() - this.random.nextFloat()) * 0.08D
                    ));
                }
            }
        }

        level.sendParticles(
                ParticleTypes.SPORE_BLOSSOM_AIR,
                this.getX(),
                this.getY() + 0.95D,
                this.getZ(),
                12,
                0.35D,
                0.25D,
                0.35D,
                0.03D
        );
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(Items.SHEARS)) {
            if (this.level() instanceof ServerLevel serverLevel && this.readyForShearing()) {
                this.shear(serverLevel, SoundSource.PLAYERS, held);
                this.gameEvent(GameEvent.SHEAR, player);
                held.hurtAndBreak(1, player, hand.asEquipmentSlot());
                return InteractionResult.SUCCESS_SERVER;
            }
            if (this.level().isClientSide() && this.readyForShearing()) {
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }
        return super.mobInteract(player, hand);
    }

    private BlockPos resolveSubstratePos() {
        BlockPos below = this.blockPosition().below();
        BlockState belowState = this.level().getBlockState(below);
        if (isThinCoverBlock(belowState) && this.level().getBlockState(below.below()).blocksMotion()) {
            return below.below();
        }
        return below;
    }

    private boolean isNutrientFungalGround(BlockState state) {
        if (state.is(Blocks.MOSS_BLOCK) || state.is(Blocks.MYCELIUM)) {
            return true;
        }
        return FROZEN_SPORE_MOSS_ID.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }

    @Override
    public void aiStep() {
        if (this.getTicksFrozen() > 0) {
            this.setTicksFrozen(0);
        }

        super.aiStep();

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            int currentPuff = this.getPuffTicks();
            if (currentPuff > 0) {
                this.setPuffTicks(currentPuff - 1);
            }

            BlockPos substratePos = this.resolveSubstratePos();
            BlockState substrateState = serverLevel.getBlockState(substratePos);

            if (!this.hasCap()) {
                int step = this.isNutrientFungalGround(substrateState) ? 2 : 1;
                this.regrowCooldown -= step;
                if (this.regrowCooldown <= 0) {
                    this.regrowCooldown = 0;
                    this.setHasCap(true);
                    serverLevel.playSound(
                            null,
                            this.getX(),
                            this.getY(),
                            this.getZ(),
                            SoundEvents.SPORE_BLOSSOM_PLACE,
                            SoundSource.NEUTRAL,
                            0.85F,
                            1.1F
                    );
                    serverLevel.sendParticles(
                            ParticleTypes.HAPPY_VILLAGER,
                            this.getX(),
                            this.getY() + 0.95D,
                            this.getZ(),
                            10,
                            0.45D,
                            0.3D,
                            0.45D,
                            0.02D
                    );
                    serverLevel.sendParticles(
                            ParticleTypes.SPORE_BLOSSOM_AIR,
                            this.getX(),
                            this.getY() + 0.95D,
                            this.getZ(),
                            16,
                            0.5D,
                            0.35D,
                            0.5D,
                            0.04D
                    );
                }
            } else if (this.tickCount > 0 && this.tickCount % 400 == 0) {
                if ((substrateState.is(Blocks.SNOW_BLOCK) || substrateState.is(Blocks.DIRT))
                        && serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
                        && this.random.nextFloat() < 0.20F) {
                    Optional<Block> mossOpt = BuiltInRegistries.BLOCK.getOptional(FROZEN_SPORE_MOSS_ID);
                    if (mossOpt.isPresent()) {
                        serverLevel.setBlockAndUpdate(substratePos, mossOpt.get().defaultBlockState());
                        serverLevel.sendParticles(
                                ParticleTypes.SPORE_BLOSSOM_AIR,
                                substratePos.getX() + 0.5D,
                                substratePos.getY() + 1.05D,
                                substratePos.getZ() + 0.5D,
                                8,
                                0.35D,
                                0.15D,
                                0.35D,
                                0.02D
                        );
                    }
                }
            }
        } else if (this.level().isClientSide()) {
            if (this.hasCap() && this.random.nextInt(18) == 0) {
                this.level().addParticle(
                        ParticleTypes.SPORE_BLOSSOM_AIR,
                        this.getRandomX(0.55D),
                        this.getY() + 0.85D + this.random.nextDouble() * 0.35D,
                        this.getRandomZ(0.55D),
                        0.0D,
                        -0.01D,
                        0.0D
                );
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.isInvulnerableTo(level, source)) {
            return false;
        }
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && this.isAlive()) {
            this.setPuffTicks(20);
            level.playSound(
                    null,
                    this.getX(),
                    this.getY() + 0.6D,
                    this.getZ(),
                    SoundEvents.SPORE_BLOSSOM_FALL,
                    SoundSource.NEUTRAL,
                    1.0F,
                    0.85F
            );
            level.sendParticles(
                    ParticleTypes.SPORE_BLOSSOM_AIR,
                    this.getX(),
                    this.getY() + 0.75D,
                    this.getZ(),
                    24,
                    0.95D,
                    0.55D,
                    0.95D,
                    0.06D
            );

            Entity directAttacker = source.getDirectEntity();
            Entity rootAttacker = source.getEntity();
            List<LivingEntity> nearby = level.getEntitiesOfClass(
                    LivingEntity.class,
                    this.getBoundingBox().inflate(3.5D),
                    e -> e != this && e.isAlive() && !(e instanceof MycelialSporeWeaverEntity)
            );
            for (LivingEntity candidate : nearby) {
                boolean isAttackerOrHostile = (candidate == rootAttacker)
                        || (candidate == directAttacker)
                        || (candidate instanceof Mob mob && mob.getTarget() == this)
                        || (candidate instanceof Enemy)
                        || (candidate instanceof Player p && !p.isCreative() && !p.isSpectator());
                if (isAttackerOrHostile && !hasSporeProtection(candidate)) {
                    candidate.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0), this);
                    candidate.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 0), this);
                }
            }
        }
        return hurt;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setHasCap(input.read("HasCap", Codec.BOOL).orElse(true));
        this.setPuffTicks(input.read("PuffTicks", Codec.INT).orElse(0));
        this.regrowCooldown = input.read("RegrowCooldown", Codec.INT).orElse(0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("HasCap", Codec.BOOL, this.hasCap());
        output.store("PuffTicks", Codec.INT, this.getPuffTicks());
        output.store("RegrowCooldown", Codec.INT, this.regrowCooldown);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.SPORE_BLOSSOM_HIT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SPORE_BLOSSOM_BREAK;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.MOOSHROOM_CONVERT;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SPORE_BLOSSOM_STEP, 0.25F, 0.95F);
    }
}
