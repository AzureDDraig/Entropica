package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AethericAutomatorBlockEntity extends BlockEntity {

    public NonNullList<ItemStack> savedPattern = NonNullList.withSize(25, ItemStack.EMPTY);
    public BlockPos linkedSynthesizer = null;

    public int state = 0;
    public int actionTimer = 0;
    public int animationTick = 0;
    private int lastState = 0;

    public AethericAutomatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AETHERIC_AUTOMATOR_BE.get(), pos, state);
    }

    public void saveRecipeFromSynthesizer(Player player) {
        findSynthesizer();
        if (linkedSynthesizer != null && level.getBlockEntity(linkedSynthesizer) instanceof AethericSynthesizerBlockEntity synth) {
            boolean hasItems = false;
            for (int i = 0; i < 25; i++) {
                savedPattern.set(i, synth.inventory.getItem(i).copy());
                if (!savedPattern.get(i).isEmpty()) hasItems = true;
            }
            if (hasItems) {
                player.displayClientMessage(Component.translatable("msg.entropica.automator_linked_pattern_saved"), true);
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            } else {
                player.displayClientMessage(Component.translatable("msg.entropica.synthesizer_is_empty_lay_out_the"), true);
            }
        } else {
            player.displayClientMessage(Component.translatable("msg.entropica.no_aetheric_synthesizer_found_within_4"), true);
        }
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (linkedSynthesizer == null) {
            if (level.getGameTime() % 40 == 0) findSynthesizer();
        }

        if (level.isClientSide()) {
            if (this.state != this.lastState) {
                this.animationTick = 0;
                this.lastState = this.state;
            }
            if (this.state == 1 || this.state == 3) {
                this.animationTick++;
            } else {
                this.animationTick = 0;
            }
            if (linkedSynthesizer != null && level.getBlockEntity(linkedSynthesizer) instanceof AethericSynthesizerBlockEntity synth) {
                synth.isOutputBeingGrabbed = (this.state == 3 && this.animationTick >= 16);
            }
            return;
        }

        if (linkedSynthesizer == null) return;
        AethericSynthesizerBlockEntity synth = (AethericSynthesizerBlockEntity) level.getBlockEntity(linkedSynthesizer);
        if (synth == null) { linkedSynthesizer = null; return; }

        Container adjInv = findAdjacentInventory();

        if (this.state == 0) {
            if (adjInv != null && isPatternSaved() && isSynthesizerEmpty(synth)) {
                if (hasRequiredItems(adjInv)) {
                    this.state = 1;
                    this.actionTimer = 80;
                    sync();
                }
            }
            if (hasAnyOutput(synth) && !synth.isCrafting) {
                this.state = 3;
                this.actionTimer = 80;
                sync();
            }
        } else if (this.state == 1) {
            actionTimer--;
            if (actionTimer <= 0) {
                if (extractRequiredItems(adjInv)) {
                    pushItemsToSynthesizer(synth);
                    synth.attemptCrafting();
                }
                this.state = 2;
                sync();
            }
        } else if (this.state == 2) {
            if (!synth.isCrafting && hasAnyOutput(synth)) {
                this.state = 3;
                this.actionTimer = 80;
                sync();
            } else if (!synth.isCrafting && !hasAnyOutput(synth)) {
                this.state = 0;
                sync();
            }
        } else if (this.state == 3) {
            actionTimer--;
            if (actionTimer <= 0) {
                boolean stillHasItems = false;
                if (adjInv != null) {
                    for (int i = 25; i < 29; i++) {
                        ItemStack output = synth.inventory.getItem(i);
                        if (!output.isEmpty()) {
                            ItemStack remainder = insertIntoInventory(adjInv, output);
                            synth.inventory.setItem(i, remainder);
                            if (!remainder.isEmpty()) stillHasItems = true;
                        }
                    }
                } else {
                    stillHasItems = true; // Chest broken? Stall.
                }

                if (!stillHasItems) {
                    this.state = 0;
                } else {
                    this.actionTimer = 20; // Chest is full! Retry in 1 second
                }
                sync();
            }
        }
    }

    private void findSynthesizer() {
        if (level == null) return;
        for (int x = -4; x <= 4; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -4; z <= 4; z++) {
                    BlockPos checkPos = worldPosition.offset(x, y, z);
                    if (level.getBlockEntity(checkPos) instanceof AethericSynthesizerBlockEntity) {
                        linkedSynthesizer = checkPos;
                        return;
                    }
                }
            }
        }
        linkedSynthesizer = null;
    }

    private Container findAdjacentInventory() {
        for (Direction dir : Direction.values()) {
            BlockEntity be = level.getBlockEntity(worldPosition.relative(dir));
            if (be instanceof Container container && !(be instanceof AethericSynthesizerBlockEntity)) {
                return container;
            }
        }
        return null;
    }

    private boolean isPatternSaved() {
        for (ItemStack stack : savedPattern) {
            if (!stack.isEmpty()) return true;
        }
        return false;
    }

    private boolean isSynthesizerEmpty(AethericSynthesizerBlockEntity synth) {
        for (int i = 0; i < 29; i++) {
            if (!synth.inventory.getItem(i).isEmpty()) return false;
        }
        return true;
    }

    private boolean hasAnyOutput(AethericSynthesizerBlockEntity synth) {
        for (int i = 25; i < 29; i++) {
            if (!synth.inventory.getItem(i).isEmpty()) return true;
        }
        return false;
    }

    private boolean hasRequiredItems(Container inv) {
        List<ItemStack> needed = new ArrayList<>();
        for (ItemStack stack : savedPattern) {
            if (!stack.isEmpty()) needed.add(stack.copy());
        }

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack inSlot = inv.getItem(i);
            if (!inSlot.isEmpty()) {
                for (ItemStack req : needed) {
                    if (req.getCount() > 0 && ItemStack.isSameItemSameComponents(inSlot, req)) {
                        int take = Math.min(inSlot.getCount(), req.getCount());
                        req.shrink(take);
                    }
                }
            }
        }

        for (ItemStack req : needed) {
            if (req.getCount() > 0) return false;
        }
        return true;
    }

    private boolean extractRequiredItems(Container inv) {
        if (!hasRequiredItems(inv)) return false;

        List<ItemStack> needed = new ArrayList<>();
        for (ItemStack stack : savedPattern) {
            if (!stack.isEmpty()) needed.add(stack.copy());
        }

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack inSlot = inv.getItem(i);
            if (!inSlot.isEmpty()) {
                for (ItemStack req : needed) {
                    if (req.getCount() > 0 && ItemStack.isSameItemSameComponents(inSlot, req)) {
                        int take = Math.min(inSlot.getCount(), req.getCount());
                        req.shrink(take);
                        inv.removeItem(i, take);
                    }
                }
            }
        }
        return true;
    }

    private void pushItemsToSynthesizer(AethericSynthesizerBlockEntity synth) {
        for (int i = 0; i < 25; i++) {
            if (!savedPattern.get(i).isEmpty()) {
                synth.inventory.setItem(i, savedPattern.get(i).copy());
            }
        }
    }

    private ItemStack insertIntoInventory(Container inv, ItemStack stack) {
        ItemStack remainder = stack.copy();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (remainder.isEmpty()) break;
            ItemStack inSlot = inv.getItem(i);

            if (inSlot.isEmpty()) {
                inv.setItem(i, remainder.copy());
                remainder.setCount(0);
                break;
            } else if (ItemStack.isSameItemSameComponents(inSlot, remainder)) {
                int space = inSlot.getMaxStackSize() - inSlot.getCount();
                int transfer = Math.min(space, remainder.getCount());
                if (transfer > 0) {
                    inSlot.grow(transfer);
                    remainder.shrink(transfer);
                }
            }
        }
        return remainder;
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("AutoState", this.state);
        output.putInt("ActionTimer", this.actionTimer);

        if (linkedSynthesizer != null) {
            output.putLong("LinkedSynth", linkedSynthesizer.asLong());
        } else {
            output.putLong("LinkedSynth", -1L);
        }

        for (int i = 0; i < 25; i++) {
            ItemStack stack = savedPattern.get(i);
            output.store("PatItemStack_" + i, ItemStack.OPTIONAL_CODEC, stack);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.state = input.getIntOr("AutoState", 0);
        this.actionTimer = input.getIntOr("ActionTimer", 0);

        long linked = input.getLongOr("LinkedSynth", -1L);
        if (linked != -1L) {
            this.linkedSynthesizer = BlockPos.of(linked);
        } else {
            this.linkedSynthesizer = null;
        }

        for (int i = 0; i < 25; i++) {
            Optional<ItemStack> optStack = input.read("PatItemStack_" + i, ItemStack.OPTIONAL_CODEC);
            if (optStack.isPresent()) {
                savedPattern.set(i, optStack.get());
            } else {
                // Fallback for older saves
                String itemStr = input.getStringOr("PatItem_" + i, "minecraft:air");
                int count = input.getIntOr("PatCount_" + i, 0);
                if (!itemStr.equals("minecraft:air") && count > 0) {
                    net.minecraft.resources.ResourceLocation rl = net.minecraft.resources.ResourceLocation.tryParse(itemStr);
                    if (rl != null) {
                        int finalI = i;
                        BuiltInRegistries.ITEM.getOptional(rl).ifPresent(holder ->
                                savedPattern.set(finalI, new ItemStack(holder, count))
                        );
                    }
                } else {
                    savedPattern.set(i, ItemStack.EMPTY);
                }
            }
        }
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