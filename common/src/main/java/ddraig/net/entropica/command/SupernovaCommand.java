package ddraig.net.entropica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import ddraig.net.entropica.astral.StellarRemnantType;
import ddraig.net.entropica.astral.SupernovaEvent;
import ddraig.net.entropica.astral.SupernovaManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public class SupernovaCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("entropica")
                .then(Commands.literal("astral")
                        .then(Commands.literal("supernova")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.literal("trigger")
                                        .then(Commands.argument("starNodeId", StringArgumentType.string())
                                                .executes(ctx -> {
                                                    String nodeId = StringArgumentType.getString(ctx, "starNodeId");
                                                    ServerLevel level = ctx.getSource().getLevel();
                                                    boolean success = SupernovaManager.triggerSupernova(level, nodeId, null);
                                                    if (success) {
                                                        ctx.getSource().sendSuccess(() -> Component.literal("§6[Supernova] §aTriggered stellar instability on §e" + nodeId + "§a!"), true);
                                                        return 1;
                                                    } else {
                                                        ctx.getSource().sendFailure(Component.literal("§c[Supernova] Failed to trigger supernova. Star ID invalid, already active, or is a constellation node."));
                                                        return 0;
                                                    }
                                                })
                                                .then(Commands.argument("remnantType", StringArgumentType.word())
                                                        .executes(ctx -> {
                                                            String nodeId = StringArgumentType.getString(ctx, "starNodeId");
                                                            String remnantStr = StringArgumentType.getString(ctx, "remnantType");
                                                            ServerLevel level = ctx.getSource().getLevel();
                                                            StellarRemnantType rType = null;
                                                            try {
                                                                rType = StellarRemnantType.valueOf(remnantStr.toUpperCase());
                                                            } catch (Exception e) {
                                                                ctx.getSource().sendFailure(Component.literal("§c[Supernova] Unknown remnant type: " + remnantStr));
                                                                return 0;
                                                            }
                                                            boolean success = SupernovaManager.triggerSupernova(level, nodeId, rType);
                                                            if (success) {
                                                                ctx.getSource().sendSuccess(() -> Component.literal("§6[Supernova] §aTriggered stellar collapse into §b" + remnantStr + "§a on §e" + nodeId + "§a!"), true);
                                                                return 1;
                                                            } else {
                                                                ctx.getSource().sendFailure(Component.literal("§c[Supernova] Failed to trigger supernova on star ID."));
                                                                return 0;
                                                            }
                                                        })
                                                )
                                        )
                                )
                                .then(Commands.literal("list")
                                        .executes(ctx -> {
                                            var events = SupernovaManager.getAllEvents();
                                            if (events.isEmpty()) {
                                                ctx.getSource().sendSuccess(() -> Component.literal("§6[Supernova] §7No active stellar events or supernova remnants in the firmament."), false);
                                                return 1;
                                            }
                                            ctx.getSource().sendSuccess(() -> Component.literal("§6[Supernova Active Events & Remnants] (" + events.size() + "):"), false);
                                            for (SupernovaEvent e : events) {
                                                ctx.getSource().sendSuccess(() -> Component.literal("§e • " + e.starNodeId() + "§7 - Phase: §b" + e.phase().name() + "§7 | Remnant: §d" + e.remnantType().getTitle() + "§7 | Essence: " + e.remnantEssence().getFormattedName()), false);
                                            }
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("clear")
                                        .executes(ctx -> {
                                            ServerLevel level = ctx.getSource().getLevel();
                                            SupernovaManager.clearAllEvents(level);
                                            ctx.getSource().sendSuccess(() -> Component.literal("§6[Supernova] §aCleared all active supernovae and restored original stellar firmament."), true);
                                            return 1;
                                        })
                                )
                        )
                )
        );
    }
}
