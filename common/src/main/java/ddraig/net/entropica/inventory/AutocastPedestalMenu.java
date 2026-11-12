package ddraig.net.entropica.inventory;

import ddraig.net.entropica.block.AutocastPedestalBlock;
import ddraig.net.entropica.block.entity.AutocastPedestalBlockEntity;
import ddraig.net.entropica.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Container menu for {@link AutocastPedestalBlock}.
 */
public class AutocastPedestalMenu extends AbstractContainerMenu {

    private final BlockPos pedestalPos;
    private final Container container;
    private final int initialIntervalTicks;
    private final float initialAimPitch;
    private final int initialRedstoneMode;
    private final List<BlockPos> initialLinkedInventories;

    public AutocastPedestalMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        this(
                containerId,
                playerInv,
                extraData.readBlockPos(),
                extraData.readVarInt(),
                extraData.readFloat(),
                extraData.readVarInt(),
                readPosList(extraData)
        );
    }

    private static List<BlockPos> readPosList(FriendlyByteBuf buf) {
        int count = Math.clamp(buf.readVarInt(), 0, AutocastPedestalBlockEntity.MAX_LINKED_INVENTORIES);
        List<BlockPos> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(buf.readBlockPos());
        }
        return list;
    }

    public AutocastPedestalMenu(
            int containerId,
            Inventory playerInv,
            BlockPos pos,
            int intervalTicks,
            float aimPitch,
            int redstoneMode,
            List<BlockPos> linkedInventories
    ) {
        super(ModMenuTypes.AUTOCAST_PEDESTAL_MENU.get(), containerId);
        this.pedestalPos = pos;
        this.initialIntervalTicks = intervalTicks;
        this.initialAimPitch = aimPitch;
        this.initialRedstoneMode = redstoneMode;
        this.initialLinkedInventories = List.copyOf(linkedInventories);

        if (playerInv.player.level().getBlockEntity(pos) instanceof AutocastPedestalBlockEntity be) {
            this.container = be.inventory;
        } else {
            this.container = new SimpleContainer(2);
        }

        // Slot 0: Mounted Inscribed Spellgem / Casting Apparatus / Engraved Weapon
        this.addSlot(new Slot(this.container, AutocastPedestalBlockEntity.SLOT_SPELL, 18, 28) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return AutocastPedestalBlockEntity.isValidSpellMountStack(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        // Slot 1: Orbis Cell / Ampoule / Raw Essence Fuel Slot
        this.addSlot(new Slot(this.container, AutocastPedestalBlockEntity.SLOT_FUEL, 18, 56) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return AutocastPedestalBlockEntity.isValidFuelStack(stack);
            }
        });

        // Player Inventory (3x9) centered for imageWidth=220
        int invStartX = 29;
        int invStartY = 120;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, invStartX + col * 18, invStartY + row * 18));
            }
        }

        // Player Hotbar (9)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, invStartX + col * 18, invStartY + 58));
        }
    }

    public BlockPos getPedestalPos() {
        return this.pedestalPos;
    }

    public int getInitialIntervalTicks() {
        return this.initialIntervalTicks;
    }

    public float getInitialAimPitch() {
        return this.initialAimPitch;
    }

    public int getInitialRedstoneMode() {
        return this.initialRedstoneMode;
    }

    public List<BlockPos> getInitialLinkedInventories() {
        return Collections.unmodifiableList(this.initialLinkedInventories);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();

            if (index < 2) {
                if (!this.moveItemStackTo(stackInSlot, 2, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (AutocastPedestalBlockEntity.isValidSpellMountStack(stackInSlot)) {
                    if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (AutocastPedestalBlockEntity.isValidFuelStack(stackInSlot)) {
                    if (!this.moveItemStackTo(stackInSlot, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        if (!(player.level().getBlockState(this.pedestalPos).getBlock() instanceof AutocastPedestalBlock)) {
            return false;
        }
        return player.distanceToSqr(
                this.pedestalPos.getX() + 0.5D,
                this.pedestalPos.getY() + 0.5D,
                this.pedestalPos.getZ() + 0.5D
        ) <= 64.0D;
    }
}
