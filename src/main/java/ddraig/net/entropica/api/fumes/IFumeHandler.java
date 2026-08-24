package ddraig.net.entropica.api.fumes;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a handler for managing Fumes within a machine or system.
 * Provides methods to interact with, store, and manipulate Fumes.
 */
public interface IFumeHandler {

    /**
     * Returns the safe operating capacity of Fumes the machine can hold.
     * This represents 1.0 Pressure (100% nominal capacity).
     */
    int getSafeCapacity();

    /**
     * Returns the absolute maximum amount of Fumes the machine can hold before structural failure.
     * This represents the maximum over-pressurization limit (e.g., 3.0 Pressure).
     */
    int getAbsoluteCapacity();

    /**
     * Calculates the current pressure of the container.
     * A value of 0.0 to 1.0 is safe operation.
     * Anything > 1.0 means the container is over-pressurized and risks venting or exploding.
     */
    default float getPressure() {
        if (getSafeCapacity() == 0) return 0.0f;
        return (float) getFumeInTank().getAmount() / getSafeCapacity();
    }

    /**
     * Returns the current Fumes stored in the machine.
     * Use this to check the type and amount.
     */
    @NotNull
    VisFumeStack getFumeInTank();

    /**
     * Attempts to push Fumes into the machine.
     * @param resource The Fumes trying to enter.
     * @param simulate If true, only calculates the result without actually modifying the tank.
     * @return The amount of Fumes that were successfully accepted (or would be accepted).
     */
    int fill(VisFumeStack resource, boolean simulate);

    /**
     * Attempts to pull Fumes out of the machine.
     * @param maxDrain The maximum amount of Fumes requested.
     * @param simulate If true, only calculates the result without actually modifying the tank.
     * @return A VisFumeStack containing the extracted Fumes (or EMPTY if none could be extracted).
     */
    @NotNull
    VisFumeStack drain(int maxDrain, boolean simulate);
}