package ddraig.net.entropica.network;

import ddraig.net.entropica.Entropica;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * C2S payload sent from {@code AutocastPedestalScreen} to update cast interval, aim pitch,
 * redstone mode, or clear linked containers.
 */
public record UpdateAutocastPedestalPayload(
        BlockPos pos,
        int intervalTicks,
        float aimPitch,
        int redstoneMode,
        boolean clearLinks
) implements CustomPacketPayload {

    public static final Type<UpdateAutocastPedestalPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "update_autocast_pedestal")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateAutocastPedestalPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, UpdateAutocastPedestalPayload::pos,
            ByteBufCodecs.VAR_INT, UpdateAutocastPedestalPayload::intervalTicks,
            ByteBufCodecs.FLOAT, UpdateAutocastPedestalPayload::aimPitch,
            ByteBufCodecs.VAR_INT, UpdateAutocastPedestalPayload::redstoneMode,
            ByteBufCodecs.BOOL, UpdateAutocastPedestalPayload::clearLinks,
            UpdateAutocastPedestalPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
