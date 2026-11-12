package ddraig.net.entropica.entity;

import ddraig.net.entropica.registry.ModEntityTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class MagmaThornEntity extends ThrowableItemProjectile {
    public MagmaThornEntity(EntityType<? extends MagmaThornEntity> type, Level level) {
        super(type, level);
    }

    public MagmaThornEntity(Level level, LivingEntity shooter) {
        super(ModEntityTypes.MAGMA_THORN.get(), shooter, level, new ItemStack(Items.FIRE_CHARGE));
    }

    public MagmaThornEntity(Level level, double x, double y, double z) {
        super(ModEntityTypes.MAGMA_THORN.get(), x, y, z, level, new ItemStack(Items.FIRE_CHARGE));
    }

    @Override
    protected Item getDefaultItem() {
        return Items.FIRE_CHARGE;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide() && result.getEntity() instanceof LivingEntity living) {
            living.hurt(this.damageSources().inFire(), 1.5f);
            living.igniteForSeconds(3);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 0.8f, 1.5f);
            this.discard();
        }
    }
}
