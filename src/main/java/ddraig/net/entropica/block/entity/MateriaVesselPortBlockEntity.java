package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.materia.IVaporHandler;
import ddraig.net.entropica.api.materia.IVaporMultiblockController;
import ddraig.net.entropica.api.materia.MateriaFumusStack;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class MateriaVesselPortBlockEntity extends BlockEntity implements IVaporHandler {

    public enum PortMode {
        INPUT, OUTPUT;
        public PortMode next() {
            return this == INPUT ? OUTPUT : INPUT;
        }
    }

    @Nullable
    private BlockPos controllerPos = null;
    private PortMode mode = PortMode.INPUT;

    public MateriaVesselPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MATERIA_VESSEL_PORT_BE.get(), pos, state);
    }

    public void toggleMode() {
        this.mode = this.mode.next();
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public PortMode getMode() {
        return mode;
    }

    public void setControllerPos(@Nullable BlockPos pos) {
        this.controllerPos = pos;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Nullable
    public BlockPos getControllerPos() {
        return controllerPos;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        this.notifyControllerOfBreak();
        super.preRemoveSideEffects(pos, state);
    }

    public void notifyControllerOfBreak() {
        if (this.level != null && this.controllerPos != null && !this.level.isClientSide()) {
            BlockEntity be = this.level.getBlockEntity(this.controllerPos);
            // Modular check: Tell any multiblock controller to invalidate
            if (be instanceof IVaporMultiblockController controller) {
                controller.invalidateMultiblock();
            }
        }
    }

    // Modular getter: Returns the handler only if the controller is fully formed
    private IVaporHandler getControllerHandler() {
        if (this.level != null && this.controllerPos != null) {
            BlockEntity be = this.level.getBlockEntity(this.controllerPos);
            if (be instanceof IVaporMultiblockController controller && controller.isFormed()) {
                if (be instanceof IVaporHandler handler) {
                    return handler;
                }
            }
        }
        return null;
    }

    // --- PROXY FUME HANDLER ---
    @Override
    public int fill(MateriaFumusStack resource, boolean simulate) {
        if (this.mode != PortMode.INPUT) return 0; // Reject if we are an output port
        IVaporHandler handler = getControllerHandler();
        if (handler != null) {
            return handler.fill(resource, simulate);
        }
        return 0;
    }

    @Override
    public MateriaFumusStack drain(int maxDrain, boolean simulate) {
        if (this.mode != PortMode.OUTPUT) return MateriaFumusStack.EMPTY; // Reject if we are an input port
        IVaporHandler handler = getControllerHandler();
        if (handler != null) {
            return handler.drain(maxDrain, simulate);
        }
        return MateriaFumusStack.EMPTY;
    }

    @Override
    public MateriaFumusStack getMateriaInTank() {
        IVaporHandler handler = getControllerHandler();
        if (handler != null) {
            return handler.getMateriaInTank();
        }
        return MateriaFumusStack.EMPTY;
    }

    @Override
    public int getSafeCapacity() {
        IVaporHandler handler = getControllerHandler();
        if (handler != null) {
            return handler.getSafeCapacity();
        }
        return 0;
    }

    @Override
    public int getAbsoluteCapacity() {
        IVaporHandler handler = getControllerHandler();
        if (handler != null) {
            return handler.getAbsoluteCapacity();
        }
        return 0;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.controllerPos != null) {
            output.putLong("ControllerPos", this.controllerPos.asLong());
        }
        output.putInt("PortMode", this.mode.ordinal());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.getLong("ControllerPos").ifPresent(l -> this.controllerPos = BlockPos.of(l));
        this.mode = PortMode.values()[input.getIntOr("PortMode", 0) % PortMode.values().length];
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