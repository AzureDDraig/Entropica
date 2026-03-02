package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.block.entity.VisFumePressureChamberControllerBlockEntity;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class VisFumePressureChamberControllerBlock extends BaseEntityBlock {
    public static final MapCodec<VisFumePressureChamberControllerBlock> CODEC = simpleCodec(VisFumePressureChamberControllerBlock::new);
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    public VisFumePressureChamberControllerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FORMED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FORMED);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof VisFumePressureChamberControllerBlockEntity controller) {
            if (!controller.isFormed()) {
                boolean formed = controller.attemptFormMultiblock();
                if (formed) {
                    player.displayClientMessage(Component.literal("§aChamber Sealed. Ready to pressurize."), true);
                    level.setBlock(pos, state.setValue(FORMED, true), 3);
                } else {
                    player.displayClientMessage(Component.literal("§cInvalid Chamber Structure."), true);
                }
            } else {
                if (player.isShiftKeyDown()) {
                    // SNEAK + CLICK (Empty Hand): Start pressurizing!
                    controller.startPressurizing(player);
                } else {
                    // NORMAL CLICK: Insert or extract the item
                    boolean swapped = controller.proxyInteractWithTable(player, InteractionHand.MAIN_HAND);
                    if (!swapped) {
                        // Helpful hint if they click an empty table with an empty hand
                        if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                            player.displayClientMessage(Component.literal("§eSneak-Right-Click to begin pressurization!"), true);
                        }
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VisFumePressureChamberControllerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.VIS_FUME_PRESSURE_CHAMBER_CONTROLLER_BE.get(),
                (lvl, pos1, st, blockEntity) -> blockEntity.tick(lvl, pos1, st));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}