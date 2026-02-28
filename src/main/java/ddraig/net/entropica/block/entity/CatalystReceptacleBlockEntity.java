package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class CatalystReceptacleBlockEntity extends BlockEntity {
    private ItemStack catalyst = ItemStack.EMPTY;

    public CatalystReceptacleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CATALYST_RECEPTACLE_BE.get(), pos, state);
    }

    public void setCatalyst(ItemStack stack) {
        this.catalyst = stack;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public ItemStack getCatalyst() {
        return this.catalyst;
    }

    public boolean hasCatalyst() {
        return !this.catalyst.isEmpty();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!this.catalyst.isEmpty()) {
            output.store("CatalystItem", ItemStack.CODEC, this.catalyst);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.catalyst = input.read("CatalystItem", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}