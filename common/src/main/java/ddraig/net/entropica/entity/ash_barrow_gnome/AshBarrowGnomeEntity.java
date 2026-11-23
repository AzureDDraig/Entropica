package ddraig.net.entropica.entity.ash_barrow_gnome;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * Fauna 47 — Ash Barrow-Gnome (Underground Digger) (entropica:ash_barrow_gnome).
 * Neutral squat slate spirit native to 6.4 Ash Canyons & 5.24 Skeleton Canyons:
 * - Waddles slowly (walk) and tucks above-ground under its wide 12x12 ash-dusted stone slab hat like a
 *   slate gravel cairn (hide / unhide), never burrowing into solid blocks.
 * - Swipes with a heavy stone shovel-claw arm when provoked (attack), dealing 5.0 damage + strong knockback.
 * - Scoops and grinds nearby gravel blocks or tossed gravel stacks (dig), converting them into bone dust
 *   (Bleached Bone Meal / Bone Meal), and topples into a Barrow cairn upon defeat (death).
 */
public class AshBarrowGnomeEntity extends PathfinderMob {

    public static final int STATE_ACTIVE = 0;
    public static final int STATE_HIDING = 1;
    public static final int STATE_UNHIDING = 2;

    private static final EntityDataAccessor<Integer> CAIRN_STATE =
            SynchedEntityData.defineId(AshBarrowGnomeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DIGGING_GRAVEL =
            SynchedEntityData.defineId(AshBarrowGnomeEntity.class, EntityDataSerializers.BOOLEAN);

    private static final byte EVENT_SHOVEL_SWIPE = 4;
    private static final byte EVENT_DIG_GRIND = 61;
    private static final byte EVENT_HIDE = 62;
    private static final byte EVENT_UNHIDE = 63;
    private static final byte EVENT_HURT_STAGGER = 64;

    private static final int ATTACK_DURATION_TICKS = 25;    // 1.25s animation
    private static final int DIG_DURATION_TICKS = 40;       // 2.00s animation
    private static final int DIG_FINISH_TICK = 28;
    private static final int UNHIDE_DURATION_TICKS = 20;    // 1.00s animation
    private static final int HURT_DURATION_TICKS = 13;      // 0.65s animation
    private static final int DEATH_DURATION_TICKS = 36;     // 1.80s animation
    private static final int RUNE_SHED_COOLDOWN_TICKS = 1200;

    private static final ResourceLocation BARROW_RUNE_STONE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "barrow_rune_stone");

