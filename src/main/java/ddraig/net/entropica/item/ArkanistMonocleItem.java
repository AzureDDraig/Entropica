package ddraig.net.entropica.item;

import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import java.util.List;
import java.util.function.Consumer;

public class ArkanistMonocleItem extends Item {
    public ArkanistMonocleItem(Properties properties) {
        super(properties.stacksTo(1).equippable(EquipmentSlot.HEAD));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("§5Slotted Lens:"));
        
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag nbt = customData != null ? customData.copyTag() : new CompoundTag();
        int activeIdx = nbt.getInt("ActiveSlotIndex").orElse(0);
        
        for (int i = 0; i < 5; i++) {
            boolean isActive = (i == activeIdx);
            String prefix = isActive ? "  > " : "    ";
            if (i < 3) {
                String key = "Lens" + i;
                String regName = nbt.getString(key).orElse("");
                if (!regName.isEmpty()) {
                    net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM
                        .getOptional(net.minecraft.resources.ResourceLocation.parse(regName))
                        .orElse(net.minecraft.world.item.Items.AIR);
                    if (item != null && item != net.minecraft.world.item.Items.AIR) {
                        String colorCode = isActive ? "§b" : "§7";
                        tooltipComponents.accept(Component.literal(prefix + colorCode + item.getDefaultInstance().getHoverName().getString()));
                        continue;
                    }
                }
                String colorCode = isActive ? "§3" : "§8";
                tooltipComponents.accept(Component.literal(prefix + colorCode + "___"));
            } else {
                String colorCode = isActive ? "§d" : "§5";
                tooltipComponents.accept(Component.literal(prefix + colorCode + "?????"));
            }
        }
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, net.minecraft.world.entity.player.Player player) {
        if (action == ClickAction.SECONDARY && slot.allowModification(player)) {
            ItemStack other = slot.getItem();
            if (other.isEmpty()) {
                ItemStack extracted = extractLens(stack, player);
                if (!extracted.isEmpty()) {
                    slot.set(extracted);
                    return true;
                }
            } else if (isValidLens(other)) {
                if (insertLens(stack, other, player)) {
                    return true;
                }
            }
        }
        return super.overrideStackedOnOther(stack, slot, action, player);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, net.minecraft.world.entity.player.Player player, SlotAccess slotAccess) {
        if (action == ClickAction.SECONDARY) {
            if (other.isEmpty()) {
                ItemStack extracted = extractLens(stack, player);
                if (!extracted.isEmpty()) {
                    slotAccess.set(extracted);
                    return true;
                }
            } else if (isValidLens(other)) {
                if (insertLens(stack, other, player)) {
                    return true;
                }
            }
        }
        return super.overrideOtherStackedOnMe(stack, other, slot, action, player, slotAccess);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            cycleLens(stack, player);
        }
        return InteractionResult.SUCCESS;
    }

    public static boolean isValidLens(ItemStack stack) {
        net.minecraft.world.item.Item item = stack.getItem();
        return item == ModItems.PROPAGATION_LENS.get() ||
               item == ModItems.AETHERIC_LENS.get() ||
               item == ModItems.VITAE_LENS.get() ||
               item == ModItems.MATERIA_LENS.get();
    }

    public static boolean insertLens(ItemStack monocle, ItemStack lens, net.minecraft.world.entity.player.Player player) {
        CustomData customData = monocle.get(DataComponents.CUSTOM_DATA);
        CompoundTag nbt = customData != null ? customData.copyTag() : new CompoundTag();
        
        int slotToInsert = -1;
        for (int i = 0; i < 3; i++) {
            String key = "Lens" + i;
            if (nbt.getString(key).orElse("").isEmpty()) {
                slotToInsert = i;
                break;
            }
        }
        
        if (slotToInsert != -1) {
            String regName = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(lens.getItem()).toString();
            nbt.putString("Lens" + slotToInsert, regName);
            monocle.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
            lens.shrink(1);
            if (player != null) {
                player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_GENERIC.value(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.2f);
            }
            return true;
        }
        return false;
    }

    public static ItemStack extractLens(ItemStack monocle, net.minecraft.world.entity.player.Player player) {
        CustomData customData = monocle.get(DataComponents.CUSTOM_DATA);
        CompoundTag nbt = customData != null ? customData.copyTag() : new CompoundTag();
        int activeIdx = nbt.getInt("ActiveSlotIndex").orElse(0);
        
        if (activeIdx < 3) {
            String key = "Lens" + activeIdx;
            String regName = nbt.getString(key).orElse("");
            if (!regName.isEmpty()) {
                net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM
                    .getOptional(net.minecraft.resources.ResourceLocation.parse(regName))
                    .orElse(net.minecraft.world.item.Items.AIR);
                if (item != null && item != net.minecraft.world.item.Items.AIR) {
                    nbt.remove(key);
                    monocle.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
                    if (player != null) {
                        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_GENERIC.value(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 0.8f);
                    }
                    return new ItemStack(item);
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public static void cycleLens(ItemStack monocle, net.minecraft.world.entity.player.Player player) {
        CustomData customData = monocle.get(DataComponents.CUSTOM_DATA);
        CompoundTag nbt = customData != null ? customData.copyTag() : new CompoundTag();
        int activeIdx = nbt.getInt("ActiveSlotIndex").orElse(0);
        
        activeIdx = (activeIdx + 1) % 5;
        nbt.putInt("ActiveSlotIndex", activeIdx);
        monocle.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        
        if (player != null) {
            String activeLensName = "None";
            if (activeIdx < 3) {
                String key = "Lens" + activeIdx;
                String regName = nbt.getString(key).orElse("");
                if (!regName.isEmpty()) {
                    net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM
                        .getOptional(net.minecraft.resources.ResourceLocation.parse(regName))
                        .orElse(net.minecraft.world.item.Items.AIR);
                    if (item != null && item != net.minecraft.world.item.Items.AIR) {
                        activeLensName = item.getDefaultInstance().getHoverName().getString();
                    }
                }
            } else {
                activeLensName = "?????";
            }
            player.displayClientMessage(Component.literal("§5[Monocle] Active Slot " + (activeIdx + 1) + ": " + activeLensName), true);
            player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), net.minecraft.sounds.SoundSource.PLAYERS, 0.8f, 1.5f);
        }
    }

    public static ItemStack getActiveLens(net.minecraft.world.entity.player.Player player) {
        if (player == null) return ItemStack.EMPTY;
        ItemStack head = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
        if (head.is(ModItems.ARKANIST_MONOCLE.get())) {
            CustomData customData = head.get(DataComponents.CUSTOM_DATA);
            CompoundTag nbt = customData != null ? customData.copyTag() : new CompoundTag();
            int activeIdx = nbt.getInt("ActiveSlotIndex").orElse(0);
            if (activeIdx < 3) {
                String key = "Lens" + activeIdx;
                String regName = nbt.getString(key).orElse("");
                if (!regName.isEmpty()) {
                    net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM
                        .getOptional(net.minecraft.resources.ResourceLocation.parse(regName))
                        .orElse(net.minecraft.world.item.Items.AIR);
                    if (item != null && item != net.minecraft.world.item.Items.AIR) {
                        return new ItemStack(item);
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }
}
