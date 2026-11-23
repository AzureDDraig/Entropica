package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.registry.AestheticGlassRegistry;
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
import ddraig.net.entropica.spell.vfx.SpellParticleHelper;
import ddraig.net.entropica.spell.vfx.SpellWorldVfxManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.WitherRoseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Complete per-part Signature Mutation implementation for
 * <b>Prismatic Materia (#51 — Chimera + Radiant / Refined Chimera Catalyst)</b>
 * (Spellcrafting System Guide Section 6.53).
 *
 * <p><b>Dynamic Spectrum Palette</b>: Cycles dynamically across radiant crystalline spectrums
 * ({@code PRISMATIC(new int[][]{})} -> {@code isDynamic() == true}: Diamond Cyan {@code #72F6FF},
 * Solar Gold {@code #FFEA78}, Vitae Rose {@code #FF6B9D}, Emerald {@code #35E67A},
 * Amethyst Violet {@code #B36BFF}, and Starlight White {@code #FFFFFF}).
 *
 * <p><b>Elemental Affinity</b>: Strong against {@code UNDEAD (+1)}, {@code BLIGHT (+1)},
 * {@code SOULFIRE (+1)}, and {@code BLOOD (+1)} | Weak against {@code UMBRAL (-1)}.
 */
public final class PrismaticSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int PRISMATIC_DIAMOND_HEX_COLOR = 0x72F6FF;
    public static final int PRISMATIC_SOLAR_GOLD_HEX_COLOR = 0xFFEA78;
    public static final int PRISMATIC_ROSE_HEX_COLOR = 0xFF6B9D;
    public static final int PRISMATIC_EMERALD_HEX_COLOR = 0x35E67A;
    public static final int PRISMATIC_VIOLET_HEX_COLOR = 0xB36BFF;
    public static final int PRISMATIC_STARLIGHT_WHITE_HEX_COLOR = 0xFFFFFF;

    public static final int[] SPECTRUM_PALETTE = new int[]{
            PRISMATIC_DIAMOND_HEX_COLOR,
            PRISMATIC_SOLAR_GOLD_HEX_COLOR,
            PRISMATIC_ROSE_HEX_COLOR,
            PRISMATIC_EMERALD_HEX_COLOR,
            PRISMATIC_VIOLET_HEX_COLOR,
            PRISMATIC_STARLIGHT_WHITE_HEX_COLOR
    };

    private static final BlockState[] PRISM_VAULT_CRYSTALS = new BlockState[]{
            Blocks.CYAN_STAINED_GLASS.defaultBlockState(),
            Blocks.MAGENTA_STAINED_GLASS.defaultBlockState(),
            Blocks.YELLOW_STAINED_GLASS.defaultBlockState(),
            Blocks.LIME_STAINED_GLASS.defaultBlockState()
    };

    private static final ResourceLocation STAR_SHOWER_ATK_SPEED_ID =
            ResourceLocation.fromNamespaceAndPath("entropica", "prismatic_star_shower_atk_speed");

    @Override
    public EssenceType materiaType() {
        return EssenceType.PRISMATIC;
    }

    @Override
    public @Nullable String resolveFlowTitle(SpellFlowModifier flow) {
        return flow == SpellFlowModifier.ORBIT_SATELLITE ? "Prismatic Kaleidoscope Orbit Flow" : null;
    }

    @Override
    public @Nullable String resolveFlowDescription(SpellFlowModifier flow) {
        return flow == SpellFlowModifier.ORBIT_SATELLITE
                ? "Conjures 4 orbiting diamond prism-lenses in a halo around the caster or spell; refracts incoming "
                        + "physical and magic projectiles into harmless rainbow motes, fires autonomous retaliatory light darts, "
                        + "and pulses a 4-block wave granting allies +20% attack speed."
                : null;
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Prismatic Oscillating Light-Disc";
            case BEAM -> "Bifurcated Rainbow Spectrum Ray & Prismatic Meltdown";
            case RADIAL_BURST -> "Rainbow Bloom Shockwave";
            case TOUCH_STRIKE -> "Prismatic Crystalline Palm-Shatter";
            case AURA -> "Chromatic Dispersion Aura";
            case GLYPH -> "Refractive Prism-Vault Glyph";
            case CONE -> "Chromatic Prismatic Deluge Cone";
            case SKYFALL -> "Chromatic Star-Shower";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE ->
                    "Launches a high-velocity oscillating disc of condensed refractive starlight; ricochets off solid blocks "
                            + "and entities up to 6 times, shattering 4 armor points per bounce, deflecting hostile projectiles "
                            + "along its wake, and collapsing on final impact in a blinding flash that heals wounded allies for 5 hearts.";
            case BEAM ->
                    "Channels a wall-penetrating rainbow laser that splits into 3 secondary tracking beams upon contact, "
                            + "siphoning 1.5 hearts/sec and melting 2 armor/sec; 5.0s continuous channeling triggers a Prismatic "
                            + "Meltdown (-5 armor, 4.0s paralysis, and radiant party absorption shields).";
            case RADIAL_BURST ->
                    "Omnidirectional 10-block rainbow bloom shockwave (zero light blocks); purges fire, cobwebs, and "
                            + "potion clouds, deals heavy Prismatic damage (+1 vs Undead, Blight, Soulfire & Blood), shatters 5 armor "
                            + "points, and restores 8 hearts to all party members.";
            case TOUCH_STRIKE ->
                    "Strikes through 50% of physical armor with a concussive blinding flashbang that disarms the target for "
                            + "4.0s, shatters 5 armor points, and outlines the victim in radiant chromatic light for 10.0s.";
            case AURA ->
                    "Undulating rainbow dispersion aura (mobile or Sneak-Cast anchored, zero physical light blocks); grants "
                            + "full fire, lava, and drowning immunity, slow-fall gliding, and a 5-block pulse every 2.0s that burns "
                            + "Undead and Blight mobs for true Prismatic damage.";
            case GLYPH ->
                    "Crystalline geometric mandala glyph (1.0s arming, 50% Sneak-Right-Click disarm refund, Single or Repeating); "
                            + "when triggered within 4 blocks, encases trapped mobs in solid prismatic crystal blocks for 5.0s, "
                            + "shreds 4 armor points, and forces armed monsters to drop their held weapons upon breaking free.";
            case CONE ->
                    "Wide wall-penetrating rainbow deluge cone; shatters 5 armor points, deflects incoming projectiles, and "
                            + "inflicts Chromatic Sensory Overload (25.0s AI disruption), while cleansing allies and granting +30% "
                            + "movement speed for 12.0s.";
            case SKYFALL ->
                    "20.0s Chromatic Star-Shower across a 14-block+ radius dropping 8 3D diamond prism-core spires that "
                            + "permanently materialize upon completion; shatters 6 armor points, knocks foes down, and leaves a 10.0s "
                            + "Crystalline Resonance Field (+100% ally attack speed, Jump Boost III, and corrupt mob smite).";
            case SUMMON_AGENT ->
                    "Summons a 100 HP Prismatic Valkyrie (45s refreshing lifecycle) — flying crystalline archon hurling rapid-fire "
                            + "piercing light javelins that bypass 50% armor and smite Undead & Blight foes.";
            case SUMMON_SENTRY ->
                    "Summons a 100 HP Refractive Mirror Monolith (45s refreshing lifecycle) — anchored diamond crystal spire "
                            + "projecting a 12-block refractive dome that reflects 100% of hostile projectile and magic damage.";
            case SUMMON_DECOY ->
                    "Summons a Sacrificial Solar Relic (14s duration) — taunts hostiles across 16 blocks and detonates in a "
                            + "10-block blinding dawn-burst granting nearby party members 15.0s of complete damage invulnerability.";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Prismatic Refractive Phase-Step";
            case TRANSMUTE, BREAK -> "Prismatic Ore Metamorphosis Transmutation";
            case AFFLICTION -> "Prismatic Refractive Dyschromia / Chromatic Overload Hex";
            case RESTORATION -> "Prismatic Spectrum Renewal";
            case WARD -> "Refractive Diamond Prism Ward";
            default -> "Prismatic Refractive " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK ->
                    "Instant 18-block teleport through barriers and solid walls; sheds 100% of mob aggro, grants 2.0s "
                            + "Invisibility, and leaves a Prismatic Mirror Decoy that forces pursuers to target it and explodes in a "
                            + "blinding flashbang if struck.";
            case TRANSMUTE, BREAK ->
                    "5.0s continuous channel across an 8-block radius; smelts all raw ores without fuel with a 10% chance "
                            + "to metamorphose into a different ore up to Diamond tier (strictly 1x yield).";
            case AFFLICTION ->
                    "Milk-immune spectral curse (-40% Armor Toughness, 50% attack fumble chance, positive buff inversion into "
                            + "damage, and continuous radiant-prismatic burn doubled vs Undead, Demonic, Blight & Blood).";
            default ->
                    "Infuses " + effect.getDisplayName() + " with dynamic spectrum-shifting Prismatic Catalyst Materia.";
        };
    }

    // =========================================================================
    // PART 1: FLOW SYNERGY — PRISMATIC KALEIDOSCOPE ORBIT FLOW
    // =========================================================================

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        if (flow != SpellFlowModifier.ORBIT_SATELLITE) {
            return;
        }
        LivingEntity owner = projectile.getOwner() instanceof LivingEntity l ? l : null;
        long now = level.getGameTime();

        // Refract incoming physical & hostile magic projectiles into harmless rainbow motes!
        refractHostileProjectilesToMotes(level, owner, projectile.getBoundingBox().inflate(2.25D), now);

        // 4 orbiting diamond prism-lens sparkles around the projectile
        double baseAng = projectile.tickCount * 0.42D;
        for (int i = 0; i < 4; i++) {
            double ang = baseAng + (Math.PI * 0.5D * i);
            int col = SPECTRUM_PALETTE[(projectile.tickCount / 2 + i) % SPECTRUM_PALETTE.length];
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), col, 10, 0.24F, 0.03F),
                    projectile.getX() + Math.cos(ang) * 0.42D,
                    projectile.getY() + Math.sin(ang * 0.5D) * 0.22D,
                    projectile.getZ() + Math.sin(ang) * 0.42D,
                    1, 0.02D, 0.02D, 0.02D, 0.005D
            );
        }

        // Autonomous retaliatory light dart every 10 ticks while flying
        if (projectile.tickCount % 10 == 0) {
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, projectile.getBoundingBox().inflate(4.5D),
                    e -> e.isAlive() && e != owner && e instanceof Monster
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, true)))) {
                SpellWorldVfxManager.broadcastChainArc(
                        level, projectile.position(), foe.getEyePosition().subtract(0.0D, 0.2D, 0.0D),
                        EssenceType.PRISMATIC, EssenceType.RADIANT, 4
                );
                SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 2.0D, 0.0D, 80L);
                foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
                SpellCombatReactionEngine.hurtWithPrismaticMateriaAdvantage(level, foe, owner, 2.6F, false, true);
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
        SpellWorldVfxManager.broadcastChainArc(level, from, to, EssenceType.PRISMATIC, EssenceType.RADIANT, spell.getEffectiveTier());
        if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(toTarget, caster, spell.hasWardFriendlyFireSafeguard())) {
            toTarget.removeEffect(MobEffects.INVISIBILITY);
            toTarget.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0));
            SpellCombatReactionEngine.applyTimedAttributeShred(toTarget, level, 2.0D, 0.0D, 80L);
        }
        refractHostileProjectilesToMotes(level, caster, new AABB(from, to).inflate(2.0D), level.getGameTime());
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
        // Section 6.53 Prismatic Oscillating Light-Disc: zero-gravity high-velocity disc with up to 6 ricochets!
        projectile.setNoGravity(true);
        level.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.90F, 1.48F);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        LivingEntity owner = projectile.getOwner() instanceof LivingEntity l ? l : null;
        long now = level.getGameTime();

        // Luminous refractive color filaments that deflect/refract hostile physical & magic projectiles!
        refractHostileProjectilesToMotes(level, owner, projectile.getBoundingBox().inflate(2.0D), now);

        int c1 = AestheticGlassRegistry.hsbToRgb(((projectile.tickCount * 0.06F) % 1.0F), 0.78F, 1.0F);
        int c2 = AestheticGlassRegistry.hsbToRgb(((projectile.tickCount * 0.06F + 0.5F) % 1.0F), 0.78F, 1.0F);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), c1, 14, 0.28F, 0.03F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.09D, 0.04D, 0.09D, 0.01D
        );
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), c2, 12, 0.24F, 0.03F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.04D, 0.08D, 0.01D
        );
        if (projectile.tickCount % 3 == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.RUNE_DAGAZ.get(), PRISMATIC_STARLIGHT_WHITE_HEX_COLOR, 14, 0.22F, 0.02F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.04D, 0.04D, 0.04D, 0.005D
            );
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
        int tier = spell.getEffectiveTier();
        Vec3 pos = hitEntity.position().add(0.0D, hitEntity.getBbHeight() * 0.5D, 0.0D);
        SpellWorldVfxManager.broadcastProjectileImpact(
                level, pos, new Vec3(0.0D, 1.0D, 0.0D), EssenceType.PRISMATIC, EssenceType.RADIANT, 1.65F, tier
        );
        if (hitEntity instanceof LivingEntity liv && liv.isAlive()
                && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                        liv, caster, spell.hasWardFriendlyFireSafeguard()))) {
            // Shatter 4 armor points on each bounce + chromatic outline + Prismatic Materia damage when no Harm verb!
            SpellCombatReactionEngine.applyTimedAttributeShred(liv, level, 4.0D, 0.0D, 140L);
            liv.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
            if (!slot.hasHarmVerb()) {
                float discDmg = (4.2F + tier * 1.15F) * Math.max(0.5F, spell.flawProfile().getPotencyMultiplier());
                SpellCombatReactionEngine.hurtWithPrismaticMateriaAdvantage(level, liv, caster, discDmg, false, true);
            }
        }
        level.playSound(null, BlockPos.containing(pos), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.85F, 1.42F);
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
        Vec3 normal = Vec3.atLowerCornerOf(blockHit.getDirection().getUnitVec3i());
        SpellWorldVfxManager.broadcastProjectileImpact(
                level, blockHit.getLocation(), normal, EssenceType.PRISMATIC, EssenceType.RADIANT, 1.35F, spell.getEffectiveTier()
        );
    }

    @Override
    public void onProjectileExpiredFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        SpellProjectileEntity.triggerPrismaticFinalFlashHeal(level, caster, projectile.position());
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
        // Section 6.53 Bifurcated Rainbow Spectrum Ray & Prismatic Meltdown:
        // Wall-penetrating rainbow laser beam that automatically splits into 3 secondary tracking beams upon contact,
        // siphoning 1.5 hearts/sec (3.0 HP/s) while melting 2 armor/sec;
        // 5.0s continuous channeling triggers Prismatic Meltdown (-5 armor, 4.0s paralysis & radiant party absorption shields)!
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        long now = level.getGameTime();
        int tier = spell.getEffectiveTier();
        boolean continuousPulse = heldTicks > 0 || apparatusPotency <= 0.45F;
        float tickFrac = continuousPulse ? 0.25F : 1.0F;

        refractHostileProjectilesToMotes(level, caster,
                caster.getBoundingBox().expandTowards(direction.scale(maxReach)).inflate(1.2D), now);

        for (LivingEntity hit : hitEntities) {
            if (!hit.isAlive() || hit == caster) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, safeguard)) continue;

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("PrismaticBeamLastTick").orElse(-100L);
            if (last == now) continue;
            int step = heldTicks > 0
                    ? ((last >= 0L && now - last <= 28L) ? (int) Math.max(1L, Math.min(20L, now - last)) : Math.min(heldTicks, 4))
                    : 0;
            int beamTicks = heldTicks > 0
                    ? ((now - last <= 28L) ? data.getInt("PrismaticBeamTicks").orElse(0) + step : step)
                    : 0;
            data.putLong("PrismaticBeamLastTick", now);
            data.putInt("PrismaticBeamTicks", beamTicks);

            int meltedArmor = Math.min(8, Math.max(2, (beamTicks / 20) * 2));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, meltedArmor, 0.0D, 100L);
            hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));

            if (!slot.hasHarmVerb()) {
                float rayDmg = (3.8F + tier * 0.95F) * tickFrac;
                SpellCombatReactionEngine.hurtWithPrismaticMateriaAdvantage(level, hit, caster, rayDmg, false, true);
            }

            // Siphon 1.5 hearts/sec (3.0 HP/s) to caster!
            float siphonHp = 3.0F * tickFrac;
            SpellCombatReactionEngine.notifyMagicalHeal(caster, siphonHp);
            caster.heal(siphonHp);

            // Split into up to 3 secondary tracking rainbow beams on adjacent hostiles!
            Vec3 hitOrigin = hit.getEyePosition().subtract(0.0D, 0.25D, 0.0D);
            List<LivingEntity> nearby = level.getEntitiesOfClass(
                    LivingEntity.class,
                    hit.getBoundingBox().inflate(7.0D),
                    e -> e.isAlive()
                            && e != hit
                            && e != caster
                            && !hitEntities.contains(e)
                            && !ApexEschatonSingularityHelper.isFriendly(e, caster)
                            && (e instanceof Monster || e == caster.getLastHurtMob())
                            && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, caster, safeguard)
            );
            int splits = Math.min(3, nearby.size());
            for (int i = 0; i < splits; i++) {
                LivingEntity sec = nearby.get(i);
                Vec3 secPos = sec.getEyePosition().subtract(0.0D, 0.25D, 0.0D);
                EssenceType splitAccent = switch (i) {
                    case 0 -> EssenceType.RADIANT;
                    case 1 -> EssenceType.AURORA;
                    default -> EssenceType.VITAE;
                };
                SpellWorldVfxManager.broadcastBeam(level, hitOrigin, secPos, EssenceType.PRISMATIC, splitAccent, tier);
                SpellCombatReactionEngine.applyTimedAttributeShred(sec, level, 2.0D, 0.0D, 80L);
                sec.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
                float splitDmg = (4.2F + tier * 1.05F) * tickFrac;
                SpellCombatReactionEngine.hurtWithPrismaticMateriaAdvantage(level, sec, caster, splitDmg, false, true);
                level.sendParticles(ParticleTypes.END_ROD, secPos.x, secPos.y, secPos.z, 4, 0.18D, 0.20D, 0.18D, 0.03D);
            }

            // 5.0 continuous seconds (100 ticks) -> Prismatic Meltdown!
            if (heldTicks >= 100 && beamTicks >= 100) {
                data.putInt("PrismaticBeamTicks", 0);
                Vec3 burstCenter = hit.position().add(0.0D, 0.5D, 0.0D);
                SpellWorldVfxManager.broadcastRadialNova(level, burstCenter, 6.5D, EssenceType.PRISMATIC, EssenceType.RADIANT, tier);
                SpellWorldVfxManager.broadcastArcaneSeal(level, hit.position().add(0.0D, 0.05D, 0.0D), 6.5D, EssenceType.PRISMATIC, EssenceType.RADIANT, 54, tier);

                AABB meltdown = hit.getBoundingBox().inflate(6.5D);
                float meltdownDmg = 12.5F + tier * 2.35F;
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, meltdown, e -> e.isAlive() && e != caster)) {
                    if (ApexEschatonSingularityHelper.isFriendly(foe, caster)) continue;
                    if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, safeguard)) continue;
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 5.0D, 0.0D, 180L);
                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 80, 0)); // 4.0s paralysis!
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    SpellCombatReactionEngine.hurtWithPrismaticMateriaAdvantage(level, foe, caster, meltdownDmg, false, true);
                }

                // Grant all party members radiant absorption shields (+6 Absorption Hearts up to 12 Hearts cap)!
                SpellLingeringZoneManager.grantPartyAbsorption(level, caster, 12.0F, 24.0F, 12.0D);
                emitSpectrumBurstParticles(level, burstCenter, 36, 2.2D);
                level.playSound(null, hit.blockPosition(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.15F, 1.25F);
                level.playSound(null, hit.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.95F, 1.45F);
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
        // Section 6.53 Rainbow Bloom Shockwave:
        // Omnidirectional 10-block+ rainbow bloom shockwave (ZERO physical light blocks!),
        // instantly purges all terrain hazards (fire, cobwebs, powder snow, wither roses, potion clouds),
        // deals massive true holy-prismatic damage (+1 vs Undead, Blight, Soulfire, Blood),
        // shatters 5 armor points, and restores 8 hearts (16.0 HP) to all party members!
        int tier = spell.getEffectiveTier();
        double r = Math.max(10.0D, radius);
        double rSq = (r + 0.5D) * (r + 0.5D);
        BlockPos cPos = BlockPos.containing(center);
        int ir = (int) Math.min(12, Math.ceil(r));

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.PRISMATIC, EssenceType.RADIANT, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, center.add(0.0D, 0.04D, 0.0D), r, EssenceType.PRISMATIC, EssenceType.RADIANT, 58, tier);

        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-ir, -2, -ir), cPos.offset(ir, 3, ir))) {
            double dx = (p.getX() + 0.5D) - center.x;
            double dz = (p.getZ() + 0.5D) - center.z;
            if (dx * dx + dz * dz > rSq) continue;
            BlockState bs = level.getBlockState(p);
            if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
            if (bs.getBlock() instanceof BaseFireBlock
                    || bs.is(Blocks.COBWEB)
                    || bs.getBlock() instanceof PowderSnowBlock
                    || bs.getBlock() instanceof WitherRoseBlock
                    || bs.getBlock() instanceof SweetBerryBushBlock) {
                level.removeBlock(p, false);
            }
        }

        AABB box = new AABB(center.x - r, center.y - 2.5D, center.z - r, center.x + r, center.y + 4.5D, center.z + r);
        for (AreaEffectCloud cloud : level.getEntitiesOfClass(AreaEffectCloud.class, box)) {
            cloud.discard();
        }
        refractHostileProjectilesToMotes(level, caster, box, level.getGameTime());

        float novaDmg = (7.2F + tier * 1.65F) * Math.max(0.5F, apparatusPotency);
        boolean healedCaster = false;
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            double dx = ent.getX() - center.x;
            double dz = ent.getZ() - center.z;
            if (dx * dx + dz * dz > rSq) continue;

            if (ent == caster || ApexEschatonSingularityHelper.isFriendly(ent, caster)) {
                if (ent == caster) healedCaster = true;
                ent.clearFire();
                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(ent);
                SpellCombatReactionEngine.notifyMagicalHeal(ent, 16.0F); // 8 full hearts!
                ent.heal(16.0F);
                level.sendParticles(ParticleTypes.HEART, ent.getX(), ent.getY(1.0D), ent.getZ(), 6, 0.3D, 0.3D, 0.3D, 0.03D);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, 5.0D, 0.0D, 180L);
            ent.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            ent.addEffect(new MobEffectInstance(MobEffects.GLOWING, 160, 0));
            if (!SpellCombatReactionEngine.isBossEntity(level, ent)) {
                Vec3 horiz = new Vec3(dx, 0.0D, dz);
                Vec3 away = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize().scale(1.45D) : new Vec3(direction.x, 0.0D, direction.z);
                SpellExecutionEngine.applySyncedMotion(ent, new Vec3(away.x, 0.40D, away.z));
            }
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithPrismaticMateriaAdvantage(level, ent, caster, novaDmg, false, true);
            }
        }
        if (!healedCaster && caster.isAlive()) {
            caster.clearFire();
            SpellCombatReactionEngine.purifyHexesAndNegativeEffects(caster);
            SpellCombatReactionEngine.notifyMagicalHeal(caster, 16.0F);
            caster.heal(16.0F);
        }

        emitSpectrumBurstParticles(level, center.add(0.0D, 0.8D, 0.0D), 54, r * 0.42D);
        level.playSound(null, cPos, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.05F, 1.38F);
        level.playSound(null, cPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.10F, 1.25F);
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
        // Section 6.53 Prismatic Crystalline Palm-Shatter:
        // Strikes through physical armor (50% bypass), concussive blinding flashbang disarming target 4.0s (80t),
        // shatters 5 armor points, 10.0s (200t) radiant chromatic Glowing, and heavy Prismatic Materia damage!
        int tier = spell.getEffectiveTier();
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D), normDir, 3.2D,
                EssenceType.PRISMATIC, EssenceType.RADIANT, tier
        );

        float strikeDmg = Math.max(4.8F + tier * 1.25F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.55F)
                * Math.max(0.5F, apparatusPotency) * spell.flawProfile().getPotencyMultiplier();
        boolean hitAny = false;
        for (int idx = 0; idx < hitEntities.size(); idx++) {
            LivingEntity hit = hitEntities.get(idx);
            if (!hit.isAlive() || hit == caster) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            hitAny = true;
            float cleaveScale = (idx == 0) ? 1.0F : 0.55F;

            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 5.0D, 0.0D, 160L);
            ApexEschatonSingularityHelper.disableShield(hit, 80);
            if (hit.isUsingItem()) {
                hit.stopUsingItem();
            }
            SpellCombatReactionEngine.applyTimedAttackDebuff(hit, level, 0.65D, 0.0D, 80L);
            hit.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 3)); // 4.0s disarm!
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0)); // 4.0s flashbang!
            hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0)); // 10.0s chromatic outline!

            Vec3 hitMid = hit.position().add(0.0D, hit.getBbHeight() * 0.5D, 0.0D);
            SpellWorldVfxManager.broadcastProjectileImpact(level, hitMid, normDir, EssenceType.PRISMATIC, EssenceType.RADIANT, 2.25F, tier);
            emitSpectrumBurstParticles(level, hitMid, 22, 0.55D);

            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithPrismaticMateriaAdvantage(
                        level, hit, caster, strikeDmg * cleaveScale, true, true
                );
            }
        }
        if (hitAny) {
            level.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.0F, 1.32F);
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
        // Section 6.53 Chromatic Dispersion Aura (ZERO physical light blocks; mobile or Sneak-Cast anchored!):
        // Full Fire, Lava, & Drowning immunity, Slow-Fall gliding, and 5-block pulse every 2.0s (40t)
        // burning Undead & Blight mobs for true Prismatic damage!
        int tier = spell.getEffectiveTier();
        int dur = 260 + tier * 45;
        long now = level.getGameTime();
        double auraRad = Math.max(5.0D, radius);

        if (caster.isShiftKeyDown()) {
            Vec3 anchor = caster.position();
            SpellWorldVfxManager.broadcastWardShell(level, anchor.add(0.0D, 1.0D, 0.0D), auraRad, EssenceType.PRISMATIC, EssenceType.RADIANT, dur, tier);
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, anchor, auraRad, dur, now, spell, slot, EssenceType.PRISMATIC, apparatusPotency,
                    (srv, zone, elapsed) -> {
                        if (elapsed > zone.maxDurationTicks) return false;
                        LivingEntity owner = zone.resolveCaster(srv);
                        long zNow = srv.getGameTime();
                        AABB box = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 1.5D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 3.5D, zone.center.z + zone.radius
                        );
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                            if (ent == owner || (owner != null && ApexEschatonSingularityHelper.isFriendly(ent, owner))) {
                                ent.clearFire();
                                EntityHelper.getPersistentData(ent).putLong(SpellLingeringZoneManager.TAG_PRISMATIC_SHROUD_UNTIL, zNow + 16L);
                                ent.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 30, 0, true, false));
                                ent.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 30, 0, true, false));
                                ent.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 30, 0, true, false));
                                ent.resetFallDistance();
                            } else if (elapsed % 40 == 0
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                            ent, owner, zone.spell.hasWardFriendlyFireSafeguard()))
                                    && isCorruptUndeadOrBlight(ent, zNow)) {
                                SpellCombatReactionEngine.hurtWithPrismaticMateriaAdvantage(
                                        srv, ent, owner, 4.5F + tier * 0.85F, false, true
                                );
                                ent.igniteForSeconds(3);
                                srv.sendParticles(ParticleTypes.END_ROD, ent.getX(), ent.getY(0.7D), ent.getZ(), 8, 0.25D, 0.25D, 0.25D, 0.04D);
                            }
                        }
                        if (elapsed % 10 == 0) {
                            emitSpectrumBurstParticles(srv, zone.center.add(0.0D, 0.6D, 0.0D), 10, zone.radius * 0.45D);
                        }
                        return true;
                    }
            ));
        } else {
            caster.clearFire();
            EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_PRISMATIC_SHROUD_UNTIL, now + dur);
            caster.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, dur, 0, false, true));
            caster.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, dur, 0, false, true));
            caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, dur, 0, false, true));
            SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.65F, null);
        }

        emitSpectrumBurstParticles(level, caster.position().add(0.0D, 1.0D, 0.0D), 24, 0.55D);
        level.playSound(null, caster.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.85F, 1.42F);
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
        // Section 6.53 Refractive Prism-Vault Glyph:
        // 1.0s (20t) arming delay, 50% Sneak-Right-Click disarm refund, Single or Repeating mode;
        // 4m hostile trigger -> encases trapped mobs in solid prismatic crystal blocks for 5.0s (100t),
        // shredding 4 armor points and forcing monsters to drop their held weapons upon breaking free!
        final int tier = spell.getEffectiveTier();
        final int maxWaitTicks = tier >= 5 ? 72_000_000 : 12_000;
        final double triggerRad = Math.max(4.0D, radius);
        String subMode = slot.primaryEffect().subMode();
        final boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        final int[] cooldown = {0};

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster,
                glyphCenter,
                triggerRad,
                maxWaitTicks,
                level.getGameTime(),
                spell,
                slot,
                EssenceType.PRISMATIC,
                apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 10 == 0) {
                        int col = SPECTRUM_PALETTE[(elapsed / 10) % SPECTRUM_PALETTE.length];
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), col, 20, 0.35F, 0.03F),
                                zone.center.x, zone.center.y + 0.12D, zone.center.z,
                                6, triggerRad * 0.42D, 0.06D, triggerRad * 0.42D, 0.01D
                        );
                    }
                    // 1.0s (20-tick) arming grace period + repeating re-arm cooldown
                    if (elapsed < 20) return true;
                    if (cooldown[0] > 0) {
                        cooldown[0]--;
                        return true;
                    }

                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB trig = new AABB(
                            zone.center.x - 4.0D, zone.center.y - 1.5D, zone.center.z - 4.0D,
                            zone.center.x + 4.0D, zone.center.y + 2.5D, zone.center.z + 4.0D
                    );
                    List<LivingEntity> victims = srv.getEntitiesOfClass(
                            LivingEntity.class,
                            trig,
                            e -> e.isAlive()
                                    && e != owner
                                    && (owner == null || !ApexEschatonSingularityHelper.isFriendly(e, owner))
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                            e, owner, zone.spell.hasWardFriendlyFireSafeguard()))
                    );
                    if (victims.isEmpty()) return true;

                    SpellWorldVfxManager.broadcastWardShell(
                            srv, zone.center.add(0.0D, 1.1D, 0.0D), 4.0D, EssenceType.PRISMATIC, EssenceType.RADIANT, 100, tier
                    );
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.PLAYERS, 1.05F, 1.25F);

                    Map<BlockPos, Block> prismShell = new HashMap<>();
                    List<LivingEntity> trapped = new ArrayList<>(victims);
                    float trapDmg = (4.2F + tier * 1.05F) * Math.max(0.5F, zone.apparatusPotency);

                    for (int vIdx = 0; vIdx < trapped.size(); vIdx++) {
                        LivingEntity foe = trapped.get(vIdx);
                        SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 4.0D, 0.0D, 180L);
                        foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 100, 0));
                        foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 6));
                        foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0));
                        if (foe instanceof Mob mob) {
                            mob.getNavigation().stop();
                        }

                        // Encase trapped hostile in prismatic stained-glass crystal blocks for 5.0s (100t)!
                        BlockPos feet = foe.blockPosition();
                        BlockState crystalState = PRISM_VAULT_CRYSTALS[vIdx % PRISM_VAULT_CRYSTALS.length];
                        for (BlockPos p : List.of(
                                feet.north(), feet.south(), feet.east(), feet.west(),
                                feet.above().north(), feet.above().south(), feet.above().east(), feet.above().west(),
                                feet.above(2)
                        )) {
                            BlockState cur = srv.getBlockState(p);
                            if (cur.isAir()
                                    && !prismShell.containsKey(p)
                                    && !SpellExecutionEngine.isProtectedBlockOrContainer(srv, p, cur)) {
                                BlockPos imm = p.immutable();
                                prismShell.put(imm, crystalState.getBlock());
                                srv.setBlockAndUpdate(imm, crystalState);
                            }
                        }

                        if (!zone.slot.hasHarmVerb()) {
                            SpellCombatReactionEngine.hurtWithPrismaticMateriaAdvantage(srv, foe, owner, trapDmg, false, true);
                        }
                        SpellExecutionEngine.deliverSubSpellOnEntity(
                                srv, owner, foe, Vec3.ZERO, zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency
                        );
                    }

                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 4.5D, 100, srv.getGameTime(), null, null, EssenceType.PRISMATIC, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl < 100) {
                                    for (LivingEntity foe : trapped) {
                                        if (foe.isAlive()) {
                                            foe.setDeltaMovement(0.0D, Math.min(0.0D, foe.getDeltaMovement().y), 0.0D);
                                        }
                                    }
                                    return true;
                                }
                                for (Map.Entry<BlockPos, Block> entry : prismShell.entrySet()) {
                                    if (sLvl.getBlockState(entry.getKey()).is(entry.getValue())) {
                                        sLvl.setBlockAndUpdate(entry.getKey(), Blocks.AIR.defaultBlockState());
                                    }
                                }
                                for (LivingEntity foe : trapped) {
                                    if (!foe.isAlive()) continue;
                                    // Force monsters to drop their held weapons upon breaking free!
                                    if (foe instanceof Monster mob && !mob.getMainHandItem().isEmpty()) {
                                        ItemStack weapon = mob.getMainHandItem().copy();
                                        mob.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                                        ItemEntity droppedWeapon = new ItemEntity(sLvl, mob.getX(), mob.getY(0.5D), mob.getZ(), weapon);
                                        droppedWeapon.setDefaultPickUpDelay();
                                        sLvl.addFreshEntity(droppedWeapon);
                                    } else {
                                        ApexEschatonSingularityHelper.disableShield(foe, 100);
                                        foe.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 3));
                                    }
                                    emitSpectrumBurstParticles(sLvl, foe.position().add(0.0D, 0.8D, 0.0D), 14, 0.45D);
                                }
                                sLvl.playSound(null, BlockPos.containing(sZone.center), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.05F, 1.28F);
                                return false;
                            }
                    ));

                    if (repeating) {
                        cooldown[0] = 130;
                        return true;
                    }
                    return false;
                }
        ).markDisarmableGlyph().withOnDisarm((srv, z) ->
                emitSpectrumBurstParticles(srv, z.center.add(0.0D, 0.3D, 0.0D), 12, 0.4D)));

        level.playSound(null, BlockPos.containing(glyphCenter), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.85F, 1.38F);
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
        // Section 6.53 Chromatic Prismatic Deluge Cone:
        // Wide wall-penetrating rainbow deluge cone; shatters 5 armor points, deflects incoming projectiles,
        // inflicts Chromatic Sensory Overload (25.0s / 500t AI disruption, target loss & blind stumbling),
        // while cleansing allies and granting +30% movement speed for 12.0s (240t)!
        int tier = spell.getEffectiveTier();
        double reach = Math.max(10.0D, coneRange);
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 origin = caster.getEyePosition();

        SpellWorldVfxManager.broadcastVortexCone(
                level, origin.subtract(0.0D, 0.2D, 0.0D), dir, reach, EssenceType.PRISMATIC, EssenceType.RADIANT, tier
        );

        AABB coneBox = caster.getBoundingBox().expandTowards(dir.scale(reach)).inflate(reach * 0.55D);
        ApexEschatonSingularityHelper.deflectHostileProjectiles(level, caster, coneBox);

        float delugeDmg = (4.8F + tier * 1.15F) * Math.max(0.5F, apparatusPotency);
        List<LivingEntity> allInCone = new ArrayList<>(hitTargets);
        for (LivingEntity cand : level.getEntitiesOfClass(LivingEntity.class, coneBox, LivingEntity::isAlive)) {
            if (cand == caster || allInCone.contains(cand)) continue;
            Vec3 toCand = cand.getBoundingBox().getCenter().subtract(origin);
            double dist = toCand.length();
            if (dist > 0.1D && dist <= reach + 0.75D && toCand.scale(1.0D / dist).dot(dir) >= 0.48D) {
                allInCone.add(cand);
            }
        }

        for (LivingEntity hit : allInCone) {
            if (!hit.isAlive()) continue;
            if (ApexEschatonSingularityHelper.isFriendly(hit, caster)) {
                hit.clearFire();
                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(hit);
                hit.addEffect(new MobEffectInstance(MobEffects.SPEED, 240, 1)); // +30% speed for 12.0s!
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(hit, level, 0.0D, 0.0D, 0.0D, 0.30D, 240L);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 5.0D, 0.0D, 180L);
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 160, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));

            // Chromatic Sensory Overload: 25.0s (500t) AI disruption + lose attack target + blind stumbling!
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 500);
            if (hit instanceof Mob mob) {
                mob.setTarget(null);
                mob.setAggressive(false);
                if (mob instanceof PathfinderMob pf) {
                    double rAng = level.random.nextDouble() * Math.PI * 2.0D;
                    pf.getNavigation().moveTo(pf.getX() + Math.cos(rAng) * 6.0D, pf.getY(), pf.getZ() + Math.sin(rAng) * 6.0D, 1.15D);
                }
            }
            if (!hitTargets.contains(hit)) {
                SpellExecutionEngine.deliverSubSpellOnEntity(level, caster, hit, dir, spell, slot, fallbackMateria, apparatusPotency);
            }
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithPrismaticMateriaAdvantage(level, hit, caster, delugeDmg, false, true);
            }
        }

        for (int s = 2; s <= 10; s += 4) {
            emitSpectrumBurstParticles(level, origin.add(dir.scale(s)), 14, s * 0.18D);
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.32F);
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
        // Section 6.53 Chromatic Star-Shower [20.0s Extended Duration + 10.0s Crystalline Resonance Field, 14m AOE]:
        // Drops 8 brilliant 3D Skyfall Pseudoblock diamond prism-cores (phasing through foliage & glass) that stand
        // for the full spell duration and permanently materialize into real crystal spires upon completion;
        // shatters 6 armor points, continuously knocks enemies down, and leaves a 10.0s Crystalline Resonance Field
        // that doubles ally attack speed (+100%), grants Jump Boost III, and burns corrupt mobs!
        final int tier = spell.getEffectiveTier();
        final double r = Math.max(14.0D, radius);
        final BlockPos centerPos = BlockPos.containing(impactCenter);
        final double skyH = SpellDeliveryForm.probeCaveSafeSkyfallHeight(level, centerPos, tier);

        SpellWorldVfxManager.broadcastSkyfallPillar(level, impactCenter, r, EssenceType.PRISMATIC, EssenceType.RADIANT, 600, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, impactCenter.add(0.0D, 0.04D, 0.0D), r, EssenceType.PRISMATIC, EssenceType.RADIANT, 600, tier);
        level.playSound(null, centerPos, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.15F, 1.35F);

        final int prismCount = 8;
        final BlockPos[] prismTargets = new BlockPos[prismCount];
        prismTargets[0] = findGroundThroughFoliageAndGlass(level, centerPos);
        for (int i = 1; i < prismCount; i++) {
            double ang = (Math.PI * 2.0D * (i - 1) / (prismCount - 1.0D)) + level.random.nextDouble() * 0.42D;
            double dist = 0.22D * r + level.random.nextDouble() * (0.58D * r);
            BlockPos raw = BlockPos.containing(impactCenter.x + Math.cos(ang) * dist, impactCenter.y, impactCenter.z + Math.sin(ang) * dist);
            prismTargets[i] = findGroundThroughFoliageAndGlass(level, raw);
        }

        final boolean[] landed = new boolean[prismCount];
        final boolean[] materialized = {false};

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 600, level.getGameTime(), spell, slot, EssenceType.PRISMATIC, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed >= zone.maxDurationTicks) {
                        if (!materialized[0]) {
                            materialized[0] = true;
                            for (int pIdx = 0; pIdx < prismCount; pIdx++) {
                                BlockState pseudoState = resolvePrismCoreBlockState(pIdx);
                                int colHeight = (pIdx % 2 == 0) ? 2 : 1;
                                SpellWorldVfxManager.materializeSkyfallPseudoblockColumn(srv, prismTargets[pIdx], pseudoState, colHeight);
                            }
                            AABB clean = new AABB(
                                    zone.center.x - zone.radius - 4.0D, zone.center.y - 4.0D, zone.center.z - zone.radius - 4.0D,
                                    zone.center.x + zone.radius + 4.0D, zone.center.y + 8.0D, zone.center.z + zone.radius + 4.0D
                            );
                            for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, clean)) {
                                AttributeInstance inst = ent.getAttribute(Attributes.ATTACK_SPEED);
                                if (inst != null) inst.removeModifier(STAR_SHOWER_ATK_SPEED_ID);
                            }
                        }
                        return false;
                    }

                    boolean showerActive = elapsed < 400;
                    int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;

                    if (showerActive) {
                        for (int pIdx = 0; pIdx < prismCount; pIdx++) {
                            int impactTick = 20 + pIdx * 46;
                            BlockPos strikePos = prismTargets[pIdx];
                            BlockState pseudoState = resolvePrismCoreBlockState(pIdx);
                            int colHeight = (pIdx % 2 == 0) ? 2 : 1;

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
                                        EssenceType.PRISMATIC,
                                        zTier
                                );
                            } else if (elapsed == impactTick) {
                                landed[pIdx] = true;
                                Vec3 hitVec = new Vec3(strikePos.getX() + 0.5D, strikePos.getY(), strikePos.getZ() + 0.5D);
                                SpellWorldVfxManager.broadcastProjectileImpact(
                                        srv, hitVec.add(0.0D, 0.25D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                                        EssenceType.PRISMATIC, EssenceType.RADIANT, 2.45F, zTier
                                );
                                srv.sendParticles(
                                        new BlockParticleOption(ParticleTypes.BLOCK, pseudoState),
                                        hitVec.x, hitVec.y + 0.6D, hitVec.z, 22, 1.0D, 0.5D, 1.0D, 0.09D
                                );
                                srv.playSound(null, strikePos, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 0.95F, 1.32F);
                            }
                        }

                        if (elapsed % 8 == 0) {
                            double ang = srv.random.nextDouble() * Math.PI * 2.0D;
                            double dist = Math.sqrt(srv.random.nextDouble()) * (zone.radius * 0.88D);
                            Vec3 strike = zone.center.add(Math.cos(ang) * dist, 0.25D, Math.sin(ang) * dist);
                            SpellWorldVfxManager.broadcastProjectileImpact(
                                    srv, strike, new Vec3(0.0D, 1.0D, 0.0D),
                                    EssenceType.PRISMATIC, EssenceType.RADIANT, 1.55F, zTier
                            );
                        }
                    }

                    for (int pIdx = 0; pIdx < prismCount; pIdx++) {
                        if (landed[pIdx]) {
                            SpellWorldVfxManager.repelEntitiesFromPseudoblockColumn(srv, prismTargets[pIdx], (pIdx % 2 == 0) ? 2 : 1, false);
                        }
                    }

                    if (elapsed % 8 == 0) {
                        int col = SPECTRUM_PALETTE[(elapsed / 8) % SPECTRUM_PALETTE.length];
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), col, 22, 0.95F, 0.05F),
                                zone.center.x, zone.center.y + (showerActive ? 4.5D : 0.45D), zone.center.z,
                                22, zone.radius * 0.55D, showerActive ? 2.2D : 0.3D, zone.radius * 0.55D, 0.04D
                        );
                    }

                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        long zNow = srv.getGameTime();
                        float starDmg = (4.8F + zTier * 1.15F) * Math.max(0.5F, zone.apparatusPotency);
                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 8.5D, zone.center.z + zone.radius
                        );
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (ent == owner || (owner != null && ApexEschatonSingularityHelper.isFriendly(ent, owner))) {
                                if (!showerActive) {
                                    // 10.0s Crystalline Resonance Field: +100% attack speed (2x) & Jump Boost III!
                                    ent.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 35, 2));
                                    AttributeInstance inst = ent.getAttribute(Attributes.ATTACK_SPEED);
                                    if (inst != null) {
                                        inst.removeModifier(STAR_SHOWER_ATK_SPEED_ID);
                                        inst.addTransientModifier(new AttributeModifier(
                                                STAR_SHOWER_ATK_SPEED_ID, 1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                                        ));
                                    }
                                }
                                continue;
                            }
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, owner, wardSafe)) {
                                continue;
                            }
                            if (showerActive) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(ent, srv, 6.0D, 0.0D, 180L);
                                ent.setDeltaMovement(ent.getDeltaMovement().x * 0.25D, -0.48D, ent.getDeltaMovement().z * 0.25D);
                                ent.hasImpulse = true;
                                if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                                    SpellCombatReactionEngine.hurtWithPrismaticMateriaAdvantage(srv, ent, owner, starDmg, false, true);
                                }
                                if (zone.spell != null && zone.slot != null) {
                                    SpellExecutionEngine.deliverSubSpellOnEntity(
                                            srv, owner, ent, new Vec3(0.0D, -1.0D, 0.0D),
                                            zone.spell, zone.slot, EssenceType.PRISMATIC, zone.apparatusPotency * 0.50F
                                    );
                                }
                            } else if (isCorruptUndeadOrBlight(ent, zNow)) {
                                ent.igniteForSeconds(3);
                                SpellCombatReactionEngine.hurtWithPrismaticMateriaAdvantage(srv, ent, owner, 5.2F, false, true, 1.75F);
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
        tag.putString("SpellEidolonMateria", EssenceType.PRISMATIC.name());
        tag.putInt("SpellEidolonTintRGB", PRISMATIC_DIAMOND_HEX_COLOR);
        SpellWorldVfxManager.broadcastSummonPillar(
                level, spawnPos, Math.max(1.6D, radius), EssenceType.PRISMATIC, EssenceType.RADIANT, spell.getEffectiveTier()
        );
        emitSpectrumBurstParticles(level, spawnPos.add(0.0D, 1.0D, 0.0D), 28, 0.55D);
        level.playSound(null, BlockPos.containing(spawnPos), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.38F);
    }

    // =========================================================================
    // PART 3: CORE EFFECT MUTATIONS — BLINK, TRANSMUTE & AFFLICTION
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Section 6.53 Prismatic Refractive Phase-Step:
        // Instant 18m teleport through barriers & solid walls; sheds 100% of mob aggro, grants 2.0s (40t) Invisibility,
        // and leaves a Prismatic Mirror Decoy at departure that forces pursuers to target it and explodes in a flashbang if struck!
        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.PRISMATIC, potencyTier);

        SpellEidolonEntity decoy = SpellEidolonEntity.spawnShortLivedDecoy(
                level, caster, departurePos, EssenceType.PRISMATIC,
                20.0F, 100, "Prismatic Mirror Decoy", null
        );
        if (decoy != null) {
            for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(departurePos, departurePos).inflate(12.0D), Mob::isAlive)) {
                if (mob != decoy && !ApexEschatonSingularityHelper.isFriendly(mob, caster)) {
                    mob.setTarget(decoy);
                }
            }
        }

        caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0)); // 2.0s Invisibility!
        ApexEschatonSingularityHelper.shedMobAggro(level, caster, arrivalPos, 24.0D);
        emitSpectrumBurstParticles(level, departurePos.add(0.0D, 0.9D, 0.0D), 18, 0.40D);
        emitSpectrumBurstParticles(level, arrivalPos.add(0.0D, 0.9D, 0.0D), 18, 0.40D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.AMETHYST_CLUSTER_STEP, SoundSource.PLAYERS, 1.0F, 1.42F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Section 6.53 Prismatic Ore Metamorphosis Transmutation [5.0s Channel]:
        // Smelts all raw ores in an 8-block radius without fuel, with an intrinsic 10% chance that any smelted ore
        // transmutes into a different existing ore type up to Diamond tier (strictly 1x yield — no bonus yield)!
        if (ApexEschatonSingularityHelper.beginOrExecuteChanneledTransmute(
                level, caster, pos, 8, potencyTier, EssenceType.PRISMATIC,
                "Prismatic Ore Metamorphosis Transmutation", null)) {
            return true;
        }

        Item smelted = ApexEschatonSingularityHelper.resolveSmeltedBlockOreOutput(state.getBlock());
        if (smelted != null) {
            Item out = ApexEschatonSingularityHelper.rollPrismaticMetamorphosisSmeltOutput(level.random, smelted);
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, new ItemStack(out, 1));
            drop.setDefaultPickUpDelay();
            level.addFreshEntity(drop);
            emitSpectrumBurstParticles(level, Vec3.atCenterOf(pos), 10, 0.30D);
            level.playSound(null, pos, out != smelted ? SoundEvents.AMETHYST_BLOCK_CHIME : SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.BLOCKS, 0.75F, 1.35F);
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
        Item smelted = ApexEschatonSingularityHelper.resolveSmeltedOreOutput(stack.getItem());
        if (smelted == null) {
            return false;
        }

        // Roll independently per ore item in the stack (10% chance per ore to metamorphose up to Diamond; strictly 1x total yield!)
        int total = stack.getCount();
        Map<Item, Integer> counts = new HashMap<>();
        for (int i = 0; i < total; i++) {
            Item rolled = ApexEschatonSingularityHelper.rollPrismaticMetamorphosisSmeltOutput(level.random, smelted);
            counts.merge(rolled, 1, Integer::sum);
        }

        boolean firstAssigned = false;
        for (Map.Entry<Item, Integer> entry : counts.entrySet()) {
            ItemStack outStack = new ItemStack(entry.getKey(), entry.getValue());
            if (!firstAssigned) {
                itemEntity.setItem(outStack);
                firstAssigned = true;
            } else {
                ItemEntity extra = new ItemEntity(level, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), outStack);
                extra.setDefaultPickUpDelay();
                level.addFreshEntity(extra);
            }
        }
        emitSpectrumBurstParticles(level, itemEntity.position().add(0.0D, 0.3D, 0.0D), 8, 0.25D);
        return true;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.53 Prismatic Refractive Dyschromia / Chromatic Overload Hex:
        // Milk-immune spectral curse (-40% Armor Toughness, 50% fumble/miss chance, positive buff inversion into damage,
        // and continuous radiant-prismatic burn doubled 2x against Undead, Demonic, Blight, Soulfire & Blood entities)!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.85D, 0.40D * mult), 45L);
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 45, 0, true, true));

        int invertedBuffs = 0;
        for (MobEffectInstance eff : new ArrayList<>(target.getActiveEffects())) {
            if (eff.getEffect().value().isBeneficial()) {
                target.removeEffect(eff.getEffect());
                invertedBuffs++;
            }
        }

        float baseBurn = (1.85F + potencyTier * 0.52F + invertedBuffs * 2.5F) * mult;
        SpellCombatReactionEngine.hurtWithPrismaticMateriaAdvantage(level, target, null, baseBurn, false, true, 2.0F);
        emitSpectrumBurstParticles(level, target.position().add(0.0D, target.getBbHeight() * 0.6D, 0.0D), 6, 0.28D);
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
        if (effect == SpellCoreEffect.RESTORATION) {
            SpellCombatReactionEngine.purifyHexesAndNegativeEffects(target);
            float bonusHeal = 4.0F + potencyTier * 1.25F;
            SpellCombatReactionEngine.notifyMagicalHeal(target, bonusHeal);
            target.heal(bonusHeal);
            emitSpectrumBurstParticles(level, target.position().add(0.0D, 0.9D, 0.0D), 12, 0.35D);
        }
    }

    // =========================================================================
    // INTERNAL HELPERS
    // =========================================================================

    private static BlockState resolvePrismCoreBlockState(int index) {
        return switch (index & 3) {
            case 0 -> Blocks.SEA_LANTERN.defaultBlockState();
            case 1 -> Blocks.AMETHYST_BLOCK.defaultBlockState();
            case 2 -> Blocks.QUARTZ_PILLAR.defaultBlockState();
            default -> Blocks.CALCITE.defaultBlockState();
        };
    }

    private static BlockPos findGroundThroughFoliageAndGlass(ServerLevel level, BlockPos start) {
        BlockPos cursor = start;
        for (int up = 0; up < 6; up++) {
            BlockState s = level.getBlockState(cursor);
            if (!s.isSolidRender() || isFoliageOrGlass(s)) {
                break;
            }
            cursor = cursor.above();
        }
        for (int down = 0; down < 16; down++) {
            BlockPos below = cursor.below();
            BlockState bs = level.getBlockState(below);
            if (bs.isSolidRender() && !isFoliageOrGlass(bs)) {
                return cursor;
            }
            cursor = below;
        }
        return SpellWorldVfxManager.findSkyfallSurfaceBase(level, start);
    }

    private static boolean isFoliageOrGlass(BlockState state) {
        if (state.is(BlockTags.LEAVES)) return true;
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return path.contains("glass");
    }

    private static boolean isCorruptUndeadOrBlight(LivingEntity ent, long now) {
        if (ApexEschatonSingularityHelper.isDarkUndeadOrAbyssal(ent) || ent.getType().is(EntityTypeTags.UNDEAD)) {
            return true;
        }
        Set<EssenceType> affs = SpellCombatReactionEngine.resolveEntityAffinities(ent);
        return affs.contains(EssenceType.UNDEAD)
                || affs.contains(EssenceType.BLIGHT)
                || affs.contains(EssenceType.SOULFIRE)
                || affs.contains(EssenceType.BLOOD)
                || EntityHelper.getPersistentData(ent).getLong(SpellLingeringZoneManager.TAG_BLIGHT_CHAIN_MARK_UNTIL).orElse(0L) > now;
    }

    private static void refractHostileProjectilesToMotes(
            ServerLevel level,
            @Nullable LivingEntity owner,
            AABB area,
            long now
    ) {
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, area)) {
            if (owner != null && proj.getOwner() == owner) continue;
            if (proj instanceof SpellProjectileEntity sp) {
                if (owner != null && sp.getOwner() instanceof LivingEntity spOwner
                        && ApexEschatonSingularityHelper.isFriendly(spOwner, owner)) {
                    continue;
                }
                if (owner == null) continue;
            }
            int col = SPECTRUM_PALETTE[(int) (Math.abs(now + proj.getId()) % SPECTRUM_PALETTE.length)];
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), col, 16, 0.75F, 0.04F),
                    proj.getX(), proj.getY(), proj.getZ(), 8, 0.18D, 0.18D, 0.18D, 0.04D
            );
            proj.discard();
        }
    }

    private static void emitSpectrumBurstParticles(ServerLevel level, Vec3 center, int totalCount, double spread) {
        int perColor = Math.max(1, totalCount / SPECTRUM_PALETTE.length);
        for (int col : SPECTRUM_PALETTE) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), col, 20, 0.85F, 0.05F),
                    center.x, center.y, center.z, perColor, spread, spread * 0.65D, spread, 0.04D
            );
        }
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y, center.z, Math.max(4, totalCount / 3),
                spread, spread * 0.65D, spread, 0.05D);
    }
}
