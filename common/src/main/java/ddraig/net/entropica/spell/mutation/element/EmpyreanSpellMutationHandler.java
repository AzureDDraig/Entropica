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
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
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
import java.util.Set;

/**
 * Complete per-part Signature Mutation implementation for
 * <b>Empyrean Materia (#52 — Radiant + Vitae + Nature, Tier 5 Eschaton Fusion: "The Infinite Life")</b>
 * (Spellcrafting System Guide Section 6.54).
 *
 * <p><b>Palette</b>: Radiant Pure Gold ({@code #FFEA78}), Vitae Blossom Pink ({@code #FF6B9D}),
 * and Nature Forest Emerald ({@code #1A5923}).
 *
 * <p><b>Tier 5 Eschaton Scaling</b>: Wall-phasing delivery, 60% armor bypass, 6–7 armor shredding,
 * 24.0s + 12.0s Skyfall duration, 60.0s summon lifecycles (110 HP Agent / 120 HP Sentry), and 16-block effect radii.
 *
 * <p><b>Elemental Affinity</b>: Strong against {@code UNDEAD (+1)}, {@code WATER (+1)}, {@code EARTH (+1)},
 * {@code NETHER (+1)}, {@code VOID (+1)}, {@code OBLIVION (+1)}, {@code WRAITH (+1)}, {@code BARROW (+1)},
 * and {@code BLIGHT (+1)} | Weak against {@code UMBRAL (-1)} (Forced Single Weakness).
 */
