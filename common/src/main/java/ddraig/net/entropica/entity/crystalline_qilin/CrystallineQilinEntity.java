package ddraig.net.entropica.entity.crystalline_qilin;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.barrow_hound.BarrowHoundEntity;
import ddraig.net.entropica.entity.cryo_stalker.CryoStalkerEntity;
import ddraig.net.entropica.entity.geyser_wiggle_worm.GeyserWiggleWormEntity;
import ddraig.net.entropica.entity.grot.GrotEntity;
import ddraig.net.entropica.entity.void_rift_shade.VoidRiftShadeEntity;
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
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Crystalline Qilin (Glow Guardian) — Neutral apex guardian native to 2.10 Radiant Glaze-Plains.
 * - Peaceful unless provoked, while actively defending the glaze-plains from undead, Barrow, and Void threats.
 * - Periodic Light-Refraction Pulse (light_refract): dazzles hostile attackers within 7 blocks with
 *   Blindness (60t) + Glowing (160t), while blessing nearby peaceful players within 8 blocks with Regeneration (80t).
 * - Friendly Offering: feeding a Weeping Amber Tear or Golden Apple heals 10 HP, triggers a radiant chime & pulse,
 *   and sheds 1x Jade Qilin Scale on a cooldown.
 */
public class CrystallineQilinEntity extends PathfinderMob {

