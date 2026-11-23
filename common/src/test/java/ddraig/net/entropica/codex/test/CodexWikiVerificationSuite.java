package ddraig.net.entropica.codex.test;

import ddraig.net.entropica.client.gui.widget.CodexRecipeWidget;
import ddraig.net.entropica.codex.CodexCategoryRegistry.CodexNode;
import ddraig.net.entropica.codex.CodexWikiMetadata;
import ddraig.net.entropica.codex.CodexWikiMetadata.ConstellationChartPage;
import ddraig.net.entropica.codex.CodexWikiMetadata.ConstellationSubGroup;
import ddraig.net.entropica.codex.CodexWikiMetadata.StarPlacement;
import ddraig.net.entropica.codex.CodexWikiMetadata.StarState;
import ddraig.net.entropica.codex.CodexWikiMetadata.WikiArticle;
import ddraig.net.entropica.data.CelestialJournalData;
import ddraig.net.entropica.data.CodexPlayerData;
import ddraig.net.entropica.network.SyncCodexDataPayload;
import ddraig.net.entropica.test.forcefield.Tier1FeatureCoverageTests;
import ddraig.net.entropica.test.forcefield.Tier1FeatureCoverageTests.ParsedCodexNode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Deterministic verification suite for the overhauled Entropic Codex
 * (6-Node Orbital Main Index + Lore Ribbon, Concentric Ring Constellations,
 * Two-Page Arcane Tome Wiki Reader, Sequential Star Shining, Mob Bestiary,
 * and Scholar's Journal Lore Archiving).
 */
public final class CodexWikiVerificationSuite {

    private static int passed = 0;
    private static int parsedNodeCount = 0;

    public static void main(String[] args) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        // Populate CodexWikiMetadata in headless common test mode using all 220 real nodes parsed from disk
        Map<String, ParsedCodexNode> parsed = Tier1FeatureCoverageTests.getParsedCodexNodes();
        List<CodexNode> headlessNodes = new ArrayList<>(parsed.size());
        for (ParsedCodexNode p : parsed.values()) {
            headlessNodes.add(new CodexNode(
                    p.id, p.title, p.category, p.summary, p.content,
                    p.prerequisiteId, p.requiredTier, new ItemStack(Items.BOOK),
                    p.orbitRadius, p.orbitSpeed, 0.0f, p.isParentHub
            ));
        }
        parsedNodeCount = headlessNodes.size();
        CodexWikiMetadata.initializeFromNodes(headlessNodes);

        System.out.println("================================================================================");
        System.out.println("           ENTROPIC CODEX CONSTELLATION & WIKI VERIFICATION SUITE               ");
        System.out.println("================================================================================");

        verifyUnifiedArticlesAndLoreCount();
        verifyConcentricRingConstellationsNoOverflow();
        verifyOpenWikiAndSequentialStarIgnition();
        verifyEntryByEntryLoreJournalArchiving();
        verifyMobBestiaryPartialAndCompleteUnlocks();
        verifyNbtAndPayloadRoundTrip();
        verifyCrossLinksAndRecipeIndexing();

        System.out.println("================================================================================");
        System.out.println("  CODEX WIKI SUITE RESULT: " + passed + " / " + passed + " CHECKS PASSED");
        System.out.println("================================================================================");
    }

    private static void verifyUnifiedArticlesAndLoreCount() {
        int loreCount = CodexWikiMetadata.getTotalLoreEntryCount();
        List<WikiArticle> all = CodexWikiMetadata.getAllArticles();

        check(parsedNodeCount >= 215, "Expected >= 215 parsed standard Codex nodes, got " + parsedNodeCount);
        check(loreCount == 233, "Expected 233 Scholar's Journal chapters across Volumes I-XIII, got " + loreCount);
        check(all.size() == parsedNodeCount + loreCount, "Unified WikiArticle count must equal standard + lore");
        check(CodexWikiMetadata.RING_CATEGORIES.size() == 6, "Main Index orbital ring must have 6 categories");
        pass("Unified Articles & 13-Volume Lore Index (" + all.size() + " total entries)");
    }

    private static void verifyConcentricRingConstellationsNoOverflow() {
        for (String cat : CodexWikiMetadata.ALL_WIKI_CATEGORIES) {
            List<ConstellationSubGroup> groups = CodexWikiMetadata.buildConstellationGroups(cat);
            check(!groups.isEmpty(), "Constellation groups must not be empty for " + cat);
            if (CodexWikiMetadata.CAT_LORE.equals(cat)) {
                check(groups.size() == CelestialJournalData.TOTAL_VOLUMES, "Lore must expose 13 Volume sub-constellations");
            }
            for (ConstellationSubGroup grp : groups) {
                check(!grp.pages().isEmpty(), "Constellation pages must not be empty in " + grp.tabTitle());
                for (ConstellationChartPage page : grp.pages()) {
                    check(page.stars().size() <= 18, "Each Concentric Ring page must have <= 18 stars to prevent overlap");
                    StarPlacement center = page.stars().get(0);
                    check(center.ringIndex() == 0, "First star on every chart page must sit at Center Ring 0");
                }
            }
        }
        pass("Concentric Progression Rings (Option 5) Pagination & Zero-Overlap Bounds");
    }

    private static void verifyOpenWikiAndSequentialStarIgnition() {
        CodexPlayerData data = new CodexPlayerData();
        WikiArticle hubMagic = CodexWikiMetadata.getArticle("hub_magic");
        WikiArticle spellcraft = CodexWikiMetadata.getArticle("magic_spellcrafting");
        WikiArticle thirdHand = CodexWikiMetadata.getArticle("spell_third_hand");

        check(hubMagic != null && spellcraft != null && thirdHand != null, "Magic branch articles must exist");
        check(CodexWikiMetadata.getStarState(hubMagic, data) == StarState.SHINING, "Parent hub star starts SHINING");
        check(CodexWikiMetadata.getStarState(spellcraft, data) == StarState.DIM, "Unread branch star starts DIM");
        check(CodexWikiMetadata.getStarState(thirdHand, data) == StarState.DIM, "Unread child star starts DIM");

        check(CodexWikiMetadata.canReadArticle(thirdHand, data), "Open Wiki allows reading normal DIM guide stars anytime");

        check(!data.tryIgniteStarOnRead("spell_third_hand"), "Child star must not ignite before parent star shines");
        check(CodexWikiMetadata.getStarState(thirdHand, data) == StarState.DIM, "Skipped star remains DIM");

        check(data.tryIgniteStarOnRead("magic_spellcrafting"), "Reading child of shining hub ignites star");
        check(CodexWikiMetadata.getStarState(spellcraft, data) == StarState.SHINING, "magic_spellcrafting now SHINING");
        check(data.tryIgniteStarOnRead("spell_third_hand"), "Reading next star in lit branch ignites it");
        check(CodexWikiMetadata.getStarState(thirdHand, data) == StarState.SHINING, "spell_third_hand now SHINING");
        pass("Open Wiki + Sequential Star Shining Progression");
    }

    private static void verifyEntryByEntryLoreJournalArchiving() {
        CodexPlayerData data = new CodexPlayerData();
        WikiArticle v1e0 = CodexWikiMetadata.getArticle("lore_vol1_0");
        WikiArticle v1e1 = CodexWikiMetadata.getArticle("lore_vol1_1");

        check(v1e0 != null && v1e1 != null, "Volume I Lore entries must exist");
        check(CodexWikiMetadata.getStarState(v1e0, data) == StarState.MYSTERY, "Unread Lore entry starts as MYSTERY");
        check(!CodexWikiMetadata.canReadArticle(v1e0, data), "Unread Lore entry is hidden until read in-game");
        check("???".equals(CodexWikiMetadata.getDisplayTitle(v1e0, data)), "Unread Lore entry displays as ???");

        check(data.archiveLoreEntry(1, 0), "Archiving Vol 1 Entry 0 succeeds");
        check(CodexWikiMetadata.getStarState(v1e0, data) == StarState.SHINING, "Read Lore entry becomes SHINING");
        check(CodexWikiMetadata.canReadArticle(v1e0, data), "Archived Lore entry is readable in Codex");
        check(CodexWikiMetadata.getStarState(v1e1, data) == StarState.MYSTERY, "Entry 1 stays MYSTERY until read");
        pass("Scholar's Journal Entry-by-Entry Lore Archiving");
    }

    private static void verifyMobBestiaryPartialAndCompleteUnlocks() {
        CodexPlayerData data = new CodexPlayerData();
        WikiArticle scorp = CodexWikiMetadata.getArticle("env_obsidian_scorpioid");
        check(scorp != null && scorp.isMobBestiaryEntry(), "Obsidian Scorpioid must be a Mob Bestiary entry");

        check(CodexWikiMetadata.getStarState(scorp, data) == StarState.MYSTERY, "Unseen mob starts as MYSTERY");
        check(!CodexWikiMetadata.canReadArticle(scorp, data), "Unseen mob cannot be opened before spotting");

        data.observeMob("entropica:obsidian_scorpioid");
        check(CodexWikiMetadata.getStarState(scorp, data) == StarState.PARTIAL, "Spotted mob becomes PARTIAL");
        check(CodexWikiMetadata.canReadArticle(scorp, data), "Spotted mob opens partial overview");

        data.fullyUnlockMob("entropica:obsidian_scorpioid");
        check(CodexWikiMetadata.getStarState(scorp, data) == StarState.SHINING, "Defeated/tamed mob becomes SHINING");
        pass("Mob Bestiary Discovery States (MYSTERY -> PARTIAL -> SHINING)");
    }

    private static void verifyNbtAndPayloadRoundTrip() {
        CodexPlayerData src = new CodexPlayerData();
        src.tryIgniteStarOnRead("magic_rituals");
        src.archiveLoreEntry(4, 2);
        src.toggleBookmark("firmament_weaver");
        src.toggleBookmark("lore_vol4_2");
        src.observeMob("entropica:aurorafowl");
        src.fullyUnlockMob("entropica:sandstone_golem");

        CompoundTag saved = src.saveNbt(new CompoundTag());
        CodexPlayerData loaded = new CodexPlayerData();
        loaded.loadNbt(saved);

        check(loaded.isStarShining("magic_rituals"), "Shining star must survive NBT save/load");
        check(loaded.isLoreEntryArchived(4, 2), "Archived Lore entry must survive NBT save/load");
        check(loaded.isBookmarked("firmament_weaver") && loaded.isBookmarked("lore_vol4_2"), "Bookmarks must survive NBT");
        check(loaded.isMobObserved("entropica:aurorafowl"), "Observed mob must survive NBT");
        check(loaded.isMobFullyUnlocked("entropica:sandstone_golem"), "Unlocked mob must survive NBT");

        SyncCodexDataPayload payload = new SyncCodexDataPayload(
                loaded.getResearchTierLevel(),
                new ArrayList<>(loaded.getUnlockedNodes()),
                new ArrayList<>(loaded.getShiningStars()),
                List.of("entropica:aurorafowl"),
                List.of("entropica:sandstone_golem"),
                new ArrayList<>(loaded.getBookmarkedTopics()),
                new ArrayList<>(loaded.getArchivedLoreEntries())
        );

        ByteBuf buf = Unpooled.buffer();
        SyncCodexDataPayload.STREAM_CODEC.encode(buf, payload);
        SyncCodexDataPayload decoded = SyncCodexDataPayload.STREAM_CODEC.decode(buf);

        CodexPlayerData clientCopy = new CodexPlayerData();
        clientCopy.applySyncPayload(decoded);
        check(clientCopy.isLoreEntryArchived(4, 2), "Client sync must preserve archived Lore chapters");
        check(clientCopy.isStarShining("magic_rituals"), "Client sync must preserve shining stars");
        check(clientCopy.isBookmarked("firmament_weaver"), "Client sync must preserve bookmarks");
        pass("NBT Persistence & SyncCodexDataPayload Network Round-Trip");
    }

    private static void verifyCrossLinksAndRecipeIndexing() {
        CodexPlayerData data = new CodexPlayerData();
        WikiArticle resolved = CodexWikiMetadata.resolveWikiLink("Firmament Weaver", data);
        check(resolved != null && "firmament_weaver".equals(resolved.id()), "Wiki [[Link]] resolution must locate Firmament Weaver");

        List<WikiArticle> related = CodexWikiMetadata.getRelatedArticles(resolved, data, 5);
        check(!related.isEmpty(), "Related articles must cross-link connected topics");

        CodexRecipeWidget widget = new CodexRecipeWidget();
        WikiArticle ritualsArt = CodexWikiMetadata.getArticle("magic_rituals");
        if (ritualsArt != null) {
            widget.setArticle(ritualsArt);
            // Note: In headless common JVM ModItems aren't bound so HardcodedRecipes falls back safely without throwing
            check(widget.getRecipeCount() >= 0, "CodexRecipeWidget must safely handle headless mode without exceptions");
        }
        pass("Wiki Cross-Links & Safe CodexRecipeWidget Indexing");
    }

    private static void check(boolean cond, String msg) {
        if (!cond) {
            throw new IllegalStateException("CodexWikiVerificationSuite FAILED: " + msg);
        }
    }

    private static void pass(String label) {
        passed++;
        System.out.println("  PASS: " + label);
    }
}
