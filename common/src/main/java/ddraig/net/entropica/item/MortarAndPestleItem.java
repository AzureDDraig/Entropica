package ddraig.net.entropica.item;

import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
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

public class MortarAndPestleItem extends Item {

    public MortarAndPestleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack otherStack = player.getItemInHand(otherHand);

        if (otherStack.is(ModItems.ASTRAL_CRYSTAL.get())) {
            if (!level.isClientSide()) {
                otherStack.shrink(1);
                ItemStack seeds = new ItemStack(ModItems.ASTRAL_CRYSTAL_SEED.get(), 4);
                if (!player.getInventory().add(seeds)) {
                    player.drop(seeds, false);
                }
                held.hurtAndBreak(1, player, player.getEquipmentSlotForItem(held));

                level.playSound(null, player.blockPosition(), SoundEvents.GRINDSTONE_USE, SoundSource.PLAYERS, 1.0f, 1.3f);
                if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 1.0, player.getZ(), 10, 0.2, 0.2, 0.2, 0.05);
                }
                player.displayClientMessage(Component.literal("§a[Mortar & Pestle] §7Ground Astral Crystal into §b4 Crystal Seeds§7."), true);
            }
            return InteractionResult.SUCCESS;
        }

        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("§6◆ Alchemical Grinding Instrument"));
        tooltipComponents.accept(Component.literal("§7  Right-click while holding an Astral Crystal in offhand to grind into 4 Seeds."));
        tooltipComponents.accept(Component.literal("§7  Also functions as a durable crafting catalyst."));
    }
}
