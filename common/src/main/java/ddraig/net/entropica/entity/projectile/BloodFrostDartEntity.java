package ddraig.net.entropica.entity.projectile;

import ddraig.net.entropica.entity.sanguine_sea_angel.SanguineSeaAngelEntity;
import ddraig.net.entropica.registry.ModEntityTypes;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Blood-Frost Dart (entropica:blood_frost_dart) — crystalline cryo-hemolymph needle spat by Sanguine Sea-Angels.
 * Glides cleanly through both air and water (0.96 water inertia), leaving a crimson blood-mist and snowflake wake,
 * dealing 4.0 projectile/magic damage, Slowness I (60t), and +80 freeze ticks on impact.
 */
public class BloodFrostDartEntity extends ThrowableProjectile {

    public static final int MAX_LIFETIME_TICKS = 80;
    public static final float DART_DAMAGE = 4.0F;
    private static final DustParticleOptions CRIMSON_DART_DUST = new DustParticleOptions(0xD4223E, 0.7F);

    public BloodFrostDartEntity(EntityType<? extends BloodFrostDartEntity> type, Level level) {
        super(type, level);
    }

    public BloodFrostDartEntity(Level level, LivingEntity shooter) {
        super(ModEntityTypes.BLOOD_FROST_DART.get(), shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ(), level);
        this.setOwner(shooter);
    }

    public BloodFrostDartEntity(Level level, double x, double y, double z) {
        super(ModEntityTypes.BLOOD_FROST_DART.get(), x, y, z, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected double getDefaultGravity() {
        return 0.008D;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (target instanceof SanguineSeaAngelEntity) {
            return false;
        }
        return super.canHitEntity(target);
    }

    @Override
    public void tick() {
        // Compensate ThrowableProjectile's hardcoded 0.80 water drag so net underwater inertia is 0.96F
        if (this.isInWater()) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.96D / 0.80D));
        }

        super.tick();

        if (this.level().isClientSide()) {
            this.level().addParticle(
                    CRIMSON_DART_DUST,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.08D,
                    this.getY() + 0.10D + (this.random.nextDouble() - 0.5D) * 0.08D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.08D,
                    0.0D,
                    0.0D,
                    0.0D
            );
            if ((this.tickCount & 1) == 0) {
                this.level().addParticle(
                        ParticleTypes.SNOWFLAKE,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.10D,
                        this.getY() + 0.10D + (this.random.nextDouble() - 0.5D) * 0.10D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.10D,
                        0.0D,
                        -0.008D,
                        0.0D
                );
            }
        } else if (this.tickCount >= MAX_LIFETIME_TICKS) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity hit = result.getEntity();
        if (hit instanceof SanguineSeaAngelEntity) {
            return;
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            if (hit instanceof LivingEntity living) {
                Entity owner = this.getOwner();
                DamageSource source = owner instanceof LivingEntity livingOwner
                        ? this.damageSources().mobProjectile(this, livingOwner)
                        : this.damageSources().magic();
                boolean damaged = living.hurtServer(serverLevel, source, DART_DAMAGE);
                if (!damaged) {
                    living.hurtServer(serverLevel, this.damageSources().indirectMagic(this, owner), DART_DAMAGE);
                }
                living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 0), this.getEffectSource());
                if (living.canFreeze()) {
                    living.setTicksFrozen(Math.min(living.getTicksRequiredToFreeze() + 40, living.getTicksFrozen() + 80));
                }
            }

            Vec3 loc = result.getLocation();
            serverLevel.sendParticles(CRIMSON_DART_DUST, loc.x, loc.y, loc.z, 10, 0.18D, 0.18D, 0.18D, 0.02D);
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, loc.x, loc.y, loc.z, 8, 0.18D, 0.18D, 0.18D, 0.04D);
            serverLevel.playSound(null, loc.x, loc.y, loc.z, SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 0.5F, 1.6F);
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level() instanceof ServerLevel serverLevel) {
            Vec3 loc = result.getLocation();
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, loc.x, loc.y, loc.z, 10, 0.16D, 0.16D, 0.16D, 0.05D);
            serverLevel.sendParticles(CRIMSON_DART_DUST, loc.x, loc.y, loc.z, 10, 0.16D, 0.16D, 0.16D, 0.02D);
            serverLevel.playSound(null, loc.x, loc.y, loc.z, SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 0.5F, 1.6F);
            this.discard();
        }
    }
}
