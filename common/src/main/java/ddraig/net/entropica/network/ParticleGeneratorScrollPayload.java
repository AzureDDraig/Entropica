package ddraig.net.entropica.network;

import ddraig.net.entropica.Entropica;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import io.netty.buffer.ByteBuf;

public record ParticleGeneratorScrollPayload(BlockPos pos, int delta) implements CustomPacketPayload {

    public static final Type<ParticleGeneratorScrollPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "particle_generator_scroll"));

    public static final StreamCodec<ByteBuf, ParticleGeneratorScrollPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ParticleGeneratorScrollPayload::pos,
            ByteBufCodecs.INT, ParticleGeneratorScrollPayload::delta,
            ParticleGeneratorScrollPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}