package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.SpellEidolonEntity;
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
import ddraig.net.entropica.spell.fuel.SpellFuelService;
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
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Entropica Materia (Tier 6 Singularity Fusion #57)</b>
 * (Spellcrafting System Guide Section 6.59 — Apotheosis + Ascended Entropic: "The Grand Cosmic Equilibrium Singularity").
 */
public final class EntropicaSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int ENTROPICA_CHRONO_SILVER_HEX_COLOR = 0xE0E5EC;

    @Override
    public EssenceType materiaType() {
        return EssenceType.ENTROPICA;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Cosmic Parallax Singularity Orbit Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Singularity Equilibrium Lance";
            case BEAM -> "Twin Helix Singularity Stream & Annihilation Blast";
            case RADIAL_BURST -> "Cosmic Equilibrium Nova";
            case TOUCH_STRIKE -> "Blade of the Grand Paradox";
            case AURA -> "Mantle of Entropica";
            case GLYPH -> "Singularity Horizon Mandala";
            case CONE -> "Thermodynamic Heat-Death Wave";
            case SKYFALL -> "Cataclysmic Singularity Equilibrium Starfall";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Parallax Event-Horizon Fold";
            case TRANSMUTE, BREAK -> "Grand Cosmic Equilibrium Transmutation";
            case AFFLICTION -> "Grand Equilibrium Paradox Hex";
            default -> "Grand Cosmic Equilibrium " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations (Cosmic Parallax Singularity Orbit Flow) ---

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(level,
                projectile.getOwner() instanceof LivingEntity l ? l : null,
                projectile.getBoundingBox().inflate(3.0D), EssenceType.ENTROPICA, 64L);
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.08D, 0.08D, 0.08D, 0.02D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.08D, 0.08D, 0.08D, 0.02D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileSpawnedFromForm(ServerLevel level, SpellProjectileEntity projectile, LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        projectile.setNoGravity(true);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        // Pulls enemies inward along its trajectory!
        LivingEntity owner = projectile.getOwner() instanceof LivingEntity l ? l : null;
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, projectile.getBoundingBox().inflate(3.8D), LivingEntity::isAlive)) {
            if (owner != null && (ApexEschatonSingularityHelper.isFriendly(foe, owner)
                    || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, true))) {
                continue;
            }
            Vec3 pull = projectile.position().subtract(foe.position()).scale(0.30D);
            foe.setDeltaMovement(foe.getDeltaMovement().add(pull.x, 0.04D, pull.z));
            foe.hasImpulse = true;
        }
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.02D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.02D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        if (projectile.getPierceCountRemaining() <= 0) {
            collapseEquilibriumNova(level, caster, hitEntity.position(), spell);
        }
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        if (!projectile.isWallPhasing()) {
            collapseEquilibriumNova(level, caster, blockHit.getLocation(), spell);
        }
    }

    @Override
    public void onProjectileExpiredFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        collapseEquilibriumNova(level, caster, projectile.position(), spell);
    }

    private static void collapseEquilibriumNova(ServerLevel level, @Nullable LivingEntity caster, Vec3 center, SpellAssembly spell) {
        // Balanced creation/dissolution nova: shatters 8 armor, knocks mobs flat, yields collectible Essence Orbs!
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        AABB nova = new AABB(center.x - 10.0D, center.y - 3.0D, center.z - 10.0D, center.x + 10.0D, center.y + 5.5D, center.z + 10.0D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, nova, LivingEntity::isAlive)) {
            if (ApexEschatonSingularityHelper.isFriendly(foe, caster)) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, safeguard)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 8.0D, 0.0D, 240L);
            foe.setDeltaMovement(foe.getDeltaMovement().x * 0.2D, -0.5D, foe.getDeltaMovement().z * 0.2D);
            foe.hasImpulse = true;
            foe.hurtServer(level, level.damageSources().fellOutOfWorld(), 12.0F + spell.getEffectiveTier() * 2.2F);
        }
        SpellLingeringZoneManager.spawnTypedEssenceOrb(level, center, EssenceType.ENTROPICA, 3);
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 0.8D, center.z, 32, 2.8D, 0.6D, 2.8D, 0.08D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 0.8D, center.z, 32, 2.8D, 0.6D, 2.8D, 0.08D);
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
        // Twin Helix Singularity Stream & Annihilation Blast: -3 armor/s, pulls mobs to beam center;
        // 5.0s continuous -> omni-directional Singularity Annihilation Blast (-8 armor, 16m KB, purges all buffs)!
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 100L);

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("EntropicaBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("EntropicaBeamStacks").orElse(0) + 1 : 1;
            data.putLong("EntropicaBeamLastTick", now);
            data.putInt("EntropicaBeamStacks", stacks);

            if (stacks >= 5) {
                data.putInt("EntropicaBeamStacks", 0);
                ApexEschatonSingularityHelper.purgeBeneficialBuffs(hit);
                SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 8.0D, 0.0D, 240L);
                hit.setDeltaMovement(dir.x * 3.1D, 0.55D, dir.z * 3.1D); // 16m KB!
                hit.hasImpulse = true;
                hit.hurtServer(level, level.damageSources().fellOutOfWorld(), 22.0F + spell.getEffectiveTier() * 3.5F);
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
        // Cosmic Equilibrium Nova: 16m shockwave (strictly ZERO light blocks!), 16m KB flat, -8 armor,
        // purges party debuffs, heals allies 12 hearts (24 HP), grants party Mantle of Entropica 20.0s (400t)!
        double r = Math.max(16.0D, radius);
        long mantleUntil = level.getGameTime() + 400L;
        AABB box = new AABB(center.x - r, center.y - 3.0D, center.z - r, center.x + r, center.y + 6.0D, center.z + r);
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (ApexEschatonSingularityHelper.isFriendly(ent, caster)) {
                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(ent);
                SpellCombatReactionEngine.notifyMagicalHeal(ent, 24.0F); // 12 hearts!
                ent.heal(24.0F);
                EntityHelper.getPersistentData(ent).putLong(SpellLingeringZoneManager.TAG_ENTROPICA_SHROUD_UNTIL, mantleUntil);
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ent, level, 14.0D, 8.0D, 0.0D, 0.0D, 400L);
                SpellLingeringZoneManager.grantArcane3DFlight(ent, 400);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = ent.position().subtract(center).normalize().scale(3.1D); // 16m KB!
            ent.setDeltaMovement(away.x, 0.35D, away.z);
            ent.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, 8.0D, 0.0D, 240L);
        }
        SpellCombatReactionEngine.purifyHexesAndNegativeEffects(caster);
        SpellCombatReactionEngine.notifyMagicalHeal(caster, 24.0F);
        caster.heal(24.0F);
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_ENTROPICA_SHROUD_UNTIL, mantleUntil);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 14.0D, 8.0D, 0.0D, 0.0D, 400L);
        SpellLingeringZoneManager.grantArcane3DFlight(caster, 400);
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 1.0D, center.z, 36, 4.0D, 0.6D, 4.0D, 0.08D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 1.0D, center.z, 36, 4.0D, 0.6D, 4.0D, 0.08D);
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
        // Blade of the Grand Paradox: 75% bypass, -8 armor, disables shields 8.0s (160t),
        // drains 50% of damage dealt into party absorption shields, mini-vortex draws adjacent hostiles into slash arc!
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        Vec3 arcOrigin = hitEntities.isEmpty() ? caster.position().add(caster.getLookAngle().scale(2.5D)) : hitEntities.getFirst().position();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, safeguard)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 8.0D, 0.0D, 240L);
            ApexEschatonSingularityHelper.disableShield(hit, 160);
            SpellLingeringZoneManager.grantPartyAbsorption(level, caster, 8.0F + spell.getEffectiveTier(), 36.0F, 20.0D);
        }

        AABB vortex = new AABB(arcOrigin.x - 8.0D, arcOrigin.y - 2.0D, arcOrigin.z - 8.0D,
                arcOrigin.x + 8.0D, arcOrigin.y + 4.0D, arcOrigin.z + 8.0D);
        for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, vortex, e -> e.isAlive() && !ApexEschatonSingularityHelper.isFriendly(e, caster))) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(adj, caster, safeguard)) continue;
            Vec3 in = arcOrigin.subtract(adj.position()).scale(0.45D);
            adj.setDeltaMovement(in.x, 0.18D, in.z);
            adj.hasImpulse = true;
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
        // Mantle of Entropica (strictly NO dynamic light emission!)
        int dur = 360 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_ENTROPICA_SHROUD_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 14.0D, 8.0D, 0.0D, 0.0D, dur);
        SpellLingeringZoneManager.grantArcane3DFlight(caster, dur);
        level.sendParticles(ParticleTypes.END_ROD, caster.getX(), caster.getY(1.0D), caster.getZ(), 24, 0.6D, 0.7D, 0.6D, 0.05D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, caster.getX(), caster.getY(1.0D), caster.getZ(), 24, 0.6D, 0.7D, 0.6D, 0.05D);
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
        // Singularity Horizon Mandala:
        // Ally step -> full health & hunger, cures curses & hexes, +12 Absorption hearts (24 HP), 3D flight;
        // Hostile step -> 8m stasis cage for 6.0s (120t), -8 armor, strips buffs, draws incoming projectiles into caught targets!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 6.0D, 480, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 0.15D, zone.center.z, 3, 1.2D, 0.05D, 1.2D, 0.01D);
                        srv.sendParticles(ParticleTypes.REVERSE_PORTAL, zone.center.x, zone.center.y + 0.15D, zone.center.z, 3, 1.2D, 0.05D, 1.2D, 0.01D);
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
                            if (st instanceof Player p) p.getFoodData().eat(20, 1.0F);
                            st.setAbsorptionAmount(Math.max(st.getAbsorptionAmount(), 24.0F));
                            SpellLingeringZoneManager.grantArcane3DFlight(st, 400);
                            return false;
                        }
                    }

                    AABB cage = new AABB(zone.center.x - 8.0D, zone.center.y - 2.0D, zone.center.z - 8.0D,
                            zone.center.x + 8.0D, zone.center.y + 5.0D, zone.center.z + 8.0D);
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, cage, e -> e.isAlive() && e != owner)) {
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                        ApexEschatonSingularityHelper.purgeBeneficialBuffs(foe);
                        SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 8.0D, 0.0D, 240L);
                        foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 120, 0));
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, foe, Vec3.ZERO, zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                    }

                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 10.0D, 120, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl > sZone.maxDurationTicks) return false;
                                LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                AABB pBox = new AABB(sZone.center.x - 10.0D, sZone.center.y - 3.0D, sZone.center.z - 10.0D,
                                        sZone.center.x + 10.0D, sZone.center.y + 6.0D, sZone.center.z + 10.0D);
                                ApexEschatonSingularityHelper.pullProjectilesToward(sLvl, sOwner, pBox, sZone.center.add(0.0D, 1.0D, 0.0D));
                                return true;
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
        // Thermodynamic Heat-Death Wave: freezes standing water -> Blue Ice, vitrifies sand -> Glass,
        // extinguishes fires, stuns hostiles 5.0s (100t), -8 armor, Cosmic Equilibrium Stupor (35.0s / 700t AI + 75% slow)!
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        BlockPos mid = BlockPos.containing(caster.getEyePosition().add(dir.scale(Math.min(8.0D, coneRange * 0.5D))));
        for (BlockPos p : BlockPos.betweenClosed(mid.offset(-4, -2, -4), mid.offset(4, 2, 4))) {
            BlockState bs = level.getBlockState(p);
            if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
            if (bs.is(Blocks.WATER)) {
                level.setBlockAndUpdate(p, Blocks.BLUE_ICE.defaultBlockState());
            } else if (bs.is(Blocks.SAND) || bs.is(Blocks.RED_SAND)) {
                level.setBlockAndUpdate(p, Blocks.GLASS.defaultBlockState());
            } else if (bs.getBlock() instanceof BaseFireBlock) {
                level.removeBlock(p, false);
            }
        }

        for (LivingEntity hit : hitTargets) {
            if (ApexEschatonSingularityHelper.isFriendly(hit, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 8.0D, 0.0D, 240L);
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 100, 0)); // 5.0s stun!
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 700, 4)); // 75% slow for 35.0s!
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
        // Cataclysmic Singularity Equilibrium Starfall: 20m zone, 30.0s (600t) + 15.0s (300t) Equilibrium Ground (900t total):
        // -8 armor, knocks flat, lifts into zero-g stasis every few seconds, heals allies +3 hearts/s (6 HP),
        // lingering 15.0s slows hostiles 80% while regenerating party Materia fuel!
        double r = Math.max(20.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 900, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    boolean starfall = elapsed <= 600;
                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + (starfall ? 5.5D : 0.4D), zone.center.z,
                                18, zone.radius * 0.55D, starfall ? 2.6D : 0.3D, zone.radius * 0.55D, 0.06D);
                        srv.sendParticles(ParticleTypes.CRIMSON_SPORE, zone.center.x, zone.center.y + (starfall ? 5.5D : 0.4D), zone.center.z,
                                18, zone.radius * 0.55D, starfall ? 2.6D : 0.3D, zone.radius * 0.55D, 0.04D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.5D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 10.0D, zone.center.z + zone.radius);
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (owner != null && ApexEschatonSingularityHelper.isFriendly(ent, owner)) {
                                if (starfall) {
                                    SpellCombatReactionEngine.notifyMagicalHeal(ent, 6.0F); // +3 hearts/s!
                                    ent.heal(6.0F);
                                } else if (ent instanceof Player p) {
                                    SpellFuelService.restorePlayerMateriaFuel(p, EssenceType.ENTROPICA, 96L);
                                }
                                continue;
                            }
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            if (starfall) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(ent, srv, 8.0D, 0.0D, 220L);
                                if (elapsed % 60 == 0) {
                                    ent.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 25, 0));
                                } else {
                                    ent.setDeltaMovement(ent.getDeltaMovement().x * 0.2D, -0.55D, ent.getDeltaMovement().z * 0.2D);
                                    ent.hasImpulse = true;
                                }
                                SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, ent, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.65F);
                            } else {
                                ent.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 5)); // 80% slow
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
        tag.putString("SpellEidolonMateria", EssenceType.ENTROPICA.name());
        tag.putInt("SpellEidolonTintRGB", ENTROPICA_CHRONO_SILVER_HEX_COLOR);
        level.sendParticles(ParticleTypes.END_ROD, spawnPos.x, spawnPos.y + 1.1D, spawnPos.z, 24, 0.6D, 0.7D, 0.6D, 0.06D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, spawnPos.x, spawnPos.y + 1.1D, spawnPos.z, 24, 0.6D, 0.7D, 0.6D, 0.06D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Parallax Event-Horizon Fold: 24m through solid terrain; departure leaves End-portal parallax decoy for 4.0s (80t)
        // taunting & pulling pursuers before exploding into balanced stasis nova;
        // arrival grants 12.0s (240t) 3D flight, Slow Falling, 4.0s (80t) complete damage immunity, sheds 100% aggro!
        SpellEidolonEntity.spawnShortLivedDecoy(
                level, caster, departurePos, EssenceType.ENTROPICA,
                32.0F, 80, "Parallax Event-Horizon Decoy", null
        );
        SpellLingeringZoneManager.grantArcane3DFlight(caster, 240);
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 260, 0));
        EntityHelper.getPersistentData(caster)
                .putLong(SpellLingeringZoneManager.TAG_COMPLETE_INVULNERABILITY_UNTIL, level.getGameTime() + 80L);
        ApexEschatonSingularityHelper.shedMobAggro(level, caster, arrivalPos, 28.0D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 0.45F, 1.35F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Grand Cosmic Equilibrium Transmutation [5.0s Channel, 16m, strictly NO fluids, strictly NO empty air]:
        // Placed cobblestone/dirt/gravel -> collectible Essence Orbs;
        // Placed stone/deepslate/netherrack -> Iridescent Primordial Runestone (Runed Astral Marble), Calcite, Harmonic Crystal Blocks (Aeterium Crystal Block);
        // Permanently purges all curses and permanently elevates ALL existing enchantments on worn equipment by +5 tiers!
        if (state.isAir() || !state.getFluidState().isEmpty()) {
            return false;
        }

        if (ApexEschatonSingularityHelper.beginOrExecuteChanneledTransmute(
                level, caster, pos, 16, potencyTier, EssenceType.ENTROPICA,
                "Grand Cosmic Equilibrium Transmutation",
                () -> {
                    if (caster != null) {
                        ApexEschatonSingularityHelper.purgeCursesAndElevateEquipment(caster, 5, 1.0F, Integer.MAX_VALUE);
                    }
                })) {
            return true;
        }

        if (state.is(Blocks.COBBLESTONE) || state.is(Blocks.DIRT) || state.is(Blocks.GRAVEL)) {
            level.removeBlock(pos, false);
            if (level.random.nextFloat() < 0.35F || caster == null) {
                SpellLingeringZoneManager.spawnTypedEssenceOrb(level, Vec3.atCenterOf(pos), EssenceType.ENTROPICA, 2);
            }
            return true;
        }

        if (state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE) || state.is(Blocks.COBBLED_DEEPSLATE) || state.is(Blocks.NETHERRACK)) {
            BlockState out = switch (level.random.nextInt(3)) {
                case 0 -> ModBlocks.RUNED_ASTRAL_MARBLE.get().defaultBlockState();
                case 1 -> Blocks.CALCITE.defaultBlockState();
                default -> ModBlocks.AETERIUM_CRYSTAL_BLOCK.get().defaultBlockState();
            };
            level.setBlockAndUpdate(pos, out);
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        if (ApexEschatonSingularityHelper.purgeCursesAndElevateStack(stack, level.random, 5, 1.0F, Integer.MAX_VALUE)) {
            itemEntity.setItem(stack.copy());
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Grand Equilibrium Paradox Hex: continuous true damage, -85% move speed, disables all healing,
        // -60% toughness, 60% weapon recoil, Ultimate Hybrid Player Hex Vector (in SpellCombatReactionEngine)!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.90D, 0.60D * mult), 45L);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 5, true, true));
        target.hurtServer(level, level.damageSources().fellOutOfWorld(), (4.0F + potencyTier * 0.95F) * mult);
        level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.9D), target.getZ(), 4, 0.3D, 0.35D, 0.3D, 0.03D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getY(0.9D), target.getZ(), 4, 0.3D, 0.35D, 0.3D, 0.03D);
        return true;
    }
}
