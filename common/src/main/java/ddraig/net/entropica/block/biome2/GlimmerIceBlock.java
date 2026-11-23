package ddraig.net.entropica.block.biome2;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Glimmer-Ice — translucent shoreline ice of the Auroral Skerries infused with Aurora Materia.
 * Shimmers with gold, pink, and emerald aurora bands.
 */
public class GlimmerIceBlock extends HalfTransparentBlock {
    public static final MapCodec<GlimmerIceBlock> CODEC = simpleCodec(GlimmerIceBlock::new);

    private static final int[] AURORA_COLORS = new int[]{
            0xFDE047, // Shimmering gold
            0xF472B6, // Auroral pink
            0x34D399  // Emerald green
    };

    public GlimmerIceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends HalfTransparentBlock> codec() {
        return CODEC;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(6) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 1.02D;
            double z = pos.getZ() + random.nextDouble();
            if (random.nextInt(4) == 0) {
                level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, 0.012D, 0.0D);
            } else {
                int color = AURORA_COLORS[random.nextInt(AURORA_COLORS.length)];
                level.addParticle(new DustParticleOptions(color, 0.75F), x, y, z, 0.0D, 0.01D, 0.0D);
            }
        }
    }
}
