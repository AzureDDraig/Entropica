package ddraig.net.entropica.item.biome1;

import net.minecraft.ChatFormatting;
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
 * Aegis Shielding Lining — crystalline defensive lining crafted from Glacial Aegis Plates
 * and Aegis Crystal Shards. Grants passive Resistance I while carried in offhand or inventory
 * and shields against Runic Spire hypothermia.
 */
public class AegisShieldingLiningItem extends Item {

    public AegisShieldingLiningItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (entity instanceof LivingEntity living && living.isAlive()) {
            if (level.getGameTime() % 20L == 0L) {
                living.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 60, 0, true, false, true));
            }
            if (living.getTicksFrozen() > 0) {
                living.setTicksFrozen(Math.max(0, living.getTicksFrozen() - 4));
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Passive: Grants Resistance I and Aegis Frost Protection while carried.")
                .withStyle(ChatFormatting.AQUA));
    }
}
