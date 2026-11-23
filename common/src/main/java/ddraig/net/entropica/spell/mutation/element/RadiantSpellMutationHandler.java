package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.entity.AutocastPedestalBlockEntity;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.network.SpellVfxEventPayload;
import ddraig.net.entropica.registry.ModDataComponents;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModEntityTags;
import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.registry.ModParticles;
import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.apparatus.CastingApparatusState;
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
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Complete per-part Signature Mutation implementation for <b>Radiant Materia (Base Type #7)</b>
 * (Spellcrafting System Guide Sections 1.2A, 2.2.1, 2.3, 4.1–4.9, 5.1–5.7 &amp; 6.8):
 * <ul>
 *     <li><b>Canonical Advantage</b>: Strong against <b>UNDEAD (+1)</b> | Weak against <b>UMBRAL (-1)</b>.</li>
 *     <li><b>Projectile</b>: <i>Homing Dawn Star</i> — zero-gravity floating orb of concentrated sunlight (#FFE278)
 *     that actively seeks out the lowest-health undead enemy within a spherical 16-block radius every tick and
 *     detonates on first impact in a holy smite (+1 vs Undead, -1 vs Umbral).</li>
 *     <li><b>Beam</b>: <i>Solar Thermal Lance</i> — continuous beam of focused daylight particles (using 3D emissive
 *     bloom rendering with zero chunk lightmap updates) that continuously burns and smites undead enemies (+1 vs
 *     Undead), deals 65% Radiant heat damage to living hostiles, strips Invisibility, melts all ice (including Blue Ice
 *     at Tier 1+) and snow blocks in its path, and ignites swooping flying Phantoms.</li>
 *     <li><b>Radial Burst</b>: <i>Supernova Flashbang</i> — detonates a circular 6-block+ 3D sphere of 16 golden
 *     sunburst rays, dealing full Radiant smite damage to undead (+1 advantage) and 55% Radiant damage to living
 *     hostiles, knocking foes backward, and inflicting 4.0 seconds of Blindness and Weakness on all hostile Enemy mobs.</li>
 *     <li><b>Touch / Strike</b>: <i>Holy Smite &amp; Daylight Ignition</i> — converts melee strike to pure Radiant
 *     Materia damage (stacking held weapon Materia Advantage) with a 9-tooth 3D solar crescent slash; strikes against
 *     Undead deal +50% weapon-scaled bonus smite damage and 7s radiant fire, disintegrating foes slain by the strike
 *     itself into glowing {@code Starlight Flakes} stardust.</li>
 *     <li><b>Aura / Self</b>: <i>Pacification Shroud</i> — registers defense as Radiant (+1 vs Undead -> 50% damage,
 *     -1 vs Umbral -> 150% damage), pacifies unprovoked neutral mobs, and runs every-tick 64m eye-contact pacification
 *     preventing Endermen from screaming or attacking when looked in the eyes (mobile or anchored sanctuary).</li>
 *     <li><b>Glyph / Trap</b>: <i>Sunburst Confinement Sigil</i> — 1.0s arming delay, 10m (or Tier 5+ indefinite)
 *     persistence, 50% Sneak-Right-Click disarm refund, and Repeating Sentry support; when tripped across its circular
 *     sensor, erupts a blinding 12-beam cage of radiant light trapping victims for 3.0 seconds with 3.0s Blindness,
 *     full Undead smite, and 45% living hostile Radiant damage.</li>
 *     <li><b>Cone / Spray</b>: <i>Cleansing Mist Cone</i> — exact 60-degree golden mist curtain that heals friendly
 *     allies, cleanses negative status effects and hexes from friendly/non-hostile targets in front (plus a 55% caster
 *     self-heal/cleanse wash), converts overheal into Vitality &amp; Saturation, and sears Undead caught in the cone.</li>
 *     <li><b>Rain / Skyfall</b>: <i>Daylight Megalith Pillar</i> — opens a cave-ceiling-adaptive 12-Ray Solar Gate
 *     and drops a tier-scaled 3D Celestial Quartz pseudoblock pillar after 2.0 seconds, crushing the impact zone,
 *     pulsing outward with 3 circular Holy Shockwaves, and materializing a permanent Celestial Quartz monument.</li>
 *     <li><b>Summon / Eidolon</b>: <i>Solar Seraph Suite</i> — solar gold/ivory (#FFE278) triad: airborne 6-winged
 *     Agent raining piercing light arrows, mirror-shield Sentry reflecting hostile projectiles in a 7.5m sphere, and
 *     damage-absorbing Decoy mitigating 45% of hit damage into circular 8m ally-healing &amp; cleansing pulses.</li>
 *     <li><b>Flow (Chains)</b>: <i>Prismatic Starlight Chains</i> — leaps along refracted prism-daylight beams,
 *     crowning each victim with a 5.0-second anti-stealth glowing halo that strips Invisibility and Umbral shrouds
 *     and reveals them through solid walls.</li>
 *     <li><b>Blink / Step</b>: <i>Luminous Cleanse</i> — leaves a blinding golden sunburst flash at departure
 *     (blinding all hostile Enemy mobs and PvP foes when Ward is off) and cleanses 1 negative status effect on arrival.</li>
 *     <li><b>Transmute &amp; Break</b>: <i>Holy Purification</i> — purifies Rotten Flesh into clean Leather (with up
 *     to 30% batch fuel discount), cures Zombie Villagers back into healthy Villagers, cleanses curses from dropped or
 *     equipped gear, purifies corrupted terrain/items, and powers Radiant-lineage Ore Sonar &amp; inventory Torch
 *     auto-placement while excavating.</li>
 *     <li><b>Affliction</b>: <i>Sun-Scorched Smite Hex</i> — persistent anti-milk holy branding curse melting
 *     4.0 / 6.0 / 8.0 physical armor points across Levels I–III, locking natural health regeneration, applying Glowing
 *     &amp; stripping stealth, and searing Undead &amp; living hostiles until cleansed via Restoration/Shadow magic or caster defeat.</li>
 * </ul>
 */
public final class RadiantSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int SOLAR_SERAPH_HEX_COLOR = 0xFFE278;
    public static final int IVORY_DAYLIGHT_HEX_COLOR = 0xFFFDF0;

    /** Exact cosine of the 30-degree half-angle for a canonical 60-degree Cleansing Mist Cone ({@code cos(30°) = 0.8660254D}). */
    public static final double CLEANSING_CONE_HALF_ANGLE_COS = Math.cos(Math.toRadians(30.0D));

    // Generous 3D world-space VFX durations so players can admire celestial & solar formations
    private static final int VFX_PRISM_CHAIN_TICKS = 58;
    private static final int VFX_DAWN_STAR_IMPACT_TICKS = 60;
    private static final int VFX_SUPERNOVA_TICKS = 68;
    private static final int VFX_SUPERNOVA_SEAL_TICKS = 64;
    private static final int VFX_HOLY_SMITE_SLASH_TICKS = 54;
    private static final int VFX_CONFINEMENT_CAGE_TICKS = 64;
    private static final int VFX_CLEANSING_MIST_TICKS = 58;
    private static final int VFX_MEGALITH_TOTAL_TICKS = 108;
    private static final int VFX_SUMMON_PILLAR_TICKS = 60;
    private static final int VFX_LUMINOUS_BLINK_TICKS = 56;

    @Override
    public EssenceType materiaType() {
        return EssenceType.RADIANT;
    }

    // =========================================================================
    // CANONICAL SIGNATURE TITLES & GUIDE DESCRIPTIONS (SECTION 6.8)
    // =========================================================================

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Prismatic Starlight Chains";
            default -> "Solar Starlight " + flow.getDisplayName();
        };
    }

    @Override
    public @Nullable String resolveFlowDescription(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Prismatic Starlight Chains: Leaping chains jump between targets along blinding beams of refracted daylight, illuminating each victim with a glowing halo that prevents stealth/invisibility and reveals them through walls for 5 seconds.";
            default -> null;
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Homing Dawn Star";
            case BEAM -> "Solar Thermal Lance";
            case RADIAL_BURST -> "Supernova Flashbang";
            case TOUCH_STRIKE -> "Holy Smite & Daylight Ignition";
            case AURA -> "Pacification Shroud";
            case GLYPH -> "Sunburst Confinement Sigil";
            case CONE -> "Cleansing Mist Cone";
            case SKYFALL -> "Daylight Megalith Pillar";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public @Nullable String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Homing Dawn Star: Floating orb of concentrated sunlight (#FFE278) that seeks out the lowest-health undead enemy within a spherical 16-block radius and detonates on first impact in a holy smite (+1 vs Undead, -1 vs Umbral).";
            case BEAM -> "Solar Thermal Lance: Continuous beam of focused daylight (bloom-rendered without chunk light updates) that smites undead (+1 vs Undead), deals 65% Radiant heat damage to living hostiles, strips Invisibility, melts all ice including Blue Ice at Tier 1+, and ignites flying Phantoms.";
            case RADIAL_BURST -> "Supernova Flashbang: Detonates a circular 6-block+ sphere of golden sunburst rays, dealing full Radiant smite damage to undead (+1 advantage) and 55% Radiant damage to living hostiles, knocking foes back, and inflicting 4s Blindness & Weakness on all hostile Enemy mobs.";
            case TOUCH_STRIKE -> "Holy Smite & Daylight Ignition: Converts melee strike to Radiant Materia damage with weapon synergy; strikes vs Undead deal +50% weapon-scaled bonus smite damage and 7s radiant fire, disintegrating targets slain by the strike into glowing Starlight Flakes.";
            case AURA -> "Pacification Shroud: Registers defense as Radiant (0.50x incoming Undead damage, 1.50x incoming Umbral damage); pacifies neutral mobs and prevents Endermen from attacking when looked at in the eyes.";
            case GLYPH -> "Sunburst Confinement Sigil: Erupts in a blinding 12-beam cage of radiant light when tripped, trapping victims for 3.0s while inflicting 3.0s Blindness, full Radiant smite damage to undead, and 45% Radiant damage to living hostiles.";
            case CONE -> "Cleansing Mist Cone: Exact 60-degree golden spray that heals friendly allies, cleanses negative status effects and hexes from friendly targets in front (plus a 55% caster wash), and sears undead.";
            case SKYFALL -> "Daylight Megalith Pillar: Summons a tier-scaled 3D Celestial Quartz pseudoblock pillar that crashes down after 2.0s with touchdown Blindness/Slowness and 3 circular holy shockwaves, leaving a permanent Celestial Quartz monument.";
            case SUMMON_AGENT -> "Solar Seraph (Agent): Solar gold/white (#FFE278) winged seraph that flies airborne and rains piercing golden light arrows down on foes.";
            case SUMMON_SENTRY -> "Solar Seraph (Sentry): Anchored solar pylon (#FFE278) orbiting 4 golden mirror-shields that reflect hostile projectiles in a 7.5m sphere back at attackers.";
            case SUMMON_DECOY -> "Solar Seraph (Decoy): Radiant winged effigy (#FFE278) that absorbs 45% of incoming hit damage and converts it into circular 8m golden healing & cleansing pulses for allies.";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Luminous Cleanse";
            case TRANSMUTE, BREAK -> "Holy Purification";
            case AFFLICTION -> "Sun-Scorched Smite Hex";
            default -> "Solar " + effect.getDisplayName();
        };
    }

    @Override
    public @Nullable String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Luminous Cleanse: Teleport leaves a blinding flash of light at departure for hostile Enemy mobs and PvP foes, and cleanses 1 negative status effect upon arrival.";
            case TRANSMUTE, BREAK -> "Holy Purification: Purifies rotten flesh into clean leather, cures zombie villagers back to human form, cleanses curses from equipment, consecrates corrupted blocks/items, and powers Radiant ore sonar & inventory torch placement.";
            case AFFLICTION -> "Sun-Scorched Smite Hex: Persistent holy branding curse melting 4 / 6 / 8 armor points across Levels I-III, locking natural health regeneration, applying Glowing & stripping stealth, and dealing continuous Radiant smite damage until cleansed (milk-immune).";
            default -> null;
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATION — PRISMATIC STARLIGHT CHAINS (CHAIN_LEAP)
    // =========================================================================

    @Override
    public void onChainLeapStep(
            ServerLevel level,
            LivingEntity caster,
            LivingEntity fromTarget,
            LivingEntity toTarget,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        int tier = spell.getEffectiveTier();
        long haloUntil = level.getGameTime() + 100L; // Exact 5.0 seconds (100 ticks)!
        applyAntiStealthHalo(level, fromTarget, haloUntil, tier);
        applyAntiStealthHalo(level, toTarget, haloUntil, tier);

        Vec3 p1 = fromTarget.position().add(0.0D, fromTarget.getBbHeight() * 0.58D, 0.0D);
        Vec3 p2 = toTarget.position().add(0.0D, toTarget.getBbHeight() * 0.58D, 0.0D);

        // Generous 58-tick 3D refracted prism-daylight beam & target halos!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_CHAIN_ARC,
                p1, p2, EssenceType.RADIANT, null,
                1.05F, VFX_PRISM_CHAIN_TICKS, tier
        );

        double dist = p1.distanceTo(p2);
        if (dist > 0.1D) {
            Vec3 step = p2.subtract(p1).normalize();
            for (double d = 0.0D; d <= dist; d += 0.55D) {
                Vec3 pt = p1.add(step.scale(d));
                level.sendParticles(
                        TimedTintableParticleOption.linear(ModParticles.TINTABLE_SUNBEAM.get(), SOLAR_SERAPH_HEX_COLOR, 28, 0.92F, 0.05F),
                        pt.x, pt.y, pt.z, 1, 0.03D, 0.03D, 0.03D, 0.005D
                );
            }
        }
        level.playSound(null, toTarget.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.90F, 1.52F);
        level.playSound(null, toTarget.blockPosition(), SoundEvents.CHISELED_BOOKSHELF_INSERT_ENCHANTED, SoundSource.PLAYERS, 0.65F, 1.35F);
    }

    private static void applyAntiStealthHalo(ServerLevel level, LivingEntity victim, long haloUntil, int tier) {
        CompoundTag tag = EntityHelper.getPersistentData(victim);
        tag.putLong(SpellLingeringZoneManager.TAG_RADIANT_HALO_REVEAL_UNTIL,
                Math.max(haloUntil, tag.getLong(SpellLingeringZoneManager.TAG_RADIANT_HALO_REVEAL_UNTIL).orElse(0L)));
        tag.remove(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL);
        victim.removeEffect(MobEffects.INVISIBILITY);
        victim.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0, false, true));

        Vec3 crown = victim.position().add(0.0D, victim.getBbHeight() + 0.32D, 0.0D);
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                crown, new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.RADIANT, null, 0.72F, 100, tier
        );
        level.sendParticles(ParticleTypes.END_ROD, crown.x, crown.y, crown.z,
                8, 0.28D, 0.08D, 0.28D, 0.02D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.RUNE_SOWILO.get(), IVORY_DAYLIGHT_HEX_COLOR, 30, 1.0F, 0.04F),
                crown.x, crown.y + 0.08D, crown.z, 3, 0.20D, 0.06D, 0.20D, 0.01D
        );
    }

    // =========================================================================
    // FORM MUTATION 1 — HOMING DAWN STAR (PROJECTILE)
    // =========================================================================

    @Override
    public void onProjectileSpawnedFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Homing Dawn Star: floating zero-gravity orb of concentrated sunlight!
        projectile.setNoGravity(true);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        projectile.setNoGravity(true);
        if (projectile.tickCount % 2 == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SUNBEAM.get(), SOLAR_SERAPH_HEX_COLOR, 24, 0.90F, 0.05F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D
            );
            level.sendParticles(ParticleTypes.END_ROD,
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.04D, 0.04D, 0.04D, 0.008D);
        }

        // Every-tick homing onto the lowest-health undead enemy within an exact spherical <= 16.0-block radius (distanceToSqr <= 256.0D)!
        Entity owner = projectile.getOwner();
        LivingEntity livingOwner = owner instanceof LivingEntity l ? l : null;
        List<LivingEntity> undead = level.getEntitiesOfClass(
                LivingEntity.class,
                projectile.getBoundingBox().inflate(16.0D),
                e -> e.isAlive()
                        && e != owner
                        && e.distanceToSqr(projectile) <= 256.0D
                        && isUndeadOrPhantomTarget(level, e)
                        && (livingOwner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, livingOwner, true))
        );
        undead.stream()
                .min(Comparator.comparingDouble(LivingEntity::getHealth)
                        .thenComparingDouble(e -> e.distanceToSqr(projectile)))
                .ifPresent(target -> {
                    Vec3 cur = projectile.getDeltaMovement();
                    double spd = Math.max(1.02D, cur.length());
                    Vec3 aimPoint = target.position().add(0.0D, target.getBbHeight() * 0.58D, 0.0D);
                    Vec3 toTarget = aimPoint.subtract(projectile.position());
                    if (toTarget.lengthSqr() > 1.0E-4D) {
                        // Sharp 90°+ banking pursuit (Section 4.1) so close/fast undead never dodge the Dawn Star!
                        Vec3 desired = toTarget.normalize().scale(spd);
                        Vec3 steered = cur.scale(0.58D).add(desired.scale(0.42D));
                        if (steered.lengthSqr() > 1.0E-4D) {
                            projectile.setDeltaMovement(steered.normalize().scale(spd));
                            projectile.hasImpulse = true;
                        }
                    }
                });
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
        Vec3 impactPos = hitEntity.position().add(0.0D, hitEntity.getBbHeight() * 0.52D, 0.0D);
        detonateHomingDawnStar(level, caster, impactPos, hitEntity, spell, slot);
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
        Vec3 impactPos = blockHit.getLocation();
        detonateHomingDawnStar(level, caster, impactPos, null, spell, slot);
    }

    @Override
    public void onProjectileExpiredFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Detonate cleanly on Timed Fuse / On-Proximity / Outer-Rim max travel distance stops (0m..128m)!
        detonateHomingDawnStar(level, caster, projectile.position(), null, spell, slot);
    }

    private void detonateHomingDawnStar(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 impactPos,
            @Nullable Entity directHit,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        int tier = spell.getEffectiveTier();
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                impactPos, new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.RADIANT, null, 2.15F, VFX_DAWN_STAR_IMPACT_TICKS, tier
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SUNBEAM.get(), SOLAR_SERAPH_HEX_COLOR, 36, 1.15F, 0.08F),
                impactPos.x, impactPos.y + 0.15D, impactPos.z, 24, 0.45D, 0.45D, 0.45D, 0.08D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.RUNE_SOWILO.get(), IVORY_DAYLIGHT_HEX_COLOR, 30, 1.10F, 0.05F),
                impactPos.x, impactPos.y + 0.25D, impactPos.z, 5, 0.35D, 0.35D, 0.35D, 0.02D
        );
        level.sendParticles(ParticleTypes.END_ROD, impactPos.x, impactPos.y + 0.15D, impactPos.z,
                22, 0.38D, 0.40D, 0.38D, 0.12D);
        level.playSound(null, BlockPos.containing(impactPos), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.05F, 1.42F);

        if (spell.prefix().enforcesZeroEntityHarm()) {
            return;
        }

        double blastRadius = 2.6D;
        double blastRadiusSq = blastRadius * blastRadius;
        boolean hasHarm = hasHarmEffect(spell, slot);
        float flawPot = spell.flawProfile().getPotencyMultiplier();
        float smiteBase = (4.8F + tier * 1.40F) * flawPot;

        AABB blastBox = new AABB(
                impactPos.x - blastRadius, impactPos.y - 1.8D, impactPos.z - blastRadius,
                impactPos.x + blastRadius, impactPos.y + 2.3D, impactPos.z + blastRadius
        );
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, blastBox, LivingEntity::isAlive)) {
            if (victim == caster) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                    victim, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            double distSq = victim.position().distanceToSqr(impactPos);
            if (victim != directHit && distSq > blastRadiusSq) continue;
            double dist = Math.sqrt(distSq);

            // Section 5.1 Linear Distance Falloff: 100% epicenter -> 33% perimeter!
            float falloff = (victim == directHit)
                    ? 1.0F
                    : (float) Mth.clamp(1.0D - (dist / blastRadius) * 0.67D, 0.33D, 1.0D);

            victim.removeEffect(MobEffects.INVISIBILITY);
            if (isUndeadOrPhantomTarget(level, victim)) {
                victim.igniteForSeconds(5);
            }

            if (!hasHarm) {
                SpellCombatReactionEngine.hurtWithRadiantMateriaAdvantage(
                        level, victim, caster, smiteBase * falloff, false, true
                );
            }
            if (victim != directHit) {
                SpellExecutionEngine.deliverSubSpellOnEntity(
                        level, caster, victim, victim.position().subtract(impactPos),
                        spell, slot, EssenceType.RADIANT, falloff * 0.65F
                );
            }
        }
    }

    // =========================================================================
    // FORM MUTATION 2 — SOLAR THERMAL LANCE (BEAM)
    // =========================================================================

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
        // Solar Thermal Lance: continuous beam of focused daylight particles (bloom-rendered ONLY, zero chunk lightmap updates)
        // that smites undead (+1 vs Undead), deals 65% Radiant burn damage to living hostiles, strips Invisibility,
        // melts all ice (including Blue Ice at Tier 1+) & snow blocks in its path, and ignites flying Phantoms!
        int tier = spell.getEffectiveTier();
        Vec3 eye = caster.getEyePosition();
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        double hitDist = (blockHit != null && blockHit.getType() == HitResult.Type.BLOCK)
                ? eye.distanceTo(blockHit.getLocation()) + 0.85D
                : maxReach;
        double scanDist = Math.min(maxReach, Math.max(2.0D, hitDist));
        Vec3 scanEnd = eye.add(normDir.scale(scanDist));

        if (!spell.prefix().enforcesZeroEntityHarm()) {
            boolean hasHarm = hasHarmEffect(spell, slot);
            boolean safeWard = spell.hasWardFriendlyFireSafeguard();
            float flawPot = spell.flawProfile().getPotencyMultiplier();
            float lanceSmite = (2.6F + tier * 0.98F) * Math.max(0.5F, apparatusPotency) * flawPot;
            Set<UUID> smited = new HashSet<>();

            for (LivingEntity hit : hitEntities) {
                if (hit == caster) continue;
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, safeWard)) {
                    continue;
                }
                smited.add(hit.getUUID());
                if (isUndeadOrPhantomTarget(level, hit)) {
                    hit.removeEffect(MobEffects.INVISIBILITY);
                    hit.igniteForSeconds(6);
                    if (!hasHarm) {
                        SpellCombatReactionEngine.hurtWithRadiantMateriaAdvantage(level, hit, caster, lanceSmite, false, true);
                    }
                } else if (!isPassiveOrFriendlyEntity(hit, caster)) {
                    hit.removeEffect(MobEffects.INVISIBILITY);
                    if (!hasHarm) {
                        SpellCombatReactionEngine.hurtWithRadiantMateriaAdvantage(level, hit, caster, lanceSmite * 0.65F, false, false);
                    }
                }
            }

            // Wide 1.45m solar thermal corona sweep specifically catching fast-swooping airborne Phantoms along the ray!
            AABB phantomCorridor = new AABB(eye, scanEnd).inflate(1.45D);
            for (Phantom phantom : level.getEntitiesOfClass(Phantom.class, phantomCorridor, LivingEntity::isAlive)) {
                if (smited.contains(phantom.getUUID())) continue;
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(phantom, caster, safeWard)) {
                    continue;
                }
                if (distanceToRaySegment(phantom.getBoundingBox().getCenter(), eye, normDir, scanDist) <= 1.45D) {
                    phantom.removeEffect(MobEffects.INVISIBILITY);
                    phantom.igniteForSeconds(6);
                    if (hasHarm) {
                        SpellExecutionEngine.deliverSubSpellOnEntity(
                                level, caster, phantom, normDir, spell, slot, fallbackMateria, apparatusPotency
                        );
                    } else {
                        SpellCombatReactionEngine.hurtWithRadiantMateriaAdvantage(level, phantom, caster, lanceSmite, false, true);
                    }
                    level.sendParticles(ParticleTypes.END_ROD, phantom.getX(), phantom.getY(0.5D), phantom.getZ(),
                            8, 0.30D, 0.25D, 0.30D, 0.05D);
                }
            }
        }

        // Melt all ice types (Ice, Frosted Ice, Packed Ice, AND Blue Ice at ALL tiers Tier 1+) -> Water,
        // plus melting Snow / Snow Block / Powder Snow along the beam path!
        Set<BlockPos> visited = new HashSet<>();
        boolean meltedAny = false;
        boolean ultraWarm = level.dimensionType().ultraWarm();
        for (double d = 0.75D; d <= scanDist; d += 0.65D) {
            Vec3 pt = eye.add(normDir.scale(d));
            BlockPos center = BlockPos.containing(pt);
            for (BlockPos p : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
                if (p.distToCenterSqr(pt) > 1.45D || !visited.add(p.immutable())) continue;
                BlockState bs = level.getBlockState(p);
                if (bs.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
                if (bs.is(Blocks.ICE) || bs.is(Blocks.FROSTED_ICE) || bs.is(Blocks.PACKED_ICE) || bs.is(Blocks.BLUE_ICE)) {
                    if (ultraWarm) {
                        level.removeBlock(p, false);
                    } else {
                        level.setBlockAndUpdate(p, Blocks.WATER.defaultBlockState());
                    }
                    level.sendParticles(ParticleTypes.CLOUD, p.getX() + 0.5D, p.getY() + 0.6D, p.getZ() + 0.5D,
                            4, 0.2D, 0.2D, 0.2D, 0.02D);
                    meltedAny = true;
                } else if (bs.is(Blocks.SNOW) || bs.is(Blocks.SNOW_BLOCK) || bs.is(Blocks.POWDER_SNOW)) {
                    level.removeBlock(p, false);
                    level.sendParticles(ParticleTypes.CLOUD, p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D,
                            3, 0.2D, 0.15D, 0.2D, 0.02D);
                    meltedAny = true;
                }
            }
        }
        if (meltedAny) {
            level.playSound(null, caster.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.35F, 1.42F);
        }
    }

    // =========================================================================
    // FORM MUTATION 3 — SUPERNOVA FLASHBANG (RADIAL_BURST)
    // =========================================================================

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
        int tier = spell.getEffectiveTier();
        double flashRadius = Math.max(6.0D, radius); // Canonical circular 6-block+ minimum radius!

        // Generous 68-tick 3D 16-ray golden Supernova Flashbang sphere & 12-ray solar compass floor seal
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_RADIAL_NOVA,
                center, new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.RADIANT, null, (float) flashRadius, VFX_SUPERNOVA_TICKS, tier
        );
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                center.add(0.0D, 0.04D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.RADIANT, null, (float) (flashRadius * 0.94D), VFX_SUPERNOVA_SEAL_TICKS, tier
        );

        if (!spell.prefix().enforcesZeroEntityHarm()) {
            AABB box = new AABB(
                    center.x - flashRadius, center.y - 2.5D, center.z - flashRadius,
                    center.x + flashRadius, center.y + 3.5D, center.z + flashRadius
            );
            boolean hasHarm = hasHarmEffect(spell, slot);
            boolean safeWard = spell.hasWardFriendlyFireSafeguard();
            float flawPot = spell.flawProfile().getPotencyMultiplier();
            float undeadSmiteBase = (5.5F + tier * 1.95F) * Math.max(0.5F, apparatusPotency) * flawPot;
            double flashRadiusSq = flashRadius * flashRadius;

            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                if (entity == caster) continue;
                double dx = entity.getX() - center.x;
                double dz = entity.getZ() - center.z;
                double distSq = dx * dx + dz * dz;
                if (distSq > flashRadiusSq) continue;
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(entity, caster, safeWard)) {
                    continue;
                }

                double dist = Math.sqrt(distSq);
                boolean undead = isUndeadOrPhantomTarget(level, entity);
                boolean hostileEnemy = entity instanceof Enemy || undead;
                boolean livingHostile = !undead && (hostileEnemy || !isPassiveOrFriendlyEntity(entity, caster));
                float distFactor = (float) Mth.clamp(1.0D - (dist / Math.max(1.0D, flashRadius)) * 0.67D, 0.33D, 1.0D);

                // Inflict 4.0 seconds (80 ticks) of Blindness & Weakness on all hostile Enemy mobs (including Slimes, Magma Cubes, Ghasts, Shulkers, Hoglins)!
                if (hostileEnemy || livingHostile) {
                    if (hostileEnemy) {
                        entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                        entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
                    } else if (entity instanceof Player) {
                        entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                        entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
                    }
                    entity.removeEffect(MobEffects.INVISIBILITY);
                    EntityHelper.getPersistentData(entity).remove(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL);
                }

                if (undead) {
                    entity.igniteForSeconds(5);
                    if (!hasHarm) {
                        SpellCombatReactionEngine.hurtWithRadiantMateriaAdvantage(
                                level, entity, caster, undeadSmiteBase * distFactor, false, true
                        );
                    }
                } else if (livingHostile && !hasHarm) {
                    SpellCombatReactionEngine.hurtWithRadiantMateriaAdvantage(
                            level, entity, caster, undeadSmiteBase * 0.55F * distFactor, false, true
                    );
                }

                if ((undead || livingHostile) && dist > radius) {
                    SpellExecutionEngine.deliverSubSpellOnEntity(
                            level, caster, entity, direction, spell, slot, fallbackMateria, apparatusPotency * distFactor
                    );
                }

                if ((undead || livingHostile) && !SpellCombatReactionEngine.isBossEntity(level, entity)) {
                    Vec3 delta = entity.position().subtract(center);
                    Vec3 horiz = new Vec3(delta.x, 0.0D, delta.z);
                    Vec3 away = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
                    SpellExecutionEngine.applySyncedMotion(entity, new Vec3(away.x * 1.18D, 0.36D, away.z * 1.18D));
                }
            }
        }

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SUNBEAM.get(), SOLAR_SERAPH_HEX_COLOR, 48, 1.28F, 0.10F),
                center.x, center.y + 1.0D, center.z, 52, flashRadius * 0.44D, 0.85D, flashRadius * 0.44D, 0.12D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.RUNE_SOWILO.get(), IVORY_DAYLIGHT_HEX_COLOR, 38, 1.20F, 0.06F),
                center.x, center.y + 1.15D, center.z, 10, flashRadius * 0.38D, 0.65D, flashRadius * 0.38D, 0.03D
        );
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 1.0D, center.z,
                46, flashRadius * 0.42D, 0.85D, flashRadius * 0.42D, 0.16D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.15F, 1.45F);
        level.playSound(null, BlockPos.containing(center), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.25F, 1.35F);
    }

    // =========================================================================
    // FORM MUTATION 4 — HOLY SMITE & DAYLIGHT IGNITION (TOUCH_STRIKE)
    // =========================================================================

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
        long now = level.getGameTime();
        int tier = spell.getEffectiveTier();
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 slashOrigin = caster.getEyePosition().subtract(0.0D, 0.28D, 0.0D).add(normDir.scale(0.95D));

        // Generous 54-tick 9-tooth 3D golden-white solar crescent slash!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_CRESCENT_SLASH,
                slashOrigin, normDir, EssenceType.RADIANT, null,
                2.65F, VFX_HOLY_SMITE_SLASH_TICKS, tier
        );

        if (spell.prefix().enforcesZeroEntityHarm()) {
            for (LivingEntity hit : hitEntities) {
                EntityHelper.getPersistentData(hit).remove(SpellLingeringZoneManager.TAG_RADIANT_PURIFY_DROPS_UNTIL);
            }
            return;
        }

        boolean hasHarm = hasHarmEffect(spell, slot);
        boolean safeWard = spell.hasWardFriendlyFireSafeguard();
        float flawPot = spell.flawProfile().getPotencyMultiplier();
        float weaponDamage = resolveCasterMeleeAttackDamage(caster);

        for (int idx = 0; idx < hitEntities.size(); idx++) {
            LivingEntity hit = hitEntities.get(idx);
            CompoundTag data = EntityHelper.getPersistentData(hit);
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, safeWard)) {
                data.remove(SpellLingeringZoneManager.TAG_RADIANT_PURIFY_DROPS_UNTIL);
                continue;
            }
            boolean isPrimary = (idx == 0);
            float splashScale = isPrimary ? 1.0F : 0.50F; // Section 4.4: 100% primary, 50% sweeping cleave!

            // Stamp stardust drop purification strictly for the immediate strike resolution (never a lingering 2s window)!
            data.putLong(SpellLingeringZoneManager.TAG_RADIANT_PURIFY_DROPS_UNTIL, now + 2L);
            hit.removeEffect(MobEffects.INVISIBILITY);

            boolean undead = isUndeadOrPhantomTarget(level, hit);
            if (undead) {
                Vec3 hitMid = hit.position().add(0.0D, hit.getBbHeight() * 0.55D, 0.0D);
                SpellWorldVfxManager.broadcast(
                        level, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                        hitMid, new Vec3(0.0D, 1.0D, 0.0D),
                        EssenceType.RADIANT, null, isPrimary ? 1.90F : 1.35F, 52, tier
                );
                if (hit.isAlive()) {
                    // Unified 7-second radiant fire ignition across both primary and cleave Undead targets,
                    // plus +50% bonus smite scaled directly from the actual converted melee weapon + spell strike damage!
                    hit.igniteForSeconds(7);
                    float undeadBonusBurst = resolveHolySmiteUndeadBonusDamage(
                            weaponDamage, hasHarm, tier, apparatusPotency, flawPot, splashScale
                    );
                    SpellCombatReactionEngine.hurtWithRadiantMateriaAdvantage(
                            level, hit, caster, undeadBonusBurst, true, true
                    );
                }
                level.sendParticles(
                        TimedTintableParticleOption.linear(ModParticles.TINTABLE_SUNBEAM.get(), SOLAR_SERAPH_HEX_COLOR, 36, 1.15F, 0.08F),
                        hit.getX(), hit.getY(0.7D), hit.getZ(), isPrimary ? 24 : 12, 0.35D, 0.45D, 0.35D, 0.08D
                );
                level.sendParticles(ParticleTypes.END_ROD, hit.getX(), hit.getY(0.7D), hit.getZ(),
                        isPrimary ? 20 : 10, 0.35D, 0.45D, 0.35D, 0.11D);
                level.playSound(null, hit.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.05F, 1.38F);
            }

            // Disintegrate slain foes into Starlight Flakes strictly when the strike itself lands the killing blow!
            if (!hit.isAlive() || hit.isDeadOrDying()) {
                purifyRottingDropsToStardust(level, hit.getBoundingBox().inflate(3.5D), hit.position());
            } else if (!isPrimary || !(caster instanceof Player)) {
                data.remove(SpellLingeringZoneManager.TAG_RADIANT_PURIFY_DROPS_UNTIL);
            } else {
                // Allow SpellCombatReactionEngine's immediate same-tick melee weapon conversion to resolve, then clear if still alive!
                final LivingEntity trackedTarget = hit;
                SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                        caster, trackedTarget.position(), 0.5D, 1, now, null, null, EssenceType.RADIANT, 1.0F,
                        (srv, z, el) -> {
                            if (el >= 1) {
                                if (trackedTarget.isAlive() && !trackedTarget.isDeadOrDying()) {
                                    EntityHelper.getPersistentData(trackedTarget).remove(SpellLingeringZoneManager.TAG_RADIANT_PURIFY_DROPS_UNTIL);
                                }
                                return false;
                            }
                            return true;
                        }
                ));
            }
        }
    }

    /**
     * Resolves the caster's effective melee weapon + attribute attack damage ({@link Attributes#ATTACK_DAMAGE}).
     */
    public static float resolveCasterMeleeAttackDamage(@Nullable LivingEntity caster) {
        if (caster == null) {
            return 1.0F;
        }
        AttributeInstance atkInst = caster.getAttribute(Attributes.ATTACK_DAMAGE);
        return atkInst != null ? (float) Math.max(1.0D, atkInst.getValue()) : 1.0F;
    }

    /**
     * Computes the canonical {@code +50%} Undead bonus smite base burst scaled directly from the actual converted
     * melee weapon + slotted spell strike damage (before Radiant + weapon Materia Advantage is applied identically).
     */
    public static float resolveHolySmiteUndeadBonusDamage(
            float meleeWeaponDamage,
            boolean hasHarmEffect,
            int potencyTier,
            float apparatusPotency,
            float flawPotencyMult,
            float splashScale
    ) {
        float weaponPart = Math.max(1.0F, meleeWeaponDamage);
        float spellPart = hasHarmEffect
                ? (MateriaStageTable.getBaseCombatDamageHp(Math.clamp(potencyTier, 1, 10))
                * Math.max(0.5F, apparatusPotency)
                * flawPotencyMult)
                : 0.0F;
        return 0.50F * (weaponPart + spellPart) * splashScale;
    }

    /**
     * Converts any nearby rotting / tainted drops ({@code ROTTEN_FLESH}, {@code POISONOUS_POTATO}, {@code SPIDER_EYE})
     * from a Holy Smite lethal kill into glowing {@link ModItems#STARLIGHT_FLAKES} stardust!
     *
     * @return number of item entities purified into glowing stardust.
     */
    public static int purifyRottingDropsToStardust(ServerLevel level, AABB searchBox, Vec3 fallbackCenter) {
        int converted = 0;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, searchBox, Entity::isAlive)) {
            ItemStack stack = item.getItem();
            if (stack.is(Items.ROTTEN_FLESH) || stack.is(Items.POISONOUS_POTATO) || stack.is(Items.SPIDER_EYE)) {
                int count = Math.max(1, stack.getCount());
                item.setItem(new ItemStack(ModItems.STARLIGHT_FLAKES.get(), count));
                item.setGlowingTag(true);
                converted++;
                level.sendParticles(ParticleTypes.END_ROD, item.getX(), item.getY(0.3D), item.getZ(),
                        8, 0.22D, 0.22D, 0.22D, 0.06D);
            }
        }
        return converted;
    }

    /**
     * Spawns 1–2 glowing {@link ModItems#STARLIGHT_FLAKES} when a slain target had no rotting meat drops to convert,
     * ensuring every lethal Holy Smite blow visibly disintegrates into glowing stardust!
     */
    public static void spawnGuaranteedStardustBurst(ServerLevel level, Vec3 pos) {
        int count = 1 + level.random.nextInt(2);
        ItemEntity drop = new ItemEntity(level, pos.x, pos.y, pos.z, new ItemStack(ModItems.STARLIGHT_FLAKES.get(), count));
        drop.setGlowingTag(true);
        drop.setDefaultPickUpDelay();
        level.addFreshEntity(drop);
        level.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y + 0.2D, pos.z, 12, 0.28D, 0.28D, 0.28D, 0.07D);
    }

    // =========================================================================
    // FORM MUTATION 5 — PACIFICATION SHROUD (AURA)
    // =========================================================================

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
        int tier = spell.getEffectiveTier();
        int dur = 240 + tier * 50;
        long until = level.getGameTime() + dur;

        CompoundTag data = EntityHelper.getPersistentData(caster);
        data.putLong(SpellLingeringZoneManager.TAG_RADIANT_SHROUD_UNTIL,
                Math.max(until, data.getLong(SpellLingeringZoneManager.TAG_RADIANT_SHROUD_UNTIL).orElse(0L)));
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.65F, null);

        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                caster.position().add(0.0D, 0.04D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.RADIANT, null, (float) Math.max(1.8D, Math.min(3.4D, radius)), 60, tier
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SUNBEAM.get(), SOLAR_SERAPH_HEX_COLOR, 42, 1.10F, 0.06F),
                caster.getX(), caster.getY(0.95D), caster.getZ(), 24, 0.45D, 0.55D, 0.45D, 0.04D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.RUNE_SOWILO.get(), IVORY_DAYLIGHT_HEX_COLOR, 36, 1.08F, 0.05F),
                caster.getX(), caster.getY(1.05D), caster.getZ(), 8, 0.40D, 0.45D, 0.40D, 0.02D
        );
        level.sendParticles(ParticleTypes.END_ROD, caster.getX(), caster.getY(0.95D), caster.getZ(),
                22, 0.42D, 0.52D, 0.42D, 0.05D);
        level.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.32F);
        level.playSound(null, caster.blockPosition(), SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.70F, 1.45F);
    }

    // =========================================================================
    // FORM MUTATION 6 — SUNBURST CONFINEMENT SIGIL (GLYPH)
    // =========================================================================

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
        int tier = spell.getEffectiveTier();
        double sigilRadius = Math.max(2.4D, Math.min(3.8D, radius));
        BlockPos surfaceCenter = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 anchoredCenter = new Vec3(surfaceCenter.getX() + 0.5D, surfaceCenter.getY() + 1.02D, surfaceCenter.getZ() + 0.5D);

        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                anchoredCenter, new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.RADIANT, null, (float) sigilRadius, 220, tier
        );
        level.playSound(null, surfaceCenter, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.90F, 1.42F);

        final boolean repeatingSentry = "repeating".equalsIgnoreCase(slot.formPart().subMode())
                || "sentry".equalsIgnoreCase(slot.formPart().subMode());
        final int maxCharges = repeatingSentry ? 3 : 1;
        final float chargePotencyScale = repeatingSentry ? 0.60F : 1.0F;
        int glyphLifetime = tier >= 5 ? (Integer.MAX_VALUE / 4) : 12_000;
        final int[] chargesUsed = {0};
        final int[] rearmUntil = {20}; // Section 4.6: 1.0-second (20-tick) arming delay!

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, anchoredCenter, sigilRadius, glyphLifetime, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed > 0 && elapsed % 180 == 0) {
                        SpellWorldVfxManager.broadcast(
                                srv, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                                zone.center, new Vec3(0.0D, 1.0D, 0.0D),
                                EssenceType.RADIANT, null, (float) zone.radius, 190, tier
                        );
                    }
                    if (elapsed < rearmUntil[0]) return true;
                    if (elapsed % 4 != 0) return true;

                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                    double triggerRadius = zone.radius;
                    double triggerRadSq = triggerRadius * triggerRadius;
                    AABB triggerBox = new AABB(
                            zone.center.x - triggerRadius, zone.center.y - 0.85D, zone.center.z - triggerRadius,
                            zone.center.x + triggerRadius, zone.center.y + 2.35D, zone.center.z + triggerRadius
                    );
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e -> {
                        if (!e.isAlive() || e == owner) return false;
                        double dx = e.getX() - zone.center.x;
                        double dz = e.getZ() - zone.center.z;
                        if (dx * dx + dz * dz > triggerRadSq) return false;
                        if (e instanceof SpellEidolonEntity eid && owner != null && (eid.isOwnedBy(owner) || eid.isFriendlyTo(owner))) {
                            return false;
                        }
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe)) {
                            return false;
                        }
                        return isUndeadOrPhantomTarget(srv, e) || !isPassiveOrFriendlyEntity(e, owner);
                    });
                    if (stepped.isEmpty()) return true;

                    double cageRadius = Math.max(2.40D, zone.radius);
                    SpellWorldVfxManager.broadcastWardShell(srv, zone.center, cageRadius, EssenceType.RADIANT, null, VFX_CONFINEMENT_CAGE_TICKS, tier);
                    SpellWorldVfxManager.broadcast(
                            srv, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                            zone.center, new Vec3(0.0D, 1.0D, 0.0D),
                            EssenceType.RADIANT, null, (float) (cageRadius * 1.15D), VFX_CONFINEMENT_CAGE_TICKS, tier
                    );
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.1F, 1.48F);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 1.32F);
                    srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 1.2D, zone.center.z,
                            38, 1.1D, 1.35D, 1.1D, 0.08D);

                    boolean zeroHarm = zone.spell != null && zone.spell.prefix().enforcesZeroEntityHarm();
                    boolean hasHarm = hasHarmEffect(zone.spell, zone.slot);
                    float flawPot = zone.spell != null ? zone.spell.flawProfile().getPotencyMultiplier() : 1.0F;
                    Set<UUID> cagedVictims = new HashSet<>();
                    float effPot = zone.apparatusPotency * chargePotencyScale;
                    float initialUndeadSmite = (6.5F + tier * 2.10F) * Math.max(0.5F, effPot) * flawPot;
                    long cageEnd = srv.getGameTime() + 60L;

                    for (LivingEntity v : stepped) {
                        if (!zeroHarm) {
                            cagedVictims.add(v.getUUID());
                            // Exact 3.0s (60 ticks) of Blindness matching the 3.0s (60t) light-beam confinement cage!
                            v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                            if (!SpellCombatReactionEngine.isBossEntity(srv, v)) {
                                v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 5));
                                v.addEffect(new MobEffectInstance(
                                        BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0));
                                EntityHelper.getPersistentData(v).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, cageEnd);
                            }
                            v.removeEffect(MobEffects.INVISIBILITY);
                            EntityHelper.getPersistentData(v).remove(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL);

                            if (isUndeadOrPhantomTarget(srv, v)) {
                                v.igniteForSeconds(6);
                                if (!hasHarm) {
                                    SpellCombatReactionEngine.hurtWithRadiantMateriaAdvantage(srv, v, owner, initialUndeadSmite, false, true);
                                }
                            } else if (!isPassiveOrFriendlyEntity(v, owner) && !hasHarm) {
                                SpellCombatReactionEngine.hurtWithRadiantMateriaAdvantage(srv, v, owner, initialUndeadSmite * 0.45F, false, true);
                            }
                        }
                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, v, Vec3.ZERO, zone.spell, zone.slot, zone.fallbackMateria, effPot * 1.15F
                            );
                        }
                    }

                    // Active 3.0-second (60-tick) Sunburst Confinement Cage holding non-boss victims inside the 12 light bars!
                    if (!zeroHarm && !cagedVictims.isEmpty()) {
                        double outerHoldRad = cageRadius + 1.5D;
                        double outerHoldRadSq = outerHoldRad * outerHoldRad;
                        SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                                owner, zone.center, cageRadius, 60, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, effPot,
                                (csrv, czone, celapsed) -> {
                                    if (celapsed >= czone.maxDurationTicks) {
                                        return false;
                                    }
                                    if (czone.spell != null && czone.spell.prefix().enforcesZeroEntityHarm()) {
                                        return false;
                                    }
                                    LivingEntity cageOwner = czone.resolveCaster(csrv);
                                    boolean cageWard = czone.spell != null && czone.spell.hasWardFriendlyFireSafeguard();
                                    AABB holdBox = new AABB(
                                            czone.center.x - outerHoldRad, czone.center.y - 1.5D, czone.center.z - outerHoldRad,
                                            czone.center.x + outerHoldRad, czone.center.y + 3.2D, czone.center.z + outerHoldRad
                                    );
                                    for (LivingEntity prisoner : csrv.getEntitiesOfClass(LivingEntity.class, holdBox, LivingEntity::isAlive)) {
                                        if (!cagedVictims.contains(prisoner.getUUID())) continue;
                                        if (cageOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(prisoner, cageOwner, cageWard)) {
                                            continue;
                                        }
                                        double dx = prisoner.getX() - czone.center.x;
                                        double dz = prisoner.getZ() - czone.center.z;
                                        double horizSq = dx * dx + dz * dz;
                                        if (horizSq > outerHoldRadSq) continue;

                                        if (!SpellCombatReactionEngine.isBossEntity(csrv, prisoner)) {
                                            Vec3 horiz = new Vec3(dx, 0.0D, dz);
                                            double maxInside = cageRadius - 0.55D;
                                            if (horizSq > maxInside * maxInside) {
                                                Vec3 pullIn = horiz.normalize().scale(-0.40D);
                                                SpellExecutionEngine.applySyncedMotion(prisoner,
                                                        new Vec3(pullIn.x, Math.min(0.0D, prisoner.getDeltaMovement().y), pullIn.z));
                                            } else {
                                                Vec3 cur = prisoner.getDeltaMovement();
                                                if (cur.horizontalDistanceSqr() > 0.02D || cur.y > 0.06D) {
                                                    SpellExecutionEngine.applySyncedMotion(prisoner,
                                                            new Vec3(cur.x * 0.12D, Math.min(0.02D, cur.y), cur.z * 0.12D));
                                                }
                                            }
                                        }
                                        if (celapsed > 0 && celapsed % 20 == 0 && isUndeadOrPhantomTarget(csrv, prisoner)) {
                                            prisoner.igniteForSeconds(4);
                                            SpellCombatReactionEngine.hurtWithRadiantMateriaAdvantage(
                                                    csrv, prisoner, cageOwner, (3.2F + tier * 1.00F) * chargePotencyScale * flawPot, false, true
                                            );
                                            csrv.sendParticles(ParticleTypes.END_ROD, prisoner.getX(), prisoner.getY(0.8D), prisoner.getZ(),
                                                    8, 0.25D, 0.35D, 0.25D, 0.04D);
                                        }
                                    }
                                    return true;
                                }
                        ));
                    }

                    chargesUsed[0]++;
                    if (chargesUsed[0] >= maxCharges) {
                        return false;
                    }
                    // Repeating Glyphic Sentry: 3.0s cage + 2.0s re-arm = 100 ticks before next eruption!
                    rearmUntil[0] = elapsed + 100;
                    return true;
                }
        ).markDisarmableGlyph());
        return true;
    }

    // =========================================================================
    // FORM MUTATION 7 — CLEANSING MIST CONE (CONE)
    // =========================================================================

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
        int tier = spell.getEffectiveTier();
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        double effRange = Math.max(7.5D, coneRange);
        float flawPot = spell.flawProfile().getPotencyMultiplier();
        float healAmt = (4.5F + tier * 1.75F) * Math.max(0.5F, apparatusPotency) * flawPot;
        boolean zeroHarm = spell.prefix().enforcesZeroEntityHarm();
        boolean hasHarm = hasHarmEffect(spell, slot);
        boolean safeWard = spell.hasWardFriendlyFireSafeguard();

        Vec3 origin = caster.getEyePosition();

        // Generous 58-tick 3D warm golden-ivory Cleansing Mist Cone curtain & halo rings!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_VORTEX_CONE,
                origin.subtract(0.0D, 0.20D, 0.0D), normDir,
                EssenceType.RADIANT, null, (float) effRange, VFX_CLEANSING_MIST_TICKS, tier
        );

        // Filter both hitTargets and the AABB scan strictly to an exact 60-degree cone (cos(30°) = 0.8660254D)!
        Set<UUID> processed = new HashSet<>();
        List<LivingEntity> candidates = new ArrayList<>();
        for (LivingEntity hit : hitTargets) {
            if (hit != null && hit.isAlive() && hit != caster && isInsideSixtyDegreeCone(origin, normDir, effRange, hit)) {
                candidates.add(hit);
            }
        }
        AABB coneBox = caster.getBoundingBox().expandTowards(normDir.scale(effRange)).inflate(effRange * 0.58D);
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, coneBox, LivingEntity::isAlive)) {
            if (candidate == caster) continue;
            if (isInsideSixtyDegreeCone(origin, normDir, effRange, candidate)) {
                candidates.add(candidate);
            }
        }

        for (LivingEntity target : candidates) {
            if (!target.isAlive() || !processed.add(target.getUUID())) continue;
            if (isFriendlyRecipient(caster, target)) {
                cleanseAndBlessFriendlyAlly(level, target, healAmt, tier);
            } else if (!isUndeadOrPhantomTarget(level, target) && isPassiveOrFriendlyEntity(target, caster)) {
                cleanseNegativeEffectsFromTarget(level, target);
            } else if (!zeroHarm
                    && isUndeadOrPhantomTarget(level, target)
                    && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(target, caster, safeWard)) {
                // Undead caught in the 60-degree holy cleansing spray are seared by purified daylight!
                target.igniteForSeconds(4);
                if (!hasHarm) {
                    SpellCombatReactionEngine.hurtWithRadiantMateriaAdvantage(
                            level, target, caster, healAmt * 0.65F, false, true
                    );
                } else if (!hitTargets.contains(target)) {
                    SpellExecutionEngine.deliverSubSpellOnEntity(
                            level, caster, target, normDir, spell, slot, fallbackMateria, apparatusPotency
                    );
                }
            }
        }

        // Cleanse & mend the caster standing at the nozzle of the golden mist (55% caster wash)
        cleanseAndBlessFriendlyAlly(level, caster, healAmt * 0.55F, tier);

        for (double d = 1.2D; d <= effRange; d += 1.4D) {
            Vec3 pt = origin.add(normDir.scale(d));
            double spread = 0.25D + d * 0.14D;
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), SOLAR_SERAPH_HEX_COLOR, 40, 1.18F, 0.08F),
                    pt.x, pt.y - 0.15D, pt.z, 5, spread, 0.28D, spread, 0.02D
            );
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SUNBEAM.get(), IVORY_DAYLIGHT_HEX_COLOR, 36, 0.98F, 0.06F),
                    pt.x, pt.y, pt.z, 3, spread * 0.8D, 0.22D, spread * 0.8D, 0.02D
            );
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.05F, 1.38F);
        level.playSound(null, caster.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.65F, 1.45F);
    }

    private static boolean isInsideSixtyDegreeCone(Vec3 origin, Vec3 normDir, double maxRange, LivingEntity target) {
        Vec3 toCenter = target.getBoundingBox().getCenter().subtract(origin);
        double dist = toCenter.length();
        if (dist <= 1.0E-4D || dist > maxRange + 0.35D) {
            return false;
        }
        return toCenter.scale(1.0D / dist).dot(normDir) >= CLEANSING_CONE_HALF_ANGLE_COS;
    }

    private static void cleanseNegativeEffectsFromTarget(ServerLevel level, LivingEntity target) {
        CompoundTag tag = EntityHelper.getPersistentData(target);
        if (level.getGameTime() <= tag.getLong(SpellLingeringZoneManager.TAG_FROZEN_STASIS_UNTIL).orElse(0L)) {
            SpellLingeringZoneManager.clearFrozenStasisAndShatter(level, target, tag, null);
        }
        tag.remove(SpellLingeringZoneManager.TAG_SOAKED_UNTIL);
        SpellCombatReactionEngine.purifyHexesAndNegativeEffects(target);
        level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.85D), target.getZ(),
                6, 0.28D, 0.32D, 0.28D, 0.03D);
    }

    private static void cleanseAndBlessFriendlyAlly(ServerLevel level, LivingEntity ally, float healAmt, int tier) {
        cleanseNegativeEffectsFromTarget(level, ally);

        boolean alreadyFullHp = ally.getHealth() >= ally.getMaxHealth() - 0.1F;
        SpellCombatReactionEngine.notifyMagicalHeal(ally, healAmt);
        ally.heal(healAmt);

        // Section 5.7 Overheal Saturation & Vitality Conversion when ally is already at 100% HP!
        if (alreadyFullHp) {
            if (ally instanceof Player player) {
                player.getFoodData().eat(Math.min(4, 1 + tier / 3), 0.5F);
            }
            ally.addEffect(new MobEffectInstance(MobEffects.SPEED, 120, 0, false, true));
        }

        level.sendParticles(ParticleTypes.HEART, ally.getX(), ally.getY(1.0D), ally.getZ(),
                4, 0.3D, 0.3D, 0.3D, 0.02D);
        level.sendParticles(ParticleTypes.END_ROD, ally.getX(), ally.getY(0.85D), ally.getZ(),
                8, 0.3D, 0.35D, 0.3D, 0.04D);
    }

    // =========================================================================
    // FORM MUTATION 8 — DAYLIGHT MEGALITH PILLAR (SKYFALL)
    // =========================================================================

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
        int tier = spell.getEffectiveTier();
        double crushRadius = Math.max(4.8D, radius);
        BlockPos surfacePos = findGroundSurfaceNear(level, BlockPos.containing(impactCenter));
        BlockPos baseAir = surfacePos.above();
        Vec3 anchoredCenter = new Vec3(surfacePos.getX() + 0.5D, baseAir.getY(), surfacePos.getZ() + 0.5D);
        double gateClearance = SpellDeliveryForm.probeCaveSafeSkyfallHeight(level, baseAir, tier);

        // Build tier-scaled Celestial Quartz Pillar blueprint & broadcast falling->standing 3D Pseudoblock columns
        // (40t fall, standing until zone end; permanent Celestial Quartz monument materializes when finished!)
        int effectiveMegalithTicks = SpellWorldVfxManager.resolveEffectiveZoneDurationTicks(spell, fallbackMateria, VFX_MEGALITH_TOTAL_TICKS);
        int maxPillarH = Math.clamp((int) Math.floor(gateClearance) - 1, 3, 4 + tier);
        List<int[]> monumentPlan = buildTierScaledCelestialQuartzColumns(tier, maxPillarH);
        BlockPos[] colBases = new BlockPos[monumentPlan.size()];
        for (int i = 0; i < monumentPlan.size(); i++) {
            int[] col = monumentPlan.get(i);
            BlockPos colBase = SpellWorldVfxManager.findSkyfallSurfaceBase(level, baseAir.offset(col[0], 0, col[1]));
            colBases[i] = colBase;
            int colH = col[2];
            BlockState pseudoState = resolveCelestialQuartzColumnState(col[3]);
            Vec3 skyOrigin = new Vec3(
                    colBase.getX() + 0.5D,
                    colBase.getY() + Math.max(3.5D, gateClearance - colH * 0.45D),
                    colBase.getZ() + 0.5D
            );
            SpellWorldVfxManager.broadcastSkyfallPseudoblockColumn(
                    level, skyOrigin, colBase, pseudoState, colH,
                    40, Math.max(2, effectiveMegalithTicks - 40), EssenceType.RADIANT, tier
            );
        }

        // Broadcast synchronized 108-tick 3D Solar Gate & Ground Target Seal
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_SKYFALL_PILLAR,
                anchoredCenter, anchoredCenter.add(0.0D, gateClearance, 0.0D),
                EssenceType.RADIANT, null, (float) crushRadius, VFX_MEGALITH_TOTAL_TICKS, tier
        );
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                anchoredCenter.add(0.0D, 0.03D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.RADIANT, null, (float) crushRadius, VFX_MEGALITH_TOTAL_TICKS, tier
        );
        level.playSound(null, baseAir, SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 1.1F, 1.35F);

        final boolean[] crashed = {false};
        final boolean[] materialized = {false};
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, anchoredCenter, crushRadius, VFX_MEGALITH_TOTAL_TICKS, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed >= zone.maxDurationTicks) {
                        if (!materialized[0]) {
                            materialized[0] = true;
                            materializePlannedCelestialQuartzColumns(srv, colBases, monumentPlan);
                        }
                        return false;
                    }

                    // Phase 1 (0..39t = 2.0s warning delay): 12-Ray Solar Gate shines & 3D Celestial Quartz Pillar plummets!
                    if (elapsed < 40) {
                        if (elapsed % 4 == 0) {
                            double dropFrac = elapsed / 40.0D;
                            double fallY = zone.center.y + gateClearance * (1.0D - dropFrac * dropFrac * dropFrac);
                            srv.sendParticles(
                                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SUNBEAM.get(), SOLAR_SERAPH_HEX_COLOR, 28, 1.08F, 0.06F),
                                    zone.center.x, fallY, zone.center.z, 10, 0.75D, 0.75D, 0.75D, 0.03D
                            );
                            srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 0.15D, zone.center.z,
                                    8, zone.radius * 0.45D, 0.1D, zone.radius * 0.45D, 0.02D);
                        }
                        if (elapsed == 20) {
                            srv.playSound(null, baseAir, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 1.28F);
                        }
                        return true;
                    }

                    LivingEntity owner = zone.resolveCaster(srv);
                    float flawPot = zone.spell != null ? zone.spell.flawProfile().getPotencyMultiplier() : 1.0F;
                    boolean allowHarm = zone.spell == null || !zone.spell.prefix().enforcesZeroEntityHarm();
                    boolean safeWard = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();

                    // Phase 2 (Tick 40 = exact 2.0s Celestial Quartz Megalith Touchdown!):
                    if (!crashed[0]) {
                        crashed[0] = true;

                        // Push overlapping entities clear of the landed pseudoblock Celestial Quartz footprint!
                        for (int i = 0; i < monumentPlan.size(); i++) {
                            SpellWorldVfxManager.repelEntitiesFromPseudoblockColumn(srv, colBases[i], monumentPlan.get(i)[2], true);
                        }

                        SpellWorldVfxManager.broadcast(
                                srv, SpellVfxEventPayload.VFX_RADIAL_NOVA,
                                zone.center, new Vec3(0.0D, 1.0D, 0.0D),
                                EssenceType.RADIANT, null, (float) zone.radius, 64, tier
                        );
                        srv.sendParticles(
                                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.QUARTZ_PILLAR.defaultBlockState()),
                                zone.center.x, zone.center.y + 1.2D, zone.center.z, 48, 1.35D, 1.5D, 1.35D, 0.18D
                        );
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SUNBEAM.get(), SOLAR_SERAPH_HEX_COLOR, 48, 1.30F, 0.10F),
                                zone.center.x, zone.center.y + 1.2D, zone.center.z, 54, 1.25D, 2.0D, 1.25D, 0.12D
                        );
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 1.2D, zone.center.z,
                                48, 1.2D, 2.0D, 1.2D, 0.16D);
                        srv.playSound(null, baseAir, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.05F, 1.18F);
                        srv.playSound(null, baseAir, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 1.42F);

                        AABB crushBox = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + Math.max(4.8D, maxPillarH + 1.5D), zone.center.z + zone.radius
                        );
                        double rSq = zone.radius * zone.radius;
                        boolean hasHarm = hasHarmEffect(zone.spell, zone.slot);
                        float crushBase = (8.5F + tier * 2.65F) * Math.max(0.5F, zone.apparatusPotency) * flawPot;

                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, crushBox, LivingEntity::isAlive)) {
                            if (v == owner) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, safeWard)) {
                                continue;
                            }
                            double dx = v.getX() - zone.center.x;
                            double dz = v.getZ() - zone.center.z;
                            double distSq = dx * dx + dz * dz;
                            if (distSq > rSq) continue;
                            double dist = Math.sqrt(distSq);
                            float falloff = (float) Mth.clamp(1.0D - (dist / Math.max(1.0D, zone.radius)) * 0.67D, 0.33D, 1.0D);

                            if (zone.spell != null && zone.slot != null) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(
                                        srv, owner, v, new Vec3(0.0D, -1.0D, 0.0D),
                                        zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 1.25F * falloff
                                );
                            }
                            if (allowHarm) {
                                v.removeEffect(MobEffects.INVISIBILITY);
                                if (isUndeadOrPhantomTarget(srv, v)) {
                                    v.igniteForSeconds(6);
                                }
                                if (!hasHarm) {
                                    SpellCombatReactionEngine.hurtWithRadiantMateriaAdvantage(
                                            srv, v, owner, crushBase * falloff, false, true
                                    );
                                }
                                v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 55, 2));
                                v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
                            }
                        }
                        return true;
                    }

                    // Maintain pseudoblock obstacle pushout while standing (ticks 41..107)
                    for (int i = 0; i < monumentPlan.size(); i++) {
                        SpellWorldVfxManager.repelEntitiesFromPseudoblockColumn(srv, colBases[i], monumentPlan.get(i)[2], false);
                    }

                    // Phase 3 (Ticks 54, 68, 82 — 3 expanding circular Holy Shockwave rings!):
                    if (elapsed == 54 || elapsed == 68 || elapsed == 82) {
                        double waveRad = zone.radius * 1.18D;
                        double waveRadSq = waveRad * waveRad;
                        SpellWorldVfxManager.broadcast(
                                srv, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                                zone.center.add(0.0D, 0.04D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                                EssenceType.RADIANT, null, (float) waveRad, 42, tier
                        );
                        float chimePitch = elapsed == 54 ? 1.32F : (elapsed == 68 ? 1.46F : 1.60F);
                        srv.playSound(null, baseAir, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, chimePitch);
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 0.65D, zone.center.z,
                                26, waveRad * 0.48D, 0.35D, waveRad * 0.48D, 0.10D);

                        if (allowHarm) {
                            AABB waveBox = new AABB(
                                    zone.center.x - waveRad, zone.center.y - 1.5D, zone.center.z - waveRad,
                                    zone.center.x + waveRad, zone.center.y + 3.2D, zone.center.z + waveRad
                            );
                            float shockDmg = (3.6F + tier * 1.15F) * Math.max(0.5F, zone.apparatusPotency) * flawPot;
                            for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, waveBox, LivingEntity::isAlive)) {
                                if (v == owner) continue;
                                if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, safeWard)) {
                                    continue;
                                }
                                double dx = v.getX() - zone.center.x;
                                double dz = v.getZ() - zone.center.z;
                                double distSq = dx * dx + dz * dz;
                                if (distSq > waveRadSq) continue;

                                if (isUndeadOrPhantomTarget(srv, v)) {
                                    v.igniteForSeconds(4);
                                }
                                SpellCombatReactionEngine.hurtWithRadiantMateriaAdvantage(srv, v, owner, shockDmg, false, false);
                                if (!SpellCombatReactionEngine.isBossEntity(srv, v)) {
                                    Vec3 horiz = new Vec3(dx, 0.0D, dz);
                                    Vec3 push = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : Vec3.ZERO;
                                    SpellExecutionEngine.applySyncedMotion(v, new Vec3(push.x * 0.58D, 0.22D, push.z * 0.58D));
                                }
                            }
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    /**
     * Builds a tier-scaled Celestial Quartz Pillar blueprint {@code [dx, dz, height, materialKind]}
     * ({@code 2 = QUARTZ_PILLAR}, {@code 1 = CHISELED_QUARTZ_BLOCK}, {@code 0 = SMOOTH_QUARTZ})
     * scaling smoothly in both footprint and height across Tiers 1–10:
     * <ul>
     *     <li><b>Core &amp; Cardinals</b>: {@link Blocks#QUARTZ_PILLAR} fluted shaft.</li>
     *     <li><b>Accents &amp; Pedestal Caps</b>: {@link Blocks#CHISELED_QUARTZ_BLOCK} carved solar pillars.</li>
     *     <li><b>Outer Steps</b>: {@link Blocks#SMOOTH_QUARTZ} stepped sanctuary plinths.</li>
     * </ul>
     */
    public static List<int[]> buildTierScaledCelestialQuartzColumns(int tier, int pillarHeight) {
        int clampedTier = Math.clamp(tier, 1, 10);
        int h = Math.max(3, pillarHeight);
        List<int[]> cols = new ArrayList<>();

        // Central fluted Quartz Pillar spire
        cols.add(new int[]{0, 0, h, 2});

        // Cardinal fluted Quartz Pillar ribs
        int cardH = Math.max(2, (int) Math.round(h * 0.68D));
        cols.add(new int[]{1, 0, cardH, 2});
        cols.add(new int[]{-1, 0, cardH, 2});
        cols.add(new int[]{0, 1, cardH, 2});
        cols.add(new int[]{0, -1, cardH, 2});

        // Chiseled Quartz diagonal accent pedestals
        int diagH = Math.max(1, (int) Math.round(h * 0.44D));
        int[][] diags = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        for (int[] d : diags) {
            cols.add(new int[]{d[0], d[1], diagH, 1});
        }

        // Stepped Smooth Quartz / Chiseled Quartz outer plinths scaling across Tiers 1–10
        if (clampedTier < 3) {
            cols.add(new int[]{2, 0, 1, 0});
            cols.add(new int[]{-2, 0, 1, 0});
            cols.add(new int[]{0, 2, 1, 0});
            cols.add(new int[]{0, -2, 1, 0});
        } else {
            int outerCardH = Math.max(1, (int) Math.round(h * 0.36D));
            cols.add(new int[]{2, 0, outerCardH, 1});
            cols.add(new int[]{-2, 0, outerCardH, 1});
            cols.add(new int[]{0, 2, outerCardH, 1});
            cols.add(new int[]{0, -2, outerCardH, 1});

            int knightH = Math.max(1, (int) Math.round(h * 0.24D));
            int[][] knights = {{2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {-1, 2}, {1, -2}, {-1, -2}};
            for (int[] k : knights) {
                cols.add(new int[]{k[0], k[1], knightH, 0});
            }
        }

        if (clampedTier >= 6) {
            int cornerStepH = Math.max(1, (int) Math.round(h * 0.20D));
            cols.add(new int[]{2, 2, cornerStepH, 0});
            cols.add(new int[]{-2, 2, cornerStepH, 0});
            cols.add(new int[]{2, -2, cornerStepH, 0});
            cols.add(new int[]{-2, -2, cornerStepH, 0});
        }
        if (clampedTier >= 8) {
            int spurH = Math.max(1, (int) Math.round(h * 0.25D));
            cols.add(new int[]{3, 0, spurH, 1});
            cols.add(new int[]{-3, 0, spurH, 1});
            cols.add(new int[]{0, 3, spurH, 1});
            cols.add(new int[]{0, -3, spurH, 1});
        }
        if (clampedTier >= 10) {
            int buttressH = Math.max(1, (int) Math.round(h * 0.16D));
            cols.add(new int[]{4, 0, buttressH, 0});
            cols.add(new int[]{-4, 0, buttressH, 0});
            cols.add(new int[]{0, 4, buttressH, 0});
            cols.add(new int[]{0, -4, buttressH, 0});
        }
        return cols;
    }

    /**
     * Maps {@code materialKind} from {@link #buildTierScaledCelestialQuartzColumns(int, int)} to its canonical
     * Celestial Quartz {@link BlockState} ({@code 2 = QUARTZ_PILLAR}, {@code 1 = CHISELED_QUARTZ_BLOCK}, {@code 0 = SMOOTH_QUARTZ}).
     */
    public static BlockState resolveCelestialQuartzColumnState(int materialKind) {
        return switch (materialKind) {
            case 2 -> Blocks.QUARTZ_PILLAR.defaultBlockState();
            case 1 -> Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState();
            default -> Blocks.SMOOTH_QUARTZ.defaultBlockState();
        };
    }

    private static void materializePlannedCelestialQuartzColumns(
            ServerLevel level,
            BlockPos[] colBases,
            List<int[]> cols
    ) {
        for (int i = 0; i < cols.size(); i++) {
            int[] col = cols.get(i);
            BlockState state = resolveCelestialQuartzColumnState(col[3]);
            SpellWorldVfxManager.materializeSkyfallPseudoblockColumn(level, colBases[i], state, col[2]);
        }
    }

    // =========================================================================
    // FORM MUTATION 9 — SOLAR SERAPH SUITE (AGENT / SENTRY / DECOY)
    // =========================================================================

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
        int tier = spell.getEffectiveTier();
        CompoundTag tag = EntityHelper.getPersistentData(summonEntity);
        tag.putString("SpellEidolonMateria", EssenceType.RADIANT.name());
        tag.putInt("SpellEidolonTintRGB", SOLAR_SERAPH_HEX_COLOR);

        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_SUMMON_PILLAR,
                spawnPos, new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.RADIANT, null, (float) Math.max(1.80D, radius * 0.65D), VFX_SUMMON_PILLAR_TICKS, tier
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SUNBEAM.get(), SOLAR_SERAPH_HEX_COLOR, 38, 1.10F, 0.06F),
                spawnPos.x, spawnPos.y + 1.0D, spawnPos.z, 24, 0.50D, 0.65D, 0.50D, 0.05D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.RUNE_SOWILO.get(), IVORY_DAYLIGHT_HEX_COLOR, 34, 1.12F, 0.05F),
                spawnPos.x, spawnPos.y + 1.15D, spawnPos.z, 8, 0.42D, 0.50D, 0.42D, 0.02D
        );
        level.sendParticles(ParticleTypes.END_ROD, spawnPos.x, spawnPos.y + 1.0D, spawnPos.z,
                24, 0.50D, 0.60D, 0.50D, 0.06D);
        level.playSound(null, BlockPos.containing(spawnPos), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.38F);
        level.playSound(null, BlockPos.containing(spawnPos), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.65F, 1.55F);
    }

    // =========================================================================
    // PART 3: CORE EFFECT SYNERGIES — BLINK, TRANSMUTE / BREAK & AFFLICTION
    // =========================================================================

    @Override
    public void onBlinkCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 departurePos,
            Vec3 arrivalPos,
            int potencyTier
    ) {
        SpellAssembly activeSpell = resolveActiveCasterSpell(level, caster, departurePos);
        boolean zeroHarm = activeSpell != null && activeSpell.prefix().enforcesZeroEntityHarm();
        boolean safeWard = resolveActiveCasterWardSafeguard(caster, activeSpell);

        // 1. Departure Blinding Flash of Light!
        double flashRadius = 4.2D;
        double flashRadiusSq = flashRadius * flashRadius;
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_RADIAL_NOVA,
                departurePos, new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.RADIANT, null, 3.8F, VFX_LUMINOUS_BLINK_TICKS, potencyTier
        );
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                departurePos.add(0.0D, 0.9D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.RADIANT, null, 2.20F, VFX_LUMINOUS_BLINK_TICKS, potencyTier
        );

        if (!zeroHarm) {
            AABB depBox = new AABB(
                    departurePos.x - flashRadius, departurePos.y - 2.0D, departurePos.z - flashRadius,
                    departurePos.x + flashRadius, departurePos.y + 3.0D, departurePos.z + flashRadius
            );
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, depBox, LivingEntity::isAlive)) {
                if (foe == caster) continue;
                double dx = foe.getX() - departurePos.x;
                double dz = foe.getZ() - departurePos.z;
                if (dx * dx + dz * dz > flashRadiusSq) continue;
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, safeWard)) {
                    continue;
                }
                boolean isHostileOrPvp = foe instanceof Enemy
                        || isUndeadOrPhantomTarget(level, foe)
                        || (foe instanceof Player && !safeWard && !foe.isAlliedTo(caster))
                        || (foe instanceof Mob m && m.getTarget() == caster)
                        || (foe instanceof NeutralMob nm && (nm.isAngry() || caster.getUUID().equals(nm.getPersistentAngerTarget())));
                if (isHostileOrPvp) {
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    foe.removeEffect(MobEffects.INVISIBILITY);
                    EntityHelper.getPersistentData(foe).remove(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL);
                    if (foe instanceof Mob mob && mob.getTarget() == caster) {
                        mob.getNavigation().stop();
                    }
                }
            }
        }

        // 2. Arrival 1-Effect Cleansing Seal!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                arrivalPos.add(0.0D, 0.04D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.RADIANT, null, 2.15F, 54, potencyTier
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SUNBEAM.get(), SOLAR_SERAPH_HEX_COLOR, 36, 1.15F, 0.08F),
                departurePos.x, departurePos.y + 0.95D, departurePos.z, 28, 0.45D, 0.55D, 0.45D, 0.09D
        );
        level.sendParticles(ParticleTypes.END_ROD, departurePos.x, departurePos.y + 0.95D, departurePos.z,
                26, 0.45D, 0.55D, 0.45D, 0.14D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.RUNE_SOWILO.get(), IVORY_DAYLIGHT_HEX_COLOR, 32, 1.10F, 0.05F),
                arrivalPos.x, arrivalPos.y + 1.0D, arrivalPos.z, 6, 0.35D, 0.45D, 0.35D, 0.02D
        );
        level.sendParticles(ParticleTypes.END_ROD, arrivalPos.x, arrivalPos.y + 0.95D, arrivalPos.z,
                18, 0.38D, 0.48D, 0.38D, 0.06D);

        // Cleanse 1 negative status effect (or hex / unquenchable fire / freeze-stasis) upon arrival!
        CompoundTag cData = EntityHelper.getPersistentData(caster);
        if (level.getGameTime() <= cData.getLong(SpellLingeringZoneManager.TAG_FROZEN_STASIS_UNTIL).orElse(0L)) {
            SpellLingeringZoneManager.clearFrozenStasisAndShatter(level, caster, cData, null);
        } else {
            SpellCombatReactionEngine.cleanseSingleNegativeEffectOrHex(caster);
        }
        level.playSound(null, BlockPos.containing(departurePos), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.75F, 1.50F);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.48F);
    }

    @Override
    public boolean onTransmuteBlock(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            BlockState state,
            int potencyTier
    ) {
        // Holy Purification: purifies rotten flesh -> clean leather, un-rots zombie villagers back to human form,
        // cleanses curses from equipment, and consecrates corrupted/darkened terrain!
        boolean didAny = false;
        AABB area = new AABB(pos).inflate(2.75D);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, area, Entity::isAlive)) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) {
                didAny = true;
            }
        }
        for (ZombieVillager zv : level.getEntitiesOfClass(ZombieVillager.class, area, LivingEntity::isAlive)) {
            if (cureZombieVillagerInstant(level, zv)) {
                didAny = true;
            }
        }
        if (caster != null && caster.position().distanceToSqr(Vec3.atCenterOf(pos)) <= 12.0D && cleanseEquippedCurses(level, caster)) {
            didAny = true;
        }

        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return didAny;
        }

        BlockState purifiedBlock = null;
        if (state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL)) {
            purifiedBlock = Blocks.SAND.defaultBlockState();
        } else if (state.is(Blocks.MYCELIUM) || state.is(Blocks.PODZOL)
                || state.is(Blocks.CRIMSON_NYLIUM) || state.is(Blocks.WARPED_NYLIUM)) {
            purifiedBlock = Blocks.GRASS_BLOCK.defaultBlockState();
        } else if (state.is(Blocks.NETHERRACK)) {
            purifiedBlock = Blocks.QUARTZ_BLOCK.defaultBlockState();
        } else if (state.is(Blocks.TINTED_GLASS)) {
            purifiedBlock = Blocks.GLASS.defaultBlockState();
        } else if (state.is(Blocks.CRYING_OBSIDIAN)) {
            purifiedBlock = Blocks.OBSIDIAN.defaultBlockState();
        } else if (state.is(Blocks.SCULK)) {
            purifiedBlock = Blocks.MOSS_BLOCK.defaultBlockState();
        } else if (state.is(Blocks.SCULK_VEIN) || state.is(Blocks.WITHER_ROSE)) {
            purifiedBlock = Blocks.DANDELION.defaultBlockState().canSurvive(level, pos)
                    ? Blocks.DANDELION.defaultBlockState()
                    : Blocks.AIR.defaultBlockState();
        } else if (state.is(Blocks.SOUL_CAMPFIRE)) {
            purifiedBlock = Blocks.CAMPFIRE.defaultBlockState()
                    .setValue(CampfireBlock.LIT, state.getValue(CampfireBlock.LIT))
                    .setValue(CampfireBlock.FACING, state.getValue(CampfireBlock.FACING));
        } else if (state.is(Blocks.SOUL_TORCH)) {
            purifiedBlock = Blocks.TORCH.defaultBlockState();
        } else if (state.is(Blocks.SOUL_LANTERN)) {
            purifiedBlock = Blocks.LANTERN.defaultBlockState();
        }

        if (purifiedBlock != null) {
            if (purifiedBlock.isAir()) {
                level.removeBlock(pos, false);
            } else {
                level.setBlockAndUpdate(pos, purifiedBlock);
            }
            level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5D, pos.getY() + 0.55D, pos.getZ() + 0.5D,
                    8, 0.25D, 0.25D, 0.25D, 0.04D);
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.75F, 1.45F);
            return true;
        }
        return didAny;
    }

    @Override
    public boolean onTransmuteDroppedItem(
            ServerLevel level,
            @Nullable LivingEntity caster,
            ItemEntity itemEntity,
            int potencyTier
    ) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        int count = stack.getCount();

        if (stack.is(Items.ROTTEN_FLESH)) {
            itemEntity.setItem(new ItemStack(Items.LEATHER, count));
            refundSubLinearBatchFuelDiscount(caster, potencyTier, count);
            emitHolyPurifyItemVfx(level, itemEntity);
            return true;
        }
        if (stack.is(Items.POISONOUS_POTATO)) {
            itemEntity.setItem(new ItemStack(Items.POTATO, count));
            refundSubLinearBatchFuelDiscount(caster, potencyTier, count);
            emitHolyPurifyItemVfx(level, itemEntity);
            return true;
        }
        if (stack.is(Items.FERMENTED_SPIDER_EYE) || stack.is(Items.SPIDER_EYE)) {
            itemEntity.setItem(new ItemStack(Items.GLOW_BERRIES, count));
            refundSubLinearBatchFuelDiscount(caster, potencyTier, count);
            emitHolyPurifyItemVfx(level, itemEntity);
            return true;
        }
        if (stack.is(Items.SOUL_SAND) || stack.is(Items.SOUL_SOIL)) {
            itemEntity.setItem(new ItemStack(Items.SAND, count));
            refundSubLinearBatchFuelDiscount(caster, potencyTier, count);
            emitHolyPurifyItemVfx(level, itemEntity);
            return true;
        }
        if (stack.is(Items.NETHERRACK)) {
            itemEntity.setItem(new ItemStack(Items.QUARTZ_BLOCK, count));
            refundSubLinearBatchFuelDiscount(caster, potencyTier, count);
            emitHolyPurifyItemVfx(level, itemEntity);
            return true;
        }
        if (stack.is(Items.MYCELIUM) || stack.is(Items.PODZOL)
                || stack.is(Items.CRIMSON_NYLIUM) || stack.is(Items.WARPED_NYLIUM)) {
            itemEntity.setItem(new ItemStack(Items.GRASS_BLOCK, count));
            refundSubLinearBatchFuelDiscount(caster, potencyTier, count);
            emitHolyPurifyItemVfx(level, itemEntity);
            return true;
        }
        if (stack.is(Items.SCULK)) {
            itemEntity.setItem(new ItemStack(Items.MOSS_BLOCK, count));
            refundSubLinearBatchFuelDiscount(caster, potencyTier, count);
            emitHolyPurifyItemVfx(level, itemEntity);
            return true;
        }
        if (stack.is(Items.SOUL_TORCH)) {
            itemEntity.setItem(new ItemStack(Items.TORCH, count));
            emitHolyPurifyItemVfx(level, itemEntity);
            return true;
        }
        if (stack.is(Items.SOUL_LANTERN)) {
            itemEntity.setItem(new ItemStack(Items.LANTERN, count));
            emitHolyPurifyItemVfx(level, itemEntity);
            return true;
        }
        if (stack.is(Items.TINTED_GLASS)) {
            itemEntity.setItem(new ItemStack(Items.GLASS, count));
            emitHolyPurifyItemVfx(level, itemEntity);
            return true;
        }
        if (stack.is(Items.CRYING_OBSIDIAN)) {
            itemEntity.setItem(new ItemStack(Items.OBSIDIAN, count));
            emitHolyPurifyItemVfx(level, itemEntity);
            return true;
        }
        if (stack.is(Items.WITHER_ROSE)) {
            itemEntity.setItem(new ItemStack(ModItems.STARLIGHT_FLAKES.get(), count));
            itemEntity.setGlowingTag(true);
            emitHolyPurifyItemVfx(level, itemEntity);
            return true;
        }

        // Cleanse curses (Curse of Binding, Curse of Vanishing, modded curses) from dropped equipment!
        if (stripCursesFromStack(stack)) {
            itemEntity.setItem(stack);
            emitHolyPurifyItemVfx(level, itemEntity);
            return true;
        }
        return false;
    }

    @Override
    public void onEffectAppliedToEntity(
            ServerLevel level,
            @Nullable LivingEntity caster,
            LivingEntity target,
            Vec3 direction,
            SpellCoreEffect effect,
            int potencyTier,
            SpellAssembly spell
    ) {
        if (effect != SpellCoreEffect.TRANSMUTE && effect != SpellCoreEffect.BREAK) {
            return;
        }
        if (target instanceof ZombieVillager zv && zv.isAlive()) {
            cureZombieVillagerInstant(level, zv);
            return;
        }
        cleanseEquippedCurses(level, target);
    }

    /**
     * Resolves canonical <i>Sun-Scorched Smite Hex</i> physical armor melt across Hex Levels I–III:
     * {@code 4.0F} ({@code Level I}), {@code 6.0F} ({@code Level II}), {@code 8.0F} ({@code Level III}).
     */
    public static float resolveSunScorchedArmorMelt(int stackCount) {
        return switch (Math.clamp(stackCount, 1, 3)) {
            case 2 -> 6.0F;
            case 3 -> 8.0F;
            default -> 4.0F;
        };
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Sun-Scorched Smite Hex (Section 6.8):
        // Persistent anti-milk holy branding curse melting 4.0 / 6.0 / 8.0 physical armor points across Levels I–III,
        // disabling natural health regeneration (via SpellCombatReactionEngine.isRegenLockingHex), applying Glowing &
        // stripping stealth, and dealing continuous Radiant smite damage to Undead & living hostiles!
        int stack = Math.clamp(SpellCombatReactionEngine.getHexStackLevel(target), 1, 3);
        float armorMelt = resolveSunScorchedArmorMelt(stack);
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);

        target.removeEffect(MobEffects.REGENERATION);
        SpellCombatReactionEngine.applyTimedAttributeShred(target, level, armorMelt, 0.0D, 45L);
        target.removeEffect(MobEffects.INVISIBILITY);
        EntityHelper.getPersistentData(target).remove(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL);
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 45, 0, true, false));

        LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        boolean protectedAlly = hexCaster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(target, hexCaster, false);
        boolean undead = isUndeadOrPhantomTarget(level, target);
        if (!protectedAlly) {
            if (undead) {
                target.igniteForSeconds(3);
                float baseTick = (2.15F + potencyTier * 0.70F) * mult * 1.45F;
                SpellCombatReactionEngine.hurtWithRadiantMateriaAdvantage(level, target, hexCaster, baseTick, false, false);
            } else if (!isPassiveOrFriendlyEntity(target, hexCaster)) {
                float baseTick = (2.15F + potencyTier * 0.70F) * mult;
                SpellCombatReactionEngine.hurtWithRadiantMateriaAdvantage(level, target, hexCaster, baseTick, false, false);
            }
        }

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SUNBEAM.get(), SOLAR_SERAPH_HEX_COLOR, 28, 0.98F, 0.05F),
                target.getX(), target.getY(0.85D), target.getZ(), 5, 0.25D, 0.35D, 0.25D, 0.03D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.RUNE_SOWILO.get(), IVORY_DAYLIGHT_HEX_COLOR, 26, 0.92F, 0.04F),
                target.getX(), target.getY(0.95D), target.getZ(), 2, 0.20D, 0.25D, 0.20D, 0.01D
        );
        level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.85D), target.getZ(),
                4, 0.25D, 0.35D, 0.25D, 0.03D);
        return true;
    }

    // =========================================================================
    // PUBLIC RADIANT UTILITY SPECIALIZATION & SHARED HELPERS
    // =========================================================================

    /**
     * Guide Sections 2.2.1 &amp; 2.3 Radiant-Lineage Excavation Specialization:
     * <ul>
     *     <li><b>Luminescent Auto-Torching</b>: Places a real Torch from the player's inventory (or Creative mode)
     *     every 8 blocks excavated in dark tunnels when a valid torch support surface is present (never placing
     *     invisible air-light blocks).</li>
     *     <li><b>Starlight Trail Ore Sonar</b>: Releases drifting golden starlight trail particles from the mined face
     *     guiding the player toward the nearest hidden ore vein!</li>
     * </ul>
     */
    public static void onRadiantLineageExcavateBlock(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos minedPos,
            int potencyTier
    ) {
        if (caster == null) {
            return;
        }
        CompoundTag cData = EntityHelper.getPersistentData(caster);
        int count = cData.getInt(SpellLingeringZoneManager.TAG_RADIANT_BORING_COUNTER).orElse(0) + 1;
        cData.putInt(SpellLingeringZoneManager.TAG_RADIANT_BORING_COUNTER, count);

        // 1. Luminescent Auto-Torching every 8 blocks while boring in dark tunnels (strictly real Torches, zero invisible light blocks!)
        if (count % 8 == 0 && level.getBrightness(LightLayer.BLOCK, minedPos) < 8 && level.getBlockState(minedPos).isAir()) {
            boolean placedInventoryTorch = false;
            if (caster instanceof Player player && Blocks.TORCH.defaultBlockState().canSurvive(level, minedPos)) {
                if (player.getAbilities().instabuild) {
                    level.setBlockAndUpdate(minedPos, Blocks.TORCH.defaultBlockState());
                    placedInventoryTorch = true;
                } else {
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack slotStack = player.getInventory().getItem(i);
                        if (slotStack.is(Items.TORCH)) {
                            slotStack.shrink(1);
                            level.setBlockAndUpdate(minedPos, Blocks.TORCH.defaultBlockState());
                            placedInventoryTorch = true;
                            break;
                        }
                    }
                }
            }
            if (placedInventoryTorch) {
                level.sendParticles(
                        TimedTintableParticleOption.linear(ModParticles.RUNE_SOWILO.get(), SOLAR_SERAPH_HEX_COLOR, 48, 1.05F, 0.05F),
                        minedPos.getX() + 0.5D, minedPos.getY() + 0.5D, minedPos.getZ() + 0.5D,
                        4, 0.15D, 0.15D, 0.15D, 0.01D
                );
                level.playSound(null, minedPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.65F, 1.50F);
            }
        }

        // 2. Starlight Trail Ore Sonar (throttled every 4 excavated blocks so bulk bursts stay silky-smooth!)
        if (count % 4 == 1) {
            int scanRad = Math.min(18, 8 + potencyTier);
            BlockPos nearestOre = null;
            double bestDistSq = Double.MAX_VALUE;
            for (BlockPos p : BlockPos.betweenClosed(
                    minedPos.offset(-scanRad, -scanRad / 2, -scanRad),
                    minedPos.offset(scanRad, scanRad / 2, scanRad))) {
                double dSq = p.distSqr(minedPos);
                if (dSq < 1.5D || dSq >= bestDistSq) continue;
                if (isHiddenOreBlock(level.getBlockState(p))) {
                    bestDistSq = dSq;
                    nearestOre = p.immutable();
                }
            }
            if (nearestOre != null) {
                Vec3 from = Vec3.atCenterOf(minedPos);
                Vec3 to = Vec3.atCenterOf(nearestOre);
                Vec3 delta = to.subtract(from);
                double len = delta.length();
                int steps = Math.min(12, Math.max(4, (int) Math.ceil(len * 1.4D)));
                for (int i = 1; i <= steps; i++) {
                    Vec3 pt = from.add(delta.scale((double) i / steps));
                    level.sendParticles(
                            TimedTintableParticleOption.linear(ModParticles.TINTABLE_SUNBEAM.get(), SOLAR_SERAPH_HEX_COLOR, 36 + i * 2, 0.90F, 0.04F),
                            pt.x, pt.y, pt.z, 1, 0.03D, 0.03D, 0.03D, 0.004D
                    );
                }
            }
        }
    }

    public static boolean isFriendlyRecipientPublic(LivingEntity caster, LivingEntity target) {
        return isFriendlyRecipient(caster, target);
    }

    // =========================================================================
    // INTERNAL HELPERS
    // =========================================================================

    private static boolean hasHarmEffect(@Nullable SpellAssembly spell, @Nullable SubSpellSlot slot) {
        if (slot != null && slot.hasHarmVerb()) {
            return true;
        }
        if (spell != null) {
            for (SubSpellSlot s : spell.subSpells()) {
                if (s.hasHarmVerb()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Identifies passive animals, villagers, peaceful golems, tamed allies, and friendly eidolons so Radiant
     * offensive pulses/hexes spare peaceful creatures even when Ward is not slotted, while still hitting hostile
     * {@link Enemy} mobs, angry neutral mobs, and non-allied PvP players (when Ward is off).
     */
    public static boolean isPassiveOrFriendlyEntity(LivingEntity ent, @Nullable LivingEntity caster) {
        if (ent == null || !ent.isAlive()) {
            return false;
        }
        if (ent instanceof SpellEidolonEntity eidolon) {
            return caster != null && (eidolon.isOwnedBy(caster) || eidolon.isFriendlyTo(caster));
        }
        if (ent instanceof Monster || ent instanceof Enemy) {
            return false;
        }
        if (ent instanceof Mob m && m.getTarget() != null
                && (caster == null
                || m.getTarget() == caster
                || isFriendlyRecipient(caster, m.getTarget())
                || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(m.getTarget(), caster, true))) {
            return false;
        }
        if (ent instanceof NeutralMob nm && (nm.isAngry()
                || (caster != null && caster.getUUID().equals(nm.getPersistentAngerTarget())))) {
            return false;
        }
        if (ent instanceof OwnableEntity ownable) {
            LivingEntity owner = ownable.getOwner();
            if (owner != null) {
                if (caster == null) {
                    return false;
                }
                return owner == caster || owner.isAlliedTo(caster);
            }
        }
        if (caster != null && (ent == caster || ent.isAlliedTo(caster))) {
            return true;
        }
        return ent instanceof Animal
                || ent instanceof AbstractVillager
                || ent instanceof AbstractGolem
                || ent instanceof WaterAnimal
                || ent instanceof AmbientCreature
                || ent.getType().getCategory().isFriendly();
    }

    @Nullable
    private static SpellAssembly resolveActiveCasterSpell(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 origin
    ) {
        if (caster != null) {
            SpellAssembly fallbackFound = null;
            for (ItemStack held : List.of(caster.getMainHandItem(), caster.getOffhandItem())) {
                if (held.isEmpty()) continue;
                CastingApparatusState appState = held.get(ModDataComponents.CASTING_APPARATUS_STATE.get());
                if (appState != null) {
                    Optional<SpellAssembly> active = appState.getActiveSpell();
                    if (active.isPresent()) {
                        SpellAssembly asm = active.get();
                        if (assemblyHasBlink(asm)) {
                            return asm;
                        }
                        if (fallbackFound == null) {
                            fallbackFound = asm;
                        }
                    }
                }
                SpellAssembly directSpell = held.get(ModDataComponents.SPELL_ASSEMBLY.get());
                if (directSpell != null) {
                    if (assemblyHasBlink(directSpell)) {
                        return directSpell;
                    }
                    if (fallbackFound == null) {
                        fallbackFound = directSpell;
                    }
                }
            }
            if (fallbackFound != null) {
                return fallbackFound;
            }
        }
        BlockPos originBlock = BlockPos.containing(origin);
        for (BlockPos check : List.of(originBlock, originBlock.below())) {
            if (level.getBlockEntity(check) instanceof AutocastPedestalBlockEntity pedestal) {
                Optional<SpellAssembly> pedSpell = pedestal.getActiveSpellAssembly();
                if (pedSpell.isPresent()) {
                    return pedSpell.get();
                }
            }
        }
        return null;
    }

    private static boolean assemblyHasBlink(SpellAssembly spell) {
        for (SubSpellSlot s : spell.subSpells()) {
            if (s.primaryEffect().effect() == SpellCoreEffect.BLINK
                    || s.secondaryEffect().map(e -> e.effect() == SpellCoreEffect.BLINK).orElse(false)) {
                return true;
            }
        }
        return false;
    }

    private static boolean resolveActiveCasterWardSafeguard(
            @Nullable LivingEntity caster,
            @Nullable SpellAssembly activeSpell
    ) {
        if (activeSpell != null && activeSpell.hasWardFriendlyFireSafeguard()) {
            return true;
        }
        if (caster == null) {
            return false;
        }
        CompoundTag data = EntityHelper.getPersistentData(caster);
        if (data.contains(SpellLingeringZoneManager.TAG_AIR_BLINK_WARD_SAFE)) {
            return data.getBoolean(SpellLingeringZoneManager.TAG_AIR_BLINK_WARD_SAFE).orElse(false);
        }
        return false;
    }

    private static boolean cureZombieVillagerInstant(ServerLevel level, ZombieVillager zv) {
        Vec3 pos = zv.position();
        var villager = zv.convertTo(EntityType.VILLAGER, ConversionParams.single(zv, false, false), v -> {
            v.setHealth(v.getMaxHealth());
            v.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
        });
        if (villager != null) {
            SpellWorldVfxManager.broadcast(
                    level, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                    pos.add(0.0D, 0.06D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                    EssenceType.RADIANT, null, 1.95F, 58, 3
            );
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SUNBEAM.get(), SOLAR_SERAPH_HEX_COLOR, 40, 1.15F, 0.08F),
                    pos.x, pos.y + 1.0D, pos.z, 26, 0.40D, 0.55D, 0.40D, 0.06D
            );
            level.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y + 1.0D, pos.z,
                    24, 0.38D, 0.55D, 0.38D, 0.08D);
            level.playSound(null, BlockPos.containing(pos), SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.NEUTRAL, 0.95F, 1.2F);
            return true;
        }
        return false;
    }

    private static boolean cleanseEquippedCurses(ServerLevel level, LivingEntity entity) {
        boolean any = false;
        String cleansedName = null;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack equipped = entity.getItemBySlot(slot);
            if (!equipped.isEmpty() && stripCursesFromStack(equipped)) {
                entity.setItemSlot(slot, equipped);
                cleansedName = equipped.getHoverName().getString();
                any = true;
            }
        }
        if (any) {
            level.sendParticles(ParticleTypes.END_ROD, entity.getX(), entity.getY(0.95D), entity.getZ(),
                    20, 0.38D, 0.48D, 0.38D, 0.06D);
            level.playSound(null, entity.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.95F, 1.52F);
            if (entity instanceof Player player && cleansedName != null) {
                player.displayClientMessage(
                        Component.literal("§e✦ Holy Purification §7— Curse stripped from §f" + cleansedName + "§7!"),
                        true
                );
            }
        }
        return any;
    }

    private static boolean stripCursesFromStack(ItemStack stack) {
        boolean[] stripped = {false};
        EnchantmentHelper.updateEnchantments(stack, mutable ->
                mutable.removeIf(holder -> {
                    if (holder.is(EnchantmentTags.CURSE)) {
                        stripped[0] = true;
                        return true;
                    }
                    return false;
                })
        );
        return stripped[0];
    }

    private static void emitHolyPurifyItemVfx(ServerLevel level, ItemEntity itemEntity) {
        level.sendParticles(ParticleTypes.END_ROD, itemEntity.getX(), itemEntity.getY(0.3D), itemEntity.getZ(),
                12, 0.22D, 0.22D, 0.22D, 0.05D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SUNBEAM.get(), SOLAR_SERAPH_HEX_COLOR, 26, 0.95F, 0.05F),
                itemEntity.getX(), itemEntity.getY(0.35D), itemEntity.getZ(),
                6, 0.18D, 0.18D, 0.18D, 0.03D
        );
        level.playSound(null, itemEntity.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.85F, 1.48F);
    }

    private static void refundSubLinearBatchFuelDiscount(@Nullable LivingEntity caster, int potencyTier, int batchCount) {
        if (!(caster instanceof Player player) || batchCount <= 1) {
            return;
        }
        double discountFrac = Math.min(0.30D, 0.06D + (Math.min(64, batchCount) / 64.0D) * 0.24D);
        long baseCastMfum = MateriaStageTable.getBaseMfumCastCost(Math.clamp(potencyTier, 1, 10));
        long refund = Math.max(1L, Math.round(baseCastMfum * discountFrac));
        SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.RADIANT, refund);
    }

    private static boolean isUndeadOrPhantomTarget(ServerLevel level, LivingEntity entity) {
        if (entity.getType().is(EntityTypeTags.UNDEAD)
                || entity.getType().is(ModEntityTags.DROPS_UNDEAD_ESSENCE)
                || entity instanceof Phantom) {
            return true;
        }
        return level.getGameTime() <= EntityHelper.getPersistentData(entity)
                .getLong(SpellLingeringZoneManager.TAG_UNDEAD_SHROUD_UNTIL).orElse(0L);
    }

    private static boolean isFriendlyRecipient(LivingEntity caster, LivingEntity target) {
        if (target == caster) return true;
        if (target.isAlliedTo(caster)) return true;
        if (target instanceof SpellEidolonEntity eid && eid.isFriendlyTo(caster)) return true;
        if (target instanceof TamableAnimal pet && pet.isTame() && pet.getOwner() == caster) return true;
        if (target instanceof Player && caster instanceof Player) return true;
        if (target instanceof AbstractVillager || target instanceof AbstractGolem || target instanceof Animal) {
            return !(target instanceof Monster) && !(target instanceof Enemy);
        }
        return false;
    }

    private static boolean isHiddenOreBlock(BlockState state) {
        if (state.isAir()) return false;
        if (state.is(BlockTags.COAL_ORES) || state.is(BlockTags.IRON_ORES)
                || state.is(BlockTags.COPPER_ORES) || state.is(BlockTags.GOLD_ORES)
                || state.is(BlockTags.REDSTONE_ORES) || state.is(BlockTags.LAPIS_ORES)
                || state.is(BlockTags.DIAMOND_ORES) || state.is(BlockTags.EMERALD_ORES)
                || state.is(Blocks.NETHER_QUARTZ_ORE) || state.is(Blocks.ANCIENT_DEBRIS)
                || state.is(Blocks.GILDED_BLACKSTONE)) {
            return true;
        }
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().toLowerCase(Locale.ROOT);
        return path.endsWith("_ore") || path.equals("ancient_debris");
    }

    private static double distanceToRaySegment(Vec3 point, Vec3 rayStart, Vec3 rayDirNorm, double rayLen) {
        Vec3 rel = point.subtract(rayStart);
        double proj = Math.clamp(rel.dot(rayDirNorm), 0.0D, rayLen);
        Vec3 closest = rayStart.add(rayDirNorm.scale(proj));
        return point.distanceTo(closest);
    }

    private static BlockPos findGroundSurfaceNear(ServerLevel level, BlockPos start) {
        for (int dy = 1; dy >= -4; dy--) {
            BlockPos check = start.offset(0, dy, 0);
            BlockState bs = level.getBlockState(check);
            BlockState above = level.getBlockState(check.above());
            if ((!bs.isAir() && !bs.getCollisionShape(level, check).isEmpty())
                    && (above.isAir() || above.getCollisionShape(level, check.above()).isEmpty())) {
                return check;
            }
        }
        return start.below();
    }
}
