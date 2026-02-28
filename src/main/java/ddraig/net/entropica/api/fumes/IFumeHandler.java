package ddraig.net.entropica.api.fumes;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a handler for managing Fumes within a machine or system.
 * Provides methods to interact with, store, and manipulate Fumes.
 */
public interface IFumeHandler {

    /**
     * Returns the total amount of Fumes the machine can hold.
     */
    int getCapacity();

    /**
     * Returns the current Fumes stored in the machine.
     * Use this to check the type and amount.
     */
    @NotNull
    VisFumeStack getFumeInTank();

    /**
     * Attempts to push Fumes into the machine.
     * * @param resource The Fumes trying to enter.
     * @param simulate If true, only calculates the result without actually modifying the tank.
     * @return The amount of Fumes that were successfully accepted (or would be accepted).
     */
    int fill(VisFumeStack resource, boolean simulate);

    /**
     * Attempts to pull Fumes out of the machine.
     * * @param maxDrain The maximum amount of Fumes requested.
     * @param simulate If true, only calculates the result without actually modifying the tank.
     * @return A VisFumeStack containing the extracted Fumes (or EMPTY if none could be extracted).
     */
    @NotNull
    VisFumeStack drain(int maxDrain, boolean simulate);
}