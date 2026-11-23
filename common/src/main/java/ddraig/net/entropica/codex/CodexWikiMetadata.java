package ddraig.net.entropica.codex;

import ddraig.net.entropica.codex.CodexCategoryRegistry.CodexNode;
import ddraig.net.entropica.data.CelestialJournalData;
import ddraig.net.entropica.data.CodexPlayerData;
import ddraig.net.entropica.event.CodexCraftingLockHandler;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Organizes all Entropic Codex nodes and the 13 Scholar's Journal Lore volumes
 * into sub-folders, concentric progression ring constellations (Option 5),
 * Mob Bestiary discovery states, and cross-category wiki links.
 */
public final class CodexWikiMetadata {

    public static final String CAT_GETTING_STARTED = "GETTING STARTED";
    public static final String CAT_MATERIALS = "MATERIALS";
    public static final String CAT_MATERIA = "MATERIA";
    public static final String CAT_MACHINERY = "MACHINERY";
    public static final String CAT_MULTIBLOCKS = "MULTIBLOCKS";
    public static final String CAT_ENVIRONMENT = "ENVIRONMENT & NATURE";
    public static final String CAT_MAGIC = "MAGIC";
    public static final String CAT_LORE = "LORE";

    public static final List<String> RING_CATEGORIES = List.of(
            CAT_MATERIALS,
            CAT_MATERIA,
            CAT_MACHINERY,
            CAT_MULTIBLOCKS,
            CAT_ENVIRONMENT,
            CAT_MAGIC
    );

    public static final List<String> ALL_WIKI_CATEGORIES = List.of(
            CAT_GETTING_STARTED,
            CAT_MATERIALS,
            CAT_MATERIA,
            CAT_MACHINERY,
            CAT_MULTIBLOCKS,
            CAT_ENVIRONMENT,
            CAT_MAGIC,
            CAT_LORE
    );

    public enum StarState {
        SHINING,
        PARTIAL,
        DIM,
        MYSTERY
    }

    public record WikiArticle(
            String id,
            String title,
            String category,
            String subFolder,
            String summary,
            String content,
            @Nullable String prerequisiteId,
            ItemStack icon,
            boolean isParentHub,
            boolean isLoreEntry,
            int loreVolume,
            int loreEntryIndex,
            int branchColor,
            List<String> mobEntityIds,
            @Nullable CodexNode backingNode
    ) {
        public boolean isMobBestiaryEntry() {
            return !mobEntityIds.isEmpty();
        }
    }

    public record StarPlacement(
            WikiArticle article,
            int ringIndex,      // 0 = Center, 1 = Inner, 2 = Middle, 3 = Outer
            float normX,        // -1.0 .. +1.0 relative to chart ellipse radius
            float normY,        // -1.0 .. +1.0 relative to chart ellipse radius
            @Nullable String connectToId
    ) {
    }

    public record ConstellationChartPage(
            int pageIndex,
            int totalPages,
            List<StarPlacement> stars
    ) {
    }

    public record ConstellationSubGroup(
            String id,
            String tabTitle,
            int accentColor,
            List<ConstellationChartPage> pages
    ) {
    }

    private static Map<String, WikiArticle> ARTICLES_BY_ID = null;
    private static List<WikiArticle> ALL_ARTICLES = null;

    private CodexWikiMetadata() {
    }

    public static synchronized void ensureInitialized() {
        if (ARTICLES_BY_ID != null) {
            return;
        }
        initializeFromNodes(CodexCategoryRegistry.ALL_NODES);
    }

