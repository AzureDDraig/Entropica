package ddraig.net.entropica.worldgen.feature;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Generates compressed, flat-topped table-like icebergs ({@code radius 5..9}, {@code height 4..8}
 * above {@code Y=62} sea level, extending {@code 3..5+} blocks below water for solid underwater footing)
 * in Biome #15 ({@code 2.15 Dust-Crusted Bergs}):
 * <ul>
 *   <li>Core &amp; lower sheer cliffs: {@link Blocks#BLUE_ICE} &amp; {@link Blocks#PACKED_ICE}.</li>
 *   <li>Upper cliff rim ({@code topY - 1} sub-cap and outer ring of {@code topY}):
 *       {@code entropica:dust_crusted_ice}.</li>
 *   <li>Flat table top ({@code topY} interior): {@code 1} solid cap of {@code entropica:grey_sand}
 *       (supported underneath by solid {@code dust_crusted_ice} so {@code grey_sand} NEVER falls!)
 *       accented with {@code entropica:coarse_gravel} scree pockets and cleared of snow layers above.</li>
 *   <li>Scatters {@code 1-4} {@code entropica:wind_blown_dust_scrub} shrubs strictly on
 *       {@code grey_sand} or {@code dust_crusted_ice} on the flat plateau.</li>
 *   <li>Occasionally ({@code 1/3} bergs) anchors {@code 1} weathered {@link Blocks#MUD_BRICKS}
 *       bollard on the cliff rim with a {@code entropica:tough_fiber_rope} hanging down the sheer
 *       iceberg wall to water level ({@code waterlogged = true} where submerged).</li>
 * </ul>
 */
public class DustTableIcebergFeature extends Feature<NoneFeatureConfiguration> {

    private static final ResourceLocation DUST_CRUSTED_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "dust_crusted_ice");
    private static final ResourceLocation GREY_SAND_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "grey_sand");
    private static final ResourceLocation COARSE_GRAVEL_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "coarse_gravel");
    private static final ResourceLocation WIND_BLOWN_DUST_SCRUB_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "wind_blown_dust_scrub");
    private static final ResourceLocation TOUGH_FIBER_ROPE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "tough_fiber_rope");

    public DustTableIcebergFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        Block dustCrustedIceBlock = BuiltInRegistries.BLOCK.getOptional(DUST_CRUSTED_ICE_ID).orElse(Blocks.PACKED_ICE);
        Block greySandBlock = BuiltInRegistries.BLOCK.getOptional(GREY_SAND_ID).orElse(Blocks.SAND);
        Block coarseGravelBlock = BuiltInRegistries.BLOCK.getOptional(COARSE_GRAVEL_ID).orElse(Blocks.GRAVEL);
        Optional<Block> scrubOpt = BuiltInRegistries.BLOCK.getOptional(WIND_BLOWN_DUST_SCRUB_ID);
        Optional<Block> ropeOpt = BuiltInRegistries.BLOCK.getOptional(TOUGH_FIBER_ROPE_ID);

        int seaLevel = level.getSeaLevel() > 0 ? level.getSeaLevel() : 63;
        int waterTopY = seaLevel - 1; // Y = 62 in standard Overworld

        BlockPos anchorPos = findValidCoastalOrWaterAnchor(
                level, origin, waterTopY, dustCrustedIceBlock, greySandBlock, coarseGravelBlock);
        if (anchorPos == null) {
            return false;
        }

        int cx = anchorPos.getX();
        int cz = anchorPos.getZ();

        // Avoid overlapping an already-generated watch-pylon or tall berg
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int checkY = waterTopY + 7; checkY <= waterTopY + 15; checkY += 4) {
            cursor.set(cx, checkY, cz);
            if (!level.getBlockState(cursor).isAir()) {
                return false;
            }
        }
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            cursor.set(cx + dir.getStepX() * 5, waterTopY + 4, cz + dir.getStepZ() * 5);
            BlockState nearState = level.getBlockState(cursor);
            if (nearState.is(Blocks.MUD_BRICKS) || nearState.is(Blocks.TUFF_BRICKS)) {
                return false;
            }
        }

        int baseRadius = 5 + random.nextInt(5); // 5..9
        int tableHeight = 4 + random.nextInt(5); // 4..8 above Y=62 -> topY = 66..70
        int keelDepth = 3 + random.nextInt(3); // 3..5 blocks below Y=62 -> bottomY = 57..59
        int topY = Math.min(level.getMaxY() - 6, waterTopY + tableHeight);
        int bottomY = Math.max(level.getMinY() + 4, waterTopY - keelDepth);
        if (topY - waterTopY < 4) {
            return false;
        }

        double stretchX = 0.86D + random.nextDouble() * 0.28D;
        double stretchZ = 0.86D + random.nextDouble() * 0.28D;
        double phase = random.nextDouble() * (Math.PI * 2.0D);
        double screeAngle = random.nextDouble() * (Math.PI * 2.0D);
        double screeDirX = Math.cos(screeAngle);
        double screeDirZ = Math.sin(screeAngle);

        BlockState blueIce = Blocks.BLUE_ICE.defaultBlockState();
        BlockState packedIce = Blocks.PACKED_ICE.defaultBlockState();
        BlockState dustCrustedIce = dustCrustedIceBlock.defaultBlockState();
        BlockState greySand = greySandBlock.defaultBlockState();
        BlockState coarseGravel = coarseGravelBlock.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        int maxExtent = baseRadius + 2;
        int gridSize = maxExtent * 2 + 1;
        boolean[][] inTable = new boolean[gridSize][gridSize];

        for (int dx = -maxExtent; dx <= maxExtent; dx++) {
            for (int dz = -maxExtent; dz <= maxExtent; dz++) {
                double angle = Math.atan2(dz, dx);
                double facetFactor = 0.92D
                        + 0.09D * Math.cos(angle * 4.0D + phase)
                        + 0.05D * Math.sin(angle * 6.0D - phase);
                double localRadius = baseRadius * facetFactor;
                double anisoDist = Math.hypot(dx * stretchX, dz * stretchZ);
                if (anisoDist <= localRadius) {
                    inTable[dx + maxExtent][dz + maxExtent] = true;
                }
            }
        }

        List<BlockPos> plateauFloraCandidates = new ArrayList<>();
        int placedColumns = 0;

        for (int dx = -maxExtent; dx <= maxExtent; dx++) {
            for (int dz = -maxExtent; dz <= maxExtent; dz++) {
                if (!inTable[dx + maxExtent][dz + maxExtent]) {
                    continue;
                }

                int bx = cx + dx;
                int bz = cz + dz;

                boolean isRimCell = false;
                for (Direction horiz : Direction.Plane.HORIZONTAL) {
                    int nx = dx + horiz.getStepX();
                    int nz = dz + horiz.getStepZ();
                    if (Math.abs(nx) > maxExtent || Math.abs(nz) > maxExtent
                            || !inTable[nx + maxExtent][nz + maxExtent]) {
                        isRimCell = true;
                        break;
                    }
                }

                // 1. Core & lower sheer cliffs (bottomY .. topY - 2)
                for (int wy = bottomY; wy <= topY - 2; wy++) {
                    cursor.set(bx, wy, bz);
                    if (!canReplaceWithBerg(level.getBlockState(cursor))) {
                        continue;
                    }
                    boolean blueCore = !isRimCell && (wy <= waterTopY + 1 || ((dx * 3 + dz * 5 + wy) & 3) == 0)
                            || (wy <= waterTopY && ((dx + dz + wy) & 1) == 0);
                    level.setBlock(cursor, blueCore ? blueIce : packedIce, 2);
                }

                // Extend solid underwater footing down to shallow seabed if close beneath bottomY
                int maxFootingDrop = Math.max(level.getMinY() + 2, bottomY - 5);
                for (int wy = bottomY - 1; wy >= maxFootingDrop; wy--) {
                    cursor.set(bx, wy, bz);
                    BlockState belowState = level.getBlockState(cursor);
                    if (belowState.isAir()
                            || belowState.is(Blocks.WATER)
                            || belowState.getFluidState().getType() == Fluids.WATER
                            || belowState.is(Blocks.ICE)
                            || belowState.canBeReplaced()) {
                        level.setBlock(cursor, isRimCell ? packedIce : blueIce, 2);
                    } else {
                        break;
                    }
                }

                // 2. Upper cliff rim sub-cap (topY - 1): 100% solid dust_crusted_ice so grey_sand NEVER falls!
                cursor.set(bx, topY - 1, bz);
                if (canReplaceWithBerg(level.getBlockState(cursor))) {
                    level.setBlock(cursor, dustCrustedIce, 2);
                } else {
                    continue;
                }

                // 3. Flat table top (topY): dust_crusted_ice outer ring + grey_sand & coarse_gravel interior cap
                cursor.set(bx, topY, bz);
                if (!canReplaceWithBerg(level.getBlockState(cursor))) {
                    continue;
                }

                if (isRimCell) {
                    level.setBlock(cursor, dustCrustedIce, 2);
                    plateauFloraCandidates.add(cursor.immutable());
                } else {
                    double leeProj = dx * screeDirX + dz * screeDirZ;
                    boolean screePocket = (leeProj > baseRadius * 0.35D && random.nextFloat() < 0.42F)
                            || random.nextFloat() < 0.14F;
                    if (screePocket) {
                        level.setBlock(cursor, coarseGravel, 2);
                    } else {
                        level.setBlock(cursor, greySand, 2);
                        plateauFloraCandidates.add(cursor.immutable());
                    }
                }
                placedColumns++;

                // Clear any snow layers above the flat table top (topY + 1 .. topY + 2)
                for (int clearDy = 1; clearDy <= 2; clearDy++) {
                    cursor.set(bx, topY + clearDy, bz);
                    BlockState aboveState = level.getBlockState(cursor);
                    if (aboveState.is(Blocks.SNOW) || aboveState.is(Blocks.POWDER_SNOW)) {
                        level.setBlock(cursor, air, 2);
                    }
                }
            }
        }

        if (placedColumns < 16) {
            return false;
        }

        // 4. Occasionally (1/3 bergs) anchor 1 weathered mud_bricks bollard on the cliff rim with a
        //    hanging entropica:tough_fiber_rope down the sheer iceberg wall to water level!
        if (ropeOpt.isPresent() && random.nextInt(3) == 0) {
            tryPlaceRimBollardAndBoardingRope(
                    level, random, cx, cz, topY, waterTopY, maxExtent, inTable, ropeOpt.get());
        }

        // 5. Scatter 1-4 entropica:wind_blown_dust_scrub shrubs STRICTLY on grey_sand or dust_crusted_ice
        if (scrubOpt.isPresent() && !plateauFloraCandidates.isEmpty()) {
            Block scrubBlock = scrubOpt.get();
            BlockState scrubState = nonWaterlogged(scrubBlock.defaultBlockState());
            int targetShrubs = 1 + random.nextInt(4); // 1..4
            int placedShrubs = 0;

            while (!plateauFloraCandidates.isEmpty() && placedShrubs < targetShrubs) {
                int idx = random.nextInt(plateauFloraCandidates.size());
                BlockPos groundPos = plateauFloraCandidates.remove(idx);
                BlockState groundState = level.getBlockState(groundPos);
                if (!groundState.is(greySandBlock) && !groundState.is(dustCrustedIceBlock)) {
                    continue;
                }
                BlockPos plantPos = groundPos.above();
                if (!level.getBlockState(plantPos).isAir()) {
                    continue;
                }
                if (scrubState.canSurvive(level, plantPos)) {
                    level.setBlock(plantPos, scrubState, 2);
                    placedShrubs++;
                }
            }
        }

        return true;
    }

    private static void tryPlaceRimBollardAndBoardingRope(
            WorldGenLevel level,
            RandomSource random,
            int cx,
            int cz,
            int topY,
            int waterTopY,
            int maxExtent,
            boolean[][] inTable,
            Block ropeBlock
    ) {
        Direction[] dirs = {Direction.SOUTH, Direction.EAST, Direction.NORTH, Direction.WEST};
        int startIdx = random.nextInt(dirs.length);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int d = 0; d < dirs.length; d++) {
            Direction outDir = dirs[(startIdx + d) % dirs.length];
            Direction sideDir = outDir.getClockWise();

            // Find the outermost table cell along the centerline of outDir
            int rimDx = 0;
            int rimDz = 0;
            boolean foundRim = false;
            for (int dist = maxExtent; dist >= 2; dist--) {
                int tx = outDir.getStepX() * dist;
                int tz = outDir.getStepZ() * dist;
                if (inTable[tx + maxExtent][tz + maxExtent]) {
                    int outX = tx + outDir.getStepX();
                    int outZ = tz + outDir.getStepZ();
                    boolean outsideClear = Math.abs(outX) > maxExtent
                            || Math.abs(outZ) > maxExtent
                            || !inTable[outX + maxExtent][outZ + maxExtent];
                    int pierDx = tx + sideDir.getStepX();
                    int pierDz = tz + sideDir.getStepZ();
                    boolean hasSidePierSpot = Math.abs(pierDx) <= maxExtent
                            && Math.abs(pierDz) <= maxExtent
                            && inTable[pierDx + maxExtent][pierDz + maxExtent];
                    if (outsideClear && hasSidePierSpot) {
                        rimDx = tx;
                        rimDz = tz;
                        foundRim = true;
                        break;
                    }
                }
            }

            if (!foundRim) {
                continue;
            }

            int rimWx = cx + rimDx;
            int rimWz = cz + rimDz;
            int ropeWx = rimWx + outDir.getStepX();
            int ropeWz = rimWz + outDir.getStepZ();
            int pierWx = rimWx + sideDir.getStepX();
            int pierWz = rimWz + sideDir.getStepZ();

            // Verify water or open coastal drop outside the sheer wall at waterTopY
            cursor.set(ropeWx, waterTopY, ropeWz);
            BlockState waterCheck = level.getBlockState(cursor);
            boolean hasWaterBelow = waterCheck.is(Blocks.WATER)
                    || waterCheck.getFluidState().getType() == Fluids.WATER
                    || waterCheck.is(Blocks.ICE)
                    || waterCheck.isAir();
            if (!hasWaterBelow) {
                continue;
            }

            BlockState mudBricks = Blocks.MUD_BRICKS.defaultBlockState();
            // Solid mud_bricks threshold & side bollard pier + overhanging davit arm at topY + 3
            // Leaves (rimWx, topY + 1..2, rimWz) completely open so players climb right onto the plateau!
            level.setBlock(new BlockPos(rimWx, topY, rimWz), mudBricks, 2);
            level.setBlock(new BlockPos(pierWx, topY, pierWz), mudBricks, 2);
            for (int h = 1; h <= 3; h++) {
                level.setBlock(new BlockPos(pierWx, topY + h, pierWz), mudBricks, 2);
            }
            level.setBlock(new BlockPos(rimWx, topY + 3, rimWz), mudBricks, 2);
            level.setBlock(new BlockPos(ropeWx, topY + 3, ropeWz), mudBricks, 2);

            // Hang tough_fiber_rope top-down from topY + 2 down to waterTopY (waterlogged where submerged!)
            for (int ry = topY + 2; ry >= waterTopY; ry--) {
                cursor.set(ropeWx, ry, ropeWz);
                BlockState cur = level.getBlockState(cursor);
                boolean inWater = cur.is(Blocks.WATER) || cur.getFluidState().getType() == Fluids.WATER;
                if (!cur.isAir() && !inWater && !cur.is(Blocks.ICE) && !cur.is(Blocks.SNOW) && !cur.canBeReplaced()) {
                    break;
                }
                BlockState ropeState = ropeState(ropeBlock, inWater);
                level.setBlock(cursor, ropeState, 2);
            }
            return;
        }
    }

    private static BlockPos findValidCoastalOrWaterAnchor(
            WorldGenLevel level,
            BlockPos origin,
            int waterTopY,
            Block dustCrustedIceBlock,
            Block greySandBlock,
            Block coarseGravelBlock
    ) {
        BlockPos.MutableBlockPos check = new BlockPos.MutableBlockPos();
        for (int dy = 4; dy >= -5; dy--) {
            int y = origin.getY() + dy;
            if (y < waterTopY - 2 || y > waterTopY + 5) {
                continue;
            }
            if (y <= level.getMinY() + 12 || y >= level.getMaxY() - 24) {
                continue;
            }
            check.set(origin.getX(), y, origin.getZ());
            BlockState state = level.getBlockState(check);
            BlockState above = level.getBlockState(check.above());
            if (!above.isAir() && !above.is(Blocks.SNOW)) {
                continue;
            }

            boolean isWater = (y >= waterTopY - 1 && y <= waterTopY + 1)
                    && (state.is(Blocks.WATER) || state.getFluidState().getType() == Fluids.WATER);
            boolean isBergShelf = state.is(Blocks.ICE)
                    || state.is(Blocks.PACKED_ICE)
                    || state.is(Blocks.BLUE_ICE)
                    || state.is(dustCrustedIceBlock)
                    || state.is(greySandBlock)
                    || state.is(coarseGravelBlock)
                    || state.is(Blocks.GRAVEL)
                    || state.is(Blocks.SAND)
                    || state.is(Blocks.TUFF);

            if (isWater || isBergShelf) {
                return check.immutable();
            }
        }
        return null;
    }

    private static boolean canReplaceWithBerg(BlockState state) {
        return state.isAir()
                || state.is(Blocks.WATER)
                || state.is(Blocks.BUBBLE_COLUMN)
                || state.is(Blocks.ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.SNOW)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.POWDER_SNOW)
                || state.is(Blocks.SAND)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.TUFF)
                || state.canBeReplaced();
    }

    private static BlockState ropeState(Block ropeBlock, boolean waterlogged) {
        BlockState state = ropeBlock.defaultBlockState();
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, waterlogged);
        }
        return state;
    }

    private static BlockState nonWaterlogged(BlockState state) {
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            return state.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        return state;
    }
}