    private static final EntityDataAccessor<Boolean> PROVOKED =
            SynchedEntityData.defineId(CrystallineQilinEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> REFRACTING =
            SynchedEntityData.defineId(CrystallineQilinEntity.class, EntityDataSerializers.BOOLEAN);

    private static final byte EVENT_ATTACK = 4;
    private static final byte EVENT_LIGHT_REFRACT = 61;
    private static final byte EVENT_HURT_FLINCH = 62;
    private static final byte EVENT_DEATH_BUCKLE = 63;

    public static final int COMBAT_REFRACT_COOLDOWN_TICKS = 160;
    public static final int SCALE_SHED_COOLDOWN_TICKS = 1200;
    private static final int REFRACT_POSE_DURATION_TICKS = 40;

    private static final ResourceLocation JADE_QILIN_SCALE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "jade_qilin_scale");
    private static final ResourceLocation WEEPING_AMBER_TEAR_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "weeping_amber_tear");

    private static final DustParticleOptions JADE_SPARKLE_DUST = new DustParticleOptions(0x84ECB6, 0.75F);
    private static final DustParticleOptions SOLAR_REFRACT_DUST = new DustParticleOptions(0xFCFED4, 0.85F);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState runAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState lightRefractAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    private int refractCooldown = 0;
    private int ambientRefractTimer;
    private int refractPoseTicks = 0;
    private int scaleShedCooldown = 0;

    public CrystallineQilinEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 10;
        this.setPathfindingMalus(PathType.POWDER_SNOW, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_POWDER_SNOW, 0.0F);
        this.ambientRefractTimer = 240 + this.random.nextInt(200);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.45D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.STEP_HEIGHT, 1.1D);
    }

    public static boolean isQilinOffering(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
            return true;
        }
        if (ModItems.WEEPING_AMBER_TEAR.isPresent() && stack.is(ModItems.WEEPING_AMBER_TEAR.get())) {
            return true;
        }
        return WEEPING_AMBER_TEAR_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    public static boolean isHostileThreatToGlazePlains(LivingEntity candidate, ServerLevel level) {
        if (candidate == null || !candidate.isAlive() || candidate instanceof GrotEntity || candidate instanceof Creeper) {
            return false;
        }
        if (candidate.getType().is(EntityTypeTags.UNDEAD)
                || candidate instanceof BarrowHoundEntity
                || candidate instanceof VoidRiftShadeEntity
                || candidate instanceof CryoStalkerEntity
                || candidate instanceof GeyserWiggleWormEntity) {
            return true;
        }
        return candidate instanceof Monster;
    }

    private static boolean isThinGlazeCover(BlockState state) {
        if (state.is(Blocks.SNOW) || state.getBlock() instanceof SnowLayerBlock) {
            return true;
        }
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return "golden_lichen".equals(path)
                || "radiant_snow_layer".equals(path)
                || "auroral_feather_moss".equals(path)
                || "blue_frozen_moss_carpet".equals(path)
                || (!state.blocksMotion() && state.getFluidState().isEmpty() && !state.isAir());
    }

    public static boolean isCrystallineQilinSpawnSurface(BlockGetter level, BlockPos belowPos, BlockState belowState) {
        if (belowState.isAir()) {
            return false;
        }
        String path = BuiltInRegistries.BLOCK.getKey(belowState.getBlock()).getPath();
        return belowState.is(Blocks.GRASS_BLOCK)
                || belowState.is(Blocks.SNOW_BLOCK)
                || belowState.is(Blocks.SNOW)
                || belowState.is(Blocks.PACKED_ICE)
                || belowState.is(Blocks.BLUE_ICE)
                || belowState.is(Blocks.ICE)
                || belowState.is(Blocks.CALCITE)
                || belowState.is(Blocks.QUARTZ_BLOCK)
                || belowState.is(BlockTags.ANIMALS_SPAWNABLE_ON)
                || "radiant_snow_block".equals(path)
                || "radiant_glaze_ice".equals(path)
                || "vitae_spring_stone".equals(path)
                || "glimmer_ice".equals(path)
                || "aegis_quartz_block".equals(path)
                || "qilin_light_refractor".equals(path)
                || (belowState.getFluidState().isEmpty() && belowState.isFaceSturdy(level, belowPos, Direction.UP));
    }

    public static boolean checkCrystallineQilinSpawnRules(
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
        if (isThinGlazeCover(below) && level.getBlockState(belowPos.below()).blocksMotion()) {
            belowPos = belowPos.below();
            below = level.getBlockState(belowPos);
        }
        return isCrystallineQilinSpawnSurface(level, belowPos, below);
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        BlockPos belowPos = this.blockPosition().below();
        BlockState below = level.getBlockState(belowPos);
        if (isThinGlazeCover(below) && level.getBlockState(belowPos.below()).blocksMotion()) {
            belowPos = belowPos.below();
            below = level.getBlockState(belowPos);
        }
        if (isCrystallineQilinSpawnSurface(level, belowPos, below)) {
            return true;
        }
        return super.checkSpawnRules(level, spawnReason);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        String path = BuiltInRegistries.BLOCK.getKey(below.getBlock()).getPath();
        if ("radiant_snow_block".equals(path)
                || "vitae_spring_stone".equals(path)
                || below.is(Blocks.GRASS_BLOCK)
                || below.is(Blocks.SNOW_BLOCK)) {
            return 10.0F;
        }
        return 0.0F;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PROVOKED, false);
        builder.define(REFRACTING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new QilinAntlerChargeGoal(this, 1.32D));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.1D, CrystallineQilinEntity::isQilinOffering, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                this,
                Mob.class,
                10,
                true,
                false,
                CrystallineQilinEntity::isHostileThreatToGlazePlains
        ));
    }

    public boolean isProvoked() {
        return this.entityData.get(PROVOKED);
    }

    public void setProvoked(boolean provoked) {
        this.entityData.set(PROVOKED, provoked);
    }

    public boolean isRefracting() {
        return this.entityData.get(REFRACTING);
    }

    public void setRefracting(boolean refracting) {
        this.entityData.set(REFRACTING, refracting);
    }

    public int getScaleShedCooldown() {
        return this.scaleShedCooldown;
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
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        if (fallDistance <= 6.0D) {
            return false;
        }
        return super.causeFallDamage(fallDistance - 5.0D, multiplier * 0.5F, source);
    }

    @Override
    public void makeStuckInBlock(BlockState state, Vec3 motionMultiplier) {
        if (state.is(Blocks.POWDER_SNOW) || state.is(Blocks.SNOW) || state.getBlock() instanceof SnowLayerBlock) {
            return;
        }
        super.makeStuckInBlock(state, motionMultiplier);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        if (!this.level().isClientSide()) {
            this.setProvoked(target != null && target.isAlive());
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        level.broadcastEntityEvent(this, EVENT_ATTACK);

        if (hit && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0), this);

            double dx = this.getX() - living.getX();
            double dz = this.getZ() - living.getZ();
            living.knockback(0.55D, dx, dz);
            Vec3 cur = living.getDeltaMovement();
            living.setDeltaMovement(cur.x, Math.max(cur.y, 0.24D), cur.z);
            living.hasImpulse = true;
            living.hurtMarked = true;

            level.playSound(
                    null,
                    this.getX(),
                    this.getY() + 1.4D,
                    this.getZ(),
                    SoundEvents.GOAT_RAM_IMPACT,
                    SoundSource.NEUTRAL,
                    0.85F,
                    1.18F
            );
            level.playSound(
                    null,
                    this.getX(),
                    this.getY() + 1.4D,
                    this.getZ(),
                    SoundEvents.AMETHYST_BLOCK_HIT,
                    SoundSource.NEUTRAL,
                    0.95F,
                    1.25F
            );
            level.sendParticles(
                    ParticleTypes.END_ROD,
                    living.getX(),
                    living.getY() + living.getBbHeight() * 0.55D,
                    living.getZ(),
                    16,
                    0.35D,
                    0.35D,
                    0.35D,
                    0.08D
            );
        }
        return hit;
    }

    /**
     * Unleashes a Light-Refraction Pulse (light_refract animation):
     * - Dazzles hostile attackers within 7 blocks with Blindness (60t) + Glowing (160t).
     * - Grants nearby peaceful players within 8 blocks Regeneration (80t).
     */
    public void triggerLightRefractionPulse(ServerLevel level) {
        if (!this.isAlive()) {
            return;
        }
        this.refractCooldown = COMBAT_REFRACT_COOLDOWN_TICKS;
        this.refractPoseTicks = REFRACT_POSE_DURATION_TICKS;
        this.setRefracting(true);
        level.broadcastEntityEvent(this, EVENT_LIGHT_REFRACT);

        // 1. Dazzle hostile attackers within 7 blocks
        LivingEntity currentTarget = this.getTarget();
        List<LivingEntity> nearbyHostiles = level.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(7.0D),
                e -> e != this && e.isAlive() && this.distanceToSqr(e) <= 49.0D
        );
        for (LivingEntity entity : nearbyHostiles) {
            boolean isHostileAttacker = (entity == currentTarget)
                    || (entity == this.getLastHurtByMob() && !(entity instanceof Player p && (p.isCreative() || p.isSpectator())))
                    || (entity instanceof Mob mob && (mob.getTarget() == this
                            || mob.getTarget() instanceof Player
                            || isHostileThreatToGlazePlains(mob, level)));
            if (isHostileAttacker) {
                entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0), this);
                entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 160, 0), this);
                level.sendParticles(
                        ParticleTypes.END_ROD,
                        entity.getX(),
                        entity.getY() + entity.getBbHeight() * 0.65D,
                        entity.getZ(),
                        10,
                        0.28D,
                        0.28D,
                        0.28D,
                        0.04D
                );
            }
        }

        // 2. Grant nearby peaceful players within 8 blocks Regeneration (80 ticks)
        List<Player> nearbyPlayers = level.getEntitiesOfClass(
                Player.class,
                this.getBoundingBox().inflate(8.0D),
                p -> p.isAlive() && !p.isSpectator() && p != currentTarget && this.distanceToSqr(p) <= 64.0D
        );
        for (Player peacefulPlayer : nearbyPlayers) {
            peacefulPlayer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 0, true, true, true), this);
        }

        // 3. Radiant emerald-jade & solar chime + particle burst
        level.playSound(
                null,
                this.getX(),
                this.getY() + 1.4D,
                this.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.NEUTRAL,
                1.1F,
                1.22F
        );
        level.playSound(
                null,
                this.getX(),
                this.getY() + 1.4D,
                this.getZ(),
                SoundEvents.AMETHYST_CLUSTER_PLACE,
                SoundSource.NEUTRAL,
                0.85F,
                1.35F
        );
        level.sendParticles(
                ParticleTypes.END_ROD,
                this.getX(),
                this.getY() + 1.35D,
                this.getZ(),
                26,
                0.75D,
                0.55D,
                0.75D,
                0.06D
        );
        level.sendParticles(
                SOLAR_REFRACT_DUST,
                this.getX(),
                this.getY() + 1.3D,
                this.getZ(),
                18,
                0.65D,
                0.45D,
                0.65D,
                0.02D
        );
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isQilinOffering(stack) && this.isAlive()) {
            boolean needsHealing = this.getHealth() < this.getMaxHealth();
            boolean canShedScale = this.scaleShedCooldown <= 0;
            boolean wasProvokedAtPlayer = this.getTarget() == player || this.getLastHurtByMob() == player;

            if (needsHealing || canShedScale || wasProvokedAtPlayer) {
                if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                    stack.consume(1, player);
                    this.heal(10.0F);

                    if (wasProvokedAtPlayer) {
                        this.setTarget(null);
                        this.setLastHurtByMob(null);
                    }

                    this.triggerLightRefractionPulse(serverLevel);
                    serverLevel.sendParticles(
                            ParticleTypes.HAPPY_VILLAGER,
                            this.getX(),
                            this.getY() + 1.4D,
                            this.getZ(),
                            10,
                            0.55D,
                            0.45D,
                            0.55D,
                            0.02D
                    );

                    if (canShedScale) {
                        this.scaleShedCooldown = SCALE_SHED_COOLDOWN_TICKS;
                        Optional<Item> scaleOpt = ModItems.JADE_QILIN_SCALE.isPresent()
                                ? Optional.of(ModItems.JADE_QILIN_SCALE.get())
                                : BuiltInRegistries.ITEM.getOptional(JADE_QILIN_SCALE_ID);
                        scaleOpt.ifPresent(scaleItem ->
                                this.spawnAtLocation(serverLevel, new ItemStack(scaleItem, 1), 0.75F));
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.isInvulnerableTo(level, source)) {
            return false;
        }
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && this.isAlive()) {
            level.broadcastEntityEvent(this, EVENT_HURT_FLINCH);
        }
        return hurt;
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.broadcastEntityEvent(this, EVENT_DEATH_BUCKLE);
            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    this.getX(),
                    this.getY() + 1.1D,
                    this.getZ(),
                    20,
                    0.55D,
                    0.45D,
                    0.55D,
                    0.06D
            );
        }
        super.die(damageSource);
    }

    @Override
    protected void tickDeath() {
        ++this.deathTime;
        if (this.deathTime >= 36 && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_ATTACK) {
            this.hurtAnimationState.stop();
            this.lightRefractAnimationState.stop();
            this.attackAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_LIGHT_REFRACT) {
            this.hurtAnimationState.stop();
            this.lightRefractAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_HURT_FLINCH) {
            if (!this.attackAnimationState.isStarted() && !this.lightRefractAnimationState.isStarted()) {
                this.hurtAnimationState.start(this.tickCount);
            }
            return;
        }
        if (id == EVENT_DEATH_BUCKLE) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.runAnimationState.stop();
            this.attackAnimationState.stop();
            this.lightRefractAnimationState.stop();
            this.hurtAnimationState.stop();
            this.deathAnimationState.start(this.tickCount);
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
            if (this.refractCooldown > 0) {
                this.refractCooldown--;
            }
            if (this.scaleShedCooldown > 0) {
                this.scaleShedCooldown--;
            }
            if (this.refractPoseTicks > 0) {
                this.refractPoseTicks--;
                if (this.refractPoseTicks == 0) {
                    this.setRefracting(false);
                }
            }

            if (this.isAlive()) {
                LivingEntity target = this.getTarget();
                if (target != null && target.isAlive()) {
                    if (this.refractCooldown <= 0 && this.distanceToSqr(target) <= 49.0D) {
                        this.triggerLightRefractionPulse(serverLevel);
                    }
                } else {
                    if (this.isProvoked()) {
                        this.setProvoked(false);
                    }
                    this.ambientRefractTimer--;
                    if (this.ambientRefractTimer <= 0) {
                        boolean sunnyOrBright = (serverLevel.isBrightOutside() && serverLevel.canSeeSky(this.blockPosition().above()))
                                || serverLevel.getMaxLocalRawBrightness(this.blockPosition().above()) >= 13;
                        if (sunnyOrBright && this.refractCooldown <= 0) {
                            this.triggerLightRefractionPulse(serverLevel);
                        }
                        this.ambientRefractTimer = 300 + this.random.nextInt(240);
                    }
                }
            }
        } else if (this.level().isClientSide()) {
            this.setupClientAnimationsAndParticles();
        }
    }

    private void setupClientAnimationsAndParticles() {
        if (!this.isAlive()) {
            this.deathAnimationState.startIfStopped(this.tickCount);
            return;
        }

        if (this.attackAnimationState.isStarted() && this.attackAnimationState.getTimeInMillis(this.tickCount) >= 1400L) {
            this.attackAnimationState.stop();
        }
        if (this.lightRefractAnimationState.isStarted() && this.lightRefractAnimationState.getTimeInMillis(this.tickCount) >= 2000L) {
            this.lightRefractAnimationState.stop();
        }
        if (this.hurtAnimationState.isStarted() && this.hurtAnimationState.getTimeInMillis(this.tickCount) >= 600L) {
            this.hurtAnimationState.stop();
        }

        double horizSpeedSqr = this.getDeltaMovement().horizontalDistanceSqr();
        boolean running = horizSpeedSqr > 0.022D || (this.isProvoked() && horizSpeedSqr > 0.004D);
        boolean walking = !running && horizSpeedSqr > 1.0E-4D;

        if (running) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.runAnimationState.startIfStopped(this.tickCount);
        } else if (walking) {
            this.idleAnimationState.stop();
            this.runAnimationState.stop();
            this.walkAnimationState.startIfStopped(this.tickCount);
        } else {
            this.walkAnimationState.stop();
            this.runAnimationState.stop();
            this.idleAnimationState.startIfStopped(this.tickCount);
        }

        // Shimmering emerald-jade & golden refractive motes around crystalline flank scales
        if (this.tickCount % (this.isRefracting() ? 4 : 14) == 0) {
            double ox = this.getX() + (this.random.nextDouble() - 0.5D) * 1.2D;
            double oy = this.getY() + 0.85D + this.random.nextDouble() * 0.95D;
            double oz = this.getZ() + (this.random.nextDouble() - 0.5D) * 1.2D;
            this.level().addParticle(
                    this.random.nextBoolean() ? JADE_SPARKLE_DUST : ParticleTypes.END_ROD,
                    ox,
                    oy,
                    oz,
                    0.0D,
                    0.01D,
                    0.0D
            );
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("ScaleShedCooldown", Codec.INT, this.scaleShedCooldown);
        output.store("RefractCooldown", Codec.INT, this.refractCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.scaleShedCooldown = input.read("ScaleShedCooldown", Codec.INT).orElse(0);
        this.refractCooldown = input.read("RefractCooldown", Codec.INT).orElse(0);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.random.nextBoolean()
                ? SoundEvents.AMETHYST_BLOCK_CHIME
                : SoundEvents.GOAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.AMETHYST_CLUSTER_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.AMETHYST_CLUSTER_BREAK;
    }

    @Override
    protected float getSoundVolume() {
        return 0.75F;
    }

    @Override
    public float getVoicePitch() {
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.12F + 1.15F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.AMETHYST_BLOCK_STEP, 0.3F, 1.15F);
    }

    static class QilinAntlerChargeGoal extends MeleeAttackGoal {
        private final CrystallineQilinEntity qilin;

        QilinAntlerChargeGoal(CrystallineQilinEntity qilin, double speedModifier) {
            super(qilin, speedModifier, true);
            this.qilin = qilin;
        }

        @Override
        public void start() {
            super.start();
            this.qilin.setSprinting(true);
        }

        @Override
        public void stop() {
            super.stop();
            this.qilin.setSprinting(false);
        }

        @Override
        public void tick() {
            super.tick();
            LivingEntity target = this.qilin.getTarget();
            if (target != null && target.isAlive()) {
                double distSqr = this.qilin.distanceToSqr(target);
                // Springy bounding leap when closing mid-range gap (3.5 - 7.5 blocks)
                if (this.qilin.onGround() && distSqr >= 12.25D && distSqr <= 56.25D && this.qilin.getRandom().nextInt(18) == 0) {
                    Vec3 dir = target.position().subtract(this.qilin.position());
                    Vec3 horiz = new Vec3(dir.x, 0.0D, dir.z);
                    if (horiz.lengthSqr() > 1.0E-4D) {
                        horiz = horiz.normalize().scale(0.45D);
                        Vec3 cur = this.qilin.getDeltaMovement();
                        this.qilin.setDeltaMovement(cur.x * 0.4D + horiz.x, 0.34D, cur.z * 0.4D + horiz.z);
                        this.qilin.hasImpulse = true;
                    }
                }
            }
        }
    }
}
