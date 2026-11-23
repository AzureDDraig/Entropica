package ddraig.net.entropica.item.biome17;

import ddraig.net.entropica.Entropica;
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
 * Starlight Draught Ampoule — luminous Astral and Glacial Materia tonic sealed in a Medium Ampoule.
 * Drinking (32 ticks) restores 2 Hearts (4.0 HP), clears Darkness, Blindness, Slowness, and freeze buildup,
 * grants Starlight Grace (60s), and returns an empty Medium Ampoule.
 */
public class StarlightDraughtAmpouleItem extends Item {

    public static final float INSTANT_HEAL_AMOUNT = 4.0F;
    public static final int STARLIGHT_GRACE_DURATION_TICKS = 1200;

    public StarlightDraughtAmpouleItem(Properties properties) {
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
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
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
            if (entity.hasEffect(MobEffects.DARKNESS)) {
                entity.removeEffect(MobEffects.DARKNESS);
            }
            if (entity.hasEffect(MobEffects.BLINDNESS)) {
                entity.removeEffect(MobEffects.BLINDNESS);
            }
            if (entity.hasEffect(MobEffects.SLOWNESS)) {
                entity.removeEffect(MobEffects.SLOWNESS);
            }
            Holder<MobEffect> graceHolder = resolveStarlightGraceHolder();
            if (graceHolder != null) {
                entity.addEffect(new MobEffectInstance(graceHolder, STARLIGHT_GRACE_DURATION_TICKS, 0));
            }
        }

        if (!hadUseRemainder && !(entity instanceof Player pl && pl.getAbilities().instabuild)) {
            Item emptyAmpouleItem = ModItems.MEDIUM_AMPOULE.isBound()
                    ? ModItems.MEDIUM_AMPOULE.get()
                    : BuiltInRegistries.ITEM
                            .getOptional(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "medium_ampoule"))
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

    public static Holder<MobEffect> resolveStarlightGraceHolder() {
        if (ModEffects.STARLIGHT_GRACE.isBound()) {
            return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.STARLIGHT_GRACE.get());
        }
        MobEffect fallback = BuiltInRegistries.MOB_EFFECT
                .getOptional(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "starlight_grace"))
                .orElse(null);
        return fallback != null ? BuiltInRegistries.MOB_EFFECT.wrapAsHolder(fallback) : null;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Drink: Restores 2 Hearts, clears Freeze, Darkness, Blindness & Slowness, and grants Starlight Grace (60s).")
                .withStyle(ChatFormatting.GOLD));
        tooltipComponents.accept(Component.literal("Starlight Grace: +0.5 step height, cushioned falls, +12% speed & steady Night Vision; returns Medium Ampoule.")
                .withStyle(ChatFormatting.AQUA));
    }
}
