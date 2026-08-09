package ddraig.net.entropica.registry;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.particle.FumeParticleOption;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Entropica.MODID, net.minecraft.resources.ResourceKey.createRegistryKey(net.minecraft.resources.ResourceLocation.withDefaultNamespace("particle_type")));

    public static final RegistrySupplier<ParticleType<FumeParticleOption>> FUME_PARTICLE = PARTICLES.register("fume", () -> new ParticleType<FumeParticleOption>(false) {
        @Override
        public MapCodec<FumeParticleOption> codec() {
            return FumeParticleOption.CODEC;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, FumeParticleOption> streamCodec() {
            return FumeParticleOption.STREAM_CODEC;
        }
    });

    public static final RegistrySupplier<SimpleParticleType> SPECTRUM_SPARKLE = PARTICLES.register("spectrum_sparkle", () -> new SimpleParticleType(false) {});
    public static final RegistrySupplier<SimpleParticleType> GALE_SWIRL_PUFF = PARTICLES.register("gale_swirl_puff", () -> new SimpleParticleType(false) {});
}