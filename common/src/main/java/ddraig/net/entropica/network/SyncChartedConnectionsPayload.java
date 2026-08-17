package ddraig.net.entropica.network;

import ddraig.net.entropica.Entropica;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record SyncChartedConnectionsPayload(List<String> edges, boolean isClear) implements CustomPacketPayload {

    public static final Type<SyncChartedConnectionsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "sync_charted_connections"));

    public static final StreamCodec<ByteBuf, SyncChartedConnectionsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), SyncChartedConnectionsPayload::edges,
            ByteBufCodecs.BOOL, SyncChartedConnectionsPayload::isClear,
            SyncChartedConnectionsPayload::new
    );

    public SyncChartedConnectionsPayload(String singleEdge) {
        this(List.of(singleEdge), false);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
