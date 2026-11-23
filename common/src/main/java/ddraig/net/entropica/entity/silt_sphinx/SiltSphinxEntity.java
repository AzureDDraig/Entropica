package ddraig.net.entropica.entity.silt_sphinx;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
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
import net.minecraft.world.DifficultyInstance;
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
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * Fauna 40 — Silt-Sphinx (Canyon Guardian) (entropica:silt_sphinx).
 * Articulated sandstone sphinx native to 5.4 Dusty Canyons & 5.23 Petrified Wood:
 * - Two natural 64x64 variants: Red Canyon Silt-Stone Monolith (0) and Gilded Pharaoh Gold & Lapis (1).
 * - Monument Lifecycle: reclines motionless as a couchant sphinx statue (dormant), awakens with a
 *   2.2s silt-shaking stone shudder (awaken) when intruders open unwarded chests or trespass too close,
 *   prowls on four heavy stone lion legs, and reclines back into monument pose (return_to_dormant)
 *   once intruders depart.
 * - Ancient Chest & Rune Seal Mechanic: if local runes are solved nearby (powered Redstone seal,
 *   powered Lectern / Chiseled Bookshelf / Chiseled Sandstone, or lit Runed Brazier), chests can be
 *   opened safely without waking the Sphinx, and offering Gold Ingots or Lapis Lazuli gilds the Sphinx
 *   and coaxes it to shed a Sphinx Sand-Stone on cooldown.
 * - Winged Double-Paw Ground Slam (attack): deals 8.0 damage and kicks up a 3.5-block silt cloud
 *   that inflicts Blindness (3s) and Slowness (2s).
 */
public class SiltSphinxEntity extends Monster {

    public static final int VARIANT_SILT_STONE = 0;
    public static final int VARIANT_PHARAOH = 1;

    public static final int STATE_DORMANT = 0;
    public static final int STATE_AWAKENING = 1;
    public static final int STATE_ACTIVE = 2;
    public static final int STATE_RETURNING_TO_DORMANT = 3;

    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(SiltSphinxEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MONUMENT_STATE =
            SynchedEntityData.defineId(SiltSphinxEntity.class, EntityDataSerializers.INT);

    private static final byte EVENT_ATTACK = 4;
    private static final byte EVENT_AWAKEN = 63;
    private static final byte EVENT_RETURN_DORMANT = 64;

    private static final int ATTACK_DURATION_TICKS = 34;         // 1.7s animation
    private static final int AWAKEN_DURATION_TICKS = 44;         // 2.2s animation
    private static final int RETURN_DORMANT_DURATION_TICKS = 44; // 2.2s animation
    private static final int IDLE_BEFORE_DORMANT_TICKS = 200;    // 10s calm before reclining
    private static final int STONE_SHED_COOLDOWN_TICKS = 1200;

    private static final ResourceLocation SPHINX_SAND_STONE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "sphinx_sand_stone");

    private static final DustParticleOptions PHARAOH_GOLD_DUST = new DustParticleOptions(0xFACC15, 0.70F);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState dormantAnimationState = new AnimationState();
    public final AnimationState awakenAnimationState = new AnimationState();
    public final AnimationState returnToDormantAnimationState = new AnimationState();

    private int transitionTicks = 0;
    private int calmTicksWithoutTarget = 0;
    private int chamberScanTicker = 0;
    private boolean guardingChamberChest = false;
    private boolean localRuneSolved = false;
    private float monumentYaw = 0.0F;
    private boolean monumentYawInitialized = false;
    private int stoneShedCooldown = 0;

