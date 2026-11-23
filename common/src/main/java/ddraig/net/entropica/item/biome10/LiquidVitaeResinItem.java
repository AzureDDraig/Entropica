package ddraig.net.entropica.item.biome10;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * Liquid Vitae Resin — warm golden tree-and-spring resin bottled from 2.10 Sun-Glazed Sanctuary.
 * Restores 6 nutrition, heals 12 HP (6 hearts), grants Regeneration, and purges Poison, Wither, and freezing.
 */
public class LiquidVitaeResinItem extends Item {

    public LiquidVitaeResinItem(Properties properties) {
        super(properties
                .stacksTo(16)
                .craftRemainder(Items.GLASS_BOTTLE)
                .usingConvertsTo(Items.GLASS_BOTTLE)
                .food(new FoodProperties.Builder()
                                .nutrition(6)
                                .saturationModifier(0.8F)
                                .alwaysEdible()
                                .build(),
                        Consumables.DEFAULT_DRINK));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide()) {
            entity.heal(12.0F);
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
            entity.removeEffect(MobEffects.POISON);
            entity.removeEffect(MobEffects.WITHER);
            entity.setTicksFrozen(0);
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Drink: Restores 6 hearts + Regeneration I (5s) and warms away frost.")
                .withStyle(ChatFormatting.GOLD));
        tooltipComponents.accept(Component.literal("Purify: Cleanses Poison and Wither. Brews Potions of Radiant Vitality.")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
