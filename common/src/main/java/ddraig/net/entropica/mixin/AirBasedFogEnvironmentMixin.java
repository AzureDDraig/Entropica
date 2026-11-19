package ddraig.net.entropica.mixin;

import ddraig.net.entropica.client.worldgen.BiomeAtmosphereClientManager;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.environment.AirBasedFogEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AirBasedFogEnvironment.class)
public abstract class AirBasedFogEnvironmentMixin {

    @Inject(method = "getBaseColor", at = @At("RETURN"), cancellable = true)
    private void entropica$tintBiomeFogColor(
            ClientLevel level,
            Camera camera,
            int renderDistanceChunks,
            float partialTick,
            CallbackInfoReturnable<Integer> cir
    ) {
        int original = cir.getReturnValueI();
        int adjusted = BiomeAtmosphereClientManager.adjustFogBaseColor(level, camera, partialTick, original);
        if (adjusted != original) {
            cir.setReturnValue(adjusted);
        }
    }
}
