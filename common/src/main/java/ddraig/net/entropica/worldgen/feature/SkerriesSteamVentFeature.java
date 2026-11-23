package ddraig.net.entropica.worldgen.feature;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Generates natural underwater geothermal steam vents in {@code 2.2 Auroral Skerries}:
 * submerged {@link Blocks#SOUL_SAND} and {@link Blocks#MAGMA_BLOCK} vent throats at channel bed
 * {@code Y=59..60} beneath {@code 2+} blocks of warm channel water creating genuine rising
 * bubble columns, ringed by a low submerged collar of {@link ModBlocks#COARSE_GRAVEL},
 * {@link Blocks#BASALT}, and {@code 1-2} {@link ModBlocks#GLIMMER_ICE} shoreline accents.
 *
 * <p>Rising white steam columns are emitted directly from the bubbling water surface on the client
 * via {@code BiomeAtmosphereClientManager} — never via hidden campfires or hay bales.</p>
 */
public class SkerriesSteamVentFeature extends Feature<NoneFeatureConfiguration> {

    private static final int SEA_WATER_TOP_Y = 62;

    public SkerriesSteamVentFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        BlockPos ventAnchor = null;
        int foundBedY = 60;

        // Locate a shallow coastal channel water column near origin
        search:
        for (int r = 0; r <= 3; r++) {
            for (int attempt = 0; attempt < (r == 0 ? 1 : 6); attempt++) {
                int ox = r == 0 ? 0 : random.nextInt(r * 2 + 1) - r;
                int oz = r == 0 ? 0 : random.nextInt(r * 2 + 1) - r;
                int tx = origin.getX() + ox;
                int tz = origin.getZ() + oz;

                BlockState topWater = level.getBlockState(new BlockPos(tx, SEA_WATER_TOP_Y, tz));
                BlockState aboveWater = level.getBlockState(new BlockPos(tx, SEA_WATER_TOP_Y + 1, tz));
                boolean isSurfaceChannel = (topWater.is(Blocks.WATER) || topWater.is(Blocks.ICE))
                        && (aboveWater.isAir() || aboveWater.is(Blocks.SNOW));
                if (!isSurfaceChannel) {
                    continue;
                }

                for (int by = SEA_WATER_TOP_Y - 1; by >= 55; by--) {
                    BlockState s = level.getBlockState(new BlockPos(tx, by, tz));
                    if (!s.is(Blocks.WATER) && !s.is(Blocks.BUBBLE_COLUMN) && !s.is(Blocks.ICE) && !s.isAir()) {
                        ventAnchor = new BlockPos(tx, by, tz);
                        foundBedY = by;
                        break search;
                    }
                }
            }
        }

        if (ventAnchor == null) {
            return false;
        }

        int vx = ventAnchor.getX();
        int vz = ventAnchor.getZ();
        // Keep vent floor submerged at Y=59..60 under at least 2 full water blocks (Y=61..62)
        int ventBedY = Mth.clamp(foundBedY, 59, 60);

        BlockState basalt = Blocks.BASALT.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState coarseGravel = ModBlocks.COARSE_GRAVEL.get().defaultBlockState();
        BlockState glimmerIce = ModBlocks.GLIMMER_ICE.get().defaultBlockState();
        BlockState magma = Blocks.MAGMA_BLOCK.defaultBlockState();
        BlockState soulSand = Blocks.SOUL_SAND.defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        Direction secondaryVentDir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        Direction tertiaryVentDir = secondaryVentDir.getClockWise();
        boolean hasThirdThroat = random.nextBoolean();

        // 1. Build natural submerged basalt & coarse-gravel geothermal crater rim around (vx, vz)
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                double dist = Math.hypot(dx, dz);
                if (dist < 0.9D || dist > 2.45D) {
                    continue;
                }
                if (dx == secondaryVentDir.getStepX() && dz == secondaryVentDir.getStepZ()) {
                    continue;
                }
                if (hasThirdThroat && dx == tertiaryVentDir.getStepX() && dz == tertiaryVentDir.getStepZ()) {
                    continue;
                }

                int bx = vx + dx;
                int bz = vz + dz;
                int rimTopY = dist <= 1.55D
                        ? Math.min(SEA_WATER_TOP_Y - 1, ventBedY + 1 + random.nextInt(2))
                        : ventBedY + random.nextInt(2);

                fillBasaltSupportDown(level, bx, ventBedY - 1, bz, basalt);
                for (int y = ventBedY; y <= rimTopY; y++) {
                    cursor.set(bx, y, bz);
                    if (canReplaceTerrainOrWater(level.getBlockState(cursor))) {
                        boolean placeBasalt = (y < rimTopY) || (random.nextFloat() < 0.42F);
                        level.setBlock(cursor, placeBasalt ? basalt : coarseGravel, 2);
                    }
                }
            }
        }

        // 2. Place submerged rising-bubble vent throats (Soul Sand upward bubbles + Magma thermal glow)
        placeBubbleVentColumn(level, vx, ventBedY, vz, soulSand, false, basalt, air);
        placeBubbleVentColumn(
                level,
                vx + secondaryVentDir.getStepX(),
                ventBedY,
                vz + secondaryVentDir.getStepZ(),
                magma,
                true,
                basalt,
                air
        );
        if (hasThirdThroat) {
            placeBubbleVentColumn(
                    level,
                    vx + tertiaryVentDir.getStepX(),
                    ventBedY,
                    vz + tertiaryVentDir.getStepZ(),
                    soulSand,
                    false,
                    basalt,
                    air
            );
        }

        // 3. Accent the outer shoreline edge with 1-2 low Glimmer-Ice blocks where bordering shallow water
        int targetGlimmer = 1 + random.nextInt(2);
        int placedGlimmer = 0;
        Direction oppDir = secondaryVentDir.getOpposite();
        int[][] glimmerOffsets = {
                {oppDir.getStepX() * 2, oppDir.getStepZ() * 2},
                {-tertiaryVentDir.getStepX() * 2, -tertiaryVentDir.getStepZ() * 2}
        };
        for (int[] off : glimmerOffsets) {
            if (placedGlimmer >= targetGlimmer) {
                break;
            }
            BlockPos gPos = new BlockPos(vx + off[0], SEA_WATER_TOP_Y, vz + off[1]);
            if (canReplaceTerrainOrWater(level.getBlockState(gPos))) {
                fillBasaltSupportDown(level, gPos.getX(), SEA_WATER_TOP_Y - 1, gPos.getZ(), basalt);
                level.setBlock(gPos, glimmerIce, 2);
                placedGlimmer++;
            }
        }

        // Keep surrounding channel water surface unfrozen around the warm geothermal vent
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos adj = new BlockPos(vx + dx, SEA_WATER_TOP_Y, vz + dz);
                if (level.getBlockState(adj).is(Blocks.ICE)) {
                    level.setBlock(adj, water, 2);
                }
            }
        }

        return true;
    }

    private static void placeBubbleVentColumn(
            WorldGenLevel level,
            int x,
            int ventBedY,
            int z,
            BlockState bedBlock,
            boolean dragDown,
            BlockState basalt,
            BlockState air
    ) {
        fillBasaltSupportDown(level, x, ventBedY - 1, z, basalt);
        BlockPos bedPos = new BlockPos(x, ventBedY, z);
        level.setBlock(bedPos, bedBlock, 2);
        for (int y = ventBedY + 1; y <= SEA_WATER_TOP_Y; y++) {
            BlockPos colPos = new BlockPos(x, y, z);
            level.setBlock(colPos, Blocks.BUBBLE_COLUMN.defaultBlockState().setValue(BubbleColumnBlock.DRAG_DOWN, dragDown), 2);
        }
        BlockPos aboveWater = new BlockPos(x, SEA_WATER_TOP_Y + 1, z);
        if (level.getBlockState(aboveWater).is(Blocks.ICE) || level.getBlockState(aboveWater).is(Blocks.SNOW)) {
            level.setBlock(aboveWater, air, 2);
        }
        level.scheduleTick(bedPos, bedBlock.getBlock(), 2);
    }

    private static void fillBasaltSupportDown(WorldGenLevel level, int x, int startY, int z, BlockState basalt) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minY = Math.max(level.getMinY() + 2, startY - 8);
        for (int y = startY; y >= minY; y--) {
            cursor.set(x, y, z);
            BlockState existing = level.getBlockState(cursor);
            if (existing.isAir() || existing.is(Blocks.WATER) || existing.is(Blocks.ICE) || existing.canBeReplaced()) {
                level.setBlock(cursor, basalt, 2);
            } else {
                break;
            }
        }
    }

    private static boolean canReplaceTerrainOrWater(BlockState state) {
        return state.isAir()
                || state.is(Blocks.WATER)
                || state.is(Blocks.BUBBLE_COLUMN)
                || state.is(Blocks.ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.SNOW)
                || state.is(Blocks.POWDER_SNOW)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.GRAVEL)
                || state.is(ModBlocks.COARSE_GRAVEL.get())
                || state.is(BlockTags.DIRT)
                || state.is(BlockTags.BASE_STONE_OVERWORLD);
    }
}
