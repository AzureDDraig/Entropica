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
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Generates wind-scoured polar silt &amp; grey-sand drifts ({@code 4..7} block wind-elliptical patches)
 * in Biome #15 ({@code 2.15 Dust-Crusted Bergs}):
 * <ul>
 *   <li>Queries {@link Heightmap.Types#MOTION_BLOCKING_NO_LEAVES} and strips any vanilla
 *       {@link Blocks#SNOW} / {@link Blocks#POWDER_SNOW} layers at the surface.</li>
 *   <li>Converts exposed {@code ice}, {@code packed_ice}, {@code snow_block}, {@code stone},
 *       and {@code gravel} into {@code entropica:dust_crusted_ice} and solidly supported
 *       {@code entropica:grey_sand} (never placing {@code grey_sand} over air or water!).</li>
 *   <li>Scatters {@code 1-3} {@code entropica:wind_blown_dust_scrub} shrubs strictly atop
 *       {@code grey_sand} or {@code dust_crusted_ice} (NEVER on bare {@code ice},
 *       {@code packed_ice}, or {@code blue_ice}!).</li>
 * </ul>
 */
public class WindBlownDustPatchFeature extends Feature<NoneFeatureConfiguration> {

    private static final ResourceLocation DUST_CRUSTED_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "dust_crusted_ice");
    private static final ResourceLocation GREY_SAND_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "grey_sand");
    private static final ResourceLocation COARSE_GRAVEL_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "coarse_gravel");
    private static final ResourceLocation WIND_BLOWN_DUST_SCRUB_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "wind_blown_dust_scrub");

    public WindBlownDustPatchFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        Block dustCrustedIceBlock = BuiltInRegistries.BLOCK.getOptional(DUST_CRUSTED_ICE_ID).orElse(Blocks.PACKED_ICE);
        Block greySandBlock = BuiltInRegistries.BLOCK.getOptional(GREY_SAND_ID).orElse(Blocks.SAND);
        Optional<Block> coarseGravelOpt = BuiltInRegistries.BLOCK.getOptional(COARSE_GRAVEL_ID);
        Optional<Block> scrubOpt = BuiltInRegistries.BLOCK.getOptional(WIND_BLOWN_DUST_SCRUB_ID);

        int seaLevel = level.getSeaLevel() > 0 ? level.getSeaLevel() : 63;
        int originSurfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, origin.getX(), origin.getZ()) - 1;
        if (originSurfaceY < seaLevel - 1 || originSurfaceY >= level.getMaxY() - 4) {
            return false;
        }

        double majorRadius = 4.0D + random.nextDouble() * 3.0D; // 4.0..7.0
        double minorRadius = majorRadius * (0.55D + random.nextDouble() * 0.25D);
        double windAngle = random.nextDouble() * (Math.PI * 2.0D);
        double cosW = Math.cos(windAngle);
        double sinW = Math.sin(windAngle);
        double edgePhase = random.nextDouble() * (Math.PI * 2.0D);

        int bound = Mth.ceil(majorRadius) + 1;
        BlockState dustCrustedIce = dustCrustedIceBlock.defaultBlockState();
        BlockState greySand = greySandBlock.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        List<BlockPos> validScrubSurfaces = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int modifiedOrMatched = 0;

        for (int dx = -bound; dx <= bound; dx++) {
            for (int dz = -bound; dz <= bound; dz++) {
                double along = dx * cosW + dz * sinW;
                double cross = -dx * sinW + dz * cosW;
                double normDist = Math.hypot(along / majorRadius, cross / minorRadius);
                double edgeRipple = 1.0D + 0.12D * Math.sin(Math.atan2(cross, along) * 3.0D + edgePhase);
                if (normDist > edgeRipple) {
                    continue;
                }

                int bx = origin.getX() + dx;
                int bz = origin.getZ() + dz;
                int hmY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, bx, bz) - 1;
                if (hmY < seaLevel - 1 || Math.abs(hmY - originSurfaceY) > 5) {
                    continue;
                }

                // Strip 1-layer snow above hmY if present
                cursor.set(bx, hmY + 1, bz);
                if (level.getBlockState(cursor).is(Blocks.SNOW)) {
                    level.setBlock(cursor, air, 2);
                }

                // Strip multi-layer snow / powder_snow down to true solid surface
                int sy = hmY;
                for (int strip = 0; strip < 3 && sy >= seaLevel - 1; strip++) {
                    cursor.set(bx, sy, bz);
                    BlockState st = level.getBlockState(cursor);
                    if (st.is(Blocks.SNOW) || st.is(Blocks.POWDER_SNOW)) {
                        level.setBlock(cursor, air, 2);
                        sy--;
                    } else {
                        break;
                    }
                }
                if (sy < seaLevel - 1) {
                    continue;
                }

                cursor.set(bx, sy + 1, bz);
                if (!level.getBlockState(cursor).isAir()) {
                    continue;
                }

                cursor.set(bx, sy, bz);
                BlockState ground = level.getBlockState(cursor);

                boolean isConvertibleVanillaOrScree = ground.is(Blocks.ICE)
                        || ground.is(Blocks.PACKED_ICE)
                        || ground.is(Blocks.SNOW_BLOCK)
                        || ground.is(Blocks.STONE)
                        || ground.is(Blocks.GRAVEL)
                        || ground.is(Blocks.TUFF)
                        || (coarseGravelOpt.isPresent() && ground.is(coarseGravelOpt.get()));

                boolean isAlreadyDustSurface = ground.is(dustCrustedIceBlock) || ground.is(greySandBlock);
                if (!isConvertibleVanillaOrScree && !isAlreadyDustSurface) {
                    continue;
                }

                BlockPos belowPos = new BlockPos(bx, sy - 1, bz);
                boolean solidSupportBelow = hasSolidSupportBelow(level, belowPos);

                if (isConvertibleVanillaOrScree) {
                    boolean placeGreySand = solidSupportBelow
                            && (normDist <= 0.62D || (normDist <= 0.82D && random.nextFloat() < 0.45F));
                    level.setBlock(cursor, placeGreySand ? greySand : dustCrustedIce, 2);
                } else if (ground.is(greySandBlock) && !solidSupportBelow) {
                    // Never allow grey_sand to remain unsupported over water or air
                    level.setBlock(cursor, dustCrustedIce, 2);
                }

                modifiedOrMatched++;
                BlockState finalGround = level.getBlockState(cursor);
                if (finalGround.is(greySandBlock) || finalGround.is(dustCrustedIceBlock)) {
                    validScrubSurfaces.add(cursor.immutable());
                }
            }
        }

        if (modifiedOrMatched == 0) {
            return false;
        }

        // Place 1-3 entropica:wind_blown_dust_scrub STRICTLY on grey_sand or dust_crusted_ice
        if (scrubOpt.isPresent() && !validScrubSurfaces.isEmpty()) {
            Block scrubBlock = scrubOpt.get();
            BlockState scrubState = nonWaterlogged(scrubBlock.defaultBlockState());
            int targetScrubs = 1 + random.nextInt(3); // 1..3
            int placedScrubs = 0;

            while (!validScrubsEmpty(validScrubSurfaces) && placedScrubs < targetScrubs) {
                int idx = random.nextInt(validScrubSurfaces.size());
                BlockPos groundPos = validScrubSurfaces.remove(idx);
                BlockState groundState = level.getBlockState(groundPos);

                // Strictly forbid bare ice, packed_ice, or blue_ice!
                if (!groundState.is(greySandBlock) && !groundState.is(dustCrustedIceBlock)) {
                    continue;
                }
                if (dustCrustedIceBlock != Blocks.PACKED_ICE
                        && (groundState.is(Blocks.ICE)
                        || groundState.is(Blocks.PACKED_ICE)
                        || groundState.is(Blocks.BLUE_ICE))) {
                    continue;
                }

                BlockPos plantPos = groundPos.above();
                if (!level.getBlockState(plantPos).isAir()) {
                    continue;
                }
                if (scrubState.canSurvive(level, plantPos)) {
                    level.setBlock(plantPos, scrubState, 2);
                    placedScrubs++;
                }
            }
        }

        return true;
    }

    private static boolean validScrubsEmpty(List<BlockPos> list) {
        return list.isEmpty();
    }

    private static boolean hasSolidSupportBelow(WorldGenLevel level, BlockPos belowPos) {
        BlockState below = level.getBlockState(belowPos);
        if (below.isAir()
                || below.is(Blocks.WATER)
                || below.getFluidState().getType() == Fluids.WATER
                || below.is(Blocks.BUBBLE_COLUMN)
                || below.is(Blocks.SNOW)
                || below.is(Blocks.POWDER_SNOW)
                || below.canBeReplaced()) {
            return false;
        }
        return below.isFaceSturdy(level, belowPos, Direction.UP);
    }

    private static BlockState nonWaterlogged(BlockState state) {
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            return state.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        return state;
    }
}
