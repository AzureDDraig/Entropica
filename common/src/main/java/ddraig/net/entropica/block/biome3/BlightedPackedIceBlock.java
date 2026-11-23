package ddraig.net.entropica.block.biome3;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Blighted Packed Ice — dense olive-teal ice floe foundation saturated with Blight and Glacial Materia.
 * Drips dark necrotic particulates into submerged water channels beneath the floe.
 */
public class BlightedPackedIceBlock extends Block {
    public static final MapCodec<BlightedPackedIceBlock> CODEC = simpleCodec(BlightedPackedIceBlock::new);

    public BlightedPackedIceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        BlockPos belowPos = pos.below();
        if (level.getFluidState(belowPos).is(FluidTags.WATER) && random.nextInt(3) == 0) {
            double x = belowPos.getX() + 0.15D + random.nextDouble() * 0.7D;
            double y = belowPos.getY() + 0.55D + random.nextDouble() * 0.4D;
            double z = belowPos.getZ() + 0.15D + random.nextDouble() * 0.7D;
            int choice = random.nextInt(3);
            if (choice == 0) {
                level.addParticle(ParticleTypes.SQUID_INK, x, y, z, 0.0D, -0.02D, 0.0D);
            } else if (choice == 1) {
                level.addParticle(ParticleTypes.ITEM_SLIME, x, y, z, 0.0D, -0.01D, 0.0D);
            } else {
                level.addParticle(ParticleTypes.ASH, x, y, z, 0.0D, -0.015D, 0.0D);
            }
        }
    }
}
