package ddraig.net.entropica.block.biome13;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ColorRGBA;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Glass Sand — solar-baked amber silica dunes infused with Mirage and Glacial Materia.
 * Sparkles subtly under bright daylight and smelts cleanly into glass.
 */
public class GlassSandBlock extends ColoredFallingBlock {
    public static final MapCodec<GlassSandBlock> CODEC = simpleCodec(GlassSandBlock::new);
    public static final ColorRGBA DUST_COLOR = new ColorRGBA(0xD26734);

    public GlassSandBlock(Properties properties) {
        super(DUST_COLOR, properties);
    }

    @Override
    public MapCodec<? extends ColoredFallingBlock> codec() {
        return CODEC;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(32) != 0) {
            return;
        }
        BlockPos above = pos.above();
        if (!level.getBlockState(above).isAir() || !level.isBrightOutside() || !level.canSeeSky(above)) {
            return;
        }

        double x = pos.getX() + 0.1D + random.nextDouble() * 0.8D;
        double y = pos.getY() + 1.03D;
        double z = pos.getZ() + 0.1D + random.nextDouble() * 0.8D;
        if (random.nextBoolean()) {
            level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, 0.008D, 0.0D);
        } else {
            level.addParticle(ParticleTypes.CRIT, x, y, z, 0.0D, 0.015D, 0.0D);
        }
    }
}
