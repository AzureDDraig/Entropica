package ddraig.net.entropica.block;

import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class AuroralButtercupBlock extends EntropicaFlowerBlock {
    public AuroralButtercupBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(2) == 0) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
            double y = pos.getY() + 0.4 + (random.nextDouble() - 0.5) * 0.3;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5;

            // Pick color-shifting auroral RGB: pastel violet, pink, or gold
            int choice = random.nextInt(3);
            float r, g, b;
            if (choice == 0) {
                // Auroral Violet (#E879F9)
                r = 0.91f; g = 0.47f; b = 0.97f;
            } else if (choice == 1) {
                // Auroral Pink (#FCE7F3)
                r = 0.99f; g = 0.90f; b = 0.95f;
            } else {
                // Auroral Gold (#F59E0B)
                r = 0.96f; g = 0.62f; b = 0.04f;
            }

            level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), x, y, z, r, g, b);
        }
    }
}
