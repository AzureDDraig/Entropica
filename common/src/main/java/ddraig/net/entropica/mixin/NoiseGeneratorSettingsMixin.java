package ddraig.net.entropica.mixin;

import ddraig.net.entropica.worldgen.biome.EntropicaSurfaceRuleStore;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prepends biome-scoped Entropica {@link SurfaceRules.RuleSource}s onto {@link NoiseGeneratorSettings#surfaceRule()}
 * when TerraBlender's {@code SurfaceRuleManager} is not already handling that dimension.
 *
 * <p>Kept strictly field-free because {@code NoiseGeneratorSettings} is a Java {@code Record}.</p>
 */
@Mixin(value = NoiseGeneratorSettings.class, priority = 1500)
public abstract class NoiseGeneratorSettingsMixin {

    @Inject(method = "surfaceRule", at = @At("RETURN"), cancellable = true)
    private void entropica$prependCustomSurfaceRules(CallbackInfoReturnable<SurfaceRules.RuleSource> cir) {
        SurfaceRules.RuleSource wrapped = EntropicaSurfaceRuleStore.resolve(
                (NoiseGeneratorSettings) (Object) this,
                cir.getReturnValue()
        );
        if (wrapped != null) {
            cir.setReturnValue(wrapped);
        }
    }
}
