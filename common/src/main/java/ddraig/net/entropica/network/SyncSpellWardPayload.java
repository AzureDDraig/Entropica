package ddraig.net.entropica.network;

import ddraig.net.entropica.Entropica;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * S2C packet synchronizing persistent entity-attached 3D Ward & Aegis barrier shells,
 * remaining shield health ratio, dynamic hit-flare opacity surges, directional impact
 * shockwaves, and shatter bursts.
 * <ul>
 *   <li>{@code remainingTicks > 0}: active/refreshed barrier (with optional {@code hitFlare > 0})</li>
 *   <li>{@code remainingTicks == 0}: graceful natural expiration fade-out</li>
 *   <li>{@code remainingTicks < 0}: barrier shattered by an incoming blow (triggers high-opacity shatter burst!)</li>
 * </ul>
 */
public record SyncSpellWardPayload(
        int entityId,
        int remainingTicks,
        int primaryOrdinal,
        int secondaryOrdinal,
        int potencyTier,
        float shieldRatio,
        float hitFlare,
        float hitDirX,
        float hitDirY,
        float hitDirZ
) implements CustomPacketPayload {

    public static final Type<SyncSpellWardPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "sync_spell_ward"));

    public static final StreamCodec<FriendlyByteBuf, SyncSpellWardPayload> STREAM_CODEC =
            CustomPacketPayload.codec(SyncSpellWardPayload::write, SyncSpellWardPayload::new);

    public SyncSpellWardPayload(FriendlyByteBuf buf) {
        this(
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat()
        );
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.entityId);
        buf.writeVarInt(this.remainingTicks);
        buf.writeVarInt(this.primaryOrdinal);
        buf.writeVarInt(this.secondaryOrdinal);
        buf.writeVarInt(this.potencyTier);
        buf.writeFloat(this.shieldRatio);
        buf.writeFloat(this.hitFlare);
        buf.writeFloat(this.hitDirX);
        buf.writeFloat(this.hitDirY);
        buf.writeFloat(this.hitDirZ);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
