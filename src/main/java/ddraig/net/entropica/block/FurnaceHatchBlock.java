package ddraig.net.entropica.block;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.entity.EntropicCoreBlockEntity;
import ddraig.net.entropica.item.OrbisCellItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Map;

public class FurnaceHatchBlock extends Block {

    public FurnaceHatchBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            EntropicCoreBlockEntity core = findCore(level, pos);

            if (core == null) {
                player.displayClientMessage(Component.literal("§cThis hatch is not connected to an Entropic Core."), true);
                return InteractionResult.SUCCESS;
            }

            EntropicCoreBlockEntity master = core.getMaster();

            if (!master.isFormed()) {
                player.displayClientMessage(Component.literal("§cMultiblock Error: " + master.getLastValidationError()), false);
                return InteractionResult.SUCCESS;
            }

            ItemStack stack = player.getMainHandItem();

            // 1. Ampoule Interaction
            if (master.interactWithAmpoule(player, stack, InteractionHand.MAIN_HAND)) {
                return InteractionResult.SUCCESS;
            }

            // 2. Orbis Cell Interaction
            if (stack.getItem() instanceof OrbisCellItem orbisCell) {
                master.updateLastInteractedHatch(pos);

                int cellCapacity = orbisCell.getMaxMana();
                CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                CompoundTag tag = customData.copyTag();

                int currentCellMana = tag.getInt("StoredMana").orElse(0);
                String storedTypeStr = tag.getString("ManaType").orElse("");

                EssenceType targetType = null;

                if (currentCellMana == 0 || storedTypeStr.isEmpty()) {
                    for (Map.Entry<EssenceType, Integer> entry : master.getManaPool().entrySet()) {
                        if (entry.getValue() > 0) {
                            targetType = entry.getKey();
                            break;
                        }
                    }
                } else {
                    try {
                        targetType = EssenceType.valueOf(storedTypeStr);
                    } catch (IllegalArgumentException e) {
                        targetType = null;
                    }
                }

                if (targetType != null) {
                    if (currentCellMana < cellCapacity) {
                        int spaceLeft = cellCapacity - currentCellMana;
                        int extracted = master.extractMana(targetType, spaceLeft);

                        if (extracted > 0) {
                            tag.putInt("StoredMana", currentCellMana + extracted);
                            tag.putString("ManaType", targetType.name());
                            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

                            player.displayClientMessage(Component.literal("§bExtracted " + extracted + " " + targetType.name().toLowerCase() + " Mana! (Cell: " + (currentCellMana + extracted) + "/" + cellCapacity + ")"), true);
                        } else {
                            player.displayClientMessage(Component.literal("§cCore has no more " + targetType.name().toLowerCase() + " mana!"), true);
                        }
                    } else {
                        player.displayClientMessage(Component.literal("§eOrbis Cell is already full!"), true);
                    }
                } else {
                    player.displayClientMessage(Component.literal("§cCore is completely empty!"), true);
                }

                return InteractionResult.SUCCESS;
            }
            // 3. Empty Hand / Other Item Interactions
            else if (stack.isEmpty()) {
                if (player.isShiftKeyDown()) {
                    master.toggleBurnMode(player);
                } else {
                    master.toggleFurnace(player, pos);
                }
                return InteractionResult.SUCCESS;
            }
            else {
                player.displayClientMessage(Component.literal("§eUse an empty hand to toggle power, sneak-click to toggle mode, or use an Ampoule/Orbis Cell to extract."), true);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private EntropicCoreBlockEntity findCore(Level level, BlockPos pos) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockEntity be = level.getBlockEntity(pos.offset(x, y, z));
                    if (be instanceof EntropicCoreBlockEntity core) {
                        return core;
                    }
                }
            }
        }
        return null;
    }
}