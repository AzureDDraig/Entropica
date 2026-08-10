package ddraig.net.entropica.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SanguineLilyBlock extends WaterlilyBlock implements BonemealableBlock {
    public SanguineLilyBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (!level.isClientSide() && entity instanceof LivingEntity living) {
            if (living.getHealth() < living.getMaxHealth()) {
                living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, false, true));
            }
        }
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
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(2) == 0) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.6;
            double y = pos.getY() + 0.15 + random.nextDouble() * 0.2;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.6;

            level.addParticle(ParticleTypes.CRIMSON_SPORE, x, y, z, 0.0, 0.02, 0.0);
            if (random.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.DAMAGE_INDICATOR, x, y + 0.1, z, 0.0, 0.03, 0.0);
            }
        }
    }
}
