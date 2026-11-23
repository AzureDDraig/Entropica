package ddraig.net.entropica.effect;

import ddraig.net.entropica.item.biome13.MirageVeilGogglesItem;
import ddraig.net.entropica.registry.ModEffects;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Double Vision — refractive Mirage & Glacial Materia disorientation caused by Mirage Ice
 * refraction and Shimmering Focus flashes. Immediately purged while wearing Mirage-Veil Goggles.
 */
public class DoubleVisionEffect extends MobEffect {

    public DoubleVisionEffect() {
        super(MobEffectCategory.HARMFUL, 0x38BDF8);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 5 == 0;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity livingEntity, int amplifier) {
        if (MirageVeilGogglesItem.hasMirageVeilGoggles(livingEntity)) {
            removeDoubleVision(livingEntity);
            return false;
        }
        return true;
    }

    public static boolean hasDoubleVision(LivingEntity entity) {
        if (entity == null || !ModEffects.DOUBLE_VISION.isBound()) {
            return false;
        }
        MobEffect target = ModEffects.DOUBLE_VISION.get();
        for (MobEffectInstance inst : entity.getActiveEffects()) {
            if (inst.getEffect().value() == target) {
                return true;
            }
        }
        return false;
    }

    public static void removeDoubleVision(LivingEntity entity) {
        if (entity == null || !ModEffects.DOUBLE_VISION.isBound()) {
            return;
        }
        MobEffect target = ModEffects.DOUBLE_VISION.get();
        List<Holder<MobEffect>> toRemove = new ArrayList<>();
        for (MobEffectInstance inst : entity.getActiveEffects()) {
            if (inst.getEffect().value() == target) {
                toRemove.add(inst.getEffect());
            }
        }
        for (Holder<MobEffect> holder : toRemove) {
            entity.removeEffect(holder);
        }
        Holder<MobEffect> wrapped = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(target);
        if (entity.hasEffect(wrapped)) {
            entity.removeEffect(wrapped);
        }
    }
}
