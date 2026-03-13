package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.recipe.AethericSynthesizerRecipe;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AethericSynthesizerBlockEntity extends BlockEntity {

    // 25 input slots + 4 output slots = 29
    public final SimpleContainer inventory = new SimpleContainer(29) {
        @Override
        public void setChanged() {
            super.setChanged();
            AethericSynthesizerBlockEntity.this.setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    public boolean isCrafting = false;
    public int craftingProgress = 0;
    public int maxCraftingProgress = 80;
    public List<ItemStack> craftingResults = new ArrayList<>();

    public boolean isOutputBeingGrabbed = false;

    public AethericSynthesizerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AETHERIC_SYNTHESIZER_BE.get(), pos, state);
    }

    // Safely drops all contents when the block is broken in 1.21.2+
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (this.level != null) {
            Containers.dropContents(this.level, pos, this.inventory);
            this.level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
        super.preRemoveSideEffects(pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (isCrafting) {
            if (craftingProgress < maxCraftingProgress) {
                craftingProgress++;
            }
            if (craftingProgress >= maxCraftingProgress) {
                if (!level.isClientSide()) {
                    finishCrafting();
                }
            }
        }
    }

    public boolean attemptCrafting() {
        if (isCrafting) return false;

        // Ensure all 4 output slots are completely empty before starting a new craft
        for (int i = 25; i < 29; i++) {
            if (!inventory.getItem(i).isEmpty()) return false;
        }

        AethericSynthesizerRecipe matchedRecipe = getMatchedRecipe();

        if (matchedRecipe != null) {
            isCrafting = true;
            craftingProgress = 0;
            maxCraftingProgress = 80;

            craftingResults.clear();
            for (ItemStack res : matchedRecipe.getResults()) {
                craftingResults.add(res.copy());
            }

            setChanged();
            if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            return true;
        }

        return false;
    }

    // --- NEW: TERMINAL GUI BULK CRAFTING LOGIC ---
    public void instantCraft(boolean bulk) {
        if (isCrafting) return; // Do not interrupt physical holograms

        int craftsPerformed = 0;
        int maxAllowed = bulk ? 64 : 1;

        while (craftsPerformed < maxAllowed) {
            AethericSynthesizerRecipe recipe = getMatchedRecipe();
            if (recipe == null) break;

            List<ItemStack> results = recipe.getResults();
            if (!canInsertResults(results)) break;

            // 1. Consume 1 of every item actively on the 5x5 grid
            for (int i = 0; i < 25; i++) {
                if (!inventory.getItem(i).isEmpty()) {
                    inventory.getItem(i).shrink(1);
                }
            }

            // 2. Insert the output items safely
            insertResults(results);
            craftsPerformed++;
        }

        if (craftsPerformed > 0) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    private AethericSynthesizerRecipe getMatchedRecipe() {
        if (this.level instanceof ServerLevel serverLevel) {
            var recipes = serverLevel.recipeAccess().recipeMap().byType(ModRecipes.AETHERIC_SYNTHESIZER_TYPE.get());
            for (var holder : recipes) {
                if (holder.value().matchesGrid(this.inventory)) {
                    return holder.value();
                }
            }
        }
        for (AethericSynthesizerRecipe recipe : AethericSynthesizerRecipe.getHardcodedRecipes()) {
            if (recipe.matchesGrid(this.inventory)) {
                return recipe;
            }
        }
        return null;
    }

    private boolean canInsertResults(List<ItemStack> results) {
        // Clone the output slots so we can simulate inserting
        SimpleContainer sim = new SimpleContainer(4);
        for (int i = 0; i < 4; i++) {
            sim.setItem(i, this.inventory.getItem(25 + i).copy());
        }

        for (ItemStack res : results) {
            ItemStack remainder = res.copy();
            for (int i = 0; i < 4; i++) {
                if (remainder.isEmpty()) break;
                ItemStack slot = sim.getItem(i);

                if (slot.isEmpty()) {
                    sim.setItem(i, remainder.copy());
                    remainder.setCount(0);
                } else if (ItemStack.isSameItemSameComponents(slot, remainder)) {
                    int space = slot.getMaxStackSize() - slot.getCount();
                    int transfer = Math.min(space, remainder.getCount());
                    slot.grow(transfer);
                    remainder.shrink(transfer);
                }
            }
            if (!remainder.isEmpty()) return false; // Output grid is too full!
        }
        return true;
    }

    private void insertResults(List<ItemStack> results) {
        for (ItemStack res : results) {
            ItemStack remainder = res.copy();
            for (int i = 25; i < 29; i++) {
                if (remainder.isEmpty()) break;
                ItemStack slot = this.inventory.getItem(i);

                if (slot.isEmpty()) {
                    this.inventory.setItem(i, remainder.copy());
                    remainder.setCount(0);
                } else if (ItemStack.isSameItemSameComponents(slot, remainder)) {
                    int space = slot.getMaxStackSize() - slot.getCount();
                    int transfer = Math.min(space, remainder.getCount());
                    slot.grow(transfer);
                    remainder.shrink(transfer);
                }
            }
        }
    }

    private void finishCrafting() {
        for (int i = 0; i < 25; i++) {
            inventory.getItem(i).shrink(1);
        }

        for (int i = 0; i < craftingResults.size() && i < 4; i++) {
            inventory.setItem(25 + i, craftingResults.get(i).copy());
        }

        isCrafting = false;
        craftingProgress = 0;
        craftingResults.clear();

        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        // Safely serializes the full ItemStack (including Data Components) via Codec!
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            output.store("SynItemStack_" + i, ItemStack.OPTIONAL_CODEC, stack);
        }

        output.putBoolean("IsCrafting", isCrafting);
        output.putInt("CraftingProgress", craftingProgress);
        output.putInt("MaxCraftingProgress", maxCraftingProgress);

        output.putInt("CraftingResultCount", craftingResults.size());
        for (int i = 0; i < craftingResults.size(); i++) {
            output.store("CraftingResultStack_" + i, ItemStack.OPTIONAL_CODEC, craftingResults.get(i));
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            Optional<ItemStack> optStack = input.read("SynItemStack_" + i, ItemStack.OPTIONAL_CODEC);
            if (optStack.isPresent()) {
                inventory.setItem(i, optStack.get());
            } else {
                // Fallback for older saves that only saved the base item and count
                String itemStr = input.getStringOr("SynItem_" + i, "minecraft:air");
                int count = input.getIntOr("SynCount_" + i, 0);
                if (!itemStr.equals("minecraft:air") && count > 0) {
                    net.minecraft.resources.ResourceLocation rl = net.minecraft.resources.ResourceLocation.tryParse(itemStr);
                    if (rl != null) {
                        int finalI = i;
                        BuiltInRegistries.ITEM.getOptional(rl).ifPresent(holder ->
                                inventory.setItem(finalI, new ItemStack(holder, count))
                        );
                    }
                } else {
                    inventory.setItem(i, ItemStack.EMPTY);
                }
            }
        }

        isCrafting = input.getBooleanOr("IsCrafting", false);
        craftingProgress = input.getIntOr("CraftingProgress", 0);
        maxCraftingProgress = input.getIntOr("MaxCraftingProgress", 80);

        craftingResults.clear();
        int resCount = input.getIntOr("CraftingResultCount", 0);
        for (int i = 0; i < resCount; i++) {
            Optional<ItemStack> optStack = input.read("CraftingResultStack_" + i, ItemStack.OPTIONAL_CODEC);
            if (optStack.isPresent() && !optStack.get().isEmpty()) {
                craftingResults.add(optStack.get());
            } else {
                // Legacy fallback
                String resItem = input.getStringOr("CraftingResultItem_" + i, "minecraft:air");
                int rCount = input.getIntOr("CraftingResultCount_" + i, 0);
                if (!resItem.equals("minecraft:air") && rCount > 0) {
                    net.minecraft.resources.ResourceLocation rl = net.minecraft.resources.ResourceLocation.tryParse(resItem);
                    if (rl != null) {
                        BuiltInRegistries.ITEM.getOptional(rl).ifPresent(holder ->
                                craftingResults.add(new ItemStack(holder, rCount))
                        );
                    }
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