package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.item.EssenceItem;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Complete per-part Signature Mutation implementation for <b>Aether Materia (Tier 4 Apex Fusion #48)</b>
 * (Spellcrafting System Guide Section 6.50 — Air + Earth + Water + Nether catalyzed by Chimera):
 * <ul>
 *     <li><b>Palette</b>: Air Chalk Gold ({@code #A88654}) + Earth Deep Bronze ({@code #773214}) + Water Tidal Azure ({@code #225C7C}) + Nether Blazing Rust Crimson ({@code #B83C08})</li>
 *     <li><b>Advantage</b>: Strong vs <b>EARTH (+1)</b>, <b>NETHER (+1)</b>, <b>VOID (+1)</b>, <b>MAGMA (+1)</b>, <b>DUST (+1)</b>, and <b>OVERGROWTH (+1)</b> |
 *         Weak vs <b>VOID (-1)</b>, <b>NATURE (-1)</b>, <b>FROZEN (-1)</b>, and <b>ARID (-1)</b></li>
 * </ul>
 */
public final class AetherSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int AETHER_CHALK_GOLD_HEX_COLOR = 0xA88654;
    public static final int AETHER_EARTH_BRONZE_HEX_COLOR = 0x773214;
    public static final int AETHER_WATER_AZURE_HEX_COLOR = 0x225C7C;
    public static final int AETHER_NETHER_RUST_HEX_COLOR = 0xB83C08;

    private static final int[] QUAD_AETHER_PALETTE = {
            AETHER_CHALK_GOLD_HEX_COLOR,
            AETHER_EARTH_BRONZE_HEX_COLOR,
            AETHER_WATER_AZURE_HEX_COLOR,
            AETHER_NETHER_RUST_HEX_COLOR
    };

    private static final String TAG_AETHER_BEAM_LAST_TICK = "AetherBeamLastTick";
    private static final String TAG_AETHER_BEAM_TICKS = "AetherBeamContactTicks";
    private static final String TAG_AETHER_BEAM_ARMOR_STACKS = "AetherBeamArmorStacks";
    private static final String TAG_AETHER_HEX_NEXT_CRUSH = "AetherHexNextCrushTick";

    @Override
    public EssenceType materiaType() {
        return EssenceType.AETHER;
    }

    // =========================================================================
    // TITLES & CODEX / TOOLTIP DESCRIPTIONS
    // =========================================================================

    @Override
    public @Nullable String resolveFlowTitle(SpellFlowModifier flow) {
        return flow == SpellFlowModifier.ORBIT_SATELLITE ? "Quintessential Quad-Harmonic Orbit Flow" : null;
    }

    @Override
    public @Nullable String resolveFlowDescription(SpellFlowModifier flow) {
        return flow == SpellFlowModifier.ORBIT_SATELLITE
                ? "Manifests 4 persistent quintessential firmament orbs (Air #A88654, Earth #773214, Water #225C7C, Nether #B83C08) that phase through solid walls, vaporize 100% of incoming enemy projectiles, and discharge retaliatory quad-elemental homing darts dealing true Aether damage and shredding 2 armor per hit."
                : null;
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Apex Celestial Meteorite";
            case BEAM -> "Quintessential Firmament Ray & Celestial Supernova";
            case RADIAL_BURST -> "Quintessential Firmament Resonance Nova";
            case TOUCH_STRIKE -> "Quintessential Firmament Sunder";
            case AURA -> "Transcendent Firmament Quintessence Shroud";
            case GLYPH -> "Quintessential Firmament Inversion Vortex Sigil";
            case CONE -> "Quintessential Firmament Deluge";
            case SKYFALL -> "Cataclysmic Firmament Skyfall";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE ->
                    "Fires a gravity-immune, wall-phasing super-condensed firmament core (#A88654 / #773214 / #225C7C / #B83C08) that detonates in a 7m multi-elemental blast and scatters 4 autonomous elemental homing spikes (Air gale, Earth shrapnel, Water deluge, Nether magma), shattering 5 armor and disabling shields for 5.0s.";
            case BEAM ->
                    "Channels a wall-phasing prismatic atmospheric beam (#A88654 / #773214 / #225C7C / #B83C08) melting 2 armor/s, vaporizing incoming projectiles, and suspending targets in zero-g; after 5.0s continuous channeling triggers a Cataclysmic Firmament Supernova (12m knockback, -6 armor, buff purge & 5.0s Levitation + Stun).";
            case RADIAL_BURST ->
                    "10m wall-penetrating Quintessential Firmament Resonance Nova (zero physical light blocks): launches hostiles 10m airborne into 4.0s zero-g suspension, shatters 5 armor, and grants Transcendent Firmament Aegis (+10 Armor, +5 Toughness, fall/drowning/suffocation immunity & 3D levitation flight for 12.0s).";
            case TOUCH_STRIKE ->
                    "Strikes with a luminous firmament blade (#A88654 / #773214 / #225C7C / #B83C08) bypassing 50% armor, shattering 5 armor, disabling shields for 5.0s, launching the target and adjacent mobs within 6m 10m airborne into 4.0s zero-g suspension, and opening a 5m atmospheric vacuum fissure.";
            case AURA ->
                    "Transcendent Firmament Quintessence Shroud (#A88654 / #773214 / #225C7C / #B83C08): grants +10 Armor, +5 Toughness, fall/drowning/suffocation immunity, -80% mob detection, 4m projectile vaporization into cinder dust, a 6m anti-gravity lift aura, and unrestricted 3D arcane levitation flight.";
            case GLYPH ->
                    "Disarmable Single/Repeating Quintessential Firmament Inversion Vortex Sigil on any floor, wall, or ceiling: 4m hostile approach detonates an 8m anti-gravity pillar pulling mobs inward, launching them 12m upward, shattering 5 armor, purging buffs, and inflicting Levitation & Weakness III for 5.0s before slamming them down.";
            case CONE ->
                    "Wall-penetrating Quintessential Firmament Deluge (#A88654 / #773214 / #225C7C / #B83C08): shatters 5 armor, vaporizes all incoming projectiles, launches hostiles 8m airborne, and inflicts Firmament Spatial Vertigo (25.0s AI pathfinding disruption & -50% movement speed).";
            case SKYFALL ->
                    "Extended 20.0s Cataclysmic Firmament Skyfall across a 14m zone (#A88654 / #773214 / #225C7C / #B83C08): rains 3D crystalline ether-spires & quad-elemental meteorites knocking hostiles flat and shattering 6 armor, leaving a 10.0s Inverted Firmament Pressure zone (-70% speed, zero-g float & 1.5 hearts/s true crushing).";
            case SUMMON_AGENT ->
                    "Firmament Archon-Colossus (45s lifecycle / 90 HP): wall-phasing celestial juggernaut delivering sweeping quad-elemental cleaves that bypass 50% armor, shatter 3 armor per strike, and launch foes 8m airborne into zero-g float.";
            case SUMMON_SENTRY ->
                    "Apex Firmament Spire of Equilibrium (45s lifecycle / 100 HP): anchored monolithic obelisk bombarding hostiles up to 18m away with wall-penetrating celestial mortars (-2 armor/hit) while pulsing a 10m Firmament Resonance Field (lifting hostiles, -1 armor/s & cleansing ally debuffs).";
            case SUMMON_DECOY ->
                    "Harmonized Quintessence Effigy (14s duration): 16m taunt multi-elemental holographic effigy reflecting 40% of all incoming damage and detonating into a 12m atmospheric ether burst (-5 armor & 5.0s mid-air suspension).";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Firmament Ascension Step";
            case TRANSMUTE, BREAK -> "Quintessential Firmament Genesis";
            case AFFLICTION -> "Apex Gravitational Collapse Hex";
            default -> "Quintessential Firmament " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK ->
                    "Teleports up to 20m through solid walls and barriers (#A88654 / #773214 / #225C7C / #B83C08); departure pulls hostiles within 6m into a 3.0s anti-gravity vacuum, while arrival blasts foes 8m back (-3 armor), grants 8.0s of 3D arcane flight & fall immunity, and sheds 100% of mob aggro.";
            case TRANSMUTE, BREAK ->
                    "Requires 5.0s continuous channeling: transmutes Stone, Deepslate, Netherrack & End Stone into Calcite, Quartz, Amethyst Blocks, or Gilded Blackstone; purges equipment curses (+35% chance to elevate an enchantment tier); refines mob drops into Aether Crystals & Distillate (+25% yield); and condenses empty air into Purified Aetheric Spring Water.";
            case AFFLICTION ->
                    "Milk-immune hyper-gravity curse forcing victims into an -80% crawl, blocking jumping, shredding 50% armor toughness, inflicting 50% weapon attack self-recoil, and dealing 1.5 hearts of true crushing damage every 3.0s; Positional Crushing Vulnerability adds +50% aerial/charge mob damage.";
            default -> "Infuses the effect with Tier 4 Apex Aether firmament resonance (#A88654 / #773214 / #225C7C / #B83C08).";
        };
    }

    // =========================================================================
    // PART 1 — FLOW MUTATIONS (QUINTESSENTIAL QUAD-HARMONIC ORBIT FLOW)
    // =========================================================================

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        if (flow != SpellFlowModifier.ORBIT_SATELLITE) {
            return;
        }
        LivingEntity owner = projectile.getOwner() instanceof LivingEntity l ? l : null;
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(
                level, owner, projectile.getBoundingBox().inflate(2.5D), null, 0L
        );
        int quadColor = QUAD_AETHER_PALETTE[projectile.tickCount & 3];
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), quadColor, 18, 0.86F, 0.04F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.08D, 0.08D, 0.08D, 0.01D
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
        int quadColor = QUAD_AETHER_PALETTE[projectile.tickCount & 3];
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), quadColor, 18, 0.88F, 0.04F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.07D, 0.07D, 0.07D, 0.01D
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
            SpellCombatReactionEngine.hurtWithAetherMateriaAdvantage(
                    level, directVictim, caster, 5.20F + tier * 1.35F, false, true
            );
        }
        if (projectile.getPierceCountRemaining() <= 0) {
            detonateCelestialMeteorite(level, caster, hitEntity.position(), hitEntity, spell);
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
            detonateCelestialMeteorite(level, caster, blockHit.getLocation(), null, spell);
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
        detonateCelestialMeteorite(level, caster, projectile.position(), null, spell);
    }

    private static void detonateCelestialMeteorite(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 center,
            @Nullable Entity directHit,
            SpellAssembly spell
    ) {
        // Section 6.50 Apex Celestial Meteorite:
        // Cataclysmic 7m multi-elemental detonation + 4 autonomous elemental homing spikes
        // (Air gale, Earth shrapnel, Water deluge, Nether magma) tracking separate hostiles to
        // shatter 5 armor points and disable shields for 5.0s (100t)!
        int tier = spell.getEffectiveTier();
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastProjectileImpact(
                level, center.add(0.0D, 0.25D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.AETHER, EssenceType.AIR, 2.25F, tier
        );
        SpellWorldVfxManager.broadcastRadialNova(level, center, 7.0D, EssenceType.AETHER, EssenceType.WATER, tier);

        float blastDmg = 5.40F + tier * 1.45F;
        AABB blast = new AABB(
                center.x - 7.0D, center.y - 2.5D, center.z - 7.0D,
                center.x + 7.0D, center.y + 4.5D, center.z + 7.0D
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, blast, LivingEntity::isAlive)) {
            if (foe == caster) continue;
            if (caster != null && isFriendlyTarget(foe, caster)) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, safeguard)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 5.0D, 0.0D, 180L);
            ApexEschatonSingularityHelper.disableShield(foe, 100);
            if (foe != directHit) {
                SpellCombatReactionEngine.hurtWithAetherMateriaAdvantage(level, foe, caster, blastDmg, false, true);
            }
        }

        List<LivingEntity> spikeTargets = new ArrayList<>(level.getEntitiesOfClass(
                LivingEntity.class,
                blast.inflate(5.0D),
                e -> e.isAlive() && e != caster
                        && (e instanceof Monster || (caster != null && e == caster.getLastHurtMob()))
                        && (caster == null || (!isFriendlyTarget(e, caster)
                        && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, caster, safeguard)))
        ));
        EssenceType[] quadElements = {EssenceType.AIR, EssenceType.EARTH, EssenceType.WATER, EssenceType.NETHER};
        int spikes = Math.min(4, spikeTargets.size());
        float spikeDmg = 5.80F + tier * 1.50F;
        for (int i = 0; i < spikes; i++) {
            LivingEntity t = spikeTargets.get(i);
            EssenceType elem = quadElements[i];
            int color = QUAD_AETHER_PALETTE[i];
            SpellWorldVfxManager.broadcastChainArc(
                    level, center.add(0.0D, 0.65D, 0.0D), t.getEyePosition(), EssenceType.AETHER, elem, tier
            );
            SpellCombatReactionEngine.applyTimedAttributeShred(t, level, 5.0D, 0.0D, 180L);
            ApexEschatonSingularityHelper.disableShield(t, 100);
            switch (i) {
                case 0 -> { // Air gale spike
                    t.setDeltaMovement(t.getDeltaMovement().x, Math.max(t.getDeltaMovement().y, 0.55D), t.getDeltaMovement().z);
                    t.hasImpulse = true;
                }
                case 1 -> // Earth shrapnel spike
                        t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 2));
                case 2 -> // Water deluge spike
                        SpellLingeringZoneManager.applySoaked(t, 120);
                default -> // Nether magma spike
                        t.igniteForSeconds(4);
            }
            SpellCombatReactionEngine.hurtWithAetherMateriaAdvantage(level, t, caster, spikeDmg, false, true);
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), color, 22, 0.95F, 0.06F),
                    t.getX(), t.getY(0.8D), t.getZ(), 10, 0.28D, 0.32D, 0.28D, 0.04D
            );
        }
        level.playSound(null, BlockPos.containing(center), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.95F, 1.15F);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 2 — QUINTESSENTIAL FIRMAMENT RAY & CELESTIAL SUPERNOVA
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
        // Section 6.50 Quintessential Firmament Ray & Celestial Supernova:
        // Melts 2 armor points/s, vaporizes incoming projectiles in mid-air, suspends targets in zero-g;
        // 5.0 continuous seconds (100t) -> Cataclysmic Firmament Supernova (12m KB, -6 armor, buff purge, Levitation + Stun 5.0s)!
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
            long last = data.getLong(TAG_AETHER_BEAM_LAST_TICK).orElse(-100L);
            if (last == now) continue;
            long gap = now - last;
            int step = heldTicks > 0
                    ? ((last >= 0L && gap <= 32L) ? (int) Math.max(1L, Math.min(20L, gap)) : Math.min(heldTicks, 4))
                    : 0;
            int accumTicks = heldTicks > 0
                    ? ((gap >= 0L && gap <= 32L) ? data.getInt(TAG_AETHER_BEAM_TICKS).orElse(0) + step : step)
                    : 0;
            int armorStacks = (gap >= 0L && gap <= 32L)
                    ? Math.min(10, Math.max(2, (accumTicks / 20) * 2 + 2))
                    : 2;
            data.putLong(TAG_AETHER_BEAM_LAST_TICK, now);
            data.putInt(TAG_AETHER_BEAM_ARMOR_STACKS, armorStacks);

            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, armorStacks, 0.0D, 90L);
            hit.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 32, 0));

            if (heldTicks >= 100 && accumTicks >= 100) {
                data.putInt(TAG_AETHER_BEAM_TICKS, 0);
                data.putInt(TAG_AETHER_BEAM_ARMOR_STACKS, 0);
                Vec3 novaPos = hit.position();
                SpellWorldVfxManager.broadcastRadialNova(level, novaPos, 8.0D, EssenceType.AETHER, EssenceType.NETHER, tier);
                float supernovaDmg = (13.5F + tier * 2.50F) * Math.max(0.5F, apparatusPotency);
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, hit.getBoundingBox().inflate(7.0D), e -> e.isAlive() && e != caster)) {
                    if (isFriendlyTarget(foe, caster)) continue;
                    if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;
                    ApexEschatonSingularityHelper.purgeBeneficialBuffs(foe);
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 6.0D, 0.0D, 200L);
                    foe.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 100, 1));
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 100, 0));
                    Vec3 blastOut = foe == hit ? dir : foe.position().subtract(novaPos);
                    Vec3 normOut = blastOut.lengthSqr() > 1.0E-4D ? blastOut.normalize() : dir;
                    foe.setDeltaMovement(normOut.x * 2.40D, 0.58D, normOut.z * 2.40D); // 12m knockback!
                    foe.hasImpulse = true;
                    SpellCombatReactionEngine.hurtWithAetherMateriaAdvantage(level, foe, caster, supernovaDmg, false, true);
                }
                level.playSound(null, hit.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.35F);
            } else {
                data.putInt(TAG_AETHER_BEAM_TICKS, accumTicks);
            }
        }
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 3 — QUINTESSENTIAL FIRMAMENT RESONANCE NOVA (RADIAL BURST)
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
        // Section 6.50 Quintessential Firmament Resonance Nova:
        // Colossal 10m shockwave (zero physical light blocks), launches hostiles 10m airborne into 4.0s (80t) zero-g suspension,
        // shatters 5 armor, and grants Transcendent Firmament Aegis (+10 Armor, +5 Toughness, fall/drowning/suffocation immunity & 3D flight for 12.0s / 240t)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        double r = Math.max(10.0D, radius);
        AABB box = new AABB(
                center.x - r, center.y - 2.5D, center.z - r,
                center.x + r, center.y + 5.0D, center.z + r
        );
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(level, caster, box, null, 0L);

        float novaDmg = (5.0F + tier * 1.25F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (isFriendlyTarget(ent, caster)) {
                EntityHelper.getPersistentData(ent).putLong(SpellLingeringZoneManager.TAG_AETHER_AEGIS_UNTIL, level.getGameTime() + 240L);
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ent, level, 10.0D, 5.0D, 0.0D, 0.0D, 240L);
                SpellLingeringZoneManager.grantArcane3DFlight(ent, 240);
                SpellCombatReactionEngine.syncEntityBarrierState(level, ent, 0.65F, null);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, wardSafe)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, 5.0D, 0.0D, 180L);
            ent.setDeltaMovement(ent.getDeltaMovement().x * 0.35D, 1.30D, ent.getDeltaMovement().z * 0.35D); // 10m vertical launch!
            ent.hasImpulse = true;
            ent.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 80, 0));
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithAetherMateriaAdvantage(level, ent, caster, novaDmg, false, true);
            }
        }

        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_AETHER_AEGIS_UNTIL, level.getGameTime() + 240L);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 10.0D, 5.0D, 0.0D, 0.0D, 240L);
        SpellLingeringZoneManager.grantArcane3DFlight(caster, 240);
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.70F, null);

        for (int c = 0; c < 4; c++) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), QUAD_AETHER_PALETTE[c], 26, 1.05F, 0.06F),
                    center.x, center.y + 0.8D, center.z, 10, 2.6D, 0.5D, 2.6D, 0.05D
            );
        }
        level.playSound(null, BlockPos.containing(center), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.95F, 1.25F);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 4 — QUINTESSENTIAL FIRMAMENT SUNDER (TOUCH / STRIKE)
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
        // Section 6.50 Quintessential Firmament Sunder:
        // Bypasses 50% armor, shatters 5 armor, disables shields 5.0s (100t), launches target + adjacent mobs within 6m
        // 10m airborne into 4.0s (80t) zero-g suspension float, and leaves an atmospheric vacuum fissure!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        Vec3 fissureOrigin = hitEntities.isEmpty()
                ? caster.position().add(dir.scale(2.5D))
                : hitEntities.getFirst().position();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D), dir, 4.2F, EssenceType.AETHER, EssenceType.AIR, tier
        );

        float sunderDmg = (5.40F + tier * 1.35F) * Math.max(0.5F, apparatusPotency);
        AABB cleave = new AABB(
                fissureOrigin.x - 6.0D, fissureOrigin.y - 2.0D, fissureOrigin.z - 6.0D,
                fissureOrigin.x + 6.0D, fissureOrigin.y + 3.5D, fissureOrigin.z + 6.0D
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, cleave, e -> e.isAlive() && e != caster)) {
            if (isFriendlyTarget(foe, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 5.0D, 0.0D, 180L);
            ApexEschatonSingularityHelper.disableShield(foe, 100);
            foe.setDeltaMovement(foe.getDeltaMovement().x * 0.3D, 1.30D, foe.getDeltaMovement().z * 0.3D);
            foe.hasImpulse = true;
            foe.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 80, 0));
            if (!slot.hasHarmVerb() || !hitEntities.contains(foe)) {
                SpellCombatReactionEngine.hurtWithAetherMateriaAdvantage(level, foe, caster, sunderDmg, true, true);
            }
        }

        SpellWorldVfxManager.broadcastArcaneSeal(level, fissureOrigin.add(0.0D, 0.05D, 0.0D), 5.0D, EssenceType.AETHER, EssenceType.WATER, 80, tier);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, fissureOrigin, 5.0D, 80, level.getGameTime(), spell, slot, EssenceType.AETHER, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 8 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean zSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        AABB vac = new AABB(
                                zone.center.x - 5.0D, zone.center.y - 2.0D, zone.center.z - 5.0D,
                                zone.center.x + 5.0D, zone.center.y + 4.5D, zone.center.z + 5.0D
                        );
                        for (LivingEntity p : srv.getEntitiesOfClass(LivingEntity.class, vac, e -> e.isAlive() && e != owner)) {
                            if (owner != null && isFriendlyTarget(p, owner)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(p, owner, zSafe)) continue;
                            Vec3 pull = zone.center.subtract(p.position()).scale(0.24D);
                            p.setDeltaMovement(p.getDeltaMovement().add(pull.x, 0.0D, pull.z));
                            p.hasImpulse = true;
                        }
                        int qCol = QUAD_AETHER_PALETTE[(int) ((elapsed / 8) & 3)];
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_VORTEX.get(), qCol, 18, 0.90F, 0.05F),
                                zone.center.x, zone.center.y + 0.35D, zone.center.z, 6, 1.4D, 0.2D, 1.4D, 0.02D
                        );
                    }
                    return true;
                }
        ));
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 5 — TRANSCENDENT FIRMAMENT QUINTESSENCE SHROUD (AURA)
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
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_AETHER_SHROUD_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 10.0D, 5.0D, 0.0D, 0.0D, dur);
        SpellLingeringZoneManager.grantArcane3DFlight(caster, dur);
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.70F, null);
        for (int c = 0; c < 4; c++) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), QUAD_AETHER_PALETTE[c], 24, 0.95F, 0.05F),
                    caster.getX(), caster.getY(1.0D), caster.getZ(), 7, 0.55D, 0.65D, 0.55D, 0.04D
            );
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.9F, 1.25F);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 6 — QUINTESSENTIAL FIRMAMENT INVERSION VORTEX SIGIL (GLYPH)
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
        // Section 6.50 Quintessential Firmament Inversion Vortex Sigil:
        // Disarmable Single/Repeating quad-elemental mandala glyph on any floor, wall, or ceiling;
        // 4m hostile approach -> 8m anti-gravity pillar pulling mobs inward, launching 12m straight up,
        // shattering 5 armor, purging buffs, inflicting Levitation + Weakness III for 5.0s (100t),
        // then violently slamming them down into the ground for massive impact damage!
        int tier = spell.getEffectiveTier();
        double sigilRad = Math.max(4.0D, radius);
        Vec3 anchored = snapIfNearFloor(level, glyphCenter);
        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE / 4 : 12_000;
        final int[] triggersLeft = {repeating ? 4 : 1};
        final int[] rearmCooldown = {0};

        SpellWorldVfxManager.broadcastArcaneSeal(level, anchored.add(0.0D, 0.04D, 0.0D), sigilRad, EssenceType.AETHER, EssenceType.AIR, 240, tier);

        SpellLingeringZoneManager.ActiveSpellZone glyphZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, anchored, sigilRad, maxDuration, level.getGameTime(), spell, slot, EssenceType.AETHER, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();

                    if (elapsed > 0 && elapsed % 200 == 0) {
                        int rem = zone.maxDurationTicks >= 1_000_000_000 ? 240 : (int) Math.min(240L, zone.maxDurationTicks - elapsed);
                        SpellWorldVfxManager.broadcastArcaneSeal(
                                srv, zone.center.add(0.0D, 0.04D, 0.0D), zone.radius, EssenceType.AETHER, EssenceType.AIR, rem, tier
                        );
                    }
                    if (elapsed % 10 == 0) {
                        int qCol = QUAD_AETHER_PALETTE[(int) ((elapsed / 10) & 3)];
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), qCol, 18, 0.85F, 0.03F),
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
                    List<LivingEntity> triggered = srv.getEntitiesOfClass(LivingEntity.class, trig, e -> e.isAlive() && e != owner
                            && (owner == null || (!isFriendlyTarget(e, owner)
                            && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe))));
                    if (triggered.isEmpty()) return true;

                    triggersLeft[0]--;
                    rearmCooldown[0] = 120;
                    SpellWorldVfxManager.broadcastRadialNova(srv, zone.center, 8.0D, EssenceType.AETHER, EssenceType.WATER, tier);

                    AABB pillar = new AABB(
                            zone.center.x - 8.0D, zone.center.y - 2.5D, zone.center.z - 8.0D,
                            zone.center.x + 8.0D, zone.center.y + 5.5D, zone.center.z + 8.0D
                    );
                    List<LivingEntity> caught = srv.getEntitiesOfClass(LivingEntity.class, pillar, e -> e.isAlive() && e != owner
                            && (owner == null || (!isFriendlyTarget(e, owner)
                            && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe))));
                    for (LivingEntity foe : caught) {
                        ApexEschatonSingularityHelper.purgeBeneficialBuffs(foe);
                        SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 5.0D, 0.0D, 180L);
                        Vec3 in = zone.center.subtract(foe.position()).scale(0.38D);
                        foe.setDeltaMovement(in.x, 1.52D, in.z); // 12m vertical launch!
                        foe.hasImpulse = true;
                        foe.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 100, 0));
                        foe.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 2));
                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, foe, new Vec3(0.0D, 1.0D, 0.0D),
                                    zone.spell, zone.slot, EssenceType.AETHER, zone.apparatusPotency
                            );
                        }
                    }
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.95F, 1.4F);

                    // 5.0s (100t) delayed hyper-gravity slam into the ground!
                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 8.0D, 100, srv.getGameTime(), zone.spell, zone.slot, EssenceType.AETHER, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl < 100) return true;
                                LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                boolean sSafe = sZone.spell != null && sZone.spell.hasWardFriendlyFireSafeguard();
                                int sTier = sZone.spell != null ? sZone.spell.getEffectiveTier() : tier;
                                float slamDmg = (10.5F + sTier * 2.15F) * Math.max(0.5F, sZone.apparatusPotency);
                                SpellWorldVfxManager.broadcastRadialNova(sLvl, sZone.center, 8.0D, EssenceType.AETHER, EssenceType.EARTH, sTier);
                                AABB slamBox = new AABB(
                                        sZone.center.x - 8.5D, sZone.center.y - 2.0D, sZone.center.z - 8.5D,
                                        sZone.center.x + 8.5D, sZone.center.y + 20.0D, sZone.center.z + 8.5D
                                );
                                for (LivingEntity foe : sLvl.getEntitiesOfClass(LivingEntity.class, slamBox, e -> e.isAlive() && e != sOwner)) {
                                    if (sOwner != null && isFriendlyTarget(foe, sOwner)) continue;
                                    if (sOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, sOwner, sSafe)) continue;
                                    foe.removeEffect(MobEffects.LEVITATION);
                                    foe.setDeltaMovement(0.0D, -2.35D, 0.0D);
                                    foe.hasImpulse = true;
                                    SpellCombatReactionEngine.hurtWithAetherMateriaAdvantage(sLvl, foe, sOwner, slamDmg, false, true);
                                }
                                sLvl.playSound(null, BlockPos.containing(sZone.center), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.90F, 0.70F);
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
    // FORM MUTATION 7 — QUINTESSENTIAL FIRMAMENT DELUGE (CONE / SPRAY)
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
        // Section 6.50 Quintessential Firmament Deluge:
        // Wall-penetrating cone shattering 5 armor, vaporizing all incoming enemy projectiles,
        // launching hostiles 8m airborne, and inflicting Firmament Spatial Vertigo (25.0s AI disruption + -50% move speed)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(
                level, caster,
                new AABB(caster.getEyePosition(), caster.getEyePosition().add(dir.scale(coneRange))).inflate(3.2D),
                null, 0L
        );

        float coneDmg = (4.80F + tier * 1.20F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitTargets) {
            if (isFriendlyTarget(hit, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 5.0D, 0.0D, 180L);
            hit.setDeltaMovement(dir.x * 0.68D, 1.16D, dir.z * 0.68D); // 8m airborne!
            hit.hasImpulse = true;
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 500); // 25.0s Firmament Spatial Vertigo!
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(hit, level, 0.50D, 500L);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 500, 2)); // 50% speed reduction
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithAetherMateriaAdvantage(level, hit, caster, coneDmg, false, true);
            }
        }
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 8 — CATACLYSMIC FIRMAMENT SKYFALL (20.0s + 10.0s, 14m ZONE)
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
        // Section 6.50 Cataclysmic Firmament Skyfall:
        // 14m zone, 20.0s (400t) bombardment of 8 falling 3D Skyfall Pseudoblock crystalline ether-spires
        // & quad-elemental meteorites knocking hostiles flat & shattering 6 armor points, followed by a
        // 10.0s (200t) zone of Inverted Firmament Pressure (-70% slow, zero-g suspension & 1.5 hearts/s true crushing)!
        int tier = spell.getEffectiveTier();
        double r = Math.max(14.0D, radius);
        BlockPos centerPos = BlockPos.containing(impactCenter);
        double skyH = SpellDeliveryForm.probeCaveSafeSkyfallHeight(level, centerPos, tier);

        SpellWorldVfxManager.broadcastSkyfallPillar(level, impactCenter, r, EssenceType.AETHER, EssenceType.AIR, 405, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, impactCenter.add(0.0D, 0.05D, 0.0D), r, EssenceType.AETHER, EssenceType.WATER, 600, tier);

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
                caster, impactCenter, r, 600, level.getGameTime(), spell, slot, EssenceType.AETHER, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed >= zone.maxDurationTicks) {
                        if (!materialized[0]) {
                            materialized[0] = true;
                            for (int sIdx = 0; sIdx < spireCount; sIdx++) {
                                BlockState pseudoState = switch (sIdx & 3) {
                                    case 0 -> Blocks.QUARTZ_PILLAR.defaultBlockState();
                                    case 1 -> Blocks.CALCITE.defaultBlockState();
                                    case 2 -> Blocks.AMETHYST_BLOCK.defaultBlockState();
                                    default -> Blocks.GILDED_BLACKSTONE.defaultBlockState();
                                };
                                int colHeight = (sIdx % 2 == 0) ? 2 : 1;
                                SpellWorldVfxManager.materializeSkyfallPseudoblockColumn(srv, spireTargets[sIdx], pseudoState, colHeight);
                            }
                        }
                        return false;
                    }

                    boolean bombardment = elapsed < 400;
                    int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;

                    if (bombardment) {
                        // 1. 8 falling 3D crystalline ether-spire & quad-elemental meteorite pseudoblock columns!
                        for (int sIdx = 0; sIdx < spireCount; sIdx++) {
                            int impactTick = 20 + sIdx * 46;
                            BlockPos strikePos = spireTargets[sIdx];
                            BlockState pseudoState = switch (sIdx & 3) {
                                case 0 -> Blocks.QUARTZ_PILLAR.defaultBlockState();
                                case 1 -> Blocks.CALCITE.defaultBlockState();
                                case 2 -> Blocks.AMETHYST_BLOCK.defaultBlockState();
                                default -> Blocks.GILDED_BLACKSTONE.defaultBlockState();
                            };
                            int colHeight = (sIdx % 2 == 0) ? 2 : 1;

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
                                        EssenceType.AETHER,
                                        zTier
                                );
                            } else if (elapsed == impactTick) {
                                landed[sIdx] = true;
                                Vec3 hitVec = new Vec3(strikePos.getX() + 0.5D, strikePos.getY(), strikePos.getZ() + 0.5D);
                                EssenceType quadSec = switch (sIdx & 3) {
                                    case 0 -> EssenceType.AIR;
                                    case 1 -> EssenceType.EARTH;
                                    case 2 -> EssenceType.WATER;
                                    default -> EssenceType.NETHER;
                                };
                                SpellWorldVfxManager.broadcastProjectileImpact(
                                        srv, hitVec.add(0.0D, 0.25D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                                        EssenceType.AETHER, quadSec, 2.50F, zTier
                                );
                                srv.sendParticles(
                                        new BlockParticleOption(ParticleTypes.BLOCK, pseudoState),
                                        hitVec.x, hitVec.y + 0.6D, hitVec.z, 22, 1.0D, 0.5D, 1.0D, 0.09D
                                );
                                srv.playSound(null, strikePos, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 0.95F, 0.85F);
                            }
                        }

                        if (elapsed % 8 == 0) {
                            double ang = srv.random.nextDouble() * Math.PI * 2.0D;
                            double dist = Math.sqrt(srv.random.nextDouble()) * (zone.radius * 0.88D);
                            Vec3 strike = zone.center.add(Math.cos(ang) * dist, 0.25D, Math.sin(ang) * dist);
                            SpellWorldVfxManager.broadcastProjectileImpact(
                                    srv, strike, new Vec3(0.0D, 1.0D, 0.0D),
                                    EssenceType.AETHER, EssenceType.AIR, 1.60F, zTier
                            );
                        }
                    }
                    for (int sIdx = 0; sIdx < spireCount; sIdx++) {
                        if (landed[sIdx]) {
                            SpellWorldVfxManager.repelEntitiesFromPseudoblockColumn(srv, spireTargets[sIdx], (sIdx % 2 == 0) ? 2 : 1, false);
                        }
                    }

                    if (elapsed % 8 == 0) {
                        int qCol = QUAD_AETHER_PALETTE[(int) ((elapsed / 8) & 3)];
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), qCol, 22, 0.95F, 0.05F),
                                zone.center.x, zone.center.y + (bombardment ? 4.2D : 0.7D), zone.center.z,
                                20, zone.radius * 0.55D, bombardment ? 2.2D : 0.4D, zone.radius * 0.55D, 0.04D
                        );
                    }

                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        float stormDmg = (4.80F + zTier * 1.15F) * Math.max(0.5F, zone.apparatusPotency);
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
                                SpellCombatReactionEngine.hurtWithAetherMateriaAdvantage(srv, foe, owner, stormDmg, false, true);
                                if (zone.spell != null && zone.slot != null) {
                                    SpellExecutionEngine.deliverSubSpellOnEntity(
                                            srv, owner, foe, new Vec3(0.0D, -1.0D, 0.0D),
                                            zone.spell, zone.slot, EssenceType.AETHER, zone.apparatusPotency * 0.50F
                                    );
                                }
                            } else {
                                // 10.0s Inverted Firmament Pressure: -70% slow, zero-g suspension & 1.5 hearts/s (3.0 HP/s) true crushing!
                                SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, srv, 0.70D, 35L);
                                foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 4));
                                foe.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 26, 0));
                                foe.hurtServer(srv, srv.damageSources().fellOutOfWorld(), 3.0F);
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
        tag.putString("SpellEidolonMateria", EssenceType.AETHER.name());
        tag.putInt("SpellEidolonTintRGB", AETHER_CHALK_GOLD_HEX_COLOR);
        for (int c = 0; c < 4; c++) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), QUAD_AETHER_PALETTE[c], 24, 0.95F, 0.05F),
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
        // Section 6.50 Firmament Ascension Step:
        // Teleports up to 20m through solid walls & barriers; departure 6m anti-gravity vacuum (3.0s / 60t);
        // arrival 8m KB, -3 armor, 8.0s (160t) 3D arcane flight + fall immunity, sheds 100% mob aggro!
        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.AETHER, potencyTier);
        SpellWorldVfxManager.broadcastRadialNova(level, departurePos, 6.0D, EssenceType.AETHER, EssenceType.WATER, potencyTier);
        SpellWorldVfxManager.broadcastRadialNova(level, arrivalPos, 6.0D, EssenceType.AETHER, EssenceType.AIR, potencyTier);

        AABB depBox = new AABB(
                departurePos.x - 6.0D, departurePos.y - 2.0D, departurePos.z - 6.0D,
                departurePos.x + 6.0D, departurePos.y + 4.0D, departurePos.z + 6.0D
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, depBox, e -> e.isAlive() && e != caster)) {
            if (isFriendlyTarget(foe, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
            Vec3 pull = departurePos.subtract(foe.position()).scale(0.38D);
            foe.setDeltaMovement(pull.x, 0.38D, pull.z);
            foe.hasImpulse = true;
            foe.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 60, 0));
        }

        AABB arrBox = new AABB(
                arrivalPos.x - 6.0D, arrivalPos.y - 2.0D, arrivalPos.z - 6.0D,
                arrivalPos.x + 6.0D, arrivalPos.y + 4.0D, arrivalPos.z + 6.0D
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, arrBox, e -> e.isAlive() && e != caster)) {
            if (isFriendlyTarget(foe, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
            Vec3 diff = foe.position().subtract(arrivalPos);
            Vec3 away = (diff.lengthSqr() > 1.0E-4D ? diff.normalize() : new Vec3(0.0D, 0.3D, 1.0D)).scale(1.88D); // 8m KB!
            foe.setDeltaMovement(away.x, 0.44D, away.z);
            foe.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 3.0D, 0.0D, 140L);
        }

        SpellLingeringZoneManager.grantArcane3DFlight(caster, 160);
        caster.resetFallDistance();
        ApexEschatonSingularityHelper.shedMobAggro(level, caster, arrivalPos, 26.0D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.PLAYERS, 0.9F, 1.3F);
    }

    @Override
    public boolean onTransmuteBlock(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            BlockState state,
            int potencyTier
    ) {
        // Section 6.50 Quintessential Firmament Genesis [5.0s Channel]:
        // Stone/Deepslate/Netherrack/End Stone -> Calcite/Quartz/Amethyst/Gilded Blackstone;
        // cleanses curses + 35% +1 enchant tier; mob drops -> Aether Crystals & Distillate;
        // empty air -> Purified Aetheric Spring Water!
        if (ApexEschatonSingularityHelper.beginOrExecuteChanneledTransmute(
                level, caster, pos, 8, potencyTier, EssenceType.AETHER, "Quintessential Firmament Genesis",
                () -> {
                    if (caster != null) {
                        ApexEschatonSingularityHelper.purgeCursesAndElevateEquipment(caster, 1, 0.35F, 1);
                    }
                    BlockPos waterPos = level.getBlockState(pos).isAir() ? pos : (level.getBlockState(pos.above()).isAir() ? pos.above() : null);
                    if (waterPos != null && !SpellExecutionEngine.isProtectedBlockOrContainer(level, waterPos, level.getBlockState(waterPos))) {
                        level.setBlockAndUpdate(waterPos, Blocks.WATER.defaultBlockState());
                        registerPurifiedAethericSpringZone(level, caster, Vec3.atCenterOf(waterPos), potencyTier);
                    }
                })) {
            return true;
        }

        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return false;
        }
        if (state.isAir()) {
            level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
            registerPurifiedAethericSpringZone(level, caster, Vec3.atCenterOf(pos), potencyTier);
            return true;
        }
        if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE) || state.is(Blocks.DEEPSLATE)
                || state.is(Blocks.COBBLED_DEEPSLATE) || state.is(Blocks.NETHERRACK) || state.is(Blocks.END_STONE)
                || state.is(Blocks.GRANITE) || state.is(Blocks.DIORITE) || state.is(Blocks.ANDESITE)) {
            BlockState out = switch (level.random.nextInt(4)) {
                case 0 -> Blocks.CALCITE.defaultBlockState();
                case 1 -> Blocks.QUARTZ_BLOCK.defaultBlockState();
                case 2 -> Blocks.AMETHYST_BLOCK.defaultBlockState();
                default -> Blocks.GILDED_BLACKSTONE.defaultBlockState();
            };
            level.setBlockAndUpdate(pos, out);
            return true;
        }
        return false;
    }

    private static void registerPurifiedAethericSpringZone(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 center,
            int potencyTier
    ) {
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, center, 2.5D, 400, level.getGameTime(), null, null, EssenceType.AETHER, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 20 == 0) {
                        if (!srv.getBlockState(BlockPos.containing(zone.center)).is(Blocks.WATER)) {
                            return false;
                        }
                        LivingEntity owner = zone.resolveCaster(srv);
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, new AABB(zone.center, zone.center).inflate(2.2D), LivingEntity::isAlive)) {
                            if (owner == null || isFriendlyTarget(ent, owner)) {
                                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(ent);
                                ent.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, true));
                            }
                        }
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), AETHER_WATER_AZURE_HEX_COLOR, 20, 0.85F, 0.04F),
                                zone.center.x, zone.center.y + 0.55D, zone.center.z, 4, 0.45D, 0.25D, 0.45D, 0.02D
                        );
                    }
                    return true;
                }
        ));
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
        if (ApexEschatonSingularityHelper.purgeCursesAndElevateStack(stack, level.random, 1, 0.35F, 1)) {
            itemEntity.setItem(stack.copy());
            return true;
        }
        if (stack.is(Items.ROTTEN_FLESH) || stack.is(Items.BONE) || stack.is(Items.GUNPOWDER)
                || stack.is(Items.SPIDER_EYE) || stack.is(Items.ENDER_PEARL) || stack.is(Items.BLAZE_ROD)
                || stack.is(Items.GHAST_TEAR) || stack.is(Items.PHANTOM_MEMBRANE) || stack.is(Items.STRING)
                || stack.is(Items.SLIME_BALL) || stack.is(Items.MAGMA_CREAM)) {
            int count = stack.getCount();
            int distillCount = Math.max(1, (int) Math.ceil(count * 1.25D)); // +25% yield!
            ItemStack aetherDistillate = new ItemStack(ModItems.STRONG_ESSENCE.get(), Math.min(64, distillCount));
            EssenceItem.setEssenceType(aetherDistillate, EssenceType.AETHER);
            itemEntity.setItem(aetherDistillate);

            ItemEntity crystalDrop = new ItemEntity(
                    level, itemEntity.getX(), itemEntity.getY() + 0.15D, itemEntity.getZ(),
                    new ItemStack(Items.AMETHYST_SHARD, Math.min(64, count))
            );
            level.addFreshEntity(crystalDrop);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.50 Apex Gravitational Collapse Hex:
        // Forces victims to crawl at -80% movement speed, blocks jumping entirely, shreds 50% of armor toughness,
        // causes any weapon attack to suffer 50% self-damage recoil, and deals 1.5 hearts (3.0 HP) of true crushing damage every 3.0s (60t)!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        long now = level.getGameTime();
        CompoundTag data = EntityHelper.getPersistentData(target);

        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.90D, 0.50D * mult), 45L);
        SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, Math.min(0.90D, 0.80D * mult), 45L);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 4, true, true));
        data.putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 40L);
        if (target.getDeltaMovement().y > 0.05D) {
            target.setDeltaMovement(target.getDeltaMovement().x, -0.15D, target.getDeltaMovement().z);
            target.hasImpulse = true;
        }

        long nextCrush = data.getLong(TAG_AETHER_HEX_NEXT_CRUSH).orElse(0L);
        if (now >= nextCrush) {
            data.putLong(TAG_AETHER_HEX_NEXT_CRUSH, now + 60L); // Every 3.0s (60t)!
            target.hurtServer(level, level.damageSources().fellOutOfWorld(), 3.0F * mult);
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SHIELD.get(), AETHER_CHALK_GOLD_HEX_COLOR, 20, 0.95F, 0.04F),
                    target.getX(), target.getY(0.35D), target.getZ(), 8, 0.35D, 0.12D, 0.35D, 0.03D
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
        if (!level.getBlockState(bp.below()).isAir() || !level.getBlockState(bp.above()).isAir()) {
            // Wall or ceiling placement stays right where inscribed unless hovering slightly above floor!
            if (!level.getBlockState(bp.above()).isAir() && level.getBlockState(bp.below()).isAir()) {
                return pos;
            }
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
