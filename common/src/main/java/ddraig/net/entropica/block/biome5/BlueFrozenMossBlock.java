package ddraig.net.entropica.block.biome5;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Blue Frozen Moss — cushioning, faintly bioluminescent pale-blue moss native to 2.5 Silent Fjord.
 * Reduces fall damage by 40% and spreads across adjacent volcanic basalt and stone when fertilized.
 */
public class BlueFrozenMossBlock extends Block implements BonemealableBlock {
    public static final MapCodec<BlueFrozenMossBlock> CODEC = simpleCodec(BlueFrozenMossBlock::new);

    public BlueFrozenMossBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        entity.causeFallDamage(fallDistance, 0.6F, level.damageSources().fall());
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return level.getBlockState(pos.above()).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public Type getType() {
        return Type.NEIGHBOR_SPREADER;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        boolean mossBound = ModBlocks.BLUE_FROZEN_MOSS.isBound();
        boolean carpetBound = ModBlocks.BLUE_FROZEN_MOSS_CARPET.isBound();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos targetPos = pos.offset(dx, dy, dz);
                    BlockPos abovePos = targetPos.above();
                    if (!level.getBlockState(abovePos).isAir()) {
                        continue;
                    }

                    BlockState targetState = level.getBlockState(targetPos);
                    boolean converted = false;

                    if (!targetPos.equals(pos) && mossBound && isConvertibleSubstrate(targetState)) {
                        int manhattan = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
                        if (manhattan == 1 || random.nextFloat() < 0.75F) {
                            targetState = ModBlocks.BLUE_FROZEN_MOSS.get().defaultBlockState();
                            level.setBlock(targetPos, targetState, 3);
                            converted = true;
                        }
                    }

                    if (carpetBound && !targetPos.equals(pos)) {
                        boolean sturdyTop = converted
                                || targetState.isFaceSturdy(level, targetPos, Direction.UP)
                                || (mossBound && targetState.is(ModBlocks.BLUE_FROZEN_MOSS.get()));
                        if (sturdyTop && random.nextFloat() < 0.55F) {
                            BlockState carpetState = ModBlocks.BLUE_FROZEN_MOSS_CARPET.get().defaultBlockState();
                            if (carpetState.canSurvive(level, abovePos)) {
                                level.setBlock(abovePos, carpetState, 3);
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isConvertibleSubstrate(BlockState state) {
        return state.is(Blocks.BASALT)
                || state.is(Blocks.SMOOTH_BASALT)
                || state.is(Blocks.POLISHED_BASALT)
                || (ModBlocks.BASALT_COLUMN.isBound() && state.is(ModBlocks.BASALT_COLUMN.get()))
                || state.is(Blocks.STONE)
                || state.is(Blocks.DEEPSLATE)
                || state.is(Blocks.COBBLESTONE)
                || state.is(Blocks.GRAVEL);
    }
}
