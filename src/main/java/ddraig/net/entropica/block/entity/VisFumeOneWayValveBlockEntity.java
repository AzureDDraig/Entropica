package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.fumes.IFumeHandler;
import ddraig.net.entropica.api.fumes.VisFumeStack;
import ddraig.net.entropica.block.VisFumeOneWayValveBlock;
import ddraig.net.entropica.config.EntropicaConfig;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Represents a block entity for the Vis Fume One-Way Valve,
 * which is a specialized pipe allowing unidirectional transfer or venting of Vis Fumes.
 * It extends the functionality of the VisFumePipeBlockEntity.
 *
 * This block entity operates by handling the flow of Vis Fume gases using a one-way mechanism
 * defined by its facing direction and its valve state (open or closed).
 * It can either transfer fumes to a valid connected block entity in its facing direction or vent fumes into the air.
 *
 * The operation of the valve is influenced by multiple factors including:
 * - The current amount of stored fumes.
 * - Whether the valve is open.
 * - The configuration-defined transfer rate.
 * - The entity's potential state of being overpowered (purging mode).
 *
 * Features include:
 * - Automated transfer of fumes to neighboring blocks if they are compatible.
 * - Venting fumes into the air if the block in the facing direction is open to the atmosphere.
 * - Synchronization of changes in stored fumes between the server and client.
 * - Automatic handling of overpressure conditions where the block may break if fumes exceed allowed storage.
 */
public class VisFumeOneWayValveBlockEntity extends VisFumePipeBlockEntity {

    public VisFumeOneWayValveBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VIS_FUME_ONE_WAY_VALVE_BE.get(), pos, state); // Make sure to register this!
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

        // If the valve is closed, absolutely no flow occurs.
        if (!state.getValue(VisFumeOneWayValveBlock.OPEN)) return;

        Direction flowDir = state.getValue(VisFumeOneWayValveBlock.FACING);

        boolean changed = false;
        int myAmount = this.storedFumes.getAmount();
        EssenceType type = this.storedFumes.getType();
        boolean beingPurged = isBeingOverpowered();

        // ONLY attempt to push gas to the FACING direction
        BlockEntity targetBE = level.getBlockEntity(pos.relative(flowDir));
        if (targetBE instanceof IFumeHandler neighbor) {
            VisFumeStack neighborFumes = neighbor.getFumeInTank();
            if (neighborFumes.isEmpty() || neighborFumes.getType() == type) {
                int diff = myAmount - neighborFumes.getAmount();
                if (diff >= 1) {
                    int toTransfer = beingPurged ?
                            Math.min(myAmount, EntropicaConfig.VIS_FUME_TRANSFER_RATE.get()) :
                            Math.min(Math.max(1, diff / 2), EntropicaConfig.VIS_FUME_TRANSFER_RATE.get());

                    int accepted = neighbor.fill(new VisFumeStack(type, toTransfer), false);
                    if (accepted > 0) {
                        this.storedFumes.shrink(accepted);
                        changed = true;
                    }
                }
            }
        } else if (level.getBlockState(pos.relative(flowDir)).isAir()) {
            // Vent out the front if it's open to air
            int ventAmount = Math.min(myAmount, EntropicaConfig.VIS_FUME_TRANSFER_RATE.get());
            ventGasIntoAir((ServerLevel) level, pos, flowDir, this.storedFumes, ventAmount);
            this.storedFumes.shrink(ventAmount);
            changed = true;
        }

        if (changed) {
            this.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }
}