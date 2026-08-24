package ddraig.net.entropica.api.materia;

import org.jetbrains.annotations.NotNull;

/**
 * Handler interface for Hydraulic Pipeline network components (Mliq).
 */
public interface ILiquidMateriaHandler {

    int getCapacity();

    @NotNull
    MateriaLiquidaStack getLiquidInTank();

    int fill(MateriaLiquidaStack resource, boolean simulate);

    @NotNull
    MateriaLiquidaStack drainLiquid(int maxDrain, boolean simulate);
}
