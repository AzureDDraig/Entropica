package ddraig.net.entropica.item;

import ddraig.net.entropica.gravity.GravityApi;
import ddraig.net.entropica.registry.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class GravitonWandItem extends Item {

    public GravitonWandItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                double currentG = GravityApi.getEffectiveGravity(player);
                double nextG;
                String modeKey;
                if (currentG > 0.05) { // Normal -> Moon
                    nextG = GravityApi.MOON_GRAVITY;
                    modeKey = "message.entropica.gravity.moon";
                } else if (currentG > 0.01) { // Moon -> Zero-G
                    nextG = GravityApi.ZERO_GRAVITY;
                    modeKey = "message.entropica.gravity.zero_g";
                } else if (currentG >= -0.01) { // Zero-G -> Inverted
                    nextG = GravityApi.INVERTED_GRAVITY;
                    modeKey = "message.entropica.gravity.inverted";
                } else { // Inverted -> Normal
                    nextG = GravityApi.VANILLA_BASE_GRAVITY;
                    modeKey = "message.entropica.gravity.normal";
                }

                GravityApi.setGravity(player, nextG);
                player.displayClientMessage(Component.translatable(modeKey), true);
            }
            player.playSound(SoundEvents.BEACON_POWER_SELECT, 1.0F, 1.5F);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        Level level = player.level();
        if (!level.isClientSide()) {
            if (player.isShiftKeyDown()) {
                // Apply Gravitational Crush
                interactionTarget.addEffect(new MobEffectInstance(ModEffects.GRAVITATIONAL_CRUSH, 200, 0));
                player.displayClientMessage(Component.translatable("message.entropica.wand.crush", interactionTarget.getDisplayName()), true);
            } else {
                // Apply Weightlessness (Zero-G, amplifier 1)
                interactionTarget.addEffect(new MobEffectInstance(ModEffects.WEIGHTLESSNESS, 200, 1));
                player.displayClientMessage(Component.translatable("message.entropica.wand.zero_g", interactionTarget.getDisplayName()), true);
            }
        }
        player.playSound(SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), 1.0F, 1.2F);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.translatable("tooltip.entropica.graviton_wand.desc_1")
                .withStyle(ChatFormatting.AQUA));
        tooltipComponents.accept(Component.translatable("tooltip.entropica.graviton_wand.desc_2")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltipComponents.accept(Component.translatable("tooltip.entropica.graviton_wand.desc_3")
                .withStyle(ChatFormatting.GRAY));
    }
}