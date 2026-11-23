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
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates natural unfrozen warm Vitae Springs ({@code radius 3..5} shallow terraced pools)
 * in {@code 2.10 Radiant Glaze-Plains}, lined with {@code entropica:vitae_spring_stone}, rimmed
 * with {@code entropica:radiant_glaze_ice}, {@link Blocks#SMOOTH_QUARTZ}, {@link Blocks#CALCITE},
 * and golden-tinted {@link Blocks#GRASS_BLOCK} + {@link Blocks#SHORT_GRASS} +
 * {@code entropica:vitae_orchid} / {@code entropica:auroral_feather_moss}, filled with warm
 * {@link Blocks#WATER} and lit from below at the center of larger springs by a
 * {@code entropica:sun_glazed_lantern} tucked beneath {@code entropica:radiant_glass}.
 */
public class VitaeSpringFeature extends Feature<NoneFeatureConfiguration> {

    private static final ResourceLocation VITAE_SPRING_STONE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vitae_spring_stone");
    private static final ResourceLocation RADIANT_GLAZE_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "radiant_glaze_ice");
    private static final ResourceLocation RADIANT_SNOW_BLOCK_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "radiant_snow_block");
    private static final ResourceLocation RADIANT_GLASS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "radiant_glass");
    private static final ResourceLocation SUN_GLAZED_LANTERN_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "sun_glazed_lantern");
    private static final ResourceLocation VITAE_ORCHID_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vitae_orchid");
    private static final ResourceLocation AURORAL_FEATHER_MOSS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "auroral_feather_moss");

    public VitaeSpringFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        Block springStoneBlock = resolveBlock(VITAE_SPRING_STONE_ID, Blocks.CALCITE);
        Block glazeIceBlock = resolveBlock(RADIANT_GLAZE_ICE_ID, Blocks.PACKED_ICE);
        Block radiantSnowBlock = resolveBlock(RADIANT_SNOW_BLOCK_ID, Blocks.SNOW_BLOCK);
        Block radiantGlassBlock = resolveBlock(RADIANT_GLASS_ID, Blocks.YELLOW_STAINED_GLASS);
        Block sunLanternBlock = resolveBlock(SUN_GLAZED_LANTERN_ID, Blocks.LANTERN);
        Block vitaeOrchidBlock = resolveBlock(VITAE_ORCHID_ID, Blocks.DANDELION);
        Block featherMossBlock = resolveBlock(AURORAL_FEATHER_MOSS_ID, Blocks.MOSS_CARPET);

        double radiusX = 3.0D + random.nextDouble() * 2.0D; // 3.0..5.0
        double radiusZ = 3.0D + random.nextDouble() * 2.0D; // 3.0..5.0

        BlockPos anchor = findFlatInlandAnchor(level, origin, random, radiusX, radiusZ,
                springStoneBlock, glazeIceBlock, radiantSnowBlock);
        if (anchor == null) {
            return false;
        }

        int cx = anchor.getX();
        int waterY = anchor.getY();
        int cz = anchor.getZ();

        BlockState springStone = springStoneBlock.defaultBlockState();
        BlockState glazeIce = glazeIceBlock.defaultBlockState();
        BlockState radiantSnow = radiantSnowBlock.defaultBlockState();
        BlockState radiantGlass = nonWaterlogged(radiantGlassBlock.defaultBlockState());
        BlockState sunLantern = standingDryLantern(sunLanternBlock.defaultBlockState());
        BlockState smoothQuartz = Blocks.SMOOTH_QUARTZ.defaultBlockState();
        BlockState calcite = Blocks.CALCITE.defaultBlockState();
        BlockState magma = Blocks.MAGMA_BLOCK.defaultBlockState();
        BlockState grassBlock = Blocks.GRASS_BLOCK.defaultBlockState();
        BlockState dirt = Blocks.DIRT.defaultBlockState();
        BlockState shortGrass = Blocks.SHORT_GRASS.defaultBlockState();
        BlockState featherMoss = featherMossBlock.defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        int maxDx = (int) Math.ceil(radiusX * 1.40D) + 1;
        int maxDz = (int) Math.ceil(radiusZ * 1.40D) + 1;

        List<BlockPos> mineralRimCells = new ArrayList<>();
        List<BlockPos> meadowRimCells = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        boolean placedWater = false;

        // 1. Carve shallow terraced warm Vitae Spring pool & watertight mineral/meadow rim
        for (int dx = -maxDx; dx <= maxDx; dx++) {
            for (int dz = -maxDz; dz <= maxDz; dz++) {
                double wobble = 0.06D * Math.sin((cx + dx) * 0.75D + (cz + dz) * 0.95D);
                double normDist = Math.hypot(dx / radiusX, dz / radiusZ) + wobble;
                if (normDist > 1.36D) {
                    continue;
                }

                int wx = cx + dx;
                int wz = cz + dz;

                if (normDist < 0.52D) {
                    // Inner deeper warm spring basin (2 blocks deep: water at waterY - 1..waterY, bed at waterY - 2)
                    int bedY = waterY - 2;
                    cursor.set(wx, bedY - 1, wz);
                    level.setBlock(cursor, calcite, 2);
                    sealSupportDown(level, wx, bedY - 2, wz, calcite);

                    boolean warmMagmaPocket = !(dx == 0 && dz == 0) && random.nextFloat() < 0.14F;
                    cursor.set(wx, bedY, wz);
                    level.setBlock(cursor, warmMagmaPocket ? magma : springStone, 2);

                    cursor.set(wx, waterY - 1, wz);
                    level.setBlock(cursor, water, 2);
                    cursor.set(wx, waterY, wz);
                    level.setBlock(cursor, water, 2);
                    placedWater = true;

                    clearOverburdenAbove(level, cursor, wx, waterY + 1, wz, 3, air,
                            springStoneBlock, glazeIceBlock, radiantSnowBlock);
                } else if (normDist < 0.88D) {
                    // Shallow terraced inner shelf (1 block deep: water at waterY, shelf bed at waterY - 1)
                    int shelfBedY = waterY - 1;
                    cursor.set(wx, shelfBedY - 1, wz);
                    level.setBlock(cursor, springStone, 2);
                    sealSupportDown(level, wx, shelfBedY - 2, wz, calcite);

                    float shelfRoll = random.nextFloat();
                    BlockState shelfFloor = (shelfRoll < 0.58F) ? springStone
                            : ((shelfRoll < 0.82F) ? smoothQuartz : calcite);
                    cursor.set(wx, shelfBedY, wz);
                    level.setBlock(cursor, shelfFloor, 2);

                    cursor.set(wx, waterY, wz);
                    level.setBlock(cursor, water, 2);
                    placedWater = true;

                    clearOverburdenAbove(level, cursor, wx, waterY + 1, wz, 3, air,
                            springStoneBlock, glazeIceBlock, radiantSnowBlock);
                } else if (normDist < 1.12D) {
                    // Watertight mineral & glaze-ice rim lip at waterY
                    sealSupportDown(level, wx, waterY - 2, wz, calcite);
                    cursor.set(wx, waterY - 1, wz);
                    level.setBlock(cursor, springStone, 2);

                    float rimRoll = random.nextFloat();
                    BlockState rimState;
                    if (rimRoll < 0.42F) {
                        rimState = springStone;
                    } else if (rimRoll < 0.70F) {
                        rimState = glazeIce;
                    } else if (rimRoll < 0.88F) {
                        rimState = smoothQuartz;
                    } else {
                        rimState = calcite;
                    }
                    BlockPos rimPos = new BlockPos(wx, waterY, wz);
                    level.setBlock(rimPos, rimState, 2);
                    if (rimState != glazeIce) {
                        mineralRimCells.add(rimPos);
                    }

                    clearOverburdenAbove(level, cursor, wx, waterY + 1, wz, 2, air,
                            springStoneBlock, glazeIceBlock, radiantSnowBlock);
                } else {
                    // Outer sun-warmed golden grass & radiant snow apron (normDist 1.12..1.36)
                    sealSupportDown(level, wx, waterY - 2, wz, dirt);
                    boolean meadowTurf = random.nextFloat() < 0.76F;
                    cursor.set(wx, waterY - 1, wz);
                    level.setBlock(cursor, meadowTurf ? dirt : calcite, 2);

                    BlockPos outerPos = new BlockPos(wx, waterY, wz);
                    level.setBlock(outerPos, meadowTurf ? grassBlock : radiantSnow, 2);
                    if (meadowTurf) {
                        meadowRimCells.add(outerPos);
                    }

                    clearOverburdenAbove(level, cursor, wx, waterY + 1, wz, 2, air,
                            springStoneBlock, glazeIceBlock, radiantSnowBlock);
                }
            }
        }

        if (!placedWater) {
            return false;
        }

        // 2. Center Sub-Floor Glow on Medium/Larger Springs: sun_glazed_lantern tucked beneath radiant_glass
        if (Math.min(radiusX, radiusZ) >= 3.35D) {
            int centerBedY = waterY - 2;
            // Seal dry pocket around the sub-bed lantern
            for (Direction dir : Direction.values()) {
                if (dir == Direction.UP) {
                    continue;
                }
                cursor.set(cx + dir.getStepX(), centerBedY - 1 + dir.getStepY(), cz + dir.getStepZ());
                level.setBlock(cursor, springStone, 2);
            }
            cursor.set(cx, centerBedY - 1, cz);
            level.setBlock(cursor, sunLantern, 2);
            cursor.set(cx, centerBedY, cz);
            level.setBlock(cursor, radiantGlass, 2);
        }

        // 3. Rim Flora: Auroral Feather-Moss on mineral rim + Golden Short Grass & Vitae Orchids on meadow apron
        shufflePositions(mineralRimCells, random);
        int targetMoss = 2 + random.nextInt(3); // 2..4
        int placedMoss = 0;
        for (BlockPos rimTop : mineralRimCells) {
            if (placedMoss >= targetMoss) {
                break;
            }
            BlockPos abovePos = rimTop.above();
            BlockState aboveState = level.getBlockState(abovePos);
            if ((aboveState.isAir() || aboveState.is(Blocks.SNOW)) && featherMoss.canSurvive(level, abovePos)) {
                level.setBlock(abovePos, featherMoss, 2);
                placedMoss++;
            }
        }

        shufflePositions(meadowRimCells, random);
        int targetOrchids = 1 + random.nextInt(3); // 1..3
        int placedOrchids = 0;
        for (BlockPos meadowTop : meadowRimCells) {
            BlockPos plantPos = meadowTop.above();
            BlockState plantExisting = level.getBlockState(plantPos);
            if (!plantExisting.isAir() && !plantExisting.is(Blocks.SNOW)) {
                continue;
            }
            if (placedOrchids < targetOrchids && tryPlacePlantOrDoubleFlower(level, plantPos, vitaeOrchidBlock)) {
                placedOrchids++;
                continue;
            }
            if (random.nextFloat() < 0.55F && shortGrass.canSurvive(level, plantPos)) {
                level.setBlock(plantPos, shortGrass, 2);
            }
        }

        return true;
    }

    private static boolean tryPlacePlantOrDoubleFlower(WorldGenLevel level, BlockPos pos, Block plantBlock) {
        BlockState baseState = plantBlock.defaultBlockState();
        if (plantBlock instanceof DoublePlantBlock) {
            BlockPos upperPos = pos.above();
            if (upperPos.getY() >= level.getMaxY() - 1 || !level.getBlockState(upperPos).isAir()) {
                return false;
            }
            BlockState lower = baseState.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER);
            if (!lower.canSurvive(level, pos)) {
                return false;
            }
            BlockState upper = baseState.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER);
            level.setBlock(pos, lower, 2);
            level.setBlock(upperPos, upper, 2);
            return true;
        }
        if (baseState.canSurvive(level, pos)) {
            level.setBlock(pos, baseState, 2);
            return true;
        }
        return false;
    }

    private static BlockPos findFlatInlandAnchor(
            WorldGenLevel level,
            BlockPos origin,
            RandomSource random,
            double radiusX,
            double radiusZ,
            Block springStoneBlock,
            Block glazeIceBlock,
            Block radiantSnowBlock
    ) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int r = 0; r <= 2; r++) {
            int attempts = (r == 0) ? 1 : 5;
            for (int a = 0; a < attempts; a++) {
                int ox = (r == 0) ? 0 : random.nextInt(r * 2 + 1) - r;
                int oz = (r == 0) ? 0 : random.nextInt(r * 2 + 1) - r;
                int tx = origin.getX() + ox;
                int tz = origin.getZ() + oz;

                int topY = findGroundTopY(level, tx, tz, origin.getY(),
                        springStoneBlock, glazeIceBlock, radiantSnowBlock, cursor);
                if (topY < 62 || topY > level.getMaxY() - 8) {
                    continue;
                }
                if (isFlatContainedSite(level, tx, topY, tz, radiusX, radiusZ,
                        springStoneBlock, glazeIceBlock, radiantSnowBlock, cursor)) {
                    return new BlockPos(tx, topY, tz);
                }
            }
        }
        return null;
    }

    private static int findGroundTopY(
            WorldGenLevel level,
            int x,
            int z,
            int hintY,
            Block springStoneBlock,
            Block glazeIceBlock,
            Block radiantSnowBlock,
            BlockPos.MutableBlockPos cursor
    ) {
        int hmY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
        int startY = Math.min(level.getMaxY() - 6, Math.max(hmY, hintY + 2));
        int minY = Math.max(62, startY - 8);
        for (int y = startY; y >= minY; y--) {
            cursor.set(x, y, z);
            BlockState ground = level.getBlockState(cursor);
            if (ground.is(Blocks.WATER) || ground.is(Blocks.LAVA)) {
                return Integer.MIN_VALUE;
            }
            if (isValidPlainsSubstrate(ground, springStoneBlock, glazeIceBlock, radiantSnowBlock)) {
                BlockState above = level.getBlockState(cursor.above());
                if (above.isAir() || above.is(Blocks.SNOW) || above.is(BlockTags.REPLACEABLE)) {
                    return y;
                }
            }
        }
        return Integer.MIN_VALUE;
    }

    private static boolean isFlatContainedSite(
            WorldGenLevel level,
            int cx,
            int waterY,
            int cz,
            double radiusX,
            double radiusZ,
            Block springStoneBlock,
            Block glazeIceBlock,
            Block radiantSnowBlock,
            BlockPos.MutableBlockPos cursor
    ) {
        int checkDx = (int) Math.ceil(radiusX * 1.38D) + 1;
        int checkDz = (int) Math.ceil(radiusZ * 1.38D) + 1;
        for (int dx = -checkDx; dx <= checkDx; dx += 2) {
            for (int dz = -checkDz; dz <= checkDz; dz += 2) {
                double norm = Math.hypot(dx / radiusX, dz / radiusZ);
                if (norm > 1.40D) {
                    continue;
                }
                int colTop = findGroundTopY(level, cx + dx, cz + dz, waterY,
                        springStoneBlock, glazeIceBlock, radiantSnowBlock, cursor);
                if (colTop == Integer.MIN_VALUE || Math.abs(colTop - waterY) > 2) {
                    return false;
                }
                cursor.set(cx + dx, waterY - 2, cz + dz);
                BlockState deep = level.getBlockState(cursor);
                if (deep.isAir() || deep.is(Blocks.LAVA)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void clearOverburdenAbove(
            WorldGenLevel level,
            BlockPos.MutableBlockPos cursor,
            int x,
            int startY,
            int z,
            int height,
            BlockState air,
            Block springStoneBlock,
            Block glazeIceBlock,
            Block radiantSnowBlock
    ) {
        for (int dy = 0; dy < height; dy++) {
            cursor.set(x, startY + dy, z);
            BlockState st = level.getBlockState(cursor);
            if (!st.isAir() && (isValidPlainsSubstrate(st, springStoneBlock, glazeIceBlock, radiantSnowBlock)
                    || st.is(Blocks.SNOW)
                    || st.is(Blocks.POWDER_SNOW)
                    || st.is(BlockTags.REPLACEABLE)
                    || st.is(BlockTags.FLOWERS))) {
                level.setBlock(cursor, air, 2);
            }
        }
    }

    private static void sealSupportDown(WorldGenLevel level, int x, int startY, int z, BlockState support) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minY = Math.max(level.getMinY() + 2, startY - 6);
        for (int y = startY; y >= minY; y--) {
            cursor.set(x, y, z);
            BlockState existing = level.getBlockState(cursor);
            if (existing.isAir() || existing.is(Blocks.WATER) || existing.is(Blocks.SNOW) || existing.canBeReplaced()) {
                level.setBlock(cursor, support, 2);
            } else {
                break;
            }
        }
    }

    private static boolean isValidPlainsSubstrate(
            BlockState state,
            Block springStoneBlock,
            Block glazeIceBlock,
            Block radiantSnowBlock
    ) {
        return state.is(Blocks.GRASS_BLOCK)
                || state.is(BlockTags.DIRT)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.CALCITE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.BLUE_ICE)
                || state.is(springStoneBlock)
                || state.is(glazeIceBlock)
                || state.is(radiantSnowBlock)
                || state.is(BlockTags.BASE_STONE_OVERWORLD);
    }

    private static BlockState nonWaterlogged(BlockState state) {
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            return state.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        return state;
    }

    private static BlockState standingDryLantern(BlockState state) {
        BlockState res = nonWaterlogged(state);
        if (res.hasProperty(BlockStateProperties.HANGING)) {
            res = res.setValue(BlockStateProperties.HANGING, false);
        }
        return res;
    }

    private static void shufflePositions(List<BlockPos> list, RandomSource random) {
        for (int i = list.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            BlockPos tmp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, tmp);
        }
    }

    private static Block resolveBlock(ResourceLocation id, Block fallback) {
        return BuiltInRegistries.BLOCK.getOptional(id).orElse(fallback);
    }
}
