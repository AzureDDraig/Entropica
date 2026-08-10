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
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CryoStaticShrubBlock extends EntropicaFlowerBlock {
    protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    public CryoStaticShrubBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return super.mayPlaceOn(state, level, pos) 
            || state.is(Blocks.SNOW_BLOCK) 
            || state.is(Blocks.POWDER_SNOW) 
            || state.is(Blocks.ICE) 
            || state.is(Blocks.PACKED_ICE) 
            || state.is(Blocks.BLUE_ICE);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, net.minecraft.world.entity.InsideBlockEffectApplier effectApplier, boolean isInside) {
        super.entityInside(state, level, pos, entity, effectApplier, isInside);
        
        // 1. Powder Snow Freezing Mechanics
        entity.setTicksFrozen(Math.min(entity.getTicksRequiredToFreeze(), entity.getTicksFrozen() + 5));

        // 2. Static Slowness I Stun
        if (entity instanceof LivingEntity livingEntity && !level.isClientSide()) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, false, false, true));
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

        if (!level.isClientSide()) {
            popResource(level, pos, new ItemStack(ModItems.CRYO_STATIC_SHRUB_ITEM.get()));
            level.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 0.8f, 1.2f);
        } else {
            // Burst of Glacial Cyan and Plasma Violet static sparkles
            RandomSource random = level.getRandom();
            for (int i = 0; i < 10; i++) {
                double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.6;
                double y = pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
                double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.6;

                boolean isStatic = random.nextBoolean();
                float r = isStatic ? 0.66f : 0.01f;
                float g = isStatic ? 0.33f : 0.52f;
                float b = isStatic ? 0.97f : 0.78f;

                level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), x, y, z, r, g, b);
            }
        }
        return InteractionResult.SUCCESS;

    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(2) == 0) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.6;
            double y = pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.6;

            // 50/50 Dual Particle Emission: Cryo Glacial Blue OR Static Plasma Violet
            boolean isStatic = random.nextBoolean();
            float r = isStatic ? 0.66f : 0.01f;
            float g = isStatic ? 0.33f : 0.52f;
            float b = isStatic ? 0.97f : 0.78f;

            level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), x, y, z, r, g, b);
        }
    }
}
