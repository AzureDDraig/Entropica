package ddraig.net.entropica.block;

import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class StardustBellBlock extends TallFlowerBlock {
    public StardustBellBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected net.minecraft.world.InteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.BlockHitResult hitResult) {
        if (ddraig.net.entropica.util.FloraHarvestHelper.tryShearHarvest(level, pos, state, player, hand)) {
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }


    @Override

    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.CLAY) || state.is(Blocks.MUD) || state.is(Blocks.MUDDY_MANGROVE_ROOTS);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        // Only spawn stardust particles from the upper half of the 2-tall flower
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER && random.nextInt(2) == 0) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.6;
            double y = pos.getY() + 0.4 + (random.nextDouble() - 0.5) * 0.4;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.6;

            int choice = random.nextInt(3);
            float r, g, b;
            if (choice == 0) {
                // Celestial Indigo/Blue (#818CF8)
                r = 0.51f; g = 0.55f; b = 0.97f;
            } else if (choice == 1) {
                // Stardust Gold (#FDE047)
                r = 0.99f; g = 0.88f; b = 0.28f;
            } else {
                // Pure Stardust White (#FFFFFF)
                r = 1.00f; g = 1.00f; b = 1.00f;
            }

            level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), x, y, z, r, g, b);
        }
    }
}
