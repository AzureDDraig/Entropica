package ddraig.net.entropica.entity.soulfire_djinn;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
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
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

/**
 * Fauna 57 — Soulfire Djinn (Desert Efreet) (entropica:soulfire_djinn).
 * Hostile legless cobalt-blue soulfire efreet bound in runic brass & bronze restraints,
 * native to 6.3 Soulfire Fjords & 5.1 Silica Dunes:
 * - Hovers near the ground (walk / idle) or launches into a high-speed aerial desert glide
 *   (glide_takeoff -> glide -> glide_landing), leaving blue soulfire flame particles in its wake.
 * - Cupped-Palm Soulfire Fireball (attack): cups both hands together to conjure a blazing soulfire
 *   orb before thrusting both palms forward to hurl it, dealing 6.0 damage and applying Soul Wither.
 * - Non-Solid Block Phasing (phase_glide): compresses its elemental smoke-and-flame form laterally
 *   to phase right through non-solid blocks (fences, iron bars, panes, chains, grates, walls),
 *   while solid blocks strictly bar its passage!
 * - Dynamic Hand-Flame Particle Emitters: flickering blue soulfire flames cupped in hand_l_grp and
 *   hand_r_grp plus trailing wake embers are emitted dynamically in-game.
 * - Drops Brass Djinn Collar (entropica:brass_djinn_collar).
 */
public class SoulfireDjinnEntity extends Monster implements FlyingAnimal {

    public static final int MODE_HOVER_GROUND = 0;
    public static final int MODE_GLIDE_TAKEOFF = 1;
    public static final int MODE_GLIDING = 2;
    public static final int MODE_GLIDE_LANDING = 3;

