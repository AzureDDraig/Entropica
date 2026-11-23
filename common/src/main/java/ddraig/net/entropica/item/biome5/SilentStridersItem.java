package ddraig.net.entropica.item.biome5;

import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
 * Silent Striders — Eclipse-moss padded stealth boots native to 2.5 Silent Fjord.
 * - Muffles footsteps and dampens world vibrations when worn.
 * - Reduces hostile monster detection range by 35%.
 * - Gradually sheds creeping Fjord Shadow Chill freeze buildup outside powder snow.
 */
public class SilentStridersItem extends Item {

    public SilentStridersItem(Properties properties) {
        super(applyBootProperties(properties));
    }

    private static Properties applyBootProperties(Properties properties) {
        ResourceLocation modifierId = ResourceLocation.fromNamespaceAndPath("entropica", "silent_striders_feet");
        EquipmentSlotGroup slotGroup = EquipmentSlotGroup.bySlot(EquipmentSlot.FEET);
        ItemAttributeModifiers modifiers = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(modifierId, 2.0D, AttributeModifier.Operation.ADD_VALUE), slotGroup)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(modifierId, 1.0D, AttributeModifier.Operation.ADD_VALUE), slotGroup)
                .build();

        return properties
                .stacksTo(1)
                .durability(360)
                .equippable(EquipmentSlot.FEET)
                .attributes(modifiers);
    }

    public static boolean hasSilentStriders(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        ItemStack feet = entity.getItemBySlot(EquipmentSlot.FEET);
        if (feet == null || feet.isEmpty()) {
            return false;
        }
        if (feet.getItem() instanceof SilentStridersItem) {
            return true;
        }
        return ModItems.SILENT_STRIDERS.isBound() && feet.is(ModItems.SILENT_STRIDERS.get());
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        boolean isWorn = slot == EquipmentSlot.FEET || living.getItemBySlot(EquipmentSlot.FEET) == stack;
        if (!isWorn) {
            return;
        }

        if (living.getTicksFrozen() > 0 && !living.isInPowderSnow) {
            living.setTicksFrozen(Math.max(0, living.getTicksFrozen() - 4));
        }

        var data = EntityHelper.getPersistentData(living);
        long now = level.getGameTime();
        if (data.getLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL).orElse(0L) < now + 5L) {
            data.putLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, now + 5L);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Worn: Muffles footsteps and dampens acoustic vibrations.")
                .withStyle(ChatFormatting.DARK_PURPLE));
        tooltipComponents.accept(Component.literal("Stealth: Reduces hostile monster detection range by 35%.")
                .withStyle(ChatFormatting.GRAY));
        tooltipComponents.accept(Component.literal("Insulation: Sheds creeping Silent Fjord Shadow Chill freeze buildup.")
                .withStyle(ChatFormatting.AQUA));
    }
}