    public static synchronized void initializeFromNodes(List<CodexNode> sourceNodes) {
        Map<String, WikiArticle> map = new LinkedHashMap<>();
        List<WikiArticle> list = new ArrayList<>();

        // 1. Wrap all standard CodexCategoryRegistry nodes
        for (CodexNode node : sourceNodes) {
            String subFolder = classifySubFolder(node);
            int branchCol = resolveBranchColor(node.category, subFolder, node.id);
            List<String> mobIds = resolveMobEntityIds(node);

            WikiArticle article = new WikiArticle(
                    node.id,
                    node.title,
                    node.category,
                    subFolder,
                    node.summary,
                    node.content,
                    node.prerequisiteId,
                    node.icon == null ? new ItemStack(Items.BOOK) : node.icon,
                    node.isParentHub,
                    false,
                    0,
                    -1,
                    branchCol,
                    mobIds,
                    node
            );
            map.put(article.id(), article);
            list.add(article);
        }

        // 2. Build Lore chapter entries from all 13 Scholar's Journals (Volumes I - XIII)
        for (int volNum = 1; volNum <= CelestialJournalData.TOTAL_VOLUMES; volNum++) {
            CelestialJournalData.JournalVolume vol = CelestialJournalData.getVolume(volNum);
            String folderName = "Vol. " + vol.roman() + " — " + vol.shortTitle();
            ItemStack journalIcon = getJournalVolumeIcon(volNum);
            int accent = 0xFF000000 | (vol.guiAccentColor() & 0x00FFFFFF);

            List<CelestialJournalData.JournalEntry> entries = vol.entries();
            for (int idx = 0; idx < entries.size(); idx++) {
                CelestialJournalData.JournalEntry entry = entries.get(idx);
                String id = CodexPlayerData.toLoreNodeId(volNum, idx);
                String prevId = (idx == 0) ? null : CodexPlayerData.toLoreNodeId(volNum, idx - 1);

                String snippet = extractLoreSummary(vol, entry);
                String plainSearchBody = flattenLoreText(entry);

                WikiArticle loreArticle = new WikiArticle(
                        id,
                        entry.title(),
                        CAT_LORE,
                        folderName,
                        snippet,
                        plainSearchBody,
                        prevId,
                        journalIcon,
                        idx == 0,
                        true,
                        volNum,
                        idx,
                        accent,
                        List.of(),
                        null
                );
                map.put(id, loreArticle);
                list.add(loreArticle);
            }
        }

        ARTICLES_BY_ID = Collections.unmodifiableMap(map);
        ALL_ARTICLES = Collections.unmodifiableList(list);
    }

    public static List<WikiArticle> getAllArticles() {
        ensureInitialized();
        return ALL_ARTICLES;
    }

    @Nullable
    public static WikiArticle getArticleIfInitialized(String id) {
        if (id == null || ARTICLES_BY_ID == null) return null;
        return ARTICLES_BY_ID.get(id);
    }

    @Nullable
    public static WikiArticle getArticle(String id) {
        if (id == null) return null;
        ensureInitialized();
        return ARTICLES_BY_ID.get(id);
    }

    public static List<WikiArticle> getArticlesForCategory(String category) {
        ensureInitialized();
        List<WikiArticle> out = new ArrayList<>();
        for (WikiArticle a : ALL_ARTICLES) {
            if (a.category().equals(category)) {
                out.add(a);
            }
        }
        return out;
    }

    public static Map<String, List<WikiArticle>> getSubFolderTree(String category) {
        ensureInitialized();
        Map<String, List<WikiArticle>> grouped = new LinkedHashMap<>();
        for (WikiArticle a : ALL_ARTICLES) {
            if (a.category().equals(category)) {
                grouped.computeIfAbsent(a.subFolder(), k -> new ArrayList<>()).add(a);
            }
        }
        return grouped;
    }

    public static int getTotalLoreEntryCount() {
        int total = 0;
        for (int v = 1; v <= CelestialJournalData.TOTAL_VOLUMES; v++) {
            total += CelestialJournalData.getVolume(v).entries().size();
        }
        return total;
    }

    public static StarState getStarState(WikiArticle article, CodexPlayerData data) {
        if (article == null) return StarState.DIM;

        // 1. Lore Journal Entries: Entry-by-entry archival!
        if (article.isLoreEntry()) {
            return data.isLoreEntryArchived(article.loreVolume(), article.loreEntryIndex())
                    ? StarState.SHINING
                    : StarState.MYSTERY;
        }

        // 2. Mob Bestiary Entries: Unseen -> MYSTERY, Spotted -> PARTIAL, Defeated/Tamed -> SHINING
        if (article.isMobBestiaryEntry()) {
            boolean anyFull = false;
            boolean anyObs = false;
            for (String mobId : article.mobEntityIds()) {
                if (data.isMobFullyUnlocked(mobId)) {
                    anyFull = true;
                    break;
                }
                if (data.isMobObserved(mobId)) {
                    anyObs = true;
                }
            }
            if (anyFull) return StarState.SHINING;
            if (anyObs) return StarState.PARTIAL;
            return StarState.MYSTERY;
        }

        // 3. Standard Guide Entries: Open Wiki + Shining Trail!
        if (article.isParentHub() || data.isStarShining(article.id())) {
            return StarState.SHINING;
        }
        return StarState.DIM;
    }