public final class EmpyreanSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int EMPYREAN_SOLAR_GOLD_HEX_COLOR = 0xFFEA78;
    public static final int EMPYREAN_VITAE_PINK_HEX_COLOR = 0xFF6B9D;
    public static final int EMPYREAN_FOREST_EMERALD_HEX_COLOR = 0x1A5923;

    public static final int[] EMPYREAN_PALETTE = new int[]{
            EMPYREAN_SOLAR_GOLD_HEX_COLOR,
            EMPYREAN_VITAE_PINK_HEX_COLOR,
            EMPYREAN_FOREST_EMERALD_HEX_COLOR
    };

    @Override
    public EssenceType materiaType() {
        return EssenceType.EMPYREAN;
    }

    @Override
    public @Nullable String resolveFlowTitle(SpellFlowModifier flow) {
        return flow == SpellFlowModifier.ORBIT_SATELLITE ? "World-Tree Siphoning Root-Web Orbit Flow" : null;
    }

    @Override
    public @Nullable String resolveFlowDescription(SpellFlowModifier flow) {
        return flow == SpellFlowModifier.ORBIT_SATELLITE
                ? "Manifests 4 persistent golden root tendrils and sacred seed pods (#FFEA78 / #FF6B9D / #1A5923) circling "
                        + "the primary spell or caster; phases through solid walls, intercepts and vaporizes 100% of incoming "
                        + "enemy projectiles, and discharges seeking golden root-tendrils that root hostiles for 2.0s, shred "
                        + "3 armor points, and siphon health into party absorption shields."
                : null;
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "World-Tree Siphoning Seed-Pod";
            case BEAM -> "Sustained Root-Lance Sunbeam & Canopy Cataclysm";
            case RADIAL_BURST -> "World-Tree Heartwood Bastion Nova";
            case TOUCH_STRIKE -> "Siphoning World-Tree Root-Cleave";
            case AURA -> "Living Titan Bark & Canopy Mantle";
            case GLYPH -> "World-Tree Siphoning Root-Vault Sigil";
            case CONE -> "Solar-Verdant Spore-Web Deluge";
            case SKYFALL -> "Cataclysmic World-Tree Canopy Skyfall";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE ->
                    "Hurls a wall-phasing seed pod of condensed living gold, rose vitae, and forest emerald; pierces "
                            + "through cover and up to 8 targets (60% armor bypass), detonating on terminal impact into "
                            + "an 8-block Siphoning Solar-Root Web (3.5s briar root, -6 armor, and 35% damage leeched "
                            + "into party absorption shields).";
            case BEAM ->
                    "Channels a colossal wall-phasing beam of golden solar fire, rose vitae currents, and living roots; "
                            + "melts 2 armor/sec, siphons health and saturation into the party pool, and grounds aerial "
                            + "hostiles; 5.0s continuous channeling triggers Cataclysmic World-Tree Canopy Cataclysm "
                            + "(12m knockback, -7 armor, buff purge, 5.0s root, and 10m golden azalea sanctuary healing 4 hearts).";
            case RADIAL_BURST ->
                    "Wall-penetrating 12-block concussive shockwave (zero light blocks); knocks hostiles 10 blocks back "
                            + "flat, shatters 6 armor points, smites Undead and Dark entities (+60%), roots survivors 4.0s, "
                            + "and grants allies Aegis of the Infinite Life (+12 Armor, +6 Toughness, poison/wither/drowning/hunger "
                            + "immunity, and continuous party absorption for 16.0s).";
            case TOUCH_STRIKE ->
                    "Wall-cleaving living heartwood & golden solar-vine blade; bypasses 60% armor, shatters 6 armor "
                            + "points, disables shields 6.0s, roots victim 4.0s, and releases seeking golden seed tendrils "
                            + "wrapping adjacent enemies within 8 blocks to siphon health into party absorption.";
            case AURA ->
                    "Envelopes the caster in living golden titan barkskin and emerald-solar canopy (mobile or Sneak-anchored); "
                            + "+12 Armor, +6 Toughness, suffocation/drowning/poison/wither/hunger immunity, -85% mob detection, "
                            + "5m arrow/trident petal vaporization, and 6m root-web aura draining 2.0 hearts/sec of true damage "
                            + "into party absorption.";
            case GLYPH ->
                    "Solar-verdant mandala glyph on any floor, wall, or ceiling (disarmable, Single or Repeating); ally "
                            + "contact restores 10 full hearts, cures curses, and grants +8 Absorption hearts for 45.0s; "
                            + "hostile approach within 5m erupts an 8m golden titan root cage for 5.0s (-6 armor, buff purge, "
                            + "and 40% health leeched into floating golden healing dewdrops).";
            case CONE ->
                    "Wall-penetrating pressurized golden pollen, lotus spore, and emerald root cone; shatters 6 armor "
                            + "points, vaporizes incoming projectiles, roots hostiles 3.0s, and inflicts Empyrean "
                            + "Mesmerization (30.0s AI disruption and -60% movement speed).";
            case SKYFALL ->
                    "24.0s World-Tree Canopy Skyfall across a 16-block+ zone dropping 8 3D golden heartwood megaliths "
                            + "that permanently materialize upon completion; knocks foes flat, shatters 7 armor points, "
                            + "smites dark entities, and leaves a 12.0s World-Tree Consecrated Soil zone (-70% hostile speed, "
                            + "4.0s periodic root, and +2 hearts/sec ally healing + Saturation + +6 Armor).";
            case SUMMON_AGENT ->
                    "Summons a 110 HP Yggdrasil Arboreal Juggernaut (60s refreshing lifecycle) — wall-phasing barkskin "
                            + "titan delivering sweeping root cleaves (60% armor bypass, -4 armor/hit, 3.0s root, and 40% "
                            + "damage siphoned into party absorption shields).";
            case SUMMON_SENTRY ->
                    "Summons a 120 HP Heartwood Canopy Obelisk (60s refreshing lifecycle) — bombards foes up to 20m with "
                            + "seeking root-seed mortars (-3 armor/hit) while pulsing a 16m Sanctuary Field (+8 Armor, "
                            + "+4 Toughness, and continuous 10 HP absorption shield).";
            case SUMMON_DECOY ->
                    "Summons a Golden Willow Effigy (16s duration) — taunts hostiles across 16m, reflects 45% melee "
                            + "damage, and detonates in a 14m burst of true damage (-6 armor, 5.0s root, and 8-heart ally heal).";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Solar Slipstream Ascent";
            case TRANSMUTE, BREAK -> "World-Tree Genesis & Vitality Consecration";
            case AFFLICTION -> "Radiant Empyrean Brand Hex";
            case RESTORATION -> "Infinite World-Tree Benediction";
            case WARD -> "Living Heartwood Canopy Bastion";
            default -> "World-Tree Empyrean " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK ->
                    "Launches up to 22 blocks into mid-air and through solid ceilings; departure erupts a 6m golden root "
                            + "blast knocking foes back and rooting 3.0s; arrival grants 10.0s unrestricted 3D flight, "
                            + "Slow Falling, fall immunity, ground healing dewdrops, and 100% aggro shed.";
            case TRANSMUTE, BREAK ->
                    "5.0s continuous channel; transmutes Stone, Deepslate, and Netherrack into Sacred World-Tree Heartwood, "
                            + "Flowering Azalea Moss, and Sacred Loam; purges worn curses (+50% +1 enchant tier), cures "
                            + "Zombie Villagers with max discounts, and bottles standing water within 3m into Dew of the World-Tree.";
            case AFFLICTION ->
                    "Milk-immune holy life-fire brand (30.0s boss cap + 10.0s immunity); deals continuous true holy burn "
                            + "(2x vs Undead/Demonic/Abyssal), disables all health recovery, 50% fumble miss, -50% Armor "
                            + "Toughness, and +120% 32m Empyrean Starlight Beacon spawn lure.";
            default ->
                    "Infuses " + effect.getDisplayName() + " with Tier 5 Eschaton Empyrean World-Tree Materia.";
        };
    }

    // =========================================================================
    // PART 1: FLOW SYNERGY — WORLD-TREE SIPHONING ROOT-WEB ORBIT FLOW
    // =========================================================================

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        if (flow != SpellFlowModifier.ORBIT_SATELLITE) {
            return;
        }
        LivingEntity owner = projectile.getOwner() instanceof LivingEntity l ? l : null;
        int vanished = ApexEschatonSingularityHelper.vaporizeHostileProjectiles(
                level, owner, projectile.getBoundingBox().inflate(2.6D), null, 0L
        );
        if (vanished > 0 && owner != null) {
            SpellLingeringZoneManager.grantPartyAbsorption(level, owner, vanished * 2.0F, 32.0F, 12.0D);
        }

        // 4 persistent golden root tendrils & sacred seed pods circling the carrier
        double baseAng = projectile.tickCount * 0.38D;
        for (int i = 0; i < 4; i++) {
            double ang = baseAng + (Math.PI * 0.5D * i);
            int col = EMPYREAN_PALETTE[i % EMPYREAN_PALETTE.length];
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), col, 12, 0.28F, 0.03F),
                    projectile.getX() + Math.cos(ang) * 0.48D,
                    projectile.getY() + Math.sin(ang * 0.5D) * 0.24D,
                    projectile.getZ() + Math.sin(ang) * 0.48D,
                    1, 0.02D, 0.02D, 0.02D, 0.005D
            );
        }

        // Seeking golden root-tendrils: root 2.0s (40t), shred 3 armor points, siphon into party absorption!
        if (projectile.tickCount % 10 == 0) {
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, projectile.getBoundingBox().inflate(5.5D),
                    e -> e.isAlive() && e != owner && e instanceof Monster
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, true)))) {
                SpellWorldVfxManager.broadcastChainArc(
                        level, projectile.position(), foe.getEyePosition().subtract(0.0D, 0.2D, 0.0D),
                        EssenceType.EMPYREAN, EssenceType.NATURE, 7
                );
                foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 6));
                foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 40, 0));
                SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 3.0D, 0.0D, 120L);
                float dealt = SpellCombatReactionEngine.hurtWithEmpyreanMateriaAdvantage(
                        level, foe, owner, 3.40F, false, true, false, false
                );
                if (owner != null && dealt > 0.0F) {
                    SpellLingeringZoneManager.grantPartyAbsorption(level, owner, Math.max(2.0F, dealt * 0.50F), 32.0F, 12.0D);
                }
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
        SpellWorldVfxManager.broadcastChainArc(level, from, to, EssenceType.EMPYREAN, EssenceType.VITAE, spell.getEffectiveTier());
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(level, caster, new AABB(from, to).inflate(2.2D), null, 0L);
        if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(toTarget, caster, spell.hasWardFriendlyFireSafeguard())) {
            toTarget.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 6));
            toTarget.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 40, 0));
            SpellCombatReactionEngine.applyTimedAttributeShred(toTarget, level, 3.0D, 0.0D, 120L);
            SpellLingeringZoneManager.grantPartyAbsorption(level, caster, 2.5F, 32.0F, 12.0D);
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
        projectile.setNoGravity(true);
        level.playSound(null, caster.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.90F, 1.32F);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        int col = EMPYREAN_PALETTE[(projectile.tickCount / 2) % EMPYREAN_PALETTE.length];
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), col, 14, 0.32F, 0.03F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.09D, 0.09D, 0.09D, 0.01D
        );
        level.sendParticles(ParticleTypes.CHERRY_LEAVES, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.08D, 0.08D, 0.08D, 0.01D);
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
        if (hitEntity instanceof LivingEntity liv && liv.isAlive()
                && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                        liv, caster, spell.hasWardFriendlyFireSafeguard()))) {
            // Pierces up to 8 targets while bypassing 60% of armor rating!
            if (!slot.hasHarmVerb()) {
                float podDmg = (5.4F + tier * 1.35F) * Math.max(0.5F, spell.flawProfile().getPotencyMultiplier());
                SpellCombatReactionEngine.hurtWithEmpyreanMateriaAdvantage(level, liv, caster, podDmg, false, true, false, true);
            }
        }
        if (projectile.getPierceCountRemaining() <= 0) {
            detonateSolarRootWeb(level, caster, hitEntity.position(), spell);
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
            detonateSolarRootWeb(level, caster, blockHit.getLocation(), spell);
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
        detonateSolarRootWeb(level, caster, projectile.position(), spell);
    }

    private static void detonateSolarRootWeb(ServerLevel level, @Nullable LivingEntity caster, Vec3 center, SpellAssembly spell) {
        // Section 6.54 Siphoning Solar-Root Web (8m): grapples caught hostiles, roots 3.5s (70t),
        // shatters 6 armor points, and leeches 35% of damage dealt directly into party absorption shields!
        int tier = spell.getEffectiveTier();
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        SpellWorldVfxManager.broadcastRadialNova(level, center.add(0.0D, 0.25D, 0.0D), 8.0D, EssenceType.EMPYREAN, EssenceType.NATURE, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, center.add(0.0D, 0.04D, 0.0D), 8.0D, EssenceType.EMPYREAN, EssenceType.VITAE, 70, tier);

        float totalDealt = 0.0F;
        AABB web = new AABB(center.x - 8.0D, center.y - 3.0D, center.z - 8.0D, center.x + 8.0D, center.y + 5.0D, center.z + 8.0D);
        float webDmg = (7.4F + tier * 1.75F) * Math.max(0.5F, spell.flawProfile().getPotencyMultiplier());

        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, web, LivingEntity::isAlive)) {
            if (foe == caster || ApexEschatonSingularityHelper.isFriendly(foe, caster)) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, safeguard)) continue;
            double dx = center.x - foe.getX();
            double dz = center.z - foe.getZ();
            if (dx * dx + dz * dz > 68.0D) continue;

            // Grapple caught hostiles inward into the golden titan briars
            if (!SpellCombatReactionEngine.isBossEntity(level, foe) && (dx * dx + dz * dz) > 1.0D) {
                Vec3 pull = new Vec3(dx, 0.0D, dz).normalize().scale(0.65D);
                SpellExecutionEngine.applySyncedMotion(foe, new Vec3(pull.x, 0.14D, pull.z));
            }
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 6.0D, 0.0D, 180L);
            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 70, 6));
            foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 70, 0));
            totalDealt += SpellCombatReactionEngine.hurtWithEmpyreanMateriaAdvantage(
                    level, foe, caster, webDmg, false, true, false, true
            );
        }

        if (caster != null && totalDealt > 0.0F) {
            SpellLingeringZoneManager.grantPartyAbsorption(level, caster, totalDealt * 0.35F, 32.0F, 16.0D);
        }
        emitEmpyreanBurstParticles(level, center.add(0.0D, 0.6D, 0.0D), 36, 2.4D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.AZALEA_LEAVES_PLACE, SoundSource.PLAYERS, 1.0F, 0.85F);
        level.playSound(null, BlockPos.containing(center), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.95F, 1.25F);
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
        // Section 6.54 Sustained Root-Lance Sunbeam & Canopy Cataclysm:
        // Melts 2 armor/sec, continually siphons health & saturation into the party pool, and grounds aerial hostiles;
        // 5.0s (100t) continuous channeling (tracked on the CASTER!) triggers Cataclysmic World-Tree Canopy Cataclysm:
        // 12m knockback, -7 armor, purges hostile buffs, 5.0s (100t) root, & 10m golden azalea sanctuary healing allies 4 hearts!
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        long now = level.getGameTime();
        int tier = spell.getEffectiveTier();
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        boolean continuousPulse = heldTicks > 0 || apparatusPotency <= 0.45F;
        float tickFrac = continuousPulse ? 0.25F : 1.0F;

        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(
                level, caster, caster.getBoundingBox().expandTowards(dir.scale(maxReach)).inflate(1.35D), null, 0L
        );

        CompoundTag casterData = EntityHelper.getPersistentData(caster);
        long lastCast = casterData.getLong("EmpyreanCasterBeamLastTick").orElse(-100L);
        int channelTicks;
        if (lastCast == now) {
            channelTicks = casterData.getInt("EmpyreanCasterBeamTicks").orElse(0);
        } else {
            int step = heldTicks > 0
                    ? ((lastCast >= 0L && now - lastCast <= 28L) ? (int) Math.max(1L, Math.min(20L, now - lastCast)) : Math.min(heldTicks, 4))
                    : 0;
            channelTicks = heldTicks > 0
                    ? ((now - lastCast <= 28L) ? casterData.getInt("EmpyreanCasterBeamTicks").orElse(0) + step : step)
                    : 0;
            casterData.putLong("EmpyreanCasterBeamLastTick", now);
            casterData.putInt("EmpyreanCasterBeamTicks", channelTicks);
        }

        boolean siphonedThisPulse = false;
        LivingEntity primaryFoe = null;

        for (LivingEntity hit : hitEntities) {
            if (!hit.isAlive()) continue;
            if (ApexEschatonSingularityHelper.isFriendly(hit, caster)) {
                CompoundTag fData = EntityHelper.getPersistentData(hit);
                if (fData.getLong("EmpyreanBeamLastTick").orElse(-100L) == now) continue;
                fData.putLong("EmpyreanBeamLastTick", now);
                float directHeal = 4.0F * tickFrac;
                SpellCombatReactionEngine.notifyMagicalHeal(hit, directHeal);
                hit.heal(directHeal);
                if (hit instanceof Player p) {
                    p.getFoodData().eat(1, 0.35F * tickFrac);
                }
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, safeguard)) continue;
            if (primaryFoe == null) primaryFoe = hit;

            CompoundTag hData = EntityHelper.getPersistentData(hit);
            if (hData.getLong("EmpyreanBeamLastTick").orElse(-100L) == now) continue;
            hData.putLong("EmpyreanBeamLastTick", now);
            long foeLast = hData.getLong("EmpyreanFoeLastTick").orElse(-100L);
            int foeStep = heldTicks > 0
                    ? ((foeLast >= 0L && now - foeLast <= 28L) ? (int) Math.max(1L, Math.min(20L, now - foeLast)) : Math.min(heldTicks, 4))
                    : 0;
            int foeTicks = heldTicks > 0
                    ? ((now - foeLast <= 28L) ? hData.getInt("EmpyreanFoeTicks").orElse(0) + foeStep : foeStep)
                    : 0;
            hData.putLong("EmpyreanFoeLastTick", now);
            hData.putInt("EmpyreanFoeTicks", foeTicks);

            int meltedArmor = Math.min(10, Math.max(2, (foeTicks / 20) * 2));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, meltedArmor, 0.0D, 100L);

            // Ground aerial hostiles!
            hit.removeEffect(MobEffects.LEVITATION);
            if (!hit.onGround()) {
                hit.setDeltaMovement(hit.getDeltaMovement().x * 0.5D, -0.85D, hit.getDeltaMovement().z * 0.5D);
                hit.hasImpulse = true;
            }

            if (!slot.hasHarmVerb()) {
                float beamDmg = (4.6F + tier * 1.15F) * tickFrac;
                SpellCombatReactionEngine.hurtWithEmpyreanMateriaAdvantage(level, hit, caster, beamDmg, false, true, false, true);
            }

            if (!siphonedThisPulse) {
                siphonedThisPulse = true;
                float partyHeal = 3.0F * tickFrac;
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(16.0D), LivingEntity::isAlive)) {
                    if (ally == caster || ApexEschatonSingularityHelper.isFriendly(ally, caster)) {
                        SpellCombatReactionEngine.notifyMagicalHeal(ally, partyHeal);
                        ally.heal(partyHeal);
                        if (ally instanceof Player p && (!continuousPulse || (channelTicks % 20 == 0))) {
                            p.getFoodData().eat(1, 0.5F);
                        }
                    }
                }
                SpellLingeringZoneManager.grantPartyAbsorption(level, caster, 2.5F * tickFrac, 32.0F, 16.0D);
            }
        }

        // 5.0 continuous seconds (100 ticks) -> Cataclysmic World-Tree Canopy Cataclysm!
        if (heldTicks >= 100 && channelTicks >= 100 && primaryFoe != null) {
            casterData.putInt("EmpyreanCasterBeamTicks", 0);
            Vec3 epicenter = primaryFoe.position();
            SpellWorldVfxManager.broadcastRadialNova(level, epicenter.add(0.0D, 0.4D, 0.0D), 10.0D, EssenceType.EMPYREAN, EssenceType.VITAE, tier);
            SpellWorldVfxManager.broadcastArcaneSeal(level, epicenter.add(0.0D, 0.04D, 0.0D), 10.0D, EssenceType.EMPYREAN, EssenceType.NATURE, 100, tier);

            AABB cataclysmBox = new AABB(epicenter.x - 10.0D, epicenter.y - 3.0D, epicenter.z - 10.0D,
                    epicenter.x + 10.0D, epicenter.y + 6.0D, epicenter.z + 10.0D);
            float cataclysmDmg = 16.0F + tier * 2.85F;

            for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, cataclysmBox, LivingEntity::isAlive)) {
                if (ent == caster || ApexEschatonSingularityHelper.isFriendly(ent, caster)) {
                    SpellCombatReactionEngine.notifyMagicalHeal(ent, 8.0F); // 4 full hearts!
                    ent.heal(8.0F);
                    level.sendParticles(ParticleTypes.HEART, ent.getX(), ent.getY(1.0D), ent.getZ(), 4, 0.25D, 0.25D, 0.25D, 0.03D);
                    continue;
                }
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, safeguard)) continue;

                ApexEschatonSingularityHelper.purgeBeneficialBuffs(ent);
                SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, 7.0D, 0.0D, 220L);
                if (!SpellCombatReactionEngine.isBossEntity(level, ent)) {
                    Vec3 diff = ent.position().subtract(caster.position());
                    Vec3 push = (diff.lengthSqr() > 1.0E-4D ? new Vec3(diff.x, 0.0D, diff.z).normalize() : new Vec3(dir.x, 0.0D, dir.z)).scale(2.45D);
                    SpellExecutionEngine.applySyncedMotion(ent, new Vec3(push.x, 0.45D, push.z)); // 12m knockback!
                }
                ent.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 6)); // 5.0s root!
                ent.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 100, 0));
                SpellCombatReactionEngine.hurtWithEmpyreanMateriaAdvantage(level, ent, caster, cataclysmDmg, false, true, false, true);
            }

            // Ensure caster also receives the 4-heart sanctuary heal if standing > 10m back
            if (caster.position().distanceToSqr(epicenter) > 100.0D) {
                SpellCombatReactionEngine.notifyMagicalHeal(caster, 8.0F);
                caster.heal(8.0F);
            }

            // Bloom a 10-block sanctuary of golden/flowering azalea flowers!
            BlockPos cPos = BlockPos.containing(epicenter);
            for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-5, -2, -5), cPos.offset(5, 2, 5))) {
                if (cPos.distSqr(p) > 25.0D) continue;
                if (!level.getBlockState(p).isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, p, level.getBlockState(p))) continue;
                if (Blocks.FLOWERING_AZALEA.defaultBlockState().canSurvive(level, p) && level.random.nextFloat() < 0.22F) {
                    level.setBlockAndUpdate(p, Blocks.FLOWERING_AZALEA.defaultBlockState());
                }
            }

            emitEmpyreanBurstParticles(level, epicenter.add(0.0D, 0.8D, 0.0D), 48, 3.6D);
            level.playSound(null, cPos, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.15F, 1.25F);
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
        // Section 6.54 World-Tree Heartwood Bastion Nova:
        // Wall-penetrating 12-block concussive shockwave (ZERO physical light blocks!),
        // knocks hostiles 10m back flat, shatters 6 armor points, smites undead & dark entities (+60%),
        // roots survivors 4.0s (80t), and grants party Aegis of the Infinite Life (+12 Armor, +6 Toughness,
        // poison/wither/drowning/hunger immunity, and continuous party absorption for 16.0s / 320t)!
        int tier = spell.getEffectiveTier();
        double r = Math.max(12.0D, radius);
        double rSq = (r + 0.5D) * (r + 0.5D);
        long aegisUntil = level.getGameTime() + 320L;

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.EMPYREAN, EssenceType.NATURE, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, center.add(0.0D, 0.04D, 0.0D), r, EssenceType.EMPYREAN, EssenceType.VITAE, 80, tier);

        float novaDmg = (8.4F + tier * 1.90F) * Math.max(0.5F, apparatusPotency);
        AABB box = new AABB(center.x - r, center.y - 3.0D, center.z - r, center.x + r, center.y + 5.5D, center.z + r);

        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            double dx = ent.getX() - center.x;
            double dz = ent.getZ() - center.z;
            if (dx * dx + dz * dz > rSq) continue;

            if (ent == caster || ApexEschatonSingularityHelper.isFriendly(ent, caster)) {
                applyInfiniteLifeAegis(level, ent, aegisUntil);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, spell.hasWardFriendlyFireSafeguard())) continue;

            if (!SpellCombatReactionEngine.isBossEntity(level, ent)) {
                Vec3 horiz = new Vec3(dx, 0.0D, dz);
                Vec3 away = (horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : new Vec3(direction.x, 0.0D, direction.z)).scale(2.05D);
                SpellExecutionEngine.applySyncedMotion(ent, new Vec3(away.x, 0.35D, away.z)); // 10m flat knockback!
            }
            SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, 6.0D, 0.0D, 180L);
            ent.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 6)); // 4.0s root!
            ent.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 80, 0));

            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithEmpyreanMateriaAdvantage(
                        level, ent, caster, novaDmg, false, true, false, true
                );
            }
        }

        applyInfiniteLifeAegis(level, caster, aegisUntil);
        SpellLingeringZoneManager.grantPartyAbsorption(level, caster, 12.0F, 36.0F, 16.0D);
        emitEmpyreanBurstParticles(level, center.add(0.0D, 0.9D, 0.0D), 56, r * 0.42D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.1F, 1.18F);
    }

    private static void applyInfiniteLifeAegis(ServerLevel level, LivingEntity ally, long aegisUntil) {
        EntityHelper.getPersistentData(ally).putLong(SpellLingeringZoneManager.TAG_EMPYREAN_AEGIS_UNTIL, aegisUntil);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, level, 12.0D, 6.0D, 0.0D, 0.0D, 320L);
        ally.removeEffect(MobEffects.POISON);
        ally.removeEffect(MobEffects.WITHER);
        ally.removeEffect(MobEffects.HUNGER);
        ally.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 320, 0, false, true));
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
        // Section 6.54 Siphoning World-Tree Root-Cleave:
        // Wall-cleaving blade; 60% armor bypass, -6 armor, disables shields 6.0s (120t), roots victim 4.0s (80t),
        // and releases seeking golden seed tendrils wrapping adjacent enemies within 8m -> shared party absorption!
        int tier = spell.getEffectiveTier();
        boolean safeguard = spell.hasWardFriendlyFireSafeguard();
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D), normDir, 3.6D,
                EssenceType.EMPYREAN, EssenceType.NATURE, tier
        );

        float cleaveDmg = Math.max(5.8F + tier * 1.45F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.60F)
                * Math.max(0.5F, apparatusPotency) * spell.flawProfile().getPotencyMultiplier();

        for (int idx = 0; idx < hitEntities.size(); idx++) {
            LivingEntity hit = hitEntities.get(idx);
            if (!hit.isAlive() || hit == caster) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, safeguard)) continue;

            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 6.0D, 0.0D, 180L);
            ApexEschatonSingularityHelper.disableShield(hit, 120); // 6.0s shield disable!
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 6)); // 4.0s golden heartwood root!
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 80, 0));

            float dealt = 4.0F;
            if (!slot.hasHarmVerb()) {
                dealt = SpellCombatReactionEngine.hurtWithEmpyreanMateriaAdvantage(
                        level, hit, caster, cleaveDmg * (idx == 0 ? 1.0F : 0.55F), true, true, false, true
                );
            }

            float siphoned = Math.max(3.0F, dealt * 0.40F);
            for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, hit.getBoundingBox().inflate(8.0D),
                    e -> e.isAlive() && e != hit && e != caster && !ApexEschatonSingularityHelper.isFriendly(e, caster))) {
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(adj, caster, safeguard)) continue;
                SpellWorldVfxManager.broadcastChainArc(
                        level, hit.getEyePosition().subtract(0.0D, 0.2D, 0.0D),
                        adj.getEyePosition().subtract(0.0D, 0.2D, 0.0D),
                        EssenceType.EMPYREAN, EssenceType.VITAE, tier
                );
                adj.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 5));
                adj.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0));
                float adjDmg = SpellCombatReactionEngine.hurtWithEmpyreanMateriaAdvantage(
                        level, adj, caster, 4.5F + tier * 0.95F, false, true, false, false
                );
                siphoned += Math.max(2.0F, adjDmg * 0.45F);
            }
            SpellLingeringZoneManager.grantPartyAbsorption(level, caster, siphoned, 36.0F, 16.0D);
            emitEmpyreanBurstParticles(level, hit.position().add(0.0D, hit.getBbHeight() * 0.5D, 0.0D), 18, 0.55D);
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
        // Section 6.54 Living Titan Bark & Canopy Mantle:
        // +12 Armor, +6 Toughness, suffocation/drowning/poison/wither/hunger immunity, -85% mob detection,
        // 5m 100% arrow/trident petal vaporization, & 6m root-web aura draining 2.0 hearts/s true damage -> party absorption!
        int tier = spell.getEffectiveTier();
        int dur = 300 + tier * 45;
        long now = level.getGameTime();
        double auraRad = Math.max(6.0D, radius);

        if (caster.isShiftKeyDown()) {
            Vec3 anchor = caster.position();
            SpellWorldVfxManager.broadcastWardShell(level, anchor.add(0.0D, 1.1D, 0.0D), auraRad, EssenceType.EMPYREAN, EssenceType.NATURE, dur, tier);
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, anchor, auraRad, dur, now, spell, slot, EssenceType.EMPYREAN, apparatusPotency,
                    (srv, zone, elapsed) -> {
                        if (elapsed > zone.maxDurationTicks) return false;
                        LivingEntity owner = zone.resolveCaster(srv);
                        long zNow = srv.getGameTime();
                        AABB box = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 4.0D, zone.center.z + zone.radius
                        );
                        float siphoned = 0.0F;
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                            if (ent == owner || (owner != null && ApexEschatonSingularityHelper.isFriendly(ent, owner))) {
                                EntityHelper.getPersistentData(ent).putLong(SpellLingeringZoneManager.TAG_EMPYREAN_SHROUD_UNTIL, zNow + 16L);
                                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ent, srv, 12.0D, 6.0D, 0.0D, 0.0D, 25L);
                                ent.removeEffect(MobEffects.POISON);
                                ent.removeEffect(MobEffects.WITHER);
                                ent.removeEffect(MobEffects.HUNGER);
                                ent.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 30, 0, true, false));
                            } else if (elapsed % 20 == 0
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                            ent, owner, zone.spell.hasWardFriendlyFireSafeguard()))
                                    && (ent instanceof Monster || (owner != null && ent == owner.getLastHurtMob()))) {
                                float dealt = SpellCombatReactionEngine.hurtWithEmpyreanMateriaAdvantage(
                                        srv, ent, owner, 4.0F, false, false, true, false
                                );
                                siphoned += Math.max(4.0F, dealt);
                            }
                        }
                        if (siphoned > 0.0F && owner != null) {
                            SpellLingeringZoneManager.grantPartyAbsorption(srv, owner, siphoned, 36.0F, 16.0D);
                        }
                        return true;
                    }
            ));
        } else {
            EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_EMPYREAN_SHROUD_UNTIL, now + dur);
            SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 12.0D, 6.0D, 0.0D, 0.0D, dur);
            caster.removeEffect(MobEffects.POISON);
            caster.removeEffect(MobEffects.WITHER);
            caster.removeEffect(MobEffects.HUNGER);
            caster.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, dur, 0, false, true));
            SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.85F, null);
        }

        emitEmpyreanBurstParticles(level, caster.position().add(0.0D, 1.0D, 0.0D), 28, 0.60D);
        level.playSound(null, caster.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.90F, 1.18F);
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
        // Section 6.54 World-Tree Siphoning Root-Vault Sigil:
        // Disarmable Single/Repeating mandala glyph on any floor, wall, or ceiling;
        // Ally step: instantly restores 10 full hearts (20.0 HP), cures all negative curses, & grants +8 Absorption hearts (16.0 HP) for 45.0s (900t);
        // Hostile approach within 5m: erupts into an indestructible 8m cage of golden titan roots trapping mobs inside for 5.0s (100t),
        // shattering 6 armor points, stripping positive mob buffs, & leeching 40% of total health into floating golden healing dewdrops!
        final int tier = spell.getEffectiveTier();
        final int maxWaitTicks = tier >= 5 ? 72_000_000 : 12_000;
        String subMode = slot.primaryEffect().subMode();
        final boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        final int[] cooldown = {0};

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, Math.max(5.0D, radius), maxWaitTicks, level.getGameTime(), spell, slot, EssenceType.EMPYREAN, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 12 == 0) {
                        int col = EMPYREAN_PALETTE[(elapsed / 12) % EMPYREAN_PALETTE.length];
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), col, 20, 0.36F, 0.03F),
                                zone.center.x, zone.center.y + 0.14D, zone.center.z, 6, 1.8D, 0.08D, 1.8D, 0.01D
                        );
                    }
                    if (elapsed < 20) return true;
                    if (cooldown[0] > 0) {
                        cooldown[0]--;
                        return true;
                    }

                    LivingEntity owner = zone.resolveCaster(srv);
                    // 1. Check ally contact on the sigil first (2.25m contact pad; caster only triggers if wounded or cursed so placement doesn't self-consume!)
                    AABB allyPad = new AABB(
                            zone.center.x - 2.25D, zone.center.y - 1.5D, zone.center.z - 2.25D,
                            zone.center.x + 2.25D, zone.center.y + 2.5D, zone.center.z + 2.25D
                    );
                    for (LivingEntity st : srv.getEntitiesOfClass(LivingEntity.class, allyPad, LivingEntity::isAlive)) {
                        boolean isAlly = (st == owner) || (owner != null && ApexEschatonSingularityHelper.isFriendly(st, owner));
                        if (!isAlly) continue;
                        if (st == owner && st.getHealth() >= st.getMaxHealth() - 0.5F && st.getActiveEffects().stream().allMatch(e -> e.getEffect().value().isBeneficial())) {
                            continue;
                        }
                        SpellCombatReactionEngine.purifyHexesAndNegativeEffects(st);
                        for (EquipmentSlot eq : EquipmentSlot.values()) {
                            PenumbraSpellMutationHandler.purgeCursesFromStack(st.getItemBySlot(eq));
                        }
                        SpellCombatReactionEngine.notifyMagicalHeal(st, 20.0F); // 10 full hearts!
                        st.heal(20.0F);
                        st.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 900, 3)); // +8 Absorption hearts (16 HP) for 45.0s!
                        st.setAbsorptionAmount(Math.max(st.getAbsorptionAmount(), 16.0F));
                        emitEmpyreanBurstParticles(srv, st.position().add(0.0D, 1.0D, 0.0D), 28, 0.65D);
                        srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0F, 1.35F);
                        if (repeating) {
                            cooldown[0] = 120;
                            return true;
                        }
                        return false;
                    }

                    // 2. Check hostile approach within 5 blocks!
                    AABB hostileTrig = new AABB(
                            zone.center.x - 5.0D, zone.center.y - 2.0D, zone.center.z - 5.0D,
                            zone.center.x + 5.0D, zone.center.y + 3.5D, zone.center.z + 5.0D
                    );
                    List<LivingEntity> hostiles = srv.getEntitiesOfClass(
                            LivingEntity.class,
                            hostileTrig,
                            e -> e.isAlive()
                                    && e != owner
                                    && (owner == null || !ApexEschatonSingularityHelper.isFriendly(e, owner))
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                                            e, owner, zone.spell.hasWardFriendlyFireSafeguard()))
                    );
                    if (hostiles.isEmpty()) return true;

                    // Erupt an 8-block cage of golden titan roots for 5.0s (100t)!
                    SpellWorldVfxManager.broadcastWardShell(
                            srv, zone.center.add(0.0D, 1.5D, 0.0D), 8.0D, EssenceType.EMPYREAN, EssenceType.NATURE, 100, tier
                    );
                    SpellWorldVfxManager.broadcastArcaneSeal(
                            srv, zone.center.add(0.0D, 0.04D, 0.0D), 8.0D, EssenceType.EMPYREAN, EssenceType.VITAE, 100, tier
                    );

                    Map<BlockPos, BlockState> savedRootBlocks = new HashMap<>();
                    BlockState rootWood = ModBlocks.MATERIA_ECHO_WOOD.get().defaultBlockState();
                    BlockPos cPos = BlockPos.containing(zone.center);
                    for (int rib = 0; rib < 12; rib++) {
                        double ang = Math.PI * 2.0D * rib / 12.0D;
                        int rx = cPos.getX() + (int) Math.round(Math.cos(ang) * 7.5D);
                        int rz = cPos.getZ() + (int) Math.round(Math.sin(ang) * 7.5D);
                        for (int dy = 0; dy <= 2; dy++) {
                            BlockPos rp = new BlockPos(rx, cPos.getY() + dy, rz);
                            BlockState cur = srv.getBlockState(rp);
                            if ((cur.isAir() || cur.canBeReplaced())
                                    && !savedRootBlocks.containsKey(rp)
                                    && !SpellExecutionEngine.isProtectedBlockOrContainer(srv, rp, cur)) {
                                savedRootBlocks.put(rp.immutable(), cur);
                                srv.setBlockAndUpdate(rp, rootWood);
                            }
                        }
                    }

                    AABB cageBox = new AABB(
                            zone.center.x - 8.0D, zone.center.y - 2.5D, zone.center.z - 8.0D,
                            zone.center.x + 8.0D, zone.center.y + 5.0D, zone.center.z + 8.0D
                    );
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, cageBox, e -> e.isAlive() && e != owner)) {
                        if (owner != null && ApexEschatonSingularityHelper.isFriendly(foe, owner)) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;

                        ApexEschatonSingularityHelper.purgeBeneficialBuffs(foe);
                        SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 6.0D, 0.0D, 200L);
                        foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 6));
                        foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 100, 0));

                        // Leech 40% of total health (capped safely on bosses) into floating golden dewdrops!
                        boolean boss = SpellCombatReactionEngine.isBossEntity(srv, foe);
                        float leechRaw = boss ? Math.min(30.0F, foe.getMaxHealth() * 0.12F) : Math.min(48.0F, foe.getMaxHealth() * 0.40F);
                        float dealt = SpellCombatReactionEngine.hurtWithEmpyreanMateriaAdvantage(
                                srv, foe, owner, leechRaw, false, false, true, false
                        );
                        SpellLingeringZoneManager.spawnCollectableHealingOrb(
                                srv, foe.position(), owner, Math.max(4.0F, dealt * 0.65F), true
                        );
                        SpellExecutionEngine.deliverSubSpellOnEntity(
                                srv, owner, foe, Vec3.ZERO, zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency
                        );
                    }

                    // Keep all entering mobs trapped inside the indestructible 8m golden root cage for 5.0s (100t), then restore terrain!
                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 8.0D, 100, srv.getGameTime(), null, null, EssenceType.EMPYREAN, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl < 100) {
                                    LivingEntity cOwner = sZone.resolveCaster(sLvl);
                                    for (LivingEntity mob : sLvl.getEntitiesOfClass(LivingEntity.class, cageBox.inflate(1.0D), LivingEntity::isAlive)) {
                                        if (mob == cOwner || (cOwner != null && ApexEschatonSingularityHelper.isFriendly(mob, cOwner))) continue;
                                        double dx = mob.getX() - sZone.center.x;
                                        double dz = mob.getZ() - sZone.center.z;
                                        if (dx * dx + dz * dz > 49.0D) {
                                            Vec3 inward = new Vec3(-dx, 0.0D, -dz).normalize().scale(0.55D);
                                            mob.setDeltaMovement(inward.x, 0.05D, inward.z);
                                            mob.hasImpulse = true;
                                        } else {
                                            mob.setDeltaMovement(mob.getDeltaMovement().x * 0.15D, Math.min(0.0D, mob.getDeltaMovement().y), mob.getDeltaMovement().z * 0.15D);
                                        }
                                    }
                                    return true;
                                }
                                for (Map.Entry<BlockPos, BlockState> entry : savedRootBlocks.entrySet()) {
                                    if (sLvl.getBlockState(entry.getKey()).is(ModBlocks.MATERIA_ECHO_WOOD.get())) {
                                        sLvl.setBlockAndUpdate(entry.getKey(), entry.getValue());
                                    }
                                }
                                return false;
                            }
                    ));

                    srv.playSound(null, cPos, SoundEvents.WOOD_PLACE, SoundSource.PLAYERS, 1.1F, 0.78F);
                    if (repeating) {
                        cooldown[0] = 140;
                        return true;
                    }
                    return false;
                }
        ).markDisarmableGlyph().withOnDisarm((srv, z) ->
                emitEmpyreanBurstParticles(srv, z.center.add(0.0D, 0.3D, 0.0D), 14, 0.45D)));

        level.playSound(null, BlockPos.containing(glyphCenter), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.85F, 1.25F);
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
        // Section 6.54 Solar-Verdant Spore-Web Deluge:
        // Wall-penetrating cone; shatters 6 armor points, vaporizes all incoming enemy projectiles,
        // roots hostiles in place for 3.0s (60t), and inflicts Empyrean Mesmerization
        // (disrupting mob AI pathfinding AND reducing movement speed by 60% for 30.0s / 600t)!
        int tier = spell.getEffectiveTier();
        double reach = Math.max(12.0D, coneRange);
        Vec3 dir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 origin = caster.getEyePosition();

        SpellWorldVfxManager.broadcastVortexCone(
                level, origin.subtract(0.0D, 0.2D, 0.0D), dir, reach, EssenceType.EMPYREAN, EssenceType.NATURE, tier
        );

        AABB coneBox = caster.getBoundingBox().expandTowards(dir.scale(reach)).inflate(reach * 0.55D);
        ApexEschatonSingularityHelper.vaporizeHostileProjectiles(level, caster, coneBox, null, 0L);

        float delugeDmg = (6.0F + tier * 1.45F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitTargets) {
            if (!hit.isAlive() || ApexEschatonSingularityHelper.isFriendly(hit, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;

            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 6.0D, 0.0D, 200L);
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0)); // 3.0s root!
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 600, 3)); // Slowness IV = exact -60% movement speed for 30.0s!
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 600); // 30.0s Empyrean Mesmerization!

            if (hit instanceof Mob mob) {
                mob.setTarget(null);
                mob.setAggressive(false);
                if (mob instanceof PathfinderMob pf) {
                    double rAng = level.random.nextDouble() * Math.PI * 2.0D;
                    pf.getNavigation().moveTo(pf.getX() + Math.cos(rAng) * 5.0D, pf.getY(), pf.getZ() + Math.sin(rAng) * 5.0D, 0.65D);
                }
            }
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithEmpyreanMateriaAdvantage(level, hit, caster, delugeDmg, false, true, false, true);
            }
        }

        for (int s = 2; s <= 10; s += 4) {
            emitEmpyreanBurstParticles(level, origin.add(dir.scale(s)), 14, s * 0.20D);
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
        // Section 6.54 Cataclysmic World-Tree Canopy Skyfall [24.0s Bombardment + 12.0s Consecrated Soil, 16m AOE]:
        // Drops 8 colossal 3D Skyfall Pseudoblock golden heartwood megaliths that permanently materialize upon completion;
        // crushes hostiles, knocks them flat, shatters 7 armor points, smites dark entities, and leaves a 12.0s zone of
        // World-Tree Consecrated Soil (-70% hostile speed, roots every 4.0s, and heals allies +2 hearts/sec + Saturation + +6 Armor)!
        final int tier = spell.getEffectiveTier();
        final double r = Math.max(16.0D, radius);
        final BlockPos centerPos = BlockPos.containing(impactCenter);
        final double skyH = SpellDeliveryForm.probeCaveSafeSkyfallHeight(level, centerPos, tier);

        SpellWorldVfxManager.broadcastSkyfallPillar(level, impactCenter, r, EssenceType.EMPYREAN, EssenceType.NATURE, 720, tier);
        SpellWorldVfxManager.broadcastArcaneSeal(level, impactCenter.add(0.0D, 0.04D, 0.0D), r, EssenceType.EMPYREAN, EssenceType.VITAE, 720, tier);
        level.playSound(null, centerPos, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 1.15F);

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
                caster, impactCenter, r, 720, level.getGameTime(), spell, slot, EssenceType.EMPYREAN, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed >= zone.maxDurationTicks) {
                        if (!materialized[0]) {
                            materialized[0] = true;
                            for (int mIdx = 0; mIdx < megalithCount; mIdx++) {
                                BlockState pseudoState = resolveHeartwoodMegalithBlockState(mIdx);
                                int colHeight = (mIdx % 2 == 0) ? 2 : 1;
                                SpellWorldVfxManager.materializeSkyfallPseudoblockColumn(srv, megalithTargets[mIdx], pseudoState, colHeight);
                            }
                        }
                        return false;
                    }

                    boolean storm = elapsed < 480; // 24.0s active bombardment, then 12.0s Consecrated Soil!
                    int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;

                    if (storm) {
                        for (int mIdx = 0; mIdx < megalithCount; mIdx++) {
                            int impactTick = 22 + mIdx * 56;
                            BlockPos strikePos = megalithTargets[mIdx];
                            BlockState pseudoState = resolveHeartwoodMegalithBlockState(mIdx);
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
                                        EssenceType.EMPYREAN,
                                        zTier
                                );
                            } else if (elapsed == impactTick) {
                                landed[mIdx] = true;
                                Vec3 hitVec = new Vec3(strikePos.getX() + 0.5D, strikePos.getY(), strikePos.getZ() + 0.5D);
                                SpellWorldVfxManager.broadcastProjectileImpact(
                                        srv, hitVec.add(0.0D, 0.25D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                                        EssenceType.EMPYREAN, EssenceType.NATURE, 2.75F, zTier
                                );
                                srv.sendParticles(
                                        new BlockParticleOption(ParticleTypes.BLOCK, pseudoState),
                                        hitVec.x, hitVec.y + 0.6D, hitVec.z, 24, 1.1D, 0.5D, 1.1D, 0.09D
                                );
                                srv.playSound(null, strikePos, SoundEvents.WOOD_PLACE, SoundSource.PLAYERS, 1.05F, 0.72F);
                            }
                        }

                        if (elapsed % 8 == 0) {
                            double ang = srv.random.nextDouble() * Math.PI * 2.0D;
                            double dist = Math.sqrt(srv.random.nextDouble()) * (zone.radius * 0.88D);
                            Vec3 seedImpact = zone.center.add(Math.cos(ang) * dist, 0.25D, Math.sin(ang) * dist);
                            SpellWorldVfxManager.broadcastProjectileImpact(
                                    srv, seedImpact, new Vec3(0.0D, 1.0D, 0.0D),
                                    EssenceType.EMPYREAN, EssenceType.VITAE, 1.65F, zTier
                            );
                        }
                    }

                    for (int mIdx = 0; mIdx < megalithCount; mIdx++) {
                        if (landed[mIdx]) {
                            SpellWorldVfxManager.repelEntitiesFromPseudoblockColumn(srv, megalithTargets[mIdx], (mIdx % 2 == 0) ? 2 : 1, false);
                        }
                    }

                    if (elapsed % 8 == 0) {
                        int col = EMPYREAN_PALETTE[(elapsed / 8) % EMPYREAN_PALETTE.length];
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), col, 24, 0.95F, 0.05F),
                                zone.center.x, zone.center.y + (storm ? 5.2D : 0.45D), zone.center.z,
                                26, zone.radius * 0.55D, storm ? 2.5D : 0.35D, zone.radius * 0.55D, 0.04D
                        );
                    }

                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        double rSq = (zone.radius + 0.5D) * (zone.radius + 0.5D);
                        float canopyDmg = (5.8F + zTier * 1.40F) * Math.max(0.5F, zone.apparatusPotency);
                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 8.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 14.0D, zone.center.z + zone.radius
                        );
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            double dx = ent.getX() - zone.center.x;
                            double dz = ent.getZ() - zone.center.z;
                            if (dx * dx + dz * dz > rSq) continue;

                            if (ent == owner || (owner != null && ApexEschatonSingularityHelper.isFriendly(ent, owner))) {
                                // +2 hearts/sec (4.0 HP/s) ally healing + Saturation + +6 Armor!
                                SpellCombatReactionEngine.notifyMagicalHeal(ent, 4.0F);
                                ent.heal(4.0F);
                                ent.addEffect(new MobEffectInstance(MobEffects.SATURATION, 30, 0, true, false));
                                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ent, srv, 6.0D, 0.0D, 0.0D, 0.0D, 35L);
                                continue;
                            }
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, owner, wardSafe)) continue;

                            if (storm) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(ent, srv, 7.0D, 0.0D, 200L);
                                ent.setDeltaMovement(ent.getDeltaMovement().x * 0.25D, -0.52D, ent.getDeltaMovement().z * 0.25D);
                                ent.hasImpulse = true;
                                if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                                    SpellCombatReactionEngine.hurtWithEmpyreanMateriaAdvantage(
                                            srv, ent, owner, canopyDmg, false, true, false, true
                                    );
                                }
                                if (zone.spell != null && zone.slot != null) {
                                    SpellExecutionEngine.deliverSubSpellOnEntity(
                                            srv, owner, ent, new Vec3(0.0D, -1.0D, 0.0D),
                                            zone.spell, zone.slot, EssenceType.EMPYREAN, zone.apparatusPotency * 0.55F
                                    );
                                }
                            } else {
                                // 12.0s World-Tree Consecrated Soil: exact -70% hostile speed & roots every 4.0s (80t)!
                                SpellCombatReactionEngine.applyTimedMovementSpeedSap(ent, srv, 0.70D, 35L);
                                if ((elapsed - 480) % 80 == 0) {
                                    ent.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 45, 0));
                                }
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
        tag.putString("SpellEidolonMateria", EssenceType.EMPYREAN.name());
        tag.putInt("SpellEidolonTintRGB", EMPYREAN_SOLAR_GOLD_HEX_COLOR);
        SpellWorldVfxManager.broadcastSummonPillar(
                level, spawnPos, Math.max(1.8D, radius), EssenceType.EMPYREAN, EssenceType.NATURE, spell.getEffectiveTier()
        );
        emitEmpyreanBurstParticles(level, spawnPos.add(0.0D, 1.1D, 0.0D), 32, 0.60D);
        level.playSound(null, BlockPos.containing(spawnPos), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.95F, 1.25F);
    }

    // =========================================================================
    // PART 3: CORE EFFECT MUTATIONS — BLINK, TRANSMUTE & AFFLICTION
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Section 6.54 Solar Slipstream Ascent [22m through ceilings]:
        // Departure 6m radial blast of golden roots knocking hostiles back & rooting 3.0s (60t);
        // Arrival grants 10.0s (200t) unrestricted 3D arcane flight, Slow Falling & fall immunity,
        // drops healing dewdrops onto the ground below, and sheds 100% of mob aggro!
        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.EMPYREAN, potencyTier);
        SpellWorldVfxManager.broadcastRadialNova(level, departurePos.add(0.0D, 0.2D, 0.0D), 6.0D, EssenceType.EMPYREAN, EssenceType.NATURE, potencyTier);

        AABB dep = new AABB(
                departurePos.x - 6.0D, departurePos.y - 2.5D, departurePos.z - 6.0D,
                departurePos.x + 6.0D, departurePos.y + 4.0D, departurePos.z + 6.0D
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, dep, e -> e.isAlive() && e != caster)) {
            if (ApexEschatonSingularityHelper.isFriendly(foe, caster)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
            Vec3 diff = foe.position().subtract(departurePos);
            Vec3 away = (diff.lengthSqr() > 1.0E-4D ? new Vec3(diff.x, 0.0D, diff.z).normalize() : new Vec3(0.0D, 0.0D, 1.0D)).scale(1.45D);
            SpellExecutionEngine.applySyncedMotion(foe, new Vec3(away.x, 0.35D, away.z));
            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 6)); // 3.0s golden root!
            foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0));
            SpellCombatReactionEngine.hurtWithEmpyreanMateriaAdvantage(level, foe, caster, 5.0F + potencyTier, false, true, false, false);
        }

        SpellLingeringZoneManager.grantArcane3DFlight(caster, 200); // 10.0s unrestricted 3D flight!
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 220, 0));
        caster.resetFallDistance();

        // Drop healing golden dewdrops onto the ground directly below the arrival point!
        BlockPos groundBelow = BlockPos.containing(arrivalPos);
        for (int dy = 0; dy < 24; dy++) {
            BlockPos check = groundBelow.below(dy);
            if (!level.getBlockState(check).isAir()) {
                groundBelow = check.above();
                break;
            }
        }
        Vec3 dewCenter = new Vec3(groundBelow.getX() + 0.5D, groundBelow.getY() + 0.1D, groundBelow.getZ() + 0.5D);
        SpellLingeringZoneManager.spawnCollectableHealingOrb(level, dewCenter, caster, 6.0F, true);
        SpellLingeringZoneManager.spawnCollectableHealingOrb(level, dewCenter.add(0.85D, 0.0D, -0.65D), caster, 6.0F, true);

        ApexEschatonSingularityHelper.shedMobAggro(level, caster, arrivalPos, 28.0D);
        emitEmpyreanBurstParticles(level, arrivalPos.add(0.0D, 0.9D, 0.0D), 24, 0.50D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.90F, 1.35F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Section 6.54 World-Tree Genesis & Vitality Consecration [5.0s Channel, strictly NO air transmutation]:
        // Transmutes Stone, Deepslate, & Netherrack into Sacred World-Tree Heartwood, Flowering Azalea Moss, & Sacred Loam;
        // purges worn curses + 50% +1 enchant tier; instantly cures Zombie Villagers w/ max discounts;
        // converts standing water within 3m into Dew of the World-Tree (Regeneration III, Saturation, +10 Absorption hearts)!
        if (state.isAir()) return false;

        if (ApexEschatonSingularityHelper.beginOrExecuteChanneledTransmute(
                level, caster, pos, 12, potencyTier, EssenceType.EMPYREAN,
                "World-Tree Genesis & Vitality Consecration",
                () -> {
                    ApexEschatonSingularityHelper.cureZombieVillagersWithMaxDiscounts(level, caster, pos, 12.0D);
                    if (caster != null) {
                        ApexEschatonSingularityHelper.purgeCursesAndElevateEquipment(caster, 1, 0.50F, 1);
                    }
                    if (!ApexEschatonSingularityHelper.tryBottleStandingWaterElixir(level, caster, pos, 3, EssenceType.EMPYREAN)
                            && caster != null) {
                        ApexEschatonSingularityHelper.tryBottleStandingWaterElixir(level, caster, caster.blockPosition(), 3, EssenceType.EMPYREAN);
                    }
                })) {
            return true;
        }

        if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE)
                || state.is(Blocks.GRANITE) || state.is(Blocks.DIORITE) || state.is(Blocks.ANDESITE)
                || state.is(Blocks.DEEPSLATE) || state.is(Blocks.COBBLED_DEEPSLATE)
                || state.is(Blocks.NETHERRACK)) {
            int pick = level.random.nextInt(3);
            BlockState out = switch (pick) {
                case 0 -> ModBlocks.MATERIA_ECHO_WOOD.get().defaultBlockState(); // Sacred World-Tree Heartwood
                case 1 -> Blocks.MOSS_BLOCK.defaultBlockState();                 // Flowering Azalea Moss
                default -> Blocks.PODZOL.defaultBlockState();                    // Sacred Loam
            };
            level.setBlockAndUpdate(pos, out);
            if (pick == 1 && level.getBlockState(pos.above()).isAir() && level.random.nextFloat() < 0.45F) {
                level.setBlockAndUpdate(pos.above(), Blocks.FLOWERING_AZALEA.defaultBlockState());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        if (ApexEschatonSingularityHelper.purgeCursesAndElevateStack(stack, level.random, 1, 0.50F, 1)) {
            itemEntity.setItem(stack.copy());
            emitEmpyreanBurstParticles(level, itemEntity.position().add(0.0D, 0.3D, 0.0D), 8, 0.25D);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.54 Radiant Empyrean Brand Hex:
        // Milk-immune holy life-fire brand; shatters 50% Armor Toughness, disables all natural & potion recovery,
        // inflicts 50% fumble/miss chance, and deals continuous TRUE holy burn damage (doubled 2x vs Undead/Demonic/Abyssal)!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.90D, 0.50D * mult), 45L);
        target.removeEffect(MobEffects.REGENERATION);
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 45, 0, true, true));

        LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCasterPublic(level, target);
        float burnDmg = (2.60F + potencyTier * 0.68F) * mult;
        Set<EssenceType> affs = SpellCombatReactionEngine.resolveEntityAffinities(target);
        if (ApexEschatonSingularityHelper.isDarkUndeadOrAbyssal(target)
                || target.getType().is(EntityTypeTags.UNDEAD)
                || affs.contains(EssenceType.UNDEAD)
                || affs.contains(EssenceType.VOID)
                || affs.contains(EssenceType.UMBRAL)
                || affs.contains(EssenceType.NETHER)
                || affs.contains(EssenceType.OBLIVION)) {
            burnDmg *= 2.0F; // Doubled against Undead, Demonic, and Abyssal entities!
        }

        SpellCombatReactionEngine.hurtWithEmpyreanMateriaAdvantage(
                level, target, hexCaster, burnDmg, false, false, true, false
        );
        emitEmpyreanBurstParticles(level, target.position().add(0.0D, target.getBbHeight() * 0.6D, 0.0D), 6, 0.30D);
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
            float bonusHeal = 6.0F + potencyTier * 1.50F;
            SpellCombatReactionEngine.notifyMagicalHeal(target, bonusHeal);
            target.heal(bonusHeal);
            target.setAbsorptionAmount(Math.max(target.getAbsorptionAmount(), 8.0F));
            emitEmpyreanBurstParticles(level, target.position().add(0.0D, 0.9D, 0.0D), 14, 0.38D);
        }
    }

    // =========================================================================
    // INTERNAL HELPERS
    // =========================================================================

    private static BlockState resolveHeartwoodMegalithBlockState(int index) {
        return switch (index & 3) {
            case 0 -> ModBlocks.MATERIA_ECHO_WOOD.get().defaultBlockState();
            case 1 -> Blocks.MOSS_BLOCK.defaultBlockState();
            case 2 -> Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState();
            default -> Blocks.PODZOL.defaultBlockState();
        };
    }

    private static void emitEmpyreanBurstParticles(ServerLevel level, Vec3 center, int totalCount, double spread) {
        int perColor = Math.max(1, totalCount / EMPYREAN_PALETTE.length);
        for (int col : EMPYREAN_PALETTE) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_LEAF.get(), col, 22, 0.85F, 0.05F),
                    center.x, center.y, center.z, perColor, spread, spread * 0.60D, spread, 0.04D
            );
        }
        level.sendParticles(ParticleTypes.CHERRY_LEAVES, center.x, center.y, center.z, Math.max(4, totalCount / 3),
                spread, spread * 0.60D, spread, 0.03D);
    }
}
