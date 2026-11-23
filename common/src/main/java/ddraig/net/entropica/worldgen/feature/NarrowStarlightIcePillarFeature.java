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
import net.minecraft.world.level.material.Fluids;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Generates Biome #17 ({@code 2.17 Astral Skerries}) signature taller-than-expected narrow
 * stretched ice pillars ({@code 12-28} blocks tall):
 * <ul>
 *   <li>Rises from barren skerry shelves ({@code starlight_ice}, {@code astral_impactite},
 *       {@code blue_ice}, {@code packed_ice}, {@code stone}, {@code coarse_gravel}) or shallow
 *       cosmic channels ({@code <= 5} blocks deep, anchoring solidly down into the seafloor).</li>
 *   <li>Compact {@code 2-4} block buttressed {@code 3x3} cross/plus base of {@code packed_ice},
 *       {@code blue_ice}, and {@code starlight_ice} that tapers immediately into a slender
 *       {@code 1x1} vertical {@code starlight_ice} needle ({@code 12-28} blocks tall).</li>
 *   <li>Features an occasional {@code 1}-block crystalline step/jog halfway up the shaft and a
 *       {@code 25%} chance of a directional {@code stardust_crystal_cluster} clinging near the upper shaft.</li>
 * </ul>
 */
public class NarrowStarlightIcePillarFeature extends Feature<NoneFeatureConfiguration> {

