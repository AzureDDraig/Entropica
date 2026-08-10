package ddraig.net.entropica.event;

import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

public class AmberLogStrippingHandler {

    public static boolean tryStripping(Player player, Level level, InteractionHand hand, BlockPos pos) {
        if (hand != InteractionHand.MAIN_HAND) return false;

        ItemStack heldStack = player.getItemInHand(hand);
        if (heldStack.isEmpty() || !(heldStack.getItem() instanceof AxeItem)) return false;

        BlockState state = level.getBlockState(pos);
        BlockState strippedState = null;

        if (state.is(ModBlocks.AMBER_LOG.get())) {
            strippedState = ModBlocks.STRIPPED_AMBER_LOG.get().defaultBlockState();
            if (state.hasProperty(RotatedPillarBlock.AXIS)) {
                strippedState = strippedState.setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
            }
        } else if (state.is(ModBlocks.AMBER_WOOD.get())) {
            strippedState = ModBlocks.STRIPPED_AMBER_WOOD.get().defaultBlockState();
            if (state.hasProperty(RotatedPillarBlock.AXIS)) {
                strippedState = strippedState.setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
            }
        }

        if (strippedState == null) return false;

        if (!level.isClientSide()) {
            level.setBlock(pos, strippedState, 3);
            level.playSound(null, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0f, 1.0f);

            // Damage Axe
            net.minecraft.world.entity.EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND;
            heldStack.hurtAndBreak(1, player, slot);


            // Drop 1-2 Amber Chunks in-world
            int count = 1 + level.random.nextInt(2);
            ItemStack dropStack = new ItemStack(ModItems.AMBER_CHUNK.get(), count);
            ItemEntity itemEntity = new ItemEntity(
                    level,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    dropStack
            );
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }

        return true;
    }
}
