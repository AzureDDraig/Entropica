package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.client.gui.ConstellationTracingScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

public class StationaryBrassTelescopeBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<StationaryBrassTelescopeBlock> CODEC = simpleCodec(StationaryBrassTelescopeBlock::new);

    public StationaryBrassTelescopeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
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
