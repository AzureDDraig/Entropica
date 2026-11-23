package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.registry.ModParticles;
import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.SpellTriggerType;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import ddraig.net.entropica.spell.assembly.SubSpellSlot;
import ddraig.net.entropica.spell.engine.SpellCombatReactionEngine;
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
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
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
 * (Spellcrafting System Guide Section 6.58 — Empyrean + Cataclysm + Terminus + Celestial:
 * <i>"The Alpha Genesis Singularity"</i>).
 *
 * <p>Palette: Supernova Brilliant White ({@code #FFFFFF}), Celestial Astral Gold ({@code #FFE484}),
 * and Ascendant Prismatic Cyan ({@code #72F6FF}); formatted with bright yellow/gold {@code "§e"}.</p>
 */
public final class ApotheosisSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int APOTHEOSIS_SUPERNOVA_WHITE_HEX_COLOR = 0xFFFFFF;
    public static final int APOTHEOSIS_ASTRAL_GOLD_HEX_COLOR = 0xFFE484;
    public static final int APOTHEOSIS_PRISMATIC_CYAN_HEX_COLOR = 0x72F6FF;
    public static final int APOTHEOSIS_SUPERNOVA_GOLD_HEX_COLOR = APOTHEOSIS_ASTRAL_GOLD_HEX_COLOR;

    private static final String TAG_TERMINAL_NOVA_FIRED = "SpellApotheosisTerminalNovaFired";
    private static final String TAG_BEAM_START_TICK = "SpellApotheosisBeamStartTick";
    private static final String TAG_BEAM_LAST_TICK = "SpellApotheosisBeamLastTick";
    private static final String TAG_BEAM_BLOOMED = "SpellApotheosisBeamCataclysmBloomed";

    @Override
    public EssenceType materiaType() {
        return EssenceType.APOTHEOSIS;
    }

    @Override
    public @Nullable String resolveFlowTitle(SpellFlowModifier flow) {
        return flow == SpellFlowModifier.ORBIT_SATELLITE ? "White-Hole Star-Orbit Flow" : null;
    }

    @Override
    public @Nullable String resolveFlowDescription(SpellFlowModifier flow) {
        return flow == SpellFlowModifier.ORBIT_SATELLITE
                ? "Manifests 4 persistent orbiting white-hole starcores and golden solar halos (#FFFFFF / #FFE484 / #72F6FF) "
                        + "that circle the primary spell or caster; phases through solid walls, intercepts and disintegrates 100% "
                        + "of incoming enemy projectiles into radiant essence and healing dew, and automatically discharges seeking "
                        + "holy star-darts at nearby hostiles (-50% armor toughness and true holy fire damage)."
                : null;
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
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE ->
                    "Throws a spinning corona ring of concentrated white-hole singularity light, living gold, and prismatic "
                            + "starlight (#FFFFFF / #FFE484 / #72F6FF); pierces infinitely through solid walls and bounces "
                            + "between up to 10 hostiles (75% armor bypass), blinding targets and siphoning life force into "
                            + "party absorption shields; terminal impact detonates an expansive 10m White-Hole Genesis Nova "
                            + "(-8 armor, smites hostiles, releases radiant essence orbs, and fully heals allies).";
            case BEAM ->
                    "Colossal wall-phasing continuous ray of pure blinding starlight, golden solar fire, and prismatic genesis "
                            + "energy; melts 3 armor points/sec, smites undead, and heals allies; sustaining for 5.0 continuous "
                            + "seconds triggers a Cataclysmic Genesis Bloom (16m knockback, -8 armor, purges all hostile buffs, "
                            + "and blooms a 14m celestial sanctuary healing allies for 6 hearts + Saturation).";
            case RADIAL_BURST ->
                    "Wall-penetrating 16-block concussive shockwave of white-hole light, golden solar rings, and prismatic "
                            + "starlight (pure shader/bloom without chunk light lag); knocks hostiles 14m back flat, shatters "
                            + "8 armor points, deals massive Tier 6 Apotheosis damage (2x vs Undead/Demonic/Void), purges party "
                            + "debuffs, heals allies for 12 hearts, and grants party Mantle of the Alpha Singularity for 20.0s.";
            case TOUCH_STRIKE ->
                    "Luminous wall-cleaving two-handed blade of condensed white-hole starlight and living gold; bypasses 75% "
                            + "armor rating, shatters 8 armor points, disables shields for 8.0s, smites dark entities, roots "
                            + "foes in starlight briars for 4.0s, and launches seeking golden crowns within 10m siphoning health "
                            + "into party absorption shields.";
            case AURA ->
                    "Envelopes the caster in blinding white-hole radiance, golden solar crowns, and prismatic starlight rings "
                            + "(mobile or Sneak-anchored); emits Level-15 ambient light, +14 Armor, +8 Toughness, complete "
                            + "fire/lava/drowning/freeze/wither/blindness/void immunity, -90% mob detection, 6m projectile-to-"
                            + "petal/radiant-droplet vaporization, 3D flight + Slow Falling, and an 8m consecrated aura (+3 "
                            + "hearts/s + Saturation for allies; 3.0 hearts/s true holy burn to dark/void/nether hostiles).";
            case GLYPH ->
                    "Glorious solar-verdant celestial mandala glyph on any floor, wall, or ceiling (disarmable, Single or "
                            + "Repeating); hostile approach within 6m erupts a 12m golden hard-light pillar cage for 6.0s (-8 "
                            + "armor, strips buffs, smites dark entities, and leeches 50% total HP into radiant essence orbs); "
                            + "ally step restores full HP, cures curses, grants +12 Absorption hearts, and 60.0s Regeneration IV.";
            case CONE ->
                    "Wide wall-penetrating fan of pressurized rainbow lasers, white-hole starlight, and solar flares; shatters "
                            + "8 armor points, blinds hostiles, ignites undead, knocks enemies back with solar wind, vitrifies "
                            + "sand into crystal glass, blooms flowers, grows crops, turns netherrack into lush moss, purges "
                            + "withered foliage, and inflicts Divine Reverence (35.0s AI disruption and -75% movement speed).";
            case SKYFALL ->
                    "30.0s Cataclysmic White-Hole Genesis Starfall across a 20-block+ radiant firmament rift calling down 8 3D "
                            + "falling celestial starcores & golden solar megaliths that permanently materialize on completion; "
                            + "knocks hostiles flat, shatters 8 armor points, smites dark entities, and leaves a 15.0s "
                            + "Consecrated Genesis Ground (roots every 3.0s, -80% slow, radiant essence orbs, and +3 hearts/s "
                            + "+ Saturation + Regeneration IV + 8 Armor for allies).";
            case SUMMON_AGENT ->
                    "Summons a 140 HP Celestial Seraph-Titan (90s refreshing lifecycle) — colossal wall-phasing 6-winged "
                            + "hard-light titan delivering sweeping divine cleaves (75% armor bypass, -5 armor/strike, smites "
                            + "undead & dark entities with holy fire, and heals nearby friendly players for 25% of damage dealt).";
            case SUMMON_SENTRY ->
                    "Summons a 150 HP White-Hole Dawn Obelisk (90s refreshing lifecycle) — bombards hostiles up to 24m with "
                            + "wall-penetrating holy supernova lances (-4 armor/hit) while pulsing a 20m Sanctuary Field (+10 "
                            + "Armor, +6 Toughness, and continuous absorption up to 15 damage per player).";
            case SUMMON_DECOY ->
                    "Summons a Radiant Ascendance Mirage (20s duration) — solar-winged avatar effigy taunting hostiles across "
                            + "20m, reflecting 50% incoming damage, and detonating in a 20m healing supernova burst (massive "
                            + "true holy damage, -8 armor, 12-heart ally heal, and radiant essence drops).";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Ascendant Solar Genesis Step";
            case TRANSMUTE, BREAK -> "Alpha Genesis Transmutation & Universal Consecration";
            case AFFLICTION -> "Divine Genesis Brand Hex";
            case WARD -> "Alpha Genesis Sanctuary Ward";
            default -> "Alpha Genesis " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK ->
                    "Dematerializes the caster on a blinding pillar of white-hole light, instantly relocating up to 24 blocks "
                            + "through solid walls and ceilings; departure erupts an 8m flashbang (6.0s Blindness + 3.0s golden "
                            + "briar root); arrival grants 12.0s 3D arcane flight, Slow Falling, fall & void immunity, drops "
                            + "healing celestial dewdrops below, and sheds 100% hostile mob aggro.";
            case TRANSMUTE, BREAK ->
                    "5.0s continuous channel across 16 blocks (air cannot be transmuted); purifies netherrack, soul sand, sculk, "
                            + "and deepslate into Calcite, Flowering Azalea Moss, Lush Loam, and Amethyst Blocks; converts cursed "
                            + "blocks into Radiant Raw Essence & Materia droplets; purges worn curses + elevates up to 2 enchants "
                            + "(+2); cures Zombie Villagers w/ max discounts; 4m standing water + glass bottle yields Nectar of "
                            + "the Alpha Genesis (Regeneration V, Saturation, +14 Absorption hearts, 90s 3D flight).";
            case AFFLICTION ->
                    "Milk-immune apocalyptic celestial brand (30.0s boss cap + 10.0s immunity, 6m conditional death contagion); "
                            + "deals continuous true holy burn damage (2x vs Undead/Demonic/Abyssal), disables all natural/potion/"
                            + "regen recovery, 60% attack fumble miss, -60% armor toughness, and dark-magic backfire. Player "
                            + "Vector (Blinding Overexposure): surrounding monsters gain +150% attack speed and 100% crit chance.";
            case WARD ->
                    "Sanctifies the recipient's arcane barrier with supernova white-hole light, cleansing negative debuffs "
                            + "and fortifying absorption reserves.";
            default -> "Infuses " + effect.getDisplayName() + " with Tier 6 Alpha Genesis Singularity radiance.";
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATIONS — WHITE-HOLE STAR-ORBIT FLOW
    // =========================================================================

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        if (flow != SpellFlowModifier.ORBIT_SATELLITE) {
            return;
        }
        projectile.setWallPhasing(true);
        LivingEntity owner = projectile.getOwner() instanceof LivingEntity l ? l : null;

        // 1. Render 4 orbiting white-hole starcores & golden solar halos (#FFFFFF / #FFE484 / #72F6FF) around the carrier!
        double baseAngle = projectile.tickCount * 0.42D;
        for (int i = 0; i < 4; i++) {
            double ang = baseAngle + (Math.PI * 0.5D * i);
            double ox = projectile.getX() + Math.cos(ang) * 0.68D;
            double oy = projectile.getY() + 0.14D + Math.sin(ang * 2.0D) * 0.18D;
            double oz = projectile.getZ() + Math.sin(ang) * 0.68D;
            int rgb = (i == 0) ? APOTHEOSIS_SUPERNOVA_WHITE_HEX_COLOR
                    : ((i & 1) == 1 ? APOTHEOSIS_ASTRAL_GOLD_HEX_COLOR : APOTHEOSIS_PRISMATIC_CYAN_HEX_COLOR);
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), rgb, 14, 0.90F, 0.03F),
                    ox, oy, oz, 1, 0.02D, 0.02D, 0.02D, 0.0D
            );
        }

        // 2. Intercept & disintegrate 100% of incoming enemy physical & magical projectiles into radiant essence & healing dew!
        for (Projectile incoming : level.getEntitiesOfClass(Projectile.class, projectile.getBoundingBox().inflate(3.2D))) {
            if (incoming == projectile || (owner != null && incoming.getOwner() == owner)) continue;
            if (incoming instanceof SpellProjectileEntity sp
                    && owner != null
                    && sp.getOwner() instanceof LivingEntity spo
                    && ApexEschatonSingularityHelper.isFriendly(spo, owner)) {
                continue;
            }
            Vec3 pPos = incoming.position();
            incoming.discard();
            level.sendParticles(ParticleTypes.END_ROD, pPos.x, pPos.y, pPos.z, 6, 0.10D, 0.10D, 0.10D, 0.03D);
            SpellLingeringZoneManager.spawnCollectableHealingOrb(level, pPos, owner, 2.0F, false);
            if (level.random.nextFloat() < 0.35F) {
                SpellLingeringZoneManager.spawnTypedEssenceOrb(level, pPos, EssenceType.RADIANT, 1);
            }
        }

        // 3. Automatically discharge seeking holy star-darts at nearby hostiles (-50% Toughness & true holy fire)!
        if (projectile.tickCount % 12 == 0) {
            int tier = Math.max(1, projectile.getVisualPotencyTier());
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, projectile.getBoundingBox().inflate(7.5D), LivingEntity::isAlive)) {
                if (foe == owner || (owner != null && ApexEschatonSingularityHelper.isFriendly(foe, owner))) continue;
                if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, true)) continue;
                if (foe instanceof Monster || (owner != null && foe == owner.getLastHurtMob())) {
                    SpellWorldVfxManager.broadcastChainArc(
                            level, projectile.position(), foe.getEyePosition().subtract(0.0D, 0.2D, 0.0D),
                            EssenceType.APOTHEOSIS, EssenceType.RADIANT, tier
                    );
                    SpellCombatReactionEngine.applyPercentageArmorShred(foe, level, 0.0D, 0.50D, 140L);
                    foe.igniteForSeconds(4);
                    SpellCombatReactionEngine.hurtWithApotheosisMateriaAdvantage(
                            level, foe, owner, 3.6F + tier * 0.90F, false, false, true, true
                    );
                    break;
                }
            }
        }
    }

    @Override
    public void onChainLeapStep(
            ServerLevel level,
            LivingEntity caster,
            LivingEntity fromEntity,
            LivingEntity toEntity,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        long now = level.getGameTime();
        if (spell.flowPart().flow() == SpellFlowModifier.ORBIT_SATELLITE) {
            EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_APOTHEOSIS_ORBIT_UNTIL, now + 200L);
        }
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.55F, null);
        SpellWorldVfxManager.broadcastChainArc(
                level, fromEntity.getEyePosition(), toEntity.getEyePosition(),
                EssenceType.APOTHEOSIS, EssenceType.RADIANT, spell.getEffectiveTier()
        );
        if (!ApexEschatonSingularityHelper.isFriendly(toEntity, caster)
                && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(toEntity, caster, spell.hasWardFriendlyFireSafeguard())) {
            SpellCombatReactionEngine.applyPercentageArmorShred(toEntity, level, 0.0D, 0.50D, 140L);
            toEntity.igniteForSeconds(4);
            SpellCombatReactionEngine.hurtWithApotheosisMateriaAdvantage(
                    level, toEntity, caster, 3.8F + spell.getEffectiveTier() * 0.95F, false, false, true, true
            );
            SpellLingeringZoneManager.spawnCollectableHealingOrb(level, toEntity.position(), caster, 2.0F, false);
        }
    }

    // =========================================================================
    // PART 2: DELIVERY FORM MUTATIONS (9 CANONICAL FORMS)
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
        projectile.setWallPhasing(true);
        emitApotheosisBurstParticles(level, projectile.position(), 12, 0.25D);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        double spin = projectile.tickCount * 0.65D;
        for (int i = 0; i < 3; i++) {
            double ang = spin + (i * Math.PI * 2.0D / 3.0D);
            double px = projectile.getX() + Math.cos(ang) * 0.36D;
            double py = projectile.getY() + Math.sin(ang * 1.5D) * 0.14D;
            double pz = projectile.getZ() + Math.sin(ang) * 0.36D;
            int rgb = (i == 0) ? APOTHEOSIS_SUPERNOVA_WHITE_HEX_COLOR
                    : (i == 1 ? APOTHEOSIS_ASTRAL_GOLD_HEX_COLOR : APOTHEOSIS_PRISMATIC_CYAN_HEX_COLOR);
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), rgb, 12, 0.85F, 0.03F),
                    px, py, pz, 1, 0.02D, 0.02D, 0.02D, 0.0D
            );
        }
        if ((projectile.tickCount & 1) == 0) {
            level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(),
                    2, 0.06D, 0.06D, 0.06D, 0.015D);
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
        // Section 6.58 Seeking Crown of Dawn:
        // Bounces between up to 10 hostiles through walls (75% armor bypass), blinding targets & siphoning life force
        // into party absorption shields; terminal impact detonates into a 10m White-Hole Genesis Nova!
        int tier = spell.getEffectiveTier();
        if (hitEntity instanceof LivingEntity le && le.isAlive()) {
            if (ApexEschatonSingularityHelper.isFriendly(le, caster)) {
                float allyHeal = 8.0F + tier * 1.5F;
                SpellCombatReactionEngine.notifyMagicalHeal(le, allyHeal);
                le.heal(allyHeal);
                SpellLingeringZoneManager.grantPartyAbsorption(level, caster, 4.0F, 36.0F, 20.0D);
                return;
            }
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(le, caster, spell.hasWardFriendlyFireSafeguard())) {
                return;
            }
            le.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
            le.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0));
            if (ApexEschatonSingularityHelper.isDarkUndeadOrAbyssal(le)) {
                le.igniteForSeconds(5);
            }
            float crownDmg = (slot.hasHarmVerb() ? 4.2F : 7.8F) + tier * 1.65F;
            float dealt = SpellCombatReactionEngine.hurtWithApotheosisMateriaAdvantage(
                    level, le, caster, crownDmg, false, true, true, false
            );
            float siphoned = Math.max(3.5F, dealt * 0.45F);
            SpellLingeringZoneManager.grantPartyAbsorption(level, caster, siphoned, 36.0F, 20.0D);
            emitApotheosisBurstParticles(level, le.position().add(0.0D, le.getBbHeight() * 0.55D, 0.0D), 14, 0.35D);
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
            tryDetonateTerminalGenesisNovaOnce(level, projectile, caster, blockHit.getLocation(), spell);
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
        tryDetonateTerminalGenesisNovaOnce(level, projectile, caster, projectile.position(), spell);
    }

    private static void tryDetonateTerminalGenesisNovaOnce(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            Vec3 center,
            SpellAssembly spell
    ) {
        CompoundTag pTag = EntityHelper.getPersistentData(projectile);
        if (pTag.getBoolean(TAG_TERMINAL_NOVA_FIRED).orElse(false)) {
            return;
        }
        pTag.putBoolean(TAG_TERMINAL_NOVA_FIRED, true);
        detonateWhiteHoleGenesisNova(level, caster, center, spell);
    }

    private static void detonateWhiteHoleGenesisNova(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 center,
            SpellAssembly spell
    ) {
        // Section 6.58 Terminal 10m White-Hole Genesis Nova:
        // Smites hostiles, shatters 8 armor points, releases radiant essence orbs, and fully heals allies!
        int tier = spell.getEffectiveTier();
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastRadialNova(level, center, 10.0D, EssenceType.APOTHEOSIS, EssenceType.RADIANT, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, center.add(0.0D, 0.04D, 0.0D), 10.0D, EssenceType.APOTHEOSIS, EssenceType.CELESTIAL, 60, tier);
        consecrateCorruptedTerrainInRadius(level, BlockPos.containing(center), 10, 24);

        AABB nova = new AABB(
                center.x - 10.0D, center.y - 3.5D, center.z - 10.0D,
                center.x + 10.0D, center.y + 6.0D, center.z + 10.0D
        );
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, nova, LivingEntity::isAlive)) {
            if (ent == caster || ApexEschatonSingularityHelper.isFriendly(ent, caster)) {
                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(ent);
                float full = ent.getMaxHealth();
                SpellCombatReactionEngine.notifyMagicalHeal(ent, full);
                ent.heal(full); // Fully heals allies!
                level.sendParticles(ParticleTypes.HEART, ent.getX(), ent.getY(1.0D), ent.getZ(), 6, 0.3D, 0.3D, 0.3D, 0.03D);
                continue;
            }
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, safeguard)) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, 8.0D, 0.0D, 240L);
            ent.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
            if (ApexEschatonSingularityHelper.isDarkUndeadOrAbyssal(ent)) {
                ent.igniteForSeconds(6);
            }
            SpellCombatReactionEngine.hurtWithApotheosisMateriaAdvantage(
                    level, ent, caster, 11.5F + tier * 2.25F, false, true, true, false
            );
        }

        if (caster != null && caster.isAlive() && caster.position().distanceToSqr(center) <= 400.0D) {
            float fullCaster = caster.getMaxHealth();
            SpellCombatReactionEngine.notifyMagicalHeal(caster, fullCaster);
            caster.heal(fullCaster);
        }

        SpellLingeringZoneManager.spawnTypedEssenceOrb(level, center, EssenceType.RADIANT, 3);
        emitApotheosisBurstParticles(level, center.add(0.0D, 0.8D, 0.0D), 52, 3.2D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.15F, 1.35F);
        level.playSound(null, BlockPos.containing(center), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.75F, 1.52F);
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
        // Section 6.58 Sustained Holy White-Hole Ray & Genesis Cataclysm:
        // Wall-phasing continuous ray; melts 3 armor points/sec, smites undead, and heals allies;
        // sustaining the channel for 5.0 continuous seconds (100t) triggers a Cataclysmic Genesis Bloom
        // (blasts all hostiles 16m back, shatters 8 armor points, purges all hostile positive buffs,
        // and blooms a 14m celestial sanctuary healing allies for 6 hearts + Saturation)!
        int tier = spell.getEffectiveTier();
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        long now = level.getGameTime();

        CompoundTag casterData = EntityHelper.getPersistentData(caster);
        long lastTick = casterData.getLong(TAG_BEAM_LAST_TICK).orElse(-100L);
        boolean sameTickRay = lastTick == now;
        long startTick;
        boolean alreadyBloomed;
        if (heldTicks <= 0 || now - lastTick > 14L) {
            startTick = now;
            alreadyBloomed = false;
            casterData.putLong(TAG_BEAM_START_TICK, startTick);
            casterData.putBoolean(TAG_BEAM_BLOOMED, false);
        } else {
            startTick = casterData.getLong(TAG_BEAM_START_TICK).orElse(now);
            alreadyBloomed = casterData.getBoolean(TAG_BEAM_BLOOMED).orElse(false);
        }
        casterData.putLong(TAG_BEAM_LAST_TICK, now);

        float tickDmg = (3.4F + tier * 0.82F) * Math.max(0.5F, apparatusPotency);
        Vec3 epicenter = blockHit != null ? blockHit.getLocation() : caster.getEyePosition().add(dir.scale(Math.min(18.0D, maxReach)));

        for (LivingEntity hit : hitEntities) {
            if (!hit.isAlive()) continue;
            epicenter = hit.position();
            if (ApexEschatonSingularityHelper.isFriendly(hit, caster)) {
                CompoundTag fData = EntityHelper.getPersistentData(hit);
                if (fData.getLong("ApotheosisBeamLastTick").orElse(-100L) == now) continue;
                fData.putLong("ApotheosisBeamLastTick", now);
                SpellCombatReactionEngine.notifyMagicalHeal(hit, 6.0F);
                hit.heal(6.0F);
                hit.addEffect(new MobEffectInstance(MobEffects.SATURATION, 25, 0, true, false));
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;

            CompoundTag hData = EntityHelper.getPersistentData(hit);
            long prevHit = hData.getLong("ApotheosisBeamLastTick").orElse(-100L);
            if (prevHit == now) continue;
            double shred = (now - prevHit <= 30L)
                    ? Math.min(12.0D, hData.getDouble("ApotheosisBeamArmorMelt").orElse(0.0D) + 1.5D)
                    : 3.0D; // Melts 3 armor points/sec (beam ticks ~2x/sec)!
            hData.putLong("ApotheosisBeamLastTick", now);
            hData.putDouble("ApotheosisBeamArmorMelt", shred);
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, shred, 0.0D, 80L);

            if (ApexEschatonSingularityHelper.isDarkUndeadOrAbyssal(hit)) {
                hit.igniteForSeconds(4);
            }
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithApotheosisMateriaAdvantage(
                        level, hit, caster, tickDmg, false, true, true, false
                );
            }
        }

        // Consecrate corrupted blocks near the ray terminus
        if (!sameTickRay) {
            consecrateCorruptedTerrainInRadius(level, BlockPos.containing(epicenter), 4, 8);
        }

        // 5.0 continuous seconds (100 ticks) -> Cataclysmic Genesis Bloom!
        if (heldTicks >= 100 && !alreadyBloomed && (now - startTick) >= 100L) {
            casterData.putBoolean(TAG_BEAM_BLOOMED, true);
            casterData.putLong(TAG_BEAM_START_TICK, now);

            SpellWorldVfxManager.broadcastRadialNova(level, epicenter, 16.0D, EssenceType.APOTHEOSIS, EssenceType.RADIANT, tier);
            SpellWorldVfxManager.broadcastArcaneSeal(level, epicenter.add(0.0D, 0.04D, 0.0D), 14.0D, EssenceType.APOTHEOSIS, EssenceType.EMPYREAN, 140, tier);
            consecrateCorruptedTerrainInRadius(level, BlockPos.containing(epicenter), 14, 36);

            // Bloom sacred flowering azalea & celestial flora across the 14m sanctuary
            BlockPos cPos = BlockPos.containing(epicenter);
            for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-7, -2, -7), cPos.offset(7, 2, 7))) {
                if (cPos.distSqr(p) > 49.0D) continue;
                BlockState cur = level.getBlockState(p);
                if (!cur.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, p, cur)) continue;
                if (Blocks.FLOWERING_AZALEA.defaultBlockState().canSurvive(level, p) && level.random.nextFloat() < 0.22F) {
                    level.setBlockAndUpdate(p, Blocks.FLOWERING_AZALEA.defaultBlockState());
                }
            }

            AABB blastBox = new AABB(
                    epicenter.x - 16.0D, epicenter.y - 4.0D, epicenter.z - 16.0D,
                    epicenter.x + 16.0D, epicenter.y + 7.0D, epicenter.z + 16.0D
            );
            for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, blastBox, LivingEntity::isAlive)) {
                double dSq = ent.position().distanceToSqr(epicenter);
                if (ent == caster || ApexEschatonSingularityHelper.isFriendly(ent, caster)) {
                    if (dSq <= 196.0D) { // 14m celestial sanctuary!
                        SpellCombatReactionEngine.notifyMagicalHeal(ent, 12.0F); // 6 hearts!
                        ent.heal(12.0F);
                        ent.addEffect(new MobEffectInstance(MobEffects.SATURATION, 120, 0));
                    }
                    continue;
                }
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, spell.hasWardFriendlyFireSafeguard())) continue;

                ApexEschatonSingularityHelper.purgeBeneficialBuffs(ent);
                SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, 8.0D, 0.0D, 240L);
                if (!SpellCombatReactionEngine.isBossEntity(level, ent)) {
                    Vec3 diff = ent.position().subtract(epicenter);
                    Vec3 away = (diff.horizontalDistanceSqr() > 1.0E-4D
                            ? new Vec3(diff.x, 0.0D, diff.z).normalize()
                            : new Vec3(dir.x, 0.0D, dir.z)).scale(3.15D); // 16m knockback!
                    SpellExecutionEngine.applySyncedMotion(ent, new Vec3(away.x, 0.50D, away.z));
                }
                SpellCombatReactionEngine.hurtWithApotheosisMateriaAdvantage(
                        level, ent, caster, 20.0F + tier * 3.25F, false, true, true, false
                );
            }

            // Guarantee caster also receives the 6-heart + Saturation bloom if channeling from >14m away!
            if (caster.position().distanceToSqr(epicenter) > 196.0D) {
                SpellCombatReactionEngine.notifyMagicalHeal(caster, 12.0F);
                caster.heal(12.0F);
                caster.addEffect(new MobEffectInstance(MobEffects.SATURATION, 120, 0));
            }

            if (caster instanceof ServerPlayer sp) {
                sp.displayClientMessage(Component.literal("§e✦ Cataclysmic Genesis Bloom unleashed!"), true);
            }
            emitApotheosisBurstParticles(level, epicenter.add(0.0D, 0.9D, 0.0D), 60, 4.2D);
            level.playSound(null, cPos, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.25F, 1.30F);
            level.playSound(null, cPos, SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.95F, 1.45F);
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
        // Section 6.58 Supernova Genesis Nova:
        // Wall-penetrating 16-block concussive shockwave (rendered purely via shaders/bloom with ZERO light blocks!),
        // knocks all hostiles 14 blocks back flat, shatters 8 armor points, deals massive Tier 6 Apotheosis Materia damage
        // (doubled against Undead, Demonic, and Void mobs), purges all negative debuffs from the party, heals allies for
        // 12 hearts (24 HP), and grants the party Mantle of the Alpha Singularity for 20.0 seconds (400t)!
        int tier = spell.getEffectiveTier();
        double r = Math.max(16.0D, radius);
        double rSq = (r + 0.5D) * (r + 0.5D);
        long mantleUntil = level.getGameTime() + 400L;

        // Pure client shader/bloom shockwaves — strictly NO Blocks.LIGHT placement during Radial Burst!
        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.APOTHEOSIS, EssenceType.RADIANT, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, center.add(0.0D, 0.04D, 0.0D), r, EssenceType.APOTHEOSIS, EssenceType.CELESTIAL, 90, tier);
        consecrateCorruptedTerrainInRadius(level, BlockPos.containing(center), 20, 42);

        float novaDmg = (10.8F + tier * 2.35F) * Math.max(0.5F, apparatusPotency);
        AABB box = new AABB(center.x - r, center.y - 3.5D, center.z - r, center.x + r, center.y + 6.5D, center.z + r);

        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            double dx = ent.getX() - center.x;
            double dz = ent.getZ() - center.z;
            if (dx * dx + dz * dz > rSq) continue;

            if (ent == caster || ApexEschatonSingularityHelper.isFriendly(ent, caster)) {
                grantAlphaSingularityMantleAndHeal(level, ent, mantleUntil, 24.0F); // 12 hearts + 20.0s Mantle!
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, spell.hasWardFriendlyFireSafeguard())) continue;

            if (!SpellCombatReactionEngine.isBossEntity(level, ent)) {
                Vec3 horiz = new Vec3(dx, 0.0D, dz);
                Vec3 away = (horiz.lengthSqr() > 1.0E-4D
                        ? horiz.normalize()
                        : new Vec3(direction.x, 0.0D, direction.z)).scale(2.78D); // 14m flat knockback!
                SpellExecutionEngine.applySyncedMotion(ent, new Vec3(away.x, 0.32D, away.z));
            }
            SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, 8.0D, 0.0D, 240L);
            if (ApexEschatonSingularityHelper.isDarkUndeadOrAbyssal(ent)) {
                ent.igniteForSeconds(6);
            }
            // Doubled against Undead, Demonic, and Void mobs even when paired with utility verbs (or boosts HARM with dark smite)!
            float baseHit = slot.hasHarmVerb() && ApexEschatonSingularityHelper.isDarkUndeadOrAbyssal(ent)
                    ? novaDmg * 0.55F
                    : (!slot.hasHarmVerb() ? novaDmg : 0.0F);
            if (baseHit > 0.0F) {
                SpellCombatReactionEngine.hurtWithApotheosisMateriaAdvantage(
                        level, ent, caster, baseHit, false, true, true, false
                );
            }
        }

        grantAlphaSingularityMantleAndHeal(level, caster, mantleUntil, 24.0F);
        emitApotheosisBurstParticles(level, center.add(0.0D, 1.0D, 0.0D), 64, r * 0.38D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.25F, 1.28F);
        level.playSound(null, BlockPos.containing(center), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.85F, 1.48F);
    }

    private static void grantAlphaSingularityMantleAndHeal(
            ServerLevel level,
            LivingEntity ally,
            long mantleUntil,
            float healAmount
    ) {
        SpellCombatReactionEngine.purifyHexesAndNegativeEffects(ally);
        if (healAmount > 0.0F) {
            SpellCombatReactionEngine.notifyMagicalHeal(ally, healAmount);
            ally.heal(healAmount);
        }
        CompoundTag aData = EntityHelper.getPersistentData(ally);
        aData.putLong(SpellLingeringZoneManager.TAG_APOTHEOSIS_SHROUD_UNTIL,
                Math.max(mantleUntil, aData.getLong(SpellLingeringZoneManager.TAG_APOTHEOSIS_SHROUD_UNTIL).orElse(0L)));
        aData.putLong(SpellLingeringZoneManager.TAG_VOID_IMMUNE_UNTIL,
                Math.max(mantleUntil, aData.getLong(SpellLingeringZoneManager.TAG_VOID_IMMUNE_UNTIL).orElse(0L)));
        int ticks = (int) Math.max(40L, mantleUntil - level.getGameTime());
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, level, 14.0D, 8.0D, 0.0D, 0.0D, ticks);
        SpellLingeringZoneManager.grantArcane3DFlight(ally, ticks);
        ally.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, ticks, 0, true, false));
        SpellCombatReactionEngine.syncEntityBarrierState(level, ally, 0.85F, null);
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
        // Section 6.58 Ascendant Solar Genesis Cleave:
        // Luminous wall-cleaving two-handed blade; 75% armor bypass, shatters 8 armor points, disables shields for 8.0s (160t),
        // smites dark entities with catastrophic holy damage, roots foes in starlight briars for 4.0s (80t),
        // and releases seeking golden crowns to surrounding hostiles within 10m siphoning health into party absorption!
        int tier = spell.getEffectiveTier();
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().add(dir.scale(1.5D)), dir, 5.8D, EssenceType.APOTHEOSIS, EssenceType.RADIANT, tier
        );
        level.playSound(null, caster.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.80F, 1.55F);

        float cleaveDmg = (8.8F + tier * 1.95F) * Math.max(0.5F, apparatusPotency);

        for (LivingEntity hit : hitEntities) {
            if (!hit.isAlive() || ApexEschatonSingularityHelper.isFriendly(hit, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, safeguard)) continue;

            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 8.0D, 0.0D, 240L);
            ApexEschatonSingularityHelper.disableShield(hit, 160); // 8.0s shield disable!
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 6)); // 4.0s starlight briar root!
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 80, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0));
            if (ApexEschatonSingularityHelper.isDarkUndeadOrAbyssal(hit)) {
                hit.igniteForSeconds(5);
            }

            float primaryDealt = 0.0F;
            if (!slot.hasHarmVerb()) {
                primaryDealt = SpellCombatReactionEngine.hurtWithApotheosisMateriaAdvantage(
                        level, hit, caster, cleaveDmg, true, true, true, false
                );
            } else if (ApexEschatonSingularityHelper.isDarkUndeadOrAbyssal(hit)) {
                primaryDealt = SpellCombatReactionEngine.hurtWithApotheosisMateriaAdvantage(
                        level, hit, caster, cleaveDmg * 0.50F, true, true, false, false
                );
            }

            float siphoned = Math.max(5.0F, primaryDealt * 0.45F);
            for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, hit.getBoundingBox().inflate(10.0D),
                    e -> e.isAlive() && e != hit && e != caster && !ApexEschatonSingularityHelper.isFriendly(e, caster))) {
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(adj, caster, safeguard)) continue;
                SpellWorldVfxManager.broadcastChainArc(
                        level, hit.getEyePosition(), adj.getEyePosition(), EssenceType.APOTHEOSIS, EssenceType.RADIANT, tier
                );
                SpellCombatReactionEngine.applyTimedAttributeShred(adj, level, 4.0D, 0.0D, 160L);
                adj.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 5));
                adj.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0));
                float crownDealt = SpellCombatReactionEngine.hurtWithApotheosisMateriaAdvantage(
                        level, adj, caster, 6.0F + tier * 1.40F, true, true, true, false
                );
                siphoned += Math.max(2.5F, crownDealt * 0.50F);
            }
            SpellLingeringZoneManager.grantPartyAbsorption(level, caster, siphoned, 36.0F, 20.0D);
            emitApotheosisBurstParticles(level, hit.position().add(0.0D, hit.getBbHeight() * 0.55D, 0.0D), 22, 0.55D);
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
        // Section 6.58 Mantle of the Alpha Singularity:
        // Level-15 ambient light, +14 Armor, +8 Toughness, complete fire/lava/drowning/freeze/wither/blindness/void immunity,
        // -90% hostile mob detection, 6m 100% physical & magical projectile vaporization into flower petals & radiant droplets,
        // unrestricted 3D flight + Slow Falling, and an 8m consecrated aura (+3 hearts/s + Saturation for allies;
        // 3.0 hearts/s true holy burn to dark/void/nether hostiles)!
        int tier = spell.getEffectiveTier();
        int dur = 360 + tier * 40;
        long now = level.getGameTime();
        long until = now + dur;

        if (!caster.isShiftKeyDown()) {
            grantAlphaSingularityMantleAndHeal(level, caster, until, 0.0F);
        }
        SpellLingeringZoneManager.pulseTemporaryLight(level, caster.blockPosition().above(), 15, 24);
        emitApotheosisBurstParticles(level, caster.position().add(0.0D, 1.0D, 0.0D), 38, 0.65D);
        level.playSound(null, caster.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.95F, 1.32F);

        // Sneak-cast anchors a persistent 8m world Alpha Singularity Sanctuary Dome!
        if (caster.isShiftKeyDown()) {
            double r = Math.max(8.0D, radius);
            Vec3 anchorPos = caster.position();
            SpellWorldVfxManager.broadcastArcaneSeal(level, anchorPos.add(0.0D, 0.04D, 0.0D), r, EssenceType.APOTHEOSIS, EssenceType.RADIANT, dur, tier);
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, anchorPos, r, dur, now, spell, slot, EssenceType.APOTHEOSIS, apparatusPotency,
                    (srv, zone, elapsed) -> {
                        if (elapsed > zone.maxDurationTicks) return false;
                        LivingEntity owner = zone.resolveCaster(srv);
                        if (elapsed % 12 == 0) {
                            SpellLingeringZoneManager.pulseTemporaryLight(srv, BlockPos.containing(zone.center).above(), 15, 16);
                            emitApotheosisBurstParticles(srv, zone.center.add(0.0D, 1.0D, 0.0D), 14, zone.radius * 0.45D);
                        }
                        AABB pBox = new AABB(zone.center.x - 6.0D, zone.center.y - 2.0D, zone.center.z - 6.0D,
                                zone.center.x + 6.0D, zone.center.y + 5.0D, zone.center.z + 6.0D);
                        for (Projectile proj : srv.getEntitiesOfClass(Projectile.class, pBox)) {
                            if (owner != null && proj.getOwner() == owner) continue;
                            if (proj instanceof SpellProjectileEntity sp
                                    && owner != null
                                    && sp.getOwner() instanceof LivingEntity spo
                                    && ApexEschatonSingularityHelper.isFriendly(spo, owner)) {
                                continue;
                            }
                            Vec3 pPos = proj.position();
                            proj.discard();
                            srv.sendParticles(ParticleTypes.CHERRY_LEAVES, pPos.x, pPos.y, pPos.z, 6, 0.15D, 0.15D, 0.15D, 0.03D);
                            srv.sendParticles(ParticleTypes.END_ROD, pPos.x, pPos.y, pPos.z, 5, 0.12D, 0.12D, 0.12D, 0.03D);
                            if (srv.random.nextFloat() < 0.45F) {
                                SpellLingeringZoneManager.spawnTypedEssenceOrb(srv, pPos, EssenceType.RADIANT, 1);
                            }
                        }
                        if (elapsed % 20 == 0) {
                            AABB box = new AABB(zone.center.x - zone.radius, zone.center.y - 2.5D, zone.center.z - zone.radius,
                                    zone.center.x + zone.radius, zone.center.y + 5.5D, zone.center.z + zone.radius);
                            for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                                if (owner != null && (ent == owner || ApexEschatonSingularityHelper.isFriendly(ent, owner))) {
                                    grantAlphaSingularityMantleAndHeal(srv, ent, srv.getGameTime() + 30L, 6.0F);
                                    ent.addEffect(new MobEffectInstance(MobEffects.SATURATION, 25, 0, true, false));
                                } else if (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, owner, true)) {
                                    if (ApexEschatonSingularityHelper.isDarkUndeadOrAbyssal(ent) || ent instanceof Monster) {
                                        ent.igniteForSeconds(4);
                                        SpellCombatReactionEngine.hurtWithApotheosisMateriaAdvantage(
                                                srv, ent, owner, 6.0F, false, false, false, true
                                        );
                                    }
                                }
                            }
                        }
                        return true;
                    }
            ));
        }
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
        // Section 6.58 White-Hole Genesis Sanctuary Sigil:
        // Disarmable solar-verdant celestial mandala glyph (Single or Repeating);
        // - Hostiles within 6m: erupts an indestructible 12-block cage of golden hard-light pillars trapping entering
        //   mobs inside for 6.0s (120t), shatters 8 armor points, strips all active positive mob buffs, smites dark
        //   entities, and leeches 50% of their total health into floating radiant essence orbs!
        // - Allies stepping on glyph: instantly restores full health, cures all negative curses & hexes,
        //   grants +12 Absorption hearts (24.0 HP), and provides 60.0s (1200t) of Regeneration IV!
        final int tier = spell.getEffectiveTier();
        final int glyphDuration = tier >= 5 ? Integer.MAX_VALUE : 12_000;
        String subMode = slot.primaryEffect().subMode();
        final boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        final int[] cooldown = {0};

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, glyphCenter.add(0.0D, 0.03D, 0.0D), 6.0D, EssenceType.APOTHEOSIS, EssenceType.RADIANT, Math.min(240, glyphDuration), tier
        );
        level.playSound(null, BlockPos.containing(glyphCenter), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.85F, 1.38F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 6.0D, glyphDuration, level.getGameTime(), spell, slot, EssenceType.APOTHEOSIS, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (cooldown[0] > 0) {
                        cooldown[0]--;
                        return true;
                    }
                    if (elapsed % 8 == 0) {
                        emitApotheosisBurstParticles(srv, zone.center.add(0.0D, 0.15D, 0.0D), 6, 1.6D);
                    }
                    if (elapsed < 10) return true;

                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB trig = new AABB(
                            zone.center.x - 6.0D, zone.center.y - 2.0D, zone.center.z - 6.0D,
                            zone.center.x + 6.0D, zone.center.y + 3.5D, zone.center.z + 6.0D
                    );
                    List<LivingEntity> steppers = srv.getEntitiesOfClass(LivingEntity.class, trig, LivingEntity::isAlive);
                    if (steppers.isEmpty()) return true;

                    // 1. Check ally sanctuary step (within inner 3.2m mandala core so caster doesn't self-consume at 6m cast time!)
                    for (LivingEntity st : steppers) {
                        if (owner != null && (st == owner || ApexEschatonSingularityHelper.isFriendly(st, owner))) {
                            if (st == owner && elapsed < 24) continue;
                            if (st.position().distanceToSqr(zone.center) > 10.24D) continue;

                            SpellCombatReactionEngine.purifyHexesAndNegativeEffects(st);
                            for (EquipmentSlot eq : EquipmentSlot.values()) {
                                PenumbraSpellMutationHandler.purgeCursesFromStack(st.getItemBySlot(eq));
                            }
                            float full = st.getMaxHealth();
                            SpellCombatReactionEngine.notifyMagicalHeal(st, full);
                            st.heal(full);
                            st.setAbsorptionAmount(Math.max(st.getAbsorptionAmount(), 24.0F)); // +12 Absorption hearts!
                            st.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1200, 3)); // 60.0s Regeneration IV!
                            srv.sendParticles(ParticleTypes.HEART, st.getX(), st.getY(1.0D), st.getZ(), 12, 0.4D, 0.4D, 0.4D, 0.03D);
                            srv.playSound(null, st.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.42F);

                            if (repeating) {
                                cooldown[0] = 140;
                                return true;
                            }
                            return false;
                        }
                    }

                    // 2. Check hostile approach within 6m -> erupt 12m Golden Hard-Light Pillar Cage for 6.0s (120t)!
                    boolean hasHostile = false;
                    for (LivingEntity st : steppers) {
                        if (owner != null && ApexEschatonSingularityHelper.isFriendly(st, owner)) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                st, owner, zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard())) {
                            continue;
                        }
                        if (st instanceof Monster || (owner != null && st == owner.getLastHurtMob())) {
                            hasHostile = true;
                            break;
                        }
                    }
                    if (!hasHostile) return true;

                    int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;
                    SpellWorldVfxManager.broadcastArcaneSeal(
                            srv, zone.center.add(0.0D, 0.04D, 0.0D), 12.0D, EssenceType.APOTHEOSIS, EssenceType.RADIANT, 120, zTier
                    );
                    for (int p = 0; p < 8; p++) {
                        double pAng = (Math.PI * 2.0D * p) / 8.0D;
                        Vec3 pillarPos = zone.center.add(Math.cos(pAng) * 11.5D, 0.0D, Math.sin(pAng) * 11.5D);
                        SpellWorldVfxManager.broadcastSummonPillar(srv, pillarPos, 1.35D, EssenceType.APOTHEOSIS, EssenceType.RADIANT, zTier);
                    }

                    AABB cage = new AABB(
                            zone.center.x - 12.0D, zone.center.y - 3.0D, zone.center.z - 12.0D,
                            zone.center.x + 12.0D, zone.center.y + 6.5D, zone.center.z + 12.0D
                    );
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, cage, e -> e.isAlive() && e != owner)) {
                        if (owner != null && ApexEschatonSingularityHelper.isFriendly(foe, owner)) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                foe, owner, zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard())) {
                            continue;
                        }
                        ApexEschatonSingularityHelper.purgeBeneficialBuffs(foe);
                        SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 8.0D, 0.0D, 240L);
                        foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 6));
                        foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 120, 0));

                        // Leech 50% of total health (boss-safe cap 60 HP) + smite dark entities & spawn Radiant essence orbs!
                        float leechHp = SpellCombatReactionEngine.isBossEntity(srv, foe)
                                ? Math.min(60.0F, foe.getMaxHealth() * 0.20F)
                                : foe.getMaxHealth() * 0.50F;
                        SpellCombatReactionEngine.hurtWithApotheosisMateriaAdvantage(
                                srv, foe, owner, Math.max(6.0F, leechHp), false, false, true, true
                        );
                        SpellLingeringZoneManager.spawnTypedEssenceOrb(srv, foe.position().add(0.0D, 0.5D, 0.0D), EssenceType.RADIANT, 2);
                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, foe, new Vec3(0.0D, 1.0D, 0.0D),
                                    zone.spell, zone.slot, EssenceType.APOTHEOSIS, zone.apparatusPotency
                            );
                        }
                    }

                    // Active 6.0s (120t) indestructible 12m hard-light cage wall enforcement!
                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 12.0D, 120, srv.getGameTime(), zone.spell, zone.slot, EssenceType.APOTHEOSIS, zone.apparatusPotency,
                            (cSrv, cZone, cElapsed) -> {
                                if (cElapsed > cZone.maxDurationTicks) return false;
                                if (cElapsed % 10 == 0) {
                                    for (int p = 0; p < 8; p++) {
                                        double pAng = (Math.PI * 2.0D * p) / 8.0D;
                                        cSrv.sendParticles(
                                                ParticleTypes.END_ROD,
                                                cZone.center.x + Math.cos(pAng) * 11.5D,
                                                cZone.center.y + 1.6D,
                                                cZone.center.z + Math.sin(pAng) * 11.5D,
                                                4, 0.15D, 1.4D, 0.15D, 0.02D
                                        );
                                    }
                                }
                                LivingEntity cOwner = cZone.resolveCaster(cSrv);
                                AABB boundary = new AABB(
                                        cZone.center.x - 13.0D, cZone.center.y - 3.0D, cZone.center.z - 13.0D,
                                        cZone.center.x + 13.0D, cZone.center.y + 6.5D, cZone.center.z + 13.0D
                                );
                                for (LivingEntity trapped : cSrv.getEntitiesOfClass(LivingEntity.class, boundary, LivingEntity::isAlive)) {
                                    if (trapped == cOwner || (cOwner != null && ApexEschatonSingularityHelper.isFriendly(trapped, cOwner))) continue;
                                    if (!(trapped instanceof Monster)) continue;
                                    Vec3 rel = new Vec3(trapped.getX() - cZone.center.x, 0.0D, trapped.getZ() - cZone.center.z);
                                    double dist = rel.length();
                                    if (dist > 10.6D) {
                                        Vec3 inward = rel.normalize().scale(-0.65D);
                                        trapped.setDeltaMovement(inward.x, Math.min(0.0D, trapped.getDeltaMovement().y), inward.z);
                                        trapped.hasImpulse = true;
                                    }
                                }
                                return true;
                            }
                    ));

                    emitApotheosisBurstParticles(srv, zone.center.add(0.0D, 1.0D, 0.0D), 42, 3.2D);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.15F, 1.30F);

                    if (repeating) {
                        cooldown[0] = 160;
                        return true;
                    }
                    return false;
                }
        ).markDisarmableGlyph().withOnDisarm((srv, z) ->
                emitApotheosisBurstParticles(srv, z.center.add(0.0D, 0.3D, 0.0D), 14, 0.45D)));

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
        // Section 6.58 Prismatic Solar Fan:
        // Wide wall-penetrating fan of rainbow lasers, white-hole starlight, and blazing solar flares;
        // shatters 8 armor points, blinds hostiles, ignites undead, knocks enemies back with heavy solar wind force;
        // vitrifies sand -> smooth crystal glass, blooms flowers, accelerates crop growth, turns netherrack -> lush moss,
        // purges withered foliage, and inflicts Divine Reverence (35.0s / 700t AI pathfinding disruption & -75% move speed)!
        int tier = spell.getEffectiveTier();
        double reach = Math.max(14.0D, coneRange);
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 origin = caster.getEyePosition();

        SpellWorldVfxManager.broadcastVortexCone(
                level, origin.subtract(0.0D, 0.2D, 0.0D), dir, reach, EssenceType.APOTHEOSIS, EssenceType.PRISMATIC, tier
        );

        // Sweep environmental consecration & vitrification across the entire fan path
        Block[] sacredFlowers = new Block[]{
                Blocks.OXEYE_DAISY,
                Blocks.CORNFLOWER,
                Blocks.ALLIUM,
                Blocks.AZURE_BLUET,
                Blocks.FLOWERING_AZALEA
        };
        for (int step = 2; step <= (int) Math.min(14.0D, reach); step += 3) {
            BlockPos stepCenter = BlockPos.containing(origin.add(dir.scale(step)));
            int spread = Math.min(5, 2 + step / 4);
            for (BlockPos p : BlockPos.betweenClosed(stepCenter.offset(-spread, -2, -spread), stepCenter.offset(spread, 2, spread))) {
                BlockState bs = level.getBlockState(p);
                if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
                if (bs.is(Blocks.SAND) || bs.is(Blocks.RED_SAND)) {
                    level.setBlockAndUpdate(p, Blocks.GLASS.defaultBlockState());
                } else if (bs.is(Blocks.NETHERRACK) || bs.is(Blocks.CRIMSON_NYLIUM) || bs.is(Blocks.WARPED_NYLIUM)) {
                    level.setBlockAndUpdate(p, Blocks.MOSS_BLOCK.defaultBlockState());
                } else if (bs.is(Blocks.WITHER_ROSE) || bs.is(Blocks.DEAD_BUSH)) {
                    Block flower = sacredFlowers[level.random.nextInt(sacredFlowers.length)];
                    if (flower.defaultBlockState().canSurvive(level, p)) {
                        level.setBlockAndUpdate(p, flower.defaultBlockState());
                    } else {
                        level.removeBlock(p, false);
                    }
                } else if (bs.getBlock() instanceof CropBlock crop && !crop.isMaxAge(bs)) {
                    crop.growCrops(level, p, bs);
                } else if ((bs.is(Blocks.GRASS_BLOCK) || bs.is(Blocks.MOSS_BLOCK))
                        && level.getBlockState(p.above()).isAir()
                        && level.random.nextFloat() < 0.14F) {
                    Block flower = sacredFlowers[level.random.nextInt(sacredFlowers.length)];
                    if (flower.defaultBlockState().canSurvive(level, p.above())) {
                        level.setBlockAndUpdate(p.above(), flower.defaultBlockState());
                    }
                }
            }
        }

        float fanDmg = (8.4F + tier * 1.90F) * Math.max(0.5F, apparatusPotency);

        for (LivingEntity hit : hitTargets) {
            if (!hit.isAlive() || ApexEschatonSingularityHelper.isFriendly(hit, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;

            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 8.0D, 0.0D, 240L);
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 240, 0));
            if (ApexEschatonSingularityHelper.isDarkUndeadOrAbyssal(hit)) {
                hit.igniteForSeconds(8);
            }
            if (!SpellCombatReactionEngine.isBossEntity(level, hit)) {
                SpellExecutionEngine.applySyncedMotion(hit, new Vec3(dir.x * 2.10D, 0.42D, dir.z * 2.10D));
            }

            // Divine Reverence: 35.0s (700t) AI pathfinding disruption + exact -75% movement speed!
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(hit, level, 0.75D, 700L);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 700, 4));
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 700);
            if (hit instanceof Mob mob) {
                mob.getNavigation().stop();
                mob.setTarget(null);
                mob.setAggressive(false);
            }

            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithApotheosisMateriaAdvantage(
                        level, hit, caster, fanDmg, false, true, true, false
                );
            }
        }

        for (int s = 3; s <= 12; s += 3) {
            emitApotheosisBurstParticles(level, origin.add(dir.scale(s)), 16, s * 0.22D);
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.10F, 1.38F);
        level.playSound(null, caster.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.65F, 1.55F);
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
        // Section 6.58 Cataclysmic White-Hole Genesis Starfall [30.0s Bombardment + 15.0s Consecrated Genesis Ground = 900t, 20m AOE]:
        // Calls down 8 colossal 3D Skyfall Pseudoblock falling celestial starcores & golden solar megaliths that hold
        // for the active spell duration and permanently materialize into real blocks on completion; crushes hostiles,
        // continuously knocks them flat, shatters 8 armor points, smites dark entities with catastrophic holy damage,
        // consecrates corrupted terrain across 20m, and leaves a lingering 15.0s Consecrated Genesis Ground
        // (roots hostiles every 3.0s, slows by 80%, spawns radiant essence orbs, and heals allies +3 hearts/s
        // while providing Saturation, Regeneration IV, and +8 Armor)!
        final int tier = spell.getEffectiveTier();
        final double r = Math.max(20.0D, radius);
        final BlockPos centerPos = BlockPos.containing(impactCenter);
        final double skyH = SpellDeliveryForm.probeCaveSafeSkyfallHeight(level, centerPos, tier);

        SpellWorldVfxManager.broadcastSkyfallPillar(level, impactCenter, r, EssenceType.APOTHEOSIS, EssenceType.RADIANT, 900, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, impactCenter.add(0.0D, 0.04D, 0.0D), r, EssenceType.APOTHEOSIS, EssenceType.CELESTIAL, 900, tier);
        level.playSound(null, centerPos, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.25F, 1.25F);
        level.playSound(null, centerPos, SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.95F, 1.42F);

        final int megalithCount = 8;
        final BlockPos[] megalithTargets = new BlockPos[megalithCount];
        megalithTargets[0] = SpellWorldVfxManager.findSkyfallSurfaceBase(level, centerPos);
        for (int i = 1; i < megalithCount; i++) {
            double ang = (Math.PI * 2.0D * (i - 1) / (megalithCount - 1.0D)) + level.random.nextDouble() * 0.40D;
            double dist = 0.22D * r + level.random.nextDouble() * (0.58D * r);
            BlockPos raw = BlockPos.containing(impactCenter.x + Math.cos(ang) * dist, impactCenter.y, impactCenter.z + Math.sin(ang) * dist);
            megalithTargets[i] = SpellWorldVfxManager.findSkyfallSurfaceBase(level, raw);
        }

        final boolean[] landed = new boolean[megalithCount];
        final boolean[] materialized = {false};

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 900, level.getGameTime(), spell, slot, EssenceType.APOTHEOSIS, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed >= zone.maxDurationTicks) {
                        if (!materialized[0]) {
                            materialized[0] = true;
                            for (int mIdx = 0; mIdx < megalithCount; mIdx++) {
                                BlockState pseudoState = resolveSolarMegalithBlockState(mIdx);
                                int colHeight = (mIdx % 2 == 0) ? 2 : 1;
                                SpellWorldVfxManager.materializeSkyfallPseudoblockColumn(srv, megalithTargets[mIdx], pseudoState, colHeight);
                            }
                        }
                        return false;
                    }

                    boolean starfall = elapsed < 600; // 30.0s active bombardment, then 15.0s Consecrated Genesis Ground!
                    int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;

                    if (elapsed == 16 || elapsed == 240 || elapsed == 480) {
                        consecrateCorruptedTerrainInRadius(srv, BlockPos.containing(zone.center), 20, 32);
                    }

                    if (starfall) {
                        for (int mIdx = 0; mIdx < megalithCount; mIdx++) {
                            int impactTick = 24 + mIdx * 68;
                            BlockPos strikePos = megalithTargets[mIdx];
                            BlockState pseudoState = resolveSolarMegalithBlockState(mIdx);
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
                                        EssenceType.APOTHEOSIS,
                                        zTier
                                );
                            } else if (elapsed == impactTick) {
                                landed[mIdx] = true;
                                Vec3 hitVec = new Vec3(strikePos.getX() + 0.5D, strikePos.getY(), strikePos.getZ() + 0.5D);
                                SpellWorldVfxManager.broadcastProjectileImpact(
                                        srv, hitVec.add(0.0D, 0.25D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                                        EssenceType.APOTHEOSIS, EssenceType.RADIANT, 3.05F, zTier
                                );
                                srv.sendParticles(
                                        new BlockParticleOption(ParticleTypes.BLOCK, pseudoState),
                                        hitVec.x, hitVec.y + 0.6D, hitVec.z, 26, 1.3D, 0.5D, 1.3D, 0.10D
                                );
                                srv.playSound(null, strikePos, SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.85F, 1.35F);
                            }
                        }

                        if (elapsed % 8 == 0) {
                            double ang = srv.random.nextDouble() * Math.PI * 2.0D;
                            double dist = Math.sqrt(srv.random.nextDouble()) * (zone.radius * 0.88D);
                            Vec3 rayImpact = zone.center.add(Math.cos(ang) * dist, 0.25D, Math.sin(ang) * dist);
                            SpellWorldVfxManager.broadcastProjectileImpact(
                                    srv, rayImpact, new Vec3(0.0D, 1.0D, 0.0D),
                                    EssenceType.APOTHEOSIS, EssenceType.PRISMATIC, 1.85F, zTier
                            );
                        }
                    } else if (elapsed % 80 == 0) {
                        // Generate collectible Radiant essence orbs during Consecrated Genesis Ground!
                        double ang = srv.random.nextDouble() * Math.PI * 2.0D;
                        double dist = srv.random.nextDouble() * (zone.radius * 0.45D);
                        SpellLingeringZoneManager.spawnTypedEssenceOrb(
                                srv,
                                zone.center.add(Math.cos(ang) * dist, 0.4D, Math.sin(ang) * dist),
                                EssenceType.RADIANT,
                                2
                        );
                    }

                    for (int mIdx = 0; mIdx < megalithCount; mIdx++) {
                        if (landed[mIdx]) {
                            SpellWorldVfxManager.repelEntitiesFromPseudoblockColumn(srv, megalithTargets[mIdx], (mIdx % 2 == 0) ? 2 : 1, false);
                        }
                    }

                    if (elapsed % 8 == 0) {
                        emitApotheosisBurstParticles(
                                srv,
                                zone.center.add(0.0D, starfall ? 5.5D : 0.4D, 0.0D),
                                28,
                                zone.radius * 0.52D
                        );
                    }

                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        float starfallDmg = (7.6F + zTier * 1.75F) * Math.max(0.5F, zone.apparatusPotency);
                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 4.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 14.0D, zone.center.z + zone.radius
                        );
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (owner != null && (ent == owner || ApexEschatonSingularityHelper.isFriendly(ent, owner))) {
                                // Allies receive +3 hearts/sec (6.0 HP), Saturation, Regeneration IV, and +8 Armor!
                                SpellCombatReactionEngine.notifyMagicalHeal(ent, 6.0F);
                                ent.heal(6.0F);
                                ent.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 35, 3, true, false)); // Regen IV!
                                ent.addEffect(new MobEffectInstance(MobEffects.SATURATION, 25, 0, true, false));
                                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ent, srv, 8.0D, 0.0D, 0.0D, 0.0D, 35L);
                                continue;
                            }
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, owner, wardSafe)) {
                                continue;
                            }
                            if (starfall) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(ent, srv, 8.0D, 0.0D, 220L);
                                ent.setDeltaMovement(ent.getDeltaMovement().x * 0.2D, -0.75D, ent.getDeltaMovement().z * 0.2D);
                                ent.hasImpulse = true;
                                if (ApexEschatonSingularityHelper.isDarkUndeadOrAbyssal(ent)) {
                                    ent.igniteForSeconds(5);
                                }
                                if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                                    SpellCombatReactionEngine.hurtWithApotheosisMateriaAdvantage(
                                            srv, ent, owner, starfallDmg, false, true, true, false
                                    );
                                }
                                if (zone.spell != null && zone.slot != null) {
                                    SpellExecutionEngine.deliverSubSpellOnEntity(
                                            srv, owner, ent, new Vec3(0.0D, -1.0D, 0.0D),
                                            zone.spell, zone.slot, EssenceType.APOTHEOSIS, zone.apparatusPotency * 0.60F
                                    );
                                }
                            } else {
                                // 15.0s Consecrated Genesis Ground: exact -80% slow + roots hostiles every 3.0s (60t)!
                                SpellCombatReactionEngine.applyTimedMovementSpeedSap(ent, srv, 0.80D, 35L);
                                ent.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 5));
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

    private static BlockState resolveSolarMegalithBlockState(int index) {
        return switch (index & 3) {
            case 0 -> Blocks.QUARTZ_PILLAR.defaultBlockState();
            case 1 -> Blocks.SEA_LANTERN.defaultBlockState();
            case 2 -> Blocks.CALCITE.defaultBlockState();
            default -> Blocks.MOSS_BLOCK.defaultBlockState();
        };
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
        tag.putString("SpellEidolonMateria", EssenceType.APOTHEOSIS.name());
        tag.putInt("SpellEidolonTintRGB", APOTHEOSIS_ASTRAL_GOLD_HEX_COLOR);
        SpellWorldVfxManager.broadcastSummonPillar(
                level, spawnPos, Math.max(2.0D, radius), EssenceType.APOTHEOSIS, EssenceType.RADIANT, spell.getEffectiveTier()
        );
        emitApotheosisBurstParticles(level, spawnPos.add(0.0D, 1.1D, 0.0D), 40, 0.65D);
    }

    // =========================================================================
    // PART 3: CORE EFFECT MUTATIONS — BLINK, TRANSMUTE, AFFLICTION & WARD
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Section 6.58 Ascendant Solar Genesis Step:
        // Dematerializes caster on a blinding pillar of white-hole light up to 24 blocks through solid walls & ceilings;
        // - Departure: 8-block radial flashbang blinding hostiles for 6.0s (120t) & rooting in golden briars for 3.0s (60t);
        // - Arrival: 12.0s (240t) unrestricted 3D arcane flight, Slow Falling, complete fall & void damage immunity,
        //   drops healing celestial dewdrops onto the ground below, and sheds 100% of hostile mob aggro!
        SpellWorldVfxManager.broadcastRadialNova(level, departurePos, 8.0D, EssenceType.APOTHEOSIS, EssenceType.RADIANT, potencyTier);
        SpellWorldVfxManager.broadcastSummonPillar(level, arrivalPos, 1.6D, EssenceType.APOTHEOSIS, EssenceType.PRISMATIC, potencyTier);

        AABB dep = new AABB(
                departurePos.x - 8.0D, departurePos.y - 2.5D, departurePos.z - 8.0D,
                departurePos.x + 8.0D, departurePos.y + 4.5D, departurePos.z + 8.0D
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, dep, e -> e.isAlive() && e != caster)) {
            if (ApexEschatonSingularityHelper.isFriendly(foe, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0)); // 6.0s Blindness!
            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 6));   // 3.0s golden briar root!
            foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0));
        }

        SpellLingeringZoneManager.grantArcane3DFlight(caster, 240); // 12.0s unrestricted 3D arcane flight!
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 260, 0));
        caster.resetFallDistance();
        EntityHelper.getPersistentData(caster).putLong(
                SpellLingeringZoneManager.TAG_VOID_IMMUNE_UNTIL, level.getGameTime() + 240L
        );

        // Drop healing celestial dewdrops onto the ground directly below the arrival point!
        BlockPos groundBelow = BlockPos.containing(arrivalPos);
        for (int dy = 0; dy < 28; dy++) {
            BlockPos check = groundBelow.below(dy);
            if (!level.getBlockState(check).isAir()) {
                groundBelow = check.above();
                break;
            }
        }
        Vec3 dewPos = new Vec3(groundBelow.getX() + 0.5D, groundBelow.getY() + 0.1D, groundBelow.getZ() + 0.5D);
        SpellLingeringZoneManager.spawnCollectableHealingOrb(level, dewPos, caster, 8.0F, false);
        SpellLingeringZoneManager.spawnCollectableHealingOrb(level, dewPos.add(0.85D, 0.0D, -0.65D), caster, 8.0F, false);

        ApexEschatonSingularityHelper.shedMobAggro(level, caster, arrivalPos, 28.0D);
        emitApotheosisBurstParticles(level, arrivalPos.add(0.0D, 0.9D, 0.0D), 28, 0.55D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.40F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Section 6.58 Alpha Genesis Transmutation & Universal Consecration [5.0s Channel, 16m radius, strictly NO air transmutation]:
        // - Purifies netherrack, soul sand, sculk, and deepslate into Calcite, Flowering Azalea Moss, Lush Loam, and Amethyst Blocks;
        // - Repairs cracked structural bricks;
        // - Converts destroyed cursed blocks directly into Radiant Raw Essence and Materia droplets;
        // - Cleanses curses from all worn equipment & permanently elevates up to 2 enchantment tiers (+2);
        // - Instantly cures Zombie Villagers back to healthy Villagers with permanent maximum trade discounts;
        // - Converts standing water within 4m into Nectar of the Alpha Genesis when bottled with empty glass bottles!
        if (state.isAir()) return false;

        if (ApexEschatonSingularityHelper.beginOrExecuteChanneledTransmute(
                level, caster, pos, 16, potencyTier, EssenceType.APOTHEOSIS,
                "Alpha Genesis Transmutation & Universal Consecration",
                () -> {
                    ApexEschatonSingularityHelper.cureZombieVillagersWithMaxDiscounts(level, caster, pos, 16.0D);
                    if (caster != null) {
                        ApexEschatonSingularityHelper.purgeCursesAndElevateEquipment(caster, 2, 1.0F, 2);
                    }
                    if (!ApexEschatonSingularityHelper.tryBottleStandingWaterElixir(level, caster, pos, 4, EssenceType.APOTHEOSIS)
                            && caster != null) {
                        ApexEschatonSingularityHelper.tryBottleStandingWaterElixir(level, caster, caster.blockPosition(), 4, EssenceType.APOTHEOSIS);
                    }
                })) {
            return true;
        }

        return consecrateSingleBlock(level, pos, state, true);
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        if (ApexEschatonSingularityHelper.purgeCursesAndElevateStack(stack, level.random, 2, 1.0F, 2)) {
            itemEntity.setItem(stack.copy());
            emitApotheosisBurstParticles(level, itemEntity.position().add(0.0D, 0.3D, 0.0D), 8, 0.25D);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.58 Divine Genesis Brand Hex:
        // Continuous true holy burn damage (doubled 2x against Undead, Demonic, and Abyssal entities),
        // completely disables all natural, potion, and regenerative health recovery, 60% attack fumble/miss chance,
        // shreds 60% of armor toughness, and dark-magic backfire (enforced in SpellExecutionEngine)!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.92D, 0.60D * mult), 45L);
        target.removeEffect(MobEffects.REGENERATION);
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 45, 0, true, true));

        LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCasterPublic(level, target);
        float burnDmg = (3.50F + potencyTier * 0.85F) * mult;
        SpellCombatReactionEngine.hurtWithApotheosisMateriaAdvantage(
                level, target, hexCaster, burnDmg, false, false, true, true
        );
        emitApotheosisBurstParticles(level, target.position().add(0.0D, target.getBbHeight() * 0.6D, 0.0D), 6, 0.30D);
        return true;
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
        if (effect == SpellCoreEffect.WARD && (caster == null || target == caster || ApexEschatonSingularityHelper.isFriendly(target, caster))) {
            SpellCombatReactionEngine.purifyHexesAndNegativeEffects(target);
            target.setAbsorptionAmount(Math.max(target.getAbsorptionAmount(), 8.0F + potencyTier * 2.0F));
        }
    }

    // =========================================================================
    // SHARED DIVINE CONSECRATION & PARTICLE HELPERS
    // =========================================================================

    private static void consecrateCorruptedTerrainInRadius(
            ServerLevel level,
            BlockPos center,
            int radius,
            int maxConversions
    ) {
        int r = Math.clamp(radius, 1, 20);
        int rSq = r * r;
        int converted = 0;
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -2, -r), center.offset(r, 2, r))) {
            if (converted >= maxConversions) break;
            if (center.distSqr(p) > (double) rSq) continue;
            BlockState bs = level.getBlockState(p);
            if (bs.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
            if (consecrateSingleBlock(level, p, bs, false)) {
                converted++;
            }
        }
    }

    private static boolean consecrateSingleBlock(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            boolean fullYieldFromCursed
    ) {
        // 1. Structural repair of cracked bricks!
        if (state.is(Blocks.CRACKED_STONE_BRICKS)) {
            level.setBlockAndUpdate(pos, Blocks.STONE_BRICKS.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.CRACKED_DEEPSLATE_BRICKS)) {
            level.setBlockAndUpdate(pos, Blocks.DEEPSLATE_BRICKS.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.CRACKED_DEEPSLATE_TILES)) {
            level.setBlockAndUpdate(pos, Blocks.DEEPSLATE_TILES.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.CRACKED_NETHER_BRICKS) || state.is(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS)) {
            level.setBlockAndUpdate(pos, Blocks.QUARTZ_BRICKS.defaultBlockState());
            return true;
        }

        // 2. Cursed flora / catalyst dissolution directly into Radiant Raw Essence & Materia droplets!
        if (state.is(Blocks.WITHER_ROSE) || state.is(Blocks.SCULK_VEIN)
                || state.is(Blocks.SCULK_CATALYST) || state.is(Blocks.SCULK_SHRIEKER)) {
            level.removeBlock(pos, false);
            spawnRadiantRawEssenceAndDroplets(level, pos, fullYieldFromCursed ? 1.0F : 0.35F);
            return true;
        }

        // 3. Corrupted & Nether terrain purification into Calcite, Flowering Azalea Moss, Lush Loam, Quartz, and Amethyst!
        boolean wasCursed = state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL)
                || state.is(Blocks.SCULK) || state.is(Blocks.CRYING_OBSIDIAN);
        if (wasCursed
                || state.is(Blocks.NETHERRACK)
                || state.is(Blocks.CRIMSON_NYLIUM)
                || state.is(Blocks.WARPED_NYLIUM)
                || state.is(Blocks.BLACKSTONE)
                || state.is(Blocks.BASALT)
                || state.is(Blocks.DEEPSLATE)
                || state.is(Blocks.COBBLED_DEEPSLATE)
                || state.is(Blocks.TUFF)) {
            int pick = level.random.nextInt(5);
            BlockState out = switch (pick) {
                case 0 -> Blocks.CALCITE.defaultBlockState();
                case 1 -> Blocks.MOSS_BLOCK.defaultBlockState();
                case 2 -> Blocks.PODZOL.defaultBlockState();
                case 3 -> Blocks.AMETHYST_BLOCK.defaultBlockState();
                default -> Blocks.QUARTZ_BLOCK.defaultBlockState();
            };
            level.setBlockAndUpdate(pos, out);
            if (pick == 1 && level.getBlockState(pos.above()).isAir() && level.random.nextFloat() < 0.55F) {
                level.setBlockAndUpdate(pos.above(), Blocks.FLOWERING_AZALEA.defaultBlockState());
            }
            if (wasCursed) {
                spawnRadiantRawEssenceAndDroplets(level, pos, fullYieldFromCursed ? 0.45F : 0.20F);
            }
            return true;
        }
        return false;
    }

    private static void spawnRadiantRawEssenceAndDroplets(ServerLevel level, BlockPos pos, float chance) {
        if (level.random.nextFloat() >= chance) return;
        Vec3 center = Vec3.atCenterOf(pos);
        SpellLingeringZoneManager.spawnTypedEssenceOrb(level, center, EssenceType.RADIANT, 2);
        if (ModItems.WEAK_ESSENCE.isPresent()) {
            Item rawEssenceItem = ModItems.WEAK_ESSENCE.get();
            ItemStack rawStack = new ItemStack(rawEssenceItem, 1);
            if (rawEssenceItem instanceof EssenceItem) {
                EssenceItem.setEssenceType(rawStack, EssenceType.RADIANT);
            }
            ItemEntity drop = new ItemEntity(level, center.x, center.y + 0.25D, center.z, rawStack);
            drop.setDefaultPickUpDelay();
            level.addFreshEntity(drop);
        }
    }

    private static void emitApotheosisBurstParticles(ServerLevel level, Vec3 center, int count, double spread) {
        int third = Math.max(1, count / 3);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), APOTHEOSIS_SUPERNOVA_WHITE_HEX_COLOR, 22, 1.05F, 0.05F),
                center.x, center.y, center.z, third, spread, spread * 0.55D, spread, 0.04D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SUNBEAM.get(), APOTHEOSIS_ASTRAL_GOLD_HEX_COLOR, 24, 1.10F, 0.05F),
                center.x, center.y, center.z, third, spread, spread * 0.55D, spread, 0.04D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), APOTHEOSIS_PRISMATIC_CYAN_HEX_COLOR, 20, 0.95F, 0.05F),
                center.x, center.y, center.z, third, spread, spread * 0.55D, spread, 0.04D
        );
        level.sendParticles(
                ParticleTypes.END_ROD,
                center.x, center.y, center.z, Math.max(4, count / 2), spread, spread * 0.50D, spread, 0.06D
        );
    }
}
