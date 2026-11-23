package ddraig.net.entropica.entity.mirage_fata_sprite;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.biome10.QilinLightRefractorBlock;
import ddraig.net.entropica.block.entity.AstralCollectorBlockEntity;
import ddraig.net.entropica.registry.ModEntityTypes;
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
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DaylightDetectorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
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
 * Fauna 43 — Mirage Fata-Sprite (Desert Sylph) (entropica:mirage_fata_sprite).
 * Passive hovering crystalline desert sylph native to 5.3 Red Silt-Wastes & 5.25 Void-Charged Flats:
 * - Floats smoothly above hot dunes on rising heat thermals (idle / walk).
 * - Holographic Refraction & Mirror Illusion (hurt + mirage_illusion): when struck, shears its
 *   neon-cyan mirage frame off its sun-stone core to blind attackers, wipe nearby mob aggro, and
 *   evade follow-up hits with 50% holographic phase dodge.
 * - Parabolic Solar Dish Reflection (attack): under bright skies (or when offered Quartz, Amethyst,
 *   Glass Panes, or Glowstone Dust), pitches its 4 mirror-shards upright to dazzle nearby monsters
 *   with Glowing + solar ignition, warm nearby players with Fire Resistance, and shed Shimmering Mirage Auras.
 * - Mirage Evaporation (death): rises on a thermal updraft and evaporates into shimmering motes.
 */
public class MirageFataSpriteEntity extends PathfinderMob implements FlyingAnimal {

    private static final EntityDataAccessor<Boolean> ILLUSION_ACTIVE =
            SynchedEntityData.defineId(MirageFataSpriteEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SOLAR_FOCUSING =
            SynchedEntityData.defineId(MirageFataSpriteEntity.class, EntityDataSerializers.BOOLEAN);

    protected static final byte EVENT_SOLAR_REFLECT = 4;
    protected static final byte EVENT_MIRAGE_ILLUSION = 61;
    protected static final byte EVENT_HURT_SHEAR = 62;

    protected static final int SOLAR_REFLECT_DURATION_TICKS = 36;   // 1.80s animation
    protected static final int MIRAGE_ILLUSION_DURATION_TICKS = 32; // 1.60s animation
    protected static final int HURT_DURATION_TICKS = 13;            // 0.65s animation
    protected static final int DEATH_DURATION_TICKS = 44;           // 2.20s animation
    private static final int AURA_SHED_COOLDOWN_TICKS = 1200;
    private static final int AFTERIMAGE_DURATION_TICKS = 120;       // 6.00s mirror illusion

    private static final ResourceLocation SHIMMERING_MIRAGE_AURA_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "shimmering_mirage_aura");

    protected static final DustParticleOptions NEON_CYAN_DUST = new DustParticleOptions(0x22D3EE, 0.60F);
    protected static final DustParticleOptions SOLAR_GOLD_DUST = new DustParticleOptions(0xFBBF24, 0.65F);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState mirageIllusionAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();

    private int illusionTicks = 0;
    private int illusionCooldown = 0;
    protected int solarFocusTicks = 0;
    private int ambientSolarTimer = 260;
    private int auraShedCooldown = 0;
    private @Nullable MirageFataAfterimageEntity activeAfterimage = null;
    private @Nullable BlockPos focusedCollectorPos = null;

