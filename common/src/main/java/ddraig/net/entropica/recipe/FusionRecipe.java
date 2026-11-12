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
import java.util.HashMap;

public record FusionRecipe(
        Map<EssenceType, Integer> inputs,
        EssenceType success,
        EssenceType failure,
        float chance,
        int yield,
        boolean requiresCatalyst
) implements Recipe<RecipeInput> {

    @Override
    public boolean matches(RecipeInput input, Level level) {
        return true;
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
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

    @Override
    public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return ModRecipes.FUSION_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<RecipeInput>> getType() {
        return ModRecipes.FUSION_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<FusionRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        private static final MapCodec<FusionRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.unboundedMap(EssenceType.CODEC, Codec.INT).fieldOf("inputs").forGetter(FusionRecipe::inputs),
                EssenceType.CODEC.fieldOf("success").forGetter(FusionRecipe::success),
                EssenceType.CODEC.fieldOf("failure").forGetter(FusionRecipe::failure),
                Codec.FLOAT.fieldOf("chance").forGetter(FusionRecipe::chance),
                Codec.INT.fieldOf("yield").forGetter(FusionRecipe::yield),
                Codec.BOOL.fieldOf("requires_catalyst").forGetter(FusionRecipe::requiresCatalyst)
        ).apply(inst, FusionRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, FusionRecipe> STREAM_CODEC = StreamCodec.of(
                (buf, recipe) -> {
                    buf.writeInt(recipe.inputs().size());
                    recipe.inputs().forEach((type, count) -> {
                        buf.writeEnum(type);
                        buf.writeInt(count);
                    });
                    buf.writeEnum(recipe.success());
                    buf.writeEnum(recipe.failure());
                    buf.writeFloat(recipe.chance());
                    buf.writeInt(recipe.yield());
                    buf.writeBoolean(recipe.requiresCatalyst());
                },
                buf -> {
                    int size = buf.readInt();
                    Map<EssenceType, Integer> inputs = new HashMap<>();
                    for (int i = 0; i < size; i++) {
                        inputs.put(buf.readEnum(EssenceType.class), buf.readInt());
                    }
                    return new FusionRecipe(
                            inputs,
                            buf.readEnum(EssenceType.class),
                            buf.readEnum(EssenceType.class),
                            buf.readFloat(),
                            buf.readInt(),
                            buf.readBoolean()
                    );
                }
        );

        @Override
        public MapCodec<FusionRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, FusionRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}