    private static final ResourceLocation STARLIGHT_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "starlight_ice");
    private static final ResourceLocation STARDUST_CRYSTAL_CLUSTER_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "stardust_crystal_cluster");
    private static final ResourceLocation ASTRAL_IMPACTITE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "astral_impactite");
    private static final ResourceLocation BUDDING_ASTRAL_IMPACTITE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "budding_astral_impactite");
    private static final ResourceLocation COARSE_GRAVEL_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "coarse_gravel");
    private static final ResourceLocation ASTRAL_MARBLE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "astral_marble");
    private static final ResourceLocation ASTRAL_MARBLE_BRICKS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "astral_marble_bricks");
    private static final ResourceLocation ASTRAL_GLASS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "astral_glass");

    public NarrowStarlightIcePillarFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        Block starlightIceBlock = resolveBlock(
                () -> ModBlocks.STARLIGHT_ICE.isPresent() ? ModBlocks.STARLIGHT_ICE.get() : null,
                STARLIGHT_ICE_ID,
                Blocks.BLUE_ICE
        );
        Block impactiteBlock = resolveBlock(
                () -> ModBlocks.ASTRAL_IMPACTITE.isPresent() ? ModBlocks.ASTRAL_IMPACTITE.get() : null,
                ASTRAL_IMPACTITE_ID,
                Blocks.DEEPSLATE
        );
        Block buddingImpactiteBlock = resolveBlock(
                () -> ModBlocks.BUDDING_ASTRAL_IMPACTITE.isPresent() ? ModBlocks.BUDDING_ASTRAL_IMPACTITE.get() : null,
                BUDDING_ASTRAL_IMPACTITE_ID,
                impactiteBlock
        );
        Block coarseGravelBlock = resolveBlock(
                () -> ModBlocks.COARSE_GRAVEL.isPresent() ? ModBlocks.COARSE_GRAVEL.get() : null,
                COARSE_GRAVEL_ID,
                Blocks.GRAVEL
        );
        Optional<Block> stardustClusterOpt = resolveOptionalBlock(
                () -> ModBlocks.STARDUST_CRYSTAL_CLUSTER.isPresent() ? ModBlocks.STARDUST_CRYSTAL_CLUSTER.get() : null,
                STARDUST_CRYSTAL_CLUSTER_ID
        );

        AnchorSite site = findSkerryOrShallowAnchor(
                level, origin, random, starlightIceBlock, impactiteBlock, buddingImpactiteBlock, coarseGravelBlock);
        if (site == null) {
            return false;
        }

        int cx = site.x;
        int cz = site.z;
        int surfaceRefY = site.surfaceRefY; // top of solid ground or waterline when in shallow channel
        int seafloorY = site.seafloorY;

        if (isNearOrreryStructure(level, cx, surfaceRefY, cz)) {
            return false;
        }

        int pillarHeight = 12 + random.nextInt(17); // 12..28 blocks tall above surface/waterline!
        if (surfaceRefY + pillarHeight + 3 >= level.getMaxY()) {
            return false;
        }

        // Ensure clear vertical headroom above the anchor so needles don't collide with overhangs or structures
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int checkDy = 2; checkDy <= pillarHeight; checkDy += 3) {
            cursor.set(cx, surfaceRefY + checkDy, cz);
            if (!isAirOrSoftCover(level.getBlockState(cursor))) {
                return false;
            }
        }

        BlockState starlightIce = starlightIceBlock.defaultBlockState();
        BlockState blueIce = Blocks.BLUE_ICE.defaultBlockState();
        BlockState packedIce = Blocks.PACKED_ICE.defaultBlockState();

        // 1. Anchor central trunk 2 blocks into solid seafloor/skerry rock and through any shallow water
        for (int wy = seafloorY - 1; wy <= surfaceRefY; wy++) {
            cursor.set(cx, wy, cz);
            if (canBaseReplace(level.getBlockState(cursor), starlightIceBlock, impactiteBlock, coarseGravelBlock)) {
                BlockState rootState = (wy < seafloorY) ? packedIce : pickButtressState(random, starlightIce, blueIce, packedIce);
                level.setBlock(cursor, rootState, 2);
            }
        }

        // 2. Compact 2-4 block buttressed 3x3 cross/plus base tapering immediately into the 1x1 needle
        int maxButtressHeight = 2 + random.nextInt(3); // 2..4 blocks tall
        Direction primaryButtressDir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        for (Direction card : Direction.Plane.HORIZONTAL) {
            int bx = cx + card.getStepX();
            int bz = cz + card.getStepZ();

            // Verify cardinal arm has solid seafloor/skerry footing within 7 blocks below surfaceRefY
            int armFloorY = findLocalFloor(
                    level, bx, surfaceRefY, bz, 7,
                    starlightIceBlock, impactiteBlock, buddingImpactiteBlock, coarseGravelBlock
            );
            if (armFloorY == Integer.MIN_VALUE) {
                continue;
            }

            int armHeight = (card == primaryButtressDir)
                    ? maxButtressHeight
                    : Math.max(1, maxButtressHeight - random.nextInt(2));

            // Seal cardinal buttress down to armFloorY so it never floats over water or a shelf step
            for (int wy = armFloorY; wy <= surfaceRefY + armHeight; wy++) {
                cursor.set(bx, wy, bz);
                if (canBaseReplace(level.getBlockState(cursor), starlightIceBlock, impactiteBlock, coarseGravelBlock)) {
                    BlockState armState = (wy == surfaceRefY + armHeight)
                            ? starlightIce
                            : pickButtressState(random, starlightIce, blueIce, packedIce);
                    level.setBlock(cursor, armState, 2);
                }
            }
        }

        // 3. Slender 1x1 stretched Starlight Ice column (12-28 blocks high) with occasional 1-block step/jog halfway up
        boolean hasCrystallineJog = pillarHeight >= 15 && random.nextFloat() < 0.45F;
        int jogDy = hasCrystallineJog
                ? (int) Math.round(pillarHeight * (0.42D + random.nextDouble() * 0.20D))
                : -1;
        Direction jogDir = Direction.Plane.HORIZONTAL.getRandomDirection(random);

        if (hasCrystallineJog) {
            // Confirm upper jogged shaft also has clear air
            int jx = cx + jogDir.getStepX();
            int jz = cz + jogDir.getStepZ();
            for (int checkDy = jogDy; checkDy <= pillarHeight; checkDy += 3) {
                cursor.set(jx, surfaceRefY + checkDy, jz);
                if (!isAirOrSoftCover(level.getBlockState(cursor))) {
                    hasCrystallineJog = false;
                    jogDy = -1;
                    break;
                }
            }
        }

        int shaftX = cx;
        int shaftZ = cz;

        for (int dy = 1; dy <= pillarHeight; dy++) {
            int wy = surfaceRefY + dy;

            if (hasCrystallineJog && dy == jogDy) {
                // Place lower shaft block + shifted step block at wy so the 1-block jog has crisp structural continuity
                cursor.set(shaftX, wy, shaftZ);
                if (isAirOrSoftCover(level.getBlockState(cursor)) || level.getBlockState(cursor).is(starlightIceBlock)) {
                    level.setBlock(cursor, starlightIce, 2);
                }
                shaftX += jogDir.getStepX();
                shaftZ += jogDir.getStepZ();
                // Add small crystal heel under the shifted column at wy - 1
                cursor.set(shaftX, wy - 1, shaftZ);
                if (isAirOrSoftCover(level.getBlockState(cursor))) {
                    level.setBlock(cursor, blueIce, 2);
                }
            }

            cursor.set(shaftX, wy, shaftZ);
            BlockState existing = level.getBlockState(cursor);
            if (isAirOrSoftCover(existing)
                    || existing.is(Blocks.BLUE_ICE)
                    || existing.is(Blocks.PACKED_ICE)
                    || existing.is(starlightIceBlock)) {
                // Subtle blue_ice fleck only in the lowest quarter; pure translucent starlight_ice above
                BlockState colState = (dy <= pillarHeight / 4 && random.nextFloat() < 0.14F)
                        ? blueIce
                        : starlightIce;
                level.setBlock(cursor, colState, 2);
            }
        }

        // 4. 25% chance of a directional stardust_crystal_cluster clinging near the upper shaft
        if (stardustClusterOpt.isPresent() && random.nextFloat() < 0.25F) {
            Block clusterBlock = stardustClusterOpt.get();
            int minUpperDy = Math.max(jogDy + 1, (int) Math.round(pillarHeight * 0.65D));
            int maxUpperDy = Math.max(minUpperDy, pillarHeight - 1);
            int clusterDy = minUpperDy + random.nextInt(maxUpperDy - minUpperDy + 1);
            int hostY = surfaceRefY + clusterDy;

            for (Direction face : Direction.Plane.HORIZONTAL.shuffledCopy(random)) {
                BlockPos clusterPos = new BlockPos(shaftX + face.getStepX(), hostY, shaftZ + face.getStepZ());
                if (!level.getBlockState(clusterPos).isAir()) {
                    continue;
                }
                BlockState clusterState = orientCluster(clusterBlock.defaultBlockState(), face, false);
                if (clusterState.canSurvive(level, clusterPos)) {
                    level.setBlock(clusterPos, clusterState, 2);
                    break;
                }
            }
        }

        return true;
    }

    private record AnchorSite(int x, int z, int seafloorY, int surfaceRefY) {}

    private static AnchorSite findSkerryOrShallowAnchor(
            WorldGenLevel level,
            BlockPos origin,
            RandomSource random,
            Block starlightIceBlock,
            Block impactiteBlock,
            Block buddingImpactiteBlock,
            Block coarseGravelBlock
    ) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int attempt = 0; attempt < 5; attempt++) {
            int ox = (attempt == 0) ? 0 : (random.nextInt(5) - 2);
            int oz = (attempt == 0) ? 0 : (random.nextInt(5) - 2);
            int tx = origin.getX() + ox;
            int tz = origin.getZ() + oz;

            int topScanY = Math.min(level.getMaxY() - 30, origin.getY() + 3);
            int bottomScanY = Math.max(level.getMinY() + 6, origin.getY() - 8);

            for (int y = topScanY; y >= bottomScanY; y--) {
                cursor.set(tx, y, tz);
                BlockState state = level.getBlockState(cursor);
                if (isAirOrSoftCover(state)) {
                    continue;
                }

                // Case A: Exposed dry skerry shelf
                if (isValidSkerryFooting(state, starlightIceBlock, impactiteBlock, buddingImpactiteBlock, coarseGravelBlock)) {
                    return new AnchorSite(tx, tz, y, y);
                }

                // Case B: Shallow cosmic channel (<= 5 blocks of water over solid seafloor)
                if (isWater(state)) {
                    int waterTopY = y;
                    for (int depth = 1; depth <= 5; depth++) {
                        int checkY = waterTopY - depth;
                        if (checkY <= level.getMinY() + 4) {
                            break;
                        }
                        cursor.set(tx, checkY, tz);
                        BlockState belowWater = level.getBlockState(cursor);
                        if (isWater(belowWater)) {
                            continue;
                        }
                        if (isValidSkerryFooting(belowWater, starlightIceBlock, impactiteBlock, buddingImpactiteBlock, coarseGravelBlock)) {
                            return new AnchorSite(tx, tz, checkY, waterTopY);
                        }
                        break;
                    }
                }
                break;
            }
        }
        return null;
    }

    private static int findLocalFloor(
            WorldGenLevel level,
            int x,
            int refY,
            int z,
            int maxDrop,
            Block starlightIceBlock,
            Block impactiteBlock,
            Block buddingImpactiteBlock,
            Block coarseGravelBlock
    ) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = 2; dy >= -maxDrop; dy--) {
            int wy = refY + dy;
            if (wy <= level.getMinY() + 4) {
                break;
            }
            cursor.set(x, wy, z);
            BlockState state = level.getBlockState(cursor);
            if (isAirOrSoftCover(state) || isWater(state)) {
                continue;
            }
            if (isValidSkerryFooting(state, starlightIceBlock, impactiteBlock, buddingImpactiteBlock, coarseGravelBlock)) {
                return wy;
            }
            return Integer.MIN_VALUE;
        }
        return Integer.MIN_VALUE;
    }

    private static boolean isNearOrreryStructure(WorldGenLevel level, int cx, int refY, int cz) {
        Optional<Block> marbleOpt = BuiltInRegistries.BLOCK.getOptional(ASTRAL_MARBLE_ID);
        Optional<Block> marbleBricksOpt = BuiltInRegistries.BLOCK.getOptional(ASTRAL_MARBLE_BRICKS_ID);
        Optional<Block> astralGlassOpt = BuiltInRegistries.BLOCK.getOptional(ASTRAL_GLASS_ID);

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            for (int dist : new int[]{3, 7}) {
                for (int dy : new int[]{1, 5}) {
                    cursor.set(cx + dir.getStepX() * dist, refY + dy, cz + dir.getStepZ() * dist);
                    BlockState st = level.getBlockState(cursor);
                    if ((marbleOpt.isPresent() && st.is(marbleOpt.get()))
                            || (marbleBricksOpt.isPresent() && st.is(marbleBricksOpt.get()))
                            || (astralGlassOpt.isPresent() && st.is(astralGlassOpt.get()))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static BlockState pickButtressState(
            RandomSource random,
            BlockState starlightIce,
            BlockState blueIce,
            BlockState packedIce
    ) {
        float roll = random.nextFloat();
        if (roll < 0.38F) {
            return starlightIce;
        }
        if (roll < 0.72F) {
            return blueIce;
        }
        return packedIce;
    }

    private static BlockState orientCluster(BlockState base, Direction face, boolean waterlogged) {
        if (base.hasProperty(BlockStateProperties.FACING)) {
            base = base.setValue(BlockStateProperties.FACING, face);
        }
        if (base.hasProperty(BlockStateProperties.WATERLOGGED)) {
            base = base.setValue(BlockStateProperties.WATERLOGGED, waterlogged);
        }
        return base;
    }

    private static boolean isValidSkerryFooting(
            BlockState state,
            Block starlightIceBlock,
            Block impactiteBlock,
            Block buddingImpactiteBlock,
            Block coarseGravelBlock
    ) {
        return state.is(starlightIceBlock)
                || state.is(impactiteBlock)
                || state.is(buddingImpactiteBlock)
                || state.is(coarseGravelBlock)
                || state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.ICE)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.GRAVEL)
                || state.is(BlockTags.BASE_STONE_OVERWORLD);
    }

    private static boolean canBaseReplace(
            BlockState state,
            Block starlightIceBlock,
            Block impactiteBlock,
            Block coarseGravelBlock
    ) {
        return isAirOrSoftCover(state)
                || isWater(state)
                || state.is(starlightIceBlock)
                || state.is(impactiteBlock)
                || state.is(coarseGravelBlock)
                || state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.ICE)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.GRAVEL)
                || state.is(BlockTags.BASE_STONE_OVERWORLD);
    }

    private static boolean isWater(BlockState state) {
        return state.is(Blocks.WATER) || state.getFluidState().getType() == Fluids.WATER;
    }

    private static boolean isAirOrSoftCover(BlockState state) {
        return state.isAir()
                || state.is(Blocks.SNOW)
                || state.is(Blocks.POWDER_SNOW)
                || (state.canBeReplaced() && state.getFluidState().isEmpty());
    }

    private static Block resolveBlock(Supplier<Block> supplier, ResourceLocation id, Block fallback) {
        try {
            Block b = supplier.get();
            if (b != null) {
                return b;
            }
        } catch (Throwable ignored) {
        }
        return BuiltInRegistries.BLOCK.getOptional(id).orElse(fallback);
    }

    private static Optional<Block> resolveOptionalBlock(Supplier<Block> supplier, ResourceLocation id) {
        try {
            Block b = supplier.get();
            if (b != null) {
                return Optional.of(b);
            }
        } catch (Throwable ignored) {
        }
        return BuiltInRegistries.BLOCK.getOptional(id);
    }
}
