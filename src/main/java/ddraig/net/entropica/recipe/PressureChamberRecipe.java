package ddraig.net.entropica.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.Optional;

public record PressureChamberRecipe(
        Ingredient inputItem,
        int inputCount,
        boolean requiresSpecificFume,
        Optional<EssenceType> requiredFume,
        int fumeAmount,
        int processingTime,
        ItemStack output
) implements Recipe<PressureChamberRecipeInput> {

    @Override
    public boolean matches(PressureChamberRecipeInput input, Level level) {
        return inputItem.test(input.item()) && input.item().getCount() >= inputCount;
    }

    @Override
    public ItemStack assemble(PressureChamberRecipeInput input, HolderLookup.Provider registries) {
        return output.copy();
    }

    @SuppressWarnings("unchecked")
    @Override
    public RecipeSerializer<PressureChamberRecipe> getSerializer() {
        return (RecipeSerializer<PressureChamberRecipe>) (Object) ModRecipes.PRESSURE_CHAMBER_SERIALIZER.get();
    }

    @SuppressWarnings("unchecked")
    @Override
    public RecipeType<PressureChamberRecipe> getType() {
        return (RecipeType<PressureChamberRecipe>) (Object) ModRecipes.PRESSURE_CHAMBER_TYPE.get();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return null;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    // FIXED: Added the Type class and INSTANCE variable to satisfy ModRecipes.java
    public static class Type implements RecipeType<PressureChamberRecipe> {
        public static final Type INSTANCE = new Type();
        private Type() {}

        @Override
        public String toString() {
            return "pressure_chamber_enriching";
        }
    }

    public static class Serializer implements RecipeSerializer<PressureChamberRecipe> {

        // FIXED: Added the INSTANCE variable to satisfy ModRecipes.java
        public static final Serializer INSTANCE = new Serializer();

        public static final MapCodec<PressureChamberRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC.fieldOf("input").forGetter(PressureChamberRecipe::inputItem),
                Codec.INT.optionalFieldOf("input_count", 1).forGetter(PressureChamberRecipe::inputCount),
                Codec.BOOL.optionalFieldOf("requires_specific_fume", false).forGetter(PressureChamberRecipe::requiresSpecificFume),
                Codec.STRING.xmap(EssenceType::valueOf, EssenceType::name).optionalFieldOf("fume_type").forGetter(PressureChamberRecipe::requiredFume),
                Codec.INT.optionalFieldOf("fume_amount", 64).forGetter(PressureChamberRecipe::fumeAmount),
                Codec.INT.optionalFieldOf("processing_time", 100).forGetter(PressureChamberRecipe::processingTime),
                ItemStack.CODEC.fieldOf("output").forGetter(PressureChamberRecipe::output)
        ).apply(inst, PressureChamberRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, PressureChamberRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, PressureChamberRecipe::inputItem,
                ByteBufCodecs.INT, PressureChamberRecipe::inputCount,
                ByteBufCodecs.BOOL, PressureChamberRecipe::requiresSpecificFume,
                ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8.map(EssenceType::valueOf, EssenceType::name)), PressureChamberRecipe::requiredFume,
                ByteBufCodecs.INT, PressureChamberRecipe::fumeAmount,
                ByteBufCodecs.INT, PressureChamberRecipe::processingTime,
                ItemStack.STREAM_CODEC, PressureChamberRecipe::output,
                PressureChamberRecipe::new
        );

        @Override
        public MapCodec<PressureChamberRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, PressureChamberRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}