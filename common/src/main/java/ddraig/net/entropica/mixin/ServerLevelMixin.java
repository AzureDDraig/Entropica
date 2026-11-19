package ddraig.net.entropica.mixin;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Section 6.9 Umbral <i>Shroud of Silence &amp; Stealth</i> &amp; Section 1.7 Stealth Assassination:
 * Guarantees that any shrouded caster, Shadow Stalker construct, or silent Umbral projectile emits
 * <b>zero</b> world {@link GameEvent} vibrations of any kind (footsteps, sprinting, jumping, eating,
 * opening chests, breaking blocks, or firing bolts), rendering them 100% undetectable to Sculk Sensors,
 * Calibrated Sculk Sensors, Sculk Shriekers, and Wardens.
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Inject(method = "gameEvent", at = @At("HEAD"), cancellable = true)
    private void entropica$suppressUmbralVibrationGameEvents(
            Holder<GameEvent> gameEvent,
            Vec3 pos,
            GameEvent.Context context,
            CallbackInfo ci
    ) {
        Entity src = context.sourceEntity();
        if (src == null) {
            return;
        }
        ServerLevel self = (ServerLevel) (Object) this;
        long now = self.getGameTime();

        if (src instanceof SpellProjectileEntity spellProj && spellProj.isSilentStealthProjectile()) {
            ci.cancel();
            return;
        }
        if (src instanceof SpellEidolonEntity eidolon && eidolon.isMutated()) {
            EssenceType mat = eidolon.getEffectiveMateria();
            if (mat == EssenceType.UMBRAL || mat == EssenceType.NULL_U || mat == EssenceType.WRAITH) {
                ci.cancel();
                return;
            }
        }
        if (src instanceof Projectile proj && proj.getOwner() instanceof LivingEntity owner) {
            var oData = EntityHelper.getPersistentData(owner);
            if (oData.getLong(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL).orElse(0L) > now
                    || oData.getLong(SpellLingeringZoneManager.TAG_NULL_U_VEIL_UNTIL).orElse(0L) > now
                    || oData.getLong(SpellLingeringZoneManager.TAG_ABYSS_SHROUD_UNTIL).orElse(0L) > now
                    || oData.getLong(SpellLingeringZoneManager.TAG_WRAITH_SHROUD_UNTIL).orElse(0L) > now
                    || oData.getLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL).orElse(0L) > now) {
                ci.cancel();
                return;
            }
        }
        if (src instanceof LivingEntity living) {
            var lData = EntityHelper.getPersistentData(living);
            if (lData.getLong(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL).orElse(0L) > now
                    || lData.getLong(SpellLingeringZoneManager.TAG_NULL_U_VEIL_UNTIL).orElse(0L) > now
                    || lData.getLong(SpellLingeringZoneManager.TAG_ABYSS_SHROUD_UNTIL).orElse(0L) > now
                    || lData.getLong(SpellLingeringZoneManager.TAG_WRAITH_SHROUD_UNTIL).orElse(0L) > now
                    || lData.getLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL).orElse(0L) > now) {
                ci.cancel();
            }
        }
    }
}
