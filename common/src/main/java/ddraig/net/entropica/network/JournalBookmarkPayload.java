package ddraig.net.entropica.network;

import ddraig.net.entropica.Entropica;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record JournalBookmarkPayload(int entryIndex) implements CustomPacketPayload {

    public static final Type<JournalBookmarkPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "journal_bookmark")
    );

    public static final StreamCodec<ByteBuf, JournalBookmarkPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, JournalBookmarkPayload::entryIndex,
            JournalBookmarkPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
