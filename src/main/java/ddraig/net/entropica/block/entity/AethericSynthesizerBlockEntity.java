package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class AethericSynthesizerBlockEntity extends BlockEntity {

    // 25 input slots + 1 output slot = 26
    public final SimpleContainer inventory = new SimpleContainer(26) {
        @Override
        public void setChanged() {
            super.setChanged();
            AethericSynthesizerBlockEntity.this.setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    // Animation & Crafting State
    public boolean isCrafting = false;
    public int craftingProgress = 0;
    public int maxCraftingProgress = 80; // 4 Seconds
    public ItemStack craftingResult = ItemStack.EMPTY;

    // Transient client state for the Automator hand
    public boolean isOutputBeingGrabbed = false;

    public AethericSynthesizerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AETHERIC_SYNTHESIZER_BE.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (isCrafting) {

            // Only increment if we haven't reached the max yet
            if (craftingProgress < maxCraftingProgress) {
                craftingProgress++;
            }

            if (craftingProgress >= maxCraftingProgress) {
                if (!level.isClientSide()) {
                    finishCrafting();
                } else {
                    // Client stays exactly at 100% and DOES NOT set isCrafting to false yet!
                    // This prevents the inputs from flashing visually before the server's update packet arrives.
                }
            }
        }
    }

    public boolean attemptCrafting() {
        if (isCrafting) return false;

        // Ensure the output slot is empty before we start
        if (!inventory.getItem(25).isEmpty()) return false;

        // --- TEST RECIPE ---
        // Requires exactly 25 Essence Enriched Glass, outputting 1 Essence Enriched Glass
        boolean isTestRecipe = true;
        for (int i = 0; i < 25; i++) {
            if (!inventory.getItem(i).is(ModItems.ESSENCE_ENRICHED_GLASS_ITEM.get())) {
                isTestRecipe = false;
                break;
            }
        }

        if (isTestRecipe) {
            isCrafting = true;
            craftingProgress = 0;
            maxCraftingProgress = 80;
            craftingResult = new ItemStack(ModItems.ESSENCE_ENRICHED_GLASS_ITEM.get(), 1);

            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
            return true;
        }

        return false;
    }

    private void finishCrafting() {
        // Delete all input items
        for (int i = 0; i < 25; i++) {
            inventory.getItem(i).shrink(1);
        }

        // Put result in slot 25
        inventory.setItem(25, craftingResult.copy());

        isCrafting = false;
        craftingProgress = 0;
        craftingResult = ItemStack.EMPTY;

        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            output.putString("SynItem_" + i, BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            output.putInt("SynCount_" + i, stack.getCount());
        }

        output.putBoolean("IsCrafting", isCrafting);
        output.putInt("CraftingProgress", craftingProgress);
        output.putInt("MaxCraftingProgress", maxCraftingProgress);
        output.putString("CraftingResultItem", BuiltInRegistries.ITEM.getKey(craftingResult.getItem()).toString());
        output.putInt("CraftingResultCount", craftingResult.getCount());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        for (int i = 0; i < inventory.getContainerSize(); i++) {
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

        isCrafting = input.getBooleanOr("IsCrafting", false);
        craftingProgress = input.getIntOr("CraftingProgress", 0);
        maxCraftingProgress = input.getIntOr("MaxCraftingProgress", 80);

        String resItem = input.getStringOr("CraftingResultItem", "minecraft:air");
        int resCount = input.getIntOr("CraftingResultCount", 0);
        if (!resItem.equals("minecraft:air") && resCount > 0) {
            net.minecraft.resources.ResourceLocation rl = net.minecraft.resources.ResourceLocation.tryParse(resItem);
            if (rl != null) {
                BuiltInRegistries.ITEM.getOptional(rl).ifPresent(holder ->
                        craftingResult = new ItemStack(holder, resCount)
                );
            }
        } else {
            craftingResult = ItemStack.EMPTY;
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