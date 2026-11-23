package ddraig.net.entropica.block.biome10;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Glazed Amber Leaves — frosted golden-amber canopy leaves of 2.10 Sun-Glazed Sanctuary.
 * Softly illuminates boughs (Light Level 3) and gently sheds luminous motes beneath the canopy.
 */
public class GlazedAmberLeavesBlock extends TintedParticleLeavesBlock {
    public static final MapCodec<GlazedAmberLeavesBlock> CODEC = simpleCodec(GlazedAmberLeavesBlock::new);
    private static final int GOLDEN_AMBER_LEAF_TINT = 0xFFF59E0B;

    public GlazedAmberLeavesBlock(Properties properties) {
        super(0.01F, properties);
    }

    @Override
    public MapCodec<? extends TintedParticleLeavesBlock> codec() {
        return CODEC;
    }

    @Override
    protected void spawnFallingLeavesParticle(Level level, BlockPos pos, RandomSource random) {
        ColorParticleOption particle = ColorParticleOption.create(ParticleTypes.TINTED_LEAVES, GOLDEN_AMBER_LEAF_TINT);
        ParticleUtils.spawnParticleBelow(level, pos, random, particle);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(12) != 0) {
            return;
        }
        BlockPos below = pos.below();
        if (level.getBlockState(below).isCollisionShapeFullBlock(level, below)) {
            return;
        }

        double x = pos.getX() + random.nextDouble();
        double y = pos.getY() - 0.05D;
        double z = pos.getZ() + random.nextDouble();
        level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, -0.02D, 0.0D);
    }
}
