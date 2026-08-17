package ddraig.net.entropica.network;

import ddraig.net.entropica.Entropica;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TelescopeAimPayload(BlockPos pos, float yaw, float pitch) implements CustomPacketPayload {

    public static final Type<TelescopeAimPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "telescope_aim"));

    public static final StreamCodec<ByteBuf, TelescopeAimPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, TelescopeAimPayload::pos,
            ByteBufCodecs.FLOAT, TelescopeAimPayload::yaw,
            ByteBufCodecs.FLOAT, TelescopeAimPayload::pitch,
            TelescopeAimPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
