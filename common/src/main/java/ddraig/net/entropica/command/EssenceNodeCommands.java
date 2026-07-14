package ddraig.net.entropica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.config.EntropicaConfig;
import ddraig.net.entropica.entity.EssenceNodeEntity;
import ddraig.net.entropica.registry.ModEntityTypes;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.List;

public class EssenceNodeCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("entropica")
                .requires(source -> source.hasPermission(2)) // Requires OP / Cheats enabled

                // ==========================================
                // SPAWN ESSENCE NODE COMMAND
                // ==========================================
                .then(Commands.literal("spawnEssenceNode")
                        // Argument 1: Essence Type (e.g., UMBRAL, RADIANT)
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(EssenceType.values()).map(Enum::name), builder))
                                .executes(context -> spawnNode(context.getSource(),
                                        StringArgumentType.getString(context, "type"),
                                        -1, // -1 tells the method to use a random config capacity
                                        true)) // Defaults to Artificial = true so they don't despawn naturally

                                // Argument 2: Capacity (Optional)
                                .then(Commands.argument("capacity", IntegerArgumentType.integer(1))
                                        .executes(context -> spawnNode(context.getSource(),
                                                StringArgumentType.getString(context, "type"),
                                                IntegerArgumentType.getInteger(context, "capacity"),
                                                true))

                                        // Argument 3: Is Artificial (Optional)
                                        .then(Commands.argument("artificial", BoolArgumentType.bool())
                                                .executes(context -> spawnNode(context.getSource(),
                                                        StringArgumentType.getString(context, "type"),
                                                        IntegerArgumentType.getInteger(context, "capacity"),
                                                        BoolArgumentType.getBool(context, "artificial")))
                                        )
                                )
                        )
                )

                // ==========================================
                // REMOVE ESSENCE NODES COMMAND
                // ==========================================
                .then(Commands.literal("removeEssenceNodes")
                        .then(Commands.argument("radius", DoubleArgumentType.doubleArg(0.1))
                                .executes(context -> removeNodes(context.getSource(), DoubleArgumentType.getDouble(context, "radius")))
                        )
                )

                // ==========================================
                // MODIFY CLOSEST ESSENCE NODE COMMAND
                // ==========================================
                .then(Commands.literal("modifyClosestNode")
                        .then(Commands.argument("radius", DoubleArgumentType.doubleArg(0.1))
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(EssenceType.values()).map(Enum::name), builder))
                                        .executes(context -> modifyClosestNode(context.getSource(),
                                                DoubleArgumentType.getDouble(context, "radius"),
                                                StringArgumentType.getString(context, "type"),
                                                -1,
                                                true))
                                        .then(Commands.argument("capacity", IntegerArgumentType.integer(1))
                                                .executes(context -> modifyClosestNode(context.getSource(),
                                                        DoubleArgumentType.getDouble(context, "radius"),
                                                        StringArgumentType.getString(context, "type"),
                                                        IntegerArgumentType.getInteger(context, "capacity"),
                                                        true))
                                                .then(Commands.argument("artificial", BoolArgumentType.bool())
                                                        .executes(context -> modifyClosestNode(context.getSource(),
                                                                DoubleArgumentType.getDouble(context, "radius"),
                                                                StringArgumentType.getString(context, "type"),
                                                                IntegerArgumentType.getInteger(context, "capacity"),
                                                                BoolArgumentType.getBool(context, "artificial")))
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int spawnNode(CommandSourceStack source, String typeString, int capacity, boolean isArtificial) {
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();

        // 1. Validate the Essence Type
        EssenceType type;
        try {
            type = EssenceType.valueOf(typeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("§cInvalid Essence Type: " + typeString));
            return 0; // 0 indicates command failure
        }

        // 2. Create the Entity
        EssenceNodeEntity node = ModEntityTypes.ESSENCE_NODE.get().create(level, EntitySpawnReason.COMMAND);
        if (node == null) {
            source.sendFailure(Component.literal("§cFailed to create Essence Node entity."));
            return 0;
        }

        // 3. Set Entity Properties
        node.setPos(pos.x, pos.y, pos.z);
        node.setEssenceType(type);
        node.setArtificial(isArtificial);

        // Generate random capacity if no capacity argument was provided (-1)
        if (capacity == -1) {
            int min = EntropicaConfig.NODE_MIN_CAPACITY.get();
            int max = EntropicaConfig.NODE_MAX_CAPACITY.get();
            capacity = min + level.getRandom().nextInt(Math.max(1, max - min + 1));
        }

        node.setMaxCapacity(capacity);
        node.setCapacity(capacity);

        // 4. Spawn into the world
        level.addFreshEntity(node);

        // 5. Send Success Message (Use a final variable for the lambda)
        final int finalCapacity = capacity;
        source.sendSuccess(() -> Component.literal(
                String.format("§aSpawned %s Essence Node (Capacity: %d, Artificial: %b) at [%.2f, %.2f, %.2f]",
                        type.getDisplayName(), finalCapacity, isArtificial, pos.x, pos.y, pos.z)
        ), true);

        return 1; // 1 indicates command success
    }

    private static int removeNodes(CommandSourceStack source, double radius) {
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();

        // Create a bounding box around the executor based on the radius
        AABB boundingBox = new AABB(
                pos.x - radius, pos.y - radius, pos.z - radius,
                pos.x + radius, pos.y + radius, pos.z + radius
        );

        // Find all essence nodes within that box
        List<EssenceNodeEntity> nodes = level.getEntitiesOfClass(EssenceNodeEntity.class, boundingBox);
        int count = 0;

        for (EssenceNodeEntity node : nodes) {
            node.discard(); // Safely removes the entity from the world
            count++;
        }

        final int finalCount = count;
        source.sendSuccess(() -> Component.literal(
                "§aRemoved " + finalCount + " Essence Node(s) within a radius of " + radius + " blocks."
        ), true);

        return finalCount; // Returns the number of nodes removed
    }

    private static int modifyClosestNode(CommandSourceStack source, double radius, String typeString, int capacity, boolean isArtificial) {
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();

        EssenceType type;
        try {
            type = EssenceType.valueOf(typeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("§cInvalid Essence Type: " + typeString));
            return 0;
        }

        AABB boundingBox = new AABB(
                pos.x - radius, pos.y - radius, pos.z - radius,
                pos.x + radius, pos.y + radius, pos.z + radius
        );

        List<EssenceNodeEntity> nodes = level.getEntitiesOfClass(EssenceNodeEntity.class, boundingBox);
        if (nodes.isEmpty()) {
            source.sendFailure(Component.literal("§cNo Essence Nodes found within radius."));
            return 0;
        }

        EssenceNodeEntity closestNode = null;
        double closestDistanceSq = Double.MAX_VALUE;

        for (EssenceNodeEntity node : nodes) {
            double distanceSq = node.distanceToSqr(pos);
            if (distanceSq < closestDistanceSq) {
                closestDistanceSq = distanceSq;
                closestNode = node;
            }
        }

        if (closestNode != null) {
            closestNode.setEssenceType(type);
            closestNode.setArtificial(isArtificial);

            if (capacity != -1) {
                closestNode.setMaxCapacity(capacity);
                closestNode.setCapacity(capacity);
            }

            final int finalCapacity = closestNode.getCapacity();
            source.sendSuccess(() -> Component.literal(
                    String.format("§aModified closest Essence Node to %s (Capacity: %d, Artificial: %b).",
                            type.getDisplayName(), finalCapacity, isArtificial)
            ), true);
            return 1;
        }

        return 0;
    }
}