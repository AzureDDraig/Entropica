package ddraig.net.entropica.item.biome2;

import ddraig.net.entropica.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Camouflage Cloak — light mist-woven mantle crafted from Mist-Veil Fronds.
 * Grants subtle mist stealth (Invisibility while sneaking) when worn on the chest
 * or held in the offhand, and halves hostile mob detection range.
 */
public class CamouflageCloakItem extends Item {

    public CamouflageCloakItem(Properties properties) {
        super(properties.stacksTo(1).durability(240).equippable(EquipmentSlot.CHEST));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        boolean activeSlot = slot == EquipmentSlot.CHEST
                || slot == EquipmentSlot.OFFHAND
                || living.getItemBySlot(EquipmentSlot.CHEST) == stack
                || living.getOffhandItem() == stack;

        if (!activeSlot) {
            return;
        }

        if (living.isCrouching() || living.isShiftKeyDown()) {
            living.addEffect(new MobEffectInstance(
                    MobEffects.INVISIBILITY,
                    40,
                    0,
                    true,
                    false,
                    true
            ));

            if (level.getGameTime() % 10L == 0L) {
                level.sendParticles(ParticleTypes.CLOUD,
                        living.getX(), living.getY() + 0.5D, living.getZ(),
                        2, 0.25D, 0.25D, 0.25D, 0.005D);
            }
        }
    }

    /**
     * Returns true if {@code entity} is wearing the Camouflage Cloak in their chest slot
     * or holding it in their offhand (-50% hostile detection/aggro visibility).
     */
    public static boolean hasCamouflageCloak(LivingEntity entity) {
        if (entity == null || !ModItems.CAMOUFLAGE_CLOAK.isBound()) {
            return false;
        }
        Item cloak = ModItems.CAMOUFLAGE_CLOAK.get();
        return entity.getItemBySlot(EquipmentSlot.CHEST).is(cloak)
                || entity.getOffhandItem().is(cloak);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Worn or Offhand: Shrouds you in warm mist while sneaking and halves mob detection range.")
                .withStyle(ChatFormatting.AQUA));
    }
}
