package ddraig.net.entropica.item.biome16;

import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * Sanguine Draught Ampoule — restorative Blood, Glacial, and Vitae Materia tonic sealed in a Medium Ampoule.
 * Drinking restores 3 Hearts (6.0 HP), purges Weakness and Wither, grants Sanguine Vigor (45s),
 * and returns an empty Medium Ampoule.
 */
public class SanguineDraughtAmpouleItem extends Item {

    public static final float INSTANT_HEAL_AMOUNT = 6.0F;
    public static final int SANGUINE_VIGOR_DURATION_TICKS = 900;

    public SanguineDraughtAmpouleItem(Properties properties) {
        super(applyProperties(properties));
    }

    private static Properties applyProperties(Properties properties) {
        Properties configured = properties
                .stacksTo(16)
                .component(DataComponents.CONSUMABLE, Consumables.DEFAULT_DRINK);
        if (ModItems.MEDIUM_AMPOULE.isBound()) {
            configured = configured
                    .craftRemainder(ModItems.MEDIUM_AMPOULE.get())
                    .usingConvertsTo(ModItems.MEDIUM_AMPOULE.get());
        }
        return configured;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        boolean hadUseRemainder = stack.has(DataComponents.USE_REMAINDER);
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide()) {
            entity.heal(INSTANT_HEAL_AMOUNT);
            if (entity.getTicksFrozen() > 0) {
                entity.setTicksFrozen(0);
            }
            if (entity.hasEffect(MobEffects.WEAKNESS)) {
                entity.removeEffect(MobEffects.WEAKNESS);
            }
            if (entity.hasEffect(MobEffects.WITHER)) {
                entity.removeEffect(MobEffects.WITHER);
            }
            Holder<MobEffect> vigorHolder = resolveSanguineVigorHolder();
            if (vigorHolder != null) {
                entity.addEffect(new MobEffectInstance(vigorHolder, SANGUINE_VIGOR_DURATION_TICKS, 0));
            }
        }

        if (!hadUseRemainder && !(entity instanceof Player pl && pl.getAbilities().instabuild)) {
            Item emptyAmpouleItem = ModItems.MEDIUM_AMPOULE.isBound()
                    ? ModItems.MEDIUM_AMPOULE.get()
                    : BuiltInRegistries.ITEM
                            .getOptional(ResourceLocation.fromNamespaceAndPath("entropica", "medium_ampoule"))
                            .orElse(null);
            if (emptyAmpouleItem != null) {
                ItemStack emptyAmpoule = new ItemStack(emptyAmpouleItem);
                if (result.isEmpty()) {
                    return emptyAmpoule;
                }
                if (entity instanceof Player player && !player.getInventory().add(emptyAmpoule)) {
                    player.drop(emptyAmpoule, false);
                }
            }
        }

        return result;
    }

    private static Holder<MobEffect> resolveSanguineVigorHolder() {
        if (ModEffects.SANGUINE_VIGOR.isBound()) {
            return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.SANGUINE_VIGOR.get());
        }
        MobEffect fallback = BuiltInRegistries.MOB_EFFECT
                .getOptional(ResourceLocation.fromNamespaceAndPath("entropica", "sanguine_vigor"))
                .orElse(null);
        return fallback != null ? BuiltInRegistries.MOB_EFFECT.wrapAsHolder(fallback) : null;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Drink: Restores 3 Hearts, purges Weakness & Wither, and grants Sanguine Vigor (45s).")
                .withStyle(ChatFormatting.GOLD));
        tooltipComponents.accept(Component.literal("Sanguine Vigor: +2 Hearts Max Health, frost immunity, and melee life-siphon; returns Medium Ampoule.")
                .withStyle(ChatFormatting.AQUA));
    }
}
