package ddraig.net.entropica.block;

import ddraig.net.entropica.block.entity.AethericSynthesizerBlockEntity;
import ddraig.net.entropica.inventory.SynthesizerUserInterfaceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class SynthesizerUserInterfaceBlock extends Block {

    // Added the FACING property so the datagen knows how to rotate the block!
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public SynthesizerUserInterfaceBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockPos synthPos = null;

            // Scan all 6 adjacent sides for a Synthesizer
            for (Direction dir : Direction.values()) {
                if (level.getBlockEntity(pos.relative(dir)) instanceof AethericSynthesizerBlockEntity) {
                    synthPos = pos.relative(dir);
                    break;
                }
            }

            if (synthPos != null) {
                final BlockPos finalSynthPos = synthPos;
                // Open the custom Menu using Architectury's MenuRegistry
                dev.architectury.registry.menu.MenuRegistry.openExtendedMenu(
                    (net.minecraft.server.level.ServerPlayer) player,
                    new dev.architectury.registry.menu.ExtendedMenuProvider() {
                        @Override
                        public void saveExtraData(net.minecraft.network.FriendlyByteBuf buf) {
                            buf.writeBlockPos(finalSynthPos);
                        }

                        @Override
                        public Component getDisplayName() {
                            return Component.translatable("block.entropica.synthesizer_user_interface");
                        }

                        @Override
                        public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, net.minecraft.world.entity.player.Inventory inv, Player p) {
                            return new SynthesizerUserInterfaceMenu(id, inv, finalSynthPos);
                        }
                    }
                );
            } else {
                player.displayClientMessage(Component.translatable("msg.entropica.synthesizer_ui_must_be_placed_adjacent"), true);
            }
        }
        return InteractionResult.SUCCESS;
    }
}