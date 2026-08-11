package ddraig.net.entropica.entity;

import ddraig.net.entropica.registry.ModEntityTypes;
import ddraig.net.entropica.registry.ModItems;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class SporeProjectileEntity extends ThrowableItemProjectile {
    public SporeProjectileEntity(EntityType<? extends SporeProjectileEntity> type, Level level) {
        super(type, level);
    }

    public SporeProjectileEntity(Level level, LivingEntity shooter) {
        super(ModEntityTypes.SPORE_PROJECTILE.get(), shooter, level, new ItemStack(ModItems.ACIDIC_SPORE_NECTAR.get()));
    }

    public SporeProjectileEntity(Level level, double x, double y, double z) {
        super(ModEntityTypes.SPORE_PROJECTILE.get(), x, y, z, level, new ItemStack(ModItems.ACIDIC_SPORE_NECTAR.get()));
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.ACIDIC_SPORE_NECTAR.get();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.level().addParticle(ParticleTypes.SPORE_BLOSSOM_AIR, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide() && result.getEntity() instanceof LivingEntity living) {
            living.hurt(this.damageSources().magic(), 2.0f);
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1, false, true, true));
            living.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 80, 0, false, true, true));
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 1.0f, 1.2f);
            this.discard();
        }
    }
}
