package ddraig.net.entropica.api.pressure;

import ddraig.net.entropica.api.materia.MateriaStack;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IPressureHandler {
    int getSafeCapacity();
    int getAbsoluteCapacity();

    default int getTransferRate() {
        return 20;
    }

    @NotNull
    MateriaStack getMateriaInTank();

    int fill(MateriaStack resource, boolean simulate);

    @NotNull
    MateriaStack drain(int maxDrain, boolean simulate);

    default float getPressure() {
        int safe = getSafeCapacity();
        if (safe <= 0) return 0.0f;
        return (float) getMateriaInTank().getAmount() / safe;
    }

    /** Pressure loss factor per block distance (friction). */
    default float getResistance() {
        return 0.0f;
    }

    /** Returns true if this handler is actively managed by a global graph. */
    default boolean isManagedByGraph() { return false; }
    default void setManagedByGraph(boolean managed) {}

    /** Temp tick-level external boost registered by adjacent pumps. */
    default void applyActiveBoost(Direction direction, float strength) {}
    
    @Nullable
    default Direction getActiveBoostDirection() {
        return null;
    }
    
    default float getActiveBoostStrength() {
        return 0.0f;
    }
    
    default void clearActiveBoost() {}
}
