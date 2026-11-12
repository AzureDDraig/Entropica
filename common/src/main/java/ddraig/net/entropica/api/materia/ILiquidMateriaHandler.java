package ddraig.net.entropica.api.materia;

import ddraig.net.entropica.api.pressure.IPressureHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Handler interface for Hydraulic Pipeline network components (Mliq).
 */
public interface ILiquidMateriaHandler extends IPressureHandler {

    int getCapacity();

    @NotNull
    MateriaLiquidaStack getLiquidInTank();

    int fill(MateriaLiquidaStack resource, boolean simulate);

    @NotNull
    MateriaLiquidaStack drainLiquid(int maxDrain, boolean simulate);

    @Override
    default int getSafeCapacity() {
        return getCapacity();
    }

    @Override
    default int getAbsoluteCapacity() {
        return getCapacity(); // Liquids don't compress
    }

    @Override
    @NotNull
    default MateriaStack getMateriaInTank() {
        return getLiquidInTank();
    }

    @Override
    default int fill(MateriaStack resource, boolean simulate) {
        if (resource instanceof MateriaLiquidaStack liquid) {
            return fill(liquid, simulate);
        }
        return 0;
    }

    @Override
    @NotNull
    default MateriaStack drain(int maxDrain, boolean simulate) {
        return drainLiquid(maxDrain, simulate);
    }
}
