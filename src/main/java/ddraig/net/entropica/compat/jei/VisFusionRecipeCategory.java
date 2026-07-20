package ddraig.net.entropica.compat.jei;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.recipe.FusionRecipe;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

@SuppressWarnings("removal")
public class VisFusionRecipeCategory implements IRecipeCategory<FusionRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vis_fusion");
    public static final RecipeType<FusionRecipe> TYPE = new RecipeType<>(UID, FusionRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    public VisFusionRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(160, 60);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.ENTROPIC_CORE.get()));
        this.title = Component.literal("Entropic Core: Vis Fusion");
    }

    @Override
    public RecipeType<FusionRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FusionRecipe recipe, IFocusGroup focuses) {
        int inputX = 5;
        int inputY = 5;

        for (Map.Entry<EssenceType, Integer> entry : recipe.inputs().entrySet()) {
            ItemStack inputOrb = new ItemStack(ModItems.WEAK_ESSENCE.get(), entry.getValue());
            EssenceItem.setEssenceType(inputOrb, entry.getKey());

            builder.addSlot(RecipeIngredientRole.INPUT, inputX, inputY)
                    .addItemStack(inputOrb)
                    .addTooltipCallback((recipeSlotView, tooltip) -> {
                        tooltip.add(Component.literal("§7Requires §f" + entry.getValue() + " §7" + entry.getKey().getDisplayName() + " Essence"));
                    });

            inputX += 18;
            if (inputX > 41) {
                inputX = 5;
                inputY += 18;
            }
        }

        if (recipe.success() != null) {
            ItemStack successOrb = new ItemStack(ModItems.WEAK_ESSENCE.get(), recipe.yield());
            EssenceItem.setEssenceType(successOrb, recipe.success());

            builder.addSlot(RecipeIngredientRole.OUTPUT, 100, 10)
                    .addItemStack(successOrb)
                    .addTooltipCallback((recipeSlotView, tooltip) -> {
                        tooltip.add(Component.literal("§aSuccess Yield: §f" + recipe.yield() + " §a" + recipe.success().getDisplayName() + " Materia"));
                    });
        }

        if (recipe.failure() != null) {
            ItemStack failureOrb = new ItemStack(ModItems.WEAK_ESSENCE.get(), recipe.yield());
            EssenceItem.setEssenceType(failureOrb, recipe.failure());

            builder.addSlot(RecipeIngredientRole.OUTPUT, 100, 35)
                    .addItemStack(failureOrb)
                    .addTooltipCallback((recipeSlotView, tooltip) -> {
                        tooltip.add(Component.literal("§cFailure Yield: §f" + recipe.yield() + " §c" + recipe.failure().getDisplayName() + " Materia"));
                    });
        }
    }

    @Override
    public void draw(FusionRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;

        String chanceText = "Chance: " + (int)(recipe.chance() * 100) + "%";
        guiGraphics.drawString(font, chanceText, 50, 15, 0xFFFFFFFF, false);

        if (recipe.requiresCatalyst()) {
            guiGraphics.drawString(font, "Needs Prismatic Catalyst", 50, 27, 0xFFFF5555, false);
        }

        guiGraphics.drawString(font, "Success", 120, 14, 0xFF55FF55, false);

        if (recipe.failure() != null) {
            guiGraphics.drawString(font, "Failure", 120, 39, 0xFFFF5555, false);
        } else {
            guiGraphics.drawString(font, "No Output", 100, 39, 0xFF888888, false);
        }
    }
}