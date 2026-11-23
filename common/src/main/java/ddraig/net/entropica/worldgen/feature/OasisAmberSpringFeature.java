package ddraig.net.entropica.worldgen.feature;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Generates shallow natural warm-water Amber Springs ({@code radius 3..5}, {@code depth 1..2})
 * in Biome #14 ({@code 2.14 Oasis Floes}), lined on the bottom and watertight rim with
 * {@code entropica:vitae_spring_stone}, bordered by a {@code 1..2}-block ring of green
 * {@link Blocks#GRASS_BLOCK} (melting away thin {@link Blocks#SNOW} layers around the warm rim),
 * filled with unfrozen {@link Blocks#WATER}, and topped with {@code 1..3} floating
 * {@code entropica:auroral_lily_pad}s and {@code 0..2} {@code entropica:amber_nectar_blossom}s
 * on the adjacent {@link Blocks#GRASS_BLOCK} bank.
 */
public class OasisAmberSpringFeature extends Feature<NoneFeatureConfiguration> {

    private static final ResourceLocation VITAE_SPRING_STONE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vitae_spring_stone");
    private static final ResourceLocation AURORAL_LILY_PAD_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "auroral_lily_pad");
    private static final ResourceLocation AMBER_NECTAR_BLOSSOM_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "amber_nectar_blossom");
    private static final ResourceLocation MIST_VEIL_MARSHMALLOW_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mist_veil_marshmallow");

    public OasisAmberSpringFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        Block springStoneBlock = resolveVitaeSpringStone();
        Block lilyPadBlock = BuiltInRegistries.BLOCK.getOptional(AURORAL_LILY_PAD_ID).orElse(Blocks.LILY_PAD);
        Block nectarBlossomBlock = BuiltInRegistries.BLOCK.getOptional(AMBER_NECTAR_BLOSSOM_ID).orElse(Blocks.DANDELION);
        Optional<Block> marshmallowOpt = BuiltInRegistries.BLOCK.getOptional(MIST_VEIL_MARSHMALLOW_ID);

        double radiusX = 3.0D + random.nextDouble() * 2.0D; // 3.0..5.0
        double radiusZ = 3.0D + random.nextDouble() * 2.0D; // 3.0..5.0

        BlockPos anchor = findFlatInlandBasinAnchor(level, origin, random, radiusX, radiusZ, springStoneBlock);
        if (anchor == null) {
            return false;
        }

        int cx = anchor.getX();
        int waterY = anchor.getY();
        int cz = anchor.getZ();

        BlockState springStone = springStoneBlock.defaultBlockState();
        BlockState grassBlock = Blocks.GRASS_BLOCK.defaultBlockState();
        BlockState dirt = Blocks.DIRT.defaultBlockState();
        BlockState shortGrass = Blocks.SHORT_GRASS.defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        int maxDx = (int) Math.ceil(radiusX * 1.55D) + 2;
        int maxDz = (int) Math.ceil(radiusZ * 1.55D) + 2;

        List<BlockPos> surfaceWaterCells = new ArrayList<>();
        List<BlockPos> warmGrassBankCells = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        boolean placedWater = false;

        // 1. Carve shallow natural warm-water pool (depth 1..2), vitae_spring_stone basin/rim, & 1..2-block green grass border
        for (int dx = -maxDx; dx <= maxDx; dx++) {
            for (int dz = -maxDz; dz <= maxDz; dz++) {
                int wx = cx + dx;
                int wz = cz + dz;
                double wobble = 0.06D * Math.sin(wx * 0.72D + wz * 0.91D);
                double normDist = Math.hypot(dx / radiusX, dz / radiusZ) + wobble;

                if (normDist > 1.62D) {
                    continue;
                }

                if (normDist < 0.48D) {
                    // Inner deeper warm spring pocket (depth 2: water at waterY - 1..waterY, vitae_spring_stone at waterY - 2)
                    int bedY = waterY - 2;
                    sealSupportDown(level, wx, bedY - 1, wz, springStone);
                    cursor.set(wx, bedY, wz);
                    level.setBlock(cursor, springStone, 2);

                    cursor.set(wx, waterY - 1, wz);
                    level.setBlock(cursor, water, 2);
                    BlockPos waterTopPos = new BlockPos(wx, waterY, wz);
                    level.setBlock(waterTopPos, water, 2);
                    surfaceWaterCells.add(waterTopPos);
                    placedWater = true;

                    clearOverburdenAndMeltSnow(level, cursor, wx, waterY + 1, wz, 3, air, springStoneBlock);
                } else if (normDist < 0.86D) {
                    // Shallow warm spring shelf (depth 1: water at waterY, vitae_spring_stone at waterY - 1)
                    int shelfBedY = waterY - 1;
                    sealSupportDown(level, wx, shelfBedY - 1, wz, springStone);
                    cursor.set(wx, shelfBedY, wz);
                    level.setBlock(cursor, springStone, 2);

                    BlockPos waterTopPos = new BlockPos(wx, waterY, wz);
                    level.setBlock(waterTopPos, water, 2);
                    surfaceWaterCells.add(waterTopPos);
                    placedWater = true;

                    clearOverburdenAndMeltSnow(level, cursor, wx, waterY + 1, wz, 3, air, springStoneBlock);
                } else if (normDist < 1.08D) {
                    // Watertight entropica:vitae_spring_stone rim lip at waterY
                    sealSupportDown(level, wx, waterY - 2, wz, dirt);
                    cursor.set(wx, waterY - 1, wz);
                    level.setBlock(cursor, springStone, 2);
                    cursor.set(wx, waterY, wz);
                    level.setBlock(cursor, springStone, 2);

                    clearOverburdenAndMeltSnow(level, cursor, wx, waterY + 1, wz, 3, air, springStoneBlock);
                } else if (normDist <= 1.48D) {
                    // 1..2-block warm green Blocks.GRASS_BLOCK ring around the spring rim (melts thin snow!)
                    sealSupportDown(level, wx, waterY - 2, wz, dirt);
                    cursor.set(wx, waterY - 1, wz);
                    level.setBlock(cursor, dirt, 2);

                    BlockPos grassPos = new BlockPos(wx, waterY, wz);
                    level.setBlock(grassPos, grassBlock, 2);
                    warmGrassBankCells.add(grassPos);

                    clearOverburdenAndMeltSnow(level, cursor, wx, waterY + 1, wz, 3, air, springStoneBlock);
                } else {
                    // Outer thermal thaw fringe (1.48 < normDist <= 1.62): melt thin snow layers without carving terrain
                    for (int dy = -1; dy <= 2; dy++) {
                        cursor.set(wx, waterY + dy, wz);
                        if (level.getBlockState(cursor).is(Blocks.SNOW)) {
                            level.setBlock(cursor, air, 2);
                        }
                    }
                }
            }
        }

        if (!placedWater || surfaceWaterCells.isEmpty()) {
            return false;
        }

        // 2. Float 1..3 glowing entropica:auroral_lily_pad blocks atop the unfrozen spring water
        shufflePositions(surfaceWaterCells, random);
        int targetLilyPads = 1 + random.nextInt(3); // 1..3
        int placedLilyPads = 0;
        BlockState lilyPadState = lilyPadBlock.defaultBlockState();
        for (BlockPos waterPos : surfaceWaterCells) {
            if (placedLilyPads >= targetLilyPads) {
                break;
            }
            BlockPos padPos = waterPos.above();
            if (level.getBlockState(padPos).isAir() && lilyPadState.canSurvive(level, padPos)) {
                level.setBlock(padPos, lilyPadState, 2);
                placedLilyPads++;
            }
        }

        // 3. Sprout 0..2 entropica:amber_nectar_blossom flowers (+ soft grass/marshmallow accents) on the warm GRASS_BLOCK ring
        shufflePositions(warmGrassBankCells, random);
        int targetBlossoms = random.nextInt(3); // 0..2
        int placedBlossoms = 0;
        BlockState blossomState = nectarBlossomBlock.defaultBlockState();

        for (BlockPos bankPos : warmGrassBankCells) {
            BlockPos plantPos = bankPos.above();
            if (!level.getBlockState(plantPos).isAir()) {
                continue;
            }
            if (placedBlossoms < targetBlossoms && blossomState.canSurvive(level, plantPos)) {
                level.setBlock(plantPos, blossomState, 2);
                placedBlossoms++;
                continue;
            }
            if (marshmallowOpt.isPresent() && random.nextFloat() < 0.08F) {
                BlockState marshmallowState = marshmallowOpt.get().defaultBlockState();
                if (marshmallowState.canSurvive(level, plantPos)) {
                    level.setBlock(plantPos, marshmallowState, 2);
                    continue;
                }
            }
            if (random.nextFloat() < 0.42F && shortGrass.canSurvive(level, plantPos)) {
                level.setBlock(plantPos, shortGrass, 2);
            }
        }

        return true;
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

    private static BlockPos findFlatInlandBasinAnchor(
            WorldGenLevel level,
            BlockPos origin,
            RandomSource random,
            double radiusX,
            double radiusZ,
            Block springStoneBlock
    ) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int r = 0; r <= 2; r++) {
            int attempts = (r == 0) ? 1 : 5;
            for (int a = 0; a < attempts; a++) {
                int ox = (r == 0) ? 0 : random.nextInt(r * 2 + 1) - r;
                int oz = (r == 0) ? 0 : random.nextInt(r * 2 + 1) - r;
                int tx = origin.getX() + ox;
                int tz = origin.getZ() + oz;

                int topY = findGroundTopY(level, tx, tz, origin.getY(), springStoneBlock, cursor);
                if (topY < 62 || topY > level.getMaxY() - 8) {
                    continue;
                }
                if (isFlatContainedSite(level, tx, topY, tz, radiusX, radiusZ, springStoneBlock, cursor)) {
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
            if (isValidBasinSubstrate(ground, springStoneBlock)) {
                BlockState above = level.getBlockState(cursor.above());
                if (above.isAir()
                        || above.is(Blocks.SNOW)
                        || above.is(BlockTags.REPLACEABLE)
                        || above.is(BlockTags.FLOWERS)) {
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
            BlockPos.MutableBlockPos cursor
    ) {
        int checkDx = (int) Math.ceil(radiusX * 1.48D) + 1;
        int checkDz = (int) Math.ceil(radiusZ * 1.48D) + 1;
        for (int dx = -checkDx; dx <= checkDx; dx += 2) {
            for (int dz = -checkDz; dz <= checkDz; dz += 2) {
                double norm = Math.hypot(dx / radiusX, dz / radiusZ);
                if (norm > 1.50D) {
                    continue;
                }
                int colTop = findGroundTopY(level, cx + dx, cz + dz, waterY, springStoneBlock, cursor);
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

    private static void clearOverburdenAndMeltSnow(
            WorldGenLevel level,
            BlockPos.MutableBlockPos cursor,
            int x,
            int startY,
            int z,
            int height,
            BlockState air,
            Block springStoneBlock
    ) {
        for (int dy = 0; dy < height; dy++) {
            cursor.set(x, startY + dy, z);
            BlockState st = level.getBlockState(cursor);
            if (!st.isAir() && (isValidBasinSubstrate(st, springStoneBlock)
                    || st.is(Blocks.SNOW)
                    || st.is(Blocks.POWDER_SNOW)
                    || st.is(Blocks.ICE)
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

    private static boolean isValidBasinSubstrate(BlockState state, Block springStoneBlock) {
        return state.is(Blocks.GRASS_BLOCK)
                || state.is(BlockTags.DIRT)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.CALCITE)
                || state.is(springStoneBlock)
                || state.is(BlockTags.BASE_STONE_OVERWORLD);
    }

    private static void shufflePositions(List<BlockPos> list, RandomSource random) {
        for (int i = list.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            BlockPos tmp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, tmp);
        }
    }
}
