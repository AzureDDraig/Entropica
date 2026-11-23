package ddraig.net.entropica.item.biome9;

import ddraig.net.entropica.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Null-Weave Mantle — buoyant Void-Weave chest mantle reinforced with Void Crystal Shards.
 * Cushions long falls with Slow Falling, halves fall damage, and anchors the wearer against
 * Void-Stalker Orchid warps and spatial displacement traps.
 */
public class NullWeaveMantleItem extends Item {

    public NullWeaveMantleItem(Properties properties) {
        super(applyMantleProperties(properties));
    }

    private static Properties applyMantleProperties(Properties properties) {
        ResourceLocation modifierId = ResourceLocation.fromNamespaceAndPath("entropica", "null_weave_mantle_chest");
        EquipmentSlotGroup slotGroup = EquipmentSlotGroup.bySlot(EquipmentSlot.CHEST);
        ItemAttributeModifiers modifiers = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(modifierId, 5.0D, AttributeModifier.Operation.ADD_VALUE), slotGroup)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(modifierId, 1.5D, AttributeModifier.Operation.ADD_VALUE), slotGroup)
                .build();

        return properties
                .stacksTo(1)
                .durability(320)
                .equippable(EquipmentSlot.CHEST)
                .attributes(modifiers);
    }

    public static boolean hasNullWeaveMantle(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (chest == null || chest.isEmpty()) {
            return false;
        }
        if (chest.getItem() instanceof NullWeaveMantleItem) {
            return true;
        }
        return ModItems.NULL_WEAVE_MANTLE.isBound() && chest.is(ModItems.NULL_WEAVE_MANTLE.get());
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        boolean isWorn = slot == EquipmentSlot.CHEST || living.getItemBySlot(EquipmentSlot.CHEST) == stack;
        if (!isWorn) {
            return;
        }

        if (living.fallDistance > 3.0F) {
            living.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 40, 0, true, false, true));
            living.resetFallDistance();
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Worn (Chest): Cushions long falls with Slow Falling and halves fall damage.")
                .withStyle(ChatFormatting.AQUA));
        tooltipComponents.accept(Component.literal("Spatial Anchor: Prevents Void-Stalker Orchid teleports and spatial displacement.")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
