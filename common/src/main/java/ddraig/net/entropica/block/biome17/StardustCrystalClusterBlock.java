package ddraig.net.entropica.block.biome17;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * Stardust Crystal Cluster — 6-way directional Astral Materia spire cluster growing across
 * Starlight Ice shelves, submerged grottoes, and meteorite impactite in 2.17 Astral Skerries.
 * Emits Light 12 and shatters into Stardust Fragments unless harvested with Silk Touch
 * or a sneaking Starlight Tuning Chisel.
 */
public class StardustCrystalClusterBlock extends AmethystClusterBlock implements SimpleWaterloggedBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public StardustCrystalClusterBlock(Properties properties) {
        this(7.0F, 3.0F, properties);
    }

    public StardustCrystalClusterBlock(float height, float aabbOffset, Properties properties) {
        super(height, aabbOffset, properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(FACING, Direction.UP)
                .setValue(WATERLOGGED, false));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(9) == 0) {
            double x = pos.getX() + 0.25D + random.nextDouble() * 0.5D;
            double y = pos.getY() + 0.25D + random.nextDouble() * 0.5D;
            double z = pos.getZ() + 0.25D + random.nextDouble() * 0.5D;
            level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, 0.01D, 0.0D);
        }
    }
}
