package ddraig.net.entropica.block;

import ddraig.net.entropica.block.entity.AethericSynthesizerBlockEntity;
import ddraig.net.entropica.inventory.SynthesizerUserInterfaceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class SynthesizerUserInterfaceBlock extends Block {

    public SynthesizerUserInterfaceBlock(Properties properties) {
        super(properties);
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
                // Open the custom Menu, passing the exact position of the Synthesizer
                player.openMenu(new SimpleMenuProvider(
                        (id, inv, p) -> new SynthesizerUserInterfaceMenu(id, inv, finalSynthPos),
                        Component.translatable("block.entropica.synthesizer_user_interface")
                ), buf -> buf.writeBlockPos(finalSynthPos));
            } else {
                player.displayClientMessage(Component.literal("§cSynthesizer UI must be placed adjacent to an Aetheric Synthesizer!"), true);
            }
        }
        return InteractionResult.SUCCESS;
    }
}