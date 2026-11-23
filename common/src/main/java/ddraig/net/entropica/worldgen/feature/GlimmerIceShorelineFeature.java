package ddraig.net.entropica.worldgen.feature;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Places sinuous coastal ribbons and low {@code 1-2} block glowing {@link ModBlocks#GLIMMER_ICE}
 * shoreline shelves along water edges ({@code Y=62..65}) in {@code 2.2 Auroral Skerries}.
 */
public class GlimmerIceShorelineFeature extends Feature<NoneFeatureConfiguration> {

    private static final int MIN_SHORE_Y = 62;
    private static final int MAX_SHORE_Y = 65;

    public GlimmerIceShorelineFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        BlockPos startPos = null;
        Direction waterDir = null;

        // Find a coastal edge block (Y=62..64) bordering open channel water or surface ice
        search:
        for (int dy = -2; dy <= 2; dy++) {
            int testY = Mth.clamp(origin.getY() + dy, MIN_SHORE_Y, MAX_SHORE_Y - 1);
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos candidate = new BlockPos(origin.getX() + dx, testY, origin.getZ() + dz);
                    if (!isShorelineLandBlock(level.getBlockState(candidate))) {
                        continue;
                    }
                    for (Direction dir : Direction.Plane.HORIZONTAL.shuffledCopy(random)) {
                        BlockPos neighbor = candidate.relative(dir);
                        BlockPos neighborWaterCheck = new BlockPos(neighbor.getX(), MIN_SHORE_Y, neighbor.getZ());
                        if (isChannelWaterOrSurfaceIce(level, neighborWaterCheck)) {
                            startPos = candidate;
                            waterDir = dir;
                            break search;
                        }
                    }
                }
            }
        }

        if (startPos == null || waterDir == null) {
            return false;
        }

        BlockState glimmerIce = ModBlocks.GLIMMER_ICE.get().defaultBlockState();
        BlockState coarseGravel = ModBlocks.COARSE_GRAVEL.get().defaultBlockState();

        Direction tangent = random.nextBoolean() ? waterDir.getClockWise() : waterDir.getCounterClockWise();
        int ribbonLength = 5 + random.nextInt(5); // 5..9 steps along the shoreline
        double curX = startPos.getX();
        double curZ = startPos.getZ();
        double angle = Math.atan2(tangent.getStepZ(), tangent.getStepX());

        boolean placedAny = false;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int step = 0; step < ribbonLength; step++) {
            int bx = Mth.floor(curX + 0.5D);
            int bz = Mth.floor(curZ + 0.5D);
            if (Math.abs(bx - origin.getX()) > 7 || Math.abs(bz - origin.getZ()) > 7) {
                break;
            }

            // Determine local shoreline Y in [62..64]
            int baseShoreY = MIN_SHORE_Y;
            for (int y = MAX_SHORE_Y - 1; y >= MIN_SHORE_Y; y--) {
                cursor.set(bx, y, bz);
                if (isShorelineLandBlock(level.getBlockState(cursor)) || isChannelWaterOrSurfaceIce(level, cursor)) {
                    baseShoreY = y;
                    break;
                }
            }

            int shelfHeight = (step > 0 && step < ribbonLength - 1 && random.nextFloat() < 0.55F) ? 2 : 1;
            for (int h = 0; h < shelfHeight; h++) {
                int py = Mth.clamp(baseShoreY + h, MIN_SHORE_Y, MAX_SHORE_Y);
                cursor.set(bx, py, bz);
                if (canShoreShelfReplace(level.getBlockState(cursor))) {
                    level.setBlock(cursor, glimmerIce, 2);
                    placedAny = true;
                }
            }

            // Extend a low 1-block glowing Glimmer-Ice lip onto the adjacent water edge at Y=62..63
            int jutX = bx + waterDir.getStepX();
            int jutZ = bz + waterDir.getStepZ();
            if (Math.abs(jutX - origin.getX()) <= 7 && Math.abs(jutZ - origin.getZ()) <= 7) {
                cursor.set(jutX, MIN_SHORE_Y, jutZ);
                if (canShoreShelfReplace(level.getBlockState(cursor)) && random.nextFloat() < 0.80F) {
                    level.setBlock(cursor, glimmerIce, 2);
                    placedAny = true;
                    if (shelfHeight == 2 && random.nextFloat() < 0.35F) {
                        cursor.set(jutX, MIN_SHORE_Y + 1, jutZ);
                        if (level.getBlockState(cursor).isAir() || level.getBlockState(cursor).is(Blocks.SNOW)) {
                            level.setBlock(cursor, glimmerIce, 2);
                        }
                    }
                }
            }

            // Blend landward edge with Coarse Gravel where exposed
            int landX = bx - waterDir.getStepX();
            int landZ = bz - waterDir.getStepZ();
            if (Math.abs(landX - origin.getX()) <= 7 && Math.abs(landZ - origin.getZ()) <= 7 && random.nextFloat() < 0.45F) {
                cursor.set(landX, MIN_SHORE_Y, landZ);
                BlockState landState = level.getBlockState(cursor);
                if (landState.is(Blocks.GRAVEL) || landState.is(Blocks.SAND) || landState.is(Blocks.DIRT)) {
                    level.setBlock(cursor, coarseGravel, 2);
                }
            }

            // Gently curve along coastline
            angle += (random.nextDouble() - 0.5D) * 0.45D;
            curX += Math.cos(angle);
            curZ += Math.sin(angle);
        }

        return placedAny;
    }

    private static boolean isShorelineLandBlock(BlockState state) {
        return state.is(ModBlocks.COARSE_GRAVEL.get())
                || state.is(ModBlocks.GLIMMER_ICE.get())
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.SAND)
                || state.is(Blocks.BASALT)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.ICE)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(BlockTags.DIRT)
                || state.is(BlockTags.BASE_STONE_OVERWORLD);
    }

    private static boolean isChannelWaterOrSurfaceIce(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.WATER)) {
            return level.getBlockState(pos.above()).isAir() || level.getBlockState(pos.above()).is(Blocks.SNOW);
        }
        return state.is(Blocks.ICE) && level.getBlockState(pos.below()).is(Blocks.WATER);
    }

    private static boolean canShoreShelfReplace(BlockState state) {
        return state.isAir()
                || state.is(Blocks.SNOW)
                || state.is(Blocks.POWDER_SNOW)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.WATER)
                || state.is(Blocks.ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.GRAVEL)
                || state.is(ModBlocks.COARSE_GRAVEL.get())
                || state.is(ModBlocks.GLIMMER_ICE.get())
                || state.is(BlockTags.DIRT);
    }
}
