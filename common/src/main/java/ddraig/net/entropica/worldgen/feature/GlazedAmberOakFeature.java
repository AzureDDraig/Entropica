package ddraig.net.entropica.worldgen.feature;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GlowLichenBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates graceful {@code 5..8}-block tall Glazed Amber-Oak trees in {@code 2.10 Radiant Glaze-Plains},
 * built from {@code entropica:amber_log} trunks and spreading upper limbs, rounded/tiered canopies of
 * {@code entropica:glazed_amber_leaves}, and luminous {@code entropica:golden_lichen} clinging naturally
 * to the sides of the {@code amber_log} trunk.
 */
public class GlazedAmberOakFeature extends Feature<NoneFeatureConfiguration> {

    private static final ResourceLocation AMBER_LOG_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "amber_log");
    private static final ResourceLocation GLAZED_AMBER_LEAVES_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "glazed_amber_leaves");
    private static final ResourceLocation GOLDEN_LICHEN_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "golden_lichen");
    private static final ResourceLocation RADIANT_SNOW_BLOCK_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "radiant_snow_block");
    private static final ResourceLocation VITAE_SPRING_STONE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vitae_spring_stone");

    public GlazedAmberOakFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        Block amberLogBlock = BuiltInRegistries.BLOCK.getOptional(AMBER_LOG_ID).orElse(Blocks.OAK_LOG);
        Block glazedLeavesBlock = BuiltInRegistries.BLOCK.getOptional(GLAZED_AMBER_LEAVES_ID).orElse(Blocks.OAK_LEAVES);
        Block goldenLichenBlock = BuiltInRegistries.BLOCK.getOptional(GOLDEN_LICHEN_ID).orElse(Blocks.GLOW_LICHEN);
        Block radiantSnowBlock = BuiltInRegistries.BLOCK.getOptional(RADIANT_SNOW_BLOCK_ID).orElse(Blocks.SNOW_BLOCK);
        Block springStoneBlock = BuiltInRegistries.BLOCK.getOptional(VITAE_SPRING_STONE_ID).orElse(Blocks.CALCITE);

        BlockPos groundPos = findTreeGround(level, origin.getX(), origin.getZ(), origin.getY(),
                radiantSnowBlock, springStoneBlock);
        if (groundPos == null) {
            return false;
        }

        int trunkHeight = 5 + random.nextInt(4); // 5..8 blocks tall
        int baseX = groundPos.getX();
        int baseY = groundPos.getY() + 1;
        int baseZ = groundPos.getZ();

        if (baseY + trunkHeight + 3 >= level.getMaxY() - 1) {
            return false;
        }

        // Ensure clean vertical trunk & canopy clearance
        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
        for (int dy = 0; dy <= trunkHeight + 1; dy++) {
            int checkRadius = (dy >= trunkHeight - 2) ? 2 : 0;
            for (int dx = -checkRadius; dx <= checkRadius; dx++) {
                for (int dz = -checkRadius; dz <= checkRadius; dz++) {
                    checkPos.set(baseX + dx, baseY + dy, baseZ + dz);
                    if (!canTreeReplace(level.getBlockState(checkPos))) {
                        return false;
                    }
                }
            }
        }

        // Convert grass_block beneath trunk base to dirt
        if (level.getBlockState(groundPos).is(Blocks.GRASS_BLOCK)) {
            level.setBlock(groundPos, Blocks.DIRT.defaultBlockState(), 2);
        }

        BlockState verticalLog = logWithAxis(amberLogBlock, Direction.Axis.Y);
        BlockState leavesState = makePersistentLeaves(glazedLeavesBlock);

        // 1. Place graceful vertical amber_log trunk (5..8 blocks tall)
        List<BlockPos> lowerMidTrunkNodes = new ArrayList<>();
        for (int h = 0; h < trunkHeight; h++) {
            BlockPos logPos = new BlockPos(baseX, baseY + h, baseZ);
            level.setBlock(logPos, verticalLog, 2);
            if (h >= 1 && h <= trunkHeight - 3) {
                lowerMidTrunkNodes.add(logPos);
            }
        }

        int topY = baseY + trunkHeight - 1;

        // 2. Place 1..2 spreading oak limbs near upper trunk for natural tiered canopy breadth
        int limbCount = (trunkHeight >= 6) ? (1 + random.nextInt(2)) : 1;
        List<Direction> limbDirs = Direction.Plane.HORIZONTAL.shuffledCopy(random);
        List<BlockPos> limbTips = new ArrayList<>();
        for (int i = 0; i < limbCount && i < limbDirs.size(); i++) {
            Direction limbDir = limbDirs.get(i);
            int limbY = topY - 1 - (i % 2);
            BlockPos limbPos = new BlockPos(baseX + limbDir.getStepX(), limbY, baseZ + limbDir.getStepZ());
            if (canTreeReplace(level.getBlockState(limbPos))) {
                level.setBlock(limbPos, logWithAxis(amberLogBlock, limbDir.getAxis()), 2);
                limbTips.add(limbPos);
            }
        }

        // 3. Rounded / Tiered Glazed Amber Leaves Canopy
        // Lower wide tiers (topY - 2 and topY - 1): radius 2 rounded disc
        for (int layerY = topY - 2; layerY <= topY - 1; layerY++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx == 0 && dz == 0) {
                        continue; // Trunk center
                    }
                    boolean isOuterCorner = Math.abs(dx) == 2 && Math.abs(dz) == 2;
                    if (isOuterCorner && (layerY == topY - 2 || random.nextFloat() < 0.65F)) {
                        continue;
                    }
                    placeLeafIfClear(level, new BlockPos(baseX + dx, layerY, baseZ + dz), leavesState);
                }
            }
        }

        // Mid crown tier (topY): radius 2 cardinal + inner diagonals
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                double dist = Math.hypot(dx, dz);
                if (dist <= 2.05D && !(Math.abs(dx) == 2 && Math.abs(dz) >= 1 && random.nextFloat() < 0.55F)) {
                    placeLeafIfClear(level, new BlockPos(baseX + dx, topY, baseZ + dz), leavesState);
                }
            }
        }

        // Upper rounded crown cap (topY + 1): plus-cross + occasional soft diagonal
        placeLeafIfClear(level, new BlockPos(baseX, topY + 1, baseZ), leavesState);
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            placeLeafIfClear(level, new BlockPos(baseX + dir.getStepX(), topY + 1, baseZ + dir.getStepZ()), leavesState);
        }
        for (int dx : new int[]{-1, 1}) {
            for (int dz : new int[]{-1, 1}) {
                if (random.nextFloat() < 0.45F) {
                    placeLeafIfClear(level, new BlockPos(baseX + dx, topY + 1, baseZ + dz), leavesState);
                }
            }
        }

        // Secondary leaf tufts around spreading oak limbs
        for (BlockPos tip : limbTips) {
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                if (random.nextFloat() < 0.75F) {
                    placeLeafIfClear(level, tip.relative(dir), leavesState);
                }
            }
            placeLeafIfClear(level, tip.above(), leavesState);
        }

        // 4. Cling entropica:golden_lichen naturally onto exposed side faces of the amber_log trunk!
        int placedLichen = 0;
        int targetLichen = 2 + random.nextInt(3); // 2..4 patches per tree
        for (BlockPos trunkPos : lowerMidTrunkNodes) {
            if (placedLichen >= targetLichen) {
                break;
            }
            for (Direction outwardDir : Direction.Plane.HORIZONTAL.shuffledCopy(random)) {
                if (placedLichen >= targetLichen) {
                    break;
                }
                if (random.nextFloat() > 0.55F) {
                    continue;
                }
                BlockPos lichenPos = trunkPos.relative(outwardDir);
                BlockState existing = level.getBlockState(lichenPos);
                if (!existing.isAir() && !existing.is(Blocks.SNOW)) {
                    continue;
                }
                // From lichenPos, the supporting amber_log trunk sits in direction outwardDir.getOpposite()
                Direction attachFace = outwardDir.getOpposite();
                BlockState lichenState = makeSingleFaceLichenState(goldenLichenBlock, attachFace);
                if (lichenState != null && lichenState.canSurvive(level, lichenPos)) {
                    level.setBlock(lichenPos, lichenState, 2);
                    placedLichen++;
                }
            }
        }

        // Guarantee at least 1 golden_lichen patch if lower trunk had clear air
        if (placedLichen == 0 && !lowerMidTrunkNodes.isEmpty()) {
            BlockPos trunkPos = lowerMidTrunkNodes.get(0);
            for (Direction outwardDir : Direction.Plane.HORIZONTAL) {
                BlockPos lichenPos = trunkPos.relative(outwardDir);
                if (level.getBlockState(lichenPos).isAir()) {
                    BlockState lichenState = makeSingleFaceLichenState(goldenLichenBlock, outwardDir.getOpposite());
                    if (lichenState != null && lichenState.canSurvive(level, lichenPos)) {
                        level.setBlock(lichenPos, lichenState, 2);
                        break;
                    }
                }
            }
        }

        return true;
    }

    public static BlockState makeSingleFaceLichenState(Block lichenBlock, Direction attachFace) {
        BlockState state = lichenBlock.defaultBlockState();
        for (Direction dir : Direction.values()) {
            BooleanProperty faceProp = GlowLichenBlock.getFaceProperty(dir);
            if (state.hasProperty(faceProp)) {
                state = state.setValue(faceProp, false);
            }
        }
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        BooleanProperty targetProp = GlowLichenBlock.getFaceProperty(attachFace);
        if (!state.hasProperty(targetProp)) {
            return null;
        }
        return state.setValue(targetProp, true);
    }

    private static void placeLeafIfClear(WorldGenLevel level, BlockPos pos, BlockState leavesState) {
        BlockState existing = level.getBlockState(pos);
        if (existing.isAir() || existing.is(Blocks.SNOW)) {
            level.setBlock(pos, leavesState, 2);
        }
    }

    private static BlockPos findTreeGround(
            WorldGenLevel level,
            int x,
            int z,
            int hintY,
            Block radiantSnowBlock,
            Block springStoneBlock
    ) {
        int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
        int startY = Math.max(topY, hintY + 3);
        int minY = Math.max(62, startY - 8);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int y = startY; y >= minY; y--) {
            cursor.set(x, y, z);
            BlockState ground = level.getBlockState(cursor);
            if (ground.is(Blocks.WATER) || ground.is(Blocks.LAVA)) {
                return null;
            }
            if (isValidAmberOakSoil(ground, radiantSnowBlock, springStoneBlock)) {
                BlockState above = level.getBlockState(cursor.above());
                if (canTreeReplace(above) && !above.is(Blocks.WATER)) {
                    return cursor.immutable();
                }
            }
        }
        return null;
    }

    private static boolean isValidAmberOakSoil(BlockState state, Block radiantSnowBlock, Block springStoneBlock) {
        return state.is(Blocks.GRASS_BLOCK)
                || state.is(BlockTags.DIRT)
                || state.is(radiantSnowBlock)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(springStoneBlock)
                || state.is(Blocks.CALCITE);
    }

    private static boolean canTreeReplace(BlockState state) {
        return state.isAir()
                || state.is(Blocks.SNOW)
                || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.REPLACEABLE)
                || state.is(BlockTags.FLOWERS);
    }

    private static BlockState logWithAxis(Block logBlock, Direction.Axis axis) {
        BlockState state = logBlock.defaultBlockState();
        if (state.hasProperty(RotatedPillarBlock.AXIS)) {
            return state.setValue(RotatedPillarBlock.AXIS, axis);
        }
        if (state.hasProperty(BlockStateProperties.AXIS)) {
            return state.setValue(BlockStateProperties.AXIS, axis);
        }
        return state;
    }

    private static BlockState makePersistentLeaves(Block leavesBlock) {
        BlockState state = leavesBlock.defaultBlockState();
        if (state.hasProperty(LeavesBlock.PERSISTENT)) {
            state = state.setValue(LeavesBlock.PERSISTENT, true);
        } else if (state.hasProperty(BlockStateProperties.PERSISTENT)) {
            state = state.setValue(BlockStateProperties.PERSISTENT, true);
        }
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        return state;
    }
}
