package ddraig.net.entropica.api.materia;

import ddraig.net.entropica.api.pressure.IPressureHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a handler for managing Materia Fumus (Vapor) within a machine or system.
 * Extends IPressureHandler to participate in centralized pressure network mechanics.
 * The MateriaFumusStack-based counterpart to IVaporHandler, used by the Vapor Pneumatic Pipe network.
 * Provides methods to interact with, store, and manipulate gaseous Materia.
 */
public interface IVaporHandler extends IPressureHandler {

    /**
     * Returns the safe operating capacity of Materia Fumus the machine can hold.
     * This represents 1.0 Pressure (100% nominal capacity).
     */
    int getSafeCapacity();

    /**
     * Returns the absolute maximum amount of Materia Fumus the machine can hold before structural failure.
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
        return (float) getMateriaInTank().getAmount() / getSafeCapacity();
    }

    /**
     * Returns the current Materia Fumus stored in the machine.
     * Use this to check the type and amount.
     */
    @NotNull
    MateriaStack getMateriaInTank();

    /**
     * Attempts to push Materia Fumus into the machine.
     * @param resource The Materia Fumus trying to enter.
     * @param simulate If true, only calculates the result without actually modifying the tank.
     * @return The amount of Materia Fumus that was successfully accepted (or would be accepted).
     */
    int fill(MateriaStack resource, boolean simulate);

    /**
     * Attempts to pull Materia Fumus out of the machine.
     * @param maxDrain The maximum amount of Materia Fumus requested.
     * @param simulate If true, only calculates the result without actually modifying the tank.
     * @return A MateriaStack containing the extracted Materia Fumus (or EMPTY if none could be extracted).
     */
    @NotNull
    MateriaStack drain(int maxDrain, boolean simulate);
}
