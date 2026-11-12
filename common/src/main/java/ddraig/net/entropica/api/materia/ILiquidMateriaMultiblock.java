package ddraig.net.entropica.api.materia;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import ddraig.net.entropica.api.EssenceType;

/**
 * Extended handler for large-scale Liquid Materia structures.
 */
public interface ILiquidMateriaMultiblock extends ILiquidMateriaHandler {
    boolean isFormed();
    BlockPos getMasterPos();
    boolean isMaster();
    boolean canConnectLiquid(Direction side);
    boolean isLiquidValid(EssenceType type);
}
