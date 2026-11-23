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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.function.Consumer;

/**
 * Cryo-Tempest Horn — copper-bound Glacial Hydra warhorn.
 * Sounds a freezing gale blast (12s cooldown) that douses flames on allies, grants brief Speed,
 * and blasts nearby hostile creatures backward with Slowness III and frostbite.
 */
public class CryoTempestHornItem extends Item {
    public static final int COOLDOWN_TICKS = 240;
    public static final double BLAST_RADIUS = 10.0D;

    public CryoTempestHornItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.TOOT_HORN;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 40;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        player.startUsingItem(hand);
        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, player.blockPosition(),
                    SoundEvents.RAID_HORN.value(), SoundSource.PLAYERS, 0.9F, 1.35F);
            serverLevel.playSound(null, player.blockPosition(),
                    SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.0F, 0.85F);

            player.clearFire();
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 120, 0));

            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    player.getX(), player.getY() + 1.0D, player.getZ(),
                    28, 1.4D, 0.6D, 1.4D, 0.22D);
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    player.getX(), player.getY() + 0.9D, player.getZ(),
                    18, 1.2D, 0.5D, 1.2D, 0.18D);

            AABB area = player.getBoundingBox().inflate(BLAST_RADIUS);
            double maxDistSq = BLAST_RADIUS * BLAST_RADIUS;
            List<LivingEntity> nearby = serverLevel.getEntitiesOfClass(LivingEntity.class, area,
                    target -> target != player
                            && target.isAlive()
                            && !target.isSpectator()
                            && target.distanceToSqr(player) <= maxDistSq);

            for (LivingEntity target : nearby) {
                boolean isAlliedOrTamed = (target instanceof OwnableEntity own && own.getOwnerReference() != null)
                        || target instanceof Player;
                if (isAlliedOrTamed) {
                    target.clearFire();
                } else if (target instanceof Enemy || (target instanceof Mob mob && mob.getTarget() == player)) {
                    double dx = player.getX() - target.getX();
                    double dz = player.getZ() - target.getZ();
                    if (dx * dx + dz * dz < 1.0E-4D) {
                        dx = 0.01D;
                    }
                    target.knockback(1.6F, dx, dz);
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 2));
                    target.setTicksFrozen(Math.min(
                            target.getTicksRequiredToFreeze() + 80,
                            target.getTicksFrozen() + 140));
                }
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Right-Click: Blasts a freezing gale across 10m (12s cooldown).")
                .withStyle(ChatFormatting.AQUA));
        tooltipComponents.accept(Component.literal("Effect: Knocks back and freezes foes, puts out fire on allies, and grants Speed.")
                .withStyle(ChatFormatting.GRAY));
    }
}
