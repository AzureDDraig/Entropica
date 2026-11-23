package ddraig.net.entropica.worldgen.feature;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Generates geometric tapering hexagonal/diamond crystalline spires ({@code 6-13} blocks tall)
 * in {@code 2.4 Crystalline Pack-Ice}, composed of glowing {@code entropica:glimmer_ice} (~55%),
 * {@link Blocks#BLUE_ICE} (~30%), and {@link Blocks#PACKED_ICE} (~15%), anchored 2 blocks into
 * the snow/ice/stone terrain and collared with {@code 1-3} {@code entropica:aegis_quartz_block}s
 * sprouting directional {@code entropica:aegis_quartz_cluster}s plus {@code 1-2} adjacent
 * {@code entropica:rimebloom} blossoms.
 */
public class GlimmerIceSpireFeature extends Feature<NoneFeatureConfiguration> {

    private static final ResourceLocation GLIMMER_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "glimmer_ice");
    private static final ResourceLocation AEGIS_QUARTZ_BLOCK_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "aegis_quartz_block");
    private static final ResourceLocation AEGIS_QUARTZ_CLUSTER_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "aegis_quartz_cluster");
    private static final ResourceLocation RIMEBLOOM_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "rimebloom");

    private static final int[][] HEX_AXIS_A = {
            {0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, -1}, {-1, 1}
    };
    private static final int[][] HEX_AXIS_B = {
            {0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, -1}
    };
    private static final int[][] DIAMOND_OFFSETS = {
            {0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };
    private static final int[][] COLLAR_RING_OFFSETS = {
            {2, 0}, {-2, 0}, {0, 2}, {0, -2},
            {1, 1}, {-1, -1}, {1, -1}, {-1, 1}
    };

    public GlimmerIceSpireFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        // Locate solid snow, ice, or stone ground near origin, rejecting open water/lava
        BlockPos groundSurface = findGroundSurface(level, origin);
        if (groundSurface == null) {
            return false;
        }

        int height = 6 + random.nextInt(8); // 6..13 blocks tall above ground
        if (groundSurface.getY() + height + 2 >= level.getMaxY()) {
            return false;
        }

        // Verify surrounding 3x3 base has solid footing within 4 blocks so spires never overhang cliffs or water
        for (Direction card : Direction.Plane.HORIZONTAL) {
            if (!hasNearbyFooting(level, groundSurface.relative(card))) {
                return false;
            }
        }

        Block glimmerIceBlock = resolveBlock(GLIMMER_ICE_ID, () -> ModBlocks.GLIMMER_ICE.get(), Blocks.BLUE_ICE);
        Block aegisQuartzBlock = resolveBlock(AEGIS_QUARTZ_BLOCK_ID, () -> ModBlocks.AEGIS_QUARTZ_BLOCK.get(), Blocks.QUARTZ_BLOCK);
        Block aegisQuartzCluster = resolveBlock(AEGIS_QUARTZ_CLUSTER_ID, () -> ModBlocks.AEGIS_QUARTZ_CLUSTER.get(), Blocks.AMETHYST_CLUSTER);
        Block rimebloomBlock = resolveBlock(RIMEBLOOM_ID, () -> ModBlocks.RIMEBLOOM.get(), Blocks.WHITE_TULIP);

        BlockState glimmerState = glimmerIceBlock.defaultBlockState();
        BlockState blueIceState = Blocks.BLUE_ICE.defaultBlockState();
        BlockState packedIceState = Blocks.PACKED_ICE.defaultBlockState();

        int[][] baseHexOffsets = random.nextBoolean() ? HEX_AXIS_A : HEX_AXIS_B;
        // Pick two opposing cardinal ribs that climb slightly higher before tapering into the sharp 1x1 needle
        boolean northSouthRibs = random.nextBoolean();

        int baseX = groundSurface.getX();
        int baseY = groundSurface.getY() + 1; // first block above ground
        int baseZ = groundSurface.getZ();

        // 1. Anchor all base hexagonal columns 2 blocks deep into the terrain (and fill any slope gap down to 4 blocks)
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int[] off : baseHexOffsets) {
            int cx = baseX + off[0];
            int cz = baseZ + off[1];
            for (int depth = 1; depth <= 2; depth++) {
                cursor.set(cx, baseY - depth, cz);
                if (canSpireReplace(level.getBlockState(cursor))) {
                    level.setBlock(cursor, pickSpireState(random, glimmerState, blueIceState, packedIceState), 2);
                }
            }
            // If on a sloping snowbank, continue anchoring down up to 2 extra blocks until hitting solid ground
            for (int extraDepth = 3; extraDepth <= 5; extraDepth++) {
                cursor.set(cx, baseY - extraDepth, cz);
                BlockState existing = level.getBlockState(cursor);
                if (isAirOrSoftCover(existing)) {
                    level.setBlock(cursor, packedIceState, 2);
                } else {
                    break;
                }
            }
        }

        // 2. Build the tapering 6..13 block hexagonal -> diamond -> faceted rib -> 1x1 prism needle
        int hexTierEnd = Math.max(2, (int) Math.round(height * 0.30D));
        int diamondTierEnd = Math.max(hexTierEnd + 2, (int) Math.round(height * 0.62D));
        int ribTierEnd = Math.min(height - 2, diamondTierEnd + (height >= 10 ? 2 : 1));

        for (int dy = 0; dy < height; dy++) {
            int wy = baseY + dy;
            if (dy < hexTierEnd) {
                for (int[] off : baseHexOffsets) {
                    cursor.set(baseX + off[0], wy, baseZ + off[1]);
                    if (canSpireReplace(level.getBlockState(cursor))) {
                        level.setBlock(cursor, pickSpireState(random, glimmerState, blueIceState, packedIceState), 2);
                    }
                }
            } else if (dy < diamondTierEnd) {
                for (int[] off : DIAMOND_OFFSETS) {
                    cursor.set(baseX + off[0], wy, baseZ + off[1]);
                    if (canSpireReplace(level.getBlockState(cursor))) {
                        level.setBlock(cursor, pickSpireState(random, glimmerState, blueIceState, packedIceState), 2);
                    }
                }
            } else if (dy < ribTierEnd) {
                cursor.set(baseX, wy, baseZ);
                if (canSpireReplace(level.getBlockState(cursor))) {
                    level.setBlock(cursor, pickSpireState(random, glimmerState, blueIceState, packedIceState), 2);
                }
                int rx = northSouthRibs ? 0 : 1;
                int rz = northSouthRibs ? 1 : 0;
                for (int sign : new int[]{-1, 1}) {
                    cursor.set(baseX + rx * sign, wy, baseZ + rz * sign);
                    if (canSpireReplace(level.getBlockState(cursor))) {
                        level.setBlock(cursor, pickSpireState(random, glimmerState, blueIceState, packedIceState), 2);
                    }
                }
            } else {
                cursor.set(baseX, wy, baseZ);
                if (canSpireReplace(level.getBlockState(cursor))) {
                    // Crown the spire apex with glowing Glimmer-Ice so the tip gleams at night
                    BlockState tipState = (dy == height - 1)
                            ? glimmerState
                            : pickSpireState(random, glimmerState, blueIceState, packedIceState);
                    level.setBlock(cursor, tipState, 2);
                }
            }
        }

        // 3. Embed 1..3 Aegis Quartz Blocks around the base collar sprouting outward/upward Aegis Quartz Clusters
        int quartzTargetCount = 1 + random.nextInt(3); // 1..3
        BlockState quartzBlockState = aegisQuartzBlock.defaultBlockState();
        List<BlockPos> placedQuartzNodes = new ArrayList<>(quartzTargetCount);

        int startIdx = random.nextInt(COLLAR_RING_OFFSETS.length);
        for (int i = 0; i < COLLAR_RING_OFFSETS.length && placedQuartzNodes.size() < quartzTargetCount; i++) {
            int[] collar = COLLAR_RING_OFFSETS[(startIdx + i * 3) % COLLAR_RING_OFFSETS.length];
            int qx = baseX + collar[0];
            int qz = baseZ + collar[1];
            BlockPos collarGround = findLocalGround(level, qx, baseY, qz);
            if (collarGround == null) {
                continue;
            }
            // Embed half-sunk or flush at collar ground +1 (anchored into collarGround so it never floats)
            BlockPos embedPos = (random.nextInt(3) == 0) ? collarGround : collarGround.above();
            if (embedPos.equals(collarGround.above())) {
                // Ensure solid block directly beneath embedPos
                if (!isSpireTerrainBlock(level.getBlockState(collarGround), glimmerIceBlock)) {
                    continue;
                }
            }
            if (canSpireReplace(level.getBlockState(embedPos))) {
                level.setBlock(embedPos, quartzBlockState, 2);
                placedQuartzNodes.add(embedPos.immutable());
            }
        }

        // Sprout directional Aegis Quartz Clusters (facing outward/upward, waterlogged=false)
        for (BlockPos qPos : placedQuartzNodes) {
            // Always try to sprout an upward cluster if clear, plus 1-2 outward horizontal clusters
            tryPlaceQuartzCluster(level, qPos, Direction.UP, aegisQuartzCluster);
            for (Direction horiz : Direction.Plane.HORIZONTAL.shuffledCopy(random)) {
                int stepDot = horiz.getStepX() * (qPos.getX() - baseX) + horiz.getStepZ() * (qPos.getZ() - baseZ);
                if (stepDot < 0) {
                    continue; // face outward from spire core, not inward
                }
                if (random.nextFloat() < 0.65F) {
                    tryPlaceQuartzCluster(level, qPos, horiz, aegisQuartzCluster);
                }
            }
        }

        // 4. Place 1..2 Rimebloom blossoms on adjacent snow/ice around the spire base
        int rimebloomTarget = 1 + random.nextInt(2); // 1..2
        int rimebloomPlaced = 0;
        BlockState rimebloomState = rimebloomBlock.defaultBlockState();

        for (int attempt = 0; attempt < 16 && rimebloomPlaced < rimebloomTarget; attempt++) {
            int dx = random.nextInt(7) - 3;
            int dz = random.nextInt(7) - 3;
            int distSq = dx * dx + dz * dz;
            if (distSq < 4 || distSq > 13) {
                continue;
            }
            BlockPos ground = findLocalGround(level, baseX + dx, baseY, baseZ + dz);
            if (ground == null) {
                continue;
            }
            BlockState floorState = level.getBlockState(ground);
            if (!isFrozenSnowOrIce(floorState, glimmerIceBlock)) {
                continue;
            }
            BlockPos flowerPos = ground.above();
            BlockState current = level.getBlockState(flowerPos);
            if ((current.isAir() || current.is(Blocks.SNOW)) && rimebloomState.canSurvive(level, flowerPos)) {
                level.setBlock(flowerPos, rimebloomState, 2);
                rimebloomPlaced++;
            }
        }

        return true;
    }

    private static BlockState pickSpireState(
            RandomSource random,
            BlockState glimmerIce,
            BlockState blueIce,
            BlockState packedIce
    ) {
        float roll = random.nextFloat();
        if (roll < 0.55F) {
            return glimmerIce;
        }
        if (roll < 0.85F) {
            return blueIce;
        }
        return packedIce;
    }

    private static void tryPlaceQuartzCluster(
            WorldGenLevel level,
            BlockPos hostPos,
            Direction face,
            Block clusterBlock
    ) {
        BlockPos budPos = hostPos.relative(face);
        BlockState target = level.getBlockState(budPos);
        if (!target.isAir() && !target.is(Blocks.SNOW)) {
            return;
        }
        BlockState clusterState = clusterBlock.defaultBlockState();
        if (clusterState.hasProperty(BlockStateProperties.FACING)) {
            clusterState = clusterState.setValue(BlockStateProperties.FACING, face);
        }
        if (clusterState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            clusterState = clusterState.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        if (clusterState.canSurvive(level, budPos)) {
            level.setBlock(budPos, clusterState, 2);
        }
    }

    private static BlockPos findGroundSurface(WorldGenLevel level, BlockPos origin) {
        Block glimmerBlock = BuiltInRegistries.BLOCK.getOptional(GLIMMER_ICE_ID).orElse(Blocks.BLUE_ICE);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = 3; dy >= -5; dy--) {
            int y = origin.getY() + dy;
            if (y <= level.getMinY() + 4 || y >= level.getMaxY() - 16) {
                continue;
            }
            cursor.set(origin.getX(), y, origin.getZ());
            BlockState above = level.getBlockState(cursor.above());
            if (above.is(Blocks.WATER) || above.is(Blocks.LAVA)) {
                return null;
            }
            BlockState ground = level.getBlockState(cursor);
            if (isSpireTerrainBlock(ground, glimmerBlock) && isAirOrSoftCover(above)) {
                return cursor.immutable();
            }
        }
        return null;
    }

    private static BlockPos findLocalGround(WorldGenLevel level, int wx, int refY, int wz) {
        Block glimmerBlock = BuiltInRegistries.BLOCK.getOptional(GLIMMER_ICE_ID).orElse(Blocks.BLUE_ICE);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = 2; dy >= -4; dy--) {
            int y = refY + dy;
            cursor.set(wx, y, wz);
            BlockState state = level.getBlockState(cursor);
            BlockState above = level.getBlockState(cursor.above());
            if (isSpireTerrainBlock(state, glimmerBlock) && isAirOrSoftCover(above)) {
                return cursor.immutable();
            }
        }
        return null;
    }

    private static boolean hasNearbyFooting(WorldGenLevel level, BlockPos checkCenter) {
        Block glimmerBlock = BuiltInRegistries.BLOCK.getOptional(GLIMMER_ICE_ID).orElse(Blocks.BLUE_ICE);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = 2; dy >= -3; dy--) {
            cursor.set(checkCenter.getX(), checkCenter.getY() + dy, checkCenter.getZ());
            BlockState state = level.getBlockState(cursor);
            if (state.is(Blocks.WATER) || state.is(Blocks.LAVA)) {
                return false;
            }
            if (isSpireTerrainBlock(state, glimmerBlock)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSpireTerrainBlock(BlockState state, Block glimmerBlock) {
        return state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.ICE)
                || state.is(glimmerBlock)
                || state.is(BlockTags.DIRT)
                || state.is(BlockTags.BASE_STONE_OVERWORLD);
    }

    private static boolean isFrozenSnowOrIce(BlockState state, Block glimmerBlock) {
        return state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.ICE)
                || state.is(glimmerBlock);
    }

    private static boolean isAirOrSoftCover(BlockState state) {
        return state.isAir()
                || state.is(Blocks.SNOW)
                || state.is(Blocks.POWDER_SNOW)
                || state.canBeReplaced();
    }

    private static boolean canSpireReplace(BlockState state) {
        return isAirOrSoftCover(state)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.ICE)
                || state.is(BlockTags.DIRT)
                || state.is(BlockTags.BASE_STONE_OVERWORLD);
    }

    private static Block resolveBlock(ResourceLocation id, Supplier<Block> modSupplier, Block fallback) {
        var opt = BuiltInRegistries.BLOCK.getOptional(id);
        if (opt.isPresent()) {
            return opt.get();
        }
        try {
            Block b = modSupplier.get();
            if (b != null) {
                return b;
            }
        } catch (Throwable ignored) {
        }
        return fallback;
    }
}
