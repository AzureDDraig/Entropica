package ddraig.net.entropica.worldgen.feature;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Generates blocky, fragmented {@code 4x4} to {@code 9x9} drifting ice floes locked right at
 * sea level ({@code Y=63}) over {@link Blocks#WATER} in {@code 2.3 Frost-Bite Floes}, composed of
 * ~{@code 55%} {@code entropica:blighted_packed_ice}, ~{@code 25%} {@link Blocks#PACKED_ICE}, and
 * ~{@code 20%} treacherous {@code entropica:rotten_ice} trap pockets, topped with scattered
 * {@code entropica:necrotic_slime_patch} crusts.
 */
public class RottenIceFloeSheetFeature extends Feature<NoneFeatureConfiguration> {

    private static final int SEA_WATER_Y = 62;
    private static final int FLOE_SURFACE_Y = 63;

    private static final ResourceLocation BLIGHTED_PACKED_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "blighted_packed_ice");
    private static final ResourceLocation ROTTEN_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "rotten_ice");
    private static final ResourceLocation NECROTIC_SLIME_PATCH_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "necrotic_slime_patch");

    public RottenIceFloeSheetFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        BlockPos waterCenter = findOpenSwampWaterAnchor(level, origin, random);
        if (waterCenter == null) {
            return false;
        }

        BlockState blightedPackedIce = resolveBlock(BLIGHTED_PACKED_ICE_ID, Blocks.PACKED_ICE).defaultBlockState();
        BlockState packedIce = Blocks.PACKED_ICE.defaultBlockState();
        BlockState rottenIce = resolveBlock(ROTTEN_ICE_ID, Blocks.ICE).defaultBlockState();
        Optional<Block> slimePatchBlockOpt = BuiltInRegistries.BLOCK.getOptional(NECROTIC_SLIME_PATCH_ID);
        BlockState water = Blocks.WATER.defaultBlockState();

        int widthX = 4 + random.nextInt(6); // 4..9
        int widthZ = 4 + random.nextInt(6); // 4..9
        int minDx = -(widthX / 2);
        int maxDx = minDx + widthX - 1;
        int minDz = -(widthZ / 2);
        int maxDz = minDz + widthZ - 1;

        // Corner notches give each pack-ice sheet a fractured, blocky polygon outline
        int notchNW = random.nextInt(3); // 0..2
        int notchNE = random.nextInt(3);
        int notchSW = random.nextInt(3);
        int notchSE = random.nextInt(3);

        // Larger floes occasionally split along an internal 1-block fracture crack channel
        boolean hasFractureSplit = (widthX >= 7 || widthZ >= 7) && random.nextFloat() < 0.45F;
        boolean splitAlongX = widthZ >= widthX;
        int fractureCoord = splitAlongX
                ? Mth.clamp(random.nextInt(3) - 1, minDz + 2, maxDz - 2)
                : Mth.clamp(random.nextInt(3) - 1, minDx + 2, maxDx - 2);
        int fractureWobble = random.nextBoolean() ? 1 : -1;

        List<BlockPos> placedFloePositions = new ArrayList<>(widthX * widthZ);
        BlockPos.MutableBlockPos waterPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos floePos = new BlockPos.MutableBlockPos();

        for (int dx = minDx; dx <= maxDx; dx++) {
            for (int dz = minDz; dz <= maxDz; dz++) {
                int bx = waterCenter.getX() + dx;
                int bz = waterCenter.getZ() + dz;
                if (Math.abs(bx - origin.getX()) > 7 || Math.abs(bz - origin.getZ()) > 7) {
                    continue;
                }

                // Carve blocky rectangular corner notches
                int distLeft = dx - minDx;
                int distRight = maxDx - dx;
                int distTop = dz - minDz;
                int distBottom = maxDz - dz;
                if (distLeft + distTop < notchNW
                        || distRight + distTop < notchNE
                        || distLeft + distBottom < notchSW
                        || distRight + distBottom < notchSE) {
                    continue;
                }

                // Occasional stepped edge bite along outer rim
                boolean isOuterEdge = (dx == minDx || dx == maxDx || dz == minDz || dz == maxDz);
                if (isOuterEdge && widthX >= 5 && widthZ >= 5 && random.nextFloat() < 0.22F) {
                    continue;
                }

                // Internal jagged fracture gap on wide pack-ice plates
                if (hasFractureSplit) {
                    if (splitAlongX) {
                        int crackZ = fractureCoord + (dx > 0 ? fractureWobble : 0);
                        if (dz == crackZ && random.nextFloat() < 0.80F) {
                            continue;
                        }
                    } else {
                        int crackX = fractureCoord + (dz > 0 ? fractureWobble : 0);
                        if (dx == crackX && random.nextFloat() < 0.80F) {
                            continue;
                        }
                    }
                }

                waterPos.set(bx, SEA_WATER_Y, bz);
                floePos.set(bx, FLOE_SURFACE_Y, bz);

                BlockState belowState = level.getBlockState(waterPos);
                BlockState surfaceState = level.getBlockState(floePos);

                if (!isSwampWaterOrSurfaceIce(belowState) || !canFloeReplace(surfaceState)) {
                    continue;
                }

                // Keep liquid swamp water directly under the Y=63 floe so blighted_packed_ice emits
                // underwater decay spores and rotten_ice trap pockets collapse straight into water.
                if (!belowState.is(Blocks.WATER)) {
                    level.setBlock(waterPos, water, 2);
                }

                // Composition: ~55% blighted_packed_ice, ~25% packed_ice, ~20% rotten_ice
                float roll = random.nextFloat();
                BlockState chosenIce;
                if (roll < 0.55F) {
                    chosenIce = blightedPackedIce;
                } else if (roll < 0.80F) {
                    chosenIce = packedIce;
                } else {
                    chosenIce = rottenIce;
                }

                level.setBlock(floePos, chosenIce, 2);
                placedFloePositions.add(floePos.immutable());
            }
        }

        if (placedFloePositions.isEmpty()) {
            return false;
        }

        // Top scattered floe tiles with necrotic_slime_patch crusts at Y=64
        if (slimePatchBlockOpt.isPresent()) {
            BlockState slimePatch = slimePatchBlockOpt.get().defaultBlockState();
            for (BlockPos icePos : placedFloePositions) {
                if (random.nextFloat() < 0.22F) {
                    BlockPos topPos = icePos.above();
                    BlockState above = level.getBlockState(topPos);
                    if ((above.isAir() || above.is(Blocks.SNOW)) && slimePatch.canSurvive(level, topPos)) {
                        level.setBlock(topPos, slimePatch, 2);
                    }
                }
            }
        }

        return true;
    }

    private static BlockPos findOpenSwampWaterAnchor(WorldGenLevel level, BlockPos origin, RandomSource random) {
        BlockPos.MutableBlockPos waterCheck = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos airCheck = new BlockPos.MutableBlockPos();

        for (int r = 0; r <= 4; r++) {
            int attempts = r == 0 ? 1 : 6;
            for (int a = 0; a < attempts; a++) {
                int ox = r == 0 ? 0 : (random.nextInt(r * 2 + 1) - r);
                int oz = r == 0 ? 0 : (random.nextInt(r * 2 + 1) - r);
                int tx = origin.getX() + ox;
                int tz = origin.getZ() + oz;

                waterCheck.set(tx, SEA_WATER_Y, tz);
                airCheck.set(tx, FLOE_SURFACE_Y, tz);
                if (isSwampWaterOrSurfaceIce(level.getBlockState(waterCheck))
                        && canFloeReplace(level.getBlockState(airCheck))) {
                    return new BlockPos(tx, FLOE_SURFACE_Y, tz);
                }
            }
        }
        return null;
    }

    private static boolean isSwampWaterOrSurfaceIce(BlockState state) {
        return state.is(Blocks.WATER) || state.is(Blocks.ICE);
    }

    private static boolean canFloeReplace(BlockState state) {
        return state.isAir()
                || state.is(Blocks.SNOW)
                || state.is(Blocks.POWDER_SNOW)
                || state.is(Blocks.ICE);
    }

    private static Block resolveBlock(ResourceLocation id, Block fallback) {
        return BuiltInRegistries.BLOCK.getOptional(id).orElse(fallback);
    }
}
