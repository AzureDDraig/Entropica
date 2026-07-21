package ddraig.net.entropica.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class ParalyzedEffect extends MobEffect {

    public ParalyzedEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        // Force slowness 255 and jump boost 250 to completely freeze movement and prevent jumping
        entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 255, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 25, 250, false, false, false));
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }
}
