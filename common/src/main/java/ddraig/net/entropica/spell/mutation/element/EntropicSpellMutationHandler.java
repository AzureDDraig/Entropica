package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.registry.ModBlocks;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
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
 * Complete per-part Signature Mutation implementation for <b>Entropic Materia (Tier 4 Apex Fusion #49)</b>
 * (Spellcrafting System Guide Section 6.51 — Void + Umbral + Undead + Blood catalyzed by Chimera).
 */
public final class EntropicSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int ENTROPIC_VOID_INDIGO_HEX_COLOR = 0x41304E;

    @Override
    public EssenceType materiaType() {
        return EssenceType.ENTROPIC;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Abyssal Sanguine-Void Chain Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Entropic Singularity Skull-Orb";
            case BEAM -> "Entropic Oblivion-Siphon Ray & Event Horizon Cataclysm";
            case RADIAL_BURST -> "Entropic Singularity Implosion Nova";
            case TOUCH_STRIKE -> "Entropic Singularity Cleave";
            case AURA -> "Abyssal Event Horizon Mantle";
            case GLYPH -> "Entropic Singularity Collapse Sigil";
            case CONE -> "Entropic Oblivion-Flame Deluge";
            case SKYFALL -> "Cataclysmic Entropic Skyfall";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Event Horizon Phase-Slip";
            case TRANSMUTE, BREAK -> "Apex Thermodynamic Dissolution & Corruption";
            case AFFLICTION -> "Apex Thermodynamic Decay & Exsanguination Hex";
            default -> "Entropic Singularity " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations (Abyssal Sanguine-Void Chain Flow) ---

    @Override
    public void onChainLeapStep(ServerLevel level, LivingEntity caster, LivingEntity fromTarget, LivingEntity toTarget, SpellAssembly spell, SubSpellSlot slot) {
        // Each chained target (up to 5 within 8m through walls, 70% damage in SpellExecutionEngine)
        // has 3 armor shattered, healing suppressed, and siphons health back to the caster!
        SpellCombatReactionEngine.applyTimedAttributeShred(toTarget, level, 3.0D, 0.0D, 160L);
        EntityHelper.getPersistentData(toTarget).putLong(SpellLingeringZoneManager.TAG_BLOOD_HEMORRHAGE_UNTIL, level.getGameTime() + 140L);
        SpellCombatReactionEngine.notifyMagicalHeal(caster, 2.5F);
        caster.heal(2.5F);
        level.sendParticles(ParticleTypes.CRIMSON_SPORE, toTarget.getX(), toTarget.getY(0.8D), toTarget.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.03D);
    }

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.02D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileSpawnedFromForm(ServerLevel level, SpellProjectileEntity projectile, LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        projectile.setNoGravity(true);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.02D);
        level.sendParticles(ParticleTypes.CRIMSON_SPORE, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        if (projectile.getPierceCountRemaining() <= 0) {
            collapseThermodynamicSingularityWell(level, caster, hitEntity.position(), spell);
        }
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        if (!projectile.isWallPhasing()) {
            collapseThermodynamicSingularityWell(level, caster, blockHit.getLocation(), spell);
        }
    }

    @Override
    public void onProjectileExpiredFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        collapseThermodynamicSingularityWell(level, caster, projectile.position(), spell);
    }

    private static void collapseThermodynamicSingularityWell(ServerLevel level, @Nullable LivingEntity caster, Vec3 center, SpellAssembly spell) {
        // 7m Thermodynamic Singularity Well: vacuums inward, -5 armor, Wither III + Hemorrhage 6.0s (120t), 30% lifesteal!
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        long now = level.getGameTime();
        float totalDealt = 0.0F;
        AABB well = new AABB(center.x - 7.0D, center.y - 2.5D, center.z - 7.0D, center.x + 7.0D, center.y + 4.5D, center.z + 7.0D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, well, LivingEntity::isAlive)) {
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, safeguard)) continue;
            Vec3 in = center.subtract(foe.position()).scale(0.42D);
            foe.setDeltaMovement(in.x, 0.2D, in.z);
            foe.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 5.0D, 0.0D, 160L);
            foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 2));
            foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.BLEEDING.get()), 120, 0));
            EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_BLOOD_HEMORRHAGE_UNTIL, now + 120L);
            float dmg = 6.0F + spell.getEffectiveTier() * 1.4F;
            foe.hurtServer(level, level.damageSources().fellOutOfWorld(), dmg);
            totalDealt += dmg;
        }
        if (caster != null && caster.isAlive() && totalDealt > 0.0F) {
            float heal = totalDealt * 0.30F;
            SpellCombatReactionEngine.notifyMagicalHeal(caster, heal);
            caster.heal(heal);
        }
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 0.6D, center.z, 32, 1.8D, 0.5D, 1.8D, 0.08D);
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
        // Entropic Oblivion-Siphon Ray & Event Horizon Cataclysm: -2 armor/s, 1 heart/s siphon, disintegrates arrows;
        // 5.0s continuous -> 12m KB, -6 armor, purges buffs, Blindness + Wither III + Slowness IV 5.0s!
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(level, caster,
                new AABB(caster.getEyePosition(), caster.getEyePosition().add(dir.scale(maxReach))).inflate(2.0D), null, 0L);

        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 2.0D, 0.0D, 80L);
            SpellCombatReactionEngine.notifyMagicalHeal(caster, 2.0F);
            caster.heal(2.0F);

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("EntropicBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("EntropicBeamStacks").orElse(0) + 1 : 1;
            data.putLong("EntropicBeamLastTick", now);
            data.putInt("EntropicBeamStacks", stacks);

            if (stacks >= 5) {
                data.putInt("EntropicBeamStacks", 0);
                ApexEschatonSingularityHelper.purgeBeneficialBuffs(hit);
                SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 6.0D, 0.0D, 200L);
                hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                hit.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 2));
                hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 3));
                hit.setDeltaMovement(dir.x * 2.4D, 0.45D, dir.z * 2.4D); // 12m KB!
                hit.hasImpulse = true;
                hit.hurtServer(level, level.damageSources().fellOutOfWorld(), 14.0F + spell.getEffectiveTier() * 2.5F);
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
        // Entropic Singularity Implosion Nova: 10m shockwave (ZERO light blocks), vacuums then launches 10m back,
        // -5 armor, grants Apex Entropic Shroud (12.0s / 240t: +10 Armor, +5 Toughness, 40% reflect, 25% lifesteal)!
        double r = Math.max(10.0D, radius);
        AABB box = new AABB(center.x - r, center.y - 2.5D, center.z - r, center.x + r, center.y + 4.5D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != caster)) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = foe.position().subtract(center).normalize().scale(2.05D);
            foe.setDeltaMovement(away.x, 0.45D, away.z);
            foe.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 5.0D, 0.0D, 160L);
        }

        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_ENTROPIC_LIFESTEAL_SHROUD_UNTIL, level.getGameTime() + 240L);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 10.0D, 5.0D, 0.0D, 0.0D, 240L);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 0.8D, center.z, 36, 2.5D, 0.5D, 2.5D, 0.08D);
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
        // Entropic Singularity Cleave: 50% bypass, -5 armor, disables shields 5.0s, 35% lifesteal,
        // leaves 5.0s (100t) fissure of light-devouring rot slowing 60% and inflicting Wither II!
        Vec3 fissurePos = hitEntities.isEmpty() ? caster.position().add(caster.getLookAngle().scale(2.2D)) : hitEntities.getFirst().position();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 5.0D, 0.0D, 160L);
            ApexEschatonSingularityHelper.disableShield(hit, 100);
        }

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, fissurePos, 4.0D, 100, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB rot = new AABB(zone.center.x - 4.0D, zone.center.y - 1.5D, zone.center.z - 4.0D,
                                zone.center.x + 4.0D, zone.center.y + 2.5D, zone.center.z + 4.0D);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, rot, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 3)); // 60% slow
                            foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 45, 1));
                        }
                        srv.sendParticles(ParticleTypes.SMOKE, zone.center.x, zone.center.y + 0.25D, zone.center.z, 10, 1.4D, 0.15D, 1.4D, 0.02D);
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
        int dur = 240 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_ENTROPIC_SHROUD_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 10.0D, 5.0D, 0.0D, 0.0D, dur);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, caster.getX(), caster.getY(0.9D), caster.getZ(), 28, 0.5D, 0.6D, 0.5D, 0.05D);
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
        // Entropic Singularity Collapse Sigil: 4m trigger -> 8m zone vacuuming inward, launching 12m straight up,
        // -5 armor, strips buffs, Wither III + Hemorrhage + Blindness 5.0s (100t), then slamming down!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 4.0D, 360, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.REVERSE_PORTAL, zone.center.x, zone.center.y + 0.15D, zone.center.z, 4, 0.9D, 0.05D, 0.9D, 0.02D);
                    }
                    if (elapsed < 10) return true;

                    AABB trig = new AABB(zone.center.x - 4.0D, zone.center.y - 1.5D, zone.center.z - 4.0D,
                            zone.center.x + 4.0D, zone.center.y + 2.5D, zone.center.z + 4.0D);
                    if (srv.getEntitiesOfClass(LivingEntity.class, trig, e -> e.isAlive() && e != owner
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard()))).isEmpty()) {
                        return true;
                    }

                    long now = srv.getGameTime();
                    AABB blast = new AABB(zone.center.x - 8.0D, zone.center.y - 2.0D, zone.center.z - 8.0D,
                            zone.center.x + 8.0D, zone.center.y + 5.0D, zone.center.z + 8.0D);
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, blast, e -> e.isAlive() && e != owner)) {
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                        ApexEschatonSingularityHelper.purgeBeneficialBuffs(foe);
                        SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 5.0D, 0.0D, 180L);
                        Vec3 in = zone.center.subtract(foe.position()).scale(0.38D);
                        foe.setDeltaMovement(in.x, 1.48D, in.z);
                        foe.hasImpulse = true;
                        foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 2));
                        foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                        foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.BLEEDING.get()), 100, 0));
                        EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_BLOOD_HEMORRHAGE_UNTIL, now + 100L);
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, foe, new Vec3(0, 1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                    }

                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 8.0D, 35, now, zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl < 35) return true;
                                LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                AABB slam = new AABB(sZone.center.x - 8.0D, sZone.center.y - 2.0D, sZone.center.z - 8.0D,
                                        sZone.center.x + 8.0D, sZone.center.y + 18.0D, sZone.center.z + 8.0D);
                                for (LivingEntity foe : sLvl.getEntitiesOfClass(LivingEntity.class, slam, e -> e.isAlive() && e != sOwner)) {
                                    if (sOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, sOwner, sZone.spell.hasWardFriendlyFireSafeguard())) continue;
                                    foe.setDeltaMovement(0.0D, -2.2D, 0.0D);
                                    foe.hasImpulse = true;
                                    foe.hurtServer(sLvl, sLvl.damageSources().fall(), 10.0F + sZone.spell.getEffectiveTier() * 2.0F);
                                }
                                return false;
                            }
                    ));
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
        // Entropic Oblivion-Flame Deluge: -5 armor, incinerates wooden blocks, boils water,
        // inflicts Thermodynamic Rot-Frenzy (25.0s / 500t AI disruption + blind frenzy attacking everything around)!
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        BlockPos mid = BlockPos.containing(caster.getEyePosition().add(dir.scale(Math.min(6.0D, coneRange * 0.5D))));
        for (BlockPos p : BlockPos.betweenClosed(mid.offset(-3, -2, -3), mid.offset(3, 2, 3))) {
            BlockState bs = level.getBlockState(p);
            if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
            if (bs.is(Blocks.WATER)) {
                level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
            } else if ((bs.is(BlockTags.LOGS) || bs.is(BlockTags.PLANKS) || bs.is(BlockTags.LEAVES)) && level.random.nextFloat() < 0.35F) {
                level.setBlockAndUpdate(p, Blocks.FIRE.defaultBlockState());
            }
        }

        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 5.0D, 0.0D, 180L);
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0));
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 500);
            SpellLingeringZoneManager.applyMirageTraitorFrenzy(hit, 500);
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
        // Cataclysmic Entropic Skyfall: 14m zone, 20.0s (400t) + 10.0s (200t) crater (600t total):
        // knocks flat, -6 armor; lingering 10s slows 70%, Wither III, drains 2.0 hearts/s (4.0 HP) true damage to heal caster!
        double r = Math.max(14.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 600, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    boolean bombardment = elapsed <= 400;
                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.REVERSE_PORTAL, zone.center.x, zone.center.y + (bombardment ? 4.5D : 0.4D), zone.center.z,
                                24, zone.radius * 0.55D, bombardment ? 2.2D : 0.3D, zone.radius * 0.55D, 0.06D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 8.0D, zone.center.z + zone.radius);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            if (bombardment) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 6.0D, 0.0D, 180L);
                                foe.setDeltaMovement(foe.getDeltaMovement().x * 0.25D, -0.45D, foe.getDeltaMovement().z * 0.25D);
                                foe.hasImpulse = true;
                                SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, foe, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.50F);
                            } else {
                                foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 4));
                                foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 45, 2));
                                foe.hurtServer(srv, srv.damageSources().fellOutOfWorld(), 4.0F);
                                if (owner != null && owner.isAlive()) {
                                    SpellCombatReactionEngine.notifyMagicalHeal(owner, 4.0F);
                                    owner.heal(4.0F);
                                }
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
        tag.putString("SpellEidolonMateria", EssenceType.ENTROPIC.name());
        tag.putInt("SpellEidolonTintRGB", ENTROPIC_VOID_INDIGO_HEX_COLOR);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, spawnPos.x, spawnPos.y + 1.0D, spawnPos.z, 28, 0.5D, 0.6D, 0.5D, 0.06D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Event Horizon Phase-Slip: 20m through walls; departure 8m black hole (-4 armor, Blindness + Wither II 4.0s);
        // arrival grants 8.0s (160t) complete physical invulnerability & sheds 100% mob aggro!
        AABB dep = new AABB(departurePos.x - 8.0D, departurePos.y - 2.5D, departurePos.z - 8.0D,
                departurePos.x + 8.0D, departurePos.y + 4.5D, departurePos.z + 8.0D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, dep, e -> e.isAlive() && e != caster)) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
            Vec3 pull = departurePos.subtract(foe.position()).scale(0.38D);
            foe.setDeltaMovement(pull.x, 0.22D, pull.z);
            foe.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 4.0D, 0.0D, 140L);
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1));
        }

        EntityHelper.getPersistentData(caster)
                .putLong(SpellLingeringZoneManager.TAG_COMPLETE_INVULNERABILITY_UNTIL, level.getGameTime() + 160L);
        SpellLingeringZoneManager.applyIntangiblePhase(caster, 160, true);
        ApexEschatonSingularityHelper.shedMobAggro(level, caster, arrivalPos, 24.0D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.0F, 0.85F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Apex Thermodynamic Dissolution & Corruption [5.0s Channel]:
        // Stone/Cobble -> Blackstone/Deepslate/Crying Obsidian; Dirt/Grass -> Soul Sand/Sculk;
        // plants -> Wither Roses; mob drops -> Dark Blood/Void Essence; damaged metal gear -> +35% ingots/nuggets + slag!
        if (ApexEschatonSingularityHelper.beginOrExecuteChanneledTransmute(
                level, caster, pos, 8, potencyTier, EssenceType.ENTROPIC,
                "Apex Thermodynamic Dissolution & Corruption", null)) {
            return true;
        }

        if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE) || state.is(Blocks.MOSSY_COBBLESTONE)) {
            BlockState out = switch (level.random.nextInt(3)) {
                case 0 -> Blocks.BLACKSTONE.defaultBlockState();
                case 1 -> Blocks.DEEPSLATE.defaultBlockState();
                default -> Blocks.CRYING_OBSIDIAN.defaultBlockState();
            };
            level.setBlockAndUpdate(pos, out);
            return true;
        }
        if (state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.PODZOL) || state.is(Blocks.MOSS_BLOCK)) {
            level.setBlockAndUpdate(pos, level.random.nextBoolean() ? Blocks.SOUL_SAND.defaultBlockState() : Blocks.SCULK.defaultBlockState());
            return true;
        }
        if ((state.is(BlockTags.SMALL_FLOWERS) || state.is(BlockTags.SAPLINGS) || state.is(Blocks.SHORT_GRASS))
                && !state.is(Blocks.WITHER_ROSE)) {
            level.setBlockAndUpdate(pos, Blocks.WITHER_ROSE.defaultBlockState());
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;

        Item scrapIngot = ApexEschatonSingularityHelper.resolveDamagedMetalScrapIngot(stack);
        if (scrapIngot != null) {
            int bonusIngots = Math.max(1, Math.round(2 * 1.35F));
            itemEntity.setItem(new ItemStack(scrapIngot, bonusIngots));
            Item nugget = scrapIngot == Items.GOLD_INGOT ? Items.GOLD_NUGGET : Items.IRON_NUGGET;
            level.addFreshEntity(new ItemEntity(level, itemEntity.getX(), itemEntity.getY() + 0.2D, itemEntity.getZ(),
                    new ItemStack(nugget, 3)));
            level.addFreshEntity(new ItemEntity(level, itemEntity.getX(), itemEntity.getY() + 0.2D, itemEntity.getZ(),
                    new ItemStack(ModBlocks.ASTRAL_IMPACTITE.get().asItem(), 1)));
            return true;
        }

        if (stack.is(Items.ROTTEN_FLESH) || stack.is(Items.BONE) || stack.is(Items.SPIDER_EYE)
                || stack.is(Items.GUNPOWDER) || stack.is(Items.ENDER_PEARL) || stack.is(Items.PORKCHOP) || stack.is(Items.BEEF)) {
            ItemStack ess = new ItemStack(ModItems.AVERAGE_ESSENCE.get(), stack.getCount());
            EssenceItem.setEssenceType(ess, level.random.nextBoolean() ? EssenceType.BLOOD : EssenceType.VOID);
            itemEntity.setItem(ess);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Apex Thermodynamic Decay & Exsanguination Hex: -30% Max HP (in SpellCombatReactionEngine), blocks healing,
        // -50% toughness, leeches 1.5 hearts (3.0 HP) every 3.0s to caster, movement/attack bone spasms deal self-damage!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        long now = level.getGameTime();
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.85D, 0.50D * mult), 45L);
        EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_BLOOD_HEMORRHAGE_UNTIL, now + 45L);

        CompoundTag data = EntityHelper.getPersistentData(target);
        double lastX = data.getDouble("EntropicHexLastX").orElse(target.getX());
        double lastZ = data.getDouble("EntropicHexLastZ").orElse(target.getZ());
        data.putDouble("EntropicHexLastX", target.getX());
        data.putDouble("EntropicHexLastZ", target.getZ());
        double movedSq = (target.getX() - lastX) * (target.getX() - lastX) + (target.getZ() - lastZ) * (target.getZ() - lastZ);
        if (movedSq > 0.04D || target.swinging) {
            target.hurtServer(level, level.damageSources().fellOutOfWorld(), 1.75F * mult);
        }

        if ((now / 20L) % 3L == 0L) {
            float leech = 3.0F * mult;
            target.hurtServer(level, level.damageSources().fellOutOfWorld(), leech);
            LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCaster(level, target);
            if (hexCaster != null && hexCaster.isAlive()) {
                SpellCombatReactionEngine.notifyMagicalHeal(hexCaster, leech);
                hexCaster.heal(leech);
            }
            level.sendParticles(ParticleTypes.CRIMSON_SPORE, target.getX(), target.getY(0.8D), target.getZ(), 8, 0.3D, 0.35D, 0.3D, 0.03D);
        }
        return true;
    }
}
