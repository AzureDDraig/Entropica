package ddraig.net.entropica.mixin;

import ddraig.net.entropica.worldgen.biome.EntropicaBiomeInjector;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Runs Entropica's native multi-noise biome & surface rule injection at {@code createLevels} HEAD
 * with priority 1500 — strictly after {@code ServerAboutToStart} and priority-1000 Mixins (such as
 * TerraBlender's region initializer), and strictly before {@code ServerLevel}s are instantiated.
 */
@Mixin(value = MinecraftServer.class, priority = 1500)
public abstract class MinecraftServerBiomeInjectorMixin {

    @Inject(method = "createLevels", at = @At("HEAD"))
    private void entropica$injectMultiNoiseBiomes(CallbackInfo ci) {
        EntropicaBiomeInjector.injectServerLevels((MinecraftServer) (Object) this);
    }
}
