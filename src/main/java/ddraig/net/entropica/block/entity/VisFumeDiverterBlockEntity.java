package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.fumes.IFumeHandler;
import ddraig.net.entropica.api.fumes.VisFumeStack;
import ddraig.net.entropica.block.VisFumeDiverterBlock;
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

public class VisFumeDiverterBlockEntity extends VisFumePipeBlockEntity {

    public VisFumeDiverterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VIS_FUME_DIVERTER_BE.get(), pos, state);
    }

    @Override
    public int getCapacity() { return EntropicaConfig.VIS_FUME_DIVERTER_CAPACITY.get(); }

    @Override
    protected List<Direction> getActiveConnections(BlockState state) {
        List<Direction> active = new ArrayList<>();
        if (!state.hasProperty(VisFumeDiverterBlock.FACING)) return active;

        Direction inputFace = state.getValue(VisFumeDiverterBlock.FACING);
        Direction playerPerspective = state.getValue(VisFumeDiverterBlock.HORIZONTAL_FACING);
        Direction forwardFace = inputFace.getOpposite();
        Direction rightFace, leftFace;

        if (inputFace.getAxis().isVertical()) {
            leftFace = playerPerspective.getCounterClockWise();
            rightFace = playerPerspective.getClockWise();
        } else {
            leftFace = inputFace.getClockWise();
            rightFace = inputFace.getCounterClockWise();
        }

        active.add(inputFace); // Ensures inherited logic sees the input
        if (state.getValue(VisFumeDiverterBlock.FORWARD_OPEN)) active.add(forwardFace);
        if (state.getValue(VisFumeDiverterBlock.LEFT_OPEN)) active.add(leftFace);
        if (state.getValue(VisFumeDiverterBlock.RIGHT_OPEN)) active.add(rightFace);

        return active;
    }

    @Override
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;
        syncIfNeeded(level, pos, state);

        if (this.storedFumes.getAmount() > 200) {
            exhaustBreakBlock((ServerLevel) level, pos);
            return;
        }

        if (storedFumes.isEmpty() || level.getGameTime() % EntropicaConfig.VIS_FUME_TICK_RATE.get() != 0) return;

        Direction inputFace = state.getValue(VisFumeDiverterBlock.FACING);
        Direction playerPerspective = state.getValue(VisFumeDiverterBlock.HORIZONTAL_FACING);
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
        if (state.getValue(VisFumeDiverterBlock.FORWARD_OPEN)) openOutputs.add(forwardFace);
        if (state.getValue(VisFumeDiverterBlock.LEFT_OPEN)) openOutputs.add(leftFace);
        if (state.getValue(VisFumeDiverterBlock.RIGHT_OPEN)) openOutputs.add(rightFace);

        if (openOutputs.isEmpty()) return;

        boolean changed = false;
        int myAmount = this.storedFumes.getAmount();
        EssenceType type = this.storedFumes.getType();
        boolean beingPurged = isBeingOverpowered();

        Direction priorityDir = null;
        if (beingPurged && openOutputs.size() > 1) {
            priorityDir = findNearestExit(level, pos, 32).orElse(null);
        }

        for (Direction outDir : openOutputs) {
            if (priorityDir != null && outDir != priorityDir) continue;

            BlockPos targetPos = pos.relative(outDir);
            BlockEntity targetBE = level.getBlockEntity(targetPos);

            if (targetBE instanceof IFumeHandler neighbor) {
                VisFumeStack neighborFumes = neighbor.getFumeInTank();
                if (neighborFumes.isEmpty() || neighborFumes.getType() == type) {
                    int diff = myAmount - neighborFumes.getAmount();
                    if (diff >= 1) {
                        int toTransfer;
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
            } else if (level.getBlockState(targetPos).isAir()) {
                // EXCLUSIVELY vent out of configured outputs, not random holes.
                int threshold = beingPurged ? 0 : 20;
                if (myAmount > threshold) {
                    int ventAmount = Math.min(myAmount - threshold, EntropicaConfig.VIS_FUME_TRANSFER_RATE.get());
                    if (ventAmount > 0) {
                        ventGasIntoAir((ServerLevel) level, pos, outDir, this.storedFumes, ventAmount);
                        this.storedFumes.shrink(ventAmount);
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