    public static boolean canReadArticle(WikiArticle article, CodexPlayerData data) {
        if (article == null) return false;
        StarState state = getStarState(article, data);
        return state != StarState.MYSTERY;
    }

    public static String getDisplayTitle(WikiArticle article, CodexPlayerData data) {
        if (article == null) return "";
        if (getStarState(article, data) == StarState.MYSTERY) {
            return "???";
        }
        return article.title();
    }

    public static int countShiningStars(String category, CodexPlayerData data) {
        int lit = 0;
        for (WikiArticle a : getArticlesForCategory(category)) {
            if (getStarState(a, data) == StarState.SHINING) {
                lit++;
            }
        }
        return lit;
    }

    public static int getCategoryColor(String category) {
        return switch (category) {
            case CAT_GETTING_STARTED -> 0xFF22E55E;
            case CAT_MATERIALS -> 0xFFFFA500;
            case CAT_MATERIA -> 0xFF00E5FF;
            case CAT_MACHINERY -> 0xFFFF4444;
            case CAT_MULTIBLOCKS -> 0xFFFFD700;
            case CAT_ENVIRONMENT -> 0xFF38F25B;
            case CAT_MAGIC -> 0xFFC04CFD;
            case CAT_LORE -> 0xFFF59E0B;
            default -> 0xFF00D9FF;
        };
    }

    public static String getCategoryHubId(String category) {
        return switch (category) {
            case CAT_GETTING_STARTED -> "hub_getting_started";
            case CAT_MATERIALS -> "hub_materials";
            case CAT_MATERIA -> "hub_materia";
            case CAT_MACHINERY -> "hub_machinery";
            case CAT_MULTIBLOCKS -> "hub_multiblocks";
            case CAT_ENVIRONMENT -> "hub_environment";
            case CAT_MAGIC -> "hub_magic";
            case CAT_LORE -> "lore_vol1_0";
            default -> "hub_getting_started";
        };
    }

    public static String getCategoryShortDescription(String category) {
        return switch (category) {
            case CAT_GETTING_STARTED -> "Technomantic Primer, Arkanist Monocle, and foundational Crucible extraction.";
            case CAT_MATERIALS -> "Elemental ores, budding crystal geodes, indigenous hides, amber, and tinted glass.";
            case CAT_MATERIA -> "Essence Tiers T1–T6, pneumatic pipes, pressure valves, and fluid routing.";
            case CAT_MACHINERY -> "Eidolic Lathes, Crucibles, Gravitational Anchors, and Graviton Bouncepads.";
            case CAT_MULTIBLOCKS -> "Runic Scribing Engine, Modular Astral Altars, and Fission Reactors.";
            case CAT_ENVIRONMENT -> "12+ elemental biomes, botanical flora, and the living Mob Bestiary.";
            case CAT_MAGIC -> "Magic Circles, 16-gate Circuits, 6-ring Spellcrafting, Forcefields, and Astral Optics.";
            case CAT_LORE -> "The 13 Scholar's Journals of Mount Aetheria — archived chapter-by-chapter as you read.";
            default -> "Entropic Codex knowledge chapter.";
        };
    }

