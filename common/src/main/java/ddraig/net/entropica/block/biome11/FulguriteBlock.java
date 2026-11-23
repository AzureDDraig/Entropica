package ddraig.net.entropica.block.biome11;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Fulgurite Block — lightning-fused storm-gravel rock of 2.11 Storm-Swept Icebergs.
 * Emits a soft static glow (Light Level 3) and a constant weak Redstone signal (Level 6).
 */
public class FulguriteBlock extends Block {
    public static final MapCodec<FulguriteBlock> CODEC = simpleCodec(FulguriteBlock::new);

    public FulguriteBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 6;
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

        double x = pos.getX() + 0.15D + random.nextDouble() * 0.7D;
        double y = pos.getY() + 1.03D;
        double z = pos.getZ() + 0.15D + random.nextDouble() * 0.7D;
        double vx = (random.nextDouble() - 0.5D) * 0.03D;
        double vy = 0.015D + random.nextDouble() * 0.02D;
        double vz = (random.nextDouble() - 0.5D) * 0.03D;
        level.addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z, vx, vy, vz);
    }
}
