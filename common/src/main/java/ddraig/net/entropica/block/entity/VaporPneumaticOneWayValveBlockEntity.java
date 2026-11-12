package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.materia.IVaporHandler;
import ddraig.net.entropica.api.materia.MateriaFumusStack;
import ddraig.net.entropica.api.materia.MateriaStack;
import ddraig.net.entropica.block.VaporPneumaticOneWayValveBlock;
import ddraig.net.entropica.config.EntropicaConfig;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Represents a block entity for the Materia Fume One-Way Valve,
 * which is a specialized pipe allowing unidirectional transfer or venting of Materia Fumes.
 * It extends the functionality of the VaporPneumaticPipeBlockEntity.
 *
 * This block entity operates by handling the flow of Materia Fume gases using a one-way mechanism
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
public class VaporPneumaticOneWayValveBlockEntity extends VaporPneumaticPipeBlockEntity {

    public VaporPneumaticOneWayValveBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VAPOR_PNEUMATIC_ONE_WAY_VALVE_BE.get(), pos, state); // Make sure to register this!
    }

    @Override
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;
        syncIfNeeded(level, pos, state);

        if (this.storedMateria.getAmount() > 200) {
            exhaustBreakBlock((ServerLevel) level, pos);
            return;
        }

        if (storedMateria.isEmpty() || level.getGameTime() % EntropicaConfig.MATERIA_FUMUS_TICK_RATE.get() != 0) return;

        // If the valve is closed, absolutely no flow occurs.
        if (!state.getValue(VaporPneumaticOneWayValveBlock.OPEN)) return;

        Direction flowDir = state.getValue(VaporPneumaticOneWayValveBlock.FACING);

        boolean changed = false;
        int myAmount = this.storedMateria.getAmount();
        EssenceType type = this.storedMateria.getType();
        boolean beingPurged = isBeingOverpowered();

        // ONLY attempt to push gas to the FACING direction
        BlockEntity targetBE = level.getBlockEntity(pos.relative(flowDir));
        if (targetBE instanceof IVaporHandler neighbor) {
            MateriaStack neighborFumes = neighbor.getMateriaInTank();
            if (neighborFumes.isEmpty() || neighborFumes.getType() == type) {
                int diff = myAmount - neighborFumes.getAmount();
                if (diff >= 1) {
                    int toTransfer = beingPurged ?
                            Math.min(myAmount, EntropicaConfig.MATERIA_FUMUS_TRANSFER_RATE.get()) :
                            Math.min(Math.max(1, diff / 2), EntropicaConfig.MATERIA_FUMUS_TRANSFER_RATE.get());

                    MateriaStack pushStack = this.storedMateria.copy();
                    pushStack.setAmount(toTransfer);
                    int accepted = neighbor.fill(pushStack, false);
                    if (accepted > 0) {
                        this.storedMateria.shrink(accepted);
                        changed = true;
                    }
                }
            }
        } else if (level.getBlockState(pos.relative(flowDir)).isAir()) {
            // Vent out the front if it's open to air
            int ventAmount = Math.min(myAmount, EntropicaConfig.MATERIA_FUMUS_TRANSFER_RATE.get());
            ventGasIntoAir((ServerLevel) level, pos, flowDir, this.storedMateria, ventAmount);
            this.storedMateria.shrink(ventAmount);
            changed = true;
        }

        if (changed) {
            this.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }
}