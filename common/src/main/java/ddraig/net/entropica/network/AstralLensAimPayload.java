package ddraig.net.entropica.network;

import ddraig.net.entropica.Entropica;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AstralLensAimPayload(BlockPos pos, float yaw, float pitch, String targetName, boolean isFocused) implements CustomPacketPayload {

    public static final Type<AstralLensAimPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "astral_lens_aim"));

    public static final StreamCodec<ByteBuf, AstralLensAimPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, AstralLensAimPayload::pos,
            ByteBufCodecs.FLOAT, AstralLensAimPayload::yaw,
            ByteBufCodecs.FLOAT, AstralLensAimPayload::pitch,
            ByteBufCodecs.STRING_UTF8, AstralLensAimPayload::targetName,
            ByteBufCodecs.BOOL, AstralLensAimPayload::isFocused,
            AstralLensAimPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
