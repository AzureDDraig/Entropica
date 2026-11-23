package ddraig.net.entropica.effect;

import ddraig.net.entropica.registry.ModEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * Barrow Ward — protective Barrow & Glacial Materia status effect brewed from Rime-Cap Spores.
 * Purges Wither every 10 ticks, blocks Wither damage, and reduces damage from Undead and Barrow Hounds.
 */
public class BarrowWardEffect extends MobEffect {

    public BarrowWardEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x67E8F9);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity livingEntity, int amplifier) {
        if (livingEntity.hasEffect(MobEffects.WITHER)) {
            livingEntity.removeEffect(MobEffects.WITHER);
        }
        return true;
    }

    public static boolean hasBarrowWard(LivingEntity entity) {
        if (entity == null || !ModEffects.BARROW_WARD.isBound()) {
            return false;
        }
        MobEffect target = ModEffects.BARROW_WARD.get();
        for (MobEffectInstance inst : entity.getActiveEffects()) {
            if (inst.getEffect().value() == target) {
                return true;
            }
        }
        return false;
    }
}