    private static final EntityDataAccessor<Integer> GLIDE_MODE =
            SynchedEntityData.defineId(SoulfireDjinnEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> CONJURING_ORB =
            SynchedEntityData.defineId(SoulfireDjinnEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> PHASING_BARS =
            SynchedEntityData.defineId(SoulfireDjinnEntity.class, EntityDataSerializers.BOOLEAN);

    private static final byte EVENT_SOULFIRE_HURL = 4;
    private static final byte EVENT_GLIDE_TAKEOFF = 61;
    private static final byte EVENT_GLIDE_LANDING = 62;
    private static final byte EVENT_PHASE_GLIDE = 63;
    private static final byte EVENT_HURT_RECOIL = 64;

    private static final int GLIDE_TAKEOFF_DURATION_TICKS = 24; // 1.20s glide_takeoff
    private static final int GLIDE_LANDING_DURATION_TICKS = 24; // 1.20s glide_landing
    private static final int SOULFIRE_HURL_DURATION_TICKS = 30; // 1.50s attack
    private static final int PHASE_GLIDE_DURATION_TICKS = 28;   // 1.40s phase_glide
    private static final int DEATH_DURATION_TICKS = 36;         // 1.80s death hold
    private static final int COLLAR_SHED_COOLDOWN_TICKS = 1200;

    private static final ResourceLocation BRASS_DJINN_COLLAR_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "brass_djinn_collar");

    private static final DustParticleOptions CYAN_PALM_DUST = new DustParticleOptions(0x0CDAFC, 0.72F);
    private static final DustParticleOptions ICE_CYAN_DUST = new DustParticleOptions(0x68F8FF, 0.58F);
    private static final DustParticleOptions COBALT_WAKE_DUST = new DustParticleOptions(0x1876D6, 0.80F);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState glideTakeoffAnimationState = new AnimationState();
    public final AnimationState glideAnimationState = new AnimationState();
    public final AnimationState glideLandingAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState phaseGlideAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    private final PathNavigation groundNav;
    private final PathNavigation flyingNav;
    private final MoveControl groundMoveControl;
    private final MoveControl flyingMoveControl;

    private int modeTransitionTicks = 0;
    private int phaseTimer = 140;
    private int attackTicks = 0;
    private int attackCooldown = 36;
    private int phaseGlideTicks = 0;
    private int phaseGlideCooldown = 0;
    private Vec3 phaseGlideDir = Vec3.ZERO;
    private int collarShedCooldown = 0;

    private int clientTakeoffTicks = 0;
    private int clientLandingTicks = 0;
    private int clientAttackTicks = 0;
    private int clientPhaseTicks = 0;

    private final List<SoulfirePalmOrb> activeSoulfireOrbs = new ArrayList<>();

    private static final class SoulfirePalmOrb {
        Vec3 pos;
        final Vec3 vel;
        int age;

        SoulfirePalmOrb(Vec3 pos, Vec3 vel) {
            this.pos = pos;
            this.vel = vel;
            this.age = 0;
        }
    }

    public SoulfireDjinnEntity(EntityType<? extends SoulfireDjinnEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 14;
        this.groundMoveControl = new MoveControl(this);
        this.flyingMoveControl = new FlyingMoveControl(this, 22, true);
        this.groundNav = new GroundPathNavigation(this, level);
        FlyingPathNavigation fNav = new FlyingPathNavigation(this, level);
        fNav.setCanOpenDoors(false);
        fNav.setCanFloat(true);
        this.flyingNav = fNav;

        this.moveControl = this.groundMoveControl;
        this.navigation = this.groundNav;
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
        this.setPathfindingMalus(PathType.LAVA, 0.0F);
        this.setPathfindingMalus(PathType.FENCE, 0.0F);
        this.setPathfindingMalus(PathType.WATER, 8.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FLYING_SPEED, 0.64D)
                .add(Attributes.FOLLOW_RANGE, 30.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(GLIDE_MODE, MODE_HOVER_GROUND);
        builder.define(CONJURING_ORB, false);
        builder.define(PHASING_BARS, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new DjinnSoulfireCombatGoal(this));
        this.goalSelector.addGoal(2, new DjinnDesertGlideGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.92D) {
            @Override
            public boolean canUse() {
                return SoulfireDjinnEntity.this.getGlideMode() == MODE_HOVER_GROUND
                        && SoulfireDjinnEntity.this.phaseGlideTicks <= 0
                        && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return SoulfireDjinnEntity.this.getGlideMode() == MODE_HOVER_GROUND
                        && SoulfireDjinnEntity.this.phaseGlideTicks <= 0
                        && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public int getGlideMode() {
        return this.entityData.get(GLIDE_MODE);
    }

    private void setGlideMode(int mode) {
        this.entityData.set(GLIDE_MODE, mode);
        boolean airborne = (mode != MODE_HOVER_GROUND);
        this.setNoGravity(airborne);
        if (airborne) {
            if (this.navigation != this.flyingNav) {
                this.groundNav.stop();
                this.navigation = this.flyingNav;
                this.moveControl = this.flyingMoveControl;
            }
        } else {
            if (this.navigation != this.groundNav) {
                this.flyingNav.stop();
                this.navigation = this.groundNav;
                this.moveControl = this.groundMoveControl;
            }
        }
    }

    public boolean isConjuringOrb() {
        return this.entityData.get(CONJURING_ORB);
    }

    private void setConjuringOrb(boolean conjuring) {
        this.entityData.set(CONJURING_ORB, conjuring);
    }

    public boolean isPhasingBars() {
        return this.entityData.get(PHASING_BARS);
    }

    private void setPhasingBars(boolean phasing) {
        this.entityData.set(PHASING_BARS, phasing);
    }

    @Override
    public boolean isFlying() {
        return this.getGlideMode() != MODE_HOVER_GROUND;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effectInstance) {
        if (effectInstance.is(MobEffects.WITHER)) {
            return false;
        }
        return super.canBeAffected(effectInstance);
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        // Legless hovering elemental never takes fall damage
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        if (source.is(DamageTypeTags.IS_FIRE)
                || source.is(DamageTypeTags.IS_FALL)
                || source.is(DamageTypes.LAVA)
                || source.is(DamageTypes.HOT_FLOOR)
                || source.is(DamageTypes.WITHER)
                || source.is(DamageTypes.IN_WALL)) {
            return true;
        }
        return super.isInvulnerableTo(level, source);
    }

    /**
     * Returns true if {@code state} is a non-solid permeable barrier (fences, iron bars, glass panes,
     * chains, walls, grates, trapdoors) that the Djinn can compress its smoke-and-flame form to phase through.
     * Full solid blocks return false and strictly bar its passage!
     */
    public static boolean isPermeableNonSolidBlock(BlockGetter level, BlockPos pos, BlockState state) {
        if (state.isAir()) {
            return false;
        }
        VoxelShape shape = state.getCollisionShape(level, pos);
        if (shape.isEmpty()) {
            return false;
        }
        if (state.is(Blocks.BEDROCK) || state.is(Blocks.BARRIER)) {
            return false;
        }
        if (state.isCollisionShapeFullBlock(level, pos) || Block.isShapeFullBlock(shape)) {
            return false;
        }
        Block block = state.getBlock();
        String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
        if (state.is(BlockTags.FENCES)
                || state.is(BlockTags.FENCE_GATES)
                || state.is(BlockTags.WALLS)
                || state.is(Blocks.IRON_BARS)
                || block instanceof FenceBlock
                || block instanceof FenceGateBlock
                || block instanceof IronBarsBlock
                || block instanceof WallBlock
                || block instanceof TrapDoorBlock
                || path.contains("bars")
                || path.contains("fence")
                || path.contains("pane")
                || path.contains("chain")
                || path.contains("grate")) {
            return true;
        }
        return !state.is(BlockTags.SLABS) && !state.is(BlockTags.STAIRS);
    }

    /**
     * Returns true if {@code box} intersects ANY solid, non-permeable block (which strictly bars phasing!).
     */
    private static boolean hasSolidBlockingPassage(Level level, AABB box) {
        int minX = Mth.floor(box.minX);
        int maxX = Mth.floor(box.maxX);
        int minY = Mth.floor(box.minY);
        int maxY = Mth.floor(box.maxY);
        int minZ = Mth.floor(box.minZ);
        int maxZ = Mth.floor(box.maxZ);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    cursor.set(x, y, z);
                    BlockState st = level.getBlockState(cursor);
                    if (st.isAir()) {
                        continue;
                    }
                    VoxelShape shape = st.getCollisionShape(level, cursor);
                    if (shape.isEmpty()) {
                        continue;
                    }
                    if (!isPermeableNonSolidBlock(level, cursor, st)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            this.updateClientAnimations();
            this.spawnClientHandAndWakeParticles();
            return;
        }

        if (!this.isAlive()) {
            this.activeSoulfireOrbs.clear();
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();

        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }
        if (this.phaseGlideCooldown > 0) {
            this.phaseGlideCooldown--;
        }
        if (this.collarShedCooldown > 0) {
            this.collarShedCooldown--;
        }

        this.tickGlideLifecycle(serverLevel);
        this.tickPhaseGlide(serverLevel);
        this.tryDetectAndStartPhaseGlide(serverLevel);
        this.tickCuppedPalmAttack(serverLevel);
        this.tickSoulfireOrbs(serverLevel);
    }

    private void tickGlideLifecycle(ServerLevel serverLevel) {
        int mode = this.getGlideMode();
        if (mode == MODE_GLIDE_TAKEOFF) {
            if (this.modeTransitionTicks > 0) {
                this.modeTransitionTicks--;
                if (this.modeTransitionTicks == 16) {
                    Vec3 vel = this.getDeltaMovement();
                    this.setDeltaMovement(vel.x, Math.max(vel.y, 0.42D), vel.z);
                    serverLevel.playSound(null, this.getX(), this.getY() + 1.0D, this.getZ(),
                            SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 0.80F, 1.28F);
                    serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                            this.getX(), this.getY() + 0.5D, this.getZ(),
                            20, 0.45D, 0.25D, 0.45D, 0.05D);
                }
            }
            if (this.modeTransitionTicks <= 0) {
                this.setGlideMode(MODE_GLIDING);
            }
            return;
        }

        if (mode == MODE_GLIDE_LANDING) {
            Vec3 vel = this.getDeltaMovement();
            this.setDeltaMovement(vel.x * 0.85D, -0.15D, vel.z * 0.85D);
            if (this.modeTransitionTicks > 0) {
                this.modeTransitionTicks--;
            }
            if (this.onGround() || this.modeTransitionTicks <= 0) {
                this.setGlideMode(MODE_HOVER_GROUND);
                this.modeTransitionTicks = 0;
            }
            return;
        }

        if (this.phaseTimer > 0) {
            this.phaseTimer--;
        }

        LivingEntity target = this.getTarget();
        if (mode == MODE_HOVER_GROUND) {
            boolean launch = false;
            if (target != null && target.isAlive()) {
                if ((target.getY() - this.getY() > 2.4D || this.distanceToSqr(target) > 56.0D)
                        && this.hasGlideHeadroom(serverLevel)) {
                    launch = true;
                }
            } else if (this.phaseTimer <= 0 && this.random.nextInt(50) == 0 && this.hasGlideHeadroom(serverLevel)) {
                launch = true;
            }
            if (launch && this.attackTicks <= 0 && this.phaseGlideTicks <= 0) {
                this.beginGlideTakeoff(serverLevel);
            }
        } else if (mode == MODE_GLIDING) {
            // Trailing blue soulfire wake while gliding at high speed!
            if (this.tickCount % 2 == 0) {
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX(), this.getY() + 0.55D, this.getZ(),
                        2, 0.18D, 0.15D, 0.18D, 0.015D);
            }
            if (target == null && this.phaseTimer <= 0 && this.attackTicks <= 0 && this.phaseGlideTicks <= 0) {
                this.beginGlideLanding(serverLevel);
            }
        }
    }

    private boolean hasGlideHeadroom(ServerLevel level) {
        BlockPos pos = this.blockPosition();
        for (int dy = 2; dy <= 4; dy++) {
            if (!level.getBlockState(pos.above(dy)).getCollisionShape(level, pos.above(dy)).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void beginGlideTakeoff(ServerLevel serverLevel) {
        if (this.getGlideMode() != MODE_HOVER_GROUND || !this.isAlive()) {
            return;
        }
        this.setGlideMode(MODE_GLIDE_TAKEOFF);
        this.modeTransitionTicks = GLIDE_TAKEOFF_DURATION_TICKS;
        this.phaseTimer = 200 + this.random.nextInt(150);
        serverLevel.broadcastEntityEvent(this, EVENT_GLIDE_TAKEOFF);
    }

    public void beginGlideLanding(ServerLevel serverLevel) {
        if (this.getGlideMode() != MODE_GLIDING || !this.isAlive()) {
            return;
        }
        this.setGlideMode(MODE_GLIDE_LANDING);
        this.modeTransitionTicks = GLIDE_LANDING_DURATION_TICKS;
        this.phaseTimer = 140 + this.random.nextInt(120);
        serverLevel.broadcastEntityEvent(this, EVENT_GLIDE_LANDING);
    }

    /**
     * Checks whether a non-solid barrier (fence, iron bars, pane, wall, grate) is directly ahead between
     * the Djinn and its intended destination, with non-solid/open space on the far side!
     */
    private void tryDetectAndStartPhaseGlide(ServerLevel serverLevel) {
        if (this.phaseGlideTicks > 0 || this.phaseGlideCooldown > 0 || this.attackTicks > 0) {
            return;
        }
        Vec3 moveIntent = Vec3.ZERO;
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            Vec3 toTarget = new Vec3(target.getX() - this.getX(), 0.0D, target.getZ() - this.getZ());
            if (toTarget.lengthSqr() > 0.25D) {
                moveIntent = toTarget.normalize();
            }
        } else if (this.horizontalCollision) {
            Vec3 look = this.getLookAngle();
            Vec3 horiz = new Vec3(look.x, 0.0D, look.z);
            if (horiz.lengthSqr() > 1.0E-4D) {
                moveIntent = horiz.normalize();
            }
        }
        if (moveIntent.lengthSqr() < 1.0E-4D) {
            return;
        }

        BlockPos frontFeet = BlockPos.containing(
                this.getX() + moveIntent.x * 0.85D,
                this.getY() + 0.2D,
                this.getZ() + moveIntent.z * 0.85D
        );
        BlockPos frontChest = frontFeet.above();
        BlockState stFeet = serverLevel.getBlockState(frontFeet);
        BlockState stChest = serverLevel.getBlockState(frontChest);

        boolean feetPermeable = isPermeableNonSolidBlock(serverLevel, frontFeet, stFeet);
        boolean chestPermeable = isPermeableNonSolidBlock(serverLevel, frontChest, stChest);
        if (!feetPermeable && !chestPermeable) {
            return;
        }

        // Ensure neither block is a solid obstacle and the far side (1.7 blocks ahead) is clear of solid blocks!
        AABB CheckBarrierBox = this.getBoundingBox().move(moveIntent.scale(0.85D)).deflate(0.08D, 0.05D, 0.08D);
        AABB beyondBox = this.getBoundingBox().move(moveIntent.scale(1.75D)).deflate(0.08D, 0.05D, 0.08D);
        if (!hasSolidBlockingPassage(serverLevel, CheckBarrierBox) && !hasSolidBlockingPassage(serverLevel, beyondBox)) {
            this.startPhaseGlide(serverLevel, moveIntent);
        }
    }

    public void startPhaseGlide(ServerLevel serverLevel, Vec3 direction) {
        this.phaseGlideTicks = PHASE_GLIDE_DURATION_TICKS;
        this.phaseGlideCooldown = 55;
        this.phaseGlideDir = direction.normalize();
        this.setPhasingBars(true);
        serverLevel.broadcastEntityEvent(this, EVENT_PHASE_GLIDE);
        serverLevel.playSound(null, this.getX(), this.getY() + 1.0D, this.getZ(),
                SoundEvents.SOUL_ESCAPE.value(), SoundSource.HOSTILE, 1.0F, 1.15F);
        serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                this.getX(), this.getY() + 1.05D, this.getZ(),
                18, 0.25D, 0.55D, 0.25D, 0.04D);
    }

    private void tickPhaseGlide(ServerLevel serverLevel) {
        if (this.phaseGlideTicks <= 0) {
            return;
        }
        this.phaseGlideTicks--;

        // Compressed lateral streak window: t = 0.25s -> 1.05s (remaining 23 -> 7)
        if (this.phaseGlideTicks <= 23 && this.phaseGlideTicks >= 7 && this.phaseGlideDir.lengthSqr() > 1.0E-4D) {
            Vec3 step = this.phaseGlideDir.scale(0.18D);
            AABB nextBox = this.getBoundingBox().move(step).deflate(0.06D, 0.04D, 0.06D);
            // Strictly verify ZERO solid blocks lie in nextBox before stepping through non-solid bars/fences!
            if (!hasSolidBlockingPassage(serverLevel, nextBox)) {
                this.setPos(this.getX() + step.x, this.getY(), this.getZ() + step.z);
                serverLevel.sendParticles(CYAN_PALM_DUST,
                        this.getX(), this.getY() + 1.1D, this.getZ(),
                        4, 0.14D, 0.45D, 0.14D, 0.02D);
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX(), this.getY() + 1.0D, this.getZ(),
                        2, 0.12D, 0.40D, 0.12D, 0.01D);
            }
        }

        if (this.phaseGlideTicks == 0) {
            this.setPhasingBars(false);
        }
    }

    /**
     * Cupped-Palm Soulfire Fireball (1.50s = 30 ticks; hurl at t = 0.78s -> remaining == 15):
     * Conjures a soulfire orb in both cupped palms, then thrusts both palms forward to hurl it
     * for 6.0 damage + Soul Wither!
     */
    public void startCuppedPalmAttack(ServerLevel serverLevel) {
        if (this.attackTicks > 0 || this.phaseGlideTicks > 0 || this.modeTransitionTicks > 0) {
            return;
        }
        this.attackTicks = SOULFIRE_HURL_DURATION_TICKS;
        this.attackCooldown = 48;
        this.setConjuringOrb(true);
        if (this.getGlideMode() == MODE_HOVER_GROUND) {
            this.getNavigation().stop();
        }
        serverLevel.broadcastEntityEvent(this, EVENT_SOULFIRE_HURL);
        serverLevel.playSound(null, this.getX(), this.getY() + 1.25D, this.getZ(),
                SoundEvents.BLAZE_AMBIENT, SoundSource.HOSTILE, 0.85F, 1.28F);
    }

    private void tickCuppedPalmAttack(ServerLevel serverLevel) {
        if (this.attackTicks <= 0) {
            return;
        }
        this.attackTicks--;

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            this.getLookControl().setLookAt(target, 32.0F, 32.0F);
        }

        Vec3 look = this.getLookAngle();
        Vec3 cuppedCenter = new Vec3(
                this.getX() + look.x * 0.68D,
                this.getY() + 1.22D + (this.getGlideMode() == MODE_GLIDING ? 0.35D : 0.0D),
                this.getZ() + look.z * 0.68D
        );

        // Conjuring soulfire orb between cupped palms (0.15s -> 0.75s)
        if (this.attackTicks > 15 && this.attackTicks <= 27 && this.attackTicks % 2 == 0) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    cuppedCenter.x, cuppedCenter.y, cuppedCenter.z,
                    4, 0.12D, 0.12D, 0.12D, 0.015D);
            serverLevel.sendParticles(ICE_CYAN_DUST,
                    cuppedCenter.x, cuppedCenter.y, cuppedCenter.z,
                    3, 0.10D, 0.10D, 0.10D, 0.01D);
        }

        // Hurl blazing soulfire fireball at t = 0.78s (remaining == 15)
        if (this.attackTicks == 15) {
            this.setConjuringOrb(false);
            Vec3 aimDir = look;
            if (target != null && target.isAlive()) {
                Vec3 toTarget = target.getBoundingBox().getCenter().subtract(cuppedCenter);
                if (toTarget.lengthSqr() > 1.0E-4D) {
                    aimDir = toTarget.normalize();
                }
            }
            this.activeSoulfireOrbs.add(new SoulfirePalmOrb(cuppedCenter, aimDir.scale(0.88D)));
            serverLevel.playSound(null, cuppedCenter.x, cuppedCenter.y, cuppedCenter.z,
                    SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.0F, 1.18F);
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    cuppedCenter.x, cuppedCenter.y, cuppedCenter.z,
                    16, 0.22D, 0.22D, 0.22D, 0.05D);
        }

