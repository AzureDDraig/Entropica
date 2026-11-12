package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import ddraig.net.entropica.spell.assembly.SubSpellSlot;
import ddraig.net.entropica.spell.engine.SpellCombatReactionEngine;
import ddraig.net.entropica.spell.engine.SpellExecutionEngine;
import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Chimera Materia (Failed T1–4 Byproduct / Catalyst)</b>
 * (Spellcrafting System Guide Section 6.49 — Dynamic Chromatic Shifting across Air, Earth, Water, &amp; Nether).
 */
public final class ChimeraSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int CHROMATIC_CHIMERA_HEX_COLOR = 0xFF55FF;

    @Override
    public EssenceType materiaType() {
        return EssenceType.CHIMERA;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Unstable Quad-Elemental Mutation Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Discordant Chimeric Swarm-Globule";
            case BEAM -> "Prismatic-Chimeric Flux Ray & Discordant Meltdown";
            case RADIAL_BURST -> "Chimeric Primal Roar Pulse";
            case TOUCH_STRIKE -> "Unstable Slag Cleave";
            case AURA -> "Unstable Hybrid Flux Shroud";
            case GLYPH -> "Discordant Spore-Geyser Trap";
            case CONE -> "Discordant Chimeric Breath Deluge";
            case SKYFALL -> "Cataclysmic Discordant Meteor Downpour";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Discordant Elemental Phase-Step";
            case TRANSMUTE, BREAK -> "Chimeric Catalytic Metamorphosis";
            case AFFLICTION -> "Discordant Chimeric Mutation Hex";
            default -> "Discordant Chimeric " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations (Unstable Quad-Elemental Mutation Flow) ---

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.ENCHANTED_HIT, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.04D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        switch (projectile.tickCount % 4) {
            case 0 -> level.sendParticles(ParticleTypes.FLAME, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
            case 1 -> level.sendParticles(ParticleTypes.SNOWFLAKE, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
            case 2 -> level.sendParticles(ParticleTypes.ELECTRIC_SPARK, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.02D);
            default -> level.sendParticles(ParticleTypes.SPLASH, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
        }
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        // Discordant Chimeric Swarm-Globule: shatters 3 armor + volatile 33% fire / 33% freeze-stone root 2.5s / 33% 3-foe lightning!
        if (!(hitEntity instanceof LivingEntity victim) || !victim.isAlive()) return;
        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(victim, caster, spell.hasWardFriendlyFireSafeguard())) return;

        SpellCombatReactionEngine.applyTimedAttributeShred(victim, level, 3.0D, 0.0D, 140L);
        switch (level.random.nextInt(3)) {
            case 0 -> {
                victim.igniteForSeconds(5);
                level.sendParticles(ParticleTypes.FLAME, victim.getX(), victim.getY(0.8D), victim.getZ(), 18, 0.4D, 0.4D, 0.4D, 0.05D);
            }
            case 1 -> {
                victim.setTicksFrozen(victim.getTicksRequiredToFreeze() + 60);
                victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 6));
                victim.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 50, 0));
                level.sendParticles(ParticleTypes.SNOWFLAKE, victim.getX(), victim.getY(0.8D), victim.getZ(), 18, 0.4D, 0.4D, 0.4D, 0.04D);
            }
            default -> {
                int zapped = 0;
                for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, victim.getBoundingBox().inflate(5.0D), e -> e.isAlive() && e != victim && e != caster)) {
                    if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(adj, caster, spell.hasWardFriendlyFireSafeguard())) continue;
                    adj.hurtServer(level, level.damageSources().lightningBolt(), 4.0F + spell.getEffectiveTier());
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK, adj.getX(), adj.getY(0.8D), adj.getZ(), 10, 0.25D, 0.3D, 0.25D, 0.05D);
                    if (++zapped >= 3) break;
                }
            }
        }
    }

    @Override
    public void onBeamFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 direction,
            double maxReach,
            List<LivingEntity> hitEntities,
            BlockHitResult blockHit,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        // Prismatic-Chimeric Flux Ray & Discordant Meltdown: melts 1 armor/s;
        // REQUIRES 5.0s sustained (stacks >= 5) -> Cataclysmic Discordant Meltdown (8m KB, melts 5 armor, Burn + Freeze + Paralyze 5.0s / 100t)!
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 1.0D, 0.0D, 80L);

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("ChimeraBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("ChimeraBeamStacks").orElse(0) + 1 : 1;
            data.putLong("ChimeraBeamLastTick", now);
            data.putInt("ChimeraBeamStacks", stacks);

            if (stacks >= 5) {
                data.putInt("ChimeraBeamStacks", 0);
                Vec3 away = direction.lengthSqr() > 1.0E-4D ? direction.normalize().scale(1.85D) : caster.getLookAngle().scale(1.85D);
                hit.setDeltaMovement(away.x, 0.42D, away.z);
                hit.hasImpulse = true;
                SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 5.0D, 0.0D, 200L);
                hit.igniteForSeconds(5);
                hit.setTicksFrozen(hit.getTicksRequiredToFreeze() + 100);
                hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 100, 0));
                hit.hurtServer(level, level.damageSources().indirectMagic(caster, caster), 10.5F + spell.getEffectiveTier() * 2.5F);
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 0.33F, 1.0F), hit.getX(), hit.getY(1.0D), hit.getZ(), 1, 0, 0, 0, 0);
            }
        }
    }

    @Override
    public void onRadialBurstFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 center,
            double radius,
            Vec3 direction,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        // Chimeric Primal Roar Pulse: 7m radius, strictly ZERO physical light blocks — stuns hostiles 2.5s (50t),
        // vaporizes incoming arrows, grants caster & allies +30% sprint speed & +25% attack damage for 8.0s (160t)!
        double r = Math.max(7.0D, radius);
        long roarUntil = level.getGameTime() + 160L;
        grantPrimalRoarBuff(caster, roarUntil);

        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.5D, center.z + r);
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, box, p -> p.getOwner() != caster && !(p instanceof SpellProjectileEntity))) {
            proj.discard();
        }
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (ent == caster || ent.isAlliedTo(caster) || (ent instanceof SpellEidolonEntity eid && caster.getUUID().equals(eid.getOwnerUuid()))) {
                grantPrimalRoarBuff(ent, roarUntil);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            ent.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 50, 0));
        }
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 0.33F, 1.0F), center.x, center.y + 1.0D, center.z, 1, 0, 0, 0, 0);
        level.playSound(null, BlockPos.containing(center), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.0F, 1.15F);
    }

    private static void grantPrimalRoarBuff(LivingEntity ally, long until) {
        ally.addEffect(new MobEffectInstance(MobEffects.SPEED, 160, 1));
        EntityHelper.getPersistentData(ally).putLong(SpellLingeringZoneManager.TAG_CHIMERA_PRIMAL_ROAR_UNTIL, until);
    }

    @Override
    public void onTouchStrikeFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 direction,
            List<LivingEntity> hitEntities,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        // Unstable Slag Cleave: 6m KB, melts 3 armor points, silences mob special abilities 5.0s (100t)!
        Vec3 push = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.setDeltaMovement(push.x * 1.45D, 0.35D, push.z * 1.45D);
            hit.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 140L);
            SpellLingeringZoneManager.applyArcaneSilence(hit, 100);
        }
    }

    @Override
    public void onAuraFormCast(
            ServerLevel level,
            LivingEntity caster,
            double radius,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        // Unstable Hybrid Flux Shroud: -70% mob detection, 20% dodge, vaporizes arrows within 3m, drains 1 heart/s flux!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_CHIMERA_SHROUD_UNTIL, level.getGameTime() + dur);
        level.sendParticles(ParticleTypes.ENCHANTED_HIT, caster.getX(), caster.getY(0.9D), caster.getZ(), 24, 0.45D, 0.55D, 0.45D, 0.06D);
    }

    @Override
    public boolean onGlyphFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 glyphCenter,
            double radius,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        // Discordant Spore-Geyser Trap: launches hostiles 8m high, shatters 3 armor, lingering cloud blinding 5.0s (100t)!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 3.0D, 300, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.ENCHANTED_HIT, zone.center.x, zone.center.y + 0.1D, zone.center.z, 4, 0.7D, 0.05D, 0.7D, 0.02D);
                    }
                    if (elapsed < 10) return true;

                    AABB trigger = new AABB(zone.center.x - 2.4D, zone.center.y - 1.0D, zone.center.z - 2.4D,
                            zone.center.x + 2.4D, zone.center.y + 2.0D, zone.center.z + 2.4D);
                    List<LivingEntity> foes = srv.getEntitiesOfClass(LivingEntity.class, trigger, e -> e.isAlive() && e != owner
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard())));
                    if (foes.isEmpty()) return true;

                    for (LivingEntity v : foes) {
                        SpellCombatReactionEngine.applyTimedAttributeShred(v, srv, 3.0D, 0.0D, 160L);
                        v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                        v.setDeltaMovement(0.0D, 1.18D, 0.0D);
                        v.hasImpulse = true;
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, new Vec3(0, 1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                    }
                    srv.sendParticles(ParticleTypes.LAVA, zone.center.x, zone.center.y + 0.5D, zone.center.z, 16, 1.0D, 0.4D, 1.0D, 0.05D);
                    srv.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, zone.center.x, zone.center.y + 0.6D, zone.center.z, 24, 1.3D, 0.5D, 1.3D, 0.04D);
                    return false;
                }
        ));
        return true;
    }

    @Override
    public void onConeFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 direction,
            double coneRange,
            List<LivingEntity> hitTargets,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        // Discordant Chimeric Breath Deluge: melts 4 armor, Discordant Genetic Panic (25.0s / 500t AI disruption, -45% slow, & panic flee)!
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 4.0D, 0.0D, 160L);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 500, 2));
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 500);
            SpellLingeringZoneManager.applyFearFlee(hit, caster.position(), 120);
        }
    }

    @Override
    public boolean onSkyfallFormCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 impactCenter,
            double radius,
            SpellAssembly spell,
            SubSpellSlot slot,
            @Nullable EssenceType fallbackMateria,
            float apparatusPotency
    ) {
        // Cataclysmic Discordant Meteor Downpour: Extended 18.0s (360t) storm + 8.0s (160t) mutating slag puddle (520t total)
        // across 12m zone — knocks flat, shatters 5 armor, 60% slow & burning slag!
        double r = Math.max(12.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 520, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    boolean stormActive = elapsed <= 360;
                    if (elapsed % 6 == 0) {
                        srv.sendParticles(ParticleTypes.ENCHANTED_HIT, zone.center.x, zone.center.y + (stormActive ? 3.8D : 0.3D), zone.center.z,
                                20, zone.radius * 0.55D, stormActive ? 2.0D : 0.2D, zone.radius * 0.55D, 0.06D);
                        srv.sendParticles(ParticleTypes.LAVA, zone.center.x, zone.center.y + (stormActive ? 2.5D : 0.2D), zone.center.z,
                                8, zone.radius * 0.50D, 0.5D, zone.radius * 0.50D, 0.04D);
                    }
                    if (elapsed % 16 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 6.0D, zone.center.z + zone.radius);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 3)); // 60% slow
                            foe.igniteForSeconds(3);
                            if (stormActive) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 5.0D, 0.0D, 160L);
                                foe.setDeltaMovement(foe.getDeltaMovement().x * 0.3D, -0.35D, foe.getDeltaMovement().z * 0.3D);
                                foe.hasImpulse = true;
                                SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, foe, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.45F);
                            }
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    @Override
    public void onSummonFormSpawned(ServerLevel level, LivingEntity caster, Entity summonEntity, Vec3 spawnPos, double radius, SpellAssembly spell, SubSpellSlot slot) {
        CompoundTag tag = EntityHelper.getPersistentData(summonEntity);
        tag.putString("SpellEidolonMateria", EssenceType.CHIMERA.name());
        tag.putInt("SpellEidolonTintRGB", CHROMATIC_CHIMERA_HEX_COLOR);
        level.sendParticles(ParticleTypes.ENCHANTED_HIT, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 26, 0.45D, 0.55D, 0.45D, 0.06D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Discordant Elemental Phase-Step: 16m step — departure and arrival randomly trigger one of the base 4 elemental bursts!
        triggerRandomBase4Burst(level, caster, departurePos);
        triggerRandomBase4Burst(level, caster, arrivalPos);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(departurePos, arrivalPos).inflate(18.0D))) {
            if (mob.getTarget() == caster) mob.setTarget(null);
        }
    }

    private static void triggerRandomBase4Burst(ServerLevel level, LivingEntity caster, Vec3 center) {
        AABB box = new AABB(center.x - 3.5D, center.y - 1.5D, center.z - 3.5D, center.x + 3.5D, center.y + 2.5D, center.z + 3.5D);
        int roll = level.random.nextInt(4);
        switch (roll) {
            case 0 -> { // Air vacuum pop
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != caster)) {
                    if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
                    Vec3 away = foe.position().subtract(center).normalize().scale(1.25D);
                    foe.setDeltaMovement(away.x, 0.42D, away.z);
                    foe.hasImpulse = true;
                }
                level.sendParticles(ParticleTypes.GUST, center.x, center.y + 0.6D, center.z, 4, 0.5D, 0.3D, 0.5D, 0.02D);
            }
            case 1 -> { // Earth shockwave stun
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != caster)) {
                    if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
                }
                level.sendParticles(ParticleTypes.CRIT, center.x, center.y + 0.4D, center.z, 18, 1.0D, 0.2D, 1.0D, 0.06D);
            }
            case 2 -> { // Water cleansing wave
                caster.clearFire();
                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(caster);
                level.sendParticles(ParticleTypes.SPLASH, center.x, center.y + 0.6D, center.z, 24, 1.1D, 0.4D, 1.1D, 0.06D);
            }
            default -> { // Nether fire blast
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != caster)) {
                    if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
                    foe.igniteForSeconds(4);
                    foe.hurtServer(level, level.damageSources().onFire(), 3.5F);
                }
                level.sendParticles(ParticleTypes.FLAME, center.x, center.y + 0.6D, center.z, 22, 1.0D, 0.4D, 1.0D, 0.05D);
            }
        }
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Chimeric Catalytic Metamorphosis: mutates stone/dirt randomly across Base-4 (sandstone, granite, mud, netherrack/magma),
        // transmutes mob drops into Chimera Essence + Slag Nuggets, strips enchantments for raw crystals!
        boolean didItem = false;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.5D))) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) didItem = true;
        }
        if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE) || state.is(BlockTags.DIRT)) {
            BlockState out = switch (level.random.nextInt(4)) {
                case 0 -> Blocks.SANDSTONE.defaultBlockState();
                case 1 -> Blocks.GRANITE.defaultBlockState();
                case 2 -> Blocks.MUD.defaultBlockState();
                default -> level.random.nextBoolean() ? Blocks.NETHERRACK.defaultBlockState() : Blocks.MAGMA_BLOCK.defaultBlockState();
            };
            level.setBlockAndUpdate(pos, out);
            return true;
        }
        return didItem;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        if (stack.is(Items.SPIDER_EYE) || stack.is(Items.GUNPOWDER) || stack.is(Items.BONE) || stack.is(Items.ROTTEN_FLESH)) {
            ItemStack chimeraEssence = new ItemStack(ModItems.WEAK_ESSENCE.get(), Math.max(1, stack.getCount() / 2));
            EssenceItem.setEssenceType(chimeraEssence, EssenceType.CHIMERA);
            itemEntity.setItem(chimeraEssence);
            level.addFreshEntity(new ItemEntity(level, itemEntity.getX(), itemEntity.getY() + 0.2D, itemEntity.getZ(),
                    new ItemStack(level.random.nextBoolean() ? Items.IRON_NUGGET : Items.GOLD_NUGGET, stack.getCount())));
            return true;
        }
        ItemEnchantments ench = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (!ench.isEmpty()) {
            int crystalCount = Math.min(16, ench.size() * 2);
            stack.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            itemEntity.setItem(stack.copy());
            level.addFreshEntity(new ItemEntity(level, itemEntity.getX(), itemEntity.getY() + 0.25D, itemEntity.getZ(),
                    new ItemStack(Items.AMETHYST_SHARD, crystalCount)));
            level.addFreshEntity(new ItemEntity(level, itemEntity.getX(), itemEntity.getY() + 0.25D, itemEntity.getZ(),
                    new ItemStack(Items.QUARTZ, crystalCount)));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Discordant Chimeric Mutation Hex: -25% Max Health, blocks conventional healing, -35% toughness,
        // random Base-4 spasms every 3.5s (Nether burn, Earth stun, Water drowning slow, or Air sky launch)!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        long now = level.getGameTime();
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.80D, 0.35D * mult), 45L);
        target.hurtServer(level, level.damageSources().magic(), (1.55F + potencyTier * 0.50F) * mult);
        if ((now / 20L) % 4L == 0L) {
            switch (level.random.nextInt(4)) {
                case 0 -> target.igniteForSeconds(4);
                case 1 -> target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
                case 2 -> {
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 3));
                    SpellLingeringZoneManager.applyAbyssLandDrowning(target, 60);
                }
                default -> {
                    target.setDeltaMovement(target.getDeltaMovement().x, 0.72D, target.getDeltaMovement().z);
                    target.hasImpulse = true;
                }
            }
        }
        return true;
    }
}
