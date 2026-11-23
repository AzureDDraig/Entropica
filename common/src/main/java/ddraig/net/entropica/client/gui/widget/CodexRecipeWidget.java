package ddraig.net.entropica.client.gui.widget;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.codex.CodexWikiMetadata;
import ddraig.net.entropica.codex.CodexWikiMetadata.WikiArticle;
import ddraig.net.entropica.data.CodexPlayerData;
import ddraig.net.entropica.recipe.AethericSynthesizerRecipe;
import ddraig.net.entropica.recipe.EidolicLatheRecipe;
import ddraig.net.entropica.recipe.HardcodedRecipes;
import ddraig.net.entropica.recipe.MagicCircleRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Client-safe visual recipe showcase widget rendered on the Right Page of the
 * Entropic Codex Two-Page Arcane Tome Spread. Supports 3x3 Crafting, Shapeless,
 * Smelting/Blasting, 5x5 Aetheric Synthesizer, Eidolic Lathe, and Magic Circle
 * Ritual recipes with clickable ingredient navigation.
 */
public final class CodexRecipeWidget {

    public enum RecipeKind {
        CRAFTING_3X3("Crafting Table"),
        SHAPELESS("Shapeless Crafting"),
        SMELTING("Furnace / Smelting"),
        SYNTHESIZER_5X5("5x5 Aetheric Synthesizer"),
        EIDOLIC_LATHE("Eidolic Lathe"),
        MAGIC_CIRCLE("Magic Circle Ritual");

        public final String label;

        RecipeKind(String label) {
            this.label = label;
        }
    }

    public record CodexDisplayRecipe(
            RecipeKind kind,
            int gridCols,
            int gridRows,
            List<List<ItemStack>> slots,
            List<List<ItemStack>> extraRunes,
            List<String> essenceNotes,
            int tierNote,
            ItemStack result
    ) {
    }

    private record RenderedSlotHit(int x, int y, int size, ItemStack stack) {
    }

    private static final Gson GSON = new Gson();
    private static Map<Item, List<CodexDisplayRecipe>> RECIPES_BY_OUTPUT = null;
    private static ItemStack[] ALL_REGISTRY_STACKS = null;
    private static final Map<Ingredient, List<ItemStack>> INGREDIENT_STACK_CACHE =
            Collections.synchronizedMap(new IdentityHashMap<>());

    private final List<CodexDisplayRecipe> activeRecipes = new ArrayList<>();
    private int currentIndex = 0;
    private final List<RenderedSlotHit> lastRenderedSlots = new ArrayList<>();
    private int lastPrevBtnX = -1, lastPrevBtnY = -1;
    private int lastNextBtnX = -1, lastNextBtnY = -1;
    private ItemStack pendingTooltipStack = ItemStack.EMPTY;
    private int pendingTooltipX = 0, pendingTooltipY = 0;

