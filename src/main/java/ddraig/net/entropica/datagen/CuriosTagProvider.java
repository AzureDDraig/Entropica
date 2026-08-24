package ddraig.net.entropica.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.concurrent.CompletableFuture;

// Extending TagsProvider<Item> bypasses the missing ItemTagsProvider class
public class CuriosTagProvider extends TagsProvider<Item> {

    public CuriosTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        // Passing Registries.ITEM tells the TagsProvider what type of tags we are building
        super(output, Registries.ITEM, lookupProvider, "entropica");
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // ==========================================
        // CURIOS API TAGS
        // ==========================================
        TagKey<Item> CURIOS_BELT = ItemTags.create(ResourceLocation.fromNamespaceAndPath("curios", "belt"));
        TagKey<Item> CURIOS_HEAD = ItemTags.create(ResourceLocation.fromNamespaceAndPath("curios", "head"));
        TagKey<Item> CURIOS_HANDS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("curios", "hands"));
        TagKey<Item> CURIOS_CHARM = ItemTags.create(ResourceLocation.fromNamespaceAndPath("curios", "charm"));

        // Spools -> Belt
        // Using getOrCreateRawBuilder safely initializes the empty JSON tag file!
        // (When you want to add items later, you can append: .addOptionalElement(ModItems.YOUR_ITEM.getId()))
        this.getOrCreateRawBuilder(CURIOS_BELT);

        // Monocle -> Head
        this.getOrCreateRawBuilder(CURIOS_HEAD);

        // Focuses -> Hands
        this.getOrCreateRawBuilder(CURIOS_HANDS);

        // Orbis Cell -> Charm
        this.getOrCreateRawBuilder(CURIOS_CHARM);
    }
}