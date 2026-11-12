package ddraig.net.entropica.inventory;

import ddraig.net.entropica.block.RunicAnvilBlock;
import ddraig.net.entropica.block.entity.ArcaneScribingTableBlockEntity;
import ddraig.net.entropica.item.CastingApparatusItem;
import ddraig.net.entropica.item.SpellParchmentItem;
import ddraig.net.entropica.item.SpellgemItem;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Container menu for the {@link ddraig.net.entropica.block.ArcaneScribingTableBlock}.
 *
 * <p>Positions the 4 workstation slots inside the Right Pull-Out Supply Drawer bay and
 * centers the player's inventory satchel neatly beneath the tabletop.</p>
 */
public class ArcaneScribingTableMenu extends AbstractContainerMenu {

    public static final int IMAGE_WIDTH = 316;
    public static final int IMAGE_HEIGHT = 236;

    private final BlockPos tablePos;
    private final Container container;
    private final ContainerLevelAccess levelAccess;

    /**
     * Client-driven visibility gate for the 4 workstation slots inside the Right Supply Drawer.
     * Always remains {@code true} on the server so shift-clicking and container sync never desync,
     * while preventing 3D item icons from poking through the closed wooden cover on the client.
     */
    private boolean supplyDrawerExposed = true;

    public ArcaneScribingTableMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        this(containerId, playerInv, extraData.readBlockPos());
    }

    public ArcaneScribingTableMenu(int containerId, Inventory playerInv, BlockPos pos) {
        super(ModMenuTypes.ARCANE_SCRIBING_TABLE_MENU.get(), containerId);
        this.tablePos = pos;
        this.levelAccess = ContainerLevelAccess.create(playerInv.player.level(), pos);

        if (playerInv.player.level().getBlockEntity(pos) instanceof ArcaneScribingTableBlockEntity be) {
            this.container = be.inventory;
        } else {
            this.container = new SimpleContainer(4);
        }

        // Slot 0: Blank Parchment Input (Top-Left of Right Supply Drawer)
        this.addSlot(new Slot(this.container, ArcaneScribingTableBlockEntity.SLOT_PARCHMENT, 246, 34) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.BLANK_PARCHMENT.get());
            }

            @Override
            public boolean isActive() {
                return ArcaneScribingTableMenu.this.supplyDrawerExposed;
            }
        });

        // Slot 1: Scribing Ink Input (Top-Right of Right Supply Drawer)
        this.addSlot(new Slot(this.container, ArcaneScribingTableBlockEntity.SLOT_INK, 282, 34) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.SCRIBING_INK.get());
            }

            @Override
            public boolean isActive() {
                return ArcaneScribingTableMenu.this.supplyDrawerExposed;
            }
        });

        // Slot 2: Study / Duplicate / Scrub / Apparatus Alignment Slot (Bottom-Left of Right Supply Drawer)
        this.addSlot(new Slot(this.container, ArcaneScribingTableBlockEntity.SLOT_STUDY, 246, 116) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof SpellParchmentItem
                        || stack.getItem() instanceof SpellgemItem
                        || stack.getItem() instanceof CastingApparatusItem
                        || RunicAnvilBlock.isEngravableWeaponOrTool(stack);
            }

            @Override
            public boolean isActive() {
                return ArcaneScribingTableMenu.this.supplyDrawerExposed;
            }
        });

        // Slot 3: Output Scroll Cradle (Bottom-Right of Right Supply Drawer)
        this.addSlot(new Slot(this.container, ArcaneScribingTableBlockEntity.SLOT_OUTPUT, 282, 116) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean isActive() {
                return ArcaneScribingTableMenu.this.supplyDrawerExposed;
            }
        });

        // Player Inventory (3x9) centered beneath the desk at x=77..239, y=158..212
        int invStartX = 77;
        int invStartY = 158;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, invStartX + col * 18, invStartY + row * 18));
            }
        }

        // Player Hotbar (9) at y=216
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, invStartX + col * 18, invStartY + 58));
        }
    }

    public BlockPos getTablePos() {
        return this.tablePos;
    }

    public void setSupplyDrawerExposed(boolean exposed) {
        this.supplyDrawerExposed = exposed;
    }

    public boolean isSupplyDrawerExposed() {
        return this.supplyDrawerExposed;
    }

    public boolean hasAnyWorkstationItem() {
        for (int i = 0; i < 4; i++) {
            if (this.slots.get(i).hasItem()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();

            if (index < 4) {
                if (!this.moveItemStackTo(stackInSlot, 4, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (stackInSlot.is(ModItems.BLANK_PARCHMENT.get())) {
                    if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (stackInSlot.is(ModItems.SCRIBING_INK.get())) {
                    if (!this.moveItemStackTo(stackInSlot, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(stackInSlot, 2, 3, false)) {
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
        return stillValid(this.levelAccess, player, ModBlocks.ARCANE_SCRIBING_TABLE.get());
    }
}
