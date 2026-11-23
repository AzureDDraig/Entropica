package ddraig.net.entropica.worldgen.feature;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.block.EntropicaFloraGroundHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
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
 * Generates gnarled, crooked {@code 5..8}-block tall {@code Spore-Wood} trees for Biome #12
 * ({@code 2.12 Spore-Glazed Pack}):
 * <ul>
 *   <li>Flared {@code entropica:spore_wood} (all-bark) basal buttress roots</li>
 *   <li>Slightly leaning {@code entropica:spore_wood_log} trunk + {@code 2..3} short upper limbs</li>
 *   <li>Layered frosted dome canopy of {@code entropica:frozen_spore_leaves}</li>
 *   <li>{@code 1..3} horizontal {@code entropica:spore_fungal_shelf_cap} brackets attached to the trunk</li>
 *   <li>Strictly grows ONLY on {@code frozen_spore_moss}, {@code snow_block}, {@code dirt},
 *       {@code grass_block}, {@code coarse_dirt}, {@code podzol}, {@code mud}, {@code mycelium}, or
 *       {@code moss_block} (replacing thin {@link Blocks#SNOW} at base; NEVER on water or any ice!)</li>
 * </ul>
 */
public class SporeWoodTreeFeature extends Feature<NoneFeatureConfiguration> {

    private static final ResourceLocation SPORE_WOOD_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spore_wood");
    private static final ResourceLocation SPORE_WOOD_LOG_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spore_wood_log");
    private static final ResourceLocation FROZEN_SPORE_LEAVES_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "frozen_spore_leaves");
    private static final ResourceLocation SPORE_FUNGAL_SHELF_CAP_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spore_fungal_shelf_cap");
    private static final ResourceLocation FROZEN_SPORE_MOSS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "frozen_spore_moss");
    private static final ResourceLocation SPORE_GLAZED_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spore_glazed_ice");

    public SporeWoodTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        Block sporeWoodBlock = BuiltInRegistries.BLOCK.getOptional(SPORE_WOOD_ID).orElse(Blocks.SPRUCE_WOOD);
        Block sporeWoodLogBlock = BuiltInRegistries.BLOCK.getOptional(SPORE_WOOD_LOG_ID).orElse(Blocks.SPRUCE_LOG);
        Block frozenSporeLeavesBlock = BuiltInRegistries.BLOCK.getOptional(FROZEN_SPORE_LEAVES_ID).orElse(Blocks.SPRUCE_LEAVES);
        Optional<Block> shelfCapOpt = BuiltInRegistries.BLOCK.getOptional(SPORE_FUNGAL_SHELF_CAP_ID);
        Optional<Block> frozenSporeMossOpt = BuiltInRegistries.BLOCK.getOptional(FROZEN_SPORE_MOSS_ID);
        Optional<Block> sporeGlazedIceOpt = BuiltInRegistries.BLOCK.getOptional(SPORE_GLAZED_ICE_ID);

        BlockPos groundPos = findValidTreeGround(level, origin.getX(), origin.getZ(), origin.getY(),
                frozenSporeMossOpt, sporeGlazedIceOpt);
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

        Direction leanDir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int bendStart = Math.max(2, trunkHeight / 2);

        // Verify trunk & crown headroom
        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
        for (int dy = 0; dy <= trunkHeight + 1; dy++) {
            int cx = baseX + (dy >= bendStart ? leanDir.getStepX() : 0);
            int cz = baseZ + (dy >= bendStart ? leanDir.getStepZ() : 0);
            int radius = (dy >= trunkHeight - 2) ? 2 : 0;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    checkPos.set(cx + dx, baseY + dy, cz + dz);
                    if (!canTreeReplace(level.getBlockState(checkPos))) {
                        return false;
                    }
                }
            }
        }

        if (level.getBlockState(groundPos).is(Blocks.GRASS_BLOCK)) {
            level.setBlock(groundPos, Blocks.DIRT.defaultBlockState(), 2);
        }

        BlockState allBarkWood = logWithAxis(sporeWoodBlock, Direction.Axis.Y);
        BlockState verticalLog = logWithAxis(sporeWoodLogBlock, Direction.Axis.Y);
        BlockState leavesState = makePersistentLeaves(frozenSporeLeavesBlock);

        // 1. Flared all-bark entropica:spore_wood basal buttress roots (2..4 cardinal roots)
        List<Direction> buttressDirs = Direction.Plane.HORIZONTAL.shuffledCopy(random);
        int buttressCount = 2 + random.nextInt(3); // 2..4 roots
        for (int i = 0; i < buttressCount && i < buttressDirs.size(); i++) {
            Direction rootDir = buttressDirs.get(i);
            BlockPos rootPos = new BlockPos(baseX + rootDir.getStepX(), baseY, baseZ + rootDir.getStepZ());
            BlockPos belowRoot = rootPos.below();
            BlockState belowState = level.getBlockState(belowRoot);

            // Only place root buttress where supported by non-water solid block (or step 1 block down onto solid ground)
            if (belowState.getFluidState().isEmpty() && !belowState.isAir() && !canTreeReplace(belowState)) {
                if (canTreeReplace(level.getBlockState(rootPos))) {
                    level.setBlock(rootPos, allBarkWood, 2);
                    if (i == 0 && trunkHeight >= 6 && canTreeReplace(level.getBlockState(rootPos.above()))) {
                        level.setBlock(rootPos.above(), allBarkWood, 2);
                    }
                }
            } else if (canTreeReplace(belowState) && belowState.getFluidState().isEmpty()) {
                BlockState twoBelow = level.getBlockState(belowRoot.below());
                if (!twoBelow.isAir() && twoBelow.getFluidState().isEmpty() && !canTreeReplace(twoBelow)) {
                    level.setBlock(belowRoot, allBarkWood, 2);
                    if (canTreeReplace(level.getBlockState(rootPos))) {
                        level.setBlock(rootPos, allBarkWood, 2);
                    }
                }
            }
        }

        // 2. Crooked, slightly leaning entropica:spore_wood_log trunk
        List<BlockPos> bracketCandidateNodes = new ArrayList<>();
        int crownX = baseX;
        int crownZ = baseZ;
        for (int h = 0; h < trunkHeight; h++) {
            if (h == bendStart) {
                // Connect the crook smoothly with an all-bark spore_wood knuckle before stepping into the lean
                BlockPos knucklePos = new BlockPos(crownX, baseY + h, crownZ);
                level.setBlock(knucklePos, allBarkWood, 2);
                crownX += leanDir.getStepX();
                crownZ += leanDir.getStepZ();
            }
            BlockPos logPos = new BlockPos(crownX, baseY + h, crownZ);
            level.setBlock(logPos, (h == 0) ? allBarkWood : verticalLog, 2);
            if (h >= 1 && h <= trunkHeight - 2) {
                bracketCandidateNodes.add(logPos);
            }
        }

        int topY = baseY + trunkHeight - 1;

        // 3. 2..3 short upper limbs of entropica:spore_wood_log
        int limbCount = 2 + random.nextInt(2); // 2..3 limbs
        List<Direction> limbDirs = Direction.Plane.HORIZONTAL.shuffledCopy(random);
        List<BlockPos> limbTips = new ArrayList<>();
        for (int i = 0; i < limbCount && i < limbDirs.size(); i++) {
            Direction limbDir = limbDirs.get(i);
            int limbY = topY - 1 - (i % 2);
            int originTrunkX = (limbY - baseY >= bendStart) ? crownX : baseX;
            int originTrunkZ = (limbY - baseY >= bendStart) ? crownZ : baseZ;
            BlockPos limbPos = new BlockPos(originTrunkX + limbDir.getStepX(), limbY, originTrunkZ + limbDir.getStepZ());
            if (canTreeReplace(level.getBlockState(limbPos))) {
                level.setBlock(limbPos, logWithAxis(sporeWoodLogBlock, limbDir.getAxis()), 2);
                limbTips.add(limbPos);
            }
        }

        // 4. Layered frosted dome canopy of entropica:frozen_spore_leaves
        // Lower wide dome skirt (topY - 1): radius 2..3 rounded disc
        int lowerRadius = (trunkHeight >= 7) ? 3 : 2;
        for (int dx = -lowerRadius; dx <= lowerRadius; dx++) {
            for (int dz = -lowerRadius; dz <= lowerRadius; dz++) {
                double dist = Math.hypot(dx, dz);
                if (dist <= lowerRadius + 0.25D) {
                    if (dist > lowerRadius - 0.35D && random.nextFloat() < 0.35F) {
                        continue;
                    }
                    placeLeafIfClear(level, new BlockPos(crownX + dx, topY - 1, crownZ + dz), leavesState);
                }
            }
        }

        // Mid frosted dome tier (topY): radius 2 rounded dome
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) == 2 && Math.abs(dz) == 2 && random.nextFloat() < 0.70F) {
                    continue;
                }
                placeLeafIfClear(level, new BlockPos(crownX + dx, topY, crownZ + dz), leavesState);
            }
        }

        // Upper rounded dome cap (topY + 1): radius 1..2
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (Math.abs(dx) == 1 && Math.abs(dz) == 1 && random.nextFloat() < 0.30F) {
                    continue;
                }
                placeLeafIfClear(level, new BlockPos(crownX + dx, topY + 1, crownZ + dz), leavesState);
            }
        }
        if (trunkHeight >= 6) {
            placeLeafIfClear(level, new BlockPos(crownX, topY + 2, crownZ), leavesState);
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                if (random.nextFloat() < 0.45F) {
                    placeLeafIfClear(level, new BlockPos(crownX + dir.getStepX(), topY + 2, crownZ + dir.getStepZ()), leavesState);
                }
            }
        }

        // Leaf pads around upper limbs + occasional drooping frost-leaf fringe
        for (BlockPos tip : limbTips) {
            placeLeafIfClear(level, tip.above(), leavesState);
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                if (random.nextFloat() < 0.80F) {
                    BlockPos pad = tip.relative(dir);
                    placeLeafIfClear(level, pad, leavesState);
                    if (random.nextFloat() < 0.25F) {
                        placeLeafIfClear(level, pad.below(), leavesState);
                    }
                }
            }
        }

        // 5. 1..3 horizontal entropica:spore_fungal_shelf_cap brackets attached to the trunk
        if (shelfCapOpt.isPresent() && !bracketCandidateNodes.isEmpty()) {
            Block shelfCapBlock = shelfCapOpt.get();
            int targetShelves = 1 + random.nextInt(3); // 1..3 brackets
            int placedShelves = 0;

            List<BlockPos> shuffledTrunk = new ArrayList<>(bracketCandidateNodes);
            for (int i = shuffledTrunk.size() - 1; i > 0; i--) {
                int swapIdx = random.nextInt(i + 1);
                BlockPos tmp = shuffledTrunk.get(i);
                shuffledTrunk.set(i, shuffledTrunk.get(swapIdx));
                shuffledTrunk.set(swapIdx, tmp);
            }

            for (BlockPos trunkNode : shuffledTrunk) {
                if (placedShelves >= targetShelves) {
                    break;
                }
                for (Direction outwardDir : Direction.Plane.HORIZONTAL.shuffledCopy(random)) {
                    BlockPos shelfPos = trunkNode.relative(outwardDir);
                    BlockState existing = level.getBlockState(shelfPos);
                    if (existing.isAir() || existing.is(Blocks.SNOW)) {
                        BlockState shelfState = orientHorizontalShelf(shelfCapBlock, outwardDir);
                        level.setBlock(shelfPos, shelfState, 2);
                        placedShelves++;
                        break;
                    }
                }
            }
        }

        return true;
    }

    private static BlockPos findValidTreeGround(
            WorldGenLevel level,
            int x,
            int z,
            int hintY,
            Optional<Block> frozenSporeMossOpt,
            Optional<Block> sporeGlazedIceOpt
    ) {
        int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
        int startY = Math.max(topY, hintY + 2);
        int minY = Math.max(61, startY - 6);
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
            if (isValidSporeWoodGround(state, frozenSporeMossOpt, sporeGlazedIceOpt)) {
                BlockState above = level.getBlockState(cursor.above());
                if (canTreeReplace(above) && above.getFluidState().isEmpty()) {
                    return cursor.immutable();
                }
            }
            return null;
        }
        return null;
    }

    private static boolean isValidSporeWoodGround(
            BlockState state,
            Optional<Block> frozenSporeMossOpt,
            Optional<Block> sporeGlazedIceOpt
    ) {
        if (EntropicaFloraGroundHelper.isIceOrSnowLayer(state)) {
            return false;
        }
        if (sporeGlazedIceOpt.isPresent() && state.is(sporeGlazedIceOpt.get())) {
            return false;
        }
        if (frozenSporeMossOpt.isPresent() && state.is(frozenSporeMossOpt.get())) {
            return true;
        }
        return state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.MUD)
                || state.is(Blocks.MYCELIUM)
                || state.is(Blocks.MOSS_BLOCK);
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

    private static BlockState orientHorizontalShelf(Block shelfBlock, Direction outwardHoriz) {
        BlockState state = shelfBlock.defaultBlockState();
        if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            state = state.setValue(HorizontalDirectionalBlock.FACING, outwardHoriz);
        } else if (state.hasProperty(BlockStateProperties.FACING)
                && BlockStateProperties.FACING.getPossibleValues().contains(outwardHoriz)) {
            state = state.setValue(BlockStateProperties.FACING, outwardHoriz);
        }
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        return state;
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
