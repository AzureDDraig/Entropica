package ddraig.net.entropica.worldgen.feature;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Generates jagged, organic coastal {@code entropica:sanguine_ice} floes and stepped shelves
 * ({@code radius 4..8}) at sea level ({@code Y = 62}) across shallow water and bay edges in
 * Biome #16 ({@code 2.16 Blood-Ice Shallows}):
 * <ul>
 *   <li>Stepped inner {@code Sanguine Ice} shelf ({@code Y = 61..63}) tapering into jagged sea-level
 *       {@code Sanguine Ice} floe margins ({@code Y = 62}) interlaced with open blood-wine water pockets.</li>
 *   <li>Dresses underwater sand, gravel, clay, mud, and stone beneath ({@code 1..4} blocks deep)
 *       into iron-rich {@code entropica:red_clay_sand} (supported underneath so sand never collapses).</li>
 *   <li>Scatters occasional {@code entropica:sanguine_lily} pads on open water pockets and
 *       {@code entropica:red_algae} tufts along floe margins.</li>
 * </ul>
 */
public class SanguineIceShelfFeature extends Feature<NoneFeatureConfiguration> {

    private static final ResourceLocation SANGUINE_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "sanguine_ice");
    private static final ResourceLocation RED_CLAY_SAND_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "red_clay_sand");
    private static final ResourceLocation SANGUINE_CLAY_BRICKS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "sanguine_clay_bricks");
    private static final ResourceLocation SANGUINE_LILY_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "sanguine_lily");
    private static final ResourceLocation RED_ALGAE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "red_algae");

    public SanguineIceShelfFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        int seaLevel = level.getSeaLevel() > 0 ? level.getSeaLevel() : 63;
        int waterTopY = seaLevel - 1; // Y = 62 in standard Overworld

        Block sanguineIceBlock = resolveModOrRegistryBlock(
                () -> ModBlocks.SANGUINE_ICE.isPresent() ? ModBlocks.SANGUINE_ICE.get() : null,
                SANGUINE_ICE_ID,
                Blocks.PACKED_ICE
        );
        Block redClaySandBlock = resolveModOrRegistryBlock(
                () -> ModBlocks.RED_CLAY_SAND.isPresent() ? ModBlocks.RED_CLAY_SAND.get() : null,
                RED_CLAY_SAND_ID,
                Blocks.RED_SAND
        );
        Block sanguineClayBricksBlock = resolveModOrRegistryBlock(
                () -> ModBlocks.SANGUINE_CLAY_BRICKS.isPresent() ? ModBlocks.SANGUINE_CLAY_BRICKS.get() : null,
                SANGUINE_CLAY_BRICKS_ID,
                Blocks.MUD_BRICKS
        );
        Optional<Block> sanguineLilyOpt = resolveOptionalBlock(
                () -> ModBlocks.SANGUINE_LILY.isPresent() ? ModBlocks.SANGUINE_LILY.get() : null,
                SANGUINE_LILY_ID
        );
        Optional<Block> redAlgaeOpt = resolveOptionalBlock(
                () -> ModBlocks.RED_ALGAE.isPresent() ? ModBlocks.RED_ALGAE.get() : null,
                RED_ALGAE_ID
        );

        BlockPos anchorPos = findShallowBayOrMarginAnchor(
                level, origin, waterTopY, random, sanguineIceBlock, redClaySandBlock);
        if (anchorPos == null) {
            return false;
        }

        int cx = anchorPos.getX();
        int cz = anchorPos.getZ();

        // Avoid encroaching on a generated Crimson Tide Sanctum
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int checkY = waterTopY + 2; checkY <= waterTopY + 10; checkY += 4) {
            cursor.set(cx, checkY, cz);
            if (!level.getBlockState(cursor).isAir()) {
                return false;
            }
        }
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            for (int dist : new int[]{4, 8}) {
                cursor.set(cx + dir.getStepX() * dist, waterTopY + 2, cz + dir.getStepZ() * dist);
                BlockState near = level.getBlockState(cursor);
                if (near.is(sanguineClayBricksBlock)
                        || near.is(Blocks.DEEPSLATE_TILES)
                        || near.is(Blocks.POLISHED_BASALT)) {
                    return false;
                }
            }
        }

        int baseRadius = 4 + random.nextInt(5); // 4..8
        double stretchX = 0.82D + random.nextDouble() * 0.36D;
        double stretchZ = 0.82D + random.nextDouble() * 0.36D;
        double phase1 = random.nextDouble() * (Math.PI * 2.0D);
        double phase2 = random.nextDouble() * (Math.PI * 2.0D);
        double phase3 = random.nextDouble() * (Math.PI * 2.0D);

        BlockState sanguineIce = sanguineIceBlock.defaultBlockState();
        BlockState redClaySand = redClaySandBlock.defaultBlockState();
        BlockState sanguineClayBricks = sanguineClayBricksBlock.defaultBlockState();
        BlockState blueIce = Blocks.BLUE_ICE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        int maxBound = baseRadius + 2;
        int placedIceColumns = 0;
        List<BlockPos> openWaterPockets = new ArrayList<>();
        List<BlockPos> dryFloeMargins = new ArrayList<>();
        List<BlockPos> submergedMargins = new ArrayList<>();

        for (int dx = -maxBound; dx <= maxBound; dx++) {
            for (int dz = -maxBound; dz <= maxBound; dz++) {
                double angle = Math.atan2(dz, dx);
                // Multi-harmonic jagged polar fracture modulation
                double jaggedFactor = 0.88D
                        + 0.17D * Math.cos(angle * 3.0D + phase1)
                        + 0.11D * Math.sin(angle * 5.0D - phase2)
                        + 0.07D * Math.cos(angle * 7.0D + phase3);
                double localRadius = baseRadius * jaggedFactor;
                double anisoDist = Math.hypot(dx * stretchX, dz * stretchZ);

                if (anisoDist > localRadius + 1.5D) {
                    continue;
                }

                int bx = cx + dx;
                int bz = cz + dz;

                // 1. Dress underwater sand/gravel/sediment beneath (1..4 blocks deep) into Red Clay Sand
                dressSeabedIntoRedClaySand(
                        level, cursor, bx, bz, waterTopY, random,
                        redClaySandBlock, redClaySand, sanguineClayBricks, sanguineIceBlock
                );

                cursor.set(bx, waterTopY, bz);
                BlockState atWaterline = level.getBlockState(cursor);
                BlockPos aboveWaterPos = new BlockPos(bx, waterTopY + 1, bz);
                BlockState aboveWater = level.getBlockState(aboveWaterPos);

                if (anisoDist > localRadius) {
                    if (isOpenSourceWater(atWaterline) && aboveWater.isAir()) {
                        openWaterPockets.add(new BlockPos(bx, waterTopY, bz));
                    }
                    continue;
                }

                // Leave occasional jagged open-water leads along the outer floe margin for Sanguine Lilies
                boolean outerFracturePocket = (anisoDist > localRadius * 0.78D)
                        && (Math.sin(angle * 6.0D + phase2) > 0.38D || random.nextFloat() < 0.18F);
                if (outerFracturePocket) {
                    if (isOpenSourceWater(atWaterline) && aboveWater.isAir()) {
                        openWaterPockets.add(new BlockPos(bx, waterTopY, bz));
                    }
                    cursor.set(bx, waterTopY - 1, bz);
                    if (isOpenSourceWater(level.getBlockState(cursor))) {
                        BlockState underSubmerged = level.getBlockState(new BlockPos(bx, waterTopY - 2, bz));
                        if (underSubmerged.is(redClaySandBlock) || underSubmerged.is(sanguineIceBlock)) {
                            submergedMargins.add(new BlockPos(bx, waterTopY - 1, bz));
                        }
                    }
                    continue;
                }

                if (!canFloeReplaceAtWaterline(atWaterline, sanguineIceBlock, redClaySandBlock)
                        || !canShelfMoundReplace(aboveWater)) {
                    continue;
                }

                // 2. Sea-level Sanguine Ice floe sheet (Y = 62)
                level.setBlock(new BlockPos(bx, waterTopY, bz), sanguineIce, 2);
                placedIceColumns++;

                // Submerged keel thickness (Y = 61) under inner & mid shelf
                if (anisoDist <= localRadius * 0.74D) {
                    cursor.set(bx, waterTopY - 1, bz);
                    if (canSubmergedKeelReplace(level.getBlockState(cursor), redClaySandBlock)) {
                        BlockState keelState = (anisoDist <= localRadius * 0.35D && random.nextFloat() < 0.25F)
                                ? blueIce
                                : sanguineIce;
                        level.setBlock(cursor, keelState, 2);
                    }
                }

                // 3. Stepped coastal Sanguine Ice shelf terrace (Y = 63, 1 block above sea level)
                double steppedShelfRadius = Math.max(1.6D, localRadius * 0.50D);
                if (anisoDist <= steppedShelfRadius) {
                    level.setBlock(aboveWaterPos, sanguineIce, 2);
                    cursor.set(bx, waterTopY + 2, bz);
                    if (level.getBlockState(cursor).is(Blocks.SNOW)) {
                        level.setBlock(cursor, air, 2);
                    }
                    if (anisoDist >= steppedShelfRadius - 1.25D && level.getBlockState(cursor).isAir()) {
                        dryFloeMargins.add(aboveWaterPos);
                    }
                } else {
                    if (aboveWater.is(Blocks.SNOW)) {
                        level.setBlock(aboveWaterPos, air, 2);
                    }
                    if (level.getBlockState(aboveWaterPos).isAir()) {
                        dryFloeMargins.add(new BlockPos(bx, waterTopY, bz));
                    }
                }
            }
        }

        if (placedIceColumns < 8) {
            return false;
        }

        // 4. Scatter occasional ModBlocks.SANGUINE_LILY on open water pockets (1..3 pads)
        if (sanguineLilyOpt.isPresent() && !openWaterPockets.isEmpty()) {
            BlockState lilyState = nonWaterlogged(sanguineLilyOpt.get().defaultBlockState());
            int targetLilies = 1 + random.nextInt(3); // 1..3
            int placedLilies = 0;
            while (!openWaterPockets.isEmpty() && placedLilies < targetLilies) {
                BlockPos waterPos = openWaterPockets.remove(random.nextInt(openWaterPockets.size()));
                BlockPos lilyPos = waterPos.above();
                if (!isOpenSourceWater(level.getBlockState(waterPos)) || !level.getBlockState(lilyPos).isAir()) {
                    continue;
                }
                if (lilyState.canSurvive(level, lilyPos)) {
                    level.setBlock(lilyPos, lilyState, 2);
                    placedLilies++;
                }
            }
        }

        // 5. Scatter occasional ModBlocks.RED_ALGAE along dry floe margins & submerged edges
        if (redAlgaeOpt.isPresent()) {
            Block algaeBlock = redAlgaeOpt.get();
            int targetDryAlgae = 2 + random.nextInt(4); // 2..5
            int placedDry = 0;
            while (!dryFloeMargins.isEmpty() && placedDry < targetDryAlgae) {
                BlockPos supportPos = dryFloeMargins.remove(random.nextInt(dryFloeMargins.size()));
                BlockPos algaePos = supportPos.above();
                BlockState support = level.getBlockState(supportPos);
                if ((!support.is(sanguineIceBlock) && !support.is(redClaySandBlock))
                        || !level.getBlockState(algaePos).isAir()) {
                    continue;
                }
                BlockState dryAlgae = RedAlgaeMarginFeature.prepareRedAlgaeState(algaeBlock, false);
                if (dryAlgae.canSurvive(level, algaePos)) {
                    level.setBlock(algaePos, dryAlgae, 2);
                    placedDry++;
                }
            }

            int targetWetAlgae = 1 + random.nextInt(3); // 1..3
            int placedWet = 0;
            while (!submergedMargins.isEmpty() && placedWet < targetWetAlgae) {
                BlockPos waterAlgaePos = submergedMargins.remove(random.nextInt(submergedMargins.size()));
                if (!isOpenSourceWater(level.getBlockState(waterAlgaePos))) {
                    continue;
                }
                BlockState wetAlgae = RedAlgaeMarginFeature.prepareRedAlgaeState(algaeBlock, true);
                if (wetAlgae.canSurvive(level, waterAlgaePos)) {
                    level.setBlock(waterAlgaePos, wetAlgae, 2);
                    placedWet++;
                }
            }
        }

        return true;
    }

    private static void dressSeabedIntoRedClaySand(
            WorldGenLevel level,
            BlockPos.MutableBlockPos cursor,
            int bx,
            int bz,
            int waterTopY,
            RandomSource random,
            Block redClaySandBlock,
            BlockState redClaySand,
            BlockState sanguineClayBricks,
            Block sanguineIceBlock
    ) {
        int minSearchY = Math.max(level.getMinY() + 3, waterTopY - 12);
        int seabedTopY = Integer.MIN_VALUE;

        for (int wy = waterTopY - 1; wy >= minSearchY; wy--) {
            cursor.set(bx, wy, bz);
            BlockState st = level.getBlockState(cursor);
            if (isOpenWaterOrIce(st, sanguineIceBlock)) {
                continue;
            }
            if (isDressableSeabedBlock(st, redClaySandBlock)) {
                seabedTopY = wy;
            }
            break;
        }

        if (seabedTopY == Integer.MIN_VALUE) {
            return;
        }

        int dressDepth = 1 + random.nextInt(4); // 1..4 blocks deep
        int bottomDressY = Math.max(level.getMinY() + 2, seabedTopY - dressDepth + 1);

        // Ensure solid block immediately beneath bottomDressY so Red Clay Sand never falls into caves
        cursor.set(bx, bottomDressY - 1, bz);
        BlockState belowBottom = level.getBlockState(cursor);
        if (belowBottom.isAir()
                || belowBottom.is(Blocks.WATER)
                || belowBottom.getFluidState().getType() == Fluids.WATER
                || belowBottom.canBeReplaced()) {
            level.setBlock(cursor, sanguineClayBricks, 2);
        }

        for (int wy = bottomDressY; wy <= seabedTopY; wy++) {
            cursor.set(bx, wy, bz);
            if (isDressableSeabedBlock(level.getBlockState(cursor), redClaySandBlock)) {
                level.setBlock(cursor, redClaySand, 2);
            }
        }
    }

    private static BlockPos findShallowBayOrMarginAnchor(
            WorldGenLevel level,
            BlockPos origin,
            int waterTopY,
            RandomSource random,
            Block sanguineIceBlock,
            Block redClaySandBlock
    ) {
        BlockPos.MutableBlockPos check = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos above = new BlockPos.MutableBlockPos();

        for (int r = 0; r <= 4; r++) {
            int attempts = (r == 0) ? 1 : 6;
            for (int a = 0; a < attempts; a++) {
                int ox = (r == 0) ? 0 : (random.nextInt(r * 2 + 1) - r);
                int oz = (r == 0) ? 0 : (random.nextInt(r * 2 + 1) - r);
                int tx = origin.getX() + ox;
                int tz = origin.getZ() + oz;

                check.set(tx, waterTopY, tz);
                above.set(tx, waterTopY + 1, tz);
                BlockState atWater = level.getBlockState(check);
                BlockState aboveState = level.getBlockState(above);

                if (!canFloeReplaceAtWaterline(atWater, sanguineIceBlock, redClaySandBlock)
                        || !canShelfMoundReplace(aboveState)) {
                    continue;
                }

                // Require at least 2 cardinal neighbors within 2 blocks to be shallow water or thin ice
                int waterCount = 0;
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    check.set(tx + dir.getStepX() * 2, waterTopY, tz + dir.getStepZ() * 2);
                    BlockState neighbor = level.getBlockState(check);
                    if (isOpenSourceWater(neighbor) || neighbor.is(Blocks.ICE)) {
                        waterCount++;
                    }
                }
                if (waterCount >= 2) {
                    return new BlockPos(tx, waterTopY, tz);
                }
            }
        }
        return null;
    }

    private static boolean isOpenSourceWater(BlockState state) {
        return state.is(Blocks.WATER) && state.getFluidState().isSource();
    }

    private static boolean isOpenWaterOrIce(BlockState state, Block sanguineIceBlock) {
        return state.is(Blocks.WATER)
                || state.getFluidState().getType() == Fluids.WATER
                || state.is(Blocks.ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.BLUE_ICE)
                || state.is(sanguineIceBlock);
    }

    private static boolean isDressableSeabedBlock(BlockState state, Block redClaySandBlock) {
        return state.is(Blocks.SAND)
                || state.is(Blocks.RED_SAND)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.CLAY)
                || state.is(Blocks.MUD)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.STONE)
                || state.is(redClaySandBlock);
    }

    private static boolean canFloeReplaceAtWaterline(
            BlockState state,
            Block sanguineIceBlock,
            Block redClaySandBlock
    ) {
        return state.is(Blocks.WATER)
                || state.getFluidState().getType() == Fluids.WATER
                || state.is(Blocks.ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(sanguineIceBlock)
                || state.is(redClaySandBlock);
    }

    private static boolean canSubmergedKeelReplace(BlockState state, Block redClaySandBlock) {
        return state.is(Blocks.WATER)
                || state.getFluidState().getType() == Fluids.WATER
                || state.is(Blocks.ICE)
                || state.is(Blocks.SAND)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.CLAY)
                || state.is(Blocks.MUD)
                || state.is(redClaySandBlock);
    }

    private static boolean canShelfMoundReplace(BlockState state) {
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

    static Block resolveModOrRegistryBlock(
            Supplier<Block> supplier,
            ResourceLocation id,
            Block fallback
    ) {
        try {
            Block fromMod = supplier.get();
            if (fromMod != null) {
                return fromMod;
            }
        } catch (Throwable ignored) {
        }
        return BuiltInRegistries.BLOCK.getOptional(id).orElse(fallback);
    }

    static Optional<Block> resolveOptionalBlock(Supplier<Block> supplier, ResourceLocation id) {
        try {
            Block fromMod = supplier.get();
            if (fromMod != null) {
                return Optional.of(fromMod);
            }
        } catch (Throwable ignored) {
        }
        return BuiltInRegistries.BLOCK.getOptional(id);
    }

    static BooleanProperty facePropertyFor(Direction dir) {
        return switch (dir) {
            case NORTH -> BlockStateProperties.NORTH;
            case SOUTH -> BlockStateProperties.SOUTH;
            case EAST -> BlockStateProperties.EAST;
            case WEST -> BlockStateProperties.WEST;
            case UP -> BlockStateProperties.UP;
            case DOWN -> BlockStateProperties.DOWN;
        };
    }

    static BlockState configureMultifaceDownIfApplicable(Block block, BlockState state) {
        if (block instanceof MultifaceBlock && state.hasProperty(BlockStateProperties.DOWN)) {
            for (Direction dir : Direction.values()) {
                BooleanProperty prop = facePropertyFor(dir);
                if (state.hasProperty(prop)) {
                    state = state.setValue(prop, dir == Direction.DOWN);
                }
            }
        }
        return state;
    }
}
