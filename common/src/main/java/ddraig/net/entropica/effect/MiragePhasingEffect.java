package ddraig.net.entropica.effect;

import ddraig.net.entropica.registry.ModEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/**
 * Mirage Phasing — beneficial Mirage & Glacial Materia refraction aura brewed from
 * Shimmering Mirage Aura or channeled through the Shimmering Focus.
 */
public class MiragePhasingEffect extends MobEffect {

    public MiragePhasingEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFDBA74);
    }

    public static boolean hasMiragePhasing(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        MobEffect target = ModEffects.MIRAGE_PHASING.isBound()
                ? ModEffects.MIRAGE_PHASING.get()
                : net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT
                        .getOptional(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("entropica", "mirage_phasing"))
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
