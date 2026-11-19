package ddraig.net.entropica.item.biome1;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Spiked Armor Lining — woven from Shielding Thorn Fiber and leather.
 * Deals 2.0F thorns retaliation damage to nearby melee attackers when carried in inventory or hands.
 */
public class SpikedArmorLiningItem extends Item {

    private static final float RETALIATION_DAMAGE = 2.0F;
    private static final double MELEE_RANGE_SQR = 25.0D;
    private static final Map<UUID, Integer> LAST_RETALIATION_STAMP = new ConcurrentHashMap<>();

    public SpikedArmorLiningItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (!(entity instanceof LivingEntity holder) || !holder.isAlive()) {
            return;
        }

        LivingEntity attacker = holder.getLastHurtByMob();
        int hurtStamp = holder.getLastHurtByMobTimestamp();
        if (attacker != null
                && attacker != holder
                && attacker.isAlive()
                && hurtStamp > 0
                && Math.abs(holder.tickCount - hurtStamp) <= 2
                && holder.distanceToSqr(attacker) <= MELEE_RANGE_SQR) {
            Integer previousStamp = LAST_RETALIATION_STAMP.get(holder.getUUID());
            if (previousStamp == null || previousStamp != hurtStamp) {
                if (LAST_RETALIATION_STAMP.size() > 512) {
                    LAST_RETALIATION_STAMP.clear();
                }
                LAST_RETALIATION_STAMP.put(holder.getUUID(), hurtStamp);
                attacker.hurtServer(level, level.damageSources().thorns(holder), RETALIATION_DAMAGE);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Passive: Retaliates with 2.0 thorn damage against melee attackers.")
                .withStyle(ChatFormatting.GRAY));
    }
}
