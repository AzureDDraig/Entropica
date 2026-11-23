package ddraig.net.entropica.item.biome6;

import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
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
 * Vapor-Shroud Cloak — geothermal lichen mantle native to 2.6 Steaming Skerries.
 * - Equippable in EquipmentSlot.CHEST (durability 240).
 * - Grants seamless Invisibility and subtle mist trails while sneaking.
 * - Dampens footsteps and world vibrations while worn.
 */
public class VaporShroudCloakItem extends Item {

    public VaporShroudCloakItem(Properties properties) {
        super(properties.stacksTo(1).durability(240).equippable(EquipmentSlot.CHEST));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        boolean wornOnChest = slot == EquipmentSlot.CHEST
                || living.getItemBySlot(EquipmentSlot.CHEST) == stack;
        if (!wornOnChest) {
            return;
        }

        // Maintain seamless vibration & footstep dampening while worn
        var data = EntityHelper.getPersistentData(living);
        long now = level.getGameTime();
        if (data.getLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL).orElse(0L) < now + 5L) {
            data.putLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, now + 5L);
        }

        if (living.isShiftKeyDown() || living.isCrouching()) {
            living.addEffect(new MobEffectInstance(
                    MobEffects.INVISIBILITY,
                    30,
                    0,
                    true,
                    false,
                    true
            ));

            if (now % 12L == 0L) {
                level.sendParticles(ParticleTypes.CLOUD,
                        living.getX(), living.getY() + 0.5D, living.getZ(),
                        2, 0.25D, 0.25D, 0.25D, 0.005D);
            }
        }
    }

    public static boolean isWearingVaporShroudCloak(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (chest == null || chest.isEmpty()) {
            return false;
        }
        if (chest.getItem() instanceof VaporShroudCloakItem) {
            return true;
        }
        return ModItems.VAPOR_SHROUD_CLOAK.isBound() && chest.is(ModItems.VAPOR_SHROUD_CLOAK.get());
    }

    /**
     * Returns true if {@code entity} is wearing the Vapor-Shroud Cloak on their chest
     * or currently has the Dampening status effect active.
     */
    public static boolean hasSoundDampening(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        if (isWearingVaporShroudCloak(entity)) {
            return true;
        }
        if (ModEffects.DAMPENING.isBound()) {
            MobEffect dampening = ModEffects.DAMPENING.get();
            for (MobEffectInstance inst : entity.getActiveEffects()) {
                if (inst.getEffect().value() == dampening) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Worn (Chest): Grants mist invisibility while sneaking.")
                .withStyle(ChatFormatting.AQUA));
        tooltipComponents.accept(Component.literal("Stealth: Muffles footsteps and dampens vibrations.")
                .withStyle(ChatFormatting.GRAY));
    }
}
