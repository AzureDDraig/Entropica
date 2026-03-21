package ddraig.net.entropica.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import ddraig.net.entropica.block.VoidRiftBlock;
import ddraig.net.entropica.block.entity.VoidRiftBlockEntity;
import ddraig.net.entropica.registry.ModBlocks;

import java.util.function.Consumer;

public class VoidResonantTuningForkItem extends EntropicaComponentItem {

    public static final int MAX_VIS = 256;
    public static final int RIFT_COST = 16;
    public static final int MAX_RANGE = 16;

    public VoidResonantTuningForkItem(Properties properties) {
        super(properties, "tooltip.entropica.void_resonant_tuning_fork");
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack forkStack = player.getItemInHand(hand);
        ItemStack offhandStack = player.getItemInHand(InteractionHand.OFF_HAND);

        if (!offhandStack.isEmpty()) {
            int visToAdd = 0;

            if (offhandStack.getItem() instanceof EssenceItem essence) {
                visToAdd = (essence.getTier() + 1) * 4;
            } else if (offhandStack.getItem() instanceof VisFumeAmpouleItem ampoule) {
                visToAdd = ampoule.getCapacity() / 2;
            }

            if (visToAdd > 0) {
                CustomData data = forkStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                CompoundTag tag = data.copyTag();
                int currentVis = tag.getInt("StoredVis").orElse(0);

                if (currentVis + visToAdd <= MAX_VIS) {
                    if (!level.isClientSide()) {
                        offhandStack.shrink(1);
                        tag.putInt("StoredVis", currentVis + visToAdd);
                        CustomData.set(DataComponents.CUSTOM_DATA, forkStack, tag);

                        level.playSound(null, player.blockPosition(), SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 0.5f, 1.5f);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos clickedPos = context.getClickedPos();
        Direction face = context.getClickedFace();
        ItemStack stack = context.getItemInHand();

        if (player == null) return InteractionResult.PASS;

        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        int currentVis = tag.getInt("StoredVis").orElse(0);

        if (currentVis < RIFT_COST) {
            if (level.isClientSide()) player.displayClientMessage(Component.literal("Not enough Vis! Requires " + RIFT_COST).withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        BlockPos riftPos = clickedPos.relative(face);

        if (!tag.contains("BoundX")) {
            if (!level.isClientSide()) {
                tag.putInt("BoundX", riftPos.getX());
                tag.putInt("BoundY", riftPos.getY());
                tag.putInt("BoundZ", riftPos.getZ());
                tag.putString("BoundDim", level.dimension().location().toString());
                CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);

                level.playSound(null, riftPos, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 0.3f, 2.0f);
            } else {
                player.displayClientMessage(Component.literal("Input Rift Anchor marked. Select Output...").withStyle(ChatFormatting.DARK_PURPLE), true);
            }
            return InteractionResult.SUCCESS;
        } else {
            String boundDim = tag.getString("BoundDim").orElse("");
            if (!level.dimension().location().toString().equals(boundDim)) {
                if (level.isClientSide()) player.displayClientMessage(Component.literal("Void Rifts cannot cross dimensions!").withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }

            BlockPos startPos = new BlockPos(tag.getInt("BoundX").orElse(0), tag.getInt("BoundY").orElse(0), tag.getInt("BoundZ").orElse(0));

            if (startPos.equals(riftPos)) {
                if (!level.isClientSide()) {
                    tag.remove("BoundX");
                    tag.remove("BoundY");
                    tag.remove("BoundZ");
                    tag.remove("BoundDim");
                    CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
                }
                if (level.isClientSide()) player.displayClientMessage(Component.literal("Rift tearing cancelled.").withStyle(ChatFormatting.YELLOW), true);
                return InteractionResult.SUCCESS;
            }

            if (startPos.distManhattan(riftPos) > MAX_RANGE) {
                if (level.isClientSide()) player.displayClientMessage(Component.literal("Too far! Max range is " + MAX_RANGE + " blocks.").withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }

            if (!level.isClientSide()) {
                tag.putInt("StoredVis", currentVis - RIFT_COST);
                tag.remove("BoundX");
                tag.remove("BoundY");
                tag.remove("BoundZ");
                tag.remove("BoundDim");
                CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);

                level.setBlock(startPos, ModBlocks.VOID_RIFT.get().defaultBlockState().setValue(VoidRiftBlock.IS_INPUT, true), 3);
                level.setBlock(riftPos, ModBlocks.VOID_RIFT.get().defaultBlockState().setValue(VoidRiftBlock.IS_INPUT, false), 3);

                if (level.getBlockEntity(startPos) instanceof VoidRiftBlockEntity inputBE) {
                    inputBE.setTarget(riftPos);
                    inputBE.setAttachedInventory(clickedPos);
                }
                if (level.getBlockEntity(riftPos) instanceof VoidRiftBlockEntity outputBE) {
                    outputBE.setAttachedInventory(clickedPos);
                }

                level.playSound(null, startPos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0f, 0.5f);
                level.playSound(null, riftPos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0f, 0.5f);
            }
            return InteractionResult.SUCCESS;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        int vis = tag.getInt("StoredVis").orElse(0);

        tooltipComponents.accept(Component.literal("Stored Vis: " + vis + " / " + MAX_VIS).withStyle(ChatFormatting.DARK_PURPLE));
        if (tag.contains("BoundX")) {
            tooltipComponents.accept(Component.literal("Linked: [" + tag.getInt("BoundX").orElse(0) + ", " + tag.getInt("BoundY").orElse(0) + ", " + tag.getInt("BoundZ").orElse(0) + "]").withStyle(ChatFormatting.GRAY));
        }
    }
}