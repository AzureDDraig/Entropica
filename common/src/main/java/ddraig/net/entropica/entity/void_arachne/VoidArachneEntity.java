package ddraig.net.entropica.entity.void_arachne;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
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
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * Fauna 46 — Void Arachne (Nether Spinner) (entropica:void_arachne).
 * Hostile mythological spider-humanoid weaver native to 6.5 Rift Flats & 6.2 Basalt Delves:
 * - Skitters across cavern floors and climbs vertical walls (walk), nesting near ancient nether portal ruins
 *   while weaving Void silk between her runic hands (idle).
 * - Casts rooting Void silk nets that snare targets for 3 seconds (attack), then lunges in to clamp with her
 *   venomous obsidian fangs for 6.0 damage (bite).
 * - Blinks out into a thin vertical portal rift (blink_out) and blinks back in (blink_in) to dodge attacks,
 *   leaving purple portal particles in-game.
 */
public class VoidArachneEntity extends Monster {

    private static final EntityDataAccessor<Boolean> CLIMBING =
            SynchedEntityData.defineId(VoidArachneEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> BLINK_PHASING =
            SynchedEntityData.defineId(VoidArachneEntity.class, EntityDataSerializers.BOOLEAN);

    private static final byte EVENT_SILK_NET = 4;
    private static final byte EVENT_FANG_BITE = 61;
    private static final byte EVENT_BLINK_OUT = 62;
    private static final byte EVENT_BLINK_IN = 63;
    private static final byte EVENT_HURT_SHIELD = 64;

    private static final int SILK_NET_DURATION_TICKS = 31;  // 1.55s animation
    private static final int SILK_NET_RELEASE_TICK = 16;
    private static final int SNARE_ROOT_DURATION_TICKS = 60; // 3.00s snare

    private static final int FANG_BITE_DURATION_TICKS = 24; // 1.20s animation
    private static final int FANG_BITE_IMPACT_TICK = 10;

    private static final int BLINK_OUT_DURATION_TICKS = 19; // 0.95s animation
    private static final int BLINK_IN_DURATION_TICKS = 19;  // 0.95s animation
    private static final int BLINK_COOLDOWN_TICKS = 90;

    private static final int HURT_DURATION_TICKS = 13;      // 0.65s animation
    private static final int DEATH_DURATION_TICKS = 44;     // 2.20s animation
    private static final int SPINDLE_HARVEST_COOLDOWN_TICKS = 1200;

    private static final ResourceLocation VOID_WEB_SPINDLE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "void_web_spindle");

    private static final DustParticleOptions VOID_SILK_DUST = new DustParticleOptions(0xD946EF, 0.65F);
    private static final DustParticleOptions PORTAL_VIOLET_DUST = new DustParticleOptions(0x9D4EDD, 0.70F);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState biteAnimationState = new AnimationState();
    public final AnimationState blinkOutAnimationState = new AnimationState();
    public final AnimationState blinkInAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    private int silkNetTicks = 0;
    private int silkNetCooldown = 0;
    private int fangBiteTicks = 0;
    private int fangBiteCooldown = 0;

    private int blinkOutTicks = 0;
    private int blinkInTicks = 0;
    private int blinkCooldown = 0;
    private @Nullable Vec3 pendingBlinkDest = null;

    private @Nullable LivingEntity snaredTarget = null;
    private int snaredTicksRemaining = 0;
    private int spindleHarvestCooldown = 0;

