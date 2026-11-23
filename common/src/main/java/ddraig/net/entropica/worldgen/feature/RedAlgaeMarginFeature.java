package ddraig.net.entropica.worldgen.feature;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
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

import java.util.Optional;

/**
 * Places coastal and shallow-tidal clusters ({@code 8..18} attempts) of
 * {@code entropica:red_algae} in Biome #16 ({@code 2.16 Blood-Ice Shallows}):
 * <ul>
 *   <li>Sets {@code WATERLOGGED = true} when placed in shallow bay water above
 *       {@code entropica:red_clay_sand} or {@code entropica:sanguine_ice}.</li>
 *   <li>Sets {@code WATERLOGGED = false} when placed on exposed coastal
 *       {@code entropica:sanguine_ice} or {@code entropica:red_clay_sand} margins.</li>
 * </ul>
 */
public class RedAlgaeMarginFeature extends Feature<NoneFeatureConfiguration> {

    private static final ResourceLocation RED_ALGAE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "red_algae");
    private static final ResourceLocation SANGUINE_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "sanguine_ice");
    private static final ResourceLocation RED_CLAY_SAND_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "red_clay_sand");

    public RedAlgaeMarginFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        Optional<Block> redAlgaeOpt = SanguineIceShelfFeature.resolveOptionalBlock(
                () -> ModBlocks.RED_ALGAE.isPresent() ? ModBlocks.RED_ALGAE.get() : null,
                RED_ALGAE_ID
        );
        if (redAlgaeOpt.isEmpty()) {
            return false;
        }

        Block algaeBlock = redAlgaeOpt.get();
        Block sanguineIceBlock = SanguineIceShelfFeature.resolveModOrRegistryBlock(
                () -> ModBlocks.SANGUINE_ICE.isPresent() ? ModBlocks.SANGUINE_ICE.get() : null,
                SANGUINE_ICE_ID,
                Blocks.PACKED_ICE
        );
        Block redClaySandBlock = SanguineIceShelfFeature.resolveModOrRegistryBlock(
                () -> ModBlocks.RED_CLAY_SAND.isPresent() ? ModBlocks.RED_CLAY_SAND.get() : null,
                RED_CLAY_SAND_ID,
                Blocks.RED_SAND
        );

        int seaLevel = level.getSeaLevel() > 0 ? level.getSeaLevel() : 63;
        int waterTopY = seaLevel - 1; // Y = 62

        int attempts = 8 + random.nextInt(11); // 8..18 attempts
        int placed = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos below = new BlockPos.MutableBlockPos();

        for (int i = 0; i < attempts; i++) {
            int dx = random.nextInt(6) - random.nextInt(6);
            int dz = random.nextInt(6) - random.nextInt(6);
            int bx = origin.getX() + dx;
            int bz = origin.getZ() + dz;

            int topScanY = Math.min(level.getMaxY() - 2, Math.max(origin.getY() + 3, waterTopY + 4));
            int bottomScanY = Math.max(level.getMinY() + 2, Math.min(origin.getY() - 5, waterTopY - 5));

            for (int sy = topScanY; sy >= bottomScanY; sy--) {
                cursor.set(bx, sy, bz);
                below.set(bx, sy - 1, bz);

                BlockState targetState = level.getBlockState(cursor);
                BlockState substrateState = level.getBlockState(below);

                if (!isValidAlgaeSubstrate(substrateState, sanguineIceBlock, redClaySandBlock)) {
                    continue;
                }

                // Case 1: Submerged shallow water above Red Clay Sand or Sanguine Ice -> WATERLOGGED = true
                boolean isSourceWater = targetState.is(Blocks.WATER)
                        && targetState.getFluidState().getType() == Fluids.WATER
                        && targetState.getFluidState().isSource();
                if (isSourceWater && sy >= waterTopY - 5 && sy <= waterTopY) {
                    BlockState wetAlgae = prepareRedAlgaeState(algaeBlock, true);
                    if (wetAlgae.canSurvive(level, cursor)) {
                        level.setBlock(cursor, wetAlgae, 2);
                        placed++;
                    }
                    break;
                }

                // Case 2: Exposed coastal Sanguine Ice / Red Clay Sand margin -> WATERLOGGED = false
                if ((targetState.isAir() || targetState.is(Blocks.SNOW)) && sy >= waterTopY && sy <= waterTopY + 4) {
                    if (targetState.is(Blocks.SNOW)) {
                        level.setBlock(cursor, Blocks.AIR.defaultBlockState(), 2);
                    }
                    BlockState dryAlgae = prepareRedAlgaeState(algaeBlock, false);
                    if (dryAlgae.canSurvive(level, cursor)) {
                        level.setBlock(cursor, dryAlgae, 2);
                        placed++;
                    }
                    break;
                }
            }
        }

        return placed > 0;
    }

    private static boolean isValidAlgaeSubstrate(
            BlockState state,
            Block sanguineIceBlock,
            Block redClaySandBlock
    ) {
        return state.is(sanguineIceBlock) || state.is(redClaySandBlock);
    }

    public static BlockState prepareRedAlgaeState(Block algaeBlock, boolean waterlogged) {
        BlockState state = algaeBlock.defaultBlockState();
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, waterlogged);
        }
        return SanguineIceShelfFeature.configureMultifaceDownIfApplicable(algaeBlock, state);
    }
}
