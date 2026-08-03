package ddraig.net.entropica.fabric;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.event.GlassCleansingHandler;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.fabric.ModFluidsFabric;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;

public class EntropicaFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (GlassCleansingHandler.tryCleansing(player, world, hand, hitResult.getBlockPos())) {
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });

        // Register Fabric-specific fluids
        Registry.register(BuiltInRegistries.FLUID, 
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "diluted_essence_fluid"), 
            ModFluidsFabric.STILL);
        Registry.register(BuiltInRegistries.FLUID, 
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "flowing_diluted_essence_fluid"), 
            ModFluidsFabric.FLOWING);

        // Register paralysis interaction event blocks natively on Fabric
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player.hasEffect(ModEffects.PARALYZED)) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (player.hasEffect(ModEffects.PARALYZED)) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        // Common initialization
        Entropica.init();
    }
}
