package ddraig.net.entropica.inventory;

import ddraig.net.entropica.block.entity.AethericSynthesizerBlockEntity;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SynthesizerUserInterfaceMenu extends AbstractContainerMenu {

    private final AethericSynthesizerBlockEntity synthesizer;
    private final ContainerLevelAccess levelAccess; // Fixed type here!

    // Client-side constructor
    public SynthesizerUserInterfaceMenu(int containerId, Inventory playerInv, FriendlyByteBuf data) {
        this(containerId, playerInv, data.readBlockPos());
    }

    // Server-side constructor
    public SynthesizerUserInterfaceMenu(int containerId, Inventory playerInv, BlockPos synthPos) {
        super(ModMenuTypes.SYNTHESIZER_USER_INTERFACE_MENU.get(), containerId);
        this.levelAccess = ContainerLevelAccess.create(playerInv.player.level(), synthPos);

        if (playerInv.player.level().getBlockEntity(synthPos) instanceof AethericSynthesizerBlockEntity be) {
            this.synthesizer = be;
        } else {
            this.synthesizer = null;
        }

        if (this.synthesizer != null) {
            // 1. Synthesizer 5x5 Input Grid (Slots 0-24)
            for (int row = 0; row < 5; row++) {
                for (int col = 0; col < 5; col++) {
                    this.addSlot(new Slot(synthesizer.inventory, row * 5 + col, 12 + col * 18, 17 + row * 18));
                }
            }

            // 2. Synthesizer 2x2 Output Grid (Slots 25-28)
            for (int row = 0; row < 2; row++) {
                for (int col = 0; col < 2; col++) {
                    // Custom output slot prevents placing items directly into the output
                    this.addSlot(new Slot(synthesizer.inventory, 25 + (row * 2 + col), 142 + col * 18, 44 + row * 18) {
                        @Override
                        public boolean mayPlace(ItemStack stack) {
                            return false;
                        }
                    });
                }
            }
        }

        // 3. Player Inventory (Slots 9-35)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 26 + col * 18, 122 + row * 18));
            }
        }

        // 4. Player Hotbar (Slots 0-8)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 26 + col * 18, 180));
        }
    }

    public BlockPos getSynthesizerPos() {
        return this.synthesizer != null ? this.synthesizer.getBlockPos() : BlockPos.ZERO;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            // Moving from Synthesizer to Player
            if (index < 29) {
                if (!this.moveItemStackTo(itemstack1, 29, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            // Moving from Player to Synthesizer Grid (Slots 0-24)
            else if (!this.moveItemStackTo(itemstack1, 0, 25, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.levelAccess, player, ModBlocks.AETHERIC_SYNTHESIZER.get()) || stillValid(this.levelAccess, player, ModBlocks.SYNTHESIZER_USER_INTERFACE.get());
    }
}