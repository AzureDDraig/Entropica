package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Apotheosis Materia (Tier 6 Singularity Fusion #56)</b>
 * (Spellcrafting System Guide Section 6.58 — Empyrean + Cataclysm + Terminus + Celestial: "The Alpha Genesis Singularity").
 */
public final class ApotheosisSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int APOTHEOSIS_SUPERNOVA_GOLD_HEX_COLOR = 0xFFE484;

    @Override
    public EssenceType materiaType() {
        return EssenceType.APOTHEOSIS;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "White-Hole Star-Orbit Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Seeking Crown of Dawn";
            case BEAM -> "Sustained Holy White-Hole Ray & Genesis Cataclysm";
            case RADIAL_BURST -> "Supernova Genesis Nova";
            case TOUCH_STRIKE -> "Ascendant Solar Genesis Cleave";
            case AURA -> "Mantle of the Alpha Singularity";
            case GLYPH -> "White-Hole Genesis Sanctuary Sigil";
            case CONE -> "Prismatic Solar Fan";
            case SKYFALL -> "Cataclysmic White-Hole Genesis Starfall";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Ascendant Solar Genesis Step";
            case TRANSMUTE, BREAK -> "Alpha Genesis Transmutation & Universal Consecration";
            case AFFLICTION -> "Divine Genesis Brand Hex";
            default -> "Alpha Genesis " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations (White-Hole Star-Orbit Flow) ---

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(level,
                projectile.getOwner() instanceof LivingEntity l ? l : null,
                projectile.getBoundingBox().inflate(2.8D), null, 0L);
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.02D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileSpawnedFromForm(ServerLevel level, SpellProjectileEntity projectile, LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        projectile.setNoGravity(true);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 3, 0.08D, 0.08D, 0.08D, 0.02D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        // Seeking Crown of Dawn: bounces between up to 10 hostiles (in SpellProjectileEntity), blinding & siphoning into party absorption;
        // on terminal impact detonates into 10m White-Hole Genesis Nova!
        if (hitEntity instanceof LivingEntity le && !ApexEschatonSingularityHelper.isFriendly(le, caster)) {
            le.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
            SpellLingeringZoneManager.grantPartyAbsorption(level, caster, 3.0F, 32.0F, 20.0D);
        }
        if (projectile.getPierceCountRemaining() <= 0) {
            detonateWhiteHoleGenesisNova(level, caster, hitEntity.position(), spell);
        }
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        if (!projectile.isWallPhasing()) {
            detonateWhiteHoleGenesisNova(level, caster, blockHit.getLocation(), spell);
        }
    }

    @Override
    public void onProjectileExpiredFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        detonateWhiteHoleGenesisNova(level, caster, projectile.position(), spell);
    }

    private static void detonateWhiteHoleGenesisNova(ServerLevel level, @Nullable LivingEntity caster, Vec3 center, SpellAssembly spell) {
        // 10m White-Hole Genesis Nova: smites hostiles, shatters 8 armor, releases radiant essence orbs, fully heals allies!
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        AABB nova = new AABB(center.x - 10.0D, center.y - 3.0D, center.z - 10.0D, center.x + 10.0D, center.y + 5.5D, center.z + 10.0D);
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, nova, LivingEntity::isAlive)) {
            if (ApexEschatonSingularityHelper.isFriendly(ent, caster)) {
                float full = ent.getMaxHealth();
                SpellCombatReactionEngine.notifyMagicalHeal(ent, full);
                ent.heal(full);
                continue;
            }
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, safeguard)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, 8.0D, 0.0D, 240L);
            float smite = ApexEschatonSingularityHelper.isDarkUndeadOrAbyssal(ent) ? 2.0F : 1.0F;
            ent.hurtServer(level, level.damageSources().magic(), (10.0F + spell.getEffectiveTier() * 2.0F) * smite);
        }
        SpellLingeringZoneManager.spawnTypedEssenceOrb(level, center, EssenceType.RADIANT, 3);
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 0.8D, center.z, 48, 3.0D, 0.6D, 3.0D, 0.08D);
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
        // Sustained Holy White-Hole Ray & Genesis Cataclysm: -3 armor/s, smites undead, heals allies;
        // 5.0s continuous -> 16m KB, -8 armor, purges buffs, blooms 14m sanctuary healing allies 6 hearts (12 HP) + Saturation!
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (ApexEschatonSingularityHelper.isFriendly(hit, caster)) {
                SpellCombatReactionEngine.notifyMagicalHeal(hit, 6.0F);
                hit.heal(6.0F);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 100L);

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("ApotheosisBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("ApotheosisBeamStacks").orElse(0) + 1 : 1;
            data.putLong("ApotheosisBeamLastTick", now);
            data.putInt("ApotheosisBeamStacks", stacks);

            if (stacks >= 5) {
                data.putInt("ApotheosisBeamStacks", 0);
                ApexEschatonSingularityHelper.purgeBeneficialBuffs(hit);
                SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 8.0D, 0.0D, 240L);
                hit.setDeltaMovement(dir.x * 3.1D, 0.50D, dir.z * 3.1D); // 16m KB!
                hit.hasImpulse = true;
                float smite = ApexEschatonSingularityHelper.isDarkUndeadOrAbyssal(hit) ? 2.0F : 1.0F;
                hit.hurtServer(level, level.damageSources().magic(), (20.0F + spell.getEffectiveTier() * 3.2F) * smite);

                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, hit.getBoundingBox().inflate(14.0D), e -> ApexEschatonSingularityHelper.isFriendly(e, caster))) {
                    SpellCombatReactionEngine.notifyMagicalHeal(ally, 12.0F); // 6 hearts!
                    ally.heal(12.0F);
                    ally.addEffect(new MobEffectInstance(MobEffects.SATURATION, 100, 0));
                }
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
        // Supernova Genesis Nova: 16m shockwave (ZERO light blocks on burst!), 14m KB flat, -8 armor,
        // purges party debuffs, heals allies 12 hearts (24 HP), grants party Mantle of the Alpha Singularity 20.0s (400t)!
        double r = Math.max(16.0D, radius);
        long mantleUntil = level.getGameTime() + 400L;
        AABB box = new AABB(center.x - r, center.y - 3.0D, center.z - r, center.x + r, center.y + 6.0D, center.z + r);
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (ApexEschatonSingularityHelper.isFriendly(ent, caster)) {
                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(ent);
                SpellCombatReactionEngine.notifyMagicalHeal(ent, 24.0F); // 12 hearts!
                ent.heal(24.0F);
                EntityHelper.getPersistentData(ent).putLong(SpellLingeringZoneManager.TAG_APOTHEOSIS_SHROUD_UNTIL, mantleUntil);
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ent, level, 14.0D, 8.0D, 0.0D, 0.0D, 400L);
                SpellLingeringZoneManager.grantArcane3DFlight(ent, 400);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = ent.position().subtract(center).normalize().scale(2.75D); // 14m KB!
            ent.setDeltaMovement(away.x, 0.35D, away.z);
            ent.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, 8.0D, 0.0D, 240L);
        }
        SpellCombatReactionEngine.purifyHexesAndNegativeEffects(caster);
        SpellCombatReactionEngine.notifyMagicalHeal(caster, 24.0F);
        caster.heal(24.0F);
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_APOTHEOSIS_SHROUD_UNTIL, mantleUntil);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 14.0D, 8.0D, 0.0D, 0.0D, 400L);
        SpellLingeringZoneManager.grantArcane3DFlight(caster, 400);
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 1.0D, center.z, 52, 4.0D, 0.6D, 4.0D, 0.08D);
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
        // Ascendant Solar Genesis Cleave: 75% bypass, -8 armor, disables shields 8.0s (160t),
        // roots 4.0s (80t), releases seeking golden crowns to hostiles within 10m -> party absorption!
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, safeguard)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 8.0D, 0.0D, 240L);
            ApexEschatonSingularityHelper.disableShield(hit, 160);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 6));
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 80, 0));

            float siphoned = 6.0F;
            for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, hit.getBoundingBox().inflate(10.0D),
                    e -> e.isAlive() && e != hit && !ApexEschatonSingularityHelper.isFriendly(e, caster))) {
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(adj, caster, safeguard)) continue;
                adj.hurtServer(level, level.damageSources().magic(), 6.0F + spell.getEffectiveTier() * 1.4F);
                siphoned += 3.0F;
            }
            SpellLingeringZoneManager.grantPartyAbsorption(level, caster, siphoned, 32.0F, 20.0D);
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
        // Mantle of the Alpha Singularity (emits Level-15 ambient light via SpellLingeringZoneManager!)
        int dur = 360 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_APOTHEOSIS_SHROUD_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 14.0D, 8.0D, 0.0D, 0.0D, dur);
        SpellLingeringZoneManager.grantArcane3DFlight(caster, dur);
        level.sendParticles(ParticleTypes.END_ROD, caster.getX(), caster.getY(1.0D), caster.getZ(), 36, 0.6D, 0.7D, 0.6D, 0.06D);
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
        // White-Hole Genesis Sanctuary Sigil: 6m trigger ->
        // Ally step: full health, cures curses & hexes, +12 Absorption hearts (24 HP), 60.0s (1200t) Regeneration IV;
        // Hostile step: 12m cage for 6.0s (120t), -8 armor, strips buffs, smites dark entities, leeches 50% HP into radiant essence orbs!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 6.0D, 480, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 0.15D, zone.center.z, 5, 1.2D, 0.05D, 1.2D, 0.01D);
                    }
                    if (elapsed < 12) return true;

                    AABB trig = new AABB(zone.center.x - 6.0D, zone.center.y - 1.5D, zone.center.z - 6.0D,
                            zone.center.x + 6.0D, zone.center.y + 3.0D, zone.center.z + 6.0D);
                    List<LivingEntity> steppers = srv.getEntitiesOfClass(LivingEntity.class, trig, LivingEntity::isAlive);
                    if (steppers.isEmpty()) return true;

                    for (LivingEntity st : steppers) {
                        if (owner != null && ApexEschatonSingularityHelper.isFriendly(st, owner)) {
                            SpellCombatReactionEngine.purifyHexesAndNegativeEffects(st);
                            for (EquipmentSlot eq : EquipmentSlot.values()) {
                                PenumbraSpellMutationHandler.purgeCursesFromStack(st.getItemBySlot(eq));
                            }
                            float full = st.getMaxHealth();
                            SpellCombatReactionEngine.notifyMagicalHeal(st, full);
                            st.heal(full);
                            st.setAbsorptionAmount(Math.max(st.getAbsorptionAmount(), 24.0F));
                            st.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1200, 3)); // Regen IV 60s!
                            return false;
                        }
                    }

                    AABB cage = new AABB(zone.center.x - 12.0D, zone.center.y - 2.5D, zone.center.z - 12.0D,
                            zone.center.x + 12.0D, zone.center.y + 5.5D, zone.center.z + 12.0D);
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, cage, e -> e.isAlive() && e != owner)) {
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                        ApexEschatonSingularityHelper.purgeBeneficialBuffs(foe);
                        SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 8.0D, 0.0D, 240L);
                        foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 6));
                        foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 120, 0));
                        float leech = Math.min(32.0F, foe.getHealth() * 0.50F);
                        foe.hurtServer(srv, srv.damageSources().magic(), leech);
                        SpellLingeringZoneManager.spawnTypedEssenceOrb(srv, foe.position(), EssenceType.RADIANT, 2);
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
        // Prismatic Solar Fan: -8 armor, blinds, ignites undead, vitrifies sand -> Glass, accelerates crops,
        // turns netherrack -> Moss, purges wither roses, Divine Reverence (35.0s / 700t AI disruption + 75% slow)!
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        BlockPos mid = BlockPos.containing(caster.getEyePosition().add(dir.scale(Math.min(8.0D, coneRange * 0.5D))));
        for (BlockPos p : BlockPos.betweenClosed(mid.offset(-4, -2, -4), mid.offset(4, 2, 4))) {
            BlockState bs = level.getBlockState(p);
            if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
            if (bs.is(Blocks.SAND) || bs.is(Blocks.RED_SAND)) {
                level.setBlockAndUpdate(p, Blocks.GLASS.defaultBlockState());
            } else if (bs.is(Blocks.NETHERRACK)) {
                level.setBlockAndUpdate(p, Blocks.MOSS_BLOCK.defaultBlockState());
            } else if (bs.is(Blocks.WITHER_ROSE)) {
                level.setBlockAndUpdate(p, Blocks.AZURE_BLUET.defaultBlockState());
            } else if (bs.getBlock() instanceof CropBlock crop && !crop.isMaxAge(bs)) {
                crop.growCrops(level, p, bs);
            }
        }

        for (LivingEntity hit : hitTargets) {
            if (ApexEschatonSingularityHelper.isFriendly(hit, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 8.0D, 0.0D, 240L);
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 240, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 700, 4));
            if (ApexEschatonSingularityHelper.isDarkUndeadOrAbyssal(hit)) {
                hit.igniteForSeconds(8);
            }
            hit.setDeltaMovement(dir.x * 1.85D, 0.4D, dir.z * 1.85D);
            hit.hasImpulse = true;
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 700); // 35.0s!
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
        // Cataclysmic White-Hole Genesis Starfall: 20m zone, 30.0s (600t) + 15.0s (300t) Consecrated Genesis Ground (900t total)!
        double r = Math.max(20.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 900, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    boolean starfall = elapsed <= 600;
                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + (starfall ? 5.5D : 0.4D), zone.center.z,
                                32, zone.radius * 0.55D, starfall ? 2.6D : 0.3D, zone.radius * 0.55D, 0.06D);
                    }
                    if (!starfall && elapsed % 100 == 0) {
                        SpellLingeringZoneManager.spawnTypedEssenceOrb(srv, zone.center, EssenceType.RADIANT, 2);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.5D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 10.0D, zone.center.z + zone.radius);
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (owner != null && ApexEschatonSingularityHelper.isFriendly(ent, owner)) {
                                SpellCombatReactionEngine.notifyMagicalHeal(ent, 6.0F); // +3 hearts/s!
                                ent.heal(6.0F);
                                ent.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 35, 3, true, false)); // Regen IV
                                ent.addEffect(new MobEffectInstance(MobEffects.SATURATION, 25, 0, true, false));
                                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ent, srv, 8.0D, 0.0D, 0.0D, 0.0D, 35L);
                                continue;
                            }
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            if (starfall) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(ent, srv, 8.0D, 0.0D, 220L);
                                ent.setDeltaMovement(ent.getDeltaMovement().x * 0.2D, -0.5D, ent.getDeltaMovement().z * 0.2D);
                                ent.hasImpulse = true;
                                SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, ent, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.60F);
                            } else {
                                ent.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 5)); // 80% slow
                                if (elapsed % 60 == 0) {
                                    ent.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 40, 0));
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
        tag.putString("SpellEidolonMateria", EssenceType.APOTHEOSIS.name());
        tag.putInt("SpellEidolonTintRGB", APOTHEOSIS_SUPERNOVA_GOLD_HEX_COLOR);
        level.sendParticles(ParticleTypes.END_ROD, spawnPos.x, spawnPos.y + 1.1D, spawnPos.z, 36, 0.6D, 0.7D, 0.6D, 0.06D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Ascendant Solar Genesis Step: 24m through walls/ceilings; departure 8m flashbang blinding 6.0s (120t) & rooting 3.0s (60t);
        // arrival grants 12.0s (240t) 3D flight, Slow Falling, fall & void damage immunity, healing dewdrops, sheds 100% aggro!
        AABB dep = new AABB(departurePos.x - 8.0D, departurePos.y - 2.0D, departurePos.z - 8.0D,
                departurePos.x + 8.0D, departurePos.y + 4.0D, departurePos.z + 8.0D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, dep, e -> e.isAlive() && e != caster)) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0));
            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 6));
            foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0));
        }

        SpellLingeringZoneManager.grantArcane3DFlight(caster, 240);
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 260, 0));
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_VOID_IMMUNE_UNTIL, level.getGameTime() + 240L);
        SpellLingeringZoneManager.spawnCollectableHealingOrb(level, arrivalPos, caster, 8.0F, false);
        ApexEschatonSingularityHelper.shedMobAggro(level, caster, arrivalPos, 28.0D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.4F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Alpha Genesis Transmutation & Universal Consecration [5.0s Channel, 16m, strictly NO air transmutation]:
        // Netherrack/Soul Sand/Deepslate -> Calcite/Flowering Azalea Moss/Lush Loam/Amethyst Blocks (+ Radiant Raw Essence from cursed blocks);
        // cleanses curses + elevates up to 2 enchants (+2); cures Zombie Villagers w/ max discounts;
        // 4m standing water + bottle -> Nectar of the Alpha Genesis!
        if (state.isAir()) return false;

        if (ApexEschatonSingularityHelper.beginOrExecuteChanneledTransmute(
                level, caster, pos, 16, potencyTier, EssenceType.APOTHEOSIS,
                "Alpha Genesis Transmutation & Universal Consecration",
                () -> {
                    ApexEschatonSingularityHelper.cureZombieVillagersWithMaxDiscounts(level, caster, pos, 16.0D);
                    if (caster != null) {
                        ApexEschatonSingularityHelper.purgeCursesAndElevateEquipment(caster, 2, 1.0F, 2);
                    }
                    ApexEschatonSingularityHelper.tryBottleStandingWaterElixir(level, caster, pos, 4, EssenceType.APOTHEOSIS);
                })) {
            return true;
        }

        if (state.is(Blocks.NETHERRACK) || state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL)
                || state.is(Blocks.SCULK) || state.is(Blocks.DEEPSLATE) || state.is(Blocks.COBBLED_DEEPSLATE)) {
            boolean wasCursed = state.is(Blocks.SOUL_SAND) || state.is(Blocks.SCULK) || state.is(Blocks.WITHER_ROSE);
            int pick = level.random.nextInt(4);
            BlockState out = switch (pick) {
                case 0 -> Blocks.CALCITE.defaultBlockState();
                case 1 -> Blocks.MOSS_BLOCK.defaultBlockState();
                case 2 -> Blocks.PODZOL.defaultBlockState();
                default -> Blocks.AMETHYST_BLOCK.defaultBlockState();
            };
            level.setBlockAndUpdate(pos, out);
            if (pick == 1 && level.getBlockState(pos.above()).isAir() && level.random.nextBoolean()) {
                level.setBlockAndUpdate(pos.above(), Blocks.FLOWERING_AZALEA.defaultBlockState());
            }
            if (wasCursed && level.random.nextFloat() < 0.25F) {
                SpellLingeringZoneManager.spawnTypedEssenceOrb(level, Vec3.atCenterOf(pos), EssenceType.RADIANT, 2);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        if (ApexEschatonSingularityHelper.purgeCursesAndElevateStack(stack, level.random, 2, 1.0F, 2)) {
            itemEntity.setItem(stack.copy());
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Divine Genesis Brand Hex: true holy burn (2x vs Undead/Demonic/Abyssal), disables all healing,
        // 60% fumble miss, -60% toughness, dark magic backfire (in SpellExecutionEngine)!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.90D, 0.60D * mult), 45L);
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 35, 0, true, true));
        float dmg = (3.5F + potencyTier * 0.85F) * mult;
        if (ApexEschatonSingularityHelper.isDarkUndeadOrAbyssal(target)) {
            dmg *= 2.0F;
        }
        target.hurtServer(level, level.damageSources().magic(), dmg);
        level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.9D), target.getZ(), 8, 0.3D, 0.4D, 0.3D, 0.04D);
        return true;
    }
}
