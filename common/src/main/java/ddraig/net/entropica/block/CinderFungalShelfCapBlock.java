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
import net.minecraft.world.level.block.state.BlockState;

public class CinderFungalShelfCapBlock extends AbstractFungalShelfBlock {
    public static final MapCodec<CinderFungalShelfCapBlock> CODEC = simpleCodec(CinderFungalShelfCapBlock::new);

    public CinderFungalShelfCapBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<CinderFungalShelfCapBlock> codec() {
        return CODEC;
    }

    @Override
    protected void applyTouchEffect(Level level, BlockPos pos, Entity entity) {
        entity.igniteForSeconds(3);
        if (entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 0, false, true, true));
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(2) == 0) {
            double x = pos.getX() + 0.2 + random.nextDouble() * 0.6;
            double y = pos.getY() + 0.4 + random.nextDouble() * 0.2;
            double z = pos.getZ() + 0.2 + random.nextDouble() * 0.6;
            level.addParticle(new TimedTintableParticleOption(ModParticles.TINTABLE_EMBER.get(), 0.95f, 0.4f, 0.05f, 30, 1.0f, 0f, 0.05f, TimedTintableParticleOption.MODE_JITTER, 0.03f, 0f, 0f), x, y, z, 0.0, 0.02, 0.0);
        }
    }
}
