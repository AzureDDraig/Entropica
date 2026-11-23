package ddraig.net.entropica.block.biome14;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Frost-Willow Leaves — frost-dusted golden-sage willow foliage of 2.14 Oasis Floes.
 * Never decays unexpectedly when placed in worldgen features and gently sheds snowflakes and warm amber motes.
 */
public class FrostWillowLeavesBlock extends Block {
    public static final MapCodec<FrostWillowLeavesBlock> CODEC = simpleCodec(FrostWillowLeavesBlock::new);

    public FrostWillowLeavesBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(14) != 0) {
            return;
        }
        BlockPos below = pos.below();
        if (level.getBlockState(below).isCollisionShapeFullBlock(level, below)) {
            return;
        }

        double x = pos.getX() + random.nextDouble();
        double y = pos.getY() - 0.05D;
        double z = pos.getZ() + random.nextDouble();
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.DRIPPING_HONEY, x, y, z, 0.0D, 0.0D, 0.0D);
        } else if (random.nextBoolean()) {
            level.addParticle(ParticleTypes.SNOWFLAKE, x, y, z, 0.0D, -0.02D, 0.0D);
        } else {
            level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, -0.015D, 0.0D);
        }
    }
}