        if (this.attackTicks == 0) {
            this.setConjuringOrb(false);
        }
    }

    private void tickSoulfireOrbs(ServerLevel serverLevel) {
        if (this.activeSoulfireOrbs.isEmpty()) {
            return;
        }
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE); // 6.0F
        Iterator<SoulfirePalmOrb> it = this.activeSoulfireOrbs.iterator();
        while (it.hasNext()) {
            SoulfirePalmOrb orb = it.next();
            orb.age++;
            Vec3 nextPos = orb.pos.add(orb.vel);

            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    orb.pos.x, orb.pos.y, orb.pos.z, 4, 0.10D, 0.10D, 0.10D, 0.01D);
            serverLevel.sendParticles(CYAN_PALM_DUST,
                    orb.pos.x, orb.pos.y, orb.pos.z, 3, 0.12D, 0.12D, 0.12D, 0.01D);
            serverLevel.sendParticles(COBALT_WAKE_DUST,
                    orb.pos.x, orb.pos.y, orb.pos.z, 2, 0.10D, 0.10D, 0.10D, 0.01D);

            boolean impacted = false;
            AABB hitBox = new AABB(orb.pos, nextPos).inflate(0.55D);
            for (LivingEntity victim : serverLevel.getEntitiesOfClass(
                    LivingEntity.class,
                    hitBox,
                    e -> e.isAlive() && e != this && !(e instanceof SoulfireDjinnEntity))) {
                if (victim instanceof Player p && (p.isCreative() || p.isSpectator())) {
                    continue;
                }
                if (victim.hurtServer(serverLevel, this.damageSources().mobAttack(this), damage)) {
                    // Soul Wither: Wither + Soulfire ignition
                    victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0), this);
                    victim.igniteForSeconds(4.0F);
                }
                impacted = true;
                break;
            }

            if (!impacted) {
                HitResult blockHit = serverLevel.clip(new ClipContext(
                        orb.pos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                if (blockHit.getType() != HitResult.Type.MISS) {
                    BlockPos hitBlockPos = BlockPos.containing(blockHit.getLocation());
                    BlockState hitState = serverLevel.getBlockState(hitBlockPos);
                    // Soulfire fireballs also pass through open bars/fences unless hitting solid masonry!
                    if (!isPermeableNonSolidBlock(serverLevel, hitBlockPos, hitState)) {
                        nextPos = blockHit.getLocation();
                        impacted = true;
                    }
                }
            }

            orb.pos = nextPos;
            if (impacted || orb.age >= 45) {
                if (impacted) {
                    serverLevel.playSound(null, orb.pos.x, orb.pos.y, orb.pos.z,
                            SoundEvents.SOUL_ESCAPE.value(), SoundSource.HOSTILE, 0.90F, 0.95F);
                    serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                            orb.pos.x, orb.pos.y, orb.pos.z, 18, 0.35D, 0.35D, 0.35D, 0.05D);
                    serverLevel.sendParticles(ParticleTypes.SOUL,
                            orb.pos.x, orb.pos.y, orb.pos.z, 6, 0.25D, 0.25D, 0.25D, 0.03D);
                }
                it.remove();
            }
        }
    }

    private void updateClientAnimations() {
        if (this.isDeadOrDying()) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.glideTakeoffAnimationState.stop();
            this.glideAnimationState.stop();
            this.glideLandingAnimationState.stop();
            this.attackAnimationState.stop();
            this.phaseGlideAnimationState.stop();
            this.hurtAnimationState.stop();
            if (!this.deathAnimationState.isStarted()) {
                this.deathAnimationState.start(this.tickCount);
            }
            return;
        }

        if (this.clientTakeoffTicks > 0) {
            this.clientTakeoffTicks--;
            if (this.clientTakeoffTicks == 0) {
                this.glideTakeoffAnimationState.stop();
                this.glideAnimationState.start(this.tickCount);
            }
        }
        if (this.clientLandingTicks > 0) {
            this.clientLandingTicks--;
            if (this.clientLandingTicks == 0) {
                this.glideLandingAnimationState.stop();
            }
        }
        if (this.clientAttackTicks > 0) {
            this.clientAttackTicks--;
            if (this.clientAttackTicks == 0) {
                this.attackAnimationState.stop();
            }
        }
        if (this.clientPhaseTicks > 0) {
            this.clientPhaseTicks--;
            if (this.clientPhaseTicks == 0) {
                this.phaseGlideAnimationState.stop();
            }
        }

        int mode = this.getGlideMode();
        if (mode == MODE_GLIDE_TAKEOFF || this.clientTakeoffTicks > 0) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.glideAnimationState.stop();
            return;
        }
        if (mode == MODE_GLIDE_LANDING || this.clientLandingTicks > 0) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.glideAnimationState.stop();
            return;
        }
        if (mode == MODE_GLIDING) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.glideAnimationState.startIfStopped(this.tickCount);
            return;
        }

        this.glideAnimationState.stop();
        double hSpeedSqr = this.getDeltaMovement().horizontalDistanceSqr();
        if (hSpeedSqr > 3.5E-4D || this.walkAnimation.speed() > 0.06F) {
            this.walkAnimationState.startIfStopped(this.tickCount);
            this.idleAnimationState.stop();
        } else {
            this.idleAnimationState.startIfStopped(this.tickCount);
            this.walkAnimationState.stop();
        }
    }

    /**
     * Dynamic particle emitters attached to {@code hand_l_grp} and {@code hand_r_grp} plus trailing
     * blue soulfire wake embers along {@code tail_5_grp}.
     */
    private void spawnClientHandAndWakeParticles() {
        if (this.isDeadOrDying()) {
            return;
        }

        float bodyYawRad = this.yBodyRot * Mth.DEG_TO_RAD;
        Vec3 forward = new Vec3(-Mth.sin(bodyYawRad), 0.0D, Mth.cos(bodyYawRad));
        Vec3 right = new Vec3(-Mth.cos(bodyYawRad), 0.0D, -Mth.sin(bodyYawRad));

        boolean gliding = (this.getGlideMode() == MODE_GLIDING);
        boolean conjuring = this.isConjuringOrb();

        Vec3 leftPalm;
        Vec3 rightPalm;
        if (conjuring) {
            // Both hands cupped together in front of chest!
            Vec3 center = this.position().add(forward.scale(0.58D)).add(0.0D, gliding ? 1.52D : 1.22D, 0.0D);
            leftPalm = center.subtract(right.scale(0.10D));
            rightPalm = center.add(right.scale(0.10D));
        } else if (gliding) {
            // Swept-back thruster hands during high-speed desert glide!
            Vec3 base = this.position().subtract(forward.scale(0.28D)).add(0.0D, 1.38D, 0.0D);
            leftPalm = base.subtract(right.scale(0.46D));
            rightPalm = base.add(right.scale(0.46D));
        } else {
            // Open curled three-clawed hands on left & right sides
            double bob = Math.sin(this.tickCount * 0.11D) * 0.05D;
            Vec3 base = this.position().add(forward.scale(0.24D)).add(0.0D, 1.02D + bob, 0.0D);
            leftPalm = base.subtract(right.scale(0.48D));
            rightPalm = base.add(right.scale(0.48D));
        }

        // Flickering blue soulfire flames cupped in both hands (hand_l_grp & hand_r_grp)
        if (this.tickCount % 2 == 0) {
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    leftPalm.x + (this.random.nextDouble() - 0.5D) * 0.08D,
                    leftPalm.y + (this.random.nextDouble() - 0.5D) * 0.06D,
                    leftPalm.z + (this.random.nextDouble() - 0.5D) * 0.08D,
                    0.0D, 0.018D, 0.0D);
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    rightPalm.x + (this.random.nextDouble() - 0.5D) * 0.08D,
                    rightPalm.y + (this.random.nextDouble() - 0.5D) * 0.06D,
                    rightPalm.z + (this.random.nextDouble() - 0.5D) * 0.08D,
                    0.0D, 0.018D, 0.0D);
        }
        if (this.random.nextInt(3) == 0) {
            this.level().addParticle(CYAN_PALM_DUST, leftPalm.x, leftPalm.y + 0.04D, leftPalm.z, 0.0D, 0.01D, 0.0D);
            this.level().addParticle(CYAN_PALM_DUST, rightPalm.x, rightPalm.y + 0.04D, rightPalm.z, 0.0D, 0.01D, 0.0D);
        }

        // Trailing genie vortex-tail wake embers
        if (gliding || this.getDeltaMovement().horizontalDistanceSqr() > 4.0E-4D || this.random.nextInt(4) == 0) {
            Vec3 tailTip = this.position().subtract(forward.scale(gliding ? 0.65D : 0.25D)).add(0.0D, gliding ? 0.72D : 0.28D, 0.0D);
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    tailTip.x + (this.random.nextDouble() - 0.5D) * 0.22D,
                    tailTip.y,
                    tailTip.z + (this.random.nextDouble() - 0.5D) * 0.22D,
                    0.0D, 0.012D, 0.0D);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(serverLevel, source, amount);
        if (hurt && this.isAlive() && this.attackTicks <= 0 && this.phaseGlideTicks <= 0 && this.modeTransitionTicks <= 0) {
            serverLevel.broadcastEntityEvent(this, EVENT_HURT_RECOIL);
        }
        return hurt;
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_SOULFIRE_HURL -> {
                this.clientAttackTicks = SOULFIRE_HURL_DURATION_TICKS;
                this.attackAnimationState.start(this.tickCount);
            }
            case EVENT_GLIDE_TAKEOFF -> {
                this.clientTakeoffTicks = GLIDE_TAKEOFF_DURATION_TICKS;
                this.glideTakeoffAnimationState.start(this.tickCount);
            }
            case EVENT_GLIDE_LANDING -> {
                this.clientLandingTicks = GLIDE_LANDING_DURATION_TICKS;
                this.glideLandingAnimationState.start(this.tickCount);
            }
            case EVENT_PHASE_GLIDE -> {
                this.clientPhaseTicks = PHASE_GLIDE_DURATION_TICKS;
                this.phaseGlideAnimationState.start(this.tickCount);
            }
            case EVENT_HURT_RECOIL -> this.hurtAnimationState.start(this.tickCount);
            default -> super.handleEntityEvent(id);
        }
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (this.deathTime == 1 && !this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            this.setGlideMode(MODE_HOVER_GROUND);
            this.setConjuringOrb(false);
            this.setPhasingBars(false);
            this.activeSoulfireOrbs.clear();
            serverLevel.playSound(null, this.getX(), this.getY() + 0.5D, this.getZ(),
                    SoundEvents.CHAIN_FALL, SoundSource.HOSTILE, 1.0F, 0.80F);
            serverLevel.sendParticles(ParticleTypes.SOUL,
                    this.getX(), this.getY() + 1.0D, this.getZ(),
                    16, 0.45D, 0.55D, 0.45D, 0.04D);
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX(), this.getY() + 0.9D, this.getZ(),
                    22, 0.45D, 0.50D, 0.45D, 0.05D);
        }
        if (this.deathTime >= DEATH_DURATION_TICKS && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        boolean isOffering = held.is(Items.SOUL_SAND)
                || held.is(Items.SOUL_SOIL)
                || held.is(Items.GOLD_INGOT)
                || held.is(Items.COPPER_INGOT);
        if (player.isShiftKeyDown() && isOffering && this.getTarget() == null && this.isAlive()) {
            if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                this.heal(6.0F);
                serverLevel.playSound(null, this.getX(), this.getY() + 1.2D, this.getZ(),
                        SoundEvents.SOUL_ESCAPE.value(), SoundSource.NEUTRAL, 0.90F, 1.20F);
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX(), this.getY() + 1.2D, this.getZ(),
                        16, 0.40D, 0.40D, 0.40D, 0.03D);
                if (this.collarShedCooldown <= 0) {
                    this.collarShedCooldown = COLLAR_SHED_COOLDOWN_TICKS;
                    Item collarItem = BuiltInRegistries.ITEM.getOptional(BRASS_DJINN_COLLAR_ID).orElse(Items.GOLD_INGOT);
                    this.spawnAtLocation(serverLevel, new ItemStack(collarItem));
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    public static boolean isDjinnSpawnSurface(BlockGetter level, BlockPos belowPos, BlockState belowState) {
        if (belowState.isAir()) {
            return false;
        }
        String path = BuiltInRegistries.BLOCK.getKey(belowState.getBlock()).getPath();
        return belowState.is(Blocks.SOUL_SAND)
                || belowState.is(Blocks.SOUL_SOIL)
                || belowState.is(Blocks.SAND)
                || belowState.is(Blocks.SANDSTONE)
                || belowState.is(Blocks.RED_SAND)
                || belowState.is(Blocks.RED_SANDSTONE)
                || belowState.is(Blocks.BASALT)
                || belowState.is(Blocks.BLACKSTONE)
                || path.contains("soul")
                || path.contains("sand")
                || path.contains("silica")
                || belowState.isFaceSturdy(level, belowPos, Direction.UP);
    }

    public static boolean checkSoulfireDjinnSpawnRules(
            EntityType<? extends SoulfireDjinnEntity> type,
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
        for (int dy = 1; dy <= 3; dy++) {
            BlockPos below = pos.below(dy);
            BlockState st = level.getBlockState(below);
            if (st.isAir()) {
                continue;
            }
            return isDjinnSpawnSurface(level, below, st);
        }
        return false;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        for (int dy = 1; dy <= 3; dy++) {
            BlockPos below = this.blockPosition().below(dy);
            BlockState st = level.getBlockState(below);
            if (st.isAir()) {
                continue;
            }
            return isDjinnSpawnSurface(level, below, st);
        }
        return super.checkSpawnRules(level, spawnReason);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("GlideMode", Codec.INT, this.getGlideMode());
        output.store("CollarShedCooldown", Codec.INT, this.collarShedCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        int saved = input.read("GlideMode", Codec.INT).orElse(MODE_HOVER_GROUND);
        this.setGlideMode(saved == MODE_GLIDING ? MODE_GLIDING : MODE_HOVER_GROUND);
        this.collarShedCooldown = input.read("CollarShedCooldown", Codec.INT).orElse(0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BLAZE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.BLAZE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BLAZE_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.72F;
    }

    @Override
    public float getVoicePitch() {
        return 0.78F + this.random.nextFloat() * 0.20F;
    }

    // =========================================================================
    // AI GOALS: Cupped-Palm Soulfire Combat & High-Speed Aerial Desert Gliding
    // =========================================================================

    private static final class DjinnSoulfireCombatGoal extends Goal {
        private final SoulfireDjinnEntity djinn;
        private boolean strafeClockwise = true;
        private int strafeTimer = 0;

        DjinnSoulfireCombatGoal(SoulfireDjinnEntity djinn) {
            this.djinn = djinn;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.djinn.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.djinn.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = this.djinn.getTarget();
            if (target == null || !(this.djinn.level() instanceof ServerLevel serverLevel)) {
                return;
            }

            this.djinn.getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (this.djinn.attackTicks > 0 || this.djinn.phaseGlideTicks > 0 || this.djinn.modeTransitionTicks > 0) {
                return;
            }

            double distSq = this.djinn.distanceToSqr(target);

            // Hurl cupped-palm Soulfire Fireball (6.0 damage + Soul Wither)
            if (distSq <= 484.0D && this.djinn.attackCooldown <= 0 && this.djinn.hasLineOfSight(target)) {
                this.djinn.startCuppedPalmAttack(serverLevel);
                return;
            }

            this.strafeTimer++;
            if (this.strafeTimer % 55 == 0) {
                this.strafeClockwise = !this.strafeClockwise;
            }

            if (this.djinn.getGlideMode() == MODE_GLIDING) {
                double ang = (this.djinn.tickCount * 0.065D) * (this.strafeClockwise ? 1.0D : -1.0D);
                double radius = 6.5D;
                double gx = target.getX() + Math.cos(ang) * radius;
                double gz = target.getZ() + Math.sin(ang) * radius;
                double gy = target.getY() + 2.4D;
                this.djinn.getMoveControl().setWantedPosition(gx, gy, gz, 1.18D);
            } else {
                if (distSq > 81.0D) {
                    this.djinn.getNavigation().moveTo(target, 1.10D);
                } else if (distSq < 16.0D) {
                    Vec3 away = this.djinn.position().subtract(target.position());
                    Vec3 horiz = new Vec3(away.x, 0.0D, away.z);
                    if (horiz.lengthSqr() > 1.0E-4D) {
                        Vec3 retreat = this.djinn.position().add(horiz.normalize().scale(4.0D));
                        this.djinn.getNavigation().moveTo(retreat.x, retreat.y, retreat.z, 1.05D);
                    }
                }
            }
        }
    }

    private static final class DjinnDesertGlideGoal extends Goal {
        private final SoulfireDjinnEntity djinn;
        @Nullable
        private Vec3 waypoint;
        private int ticksLeft = 0;

        DjinnDesertGlideGoal(SoulfireDjinnEntity djinn) {
            this.djinn = djinn;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.djinn.getGlideMode() != MODE_GLIDING || this.djinn.getTarget() != null) {
                return false;
            }
            this.waypoint = this.pickGlideWaypoint();
            return this.waypoint != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.djinn.getGlideMode() == MODE_GLIDING
                    && this.djinn.getTarget() == null
                    && this.waypoint != null
                    && this.ticksLeft > 0
                    && this.djinn.position().distanceToSqr(this.waypoint) > 2.25D;
        }

        @Override
        public void start() {
            this.ticksLeft = 80;
            if (this.waypoint != null) {
                this.djinn.getMoveControl().setWantedPosition(this.waypoint.x, this.waypoint.y, this.waypoint.z, 1.08D);
            }
        }

        @Override
        public void tick() {
            this.ticksLeft--;
        }

        @Nullable
        private Vec3 pickGlideWaypoint() {
            RandomSource rng = this.djinn.getRandom();
            Level level = this.djinn.level();
            for (int attempt = 0; attempt < 8; attempt++) {
                double dx = (rng.nextDouble() - 0.5D) * 18.0D;
                double dy = (rng.nextDouble() - 0.4D) * 6.0D;
                double dz = (rng.nextDouble() - 0.5D) * 18.0D;
                BlockPos check = BlockPos.containing(this.djinn.getX() + dx, this.djinn.getY() + dy, this.djinn.getZ() + dz);
                if (level.isEmptyBlock(check) && level.isEmptyBlock(check.above())) {
                    return Vec3.atCenterOf(check);
                }
            }
            return null;
        }
    }
}
