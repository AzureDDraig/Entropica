package ddraig.net.entropica.block.biome12;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Spore-Glazed Ice — translucent pack-ice suspended with golden-lime Spore and Glacial Materia motes.
 * Never melts in light and emits subtle spore sparkles.
 */
public class SporeGlazedIceBlock extends HalfTransparentBlock {
    public static final MapCodec<SporeGlazedIceBlock> CODEC = simpleCodec(SporeGlazedIceBlock::new);

    public SporeGlazedIceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends HalfTransparentBlock> codec() {
        return CODEC;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(24) != 0) {
            return;
        }
        BlockPos above = pos.above();
        if (level.getBlockState(above).isCollisionShapeFullBlock(level, above)) {
            return;
        }

        double x = pos.getX() + 0.15D + random.nextDouble() * 0.7D;
        double y = pos.getY() + 1.04D;
        double z = pos.getZ() + 0.15D + random.nextDouble() * 0.7D;
        level.addParticle(ParticleTypes.SPORE_BLOSSOM_AIR, x, y, z, 0.0D, 0.01D, 0.0D);
    }
}
