package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.materia.MateriaVolatilisStack;
import ddraig.net.entropica.api.pressure.IPressureHandler;
import ddraig.net.entropica.api.pressure.PressureNetworkManager;
import ddraig.net.entropica.api.pressure.PressureNetworkHelper;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

public class VoltaicConduitBlockEntity extends BlockEntity implements IPressureHandler {



    protected MateriaVolatilisStack storedVolatilis = MateriaVolatilisStack.EMPTY;
    private boolean managedByGraph = false;
    private Direction activeBoostDirection = null;
    private float activeBoostStrength = 0.0f;
    private boolean firstTick = true;

    public VoltaicConduitBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VOLTAIC_CONDUIT_BE.get(), pos, state);
    }

    public int getCapacity() {
        return PressureNetworkHelper.getConduitCapacity(this.getBlockState(), 2);
    }

    @Override
    public int getSafeCapacity() { return getCapacity(); }
    @Override
    public int getAbsoluteCapacity() { return PressureNetworkHelper.getConduitAbsoluteCapacity(this.getBlockState(), 2); }
    @Override
    @NotNull
    public ddraig.net.entropica.api.materia.MateriaStack getMateriaInTank() { return getVolatilisInTank(); }
    @Override
    public int fill(ddraig.net.entropica.api.materia.MateriaStack resource, boolean simulate) {
        if (resource instanceof MateriaVolatilisStack vol) return fill(vol, simulate);
        return 0;
    }
    @Override
    public int getTransferRate() {
        return PressureNetworkHelper.getConduitTransferRate(this.getBlockState(), 2, this.storedVolatilis.getAmount());
    }
    @Override
    public float getResistance() { return PressureNetworkHelper.getConduitResistance(this.getBlockState(), 0.03f); }
    @Override
    public boolean isManagedByGraph() { return this.managedByGraph; }
    @Override
    public void setManagedByGraph(boolean managed) { this.managedByGraph = managed; }
    @Override
    public void applyActiveBoost(Direction direction, float strength) {
        this.activeBoostDirection = direction;
        this.activeBoostStrength = strength;
    }
    @Override
    public Direction getActiveBoostDirection() { return this.activeBoostDirection; }
    @Override
    public float getActiveBoostStrength() { return this.activeBoostStrength; }
    @Override
    public void clearActiveBoost() {
        this.activeBoostDirection = null;
        this.activeBoostStrength = 0.0f;
    }

    @Override
    public void setRemoved() {
        PressureNetworkManager.onBlockEntityRemoved(this);
        super.setRemoved();
    }

    @NotNull
    public MateriaVolatilisStack getVolatilisInTank() {
        return this.storedVolatilis;
    }

    public int fill(MateriaVolatilisStack resource, boolean simulate) {
        if (resource.isEmpty()) return 0;
        if (!this.storedVolatilis.isEmpty() && !this.storedVolatilis.is(resource.getType())) return 0;

        int space    = Math.max(0, getCapacity() - this.storedVolatilis.getAmount());
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

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;

        if (firstTick) {
            firstTick = false;
            PressureNetworkManager.onBlockAdded(level, pos, this);
        }

        PressureNetworkManager.tick(level);
        if (!isManagedByGraph()) {
            PressureNetworkHelper.tickLocalEqualization(level, pos, this);
        }
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
