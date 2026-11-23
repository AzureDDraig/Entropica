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
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Generates small wind-swept clumps ({@code 1-2} trees) of twisted, lightning-split
 * {@code entropica:fulgurite_pine_log} trunks ({@code 4-7} blocks tall with {@code 1-2} angled
 * side branch stubs) and sparse {@code entropica:fulgurite_pine_leaves} boughs ({@code persistent=true}),
 * anchored by {@code 1-3} vitrified {@code entropica:fulgurite_ice} root scars and {@code 1-2}
 * {@code entropica:cryo_static_shrub} accents in {@code 2.7 Static Ice-Plains}.
 */
public class FulguritePineTreeFeature extends Feature<NoneFeatureConfiguration> {

    private static final ResourceLocation FULGURITE_PINE_LOG_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "fulgurite_pine_log");
    private static final ResourceLocation FULGURITE_PINE_LEAVES_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "fulgurite_pine_leaves");
    private static final ResourceLocation FULGURITE_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "fulgurite_ice");
    private static final ResourceLocation CRYO_STATIC_SHRUB_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "cryo_static_shrub");

    public FulguritePineTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        Block logBlock = BuiltInRegistries.BLOCK.getOptional(FULGURITE_PINE_LOG_ID).orElse(Blocks.SPRUCE_LOG);
        Block leavesBlock = BuiltInRegistries.BLOCK.getOptional(FULGURITE_PINE_LEAVES_ID).orElse(Blocks.SPRUCE_LEAVES);
        Block fulguriteIceBlock = BuiltInRegistries.BLOCK.getOptional(FULGURITE_ICE_ID).orElse(Blocks.PACKED_ICE);
        Optional<Block> shrubBlockOpt = BuiltInRegistries.BLOCK.getOptional(CRYO_STATIC_SHRUB_ID);

        BlockState persistentLeaves = makePersistentLeaves(leavesBlock);
        BlockState fulguriteIce = fulguriteIceBlock.defaultBlockState();

        BlockPos primaryGround = findSnowfieldGround(level, origin.getX(), origin.getZ(), origin.getY(), fulguriteIceBlock);
        if (primaryGround == null) {
            return false;
        }

        int treeCount = random.nextFloat() < 0.42F ? 2 : 1;
        boolean placedAny = placeSingleLightningPine(
                level, origin, primaryGround, random, logBlock, persistentLeaves, fulguriteIce, shrubBlockOpt);

        if (treeCount == 2) {
            Direction offsetDir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            Direction perpDir = offsetDir.getClockWise();
            int dist = 3 + random.nextInt(2); // 3..4 blocks away
            int sideShift = random.nextInt(3) - 1; // -1..+1
            int secX = origin.getX() + offsetDir.getStepX() * dist + perpDir.getStepX() * sideShift;
            int secZ = origin.getZ() + offsetDir.getStepZ() * dist + perpDir.getStepZ() * sideShift;

            if (Math.abs(secX - origin.getX()) <= 4 && Math.abs(secZ - origin.getZ()) <= 4) {
                BlockPos secondaryGround = findSnowfieldGround(level, secX, secZ, primaryGround.getY(), fulguriteIceBlock);
                if (secondaryGround != null && Math.abs(secondaryGround.getY() - primaryGround.getY()) <= 3) {
                    placedAny |= placeSingleLightningPine(
                            level, origin, secondaryGround, random, logBlock, persistentLeaves, fulguriteIce, shrubBlockOpt);
                }
            }
        }

        return placedAny;
    }

    private static boolean placeSingleLightningPine(
            WorldGenLevel level,
            BlockPos featureOrigin,
            BlockPos groundPos,
            RandomSource random,
            Block logBlock,
            BlockState persistentLeaves,
            BlockState fulguriteIce,
            Optional<Block> shrubBlockOpt
    ) {
        int trunkHeight = 4 + random.nextInt(4); // 4..7 blocks tall
        int baseX = groundPos.getX();
        int baseY = groundPos.getY() + 1;
        int baseZ = groundPos.getZ();

        if (baseY + trunkHeight + 2 >= level.getMaxY() - 1) {
            return false;
        }

        // Check vertical clearance so trees never clip into structures or cliffs
        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
        for (int dy = 0; dy < trunkHeight; dy++) {
            checkPos.set(baseX, baseY + dy, baseZ);
            if (!canTreeReplace(level.getBlockState(checkPos))) {
                return false;
            }
        }

        // Reinforce snow/ice directly beneath trunk base
        if (canRootVitrify(level.getBlockState(groundPos))) {
            level.setBlock(groundPos, Blocks.PACKED_ICE.defaultBlockState(), 2);
        }

        Direction bendDir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int bendStart = 2 + random.nextInt(Math.max(1, trunkHeight - 2)); // mid-trunk lightning/wind twist
        boolean splitForkCrown = trunkHeight >= 5 && random.nextFloat() < 0.55F;

        int curX = baseX;
        int curZ = baseZ;
        List<BlockPos> trunkNodes = new ArrayList<>(trunkHeight + 1);

        for (int h = 0; h < trunkHeight; h++) {
            if (h == bendStart) {
                int nextX = curX + bendDir.getStepX();
                int nextZ = curZ + bendDir.getStepZ();
                if (withinBounds(featureOrigin, nextX, nextZ, 5)) {
                    // Bridge elbow log so the lightning-bent trunk remains visually connected
                    BlockPos elbowPos = new BlockPos(curX, baseY + h, curZ);
                    if (canTreeReplace(level.getBlockState(elbowPos))) {
                        level.setBlock(elbowPos, logWithAxis(logBlock, Direction.Axis.Y), 2);
                        trunkNodes.add(elbowPos);
                    }
                    curX = nextX;
                    curZ = nextZ;
                }
            }

            BlockPos logPos = new BlockPos(curX, baseY + h, curZ);
            if (withinBounds(featureOrigin, curX, curZ, 6) && canTreeReplace(level.getBlockState(logPos))) {
                level.setBlock(logPos, logWithAxis(logBlock, Direction.Axis.Y), 2);
                trunkNodes.add(logPos);
            }
        }

        if (trunkNodes.size() < 3) {
            return false;
        }

        // Optional lightning-split forked snag near the crown
        if (splitForkCrown) {
            Direction forkDir = bendDir.getOpposite();
            int forkY = baseY + trunkHeight - 2;
            int fx = baseX + forkDir.getStepX();
            int fz = baseZ + forkDir.getStepZ();
            if (withinBounds(featureOrigin, fx, fz, 6)) {
                BlockPos forkArm = new BlockPos(fx, forkY, fz);
                if (canTreeReplace(level.getBlockState(forkArm))) {
                    level.setBlock(forkArm, logWithAxis(logBlock, forkDir.getAxis()), 2);
                    BlockPos forkTip = forkArm.above();
                    if (canTreeReplace(level.getBlockState(forkTip))) {
                        level.setBlock(forkTip, logWithAxis(logBlock, Direction.Axis.Y), 2);
                    }
                    placeSparseBoughCluster(level, featureOrigin, forkArm.above(), persistentLeaves, random, 0.55F);
                }
            }
        }

        // 1-2 angled side branch stubs jutting outward from the middle/upper trunk
        int branchStubs = 1 + random.nextInt(2); // 1..2
        List<Direction> stubDirs = Direction.Plane.HORIZONTAL.shuffledCopy(random);
        int placedStubs = 0;
        for (Direction stubDir : stubDirs) {
            if (placedStubs >= branchStubs) {
                break;
            }
            int nodeIdx = Mth.clamp(1 + placedStubs + random.nextInt(Math.max(1, trunkNodes.size() - 2)), 1, trunkNodes.size() - 2);
            BlockPos anchor = trunkNodes.get(nodeIdx);
            BlockPos stubPos = anchor.relative(stubDir);
            if (!withinBounds(featureOrigin, stubPos.getX(), stubPos.getZ(), 6)) {
                continue;
            }
            if (canTreeReplace(level.getBlockState(stubPos))) {
                level.setBlock(stubPos, logWithAxis(logBlock, stubDir.getAxis()), 2);
                // Occasional upward-angled tip on longer lightning pine limbs
                if (trunkHeight >= 6 && random.nextFloat() < 0.45F) {
                    BlockPos angledTip = stubPos.relative(stubDir).above();
                    if (withinBounds(featureOrigin, angledTip.getX(), angledTip.getZ(), 6)
                            && canTreeReplace(level.getBlockState(angledTip))) {
                        level.setBlock(angledTip, logWithAxis(logBlock, Direction.Axis.Y), 2);
                        placeSparseBoughCluster(level, featureOrigin, angledTip, persistentLeaves, random, 0.65F);
                    }
                }
                placeSparseBoughCluster(level, featureOrigin, stubPos, persistentLeaves, random, 0.72F);
                placedStubs++;
            }
        }

        // Sparse wind-sheared boreal bough tiers along upper half of trunk + crown tip
        BlockPos topNode = trunkNodes.get(trunkNodes.size() - 1);
        for (int i = Math.max(1, trunkNodes.size() / 2); i < trunkNodes.size(); i++) {
            BlockPos node = trunkNodes.get(i);
            float density = (i == trunkNodes.size() - 1) ? 0.80F : 0.58F;
            placeSparseBoughCluster(level, featureOrigin, node, persistentLeaves, random, density);
        }
        // Crown needle spire (leaves or exposed lightning-charred tip)
        BlockPos crownTip = topNode.above();
        if (withinBounds(featureOrigin, crownTip.getX(), crownTip.getZ(), 6)
                && canFoliageReplace(level.getBlockState(crownTip))) {
            level.setBlock(crownTip, persistentLeaves, 2);
        }

        // Root grounding scar: 1-3 vitrified fulgurite_ice blocks embedded around the trunk roots
        int targetFulguriteRoots = 1 + random.nextInt(3); // 1..3
        int placedFulguriteRoots = 0;
        for (Direction rootDir : Direction.Plane.HORIZONTAL.shuffledCopy(random)) {
            if (placedFulguriteRoots >= targetFulguriteRoots) {
                break;
            }
            int rx = baseX + rootDir.getStepX();
            int rz = baseZ + rootDir.getStepZ();
            if (!withinBounds(featureOrigin, rx, rz, 6)) {
                continue;
            }
            BlockPos rootSurface = findNearRootSurface(level, rx, groundPos.getY(), rz);
            if (rootSurface != null && canRootVitrify(level.getBlockState(rootSurface))) {
                level.setBlock(rootSurface, fulguriteIce, 2);
                BlockPos aboveRoot = rootSurface.above();
                if (level.getBlockState(aboveRoot).is(Blocks.SNOW)) {
                    level.setBlock(aboveRoot, Blocks.AIR.defaultBlockState(), 2);
                }
                placedFulguriteRoots++;
            }
        }

        // Root flora: 1-2 cryo_static_shrub blocks nestled in snow/ice around the tree base
        if (shrubBlockOpt.isPresent()) {
            BlockState shrubState = shrubBlockOpt.get().defaultBlockState();
            int targetShrubs = 1 + random.nextInt(2); // 1..2
            int placedShrubs = 0;
            for (int attempt = 0; attempt < 10 && placedShrubs < targetShrubs; attempt++) {
                int ox = random.nextInt(5) - 2;
                int oz = random.nextInt(5) - 2;
                if (ox == 0 && oz == 0) {
                    continue;
                }
                int sx = baseX + ox;
                int sz = baseZ + oz;
                if (!withinBounds(featureOrigin, sx, sz, 6)) {
                    continue;
                }
                BlockPos soilPos = findNearRootSurface(level, sx, groundPos.getY(), sz);
                if (soilPos == null) {
                    continue;
                }
                BlockState soilState = level.getBlockState(soilPos);
                if (ddraig.net.entropica.block.EntropicaFloraGroundHelper.isIceOrSnowLayer(soilState)
                        || soilState.is(fulguriteIce.getBlock())) {
                    continue;
                }
                BlockPos shrubPos = soilPos.above();
                BlockState existingAbove = level.getBlockState(shrubPos);
                if (!existingAbove.isAir() && !existingAbove.is(Blocks.SNOW)) {
                    continue;
                }
                if (shrubState.canSurvive(level, shrubPos)) {
                    level.setBlock(shrubPos, shrubState, 2);
                    placedShrubs++;
                }
            }
        }

        return true;
    }

    private static void placeSparseBoughCluster(
            WorldGenLevel level,
            BlockPos featureOrigin,
            BlockPos center,
            BlockState leavesState,
            RandomSource random,
            float cardinalChance
    ) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (random.nextFloat() <= cardinalChance) {
                BlockPos leafPos = center.relative(dir);
                if (withinBounds(featureOrigin, leafPos.getX(), leafPos.getZ(), 7)
                        && canFoliageReplace(level.getBlockState(leafPos))) {
                    level.setBlock(leafPos, leavesState, 2);
                }
            }
        }
        // Occasional diagonal needle tuft for organic asymmetric pine silhouette
        if (random.nextFloat() < cardinalChance * 0.45F) {
            Direction d1 = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            Direction d2 = d1.getClockWise();
            BlockPos diagPos = center.relative(d1).relative(d2);
            if (withinBounds(featureOrigin, diagPos.getX(), diagPos.getZ(), 7)
                    && canFoliageReplace(level.getBlockState(diagPos))) {
                level.setBlock(diagPos, leavesState, 2);
            }
        }
    }

    private static BlockPos findSnowfieldGround(WorldGenLevel level, int x, int z, int hintY, Block fulguriteIceBlock) {
        int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
        int startY = Math.max(topY, hintY + 3);
        int minY = Math.max(62, startY - 8);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int y = startY; y >= minY; y--) {
            cursor.set(x, y, z);
            BlockState state = level.getBlockState(cursor);
            if (state.is(Blocks.WATER) || state.is(Blocks.LAVA)) {
                return null;
            }
            if (isValidPineSubstrate(state, fulguriteIceBlock)) {
                BlockState above = level.getBlockState(cursor.above());
                if (canTreeReplace(above) && !above.is(Blocks.WATER)) {
                    return cursor.immutable();
                }
            }
        }
        return null;
    }

    private static BlockPos findNearRootSurface(WorldGenLevel level, int x, int refY, int z) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = 1; dy >= -2; dy--) {
            cursor.set(x, refY + dy, z);
            BlockState state = level.getBlockState(cursor);
            BlockState above = level.getBlockState(cursor.above());
            if (!state.isAir() && !state.is(Blocks.SNOW) && !state.is(Blocks.WATER)
                    && (above.isAir() || above.is(Blocks.SNOW))) {
                return cursor.immutable();
            }
        }
        return null;
    }

    private static boolean isValidPineSubstrate(BlockState state, Block fulguriteIceBlock) {
        return state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.ICE)
                || state.is(fulguriteIceBlock)
                || state.is(BlockTags.DIRT)
                || state.is(Blocks.GRAVEL);
    }

    private static boolean canRootVitrify(BlockState state) {
        return state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.ICE)
                || state.is(Blocks.POWDER_SNOW)
                || state.is(BlockTags.DIRT);
    }

    private static boolean canTreeReplace(BlockState state) {
        return state.isAir()
                || state.is(Blocks.SNOW)
                || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.REPLACEABLE);
    }

    private static boolean canFoliageReplace(BlockState state) {
        return state.isAir() || state.is(Blocks.SNOW);
    }

    private static boolean withinBounds(BlockPos origin, int x, int z, int maxHorizontal) {
        return Math.abs(x - origin.getX()) <= maxHorizontal && Math.abs(z - origin.getZ()) <= maxHorizontal;
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
