package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.block.entity.ManaFilterBlockEntity;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ManaFilterBlock extends BaseEntityBlock {
    public static final MapCodec<ManaFilterBlock> CODEC = simpleCodec(ManaFilterBlock::new);

    public ManaFilterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ManaFilterBlockEntity(pos, state);
    }

    // --- ADDED TICKER ---
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // We only want the auto-pushing math to happen on the server
        if (level.isClientSide()) {
            return null;
        }

        // Safely map the ticking logic to our Block Entity
        return createTickerHelper(type, ModBlockEntities.MANA_FILTER_BE.get(),
                (lvl, pos, st, be) -> be.tick(lvl, pos, st));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ManaFilterBlockEntity filter) {
                ItemStack stack = player.getMainHandItem();

                // 1. If the player is holding an ampoule, try to extract mana
                if (!stack.isEmpty() && filter.interactWithAmpoule(player, stack, InteractionHand.MAIN_HAND)) {
                    return InteractionResult.SUCCESS;
                }

                // 2. Otherwise, cycle the filter normally
                filter.cycleFilter();
                player.displayClientMessage(
                        Component.literal("§7Filter set to: ").append(Component.literal(filter.getFilterType().getFormattedName())),
                        true
                );
            }
        }
        return InteractionResult.SUCCESS;
    }
}