package ddraig.net.entropica.block.biome9;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Spatial Phase Anchor — 4-tier sculpted Void Materia obelisk that serves as a local
 * recall beacon for attuned Phase-Recall Stones within 128 blocks.
 */
public class SpatialPhaseAnchorBlock extends Block {
    public static final MapCodec<SpatialPhaseAnchorBlock> CODEC = simpleCodec(SpatialPhaseAnchorBlock::new);

    protected static final VoxelShape SHAPE = Shapes.or(
            Block.box(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D),
            Block.box(4.0D, 3.0D, 4.0D, 12.0D, 11.0D, 12.0D),
            Block.box(2.5D, 11.0D, 2.5D, 13.5D, 14.0D, 13.5D),
            Block.box(5.5D, 14.0D, 5.5D, 10.5D, 16.0D, 10.5D)
    );

    public SpatialPhaseAnchorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(2) == 0) {
            double x = pos.getX() + 0.25D + random.nextDouble() * 0.5D;
            double y = pos.getY() + 0.3D + random.nextDouble() * 0.75D;
            double z = pos.getZ() + 0.25D + random.nextDouble() * 0.5D;
            level.addParticle(ParticleTypes.REVERSE_PORTAL, x, y, z, 0.0D, 0.025D, 0.0D);
            int color = random.nextBoolean() ? 0x38BDF8 : 0xE879F9;
            level.addParticle(new DustParticleOptions(color, 0.8F),
                    pos.getX() + 0.35D + random.nextDouble() * 0.3D,
                    pos.getY() + 0.85D + random.nextDouble() * 0.25D,
                    pos.getZ() + 0.35D + random.nextDouble() * 0.3D,
                    0.0D, 0.015D, 0.0D);
        }
    }
}
