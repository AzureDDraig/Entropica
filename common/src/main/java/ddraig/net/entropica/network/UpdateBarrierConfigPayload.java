package ddraig.net.entropica.network;

import ddraig.net.entropica.Entropica;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * C2S packet sent when the creator updates barrier settings.
 */
public record UpdateBarrierConfigPayload(
        int entityId,
        int shapeOrdinal,
        float width,
        float height,
        float radius,
        int filterModeOrdinal,
        int themeOrdinal,
        float elasticity
) implements CustomPacketPayload {

    public static final Type<UpdateBarrierConfigPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "update_barrier_config"));

    public static final StreamCodec<FriendlyByteBuf, UpdateBarrierConfigPayload> STREAM_CODEC =
            CustomPacketPayload.codec(UpdateBarrierConfigPayload::write, UpdateBarrierConfigPayload::new);

    public UpdateBarrierConfigPayload(FriendlyByteBuf buf) {
        this(
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readFloat()
        );
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeVarInt(shapeOrdinal);
        buf.writeFloat(width);
        buf.writeFloat(height);
        buf.writeFloat(radius);
        buf.writeVarInt(filterModeOrdinal);
        buf.writeVarInt(themeOrdinal);
        buf.writeFloat(elasticity);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
