package ddraig.net.entropica.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class VisToxicityEffect extends MobEffect {

    public VisToxicityEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        // In 1.21.2+, we use hurtServer to properly pass the ServerLevel to the damage event
        entity.hurtServer(level, level.damageSources().magic(), 1.0F + amplifier);
        return true;
    }

    // In 1.21+, this method is explicitly named shouldApplyEffectTickThisTick
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // Ticks every 40 ticks (2 seconds) at level 1.
        // Ticks twice as fast for every amplifier level up.
        int tickInterval = 40 >> amplifier;
        if (tickInterval > 0) {
            return duration % tickInterval == 0;
        } else {
            return true;
        }
    }
}