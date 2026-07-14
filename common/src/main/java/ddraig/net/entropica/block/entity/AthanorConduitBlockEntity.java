package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.materia.MateriaLiminaliaStack;
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

public class AthanorConduitBlockEntity extends BlockEntity {

    private static final int CAPACITY = 1;

    protected MateriaLiminaliaStack storedLiminalia = MateriaLiminaliaStack.EMPTY;

    public AthanorConduitBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ATHANOR_CONDUIT_BE.get(), pos, state);
    }

    public int getCapacity() {
        return CAPACITY;
    }

    @NotNull
    public MateriaLiminaliaStack getLiminaliaInTank() {
        return this.storedLiminalia;
    }

    public int fill(MateriaLiminaliaStack resource, boolean simulate) {
        if (resource.isEmpty()) return 0;
        if (!this.storedLiminalia.isEmpty() && !this.storedLiminalia.is(resource.getType())) return 0;

        int space    = Math.max(0, CAPACITY - this.storedLiminalia.getAmount());
        int accepted = Math.min(space, resource.getAmount());

        if (!simulate && accepted > 0) {
            if (this.storedLiminalia.isEmpty()) {
                this.storedLiminalia = new MateriaLiminaliaStack(resource.getType(), accepted);
            } else {
                this.storedLiminalia.grow(accepted);
            }
            this.setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
        return accepted;
    }

    @NotNull
    public MateriaLiminaliaStack drain(int maxDrain, boolean simulate) {
        if (this.storedLiminalia.isEmpty() || maxDrain <= 0) return MateriaLiminaliaStack.EMPTY;
        int drained = Math.min(this.storedLiminalia.getAmount(), maxDrain);
        MateriaLiminaliaStack result = new MateriaLiminaliaStack(this.storedLiminalia.getType(), drained);
        if (!simulate) {
            this.storedLiminalia.shrink(drained);
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
        output.putInt("LiminaliaAmount", this.storedLiminalia.getAmount());
        output.putInt("LiminaliaType", this.storedLiminalia.isEmpty() ? -1 : this.storedLiminalia.getType().ordinal());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        int amount  = input.getIntOr("LiminaliaAmount", 0);
        int typeOrd = input.getIntOr("LiminaliaType", -1);
        if (amount <= 0 || typeOrd < 0 || typeOrd >= EssenceType.values().length) {
            this.storedLiminalia = MateriaLiminaliaStack.EMPTY;
        } else {
            this.storedLiminalia = new MateriaLiminaliaStack(EssenceType.values()[typeOrd], amount);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p) { return this.saveWithoutMetadata(p); }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
