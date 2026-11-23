package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModEffects;
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
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.spell.vfx.SpellWorldVfxManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
 * Complete per-part Signature Mutation implementation for <b>Entropic Materia (Tier 4 Apex Fusion #49)</b>
 * (Spellcrafting System Guide Section 6.51 — Void + Umbral + Undead + Blood catalyzed by Chimera):
 * <ul>
 *     <li><b>Palette</b>: Void Deep Indigo ({@code #41304E}) + Umbral Shadow Charcoal ({@code #211A21}) + Undead Grave Slate ({@code #4D6453}) + Blood Crimson ({@code #8A0303})</li>
 *     <li><b>Advantage</b>: Strong vs <b>AIR (+1)</b>, <b>RADIANT (+1)</b>, <b>NATURE (+1)</b>, <b>FROZEN (+1)</b>, <b>CELESTIAL (+1)</b>, <b>AEGIS (+1)</b>, and <b>AURORA (+1)</b> |
 *         Weak vs <b>EARTH (-1)</b>, <b>VOID (-1)</b>, <b>RADIANT (-1)</b>, <b>NETHER (-1)</b>, and <b>UMBRAL (-1)</b></li>
 * </ul>
 */
public final class EntropicSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int ENTROPIC_VOID_INDIGO_HEX_COLOR = 0x41304E;
    public static final int ENTROPIC_UMBRAL_CHARCOAL_HEX_COLOR = 0x211A21;
    public static final int ENTROPIC_UNDEAD_SLATE_HEX_COLOR = 0x4D6453;
    public static final int ENTROPIC_BLOOD_CRIMSON_HEX_COLOR = 0x8A0303;

    private static final int[] QUAD_ENTROPIC_PALETTE = {
            ENTROPIC_VOID_INDIGO_HEX_COLOR,
            ENTROPIC_UMBRAL_CHARCOAL_HEX_COLOR,
            ENTROPIC_UNDEAD_SLATE_HEX_COLOR,
            ENTROPIC_BLOOD_CRIMSON_HEX_COLOR
    };

    private static final String TAG_ENTROPIC_BEAM_LAST_TICK = "EntropicBeamLastTick";
    private static final String TAG_ENTROPIC_BEAM_TICKS = "EntropicBeamContactTicks";
    private static final String TAG_ENTROPIC_BEAM_ARMOR_STACKS = "EntropicBeamArmorStacks";
    private static final String TAG_ENTROPIC_BEAM_NEXT_SIPHON = "EntropicBeamNextSiphonTick";
    private static final String TAG_ENTROPIC_HEX_NEXT_LEECH = "EntropicHexNextLeechTick";

    @Override
    public EssenceType materiaType() {
        return EssenceType.ENTROPIC;
    }

    // =========================================================================
    // TITLES & CODEX / TOOLTIP DESCRIPTIONS
    // =========================================================================

    @Override
    public @Nullable String resolveFlowTitle(SpellFlowModifier flow) {
        return flow == SpellFlowModifier.CHAIN_LEAP ? "Abyssal Sanguine-Void Chain Flow" : null;
    }

    @Override
    public @Nullable String resolveFlowDescription(SpellFlowModifier flow) {
        return flow == SpellFlowModifier.CHAIN_LEAP
                ? "Modifies projectile and strike spells so tendrils of dark matter, necrotic shadow & boiling blood (#41304E / #211A21 / #4D6453 / #8A0303) leap through solid walls to up to 5 nearby hostiles within 8m; each chained target takes 70% initial damage, loses 3 armor, has healing suppressed, and siphons health back to the caster."
                : null;
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
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE ->
                    "Hurls a wall-phasing orb of collapsed dark matter, necrotic mist & boiling sanguine tendrils (#41304E / #211A21 / #4D6453 / #8A0303) piercing up to 6 entities and bypassing 50% armor before collapsing into a 7m Thermodynamic Singularity Well (vacuum pull, -5 armor, 6.0s Wither III + Hemorrhage & 30% lifesteal).";
            case BEAM ->
                    "Channels a wall-phasing beam of void darkness, necrotic ash & crimson blood mist (#41304E / #211A21 / #4D6453 / #8A0303) melting 2 armor/s, siphoning 1 heart/s, and disintegrating incoming arrows; after 5.0s continuous channeling triggers an Apex Event Horizon Cataclysm (12m knockback, -6 armor, buff purge & 5.0s Blindness + Wither III + Slowness IV).";
            case RADIAL_BURST ->
                    "10m wall-penetrating Entropic Singularity Implosion Nova (zero physical light blocks): vacuums hostiles inward before blasting them 10m outward, shatters 5 armor, and grants Apex Entropic Shroud (+10 Armor, +5 Toughness, Wither/Poison/Blindness immunity, 40% melee reflect & 25% attack lifesteal for 12.0s).";
            case TOUCH_STRIKE ->
                    "Strikes with a colossal wall-cleaving scythe blade (#41304E / #211A21 / #4D6453 / #8A0303) bypassing 50% armor, shattering 5 armor, disabling shields for 5.0s, siphoning 35% of damage dealt as caster health, and leaving a 5.0s light-devouring rot fissure (-60% speed & Wither II).";
            case AURA ->
                    "Abyssal Event Horizon Mantle (#41304E / #211A21 / #4D6453 / #8A0303): snuffs torches within 6m, reduces mob detection by 80%, grants +10 Armor, +5 Toughness & Wither/Poison/Blindness/Darkness immunity, vaporizes incoming arrows & tridents within 4m into ash, and drains 1.5 hearts/s from nearby foes to heal the caster.";
            case GLYPH ->
                    "Disarmable Single/Repeating Entropic Singularity Collapse Sigil on any floor, wall, or ceiling: 4m hostile approach detonates an 8m gravitational singularity vacuuming mobs inward, launching them 12m upward, shattering 5 armor, purging buffs, and inflicting Wither III, Hemorrhage & Blindness for 5.0s before slamming them down.";
            case CONE ->
                    "Wall-penetrating Entropic Oblivion-Flame Deluge (#41304E / #211A21 / #4D6453 / #8A0303): shatters 5 armor, incinerates wooden blocks, boils water away, and inflicts Thermodynamic Rot-Frenzy (25.0s AI disruption causing mobs to attack everything around them in blind panic).";
            case SKYFALL ->
                    "Extended 20.0s Cataclysmic Entropic Skyfall across a 14m zone (#41304E / #211A21 / #4D6453 / #8A0303): rains 3D black-hole singularities, blood meteors & necrotic monoliths knocking hostiles flat and shattering 6 armor, leaving a 10.0s blood-slag crater (-70% speed, Wither III & 2.0 hearts/s life-drain to caster).";
            case SUMMON_AGENT ->
                    "Entropic Dread-Titan (45s lifecycle / 90 HP): wall-phasing dark behemoth delivering sweeping scythe cleaves that bypass 50% armor, shatter 3 armor per strike, siphon 35% damage as health, and knock foes 8m back.";
            case SUMMON_SENTRY ->
                    "Apex Event Horizon Monolith (45s lifecycle / 100 HP): anchored monolithic obelisk bombarding hostiles up to 18m away with wall-penetrating singularity mortars (-2 armor/hit) while pulsing a 10m Decay Field (draining 1.5 hearts/s, -1 armor/s & blinding hostiles).";
            case SUMMON_DECOY ->
                    "Smoldering Void-Husk Effigy (14s duration): 16m taunt bone-and-darkfire effigy reflecting 40% of all incoming damage and detonating into a 12m entropic dark matter burst (-5 armor, Wither III & Blindness for 5.0s).";
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

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK ->
                    "Teleports up to 20m through solid walls & barriers (#41304E / #211A21 / #4D6453 / #8A0303); departure collapses into an 8m black hole pulling foes inward (-4 armor & 4.0s Blindness + Wither II), while arrival grants 8.0s of complete physical invulnerability and sheds 100% of mob aggro.";
            case TRANSMUTE, BREAK ->
                    "Requires 5.0s continuous channeling: corrupts Stone/Cobblestone into Blackstone, Deepslate, or Crying Obsidian; Dirt/Grass into Soul Sand or Sculk; plants into Wither Roses; mob drops into Dark Blood & Void Essence (+25% yield); and damaged metal gear into raw ingots/nuggets & slag (+35% yield).";
            case AFFLICTION ->
                    "Milk-immune entropic curse cutting Max Health by 30%, blocking all natural & potion healing, shredding 50% armor toughness, leeching 1.5 hearts every 3.0s to the caster, and triggering painful bone spasms on movement/attacks; Apex Heat-Death Influx spawns cosmic void horrors across 32m.";
            default -> "Infuses the effect with Tier 4 Apex Entropic singularity resonance (#41304E / #211A21 / #4D6453 / #8A0303).";
        };
    }

    // =========================================================================
    // PART 1 — FLOW MUTATIONS (ABYSSAL SANGUINE-VOID CHAIN FLOW)
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
        // Section 6.51 Abyssal Sanguine-Void Chain Flow:
        // Leaps through solid walls to up to 5 nearby hostiles within 8m (70% damage in SpellExecutionEngine);
        // each chained target has 3 armor shattered, healing completely suppressed, and siphons health back to caster!
        long now = level.getGameTime();
        CompoundTag tData = EntityHelper.getPersistentData(toTarget);
        SpellCombatReactionEngine.applyTimedAttributeShred(toTarget, level, 3.0D, 0.0D, 160L);
        tData.putLong(SpellLingeringZoneManager.TAG_BLOOD_HEMORRHAGE_UNTIL, now + 140L);
        tData.putLong(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_UNTIL, now + 140L);
        tData.putFloat(SpellLingeringZoneManager.TAG_ENTANGLING_MIASMA_MAX_HP, toTarget.getHealth());

        float siphon = 2.5F + spell.getEffectiveTier() * 0.35F;
        if (caster.isAlive()) {
            SpellCombatReactionEngine.notifyMagicalHeal(caster, siphon);
            caster.heal(siphon);
        }
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_BLOOD.get(), ENTROPIC_BLOOD_CRIMSON_HEX_COLOR, 20, 0.90F, 0.05F),
                toTarget.getX(), toTarget.getY(0.8D), toTarget.getZ(), 8, 0.28D, 0.30D, 0.28D, 0.03D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SINGULARITY.get(), ENTROPIC_VOID_INDIGO_HEX_COLOR, 20, 0.88F, 0.05F),
                toTarget.getX(), toTarget.getY(0.65D), toTarget.getZ(), 6, 0.24D, 0.26D, 0.24D, 0.03D
        );
    }

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        int qCol = QUAD_ENTROPIC_PALETTE[projectile.tickCount & 3];
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SINGULARITY.get(), qCol, 16, 0.84F, 0.04F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.02D
        );
    }

    // =========================================================================
    // PART 2 — DELIVERY FORM MUTATIONS
    // =========================================================================

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
        int qCol = QUAD_ENTROPIC_PALETTE[projectile.tickCount & 3];
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), qCol, 18, 0.88F, 0.04F),
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
        if (hitEntity instanceof LivingEntity directVictim && directVictim.isAlive()
                && !slot.hasHarmVerb()
                && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(directVictim, caster, wardSafe))) {
            SpellCombatReactionEngine.hurtWithEntropicMateriaAdvantage(
                    level, directVictim, caster, 5.20F + tier * 1.35F, false, true
            );
        }
        if (projectile.getPierceCountRemaining() <= 0) {
            collapseThermodynamicSingularityWell(level, caster, hitEntity.position(), hitEntity, spell);
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
            collapseThermodynamicSingularityWell(level, caster, blockHit.getLocation(), null, spell);
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
        collapseThermodynamicSingularityWell(level, caster, projectile.position(), null, spell);
    }

    private static void collapseThermodynamicSingularityWell(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 center,
            @Nullable Entity directHit,
            SpellAssembly spell
    ) {
        // Section 6.51 Entropic Singularity Skull-Orb:
        // 7m Thermodynamic Singularity Well: vacuums caught hostiles inward, shatters 5 armor points,
        // inflicts Wither III and Hemorrhage for 6.0s (120t), and siphons 30% of total damage dealt back as caster health!
        int tier = spell.getEffectiveTier();
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        long now = level.getGameTime();
        float totalDealt = 0.0F;

        SpellWorldVfxManager.broadcastProjectileImpact(
                level, center.add(0.0D, 0.25D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.ENTROPIC, EssenceType.BLOOD, 2.25F, tier
        );
        SpellWorldVfxManager.broadcastRadialNova(level, center, 7.0D, EssenceType.ENTROPIC, EssenceType.VOID, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, center.add(0.0D, 0.04D, 0.0D), 7.0D, EssenceType.ENTROPIC, EssenceType.BLOOD, 60, tier);

        float wellDmg = 6.20F + tier * 1.45F;
        AABB well = new AABB(
                center.x - 7.0D, center.y - 2.5D, center.z - 7.0D,
                center.x + 7.0D, center.y + 4.5D, center.z + 7.0D
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, well, LivingEntity::isAlive)) {
            if (foe == caster) continue;
            if (caster != null && isFriendlyTarget(foe, caster)) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, safeguard)) continue;
            Vec3 in = center.subtract(foe.position()).scale(0.42D);
            foe.setDeltaMovement(in.x, 0.22D, in.z);
            foe.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 5.0D, 0.0D, 180L);
            foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 2));
            foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.BLEEDING.get()), 120, 0));
            EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_BLOOD_HEMORRHAGE_UNTIL, now + 120L);
            float dealt = (foe == directHit)
                    ? wellDmg * 0.50F
                    : SpellCombatReactionEngine.hurtWithEntropicMateriaAdvantage(level, foe, caster, wellDmg, false, true);
            totalDealt += dealt;
        }
        if (caster != null && caster.isAlive() && totalDealt > 0.0F) {
            float heal = totalDealt * 0.30F;
            SpellCombatReactionEngine.notifyMagicalHeal(caster, heal);
            caster.heal(heal);
        }
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_BLOOD.get(), ENTROPIC_BLOOD_CRIMSON_HEX_COLOR, 24, 1.0F, 0.06F),
                center.x, center.y + 0.6D, center.z, 22, 1.8D, 0.5D, 1.8D, 0.05D
        );
        level.playSound(null, BlockPos.containing(center), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.90F, 0.58F);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 2 — ENTROPIC OBLIVION-SIPHON RAY & EVENT HORIZON CATACLYSM
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
        // Section 6.51 Entropic Oblivion-Siphon Ray & Event Horizon Cataclysm:
        // Melts 2 armor/s, siphons 1 heart/s (2.0 HP/s) back to caster, disintegrates incoming arrows;
        // 5.0s continuous (100t) -> Apex Event Horizon Cataclysm (12m KB, -6 armor, purges buffs, Blindness + Wither III + Slowness IV 5.0s)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(
                level, caster,
                new AABB(caster.getEyePosition(), caster.getEyePosition().add(dir.scale(maxReach))).inflate(2.2D),
                null, 0L
        );

        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (isFriendlyTarget(hit, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong(TAG_ENTROPIC_BEAM_LAST_TICK).orElse(-100L);
            if (last == now) continue;
            long gap = now - last;
            int step = heldTicks > 0
                    ? ((last >= 0L && gap <= 32L) ? (int) Math.max(1L, Math.min(20L, gap)) : Math.min(heldTicks, 4))
                    : 0;
            int accumTicks = heldTicks > 0
                    ? ((gap >= 0L && gap <= 32L) ? data.getInt(TAG_ENTROPIC_BEAM_TICKS).orElse(0) + step : step)
                    : 0;
            int armorStacks = (gap >= 0L && gap <= 32L)
                    ? Math.min(10, Math.max(2, (accumTicks / 20) * 2 + 2))
                    : 2;
            data.putLong(TAG_ENTROPIC_BEAM_LAST_TICK, now);
            data.putInt(TAG_ENTROPIC_BEAM_ARMOR_STACKS, armorStacks);

            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, armorStacks, 0.0D, 90L);
            long nextSiphon = data.getLong(TAG_ENTROPIC_BEAM_NEXT_SIPHON).orElse(0L);
            if (now >= nextSiphon) {
                data.putLong(TAG_ENTROPIC_BEAM_NEXT_SIPHON, now + 20L);
                if (caster.isAlive()) {
                    SpellCombatReactionEngine.notifyMagicalHeal(caster, 2.0F);
                    caster.heal(2.0F);
                }
            }

            if (heldTicks >= 100 && accumTicks >= 100) {
                data.putInt(TAG_ENTROPIC_BEAM_TICKS, 0);
                data.putInt(TAG_ENTROPIC_BEAM_ARMOR_STACKS, 0);
                Vec3 novaPos = hit.position();
                SpellWorldVfxManager.broadcastRadialNova(level, novaPos, 8.0D, EssenceType.ENTROPIC, EssenceType.BLOOD, tier);
                float cataclysmDmg = (14.0F + tier * 2.55F) * Math.max(0.5F, apparatusPotency);
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, hit.getBoundingBox().inflate(7.0D), e -> e.isAlive() && e != caster)) {
                    if (isFriendlyTarget(foe, caster)) continue;
                    if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;
                    ApexEschatonSingularityHelper.purgeBeneficialBuffs(foe);
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 6.0D, 0.0D, 200L);
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                    foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 2));
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 3));
                    Vec3 blastOut = foe == hit ? dir : foe.position().subtract(novaPos);
                    Vec3 normOut = blastOut.lengthSqr() > 1.0E-4D ? blastOut.normalize() : dir;
                    foe.setDeltaMovement(normOut.x * 2.40D, 0.48D, normOut.z * 2.40D); // 12m KB!
                    foe.hasImpulse = true;
                    SpellCombatReactionEngine.hurtWithEntropicMateriaAdvantage(level, foe, caster, cataclysmDmg, false, true);
                }
                level.playSound(null, hit.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.90F, 0.70F);
            } else {
                data.putInt(TAG_ENTROPIC_BEAM_TICKS, accumTicks);
            }
        }
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 3 — ENTROPIC SINGULARITY IMPLOSION NOVA (RADIAL BURST)
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
        // Section 6.51 Entropic Singularity Implosion Nova:
        // Colossal 10m shockwave (zero physical light blocks): violently vacuums hostiles into central perimeter
        // before detonating outward (10m KB, -5 armor), granting Apex Entropic Shroud (12.0s / 240t)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        double r = Math.max(10.0D, radius);
        AABB box = new AABB(
                center.x - r, center.y - 2.5D, center.z - r,
                center.x + r, center.y + 4.5D, center.z + r
        );

        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (isFriendlyTarget(ent, caster)) {
                EntityHelper.getPersistentData(ent).putLong(SpellLingeringZoneManager.TAG_ENTROPIC_LIFESTEAL_SHROUD_UNTIL, level.getGameTime() + 240L);
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ent, level, 10.0D, 5.0D, 0.0D, 0.0D, 240L);
                SpellCombatReactionEngine.syncEntityBarrierState(level, ent, 0.65F, null);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, wardSafe)) continue;
            Vec3 inward = center.subtract(ent.position()).scale(0.46D);
            ent.setDeltaMovement(inward.x, 0.16D, inward.z);
            ent.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, 5.0D, 0.0D, 180L);
        }

        // Brief 10-tick singularity vacuum collapse followed by violent 10m outward blast!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, center, r, 10, level.getGameTime(), spell, slot, EssenceType.ENTROPIC, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed < 10) return true;
                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean zSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                    int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;
                    float novaDmg = (5.40F + zTier * 1.30F) * Math.max(0.5F, zone.apparatusPotency);
                    SpellWorldVfxManager.broadcastRadialNova(srv, zone.center, zone.radius, EssenceType.ENTROPIC, EssenceType.BLOOD, zTier);
                    AABB outBox = new AABB(
                            zone.center.x - zone.radius, zone.center.y - 2.5D, zone.center.z - zone.radius,
                            zone.center.x + zone.radius, zone.center.y + 4.5D, zone.center.z + zone.radius
                    );
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, outBox, e -> e.isAlive() && e != owner)) {
                        if (owner != null && isFriendlyTarget(foe, owner)) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zSafe)) continue;
                        Vec3 diff = foe.position().subtract(zone.center);
                        Vec3 away = (diff.lengthSqr() > 1.0E-4D ? diff.normalize() : new Vec3(0.0D, 0.3D, 1.0D)).scale(2.08D); // 10m KB!
                        foe.setDeltaMovement(away.x, 0.46D, away.z);
                        foe.hasImpulse = true;
                        if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                            SpellCombatReactionEngine.hurtWithEntropicMateriaAdvantage(srv, foe, owner, novaDmg, false, true);
                        }
                    }
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.95F, 0.62F);
                    return false;
                }
        ));

        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_ENTROPIC_LIFESTEAL_SHROUD_UNTIL, level.getGameTime() + 240L);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 10.0D, 5.0D, 0.0D, 0.0D, 240L);
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.70F, null);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 4 — ENTROPIC SINGULARITY CLEAVE (TOUCH / STRIKE)
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
        // Section 6.51 Entropic Singularity Cleave:
        // Bypasses 50% armor, shatters 5 armor, disables shields 5.0s (100t), siphons 35% of damage dealt into caster health,
        // and leaves a 5.0s (100t) fissure of light-devouring rot (-60% speed & Wither II)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        Vec3 fissurePos = hitEntities.isEmpty()
                ? caster.position().add(dir.scale(2.4D))
                : hitEntities.getFirst().position();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D), dir, 4.2F, EssenceType.ENTROPIC, EssenceType.BLOOD, tier
        );

        float cleaveDmg = (5.60F + tier * 1.40F) * Math.max(0.5F, apparatusPotency);
        float totalDealt = 0.0F;
        for (LivingEntity hit : hitEntities) {
            if (isFriendlyTarget(hit, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 5.0D, 0.0D, 180L);
            ApexEschatonSingularityHelper.disableShield(hit, 100);
            if (!slot.hasHarmVerb()) {
                totalDealt += SpellCombatReactionEngine.hurtWithEntropicMateriaAdvantage(level, hit, caster, cleaveDmg, true, true);
            }
        }
        if (totalDealt > 0.0F && caster.isAlive()) {
            float siphon = totalDealt * 0.35F;
            SpellCombatReactionEngine.notifyMagicalHeal(caster, siphon);
            caster.heal(siphon);
        }

        SpellWorldVfxManager.broadcastArcaneSeal(level, fissurePos.add(0.0D, 0.04D, 0.0D), 4.5D, EssenceType.ENTROPIC, EssenceType.UNDEAD, 100, tier);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, fissurePos, 4.5D, 100, level.getGameTime(), spell, slot, EssenceType.ENTROPIC, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 16 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean zSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        AABB rot = new AABB(
                                zone.center.x - 4.5D, zone.center.y - 1.5D, zone.center.z - 4.5D,
                                zone.center.x + 4.5D, zone.center.y + 2.5D, zone.center.z + 4.5D
                        );
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, rot, e -> e.isAlive() && e != owner)) {
                            if (owner != null && isFriendlyTarget(foe, owner)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zSafe)) continue;
                            SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, srv, 0.60D, 30L);
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 3)); // 60% slow
                            foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 45, 1));
                        }
                        int qCol = QUAD_ENTROPIC_PALETTE[(int) ((elapsed / 16) & 3)];
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), qCol, 18, 0.90F, 0.04F),
                                zone.center.x, zone.center.y + 0.25D, zone.center.z, 8, 1.5D, 0.15D, 1.5D, 0.02D
                        );
                    }
                    return true;
                }
        ));
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 5 — ABYSSAL EVENT HORIZON MANTLE (AURA / SELF)
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
        int dur = 240 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_ENTROPIC_SHROUD_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 10.0D, 5.0D, 0.0D, 0.0D, dur);
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.70F, null);
        for (int c = 0; c < 4; c++) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), QUAD_ENTROPIC_PALETTE[c], 24, 0.95F, 0.05F),
                    caster.getX(), caster.getY(0.95D), caster.getZ(), 7, 0.55D, 0.65D, 0.55D, 0.04D
            );
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.9F, 0.75F);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 6 — ENTROPIC SINGULARITY COLLAPSE SIGIL (GLYPH / TRAP)
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
        // Section 6.51 Entropic Singularity Collapse Sigil:
        // Disarmable Single/Repeating void-shadow mandala glyph on any floor, wall, or ceiling;
        // 4m hostile approach -> 8m gravitational singularity vacuuming mobs inward, launching 12m straight up,
        // shattering 5 armor, purging buffs, inflicting Wither III + Hemorrhage + Blindness 5.0s (100t),
        // then slamming them straight down for massive impact damage!
        int tier = spell.getEffectiveTier();
        double sigilRad = Math.max(4.0D, radius);
        Vec3 anchored = snapIfNearFloor(level, glyphCenter);
        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE / 4 : 12_000;
        final int[] triggersLeft = {repeating ? 4 : 1};
        final int[] rearmCooldown = {0};

        SpellWorldVfxManager.broadcastArcaneSeal(level, anchored.add(0.0D, 0.04D, 0.0D), sigilRad, EssenceType.ENTROPIC, EssenceType.BLOOD, 240, tier);

        SpellLingeringZoneManager.ActiveSpellZone glyphZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, anchored, sigilRad, maxDuration, level.getGameTime(), spell, slot, EssenceType.ENTROPIC, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();

                    if (elapsed > 0 && elapsed % 200 == 0) {
                        int rem = zone.maxDurationTicks >= 1_000_000_000 ? 240 : (int) Math.min(240L, zone.maxDurationTicks - elapsed);
                        SpellWorldVfxManager.broadcastArcaneSeal(
                                srv, zone.center.add(0.0D, 0.04D, 0.0D), zone.radius, EssenceType.ENTROPIC, EssenceType.BLOOD, rem, tier
                        );
                    }
                    if (elapsed % 10 == 0) {
                        int qCol = QUAD_ENTROPIC_PALETTE[(int) ((elapsed / 10) & 3)];
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SINGULARITY.get(), qCol, 18, 0.85F, 0.03F),
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
                    if (srv.getEntitiesOfClass(LivingEntity.class, trig, e -> e.isAlive() && e != owner
                            && (owner == null || (!isFriendlyTarget(e, owner)
                            && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe)))).isEmpty()) {
                        return true;
                    }

                    triggersLeft[0]--;
                    rearmCooldown[0] = 80;
                    long now = srv.getGameTime();
                    SpellWorldVfxManager.broadcastRadialNova(srv, zone.center, 8.0D, EssenceType.ENTROPIC, EssenceType.VOID, tier);

                    AABB blast = new AABB(
                            zone.center.x - 8.0D, zone.center.y - 2.5D, zone.center.z - 8.0D,
                            zone.center.x + 8.0D, zone.center.y + 5.5D, zone.center.z + 8.0D
                    );
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, blast, e -> e.isAlive() && e != owner)) {
                        if (owner != null && isFriendlyTarget(foe, owner)) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) continue;
                        ApexEschatonSingularityHelper.purgeBeneficialBuffs(foe);
                        SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 5.0D, 0.0D, 180L);
                        Vec3 in = zone.center.subtract(foe.position()).scale(0.40D);
                        foe.setDeltaMovement(in.x, 1.52D, in.z); // 12m vertical launch!
                        foe.hasImpulse = true;
                        foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 2));
                        foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                        foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.BLEEDING.get()), 100, 0));
                        EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_BLOOD_HEMORRHAGE_UNTIL, now + 100L);
                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, foe, new Vec3(0.0D, 1.0D, 0.0D),
                                    zone.spell, zone.slot, EssenceType.ENTROPIC, zone.apparatusPotency
                            );
                        }
                    }
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 0.85F, 0.72F);

                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 8.0D, 36, now, zone.spell, zone.slot, EssenceType.ENTROPIC, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl < 36) return true;
                                LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                boolean sSafe = sZone.spell != null && sZone.spell.hasWardFriendlyFireSafeguard();
                                int sTier = sZone.spell != null ? sZone.spell.getEffectiveTier() : tier;
                                float slamDmg = (10.8F + sTier * 2.20F) * Math.max(0.5F, sZone.apparatusPotency);
                                SpellWorldVfxManager.broadcastRadialNova(sLvl, sZone.center, 8.0D, EssenceType.ENTROPIC, EssenceType.BLOOD, sTier);
                                AABB slam = new AABB(
                                        sZone.center.x - 8.5D, sZone.center.y - 2.0D, sZone.center.z - 8.5D,
                                        sZone.center.x + 8.5D, sZone.center.y + 20.0D, sZone.center.z + 8.5D
                                );
                                for (LivingEntity foe : sLvl.getEntitiesOfClass(LivingEntity.class, slam, e -> e.isAlive() && e != sOwner)) {
                                    if (sOwner != null && isFriendlyTarget(foe, sOwner)) continue;
                                    if (sOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, sOwner, sSafe)) continue;
                                    foe.setDeltaMovement(0.0D, -2.35D, 0.0D);
                                    foe.hasImpulse = true;
                                    SpellCombatReactionEngine.hurtWithEntropicMateriaAdvantage(sLvl, foe, sOwner, slamDmg, false, true);
                                }
                                sLvl.playSound(null, BlockPos.containing(sZone.center), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.90F, 0.60F);
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
    // FORM MUTATION 7 — ENTROPIC OBLIVION-FLAME DELUGE (CONE / SPRAY)
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
        // Section 6.51 Entropic Oblivion-Flame Deluge:
        // Wall-penetrating cone shattering 5 armor, incinerating wooden blocks, boiling water away,
        // and inflicting Thermodynamic Rot-Frenzy (25.0s / 500t AI disruption + blind frenzy attacking everything around)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        Vec3 eye = caster.getEyePosition();

        for (double d = 1.5D; d <= coneRange; d += 1.8D) {
            BlockPos step = BlockPos.containing(eye.add(dir.scale(d)));
            for (BlockPos p : BlockPos.betweenClosed(step.offset(-1, -1, -1), step.offset(1, 1, 1))) {
                BlockState bs = level.getBlockState(p);
                if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
                if (bs.is(Blocks.WATER)) {
                    level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
                    level.sendParticles(ParticleTypes.CLOUD, p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D, 3, 0.2D, 0.2D, 0.2D, 0.02D);
                } else if ((bs.is(BlockTags.LOGS) || bs.is(BlockTags.PLANKS) || bs.is(BlockTags.LEAVES)) && level.random.nextFloat() < 0.35F) {
                    level.setBlockAndUpdate(p, Blocks.FIRE.defaultBlockState());
                }
            }
        }

        float coneDmg = (5.0F + tier * 1.25F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitTargets) {
            if (isFriendlyTarget(hit, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 5.0D, 0.0D, 180L);
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0));
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 500);
            SpellLingeringZoneManager.applyMirageTraitorFrenzy(hit, 500);
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithEntropicMateriaAdvantage(level, hit, caster, coneDmg, false, true);
            }
        }
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 8 — CATACLYSMIC ENTROPIC SKYFALL (20.0s + 10.0s, 14m ZONE)
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
        // Section 6.51 Cataclysmic Entropic Skyfall:
        // 14m zone, 20.0s (400t) bombardment of 8 falling 3D Skyfall Pseudoblock black-hole singularities,
        // blood meteors & necrotic monoliths knocking hostiles flat & shattering 6 armor points, followed by a
        // 10.0s (200t) bubbling abyssal blood-slag crater (-70% speed, Wither III & 2.0 hearts/s life-drain to caster)!
        int tier = spell.getEffectiveTier();
        double r = Math.max(14.0D, radius);
        BlockPos centerPos = BlockPos.containing(impactCenter);
        double skyH = SpellDeliveryForm.probeCaveSafeSkyfallHeight(level, centerPos, tier);

        SpellWorldVfxManager.broadcastSkyfallPillar(level, impactCenter, r, EssenceType.ENTROPIC, EssenceType.BLOOD, 405, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, impactCenter.add(0.0D, 0.05D, 0.0D), r, EssenceType.ENTROPIC, EssenceType.VOID, 600, tier);

        final int monolithCount = 8;
        final BlockPos[] monolithTargets = new BlockPos[monolithCount];
        monolithTargets[0] = findGroundSurfaceNear(level, centerPos).above();
        for (int i = 1; i < monolithCount; i++) {
            double ang = (Math.PI * 2.0D * (i - 1) / (monolithCount - 1.0D)) + level.random.nextDouble() * 0.38D;
            double dist = 0.22D * r + level.random.nextDouble() * (0.60D * r);
            BlockPos raw = BlockPos.containing(impactCenter.x + Math.cos(ang) * dist, impactCenter.y, impactCenter.z + Math.sin(ang) * dist);
            monolithTargets[i] = findGroundSurfaceNear(level, raw).above();
        }

        final boolean[] landed = new boolean[monolithCount];
        final boolean[] materialized = {false};

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 600, level.getGameTime(), spell, slot, EssenceType.ENTROPIC, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed >= zone.maxDurationTicks) {
                        if (!materialized[0]) {
                            materialized[0] = true;
                            for (int mIdx = 0; mIdx < monolithCount; mIdx++) {
                                BlockState pseudoState = switch (mIdx & 3) {
                                    case 0 -> Blocks.CRYING_OBSIDIAN.defaultBlockState();
                                    case 1 -> Blocks.BLACKSTONE.defaultBlockState();
                                    case 2 -> Blocks.SCULK.defaultBlockState();
                                    default -> Blocks.NETHER_WART_BLOCK.defaultBlockState();
                                };
                                int colHeight = (mIdx % 2 == 0) ? 2 : 1;
                                SpellWorldVfxManager.materializeSkyfallPseudoblockColumn(srv, monolithTargets[mIdx], pseudoState, colHeight);
                            }
                        }
                        return false;
                    }

                    boolean bombardment = elapsed < 400;
                    int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;

                    if (bombardment) {
                        // 1. 8 falling 3D black-hole singularity, blood-meteor & necrotic monolith pseudoblock columns!
                        for (int mIdx = 0; mIdx < monolithCount; mIdx++) {
                            int impactTick = 20 + mIdx * 46;
                            BlockPos strikePos = monolithTargets[mIdx];
                            BlockState pseudoState = switch (mIdx & 3) {
                                case 0 -> Blocks.CRYING_OBSIDIAN.defaultBlockState();
                                case 1 -> Blocks.BLACKSTONE.defaultBlockState();
                                case 2 -> Blocks.SCULK.defaultBlockState();
                                default -> Blocks.NETHER_WART_BLOCK.defaultBlockState();
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
                                        EssenceType.ENTROPIC,
                                        zTier
                                );
                            } else if (elapsed == impactTick) {
                                landed[mIdx] = true;
                                Vec3 hitVec = new Vec3(strikePos.getX() + 0.5D, strikePos.getY(), strikePos.getZ() + 0.5D);
                                EssenceType quadSec = switch (mIdx & 3) {
                                    case 0 -> EssenceType.VOID;
                                    case 1 -> EssenceType.UMBRAL;
                                    case 2 -> EssenceType.UNDEAD;
                                    default -> EssenceType.BLOOD;
                                };
                                SpellWorldVfxManager.broadcastProjectileImpact(
                                        srv, hitVec.add(0.0D, 0.25D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                                        EssenceType.ENTROPIC, quadSec, 2.50F, zTier
                                );
                                srv.sendParticles(
                                        new BlockParticleOption(ParticleTypes.BLOCK, pseudoState),
                                        hitVec.x, hitVec.y + 0.6D, hitVec.z, 22, 1.0D, 0.5D, 1.0D, 0.09D
                                );
                                srv.playSound(null, strikePos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.85F, 0.62F);
                            }
                        }

                        if (elapsed % 8 == 0) {
                            double ang = srv.random.nextDouble() * Math.PI * 2.0D;
                            double dist = Math.sqrt(srv.random.nextDouble()) * (zone.radius * 0.88D);
                            Vec3 strike = zone.center.add(Math.cos(ang) * dist, 0.25D, Math.sin(ang) * dist);
                            SpellWorldVfxManager.broadcastProjectileImpact(
                                    srv, strike, new Vec3(0.0D, 1.0D, 0.0D),
                                    EssenceType.ENTROPIC, EssenceType.BLOOD, 1.60F, zTier
                            );
                        }
                    }
                    for (int mIdx = 0; mIdx < monolithCount; mIdx++) {
                        if (landed[mIdx]) {
                            SpellWorldVfxManager.repelEntitiesFromPseudoblockColumn(srv, monolithTargets[mIdx], (mIdx % 2 == 0) ? 2 : 1, false);
                        }
                    }

                    if (elapsed % 8 == 0) {
                        int qCol = QUAD_ENTROPIC_PALETTE[(int) ((elapsed / 8) & 3)];
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), qCol, 22, 0.95F, 0.05F),
                                zone.center.x, zone.center.y + (bombardment ? 4.2D : 0.5D), zone.center.z,
                                20, zone.radius * 0.55D, bombardment ? 2.2D : 0.3D, zone.radius * 0.55D, 0.04D
                        );
                    }

                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        float stormDmg = (5.0F + zTier * 1.20F) * Math.max(0.5F, zone.apparatusPotency);
                        float drainedTotal = 0.0F;
                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 8.5D, zone.center.z + zone.radius
                        );
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && isFriendlyTarget(foe, owner)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) continue;
                            if (bombardment) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 6.0D, 0.0D, 180L);
                                foe.setDeltaMovement(foe.getDeltaMovement().x * 0.25D, -0.50D, foe.getDeltaMovement().z * 0.25D);
                                foe.hasImpulse = true;
                                SpellCombatReactionEngine.hurtWithEntropicMateriaAdvantage(srv, foe, owner, stormDmg, false, true);
                                if (zone.spell != null && zone.slot != null) {
                                    SpellExecutionEngine.deliverSubSpellOnEntity(
                                            srv, owner, foe, new Vec3(0.0D, -1.0D, 0.0D),
                                            zone.spell, zone.slot, EssenceType.ENTROPIC, zone.apparatusPotency * 0.50F
                                    );
                                }
                            } else {
                                // 10.0s blood-slag & wither-fire crater: -70% slow, Wither III & 2.0 hearts/s (4.0 HP/s) true drain to caster!
                                SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, srv, 0.70D, 35L);
                                foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 4));
                                foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 45, 2));
                                if (foe.hurtServer(srv, srv.damageSources().fellOutOfWorld(), 4.0F)) {
                                    drainedTotal += 4.0F;
                                }
                            }
                        }
                        if (drainedTotal > 0.0F && owner != null && owner.isAlive()) {
                            SpellCombatReactionEngine.notifyMagicalHeal(owner, drainedTotal);
                            owner.heal(drainedTotal);
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
        tag.putString("SpellEidolonMateria", EssenceType.ENTROPIC.name());
        tag.putInt("SpellEidolonTintRGB", ENTROPIC_VOID_INDIGO_HEX_COLOR);
        for (int c = 0; c < 4; c++) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), QUAD_ENTROPIC_PALETTE[c], 24, 0.95F, 0.05F),
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
        // Section 6.51 Event Horizon Phase-Slip:
        // Teleports up to 20m through solid walls & barriers; departure 8m black hole singularity (-4 armor, Blindness + Wither II 4.0s);
        // arrival boiling dark-blood shockwave granting 8.0s (160t) complete physical invulnerability & shedding 100% mob aggro!
        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.ENTROPIC, potencyTier);
        SpellWorldVfxManager.broadcastRadialNova(level, departurePos, 8.0D, EssenceType.ENTROPIC, EssenceType.VOID, potencyTier);
        SpellWorldVfxManager.broadcastRadialNova(level, arrivalPos, 6.0D, EssenceType.ENTROPIC, EssenceType.BLOOD, potencyTier);

        AABB dep = new AABB(
                departurePos.x - 8.0D, departurePos.y - 2.5D, departurePos.z - 8.0D,
                departurePos.x + 8.0D, departurePos.y + 4.5D, departurePos.z + 8.0D
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, dep, e -> e.isAlive() && e != caster)) {
            if (isFriendlyTarget(foe, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
            Vec3 pull = departurePos.subtract(foe.position()).scale(0.40D);
            foe.setDeltaMovement(pull.x, 0.22D, pull.z);
            foe.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 4.0D, 0.0D, 140L);
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1));
        }

        EntityHelper.getPersistentData(caster)
                .putLong(SpellLingeringZoneManager.TAG_COMPLETE_INVULNERABILITY_UNTIL, level.getGameTime() + 160L);
        SpellLingeringZoneManager.applyIntangiblePhase(caster, 160, true);
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.75F, null);
        ApexEschatonSingularityHelper.shedMobAggro(level, caster, arrivalPos, 26.0D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.0F, 0.85F);
    }

    @Override
    public boolean onTransmuteBlock(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            BlockState state,
            int potencyTier
    ) {
        // Section 6.51 Apex Thermodynamic Dissolution & Corruption [5.0s Channel]:
        // Stone/Cobble -> Blackstone/Deepslate/Crying Obsidian; Dirt/Grass -> Soul Sand/Sculk;
        // living plants -> Wither Roses; mob drops -> Dark Blood/Void Essence; damaged metal gear -> +35% ingots/nuggets + slag!
        if (ApexEschatonSingularityHelper.beginOrExecuteChanneledTransmute(
                level, caster, pos, 8, potencyTier, EssenceType.ENTROPIC,
                "Apex Thermodynamic Dissolution & Corruption", null)) {
            return true;
        }

        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return false;
        }
        if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE) || state.is(Blocks.MOSSY_COBBLESTONE)
                || state.is(Blocks.ANDESITE) || state.is(Blocks.DIORITE) || state.is(Blocks.GRANITE)) {
            BlockState out = switch (level.random.nextInt(3)) {
                case 0 -> Blocks.BLACKSTONE.defaultBlockState();
                case 1 -> Blocks.DEEPSLATE.defaultBlockState();
                default -> Blocks.CRYING_OBSIDIAN.defaultBlockState();
            };
            level.setBlockAndUpdate(pos, out);
            return true;
        }
        if (state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.PODZOL)
                || state.is(Blocks.MOSS_BLOCK) || state.is(Blocks.MUD)) {
            level.setBlockAndUpdate(pos, level.random.nextBoolean() ? Blocks.SOUL_SAND.defaultBlockState() : Blocks.SCULK.defaultBlockState());
            return true;
        }
        if ((state.is(BlockTags.SMALL_FLOWERS) || state.is(BlockTags.SAPLINGS) || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.FERN))
                && !state.is(Blocks.WITHER_ROSE)) {
            level.setBlockAndUpdate(pos, Blocks.WITHER_ROSE.defaultBlockState());
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

        Item scrapIngot = ApexEschatonSingularityHelper.resolveDamagedMetalScrapIngot(stack);
        if (scrapIngot != null) {
            int bonusIngots = Math.max(1, Math.round(2 * 1.35F)); // +35% bonus yield!
            itemEntity.setItem(new ItemStack(scrapIngot, bonusIngots));
            Item nugget = scrapIngot == Items.GOLD_INGOT ? Items.GOLD_NUGGET : Items.IRON_NUGGET;
            level.addFreshEntity(new ItemEntity(
                    level, itemEntity.getX(), itemEntity.getY() + 0.2D, itemEntity.getZ(),
                    new ItemStack(nugget, 4)
            ));
            level.addFreshEntity(new ItemEntity(
                    level, itemEntity.getX(), itemEntity.getY() + 0.2D, itemEntity.getZ(),
                    new ItemStack(ModBlocks.ASTRAL_IMPACTITE.get().asItem(), 1)
            ));
            return true;
        }

        if (stack.is(Items.ROTTEN_FLESH) || stack.is(Items.BONE) || stack.is(Items.SPIDER_EYE)
                || stack.is(Items.GUNPOWDER) || stack.is(Items.ENDER_PEARL) || stack.is(Items.PORKCHOP)
                || stack.is(Items.BEEF) || stack.is(Items.STRING) || stack.is(Items.PHANTOM_MEMBRANE)) {
            int outCount = Math.max(1, (int) Math.ceil(stack.getCount() * 1.25D));
            ItemStack ess = new ItemStack(ModItems.STRONG_ESSENCE.get(), Math.min(64, outCount));
            EssenceItem.setEssenceType(ess, level.random.nextBoolean() ? EssenceType.BLOOD : EssenceType.VOID);
            itemEntity.setItem(ess);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.51 Apex Thermodynamic Decay & Exsanguination Hex:
        // -30% Max HP (in SpellCombatReactionEngine), blocks healing, shreds 50% armor toughness,
        // leeches 1.5 hearts (3.0 HP) every 3.0s (60t) directly to caster, and movement/attack triggers bone spasms!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        long now = level.getGameTime();
        CompoundTag data = EntityHelper.getPersistentData(target);

        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.90D, 0.50D * mult), 45L);
        data.putLong(SpellLingeringZoneManager.TAG_BLOOD_HEMORRHAGE_UNTIL, now + 45L);

        double lastX = data.getDouble("EntropicHexLastX").orElse(target.getX());
        double lastZ = data.getDouble("EntropicHexLastZ").orElse(target.getZ());
        data.putDouble("EntropicHexLastX", target.getX());
        data.putDouble("EntropicHexLastZ", target.getZ());
        double movedSq = (target.getX() - lastX) * (target.getX() - lastX) + (target.getZ() - lastZ) * (target.getZ() - lastZ);
        if (movedSq > 0.04D || target.swinging) {
            target.hurtServer(level, level.damageSources().fellOutOfWorld(), 1.75F * mult);
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), ENTROPIC_UNDEAD_SLATE_HEX_COLOR, 16, 0.85F, 0.04F),
                    target.getX(), target.getY(0.75D), target.getZ(), 5, 0.25D, 0.30D, 0.25D, 0.02D
            );
        }

        long nextLeech = data.getLong(TAG_ENTROPIC_HEX_NEXT_LEECH).orElse(0L);
        if (now >= nextLeech) {
            data.putLong(TAG_ENTROPIC_HEX_NEXT_LEECH, now + 60L); // Every 3.0s (60t)!
            float leech = 3.0F * mult;
            target.hurtServer(level, level.damageSources().fellOutOfWorld(), leech);
            LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCaster(level, target);
            if (hexCaster != null && hexCaster.isAlive()) {
                SpellCombatReactionEngine.notifyMagicalHeal(hexCaster, leech);
                hexCaster.heal(leech);
            }
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_BLOOD.get(), ENTROPIC_BLOOD_CRIMSON_HEX_COLOR, 22, 0.95F, 0.05F),
                    target.getX(), target.getY(0.8D), target.getZ(), 8, 0.30D, 0.35D, 0.30D, 0.03D
            );
        }
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
