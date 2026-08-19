package ddraig.net.entropica.network;

import ddraig.net.entropica.Entropica;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record SyncAstralProgressPayload(List<ResourceLocation> discoveredConstellations, List<String> chartedConnections) implements CustomPacketPayload {

    public static final Type<SyncAstralProgressPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "sync_astral_progress"));

    public static final StreamCodec<ByteBuf, SyncAstralProgressPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), SyncAstralProgressPayload::discoveredConstellations,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), SyncAstralProgressPayload::chartedConnections,
            SyncAstralProgressPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
