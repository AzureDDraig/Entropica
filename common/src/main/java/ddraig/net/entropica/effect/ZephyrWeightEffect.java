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
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Zephyr Weight — buoyant wind-cushioning status effect of 2.15 Dust-Crusted Bergs,
 * radiated by Wind-Baffle Lanterns, uncorked from Dust-Devil Ampoules, and brewed from Dust-Core Shards.
 * Boosts jump height, extends safe fall distance, cushions fall damage by 55%, and grants active
 * immunity to sandstorm and dust-scrub Blindness.
 */
public class ZephyrWeightEffect extends MobEffect {

    public static final ResourceLocation MOD_SAFE_FALL_ID =
            ResourceLocation.fromNamespaceAndPath("entropica", "effect.zephyr_weight_safe_fall");
    public static final ResourceLocation MOD_FALL_DAMAGE_ID =
            ResourceLocation.fromNamespaceAndPath("entropica", "effect.zephyr_weight_fall_damage");
    public static final ResourceLocation MOD_JUMP_STRENGTH_ID =
            ResourceLocation.fromNamespaceAndPath("entropica", "effect.zephyr_weight_jump_strength");

    public ZephyrWeightEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xC5CBD4);
        this.addAttributeModifier(
                Attributes.SAFE_FALL_DISTANCE,
                MOD_SAFE_FALL_ID,
                5.0D,
                AttributeModifier.Operation.ADD_VALUE
        );
        this.addAttributeModifier(
                Attributes.FALL_DAMAGE_MULTIPLIER,
                MOD_FALL_DAMAGE_ID,
                -0.55D,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
        this.addAttributeModifier(
                Attributes.JUMP_STRENGTH,
                MOD_JUMP_STRENGTH_ID,
                0.09D,
                AttributeModifier.Operation.ADD_VALUE
        );
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        if (entity.hasEffect(MobEffects.BLINDNESS)) {
            entity.removeEffect(MobEffects.BLINDNESS);
        }
        return true;
    }

    public static boolean hasZephyrWeight(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        MobEffect target = ModEffects.ZEPHYR_WEIGHT.isBound()
                ? ModEffects.ZEPHYR_WEIGHT.get()
                : BuiltInRegistries.MOB_EFFECT
                        .getOptional(ResourceLocation.fromNamespaceAndPath("entropica", "zephyr_weight"))
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
