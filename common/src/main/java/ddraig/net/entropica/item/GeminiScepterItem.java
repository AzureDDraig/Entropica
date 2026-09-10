package ddraig.net.entropica.item;

import ddraig.net.entropica.entity.projectile.RadiantFireLanceEntity;
import ddraig.net.entropica.entity.projectile.UmbralVortexEntity;
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
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public class GeminiScepterItem extends Item {

    public GeminiScepterItem(Properties properties) {
        super(properties.durability(850).stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            Vec3 look = player.getLookAngle();

            if (player.isShiftKeyDown()) {
                // Secondary Cast: Umbral Water Vortex
                UmbralVortexEntity vortex = new UmbralVortexEntity(level, player);
                vortex.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
                vortex.shoot(look.x, look.y, look.z, 1.3F, 1.0F);
                level.addFreshEntity(vortex);

                level.playSound(null, player.blockPosition(), SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 1.0F, 0.7F);
                player.getCooldowns().addCooldown(stack, 30); // 1.5s cooldown
            } else {
                // Primary Cast: Radiant Fire Lance
                RadiantFireLanceEntity fire = new RadiantFireLanceEntity(level, player);
                fire.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
                fire.shoot(look.x, look.y, look.z, 1.8F, 0.5F);
                level.addFreshEntity(fire);

                level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 1.4F);
                player.getCooldowns().addCooldown(stack, 12); // 0.6s cooldown
            }

            stack.hurtAndBreak(1, player, player.getEquipmentSlotForItem(stack));
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("§6Primary (Right-Click): §eRadiant Fire Lance (Piercing)"));
        tooltipComponents.accept(Component.literal("§5Secondary (Sneak + Right-Click): §bUmbral Water Vortex (Suction & Drench)"));
        tooltipComponents.accept(Component.literal("§dCombo: §fThermal-Void Cavitation (Steam Shock against Drenched targets)"));
    }
}
