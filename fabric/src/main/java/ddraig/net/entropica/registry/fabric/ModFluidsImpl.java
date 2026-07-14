package ddraig.net.entropica.registry.fabric;

import net.minecraft.world.level.material.FlowingFluid;

public class ModFluidsImpl {
    public static FlowingFluid getSource() {
        return ModFluidsFabric.STILL;
    }

    public static FlowingFluid getFlowing() {
        return ModFluidsFabric.FLOWING;
    }
}
