package ddraig.net.entropica.block;

import ddraig.net.entropica.block.entity.VisFumeVesselPortBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class VisFumeVesselPortBlock extends Block implements EntityBlock {
    // true = Export, false = Import
    public static final BooleanProperty IS_EXPORT = BooleanProperty.create("is_export");

    public VisFumeVesselPortBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(IS_EXPORT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(IS_EXPORT);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof VisFumeVesselPortBlockEntity port) {
                port.toggleMode();
                boolean isExport = port.getMode() == VisFumeVesselPortBlockEntity.PortMode.OUTPUT;
                level.setBlock(pos, state.setValue(IS_EXPORT, isExport), 3);

                String msg = isExport ? "§6OUTPUT (Export)" : "§bINPUT (Import)";
                player.displayClientMessage(Component.literal("Port set to: " + msg), true);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VisFumeVesselPortBlockEntity(pos, state);
    }
}