    public VoidArachneEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 9;
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.LAVA, -1.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 26.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WallClimberNavigation(this, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CLIMBING, false);
        builder.define(BLINK_PHASING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ArachneSilkAndBiteCombatGoal(this));
        this.goalSelector.addGoal(2, new ArachnePortalNestWanderGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.90D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    public boolean isClimbing() {
        return this.entityData.get(CLIMBING);
    }

    public void setClimbing(boolean climbing) {
        this.entityData.set(CLIMBING, climbing);
    }

    public boolean isBlinkPhasing() {
        return this.entityData.get(BLINK_PHASING);
    }

    public void setBlinkPhasing(boolean phasing) {
        this.entityData.set(BLINK_PHASING, phasing);
    }

    @Override
    public boolean onClimbable() {
        return this.isClimbing();
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public void makeStuckInBlock(BlockState state, Vec3 motionMultiplier) {
        if (!state.is(Blocks.COBWEB)) {
            super.makeStuckInBlock(state, motionMultiplier);
        }
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effectInstance) {
        if (effectInstance.is(MobEffects.POISON)) {
            return false;
        }
        return super.canBeAffected(effectInstance);
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        if (fallDistance <= 8.0D || this.isClimbing()) {
            return false;
        }
        return super.causeFallDamage(fallDistance - 6.0D, multiplier * 0.4F, source);
    }

    public static boolean isRiftOrDelveSpawnSurface(BlockGetter level, BlockPos belowPos, BlockState belowState) {
        if (belowState.isAir()) {
            return false;
        }
        String path = BuiltInRegistries.BLOCK.getKey(belowState.getBlock()).getPath();
        return belowState.is(Blocks.OBSIDIAN)
                || belowState.is(Blocks.CRYING_OBSIDIAN)
                || belowState.is(Blocks.BASALT)
                || belowState.is(Blocks.SMOOTH_BASALT)
                || belowState.is(Blocks.POLISHED_BASALT)
                || belowState.is(Blocks.BLACKSTONE)
                || belowState.is(Blocks.NETHERRACK)
                || path.contains("void")
                || path.contains("obsidian")
                || path.contains("basalt")
                || path.contains("rift")
                || belowState.isFaceSturdy(level, belowPos, Direction.UP);
    }

    public static boolean checkVoidArachneSpawnRules(
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
                || spawnReason == EntitySpawnReason.COMMAND) {
            return true;
        }
        BlockPos belowPos = pos.below();
        return isRiftOrDelveSpawnSurface(level, belowPos, level.getBlockState(belowPos));
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        BlockPos belowPos = this.blockPosition().below();
        return isRiftOrDelveSpawnSurface(level, belowPos, level.getBlockState(belowPos));
    }

    private boolean canClimbAdjacentWall() {
        BlockPos base = this.blockPosition();
        for (int dy = 0; dy <= 1; dy++) {
            BlockPos center = base.above(dy);
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos adj = center.relative(dir);
                BlockState st = this.level().getBlockState(adj);
                if (!st.isAir() && (st.blocksMotion() || st.isFaceSturdy(this.level(), adj, dir.getOpposite()))) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean tryStartPortalBlink(ServerLevel serverLevel, @Nullable LivingEntity refTarget) {
        if (this.blinkCooldown > 0
                || this.blinkOutTicks > 0
                || this.blinkInTicks > 0
                || !this.isAlive()) {
            return false;
        }
        if (VoidRiftShadeEntity.isWithinStabilizedRiftField(serverLevel, this.blockPosition(), 16.0D)) {
            return false;
        }

        Vec3 dest = this.findPortalBlinkDestination(serverLevel, refTarget);
        if (dest == null) {
            return false;
        }

        this.blinkCooldown = BLINK_COOLDOWN_TICKS;
        this.pendingBlinkDest = dest;
        this.blinkOutTicks = BLINK_OUT_DURATION_TICKS;
        this.silkNetTicks = 0;
        this.fangBiteTicks = 0;
        this.setBlinkPhasing(true);
        this.getNavigation().stop();

        serverLevel.broadcastEntityEvent(this, EVENT_BLINK_OUT);
        serverLevel.playSound(null, this.getX(), this.getY() + 0.9D, this.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.85F, 1.25F);
        serverLevel.sendParticles(ParticleTypes.PORTAL,
                this.getX(), this.getY() + 0.9D, this.getZ(),
                26, 0.30D, 0.65D, 0.30D, 0.18D);
        serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                this.getX(), this.getY() + 0.9D, this.getZ(),
                16, 0.18D, 0.55D, 0.18D, 0.05D);
        return true;
    }

    private @Nullable Vec3 findPortalBlinkDestination(ServerLevel serverLevel, @Nullable LivingEntity refTarget) {
        Vec3 center = (refTarget != null && refTarget.isAlive()) ? refTarget.position() : this.position();
        for (int attempt = 0; attempt < 16; attempt++) {
            double angle = this.random.nextDouble() * (Math.PI * 2.0D);
            double dist = (refTarget != null && this.snaredTarget == refTarget && this.snaredTicksRemaining > 0)
                    ? (1.8D + this.random.nextDouble() * 1.2D)
                    : (3.8D + this.random.nextDouble() * 4.2D);
            int cx = Mth.floor(center.x + Math.cos(angle) * dist);
            int cz = Mth.floor(center.z + Math.sin(angle) * dist);
            int baseY = Mth.floor(center.y);

            for (int dy = 3; dy >= -4; dy--) {
                BlockPos floorPos = new BlockPos(cx, baseY + dy - 1, cz);
                BlockPos standPos = floorPos.above();
                BlockState floor = serverLevel.getBlockState(floorPos);
                if (floor.isAir() || !floor.blocksMotion() || !floor.getFluidState().isEmpty()) {
                    continue;
                }
                if (!serverLevel.getBlockState(standPos).getFluidState().isEmpty()) {
                    continue;
                }
                Vec3 candidate = new Vec3(cx + 0.5D, standPos.getY(), cz + 0.5D);
                if (serverLevel.noCollision(this, this.getDimensions(this.getPose()).makeBoundingBox(candidate))) {
                    return candidate;
                }
            }
        }
        return null;
    }

    public void startVoidSilkNetCast(ServerLevel serverLevel) {
        this.silkNetTicks = SILK_NET_DURATION_TICKS;
        this.silkNetCooldown = 95;
        this.getNavigation().stop();
        serverLevel.broadcastEntityEvent(this, EVENT_SILK_NET);
        serverLevel.playSound(null, this.getX(), this.getY() + 1.1D, this.getZ(),
                SoundEvents.SPIDER_AMBIENT, SoundSource.HOSTILE, 0.90F, 1.35F);
    }

    private void releaseVoidSilkNet(ServerLevel serverLevel) {
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive() || this.distanceToSqr(target) > 144.0D || !this.hasLineOfSight(target)) {
            return;
        }

        Vec3 from = new Vec3(this.getX(), this.getY() + 1.15D, this.getZ());
        Vec3 to = new Vec3(target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ());
        int steps = 12;
        for (int i = 1; i <= steps; i++) {
            double u = (double) i / (double) steps;
            serverLevel.sendParticles(VOID_SILK_DUST,
                    Mth.lerp(u, from.x, to.x),
                    Mth.lerp(u, from.y, to.y),
                    Mth.lerp(u, from.z, to.z),
                    2, 0.08D, 0.08D, 0.08D, 0.01D);
        }

        // Snare target in rooting Void silk for 3 seconds (60 ticks)!
        this.snaredTarget = target;
        this.snaredTicksRemaining = SNARE_ROOT_DURATION_TICKS;
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, SNARE_ROOT_DURATION_TICKS, 6), this);
        target.setDeltaMovement(0.0D, Math.min(0.0D, target.getDeltaMovement().y), 0.0D);
        target.hurtMarked = true;

