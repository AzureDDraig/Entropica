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
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import ddraig.net.entropica.block.VoidRiftBlock;
import ddraig.net.entropica.block.entity.VoidRiftBlockEntity;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModItems;

import java.util.function.Consumer;

public class VoidResonantTuningForkItem extends EntropicaComponentItem {

    public static final int MAX_VIS = 256;
    public static final int RIFT_COST = 16;
    public static final int LINK_COST = 32;
    public static final int BREAK_COST = 64;
    public static final int MAX_RANGE = 16;

    public VoidResonantTuningForkItem(Properties properties) {
        super(properties, "tooltip.entropica.void_resonant_tuning_fork");
    }

    private int getVisValue(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        Item item = stack.getItem();

        if (item instanceof EssenceItem essence) {
            int tier = essence.getTier();
            if (tier == 0) return 2;
            if (tier == 1) return 8;
            if (tier == 2) return 32;
            if (tier == 3) return 128;
        } else if (item instanceof EssenceAmpouleItem ampoule) {
            int tier = ampoule.getTier();
            if (tier == 1) return 8;
            if (tier == 2) return 32;
            if (tier == 3) return 128;
        } else if (item instanceof VisFumeAmpouleItem fumeAmpoule) {
            return fumeAmpoule.getCapacity() * 2;
        }
        return 0;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack forkStack, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY) return false;
        ItemStack slotStack = slot.getItem();
        if (slotStack.isEmpty()) return false;

