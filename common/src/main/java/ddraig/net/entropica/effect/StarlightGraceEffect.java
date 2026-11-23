package ddraig.net.entropica.effect;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModEffects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Starlight Grace — luminous Astral and Glacial Materia blessing of 2.17 Astral Skerries,
 * radiated by Celestial Orrery Lanterns, uncorked from Starlight Draught Ampoules,
 * savored in cooked Star-Fish delicacies, and brewed from Stardust Fragments.
 * Elevates step height (+0.5), extends safe fall distance (+5.0), cushions fall damage (-60%),
 * quickens stride (+12% Movement Speed), thaws freeze buildup, banishes Darkness and Blindness,
 * and sustains steady, flicker-free Night Vision.
 */
public class StarlightGraceEffect extends MobEffect {

    public static final ResourceLocation MOD_STEP_HEIGHT_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "effect.starlight_grace_step_height");
    public static final ResourceLocation MOD_SAFE_FALL_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "effect.starlight_grace_safe_fall");
    public static final ResourceLocation MOD_FALL_DAMAGE_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "effect.starlight_grace_fall_damage");
    public static final ResourceLocation MOD_MOVEMENT_SPEED_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "effect.starlight_grace_movement_speed");

    public StarlightGraceEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8BE8FF);
        this.addAttributeModifier(
                Attributes.STEP_HEIGHT,
                MOD_STEP_HEIGHT_ID,
                0.5D,
                AttributeModifier.Operation.ADD_VALUE
        );
        this.addAttributeModifier(
                Attributes.SAFE_FALL_DISTANCE,
                MOD_SAFE_FALL_ID,
                5.0D,
                AttributeModifier.Operation.ADD_VALUE
        );
        this.addAttributeModifier(
                Attributes.FALL_DAMAGE_MULTIPLIER,
                MOD_FALL_DAMAGE_ID,
                -0.60D,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                MOD_MOVEMENT_SPEED_ID,
                0.12D,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        if (entity.hasEffect(MobEffects.DARKNESS)) {
            entity.removeEffect(MobEffects.DARKNESS);
        }
        if (entity.hasEffect(MobEffects.BLINDNESS)) {
            entity.removeEffect(MobEffects.BLINDNESS);
        }
        if (entity.getTicksFrozen() > 0) {
            entity.setTicksFrozen(0);
        }
        entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 320, 0, true, false, false));
        return true;
    }

    public static boolean hasStarlightGrace(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        MobEffect target = ModEffects.STARLIGHT_GRACE.isBound()
                ? ModEffects.STARLIGHT_GRACE.get()
                : BuiltInRegistries.MOB_EFFECT
                        .getOptional(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "starlight_grace"))
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
