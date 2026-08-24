package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.fumes.IFumeHandler;
import ddraig.net.entropica.api.fumes.VisFumeStack;
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

public class VisFumeInputPortBlockEntity extends BlockEntity implements IFumeHandler {

    @Nullable
    private BlockPos controllerPos = null;

    public VisFumeInputPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VIS_FUME_INPUT_PORT_BE.get(), pos, state);
    }

    /**
     * Dynamically locates the Eidolic Lathe controller.
     * It checks a generous 7x13x7 bounding box, allowing the port to be placed
     * virtually anywhere within or slightly above/below the structure.
     */
    @Nullable
    public BlockPos getControllerPos() {
        if (this.controllerPos != null && this.level != null) {
            BlockEntity be = this.level.getBlockEntity(this.controllerPos);
            if (be instanceof EidolicLatheBlockEntity lathe && lathe.isFormed()) {
                return this.controllerPos;
            }
        }

        // Auto-detect if missing or broken
        if (this.level != null) {
            for (int x = -3; x <= 3; x++) {
                for (int y = -6; y <= 6; y++) {
                    for (int z = -3; z <= 3; z++) {
                        BlockPos checkPos = this.worldPosition.offset(x, y, z);
                        BlockEntity be = this.level.getBlockEntity(checkPos);
                        if (be instanceof EidolicLatheBlockEntity lathe && lathe.isFormed()) {
                            this.controllerPos = checkPos;
                            this.setChanged();
                            return this.controllerPos;
                        }
                    }
                }
            }
        }
        this.controllerPos = null;
        return null;
    }

    private IFumeHandler getControllerHandler() {
        BlockPos pos = getControllerPos();
        if (pos != null && this.level != null) {
            BlockEntity be = this.level.getBlockEntity(pos);
            if (be instanceof EidolicLatheBlockEntity lathe && lathe.isFormed()) {
                return lathe;
            }
        }
        return null;
    }

    // --- PROXY FUME HANDLER ---

    @Override
    public int fill(VisFumeStack resource, boolean simulate) {
        IFumeHandler handler = getControllerHandler();
        if (handler != null) {
            return handler.fill(resource, simulate);
        }
        return 0;
    }

    @Override
    public VisFumeStack drain(int maxDrain, boolean simulate) {
        IFumeHandler handler = getControllerHandler();
        if (handler != null) {
            return handler.drain(maxDrain, simulate);
        }
        return VisFumeStack.EMPTY;
    }

    @Override
    public VisFumeStack getFumeInTank() {
        IFumeHandler handler = getControllerHandler();
        if (handler != null) {
            return handler.getFumeInTank();
        }
        return VisFumeStack.EMPTY;
    }

    @Override
    public int getSafeCapacity() {
        IFumeHandler handler = getControllerHandler();
        if (handler != null) {
            return handler.getSafeCapacity();
        }
        return 0;
    }

    @Override
    public int getAbsoluteCapacity() {
        IFumeHandler handler = getControllerHandler();
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
        } else {
            output.putLong("ControllerPos", -1L);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        long pos = input.getLongOr("ControllerPos", -1L);
        if (pos != -1L) {
            this.controllerPos = BlockPos.of(pos);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p) { return this.saveWithoutMetadata(p); }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}