        serverLevel.playSound(null, target.getX(), target.getY() + 0.6D, target.getZ(),
                SoundEvents.COBWEB_PLACE, SoundSource.HOSTILE, 1.0F, 0.85F);
        serverLevel.sendParticles(PORTAL_VIOLET_DUST,
                target.getX(), target.getY() + 0.65D, target.getZ(),
                22, 0.45D, 0.45D, 0.45D, 0.02D);
        serverLevel.sendParticles(ParticleTypes.PORTAL,
                target.getX(), target.getY() + 0.65D, target.getZ(),
                16, 0.35D, 0.45D, 0.35D, 0.12D);
    }

    public void startVenomousFangBite(ServerLevel serverLevel, LivingEntity target) {
        this.fangBiteTicks = FANG_BITE_DURATION_TICKS;
        this.fangBiteCooldown = FANG_BITE_DURATION_TICKS + 8;
        serverLevel.broadcastEntityEvent(this, EVENT_FANG_BITE);

        // Predatory lunge forward toward snared prey
        Vec3 dir = target.position().subtract(this.position());
        double horiz = Math.hypot(dir.x, dir.z);
        if (horiz > 1.0E-4D) {
            Vec3 vel = this.getDeltaMovement();
            this.setDeltaMovement(
                    vel.x + (dir.x / horiz) * 0.34D,
                    Math.max(vel.y, 0.16D),
                    vel.z + (dir.z / horiz) * 0.34D
            );
        }
        serverLevel.playSound(null, this.getX(), this.getY() + 0.7D, this.getZ(),
                SoundEvents.SPIDER_HURT, SoundSource.HOSTILE, 0.85F, 0.80F);
    }

    private void executeVenomousFangBiteImpact(ServerLevel serverLevel) {
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        if (this.distanceToSqr(target) <= 12.5D && this.hasLineOfSight(target)) {
            boolean hit = this.doHurtTarget(serverLevel, target);
            if (hit) {
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0), this);
                serverLevel.sendParticles(VOID_SILK_DUST,
                        target.getX(), target.getY() + 0.7D, target.getZ(),
                        12, 0.30D, 0.30D, 0.30D, 0.02D);
            }
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean isSilkBarter = stack.is(Items.STRING)
                || stack.is(Items.ENDER_PEARL)
                || stack.is(Items.CHORUS_FRUIT);
        if (isSilkBarter && this.isAlive() && this.spindleHarvestCooldown <= 0) {
            if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                stack.consume(1, player);
                this.spindleHarvestCooldown = SPINDLE_HARVEST_COOLDOWN_TICKS;
                this.heal(3.0F);
                serverLevel.playSound(null, this.getX(), this.getY() + 1.0D, this.getZ(),
                        SoundEvents.UI_LOOM_TAKE_RESULT, SoundSource.HOSTILE, 0.90F, 1.20F);
                serverLevel.sendParticles(VOID_SILK_DUST,
                        this.getX(), this.getY() + 1.1D, this.getZ(),
                        16, 0.40D, 0.35D, 0.40D, 0.02D);

                Optional<Item> spindleOpt = ModItems.VOID_WEB_SPINDLE.isPresent()
                        ? Optional.of(ModItems.VOID_WEB_SPINDLE.get())
                        : BuiltInRegistries.ITEM.getOptional(VOID_WEB_SPINDLE_ID);
                spindleOpt.ifPresent(spindle ->
                        this.spawnAtLocation(serverLevel, new ItemStack(spindle, 1), 0.75F));
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        // Dodge incoming projectiles or attacks while portal-phasing!
        if (this.isBlinkPhasing() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        if (source.is(DamageTypeTags.IS_PROJECTILE)
                && this.blinkCooldown <= 0
                && this.tryStartPortalBlink(serverLevel, this.getTarget())) {
            return false;
        }

        boolean hurt = super.hurtServer(serverLevel, source, amount);
        if (hurt && this.isAlive()) {
            LivingEntity attacker = (source.getEntity() instanceof LivingEntity liv) ? liv : this.getTarget();
            if (this.blinkCooldown <= 0 && this.random.nextFloat() < 0.55F
                    && this.tryStartPortalBlink(serverLevel, attacker)) {
                return true;
            }
            serverLevel.broadcastEntityEvent(this, EVENT_HURT_SHIELD);
        }
        return hurt;
    }

    @Override
    protected void tickDeath() {
        ++this.deathTime;
        if (this.level().isClientSide() && this.deathTime % 4 == 0) {
            this.level().addParticle(ParticleTypes.PORTAL,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.8D,
                    this.getY() + 0.5D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.8D,
                    0.0D, 0.05D, 0.0D);
        }
        if (this.deathTime >= DEATH_DURATION_TICKS && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_SILK_NET) {
            this.hurtAnimationState.stop();
            this.biteAnimationState.stop();
            this.attackAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_FANG_BITE) {
            this.hurtAnimationState.stop();
            this.attackAnimationState.stop();
            this.biteAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_BLINK_OUT) {
            this.attackAnimationState.stop();
            this.biteAnimationState.stop();
            this.blinkInAnimationState.stop();
            this.blinkOutAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_BLINK_IN) {
            this.blinkOutAnimationState.stop();
            this.blinkInAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_HURT_SHIELD) {
            this.hurtAnimationState.start(this.tickCount);
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            if (this.silkNetCooldown > 0) {
                this.silkNetCooldown--;
            }
            if (this.fangBiteCooldown > 0) {
                this.fangBiteCooldown--;
            }
            if (this.blinkCooldown > 0) {
                this.blinkCooldown--;
            }
            if (this.spindleHarvestCooldown > 0) {
                this.spindleHarvestCooldown--;
            }

            if (this.isAlive()) {
                this.setClimbing(this.horizontalCollision && this.canClimbAdjacentWall());

                // Proactively blink-dodge incoming arrows/bolts in flight!
                if (this.blinkCooldown <= 0 && this.blinkOutTicks <= 0 && this.tickCount % 3 == 0) {
                    List<Projectile> incoming = serverLevel.getEntitiesOfClass(
                            Projectile.class,
                            this.getBoundingBox().inflate(3.2D),
                            p -> p.isAlive() && p.getOwner() != this && p.getDeltaMovement().lengthSqr() > 0.09D
                    );
                    if (!incoming.isEmpty()) {
                        this.tryStartPortalBlink(serverLevel, this.getTarget());
                    }
                }

                // Active 3-second Void Silk Snare maintenance on rooted prey
                if (this.snaredTicksRemaining > 0) {
                    this.snaredTicksRemaining--;
                    if (this.snaredTarget != null && this.snaredTarget.isAlive()) {
                        Vec3 v = this.snaredTarget.getDeltaMovement();
                        this.snaredTarget.setDeltaMovement(0.0D, Math.min(0.0D, v.y), 0.0D);
                        this.snaredTarget.hurtMarked = true;
                        if (this.snaredTicksRemaining % 5 == 0) {
                            serverLevel.sendParticles(VOID_SILK_DUST,
                                    this.snaredTarget.getX(),
                                    this.snaredTarget.getY() + 0.55D,
                                    this.snaredTarget.getZ(),
                                    4, 0.35D, 0.35D, 0.35D, 0.01D);
                        }
                    } else {
                        this.snaredTicksRemaining = 0;
                        this.snaredTarget = null;
                    }
                    if (this.snaredTicksRemaining == 0) {
                        this.snaredTarget = null;
                    }
                }

                // Blink-out -> portal warp -> Blink-in sequence
                if (this.blinkOutTicks > 0) {
                    this.blinkOutTicks--;
                    this.setDeltaMovement(Vec3.ZERO);
                    if (this.blinkOutTicks == 0) {
                        if (this.pendingBlinkDest != null) {
                            this.teleportTo(this.pendingBlinkDest.x, this.pendingBlinkDest.y, this.pendingBlinkDest.z);
                            this.pendingBlinkDest = null;
                        }
                        this.blinkInTicks = BLINK_IN_DURATION_TICKS;
                        serverLevel.broadcastEntityEvent(this, EVENT_BLINK_IN);
                        serverLevel.playSound(null, this.getX(), this.getY() + 0.9D, this.getZ(),
                                SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.85F, 1.40F);
                        serverLevel.sendParticles(ParticleTypes.PORTAL,
                                this.getX(), this.getY() + 0.9D, this.getZ(),
                                28, 0.32D, 0.65D, 0.32D, 0.20D);
                        serverLevel.sendParticles(PORTAL_VIOLET_DUST,
                                this.getX(), this.getY() + 0.9D, this.getZ(),
                                14, 0.25D, 0.55D, 0.25D, 0.02D);
                    }
                } else if (this.blinkInTicks > 0) {
                    this.blinkInTicks--;
                    if (this.blinkInTicks == 0) {
                        this.setBlinkPhasing(false);
                    }
                }

                // Void Silk Net cast sequence
                if (this.silkNetTicks > 0) {
                    int elapsed = SILK_NET_DURATION_TICKS - this.silkNetTicks;
                    this.silkNetTicks--;
                    if (elapsed == SILK_NET_RELEASE_TICK) {
                        this.releaseVoidSilkNet(serverLevel);
                    }
                }

                // Venomous Obsidian Fang Bite sequence
                if (this.fangBiteTicks > 0) {
                    int elapsed = FANG_BITE_DURATION_TICKS - this.fangBiteTicks;
                    this.fangBiteTicks--;
                    if (elapsed == FANG_BITE_IMPACT_TICK) {
                        this.executeVenomousFangBiteImpact(serverLevel);
                    }
                }
            } else {
                this.setClimbing(false);
                this.setBlinkPhasing(false);
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
            this.biteAnimationState.stop();
            this.blinkOutAnimationState.stop();
            this.blinkInAnimationState.stop();
            this.hurtAnimationState.stop();
            this.deathAnimationState.startIfStopped(this.tickCount);
            return;
        }

        if (this.attackAnimationState.isStarted()
                && this.attackAnimationState.getTimeInMillis(this.tickCount) >= SILK_NET_DURATION_TICKS * 50L) {
            this.attackAnimationState.stop();
        }
        if (this.biteAnimationState.isStarted()
                && this.biteAnimationState.getTimeInMillis(this.tickCount) >= FANG_BITE_DURATION_TICKS * 50L) {
            this.biteAnimationState.stop();
        }
        if (this.blinkOutAnimationState.isStarted()
                && this.blinkOutAnimationState.getTimeInMillis(this.tickCount) >= BLINK_OUT_DURATION_TICKS * 50L) {
            this.blinkOutAnimationState.stop();
        }
        if (this.blinkInAnimationState.isStarted()
                && this.blinkInAnimationState.getTimeInMillis(this.tickCount) >= BLINK_IN_DURATION_TICKS * 50L) {
            this.blinkInAnimationState.stop();
        }
        if (this.hurtAnimationState.isStarted()
                && this.hurtAnimationState.getTimeInMillis(this.tickCount) >= HURT_DURATION_TICKS * 50L) {
            this.hurtAnimationState.stop();
        }

        boolean skittering = this.isClimbing() || this.getDeltaMovement().horizontalDistanceSqr() > 2.5E-4D;
        if (skittering) {
            this.idleAnimationState.stop();
            this.walkAnimationState.startIfStopped(this.tickCount);
        } else {
            this.walkAnimationState.stop();
            this.idleAnimationState.startIfStopped(this.tickCount);
        }

        // Ambient purple Void-silk motes between her weaving hands & portal-ringed legs
        if (this.tickCount % (this.isBlinkPhasing() ? 2 : 9) == 0) {
            this.level().addParticle(
                    this.random.nextBoolean() ? ParticleTypes.PORTAL : VOID_SILK_DUST,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.75D,
                    this.getY() + 0.75D + this.random.nextDouble() * 0.65D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.75D,
                    0.0D, 0.02D, 0.0D
            );
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("SpindleHarvestCooldown", Codec.INT, this.spindleHarvestCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.spindleHarvestCooldown = input.read("SpindleHarvestCooldown", Codec.INT).orElse(0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SPIDER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SPIDER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SPIDER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SPIDER_STEP, 0.35F, 1.10F);
    }

    private static final class ArachneSilkAndBiteCombatGoal extends Goal {
        private final VoidArachneEntity arachne;
        private int repathTicks = 0;

        ArachneSilkAndBiteCombatGoal(VoidArachneEntity arachne) {
            this.arachne = arachne;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.arachne.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.arachne.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }
            if (target instanceof Player p && (p.isCreative() || p.isSpectator())) {
                return false;
            }
            return this.arachne.distanceToSqr(target) <= 784.0D;
        }

        @Override
        public void stop() {
            this.arachne.getNavigation().stop();
        }

        @Override
        public void tick() {
            LivingEntity target = this.arachne.getTarget();
            if (target == null || !(this.arachne.level() instanceof ServerLevel serverLevel)) {
                return;
            }
            if (this.arachne.blinkOutTicks > 0 || this.arachne.blinkInTicks > 0) {
                return;
            }

            this.arachne.getLookControl().setLookAt(target, 32.0F, 32.0F);
            double distSqr = this.arachne.distanceToSqr(target);

            // If prey is snared and far, blink right onto them for the venomous fang lunge!
            if (this.arachne.snaredTarget == target
                    && this.arachne.snaredTicksRemaining > 15
                    && distSqr > 16.0D
                    && this.arachne.blinkCooldown <= 0) {
                if (this.arachne.tryStartPortalBlink(serverLevel, target)) {
                    return;
                }
            }

            if (this.arachne.silkNetTicks <= 0 && --this.repathTicks <= 0) {
                this.repathTicks = 4 + this.arachne.getRandom().nextInt(5);
                double rushMult = (this.arachne.snaredTarget == target && this.arachne.snaredTicksRemaining > 0)
                        ? 1.32D
                        : 1.12D;
                this.arachne.getNavigation().moveTo(target, rushMult);
            }

            if (this.arachne.silkNetTicks > 0 || this.arachne.fangBiteTicks > 0) {
                return;
            }

            // 1. Cast rooting Void silk net at medium range (or before closing in)
            if (this.arachne.silkNetCooldown <= 0
                    && distSqr >= 6.25D
                    && distSqr <= 110.0D
                    && (this.arachne.snaredTarget != target || this.arachne.snaredTicksRemaining <= 0)
                    && this.arachne.hasLineOfSight(target)) {
                this.arachne.startVoidSilkNetCast(serverLevel);
                return;
            }

            // 2. Lunge in to clamp with venomous obsidian fangs for 6.0 damage!
            if (this.arachne.fangBiteCooldown <= 0
                    && distSqr <= 9.6D
                    && this.arachne.hasLineOfSight(target)) {
                this.arachne.startVenomousFangBite(serverLevel, target);
            }
        }
    }

    private static final class ArachnePortalNestWanderGoal extends Goal {
        private final VoidArachneEntity arachne;
        private @Nullable BlockPos nestTarget = null;

        ArachnePortalNestWanderGoal(VoidArachneEntity arachne) {
            this.arachne = arachne;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.arachne.getTarget() != null
                    || this.arachne.getNavigation().isInProgress()
                    || this.arachne.getRandom().nextInt(60) != 0) {
                return false;
            }
            BlockPos origin = this.arachne.blockPosition();
            for (BlockPos check : BlockPos.betweenClosed(origin.offset(-10, -4, -10), origin.offset(10, 4, 10))) {
                BlockState st = this.arachne.level().getBlockState(check);
                if (st.is(Blocks.NETHER_PORTAL)
                        || st.is(Blocks.CRYING_OBSIDIAN)
                        || st.is(Blocks.OBSIDIAN)) {
                    this.nestTarget = check.immutable();
                    return origin.distSqr(this.nestTarget) > 9.0D;
                }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.arachne.getTarget() == null && this.arachne.getNavigation().isInProgress();
        }

        @Override
        public void start() {
            if (this.nestTarget != null) {
                this.arachne.getNavigation().moveTo(
                        this.nestTarget.getX() + 0.5D,
                        this.nestTarget.getY() + 1.0D,
                        this.nestTarget.getZ() + 0.5D,
                        0.85D
                );
            }
        }
    }
}
