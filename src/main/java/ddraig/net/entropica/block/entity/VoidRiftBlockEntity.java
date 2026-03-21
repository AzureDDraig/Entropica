package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.block.VoidRiftBlock;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class VoidRiftBlockEntity extends BlockEntity {

    private BlockPos targetPos = null;
    private BlockPos attachedInvPos = null;

    private ItemStack processingItem = ItemStack.EMPTY;
    private int processTick = 0;
    public static final int MAX_PROCESS_TICKS = 20;

    public VoidRiftBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VOID_RIFT_BE.get(), pos, state);
    }

    public void setTarget(BlockPos pos) {
        this.targetPos = pos;
        setChanged();
    }

    public void setAttachedInventory(BlockPos pos) {
        this.attachedInvPos = pos;
        setChanged();
    }

    public ItemStack getProcessingItem() {
        return processingItem;
    }

    public int getProcessTick() {
        return processTick;
    }

    public void receiveItem(ItemStack stack) {
        this.processingItem = stack.copy();
        this.processTick = 0;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        boolean isInput = state.getValue(VoidRiftBlock.IS_INPUT);

        if (isInput) {
            handleInput(level, pos);
        } else {
            handleOutput(level, pos);
        }
    }

    private void handleInput(Level level, BlockPos pos) {
        if (processingItem.isEmpty()) {
            if (attachedInvPos != null && targetPos != null && level.getGameTime() % 10 == 0) {
                BlockEntity targetBE = level.getBlockEntity(targetPos);
                if (!(targetBE instanceof VoidRiftBlockEntity) || !((VoidRiftBlockEntity) targetBE).getProcessingItem().isEmpty()) return;

                BlockEntity invBE = level.getBlockEntity(attachedInvPos);
                if (invBE instanceof Container container) {
                    for (int i = 0; i < container.getContainerSize(); i++) {
                        ItemStack stack = container.getItem(i);
                        if (!stack.isEmpty()) {
                            this.processingItem = container.removeItem(i, 1);
                            this.processTick = 0;
                            setChanged();
                            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                            break;
                        }
                    }
                }
            }
        } else {
            processTick++;
            if (processTick >= MAX_PROCESS_TICKS) {
                if (!level.isClientSide() && targetPos != null) {
                    BlockEntity be = level.getBlockEntity(targetPos);
                    if (be instanceof VoidRiftBlockEntity outputBE) {
                        outputBE.receiveItem(this.processingItem);
                    } else {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), processingItem);
                    }
                }
                this.processingItem = ItemStack.EMPTY;
                this.processTick = 0;
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    private void handleOutput(Level level, BlockPos pos) {
        if (!processingItem.isEmpty()) {
            processTick++;
            if (processTick >= MAX_PROCESS_TICKS) {
                if (!level.isClientSide()) {
                    boolean inserted = false;
                    if (attachedInvPos != null) {
                        BlockEntity invBE = level.getBlockEntity(attachedInvPos);
                        if (invBE instanceof Container container) {
                            for (int i = 0; i < container.getContainerSize(); i++) {
                                if (container.canPlaceItem(i, processingItem)) {
                                    ItemStack slotStack = container.getItem(i);
                                    if (slotStack.isEmpty()) {
                                        container.setItem(i, processingItem);
                                        inserted = true;
                                        break;
                                    } else if (ItemStack.isSameItemSameComponents(slotStack, processingItem) && slotStack.getCount() + processingItem.getCount() <= slotStack.getMaxStackSize()) {
                                        slotStack.grow(processingItem.getCount());
                                        inserted = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (!inserted) {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), processingItem);
                    }
                }
                this.processingItem = ItemStack.EMPTY;
                this.processTick = 0;
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    // =========================================================================
    // NBT & SYNCING (Modernized 1.21.10 Codec Integration)
    // =========================================================================

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.targetPos != null) {
            output.store("TargetPos", BlockPos.CODEC, this.targetPos);
        }
        if (this.attachedInvPos != null) {
            output.store("AttInvPos", BlockPos.CODEC, this.attachedInvPos);
        }
        output.store("ProcessingItem", ItemStack.OPTIONAL_CODEC, this.processingItem);
        output.store("ProcessTick", Codec.INT, this.processTick);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("TargetPos", BlockPos.CODEC).ifPresent(p -> this.targetPos = p);
        input.read("AttInvPos", BlockPos.CODEC).ifPresent(p -> this.attachedInvPos = p);
        input.read("ProcessingItem", ItemStack.OPTIONAL_CODEC).ifPresent(i -> this.processingItem = i);
        this.processTick = input.read("ProcessTick", Codec.INT).orElse(0);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p) {
        return this.saveWithoutMetadata(p);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}