package ddraig.net.entropica.block;

import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class SpectralLanternFlowerBlock extends EntropicaFlowerBlock {
    public SpectralLanternFlowerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL) || state.is(Blocks.WARPED_NYLIUM)
            || state.is(Blocks.CRIMSON_NYLIUM) || state.is(Blocks.SCULK) || state.is(Blocks.END_STONE)
            || state.is(Blocks.MOSS_BLOCK) || super.mayPlaceOn(state, level, pos);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(2) == 0) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
            double y = pos.getY() + 0.3 + random.nextDouble() * 0.4;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
            level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), x, y, z, 0.23f, 0.51f, 0.96f);
        }

        if (!level.isClientSide() && random.nextInt(10) == 0) {
            AABB area = new AABB(pos).inflate(6.0);
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area)) {
                if (entity instanceof Enemy || entity.isInvertedHealAndHarm()) {
                    entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, false, true, true));
                    entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, false, true, true));
                }
            }
        }
    }
}
