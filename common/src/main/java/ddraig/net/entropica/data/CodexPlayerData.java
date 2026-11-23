package ddraig.net.entropica.data;

import ddraig.net.entropica.codex.CodexCategoryRegistry;
import ddraig.net.entropica.codex.CodexCategoryRegistry.CodexNode;
import ddraig.net.entropica.item.CelestialJournalItem;
import ddraig.net.entropica.network.SyncCodexDataPayload;
import ddraig.net.entropica.util.EntityHelper;
import dev.architectury.networking.NetworkManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CodexPlayerData {

    public static final String NBT_CODEX_ROOT = "EntropicaCodexData";

    private static final List<String> DEFAULT_HUB_STARS = List.of(
            "hub_getting_started",
            "hub_materials",
            "hub_materia",
            "hub_machinery",
            "hub_multiblocks",
            "hub_environment",
            "hub_magic"
    );

    private static final ConcurrentHashMap<UUID, CodexPlayerData> SERVER_PLAYER_DATA = new ConcurrentHashMap<>();
    public static final CodexPlayerData CLIENT_DATA = new CodexPlayerData();

    private boolean hasReceivedCodex = false;
    private int researchTierLevel = 1;
    private final Set<String> unlockedNodes = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> shiningStars = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> observedMobs = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> fullyUnlockedMobs = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> bookmarkedTopics = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> archivedLoreEntries = Collections.synchronizedSet(new HashSet<>());

    public CodexPlayerData() {
        applyStartingDefaults();
    }

    private void applyStartingDefaults() {
        unlockedNodes.add("getting_started_primer");
        unlockedNodes.add("arcanist_monocle");
        unlockedNodes.add("tool_fusion_simulator");
        unlockedNodes.add("tool_network_diagnostics");
        unlockedNodes.addAll(DEFAULT_HUB_STARS);

        shiningStars.addAll(DEFAULT_HUB_STARS);
        shiningStars.add("getting_started_primer");
    }

    public static CodexPlayerData getForPlayer(Player player) {
        if (player == null || player.level().isClientSide()) {
            return CLIENT_DATA;
        }
        return getServerData(player);
    }

    public static CodexPlayerData getServerData(Player player) {
        return SERVER_PLAYER_DATA.computeIfAbsent(player.getUUID(), uuid -> {
            CodexPlayerData data = new CodexPlayerData();
            data.loadFromPlayerEntity(player);
            return data;
        });
    }

    public void loadFromPlayerEntity(Player player) {
        if (player == null) return;
        CompoundTag persistent = EntityHelper.getPersistentData(player);
        persistent.getCompound(NBT_CODEX_ROOT).ifPresent(this::loadNbt);
        applyStartingDefaults();
    }

    public void saveToPlayerEntity(Player player) {
        if (player == null || player.level().isClientSide()) return;
        CompoundTag persistent = EntityHelper.getPersistentData(player);
        persistent.put(NBT_CODEX_ROOT, saveNbt(new CompoundTag()));
    }

    public static void syncToClient(ServerPlayer serverPlayer) {
        if (serverPlayer == null) return;
        CodexPlayerData data = getServerData(serverPlayer);
        data.importReadJournalsFromInventory(serverPlayer);
        data.saveToPlayerEntity(serverPlayer);

        SyncCodexDataPayload payload = new SyncCodexDataPayload(
                data.getResearchTierLevel(),
                new ArrayList<>(data.unlockedNodes),
                new ArrayList<>(data.shiningStars),
                new ArrayList<>(data.observedMobs),
                new ArrayList<>(data.fullyUnlockedMobs),
                new ArrayList<>(data.bookmarkedTopics),
                new ArrayList<>(data.archivedLoreEntries)
        );
        NetworkManager.sendToPlayer(serverPlayer, payload);
    }

    public void applySyncPayload(SyncCodexDataPayload payload) {
        this.researchTierLevel = payload.researchTierLevel();

        this.unlockedNodes.clear();
        this.unlockedNodes.addAll(payload.unlockedNodes());

        this.shiningStars.clear();
        this.shiningStars.addAll(payload.shiningStars());

        this.observedMobs.clear();
        this.observedMobs.addAll(payload.observedMobs());

        this.fullyUnlockedMobs.clear();
        this.fullyUnlockedMobs.addAll(payload.fullyUnlockedMobs());

        this.bookmarkedTopics.clear();
        this.bookmarkedTopics.addAll(payload.bookmarkedTopics());

        this.archivedLoreEntries.clear();
        this.archivedLoreEntries.addAll(payload.archivedLoreEntries());

        applyStartingDefaults();
    }

    /**
     * Scans a player's inventory for any Scholar's Journals that already carry
     * read-entry component data and imports those entries into the Lore chapter.
     */
    public boolean importReadJournalsFromInventory(Player player) {
        if (player == null) return false;
        boolean changed = false;
        int size = player.getInventory().getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof CelestialJournalItem journalItem) {
                int vol = journalItem.getVolumeNumber();
                Set<Integer> readSet = CelestialJournalItem.getReadEntries(stack);
                for (int entryIdx : readSet) {
                    if (archiveLoreEntry(vol, entryIdx)) {
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }

    public boolean hasReceivedCodex() {
        return hasReceivedCodex;
    }

    public void setReceivedCodex(boolean received) {
        this.hasReceivedCodex = received;
    }

    public int getResearchTierLevel() {
        return researchTierLevel;
    }

    public void setResearchTierLevel(int level) {
        this.researchTierLevel = level;
    }

    public boolean isNodeUnlocked(String nodeId) {
        return unlockedNodes.contains(nodeId);
    }

    public boolean isNodeLocked(String nodeId) {
        return !isNodeUnlocked(nodeId);
    }

    public Set<String> getUnlockedNodes() {
        return unlockedNodes;
    }

    public void unlockNode(String nodeId) {
        if (nodeId != null && !nodeId.isEmpty()) {
            unlockedNodes.add(nodeId);
        }
    }

    public boolean isStarShining(String nodeId) {
        if (nodeId == null) return false;
        if (nodeId.startsWith("lore_vol")) {
            return isLoreNodeIdArchived(nodeId);
        }
        return shiningStars.contains(nodeId);
    }

    /**
     * Records that the player read a standard Codex node.
     * In Open Wiki + Visual Star Glow mode, any normal guide is readable right away,
     * and its constellation star ignites into a shining star once its parent star
     * in the branch is already shining!
     *
     * @return true if the star newly ignited into a shining star.
     */
    public boolean tryIgniteStarOnRead(String nodeId) {
        if (nodeId == null || nodeId.isEmpty()) return false;
        String prereqId = null;
        var wikiArt = ddraig.net.entropica.codex.CodexWikiMetadata.getArticleIfInitialized(nodeId);
        if (wikiArt != null) {
            prereqId = wikiArt.prerequisiteId();
        } else {
            CodexNode node = CodexCategoryRegistry.getNodeById(nodeId);
            if (node == null) return false;
            prereqId = node.prerequisiteId;
        }

        boolean parentLit = (prereqId == null || prereqId.isEmpty()
                || shiningStars.contains(prereqId));
        if (parentLit) {
            unlockedNodes.add(nodeId);
            return shiningStars.add(nodeId);
        }
        return false;
    }

    public Set<String> getShiningStars() {
        return shiningStars;
    }

    public boolean isMobObserved(String mobId) {
        return observedMobs.contains(mobId);
    }

    public void observeMob(String mobId) {
        if (mobId != null && !mobId.isEmpty()) {
            observedMobs.add(mobId);
        }
    }

    public boolean isMobFullyUnlocked(String mobId) {
        return fullyUnlockedMobs.contains(mobId);
    }

    public void fullyUnlockMob(String mobId) {
        if (mobId != null && !mobId.isEmpty()) {
            observedMobs.add(mobId);
            fullyUnlockedMobs.add(mobId);
        }
    }

    public boolean isBookmarked(String topicId) {
        return bookmarkedTopics.contains(topicId);
    }

    public void toggleBookmark(String topicId) {
        if (topicId == null || topicId.isEmpty()) return;
        if (bookmarkedTopics.contains(topicId)) {
            bookmarkedTopics.remove(topicId);
        } else {
            bookmarkedTopics.add(topicId);
        }
    }

    public Set<String> getBookmarkedTopics() {
        return bookmarkedTopics;
    }

    public static String toLoreKey(int volumeNumber, int entryIndex) {
        return "vol" + volumeNumber + ":" + entryIndex;
    }

    public static String toLoreNodeId(int volumeNumber, int entryIndex) {
        return "lore_vol" + volumeNumber + "_" + entryIndex;
    }

    public boolean archiveLoreEntry(int volumeNumber, int entryIndex) {
        if (volumeNumber < 1 || volumeNumber > CelestialJournalData.TOTAL_VOLUMES || entryIndex < 0) {
            return false;
        }
        int maxEntries = CelestialJournalData.getVolume(volumeNumber).entries().size();
        if (maxEntries > 0 && entryIndex >= maxEntries) {
            return false;
        }
        return archivedLoreEntries.add(toLoreKey(volumeNumber, entryIndex));
    }

    public boolean archiveLoreKey(String loreKey) {
        if (loreKey == null || !loreKey.startsWith("vol")) return false;
        int colon = loreKey.indexOf(':');
        if (colon <= 3 || colon >= loreKey.length() - 1) return false;
        try {
            int vol = Integer.parseInt(loreKey.substring(3, colon));
            int idx = Integer.parseInt(loreKey.substring(colon + 1));
            return archiveLoreEntry(vol, idx);
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    public boolean isLoreEntryArchived(int volumeNumber, int entryIndex) {
        return archivedLoreEntries.contains(toLoreKey(volumeNumber, entryIndex));
    }

    public boolean isLoreNodeIdArchived(String loreNodeId) {
        if (loreNodeId == null || !loreNodeId.startsWith("lore_vol")) return false;
        int under = loreNodeId.indexOf('_', 8);
        if (under <= 8 || under >= loreNodeId.length() - 1) return false;
        try {
            int vol = Integer.parseInt(loreNodeId.substring(8, under));
            int idx = Integer.parseInt(loreNodeId.substring(under + 1));
            return isLoreEntryArchived(vol, idx);
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    public int getArchivedLoreCountForVolume(int volumeNumber) {
        String prefix = "vol" + volumeNumber + ":";
        int count = 0;
        synchronized (archivedLoreEntries) {
            for (String s : archivedLoreEntries) {
                if (s.startsWith(prefix)) count++;
            }
        }
        return count;
    }

    public int getTotalArchivedLoreCount() {
        return archivedLoreEntries.size();
    }

    public int getDiscoveredVolumeCount() {
        int discovered = 0;
        for (int v = 1; v <= CelestialJournalData.TOTAL_VOLUMES; v++) {
            if (getArchivedLoreCountForVolume(v) > 0) discovered++;
        }
        return discovered;
    }

    public Set<String> getArchivedLoreEntries() {
        return archivedLoreEntries;
    }

    public CompoundTag saveNbt(CompoundTag tag) {
        tag.putBoolean("HasReceivedCodex", hasReceivedCodex);
        tag.putInt("ResearchTierLevel", researchTierLevel);

        putStringSet(tag, "UnlockedNodes", unlockedNodes);
        putStringSet(tag, "ShiningStars", shiningStars);
        putStringSet(tag, "ObservedMobs", observedMobs);
        putStringSet(tag, "FullyUnlockedMobs", fullyUnlockedMobs);
        putStringSet(tag, "BookmarkedTopics", bookmarkedTopics);
        putStringSet(tag, "ArchivedLoreEntries", archivedLoreEntries);

        return tag;
    }

    public void loadNbt(CompoundTag tag) {
        hasReceivedCodex = tag.getBoolean("HasReceivedCodex").orElse(false);
        researchTierLevel = tag.getInt("ResearchTierLevel").orElse(1);

        readStringSet(tag, "UnlockedNodes", unlockedNodes);
        readStringSet(tag, "ShiningStars", shiningStars);
        readStringSet(tag, "ObservedMobs", observedMobs);
        readStringSet(tag, "FullyUnlockedMobs", fullyUnlockedMobs);
        readStringSet(tag, "BookmarkedTopics", bookmarkedTopics);
        readStringSet(tag, "ArchivedLoreEntries", archivedLoreEntries);

        applyStartingDefaults();
    }

    private static void putStringSet(CompoundTag tag, String key, Set<String> set) {
        ListTag list = new ListTag();
        synchronized (set) {
            for (String item : set) {
                if (item != null && !item.isEmpty()) {
                    list.add(StringTag.valueOf(item));
                }
            }
        }
        tag.put(key, list);
    }

    private static void readStringSet(CompoundTag tag, String key, Set<String> target) {
        tag.getList(key).ifPresent(list -> {
            target.clear();
            for (int i = 0; i < list.size(); i++) {
                String val = list.getString(i).orElse("");
                if (!val.isEmpty()) {
                    target.add(val);
                }
            }
        });
    }
}
