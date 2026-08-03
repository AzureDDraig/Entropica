package ddraig.net.entropica.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.recipe.AethericSynthesizerRecipe;
import ddraig.net.entropica.recipe.DilutedEssenceRecipe;
import ddraig.net.entropica.recipe.FusionRecipe;
import ddraig.net.entropica.recipe.GlassRedyeRecipe;
import ddraig.net.entropica.recipe.PressureChamberRecipe;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create("entropica", net.minecraft.resources.ResourceKey.createRegistryKey(net.minecraft.resources.ResourceLocation.withDefaultNamespace("recipe_serializer")));
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create("entropica", net.minecraft.resources.ResourceKey.createRegistryKey(net.minecraft.resources.ResourceLocation.withDefaultNamespace("recipe_type")));

    // --- Fusion Recipes ---
    public static final RegistrySupplier<RecipeType<FusionRecipe>> FUSION_TYPE =
            TYPES.register("fusion", () -> new RecipeType<>() {});

    public static final RegistrySupplier<RecipeSerializer<FusionRecipe>> FUSION_SERIALIZER =
            SERIALIZERS.register("fusion", () -> new FusionSerializer());

    // --- Pressure Chamber Recipes ---
    public static final RegistrySupplier<RecipeType<PressureChamberRecipe>> PRESSURE_CHAMBER_TYPE =
            TYPES.register("pressure_chamber_enriching", () -> PressureChamberRecipe.Type.INSTANCE);

    public static final RegistrySupplier<RecipeSerializer<PressureChamberRecipe>> PRESSURE_CHAMBER_SERIALIZER =
            SERIALIZERS.register("pressure_chamber_enriching", () -> PressureChamberRecipe.Serializer.INSTANCE);

    // --- Diluted Essence Recipes ---
    public static final RegistrySupplier<RecipeType<DilutedEssenceRecipe>> DILUTED_ESSENCE_FLUID_TYPE =
            TYPES.register("diluted_essence", () -> new RecipeType<>() {});

    public static final RegistrySupplier<RecipeSerializer<DilutedEssenceRecipe>> DILUTED_ESSENCE_FLUID_SERIALIZER =
            SERIALIZERS.register("diluted_essence", DilutedEssenceRecipe.Serializer::new);

    // --- Aetheric Synthesizer Recipes ---
    public static final RegistrySupplier<RecipeType<AethericSynthesizerRecipe>> AETHERIC_SYNTHESIZER_TYPE =
            TYPES.register("aetheric_synthesis", () -> AethericSynthesizerRecipe.Type.INSTANCE);

    public static final RegistrySupplier<RecipeSerializer<AethericSynthesizerRecipe>> AETHERIC_SYNTHESIZER_SERIALIZER =
            SERIALIZERS.register("aetheric_synthesis", () -> AethericSynthesizerRecipe.Serializer.INSTANCE);

    // --- Eidolic Lathe Recipes ---
    public static final RegistrySupplier<RecipeType<ddraig.net.entropica.recipe.EidolicLatheRecipe>> EIDOLIC_LATHE_TYPE =
            TYPES.register("eidolic_lathe", () -> ddraig.net.entropica.recipe.EidolicLatheRecipe.Type.INSTANCE);

    public static final RegistrySupplier<RecipeSerializer<ddraig.net.entropica.recipe.EidolicLatheRecipe>> EIDOLIC_LATHE_SERIALIZER =
            SERIALIZERS.register("eidolic_lathe", () -> ddraig.net.entropica.recipe.EidolicLatheRecipe.Serializer.INSTANCE);

    // --- Magic Circle Recipes ---
    public static final RegistrySupplier<RecipeType<ddraig.net.entropica.recipe.MagicCircleRecipe>> MAGIC_CIRCLE_TYPE =
            TYPES.register("magic_circle", () -> ddraig.net.entropica.recipe.MagicCircleRecipe.Type.INSTANCE);

    public static final RegistrySupplier<RecipeSerializer<ddraig.net.entropica.recipe.MagicCircleRecipe>> MAGIC_CIRCLE_SERIALIZER =
            SERIALIZERS.register("magic_circle", () -> ddraig.net.entropica.recipe.MagicCircleRecipe.Serializer.INSTANCE);

    // --- Aesthetic Glass Re-Dyeing Recipe ---
    public static final RegistrySupplier<RecipeSerializer<GlassRedyeRecipe>> GLASS_REDYE_SERIALIZER =
            SERIALIZERS.register("glass_redye", GlassRedyeRecipe.Serializer::new);

    public static void register() {
        SERIALIZERS.register();
        TYPES.register();
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