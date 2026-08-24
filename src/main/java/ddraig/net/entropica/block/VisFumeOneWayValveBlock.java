package ddraig.net.entropica.block;

import ddraig.net.entropica.block.entity.VisFumeOneWayValveBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class VisFumeOneWayValveBlock extends VisFumeValveBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public VisFumeOneWayValveBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(AXIS, Direction.Axis.Z)
                .setValue(OPEN, true)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state != null) {
            // Set the initial flow direction based on the axis and player look direction
            Direction.Axis axis = state.getValue(AXIS);
            Direction lookDir = context.getNearestLookingDirection();
            Direction flowDir = lookDir.getAxis() == axis ? lookDir : Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
            return state.setValue(FACING, flowDir);
        }
        return null;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        // SNEAK-CLICK: Flip the flow direction
        if (player.isShiftKeyDown()) {
            Direction currentFacing = state.getValue(FACING);
            Direction newFacing = currentFacing.getOpposite();

            level.setBlock(pos, state.setValue(FACING, newFacing), 3);
            level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.6F, 1.2F);

            return InteractionResult.SUCCESS;
        }

        // NORMAL CLICK: Toggle the valve Open/Closed (Handled by super class)
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    // FIXED: Overrides the parent method to spawn the correct One-Way Entity
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VisFumeOneWayValveBlockEntity(pos, state);
    }
}