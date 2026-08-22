package ddraig.net.entropica.item;

import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CrystalToolItem extends Item {

    public enum ToolType {
        SWORD(() -> ModItems.DRAINED_CRYSTAL_SWORD.get()),
        PICKAXE(() -> ModItems.DRAINED_CRYSTAL_PICKAXE.get()),
        AXE(() -> ModItems.DRAINED_CRYSTAL_AXE.get()),
        SHOVEL(() -> ModItems.DRAINED_CRYSTAL_SHOVEL.get()),
        HOE(() -> ModItems.DRAINED_CRYSTAL_HOE.get());

        private final Supplier<Item> drainedSupplier;

        ToolType(Supplier<Item> drainedSupplier) {
            this.drainedSupplier = drainedSupplier;
        }

        public Item getDrainedItem() {
            return drainedSupplier.get();
        }
    }

    private final ToolType toolType;

    public CrystalToolItem(Properties properties, ToolType toolType) {
        super(properties);
        this.toolType = toolType;
    }

    public ToolType getToolType() {
        return toolType;
    }

    public ItemStack getDrainedVariant(ItemStack original) {
        Item drainedItem = toolType.getDrainedItem();
        if (drainedItem == null) return ItemStack.EMPTY;
        ItemStack drainedStack = new ItemStack(drainedItem);
        // Preserve custom data / enchantments
        drainedStack.applyComponents(original.getComponents());
        return drainedStack;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);

        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        // Night Starlight Self-Repair: 1 Durability restored every 100 ticks (5 seconds) under open night skies
        if (stack.isDamaged() && level.getGameTime() % 100 == 0) {
            BlockPos pos = living.blockPosition();
            long dayTime = level.getDayTime() % 24000L;
            boolean isNight = (dayTime >= 12500L && dayTime <= 23500L) && !level.isRaining();
            if (isNight && level.canSeeSky(pos.above())) {
                stack.setDamageValue(Math.max(0, stack.getDamageValue() - 1));
            }
        }

        // Breakage Transition: If tool reaches 0 durability (damage >= maxDamage) -> convert to drained variant
        if (stack.getDamageValue() >= stack.getMaxDamage()) {
            ItemStack drained = getDrainedVariant(stack);
            if (entity instanceof ServerPlayer player) {
                if (slot != null) {
                    player.setItemSlot(slot, drained);
                } else {
                    int idx = player.getInventory().findSlotMatchingItem(stack);
                    if (idx >= 0) {
                        player.getInventory().setItem(idx, drained);
                    }
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("§b✦ Celestial Crystal Matrix"));
        tooltipComponents.accept(Component.literal("§7  Passively self-repairs under open night skies (1 dur/5s)."));
        tooltipComponents.accept(Component.literal("§8  Converts into a Drained Crystal Tool upon total depletion."));
    }
}
