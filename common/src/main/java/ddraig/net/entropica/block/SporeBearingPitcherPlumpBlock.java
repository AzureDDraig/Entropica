package ddraig.net.entropica.block;

import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class SporeBearingPitcherPlumpBlock extends EntropicaFlowerBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public SporeBearingPitcherPlumpBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    private static boolean isNetherGround(BlockState state) {
        return state.is(Blocks.NETHERRACK) || state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL) || state.is(Blocks.CRIMSON_NYLIUM) || state.is(Blocks.WARPED_NYLIUM) || state.is(Blocks.NETHER_WART_BLOCK) || state.is(Blocks.WARPED_WART_BLOCK) || state.is(Blocks.BASALT) || state.is(Blocks.SMOOTH_BASALT) || state.is(Blocks.BLACKSTONE) || state.is(Blocks.POLISHED_BLACKSTONE) || state.is(Blocks.MAGMA_BLOCK);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        BlockPos supportPos = context.getClickedPos().relative(face.getOpposite());
        BlockState supportState = context.getLevel().getBlockState(supportPos);
        if (isNetherGround(supportState) || supportState.isFaceSturdy(context.getLevel(), supportPos, face)) {
            return this.defaultBlockState().setValue(FACING, face);
        }
        return null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.UP;
        BlockPos supportPos = pos.relative(facing.getOpposite());
        BlockState supportState = level.getBlockState(supportPos);
        return isNetherGround(supportState) || supportState.isFaceSturdy(level, supportPos, facing);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.Entity entity, net.minecraft.world.entity.InsideBlockEffectApplier effectApplier, boolean isInside) {
        super.entityInside(state, level, pos, entity, effectApplier, isInside);
        if (entity instanceof LivingEntity living && !level.isClientSide()) {
            living.hurt(level.damageSources().magic(), 1.5f);
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1, false, true, true));
            living.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0, false, true, true));
            
            ItemStack chest = living.getItemBySlot(EquipmentSlot.CHEST);
            if (!chest.isEmpty()) {
                chest.hurtAndBreak(1, living, EquipmentSlot.CHEST);
            }
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(Items.GLASS_BOTTLE)) {
            if (!level.isClientSide()) {
                stack.shrink(1);
                ItemStack bottle = new ItemStack(ModItems.ACIDIC_SPORE_NECTAR.get());
                if (!player.getInventory().add(bottle)) {
                    player.drop(bottle, false);
                }
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}
