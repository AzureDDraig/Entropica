package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BarrowFungalShelfCapBlock extends AbstractFungalShelfBlock {
    public static final MapCodec<BarrowFungalShelfCapBlock> CODEC = simpleCodec(BarrowFungalShelfCapBlock::new);

    public BarrowFungalShelfCapBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BarrowFungalShelfCapBlock> codec() {
        return CODEC;
    }

    @Override
    protected void applyTouchEffect(Level level, BlockPos pos, Entity entity) {

        if (entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 80, 0, false, true, true));
        }
    
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(3) == 0) {
            double x = pos.getX() + 0.2 + random.nextDouble() * 0.6;
            double y = pos.getY() + 0.4 + random.nextDouble() * 0.2;
            double z = pos.getZ() + 0.2 + random.nextDouble() * 0.6;
            level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), x, y, z, 0.0, 0.02, 0.0);
        }
    }
}