    public static ItemStack getJournalVolumeIcon(int volumeNumber) {
        try {
            Item item = switch (volumeNumber) {
                case 1 -> ModItems.CELESTIAL_JOURNAL_VOL1.get();
                case 2 -> ModItems.CELESTIAL_JOURNAL_VOL2.get();
                case 3 -> ModItems.CELESTIAL_JOURNAL_VOL3.get();
                case 4 -> ModItems.CELESTIAL_JOURNAL_VOL4.get();
                case 5 -> ModItems.CELESTIAL_JOURNAL_VOL5.get();
                case 6 -> ModItems.CELESTIAL_JOURNAL_VOL6.get();
                case 7 -> ModItems.CELESTIAL_JOURNAL_VOL7.get();
                case 8 -> ModItems.CELESTIAL_JOURNAL_VOL8.get();
                case 9 -> ModItems.CELESTIAL_JOURNAL_VOL9.get();
                case 10 -> ModItems.CELESTIAL_JOURNAL_VOL10.get();
                case 11 -> ModItems.CELESTIAL_JOURNAL_VOL11.get();
                case 12 -> ModItems.CELESTIAL_JOURNAL_VOL12.get();
                default -> ModItems.CELESTIAL_JOURNAL_VOL13.get();
            };
            return new ItemStack(item);
        } catch (Throwable ignored) {
            return new ItemStack(Items.WRITTEN_BOOK);
        }
    }

    // =========================================================================
    // OPTION 5: CONCENTRIC PROGRESSION RING CONSTELLATION BUILDER
    // =========================================================================

    private static final int MAX_OUTER_STARS_PER_PAGE = 17;

    public static List<ConstellationSubGroup> buildConstellationGroups(String category) {
        ensureInitialized();
        List<ConstellationSubGroup> groups = new ArrayList<>();

        if (CAT_LORE.equals(category)) {
            for (int v = 1; v <= CelestialJournalData.TOTAL_VOLUMES; v++) {
                CelestialJournalData.JournalVolume vol = CelestialJournalData.getVolume(v);
                String folderName = "Vol. " + vol.roman() + " — " + vol.shortTitle();
                List<WikiArticle> volArticles = new ArrayList<>();
                for (WikiArticle a : getArticlesForCategory(CAT_LORE)) {
                    if (a.loreVolume() == v) volArticles.add(a);
                }
                if (!volArticles.isEmpty()) {
                    int accent = 0xFF000000 | (vol.guiAccentColor() & 0x00FFFFFF);
                    WikiArticle centerAnchor = volArticles.get(0);
                    List<WikiArticle> rest = volArticles.subList(1, volArticles.size());
                    groups.add(new ConstellationSubGroup(
                            "lore_vol_" + v,
                            "Vol. " + vol.roman(),
                            accent,
                            paginateIntoConcentricRings(centerAnchor, rest, true)
                    ));
                }
            }
            return groups;
        }

        List<WikiArticle> catArticles = getArticlesForCategory(category);
        if (catArticles.isEmpty()) return groups;

        // Compact chapters fit cleanly into a single Concentric Ring chart!
        if (catArticles.size() <= MAX_OUTER_STARS_PER_PAGE + 1) {
            WikiArticle hub = findCenterAnchor(catArticles, getCategoryHubId(category));
            List<WikiArticle> children = new ArrayList<>();
            for (WikiArticle a : catArticles) {
                if (!a.id().equals(hub.id())) children.add(a);
            }
            groups.add(new ConstellationSubGroup(
                    "all",
                    "✦ Constellation",
                    getCategoryColor(category),
                    paginateIntoConcentricRings(hub, children, false)
            ));
            return groups;
        }

        // Large chapters (Environment & Nature, Magic) split cleanly by Sub-Folder tabs!
        Map<String, List<WikiArticle>> byFolder = getSubFolderTree(category);
        for (Map.Entry<String, List<WikiArticle>> entry : byFolder.entrySet()) {
            String folder = entry.getKey();
            List<WikiArticle> items = entry.getValue();
            if (items.isEmpty()) continue;

            WikiArticle anchor = items.get(0);
            for (WikiArticle candidate : items) {
                if (candidate.isParentHub()
                        || "env_biomes".equals(candidate.id())
                        || "env_flora".equals(candidate.id())
                        || "env_fauna".equals(candidate.id())
                        || "magic_spellcrafting".equals(candidate.id())
                        || "magic_astral".equals(candidate.id())) {
                    anchor = candidate;
                    break;
                }
            }

            List<WikiArticle> rest = new ArrayList<>();
            for (WikiArticle a : items) {
                if (!a.id().equals(anchor.id())) {
                    rest.add(a);
                }
            }

            int accent = anchor.branchColor();
            groups.add(new ConstellationSubGroup(
                    folder.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_"),
                    folder,
                    accent,
                    paginateIntoConcentricRings(anchor, rest, false)
            ));
        }

        return groups;
    }

