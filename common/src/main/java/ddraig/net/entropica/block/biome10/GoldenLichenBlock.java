package ddraig.net.entropica.block.biome10;

import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.GlowLichenBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Golden Lichen — warm, sun-fed multi-face golden lichen clinging to travertine spring basins
 * and amber trunks in 2.10 Sun-Glazed Sanctuary.
 */
public class GoldenLichenBlock extends GlowLichenBlock {

    public GoldenLichenBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (entity instanceof LivingEntity living && living.getTicksFrozen() > 0) {
            living.setTicksFrozen(0);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(3) == 0 && ModParticles.SPECTRUM_SPARKLE.isBound()) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), x, y, z, 1.0f, 0.82f, 0.22f);
        }
    }
}
