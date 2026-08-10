package ddraig.net.entropica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import ddraig.net.entropica.entity.veil_fox.VeilFoxEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class VeilFoxCommands {

    // --- AUTO-COMPLETE SUGGESTIONS ---
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_GAMES = (context, builder) ->
            SharedSuggestionProvider.suggest(List.of(
                    "mirror_match", "hot_potato", "king_of_the_hill", "leapfrog",
                    "ambush", "blink_tag", "hide_and_seek",
                    "night_gathering", "void_fetch"
            ), builder);

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_MELODIES = (context, builder) ->
            SharedSuggestionProvider.suggest(List.of(
                    "-2", "-1", "0", "1", "2", "3", "4", "5", "6", "7", "8"
            ), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("entropica")
                .then(Commands.literal("veil_fox_commands")
                        .then(Commands.literal("behaviors")
                                // Branch 1: Explicitly handle howling to allow the melody argument
                                .then(Commands.literal("howling")
                                        .then(Commands.argument("melody_type", IntegerArgumentType.integer(-3, 8))
                                                .suggests(SUGGEST_MELODIES) // Injects the Melody Tab-Complete
                                                .executes(context -> forceBehavior(
                                                        context.getSource(),
                                                        "howling",
                                                        IntegerArgumentType.getInteger(context, "melody_type")
                                                ))
                                        )
                                        .executes(context -> forceBehavior(
                                                context.getSource(),
                                                "howling",
                                                -1
                                        ))
                                )
                                // Branch 2: Handle all other games without the melody argument
                                .then(Commands.argument("game", StringArgumentType.string())
                                        .suggests(SUGGEST_GAMES) // Injects the Game Tab-Complete
                                        .executes(context -> forceBehavior(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "game"),
                                                -1
                                        ))
                                )
                        )
                )
        );
    }

    private static int forceBehavior(CommandSourceStack source, String game, int melodyType) {
        if (source.getEntity() instanceof Player player) {
            ServerLevel level = source.getLevel();

            // Grab all Veil Foxes within a 32-block radius
            List<VeilFoxEntity> foxes = level.getEntitiesOfClass(VeilFoxEntity.class, player.getBoundingBox().inflate(32.0D));

            if (foxes.isEmpty()) {
                source.sendFailure(Component.translatable("msg.entropica.no_veil_foxes_found_nearby"));
                return 0;
            }

            for (VeilFoxEntity fox : foxes) {
                // Instantly override their AI conditions to forcefully trigger the selected pack game!
                fox.forcedPackGame = game;

                // Wake them up and unsit them if they are resting
                fox.setSleeping(false);
                fox.setOrderedToSit(false);
            }

            source.sendSuccess(() -> Component.literal("Forced behavior '" + game + "' (Melody: " + melodyType + ") on " + foxes.size() + " nearby foxes!"), true);
            return foxes.size();
        }
        return 0;
    }
}