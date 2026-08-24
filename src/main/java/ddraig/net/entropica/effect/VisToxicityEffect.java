package ddraig.net.entropica.effect;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.registry.ModAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class VisToxicityEffect extends MobEffect {

    public VisToxicityEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        String typeName = entity.getData(ModAttachments.TOXICITY_SOURCE);
        EssenceType type = null;

        try {
            if (!typeName.equals("UNKNOWN")) {
                type = EssenceType.valueOf(typeName);
            }
        } catch (IllegalArgumentException e) {
            // Failsafe
        }

        // Base Magic Damage (The raw physical toll of inhaling gas)
        entity.hurtServer(level, level.damageSources().magic(), 1.0F + amplifier);

        if (type == null || type.isFragment()) {
            return true; // Fragments and unknown gases only deal the base magic damage
        }

        // Dynamic, conceptual punishments based on the exact essence inhaled!
        switch (type) {
            // --- BASE TYPES ---
            case AIR, LIGHTNING -> {
                // Violently throws the player upward
                entity.setDeltaMovement(entity.getDeltaMovement().add(0, 0.15 + (0.05 * amplifier), 0));
                entity.hasImpulse = true;
            }
            case WATER, VAPOR, ABYSS -> {
                // Drowning on dry land
                int air = entity.getAirSupply();
                entity.setAirSupply(Math.max(-20, air - (15 + (5 * amplifier))));
                if (entity.getAirSupply() <= -20) {
                    entity.hurtServer(level, level.damageSources().drown(), 2.0F);
                }
            }
            case EARTH, ARID, DUST -> {
                // Extreme heaviness and exhaustion
                entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2 + amplifier));
                if (entity instanceof Player player) {
                    player.causeFoodExhaustion(0.5F * (amplifier + 1));
                }
            }
            case FROZEN, GLACIAL, TAIGA, RIME -> {
                // Rapidly freezes the player from the inside out
                entity.setTicksFrozen(entity.getTicksFrozen() + 60 + (20 * amplifier));
            }
            case NETHER, MAGMA, PYRE -> {
                // Ignites the lungs
                entity.igniteForSeconds(3 + amplifier);
            }
            case RADIANT, AURORA, DAWN -> {
                // Overwhelming light
                entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            }
            case UMBRAL, ECLIPSE, PENUMBRA -> {
                // Crushing darkness
                entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, amplifier));
            }
            case UNDEAD, WRAITH, BARROW -> {
                // Necrosis
                entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, amplifier));
            }
            case VOID, NULL_R, NULL_U -> {
                // Guaranteed spacial displacement (Teleports every 2 seconds / tick interval)
                double d0 = entity.getX() + (level.random.nextDouble() - 0.5D) * 16.0D;
                double d1 = entity.getY() + (double)(level.random.nextInt(16) - 8);
                double d2 = entity.getZ() + (level.random.nextDouble() - 0.5D) * 16.0D;
                entity.randomTeleport(d0, d1, d2, true);

                // Add disorientation to accompany the violent teleportation
                entity.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 60, 0));
            }
            case NATURE, OVERGROWTH, SPORE -> {
                // Vines/Spores constricting the body
                entity.addEffect(new MobEffectInstance(MobEffects.POISON, 60, amplifier));
            }

            // --- VITAL FUSIONS ---
            case VITAE, SPRING, GENESIS -> {
                // Runaway cellular growth. Heals if hurt, but ruptures (damages) if at max health.
                if (entity.getHealth() < entity.getMaxHealth()) {
                    entity.heal(1.0F);
                    entity.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 80, 0)); // Dizzy from rapid growth
                } else {
                    entity.hurtServer(level, level.damageSources().magic(), 3.0F + amplifier); // Tumor rupture
                }
            }
            case BLOOD -> {
                // Unstoppable internal bleeding (Bypasses standard damage ticks)
                entity.hurtServer(level, level.damageSources().generic(), 2.0F + amplifier);
            }
            case AMBER -> {
                // Total paralysis (Trapped in resin)
                entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 255));
                entity.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 60, 250)); // Prevents jumping
            }

            // --- ETHEREAL & PARADOX FUSIONS ---
            case ASTRAL -> {
                // Uncontrollable spacial tearing (Lesser teleport effect compared to Void)
                if (level.random.nextInt(4) == 0) {
                    double d0 = entity.getX() + (level.random.nextDouble() - 0.5D) * 16.0D;
                    double d1 = entity.getY() + (double)(level.random.nextInt(16) - 8);
                    double d2 = entity.getZ() + (level.random.nextDouble() - 0.5D) * 16.0D;
                    entity.randomTeleport(d0, d1, d2, true);
                }
            }
            case SOULFIRE -> {
                // Burns the soul (More damage, doesn't actually light them on fire visually)
                entity.hurtServer(level, level.damageSources().magic(), 2.0F + (amplifier * 2));
            }

            // --- APEX / ESCHATON / SINGULARITY ---
            case ESCHATON, CATACLYSM, OBLIVION -> {
                // Apocalyptic meltdown
                entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, amplifier + 1));
                entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, amplifier + 1));
                entity.hurtServer(level, level.damageSources().genericKill(), 4.0F); // Extremely lethal
            }
            case ENTROPICA -> {
                // Pure chaotic equilibrium - Randomly cycles through elements every single tick!
                int roll = level.random.nextInt(5);
                if (roll == 0) entity.igniteForSeconds(2);
                else if (roll == 1) entity.setTicksFrozen(entity.getTicksFrozen() + 40);
                else if (roll == 2) entity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20, 1));
                else if (roll == 3) entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
                else entity.hurtServer(level, level.damageSources().lightningBolt(), 2.0F);
            }
            default -> {
                // Fallback for minor unmapped fusions
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, amplifier));
            }
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // Ticks every 40 ticks (2 seconds) at level 1.
        // Ticks twice as fast for every amplifier level up.
        int tickInterval = 40 >> amplifier;
        if (tickInterval > 0) {
            return duration % tickInterval == 0;
        } else {
            return true;
        }
    }
}