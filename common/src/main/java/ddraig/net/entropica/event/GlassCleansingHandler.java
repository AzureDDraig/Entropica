package ddraig.net.entropica.event;

import ddraig.net.entropica.block.DilutedEssenceFluidBlock;
import ddraig.net.entropica.registry.AestheticGlassRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

public class GlassCleansingHandler {

    public static boolean tryCleansing(Player player, Level level, InteractionHand hand, BlockPos pos) {
        if (hand != InteractionHand.MAIN_HAND) return false;

        ItemStack heldStack = player.getItemInHand(hand);
        if (heldStack.isEmpty()) return false;

        Item heldItem = heldStack.getItem();
        int shapeIndex = AestheticGlassRegistry.getShapeIndex(heldItem);
        if (shapeIndex < 0) return false;

        Item clearVariant = AestheticGlassRegistry.getClearGlassItem(shapeIndex);
        if (clearVariant == null || heldItem == clearVariant) {
            return false;
        }

        BlockState state = level.getBlockState(pos);
        boolean isWaterCauldron = state.is(Blocks.WATER_CAULDRON);
        boolean isCauldron = state.is(Blocks.CAULDRON);
        boolean isDilutedEssence = state.getBlock() instanceof DilutedEssenceFluidBlock;

        if (!isWaterCauldron && !isCauldron && !isDilutedEssence) {
            return false;
        }

        if (!level.isClientSide()) {
            if (isWaterCauldron) {
                if (state.hasProperty(LayeredCauldronBlock.LEVEL)) {
                    int currentLevel = state.getValue(LayeredCauldronBlock.LEVEL);
                    if (currentLevel > 1) {
                        level.setBlock(pos, state.setValue(LayeredCauldronBlock.LEVEL, currentLevel - 1), 3);
                    } else {
                        level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
                    }
                }
                level.playSound(null, pos, SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON, SoundSource.BLOCKS, 1.0f, 1.0f);
            } else {
                level.playSound(null, pos, SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON, SoundSource.BLOCKS, 1.0f, 1.0f);
            }

            ItemStack clearStack = new ItemStack(clearVariant, 1);
            heldStack.shrink(1);
            if (heldStack.isEmpty()) {
                player.setItemInHand(hand, clearStack);
            } else if (!player.getInventory().add(clearStack)) {
                player.drop(clearStack, false);
            }
        }

        return true;
    }
}
