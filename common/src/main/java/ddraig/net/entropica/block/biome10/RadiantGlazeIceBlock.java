package ddraig.net.entropica.block.biome10;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Radiant Glaze Ice — sun-glazed amber-blue ice infused with Radiant and Vitae Materia.
 * Never melts into water under sunlight and clears frost buildup when stepped upon.
 */
public class RadiantGlazeIceBlock extends HalfTransparentBlock {
    public static final MapCodec<RadiantGlazeIceBlock> CODEC = simpleCodec(RadiantGlazeIceBlock::new);

    public RadiantGlazeIceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends HalfTransparentBlock> codec() {
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
        if (random.nextInt(12) != 0) {
            return;
        }
        BlockPos above = pos.above();
        if (level.getBlockState(above).isCollisionShapeFullBlock(level, above)) {
            return;
        }

        double x = pos.getX() + 0.15D + random.nextDouble() * 0.7D;
        double y = pos.getY() + 1.03D;
        double z = pos.getZ() + 0.15D + random.nextDouble() * 0.7D;

        if (ModParticles.SPECTRUM_SPARKLE.isBound() && random.nextBoolean()) {
            level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), x, y, z, 1.0F, 0.84F, 0.36F);
        } else {
            level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, 0.01D, 0.0D);
        }
    }
}
