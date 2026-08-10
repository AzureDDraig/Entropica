package ddraig.net.entropica.client.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TimedTintableParticleOption(
        ParticleType<TimedTintableParticleOption> type,
        float r,
        float g,
        float b,
        int maxAge,
        float scale,
        float roll,
        float rollSpeed,
        int motionMode,
        float param1,
        float param2,
        float param3
) implements ParticleOptions {

    public static final int MODE_LINEAR = 0;
    public static final int MODE_TOWARDS = 1;
    public static final int MODE_AWAY = 2;
    public static final int MODE_JITTER = 3;
    public static final int MODE_NOISE = 4;

    public static MapCodec<TimedTintableParticleOption> codec(ParticleType<TimedTintableParticleOption> type) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.FLOAT.fieldOf("r").forGetter(TimedTintableParticleOption::r),
                Codec.FLOAT.fieldOf("g").forGetter(TimedTintableParticleOption::g),
                Codec.FLOAT.fieldOf("b").forGetter(TimedTintableParticleOption::b),
                Codec.INT.fieldOf("maxAge").forGetter(TimedTintableParticleOption::maxAge),
                Codec.FLOAT.fieldOf("scale").forGetter(TimedTintableParticleOption::scale),
                Codec.FLOAT.fieldOf("roll").forGetter(TimedTintableParticleOption::roll),
                Codec.FLOAT.fieldOf("rollSpeed").forGetter(TimedTintableParticleOption::rollSpeed),
                Codec.INT.fieldOf("motionMode").forGetter(TimedTintableParticleOption::motionMode),
                Codec.FLOAT.fieldOf("param1").forGetter(TimedTintableParticleOption::param1),
                Codec.FLOAT.fieldOf("param2").forGetter(TimedTintableParticleOption::param2),
                Codec.FLOAT.fieldOf("param3").forGetter(TimedTintableParticleOption::param3)
        ).apply(instance, (r, g, b, maxAge, scale, roll, rollSpeed, motionMode, p1, p2, p3) ->
                new TimedTintableParticleOption(type, r, g, b, maxAge, scale, roll, rollSpeed, motionMode, p1, p2, p3)));
    }

    public static StreamCodec<RegistryFriendlyByteBuf, TimedTintableParticleOption> streamCodec(ParticleType<TimedTintableParticleOption> type) {
        return StreamCodec.composite(
                ByteBufCodecs.FLOAT, TimedTintableParticleOption::r,
                ByteBufCodecs.FLOAT, TimedTintableParticleOption::g,
                ByteBufCodecs.FLOAT, TimedTintableParticleOption::b,
                ByteBufCodecs.INT, TimedTintableParticleOption::maxAge,
                ByteBufCodecs.FLOAT, TimedTintableParticleOption::scale,
                ByteBufCodecs.FLOAT, TimedTintableParticleOption::roll,
                ByteBufCodecs.FLOAT, TimedTintableParticleOption::rollSpeed,
                ByteBufCodecs.INT, TimedTintableParticleOption::motionMode,
                ByteBufCodecs.FLOAT, TimedTintableParticleOption::param1,
                ByteBufCodecs.FLOAT, TimedTintableParticleOption::param2,
                ByteBufCodecs.FLOAT, TimedTintableParticleOption::param3,
                (r, g, b, maxAge, scale, roll, rollSpeed, motionMode, p1, p2, p3) ->
                        new TimedTintableParticleOption(type, r, g, b, maxAge, scale, roll, rollSpeed, motionMode, p1, p2, p3)
        );
    }

    @Override
    public ParticleType<TimedTintableParticleOption> getType() {
        return this.type;
    }
}
