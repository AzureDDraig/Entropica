package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.materia.MateriaTransmutataStack;
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

public class EquilibriumConduitBlockEntity extends BlockEntity {

    private static final int CAPACITY = 4;

    protected MateriaTransmutataStack storedTransmutata = MateriaTransmutataStack.EMPTY;

    public EquilibriumConduitBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EQUILIBRIUM_CONDUIT_BE.get(), pos, state);
    }

    public int getCapacity() {
        return CAPACITY;
    }

    @NotNull
    public MateriaTransmutataStack getTransmutataInTank() {
        return this.storedTransmutata;
    }

    public int fill(MateriaTransmutataStack resource, boolean simulate) {
        if (resource.isEmpty()) return 0;
        if (!this.storedTransmutata.isEmpty() && !this.storedTransmutata.is(resource.getType())) return 0;

        int space    = Math.max(0, CAPACITY - this.storedTransmutata.getAmount());
        int accepted = Math.min(space, resource.getAmount());

        if (!simulate && accepted > 0) {
            if (this.storedTransmutata.isEmpty()) {
                this.storedTransmutata = new MateriaTransmutataStack(resource.getType(), accepted);
            } else {
                this.storedTransmutata.grow(accepted);
            }
            this.setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
        return accepted;
    }

    @NotNull
    public MateriaTransmutataStack drain(int maxDrain, boolean simulate) {
        if (this.storedTransmutata.isEmpty() || maxDrain <= 0) return MateriaTransmutataStack.EMPTY;
        int drained = Math.min(this.storedTransmutata.getAmount(), maxDrain);
        MateriaTransmutataStack result = new MateriaTransmutataStack(this.storedTransmutata.getType(), drained);
        if (!simulate) {
            this.storedTransmutata.shrink(drained);
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
        output.putInt("TransmutataAmount", this.storedTransmutata.getAmount());
        output.putInt("TransmutataType", this.storedTransmutata.isEmpty() ? -1 : this.storedTransmutata.getType().ordinal());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        int amount  = input.getIntOr("TransmutataAmount", 0);
        int typeOrd = input.getIntOr("TransmutataType", -1);
        if (amount <= 0 || typeOrd < 0 || typeOrd >= EssenceType.values().length) {
            this.storedTransmutata = MateriaTransmutataStack.EMPTY;
        } else {
            this.storedTransmutata = new MateriaTransmutataStack(EssenceType.values()[typeOrd], amount);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p) { return this.saveWithoutMetadata(p); }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
