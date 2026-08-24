package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.materia.MateriaIchorStack;
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

public class SanguineConduitBlockEntity extends BlockEntity {

    private static final int CAPACITY = 8;

    protected MateriaIchorStack storedIchor = MateriaIchorStack.EMPTY;

    public SanguineConduitBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SANGUINE_CONDUIT_BE.get(), pos, state);
    }

    public int getCapacity() {
        return CAPACITY;
    }

    @NotNull
    public MateriaIchorStack getIchorInTank() {
        return this.storedIchor;
    }

    public int fill(MateriaIchorStack resource, boolean simulate) {
        if (resource.isEmpty()) return 0;
        if (!this.storedIchor.isEmpty() && !this.storedIchor.is(resource.getType())) return 0;

        int space    = Math.max(0, CAPACITY - this.storedIchor.getAmount());
        int accepted = Math.min(space, resource.getAmount());

        if (!simulate && accepted > 0) {
            if (this.storedIchor.isEmpty()) {
                this.storedIchor = new MateriaIchorStack(resource.getType(), accepted);
            } else {
                this.storedIchor.grow(accepted);
            }
            this.setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
        return accepted;
    }

    @NotNull
    public MateriaIchorStack drain(int maxDrain, boolean simulate) {
        if (this.storedIchor.isEmpty() || maxDrain <= 0) return MateriaIchorStack.EMPTY;
        int drained = Math.min(this.storedIchor.getAmount(), maxDrain);
        MateriaIchorStack result = new MateriaIchorStack(this.storedIchor.getType(), drained);
        if (!simulate) {
            this.storedIchor.shrink(drained);
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
        output.putInt("IchorAmount", this.storedIchor.getAmount());
        output.putInt("IchorType", this.storedIchor.isEmpty() ? -1 : this.storedIchor.getType().ordinal());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        int amount  = input.getIntOr("IchorAmount", 0);
        int typeOrd = input.getIntOr("IchorType", -1);
        if (amount <= 0 || typeOrd < 0 || typeOrd >= EssenceType.values().length) {
            this.storedIchor = MateriaIchorStack.EMPTY;
        } else {
            this.storedIchor = new MateriaIchorStack(EssenceType.values()[typeOrd], amount);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p) { return this.saveWithoutMetadata(p); }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
