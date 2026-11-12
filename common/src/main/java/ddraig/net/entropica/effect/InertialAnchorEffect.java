package ddraig.net.entropica.effect;

import ddraig.net.entropica.gravity.GravityApi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class InertialAnchorEffect extends MobEffect {

    public InertialAnchorEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFD166);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        GravityApi.resetGravity(entity, WeightlessnessEffect.EFFECT_ID);
        GravityApi.resetGravity(entity, GravitationalCrushEffect.EFFECT_ID);
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }
}