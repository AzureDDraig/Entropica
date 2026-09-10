package ddraig.net.entropica.entity.gemini_goat_mage;

import ddraig.net.entropica.entity.projectile.RadiantFireLanceEntity;
import ddraig.net.entropica.entity.projectile.UmbralVortexEntity;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class GeminiGoatMageEntity extends PathfinderMob {

    private static final EntityDataAccessor<Integer> CASTING_PHASE = SynchedEntityData.defineId(GeminiGoatMageEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> WITNESSING_ALTAR = SynchedEntityData.defineId(GeminiGoatMageEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState castSolarAnimationState = new AnimationState();
    public final AnimationState castUmbralAnimationState = new AnimationState();
    public final AnimationState castDualAnimationState = new AnimationState();

    private int spellCooldown = 0;
    private int castingTimer = 0;

    public GeminiGoatMageEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 80.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CASTING_PHASE, 0);
        builder.define(WITNESSING_ALTAR, false);
    }

    public int getCastingPhase() {
        return this.entityData.get(CASTING_PHASE);
    }

    public void setCastingPhase(int phase) {
        this.entityData.set(CASTING_PHASE, phase);
    }

    public boolean isWitnessingAltar() {
        return this.entityData.get(WITNESSING_ALTAR);
    }

    public void setWitnessingAltar(boolean witnessing) {
        this.entityData.set(WITNESSING_ALTAR, witnessing);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new GeminiSpellcastingGoal(this));
        this.goalSelector.addGoal(2, new GeminiAltarWitnessGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            this.updateClientAnimations();
            // Ambient flame particles from orbiting foci
            if (this.random.nextFloat() < 0.3F) {
                // Solar flame side (+X)
                this.level().addParticle(ParticleTypes.FLAME, this.getX() + 0.8, this.getY() + 1.8, this.getZ() + 0.2, 0, 0.02, 0);
                // Umbral water side (-X)
                this.level().addParticle(ParticleTypes.WITCH, this.getX() - 0.8, this.getY() + 1.8, this.getZ() + 0.2, 0, 0.02, 0);
            }
        } else {
            if (this.spellCooldown > 0) this.spellCooldown--;
            if (this.castingTimer > 0) {
                this.castingTimer--;
                if (this.castingTimer == 0) {
                    this.setCastingPhase(0);
                }
            }
        }
    }

    private void updateClientAnimations() {
        int phase = this.getCastingPhase();
        boolean isMoving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6D;

        if (phase == 1) { // Solar
            this.castSolarAnimationState.startIfStopped(this.tickCount);
            this.castUmbralAnimationState.stop();
            this.castDualAnimationState.stop();
            this.walkAnimationState.stop();
            this.idleAnimationState.stop();
        } else if (phase == 2) { // Umbral
            this.castUmbralAnimationState.startIfStopped(this.tickCount);
            this.castSolarAnimationState.stop();
            this.castDualAnimationState.stop();
            this.walkAnimationState.stop();
            this.idleAnimationState.stop();
        } else if (phase == 3) { // Dual
            this.castDualAnimationState.startIfStopped(this.tickCount);
            this.castSolarAnimationState.stop();
            this.castUmbralAnimationState.stop();
            this.walkAnimationState.stop();
            this.idleAnimationState.stop();
        } else if (isMoving) {
            this.walkAnimationState.startIfStopped(this.tickCount);
            this.idleAnimationState.stop();
            this.castSolarAnimationState.stop();
            this.castUmbralAnimationState.stop();
            this.castDualAnimationState.stop();
        } else {
            this.idleAnimationState.startIfStopped(this.tickCount);
            this.walkAnimationState.stop();
            this.castSolarAnimationState.stop();
            this.castUmbralAnimationState.stop();
            this.castDualAnimationState.stop();
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        ItemStack offhand = player.getOffhandItem();

        // 1. Entropic Codex Lore Interaction
        if (held.getItem().toString().toLowerCase().contains("codex") || held.getItem().toString().toLowerCase().contains("book")) {
            if (!this.level().isClientSide()) {
                player.displayClientMessage(Component.literal("§6[Gemini Goat Mage] §e\"Ah, a seeker of the twin polarities. Know this: Radiant Fire purges while Umbral Waters dissolve. In their balance lies the Equinox.\""), false);
                this.level().playSound(null, this.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.NEUTRAL, 1.0F, 1.2F);
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.ENCHANT, this.getX(), this.getY() + 1.5, this.getZ(), 25, 0.5, 0.5, 0.5, 0.2);
                }
            }
            return InteractionResult.SUCCESS;
        }

        // 2. Monocle or Astrolabe Observation Interaction
        if (held.getItem().toString().toLowerCase().contains("monocle") || held.getItem().toString().toLowerCase().contains("astrolabe")) {
            if (!this.level().isClientSide()) {
                player.displayClientMessage(Component.literal("§5[Gemini Goat Mage] §b\"The celestial currents flow steady today. The Materia density favors neither sun nor abyss.\""), false);
                this.level().playSound(null, this.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }

        // 3. Duality Barter System
        for (GeminiDualityTrades.DualityTradeOffer trade : GeminiDualityTrades.TRADES) {
            if (trade.matches(held, offhand)) {
                if (!this.level().isClientSide()) {
                    held.shrink(trade.costA.getCount());
                    if (!trade.costB.isEmpty()) {
                        offhand.shrink(trade.costB.getCount());
                    }

                    ItemStack reward = trade.result.copy();
                    if (!player.addItem(reward)) {
                        this.spawnAtLocation((ServerLevel) this.level(), reward);
                    }

                    this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.5F);
                    if (this.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, this.getX(), this.getY() + 1.2, this.getZ(), 20, 0.3, 0.3, 0.3, 0.1);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }

        return super.mobInteract(player, hand);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel serverLevel, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(serverLevel, damageSource, recentlyHit);
        // Rare chance for Equinox Velvet Cloth or Gemini Focus Horn
        if (this.random.nextFloat() < 0.4F) {
            this.spawnAtLocation(serverLevel, new ItemStack(ModItems.EQUINOX_VELVET_CLOTH.get(), 1 + this.random.nextInt(2)));
        }
        if (this.random.nextFloat() < 0.25F) {
            this.spawnAtLocation(serverLevel, new ItemStack(ModItems.GEMINI_FOCUS_HORN.get(), 1));
        }
    }

    // --- AI GOALS ---

    public static class GeminiSpellcastingGoal extends Goal {
        private final GeminiGoatMageEntity mage;
        private LivingEntity target;
        private int spellStep = 0;

        public GeminiSpellcastingGoal(GeminiGoatMageEntity mage) {
            this.mage = mage;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            this.target = mage.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public void tick() {
            if (target == null) return;
            mage.getLookControl().setLookAt(target, 30.0F, 30.0F);

            double distSq = mage.distanceToSqr(target);
            if (distSq > 64.0D) {
                mage.getNavigation().moveTo(target, 1.1D);
            } else if (distSq < 16.0D) {
                // Back away slightly to maintain casting range
                Vec3 away = mage.position().subtract(target.position()).normalize().scale(2.0D);
                mage.getNavigation().moveTo(mage.getX() + away.x, mage.getY(), mage.getZ() + away.z, 1.2D);
            } else {
                mage.getNavigation().stop();
            }

            if (mage.spellCooldown <= 0 && mage.getCastingPhase() == 0) {
                this.spellStep = (this.spellStep + 1) % 3;
                if (this.spellStep == 0) {
                    // Cast 1: Radiant Fire Lance
                    mage.setCastingPhase(1);
                    mage.castingTimer = 32; // 1.6s
                    mage.spellCooldown = 45;

                    Vec3 shootVec = target.position().add(0, target.getEyeHeight() * 0.5, 0).subtract(mage.position().add(0, 1.8, 0)).normalize();
                    RadiantFireLanceEntity fire = new RadiantFireLanceEntity(mage.level(), mage);
                    fire.setPos(mage.getX() + 0.5, mage.getY() + 1.8, mage.getZ());
                    fire.shoot(shootVec.x, shootVec.y, shootVec.z, 1.6F, 1.0F);
                    mage.level().addFreshEntity(fire);

                } else if (this.spellStep == 1) {
                    // Cast 2: Umbral Vortex
                    mage.setCastingPhase(2);
                    mage.castingTimer = 32; // 1.6s
                    mage.spellCooldown = 50;

                    Vec3 shootVec = target.position().subtract(mage.position().add(0, 1.8, 0)).normalize();
                    UmbralVortexEntity vortex = new UmbralVortexEntity(mage.level(), mage);
                    vortex.setPos(mage.getX() - 0.5, mage.getY() + 1.8, mage.getZ());
                    vortex.shoot(shootVec.x, shootVec.y, shootVec.z, 1.2F, 1.0F);
                    mage.level().addFreshEntity(vortex);

                } else {
                    // Cast 3: Dual Gemini Equinox Slam (Ultimate)
                    mage.setCastingPhase(3);
                    mage.castingTimer = 50; // 2.5s
                    mage.spellCooldown = 80;

                    // Concussive shockwave blast
                    mage.level().playSound(null, mage.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 1.2F, 1.2F);
                    target.hurt(mage.damageSources().mobAttack(mage), 12.0F);
                    Vec3 knock = target.position().subtract(mage.position()).normalize().scale(1.8D);
                    target.setDeltaMovement(knock.x, 0.4D, knock.z);
                }
            }
        }
    }

    public static class GeminiAltarWitnessGoal extends Goal {
        private final GeminiGoatMageEntity mage;

        public GeminiAltarWitnessGoal(GeminiGoatMageEntity mage) {
            this.mage = mage;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return mage.getTarget() == null;
        }

        @Override
        public void tick() {
            // Channel orbiting flames if near ritual altars
            if (mage.tickCount % 40 == 0) {
                mage.setWitnessingAltar(true);
            }
        }
    }
}
