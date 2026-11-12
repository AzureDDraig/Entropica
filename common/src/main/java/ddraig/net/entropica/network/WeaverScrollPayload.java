package ddraig.net.entropica.network;

import ddraig.net.entropica.Entropica;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record WeaverScrollPayload(int delta) implements CustomPacketPayload {
    public static final Type<WeaverScrollPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "weaver_scroll"));

    public static final StreamCodec<ByteBuf, WeaverScrollPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, WeaverScrollPayload::delta,
            WeaverScrollPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
