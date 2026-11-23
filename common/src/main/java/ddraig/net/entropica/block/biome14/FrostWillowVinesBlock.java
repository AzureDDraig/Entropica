package ddraig.net.entropica.block.biome14;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Frost-Willow Vines — full-face weeping willow curtains draped from Frost-Willow canopies in 2.14 Oasis Floes.
 * Extends vanilla VineBlock so all five attachment faces (north, south, east, west, up) connect seamlessly.
 */
public class FrostWillowVinesBlock extends VineBlock {

    public FrostWillowVinesBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(24) != 0) {
            return;
        }

        double x = pos.getX() + 0.15D + random.nextDouble() * 0.7D;
        double y = pos.getY() + 0.15D + random.nextDouble() * 0.7D;
        double z = pos.getZ() + 0.15D + random.nextDouble() * 0.7D;
        if (random.nextBoolean()) {
            level.addParticle(ParticleTypes.SNOWFLAKE, x, y, z, 0.0D, -0.015D, 0.0D);
        } else {
            level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, -0.01D, 0.0D);
        }
    }
}
