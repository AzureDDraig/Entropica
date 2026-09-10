package ddraig.net.entropica.effect;

import ddraig.net.entropica.gravity.GravityApi;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class WeightlessnessEffect extends MobEffect {

    public static final ResourceLocation EFFECT_ID = ResourceLocation.fromNamespaceAndPath("entropica", "effect_weightlessness");

    public WeightlessnessEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x4CC9F0);
    }

    private double getTargetGravity(int amplifier) {
        if (amplifier <= 0) {
            return GravityApi.MOON_GRAVITY; // Level I: 0.02 (25%)
        } else if (amplifier == 1) {
            return GravityApi.ZERO_GRAVITY; // Level II: 0.0
        } else {
            return GravityApi.INVERTED_GRAVITY; // Level III+: -0.08 (Upward fall)
        }
    }

    @Override
    public void addAttributeModifiers(AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(attributeMap, amplifier);
    }

    @Override
    public void removeAttributeModifiers(AttributeMap attributeMap) {
        super.removeAttributeModifiers(attributeMap);
        AttributeInstance grav = attributeMap.getInstance(Attributes.GRAVITY);
        if (grav != null) grav.removeModifier(EFFECT_ID);
        AttributeInstance safeFall = attributeMap.getInstance(Attributes.SAFE_FALL_DISTANCE);
        if (safeFall != null) safeFall.removeModifier(EFFECT_ID);
        AttributeInstance fallDmg = attributeMap.getInstance(Attributes.FALL_DAMAGE_MULTIPLIER);
        if (fallDmg != null) fallDmg.removeModifier(EFFECT_ID);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        if (GravityApi.isAnchored(entity)) {
            GravityApi.resetGravity(entity, EFFECT_ID);
            return true;
        }
        double targetG = getTargetGravity(amplifier);
        GravityApi.setGravity(entity, EFFECT_ID, targetG);
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}