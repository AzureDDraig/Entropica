package ddraig.net.entropica.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AethericSynthesizerRecipe implements Recipe<RecipeInput> {

    private final List<String> pattern;
    private final Map<String, Ingredient> key;
    private final List<ItemStack> results;

    private final NonNullList<Optional<Ingredient>> gridIngredients;

    public AethericSynthesizerRecipe(List<String> pattern, Map<String, Ingredient> key, List<ItemStack> results) {
        this.pattern = pattern;
        this.key = key;
        this.results = results;

        this.gridIngredients = NonNullList.withSize(25, Optional.empty());
        for (int r = 0; r < 5; r++) {
            String row = r < pattern.size() ? pattern.get(r) : "";
            for (int c = 0; c < 5; c++) {
                char ch = c < row.length() ? row.charAt(c) : ' ';
                if (ch != ' ') {
                    Ingredient ing = key.get(String.valueOf(ch));
                    if (ing != null) {
                        this.gridIngredients.set(r * 5 + c, Optional.of(ing));
                    }
                }
            }
        }
    }

    public List<String> getPattern() { return pattern; }
    public Map<String, Ingredient> getKey() { return key; }
    public NonNullList<Optional<Ingredient>> getGridIngredients() { return gridIngredients; }
    public List<ItemStack> getResults() { return results; }

    @Override
    public boolean matches(RecipeInput input, Level level) { return false; }

    public boolean matchesGrid(SimpleContainer inventory) {
        for (int rotation = 0; rotation < 4; rotation++) {
            if (checkMatchWithRotation(inventory, rotation)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkMatchWithRotation(SimpleContainer inventory, int rotation) {
        // 0. Build the 5x5 expected grid for THIS specific rotation
        List<Optional<Ingredient>> rotatedExpected = new ArrayList<>(25);
        for (int i = 0; i < 25; i++) rotatedExpected.add(Optional.empty());

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                int originalIndex = r * 5 + c;
                int rotatedIndex = getRotatedIndex(r, c, rotation);
                rotatedExpected.set(rotatedIndex, gridIngredients.get(originalIndex));
            }
        }

        // 1. Find the bounding box of the rotated recipe pattern
        int recMinCol = 5, recMaxCol = -1, recMinRow = 5, recMaxRow = -1;
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                if (rotatedExpected.get(r * 5 + c).isPresent()) {
                    if (c < recMinCol) recMinCol = c;
                    if (c > recMaxCol) recMaxCol = c;
                    if (r < recMinRow) recMinRow = r;
                    if (r > recMaxRow) recMaxRow = r;
                }
            }
        }

        // Failsafe: If the recipe is entirely blank, make sure the inventory is also blank
        if (recMaxCol == -1) {
            for (int i = 0; i < 25; i++) {
                if (!inventory.getItem(i).isEmpty()) return false;
            }
            return true;
        }

        int recWidth = recMaxCol - recMinCol + 1;
        int recHeight = recMaxRow - recMinRow + 1;

        // 2. Find the bounding box of the actual items sitting on the table
        int invMinCol = 5, invMaxCol = -1, invMinRow = 5, invMaxRow = -1;
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                // Ensure we only check up to slot 24 (ignoring the 4 output slots)
                if (!inventory.getItem(r * 5 + c).isEmpty()) {
                    if (c < invMinCol) invMinCol = c;
                    if (c > invMaxCol) invMaxCol = c;
                    if (r < invMinRow) invMinRow = r;
                    if (r > invMaxRow) invMaxRow = r;
                }
            }
        }

        // If there's nothing on the table but the recipe requires something, it's a fail
        if (invMaxCol == -1) return false;

        int invWidth = invMaxCol - invMinCol + 1;
        int invHeight = invMaxRow - invMinRow + 1;

        // If the shapes don't perfectly match in dimensions, abort early
        if (recWidth != invWidth || recHeight != invHeight) return false;

        // 3. Compare the cropped bounding boxes directly!
        // This allows a 1x3 wire recipe to be placed ANYWHERE on the 5x5 grid!
        for (int r = 0; r < recHeight; r++) {
            for (int c = 0; c < recWidth; c++) {
                int recIndex = (recMinRow + r) * 5 + (recMinCol + c);
                int invIndex = (invMinRow + r) * 5 + (invMinCol + c);

                Optional<Ingredient> expectedIngredient = rotatedExpected.get(recIndex);
                ItemStack stackInSlot = inventory.getItem(invIndex);

                if (expectedIngredient.isPresent()) {
                    if (!expectedIngredient.get().test(stackInSlot)) return false;
                } else {
                    // If the recipe expects an empty space inside its bounds, ensure the slot is actually empty
                    if (!stackInSlot.isEmpty()) return false;
                }
            }
        }

        return true;
    }

    private int getRotatedIndex(int r, int c, int rotation) {
        return switch (rotation) {
            case 0 -> r * 5 + c;
            case 1 -> c * 5 + (4 - r);
            case 2 -> (4 - r) * 5 + (4 - c);
            case 3 -> (4 - c) * 5 + r;
            default -> r * 5 + c;
        };
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return results.isEmpty() ? ItemStack.EMPTY : results.get(0).copy();
    }

    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return results.isEmpty() ? ItemStack.EMPTY : results.get(0);
    }

    @Override public RecipeSerializer<AethericSynthesizerRecipe> getSerializer() { return Serializer.INSTANCE; }
    @Override public RecipeType<AethericSynthesizerRecipe> getType() { return Type.INSTANCE; }
    @Override public RecipeBookCategory recipeBookCategory() { return RecipeBookCategories.CRAFTING_MISC; }
    @Override public PlacementInfo placementInfo() { return PlacementInfo.NOT_PLACEABLE; }

    public static class Type implements RecipeType<AethericSynthesizerRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "aetheric_synthesis";
    }

    public static class Serializer implements RecipeSerializer<AethericSynthesizerRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        public static final MapCodec<AethericSynthesizerRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.STRING.listOf().fieldOf("pattern").forGetter(AethericSynthesizerRecipe::getPattern),
                Codec.unboundedMap(Codec.STRING, Ingredient.CODEC).fieldOf("key").forGetter(AethericSynthesizerRecipe::getKey),
                ItemStack.CODEC.listOf().fieldOf("results").forGetter(AethericSynthesizerRecipe::getResults)
        ).apply(inst, AethericSynthesizerRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, AethericSynthesizerRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork,
                Serializer::fromNetwork
        );

        @Override public MapCodec<AethericSynthesizerRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, AethericSynthesizerRecipe> streamCodec() { return STREAM_CODEC; }

        private static AethericSynthesizerRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            int patSize = buffer.readVarInt();
            List<String> pattern = new ArrayList<>();
            for (int i = 0; i < patSize; i++) pattern.add(buffer.readUtf());

            int keySize = buffer.readVarInt();
            Map<String, Ingredient> key = new HashMap<>();
            for (int i = 0; i < keySize; i++) {
                String k = buffer.readUtf();
                Ingredient ing = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
                key.put(k, ing);
            }

            int resSize = buffer.readVarInt();
            List<ItemStack> results = new ArrayList<>();
            for (int i = 0; i < resSize; i++) results.add(ItemStack.STREAM_CODEC.decode(buffer));

            return new AethericSynthesizerRecipe(pattern, key, results);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, AethericSynthesizerRecipe recipe) {
            buffer.writeVarInt(recipe.pattern.size());
            for (String s : recipe.pattern) buffer.writeUtf(s);

            buffer.writeVarInt(recipe.key.size());
            for (Map.Entry<String, Ingredient> entry : recipe.key.entrySet()) {
                buffer.writeUtf(entry.getKey());
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, entry.getValue());
            }

            buffer.writeVarInt(recipe.results.size());
            for (ItemStack stack : recipe.results) ItemStack.STREAM_CODEC.encode(buffer, stack);
        }
    }
}