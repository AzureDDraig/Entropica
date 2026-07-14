package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.materia.MateriaCoagulataStack;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

public class ViscousAgitatorBlockEntity extends BlockEntity {

    private static final int CAPACITY = 16;

    protected MateriaCoagulataStack storedCoagulata = MateriaCoagulataStack.EMPTY;

    public ViscousAgitatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VISCOUS_AGITATOR_BE.get(), pos, state);
    }

    public int getCapacity() {
        return CAPACITY;
    }

    @NotNull
    public MateriaCoagulataStack getCoagulataInTank() {
        return this.storedCoagulata;
    }

    public int fill(MateriaCoagulataStack resource, boolean simulate) {
        if (resource.isEmpty()) return 0;
        if (!this.storedCoagulata.isEmpty() && !this.storedCoagulata.is(resource.getType())) return 0;

        int space    = Math.max(0, CAPACITY - this.storedCoagulata.getAmount());
        int accepted = Math.min(space, resource.getAmount());

        if (!simulate && accepted > 0) {
            if (this.storedCoagulata.isEmpty()) {
                this.storedCoagulata = new MateriaCoagulataStack(resource.getType(), accepted);
            } else {
                this.storedCoagulata.grow(accepted);
            }
            this.setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
        return accepted;
    }

    @NotNull
    public MateriaCoagulataStack drain(int maxDrain, boolean simulate) {
        if (this.storedCoagulata.isEmpty() || maxDrain <= 0) return MateriaCoagulataStack.EMPTY;
        int drained = Math.min(this.storedCoagulata.getAmount(), maxDrain);
        MateriaCoagulataStack result = new MateriaCoagulataStack(this.storedCoagulata.getType(), drained);
        if (!simulate) {
            this.storedCoagulata.shrink(drained);
            this.setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
        return result;
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;
        // Future: pump-driven transfer logic
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("CoagulataAmount", this.storedCoagulata.getAmount());
        output.putInt("CoagulataType", this.storedCoagulata.isEmpty() ? -1 : this.storedCoagulata.getType().ordinal());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        int amount  = input.getIntOr("CoagulataAmount", 0);
        int typeOrd = input.getIntOr("CoagulataType", -1);
        if (amount <= 0 || typeOrd < 0 || typeOrd >= EssenceType.values().length) {
            this.storedCoagulata = MateriaCoagulataStack.EMPTY;
        } else {
            this.storedCoagulata = new MateriaCoagulataStack(EssenceType.values()[typeOrd], amount);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p) { return this.saveWithoutMetadata(p); }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
