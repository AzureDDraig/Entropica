package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.fumes.IFumeHandler;
import ddraig.net.entropica.api.fumes.VisFumeStack;
import ddraig.net.entropica.block.VisFumePipeBlock;
import ddraig.net.entropica.block.VisFumeValveBlock;
import ddraig.net.entropica.config.EntropicaConfig;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class VisFumeValveBlockEntity extends VisFumePipeBlockEntity {

    public VisFumeValveBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VIS_FUME_VALVE_BE.get(), pos, state);
    }

    @Override
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;
        syncIfNeeded(level, pos, state);

        // Always check overpressure, even if the valve is closed
        if (this.storedFumes.getAmount() > 200) {
            exhaustBreakBlock((ServerLevel) level, pos);
            return;
        }

        if (storedFumes.isEmpty() || level.getGameTime() % EntropicaConfig.VIS_FUME_TICK_RATE.get() != 0) return;

        // The closed Valve prevents all forward momentum and load balancing
        if (!state.getValue(VisFumeValveBlock.OPEN)) return;

        Direction.Axis activeAxis = state.getValue(VisFumeValveBlock.AXIS);
        List<Direction> openEnds = new ArrayList<>();

        for (Direction dir : Direction.values()) {
            if (dir.getAxis() == activeAxis &&
                    state.hasProperty(VisFumePipeBlock.getDirectionProperty(dir)) &&
                    state.getValue(VisFumePipeBlock.getDirectionProperty(dir))) {
                openEnds.add(dir);
            }
        }

        if (openEnds.isEmpty()) return;

        boolean changed = false;
        int myAmount = this.storedFumes.getAmount();
        EssenceType type = this.storedFumes.getType();
        boolean beingPurged = isBeingOverpowered(); // Logic from pipe

        // 1. Load Balance strictly along the Valve's axis
        List<IFumeHandler> validNeighbors = new ArrayList<>();
        for (Direction dir : openEnds) {
            BlockEntity be = level.getBlockEntity(pos.relative(dir));
            if (be instanceof IFumeHandler handler) validNeighbors.add(handler);
        }

        for (IFumeHandler neighbor : validNeighbors) {
            VisFumeStack neighborFumes = neighbor.getFumeInTank();
            if (neighborFumes.isEmpty() || neighborFumes.getType() == type) {
                int diff = myAmount - neighborFumes.getAmount();
                if (diff >= 1) { // Changed to match pipe logic
                    int toTransfer;
                    // PURGE LOGIC: Push at max rate if overpowered
                    if (beingPurged) {
                        toTransfer = Math.min(myAmount, EntropicaConfig.VIS_FUME_TRANSFER_RATE.get());
                    } else {
                        toTransfer = Math.min(Math.max(1, diff / 2), EntropicaConfig.VIS_FUME_TRANSFER_RATE.get());
                    }

                    if (toTransfer > 0) {
                        int accepted = neighbor.fill(new VisFumeStack(type, toTransfer), false);
                        if (accepted > 0) {
                            this.storedFumes.shrink(accepted);
                            myAmount -= accepted;
                            changed = true;
                        }
                    }
                }
            }
        }

        // 2. Dead-End Venting (Valve is placed at the end of a pipe pointing at air)
        if (openEnds.size() == 1 && myAmount > 0) {
            Direction sourceDir = openEnds.get(0);
            Direction ventDir = sourceDir.getOpposite(); // Vent out the unconnected side of the axis

            if (level.getBlockState(pos.relative(ventDir)).isAir()) {
                // PURGE LOGIC: Drop threshold to 0 during a purge to clear the line
                int threshold = beingPurged ? 0 : 20;
                if (myAmount > threshold) {
                    int ventAmount = Math.min(myAmount - threshold, EntropicaConfig.VIS_FUME_TRANSFER_RATE.get());
                    if (ventAmount > 0) {
                        ventGasIntoAir((ServerLevel) level, pos, ventDir, this.storedFumes, ventAmount);
                        this.storedFumes.shrink(ventAmount);
                        changed = true;
                    }
                }
            }
        }

        if (changed) {
            this.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }
}