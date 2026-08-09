package ddraig.net.entropica.block;

import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BarrowMossCarpetBlock extends EntropicaFlowerBlock {
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);

    public BarrowMossCarpetBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        
        if (entity instanceof LivingEntity livingEntity && !level.isClientSide) {
            if (livingEntity.isInvertedHealAndHarm()) {
                // Undead Mob Buff: Strength I & Speed I for 6 seconds
                livingEntity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 120, 0, false, true, true));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 120, 0, false, true, true));
            } else {
                // Living Non-Undead Entity: 1 Damage Trade-Off + Debuff Cleansing + 15s Regen Cooldown
                boolean hasDebuff = livingEntity.hasEffect(MobEffects.WITHER) || 
                                    livingEntity.hasEffect(MobEffects.POISON) || 
                                    livingEntity.hasEffect(MobEffects.WEAKNESS);

                if (hasDebuff) {
                    // 1. Deal 1 damage first as trade-off for cleansing
                    livingEntity.hurt(level.damageSources().magic(), 1.0F);

                    // 2. Remove debuffs
                    livingEntity.removeEffect(MobEffects.WITHER);
                    livingEntity.removeEffect(MobEffects.POISON);
                    livingEntity.removeEffect(MobEffects.WEAKNESS);
                }

                // 3. Apply Regeneration I if 15-second cooldown has passed
                BarrowMossBlock.tryApplyRegeneration(livingEntity, level);
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(4) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 0.15;
            double z = pos.getZ() + random.nextDouble();

            float r = 0.06f;
            float g = 0.72f;
            float b = 0.50f;

            level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), x, y, z, r, g, b);
        }
    }
}
