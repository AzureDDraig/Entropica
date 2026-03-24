package ddraig.net.entropica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.grot.GrotEntity;
import ddraig.net.entropica.registry.ModEntityTypes;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.stream.Collectors;

public class GrotSummonCommands {

    // Automatically provides Tab-Complete suggestions for all your EssenceTypes
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ESSENCE_TYPES = (context, builder) ->
            SharedSuggestionProvider.suggest(Arrays.stream(EssenceType.values()).map(Enum::name).collect(Collectors.toList()), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("summongrot")
                .requires(source -> source.hasPermission(2)) // Requires OP/Cheats enabled
                .then(Commands.argument("type", StringArgumentType.word())
                        .suggests(SUGGEST_ESSENCE_TYPES)
                        // Executes if the user only types: /summongrot <type> (Defaults to Size 1)
                        .executes(context -> spawnGrot(context, StringArgumentType.getString(context, "type"), 1))
                        .then(Commands.argument("size", IntegerArgumentType.integer(1, 16))
                                // Executes if the user types: /summongrot <type> <size>
                                .executes(context -> spawnGrot(context, StringArgumentType.getString(context, "type"), IntegerArgumentType.getInteger(context, "size")))
                        )
                )
        );
    }

    private static int spawnGrot(CommandContext<CommandSourceStack> context, String typeString, int size) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();

        EssenceType type;
        try {
            // Safely parse the string to your enum
            type = EssenceType.valueOf(typeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("Invalid Essence Type: " + typeString));
            return 0; // 0 indicates command failure
        }

        // 1.21.2+ entity creation syntax
        GrotEntity grot = (GrotEntity) ModEntityTypes.GROT.get().create(level, EntitySpawnReason.COMMAND);

        if (grot != null) {
            grot.setPos(pos.x, pos.y, pos.z);
            grot.setEssenceType(type);
            grot.setSize(size, true);

            level.addFreshEntity(grot);
            source.sendSuccess(() -> Component.literal("Summoned a Size " + size + " " + type.name() + " Grot!"), true);
            return 1; // 1 indicates success
        }

        source.sendFailure(Component.literal("Failed to summon the Grot."));
        return 0;
    }
}