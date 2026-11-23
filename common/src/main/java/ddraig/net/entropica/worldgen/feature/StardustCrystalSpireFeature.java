package ddraig.net.entropica.worldgen.feature;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Generates Biome #17 ({@code 2.17 Astral Skerries}) towering {@code Stardust Spikes}
 * ({@code 10-20} blocks tall):
 * <ul>
 *   <li>Anchored in a fractured {@code astral_impactite} &amp; {@code budding_astral_impactite}
 *       crater collar ({@code radius 2}) on rocky/ice skerry islands or shallow reefs ({@code <= 4} blocks deep).</li>
 *   <li>Central {@code stardust_crystal_block} obelisk ({@code 10-20} blocks tall, {@code 2x2} or
 *       {@code 3x3} cross base narrowing into a sharp {@code 1x1} spire).</li>
 *   <li>Flanked by {@code 2-3} angled {@code 4-9} block secondary crystal prongs jutting outward from the collar.</li>
 *   <li>Richly studded along lateral faces and tips with properly oriented ({@code FACING}, {@code WATERLOGGED})
 *       {@code stardust_crystal_cluster} formations.</li>
 * </ul>
 */
public class StardustCrystalSpireFeature extends Feature<NoneFeatureConfiguration> {

    private static final ResourceLocation STARDUST_CRYSTAL_BLOCK_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "stardust_crystal_block");
    private static final ResourceLocation STARDUST_CRYSTAL_CLUSTER_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "stardust_crystal_cluster");
    private static final ResourceLocation STARLIGHT_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "starlight_ice");
    private static final ResourceLocation ASTRAL_IMPACTITE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "astral_impactite");
    private static final ResourceLocation BUDDING_ASTRAL_IMPACTITE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "budding_astral_impactite");
    private static final ResourceLocation COARSE_GRAVEL_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "coarse_gravel");
    private static final ResourceLocation ASTRAL_MARBLE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "astral_marble");
    private static final ResourceLocation ASTRAL_MARBLE_BRICKS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "astral_marble_bricks");
    private static final ResourceLocation ASTRAL_GLASS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "astral_glass");

    private static final int[][] CROSS_BASE_OFFSETS = {
            {0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };
    private static final int[][] TWO_BY_TWO_OFFSETS = {
            {0, 0}, {1, 0}, {0, 1}, {1, 1}
    };

    public StardustCrystalSpireFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        Block stardustCrystalBlock = resolveBlock(
                () -> ModBlocks.STARDUST_CRYSTAL_BLOCK.isPresent() ? ModBlocks.STARDUST_CRYSTAL_BLOCK.get() : null,
                STARDUST_CRYSTAL_BLOCK_ID,
                Blocks.AMETHYST_BLOCK
        );
        Optional<Block> stardustClusterOpt = resolveOptionalBlock(
                () -> ModBlocks.STARDUST_CRYSTAL_CLUSTER.isPresent() ? ModBlocks.STARDUST_CRYSTAL_CLUSTER.get() : null,
                STARDUST_CRYSTAL_CLUSTER_ID
        );
        Block starlightIceBlock = resolveBlock(
                () -> ModBlocks.STARLIGHT_ICE.isPresent() ? ModBlocks.STARLIGHT_ICE.get() : null,
                STARLIGHT_ICE_ID,
                Blocks.BLUE_ICE
        );
        Block impactiteBlock = resolveBlock(
                () -> ModBlocks.ASTRAL_IMPACTITE.isPresent() ? ModBlocks.ASTRAL_IMPACTITE.get() : null,
                ASTRAL_IMPACTITE_ID,
                Blocks.DEEPSLATE
        );
        Block buddingImpactiteBlock = resolveBlock(
                () -> ModBlocks.BUDDING_ASTRAL_IMPACTITE.isPresent() ? ModBlocks.BUDDING_ASTRAL_IMPACTITE.get() : null,
                BUDDING_ASTRAL_IMPACTITE_ID,
                impactiteBlock
        );
        Block coarseGravelBlock = resolveBlock(
                () -> ModBlocks.COARSE_GRAVEL.isPresent() ? ModBlocks.COARSE_GRAVEL.get() : null,
                COARSE_GRAVEL_ID,
                Blocks.GRAVEL
        );

        ReefSite site = findSkerryOrShallowReefAnchor(
                level, origin, random, starlightIceBlock, impactiteBlock, buddingImpactiteBlock, coarseGravelBlock);
        if (site == null) {
            return false;
        }

        int cx = site.x;
        int cz = site.z;
        int surfaceRefY = site.surfaceRefY;
        int seafloorY = site.seafloorY;

        if (isNearOrreryStructure(level, cx, surfaceRefY, cz)) {
            return false;
        }

        int obeliskHeight = 10 + random.nextInt(11); // 10..20 blocks tall!
        if (surfaceRefY + obeliskHeight + 3 >= level.getMaxY()) {
            return false;
        }

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int checkDy = 3; checkDy <= obeliskHeight; checkDy += 3) {
            cursor.set(cx, surfaceRefY + checkDy, cz);
            if (!isAirOrSoftCover(level.getBlockState(cursor))) {
                return false;
            }
        }

        BlockState crystalState = stardustCrystalBlock.defaultBlockState();
        BlockState impactiteState = impactiteBlock.defaultBlockState();
        BlockState buddingState = buddingImpactiteBlock.defaultBlockState();

        Set<BlockPos> placedCrystalBlocks = new LinkedHashSet<>();
        List<BlockPos> spireTipPositions = new ArrayList<>();

        // 1. Fractured astral_impactite & budding_astral_impactite collar (radius 2) anchored down to seafloor
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                int distSq = dx * dx + dz * dz;
                if (distSq > 6 || (distSq == 5 && random.nextFloat() < 0.25F) || (distSq == 6 && random.nextFloat() < 0.65F)) {
                    continue;
                }

                int bx = cx + dx;
                int bz = cz + dz;
                int localFloorY = findLocalFloor(
                        level, bx, surfaceRefY, bz, 6,
                        starlightIceBlock, impactiteBlock, buddingImpactiteBlock, coarseGravelBlock, stardustCrystalBlock
                );
                if (localFloorY == Integer.MIN_VALUE) {
                    continue;
                }

                int collarRimHeight = (distSq <= 2)
                        ? (1 + random.nextInt(2))
                        : (random.nextFloat() < 0.60F ? 1 : 0);
                int collarTopY = Math.max(localFloorY, surfaceRefY - 1) + collarRimHeight;

                for (int wy = localFloorY; wy <= collarTopY; wy++) {
                    cursor.set(bx, wy, bz);
                    if (canReplaceTerrain(level.getBlockState(cursor), starlightIceBlock, impactiteBlock, buddingImpactiteBlock, coarseGravelBlock)) {
                        boolean useBudding = (wy >= collarTopY - 1) && (distSq >= 1) && (random.nextFloat() < 0.24F);
                        level.setBlock(cursor, useBudding ? buddingState : impactiteState, 2);
                    }
                }
            }
        }

        // 2. Central stardust_crystal_block obelisk (10-20 blocks tall, 2x2 or cross base -> faceted ribs -> 1x1 spire)
        boolean useCrossBase = random.nextBoolean();
        int[][] baseOffsets = useCrossBase ? CROSS_BASE_OFFSETS : TWO_BY_TWO_OFFSETS;
        int fullBaseEnd = Math.max(3, (int) Math.round(obeliskHeight * 0.30D));
        int facetMidEnd = Math.max(fullBaseEnd + 2, (int) Math.round(obeliskHeight * 0.58D));
        boolean nsFacetAxis = random.nextBoolean();

        // Root central crystal core into seafloor/impactite collar
        for (int[] off : baseOffsets) {
            int bx = cx + off[0];
            int bz = cz + off[1];
            for (int wy = Math.max(level.getMinY() + 4, seafloorY); wy <= surfaceRefY; wy++) {
                BlockPos p = new BlockPos(bx, wy, bz);
                if (canReplaceTerrain(level.getBlockState(p), starlightIceBlock, impactiteBlock, buddingImpactiteBlock, coarseGravelBlock)) {
                    level.setBlock(p, crystalState, 2);
                    placedCrystalBlocks.add(p);
                }
            }
        }

        for (int dy = 1; dy <= obeliskHeight; dy++) {
            int wy = surfaceRefY + dy;
            if (dy <= fullBaseEnd) {
                for (int[] off : baseOffsets) {
                    placeCrystalBlock(level, new BlockPos(cx + off[0], wy, cz + off[1]),
                            crystalState, starlightIceBlock, impactiteBlock, buddingImpactiteBlock, coarseGravelBlock, placedCrystalBlocks);
                }
            } else if (dy <= facetMidEnd) {
                placeCrystalBlock(level, new BlockPos(cx, wy, cz),
                        crystalState, starlightIceBlock, impactiteBlock, buddingImpactiteBlock, coarseGravelBlock, placedCrystalBlocks);
                if (useCrossBase) {
                    int rx = nsFacetAxis ? 0 : 1;
                    int rz = nsFacetAxis ? 1 : 0;
                    placeCrystalBlock(level, new BlockPos(cx + rx, wy, cz + rz),
                            crystalState, starlightIceBlock, impactiteBlock, buddingImpactiteBlock, coarseGravelBlock, placedCrystalBlocks);
                    if (dy <= (fullBaseEnd + facetMidEnd) / 2) {
                        placeCrystalBlock(level, new BlockPos(cx - rx, wy, cz - rz),
                                crystalState, starlightIceBlock, impactiteBlock, buddingImpactiteBlock, coarseGravelBlock, placedCrystalBlocks);
                    }
                } else {
                    // Taper 2x2 down to 2 blocks then 1x1
                    placeCrystalBlock(level, new BlockPos(cx + (nsFacetAxis ? 0 : 1), wy, cz + (nsFacetAxis ? 1 : 0)),
                            crystalState, starlightIceBlock, impactiteBlock, buddingImpactiteBlock, coarseGravelBlock, placedCrystalBlocks);
                }
            } else {
                placeCrystalBlock(level, new BlockPos(cx, wy, cz),
                        crystalState, starlightIceBlock, impactiteBlock, buddingImpactiteBlock, coarseGravelBlock, placedCrystalBlocks);
            }
        }
        spireTipPositions.add(new BlockPos(cx, surfaceRefY + obeliskHeight, cz));

        // 3. 2-3 angled 4-9 block secondary crystal prongs jutting outward from the impactite collar
        int prongCount = 2 + random.nextInt(2); // 2..3 secondary prongs
        double baseAzimuth = random.nextDouble() * (Math.PI * 2.0D);

        for (int p = 0; p < prongCount; p++) {
            double angle = baseAzimuth + (p * (Math.PI * 2.0D) / prongCount) + (random.nextDouble() - 0.5D) * 0.45D;
            double dirX = Math.cos(angle);
            double dirZ = Math.sin(angle);
            int prongLength = 4 + random.nextInt(6); // 4..9 blocks!
            double tiltPerStep = 0.42D + random.nextDouble() * 0.16D;

            int prevPx = cx + Mth.floor(dirX * 1.15D + 0.5D);
            int prevPz = cz + Mth.floor(dirZ * 1.15D + 0.5D);
            int startY = surfaceRefY;

            // Anchor prong root down to collar/seafloor
            int rootFloor = findLocalFloor(
                    level, prevPx, startY, prevPz, 5,
                    starlightIceBlock, impactiteBlock, buddingImpactiteBlock, coarseGravelBlock, stardustCrystalBlock
            );
            if (rootFloor == Integer.MIN_VALUE) {
                continue;
            }
            for (int wy = rootFloor; wy <= startY; wy++) {
                placeCrystalBlock(level, new BlockPos(prevPx, wy, prevPz),
                        crystalState, starlightIceBlock, impactiteBlock, buddingImpactiteBlock, coarseGravelBlock, placedCrystalBlocks);
            }

            BlockPos lastProngPos = null;
            for (int step = 1; step <= prongLength; step++) {
                int px = cx + Mth.floor(dirX * (1.15D + step * tiltPerStep) + 0.5D);
                int pz = cz + Mth.floor(dirZ * (1.15D + step * tiltPerStep) + 0.5D);
                int py = startY + step;

                // Ensure face-connected structural continuity when the angled prong steps outward horizontally
                if (px != prevPx || pz != prevPz) {
                    placeCrystalBlock(level, new BlockPos(prevPx, py, prevPz),
                            crystalState, starlightIceBlock, impactiteBlock, buddingImpactiteBlock, coarseGravelBlock, placedCrystalBlocks);
                }

                BlockPos prongPos = new BlockPos(px, py, pz);
                if (placeCrystalBlock(level, prongPos,
                        crystalState, starlightIceBlock, impactiteBlock, buddingImpactiteBlock, coarseGravelBlock, placedCrystalBlocks)) {
                    lastProngPos = prongPos;
                }
                prevPx = px;
                prevPz = pz;
            }
            if (lastProngPos != null) {
                spireTipPositions.add(lastProngPos);
            }
        }

        if (placedCrystalBlocks.isEmpty()) {
            return false;
        }

        // 4. Richly stud tips and lateral faces with properly oriented (FACING, WATERLOGGED) stardust_crystal_cluster
        if (stardustClusterOpt.isPresent()) {
            Block clusterBlock = stardustClusterOpt.get();

            // Always crown spire tips (central apex + secondary prong tips) with upward clusters
            for (BlockPos tipPos : spireTipPositions) {
                tryPlaceStardustCluster(level, tipPos, Direction.UP, clusterBlock);
            }

            // Stud lateral faces and crystal steps along the obelisk & prongs
            for (BlockPos cPos : placedCrystalBlocks) {
                int relY = cPos.getY() - surfaceRefY;
                float lateralChance = (relY >= fullBaseEnd) ? 0.30F : 0.22F;
                for (Direction face : Direction.Plane.HORIZONTAL) {
                    if (random.nextFloat() < lateralChance) {
                        tryPlaceStardustCluster(level, cPos, face, clusterBlock);
                    }
                }
                if (random.nextFloat() < 0.28F) {
                    tryPlaceStardustCluster(level, cPos, Direction.UP, clusterBlock);
                }
            }
        }

        return true;
    }

    private static boolean placeCrystalBlock(
            WorldGenLevel level,
            BlockPos pos,
            BlockState crystalState,
            Block starlightIceBlock,
            Block impactiteBlock,
            Block buddingImpactiteBlock,
            Block coarseGravelBlock,
            Set<BlockPos> outPlaced
    ) {
        if (canReplaceTerrain(level.getBlockState(pos), starlightIceBlock, impactiteBlock, buddingImpactiteBlock, coarseGravelBlock)) {
            level.setBlock(pos, crystalState, 2);
            outPlaced.add(pos.immutable());
            return true;
        }
        return false;
    }

    private static void tryPlaceStardustCluster(
            WorldGenLevel level,
            BlockPos hostPos,
            Direction face,
            Block clusterBlock
    ) {
        BlockPos budPos = hostPos.relative(face);
        BlockState target = level.getBlockState(budPos);
        boolean inWater = isWater(target);
        if (!inWater && !isAirOrSoftCover(target)) {
            return;
        }
        boolean waterlogged = target.getFluidState().getType() == Fluids.WATER;
        BlockState clusterState = clusterBlock.defaultBlockState();
        if (clusterState.hasProperty(BlockStateProperties.FACING)) {
            clusterState = clusterState.setValue(BlockStateProperties.FACING, face);
        }
        if (clusterState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            clusterState = clusterState.setValue(BlockStateProperties.WATERLOGGED, waterlogged);
        }
        if (clusterState.canSurvive(level, budPos)) {
            level.setBlock(budPos, clusterState, 2);
        }
    }

    private record ReefSite(int x, int z, int seafloorY, int surfaceRefY) {}

    private static ReefSite findSkerryOrShallowReefAnchor(
            WorldGenLevel level,
            BlockPos origin,
            RandomSource random,
            Block starlightIceBlock,
            Block impactiteBlock,
            Block buddingImpactiteBlock,
            Block coarseGravelBlock
    ) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int attempt = 0; attempt < 5; attempt++) {
            int ox = (attempt == 0) ? 0 : (random.nextInt(5) - 2);
            int oz = (attempt == 0) ? 0 : (random.nextInt(5) - 2);
            int tx = origin.getX() + ox;
            int tz = origin.getZ() + oz;

            int topScanY = Math.min(level.getMaxY() - 25, origin.getY() + 3);
            int bottomScanY = Math.max(level.getMinY() + 6, origin.getY() - 8);

            for (int y = topScanY; y >= bottomScanY; y--) {
                cursor.set(tx, y, tz);
                BlockState state = level.getBlockState(cursor);
                if (isAirOrSoftCover(state)) {
                    continue;
                }

                if (isValidFooting(state, starlightIceBlock, impactiteBlock, buddingImpactiteBlock, coarseGravelBlock)) {
                    return new ReefSite(tx, tz, y, y);
                }

                // Shallow reef <= 4 blocks deep
                if (isWater(state)) {
                    int waterTopY = y;
                    for (int depth = 1; depth <= 4; depth++) {
                        int checkY = waterTopY - depth;
                        if (checkY <= level.getMinY() + 4) {
                            break;
                        }
                        cursor.set(tx, checkY, tz);
                        BlockState below = level.getBlockState(cursor);
                        if (isWater(below)) {
                            continue;
                        }
                        if (isValidFooting(below, starlightIceBlock, impactiteBlock, buddingImpactiteBlock, coarseGravelBlock)) {
                            return new ReefSite(tx, tz, checkY, waterTopY);
                        }
                        break;
                    }
                }
                break;
            }
        }
        return null;
    }

    private static int findLocalFloor(
            WorldGenLevel level,
            int x,
            int refY,
            int z,
            int maxDrop,
            Block starlightIceBlock,
            Block impactiteBlock,
            Block buddingImpactiteBlock,
            Block coarseGravelBlock,
            Block crystalBlock
    ) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = 2; dy >= -maxDrop; dy--) {
            int wy = refY + dy;
            if (wy <= level.getMinY() + 4) {
                break;
            }
            cursor.set(x, wy, z);
            BlockState state = level.getBlockState(cursor);
            if (isAirOrSoftCover(state) || isWater(state)) {
                continue;
            }
            if (state.is(crystalBlock)
                    || isValidFooting(state, starlightIceBlock, impactiteBlock, buddingImpactiteBlock, coarseGravelBlock)) {
                return wy;
            }
            return Integer.MIN_VALUE;
        }
        return Integer.MIN_VALUE;
    }

    private static boolean isNearOrreryStructure(WorldGenLevel level, int cx, int refY, int cz) {
        Optional<Block> marbleOpt = BuiltInRegistries.BLOCK.getOptional(ASTRAL_MARBLE_ID);
        Optional<Block> marbleBricksOpt = BuiltInRegistries.BLOCK.getOptional(ASTRAL_MARBLE_BRICKS_ID);
        Optional<Block> astralGlassOpt = BuiltInRegistries.BLOCK.getOptional(ASTRAL_GLASS_ID);

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            for (int dist : new int[]{4, 8}) {
                for (int dy : new int[]{1, 5}) {
                    cursor.set(cx + dir.getStepX() * dist, refY + dy, cz + dir.getStepZ() * dist);
                    BlockState st = level.getBlockState(cursor);
                    if ((marbleOpt.isPresent() && st.is(marbleOpt.get()))
                            || (marbleBricksOpt.isPresent() && st.is(marbleBricksOpt.get()))
                            || (astralGlassOpt.isPresent() && st.is(astralGlassOpt.get()))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isValidFooting(
            BlockState state,
            Block starlightIceBlock,
            Block impactiteBlock,
            Block buddingImpactiteBlock,
            Block coarseGravelBlock
    ) {
        return state.is(starlightIceBlock)
                || state.is(impactiteBlock)
                || state.is(buddingImpactiteBlock)
                || state.is(coarseGravelBlock)
                || state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.ICE)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.GRAVEL)
                || state.is(BlockTags.BASE_STONE_OVERWORLD);
    }

    private static boolean canReplaceTerrain(
            BlockState state,
            Block starlightIceBlock,
            Block impactiteBlock,
            Block buddingImpactiteBlock,
            Block coarseGravelBlock
    ) {
        return isAirOrSoftCover(state)
                || isWater(state)
                || state.is(starlightIceBlock)
                || state.is(impactiteBlock)
                || state.is(buddingImpactiteBlock)
                || state.is(coarseGravelBlock)
                || state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.ICE)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.GRAVEL)
                || state.is(BlockTags.BASE_STONE_OVERWORLD);
    }

    private static boolean isWater(BlockState state) {
        return state.is(Blocks.WATER) || state.getFluidState().getType() == Fluids.WATER;
    }

    private static boolean isAirOrSoftCover(BlockState state) {
        return state.isAir()
                || state.is(Blocks.SNOW)
                || state.is(Blocks.POWDER_SNOW)
                || (state.canBeReplaced() && state.getFluidState().isEmpty());
    }

    private static Block resolveBlock(Supplier<Block> supplier, ResourceLocation id, Block fallback) {
        try {
            Block b = supplier.get();
            if (b != null) {
                return b;
            }
        } catch (Throwable ignored) {
        }
        return BuiltInRegistries.BLOCK.getOptional(id).orElse(fallback);
    }

    private static Optional<Block> resolveOptionalBlock(Supplier<Block> supplier, ResourceLocation id) {
        try {
            Block b = supplier.get();
            if (b != null) {
                return Optional.of(b);
            }
        } catch (Throwable ignored) {
        }
        return BuiltInRegistries.BLOCK.getOptional(id);
    }
}
