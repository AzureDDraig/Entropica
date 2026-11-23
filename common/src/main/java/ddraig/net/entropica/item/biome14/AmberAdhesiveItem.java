package ddraig.net.entropica.item.biome14;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Amber Adhesive — sticky golden resin paste refined from Amber Chunks and Redstone.
 * Substitutes for Slimeballs in Sticky Pistons and Leads, and seals Copper blocks against oxidation like Honeycomb.
 */
public class AmberAdhesiveItem extends Item {

    public AmberAdhesiveItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Optional<BlockState> waxed = HoneycombItem.getWaxed(state);
        if (waxed.isPresent()) {
            Player player = context.getPlayer();
            ItemStack stack = context.getItemInHand();
            BlockState waxedState = waxed.get();
            if (player instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
            }
            stack.consume(1, player);
            level.setBlock(pos, waxedState, 11);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, waxedState));
            level.levelEvent(player, 3003, pos, 0);
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Use on Copper: Seals and waxes Copper blocks against oxidation.")
                .withStyle(ChatFormatting.GOLD));
        tooltipComponents.accept(Component.literal("Crafting: Sticky resin binder for Sticky Pistons, Leads, and Wader Gear.")
                .withStyle(ChatFormatting.GRAY));
    }
}