    public MirageFataSpriteEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 5;
        this.moveControl = new FlyingMoveControl(this, 18, true);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.LAVA, -1.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.FLYING_SPEED, 0.38D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 20.0D);
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
        builder.define(ILLUSION_ACTIVE, false);
        builder.define(SOLAR_FOCUSING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.45D));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.15D, MirageFataSpriteEntity::isRefractiveOfferingItem, false));
        this.goalSelector.addGoal(3, new ThermalMirageDriftGoal(this));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    public static boolean isRefractiveOfferingItem(ItemStack stack) {
        return stack.is(Items.QUARTZ)
                || stack.is(Items.AMETHYST_SHARD)
                || stack.is(Items.GLASS_PANE)
                || stack.is(Items.GLOWSTONE_DUST);
    }

    public boolean isIllusionActive() {
        return this.entityData.get(ILLUSION_ACTIVE);
    }

    public void setIllusionActive(boolean active) {
        this.entityData.set(ILLUSION_ACTIVE, active);
    }

    public boolean isSolarFocusing() {
        return this.entityData.get(SOLAR_FOCUSING);
    }

    public void setSolarFocusing(boolean focusing) {
        this.entityData.set(SOLAR_FOCUSING, focusing);
    }

    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    private static boolean isThinSnowOrFlora(BlockState state) {
        if (state.is(Blocks.SNOW)) {
            return true;
        }
        return !state.isAir() && !state.blocksMotion() && state.getFluidState().isEmpty();
    }

    public static boolean isMirageSpawnSurface(BlockGetter level, BlockPos belowPos, BlockState belowState) {
        if (belowState.isAir()) {
            return false;
        }
        String path = BuiltInRegistries.BLOCK.getKey(belowState.getBlock()).getPath();
        return belowState.is(Blocks.SAND)
                || belowState.is(Blocks.RED_SAND)
                || belowState.is(Blocks.SANDSTONE)
                || belowState.is(Blocks.RED_SANDSTONE)
                || belowState.is(Blocks.BLUE_ICE)
                || belowState.is(Blocks.PACKED_ICE)
                || belowState.is(Blocks.SNOW_BLOCK)
                || belowState.is(BlockTags.TERRACOTTA)
                || "glass_sand".equals(path)
                || "glass_sandstone".equals(path)
                || "mirage_ice".equals(path)
                || path.contains("silt")
                || path.contains("void_sand")
                || path.contains("void_stone")
                || path.contains("flat")
                || (belowState.getFluidState().isEmpty() && belowState.isFaceSturdy(level, belowPos, Direction.UP));
    }

    public static boolean checkMirageFataSpriteSpawnRules(
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
        for (int dy = 1; dy <= 5; dy++) {
            BlockPos belowPos = pos.below(dy);
            BlockState belowState = level.getBlockState(belowPos);
            if (belowState.isAir() || isThinSnowOrFlora(belowState)) {
                continue;
            }
            return isMirageSpawnSurface(level, belowPos, belowState);
        }
        return false;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, EntitySpawnReason spawnReason) {
        for (int dy = 1; dy <= 5; dy++) {
            BlockPos belowPos = this.blockPosition().below(dy);
            BlockState belowState = level.getBlockState(belowPos);
            if (belowState.isAir() || isThinSnowOrFlora(belowState)) {
                continue;
            }
            if (isMirageSpawnSurface(level, belowPos, belowState)) {
                return true;
            }
            break;
        }
        return super.checkSpawnRules(level, spawnReason);
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        // Hovering desert sylph never takes fall damage
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        if (source.is(DamageTypes.CACTUS)
                || source.is(DamageTypes.IN_WALL)
                || source.is(DamageTypeTags.IS_FALL)
                || source.is(DamageTypeTags.IS_FIRE)) {
            return true;
        }
        return super.isInvulnerableTo(level, source);
    }

    protected boolean canCastMirageIllusion() {
        return true;
    }

    protected boolean canFocusSolarDishAutonomously() {
        return true;
    }

    protected boolean shouldRunAutonomousHover() {
        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        // 50% holographic mirage evasion while illusion is active (main sprite only)
        if (this.canCastMirageIllusion()
                && this.isIllusionActive()
                && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                && this.random.nextFloat() < 0.50F) {
            serverLevel.playSound(null, this.getX(), this.getY() + 0.8D, this.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 0.70F, 1.55F);
            serverLevel.sendParticles(NEON_CYAN_DUST,
                    this.getX(), this.getY() + 0.9D, this.getZ(),
                    14, 0.55D, 0.40D, 0.55D, 0.03D);
            return false;
        }

        boolean hurt = super.hurtServer(serverLevel, source, amount);
        if (hurt && this.isAlive()) {
            serverLevel.broadcastEntityEvent(this, EVENT_HURT_SHEAR);
            if (this.canCastMirageIllusion() && this.illusionCooldown <= 0) {
                this.triggerMirageIllusion(serverLevel, source.getEntity());
            }
        }
        return hurt;
    }

    private void triggerMirageIllusion(ServerLevel serverLevel, @Nullable Entity attacker) {
        if (!this.canCastMirageIllusion()) {
            return;
        }
        this.illusionTicks = AFTERIMAGE_DURATION_TICKS;
        this.illusionCooldown = 140;
        this.solarFocusTicks = 0;
        this.setSolarFocusing(false);
        this.setIllusionActive(true);
        serverLevel.broadcastEntityEvent(this, EVENT_MIRAGE_ILLUSION);

        if (attacker instanceof LivingEntity liv && liv.isAlive()) {
            liv.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0), this);
        }

        // Release any previous afterimage and spawn a duplicate mirror-illusion entity that mirrors this sprite!
        this.releaseActiveAfterimage(serverLevel, false);

        double yawRad = this.getYRot() * Mth.DEG_TO_RAD;
        // Lateral mirror normal perpendicular to the sprite's current heading
        double normalX = Math.cos(yawRad);
        double normalZ = Math.sin(yawRad);
        if (this.random.nextBoolean()) {
            normalX = -normalX;
            normalZ = -normalZ;
        }

        MirageFataAfterimageEntity afterimage = null;
        if (ModEntityTypes.MIRAGE_FATA_AFTERIMAGE.isPresent()) {
            afterimage = ModEntityTypes.MIRAGE_FATA_AFTERIMAGE.get().create(serverLevel, EntitySpawnReason.MOB_SUMMONED);
        }
        if (afterimage != null) {
            afterimage.setPos(this.getX(), this.getY(), this.getZ());
            afterimage.setYRot(this.getYRot());
            afterimage.yBodyRot = this.yBodyRot;
            afterimage.setYHeadRot(this.getYHeadRot());
            afterimage.setXRot(this.getXRot());
            afterimage.bindToMirrorOwner(this, normalX, normalZ, AFTERIMAGE_DURATION_TICKS);
            if (serverLevel.addFreshEntity(afterimage)) {
                this.activeAfterimage = afterimage;
                serverLevel.broadcastEntityEvent(afterimage, EVENT_MIRAGE_ILLUSION);

                // Push the main sprite smoothly to the opposite side of the mirror plane as the afterimage splits out
                Vec3 vel = this.getDeltaMovement();
                this.setDeltaMovement(vel.x - normalX * 0.22D, vel.y + 0.04D, vel.z - normalZ * 0.22D);

                List<Mob> nearbyMobs = serverLevel.getEntitiesOfClass(
                        Mob.class,
                        this.getBoundingBox().inflate(12.0D),
                        m -> m.isAlive() && !(m instanceof MirageFataSpriteEntity)
                );
                for (Mob mob : nearbyMobs) {
                    if (mob.getTarget() != null || mob instanceof Monster) {
                        mob.setTarget(afterimage);
                    }
                }
            }
        }

        serverLevel.playSound(null, this.getX(), this.getY() + 0.9D, this.getZ(),
                SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.NEUTRAL, 0.85F, 1.35F);
        serverLevel.sendParticles(NEON_CYAN_DUST,
                this.getX(), this.getY() + 0.95D, this.getZ(),
                24, 0.85D, 0.45D, 0.85D, 0.04D);
        serverLevel.sendParticles(SOLAR_GOLD_DUST,
                this.getX(), this.getY() + 0.95D, this.getZ(),
                16, 0.65D, 0.35D, 0.65D, 0.03D);
    }

    private void releaseActiveAfterimage(@Nullable ServerLevel serverLevel, boolean immediateDiscard) {
        if (this.activeAfterimage != null) {
            if (!this.activeAfterimage.isRemoved()) {
                if (immediateDiscard || serverLevel == null) {
                    this.activeAfterimage.discard();
                } else {
                    this.activeAfterimage.startEvaporation(serverLevel);
                }
            }
            this.activeAfterimage = null;
        }
    }

    private @Nullable BlockPos findNearbySolarCollector(ServerLevel serverLevel) {
        BlockPos origin = this.blockPosition();
        BlockPos closest = null;
        double bestDistSqr = Double.MAX_VALUE;
        for (BlockPos check : BlockPos.betweenClosed(origin.offset(-10, -5, -10), origin.offset(10, 5, 10))) {
            BlockState st = serverLevel.getBlockState(check);
            if (st.isAir()) {
                continue;
            }
            String path = BuiltInRegistries.BLOCK.getKey(st.getBlock()).getPath();
            boolean isCollector = st.getBlock() instanceof DaylightDetectorBlock
                    || st.getBlock() instanceof QilinLightRefractorBlock
                    || serverLevel.getBlockEntity(check) instanceof AstralCollectorBlockEntity
                    || path.contains("solar")
                    || path.contains("mirage_reflector");
            if (isCollector) {
                double dSqr = check.distToCenterSqr(this.position());
                if (dSqr < bestDistSqr) {
                    bestDistSqr = dSqr;
                    closest = check.immutable();
                }
            }
        }
        return closest;
    }

    private void beamSunlightToCollector(ServerLevel serverLevel, BlockPos collectorPos) {
        Vec3 from = new Vec3(this.getX(), this.getY() + 0.90D, this.getZ());
        Vec3 to = Vec3.atCenterOf(collectorPos);
        this.getLookControl().setLookAt(to.x, to.y, to.z, 45.0F, 45.0F);

        int steps = 10;
        for (int i = 1; i <= steps; i++) {
            double u = (double) i / (double) steps;
            double px = Mth.lerp(u, from.x, to.x);
            double py = Mth.lerp(u, from.y, to.y);
            double pz = Mth.lerp(u, from.z, to.z);
            serverLevel.sendParticles(SOLAR_GOLD_DUST, px, py, pz, 1, 0.03D, 0.03D, 0.03D, 0.0D);
            if (i % 3 == 0) {
                serverLevel.sendParticles(ParticleTypes.END_ROD, px, py, pz, 1, 0.02D, 0.02D, 0.02D, 0.0D);
            }
        }

        BlockEntity be = serverLevel.getBlockEntity(collectorPos);
        if (be instanceof AstralCollectorBlockEntity astralCollector) {
            astralCollector.receiveCannibalizedBeam(this.blockPosition(), 20, EssenceType.MIRAGE);
        }
        BlockState st = serverLevel.getBlockState(collectorPos);
        if (st.getBlock() instanceof QilinLightRefractorBlock || st.getBlock() instanceof DaylightDetectorBlock) {
            serverLevel.scheduleTick(collectorPos, st.getBlock(), 1);
            serverLevel.updateNeighborsAt(collectorPos, st.getBlock());
        }
    }

    private void triggerParabolicSolarDish(ServerLevel serverLevel, boolean tryShedAura) {
        this.solarFocusTicks = SOLAR_REFLECT_DURATION_TICKS;
        this.focusedCollectorPos = this.findNearbySolarCollector(serverLevel);
        this.setSolarFocusing(true);
        serverLevel.broadcastEntityEvent(this, EVENT_SOLAR_REFLECT);

        if (this.activeAfterimage != null && this.activeAfterimage.isAlive() && !this.activeAfterimage.isRemoved()) {
            this.activeAfterimage.mirrorSolarDish(serverLevel);
        }

        if (this.focusedCollectorPos != null) {
            this.beamSunlightToCollector(serverLevel, this.focusedCollectorPos);
        }

        serverLevel.playSound(null, this.getX(), this.getY() + 0.9D, this.getZ(),
                SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.NEUTRAL, 0.85F, 1.35F);
        serverLevel.sendParticles(ParticleTypes.END_ROD,
                this.getX(), this.getY() + 0.95D, this.getZ(),
                18, 0.75D, 0.45D, 0.75D, 0.05D);
        serverLevel.sendParticles(SOLAR_GOLD_DUST,
                this.getX(), this.getY() + 0.95D, this.getZ(),
                18, 0.75D, 0.45D, 0.75D, 0.02D);

        // Focus sunlight: dazzle nearby monsters & warm nearby peaceful players
        List<LivingEntity> nearby = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(6.0D),
                e -> e != this && e.isAlive()
        );
        for (LivingEntity entity : nearby) {
            if (entity instanceof Monster || entity.getType().is(EntityTypeTags.UNDEAD)) {
                entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0), this);
                if (entity.getType().is(EntityTypeTags.UNDEAD)) {
                    entity.igniteForSeconds(3.0F);
                }
            } else if (entity instanceof Player p && !p.isSpectator()) {
                p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 300, 0), this);
            }
        }

        if (tryShedAura && this.auraShedCooldown <= 0) {
            this.auraShedCooldown = AURA_SHED_COOLDOWN_TICKS;
            Optional<Item> auraOpt = ModItems.SHIMMERING_MIRAGE_AURA.isPresent()
                    ? Optional.of(ModItems.SHIMMERING_MIRAGE_AURA.get())
                    : BuiltInRegistries.ITEM.getOptional(SHIMMERING_MIRAGE_AURA_ID);
            auraOpt.ifPresent(aura ->
                    this.spawnAtLocation(serverLevel, new ItemStack(aura, 1), 0.75F));
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isRefractiveOfferingItem(stack) && this.isAlive()) {
            boolean needsHeal = this.getHealth() < this.getMaxHealth();
            boolean canShed = this.auraShedCooldown <= 0;
            if (needsHeal || canShed || !this.isSolarFocusing()) {
                if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                    stack.consume(1, player);
                    this.heal(4.0F);
                    this.triggerParabolicSolarDish(serverLevel, canShed);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void remove(RemovalReason reason) {
        ServerLevel sl = (this.level() instanceof ServerLevel s) ? s : null;
        this.releaseActiveAfterimage(sl, reason != RemovalReason.KILLED);
        super.remove(reason);
    }

    @Override
    protected void tickDeath() {
        ++this.deathTime;
        if (this.level().isClientSide() && this.deathTime % 3 == 0) {
            this.level().addParticle(NEON_CYAN_DUST,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.7D,
                    this.getY() + 0.8D + this.deathTime * 0.025D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.7D,
                    0.0D, 0.03D, 0.0D);
        }
        if (this.deathTime >= DEATH_DURATION_TICKS && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_SOLAR_REFLECT) {
            this.hurtAnimationState.stop();
            this.attackAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_MIRAGE_ILLUSION) {
            this.attackAnimationState.stop();
            this.mirageIllusionAnimationState.start(this.tickCount);
            return;
        }
        if (id == EVENT_HURT_SHEAR) {
            this.hurtAnimationState.start(this.tickCount);
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public void tick() {
        this.setNoGravity(true);
        super.tick();

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            if (this.illusionCooldown > 0) {
                this.illusionCooldown--;
            }
            if (this.auraShedCooldown > 0) {
                this.auraShedCooldown--;
            }
            if (this.activeAfterimage != null && (!this.activeAfterimage.isAlive() || this.activeAfterimage.isRemoved())) {
                this.activeAfterimage = null;
            }
            if (this.illusionTicks > 0) {
                this.illusionTicks--;
                if (this.illusionTicks == 0) {
                    this.setIllusionActive(false);
                }
            }
            if (this.solarFocusTicks > 0) {
                this.solarFocusTicks--;
                if (this.focusedCollectorPos != null && this.solarFocusTicks % 6 == 0) {
                    this.beamSunlightToCollector(serverLevel, this.focusedCollectorPos);
                }
                if (this.solarFocusTicks == 0) {
                    this.setSolarFocusing(false);
                    this.focusedCollectorPos = null;
                }
            } else if (this.canFocusSolarDishAutonomously() && --this.ambientSolarTimer <= 0) {
                this.ambientSolarTimer = 280 + this.random.nextInt(240);
                if (serverLevel.isBrightOutside() && serverLevel.canSeeSky(this.blockPosition())) {
                    this.triggerParabolicSolarDish(serverLevel, false);
                }
            }

            if (this.isAlive() && this.shouldRunAutonomousHover()) {
                this.maintainThermalHover();
            }
        } else if (this.level().isClientSide()) {
            this.setupClientAnimationsAndParticles();
        }
    }

    private void maintainThermalHover() {
        BlockPos origin = this.blockPosition();
        double surfaceY = Double.NaN;
        for (int dy = 0; dy <= 8; dy++) {
            BlockPos check = origin.below(dy);
            BlockState state = this.level().getBlockState(check);
            if (!state.isAir() && (!state.getFluidState().isEmpty() || state.blocksMotion())) {
                surfaceY = check.getY() + 1.0D;
                break;
            }
        }
        if (Double.isNaN(surfaceY)) {
            return;
        }
        double phase = this.getId() * 0.67D + this.tickCount * 0.055D;
        double targetY = surfaceY + 1.85D + Math.sin(phase) * 0.45D;
        double diffY = targetY - this.getY();
        if (Math.abs(diffY) > 0.05D) {
            Vec3 vel = this.getDeltaMovement();
            double adjustY = Mth.clamp(diffY * 0.09D, -0.09D, 0.11D);
            this.setDeltaMovement(vel.x, Mth.lerp(0.30D, vel.y, adjustY), vel.z);
        }
    }

    private void setupClientAnimationsAndParticles() {
        if (!this.isAlive()) {
            this.idleAnimationState.stop();
            this.walkAnimationState.stop();
            this.attackAnimationState.stop();
            this.mirageIllusionAnimationState.stop();
            this.hurtAnimationState.stop();
            this.deathAnimationState.startIfStopped(this.tickCount);
            return;
        }

        if (this.attackAnimationState.isStarted()
                && this.attackAnimationState.getTimeInMillis(this.tickCount) >= SOLAR_REFLECT_DURATION_TICKS * 50L) {
            this.attackAnimationState.stop();
        }
        if (this.mirageIllusionAnimationState.isStarted()
                && this.mirageIllusionAnimationState.getTimeInMillis(this.tickCount) >= MIRAGE_ILLUSION_DURATION_TICKS * 50L) {
            this.mirageIllusionAnimationState.stop();
        }
        if (this.hurtAnimationState.isStarted()
                && this.hurtAnimationState.getTimeInMillis(this.tickCount) >= HURT_DURATION_TICKS * 50L) {
            this.hurtAnimationState.stop();
        }

        boolean gliding = this.getDeltaMovement().horizontalDistanceSqr() > 2.5E-4D;
        if (gliding) {
            this.idleAnimationState.stop();
            this.walkAnimationState.startIfStopped(this.tickCount);
        } else {
            this.walkAnimationState.stop();
            this.idleAnimationState.startIfStopped(this.tickCount);
        }

        if (this.tickCount % (this.isIllusionActive() ? 3 : 9) == 0) {
            DustParticleOptions dust = (this.random.nextBoolean()) ? NEON_CYAN_DUST : SOLAR_GOLD_DUST;
            this.level().addParticle(
                    dust,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.95D,
                    this.getY() + 0.55D + this.random.nextDouble() * 0.65D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.95D,
                    0.0D, 0.015D, 0.0D
            );
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("AuraShedCooldown", Codec.INT, this.auraShedCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.auraShedCooldown = input.read("AuraShedCooldown", Codec.INT).orElse(0);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.AMETHYST_BLOCK_CHIME;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.AMETHYST_BLOCK_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.AMETHYST_BLOCK_BREAK;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        // Silent hovering desert sylph
    }

    private static final class ThermalMirageDriftGoal extends Goal {
        private final MirageFataSpriteEntity sprite;

        ThermalMirageDriftGoal(MirageFataSpriteEntity sprite) {
            this.sprite = sprite;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return this.sprite.shouldRunAutonomousHover()
                    && !this.sprite.getNavigation().isInProgress()
                    && this.sprite.getRandom().nextInt(22) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.sprite.shouldRunAutonomousHover() && this.sprite.getNavigation().isInProgress();
        }

        @Override
        public void start() {
            Vec3 pos = this.sprite.position();
            double tx = pos.x + (this.sprite.getRandom().nextDouble() - 0.5D) * 16.0D;
            double tz = pos.z + (this.sprite.getRandom().nextDouble() - 0.5D) * 16.0D;
            double ty = pos.y + (this.sprite.getRandom().nextDouble() - 0.5D) * 2.5D;
            this.sprite.getNavigation().moveTo(tx, ty, tz, 1.0D);
        }
    }
}
