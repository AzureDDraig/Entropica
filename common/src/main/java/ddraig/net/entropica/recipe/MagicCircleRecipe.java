package ddraig.net.entropica.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;

public record MagicCircleRecipe(
        List<Ingredient> inputs,
        List<Ingredient> runes,
        Map<EssenceType, Integer> essences,
        int tier,
        ItemStack output
) implements Recipe<MagicCircleRecipeInput> {

    @Override
    public boolean matches(MagicCircleRecipeInput input, Level level) {
        if (input.circleTier() < this.tier) {
            return false;
        }

        // 1. Verify inputs (items on INPUT nodes)
        List<ItemStack> availableInputs = new java.util.ArrayList<>(input.inputs());
        for (Ingredient ing : this.inputs) {
            boolean matched = false;
            for (int i = 0; i < availableInputs.size(); i++) {
                if (ing.test(availableInputs.get(i))) {
                    availableInputs.remove(i);
                    matched = true;
                    break;
                }
            }
            if (!matched) return false;
        }

        // 2. Verify runes (stored runes in RUNE nodes)
        List<ItemStack> availableRunes = new java.util.ArrayList<>(input.runes());
        for (Ingredient ing : this.runes) {
            boolean matched = false;
            for (int i = 0; i < availableRunes.size(); i++) {
                if (ing.test(availableRunes.get(i))) {
                    availableRunes.remove(i);
                    matched = true;
                    break;
                }
            }
            if (!matched) return false;
        }

        // 3. Verify essences
        for (Map.Entry<EssenceType, Integer> entry : this.essences.entrySet()) {
            int requiredAmount = entry.getValue();
            if (requiredAmount <= 0) continue;

            if (entry.getKey() == EssenceType.REGULAR) {
                // Wildcard requirement: sum of ALL essence types in circuit/circle
                int totalProvided = input.essences().values().stream()
                        .mapToInt(Integer::intValue)
                        .sum();
                if (totalProvided < requiredAmount) {
                    return false;
                }
            } else {
                // Specific requirement: MUST match this exact essence type specifically!
                int providedAmount = input.essences().getOrDefault(entry.getKey(), 0);
                if (providedAmount < requiredAmount) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public ItemStack assemble(MagicCircleRecipeInput input, HolderLookup.Provider registries) {
        return this.output.copy();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @SuppressWarnings("unchecked")
    @Override
    public RecipeSerializer<MagicCircleRecipe> getSerializer() {
        return (RecipeSerializer<MagicCircleRecipe>) (Object) ModRecipes.MAGIC_CIRCLE_SERIALIZER.get();
    }

    @SuppressWarnings("unchecked")
    @Override
    public RecipeType<MagicCircleRecipe> getType() {
        return (RecipeType<MagicCircleRecipe>) (Object) ModRecipes.MAGIC_CIRCLE_TYPE.get();
    }

    public static class Type implements RecipeType<MagicCircleRecipe> {
        public static final Type INSTANCE = new Type();
        private Type() {}

        @Override
        public String toString() {
            return "magic_circle";
        }
    }

    public static class Serializer implements RecipeSerializer<MagicCircleRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        public static final MapCodec<MagicCircleRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC.listOf().fieldOf("inputs").forGetter(MagicCircleRecipe::inputs),
                Ingredient.CODEC.listOf().fieldOf("runes").forGetter(MagicCircleRecipe::runes),
                Codec.unboundedMap(EssenceType.CODEC, Codec.INT).fieldOf("essences").forGetter(MagicCircleRecipe::essences),
                Codec.INT.fieldOf("tier").forGetter(MagicCircleRecipe::tier),
                ItemStack.CODEC.fieldOf("result").forGetter(MagicCircleRecipe::output)
        ).apply(inst, MagicCircleRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, MagicCircleRecipe> STREAM_CODEC = StreamCodec.of(
                (buf, recipe) -> {
                    buf.writeInt(recipe.inputs().size());
                    for (Ingredient ing : recipe.inputs()) {
                        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ing);
                    }
                    buf.writeInt(recipe.runes().size());
                    for (Ingredient ing : recipe.runes()) {
                        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ing);
                    }
                    buf.writeInt(recipe.essences().size());
                    recipe.essences().forEach((type, count) -> {
                        buf.writeEnum(type);
                        buf.writeInt(count);
                    });
                    buf.writeInt(recipe.tier());
                    ItemStack.STREAM_CODEC.encode(buf, recipe.output());
                },
                buf -> {
                    int inputSize = buf.readInt();
                    List<Ingredient> inputs = new java.util.ArrayList<>(inputSize);
                    for (int i = 0; i < inputSize; i++) {
                        inputs.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
                    }
                    int runeSize = buf.readInt();
                    List<Ingredient> runes = new java.util.ArrayList<>(runeSize);
                    for (int i = 0; i < runeSize; i++) {
                        runes.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
                    }
                    int essenceSize = buf.readInt();
                    Map<EssenceType, Integer> essences = new java.util.HashMap<>(essenceSize);
                    for (int i = 0; i < essenceSize; i++) {
                        essences.put(buf.readEnum(EssenceType.class), buf.readInt());
                    }
                    int tier = buf.readInt();
                    ItemStack output = ItemStack.STREAM_CODEC.decode(buf);
                    return new MagicCircleRecipe(inputs, runes, essences, tier, output);
                }
        );

        @Override
        public MapCodec<MagicCircleRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MagicCircleRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
