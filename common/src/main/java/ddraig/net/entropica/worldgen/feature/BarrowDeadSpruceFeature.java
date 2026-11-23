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
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
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
 * Generates sparse twisted dead {@link Blocks#SPRUCE_LOG} trunks ({@code 5-9} blocks tall)
 * in {@code 2.8 Barrow-Mound Tundra} with bare branch stubs, {@link Blocks#SNOW} caps,
 * directional {@code entropica:barrow_fungal_shelf_cap} bracket fungi clinging to the vertical trunk,
 * and {@code grave_moss}, {@code rime_cap_mushroom}, and {@code crypt_grave_root} clustered around the roots.
 */
public class BarrowDeadSpruceFeature extends Feature<NoneFeatureConfiguration> {

    private static final int MIN_GROUND_Y = 62;

    private static final ResourceLocation NECROTIC_SOIL_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "necrotic_soil");
    private static final ResourceLocation BARROW_MOSS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "barrow_moss");
    private static final ResourceLocation BARROW_FUNGAL_SHELF_CAP_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "barrow_fungal_shelf_cap");
    private static final ResourceLocation GRAVE_MOSS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "grave_moss");
    private static final ResourceLocation RIME_CAP_MUSHROOM_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "rime_cap_mushroom");
    private static final ResourceLocation CRYPT_GRAVE_ROOT_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "crypt_grave_root");

    private record ShelfCandidate(BlockPos trunkPos, Direction outwardFacing) {}

    public BarrowDeadSpruceFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        Block necroticSoilBlock = BuiltInRegistries.BLOCK.getOptional(NECROTIC_SOIL_ID).orElse(Blocks.PODZOL);
        Block barrowMossBlock = BuiltInRegistries.BLOCK.getOptional(BARROW_MOSS_ID).orElse(Blocks.MOSS_BLOCK);
        Optional<Block> shelfCapOpt = BuiltInRegistries.BLOCK.getOptional(BARROW_FUNGAL_SHELF_CAP_ID);
        Optional<Block> graveMossOpt = BuiltInRegistries.BLOCK.getOptional(GRAVE_MOSS_ID);
        Optional<Block> rimeCapOpt = BuiltInRegistries.BLOCK.getOptional(RIME_CAP_MUSHROOM_ID);
        Optional<Block> graveRootOpt = BuiltInRegistries.BLOCK.getOptional(CRYPT_GRAVE_ROOT_ID);

        BlockPos groundPos = findTundraGround(level, origin.getX(), origin.getZ(), origin.getY(),
                necroticSoilBlock, barrowMossBlock);
        if (groundPos == null) {
            return false;
        }

        int trunkHeight = 5 + random.nextInt(5); // 5..9 blocks tall
        int baseX = groundPos.getX();
        int baseY = groundPos.getY() + 1;
        int baseZ = groundPos.getZ();

        if (baseY + trunkHeight + 2 >= level.getMaxY() - 1) {
            return false;
        }

        // Check vertical clearance so dead spruces never clip into barrows or tombs
        BlockPos.MutableBlockPos checkCursor = new BlockPos.MutableBlockPos();
        for (int dy = 0; dy <= trunkHeight; dy++) {
            checkCursor.set(baseX, baseY + dy, baseZ);
            if (!canTreeReplace(level.getBlockState(checkCursor))) {
                return false;
            }
        }

        BlockState necroticSoil = necroticSoilBlock.defaultBlockState();
        BlockState spruceLogY = Blocks.SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState strippedTopY = Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState snowCap = Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1);

        // 1. Anchor trunk base into necrotic soil + 1-2 gnarled surface root buttresses
        level.setBlock(groundPos, necroticSoil, 2);
        int rootButtresses = 1 + random.nextInt(2);
        int placedRoots = 0;
        for (Direction rootDir : Direction.Plane.HORIZONTAL.shuffledCopy(random)) {
            if (placedRoots >= rootButtresses) {
                break;
            }
            BlockPos rootSoil = findLocalRootGround(level, baseX + rootDir.getStepX(), groundPos.getY(),
                    baseZ + rootDir.getStepZ(), necroticSoilBlock, barrowMossBlock);
            if (rootSoil != null) {
                level.setBlock(rootSoil, necroticSoil, 2);
                BlockPos rootLogPos = rootSoil.above();
                if (canTreeReplace(level.getBlockState(rootLogPos))) {
                    level.setBlock(rootLogPos, Blocks.SPRUCE_WOOD.defaultBlockState()
                            .setValue(RotatedPillarBlock.AXIS, rootDir.getAxis()), 2);
                    if (snowCap.canSurvive(level, rootLogPos.above()) && level.getBlockState(rootLogPos.above()).isAir()
                            && random.nextBoolean()) {
                        level.setBlock(rootLogPos.above(), snowCap, 2);
                    }
                    placedRoots++;
                }
            }
        }

        // 2. Build twisted 5-9 block dead spruce trunk (with optional wind-bent crook mid-trunk)
        Direction twistDir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int twistHeight = (trunkHeight >= 6 && random.nextFloat() < 0.65F)
                ? (2 + random.nextInt(trunkHeight - 3))
                : -1;

        int curX = baseX;
        int curZ = baseZ;
        List<BlockPos> verticalTrunkLogs = new ArrayList<>(trunkHeight + 1);

        for (int h = 0; h < trunkHeight; h++) {
            if (h == twistHeight) {
                // Place connected vertical elbow log before stepping 1 block sideways
                BlockPos elbowPos = new BlockPos(curX, baseY + h, curZ);
                if (canTreeReplace(level.getBlockState(elbowPos))) {
                    level.setBlock(elbowPos, spruceLogY, 2);
                    verticalTrunkLogs.add(elbowPos);
                }
                int nextX = curX + twistDir.getStepX();
                int nextZ = curZ + twistDir.getStepZ();
                if (withinBounds(origin, nextX, nextZ, 5)) {
                    curX = nextX;
                    curZ = nextZ;
                }
            }

            BlockPos logPos = new BlockPos(curX, baseY + h, curZ);
            if (withinBounds(origin, curX, curZ, 5) && canTreeReplace(level.getBlockState(logPos))) {
                boolean weatheredCrownTip = (h == trunkHeight - 1 && random.nextFloat() < 0.45F);
                level.setBlock(logPos, weatheredCrownTip ? strippedTopY : spruceLogY, 2);
                verticalTrunkLogs.add(logPos);
            }
        }

        if (verticalTrunkLogs.size() < 4) {
            return false;
        }

        // Snow cap on the dead trunk apex
        BlockPos topLog = verticalTrunkLogs.get(verticalTrunkLogs.size() - 1);
        BlockPos apexSnow = topLog.above();
        if (level.getBlockState(apexSnow).isAir() && snowCap.canSurvive(level, apexSnow)) {
            level.setBlock(apexSnow, snowCap, 2);
        }

        // 3. Sprout 2-4 bare dead branch stubs along middle and upper trunk with snow caps
        int targetBranches = 2 + random.nextInt(3); // 2..4
        List<Direction> branchDirs = Direction.Plane.HORIZONTAL.shuffledCopy(random);
        int placedBranches = 0;
        for (int i = 0; i < branchDirs.size() && placedBranches < targetBranches; i++) {
            Direction bDir = branchDirs.get(i);
            int minIdx = Math.max(2, verticalTrunkLogs.size() / 3);
            int maxIdx = verticalTrunkLogs.size() - 2;
            if (maxIdx < minIdx) {
                break;
            }
            int idx = Mth.clamp(minIdx + ((i * 2) % (maxIdx - minIdx + 1)), minIdx, maxIdx);
            BlockPos anchor = verticalTrunkLogs.get(idx);
            BlockPos stub1 = anchor.relative(bDir);
            if (!withinBounds(origin, stub1.getX(), stub1.getZ(), 6) || !canTreeReplace(level.getBlockState(stub1))) {
                continue;
            }

            BlockState branchLog = Blocks.SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, bDir.getAxis());
            level.setBlock(stub1, branchLog, 2);
            if (level.getBlockState(stub1.above()).isAir() && snowCap.canSurvive(level, stub1.above()) && random.nextFloat() < 0.75F) {
                level.setBlock(stub1.above(), snowCap, 2);
            }

            // Occasional 2-block crooked dead limb on taller spruces
            if (trunkHeight >= 7 && placedBranches == 0 && random.nextFloat() < 0.50F) {
                BlockPos stub2 = stub1.relative(bDir).above();
                if (withinBounds(origin, stub2.getX(), stub2.getZ(), 6) && canTreeReplace(level.getBlockState(stub2))) {
                    level.setBlock(stub2, spruceLogY, 2);
                    if (level.getBlockState(stub2.above()).isAir() && snowCap.canSurvive(level, stub2.above())) {
                        level.setBlock(stub2.above(), snowCap, 2);
                    }
                }
            }
            placedBranches++;
        }

        // 4. Cling 1-3 directional barrow_fungal_shelf_cap brackets to exposed vertical spruce_log faces
        if (shelfCapOpt.isPresent()) {
            Block shelfBlock = shelfCapOpt.get();
            List<ShelfCandidate> candidates = new ArrayList<>();
            int maxShelfNode = Math.max(1, verticalTrunkLogs.size() - 2);
            for (int idx = 0; idx <= maxShelfNode; idx++) {
                BlockPos logNode = verticalTrunkLogs.get(idx);
                for (Direction outward : Direction.Plane.HORIZONTAL) {
                    candidates.add(new ShelfCandidate(logNode, outward));
                }
            }

            // Fisher-Yates shuffle
            for (int i = candidates.size() - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                ShelfCandidate tmp = candidates.get(i);
                candidates.set(i, candidates.get(j));
                candidates.set(j, tmp);
            }

            int targetShelves = 1 + random.nextInt(3); // 1..3
            int placedShelves = 0;
            for (ShelfCandidate cand : candidates) {
                if (placedShelves >= targetShelves) {
                    break;
                }
                BlockPos shelfPos = cand.trunkPos().relative(cand.outwardFacing());
                if (!withinBounds(origin, shelfPos.getX(), shelfPos.getZ(), 6)) {
                    continue;
                }
                BlockState existing = level.getBlockState(shelfPos);
                if (!existing.isAir() && !existing.is(Blocks.SNOW)) {
                    continue;
                }

                BlockState shelfState = withHorizontalFacing(shelfBlock.defaultBlockState(), cand.outwardFacing());
                if (shelfState.canSurvive(level, shelfPos)) {
                    level.setBlock(shelfPos, shelfState, 2);
                    placedShelves++;
                }
            }
        }

        // 5. Scatter 2-5 grave_moss, rime_cap_mushroom, and crypt_grave_root patches around the roots
        List<Block> rootFloraChoices = new ArrayList<>(3);
        graveMossOpt.ifPresent(rootFloraChoices::add);
        rimeCapOpt.ifPresent(rootFloraChoices::add);
        graveRootOpt.ifPresent(rootFloraChoices::add);

        if (!rootFloraChoices.isEmpty()) {
            int targetRootFlora = 2 + random.nextInt(4); // 2..5
            int placedFlora = 0;
            for (int attempt = 0; attempt < 16 && placedFlora < targetRootFlora; attempt++) {
                int ox = random.nextInt(5) - 2;
                int oz = random.nextInt(5) - 2;
                if (ox == 0 && oz == 0) {
                    continue;
                }
                int fx = baseX + ox;
                int fz = baseZ + oz;
                if (!withinBounds(origin, fx, fz, 6)) {
                    continue;
                }
                BlockPos soilPos = findLocalRootGround(level, fx, groundPos.getY(), fz, necroticSoilBlock, barrowMossBlock);
                if (soilPos == null) {
                    continue;
                }
                BlockPos plantPos = soilPos.above();
                BlockState aboveState = level.getBlockState(plantPos);
                if (!aboveState.isAir() && !aboveState.is(Blocks.SNOW)) {
                    continue;
                }

                Block chosenFlora = rootFloraChoices.get(random.nextInt(rootFloraChoices.size()));
                BlockState floraState = chosenFlora.defaultBlockState();
                if (!floraState.canSurvive(level, plantPos)) {
                    level.setBlock(soilPos, necroticSoil, 2);
                }
                if (floraState.canSurvive(level, plantPos)) {
                    level.setBlock(plantPos, floraState, 2);
                    placedFlora++;
                }
            }
        }

        return true;
    }

    private static BlockState withHorizontalFacing(BlockState state, Direction dir) {
        if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            return state.setValue(HorizontalDirectionalBlock.FACING, dir);
        }
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.setValue(BlockStateProperties.HORIZONTAL_FACING, dir);
        }
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return state.setValue(BlockStateProperties.FACING, dir);
        }
        return state;
    }

    private static BlockPos findTundraGround(
            WorldGenLevel level,
            int x,
            int z,
            int hintY,
            Block necroticSoil,
            Block barrowMoss
    ) {
        int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
        int startY = Math.max(topY, hintY + 3);
        int minY = Math.max(MIN_GROUND_Y, startY - 8);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int y = startY; y >= minY; y--) {
            cursor.set(x, y, z);
            BlockState state = level.getBlockState(cursor);
            if (state.is(Blocks.WATER) || state.is(Blocks.LAVA) || state.is(Blocks.ICE)) {
                return null;
            }
            if (isValidSpruceSubstrate(state, necroticSoil, barrowMoss)) {
                BlockState above = level.getBlockState(cursor.above());
                if (canTreeReplace(above) && !above.is(Blocks.WATER)) {
                    return cursor.immutable();
                }
            }
        }
        return null;
    }

    private static BlockPos findLocalRootGround(
            WorldGenLevel level,
            int x,
            int refY,
            int z,
            Block necroticSoil,
            Block barrowMoss
    ) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = 1; dy >= -2; dy--) {
            cursor.set(x, refY + dy, z);
            BlockState state = level.getBlockState(cursor);
            BlockState above = level.getBlockState(cursor.above());
            if (isValidSpruceSubstrate(state, necroticSoil, barrowMoss)
                    && (above.isAir() || above.is(Blocks.SNOW))) {
                return cursor.immutable();
            }
        }
        return null;
    }

    private static boolean isValidSpruceSubstrate(BlockState state, Block necroticSoil, Block barrowMoss) {
        return state.is(necroticSoil)
                || state.is(barrowMoss)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.PODZOL)
                || state.is(BlockTags.DIRT)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.PACKED_ICE);
    }

    private static boolean canTreeReplace(BlockState state) {
        return state.isAir()
                || state.is(Blocks.SNOW)
                || state.is(Blocks.POWDER_SNOW)
                || state.is(BlockTags.REPLACEABLE);
    }

    private static boolean withinBounds(BlockPos origin, int x, int z, int maxHorizontal) {
        return Math.abs(x - origin.getX()) <= maxHorizontal && Math.abs(z - origin.getZ()) <= maxHorizontal;
    }
}
