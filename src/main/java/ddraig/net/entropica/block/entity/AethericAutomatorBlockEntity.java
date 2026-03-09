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

public class AethericAutomatorBlockEntity extends BlockEntity {

    public NonNullList<ItemStack> savedPattern = NonNullList.withSize(25, ItemStack.EMPTY);
    public BlockPos linkedSynthesizer = null;

    // 0 = IDLE, 1 = PUSHING (Inputs), 2 = WAITING_FOR_CRAFT, 3 = PULLING (Output)
    public int state = 0;
    public int actionTimer = 0;

    // Client-side animation tracking for the mechanical hand
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
                player.displayClientMessage(Component.literal("§aAutomator Linked & Pattern Saved!"), true);
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            } else {
                player.displayClientMessage(Component.literal("§cSynthesizer is empty! Lay out the recipe first."), true);
            }
        } else {
            player.displayClientMessage(Component.literal("§cNo Aetheric Synthesizer found within 4 blocks!"), true);
        }
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        // Both Client and Server should find the synthesizer if it's missing
        if (linkedSynthesizer == null) {
            if (level.getGameTime() % 40 == 0) findSynthesizer();
        }

        if (level.isClientSide()) {
            // Track state changes to reset the animation tick
            if (this.state != this.lastState) {
                this.animationTick = 0;
                this.lastState = this.state;
            }

            // Advance animation if it's currently performing an action
            if (this.state == 1 || this.state == 3) {
                this.animationTick++;
            } else {
                this.animationTick = 0; // Hand rests while idle or waiting
            }

            // Tell the Synthesizer to hide the output item if we are pinching it!
            if (linkedSynthesizer != null && level.getBlockEntity(linkedSynthesizer) instanceof AethericSynthesizerBlockEntity synth) {
                // state 3 = PULLING. It pinches at t=0.2f (16 ticks out of 80)
                synth.isOutputBeingGrabbed = (this.state == 3 && this.animationTick >= 16);
            }
            return;
        }

        if (linkedSynthesizer == null) return;

        AethericSynthesizerBlockEntity synth = (AethericSynthesizerBlockEntity) level.getBlockEntity(linkedSynthesizer);
        if (synth == null) {
            linkedSynthesizer = null;
            return;
        }

        Container adjInv = findAdjacentInventory();

        if (this.state == 0) { // IDLE
            if (adjInv != null && isPatternSaved() && isSynthesizerEmpty(synth)) {
                if (hasRequiredItems(adjInv)) {
                    this.state = 1; // Start pushing
                    this.actionTimer = 80; // 4 seconds for the hand to reach, grab like dice, and slap
                    sync();
                }
            }
            // Catch edge case: Finished crafting but state got reset
            if (!synth.inventory.getItem(25).isEmpty() && !synth.isCrafting) {
                this.state = 3;
                this.actionTimer = 80; // 4 seconds to reach, pinch, and pull the item back
                sync();
            }
        } else if (this.state == 1) { // PUSHING
            actionTimer--;
            if (actionTimer <= 0) {
                if (extractRequiredItems(adjInv)) {
                    // This is the moment the hand "slaps" the table
                    pushItemsToSynthesizer(synth);
                    synth.attemptCrafting();
                }
                this.state = 2; // Move to Wait
                sync();
            }
        } else if (this.state == 2) { // WAITING FOR CRAFT
            if (!synth.isCrafting && !synth.inventory.getItem(25).isEmpty()) {
                this.state = 3; // Finished! Start pulling
                this.actionTimer = 80; // 4 seconds to pull the item back
                sync();
            } else if (!synth.isCrafting && synth.inventory.getItem(25).isEmpty()) {
                // Craft failed or was manually stolen by player
                this.state = 0;
                sync();
            }
        } else if (this.state == 3) { // PULLING
            actionTimer--;
            if (actionTimer <= 0) {
                ItemStack output = synth.inventory.getItem(25);
                if (!output.isEmpty() && adjInv != null) {
                    ItemStack remainder = insertIntoInventory(adjInv, output);
                    synth.inventory.setItem(25, remainder);

                    if (remainder.isEmpty()) {
                        this.state = 0; // Successfully stored, ready to repeat
                    } else {
                        this.actionTimer = 20; // Chest is full! Retry in 1 second
                    }
                    sync();
                } else {
                    this.state = 0;
                    sync();
                }
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
        for (int i = 0; i < 26; i++) {
            if (!synth.inventory.getItem(i).isEmpty()) return false;
        }
        return true;
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
        if (linkedSynthesizer != null) output.putLong("LinkedSynth", linkedSynthesizer.asLong());

        for (int i = 0; i < 25; i++) {
            ItemStack stack = savedPattern.get(i);
            output.putString("PatItem_" + i, BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            output.putInt("PatCount_" + i, stack.getCount());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.state = input.getIntOr("AutoState", 0);
        this.actionTimer = input.getIntOr("ActionTimer", 0);
        long linked = input.getLongOr("LinkedSynth", -1L);
        if (linked != -1L) this.linkedSynthesizer = BlockPos.of(linked);
        else this.linkedSynthesizer = null;

        for (int i = 0; i < 25; i++) {
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

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p) { return this.saveWithoutMetadata(p); }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}