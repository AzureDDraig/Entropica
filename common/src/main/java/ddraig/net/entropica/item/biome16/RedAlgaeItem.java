package ddraig.net.entropica.item.biome16;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

/**
 * Red Algae — edible & placeable mucilaginous crimson algae mat from 2.16 Blood-Ice Shallows.
 * Clicking a valid surface places the algae mat; clicking in air or an invalid surface eats it,
 * halving active Poison and Wither durations and granting Regeneration I (6s).
 */
public class RedAlgaeItem extends BlockItem {

    public static final FoodProperties RED_ALGAE_FOOD = new FoodProperties.Builder()
            .nutrition(3)
            .saturationModifier(0.6F)
            .alwaysEdible()
            .build();

    public RedAlgaeItem(Block block, Properties properties) {
        super(block, properties.food(RED_ALGAE_FOOD));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionResult placeResult = super.useOn(context);
        if (!placeResult.consumesAction() && context.getPlayer() != null) {
            return this.use(context.getLevel(), context.getPlayer(), context.getHand());
        }
        return placeResult;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide()) {
            halveEffectDuration(entity, MobEffects.POISON);
            halveEffectDuration(entity, MobEffects.WITHER);
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 0));
        }
        return result;
    }

    private static void halveEffectDuration(LivingEntity entity, Holder<MobEffect> effect) {
        if (entity.hasEffect(effect)) {
            MobEffectInstance cur = entity.getEffect(effect);
            entity.removeEffect(effect);
            if (cur != null && cur.getDuration() > 40) {
                entity.addEffect(new MobEffectInstance(
                        effect,
                        cur.getDuration() / 2,
                        cur.getAmplifier(),
                        cur.isAmbient(),
                        cur.isVisible(),
                        cur.showIcon()
                ));
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Eat: Restores 3 hunger, grants Regeneration I (6s), and halves Poison & Wither time.")
                .withStyle(ChatFormatting.GOLD));
        tooltipComponents.accept(Component.literal("Craft & Place: Spreads with Bone Meal; substitutes for Slimeballs and Red Dye.")
                .withStyle(ChatFormatting.AQUA));
    }
}
