package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.materia.ILiquidMateriaHandler;
import ddraig.net.entropica.api.materia.MateriaLiquidaStack;
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

public class HydraulicPipelineBlockEntity extends BlockEntity implements ILiquidMateriaHandler {

    private static final int CAPACITY = 64;

    protected MateriaLiquidaStack storedLiquid = MateriaLiquidaStack.EMPTY;

    public HydraulicPipelineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HYDRAULIC_PIPELINE_BE.get(), pos, state);
    }

    @Override
    public int getCapacity() {
        return CAPACITY;
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
        int space = Math.max(0, CAPACITY - currentAmount);
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

    // Currently a passive conduit — fluid only moves when driven by an external pump.
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;
        // Future: pump-driven transfer logic
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
