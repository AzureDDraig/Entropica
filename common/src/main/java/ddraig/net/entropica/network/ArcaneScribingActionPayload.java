package ddraig.net.entropica.network;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/**
 * Client-to-server action packet for the {@link ddraig.net.entropica.block.ArcaneScribingTableBlock}.
 *
 * <ul>
 *     <li>{@code ACTION_INSCRIBE (0)}: Inscribes {@code draftAssembly} onto Blank Parchment + Scribing Ink.</li>
 *     <li>{@code ACTION_DUPLICATE (1)}: Duplicates a researched Spell Parchment in Slot 2.</li>
 *     <li>{@code ACTION_RECYCLE_GEM (2)}: Scrubs a researched non-Unique Spellgem in Slot 2 into a Blank Spellgem.</li>
 *     <li>{@code ACTION_SAFE_ALIGN (3)}: Safely aligns or extracts a Spellgem on an Apparatus/Weapon in Slot 2.</li>
 * </ul>
 */
public record ArcaneScribingActionPayload(
        BlockPos pos,
        int action,
        int socketIndex,
        Optional<SpellAssembly> draftAssembly
) implements CustomPacketPayload {

    public static final int ACTION_INSCRIBE = 0;
    public static final int ACTION_DUPLICATE = 1;
    public static final int ACTION_RECYCLE_GEM = 2;
    public static final int ACTION_SAFE_ALIGN = 3;

    public static final Type<ArcaneScribingActionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "arcane_scribing_action")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ArcaneScribingActionPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ArcaneScribingActionPayload::pos,
            ByteBufCodecs.VAR_INT, ArcaneScribingActionPayload::action,
            ByteBufCodecs.VAR_INT, ArcaneScribingActionPayload::socketIndex,
            ByteBufCodecs.optional(SpellAssembly.STREAM_CODEC), ArcaneScribingActionPayload::draftAssembly,
            ArcaneScribingActionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
