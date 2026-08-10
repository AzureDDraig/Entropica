package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AbyssalWeeprootBlock extends GrowingPlantHeadBlock {
    public static final MapCodec<AbyssalWeeprootBlock> CODEC = simpleCodec(AbyssalWeeprootBlock::new);
    protected static final VoxelShape SHAPE = Block.box(4.0, 5.0, 4.0, 12.0, 16.0, 12.0);

    public AbyssalWeeprootBlock(Properties properties) {
        super(properties, Direction.DOWN, SHAPE, false, 0.1);
    }

    @Override
    protected MapCodec<? extends GrowingPlantHeadBlock> codec() {
        return CODEC;
    }

    @Override
    protected int getBlocksToGrowWhenBonemealed(RandomSource random) {
        return random.nextInt(3) + 1;
    }

    @Override
    protected boolean canGrowInto(BlockState state) {
        return state.isAir();
    }

    @Override
    protected Block getBodyBlock() {
        return ModBlocks.ABYSSAL_WEEPROOT_PLANT.get();
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, net.minecraft.world.entity.InsideBlockEffectApplier effectApplier, boolean isInside) {
        super.entityInside(state, level, pos, entity, effectApplier, isInside);
        
        // Void-Tether Slow Falling Mechanics
        if (entity instanceof LivingEntity livingEntity && !level.isClientSide()) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0, false, false, true));
        }
    }


    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.BERRIES) && state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.BERRIES)) {
            if (!level.isClientSide()) {
                popResource(level, pos, new ItemStack(ModItems.ABYSSAL_TENDRIL.get()));
                level.playSound(null, pos, SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.BLOCKS, 1.0f, 1.0f);
                level.setBlock(pos, state.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.BERRIES, false), 2);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }


    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(4) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble() * 0.5;
            double z = pos.getZ() + random.nextDouble();

            // Dark Abyssal Cyan Droplet Sparkles (#0284C7)
            float r = 0.01f;
            float g = 0.52f;
            float b = 0.78f;

            level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), x, y, z, r, g, b);
        }
    }
}
