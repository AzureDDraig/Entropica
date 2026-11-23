package ddraig.net.entropica.worldgen.feature;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates tight stepped clusters of {@code 3-7} hexagonal basalt columns ({@code 4-14} blocks tall)
 * along {@code 2.5 Silent Fjord} cliffs and dark water shorelines, anchored 3 blocks deep into solid
 * ground or seabed (zero floating pillars), capping flat dry column tops ({@code y >= 63}) with
 * {@code entropica:blue_frozen_moss} or {@code entropica:blue_frozen_moss_carpet} (40% chance) and
 * tucking {@code 1-2} {@code entropica:shadowed_eclipse_moss} patches into shaded base crevices.
 */
public class FjordBasaltColumnFeature extends Feature<NoneFeatureConfiguration> {

    private static final int SEA_WATER_TOP_Y = 62;

    private static final ResourceLocation BASALT_COLUMN_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "basalt_column");
    private static final ResourceLocation BLUE_FROZEN_MOSS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "blue_frozen_moss");
    private static final ResourceLocation BLUE_FROZEN_MOSS_CARPET_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "blue_frozen_moss_carpet");
    private static final ResourceLocation SHADOWED_ECLIPSE_MOSS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "shadowed_eclipse_moss");

    private static final int[][] HEX_NEIGHBORS_A = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, -1}, {-1, 1}
    };
    private static final int[][] HEX_NEIGHBORS_B = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, -1}
    };

    public FjordBasaltColumnFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        Block basaltColumnBlock = resolveBlock(BASALT_COLUMN_ID, Blocks.BASALT);
        Block blueFrozenMossBlock = resolveBlock(BLUE_FROZEN_MOSS_ID, Blocks.MOSS_BLOCK);
        Block blueFrozenMossCarpetBlock = resolveBlock(BLUE_FROZEN_MOSS_CARPET_ID, Blocks.MOSS_CARPET);
        Block shadowedEclipseMossBlock = resolveBlock(SHADOWED_ECLIPSE_MOSS_ID, Blocks.MOSS_CARPET);

        BlockPos anchorSurface = findAnchorGroundOrShoreline(level, origin, basaltColumnBlock, blueFrozenMossBlock);
        if (anchorSurface == null) {
            return false;
        }

        BlockState basaltColumnState = verticalPillarState(basaltColumnBlock);
        BlockState basaltState = Blocks.BASALT.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState smoothBasaltState = Blocks.SMOOTH_BASALT.defaultBlockState();
        BlockState blueFrozenMossState = blueFrozenMossBlock.defaultBlockState();
        BlockState blueFrozenMossCarpetState = blueFrozenMossCarpetBlock.defaultBlockState();
        BlockState shadowedEclipseMossState = shadowedEclipseMossBlock.defaultBlockState();

        int clusterSize = 3 + random.nextInt(5); // 3..7 columns
        int[][] hexRing = (random.nextBoolean() ? HEX_NEIGHBORS_A : HEX_NEIGHBORS_B).clone();
        shuffleOffsets(hexRing, random);

        List<int[]> selectedOffsets = new ArrayList<>(clusterSize);
        selectedOffsets.add(new int[]{0, 0});
        for (int i = 0; i < clusterSize - 1 && i < hexRing.length; i++) {
            selectedOffsets.add(hexRing[i]);
        }

        int centerHeight = 7 + random.nextInt(8); // 7..14 blocks tall for tallest spine
        int anchorX = anchorSurface.getX();
        int anchorY = anchorSurface.getY();
        int anchorZ = anchorSurface.getZ();

        List<ColumnTop> placedTops = new ArrayList<>(clusterSize);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int idx = 0; idx < selectedOffsets.size(); idx++) {
            int[] off = selectedOffsets.get(idx);
            int cx = anchorX + off[0];
            int cz = anchorZ + off[1];

            int colGroundY = findSolidColumnBaseY(level, cx, anchorY, cz, basaltColumnBlock, blueFrozenMossBlock);
            if (colGroundY == Integer.MIN_VALUE) {
                continue;
            }

            int colHeight;
            if (idx == 0) {
                colHeight = centerHeight;
            } else {
                int stepDown = 1 + idx + random.nextInt(3);
                colHeight = Mth.clamp(centerHeight - stepDown, 4, 14);
            }

            // Ensure submerged shoreline columns breach above the dark fjord water surface
            if (colGroundY <= SEA_WATER_TOP_Y) {
                int minBreachHeight = (SEA_WATER_TOP_Y + 1 + random.nextInt(3)) - colGroundY;
                if (minBreachHeight > 14) {
                    continue;
                }
                colHeight = Mth.clamp(Math.max(colHeight, minBreachHeight), 4, 14);
            }

            int colTopY = colGroundY + colHeight;
            if (colTopY + 2 >= level.getMaxY()) {
                continue;
            }

            // 1. Anchor 3 blocks deep into solid ground/seabed (plus seal any steep cliff pocket underneath)
            for (int d = 0; d < 3; d++) {
                int ay = colGroundY - d;
                if (ay <= level.getMinY() + 1) {
                    break;
                }
                cursor.set(cx, ay, cz);
                if (canColumnReplace(level.getBlockState(cursor), basaltColumnBlock, blueFrozenMossBlock)) {
                    level.setBlock(cursor, pickColumnState(random, basaltColumnState, basaltState, smoothBasaltState), 2);
                }
            }
            for (int extra = 3; extra <= 6; extra++) {
                int ay = colGroundY - extra;
                if (ay <= level.getMinY() + 1) {
                    break;
                }
                cursor.set(cx, ay, cz);
                BlockState belowState = level.getBlockState(cursor);
                if (isFluidOrReplaceableCover(belowState)) {
                    level.setBlock(cursor, basaltState, 2);
                } else {
                    break;
                }
            }

            // 2. Build vertical hexagonal basalt shaft from colGroundY + 1..colTopY
            for (int y = colGroundY + 1; y <= colTopY; y++) {
                cursor.set(cx, y, cz);
                if (canColumnReplace(level.getBlockState(cursor), basaltColumnBlock, blueFrozenMossBlock)) {
                    BlockState shaftState = (y == colTopY)
                            ? basaltColumnState
                            : pickColumnState(random, basaltColumnState, basaltState, smoothBasaltState);
                    level.setBlock(cursor, shaftState, 2);
                }
            }

            placedTops.add(new ColumnTop(cx, colTopY, cz, colHeight));
        }

        if (placedTops.isEmpty()) {
            return false;
        }

        // 3. Cap flat column tops above water (y >= 63) with blue_frozen_moss or blue_frozen_moss_carpet (40% chance)
        for (ColumnTop top : placedTops) {
            if (top.topY < SEA_WATER_TOP_Y + 1 || random.nextFloat() >= 0.40F) {
                continue;
            }
            BlockPos topPos = new BlockPos(top.x, top.topY, top.z);
            BlockPos abovePos = topPos.above();
            BlockState aboveState = level.getBlockState(abovePos);
            if (!aboveState.isAir() && !aboveState.is(Blocks.SNOW)) {
                continue;
            }

            if (top.height >= 5 && random.nextFloat() < 0.35F) {
                level.setBlock(topPos, blueFrozenMossState, 2);
                if (aboveState.is(Blocks.SNOW)) {
                    level.setBlock(abovePos, Blocks.AIR.defaultBlockState(), 2);
                }
            } else if (blueFrozenMossCarpetState.canSurvive(level, abovePos)) {
                level.setBlock(abovePos, blueFrozenMossCarpetState, 2);
            }
        }

        // 4. Tuck 1-2 shadowed_eclipse_moss patches into shaded (blockLight <= 4) base crevices
        int targetShadowMoss = 1 + random.nextInt(2); // 1..2
        int placedShadowMoss = 0;
        List<BlockPos> creviceCandidates = new ArrayList<>();

        for (ColumnTop col : placedTops) {
            for (Direction horiz : Direction.Plane.HORIZONTAL) {
                int nx = col.x + horiz.getStepX();
                int nz = col.z + horiz.getStepZ();
                if (containsColumnAt(placedTops, nx, nz)) {
                    continue;
                }
                int groundY = findDryLedgeNear(level, nx, anchorY, nz, basaltColumnBlock, blueFrozenMossBlock);
                if (groundY < SEA_WATER_TOP_Y || groundY >= col.topY) {
                    continue;
                }
                BlockPos mossPos = new BlockPos(nx, groundY + 1, nz);
                if (!creviceCandidates.contains(mossPos)) {
                    creviceCandidates.add(mossPos);
                }
            }
        }

        for (int i = creviceCandidates.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            BlockPos tmp = creviceCandidates.get(i);
            creviceCandidates.set(i, creviceCandidates.get(j));
            creviceCandidates.set(j, tmp);
        }

        for (BlockPos mossPos : creviceCandidates) {
            if (placedShadowMoss >= targetShadowMoss) {
                break;
            }
            BlockState existing = level.getBlockState(mossPos);
            if (!existing.isAir() && !existing.is(Blocks.SNOW)) {
                continue;
            }
            if (level.getBrightness(LightLayer.BLOCK, mossPos) <= 4
                    && shadowedEclipseMossState.canSurvive(level, mossPos)) {
                level.setBlock(mossPos, shadowedEclipseMossState, 2);
                placedShadowMoss++;
            }
        }

        return true;
    }

    private record ColumnTop(int x, int topY, int z, int height) {}

    private static boolean containsColumnAt(List<ColumnTop> cols, int x, int z) {
        for (ColumnTop c : cols) {
            if (c.x == x && c.z == z) {
                return true;
            }
        }
        return false;
    }

    private static BlockState pickColumnState(
            RandomSource random,
            BlockState basaltColumn,
            BlockState basalt,
            BlockState smoothBasalt
    ) {
        float roll = random.nextFloat();
        if (roll < 0.60F) {
            return basaltColumn;
        }
        if (roll < 0.85F) {
            return basalt;
        }
        return smoothBasalt;
    }

    private static BlockState verticalPillarState(Block block) {
        BlockState state = block.defaultBlockState();
        if (state.hasProperty(RotatedPillarBlock.AXIS)) {
            return state.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        }
        return state;
    }

    private static BlockPos findAnchorGroundOrShoreline(
            WorldGenLevel level,
            BlockPos origin,
            Block basaltColumnBlock,
            Block blueFrozenMossBlock
    ) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = 3; dy >= -8; dy--) {
            int y = origin.getY() + dy;
            if (y <= level.getMinY() + 5 || y >= level.getMaxY() - 18) {
                continue;
            }
            cursor.set(origin.getX(), y, origin.getZ());
            BlockState state = level.getBlockState(cursor);
            if (state.is(Blocks.LAVA)) {
                return null;
            }
            BlockState above = level.getBlockState(cursor.above());
            if (above.is(Blocks.LAVA)) {
                return null;
            }
            if (isSolidFjordGround(state, basaltColumnBlock, blueFrozenMossBlock) && isFluidOrReplaceableCover(above)) {
                if (y < 57) {
                    return null; // Reject deep channel floor far from shore
                }
                if (y < SEA_WATER_TOP_Y - 1 && !hasDryShoreWithinRadius(level, origin.getX(), origin.getZ(), 3, basaltColumnBlock, blueFrozenMossBlock)) {
                    return null;
                }
                return cursor.immutable();
            }
        }
        return null;
    }

    private static boolean hasDryShoreWithinRadius(
            WorldGenLevel level,
            int cx,
            int cz,
            int radius,
            Block basaltColumnBlock,
            Block blueFrozenMossBlock
    ) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                for (int y = SEA_WATER_TOP_Y; y <= SEA_WATER_TOP_Y + 5; y++) {
                    cursor.set(cx + dx, y, cz + dz);
                    BlockState s = level.getBlockState(cursor);
                    BlockState above = level.getBlockState(cursor.above());
                    if (isSolidFjordGround(s, basaltColumnBlock, blueFrozenMossBlock)
                            && !above.is(Blocks.WATER) && !above.is(Blocks.LAVA)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static int findSolidColumnBaseY(
            WorldGenLevel level,
            int x,
            int refY,
            int z,
            Block basaltColumnBlock,
            Block blueFrozenMossBlock
    ) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int maxY = Math.min(level.getMaxY() - 18, refY + 4);
        int minY = Math.max(level.getMinY() + 5, Math.max(55, refY - 8));
        for (int y = maxY; y >= minY; y--) {
            cursor.set(x, y, z);
            BlockState state = level.getBlockState(cursor);
            if (state.is(Blocks.LAVA)) {
                return Integer.MIN_VALUE;
            }
            BlockState above = level.getBlockState(cursor.above());
            if (isSolidFjordGround(state, basaltColumnBlock, blueFrozenMossBlock) && isFluidOrReplaceableCover(above)) {
                return y;
            }
        }
        return Integer.MIN_VALUE;
    }

    private static int findDryLedgeNear(
            WorldGenLevel level,
            int x,
            int refY,
            int z,
            Block basaltColumnBlock,
            Block blueFrozenMossBlock
    ) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int maxY = Math.min(level.getMaxY() - 4, refY + 4);
        int minY = Math.max(SEA_WATER_TOP_Y, refY - 4);
        for (int y = maxY; y >= minY; y--) {
            cursor.set(x, y, z);
            BlockState ground = level.getBlockState(cursor);
            BlockState above = level.getBlockState(cursor.above());
            if (isSolidFjordGround(ground, basaltColumnBlock, blueFrozenMossBlock)
                    && (above.isAir() || above.is(Blocks.SNOW))) {
                return y;
            }
        }
        return Integer.MIN_VALUE;
    }

    private static boolean isSolidFjordGround(BlockState state, Block basaltColumnBlock, Block blueFrozenMossBlock) {
        return state.is(Blocks.BASALT)
                || state.is(Blocks.SMOOTH_BASALT)
                || state.is(Blocks.POLISHED_BASALT)
                || state.is(basaltColumnBlock)
                || state.is(blueFrozenMossBlock)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(BlockTags.DIRT)
                || state.is(BlockTags.BASE_STONE_OVERWORLD);
    }

    private static boolean isFluidOrReplaceableCover(BlockState state) {
        return state.isAir()
                || state.is(Blocks.WATER)
                || state.is(Blocks.BUBBLE_COLUMN)
                || state.is(Blocks.ICE)
                || state.is(Blocks.SNOW)
                || state.is(Blocks.POWDER_SNOW)
                || state.canBeReplaced();
    }

    private static boolean canColumnReplace(BlockState state, Block basaltColumnBlock, Block blueFrozenMossBlock) {
        return isFluidOrReplaceableCover(state)
                || isSolidFjordGround(state, basaltColumnBlock, blueFrozenMossBlock);
    }

    private static void shuffleOffsets(int[][] array, RandomSource random) {
        for (int i = array.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int[] tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
        }
    }

    private static Block resolveBlock(ResourceLocation id, Block fallback) {
        return BuiltInRegistries.BLOCK.getOptional(id).orElse(fallback);
    }
}
