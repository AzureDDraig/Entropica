package ddraig.net.entropica.forcefield;

import ddraig.net.entropica.entity.forcefield.ForcefieldBarrierEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Encapsulates the result of an edge-fusing snap alignment query.
 */
public record SnapResult(
        boolean isSnapped,
        Vec3 snappedPos,
        float snappedYaw,
        float snappedPitch,
        @Nullable ForcefieldBarrierEntity snappedNeighbor,
        SnapType snapType,
        double snapDistance
) {
    public static SnapResult unSnapped(Vec3 pos, float yaw, float pitch) {
        return new SnapResult(false, pos, yaw, pitch, null, SnapType.NONE, 0.0);
    }

    public static SnapResult snapped(Vec3 pos, float yaw, float pitch, @Nullable ForcefieldBarrierEntity neighbor, SnapType type, double distance) {
        return new SnapResult(true, pos, yaw, pitch, neighbor, type, distance);
    }
}
