package ddraig.net.entropica.network;

import ddraig.net.entropica.Entropica;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Synchronizes a player's Entropic Codex state (unlocked research nodes,
 * shining constellation stars, mob bestiary progress, bookmarks, and
 * archived Scholar's Journal lore chapters) from server to client.
 */
public record SyncCodexDataPayload(
        int researchTierLevel,
        List<String> unlockedNodes,
        List<String> shiningStars,
        List<String> observedMobs,
        List<String> fullyUnlockedMobs,
        List<String> bookmarkedTopics,
        List<String> archivedLoreEntries
) implements CustomPacketPayload {

    public static final Type<SyncCodexDataPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "sync_codex_data")
    );

    private static final StreamCodec<ByteBuf, List<String>> STRING_LIST_CODEC =
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list());

    public static final StreamCodec<ByteBuf, SyncCodexDataPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                ByteBufCodecs.VAR_INT.encode(buf, payload.researchTierLevel());
                STRING_LIST_CODEC.encode(buf, payload.unlockedNodes());
                STRING_LIST_CODEC.encode(buf, payload.shiningStars());
                STRING_LIST_CODEC.encode(buf, payload.observedMobs());
                STRING_LIST_CODEC.encode(buf, payload.fullyUnlockedMobs());
                STRING_LIST_CODEC.encode(buf, payload.bookmarkedTopics());
                STRING_LIST_CODEC.encode(buf, payload.archivedLoreEntries());
            },
            buf -> new SyncCodexDataPayload(
                    ByteBufCodecs.VAR_INT.decode(buf),
                    STRING_LIST_CODEC.decode(buf),
                    STRING_LIST_CODEC.decode(buf),
                    STRING_LIST_CODEC.decode(buf),
                    STRING_LIST_CODEC.decode(buf),
                    STRING_LIST_CODEC.decode(buf),
                    STRING_LIST_CODEC.decode(buf)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
