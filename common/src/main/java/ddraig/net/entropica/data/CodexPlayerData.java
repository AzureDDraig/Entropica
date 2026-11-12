package ddraig.net.entropica.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.HashSet;
import java.util.Set;

public class CodexPlayerData {
    private boolean hasReceivedCodex = false;
    private int researchTierLevel = 1;
    private final Set<String> unlockedNodes = new HashSet<>();
    private final Set<String> observedMobs = new HashSet<>();
    private final Set<String> fullyUnlockedMobs = new HashSet<>();
    private final Set<String> bookmarkedTopics = new HashSet<>();

    public CodexPlayerData() {
        // Unlock starting nodes by default
        unlockedNodes.add("getting_started_primer");
        unlockedNodes.add("arcanist_monocle");
        unlockedNodes.add("tool_fusion_simulator");
        unlockedNodes.add("tool_network_diagnostics");
    }

    public static final CodexPlayerData CLIENT_DATA = new CodexPlayerData();

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
        unlockedNodes.add(nodeId);
    }

    public boolean isMobObserved(String mobId) {
        return observedMobs.contains(mobId);
    }

    public void observeMob(String mobId) {
        observedMobs.add(mobId);
    }

    public boolean isMobFullyUnlocked(String mobId) {
        return fullyUnlockedMobs.contains(mobId);
    }

    public void fullyUnlockMob(String mobId) {
        observedMobs.add(mobId);
        fullyUnlockedMobs.add(mobId);
    }

    public boolean isBookmarked(String topicId) {
        return bookmarkedTopics.contains(topicId);
    }

    public void toggleBookmark(String topicId) {
        if (bookmarkedTopics.contains(topicId)) {
            bookmarkedTopics.remove(topicId);
        } else {
            bookmarkedTopics.add(topicId);
        }
    }

    public Set<String> getBookmarkedTopics() {
        return bookmarkedTopics;
    }

    public CompoundTag saveNbt(CompoundTag tag) {
        tag.putBoolean("HasReceivedCodex", hasReceivedCodex);
        tag.putInt("ResearchTierLevel", researchTierLevel);

        ListTag nodesList = new ListTag();
        for (String node : unlockedNodes) {
            nodesList.add(StringTag.valueOf(node));
        }
        tag.put("UnlockedNodes", nodesList);

        ListTag obsList = new ListTag();
        for (String mob : observedMobs) {
            obsList.add(StringTag.valueOf(mob));
        }
        tag.put("ObservedMobs", obsList);

        ListTag fullMobsList = new ListTag();
        for (String mob : fullyUnlockedMobs) {
            fullMobsList.add(StringTag.valueOf(mob));
        }
        tag.put("FullyUnlockedMobs", fullMobsList);

        ListTag bmList = new ListTag();
        for (String bm : bookmarkedTopics) {
            bmList.add(StringTag.valueOf(bm));
        }
        tag.put("BookmarkedTopics", bmList);

        return tag;
    }

    public void loadNbt(CompoundTag tag) {
        hasReceivedCodex = tag.getBoolean("HasReceivedCodex").orElse(false);
        researchTierLevel = tag.getInt("ResearchTierLevel").orElse(1);

        tag.getList("UnlockedNodes").ifPresent(list -> {
            unlockedNodes.clear();
            for (int i = 0; i < list.size(); i++) {
                unlockedNodes.add(list.getString(i).orElse(""));
            }
        });

        tag.getList("ObservedMobs").ifPresent(list -> {
            observedMobs.clear();
            for (int i = 0; i < list.size(); i++) {
                observedMobs.add(list.getString(i).orElse(""));
            }
        });

        tag.getList("FullyUnlockedMobs").ifPresent(list -> {
            fullyUnlockedMobs.clear();
            for (int i = 0; i < list.size(); i++) {
                fullyUnlockedMobs.add(list.getString(i).orElse(""));
            }
        });

        tag.getList("BookmarkedTopics").ifPresent(list -> {
            bookmarkedTopics.clear();
            for (int i = 0; i < list.size(); i++) {
                bookmarkedTopics.add(list.getString(i).orElse(""));
            }
        });
    }
}
