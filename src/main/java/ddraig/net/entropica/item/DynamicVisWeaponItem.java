package ddraig.net.entropica.item;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.component.VisWeaponState;
import ddraig.net.entropica.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class DynamicVisWeaponItem extends Item {

    public DynamicVisWeaponItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.has(ModDataComponents.VIS_WEAPON_STATE.get())) {
            VisWeaponState state = stack.get(ModDataComponents.VIS_WEAPON_STATE.get());

            // Trigger embedded Spell Gem ability if it exists
            if (state != null && !state.activeSpell().equals("none")) {
                if (!level.isClientSide()) {
                    // TODO: Replace with dynamic spell casting execution based on state.activeSpell() ID
                    player.displayClientMessage(Component.literal("§dCasting Spell: " + state.activeSpell() + " (Reduced Potency)"), true);
                    level.playSound(null, player.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0f, 1.2f);

                    // Apply weapon cooldown to prevent spamming the spell (1.21.4+ uses stack!)
                    player.getCooldowns().addCooldown(stack, 100);
                }
                return InteractionResult.SUCCESS;
            }
        }

        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);

        tooltipComponents.accept(Component.translatable("tooltip.entropica.dynamic_weapon_desc").withStyle(ChatFormatting.DARK_PURPLE));

        if (stack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)) {
            tooltipComponents.accept(Component.literal("A weapon bound to the soul...").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }

        if (stack.has(ModDataComponents.VIS_WEAPON_STATE.get())) {
            VisWeaponState state = stack.get(ModDataComponents.VIS_WEAPON_STATE.get());
            if (state != null && state.baseType() != null) {
                tooltipComponents.accept(Component.empty()); // Spacer

                // Indicate if the weapon is autonomous
                if (state.isAutonomous()) {
                    tooltipComponents.accept(Component.literal("⚔ Autonomous Eidolic Weapon").withStyle(ChatFormatting.GOLD));
                    tooltipComponents.accept(Component.literal(" Requires: Autonomous Weapon Sheath").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                } else {
                    tooltipComponents.accept(Component.literal("Eidolic Forged Weapon").withStyle(ChatFormatting.LIGHT_PURPLE));
                }

                tooltipComponents.accept(Component.literal(" Material: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(state.materialTier()).withStyle(ChatFormatting.WHITE)));

                Component essenceName = Component.literal(state.baseType().getSerializedName())
                        .withStyle(style -> style.withColor(state.baseType().getColorInt()));
                tooltipComponents.accept(Component.literal(" Vis Affinity: ").withStyle(ChatFormatting.GRAY).append(essenceName));

                if (state.isAltered()) {
                    tooltipComponents.accept(Component.literal(" Active Profile: " + state.activeProfile())
                            .withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
                }

                if (state.baseDamage() > 0) {
                    tooltipComponents.accept(Component.literal(String.format(" Resonance Damage: +%.1f", state.baseDamage())).withStyle(ChatFormatting.BLUE));
                }

                if (state.bonusSpeed() > 0) {
                    tooltipComponents.accept(Component.literal(String.format(" Kinetic Swiftness: +%.2f", state.bonusSpeed())).withStyle(ChatFormatting.AQUA));
                }

                tooltipComponents.accept(Component.literal(" Augment Slots: " + state.slottedFragments().size() + " / " + state.innateSlots())
                        .withStyle(ChatFormatting.GOLD));

                if (!state.activeSpell().equals("none")) {
                    tooltipComponents.accept(Component.empty()); // Spacer
                    tooltipComponents.accept(Component.literal(" Embedded Spell: ").withStyle(ChatFormatting.LIGHT_PURPLE)
                            .append(Component.literal(state.activeSpell()).withStyle(ChatFormatting.GOLD)));
                    tooltipComponents.accept(Component.literal(" Right-Click to Cast").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                }
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}