    public void setArticle(@Nullable WikiArticle article) {
        this.activeRecipes.clear();
        this.currentIndex = 0;
        this.lastRenderedSlots.clear();
        if (article == null || article.isLoreEntry()) {
            return;
        }

        ensureRecipesIndexed();

        // 1. Primary lookup by article's icon item (if it's an Entropica item or specific guide)
        Item iconItem = article.icon().getItem();
        ResourceLocation iconKey = BuiltInRegistries.ITEM.getKey(iconItem);
        if ("entropica".equals(iconKey.getNamespace()) && !article.isParentHub()) {
            List<CodexDisplayRecipe> direct = RECIPES_BY_OUTPUT.get(iconItem);
            if (direct != null) {
                activeRecipes.addAll(direct);
            }
        }

        // 2. Match Entropica items whose registry path matches keywords in the article ID
        String cleanId = article.id()
                .replace("materials_", "")
                .replace("machinery_", "")
                .replace("workstations_", "")
                .replace("multiblock_", "")
                .replace("magic_", "")
                .replace("env_", "");

        if (cleanId.length() >= 4 && !article.isParentHub()) {
            for (Map.Entry<Item, List<CodexDisplayRecipe>> entry : RECIPES_BY_OUTPUT.entrySet()) {
                if (activeRecipes.size() >= 8) break;
                Item item = entry.getKey();
                if (item == iconItem) continue;
                String itemPath = BuiltInRegistries.ITEM.getKey(item).getPath();
                if (itemPath.equals(cleanId) || itemPath.startsWith(cleanId + "_")) {
                    for (CodexDisplayRecipe r : entry.getValue()) {
                        if (activeRecipes.size() < 8 && !activeRecipes.contains(r)) {
                            activeRecipes.add(r);
                        }
                    }
                }
            }
        }

        // 3. Special workstation showcase fallbacks so Lathe / Ritual / Synthesizer articles show sample recipes
        if (activeRecipes.isEmpty()) {
            try {
                if (article.id().contains("lathe")) {
                    for (EidolicLatheRecipe lr : HardcodedRecipes.getLatheRecipes()) {
                        if (activeRecipes.size() >= 4) break;
                        activeRecipes.add(convertLatheRecipe(lr));
                    }
                } else if ("magic_rituals".equals(article.id())) {
                    for (MagicCircleRecipe mr : HardcodedRecipes.getMagicCircleRecipes()) {
                        if (activeRecipes.size() >= 5) break;
                        activeRecipes.add(convertMagicCircleRecipe(mr));
                    }
                }
            } catch (Throwable ignored) {
            }
        }
    }

    public boolean hasRecipes() {
        return !activeRecipes.isEmpty();
    }

    public int getRecipeCount() {
        return activeRecipes.size();
    }

    public int getPreferredHeight() {
        if (activeRecipes.isEmpty()) return 0;
        CodexDisplayRecipe cur = activeRecipes.get(currentIndex);
        return switch (cur.kind()) {
            case SYNTHESIZER_5X5 -> 118;
            case MAGIC_CIRCLE -> 112;
            case EIDOLIC_LATHE -> 98;
            default -> 88;
        };
    }

