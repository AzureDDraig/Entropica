package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.registry.ModBlocks;
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
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.spell.vfx.SpellWorldVfxManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.TorchBlock;
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
 * Complete per-part Signature Mutation implementation for
 * <b>Cataclysm Materia (#53 — Void + Umbral + Nether, Tier 5 Eschaton Fusion: "The World Breaker")</b>
 * (Spellcrafting System Guide Section 6.55).
 *
 * <p><b>Palette</b>: Void Deep Indigo ({@code #41304E}), Umbral Shadow Charcoal ({@code #211A21}),
 * and Nether Blazing Rust Crimson ({@code #B83C08}); text format code {@code "§c"}.
 *
 * <p><b>Tier 5 Eschaton Scaling</b>: Wall-phasing delivery, 60% armor bypass, 6–7 armor shredding,
 * 24.0s + 12.0s Skyfall duration, 60.0s summon lifecycles (110 HP Serpent / 120 HP Volcanic Spire), and 16-block effect radii.
 *
 * <p><b>Elemental Affinity</b>: Strong against {@code AIR (+1)}, {@code RADIANT (+1)}, {@code NATURE (+1)},
 * {@code FROZEN (+1)}, {@code WATER (+1)}, {@code CELESTIAL (+1)}, {@code AEGIS (+1)}, {@code AURORA (+1)},
 * and {@code GENESIS (+1)} | Weak against {@code VITAE (-1)} (Forced Single Weakness).
 */
public final class CataclysmSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int CATACLYSM_VOID_INDIGO_HEX_COLOR = 0x41304E;
    public static final int CATACLYSM_UMBRAL_CHARCOAL_HEX_COLOR = 0x211A21;
    public static final int CATACLYSM_STYGIAN_CRIMSON_HEX_COLOR = 0xB83C08;

    public static final int[] CATACLYSM_PALETTE = new int[]{
            CATACLYSM_VOID_INDIGO_HEX_COLOR,
            CATACLYSM_UMBRAL_CHARCOAL_HEX_COLOR,
            CATACLYSM_STYGIAN_CRIMSON_HEX_COLOR
    };

    @Override
    public EssenceType materiaType() {
        return EssenceType.CATACLYSM;
    }

    @Override
    public @Nullable String resolveFlowTitle(SpellFlowModifier flow) {
        return flow == SpellFlowModifier.ORBIT_SATELLITE ? "Singularity Orbit Flow" : null;
    }

    @Override
    public @Nullable String resolveFlowDescription(SpellFlowModifier flow) {
        return flow == SpellFlowModifier.ORBIT_SATELLITE
                ? "Manifests 4 persistent collapsed void-magma cores (#41304E / #211A21 / #B83C08) circling "
                        + "the primary spell or caster; phases through solid walls, intercepts and crushes 100% of "
                        + "incoming enemy projectiles, and automatically discharges seeking railgun obsidian spikes "
                        + "at nearby hostiles (-40% Armor Toughness and true fire damage)."
                : null;
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Catastrophic Hellfire Railgun Slug";
            case BEAM -> "Tectonic Magma Fissure Ray & Seismic Cataclysm";
            case RADIAL_BURST -> "World-Breaker Seismic Nova";
            case TOUCH_STRIKE -> "Tectonic Calamity Sunder";
            case AURA -> "Mantle of the World-Breaker";
            case GLYPH -> "Abyssal Calamity Caldera Sigil";
            case CONE -> "Cataclysmic Magma-Ash Deluge";
            case SKYFALL -> "Cataclysmic World-Breaker Skyfall";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE ->
                    "Launches an ultra-dense, wall-penetrating obsidian spike encased in compressed dark matter and "
                            + "roaring netherfire (#41304E / #211A21 / #B83C08) at ultra-high velocity; pierces solid "
                            + "terrain and up to 10 entities (60% armor bypass), triggering a secondary volcanic magma "
                            + "eruption on every entity penetrated (knocking mobs flat, -35% Armor Toughness, and "
                            + "inflicting unquenchable stygian burns).";
            case BEAM ->
                    "Channels a colossal wall-phasing ray of light-devouring void fire, scorched ash, and pressurized "
                            + "molten lava; melts 2 armor/sec, steadily pulls distant enemies into its searing core, "
                            + "and disintegrates incoming arrows; 5.0s continuous channeling triggers Cataclysmic "
                            + "Seismic Eruption (14m knockback, -7 armor, buff purge, and a 12m molten caldera slowing "
                            + "hostiles by 70% and burning underwater).";
            case RADIAL_BURST ->
                    "Wall-penetrating 12-block concussive shockwave of subterranean magma geysers, blackstone pillars, "
                            + "and light-devouring gravity rings (zero light blocks); knocks hostiles 12 blocks back "
                            + "flat, shatters 6 armor points, disables shields 6.0s, deals massive Tier 5 Cataclysm "
                            + "damage, and leaves bubbling molten obsidian cracks.";
            case TOUCH_STRIKE ->
                    "Wall-cleaving two-handed volcanic blade forged of collapsed event horizons and bubbling nether "
                            + "magma; bypasses 60% armor, shatters 6 armor points, disables shields 6.0s, and opens a "
                            + "10-block fissure of light-devouring void fire dragging pursuers inward (2.0 hearts/sec).";
            case AURA ->
                    "Envelopes the caster in light-devouring void darkness, umbral shadows, and molten magma plates "
                            + "(mobile or Sneak-anchored); snuffs torches within 6m, grants +12 Armor, +6 Toughness, "
                            + "fire/lava/wither/blindness immunity, -85% mob detection, 5m arrow/trident cinder-slag "
                            + "vaporization, and a 6m gravity-well aura pulling hostiles inward (2.0 hearts/sec true fire).";
            case GLYPH ->
                    "Void-magma mandala glyph on any floor, wall, or ceiling (disarmable, Single or Repeating); hostile "
                            + "approach within 5m detonates an 8m volcanic caldera vacuuming mobs inward, launching "
                            + "them 14m straight up (-6 armor, buff purge, and 6.0s Wither III + Stygian Conflagration) "
                            + "before violently slamming them into the ground for catastrophic impact damage.";
            case CONE ->
                    "Wall-penetrating cone of pressurized molten basalt, light-devouring darkfire, and obsidian ash; "
                            + "shatters 6 armor points, vaporizes incoming projectiles, incinerates wooden blocks, boils "
                            + "water into steam, and inflicts Tectonic Vertigo Panic (30.0s AI disruption, -60% movement "
                            + "speed, and frantic blind terror).";
            case SKYFALL ->
                    "24.0s World-Breaker Skyfall across a 16-block+ zone dropping 8 3D molten obsidian & blackstone "
                            + "meteorites that permanently materialize upon completion; crushes hostiles, knocks them "
                            + "flat, shatters 7 armor points, and leaves a 12.0s Stygian Magma Slag crater (-70% speed, "
                            + "Wither III, and underwater true fire burn).";
            case SUMMON_AGENT ->
                    "Summons a 110 HP Stygian Magma-Void Serpent (60s refreshing lifecycle) — wall-phasing abyssal "
                            + "leviathan serpent burrowing through walls to coil & crush hostiles (60% armor bypass, "
                            + "-4 armor/hit, and pulling distant enemies directly into its molten jaws).";
            case SUMMON_SENTRY ->
                    "Summons a 120 HP Volcanic Eruption Spire (60s refreshing lifecycle) — bombards foes up to 20m "
                            + "with wall-penetrating railgun magma spikes (-3 armor/hit) while pulsing a 16m "
                            + "Gravitational Caldera Field (inward pull, -1 armor/sec, and 1.5 hearts/sec burn).";
            case SUMMON_DECOY ->
                    "Summons a Molten Core Beacon (16s duration) — taunts hostiles across 16m, reflects 45% melee "
                            + "damage, and detonates in a 14m burst of stygian hellfire & dark matter (massive true "
                            + "damage, -6 armor, and singularity pull).";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Gravitational Void-Warp Collapse";
            case TRANSMUTE, BREAK -> "Tectonic Subduction & Magma Vitrification";
            case AFFLICTION -> "Abyssal World-Breaker Singularity Hex";
            case WARD -> "Obsidian Event-Horizon Bastion";
            default -> "World-Breaker Cataclysm " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK ->
                    "Instantly and silently teleports up to 22 blocks through solid walls via a gravitational wormhole; "
                            + "departure collapses into a 6m pulling micro-singularity (crushing arrows, pulling foes "
                            + "inward, and stunning 2.5s); arrival grants 8.0s fire & fall immunity and 100% aggro shed.";
            case TRANSMUTE, BREAK ->
                    "5.0s continuous channel across 12 blocks; transmutes Stone, Deepslate, and Cobblestone into "
                            + "Obsidian, Crying Obsidian, or Blackstone; smelts raw metal/mineral ores with +35% bonus "
                            + "yield & nuggets; vitrifies Sand/Red Sand into Glass; and melts damaged equipment, "
                            + "unwanted tools, and metallic items into raw Void Slag and base ingots.";
            case AFFLICTION ->
                    "Milk-immune dimensional collapse curse (30.0s boss cap + 10.0s immunity); crushes with inward "
                            + "gravity and underwater-burning void-nether fire, -70% move speed, blocks jumping, -50% "
                            + "Armor Toughness, 50% weapon recoil, and +150% 32m Abyssal World-Breaker Beacon spawn lure.";
            default ->
                    "Infuses " + effect.getDisplayName() + " with Tier 5 Eschaton Cataclysm World-Breaker Materia.";
        };
    }

    // =========================================================================
    // PART 1: FLOW SYNERGY — SINGULARITY ORBIT FLOW
    // =========================================================================

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        if (flow != SpellFlowModifier.ORBIT_SATELLITE) {
            return;
        }
        LivingEntity owner = projectile.getOwner() instanceof LivingEntity l ? l : null;
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(
                level, owner, projectile.getBoundingBox().inflate(2.6D), null, 0L
        );

        // 4 persistent collapsed void-magma cores circling the spell carrier (#41304E / #211A21 / #B83C08)
        double baseAng = projectile.tickCount * 0.42D;
        for (int i = 0; i < 4; i++) {
            double ang = baseAng + (Math.PI * 0.5D * i);
            int col = CATACLYSM_PALETTE[i % CATACLYSM_PALETTE.length];
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), col, 12, 0.30F, 0.03F),
                    projectile.getX() + Math.cos(ang) * 0.48D,
                    projectile.getY() + Math.sin(ang * 0.5D) * 0.24D,
                    projectile.getZ() + Math.sin(ang) * 0.48D,
                    1, 0.02D, 0.02D, 0.02D, 0.005D
            );
        }

        // Seeking railgun obsidian spikes: -40% target Armor Toughness & true fire damage!
        if (projectile.tickCount % 10 == 0) {
            long now = level.getGameTime();
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, projectile.getBoundingBox().inflate(6.0D),
                    e -> e.isAlive() && e != owner && e instanceof Monster
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, true)))) {
                SpellWorldVfxManager.broadcastChainArc(
                        level, projectile.position(), foe.getEyePosition().subtract(0.0D, 0.2D, 0.0D),
                        EssenceType.CATACLYSM, EssenceType.NETHER, 8
                );
                SpellCombatReactionEngine.applyPercentageArmorShred(foe, level, 0.0D, 0.40D, 120L);
                foe.igniteForSeconds(4);
                EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 80L);
                SpellCombatReactionEngine.hurtWithCataclysmMateriaAdvantage(level, foe, owner, 3.60F, false, false, true);
                break;
            }
        }
    }

    @Override
    public void onChainLeapStep(
            ServerLevel level,
            LivingEntity caster,
            LivingEntity fromTarget,
            LivingEntity toTarget,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        Vec3 from = fromTarget.getEyePosition().subtract(0.0D, 0.2D, 0.0D);
        Vec3 to = toTarget.getEyePosition().subtract(0.0D, 0.2D, 0.0D);
        SpellWorldVfxManager.broadcastChainArc(level, from, to, EssenceType.CATACLYSM, EssenceType.VOID, spell.getEffectiveTier());
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(level, caster, new AABB(from, to).inflate(2.2D), null, 0L);
        if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(toTarget, caster, spell.hasWardFriendlyFireSafeguard())) {
            SpellCombatReactionEngine.applyPercentageArmorShred(toTarget, level, 0.0D, 0.40D, 120L);
            toTarget.igniteForSeconds(4);
            EntityHelper.getPersistentData(toTarget).putLong(
                    SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, level.getGameTime() + 80L
            );
            SpellCombatReactionEngine.hurtWithCataclysmMateriaAdvantage(
                    level, toTarget, caster, 3.80F + spell.getEffectiveTier() * 0.55F, false, false, true
            );
        }
    }

    // =========================================================================
    // PART 2: DELIVERY FORM MUTATIONS (ALL 9 FORMS)
    // =========================================================================

    @Override
    public void onProjectileSpawnedFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Ultra-high-velocity zero-gravity wall-penetrating railgun slug!
        projectile.setNoGravity(true);
        Vec3 vel = projectile.getDeltaMovement();
        if (vel.lengthSqr() > 1.0E-4D) {
            projectile.setDeltaMovement(vel.normalize().scale(Math.max(2.35D, vel.length() * 1.45D)));
            projectile.hasImpulse = true;
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.85F, 0.72F);
        level.playSound(null, caster.blockPosition(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 0.70F, 1.55F);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        int col = CATACLYSM_PALETTE[(projectile.tickCount / 2) % CATACLYSM_PALETTE.length];
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), col, 12, 0.34F, 0.03F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.02D
        );
        level.sendParticles(ParticleTypes.FLAME, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.05D, 0.05D, 0.05D, 0.02D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.05D, 0.05D, 0.05D, 0.02D);
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
        // Section 6.55 Catastrophic Hellfire Railgun Slug:
        // Pierces infinitely through solid terrain and up to 10 entities while bypassing 60% of armor rating;
        // EACH entity penetrated triggers a secondary volcanic magma eruption that knocks mobs flat,
        // shreds 35% armor toughness, and inflicts unquenchable stygian burns!
        int tier = spell.getEffectiveTier();
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        long now = level.getGameTime();
        Vec3 pos = hitEntity.position();

        if (hitEntity instanceof LivingEntity direct && direct.isAlive()
                && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(direct, caster, safeguard))
                && !slot.hasHarmVerb()) {
            float slugDmg = (5.8F + tier * 1.45F) * Math.max(0.5F, spell.flawProfile().getPotencyMultiplier());
            SpellCombatReactionEngine.hurtWithCataclysmMateriaAdvantage(level, direct, caster, slugDmg, false, true, false);
        }

        SpellWorldVfxManager.broadcastRadialNova(level, pos.add(0.0D, 0.20D, 0.0D), 3.8D, EssenceType.CATACLYSM, EssenceType.NETHER, tier);
        AABB erupt = new AABB(pos.x - 3.8D, pos.y - 1.5D, pos.z - 3.8D, pos.x + 3.8D, pos.y + 3.2D, pos.z + 3.8D);
        float eruptDmg = (5.2F + tier * 1.30F) * Math.max(0.5F, spell.flawProfile().getPotencyMultiplier());

        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, erupt, LivingEntity::isAlive)) {
            if (foe == caster || ApexEschatonSingularityHelper.isFriendly(foe, caster)) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, safeguard)) continue;

            SpellCombatReactionEngine.applyPercentageArmorShred(foe, level, 0.0D, 0.35D, 160L);
            foe.setDeltaMovement(foe.getDeltaMovement().x * 0.25D, -0.52D, foe.getDeltaMovement().z * 0.25D);
            foe.hasImpulse = true;
            foe.igniteForSeconds(7);
            EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 140L);
            if (foe != hitEntity) {
                SpellCombatReactionEngine.hurtWithCataclysmMateriaAdvantage(level, foe, caster, eruptDmg, false, true, false);
            }
        }
        emitCataclysmBurstParticles(level, pos.add(0.0D, 0.6D, 0.0D), 18, 0.75D);
        level.playSound(null, BlockPos.containing(pos), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.65F, 0.85F);
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
        // Section 6.55 Tectonic Magma Fissure Ray & Seismic Cataclysm:
        // Melts 2 armor/sec, steadily pulls distant enemies into its searing core, & disintegrates incoming arrows;
        // 5.0s (100t) continuous channeling (tracked on the CASTER!) triggers Cataclysmic Seismic Eruption:
        // 14m knockback, -7 armor, purges all positive mob buffs, & 12m molten caldera (-70% speed & burns underwater)!
        Vec3 eye = caster.getEyePosition();
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        long now = level.getGameTime();
        int tier = spell.getEffectiveTier();
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        boolean continuousPulse = heldTicks > 0 || apparatusPotency <= 0.45F;
        float tickFrac = continuousPulse ? 0.25F : 1.0F;

        AABB rayBox = new AABB(eye, eye.add(dir.scale(maxReach))).inflate(2.5D);
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(level, caster, rayBox, null, 0L);

        // Steadily pull distant enemies within 4.5m of the ray into its searing core!
        AABB pullBox = new AABB(eye, eye.add(dir.scale(maxReach))).inflate(4.5D);
        for (LivingEntity near : level.getEntitiesOfClass(LivingEntity.class, pullBox, e -> e.isAlive() && e != caster)) {
            if (ApexEschatonSingularityHelper.isFriendly(near, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(near, caster, safeguard)) continue;
            if (SpellCombatReactionEngine.isBossEntity(level, near)) continue;
            Vec3 rel = near.getEyePosition().subtract(eye);
            double projDist = Math.clamp(rel.dot(dir), 1.5D, maxReach);
            Vec3 corePt = eye.add(dir.scale(projDist));
            Vec3 toCore = corePt.subtract(near.position());
            double hDistSq = toCore.x * toCore.x + toCore.z * toCore.z;
            if (hDistSq > 0.20D && hDistSq <= 20.25D) {
                Vec3 pull = new Vec3(toCore.x, 0.0D, toCore.z).normalize().scale(0.32D * Math.max(0.45F, tickFrac));
                near.setDeltaMovement(near.getDeltaMovement().add(pull.x, 0.04D, pull.z));
                near.hasImpulse = true;
            }
        }

        CompoundTag casterData = EntityHelper.getPersistentData(caster);
        long lastCast = casterData.getLong("CataclysmCasterBeamLastTick").orElse(-100L);
        int channelTicks;
        if (lastCast == now) {
            channelTicks = casterData.getInt("CataclysmCasterBeamTicks").orElse(0);
        } else {
            int step = heldTicks > 0
                    ? ((lastCast >= 0L && now - lastCast <= 28L) ? (int) Math.max(1L, Math.min(20L, now - lastCast)) : Math.min(heldTicks, 4))
                    : 0;
            channelTicks = heldTicks > 0
                    ? ((now - lastCast <= 28L) ? casterData.getInt("CataclysmCasterBeamTicks").orElse(0) + step : step)
                    : 0;
            casterData.putLong("CataclysmCasterBeamLastTick", now);
            casterData.putInt("CataclysmCasterBeamTicks", channelTicks);
        }

        LivingEntity primaryFoe = null;
        for (LivingEntity hit : hitEntities) {
            if (!hit.isAlive() || hit == caster || ApexEschatonSingularityHelper.isFriendly(hit, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, safeguard)) continue;
            if (primaryFoe == null) primaryFoe = hit;

            CompoundTag hData = EntityHelper.getPersistentData(hit);
            if (hData.getLong("CataclysmBeamLastTick").orElse(-100L) == now) continue;
            hData.putLong("CataclysmBeamLastTick", now);
            hData.putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 60L);
            hit.igniteForSeconds(3);

            long foeLast = hData.getLong("CataclysmFoeLastTick").orElse(-100L);
            int foeStep = heldTicks > 0
                    ? ((foeLast >= 0L && now - foeLast <= 28L) ? (int) Math.max(1L, Math.min(20L, now - foeLast)) : Math.min(heldTicks, 4))
                    : 0;
            int foeTicks = heldTicks > 0
                    ? ((now - foeLast <= 28L) ? hData.getInt("CataclysmFoeTicks").orElse(0) + foeStep : foeStep)
                    : 0;
            hData.putLong("CataclysmFoeLastTick", now);
            hData.putInt("CataclysmFoeTicks", foeTicks);

            int meltedArmor = Math.min(10, Math.max(2, (foeTicks / 20) * 2));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, meltedArmor, 0.0D, 100L);

            if (!slot.hasHarmVerb()) {
                float beamDmg = (4.8F + tier * 1.20F) * tickFrac;
                SpellCombatReactionEngine.hurtWithCataclysmMateriaAdvantage(level, hit, caster, beamDmg, false, true, false);
            }
        }

        // 5.0 continuous seconds (100 ticks) -> Cataclysmic Seismic Eruption!
        if (heldTicks >= 100 && channelTicks >= 100 && (primaryFoe != null || blockHit != null)) {
            casterData.putInt("CataclysmCasterBeamTicks", 0);
            Vec3 epicenter = primaryFoe != null
                    ? primaryFoe.position()
                    : (blockHit != null ? blockHit.getLocation() : eye.add(dir.scale(Math.min(12.0D, maxReach))));

            SpellWorldVfxManager.broadcastRadialNova(level, epicenter.add(0.0D, 0.3D, 0.0D), 12.0D, EssenceType.CATACLYSM, EssenceType.NETHER, tier);
            SpellWorldVfxManager.broadcastArcaneSeal(level, epicenter.add(0.0D, 0.04D, 0.0D), 12.0D, EssenceType.CATACLYSM, EssenceType.VOID, 140, tier);

            AABB eruptBox = new AABB(epicenter.x - 12.0D, epicenter.y - 3.0D, epicenter.z - 12.0D,
                    epicenter.x + 12.0D, epicenter.y + 6.0D, epicenter.z + 12.0D);
            float eruptDmg = 16.5F + tier * 2.90F;

            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, eruptBox, LivingEntity::isAlive)) {
                if (foe == caster || ApexEschatonSingularityHelper.isFriendly(foe, caster)) continue;
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, safeguard)) continue;

                ApexEschatonSingularityHelper.purgeBeneficialBuffs(foe);
                SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 7.0D, 0.0D, 220L);
                if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                    Vec3 diff = foe.position().subtract(caster.position());
                    Vec3 push = (diff.lengthSqr() > 1.0E-4D ? new Vec3(diff.x, 0.0D, diff.z).normalize() : new Vec3(dir.x, 0.0D, dir.z)).scale(2.85D);
                    SpellExecutionEngine.applySyncedMotion(foe, new Vec3(push.x, 0.50D, push.z)); // 14m knockback!
                }
                foe.igniteForSeconds(6);
                EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 120L);
                SpellCombatReactionEngine.hurtWithCataclysmMateriaAdvantage(level, foe, caster, eruptDmg, false, true, false);
            }

            // 12-block Molten Caldera: slows hostiles by exact 70% & burns underwater!
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, epicenter, 12.0D, 140, now, spell, slot, EssenceType.CATACLYSM, apparatusPotency,
                    (srv, zone, elapsed) -> {
                        if (elapsed > zone.maxDurationTicks) return false;
                        if (elapsed % 20 == 0) {
                            LivingEntity owner = zone.resolveCaster(srv);
                            long zNow = srv.getGameTime();
                            double rSq = 12.5D * 12.5D;
                            AABB cal = new AABB(
                                    zone.center.x - 12.0D, zone.center.y - 2.5D, zone.center.z - 12.0D,
                                    zone.center.x + 12.0D, zone.center.y + 4.0D, zone.center.z + 12.0D
                            );
                            for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, cal, e -> e.isAlive() && e != owner)) {
                                if (owner != null && ApexEschatonSingularityHelper.isFriendly(foe, owner)) continue;
                                if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                                double dx = foe.getX() - zone.center.x;
                                double dz = foe.getZ() - zone.center.z;
                                if (dx * dx + dz * dz > rSq) continue;

                                SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, srv, 0.70D, 35L); // Exact -70% slow!
                                EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, zNow + 55L);
                                foe.igniteForSeconds(3);
                                SpellCombatReactionEngine.hurtWithCataclysmMateriaAdvantage(srv, foe, owner, 4.0F, false, false, true);
                            }
                            emitCataclysmBurstParticles(srv, zone.center.add(0.0D, 0.3D, 0.0D), 22, 4.0D);
                        }
                        return true;
                    }
            ));

            emitCataclysmBurstParticles(level, epicenter.add(0.0D, 0.8D, 0.0D), 52, 4.2D);
            level.playSound(null, BlockPos.containing(epicenter), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.15F, 0.70F);
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
        // Section 6.55 World-Breaker Seismic Nova:
        // Wall-penetrating 12-block concussive shockwave (ZERO physical light blocks!),
        // knocks all hostiles 12 blocks back flat, shatters 6 armor points, disables shields 6.0s (120t),
        // deals massive Tier 5 Cataclysm Materia damage, & leaves bubbling molten obsidian cracks!
        int tier = spell.getEffectiveTier();
        double r = Math.max(12.0D, radius);
        double rSq = (r + 0.5D) * (r + 0.5D);
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.CATACLYSM, EssenceType.NETHER, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, center.add(0.0D, 0.04D, 0.0D), r, EssenceType.CATACLYSM, EssenceType.VOID, 120, tier);

        float novaDmg = (8.8F + tier * 2.00F) * Math.max(0.5F, apparatusPotency);
        AABB box = new AABB(center.x - r, center.y - 3.0D, center.z - r, center.x + r, center.y + 5.5D, center.z + r);

        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != caster)) {
            if (ApexEschatonSingularityHelper.isFriendly(foe, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, safeguard)) continue;
            double dx = foe.getX() - center.x;
            double dz = foe.getZ() - center.z;
            if (dx * dx + dz * dz > rSq) continue;

            if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                Vec3 horiz = new Vec3(dx, 0.0D, dz);
                Vec3 away = (horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : new Vec3(direction.x, 0.0D, direction.z)).scale(2.45D);
                SpellExecutionEngine.applySyncedMotion(foe, new Vec3(away.x, 0.34D, away.z)); // 12m flat knockback!
            }
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 6.0D, 0.0D, 180L);
            ApexEschatonSingularityHelper.disableShield(foe, 120); // 6.0s shield disable!
            foe.igniteForSeconds(5);
            EntityHelper.getPersistentData(foe).putLong(
                    SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, level.getGameTime() + 100L
            );

            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithCataclysmMateriaAdvantage(level, foe, caster, novaDmg, false, true, false);
            }
        }

        // Bubbling molten obsidian cracks that continuously damage enemies!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, center, r, 120, level.getGameTime(), spell, slot, EssenceType.CATACLYSM, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        long zNow = srv.getGameTime();
                        double zRSq = (zone.radius + 0.5D) * (zone.radius + 0.5D);
                        AABB cracks = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 2.8D, zone.center.z + zone.radius
                        );
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, cracks, e -> e.isAlive() && e != owner)) {
                            if (owner != null && ApexEschatonSingularityHelper.isFriendly(foe, owner)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            double dx = foe.getX() - zone.center.x;
                            double dz = foe.getZ() - zone.center.z;
                            if (dx * dx + dz * dz > zRSq) continue;

                            foe.igniteForSeconds(4);
                            EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, zNow + 60L);
                            SpellCombatReactionEngine.hurtWithCataclysmMateriaAdvantage(srv, foe, owner, 3.8F, false, false, true);
                        }
                        emitCataclysmBurstParticles(srv, zone.center.add(0.0D, 0.2D, 0.0D), 18, zone.radius * 0.35D);
                    }
                    return true;
                }
        ));

        emitCataclysmBurstParticles(level, center.add(0.0D, 0.75D, 0.0D), 54, r * 0.40D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.10F, 0.72F);
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
        // Section 6.55 Tectonic Calamity Sunder:
        // Wall-cleaving two-handed volcanic blade; bypasses 60% armor, shatters 6 armor points,
        // disables shields 6.0s (120t), and opens a 10m fissure of light-devouring void fire
        // dragging pursuing hostiles inward & burning them for 2.0 hearts/sec (4.0 HP/s)!
        int tier = spell.getEffectiveTier();
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D), normDir, 3.8D,
                EssenceType.CATACLYSM, EssenceType.NETHER, tier
        );

        float sunderDmg = Math.max(6.2F + tier * 1.50F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.60F)
                * Math.max(0.5F, apparatusPotency) * spell.flawProfile().getPotencyMultiplier();

        Vec3 fissureOrigin = hitEntities.isEmpty()
                ? caster.position().add(normDir.scale(2.6D))
                : hitEntities.getFirst().position();

        for (int idx = 0; idx < hitEntities.size(); idx++) {
            LivingEntity hit = hitEntities.get(idx);
            if (!hit.isAlive() || hit == caster || ApexEschatonSingularityHelper.isFriendly(hit, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, safeguard)) continue;

            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 6.0D, 0.0D, 180L);
            ApexEschatonSingularityHelper.disableShield(hit, 120); // 6.0s shield disable!
            hit.igniteForSeconds(5);
            EntityHelper.getPersistentData(hit).putLong(
                    SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, level.getGameTime() + 100L
            );

            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithCataclysmMateriaAdvantage(
                        level, hit, caster, sunderDmg * (idx == 0 ? 1.0F : 0.60F), true, true, false
                );
            }
            emitCataclysmBurstParticles(level, hit.position().add(0.0D, hit.getBbHeight() * 0.5D, 0.0D), 16, 0.55D);
        }

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, fissureOrigin.add(0.0D, 0.04D, 0.0D), 10.0D, EssenceType.CATACLYSM, EssenceType.VOID, 120, tier
        );
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, fissureOrigin, 10.0D, 120, level.getGameTime(), spell, slot, EssenceType.CATACLYSM, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        long zNow = srv.getGameTime();
                        AABB fis = new AABB(
                                zone.center.x - 10.0D, zone.center.y - 2.0D, zone.center.z - 10.0D,
                                zone.center.x + 10.0D, zone.center.y + 3.5D, zone.center.z + 10.0D
                        );
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, fis, e -> e.isAlive() && e != owner)) {
                            if (owner != null && ApexEschatonSingularityHelper.isFriendly(foe, owner)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            if (!SpellCombatReactionEngine.isBossEntity(srv, foe)) {
                                Vec3 diff = zone.center.subtract(foe.position());
                                if (diff.horizontalDistanceSqr() > 0.25D) {
                                    Vec3 pull = new Vec3(diff.x, 0.0D, diff.z).normalize().scale(0.38D);
                                    foe.setDeltaMovement(foe.getDeltaMovement().add(pull.x, 0.04D, pull.z));
                                    foe.hasImpulse = true;
                                }
                            }
                            foe.igniteForSeconds(4);
                            EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, zNow + 60L);
                            SpellCombatReactionEngine.hurtWithCataclysmMateriaAdvantage(srv, foe, owner, 4.0F, false, false, true);
                        }
                        emitCataclysmBurstParticles(srv, zone.center.add(0.0D, 0.3D, 0.0D), 16, 3.0D);
                    }
                    return true;
                }
        ));
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
        // Section 6.55 Mantle of the World-Breaker:
        // Snuffs torches within 6m, +12 Armor, +6 Toughness, complete fire/lava/wither/blindness immunity,
        // -85% hostile mob detection, 5m 100% arrow/trident molten cinder slag vaporization,
        // and continuous 6m gravity-well aura pulling hostiles inward & dealing 2.0 hearts/s (4.0 HP/s) true fire damage!
        int tier = spell.getEffectiveTier();
        int dur = 300 + tier * 45;
        long now = level.getGameTime();
        double auraRad = Math.max(6.0D, radius);

        snuffTorchesWithinSixBlocks(level, caster.blockPosition());

        if (caster.isShiftKeyDown()) {
            Vec3 anchor = caster.position();
            SpellWorldVfxManager.broadcastWardShell(level, anchor.add(0.0D, 1.1D, 0.0D), auraRad, EssenceType.CATACLYSM, EssenceType.NETHER, dur, tier);
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, anchor, auraRad, dur, now, spell, slot, EssenceType.CATACLYSM, apparatusPotency,
                    (srv, zone, elapsed) -> {
                        if (elapsed > zone.maxDurationTicks) return false;
                        LivingEntity owner = zone.resolveCaster(srv);
                        long zNow = srv.getGameTime();
                        BlockPos cPos = BlockPos.containing(zone.center);
                        if (elapsed % 10 == 0) {
                            snuffTorchesWithinSixBlocks(srv, cPos);
                        }
                        // Vaporize physical arrows & tridents within 5 blocks of the anchored mantle
                        for (AbstractArrow arrow : srv.getEntitiesOfClass(AbstractArrow.class, new AABB(zone.center, zone.center).inflate(5.0D))) {
                            if (owner != null && (arrow.getOwner() == owner
                                    || (arrow.getOwner() instanceof LivingEntity ao && ApexEschatonSingularityHelper.isFriendly(ao, owner)))) {
                                continue;
                            }
                            srv.sendParticles(ParticleTypes.LAVA, arrow.getX(), arrow.getY(), arrow.getZ(), 6, 0.14D, 0.14D, 0.14D, 0.03D);
                            srv.sendParticles(ParticleTypes.ASH, arrow.getX(), arrow.getY(), arrow.getZ(), 8, 0.16D, 0.16D, 0.16D, 0.02D);
                            arrow.discard();
                        }

                        AABB box = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 4.0D, zone.center.z + zone.radius
                        );
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                            if (ent == owner || (owner != null && ApexEschatonSingularityHelper.isFriendly(ent, owner))) {
                                EntityHelper.getPersistentData(ent).putLong(SpellLingeringZoneManager.TAG_CATACLYSM_SHROUD_UNTIL, zNow + 16L);
                                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ent, srv, 12.0D, 6.0D, 0.0D, 0.0D, 25L);
                                ent.clearFire();
                                ent.removeEffect(MobEffects.WITHER);
                                ent.removeEffect(MobEffects.BLINDNESS);
                                ent.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 30, 0, true, false));
                            } else if (elapsed % 20 == 0
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                            ent, owner, zone.spell.hasWardFriendlyFireSafeguard()))
                                    && (ent instanceof Monster || (owner != null && ent == owner.getLastHurtMob()))) {
                                if (!SpellCombatReactionEngine.isBossEntity(srv, ent)) {
                                    Vec3 diff = zone.center.subtract(ent.position());
                                    if (diff.horizontalDistanceSqr() > 0.25D) {
                                        Vec3 pull = new Vec3(diff.x, 0.0D, diff.z).normalize().scale(0.38D);
                                        ent.setDeltaMovement(ent.getDeltaMovement().add(pull.x, 0.06D, pull.z));
                                        ent.hasImpulse = true;
                                    }
                                }
                                EntityHelper.getPersistentData(ent).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, zNow + 60L);
                                ent.igniteForSeconds(3);
                                SpellCombatReactionEngine.hurtWithCataclysmMateriaAdvantage(srv, ent, owner, 4.0F, false, false, true);
                            }
                        }
                        return true;
                    }
            ));
        } else {
            EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_CATACLYSM_SHROUD_UNTIL, now + dur);
            SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 12.0D, 6.0D, 0.0D, 0.0D, dur);
            caster.clearFire();
            caster.removeEffect(MobEffects.WITHER);
            caster.removeEffect(MobEffects.BLINDNESS);
            caster.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, dur, 0, false, true));
            SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.88F, null);
        }

        emitCataclysmBurstParticles(level, caster.position().add(0.0D, 1.0D, 0.0D), 28, 0.60D);
        level.playSound(null, caster.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.85F, 0.68F);
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
        // Section 6.55 Abyssal Calamity Caldera Sigil:
        // Disarmable Single/Repeating void-magma mandala glyph on any floor, wall, or ceiling;
        // Hostile approach within 5m detonates an 8m volcanic caldera vacuuming all mobs into its molten epicenter,
        // launching them 14 blocks straight into the air, shattering 6 armor points, stripping positive mob buffs,
        // inflicting Wither III & Stygian Conflagration for 6.0s (120t), BEFORE violently slamming them into the ground
        // for catastrophic impact damage!
        final int tier = spell.getEffectiveTier();
        final int maxWaitTicks = tier >= 5 ? 72_000_000 : 12_000;
        String subMode = slot.primaryEffect().subMode();
        final boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        final int[] cooldown = {0};

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, Math.max(5.0D, radius), maxWaitTicks, level.getGameTime(), spell, slot, EssenceType.CATACLYSM, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 12 == 0) {
                        int col = CATACLYSM_PALETTE[(elapsed / 12) % CATACLYSM_PALETTE.length];
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), col, 20, 0.36F, 0.03F),
                                zone.center.x, zone.center.y + 0.14D, zone.center.z, 6, 1.8D, 0.08D, 1.8D, 0.01D
                        );
                    }
                    if (elapsed < 16) return true;
                    if (cooldown[0] > 0) {
                        cooldown[0]--;
                        return true;
                    }

                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB trig = new AABB(
                            zone.center.x - 5.0D, zone.center.y - 2.0D, zone.center.z - 5.0D,
                            zone.center.x + 5.0D, zone.center.y + 3.5D, zone.center.z + 5.0D
                    );
                    if (srv.getEntitiesOfClass(LivingEntity.class, trig, e -> e.isAlive()
                            && e != owner
                            && (owner == null || !ApexEschatonSingularityHelper.isFriendly(e, owner))
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                    e, owner, zone.spell.hasWardFriendlyFireSafeguard()))).isEmpty()) {
                        return true;
                    }

                    long now = srv.getGameTime();
                    SpellWorldVfxManager.broadcastVacuumSphere(
                            srv, zone.center.add(0.0D, 0.6D, 0.0D), 8.0D, true, EssenceType.CATACLYSM, EssenceType.VOID, 36, tier
                    );
                    SpellWorldVfxManager.broadcastGeyserPlume(
                            srv, zone.center, 2.4D, 14.0D, EssenceType.CATACLYSM, EssenceType.NETHER, 36, tier
                    );
                    SpellWorldVfxManager.broadcastArcaneSeal(
                            srv, zone.center.add(0.0D, 0.04D, 0.0D), 8.0D, EssenceType.CATACLYSM, EssenceType.NETHER, 120, tier
                    );

                    AABB caldera = new AABB(
                            zone.center.x - 8.0D, zone.center.y - 2.5D, zone.center.z - 8.0D,
                            zone.center.x + 8.0D, zone.center.y + 5.0D, zone.center.z + 8.0D
                    );
                    List<UUID> launchedTargets = new ArrayList<>();

                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, caldera, e -> e.isAlive() && e != owner)) {
                        if (owner != null && ApexEschatonSingularityHelper.isFriendly(foe, owner)) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) {
                            continue;
                        }

                        ApexEschatonSingularityHelper.purgeBeneficialBuffs(foe);
                        SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 6.0D, 0.0D, 200L);

                        Vec3 in = zone.center.subtract(foe.position()).scale(0.38D);
                        SpellExecutionEngine.applySyncedMotion(foe, new Vec3(in.x, 1.66D, in.z)); // 14m straight upward caldera launch!
                        launchedTargets.add(foe.getUUID());

                        foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 2)); // Wither III for 6.0s!
                        foe.igniteForSeconds(6);
                        CompoundTag fData = EntityHelper.getPersistentData(foe);
                        fData.putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 120L);
                        fData.putLong(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_UNTIL, now + 120L); // Stygian Conflagration 6.0s!
                        fData.putFloat(SpellLingeringZoneManager.TAG_OBLIVION_CONFLAGRATION_MAX_HP, foe.getHealth());

                        SpellExecutionEngine.deliverSubSpellOnEntity(
                                srv, owner, foe, new Vec3(0.0D, 1.0D, 0.0D),
                                zone.spell, zone.slot, EssenceType.CATACLYSM, zone.apparatusPotency
                        );
                    }

                    // Apex Slam Sequence: violently slam launched victims back into the ground for catastrophic impact damage!
                    if (!launchedTargets.isEmpty()) {
                        float slamDmg = (12.5F + tier * 2.40F) * Math.max(0.5F, zone.apparatusPotency);
                        SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                                owner, zone.center, 8.0D, 34, now, zone.spell, zone.slot, EssenceType.CATACLYSM, zone.apparatusPotency,
                                (sLvl, sZone, sEl) -> {
                                    if (sEl == 24) {
                                        for (UUID id : launchedTargets) {
                                            Entity ent = sLvl.getEntity(id);
                                            if (ent instanceof LivingEntity victim && victim.isAlive()) {
                                                Vec3 toCenter = sZone.center.subtract(victim.position()).scale(0.22D);
                                                SpellExecutionEngine.applySyncedMotion(victim, new Vec3(toCenter.x, -1.95D, toCenter.z));
                                            }
                                        }
                                    } else if (sEl == 32) {
                                        LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                        SpellWorldVfxManager.broadcastRadialNova(
                                                sLvl, sZone.center.add(0.0D, 0.2D, 0.0D), 8.0D, EssenceType.CATACLYSM, EssenceType.NETHER, tier
                                        );
                                        for (UUID id : launchedTargets) {
                                            Entity ent = sLvl.getEntity(id);
                                            if (ent instanceof LivingEntity victim && victim.isAlive()) {
                                                SpellCombatReactionEngine.hurtWithCataclysmMateriaAdvantage(
                                                        sLvl, victim, sOwner, slamDmg, false, true, false
                                                );
                                                emitCataclysmBurstParticles(sLvl, victim.position().add(0.0D, 0.3D, 0.0D), 16, 0.65D);
                                            }
                                        }
                                        sLvl.playSound(null, BlockPos.containing(sZone.center), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 0.72F);
                                        return false;
                                    }
                                    return sEl < 34;
                                }
                        ));
                    }

                    emitCataclysmBurstParticles(srv, zone.center.add(0.0D, 0.8D, 0.0D), 36, 2.5D);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.1F, 0.68F);

                    if (repeating) {
                        cooldown[0] = 140;
                        return true;
                    }
                    return false;
                }
        ).markDisarmableGlyph().withOnDisarm((srv, z) ->
                emitCataclysmBurstParticles(srv, z.center.add(0.0D, 0.3D, 0.0D), 14, 0.45D)));

        level.playSound(null, BlockPos.containing(glyphCenter), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.75F, 0.65F);
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
        // Section 6.55 Cataclysmic Magma-Ash Deluge:
        // Wall-penetrating cone; shatters 6 armor points, vaporizes all incoming enemy projectiles,
        // incinerates wooden blocks, boils water into steam, and inflicts Tectonic Vertigo Panic
        // (disrupting mob AI pathfinding AND slowing movement by 60% for 30.0s / 600t + frantic blind terror)!
        int tier = spell.getEffectiveTier();
        double reach = Math.max(12.0D, coneRange);
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 origin = caster.getEyePosition();

        SpellWorldVfxManager.broadcastVortexCone(
                level, origin.subtract(0.0D, 0.2D, 0.0D), dir, reach, EssenceType.CATACLYSM, EssenceType.NETHER, tier
        );

        AABB coneBox = caster.getBoundingBox().expandTowards(dir.scale(reach)).inflate(reach * 0.55D);
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(level, caster, coneBox, null, 0L);

        // Incinerate wooden blocks & boil water into steam along the cone trajectory
        for (int step = 2; step <= (int) Math.min(14.0D, reach); step += 3) {
            BlockPos stepCenter = BlockPos.containing(origin.add(dir.scale(step)));
            int rad = Math.max(1, step / 4);
            for (BlockPos p : BlockPos.betweenClosed(stepCenter.offset(-rad, -2, -rad), stepCenter.offset(rad, 2, rad))) {
                BlockState bs = level.getBlockState(p);
                if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
                if (bs.is(Blocks.WATER) || bs.is(Blocks.BUBBLE_COLUMN)) {
                    level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
                    level.sendParticles(ParticleTypes.CLOUD, p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D, 5, 0.25D, 0.25D, 0.25D, 0.04D);
                } else if ((bs.is(BlockTags.LOGS) || bs.is(BlockTags.PLANKS) || bs.is(BlockTags.WOODEN_FENCES) || bs.is(BlockTags.LEAVES))
                        && level.random.nextFloat() < 0.42F) {
                    level.setBlockAndUpdate(p, Blocks.FIRE.defaultBlockState());
                }
            }
        }

        long panicUntil = level.getGameTime() + 600L; // Full 30.0s Tectonic Vertigo Panic!
        float delugeDmg = (6.4F + tier * 1.50F) * Math.max(0.5F, apparatusPotency);

        for (LivingEntity hit : hitTargets) {
            if (!hit.isAlive() || ApexEschatonSingularityHelper.isFriendly(hit, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;

            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 6.0D, 0.0D, 200L);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 600, 3)); // Slowness IV = exact -60% movement speed for 30.0s!
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 600);         // 30.0s AI disruption!
            hit.igniteForSeconds(5);

            CompoundTag d = EntityHelper.getPersistentData(hit);
            d.putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, level.getGameTime() + 100L);
            d.putLong(SpellLingeringZoneManager.TAG_FEARED_UNTIL, panicUntil); // 30.0s frantic blind terror!
            d.putDouble(SpellLingeringZoneManager.TAG_FEAR_X, caster.getX());
            d.putDouble(SpellLingeringZoneManager.TAG_FEAR_Z, caster.getZ());

            if (hit instanceof Mob mob) {
                mob.setTarget(null);
                mob.setAggressive(false);
            }
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithCataclysmMateriaAdvantage(level, hit, caster, delugeDmg, false, true, false);
            }
        }

        for (int s = 2; s <= 10; s += 4) {
            emitCataclysmBurstParticles(level, origin.add(dir.scale(s)), 14, s * 0.22D);
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
        // Section 6.55 Cataclysmic World-Breaker Skyfall [24.0s Bombardment + 12.0s Stygian Magma Slag, 16m AOE]:
        // Drops 8 colossal 3D Skyfall Pseudoblock molten obsidian & blackstone meteorites that permanently materialize
        // into real blocks upon spell completion; crushes hostiles, knocks them flat, shatters 7 armor points,
        // and leaves a 12.0s crater zone of Stygian Magma Slag (-70% speed, Wither III, & underwater true fire burn)!
        final int tier = spell.getEffectiveTier();
        final double r = Math.max(16.0D, radius);
        final BlockPos centerPos = BlockPos.containing(impactCenter);
        final double skyH = SpellDeliveryForm.probeCaveSafeSkyfallHeight(level, centerPos, tier);

        SpellWorldVfxManager.broadcastSkyfallPillar(level, impactCenter, r, EssenceType.CATACLYSM, EssenceType.NETHER, 720, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, impactCenter.add(0.0D, 0.04D, 0.0D), r, EssenceType.CATACLYSM, EssenceType.VOID, 720, tier);
        level.playSound(null, centerPos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.2F, 0.68F);

        final int meteorCount = 8;
        final BlockPos[] meteorTargets = new BlockPos[meteorCount];
        meteorTargets[0] = SpellWorldVfxManager.findSkyfallSurfaceBase(level, centerPos);
        for (int i = 1; i < meteorCount; i++) {
            double ang = (Math.PI * 2.0D * (i - 1) / (meteorCount - 1.0D)) + level.random.nextDouble() * 0.40D;
            double dist = 0.22D * r + level.random.nextDouble() * (0.58D * r);
            BlockPos raw = BlockPos.containing(impactCenter.x + Math.cos(ang) * dist, impactCenter.y, impactCenter.z + Math.sin(ang) * dist);
            meteorTargets[i] = SpellWorldVfxManager.findSkyfallSurfaceBase(level, raw);
        }

        final boolean[] landed = new boolean[meteorCount];
        final boolean[] materialized = {false};

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 720, level.getGameTime(), spell, slot, EssenceType.CATACLYSM, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed >= zone.maxDurationTicks) {
                        if (!materialized[0]) {
                            materialized[0] = true;
                            for (int mIdx = 0; mIdx < meteorCount; mIdx++) {
                                BlockState pseudoState = resolveWorldBreakerMeteorBlockState(mIdx);
                                int colHeight = (mIdx % 2 == 0) ? 2 : 1;
                                SpellWorldVfxManager.materializeSkyfallPseudoblockColumn(srv, meteorTargets[mIdx], pseudoState, colHeight);
                            }
                        }
                        return false;
                    }

                    boolean storm = elapsed < 480; // 24.0s active bombardment, then 12.0s Stygian Magma Slag!
                    int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;

                    if (storm) {
                        for (int mIdx = 0; mIdx < meteorCount; mIdx++) {
                            int impactTick = 22 + mIdx * 56;
                            BlockPos strikePos = meteorTargets[mIdx];
                            BlockState pseudoState = resolveWorldBreakerMeteorBlockState(mIdx);
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
                                        EssenceType.CATACLYSM,
                                        zTier
                                );
                            } else if (elapsed == impactTick) {
                                landed[mIdx] = true;
                                Vec3 hitVec = new Vec3(strikePos.getX() + 0.5D, strikePos.getY(), strikePos.getZ() + 0.5D);
                                SpellWorldVfxManager.broadcastProjectileImpact(
                                        srv, hitVec.add(0.0D, 0.25D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                                        EssenceType.CATACLYSM, EssenceType.NETHER, 2.85F, zTier
                                );
                                srv.sendParticles(
                                        new BlockParticleOption(ParticleTypes.BLOCK, pseudoState),
                                        hitVec.x, hitVec.y + 0.6D, hitVec.z, 26, 1.2D, 0.5D, 1.2D, 0.10D
                                );
                                srv.playSound(null, strikePos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.95F, 0.72F);
                            }
                        }

                        if (elapsed % 8 == 0) {
                            double ang = srv.random.nextDouble() * Math.PI * 2.0D;
                            double dist = Math.sqrt(srv.random.nextDouble()) * (zone.radius * 0.88D);
                            Vec3 boulderImpact = zone.center.add(Math.cos(ang) * dist, 0.25D, Math.sin(ang) * dist);
                            SpellWorldVfxManager.broadcastProjectileImpact(
                                    srv, boulderImpact, new Vec3(0.0D, 1.0D, 0.0D),
                                    EssenceType.CATACLYSM, EssenceType.VOID, 1.75F, zTier
                            );
                        }
                    }

                    for (int mIdx = 0; mIdx < meteorCount; mIdx++) {
                        if (landed[mIdx]) {
                            SpellWorldVfxManager.repelEntitiesFromPseudoblockColumn(srv, meteorTargets[mIdx], (mIdx % 2 == 0) ? 2 : 1, false);
                        }
                    }

                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.LAVA, zone.center.x, zone.center.y + (storm ? 5.0D : 0.35D), zone.center.z,
                                22, zone.radius * 0.55D, storm ? 2.5D : 0.35D, zone.radius * 0.55D, 0.05D);
                        srv.sendParticles(ParticleTypes.REVERSE_PORTAL, zone.center.x, zone.center.y + (storm ? 4.8D : 0.45D), zone.center.z,
                                16, zone.radius * 0.52D, storm ? 2.2D : 0.35D, zone.radius * 0.52D, 0.04D);
                    }

                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        long now = srv.getGameTime();
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        double rSq = (zone.radius + 0.5D) * (zone.radius + 0.5D);
                        float stormDmg = (6.2F + zTier * 1.45F) * Math.max(0.5F, zone.apparatusPotency);
                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 8.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 14.0D, zone.center.z + zone.radius
                        );
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && ApexEschatonSingularityHelper.isFriendly(foe, owner)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) continue;
                            double dx = foe.getX() - zone.center.x;
                            double dz = foe.getZ() - zone.center.z;
                            if (dx * dx + dz * dz > rSq) continue;

                            if (storm) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 7.0D, 0.0D, 200L);
                                foe.setDeltaMovement(foe.getDeltaMovement().x * 0.25D, -0.52D, foe.getDeltaMovement().z * 0.25D);
                                foe.hasImpulse = true;
                                foe.igniteForSeconds(4);
                                EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 60L);
                                if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                                    SpellCombatReactionEngine.hurtWithCataclysmMateriaAdvantage(srv, foe, owner, stormDmg, false, true, false);
                                }
                                if (zone.spell != null && zone.slot != null) {
                                    SpellExecutionEngine.deliverSubSpellOnEntity(
                                            srv, owner, foe, new Vec3(0.0D, -1.0D, 0.0D),
                                            zone.spell, zone.slot, EssenceType.CATACLYSM, zone.apparatusPotency * 0.55F
                                    );
                                }
                            } else {
                                // 12.0s Stygian Magma Slag: exact -70% hostile speed, Wither III, & underwater true fire burn!
                                SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, srv, 0.70D, 35L);
                                foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 45, 2));
                                foe.igniteForSeconds(3);
                                EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 55L);
                                SpellCombatReactionEngine.hurtWithCataclysmMateriaAdvantage(srv, foe, owner, 4.5F, false, false, true);
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
        tag.putString("SpellEidolonMateria", EssenceType.CATACLYSM.name());
        tag.putInt("SpellEidolonTintRGB", CATACLYSM_STYGIAN_CRIMSON_HEX_COLOR);
        SpellWorldVfxManager.broadcastSummonPillar(
                level, spawnPos, Math.max(1.8D, radius), EssenceType.CATACLYSM, EssenceType.NETHER, spell.getEffectiveTier()
        );
        emitCataclysmBurstParticles(level, spawnPos.add(0.0D, 1.0D, 0.0D), 30, 0.55D);
        level.playSound(null, BlockPos.containing(spawnPos), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.90F, 0.68F);
    }

    // =========================================================================
    // PART 3: CORE EFFECT MUTATIONS — BLINK, TRANSMUTE & AFFLICTION
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Section 6.55 Gravitational Void-Warp Collapse [100% SILENT 22m Wormhole]:
        // Departure collapses into a 6m pulling micro-singularity that crushes incoming arrows,
        // pulls pursuing hostiles inward, and stuns them for 2.5s (50t);
        // Arrival grants complete fire and fall damage immunity for 8.0s (160t) while shedding 100% of hostile mob aggro!
        long now = level.getGameTime();
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, now + 60L);

        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.CATACLYSM, potencyTier);
        SpellWorldVfxManager.broadcastVacuumSphere(
                level, departurePos.add(0.0D, 0.6D, 0.0D), 6.0D, true, EssenceType.CATACLYSM, EssenceType.VOID, 34, potencyTier
        );

        AABB dep = new AABB(
                departurePos.x - 6.0D, departurePos.y - 2.5D, departurePos.z - 6.0D,
                departurePos.x + 6.0D, departurePos.y + 4.0D, departurePos.z + 6.0D
        );
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(level, caster, dep, null, 0L);

        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, dep, e -> e.isAlive() && e != caster)) {
            if (ApexEschatonSingularityHelper.isFriendly(foe, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
            Vec3 in = departurePos.subtract(foe.position()).scale(0.44D);
            SpellExecutionEngine.applySyncedMotion(foe, new Vec3(in.x, 0.18D, in.z));
            foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 50, 0)); // 2.5s stun!
            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 6));
        }

        // Complete fire and fall damage immunity for 8.0s (160t) + 100% mob aggro shed (strictly NO explosion sound!)
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_OBLIVION_FIRE_FALL_IMMUNE_UNTIL, now + 160L);
        caster.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 160, 0, true, false));
        caster.clearFire();
        caster.resetFallDistance();
        ApexEschatonSingularityHelper.shedMobAggro(level, caster, arrivalPos, 28.0D);
        emitCataclysmBurstParticles(level, arrivalPos.add(0.0D, 0.9D, 0.0D), 18, 0.45D);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Section 6.55 Tectonic Subduction & Magma Vitrification [5.0s Channel, 12m Radius]:
        // Transmutes base Stone, Deepslate, & Cobblestone into Obsidian, Crying Obsidian, or Blackstone;
        // smelts all raw metal & mineral ores in a 12m radius with a +35% bonus yield and nugget drops;
        // vitrifies loose Sand & Red Sand into smooth Glass Blocks;
        // melts damaged equipment, unwanted tools, & metallic items directly into raw Void Slag and base ingots!
        if (state.isAir()) return false;

        if (ApexEschatonSingularityHelper.beginOrExecuteChanneledTransmute(
                level, caster, pos, 12, potencyTier, EssenceType.CATACLYSM,
                "Tectonic Subduction & Magma Vitrification", null)) {
            return true;
        }

        Item smeltedOre = resolveBlockSmeltedMineralOrMetal(level, state);
        if (smeltedOre != null) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            int count = (level.random.nextFloat() < 0.35F) ? 2 : 1; // +35% bonus yield!
            spawnDroppedStack(level, pos, new ItemStack(smeltedOre, count));
            spawnDroppedStack(level, pos, new ItemStack(resolveMatchingNugget(smeltedOre), 2));
            return true;
        }

        if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
            level.setBlockAndUpdate(pos, Blocks.GLASS.defaultBlockState());
            return true;
        }

        if (state.is(Blocks.STONE)
                || state.is(Blocks.DEEPSLATE)
                || state.is(Blocks.COBBLED_DEEPSLATE)
                || state.is(Blocks.COBBLESTONE)
                || state.is(Blocks.GRANITE)
                || state.is(Blocks.DIORITE)
                || state.is(Blocks.ANDESITE)
                || state.is(Blocks.TUFF)) {
            BlockState out = switch (level.random.nextInt(3)) {
                case 0 -> Blocks.OBSIDIAN.defaultBlockState();
                case 1 -> Blocks.CRYING_OBSIDIAN.defaultBlockState();
                default -> Blocks.BLACKSTONE.defaultBlockState();
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

        Item smelted = resolveItemSmeltedMineralOrMetal(level, stack);
        if (smelted != null) {
            int inCount = stack.getCount();
            int bonus = Math.round(inCount * 0.35F);
            if (bonus == 0 && level.random.nextFloat() < 0.35F) {
                bonus = 1;
            }
            int boosted = Math.min(64, inCount + Math.max(1, bonus));
            itemEntity.setItem(new ItemStack(smelted, boosted));
            Item nugget = resolveMatchingNugget(smelted);
            ItemEntity nugDrop = new ItemEntity(
                    level, itemEntity.getX(), itemEntity.getY() + 0.2D, itemEntity.getZ(),
                    new ItemStack(nugget, Math.min(64, inCount * 2))
            );
            nugDrop.setDefaultPickUpDelay();
            level.addFreshEntity(nugDrop);
            emitCataclysmBurstParticles(level, itemEntity.position().add(0.0D, 0.25D, 0.0D), 8, 0.20D);
            return true;
        }

        Item scrapIngot = resolveRecyclableEquipmentOrMetalIngot(stack);
        if (scrapIngot != null) {
            int itemCount = Math.max(1, stack.getCount());
            itemEntity.setItem(new ItemStack(scrapIngot, Math.min(64, itemCount * 2)));
            ItemStack voidSlag = createVoidSlagStack(itemCount);
            ItemEntity slagDrop = new ItemEntity(level, itemEntity.getX(), itemEntity.getY() + 0.2D, itemEntity.getZ(), voidSlag);
            slagDrop.setDefaultPickUpDelay();
            level.addFreshEntity(slagDrop);
            emitCataclysmBurstParticles(level, itemEntity.position().add(0.0D, 0.25D, 0.0D), 10, 0.25D);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.55 Abyssal World-Breaker Singularity Hex:
        // Continuously crushes the victim with inward gravitational pressure & unquenchable void-nether fire (burns underwater),
        // cuts movement speed by exact 70%, blocks jumping, shreds 50% of Armor Toughness, and causes 50% weapon self-recoil!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        long now = level.getGameTime();
        CompoundTag data = EntityHelper.getPersistentData(target);

        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.90D, 0.50D * mult), 45L);
        SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, Math.min(0.90D, 0.70D * mult), 35L); // Exact -70% move speed!
        data.putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 35L);
        data.putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 45L);
        target.igniteForSeconds(3);

        // Continuous inward/downward gravitational crush toward the imploding dimensional tear
        if (!data.contains("SpellCataclysmHexAnchorX")) {
            data.putDouble("SpellCataclysmHexAnchorX", target.getX());
            data.putDouble("SpellCataclysmHexAnchorZ", target.getZ());
        }
        double ax = data.getDouble("SpellCataclysmHexAnchorX").orElse(target.getX());
        double az = data.getDouble("SpellCataclysmHexAnchorZ").orElse(target.getZ());
        double dx = ax - target.getX();
        double dz = az - target.getZ();
        Vec3 crushPull = (dx * dx + dz * dz > 0.25D && !SpellCombatReactionEngine.isBossEntity(level, target))
                ? new Vec3(dx, 0.0D, dz).normalize().scale(0.26D)
                : Vec3.ZERO;
        target.setDeltaMovement(
                target.getDeltaMovement().x * 0.45D + crushPull.x,
                Math.min(-0.18D, target.getDeltaMovement().y - 0.16D),
                target.getDeltaMovement().z * 0.45D + crushPull.z
        );
        target.hasImpulse = true;

        LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCasterPublic(level, target);
        float crushDmg = (2.75F + potencyTier * 0.70F) * mult;
        SpellCombatReactionEngine.hurtWithCataclysmMateriaAdvantage(level, target, hexCaster, crushDmg, false, false, true);
        emitCataclysmBurstParticles(level, target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D), 6, 0.28D);
        return true;
    }

    // =========================================================================
    // INTERNAL HELPERS
    // =========================================================================

    public static ItemStack createVoidSlagStack(int count) {
        ItemStack slag = new ItemStack(ModBlocks.ASTRAL_IMPACTITE.get().asItem(), Math.clamp(count, 1, 64));
        slag.set(DataComponents.CUSTOM_NAME, Component.literal("§5Void Slag"));
        return slag;
    }

    private static BlockState resolveWorldBreakerMeteorBlockState(int index) {
        return switch (index & 3) {
            case 0 -> Blocks.OBSIDIAN.defaultBlockState();
            case 1 -> Blocks.CRYING_OBSIDIAN.defaultBlockState();
            case 2 -> Blocks.BLACKSTONE.defaultBlockState();
            default -> Blocks.MAGMA_BLOCK.defaultBlockState();
        };
    }

    private static void snuffTorchesWithinSixBlocks(ServerLevel level, BlockPos center) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-6, -3, -6), center.offset(6, 4, 6))) {
            if (center.distSqr(p) > 36.0D) continue;
            BlockState bs = level.getBlockState(p);
            if (bs.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
            if (bs.getBlock() instanceof TorchBlock
                    || bs.is(Blocks.TORCH) || bs.is(Blocks.WALL_TORCH)
                    || bs.is(Blocks.SOUL_TORCH) || bs.is(Blocks.SOUL_WALL_TORCH)) {
                level.destroyBlock(p, true);
            } else if (bs.getBlock() instanceof CampfireBlock && bs.getValue(CampfireBlock.LIT)) {
                level.setBlockAndUpdate(p, bs.setValue(CampfireBlock.LIT, false));
            } else if (bs.getBlock() instanceof CandleBlock && bs.getValue(CandleBlock.LIT)) {
                level.setBlockAndUpdate(p, bs.setValue(CandleBlock.LIT, false));
            }
        }
    }

    @Nullable
    private static Item resolveBlockSmeltedMineralOrMetal(ServerLevel level, BlockState state) {
        Item direct = ApexEschatonSingularityHelper.resolveSmeltedBlockOreOutput(state.getBlock());
        if (direct != null) return direct;
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        String path = key.getPath().toLowerCase(Locale.ROOT);
        if (!path.endsWith("_ore") || path.equals("entropic_ore")) {
            return null;
        }
        Item asItem = state.getBlock().asItem();
        if (asItem == Items.AIR) return null;
        ItemStack smelted = NetherSpellMutationHandler.trySmeltStack(level, new ItemStack(asItem, 1));
        return (!smelted.isEmpty() && smelted.getItem() != asItem) ? smelted.getItem() : null;
    }

    @Nullable
    private static Item resolveItemSmeltedMineralOrMetal(ServerLevel level, ItemStack stack) {
        Item direct = ApexEschatonSingularityHelper.resolveSmeltedOreOutput(stack.getItem());
        if (direct != null) return direct;
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().toLowerCase(Locale.ROOT);
        if (path.equals("raw_rubber") || path.equals("raw_materia_readout") || path.equals("raw_aurorafowl") || path.equals("entropic_ore")) {
            return null;
        }
        if (path.startsWith("raw_") || path.endsWith("_ore")) {
            ItemStack smelted = NetherSpellMutationHandler.trySmeltStack(level, new ItemStack(stack.getItem(), 1));
            if (!smelted.isEmpty() && smelted.getItem() != stack.getItem()) {
                return smelted.getItem();
            }
        }
        return null;
    }

    private static Item resolveMatchingNugget(Item smeltedOutput) {
        if (smeltedOutput == Items.GOLD_INGOT) {
            return Items.GOLD_NUGGET;
        }
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(smeltedOutput);
        String path = key.getPath();
        if (path.endsWith("_ingot")) {
            String base = path.substring(0, path.length() - 6);
            ResourceLocation nugLoc = ResourceLocation.fromNamespaceAndPath(key.getNamespace(), base + "_nugget");
            if (BuiltInRegistries.ITEM.containsKey(nugLoc)) {
                Item nug = BuiltInRegistries.ITEM.getValue(nugLoc);
                if (nug != Items.AIR) return nug;
            }
        }
        return Items.IRON_NUGGET;
    }

    @Nullable
    private static Item resolveRecyclableEquipmentOrMetalIngot(ItemStack stack) {
        Item direct = ApexEschatonSingularityHelper.resolveDamagedMetalScrapIngot(stack);
        if (direct != null) return direct;

        Item it = stack.getItem();
        if (it == Items.IRON_INGOT || it == Items.GOLD_INGOT || it == Items.COPPER_INGOT || it == Items.NETHERITE_SCRAP) {
            return null; // Do not recursively loop already-refined base ingots!
        }
        if (it == Items.BUCKET || it == Items.CAULDRON || it == Items.HOPPER || it == Items.MINECART
                || it == Items.ANVIL || it == Items.CHIPPED_ANVIL || it == Items.DAMAGED_ANVIL
                || it == Items.IRON_BARS || it == Items.IRON_DOOR || it == Items.IRON_TRAPDOOR
                || it == Items.IRON_CHAIN || it == Items.LANTERN || it == Items.SOUL_LANTERN
                || it == Items.HEAVY_WEIGHTED_PRESSURE_PLATE || it == Items.TRIPWIRE_HOOK) {
            return Items.IRON_INGOT;
        }
        if (it == Items.LIGHT_WEIGHTED_PRESSURE_PLATE || it == Items.BELL || it == Items.CLOCK) {
            return Items.GOLD_INGOT;
        }
        if (it == Items.LIGHTNING_ROD || it == Items.SPYGLASS || it == Items.BRUSH) {
            return Items.COPPER_INGOT;
        }

        ResourceLocation key = BuiltInRegistries.ITEM.getKey(it);
        String path = key.getPath().toLowerCase(Locale.ROOT);
        boolean isEquipmentOrTool = stack.has(DataComponents.MAX_DAMAGE)
                || path.endsWith("_sword") || path.endsWith("_pickaxe") || path.endsWith("_axe")
                || path.endsWith("_shovel") || path.endsWith("_hoe") || path.endsWith("_helmet")
                || path.endsWith("_chestplate") || path.endsWith("_leggings") || path.endsWith("_boots");

        if (path.contains("copper")) {
            return Items.COPPER_INGOT;
        }
        if (path.contains("netherite")) {
            return Items.NETHERITE_SCRAP;
        }
        if (path.contains("gold") || path.contains("gilded")) {
            return Items.GOLD_INGOT;
        }
        if (path.contains("iron") || path.contains("chainmail") || path.contains("steel")) {
            return Items.IRON_INGOT;
        }

        // Support modded metal tools/armor (e.g. bronze_pickaxe -> bronze_ingot, arcanite_sword -> arcanite_ingot)
        int underscore = path.indexOf('_');
        if (underscore > 0) {
            String prefix = path.substring(0, underscore);
            if (!prefix.equals("wooden") && !prefix.equals("stone") && !prefix.equals("leather")
                    && !prefix.equals("diamond") && !prefix.equals("turtle")) {
                ResourceLocation moddedIngot = ResourceLocation.fromNamespaceAndPath(key.getNamespace(), prefix + "_ingot");
                if (BuiltInRegistries.ITEM.containsKey(moddedIngot)) {
                    Item out = BuiltInRegistries.ITEM.getValue(moddedIngot);
                    if (out != Items.AIR) return out;
                }
            }
        }

        // Unwanted damaged equipment fallback (e.g. damaged bows/crossbows/stone/diamond tools melted in tectonic subduction)
        if (isEquipmentOrTool && stack.isDamaged()) {
            return Items.IRON_INGOT;
        }
        return null;
    }

    private static void spawnDroppedStack(ServerLevel level, BlockPos pos, ItemStack stack) {
        if (stack.isEmpty()) return;
        ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack);
        drop.setDefaultPickUpDelay();
        level.addFreshEntity(drop);
    }

    private static void emitCataclysmBurstParticles(ServerLevel level, Vec3 center, int totalCount, double spread) {
        int perColor = Math.max(1, totalCount / CATACLYSM_PALETTE.length);
        for (int col : CATACLYSM_PALETTE) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_EMBER.get(), col, 22, 0.88F, 0.05F),
                    center.x, center.y, center.z, perColor, spread, spread * 0.60D, spread, 0.04D
            );
        }
        level.sendParticles(ParticleTypes.LAVA, center.x, center.y, center.z, Math.max(4, totalCount / 3),
                spread, spread * 0.50D, spread, 0.03D);
    }
}
