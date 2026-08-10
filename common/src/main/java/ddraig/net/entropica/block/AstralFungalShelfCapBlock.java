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

public class AstralFungalShelfCapBlock extends AbstractFungalShelfBlock {
    public static final MapCodec<AstralFungalShelfCapBlock> CODEC = simpleCodec(AstralFungalShelfCapBlock::new);

    public AstralFungalShelfCapBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<AstralFungalShelfCapBlock> codec() {
        return CODEC;
    }

    @Override
    protected void applyTouchEffect(Level level, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 80, 1, false, true, true));
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(2) == 0) {
            double x = pos.getX() + 0.2 + random.nextDouble() * 0.6;
            double y = pos.getY() + 0.4 + random.nextDouble() * 0.2;
            double z = pos.getZ() + 0.2 + random.nextDouble() * 0.6;
            level.addParticle(new TimedTintableParticleOption(ModParticles.TINTABLE_SPARKLE.get(), 0.39f, 0.4f, 0.95f, 40, 1.1f, (float)(random.nextDouble()*6.28), 0.05f, TimedTintableParticleOption.MODE_NOISE, 0f, 0f, 0f), x, y, z, 0.0, 0.03, 0.0);
        }
    }
}