    private static final DustParticleOptions BARROW_RUNE_DUST = new DustParticleOptions(0xA855F7, 0.55F);
    private static final BlockParticleOption GRAVEL_CRUMB_PARTICLE =
            new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GRAVEL.defaultBlockState());

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState digAnimationState = new AnimationState();
    public final AnimationState hideAnimationState = new AnimationState();
    public final AnimationState unhideAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    private int unhideTicks = 0;
    private int hideRestTimer = 220;
    private int digTicks = 0;
    private int digCooldown = 140;
    private @Nullable BlockPos targetGravelBlockPos = null;
    private boolean handFedGravel = false;
    private int runeShedCooldown = 0;

    public AshBarrowGnomeEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 6;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.40D)
                .add(Attributes.FOLLOW_RANGE, 20.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CAIRN_STATE, STATE_ACTIVE);
        builder.define(DIGGING_GRAVEL, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.22D, true) {
            @Override
            public boolean canUse() {
                return AshBarrowGnomeEntity.this.getCairnState() == STATE_ACTIVE && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return AshBarrowGnomeEntity.this.getCairnState() == STATE_ACTIVE && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(2, new GnomeGrindGravelGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.85D) {
            @Override
            public boolean canUse() {
                return AshBarrowGnomeEntity.this.getCairnState() == STATE_ACTIVE
                        && !AshBarrowGnomeEntity.this.isDiggingGravel()
                        && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return AshBarrowGnomeEntity.this.getCairnState() == STATE_ACTIVE
                        && !AshBarrowGnomeEntity.this.isDiggingGravel()
                        && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F) {
            @Override
            public boolean canUse() {
                return AshBarrowGnomeEntity.this.getCairnState() == STATE_ACTIVE && super.canUse();
            }
        });
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this) {
            @Override
            public boolean canUse() {
                return AshBarrowGnomeEntity.this.getCairnState() == STATE_ACTIVE && super.canUse();
            }
        });

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
    }

    public int getCairnState() {
        return this.entityData.get(CAIRN_STATE);
    }

    private void setCairnState(int state) {
        this.entityData.set(CAIRN_STATE, state);
    }

    public boolean isDiggingGravel() {
        return this.entityData.get(DIGGING_GRAVEL);
    }

    private void setDiggingGravel(boolean digging) {
        this.entityData.set(DIGGING_GRAVEL, digging);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    public static boolean isGravelItem(ItemStack stack) {
        return stack.is(Items.GRAVEL)
                || (ModItems.COARSE_GRAVEL_ITEM.isPresent() && stack.is(ModItems.COARSE_GRAVEL_ITEM.get()));
    }

    public static boolean isGravelWorldBlock(BlockState state) {
        return state.is(Blocks.GRAVEL)
                || (ModBlocks.COARSE_GRAVEL.isPresent() && state.is(ModBlocks.COARSE_GRAVEL.get()));
    }

    public static boolean isAshOrSkeletonCanyonSurface(BlockGetter level, BlockPos belowPos, BlockState belowState) {
        if (belowState.isAir()) {
            return false;
        }
        String path = BuiltInRegistries.BLOCK.getKey(belowState.getBlock()).getPath();
        return isGravelWorldBlock(belowState)
                || belowState.is(Blocks.BASALT)
                || belowState.is(Blocks.SMOOTH_BASALT)
                || belowState.is(Blocks.BLACKSTONE)
                || belowState.is(Blocks.STONE)
                || belowState.is(Blocks.DEEPSLATE)
                || belowState.is(Blocks.TUFF)
                || belowState.is(Blocks.BONE_BLOCK)
                || path.contains("ash")
                || path.contains("barrow")
                || path.contains("slate")
                || path.contains("gravel")
                || belowState.isFaceSturdy(level, belowPos, Direction.UP);
    }

    public static boolean checkAshBarrowGnomeSpawnRules(
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
        return isAshOrSkeletonCanyonSurface(level, belowPos, level.getBlockState(belowPos));
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        BlockPos belowPos = this.blockPosition().below();
        return isAshOrSkeletonCanyonSurface(level, belowPos, level.getBlockState(belowPos));
    }

    public void enterCairnHide(ServerLevel serverLevel) {
        if (this.getCairnState() != STATE_ACTIVE || this.isDiggingGravel() || this.getTarget() != null || !this.isAlive()) {
            return;
        }
        this.getNavigation().stop();
        this.setCairnState(STATE_HIDING);
        this.hideRestTimer = 160 + this.random.nextInt(180);
        serverLevel.broadcastEntityEvent(this, EVENT_HIDE);
        serverLevel.playSound(null, this.getX(), this.getY() + 0.4D, this.getZ(),
                SoundEvents.GRAVEL_PLACE, SoundSource.NEUTRAL, 0.75F, 0.85F);
        serverLevel.sendParticles(ParticleTypes.ASH,
                this.getX(), this.getY() + 0.55D, this.getZ(),
                12, 0.35D, 0.15D, 0.35D, 0.02D);
    }

    public void popOutOfCairnHide(ServerLevel serverLevel) {
        if (this.getCairnState() != STATE_HIDING || !this.isAlive()) {
            return;
        }
        this.setCairnState(STATE_UNHIDING);
        this.unhideTicks = UNHIDE_DURATION_TICKS;
        this.hideRestTimer = 240 + this.random.nextInt(240);
        serverLevel.broadcastEntityEvent(this, EVENT_UNHIDE);
        serverLevel.playSound(null, this.getX(), this.getY() + 0.5D, this.getZ(),
                SoundEvents.GRAVEL_STEP, SoundSource.NEUTRAL, 0.80F, 1.10F);
        serverLevel.sendParticles(ParticleTypes.WHITE_ASH,
                this.getX(), this.getY() + 0.95D, this.getZ(),
                16, 0.40D, 0.25D, 0.40D, 0.03D);
    }

    public void startGravelGrind(ServerLevel serverLevel, @Nullable BlockPos blockPos, boolean playerFed) {
        if (this.isDiggingGravel() || !this.isAlive()) {
            return;
        }
        if (this.getCairnState() == STATE_HIDING) {
            this.setCairnState(STATE_ACTIVE);
        }
        this.getNavigation().stop();
        this.digTicks = DIG_DURATION_TICKS;
        this.digCooldown = playerFed ? 30 : (280 + this.random.nextInt(220));
        this.targetGravelBlockPos = blockPos;
        this.handFedGravel = playerFed;
        this.setDiggingGravel(true);
        serverLevel.broadcastEntityEvent(this, EVENT_DIG_GRIND);
        serverLevel.playSound(null, this.getX(), this.getY() + 0.5D, this.getZ(),
                SoundEvents.GRAVEL_HIT, SoundSource.NEUTRAL, 0.85F, 0.90F);
    }

    private void completeGravelToBoneDustGrind(ServerLevel serverLevel) {
        boolean convertedSource = this.handFedGravel;

        if (!convertedSource && this.targetGravelBlockPos != null) {
            BlockState st = serverLevel.getBlockState(this.targetGravelBlockPos);
            if (isGravelWorldBlock(st)) {
                if (serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
                        && !this.hasPlayerContainerNearby(serverLevel, this.targetGravelBlockPos)) {
                    serverLevel.destroyBlock(this.targetGravelBlockPos, false, this);
                }
                convertedSource = true;
            }
        }

        if (convertedSource) {
            serverLevel.playSound(null, this.getX(), this.getY() + 0.55D, this.getZ(),
                    SoundEvents.GRINDSTONE_USE, SoundSource.NEUTRAL, 0.70F, 1.30F);
            serverLevel.sendParticles(GRAVEL_CRUMB_PARTICLE,
                    this.getX(), this.getY() + 0.50D, this.getZ(),
                    18, 0.30D, 0.20D, 0.30D, 0.08D);
            serverLevel.sendParticles(ParticleTypes.WHITE_ASH,
                    this.getX(), this.getY() + 0.65D, this.getZ(),
                    14, 0.35D, 0.25D, 0.35D, 0.02D);

            // Spawn 1-2 Bone Dust (Bleached Bone Meal when available, plus Bone Meal)
            int count = 1 + this.random.nextInt(2);
            Item boneDustItem = ModItems.BLEACHED_BONE_MEAL.isPresent()
                    ? ModItems.BLEACHED_BONE_MEAL.get()
                    : Items.BONE_MEAL;
            this.spawnAtLocation(serverLevel, new ItemStack(boneDustItem, count), 0.45F);

            if (this.handFedGravel && this.runeShedCooldown <= 0) {
                this.runeShedCooldown = RUNE_SHED_COOLDOWN_TICKS;
                Optional<Item> runeOpt = ModItems.BARROW_RUNE_STONE.isPresent()
                        ? Optional.of(ModItems.BARROW_RUNE_STONE.get())
                        : BuiltInRegistries.ITEM.getOptional(BARROW_RUNE_STONE_ID);
                runeOpt.ifPresent(rune ->
                        this.spawnAtLocation(serverLevel, new ItemStack(rune, 1), 0.55F));
            }
        }

        this.targetGravelBlockPos = null;
        this.handFedGravel = false;
    }

    private boolean hasPlayerContainerNearby(ServerLevel level, BlockPos center) {
        for (BlockPos check : BlockPos.betweenClosed(center.offset(-4, -2, -4), center.offset(4, 2, 4))) {
            BlockEntity be = level.getBlockEntity(check);
            if (be != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean doHurtTarget(ServerLevel serverLevel, Entity target) {
        serverLevel.broadcastEntityEvent(this, EVENT_SHOVEL_SWIPE);
        boolean hurt = super.doHurtTarget(serverLevel, target);
        if (hurt && target instanceof LivingEntity living) {
            // Heavy stone shovel-claw swipe applies strong knockback!
            double dx = living.getX() - this.getX();
            double dz = living.getZ() - this.getZ();
            double dist = Math.max(0.001D, Math.hypot(dx, dz));
            living.knockback(0.85D, -dx / dist, -dz / dist);
            living.hurtMarked = true;
            serverLevel.playSound(null, living.getX(), living.getY() + 0.5D, living.getZ(),
                    SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 0.35F, 1.45F);
        }
        return hurt;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if ((isGravelItem(stack) || stack.is(Items.FLINT)) && this.isAlive() && !this.isDiggingGravel()) {
            if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                stack.consume(1, player);
                this.heal(3.0F);
                this.startGravelGrind(serverLevel, null, true);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        // Wide 12x12 ash-covered stone slab hat halves projectile/ambient damage while tucked as a cairn
        float adjusted = amount;
        if (this.getCairnState() == STATE_HIDING && !source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            adjusted *= 0.50F;
        }
        boolean hurt = super.hurtServer(serverLevel, source, adjusted);
        if (hurt && this.isAlive()) {
            if (this.getCairnState() == STATE_HIDING) {
                this.popOutOfCairnHide(serverLevel);
            } else {
                serverLevel.broadcastEntityEvent(this, EVENT_HURT_STAGGER);
            }
        }
        return hurt;
    }

    @Override
    protected void tickDeath() {
        ++this.deathTime;
        if (this.level().isClientSide() && this.deathTime % 4 == 0) {
            this.level().addParticle(ParticleTypes.ASH,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.65D,
                    this.getY() + 0.45D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.65D,
                    0.0D, 0.02D, 0.0D);
        }
        if (this.deathTime >= DEATH_DURATION_TICKS && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_SHOVEL_SWIPE) {
            this.hurtAnimationState.stop();
            this.digAnimationState.stop();
            this.attackAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_DIG_GRIND) {
            this.attackAnimationState.stop();
            this.hideAnimationState.stop();
            this.digAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_HIDE) {
            this.unhideAnimationState.stop();
            this.walkAnimationState.stop();
            this.idleAnimationState.stop();
            this.hideAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_UNHIDE) {
            this.hideAnimationState.stop();
            this.unhideAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_HURT_STAGGER) {
            this.hurtAnimationState.start(this.tickCount);
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            if (this.digCooldown > 0) {
                this.digCooldown--;
            }
            if (this.runeShedCooldown > 0) {
                this.runeShedCooldown--;
            }

            if (this.isAlive()) {
                // Active gravel grinding cycle
                if (this.digTicks > 0) {
                    int elapsed = DIG_DURATION_TICKS - this.digTicks;
                    this.digTicks--;
                    this.getNavigation().stop();
                    if (elapsed > 4 && elapsed < DIG_FINISH_TICK && elapsed % 6 == 0) {
                        serverLevel.sendParticles(GRAVEL_CRUMB_PARTICLE,
                                this.getX(), this.getY() + 0.45D, this.getZ(),
                                5, 0.22D, 0.15D, 0.22D, 0.05D);
                        serverLevel.playSound(null, this.getX(), this.getY() + 0.45D, this.getZ(),
                                SoundEvents.GRAVEL_STEP, SoundSource.NEUTRAL, 0.55F, 0.85F);
                    }
                    if (elapsed == DIG_FINISH_TICK) {
                        this.completeGravelToBoneDustGrind(serverLevel);
                    }
                    if (this.digTicks == 0) {
                        this.setDiggingGravel(false);
                    }
                }

                // Cairn stone-hat hide / unhide cycle (strictly above ground; never burrows into blocks!)
                int state = this.getCairnState();
                if (state == STATE_UNHIDING) {
                    if (--this.unhideTicks <= 0) {
                        this.setCairnState(STATE_ACTIVE);
                    }
                } else if (state == STATE_HIDING) {
                    this.getNavigation().stop();
                    Vec3 v = this.getDeltaMovement();
                    this.setDeltaMovement(0.0D, v.y, 0.0D);
                    if (this.getTarget() != null || --this.hideRestTimer <= 0) {
                        this.popOutOfCairnHide(serverLevel);
                    }
                } else if (this.getTarget() == null && !this.isDiggingGravel() && --this.hideRestTimer <= 0) {
                    // Tuck under wide ash-covered stone hat if resting near gravel/ash or startled by sprinting players
                    Player sprintingNearby = serverLevel.getNearestPlayer(this.getX(), this.getY(), this.getZ(), 6.0D,
                            e -> e instanceof Player p && !p.isSpectator() && p.isSprinting());
                    boolean onCanyonSurface = isAshOrSkeletonCanyonSurface(
                            serverLevel, this.blockPosition().below(), serverLevel.getBlockState(this.blockPosition().below()));
                    if (sprintingNearby != null || (onCanyonSurface && this.random.nextInt(3) == 0)) {
                        this.enterCairnHide(serverLevel);
                    } else {
                        this.hideRestTimer = 180 + this.random.nextInt(180);
                    }
                }
            } else {
                this.setDiggingGravel(false);
            }
        } else if (this.level().isClientSide()) {
            this.setupClientAnimationsAndParticles();
        }
    }

    private void setupClientAnimationsAndParticles() {
        if (!this.isAlive()) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.attackAnimationState.stop();
            this.digAnimationState.stop();
            this.hideAnimationState.stop();
            this.unhideAnimationState.stop();
            this.hurtAnimationState.stop();
            this.deathAnimationState.startIfStopped(this.tickCount);
            return;
        }

        if (this.attackAnimationState.isStarted()
                && this.attackAnimationState.getTimeInMillis(this.tickCount) >= ATTACK_DURATION_TICKS * 50L) {
            this.attackAnimationState.stop();
        }
        if (this.digAnimationState.isStarted()
                && this.digAnimationState.getTimeInMillis(this.tickCount) >= DIG_DURATION_TICKS * 50L) {
            this.digAnimationState.stop();
        }
        if (this.unhideAnimationState.isStarted()
                && this.unhideAnimationState.getTimeInMillis(this.tickCount) >= UNHIDE_DURATION_TICKS * 50L) {
            this.unhideAnimationState.stop();
        }
        if (this.hurtAnimationState.isStarted()
                && this.hurtAnimationState.getTimeInMillis(this.tickCount) >= HURT_DURATION_TICKS * 50L) {
            this.hurtAnimationState.stop();
        }

        int state = this.getCairnState();
        if (state == STATE_HIDING) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.hideAnimationState.startIfStopped(this.tickCount);
        } else {
            this.hideAnimationState.stop();
            boolean waddling = this.getDeltaMovement().horizontalDistanceSqr() > 2.0E-4D;
            if (waddling) {
                this.idleAnimationState.stop();
                this.walkAnimationState.startIfStopped(this.tickCount);
            } else {
                this.walkAnimationState.stop();
                this.idleAnimationState.startIfStopped(this.tickCount);
            }
        }

        // Faint purple Barrow Rune-Stone motes from the inscribed stone recessed in its back
        if (this.tickCount % 16 == 0 && state != STATE_HIDING) {
            this.level().addParticle(
                    BARROW_RUNE_DUST,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.35D,
                    this.getY() + 0.55D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.35D,
                    0.0D, 0.01D, 0.0D
            );
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("RuneShedCooldown", Codec.INT, this.runeShedCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.runeShedCooldown = input.read("RuneShedCooldown", Codec.INT).orElse(0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.TUFF_STEP;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.DEEPSLATE_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.DEEPSLATE_BREAK;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.TUFF_STEP, 0.35F, 0.90F);
    }

    private static final class GnomeGrindGravelGoal extends Goal {
        private final AshBarrowGnomeEntity gnome;
        private @Nullable ItemEntity droppedGravel = null;
        private @Nullable BlockPos gravelBlockPos = null;

        GnomeGrindGravelGoal(AshBarrowGnomeEntity gnome) {
            this.gnome = gnome;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.gnome.getCairnState() != STATE_ACTIVE
                    || this.gnome.isDiggingGravel()
                    || this.gnome.getTarget() != null
                    || this.gnome.digCooldown > 0) {
                return false;
            }

            // 1. Check for nearby tossed Gravel / Coarse Gravel items on the ground
            List<ItemEntity> items = this.gnome.level().getEntitiesOfClass(
                    ItemEntity.class,
                    this.gnome.getBoundingBox().inflate(5.0D, 2.0D, 5.0D),
                    ie -> ie.isAlive() && !ie.hasPickUpDelay() && isGravelItem(ie.getItem())
            );
            if (!items.isEmpty()) {
                this.droppedGravel = items.get(0);
                this.gravelBlockPos = null;
                return true;
            }

            // 2. Otherwise occasionally scoop & grind a nearby natural Gravel / Coarse Gravel block
            if (this.gnome.getRandom().nextInt(18) != 0) {
                return false;
            }
            BlockPos origin = this.gnome.blockPosition();
            for (BlockPos check : BlockPos.betweenClosed(origin.offset(-4, -1, -4), origin.offset(4, 1, 4))) {
                if (isGravelWorldBlock(this.gnome.level().getBlockState(check))
                        && this.gnome.level().getBlockState(check.above()).isAir()) {
                    this.gravelBlockPos = check.immutable();
                    this.droppedGravel = null;
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.gnome.getCairnState() != STATE_ACTIVE
                    || this.gnome.isDiggingGravel()
                    || this.gnome.getTarget() != null) {
                return false;
            }
            if (this.droppedGravel != null) {
                return this.droppedGravel.isAlive() && isGravelItem(this.droppedGravel.getItem());
            }
            return this.gravelBlockPos != null && isGravelWorldBlock(this.gnome.level().getBlockState(this.gravelBlockPos));
        }

        @Override
        public void tick() {
            if (!(this.gnome.level() instanceof ServerLevel serverLevel)) {
                return;
            }
            if (this.droppedGravel != null && this.droppedGravel.isAlive()) {
                this.gnome.getLookControl().setLookAt(this.droppedGravel, 30.0F, 30.0F);
                if (this.gnome.distanceToSqr(this.droppedGravel) <= 2.5D) {
                    ItemStack stack = this.droppedGravel.getItem();
                    stack.shrink(1);
                    if (stack.isEmpty()) {
                        this.droppedGravel.discard();
                    }
                    this.gnome.startGravelGrind(serverLevel, null, true);
                } else {
                    this.gnome.getNavigation().moveTo(this.droppedGravel, 1.0D);
                }
            } else if (this.gravelBlockPos != null) {
                Vec3 center = Vec3.atCenterOf(this.gravelBlockPos);
                this.gnome.getLookControl().setLookAt(center.x, center.y, center.z, 30.0F, 30.0F);
                if (this.gnome.position().distanceToSqr(center) <= 3.2D) {
                    this.gnome.startGravelGrind(serverLevel, this.gravelBlockPos, false);
                } else {
                    this.gnome.getNavigation().moveTo(center.x, center.y + 1.0D, center.z, 0.95D);
                }
            }
        }
    }
}
