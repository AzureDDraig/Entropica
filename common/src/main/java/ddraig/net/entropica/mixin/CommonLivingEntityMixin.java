package ddraig.net.entropica.mixin;

import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Stealth &amp; Obscuration Mob Detection Mixin:
 * <ul>
 *     <li>Section 6.9 Umbral <i>Shroud of Silence &amp; Stealth</i>: cuts hostile mob AI detection range by an exact 60% (0.40x).</li>
 *     <li>Section 6.12 Arid <i>Sandstorm Veil</i>: cuts hostile mob AI detection range by an exact 50% (0.50x).</li>
 * </ul>
 */
@Mixin(LivingEntity.class)
public abstract class CommonLivingEntityMixin {

    @Inject(method = "getVisibilityPercent", at = @At("RETURN"), cancellable = true)
    private void entropica$reduceUmbralShroudVisibility(@Nullable Entity lookingEntity, CallbackInfoReturnable<Double> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level() != null) {
            long now = self.level().getGameTime();
            var data = EntityHelper.getPersistentData(self);
            if (data.getLong(SpellLingeringZoneManager.TAG_NULL_U_VEIL_UNTIL).orElse(0L) > now) {
                // Section 6.32 Null_U Abyssal Zero-Acoustic Veil: cuts hostile mob detection range by an exact 70% (0.30x)!
                cir.setReturnValue(cir.getReturnValueD() * 0.30D);
            } else if (data.getLong(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL).orElse(0L) > now
                    || data.getLong(SpellLingeringZoneManager.TAG_ABYSS_SHROUD_UNTIL).orElse(0L) > now) {
                // Section 6.9 Umbral Shroud & Section 6.34 Abyss Hadopelagic Trench Shroud: cuts mob detection range by 60% (0.40x)!
                cir.setReturnValue(cir.getReturnValueD() * 0.40D);
            } else if (data.getLong(SpellLingeringZoneManager.TAG_ARID_VEIL_UNTIL).orElse(0L) > now) {
                cir.setReturnValue(cir.getReturnValueD() * 0.50D);
            }
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void entropica$tickBiomeEnvironmentalPhysics(CallbackInfo ci) {
        ddraig.net.entropica.worldgen.biome.EntropicaBiomeEnvironmentHandler.tickLivingEntity((LivingEntity) (Object) this);
    }
}
