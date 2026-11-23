package ddraig.net.entropica.block.biome12;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Frozen Spore Leaves — frost-rimmed bioluminescent canopy foliage of Spore-Wood trees.
 * Never decays when placed in structures or features and gently sheds drifting spore motes.
 */
public class FrozenSporeLeavesBlock extends Block {
    public static final MapCodec<FrozenSporeLeavesBlock> CODEC = simpleCodec(FrozenSporeLeavesBlock::new);

    public FrozenSporeLeavesBlock(Properties properties) {
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
        level.addParticle(ParticleTypes.SPORE_BLOSSOM_AIR, x, y, z, 0.0D, -0.02D, 0.0D);
    }
}
