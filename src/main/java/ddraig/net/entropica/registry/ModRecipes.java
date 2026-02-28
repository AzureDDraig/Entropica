package ddraig.net.entropica.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.recipe.FusionRecipe;
import ddraig.net.entropica.recipe.PressureChamberRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, "entropica");
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, "entropica");

    public static final DeferredHolder<RecipeType<?>, RecipeType<FusionRecipe>> FUSION_TYPE =
            TYPES.register("fusion", () -> new RecipeType<>() {});

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FusionRecipe>> FUSION_SERIALIZER =
            SERIALIZERS.register("fusion", () -> new FusionSerializer());

    public static final DeferredHolder<RecipeType<?>, RecipeType<PressureChamberRecipe>> PRESSURE_CHAMBER_TYPE =
            TYPES.register("pressure_chamber_enriching", () -> PressureChamberRecipe.Type.INSTANCE);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<PressureChamberRecipe>> PRESSURE_CHAMBER_SERIALIZER =
            SERIALIZERS.register("pressure_chamber_enriching", () -> PressureChamberRecipe.Serializer.INSTANCE);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);
    }

    private static class FusionSerializer implements RecipeSerializer<FusionRecipe> {
        // This Codec defines the JSON structure
        private static final MapCodec<FusionRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.unboundedMap(EssenceType.CODEC, Codec.INT).fieldOf("inputs").forGetter(FusionRecipe::inputs),
                EssenceType.CODEC.fieldOf("success").forGetter(FusionRecipe::success),
                EssenceType.CODEC.optionalFieldOf("failure", EssenceType.REGULAR).forGetter(FusionRecipe::failure),
                Codec.FLOAT.fieldOf("chance").forGetter(FusionRecipe::chance),
                Codec.INT.fieldOf("yield").forGetter(FusionRecipe::yield),
                Codec.BOOL.fieldOf("requires_catalyst").forGetter(FusionRecipe::requiresCatalyst)
        ).apply(inst, FusionRecipe::new));

        @Override
        public MapCodec<FusionRecipe> codec() { return CODEC; }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, FusionRecipe> streamCodec() {
            // Needed for syncing recipes to the client (e.g. for JEI)
            return StreamCodec.of((buf, recipe) -> {
                buf.writeInt(recipe.inputs().size());
                recipe.inputs().forEach((type, amt) -> {
                    buf.writeEnum(type);
                    buf.writeInt(amt);
                });
                buf.writeEnum(recipe.success());
                buf.writeEnum(recipe.failure());
                buf.writeFloat(recipe.chance());
                buf.writeInt(recipe.yield());
                buf.writeBoolean(recipe.requiresCatalyst());
            }, buf -> {
                // Reader logic... (simplified for brevity)
                return null;
            });
        }
    }
}