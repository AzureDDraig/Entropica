package ddraig.net.entropica.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record WeaponNamingPayload(String name) implements CustomPacketPayload {

    public static final Type<WeaponNamingPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("entropica", "name_weapon"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WeaponNamingPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, WeaponNamingPayload::name,
            WeaponNamingPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}