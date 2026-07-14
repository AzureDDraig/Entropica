package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.block.entity.CrucibleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CrucibleBlock extends BaseEntityBlock {
    public static final MapCodec<CrucibleBlock> CODEC = simpleCodec(CrucibleBlock::new);

    // Intricately mapped to match a 6-sided Hollow Hexagon geometry!
    private static final VoxelShape BASE = Block.box(2.0D, 1.0D, 2.0D, 14.0D, 3.0D, 14.0D);

    // Flat North and South walls
    private static final VoxelShape WALL_N = Block.box(4.0D, 3.0D, 1.0D, 12.0D, 16.0D, 3.0D);
    private static final VoxelShape WALL_S = Block.box(4.0D, 3.0D, 13.0D, 12.0D, 16.0D, 15.0D);

    // Angled side walls that meet at a point on the East/West axis
    private static final VoxelShape WALL_NW = Block.box(1.0D, 3.0D, 3.0D, 4.0D, 16.0D, 8.0D);
    private static final VoxelShape WALL_SW = Block.box(1.0D, 3.0D, 8.0D, 4.0D, 16.0D, 13.0D);
    private static final VoxelShape WALL_NE = Block.box(12.0D, 3.0D, 3.0D, 15.0D, 16.0D, 8.0D);
    private static final VoxelShape WALL_SE = Block.box(12.0D, 3.0D, 8.0D, 15.0D, 16.0D, 13.0D);

    private static final VoxelShape SHAPE = Shapes.or(BASE, WALL_N, WALL_S, WALL_NW, WALL_SW, WALL_NE, WALL_SE);

    public CrucibleBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrucibleBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof CrucibleBlockEntity be) {
            if (!level.isClientSide()) {
                // Delegate all interaction logic (inserting/extracting) directly to the Block Entity
                be.interactWithPlayer(player, InteractionHand.MAIN_HAND);
            }
            // Replaced outdated sidedSuccess with the universally accepted SUCCESS
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        // Utilizing playerWillDestroy reliably avoids signature changes in onRemove between sub-versions
        if (level.getBlockEntity(pos) instanceof CrucibleBlockEntity be) {
            Containers.dropContents(level, pos, be.inventory);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}