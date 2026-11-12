package ddraig.net.entropica.block;

import ddraig.net.entropica.block.entity.AethericAutomatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class AethericAutomatorBlock extends Block implements EntityBlock {

    // Physically models an upside-down hopper
    private static final VoxelShape TOP = Block.box(0.0D, 10.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape MIDDLE = Block.box(4.0D, 4.0D, 4.0D, 12.0D, 10.0D, 12.0D);
    private static final VoxelShape BOTTOM = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 4.0D, 10.0D);
    private static final VoxelShape SHAPE = Shapes.or(TOP, MIDDLE, BOTTOM);

    public AethericAutomatorBlock(Properties properties) {
        super(properties.noOcclusion());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AethericAutomatorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> {
            if (be instanceof AethericAutomatorBlockEntity automator) {
                automator.tick(lvl, pos, st);
            }
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        // Shift right-clicking with an empty hand saves the target recipe from the nearest Synthesizer!
        if (!level.isClientSide() && player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof AethericAutomatorBlockEntity be) {
            be.saveRecipeFromSynthesizer(player);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}