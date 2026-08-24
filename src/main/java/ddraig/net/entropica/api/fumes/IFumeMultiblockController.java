package ddraig.net.entropica.api.fumes;

public interface IFumeMultiblockController {
    boolean isFormed();

    VisFumeStack getStoredFume();

    /** The safe operating capacity (1.0 Pressure) of the entire multiblock */
    int getSafeCapacity();

    /** The absolute breaking point of the entire multiblock */
    int getAbsoluteCapacity();

    void invalidateMultiblock(); // <- Added this so the port can trigger breaks
}