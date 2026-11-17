package ddraig.net.entropica.mixin;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.forcefield.BarrierFieldManager;
import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    /**
     * Section 6.9 Umbral <i>Shroud of Silence &amp; Stealth</i> &amp; Section 1.7 Stealth Assassination:
     * Shrouded casters, Shadow Stalker constructs, and silent Umbral projectiles emit zero acoustic vibrations
     * (100% immune to Sculk Sensors, Calibrated Sculk Sensors, Sculk Shriekers, and Wardens).
     */
    @Inject(method = "dampensVibrations", at = @At("HEAD"), cancellable = true)
    private void entropica$dampenUmbralShroudVibrations(CallbackInfoReturnable<Boolean> cir) {
        if (entropica$isUmbralVibrationSilenced((Entity) (Object) this)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "gameEvent(Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void entropica$suppressUmbralEntityGameEvents(Holder<GameEvent> event, @Nullable Entity sourceEntity, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (entropica$isUmbralVibrationSilenced(self)
                || (sourceEntity != null && entropica$isUmbralVibrationSilenced(sourceEntity))) {
            ci.cancel();
        }
    }

    private static boolean entropica$isUmbralVibrationSilenced(Entity entity) {
        if (entity instanceof SpellEidolonEntity eidolon && eidolon.isMutated()) {
            EssenceType mat = eidolon.getEffectiveMateria();
            if (mat == EssenceType.UMBRAL || mat == EssenceType.NULL_U || mat == EssenceType.WRAITH) {
                return true;
            }
        }
        if (entity instanceof SpellProjectileEntity spellProj && spellProj.isSilentStealthProjectile()) {
            return true;
        }
        if (entity instanceof Projectile proj && proj.getOwner() instanceof LivingEntity owner && owner.level() != null) {
            long ownerShroud = EntityHelper.getPersistentData(owner)
                    .getLong(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL).orElse(0L);
            if (ownerShroud > owner.level().getGameTime()) {
                return true;
            }
        }
        if (entity instanceof LivingEntity living && living.level() != null) {
            long shroudUntil = EntityHelper.getPersistentData(living)
                    .getLong(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL).orElse(0L);
            return shroudUntil > living.level().getGameTime();
        }
        return false;
    }
}
