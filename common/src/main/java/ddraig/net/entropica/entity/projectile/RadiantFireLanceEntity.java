package ddraig.net.entropica.entity.projectile;

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

public class RadiantFireLanceEntity extends ThrowableItemProjectile {

    public RadiantFireLanceEntity(EntityType<? extends RadiantFireLanceEntity> type, Level level) {
        super(type, level);
    }

    public RadiantFireLanceEntity(Level level, LivingEntity shooter) {
        super(ModEntityTypes.RADIANT_FIRE_LANCE.get(), shooter, level, new ItemStack(Items.BLAZE_POWDER));
    }

    public RadiantFireLanceEntity(Level level, double x, double y, double z) {
        super(ModEntityTypes.RADIANT_FIRE_LANCE.get(), x, y, z, level, new ItemStack(Items.BLAZE_POWDER));
    }

    @Override
    protected Item getDefaultItem() {
        return Items.BLAZE_POWDER;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 0.0, 0.02, 0.0);
            this.level().addParticle(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 0.0, 0.01, 0.0);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            // Check for cavitation shock if target is already drenched/slowed
            float damage = 9.0F;
            if (target.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS) || target.isInWaterOrRain()) {
                damage = 18.0F; // Thermal-void steam-cavitation reaction bonus
                this.level().playSound(null, target.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 1.8F);
            }
            target.hurt(this.damageSources().mobAttack(this.getOwner() instanceof LivingEntity living ? living : null), damage);
            target.igniteForSeconds(5);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 1.2F);
            this.discard();
        }
    }
}
