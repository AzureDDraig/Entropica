package ddraig.net.entropica.block;

import ddraig.net.entropica.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class BloodTendrilBrambleBlock extends BushBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public BloodTendrilBrambleBlock(Properties properties) {
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
            living.hurt(level.damageSources().cactus(), 1.0f);
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 2, false, true, true));
            living.addEffect(new MobEffectInstance(Holder.direct(ModEffects.BLEEDING.get()), 100, 0, false, true, true));
        }
    }
}