        int visValue = getVisValue(slotStack);
        if (visValue > 0) return absorbVis(forkStack, slotStack, visValue, player);
        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack forkStack, ItemStack otherStack, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) return false;
        if (otherStack.isEmpty()) return false;

        int visValue = getVisValue(otherStack);
        if (visValue > 0) return absorbVis(forkStack, otherStack, visValue, player);
        return false;
    }

    private boolean absorbVis(ItemStack forkStack, ItemStack essenceStack, int visPerItem, Player player) {
        CustomData data = forkStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        int currentVis = tag.getInt("StoredMateria2").orElse(0);

        if (currentVis >= MAX_VIS) return false;

        int spaceRemaining = MAX_VIS - currentVis;
        int itemsToConsume = Math.min(essenceStack.getCount(), spaceRemaining / visPerItem);

        if (itemsToConsume <= 0) return false;

        int totalVisGained = itemsToConsume * visPerItem;

        if (essenceStack.getItem() instanceof EssenceAmpouleItem || essenceStack.getItem() instanceof VisFumeAmpouleItem) {
            Item baseItem = net.minecraft.world.item.Items.GLASS_BOTTLE;
            if (essenceStack.getItem() instanceof VisFumeAmpouleItem fume) {
                baseItem = fume.getBaseItem();
            } else if (essenceStack.getItem() instanceof EssenceAmpouleItem ampoule) {
                int tier = ampoule.getTier();
                if (tier == 1) baseItem = ModItems.SMALL_AMPOULE_BASE.get();
                else if (tier == 2) baseItem = ModItems.MEDIUM_AMPOULE_BASE.get();
                else if (tier == 3) baseItem = ModItems.LARGE_AMPOULE_BASE.get();
            }

            ItemStack emptyBottles = new ItemStack(baseItem, itemsToConsume);
            if (!player.getInventory().add(emptyBottles)) {
                player.drop(emptyBottles, false);
            }
        }

        essenceStack.shrink(itemsToConsume);
        tag.putInt("StoredMateria2", currentVis + totalVisGained);
        CustomData.set(DataComponents.CUSTOM_DATA, forkStack, tag);
        player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
        return true;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack forkStack = player.getItemInHand(hand);
        ItemStack offhandStack = player.getItemInHand(InteractionHand.OFF_HAND);

        if (!offhandStack.isEmpty()) {
            int visPerItem = getVisValue(offhandStack);
            if (visPerItem > 0) {
                CustomData data = forkStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                CompoundTag tag = data.copyTag();
                int currentVis = tag.getInt("StoredMateria2").orElse(0);

                int spaceRemaining = MAX_VIS - currentVis;
                int itemsToConsume = Math.min(offhandStack.getCount(), spaceRemaining / visPerItem);

                if (itemsToConsume > 0) {
                    if (!level.isClientSide()) {
                        if (offhandStack.getItem() instanceof EssenceAmpouleItem || offhandStack.getItem() instanceof VisFumeAmpouleItem) {
                            Item baseItem = net.minecraft.world.item.Items.GLASS_BOTTLE;
                            if (offhandStack.getItem() instanceof VisFumeAmpouleItem fume) {
                                baseItem = fume.getBaseItem();
                            } else if (offhandStack.getItem() instanceof EssenceAmpouleItem ampoule) {
                                int tier = ampoule.getTier();
                                if (tier == 1) baseItem = ModItems.SMALL_AMPOULE_BASE.get();
                                else if (tier == 2) baseItem = ModItems.MEDIUM_AMPOULE_BASE.get();
                                else if (tier == 3) baseItem = ModItems.LARGE_AMPOULE_BASE.get();
                            }

                            ItemStack emptyBottles = new ItemStack(baseItem, itemsToConsume);
                            if (!player.getInventory().add(emptyBottles)) {
                                player.drop(emptyBottles, false);
                            }
                        }

                        int totalVisGained = itemsToConsume * visPerItem;
                        offhandStack.shrink(itemsToConsume);
                        tag.putInt("StoredMateria2", currentVis + totalVisGained);
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
        int currentVis = tag.getInt("StoredMateria2").orElse(0);

        boolean isRift = level.getBlockState(clickedPos).getBlock() instanceof VoidRiftBlock;

        if (isRift) {
            if (player.isShiftKeyDown()) {
                if (!level.isClientSide()) {
                    tag.putInt("LinkRiftX", clickedPos.getX());
                    tag.putInt("LinkRiftY", clickedPos.getY());
                    tag.putInt("LinkRiftZ", clickedPos.getZ());
                    tag.putString("LinkRiftDim", level.dimension().location().toString());
                    CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
                } else {
                    player.displayClientMessage(Component.literal("Rift selected. Sneak-Right-Click an inventory to link it.").withStyle(ChatFormatting.AQUA), true);
                }
                return InteractionResult.SUCCESS;
            } else if (tag.contains("LinkRiftX")) {
                if (!level.isClientSide()) {
                    tag.remove("LinkRiftX"); tag.remove("LinkRiftY"); tag.remove("LinkRiftZ"); tag.remove("LinkRiftDim");
                    CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
                } else {
                    player.displayClientMessage(Component.literal("Rift linking mode cancelled.").withStyle(ChatFormatting.YELLOW), true);
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }

        if (tag.contains("LinkRiftX")) {
            String linkDim = tag.getString("LinkRiftDim").orElse("");
            if (!level.dimension().location().toString().equals(linkDim)) {
                if (level.isClientSide()) player.displayClientMessage(Component.literal("Cannot link across dimensions!").withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }

            if (currentVis < LINK_COST) {
                if (level.isClientSide()) player.displayClientMessage(Component.literal("Not enough Materia to link! Requires " + LINK_COST).withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }

            BlockPos riftPos = new BlockPos(tag.getInt("LinkRiftX").orElse(0), tag.getInt("LinkRiftY").orElse(0), tag.getInt("LinkRiftZ").orElse(0));

            // Adjusted to a 9x9x9 area (Chebyshev distance of 4 blocks from the center Rift)
            int dx = Math.abs(riftPos.getX() - clickedPos.getX());
            int dy = Math.abs(riftPos.getY() - clickedPos.getY());
            int dz = Math.abs(riftPos.getZ() - clickedPos.getZ());

            if (dx <= 4 && dy <= 4 && dz <= 4) {
                if (!level.isClientSide()) {
                    BlockEntity be = level.getBlockEntity(riftPos);
                    if (be instanceof VoidRiftBlockEntity riftBE) {
                        riftBE.addAttachedInventory(clickedPos);
                    }
                    tag.remove("LinkRiftX"); tag.remove("LinkRiftY"); tag.remove("LinkRiftZ"); tag.remove("LinkRiftDim");
                    tag.putInt("StoredMateria2", currentVis - LINK_COST);
                    CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);

                    level.playSound(null, clickedPos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.5f, 1.5f);
                } else {
                    player.displayClientMessage(Component.literal("Inventory successfully linked to Rift! (-" + LINK_COST + " Materia)").withStyle(ChatFormatting.GREEN), true);
                }
            } else {
                if (level.isClientSide()) player.displayClientMessage(Component.literal("Inventory too far! Must be within 4 blocks (9x9x9 area) of the Rift.").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResult.SUCCESS;
        }

        if (currentVis < RIFT_COST) {
            if (level.isClientSide()) player.displayClientMessage(Component.literal("Not enough Materia! Requires " + RIFT_COST).withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        BlockPos riftPos = clickedPos.relative(face);

        if (!tag.contains("BoundX")) {
            if (!level.isClientSide()) {
                tag.putInt("BoundX", riftPos.getX());
                tag.putInt("BoundY", riftPos.getY());
                tag.putInt("BoundZ", riftPos.getZ());
                tag.putInt("InvX", clickedPos.getX());
                tag.putInt("InvY", clickedPos.getY());
                tag.putInt("InvZ", clickedPos.getZ());
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
            BlockPos startInvPos = new BlockPos(tag.getInt("InvX").orElse(0), tag.getInt("InvY").orElse(0), tag.getInt("InvZ").orElse(0));

            if (startPos.equals(riftPos)) {
                if (!level.isClientSide()) {
                    tag.remove("BoundX"); tag.remove("BoundY"); tag.remove("BoundZ");
                    tag.remove("InvX"); tag.remove("InvY"); tag.remove("InvZ");
                    tag.remove("BoundDim");
                    CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
                }
                if (level.isClientSide()) player.displayClientMessage(Component.literal("Rift tearing cancelled.").withStyle(ChatFormatting.YELLOW), true);
                return InteractionResult.SUCCESS;
            }

            // Calculate Chebyshev distance to the Input Rift AND to the Input Inventory
            int distToRift = Math.max(Math.max(Math.abs(startPos.getX() - riftPos.getX()), Math.abs(startPos.getY() - riftPos.getY())), Math.abs(startPos.getZ() - riftPos.getZ()));
            int distToInv = Math.max(Math.max(Math.abs(startInvPos.getX() - riftPos.getX()), Math.abs(startInvPos.getY() - riftPos.getY())), Math.abs(startInvPos.getZ() - riftPos.getZ()));

            // Verifies the output is within 16 blocks of EITHER the input rift or its inventory
            if (distToRift > MAX_RANGE && distToInv > MAX_RANGE) {
                if (level.isClientSide()) player.displayClientMessage(Component.literal("Too far! Output must be within " + MAX_RANGE + " blocks of the Input Anchor or Inventory.").withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }

            if (!level.isClientSide()) {
                tag.putInt("StoredMateria2", currentVis - RIFT_COST);
                tag.remove("BoundX"); tag.remove("BoundY"); tag.remove("BoundZ");
                tag.remove("InvX"); tag.remove("InvY"); tag.remove("InvZ");
                tag.remove("BoundDim");
                CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);

                level.setBlock(startPos, ModBlocks.VOID_RIFT.get().defaultBlockState().setValue(VoidRiftBlock.IS_INPUT, true), 3);
                level.setBlock(riftPos, ModBlocks.VOID_RIFT.get().defaultBlockState().setValue(VoidRiftBlock.IS_INPUT, false), 3);

                if (level.getBlockEntity(startPos) instanceof VoidRiftBlockEntity inputBE) {
                    inputBE.setTarget(riftPos);
                    inputBE.addAttachedInventory(startInvPos);
                }
                if (level.getBlockEntity(riftPos) instanceof VoidRiftBlockEntity outputBE) {
                    outputBE.addAttachedInventory(clickedPos);
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
        int materia = tag.getInt("StoredMateria2").orElse(0);

        tooltipComponents.accept(Component.literal("Stored Materia: " + materia + " / " + MAX_VIS).withStyle(ChatFormatting.DARK_PURPLE));
        if (tag.contains("BoundX")) {
            tooltipComponents.accept(Component.literal("Rift Anchor: [" + tag.getInt("BoundX").orElse(0) + ", " + tag.getInt("BoundY").orElse(0) + ", " + tag.getInt("BoundZ").orElse(0) + "]").withStyle(ChatFormatting.GRAY));
        }
        if (tag.contains("LinkRiftX")) {
            tooltipComponents.accept(Component.literal("Linking Rift: [" + tag.getInt("LinkRiftX").orElse(0) + ", " + tag.getInt("LinkRiftY").orElse(0) + ", " + tag.getInt("LinkRiftZ").orElse(0) + "]").withStyle(ChatFormatting.AQUA));
        }
    }
}