package ddraig.net.entropica.block.biome7;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Fulgurite Pine Leaves — frost-rimmed electric conifer needles of 2.7 Static Ice-Plains.
 * Softly illuminates canopies (Light Level 1) and sheds occasional static sparks.
 */
public class FulguritePineLeavesBlock extends Block {
    public static final MapCodec<FulguritePineLeavesBlock> CODEC = simpleCodec(FulguritePineLeavesBlock::new);

    public FulguritePineLeavesBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(10) != 0) {
            return;
        }
        BlockPos below = pos.below();
        if (level.getBlockState(below).isCollisionShapeFullBlock(level, below)) {
            return;
        }
        double x = pos.getX() + random.nextDouble();
        double y = pos.getY() - 0.05D;
        double z = pos.getZ() + random.nextDouble();

        if (random.nextBoolean() || !ModParticles.SPECTRUM_SPARKLE.isBound()) {
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z, 0.0D, -0.02D, 0.0D);
        } else {
            level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), x, y, z, 0.13F, 0.83F, 0.93F);
        }
    }
}
