package ddraig.net.entropica.astral;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ddraig.net.entropica.api.EssenceType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record SupernovaEvent(
        UUID eventId,
        String starNodeId,
        float azimuthDeg,
        float altitudeDeg,
        float bipolarAngleRad,
        SupernovaPhase phase,
        long startTick,
        int durationTicks,
        float progress,
        SpectralClass progenitorSpectralClass,
        EssenceType progenitorEssence,
        StellarRemnantType remnantType,
        EssenceType remnantEssence,
        String remnantTitle
) {
    public static final Codec<SupernovaEvent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("eventId").forGetter(SupernovaEvent::eventId),
            Codec.STRING.fieldOf("starNodeId").forGetter(SupernovaEvent::starNodeId),
            Codec.FLOAT.fieldOf("azimuthDeg").forGetter(SupernovaEvent::azimuthDeg),
            Codec.FLOAT.fieldOf("altitudeDeg").forGetter(SupernovaEvent::altitudeDeg),
            Codec.FLOAT.fieldOf("bipolarAngleRad").forGetter(SupernovaEvent::bipolarAngleRad),
            Codec.STRING.xmap(SupernovaPhase::valueOf, SupernovaPhase::name).fieldOf("phase").forGetter(SupernovaEvent::phase),
            Codec.LONG.fieldOf("startTick").forGetter(SupernovaEvent::startTick),
            Codec.INT.fieldOf("durationTicks").forGetter(SupernovaEvent::durationTicks),
            Codec.FLOAT.fieldOf("progress").forGetter(SupernovaEvent::progress),
            Codec.STRING.xmap(SpectralClass::valueOf, SpectralClass::name).fieldOf("progenitorSpectralClass").forGetter(SupernovaEvent::progenitorSpectralClass),
            Codec.STRING.xmap(EssenceType::valueOf, EssenceType::name).fieldOf("progenitorEssence").forGetter(SupernovaEvent::progenitorEssence),
            Codec.STRING.xmap(StellarRemnantType::valueOf, StellarRemnantType::name).fieldOf("remnantType").forGetter(SupernovaEvent::remnantType),
            Codec.STRING.xmap(EssenceType::valueOf, EssenceType::name).fieldOf("remnantEssence").forGetter(SupernovaEvent::remnantEssence),
            Codec.STRING.fieldOf("remnantTitle").forGetter(SupernovaEvent::remnantTitle)
    ).apply(instance, SupernovaEvent::new));

    public static final StreamCodec<ByteBuf, SupernovaEvent> STREAM_CODEC = StreamCodec.of(
            (buf, e) -> {
                buf.writeLong(e.eventId().getMostSignificantBits());
                buf.writeLong(e.eventId().getLeastSignificantBits());
                ByteBufCodecs.STRING_UTF8.encode(buf, e.starNodeId());
                buf.writeFloat(e.azimuthDeg());
                buf.writeFloat(e.altitudeDeg());
                buf.writeFloat(e.bipolarAngleRad());
                ByteBufCodecs.STRING_UTF8.encode(buf, e.phase().name());
                buf.writeLong(e.startTick());
                buf.writeInt(e.durationTicks());
                buf.writeFloat(e.progress());
                ByteBufCodecs.STRING_UTF8.encode(buf, e.progenitorSpectralClass().name());
                ByteBufCodecs.STRING_UTF8.encode(buf, e.progenitorEssence().name());
                ByteBufCodecs.STRING_UTF8.encode(buf, e.remnantType().name());
                ByteBufCodecs.STRING_UTF8.encode(buf, e.remnantEssence().name());
                ByteBufCodecs.STRING_UTF8.encode(buf, e.remnantTitle());
            },
            buf -> {
                UUID id = new UUID(buf.readLong(), buf.readLong());
                String nodeId = ByteBufCodecs.STRING_UTF8.decode(buf);
                float azim = buf.readFloat();
                float alt = buf.readFloat();
                float bAngle = buf.readFloat();
                SupernovaPhase ph = SupernovaPhase.valueOf(ByteBufCodecs.STRING_UTF8.decode(buf));
                long sTick = buf.readLong();
                int dur = buf.readInt();
                float prog = buf.readFloat();
                SpectralClass pSc = SpectralClass.valueOf(ByteBufCodecs.STRING_UTF8.decode(buf));
                EssenceType pEss = EssenceType.valueOf(ByteBufCodecs.STRING_UTF8.decode(buf));
                StellarRemnantType rType = StellarRemnantType.valueOf(ByteBufCodecs.STRING_UTF8.decode(buf));
                EssenceType rEss = EssenceType.valueOf(ByteBufCodecs.STRING_UTF8.decode(buf));
                String rTitle = ByteBufCodecs.STRING_UTF8.decode(buf);
                return new SupernovaEvent(id, nodeId, azim, alt, bAngle, ph, sTick, dur, prog, pSc, pEss, rType, rEss, rTitle);
            }
    );
}
