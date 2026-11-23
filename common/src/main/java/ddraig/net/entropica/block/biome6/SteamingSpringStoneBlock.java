package ddraig.net.entropica.block.biome6;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Steaming Spring-Stone — warm geothermal basalt lining Skerries hot pools.
 * Emits gentle rising steam and clears frost buildup on creatures walking across it.
 */
public class SteamingSpringStoneBlock extends Block {
    public static final MapCodec<SteamingSpringStoneBlock> CODEC = simpleCodec(SteamingSpringStoneBlock::new);

    public SteamingSpringStoneBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (entity.getTicksFrozen() > 0) {
            entity.setTicksFrozen(0);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(4) != 0) {
            return;
        }
        BlockPos above = pos.above();
        if (level.getBlockState(above).isCollisionShapeFullBlock(level, above)) {
            return;
        }

        double x = pos.getX() + 0.2D + random.nextDouble() * 0.6D;
        double y = pos.getY() + 1.04D;
        double z = pos.getZ() + 0.2D + random.nextDouble() * 0.6D;

        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, 0.0D, 0.04D, 0.0D);
        } else {
            level.addParticle(ParticleTypes.CLOUD, x, y, z, 0.0D, 0.04D, 0.0D);
        }
    }
}
