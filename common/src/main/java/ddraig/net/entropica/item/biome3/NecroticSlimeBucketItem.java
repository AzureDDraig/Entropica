package ddraig.net.entropica.item.biome3;

import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.function.Consumer;

/**
 * Necrotic Slime Bucket — harvested Blight Materia ooze from Frost-Bite Floes slime crusts.
 * Places a Necrotic Slime Patch onto valid sturdy or icy surfaces and returns an empty bucket.
 */
public class NecroticSlimeBucketItem extends Item {

    public NecroticSlimeBucketItem(Properties properties) {
        super(properties.stacksTo(1).craftRemainder(Items.BUCKET));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!ModBlocks.NECROTIC_SLIME_PATCH.isBound()) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Direction face = context.getClickedFace();
        BlockState clickedState = level.getBlockState(clickedPos);

        BlockPos placePos;
        if (clickedState.canBeReplaced() && !clickedState.is(ModBlocks.NECROTIC_SLIME_PATCH.get())) {
            placePos = clickedPos;
        } else if (face == Direction.UP) {
            placePos = clickedPos.above();
        } else {
            placePos = clickedPos.relative(face);
        }

        BlockState targetExisting = level.getBlockState(placePos);
        if (!targetExisting.isAir() && (!targetExisting.canBeReplaced() || targetExisting.is(ModBlocks.NECROTIC_SLIME_PATCH.get()))) {
            return InteractionResult.FAIL;
        }
        if (!level.getFluidState(placePos).isEmpty()) {
            return InteractionResult.FAIL;
        }

        BlockState patchState = ModBlocks.NECROTIC_SLIME_PATCH.get().defaultBlockState();
        if (!patchState.canSurvive(level, placePos)) {
            return InteractionResult.FAIL;
        }

        Player player = context.getPlayer();
        level.playSound(player, placePos, SoundEvents.SLIME_SQUISH, SoundSource.BLOCKS, 1.0F, 0.9F);
        if (!level.isClientSide()) {
            level.setBlock(placePos, patchState, 3);
            level.gameEvent(player, GameEvent.BLOCK_PLACE, placePos);
            if (player != null && !player.getAbilities().instabuild) {
                player.setItemInHand(context.getHand(), new ItemStack(Items.BUCKET));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Use on Top Face: Spreads a Necrotic Slime Patch onto ice or stone.")
                .withStyle(ChatFormatting.DARK_GREEN));
    }
}
