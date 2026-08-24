package ddraig.net.entropica.api.materia;

/**
 * Interface for Vapor Pneumatic multiblock controllers (Mfum / Msub).
 */
public interface IVaporMultiblockController {
    boolean isFormed();

    MateriaFumusStack getStoredMateria();

    /** The safe operating capacity (1.0 Pressure) of the entire multiblock */
    int getSafeCapacity();

    /** The absolute breaking point of the entire multiblock */
    int getAbsoluteCapacity();

    void invalidateMultiblock();
}
