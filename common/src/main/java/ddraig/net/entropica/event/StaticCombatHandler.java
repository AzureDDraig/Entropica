package ddraig.net.entropica.event;

import ddraig.net.entropica.block.CryoStaticShrubBlock;
import ddraig.net.entropica.block.biome7.BiomassCapacitorBlock;
import ddraig.net.entropica.block.biome7.StaticChargeHelper;
import ddraig.net.entropica.item.GroundingCoilItem;
import ddraig.net.entropica.registry.ModEffects;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Biome #7 (2.7 Static Ice-Plains) combat & lightning hooks:
 * 1. Galvanic Strike weapon imbue ("GalvanicStrikes" > 0 in CUSTOM_DATA)
 * 2. Cryo-Static Shrub melee discharge (+2.0F electric damage)
 * 3. Grounding Coil 50% lightning reduction + stored static melee discharge (+3.0F + Slowness II)
 * 4. Galvanic Surge potion retaliation (2.0F lightning + 40 freeze ticks against melee attackers)
 * 5. World LightningBolt charging for Biomass Capacitors and Cryo-Static Shrubs
 */
public final class StaticCombatHandler {

    private static final ThreadLocal<Boolean> REENTRANT_STATIC_HURT = ThreadLocal.withInitial(() -> false);

    private StaticCombatHandler() {}

