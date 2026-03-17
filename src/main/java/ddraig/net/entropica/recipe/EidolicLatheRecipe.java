package ddraig.net.entropica.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public record EidolicLatheRecipe(
        Ingredient etherealShape,
        Ingredient core,
        List<Ingredient> modifiers,
        ItemStack result
) implements Recipe<RecipeInput> {

    @Override
    public boolean matches(RecipeInput input, Level level) {
        // We handle custom multiblock matching inside the BlockEntity itself!
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public RecipeSerializer<EidolicLatheRecipe> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<EidolicLatheRecipe> getType() {
        return Type.INSTANCE;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    // ==========================================
    // HARDCODED FALLBACK RECIPES
    // ==========================================
    private static List<EidolicLatheRecipe> HARDCODED_RECIPES = null;

    public static List<EidolicLatheRecipe> getHardcodedRecipes() {
        if (HARDCODED_RECIPES == null) {
            HARDCODED_RECIPES = new ArrayList<>();

            // Build the exact test recipe from the JSON
            List<Ingredient> testModifiers = new ArrayList<>();
            for (int i = 0; i < 4; i++) testModifiers.add(Ingredient.of(Items.DIAMOND));
            for (int i = 0; i < 2; i++) testModifiers.add(Ingredient.of(ModItems.ENTROPIC_ORE_ITEM.get()));
            for (int i = 0; i < 3; i++) testModifiers.add(Ingredient.of(Items.BLAZE_POWDER));
            testModifiers.add(Ingredient.of(Items.ECHO_SHARD));

            HARDCODED_RECIPES.add(new EidolicLatheRecipe(
                    Ingredient.of(Items.PAPER), // Ethereal Shape
                    Ingredient.of(Items.NETHER_STAR), // Core
                    testModifiers, // 10 Modifiers
                    new ItemStack(ModItems.TEST_EIDOLIC_WEAPON.get()) // Output
            ));
        }
        return HARDCODED_RECIPES;
    }

    public static class Type implements RecipeType<EidolicLatheRecipe> {
        private Type() {}
        public static final Type INSTANCE = new Type();
        public static final String ID = "eidolic_lathe";
    }

    public static class Serializer implements RecipeSerializer<EidolicLatheRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        public static final MapCodec<EidolicLatheRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC.fieldOf("ethereal_shape").forGetter(EidolicLatheRecipe::etherealShape),
                Ingredient.CODEC.fieldOf("core").forGetter(EidolicLatheRecipe::core),
                Ingredient.CODEC.listOf().fieldOf("modifiers").forGetter(EidolicLatheRecipe::modifiers),
                ItemStack.CODEC.fieldOf("result").forGetter(EidolicLatheRecipe::result)
        ).apply(inst, EidolicLatheRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, EidolicLatheRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, EidolicLatheRecipe::etherealShape,
                Ingredient.CONTENTS_STREAM_CODEC, EidolicLatheRecipe::core,
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), EidolicLatheRecipe::modifiers,
                ItemStack.STREAM_CODEC, EidolicLatheRecipe::result,
                EidolicLatheRecipe::new
        );

        @Override
        public MapCodec<EidolicLatheRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, EidolicLatheRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}