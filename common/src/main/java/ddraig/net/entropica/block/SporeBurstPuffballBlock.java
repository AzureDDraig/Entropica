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
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class SporeBurstPuffballBlock extends EntropicaFlowerBlock {

    public SporeBurstPuffballBlock(Properties properties) {
        super(properties);
    }

    private void triggerSporeCloud(Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide()) {
            level.playSound(null, pos, SoundEvents.SLIME_SQUISH, SoundSource.BLOCKS, 1.0f, 0.8f);
            
            // Nausea I & Poison I spore cloud on target entity
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 120, 0, false, true, true));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 0, false, true, true));
            }
        } else {
            // Burst of emerald/yellow spore particles
            RandomSource random = level.getRandom();
            for (int i = 0; i < 15; i++) {
                double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.8;
                double y = pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 0.6;
                double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.8;

                float r = 0.52f;
                float g = 0.80f;
                float b = 0.08f;

                level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), x, y, z, r, g, b);
            }
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isInside) {
        super.entityInside(state, level, pos, entity, effectApplier, isInside);
        if (entity instanceof LivingEntity) {
            triggerSporeCloud(level, pos, entity);
        }
    }

    private static final java.util.Map<BlockPos, Long> HARVEST_COOLDOWNS = new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    protected net.minecraft.world.InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hitResult) {
        if (ddraig.net.entropica.util.FloraHarvestHelper.tryShearHarvest(level, pos, state, player, hand)) {
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        long now = level.getGameTime();
        Long lastHarvest = HARVEST_COOLDOWNS.get(pos);
        if (lastHarvest != null && (now - lastHarvest) < 6000L) {
            return InteractionResult.PASS;
        }
        HARVEST_COOLDOWNS.put(pos, now);

        triggerSporeCloud(level, pos, player);
        if (!level.isClientSide()) {
            popResource(level, pos, new ItemStack(ModItems.SPORE_PUFF.get()));
        }
        return InteractionResult.SUCCESS;
    }


    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(4) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble() * 0.5;
            double z = pos.getZ() + random.nextDouble();

            float r = 0.06f;
            float g = 0.72f;
            float b = 0.50f;

            level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), x, y, z, r, g, b);
        }
    }
}