    public void render(GuiGraphics g, Font font, int x, int y, int width, int mouseX, int mouseY, CodexPlayerData playerData) {
        this.lastRenderedSlots.clear();
        this.lastPrevBtnX = -1;
        this.lastNextBtnX = -1;
        this.pendingTooltipStack = ItemStack.EMPTY;

        if (activeRecipes.isEmpty()) return;

        CodexDisplayRecipe recipe = activeRecipes.get(currentIndex);
        int boxH = getPreferredHeight();

        // Framed Arcane Card
        g.fill(x, y, x + width, y + boxH, 0xE60B1124);
        drawHollowRect(g, x, y, width, boxH, 0xFF283A5E);
        g.fill(x, y, x + width, y + 18, 0xF0131D38);
        g.fill(x, y + 17, x + width, y + 18, 0xFFD4AF37);

        // Title & Recipe Switcher (< 1/N >)
        String header = "§6🛠 " + recipe.kind().label;
        g.drawString(font, header, x + 6, y + 5, 0xFFFFD700, false);

        if (activeRecipes.size() > 1) {
            String counter = (currentIndex + 1) + "/" + activeRecipes.size();
            int cw = font.width(counter);
            int rightEdge = x + width - 6;

            this.lastNextBtnX = rightEdge - 14;
            this.lastNextBtnY = y + 3;
            this.lastPrevBtnX = this.lastNextBtnX - cw - 18;
            this.lastPrevBtnY = y + 3;

            boolean prevHov = isInside(mouseX, mouseY, lastPrevBtnX, lastPrevBtnY, 14, 12);
            boolean nextHov = isInside(mouseX, mouseY, lastNextBtnX, lastNextBtnY, 14, 12);

            g.fill(lastPrevBtnX, lastPrevBtnY, lastPrevBtnX + 14, lastPrevBtnY + 12, prevHov ? 0xFF2B4268 : 0xFF18243E);
            g.drawString(font, "<", lastPrevBtnX + 4, lastPrevBtnY + 2, prevHov ? 0xFF00E5FF : 0xFFCBD5E1, false);

            g.drawString(font, counter, lastPrevBtnX + 16, y + 5, 0xFF94A3B8, false);

            g.fill(lastNextBtnX, lastNextBtnY, lastNextBtnX + 14, lastNextBtnY + 12, nextHov ? 0xFF2B4268 : 0xFF18243E);
            g.drawString(font, ">", lastNextBtnX + 5, lastNextBtnY + 2, nextHov ? 0xFF00E5FF : 0xFFCBD5E1, false);
        }

        int bodyY = y + 23;
        long cycleTick = (System.currentTimeMillis() / 1400L);

        switch (recipe.kind()) {
            case CRAFTING_3X3, SHAPELESS -> renderGridRecipe(g, font, x + 10, bodyY, 3, 3, 19, recipe, cycleTick, mouseX, mouseY);
            case SMELTING -> renderSmeltingRecipe(g, font, x + 14, bodyY + 6, recipe, cycleTick, mouseX, mouseY);
            case SYNTHESIZER_5X5 -> renderGridRecipe(g, font, x + 8, bodyY - 1, 5, 5, 17, recipe, cycleTick, mouseX, mouseY);
            case EIDOLIC_LATHE -> renderLatheRecipe(g, font, x + 8, bodyY, recipe, cycleTick, mouseX, mouseY);
            case MAGIC_CIRCLE -> renderMagicCircleRecipe(g, font, x + 8, bodyY, width - 16, recipe, cycleTick, mouseX, mouseY);
        }
    }

    public void renderDeferredTooltip(GuiGraphics g, Font font, CodexPlayerData playerData) {
        if (pendingTooltipStack.isEmpty()) return;

        Component nameComp = pendingTooltipStack.getHoverName();
        WikiArticle linked = CodexWikiMetadata.findArticleForItem(pendingTooltipStack, playerData);

        String line2 = (linked != null) ? "§b✦ Click to open Wiki: §f" + linked.title() : null;
        int w = font.width(nameComp);
        if (line2 != null) {
            w = Math.max(w, font.width(line2));
        }
        int h = (line2 != null) ? 24 : 14;
        int screenW = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int tx = Math.min(screenW - w - 12, Math.max(6, pendingTooltipX + 10));
        int ty = Math.max(6, pendingTooltipY - h - 4);

        g.fill(tx - 3, ty - 3, tx + w + 4, ty + h + 2, 0xF4080C1A);
        drawHollowRect(g, tx - 3, ty - 3, w + 7, h + 5, 0xFF38BDF8);
        g.drawString(font, nameComp, tx, ty, 0xFFFFFFFF, true);
        if (line2 != null) {
            g.drawString(font, line2, tx, ty + 11, 0xFF7DD3FC, false);
        }
    }

    /**
     * Handles clicks on recipe cycle arrows or ingredient/result item slots.
     *
     * @return target WikiArticle if an item with a matching Codex article was clicked, else null.
     */
    @Nullable
    public WikiArticle handleMouseClicked(double mouseX, double mouseY, CodexPlayerData playerData, Runnable onUiHandled) {
        if (activeRecipes.isEmpty()) return null;

        if (activeRecipes.size() > 1) {
            if (isInside(mouseX, mouseY, lastPrevBtnX, lastPrevBtnY, 14, 12)) {
                currentIndex = (currentIndex - 1 + activeRecipes.size()) % activeRecipes.size();
                if (onUiHandled != null) onUiHandled.run();
                return null;
            }
            if (isInside(mouseX, mouseY, lastNextBtnX, lastNextBtnY, 14, 12)) {
                currentIndex = (currentIndex + 1) % activeRecipes.size();
                if (onUiHandled != null) onUiHandled.run();
                return null;
            }
        }

        for (RenderedSlotHit slot : lastRenderedSlots) {
            if (!slot.stack().isEmpty() && isInside(mouseX, mouseY, slot.x(), slot.y(), slot.size(), slot.size())) {
                WikiArticle target = CodexWikiMetadata.findArticleForItem(slot.stack(), playerData);
                if (target != null) {
                    return target;
                }
            }
        }
        return null;
    }

