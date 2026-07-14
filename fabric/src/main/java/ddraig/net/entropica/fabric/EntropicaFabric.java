package ddraig.net.entropica.fabric;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.fabric.ModFluidsFabric;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class EntropicaFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // Register Fabric-specific fluids
        Registry.register(BuiltInRegistries.FLUID, 
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "diluted_essence_fluid"), 
            ModFluidsFabric.STILL);
        Registry.register(BuiltInRegistries.FLUID, 
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "flowing_diluted_essence_fluid"), 
            ModFluidsFabric.FLOWING);

        // Common initialization
        Entropica.init();
    }
}
