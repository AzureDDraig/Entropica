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

import java.util.List;

/**
 * Generates projecting horizontal cantilevered shelves and stepped terraces of compressed
 * {@link Blocks#BLUE_ICE} and {@link Blocks#PACKED_ICE} (4-9 blocks wide, 2-4 blocks thick)
 * jutting out from sheer coastal ice cliffs (Y=63..90) over freezing ocean waters, with
 * {@link ModBlocks#GLACIAL_HEART_ROOT} hanging beneath sheltered shelf ceilings.
 */
public class BlueGlacierIceShelfFeature extends Feature<NoneFeatureConfiguration> {

    private static final int MIN_SHELF_Y = 63;
    private static final int MAX_SHELF_Y = 90;
    private static final int[] Y_SEARCH_OFFSETS = {0, -2, 2, -4, 4, -6, 6, -8, 8};

    public BlueGlacierIceShelfFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        int baseSearchY = Mth.clamp(origin.getY(), MIN_SHELF_Y, MAX_SHELF_Y);
        List<Direction> horizontalDirs = Direction.Plane.HORIZONTAL.shuffledCopy(random);

        BlockPos anchorPos = null;
        Direction outDir = null;

        outer:
        for (int yOff : Y_SEARCH_OFFSETS) {
            int testY = baseSearchY + yOff;
            if (testY < MIN_SHELF_Y || testY > MAX_SHELF_Y) {
                continue;
            }
            for (int r = 0; r <= 3; r++) {
                int ox = r == 0 ? 0 : (random.nextInt(r * 2 + 1) - r);
                int oz = r == 0 ? 0 : (random.nextInt(r * 2 + 1) - r);
                BlockPos candidate = new BlockPos(origin.getX() + ox, testY, origin.getZ() + oz);
                if (!isAnchorCliffBlock(level.getBlockState(candidate))) {
                    continue;
                }
                for (Direction dir : horizontalDirs) {
                    if (hasCoastalOrCliffDropOutward(level, candidate, dir)) {
                        anchorPos = candidate;
                        outDir = dir;
                        break outer;
                    }
                }
            }
        }

        if (anchorPos == null || outDir == null) {
            return false;
        }

        Direction lateralDir = outDir.getClockWise();
        int width = 4 + random.nextInt(6); // 4..9 blocks wide
        int halfLeft = width / 2;
        int halfRight = width - 1 - halfLeft;
        int maxProjection = 4 + random.nextInt(4); // 4..7 blocks projecting outward (staying well inside 3x3 chunk write area)
        int rootThickness = 2 + random.nextInt(3); // 2..4 blocks thick

        BlockState blueIce = Blocks.BLUE_ICE.defaultBlockState();
        BlockState packedIce = Blocks.PACKED_ICE.defaultBlockState();
        BlockState heartRoot = ModBlocks.GLACIAL_HEART_ROOT.get().defaultBlockState();

        boolean placedAny = false;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int lat = -halfLeft; lat <= halfRight; lat++) {
            float centerOffset = lat - (-halfLeft + halfRight) * 0.5F;
            float normLat = width <= 1 ? 0.0F : centerOffset / (width * 0.5F + 0.4F);
            float ellipse = (float) Math.sqrt(Math.max(0.2F, 1.0F - normLat * normLat * 0.82F));
            int colProjection = Math.max(2, Math.round(maxProjection * ellipse) - (random.nextInt(3) == 0 ? 1 : 0));

            // Anchor 1 block inside the cliff wall (jut = -1) so the shelf cantilevers seamlessly
            for (int jut = -1; jut <= colProjection; jut++) {
                int bx = anchorPos.getX() + lateralDir.getStepX() * lat + outDir.getStepX() * jut;
                int bz = anchorPos.getZ() + lateralDir.getStepZ() * lat + outDir.getStepZ() * jut;

                int localThickness;
                if (jut <= 1) {
                    localThickness = rootThickness;
                } else {
                    float t = (jut - 1) / (float) Math.max(1, colProjection - 1);
                    localThickness = Math.max(1, Math.round(Mth.lerp(t, rootThickness, 1.35F)));
                }

                int lowestPlacedY = Integer.MAX_VALUE;
                for (int d = 0; d < localThickness; d++) {
                    int by = anchorPos.getY() - d;
                    if (by < MIN_SHELF_Y - 1) {
                        break;
                    }
                    cursor.set(bx, by, bz);
                    BlockState existing = level.getBlockState(cursor);
                    if (!canShelfReplace(existing)) {
                        continue;
                    }

                    boolean isTopOrLip = (d == 0) || (jut >= colProjection - 1);
                    float blueIceChance = isTopOrLip ? 0.78F : 0.58F;
                    BlockState toPlace = random.nextFloat() < blueIceChance ? blueIce : packedIce;
                    level.setBlock(cursor, toPlace, 2);
                    placedAny = true;
                    if (by < lowestPlacedY) {
                        lowestPlacedY = by;
                    }
                }

                // Hang Glacial Heart-Root beneath overhanging shelf ceiling blocks
                if (jut >= 1 && lowestPlacedY != Integer.MAX_VALUE && lowestPlacedY - 1 >= MIN_SHELF_Y) {
                    cursor.set(bx, lowestPlacedY - 1, bz);
                    if (level.isEmptyBlock(cursor) && random.nextFloat() < 0.22F) {
                        if (heartRoot.canSurvive(level, cursor)) {
                            level.setBlock(cursor, heartRoot, 2);
                        }
                    }
                }
            }
        }

        return placedAny;
    }

    private static boolean hasCoastalOrCliffDropOutward(WorldGenLevel level, BlockPos wallPos, Direction outDir) {
        BlockPos front1 = wallPos.relative(outDir, 1);
        BlockPos front2 = wallPos.relative(outDir, 2);
        if (!isOpenAirOrSnow(level.getBlockState(front1)) || !isOpenAirOrSnow(level.getBlockState(front2))) {
            return false;
        }

        BlockPos probe = wallPos.relative(outDir, 3);
        if (!isOpenAirOrSnow(level.getBlockState(probe))) {
            return false;
        }

        // Verify a sheer drop toward lower cliff ledges or freezing ocean water below
        BlockPos.MutableBlockPos down = probe.mutable();
        int airDepth = 0;
        for (int step = 1; step <= 28 && down.getY() >= 60; step++) {
            down.move(Direction.DOWN);
            BlockState below = level.getBlockState(down);
            if (below.is(Blocks.WATER) || (down.getY() <= 64 && (below.is(Blocks.ICE) || below.is(Blocks.BLUE_ICE) || below.is(Blocks.PACKED_ICE)))) {
                return true;
            }
            if (isOpenAirOrSnow(below)) {
                airDepth++;
            } else {
                return airDepth >= 3;
            }
        }
        return airDepth >= 3;
    }

    private static boolean isAnchorCliffBlock(BlockState state) {
        return state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.ICE)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(BlockTags.BASE_STONE_OVERWORLD);
    }

    private static boolean isOpenAirOrSnow(BlockState state) {
        return state.isAir() || state.is(Blocks.SNOW) || state.is(Blocks.POWDER_SNOW);
    }

    private static boolean canShelfReplace(BlockState state) {
        return state.isAir()
                || state.is(Blocks.SNOW)
                || state.is(Blocks.POWDER_SNOW)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.WATER)
                || state.is(Blocks.ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.GRAVEL)
                || state.is(BlockTags.DIRT)
                || state.is(BlockTags.BASE_STONE_OVERWORLD);
    }
}
