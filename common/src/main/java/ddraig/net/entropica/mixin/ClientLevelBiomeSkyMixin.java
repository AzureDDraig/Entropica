package ddraig.net.entropica.mixin;

import ddraig.net.entropica.client.worldgen.BiomeAtmosphereClientManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public abstract class ClientLevelBiomeSkyMixin {

    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
    private void entropica$adjustBiomeSkyColor(Vec3 cameraPos, float partialTick, CallbackInfoReturnable<Integer> cir) {
        int original = cir.getReturnValueI();
        int adjusted = BiomeAtmosphereClientManager.adjustSkyColor((ClientLevel) (Object) this, cameraPos, partialTick, original);
        if (adjusted != original) {
            cir.setReturnValue(adjusted);
        }
    }

    @Inject(method = "getStarBrightness", at = @At("RETURN"), cancellable = true)
    private void entropica$boostAstralStarBrightness(float partialTick, CallbackInfoReturnable<Float> cir) {
        float original = cir.getReturnValueF();
        float adjusted = BiomeAtmosphereClientManager.adjustStarBrightness((ClientLevel) (Object) this, original);
        if (adjusted != original) {
            cir.setReturnValue(adjusted);
        }
    }
}
