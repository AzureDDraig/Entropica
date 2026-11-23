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
 * Generates suspended crystalline rift shards ({@code 4-8} blocks tall, {@code 1-3} blocks wide
 * tapering to sharp points at top and bottom) hovering {@code 7-15} blocks above
 * {@code 2.9 Null-Fringe Bay} water, {@code void_sand}, {@code void_sand_turf}, or {@code blue_ice}.
 */
public class FloatingObsidianShardFeature extends Feature<NoneFeatureConfiguration> {

    private static final ResourceLocation VOID_SAND_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "void_sand");
    private static final ResourceLocation VOID_SAND_TURF_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "void_sand_turf");
    private static final ResourceLocation VOID_STONE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "void_stone");
    private static final ResourceLocation OBSIDIAN_BRICKS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "obsidian_bricks");
    private static final ResourceLocation VOID_CRYSTAL_CLUSTER_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "void_crystal_cluster");
    private static final ResourceLocation VOID_CRYSTAL_BUD_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "void_crystal_bud");

    public FloatingObsidianShardFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        Optional<Block> voidSandOpt = BuiltInRegistries.BLOCK.getOptional(VOID_SAND_ID);
        Optional<Block> voidSandTurfOpt = BuiltInRegistries.BLOCK.getOptional(VOID_SAND_TURF_ID);

        BlockPos surfacePos = findBaySurface(level, origin, voidSandOpt, voidSandTurfOpt);
        if (surfacePos == null) {
            return false;
        }

        int hoverOffset = 7 + random.nextInt(9); // 7..15 blocks above surface
        int shardHeight = 4 + random.nextInt(5); // 4..8 blocks tall
        int maxWidthTier = 1 + random.nextInt(3); // 1 (slender needle), 2 (faceted prism), or 3 (3-wide diamond monolith)
        int baseY = surfacePos.getY() + hoverOffset;

        if (baseY + shardHeight + 2 >= level.getMaxY()) {
            return false;
        }

        int centerX = surfacePos.getX();
        int centerZ = surfacePos.getZ();

        // Ensure complete clear air padding around the entire suspended shard volume before placing anything
        int clearanceRadius = (maxWidthTier >= 3) ? 2 : 1;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = -2; dy <= shardHeight + 1; dy++) {
            int wy = baseY + dy;
            for (int dx = -clearanceRadius - 1; dx <= clearanceRadius + 1; dx++) {
                for (int dz = -clearanceRadius - 1; dz <= clearanceRadius + 1; dz++) {
                    cursor.set(centerX + dx, wy, centerZ + dz);
                    if (!level.getBlockState(cursor).isAir()) {
                        return false;
                    }
                }
            }
        }

        Block obsidianBricks = BuiltInRegistries.BLOCK.getOptional(OBSIDIAN_BRICKS_ID).orElse(Blocks.OBSIDIAN);
        Block voidStone = BuiltInRegistries.BLOCK.getOptional(VOID_STONE_ID).orElse(Blocks.DEEPSLATE);
        Block crystalCluster = BuiltInRegistries.BLOCK.getOptional(VOID_CRYSTAL_CLUSTER_ID).orElse(Blocks.AMETHYST_CLUSTER);
        Block crystalBud = BuiltInRegistries.BLOCK.getOptional(VOID_CRYSTAL_BUD_ID).orElse(Blocks.LARGE_AMETHYST_BUD);

        Direction driftDir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        boolean slightLean = shardHeight >= 6 && random.nextBoolean();

        List<BlockPos> placedShardBlocks = new ArrayList<>(24);
        int waistLow = shardHeight / 3;
        int waistHigh = shardHeight - 1 - (shardHeight / 3);

        for (int ry = 0; ry < shardHeight; ry++) {
            int wy = baseY + ry;
            int leanStep = (slightLean && ry >= shardHeight / 2) ? 1 : 0;
            int layerX = centerX + driftDir.getStepX() * leanStep;
            int layerZ = centerZ + driftDir.getStepZ() * leanStep;

            // Always place the central spine block (tapers to 1x1 at ry == 0 and ry == shardHeight - 1)
            BlockPos spinePos = new BlockPos(layerX, wy, layerZ);
            placeShardBlock(level, spinePos, pickShardMaterial(random, obsidianBricks, voidStone), placedShardBlocks);

            boolean inWaist = (ry >= waistLow && ry <= waistHigh && ry > 0 && ry < shardHeight - 1);
            if (!inWaist || maxWidthTier == 1) {
                continue;
            }

            if (maxWidthTier == 2) {
                // 2-wide faceted profile along two perpendicular horizontal faces
                Direction sideA = driftDir;
                Direction sideB = driftDir.getClockWise();
                placeShardBlock(level, spinePos.relative(sideA),
                        pickShardMaterial(random, obsidianBricks, voidStone), placedShardBlocks);
                if (ry == shardHeight / 2 || random.nextBoolean()) {
                    placeShardBlock(level, spinePos.relative(sideB),
                            pickShardMaterial(random, obsidianBricks, voidStone), placedShardBlocks);
                }
            } else {
                // 3-wide tapered diamond cross-section at the middle waist
                boolean peakWaist = (ry == shardHeight / 2);
                for (Direction horiz : Direction.Plane.HORIZONTAL) {
                    if (peakWaist || random.nextInt(3) != 0) {
                        placeShardBlock(level, spinePos.relative(horiz),
                                pickShardMaterial(random, obsidianBricks, voidStone), placedShardBlocks);
                    }
                }
            }
        }

        if (placedShardBlocks.isEmpty()) {
            return false;
        }

        // Sprout 1-2 directional void_crystal_cluster / void_crystal_bud crystals from top/bottom/sides
        record CrystalCandidate(BlockPos pos, Direction facing) {}
        List<CrystalCandidate> candidates = new ArrayList<>();

        BlockPos bottomTip = new BlockPos(centerX, baseY - 1, centerZ);
        if (level.getBlockState(bottomTip).isAir()) {
            candidates.add(new CrystalCandidate(bottomTip, Direction.DOWN));
        }

        int topLean = slightLean ? 1 : 0;
        BlockPos topTip = new BlockPos(
                centerX + driftDir.getStepX() * topLean,
                baseY + shardHeight,
                centerZ + driftDir.getStepZ() * topLean
        );
        if (level.getBlockState(topTip).isAir()) {
            candidates.add(new CrystalCandidate(topTip, Direction.UP));
        }

        for (BlockPos sPos : placedShardBlocks) {
            if (sPos.getY() <= baseY || sPos.getY() >= baseY + shardHeight - 1) {
                continue;
            }
            for (Direction horiz : Direction.Plane.HORIZONTAL) {
                BlockPos adj = sPos.relative(horiz);
                if (level.getBlockState(adj).isAir()) {
                    candidates.add(new CrystalCandidate(adj, horiz));
                }
            }
        }

        int crystalsToPlace = 1 + random.nextInt(2); // 1..2 crystals
        int placedCrystals = 0;
        while (!candidates.isEmpty() && placedCrystals < crystalsToPlace) {
            int idx = random.nextInt(candidates.size());
            CrystalCandidate pick = candidates.remove(idx);
            if (!level.getBlockState(pick.pos()).isAir()) {
                continue;
            }
            Block chosenCrystal = (placedCrystals == 0 || random.nextBoolean()) ? crystalCluster : crystalBud;
            BlockState crystalState = orientCrystal(chosenCrystal.defaultBlockState(), pick.facing(), false);
            if (crystalState.canSurvive(level, pick.pos())) {
                level.setBlock(pick.pos(), crystalState, 2);
                placedCrystals++;
            }
        }

        return true;
    }

    private static BlockPos findBaySurface(
            WorldGenLevel level,
            BlockPos origin,
            Optional<Block> voidSandOpt,
            Optional<Block> voidSandTurfOpt
    ) {
        BlockPos.MutableBlockPos check = new BlockPos.MutableBlockPos();
        for (int dy = 1; dy >= -3; dy--) {
            int y = origin.getY() + dy;
            if (y <= level.getMinY() + 2 || y >= level.getMaxY() - 20) {
                continue;
            }
            check.set(origin.getX(), y, origin.getZ());
            BlockState state = level.getBlockState(check);
            if (isValidBaySurface(state, level, check, voidSandOpt, voidSandTurfOpt)) {
                return check.immutable();
            }
        }
        return null;
    }

    private static boolean isValidBaySurface(
            BlockState state,
            WorldGenLevel level,
            BlockPos pos,
            Optional<Block> voidSandOpt,
            Optional<Block> voidSandTurfOpt
    ) {
        if (state.is(Blocks.WATER) || state.getFluidState().getType() == Fluids.WATER || state.is(Blocks.BLUE_ICE)) {
            return true;
        }
        if (state.is(Blocks.ICE) && level.getBlockState(pos.below()).is(Blocks.WATER)) {
            return true;
        }
        if (voidSandOpt.isPresent() && state.is(voidSandOpt.get())) {
            return true;
        }
        return voidSandTurfOpt.isPresent() && state.is(voidSandTurfOpt.get());
    }

    private static BlockState pickShardMaterial(RandomSource random, Block obsidianBricks, Block voidStone) {
        int roll = random.nextInt(20);
        if (roll < 8) {
            return Blocks.OBSIDIAN.defaultBlockState();
        } else if (roll < 13) {
            return voidStone.defaultBlockState();
        } else if (roll < 17) {
            return obsidianBricks.defaultBlockState();
        } else {
            return Blocks.CRYING_OBSIDIAN.defaultBlockState();
        }
    }

    private static void placeShardBlock(
            WorldGenLevel level,
            BlockPos pos,
            BlockState state,
            List<BlockPos> outPlaced
    ) {
        if (level.getBlockState(pos).isAir()) {
            level.setBlock(pos, state, 2);
            outPlaced.add(pos.immutable());
        }
    }

    private static BlockState orientCrystal(BlockState base, Direction facing, boolean waterlogged) {
        BlockState state = base;
        if (state.hasProperty(BlockStateProperties.FACING)) {
            state = state.setValue(BlockStateProperties.FACING, facing);
        }
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, waterlogged);
        }
        return state;
    }
}