    public static void register() {
        EntityEvent.LIVING_HURT.register((entity, source, amount) -> {
            if (entity.level().isClientSide()
                    || !(entity.level() instanceof ServerLevel serverLevel)
                    || amount <= 0.0F
                    || REENTRANT_STATIC_HURT.get()) {
                return EventResult.pass();
            }

            // 1. Grounding Coil: halve incoming lightning bolt damage and store static charge
            if (entity instanceof Player player
                    && (source.is(DamageTypes.LIGHTNING_BOLT) || source.is(DamageTypeTags.IS_LIGHTNING))) {
                if (GroundingCoilItem.tryAbsorbLightningOrStatic(player)) {
                    serverLevel.playSound(null, player.blockPosition(), SoundEvents.COPPER_HIT, SoundSource.PLAYERS, 0.9F, 1.35F);
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            player.getX(), player.getY() + 1.0D, player.getZ(),
                            14, 0.35D, 0.5D, 0.35D, 0.1D);
                    try {
                        REENTRANT_STATIC_HURT.set(true);
                        player.hurtServer(serverLevel, source, amount * 0.5F);
                    } finally {
                        REENTRANT_STATIC_HURT.set(false);
                    }
                    return EventResult.interruptFalse();
                }
            }

            // Direct melee strike checks (non-projectile, non-cooldown i-frame spam)
            if (!source.is(DamageTypeTags.IS_PROJECTILE) && entity.invulnerableTime <= 10) {
                Entity direct = source.getDirectEntity();
                if (direct instanceof LivingEntity attacker && attacker != entity) {

                    // 2. Galvanic Surge on defender: zap melee attacker back for 2.0F lightning + 40 freeze ticks
                    if (hasGalvanicSurge(entity) && attacker.isAlive()) {
                        try {
                            REENTRANT_STATIC_HURT.set(true);
                            attacker.hurtServer(serverLevel, serverLevel.damageSources().lightningBolt(), 2.0F);
                        } finally {
                            REENTRANT_STATIC_HURT.set(false);
                        }
                        int maxAttackerFreeze = attacker.getTicksRequiredToFreeze() + 80;
                        attacker.setTicksFrozen(Math.min(maxAttackerFreeze, attacker.getTicksFrozen() + 40));
                        serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                                attacker.getX(), attacker.getY() + attacker.getBbHeight() * 0.5D, attacker.getZ(),
                                10, 0.3D, 0.35D, 0.3D, 0.1D);
                    }

                    // 3. Attacker melee electric discharges onto target
                    if (entity.isAlive() && !entity.isInvulnerableTo(serverLevel, source)) {
                        float bonusLightning = 0.0F;
                        boolean applySlowness2 = false;
                        int bonusFreezeTicks = 0;

                        // 3a. Weapon "GalvanicStrikes" (> 0 in CUSTOM_DATA)
                        ItemStack weapon = attacker.getMainHandItem();
                        if (!weapon.isEmpty()) {
                            CustomData cd = weapon.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                            int strikes = cd.copyTag().getInt(StaticChargeHelper.TAG_GALVANIC_STRIKES).orElse(0);
                            if (strikes > 0) {
                                int remaining = strikes - 1;
                                CustomData.update(DataComponents.CUSTOM_DATA, weapon, tag -> {
                                    if (remaining > 0) {
                                        tag.putInt(StaticChargeHelper.TAG_GALVANIC_STRIKES, remaining);
                                    } else {
                                        tag.remove(StaticChargeHelper.TAG_GALVANIC_STRIKES);
                                    }
                                });
                                bonusLightning += 2.0F;
                                applySlowness2 = true;
                                bonusFreezeTicks += 60;
                            }
                        }

                        // 3b. Cryo-Static Shrub melee charge (+2.0F lightning)
                        if (attacker instanceof Player player && StaticChargeHelper.consumeShrubMeleeCharge(player)) {
                            bonusLightning += 2.0F;
                        }

                        // 3c. Grounding Coil stored static (+3.0F lightning + Slowness II)
                        if (attacker instanceof Player player && GroundingCoilItem.tryConsumeStoredStatic(player)) {
                            bonusLightning += 3.0F;
                            applySlowness2 = true;
                        }

                        if (bonusLightning > 0.0F) {
                            if (applySlowness2) {
                                entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1), attacker);
                            }
                            if (bonusFreezeTicks > 0) {
                                int maxFreeze = entity.getTicksRequiredToFreeze() + 80;
                                entity.setTicksFrozen(Math.min(maxFreeze, entity.getTicksFrozen() + bonusFreezeTicks));
                            }
                            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                                    entity.getX(), entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ(),
                                    14, 0.35D, 0.4D, 0.35D, 0.12D);
                            serverLevel.playSound(null, entity.blockPosition(),
                                    SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.45F, 1.65F);

                            int prevInvuln = entity.invulnerableTime;
                            try {
                                REENTRANT_STATIC_HURT.set(true);
                                entity.setLastHurtByMob(attacker);
                                entity.invulnerableTime = 0;
                                entity.hurtServer(serverLevel, serverLevel.damageSources().lightningBolt(), bonusLightning);
                            } finally {
                                entity.invulnerableTime = prevInvuln;
                                REENTRANT_STATIC_HURT.set(false);
                            }
                        }
                    }
                }
            }

            return EventResult.pass();
        });

        // Natural / Channeling LightningBolt hooks for Cryo-Static Shrubs & Biomass Capacitors
        EntityEvent.ADD.register((entity, level) -> {
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel && entity instanceof LightningBolt bolt) {
                BlockPos strikePos = BlockPos.containing(bolt.position());
                CryoStaticShrubBlock.spreadOnLightning(serverLevel, strikePos);
                for (BlockPos check : BlockPos.betweenClosed(strikePos.offset(-2, -2, -2), strikePos.offset(2, 2, 2))) {
                    BlockState st = serverLevel.getBlockState(check);
                    if (st.getBlock() instanceof BiomassCapacitorBlock) {
                        BiomassCapacitorBlock.chargeFromLightning(serverLevel, check, st);
                    }
                }
            }
            return EventResult.pass();
        });
    }

    private static boolean hasGalvanicSurge(LivingEntity entity) {
        if (entity == null || !ModEffects.GALVANIC_SURGE.isBound()) {
            return false;
        }
        MobEffect targetEffect = ModEffects.GALVANIC_SURGE.get();
        for (MobEffectInstance inst : entity.getActiveEffects()) {
            if (inst.getEffect().value() == targetEffect) {
                return true;
            }
        }
        return false;
    }
}
