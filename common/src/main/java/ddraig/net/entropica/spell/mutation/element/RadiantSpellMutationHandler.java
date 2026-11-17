package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.network.SpellVfxEventPayload;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModEntityTags;
import ddraig.net.entropica.registry.ModItems;
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
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
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
import net.minecraft.world.level.block.LightBlock;
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
import java.util.Set;
import java.util.UUID;

/**
 * Complete per-part Signature Mutation implementation for <b>Radiant Materia (Base Type #7)</b>
 * (Spellcrafting System Guide Sections 1.2A, 2.2.1, 2.3, 4.1–4.9, 5.1–5.7 &amp; 6.8):
 * <ul>
 *     <li><b>Canonical Advantage</b>: Strong against <b>UNDEAD (+1)</b> | Weak against <b>UMBRAL (-1)</b>.</li>
 *     <li><b>Projectile</b>: <i>Homing Dawn Star</i> — zero-gravity floating orb of concentrated sunlight (#FFE278)
 *     that actively seeks out the lowest-health undead enemy within 16 blocks every tick and detonates in a holy
 *     smite (+1 vs Undead, -1 vs Umbral) on impact or max-range airburst.</li>
 *     <li><b>Beam</b>: <i>Solar Thermal Lance</i> — continuous beam of focused daylight particles (using 3D emissive
 *     bloom rendering with zero chunk lightmap updates!) that continuously burns and smites undead enemies (+1 vs
 *     Undead), melts ice and snow blocks in its path, and ignites swooping flying Phantoms.</li>
 *     <li><b>Radial Burst</b>: <i>Supernova Flashbang</i> — detonates a brilliant 6-block 3D sphere of 16 golden
 *     sunburst rays, dealing heavy Radiant smite damage to undead (+1 advantage), knocking hostile mobs backward,
 *     and inflicting 4.0 seconds of Blindness and Weakness on all hostile monsters.</li>
 *     <li><b>Touch / Strike</b>: <i>Holy Smite &amp; Daylight Ignition</i> — converts melee strike to pure Radiant
 *     Materia damage (stacking held weapon Materia Advantage) with a 9-tooth 3D solar crescent slash; strikes against
 *     Undead trigger an additional holy burst dealing +50% bonus smite damage and radiant fire; lethal blows
 *     disintegrate rotting meat and slain foes into glowing {@code Starlight Flakes} stardust.</li>
 *     <li><b>Aura / Self</b>: <i>Pacification Shroud</i> — registers defense as Radiant (+1 vs Undead -> 50% damage,
 *     -1 vs Umbral -> 150% damage), pacifies unprovoked neutral mobs, and runs every-tick 64m eye-contact pacification
 *     preventing Endermen from ever screaming or attacking when looked in the eyes (mobile or anchored sanctuary).</li>
 *     <li><b>Glyph / Trap</b>: <i>Sunburst Confinement Sigil</i> — 1.0s arming delay, 10m (or Tier 5+ indefinite)
 *     persistence, 50% Sneak-Right-Click disarm refund, and Repeating Sentry support; when tripped, erupts a blinding
 *     12-beam cage of radiant light trapping victims for 3.0 seconds with Blindness and heavy undead Radiant smite.</li>
 *     <li><b>Cone / Spray</b>: <i>Cleansing Mist Cone</i> — golden mist curtain that heals friendly allies, pets,
 *     villagers, golems, and summoned Eidolons (with zero friendly-fire harm!), cleanses negative status effects,
 *     hexes, unquenchable fire, and freeze-stasis, and converts overheal into Vitality &amp; Saturation.</li>
 *     <li><b>Rain / Skyfall</b>: <i>Daylight Megalith Pillar</i> — opens a cave-ceiling-adaptive 12-Ray Solar Gate
 *     and drops a colossal 8-faceted pillar of white-hot celestial quartz after 2.0 seconds, crushing the impact zone
 *     and pulsing outward with 3 synchronized Holy Shockwaves.</li>
 *     <li><b>Summon / Eidolon</b>: <i>Solar Seraph Suite</i> — solar gold/ivory (#FFE278) triad: airborne 6-winged
 *     Agent raining piercing light arrows, mirror-shield Sentry reflecting hostile projectiles back at shooters, and
 *     damage-absorbing Decoy converting hits into golden ally-healing &amp; cleansing pulses.</li>
 *     <li><b>Flow (Chains)</b>: <i>Prismatic Starlight Chains</i> — leaps along refracted prism-daylight beams,
 *     crowning each victim with a 5.0-second anti-stealth glowing halo that strips Invisibility and Umbral shrouds
 *     and reveals them through solid walls.</li>
 *     <li><b>Blink / Step</b>: <i>Luminous Cleanse</i> — leaves a blinding golden sunburst flash at departure and
 *     cleanses 1 negative status effect (or hex / fire / freeze) upon arrival.</li>
 *     <li><b>Transmute &amp; Break</b>: <i>Holy Purification</i> — purifies Rotten Flesh into clean Leather (with up
 *     to 30% batch fuel discount), cures Zombie Villagers back into healthy Villagers, cleanses curses from dropped or
 *     equipped gear, purifies corrupted blocks, AND powers Radiant-lineage Ore Sonar &amp; every-8-blocks Luminescent
 *     Auto-Torching while excavating.</li>
 *     <li><b>Affliction</b>: <i>Sun-Scorched Smite Hex</i> — persistent anti-milk holy branding curse melting 4
 *     physical armor points, locking natural health regeneration, stripping stealth, and dealing continuous Radiant
 *     smite damage until cleansed via Restoration/Shadow magic or caster defeat.</li>
 * </ul>
 */
