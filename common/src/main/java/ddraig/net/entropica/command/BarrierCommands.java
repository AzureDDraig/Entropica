package ddraig.net.entropica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import ddraig.net.entropica.entity.forcefield.ForcefieldBarrierEntity;
import ddraig.net.entropica.forcefield.ApexPredatorTheme;
import ddraig.net.entropica.forcefield.BarrierFieldManager;
import ddraig.net.entropica.forcefield.BarrierFilterMode;
import ddraig.net.entropica.forcefield.BarrierShape;
import ddraig.net.entropica.registry.ModEntityTypes;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class BarrierCommands {

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_SHAPES = (ctx, builder) ->
            SharedSuggestionProvider.suggest(
                    Arrays.stream(BarrierShape.values()).map(s -> s.name().toLowerCase(Locale.ROOT)),
                    builder
            );

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_FILTERS = (ctx, builder) ->
            SharedSuggestionProvider.suggest(
                    Arrays.stream(BarrierFilterMode.values()).map(f -> f.name().toLowerCase(Locale.ROOT)),
                    builder
            );

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_THEMES = (ctx, builder) ->
            SharedSuggestionProvider.suggest(
                    Arrays.stream(ApexPredatorTheme.values()).map(t -> t.name().toLowerCase(Locale.ROOT)),
                    builder
            );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("entropica")
                .then(Commands.literal("barrier")
                        .requires(source -> source.hasPermission(2))
                        // --- /entropica barrier spawn <shape> [dim1] [dim2] [filter] ---
                        .then(Commands.literal("spawn")
                                .then(Commands.argument("shape", StringArgumentType.word())
                                        .suggests(SUGGEST_SHAPES)
                                        .executes(ctx -> spawnBarrier(ctx.getSource(), StringArgumentType.getString(ctx, "shape"), 4.0F, 3.5F, "ALL_ENTITIES"))
                                        .then(Commands.argument("dim1", FloatArgumentType.floatArg(0.5F, 128.0F))
                                                .executes(ctx -> spawnBarrier(ctx.getSource(), StringArgumentType.getString(ctx, "shape"), FloatArgumentType.getFloat(ctx, "dim1"), 3.5F, "ALL_ENTITIES"))
                                                .then(Commands.argument("dim2", FloatArgumentType.floatArg(0.5F, 128.0F))
                                                        .executes(ctx -> spawnBarrier(ctx.getSource(), StringArgumentType.getString(ctx, "shape"), FloatArgumentType.getFloat(ctx, "dim1"), FloatArgumentType.getFloat(ctx, "dim2"), "ALL_ENTITIES"))
                                                        .then(Commands.argument("filter", StringArgumentType.word())
                                                                .suggests(SUGGEST_FILTERS)
                                                                .executes(ctx -> spawnBarrier(ctx.getSource(), StringArgumentType.getString(ctx, "shape"), FloatArgumentType.getFloat(ctx, "dim1"), FloatArgumentType.getFloat(ctx, "dim2"), StringArgumentType.getString(ctx, "filter")))
                                                        )
                                                )
                                        )
                                )
                        )
                        // --- /entropica barrier spawn_arena <theme> [radius] ---
                        .then(Commands.literal("spawn_arena")
                                .then(Commands.argument("theme", StringArgumentType.word())
                                        .suggests(SUGGEST_THEMES)
                                        .executes(ctx -> spawnArena(ctx.getSource(), StringArgumentType.getString(ctx, "theme"), 32.0F))
                                        .then(Commands.argument("radius", FloatArgumentType.floatArg(4.0F, 128.0F))
                                                .executes(ctx -> spawnArena(ctx.getSource(), StringArgumentType.getString(ctx, "theme"), FloatArgumentType.getFloat(ctx, "radius")))
                                        )
                                )
                        )
                        // --- /entropica barrier list [radius] ---
                        .then(Commands.literal("list")
                                .executes(ctx -> listBarriers(ctx.getSource(), 64.0))
                                .then(Commands.argument("radius", DoubleArgumentType.doubleArg(1.0, 512.0))
                                        .executes(ctx -> listBarriers(ctx.getSource(), DoubleArgumentType.getDouble(ctx, "radius")))
                                )
                        )
                        // --- /entropica barrier clear [radius] ---
                        .then(Commands.literal("clear")
                                .executes(ctx -> clearBarriers(ctx.getSource(), 64.0))
                                .then(Commands.argument("radius", DoubleArgumentType.doubleArg(1.0, 512.0))
                                        .executes(ctx -> clearBarriers(ctx.getSource(), DoubleArgumentType.getDouble(ctx, "radius")))
                                )
                        )
                        // --- /entropica barrier info ---
                        .then(Commands.literal("info")
                                .executes(ctx -> inspectTargetBarrier(ctx.getSource()))
                        )
                        // --- /entropica barrier test_bounce [elasticity] ---
                        .then(Commands.literal("test_bounce")
                                .executes(ctx -> testBounce(ctx.getSource(), 1.0F))
                                .then(Commands.argument("elasticity", FloatArgumentType.floatArg(0.1F, 3.0F))
                                        .executes(ctx -> testBounce(ctx.getSource(), FloatArgumentType.getFloat(ctx, "elasticity")))
                                )
                        )
                )
        );
    }

    private static int spawnBarrier(CommandSourceStack source, String shapeStr, float dim1, float dim2, String filterStr) {
        ServerPlayer player = source.getPlayer();
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();

        BarrierShape shape;
        try {
            shape = BarrierShape.valueOf(shapeStr.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cUnknown shape: " + shapeStr + ". Valid: PLANAR_QUAD, CIRCULAR_DISC, HEMISPHERICAL_DOME, SPHERICAL_BUBBLE, CYLINDER"));
            return 0;
        }

        BarrierFilterMode filter;
        try {
            filter = BarrierFilterMode.valueOf(filterStr.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            filter = BarrierFilterMode.ALL_ENTITIES;
        }
        final BarrierFilterMode finalFilter = filter;

        ForcefieldBarrierEntity barrier = new ForcefieldBarrierEntity(ModEntityTypes.FORCEFIELD_BARRIER.get(), level);
        barrier.setPos(pos.x, pos.y, pos.z);
        if (player != null) {
            barrier.setYRot(player.getYRot());
            barrier.setXRot(shape == BarrierShape.PLANAR_QUAD ? 0.0F : player.getXRot());
            barrier.setOwnerUUID(player.getUUID());
            barrier.setOwnerName(player.getName().getString());
        }

        barrier.setShape(shape);
        barrier.setFilterMode(finalFilter);

        if (shape == BarrierShape.PLANAR_QUAD || shape == BarrierShape.CONVEX_POLYGON) {
            barrier.setWidth(dim1);
            barrier.setHeight(dim2);
        } else if (shape == BarrierShape.CYLINDER) {
            barrier.setRadius(dim1);
            barrier.setHeight(dim2);
        } else {
            barrier.setRadius(dim1);
        }

        level.addFreshEntity(barrier);
        source.sendSuccess(() -> Component.literal("§d[Barrier Debug] §aSpawned §b" + shape.getDisplayName() + " §f(" + finalFilter.getDisplayName() + ") at " + barrier.blockPosition().toShortString()), true);
        return 1;
    }

    private static int spawnArena(CommandSourceStack source, String themeStr, float radius) {
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();

        ApexPredatorTheme theme;
        try {
            theme = ApexPredatorTheme.valueOf(themeStr.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            theme = ApexPredatorTheme.STANDARD;
        }
        final ApexPredatorTheme finalTheme = theme;

        ForcefieldBarrierEntity arena = new ForcefieldBarrierEntity(ModEntityTypes.FORCEFIELD_BARRIER.get(), level);
        arena.setPos(pos.x, pos.y, pos.z);
        arena.setShape(BarrierShape.HEMISPHERICAL_DOME);
        arena.setRadius(radius);
        arena.setFilterMode(BarrierFilterMode.ALL_ENTITIES);
        arena.setPredatorTheme(finalTheme);
        arena.setBossEncounter(true);
        arena.setBounceElasticity(1.4F);

        level.addFreshEntity(arena);
        source.sendSuccess(() -> Component.literal("§4[Apex Arena] §aErected §c" + finalTheme.getDisplayName() + " §6firmament dome (Radius: " + radius + "m) at " + arena.blockPosition().toShortString()), true);
        return 1;
    }

    private static int listBarriers(CommandSourceStack source, double radius) {
        Vec3 pos = source.getPosition();
        List<ForcefieldBarrierEntity> barriers = BarrierFieldManager.getNearbyBarriers(source.getLevel(), pos, radius);

        if (barriers.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7[Barrier Debug] No active barriers found within " + radius + "m."), false);
            return 0;
        }

        source.sendSuccess(() -> Component.literal("§d[Barrier Debug] Active Barriers within " + radius + "m (" + barriers.size() + "):"), false);
        for (ForcefieldBarrierEntity b : barriers) {
            String owner = b.getOwnerName().isEmpty() ? "Unowned/Boss" : b.getOwnerName();
            source.sendSuccess(() -> Component.literal("  §f- §b" + b.getShape().getDisplayName() + " §7| §e" + b.getFilterMode().getDisplayName() + " §7| Owner: §a" + owner + " §7at " + b.blockPosition().toShortString()), false);
        }
        return barriers.size();
    }

    private static int clearBarriers(CommandSourceStack source, double radius) {
        int count = BarrierFieldManager.clearBarriers(source.getLevel(), source.getPosition(), radius);
        source.sendSuccess(() -> Component.literal("§d[Barrier Debug] §aDispelled §e" + count + " §abarrier entities within " + radius + "m."), true);
        return count;
    }

    private static int inspectTargetBarrier(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 reach = eye.add(look.scale(16.0));

        List<ForcefieldBarrierEntity> nearby = BarrierFieldManager.getNearbyBarriers(source.getLevel(), eye, 16.0);
        ForcefieldBarrierEntity targeted = null;
        double bestDist = Double.MAX_VALUE;

        for (ForcefieldBarrierEntity b : nearby) {
            if (b.getBoundingBox().clip(eye, reach).isPresent()) {
                double dist = b.distanceToSqr(player);
                if (dist < bestDist) {
                    bestDist = dist;
                    targeted = b;
                }
            }
        }

        if (targeted == null) {
            source.sendFailure(Component.literal("§c[Barrier Debug] No barrier in crosshair within 16 blocks."));
            return 0;
        }

        ForcefieldBarrierEntity b = targeted;
        source.sendSuccess(() -> Component.literal(
                "§d[Barrier Telemetry] §b" + b.getShape().getDisplayName() + "§7:\n" +
                "  §fEntity ID: §7" + b.getId() + " §8(" + b.getUUID() + ")\n" +
                "  §fFilter: §e" + b.getFilterMode().getDisplayName() + "\n" +
                "  §fTheme: §a" + b.getPredatorTheme().getDisplayName() + "\n" +
                "  §fDimensions: §7W=" + b.getWidth() + ", H=" + b.getHeight() + ", R=" + b.getRadius() + "\n" +
                "  §fElasticity: §b" + b.getBounceElasticity() + "x\n" +
                "  §fCreator: §6" + (b.getOwnerName().isEmpty() ? "None" : b.getOwnerName()) + "\n" +
                "  §fBoss Arena: " + (b.isBossEncounter() ? "§aYES (Locked)" : "§7NO")
        ), false);
        return 1;
    }

    private static int testBounce(CommandSourceStack source, float elasticity) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        Vec3 look = player.getViewVector(1.0F);
        Vec3 normal = look.scale(-1.0).normalize();
        Vec3 reflected = normal.scale(1.2 * elasticity);

        player.setDeltaMovement(reflected);
        player.hasImpulse = true;

        source.sendSuccess(() -> Component.literal("§d[Barrier Debug] §aSimulated bounce rebound (Elasticity: " + elasticity + "x)."), true);
        return 1;
    }
}
