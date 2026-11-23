package ddraig.net.entropica.item.biome13;

import ddraig.net.entropica.effect.DoubleVisionEffect;
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

import java.util.List;
import java.util.function.Consumer;

/**
 * Mirage-Veil Goggles — copper, leather, and Silica Glass Plate optical lenses.
 * Prevents Mirage Ice from phasing out underfoot, immediately clears Double Vision,
 * Blindness, and Darkness, and outlines nearby invisible creatures with Glowing.
 */
public class MirageVeilGogglesItem extends Item {

    public MirageVeilGogglesItem(Properties properties) {
        super(applyGogglesProperties(properties));
    }

    private static Properties applyGogglesProperties(Properties properties) {
        ResourceLocation modifierId = ResourceLocation.fromNamespaceAndPath("entropica", "mirage_veil_goggles_head");
        EquipmentSlotGroup slotGroup = EquipmentSlotGroup.bySlot(EquipmentSlot.HEAD);
        ItemAttributeModifiers modifiers = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(modifierId, 2.0D, AttributeModifier.Operation.ADD_VALUE), slotGroup)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(modifierId, 1.0D, AttributeModifier.Operation.ADD_VALUE), slotGroup)
                .build();

        return properties
                .stacksTo(1)
                .durability(340)
                .rarity(Rarity.RARE)
                .equippable(EquipmentSlot.HEAD)
                .attributes(modifiers);
    }

    public static boolean hasMirageVeilGoggles(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (head == null || head.isEmpty()) {
            return false;
        }
        if (head.getItem() instanceof MirageVeilGogglesItem) {
            return true;
        }
        return ModItems.MIRAGE_VEIL_GOGGLES.isBound() && head.is(ModItems.MIRAGE_VEIL_GOGGLES.get());
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

        DoubleVisionEffect.removeDoubleVision(living);
        if (living.hasEffect(MobEffects.BLINDNESS)) {
            living.removeEffect(MobEffects.BLINDNESS);
        }
        if (living.hasEffect(MobEffects.DARKNESS)) {
            living.removeEffect(MobEffects.DARKNESS);
        }

        if (level.getGameTime() % 40L == 0L) {
            double maxDistSq = 16.0D * 16.0D;
            List<LivingEntity> unseen = level.getEntitiesOfClass(
                    LivingEntity.class,
                    living.getBoundingBox().inflate(16.0D),
                    other -> other != living
                            && other.isAlive()
                            && !other.isSpectator()
                            && (other.isInvisible() || other.hasEffect(MobEffects.INVISIBILITY))
                            && other.distanceToSqr(living) <= maxDistSq
            );
            for (LivingEntity other : unseen) {
                other.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0, false, true, true));
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Worn (Head): Clears Double Vision, Blindness, and Darkness and keeps Mirage Ice solid underfoot.")
                .withStyle(ChatFormatting.GOLD));
        tooltipComponents.accept(Component.literal("True Sight: Outlines invisible creatures within 16 blocks with Glowing every 2s.")
                .withStyle(ChatFormatting.AQUA));
    }
}
