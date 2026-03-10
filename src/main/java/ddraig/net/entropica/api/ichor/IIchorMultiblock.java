package ddraig.net.entropica.api.ichor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import ddraig.net.entropica.api.EssenceType;

/**
 * Extended handler for large-scale Ichor structures.
 * Allows ports and structural components to safely interface with a central master controller,
 * while strictly locking out unauthorized inputs or improper physical connections.
 */
public interface IIchorMultiblock extends IIchorHandler {

    /**
     * Checks if the multiblock structure is fully and correctly formed.
     * @return true if the multiblock is active and formed, false otherwise.
     */
    boolean isFormed();

    /**
     * Gets the BlockPos of the master/controller block for this multiblock.
     * @return The BlockPos of the master block, or null if the structure is invalid.
     */
    BlockPos getMasterPos();

    /**
     * Checks if the current block entity is the master/controller of the multiblock network.
     * @return true if this is the master, false if it is a subordinate/port block.
     */
    boolean isMaster();

    /**
     * STRICT LOCK-OUT: Determines if a specific face of this block is allowed to connect to the Ichor network.
     * Prevents Ichor from flowing into the wrong sides of machines.
     * @param side The direction from which the connection is being attempted.
     * @return true if the side can accept/extract Ichor, false to lock it out.
     */
    boolean canConnectIchor(Direction side);

    /**
     * STRICT LOCK-OUT: Determines if the specific EssenceType of Ichor is allowed in this multiblock.
     * Prevents incompatible or highly volatile Ichor from entering machines not rated for it.
     * @param type The type of Vis Ichor attempting to enter.
     * @return true if the multiblock can process this specific Ichor, false to lock it out.
     */
    boolean isIchorValid(EssenceType type);
}