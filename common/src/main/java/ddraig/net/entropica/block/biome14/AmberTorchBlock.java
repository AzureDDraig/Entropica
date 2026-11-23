package ddraig.net.entropica.block.biome14;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Amber Torch — warm resinous torch crafted from Amber Chunks in 2.14 Oasis Floes.
 * Emits warm golden light (14) and actively melts nearby snow layers and ice into water.
 */
public class AmberTorchBlock extends TorchBlock {
    public static final MapCodec<AmberTorchBlock> CODEC = simpleCodec(AmberTorchBlock::new);

    public AmberTorchBlock(Properties properties) {
        super(ParticleTypes.FLAME, properties);
    }

    @Override
    public MapCodec<? extends TorchBlock> codec() {
        return CODEC;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        meltNearbySnowAndIce(level, pos);
    }

    public static void meltNearbySnowAndIce(ServerLevel level, BlockPos origin) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        boolean ultraWarm = level.dimensionType().ultraWarm();
        for (int dy = -2; dy <= 2; dy++) {
            int y = origin.getY() + dy;
            if (y < level.getMinY() || y >= level.getMaxY()) {
                continue;
            }
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    cursor.set(origin.getX() + dx, y, origin.getZ() + dz);
                    if (!level.hasChunkAt(cursor)) {
                        continue;
                    }
                    BlockState target = level.getBlockState(cursor);
                    if (target.is(Blocks.SNOW)) {
                        level.removeBlock(cursor, false);
                    } else if (target.is(Blocks.ICE)) {
                        BlockPos targetPos = cursor.immutable();
                        if (ultraWarm) {
                            level.removeBlock(targetPos, false);
                        } else {
                            level.setBlockAndUpdate(targetPos, Blocks.WATER.defaultBlockState());
                            level.neighborChanged(targetPos, Blocks.WATER, null);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(5) == 0) {
            level.addParticle(ParticleTypes.END_ROD,
                    pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.1D,
                    pos.getY() + 0.72D,
                    pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.1D,
                    0.0D, 0.01D, 0.0D);
        }
    }
}
