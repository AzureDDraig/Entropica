package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.registry.ModEffects;
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
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Genesis Materia (Tier 3 Trinity Fusion #46)</b>
 * (Spellcrafting System Guide Section 6.47 — Vitae + Radiant + Water).
 */
public final class GenesisSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int DAWN_GENESIS_HEX_COLOR = 0xFFEA78;

    @Override
    public EssenceType materiaType() {
        return EssenceType.GENESIS;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Dawn-Tide Lotus Orbit Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Primordial Dawn-Tear Globule";
            case BEAM -> "Primordial Genesis Creation Ray & Rebirth Pulse";
            case RADIAL_BURST -> "Dawn-Tide Rebirth Nova";
            case TOUCH_STRIKE -> "Primordial Inception Palm-Strike";
            case AURA -> "Amniotic Genesis Shroud";
            case GLYPH -> "Well of Creation & Cleansing Geyser";
            case CONE -> "Dawn-Tide Rejuvenation Deluge";
            case SKYFALL -> "Cataclysmic Waters of Creation Downpour";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Dawn-Glory Solar Slipstream";
            case TRANSMUTE, BREAK -> "Primordial Creation & Genesis Rebirth Fusion";
            case AFFLICTION -> "Hyper-Cellular Genesis Destabilization Hex";
            default -> "Primordial Genesis " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations (Dawn-Tide Lotus Orbit Flow) ---

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
        level.sendParticles(ParticleTypes.SPLASH, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.01D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        splashAmnioticDew(level, caster, hitEntity.position(), spell.hasWardFriendlyFireSafeguard());
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        splashAmnioticDew(level, caster, blockHit.getLocation(), spell.hasWardFriendlyFireSafeguard());
    }

    private static void splashAmnioticDew(ServerLevel level, @Nullable LivingEntity caster, Vec3 center, boolean safeguard) {
        // 4m splash: heals allies 3 hearts (6.0 HP) + Regen II; strips 3 armor on Undead & roots foes 2.0s (40t)!
        AABB box = new AABB(center.x - 4.0D, center.y - 1.5D, center.z - 4.0D, center.x + 4.0D, center.y + 2.5D, center.z + 4.0D);
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (caster != null && isFriendlyTarget(ent, caster)) {
                SpellCombatReactionEngine.notifyMagicalHeal(ent, 6.0F);
                ent.heal(6.0F);
                ent.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 1));
                continue;
            }
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, safeguard)) continue;
            if (ent.getType().is(EntityTypeTags.UNDEAD)) {
                SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, 3.0D, 0.0D, 140L);
            }
            ent.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 6));
            ent.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 40, 0));
        }
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 0.6D, center.z, 22, 1.4D, 0.5D, 1.4D, 0.05D);
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
        // Primordial Genesis Creation Ray & Rebirth Pulse: heals allies 2 hearts/s (4 HP) + saturation;
        // REQUIRES 5.0s sustained (stacks >= 5) -> Cataclysmic Rebirth Pulse (8m KB, melts 5 armor, +6 Absorption Hearts & Speed II 15s)!
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (isFriendlyTarget(hit, caster)) {
                SpellCombatReactionEngine.notifyMagicalHeal(hit, 4.0F);
                hit.heal(4.0F);
                if (hit instanceof Player p) {
                    p.getFoodData().eat(1, 0.4F);
                }
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("GenesisBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("GenesisBeamStacks").orElse(0) + 1 : 1;
            data.putLong("GenesisBeamLastTick", now);
            data.putInt("GenesisBeamStacks", stacks);

            if (stacks >= 5) {
                data.putInt("GenesisBeamStacks", 0);
                Vec3 away = direction.lengthSqr() > 1.0E-4D ? direction.normalize().scale(1.85D) : caster.getLookAngle().scale(1.85D);
                hit.setDeltaMovement(away.x, 0.42D, away.z);
                hit.hasImpulse = true;
                SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 5.0D, 0.0D, 200L);
                hit.hurtServer(level, level.damageSources().indirectMagic(caster, caster), 10.0F + spell.getEffectiveTier() * 2.4F);

                grantFirstDawnAegis(caster);
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(10.0D), e -> isFriendlyTarget(e, caster))) {
                    grantFirstDawnAegis(ally);
                }
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 0.92F, 0.47F), hit.getX(), hit.getY(1.0D), hit.getZ(), 1, 0, 0, 0, 0);
            }
        }
    }

    private static void grantFirstDawnAegis(LivingEntity ally) {
        ally.setAbsorptionAmount(Math.max(ally.getAbsorptionAmount(), 12.0F)); // +6 Absorption Hearts
        ally.addEffect(new MobEffectInstance(MobEffects.SPEED, 300, 1));
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
        // Dawn-Tide Rebirth Nova: 6m radius, strictly ZERO physical light blocks — 8m KB, strips 3 armor,
        // cures negative status effects on caster/allies, leaves 5.0s (100t) Aura of Inception (+1 heart/s, +4 Armor, +2 Toughness)!
        double r = Math.max(6.0D, radius);
        SpellCombatReactionEngine.purifyHexesAndNegativeEffects(caster);

        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.5D, center.z + r);
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (isFriendlyTarget(ent, caster)) {
                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(ent);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = ent.position().subtract(center).normalize().scale(1.85D);
            ent.setDeltaMovement(away.x, 0.40D, away.z);
            ent.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, 3.0D, 0.0D, 160L);
        }

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, center, r, 100, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB aura = new AABB(zone.center.x - zone.radius, zone.center.y - 1.5D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 3.0D, zone.center.z + zone.radius);
                        for (LivingEntity ally : srv.getEntitiesOfClass(LivingEntity.class, aura, e -> owner != null && isFriendlyTarget(e, owner))) {
                            SpellCombatReactionEngine.notifyMagicalHeal(ally, 2.0F);
                            ally.heal(2.0F);
                            SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, srv, 4.0D, 2.0D, 0.0D, 0.0D, 35L);
                        }
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 0.5D, zone.center.z, 14, 2.0D, 0.3D, 2.0D, 0.03D);
                    }
                    return true;
                }
        ));
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 0.92F, 0.47F), center.x, center.y + 1.0D, center.z, 1, 0, 0, 0, 0);
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
        // Primordial Inception Palm-Strike: strips 3 armor, 6m KB, leaves 3m Amniotic Waters of Origin puddle
        // healing allies 2 hearts + Saturation while rooting foes 2.5s (50t) and setting +25% damage vulnerability!
        Vec3 push = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        Vec3 puddlePos = hitEntities.isEmpty() ? caster.position().add(push.scale(2.0D)) : hitEntities.getFirst().position();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 140L);
            hit.setDeltaMovement(push.x * 1.45D, 0.35D, push.z * 1.45D);
            hit.hasImpulse = true;
        }
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, puddlePos, 3.0D, 80, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        long now = srv.getGameTime();
                        AABB box = new AABB(zone.center.x - 3.0D, zone.center.y - 1.0D, zone.center.z - 3.0D,
                                zone.center.x + 3.0D, zone.center.y + 2.0D, zone.center.z + 3.0D);
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                            if (owner != null && isFriendlyTarget(ent, owner)) {
                                SpellCombatReactionEngine.notifyMagicalHeal(ent, 4.0F);
                                ent.heal(4.0F);
                                if (ent instanceof Player p) p.getFoodData().eat(1, 0.5F);
                            } else if (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, owner, zone.spell.hasWardFriendlyFireSafeguard())) {
                                ent.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 6));
                                ent.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 50, 0));
                                EntityHelper.getPersistentData(ent).putLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL, now + 50L);
                            }
                        }
                    }
                    return true;
                }
        ));
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
        // Amniotic Genesis Shroud: +7 Armor, +3 Toughness, Water Breathing + swim boost, 40% damage -> Regen II & Materia fuel!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_GENESIS_SHROUD_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 7.0D, 3.0D, 0.0D, 0.0D, dur);
        caster.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, dur + 40, 0, false, false));
        caster.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, dur, 0, false, false));
        level.sendParticles(ParticleTypes.END_ROD, caster.getX(), caster.getY(0.9D), caster.getZ(), 24, 0.45D, 0.55D, 0.45D, 0.04D);
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
        // Well of Creation & Cleansing Geyser:
        // Ally step -> Fountain of Inception (6 hearts / 12 HP, cures curses & hexes, +4 Absorption Hearts / 8 HP);
        // Hostile step -> Cataclysmic Cleansing Geyser (10m launch, purges buffs, shreds 3 armor, +50% fall vuln 6s)!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 3.0D, 300, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 0.15D, zone.center.z, 4, 0.7D, 0.05D, 0.7D, 0.01D);
                    }
                    if (elapsed < 12) return true;

                    AABB trigger = new AABB(zone.center.x - 2.3D, zone.center.y - 1.0D, zone.center.z - 2.3D,
                            zone.center.x + 2.3D, zone.center.y + 2.0D, zone.center.z + 2.3D);
                    List<LivingEntity> steppers = srv.getEntitiesOfClass(LivingEntity.class, trigger, LivingEntity::isAlive);
                    if (steppers.isEmpty()) return true;

                    for (LivingEntity st : steppers) {
                        if (owner != null && isFriendlyTarget(st, owner)) {
                            SpellCombatReactionEngine.purifyHexesAndNegativeEffects(st);
                            for (EquipmentSlot eq : EquipmentSlot.values()) {
                                PenumbraSpellMutationHandler.purgeCursesFromStack(st.getItemBySlot(eq));
                            }
                            SpellCombatReactionEngine.notifyMagicalHeal(st, 12.0F);
                            st.heal(12.0F);
                            st.setAbsorptionAmount(Math.max(st.getAbsorptionAmount(), 8.0F));
                            srv.sendParticles(ParticleTypes.HEART, st.getX(), st.getY(1.0D), st.getZ(), 10, 0.35D, 0.4D, 0.35D, 0.03D);
                            return false;
                        }
                    }

                    long fallVuln = srv.getGameTime() + 120L;
                    for (LivingEntity foe : steppers) {
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                        MiasmaSpellMutationHandler.purgeBeneficialBuffs(foe);
                        SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 3.0D, 0.0D, 160L);
                        EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_FALL_VULN_50_UNTIL, fallVuln);
                        foe.setDeltaMovement(0.0D, 1.32D, 0.0D); // 10m vertical geyser launch!
                        foe.hasImpulse = true;
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, foe, new Vec3(0, 1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                    }
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
        // Dawn-Tide Rejuvenation Deluge: purges debuffs & curses from allies (+2 hearts & hunger);
        // on hostiles -> melts armor, -50% move speed, & 25.0s (500t) AI pathfinding disruption!
        for (LivingEntity hit : hitTargets) {
            if (isFriendlyTarget(hit, caster)) {
                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(hit);
                for (EquipmentSlot eq : EquipmentSlot.values()) {
                    PenumbraSpellMutationHandler.purgeCursesFromStack(hit.getItemBySlot(eq));
                }
                SpellCombatReactionEngine.notifyMagicalHeal(hit, 4.0F);
                hit.heal(4.0F);
                if (hit instanceof Player p) p.getFoodData().eat(1, 0.3F);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 2.0D, 0.0D, 120L);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 500);
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
        // Cataclysmic Waters of Creation Downpour: Extended 18.0s (360t) storm + 8.0s (160t) shimmering pool (520t total)
        // across 12m zone — knocks flat, shatters 5 armor, heals allies +1 heart/s, slows foes 60%!
        double r = Math.max(12.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 520, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    boolean stormActive = elapsed <= 360;
                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + (stormActive ? 3.8D : 0.3D), zone.center.z,
                                20, zone.radius * 0.55D, stormActive ? 1.8D : 0.2D, zone.radius * 0.55D, 0.04D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 6.0D, zone.center.z + zone.radius);
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (owner != null && isFriendlyTarget(ent, owner)) {
                                SpellCombatReactionEngine.notifyMagicalHeal(ent, 2.0F);
                                ent.heal(2.0F);
                                continue;
                            }
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            ent.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 3)); // 60% slow
                            if (stormActive) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(ent, srv, 5.0D, 0.0D, 160L);
                                ent.setDeltaMovement(ent.getDeltaMovement().x * 0.3D, -0.35D, ent.getDeltaMovement().z * 0.3D);
                                ent.hasImpulse = true;
                                SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, ent, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.45F);
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
        tag.putString("SpellEidolonMateria", EssenceType.GENESIS.name());
        tag.putInt("SpellEidolonTintRGB", DAWN_GENESIS_HEX_COLOR);
        level.sendParticles(ParticleTypes.END_ROD, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 24, 0.45D, 0.55D, 0.45D, 0.04D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Dawn-Glory Solar Slipstream: 18m solar water jet — knocks aside, blinds 4.0s (80t), & smites passed-through hostiles!
        AABB path = new AABB(departurePos, arrivalPos).inflate(1.6D);
        for (LivingEntity passed : level.getEntitiesOfClass(LivingEntity.class, path, e -> e.isAlive() && e != caster)) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(passed, caster, true)) continue;
            Vec3 side = passed.position().subtract(departurePos).normalize().scale(1.1D);
            passed.setDeltaMovement(side.x, 0.32D, side.z);
            passed.hasImpulse = true;
            passed.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            passed.hurtServer(level, level.damageSources().indirectMagic(caster, caster),
                    (passed.getType().is(EntityTypeTags.UNDEAD) ? 6.0F : 3.5F) + potencyTier);
        }
        for (Mob mob : level.getEntitiesOfClass(Mob.class, path.inflate(16.0D))) {
            if (mob.getTarget() == caster) mob.setTarget(null);
        }
        level.sendParticles(ParticleTypes.END_ROD, arrivalPos.x, arrivalPos.y + 0.8D, arrivalPos.z, 20, 0.4D, 0.4D, 0.4D, 0.05D);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Primordial Creation & Genesis Rebirth Fusion: cures Zombie Villagers, Netherrack/SoulSand/Basalt -> Grass/Clay/Flowering Azalea,
        // cleanses cursed equipment!
        boolean didAny = SpringSpellMutationHandler.cureNearbyZombieVillagers(level, pos);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.5D))) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) didAny = true;
        }
        if (caster != null) {
            for (EquipmentSlot eq : EquipmentSlot.values()) {
                if (PenumbraSpellMutationHandler.purgeCursesFromStack(caster.getItemBySlot(eq))) didAny = true;
            }
        }
        if (state.is(Blocks.NETHERRACK) || state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL) || state.is(Blocks.BASALT)) {
            BlockState out = switch (level.random.nextInt(3)) {
                case 0 -> Blocks.GRASS_BLOCK.defaultBlockState();
                case 1 -> Blocks.CLAY.defaultBlockState();
                default -> Blocks.MOSS_BLOCK.defaultBlockState();
            };
            level.setBlockAndUpdate(pos, out);
            if (level.getBlockState(pos.above()).isAir() && level.random.nextBoolean()) {
                level.setBlockAndUpdate(pos.above(), Blocks.FLOWERING_AZALEA.defaultBlockState());
            }
            return true;
        }
        return didAny;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        if (PenumbraSpellMutationHandler.purgeCursesFromStack(stack)) {
            itemEntity.setItem(stack.copy());
            return true;
        }
        if (stack.is(Items.NETHERRACK) || stack.is(Items.BASALT)) {
            itemEntity.setItem(new ItemStack(Items.FLOWERING_AZALEA, stack.getCount()));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Hyper-Cellular Genesis Destabilization Hex: -25% Max Health, blocks conventional healing, -35% toughness,
        // staggers attacks, 1 heart (2.0 HP) true damage every 3.5s, expels radiant sparks healing adjacent opponents!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        long now = level.getGameTime();
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.80D, 0.35D * mult), 45L);
        target.stopUsingItem();
        target.hurtServer(level, level.damageSources().magic(), 2.0F * mult);
        if ((now / 20L) % 4L == 0L) {
            LivingEntity caster = SpellCombatReactionEngine.resolveHexCaster(level, target);
            if (caster != null && caster.isAlive() && caster.distanceToSqr(target) <= 100.0D) {
                SpellCombatReactionEngine.notifyMagicalHeal(caster, 2.5F);
                caster.heal(2.5F);
            } else {
                for (Monster opp : level.getEntitiesOfClass(Monster.class, target.getBoundingBox().inflate(5.0D), e -> e != target && e.isAlive())) {
                    opp.heal(2.5F);
                    break;
                }
            }
            level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.9D), target.getZ(), 8, 0.35D, 0.4D, 0.35D, 0.04D);
        }
        return true;
    }

    private static boolean isFriendlyTarget(LivingEntity entity, LivingEntity caster) {
        return entity == caster || entity.isAlliedTo(caster)
                || (entity instanceof SpellEidolonEntity eid && caster.getUUID().equals(eid.getOwnerUuid()));
    }
}
