package ddraig.net.entropica.astral;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerAstralProgress {

    // Discovery registry keyed by player UUID
    private static final ConcurrentHashMap<UUID, Set<ResourceLocation>> DISCOVERED = new ConcurrentHashMap<>();
    private static final Set<ResourceLocation> CLIENT_DISCOVERED = Collections.synchronizedSet(new HashSet<>());

    // Charted star connections keyed by player UUID ("nodeA---nodeB")
    private static final ConcurrentHashMap<UUID, Set<String>> CHARTED_CONNECTIONS = new ConcurrentHashMap<>();
    private static final Set<String> CLIENT_CHARTED_CONNECTIONS = Collections.synchronizedSet(new HashSet<>());

    public static boolean isDiscovered(Player player, Constellation constellation) {
        if (constellation == null) return false;
        if (player == null || player.level().isClientSide()) {
            synchronized (CLIENT_DISCOVERED) {
                return CLIENT_DISCOVERED.contains(constellation.getId());
            }
        }

        Set<ResourceLocation> set = DISCOVERED.get(player.getUUID());
        return set != null && set.contains(constellation.getId());
    }

    public static void discover(Player player, Constellation constellation) {
        if (constellation == null) return;
        CLIENT_DISCOVERED.add(constellation.getId());

        if (player != null) {
            DISCOVERED.computeIfAbsent(player.getUUID(), k -> Collections.synchronizedSet(new HashSet<>())).add(constellation.getId());
        }
    }

    public static Set<String> getChartedConnections(Player player) {
        if (player == null || player.level().isClientSide()) {
            synchronized (CLIENT_CHARTED_CONNECTIONS) {
                return new HashSet<>(CLIENT_CHARTED_CONNECTIONS);
            }
        }
        Set<String> set = CHARTED_CONNECTIONS.get(player.getUUID());
        if (set != null) {
            synchronized (set) {
                return new HashSet<>(set);
            }
        }
        return new HashSet<>();
    }

    public static void addChartedConnection(Player player, String nodeA, String nodeB) {
        String edge = makeEdgeKey(nodeA, nodeB);
        addChartedConnection(player, edge);
    }

    public static void addChartedConnection(Player player, String edge) {
        CLIENT_CHARTED_CONNECTIONS.add(edge);
        if (player != null) {
            CHARTED_CONNECTIONS.computeIfAbsent(player.getUUID(), k -> Collections.synchronizedSet(new HashSet<>())).add(edge);
        }
    }

    public static void removeChartedConnection(Player player, String nodeA, String nodeB) {
        String edge = makeEdgeKey(nodeA, nodeB);
        removeChartedConnection(player, edge);
    }

    public static void removeChartedConnection(Player player, String edge) {
        CLIENT_CHARTED_CONNECTIONS.remove(edge);
        if (player != null) {
            Set<String> set = CHARTED_CONNECTIONS.get(player.getUUID());
            if (set != null) {
                set.remove(edge);
            }
        }
    }

    public static void clearChartedConnections(Player player) {
        CLIENT_CHARTED_CONNECTIONS.clear();
        if (player != null) {
            Set<String> set = CHARTED_CONNECTIONS.get(player.getUUID());
            if (set != null) {
                set.clear();
            }
        }
    }

    public static void setChartedConnections(Player player, Collection<String> edges) {
        if (player == null || player.level().isClientSide()) {
            synchronized (CLIENT_CHARTED_CONNECTIONS) {
                CLIENT_CHARTED_CONNECTIONS.clear();
                CLIENT_CHARTED_CONNECTIONS.addAll(edges);
            }
        } else {
            Set<String> set = CHARTED_CONNECTIONS.computeIfAbsent(player.getUUID(), k -> Collections.synchronizedSet(new HashSet<>()));
            synchronized (set) {
                set.clear();
                set.addAll(edges);
            }
        }
    }

    public static String makeEdgeKey(String a, String b) {
        return (a.compareTo(b) < 0) ? (a + "---" + b) : (b + "---" + a);
    }

    public static int getDiscoveredCount(Player player, ConstellationTier tier) {
        int count = 0;
        for (Constellation c : ModConstellations.getAllConstellations()) {
            if (c.getTier() == tier && isDiscovered(player, c)) {
                count++;
            }
        }
        return count;
    }

    public static boolean canPerceiveTier(Player player, ConstellationTier tier) {
        if (tier == ConstellationTier.FUNDAMENTAL) return true;
        if (tier == ConstellationTier.ADVANCED) {
            return getDiscoveredCount(player, ConstellationTier.FUNDAMENTAL) >= 1;
        }
        if (tier == ConstellationTier.MASTER) {
            return getDiscoveredCount(player, ConstellationTier.ADVANCED) >= 1;
        }
        if (tier == ConstellationTier.MYTHIC) {
            return getDiscoveredCount(player, ConstellationTier.MASTER) >= 1;
        }
        return false;
    }

    public static Set<ResourceLocation> getClientDiscovered() {
        synchronized (CLIENT_DISCOVERED) {
            return new HashSet<>(CLIENT_DISCOVERED);
        }
    }
}
