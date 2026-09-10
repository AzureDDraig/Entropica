package ddraig.net.entropica.entity.projectile;

import ddraig.net.entropica.gravity.GravityApi;
import ddraig.net.entropica.gravity.GravityField;
import ddraig.net.entropica.gravity.GravityFieldManager;
import ddraig.net.entropica.registry.ModEntityTypes;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SingularityGrenadeEntity extends ThrowableItemProjectile {

    private int vortexDuration = 0;
    private boolean isAnchored = false;
    private ResourceLocation fieldId;

    public SingularityGrenadeEntity(EntityType<? extends SingularityGrenadeEntity> type, Level level) {
        super(type, level);
        this.fieldId = ResourceLocation.fromNamespaceAndPath("entropica", "singularity_" + this.getId());
    }

    public SingularityGrenadeEntity(Level level, LivingEntity shooter) {
        super(ModEntityTypes.SINGULARITY_GRENADE.get(), shooter, level, new ItemStack(ModItems.SINGULARITY_GRENADE.get()));
        this.fieldId = ResourceLocation.fromNamespaceAndPath("entropica", "singularity_" + this.getId());
    }

    public SingularityGrenadeEntity(Level level, double x, double y, double z) {
        super(ModEntityTypes.SINGULARITY_GRENADE.get(), x, y, z, level, new ItemStack(ModItems.SINGULARITY_GRENADE.get()));
        this.fieldId = ResourceLocation.fromNamespaceAndPath("entropica", "singularity_" + this.getId());
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.SINGULARITY_GRENADE.get();
    }

    private void anchorVortex() {
        if (!isAnchored) {
            this.isAnchored = true;
            this.setNoGravity(true);
            this.setDeltaMovement(Vec3.ZERO);

            if (!this.level().isClientSide()) {
                this.fieldId = ResourceLocation.fromNamespaceAndPath("entropica", "singularity_" + this.getUUID());
                GravityField field = new GravityField(fieldId, this.level().dimension(), this.blockPosition(), 12.0, GravityField.Mode.SINGULARITY, 85);
                GravityFieldManager.registerField(field);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 1.2F, 0.5F);
            }
        }
    }

    @Override
    public void tick() {
        if (isAnchored) {
            this.setDeltaMovement(Vec3.ZERO);
            this.vortexDuration++;

            if (this.level().isClientSide()) {
                // Swirling accretion vortex particles
                for (int i = 0; i < 6; i++) {
                    double angle = (this.vortexDuration * 0.3) + (i * Math.PI / 3.0);
                    double radius = 1.0 + (this.random.nextDouble() * 2.0);
                    double px = this.getX() + Math.cos(angle) * radius;
                    double pz = this.getZ() + Math.sin(angle) * radius;
                    double py = this.getY() + (this.random.nextDouble() - 0.5) * 1.5;

                    // Inward vector towards vortex core
                    double vx = (this.getX() - px) * 0.2;
                    double vy = (this.getY() - py) * 0.2;
                    double vz = (this.getZ() - pz) * 0.2;

                    this.level().addParticle(ParticleTypes.PORTAL, px, py, pz, vx, vy, vz);
                    this.level().addParticle(ParticleTypes.REVERSE_PORTAL, px, py, pz, vx, vy, vz);
                }
            } else {
                // 4 seconds (80 ticks) accretion duration
                if (this.vortexDuration >= 80) {
                    collapseSingularity();
                }
            }
        } else {
            super.tick();
            if (this.level().isClientSide()) {
                this.level().addParticle(ParticleTypes.PORTAL, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    private void collapseSingularity() {
        if (!this.level().isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) this.level();

            // Implosive shockwave
            serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 2.0F, 0.8F);

            List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0));
            for (LivingEntity target : targets) {
                target.hurtServer(serverLevel, this.damageSources().explosion(this, getOwner()), 16.0F);
                // Outward shockwave fling from collapse (immune if anchored)
                if (!GravityApi.isAnchored(target)) {
                    Vec3 away = target.position().subtract(this.position()).normalize().scale(1.2);
                    target.setDeltaMovement(away.x, 0.4, away.z);
                    target.hasImpulse = true;
                }
            }

            if (fieldId != null) {
                GravityFieldManager.unregisterField(fieldId);
            }
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        anchorVortex();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        anchorVortex();
    }

    @Override
    public void remove(RemovalReason reason) {
        if (fieldId != null) {
            GravityFieldManager.unregisterField(fieldId);
        }
        super.remove(reason);
    }
}