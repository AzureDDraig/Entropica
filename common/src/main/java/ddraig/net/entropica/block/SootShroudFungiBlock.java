package ddraig.net.entropica.block;

import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SootShroudFungiBlock extends EntropicaFlowerBlock {

    public SootShroudFungiBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.NETHERRACK) || state.is(Blocks.SOUL_SOIL) || state.is(Blocks.SOUL_SAND)
            || state.is(Blocks.BASALT) || state.is(Blocks.POLISHED_BASALT) || state.is(Blocks.CRIMSON_NYLIUM)
            || state.is(Blocks.WARPED_NYLIUM) || state.is(Blocks.MYCELIUM) || state.is(Blocks.PODZOL)
            || state.is(BlockTags.LOGS) || super.mayPlaceOn(state, level, pos);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, net.minecraft.world.entity.InsideBlockEffectApplier effectApplier, boolean isInside) {
        super.entityInside(state, level, pos, entity, effectApplier, isInside);
        if (entity instanceof LivingEntity livingEntity && !level.isClientSide()) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 80, 0, false, true, true));
            livingEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0, false, true, true));
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(2) == 0) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
            double y = pos.getY() + 0.3 + random.nextDouble() * 0.4;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
            level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), x, y, z, 0.98f, 0.80f, 0.08f);
        }
    }
}
