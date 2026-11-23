package ddraig.net.entropica.block.biome15;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ColorRGBA;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Grey Sand — weathered basalt-ash silt and glacial rock flour drifted across 2.15 Dust-Crusted Bergs.
 * Smelts into Dust Glass, compacts into Tuff with a Dust-Core Shard, and yields mineral nuggets in a Copper Silt-Sieve.
 */
public class GreySandBlock extends ColoredFallingBlock {
    public static final MapCodec<GreySandBlock> CODEC = simpleCodec(GreySandBlock::new);
    public static final ColorRGBA DUST_COLOR = new ColorRGBA(0x8C929BFF);

    public GreySandBlock(Properties properties) {
        super(DUST_COLOR, properties);
    }

    @Override
    public MapCodec<? extends ColoredFallingBlock> codec() {
        return CODEC;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(40) != 0) {
            return;
        }
        BlockPos above = pos.above();
        if (!level.getBlockState(above).isAir() || !level.canSeeSky(above)) {
            return;
        }

        double x = pos.getX() + 0.1D + random.nextDouble() * 0.8D;
        double y = pos.getY() + 1.03D;
        double z = pos.getZ() + 0.1D + random.nextDouble() * 0.8D;
        level.addParticle(ParticleTypes.ASH, x, y, z, 0.02D, 0.006D, 0.006D);
    }
}
