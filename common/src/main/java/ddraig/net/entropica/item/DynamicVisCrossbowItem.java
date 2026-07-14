package ddraig.net.entropica.item;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.component.VisWeaponState;
import ddraig.net.entropica.registry.ModDataComponents;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class DynamicVisCrossbowItem extends CrossbowItem {

    public DynamicVisCrossbowItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack weaponStack, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY) return false;

        ItemStack slotStack = slot.getItem();
        VisWeaponState state = weaponStack.get(ModDataComponents.VIS_WEAPON_STATE.get());
        if (state == null || !state.hasOrbisSlot()) return false;

        if (slotStack.isEmpty()) {
            if (!state.orbisCell().isEmpty()) {
                slot.set(state.orbisCell().copy());
                weaponStack.set(ModDataComponents.VIS_WEAPON_STATE.get(), state.withOrbisCell(ItemStack.EMPTY));
                player.playSound(SoundEvents.ARMOR_EQUIP_IRON.value(), 0.8F, 1.2F);
                return true;
            }
        } else if (slotStack.is(ModItems.ORBIS_CELL_ITEM.get())) {
            if (state.orbisCell().isEmpty()) {
                weaponStack.set(ModDataComponents.VIS_WEAPON_STATE.get(), state.withOrbisCell(slotStack.copyWithCount(1)));
                slotStack.shrink(1);
                player.playSound(SoundEvents.ARMOR_EQUIP_GOLD.value(), 0.8F, 0.8F);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack weaponStack, ItemStack otherStack, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) return false;

        VisWeaponState state = weaponStack.get(ModDataComponents.VIS_WEAPON_STATE.get());
        if (state == null || !state.hasOrbisSlot()) return false;

        if (otherStack.isEmpty()) {
            if (!state.orbisCell().isEmpty()) {
                access.set(state.orbisCell().copy());
                weaponStack.set(ModDataComponents.VIS_WEAPON_STATE.get(), state.withOrbisCell(ItemStack.EMPTY));
                player.playSound(SoundEvents.ARMOR_EQUIP_IRON.value(), 0.8F, 1.2F);
                return true;
            }
        } else if (otherStack.is(ModItems.ORBIS_CELL_ITEM.get())) {
            if (state.orbisCell().isEmpty()) {
                weaponStack.set(ModDataComponents.VIS_WEAPON_STATE.get(), state.withOrbisCell(otherStack.copyWithCount(1)));
                otherStack.shrink(1);
                player.playSound(SoundEvents.ARMOR_EQUIP_GOLD.value(), 0.8F, 0.8F);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        VisWeaponState state = stack.get(ModDataComponents.VIS_WEAPON_STATE.get());
        return state != null && state.hasOrbisSlot() && !state.orbisCell().isEmpty();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        VisWeaponState state = stack.get(ModDataComponents.VIS_WEAPON_STATE.get());
        if (state != null && state.hasOrbisSlot() && !state.orbisCell().isEmpty()) {
            ItemStack cell = state.orbisCell();
            net.minecraft.world.item.component.CustomData data = cell.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY);
            int stored = data.copyTag().getInt("StoredVis").orElse(0);
            int max = 10000;
            if (stored > 0) {
                return Math.round(13.0f * ((float)stored / max));
            }
            return 13;
        }
        return 0;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x00FFFF;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.translatable("tooltip.entropica.dynamic_weapon_desc").withStyle(ChatFormatting.DARK_PURPLE));

        if (stack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)) {
            tooltipComponents.accept(Component.literal("A weapon bound to the soul...").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }

        if (stack.has(ModDataComponents.VIS_WEAPON_STATE.get())) {
            VisWeaponState state = stack.get(ModDataComponents.VIS_WEAPON_STATE.get());
            if (state != null && state.baseType() != null) {
                tooltipComponents.accept(Component.empty());

                if (state.isAutonomous()) {
                    tooltipComponents.accept(Component.literal("⚔ Autonomous Eidolic Weapon").withStyle(ChatFormatting.GOLD));
                    tooltipComponents.accept(Component.literal(" Requires: Autonomous Weapon Sheath").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                } else {
                    tooltipComponents.accept(Component.literal("Forged in the Eidolic Lathe").withStyle(ChatFormatting.LIGHT_PURPLE));
                }

                tooltipComponents.accept(Component.literal(" Core Type: ").withStyle(ChatFormatting.GRAY).append(Component.literal(state.coreType()).withStyle(ChatFormatting.WHITE)));

                if (state.isAltered()) {
                    tooltipComponents.accept(Component.literal(" Active Profile: " + state.activeProfile())
                            .withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
                }

                if (state.baseType() != EssenceType.REGULAR && state.baseDamage() > 0) {
                    String rawName = state.baseType().name();
                    String essenceNameStr = rawName.substring(0, 1).toUpperCase() + rawName.substring(1).toLowerCase();
                    Component damageValue = Component.literal(String.format(" %.1f ", state.baseDamage())).withStyle(ChatFormatting.DARK_GREEN);
                    Component essenceName = Component.literal(essenceNameStr).withStyle(style -> style.withColor(state.baseType().getColorInt()));
                    Component damageLabel = Component.literal(" Damage").withStyle(ChatFormatting.DARK_GREEN);
                    tooltipComponents.accept(Component.empty().append(damageValue).append(essenceName).append(damageLabel));
                } else {
                    Component damageValue = Component.literal(String.format(" %.1f Physical Damage", state.baseDamage())).withStyle(ChatFormatting.DARK_GREEN);
                    tooltipComponents.accept(Component.empty().append(damageValue));
                }

                tooltipComponents.accept(Component.literal(String.format(" %.2f Draw Speed", state.absoluteSpeed())).withStyle(ChatFormatting.DARK_GREEN));

                tooltipComponents.accept(Component.literal(" Augment Slots: " + state.slottedFragments().size() + " / " + state.innateSlots())
                        .withStyle(ChatFormatting.GOLD));

                if (state.hasOrbisSlot()) {
                    if (state.orbisCell().isEmpty()) {
                        tooltipComponents.accept(Component.literal(" No Orbis Cell Equipped").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                    } else {
                        ItemStack cell = state.orbisCell();
                        net.minecraft.world.item.component.CustomData data = cell.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY);

                        // Cleanly unwraps the Optional using orElse!
                        int storedVis = data.copyTag().getInt("StoredVis").orElse(0);

                        String typeStr = data.copyTag().getString("EssenceType").orElse("REGULAR");
                        EssenceType parsedType;
                        try { parsedType = EssenceType.valueOf(typeStr); } catch (Exception e) { parsedType = EssenceType.REGULAR; }

                        // Assign to a final variable to satisfy Java's lambda requirements!
                        final EssenceType cellType = parsedType;

                        int bars = Math.min(20, storedVis / 500);
                        String filled = "|".repeat(bars);
                        String empty = "|".repeat(20 - bars);

                        tooltipComponents.accept(Component.literal(" Stored Vis ").withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(filled).withStyle(style -> style.withColor(cellType.getColorInt())))
                                .append(Component.literal(empty).withStyle(ChatFormatting.DARK_GRAY))
                        );
                    }
                }

                if (!state.activeSpell().equals("none")) {
                    tooltipComponents.accept(Component.empty());
                    tooltipComponents.accept(Component.literal(" Embedded Spell: ").withStyle(ChatFormatting.LIGHT_PURPLE)
                            .append(Component.literal(state.activeSpell()).withStyle(ChatFormatting.GOLD)));
                    tooltipComponents.accept(Component.literal(" Shift + Right-Click to Cast").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                }
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) { return true; }
}