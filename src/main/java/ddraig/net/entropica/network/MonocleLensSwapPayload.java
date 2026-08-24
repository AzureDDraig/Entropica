package ddraig.net.entropica.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MonocleLensSwapPayload(boolean dummy) implements CustomPacketPayload {

    public static final Type<MonocleLensSwapPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("entropica", "monocle_lens_swap"));

    public static final StreamCodec<FriendlyByteBuf, MonocleLensSwapPayload> STREAM_CODEC = StreamCodec.composite(
            net.minecraft.network.codec.ByteBufCodecs.BOOL, MonocleLensSwapPayload::dummy,
            MonocleLensSwapPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
