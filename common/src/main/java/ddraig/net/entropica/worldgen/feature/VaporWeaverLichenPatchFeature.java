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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Places {@code entropica:vapor_weaver_lichen} ({@code MultifaceBlock}) patches clinging naturally
 * onto vertical cracks and warm fissures of {@link Blocks#BLUE_ICE}, {@code entropica:steaming_ice},
 * {@link Blocks#BASALT}, and {@code entropica:steaming_spring_stone}, setting the directional
 * face booleans ({@code NORTH/SOUTH/EAST/WEST/UP/DOWN}) and {@code WATERLOGGED} when submerged.
 */
public class VaporWeaverLichenPatchFeature extends Feature<NoneFeatureConfiguration> {

    private static final ResourceLocation VAPOR_WEAVER_LICHEN_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vapor_weaver_lichen");
    private static final ResourceLocation STEAMING_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "steaming_ice");
    private static final ResourceLocation STEAMING_SPRING_STONE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "steaming_spring_stone");

    public VaporWeaverLichenPatchFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        Block lichenBlock = resolveBlock(VAPOR_WEAVER_LICHEN_ID, Blocks.GLOW_LICHEN);
        Block steamingIceBlock = resolveBlock(STEAMING_ICE_ID, Blocks.BLUE_ICE);
        Block springStoneBlock = resolveBlock(STEAMING_SPRING_STONE_ID, Blocks.BASALT);

        int placed = 0;
        int tries = 28;
        BlockPos.MutableBlockPos targetPos = new BlockPos.MutableBlockPos();

        for (int i = 0; i < tries; i++) {
            int ox = random.nextInt(7) - random.nextInt(7);
            int oy = random.nextInt(4) - random.nextInt(4);
            int oz = random.nextInt(7) - random.nextInt(7);
            targetPos.set(origin.getX() + ox, origin.getY() + oy, origin.getZ() + oz);

            if (targetPos.getY() <= level.getMinY() + 2 || targetPos.getY() >= level.getMaxY() - 2) {
                continue;
            }

            BlockState existing = level.getBlockState(targetPos);
            boolean inWater = existing.is(Blocks.WATER);
            if (!existing.isAir() && !inWater) {
                continue;
            }

            // Require at least one vertical crack host face (NORTH/SOUTH/EAST/WEST) on Blue Ice, Steaming Ice, Basalt, or Spring Stone
            boolean hasVerticalHost = false;
            BlockState lichenState = blankMultifaceState(lichenBlock, inWater);

            for (Direction horiz : Direction.Plane.HORIZONTAL) {
                BlockPos hostPos = targetPos.relative(horiz);
                BlockState hostState = level.getBlockState(hostPos);
                if (isValidCrackHost(hostState, steamingIceBlock, springStoneBlock)
                        && hostState.isFaceSturdy(level, hostPos, horiz.getOpposite())) {
                    BooleanProperty prop = facePropertyFor(horiz);
                    if (lichenState.hasProperty(prop)) {
                        lichenState = lichenState.setValue(prop, true);
                        hasVerticalHost = true;
                    }
                }
            }

            if (!hasVerticalHost) {
                continue;
            }

            // If nestled into a corner/ledge crack, optionally cling to adjacent floor/overhang host too
            if (random.nextFloat() < 0.35F) {
                for (Direction vert : Direction.Plane.VERTICAL) {
                    BlockPos hostPos = targetPos.relative(vert);
                    BlockState hostState = level.getBlockState(hostPos);
                    if (isValidCrackHost(hostState, steamingIceBlock, springStoneBlock)
                            && hostState.isFaceSturdy(level, hostPos, vert.getOpposite())) {
                        BooleanProperty prop = facePropertyFor(vert);
                        if (lichenState.hasProperty(prop)) {
                            lichenState = lichenState.setValue(prop, true);
                        }
                    }
                }
            }

            if (lichenState.canSurvive(level, targetPos)) {
                level.setBlock(targetPos, lichenState, 2);
                placed++;
            }
        }

        return placed > 0;
    }

    public static BlockState blankMultifaceState(Block lichenBlock, boolean waterlogged) {
        BlockState state = lichenBlock.defaultBlockState();
        for (Direction dir : Direction.values()) {
            BooleanProperty prop = facePropertyFor(dir);
            if (state.hasProperty(prop)) {
                state = state.setValue(prop, false);
            }
        }
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, waterlogged);
        }
        return state;
    }

    public static BooleanProperty facePropertyFor(Direction dir) {
        return switch (dir) {
            case NORTH -> BlockStateProperties.NORTH;
            case SOUTH -> BlockStateProperties.SOUTH;
            case EAST -> BlockStateProperties.EAST;
            case WEST -> BlockStateProperties.WEST;
            case UP -> BlockStateProperties.UP;
            case DOWN -> BlockStateProperties.DOWN;
        };
    }

    private static boolean isValidCrackHost(BlockState state, Block steamingIceBlock, Block springStoneBlock) {
        return state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(steamingIceBlock)
                || state.is(Blocks.BASALT)
                || state.is(Blocks.SMOOTH_BASALT)
                || state.is(Blocks.POLISHED_BASALT)
                || state.is(springStoneBlock);
    }

    private static Block resolveBlock(ResourceLocation id, Block fallback) {
        return BuiltInRegistries.BLOCK.getOptional(id).orElse(fallback);
    }
}
