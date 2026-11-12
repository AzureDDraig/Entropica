package ddraig.net.entropica.mixin;

import ddraig.net.entropica.forcefield.BarrierFieldManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Continuous collision detection (CCD) hook for forcefield barriers.
 * Guarantees paper-thin impenetrability by intercepting entity swept paths before movement execution
 * and projectile swept trajectories during projectile tick.
 */
@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    private void entropica$onEntityMove(MoverType moverType, Vec3 deltaMovement, CallbackInfo ci) {
        if (deltaMovement.lengthSqr() > 1e-7) {
            Entity entity = (Entity) (Object) this;
            Vec3 startPos = entity.position();
            Vec3 endPos = startPos.add(deltaMovement);

            if (BarrierFieldManager.checkMovementCollisions(entity, startPos, endPos)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void entropica$onProjectileTick(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof Projectile projectile) {
            Vec3 deltaMovement = projectile.getDeltaMovement();
            if (deltaMovement.lengthSqr() > 1e-7) {
                Vec3 startPos = projectile.position();
                Vec3 endPos = startPos.add(deltaMovement);

                if (BarrierFieldManager.checkMovementCollisions(projectile, startPos, endPos)) {
                    ci.cancel();
                }
            }
        }
    }
}
