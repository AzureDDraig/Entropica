package ddraig.net.entropica.effect;

import ddraig.net.entropica.registry.ModEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * Spore Clarity — purifying Spore and Glacial Materia status effect brewed from Mycelial Spore Caps
 * or radiated by Cryo-Spore Censers. Purges Nausea and Poison every 10 ticks and protects against
 * Frozen Spore Moss disorientation.
 */
public class SporeClarityEffect extends MobEffect {

    public SporeClarityEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xA3E635);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity livingEntity, int amplifier) {
        if (livingEntity.hasEffect(MobEffects.NAUSEA)) {
            livingEntity.removeEffect(MobEffects.NAUSEA);
        }
        if (livingEntity.hasEffect(MobEffects.POISON)) {
            livingEntity.removeEffect(MobEffects.POISON);
        }
        return true;
    }

    public static boolean hasSporeClarity(LivingEntity entity) {
        if (entity == null || !ModEffects.SPORE_CLARITY.isBound()) {
            return false;
        }
        MobEffect target = ModEffects.SPORE_CLARITY.get();
        for (MobEffectInstance inst : entity.getActiveEffects()) {
            if (inst.getEffect().value() == target) {
                return true;
            }
        }
        return false;
    }
}
