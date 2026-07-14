package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.materia.IVaporHandler;
import ddraig.net.entropica.api.materia.MateriaFumusStack;
import ddraig.net.entropica.api.materia.MateriaStack;
import ddraig.net.entropica.block.VaporPneumaticDiverterBlock;
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

public class VaporPneumaticDiverterBlockEntity extends VaporPneumaticPipeBlockEntity {

    public VaporPneumaticDiverterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VAPOR_PNEUMATIC_DIVERTER_BE.get(), pos, state);
    }

    @Override
    public int getSafeCapacity() { return EntropicaConfig.VAPOR_PNEUMATIC_DIVERTER_CAPACITY.get(); }

    @Override
    protected List<Direction> getActiveConnections(BlockState state) {
        List<Direction> active = new ArrayList<>();
        if (!state.hasProperty(VaporPneumaticDiverterBlock.FACING)) return active;

        Direction inputFace = state.getValue(VaporPneumaticDiverterBlock.FACING);
        Direction playerPerspective = state.getValue(VaporPneumaticDiverterBlock.HORIZONTAL_FACING);
        Direction forwardFace = inputFace.getOpposite();
        Direction rightFace, leftFace;

        if (inputFace.getAxis().isVertical()) {
            leftFace = playerPerspective.getCounterClockWise();
            rightFace = playerPerspective.getClockWise();
        } else {
            leftFace = inputFace.getClockWise();
            rightFace = inputFace.getCounterClockWise();
        }

        active.add(inputFace);
        if (state.getValue(VaporPneumaticDiverterBlock.FORWARD_OPEN)) active.add(forwardFace);
        if (state.getValue(VaporPneumaticDiverterBlock.LEFT_OPEN)) active.add(leftFace);
        if (state.getValue(VaporPneumaticDiverterBlock.RIGHT_OPEN)) active.add(rightFace);

        return active;
    }

    @Override
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;
        syncIfNeeded(level, pos, state);

        if (this.storedMateria.getAmount() > getAbsoluteCapacity()) {
            exhaustBreakBlock((ServerLevel) level, pos);
            return;
        }

        if (storedMateria.isEmpty() || level.getGameTime() % EntropicaConfig.MATERIA_FUMUS_TICK_RATE.get() != 0) return;

        Direction inputFace = state.getValue(VaporPneumaticDiverterBlock.FACING);
        Direction playerPerspective = state.getValue(VaporPneumaticDiverterBlock.HORIZONTAL_FACING);
        Direction forwardFace = inputFace.getOpposite();
        Direction rightFace, leftFace;

        if (inputFace.getAxis().isVertical()) {
            leftFace = playerPerspective.getCounterClockWise();
            rightFace = playerPerspective.getClockWise();
        } else {
            leftFace = inputFace.getClockWise();
            rightFace = inputFace.getCounterClockWise();
        }

        List<Direction> openOutputs = new ArrayList<>();
        if (state.getValue(VaporPneumaticDiverterBlock.FORWARD_OPEN)) openOutputs.add(forwardFace);
        if (state.getValue(VaporPneumaticDiverterBlock.LEFT_OPEN)) openOutputs.add(leftFace);
        if (state.getValue(VaporPneumaticDiverterBlock.RIGHT_OPEN)) openOutputs.add(rightFace);

        if (openOutputs.isEmpty()) return;

        boolean changed = false;
        int myAmount = this.storedMateria.getAmount();
        EssenceType type = this.storedMateria.getType();
        boolean beingPurged = isBeingOverpowered();

        Direction priorityDir = null;
        if (beingPurged && openOutputs.size() > 1) {
            priorityDir = findNearestExit(level, pos, 32).orElse(null);
        }

        for (Direction outDir : openOutputs) {
            if (priorityDir != null && outDir != priorityDir) continue;

            BlockPos targetPos = pos.relative(outDir);
            BlockEntity targetBE = level.getBlockEntity(targetPos);

            if (targetBE instanceof IVaporHandler neighbor) {
                MateriaStack neighborFumes = neighbor.getMateriaInTank();
                if (neighborFumes.isEmpty() || neighborFumes.getType() == type) {
                    int diff = myAmount - neighborFumes.getAmount();
                    if (diff >= 1) {
                        int toTransfer;
                        if (beingPurged) {
                            toTransfer = Math.min(myAmount, EntropicaConfig.MATERIA_FUMUS_TRANSFER_RATE.get());
                        } else {
                            toTransfer = Math.min(Math.max(1, diff / 2), EntropicaConfig.MATERIA_FUMUS_TRANSFER_RATE.get());
                        }

                        if (toTransfer > 0) {
                            MateriaStack pushStack = this.storedMateria.copy();
                            pushStack.setAmount(toTransfer);
                            int accepted = neighbor.fill(pushStack, false);
                            if (accepted > 0) {
                                this.storedMateria.shrink(accepted);
                                myAmount -= accepted;
                                changed = true;
                            }
                        }
                    }
                }
            } else if (level.getBlockState(targetPos).isAir()) {
                int threshold = beingPurged ? 0 : 20;
                if (myAmount > threshold) {
                    int ventAmount = Math.min(myAmount - threshold, EntropicaConfig.MATERIA_FUMUS_TRANSFER_RATE.get());
                    if (ventAmount > 0) {
                        ventGasIntoAir((ServerLevel) level, pos, outDir, this.storedMateria, ventAmount);
                        this.storedMateria.shrink(ventAmount);
                        myAmount -= ventAmount;
                        changed = true;
                    }
                }
            }
            if (myAmount <= 0) break;
        }

        if (changed) {
            this.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }
}