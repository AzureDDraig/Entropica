package ddraig.net.entropica.block;

import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BloodRootSucculentBlock extends EntropicaFlowerBlock {

    public BloodRootSucculentBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return super.mayPlaceOn(state, level, pos) || 
               state.is(Blocks.RED_SAND) || 
               state.is(Blocks.TERRACOTTA) || 
               state.is(Blocks.NETHERRACK) || 
               state.is(Blocks.CRIMSON_NYLIUM) || 
               state.is(Blocks.SAND);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        
        if (entity instanceof LivingEntity livingEntity && !level.isClientSide()) {
            if (!livingEntity.isInvertedHealAndHarm()) {
                // Stepping on succulent inflicts 1 life-drain damage to living entities
                livingEntity.hurt(level.damageSources().magic(), 1.0F);
            } else {
                // Undead mobs are nourished by the blood sap, receiving Regeneration I for 3 seconds
                livingEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, false, false, true));
            }
        }
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            // Harvest Blood-Root Pulp
            popResource(level, pos, new ItemStack(ModItems.BLOOD_ROOT_PULP.get()));
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        return InteractionResult.SUCCESS;

    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(3) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble() * 0.4;
            double z = pos.getZ() + random.nextDouble();

            // Bioluminescent Crimson Life Sparkles (#DC2626)
            float r = 0.86f;
            float g = 0.15f;
            float b = 0.15f;

            level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), x, y, z, r, g, b);
        }
    }
}
