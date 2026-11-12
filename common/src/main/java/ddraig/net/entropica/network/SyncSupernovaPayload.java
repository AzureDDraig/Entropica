package ddraig.net.entropica.network;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.astral.SupernovaEvent;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record SyncSupernovaPayload(List<SupernovaEvent> events) implements CustomPacketPayload {

    public static final Type<SyncSupernovaPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "sync_supernova"));

    public static final StreamCodec<ByteBuf, SyncSupernovaPayload> STREAM_CODEC = StreamCodec.composite(
            SupernovaEvent.STREAM_CODEC.apply(ByteBufCodecs.list()), SyncSupernovaPayload::events,
            SyncSupernovaPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
