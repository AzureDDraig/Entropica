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
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;

/**
 * Carves shallow ({@code 1-2} block deep) clear warm geothermal water pools into flat
 * {@link Blocks#BLUE_ICE}, {@link Blocks#PACKED_ICE}, and {@code entropica:steaming_ice} coastal
 * plates around {@code Y=62..64} in {@code 2.6 Steaming Skerries}, lining rims with
 * {@code steaming_ice} and {@code steaming_spring_stone}, embedding {@code 1-2} bubbling
 * {@code steam_vent} blocks on the pool bed/rim (never campfires), and scattering
 * {@code mist_veil_fern} along warm perimeter cracks.
 */
public class SteamingGeyserPoolFeature extends Feature<NoneFeatureConfiguration> {

    private static final ResourceLocation STEAMING_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "steaming_ice");
    private static final ResourceLocation STEAMING_SPRING_STONE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "steaming_spring_stone");
    private static final ResourceLocation STEAM_VENT_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "steam_vent");
    private static final ResourceLocation MIST_VEIL_FERN_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mist_veil_fern");

    public SteamingGeyserPoolFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        Block steamingIceBlock = resolveBlock(STEAMING_ICE_ID, Blocks.BLUE_ICE);
        Block springStoneBlock = resolveBlock(STEAMING_SPRING_STONE_ID, Blocks.BASALT);
        Block steamVentBlock = resolveBlock(STEAM_VENT_ID, Blocks.MAGMA_BLOCK);
        Block mistVeilFernBlock = resolveBlock(MIST_VEIL_FERN_ID, Blocks.FERN);

        double radiusX = 2.15D + random.nextDouble() * 1.05D;
        double radiusZ = 2.15D + random.nextDouble() * 1.05D;

        BlockPos anchor = findFlatPlateAnchor(level, origin, random, radiusX, radiusZ, steamingIceBlock, springStoneBlock);
        if (anchor == null) {
            return false;
        }

        int cx = anchor.getX();
        int poolWaterY = anchor.getY(); // 62..65
        int cz = anchor.getZ();
        int poolDepth = 1 + random.nextInt(2); // 1..2 blocks deep
        int poolBedY = poolWaterY - poolDepth;

        BlockState steamingIce = steamingIceBlock.defaultBlockState();
        BlockState springStone = springStoneBlock.defaultBlockState();
        BlockState steamVent = steamVentBlock.defaultBlockState();
        BlockState mistVeilFern = mistVeilFernBlock.defaultBlockState();
        BlockState packedIce = Blocks.PACKED_ICE.defaultBlockState();
        BlockState blueIce = Blocks.BLUE_ICE.defaultBlockState();
        BlockState basalt = Blocks.BASALT.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        int maxDx = (int) Math.ceil(radiusX * 1.45D) + 1;
        int maxDz = (int) Math.ceil(radiusZ * 1.45D) + 1;

        List<BlockPos> bedCells = new ArrayList<>();
        List<BlockPos> rimCells = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        // 1. Carve shallow warm pool basin & seal perimeter rim with steaming_ice and steaming_spring_stone
        for (int dx = -maxDx; dx <= maxDx; dx++) {
            for (int dz = -maxDz; dz <= maxDz; dz++) {
                double wobble = 0.08D * Math.sin((cx + dx) * 0.9D + (cz + dz) * 1.1D);
                double normDist = Math.hypot(dx / radiusX, dz / radiusZ) + wobble;
                if (normDist > 1.38D) {
                    continue;
                }

                int wx = cx + dx;
                int wz = cz + dz;

                if (normDist < 0.88D) {
                    // Sub-bed seal so pool water never leaks into underlying pockets
                    cursor.set(wx, poolBedY - 1, wz);
                    level.setBlock(cursor, (random.nextFloat() < 0.65F) ? packedIce : basalt, 2);
                    sealSupportDown(level, wx, poolBedY - 2, wz, basalt);

                    // Pool bed at poolBedY
                    BlockState bedState;
                    float bedRoll = random.nextFloat();
                    if (bedRoll < 0.60F) {
                        bedState = springStone;
                    } else if (bedRoll < 0.85F) {
                        bedState = steamingIce;
                    } else {
                        bedState = basalt;
                    }
                    BlockPos bedPos = new BlockPos(wx, poolBedY, wz);
                    level.setBlock(bedPos, bedState, 2);
                    bedCells.add(bedPos);

                    // Clear warm water column (1..2 blocks deep)
                    for (int y = poolBedY + 1; y <= poolWaterY; y++) {
                        cursor.set(wx, y, wz);
                        level.setBlock(cursor, water, 2);
                    }

                    // Clear ice/snow/overhangs directly above the warm pool so steam rises cleanly
                    for (int y = poolWaterY + 1; y <= poolWaterY + 3; y++) {
                        cursor.set(wx, y, wz);
                        BlockState above = level.getBlockState(cursor);
                        if (isRemovableOverburden(above, steamingIceBlock, springStoneBlock)) {
                            level.setBlock(cursor, air, 2);
                        }
                    }
                } else {
                    // Watertight geothermal rim collar around the pool (normDist 0.88..1.38)
                    sealSupportDown(level, wx, poolBedY - 1, wz, packedIce);
                    for (int y = poolBedY; y <= poolWaterY; y++) {
                        cursor.set(wx, y, wz);
                        BlockState rimState;
                        if (y < poolWaterY) {
                            rimState = (random.nextBoolean()) ? steamingIce : springStone;
                        } else {
                            float rimRoll = random.nextFloat();
                            if (rimRoll < 0.46F) {
                                rimState = steamingIce;
                            } else if (rimRoll < 0.82F) {
                                rimState = springStone;
                            } else if (rimRoll < 0.92F) {
                                rimState = basalt;
                            } else {
                                rimState = blueIce;
                            }
                        }
                        level.setBlock(cursor, rimState, 2);
                    }

                    // Clear snow/ice crust above the warm rim so rim stays dry and walkable
                    cursor.set(wx, poolWaterY + 1, wz);
                    BlockState aboveRim = level.getBlockState(cursor);
                    if (aboveRim.is(Blocks.SNOW) || aboveRim.is(Blocks.POWDER_SNOW) || aboveRim.is(Blocks.ICE)) {
                        level.setBlock(cursor, air, 2);
                    }

                    rimCells.add(new BlockPos(wx, poolWaterY, wz));
                }
            }
        }

        if (bedCells.isEmpty()) {
            return false;
        }

        // 2. Place 1-2 bubbling steam_vent blocks on the pool bed and/or warm rim
        BlockPos primaryVentPos = new BlockPos(cx, poolBedY, cz);
        level.setBlock(primaryVentPos, steamVent, 2);
        level.scheduleTick(primaryVentPos, steamVentBlock, 2);
        level.scheduleTick(primaryVentPos.above(), Fluids.WATER, 2);

        if (random.nextBoolean()) {
            boolean ventOnRim = !rimCells.isEmpty() && random.nextFloat() < 0.40F;
            if (ventOnRim) {
                BlockPos rimVentPos = rimCells.get(random.nextInt(rimCells.size()));
                level.setBlock(rimVentPos, steamVent, 2);
                level.scheduleTick(rimVentPos, steamVentBlock, 2);
            } else {
                Direction offsetDir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                BlockPos secondBedVent = new BlockPos(cx + offsetDir.getStepX(), poolBedY, cz + offsetDir.getStepZ());
                if (bedCells.contains(secondBedVent)) {
                    level.setBlock(secondBedVent, steamVent, 2);
                    level.scheduleTick(secondBedVent, steamVentBlock, 2);
                    level.scheduleTick(secondBedVent.above(), Fluids.WATER, 2);
                }
            }
        }

        // 3. Scatter 2-5 mist_veil_fern tufts along warm cracks on the pool rim
        shufflePositions(rimCells, random);
        int targetFerns = 2 + random.nextInt(4);
        int placedFerns = 0;
        for (BlockPos rimTop : rimCells) {
            if (placedFerns >= targetFerns) {
                break;
            }
            if (level.getBlockState(rimTop).is(steamVentBlock)) {
                continue;
            }
            BlockPos fernPos = rimTop.above();
            if (!level.getBlockState(fernPos).isAir()) {
                continue;
            }
            if (!mistVeilFern.canSurvive(level, fernPos)) {
                // Seat a warm basalt crack block beneath the fern if the rim block doesn't support vegetation
                level.setBlock(rimTop, basalt, 2);
            }
            if (mistVeilFern.canSurvive(level, fernPos)) {
                level.setBlock(fernPos, mistVeilFern, 2);
                placedFerns++;
            }
        }

        return true;
    }

    private static BlockPos findFlatPlateAnchor(
            WorldGenLevel level,
            BlockPos origin,
            RandomSource random,
            double radiusX,
            double radiusZ,
            Block steamingIceBlock,
            Block springStoneBlock
    ) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int r = 0; r <= 2; r++) {
            int attempts = (r == 0) ? 1 : 6;
            for (int a = 0; a < attempts; a++) {
                int ox = (r == 0) ? 0 : random.nextInt(r * 2 + 1) - r;
                int oz = (r == 0) ? 0 : random.nextInt(r * 2 + 1) - r;
                int tx = origin.getX() + ox;
                int tz = origin.getZ() + oz;

                int topY = findPlateTopY(level, tx, origin.getY(), tz, steamingIceBlock, springStoneBlock);
                if (topY < 62 || topY > 65) {
                    continue;
                }
                if (isContainedShelfFootprint(level, tx, topY, tz, radiusX, radiusZ, steamingIceBlock, springStoneBlock, cursor)) {
                    return new BlockPos(tx, topY, tz);
                }
            }
        }
        return null;
    }

    private static int findPlateTopY(
            WorldGenLevel level,
            int x,
            int hintY,
            int z,
            Block steamingIceBlock,
            Block springStoneBlock
    ) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int startY = Math.min(66, Math.max(62, hintY + 2));
        for (int y = startY; y >= 62; y--) {
            cursor.set(x, y, z);
            BlockState ground = level.getBlockState(cursor);
            BlockState above = level.getBlockState(cursor.above());
            if (isShelfPlateBlock(ground, steamingIceBlock, springStoneBlock)
                    && (above.isAir() || above.is(Blocks.SNOW) || above.is(Blocks.POWDER_SNOW))) {
                return y;
            }
        }
        return Integer.MIN_VALUE;
    }

    private static boolean isContainedShelfFootprint(
            WorldGenLevel level,
            int cx,
            int waterY,
            int cz,
            double radiusX,
            double radiusZ,
            Block steamingIceBlock,
            Block springStoneBlock,
            BlockPos.MutableBlockPos cursor
    ) {
        int checkDx = (int) Math.ceil(radiusX * 1.45D) + 1;
        int checkDz = (int) Math.ceil(radiusZ * 1.45D) + 1;
        for (int dx = -checkDx; dx <= checkDx; dx++) {
            for (int dz = -checkDz; dz <= checkDz; dz++) {
                double norm = Math.hypot(dx / radiusX, dz / radiusZ);
                if (norm > 1.42D) {
                    continue;
                }
                cursor.set(cx + dx, waterY - 1, cz + dz);
                BlockState sub = level.getBlockState(cursor);
                if (sub.isAir() || sub.is(Blocks.LAVA)) {
                    return false;
                }
                // Outer rim must already be backed by solid shelf or sea-level ice/water so pool never spills sideways
                if (norm >= 0.85D) {
                    cursor.set(cx + dx, waterY, cz + dz);
                    BlockState rimExisting = level.getBlockState(cursor);
                    boolean validBacking = isShelfPlateBlock(rimExisting, steamingIceBlock, springStoneBlock)
                            || (waterY == 62 && (rimExisting.is(Blocks.WATER) || rimExisting.is(Blocks.ICE)));
                    if (!validBacking) {
                        return false;
                    }
                    cursor.set(cx + dx, waterY + 2, cz + dz);
                    if (!level.getBlockState(cursor).isAir() && !level.getBlockState(cursor).is(Blocks.SNOW)) {
                        return false; // Avoid carving into steep cliff walls
                    }
                }
            }
        }
        return true;
    }

    private static void sealSupportDown(WorldGenLevel level, int x, int startY, int z, BlockState support) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minY = Math.max(level.getMinY() + 2, startY - 6);
        for (int y = startY; y >= minY; y--) {
            cursor.set(x, y, z);
            BlockState existing = level.getBlockState(cursor);
            if (existing.isAir() || existing.is(Blocks.WATER) || existing.is(Blocks.ICE) || existing.canBeReplaced()) {
                level.setBlock(cursor, support, 2);
            } else {
                break;
            }
        }
    }

    private static boolean isShelfPlateBlock(BlockState state, Block steamingIceBlock, Block springStoneBlock) {
        return state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.ICE)
                || state.is(steamingIceBlock)
                || state.is(springStoneBlock)
                || state.is(Blocks.BASALT)
                || state.is(Blocks.SMOOTH_BASALT)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.GRAVEL)
                || state.is(BlockTags.BASE_STONE_OVERWORLD);
    }

    private static boolean isRemovableOverburden(BlockState state, Block steamingIceBlock, Block springStoneBlock) {
        return state.is(Blocks.SNOW)
                || state.is(Blocks.POWDER_SNOW)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.BLUE_ICE)
                || state.is(steamingIceBlock)
                || state.is(springStoneBlock);
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
