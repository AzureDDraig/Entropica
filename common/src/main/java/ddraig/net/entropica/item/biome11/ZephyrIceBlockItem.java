package ddraig.net.entropica.item.biome11;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.function.Consumer;

/**
 * Zephyr Ice BlockItem — allows placing buoyant Zephyr Ice directly onto open water
 * surfaces (like a Lily Pad) as well as against solid blocks to build floating ice bridges.
 */
public class ZephyrIceBlockItem extends BlockItem {

    public ZephyrIceBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player != null && !player.isEyeInFluid(FluidTags.WATER)) {
            BlockHitResult fluidHit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
            if (fluidHit.getType() == HitResult.Type.BLOCK
                    && level.getFluidState(fluidHit.getBlockPos()).is(FluidTags.WATER)) {
                BlockHitResult aboveWater = fluidHit.withPosition(fluidHit.getBlockPos().above());
                InteractionResult waterResult = super.useOn(new UseOnContext(player, context.getHand(), aboveWater));
                if (waterResult.consumesAction()) {
                    return waterResult;
                }
            }
        }
        return super.useOn(context);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hitResult.getType() == HitResult.Type.BLOCK
                && level.getFluidState(hitResult.getBlockPos()).is(FluidTags.WATER)) {
            BlockHitResult aboveWater = hitResult.withPosition(hitResult.getBlockPos().above());
            return super.useOn(new UseOnContext(player, hand, aboveWater));
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Can be placed directly onto open water to build floating ice bridges.")
                .withStyle(ChatFormatting.AQUA));
    }
}
