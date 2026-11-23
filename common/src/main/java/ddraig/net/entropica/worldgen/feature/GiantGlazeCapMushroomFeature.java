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
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
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
 * Generates {@code 5..9}-block tall Giant Frozen Glaze-Cap Mushrooms for Biome #12
 * ({@code 2.12 Spore-Glazed Pack}):
 * <ul>
 *   <li><b>STRICT RULE</b>: Trunk, stalk, and flared root buttresses strictly use vanilla
 *       {@link Blocks#MUSHROOM_STEM} ({@code minecraft:mushroom_stem}) — NEVER {@code spore_wood_log}!</li>
 *   <li>{@code 1..3} horizontal {@code entropica:spore_fungal_shelf_cap} brackets attached to the stem sides</li>
 *   <li>Broad frosted golden-ochre umbrella dome ({@code radius 2..3}, {@code 2..3} tiers high with drooping
 *       rim corners and hollow under-rim) of {@code entropica:glaze_cap_mushroom_block}</li>
 *   <li>Grows on {@code frozen_spore_moss}, {@code spore_glazed_ice}, {@code packed_ice}, {@code blue_ice},
 *       {@code snow_block}, or soil above water (cleanly replacing thin {@link Blocks#SNOW} layers)</li>
 * </ul>
 */
public class GiantGlazeCapMushroomFeature extends Feature<NoneFeatureConfiguration> {

    private static final ResourceLocation GLAZE_CAP_MUSHROOM_BLOCK_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "glaze_cap_mushroom_block");
    private static final ResourceLocation SPORE_FUNGAL_SHELF_CAP_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spore_fungal_shelf_cap");
    private static final ResourceLocation FROZEN_SPORE_MOSS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "frozen_spore_moss");
    private static final ResourceLocation SPORE_GLAZED_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spore_glazed_ice");

    public GiantGlazeCapMushroomFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        Block glazeCapBlock = BuiltInRegistries.BLOCK.getOptional(GLAZE_CAP_MUSHROOM_BLOCK_ID)
                .orElse(Blocks.BROWN_MUSHROOM_BLOCK);
        Optional<Block> shelfCapOpt = BuiltInRegistries.BLOCK.getOptional(SPORE_FUNGAL_SHELF_CAP_ID);
        Optional<Block> frozenSporeMossOpt = BuiltInRegistries.BLOCK.getOptional(FROZEN_SPORE_MOSS_ID);
        Optional<Block> sporeGlazedIceOpt = BuiltInRegistries.BLOCK.getOptional(SPORE_GLAZED_ICE_ID);

        BlockPos groundPos = findValidMushroomGround(level, origin.getX(), origin.getZ(), origin.getY(),
                frozenSporeMossOpt, sporeGlazedIceOpt);
        if (groundPos == null) {
            return false;
        }

        int stalkHeight = 5 + random.nextInt(5); // 5..9 blocks tall
        int capRadius = (stalkHeight >= 7) ? (2 + random.nextInt(2)) : 2; // radius 2 or 3
        boolean threeTierCap = (stalkHeight >= 7) || (capRadius == 3);

        int baseX = groundPos.getX();
        int baseY = groundPos.getY() + 1;
        int baseZ = groundPos.getZ();

        if (baseY + stalkHeight + 3 >= level.getMaxY() - 1) {
            return false;
        }

        boolean curvedStalk = stalkHeight >= 6 && random.nextFloat() < 0.75F;
        Direction curveDir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int bendIndex = stalkHeight / 2;

        // Check stalk and umbrella cap clearance
        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
        for (int dy = 0; dy <= stalkHeight + 1; dy++) {
            int cx = baseX + (curvedStalk && dy >= bendIndex ? curveDir.getStepX() : 0);
            int cz = baseZ + (curvedStalk && dy >= bendIndex ? curveDir.getStepZ() : 0);
            int r = (dy >= stalkHeight - 2) ? capRadius : 0;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    checkPos.set(cx + dx, baseY + dy, cz + dz);
                    if (!canMushroomReplace(level.getBlockState(checkPos))) {
                        return false;
                    }
                }
            }
        }

        if (level.getBlockState(groundPos).is(Blocks.GRASS_BLOCK)) {
            level.setBlock(groundPos, Blocks.DIRT.defaultBlockState(), 2);
        }

        // STRICT USER RULE: Trunk, stalk, and root buttresses MUST strictly use vanilla Blocks.MUSHROOM_STEM!
        BlockState stemState = Blocks.MUSHROOM_STEM.defaultBlockState();
        BlockState capState = nonWaterlogged(glazeCapBlock.defaultBlockState());

        // 1. Flared Blocks.MUSHROOM_STEM root buttresses (2..4 cardinal root flares)
        List<Direction> rootDirs = Direction.Plane.HORIZONTAL.shuffledCopy(random);
        int rootCount = 2 + random.nextInt(3);
        for (int i = 0; i < rootCount && i < rootDirs.size(); i++) {
            Direction rootDir = rootDirs.get(i);
            BlockPos flarePos = new BlockPos(baseX + rootDir.getStepX(), baseY, baseZ + rootDir.getStepZ());
            BlockPos belowFlare = flarePos.below();
            BlockState belowState = level.getBlockState(belowFlare);

            if (belowState.getFluidState().isEmpty() && !belowState.isAir() && !canMushroomReplace(belowState)) {
                if (canMushroomReplace(level.getBlockState(flarePos))) {
                    level.setBlock(flarePos, stemState, 2);
                    if (i == 0 && stalkHeight >= 7 && canMushroomReplace(level.getBlockState(flarePos.above()))) {
                        level.setBlock(flarePos.above(), stemState, 2);
                    }
                }
            } else if (canMushroomReplace(belowState) && belowState.getFluidState().isEmpty()) {
                BlockState twoBelow = level.getBlockState(belowFlare.below());
                if (!twoBelow.isAir() && twoBelow.getFluidState().isEmpty() && !canMushroomReplace(twoBelow)) {
                    level.setBlock(belowFlare, stemState, 2);
                    if (canMushroomReplace(level.getBlockState(flarePos))) {
                        level.setBlock(flarePos, stemState, 2);
                    }
                }
            }
        }

        // 2. Thick/curved Blocks.MUSHROOM_STEM stalk (5..9 blocks tall)
        List<BlockPos> stemNodes = new ArrayList<>();
        int capCenterX = baseX;
        int capCenterZ = baseZ;
        for (int h = 0; h < stalkHeight; h++) {
            if (curvedStalk && h == bendIndex) {
                // Thick 2-block overlap at the curve bend using strictly Blocks.MUSHROOM_STEM
                BlockPos bendElbow = new BlockPos(capCenterX, baseY + h, capCenterZ);
                level.setBlock(bendElbow, stemState, 2);
                capCenterX += curveDir.getStepX();
                capCenterZ += curveDir.getStepZ();
            }
            BlockPos stalkPos = new BlockPos(capCenterX, baseY + h, capCenterZ);
            level.setBlock(stalkPos, stemState, 2);
            if (h >= 1 && h <= stalkHeight - 3) {
                stemNodes.add(stalkPos);
            }
        }

        int stalkTopY = baseY + stalkHeight - 1;
        int capApexY = stalkTopY + 1;

        // 3. Broad frosted golden-ochre umbrella dome (radius 2..3, 2..3 tiers high, hollow under-rim + drooping corners)
        // Tier A (capApexY): Solid rounded upper dome crown (inner radius = capRadius - 1)
        int innerCrownR = Math.max(1, capRadius - 1);
        for (int dx = -innerCrownR; dx <= innerCrownR; dx++) {
            for (int dz = -innerCrownR; dz <= innerCrownR; dz++) {
                if (innerCrownR >= 2 && Math.abs(dx) == innerCrownR && Math.abs(dz) == innerCrownR) {
                    continue; // Soften top crown corners on radius-3 domes
                }
                placeCapIfClear(level, new BlockPos(capCenterX + dx, capApexY, capCenterZ + dz), capState);
            }
        }

        // Tier B (stalkTopY): Umbrella outer bell wall at capRadius, keeping intermediate under-rim hollow!
        for (int dx = -capRadius; dx <= capRadius; dx++) {
            for (int dz = -capRadius; dz <= capRadius; dz++) {
                if (dx == 0 && dz == 0) {
                    continue; // Central Blocks.MUSHROOM_STEM already occupies (0, stalkTopY, 0)!
                }
                int maxAxis = Math.max(Math.abs(dx), Math.abs(dz));
                boolean isExtremeCorner = Math.abs(dx) == capRadius && Math.abs(dz) == capRadius;
                if (maxAxis == capRadius && !isExtremeCorner) {
                    placeCapIfClear(level, new BlockPos(capCenterX + dx, stalkTopY, capCenterZ + dz), capState);
                } else if (capRadius == 3 && Math.abs(dx) == 2 && Math.abs(dz) == 2) {
                    // Bridge diagonal shoulder on radius-3 dome while keeping radius 1..2 interior hollow!
                    placeCapIfClear(level, new BlockPos(capCenterX + dx, stalkTopY, capCenterZ + dz), capState);
                }
            }
        }

        // Tier C (stalkTopY - 1): Drooping rim corners & 3rd lower skirt tier (hollow interior!)
        int droopY = stalkTopY - 1;
        if (threeTierCap) {
            for (int dx = -capRadius; dx <= capRadius; dx++) {
                for (int dz = -capRadius; dz <= capRadius; dz++) {
                    int maxAxis = Math.max(Math.abs(dx), Math.abs(dz));
                    boolean isExtremeCorner = Math.abs(dx) == capRadius && Math.abs(dz) == capRadius;
                    // Drooping rim corners & scalloped eaves tabs along outer perimeter
                    boolean isDroopCorner = (capRadius == 2 && isExtremeCorner)
                            || (capRadius == 3 && ((Math.abs(dx) == 3 && Math.abs(dz) == 2) || (Math.abs(dx) == 2 && Math.abs(dz) == 3)));
                    boolean isCardinalLip = (dx == 0 && Math.abs(dz) == capRadius) || (dz == 0 && Math.abs(dx) == capRadius);
                    if (isDroopCorner || (isCardinalLip && random.nextFloat() < 0.75F)
                            || (maxAxis == capRadius && !isExtremeCorner && random.nextFloat() < 0.35F)) {
                        placeCapIfClear(level, new BlockPos(capCenterX + dx, droopY, capCenterZ + dz), capState);
                    }
                }
            }
        } else {
            // 2-tier umbrella still gets iconic frosted drooping rim corners!
            for (int sx : new int[]{-capRadius, capRadius}) {
                for (int sz : new int[]{-capRadius, capRadius}) {
                    if (random.nextFloat() < 0.80F) {
                        placeCapIfClear(level, new BlockPos(capCenterX + sx, droopY, capCenterZ + sz), capState);
                    }
                }
            }
        }

        // 4. 1..3 horizontal entropica:spore_fungal_shelf_cap brackets on the Blocks.MUSHROOM_STEM sides
        if (shelfCapOpt.isPresent() && !stemNodes.isEmpty()) {
            Block shelfCapBlock = shelfCapOpt.get();
            int targetBrackets = 1 + random.nextInt(3); // 1..3 brackets
            int placedBrackets = 0;

            List<BlockPos> shuffledStem = new ArrayList<>(stemNodes);
            for (int i = shuffledStem.size() - 1; i > 0; i--) {
                int swapIdx = random.nextInt(i + 1);
                BlockPos tmp = shuffledStem.get(i);
                shuffledStem.set(i, shuffledStem.get(swapIdx));
                shuffledStem.set(swapIdx, tmp);
            }

            for (BlockPos stemNode : shuffledStem) {
                if (placedBrackets >= targetBrackets) {
                    break;
                }
                for (Direction outwardDir : Direction.Plane.HORIZONTAL.shuffledCopy(random)) {
                    BlockPos bracketPos = stemNode.relative(outwardDir);
                    BlockState existing = level.getBlockState(bracketPos);
                    if (existing.isAir() || existing.is(Blocks.SNOW)) {
                        level.setBlock(bracketPos, orientHorizontalShelf(shelfCapBlock, outwardDir), 2);
                        placedBrackets++;
                        break;
                    }
                }
            }
        }

        return true;
    }

    private static BlockPos findValidMushroomGround(
            WorldGenLevel level,
            int x,
            int z,
            int hintY,
            Optional<Block> frozenSporeMossOpt,
            Optional<Block> sporeGlazedIceOpt
    ) {
        int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
        int startY = Math.max(topY, hintY + 2);
        int seaLevel = level.getSeaLevel() > 0 ? level.getSeaLevel() : 63;
        int minY = Math.max(seaLevel - 1, startY - 6);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int y = startY; y >= minY; y--) {
            cursor.set(x, y, z);
            BlockState state = level.getBlockState(cursor);
            if (!state.getFluidState().isEmpty()) {
                return null;
            }
            if (state.isAir() || state.is(Blocks.SNOW) || canMushroomReplace(state)) {
                continue;
            }
            if (isValidGlazeCapSubstrate(state, frozenSporeMossOpt, sporeGlazedIceOpt)) {
                BlockState above = level.getBlockState(cursor.above());
                if (canMushroomReplace(above) && above.getFluidState().isEmpty()) {
                    return cursor.immutable();
                }
            }
            return null;
        }
        return null;
    }

    private static boolean isValidGlazeCapSubstrate(
            BlockState state,
            Optional<Block> frozenSporeMossOpt,
            Optional<Block> sporeGlazedIceOpt
    ) {
        if (frozenSporeMossOpt.isPresent() && state.is(frozenSporeMossOpt.get())) {
            return true;
        }
        if (sporeGlazedIceOpt.isPresent() && state.is(sporeGlazedIceOpt.get())) {
            return true;
        }
        return state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.MUD)
                || state.is(Blocks.MYCELIUM)
                || state.is(Blocks.MOSS_BLOCK);
    }

    private static boolean canMushroomReplace(BlockState state) {
        return state.isAir()
                || state.is(Blocks.SNOW)
                || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.REPLACEABLE)
                || state.is(BlockTags.FLOWERS);
    }

    private static void placeCapIfClear(WorldGenLevel level, BlockPos pos, BlockState capState) {
        BlockState existing = level.getBlockState(pos);
        if (existing.isAir() || existing.is(Blocks.SNOW) || existing.is(BlockTags.LEAVES)) {
            level.setBlock(pos, capState, 2);
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
        return nonWaterlogged(state);
    }

    private static BlockState nonWaterlogged(BlockState state) {
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            return state.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        return state;
    }
}