    private static WikiArticle findCenterAnchor(List<WikiArticle> list, String preferredId) {
        for (WikiArticle a : list) {
            if (a.id().equals(preferredId) || a.isParentHub()) return a;
        }
        return list.get(0);
    }

    private static List<ConstellationChartPage> paginateIntoConcentricRings(
            WikiArticle centerAnchor,
            List<WikiArticle> children,
            boolean sequentialChain
    ) {
        List<ConstellationChartPage> pages = new ArrayList<>();
        if (children.isEmpty()) {
            List<StarPlacement> single = List.of(new StarPlacement(centerAnchor, 0, 0.0f, 0.0f, null));
            pages.add(new ConstellationChartPage(0, 1, single));
            return pages;
        }

        int totalPages = (int) Math.ceil((double) children.size() / MAX_OUTER_STARS_PER_PAGE);
        for (int p = 0; p < totalPages; p++) {
            int from = p * MAX_OUTER_STARS_PER_PAGE;
            int to = Math.min(children.size(), from + MAX_OUTER_STARS_PER_PAGE);
            List<WikiArticle> slice = children.subList(from, to);

            Set<String> sliceIds = new LinkedHashSet<>();
            sliceIds.add(centerAnchor.id());
            for (WikiArticle s : slice) sliceIds.add(s.id());

            // Assign stars across the 3 concentric progression rings (Inner <= 5, Middle <= 6, Outer <= 6)
            List<WikiArticle> ring1 = new ArrayList<>();
            List<WikiArticle> ring2 = new ArrayList<>();
            List<WikiArticle> ring3 = new ArrayList<>();

            if (sequentialChain) {
                for (int i = 0; i < slice.size(); i++) {
                    if (i < 5) ring1.add(slice.get(i));
                    else if (i < 11) ring2.add(slice.get(i));
                    else ring3.add(slice.get(i));
                }
            } else {
                for (WikiArticle node : slice) {
                    int depth = computeDepthWithinSlice(node, centerAnchor.id(), sliceIds);
                    if (depth <= 1 && ring1.size() < 5) {
                        ring1.add(node);
                    } else if (depth <= 2 && ring2.size() < 6) {
                        ring2.add(node);
                    } else if (ring3.size() < 6) {
                        ring3.add(node);
                    } else if (ring2.size() < 6) {
                        ring2.add(node);
                    } else {
                        ring1.add(node);
                    }
                }
            }

            List<StarPlacement> placements = new ArrayList<>();
            placements.add(new StarPlacement(centerAnchor, 0, 0.0f, 0.0f, null));

            Map<String, Float> assignedAngles = new LinkedHashMap<>();
            placeRingStars(ring1, 1, 0.36f, -Math.PI / 2.0, centerAnchor.id(), sliceIds, sequentialChain, assignedAngles, placements);
            placeRingStars(ring2, 2, 0.67f, -Math.PI / 2.0 + 0.28, centerAnchor.id(), sliceIds, sequentialChain, assignedAngles, placements);
            placeRingStars(ring3, 3, 0.95f, -Math.PI / 2.0 - 0.22, centerAnchor.id(), sliceIds, sequentialChain, assignedAngles, placements);

            pages.add(new ConstellationChartPage(p, totalPages, Collections.unmodifiableList(placements)));
        }

        return pages;
    }

