package ddraig.net.entropica.command;

import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.commands.CommandSourceStack;

public class ModCommands {

    public static void register() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registryAccess, selection) -> {
            EssenceNodeCommands.register(dispatcher);
            GrotSummonCommands.register(dispatcher);
            VeilFoxCommands.register(dispatcher);
            SupernovaCommand.register(dispatcher);
        });
    }
}