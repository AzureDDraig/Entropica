package ddraig.net.entropica.api.fumes;

public interface IFumeMultiblockController {
    boolean isFormed();
    VisFumeStack getStoredFume();
    int getMaxCapacity();
}