package ddraig.net.entropica.block.biome14;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Wall Amber Torch — wall-mounted variant of the Amber Torch.
 * Shares warm snow-and-ice melting behavior with the standing Amber Torch.
 */
public class WallAmberTorchBlock extends WallTorchBlock {

    public WallAmberTorchBlock(Properties properties) {
        super(ParticleTypes.FLAME, properties);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        AmberTorchBlock.meltNearbySnowAndIce(level, pos);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(5) == 0) {
            Direction opposite = state.getValue(FACING).getOpposite();
            double x = pos.getX() + 0.5D + 0.27D * opposite.getStepX();
            double y = pos.getY() + 0.92D;
            double z = pos.getZ() + 0.5D + 0.27D * opposite.getStepZ();
            level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, 0.01D, 0.0D);
        }
    }
}
