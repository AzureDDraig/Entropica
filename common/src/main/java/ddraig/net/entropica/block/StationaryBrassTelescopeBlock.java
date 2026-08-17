package ddraig.net.entropica.block;

import ddraig.net.entropica.client.gui.ConstellationTracingScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class StationaryBrassTelescopeBlock extends Block {

    public StationaryBrassTelescopeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        level.playSound(player, pos, SoundEvents.SPYGLASS_USE, SoundSource.BLOCKS, 1.0f, 0.8f);
        if (level.isClientSide()) {
            ConstellationTracingScreen.openForCurrentNight(player);
        }
        return InteractionResult.SUCCESS;
    }
}
