package ddraig.net.entropica.item.biome14;

import ddraig.net.entropica.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
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
 * Salamander Wader Hood — insulated Waterproof Salamander Skin, copper, and Oasis Glass diving cowl.
 * Prevents freezing buildup, extends underwater breath supply by +50%, and grants smooth wading swim grace.
 */
public class SalamanderWaderHoodItem extends Item {

    public SalamanderWaderHoodItem(Properties properties) {
        super(applyHoodProperties(properties));
    }

    private static Properties applyHoodProperties(Properties properties) {
        ResourceLocation modifierId = ResourceLocation.fromNamespaceAndPath("entropica", "salamander_wader_hood_head");
        EquipmentSlotGroup slotGroup = EquipmentSlotGroup.bySlot(EquipmentSlot.HEAD);
        ItemAttributeModifiers modifiers = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(modifierId, 2.0D, AttributeModifier.Operation.ADD_VALUE), slotGroup)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(modifierId, 1.0D, AttributeModifier.Operation.ADD_VALUE), slotGroup)
                .build();

        return properties
                .stacksTo(1)
                .durability(320)
                .rarity(Rarity.UNCOMMON)
                .equippable(EquipmentSlot.HEAD)
                .attributes(modifiers);
    }

    public static boolean hasSalamanderWaderHood(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (head == null || head.isEmpty()) {
            return false;
        }
        if (head.getItem() instanceof SalamanderWaderHoodItem) {
            return true;
        }
        if (ModItems.SALAMANDER_WADER_HOOD.isBound() && head.is(ModItems.SALAMANDER_WADER_HOOD.get())) {
            return true;
        }
        return ResourceLocation.fromNamespaceAndPath("entropica", "salamander_wader_hood")
                .equals(BuiltInRegistries.ITEM.getKey(head.getItem()));
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

        if (living.getTicksFrozen() > 0) {
            living.setTicksFrozen(0);
        }

        if (living.isEyeInFluid(FluidTags.WATER)) {
            if (level.getGameTime() % 3L == 0L && living.getAirSupply() < living.getMaxAirSupply()) {
                living.setAirSupply(Math.min(living.getMaxAirSupply(), living.getAirSupply() + 1));
            }
        }

        if (living.isInWater() && level.getGameTime() % 20L == 0L) {
            living.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 60, 0, true, false, true));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Worn (Head): Prevents frost buildup in icy waters and blizzards.")
                .withStyle(ChatFormatting.GOLD));
        tooltipComponents.accept(Component.literal("Amphibious Wader: Extends underwater air by +50% and grants Dolphin's Grace.")
                .withStyle(ChatFormatting.AQUA));
    }
}
