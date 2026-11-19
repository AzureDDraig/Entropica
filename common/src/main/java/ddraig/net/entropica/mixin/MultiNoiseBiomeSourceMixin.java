package ddraig.net.entropica.mixin;

import ddraig.net.entropica.worldgen.biome.EntropicaBiomeSourceExtension;
import ddraig.net.entropica.worldgen.biome.EntropicaMultiNoiseLayer;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Attaches Entropica's regional multi-noise overlay layer onto {@link MultiNoiseBiomeSource}
 * without replacing or mutating its underlying {@code Climate.ParameterList}, preserving 100%
 * compatibility with TerraBlender's {@code IExtendedParameterList} regional trees.
 */
@Mixin(value = MultiNoiseBiomeSource.class, priority = 1500)
public abstract class MultiNoiseBiomeSourceMixin implements EntropicaBiomeSourceExtension {

    @Unique
    private volatile EntropicaMultiNoiseLayer entropica$multiNoiseLayer;

    @Unique
    private Supplier<Set<Holder<Biome>>> entropica$originalPossibleBiomes;

    @Override
    public void entropica$setMultiNoiseLayer(EntropicaMultiNoiseLayer layer) {
        this.entropica$multiNoiseLayer = layer;
    }

    @Override
    public EntropicaMultiNoiseLayer entropica$getMultiNoiseLayer() {
        return this.entropica$multiNoiseLayer;
    }

    @Override
    public Supplier<Set<Holder<Biome>>> entropica$getOrCaptureOriginalPossibleBiomes(Supplier<Set<Holder<Biome>>> current) {
        if (this.entropica$originalPossibleBiomes == null) {
            this.entropica$originalPossibleBiomes = current;
        }
        return this.entropica$originalPossibleBiomes;
    }

    @Inject(
            method = "getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void entropica$resolveMultiNoiseBiome(
            int quartX,
            int quartY,
            int quartZ,
            Climate.Sampler sampler,
            CallbackInfoReturnable<Holder<Biome>> cir
    ) {
        EntropicaMultiNoiseLayer layer = this.entropica$multiNoiseLayer;
        if (layer == null) {
            return;
        }
        Holder<Biome> base = cir.getReturnValue();
        Holder<Biome> resolved = layer.resolveBiome(quartX, quartY, quartZ, sampler, base);
        if (resolved != null && resolved != base) {
            cir.setReturnValue(resolved);
        }
    }

    @Inject(method = "collectPossibleBiomes", at = @At("RETURN"), cancellable = true)
    private void entropica$appendPossibleBiomes(CallbackInfoReturnable<Stream<Holder<Biome>>> cir) {
        EntropicaMultiNoiseLayer layer = this.entropica$multiNoiseLayer;
        if (layer != null && !layer.isEmpty()) {
            cir.setReturnValue(Stream.concat(cir.getReturnValue(), layer.activeBiomes().stream()));
        }
    }
}
