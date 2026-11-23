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
 * Places coastal clusters of {@code 1-3} {@code entropica:void_crystal_block} embedded in
 * {@code void_sand}, {@code void_sand_turf}, {@code void_stone}, or {@code blue_ice},
 * crowned with directional {@code entropica:void_crystal_cluster} and {@code entropica:void_crystal_bud} crystals.
 */
public class VoidCrystalBeachPatchFeature extends Feature<NoneFeatureConfiguration> {

    private static final ResourceLocation VOID_SAND_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "void_sand");
    private static final ResourceLocation VOID_SAND_TURF_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "void_sand_turf");
    private static final ResourceLocation VOID_STONE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "void_stone");
    private static final ResourceLocation VOID_CRYSTAL_BLOCK_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "void_crystal_block");
    private static final ResourceLocation VOID_CRYSTAL_CLUSTER_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "void_crystal_cluster");
    private static final ResourceLocation VOID_CRYSTAL_BUD_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "void_crystal_bud");

    public VoidCrystalBeachPatchFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        Optional<Block> voidSandOpt = BuiltInRegistries.BLOCK.getOptional(VOID_SAND_ID);
        Optional<Block> voidSandTurfOpt = BuiltInRegistries.BLOCK.getOptional(VOID_SAND_TURF_ID);
        Optional<Block> voidStoneOpt = BuiltInRegistries.BLOCK.getOptional(VOID_STONE_ID);

        BlockPos anchor = findCoastalHost(level, origin, random, voidSandOpt, voidSandTurfOpt, voidStoneOpt);
        if (anchor == null) {
            return false;
        }

        Block crystalBlock = BuiltInRegistries.BLOCK.getOptional(VOID_CRYSTAL_BLOCK_ID).orElse(Blocks.AMETHYST_BLOCK);
        Block crystalCluster = BuiltInRegistries.BLOCK.getOptional(VOID_CRYSTAL_CLUSTER_ID).orElse(Blocks.AMETHYST_CLUSTER);
        Block crystalBud = BuiltInRegistries.BLOCK.getOptional(VOID_CRYSTAL_BUD_ID).orElse(Blocks.LARGE_AMETHYST_BUD);

        BlockState crystalBlockState = crystalBlock.defaultBlockState();
        int targetBlocks = 1 + random.nextInt(3); // 1..3 void_crystal_block blocks
        List<BlockPos> placedCores = new ArrayList<>(3);

        // 1. Primary embedded beach crystal block
        level.setBlock(anchor, crystalBlockState, 2);
        placedCores.add(anchor.immutable());

        // 2. Optional jutting upper spur or embedded neighbor (up to 3 total blocks)
        if (targetBlocks >= 2) {
            Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            BlockPos abovePos = anchor.above();
            BlockPos sidePos = anchor.relative(side);

            if (random.nextBoolean() && isCrystalReplaceableAbove(level.getBlockState(abovePos))) {
                level.setBlock(abovePos, crystalBlockState, 2);
                placedCores.add(abovePos.immutable());
            } else if (canEmbedIntoBeach(level.getBlockState(sidePos), voidSandOpt, voidSandTurfOpt, voidStoneOpt)) {
                level.setBlock(sidePos, crystalBlockState, 2);
                placedCores.add(sidePos.immutable());
            }

            if (targetBlocks == 3) {
                Direction secondSide = side.getClockWise();
                BlockPos thirdPos = placedCores.contains(abovePos) ? anchor.relative(side) : abovePos;
                BlockState thirdExisting = level.getBlockState(thirdPos);
                if (isCrystalReplaceableAbove(thirdExisting)
                        || canEmbedIntoBeach(thirdExisting, voidSandOpt, voidSandTurfOpt, voidStoneOpt)) {
                    level.setBlock(thirdPos, crystalBlockState, 2);
                    placedCores.add(thirdPos.immutable());
                } else {
                    BlockPos altSide = anchor.relative(secondSide);
                    if (canEmbedIntoBeach(level.getBlockState(altSide), voidSandOpt, voidSandTurfOpt, voidStoneOpt)) {
                        level.setBlock(altSide, crystalBlockState, 2);
                        placedCores.add(altSide.immutable());
                    }
                }
            }
        }

        // 3. Crown exposed top & coastal faces with directional void_crystal_cluster & void_crystal_bud
        boolean crownedTop = false;
        for (BlockPos corePos : placedCores) {
            // Always try to crown the top face first so every outcrop has a signature upward crystal crown
            BlockPos topPos = corePos.above();
            BlockState topTarget = level.getBlockState(topPos);
            if (!placedCores.contains(topPos) && isCrystalReplaceableAbove(topTarget)) {
                boolean waterlogged = topTarget.getFluidState().getType() == Fluids.WATER;
                Block chosenTop = (!crownedTop || random.nextFloat() < 0.65F) ? crystalCluster : crystalBud;
                BlockState crownState = orientCrystal(chosenTop.defaultBlockState(), Direction.UP, waterlogged);
                if (crownState.canSurvive(level, topPos)) {
                    level.setBlock(topPos, crownState, 2);
                    crownedTop = true;
                }
            }

            // Side crystal buds & clusters on exposed horizontal faces
            for (Direction horiz : Direction.Plane.HORIZONTAL) {
                if (random.nextFloat() >= 0.45F) {
                    continue;
                }
                BlockPos sideBudPos = corePos.relative(horiz);
                if (placedCores.contains(sideBudPos)) {
                    continue;
                }
                BlockState sideTarget = level.getBlockState(sideBudPos);
                if (isCrystalReplaceableAbove(sideTarget)) {
                    boolean waterlogged = sideTarget.getFluidState().getType() == Fluids.WATER;
                    Block chosenSide = random.nextFloat() < 0.55F ? crystalBud : crystalCluster;
                    BlockState sideState = orientCrystal(chosenSide.defaultBlockState(), horiz, waterlogged);
                    if (sideState.canSurvive(level, sideBudPos)) {
                        level.setBlock(sideBudPos, sideState, 2);
                    }
                }
            }
        }

        return true;
    }

    private static BlockPos findCoastalHost(
            WorldGenLevel level,
            BlockPos origin,
            RandomSource random,
            Optional<Block> voidSandOpt,
            Optional<Block> voidSandTurfOpt,
            Optional<Block> voidStoneOpt
    ) {
        List<BlockPos> matches = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int dy = 2; dy >= -4; dy--) {
            int y = origin.getY() + dy;
            if (y <= level.getMinY() + 3 || y >= level.getMaxY() - 6) {
                continue;
            }
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    cursor.set(origin.getX() + dx, y, origin.getZ() + dz);
                    BlockState ground = level.getBlockState(cursor);
                    if (!canEmbedIntoBeach(ground, voidSandOpt, voidSandTurfOpt, voidStoneOpt)) {
                        continue;
                    }
                    BlockState above = level.getBlockState(cursor.above());
                    if (isCrystalReplaceableAbove(above)) {
                        matches.add(cursor.immutable());
                    }
                }
            }
        }

        if (matches.isEmpty()) {
            return null;
        }
        return matches.get(random.nextInt(matches.size()));
    }

    private static boolean canEmbedIntoBeach(
            BlockState state,
            Optional<Block> voidSandOpt,
            Optional<Block> voidSandTurfOpt,
            Optional<Block> voidStoneOpt
    ) {
        if (state.is(Blocks.BLUE_ICE)) {
            return true;
        }
        if (voidSandOpt.isPresent() && state.is(voidSandOpt.get())) {
            return true;
        }
        if (voidSandTurfOpt.isPresent() && state.is(voidSandTurfOpt.get())) {
            return true;
        }
        return voidStoneOpt.isPresent() && state.is(voidStoneOpt.get());
    }

    private static boolean isCrystalReplaceableAbove(BlockState state) {
        return state.isAir()
                || state.is(Blocks.SNOW)
                || state.is(Blocks.WATER);
    }

    private static BlockState orientCrystal(BlockState base, Direction facing, boolean waterlogged) {
        BlockState state = base;
        if (state.hasProperty(BlockStateProperties.FACING)) {
            state = state.setValue(BlockStateProperties.FACING, facing);
        }
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, waterlogged);
        }
        return state;
    }
}
