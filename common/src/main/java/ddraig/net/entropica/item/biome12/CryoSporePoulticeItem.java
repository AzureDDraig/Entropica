package ddraig.net.entropica.item.biome12;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * Cryo-Spore Poultice — chilled medicinal salve of Frozen Spore Moss, snow, and Living Spores.
 * Instantly purges Poison, Nausea, Wither, and frost buildup, restores 2 hearts, and grants Regeneration II.
 */
public class CryoSporePoulticeItem extends Item {
    public static final int COOLDOWN_TICKS = 120;

    public CryoSporePoulticeItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);

        if (!level.isClientSide()) {
            player.removeEffect(MobEffects.POISON);
            player.removeEffect(MobEffects.NAUSEA);
            player.removeEffect(MobEffects.WITHER);
            player.setTicksFrozen(0);
            player.heal(4.0F);
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1));

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.HONEY_DRINK.value(), SoundSource.PLAYERS, 0.9F, 1.05F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.3F);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                        player.getX(), player.getY() + 1.0D, player.getZ(),
                        10, 0.35D, 0.35D, 0.35D, 0.02D);
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.isCreative()) {
            stack.shrink(1);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Right-Click: Restores 2 hearts + Regeneration II (6s) and clears frost.")
                .withStyle(ChatFormatting.GREEN));
        tooltipComponents.accept(Component.literal("Antidote Salve: Cleanses Poison, Nausea, and Wither (6s cooldown).")
                .withStyle(ChatFormatting.AQUA));
    }
}
