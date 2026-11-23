package ddraig.net.entropica.item.biome11;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

/**
 * Storm-Kite Glider — lightweight membrane hang-glider framed in Zephyr Glass Fiber and Simurgh Feathers.
 * Holding in either hand lets the player glide smoothly across chasms, catch stronger lift from
 * Zephyr Updraft Grates, and ride high-altitude thunderstorm thermals.
 */
public class StormKiteGliderItem extends Item {

    public StormKiteGliderItem(Properties properties) {
        super(properties.stacksTo(1).durability(432));
    }

    public static boolean isHoldingGlider(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        return entity.getMainHandItem().getItem() instanceof StormKiteGliderItem
                || entity.getOffhandItem().getItem() instanceof StormKiteGliderItem;
    }

    public static void tickPlayerGliding(Player player) {
        if (player == null || !player.isAlive() || !isHoldingGlider(player)) {
            return;
        }
        if (player.onGround()
                || player.isInWater()
                || player.isInLava()
                || player.isPassenger()
                || player.getAbilities().flying
                || player.isFallFlying()
                || player.isShiftKeyDown()) {
            return;
        }

        Vec3 vel = player.getDeltaMovement();
        if (vel.y >= -0.05D) {
            return;
        }

        double capVy = (player.level().isThundering() && player.level().canSeeSky(player.blockPosition()))
                ? -0.035D
                : -0.075D;

        if (vel.y < capVy) {
            Vec3 look = player.getLookAngle();
            Vec3 horizLook = new Vec3(look.x, 0.0D, look.z);
            double newVx = vel.x;
            double newVz = vel.z;
            double horizSpeed = Math.hypot(vel.x, vel.z);

            if (horizLook.lengthSqr() > 1.0E-4D && horizSpeed < 0.62D) {
                Vec3 dir = horizLook.normalize();
                newVx += dir.x * 0.022D;
                newVz += dir.z * 0.022D;
            }

            player.setDeltaMovement(newVx, capVy, newVz);
            player.resetFallDistance();

            if (player instanceof ServerPlayer serverPlayer && player.tickCount % 60 == 0) {
                InteractionHand hand = serverPlayer.getMainHandItem().getItem() instanceof StormKiteGliderItem
                        ? InteractionHand.MAIN_HAND
                        : InteractionHand.OFF_HAND;
                ItemStack stack = serverPlayer.getItemInHand(hand);
                stack.hurtAndBreak(1, serverPlayer,
                        hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Hold in Either Hand: Glides forward smoothly while airborne (Sneak to drop).")
                .withStyle(ChatFormatting.AQUA));
        tooltipComponents.accept(Component.literal("Storm Lift: Gains extra lift over Zephyr Updraft Grates and under thunderstorms.")
                .withStyle(ChatFormatting.YELLOW));
    }
}
