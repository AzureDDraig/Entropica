package ddraig.net.entropica.astral;

import ddraig.net.entropica.network.SyncAstralProgressPayload;
import ddraig.net.entropica.util.EntityHelper;
import dev.architectury.networking.NetworkManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerAstralProgress {

    public static final String NBT_DISCOVERED = "EntropicaAstralDiscovered";
    public static final String NBT_CHARTED = "EntropicaAstralCharted";

    // Discovery registry keyed by player UUID
    private static final ConcurrentHashMap<UUID, Set<ResourceLocation>> DISCOVERED = new ConcurrentHashMap<>();
    private static final Set<ResourceLocation> CLIENT_DISCOVERED = Collections.synchronizedSet(new HashSet<>());

    // Charted star connections keyed by player UUID ("nodeA---nodeB")
    private static final ConcurrentHashMap<UUID, Set<String>> CHARTED_CONNECTIONS = new ConcurrentHashMap<>();
    private static final Set<String> CLIENT_CHARTED_CONNECTIONS = Collections.synchronizedSet(new HashSet<>());

    public static boolean isDiscovered(Player player, Constellation constellation) {
        if (constellation == null) return false;
        return isDiscovered(player, constellation.getId());
    }

    public static boolean isDiscovered(Player player, ResourceLocation constellationId) {
        if (constellationId == null) return false;
        if (player == null || player.level().isClientSide()) {
            synchronized (CLIENT_DISCOVERED) {
                return CLIENT_DISCOVERED.contains(constellationId);
            }
        }

        Set<ResourceLocation> set = DISCOVERED.get(player.getUUID());
        if (set == null) {
            loadFromPlayer(player);
            set = DISCOVERED.get(player.getUUID());
        }
        return set != null && set.contains(constellationId);
    }

    public static void discover(Player player, Constellation constellation) {
        if (constellation == null) return;
        discover(player, constellation.getId());
    }

    public static void discover(Player player, ResourceLocation constellationId) {
        if (constellationId == null) return;
        CLIENT_DISCOVERED.add(constellationId);

        if (player != null && !player.level().isClientSide()) {
            DISCOVERED.computeIfAbsent(player.getUUID(), k -> Collections.synchronizedSet(new HashSet<>())).add(constellationId);
            saveToPlayer(player);
            if (player instanceof ServerPlayer sp) {
                sync(sp);
            }
        }
    }

    public static boolean undiscover(Player player, Constellation constellation) {
        if (constellation == null) return false;
        return undiscover(player, constellation.getId());
    }

    public static boolean undiscover(Player player, ResourceLocation constellationId) {
        if (constellationId == null) return false;
        CLIENT_DISCOVERED.remove(constellationId);

        if (player != null && !player.level().isClientSide()) {
            Set<ResourceLocation> set = DISCOVERED.get(player.getUUID());
            if (set != null) {
                boolean removed = set.remove(constellationId);
                saveToPlayer(player);
                if (player instanceof ServerPlayer sp) {
                    sync(sp);
                }
                return removed;
            }
        }
        return false;
    }

    public static void clearDiscovered(Player player) {
        CLIENT_DISCOVERED.clear();
        if (player != null && !player.level().isClientSide()) {
            Set<ResourceLocation> set = DISCOVERED.get(player.getUUID());
            if (set != null) {
                set.clear();
            }
            saveToPlayer(player);
            if (player instanceof ServerPlayer sp) {
                sync(sp);
            }
        }
    }

    public static void clearPersonal(Player player) {
        CLIENT_DISCOVERED.clear();
        CLIENT_CHARTED_CONNECTIONS.clear();
        if (player != null && !player.level().isClientSide()) {
            Set<ResourceLocation> disc = DISCOVERED.get(player.getUUID());
            if (disc != null) {
                disc.clear();
            }
            Set<String> charted = CHARTED_CONNECTIONS.get(player.getUUID());
            if (charted != null) {
                charted.clear();
            }
            saveToPlayer(player);
            if (player instanceof ServerPlayer sp) {
                sync(sp);
            }
        }
    }

    public static void sync(ServerPlayer player) {
        if (player == null) return;
        List<ResourceLocation> discovered = new ArrayList<>(getDiscovered(player));
        List<String> charted = new ArrayList<>(getChartedConnections(player));
        NetworkManager.sendToPlayer(player, new SyncAstralProgressPayload(discovered, charted));
    }

    public static Set<ResourceLocation> getDiscovered(Player player) {
        if (player == null || player.level().isClientSide()) {
            synchronized (CLIENT_DISCOVERED) {
                return new HashSet<>(CLIENT_DISCOVERED);
            }
        }
        Set<ResourceLocation> set = DISCOVERED.get(player.getUUID());
        if (set == null) {
            loadFromPlayer(player);
            set = DISCOVERED.get(player.getUUID());
        }
        if (set != null) {
            synchronized (set) {
                return new HashSet<>(set);
            }
        }
        return new HashSet<>();
    }

    public static Set<String> getChartedConnections(Player player) {
        if (player == null || player.level().isClientSide()) {
            synchronized (CLIENT_CHARTED_CONNECTIONS) {
                return new HashSet<>(CLIENT_CHARTED_CONNECTIONS);
            }
        }
        Set<String> set = CHARTED_CONNECTIONS.get(player.getUUID());
        if (set == null) {
            loadFromPlayer(player);
            set = CHARTED_CONNECTIONS.get(player.getUUID());
        }
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
        if (player != null && !player.level().isClientSide()) {
            CHARTED_CONNECTIONS.computeIfAbsent(player.getUUID(), k -> Collections.synchronizedSet(new HashSet<>())).add(edge);
            saveToPlayer(player);
        }
    }

    public static void removeChartedConnection(Player player, String nodeA, String nodeB) {
        String edge = makeEdgeKey(nodeA, nodeB);
        removeChartedConnection(player, edge);
    }

    public static void removeChartedConnection(Player player, String edge) {
        CLIENT_CHARTED_CONNECTIONS.remove(edge);
        if (player != null && !player.level().isClientSide()) {
            Set<String> set = CHARTED_CONNECTIONS.get(player.getUUID());
            if (set != null) {
                set.remove(edge);
                saveToPlayer(player);
            }
        }
    }

    public static void clearChartedConnections(Player player) {
        CLIENT_CHARTED_CONNECTIONS.clear();
        if (player != null && !player.level().isClientSide()) {
            Set<String> set = CHARTED_CONNECTIONS.get(player.getUUID());
            if (set != null) {
                set.clear();
                saveToPlayer(player);
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
            saveToPlayer(player);
        }
    }

    public static void setClientProgress(Collection<ResourceLocation> discovered, Collection<String> charted) {
        synchronized (CLIENT_DISCOVERED) {
            CLIENT_DISCOVERED.clear();
            if (discovered != null) {
                CLIENT_DISCOVERED.addAll(discovered);
            }
        }
        synchronized (CLIENT_CHARTED_CONNECTIONS) {
            CLIENT_CHARTED_CONNECTIONS.clear();
            if (charted != null) {
                CLIENT_CHARTED_CONNECTIONS.addAll(charted);
            }
        }
    }

    public static void loadFromPlayer(Player player) {
        if (player == null || player.level().isClientSide()) return;
        CompoundTag tag = EntityHelper.getPersistentData(player);

        tag.getList(NBT_DISCOVERED).ifPresent(list -> {
            Set<ResourceLocation> set = Collections.synchronizedSet(new HashSet<>());
            for (int i = 0; i < list.size(); i++) {
                String str = list.getString(i).orElse("");
                ResourceLocation id = ResourceLocation.tryParse(str);
                if (id != null) {
                    set.add(id);
                }
            }
            DISCOVERED.put(player.getUUID(), set);
        });

        tag.getList(NBT_CHARTED).ifPresent(list -> {
            Set<String> set = Collections.synchronizedSet(new HashSet<>());
            for (int i = 0; i < list.size(); i++) {
                String str = list.getString(i).orElse("");
                if (!str.isEmpty()) {
                    set.add(str);
                }
            }
            CHARTED_CONNECTIONS.put(player.getUUID(), set);
        });
    }

    public static void saveToPlayer(Player player) {
        if (player == null || player.level().isClientSide()) return;
        CompoundTag tag = EntityHelper.getPersistentData(player);

        Set<ResourceLocation> disc = DISCOVERED.get(player.getUUID());
        if (disc != null) {
            ListTag list = new ListTag();
            synchronized (disc) {
                for (ResourceLocation id : disc) {
                    list.add(StringTag.valueOf(id.toString()));
                }
            }
            tag.put(NBT_DISCOVERED, list);
        }

        Set<String> charted = CHARTED_CONNECTIONS.get(player.getUUID());
        if (charted != null) {
            ListTag list = new ListTag();
            synchronized (charted) {
                for (String edge : charted) {
                    list.add(StringTag.valueOf(edge));
                }
            }
            tag.put(NBT_CHARTED, list);
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
        if (tier == ConstellationTier.MYTHIC || tier == ConstellationTier.TRANSCENDENT) {
            return getDiscoveredCount(player, ConstellationTier.MASTER) >= 1;
        }
        return false;
    }

    public static String getRequiredInstrumentName(ConstellationTier tier) {
        return switch (tier) {
            case FUNDAMENTAL -> "Celestial Looking Glass";
            case ADVANCED -> "Astrolabe / Refractive Lens";
            case MASTER -> "Brass Telescope";
            case MYTHIC, TRANSCENDENT -> "Grand Observatory Armillary";
        };
    }

    public static int getInstrumentOpticTier(Player player) {
        if (player == null) return 1;
        if (player.getMainHandItem().is(ddraig.net.entropica.registry.ModItems.CELESTIAL_ARMILLARY_CONTROLLER_ITEM.get()) ||
            player.getOffhandItem().is(ddraig.net.entropica.registry.ModItems.CELESTIAL_ARMILLARY_CONTROLLER_ITEM.get())) {
            return 4;
        }
        if (player.getMainHandItem().is(ddraig.net.entropica.registry.ModItems.ASTROLABE.get()) ||
            player.getOffhandItem().is(ddraig.net.entropica.registry.ModItems.ASTROLABE.get())) {
            return 2;
        }
        return 1;
    }

    public static boolean canHardwareObserve(int opticTier, ConstellationTier tier) {
        int requiredTier = tier.ordinal() + 1;
        int minRequiredOptic = Math.min(4, requiredTier);
        return opticTier >= minRequiredOptic;
    }

    public static boolean canHardwareObserve(boolean isTelescope, boolean isObservatory, ConstellationTier tier) {
        int opticTier = isObservatory ? 4 : (isTelescope ? 3 : 1);
        return canHardwareObserve(opticTier, tier);
    }

    public static boolean canObserveConstellation(Player player, boolean isTelescope, boolean isObservatory, Constellation constellation) {
        if (constellation == null) return false;
        return canHardwareObserve(isTelescope, isObservatory, constellation.getTier());
    }

    public static Set<ResourceLocation> getClientDiscovered() {
        synchronized (CLIENT_DISCOVERED) {
            return new HashSet<>(CLIENT_DISCOVERED);
        }
    }
}
