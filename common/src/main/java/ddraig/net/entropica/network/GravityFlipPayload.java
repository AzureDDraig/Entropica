package ddraig.net.entropica.network;

import ddraig.net.entropica.Entropica;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record GravityFlipPayload(double targetGravity, boolean inverted) implements CustomPacketPayload {

    public static final Type<GravityFlipPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "gravity_flip"));

    public static final StreamCodec<ByteBuf, GravityFlipPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, GravityFlipPayload::targetGravity,
            ByteBufCodecs.BOOL, GravityFlipPayload::inverted,
            GravityFlipPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
