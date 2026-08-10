package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.block.entity.EntropicCoreBlockEntity;
import ddraig.net.entropica.block.entity.EssenceReceptacleBlockEntity;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class EssenceReceptacleBlock extends BaseEntityBlock {
    public static final MapCodec<EssenceReceptacleBlock> CODEC = simpleCodec(EssenceReceptacleBlock::new);

    public EssenceReceptacleBlock(Properties properties) {
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
        return new EssenceReceptacleBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof EssenceReceptacleBlockEntity receptacle) {
            EntropicCoreBlockEntity master = receptacle.getMaster();

            if (master == null || !master.isFormed()) {
                player.displayClientMessage(Component.translatable("msg.entropica.receptacle_is_disconnected_or_core_is"), true);
                return InteractionResult.SUCCESS;
            }

            ItemStack heldItem = player.getMainHandItem();
            if (!heldItem.isEmpty()) {
                if (master.getEssenceTypeFromItem(heldItem) != null) {
                    ItemStack remainder = master.insertIntoSharedBuffer(heldItem);

                    if (remainder.getCount() < heldItem.getCount()) {
                        player.setItemInHand(InteractionHand.MAIN_HAND, remainder);
                        player.displayClientMessage(Component.translatable("msg.entropica.added_items_to_shared_essence_buffer"), true);
                    } else {
                        player.displayClientMessage(Component.translatable("msg.entropica.the_shared_buffer_is_full"), true);
                    }
                } else {
                    player.displayClientMessage(Component.translatable("msg.entropica.this_item_is_not_a_valid"), true);
                }
            } else {
                int totalBuffered = 0;
                for (int i = 0; i < master.sharedReceptacleBuffer.getContainerSize(); i++) {
                    totalBuffered += master.sharedReceptacleBuffer.getItem(i).getCount();
                }
                player.displayClientMessage(Component.literal("§7Shared Buffer contains " + totalBuffered + " items waiting to process."), true);
            }
        }
        return InteractionResult.SUCCESS;
    }
}