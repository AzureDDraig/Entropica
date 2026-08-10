package ddraig.net.entropica.block;

import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class GaleThistleBlock extends EntropicaFlowerBlock {

    public GaleThistleBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isInside) {
        super.entityInside(state, level, pos, entity, effectApplier, isInside);
        
        // Metallic Thorn Prick Damage
        entity.hurt(level.damageSources().cactus(), 1.0F);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(3) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble() * 0.6;
            double z = pos.getZ() + random.nextDouble();

            // Swirling Wind Puff Particle
            float r = 0.99f;
            float g = 0.90f;
            float b = 0.54f;

            level.addParticle(ModParticles.GALE_SWIRL_PUFF.get(), x, y, z, r, g, b);
        }
    }
}
