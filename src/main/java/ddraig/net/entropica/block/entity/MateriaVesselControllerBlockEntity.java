package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.materia.IVaporHandler;
import ddraig.net.entropica.api.materia.IVaporMultiblockController;
import ddraig.net.entropica.api.materia.MateriaFumusStack;
import ddraig.net.entropica.block.ManaEnrichedGlassBlock;
import ddraig.net.entropica.block.VaporPneumaticOneWayValveBlock;
import ddraig.net.entropica.block.MateriaVesselControllerBlock;
import ddraig.net.entropica.block.MateriaVesselPortBlock;
import ddraig.net.entropica.config.EntropicaConfig;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MateriaVesselControllerBlockEntity extends BlockEntity implements IVaporHandler, IVaporMultiblockController {
    private boolean isFormed = false;
    private int maxCapacity = 0;
    private boolean isExportMode = false;
    private final Set<BlockPos> connectedBlocks = new HashSet<>();

    private int tickCounter = 0;
    private int recheckDelay = -1;

    private MateriaFumusStack storedFume = MateriaFumusStack.EMPTY;

    private static final int MAX_STRUCTURE_SIZE = 256;

    public MateriaVesselControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MATERIA_VESSEL_CONTROLLER_BE.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;

        if (this.recheckDelay > 0) {
            this.recheckDelay--;
            if (this.recheckDelay == 0) {
                boolean wasFormed = this.isFormed;
                boolean success = this.attemptFormMultiblock();
                if (wasFormed != success) {
                    level.sendBlockUpdated(pos, state, state, 3);
                }
            }
        }

        tickCounter++;
        if (tickCounter % 100 == 0 && this.isFormed) {
            this.attemptFormMultiblock();
        }

        if (!this.isFormed || !this.isExportMode || this.storedFume.isEmpty()) return;

        if (level.getGameTime() % EntropicaConfig.MATERIA_FUMUS_TICK_RATE.get() != 0) return;

        boolean changed = false;
        int amountToPush = Math.min(this.storedFume.getAmount(), EntropicaConfig.MATERIA_FUMUS_TRANSFER_RATE.get());

        if (amountToPush <= 0) return;

        for (BlockPos connectedPos : this.connectedBlocks) {
            if (amountToPush <= 0) break;

            BlockEntity be = level.getBlockEntity(connectedPos);

            if (be instanceof MateriaVesselPortBlockEntity port && port.getMode() == MateriaVesselPortBlockEntity.PortMode.OUTPUT) {
                for (Direction dir : Direction.values()) {
                    if (amountToPush <= 0) break;

                    BlockPos neighborPos = connectedPos.relative(dir);

                    if (this.connectedBlocks.contains(neighborPos) || neighborPos.equals(this.worldPosition)) continue;

                    BlockEntity neighborBE = level.getBlockEntity(neighborPos);

                    if (neighborBE instanceof VaporPneumaticOneWayValveBlockEntity oneWay) {
                        if (oneWay.getBlockState().hasProperty(VaporPneumaticOneWayValveBlock.FACING) &&
                                oneWay.getBlockState().getValue(VaporPneumaticOneWayValveBlock.FACING) == dir.getOpposite()) {
                            continue;
                        }
                    }

                    if (neighborBE instanceof IVaporHandler handler) {
                        int accepted = handler.fill(new MateriaFumusStack(this.storedFume.getType(), amountToPush), false);
                        if (accepted > 0) {
                            this.storedFume.shrink(accepted);
                            amountToPush -= accepted;
                            changed = true;
                        }
                    }
                }
            }
        }

        if (changed) {
            if (this.storedFume.getAmount() <= 0) {
                this.storedFume = MateriaFumusStack.EMPTY;
            }
            this.setChanged();
            level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public void toggleMode() {
        this.isExportMode = !this.isExportMode;
        if (this.level != null) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            if (state.hasProperty(MateriaVesselControllerBlock.IS_EXPORT)) {
                this.level.setBlock(this.worldPosition, state.setValue(MateriaVesselControllerBlock.IS_EXPORT, this.isExportMode), 3);
            }
        }
        this.setChanged();
    }

    public boolean isExportMode() {
        return this.isExportMode;
    }

    public boolean attemptFormMultiblock() {
        if (this.level == null || this.level.isClientSide()) return false;

        invalidate(false);

        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        List<Block> foundGlass = new ArrayList<>();
        List<BlockPos> foundPorts = new ArrayList<>();

        queue.add(this.worldPosition);
        visited.add(this.worldPosition);

        int controllerCount = 1;

        while (!queue.isEmpty()) {
            if (visited.size() > MAX_STRUCTURE_SIZE) {
                return false;
            }

            BlockPos currentPos = queue.poll();

            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = currentPos.relative(dir);
                if (visited.contains(neighborPos)) continue;

                BlockState neighborState = this.level.getBlockState(neighborPos);
                Block neighborBlock = neighborState.getBlock();

                if (neighborBlock instanceof MateriaVesselControllerBlock) {
                    controllerCount++;
                    if (controllerCount > 1) return false;
                    visited.add(neighborPos);
                    continue;
                }

                if (neighborBlock instanceof ManaEnrichedGlassBlock || neighborBlock instanceof MateriaVesselPortBlock) {
                    BlockEntity be = this.level.getBlockEntity(neighborPos);
                    BlockPos claimedPos = null;
                    if (be instanceof ManaEnrichedGlassBlockEntity glass) claimedPos = glass.getControllerPos();
                    else if (be instanceof MateriaVesselPortBlockEntity port) claimedPos = port.getControllerPos();

                    if (claimedPos != null && !claimedPos.equals(this.worldPosition)) {
                        BlockEntity otherBE = this.level.getBlockEntity(claimedPos);
                        if (otherBE instanceof MateriaVesselControllerBlockEntity otherCont && otherCont.isFormed()) {
                            return false;
                        }
                    }

                    visited.add(neighborPos);
                    queue.add(neighborPos);

                    if (neighborBlock instanceof ManaEnrichedGlassBlock) {
                        foundGlass.add(neighborBlock);
                    } else {
                        foundPorts.add(neighborPos);
                    }
                }
            }
        }

        if (foundPorts.size() < 2 || foundGlass.size() < 5) {
            updateBlockState(false);
            return false;
        }

        this.isFormed = true;
        this.connectedBlocks.addAll(visited);
        this.connectedBlocks.remove(this.worldPosition);

        this.maxCapacity = calculateDiminishingCapacity(foundGlass);

        if (!this.storedFume.isEmpty() && this.storedFume.getAmount() > this.maxCapacity) {
            this.storedFume.setAmount(this.maxCapacity);
        }
        if (this.storedFume.getAmount() <= 0) {
            this.storedFume = MateriaFumusStack.EMPTY;
        }

        for (BlockPos pos : this.connectedBlocks) {
            BlockEntity be = this.level.getBlockEntity(pos);
            if (be instanceof ManaEnrichedGlassBlockEntity glass) {
                glass.setControllerPos(this.worldPosition);
                glass.setChanged();
                this.level.sendBlockUpdated(pos, glass.getBlockState(), glass.getBlockState(), 3);
            } else if (be instanceof MateriaVesselPortBlockEntity port) {
                port.setControllerPos(this.worldPosition);
                port.setChanged();
                this.level.sendBlockUpdated(pos, port.getBlockState(), port.getBlockState(), 3);
            }
        }

        updateBlockState(this.isExportMode);
        this.setChanged();
        return true;
    }

    public void invalidateMultiblock() {
        invalidate(true);
    }

    private void invalidate(boolean scheduleRecheck) {
        if (!this.isFormed) return;
        this.isFormed = false;
        this.maxCapacity = 0;

        clearOldConnections();
        this.setChanged();

        if (scheduleRecheck) {
            this.recheckDelay = 5;
        }
    }

    private void clearOldConnections() {
        if (this.level != null) {
            for (BlockPos pos : this.connectedBlocks) {
                BlockEntity be = this.level.getBlockEntity(pos);
                if (be instanceof ManaEnrichedGlassBlockEntity glass) {
                    glass.setControllerPos(null);
                    glass.setChanged();
                    this.level.sendBlockUpdated(pos, glass.getBlockState(), glass.getBlockState(), 3);
                } else if (be instanceof MateriaVesselPortBlockEntity port) {
                    port.setControllerPos(null);
                    port.setChanged();
                    this.level.sendBlockUpdated(pos, port.getBlockState(), port.getBlockState(), 3);
                }
            }
        }
        this.connectedBlocks.clear();
    }

    private void updateBlockState(boolean export) {
        if (this.level != null) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            if (state.hasProperty(MateriaVesselControllerBlock.IS_EXPORT)) {
                this.level.setBlock(this.worldPosition, state.setValue(MateriaVesselControllerBlock.IS_EXPORT, export), 3);
            }
        }
    }

    private int calculateDiminishingCapacity(List<Block> glassBlocks) {
        glassBlocks.sort((b1, b2) -> Integer.compare(getGlassBaseCapacity(b2), getGlassBaseCapacity(b1)));

        double totalCapacity = 0;
        double decayFactor = EntropicaConfig.VESSEL_CAPACITY_DECAY_MULTIPLIER.get();
        int decayStartOffset = EntropicaConfig.VESSEL_DECAY_STARTING_BLOCK.get();

        for (int i = 0; i < glassBlocks.size(); i++) {
            int baseValue = getGlassBaseCapacity(glassBlocks.get(i));
            int decayExponents = Math.max(0, i - decayStartOffset);
            totalCapacity += baseValue * Math.pow(decayFactor, decayExponents);
        }

        return (int) totalCapacity;
    }

    private int getGlassBaseCapacity(Block block) {
        if (block == ModBlocks.FRAGMENT_LATTICE_GLASS.get()) return EntropicaConfig.VESSEL_GLASS_LATTICE_CAPACITY.get();
        if (block == ModBlocks.MATERIA_LIQUIDA_ENRICHED_GLASS.get()) return EntropicaConfig.VESSEL_GLASS_ICHOR_CAPACITY.get();
        if (block == ModBlocks.MATERIA_FUMUS_STRENGTHENED_GLASS.get()) return EntropicaConfig.VESSEL_GLASS_STRENGTHENED_CAPACITY.get();
        return EntropicaConfig.VESSEL_GLASS_BASE_CAPACITY.get();
    }

    @Override public boolean isFormed() { return isFormed; }

    // --- PRESSURE LIMITS (Hard Capped) ---
    @Override public int getSafeCapacity() { return this.maxCapacity; }
    @Override public int getAbsoluteCapacity() { return this.maxCapacity; }
    public int getMaxCapacity() { return this.maxCapacity; } // Retained for Block interaction

    @Override public MateriaFumusStack getStoredMateria() { return storedFume; }

    @Override
    public int fill(MateriaFumusStack resource, boolean simulate) {
        if (!isFormed || resource.isEmpty() || this.isExportMode) return 0;

        if (!this.storedFume.isEmpty() && this.storedFume.getType() != resource.getType()) {
            return 0;
        }

        int availableSpace = this.maxCapacity - this.storedFume.getAmount();
        if (availableSpace <= 0) return 0;

        int amountToFill = Math.min(resource.getAmount(), availableSpace);

        if (!simulate) {
            if (this.storedFume.isEmpty()) {
                this.storedFume = new MateriaFumusStack(resource.getType(), amountToFill);
            } else {
                this.storedFume.grow(amountToFill);
            }
            this.setChanged();
            if (this.level != null && !this.level.isClientSide()) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
        return amountToFill;
    }

    @Override
    public MateriaFumusStack drain(int maxDrain, boolean simulate) {
        if (!isFormed || this.storedFume.isEmpty() || maxDrain <= 0 || !this.isExportMode) return MateriaFumusStack.EMPTY;

        int amountToDrain = Math.min(maxDrain, this.storedFume.getAmount());
        MateriaFumusStack drainedStack = new MateriaFumusStack(this.storedFume.getType(), amountToDrain);

        if (!simulate) {
            this.storedFume.shrink(amountToDrain);
            if (this.storedFume.getAmount() <= 0) {
                this.storedFume = MateriaFumusStack.EMPTY;
            }
            this.setChanged();
            if (this.level != null && !this.level.isClientSide()) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
        return drainedStack;
    }

    @Override
    public MateriaFumusStack getMateriaInTank() {
        return this.storedFume != null ? this.storedFume : MateriaFumusStack.EMPTY;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("IsFormed", this.isFormed);
        output.putBoolean("IsExportMode", this.isExportMode);
        output.putInt("MaxCapacity", this.maxCapacity);

        if (!this.storedFume.isEmpty()) {
            output.putString("FumeType", this.storedFume.getType().name());
            output.putInt("FumeAmount", this.storedFume.getAmount());
        }

        output.putInt("ConnectedBlockCount", this.connectedBlocks.size());
        int i = 0;
        for (BlockPos pos : this.connectedBlocks) {
            output.putLong("ConnectedBlock_" + i, pos.asLong());
            i++;
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.isFormed = input.getBooleanOr("IsFormed", false);
        this.isExportMode = input.getBooleanOr("IsExportMode", false);
        this.maxCapacity = input.getIntOr("MaxCapacity", 0);

        String fumeTypeStr = input.getStringOr("FumeType", "");
        int fumeAmt = input.getIntOr("FumeAmount", 0);
        if (!fumeTypeStr.isEmpty() && fumeAmt > 0) {
            try {
                EssenceType type = EssenceType.valueOf(fumeTypeStr);
                this.storedFume = new MateriaFumusStack(type, fumeAmt);
            } catch (IllegalArgumentException e) {
                this.storedFume = MateriaFumusStack.EMPTY;
            }
        } else {
            this.storedFume = MateriaFumusStack.EMPTY;
        }

        this.connectedBlocks.clear();
        int count = input.getIntOr("ConnectedBlockCount", 0);
        for (int i = 0; i < count; i++) {
            input.getLong("ConnectedBlock_" + i).ifPresent(l -> this.connectedBlocks.add(BlockPos.of(l)));
        }
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