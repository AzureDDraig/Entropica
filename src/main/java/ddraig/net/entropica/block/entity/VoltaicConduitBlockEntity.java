package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.materia.MateriaVolatilisStack;
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

public class VoltaicConduitBlockEntity extends BlockEntity {

    private static final int CAPACITY = 32;

    protected MateriaVolatilisStack storedVolatilis = MateriaVolatilisStack.EMPTY;

    public VoltaicConduitBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VOLTAIC_CONDUIT_BE.get(), pos, state);
    }

    public int getCapacity() {
        return CAPACITY;
    }

    @NotNull
    public MateriaVolatilisStack getVolatilisInTank() {
        return this.storedVolatilis;
    }

    public int fill(MateriaVolatilisStack resource, boolean simulate) {
        if (resource.isEmpty()) return 0;
        if (!this.storedVolatilis.isEmpty() && !this.storedVolatilis.is(resource.getType())) return 0;

        int space    = Math.max(0, CAPACITY - this.storedVolatilis.getAmount());
        int accepted = Math.min(space, resource.getAmount());

        if (!simulate && accepted > 0) {
            if (this.storedVolatilis.isEmpty()) {
                this.storedVolatilis = new MateriaVolatilisStack(resource.getType(), accepted);
            } else {
                this.storedVolatilis.grow(accepted);
            }
            this.setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
        return accepted;
    }

    @NotNull
    public MateriaVolatilisStack drain(int maxDrain, boolean simulate) {
        if (this.storedVolatilis.isEmpty() || maxDrain <= 0) return MateriaVolatilisStack.EMPTY;
        int drained = Math.min(this.storedVolatilis.getAmount(), maxDrain);
        MateriaVolatilisStack result = new MateriaVolatilisStack(this.storedVolatilis.getType(), drained);
        if (!simulate) {
            this.storedVolatilis.shrink(drained);
            this.setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
        return result;
    }

    // Currently a passive conduit — fluid only moves when driven by an external pump.
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;
        // Future: pump-driven transfer logic
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("VolatilisAmount", this.storedVolatilis.getAmount());
        output.putInt("VolatilisType", this.storedVolatilis.isEmpty() ? -1 : this.storedVolatilis.getType().ordinal());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        int amount  = input.getIntOr("VolatilisAmount", 0);
        int typeOrd = input.getIntOr("VolatilisType", -1);
        if (amount <= 0 || typeOrd < 0 || typeOrd >= EssenceType.values().length) {
            this.storedVolatilis = MateriaVolatilisStack.EMPTY;
        } else {
            this.storedVolatilis = new MateriaVolatilisStack(EssenceType.values()[typeOrd], amount);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p) { return this.saveWithoutMetadata(p); }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
