package ddraig.net.entropica.block.biome9;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Void-Sand Turf — Materia-threaded dark sand turf along Null-Fringe Bay shores.
 * Bone-mealing spreads Null-Fringe Tussock, Void-Stalker Orchid, or Void Crystal Buds in a 3x3 patch.
 */
public class VoidSandTurfBlock extends Block implements BonemealableBlock {
    public static final MapCodec<VoidSandTurfBlock> CODEC = simpleCodec(VoidSandTurfBlock::new);

    public VoidSandTurfBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        BlockState above = level.getBlockState(pos.above());
        return above.isAir() || (above.is(Blocks.SNOW) && above.getValue(SnowLayerBlock.LAYERS) <= 1);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        List<BlockState> candidates = new ArrayList<>();
        if (ModBlocks.NULL_FRINGE_TUSSOCK.isBound()) {
            candidates.add(ModBlocks.NULL_FRINGE_TUSSOCK.get().defaultBlockState());
            candidates.add(ModBlocks.NULL_FRINGE_TUSSOCK.get().defaultBlockState());
        }
        if (ModBlocks.VOID_STALKER_ORCHID.isBound()) {
            candidates.add(ModBlocks.VOID_STALKER_ORCHID.get().defaultBlockState());
        }
        if (ModBlocks.VOID_CRYSTAL_BUD.isBound()) {
            candidates.add(ModBlocks.VOID_CRYSTAL_BUD.get().defaultBlockState()
                    .setValue(VoidCrystalClusterBlock.FACING, Direction.UP)
                    .setValue(VoidCrystalClusterBlock.WATERLOGGED, false));
        }
        if (candidates.isEmpty()) {
            return;
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                boolean isCenter = (dx == 0 && dz == 0);
                if (!isCenter && random.nextFloat() > 0.70F) {
                    continue;
                }
                for (int dy = 1; dy >= -1; dy--) {
                    BlockPos groundPos = pos.offset(dx, dy, dz);
                    BlockState groundState = level.getBlockState(groundPos);
                    boolean validSoil = groundState.is(this)
                            || (ModBlocks.VOID_SAND.isBound() && groundState.is(ModBlocks.VOID_SAND.get()));
                    if (!validSoil) {
                        continue;
                    }
                    BlockPos targetPos = groundPos.above();
                    BlockState existing = level.getBlockState(targetPos);
                    boolean replaceable = existing.isAir()
                            || (existing.is(Blocks.SNOW) && existing.getValue(SnowLayerBlock.LAYERS) <= 1);
                    if (!replaceable) {
                        break;
                    }
                    if (ModBlocks.VOID_SAND.isBound()
                            && groundState.is(ModBlocks.VOID_SAND.get())
                            && random.nextBoolean()) {
                        level.setBlock(groundPos, this.defaultBlockState(), 3);
                    }
                    BlockState chosen = candidates.get(random.nextInt(candidates.size()));
                    if (chosen.canSurvive(level, targetPos)) {
                        level.setBlock(targetPos, chosen, 3);
                    }
                    break;
                }
            }
        }
    }
}
