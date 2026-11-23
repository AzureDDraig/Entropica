package ddraig.net.entropica.item.biome11;

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
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

/**
 * Tempest Bottle — reusable bottled gale wind of the Storm-Swept Icebergs.
 * Launches the user upward on a burst of storm wind (5s cooldown, not consumed on use)
 * and can also be brewed with Awkward Potions into Potions of Tempest Stride.
 */
public class TempestBottleItem extends Item {
    public static final int COOLDOWN_TICKS = 100;

    public TempestBottleItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);

        Vec3 look = player.getLookAngle();
        Vec3 cur = player.getDeltaMovement();
        player.setDeltaMovement(
                cur.x * 0.5D + look.x * 0.42D,
                0.72D,
                cur.z * 0.5D + look.z * 0.42D
        );
        player.hurtMarked = true;
        player.resetFallDistance();

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0));
            serverLevel.playSound(null, player.blockPosition(),
                    SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.9F, 1.15F);
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    player.getX(), player.getY() + 0.1D, player.getZ(),
                    18, 0.35D, 0.15D, 0.35D, 0.08D);
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    player.getX(), player.getY() + 0.15D, player.getZ(),
                    10, 0.3D, 0.15D, 0.3D, 0.12D);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Right-Click: Reusable wind-jump boost with brief Slow Falling (5s cooldown).")
                .withStyle(ChatFormatting.AQUA));
        tooltipComponents.accept(Component.literal("Brewing: Brew with Awkward Potions to craft Potions of Tempest Stride.")
                .withStyle(ChatFormatting.GRAY));
    }
}
