package ddraig.net.entropica.astral;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerAstralProgress {

    // Discovery registry keyed by player UUID
    private static final ConcurrentHashMap<UUID, Set<ResourceLocation>> DISCOVERED = new ConcurrentHashMap<>();
    private static final Set<ResourceLocation> CLIENT_DISCOVERED = new HashSet<>();

    public static boolean isDiscovered(Player player, Constellation constellation) {
        if (constellation == null) return false;
        if (player == null) {
            return CLIENT_DISCOVERED.contains(constellation.getId());
        }
        if (player.level().isClientSide()) {
            return CLIENT_DISCOVERED.contains(constellation.getId());
        }

        Set<ResourceLocation> set = DISCOVERED.get(player.getUUID());
        return set != null && set.contains(constellation.getId());
    }

    public static void discover(Player player, Constellation constellation) {
        if (constellation == null) return;
        CLIENT_DISCOVERED.add(constellation.getId());

        if (player != null) {
            DISCOVERED.computeIfAbsent(player.getUUID(), k -> new HashSet<>()).add(constellation.getId());
        }
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
        return CLIENT_DISCOVERED;
    }
}
