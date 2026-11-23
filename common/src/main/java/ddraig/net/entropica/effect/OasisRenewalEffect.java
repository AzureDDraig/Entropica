package ddraig.net.entropica.effect;

import ddraig.net.entropica.registry.ModEffects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * Oasis Renewal — warm Oasis and Glacial Materia renewal effect radiated by Oasis Dew Basins
 * and brewed from Bottled Amber Nectar. Continuously melts away freeze buildup and purges Wither chill.
 */
public class OasisRenewalEffect extends MobEffect {

    public OasisRenewalEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xF59E0B);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity livingEntity, int amplifier) {
        if (livingEntity.getTicksFrozen() > 0) {
            livingEntity.setTicksFrozen(0);
        }
        if (livingEntity.hasEffect(MobEffects.WITHER)) {
            livingEntity.removeEffect(MobEffects.WITHER);
        }
        return true;
    }

    public static boolean hasOasisRenewal(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        MobEffect target = ModEffects.OASIS_RENEWAL.isBound()
                ? ModEffects.OASIS_RENEWAL.get()
                : BuiltInRegistries.MOB_EFFECT
                        .getOptional(ResourceLocation.fromNamespaceAndPath("entropica", "oasis_renewal"))
                        .orElse(null);
        if (target == null) {
            return false;
        }
        for (MobEffectInstance inst : entity.getActiveEffects()) {
            if (inst.getEffect().value() == target) {
                return true;
            }
        }
        return false;
    }
}