    public SiltSphinxEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 12;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 120.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 2.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.70D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, VARIANT_SILT_STONE);
        builder.define(MONUMENT_STATE, STATE_DORMANT);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MonumentPoseHoldGoal(this));
        this.goalSelector.addGoal(2, new SphinxMeleeAttackGoal(this, 1.18D));
        this.goalSelector.addGoal(3, new SphinxActiveStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(4, new SphinxActiveLookGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(5, new SphinxActiveRandomLookGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public int getVariant() {
        return this.entityData.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(VARIANT, variant == VARIANT_PHARAOH ? VARIANT_PHARAOH : VARIANT_SILT_STONE);
    }

    public boolean isPharaohVariant() {
        return this.getVariant() == VARIANT_PHARAOH;
    }

    public int getMonumentState() {
        return this.entityData.get(MONUMENT_STATE);
    }

    public void setMonumentState(int state) {
        this.entityData.set(MONUMENT_STATE, state);
    }

    public boolean isDormantOrTransitioning() {
        return this.getMonumentState() != STATE_ACTIVE;
    }

    public boolean isFullyDormant() {
        return this.getMonumentState() == STATE_DORMANT;
    }

    private static boolean isCanyonOrPetrifiedSurface(BlockGetter level, BlockPos belowPos, BlockState belowState) {
        if (belowState.isAir()) {
            return false;
        }
        String path = BuiltInRegistries.BLOCK.getKey(belowState.getBlock()).getPath();
        return belowState.is(Blocks.RED_SAND)
                || belowState.is(Blocks.RED_SANDSTONE)
                || belowState.is(Blocks.CUT_RED_SANDSTONE)
                || belowState.is(Blocks.CHISELED_RED_SANDSTONE)
                || belowState.is(Blocks.SAND)
                || belowState.is(Blocks.SANDSTONE)
                || belowState.is(Blocks.CHISELED_SANDSTONE)
                || belowState.is(BlockTags.TERRACOTTA)
                || path.contains("silt")
                || path.contains("canyon")
                || path.contains("petrified")
                || (belowState.getFluidState().isEmpty() && belowState.isFaceSturdy(level, belowPos, Direction.UP));
    }

    public static boolean checkSiltSphinxSpawnRules(
            EntityType<? extends Monster> type,
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
                || spawnReason == EntitySpawnReason.COMMAND
                || spawnReason == EntitySpawnReason.STRUCTURE) {
            return true;
        }
        BlockPos belowPos = pos.below();
        return isCanyonOrPetrifiedSurface(level, belowPos, level.getBlockState(belowPos));
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        BlockPos belowPos = this.blockPosition().below();
        if (isCanyonOrPetrifiedSurface(level, belowPos, level.getBlockState(belowPos))) {
            return true;
        }
        return super.checkSpawnRules(level, spawnReason);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !this.isFullyDormant() && !this.guardingChamberChest;
    }

    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason spawnType,
            @Nullable SpawnGroupData spawnGroupData
    ) {
        BlockPos origin = this.blockPosition();
        boolean nearRoyalTomb = false;
        for (BlockPos check : BlockPos.betweenClosed(origin.offset(-4, -2, -4), origin.offset(4, 2, 4))) {
            BlockState st = level.getBlockState(check);
            if (st.is(Blocks.GOLD_BLOCK)
                    || st.is(Blocks.LAPIS_BLOCK)
                    || st.is(Blocks.CHISELED_SANDSTONE)
                    || st.is(Blocks.CHISELED_RED_SANDSTONE)) {
                nearRoyalTomb = true;
                break;
            }
        }
        Holder<Biome> biome = level.getBiome(origin);
        String biomePath = biome.unwrapKey().map(k -> k.location().getPath()).orElse("");
        if (nearRoyalTomb || biomePath.contains("pharaoh")) {
            this.setVariant(VARIANT_PHARAOH);
        } else {
            this.setVariant(this.random.nextFloat() < 0.30F ? VARIANT_PHARAOH : VARIANT_SILT_STONE);
        }
        this.setMonumentState(STATE_DORMANT);
        this.monumentYaw = Math.round(this.getYRot() / 90.0F) * 90.0F;
        this.monumentYawInitialized = true;
        this.setYRot(this.monumentYaw);
        this.setYBodyRot(this.monumentYaw);
        this.setYHeadRot(this.monumentYaw);
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    private void scanAncientChamberAndRunes(ServerLevel level) {
        BlockPos origin = this.blockPosition();
        boolean foundChest = false;
        boolean foundRuneSeal = false;

        for (BlockPos check : BlockPos.betweenClosed(origin.offset(-9, -3, -9), origin.offset(9, 4, 9))) {
            BlockState state = level.getBlockState(check);
            if (state.isAir()) {
                continue;
            }
            BlockEntity be = level.getBlockEntity(check);
            if (be instanceof ChestBlockEntity || be instanceof BarrelBlockEntity || be instanceof ShulkerBoxBlockEntity) {
                foundChest = true;
            }

            String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
            if (state.is(Blocks.REDSTONE_BLOCK)
                    || (state.is(Blocks.REDSTONE_LAMP) && state.getOptionalValue(BlockStateProperties.LIT).orElse(false))
                    || ("runed_brazier".equals(path) && state.getOptionalValue(BlockStateProperties.LIT).orElse(false))
                    || path.startsWith("autocast_pedestal")
                    || ((state.is(Blocks.LECTERN)
                        || state.is(Blocks.CHISELED_BOOKSHELF)
                        || state.is(Blocks.CHISELED_SANDSTONE)
                        || state.is(Blocks.CHISELED_RED_SANDSTONE))
                        && level.hasNeighborSignal(check))) {
                foundRuneSeal = true;
            }
        }

        this.guardingChamberChest = foundChest;
        this.localRuneSolved = foundRuneSeal;
    }

    private void awakenFromMonument(ServerLevel serverLevel, @Nullable LivingEntity intruder) {
        if (this.getMonumentState() == STATE_ACTIVE || this.getMonumentState() == STATE_AWAKENING) {
            if (intruder != null && this.getTarget() == null) {
                this.setTarget(intruder);
            }
            return;
        }

        this.setMonumentState(STATE_AWAKENING);
        this.transitionTicks = AWAKEN_DURATION_TICKS;
        this.calmTicksWithoutTarget = 0;
        this.getNavigation().stop();
        if (intruder != null) {
            this.setTarget(intruder);
        }
        serverLevel.broadcastEntityEvent(this, EVENT_AWAKEN);

        serverLevel.playSound(null, this.getX(), this.getY() + 0.8D, this.getZ(),
                SoundEvents.DEEPSLATE_BRICKS_BREAK, SoundSource.HOSTILE, 1.0F, 0.65F);
        serverLevel.playSound(null, this.getX(), this.getY() + 0.8D, this.getZ(),
                SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 0.70F, 0.75F);

        BlockState siltState = Blocks.RED_SAND.defaultBlockState();
        serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, siltState),
                this.getX(), this.getY() + 0.9D, this.getZ(),
                36, 1.15D, 0.50D, 1.15D, 0.06D);
    }

    private void beginReturnToDormant(ServerLevel serverLevel) {
        this.setMonumentState(STATE_RETURNING_TO_DORMANT);
        this.transitionTicks = RETURN_DORMANT_DURATION_TICKS;
        this.getNavigation().stop();
        this.monumentYaw = this.getYRot();
        this.monumentYawInitialized = true;
        serverLevel.broadcastEntityEvent(this, EVENT_RETURN_DORMANT);
        serverLevel.playSound(null, this.getX(), this.getY() + 0.5D, this.getZ(),
                SoundEvents.DEEPSLATE_BRICKS_PLACE, SoundSource.HOSTILE, 0.85F, 0.75F);
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        if (source.is(DamageTypes.CACTUS) || source.is(DamageTypes.IN_WALL) || source.is(DamageTypeTags.IS_FALL)) {
            return true;
        }
        return super.isInvulnerableTo(level, source);
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        float adjusted = (this.isFullyDormant() && source.is(DamageTypeTags.IS_PROJECTILE)) ? amount * 0.5F : amount;
        boolean hurt = super.hurtServer(serverLevel, source, adjusted);
        if (hurt && this.isAlive()) {
            LivingEntity attacker = source.getEntity() instanceof LivingEntity liv ? liv : null;
            this.awakenFromMonument(serverLevel, attacker);
        }
        return hurt;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        level.broadcastEntityEvent(this, EVENT_ATTACK);

        BlockState siltBlock = Blocks.RED_SAND.defaultBlockState();
        BlockState canyonStone = Blocks.TERRACOTTA.defaultBlockState();

        // 3.5-block Winged Double-Paw Ground Slam silt cloud (Blindness 3s + Slowness 2s)
        List<LivingEntity> victims = level.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(3.5D),
                e -> e != this && e.isAlive() && !(e instanceof SiltSphinxEntity) && this.distanceToSqr(e) <= 12.25D
        );
        for (LivingEntity victim : victims) {
            if (victim == target || victim == this.getLastHurtByMob() || (victim instanceof Player p && !p.isCreative() && !p.isSpectator())) {
                victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0), this);
                victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0), this);
                if (victim != target) {
                    double dx = this.getX() - victim.getX();
                    double dz = this.getZ() - victim.getZ();
                    victim.knockback(0.45D, dx, dz);
                }
            }
        }

        if (hit && target instanceof LivingEntity living) {
            double dx = this.getX() - living.getX();
            double dz = this.getZ() - living.getZ();
            living.knockback(0.65D, dx, dz);
            Vec3 cur = living.getDeltaMovement();
            living.setDeltaMovement(cur.x, Math.max(cur.y, 0.25D), cur.z);
            living.hasImpulse = true;
            living.hurtMarked = true;
        }

        level.playSound(null, this.getX(), this.getY() + 0.6D, this.getZ(),
                SoundEvents.DEEPSLATE_BRICKS_FALL, SoundSource.HOSTILE, 1.0F, 0.65F);
        level.playSound(null, this.getX(), this.getY() + 0.6D, this.getZ(),
                SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 0.45F, 0.60F);

        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, canyonStone),
                this.getX(), this.getY() + 0.25D, this.getZ(),
                30, 1.30D, 0.20D, 1.30D, 0.14D);
        level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, siltBlock),
                this.getX(), this.getY() + 0.85D, this.getZ(),
                30, 1.45D, 0.55D, 1.45D, 0.05D);

        return hit;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean royalTribute = stack.is(Items.GOLD_INGOT) || stack.is(Items.LAPIS_LAZULI);
        if (royalTribute && this.isAlive() && this.isFullyDormant() && this.localRuneSolved) {
            boolean needsHeal = this.getHealth() < this.getMaxHealth();
            boolean canShed = this.stoneShedCooldown <= 0;
            if (needsHeal || canShed || !this.isPharaohVariant()) {
                if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                    stack.consume(1, player);
                    this.setVariant(VARIANT_PHARAOH);
                    this.heal(6.0F);
                    serverLevel.playSound(null, this.getX(), this.getY() + 0.9D, this.getZ(),
                            SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.NEUTRAL, 0.70F, 1.20F);
                    serverLevel.sendParticles(PHARAOH_GOLD_DUST,
                            this.getX(), this.getY() + 1.05D, this.getZ(),
                            16, 0.55D, 0.40D, 0.55D, 0.02D);

                    if (canShed) {
                        this.stoneShedCooldown = STONE_SHED_COOLDOWN_TICKS;
                        Optional<Item> stoneOpt = ModItems.SPHINX_SAND_STONE.isPresent()
                                ? Optional.of(ModItems.SPHINX_SAND_STONE.get())
                                : BuiltInRegistries.ITEM.getOptional(SPHINX_SAND_STONE_ID);
                        stoneOpt.ifPresent(stone ->
                                this.spawnAtLocation(serverLevel, new ItemStack(stone, 1), 0.75F));
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_ATTACK) {
            this.awakenAnimationState.stop();
            this.returnToDormantAnimationState.stop();
            this.attackAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_AWAKEN) {
            this.dormantAnimationState.stop();
            this.returnToDormantAnimationState.stop();
            this.attackAnimationState.stop();
            this.awakenAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_RETURN_DORMANT) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.attackAnimationState.stop();
            this.awakenAnimationState.stop();
            this.returnToDormantAnimationState.start(this.tickCount);
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.monumentYawInitialized) {
            this.monumentYaw = this.getYRot();
            this.monumentYawInitialized = true;
        }

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            if (this.stoneShedCooldown > 0) {
                this.stoneShedCooldown--;
            }

            if (++this.chamberScanTicker >= 20) {
                this.chamberScanTicker = 0;
                this.scanAncientChamberAndRunes(serverLevel);
            }

            int state = this.getMonumentState();
            if (state == STATE_DORMANT) {
                this.getNavigation().stop();
                this.setYRot(this.monumentYaw);
                this.setYBodyRot(this.monumentYaw);
                this.setYHeadRot(this.monumentYaw);
                this.setXRot(0.0F);

                if (!this.localRuneSolved) {
                    List<Player> nearbyPlayers = serverLevel.getEntitiesOfClass(
                            Player.class,
                            this.getBoundingBox().inflate(14.0D),
                            p -> p.isAlive() && !p.isCreative() && !p.isSpectator()
                    );
                    for (Player p : nearbyPlayers) {
                        double distSqr = this.distanceToSqr(p);
                        boolean openedContainer = p.containerMenu != p.inventoryMenu && distSqr <= 196.0D && this.hasLineOfSight(p);
                        // When guarding an ancient chest chamber, ONLY attack if a player opens a chest without solving local runes!
                        boolean wildCanyonTrespass = !this.guardingChamberChest && distSqr <= 64.0D && this.hasLineOfSight(p);
                        if (openedContainer || wildCanyonTrespass) {
                            this.awakenFromMonument(serverLevel, p);
                            break;
                        }
                    }
                }
            } else if (state == STATE_AWAKENING) {
                this.getNavigation().stop();
                if (this.transitionTicks % 5 == 0) {
                    serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.RED_SAND.defaultBlockState()),
                            this.getX(), this.getY() + 0.85D, this.getZ(),
                            8, 0.75D, 0.35D, 0.75D, 0.03D);
                }
                if (--this.transitionTicks <= 0) {
                    this.setMonumentState(STATE_ACTIVE);
                }
            } else if (state == STATE_ACTIVE) {
                LivingEntity currentTarget = this.getTarget();
                if (currentTarget != null && (!currentTarget.isAlive()
                        || (currentTarget instanceof Player p && (p.isCreative() || p.isSpectator()))
                        || this.distanceToSqr(currentTarget) > 676.0D)) {
                    this.setTarget(null);
                    currentTarget = null;
                }

                if (currentTarget == null && !this.localRuneSolved) {
                    List<Player> raiders = serverLevel.getEntitiesOfClass(
                            Player.class,
                            this.getBoundingBox().inflate(12.0D),
                            p -> p.isAlive() && !p.isCreative() && !p.isSpectator() && this.hasLineOfSight(p)
                                    && (this.guardingChamberChest
                                        ? (p.containerMenu != p.inventoryMenu)
                                        : (this.distanceToSqr(p) <= 49.0D))
                    );
                    if (!raiders.isEmpty()) {
                        this.setTarget(raiders.get(0));
                        currentTarget = raiders.get(0);
                    }
                }

                if (currentTarget == null) {
                    if (this.onGround() && ++this.calmTicksWithoutTarget >= IDLE_BEFORE_DORMANT_TICKS) {
                        this.calmTicksWithoutTarget = 0;
                        this.beginReturnToDormant(serverLevel);
                    }
                } else {
                    this.calmTicksWithoutTarget = 0;
                }
            } else if (state == STATE_RETURNING_TO_DORMANT) {
                this.getNavigation().stop();
                if (--this.transitionTicks <= 0) {
                    this.setMonumentState(STATE_DORMANT);
                    this.monumentYaw = this.getYRot();
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
                && this.attackAnimationState.getTimeInMillis(this.tickCount) >= ATTACK_DURATION_TICKS * 50L) {
            this.attackAnimationState.stop();
        }
        if (this.awakenAnimationState.isStarted()
                && this.awakenAnimationState.getTimeInMillis(this.tickCount) >= AWAKEN_DURATION_TICKS * 50L) {
            this.awakenAnimationState.stop();
        }
        if (this.returnToDormantAnimationState.isStarted()
                && this.returnToDormantAnimationState.getTimeInMillis(this.tickCount) >= RETURN_DORMANT_DURATION_TICKS * 50L) {
            this.returnToDormantAnimationState.stop();
        }

        int state = this.getMonumentState();
        if (state == STATE_DORMANT) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.dormantAnimationState.startIfStopped(this.tickCount);
        } else if (state == STATE_AWAKENING) {
            this.dormantAnimationState.stop();
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.awakenAnimationState.startIfStopped(this.tickCount);
        } else if (state == STATE_RETURNING_TO_DORMANT) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.dormantAnimationState.stop();
            this.returnToDormantAnimationState.startIfStopped(this.tickCount);
        } else {
            this.dormantAnimationState.stop();
            boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
            if (moving) {
                this.idleAnimationState.stop();
                this.walkAnimationState.startIfStopped(this.tickCount);
            } else {
                this.walkAnimationState.stop();
                this.idleAnimationState.startIfStopped(this.tickCount);
            }
        }

        if (state == STATE_ACTIVE && this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D && this.tickCount % 5 == 0) {
            this.level().addParticle(
                    new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.RED_SAND.defaultBlockState()),
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.90D,
                    this.getY() + 0.15D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.90D,
                    0.0D, 0.0D, 0.0D
            );
        } else if (this.isPharaohVariant() && state == STATE_ACTIVE && this.tickCount % 16 == 0) {
            this.level().addParticle(
                    PHARAOH_GOLD_DUST,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.60D,
                    this.getY() + 1.20D + (this.random.nextDouble() - 0.5D) * 0.30D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.60D,
                    0.0D, 0.01D, 0.0D
            );
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("Variant", Codec.INT, this.getVariant());
        int persistedState = (this.getMonumentState() == STATE_ACTIVE) ? STATE_ACTIVE : STATE_DORMANT;
        output.store("MonumentState", Codec.INT, persistedState);
        output.store("MonumentYaw", Codec.FLOAT, this.monumentYaw);
        output.store("StoneShedCooldown", Codec.INT, this.stoneShedCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setVariant(input.read("Variant", Codec.INT).orElse(VARIANT_SILT_STONE));
        this.setMonumentState(input.read("MonumentState", Codec.INT).orElse(STATE_DORMANT));
        this.monumentYaw = input.read("MonumentYaw", Codec.FLOAT).orElse(this.getYRot());
        this.monumentYawInitialized = true;
        this.stoneShedCooldown = input.read("StoneShedCooldown", Codec.INT).orElse(0);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.isFullyDormant() ? null : SoundEvents.DEEPSLATE_BRICKS_STEP;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.DEEPSLATE_BRICKS_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.DEEPSLATE_BRICKS_BREAK;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        if (!this.isDormantOrTransitioning()) {
            this.playSound(SoundEvents.DEEPSLATE_BRICKS_STEP, 0.45F, 0.75F);
        }
    }

    private static final class MonumentPoseHoldGoal extends net.minecraft.world.entity.ai.goal.Goal {
        private final SiltSphinxEntity sphinx;

        MonumentPoseHoldGoal(SiltSphinxEntity sphinx) {
            this.sphinx = sphinx;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return this.sphinx.isDormantOrTransitioning();
        }

        @Override
        public void tick() {
            this.sphinx.getNavigation().stop();
        }
    }

    private static final class SphinxMeleeAttackGoal extends MeleeAttackGoal {
        private final SiltSphinxEntity sphinx;

        SphinxMeleeAttackGoal(SiltSphinxEntity sphinx, double speedModifier) {
            super(sphinx, speedModifier, true);
            this.sphinx = sphinx;
        }

        @Override
        public boolean canUse() {
            return !this.sphinx.isDormantOrTransitioning() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !this.sphinx.isDormantOrTransitioning() && super.canContinueToUse();
        }
    }

    private static final class SphinxActiveStrollGoal extends WaterAvoidingRandomStrollGoal {
        private final SiltSphinxEntity sphinx;

        SphinxActiveStrollGoal(SiltSphinxEntity sphinx, double speedModifier) {
            super(sphinx, speedModifier);
            this.sphinx = sphinx;
        }

        @Override
        public boolean canUse() {
            return !this.sphinx.isDormantOrTransitioning() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !this.sphinx.isDormantOrTransitioning() && super.canContinueToUse();
        }
    }

    private static final class SphinxActiveLookGoal extends LookAtPlayerGoal {
        private final SiltSphinxEntity sphinx;

        SphinxActiveLookGoal(SiltSphinxEntity sphinx, Class<? extends LivingEntity> lookAtType, float lookDistance) {
            super(sphinx, lookAtType, lookDistance);
            this.sphinx = sphinx;
        }

        @Override
        public boolean canUse() {
            return !this.sphinx.isDormantOrTransitioning() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !this.sphinx.isDormantOrTransitioning() && super.canContinueToUse();
        }
    }

    private static final class SphinxActiveRandomLookGoal extends RandomLookAroundGoal {
        private final SiltSphinxEntity sphinx;

        SphinxActiveRandomLookGoal(SiltSphinxEntity sphinx) {
            super(sphinx);
            this.sphinx = sphinx;
        }

        @Override
        public boolean canUse() {
            return !this.sphinx.isDormantOrTransitioning() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !this.sphinx.isDormantOrTransitioning() && super.canContinueToUse();
        }
    }
}
