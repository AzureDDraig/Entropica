package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class StarlightCarpetBlock extends CarpetBlock {

    public static final MapCodec<StarlightCarpetBlock> CODEC = simpleCodec(StarlightCarpetBlock::new);

    public StarlightCarpetBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<StarlightCarpetBlock> codec() {
        return CODEC;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);

        if (entity instanceof LivingEntity living && living.onGround()) {
            // Apply a controlled Speed boost (Speed II: +40% speed) while on the carpet
            // without compounding velocity multiplication or runaway acceleration
            living.addEffect(new MobEffectInstance(
                    MobEffects.SPEED,
                    15, // 0.75 seconds duration, continuously refreshed while traversing the carpet
                    1,  // amplifier 1 = Speed II (+40% movement speed)
                    false, // ambient
                    false, // visible particles (clean, no potion swirls)
                    false  // show icon
            ));

            // Spawn celestial starlight sparkles underfoot while gliding
            if (level.isClientSide()) {
                Vec3 motion = living.getDeltaMovement();
                double horizontalSpeedSq = motion.x * motion.x + motion.z * motion.z;
                if (horizontalSpeedSq > 0.001) {
                    RandomSource random = level.getRandom();
                    if (random.nextFloat() < 0.45F) {
                        double px = living.getX() + (random.nextDouble() - 0.5) * 0.4;
                        double py = living.getY() + 0.06;
                        double pz = living.getZ() + (random.nextDouble() - 0.5) * 0.4;
                        level.addParticle(ddraig.net.entropica.registry.ModParticles.SPECTRUM_SPARKLE.get(), px, py, pz, 0.4f, 0.85f, 1.0f);
                    }
                }
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Gentle ambient starlight glint
        if (random.nextInt(8) == 0) {
            double px = pos.getX() + random.nextDouble();
            double py = pos.getY() + 0.07;
            double pz = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 0.0, 0.01, 0.0);
        }
    }
}
