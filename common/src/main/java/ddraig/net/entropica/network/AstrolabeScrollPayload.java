package ddraig.net.entropica.network;

import ddraig.net.entropica.Entropica;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AstrolabeScrollPayload(int delta) implements CustomPacketPayload {

    public static final Type<AstrolabeScrollPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "astrolabe_scroll"));

    public static final StreamCodec<ByteBuf, AstrolabeScrollPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, AstrolabeScrollPayload::delta,
            AstrolabeScrollPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
