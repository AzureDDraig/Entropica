package ddraig.net.entropica.client.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record FumeParticleOption(float r, float g, float b) implements ParticleOptions {

    public static final MapCodec<FumeParticleOption> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("r").forGetter(FumeParticleOption::r),
            Codec.FLOAT.fieldOf("g").forGetter(FumeParticleOption::g),
            Codec.FLOAT.fieldOf("b").forGetter(FumeParticleOption::b)
    ).apply(instance, FumeParticleOption::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FumeParticleOption> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, FumeParticleOption::r,
            ByteBufCodecs.FLOAT, FumeParticleOption::g,
            ByteBufCodecs.FLOAT, FumeParticleOption::b,
            FumeParticleOption::new
    );

    @Override
    public ParticleType<?> getType() {
        return ModParticles.FUME_PARTICLE.get();
    }
}