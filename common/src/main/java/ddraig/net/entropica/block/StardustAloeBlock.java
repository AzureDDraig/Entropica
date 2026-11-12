package ddraig.net.entropica.block;

import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;

public class StardustAloeBlock extends EntropicaFlowerBlock {
    public StardustAloeBlock(Properties properties) {
        super(properties);
    }


    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.SAND)
            || state.is(Blocks.RED_SAND)
            || state.is(Blocks.TERRACOTTA)
            || state.is(Blocks.END_STONE)
            || state.is(Blocks.DIRT)
            || state.is(Blocks.GRASS_BLOCK)
            || state.is(Blocks.CLAY);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        popResource(level, pos, new ItemStack(this.asItem()));
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.Entity entity, net.minecraft.world.entity.InsideBlockEffectApplier effectApplier, boolean isInside) {
        super.entityInside(state, level, pos, entity, effectApplier, isInside);
        if (entity instanceof net.minecraft.world.entity.LivingEntity living) {
            living.clearFire();
            living.removeEffect(net.minecraft.world.effect.MobEffects.POISON);
            living.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 40, 0, false, true, true));
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(2) == 0) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
            double y = pos.getY() + 0.4 + (random.nextDouble() - 0.5) * 0.3;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5;

            // Teal (#2DD4BF) or Stardust Gold (#FDE047)
            float r = random.nextBoolean() ? 0.17f : 0.99f;
            float g = random.nextBoolean() ? 0.83f : 0.88f;
            float b = random.nextBoolean() ? 0.75f : 0.28f;

            level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), x, y, z, r, g, b);
        }
    }
}
