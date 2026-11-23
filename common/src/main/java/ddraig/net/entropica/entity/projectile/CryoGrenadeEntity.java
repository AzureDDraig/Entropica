package ddraig.net.entropica.entity.projectile;

import ddraig.net.entropica.registry.ModEntityTypes;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

import java.util.List;

/**
 * Cryo-Grenade — thrown Glacial flash-freeze canister from 2.5 Silent Fjord.
 * Trails snowflakes in flight and shatters on impact to freeze surface water, extinguish flames,
 * and blast nearby creatures with frost damage, Slowness III, and deep freeze buildup.
 */
public class CryoGrenadeEntity extends ThrowableItemProjectile {

    public CryoGrenadeEntity(EntityType<? extends CryoGrenadeEntity> type, Level level) {
        super(type, level);
    }

    public CryoGrenadeEntity(Level level, LivingEntity shooter) {
        super(ModEntityTypes.CRYO_GRENADE.get(), shooter, level, new ItemStack(ModItems.CRYO_GRENADE.get()));
    }

    public CryoGrenadeEntity(Level level, LivingEntity shooter, ItemStack stack) {
        super(ModEntityTypes.CRYO_GRENADE.get(), shooter, level, stack);
    }

    public CryoGrenadeEntity(Level level, double x, double y, double z) {
        super(ModEntityTypes.CRYO_GRENADE.get(), x, y, z, level, new ItemStack(ModItems.CRYO_GRENADE.get()));
    }

    public CryoGrenadeEntity(Level level, double x, double y, double z, ItemStack stack) {
        super(ModEntityTypes.CRYO_GRENADE.get(), x, y, z, level, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.CRYO_GRENADE.get();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.level().addParticle(ParticleTypes.SNOWFLAKE,
                    this.getX(), this.getY(), this.getZ(),
                    0.0D, -0.01D, 0.0D);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel serverLevel) {
            double x = this.getX();
            double y = this.getY();
            double z = this.getZ();

            serverLevel.playSound(null, x, y, z, SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 1.0F, 1.15F);
            serverLevel.playSound(null, x, y, z, SoundEvents.PLAYER_HURT_FREEZE, SoundSource.NEUTRAL, 1.0F, 1.0F);

            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, x, y, z, 28, 1.2D, 0.8D, 1.2D, 0.08D);
            serverLevel.sendParticles(ParticleTypes.CLOUD, x, y, z, 16, 1.0D, 0.5D, 1.0D, 0.04D);

            BlockPos center = BlockPos.containing(result.getLocation());
            for (int dx = -3; dx <= 3; dx++) {
                for (int dy = -3; dy <= 3; dy++) {
                    for (int dz = -3; dz <= 3; dz++) {
                        if (dx * dx + dy * dy + dz * dz > 9) {
                            continue;
                        }
                        BlockPos targetPos = center.offset(dx, dy, dz);
                        BlockState targetState = serverLevel.getBlockState(targetPos);
                        if (targetState.is(Blocks.WATER)
                                && targetState.getFluidState().isSource()
                                && serverLevel.getBlockState(targetPos.above()).isAir()) {
                            serverLevel.setBlockAndUpdate(targetPos, Blocks.ICE.defaultBlockState());
                        } else if (targetState.getBlock() instanceof BaseFireBlock) {
                            serverLevel.removeBlock(targetPos, false);
                        }
                    }
                }
            }

            List<LivingEntity> targets = serverLevel.getEntitiesOfClass(
                    LivingEntity.class,
                    this.getBoundingBox().inflate(3.5D),
                    LivingEntity::isAlive
            );
            for (LivingEntity target : targets) {
                if (target.isOnFire()) {
                    target.clearFire();
                }
                target.hurtServer(serverLevel, serverLevel.damageSources().freeze(), 3.0F);
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 2), this.getOwner());
                int maxFreeze = target.getTicksRequiredToFreeze() + 140;
                target.setTicksFrozen(Math.min(maxFreeze, target.getTicksFrozen() + 140));
            }

            this.discard();
        }
    }
}
