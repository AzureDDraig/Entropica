package ddraig.net.entropica.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ddraig.net.entropica.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record DilutedEssenceRecipe(List<Ingredient> inputs, ItemStack output, int processingTime, int chargeCost) implements Recipe<FluidCraftingInput> {

    @Override
    public boolean matches(FluidCraftingInput input, Level level) {
        if (input.size() < this.inputs.size()) return false;

        List<ItemStack> available = new ArrayList<>(input.items());

        for (Ingredient ing : this.inputs) {
            boolean found = false;
            for (int i = 0; i < available.size(); i++) {
                if (ing.test(available.get(i))) {
                    available.remove(i);
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    @Override
    public ItemStack assemble(FluidCraftingInput input, HolderLookup.Provider registries) {
        return this.output.copy();
    }

    @Override
    public RecipeSerializer<DilutedEssenceRecipe> getSerializer() {
        return ModRecipes.DILUTED_ESSENCE_FLUID_SERIALIZER.get(); // FIXED
    }

    @Override
    public RecipeType<DilutedEssenceRecipe> getType() {
        return ModRecipes.DILUTED_ESSENCE_FLUID_TYPE.get(); // FIXED
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(this.inputs.get(0));
    }

    @Nullable
    @Override
    public RecipeBookCategory recipeBookCategory() {
        return null;
    }

    // --- SERIALIZER ---
    public static class Serializer implements RecipeSerializer<DilutedEssenceRecipe> {

        public static final MapCodec<DilutedEssenceRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC.listOf().fieldOf("inputs").forGetter(DilutedEssenceRecipe::inputs),
                ItemStack.CODEC.fieldOf("output").forGetter(DilutedEssenceRecipe::output),
                Codec.INT.optionalFieldOf("processingTime", 100).forGetter(DilutedEssenceRecipe::processingTime),
                Codec.INT.optionalFieldOf("chargeCost", 1).forGetter(DilutedEssenceRecipe::chargeCost)
        ).apply(inst, DilutedEssenceRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, DilutedEssenceRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), DilutedEssenceRecipe::inputs,
                ItemStack.STREAM_CODEC, DilutedEssenceRecipe::output,
                ByteBufCodecs.INT, DilutedEssenceRecipe::processingTime,
                ByteBufCodecs.INT, DilutedEssenceRecipe::chargeCost,
                DilutedEssenceRecipe::new
        );

        @Override
        public MapCodec<DilutedEssenceRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DilutedEssenceRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}