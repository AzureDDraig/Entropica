package ddraig.net.entropica.worldgen.feature;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Embeds irregular {@code 3-6} radius vitrified surface veins and sheets of
 * {@code entropica:fulgurite_ice} and {@link Blocks#PACKED_ICE} flush into
 * {@link Blocks#SNOW_BLOCK} / {@link Blocks#PACKED_ICE} terrain across {@code 2.7 Static Ice-Plains},
 * creating spark-jumping fulgurite ice patches across the wind-swept snowfields.
 */
public class FulguriteIceSheetFeature extends Feature<NoneFeatureConfiguration> {

    private static final ResourceLocation FULGURITE_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "fulgurite_ice");

    public FulguriteIceSheetFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        Block fulguriteIceBlock = BuiltInRegistries.BLOCK.getOptional(FULGURITE_ICE_ID).orElse(Blocks.PACKED_ICE);
        BlockState fulguriteIce = fulguriteIceBlock.defaultBlockState();
        BlockState packedIce = Blocks.PACKED_ICE.defaultBlockState();

        BlockPos centerGround = findFlushSnowSurface(level, origin.getX(), origin.getZ(), fulguriteIceBlock);
        if (centerGround == null) {
            return false;
        }

        int radius = 3 + random.nextInt(4); // 3..6 blocks
        float stretchX = 0.80F + random.nextFloat() * 0.45F;
        float stretchZ = 0.80F + random.nextFloat() * 0.45F;
        float strikeAngle = random.nextFloat() * ((float) Math.PI * 2.0F);
        float branchPhase = random.nextFloat() * ((float) Math.PI * 2.0F);

        int placedCount = 0;
        int maxScan = Math.min(7, radius + 1);

        for (int dx = -maxScan; dx <= maxScan; dx++) {
            for (int dz = -maxScan; dz <= maxScan; dz++) {
                float sx = dx / stretchX;
                float sz = dz / stretchZ;
                float dist = Mth.sqrt(sx * sx + sz * sz);
                if (dist > radius + 1.15F) {
                    continue;
                }

                float theta = (float) Mth.atan2(dz, dx);
                // Lobed lightning-strike dendritic contour (3-5 radial Lichtenberg lobes)
                float lobe = 0.72F
                        + 0.22F * Mth.cos((theta - strikeAngle) * 3.0F)
                        + 0.14F * Mth.sin(theta * 5.0F + branchPhase);
                float effectiveRadius = radius * lobe;
                if (dist > effectiveRadius) {
                    continue;
                }

                int wx = centerGround.getX() + dx;
                int wz = centerGround.getZ() + dz;
                if (Math.abs(wx - origin.getX()) > 7 || Math.abs(wz - origin.getZ()) > 7) {
                    continue;
                }

                BlockPos tileGround = findFlushSnowSurface(level, wx, wz, fulguriteIceBlock);
                if (tileGround == null || Math.abs(tileGround.getY() - centerGround.getY()) > 2) {
                    continue;
                }

                float normDist = effectiveRadius > 0.001F ? (dist / effectiveRadius) : 0.0F;
                // Inner core & dendritic veins are vitrified fulgurite_ice; outer collar blends into packed_ice
                boolean alongVein = Math.abs(Mth.sin((theta - strikeAngle) * 2.0F)) < 0.35F;
                boolean placeFulgurite = normDist < 0.68F
                        || (alongVein && normDist < 0.90F)
                        || random.nextFloat() < 0.35F;

                BlockState chosenIce = placeFulgurite ? fulguriteIce : packedIce;
                level.setBlock(tileGround, chosenIce, 2);

                // Keep fulgurite_ice exposed flush to the sky so its blue surface sparks are clearly visible
                BlockPos abovePos = tileGround.above();
                if (placeFulgurite && level.getBlockState(abovePos).is(Blocks.SNOW)) {
                    level.setBlock(abovePos, Blocks.AIR.defaultBlockState(), 2);
                }

                // Occasional 2-block deep vitrified taproot at sheet center
                if (normDist < 0.32F && random.nextFloat() < 0.50F) {
                    BlockPos subPos = tileGround.below();
                    if (isReplaceableTerrain(level.getBlockState(subPos), fulguriteIceBlock)) {
                        level.setBlock(subPos, packedIce, 2);
                    }
                }

                placedCount++;
            }
        }

        return placedCount >= 5;
    }

    private static BlockPos findFlushSnowSurface(WorldGenLevel level, int x, int z, Block fulguriteIceBlock) {
        int topY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
        if (topY < 62) {
            return null;
        }

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, topY, z);
        BlockState state = level.getBlockState(cursor);
        if (state.is(Blocks.SNOW) || state.isAir()) {
            cursor.move(0, -1, 0);
            state = level.getBlockState(cursor);
        }

        if (!isReplaceableTerrain(state, fulguriteIceBlock)) {
            return null;
        }

        BlockState above = level.getBlockState(cursor.above());
        if (!above.isAir() && !above.is(Blocks.SNOW)) {
            return null;
        }
        return cursor.immutable();
    }

    private static boolean isReplaceableTerrain(BlockState state, Block fulguriteIceBlock) {
        return state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.ICE)
                || state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.POWDER_SNOW)
                || state.is(fulguriteIceBlock)
                || state.is(BlockTags.DIRT)
                || state.is(Blocks.GRAVEL);
    }
}
