package ddraig.net.entropica.item.biome10;

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
 * Qilin Prism Circlet — regal jade-and-gold diadem set with a Weeping Amber Tear.
 * Prevents frost buildup, slowly regenerates health under open daylight, and marks nearby attackers with Glowing.
 */
public class QilinPrismCircletItem extends Item {

    public QilinPrismCircletItem(Properties properties) {
        super(applyCircletProperties(properties));
    }

    private static Properties applyCircletProperties(Properties properties) {
        ResourceLocation modifierId = ResourceLocation.fromNamespaceAndPath("entropica", "qilin_prism_circlet_head");
        EquipmentSlotGroup slotGroup = EquipmentSlotGroup.bySlot(EquipmentSlot.HEAD);
        ItemAttributeModifiers modifiers = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(modifierId, 3.0D, AttributeModifier.Operation.ADD_VALUE), slotGroup)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(modifierId, 1.5D, AttributeModifier.Operation.ADD_VALUE), slotGroup)
                .build();

        return properties
                .stacksTo(1)
                .durability(310)
                .rarity(Rarity.RARE)
                .equippable(EquipmentSlot.HEAD)
                .attributes(modifiers);
    }

    public static boolean hasQilinPrismCirclet(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (head == null || head.isEmpty()) {
            return false;
        }
        if (head.getItem() instanceof QilinPrismCircletItem) {
            return true;
        }
        return ModItems.QILIN_PRISM_CIRCLET.isBound() && head.is(ModItems.QILIN_PRISM_CIRCLET.get());
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

        if (level.getGameTime() % 80L == 0L
                && level.isBrightOutside()
                && level.canSeeSky(living.blockPosition().above())
                && living.getHealth() < living.getMaxHealth()) {
            living.heal(1.0F);
        }

        LivingEntity attacker = living.getLastHurtByMob();
        if (attacker != null && attacker.isAlive() && attacker != living && attacker.distanceToSqr(living) <= 256.0D) {
            MobEffectInstance existing = attacker.getEffect(MobEffects.GLOWING);
            if (existing == null || existing.getDuration() < 40) {
                attacker.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0, false, true, true));
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Worn (Head): Clears frost buildup and restores 0.5 hearts every 4s in open sunlight.")
                .withStyle(ChatFormatting.GOLD));
        tooltipComponents.accept(Component.literal("Prismatic Sight: Outlines nearby attackers within 16 blocks with Glowing.")
                .withStyle(ChatFormatting.AQUA));
    }
}