    private static void placeRingStars(
            List<WikiArticle> ringNodes,
            int ringIdx,
            float normalizedRadius,
            double startAngleOffset,
            String centerAnchorId,
            Set<String> pageIds,
            boolean sequentialChain,
            Map<String, Float> assignedAngles,
            List<StarPlacement> out
    ) {
        if (ringNodes.isEmpty()) return;

        // Sort nodes on outer rings by their parent's angle so connecting lines radiate cleanly without crossing!
        if (!sequentialChain && ringIdx > 1) {
            ringNodes.sort((a, b) -> {
                float angA = a.prerequisiteId() != null ? assignedAngles.getOrDefault(a.prerequisiteId(), 0f) : 0f;
                float angB = b.prerequisiteId() != null ? assignedAngles.getOrDefault(b.prerequisiteId(), 0f) : 0f;
                return Float.compare(angA, angB);
            });
        }

        int n = ringNodes.size();
        for (int i = 0; i < n; i++) {
            WikiArticle art = ringNodes.get(i);
            double angle = startAngleOffset + (i * (Math.PI * 2.0) / n);
            // Subtle deterministic organic offset so stars feel celestial while remaining evenly spaced
            double jitterR = ((art.id().hashCode() & 7) - 3.5) * 0.012;
            float r = (float) (normalizedRadius + jitterR);
            float nx = (float) (Math.cos(angle) * r);
            float ny = (float) (Math.sin(angle) * r);

            assignedAngles.put(art.id(), (float) angle);

            String connectTo = resolveVisibleParentInSlice(art, centerAnchorId, pageIds);
            out.add(new StarPlacement(art, ringIdx, nx, ny, connectTo));
        }
    }

    private static int computeDepthWithinSlice(WikiArticle node, String anchorId, Set<String> sliceIds) {
        int depth = 1;
        String curr = node.prerequisiteId();
        int guard = 0;
        while (curr != null && !curr.equals(anchorId) && guard < 8) {
            if (sliceIds.contains(curr)) {
                depth++;
            }
            WikiArticle p = getArticle(curr);
            curr = (p != null) ? p.prerequisiteId() : null;
            guard++;
        }
        return Math.min(3, depth);
    }

    private static String resolveVisibleParentInSlice(WikiArticle node, String anchorId, Set<String> pageIds) {
        String curr = node.prerequisiteId();
        int guard = 0;
        while (curr != null && guard < 8) {
            if (pageIds.contains(curr)) {
                return curr;
            }
            WikiArticle p = getArticle(curr);
            curr = (p != null) ? p.prerequisiteId() : null;
            guard++;
        }
        return anchorId;
    }

    // =========================================================================
    // WIKI CROSS-LINKING, ITEM LOOKUP & SUB-FOLDER HELPERS
    // =========================================================================

    @Nullable
    public static WikiArticle resolveWikiLink(String rawLink, CodexPlayerData data) {
        if (rawLink == null || rawLink.isBlank()) return null;
        ensureInitialized();
        String clean = rawLink.trim();
        int pipe = clean.indexOf('|');
        if (pipe > 0) {
            clean = clean.substring(0, pipe).trim();
        }

        // 1. Direct ID match
        WikiArticle byId = ARTICLES_BY_ID.get(clean.toLowerCase(Locale.ROOT).replace(' ', '_'));
        if (byId != null && canReadArticle(byId, data)) return byId;

        String lower = clean.toLowerCase(Locale.ROOT);

        // 2. Exact title match
        for (WikiArticle a : ALL_ARTICLES) {
            if (!canReadArticle(a, data)) continue;
            if (a.title().equalsIgnoreCase(clean)) return a;
        }

        // 3. Substring title match (prefer shorter specific titles over hub names)
        WikiArticle best = null;
        for (WikiArticle a : ALL_ARTICLES) {
            if (!canReadArticle(a, data)) continue;
            String tLower = a.title().toLowerCase(Locale.ROOT);
            if (tLower.contains(lower) || lower.contains(tLower)) {
                if (tLower.length() >= 4 && (best == null || a.title().length() < best.title().length())) {
                    best = a;
                }
            }
        }
        return best;
    }

    @Nullable
    public static WikiArticle findArticleForItem(ItemStack stack, CodexPlayerData data) {
        if (stack == null || stack.isEmpty()) return null;
        ensureInitialized();

        // 1. Exact icon match on non-hub standard articles
        for (WikiArticle a : ALL_ARTICLES) {
            if (a.isLoreEntry() || a.isParentHub() || !canReadArticle(a, data)) continue;
            if (!a.icon().isEmpty() && a.icon().getItem() == stack.getItem()) {
                return a;
            }
        }

        // 2. Check CodexCraftingLockHandler item->node mapping
        CodexNode lockNode = CodexCraftingLockHandler.getRequiredNodeForStack(stack);
        if (lockNode != null) {
            WikiArticle mapped = ARTICLES_BY_ID.get(lockNode.id);
            if (mapped != null && canReadArticle(mapped, data)) return mapped;
        }

        // 3. Fallback by item display name
        return resolveWikiLink(stack.getHoverName().getString(), data);
    }

