package ddraig.net.entropica.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SoulFlameOrchidBlock extends BushBlock {
    public SoulFlameOrchidBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.SOUL_SOIL)
            || state.is(Blocks.SOUL_SAND)
            || state.is(Blocks.NETHERRACK)
            || state.is(Blocks.OBSIDIAN)
            || state.is(Blocks.CRYING_OBSIDIAN)
            || state.is(Blocks.BASALT)
            || state.is(Blocks.POLISHED_BASALT)
            || state.is(Blocks.SMOOTH_BASALT);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(3) == 0) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
            double y = pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5;

            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 0.0, 0.03, 0.0);
            if (random.nextBoolean()) {
                level.addParticle(ParticleTypes.SOUL, x, y + 0.2, z, 0.0, 0.02, 0.0);
            }
        }
    }
}
