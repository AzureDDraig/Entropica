package ddraig.net.entropica.worldgen.feature;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Generates weathered {@code 3x3} basalt ritual cairns on solid dry basalt/moss ledges
 * ({@code y >= 64}, rejecting water) in {@code 2.5 Silent Fjord}, built from
 * {@code entropica:basalt_column} corner posts, {@link Blocks#SMOOTH_BASALT} steps,
 * a central {@code entropica:chiseled_eclipse_basalt} capstone, surrounding
 * {@code entropica:blue_frozen_moss_carpet}, and shaded {@code entropica:shadowed_eclipse_moss} patches.
 */
public class FjordEclipseCairnFeature extends Feature<NoneFeatureConfiguration> {

    private static final int MIN_DRY_LEDGE_Y = 64;

    private static final ResourceLocation BASALT_COLUMN_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "basalt_column");
    private static final ResourceLocation CHISELED_ECLIPSE_BASALT_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "chiseled_eclipse_basalt");
    private static final ResourceLocation BLUE_FROZEN_MOSS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "blue_frozen_moss");
    private static final ResourceLocation BLUE_FROZEN_MOSS_CARPET_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "blue_frozen_moss_carpet");
    private static final ResourceLocation SHADOWED_ECLIPSE_MOSS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "shadowed_eclipse_moss");

    private static final int[][] CORNER_OFFSETS = {
            {-1, -1}, {1, -1}, {-1, 1}, {1, 1}
    };
    private static final int[][] CARDINAL_OFFSETS = {
            {0, -1}, {0, 1}, {-1, 0}, {1, 0}
    };

    public FjordEclipseCairnFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        Block basaltColumnBlock = resolveBlock(BASALT_COLUMN_ID, Blocks.BASALT);
        Block chiseledEclipseBasaltBlock = resolveBlock(CHISELED_ECLIPSE_BASALT_ID, Blocks.POLISHED_BASALT);
        Block blueFrozenMossBlock = resolveBlock(BLUE_FROZEN_MOSS_ID, Blocks.MOSS_BLOCK);
        Block blueFrozenMossCarpetBlock = resolveBlock(BLUE_FROZEN_MOSS_CARPET_ID, Blocks.MOSS_CARPET);
        Block shadowedEclipseMossBlock = resolveBlock(SHADOWED_ECLIPSE_MOSS_ID, Blocks.MOSS_CARPET);

        BlockPos ledgeCenter = findDryLedgeSurface(level, origin, basaltColumnBlock, blueFrozenMossBlock);
        if (ledgeCenter == null) {
            return false;
        }

        int cx = ledgeCenter.getX();
        int groundY = ledgeCenter.getY();
        int cz = ledgeCenter.getZ();

        if (groundY < MIN_DRY_LEDGE_Y || groundY + 6 >= level.getMaxY()) {
            return false;
        }

        // Reject water/lava anywhere near the 3x3 footprint and verify solid 3x3 ledge footing
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = -1; dy <= 3; dy++) {
                    cursor.set(cx + dx, groundY + dy, cz + dz);
                    BlockState check = level.getBlockState(cursor);
                    if (check.is(Blocks.WATER) || check.is(Blocks.LAVA) || check.is(Blocks.BUBBLE_COLUMN)) {
                        return false;
                    }
                }
            }
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (!hasDryFootingAt(level, cx + dx, groundY, cz + dz, basaltColumnBlock, blueFrozenMossBlock)) {
                    return false;
                }
                for (int dy = 1; dy <= 3; dy++) {
                    cursor.set(cx + dx, groundY + dy, cz + dz);
                    if (!isClearHeadroom(level.getBlockState(cursor))) {
                        return false;
                    }
                }
            }
        }

        BlockState basaltColumnState = verticalPillarState(basaltColumnBlock);
        BlockState basaltState = Blocks.BASALT.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState smoothBasaltState = Blocks.SMOOTH_BASALT.defaultBlockState();
        BlockState chiseledCapstoneState = chiseledEclipseBasaltBlock.defaultBlockState();
        BlockState blueFrozenMossState = blueFrozenMossBlock.defaultBlockState();
        BlockState blueCarpetState = blueFrozenMossCarpetBlock.defaultBlockState();
        BlockState shadowMossState = shadowedEclipseMossBlock.defaultBlockState();

        // 1. Anchor solid 3x3 basalt/moss plinth at groundY (filling up to 3 blocks down into micro-slopes)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int bx = cx + dx;
                int bz = cz + dz;
                cursor.set(bx, groundY, bz);
                BlockState foundation = (random.nextFloat() < 0.25F) ? blueFrozenMossState : smoothBasaltState;
                level.setBlock(cursor, foundation, 2);

                for (int d = 1; d <= 3; d++) {
                    cursor.set(bx, groundY - d, bz);
                    BlockState below = level.getBlockState(cursor);
                    if (isClearHeadroom(below)) {
                        level.setBlock(cursor, basaltState, 2);
                    } else {
                        break;
                    }
                }

                // Clear snow/replaceable cover across the 3x3 altar space before placing cairn stones
                for (int dy = 1; dy <= 3; dy++) {
                    cursor.set(bx, groundY + dy, bz);
                    if (!level.getBlockState(cursor).isAir()) {
                        level.setBlock(cursor, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        int baseY = groundY + 1;
        int weatheredCornerIdx = random.nextInt(CORNER_OFFSETS.length);

        // 2. Place 4 weathered basalt_column corner posts (1-2 blocks tall)
        for (int i = 0; i < CORNER_OFFSETS.length; i++) {
            int[] off = CORNER_OFFSETS[i];
            BlockPos cBase = new BlockPos(cx + off[0], baseY, cz + off[1]);
            level.setBlock(cBase, basaltColumnState, 2);
            if (i != weatheredCornerIdx && random.nextFloat() < 0.70F) {
                level.setBlock(cBase.above(), basaltColumnState, 2);
            }
        }

        // 3. Place 4 cardinal SMOOTH_BASALT steps around the ritual core
        for (int[] off : CARDINAL_OFFSETS) {
            BlockPos stepPos = new BlockPos(cx + off[0], baseY, cz + off[1]);
            level.setBlock(stepPos, smoothBasaltState, 2);
        }

        // 4. Place central basalt pedestal + chiseled_eclipse_basalt capstone
        BlockPos centerPedestal = new BlockPos(cx, baseY, cz);
        boolean tallAltar = random.nextBoolean();
        if (tallAltar) {
            level.setBlock(centerPedestal, smoothBasaltState, 2);
            level.setBlock(centerPedestal.above(), basaltColumnState, 2);
            level.setBlock(centerPedestal.above(2), chiseledCapstoneState, 2);
        } else {
            level.setBlock(centerPedestal, basaltColumnState, 2);
            level.setBlock(centerPedestal.above(), chiseledCapstoneState, 2);
        }

        // 5. Drape blue_frozen_moss_carpet on 1-2 weathered steps/posts and surrounding dry ledge
        for (int[] off : CARDINAL_OFFSETS) {
            if (random.nextFloat() < 0.45F) {
                BlockPos carpetOnStep = new BlockPos(cx + off[0], baseY + 1, cz + off[1]);
                if (level.getBlockState(carpetOnStep).isAir() && blueCarpetState.canSurvive(level, carpetOnStep)) {
                    level.setBlock(carpetOnStep, blueCarpetState, 2);
                }
            }
        }

        int outerCarpetsPlaced = 0;
        int outerCarpetTarget = 2 + random.nextInt(3); // 2..4
        for (int attempt = 0; attempt < 16 && outerCarpetsPlaced < outerCarpetTarget; attempt++) {
            int dx = random.nextInt(5) - 2;
            int dz = random.nextInt(5) - 2;
            if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
                continue;
            }
            BlockPos outerGround = findLocalDryGround(level, cx + dx, groundY, cz + dz, basaltColumnBlock, blueFrozenMossBlock);
            if (outerGround == null) {
                continue;
            }
            BlockPos carpetPos = outerGround.above();
            BlockState existing = level.getBlockState(carpetPos);
            if ((existing.isAir() || existing.is(Blocks.SNOW)) && blueCarpetState.canSurvive(level, carpetPos)) {
                level.setBlock(carpetPos, blueCarpetState, 2);
                outerCarpetsPlaced++;
            }
        }

        // 6. Tuck 1-3 shadowed_eclipse_moss patches into shaded niches around the cairn perimeter
        int shadowTarget = 1 + random.nextInt(3); // 1..3
        int shadowPlaced = 0;
        for (int dx = -2; dx <= 2 && shadowPlaced < shadowTarget; dx++) {
            for (int dz = -2; dz <= 2 && shadowPlaced < shadowTarget; dz++) {
                if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
                    continue;
                }
                if (random.nextFloat() < 0.35F) {
                    continue;
                }
                BlockPos outerGround = findLocalDryGround(level, cx + dx, groundY, cz + dz, basaltColumnBlock, blueFrozenMossBlock);
                if (outerGround == null) {
                    continue;
                }
                BlockPos mossPos = outerGround.above();
                BlockState existing = level.getBlockState(mossPos);
                if (!existing.isAir() && !existing.is(Blocks.SNOW)) {
                    continue;
                }
                if (level.getBrightness(LightLayer.BLOCK, mossPos) <= 4
                        && shadowMossState.canSurvive(level, mossPos)) {
                    level.setBlock(mossPos, shadowMossState, 2);
                    shadowPlaced++;
                }
            }
        }

        return true;
    }

    private static BlockPos findDryLedgeSurface(
            WorldGenLevel level,
            BlockPos origin,
            Block basaltColumnBlock,
            Block blueFrozenMossBlock
    ) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = 3; dy >= -4; dy--) {
            int y = origin.getY() + dy;
            if (y < MIN_DRY_LEDGE_Y || y >= level.getMaxY() - 8) {
                continue;
            }
            cursor.set(origin.getX(), y, origin.getZ());
            BlockState ground = level.getBlockState(cursor);
            BlockState above = level.getBlockState(cursor.above());
            if (above.is(Blocks.WATER) || above.is(Blocks.LAVA)) {
                return null;
            }
            if (isDryCairnSubstrate(ground, basaltColumnBlock, blueFrozenMossBlock) && isClearHeadroom(above)) {
                return cursor.immutable();
            }
        }
        return null;
    }

    private static boolean hasDryFootingAt(
            WorldGenLevel level,
            int x,
            int refGroundY,
            int z,
            Block basaltColumnBlock,
            Block blueFrozenMossBlock
    ) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = 1; dy >= -2; dy--) {
            cursor.set(x, refGroundY + dy, z);
            BlockState state = level.getBlockState(cursor);
            if (state.is(Blocks.WATER) || state.is(Blocks.LAVA)) {
                return false;
            }
            if (isDryCairnSubstrate(state, basaltColumnBlock, blueFrozenMossBlock)) {
                return true;
            }
        }
        return false;
    }

    private static BlockPos findLocalDryGround(
            WorldGenLevel level,
            int x,
            int refGroundY,
            int z,
            Block basaltColumnBlock,
            Block blueFrozenMossBlock
    ) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = 1; dy >= -2; dy--) {
            int y = refGroundY + dy;
            if (y < MIN_DRY_LEDGE_Y - 1) {
                continue;
            }
            cursor.set(x, y, z);
            BlockState ground = level.getBlockState(cursor);
            BlockState above = level.getBlockState(cursor.above());
            if (isDryCairnSubstrate(ground, basaltColumnBlock, blueFrozenMossBlock)
                    && (above.isAir() || above.is(Blocks.SNOW))) {
                return cursor.immutable();
            }
        }
        return null;
    }

    private static boolean isDryCairnSubstrate(BlockState state, Block basaltColumnBlock, Block blueFrozenMossBlock) {
        return state.is(Blocks.BASALT)
                || state.is(Blocks.SMOOTH_BASALT)
                || state.is(Blocks.POLISHED_BASALT)
                || state.is(basaltColumnBlock)
                || state.is(blueFrozenMossBlock)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.BLUE_ICE)
                || state.is(BlockTags.DIRT)
                || state.is(BlockTags.BASE_STONE_OVERWORLD);
    }

    private static boolean isClearHeadroom(BlockState state) {
        return !state.is(Blocks.WATER)
                && !state.is(Blocks.LAVA)
                && (state.isAir() || state.is(Blocks.SNOW) || state.is(Blocks.POWDER_SNOW) || state.canBeReplaced());
    }

    private static BlockState verticalPillarState(Block block) {
        BlockState state = block.defaultBlockState();
        if (state.hasProperty(RotatedPillarBlock.AXIS)) {
            return state.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        }
        return state;
    }

    private static Block resolveBlock(ResourceLocation id, Block fallback) {
        return BuiltInRegistries.BLOCK.getOptional(id).orElse(fallback);
    }
}
