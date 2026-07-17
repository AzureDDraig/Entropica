package ddraig.net.entropica.block;

import ddraig.net.entropica.block.entity.ScribingControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ScribingControllerBlock extends BaseEntityBlock {
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ScribingControllerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FORMED, false)
                .setValue(FACING, Direction.NORTH));
    }

    public static final com.mojang.serialization.MapCodec<ScribingControllerBlock> CODEC = simpleCodec(ScribingControllerBlock::new);

    @Override
    protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FORMED, FACING);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ScribingControllerBlockEntity controller) {
                net.minecraft.world.Containers.dropContents(level, pos, controller);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ScribingControllerBlockEntity controller) {
                if (player.isShiftKeyDown()) {
                    if (controller.isFormed()) {
                        player.displayClientMessage(Component.literal("§eScribing Engine is formed! Stored Materia Fumus: " + controller.getStoredMateria().getAmount() + "mb"), false);
                    } else {
                        player.displayClientMessage(Component.literal("§cScribing Engine is not formed."), false);
                    }
                    return InteractionResult.SUCCESS;
                }

                if (!controller.isFormed()) {
                    boolean success = controller.attemptFormMultiblock();
                    if (success) {
                        player.displayClientMessage(Component.literal("§aScribing Engine Formed Successfully!"), false);
                    } else {
                        player.displayClientMessage(Component.literal("§cInvalid structure. Check block placements for 3x3x3 layout."), false);
                    }
                } else {
                    // Open Scribing Engine GUI (to be implemented)
                    player.displayClientMessage(Component.literal("§eOpening Runic Scribing Controller interface..."), false);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ScribingControllerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        // Return null or ticker for the controller block entity to update logic
        // We will register a tick helper
        return createTickerHelper(blockEntityType, ddraig.net.entropica.registry.ModBlockEntities.SCRIBING_CONTROLLER_BE.get(), ScribingControllerBlockEntity::tick);
    }
}
