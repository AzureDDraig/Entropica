package ddraig.net.entropica.entity.vapor_bellows_crab;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
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
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Fauna 22 — Vapor-Bellows Crab (Thermal Crustacean).
 * Neutral geothermal crustacean native to 2.6 Steaming Skerries:
 * - Passively exhales rising steam puffs from its dorsal basalt chimneys and scuttles sideways along shores.
 * - Hot-Vent Shell Charging (+50% Defense): standing on or within 1.5 blocks of a hot vent sets VENT_CHARGED,
 *   triggers braceDefenseAnimationState, emits ember/steam motes, and halves incoming non-bypass damage.
 * - Heated Pincer Snap (8.0F + 3s ignite) and periodic 3-block Steam Cone Blast (4.0F heat damage + knockback).
 * - Cold-Attack Shedding: freeze/cold damage sheds 1x entropica:porous_basalt_shell on a 600-tick (30s) cooldown.
 */
public class VaporBellowsCrabEntity extends PathfinderMob {

    private static final EntityDataAccessor<Boolean> VENT_CHARGED =
            SynchedEntityData.defineId(VaporBellowsCrabEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_ATTACKING =
            SynchedEntityData.defineId(VaporBellowsCrabEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_VENTING_STEAM =
            SynchedEntityData.defineId(VaporBellowsCrabEntity.class, EntityDataSerializers.BOOLEAN);

    private static final byte EVENT_PINCER_ATTACK = 4;
    private static final byte EVENT_STEAM_VENT = 5;
    private static final byte EVENT_BRACE_DEFENSE = 6;

    private static final int SHED_COOLDOWN_TICKS = 600;
    private static final int STEAM_CONE_COOLDOWN_TICKS = 80;

    private static final ResourceLocation POROUS_BASALT_SHELL_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "porous_basalt_shell");
    private static final ResourceLocation VAPOR_CONDENSATE_CRYSTALS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vapor_condensate_crystals");

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState steamVentAnimationState = new AnimationState();
    public final AnimationState braceDefenseAnimationState = new AnimationState();

    private int shedCooldown = 0;
    private int attackAnimTimer = 0;
    private int steamVentTimer = 0;
    private int steamConeCooldown = 0;
    private int attacksSinceSteamCone = 0;
    private int bracePulseTimer = 0;
    private boolean clientPrevVentCharged = false;

    public VaporBellowsCrabEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
        this.setPathfindingMalus(PathType.LAVA, 8.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 48.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4D)
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    public static boolean isHotVentBlock(BlockState state) {
        if (state.isAir()) {
            return false;
        }
        if (state.is(Blocks.MAGMA_BLOCK)) {
            return true;
        }
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return "steam_vent".equals(path)
                || "steaming_spring_stone".equals(path);
    }

    private static boolean isThinSurfaceCover(BlockState state) {
        if (state.is(Blocks.SNOW)) {
            return true;
        }
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return "vapor_weaver_lichen".equals(path)
                || "mist_veil_fern".equals(path)
                || "auroral_feather_moss".equals(path)
                || "blue_frozen_moss_carpet".equals(path)
                || "shadowed_eclipse_moss".equals(path);
    }

    public static boolean isVaporBellowsCrabSpawnSurface(BlockGetter level, BlockPos belowPos, BlockState belowState) {
        if (belowState.isAir()) {
            return false;
        }
        if (belowState.is(Blocks.GRAVEL)
                || belowState.is(Blocks.BLUE_ICE)
                || belowState.is(Blocks.PACKED_ICE)
                || belowState.is(Blocks.ICE)
                || belowState.is(Blocks.FROSTED_ICE)
                || belowState.is(Blocks.BASALT)
                || belowState.is(Blocks.SMOOTH_BASALT)
                || belowState.is(Blocks.POLISHED_BASALT)
                || belowState.is(Blocks.STONE)
                || belowState.is(Blocks.SAND)
                || belowState.is(Blocks.MAGMA_BLOCK)
                || belowState.is(BlockTags.ICE)
                || belowState.is(BlockTags.SAND)
                || belowState.is(BlockTags.BASE_STONE_OVERWORLD)
                || belowState.is(BlockTags.ANIMALS_SPAWNABLE_ON)) {
            return true;
        }
        String path = BuiltInRegistries.BLOCK.getKey(belowState.getBlock()).getPath();
        return "steaming_ice".equals(path)
                || "steaming_spring_stone".equals(path)
                || "steam_vent".equals(path)
                || "coarse_gravel".equals(path)
                || "basalt_column".equals(path)
                || "glimmer_ice".equals(path)
                || (belowState.getFluidState().isEmpty() && belowState.isFaceSturdy(level, belowPos, Direction.UP));
    }

    public static boolean checkVaporBellowsCrabSpawnRules(
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
        if (isThinSurfaceCover(below) && level.getBlockState(belowPos.below()).blocksMotion()) {
            belowPos = belowPos.below();
            below = level.getBlockState(belowPos);
        }
        return isVaporBellowsCrabSpawnSurface(level, belowPos, below);
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        BlockPos belowPos = this.blockPosition().below();
        BlockState belowState = level.getBlockState(belowPos);
        if (isThinSurfaceCover(belowState) && level.getBlockState(belowPos.below()).blocksMotion()) {
            belowPos = belowPos.below();
            belowState = level.getBlockState(belowPos);
        }
        if (isVaporBellowsCrabSpawnSurface(level, belowPos, belowState)) {
            return true;
        }
        return super.checkSpawnRules(level, spawnReason);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        // Strongly favor hot geothermal vents & steaming spring stone
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 0; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (isHotVentBlock(level.getBlockState(pos.offset(dx, dy, dz)))) {
                        return 12.0F;
                    }
                }
            }
        }
        // Favor shoreline blocks adjacent to water
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (level.getFluidState(pos.below().relative(dir)).is(FluidTags.WATER)
                    || level.getFluidState(pos.relative(dir)).is(FluidTags.WATER)) {
                return 6.0F;
            }
        }
        BlockState below = level.getBlockState(pos.below());
        if (isVaporBellowsCrabSpawnSurface(level, pos.below(), below)) {
            return 2.0F;
        }
        return 0.0F;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VENT_CHARGED, false);
        builder.define(IS_ATTACKING, false);
        builder.define(IS_VENTING_STEAM, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(2, new CrabShoreStrafeGoal(this));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.9D, 70));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
    }

    public boolean isVentCharged() {
        return this.entityData.get(VENT_CHARGED);
    }

    public void setVentCharged(boolean charged) {
        this.entityData.set(VENT_CHARGED, charged);
    }

    public boolean isAttacking() {
        return this.entityData.get(IS_ATTACKING);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(IS_ATTACKING, attacking);
    }

    public boolean isVentingSteam() {
        return this.entityData.get(IS_VENTING_STEAM);
    }

    public void setVentingSteam(boolean venting) {
        this.entityData.set(IS_VENTING_STEAM, venting);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    public boolean checkNearHotVent() {
        Level level = this.level();
        BlockPos onPos = this.getOnPos();
        if (isHotVentBlock(level.getBlockState(onPos)) || isHotVentBlock(level.getBlockState(this.blockPosition()))) {
            return true;
        }

        AABB bb = this.getBoundingBox();
        int minX = Mth.floor(bb.minX - 1.5D);
        int maxX = Mth.floor(bb.maxX + 1.5D);
        int minY = Mth.floor(bb.minY - 1.5D);
        int maxY = Mth.floor(bb.maxY + 1.5D);
        int minZ = Mth.floor(bb.minZ - 1.5D);
        int maxZ = Mth.floor(bb.maxZ + 1.5D);

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    double dx = Math.max(0.0D, Math.max(x - bb.maxX, bb.minX - (x + 1.0D)));
                    double dy = Math.max(0.0D, Math.max(y - bb.maxY, bb.minY - (y + 1.0D)));
                    double dz = Math.max(0.0D, Math.max(z - bb.maxZ, bb.minZ - (z + 1.0D)));
                    if (dx * dx + dy * dy + dz * dz <= 2.25D) {
                        cursor.set(x, y, z);
                        if (isHotVentBlock(level.getBlockState(cursor))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isColdDamage(DamageSource source) {
        if (source.is(DamageTypeTags.IS_FREEZING) || source.is(DamageTypes.FREEZE)) {
            return true;
        }
        Entity direct = source.getDirectEntity();
        if (direct instanceof Snowball) {
            return true;
        }
        if (direct != null) {
            String directPath = BuiltInRegistries.ENTITY_TYPE.getKey(direct.getType()).getPath().toLowerCase(Locale.ROOT);
            if (directPath.contains("cryo") || directPath.contains("frost") || directPath.contains("snow") || directPath.contains("ice")) {
                return true;
            }
        }
        return this.isInPowderSnow || this.getTicksFrozen() > 0;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        if (hit) {
            target.igniteForSeconds(3.0F);
            this.setAttacking(true);
            this.attackAnimTimer = 28;
            level.broadcastEntityEvent(this, EVENT_PINCER_ATTACK);

            level.playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.EVOKER_FANGS_ATTACK,
                    SoundSource.HOSTILE,
                    0.95F,
                    1.25F
            );
            level.sendParticles(
                    ParticleTypes.FLAME,
                    target.getX(),
                    target.getY() + target.getBbHeight() * 0.5D,
                    target.getZ(),
                    8,
                    0.25D,
                    0.25D,
                    0.25D,
                    0.03D
            );
            level.sendParticles(
                    ParticleTypes.CLOUD,
                    target.getX(),
                    target.getY() + target.getBbHeight() * 0.5D,
                    target.getZ(),
                    6,
                    0.25D,
                    0.2D,
                    0.25D,
                    0.04D
            );

            this.attacksSinceSteamCone++;
        }
        return hit;
    }

    private void performSteamConeBlast(ServerLevel level, LivingEntity primaryTarget) {
        this.attacksSinceSteamCone = 0;
        this.steamConeCooldown = STEAM_CONE_COOLDOWN_TICKS;
        this.steamVentTimer = 36;
        this.setVentingSteam(true);

        this.getLookControl().setLookAt(primaryTarget, 45.0F, 30.0F);
        Vec3 toTarget = primaryTarget.position().subtract(this.position());
        Vec3 horizAim = new Vec3(toTarget.x, 0.0D, toTarget.z);
        if (horizAim.lengthSqr() > 1.0E-4D) {
            horizAim = horizAim.normalize();
        } else {
            horizAim = Vec3.directionFromRotation(0.0F, this.getYRot());
        }

        level.broadcastEntityEvent(this, EVENT_STEAM_VENT);
        level.playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.FIRE_EXTINGUISH,
                SoundSource.HOSTILE,
                1.35F,
                0.75F
        );
        level.playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.BLAZE_SHOOT,
                SoundSource.HOSTILE,
                0.85F,
                1.35F
        );

        // Emit 3-block forward steam plume particles
        for (int step = 1; step <= 6; step++) {
            double dist = step * 0.5D;
            double spread = 0.15D + dist * 0.22D;
            level.sendParticles(
                    ParticleTypes.CLOUD,
                    this.getX() + horizAim.x * dist,
                    this.getY() + 0.55D + step * 0.05D,
                    this.getZ() + horizAim.z * dist,
                    5,
                    spread,
                    0.22D,
                    spread,
                    0.05D
            );
        }

        List<LivingEntity> victims = level.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(3.5D, 2.0D, 3.5D),
                entity -> entity != this
                        && entity.isAlive()
                        && !(entity instanceof VaporBellowsCrabEntity)
                        && !(entity instanceof Player p && (p.isCreative() || p.isSpectator()))
        );

        for (LivingEntity victim : victims) {
            Vec3 rel = victim.position().subtract(this.position());
            double horizDistSqr = rel.x * rel.x + rel.z * rel.z;
            if (horizDistSqr > 3.5D * 3.5D) {
                continue;
            }
            if (horizDistSqr > 0.25D) {
                Vec3 dir = new Vec3(rel.x, 0.0D, rel.z).normalize();
                if (horizAim.dot(dir) < 0.35D) {
                    continue;
                }
            }

            int prevInvuln = victim.invulnerableTime;
            victim.invulnerableTime = 0;
            boolean damaged = victim.hurtServer(level, this.damageSources().onFire(), 4.0F);
            if (!damaged) {
                victim.hurtServer(level, this.damageSources().mobAttack(this), 4.0F);
            }
            victim.invulnerableTime = prevInvuln;

            victim.igniteForSeconds(2.0F);
            double kbX = this.getX() - victim.getX();
            double kbZ = this.getZ() - victim.getZ();
            if (kbX * kbX + kbZ * kbZ < 1.0E-4D) {
                kbX = -horizAim.x;
                kbZ = -horizAim.z;
            }
            victim.knockback(0.65D, kbX, kbZ);
            victim.hurtMarked = true;
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean coldHit = isColdDamage(source);
        if (source.getDirectEntity() instanceof Snowball && amount < 1.0F) {
            amount = 1.0F;
        }

        if (this.isVentCharged() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            amount *= 0.5F;
            if (this.attackAnimTimer <= 0 && this.steamVentTimer <= 0) {
                level.broadcastEntityEvent(this, EVENT_BRACE_DEFENSE);
            }
            level.playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.BASALT_HIT,
                    SoundSource.NEUTRAL,
                    1.0F,
                    0.8F
            );
            level.sendParticles(
                    ParticleTypes.CLOUD,
                    this.getX(),
                    this.getY() + 0.85D,
                    this.getZ(),
                    8,
                    0.35D,
                    0.2D,
                    0.35D,
                    0.04D
            );
        }

        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && coldHit && this.shedCooldown <= 0) {
            this.shedCooldown = SHED_COOLDOWN_TICKS;
            Optional<Item> shellItem = BuiltInRegistries.ITEM.getOptional(POROUS_BASALT_SHELL_ID);
            shellItem.ifPresent(item -> this.spawnAtLocation(level, new ItemStack(item, 1), 0.45F));

            level.playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.BASALT_BREAK,
                    SoundSource.NEUTRAL,
                    1.0F,
                    0.9F
            );
            level.playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.NEUTRAL,
                    0.9F,
                    1.2F
            );
            level.sendParticles(
                    ParticleTypes.CLOUD,
                    this.getX(),
                    this.getY() + 0.65D,
                    this.getZ(),
                    14,
                    0.4D,
                    0.25D,
                    0.4D,
                    0.06D
            );
            level.sendParticles(
                    ParticleTypes.SNOWFLAKE,
                    this.getX(),
                    this.getY() + 0.65D,
                    this.getZ(),
                    10,
                    0.4D,
                    0.25D,
                    0.4D,
                    0.04D
            );
        }
        return hurt;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_PINCER_ATTACK) {
            this.attackAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_STEAM_VENT) {
            this.steamVentAnimationState.start(this.tickCount);
            Vec3 forward = Vec3.directionFromRotation(0.0F, this.getYRot());
            for (int i = 0; i < 22; i++) {
                double spreadYaw = (this.random.nextDouble() - 0.5D) * 0.7D;
                double cos = Math.cos(spreadYaw);
                double sin = Math.sin(spreadYaw);
                double vx = (forward.x * cos - forward.z * sin) * (0.18D + this.random.nextDouble() * 0.2D);
                double vz = (forward.x * sin + forward.z * cos) * (0.18D + this.random.nextDouble() * 0.2D);
                this.level().addParticle(
                        ParticleTypes.CLOUD,
                        this.getX() + forward.x * 0.45D,
                        this.getY() + 0.55D + this.random.nextDouble() * 0.35D,
                        this.getZ() + forward.z * 0.45D,
                        vx,
                        0.03D + this.random.nextDouble() * 0.05D,
                        vz
                );
            }
            return;
        }
        if (id == EVENT_BRACE_DEFENSE) {
            this.braceDefenseAnimationState.start(this.tickCount);
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            if (this.shedCooldown > 0) {
                this.shedCooldown--;
            }
            if (this.steamConeCooldown > 0) {
                this.steamConeCooldown--;
            }
            if (this.attackAnimTimer > 0) {
                this.attackAnimTimer--;
                if (this.attackAnimTimer <= 0) {
                    this.setAttacking(false);
                }
            }
            if (this.steamVentTimer > 0) {
                this.steamVentTimer--;
                if (this.steamVentTimer <= 0) {
                    this.setVentingSteam(false);
                }
            }

            // Evaluate Hot-Vent Shell Charging every 10 ticks
            if ((this.tickCount + this.getId()) % 10 == 0) {
                boolean wasCharged = this.isVentCharged();
                boolean nowCharged = this.checkNearHotVent();
                if (nowCharged != wasCharged) {
                    this.setVentCharged(nowCharged);
                    if (nowCharged) {
                        this.bracePulseTimer = 80;
                        serverLevel.broadcastEntityEvent(this, EVENT_BRACE_DEFENSE);
                        serverLevel.playSound(
                                null,
                                this.getX(),
                                this.getY(),
                                this.getZ(),
                                SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT,
                                SoundSource.NEUTRAL,
                                0.8F,
                                0.85F
                        );
                    }
                }
            }

            if (this.isVentCharged()) {
                if (this.bracePulseTimer > 0) {
                    this.bracePulseTimer--;
                } else if (this.getTarget() == null
                        && this.getDeltaMovement().horizontalDistanceSqr() <= 1.0E-4D
                        && this.attackAnimTimer <= 0
                        && this.steamVentTimer <= 0) {
                    this.bracePulseTimer = 90 + this.random.nextInt(40);
                    serverLevel.broadcastEntityEvent(this, EVENT_BRACE_DEFENSE);
                }
            }

            // Periodic 3-block steam cone blast every few attacks when target is within 3.5 blocks
            LivingEntity target = this.getTarget();
            if (target != null
                    && target.isAlive()
                    && this.attacksSinceSteamCone >= 2
                    && this.steamConeCooldown <= 0
                    && this.attackAnimTimer <= 10
                    && this.distanceToSqr(target) <= 3.5D * 3.5D) {
                performSteamConeBlast(serverLevel, target);
            }
        } else if (this.level().isClientSide()) {
            this.setupClientAnimationsAndParticles();
        }
    }

    private void setupClientAnimationsAndParticles() {
        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
        boolean charged = this.isVentCharged();

        if (charged && !this.clientPrevVentCharged) {
            this.braceDefenseAnimationState.start(this.tickCount);
        } else if (!charged && this.clientPrevVentCharged) {
            this.braceDefenseAnimationState.stop();
        }
        this.clientPrevVentCharged = charged;

        if (this.isAttacking()) {
            this.attackAnimationState.startIfStopped(this.tickCount);
        } else if (this.attackAnimationState.getTimeInMillis(this.tickCount) >= 1400L) {
            this.attackAnimationState.stop();
        }

        if (this.isVentingSteam()) {
            this.steamVentAnimationState.startIfStopped(this.tickCount);
        } else if (this.steamVentAnimationState.getTimeInMillis(this.tickCount) >= 1800L) {
            this.steamVentAnimationState.stop();
        }

        if (moving) {
            this.idleAnimationState.stop();
            this.walkAnimationState.startIfStopped(this.tickCount);
        } else {
            this.walkAnimationState.stop();
            this.idleAnimationState.startIfStopped(this.tickCount);
        }

        // Passive rising CLOUD steam puffs from dorsal basalt chimneys
        int puffInterval = charged ? 4 : 8;
        if (this.tickCount % puffInterval == 0 || (this.isVentingSteam() && this.random.nextBoolean())) {
            double chimneyX = this.getX() + (this.random.nextDouble() - 0.5D) * 0.55D;
            double chimneyY = this.getY() + 0.98D + this.random.nextDouble() * 0.18D;
            double chimneyZ = this.getZ() + (this.random.nextDouble() - 0.5D) * 0.45D;
            this.level().addParticle(
                    ParticleTypes.CLOUD,
                    chimneyX,
                    chimneyY,
                    chimneyZ,
                    (this.random.nextDouble() - 0.5D) * 0.015D,
                    0.045D + this.random.nextDouble() * 0.035D,
                    (this.random.nextDouble() - 0.5D) * 0.015D
            );

            if (charged && this.random.nextInt(3) == 0) {
                this.level().addParticle(
                        ParticleTypes.FLAME,
                        chimneyX,
                        chimneyY - 0.08D,
                        chimneyZ,
                        (this.random.nextDouble() - 0.5D) * 0.01D,
                        0.025D,
                        (this.random.nextDouble() - 0.5D) * 0.01D
                );
                this.level().addParticle(
                        ParticleTypes.SMOKE,
                        chimneyX,
                        chimneyY,
                        chimneyZ,
                        0.0D,
                        0.04D,
                        0.0D
                );
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        BuiltInRegistries.ITEM.getOptional(VAPOR_CONDENSATE_CRYSTALS_ID)
                .ifPresent(crystals -> this.spawnAtLocation(level, new ItemStack(crystals, 1)));
        BuiltInRegistries.ITEM.getOptional(POROUS_BASALT_SHELL_ID)
                .ifPresent(shell -> this.spawnAtLocation(level, new ItemStack(shell, 1 + this.random.nextInt(2))));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("ShedCooldown", Codec.INT, this.shedCooldown);
        output.store("VentCharged", Codec.BOOL, this.isVentCharged());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.shedCooldown = input.read("ShedCooldown", Codec.INT).orElse(0);
        this.setVentCharged(input.read("VentCharged", Codec.BOOL).orElse(false));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SILVERFISH_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.TURTLE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.TURTLE_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SPIDER_STEP, 0.2F, 0.85F);
    }

    /**
     * Allows the Vapor-Bellows Crab to occasionally scuttle sideways along shorelines and around hot vents.
     */
    static class CrabShoreStrafeGoal extends Goal {
        private final VaporBellowsCrabEntity crab;
        private float strafeDir = 1.0F;
        private int strafeTicks = 0;

        CrabShoreStrafeGoal(VaporBellowsCrabEntity crab) {
            this.crab = crab;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.crab.getTarget() != null || !this.crab.onGround() || this.crab.isInWater()) {
                return false;
            }
            if (this.crab.getRandom().nextInt(75) != 0) {
                return false;
            }
            this.strafeDir = this.crab.getRandom().nextBoolean() ? 1.0F : -1.0F;
            this.strafeTicks = 28 + this.crab.getRandom().nextInt(32);
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.strafeTicks > 0 && this.crab.getTarget() == null && this.crab.onGround();
        }

        @Override
        public void start() {
            this.crab.getNavigation().stop();
        }

        @Override
        public void tick() {
            this.strafeTicks--;
            this.crab.getMoveControl().strafe(0.0F, this.strafeDir * 0.55F);
            if (this.crab.horizontalCollision && this.strafeTicks > 6) {
                this.strafeDir = -this.strafeDir;
            }
        }

        @Override
        public void stop() {
            this.crab.setZza(0.0F);
            this.crab.setXxa(0.0F);
        }
    }
}
