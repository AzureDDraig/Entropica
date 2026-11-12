package ddraig.net.entropica.block;

import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class FulguriteReedBlock extends SugarCaneBlock implements BonemealableBlock {

    public FulguriteReedBlock(Properties properties) {
        super(properties);
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
        int height = 1;
        BlockPos currentPos = pos.below();
        while (level.getBlockState(currentPos).is(this)) {
            height++;
            currentPos = currentPos.below();
        }
        currentPos = pos.above();
        while (level.getBlockState(currentPos).is(this)) {
            height++;
            currentPos = currentPos.above();
        }

        if (height < 3 && level.getBlockState(currentPos).canBeReplaced()) {
            level.setBlock(currentPos, this.defaultBlockState(), 3);
        } else {
            // Overcharge Electric Shock at Max Height / Blocked Above!
            level.playSound(null, pos, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.BLOCKS, 0.8f, 1.8f);
            level.playSound(null, pos, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.BLOCKS, 1.0f, 1.4f);

            // Dense Cyan Electric Sparkle Burst Particles
            for (int i = 0; i < 20; i++) {
                double px = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 1.5;
                double py = pos.getY() + 0.5 + random.nextDouble() * 2.0;
                double pz = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 1.5;
                level.sendParticles(ModParticles.SPECTRUM_SPARKLE.get(), px, py, pz, 1, 0.02, 0.71, 0.83, 0.1);
            }

            // Shock all living entities within 4 blocks and apply Paralyzed for 3 seconds (60 ticks)!
            AABB shockArea = new AABB(pos).inflate(4.0);
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, shockArea)) {
                entity.addEffect(new MobEffectInstance(ModEffects.PARALYZED, 60, 0, false, true, true));
                entity.hurt(level.damageSources().lightningBolt(), 3.0f);
            }
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isInside) {
        super.entityInside(state, level, pos, entity, effectApplier, isInside);
        
        if (entity instanceof LivingEntity livingEntity && !level.isClientSide()) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.SPEED, 60, 0, false, false, true));
            if (level.getRandom().nextInt(10) == 0) {
                level.playSound(null, pos, SoundEvents.BEE_STING, SoundSource.BLOCKS, 0.3f, 1.6f);
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(3) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();

            float r = 0.02f;
            float g = 0.71f;
            float b = 0.83f;

            level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), x, y, z, r, g, b);
        }
    }
}
