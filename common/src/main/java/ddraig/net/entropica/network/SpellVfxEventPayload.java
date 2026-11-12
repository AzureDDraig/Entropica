package ddraig.net.entropica.network;

import ddraig.net.entropica.Entropica;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Compact S2C packet triggering custom 3D procedural spell geometry in {@code SpellWorldVfxRenderer}.
 */
public record SpellVfxEventPayload(
        byte vfxType,
        float sx,
        float sy,
        float sz,
        float ex,
        float ey,
        float ez,
        int primaryRgb,
        int accentRgb,
        int essenceOrdinal,
        float radiusOrScale,
        int durationTicks,
        int tier
) implements CustomPacketPayload {

    public static final byte VFX_BEAM = 0;
    public static final byte VFX_CHAIN_ARC = 1;
    public static final byte VFX_RADIAL_NOVA = 2;
    public static final byte VFX_PROJECTILE_IMPACT = 3;
    public static final byte VFX_ARCANE_SEAL = 4;
    public static final byte VFX_CRESCENT_SLASH = 5;
    public static final byte VFX_VORTEX_CONE = 6;
    public static final byte VFX_SKYFALL_PILLAR = 7;
    public static final byte VFX_BLINK_RIFT = 8;
    public static final byte VFX_WARD_SHELL = 9;
    public static final byte VFX_SUMMON_PILLAR = 10;

    public static final Type<SpellVfxEventPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spell_vfx_event"));

    public static final StreamCodec<FriendlyByteBuf, SpellVfxEventPayload> STREAM_CODEC =
            CustomPacketPayload.codec(SpellVfxEventPayload::write, SpellVfxEventPayload::new);

    public SpellVfxEventPayload(FriendlyByteBuf buf) {
        this(
                buf.readByte(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readInt(),
                buf.readInt(),
                buf.readVarInt(),
                buf.readFloat(),
                buf.readVarInt(),
                buf.readVarInt()
        );
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeByte(vfxType);
        buf.writeFloat(sx);
        buf.writeFloat(sy);
        buf.writeFloat(sz);
        buf.writeFloat(ex);
        buf.writeFloat(ey);
        buf.writeFloat(ez);
        buf.writeInt(primaryRgb);
        buf.writeInt(accentRgb);
        buf.writeVarInt(essenceOrdinal);
        buf.writeFloat(radiusOrScale);
        buf.writeVarInt(durationTicks);
        buf.writeVarInt(tier);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
