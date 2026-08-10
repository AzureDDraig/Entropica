package ddraig.net.entropica.block;

import ddraig.net.entropica.api.materia.MateriaFumusStack;
import ddraig.net.entropica.block.entity.MateriaVesselControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
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

public class MateriaVesselControllerBlock extends Block implements EntityBlock {

    public static final BooleanProperty IS_EXPORT = BooleanProperty.create("is_export");
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public MateriaVesselControllerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(IS_EXPORT, false)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(IS_EXPORT, FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Rotates the face of the block to look directly at the player when placed
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof MateriaVesselControllerBlockEntity controller) {

                // --- Sneak-Right-Click Diagnostic Check ---
                if (player.isShiftKeyDown()) {
                    if (controller.isFormed()) {
                        MateriaFumusStack fume = controller.getStoredMateria();
                        int amount = fume.isEmpty() ? 0 : fume.getAmount();
                        String typeName = fume.isEmpty() ? "Empty" : fume.getType().name();

                        player.displayClientMessage(Component.literal("§ePressure Vessel Status: " + amount + " / " + controller.getMaxCapacity() + "mb of " + typeName), true);
                    } else {
                        player.displayClientMessage(Component.translatable("msg.entropica.pressure_vessel_is_not_currently_formed"), true);
                    }
                    return InteractionResult.SUCCESS;
                }

                // --- NORMAL RIGHT-CLICK BEHAVIOR ---
                if (!controller.isFormed()) {
                    // Trigger the flood-fill validation if not formed
                    boolean success = controller.attemptFormMultiblock();
                    if (success) {
                        player.displayClientMessage(Component.literal("§aVis Fume Pressure Vessel Formed! Capacity: " + controller.getMaxCapacity() + "mb"), true);
                    } else {
                        player.displayClientMessage(Component.translatable("msg.entropica.invalid_pressure_vessel_structure_requires_at"), true);
                    }
                } else {
                    // If already formed, toggle between Import and Export mode
                    controller.toggleMode();
                    boolean isExport = controller.isExportMode();
                    level.setBlock(pos, state.setValue(IS_EXPORT, isExport), 3);

                    String msg = isExport ? "§6OUTPUT (Export)" : "§bINPUT (Import)";
                    player.displayClientMessage(Component.literal("Pressure Vessel set to: " + msg), true);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MateriaVesselControllerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (be instanceof MateriaVesselControllerBlockEntity controller) {
                controller.tick(lvl, pos, st);
            }
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}