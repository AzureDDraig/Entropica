package ddraig.net.entropica.item.biome10;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.function.Consumer;

/**
 * Sun-Glazed Talisman — consecrated Jade Qilin and Liquid Vitae Resin sanctuary relic.
 * Releases an 8-block warm sanctuary pulse that cleanses ailments, restores 4 hearts,
 * and gently mends worn gear (25s cooldown).
 */
public class SunGlazedTalismanItem extends Item {
    public static final int COOLDOWN_TICKS = 500;
    public static final double SANCTUARY_RADIUS = 8.0D;

    private static final EquipmentSlot[] MENDABLE_SLOTS = {
            EquipmentSlot.MAINHAND,
            EquipmentSlot.OFFHAND,
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    public SunGlazedTalismanItem(Properties properties) {
        super(properties.stacksTo(1).durability(64).rarity(Rarity.RARE));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);

            // Cleanse & heal user + allies/pets within 8.0 blocks
            AABB box = player.getBoundingBox().inflate(SANCTUARY_RADIUS);
            double maxDistSq = SANCTUARY_RADIUS * SANCTUARY_RADIUS;
            List<LivingEntity> recipients = serverLevel.getEntitiesOfClass(LivingEntity.class, box,
                    entity -> entity.isAlive()
                            && !entity.isSpectator()
                            && entity.distanceToSqr(player) <= maxDistSq
                            && isSanctuaryRecipient(player, entity));

            if (!recipients.contains(player)) {
                applySanctuaryBlessing(player);
            }
            for (LivingEntity target : recipients) {
                applySanctuaryBlessing(target);
            }

            // Mend up to 2 durability on damaged items in mainhand/offhand/armor slots (excluding the talisman)
            for (EquipmentSlot slot : MENDABLE_SLOTS) {
                ItemStack equipped = player.getItemBySlot(slot);
                if (!equipped.isEmpty()
                        && equipped != stack
                        && !(equipped.getItem() instanceof SunGlazedTalismanItem)
                        && equipped.isDamaged()) {
                    equipped.setDamageValue(Math.max(0, equipped.getDamageValue() - 2));
                }
            }

            // Emit radiant ring particles
            double cx = player.getX();
            double cy = player.getY() + 0.25D;
            double cz = player.getZ();
            int points = 36;
            for (int i = 0; i < points; i++) {
                double angle = (Math.PI * 2.0D * i) / points;
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        cx + cos * 3.5D, cy + 0.4D, cz + sin * 3.5D,
                        1, 0.05D, 0.1D, 0.05D, 0.01D);
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        cx + cos * SANCTUARY_RADIUS, cy + 0.2D, cz + sin * SANCTUARY_RADIUS,
                        1, 0.08D, 0.15D, 0.08D, 0.015D);
            }

            serverLevel.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.2F);
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.85F, 1.3F);

            EquipmentSlot handSlot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            stack.hurtAndBreak(1, player, handSlot);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }

    private static void applySanctuaryBlessing(LivingEntity target) {
        target.removeEffect(MobEffects.POISON);
        target.removeEffect(MobEffects.WITHER);
        target.removeEffect(MobEffects.SLOWNESS);
        target.setTicksFrozen(0);
        target.heal(8.0F);
    }

    private static boolean isSanctuaryRecipient(Player caster, LivingEntity candidate) {
        if (candidate == caster) {
            return true;
        }
        if (candidate instanceof Player otherPlayer) {
            return caster.isAlliedTo(otherPlayer) || caster.getTeam() == null || !caster.canHarmPlayer(otherPlayer);
        }
        if (candidate instanceof OwnableEntity ownable && ownable.getOwnerReference() != null) {
            return !(candidate instanceof Monster m && m.getTarget() == caster);
        }
        return !(candidate instanceof Enemy) && !(candidate instanceof Monster);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Right-Click: Releases an 8m warm sanctuary pulse (25s cooldown).")
                .withStyle(ChatFormatting.GOLD));
        tooltipComponents.accept(Component.literal("Sanctuary: Heals 4 hearts, clears Poison/Wither/Slowness/frost, and mends 2 gear durability.")
                .withStyle(ChatFormatting.YELLOW));
    }
}
