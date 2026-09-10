package ddraig.net.entropica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import ddraig.net.entropica.block.entity.GravityCenterBlockEntity;
import ddraig.net.entropica.gravity.GravityApi;
import ddraig.net.entropica.gravity.GravityField;
import ddraig.net.entropica.gravity.GravityFieldManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.Locale;

public class GravityCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("entropica")
                .then(Commands.literal("gravity")
                        .requires(source -> source.hasPermission(2))
                        // --- /entropica gravity get [player] ---
                        .then(Commands.literal("get")
                                .executes(ctx -> reportGravity(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(ctx -> reportGravity(ctx.getSource(), EntityArgument.getPlayer(ctx, "target")))
                                )
                        )
                        // --- /entropica gravity set <value> [player] ---
                        .then(Commands.literal("set")
                                .then(Commands.argument("value", DoubleArgumentType.doubleArg(-1.0, 5.0))
                                        .executes(ctx -> setGravity(ctx.getSource(), ctx.getSource().getPlayerOrException(), DoubleArgumentType.getDouble(ctx, "value")))
                                        .then(Commands.argument("target", EntityArgument.player())
                                                .executes(ctx -> setGravity(ctx.getSource(), EntityArgument.getPlayer(ctx, "target"), DoubleArgumentType.getDouble(ctx, "value")))
                                        )
                                )
                        )
                        // --- /entropica gravity reset [player] ---
                        .then(Commands.literal("reset")
                                .executes(ctx -> resetGravity(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(ctx -> resetGravity(ctx.getSource(), EntityArgument.getPlayer(ctx, "target")))
                                )
                        )
                        // --- /entropica gravity invert [player] ---
                        .then(Commands.literal("invert")
                                .executes(ctx -> toggleInvert(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(ctx -> toggleInvert(ctx.getSource(), EntityArgument.getPlayer(ctx, "target")))
                                )
                        )
                        // --- /entropica gravity field spawn <mode> [radius] [durationTicks] ---
                        .then(Commands.literal("field")
                                .then(Commands.literal("spawn")
                                        .then(Commands.argument("mode", StringArgumentType.word())
                                                .executes(ctx -> spawnField(ctx.getSource(), StringArgumentType.getString(ctx, "mode"), 12.0, 600))
                                                .then(Commands.argument("radius", DoubleArgumentType.doubleArg(1.0, 64.0))
                                                        .executes(ctx -> spawnField(ctx.getSource(), StringArgumentType.getString(ctx, "mode"), DoubleArgumentType.getDouble(ctx, "radius"), 600))
                                                        .then(Commands.argument("durationTicks", IntegerArgumentType.integer(-1, 72000))
                                                                .executes(ctx -> spawnField(ctx.getSource(), StringArgumentType.getString(ctx, "mode"), DoubleArgumentType.getDouble(ctx, "radius"), IntegerArgumentType.getInteger(ctx, "durationTicks")))
                                                        )
                                                )
                                        )
                                )
                                .then(Commands.literal("clear")
                                        .executes(ctx -> clearFields(ctx.getSource()))
                                )
                                .then(Commands.literal("list")
                                        .executes(ctx -> listFields(ctx.getSource()))
                                )
                        )
                )
        );
    }

    private static int reportGravity(CommandSourceStack source, ServerPlayer player) {
        double eff = GravityApi.getEffectiveGravity(player);
        boolean inverted = GravityApi.isInverted(player);
        boolean soles = GravityApi.hasGravitonSoles(player);
        boolean anchored = GravityApi.isAnchored(player);

        GravityCenterBlockEntity center = GravityCenterBlockEntity.getAffectingCenter(player.level(), player.position());
        String centerInfo = center != null ? "§aActive (Core at " + center.getBlockPos().toShortString() + ")" : "§7None";

        source.sendSuccess(() -> Component.literal(
                "§6[Gravity Debug] §e" + player.getName().getString() + "§7:\n" +
                "  §fEffective Gravity: §b" + String.format(Locale.ROOT, "%.4f", eff) + "\n" +
                "  §fInverted: " + (inverted ? "§aYES" : "§cNO") + "\n" +
                "  §fGraviton Soles: " + (soles ? "§aEQUIPPED" : "§cNONE") + "\n" +
                "  §fInertial Anchor: " + (anchored ? "§aIMMUNE" : "§7VULNERABLE") + "\n" +
                "  §fGravity Center: " + centerInfo
        ), false);
        return 1;
    }

    private static int setGravity(CommandSourceStack source, ServerPlayer player, double value) {
        GravityApi.setGravity(player, value);
        source.sendSuccess(() -> Component.literal("§6[Gravity Debug] §aSet gravity of §e" + player.getName().getString() + " §ato §b" + value), true);
        return 1;
    }

    private static int resetGravity(CommandSourceStack source, ServerPlayer player) {
        GravityApi.resetGravity(player);
        GravityApi.SOLES_INVERTED_ENTITIES.remove(player.getUUID());
        source.sendSuccess(() -> Component.literal("§6[Gravity Debug] §aReset gravity of §e" + player.getName().getString() + " §ato vanilla default (0.08)"), true);
        return 1;
    }

    private static int toggleInvert(CommandSourceStack source, ServerPlayer player) {
        boolean currentlyInverted = GravityApi.isInverted(player);
        if (currentlyInverted) {
            GravityApi.SOLES_INVERTED_ENTITIES.remove(player.getUUID());
            GravityApi.resetGravity(player);
            source.sendSuccess(() -> Component.literal("§6[Gravity Debug] §aNormalized gravity for §e" + player.getName().getString()), true);
        } else {
            GravityApi.SOLES_INVERTED_ENTITIES.add(player.getUUID());
            GravityApi.setGravity(player, GravityApi.INVERTED_GRAVITY);
            source.sendSuccess(() -> Component.literal("§6[Gravity Debug] §bInverted gravity for §e" + player.getName().getString() + " (-0.08)"), true);
        }
        return 1;
    }

    private static int spawnField(CommandSourceStack source, String modeStr, double radius, int duration) {
        GravityField.Mode mode;
        try {
            mode = GravityField.Mode.valueOf(modeStr.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cUnknown mode: " + modeStr + ". Valid: ZERO_G, LUNAR, INVERSION, SINGULARITY, GRAV_LIFT, REPULSOR, TIDAL_PULSE"));
            return 0;
        }

        ServerLevel level = source.getLevel();
        BlockPos pos = BlockPos.containing(source.getPosition());
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("entropica", "cmd_field_" + System.currentTimeMillis());

        GravityField field = new GravityField(id, level.dimension(), pos, radius, mode, duration);
        GravityFieldManager.registerField(field);

        source.sendSuccess(() -> Component.literal("§6[Gravity Debug] §aSpawned §b" + mode.getDisplayName() + " §afield (Radius: " + radius + ", Duration: " + duration + "t) at " + pos.toShortString()), true);
        return 1;
    }

    private static int clearFields(CommandSourceStack source) {
        var fields = GravityFieldManager.getAllFields();
        int count = fields.size();
        for (GravityField f : fields) {
            GravityFieldManager.unregisterField(f.getId());
        }
        source.sendSuccess(() -> Component.literal("§6[Gravity Debug] §aCleared all §e" + count + " §aspatial gravity fields."), true);
        return count;
    }

    private static int listFields(CommandSourceStack source) {
        Collection<GravityField> fields = GravityFieldManager.getAllFields();
        if (fields.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7[Gravity Debug] No active spatial fields in memory."), false);
            return 0;
        }

        source.sendSuccess(() -> Component.literal("§6[Gravity Debug] Active Spatial Fields (" + fields.size() + "):"), false);
        for (GravityField f : fields) {
            source.sendSuccess(() -> Component.literal("  §f- §b" + f.getMode().getDisplayName() + " §7at " + f.getCenterPos().toShortString() + " §8(R: " + f.getRadius() + ", Age: " + f.getAgeTicks() + "t)"), false);
        }
        return fields.size();
    }
}
