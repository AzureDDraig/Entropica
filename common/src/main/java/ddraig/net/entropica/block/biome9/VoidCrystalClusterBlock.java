package ddraig.net.entropica.block.biome9;

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
 * Void Crystal Bud & Cluster — directional spatial crystal formations growing along
 * floating islets, underwater void rifts, and Void Crystal geodes in Null-Fringe Bay.
 */
public class VoidCrystalClusterBlock extends AmethystClusterBlock {
    public static final EnumProperty<Direction> FACING = AmethystClusterBlock.FACING;
    public static final BooleanProperty WATERLOGGED = AmethystClusterBlock.WATERLOGGED;

    public VoidCrystalClusterBlock(float height, float aabbOffset, Properties properties) {
        super(height, aabbOffset, properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(5) == 0) {
            double x = pos.getX() + 0.25D + random.nextDouble() * 0.5D;
            double y = pos.getY() + 0.25D + random.nextDouble() * 0.5D;
            double z = pos.getZ() + 0.25D + random.nextDouble() * 0.5D;
            if (random.nextBoolean()) {
                level.addParticle(ParticleTypes.REVERSE_PORTAL, x, y, z, 0.0D, 0.015D, 0.0D);
            } else {
                level.addParticle(ParticleTypes.PORTAL, x, y, z,
                        (random.nextDouble() - 0.5D) * 0.15D,
                        (random.nextDouble() - 0.5D) * 0.15D,
                        (random.nextDouble() - 0.5D) * 0.15D);
            }
        }
    }
}
