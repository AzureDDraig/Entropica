package ddraig.net.entropica.block;

import ddraig.net.entropica.block.entity.AethericSynthesizerBlockEntity;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class AethericSynthesizerBlock extends Block implements EntityBlock {

    public AethericSynthesizerBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AethericSynthesizerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.AETHERIC_SYNTHESIZER_BE.get() ?
                (lvl, pos, st, be) -> ((AethericSynthesizerBlockEntity) be).tick(lvl, pos, st) : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof AethericSynthesizerBlockEntity be) {
            // Block manual extraction while an animation is playing
            if (be.isCrafting) return InteractionResult.CONSUME;

            ItemStack heldItem = player.getMainHandItem();

            // --- BATCH OUTPUT EXTRACTION ---
            boolean extractedAny = false;
            for (int i = 25; i < 29; i++) {
                ItemStack outputItem = be.inventory.getItem(i);
                if (!outputItem.isEmpty()) {
                    if (!level.isClientSide()) {
                        player.getInventory().placeItemBackInInventory(outputItem.copy());
                        be.inventory.setItem(i, ItemStack.EMPTY);
                    }
                    extractedAny = true;
                }
            }
            if (extractedAny) return InteractionResult.SUCCESS;

            // Shift Right-Click
            if (player.isShiftKeyDown()) {
                // Try to craft first
                if (be.attemptCrafting()) {
                    return InteractionResult.SUCCESS;
                }
                // If it fails (recipe invalid) and hand is empty, extract the last item
                else if (heldItem.isEmpty()) {
                    for (int i = 24; i >= 0; i--) {
                        if (!be.inventory.getItem(i).isEmpty()) {
                            if (!level.isClientSide()) {
                                player.addItem(be.inventory.getItem(i));
                                be.inventory.setItem(i, ItemStack.EMPTY);
                            }
                            return InteractionResult.SUCCESS;
                        }
                    }
                }
                return InteractionResult.SUCCESS;
            }

            // Normal Right-Click on top face (Insert/Extract specific slots)
            boolean didSlotAction = false;
            if (hitResult.getDirection() == Direction.UP) {
                int slot = getClickedSlot(hitResult, pos);

                if (slot >= 0 && slot < 25) {
                    ItemStack slotItem = be.inventory.getItem(slot);

                    // Insert
                    if (slotItem.isEmpty() && !heldItem.isEmpty()) {
                        if (!level.isClientSide()) {
                            ItemStack insert = heldItem.copyWithCount(1);
                            be.inventory.setItem(slot, insert);
                            heldItem.shrink(1);
                        }
                        didSlotAction = true;
                    }
                    // Extract specific
                    else if (!slotItem.isEmpty() && heldItem.isEmpty()) {
                        if (!level.isClientSide()) {
                            player.getInventory().placeItemBackInInventory(slotItem);
                            be.inventory.setItem(slot, ItemStack.EMPTY);
                        }
                        didSlotAction = true;
                    }
                }
            }

            // --- ATTEMPT CRAFTING (Fallback for Normal Right-Click) ---
            // If they clicked an empty slot with an empty hand, or clicked the side of the block, try to craft!
            if (!didSlotAction && heldItem.isEmpty()) {
                if (be.attemptCrafting()) {
                    return InteractionResult.SUCCESS;
                }
            }

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private int getClickedSlot(BlockHitResult hitResult, BlockPos pos) {
        double relativeX = hitResult.getLocation().x - pos.getX();
        double relativeZ = hitResult.getLocation().z - pos.getZ();

        int col = (int) (relativeX / 0.2);
        int row = (int) (relativeZ / 0.2);

        col = Math.max(0, Math.min(4, col));
        row = Math.max(0, Math.min(4, row));

        return (row * 5) + col;
    }
}