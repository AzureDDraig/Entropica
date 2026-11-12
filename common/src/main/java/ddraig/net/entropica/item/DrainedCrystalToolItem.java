package ddraig.net.entropica.item;

import ddraig.net.entropica.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class DrainedCrystalToolItem extends Item {

    public enum DrainedType {
        SWORD(() -> ModItems.CRYSTAL_SWORD.get()),
        PICKAXE(() -> ModItems.CRYSTAL_PICKAXE.get()),
        AXE(() -> ModItems.CRYSTAL_AXE.get()),
        SHOVEL(() -> ModItems.CRYSTAL_SHOVEL.get()),
        HOE(() -> ModItems.CRYSTAL_HOE.get()),
        GENERIC(() -> ModItems.CRYSTAL_SWORD.get());

        private final Supplier<Item> restoredSupplier;

        DrainedType(Supplier<Item> restoredSupplier) {
            this.restoredSupplier = restoredSupplier;
        }

        public Item getRestoredItem() {
            return restoredSupplier.get();
        }
    }

    private final DrainedType drainedType;

    public DrainedCrystalToolItem(Properties properties, DrainedType drainedType) {
        super(properties);
        this.drainedType = drainedType;
    }

    public DrainedType getDrainedType() {
        return drainedType;
    }

    public ItemStack getRestoredVariant(ItemStack original) {
        Item restoredItem = drainedType.getRestoredItem();
        if (restoredItem == null) return ItemStack.EMPTY;
        ItemStack restoredStack = new ItemStack(restoredItem);
        restoredStack.applyComponents(original.getComponents());
        restoredStack.setDamageValue(0);
        return restoredStack;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("§8✦ Depleted Celestial Lattice"));
        tooltipComponents.accept(Component.literal("§7  Place onto a Modular Astral Altar under open night starlight to restore full crystalline resonance."));
    }
}
