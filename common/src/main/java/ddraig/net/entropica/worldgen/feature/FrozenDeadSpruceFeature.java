package ddraig.net.entropica.worldgen.feature;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
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
 * Generates fallen and tilted dead {@link Blocks#SPRUCE_LOG} and {@link Blocks#STRIPPED_SPRUCE_WOOD}
 * trunks ({@code 3-6} blocks long) frozen half-submerged into the ice floes ({@code Y=62..65}) of
 * {@code 2.3 Frost-Bite Floes}, sprouting {@code 2-5} directional {@code entropica:frozen_spruce_branch} limbs.
 */
public class FrozenDeadSpruceFeature extends Feature<NoneFeatureConfiguration> {

    private static final int MIN_TRUNK_Y = 62;
    private static final int MAX_TRUNK_Y = 65;

    private static final ResourceLocation BLIGHTED_PACKED_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "blighted_packed_ice");
    private static final ResourceLocation ROTTEN_ICE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "rotten_ice");
    private static final ResourceLocation FROZEN_SPRUCE_BRANCH_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "frozen_spruce_branch");

    private record BranchCandidate(BlockPos trunkPos, Direction outwardFace) {}

    public FrozenDeadSpruceFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        Block blightedIceBlock = BuiltInRegistries.BLOCK.getOptional(BLIGHTED_PACKED_ICE_ID).orElse(Blocks.PACKED_ICE);
        Block rottenIceBlock = BuiltInRegistries.BLOCK.getOptional(ROTTEN_ICE_ID).orElse(Blocks.ICE);
        Optional<Block> branchBlockOpt = BuiltInRegistries.BLOCK.getOptional(FROZEN_SPRUCE_BRANCH_ID);

        BlockPos anchor = findFloeOrShallowAnchor(level, origin, random, blightedIceBlock, rottenIceBlock);
        if (anchor == null) {
            return false;
        }

        int trunkLength = 3 + random.nextInt(4); // 3..6 blocks
        Direction trunkDir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        boolean fallenDriftTrunk = random.nextFloat() < 0.65F;

        List<BlockPos> placedTrunk = new ArrayList<>(trunkLength);
        BlockState blightedIce = blightedIceBlock.defaultBlockState();

        if (fallenDriftTrunk) {
            // Half-submerged fallen drift trunk spanning Y=62..64 across the ice floe
            Direction.Axis logAxis = trunkDir.getAxis();
            int baseY = Mth.clamp(anchor.getY(), MIN_TRUNK_Y, MIN_TRUNK_Y + 1); // 62..63
            int riseStep = 1 + random.nextInt(Math.max(1, trunkLength - 1));

            for (int i = 0; i < trunkLength; i++) {
                int bx = anchor.getX() + trunkDir.getStepX() * i;
                int bz = anchor.getZ() + trunkDir.getStepZ() * i;
                if (Math.abs(bx - origin.getX()) > 6 || Math.abs(bz - origin.getZ()) > 6) {
                    break;
                }

                int by = baseY + (i >= riseStep ? 1 : 0);
                if (i == trunkLength - 1 && trunkLength >= 5 && random.nextBoolean()) {
                    by = Math.min(MAX_TRUNK_Y, by + 1);
                }
                by = Mth.clamp(by, MIN_TRUNK_Y, MAX_TRUNK_Y);

                BlockPos tPos = new BlockPos(bx, by, bz);
                if (canTrunkReplace(level.getBlockState(tPos), blightedIceBlock, rottenIceBlock)) {
                    BlockState woodState = chooseTrunkWood(random, logAxis);
                    level.setBlock(tPos, woodState, 2);
                    placedTrunk.add(tPos);
                }
            }
        } else {
            // Tilted wind-snapped boreal snag wedged at Y=62 and leaning up to Y=64..65
            int curX = anchor.getX();
            int curZ = anchor.getZ();
            int curY = MIN_TRUNK_Y;
            int leanIndex = 1 + random.nextInt(Math.max(1, trunkLength - 1));

            for (int i = 0; i < trunkLength; i++) {
                Direction.Axis segAxis = Direction.Axis.Y;
                if (i == leanIndex || (trunkLength >= 5 && i == trunkLength - 1)) {
                    curX += trunkDir.getStepX();
                    curZ += trunkDir.getStepZ();
                    if (curY >= MAX_TRUNK_Y) {
                        segAxis = trunkDir.getAxis();
                    }
                }
                if (Math.abs(curX - origin.getX()) > 6 || Math.abs(curZ - origin.getZ()) > 6) {
                    break;
                }

                int placeY = Mth.clamp(curY, MIN_TRUNK_Y, MAX_TRUNK_Y);
                BlockPos tPos = new BlockPos(curX, placeY, curZ);
                if (canTrunkReplace(level.getBlockState(tPos), blightedIceBlock, rottenIceBlock)) {
                    BlockState woodState = chooseTrunkWood(random, segAxis);
                    level.setBlock(tPos, woodState, 2);
                    placedTrunk.add(tPos);
                }
                if (curY < MAX_TRUNK_Y) {
                    curY++;
                }
            }
        }

        if (placedTrunk.size() < 2) {
            return false;
        }

        // Freeze a small rime collar around the trunk base if wedged in open water so it looks locked in ice
        BlockPos buttPos = placedTrunk.get(0);
        for (Direction h : Direction.Plane.HORIZONTAL.shuffledCopy(random)) {
            BlockPos collarPos = new BlockPos(buttPos.getX() + h.getStepX(), 63, buttPos.getZ() + h.getStepZ());
            if (Math.abs(collarPos.getX() - origin.getX()) <= 6
                    && Math.abs(collarPos.getZ() - origin.getZ()) <= 6
                    && level.getBlockState(collarPos.below()).is(Blocks.WATER)
                    && level.getBlockState(collarPos).isAir()
                    && random.nextFloat() < 0.65F) {
                level.setBlock(collarPos, random.nextFloat() < 0.75F ? blightedIce : Blocks.PACKED_ICE.defaultBlockState(), 2);
            }
        }

        // Sprout 2-5 directional frozen_spruce_branch limbs on exposed trunk faces
        if (branchBlockOpt.isPresent()) {
            Block branchBlock = branchBlockOpt.get();
            int targetLimbs = 2 + random.nextInt(4); // 2..5
            List<BranchCandidate> candidates = new ArrayList<>();

            for (int idx = 0; idx < placedTrunk.size(); idx++) {
                BlockPos tPos = placedTrunk.get(idx);
                candidates.add(new BranchCandidate(tPos, Direction.UP));
                for (Direction h : Direction.Plane.HORIZONTAL) {
                    // Prefer lateral faces and exposed trunk tip
                    if (idx == placedTrunk.size() - 1 || h.getAxis() != trunkDir.getAxis() || !fallenDriftTrunk) {
                        candidates.add(new BranchCandidate(tPos, h));
                    }
                }
            }

            // Deterministic Fisher-Yates shuffle
            for (int i = candidates.size() - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                BranchCandidate tmp = candidates.get(i);
                candidates.set(i, candidates.get(j));
                candidates.set(j, tmp);
            }

            int placedLimbs = 0;
            for (BranchCandidate cand : candidates) {
                if (placedLimbs >= targetLimbs) {
                    break;
                }
                BlockPos limbPos = cand.trunkPos().relative(cand.outwardFace());
                if (Math.abs(limbPos.getX() - origin.getX()) > 7
                        || Math.abs(limbPos.getZ() - origin.getZ()) > 7
                        || limbPos.getY() < MIN_TRUNK_Y
                        || limbPos.getY() > MAX_TRUNK_Y + 1) {
                    continue;
                }

                BlockState existing = level.getBlockState(limbPos);
                if (!existing.isAir() && !existing.is(Blocks.SNOW) && !existing.is(Blocks.WATER)) {
                    continue;
                }

                boolean waterlogged = existing.getFluidState().getType() == Fluids.WATER;
                BlockState limbState = branchBlock.defaultBlockState()
                        .trySetValue(BlockStateProperties.FACING, cand.outwardFace())
                        .trySetValue(BlockStateProperties.WATERLOGGED, waterlogged);
                if (limbState.canSurvive(level, limbPos)) {
                    level.setBlock(limbPos, limbState, 2);
                    placedLimbs++;
                }
            }
        }

        return true;
    }

    private static BlockState chooseTrunkWood(RandomSource random, Direction.Axis axis) {
        Block wood = random.nextFloat() < 0.60F ? Blocks.SPRUCE_LOG : Blocks.STRIPPED_SPRUCE_WOOD;
        return wood.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    private static BlockPos findFloeOrShallowAnchor(WorldGenLevel level, BlockPos origin, RandomSource random,
                                                    Block blightedIce, Block rottenIce) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int r = 0; r <= 4; r++) {
            int attempts = r == 0 ? 1 : 6;
            for (int a = 0; a < attempts; a++) {
                int ox = r == 0 ? 0 : (random.nextInt(r * 2 + 1) - r);
                int oz = r == 0 ? 0 : (random.nextInt(r * 2 + 1) - r);
                int tx = origin.getX() + ox;
                int tz = origin.getZ() + oz;

                // Prefer embedding directly into an existing Y=62..63 ice floe or ice-adjacent swamp water
                for (int ty = 63; ty >= 62; ty--) {
                    cursor.set(tx, ty, tz);
                    BlockState state = level.getBlockState(cursor);
                    BlockState above = level.getBlockState(cursor.above());
                    if (!above.isAir() && !above.is(Blocks.SNOW)) {
                        continue;
                    }
                    if (isFloeIce(state, blightedIce, rottenIce)) {
                        return new BlockPos(tx, 62, tz);
                    }
                    if (ty == 62 && state.is(Blocks.WATER)) {
                        for (Direction h : Direction.Plane.HORIZONTAL) {
                            BlockState adj63 = level.getBlockState(new BlockPos(tx + h.getStepX(), 63, tz + h.getStepZ()));
                            BlockState adj62 = level.getBlockState(new BlockPos(tx + h.getStepX(), 62, tz + h.getStepZ()));
                            if (isFloeIce(adj63, blightedIce, rottenIce) || isFloeIce(adj62, blightedIce, rottenIce)) {
                                return new BlockPos(tx, 62, tz);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private static boolean isFloeIce(BlockState state, Block blightedIce, Block rottenIce) {
        return state.is(blightedIce)
                || state.is(rottenIce)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.ICE)
                || state.is(Blocks.BLUE_ICE);
    }

    private static boolean canTrunkReplace(BlockState state, Block blightedIce, Block rottenIce) {
        return state.isAir()
                || state.is(Blocks.SNOW)
                || state.is(Blocks.POWDER_SNOW)
                || state.is(Blocks.WATER)
                || state.is(Blocks.ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(blightedIce)
                || state.is(rottenIce)
                || state.is(Blocks.MUD)
                || state.is(Blocks.CLAY)
                || state.is(BlockTags.REPLACEABLE);
    }
}
