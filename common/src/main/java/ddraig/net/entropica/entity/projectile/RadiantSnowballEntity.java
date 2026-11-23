package ddraig.net.entropica.entity.projectile;

import ddraig.net.entropica.registry.ModEntityTypes;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Radiant Snowball — sun-warmed pearl-gold snowball infused with Radiant and Vitae Materia.
 * Trails End Rod motes in flight, clears frost, sears undead/monsters with radiant damage + Glowing,
 * and grants brief Regeneration to friendly targets.
 */
public class RadiantSnowballEntity extends ThrowableItemProjectile {

    public RadiantSnowballEntity(EntityType<? extends RadiantSnowballEntity> type, Level level) {
        super(type, level);
    }

    public RadiantSnowballEntity(Level level, LivingEntity shooter) {
        super(ModEntityTypes.RADIANT_SNOWBALL.get(), shooter, level, new ItemStack(ModItems.RADIANT_SNOWBALL.get()));
    }

    public RadiantSnowballEntity(Level level, LivingEntity shooter, ItemStack stack) {
        super(ModEntityTypes.RADIANT_SNOWBALL.get(), shooter, level, stack);
    }

    public RadiantSnowballEntity(Level level, double x, double y, double z) {
        super(ModEntityTypes.RADIANT_SNOWBALL.get(), x, y, z, level, new ItemStack(ModItems.RADIANT_SNOWBALL.get()));
    }

    public RadiantSnowballEntity(Level level, double x, double y, double z, ItemStack stack) {
        super(ModEntityTypes.RADIANT_SNOWBALL.get(), x, y, z, level, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.RADIANT_SNOWBALL.get();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.level().addParticle(ParticleTypes.END_ROD,
                    this.getX(), this.getY(), this.getZ(),
                    0.0D, -0.01D, 0.0D);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Entity hit = result.getEntity();
        if (hit instanceof LivingEntity target) {
            target.setTicksFrozen(0);
            if (target.getType().is(EntityTypeTags.UNDEAD) || target instanceof Monster || target instanceof Enemy) {
                target.hurtServer(serverLevel, serverLevel.damageSources().thrown(this, this.getOwner()), 4.0F);
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 160, 0), this.getOwner());
            } else {
                target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0), this.getOwner());
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel serverLevel) {
            double x = this.getX();
            double y = this.getY();
            double z = this.getZ();

            serverLevel.playSound(null, x, y, z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 0.85F, 1.3F);
            serverLevel.playSound(null, x, y, z, SoundEvents.SNOW_BREAK, SoundSource.NEUTRAL, 0.9F, 1.05F);

            serverLevel.sendParticles(ParticleTypes.END_ROD, x, y, z, 16, 0.45D, 0.45D, 0.45D, 0.06D);
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, x, y, z, 14, 0.4D, 0.4D, 0.4D, 0.04D);

            BlockPos center = BlockPos.containing(result.getLocation());
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos checkPos = center.offset(dx, dy, dz);
                        BlockState checkState = serverLevel.getBlockState(checkPos);
                        if (checkState.is(Blocks.SNOW)) {
                            serverLevel.removeBlock(checkPos, false);
                        } else if (checkState.is(Blocks.FROSTED_ICE)) {
                            serverLevel.setBlockAndUpdate(checkPos, Blocks.WATER.defaultBlockState());
                        }
                    }
                }
            }

            this.discard();
        }
    }
}
