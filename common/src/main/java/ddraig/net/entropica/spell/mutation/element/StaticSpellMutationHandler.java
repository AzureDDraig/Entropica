package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModParticles;
import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import ddraig.net.entropica.spell.assembly.SubSpellSlot;
import ddraig.net.entropica.spell.engine.SpellCombatReactionEngine;
import ddraig.net.entropica.spell.engine.SpellEidolonSpecs;
import ddraig.net.entropica.spell.engine.SpellExecutionEngine;
import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.spell.fuel.MateriaStageTable;
import ddraig.net.entropica.spell.fuel.SpellFuelService;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.spell.vfx.SpellWorldVfxManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Complete per-part Signature Mutation implementation for <b>Static Materia (Tier 3 Trinity Fusion #43)</b>
 * (Spellcrafting System Guide Section 6.44 — Trinity Lightning + Air + Arid harmonic matrix):
 * <ul>
 *     <li><b>Palette</b>: Electric Yellow ({@code #FFE600}) + Galvanized Slate Gray ({@code #878DA2})</li>
 *     <li><b>Advantage</b>: Strong vs <b>EARTH (+1)</b>, <b>WATER (+1)</b>, <b>VOID (+1)</b>, <b>GLACIAL (+1)</b>,
 *         and <b>VAPOR (+1)</b> | Weak vs <b>NATURE (-1)</b> (plus <b>VOID</b> weakness under Air Dominance)</li>
 * </ul>
 */
public final class StaticSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int GALVANIC_STATIC_HEX_COLOR = 0xFFE600;
    public static final int GALVANIZED_SLATE_HEX_COLOR = 0x878DA2;

    private static final String TAG_STATIC_BEAM_LAST_TICK = "StaticBeamLastTick";
    private static final String TAG_STATIC_BEAM_TICKS = "StaticBeamContactTicks";
    private static final String TAG_STATIC_BEAM_ARMOR_SHRED = "StaticBeamArmorShred";
    private static final String TAG_STATIC_TRANSMUTE_BATCH_TICK = "StaticTransmuteBatchTick";

    @Override
    public EssenceType materiaType() {
        return EssenceType.STATIC;
    }

    // =========================================================================
    // TITLES & CODEX / TOOLTIP DESCRIPTIONS
    // =========================================================================

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return flow == SpellFlowModifier.ORBIT_SATELLITE ? "Galvanic Tesla Orbit Flow" : null;
    }

    @Override
    public String resolveFlowDescription(SpellFlowModifier flow) {
        return flow == SpellFlowModifier.ORBIT_SATELLITE
                ? "4 high-voltage ball-lightning spheres and polarized slate fragments (#FFE600 / #878DA2) paired in double-helix orbits grant +5 Armor, +2 Toughness, Lightning/Fall/Slowness immunity, metallic arrow vaporization, 1.0s melee paralyze backlash, and homing arc bolts that root targets for 2.0s and shock 3 nearby foes."
                : null;
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Dielectric Ball-Lightning Mortar";
            case BEAM -> "Dielectric Breakdown Arc-Stream Beam";
            case RADIAL_BURST -> "Ground-Discharge EMP Shockwave";
            case TOUCH_STRIKE -> "Dielectric Palm Overload";
            case AURA -> "Dielectric Faraday Shroud";
            case GLYPH -> "Galvanic Arc-Snare Sigil";
            case CONE -> "High-Voltage Static Arc-Fan Deluge";
            case SKYFALL -> "Galvanic Tesla Cataclysmic Tempest";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE ->
                    "Hurls a crackling orb of high-voltage static plasma & dielectric dust (#FFE600 / #878DA2) that zaps foes within 3m in flight, magnetizes metal armor, and detonates in a 5m EM burst knocking enemies 5m back, stripping 3 armor, and dealing heavy Static Materia damage.";
            case BEAM ->
                    "Channels a high-voltage arc stream & ionizing dust slipstream (#FFE600 / #878DA2) that fries targets, arcs secondary shocks, strips -1 armor per 1.5s, magnetizes metallic items into inventory, and after 5.0s sustained triggers Catastrophic Dielectric Flashover (colossal EM blast + 3.0s paralyze).";
            case RADIAL_BURST ->
                    "Slams a 6m Ground-Discharge EMP Shockwave (#FFE600 / #878DA2, zero physical light blocks) that disables creeper explosions, clears hostile mob aggro, paralyzes hostiles for 2.5s, and deals heavy Static Materia damage.";
            case TOUCH_STRIKE ->
                    "Direct Dielectric Palm Overload (#FFE600 / #878DA2) dealing heavy internal Static Materia damage, safely discharging creeper fuses without exploding, and blasting the target 8m backward with a galvanic shock.";
            case AURA ->
                    "Dielectric Faraday Shroud (#FFE600 / #878DA2): +7 Armor, +3 Toughness, Lightning/Fall/Slowness/Kinetic/Thorns immunity, +25% sprint & attack speed, mid-air metallic arrow vaporization, and 1.5s paralyzing Galvanic Backlash.";
            case GLYPH ->
                    "Disarmable Single/Repeating floor sigil (#FFE600 / #878DA2) discharging an 8m chain-lightning web that tethers up to 5 hostiles together, preventing separation, stripping 3 armor, and sharing shock damage whenever any linked foe is hurt.";
            case CONE ->
                    "60-degree High-Voltage Static Arc-Fan Deluge (#FFE600 / #878DA2) dealing continuous Static Materia damage, melting arrows/light projectiles, knocking foes 3m back, magnetizing weapons, and applying Electromagnetic Neurological Overload (-50% attack speed & 25.0s AI pathfinding disruption).";
            case SKYFALL ->
                    "18.0s Galvanic Tesla Cataclysmic Tempest across a 12m zone (#FFE600 / #878DA2): pins foes downward, shatters 5 armor, chain-paralyzes for 3.0s, disables creepers, and leaves an ionized ground field slowing enemies by 60% for 18s + 8s after while shocking sprinting mobs.";
            case SUMMON_AGENT ->
                    "Galvanic Arc-Vanguard (45s lifecycle / 65 HP): twin lightning-khopesh electric warrior executing rapid flashing dashes, 35% armor bypass, chain-lightning shocks, and 2.0s paralyze on critical strikes.";
            case SUMMON_SENTRY ->
                    "Dielectric Tesla Citadel Spire (45s lifecycle / 85 HP): anchored monolithic pylon firing 16m lightning beams, pulsing an 8m EMP every 4.0s that disables creepers & knocks back foes, and granting allies +6 Armor, +3 Toughness & Lightning/Fall immunity.";
            case SUMMON_DECOY ->
                    "Volatile Dielectric Spark Effigy (12s duration): 14m pack-taunt crackling lightning rod detonating into a 10m Catastrophic Tesla Nova (8m knockback, -4 armor, massive Static damage & 4.0s magnetize/paralyze).";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Ionized Slipstream Step";
            case TRANSMUTE, BREAK -> "Dielectric Electrolysis & Vitrification Transmute";
            case AFFLICTION -> "Galvanic Nerve-Spasm Hex";
            default -> "Galvanic Static " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK ->
                    "Silently phase-slides up to 14m through the atmospheric electrical grid (#FFE600 / #878DA2), leaving a sparking dielectric dust trail and completely shedding all hostile mob aggro.";
            case TRANSMUTE, BREAK ->
                    "Converts raw iron/copper ores & items into refined ingots with +25% bonus yield, vitrifies sand into glass/fulgurite, fuses cobblestone/gravel into deepslate, recharges machinery/batteries, and evaporates water into dry air (25% batch fuel discount).";
            case AFFLICTION ->
                    "Persistent neuro-muscular overload curse staggering & interrupting attacks/bows/spells every 3.0s, cutting move speed by 50%, attack speed by 40%, stripping 35% toughness, dealing continuous Static shocks, and arcing shocks to hostiles within 4m.";
            default -> "Infuses the effect with high-voltage Static Materia resonance (#FFE600 / #878DA2).";
        };
    }

    // =========================================================================
    // PART 1 — FLOW MUTATIONS (GALVANIC TESLA ORBIT FLOW)
    // =========================================================================

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        if (projectile.tickCount % 2 == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_ARC.get(), GALVANIC_STATIC_HEX_COLOR, 14, 0.88F, 0.05F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D
            );
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), GALVANIZED_SLATE_HEX_COLOR, 16, 0.72F, 0.05F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D
            );
        }
        // Vaporizes incoming metallic arrows in mid-air around the orbiting spheres ONLY when ORBIT_SATELLITE Flow is active!
        if (flow == SpellFlowModifier.ORBIT_SATELLITE) {
            Entity owner = projectile.getOwner();
            for (AbstractArrow arrow : level.getEntitiesOfClass(AbstractArrow.class, projectile.getBoundingBox().inflate(2.2D))) {
                if (arrow.getOwner() == owner) continue;
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, arrow.getX(), arrow.getY(), arrow.getZ(), 8, 0.12D, 0.12D, 0.12D, 0.06D);
                arrow.discard();
            }
        }
    }

    // =========================================================================
    // PART 2 — DELIVERY FORM MUTATIONS
    // =========================================================================

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        // Section 6.44 Dielectric Ball-Lightning Mortar: continuously zaps enemies within 3m as it flies & magnetizes metal armor!
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.10D, 0.10D, 0.10D, 0.03D);
        if (projectile.tickCount % 5 == 0) {
            Entity owner = projectile.getOwner();
            LivingEntity caster = owner instanceof LivingEntity l ? l : null;
            int tier = projectile.getVisualPotencyTier();
            for (LivingEntity near : level.getEntitiesOfClass(LivingEntity.class, projectile.getBoundingBox().inflate(3.0D), e -> e.isAlive() && e != owner)) {
                if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(near, caster, true)) continue;
                SpellWorldVfxManager.broadcastChainArc(
                        level, projectile.position(), near.getBoundingBox().getCenter(),
                        EssenceType.STATIC, EssenceType.LIGHTNING, tier
                );
                if (hasMetalEquipment(near)) {
                    Vec3 pull = projectile.position().subtract(near.position()).normalize().scale(0.28D);
                    near.setDeltaMovement(near.getDeltaMovement().scale(0.65D).add(pull.x, 0.06D, pull.z));
                    near.hasImpulse = true;
                    SpellCombatReactionEngine.applyTimedAttributeShred(near, level, 1.0D, 0.0D, 60L);
                }
                SpellCombatReactionEngine.hurtWithStaticMateriaAdvantage(
                        level, near, caster, 1.85F + tier * 0.45F, false, true
                );
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, near.getX(), near.getY(0.8D), near.getZ(), 6, 0.2D, 0.25D, 0.2D, 0.04D);
            }
        }
    }

    @Override
    public void onProjectileHitEntityFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            Entity hitEntity,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        detonateEmBurst(level, caster, hitEntity.position(), hitEntity, spell, slot);
    }

    @Override
    public void onProjectileHitBlockFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            BlockHitResult blockHit,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        LightningSpellMutationHandler.tryMachineRecharging(level, blockHit.getBlockPos(), EssenceType.STATIC, spell.getEffectiveTier());
        detonateEmBurst(level, caster, blockHit.getLocation(), null, spell, slot);
    }

    private static void detonateEmBurst(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 center,
            @Nullable Entity directHit,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Section 6.44: 5m electromagnetic burst knocking enemies 5m backward, stripping 3 armor & dealing heavy Static Materia damage!
        int tier = spell.getEffectiveTier();
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        SpellWorldVfxManager.broadcastProjectileImpact(
                level, center.add(0.0D, 0.2D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.STATIC, EssenceType.LIGHTNING, 1.85F, tier
        );
        SpellWorldVfxManager.broadcastRadialNova(level, center, 5.0D, EssenceType.STATIC, EssenceType.ARID, tier);

        float baseDmg = 3.85F + tier * 0.95F;
        AABB box = new AABB(center.x - 5.0D, center.y - 2.0D, center.z - 5.0D, center.x + 5.0D, center.y + 3.2D, center.z + 5.0D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != caster)) {
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, safeguard)) continue;
            Vec3 delta = foe.position().subtract(center);
            Vec3 away = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.28D) : new Vec3(0.0D, 0.36D, 0.0D);
            foe.setDeltaMovement(away.x, 0.36D, away.z); // ~5m backward knockback!
            foe.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 3.0D, 0.0D, 140L);
            if (!(foe == directHit && slot.hasHarmVerb())) {
                float splashMult = (foe == directHit) ? 1.0F : 0.72F;
                SpellCombatReactionEngine.hurtWithStaticMateriaAdvantage(level, foe, caster, baseDmg * splashMult, false, true);
            }
        }
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y + 0.6D, center.z, 32, 1.8D, 0.6D, 1.8D, 0.08D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.75F, 1.38F);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 2 — DIELECTRIC BREAKDOWN ARC-STREAM BEAM (BEAM)
    // -------------------------------------------------------------------------

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
        onBeamFormCast(level, caster, direction, maxReach, hitEntities, blockHit, spell, slot, fallbackMateria, apparatusPotency, 0);
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
            float apparatusPotency,
            int heldTicks
    ) {
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        Vec3 eye = caster.getEyePosition();
        Vec3 dirNorm = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();

        if (blockHit != null) {
            LightningSpellMutationHandler.tryMachineRecharging(level, blockHit.getBlockPos(), EssenceType.STATIC, tier);
        }

        AABB corridor = new AABB(eye, eye.add(dirNorm.scale(maxReach))).inflate(2.5D);
        for (ItemEntity ie : level.getEntitiesOfClass(ItemEntity.class, corridor, ItemEntity::isAlive)) {
            if (isMetallicItem(ie.getItem()) || ie.distanceToSqr(caster) <= 36.0D) {
                if (caster instanceof Player player && player.getInventory().add(ie.getItem().copy())) {
                    ie.discard();
                } else {
                    ie.setPos(caster.getX(), caster.getY() + 0.5D, caster.getZ());
                }
            }
        }

        long now = level.getGameTime();
        float beamDmg = (2.35F + tier * 0.60F) * Math.max(0.5F, apparatusPotency);

        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithStaticMateriaAdvantage(level, hit, caster, beamDmg, false, true);
            }

            // Secondary arc shocks to adjacent hostiles!
            int secondary = 0;
            for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, hit.getBoundingBox().inflate(3.5D), e -> e.isAlive() && e != hit && e != caster)) {
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(adj, caster, wardSafe)) continue;
                SpellWorldVfxManager.broadcastChainArc(
                        level, hit.getBoundingBox().getCenter(), adj.getBoundingBox().getCenter(),
                        EssenceType.STATIC, EssenceType.LIGHTNING, tier
                );
                SpellCombatReactionEngine.hurtWithStaticMateriaAdvantage(level, adj, caster, beamDmg * 0.65F, false, true);
                if (++secondary >= 3) break;
            }

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong(TAG_STATIC_BEAM_LAST_TICK).orElse(-100L);
            if (last == now) {
                continue;
            }
            long delta = now - last;
            int contactTicks = heldTicks <= 0
                    ? 0
                    : ((delta > 0L && delta <= 16L) ? data.getInt(TAG_STATIC_BEAM_TICKS).orElse(0) + (int) delta : 4);
            data.putLong(TAG_STATIC_BEAM_LAST_TICK, now);
            data.putInt(TAG_STATIC_BEAM_TICKS, contactTicks);

            // -1 armor per 1.5s (30 ticks) of sustained contact (up to -8 armor)!
            double armorShred = Math.clamp(1.0D + (contactTicks / 30), 1.0D, 8.0D);
            data.putDouble(TAG_STATIC_BEAM_ARMOR_SHRED, armorShred);
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, armorShred, 0.0D, 100L);

            if (heldTicks > 0 && contactTicks >= 100) {
                data.putInt(TAG_STATIC_BEAM_TICKS, 0);
                SpellWorldVfxManager.broadcastRadialNova(level, hit.position(), 4.5D, EssenceType.STATIC, EssenceType.LIGHTNING, tier);
                float flashoverDmg = (11.0F + tier * 2.65F) * Math.max(0.5F, apparatusPotency);
                for (LivingEntity caught : level.getEntitiesOfClass(LivingEntity.class, hit.getBoundingBox().inflate(4.5D), e -> e.isAlive() && e != caster)) {
                    if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(caught, caster, wardSafe)) continue;
                    caught.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 60, 0));
                    SpellCombatReactionEngine.hurtWithStaticMateriaAdvantage(level, caught, caster, flashoverDmg, false, true);
                }
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 0.90F, 0.05F),
                        hit.getX(), hit.getY(1.0D), hit.getZ(), 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, hit.getX(), hit.getY(0.9D), hit.getZ(), 36, 1.5D, 0.6D, 1.5D, 0.10D);
                level.playSound(null, hit.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.95F, 1.30F);
            }
        }
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 3 — GROUND-DISCHARGE EMP SHOCKWAVE (RADIAL BURST)
    // -------------------------------------------------------------------------

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
        // Section 6.44 Ground-Discharge EMP Shockwave:
        // 6m radius, strictly ZERO physical light blocks — disables creepers, clears all hostile mob aggro,
        // paralyzes caught hostiles in galvanic stasis for 2.5s (50t), and deals heavy Static Materia damage!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        double r = Math.max(6.0D, radius);

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.STATIC, EssenceType.ARID, tier);

        float empDmg = (4.20F + tier * 1.05F) * Math.max(0.5F, apparatusPotency);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.5D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != caster)) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;
            if (foe instanceof Creeper creeper) {
                creeper.setSwellDir(-1);
                SpellLingeringZoneManager.applyArcaneSilence(creeper, 120);
            }
            if (foe instanceof Mob mob) {
                mob.setTarget(null);
                mob.setAggressive(false);
            }
            foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 50, 0));
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithStaticMateriaAdvantage(level, foe, caster, empDmg, false, true);
            }
        }
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 0.90F, 0.05F),
                center.x, center.y + 0.8D, center.z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y + 0.4D, center.z, 44, 2.4D, 0.3D, 2.4D, 0.08D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.85F, 1.42F);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 4 — DIELECTRIC PALM OVERLOAD (TOUCH / STRIKE)
    // -------------------------------------------------------------------------

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
        // Section 6.44 Dielectric Palm Overload:
        // High internal Static Materia damage, safely discharges creeper fuses without exploding,
        // and blasts victim 8m backward with a disorienting galvanic shock!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        Vec3 push = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D), push, 2.8D,
                EssenceType.STATIC, EssenceType.LIGHTNING, tier
        );

        float palmDmg = (4.60F + tier * 1.15F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;
            if (hit instanceof Creeper creeper) {
                creeper.setSwellDir(-1);
                SpellLingeringZoneManager.applyArcaneSilence(creeper, 120);
            }
            hit.setDeltaMovement(push.x * 1.85D, 0.42D, push.z * 1.85D); // 8m knockback!
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 20, 0));
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithStaticMateriaAdvantage(level, hit, caster, palmDmg, true, true);
            }
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, hit.getX(), hit.getY(0.9D), hit.getZ(), 22, 0.35D, 0.4D, 0.35D, 0.08D);
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.72F, 1.52F);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 5 — DIELECTRIC FARADAY SHROUD (AURA / SELF)
    // -------------------------------------------------------------------------

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
        // Section 6.44 Dielectric Faraday Shroud:
        // +7 Armor, +3 Toughness, +25% sprint & attack speed, Lightning/Fall/Slowness/Kinetic/Thorns immunity,
        // mid-air metallic arrow vaporization, and 1.5s paralyzing Galvanic Backlash!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_STATIC_SHROUD_UNTIL, level.getGameTime() + dur);
        caster.removeEffect(MobEffects.SLOWNESS);
        caster.resetFallDistance();
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 7.0D, 3.0D, 0.0D, 0.25D, dur);
        SpellCombatReactionEngine.applyTimedAttackSpeedBuff(caster, level, 0.25D, dur);
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.60F, null);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, caster.getX(), caster.getY(0.9D), caster.getZ(), 26, 0.45D, 0.55D, 0.45D, 0.06D);
        level.playSound(null, caster.blockPosition(), SoundEvents.COPPER_BULB_TURN_ON, SoundSource.PLAYERS, 0.95F, 1.35F);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 6 — GALVANIC ARC-SNARE SIGIL (GLYPH / TRAP)
    // -------------------------------------------------------------------------

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
        // Section 6.44 Galvanic Arc-Snare Sigil:
        // Disarmable Single/Repeating hidden floor trap (#FFE600 / #878DA2): when stepped on by a hostile mob,
        // discharges a web of chain-lightning across an 8m radius, tethering up to 5 caught hostiles together,
        // preventing them from moving apart, shocking all linked targets whenever one takes damage, and stripping 3 armor!
        int tier = spell.getEffectiveTier();
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snapped = new Vec3(surface.getX() + 0.5D, surface.above().getY(), surface.getZ() + 0.5D);
        double trapRad = Math.max(3.5D, radius);

        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxTriggers = repeating ? 3 : 1;
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE / 4 : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snapped.add(0.0D, 0.03D, 0.0D), trapRad, EssenceType.STATIC, EssenceType.ARID, Math.min(240, maxDuration), tier
        );

        final int[] triggersLeft = {maxTriggers};
        final int[] rearmCooldown = {0};

        SpellLingeringZoneManager.ActiveSpellZone glyphZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snapped, trapRad, maxDuration, level.getGameTime(), spell, slot, EssenceType.STATIC, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();

                    if (elapsed > 0 && elapsed % 200 == 0) {
                        int rem = zone.maxDurationTicks >= 1_000_000_000 ? 240 : (int) Math.min(240L, zone.maxDurationTicks - elapsed);
                        SpellWorldVfxManager.broadcastArcaneSeal(
                                srv, zone.center.add(0.0D, 0.03D, 0.0D), zone.radius, EssenceType.STATIC, EssenceType.ARID, rem, tier
                        );
                    }
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.ELECTRIC_SPARK, zone.center.x, zone.center.y + 0.08D, zone.center.z, 4, 0.8D, 0.05D, 0.8D, 0.02D);
                    }

                    if (elapsed < 20) return true; // 1.0s arming delay
                    if (rearmCooldown[0] > 0) {
                        rearmCooldown[0]--;
                        return true;
                    }

                    AABB triggerBox = new AABB(
                            zone.center.x - 2.6D, zone.center.y - 1.0D, zone.center.z - 2.6D,
                            zone.center.x + 2.6D, zone.center.y + 2.0D, zone.center.z + 2.6D
                    );
                    if (srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e -> e.isAlive() && e != owner
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe))).isEmpty()) {
                        return true;
                    }

                    triggersLeft[0]--;
                    rearmCooldown[0] = 60;
                    triggerGalvanicArcSnareWeb(srv, owner, zone.center, zone.spell, zone.slot, zone.apparatusPotency);
                    return triggersLeft[0] > 0;
                }
        ).markDisarmableGlyph();

        SpellLingeringZoneManager.registerZone(level, glyphZone);
        return true;
    }

    private static void triggerGalvanicArcSnareWeb(
            ServerLevel level,
            @Nullable LivingEntity owner,
            Vec3 center,
            SpellAssembly spell,
            SubSpellSlot slot,
            float apparatusPotency
    ) {
        int tier = spell != null ? spell.getEffectiveTier() : 1;
        boolean wardSafe = spell != null && spell.hasWardFriendlyFireSafeguard();
        int webDuration = 120; // 6.0s electromagnetic snare web!
        long snareUntil = level.getGameTime() + webDuration;
        String groupId = UUID.randomUUID().toString();
        String casterId = owner != null ? owner.getUUID().toString() : "";

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, center.add(0.0D, 0.04D, 0.0D), 8.0D, EssenceType.STATIC, EssenceType.LIGHTNING, webDuration, tier
        );
        level.playSound(null, BlockPos.containing(center), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.78F, 1.48F);

        AABB snareSearch = new AABB(center.x - 8.0D, center.y - 2.0D, center.z - 8.0D, center.x + 8.0D, center.y + 3.5D, center.z + 8.0D);
        List<UUID> linkedIds = new ArrayList<>();
        LivingEntity prev = null;
        for (LivingEntity cand : level.getEntitiesOfClass(LivingEntity.class, snareSearch, e -> e.isAlive() && e != owner)) {
            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(cand, owner, wardSafe)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(cand, level, 3.0D, 0.0D, webDuration + 40L);
            CompoundTag cTag = EntityHelper.getPersistentData(cand);
            cTag.putLong(SpellLingeringZoneManager.TAG_STATIC_ARC_SNARE_UNTIL, snareUntil);
            cTag.putString(SpellLingeringZoneManager.TAG_STATIC_ARC_SNARE_GROUP, groupId);
            cTag.putString(SpellLingeringZoneManager.TAG_STATIC_ARC_SNARE_CASTER, casterId);

            SpellWorldVfxManager.broadcastChainArc(
                    level, prev != null ? prev.getBoundingBox().getCenter() : center.add(0.0D, 0.4D, 0.0D),
                    cand.getBoundingBox().getCenter(), EssenceType.STATIC, EssenceType.LIGHTNING, tier
            );
            prev = cand;
            linkedIds.add(cand.getUUID());
            if (linkedIds.size() >= 5) break;
        }

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                owner, center, 8.0D, webDuration, level.getGameTime(), spell, slot, EssenceType.STATIC, apparatusPotency,
                (sLvl, sZone, sEl) -> {
                    if (sEl > sZone.maxDurationTicks || linkedIds.isEmpty()) return false;
                    LivingEntity sOwner = sZone.resolveCaster(sLvl);
                    List<LivingEntity> activeLinked = new ArrayList<>();
                    double sumX = 0.0D;
                    double sumY = 0.0D;
                    double sumZ = 0.0D;
                    for (UUID id : linkedIds) {
                        Entity e = sLvl.getEntity(id);
                        if (e instanceof LivingEntity v && v.isAlive()) {
                            activeLinked.add(v);
                            sumX += v.getX();
                            sumY += v.getY();
                            sumZ += v.getZ();
                        }
                    }
                    if (activeLinked.isEmpty()) return false;

                    Vec3 tetherHub = activeLinked.size() >= 2
                            ? new Vec3(sumX / activeLinked.size(), sumY / activeLinked.size(), sumZ / activeLinked.size())
                            : sZone.center;

                    for (int i = 0; i < activeLinked.size(); i++) {
                        LivingEntity v = activeLinked.get(i);
                        // Prevent tethered hostiles from moving apart!
                        if (v.position().distanceToSqr(tetherHub) > 10.24D) {
                            Vec3 pull = tetherHub.subtract(v.position()).scale(0.36D);
                            v.setDeltaMovement(pull.x, 0.06D, pull.z);
                            v.hasImpulse = true;
                        }
                        if (sEl % 10 == 0 && activeLinked.size() >= 2) {
                            LivingEntity next = activeLinked.get((i + 1) % activeLinked.size());
                            SpellWorldVfxManager.broadcastChainArc(
                                    sLvl, v.getBoundingBox().getCenter(), next.getBoundingBox().getCenter(),
                                    EssenceType.STATIC, EssenceType.ARID, tier
                            );
                        }
                        if (sEl % 20 == 0) {
                            SpellCombatReactionEngine.hurtWithStaticMateriaAdvantage(
                                    sLvl, v, sOwner, 2.05F + tier * 0.50F, false, true
                            );
                            if (sZone.spell != null && sZone.slot != null) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(
                                        sLvl, sOwner, v, Vec3.ZERO, sZone.spell, sZone.slot, EssenceType.STATIC, sZone.apparatusPotency * 0.50F
                                );
                            }
                            sLvl.sendParticles(ParticleTypes.ELECTRIC_SPARK, v.getX(), v.getY(0.8D), v.getZ(), 8, 0.25D, 0.3D, 0.25D, 0.04D);
                        }
                    }
                    return true;
                }
        ));
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 7 — HIGH-VOLTAGE STATIC ARC-FAN DELUGE (CONE / SPRAY)
    // -------------------------------------------------------------------------

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
        // Section 6.44 High-Voltage Static Arc-Fan Deluge:
        // Continuous Static Materia damage, melts arrows & light projectiles, knocks enemies back 3m,
        // magnetizes weapons, and applies Electromagnetic Neurological Overload (-50% attack speed & 25.0s / 500t AI disruption)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        Vec3 dirNorm = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();

        SpellWorldVfxManager.broadcastVortexCone(
                level, caster.getEyePosition().subtract(0.0D, 0.2D, 0.0D), dirNorm, coneRange,
                EssenceType.STATIC, EssenceType.ARID, tier
        );

        AABB projCone = new AABB(caster.getEyePosition(), caster.getEyePosition().add(dirNorm.scale(coneRange))).inflate(2.5D);
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, projCone, p -> p.getOwner() != caster && !(p instanceof SpellProjectileEntity))) {
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, proj.getX(), proj.getY(), proj.getZ(), 6, 0.12D, 0.12D, 0.12D, 0.04D);
            proj.discard();
        }

        float coneDmg = (3.15F + tier * 0.80F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;
            hit.setDeltaMovement(dirNorm.x * 0.88D, 0.24D, dirNorm.z * 0.88D); // 3m knockback!
            hit.hasImpulse = true;
            if (hit.isUsingItem()) {
                hit.stopUsingItem(); // Magnetizes held weapons/bows!
            }
            SpellCombatReactionEngine.applyTimedAttackDebuff(hit, level, 0.0D, 0.50D, 120L);
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 500);
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithStaticMateriaAdvantage(level, hit, caster, coneDmg, false, true);
            }
        }
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 8 — GALVANIC TESLA CATACLYSMIC TEMPEST (RAIN / SKYFALL)
    // -------------------------------------------------------------------------

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
        // Section 6.44 Galvanic Tesla Cataclysmic Tempest:
        // Extended 18.0s (360t) storm + 8.0s (160t) ionized ground field (520t total) across 12m+ radius —
        // repeatedly strikes hostiles with heavy Static Materia damage, forcefully pins enemies downward,
        // shatters 5 armor points, chain-paralyzes 3.0s (60t), disables creepers, and slows enemy movement speed
        // by 60% for all 520t while shocking any mob that attempts to sprint!
        int tier = spell.getEffectiveTier();
        double r = Math.max(12.0D, radius);

        SpellWorldVfxManager.broadcastSkyfallPillar(level, impactCenter, r, EssenceType.STATIC, EssenceType.LIGHTNING, 360, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, impactCenter.add(0.0D, 0.05D, 0.0D), r, EssenceType.STATIC, EssenceType.ARID, 520, tier);
        level.playSound(null, BlockPos.containing(impactCenter), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 1.0F, 1.15F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 520, level.getGameTime(), spell, slot, EssenceType.STATIC, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    boolean stormActive = elapsed <= 360;

                    if (stormActive && elapsed > 0 && elapsed % 90 == 0) {
                        SpellWorldVfxManager.broadcastSkyfallPillar(
                                srv, zone.center, zone.radius, EssenceType.STATIC, EssenceType.LIGHTNING,
                                (int) Math.max(40L, 360L - elapsed), tier
                        );
                    }
                    if (stormActive && elapsed % 12 == 0) {
                        double ang = srv.random.nextDouble() * (Math.PI * 2.0D);
                        double dist = Math.sqrt(srv.random.nextDouble()) * (zone.radius * 0.85D);
                        Vec3 strikePos = new Vec3(
                                zone.center.x + Math.cos(ang) * dist,
                                zone.center.y + 0.15D,
                                zone.center.z + Math.sin(ang) * dist
                        );
                        SpellWorldVfxManager.broadcastProjectileImpact(
                                srv, strikePos, new Vec3(0.0D, 1.0D, 0.0D),
                                EssenceType.STATIC, EssenceType.LIGHTNING, 1.6F, tier
                        );
                    }
                    if (elapsed % 6 == 0) {
                        srv.sendParticles(ParticleTypes.ELECTRIC_SPARK, zone.center.x, zone.center.y + (stormActive ? 3.5D : 0.3D), zone.center.z,
                                26, zone.radius * 0.55D, stormActive ? 2.0D : 0.2D, zone.radius * 0.55D, 0.08D);
                    }

                    if (elapsed % 16 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        long now = srv.getGameTime();
                        float tempestDmg = (3.45F + tier * 0.88F) * Math.max(0.5F, zone.apparatusPotency);
                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 6.0D, zone.center.z + zone.radius
                        );
                        LivingEntity prevStruck = null;
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) continue;

                            // 60% slow + sprint-shock ionized ground field for full 18s storm + 8s after (520t)!
                            SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, srv, 0.60D, 35L);
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 3));
                            EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_STATIC_SPRINT_SHOCK_UNTIL, now + 35L);
                            if (foe instanceof Creeper creeper) {
                                creeper.setSwellDir(-1);
                                SpellLingeringZoneManager.applyArcaneSilence(creeper, 80);
                            }

                            if (stormActive) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 5.0D, 0.0D, 160L);
                                foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 60, 0));
                                foe.setDeltaMovement(foe.getDeltaMovement().x * 0.25D, -0.38D, foe.getDeltaMovement().z * 0.25D);
                                foe.hasImpulse = true;
                                if (prevStruck != null) {
                                    SpellWorldVfxManager.broadcastChainArc(
                                            srv, prevStruck.getBoundingBox().getCenter(), foe.getBoundingBox().getCenter(),
                                            EssenceType.STATIC, EssenceType.LIGHTNING, tier
                                    );
                                }
                                prevStruck = foe;
                                SpellCombatReactionEngine.hurtWithStaticMateriaAdvantage(srv, foe, owner, tempestDmg, false, true);
                                if (zone.spell != null && zone.slot != null) {
                                    SpellExecutionEngine.deliverSubSpellOnEntity(
                                            srv, owner, foe, new Vec3(0.0D, -1.0D, 0.0D),
                                            zone.spell, zone.slot, EssenceType.STATIC, zone.apparatusPotency * 0.45F
                                    );
                                }
                            }
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 9 — GALVANIC STATIC APEX EIDOLON SUITE (SUMMON / EIDOLON)
    // -------------------------------------------------------------------------

    @Override
    public void onSummonFormSpawned(
            ServerLevel level,
            LivingEntity caster,
            Entity summonEntity,
            Vec3 spawnPos,
            double radius,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        CompoundTag tag = EntityHelper.getPersistentData(summonEntity);
        tag.putString("SpellEidolonMateria", EssenceType.STATIC.name());
        tag.putInt("SpellEidolonTintRGB", GALVANIC_STATIC_HEX_COLOR);
        SpellWorldVfxManager.broadcastSummonPillar(
                level, spawnPos, Math.max(1.8D, radius * 0.55D), EssenceType.STATIC, EssenceType.LIGHTNING, spell.getEffectiveTier()
        );
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 28, 0.45D, 0.55D, 0.45D, 0.06D);
    }

    // =========================================================================
    // PART 3 — CORE EFFECT MUTATIONS
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Section 6.44 Ionized Slipstream Step:
        // Silent 14m step leaving behind a lingering trail of sparking dielectric dust and shedding all hostile mob aggro!
        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.STATIC, potencyTier);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(departurePos, arrivalPos).inflate(18.0D))) {
            if (mob.getTarget() == caster) {
                mob.setTarget(null);
                mob.setAggressive(false);
            }
        }
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, departurePos.x, departurePos.y + 0.8D, departurePos.z, 20, 0.35D, 0.45D, 0.35D, 0.05D);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, arrivalPos.x, arrivalPos.y + 0.8D, arrivalPos.z, 20, 0.35D, 0.45D, 0.35D, 0.05D);

        // Lingering 3.0s (60t) trail of sparking dielectric dust (#FFE600 / #878DA2) between departure & arrival!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, departurePos.lerp(arrivalPos, 0.5D), 2.5D, 60, level.getGameTime(), null, null, EssenceType.STATIC, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 8 == 0) {
                        for (int i = 0; i <= 5; i++) {
                            Vec3 p = departurePos.lerp(arrivalPos, i / 5.0D);
                            srv.sendParticles(
                                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_ARC.get(), GALVANIC_STATIC_HEX_COLOR, 16, 0.85F, 0.05F),
                                    p.x, p.y + 0.65D, p.z, 2, 0.18D, 0.18D, 0.18D, 0.01D
                            );
                            srv.sendParticles(
                                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), GALVANIZED_SLATE_HEX_COLOR, 18, 0.72F, 0.05F),
                                    p.x, p.y + 0.55D, p.z, 2, 0.22D, 0.15D, 0.22D, 0.01D
                            );
                        }
                    }
                    return true;
                }
        ));
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Section 6.44 Dielectric Electrolysis & Vitrification Transmute [2.5s Continuous Held Right-Click Channel]:
        if (ApexEschatonSingularityHelper.beginOrExecuteChanneledTransmute(
                level, caster, pos, 5, potencyTier, EssenceType.STATIC,
                "Dielectric Electrolysis & Vitrification Transmute",
                () -> LightningSpellMutationHandler.tryMachineRecharging(level, pos, EssenceType.STATIC, potencyTier))) {
            return true;
        }
        boolean didAny = LightningSpellMutationHandler.tryMachineRecharging(level, pos, EssenceType.STATIC, potencyTier);

        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return didAny;
        }

        if (state.is(Blocks.IRON_ORE) || state.is(Blocks.DEEPSLATE_IRON_ORE)) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            spawnRefinedOreYield(level, Vec3.atCenterOf(pos), true, 1);
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.RAW_IRON_BLOCK)) {
            level.setBlockAndUpdate(pos, Blocks.IRON_BLOCK.defaultBlockState());
            // +25% bonus yield on 9 ingots = +2.25 ingots (2 Iron Ingots + 2 Iron Nuggets)!
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D, new ItemStack(Items.IRON_INGOT, 2)));
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D, new ItemStack(Items.IRON_NUGGET, 2)));
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.COPPER_ORE) || state.is(Blocks.DEEPSLATE_COPPER_ORE)) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            int rawEquiv = 2 + level.random.nextInt(4); // Vanilla copper ore drops 2–5 raw copper!
            spawnRefinedOreYield(level, Vec3.atCenterOf(pos), false, rawEquiv);
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.RAW_COPPER_BLOCK)) {
            level.setBlockAndUpdate(pos, Blocks.COPPER_BLOCK.defaultBlockState());
            int bonusCopper = 2 + (level.random.nextFloat() < 0.25F ? 1 : 0);
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D, new ItemStack(Items.COPPER_INGOT, bonusCopper)));
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
            BlockState glass = level.random.nextFloat() < 0.30F ? Blocks.TINTED_GLASS.defaultBlockState() : Blocks.GLASS.defaultBlockState();
            level.setBlockAndUpdate(pos, glass);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.getX() + 0.5D, pos.getY() + 0.6D, pos.getZ() + 0.5D, 8, 0.25D, 0.25D, 0.25D, 0.04D);
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.COBBLESTONE) || state.is(Blocks.GRAVEL)) {
            level.setBlockAndUpdate(pos, Blocks.DEEPSLATE.defaultBlockState());
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.WATER)) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            level.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 6, 0.25D, 0.25D, 0.25D, 0.03D);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 6, 0.25D, 0.25D, 0.25D, 0.04D);
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (didAny) {
            refundBatchDiscount(level, caster, potencyTier);
        }
        return didAny;
    }

    private static void spawnRefinedOreYield(ServerLevel level, Vec3 pos, boolean iron, int rawCount) {
        float totalIngots = rawCount * 1.25F;
        int fullIngots = Math.max(1, (int) totalIngots);
        float frac = totalIngots - fullIngots;
        level.addFreshEntity(new ItemEntity(level, pos.x, pos.y, pos.z, new ItemStack(iron ? Items.IRON_INGOT : Items.COPPER_INGOT, fullIngots)));
        if (iron) {
            int extraNuggets = Math.round(frac * 9.0F);
            if (extraNuggets > 0) {
                level.addFreshEntity(new ItemEntity(level, pos.x, pos.y + 0.15D, pos.z, new ItemStack(Items.IRON_NUGGET, extraNuggets)));
            }
        } else if (frac > 0.001F && level.random.nextFloat() < frac) {
            level.addFreshEntity(new ItemEntity(level, pos.x, pos.y + 0.15D, pos.z, new ItemStack(Items.COPPER_INGOT, 1)));
        }
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y + 0.2D, pos.z, 12, 0.25D, 0.25D, 0.25D, 0.06D);
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        if (stack.is(Items.RAW_IRON) || stack.is(Items.RAW_COPPER)
                || stack.is(Items.IRON_ORE) || stack.is(Items.DEEPSLATE_IRON_ORE)
                || stack.is(Items.COPPER_ORE) || stack.is(Items.DEEPSLATE_COPPER_ORE)) {
            boolean iron = stack.is(Items.RAW_IRON) || stack.is(Items.IRON_ORE) || stack.is(Items.DEEPSLATE_IRON_ORE);
            float totalIngots = stack.getCount() * 1.25F;
            int fullIngots = Math.min(64, Math.max(1, (int) totalIngots));
            float frac = Math.max(0.0F, totalIngots - (int) totalIngots);
            itemEntity.setItem(new ItemStack(iron ? Items.IRON_INGOT : Items.COPPER_INGOT, fullIngots));
            if (iron) {
                int extraNuggets = Math.round(frac * 9.0F);
                if (extraNuggets > 0) {
                    level.addFreshEntity(new ItemEntity(level, itemEntity.getX(), itemEntity.getY() + 0.2D, itemEntity.getZ(),
                            new ItemStack(Items.IRON_NUGGET, extraNuggets)));
                }
            } else if (frac > 0.001F && level.random.nextFloat() < frac) {
                level.addFreshEntity(new ItemEntity(level, itemEntity.getX(), itemEntity.getY() + 0.2D, itemEntity.getZ(),
                        new ItemStack(Items.COPPER_INGOT, 1)));
            }
            return true;
        }
        if (stack.is(Items.SAND) || stack.is(Items.RED_SAND)) {
            itemEntity.setItem(new ItemStack(level.random.nextFloat() < 0.30F ? Items.TINTED_GLASS : Items.GLASS, stack.getCount()));
            return true;
        }
        if (stack.is(Items.COBBLESTONE) || stack.is(Items.GRAVEL)) {
            itemEntity.setItem(new ItemStack(Items.DEEPSLATE, stack.getCount()));
            return true;
        }
        return false;
    }

    private static void refundBatchDiscount(ServerLevel level, @Nullable LivingEntity caster, int tier) {
        if (caster instanceof Player player) {
            CompoundTag pData = EntityHelper.getPersistentData(player);
            long now = level.getGameTime();
            if (pData.getLong(TAG_STATIC_TRANSMUTE_BATCH_TICK).orElse(-10L) != now) {
                pData.putLong(TAG_STATIC_TRANSMUTE_BATCH_TICK, now);
                long refund = Math.max(1L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25D));
                SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.STATIC, refund);
            }
        }
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.44 Galvanic Nerve-Spasm Hex:
        // Periodic stagger/interrupt every 3.0s (attacks, bow draws & spellcasting), -50% move speed,
        // -40% attack speed, -35% toughness, continuous Static shocks, and 4m secondary arc shocks!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        long now = level.getGameTime();
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.80D, 0.35D * mult), 45L);
        SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, Math.min(0.80D, 0.50D * mult), 45L);
        SpellCombatReactionEngine.applyTimedAttackDebuff(target, level, 0.0D, Math.min(0.80D, 0.40D * mult), 45L);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 2, true, true));

        if ((now / 20L) % 3L == 0L) {
            target.stopUsingItem();
            SpellLingeringZoneManager.applyArcaneSilence(target, 25);
            target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 12, 0));
        }

        LivingEntity caster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        float shock = (1.60F + potencyTier * 0.52F) * mult;
        SpellCombatReactionEngine.hurtWithStaticMateriaAdvantage(level, target, caster, shock, false, true);

        int arcs = 0;
        for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(4.0D), e -> e.isAlive() && e != target && e != caster)) {
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(adj, caster, true)) continue;
            SpellWorldVfxManager.broadcastChainArc(
                    level, target.getBoundingBox().getCenter(), adj.getBoundingBox().getCenter(),
                    EssenceType.STATIC, EssenceType.LIGHTNING, potencyTier
            );
            SpellCombatReactionEngine.hurtWithStaticMateriaAdvantage(level, adj, caster, shock * 0.55F, false, true);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, adj.getX(), adj.getY(0.8D), adj.getZ(), 5, 0.2D, 0.25D, 0.2D, 0.04D);
            if (++arcs >= 3) break;
        }
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY(0.85D), target.getZ(), 6, 0.25D, 0.3D, 0.25D, 0.04D);
        return true;
    }

    private static boolean hasMetalEquipment(LivingEntity entity) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack st = entity.getItemBySlot(slot);
            if (!st.isEmpty() && isMetallicItem(st)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isMetallicItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().toLowerCase(Locale.ROOT);
        return id.contains("iron") || id.contains("chainmail") || id.contains("gold")
                || id.contains("copper") || id.contains("netherite") || id.contains("steel")
                || id.contains("bronze") || id.contains("silver") || id.contains("tin")
                || id.contains("ingot") || id.contains("nugget");
    }

    private static BlockPos findGroundSurfaceNear(ServerLevel level, BlockPos origin) {
        for (int dy = 2; dy >= -6; dy--) {
            BlockPos check = origin.offset(0, dy, 0);
            BlockState ground = level.getBlockState(check);
            if (!ground.getCollisionShape(level, check).isEmpty() && level.getBlockState(check.above()).isAir()) {
                return check;
            }
        }
        return origin.below();
    }
}
