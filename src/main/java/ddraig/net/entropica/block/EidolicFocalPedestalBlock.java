package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.block.entity.EidolicLatheBlockEntity;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class EidolicFocalPedestalBlock extends BaseEntityBlock {
    public static final MapCodec<EidolicFocalPedestalBlock> CODEC = simpleCodec(EidolicFocalPedestalBlock::new);
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    public EidolicFocalPedestalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FORMED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FORMED, BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EidolicLatheBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.EIDOLIC_FOCAL_PEDESTAL_BE.get(),
                (lvl, pos, st, be) -> be.tick(lvl, pos, st));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof EidolicLatheBlockEntity lathe) {
            if (!lathe.isFormed()) {
                boolean formed = lathe.attemptFormMultiblock();
                if (formed) {
                    player.displayClientMessage(Component.literal("§aEidolic Lathe Resonating and Active."), true);
                } else {
                    player.displayClientMessage(Component.literal("§cIncomplete Lathe Structure."), true);
                }
            } else {
                // Trigger the forging process if sneaking!
                if (player.isShiftKeyDown()) {
                    lathe.attemptCraft(player);
                } else {
                    boolean swapped = lathe.interactWithPlayer(player, InteractionHand.MAIN_HAND);
                    if (!swapped && player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                        player.displayClientMessage(Component.literal("§eSneak-Right-Click to begin forging sequence!"), true);
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }
}