    public static List<WikiArticle> getRelatedArticles(WikiArticle current, CodexPlayerData data, int limit) {
        if (current == null) return List.of();
        ensureInitialized();

        Set<WikiArticle> related = new LinkedHashSet<>();

        // 1. Prerequisite parent
        if (current.prerequisiteId() != null) {
            WikiArticle parent = ARTICLES_BY_ID.get(current.prerequisiteId());
            if (parent != null && canReadArticle(parent, data)) {
                related.add(parent);
            }
        }

        // 2. Direct child articles
        for (WikiArticle candidate : ALL_ARTICLES) {
            if (related.size() >= limit) break;
            if (current.id().equals(candidate.prerequisiteId()) && canReadArticle(candidate, data)) {
                related.add(candidate);
            }
        }

        // 3. Cross-category wiki mentions (solves overlapping chapters cleanly!)
        String bodyLower = (current.title() + " " + current.summary() + " " + current.content()).toLowerCase(Locale.ROOT);
        for (WikiArticle candidate : ALL_ARTICLES) {
            if (related.size() >= limit) break;
            if (candidate.id().equals(current.id()) || candidate.isParentHub() || candidate.isLoreEntry()) continue;
            if (!canReadArticle(candidate, data)) continue;

            // Prioritize cross-category overlaps first!
            if (!candidate.category().equals(current.category())) {
                String titleKey = candidate.title().toLowerCase(Locale.ROOT);
                int ampIdx = titleKey.indexOf('&');
                String primaryTerm = (ampIdx > 3 ? titleKey.substring(0, ampIdx) : titleKey).trim();
                if (primaryTerm.length() >= 5 && bodyLower.contains(primaryTerm)) {
                    related.add(candidate);
                }
            }
        }

        // 4. If this is a Lore entry, also cross-link relevant Astral/Magic guides mentioned in the text
        if (current.isLoreEntry()) {
            for (WikiArticle candidate : getArticlesForCategory(CAT_MAGIC)) {
                if (related.size() >= limit) break;
                if (candidate.isParentHub()) continue;
                String primary = candidate.title().split("&")[0].trim().toLowerCase(Locale.ROOT);
                if (primary.length() >= 5 && bodyLower.contains(primary)) {
                    related.add(candidate);
                }
            }
        }

        List<WikiArticle> out = new ArrayList<>(related);
        return out.size() > limit ? out.subList(0, limit) : out;
    }

    private static String classifySubFolder(CodexNode node) {
        String id = node.id;
        String prereq = node.prerequisiteId != null ? node.prerequisiteId : "";

        return switch (node.category) {
            case CAT_GETTING_STARTED -> "Foundations & Primer";
            case CAT_MATERIALS -> {
                if (id.contains("crystal") || id.contains("blessing")) yield "Crystals & Geodes";
                if (id.contains("ore") || id.contains("arcanite") || id.contains("viscanite")) yield "Ores & Metallurgy";
                if (id.contains("fauna") || id.contains("amber") || id.contains("glass")) yield "Organic & Glassworking";
                yield "Materials Overview";
            }
            case CAT_MATERIA -> "Essences & Logistics";
            case CAT_MACHINERY -> {
                if (id.contains("anchor") || id.contains("bouncepad") || id.contains("vehicle")) yield "Gravity & Kinetics";
                yield "Workstations & Crafters";
            }
            case CAT_MULTIBLOCKS -> {
                if (id.contains("astral") || id.contains("altar") || id.contains("observatory")) yield "Astral Structures";
                yield "Arcane & Reactor Multiblocks";
            }
            case CAT_ENVIRONMENT -> {
                if ("env_flora".equals(id) || "env_flora".equals(prereq)
                        || id.contains("tree") || id.contains("willow") || id.contains("mangrove")
                        || id.contains("rose") || id.contains("blossom")) {
                    yield "Botanical Flora";
                }
                if ("env_fauna".equals(id) || "env_fauna".equals(prereq) || "env_cryo_stalker".equals(prereq)) {
                    yield "Fauna Bestiary";
                }
                if ("env_biomes".equals(id) || "env_biomes".equals(prereq) || prereq.startsWith("env_biome_")
                        || prereq.equals("env_storm_swept_icebergs") || prereq.equals("env_spore_glazed_pack")
                        || prereq.equals("env_materia_blessing")) {
                    yield "Biomes & Regional Wonders";
                }
                yield "World Overview";
            }
            case CAT_MAGIC -> {
                if (id.startsWith("spell") || "magic_spellcrafting".equals(id)
                        || "magic_spellcrafting".equals(prereq) || id.contains("autocast")) {
                    yield "Spellcrafting & Glyphs";
                }
                if (id.startsWith("astral") || "magic_astral".equals(id) || "magic_astral".equals(prereq)
                        || id.contains("glowpeg") || id.contains("optic") || id.contains("starlight")) {
                    yield "Astral Magic & Optics";
                }
                yield "Rituals, Alchemy & Arcana";
            }
            default -> "General Articles";
        };
    }

