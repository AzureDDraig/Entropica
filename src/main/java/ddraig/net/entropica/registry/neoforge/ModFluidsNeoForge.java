package ddraig.net.entropica.registry.neoforge;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModFluidsNeoForge {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Entropica.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(BuiltInRegistries.FLUID, Entropica.MODID);

    public static final Supplier<FluidType> DILUTED_ESSENCE_FLUID_TYPE = FLUID_TYPES.register("diluted_essence_fluid",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("block.entropica.diluted_essence_fluid")
                    .fallDistanceModifier(0F)
                    .canExtinguish(true)
                    .canConvertToSource(false)
                    .density(1000)
                    .viscosity(1000)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)));

    private static FlowingFluid SOURCE_INSTANCE;
    private static FlowingFluid FLOWING_INSTANCE;

    public static FlowingFluid getSource() {
        if (SOURCE_INSTANCE == null) SOURCE_INSTANCE = new BaseFlowingFluid.Source(DILUTED_ESSENCE_PROPERTIES);
        return SOURCE_INSTANCE;
    }

    public static FlowingFluid getFlowing() {
        if (FLOWING_INSTANCE == null) FLOWING_INSTANCE = new BaseFlowingFluid.Flowing(DILUTED_ESSENCE_PROPERTIES);
        return FLOWING_INSTANCE;
    }

    public static final Supplier<FlowingFluid> DILUTED_ESSENCE_FLUID = FLUIDS.register("diluted_essence_fluid", ModFluidsNeoForge::getSource);
    public static final Supplier<FlowingFluid> DILUTED_ESSENCE_FLUID_FLOWING = FLUIDS.register("flowing_diluted_essence_fluid", ModFluidsNeoForge::getFlowing);

    public static final BaseFlowingFluid.Properties DILUTED_ESSENCE_PROPERTIES = new BaseFlowingFluid.Properties(
            DILUTED_ESSENCE_FLUID_TYPE, ModFluidsNeoForge::getSource, ModFluidsNeoForge::getFlowing)
            .slopeFindDistance(2)
            .levelDecreasePerBlock(1)
            .block(() -> (net.minecraft.world.level.block.LiquidBlock) ModBlocks.DILUTED_ESSENCE_FLUID_BLOCK.get());
}
