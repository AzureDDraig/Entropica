package ddraig.net.entropica.network;

import ddraig.net.entropica.Entropica;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SpellApparatusScrollPayload(int delta) implements CustomPacketPayload {
    public static final Type<SpellApparatusScrollPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spell_apparatus_scroll"));

    public static final StreamCodec<ByteBuf, SpellApparatusScrollPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SpellApparatusScrollPayload::delta,
            SpellApparatusScrollPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
