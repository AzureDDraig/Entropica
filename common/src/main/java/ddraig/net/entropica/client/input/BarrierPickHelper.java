package ddraig.net.entropica.client.input;

import ddraig.net.entropica.entity.forcefield.ForcefieldBarrierEntity;
import ddraig.net.entropica.forcefield.BarrierGeometry;
import ddraig.net.entropica.forcefield.BarrierRaycastHit;
import ddraig.net.entropica.forcefield.BarrierShapeHandler;
import ddraig.net.entropica.forcefield.BarrierShapeRegistry;
import ddraig.net.entropica.item.FirmamentWeaverItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Client-side helper that manages crosshair selection for ForcefieldBarrierEntity.
 * Ensures the barrier only intercepts crosshair picking when the player is holding
 * the Firmament Weaver or a dyeing item and aiming directly at the barrier's membrane,
 * allowing full unobstructed interaction with blocks, chests, and levers behind/inside it.
 */
public class BarrierPickHelper {

    public static boolean canPickBarrier(ForcefieldBarrierEntity barrier) {
        if (barrier == null || !barrier.isAlive() || !barrier.isActive()) {
            return false;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return false;
        }

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        boolean holdsWeaver = mainHand.getItem() instanceof FirmamentWeaverItem || offHand.getItem() instanceof FirmamentWeaverItem;
        boolean holdsTintItem = ForcefieldBarrierEntity.resolveColorTint(mainHand) != null || ForcefieldBarrierEntity.resolveColorTint(offHand) != null;

        // If not holding the Weaver or a dye/cleanser item, completely ignore the barrier
        if (!holdsWeaver && !holdsTintItem) {
            return false;
        }

        // If holding the Weaver with an active Drag & Snap anchor, do not pick the barrier
        // so the player can cleanly select Point B on adjacent blocks
        if (holdsWeaver && (FirmamentWeaverItem.hasAnchor(mainHand) || FirmamentWeaverItem.hasAnchor(offHand))) {
            return false;
        }

        // Precise raycast against the actual mathematical barrier membrane
        double reach = Math.max(player.blockInteractionRange(), player.entityInteractionRange());
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getViewVector(1.0F);
        Vec3 rayEnd = eyePos.add(lookVec.scale(reach));

        BarrierShapeHandler handler = BarrierShapeRegistry.get(barrier.getShape().ordinal());
        BarrierRaycastHit hit;
        if (handler != null) {
            hit = handler.intersect(
                    barrier.position(), barrier.getYRot(), barrier.getXRot(),
                    barrier.getWidth(), barrier.getHeight(), barrier.getRadius(),
                    eyePos, rayEnd, 0.0, barrier
            );
        } else {
            hit = BarrierGeometry.intersect(
                    barrier.getShape(), barrier.position(), barrier.getYRot(), barrier.getXRot(),
                    barrier.getWidth(), barrier.getHeight(), barrier.getRadius(),
                    eyePos, rayEnd, 0.0
            );
        }

        if (!hit.hit() || hit.t() < 0.0 || hit.t() > 1.0) {
            return false;
        }

        // If there is an obstructing solid block between the player and the barrier membrane,
        // let the block take precedence so players can place blocks or open chests freely
        HitResult blockHit = player.pick(reach, 1.0F, false);
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            double blockDistSqr = blockHit.getLocation().distanceToSqr(eyePos);
            double barrierDistSqr = hit.impactPoint().distanceToSqr(eyePos);
            if (blockDistSqr < barrierDistSqr - 1e-4) {
                return false;
            }
        }

        return true;
    }
}
