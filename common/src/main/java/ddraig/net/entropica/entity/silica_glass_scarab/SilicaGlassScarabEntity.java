package ddraig.net.entropica.entity.silica_glass_scarab;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
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
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
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

import java.util.Optional;

/**
 * Fauna 13 — Silica Glass-Scarab (Solar Scarab) (entropica:silica_glass_scarab).
 * Armored terracotta & solar-quartz beetle native to 2.13 Mirage Glacier:
 * - Peaceful Shedding: right-clicking with a Brush, Quartz, or Glass Sand sheds 1x Silica Glass Plate
 *   on a 2400-tick cooldown with golden solar motes and crystalline chimes.
 * - Solar Reflection Defense: when struck by a LivingEntity, flares its 4 quartz wing-panes to focus a
 *   blinding solar flash, dealing 5.0 fire/magic reflection damage + 3s ignition and inflicting
 *   Double Vision (120t) unless the attacker wears Mirage Veil Goggles.
 */
public class SilicaGlassScarabEntity extends PathfinderMob {

    private static final EntityDataAccessor<Integer> SOLAR_FLASH_TICKS =
            SynchedEntityData.defineId(SilicaGlassScarabEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SHED_COOLDOWN =
            SynchedEntityData.defineId(SilicaGlassScarabEntity.class, EntityDataSerializers.INT);

    public static final int DEFAULT_SHED_COOLDOWN_TICKS = 2400;
    public static final int SOLAR_FLASH_DURATION_TICKS = 18;

    private static final ResourceLocation GLASS_SAND_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "glass_sand");
    private static final ResourceLocation GLASS_SANDSTONE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "glass_sandstone");
    private static final ResourceLocation MIRAGE_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mirage_ice");
    private static final ResourceLocation SILICA_GLASS_PLATE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "silica_glass_plate");
    private static final ResourceLocation MIRAGE_VEIL_GOGGLES_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mirage_veil_goggles");
    private static final ResourceLocation DOUBLE_VISION_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "double_vision");

    private static final DustParticleOptions GOLDEN_SOLAR_DUST = new DustParticleOptions(0xFBBF24, 0.65F);

    public SilicaGlassScarabEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.xpReward = 4;
        this.setPathfindingMalus(PathType.POWDER_SNOW, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_POWDER_SNOW, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.21D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    public static boolean isTemptFood(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.is(Items.QUARTZ) || stack.is(Items.AMETHYST_SHARD)) {
            return true;
        }
        return GLASS_SAND_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    public static boolean isSheddingToolOrOffering(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.is(Items.BRUSH) || stack.is(Items.QUARTZ)) {
            return true;
        }
        return GLASS_SAND_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    public static boolean isWearingMirageVeilGoggles(LivingEntity entity) {
        ItemStack headStack = entity.getItemBySlot(EquipmentSlot.HEAD);
        return !headStack.isEmpty()
                && MIRAGE_VEIL_GOGGLES_ID.equals(BuiltInRegistries.ITEM.getKey(headStack.getItem()));
    }

    private static boolean isThinCoverBlock(BlockState state) {
        if (state.is(Blocks.SNOW) || state.getBlock() instanceof SnowLayerBlock) {
            return true;
        }
        return !state.blocksMotion() && state.getFluidState().isEmpty() && !state.isAir();
    }

    public static boolean isScarabSpawnSurface(BlockState state) {
        if (state == null || state.isAir()) {
            return false;
        }
        if (state.is(Blocks.RED_SAND)
                || state.is(Blocks.SAND)
                || state.is(Blocks.RED_SANDSTONE)
                || state.is(Blocks.SANDSTONE)
                || state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.SNOW_BLOCK)) {
            return true;
        }
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return GLASS_SAND_ID.equals(key)
                || GLASS_SANDSTONE_ID.equals(key)
                || MIRAGE_ICE_ID.equals(key);
    }

    public static boolean checkSilicaGlassScarabSpawnRules(
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
        return isScarabSpawnSurface(below);
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        BlockPos belowPos = this.blockPosition().below();
        BlockState below = level.getBlockState(belowPos);
        if (isThinCoverBlock(below) && level.getBlockState(belowPos.below()).blocksMotion()) {
            below = level.getBlockState(belowPos.below());
        }
        if (isScarabSpawnSurface(below)) {
            return true;
        }
        return super.checkSpawnRules(level, spawnReason);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        BlockState below = level.getBlockState(pos.below());
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(below.getBlock());
        if (GLASS_SAND_ID.equals(key) || GLASS_SANDSTONE_ID.equals(key) || MIRAGE_ICE_ID.equals(key)) {
            return 10.0F;
        }
        if (isScarabSpawnSurface(below)) {
            return 4.0F;
        }
        return 0.0F;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SOLAR_FLASH_TICKS, 0);
        builder.define(SHED_COOLDOWN, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.3D));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.1D, SilicaGlassScarabEntity::isTemptFood, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 7.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    public int getSolarFlashTicks() {
        return this.entityData.get(SOLAR_FLASH_TICKS);
    }

    public void setSolarFlashTicks(int ticks) {
        this.entityData.set(SOLAR_FLASH_TICKS, Math.max(0, ticks));
    }

    public int getShedCooldown() {
        return this.entityData.get(SHED_COOLDOWN);
    }

    public void setShedCooldown(int cooldown) {
        this.entityData.set(SHED_COOLDOWN, Math.max(0, cooldown));
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
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (this.isAlive() && isSheddingToolOrOffering(held)) {
            if (this.getShedCooldown() <= 0) {
                if (this.level() instanceof ServerLevel serverLevel) {
                    if (held.is(Items.BRUSH)) {
                        held.hurtAndBreak(1, player, hand.asEquipmentSlot());
                    } else {
                        held.consume(1, player);
                        this.heal(3.0F);
                    }

                    Optional<Item> plateOpt = BuiltInRegistries.ITEM.getOptional(SILICA_GLASS_PLATE_ID);
                    if (plateOpt.isPresent()) {
                        ItemEntity drop = this.spawnAtLocation(serverLevel, new ItemStack(plateOpt.get(), 1), 0.45F);
                        if (drop != null) {
                            drop.setDeltaMovement(drop.getDeltaMovement().add(
                                    (this.random.nextFloat() - this.random.nextFloat()) * 0.07D,
                                    0.04D + this.random.nextFloat() * 0.04D,
                                    (this.random.nextFloat() - this.random.nextFloat()) * 0.07D
                            ));
                        }
                    }

                    serverLevel.playSound(
                            null,
                            this.getX(),
                            this.getY(),
                            this.getZ(),
                            SoundEvents.AMETHYST_BLOCK_CHIME,
                            SoundSource.NEUTRAL,
                            1.0F,
                            1.25F
                    );
                    serverLevel.playSound(
                            null,
                            this.getX(),
                            this.getY(),
                            this.getZ(),
                            SoundEvents.BRUSH_GENERIC,
                            SoundSource.PLAYERS,
                            0.95F,
                            1.10F
                    );
                    serverLevel.sendParticles(
                            ParticleTypes.END_ROD,
                            this.getX(),
                            this.getY() + 0.42D,
                            this.getZ(),
                            16,
                            0.35D,
                            0.22D,
                            0.35D,
                            0.04D
                    );
                    serverLevel.sendParticles(
                            GOLDEN_SOLAR_DUST,
                            this.getX(),
                            this.getY() + 0.45D,
                            this.getZ(),
                            12,
                            0.32D,
                            0.18D,
                            0.32D,
                            0.02D
                    );

                    this.setShedCooldown(DEFAULT_SHED_COOLDOWN_TICKS);
                    this.setSolarFlashTicks(Math.max(this.getSolarFlashTicks(), 10));
                    this.gameEvent(GameEvent.ENTITY_INTERACT, player);
                    return InteractionResult.SUCCESS_SERVER;
                }
                if (this.level().isClientSide()) {
                    return InteractionResult.CONSUME;
                }
            }
            return InteractionResult.PASS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void aiStep() {
        if (this.getTicksFrozen() > 0) {
            this.setTicksFrozen(0);
        }

        super.aiStep();

        if (!this.level().isClientSide()) {
            int flash = this.getSolarFlashTicks();
            if (flash > 0) {
                this.setSolarFlashTicks(flash - 1);
            }
            int cooldown = this.getShedCooldown();
            if (cooldown > 0) {
                this.setShedCooldown(cooldown - 1);
            }
        } else {
            int flash = this.getSolarFlashTicks();
            int chance = flash > 0 ? 3 : 28;
            if (this.random.nextInt(chance) == 0) {
                this.level().addParticle(
                        ParticleTypes.END_ROD,
                        this.getRandomX(0.55D),
                        this.getY() + 0.38D + this.random.nextDouble() * 0.25D,
                        this.getRandomZ(0.55D),
                        0.0D,
                        0.015D,
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
            LivingEntity attacker = null;
            Entity rootEntity = source.getEntity();
            Entity directEntity = source.getDirectEntity();
            if (rootEntity instanceof LivingEntity liv) {
                attacker = liv;
            } else if (directEntity instanceof LivingEntity dliv) {
                attacker = dliv;
            }

            if (attacker != null && attacker != this && attacker.isAlive() && !(attacker instanceof SilicaGlassScarabEntity)) {
                this.setSolarFlashTicks(SOLAR_FLASH_DURATION_TICKS);

                level.playSound(
                        null,
                        this.getX(),
                        this.getY() + 0.35D,
                        this.getZ(),
                        SoundEvents.AMETHYST_CLUSTER_HIT,
                        SoundSource.NEUTRAL,
                        1.0F,
                        1.45F
                );
                level.playSound(
                        null,
                        this.getX(),
                        this.getY() + 0.35D,
                        this.getZ(),
                        SoundEvents.BLAZE_SHOOT,
                        SoundSource.NEUTRAL,
                        0.65F,
                        1.65F
                );

                level.sendParticles(
                        ParticleTypes.END_ROD,
                        this.getX(),
                        this.getY() + 0.45D,
                        this.getZ(),
                        24,
                        0.55D,
                        0.35D,
                        0.55D,
                        0.08D
                );
                level.sendParticles(
                        ParticleTypes.FLAME,
                        this.getX(),
                        this.getY() + 0.45D,
                        this.getZ(),
                        18,
                        0.50D,
                        0.30D,
                        0.50D,
                        0.05D
                );

                // Solar Reflection Defense: 5.0 fire/magic reflection damage + 3.0s ignition
                attacker.igniteForSeconds(3.0F);
                if (!attacker.hurtServer(level, level.damageSources().onFire(), 5.0F)) {
                    attacker.hurtServer(level, level.damageSources().magic(), 5.0F);
                }

                // Inflict Double Vision (120t) unless protected by Mirage Veil Goggles
                if (!isWearingMirageVeilGoggles(attacker)) {
                    Optional<MobEffect> doubleVisionOpt = BuiltInRegistries.MOB_EFFECT.getOptional(DOUBLE_VISION_ID);
                    if (doubleVisionOpt.isPresent()) {
                        Holder<MobEffect> holder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(doubleVisionOpt.get());
                        attacker.addEffect(new MobEffectInstance(holder, 120, 0), this);
                    }
                }
            }
        }
        return hurt;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setSolarFlashTicks(input.read("SolarFlashTicks", Codec.INT).orElse(0));
        this.setShedCooldown(input.read("ShedCooldown", Codec.INT).orElse(0));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("SolarFlashTicks", Codec.INT, this.getSolarFlashTicks());
        output.store("ShedCooldown", Codec.INT, this.getShedCooldown());
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.AMETHYST_BLOCK_STEP;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.AMETHYST_CLUSTER_PLACE;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.AMETHYST_CLUSTER_BREAK;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.TUFF_STEP, 0.25F, 1.35F);
    }
}
