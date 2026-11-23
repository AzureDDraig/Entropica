package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Auroral Lily-Pad — warm geothermal water lily of the Auroral Skerries.
 * Emits soft light (11) and melts nearby surface ice within a 3-block horizontal radius.
 */
public class AuroralLilyPadBlock extends WaterlilyBlock {
    public static final MapCodec<AuroralLilyPadBlock> CODEC = simpleCodec(AuroralLilyPadBlock::new);
    protected static final VoxelShape AABB = box(0.0, 0.0, 0.0, 16.0, 1.5, 16.0);
    public static final int WARMTH_RADIUS = 3;

    public AuroralLilyPadBlock(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapCodec<WaterlilyBlock> codec() {
        return (MapCodec<WaterlilyBlock>) (MapCodec<?>) CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AABB;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, 10);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        meltIceBelow(level, pos);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        meltIceBelow(level, pos);
    }

    private static void meltIceBelow(ServerLevel level, BlockPos lilyPos) {
        BlockPos waterCenter = lilyPos.below();
        for (int dx = -WARMTH_RADIUS; dx <= WARMTH_RADIUS; dx++) {
            for (int dz = -WARMTH_RADIUS; dz <= WARMTH_RADIUS; dz++) {
                if (dx * dx + dz * dz > WARMTH_RADIUS * WARMTH_RADIUS) {
                    continue;
                }
                BlockPos targetPos = waterCenter.offset(dx, 0, dz);
                BlockState targetState = level.getBlockState(targetPos);
                if (targetState.is(Blocks.ICE) || targetState.is(Blocks.FROSTED_ICE)) {
                    if (level.dimensionType().ultraWarm()) {
                        level.removeBlock(targetPos, false);
                    } else {
                        level.setBlockAndUpdate(targetPos, Blocks.WATER.defaultBlockState());
                        level.neighborChanged(targetPos, Blocks.WATER, null);
                    }
                }
            }
        }
    }

    /**
     * Checks whether an Auroral Lily-Pad is within {@code radius} blocks horizontally
     * (at {@code pos} or {@code pos.above()}), keeping surface channels unfrozen.
     */
    public static boolean preventsFreezingNear(LevelReader level, BlockPos pos, int radius) {
        if (!ModBlocks.AURORAL_LILY_PAD.isBound()) {
            return false;
        }
        int rSq = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > rSq) {
                    continue;
                }
                for (int dy = 0; dy <= 1; dy++) {
                    BlockPos checkPos = pos.offset(dx, dy, dz);
                    if (level.getBlockState(checkPos).is(ModBlocks.AURORAL_LILY_PAD.get())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(5) == 0) {
            double x = pos.getX() + 0.2D + random.nextDouble() * 0.6D;
            double y = pos.getY() + 0.12D + random.nextDouble() * 0.15D;
            double z = pos.getZ() + 0.2D + random.nextDouble() * 0.6D;
            if (random.nextBoolean()) {
                level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, 0.015D, 0.0D);
            } else {
                level.addParticle(ParticleTypes.GLOW, x, y, z, 0.0D, 0.01D, 0.0D);
            }
        }
    }
}
