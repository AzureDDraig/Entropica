package ddraig.net.entropica.effect;

import ddraig.net.entropica.registry.ModEffects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Sanguine Vigor — warm Blood and Glacial Materia vitality of 2.16 Blood-Ice Shallows,
 * uncorked from Sanguine Draught Ampoules and brewed from Sanguine Heart-Gel.
 * Bolsters maximum health by +2 full Hearts (+4.0 HP), continually melts away freeze buildup,
 * and siphons health on direct melee strikes via {@link ddraig.net.entropica.event.SanguineCombatHandler}.
 */
public class SanguineVigorEffect extends MobEffect {

    public static final ResourceLocation MOD_MAX_HEALTH_ID =
            ResourceLocation.fromNamespaceAndPath("entropica", "effect.sanguine_vigor");

    public SanguineVigorEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xD42A48);
        this.addAttributeModifier(
                Attributes.MAX_HEALTH,
                MOD_MAX_HEALTH_ID,
                4.0D,
                AttributeModifier.Operation.ADD_VALUE
        );
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        if (entity.getTicksFrozen() > 0) {
            entity.setTicksFrozen(0);
        }
        return true;
    }

    public static boolean hasSanguineVigor(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        MobEffect target = ModEffects.SANGUINE_VIGOR.isBound()
                ? ModEffects.SANGUINE_VIGOR.get()
                : BuiltInRegistries.MOB_EFFECT
                        .getOptional(ResourceLocation.fromNamespaceAndPath("entropica", "sanguine_vigor"))
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
