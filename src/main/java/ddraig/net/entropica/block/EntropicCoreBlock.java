package ddraig.net.entropica.block;

import ddraig.net.entropica.block.entity.EntropicCoreBlockEntity;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.Nullable;

/**
 * The EntropicCoreBlock class represents a block that interacts with redstone signals to control its behavior.
 * It is designed to work with the EntropicCoreBlockEntity, enabling complex functionality via tile entities.
 *
 * This block can toggle its state (powered or not) based on neighboring redstone signals, perform block entity
 * ticking, and provide analog output signals for interaction with comparators.
 */
public class EntropicCoreBlock extends Block implements EntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public EntropicCoreBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;

        return type == ModBlockEntities.ENTROPIC_CORE_BE.get()
                ? (lvl, pos, st, be) -> ((EntropicCoreBlockEntity) be).tick(lvl, pos, st)
                : null;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean isMoving) {
        if (level.isClientSide()) return;

        boolean isReceivingPower = level.hasNeighborSignal(pos);
        boolean isPoweredState = state.getValue(POWERED);

        // Rising Edge: Redstone turned ON
        if (isReceivingPower && !isPoweredState) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EntropicCoreBlockEntity coreBE) {
                // This call now redirects to the Master core automatically
                coreBE.toggleFurnaceFromRedstone();
            }
            level.setBlock(pos, state.setValue(POWERED, true), 3);
        }
        // Falling Edge: Redstone turned OFF
        else if (!isReceivingPower && isPoweredState) {
            level.setBlock(pos, state.setValue(POWERED, false), 3);
        }
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        // This call now calculates based on the Master core's shared mana pool
        return (be instanceof EntropicCoreBlockEntity coreBE) ? coreBE.getComparatorOutput() : 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EntropicCoreBlockEntity(pos, state);
    }
}