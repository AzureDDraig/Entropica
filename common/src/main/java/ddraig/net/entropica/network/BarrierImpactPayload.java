package ddraig.net.entropica.network;

import ddraig.net.entropica.Entropica;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * S2C packet notifying clients of a bounce impact to trigger ripple wave animations and audio.
 */
public record BarrierImpactPayload(
        int entityId,
        float x,
        float y,
        float z,
        float intensity
) implements CustomPacketPayload {

    public static final Type<BarrierImpactPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "barrier_impact"));

    public static final StreamCodec<FriendlyByteBuf, BarrierImpactPayload> STREAM_CODEC =
            CustomPacketPayload.codec(BarrierImpactPayload::write, BarrierImpactPayload::new);

    public BarrierImpactPayload(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeFloat(x);
        buf.writeFloat(y);
        buf.writeFloat(z);
        buf.writeFloat(intensity);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
