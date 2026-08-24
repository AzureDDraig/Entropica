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
        Ingredient orbisAcceptor,
        Ingredient baseMaterial,
        List<Ingredient> modifiers,
        ItemStack result
) implements Recipe<RecipeInput> {

    @Override
    public boolean matches(RecipeInput input, Level level) {
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
                Ingredient.CODEC.fieldOf("orbis_acceptor").forGetter(EidolicLatheRecipe::orbisAcceptor),
                Ingredient.CODEC.fieldOf("base_material").forGetter(EidolicLatheRecipe::baseMaterial),
                Ingredient.CODEC.listOf().fieldOf("modifiers").forGetter(EidolicLatheRecipe::modifiers),
                ItemStack.CODEC.fieldOf("result").forGetter(EidolicLatheRecipe::result)
        ).apply(inst, EidolicLatheRecipe::new));

        // Using StreamCodec.of() perfectly bypasses any parameter limits that .composite() might have!
        public static final StreamCodec<RegistryFriendlyByteBuf, EidolicLatheRecipe> STREAM_CODEC = StreamCodec.of(
                (buf, recipe) -> {
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.etherealShape());
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.core());
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.orbisAcceptor());
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.baseMaterial());
                    Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.modifiers());
                    ItemStack.STREAM_CODEC.encode(buf, recipe.result());
                },
                buf -> new EidolicLatheRecipe(
                        Ingredient.CONTENTS_STREAM_CODEC.decode(buf),
                        Ingredient.CONTENTS_STREAM_CODEC.decode(buf),
                        Ingredient.CONTENTS_STREAM_CODEC.decode(buf),
                        Ingredient.CONTENTS_STREAM_CODEC.decode(buf),
                        Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf),
                        ItemStack.STREAM_CODEC.decode(buf)
                )
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