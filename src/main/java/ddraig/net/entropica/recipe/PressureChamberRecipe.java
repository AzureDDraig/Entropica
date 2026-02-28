package ddraig.net.entropica.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ddraig.net.entropica.api.EssenceType;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public record PressureChamberRecipe(Ingredient inputItem, EssenceType requiredFume, int fumeAmount, ItemStack output, int processingTime) implements Recipe<PressureChamberRecipeInput> {

    @Override
    public boolean matches(PressureChamberRecipeInput input, Level level) {
        if (!inputItem.test(input.item())) return false;
        if (input.fume().isEmpty() || input.fume().getType() != requiredFume) return false;
        return input.fume().getAmount() >= fumeAmount;
    }

    @Override
    public ItemStack assemble(PressureChamberRecipeInput input, HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public RecipeSerializer<PressureChamberRecipe> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<PressureChamberRecipe> getType() {
        return Type.INSTANCE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return null;
    }

    // FIXED: Required by 1.21.2+ for the vanilla recipe book.
    // Since this is an in-world multiblock, we tell the game it cannot be auto-placed in a GUI grid.
    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    public static class Type implements RecipeType<PressureChamberRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "pressure_chamber_enriching";
    }

    public static class Serializer implements RecipeSerializer<PressureChamberRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        public static final MapCodec<PressureChamberRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(PressureChamberRecipe::inputItem),
                EssenceType.CODEC.fieldOf("fume_type").forGetter(PressureChamberRecipe::requiredFume),
                Codec.INT.fieldOf("fume_amount").forGetter(PressureChamberRecipe::fumeAmount),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(PressureChamberRecipe::output),
                Codec.INT.optionalFieldOf("processing_time", 100).forGetter(PressureChamberRecipe::processingTime)
        ).apply(inst, PressureChamberRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, PressureChamberRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, PressureChamberRecipe::inputItem,
                EssenceType.STREAM_CODEC, PressureChamberRecipe::requiredFume,
                ByteBufCodecs.INT, PressureChamberRecipe::fumeAmount,
                ItemStack.STREAM_CODEC, PressureChamberRecipe::output,
                ByteBufCodecs.INT, PressureChamberRecipe::processingTime,
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