    private static int resolveBranchColor(String category, String subFolder, String id) {
        if (CAT_ENVIRONMENT.equals(category)) {
            return switch (subFolder) {
                case "Botanical Flora" -> 0xFF34D399;
                case "Fauna Bestiary" -> 0xFFF97316;
                case "Biomes & Regional Wonders" -> 0xFF38BDF8;
                default -> 0xFF38F25B;
            };
        }
        if (CAT_MAGIC.equals(category)) {
            return switch (subFolder) {
                case "Spellcrafting & Glyphs" -> 0xFF38BDF8;
                case "Astral Magic & Optics" -> 0xFFFBBF24;
                default -> 0xFFE879F9;
            };
        }
        if (CAT_MATERIALS.equals(category)) {
            if (subFolder.contains("Crystals")) return 0xFF60A5FA;
            if (subFolder.contains("Organic")) return 0xFFFBBF24;
            return 0xFFFFA500;
        }
        if (CAT_MACHINERY.equals(category) && subFolder.contains("Gravity")) {
            return 0xFFFB7185;
        }
        return getCategoryColor(category);
    }

    private static List<String> resolveMobEntityIds(CodexNode node) {
        if (!CAT_ENVIRONMENT.equals(node.category)) {
            return List.of();
        }
        // Only gate individual creature entries under Fauna Bestiary (keep gear/overview articles open!)
        if (!"env_fauna".equals(node.prerequisiteId)) {
            return List.of();
        }
        if ("env_ovis_species".equals(node.id)) {
            return List.of(
                    "entropica:rime_back_ovis",
                    "entropica:overgrowth_ovis",
                    "entropica:patina_ovis"
            );
        }
        if (node.id.startsWith("env_")) {
            String entityPath = node.id.substring(4);
            ResourceLocation entityLoc = ResourceLocation.fromNamespaceAndPath("entropica", entityPath);
            if (BuiltInRegistries.ENTITY_TYPE.containsKey(entityLoc) || !entityPath.isEmpty()) {
                return List.of(entityLoc.toString());
            }
        }
        return List.of();
    }

    private static String extractLoreSummary(CelestialJournalData.JournalVolume vol, CelestialJournalData.JournalEntry entry) {
        for (CelestialJournalData.EntryBlock block : entry.blocks()) {
            if (block.text() != null && !block.text().isBlank()) {
                String clean = block.text().replace("**", "").replace("*", "").trim();
                if (clean.length() > 95) {
                    clean = clean.substring(0, 92) + "...";
                }
                return clean;
            }
        }
        return vol.title() + " — " + vol.author();
    }

    private static String flattenLoreText(CelestialJournalData.JournalEntry entry) {
        StringBuilder sb = new StringBuilder();
        for (CelestialJournalData.EntryBlock block : entry.blocks()) {
            if (block.text() != null && !block.text().isBlank()) {
                sb.append(block.text()).append('\n');
            }
        }
        return sb.toString();
    }
}
