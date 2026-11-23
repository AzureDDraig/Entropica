package ddraig.net.entropica.item.biome12;

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
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Spore-Filter Mask — copper, Spore Glass, and Mossy Aegis Plate respirator helm.
 * Blocks Nausea from spore hazards and halves the remaining duration of Poison while worn.
 */
public class SporeFilterMaskItem extends Item {

    public SporeFilterMaskItem(Properties properties) {
        super(applyMaskProperties(properties));
    }

    private static Properties applyMaskProperties(Properties properties) {
        ResourceLocation modifierId = ResourceLocation.fromNamespaceAndPath("entropica", "spore_filter_mask_head");
        EquipmentSlotGroup slotGroup = EquipmentSlotGroup.bySlot(EquipmentSlot.HEAD);
        ItemAttributeModifiers modifiers = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(modifierId, 2.0D, AttributeModifier.Operation.ADD_VALUE), slotGroup)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(modifierId, 1.0D, AttributeModifier.Operation.ADD_VALUE), slotGroup)
                .build();

        return properties
                .stacksTo(1)
                .durability(325)
                .rarity(Rarity.UNCOMMON)
                .equippable(EquipmentSlot.HEAD)
                .attributes(modifiers);
    }

    public static boolean hasSporeFilterMask(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (head == null || head.isEmpty()) {
            return false;
        }
        if (head.getItem() instanceof SporeFilterMaskItem) {
            return true;
        }
        return ModItems.SPORE_FILTER_MASK.isBound() && head.is(ModItems.SPORE_FILTER_MASK.get());
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        boolean isWorn = slot == EquipmentSlot.HEAD || living.getItemBySlot(EquipmentSlot.HEAD) == stack;
        if (!isWorn) {
            return;
        }

        if (living.hasEffect(MobEffects.NAUSEA)) {
            living.removeEffect(MobEffects.NAUSEA);
        }

        if (living.hasEffect(MobEffects.POISON) && level.getGameTime() % 2L == 0L) {
            MobEffectInstance poison = living.getEffect(MobEffects.POISON);
            if (poison != null) {
                int remaining = poison.getDuration();
                living.removeEffect(MobEffects.POISON);
                if (remaining > 2) {
                    living.addEffect(new MobEffectInstance(
                            MobEffects.POISON,
                            remaining - 2,
                            poison.getAmplifier(),
                            poison.isAmbient(),
                            poison.isVisible(),
                            poison.showIcon()
                    ));
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Worn (Head): Prevents Nausea from spore clouds and Frozen Spore Moss.")
                .withStyle(ChatFormatting.GREEN));
        tooltipComponents.accept(Component.literal("Purifying Filter: Drains active Poison twice as fast.")
                .withStyle(ChatFormatting.AQUA));
    }
}
