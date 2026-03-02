package ddraig.net.entropica.registry;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.particle.FumeParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, Entropica.MODID);

    public static final Supplier<ParticleType<FumeParticleOption>> FUME_PARTICLE = PARTICLES.register("fume", () -> new ParticleType<FumeParticleOption>(false) {
        @Override
        public MapCodec<FumeParticleOption> codec() {
            return FumeParticleOption.CODEC;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, FumeParticleOption> streamCodec() {
            return FumeParticleOption.STREAM_CODEC;
        }
    });
}