    private void renderGridRecipe(GuiGraphics g, Font font, int startX, int startY, int cols, int rows, int step,
                                  CodexDisplayRecipe recipe, long cycleTick, int mouseX, int mouseY) {
        int slotSize = step - 1;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int idx = r * cols + c;
                List<ItemStack> options = (idx < recipe.slots().size()) ? recipe.slots().get(idx) : List.of();
                ItemStack stack = pickCycledStack(options, cycleTick + idx);
                drawItemSlot(g, font, startX + c * step, startY + r * step, slotSize, stack, false, mouseX, mouseY);
            }
        }

        int gridW = cols * step;
        int arrowX = startX + gridW + 8;
        int midY = startY + (rows * step) / 2 - 4;
        g.drawString(font, "§b→", arrowX, midY, 0xFF38BDF8, false);

        int outX = arrowX + 16;
        int outY = startY + (rows * step) / 2 - 11;
        drawItemSlot(g, font, outX, outY, 22, recipe.result(), true, mouseX, mouseY);
    }

    private void renderSmeltingRecipe(GuiGraphics g, Font font, int startX, int startY,
                                      CodexDisplayRecipe recipe, long cycleTick, int mouseX, int mouseY) {
        ItemStack inStack = recipe.slots().isEmpty() ? ItemStack.EMPTY : pickCycledStack(recipe.slots().get(0), cycleTick);
        drawItemSlot(g, font, startX, startY, 20, inStack, false, mouseX, mouseY);
        g.drawString(font, "§6🔥 →", startX + 26, startY + 6, 0xFFFFAA00, false);
        drawItemSlot(g, font, startX + 56, startY - 1, 22, recipe.result(), true, mouseX, mouseY);
    }

    private void renderLatheRecipe(GuiGraphics g, Font font, int startX, int startY,
                                   CodexDisplayRecipe recipe, long cycleTick, int mouseX, int mouseY) {
        String[] labels = {"Shape", "Core", "Orbis", "Metal"};
        for (int i = 0; i < Math.min(4, recipe.slots().size()); i++) {
            int sx = startX + i * 24;
            ItemStack st = pickCycledStack(recipe.slots().get(i), cycleTick + i);
            drawItemSlot(g, font, sx, startY + 2, 18, st, false, mouseX, mouseY);
            g.drawString(font, "§8" + labels[i], sx, startY + 23, 0xFF94A3B8, false);
        }

        g.drawString(font, "§b→", startX + 100, startY + 7, 0xFF38BDF8, false);
        drawItemSlot(g, font, startX + 114, startY, 22, recipe.result(), true, mouseX, mouseY);

        if (!recipe.extraRunes().isEmpty()) {
            g.drawString(font, "§7Modifiers:", startX, startY + 40, 0xFF94A3B8, false);
            for (int m = 0; m < Math.min(4, recipe.extraRunes().size()); m++) {
                ItemStack mod = pickCycledStack(recipe.extraRunes().get(m), cycleTick + m);
                drawItemSlot(g, font, startX + 54 + m * 20, startY + 35, 18, mod, false, mouseX, mouseY);
            }
        }
    }

    private void renderMagicCircleRecipe(GuiGraphics g, Font font, int startX, int startY, int availW,
                                         CodexDisplayRecipe recipe, long cycleTick, int mouseX, int mouseY) {
        g.drawString(font, "§dChalk Tier " + recipe.tierNote() + "  §8|  §7Inputs:", startX, startY, 0xFFE879F9, false);

        int inY = startY + 12;
        int count = Math.min(5, recipe.slots().size());
        for (int i = 0; i < count; i++) {
            ItemStack st = pickCycledStack(recipe.slots().get(i), cycleTick + i);
            drawItemSlot(g, font, startX + i * 20, inY, 18, st, false, mouseX, mouseY);
        }

        int arrowX = startX + Math.max(1, count) * 20 + 4;
        g.drawString(font, "§d→", arrowX, inY + 5, 0xFFE879F9, false);
        drawItemSlot(g, font, arrowX + 13, inY - 2, 22, recipe.result(), true, mouseX, mouseY);

        int runeY = inY + 24;
        if (!recipe.extraRunes().isEmpty()) {
            g.drawString(font, "§bRunes:", startX, runeY + 4, 0xFF7DD3FC, false);
            for (int r = 0; r < Math.min(4, recipe.extraRunes().size()); r++) {
                ItemStack rn = pickCycledStack(recipe.extraRunes().get(r), cycleTick + r);
                drawItemSlot(g, font, startX + 36 + r * 20, runeY, 18, rn, false, mouseX, mouseY);
            }
        }

        if (!recipe.essenceNotes().isEmpty()) {
            String essStr = "§7Materia: §e" + String.join("§7, §e", recipe.essenceNotes());
            if (font.width(essStr) > availW) {
                essStr = essStr.substring(0, Math.min(essStr.length(), 38)) + "...";
            }
            g.drawString(font, essStr, startX, runeY + 21, 0xFFFDE047, false);
        }
    }

    private void drawItemSlot(GuiGraphics g, Font font, int x, int y, int size, ItemStack stack,
                              boolean highlightOutput, int mouseX, int mouseY) {
        boolean hov = isInside(mouseX, mouseY, x, y, size, size);
        int bg = hov ? 0xFF223354 : (highlightOutput ? 0xFF162440 : 0xFF10182C);
        int border = hov ? 0xFF00E5FF : (highlightOutput ? 0xFFD4AF37 : 0xFF2B3B59);

        g.fill(x, y, x + size, y + size, bg);
        drawHollowRect(g, x, y, size, size, border);

        if (!stack.isEmpty()) {
            int pad = (size - 16) / 2;
            g.renderItem(stack, x + pad, y + pad);
            g.renderItemDecorations(font, stack, x + pad, y + pad);
            lastRenderedSlots.add(new RenderedSlotHit(x, y, size, stack));
            if (hov) {
                this.pendingTooltipStack = stack;
                this.pendingTooltipX = mouseX;
                this.pendingTooltipY = mouseY;
            }
        }
    }

    private static ItemStack pickCycledStack(List<ItemStack> options, long tick) {
        if (options == null || options.isEmpty()) return ItemStack.EMPTY;
        int idx = (int) (Math.abs(tick) % options.size());
        return options.get(idx);
    }

    private static boolean isInside(double mx, double my, int x, int y, int w, int h) {
        return x >= 0 && mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static void drawHollowRect(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    // =========================================================================
    // CLIENT-SAFE BUNDLED RECIPE & HARDCODED RECIPE INDEXER
    // =========================================================================

    private static synchronized void ensureRecipesIndexed() {
        if (RECIPES_BY_OUTPUT != null) return;
        Map<Item, List<CodexDisplayRecipe>> map = new HashMap<>();

        // 1. Scan bundled /data/entropica/recipe/*.json files from classpath
        try {
            List<String> jsonPaths = discoverBundledRecipeJsonPaths();
            for (String resourcePath : jsonPaths) {
                try (InputStream in = CodexRecipeWidget.class.getResourceAsStream(resourcePath)) {
                    if (in == null) continue;
                    try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                        JsonObject root = GSON.fromJson(reader, JsonObject.class);
                        CodexDisplayRecipe parsed = parseRecipeJson(root);
                        if (parsed != null && !parsed.result().isEmpty()) {
                            map.computeIfAbsent(parsed.result().getItem(), k -> new ArrayList<>()).add(parsed);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }

        // 2. Index Hardcoded 5x5 Synthesizer Recipes
        try {
            for (AethericSynthesizerRecipe syn : HardcodedRecipes.getSynthesizerRecipes()) {
                if (syn.getResults().isEmpty()) continue;
                ItemStack out = syn.getResults().get(0);
                if (out.isEmpty()) continue;

                List<List<ItemStack>> grid = new ArrayList<>(25);
                NonNullList<Optional<Ingredient>> ings = syn.getGridIngredients();
                for (int i = 0; i < 25; i++) {
                    Optional<Ingredient> opt = i < ings.size() ? ings.get(i) : Optional.empty();
                    grid.add(opt.map(CodexRecipeWidget::expandIngredient).orElse(List.of()));
                }
                CodexDisplayRecipe disp = new CodexDisplayRecipe(
                        RecipeKind.SYNTHESIZER_5X5, 5, 5, grid, List.of(), List.of(), 0, out.copy()
                );
                map.computeIfAbsent(out.getItem(), k -> new ArrayList<>()).add(disp);
            }
        } catch (Throwable ignored) {
        }

        // 3. Index Hardcoded Eidolic Lathe Recipes
        try {
            for (EidolicLatheRecipe lr : HardcodedRecipes.getLatheRecipes()) {
                CodexDisplayRecipe disp = convertLatheRecipe(lr);
                if (!disp.result().isEmpty()) {
                    map.computeIfAbsent(disp.result().getItem(), k -> new ArrayList<>()).add(disp);
                }
            }
        } catch (Throwable ignored) {
        }

        // 4. Index Hardcoded Magic Circle Ritual Recipes
        try {
            for (MagicCircleRecipe mr : HardcodedRecipes.getMagicCircleRecipes()) {
                CodexDisplayRecipe disp = convertMagicCircleRecipe(mr);
                if (!disp.result().isEmpty()) {
                    map.computeIfAbsent(disp.result().getItem(), k -> new ArrayList<>()).add(disp);
                }
            }
        } catch (Throwable ignored) {
        }

        RECIPES_BY_OUTPUT = map;
    }

    private static CodexDisplayRecipe convertLatheRecipe(EidolicLatheRecipe lr) {
        List<List<ItemStack>> mainSlots = List.of(
                expandIngredient(lr.etherealShape()),
                expandIngredient(lr.core()),
                expandIngredient(lr.orbisAcceptor()),
                expandIngredient(lr.baseMaterial())
        );
        List<List<ItemStack>> modSlots = new ArrayList<>();
        for (Ingredient mod : lr.modifiers()) {
            modSlots.add(expandIngredient(mod));
        }
        return new CodexDisplayRecipe(
                RecipeKind.EIDOLIC_LATHE, 4, 1, mainSlots, modSlots, List.of(), 0, lr.result().copy()
        );
    }

    private static CodexDisplayRecipe convertMagicCircleRecipe(MagicCircleRecipe mr) {
        List<List<ItemStack>> inSlots = new ArrayList<>();
        for (Ingredient ing : mr.inputs()) {
            inSlots.add(expandIngredient(ing));
        }
        List<List<ItemStack>> runeSlots = new ArrayList<>();
        for (Ingredient r : mr.runes()) {
            runeSlots.add(expandIngredient(r));
        }
        List<String> essences = new ArrayList<>();
        for (Map.Entry<EssenceType, Integer> e : mr.essences().entrySet()) {
            if (e.getValue() > 0) {
                essences.add(e.getValue() + " " + e.getKey().name());
            }
        }
        return new CodexDisplayRecipe(
                RecipeKind.MAGIC_CIRCLE, inSlots.size(), 1, inSlots, runeSlots, essences, mr.tier(), mr.output().copy()
        );
    }

    private static List<ItemStack> expandIngredient(Ingredient ingredient) {
        if (ingredient == null || ingredient.isEmpty()) return List.of();
        List<ItemStack> cached = INGREDIENT_STACK_CACHE.get(ingredient);
        if (cached != null) return cached;

        if (ALL_REGISTRY_STACKS == null) {
            ALL_REGISTRY_STACKS = BuiltInRegistries.ITEM.stream()
                    .filter(i -> i != Items.AIR)
                    .map(ItemStack::new)
                    .toArray(ItemStack[]::new);
        }

        List<ItemStack> matches = new ArrayList<>();
        for (ItemStack candidate : ALL_REGISTRY_STACKS) {
            try {
                if (ingredient.test(candidate)) {
                    matches.add(candidate);
                    if (matches.size() >= 12) break;
                }
            } catch (Exception ignored) {
            }
        }
        List<ItemStack> immutable = Collections.unmodifiableList(matches);
        INGREDIENT_STACK_CACHE.put(ingredient, immutable);
        return immutable;
    }

    private static List<String> discoverBundledRecipeJsonPaths() {
        List<String> out = new ArrayList<>();
        String baseDir = "data/entropica/recipe";
        try {
            Enumeration<URL> urls = CodexRecipeWidget.class.getClassLoader().getResources(baseDir);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    Path root = Paths.get(url.toURI());
                    if (Files.exists(root)) {
                        try (Stream<Path> walk = Files.walk(root, 3)) {
                            walk.filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                                String rel = root.relativize(p).toString().replace('\\', '/');
                                out.add("/" + baseDir + "/" + rel);
                            });
                        }
                    }
                } else if ("jar".equals(protocol)) {
                    JarURLConnection conn = (JarURLConnection) url.openConnection();
                    JarFile jar = conn.getJarFile();
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        String name = entries.nextElement().getName();
                        if (name.startsWith(baseDir + "/") && name.endsWith(".json")) {
                            out.add("/" + name);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return out;
    }

    @Nullable
    private static CodexDisplayRecipe parseRecipeJson(JsonObject root) {
        if (root == null || !root.has("type")) return null;
        String type = root.get("type").getAsString();
        ItemStack result = parseResultStack(root.get("result"));
        if (result.isEmpty()) return null;

        if (type.endsWith("crafting_shaped") && root.has("pattern") && root.has("key")) {
            JsonArray patArr = root.getAsJsonArray("pattern");
            JsonObject keyObj = root.getAsJsonObject("key");
            List<List<ItemStack>> grid = new ArrayList<>(9);
            for (int r = 0; r < 3; r++) {
                String row = r < patArr.size() ? patArr.get(r).getAsString() : "";
                for (int c = 0; c < 3; c++) {
                    char ch = c < row.length() ? row.charAt(c) : ' ';
                    if (ch != ' ' && keyObj.has(String.valueOf(ch))) {
                        grid.add(parseJsonIngredient(keyObj.get(String.valueOf(ch))));
                    } else {
                        grid.add(List.of());
                    }
                }
            }
            return new CodexDisplayRecipe(RecipeKind.CRAFTING_3X3, 3, 3, grid, List.of(), List.of(), 0, result);
        }

        if (type.endsWith("crafting_shapeless") && root.has("ingredients")) {
            JsonArray ingArr = root.getAsJsonArray("ingredients");
            List<List<ItemStack>> grid = new ArrayList<>(9);
            for (int i = 0; i < 9; i++) {
                if (i < ingArr.size()) {
                    grid.add(parseJsonIngredient(ingArr.get(i)));
                } else {
                    grid.add(List.of());
                }
            }
            return new CodexDisplayRecipe(RecipeKind.SHAPELESS, 3, 3, grid, List.of(), List.of(), 0, result);
        }

        if ((type.endsWith("smelting") || type.endsWith("blasting")) && root.has("ingredient")) {
            List<ItemStack> in = parseJsonIngredient(root.get("ingredient"));
            return new CodexDisplayRecipe(RecipeKind.SMELTING, 1, 1, List.of(in), List.of(), List.of(), 0, result);
        }

        return null;
    }

    private static ItemStack parseResultStack(@Nullable JsonElement el) {
        if (el == null || el.isJsonNull()) return ItemStack.EMPTY;
        if (el.isJsonPrimitive()) {
            Item item = resolveItem(el.getAsString());
            return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
        }
        if (el.isJsonObject()) {
            JsonObject obj = el.getAsJsonObject();
            String id = obj.has("id") ? obj.get("id").getAsString()
                    : (obj.has("item") ? obj.get("item").getAsString() : "");
            int count = obj.has("count") ? obj.get("count").getAsInt() : 1;
            Item item = resolveItem(id);
            return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item, Math.max(1, count));
        }
        return ItemStack.EMPTY;
    }

    private static List<ItemStack> parseJsonIngredient(@Nullable JsonElement el) {
        if (el == null || el.isJsonNull()) return List.of();
        List<ItemStack> out = new ArrayList<>();

        if (el.isJsonPrimitive()) {
            String raw = el.getAsString();
            addResolvedSpec(raw, out);
        } else if (el.isJsonObject()) {
            JsonObject obj = el.getAsJsonObject();
            if (obj.has("item")) addResolvedSpec(obj.get("item").getAsString(), out);
            else if (obj.has("id")) addResolvedSpec(obj.get("id").getAsString(), out);
            else if (obj.has("tag")) addResolvedSpec("#" + obj.get("tag").getAsString(), out);
        } else if (el.isJsonArray()) {
            for (JsonElement sub : el.getAsJsonArray()) {
                out.addAll(parseJsonIngredient(sub));
            }
        }
        return out;
    }

    private static void addResolvedSpec(String spec, List<ItemStack> out) {
        if (spec == null || spec.isBlank()) return;
        if (spec.startsWith("#")) {
            ResourceLocation tagId = ResourceLocation.tryParse(spec.substring(1));
            if (tagId != null) {
                TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagId);
                BuiltInRegistries.ITEM.getTagOrEmpty(tagKey).forEach(holder -> {
                    if (out.size() < 10 && holder.value() != Items.AIR) {
                        out.add(new ItemStack(holder.value()));
                    }
                });
                // Fallback if client tags aren't bound yet
                if (out.isEmpty()) {
                    String path = tagId.getPath().toLowerCase(Locale.ROOT);
                    if (path.contains("planks")) out.add(new ItemStack(Items.OAK_PLANKS));
                    else if (path.contains("logs")) out.add(new ItemStack(Items.OAK_LOG));
                    else if (path.contains("glass")) out.add(new ItemStack(Items.GLASS));
                    else if (path.contains("ingots/iron")) out.add(new ItemStack(Items.IRON_INGOT));
                    else if (path.contains("ingots/copper")) out.add(new ItemStack(Items.COPPER_INGOT));
                    else if (path.contains("ingots/gold")) out.add(new ItemStack(Items.GOLD_INGOT));
                }
            }
        } else {
            Item item = resolveItem(spec);
            if (item != Items.AIR) {
                out.add(new ItemStack(item));
            }
        }
    }

    private static Item resolveItem(String idStr) {
        if (idStr == null || idStr.isBlank()) return Items.AIR;
        ResourceLocation loc = ResourceLocation.tryParse(idStr);
        if (loc == null) return Items.AIR;
        return BuiltInRegistries.ITEM.getOptional(loc).orElse(Items.AIR);
    }
}
