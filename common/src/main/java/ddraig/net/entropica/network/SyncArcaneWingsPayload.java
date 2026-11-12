package ddraig.net.entropica.network;

import ddraig.net.entropica.Entropica;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * S2C packet synchronizing active Materia-colored 3D Arcane Wings on a player or living entity.
 * Sending {@code remainingTicks <= 0} triggers a smooth wing-fold fade-out on clients.
 */
public record SyncArcaneWingsPayload(
        int entityId,
        int remainingTicks,
        int primaryOrdinal,
        int secondaryOrdinal,
        int potencyTier
) implements CustomPacketPayload {

    public static final Type<SyncArcaneWingsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "sync_arcane_wings"));

    public static final StreamCodec<FriendlyByteBuf, SyncArcaneWingsPayload> STREAM_CODEC =
            CustomPacketPayload.codec(SyncArcaneWingsPayload::write, SyncArcaneWingsPayload::new);

    public SyncArcaneWingsPayload(FriendlyByteBuf buf) {
        this(
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt()
        );
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.entityId);
        buf.writeVarInt(this.remainingTicks);
        buf.writeVarInt(this.primaryOrdinal);
        buf.writeVarInt(this.secondaryOrdinal);
        buf.writeVarInt(this.potencyTier);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
