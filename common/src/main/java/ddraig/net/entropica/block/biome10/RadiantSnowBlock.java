package ddraig.net.entropica.block.biome10;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Radiant Snow Block — warm pearl-gold sunlit snow of 2.10 Sun-Glazed Sanctuary.
 * Clears freeze buildup on living creatures walking across it and emits gentle golden motes.
 */
public class RadiantSnowBlock extends Block {
    public static final MapCodec<RadiantSnowBlock> CODEC = simpleCodec(RadiantSnowBlock::new);

    public RadiantSnowBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (entity instanceof LivingEntity living && living.getTicksFrozen() > 0) {
            living.setTicksFrozen(0);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(8) != 0) {
            return;
        }
        BlockPos above = pos.above();
        if (level.getBlockState(above).isCollisionShapeFullBlock(level, above)) {
            return;
        }

        double x = pos.getX() + 0.1D + random.nextDouble() * 0.8D;
        double y = pos.getY() + 1.05D;
        double z = pos.getZ() + 0.1D + random.nextDouble() * 0.8D;

        if (random.nextBoolean() || !ModParticles.SPECTRUM_SPARKLE.isBound()) {
            level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, 0.015D, 0.0D);
        } else {
            level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), x, y, z, 1.0F, 0.86F, 0.32F);
        }
    }
}
