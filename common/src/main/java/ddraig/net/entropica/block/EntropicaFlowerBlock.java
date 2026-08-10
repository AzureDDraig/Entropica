package ddraig.net.entropica.block;

import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;

public class EntropicaFlowerBlock extends BushBlock implements BonemealableBlock {
    public EntropicaFlowerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.CLAY) || state.is(Blocks.MUD) || state.is(Blocks.MUDDY_MANGROVE_ROOTS);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        // If this 1-tall flower has a 2-tall variant and space above is clear, grow it into the 2-tall variant!
        if (this == ModBlocks.AEGIS_SPIRE_ORCHID.get()) {
            BlockPos posAbove = pos.above();
            if (level.getBlockState(posAbove).canBeReplaced()) {
                DoublePlantBlock.placeAt(level, ModBlocks.TALL_AEGIS_SPIRE_ORCHID.get().defaultBlockState(), pos, 3);
                return;
            }
        }

        // Default behavior for flowers/mushrooms without a 2-tall variant or blocked space above: drop 1x item in world!
        popResource(level, pos, new ItemStack(this.asItem()));
    }

    @Override
    protected net.minecraft.world.InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.BlockHitResult hitResult) {
        if (ddraig.net.entropica.util.FloraHarvestHelper.tryShearHarvest(level, pos, state, player, hand)) {
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}

