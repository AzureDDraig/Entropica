package ddraig.net.entropica.block;

import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class GaleBloomDandelionBlock extends EntropicaFlowerBlock {
    public GaleBloomDandelionBlock(Properties properties) {
        super(properties);
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
        if (!level.isClientSide()) {
            level.playSound(null, pos, SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.BLOCKS, 0.5f, 1.4f);
        } else {

            // Burst of swirling wind puff particles with Arid Essence cream/gold tinting
            RandomSource random = level.getRandom();
            for (int i = 0; i < 8; i++) {
                double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.6;
                double y = pos.getY() + 0.6 + (random.nextDouble() - 0.5) * 0.4;
                double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.6;

                // Arid Essence Tint: warm cream (#FDE68A) or soft ivory gold (#FEF3C7)
                float r = 0.99f;
                float g = random.nextBoolean() ? 0.90f : 0.95f;
                float b = random.nextBoolean() ? 0.54f : 0.78f;

                level.addParticle(ModParticles.GALE_SWIRL_PUFF.get(), x, y, z, r, g, b);
            }
        }
        return InteractionResult.SUCCESS;

    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.Entity entity, net.minecraft.world.entity.InsideBlockEffectApplier effectApplier, boolean isInside) {
        super.entityInside(state, level, pos, entity, effectApplier, isInside);
        if (entity instanceof net.minecraft.world.entity.LivingEntity living) {
            net.minecraft.world.phys.Vec3 delta = living.getDeltaMovement();
            if (delta.y < 0.25) {
                living.setDeltaMovement(delta.x, 0.25, delta.z);
                living.hasImpulse = true;
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(2) == 0) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
            double y = pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5;

            // Arid Essence warm cream tint
            float r = 0.99f;
            float g = random.nextBoolean() ? 0.90f : 0.95f;
            float b = random.nextBoolean() ? 0.54f : 0.78f;

            level.addParticle(ModParticles.GALE_SWIRL_PUFF.get(), x, y, z, r, g, b);
        }
    }
}