public final class RadiantSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int SOLAR_SERAPH_HEX_COLOR = 0xFFE278;
    public static final int IVORY_DAYLIGHT_HEX_COLOR = 0xFFFDF0;

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
            default -> "Solar Starlight Flow: Channels luminous golden starlight along celestial meridians.";
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
            case PROJECTILE -> "Homing Dawn Star: Floating orb of concentrated sunlight that seeks out the lowest-health undead enemy within 16 blocks and detonates in a holy smite (+1 vs Undead, -1 vs Umbral).";
            case BEAM -> "Solar Thermal Lance: Continuous beam of focused daylight (bloom-rendered without chunk light updates) that smites undead (+1 vs Undead), melts ice blocks in its path, and ignites flying Phantoms.";
            case RADIAL_BURST -> "Supernova Flashbang: Detonates a 6-block sphere of golden sunburst rays, dealing heavy Radiant damage to undead (+1 advantage), knocking mobs back, and inflicting 4s Blindness & Weakness on hostile monsters.";
            case TOUCH_STRIKE -> "Holy Smite & Daylight Ignition: Converts melee strike to Radiant Materia damage; strikes vs Undead deal +50% bonus holy smite damage and radiant fire, disintegrating lethal kills into glowing stardust.";
            case AURA -> "Pacification Shroud: Registers defense as Radiant (+1 vs Undead, -1 vs Umbral); pacifies neutral mobs and prevents Endermen from attacking when looked at in the eyes.";
            case GLYPH -> "Sunburst Confinement Sigil: Erupts in a blinding 12-beam cage of radiant light when tripped, trapping victims for 3s while inflicting Blindness and heavy Radiant smite damage to undead.";
            case CONE -> "Cleansing Mist Cone: Golden spray that heals friendly allies and cleanses negative status effects and hexes from targets in front of the caster.";
            case SKYFALL -> "Daylight Megalith Pillar: Summons a single colossal pillar of white-hot celestial quartz that crashes down after 2.0s, crushing the impact zone and emitting holy shockwaves.";
            case SUMMON_AGENT -> "Solar Seraph (Agent): Solar gold/white (#FFE278) winged seraph that flies airborne and rains piercing golden light arrows down on foes.";
            case SUMMON_SENTRY -> "Solar Seraph (Sentry): Anchored solar pylon (#FFE278) orbiting 4 golden mirror-shields that reflect hostile projectiles back at attackers.";
            case SUMMON_DECOY -> "Solar Seraph (Decoy): Radiant winged effigy (#FFE278) that absorbs incoming damage and converts it into golden healing & cleansing pulses for allies.";
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
            case BLINK -> "Luminous Cleanse: Teleport leaves a blinding flash of light at departure and cleanses 1 negative status effect upon arrival.";
            case TRANSMUTE, BREAK -> "Holy Purification: Purifies rotten flesh into clean leather, cures zombie villagers back to human form, cleanses curses from equipment, and powers Radiant ore sonar & auto-torching.";
            case AFFLICTION -> "Sun-Scorched Smite Hex: Persistent holy branding curse melting 4 armor points, locking natural health regeneration, and dealing continuous Radiant smite damage until cleansed (milk-immune).";
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

        // Every-tick homing onto the lowest-health undead enemy (including Phantoms & Necrotic Shroud bearers) within 16 blocks!
        Entity owner = projectile.getOwner();
        LivingEntity livingOwner = owner instanceof LivingEntity l ? l : null;
        List<LivingEntity> undead = level.getEntitiesOfClass(
                LivingEntity.class,
                projectile.getBoundingBox().inflate(16.0D),
                e -> e.isAlive()
                        && e != owner
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
        boolean hasHarm = slot.hasHarmVerb();
        float flawPot = spell.flawProfile().getPotencyMultiplier();
        float smiteBase = ((hasHarm ? 3.8F : 4.8F) + tier * 1.40F) * flawPot;

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
            double dist = victim.position().distanceTo(impactPos);
            if (dist > blastRadius + 0.45D) continue;

            // Section 5.1 Linear Distance Falloff: 100% epicenter -> 33% perimeter!
            float falloff = (victim == directHit)
                    ? 1.0F
                    : (float) Mth.clamp(1.0D - (dist / blastRadius) * 0.67D, 0.33D, 1.0D);

            victim.removeEffect(MobEffects.INVISIBILITY);
            boolean undead = isUndeadOrPhantomTarget(level, victim);
            if (undead) {
                victim.igniteForSeconds(5);
            }

            float mult = (victim == directHit && hasHarm) ? (undead ? 0.68F : 0.38F) : falloff;
            SpellCombatReactionEngine.hurtWithRadiantMateriaAdvantage(
                    level, victim, caster, smiteBase * mult, false, true
            );
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
        // Solar Thermal Lance: continuous beam of focused daylight particles (bloom-rendered ONLY, zero chunk lightmap updates!)
        // that continuously burns and smites undead (+1 vs Undead), melts ice blocks in its path, and ignites flying Phantoms!
        int tier = spell.getEffectiveTier();
        Vec3 eye = caster.getEyePosition();
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        double hitDist = (blockHit != null && blockHit.getType() == HitResult.Type.BLOCK)
                ? eye.distanceTo(blockHit.getLocation()) + 0.85D
                : maxReach;
        double scanDist = Math.min(maxReach, Math.max(2.0D, hitDist));
        Vec3 scanEnd = eye.add(normDir.scale(scanDist));

        if (!spell.prefix().enforcesZeroEntityHarm()) {
            float flawPot = spell.flawProfile().getPotencyMultiplier();
            float lanceSmite = (2.6F + tier * 0.98F) * Math.max(0.5F, apparatusPotency) * flawPot;
            Set<UUID> smited = new HashSet<>();

            for (LivingEntity hit : hitEntities) {
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                    continue;
                }
                smited.add(hit.getUUID());
                hit.removeEffect(MobEffects.INVISIBILITY);
                if (isUndeadOrPhantomTarget(level, hit)) {
                    hit.igniteForSeconds(6);
                    SpellCombatReactionEngine.hurtWithRadiantMateriaAdvantage(level, hit, caster, lanceSmite, false, true);
                } else if (!slot.hasHarmVerb()) {
                    SpellCombatReactionEngine.hurtWithRadiantMateriaAdvantage(level, hit, caster, lanceSmite * 0.65F, false, false);
                }
            }

            // Wide 1.35m solar thermal corona sweep specifically catching fast-swooping airborne Phantoms along the ray!
            AABB phantomCorridor = new AABB(eye, scanEnd).inflate(1.45D);
            for (Phantom phantom : level.getEntitiesOfClass(Phantom.class, phantomCorridor, LivingEntity::isAlive)) {
                if (smited.contains(phantom.getUUID())) continue;
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(phantom, caster, spell.hasWardFriendlyFireSafeguard())) {
                    continue;
                }
                if (distanceToRaySegment(phantom.getBoundingBox().getCenter(), eye, normDir, scanDist) <= 1.45D) {
                    phantom.igniteForSeconds(6);
                    SpellCombatReactionEngine.hurtWithRadiantMateriaAdvantage(level, phantom, caster, lanceSmite, false, true);
                    level.sendParticles(ParticleTypes.END_ROD, phantom.getX(), phantom.getY(0.5D), phantom.getZ(),
                            8, 0.30D, 0.25D, 0.30D, 0.05D);
                }
            }
        }

        // Melt ice (Ice, Frosted Ice, Packed Ice, and Tier 2+ Blue Ice) & snow blocks along the beam path
        // STRICTLY without placing any light-emitting blocks (zero chunk lightmap lag!)
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
                if (bs.is(Blocks.ICE) || bs.is(Blocks.FROSTED_ICE) || bs.is(Blocks.PACKED_ICE)
                        || (tier >= 2 && bs.is(Blocks.BLUE_ICE))) {
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
        double flashRadius = Math.max(6.0D, radius); // Canonical 6-block minimum radius!

        // Generous 68-tick 3D 16-ray golden Supernova Flashbang sphere & 12-ray solar compass floor seal (zero light blocks!)
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

        AABB box = new AABB(
                center.x - flashRadius, center.y - 2.5D, center.z - flashRadius,
                center.x + flashRadius, center.y + 3.5D, center.z + flashRadius
        );
        boolean hasHarm = slot.hasHarmVerb();
        float flawPot = spell.flawProfile().getPotencyMultiplier();
        float undeadSmiteBase = (5.5F + tier * 1.95F) * Math.max(0.5F, apparatusPotency) * flawPot;

        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (entity == caster) continue;
            double dist = Math.sqrt(
                    Math.pow(entity.getX() - center.x, 2) + Math.pow(entity.getZ() - center.z, 2)
            );
            if (dist > flashRadius + 0.5D) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                    entity, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }

            boolean hostileMonster = entity instanceof Monster;
            boolean undead = isUndeadOrPhantomTarget(level, entity);
            float distFactor = (float) Mth.clamp(1.0D - (dist / Math.max(1.0D, flashRadius)) * 0.67D, 0.33D, 1.0D);

            // Inflict 4.0 seconds (80 ticks) of Blindness & Weakness on all hostile monsters!
            if (hostileMonster || undead) {
                entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
                entity.removeEffect(MobEffects.INVISIBILITY);
                EntityHelper.getPersistentData(entity).remove(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL);
            }

            if (!spell.prefix().enforcesZeroEntityHarm()) {
                if (undead) {
                    entity.igniteForSeconds(5);
                    SpellCombatReactionEngine.hurtWithRadiantMateriaAdvantage(
                            level, entity, caster, undeadSmiteBase * distFactor, false, true
                    );
                } else if (hostileMonster && (!hasHarm || dist > radius)) {
                    float livingFlashDmg = (hasHarm
                            ? MateriaStageTable.getBaseCombatDamageHp(tier) * distFactor * apparatusPotency
                            : (3.2F + tier * 1.05F) * distFactor * apparatusPotency) * flawPot;
                    SpellCombatReactionEngine.hurtWithRadiantMateriaAdvantage(
                            level, entity, caster, livingFlashDmg, false, true
                    );
                }
                if ((hostileMonster || undead) && dist > radius) {
                    SpellExecutionEngine.deliverSubSpellOnEntity(
                            level, caster, entity, direction, spell, slot, fallbackMateria, apparatusPotency * distFactor
                    );
                }
            }

            if ((hostileMonster || undead) && !SpellCombatReactionEngine.isBossEntity(level, entity)) {
                Vec3 delta = entity.position().subtract(center);
                Vec3 horiz = new Vec3(delta.x, 0.0D, delta.z);
                Vec3 away = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
                SpellExecutionEngine.applySyncedMotion(entity, new Vec3(away.x * 1.18D, 0.36D, away.z * 1.18D));
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
        int tier = spell.getEffectiveTier();
        boolean hasHarm = slot.hasHarmVerb();
        float flawPot = spell.flawProfile().getPotencyMultiplier();
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 slashOrigin = caster.getEyePosition().subtract(0.0D, 0.28D, 0.0D).add(normDir.scale(0.95D));

        // Generous 54-tick 9-tooth 3D golden-white solar crescent slash!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_CRESCENT_SLASH,
                slashOrigin, normDir, EssenceType.RADIANT, null,
                2.65F, VFX_HOLY_SMITE_SLASH_TICKS, tier
        );

        float baseStrikeDmg = (3.5F + tier * 1.10F) * Math.max(0.5F, apparatusPotency) * flawPot;
        float undeadSmiteBonus = 0.50F * MateriaStageTable.getBaseCombatDamageHp(tier) * Math.max(0.5F, apparatusPotency) * flawPot;
        long purifyUntil = level.getGameTime() + 45L;

        for (int idx = 0; idx < hitEntities.size(); idx++) {
            LivingEntity hit = hitEntities.get(idx);
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                    hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            boolean isPrimary = (idx == 0);
            float splashScale = isPrimary ? 1.0F : 0.50F; // Section 4.4: 100% primary, 50% sweeping cleave!

            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_RADIANT_PURIFY_DROPS_UNTIL, purifyUntil);
            hit.removeEffect(MobEffects.INVISIBILITY);

            boolean undead = isUndeadOrPhantomTarget(level, hit);
            if (!hasHarm && hit.isAlive()) {
                SpellCombatReactionEngine.hurtWithRadiantMateriaAdvantage(
                        level, hit, caster, baseStrikeDmg * splashScale, true, true
                );
            }

            if (undead) {
                Vec3 hitMid = hit.position().add(0.0D, hit.getBbHeight() * 0.55D, 0.0D);
                SpellWorldVfxManager.broadcast(
                        level, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                        hitMid, new Vec3(0.0D, 1.0D, 0.0D),
                        EssenceType.RADIANT, null, isPrimary ? 1.90F : 1.35F, 52, tier
                );
                if (hit.isAlive()) {
                    hit.igniteForSeconds(isPrimary ? 6 : 4);
                    SpellCombatReactionEngine.hurtWithRadiantMateriaAdvantage(
                            level, hit, caster, Math.max(4.0F * splashScale, undeadSmiteBonus * splashScale), true, true
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

            if (!hit.isAlive() || hit.getHealth() <= 0.0F) {
                purifyRottingDropsToStardust(level, hit.getBoundingBox().inflate(3.5D), hit.position());
            }
        }
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
                    AABB triggerBox = new AABB(
                            zone.center.x - 2.1D, zone.center.y - 0.85D, zone.center.z - 2.1D,
                            zone.center.x + 2.1D, zone.center.y + 2.35D, zone.center.z + 2.1D
                    );
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, triggerBox,
                            e -> e.isAlive() && e != owner
                                    && !(e instanceof SpellEidolonEntity eid && owner != null && eid.isOwnedBy(owner))
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                            e, owner, zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard())));
                    if (stepped.isEmpty()) return true;

                    double cageRadius = 2.40D;
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

                    Set<UUID> cagedVictims = new HashSet<>();
                    float effPot = zone.apparatusPotency * chargePotencyScale;
                    float initialUndeadSmite = (6.5F + tier * 2.10F) * Math.max(0.5F, effPot);
                    long cageEnd = srv.getGameTime() + 60L;

                    for (LivingEntity v : stepped) {
                        cagedVictims.add(v.getUUID());
                        v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 65, 0));
                        v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 5));
                        v.addEffect(new MobEffectInstance(
                                BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0));
                        EntityHelper.getPersistentData(v).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, cageEnd);
                        v.removeEffect(MobEffects.INVISIBILITY);

                        if (zone.spell != null && !zone.spell.prefix().enforcesZeroEntityHarm()) {
                            if (isUndeadOrPhantomTarget(srv, v)) {
                                v.igniteForSeconds(6);
                                SpellCombatReactionEngine.hurtWithRadiantMateriaAdvantage(srv, v, owner, initialUndeadSmite, false, true);
                            } else if (zone.slot != null && !zone.slot.hasHarmVerb()) {
                                SpellCombatReactionEngine.hurtWithRadiantMateriaAdvantage(srv, v, owner, initialUndeadSmite * 0.45F, false, true);
                            }
                        }
                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, v, Vec3.ZERO, zone.spell, zone.slot, zone.fallbackMateria, effPot * 1.15F
                            );
                        }
                    }

                    // Active 3.0-second (60-tick) Sunburst Confinement Cage holding trapped victims inside the 12 light bars!
                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, cageRadius, 60, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, effPot,
                            (csrv, czone, celapsed) -> {
                                if (celapsed >= czone.maxDurationTicks) {
                                    return false;
                                }
                                LivingEntity cageOwner = czone.resolveCaster(csrv);
                                AABB holdBox = new AABB(
                                        czone.center.x - cageRadius - 1.5D, czone.center.y - 1.5D, czone.center.z - cageRadius - 1.5D,
                                        czone.center.x + cageRadius + 1.5D, czone.center.y + 3.2D, czone.center.z + cageRadius + 1.5D
                                );
                                for (LivingEntity prisoner : csrv.getEntitiesOfClass(LivingEntity.class, holdBox, LivingEntity::isAlive)) {
                                    if (!cagedVictims.contains(prisoner.getUUID())) continue;
                                    if (!SpellCombatReactionEngine.isBossEntity(csrv, prisoner)) {
                                        Vec3 offset = prisoner.position().subtract(czone.center);
                                        Vec3 horiz = new Vec3(offset.x, 0.0D, offset.z);
                                        double maxInside = cageRadius - 0.55D;
                                        if (horiz.lengthSqr() > maxInside * maxInside) {
                                            Vec3 pullIn = horiz.normalize().scale(-0.40D);
                                            SpellExecutionEngine.applySyncedMotion(prisoner, new Vec3(pullIn.x, Math.min(0.0D, prisoner.getDeltaMovement().y), pullIn.z));
                                        } else {
                                            Vec3 cur = prisoner.getDeltaMovement();
                                            if (cur.horizontalDistanceSqr() > 0.02D || cur.y > 0.06D) {
                                                SpellExecutionEngine.applySyncedMotion(prisoner, new Vec3(cur.x * 0.12D, Math.min(0.02D, cur.y), cur.z * 0.12D));
                                            }
                                        }
                                    }
                                    if (celapsed > 0 && celapsed % 20 == 0
                                            && czone.spell != null && !czone.spell.prefix().enforcesZeroEntityHarm()
                                            && isUndeadOrPhantomTarget(csrv, prisoner)) {
                                        prisoner.igniteForSeconds(4);
                                        SpellCombatReactionEngine.hurtWithRadiantMateriaAdvantage(
                                                csrv, prisoner, cageOwner, (3.2F + tier * 1.00F) * chargePotencyScale, false, true
                                        );
                                        csrv.sendParticles(ParticleTypes.END_ROD, prisoner.getX(), prisoner.getY(0.8D), prisoner.getZ(),
                                                8, 0.25D, 0.35D, 0.25D, 0.04D);
                                    }
                                }
                                return true;
                            }
                    ));

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

        Vec3 origin = caster.getEyePosition();

        // Generous 58-tick 3D warm golden-ivory Cleansing Mist Cone curtain & halo rings!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_VORTEX_CONE,
                origin.subtract(0.0D, 0.20D, 0.0D), normDir,
                EssenceType.RADIANT, null, (float) effRange, VFX_CLEANSING_MIST_TICKS, tier
        );

        Set<UUID> processed = new HashSet<>();
        List<LivingEntity> candidates = new ArrayList<>(hitTargets);
        AABB coneBox = caster.getBoundingBox().expandTowards(normDir.scale(effRange)).inflate(effRange * 0.55D);
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, coneBox, LivingEntity::isAlive)) {
            if (candidate == caster) continue;
            Vec3 to = candidate.getBoundingBox().getCenter().subtract(origin);
            double dist = to.length();
            if (dist <= effRange + 0.5D && dist > 1.0E-4D && to.scale(1.0D / dist).dot(normDir) >= 0.50D) {
                candidates.add(candidate);
            }
        }

        for (LivingEntity target : candidates) {
            if (!target.isAlive() || !processed.add(target.getUUID())) continue;
            if (isFriendlyRecipient(caster, target)) {
                cleanseAndBlessFriendlyAlly(level, target, healAmt, tier);
            } else if (!spell.prefix().enforcesZeroEntityHarm() && isUndeadOrPhantomTarget(level, target)) {
                // Undead caught in the holy cleansing spray are seared by purified daylight!
                target.igniteForSeconds(4);
                SpellCombatReactionEngine.hurtWithRadiantMateriaAdvantage(
                        level, target, caster, healAmt * 0.65F, false, true
                );
            }
        }

        // Cleanse & mend the caster standing at the nozzle of the golden mist
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

    private static void cleanseAndBlessFriendlyAlly(ServerLevel level, LivingEntity ally, float healAmt, int tier) {
        CompoundTag aTag = EntityHelper.getPersistentData(ally);
        if (level.getGameTime() <= aTag.getLong(SpellLingeringZoneManager.TAG_FROZEN_STASIS_UNTIL).orElse(0L)) {
            SpellLingeringZoneManager.clearFrozenStasisAndShatter(level, ally, aTag, null);
        }
        aTag.remove(SpellLingeringZoneManager.TAG_SOAKED_UNTIL);
        SpellCombatReactionEngine.purifyHexesAndNegativeEffects(ally);

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
        Vec3 anchoredCenter = new Vec3(impactCenter.x, surfacePos.getY() + 1.0D, impactCenter.z);
        double gateClearance = probeOverheadClearance(level, BlockPos.containing(anchoredCenter), 11);

        // Broadcast synchronized 108-tick 3D Celestial Quartz Monolith & Solar Gate (0..40t warning drop, 40t impact, 3 shockwaves!)
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_SKYFALL_PILLAR,
                anchoredCenter, new Vec3(0.0D, -1.0D, 0.0D),
                EssenceType.RADIANT, null, (float) crushRadius, VFX_MEGALITH_TOTAL_TICKS, tier
        );
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                anchoredCenter.add(0.0D, 0.03D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.RADIANT, null, (float) crushRadius, VFX_MEGALITH_TOTAL_TICKS, tier
        );
        level.playSound(null, BlockPos.containing(anchoredCenter), SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 1.1F, 1.35F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, anchoredCenter, crushRadius, VFX_MEGALITH_TOTAL_TICKS, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;

                    // Phase 1 (0..39t = 2.0s warning delay): 12-Ray Solar Gate shines & Celestial Quartz Megalith plummets!
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
                            srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 1.28F);
                        }
                        return true;
                    }

                    LivingEntity owner = zone.resolveCaster(srv);
                    float flawPot = zone.spell != null ? zone.spell.flawProfile().getPotencyMultiplier() : 1.0F;

                    // Phase 2 (Tick 40 = exact 2.0s Celestial Quartz Megalith Touchdown!):
                    if (elapsed == 40) {
                        SpellWorldVfxManager.broadcast(
                                srv, SpellVfxEventPayload.VFX_RADIAL_NOVA,
                                zone.center, new Vec3(0.0D, 1.0D, 0.0D),
                                EssenceType.RADIANT, null, (float) zone.radius, 64, tier
                        );
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SUNBEAM.get(), SOLAR_SERAPH_HEX_COLOR, 48, 1.30F, 0.10F),
                                zone.center.x, zone.center.y + 1.2D, zone.center.z, 54, 1.25D, 2.0D, 1.25D, 0.12D
                        );
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 1.2D, zone.center.z,
                                48, 1.2D, 2.0D, 1.2D, 0.16D);
                        srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.05F, 1.18F);
                        srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 1.42F);

                        AABB crushBox = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 4.8D, zone.center.z + zone.radius
                        );
                        float crushBase = (8.5F + tier * 2.65F) * Math.max(0.5F, zone.apparatusPotency) * flawPot;

                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, crushBox, LivingEntity::isAlive)) {
                            if (v == owner) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                    v, owner, zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard())) {
                                continue;
                            }
                            double dist = Math.sqrt(Math.pow(v.getX() - zone.center.x, 2) + Math.pow(v.getZ() - zone.center.z, 2));
                            if (dist > zone.radius + 0.5D) continue;
                            float falloff = (float) Mth.clamp(1.0D - (dist / Math.max(1.0D, zone.radius)) * 0.67D, 0.33D, 1.0D);

                            v.removeEffect(MobEffects.INVISIBILITY);
                            if (isUndeadOrPhantomTarget(srv, v)) {
                                v.igniteForSeconds(6);
                            }
                            if (zone.spell != null && !zone.spell.prefix().enforcesZeroEntityHarm()) {
                                SpellCombatReactionEngine.hurtWithRadiantMateriaAdvantage(
                                        srv, v, owner, crushBase * falloff, false, true
                                );
                            }
                            v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 55, 2));
                            v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
                            if (zone.spell != null && zone.slot != null) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(
                                        srv, owner, v, new Vec3(0.0D, -1.0D, 0.0D),
                                        zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 1.25F * falloff
                                );
                            }
                        }
                        return true;
                    }

                    // Phase 3 (Ticks 54, 68, 82 — synchronized with SpellWorldVfxRenderer's 3 expanding Holy Shockwave rings!):
                    if (elapsed == 54 || elapsed == 68 || elapsed == 82) {
                        double waveRad = zone.radius * 1.18D;
                        SpellWorldVfxManager.broadcast(
                                srv, SpellVfxEventPayload.VFX_ARCANE_SEAL,
                                zone.center.add(0.0D, 0.04D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                                EssenceType.RADIANT, null, (float) waveRad, 42, tier
                        );
                        float chimePitch = elapsed == 54 ? 1.32F : (elapsed == 68 ? 1.46F : 1.60F);
                        srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, chimePitch);
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 0.65D, zone.center.z,
                                26, waveRad * 0.48D, 0.35D, waveRad * 0.48D, 0.10D);

                        if (zone.spell != null && !zone.spell.prefix().enforcesZeroEntityHarm()) {
                            AABB waveBox = new AABB(
                                    zone.center.x - waveRad, zone.center.y - 1.5D, zone.center.z - waveRad,
                                    zone.center.x + waveRad, zone.center.y + 3.2D, zone.center.z + waveRad
                            );
                            float shockDmg = (3.6F + tier * 1.15F) * Math.max(0.5F, zone.apparatusPotency) * flawPot;
                            for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, waveBox, LivingEntity::isAlive)) {
                                if (v == owner) continue;
                                if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                        v, owner, zone.spell.hasWardFriendlyFireSafeguard())) {
                                    continue;
                                }
                                if (isUndeadOrPhantomTarget(srv, v)) {
                                    v.igniteForSeconds(4);
                                }
                                SpellCombatReactionEngine.hurtWithRadiantMateriaAdvantage(srv, v, owner, shockDmg, false, false);
                                if (!SpellCombatReactionEngine.isBossEntity(srv, v)) {
                                    Vec3 away = v.position().subtract(zone.center);
                                    Vec3 horiz = new Vec3(away.x, 0.0D, away.z);
                                    Vec3 push = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : Vec3.ZERO;
                                    SpellExecutionEngine.applySyncedMotion(v, new Vec3(push.x * 0.58D, 0.22D, push.z * 0.58D));
                                }
                            }
                        }
                    }
                    return elapsed < zone.maxDurationTicks;
                }
        ));
        return true;
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

        SpellCombatReactionEngine.notifyMagicalHeal(caster, 2.5F + tier * 0.35F);
        caster.heal(2.5F + tier * 0.35F);

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
        // 1. Departure Blinding Flash of Light!
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

        AABB depBox = new AABB(
                departurePos.x - 4.2D, departurePos.y - 2.0D, departurePos.z - 4.2D,
                departurePos.x + 4.2D, departurePos.y + 3.0D, departurePos.z + 4.2D
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, depBox, LivingEntity::isAlive)) {
            if (foe == caster) continue;
            if (foe instanceof Monster || isUndeadOrPhantomTarget(level, foe)) {
                foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                foe.removeEffect(MobEffects.INVISIBILITY);
                EntityHelper.getPersistentData(foe).remove(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL);
                if (foe instanceof Mob mob && mob.getTarget() == caster) {
                    mob.getNavigation().stop();
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

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Sun-Scorched Smite Hex (Section 6.8):
        // Persistent anti-milk holy branding curse melting 4 physical armor points, disabling natural health regeneration
        // (via SpellCombatReactionEngine.isRegenLockingHex), stripping stealth, and dealing continuous Radiant smite damage!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 4.0D * mult, 0.0D, 45L);
        target.removeEffect(MobEffects.INVISIBILITY);
        EntityHelper.getPersistentData(target).remove(SpellLingeringZoneManager.TAG_UMBRAL_SHROUD_UNTIL);
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 45, 0, true, false));

        LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        boolean undead = isUndeadOrPhantomTarget(level, target);
        if (undead) {
            target.igniteForSeconds(3);
        }
        float baseTick = (2.15F + potencyTier * 0.70F) * mult * (undead ? 1.45F : 1.0F);
        SpellCombatReactionEngine.hurtWithRadiantMateriaAdvantage(level, target, hexCaster, baseTick, false, false);

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
     *     <li><b>Luminescent Auto-Torching</b>: Places a Torch from the player's inventory (or a permanent Level-14
     *     Radiant Light Wisp {@link Blocks#LIGHT} if no torches are carried) every 8 blocks excavated in dark tunnels.</li>
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

        // 1. Luminescent Auto-Torching every 8 blocks while boring in dark tunnels!
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
            if (!placedInventoryTorch) {
                level.setBlockAndUpdate(minedPos, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 14));
            }
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.RUNE_SOWILO.get(), SOLAR_SERAPH_HEX_COLOR, 48, 1.05F, 0.05F),
                    minedPos.getX() + 0.5D, minedPos.getY() + 0.5D, minedPos.getZ() + 0.5D,
                    4, 0.15D, 0.15D, 0.15D, 0.01D
            );
            level.playSound(null, minedPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.65F, 1.50F);
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
            return !(target instanceof Monster);
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

    private static double probeOverheadClearance(ServerLevel level, BlockPos groundAir, int maxScan) {
        for (int dy = 3; dy <= maxScan; dy++) {
            BlockPos p = groundAir.above(dy);
            BlockState bs = level.getBlockState(p);
            if (!bs.isAir() && !bs.getCollisionShape(level, p).isEmpty()) {
                return Math.max(3.5D, dy - 0.35D);
            }
        }
        return maxScan;
    }
}
