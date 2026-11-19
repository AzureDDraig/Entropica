package ddraig.net.entropica.worldgen.biome;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

import java.util.Set;
import java.util.function.Supplier;

/**
 * Duck interface implemented on {@link net.minecraft.world.level.biome.MultiNoiseBiomeSource}
 * via Mixin to attach and query Entropica's multi-noise overlay layer idempotently.
 */
public interface EntropicaBiomeSourceExtension {
    void entropica$setMultiNoiseLayer(EntropicaMultiNoiseLayer layer);

    EntropicaMultiNoiseLayer entropica$getMultiNoiseLayer();

    Supplier<Set<Holder<Biome>>> entropica$getOrCaptureOriginalPossibleBiomes(Supplier<Set<Holder<Biome>>> current);
}
