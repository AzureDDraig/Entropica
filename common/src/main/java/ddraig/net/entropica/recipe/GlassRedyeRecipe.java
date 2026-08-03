package ddraig.net.entropica.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.registry.AestheticGlassRegistry;
import ddraig.net.entropica.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GlassRedyeRecipe extends CustomRecipe {

    public GlassRedyeRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        List<ItemStack> nonItems = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                nonItems.add(stack);
            }
        }

        if (nonItems.size() < 2 || nonItems.size() > 9) {
            return false;
        }

        ItemStack dyeStack = ItemStack.EMPTY;
        int glassCount = 0;
        int targetShapeIndex = -1;

        for (ItemStack stack : nonItems) {
            int shapeIndex = AestheticGlassRegistry.getShapeIndex(stack.getItem());
            if (shapeIndex >= 0) {
                if (targetShapeIndex == -1) {
                    targetShapeIndex = shapeIndex;
                } else if (targetShapeIndex != shapeIndex) {
                    return false; // Mismatched shape variants in recipe
                }
                glassCount++;
            } else if (dyeStack.isEmpty() && getTargetFamilyName(stack) != null) {
                dyeStack = stack;
            } else {
                return false; // Invalid or unexpected item in crafting grid
            }
        }

        if (dyeStack.isEmpty() || glassCount == 0 || targetShapeIndex < 0) {
            return false;
        }

        String targetFamily = getTargetFamilyName(dyeStack);
        if (targetFamily == null) return false;

        Item resultItem = AestheticGlassRegistry.getGlassItem(targetFamily, targetShapeIndex);
        return resultItem != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        List<ItemStack> nonItems = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                nonItems.add(stack);
            }
        }

        ItemStack dyeStack = ItemStack.EMPTY;
        int glassCount = 0;
        int targetShapeIndex = -1;

        for (ItemStack stack : nonItems) {
            int shapeIndex = AestheticGlassRegistry.getShapeIndex(stack.getItem());
            if (shapeIndex >= 0) {
                if (targetShapeIndex == -1) {
                    targetShapeIndex = shapeIndex;
                }
                glassCount++;
            } else if (dyeStack.isEmpty()) {
                dyeStack = stack;
            }
        }

        if (dyeStack.isEmpty() || glassCount == 0 || targetShapeIndex < 0) {
            return ItemStack.EMPTY;
        }

        String targetFamily = getTargetFamilyName(dyeStack);
        if (targetFamily == null) return ItemStack.EMPTY;

        Item resultItem = AestheticGlassRegistry.getGlassItem(targetFamily, targetShapeIndex);
        if (resultItem == null) return ItemStack.EMPTY;

        return new ItemStack(resultItem, glassCount);
    }

    @Override
    public RecipeSerializer<GlassRedyeRecipe> getSerializer() {
        return ModRecipes.GLASS_REDYE_SERIALIZER.get();
    }

    public static String getTargetFamilyName(ItemStack dyeStack) {
        if (dyeStack.isEmpty()) return null;
        Item dyeItem = dyeStack.getItem();

        // 1. Vanilla Dye
        if (dyeItem instanceof DyeItem dye) {
            return dye.getDyeColor().getName();
        }

        // 2. Essence Item / Essence Orb
        EssenceType type = EssenceItem.getEssenceType(dyeStack);
        if (type != null) {
            String name = type.name().toLowerCase(Locale.ROOT);
            if (AestheticGlassRegistry.hasFamily("essence_" + name)) {
                return "essence_" + name;
            } else if (AestheticGlassRegistry.hasFamily(name)) {
                return name;
            }
        }

        // 3. Dyenamics Dye / Tag / ResourceLocation
        ResourceLocation loc = BuiltInRegistries.ITEM.getKey(dyeItem);
        if (loc != null) {
            String path = loc.getPath().toLowerCase(Locale.ROOT);
            if (path.endsWith("_dye")) {
                String candidate = path.substring(0, path.length() - 4);
                if (AestheticGlassRegistry.hasFamily(candidate)) {
                    return candidate;
                }
            }
            if (AestheticGlassRegistry.hasFamily(path)) {
                return path;
            }
        }
        return null;
    }

    public static class Serializer implements RecipeSerializer<GlassRedyeRecipe> {
        private final MapCodec<GlassRedyeRecipe> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, GlassRedyeRecipe> streamCodec;

        public Serializer() {
            this.codec = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    CraftingBookCategory.CODEC.fieldOf("category").forGetter(CustomRecipe::category)
            ).apply(inst, GlassRedyeRecipe::new));

            this.streamCodec = StreamCodec.composite(
                    CraftingBookCategory.STREAM_CODEC, CustomRecipe::category,
                    GlassRedyeRecipe::new
            );
        }

        @Override
        public MapCodec<GlassRedyeRecipe> codec() {
            return this.codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, GlassRedyeRecipe> streamCodec() {
            return this.streamCodec;
        }
    }
}
