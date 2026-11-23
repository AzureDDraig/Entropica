package ddraig.net.entropica.item.biome3;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * Frost-Bite Dagger — swift fang-forged blade channeling Glacial and Frozen Materia.
 * Inflicts deep frostbite ticks, Slowness III (-45% speed), and Mining Fatigue II on strike.
 */
public class FrostBiteDaggerItem extends Item {

    public FrostBiteDaggerItem(Properties properties) {
        super(properties);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        applyFrostbiteStrike(target, attacker);
    }

    private static void applyFrostbiteStrike(LivingEntity target, LivingEntity attacker) {
        if (CryoInsulatedArmorItem.hasFullCryoInsulation(target)) {
            return;
        }

        int maxFreeze = target.getTicksRequiredToFreeze() + 80;
        target.setTicksFrozen(Math.min(maxFreeze, target.getTicksFrozen() + 90));
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2), attacker);
        target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 60, 1), attacker);

        if (target.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, target.blockPosition(), SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS, 0.85F, 1.15F);
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(),
                    8, 0.25D, 0.3D, 0.25D, 0.04D);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("On Hit: Inflicts deep frostbite, Slowness III, and numbs attack speed.")
                .withStyle(ChatFormatting.AQUA));
    }
}
