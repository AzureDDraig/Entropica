package ddraig.net.entropica.entity.mirage_fata_sprite;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Duplicate mirror-illusion entity spawned by {@link MirageFataSpriteEntity} during {@code mirage_illusion}.
 * Shares the Mirage Fata-Sprite's appearance, glow, and animations, mirrors the main sprite's movement,
 * heading, and solar-dish animations across a reflection plane to draw away hostile mob aggro, and lacks
 * the mirage/afterimage split ability or item/XP drops.
 */
public class MirageFataAfterimageEntity extends MirageFataSpriteEntity {

    private @Nullable MirageFataSpriteEntity mirrorOwner = null;
    private Vec3 mirrorOrigin = Vec3.ZERO;
    private double mirrorNormalX = 1.0D;
    private double mirrorNormalZ = 0.0D;
    private int remainingMirrorTicks = -1;
    private int standaloneDecoyTicks = -1;
    private int splitOutTicks = 0;
    private boolean evaporating = false;

    public MirageFataAfterimageEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 0;
    }

    @Override
    protected boolean canCastMirageIllusion() {
        return false;
    }

    @Override
    protected boolean canFocusSolarDishAutonomously() {
        return false;
    }

    @Override
    protected boolean shouldRunAutonomousHover() {
        return this.mirrorOwner == null && this.remainingMirrorTicks < 0;
    }

    @Override
    protected void dropFromLootTable(ServerLevel serverLevel, DamageSource damageSource, boolean playerKilled) {
        // Mirror afterimages never drop loot
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel serverLevel, DamageSource damageSource, boolean recentlyHit) {
        // Mirror afterimages never drop loot
    }

    @Override
    public boolean shouldDropExperience() {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return this.mirrorOwner == null
                && this.remainingMirrorTicks < 0
                && this.standaloneDecoyTicks < 0
                && super.shouldBeSaved();
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    public void activateStandaloneDecoy(int durationTicks) {
        this.mirrorOwner = null;
        this.remainingMirrorTicks = -1;
        this.standaloneDecoyTicks = Math.max(20, durationTicks);
        this.evaporating = false;
        this.setIllusionActive(true);
        this.setSolarFocusing(true);
    }

    public void bindToMirrorOwner(MirageFataSpriteEntity owner, double normalX, double normalZ, int durationTicks) {
        this.mirrorOwner = owner;
        this.mirrorOrigin = owner.position();
        double len = Math.hypot(normalX, normalZ);
        if (len > 1.0E-5D) {
            this.mirrorNormalX = normalX / len;
            this.mirrorNormalZ = normalZ / len;
        } else {
            this.mirrorNormalX = 1.0D;
            this.mirrorNormalZ = 0.0D;
        }
        this.remainingMirrorTicks = durationTicks;
        this.splitOutTicks = 0;
        this.evaporating = false;
        this.setIllusionActive(true);
    }

    public void mirrorSolarDish(ServerLevel serverLevel) {
        if (!this.isAlive() || this.evaporating) {
            return;
        }
        this.solarFocusTicks = SOLAR_REFLECT_DURATION_TICKS;
        this.setSolarFocusing(true);
        serverLevel.broadcastEntityEvent(this, EVENT_SOLAR_REFLECT);
    }

    public void startEvaporation(ServerLevel serverLevel) {
        if (this.evaporating || !this.isAlive()) {
            return;
        }
        this.evaporating = true;
        serverLevel.playSound(null, this.getX(), this.getY() + 0.85D, this.getZ(),
                SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.NEUTRAL, 0.65F, 1.55F);
        serverLevel.sendParticles(NEON_CYAN_DUST,
                this.getX(), this.getY() + 0.90D, this.getZ(),
                18, 0.55D, 0.40D, 0.55D, 0.03D);
        serverLevel.sendParticles(SOLAR_GOLD_DUST,
                this.getX(), this.getY() + 0.90D, this.getZ(),
                12, 0.45D, 0.35D, 0.45D, 0.02D);
        this.setHealth(0.0F);
        this.die(this.damageSources().generic());
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        if (this.evaporating || !this.isAlive()) {
            return false;
        }
        // Striking a bound mirror afterimage or standalone prism decoy shatters the illusion into shimmering motes
        if (this.mirrorOwner != null || this.remainingMirrorTicks >= 0 || this.standaloneDecoyTicks >= 0) {
            serverLevel.playSound(null, this.getX(), this.getY() + 0.85D, this.getZ(),
                    SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.NEUTRAL, 0.80F, 1.45F);
            this.startEvaporation(serverLevel);
            return true;
        }
        return super.hurtServer(serverLevel, source, amount);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()
                && this.level() instanceof ServerLevel serverLevel
                && !this.evaporating
                && this.isAlive()
                && this.standaloneDecoyTicks >= 0) {
            if (--this.standaloneDecoyTicks <= 0) {
                this.startEvaporation(serverLevel);
                return;
            }
            if (this.tickCount % 10 == 0) {
                List<Mob> nearbyMobs = serverLevel.getEntitiesOfClass(
                        Mob.class,
                        this.getBoundingBox().inflate(12.0D),
                        m -> m != this && m.isAlive() && !(m instanceof MirageFataSpriteEntity)
                );
                for (Mob mob : nearbyMobs) {
                    if (mob instanceof Monster) {
                        mob.setTarget(this);
                        mob.getNavigation().moveTo(this, 1.15D);
                    }
                }
            }
            return;
        }

        if (!this.level().isClientSide()
                && this.level() instanceof ServerLevel serverLevel
                && !this.evaporating
                && this.isAlive()
                && this.remainingMirrorTicks >= 0) {
            if (this.mirrorOwner == null
                    || !this.mirrorOwner.isAlive()
                    || this.mirrorOwner.isRemoved()
                    || --this.remainingMirrorTicks <= 0) {
                this.startEvaporation(serverLevel);
                return;
            }

            this.splitOutTicks++;
            this.getNavigation().stop();

            // Gently glide the mirror plane origin horizontally if the main sprite travels far
            Vec3 ownerPos = this.mirrorOwner.position();
            this.mirrorOrigin = new Vec3(
                    Mth.lerp(0.045D, this.mirrorOrigin.x, ownerPos.x),
                    ownerPos.y,
                    Mth.lerp(0.045D, this.mirrorOrigin.z, ownerPos.z)
            );

            double dx = ownerPos.x - this.mirrorOrigin.x;
            double dz = ownerPos.z - this.mirrorOrigin.z;
            double proj = dx * this.mirrorNormalX + dz * this.mirrorNormalZ;

            // Reflect horizontal position across the mirror plane, enforcing a clear lateral split from frame 1
            double rx = dx - 2.0D * proj * this.mirrorNormalX;
            double rz = dz - 2.0D * proj * this.mirrorNormalZ;
            double splitFactor = Mth.clamp(this.splitOutTicks / 12.0D, 0.0D, 1.0D);
            double minHalfSpan = 1.90D * splitFactor;
            if (Math.abs(proj) < minHalfSpan) {
                double pushSign = (proj <= 0.0D) ? 1.0D : -1.0D;
                double extra = minHalfSpan - Math.abs(proj);
                rx += pushSign * extra * this.mirrorNormalX;
                rz += pushSign * extra * this.mirrorNormalZ;
            }

            Vec3 targetPos = new Vec3(this.mirrorOrigin.x + rx, ownerPos.y, this.mirrorOrigin.z + rz);
            Vec3 toTarget = targetPos.subtract(this.position());
            Vec3 step = new Vec3(
                    Mth.clamp(toTarget.x * 0.38D, -0.45D, 0.45D),
                    Mth.clamp(toTarget.y * 0.38D, -0.35D, 0.35D),
                    Mth.clamp(toTarget.z * 0.38D, -0.45D, 0.45D)
            );

            // Reflect velocity so client glide (walk) / hover (idle) animations mirror the main sprite
            Vec3 ownerVel = this.mirrorOwner.getDeltaMovement();
            double vProj = ownerVel.x * this.mirrorNormalX + ownerVel.z * this.mirrorNormalZ;
            Vec3 mirroredVel = new Vec3(
                    ownerVel.x - 2.0D * vProj * this.mirrorNormalX,
                    ownerVel.y,
                    ownerVel.z - 2.0D * vProj * this.mirrorNormalZ
            );

            this.move(MoverType.SELF, step);
            this.setDeltaMovement(mirroredVel.add(step.scale(0.35D)));

            // Reflect yaw, body yaw, and head yaw across the mirror plane
            float mirroredYaw = reflectYaw(this.mirrorOwner.getYRot(), this.mirrorNormalX, this.mirrorNormalZ);
            float mirroredBodyYaw = reflectYaw(this.mirrorOwner.yBodyRot, this.mirrorNormalX, this.mirrorNormalZ);
            float mirroredHeadYaw = reflectYaw(this.mirrorOwner.getYHeadRot(), this.mirrorNormalX, this.mirrorNormalZ);
            this.setYRot(mirroredYaw);
            this.yBodyRot = mirroredBodyYaw;
            this.setYHeadRot(mirroredHeadYaw);
            this.setXRot(this.mirrorOwner.getXRot());

            this.setSolarFocusing(this.mirrorOwner.isSolarFocusing());
            this.setIllusionActive(this.mirrorOwner.isIllusionActive());

            if (this.tickCount % 10 == 0) {
                List<Mob> nearbyMobs = serverLevel.getEntitiesOfClass(
                        Mob.class,
                        this.getBoundingBox().inflate(12.0D),
                        m -> m != this && m != this.mirrorOwner && m.isAlive() && !(m instanceof MirageFataSpriteEntity)
                );
                for (Mob mob : nearbyMobs) {
                    if (mob.getTarget() == this.mirrorOwner || (mob instanceof Monster && mob.getTarget() == null)) {
                        mob.setTarget(this);
                    }
                }
            }
        }
    }

    private static float reflectYaw(float yawDegrees, double nx, double nz) {
        double rad = yawDegrees * Mth.DEG_TO_RAD;
        double fx = -Math.sin(rad);
        double fz = Math.cos(rad);
        double dot = fx * nx + fz * nz;
        double rfx = fx - 2.0D * dot * nx;
        double rfz = fz - 2.0D * dot * nz;
        return (float) (Mth.atan2(-rfx, rfz) * Mth.RAD_TO_DEG);
    }
}
