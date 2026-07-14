package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class AttunementPedestalBlockEntity extends BlockEntity {
    public final SimpleContainer inventory = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            super.setChanged();
            AttunementPedestalBlockEntity.this.setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    public AttunementPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ATTUNEMENT_PEDESTAL_BE.get(), pos, state);
    }

    public boolean interactWithPlayer(Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);

        // Extract
        if (heldItem.isEmpty()) {
            for (int i = 1; i >= 0; i--) {
                ItemStack stackInSlot = inventory.getItem(i);
                if (!stackInSlot.isEmpty()) {
                    player.setItemInHand(hand, stackInSlot.copy());
                    inventory.setItem(i, ItemStack.EMPTY);
                    return true;
                }
            }
        }
        // Insert
        else {
            // 1. Try to stack with an existing matching item first
            for (int i = 0; i < 2; i++) {
                ItemStack stackInSlot = inventory.getItem(i);
                if (!stackInSlot.isEmpty() && ItemStack.isSameItemSameComponents(stackInSlot, heldItem)) {
                    if (stackInSlot.getCount() < stackInSlot.getMaxStackSize()) {
                        stackInSlot.grow(1);
                        heldItem.shrink(1);
                        this.setChanged();
                        if (this.level != null && !this.level.isClientSide()) {
                            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
                        }
                        return true;
                    }
                }
            }

            // 2. If no matching stack is found, place it in the first empty slot
            for (int i = 0; i < 2; i++) {
                ItemStack stackInSlot = inventory.getItem(i);
                if (stackInSlot.isEmpty()) {
                    inventory.setItem(i, heldItem.copyWithCount(1));
                    heldItem.shrink(1);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level != null) {
            Containers.dropContents(this.level, pos, this.inventory);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            output.store("PedestalStack_" + i, ItemStack.OPTIONAL_CODEC, inventory.getItem(i));
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            int finalI = i;
            input.read("PedestalStack_" + i, ItemStack.OPTIONAL_CODEC).ifPresent(stack -> inventory.setItem(finalI, stack));
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p) { return this.saveWithoutMetadata(p); }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}