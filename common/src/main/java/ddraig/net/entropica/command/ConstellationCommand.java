package ddraig.net.entropica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import ddraig.net.entropica.astral.Constellation;
import ddraig.net.entropica.astral.ModConstellations;
import ddraig.net.entropica.astral.PlayerAstralProgress;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class ConstellationCommand {

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_CONSTELLATIONS = (ctx, builder) -> {
        for (Constellation c : ModConstellations.getAllConstellations()) {
            builder.suggest(c.getId().toString());
            builder.suggest(c.getId().getPath());
        }
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("entropica")
                .then(Commands.literal("astral")
                        .then(Commands.literal("constellations")
                                .requires(source -> source.hasPermission(2))
                                // --- PERSONAL BRANCH (only clear is available) ---
                                .then(Commands.literal("personal")
                                        .then(Commands.literal("clear")
                                                .executes(ctx -> executeClearPersonal(ctx, Collections.singleton(ctx.getSource().getPlayerOrException())))
                                                .then(Commands.argument("targets", EntityArgument.players())
                                                        .executes(ctx -> executeClearPersonal(ctx, EntityArgument.getPlayers(ctx, "targets")))
                                                )
                                        )
                                )
                                // --- NONPERSONAL BRANCH (clear, add, remove with brigadier completion) ---
                                .then(Commands.literal("nonpersonal")
                                        .then(Commands.literal("clear")
                                                .executes(ctx -> executeClearNonPersonal(ctx, Collections.singleton(ctx.getSource().getPlayerOrException())))
                                                .then(Commands.argument("targets", EntityArgument.players())
                                                        .executes(ctx -> executeClearNonPersonal(ctx, EntityArgument.getPlayers(ctx, "targets")))
                                                )
                                        )
                                        .then(Commands.literal("add")
                                                .then(Commands.argument("constellation", StringArgumentType.string())
                                                        .suggests(SUGGEST_CONSTELLATIONS)
                                                        .executes(ctx -> executeAdd(ctx, Collections.singleton(ctx.getSource().getPlayerOrException())))
                                                        .then(Commands.argument("targets", EntityArgument.players())
                                                                .executes(ctx -> executeAdd(ctx, EntityArgument.getPlayers(ctx, "targets")))
                                                        )
                                                )
                                        )
                                        .then(Commands.literal("remove")
                                                .then(Commands.argument("constellation", StringArgumentType.string())
                                                        .suggests(SUGGEST_CONSTELLATIONS)
                                                        .executes(ctx -> executeRemove(ctx, Collections.singleton(ctx.getSource().getPlayerOrException())))
                                                        .then(Commands.argument("targets", EntityArgument.players())
                                                                .executes(ctx -> executeRemove(ctx, EntityArgument.getPlayers(ctx, "targets")))
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int executeClearPersonal(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets) {
        for (ServerPlayer player : targets) {
            PlayerAstralProgress.clearPersonal(player);
        }
        if (targets.size() == 1) {
            ServerPlayer single = targets.iterator().next();
            ctx.getSource().sendSuccess(() -> Component.literal("§6[Astral] §aCleared all personal constellations and star chart connections for §e" + single.getName().getString() + "§a."), true);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal("§6[Astral] §aCleared all personal constellations and star chart connections for §e" + targets.size() + " players§a."), true);
        }
        return targets.size();
    }

    private static int executeClearNonPersonal(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets) {
        for (ServerPlayer player : targets) {
            PlayerAstralProgress.clearDiscovered(player);
        }
        if (targets.size() == 1) {
            ServerPlayer single = targets.iterator().next();
            ctx.getSource().sendSuccess(() -> Component.literal("§6[Astral] §aCleared all discovered constellations for §e" + single.getName().getString() + "§a."), true);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal("§6[Astral] §aCleared all discovered constellations for §e" + targets.size() + " players§a."), true);
        }
        return targets.size();
    }

    private static int executeAdd(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets) {
        String input = StringArgumentType.getString(ctx, "constellation");
        Constellation constellation = resolveConstellation(input);
        if (constellation == null) {
            ctx.getSource().sendFailure(Component.literal("§c[Astral] Unknown constellation: " + input));
            return 0;
        }

        for (ServerPlayer player : targets) {
            PlayerAstralProgress.discover(player, constellation);
        }

        if (targets.size() == 1) {
            ServerPlayer single = targets.iterator().next();
            ctx.getSource().sendSuccess(() -> Component.literal("§6[Astral] §aAdded constellation §b" + Component.translatable(constellation.getUnlocalizedName()).getString() + " §ato §e" + single.getName().getString() + "§a."), true);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal("§6[Astral] §aAdded constellation §b" + Component.translatable(constellation.getUnlocalizedName()).getString() + " §ato §e" + targets.size() + " players§a."), true);
        }
        return targets.size();
    }

    private static int executeRemove(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets) {
        String input = StringArgumentType.getString(ctx, "constellation");
        Constellation constellation = resolveConstellation(input);
        if (constellation == null) {
            ctx.getSource().sendFailure(Component.literal("§c[Astral] Unknown constellation: " + input));
            return 0;
        }

        for (ServerPlayer player : targets) {
            PlayerAstralProgress.undiscover(player, constellation);
        }

        if (targets.size() == 1) {
            ServerPlayer single = targets.iterator().next();
            ctx.getSource().sendSuccess(() -> Component.literal("§6[Astral] §aRemoved constellation §b" + Component.translatable(constellation.getUnlocalizedName()).getString() + " §afrom §e" + single.getName().getString() + "§a."), true);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal("§6[Astral] §aRemoved constellation §b" + Component.translatable(constellation.getUnlocalizedName()).getString() + " §afrom §e" + targets.size() + " players§a."), true);
        }
        return targets.size();
    }

    private static Constellation resolveConstellation(String input) {
        if (input == null || input.isEmpty()) return null;
        ResourceLocation rl = ResourceLocation.tryParse(input);
        if (rl != null) {
            Optional<Constellation> opt = ModConstellations.getById(rl);
            if (opt.isPresent()) return opt.get();
            if (rl.getNamespace().equals("minecraft")) {
                ResourceLocation entropicaRl = ResourceLocation.fromNamespaceAndPath("entropica", rl.getPath());
                opt = ModConstellations.getById(entropicaRl);
                if (opt.isPresent()) return opt.get();
            }
        }
        for (Constellation c : ModConstellations.getAllConstellations()) {
            if (c.getId().getPath().equalsIgnoreCase(input) || c.getId().toString().equalsIgnoreCase(input)) {
                return c;
            }
        }
        return null;
    }
}
