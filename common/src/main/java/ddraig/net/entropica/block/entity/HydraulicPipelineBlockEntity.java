package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.materia.ILiquidMateriaHandler;
import ddraig.net.entropica.api.materia.MateriaLiquidaStack;
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

public class HydraulicPipelineBlockEntity extends BlockEntity implements ILiquidMateriaHandler {



    protected MateriaLiquidaStack storedLiquid = MateriaLiquidaStack.EMPTY;
    private boolean managedByGraph = false;
    private Direction activeBoostDirection = null;
    private float activeBoostStrength = 0.0f;
    private boolean firstTick = true;

    public HydraulicPipelineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HYDRAULIC_PIPELINE_BE.get(), pos, state);
    }

    @Override
    public int getCapacity() {
        return PressureNetworkHelper.getConduitCapacity(this.getBlockState(), 1);
    }

    @Override
    public int getTransferRate() {
        return PressureNetworkHelper.getConduitTransferRate(this.getBlockState(), 1, this.storedLiquid.getAmount());
    }

    @Override
    public float getResistance() {
        return PressureNetworkHelper.getConduitResistance(this.getBlockState(), 0.04f); // Viscosity friction
    }

    @Override
    public boolean isManagedByGraph() {
        return this.managedByGraph;
    }

    @Override
    public void setManagedByGraph(boolean managed) {
        this.managedByGraph = managed;
    }

    @Override
    public void applyActiveBoost(Direction direction, float strength) {
        this.activeBoostDirection = direction;
        this.activeBoostStrength = strength;
    }

    @Override
    public Direction getActiveBoostDirection() {
        return this.activeBoostDirection;
    }

    @Override
    public float getActiveBoostStrength() {
        return this.activeBoostStrength;
    }

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

    @Override
    @NotNull
    public MateriaLiquidaStack getLiquidInTank() {
        return this.storedLiquid;
    }

    @Override
    public int fill(MateriaLiquidaStack resource, boolean simulate) {
        if (resource.isEmpty()) return 0;
        if (!this.storedLiquid.isEmpty() && !this.storedLiquid.is(resource.getType())) return 0;

        int currentAmount = this.storedLiquid.getAmount();
        int space = Math.max(0, getCapacity() - currentAmount);
        int accepted = Math.min(space, resource.getAmount());

        if (!simulate && accepted > 0) {
            if (this.storedLiquid.isEmpty()) {
                this.storedLiquid = new MateriaLiquidaStack(resource.getType(), accepted);
            } else {
                this.storedLiquid.grow(accepted);
            }
            this.setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
        return accepted;
    }

    @Override
    @NotNull
    public MateriaLiquidaStack drainLiquid(int maxDrain, boolean simulate) {
        if (this.storedLiquid.isEmpty() || maxDrain <= 0) return MateriaLiquidaStack.EMPTY;
        int drained = Math.min(this.storedLiquid.getAmount(), maxDrain);
        MateriaLiquidaStack result = new MateriaLiquidaStack(this.storedLiquid.getType(), drained);
        if (!simulate) {
            this.storedLiquid.shrink(drained);
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
        output.putInt("LiquidAmount", this.storedLiquid.getAmount());
        output.putInt("LiquidType", this.storedLiquid.isEmpty() ? -1 : this.storedLiquid.getType().ordinal());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        int amount  = input.getIntOr("LiquidAmount", 0);
        int typeOrd = input.getIntOr("LiquidType", -1);
        if (amount <= 0 || typeOrd < 0 || typeOrd >= EssenceType.values().length) {
            this.storedLiquid = MateriaLiquidaStack.EMPTY;
        } else {
            this.storedLiquid = new MateriaLiquidaStack(EssenceType.values()[typeOrd], amount);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p) { return this.saveWithoutMetadata(p); }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
