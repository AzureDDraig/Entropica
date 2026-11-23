package ddraig.net.entropica.item.biome8;

import ddraig.net.entropica.block.biome8.CryptGraveRootBlock;
import ddraig.net.entropica.block.biome8.GraveMossBlock;
import ddraig.net.entropica.event.BarrowCombatHandler;
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
 * Barrow-Slate Greaves — heavy tomb-forged legplates plated with Barrow Skull-Plates.
 * Protects legs from Crypt-Grave Roots and Grave-Moss hunger drain and reduces damage from Undead & Barrow Hounds.
 */
public class BarrowSlateGreavesItem extends Item {

    public BarrowSlateGreavesItem(Properties properties) {
        super(applyGreavesProperties(properties));
    }

    private static Properties applyGreavesProperties(Properties properties) {
        ResourceLocation modifierId = ResourceLocation.fromNamespaceAndPath("entropica", "barrow_slate_greaves_legs");
        EquipmentSlotGroup slotGroup = EquipmentSlotGroup.bySlot(EquipmentSlot.LEGS);
        ItemAttributeModifiers modifiers = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(modifierId, 6.0D, AttributeModifier.Operation.ADD_VALUE), slotGroup)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(modifierId, 1.5D, AttributeModifier.Operation.ADD_VALUE), slotGroup)
                .add(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(modifierId, 0.05D, AttributeModifier.Operation.ADD_VALUE), slotGroup)
                .build();

        return properties
                .stacksTo(1)
                .durability(315)
                .equippable(EquipmentSlot.LEGS)
                .attributes(modifiers);
    }

    public static boolean hasBarrowSlateGreaves(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        ItemStack legs = entity.getItemBySlot(EquipmentSlot.LEGS);
        if (legs == null || legs.isEmpty()) {
            return false;
        }
        if (legs.getItem() instanceof BarrowSlateGreavesItem) {
            return true;
        }
        return ModItems.BARROW_SLATE_GREAVES.isBound() && legs.is(ModItems.BARROW_SLATE_GREAVES.get());
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        boolean isWorn = slot == EquipmentSlot.LEGS || living.getItemBySlot(EquipmentSlot.LEGS) == stack;
        if (!isWorn) {
            return;
        }

        if (GraveMossBlock.isStandingOnGraveMoss(level, living)) {
            BarrowCombatHandler.resetGraveMossTimer(living.getUUID());
            if (living.hasEffect(MobEffects.HUNGER)) {
                living.removeEffect(MobEffects.HUNGER);
            }
        }

        if (CryptGraveRootBlock.isStandingOnCryptGraveRoot(level, living)) {
            MobEffectInstance slowness = living.getEffect(MobEffects.SLOWNESS);
            if (slowness != null && slowness.getAmplifier() >= 3) {
                living.removeEffect(MobEffects.SLOWNESS);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Worn: Walk safely over Crypt-Grave Roots and Grave-Moss; -20% damage from Undead & Barrow Hounds.")
                .withStyle(ChatFormatting.AQUA));
    }
}
