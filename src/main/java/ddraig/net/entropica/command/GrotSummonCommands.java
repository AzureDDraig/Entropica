package ddraig.net.entropica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
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
        // Construct the base command
        var cmd = Commands.literal("summongrot").requires(source -> source.hasPermission(2));

        // Define all our arguments in sequential order
        var typeArg = Commands.argument("type", StringArgumentType.word()).suggests(SUGGEST_ESSENCE_TYPES);
        var sizeArg = Commands.argument("size", IntegerArgumentType.integer(1, 16));
        var scaleArg = Commands.argument("scale", FloatArgumentType.floatArg(0.1f, 10.0f));
        var strArg = Commands.argument("strength", FloatArgumentType.floatArg(0.1f, 10.0f));
        var visArg = Commands.argument("materia", FloatArgumentType.floatArg(0.1f, 10.0f));
        var speedArg = Commands.argument("speed", FloatArgumentType.floatArg(0.1f, 10.0f));
        var waterSpeedArg = Commands.argument("waterspeed", FloatArgumentType.floatArg(0.1f, 10.0f));
        var intArg = Commands.argument("intelligence", IntegerArgumentType.integer(1, 20));
        var friendArg = Commands.argument("friendliness", IntegerArgumentType.integer(0, 255));
        var purityArg = Commands.argument("purity", IntegerArgumentType.integer(1, 28));
        var fertArg = Commands.argument("fertility", IntegerArgumentType.integer(1, 20));
        var sterileArg = Commands.argument("sterile", BoolArgumentType.bool());
        var rangedArg = Commands.argument("ranged", BoolArgumentType.bool());
        var cohesiveArg = Commands.argument("cohesive", BoolArgumentType.bool());

        // Assign execution endpoints tracking how many arguments deep the user went
        typeArg.executes(c -> executeSpawn(c, 1));
        sizeArg.executes(c -> executeSpawn(c, 2));
        scaleArg.executes(c -> executeSpawn(c, 3));
        strArg.executes(c -> executeSpawn(c, 4));
        visArg.executes(c -> executeSpawn(c, 5));
        speedArg.executes(c -> executeSpawn(c, 6));
        waterSpeedArg.executes(c -> executeSpawn(c, 7));
        intArg.executes(c -> executeSpawn(c, 8));
        friendArg.executes(c -> executeSpawn(c, 9));
        purityArg.executes(c -> executeSpawn(c, 10));
        fertArg.executes(c -> executeSpawn(c, 11));
        sterileArg.executes(c -> executeSpawn(c, 12));
        rangedArg.executes(c -> executeSpawn(c, 13));
        cohesiveArg.executes(c -> executeSpawn(c, 14));

        // Chain the arguments backwards from end to start
        rangedArg.then(cohesiveArg);
        sterileArg.then(rangedArg);
        fertArg.then(sterileArg);
        purityArg.then(fertArg);
        friendArg.then(purityArg);
        intArg.then(friendArg);
        waterSpeedArg.then(intArg);
        speedArg.then(waterSpeedArg);
        visArg.then(speedArg);
        strArg.then(visArg);
        scaleArg.then(strArg);
        sizeArg.then(scaleArg);
        typeArg.then(sizeArg);
        cmd.then(typeArg);

        dispatcher.register(cmd);
    }

    private static int executeSpawn(CommandContext<CommandSourceStack> context, int argsProvided) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();

        String typeString = StringArgumentType.getString(context, "type");
        EssenceType type;
        try {
            type = EssenceType.valueOf(typeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("Invalid Essence Type: " + typeString));
            return 0; // 0 indicates command failure
        }

        GrotEntity grot = (GrotEntity) ModEntityTypes.GROT.get().create(level, EntitySpawnReason.COMMAND);

        if (grot != null) {
            grot.setPos(pos.x, pos.y, pos.z);

            // 1. Let your class roll completely natural randoms first
            grot.finalizeSpawn(level, level.getCurrentDifficultyAt(grot.blockPosition()), EntitySpawnReason.COMMAND, null);

            // 2. Now securely override only what the command explicitly asked for!
            grot.setEssenceType(type);

            int size = argsProvided >= 2 ? IntegerArgumentType.getInteger(context, "size") : 1;
            grot.setSize(size, true); // true adjusts and resets health

            if (argsProvided >= 3) grot.setScaleFactor(FloatArgumentType.getFloat(context, "scale"));
            if (argsProvided >= 4) grot.setStrengthFactor(FloatArgumentType.getFloat(context, "strength"));
            if (argsProvided >= 5) grot.setVisFactor(FloatArgumentType.getFloat(context, "materia"));
            if (argsProvided >= 6) grot.setSpeedFactor(FloatArgumentType.getFloat(context, "speed"));
            if (argsProvided >= 7) grot.setWaterSpeedFactor(FloatArgumentType.getFloat(context, "waterspeed"));
            if (argsProvided >= 8) grot.setIntelligence(IntegerArgumentType.getInteger(context, "intelligence"));
            if (argsProvided >= 9) grot.setFriendliness(IntegerArgumentType.getInteger(context, "friendliness"));
            if (argsProvided >= 10) grot.setPurity(IntegerArgumentType.getInteger(context, "purity"));
            if (argsProvided >= 11) grot.setFertility(IntegerArgumentType.getInteger(context, "fertility"));
            if (argsProvided >= 12) grot.setSterile(BoolArgumentType.getBool(context, "sterile"));
            if (argsProvided >= 13) grot.setCanRanged(BoolArgumentType.getBool(context, "ranged"));
            if (argsProvided >= 14) grot.setCohesive(BoolArgumentType.getBool(context, "cohesive"));

            // Safety catch to heal after max-health modifiers apply
            grot.setHealth(grot.getMaxHealth());

            level.addFreshEntity(grot);
            source.sendSuccess(() -> Component.literal("Summoned a custom Size " + size + " " + type.name() + " Grot!"), true);
            return 1; // 1 indicates success
        }

        source.sendFailure(Component.literal("Failed to summon the Grot."));
        return 0;
    }
}