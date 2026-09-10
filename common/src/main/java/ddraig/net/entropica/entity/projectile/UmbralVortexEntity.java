package ddraig.net.entropica.entity.projectile;

import ddraig.net.entropica.registry.ModEntityTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class UmbralVortexEntity extends ThrowableItemProjectile {

    private int vortexDuration = 0;
    private boolean isAnchored = false;

    public UmbralVortexEntity(EntityType<? extends UmbralVortexEntity> type, Level level) {
        super(type, level);
    }

    public UmbralVortexEntity(Level level, LivingEntity shooter) {
        super(ModEntityTypes.UMBRAL_VORTEX.get(), shooter, level, new ItemStack(Items.PRISMARINE_CRYSTALS));
    }

    public UmbralVortexEntity(Level level, double x, double y, double z) {
        super(ModEntityTypes.UMBRAL_VORTEX.get(), x, y, z, level, new ItemStack(Items.PRISMARINE_CRYSTALS));
    }

    @Override
    protected Item getDefaultItem() {
        return Items.PRISMARINE_CRYSTALS;
    }

    @Override
    public void tick() {
        if (isAnchored) {
            this.setDeltaMovement(Vec3.ZERO);
            this.vortexDuration++;

            if (this.level().isClientSide()) {
                // Swirling portal & bubble particles
                for (int i = 0; i < 4; i++) {
                    double angle = (this.vortexDuration * 0.4) + (i * Math.PI * 0.5);
                    double radius = 1.5;
                    double px = this.getX() + Math.cos(angle) * radius;
                    double pz = this.getZ() + Math.sin(angle) * radius;
                    this.level().addParticle(ParticleTypes.PORTAL, px, this.getY() + 0.2, pz, (this.getX() - px) * 0.1, 0.05, (this.getZ() - pz) * 0.1);
                    this.level().addParticle(ParticleTypes.SPLASH, px, this.getY() + 0.1, pz, 0, 0.05, 0);
                }
            } else {
                // Suction & Drenching effect
                List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(3.5));
                for (LivingEntity target : nearby) {
                    if (target != this.getOwner()) {
                        Vec3 pull = this.position().subtract(target.position()).normalize().scale(0.12);
                        target.setDeltaMovement(target.getDeltaMovement().add(pull));
                        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 2));
                        target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0));
                        target.hurt(this.damageSources().magic(), 1.0F);
                    }
                }

                if (this.vortexDuration > 60) { // 3 seconds vortex duration
                    this.discard();
                }
            }
        } else {
            super.tick();
            if (this.level().isClientSide()) {
                this.level().addParticle(ParticleTypes.FALLING_WATER, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
                this.level().addParticle(ParticleTypes.WITCH, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!this.level().isClientSide() && !isAnchored) {
            this.isAnchored = true;
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 1.0F, 0.5F);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide() && !isAnchored) {
            this.isAnchored = true;
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SPLASH_POTION_BREAK, SoundSource.PLAYERS, 1.0F, 0.6F);
        }
    }
}
