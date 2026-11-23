package ddraig.net.entropica.item.biome14;

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
 * Bottled Amber Nectar — warm golden spring nectar drawn from infused Oasis Dew Basins in 2.14 Oasis Floes.
 * Restores 4 nutrition, clears freeze, Poison, and Weakness, and grants Absorption I (30s) + Regeneration I (8s).
 */
public class AmberNectarBottleItem extends Item {

    public AmberNectarBottleItem(Properties properties) {
        super(properties
                .stacksTo(16)
                .craftRemainder(Items.GLASS_BOTTLE)
                .usingConvertsTo(Items.GLASS_BOTTLE)
                .food(new FoodProperties.Builder()
                                .nutrition(4)
                                .saturationModifier(0.7F)
                                .alwaysEdible()
                                .build(),
                        Consumables.DEFAULT_DRINK));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide()) {
            entity.setTicksFrozen(0);
            entity.removeEffect(MobEffects.POISON);
            entity.removeEffect(MobEffects.WEAKNESS);
            entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 0));
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 0));
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Drink: Grants Absorption I (30s) + Regeneration I (8s) and melts away frost.")
                .withStyle(ChatFormatting.GOLD));
        tooltipComponents.accept(Component.literal("Purify: Cleanses Poison and Weakness. Brews Potions of Oasis Renewal.")
                .withStyle(ChatFormatting.AQUA));
    }
}
