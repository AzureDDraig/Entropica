package ddraig.net.entropica.registry;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.level.material.FlowingFluid;
import java.util.function.Supplier;

public class ModFluids {
    public static final Supplier<FlowingFluid> DILUTED_ESSENCE_FLUID = ModFluids::getSource;
    public static final Supplier<FlowingFluid> DILUTED_ESSENCE_FLUID_FLOWING = ModFluids::getFlowing;

    @ExpectPlatform
    public static FlowingFluid getSource() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static FlowingFluid getFlowing() {
        throw new AssertionError();
    }
}