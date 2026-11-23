package ddraig.net.entropica.effect;

import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Dampening — beneficial Vapor Materia status effect brewed from Vapor-Condenser Spores.
 * Silences footsteps and suppresses world acoustic vibrations while active.
 */
public class DampeningEffect extends MobEffect {
    public DampeningEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8FB8C8);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        var data = EntityHelper.getPersistentData(entity);
        long now = level.getGameTime();
        if (data.getLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL).orElse(0L) < now + 5L) {
            data.putLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, now + 5L);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
