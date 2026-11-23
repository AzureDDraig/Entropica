package ddraig.net.entropica.block.biome11;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Zephyr Ice — buoyant sky-crystal ice infused with Storm and Glacial Materia.
 * Never melts in sunlight or near heat sources and emits gentle upward wind wisps.
 */
public class ZephyrIceBlock extends HalfTransparentBlock {
    public static final MapCodec<ZephyrIceBlock> CODEC = simpleCodec(ZephyrIceBlock::new);

    public ZephyrIceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends HalfTransparentBlock> codec() {
        return CODEC;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(10) != 0) {
            return;
        }
        if (!level.getBlockState(pos.above()).isAir()) {
            return;
        }

        double x = pos.getX() + 0.15D + random.nextDouble() * 0.7D;
        double y = pos.getY() + 1.04D;
        double z = pos.getZ() + 0.15D + random.nextDouble() * 0.7D;
        double vx = (random.nextDouble() - 0.5D) * 0.01D;
        double vz = (random.nextDouble() - 0.5D) * 0.01D;
        level.addParticle(ParticleTypes.CLOUD, x, y, z, vx, 0.015D, vz);
    }
}
