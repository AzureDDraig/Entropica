package ddraig.net.entropica.worldgen.feature;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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
 * Generates organic coastal bay pack-ice rafts ({@code radius 3..6}) floating on shallow sea-level
 * {@link Blocks#WATER} ({@code Y = 62}) in Biome #12 ({@code 2.12 Spore-Glazed Pack}):
 * <ul>
 *   <li>{@code 2}-block-thick core of {@code entropica:spore_glazed_ice} &amp; {@link Blocks#BLUE_ICE}
 *       ({@code Y = 61..62})</li>
 *   <li>Topped at the center ({@code Y = 63}, {@code 1} block above water) with a cushioned mound of
 *       {@code entropica:frozen_spore_moss}</li>
 *   <li>Garnished strictly atop {@code frozen_spore_moss} ({@code Y = 64}) with occasional
 *       {@code entropica:frozen_spore_moss_carpet} or {@code entropica:spore_burst_puffball}
 *       (NEVER on bare regular ice!)</li>
 * </ul>
 */
public class SporeGlazedFloeFeature extends Feature<NoneFeatureConfiguration> {

    private static final ResourceLocation SPORE_GLAZED_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spore_glazed_ice");
    private static final ResourceLocation FROZEN_SPORE_MOSS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "frozen_spore_moss");
    private static final ResourceLocation FROZEN_SPORE_MOSS_CARPET_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "frozen_spore_moss_carpet");
    private static final ResourceLocation SPORE_BURST_PUFFBALL_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spore_burst_puffball");

    public SporeGlazedFloeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        int seaLevel = level.getSeaLevel() > 0 ? level.getSeaLevel() : 63;
        int waterTopY = seaLevel - 1; // Y = 62 in standard Overworld

        BlockPos waterCenter = findCoastalWaterAnchor(level, origin, waterTopY, random);
        if (waterCenter == null) {
            return false;
        }

        Block sporeGlazedIceBlock = BuiltInRegistries.BLOCK.getOptional(SPORE_GLAZED_ICE_ID).orElse(Blocks.PACKED_ICE);
        Block frozenSporeMossBlock = BuiltInRegistries.BLOCK.getOptional(FROZEN_SPORE_MOSS_ID).orElse(Blocks.MOSS_BLOCK);
        Optional<Block> mossCarpetOpt = BuiltInRegistries.BLOCK.getOptional(FROZEN_SPORE_MOSS_CARPET_ID);
        Optional<Block> puffballOpt = BuiltInRegistries.BLOCK.getOptional(SPORE_BURST_PUFFBALL_ID);

        BlockState sporeGlazedIce = sporeGlazedIceBlock.defaultBlockState();
        BlockState blueIce = Blocks.BLUE_ICE.defaultBlockState();
        BlockState frozenSporeMoss = frozenSporeMossBlock.defaultBlockState();

        int baseRadius = 3 + random.nextInt(4); // 3..6
        double stretchX = 0.85D + random.nextDouble() * 0.35D;
        double stretchZ = 0.85D + random.nextDouble() * 0.35D;
        double phase = random.nextDouble() * Math.PI * 2.0D;

        int cx = waterCenter.getX();
        int cz = waterCenter.getZ();

        List<BlockPos> mossMoundTopPositions = new ArrayList<>();
        BlockPos.MutableBlockPos keelPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos surfacePos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos moundPos = new BlockPos.MutableBlockPos();
        int placedIceColumns = 0;

        for (int dx = -baseRadius - 1; dx <= baseRadius + 1; dx++) {
            for (int dz = -baseRadius - 1; dz <= baseRadius + 1; dz++) {
                double angle = Math.atan2(dz, dx);
                double lobeFactor = 0.90D + 0.16D * Math.sin(angle * 3.0D + phase)
                        + 0.08D * Math.cos(angle * 5.0D - phase);
                double localRadius = baseRadius * lobeFactor;
                double anisoDist = Math.hypot(dx * stretchX, dz * stretchZ);

                if (anisoDist > localRadius) {
                    continue;
                }

                int bx = cx + dx;
                int bz = cz + dz;

                surfacePos.set(bx, waterTopY, bz);
                moundPos.set(bx, waterTopY + 1, bz);

                BlockState atWaterTop = level.getBlockState(surfacePos);
                BlockState aboveWater = level.getBlockState(moundPos);

                if (!isBayWaterOrThinIce(atWaterTop) || !canFloeMoundReplace(aboveWater)) {
                    continue;
                }

                // 1. Waterline raft layer (Y = 62): core of entropica:spore_glazed_ice & Blocks.BLUE_ICE
                boolean glazedPrimary = (anisoDist <= localRadius * 0.72D)
                        ? (random.nextFloat() < 0.68F)
                        : (random.nextFloat() < 0.48F);
                level.setBlock(surfacePos, glazedPrimary ? sporeGlazedIce : blueIce, 2);
                placedIceColumns++;

                // 2. Submerged 2nd raft thickness layer (Y = 61)
                if (anisoDist <= localRadius - 0.35D) {
                    keelPos.set(bx, waterTopY - 1, bz);
                    if (canSubmergedRaftReplace(level.getBlockState(keelPos))) {
                        BlockState keelIce = (random.nextFloat() < 0.58F) ? blueIce : sporeGlazedIce;
                        level.setBlock(keelPos, keelIce, 2);
                    }
                }

                // 3. Cushioned center mound (1 block above water at Y = 63) of entropica:frozen_spore_moss
                double mossRadius = Math.max(1.45D, localRadius * 0.56D);
                if (anisoDist <= mossRadius) {
                    level.setBlock(moundPos, frozenSporeMoss, 2);
                    mossMoundTopPositions.add(moundPos.immutable());
                }
            }
        }

        if (placedIceColumns < 6) {
            return false;
        }

        // 4. Occasional spore_burst_puffball or frozen_spore_moss_carpet STRICTLY atop frozen_spore_moss (Y = 64)
        for (BlockPos mossPos : mossMoundTopPositions) {
            BlockPos floraPos = mossPos.above();
            BlockState aboveMoss = level.getBlockState(floraPos);
            if (!aboveMoss.isAir() && !aboveMoss.is(Blocks.SNOW)) {
                continue;
            }

            float roll = random.nextFloat();
            if (roll < 0.14F && puffballOpt.isPresent()) {
                BlockState puffball = nonWaterlogged(puffballOpt.get().defaultBlockState());
                if (puffball.canSurvive(level, floraPos)) {
                    level.setBlock(floraPos, puffball, 2);
                    continue;
                }
            }
            if (roll < 0.38F && mossCarpetOpt.isPresent()) {
                BlockState carpet = nonWaterlogged(mossCarpetOpt.get().defaultBlockState());
                if (carpet.canSurvive(level, floraPos)) {
                    level.setBlock(floraPos, carpet, 2);
                }
            }
        }

        return true;
    }

    private static BlockPos findCoastalWaterAnchor(
            WorldGenLevel level,
            BlockPos origin,
            int waterTopY,
            RandomSource random
    ) {
        BlockPos.MutableBlockPos waterCheck = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos airCheck = new BlockPos.MutableBlockPos();

        for (int r = 0; r <= 4; r++) {
            int attempts = (r == 0) ? 1 : 6;
            for (int a = 0; a < attempts; a++) {
                int ox = (r == 0) ? 0 : (random.nextInt(r * 2 + 1) - r);
                int oz = (r == 0) ? 0 : (random.nextInt(r * 2 + 1) - r);
                int tx = origin.getX() + ox;
                int tz = origin.getZ() + oz;

                waterCheck.set(tx, waterTopY, tz);
                airCheck.set(tx, waterTopY + 1, tz);
                if (!isBayWaterOrThinIce(level.getBlockState(waterCheck))
                        || !canFloeMoundReplace(level.getBlockState(airCheck))) {
                    continue;
                }

                // Require at least 3 of 4 cardinal neighbors at distance 2 to also be bay water
                int waterNeighbors = 0;
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    waterCheck.set(tx + dir.getStepX() * 2, waterTopY, tz + dir.getStepZ() * 2);
                    if (isBayWaterOrThinIce(level.getBlockState(waterCheck))) {
                        waterNeighbors++;
                    }
                }
                if (waterNeighbors >= 3) {
                    return new BlockPos(tx, waterTopY, tz);
                }
            }
        }
        return null;
    }

    private static boolean isBayWaterOrThinIce(BlockState state) {
        return state.is(Blocks.WATER)
                || state.getFluidState().getType() == Fluids.WATER
                || state.is(Blocks.ICE);
    }

    private static boolean canSubmergedRaftReplace(BlockState state) {
        return isBayWaterOrThinIce(state)
                || state.is(Blocks.MUD)
                || state.is(Blocks.CLAY)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.SAND);
    }

    private static boolean canFloeMoundReplace(BlockState state) {
        return state.isAir()
                || state.is(Blocks.SNOW)
                || state.is(Blocks.POWDER_SNOW);
    }

    private static BlockState nonWaterlogged(BlockState state) {
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            return state.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        return state;
    }
}
