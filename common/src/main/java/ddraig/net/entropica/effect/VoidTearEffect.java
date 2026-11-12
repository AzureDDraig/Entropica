package ddraig.net.entropica.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class VoidTearEffect extends MobEffect {

    public VoidTearEffect() {
        // HARMFUL category, Dark Abyssal Purple color
        super(MobEffectCategory.HARMFUL, 0x1A0033);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        // Deals 1 full heart (2.0F) of magic damage per tick applied
        entity.hurtServer(level, level.damageSources().magic(), 2.0F);
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // Applies the damage tick every 10 ticks (half a second)
        int i = 10 >> amplifier;
        if (i > 0) {
            return duration % i == 0;
        } else {
            return true;
        }
    }
}