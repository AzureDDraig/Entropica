package ddraig.net.entropica.network;

import ddraig.net.entropica.Entropica;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ConstellationDiscoveryPayload(ResourceLocation constellationId) implements CustomPacketPayload {

    public static final Type<ConstellationDiscoveryPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "constellation_discovery"));

    public static final StreamCodec<ByteBuf, ConstellationDiscoveryPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ConstellationDiscoveryPayload::constellationId,
            ConstellationDiscoveryPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
