package ddraig.net.entropica.block;

import ddraig.net.entropica.block.entity.GravityCenterBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class GravityCenterBlock extends Block implements EntityBlock {

    public static final IntegerProperty RADIUS_LEVEL = IntegerProperty.create("radius_level", 1, 5);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);

    public static final int[] RADIUS_VALUES = {8, 16, 24, 32, 48};

    public GravityCenterBlock(Properties properties) {
        super(properties.noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(RADIUS_LEVEL, 2)
                .setValue(POWERED, false));
    }

    public static int getRadiusForLevel(int level) {
        int idx = Math.max(1, Math.min(5, level)) - 1;
        return RADIUS_VALUES[idx];
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RADIUS_LEVEL, POWERED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GravityCenterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> {
            if (be instanceof GravityCenterBlockEntity center) {
                center.tick(lvl, pos, st);
            }
        };
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            int currentLevel = state.getValue(RADIUS_LEVEL);
            int nextLevel = (currentLevel % 5) + 1;
            BlockState newState = state.setValue(RADIUS_LEVEL, nextLevel);
            level.setBlock(pos, newState, 3);

            int radius = getRadiusForLevel(nextLevel);
            if (level.getBlockEntity(pos) instanceof GravityCenterBlockEntity be) {
                be.setRadius(radius);
                int fuel = be.getStoredAmount();
                player.displayClientMessage(
                        Component.translatable("message.entropica.gravity_center.radius_fueled", radius, nextLevel, fuel)
                                .withStyle(ChatFormatting.GOLD),
                        true
                );
            }

            level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.6F, 1.2F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        if (!level.isClientSide()) {
            boolean isSignal = level.hasNeighborSignal(pos);
            if (isSignal != state.getValue(POWERED)) {
                level.setBlock(pos, state.setValue(POWERED, isSignal), 3);
            }
        }
    }
}
