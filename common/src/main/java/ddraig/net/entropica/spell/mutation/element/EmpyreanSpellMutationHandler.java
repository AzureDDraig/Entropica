package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.registry.ModBlocks;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Empyrean Materia (Tier 5 Eschaton Fusion #52)</b>
 * (Spellcrafting System Guide Section 6.54 — Radiant + Vitae + Nature: "The Infinite Life").
 */
public final class EmpyreanSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int EMPYREAN_SOLAR_GOLD_HEX_COLOR = 0xFFEA78;

    @Override
    public EssenceType materiaType() {
        return EssenceType.EMPYREAN;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "World-Tree Siphoning Root-Web Orbit Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "World-Tree Siphoning Seed-Pod";
            case BEAM -> "Sustained Root-Lance Sunbeam & Canopy Cataclysm";
            case RADIAL_BURST -> "World-Tree Heartwood Bastion Nova";
            case TOUCH_STRIKE -> "Siphoning World-Tree Root-Cleave";
            case AURA -> "Living Titan Bark & Canopy Mantle";
            case GLYPH -> "World-Tree Siphoning Root-Vault Sigil";
            case CONE -> "Solar-Verdant Spore-Web Deluge";
            case SKYFALL -> "Cataclysmic World-Tree Canopy Skyfall";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Solar Slipstream Ascent";
            case TRANSMUTE, BREAK -> "World-Tree Genesis & Vitality Consecration";
            case AFFLICTION -> "Radiant Empyrean Brand Hex";
            default -> "World-Tree Empyrean " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations (World-Tree Siphoning Root-Web Orbit Flow) ---

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(level,
                projectile.getOwner() instanceof LivingEntity l ? l : null,
                projectile.getBoundingBox().inflate(2.4D), null, 0L);
        level.sendParticles(ParticleTypes.CHERRY_LEAVES, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.01D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileSpawnedFromForm(ServerLevel level, SpellProjectileEntity projectile, LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        projectile.setNoGravity(true);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.01D);
        level.sendParticles(ParticleTypes.CHERRY_LEAVES, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.08D, 0.08D, 0.08D, 0.01D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        if (projectile.getPierceCountRemaining() <= 0) {
            detonateSolarRootWeb(level, caster, hitEntity.position(), spell);
        }
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        if (!projectile.isWallPhasing()) {
            detonateSolarRootWeb(level, caster, blockHit.getLocation(), spell);
        }
    }

    @Override
    public void onProjectileExpiredFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        detonateSolarRootWeb(level, caster, projectile.position(), spell);
    }

    private static void detonateSolarRootWeb(ServerLevel level, @Nullable LivingEntity caster, Vec3 center, SpellAssembly spell) {
        // 8m Siphoning Solar-Root Web: roots 3.5s (70t), -6 armor, leeches 35% into party absorption!
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        float totalDmg = 0.0F;
        AABB web = new AABB(center.x - 8.0D, center.y - 2.5D, center.z - 8.0D, center.x + 8.0D, center.y + 4.5D, center.z + 8.0D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, web, LivingEntity::isAlive)) {
            if (ApexEschatonSingularityHelper.isFriendly(foe, caster)) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, safeguard)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 6.0D, 0.0D, 180L);
            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 70, 6));
            foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 70, 0));
            float dmg = 7.0F + spell.getEffectiveTier() * 1.6F;
            foe.hurtServer(level, level.damageSources().magic(), dmg);
            totalDmg += dmg;
        }
        if (caster != null && totalDmg > 0.0F) {
            SpellLingeringZoneManager.grantPartyAbsorption(level, caster, totalDmg * 0.35F, 28.0F, 16.0D);
        }
        level.sendParticles(ParticleTypes.CHERRY_LEAVES, center.x, center.y + 0.6D, center.z, 32, 2.2D, 0.5D, 2.2D, 0.04D);
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
        // Sustained Root-Lance Sunbeam & Canopy Cataclysm: -2 armor/s, siphons health & saturation into party, grounds flyers;
        // 5.0s continuous -> 12m KB, -7 armor, purges buffs, 5.0s (100t) root, 10m golden azalea sanctuary healing allies 4 hearts!
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (ApexEschatonSingularityHelper.isFriendly(hit, caster)) {
                SpellCombatReactionEngine.notifyMagicalHeal(hit, 4.0F);
                hit.heal(4.0F);
                if (hit instanceof Player p) p.getFoodData().eat(1, 0.4F);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 2.0D, 0.0D, 80L);
            if (!hit.onGround()) {
                hit.setDeltaMovement(hit.getDeltaMovement().x, -0.8D, hit.getDeltaMovement().z);
                hit.hasImpulse = true;
            }
            SpellLingeringZoneManager.grantPartyAbsorption(level, caster, 2.0F, 24.0F, 12.0D);

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("EmpyreanBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("EmpyreanBeamStacks").orElse(0) + 1 : 1;
            data.putLong("EmpyreanBeamLastTick", now);
            data.putInt("EmpyreanBeamStacks", stacks);

            if (stacks >= 5) {
                data.putInt("EmpyreanBeamStacks", 0);
                ApexEschatonSingularityHelper.purgeBeneficialBuffs(hit);
                SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 7.0D, 0.0D, 220L);
                hit.setDeltaMovement(dir.x * 2.4D, 0.45D, dir.z * 2.4D); // 12m KB!
                hit.hasImpulse = true;
                hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 6));
                hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 100, 0));
                hit.hurtServer(level, level.damageSources().magic(), 16.0F + spell.getEffectiveTier() * 2.8F);

                BlockPos c = hit.blockPosition();
                for (BlockPos p : BlockPos.betweenClosed(c.offset(-3, -1, -3), c.offset(3, 1, 3))) {
                    if (level.getBlockState(p).isAir() && Blocks.FLOWERING_AZALEA.defaultBlockState().canSurvive(level, p) && level.random.nextFloat() < 0.25F) {
                        level.setBlockAndUpdate(p, Blocks.FLOWERING_AZALEA.defaultBlockState());
                    }
                }
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, hit.getBoundingBox().inflate(10.0D), e -> ApexEschatonSingularityHelper.isFriendly(e, caster))) {
                    SpellCombatReactionEngine.notifyMagicalHeal(ally, 8.0F); // 4 hearts!
                    ally.heal(8.0F);
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
        // World-Tree Heartwood Bastion Nova: 12m shockwave (ZERO light blocks!), 10m KB flat, -6 armor,
        // roots 4.0s (80t), grants party Aegis of the Infinite Life for 16.0s (320t)!
        double r = Math.max(12.0D, radius);
        long aegisUntil = level.getGameTime() + 320L;
        AABB box = new AABB(center.x - r, center.y - 2.5D, center.z - r, center.x + r, center.y + 5.0D, center.z + r);
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (ApexEschatonSingularityHelper.isFriendly(ent, caster)) {
                EntityHelper.getPersistentData(ent).putLong(SpellLingeringZoneManager.TAG_EMPYREAN_AEGIS_UNTIL, aegisUntil);
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ent, level, 12.0D, 6.0D, 0.0D, 0.0D, 320L);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = ent.position().subtract(center).normalize().scale(2.05D); // 10m KB!
            ent.setDeltaMovement(away.x, 0.35D, away.z);
            ent.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, 6.0D, 0.0D, 180L);
            ent.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 6));
            ent.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 80, 0));
        }
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_EMPYREAN_AEGIS_UNTIL, aegisUntil);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 12.0D, 6.0D, 0.0D, 0.0D, 320L);
        SpellLingeringZoneManager.grantPartyAbsorption(level, caster, 10.0F, 28.0F, 16.0D);
        level.sendParticles(ParticleTypes.CHERRY_LEAVES, center.x, center.y + 1.0D, center.z, 40, 3.0D, 0.6D, 3.0D, 0.05D);
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
        // Siphoning World-Tree Root-Cleave: 60% bypass, -6 armor, disables shields 6.0s (120t), roots 4.0s (80t),
        // seeking golden seed tendrils wrap adjacent foes within 8m -> party absorption!
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, safeguard)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 6.0D, 0.0D, 180L);
            ApexEschatonSingularityHelper.disableShield(hit, 120);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 6));
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 80, 0));

            float siphoned = 4.0F;
            for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, hit.getBoundingBox().inflate(8.0D),
                    e -> e.isAlive() && e != hit && !ApexEschatonSingularityHelper.isFriendly(e, caster))) {
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(adj, caster, safeguard)) continue;
                adj.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 4));
                adj.hurtServer(level, level.damageSources().magic(), 5.0F + spell.getEffectiveTier());
                siphoned += 2.5F;
            }
            SpellLingeringZoneManager.grantPartyAbsorption(level, caster, siphoned, 28.0F, 16.0D);
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
        int dur = 280 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_EMPYREAN_SHROUD_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 12.0D, 6.0D, 0.0D, 0.0D, dur);
        level.sendParticles(ParticleTypes.CHERRY_LEAVES, caster.getX(), caster.getY(1.0D), caster.getZ(), 32, 0.55D, 0.65D, 0.55D, 0.04D);
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
        // World-Tree Siphoning Root-Vault Sigil: 5m trigger ->
        // Ally step: 10 full hearts (20 HP), cures curses & hexes, +8 Absorption hearts (16 HP) for 45.0s;
        // Hostile step: 8m cage of golden titan roots for 5.0s (100t), -6 armor, strips buffs, leeches 40% HP into golden dewdrops!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 5.0D, 400, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.CHERRY_LEAVES, zone.center.x, zone.center.y + 0.15D, zone.center.z, 5, 1.1D, 0.05D, 1.1D, 0.01D);
                    }
                    if (elapsed < 12) return true;

                    AABB trig = new AABB(zone.center.x - 5.0D, zone.center.y - 1.5D, zone.center.z - 5.0D,
                            zone.center.x + 5.0D, zone.center.y + 2.5D, zone.center.z + 5.0D);
                    List<LivingEntity> steppers = srv.getEntitiesOfClass(LivingEntity.class, trig, LivingEntity::isAlive);
                    if (steppers.isEmpty()) return true;

                    for (LivingEntity st : steppers) {
                        if (owner != null && ApexEschatonSingularityHelper.isFriendly(st, owner)) {
                            SpellCombatReactionEngine.purifyHexesAndNegativeEffects(st);
                            for (EquipmentSlot eq : EquipmentSlot.values()) {
                                PenumbraSpellMutationHandler.purgeCursesFromStack(st.getItemBySlot(eq));
                            }
                            SpellCombatReactionEngine.notifyMagicalHeal(st, 20.0F);
                            st.heal(20.0F);
                            st.setAbsorptionAmount(Math.max(st.getAbsorptionAmount(), 16.0F));
                            return false;
                        }
                    }

                    AABB cage = new AABB(zone.center.x - 8.0D, zone.center.y - 2.0D, zone.center.z - 8.0D,
                            zone.center.x + 8.0D, zone.center.y + 4.5D, zone.center.z + 8.0D);
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, cage, e -> e.isAlive() && e != owner)) {
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                        ApexEschatonSingularityHelper.purgeBeneficialBuffs(foe);
                        SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 6.0D, 0.0D, 200L);
                        foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 6));
                        foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 100, 0));
                        float leech = Math.min(24.0F, foe.getHealth() * 0.40F);
                        foe.hurtServer(srv, srv.damageSources().magic(), leech);
                        SpellLingeringZoneManager.spawnCollectableHealingOrb(srv, foe.position(), owner, Math.max(4.0F, leech * 0.5F), true);
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
        // Solar-Verdant Spore-Web Deluge: -6 armor, vaporizes projectiles, roots 3.0s (60t),
        // Empyrean Mesmerization (30.0s / 600t AI disruption + 60% Slowness IV)!
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(level, caster,
                new AABB(caster.getEyePosition(), caster.getEyePosition().add(dir.scale(coneRange))).inflate(3.5D), null, 0L);

        for (LivingEntity hit : hitTargets) {
            if (ApexEschatonSingularityHelper.isFriendly(hit, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 6.0D, 0.0D, 200L);
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 600, 3)); // 60% slow for 30.0s!
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 600); // 30.0s!
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
        // Cataclysmic World-Tree Canopy Skyfall: 16m zone, 24.0s (480t) + 12.0s (240t) Consecrated Soil (720t total):
        // knocks flat, -7 armor; lingering 12s slows 70%, roots every 4s, heals allies +2 hearts/s + Saturation + +6 Armor!
        double r = Math.max(16.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 720, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    boolean storm = elapsed <= 480;
                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.CHERRY_LEAVES, zone.center.x, zone.center.y + (storm ? 5.0D : 0.4D), zone.center.z,
                                28, zone.radius * 0.55D, storm ? 2.4D : 0.3D, zone.radius * 0.55D, 0.05D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 8.5D, zone.center.z + zone.radius);
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (owner != null && ApexEschatonSingularityHelper.isFriendly(ent, owner)) {
                                SpellCombatReactionEngine.notifyMagicalHeal(ent, 4.0F);
                                ent.heal(4.0F);
                                ent.addEffect(new MobEffectInstance(MobEffects.SATURATION, 25, 0, true, false));
                                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ent, srv, 6.0D, 0.0D, 0.0D, 0.0D, 35L);
                                continue;
                            }
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            if (storm) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(ent, srv, 7.0D, 0.0D, 200L);
                                ent.setDeltaMovement(ent.getDeltaMovement().x * 0.25D, -0.45D, ent.getDeltaMovement().z * 0.25D);
                                ent.hasImpulse = true;
                                SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, ent, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.55F);
                            } else {
                                ent.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 4));
                                if (elapsed % 80 == 0) {
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
        tag.putString("SpellEidolonMateria", EssenceType.EMPYREAN.name());
        tag.putInt("SpellEidolonTintRGB", EMPYREAN_SOLAR_GOLD_HEX_COLOR);
        level.sendParticles(ParticleTypes.CHERRY_LEAVES, spawnPos.x, spawnPos.y + 1.1D, spawnPos.z, 32, 0.55D, 0.65D, 0.55D, 0.05D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Solar Slipstream Ascent: 22m through ceilings; departure 6m golden roots blast + 3.0s (60t) root;
        // arrival 10.0s (200t) 3D flight + Slow Falling + fall immunity + healing dewdrops + sheds 100% aggro!
        AABB dep = new AABB(departurePos.x - 6.0D, departurePos.y - 2.0D, departurePos.z - 6.0D,
                departurePos.x + 6.0D, departurePos.y + 3.5D, departurePos.z + 6.0D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, dep, e -> e.isAlive() && e != caster)) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
            Vec3 away = foe.position().subtract(departurePos).normalize().scale(1.4D);
            foe.setDeltaMovement(away.x, 0.35D, away.z);
            foe.hasImpulse = true;
            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 6));
            foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0));
        }

        SpellLingeringZoneManager.grantArcane3DFlight(caster, 200);
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 220, 0));
        SpellLingeringZoneManager.spawnCollectableHealingOrb(level, arrivalPos, caster, 6.0F, true);
        ApexEschatonSingularityHelper.shedMobAggro(level, caster, arrivalPos, 26.0D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.85F, 1.35F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // World-Tree Genesis & Vitality Consecration [5.0s Channel, strictly NO air transmutation]:
        // Stone/Deepslate/Netherrack -> Sacred World-Tree Heartwood (Materia Echo Wood), Flowering Azalea Moss, Sacred Loam (Podzol);
        // purges curses + 50% +1 enchant; cures Zombie Villagers w/ max discounts; 3m standing water + bottle -> Dew of the World-Tree!
        if (state.isAir()) return false;

        if (ApexEschatonSingularityHelper.beginOrExecuteChanneledTransmute(
                level, caster, pos, 12, potencyTier, EssenceType.EMPYREAN,
                "World-Tree Genesis & Vitality Consecration",
                () -> {
                    ApexEschatonSingularityHelper.cureZombieVillagersWithMaxDiscounts(level, caster, pos, 12.0D);
                    if (caster != null) {
                        ApexEschatonSingularityHelper.purgeCursesAndElevateEquipment(caster, 1, 0.50F, 1);
                    }
                    ApexEschatonSingularityHelper.tryBottleStandingWaterElixir(level, caster, pos, 3, EssenceType.EMPYREAN);
                })) {
            return true;
        }

        if (state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE) || state.is(Blocks.COBBLED_DEEPSLATE) || state.is(Blocks.NETHERRACK)) {
            int pick = level.random.nextInt(3);
            BlockState out = switch (pick) {
                case 0 -> ModBlocks.MATERIA_ECHO_WOOD.get().defaultBlockState();
                case 1 -> Blocks.MOSS_BLOCK.defaultBlockState();
                default -> Blocks.PODZOL.defaultBlockState();
            };
            level.setBlockAndUpdate(pos, out);
            if (pick == 1 && level.getBlockState(pos.above()).isAir() && level.random.nextBoolean()) {
                level.setBlockAndUpdate(pos.above(), Blocks.FLOWERING_AZALEA.defaultBlockState());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        if (ApexEschatonSingularityHelper.purgeCursesAndElevateStack(stack, level.random, 1, 0.50F, 1)) {
            itemEntity.setItem(stack.copy());
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Radiant Empyrean Brand Hex: true holy burn (2x vs Undead/Demonic/Abyssal), disables healing,
        // 50% fumble miss, -50% armor toughness!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.85D, 0.50D * mult), 45L);
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 35, 0, true, true));
        float dmg = (2.5F + potencyTier * 0.65F) * mult;
        if (ApexEschatonSingularityHelper.isDarkUndeadOrAbyssal(target)) {
            dmg *= 2.0F;
        }
        target.hurtServer(level, level.damageSources().magic(), dmg);
        level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.9D), target.getZ(), 7, 0.3D, 0.35D, 0.3D, 0.03D);
        return true;
    }
}
