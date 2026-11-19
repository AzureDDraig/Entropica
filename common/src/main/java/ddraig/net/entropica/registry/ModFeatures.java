package ddraig.net.entropica.registry;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.worldgen.feature.AegisCrystalFormationFeature;
import ddraig.net.entropica.worldgen.feature.BlueGlacierIceShelfFeature;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Entropica.MODID, Registries.FEATURE);

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> BLUE_GLACIER_ICE_SHELF =
            FEATURES.register("blue_glacier_ice_shelf",
                    () -> new BlueGlacierIceShelfFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> AEGIS_CRYSTAL_FORMATION =
            FEATURES.register("aegis_crystal_formation",
                    () -> new AegisCrystalFormationFeature(NoneFeatureConfiguration.CODEC));

    private ModFeatures() {}

    public static void register() {
        FEATURES.register();
    }
}
