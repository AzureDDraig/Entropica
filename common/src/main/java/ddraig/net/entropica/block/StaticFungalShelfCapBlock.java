package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.nbt.CompoundTag;

public class StaticFungalShelfCapBlock extends AbstractFungalShelfBlock {
    public static final MapCodec<StaticFungalShelfCapBlock> CODEC = simpleCodec(StaticFungalShelfCapBlock::new);

    public StaticFungalShelfCapBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<StaticFungalShelfCapBlock> codec() {
        return CODEC;
    }

    @Override
    protected void applyTouchEffect(Level level, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity living && !level.isClientSide()) {
            long now = level.getGameTime();
            CompoundTag nbt = EntityHelper.getPersistentData(living);

            long lastLightning = nbt.getLong("entropica_static_shelf_lightning_time").orElse(0L);
            if (now - lastLightning < 100) {
                return;
            }

            long lastTouch = nbt.getLong("entropica_static_shelf_last_touch_time").orElse(0L);

            if (now - lastTouch >= 5) {
                int touchCount = nbt.getInt("entropica_static_shelf_touch_count").orElse(0) + 1;

                nbt.putLong("entropica_static_shelf_last_touch_time", now);
                nbt.putInt("entropica_static_shelf_touch_count", touchCount);

                living.hurt(level.damageSources().lightningBolt(), 1.0f);
                living.addEffect(new MobEffectInstance(MobEffects.SPEED, 60, 1, false, true, true));

                if (touchCount >= 3) {
                    nbt.putInt("entropica_static_shelf_touch_count", 0);
                    nbt.putLong("entropica_static_shelf_lightning_time", now);

                    if (level instanceof ServerLevel serverLevel) {
                        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel, EntitySpawnReason.TRIGGERED);
                        if (bolt != null) {
                            bolt.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                            serverLevel.addFreshEntity(bolt);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(2) == 0) {
            double x = pos.getX() + 0.2 + random.nextDouble() * 0.6;
            double y = pos.getY() + 0.4 + random.nextDouble() * 0.2;
            double z = pos.getZ() + 0.2 + random.nextDouble() * 0.6;
            level.addParticle(new TimedTintableParticleOption(ModParticles.TINTABLE_ARC.get(), 0.22f, 0.74f, 0.97f, 15, 1.2f, (float)(random.nextDouble()*6.28), 0.2f, TimedTintableParticleOption.MODE_JITTER, 0.05f, 0f, 0f), x, y, z, 0.0, 0.0, 0.0);
        }
    }
}
