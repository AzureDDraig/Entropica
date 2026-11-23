package ddraig.net.entropica.network;

import ddraig.net.entropica.Entropica;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sent from client to server when the player reads a star entry, toggles a
 * Codex bookmark, or archives a Scholar's Journal lore chapter.
 */
public record UpdateCodexStatePayload(int action, String targetId) implements CustomPacketPayload {

    public static final int ACTION_READ_STAR = 0;
    public static final int ACTION_TOGGLE_BOOKMARK = 1;
    public static final int ACTION_ARCHIVE_LORE = 2;

    public static final Type<UpdateCodexStatePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "update_codex_state")
    );

    public static final StreamCodec<ByteBuf, UpdateCodexStatePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, UpdateCodexStatePayload::action,
            ByteBufCodecs.STRING_UTF8, UpdateCodexStatePayload::targetId,
            UpdateCodexStatePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
