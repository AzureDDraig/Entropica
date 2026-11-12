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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class GravitationalCrushEffect extends MobEffect {

    public static final ResourceLocation EFFECT_ID = ResourceLocation.fromNamespaceAndPath("entropica", "effect_gravitational_crush");

    public GravitationalCrushEffect() {
        super(MobEffectCategory.HARMFUL, 0x3A0CA3);
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

        double targetG = GravityApi.CRUSH_GRAVITY * (1.0 + (amplifier * 0.5));
        GravityApi.setGravity(entity, EFFECT_ID, targetG);

        // Flight crash: suppress active flight
        if (entity instanceof Player player) {
            if (player.getAbilities().flying) {
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }
        if (entity.isFallFlying()) {
            entity.stopFallFlying();
        }

        // Jump suppression & downward crushing slam
        Vec3 motion = entity.getDeltaMovement();
        if (motion.y > 0.0) {
            entity.setDeltaMovement(motion.x * 0.8, -0.1, motion.z * 0.8);
            entity.hasImpulse = true;
        } else if (!entity.onGround()) {
            entity.setDeltaMovement(motion.x, motion.y - 0.08, motion.z);
            entity.hasImpulse = true;
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}