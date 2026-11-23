package ddraig.net.entropica.worldgen.feature;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.block.EntropicaFloraGroundHelper;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Generates crooked resin-veined {@code 6..8}-block tall Amber Weeping Willows in Biome #14
 * ({@code 2.14 Oasis Floes}):
 * <ul>
 *   <li>Strictly grows ONLY on {@link Blocks#GRASS_BLOCK}, {@link Blocks#DIRT}, {@link Blocks#PODZOL},
 *       {@link Blocks#SNOW_BLOCK}, or {@code entropica:vitae_spring_stone} (never on bare regular ice!
 *       Replaces thin {@link Blocks#SNOW} at base).</li>
 *   <li>Crooked {@code entropica:amber_log} trunk ({@code height 6..8}) with arching {@code amber_log} boughs.</li>
 *   <li>Lush rounded weeping canopy of {@code entropica:frost_willow_leaves}.</li>
 *   <li>{@code 1..2} {@code entropica:vapor_weaver_lichen} faces clinging to the lower trunk.</li>
 *   <li>{@code 2..4}-block vertical curtains of {@code entropica:frost_willow_vines} hung around the outer
 *       edge of the canopy with the inward-facing {@link VineBlock#getPropertyForFace(Direction)} set to
 *       {@code true} (and {@link VineBlock#UP} {@code true} on the top segment under overhanging leaves)
 *       so {@code VineBlock.canSurvive} is 100% satisfied on every vine block.</li>
 * </ul>
 */
public class AmberWeepingWillowFeature extends Feature<NoneFeatureConfiguration> {

    private static final ResourceLocation AMBER_LOG_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "amber_log");
    private static final ResourceLocation FROST_WILLOW_LEAVES_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "frost_willow_leaves");
    private static final ResourceLocation FROST_WILLOW_VINES_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "frost_willow_vines");
    private static final ResourceLocation VAPOR_WEAVER_LICHEN_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vapor_weaver_lichen");
    private static final ResourceLocation VITAE_SPRING_STONE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vitae_spring_stone");

    public AmberWeepingWillowFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        Block amberLogBlock = BuiltInRegistries.BLOCK.getOptional(AMBER_LOG_ID).orElse(Blocks.OAK_LOG);
        Block willowLeavesBlock = BuiltInRegistries.BLOCK.getOptional(FROST_WILLOW_LEAVES_ID).orElse(Blocks.AZALEA_LEAVES);
        Block willowVinesBlock = BuiltInRegistries.BLOCK.getOptional(FROST_WILLOW_VINES_ID).orElse(Blocks.VINE);
        Block vaporLichenBlock = BuiltInRegistries.BLOCK.getOptional(VAPOR_WEAVER_LICHEN_ID).orElse(Blocks.GLOW_LICHEN);
        Block springStoneBlock = resolveVitaeSpringStone();

        BlockPos groundPos = findWillowGround(level, origin.getX(), origin.getZ(), origin.getY(), springStoneBlock);
        if (groundPos == null) {
            return false;
        }

        int trunkHeight = 6 + random.nextInt(3); // 6..8 blocks tall
        int baseX = groundPos.getX();
        int baseY = groundPos.getY() + 1;
        int baseZ = groundPos.getZ();

        if (baseY + trunkHeight + 3 >= level.getMaxY() - 1) {
            return false;
        }

        Direction leanDir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int bendStart = Math.max(3, trunkHeight / 2);

        // Verify trunk & canopy clearance
        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
        for (int dy = 0; dy <= trunkHeight + 1; dy++) {
            int cx = baseX + (dy >= bendStart ? leanDir.getStepX() : 0);
            int cz = baseZ + (dy >= bendStart ? leanDir.getStepZ() : 0);
            int radius = (dy >= trunkHeight - 2) ? 3 : 0;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    checkPos.set(cx + dx, baseY + dy, cz + dz);
                    if (!canTreeReplace(level.getBlockState(checkPos))) {
                        return false;
                    }
                }
            }
        }

        // Replace thin snow at base and convert grass_block beneath trunk base to dirt
        if (level.getBlockState(groundPos).is(Blocks.GRASS_BLOCK)) {
            level.setBlock(groundPos, Blocks.DIRT.defaultBlockState(), 2);
        }
        if (level.getBlockState(groundPos.above()).is(Blocks.SNOW)) {
            level.setBlock(groundPos.above(), Blocks.AIR.defaultBlockState(), 2);
        }

        BlockState verticalLog = logWithAxis(amberLogBlock, Direction.Axis.Y);
        BlockState leavesState = makePersistentLeaves(willowLeavesBlock);

        // 1. Crooked resin-veined entropica:amber_log trunk (height 6..8) + subtle basal root flare
        for (Direction rootDir : Direction.Plane.HORIZONTAL.shuffledCopy(random)) {
            BlockPos rootBase = groundPos.relative(rootDir);
            BlockState belowRoot = level.getBlockState(rootBase);
            if (!belowRoot.isAir()
                    && belowRoot.getFluidState().isEmpty()
                    && !EntropicaFloraGroundHelper.isIceOrSnowLayer(belowRoot)
                    && canTreeReplace(level.getBlockState(rootBase.above()))) {
                level.setBlock(rootBase.above(), verticalLog, 2);
                break;
            }
        }

        List<BlockPos> lowerTrunkNodes = new ArrayList<>();
        int crownX = baseX;
        int crownZ = baseZ;
        for (int h = 0; h < trunkHeight; h++) {
            if (h == bendStart) {
                BlockPos elbowPos = new BlockPos(crownX, baseY + h, crownZ);
                level.setBlock(elbowPos, verticalLog, 2);
                crownX += leanDir.getStepX();
                crownZ += leanDir.getStepZ();
            }
            BlockPos logPos = new BlockPos(crownX, baseY + h, crownZ);
            level.setBlock(logPos, verticalLog, 2);
            if (h >= 1 && h <= Math.min(3, trunkHeight - 3)) {
                lowerTrunkNodes.add(logPos);
            }
        }

        int topY = baseY + trunkHeight - 1;

        // 2. Arching amber_log boughs radiating outward from the upper trunk
        List<Direction> boughDirs = Direction.Plane.HORIZONTAL.shuffledCopy(random);
        int boughCount = 3 + random.nextInt(2); // 3..4 arching boughs
        for (int i = 0; i < boughCount && i < boughDirs.size(); i++) {
            Direction bDir = boughDirs.get(i);
            int boughY = (i < 2) ? topY : (topY - 1);
            BlockState axisLog = logWithAxis(amberLogBlock, bDir.getAxis());
            BlockPos innerBough = new BlockPos(crownX + bDir.getStepX(), boughY, crownZ + bDir.getStepZ());
            if (canTreeReplace(level.getBlockState(innerBough))) {
                level.setBlock(innerBough, axisLog, 2);
            }
            BlockPos outerBough = innerBough.relative(bDir);
            if (canTreeReplace(level.getBlockState(outerBough))) {
                level.setBlock(outerBough, axisLog, 2);
            }
        }

        // 3. Lush rounded weeping canopy of entropica:frost_willow_leaves
        //    - Crown cap (topY + 1): radius 2.15 rounded dome cap
        //    - Upper wide overhang tier (topY): radius 3.15 rounded canopy so outer rim overhangs topY - 1!
        //    - Inner weeping core tier (topY - 1): radius 2.35 solid inner leaf support ring so every outer
        //      overhang cell at (topY - 1) has BOTH a leaf directly above (for UP=true) AND an inward leaf
        //      neighbor (for inwardFace=true)!
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.hypot(dx, dz) <= 2.15D) {
                    placeLeafIfClear(level, new BlockPos(crownX + dx, topY + 1, crownZ + dz), leavesState);
                }
            }
        }

        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                double dist = Math.hypot(dx, dz);
                if (dist <= 3.15D) {
                    placeLeafIfClear(level, new BlockPos(crownX + dx, topY, crownZ + dz), leavesState);
                }
                if (dist <= 2.35D) {
                    placeLeafIfClear(level, new BlockPos(crownX + dx, topY - 1, crownZ + dz), leavesState);
                }
            }
        }

        // Soft drooping leaf tufts on inner corners at topY - 2
        for (int sx : new int[]{-1, 1}) {
            for (int sz : new int[]{-1, 1}) {
                if (random.nextFloat() < 0.65F) {
                    placeLeafIfClear(level, new BlockPos(crownX + sx * 2, topY - 2, crownZ + sz * 2), leavesState);
                }
            }
        }

        // 4. Cling 1..2 entropica:vapor_weaver_lichen faces onto exposed lower trunk sides
        int targetLichen = 1 + random.nextInt(2); // 1..2 faces
        int placedLichen = 0;
        for (BlockPos trunkNode : lowerTrunkNodes) {
            if (placedLichen >= targetLichen) {
                break;
            }
            for (Direction outwardDir : Direction.Plane.HORIZONTAL.shuffledCopy(random)) {
                if (placedLichen >= targetLichen) {
                    break;
                }
                BlockPos lichenPos = trunkNode.relative(outwardDir);
                BlockState existing = level.getBlockState(lichenPos);
                if (!existing.isAir() && !existing.is(Blocks.SNOW)) {
                    continue;
                }
                BlockState lichenState = makeSingleFaceLichenState(vaporLichenBlock, outwardDir.getOpposite());
                if (lichenState != null && lichenState.canSurvive(level, lichenPos)) {
                    level.setBlock(lichenPos, lichenState, 2);
                    placedLichen++;
                }
            }
        }

        // 5. Hang 2..4 block vertical curtains of entropica:frost_willow_vines around the outer canopy edge!
        //    Around the radius-3 overhang ring (e.g. (±3, -1..1) and (-1..1, ±3) at y = topY - 1):
        //    - Above at y = topY is a frost_willow_leaves overhang block -> VineBlock.UP = true
        //    - Inward at y = topY - 1 is an inner frost_willow_leaves block -> VineBlock.getPropertyForFace(inwardDir) = true
        //    - Lower segments at y = topY - 2..topY - curtainLen inherit horizontal face support from the vine above!
        for (Direction outwardDir : Direction.Plane.HORIZONTAL) {
            Direction inwardDir = outwardDir.getOpposite();
            Direction sideDir = outwardDir.getClockWise();

            for (int lateral = -2; lateral <= 2; lateral++) {
                // Skip occasional lateral slots for organic natural weeping spacing while keeping lush curtains
                if (Math.abs(lateral) == 2 && random.nextFloat() < 0.45F) {
                    continue;
                }
                if (Math.abs(lateral) <= 1 && random.nextFloat() < 0.20F) {
                    continue;
                }

                // Find outermost air cell at y = topY - 1 backed inward by a leaf/log block
                int stepOut = (Math.abs(lateral) == 2) ? 2 : 3;
                int vx = crownX + outwardDir.getStepX() * stepOut + sideDir.getStepX() * lateral;
                int vz = crownZ + outwardDir.getStepZ() * stepOut + sideDir.getStepZ() * lateral;
                BlockPos startVinePos = new BlockPos(vx, topY - 1, vz);

                if (!level.getBlockState(startVinePos).isAir() && !level.getBlockState(startVinePos).is(Blocks.SNOW)) {
                    // Step 1 block further out if (vx, topY - 1, vz) itself was occupied by an inner leaf
                    startVinePos = startVinePos.relative(outwardDir);
                }

                hangSupportedWillowVineCurtain(level, startVinePos, inwardDir, willowVinesBlock, 2 + random.nextInt(3));
            }
        }

        return true;
    }

    /**
     * Places a {@code 2..4}-block vertical {@code VineBlock} curtain starting at {@code topPos},
     * setting {@code VineBlock.getPropertyForFace(inwardDir) = true} on every segment (and
     * {@code VineBlock.UP = true} on {@code topPos} when backed overhead by leaves/solid face)
     * so {@code VineBlock.canSurvive} is 100% satisfied without any vine popping off.
     */
    public static int hangSupportedWillowVineCurtain(
            WorldGenLevel level,
            BlockPos topPos,
            Direction inwardDir,
            Block vineBlock,
            int desiredLength
    ) {
        if (inwardDir.getAxis() == Direction.Axis.Y || desiredLength <= 0) {
            return 0;
        }

        BlockPos inwardPos = topPos.relative(inwardDir);
        if (!VineBlock.isAcceptableNeighbour(level, inwardPos, inwardDir)) {
            return 0;
        }

        BooleanProperty inwardProp = VineBlock.getPropertyForFace(inwardDir);
        BlockState baseVine = cleanVineState(vineBlock);
        if (!baseVine.hasProperty(inwardProp)) {
            return 0;
        }

        boolean hasOverheadSupport = VineBlock.isAcceptableNeighbour(level, topPos.above(), Direction.DOWN);
        int placed = 0;

        for (int i = 0; i < desiredLength; i++) {
            BlockPos curPos = topPos.below(i);
            if (curPos.getY() <= level.getMinY() + 2) {
                break;
            }
            BlockState existing = level.getBlockState(curPos);
            if (!existing.isAir() && !existing.is(Blocks.SNOW)) {
                break;
            }
            if (!existing.getFluidState().isEmpty()) {
                break;
            }

            BlockState segState = baseVine.setValue(inwardProp, true);
            if (i == 0 && hasOverheadSupport && segState.hasProperty(VineBlock.UP)) {
                segState = segState.setValue(VineBlock.UP, true);
            }

            if (!segState.canSurvive(level, curPos)) {
                break;
            }
            level.setBlock(curPos, segState, 2);
            placed++;
        }

        return placed;
    }

    public static BlockState cleanVineState(Block vineBlock) {
        BlockState state = vineBlock.defaultBlockState();
        for (BooleanProperty prop : new BooleanProperty[]{
                VineBlock.UP, VineBlock.NORTH, VineBlock.SOUTH, VineBlock.EAST, VineBlock.WEST
        }) {
            if (state.hasProperty(prop)) {
                state = state.setValue(prop, false);
            }
        }
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        return state;
    }

    public static BlockState makeSingleFaceLichenState(Block lichenBlock, Direction attachFace) {
        BlockState state = lichenBlock.defaultBlockState();
        for (Direction dir : Direction.values()) {
            BooleanProperty faceProp = MultifaceBlock.getFaceProperty(dir);
            if (state.hasProperty(faceProp)) {
                state = state.setValue(faceProp, false);
            }
        }
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        BooleanProperty targetProp = MultifaceBlock.getFaceProperty(attachFace);
        if (!state.hasProperty(targetProp)) {
            return null;
        }
        return state.setValue(targetProp, true);
    }

    private static Block resolveVitaeSpringStone() {
        Optional<Block> regOpt = BuiltInRegistries.BLOCK.getOptional(VITAE_SPRING_STONE_ID);
        if (regOpt.isPresent()) {
            return regOpt.get();
        }
        try {
            if (ModBlocks.VITAE_SPRING_STONE.isPresent()) {
                return ModBlocks.VITAE_SPRING_STONE.get();
            }
        } catch (Throwable ignored) {
        }
        return Blocks.CALCITE;
    }

    private static BlockPos findWillowGround(
            WorldGenLevel level,
            int x,
            int z,
            int hintY,
            Block springStoneBlock
    ) {
        int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
        int startY = Math.max(topY, hintY + 3);
        int minY = Math.max(61, startY - 8);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int y = startY; y >= minY; y--) {
            cursor.set(x, y, z);
            BlockState state = level.getBlockState(cursor);
            if (!state.getFluidState().isEmpty()) {
                return null;
            }
            if (state.isAir() || state.is(Blocks.SNOW) || canTreeReplace(state)) {
                continue;
            }
            if (isValidWillowGround(state, springStoneBlock)) {
                BlockState above = level.getBlockState(cursor.above());
                if (canTreeReplace(above) && above.getFluidState().isEmpty()) {
                    return cursor.immutable();
                }
            }
            return null;
        }
        return null;
    }

    private static boolean isValidWillowGround(BlockState state, Block springStoneBlock) {
        if (EntropicaFloraGroundHelper.isIceOrSnowLayer(state)) {
            return false;
        }
        return state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(springStoneBlock);
    }

    private static boolean canTreeReplace(BlockState state) {
        return state.isAir()
                || state.is(Blocks.SNOW)
                || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.REPLACEABLE)
                || state.is(BlockTags.FLOWERS);
    }

    private static void placeLeafIfClear(WorldGenLevel level, BlockPos pos, BlockState leavesState) {
        BlockState existing = level.getBlockState(pos);
        if (existing.isAir() || existing.is(Blocks.SNOW)) {
            level.setBlock(pos, leavesState, 2);
        }
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
