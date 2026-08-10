package ddraig.net.entropica.effect;

import net.minecraft.core.particles.ParticleTypes;
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
        // Inflict 1.0 damage + 0.5 per amplifier level
        livingEntity.hurtServer(level, level.damageSources().cactus(), 1.0F + (float) amplifier * 0.5F);

        // Spawn blood droplet particles on server level
        double x = livingEntity.getX();
        double y = livingEntity.getY() + livingEntity.getBbHeight() * 0.5;
        double z = livingEntity.getZ();
        level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, x, y, z, 3, 0.2, 0.2, 0.2, 0.05);

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
