package ddraig.net.entropica.network;

import ddraig.net.entropica.Entropica;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * C2S packet sent when the creator updates barrier settings.
 * Contains all 12 configurable physical, visual, and permission properties.
 */
public record UpdateBarrierConfigPayload(
        int entityId,
        int shapeOrdinal,
        float width,
        float height,
        float radius,
        int filterModeOrdinal,
        int themeOrdinal,
        float elasticity,
        boolean oneWay,
        int redstoneMode,
        int colorTint,
        List<String> whitelistUsernames
) implements CustomPacketPayload {

    public static final Type<UpdateBarrierConfigPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "update_barrier_config"));

    public static final StreamCodec<FriendlyByteBuf, UpdateBarrierConfigPayload> STREAM_CODEC =
            CustomPacketPayload.codec(UpdateBarrierConfigPayload::write, UpdateBarrierConfigPayload::new);

    public UpdateBarrierConfigPayload {
        whitelistUsernames = (whitelistUsernames != null) ? List.copyOf(whitelistUsernames) : Collections.emptyList();
    }

    public float sanitizedWidth() {
        if (!Float.isFinite(width)) {
            return (width == Float.POSITIVE_INFINITY) ? 32.0F : ((width == Float.NEGATIVE_INFINITY) ? 1.0F : 4.0F);
        }
        return Math.max(1.0F, Math.min(32.0F, width));
    }

    public float sanitizedHeight() {
        if (!Float.isFinite(height)) {
            return (height == Float.POSITIVE_INFINITY) ? 32.0F : ((height == Float.NEGATIVE_INFINITY) ? 1.0F : 4.0F);
        }
        return Math.max(1.0F, Math.min(32.0F, height));
    }

    public float sanitizedRadius() {
        if (!Float.isFinite(radius)) {
            return (radius == Float.POSITIVE_INFINITY) ? 32.0F : ((radius == Float.NEGATIVE_INFINITY) ? 1.0F : 4.0F);
        }
        return Math.max(1.0F, Math.min(32.0F, radius));
    }

    public float sanitizedElasticity() {
        if (!Float.isFinite(elasticity)) {
            return (elasticity == Float.POSITIVE_INFINITY) ? 3.0F : ((elasticity == Float.NEGATIVE_INFINITY) ? 0.0F : 1.0F);
        }
        return Math.max(0.0F, Math.min(3.0F, elasticity));
    }

    public UpdateBarrierConfigPayload(FriendlyByteBuf buf) {
        this(
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readFloat(),
                buf.readBoolean(),
                buf.readVarInt(),
                buf.readInt(),
                readWhitelist(buf)
        );
    }

    private static List<String> readWhitelist(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        if (count < 0 || count > 128) {
            if (buf.isReadable()) {
                buf.skipBytes(buf.readableBytes());
            }
            return Collections.emptyList();
        }
        List<String> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(buf.readUtf(64));
        }
        return list;
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
        buf.writeBoolean(oneWay);
        buf.writeVarInt(redstoneMode);
        buf.writeInt(colorTint);
        if (whitelistUsernames == null) {
            buf.writeVarInt(0);
        } else {
            int size = Math.min(128, whitelistUsernames.size());
            buf.writeVarInt(size);
            for (int i = 0; i < size; i++) {
                buf.writeUtf(whitelistUsernames.get(i), 64);
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
