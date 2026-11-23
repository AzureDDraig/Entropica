package ddraig.net.entropica.worldgen.feature;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Generates wind-sheared, lightning-struck ocean icebergs for Biome #11 ({@code 2.11 Storm-Swept Icebergs}).
 *
 * <p>Each iceberg anchors on open ocean water ({@code Y = 62..64}) or polar ice, extends a tapered
 * {@link Blocks#BLUE_ICE} &amp; {@link Blocks#PACKED_ICE} keel down to {@code seaLevel - 12} (~{@code Y = 51}),
 * and rises {@code 18..43} blocks ({@code Y = 81..107}) into an asymmetric storm-carved spire with a
 * {@link Blocks#SNOW_BLOCK}-crowned shoulder ledge, {@code entropica:zephyr_ice} wind ribs/overhangs,
 * {@code entropica:fulgurite_ice} lightning veins, {@code 1-3} fused {@code entropica:fulgurite_block}
 * summit crags, and {@code 2-5} tufts of {@code entropica:zephyr_glass_grass} strictly on snow or zephyr ice.</p>
 */
public class StormIcebergFeature extends Feature<NoneFeatureConfiguration> {

    private static final ResourceLocation ZEPHYR_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "zephyr_ice");
    private static final ResourceLocation FULGURITE_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "fulgurite_ice");
    private static final ResourceLocation FULGURITE_BLOCK_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "fulgurite_block");
    private static final ResourceLocation ZEPHYR_GLASS_GRASS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "zephyr_glass_grass");

    private static final int MAX_HORIZ_EXTENT = 11;

    public StormIcebergFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        Block zephyrIceBlock = BuiltInRegistries.BLOCK.getOptional(ZEPHYR_ICE_ID).orElse(Blocks.PACKED_ICE);
        Block fulguriteIceBlock = BuiltInRegistries.BLOCK.getOptional(FULGURITE_ICE_ID).orElse(Blocks.BLUE_ICE);
        Block fulguriteStoneBlock = BuiltInRegistries.BLOCK.getOptional(FULGURITE_BLOCK_ID).orElse(Blocks.DEEPSLATE);
        Optional<Block> zephyrGrassOpt = BuiltInRegistries.BLOCK.getOptional(ZEPHYR_GLASS_GRASS_ID);

        int seaLevel = level.getSeaLevel() > 0 ? level.getSeaLevel() : 63;
        BlockPos anchorPos = findValidAnchor(level, origin, seaLevel, zephyrIceBlock, fulguriteIceBlock);
        if (anchorPos == null) {
            return false;
        }

        int cx = anchorPos.getX();
        int anchorY = anchorPos.getY();
        int cz = anchorPos.getZ();

        // Avoid stacking directly on top of an already-generated tall iceberg or observatory
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int checkY = anchorY + 8; checkY <= anchorY + 18; checkY += 5) {
            cursor.set(cx, checkY, cz);
            if (!level.getBlockState(cursor).isAir()) {
                return false;
            }
        }

        int height = 18 + random.nextInt(26); // 18..43 -> reaches Y = 81..107
        int keelBottomY = Math.max(level.getMinY() + 4, seaLevel - 12 - random.nextInt(3));
        int peakY = Math.min(level.getMaxY() - 4, anchorY + height);
        int actualHeight = peakY - anchorY;
        if (actualHeight < 14) {
            return false;
        }

        double baseRadius = 5.6D + random.nextDouble() * 3.2D; // 5.6..8.8
        double windAngle = random.nextDouble() * (Math.PI * 2.0D);
        double windDirX = Math.cos(windAngle);
        double windDirZ = Math.sin(windAngle);
        double perpDirX = -windDirZ;
        double perpDirZ = windDirX;

        // Flatter snow-capped shoulder lobe offset from the main spire
        double shoulderOffset = baseRadius * (0.55D + random.nextDouble() * 0.25D);
        double shoulderCenterX = -windDirX * (shoulderOffset * 0.65D) + perpDirX * (shoulderOffset * 0.55D);
        double shoulderCenterZ = -windDirZ * (shoulderOffset * 0.65D) + perpDirZ * (shoulderOffset * 0.55D);
        double shoulderRadius = 3.2D + random.nextDouble() * 1.6D;
        int shoulderTopRy = Mth.clamp((int) Math.round(actualHeight * (0.42D + random.nextDouble() * 0.16D)), 8, actualHeight - 6);

        BlockState blueIce = Blocks.BLUE_ICE.defaultBlockState();
        BlockState packedIce = Blocks.PACKED_ICE.defaultBlockState();
        BlockState snowBlock = Blocks.SNOW_BLOCK.defaultBlockState();
        BlockState zephyrIce = zephyrIceBlock.defaultBlockState();
        BlockState fulguriteIce = fulguriteIceBlock.defaultBlockState();
        BlockState fulguriteBlock = fulguriteStoneBlock.defaultBlockState();

        // Track top-most placed Y for each column in [-11..11] x [-11..11]
        int gridSize = MAX_HORIZ_EXTENT * 2 + 1;
        int[][] columnTopY = new int[gridSize][gridSize];
        boolean[][] isShoulderColumn = new boolean[gridSize][gridSize];
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                columnTopY[i][j] = Integer.MIN_VALUE;
            }
        }

        // 1. Submerged Blue-Ice & Packed-Ice Keel (keelBottomY .. anchorY)
        int keelSpan = Math.max(1, anchorY - keelBottomY);
        for (int wy = keelBottomY; wy <= anchorY; wy++) {
            double keelFrac = (double) (wy - keelBottomY) / (double) keelSpan; // 0 at bottom tip -> 1 at waterline
            double layerRadius = 1.4D + (baseRadius + 0.8D) * Math.pow(keelFrac, 0.68D);

            for (int dx = -MAX_HORIZ_EXTENT; dx <= MAX_HORIZ_EXTENT; dx++) {
                for (int dz = -MAX_HORIZ_EXTENT; dz <= MAX_HORIZ_EXTENT; dz++) {
                    double angle = Math.atan2(dz, dx);
                    double ribNoise = 0.88D + 0.18D * Math.sin(angle * 3.0D + wy * 0.25D);
                    double sDist = Math.hypot(dx - shoulderCenterX * keelFrac, dz - shoulderCenterZ * keelFrac);
                    boolean inMain = Math.hypot(dx, dz) <= layerRadius * ribNoise;
                    boolean inShoulderKeel = keelFrac > 0.35D && sDist <= (shoulderRadius + 0.8D) * keelFrac;

                    if (!inMain && !inShoulderKeel) {
                        continue;
                    }

                    cursor.set(cx + dx, wy, cz + dz);
                    if (!canReplaceWithIceberg(level.getBlockState(cursor))) {
                        continue;
                    }

                    boolean innerCore = Math.hypot(dx, dz) <= layerRadius * 0.68D || wy >= anchorY - 1;
                    level.setBlock(cursor, innerCore ? blueIce : packedIce, 2);
                    columnTopY[dx + MAX_HORIZ_EXTENT][dz + MAX_HORIZ_EXTENT] = wy;
                }
            }
        }

        // 2. Wind-Sheared Above-Water Spire, Zephyr-Ice Overhangs & Shoulder Ledge (ry = 1 .. actualHeight)
        int summitDx = 0;
        int summitDz = 0;
        int summitWy = anchorY;

        for (int ry = 1; ry <= actualHeight; ry++) {
            int wy = anchorY + ry;
            double hFrac = (double) ry / (double) actualHeight;

            // Asymmetric windward shear: the spire leans downwind as elevation climbs
            double shearShift = Math.pow(hFrac, 1.15D) * 3.4D;
            double spireCenterX = windDirX * shearShift;
            double spireCenterZ = windDirZ * shearShift;

            double spireRadius = Math.max(0.85D, baseRadius * Math.pow(1.0D - hFrac, 0.78D));
            // Wind-carved upper overhang flare in the upper third
            double overhangBoost = (hFrac >= 0.55D && hFrac <= 0.86D)
                    ? Math.sin((hFrac - 0.55D) / 0.31D * Math.PI) * 1.15D
                    : 0.0D;

            for (int dx = -MAX_HORIZ_EXTENT; dx <= MAX_HORIZ_EXTENT; dx++) {
                for (int dz = -MAX_HORIZ_EXTENT; dz <= MAX_HORIZ_EXTENT; dz++) {
                    double relX = dx - spireCenterX;
                    double relZ = dz - spireCenterZ;
                    double alongWind = relX * windDirX + relZ * windDirZ;
                    double crossWind = relX * perpDirX + relZ * perpDirZ;

                    // Streamlined aerodynamic cross-section + fluted wind ribs
                    double flute = 0.88D + 0.16D * Math.cos(Math.atan2(relZ, relX) * 4.0D + ry * 0.18D);
                    double effectiveRadius = spireRadius * flute + (alongWind > 0.0D ? overhangBoost : -overhangBoost * 0.25D);
                    double anisoDist = Math.hypot(alongWind * 0.88D, crossWind * 1.08D);

                    boolean inSpire = anisoDist <= effectiveRadius;
                    double distToShoulder = Math.hypot(dx - shoulderCenterX, dz - shoulderCenterZ);
                    double shoulderTaper = ry <= shoulderTopRy
                            ? shoulderRadius * (1.0D - 0.22D * ((double) ry / (double) shoulderTopRy))
                            : 0.0D;
                    boolean inShoulder = ry <= shoulderTopRy && distToShoulder <= shoulderTaper;

                    if (!inSpire && !inShoulder) {
                        continue;
                    }

                    cursor.set(cx + dx, wy, cz + dz);
                    if (!canReplaceWithIceberg(level.getBlockState(cursor))) {
                        continue;
                    }

                    BlockState chosen;
                    if (hFrac <= 0.28D) {
                        // Wave-battered lower base & core
                        chosen = (anisoDist <= effectiveRadius * 0.65D || alongWind < 0.0D) ? blueIce : packedIce;
                    } else if (hFrac <= 0.66D) {
                        // Mid walls with wind-scoured Zephyr Ice ribs
                        boolean windRib = (alongWind > effectiveRadius * 0.38D) || (((dx * 3 + dz * 5 + ry) & 3) == 0);
                        chosen = (anisoDist <= effectiveRadius * 0.42D) ? blueIce : (windRib ? zephyrIce : packedIce);
                    } else {
                        // Upper wind-carved overhangs & storm spire
                        chosen = (anisoDist <= effectiveRadius * 0.35D) ? blueIce : zephyrIce;
                    }

                    level.setBlock(cursor, chosen, 2);
                    int gx = dx + MAX_HORIZ_EXTENT;
                    int gz = dz + MAX_HORIZ_EXTENT;
                    columnTopY[gx][gz] = wy;
                    if (inShoulder && !inSpire) {
                        isShoulderColumn[gx][gz] = true;
                    }
                    if (wy >= summitWy) {
                        summitWy = wy;
                        summitDx = dx;
                        summitDz = dz;
                    }
                }
            }
        }

        // 3. Snow-Block High Ledge Crown (top 2 layers on flatter shoulder & mid-elevation ledges)
        List<BlockPos> floraCandidateSurfaces = new ArrayList<>();
        for (int dx = -MAX_HORIZ_EXTENT + 1; dx <= MAX_HORIZ_EXTENT - 1; dx++) {
            for (int dz = -MAX_HORIZ_EXTENT + 1; dz <= MAX_HORIZ_EXTENT - 1; dz++) {
                int gx = dx + MAX_HORIZ_EXTENT;
                int gz = dz + MAX_HORIZ_EXTENT;
                int topY = columnTopY[gx][gz];
                if (topY < anchorY + 4) {
                    continue;
                }

                // Check local slope across cardinal neighbors to identify flatter shoulder cells
                int maxDiff = 0;
                int validNeighbors = 0;
                for (Direction horiz : Direction.Plane.HORIZONTAL) {
                    int nTop = columnTopY[gx + horiz.getStepX()][gz + horiz.getStepZ()];
                    if (nTop != Integer.MIN_VALUE) {
                        validNeighbors++;
                        maxDiff = Math.max(maxDiff, Math.abs(topY - nTop));
                    }
                }

                boolean flatterLedge = (isShoulderColumn[gx][gz] || (topY <= summitWy - 4 && validNeighbors >= 3 && maxDiff <= 2));
                if (flatterLedge) {
                    for (int depth = 0; depth < 2; depth++) {
                        int sy = topY - depth;
                        if (sy > anchorY + 1) {
                            cursor.set(cx + dx, sy, cz + dz);
                            level.setBlock(cursor, snowBlock, 2);
                        }
                    }
                }

                // Record valid Snow Block or Zephyr Ice top cells with clear air above for Zephyr-Glass Grass
                cursor.set(cx + dx, topY, cz + dz);
                BlockState topState = level.getBlockState(cursor);
                if ((topState.is(Blocks.SNOW_BLOCK) || topState.is(zephyrIceBlock))
                        && level.getBlockState(cursor.above()).isAir()) {
                    floraCandidateSurfaces.add(cursor.immutable());
                }
            }
        }

        // 4. Lightning-Struck Peak Veins of Fulgurite Ice + 1-3 Fused Fulgurite Block Summit Crags
        int cragCount = 1 + random.nextInt(3); // 1..3 fused fulgurite_block crags near the summit
        for (int c = 0; c < cragCount; c++) {
            int ox = summitDx + (c == 0 ? 0 : random.nextInt(3) - 1);
            int oz = summitDz + (c == 0 ? 0 : random.nextInt(3) - 1);
            if (Math.abs(ox) <= MAX_HORIZ_EXTENT && Math.abs(oz) <= MAX_HORIZ_EXTENT) {
                int colTop = columnTopY[ox + MAX_HORIZ_EXTENT][oz + MAX_HORIZ_EXTENT];
                int cy = (colTop != Integer.MIN_VALUE) ? colTop : summitWy - c;
                cursor.set(cx + ox, cy, cz + oz);
                level.setBlock(cursor, fulguriteBlock, 2);
                if (c == 0 && cy + 1 < level.getMaxY() - 2 && level.getBlockState(cursor.above()).isAir()) {
                    level.setBlock(cursor.above(), fulguriteBlock, 2);
                }
            }
        }

        // Jagged fulgurite_ice lightning veins coursing down from the summit
        int veinCount = 1 + random.nextInt(2);
        for (int v = 0; v < veinCount; v++) {
            double vx = summitDx;
            double vz = summitDz;
            double vAngle = windAngle + (v == 0 ? Math.PI : Math.PI * 0.45D) + (random.nextDouble() - 0.5D) * 0.6D;
            int veinSteps = Math.min(actualHeight - 2, 9 + random.nextInt(7));
            for (int s = 1; s <= veinSteps; s++) {
                int vy = summitWy - s;
                vx += Math.cos(vAngle) * 0.45D + (random.nextDouble() - 0.5D) * 0.35D;
                vz += Math.sin(vAngle) * 0.45D + (random.nextDouble() - 0.5D) * 0.35D;
                int ix = Mth.clamp((int) Math.round(vx), -MAX_HORIZ_EXTENT, MAX_HORIZ_EXTENT);
                int iz = Mth.clamp((int) Math.round(vz), -MAX_HORIZ_EXTENT, MAX_HORIZ_EXTENT);
                int surfaceAtVein = columnTopY[ix + MAX_HORIZ_EXTENT][iz + MAX_HORIZ_EXTENT];
                int targetY = (surfaceAtVein != Integer.MIN_VALUE) ? Math.min(vy, surfaceAtVein) : vy;
                if (targetY > anchorY + 1) {
                    cursor.set(cx + ix, targetY, cz + iz);
                    if (!level.getBlockState(cursor).is(fulguriteStoneBlock)) {
                        level.setBlock(cursor, fulguriteIce, 2);
                    }
                }
            }
        }

        // 5. Sprinkle 2-5 Tufts of entropica:zephyr_glass_grass STRICTLY on Snow Block or Zephyr Ice with Air Above
        if (zephyrGrassOpt.isPresent() && !floraCandidateSurfaces.isEmpty()) {
            Block grassBlock = zephyrGrassOpt.get();
            BlockState grassState = nonWaterlogged(grassBlock.defaultBlockState());
            int targetTufts = 2 + random.nextInt(4); // 2..5 tufts
            int placedTufts = 0;

            while (!floraCandidateSurfaces.isEmpty() && placedTufts < targetTufts) {
                int idx = random.nextInt(floraCandidateSurfaces.size());
                BlockPos groundPos = floraCandidateSurfaces.remove(idx);
                BlockState groundState = level.getBlockState(groundPos);

                // Re-verify ground wasn't converted to fulgurite_ice / fulgurite_block by step 4!
                if (!groundState.is(Blocks.SNOW_BLOCK) && !groundState.is(zephyrIceBlock)) {
                    continue;
                }
                BlockPos plantPos = groundPos.above();
                if (!level.getBlockState(plantPos).isAir()) {
                    continue;
                }
                if (grassState.canSurvive(level, plantPos)) {
                    level.setBlock(plantPos, grassState, 2);
                    placedTufts++;
                }
            }
        }

        return true;
    }

    private static BlockPos findValidAnchor(
            WorldGenLevel level,
            BlockPos origin,
            int seaLevel,
            Block zephyrIceBlock,
            Block fulguriteIceBlock
    ) {
        BlockPos.MutableBlockPos check = new BlockPos.MutableBlockPos();
        for (int dy = 3; dy >= -6; dy--) {
            int y = origin.getY() + dy;
            if (y <= level.getMinY() + 16 || y >= level.getMaxY() - 48) {
                continue;
            }
            check.set(origin.getX(), y, origin.getZ());
            BlockState state = level.getBlockState(check);
            BlockState above = level.getBlockState(check.above());
            if (!above.isAir() && !above.is(Blocks.SNOW)) {
                continue;
            }

            boolean isOceanWaterSurface = (y >= seaLevel - 2 && y <= seaLevel + 2)
                    && (state.is(Blocks.WATER) || state.getFluidState().getType() == Fluids.WATER);
            boolean isColdIceSurface = (y >= seaLevel - 2 && y <= seaLevel + 8)
                    && (state.is(Blocks.ICE)
                    || state.is(Blocks.PACKED_ICE)
                    || state.is(Blocks.BLUE_ICE)
                    || state.is(Blocks.SNOW_BLOCK)
                    || state.is(zephyrIceBlock)
                    || state.is(fulguriteIceBlock));

            if (isOceanWaterSurface || isColdIceSurface) {
                return check.immutable();
            }
        }
        return null;
    }

    private static boolean canReplaceWithIceberg(BlockState state) {
        return state.isAir()
                || state.is(Blocks.WATER)
                || state.is(Blocks.BUBBLE_COLUMN)
                || state.is(Blocks.ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.SNOW)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.POWDER_SNOW)
                || state.is(Blocks.GRAVEL)
                || state.canBeReplaced();
    }

    private static BlockState nonWaterlogged(BlockState state) {
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            return state.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        return state;
    }
}
