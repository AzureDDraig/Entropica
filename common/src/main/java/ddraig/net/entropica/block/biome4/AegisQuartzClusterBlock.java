package ddraig.net.entropica.block.biome4;

import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * Aegis Quartz Cluster — directional crystal cluster growing along pack-ice rifts
 * and Monolith vaults in Crystalline Pack-Ice (Biome 4).
 */
public class AegisQuartzClusterBlock extends AmethystClusterBlock {
    public static final EnumProperty<Direction> FACING = AmethystClusterBlock.FACING;
    public static final BooleanProperty WATERLOGGED = AmethystClusterBlock.WATERLOGGED;

    public AegisQuartzClusterBlock(Properties properties) {
        super(7.0F, 3.0F, properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(5) == 0) {
            double x = pos.getX() + 0.25D + random.nextDouble() * 0.5D;
            double y = pos.getY() + 0.25D + random.nextDouble() * 0.5D;
            double z = pos.getZ() + 0.25D + random.nextDouble() * 0.5D;
            if (random.nextBoolean() && ModParticles.SPECTRUM_SPARKLE.isBound()) {
                level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), x, y, z, 0.28F, 0.72F, 0.98F);
            } else {
                level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, 0.01D, 0.0D);
            }
        }
    }
}
