package ddraig.net.entropica.network;

import ddraig.net.entropica.block.entity.AethericSynthesizerBlockEntity;
import ddraig.net.entropica.block.entity.CreativeVisFumeGeneratorBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ModNetwork {

    public static void register(RegisterPayloadHandlersEvent event) {
        // Register the Generator Scroll Payload
        event.registrar("1.0").playToServer(
                GeneratorScrollPayload.TYPE,
                GeneratorScrollPayload.STREAM_CODEC,
                ModNetwork::handleGeneratorScroll
        );

        // Register the Synthesizer UI Crafting Payload
        event.registrar("1.0").playToServer(
                TerminalCraftPayload.TYPE,
                TerminalCraftPayload.STREAM_CODEC,
                ModNetwork::handleTerminalCraft
        );
    }

    public static void handleGeneratorScroll(final GeneratorScrollPayload data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player != null && player.level().isLoaded(data.pos())) {
                BlockEntity be = player.level().getBlockEntity(data.pos());
                if (be instanceof CreativeVisFumeGeneratorBlockEntity gen) {
                    gen.cycleType(data.delta());
                }
            }
        });
    }

    public static void handleTerminalCraft(final TerminalCraftPayload data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player != null && player.level().isLoaded(data.pos())) {
                BlockEntity be = player.level().getBlockEntity(data.pos());
                if (be instanceof AethericSynthesizerBlockEntity synth) {
                    synth.instantCraft(data.isBulk());
                }
            }
        });
    }
}