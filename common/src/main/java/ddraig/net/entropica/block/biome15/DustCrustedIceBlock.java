package ddraig.net.entropica.block.biome15;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Dust-Crusted Ice — gritty basalt-silt and windblown sand sintered into glacial ice bergs
 * in 2.15 Dust-Crusted Bergs. Provides firm traction (0.96F friction), never melts in light,
 * and breaks into Grey Sand unless harvested with Silk Touch.
 */
public class DustCrustedIceBlock extends Block {
    public static final MapCodec<DustCrustedIceBlock> CODEC = simpleCodec(DustCrustedIceBlock::new);

    public DustCrustedIceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(36) != 0) {
            return;
        }
        BlockPos above = pos.above();
        if (!level.getBlockState(above).isAir() || !level.canSeeSky(above)) {
            return;
        }

        double x = pos.getX() + 0.1D + random.nextDouble() * 0.8D;
        double y = pos.getY() + 1.03D;
        double z = pos.getZ() + 0.1D + random.nextDouble() * 0.8D;
        level.addParticle(ParticleTypes.ASH, x, y, z, 0.015D, 0.005D, 0.005D);
    }
}
