package ddraig.net.entropica.forcefield;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Filter mode defining which entities are blocked vs permitted by a forcefield barrier.
 */
public enum BarrierFilterMode implements StringRepresentable {
    ALL_ENTITIES("All Entities", "Blocks all unauthorized players, mobs, and projectiles"),
    MOBS_ONLY("Mobs Only", "Blocks all creatures; all players pass through freely"),
    PLAYERS_ONLY("Players Only", "Blocks unauthorized players; all mobs pass through freely"),
    HOSTILE_MOBS("Hostile Mobs", "Blocks hostile monsters; friendly mobs and players pass freely"),
    PROJECTILES("Projectiles Only", "Reflects incoming arrows, tridents, and projectiles; living entities pass");

    private final String displayName;
    private final String description;

    BarrierFilterMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public static BarrierFilterMode fromOrdinal(int ordinal) {
        BarrierFilterMode[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return ALL_ENTITIES;
        }
        return values[ordinal];
    }

    /**
     * Determines whether the given entity should be blocked (and bounced) by this filter.
     *
     * @param entity    Entity attempting to cross the barrier
     * @param ownerUUID UUID of the barrier's creator (null if unowned or boss encounter)
     * @param whitelist Set of explicitly permitted player UUIDs
     * @return true if entity is blocked; false if entity is permitted to pass through freely
     */
    public boolean isBlocked(Entity entity, UUID ownerUUID, Set<UUID> whitelist) {
        if (entity instanceof Player player) {
            // Spectator mode always bypasses
            if (player.isSpectator()) {
                return false;
            }
            // Creator always has free passage
            if (ownerUUID != null && player.getUUID().equals(ownerUUID)) {
                return false;
            }
            // Whitelisted players pass freely
            if (whitelist != null && whitelist.contains(player.getUUID())) {
                return false;
            }
            // Check filter applicability for players
            return this == ALL_ENTITIES || this == PLAYERS_ONLY;
        }

        if (entity instanceof Projectile) {
            return this == ALL_ENTITIES || this == PROJECTILES;
        }

        // For other entities (mobs, animals, monsters)
        if (this == PLAYERS_ONLY || this == PROJECTILES) {
            return false;
        }
        if (this == HOSTILE_MOBS) {
            return entity instanceof Enemy;
        }
        // ALL_ENTITIES or MOBS_ONLY
        return true;
    }
}
