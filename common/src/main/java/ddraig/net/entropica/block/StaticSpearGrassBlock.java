package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;

public class StaticSpearGrassBlock extends BushBlock {
        public StaticSpearGrassBlock(Properties properties) {
        super(properties);
    }
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, net.minecraft.world.entity.InsideBlockEffectApplier effectApplier, boolean isInside) {
        super.entityInside(state, level, pos, entity, effectApplier, isInside);
        if (!level.isClientSide() && entity instanceof LivingEntity living) {
            living.hurt(level.damageSources().lightningBolt(), 1.0f);
            living.addEffect(new MobEffectInstance(MobEffects.SPEED, 60, 1, false, true, true));
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(3) == 0) {
            double x = pos.getX() + 0.2 + random.nextDouble() * 0.6;
            double y = pos.getY() + 0.2 + random.nextDouble() * 0.6;
            double z = pos.getZ() + 0.2 + random.nextDouble() * 0.6;
            level.addParticle(new TimedTintableParticleOption(ModParticles.TINTABLE_ARC.get(), 0.22f, 0.74f, 0.97f, 12, 1.0f, (float)(random.nextDouble()*6.28), 0.2f, TimedTintableParticleOption.MODE_JITTER, 0.04f, 0f, 0f), x, y, z, 0.0, 0.0, 0.0);
        }
    }
}
