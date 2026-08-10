package ddraig.net.entropica.effect;

import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class BleedingEffect extends MobEffect {

    public BleedingEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity livingEntity, int amplifier) {
        livingEntity.hurtServer(level, level.damageSources().magic(), 1.0F + (float) amplifier * 0.5F);

        double x = livingEntity.getX();
        double y = livingEntity.getY() + livingEntity.getBbHeight() * 0.5;
        double z = livingEntity.getZ();
        level.sendParticles(new TimedTintableParticleOption(ModParticles.TINTABLE_BLOOD.get(), 0.54f, 0.01f, 0.01f, 25, 1.0f, 0f, 0f, TimedTintableParticleOption.MODE_LINEAR, 0f, 0f, 0f), x, y, z, 3, 0.2, 0.2, 0.2, 0.05);

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int interval = 30 >> amplifier;
        if (interval > 0) {
            return duration % interval == 0;
        }
        return true;
    }
}
