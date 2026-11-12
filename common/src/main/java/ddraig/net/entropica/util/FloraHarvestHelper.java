package ddraig.net.entropica.util;

import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FloraHarvestHelper {

    public static Item getPetalForBlock(Block block) {
        if (block == ModBlocks.AEGIS_ROSE.get() || block == ModBlocks.TALL_AEGIS_ROSE.get()) {
            return ModItems.AEGIS_ROSE_PETALS.get();
        } else if (block == ModBlocks.AEGIS_SPIRE_ORCHID.get() || block == ModBlocks.TALL_AEGIS_SPIRE_ORCHID.get()) {
            return ModItems.AEGIS_SPIRE_PETAL.get();
        } else if (block == ModBlocks.SOUL_FLAME_ORCHID.get() || block == ModBlocks.TALL_SOUL_FLAME_ORCHID.get()) {
            return ModItems.SOUL_FLAME_PETAL.get();
        } else if (block == ModBlocks.VITAE_ORCHID.get() || block == ModBlocks.TALL_VITAE_ORCHID.get()) {
            return ModItems.VITAE_PETAL.get();
        } else if (block == ModBlocks.VOID_STALKER_ORCHID.get() || block == ModBlocks.TALL_VOID_STALKER_ORCHID.get()) {
            return ModItems.VOID_STALKER_PETAL.get();
        } else if (block == ModBlocks.NECROTIC_ROSE_OF_JERICHO.get() || block == ModBlocks.TALL_NECROTIC_ROSE_OF_JERICHO.get()) {
            return ModItems.NECROTIC_ROSE_PETAL.get();
        } else if (block == ModBlocks.SANGUINE_LILY.get()) {
            return ModItems.SANGUINE_PETAL.get();
        } else if (block == ModBlocks.AURORAL_BUTTERCUP.get()) {
            return ModItems.AURORAL_PETAL.get();
        } else if (block == ModBlocks.STARDUST_BELL.get()) {
            return ModItems.STARDUST_BELL_PETAL.get();
        } else if (block == ModBlocks.FULGURITE_SWAMP_BLOOM.get()) {
            return ModItems.FULGURITE_PETAL.get();
        } else if (block == ModBlocks.GALE_BLOOM_DANDELION.get()) {
            return ModItems.GALE_BLOOM_PETAL.get();
        } else if (block == ModBlocks.CINDER_SPORE_MUSHROOM.get()) {
            return ModItems.CINDER_SPORE_CAP.get();
        } else if (block == ModBlocks.SPECTRAL_LANTERN_FLOWER.get()) {
            return ModItems.SPECTRAL_LANTERN_POD.get();
        } else if (block == ModBlocks.CINDER_GRIP_LICHEN.get()) {
            return ModItems.CINDER_LICHEN_FLAKES.get();
        } else if (block == ModBlocks.SOOT_SHROUD_FUNGI.get()) {
            return ModItems.SOOT_SHROUD_CAP.get();
        }
        return null;
    }


    public static boolean tryShearHarvest(Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);
        if (!heldStack.is(Items.SHEARS)) {
            return false;
        }

        Item petalItem = getPetalForBlock(state.getBlock());
        if (petalItem == null) {
            return false;
        }

        if (!level.isClientSide()) {
            boolean is2Tall = state.getBlock() instanceof DoublePlantBlock;
            int count = is2Tall ? 3 + level.random.nextInt(2) : 1 + level.random.nextInt(2);

            Block.popResource(level, pos, new ItemStack(petalItem, count));

            EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            heldStack.hurtAndBreak(1, player, slot);

            level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0f, 1.0f);
            level.destroyBlock(pos, false);
        }
        return true;
    }
}

