package ddraig.net.entropica.command;

import com.mojang.brigadier.CommandDispatcher;
import ddraig.net.entropica.Entropica;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = Entropica.MODID)
public class ModCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        // Register all individual commands here as you make them!
        EssenceNodeCommands.register(dispatcher);

        // --- Register the new Grot Summon Command ---
        GrotSummonCommands.register(dispatcher);
    }
}