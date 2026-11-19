package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.registry.ModItems;
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
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Penumbra Materia (Tier 3 Trinity Fusion #40)</b>
 * (Spellcrafting System Guide Section 6.41 — Trinity Radiant + Void + Umbral harmonic matrix):
 * <ul>
 *     <li><b>Palette</b>: Blazing Solar Corona Gold ({@code #FFEA78}) + Deep Twilight Indigo ({@code #281E3C})</li>
 *     <li><b>Advantage</b>: Strong vs <b>AIR (+1)</b>, <b>RADIANT (+1)</b>, <b>NATURE (+1)</b>, <b>CELESTIAL (+1)</b>,
 *         and <b>AEGIS (+1)</b> (or <b>UNDEAD</b> under Radiant Dominance) | Weak vs <b>EARTH (-1)</b>, <b>VOID (-1)</b>, and <b>UNDEAD (-1)</b></li>
 * </ul>
 */
public final class PenumbraSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int PENUMBRA_CORONA_GOLD_HEX_COLOR = 0xFFEA78;
    public static final int PENUMBRA_TWILIGHT_HEX_COLOR = 0x281E3C;

    private static final String TAG_PENUMBRA_BEAM_LAST_TICK = "PenumbraBeamLastTick";
    private static final String TAG_PENUMBRA_BEAM_TICKS = "PenumbraBeamContactTicks";
    private static final String TAG_PENUMBRA_TRANSMUTE_BATCH_TICK = "PenumbraTransmuteBatchTick";

    @Override
    public EssenceType materiaType() {
        return EssenceType.PENUMBRA;
    }

    // =========================================================================
    // TITLES & CODEX / TOOLTIP DESCRIPTIONS
    // =========================================================================

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Penumbral Corona-Umbra Binary Orbit Flow";
    }

    @Override
    public String resolveFlowDescription(SpellFlowModifier flow) {
        return "4 oscillating twin-spheres of radiant corona gold (#FFEA78) and deep indigo twilight (#281E3C) grant +5 Armor, +2 Toughness, Blindness/Darkness immunity, arrow dissolution, contact Penumbra burn, and 3s-blinding homing darts on melee or spell hit.";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Penumbral Twilight Eclipse-Bolt";
            case BEAM -> "Penumbral Event-Lensing Ray";
            case RADIAL_BURST -> "Twilight Corona Nova";
            case TOUCH_STRIKE -> "Penumbral Eclipse Cleave";
            case AURA -> "Penumbral Corona Mantle";
            case GLYPH -> "Penumbral Twilight Trap Glyph";
            case CONE -> "Penumbral Corona-Twilight Fan Deluge";
            case SKYFALL -> "Penumbral Cataclysmic Eclipse Tempest";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE ->
                    "Hurls a 3D wall-phasing dual-core corona-twilight missile (#FFEA78 / #281E3C) that detonates into a bifurcation blast, dealing Penumbra Materia damage, blinding foes for 3.5s, and dragging mobs within 3m inward.";
            case BEAM ->
                    "Channels an optical-gravitational twilight ray warping victim vision between Blindness and Darkness; 5.0s sustained channeling triggers Penumbral Convergence Flash for massive burst damage and a 4m inward pull.";
            case RADIAL_BURST ->
                    "6m+ zero-light-block coronal twilight shockwave knocking enemies 6m back, inflicting 4.0s Blindness & Glowing, smiting undead, and cloaking caster & allies in +20% Twilight Shimmer dodge for 6.0s.";
            case TOUCH_STRIKE ->
                    "Armor-bypassing scythe cleave stacking weapon Materia Advantage, blinding primary targets for 3.0s, and leaving a 4.0s twilight distortion crescent slowing foes by 35%.";
            case AURA ->
                    "Envelops caster (or anchored area) in coronal flares & twilight shadow (+6 Armor, +2 Toughness, Blindness/Darkness immunity, 25% projectile void-refraction, and 30% attacker melee accuracy penalty for 3.0s).";
            case GLYPH ->
                    "Disarmable Single/Repeating twilight sigil that erupts into a 5m Eclipse Gravity Vortex for 4.0s, dragging mobs inward with high-contrast blindness and continuous Penumbra crushing.";
            case CONE ->
                    "60° cone of solar coronal rays & indigo twilight ribbons vaporizing incoming arrows/light projectiles, knocking foes 3m back, and inflicting 25.0s Optical Eclipse Dazzle AI disruption.";
            case SKYFALL ->
                    "Extended 18.0s storm + 8.0s lingering twilight event horizon across 12m+: slams & drags mobs 5m inward, strips 5 armor points, blinds for 6.0s, and slows enemy movement by 60%.";
            case SUMMON_AGENT ->
                    "45s / 60 HP refreshing Penumbral Void-Bladewarden executing shadow-step twin-scythe strikes that pull and blind foes for 5.0s.";
            case SUMMON_SENTRY ->
                    "45s / 80 HP refreshing Twilight Corona Citadel firing twin suppression lances up to 16m, pulsing an 8m repulsion wave every 4s, and granting allies +6 Armor, +3 Toughness & debuff immunity.";
            case SUMMON_DECOY ->
                    "12s Volatile Eclipse Singularity Effigy projecting a 14m taunt before collapsing into a 10m event-horizon implosion that silences abilities for 8.0s.";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Penumbral Horizon-Phase Step";
            case TRANSMUTE, BREAK -> "Penumbral Eclipse Transmutation";
            case AFFLICTION -> "Penumbral Contrast-Collapse Hex";
            default -> "Penumbral Eclipse " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK ->
                    "Phase-shifts up to 16m through twilight space, leaving a 2.0s Twilight Silhouette Decoy that implodes (4m pull + 4.0s Blindness) and granting +30% sprint speed and +25% dodge for 5.0s.";
            case TRANSMUTE, BREAK ->
                    "Catalytic light-dark threshold manipulation: flips Obsidian/Crying Obsidian <-> End Stone/Sculk, shifts Glowstone <-> Sea Lantern/Shroomlight, purges Curse enchantments from gear, and coagulates Rotten Flesh into Penumbra Essence.";
            case AFFLICTION ->
                    "Milk-immune sensory collapse curse oscillating Blindness & Darkness, cutting enemy attack hit accuracy by 50%, disrupting AI pathfinding, stripping 35% Armor Toughness, and locking natural health regen.";
            default -> "Infuses " + effect.getDisplayName() + " with Tier 3 Trinity Penumbra Materia.";
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATION — PENUMBRAL CORONA-UMBRA BINARY ORBIT FLOW
    // =========================================================================

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.05D, 0.05D, 0.05D, 0.02D);
        if (flow == SpellFlowModifier.ORBIT_SATELLITE && projectile.tickCount % 5 == 0) {
            LivingEntity owner = projectile.getOwner() instanceof LivingEntity liv ? liv : null;
            for (AbstractArrow arrow : level.getEntitiesOfClass(AbstractArrow.class, projectile.getBoundingBox().inflate(1.6D))) {
                if (owner != null && arrow.getOwner() == owner) continue;
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, arrow.getX(), arrow.getY(), arrow.getZ(), 6, 0.1D, 0.1D, 0.1D, 0.03D);
                arrow.discard();
            }
        }
    }

    // =========================================================================
    // PART 2: DELIVERY FORM MUTATIONS
    // =========================================================================

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
        level.sendParticles(ParticleTypes.SQUID_INK, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 1 — PENUMBRAL TWILIGHT ECLIPSE-BOLT (PROJECTILE)
    // -------------------------------------------------------------------------

    @Override
    public void onProjectileHitEntityFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            Entity hitEntity,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Section 6.41 Penumbral Twilight Eclipse-Bolt:
        // Detonates into a twilight bifurcation blast, dealing heavy Penumbra Materia damage, blinding struck enemies
        // with searing coronal glare for 3.5s (70t), and dragging all mobs within 3m toward the impact epicenter!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        if (hitEntity instanceof LivingEntity victim && victim.isAlive()
                && !SpellExecutionEngine.isFriendlyAllyForOasis(caster, victim)
                && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(victim, caster, wardSafe))) {
            victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 70, 0));
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithPenumbraMateriaAdvantage(
                        level, victim, caster, 3.60F + tier * 0.95F, false, true
                );
            }
        }
        pullMobsTowardEpicenter(level, caster, hitEntity.position(), hitEntity, 3.0D, tier, wardSafe);
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
        pullMobsTowardEpicenter(level, caster, blockHit.getLocation(), null, 3.0D,
                spell.getEffectiveTier(), spell.hasWardFriendlyFireSafeguard());
    }

    private static void pullMobsTowardEpicenter(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 center,
            @Nullable Entity directTarget,
            double r,
            int tier,
            boolean safeguard
    ) {
        SpellWorldVfxManager.broadcastProjectileImpact(
                level, center.add(0.0D, 0.2D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.PENUMBRA, EssenceType.RADIANT, 1.80F, tier
        );
        float splashDmg = 2.15F + tier * 0.55F;
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 2.5D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != caster)) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, safeguard)) continue;
            Vec3 pull = center.subtract(foe.position()).scale(0.45D);
            foe.setDeltaMovement(pull.x, 0.18D, pull.z);
            foe.hasImpulse = true;
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 70, 0));
            if (foe != directTarget) {
                SpellCombatReactionEngine.hurtWithPenumbraMateriaAdvantage(level, foe, caster, splashDmg, false, true);
            }
        }
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 0.5D, center.z, 18, 0.8D, 0.4D, 0.8D, 0.06D);
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 0.5D, center.z, 10, 0.6D, 0.3D, 0.6D, 0.04D);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 2 — PENUMBRAL EVENT-LENSING RAY (BEAM)
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
        // Section 6.41 Penumbral Event-Lensing Ray:
        // Continuously sears caught targets with Penumbra Materia damage while warping target vision between extreme
        // contrast streaks; REQUIRES 5.0s (100t / 5 pulses) sustained continuous channeling to trigger Penumbral
        // Convergence Flash (massive burst Penumbra damage + 4m inward pull)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        long now = level.getGameTime();

        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.addEffect(new MobEffectInstance((now % 40L < 20L) ? MobEffects.BLINDNESS : MobEffects.DARKNESS, 40, 0));

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong(TAG_PENUMBRA_BEAM_LAST_TICK).orElse(-100L);
            long delta = now - last;
            int stepTicks = (delta > 0L && delta <= 8L) ? (int) Math.max(4L, delta) : 20;
            int contactTicks = (delta <= 45L) ? data.getInt(TAG_PENUMBRA_BEAM_TICKS).orElse(0) + stepTicks : stepTicks;
            data.putLong(TAG_PENUMBRA_BEAM_LAST_TICK, now);
            data.putInt(TAG_PENUMBRA_BEAM_TICKS, contactTicks);

            if (!slot.hasHarmVerb()) {
                float rayDmg = (1.55F + tier * 0.44F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithPenumbraMateriaAdvantage(level, hit, caster, rayDmg, false, true);
            }

            if (contactTicks >= 100) {
                data.putInt(TAG_PENUMBRA_BEAM_TICKS, 0);
                SpellWorldVfxManager.broadcastRadialNova(
                        level, hit.position(), 4.2D, EssenceType.PENUMBRA, EssenceType.RADIANT, tier
                );
                SpellWorldVfxManager.broadcastVacuumSphere(
                        level, hit.position().add(0.0D, 0.8D, 0.0D), 4.0D, true, EssenceType.PENUMBRA, EssenceType.RADIANT, 36, tier
                );
                float flashDmg = (10.20F + tier * 2.55F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithPenumbraMateriaAdvantage(level, hit, caster, flashDmg, false, true);
                pullMobsTowardEpicenter(level, caster, hit.position(), hit, 4.0D, tier, wardSafe);
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 0.92F, 0.47F),
                        hit.getX(), hit.getY(1.0D), hit.getZ(), 1, 0, 0, 0, 0);
                level.playSound(null, hit.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.35F);
            }
        }
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 3 — TWILIGHT CORONA NOVA (RADIAL BURST)
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
        // Section 6.41 Twilight Corona Nova:
        // 6m+ radius, strictly ZERO physical light blocks — knocks enemies 6m backward, inflicts 4.0s (80t) optical
        // dazzle/blindness, smites undead, and cloaks caster & allies in Twilight Shimmer (+20% dodge for 6.0s / 120t)!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.0D, radius);
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        long shimmerUntil = level.getGameTime() + 120L;
        applyTwilightShimmer(caster, shimmerUntil, 0.20F);

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.PENUMBRA, EssenceType.RADIANT, tier);

        float novaDmg = (3.45F + tier * 0.90F) * Math.max(0.5F, apparatusPotency);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.5D, center.z + r);
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (ent == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, ent)) {
                applyTwilightShimmer(ent, shimmerUntil, 0.20F);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, wardSafe)) continue;

            Vec3 delta = ent.position().subtract(center);
            Vec3 away = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.48D) : new Vec3(0.0D, 0.36D, 0.0D);
            ent.setDeltaMovement(away.x, 0.36D, away.z); // ~6m knockback!
            ent.hasImpulse = true;
            ent.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            ent.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
            boolean isUndead = ent.getType().is(EntityTypeTags.UNDEAD);
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithPenumbraMateriaAdvantage(
                        level, ent, caster, novaDmg * (isUndead ? 1.50F : 1.0F), false, true
                );
            } else if (isUndead) {
                SpellCombatReactionEngine.hurtWithPenumbraMateriaAdvantage(level, ent, caster, 4.5F + tier * 0.85F, false, true);
            }
        }
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 0.92F, 0.47F),
                center.x, center.y + 1.0D, center.z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 0.9D, center.z, 28, 2.2D, 0.5D, 2.2D, 0.06D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 0.8D, center.z, 28, 2.2D, 0.5D, 2.2D, 0.08D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.95F, 1.18F);
    }

    public static void applyTwilightShimmer(LivingEntity ally, long until, float dodgeChance) {
        CompoundTag tag = EntityHelper.getPersistentData(ally);
        tag.putLong(SpellLingeringZoneManager.TAG_PENUMBRA_SHIMMER_UNTIL, until);
        tag.putFloat(SpellLingeringZoneManager.TAG_PENUMBRA_SHIMMER_DODGE, dodgeChance);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 4 — PENUMBRAL ECLIPSE CLEAVE (TOUCH / STRIKE)
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
        // Section 6.41 Penumbral Eclipse Cleave:
        // Sweeping scythe-like arc of lensed radiant sunlight & crushing twilight shadow (#FFEA78 / #281E3C);
        // blinds primary target for 3.0s (60t), bypasses armor, and leaves a 4.0s (80t) crescent wake slowing by 35%!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        Vec3 push = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D), push, 3.3D,
                EssenceType.PENUMBRA, EssenceType.RADIANT, tier
        );

        float cleaveDmg = (3.90F + tier * 1.00F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithPenumbraMateriaAdvantage(level, hit, caster, cleaveDmg, true, true);
            }
        }

        Vec3 wakePos = caster.position().add(push.scale(2.4D));
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, wakePos.add(0.0D, 0.04D, 0.0D), 2.6D, EssenceType.PENUMBRA, EssenceType.RADIANT, 80, tier
        );
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, wakePos, 2.6D, 80, level.getGameTime(), spell, slot, EssenceType.PENUMBRA, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 10 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean zSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        AABB box = new AABB(
                                zone.center.x - 2.6D, zone.center.y - 1.0D, zone.center.z - 2.6D,
                                zone.center.x + 2.6D, zone.center.y + 2.0D, zone.center.z + 2.6D
                        );
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zSafe)) continue;
                            SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, srv, 0.35D, 30L);
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 1));
                        }
                        srv.sendParticles(ParticleTypes.REVERSE_PORTAL, zone.center.x, zone.center.y + 0.2D, zone.center.z, 8, 1.0D, 0.15D, 1.0D, 0.03D);
                    }
                    return true;
                }
        ));
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 5 — PENUMBRAL CORONA MANTLE (AURA / SELF)
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
        // Section 6.41 Penumbral Corona Mantle:
        // +6 Armor, +2 Toughness, Blindness/Darkness immunity, 25% projectile void-refraction, & 30% attacker melee dazzle!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_PENUMBRA_MANTLE_UNTIL, level.getGameTime() + dur);
        caster.removeEffect(MobEffects.BLINDNESS);
        caster.removeEffect(MobEffects.DARKNESS);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 6.0D, 2.0D, 0.0D, 0.0D, dur);
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.55F, null);
        level.sendParticles(ParticleTypes.END_ROD, caster.getX(), caster.getY(0.9D), caster.getZ(), 20, 0.4D, 0.5D, 0.4D, 0.04D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, caster.getX(), caster.getY(0.9D), caster.getZ(), 16, 0.4D, 0.5D, 0.4D, 0.04D);
        level.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.95F, 1.15F);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 6 — PENUMBRAL TWILIGHT TRAP GLYPH (GLYPH / TRAP)
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
        // Section 6.41 Penumbral Twilight Trap Glyph:
        // Disarmable Single/Repeating twilight sigil (#FFEA78 / #281E3C) that erupts into an Eclipse Gravity Vortex
        // lasting 4.0s (80t), dragging all mobs within 5m to the epicenter with high-contrast blindness & Penumbra damage!
        int tier = spell.getEffectiveTier();
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snapped = new Vec3(surface.getX() + 0.5D, surface.above().getY(), surface.getZ() + 0.5D);
        double trapRad = Math.max(3.5D, radius);

        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxTriggers = repeating ? 3 : 1;
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE / 4 : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snapped.add(0.0D, 0.03D, 0.0D), trapRad, EssenceType.PENUMBRA, EssenceType.RADIANT, Math.min(240, maxDuration), tier
        );

        final int[] triggersLeft = {maxTriggers};
        final int[] rearmCooldown = {0};

        SpellLingeringZoneManager.ActiveSpellZone glyphZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snapped, trapRad, maxDuration, level.getGameTime(), spell, slot, EssenceType.PENUMBRA, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();

                    if (elapsed > 0 && elapsed % 200 == 0) {
                        int rem = zone.maxDurationTicks >= 1_000_000_000 ? 240 : (int) Math.min(240L, zone.maxDurationTicks - elapsed);
                        SpellWorldVfxManager.broadcastArcaneSeal(
                                srv, zone.center.add(0.0D, 0.03D, 0.0D), zone.radius, EssenceType.PENUMBRA, EssenceType.RADIANT, rem, tier
                        );
                    }
                    if (elapsed % 12 == 0) {
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 0.15D, zone.center.z, 3, 0.8D, 0.05D, 0.8D, 0.01D);
                    }

                    if (elapsed < 20) return true; // 1.0s arming delay
                    if (rearmCooldown[0] > 0) {
                        rearmCooldown[0]--;
                        return true;
                    }

                    AABB trigger = new AABB(
                            zone.center.x - 2.5D, zone.center.y - 1.0D, zone.center.z - 2.5D,
                            zone.center.x + 2.5D, zone.center.y + 2.0D, zone.center.z + 2.5D
                    );
                    List<LivingEntity> victims = srv.getEntitiesOfClass(LivingEntity.class, trigger, e ->
                            e.isAlive() && e != owner
                                    && !SpellExecutionEngine.isFriendlyAllyForOasis(owner, e)
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe))
                    );
                    if (victims.isEmpty()) return true;

                    SpellWorldVfxManager.broadcastVacuumSphere(
                            srv, zone.center.add(0.0D, 1.0D, 0.0D), 5.0D, true, EssenceType.PENUMBRA, EssenceType.RADIANT, 80, tier
                    );
                    SpellWorldVfxManager.broadcastArcaneSeal(
                            srv, zone.center.add(0.0D, 0.04D, 0.0D), 5.0D, EssenceType.PENUMBRA, EssenceType.RADIANT, 80, tier
                    );

                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 5.0D, 80, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl > sZone.maxDurationTicks) return false;
                                LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                boolean sSafe = sZone.spell != null && sZone.spell.hasWardFriendlyFireSafeguard();
                                int sTier = sZone.spell != null ? sZone.spell.getEffectiveTier() : tier;
                                long now = sLvl.getGameTime();
                                AABB vortex = new AABB(
                                        sZone.center.x - 5.0D, sZone.center.y - 2.0D, sZone.center.z - 5.0D,
                                        sZone.center.x + 5.0D, sZone.center.y + 3.0D, sZone.center.z + 5.0D
                                );
                                for (LivingEntity v : sLvl.getEntitiesOfClass(LivingEntity.class, vortex, e -> e.isAlive() && e != sOwner)) {
                                    if (sOwner != null && SpellExecutionEngine.isFriendlyAllyForOasis(sOwner, v)) continue;
                                    if (sOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, sOwner, sSafe)) continue;

                                    Vec3 pull = sZone.center.subtract(v.position()).scale(0.32D);
                                    v.setDeltaMovement(pull.x, 0.05D, pull.z);
                                    v.hasImpulse = true;
                                    v.addEffect(new MobEffectInstance((now % 30L < 15L) ? MobEffects.BLINDNESS : MobEffects.DARKNESS, 40, 0));
                                    if (sEl % 20 == 0) {
                                        float vortexDmg = (2.75F + sTier * 0.70F) * Math.max(0.5F, sZone.apparatusPotency);
                                        SpellCombatReactionEngine.hurtWithPenumbraMateriaAdvantage(sLvl, v, sOwner, vortexDmg, false, true);
                                        if (sZone.spell != null && sZone.slot != null) {
                                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                                    sLvl, sOwner, v, Vec3.ZERO,
                                                    sZone.spell, sZone.slot, sZone.fallbackMateria, sZone.apparatusPotency * 0.55F
                                            );
                                        }
                                    }
                                }
                                if (sEl % 5 == 0) {
                                    sLvl.sendParticles(ParticleTypes.REVERSE_PORTAL, sZone.center.x, sZone.center.y + 0.4D, sZone.center.z, 14, 1.6D, 0.3D, 1.6D, 0.05D);
                                }
                                return true;
                            }
                    ));
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.95F, 1.25F);
                    triggersLeft[0]--;
                    rearmCooldown[0] = 95;
                    return triggersLeft[0] > 0;
                }
        ).markDisarmableGlyph();
        SpellLingeringZoneManager.registerZone(level, glyphZone);
        return true;
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 7 — PENUMBRAL CORONA-TWILIGHT FAN DELUGE (CONE / SPRAY)
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
        // Section 6.41 Penumbral Corona-Twilight Fan Deluge:
        // 60° cone of solar coronal rays & deep indigo twilight ribbons; deals Penumbra Materia damage, incinerates
        // incoming arrows/light projectiles, knocks foes 3m back, & applies 25.0s (500t) Optical Eclipse Dazzle AI disruption!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        Vec3 dirNorm = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();

        SpellWorldVfxManager.broadcastVortexCone(
                level, caster.getEyePosition().subtract(0.0D, 0.2D, 0.0D), dirNorm, coneRange,
                EssenceType.PENUMBRA, EssenceType.RADIANT, tier
        );

        AABB projCone = new AABB(caster.getEyePosition(), caster.getEyePosition().add(dirNorm.scale(coneRange))).inflate(2.5D);
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, projCone,
                p -> p.getOwner() != caster && !(p instanceof SpellProjectileEntity))) {
            level.sendParticles(ParticleTypes.END_ROD, proj.getX(), proj.getY(), proj.getZ(), 5, 0.1D, 0.1D, 0.1D, 0.02D);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, proj.getX(), proj.getY(), proj.getZ(), 5, 0.1D, 0.1D, 0.1D, 0.02D);
            proj.discard();
        }

        float coneDmg = (2.95F + tier * 0.76F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitTargets) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.setDeltaMovement(dirNorm.x * 0.85D, 0.25D, dirNorm.z * 0.85D); // ~3m knockback!
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0));
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 500); // 25.0s AI pathfinding disruption!
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithPenumbraMateriaAdvantage(level, hit, caster, coneDmg, false, true);
            }
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.95F, 1.25F);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 8 — PENUMBRAL CATACLYSMIC ECLIPSE TEMPEST (SKYFALL)
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
        // Section 6.41 Penumbral Cataclysmic Eclipse Tempest (Extended 18s Duration, Tier 3 Trinity Power):
        // 18.0s (360t) storm of heavy solar corona meteors & twilight singularity boulders across 12m+ radius + 8.0s (160t)
        // lingering twilight event horizon (520t total); slams mobs downward & drags 5m inward, strips 5 armor points,
        // inflicts 6.0s (120t) Blindness, and slows enemy movement speed by 60% across the entire 26.0s span!
        int tier = spell.getEffectiveTier();
        double r = Math.max(12.0D, radius);

        SpellWorldVfxManager.broadcastSkyfallPillar(
                level, impactCenter, r, EssenceType.PENUMBRA, EssenceType.RADIANT, 365, tier
        );

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 520, level.getGameTime(), spell, slot, EssenceType.PENUMBRA, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    boolean stormActive = elapsed <= 360;
                    int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;

                    if (elapsed == 361) {
                        SpellWorldVfxManager.broadcastArcaneSeal(
                                srv, zone.center.add(0.0D, 0.04D, 0.0D), zone.radius,
                                EssenceType.PENUMBRA, EssenceType.RADIANT, 160, zTier
                        );
                    }
                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.REVERSE_PORTAL, zone.center.x, zone.center.y + (stormActive ? 3.5D : 0.4D), zone.center.z,
                                22, zone.radius * 0.55D, stormActive ? 1.8D : 0.2D, zone.radius * 0.55D, 0.06D);
                        if (stormActive) {
                            srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 4.0D, zone.center.z,
                                    14, zone.radius * 0.50D, 1.6D, zone.radius * 0.50D, 0.05D);
                        }
                    }
                    if (elapsed % 16 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        float meteorDmg = (4.25F + zTier * 1.10F) * Math.max(0.5F, zone.apparatusPotency);
                        if (stormActive) {
                            double ang = srv.random.nextDouble() * Math.PI * 2.0D;
                            double dist = srv.random.nextDouble() * (zone.radius * 0.72D);
                            Vec3 strike = zone.center.add(Math.cos(ang) * dist, 0.2D, Math.sin(ang) * dist);
                            SpellWorldVfxManager.broadcastProjectileImpact(
                                    srv, strike, new Vec3(0.0D, 1.0D, 0.0D), EssenceType.PENUMBRA, EssenceType.RADIANT, 2.35F, zTier
                            );
                            srv.playSound(null, BlockPos.containing(strike), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.75F, 0.90F);
                        }

                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 6.0D, zone.center.z + zone.radius
                        );
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) continue;

                            SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, srv, 0.60D, 35L);
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 3)); // 60% slow across 18s + 8s after!
                            if (stormActive) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 5.0D, 0.0D, 160L);
                                foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0));
                                Vec3 delta = zone.center.subtract(foe.position());
                                Vec3 inward = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(0.68D) : Vec3.ZERO;
                                foe.setDeltaMovement(inward.x, -0.38D, inward.z);
                                foe.hasImpulse = true;
                                if (zone.spell != null && zone.slot != null) {
                                    SpellExecutionEngine.deliverSubSpellOnEntity(
                                            srv, owner, foe, new Vec3(0.0D, -1.0D, 0.0D),
                                            zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.45F
                                    );
                                }
                                SpellCombatReactionEngine.hurtWithPenumbraMateriaAdvantage(srv, foe, owner, meteorDmg, false, true);
                            }
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 9 — PENUMBRAL APEX EIDOLON SUITE (SUMMON / EIDOLON)
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
        tag.putString("SpellEidolonMateria", EssenceType.PENUMBRA.name());
        tag.putInt("SpellEidolonTintRGB", PENUMBRA_TWILIGHT_HEX_COLOR);
        SpellWorldVfxManager.broadcastSummonPillar(
                level, spawnPos, Math.max(1.5D, radius), EssenceType.PENUMBRA, EssenceType.RADIANT, spell.getEffectiveTier()
        );
        level.sendParticles(ParticleTypes.END_ROD, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 20, 0.45D, 0.55D, 0.45D, 0.05D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 20, 0.45D, 0.55D, 0.45D, 0.05D);
    }

    // =========================================================================
    // PART 3: CORE EFFECT MUTATIONS — BLINK, TRANSMUTE & AFFLICTION
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Section 6.41 Penumbral Horizon-Phase Step:
        // Phase-shifts up to 16m; leaves unstable Twilight Silhouette Decoy at departure that taunts hostiles and
        // implodes after 2.0s (40t) pulling nearby mobs 4m inward & blinding 4.0s (80t); arrival grants +30% sprint
        // speed & +25% dodge chance for 5.0s (100t)!
        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.PENUMBRA, potencyTier);

        SpellEidolonEntity decoy = SpellEidolonEntity.spawnShortLivedDecoy(
                level, caster, departurePos, EssenceType.PENUMBRA,
                16.0F, 40, "Twilight Silhouette Decoy", null
        );
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(departurePos, departurePos).inflate(10.0D), Mob::isAlive)) {
            if (mob != decoy && mob.getTarget() == caster && decoy != null) {
                mob.setTarget(decoy);
            }
        }
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, departurePos, 4.0D, 40, level.getGameTime(), null, null, EssenceType.PENUMBRA, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed < zone.maxDurationTicks) return true;
                    LivingEntity owner = zone.resolveCaster(srv);
                    SpellWorldVfxManager.broadcastVacuumSphere(
                            srv, departurePos.add(0.0D, 0.9D, 0.0D), 4.0D, true, EssenceType.PENUMBRA, EssenceType.RADIANT, 36, potencyTier
                    );
                    pullMobsTowardEpicenter(srv, owner, departurePos, null, 4.0D, potencyTier, true);
                    AABB blindBox = new AABB(
                            departurePos.x - 4.0D, departurePos.y - 2.0D, departurePos.z - 4.0D,
                            departurePos.x + 4.0D, departurePos.y + 2.5D, departurePos.z + 4.0D
                    );
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, blindBox, e -> e.isAlive() && e != owner)) {
                        if (owner != null && SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, true)) continue;
                        foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    }
                    return false;
                }
        ));

        caster.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 1));
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 0.0D, 0.0D, 0.0D, 0.30D, 100L);
        applyTwilightShimmer(caster, level.getGameTime() + 100L, 0.25F);
        level.sendParticles(ParticleTypes.END_ROD, arrivalPos.x, arrivalPos.y + 0.9D, arrivalPos.z, 18, 0.35D, 0.45D, 0.35D, 0.04D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, arrivalPos.x, arrivalPos.y + 0.9D, arrivalPos.z, 18, 0.35D, 0.45D, 0.35D, 0.05D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.85F, 1.25F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Section 6.41 Penumbral Eclipse Transmutation:
        // Obsidian / Crying Obsidian <-> End Stone / Sculk; Glowstone <-> Sea Lantern / Shroomlight;
        // purges negative curses (Curse of Binding, Curse of Vanishing) from equipment; Rotten Flesh -> Penumbra Essence!
        boolean didAny = false;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.5D), ItemEntity::isAlive)) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) {
                didAny = true;
            }
        }
        if (caster != null) {
            for (EquipmentSlot eq : EquipmentSlot.values()) {
                if (purgeCursesFromStack(caster.getItemBySlot(eq))) {
                    didAny = true;
                }
            }
        }
        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            if (didAny) refundBatchDiscount(level, caster, potencyTier);
            return didAny;
        }
        if (state.is(Blocks.OBSIDIAN) || state.is(Blocks.CRYING_OBSIDIAN)) {
            level.setBlockAndUpdate(pos, level.random.nextBoolean() ? Blocks.END_STONE.defaultBlockState() : Blocks.SCULK.defaultBlockState());
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.END_STONE) || state.is(Blocks.SCULK)) {
            level.setBlockAndUpdate(pos, level.random.nextBoolean() ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.CRYING_OBSIDIAN.defaultBlockState());
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.GLOWSTONE)) {
            level.setBlockAndUpdate(pos, level.random.nextBoolean() ? Blocks.SEA_LANTERN.defaultBlockState() : Blocks.SHROOMLIGHT.defaultBlockState());
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.SEA_LANTERN) || state.is(Blocks.SHROOMLIGHT)) {
            level.setBlockAndUpdate(pos, Blocks.GLOWSTONE.defaultBlockState());
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (didAny) {
            refundBatchDiscount(level, caster, potencyTier);
        }
        return didAny;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        if (purgeCursesFromStack(stack)) {
            itemEntity.setItem(stack.copy());
            return true;
        }
        if (stack.is(Items.ROTTEN_FLESH)) {
            ItemStack essence = new ItemStack(ModItems.WEAK_ESSENCE.get(), stack.getCount());
            EssenceItem.setEssenceType(essence, EssenceType.PENUMBRA);
            itemEntity.setItem(essence);
            return true;
        }
        if (stack.is(Items.OBSIDIAN) || stack.is(Items.CRYING_OBSIDIAN)) {
            itemEntity.setItem(new ItemStack(level.random.nextBoolean() ? Items.END_STONE : Items.SCULK, stack.getCount()));
            return true;
        }
        if (stack.is(Items.END_STONE) || stack.is(Items.SCULK)) {
            itemEntity.setItem(new ItemStack(level.random.nextBoolean() ? Items.OBSIDIAN : Items.CRYING_OBSIDIAN, stack.getCount()));
            return true;
        }
        if (stack.is(Items.GLOWSTONE)) {
            itemEntity.setItem(new ItemStack(level.random.nextBoolean() ? Items.SEA_LANTERN : Items.SHROOMLIGHT, stack.getCount()));
            return true;
        }
        return false;
    }

    public static boolean purgeCursesFromStack(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ItemEnchantments ench = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (ench.isEmpty()) return false;
        ItemEnchantments.Mutable mut = new ItemEnchantments.Mutable(ench);
        boolean[] removed = {false};
        mut.removeIf(h -> {
            if (h.is(EnchantmentTags.CURSE)) {
                removed[0] = true;
                return true;
            }
            return false;
        });
        if (removed[0]) {
            stack.set(DataComponents.ENCHANTMENTS, mut.toImmutable());
            return true;
        }
        return false;
    }

    private static void refundBatchDiscount(ServerLevel level, @Nullable LivingEntity caster, int tier) {
        if (caster instanceof Player player) {
            CompoundTag pData = EntityHelper.getPersistentData(player);
            long now = level.getGameTime();
            if (pData.getLong(TAG_PENUMBRA_TRANSMUTE_BATCH_TICK).orElse(-10L) != now) {
                pData.putLong(TAG_PENUMBRA_TRANSMUTE_BATCH_TICK, now);
                long refund = Math.max(1L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25D));
                SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.PENUMBRA, refund);
            }
        }
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.41 Penumbral Contrast-Collapse Hex:
        // Violently oscillates victim vision between Blindness & Darkness, reduces mob/boss hit accuracy by 50%,
        // heavily cripples AI pathfinding, strips 35% of armor toughness, and blocks natural health regeneration!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        long now = level.getGameTime();
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.80D, 0.35D * mult), 45L);
        SpellLingeringZoneManager.applyAiPathDisruption(target, 60);
        target.addEffect(new MobEffectInstance((now % 40L < 20L) ? MobEffects.BLINDNESS : MobEffects.DARKNESS, 35, 0, true, true));

        LivingEntity caster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        float hexDmg = (1.55F + potencyTier * 0.50F) * mult;
        SpellCombatReactionEngine.hurtWithPenumbraMateriaAdvantage(level, target, caster, hexDmg, false, true);

        level.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getY(0.85D), target.getZ(), 5, 0.25D, 0.3D, 0.25D, 0.03D);
        level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.95D), target.getZ(), 3, 0.2D, 0.25D, 0.2D, 0.02D);
        return true;
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
