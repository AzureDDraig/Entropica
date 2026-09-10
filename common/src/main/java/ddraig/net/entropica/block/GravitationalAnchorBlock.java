package ddraig.net.entropica.block;

import ddraig.net.entropica.block.entity.GravitationalAnchorBlockEntity;
import ddraig.net.entropica.gravity.GravityField;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class GravitationalAnchorBlock extends Block implements EntityBlock {

    public static final EnumProperty<GravityField.Mode> MODE = EnumProperty.create("mode", GravityField.Mode.class);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D);

    public GravitationalAnchorBlock(Properties properties) {
        super(properties.noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(MODE, GravityField.Mode.ZERO_G)
                .setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MODE, POWERED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GravitationalAnchorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> {
            if (be instanceof GravitationalAnchorBlockEntity anchor) {
                anchor.tick(lvl, pos, st);
            }
        };
    }

    @Override
    protected InteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hitResult) {
        return useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            GravityField.Mode current = state.getValue(MODE);
            GravityField.Mode next;
            switch (current) {
                case ZERO_G -> next = GravityField.Mode.LUNAR;
                case LUNAR -> next = GravityField.Mode.INVERSION;
                case INVERSION -> next = GravityField.Mode.SINGULARITY;
                case SINGULARITY -> next = GravityField.Mode.REPULSOR;
                case REPULSOR -> next = GravityField.Mode.TIDAL_PULSE;
                default -> next = GravityField.Mode.ZERO_G;
            }

            BlockState newState = state.setValue(MODE, next);
            level.setBlock(pos, newState, 3);

            if (level.getBlockEntity(pos) instanceof GravitationalAnchorBlockEntity be) {
                be.onModeChanged(next);
            }

            net.minecraft.ChatFormatting modeColor = switch (next) {
                case ZERO_G -> net.minecraft.ChatFormatting.AQUA;
                case LUNAR -> net.minecraft.ChatFormatting.GOLD;
                case INVERSION -> net.minecraft.ChatFormatting.DARK_PURPLE;
                case SINGULARITY -> net.minecraft.ChatFormatting.LIGHT_PURPLE;
                case REPULSOR -> net.minecraft.ChatFormatting.RED;
                case TIDAL_PULSE -> net.minecraft.ChatFormatting.BLUE;
                default -> net.minecraft.ChatFormatting.WHITE;
            };

            Component modeText = Component.literal(next.getDisplayName()).withStyle(modeColor, net.minecraft.ChatFormatting.BOLD);
            player.displayClientMessage(Component.translatable("message.entropica.anchor.mode", modeText), true);
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 1.2F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean isMoving) {
        if (!level.isClientSide()) {
            boolean isPowered = level.hasNeighborSignal(pos);
            if (isPowered != state.getValue(POWERED)) {
                level.setBlock(pos, state.setValue(POWERED, isPowered), 3);
            }
        }
    }
}