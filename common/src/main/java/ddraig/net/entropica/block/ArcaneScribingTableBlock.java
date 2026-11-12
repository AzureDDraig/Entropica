package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.block.entity.ArcaneScribingTableBlockEntity;
import ddraig.net.entropica.inventory.ArcaneScribingTableMenu;
import dev.architectury.registry.menu.ExtendedMenuProvider;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Arcane Scribing Table — 2-block-wide visual spell design, blueprint duplication,
 * Spellgem recycling, and safe workstation alignment desk (Spellcrafting Guide Sections 3.7, 8.2, 8.6).
 */
public class ArcaneScribingTableBlock extends BaseEntityBlock {

    public static final MapCodec<ArcaneScribingTableBlock> CODEC = simpleCodec(ArcaneScribingTableBlock::new);

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleWorkstationPart> PART = EnumProperty.create("part", DoubleWorkstationPart.class);

    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D);

    public ArcaneScribingTableBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, DoubleWorkstationPart.LEFT));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    public static Direction getPartnerDirection(Direction facing, DoubleWorkstationPart part) {
        return part == DoubleWorkstationPart.LEFT ? facing.getCounterClockWise() : facing.getClockWise();
    }

    public static BlockPos getPrimaryPos(BlockPos pos, BlockState state) {
        if (state.hasProperty(PART) && state.getValue(PART) == DoubleWorkstationPart.RIGHT) {
            return pos.relative(getPartnerDirection(state.getValue(FACING), DoubleWorkstationPart.RIGHT));
        }
        return pos;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos pos = context.getClickedPos();
        BlockPos partnerPos = pos.relative(getPartnerDirection(facing, DoubleWorkstationPart.LEFT));
        Level level = context.getLevel();
        if (level.getWorldBorder().isWithinBounds(partnerPos)
                && level.getBlockState(partnerPos).canBeReplaced(context)) {
            return this.defaultBlockState()
                    .setValue(FACING, facing)
                    .setValue(PART, DoubleWorkstationPart.LEFT);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide()) {
            BlockPos partnerPos = pos.relative(getPartnerDirection(state.getValue(FACING), DoubleWorkstationPart.LEFT));
            level.setBlock(partnerPos, state.setValue(PART, DoubleWorkstationPart.RIGHT), Block.UPDATE_ALL);
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        DoubleWorkstationPart part = state.getValue(PART);
        if (direction == getPartnerDirection(state.getValue(FACING), part)) {
            if (!neighborState.is(this) || neighborState.getValue(PART) == part) {
                return Blocks.AIR.defaultBlockState();
            }
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            BlockPos primaryPos = getPrimaryPos(pos, state);
            if (level.getBlockEntity(primaryPos) instanceof ArcaneScribingTableBlockEntity be) {
                Containers.dropContents(level, primaryPos, be.inventory);
                be.inventory.clearContent();
            }

            DoubleWorkstationPart part = state.getValue(PART);
            BlockPos partnerPos = pos.relative(getPartnerDirection(state.getValue(FACING), part));
            BlockState partnerState = level.getBlockState(partnerPos);
            if (partnerState.is(this) && partnerState.getValue(PART) != part) {
                if (part == DoubleWorkstationPart.RIGHT && !player.isCreative()) {
                    Block.dropResources(partnerState, level, partnerPos, level.getBlockEntity(partnerPos), player, player.getMainHandItem());
                }
                level.setBlock(partnerPos, Blocks.AIR.defaultBlockState(), 35);
                level.levelEvent(player, 2001, partnerPos, Block.getId(partnerState));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcaneScribingTableBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockPos primaryPos = getPrimaryPos(pos, state);
        if (level.getBlockEntity(primaryPos) instanceof ArcaneScribingTableBlockEntity && player instanceof ServerPlayer serverPlayer) {
            MenuRegistry.openExtendedMenu(serverPlayer, new ExtendedMenuProvider() {
                @Override
                public void saveExtraData(FriendlyByteBuf buf) {
                    buf.writeBlockPos(primaryPos);
                }

                @Override
                public Component getDisplayName() {
                    return Component.translatable("block.entropica.arcane_scribing_table");
                }

                @Override
                public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player p) {
                    return new ArcaneScribingTableMenu(syncId, inv, primaryPos);
                }
            });
        }
        return InteractionResult.SUCCESS;
    }
}
