package ddraig.net.entropica.worldgen.feature;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Biome #13 ({@code 2.13 Mirage Glacier}) Surface Feature:
 * Generates dramatic crystalline parabolic ice arches ({@code span 9-15} blocks, {@code height 5-9} blocks)
 * composed of {@link Blocks#BLUE_ICE} abutments, seamless solid {@code entropica:mirage_ice} ribs, and
 * translucent {@code entropica:mirage_glass} prism cores bridging across glacier valleys and
 * {@code entropica:glass_sand} margins.
 */
public class MirageIceArchFeature extends Feature<NoneFeatureConfiguration> {

    private static final ResourceLocation MIRAGE_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mirage_ice");
    private static final ResourceLocation MIRAGE_GLASS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mirage_glass");
    private static final ResourceLocation GLASS_SAND_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "glass_sand");
    private static final ResourceLocation GLASS_SANDSTONE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "glass_sandstone");
    private static final ResourceLocation VITREOUS_CACTUS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vitreous_cactus");

    public MirageIceArchFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos rawOrigin = context.origin();
        RandomSource random = context.random();

        // Keep center safely near the middle of the decorating chunk so span <= 15 stays well inside 3x3 region
        ChunkPos chunkPos = new ChunkPos(rawOrigin);
        int cx = Mth.clamp(rawOrigin.getX(), chunkPos.getMinBlockX() + 4, chunkPos.getMaxBlockX() - 4);
        int cz = Mth.clamp(rawOrigin.getZ(), chunkPos.getMinBlockZ() + 4, chunkPos.getMaxBlockZ() - 4);
        int refY = rawOrigin.getY();

        int span = 9 + random.nextInt(7);      // 9..15 blocks
        int archHeight = 5 + random.nextInt(5); // 5..9 blocks
        double halfSpan = span * 0.5D;
        double baseAngle = random.nextDouble() * Math.PI;

        double chosenUx = 0.0D;
        double chosenUz = 0.0D;
        int ay = Integer.MIN_VALUE;
        int by = Integer.MIN_VALUE;

        for (int attempt = 0; attempt < 4; attempt++) {
            double theta = baseAngle + attempt * (Math.PI * 0.25D);
            double ux = Math.cos(theta);
            double uz = Math.sin(theta);

            int ax = cx - (int) Math.round(ux * halfSpan);
            int az = cz - (int) Math.round(uz * halfSpan);
            int bx = cx + (int) Math.round(ux * halfSpan);
            int bz = cz + (int) Math.round(uz * halfSpan);

            int candAy = findFirmAbutmentY(level, ax, refY, az);
            int candBy = findFirmAbutmentY(level, bx, refY, bz);
            if (candAy != Integer.MIN_VALUE && candBy != Integer.MIN_VALUE && Math.abs(candAy - candBy) <= 6) {
                chosenUx = ux;
                chosenUz = uz;
                ay = candAy;
                by = candBy;
                break;
            }
        }

        if (ay == Integer.MIN_VALUE || by == Integer.MIN_VALUE) {
            return false;
        }

        int peakCheckY = Math.max(ay, by) + archHeight + 2;
        if (peakCheckY >= level.getMaxY() - 2) {
            return false;
        }

        // Reject if central span lacks clear valley/air clearance beneath the arch crown
        int midGroundY = findFirmAbutmentY(level, cx, (ay + by) / 2, cz);
        int crownExpectedY = (ay + by) / 2 + archHeight;
        if (midGroundY != Integer.MIN_VALUE && midGroundY >= crownExpectedY - 2) {
            return false;
        }

        Block mirageIceBlock = BuiltInRegistries.BLOCK.getOptional(MIRAGE_ICE_ID).orElse(Blocks.BLUE_ICE);
        Block mirageGlassBlock = BuiltInRegistries.BLOCK.getOptional(MIRAGE_GLASS_ID).orElse(Blocks.TINTED_GLASS);
        Block glassSandstoneBlock = BuiltInRegistries.BLOCK.getOptional(GLASS_SANDSTONE_ID).orElse(Blocks.SANDSTONE);

        BlockState blueIce = Blocks.BLUE_ICE.defaultBlockState();
        BlockState packedIce = Blocks.PACKED_ICE.defaultBlockState();
        BlockState mirageIce = solidMirageIceState(mirageIceBlock);
        BlockState mirageGlass = nonWaterlogged(mirageGlassBlock.defaultBlockState());
        BlockState glassSandstone = glassSandstoneBlock.defaultBlockState();

        double nx = -chosenUz;
        double nz = chosenUx;

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int placedBlocks = 0;

        int steps = Math.max(28, span * 3);
        for (int s = 0; s <= steps; s++) {
            double frac = (double) s / (double) steps; // 0..1
            double t = frac * 2.0D - 1.0D;             // -1..+1
            double absT = Math.abs(t);
            double u = t * halfSpan;

            double baseLineY = Mth.lerp(frac, ay, by);
            double archCenterY = baseLineY + archHeight * (1.0D - t * t);

            // Sturdy tapered profile: wider at abutments (|t| -> 1), sleek prism rib at crown (t -> 0)
            double lateralHalfWidth = Mth.lerp(absT * absT, 0.85D, 1.55D);
            double vertHalfThick = Mth.lerp(absT, 0.95D, 1.45D);

            int wBound = (int) Math.ceil(lateralHalfWidth);
            int yMin = (int) Math.floor(archCenterY - vertHalfThick);
            int yMax = (int) Math.ceil(archCenterY + vertHalfThick);

            for (int wStep = -wBound; wStep <= wBound; wStep++) {
                double wDist = Math.abs(wStep);
                if (wDist > lateralHalfWidth + 0.15D) {
                    continue;
                }

                int wx = (int) Math.round(cx + chosenUx * u + nx * wStep);
                int wz = (int) Math.round(cz + chosenUz * u + nz * wStep);

                for (int wy = yMin; wy <= yMax; wy++) {
                    double dyNorm = (wy - archCenterY) / Math.max(0.65D, vertHalfThick);
                    double dwNorm = wDist / Math.max(0.65D, lateralHalfWidth);
                    if (dyNorm * dyNorm + dwNorm * dwNorm > 1.18D) {
                        continue;
                    }

                    cursor.set(wx, wy, wz);
                    if (!canArchReplace(level.getBlockState(cursor))) {
                        continue;
                    }

                    BlockState toPlace;
                    if (absT >= 0.74D) {
                        // Abutments: heavy Blue Ice buttresses transitioning into Mirage Ice ribs
                        toPlace = (absT >= 0.84D || dyNorm < -0.15D) ? blueIce : mirageIce;
                    } else if (absT <= 0.58D && wDist <= 0.45D && dyNorm <= 0.25D) {
                        // Inner arch belly & crown heart: translucent Mirage Glass prism core
                        toPlace = mirageGlass;
                    } else {
                        // Seamless sweeping Mirage Ice structural ribs
                        toPlace = mirageIce;
                    }

                    level.setBlock(cursor, toPlace, 2);
                    placedBlocks++;
                }

                // Anchor both abutment ends down into the valley floor / glass_sand slope so arches never float
                if (absT >= 0.78D && wDist <= 1.15D) {
                    int anchorBottom = Math.max(level.getMinY() + 2, yMin - 6);
                    for (int wy = yMin - 1; wy >= anchorBottom; wy--) {
                        cursor.set(wx, wy, wz);
                        BlockState existing = level.getBlockState(cursor);
                        if (isSoftOrAirForFoundation(existing)) {
                            BlockState rootState = isSandLike(existing)
                                    ? glassSandstone
                                    : ((wy == yMin - 1) ? blueIce : packedIce);
                            level.setBlock(cursor, rootState, 2);
                        } else {
                            break;
                        }
                    }
                }
            }

            // Hanging crystalline Mirage Glass prism facet beneath the arch keystone
            if (absT <= 0.14D) {
                int kx = (int) Math.round(cx + chosenUx * u);
                int kz = (int) Math.round(cz + chosenUz * u);
                cursor.set(kx, yMin - 1, kz);
                if (level.getBlockState(cursor).isAir()) {
                    level.setBlock(cursor, mirageGlass, 2);
                }
            }
        }

        return placedBlocks >= 16;
    }

    private static int findFirmAbutmentY(WorldGenLevel level, int x, int refY, int z) {
        int maxScan = Math.min(level.getMaxY() - 4, refY + 8);
        int minScan = Math.max(level.getMinY() + 4, refY - 8);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int y = maxScan; y >= minScan; y--) {
            cursor.set(x, y, z);
            BlockState state = level.getBlockState(cursor);
            if (!state.getFluidState().isEmpty()) {
                return Integer.MIN_VALUE;
            }
            if (isFirmTerrain(state)) {
                cursor.set(x, y + 1, z);
                BlockState above = level.getBlockState(cursor);
                if (above.getFluidState().isEmpty() && canArchReplace(above)) {
                    return y;
                }
            }
        }
        return Integer.MIN_VALUE;
    }

    private static boolean isFirmTerrain(BlockState state) {
        if (state.isAir() || state.is(Blocks.SNOW) || state.is(Blocks.DEAD_BUSH)) {
            return false;
        }
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (VITREOUS_CACTUS_ID.equals(key)) {
            return false;
        }
        return state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.ICE)
                || state.is(Blocks.SAND)
                || state.is(Blocks.RED_SAND)
                || state.is(Blocks.SANDSTONE)
                || state.is(Blocks.RED_SANDSTONE)
                || state.is(Blocks.STONE)
                || state.is(Blocks.DEEPSLATE)
                || state.is(Blocks.CALCITE)
                || MIRAGE_ICE_ID.equals(key)
                || GLASS_SAND_ID.equals(key)
                || GLASS_SANDSTONE_ID.equals(key);
    }

    private static boolean canArchReplace(BlockState state) {
        if (state.getBlock() instanceof ChestBlock || state.hasBlockEntity()) {
            return false;
        }
        if (state.isAir() || state.is(Blocks.SNOW) || state.is(Blocks.POWDER_SNOW) || state.is(Blocks.DEAD_BUSH)) {
            return true;
        }
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.SAND)
                || state.is(Blocks.RED_SAND)
                || MIRAGE_ICE_ID.equals(key)
                || GLASS_SAND_ID.equals(key);
    }

    private static boolean isSoftOrAirForFoundation(BlockState state) {
        if (state.getBlock() instanceof ChestBlock || state.hasBlockEntity()) {
            return false;
        }
        if (state.isAir() || state.is(Blocks.SNOW) || state.is(Blocks.POWDER_SNOW)
                || state.is(Blocks.DEAD_BUSH) || state.canBeReplaced()) {
            return true;
        }
        return isSandLike(state);
    }

    private static boolean isSandLike(BlockState state) {
        if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
            return true;
        }
        return GLASS_SAND_ID.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }

    private static BlockState solidMirageIceState(Block block) {
        BlockState state = nonWaterlogged(block.defaultBlockState());
        for (Property<?> prop : state.getProperties()) {
            if ("phased".equals(prop.getName()) && prop instanceof BooleanProperty boolProp) {
                return state.setValue(boolProp, false);
            }
        }
        return state;
    }

    private static BlockState nonWaterlogged(BlockState state) {
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            return state.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        return state;
    }
}
