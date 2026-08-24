package ddraig.net.entropica.api.ichor;

/**
 * Capability interface for anything that can store, accept, or provide Materia Ichor.
 * Implement this on BlockEntities (like the Ichor Input Port or vessels) to allow Ichor transfer.
 */
public interface IIchorHandler {

    /**
     * Fills the handler with Materia Ichor.
     * @param resource VisIchorStack representing the Ichor to be filled.
     * @param simulate If true, the action will only be simulated without mutating state.
     * @return The amount of Ichor that was (or would be) successfully accepted.
     */
    int fill(VisIchorStack resource, boolean simulate);

    /**
     * Drains Materia Ichor from the handler.
     * @param maxDrain Maximum amount of Ichor to be drained.
     * @param simulate If true, the action will only be simulated without mutating state.
     * @return A VisIchorStack representing the drained Ichor.
     */
    VisIchorStack drainIchor(int maxDrain, boolean simulate);

    /**
     * Gets the current Materia Ichor stored in the handler.
     * @return The VisIchorStack currently stored, or VisIchorStack.EMPTY if none.
     */
    VisIchorStack getIchorInTank();

    /**
     * Gets the maximum capacity of the handler.
     * @return The maximum amount of Ichor this handler can hold.
     */
    int getCapacity();
}