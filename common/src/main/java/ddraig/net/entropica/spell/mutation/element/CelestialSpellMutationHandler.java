package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
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
import ddraig.net.entropica.spell.fuel.SpellFuelService;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.spell.vfx.SpellWorldVfxManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Complete per-part Signature Mutation implementation for <b>Celestial Materia (Tier 4 Apex Fusion #50)</b>
 * (Spellcrafting System Guide Section 6.52 — Radiant + Nature + Vitae + Air catalyzed by Chimera):
 * <ul>
 *     <li><b>Palette</b>: Radiant Pure Gold ({@code #FFEA78}) + Vitae Rose Pink ({@code #FF6B9D}) + Nature Forest Emerald ({@code #1A5923}) + Air Chalk / Pale Gold ({@code #A88654})</li>
 *     <li><b>Advantage</b>: Strong vs <b>UNDEAD (+1)</b>, <b>WATER (+1)</b>, <b>EARTH (+1)</b>, <b>OBLIVION (+1)</b>, <b>WRAITH (+1)</b>, and <b>BARROW (+1)</b> |
 *         Weak vs <b>UMBRAL (-1)</b> and <b>VOID (-1)</b></li>
 * </ul>
 */
public final class CelestialSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int CELESTIAL_STARLIGHT_GOLD_HEX_COLOR = 0xFFEA78;
    public static final int CELESTIAL_ROSE_PRANA_HEX_COLOR = 0xFF6B9D;
    public static final int CELESTIAL_FOREST_EMERALD_HEX_COLOR = 0x1A5923;
    public static final int CELESTIAL_AIR_CHALK_GOLD_HEX_COLOR = 0xA88654;

    private static final int[] QUAD_CELESTIAL_PALETTE = {
            CELESTIAL_STARLIGHT_GOLD_HEX_COLOR,
            CELESTIAL_ROSE_PRANA_HEX_COLOR,
            CELESTIAL_FOREST_EMERALD_HEX_COLOR,
            CELESTIAL_AIR_CHALK_GOLD_HEX_COLOR
    };

    private static final String TAG_CELESTIAL_BEAM_LAST_TICK = "CelestialBeamLastTick";
    private static final String TAG_CELESTIAL_BEAM_TICKS = "CelestialBeamContactTicks";
    private static final String TAG_CELESTIAL_BEAM_ARMOR_STACKS = "CelestialBeamArmorStacks";
    private static final String TAG_CELESTIAL_BEAM_NEXT_SIPHON = "CelestialBeamNextSiphonTick";

    @Override
    public EssenceType materiaType() {
        return EssenceType.CELESTIAL;
    }

    // =========================================================================
    // TITLES & CODEX / TOOLTIP DESCRIPTIONS
    // =========================================================================

    @Override
    public @Nullable String resolveFlowTitle(SpellFlowModifier flow) {
        return flow == SpellFlowModifier.ORBIT_SATELLITE ? "Holy White-Hole Star-Orbit Flow" : null;
    }

    @Override
    public @Nullable String resolveFlowDescription(SpellFlowModifier flow) {
        return flow == SpellFlowModifier.ORBIT_SATELLITE
                ? "Manifests 4 orbiting celestial star-cores in a glowing halo (#FFEA78 / #FF6B9D / #1A5923 / #A88654) around the caster; vaporizes incoming hostile projectiles into motes of healing starlight, fires autonomous retaliatory starlight darts bypassing 50% armor and blinding attackers, and pulses a 4m pranic aura healing adjacent allies for 1 heart every 3.0s."
                : null;
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Holy Solar Singularity Dart";
            case BEAM -> "Holy Pranic Fusion Beam & Cataclysmic Supernova";
            case RADIAL_BURST -> "Holy Supernova Dawn-Shockwave";
            case TOUCH_STRIKE -> "Holy Supernova Cleave / White-Hole Touch";
            case AURA -> "Holy Celestial Supernova Shroud";
            case GLYPH -> "Holy White-Hole Sigil";
            case CONE -> "Holy Dawn-Deluge Cone";
            case SKYFALL -> "Cataclysmic Supernova Starfall";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE ->
                    "Hurls a wall-phasing white-hole starcore wrapped in radiant gold, blooming rose-prana & emerald life-mist (#FFEA78 / #FF6B9D / #1A5923 / #A88654); phases through walls and shields, bypasses 50% armor (3.5x damage vs Undead, Demonic & Abyssal), and collapses into a 6m Holy White-Hole Singularity pulling foes inward (-5 armor, 8.0s Blindness & party health siphon).";
            case BEAM ->
                    "Channels a wall-penetrating ray of white-gold starlight & photosynthetic plasma (#FFEA78 / #FF6B9D / #1A5923 / #A88654) melting 2 armor/s, blinding hostiles, healing tethered allies, and refilling caster fuel; after 5.0s continuous channeling triggers a Cataclysmic Supernova Collapse (-6 armor, 12.0s Blindness, holy vortex -> 10m knockback & restorative Celestial Lilies).";
            case RADIAL_BURST ->
                    "10m Holy Supernova Dawn-Shockwave (zero physical light blocks): blasts hostiles back 8m, vaporizes hostile projectiles & necrotic summons, shatters 5 armor, and cloaks the party in a Celestial Ascendance Shroud (+10 Armor, +5 Toughness, Wither/Poison/Blindness immunity & 40% holy retaliation for 15.0s).";
            case TOUCH_STRIKE ->
                    "Sweeping wall-cleaving strike of condensed solar corona, pranic rose-light & emerald briars (#FFEA78 / #FF6B9D / #1A5923 / #A88654): bypasses 50% armor, shatters 5 armor, disables shields for 5.0s, leeches 35% of damage dealt as health/fuel to the party, and launches targets 6m backward with Holy Blindness & Brand.";
            case AURA ->
                    "Holy Celestial Supernova Shroud (zero physical light blocks): grants +10 Armor, +5 Toughness & Wither/Poison/Darkness/Blindness immunity, reduces hostile mob detection by 80%, vaporizes incoming arrows & hostile projectiles within 4m into healing starlight, and retaliates against melee attackers with holy white-hole pulses.";
            case GLYPH ->
                    "Disarmable Single/Repeating Holy White-Hole Sigil on any floor, wall, or ceiling: ally contact grants instant 10-heart healing, Regeneration III & debuff cleansing; hostile approach within 4m triggers a 10m holy inversion vortex dragging foes inward, launching them 12m airborne, shattering 5 armor & blinding for 10.0s before slamming them down.";
            case CONE ->
                    "Wall-penetrating Holy Dawn-Deluge Cone (#FFEA78 / #FF6B9D / #1A5923 / #A88654): cleanses allies and grants Regeneration III for 12.0s, while shattering 5 armor on hostiles, bypassing 50% armor, and inflicting Divine Stupefaction / Holy Reverence (25.0s AI disruption causing mobs to lose player lock, flee, or strike demonic/undead allies).";
            case SKYFALL ->
                    "Extended 20.0s Cataclysmic Supernova Starfall across a 14m zone (#FFEA78 / #FF6B9D / #1A5923 / #A88654): rains 3D white-hole starcores, solar genesis meteors & emerald life-spires (3.5x vs Undead/Abyssal, -6 armor & knockdown), leaving a 10.0s Consecrated White-Hole Singularity Zone pulling & burning hostiles while restoring ally health, hunger & Materia fuel.";
            case SUMMON_AGENT ->
                    "Celestial Seraph-Colossus (45s lifecycle / 100 HP): wall-phasing flying starlight archon delivering sweeping greatblade cleaves that bypass 50% armor, shatter 3 armor per strike, smite undead, siphon 35% damage as party health, and knock foes 8m back.";
            case SUMMON_SENTRY ->
                    "Apex White-Hole Dawn Spire (45s lifecycle / 100 HP): anchored monolithic celestial spire bombarding hostiles up to 18m away with wall-penetrating white-hole mortars (-2 armor/hit) while pulsing a 10m Genesis Field (holy gravity tether, -2 armor/s & 6m healing/smiting waves).";
            case SUMMON_DECOY ->
                    "Holy Genesis Effigy (14s duration): 16m taunt solar-bloom effigy reflecting 40% of all incoming damage as holy fire and detonating in a 10m Supernova Dawnburst (-6 armor, 8.0s Blindness & full party health restoration).";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Celestial Supernova Ascendance Step";
            case TRANSMUTE, BREAK -> "Celestial Genesis & Consecration Transmutation";
            case AFFLICTION -> "Divine Genesis Brand / Holy Supernova Overload Hex";
            default -> "Celestial Supernova " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK ->
                    "Teleports up to 20m through solid walls & barriers (#FFEA78 / #FF6B9D / #1A5923 / #A88654); sheds 100% of mob aggro, grants true 3D flight & Slow Falling for 6.0s, and leaves behind a blinding Holy Dawn-Decoy that blinds pursuers for 6.0s and explodes in a white-hole flashbang if struck.";
            case TRANSMUTE, BREAK ->
                    "Requires 5.0s continuous channeling: instantly cures Zombie Villagers with maximum trade discounts; purifies Netherrack, Soul Sand & Sculk into Calcite, White Terracotta & Flowering Azalea Moss; turns Rotten Flesh & Spider Eyes into Golden Carrots & Glistering Melons (+25% yield); and purges Curses from gear into divine blessings.";
            case AFFLICTION ->
                    "Milk-immune apocalyptic holy brand dealing continuous true holy burn (2x vs Undead, Demonic & Abyssal), blocking all health recovery & positive buffs, inflicting a 50% attack fumble/miss chance, and shattering 40% of armor toughness.";
            default -> "Infuses the effect with Tier 4 Apex Celestial starlight-prana resonance (#FFEA78 / #FF6B9D / #1A5923 / #A88654).";
        };
    }

    // =========================================================================
    // PART 1 — FLOW MUTATIONS (HOLY WHITE-HOLE STAR-ORBIT FLOW)
    // =========================================================================

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        if (flow != SpellFlowModifier.ORBIT_SATELLITE) {
            return;
        }
        LivingEntity owner = projectile.getOwner() instanceof LivingEntity l ? l : null;
        int vaporized = ApexEschatonSingularityHelper.vaporizeHostileProjectiles(
                level, owner, projectile.getBoundingBox().inflate(2.4D), null, 0L
        );
        if (vaporized > 0 && owner != null && owner.isAlive()) {
            float starlightHeal = Math.min(6.0F, vaporized * 1.5F);
            SpellCombatReactionEngine.notifyMagicalHeal(owner, starlightHeal);
            owner.heal(starlightHeal);
        }
        int qCol = QUAD_CELESTIAL_PALETTE[projectile.tickCount & 3];
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), qCol, 18, 0.88F, 0.04F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.07D, 0.07D, 0.07D, 0.01D
        );
    }

    // =========================================================================
    // PART 2 — DELIVERY FORM MUTATIONS
    // =========================================================================

    // -------------------------------------------------------------------------
    // FORM MUTATION 1 — HOLY SOLAR SINGULARITY DART (PROJECTILE)
    // -------------------------------------------------------------------------

    @Override
    public void onProjectileSpawnedFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        projectile.setNoGravity(true);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        int qCol = QUAD_CELESTIAL_PALETTE[projectile.tickCount & 3];
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), qCol, 18, 0.90F, 0.04F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.08D, 0.08D, 0.08D, 0.02D
        );
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
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        if (hitEntity instanceof LivingEntity le) {
            ApexEschatonSingularityHelper.disableShield(le, 100);
            if (le.isAlive() && !slot.hasHarmVerb()
                    && (caster == null || (!isFriendlyTarget(le, caster)
                    && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(le, caster, wardSafe)))) {
                // 3.5x against Undead, Demonic, and Abyssal entities!
                SpellCombatReactionEngine.hurtWithCelestialMateriaAdvantage(
                        level, le, caster, 5.25F + tier * 1.35F, false, true, 3.50F
                );
            }
        }
        if (projectile.getPierceCountRemaining() <= 0) {
            collapseHolyWhiteHole(level, caster, hitEntity.position(), hitEntity, spell);
        }
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
        if (!projectile.isWallPhasing()) {
            collapseHolyWhiteHole(level, caster, blockHit.getLocation(), null, spell);
        }
    }

    @Override
    public void onProjectileExpiredFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        collapseHolyWhiteHole(level, caster, projectile.position(), null, spell);
    }

    private static void collapseHolyWhiteHole(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 center,
            @Nullable Entity directVictim,
            SpellAssembly spell
    ) {
        // Section 6.52: 6m Holy White-Hole Singularity pulling caught hostiles inward, shattering 5 armor,
        // blinding for 8.0s (160t), and siphoning health to heal the caster and adjacent allies!
        int tier = spell.getEffectiveTier();
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        SpellWorldVfxManager.broadcastRadialNova(level, center, 6.0D, EssenceType.CELESTIAL, EssenceType.RADIANT, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, center.add(0.0D, 0.05D, 0.0D), 6.0D, EssenceType.CELESTIAL, EssenceType.VITAE, 45, tier);

        float siphoned = 0.0F;
        float wellDmg = 4.20F + tier * 1.15F;
        AABB well = new AABB(
                center.x - 6.0D, center.y - 2.5D, center.z - 6.0D,
                center.x + 6.0D, center.y + 4.5D, center.z + 6.0D
        );
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, well, LivingEntity::isAlive)) {
            if (caster != null && isFriendlyTarget(ent, caster)) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, safeguard)) continue;
            Vec3 in = center.subtract(ent.position()).scale(0.40D);
            ent.setDeltaMovement(in.x, 0.22D, in.z);
            ent.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, 5.0D, 0.0D, 180L);
            ent.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 160, 0)); // 8.0s Blindness!
            if (ent != directVictim) {
                SpellCombatReactionEngine.hurtWithCelestialMateriaAdvantage(level, ent, caster, wellDmg, false, true, 3.50F);
            }
            siphoned += 3.5F;
        }
        if (caster != null && siphoned > 0.0F) {
            float healEach = Math.min(14.0F, siphoned);
            for (LivingEntity ally : level.getEntitiesOfClass(
                    LivingEntity.class, caster.getBoundingBox().inflate(8.0D), e -> e.isAlive() && isFriendlyTarget(e, caster))) {
                SpellCombatReactionEngine.notifyMagicalHeal(ally, healEach);
                ally.heal(healEach);
            }
        }
        for (int c = 0; c < 4; c++) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), QUAD_CELESTIAL_PALETTE[c], 22, 0.95F, 0.05F),
                    center.x, center.y + 0.65D, center.z, 8, 1.5D, 0.45D, 1.5D, 0.05D
            );
        }
        level.playSound(null, BlockPos.containing(center), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.45F);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 2 — HOLY PRANIC FUSION BEAM & CATACLYSMIC SUPERNOVA (BEAM)
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
        // Section 6.52 Holy Pranic Fusion Beam & Cataclysmic Supernova:
        // Melts 2 armor/s, blinds hostiles, heals tethered allies, and siphons vitality to refill caster fuel;
        // 5.0 continuous seconds (100t) triggers Cataclysmic Supernova Collapse (-6 armor, 12.0s Blindness,
        // holy gravity vortex -> 10m knockback & blooms restorative Celestial Lilies)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        long now = level.getGameTime();

        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(
                level, caster, caster.getBoundingBox().expandTowards(dir.scale(maxReach)).inflate(1.25D), EssenceType.CELESTIAL, 16L
        );

        for (LivingEntity hit : hitEntities) {
            if (isFriendlyTarget(hit, caster)) {
                CompoundTag fData = EntityHelper.getPersistentData(hit);
                if (fData.getLong(TAG_CELESTIAL_BEAM_LAST_TICK).orElse(-100L) == now) continue;
                fData.putLong(TAG_CELESTIAL_BEAM_LAST_TICK, now);
                SpellCombatReactionEngine.cleanseSingleNegativeEffectOrHex(hit);
                float tetherHeal = (3.5F + tier * 0.45F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.notifyMagicalHeal(hit, tetherHeal);
                hit.heal(tetherHeal);
                level.sendParticles(
                        TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), CELESTIAL_ROSE_PRANA_HEX_COLOR, 18, 0.88F, 0.04F),
                        hit.getX(), hit.getY(0.95D), hit.getZ(), 5, 0.25D, 0.30D, 0.25D, 0.02D
                );
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong(TAG_CELESTIAL_BEAM_LAST_TICK).orElse(-100L);
            if (last == now) continue;
            int step = heldTicks > 0
                    ? ((last >= 0L && now - last <= 45L) ? (int) Math.max(1L, Math.min(24L, now - last)) : Math.min(heldTicks, 4))
                    : 0;
            int accumTicks = heldTicks > 0
                    ? ((now - last <= 45L) ? data.getInt(TAG_CELESTIAL_BEAM_TICKS).orElse(0) + step : step)
                    : 0;
            data.putLong(TAG_CELESTIAL_BEAM_LAST_TICK, now);

            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));

            long nextSiphon = data.getLong(TAG_CELESTIAL_BEAM_NEXT_SIPHON).orElse(0L);
            if (now >= nextSiphon) {
                data.putLong(TAG_CELESTIAL_BEAM_NEXT_SIPHON, now + 20L);
                int armorStacks = Math.min(8, (now - last <= 45L ? data.getInt(TAG_CELESTIAL_BEAM_ARMOR_STACKS).orElse(0) : 0) + 2);
                data.putInt(TAG_CELESTIAL_BEAM_ARMOR_STACKS, armorStacks);
                SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, armorStacks, 0.0D, 80L);
                if (caster instanceof Player p) {
                    SpellFuelService.restorePlayerMateriaFuel(p, EssenceType.CELESTIAL, 36L);
                }
            }

            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithCelestialMateriaAdvantage(
                        level, hit, caster, (3.15F + tier * 0.85F) * Math.max(0.5F, apparatusPotency), false, false
                );
            }

            if (heldTicks >= 100 && accumTicks >= 100) {
                // 5.0 continuous seconds -> Cataclysmic Supernova Collapse!
                data.putInt(TAG_CELESTIAL_BEAM_TICKS, 0);
                data.putInt(TAG_CELESTIAL_BEAM_ARMOR_STACKS, 0);
                Vec3 novaPos = hit.position();
                SpellWorldVfxManager.broadcastRadialNova(level, novaPos, 8.0D, EssenceType.CELESTIAL, EssenceType.RADIANT, tier);

                // Pull nearby hostiles into a holy gravity vortex for 8 ticks, then blast 10m back and bloom Celestial Lilies!
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, hit.getBoundingBox().inflate(7.0D), e -> e.isAlive() && e != caster)) {
                    if (isFriendlyTarget(foe, caster)) continue;
                    if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;
                    Vec3 inward = novaPos.subtract(foe.position()).scale(0.45D);
                    foe.setDeltaMovement(inward.x, 0.20D, inward.z);
                    foe.hasImpulse = true;
                }

                Map<BlockPos, BlockState> bloomedLilies = new HashMap<>();
                BlockPos lilyCenter = hit.blockPosition();
                for (BlockPos p : BlockPos.betweenClosed(lilyCenter.offset(-3, -1, -3), lilyCenter.offset(3, 1, 3))) {
                    double dSq = lilyCenter.distSqr(p);
                    if (dSq >= 2.25D && dSq <= 10.5D
                            && level.getBlockState(p).isAir()
                            && Blocks.LILY_OF_THE_VALLEY.defaultBlockState().canSurvive(level, p)
                            && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, level.getBlockState(p))
                            && level.random.nextFloat() < 0.45F) {
                        BlockPos imm = p.immutable();
                        bloomedLilies.put(imm, level.getBlockState(imm));
                        level.setBlockAndUpdate(imm, Blocks.LILY_OF_THE_VALLEY.defaultBlockState());
                    }
                }

                SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                        caster, novaPos, 7.5D, 160, now, spell, slot, EssenceType.CELESTIAL, apparatusPotency,
                        (srv, zone, elapsed) -> {
                            if (elapsed > zone.maxDurationTicks) {
                                for (Map.Entry<BlockPos, BlockState> entry : bloomedLilies.entrySet()) {
                                    if (srv.getBlockState(entry.getKey()).is(Blocks.LILY_OF_THE_VALLEY)) {
                                        srv.setBlockAndUpdate(entry.getKey(), entry.getValue());
                                    }
                                }
                                return false;
                            }
                            LivingEntity owner = zone.resolveCaster(srv);
                            boolean zSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                            int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;
                            if (elapsed == 8) {
                                SpellWorldVfxManager.broadcastRadialNova(srv, zone.center, 8.0D, EssenceType.CELESTIAL, EssenceType.VITAE, zTier);
                                float supernovaDmg = (14.2F + zTier * 2.55F) * Math.max(0.5F, zone.apparatusPotency);
                                AABB blast = new AABB(
                                        zone.center.x - 7.5D, zone.center.y - 2.5D, zone.center.z - 7.5D,
                                        zone.center.x + 7.5D, zone.center.y + 4.5D, zone.center.z + 7.5D
                                );
                                for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, blast, e -> e.isAlive() && e != owner)) {
                                    if (owner != null && isFriendlyTarget(foe, owner)) continue;
                                    if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zSafe)) continue;
                                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 6.0D, 0.0D, 220L);
                                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 240, 0)); // 12.0s!
                                    Vec3 out = foe.position().subtract(zone.center);
                                    Vec3 normOut = out.lengthSqr() > 1.0E-4D ? out.normalize() : dir;
                                    foe.setDeltaMovement(normOut.x * 2.12D, 0.46D, normOut.z * 2.12D); // 10m KB!
                                    foe.hasImpulse = true;
                                    SpellCombatReactionEngine.hurtWithCelestialMateriaAdvantage(srv, foe, owner, supernovaDmg, false, true);
                                }
                                srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.35F);
                            } else if (elapsed % 20 == 0) {
                                // Restorative Celestial Lily circle heals allies!
                                AABB lilyBox = new AABB(
                                        zone.center.x - 5.0D, zone.center.y - 1.5D, zone.center.z - 5.0D,
                                        zone.center.x + 5.0D, zone.center.y + 3.0D, zone.center.z + 5.0D
                                );
                                for (LivingEntity ally : srv.getEntitiesOfClass(LivingEntity.class, lilyBox, LivingEntity::isAlive)) {
                                    if (owner != null && isFriendlyTarget(ally, owner)) {
                                        SpellCombatReactionEngine.notifyMagicalHeal(ally, 3.0F);
                                        ally.heal(3.0F);
                                    }
                                }
                            }
                            return true;
                        }
                ));
            } else {
                data.putInt(TAG_CELESTIAL_BEAM_TICKS, accumTicks);
            }
        }
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 3 — HOLY SUPERNOVA DAWN-SHOCKWAVE (RADIAL BURST)
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
        // Section 6.52 Holy Supernova Dawn-Shockwave:
        // 10m burst (strictly NO physical light blocks!), 8m knockback, vaporizes hostile projectiles & necrotic summons,
        // shatters 5 armor, and cloaks party in Celestial Ascendance Shroud for 15.0s (300t)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        double r = Math.max(10.0D, radius);
        AABB box = new AABB(
                center.x - r, center.y - 2.5D, center.z - r,
                center.x + r, center.y + 4.5D, center.z + r
        );

        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(level, caster, box, EssenceType.CELESTIAL, 16L);
        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.CELESTIAL, EssenceType.RADIANT, tier);

        long shroudUntil = level.getGameTime() + 300L; // 15.0s!
        float novaDmg = (5.40F + tier * 1.30F) * Math.max(0.5F, apparatusPotency);

        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (isFriendlyTarget(ent, caster)) {
                EntityHelper.getPersistentData(ent).putLong(SpellLingeringZoneManager.TAG_CELESTIAL_ASCENDANCE_UNTIL, shroudUntil);
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ent, level, 10.0D, 5.0D, 0.0D, 0.0D, 300L);
                SpellCombatReactionEngine.syncEntityBarrierState(level, ent, 0.65F, null);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, wardSafe)) continue;

            // Vaporize hostile necrotic summons!
            if (ent instanceof SpellEidolonEntity enemyEid && !enemyEid.isFriendlyTo(caster)) {
                EssenceType eMat = enemyEid.getEffectiveMateria();
                if (eMat == EssenceType.UNDEAD || eMat == EssenceType.BLIGHT || eMat == EssenceType.WRAITH
                        || eMat == EssenceType.BARROW || eMat == EssenceType.MIASMA || eMat == EssenceType.OBLIVION
                        || eMat == EssenceType.ENTROPIC) {
                    level.sendParticles(ParticleTypes.END_ROD, enemyEid.getX(), enemyEid.getY(0.8D), enemyEid.getZ(), 18, 0.4D, 0.5D, 0.4D, 0.06D);
                    enemyEid.discard();
                    continue;
                }
            }

            Vec3 diff = ent.position().subtract(center);
            Vec3 away = (diff.lengthSqr() > 1.0E-4D ? diff.normalize() : new Vec3(0.0D, 0.3D, 1.0D)).scale(1.88D); // 8m KB!
            ent.setDeltaMovement(away.x, 0.42D, away.z);
            ent.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, 5.0D, 0.0D, 180L);
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithCelestialMateriaAdvantage(level, ent, caster, novaDmg, false, true);
            }
        }

        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_CELESTIAL_ASCENDANCE_UNTIL, shroudUntil);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 10.0D, 5.0D, 0.0D, 0.0D, 300L);
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.70F, null);
        for (int c = 0; c < 4; c++) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), QUAD_CELESTIAL_PALETTE[c], 22, 0.95F, 0.05F),
                    center.x, center.y + 1.0D, center.z, 10, 2.5D, 0.5D, 2.5D, 0.05D
            );
        }
        level.playSound(null, BlockPos.containing(center), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.95F, 1.30F);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 4 — HOLY SUPERNOVA CLEAVE / WHITE-HOLE TOUCH (STRIKE)
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
        // Section 6.52 Holy Supernova Cleave / White-Hole Touch:
        // Bypasses 50% armor, shatters 5 armor, disables shields 5.0s (100t), leeches 35% of damage dealt as health/fuel
        // to caster & adjacent allies, launches 6m backward & inflicts Holy Blindness + Holy Brand!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        Vec3 push = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D), push, 4.2F, EssenceType.CELESTIAL, EssenceType.VITAE, tier
        );

        float cleaveDmg = (5.60F + tier * 1.40F) * Math.max(0.5F, apparatusPotency);
        float totalDealt = 0.0F;
        for (LivingEntity hit : hitEntities) {
            if (isFriendlyTarget(hit, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 5.0D, 0.0D, 180L);
            ApexEschatonSingularityHelper.disableShield(hit, 100);
            hit.setDeltaMovement(push.x * 1.48D, 0.36D, push.z * 1.48D); // 6m KB!
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 160, 0));
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_VULNERABLE_25_UNTIL, level.getGameTime() + 100L);
            if (!slot.hasHarmVerb()) {
                totalDealt += SpellCombatReactionEngine.hurtWithCelestialMateriaAdvantage(level, hit, caster, cleaveDmg, true, true);
            } else {
                totalDealt += cleaveDmg;
            }
        }

        if (totalDealt > 0.0F) {
            float partyHeal = Math.max(2.0F, totalDealt * 0.35F);
            for (LivingEntity ally : level.getEntitiesOfClass(
                    LivingEntity.class, caster.getBoundingBox().inflate(8.0D), e -> e.isAlive() && isFriendlyTarget(e, caster))) {
                SpellCombatReactionEngine.notifyMagicalHeal(ally, partyHeal);
                ally.heal(partyHeal);
            }
            if (caster instanceof Player p) {
                SpellCombatReactionEngine.siphonCasterMateriaFuelFromDamage(p, EssenceType.CELESTIAL, tier, totalDealt, 0.35F);
            }
        }
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 5 — HOLY CELESTIAL SUPERNOVA SHROUD (AURA / SELF)
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
        // Strictly NO physical light blocks!
        int dur = 240 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_CELESTIAL_SHROUD_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 10.0D, 5.0D, 0.0D, 0.0D, dur);
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.70F, null);
        for (int c = 0; c < 4; c++) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), QUAD_CELESTIAL_PALETTE[c], 24, 0.95F, 0.05F),
                    caster.getX(), caster.getY(0.95D), caster.getZ(), 7, 0.55D, 0.65D, 0.55D, 0.04D
            );
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.95F, 1.35F);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 6 — HOLY WHITE-HOLE SIGIL (GLYPH / TRAP)
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
        // Section 6.52 Holy White-Hole Sigil:
        // Disarmable Single/Repeating white-gold star-mandala glyph on any floor, wall, or ceiling;
        // Ally step -> instant 10-heart (20.0 HP) healing, Regeneration III & debuff cleansing;
        // Hostile step within 4m -> 10m holy gravitational inversion vortex dragging enemies inward, launching 12m airborne,
        // shattering 5 armor, and blinding for 10.0s (200t) before slamming them into the ground!
        int tier = spell.getEffectiveTier();
        double sigilRad = Math.max(4.0D, radius);
        Vec3 anchored = snapIfNearFloor(level, glyphCenter);
        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE / 4 : 12_000;
        final int[] triggersLeft = {repeating ? 4 : 1};
        final int[] rearmCooldown = {0};

        SpellWorldVfxManager.broadcastArcaneSeal(level, anchored.add(0.0D, 0.04D, 0.0D), sigilRad, EssenceType.CELESTIAL, EssenceType.RADIANT, 240, tier);

        SpellLingeringZoneManager.ActiveSpellZone glyphZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, anchored, sigilRad, maxDuration, level.getGameTime(), spell, slot, EssenceType.CELESTIAL, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();

                    if (elapsed > 0 && elapsed % 200 == 0) {
                        int rem = zone.maxDurationTicks >= 1_000_000_000 ? 240 : (int) Math.min(240L, zone.maxDurationTicks - elapsed);
                        SpellWorldVfxManager.broadcastArcaneSeal(
                                srv, zone.center.add(0.0D, 0.04D, 0.0D), zone.radius, EssenceType.CELESTIAL, EssenceType.RADIANT, rem, tier
                        );
                    }
                    if (elapsed % 10 == 0) {
                        int qCol = QUAD_CELESTIAL_PALETTE[(int) ((elapsed / 10) & 3)];
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), qCol, 18, 0.88F, 0.03F),
                                zone.center.x, zone.center.y + 0.15D, zone.center.z, 4, 0.95D, 0.06D, 0.95D, 0.01D
                        );
                    }
                    if (elapsed < 20) return true; // 1.0s arming delay
                    if (rearmCooldown[0] > 0) {
                        rearmCooldown[0]--;
                        return true;
                    }

                    AABB trig = new AABB(
                            zone.center.x - 4.0D, zone.center.y - 2.0D, zone.center.z - 4.0D,
                            zone.center.x + 4.0D, zone.center.y + 3.0D, zone.center.z + 4.0D
                    );
                    List<LivingEntity> steppers = srv.getEntitiesOfClass(LivingEntity.class, trig, LivingEntity::isAlive);
                    if (steppers.isEmpty()) return true;

                    // Check hostile trigger first if hostiles are present; otherwise check wounded/debuffed or stepping allies!
                    boolean hasHostileStepper = false;
                    LivingEntity allyStepper = null;
                    for (LivingEntity st : steppers) {
                        if (owner != null && isFriendlyTarget(st, owner)) {
                            if (st != owner || st.getHealth() < st.getMaxHealth() || elapsed >= 45) {
                                allyStepper = st;
                            }
                        } else if (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(st, owner, wardSafe)) {
                            hasHostileStepper = true;
                        }
                    }

                    if (!hasHostileStepper && allyStepper != null) {
                        triggersLeft[0]--;
                        rearmCooldown[0] = 80;
                        SpellWorldVfxManager.broadcastRadialNova(srv, zone.center, 5.0D, EssenceType.CELESTIAL, EssenceType.VITAE, tier);
                        for (LivingEntity ally : steppers) {
                            if (owner != null && isFriendlyTarget(ally, owner)) {
                                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(ally);
                                SpellCombatReactionEngine.notifyMagicalHeal(ally, 20.0F); // Instant 10-heart heal!
                                ally.heal(20.0F);
                                ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 2)); // Regen III!
                            }
                        }
                        srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.45F);
                        return triggersLeft[0] > 0;
                    }

                    if (!hasHostileStepper) {
                        return true;
                    }

                    triggersLeft[0]--;
                    rearmCooldown[0] = 80;
                    long now = srv.getGameTime();
                    SpellWorldVfxManager.broadcastRadialNova(srv, zone.center, 10.0D, EssenceType.CELESTIAL, EssenceType.RADIANT, tier);

                    AABB vortex = new AABB(
                            zone.center.x - 10.0D, zone.center.y - 2.5D, zone.center.z - 10.0D,
                            zone.center.x + 10.0D, zone.center.y + 5.5D, zone.center.z + 10.0D
                    );
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, vortex, e -> e.isAlive() && e != owner)) {
                        if (owner != null && isFriendlyTarget(foe, owner)) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) continue;
                        Vec3 in = zone.center.subtract(foe.position()).scale(0.38D);
                        foe.setDeltaMovement(in.x, 1.52D, in.z); // 12m airborne launch!
                        foe.hasImpulse = true;
                        SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 5.0D, 0.0D, 180L);
                        foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0)); // 10.0s Blindness!
                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, foe, new Vec3(0.0D, 1.0D, 0.0D),
                                    zone.spell, zone.slot, EssenceType.CELESTIAL, zone.apparatusPotency
                            );
                        }
                    }
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.90F, 1.35F);

                    // Ground slam 36 ticks later!
                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 10.0D, 36, now, zone.spell, zone.slot, EssenceType.CELESTIAL, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl < 36) return true;
                                LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                boolean sSafe = sZone.spell != null && sZone.spell.hasWardFriendlyFireSafeguard();
                                int sTier = sZone.spell != null ? sZone.spell.getEffectiveTier() : tier;
                                float slamDmg = (10.8F + sTier * 2.20F) * Math.max(0.5F, sZone.apparatusPotency);
                                SpellWorldVfxManager.broadcastRadialNova(sLvl, sZone.center, 10.0D, EssenceType.CELESTIAL, EssenceType.NATURE, sTier);
                                AABB slam = new AABB(
                                        sZone.center.x - 10.5D, sZone.center.y - 2.0D, sZone.center.z - 10.5D,
                                        sZone.center.x + 10.5D, sZone.center.y + 20.0D, sZone.center.z + 10.5D
                                );
                                for (LivingEntity foe : sLvl.getEntitiesOfClass(LivingEntity.class, slam, e -> e.isAlive() && e != sOwner)) {
                                    if (sOwner != null && isFriendlyTarget(foe, sOwner)) continue;
                                    if (sOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, sOwner, sSafe)) continue;
                                    foe.setDeltaMovement(0.0D, -2.35D, 0.0D);
                                    foe.hasImpulse = true;
                                    SpellCombatReactionEngine.hurtWithCelestialMateriaAdvantage(sLvl, foe, sOwner, slamDmg, false, true);
                                }
                                sLvl.playSound(null, BlockPos.containing(sZone.center), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.85F, 1.15F);
                                return false;
                            }
                    ));
                    return triggersLeft[0] > 0;
                }
        ).markDisarmableGlyph();

        SpellLingeringZoneManager.registerZone(level, glyphZone);
        return true;
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 7 — HOLY DAWN-DELUGE CONE (CONE / SPRAY)
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
        // Section 6.52 Holy Dawn-Deluge Cone:
        // Allies cleansed of all debuffs + Regeneration III for 12.0s (240t);
        // Hostiles bypass 50% armor, shatter 5 armor, and suffer Divine Stupefaction / Holy Reverence
        // (25.0s / 500t AI path disruption, player-target wipe, fearful fleeing & blind strikes at demonic/undead allies)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        float coneDmg = (5.0F + tier * 1.25F) * Math.max(0.5F, apparatusPotency);

        for (LivingEntity hit : hitTargets) {
            if (isFriendlyTarget(hit, caster)) {
                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(hit);
                hit.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 240, 2)); // 12.0s Regen III!
                level.sendParticles(
                        TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), CELESTIAL_ROSE_PRANA_HEX_COLOR, 20, 0.90F, 0.04F),
                        hit.getX(), hit.getY(0.95D), hit.getZ(), 6, 0.30D, 0.35D, 0.30D, 0.03D
                );
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 5.0D, 0.0D, 180L);
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 240, 0));
            if (hit instanceof Mob mob) {
                mob.setTarget(null);
                mob.setLastHurtByMob(null);
                mob.setAggressive(false);
            }
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 500);
            if (level.random.nextBoolean()) {
                SpellLingeringZoneManager.applyMirageTraitorFrenzy(hit, 500);
            } else {
                SpellLingeringZoneManager.applyFearFlee(hit, caster.position(), 500);
            }
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithCelestialMateriaAdvantage(level, hit, caster, coneDmg, false, true);
            }
        }
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 8 — CATACLYSMIC SUPERNOVA STARFALL (20.0s + 10.0s, 14m ZONE)
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
        // Section 6.52 Cataclysmic Supernova Starfall:
        // 14m zone, 20.0s (400t) bombardment of 8 falling 3D Skyfall Pseudoblock white-hole starcores,
        // solar genesis meteors & emerald life-spires (3.5x vs Undead/Abyssal, -6 armor & knockdown),
        // followed by a 10.0s (200t) Consecrated White-Hole Singularity Zone pulling & burning hostiles
        // while showering allies in pranic nectar (restoring health, hunger & Celestial Materia fuel)!
        int tier = spell.getEffectiveTier();
        double r = Math.max(14.0D, radius);
        BlockPos centerPos = BlockPos.containing(impactCenter);
        double skyH = SpellDeliveryForm.probeCaveSafeSkyfallHeight(level, centerPos, tier);

        SpellWorldVfxManager.broadcastSkyfallPillar(level, impactCenter, r, EssenceType.CELESTIAL, EssenceType.RADIANT, 405, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, impactCenter.add(0.0D, 0.05D, 0.0D), r, EssenceType.CELESTIAL, EssenceType.VITAE, 600, tier);

        final int spireCount = 8;
        final BlockPos[] spireTargets = new BlockPos[spireCount];
        spireTargets[0] = findGroundSurfaceNear(level, centerPos).above();
        for (int i = 1; i < spireCount; i++) {
            double ang = (Math.PI * 2.0D * (i - 1) / (spireCount - 1.0D)) + level.random.nextDouble() * 0.38D;
            double dist = 0.22D * r + level.random.nextDouble() * (0.60D * r);
            BlockPos raw = BlockPos.containing(impactCenter.x + Math.cos(ang) * dist, impactCenter.y, impactCenter.z + Math.sin(ang) * dist);
            spireTargets[i] = findGroundSurfaceNear(level, raw).above();
        }

        final boolean[] landed = new boolean[spireCount];
        final boolean[] materialized = {false};

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 600, level.getGameTime(), spell, slot, EssenceType.CELESTIAL, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed >= zone.maxDurationTicks) {
                        if (!materialized[0]) {
                            materialized[0] = true;
                            for (int mIdx = 0; mIdx < spireCount; mIdx++) {
                                BlockState pseudoState = switch (mIdx & 3) {
                                    case 0 -> Blocks.QUARTZ_PILLAR.defaultBlockState();
                                    case 1 -> Blocks.CALCITE.defaultBlockState();
                                    case 2 -> Blocks.MOSS_BLOCK.defaultBlockState();
                                    default -> Blocks.AMETHYST_BLOCK.defaultBlockState();
                                };
                                int colHeight = (mIdx % 2 == 0) ? 2 : 1;
                                SpellWorldVfxManager.materializeSkyfallPseudoblockColumn(srv, spireTargets[mIdx], pseudoState, colHeight);
                            }
                        }
                        return false;
                    }

                    boolean bombardment = elapsed < 400;
                    int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;

                    if (bombardment) {
                        // 1. 8 falling 3D white-hole starcore, solar genesis meteor & emerald life-spire pseudoblock columns (zero light blocks)!
                        for (int mIdx = 0; mIdx < spireCount; mIdx++) {
                            int impactTick = 20 + mIdx * 46;
                            BlockPos strikePos = spireTargets[mIdx];
                            BlockState pseudoState = switch (mIdx & 3) {
                                case 0 -> Blocks.QUARTZ_PILLAR.defaultBlockState();
                                case 1 -> Blocks.CALCITE.defaultBlockState();
                                case 2 -> Blocks.MOSS_BLOCK.defaultBlockState();
                                default -> Blocks.AMETHYST_BLOCK.defaultBlockState();
                            };
                            int colHeight = (mIdx % 2 == 0) ? 2 : 1;

                            if (elapsed == impactTick - 14) {
                                int holdTicks = Math.max(4, zone.maxDurationTicks - impactTick);
                                SpellWorldVfxManager.broadcastSkyfallPseudoblockColumn(
                                        srv,
                                        new Vec3(strikePos.getX() + 0.5D, strikePos.getY() + skyH, strikePos.getZ() + 0.5D),
                                        strikePos,
                                        pseudoState,
                                        colHeight,
                                        14,
                                        holdTicks,
                                        EssenceType.CELESTIAL,
                                        zTier
                                );
                            } else if (elapsed == impactTick) {
                                landed[mIdx] = true;
                                Vec3 hitVec = new Vec3(strikePos.getX() + 0.5D, strikePos.getY(), strikePos.getZ() + 0.5D);
                                EssenceType quadSec = switch (mIdx & 3) {
                                    case 0 -> EssenceType.RADIANT;
                                    case 1 -> EssenceType.VITAE;
                                    case 2 -> EssenceType.NATURE;
                                    default -> EssenceType.AIR;
                                };
                                SpellWorldVfxManager.broadcastProjectileImpact(
                                        srv, hitVec.add(0.0D, 0.25D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                                        EssenceType.CELESTIAL, quadSec, 2.50F, zTier
                                );
                                srv.sendParticles(
                                        new BlockParticleOption(ParticleTypes.BLOCK, pseudoState),
                                        hitVec.x, hitVec.y + 0.6D, hitVec.z, 22, 1.0D, 0.5D, 1.0D, 0.09D
                                );
                                srv.playSound(null, strikePos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.95F, 1.25F);
                            }
                        }

                        if (elapsed % 8 == 0) {
                            double ang = srv.random.nextDouble() * Math.PI * 2.0D;
                            double dist = Math.sqrt(srv.random.nextDouble()) * (zone.radius * 0.88D);
                            Vec3 strike = zone.center.add(Math.cos(ang) * dist, 0.25D, Math.sin(ang) * dist);
                            SpellWorldVfxManager.broadcastProjectileImpact(
                                    srv, strike, new Vec3(0.0D, 1.0D, 0.0D),
                                    EssenceType.CELESTIAL, EssenceType.RADIANT, 1.60F, zTier
                            );
                        }
                    }
                    for (int mIdx = 0; mIdx < spireCount; mIdx++) {
                        if (landed[mIdx]) {
                            SpellWorldVfxManager.repelEntitiesFromPseudoblockColumn(srv, spireTargets[mIdx], (mIdx % 2 == 0) ? 2 : 1, false);
                        }
                    }

                    if (elapsed % 8 == 0) {
                        int qCol = QUAD_CELESTIAL_PALETTE[(int) ((elapsed / 8) & 3)];
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), qCol, 22, 0.95F, 0.05F),
                                zone.center.x, zone.center.y + (bombardment ? 4.2D : 0.5D), zone.center.z,
                                20, zone.radius * 0.55D, bombardment ? 2.2D : 0.3D, zone.radius * 0.55D, 0.04D
                        );
                    }

                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        float starfallDmg = (5.0F + zTier * 1.20F) * Math.max(0.5F, zone.apparatusPotency);
                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 8.5D, zone.center.z + zone.radius
                        );
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (owner != null && isFriendlyTarget(ent, owner)) {
                                if (!bombardment) {
                                    SpellCombatReactionEngine.notifyMagicalHeal(ent, 4.0F);
                                    ent.heal(4.0F);
                                    if (ent instanceof Player p) {
                                        p.getFoodData().eat(1, 0.45F);
                                        SpellFuelService.restorePlayerMateriaFuel(p, EssenceType.CELESTIAL, 36L);
                                    }
                                }
                                continue;
                            }
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, owner, wardSafe)) continue;
                            if (bombardment) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(ent, srv, 6.0D, 0.0D, 180L);
                                ent.setDeltaMovement(ent.getDeltaMovement().x * 0.25D, -0.50D, ent.getDeltaMovement().z * 0.25D);
                                ent.hasImpulse = true;
                                if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                                    SpellCombatReactionEngine.hurtWithCelestialMateriaAdvantage(srv, ent, owner, starfallDmg, false, true, 3.50F);
                                }
                                if (zone.spell != null && zone.slot != null) {
                                    SpellExecutionEngine.deliverSubSpellOnEntity(
                                            srv, owner, ent, new Vec3(0.0D, -1.0D, 0.0D),
                                            zone.spell, zone.slot, EssenceType.CELESTIAL, zone.apparatusPotency * 0.50F
                                    );
                                }
                            } else {
                                // 10.0s Consecrated White-Hole Singularity Zone: pulls hostiles inward & burns with continuous holy fire!
                                Vec3 in = zone.center.subtract(ent.position()).scale(0.26D);
                                ent.setDeltaMovement(ent.getDeltaMovement().add(in.x, 0.0D, in.z));
                                ent.hasImpulse = true;
                                ent.igniteForSeconds(3);
                                SpellCombatReactionEngine.hurtWithCelestialMateriaAdvantage(srv, ent, owner, 3.6F, false, true);
                            }
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

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
        tag.putString("SpellEidolonMateria", EssenceType.CELESTIAL.name());
        tag.putInt("SpellEidolonTintRGB", CELESTIAL_STARLIGHT_GOLD_HEX_COLOR);
        for (int c = 0; c < 4; c++) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), QUAD_CELESTIAL_PALETTE[c], 24, 0.95F, 0.05F),
                    spawnPos.x, spawnPos.y + 1.0D, spawnPos.z, 7, 0.50D, 0.60D, 0.50D, 0.04D
            );
        }
    }

    // =========================================================================
    // PART 3 — CORE EFFECT MUTATIONS
    // =========================================================================

    @Override
    public void onBlinkCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 departurePos,
            Vec3 arrivalPos,
            int potencyTier
    ) {
        // Section 6.52 Celestial Supernova Ascendance Step:
        // Teleports up to 20m through solid walls & barriers; sheds 100% of mob aggro, grants true 3D flight + Slow Falling
        // for 6.0s (120t), and leaves behind a blinding Holy Dawn-Decoy (6.0s Blindness + white-hole flashbang if struck)!
        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.CELESTIAL, potencyTier);
        SpellWorldVfxManager.broadcastRadialNova(level, departurePos, 6.0D, EssenceType.CELESTIAL, EssenceType.RADIANT, potencyTier);
        SpellWorldVfxManager.broadcastRadialNova(level, arrivalPos, 5.0D, EssenceType.CELESTIAL, EssenceType.VITAE, potencyTier);

        SpellEidolonEntity.spawnShortLivedDecoy(
                level, caster, departurePos, EssenceType.CELESTIAL,
                24.0F, 120, "Holy Dawn-Decoy", null
        );
        AABB dep = new AABB(
                departurePos.x - 6.0D, departurePos.y - 2.0D, departurePos.z - 6.0D,
                departurePos.x + 6.0D, departurePos.y + 3.5D, departurePos.z + 6.0D
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, dep, e -> e.isAlive() && e != caster)) {
            if (isFriendlyTarget(foe, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0));
        }

        SpellLingeringZoneManager.grantArcane3DFlight(caster, 120);
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 140, 0));
        ApexEschatonSingularityHelper.shedMobAggro(level, caster, arrivalPos, 26.0D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.40F);
    }

    @Override
    public boolean onTransmuteBlock(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            BlockState state,
            int potencyTier
    ) {
        // Section 6.52 Celestial Genesis & Consecration Transmutation [5.0s Channel]:
        // Instantly cures Zombie Villagers with maximum trade discounts; purifies Netherrack, Soul Sand & Sculk into
        // Calcite, White Terracotta & Flowering Azalea Moss; turns Rotten Flesh & Spider Eyes into Golden Carrots & Glistering Melons;
        // and permanently purges Curses from held weapons & armor into divine blessings!
        if (ApexEschatonSingularityHelper.beginOrExecuteChanneledTransmute(
                level, caster, pos, 8, potencyTier, EssenceType.CELESTIAL,
                "Celestial Genesis & Consecration Transmutation",
                () -> {
                    ApexEschatonSingularityHelper.cureZombieVillagersWithMaxDiscounts(level, caster, pos, 8.0D);
                    if (caster != null) {
                        ApexEschatonSingularityHelper.purgeCursesAndElevateEquipment(caster, 1, 1.0F, 1);
                    }
                })) {
            return true;
        }

        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return false;
        }
        if (state.is(Blocks.NETHERRACK) || state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL)
                || state.is(Blocks.SCULK) || state.is(Blocks.BLACKSTONE) || state.is(Blocks.CRYING_OBSIDIAN)) {
            int pick = level.random.nextInt(3);
            BlockState out = switch (pick) {
                case 0 -> Blocks.CALCITE.defaultBlockState();
                case 1 -> Blocks.WHITE_TERRACOTTA.defaultBlockState();
                default -> Blocks.MOSS_BLOCK.defaultBlockState();
            };
            level.setBlockAndUpdate(pos, out);
            if (pick == 2 && level.getBlockState(pos.above()).isAir() && level.random.nextBoolean()
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, pos.above(), level.getBlockState(pos.above()))) {
                level.setBlockAndUpdate(pos.above(), Blocks.FLOWERING_AZALEA.defaultBlockState());
            }
            return true;
        }
        if (state.is(Blocks.WITHER_ROSE)) {
            level.setBlockAndUpdate(pos, Blocks.LILY_OF_THE_VALLEY.defaultBlockState());
            return true;
        }
        return false;
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

        if (ApexEschatonSingularityHelper.purgeCursesAndElevateStack(stack, level.random, 1, 1.0F, 1)) {
            itemEntity.setItem(stack.copy());
            return true;
        }
        if (stack.is(Items.ROTTEN_FLESH)) {
            int outCount = Math.min(64, Math.max(1, (int) Math.ceil(stack.getCount() * 1.25D)));
            itemEntity.setItem(new ItemStack(Items.GOLDEN_CARROT, outCount));
            return true;
        }
        if (stack.is(Items.SPIDER_EYE) || stack.is(Items.FERMENTED_SPIDER_EYE)) {
            int outCount = Math.min(64, Math.max(1, (int) Math.ceil(stack.getCount() * 1.25D)));
            itemEntity.setItem(new ItemStack(Items.GLISTERING_MELON_SLICE, outCount));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.52 Divine Genesis Brand / Holy Supernova Overload Hex:
        // Continuous true holy burn (2x vs Undead, Demonic & Abyssal), disables all health recovery & positive buffs,
        // 50% attack fumble/miss chance (in SpellCombatReactionEngine), and shatters 40% of armor toughness!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        ApexEschatonSingularityHelper.purgeBeneficialBuffs(target);
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.85D, 0.40D * mult), 45L);
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 35, 0, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 45, 0, true, true));

        float base = (2.15F + potencyTier * 0.55F) * mult;
        boolean doubleSmite = ApexEschatonSingularityHelper.isDarkUndeadOrAbyssal(target)
                || target.getType().is(EntityTypeTags.UNDEAD);
        target.hurtServer(level, level.damageSources().fellOutOfWorld(), doubleSmite ? base * 2.0F : base);

        int qCol = QUAD_CELESTIAL_PALETTE[(int) ((level.getGameTime() / 10L) & 3L)];
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), qCol, 18, 0.90F, 0.04F),
                target.getX(), target.getY(0.9D), target.getZ(), 6, 0.30D, 0.35D, 0.30D, 0.03D
        );
        return true;
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private static boolean isFriendlyTarget(LivingEntity target, LivingEntity caster) {
        if (target == caster) return true;
        if (target.isAlliedTo(caster)) return true;
        if (target instanceof SpellEidolonEntity eidolon && eidolon.isFriendlyTo(caster)) return true;
        return target instanceof Player && caster instanceof Player && !caster.canAttack(target);
    }

    private static Vec3 snapIfNearFloor(ServerLevel level, Vec3 pos) {
        BlockPos bp = BlockPos.containing(pos);
        if (!level.getBlockState(bp.above()).isAir() && level.getBlockState(bp.below()).isAir()) {
            return pos;
        }
        for (int dy = 0; dy <= 3; dy++) {
            BlockPos check = bp.below(dy);
            if (!level.getBlockState(check).isAir() && level.getBlockState(check.above()).isAir()) {
                return new Vec3(pos.x, check.getY() + 1.01D, pos.z);
            }
        }
        return pos;
    }

    private static BlockPos findGroundSurfaceNear(ServerLevel level, BlockPos start) {
        for (int dy = 2; dy >= -8; dy--) {
            BlockPos p = start.offset(0, dy, 0);
            if (!level.getBlockState(p).isAir() && level.getBlockState(p.above()).isAir()) {
                return p;
            }
        }
        return start;
    }
}
