package ddraig.net.entropica.event;

import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

public class TreeLogStrippingHandler {

    public static boolean tryStripping(Player player, Level level, InteractionHand hand, BlockPos pos) {
        if (hand != InteractionHand.MAIN_HAND) return false;

        ItemStack heldStack = player.getItemInHand(hand);
        if (heldStack.isEmpty() || !(heldStack.getItem() instanceof AxeItem)) return false;

        BlockState state = level.getBlockState(pos);
        BlockState strippedState = null;
        Item dropItem = null;
        int minDrop = 1;
        int maxDrop = 2;

        if (state.is(ModBlocks.PYRE_ASH_CEDAR_LOG.get())) {
            strippedState = ModBlocks.STRIPPED_PYRE_ASH_CEDAR_LOG.get().defaultBlockState();
            dropItem = ModItems.CINDER_ASH_FLAKES.get();
        } else if (state.is(ModBlocks.PYRE_ASH_CEDAR_WOOD.get())) {
            strippedState = ModBlocks.STRIPPED_PYRE_ASH_CEDAR_WOOD.get().defaultBlockState();
            dropItem = ModItems.CINDER_ASH_FLAKES.get();
        } else if (state.is(ModBlocks.ABYSSAL_SPORE_CYPRESS_LOG.get())) {
            strippedState = ModBlocks.STRIPPED_ABYSSAL_SPORE_CYPRESS_LOG.get().defaultBlockState();
            dropItem = ModItems.BIOLUMINESCENT_SPORE_POD.get();
        } else if (state.is(ModBlocks.ABYSSAL_SPORE_CYPRESS_WOOD.get())) {
            strippedState = ModBlocks.STRIPPED_ABYSSAL_SPORE_CYPRESS_WOOD.get().defaultBlockState();
            dropItem = ModItems.BIOLUMINESCENT_SPORE_POD.get();
        } else if (state.is(ModBlocks.STARLIGHT_AETHER_BIRCH_LOG.get())) {
            strippedState = ModBlocks.STRIPPED_STARLIGHT_AETHER_BIRCH_LOG.get().defaultBlockState();
            dropItem = ModItems.AETHERIC_BIRCH_BARK.get();
        } else if (state.is(ModBlocks.STARLIGHT_AETHER_BIRCH_WOOD.get())) {
            strippedState = ModBlocks.STRIPPED_STARLIGHT_AETHER_BIRCH_WOOD.get().defaultBlockState();
            dropItem = ModItems.AETHERIC_BIRCH_BARK.get();
        } else if (state.is(ModBlocks.BLOOD_ROOT_IRON_OAK_LOG.get())) {
            strippedState = ModBlocks.STRIPPED_BLOOD_ROOT_IRON_OAK_LOG.get().defaultBlockState();
            dropItem = ModItems.IRON_ROOT_SAP.get();
        } else if (state.is(ModBlocks.BLOOD_ROOT_IRON_OAK_WOOD.get())) {
            strippedState = ModBlocks.STRIPPED_BLOOD_ROOT_IRON_OAK_WOOD.get().defaultBlockState();
            dropItem = ModItems.IRON_ROOT_SAP.get();
        } else if (state.is(ModBlocks.ASTRAL_VEIL_WILLOW_LOG.get())) {
            strippedState = ModBlocks.STRIPPED_ASTRAL_VEIL_WILLOW_LOG.get().defaultBlockState();
            dropItem = ModItems.ASTRAL_WILLOW_FIBRE.get();
        } else if (state.is(ModBlocks.ASTRAL_VEIL_WILLOW_WOOD.get())) {
            strippedState = ModBlocks.STRIPPED_ASTRAL_VEIL_WILLOW_WOOD.get().defaultBlockState();
            dropItem = ModItems.ASTRAL_WILLOW_FIBRE.get();
        } else if (state.is(ModBlocks.VOID_BLIGHT_MANGROVE_LOG.get())) {
            strippedState = ModBlocks.STRIPPED_VOID_BLIGHT_MANGROVE_LOG.get().defaultBlockState();
            dropItem = ModItems.BLIGHTED_BARK_FLAKES.get();
        } else if (state.is(ModBlocks.VOID_BLIGHT_MANGROVE_WOOD.get())) {
            strippedState = ModBlocks.STRIPPED_VOID_BLIGHT_MANGROVE_WOOD.get().defaultBlockState();
            dropItem = ModItems.BLIGHTED_BARK_FLAKES.get();
        } else if (state.is(ModBlocks.AMBER_LOG.get())) {
            strippedState = ModBlocks.STRIPPED_AMBER_LOG.get().defaultBlockState();
            dropItem = ModItems.AMBER_CHUNK.get();
        } else if (state.is(ModBlocks.AMBER_WOOD.get())) {
            strippedState = ModBlocks.STRIPPED_AMBER_WOOD.get().defaultBlockState();
            dropItem = ModItems.AMBER_CHUNK.get();
        }

        if (strippedState == null) return false;

        if (state.hasProperty(RotatedPillarBlock.AXIS)) {
            strippedState = strippedState.setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
        }

        if (!level.isClientSide()) {
            level.setBlock(pos, strippedState, 3);
            level.playSound(null, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0f, 1.0f);

            // Damage Axe
            EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            heldStack.hurtAndBreak(1, player, slot);

            // Particles
            if (level instanceof ServerLevel serverLevel) {
                if (dropItem == ModItems.CINDER_ASH_FLAKES.get()) {
                    serverLevel.sendParticles(ParticleTypes.LAVA, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.2, 0.2, 0.2, 0.05);
                } else if (dropItem == ModItems.BIOLUMINESCENT_SPORE_POD.get()) {
                    serverLevel.sendParticles(ParticleTypes.GLOW, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8, 0.2, 0.2, 0.2, 0.05);
                } else if (dropItem == ModItems.AETHERIC_BIRCH_BARK.get()) {
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.2, 0.2, 0.2, 0.05);
                } else if (dropItem == ModItems.IRON_ROOT_SAP.get()) {
                    serverLevel.sendParticles(ParticleTypes.DRIPPING_DRIPSTONE_LAVA, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.2, 0.2, 0.2, 0.05);
                } else if (dropItem == ModItems.ASTRAL_WILLOW_FIBRE.get()) {
                    serverLevel.sendParticles(ParticleTypes.PORTAL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.2, 0.2, 0.2, 0.05);
                } else if (dropItem == ModItems.BLIGHTED_BARK_FLAKES.get()) {
                    serverLevel.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.2, 0.2, 0.2, 0.05);
                }
            }

            // Drop Item
            if (dropItem != null) {
                int count = minDrop + level.random.nextInt(maxDrop - minDrop + 1);
                ItemStack dropStack = new ItemStack(dropItem, count);
                ItemEntity itemEntity = new ItemEntity(
                        level,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        dropStack
                );
                itemEntity.setDefaultPickUpDelay();
                level.addFreshEntity(itemEntity);
            }
        }

        return true;
    }
}
