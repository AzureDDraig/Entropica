package ddraig.net.entropica.network;

import ddraig.net.entropica.Entropica;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ReinscribeStarChartPayload(ResourceLocation constellationId) implements CustomPacketPayload {

    public static final Type<ReinscribeStarChartPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "reinscribe_star_chart"));

    public static final StreamCodec<ByteBuf, ReinscribeStarChartPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ReinscribeStarChartPayload::constellationId,
            ReinscribeStarChartPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
