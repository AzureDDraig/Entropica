package ddraig.net.entropica.worldgen.feature;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates geometric faceted veins and crystal spurs of {@link ModBlocks#AEGIS_CRYSTAL_BLOCK}
 * studded with directional {@link ModBlocks#AEGIS_CRYSTAL_CLUSTER} buds embedded into
 * {@code Blue Ice}, {@code Packed Ice}, and coastal cliff rock, accompanied by occasional
 * {@link ModBlocks#SHIELDING_BRAMBLE} along shelf ledges and {@link ModBlocks#RIMEBLOOM}
 * nestled in sheltered ice crevasses.
 */
public class AegisCrystalFormationFeature extends Feature<NoneFeatureConfiguration> {

    public AegisCrystalFormationFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        BlockPos anchor = null;
        Direction exposedDir = null;

        // Locate an exposed cliff wall, ledge, or crevasse surface near origin
        search:
        for (int dy = -3; dy <= 3; dy++) {
            int y = origin.getY() + dy;
            if (y < level.getMinY() + 4 || y > level.getMaxY() - 6) {
                continue;
            }
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos pos = new BlockPos(origin.getX() + dx, y, origin.getZ() + dz);
                    if (!isHostWall(level.getBlockState(pos))) {
                        continue;
                    }
                    for (Direction dir : Direction.allShuffled(random)) {
                        if (dir == Direction.DOWN && random.nextInt(3) != 0) {
                            continue;
                        }
                        BlockState neighbor = level.getBlockState(pos.relative(dir));
                        if (neighbor.isAir() || neighbor.is(Blocks.WATER) || neighbor.is(Blocks.SNOW)) {
                            anchor = pos;
                            exposedDir = dir;
                            break search;
                        }
                    }
                }
            }
        }

        if (anchor == null || exposedDir == null) {
            return false;
        }

        BlockState crystalBlock = ModBlocks.AEGIS_CRYSTAL_BLOCK.get().defaultBlockState();
        List<BlockPos> placedCrystals = new ArrayList<>(16);

        // Primary faceted hexagonal prism axis jutting out from the embedded ice/rock root
        double dirX = exposedDir.getStepX() * 0.75D + (random.nextDouble() - 0.5D) * 0.55D;
        double dirY = exposedDir.getStepY() * 0.65D + 0.35D + (random.nextDouble() - 0.3D) * 0.35D;
        double dirZ = exposedDir.getStepZ() * 0.75D + (random.nextDouble() - 0.5D) * 0.55D;
        double lenNorm = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        if (lenNorm < 1.0E-4D) {
            dirY = 1.0D;
            lenNorm = 1.0D;
        }
        dirX /= lenNorm;
        dirY /= lenNorm;
        dirZ /= lenNorm;

        int spineLength = 3 + random.nextInt(3); // 3..5 steps (plus root = 4..6 blocks long)
        for (int step = -1; step <= spineLength; step++) {
            int cx = anchor.getX() + Mth.floor(dirX * step + 0.5D);
            int cy = anchor.getY() + Mth.floor(dirY * step + 0.5D);
            int cz = anchor.getZ() + Mth.floor(dirZ * step + 0.5D);
            BlockPos mainPos = new BlockPos(cx, cy, cz);
            tryPlaceCrystal(level, mainPos, crystalBlock, placedCrystals);

            // Thicker faceted base near the cliff root
            if (step <= 1) {
                Direction facetSide = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                tryPlaceCrystal(level, mainPos.relative(facetSide), crystalBlock, placedCrystals);
                if (step == 0 && random.nextBoolean()) {
                    tryPlaceCrystal(level, mainPos.below(), crystalBlock, placedCrystals);
                }
            }
        }

        // Secondary angular crystal spur
        Direction spurHorizontal = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int spurLen = 2 + random.nextInt(2);
        for (int s = 1; s <= spurLen; s++) {
            BlockPos spurPos = anchor.offset(
                    spurHorizontal.getStepX() * s,
                    (s + 1) / 2,
                    spurHorizontal.getStepZ() * s
            );
            tryPlaceCrystal(level, spurPos, crystalBlock, placedCrystals);
        }

        if (placedCrystals.isEmpty()) {
            return false;
        }

        // Stud exposed crystal faces with directional Aegis Crystal Clusters
        BlockState baseCluster = ModBlocks.AEGIS_CRYSTAL_CLUSTER.get().defaultBlockState();
        for (BlockPos cPos : placedCrystals) {
            for (Direction face : Direction.values()) {
                if (random.nextFloat() >= 0.36F) {
                    continue;
                }
                BlockPos budPos = cPos.relative(face);
                BlockState target = level.getBlockState(budPos);
                if (target.isAir() || target.is(Blocks.SNOW) || target.is(Blocks.WATER)) {
                    boolean waterlogged = target.getFluidState().getType() == Fluids.WATER;
                    BlockState clusterState = baseCluster
                            .setValue(AmethystClusterBlock.FACING, face)
                            .setValue(AmethystClusterBlock.WATERLOGGED, waterlogged);
                    if (clusterState.canSurvive(level, budPos)) {
                        level.setBlock(budPos, clusterState, 2);
                    }
                }
            }
        }

        // Accent nearby shelf ledges with Shielding Bramble and sheltered crevasses with Rimebloom
        BlockState bramble = ModBlocks.SHIELDING_BRAMBLE.get().defaultBlockState();
        BlockState rimebloom = ModBlocks.RIMEBLOOM.get().defaultBlockState();
        BlockPos.MutableBlockPos floraPos = new BlockPos.MutableBlockPos();

        for (int attempt = 0; attempt < 10; attempt++) {
            int fx = anchor.getX() + random.nextInt(9) - 4;
            int fy = anchor.getY() + random.nextInt(7) - 3;
            int fz = anchor.getZ() + random.nextInt(9) - 4;
            floraPos.set(fx, fy, fz);

            BlockState current = level.getBlockState(floraPos);
            if (!current.isAir() && !current.is(Blocks.SNOW)) {
                continue;
            }

            BlockState below = level.getBlockState(floraPos.below());
            if (!isFrozenShelfFloor(below)) {
                continue;
            }

            boolean onShelfLedge = false;
            int adjacentWalls = 0;
            for (Direction h : Direction.Plane.HORIZONTAL) {
                if (level.isEmptyBlock(floraPos.below().relative(h))) {
                    onShelfLedge = true;
                }
                if (!level.isEmptyBlock(floraPos.relative(h))) {
                    adjacentWalls++;
                }
            }

            if (onShelfLedge && random.nextFloat() < 0.45F) {
                if (bramble.canSurvive(level, floraPos)) {
                    level.setBlock(floraPos, bramble, 2);
                }
            } else if (adjacentWalls >= 2 && random.nextFloat() < 0.40F) {
                if (rimebloom.canSurvive(level, floraPos)) {
                    level.setBlock(floraPos, rimebloom, 2);
                }
            }
        }

        return true;
    }

    private static void tryPlaceCrystal(WorldGenLevel level, BlockPos pos, BlockState crystalState, List<BlockPos> outPlaced) {
        BlockState existing = level.getBlockState(pos);
        if (canCrystalReplace(existing)) {
            level.setBlock(pos, crystalState, 2);
            outPlaced.add(pos.immutable());
        }
    }

    private static boolean isHostWall(BlockState state) {
        return state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.ICE)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.BASALT)
                || state.is(Blocks.SMOOTH_BASALT)
                || state.is(BlockTags.BASE_STONE_OVERWORLD);
    }

    private static boolean isFrozenShelfFloor(BlockState state) {
        return state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.ICE)
                || state.is(Blocks.SNOW_BLOCK);
    }

    private static boolean canCrystalReplace(BlockState state) {
        return state.isAir()
                || state.is(Blocks.SNOW)
                || state.is(Blocks.POWDER_SNOW)
                || state.is(Blocks.WATER)
                || state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.ICE)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(BlockTags.BASE_STONE_OVERWORLD);
    }
}
