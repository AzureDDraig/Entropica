package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.registry.ModEffects;
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
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ColorParticleOption;
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
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Complete per-part Signature Mutation implementation for <b>Rime Materia (Tier 3 Trinity Fusion #41)</b>
 * (Spellcrafting System Guide Section 6.42 — Trinity Air + Water + Frozen harmonic matrix):
 * <ul>
 *     <li><b>Palette</b>: Frostbite Slate Blue ({@code #6395B1}) + Hoarfrost White ({@code #DCF0FF})</li>
 *     <li><b>Advantage</b>: Strong vs <b>EARTH (+1)</b>, <b>NATURE (+1)</b>, <b>MAGMA (+1)</b>, and <b>OVERGROWTH (+1)</b>
 *         (plus <b>NETHER</b> &amp; <b>ARID</b> under Water Dominance) | Weak vs <b>VOID (-1)</b> and <b>UNDEAD (-1)</b></li>
 * </ul>
 */
public final class RimeSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int HOARFROST_RIME_HEX_COLOR = 0x6395B1;
    public static final int HOARFROST_WHITE_HEX_COLOR = 0xDCF0FF;

    private static final String TAG_RIME_BEAM_LAST_TICK = "RimeBeamLastTick";
    private static final String TAG_RIME_BEAM_TICKS = "RimeBeamContactTicks";
    private static final String TAG_RIME_TRANSMUTE_BATCH_TICK = "RimeTransmuteBatchTick";

    @Override
    public EssenceType materiaType() {
        return EssenceType.RIME;
    }

    // =========================================================================
    // TITLES & CODEX / TOOLTIP DESCRIPTIONS
    // =========================================================================

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Crystalline Hoarfrost Spiral Flow";
    }

    @Override
    public String resolveFlowDescription(SpellFlowModifier flow) {
        return "4 razor-sharp crystalline hoarfrost icicles and sub-zero winds (#6395B1 / #DCF0FF) grant +5 Armor, +2 Toughness, Freeze/Powder-Snow/Slowness immunity, arrow frost-shattering, -30% attacker chill, and 2.0s-rooting homing frost darts on melee or spell hit.";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Hoarfrost Rime-Spike Javelin";
            case BEAM -> "Glacial Absolute-Zero Jetstream Beam";
            case RADIAL_BURST -> "Flash-Freeze Rime Frost-Nova";
            case TOUCH_STRIKE -> "Permafrost Rime-Cleave";
            case AURA -> "Hoarfrost Glacial Aegis Mantle";
            case GLYPH -> "Flash-Freeze Rime Pitfall Glyph";
            case CONE -> "Sub-Zero Blizzarding Rime Cone";
            case SKYFALL -> "Glacial Rime Cataclysmic Blizzard";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE ->
                    "Hurls a 3-pierce 3D hoarfrost javelin (#6395B1 / #DCF0FF) encasing struck foes in brittle rime-ice (3.0s root + freeze) and shattering into 3.5m frost shrapnel (-50% speed for 4.0s).";
            case BEAM ->
                    "Channels a sub-zero needle-ice jetstream stacking Slowness up to IV; 5.0s sustained channeling triggers Deep Freeze Shatter, freezing caught foes solid for 2.5s with massive burst damage.";
            case RADIAL_BURST ->
                    "6m+ zero-light-block arctic shockwave knocking foes 6m back, flash-freezing them solid for 3.0s, coating ground in rime-frost, and granting caster & allies Hoarfrost Carapace (+4 Armor & 50% fire resistance for 8.0s).";
            case TOUCH_STRIKE ->
                    "Sweeping hoarfrost cleave stacking weapon Materia Advantage, shattering 3 armor points, flash-freezing primary targets for 2.5s, knocking them 5m back, and leaving a 4.0s needle-ice arc that roots foes for 2.0s.";
            case AURA ->
                    "Envelops caster (or anchored area) in crystalline hoarfrost armor (+7 Armor, +3 Toughness, Freeze/Powder-Snow/Slowness/Fire immunity, Packed Ice underfoot, and -35% attack-speed Cryogenic Brittle-Frost retaliation).";
            case GLYPH ->
                    "Disarmable Single/Repeating frost sigil erupting into a 4m jagged rime-ice chasm for 4.0s, rooting caught hostiles for 3.5s, stripping 3 armor points, and locking jumping.";
            case CONE ->
                    "60° blizzard cone extinguishing fires & torches, freezing water into Packed Ice, knocking foes 3m back, and inflicting Hypothermic Numbness (Slowness IV, -50% attack speed, and 25.0s AI pathfinding disruption).";
            case SKYFALL ->
                    "Extended 18.0s storm of 10 falling 3D hoarfrost stalactites + 8.0s lingering permafrost across 12m+: slams downward, shatters 5 armor points, inflicts 6.0s freezing stasis, and slows enemy movement by 60%.";
            case SUMMON_AGENT ->
                    "45s / 65 HP refreshing Glacial Rime-Vanguard delivering 35%-armor-bypassing rime-greatsword cleaves that flash-freeze foes solid for 2.5s.";
            case SUMMON_SENTRY ->
                    "45s / 85 HP refreshing Hoarfrost Glacial Citadel Spire firing piercing needle-ice bolts up to 16m, pulsing an 8m root burst every 4s, and granting allies +6 Armor, +3 Toughness & Fire/Freeze immunity.";
            case SUMMON_DECOY ->
                    "12s Brittle Permafrost Effigy projecting a 14m taunt before detonating into a 10m Deep Freeze Shatter Nova (3.5s ice-statue freeze + -4 armor shred).";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Permafrost Glacial Slide";
            case TRANSMUTE, BREAK -> "Glacial Cryo-Crystallization Transmute";
            case AFFLICTION -> "Sub-Zero Cryo-Numbness Hex";
            default -> "Hoarfrost Rime " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK ->
                    "Propels the caster up to 18m along a sub-zero packed/blue ice slide, knocking aside foes in the trajectory and granting Frost Walker + 30% movement speed for 6.0s.";
            case TRANSMUTE, BREAK ->
                    "Sub-zero catalytic transmutation: freezes water into Packed/Blue Ice, lava into Obsidian/Crying Obsidian, dirt/grass into Permafrost, mud into Clay, sand into Frosted Quartz/Calcite, and condenses air vapor into water or ice.";
            case AFFLICTION ->
                    "Milk-immune cryogenic nerve & enchantment-suppression curse suppressing victim weapon & armor enchantments, stripping 30% Armor Toughness, stunning for 0.5s every 3.0s, and dealing continuous sub-zero Rime damage.";
            default -> "Infuses " + effect.getDisplayName() + " with Tier 3 Trinity Rime Materia.";
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATION — CRYSTALLINE HOARFROST SPIRAL FLOW
    // =========================================================================

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.SNOWFLAKE, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.01D);
        level.sendParticles(ParticleTypes.WHITE_ASH, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
        if (flow == SpellFlowModifier.ORBIT_SATELLITE && projectile.tickCount % 4 == 0) {
            LivingEntity owner = projectile.getOwner() instanceof LivingEntity liv ? liv : null;
            for (AbstractArrow arrow : level.getEntitiesOfClass(AbstractArrow.class, projectile.getBoundingBox().inflate(1.65D))) {
                if (owner != null && arrow.getOwner() == owner) continue;
                level.sendParticles(ParticleTypes.SNOWFLAKE, arrow.getX(), arrow.getY(), arrow.getZ(), 8, 0.12D, 0.12D, 0.12D, 0.03D);
                level.playSound(null, arrow.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.55F, 1.55F);
                arrow.discard();
            }
        }
    }

    // =========================================================================
    // PART 2: DELIVERY FORM MUTATIONS
    // =========================================================================

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.SNOWFLAKE, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.05D, 0.05D, 0.05D, 0.02D);
        level.sendParticles(ParticleTypes.WHITE_ASH, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 1 — HOARFROST RIME-SPIKE JAVELIN (PROJECTILE)
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
        // Section 6.42 Hoarfrost Rime-Spike Javelin:
        // Pierces through up to 3 enemies dealing heavy Rime Materia damage, encasing struck targets in brittle
        // rime-ice (Freezing ticks + 3.0s / 60t root); on impact with terrain or 3rd target, shatters into 3.5m
        // crystalline frost shrapnel slowing adjacent mobs by 50% for 4.0s (80t)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        if (hitEntity instanceof LivingEntity victim && victim.isAlive()
                && !SpellExecutionEngine.isFriendlyAllyForOasis(caster, victim)
                && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(victim, caster, wardSafe))) {
            victim.setTicksFrozen(victim.getTicksRequiredToFreeze() + 90);
            victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 6));
            victim.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0));
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithRimeMateriaAdvantage(
                        level, victim, caster, 3.65F + tier * 0.95F, false, true
                );
            }
        }
        if (projectile.getPierceRemaining() <= 1) {
            shatterFrostShrapnel(level, caster, hitEntity.position(), hitEntity, tier, wardSafe);
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
        shatterFrostShrapnel(level, caster, blockHit.getLocation(), null,
                spell.getEffectiveTier(), spell.hasWardFriendlyFireSafeguard());
    }

    private static void shatterFrostShrapnel(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 center,
            @Nullable Entity directTarget,
            int tier,
            boolean safeguard
    ) {
        SpellWorldVfxManager.broadcastProjectileImpact(
                level, center.add(0.0D, 0.2D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.RIME, EssenceType.FROZEN, 1.85F, tier
        );
        float shrapnelDmg = 2.25F + tier * 0.58F;
        AABB box = new AABB(center.x - 3.5D, center.y - 1.5D, center.z - 3.5D, center.x + 3.5D, center.y + 2.5D, center.z + 3.5D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != caster)) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, safeguard)) continue;
            SpellCombatReactionEngine.applyTimedMovementSpeedSap(foe, level, 0.50D, 80L);
            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2)); // 50% slow for 4.0s!
            foe.setTicksFrozen(Math.max(foe.getTicksFrozen(), foe.getTicksRequiredToFreeze() + 60));
            if (foe != directTarget) {
                SpellCombatReactionEngine.hurtWithRimeMateriaAdvantage(level, foe, caster, shrapnelDmg, false, true);
            }
        }
        placeTemporaryRimeFrostSheets(level, caster, center, 2, 100);
        level.sendParticles(ParticleTypes.SNOWFLAKE, center.x, center.y + 0.5D, center.z, 28, 1.2D, 0.5D, 1.2D, 0.06D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.32F);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 2 — GLACIAL ABSOLUTE-ZERO JETSTREAM BEAM (BEAM)
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
        // Section 6.42 Glacial Absolute-Zero Jetstream Beam:
        // Continuously crystallizes caught targets with Rime Materia damage, rapid freezing, and stacking Slowness
        // (up to Slowness IV); REQUIRES 5.0s (100t / 5 pulses) sustained continuous channeling to trigger Deep Freeze
        // Shatter, flash-freezing caught hostiles solid for 2.5s (50t) and detonating a massive frost-shockwave!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        long now = level.getGameTime();

        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong(TAG_RIME_BEAM_LAST_TICK).orElse(-100L);
            long delta = now - last;
            int stepTicks = (delta > 0L && delta <= 8L) ? (int) Math.max(4L, delta) : 20;
            int contactTicks = (delta <= 45L) ? data.getInt(TAG_RIME_BEAM_TICKS).orElse(0) + stepTicks : stepTicks;
            data.putLong(TAG_RIME_BEAM_LAST_TICK, now);
            data.putInt(TAG_RIME_BEAM_TICKS, contactTicks);

            int slowAmp = Math.min(3, contactTicks / 25); // Stacks up to Slowness IV (amp 3)!
            hit.setTicksFrozen(hit.getTicksRequiredToFreeze() + 70);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, slowAmp));

            if (!slot.hasHarmVerb()) {
                float beamDmg = (1.60F + tier * 0.45F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithRimeMateriaAdvantage(level, hit, caster, beamDmg, false, true);
            }

            if (contactTicks >= 100) {
                data.putInt(TAG_RIME_BEAM_TICKS, 0);
                SpellWorldVfxManager.broadcastRadialNova(
                        level, hit.position(), 3.8D, EssenceType.RIME, EssenceType.FROZEN, tier
                );
                float shatterDmg = (10.40F + tier * 2.55F) * Math.max(0.5F, apparatusPotency);
                for (LivingEntity caught : level.getEntitiesOfClass(LivingEntity.class, hit.getBoundingBox().inflate(3.5D), e -> e.isAlive() && e != caster)) {
                    if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, caught)) continue;
                    if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(caught, caster, wardSafe)) continue;
                    caught.setTicksFrozen(caught.getTicksRequiredToFreeze() + 90);
                    caught.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 50, 0));
                    SpellCombatReactionEngine.hurtWithRimeMateriaAdvantage(level, caught, caster, shatterDmg, false, true);
                }
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0.86F, 0.94F, 1.0F),
                        hit.getX(), hit.getY(1.0D), hit.getZ(), 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.SNOWFLAKE, hit.getX(), hit.getY(0.9D), hit.getZ(), 32, 1.5D, 0.5D, 1.5D, 0.07D);
                level.playSound(null, hit.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.15F, 0.75F);
            }
        }
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 3 — FLASH-FREEZE RIME FROST-NOVA (RADIAL BURST)
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
        // Section 6.42 Flash-Freeze Rime Frost-Nova:
        // 6m+ radius, strictly ZERO physical light blocks — knocks enemies 6m backward, flash-freezes all struck
        // hostiles for 3.0s (60t), coats ground in frosted rime-sheets, and grants caster & nearby allies
        // Hoarfrost Carapace (+4 Armor and 50% fire damage resistance for 8.0s / 160t)!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.0D, radius);
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        long carapaceUntil = level.getGameTime() + 160L;
        grantHoarfrostCarapace(caster, level, carapaceUntil);

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.RIME, EssenceType.FROZEN, tier);

        float novaDmg = (3.55F + tier * 0.92F) * Math.max(0.5F, apparatusPotency);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.5D, center.z + r);
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (ent == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, ent)) {
                grantHoarfrostCarapace(ent, level, carapaceUntil);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, wardSafe)) continue;

            Vec3 delta = ent.position().subtract(center);
            Vec3 away = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.48D) : new Vec3(0.0D, 0.36D, 0.0D);
            ent.setDeltaMovement(away.x, 0.35D, away.z); // ~6m knockback!
            ent.hasImpulse = true;
            ent.setTicksFrozen(ent.getTicksRequiredToFreeze() + 90);
            ent.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 60, 0));
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithRimeMateriaAdvantage(level, ent, caster, novaDmg, false, true);
            }
        }
        placeTemporaryRimeFrostSheets(level, caster, center, 4, 120);
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0.86F, 0.94F, 1.0F),
                center.x, center.y + 1.0D, center.z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.SNOWFLAKE, center.x, center.y + 0.9D, center.z, 44, 2.3D, 0.6D, 2.3D, 0.06D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.05F, 0.82F);
    }

    private static void grantHoarfrostCarapace(LivingEntity ally, ServerLevel level, long until) {
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, level, 4.0D, 0.0D, 0.0D, 0.0D, 160L);
        EntityHelper.getPersistentData(ally).putLong(SpellLingeringZoneManager.TAG_RIME_FIRE_RES_UNTIL, until);
        ally.clearFire();
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 4 — PERMAFROST RIME-CLEAVE (TOUCH / STRIKE)
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
        // Section 6.42 Permafrost Rime-Cleave:
        // Sweeping heavy cleave of jagged hoarfrost ice & sub-zero gale (#6395B1 / #DCF0FF); deals heavy Rime Materia
        // damage stacking weapon synergy, shatters 3 armor points, flash-freezes primary targets solid for 2.5s (50t),
        // knocks them 5m backward, and leaves a 4.0s (80t) frosted needle-ice crescent rooting walking mobs for 2.0s (40t)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        Vec3 push = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D), push, 3.3D,
                EssenceType.RIME, EssenceType.FROZEN, tier
        );

        float cleaveDmg = (3.95F + tier * 1.02F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 140L);
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 50, 0));
            hit.setTicksFrozen(hit.getTicksRequiredToFreeze() + 90);
            hit.setDeltaMovement(push.x * 1.25D, 0.32D, push.z * 1.25D); // ~5m knockback!
            hit.hasImpulse = true;
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithRimeMateriaAdvantage(level, hit, caster, cleaveDmg, false, true);
            }
        }

        Vec3 trailPos = caster.position().add(push.scale(2.4D));
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, trailPos.add(0.0D, 0.04D, 0.0D), 2.5D, EssenceType.RIME, EssenceType.FROZEN, 80, tier
        );
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, trailPos, 2.5D, 80, level.getGameTime(), spell, slot, EssenceType.RIME, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 10 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean zSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;
                        AABB box = new AABB(
                                zone.center.x - 2.5D, zone.center.y - 1.0D, zone.center.z - 2.5D,
                                zone.center.x + 2.5D, zone.center.y + 1.8D, zone.center.z + 2.5D
                        );
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zSafe)) continue;
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 6));
                            foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 40, 0));
                            if (elapsed % 20 == 0) {
                                SpellCombatReactionEngine.hurtWithRimeMateriaAdvantage(srv, foe, owner, 1.35F + zTier * 0.35F, false, true);
                            }
                        }
                        srv.sendParticles(ParticleTypes.SNOWFLAKE, zone.center.x, zone.center.y + 0.15D, zone.center.z, 8, 1.0D, 0.1D, 1.0D, 0.02D);
                    }
                    return true;
                }
        ));
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 5 — HOARFROST GLACIAL AEGIS MANTLE (AURA / SELF)
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
        // Section 6.42 Hoarfrost Glacial Aegis Mantle:
        // +7 Armor, +3 Toughness, complete immunity to Freezing, Powder Snow, Slowness, and fire ticks;
        // turns source water underfoot into Packed Ice; inflicts Cryogenic Brittle-Frost on melee attackers!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_RIME_MANTLE_UNTIL, level.getGameTime() + dur);
        caster.clearFire();
        caster.setTicksFrozen(0);
        caster.setIsInPowderSnow(false);
        caster.removeEffect(MobEffects.SLOWNESS);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 7.0D, 3.0D, 0.0D, 0.0D, dur);
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.55F, null);
        level.sendParticles(ParticleTypes.SNOWFLAKE, caster.getX(), caster.getY(0.9D), caster.getZ(), 24, 0.45D, 0.55D, 0.45D, 0.04D);
        level.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.95F, 1.28F);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 6 — FLASH-FREEZE RIME PITFALL GLYPH (GLYPH / TRAP)
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
        // Section 6.42 Flash-Freeze Rime Pitfall Glyph:
        // Disarmable Single/Repeating frost sigil (#6395B1 / #DCF0FF) erupting into a 4m jagged rime-ice chasm
        // for 4.0s (80t), dropping caught hostiles inside, rooting them in brittle permafrost for 3.5s (70t),
        // stripping 3 armor points, and preventing jumping or escape while dealing continuous Rime Materia damage!
        int tier = spell.getEffectiveTier();
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snapped = new Vec3(surface.getX() + 0.5D, surface.above().getY(), surface.getZ() + 0.5D);
        double trapRad = Math.max(3.5D, radius);

        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxTriggers = repeating ? 3 : 1;
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE / 4 : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snapped.add(0.0D, 0.03D, 0.0D), trapRad, EssenceType.RIME, EssenceType.FROZEN, Math.min(240, maxDuration), tier
        );

        final int[] triggersLeft = {maxTriggers};
        final int[] rearmCooldown = {0};

        SpellLingeringZoneManager.ActiveSpellZone glyphZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snapped, trapRad, maxDuration, level.getGameTime(), spell, slot, EssenceType.RIME, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();

                    if (elapsed > 0 && elapsed % 200 == 0) {
                        int rem = zone.maxDurationTicks >= 1_000_000_000 ? 240 : (int) Math.min(240L, zone.maxDurationTicks - elapsed);
                        SpellWorldVfxManager.broadcastArcaneSeal(
                                srv, zone.center.add(0.0D, 0.03D, 0.0D), zone.radius, EssenceType.RIME, EssenceType.FROZEN, rem, tier
                        );
                    }
                    if (elapsed % 12 == 0) {
                        srv.sendParticles(ParticleTypes.SNOWFLAKE, zone.center.x, zone.center.y + 0.12D, zone.center.z, 4, 0.85D, 0.05D, 0.85D, 0.01D);
                    }

                    if (elapsed < 20) return true; // 1.0s arming delay
                    if (rearmCooldown[0] > 0) {
                        rearmCooldown[0]--;
                        return true;
                    }

                    AABB trigger = new AABB(
                            zone.center.x - 2.4D, zone.center.y - 1.0D, zone.center.z - 2.4D,
                            zone.center.x + 2.4D, zone.center.y + 2.0D, zone.center.z + 2.4D
                    );
                    List<LivingEntity> victims = srv.getEntitiesOfClass(LivingEntity.class, trigger, e ->
                            e.isAlive() && e != owner
                                    && !SpellExecutionEngine.isFriendlyAllyForOasis(owner, e)
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe))
                    );
                    if (victims.isEmpty()) return true;

                    SpellWorldVfxManager.broadcastArcaneSeal(
                            srv, zone.center.add(0.0D, 0.04D, 0.0D), 4.0D, EssenceType.RIME, EssenceType.FROZEN, 80, tier
                    );

                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 4.0D, 80, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl > sZone.maxDurationTicks) return false;
                                LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                boolean sSafe = sZone.spell != null && sZone.spell.hasWardFriendlyFireSafeguard();
                                int sTier = sZone.spell != null ? sZone.spell.getEffectiveTier() : tier;
                                long now = sLvl.getGameTime();
                                AABB chasm = new AABB(
                                        sZone.center.x - 4.0D, sZone.center.y - 2.0D, sZone.center.z - 4.0D,
                                        sZone.center.x + 4.0D, sZone.center.y + 2.5D, sZone.center.z + 4.0D
                                );
                                for (LivingEntity v : sLvl.getEntitiesOfClass(LivingEntity.class, chasm, e -> e.isAlive() && e != sOwner)) {
                                    if (sOwner != null && SpellExecutionEngine.isFriendlyAllyForOasis(sOwner, v)) continue;
                                    if (sOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, sOwner, sSafe)) continue;

                                    if (sEl <= 70) { // 3.5s (70t) brittle permafrost root!
                                        v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 6));
                                        v.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 25, 0));
                                    }
                                    EntityHelper.getPersistentData(v).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 25L);
                                    v.setTicksFrozen(v.getTicksRequiredToFreeze() + 70);
                                    Vec3 inward = sZone.center.subtract(v.position()).scale(0.26D);
                                    v.setDeltaMovement(inward.x, Math.min(-0.16D, v.getDeltaMovement().y), inward.z);
                                    v.hasImpulse = true;
                                    if (sEl == 0) {
                                        SpellCombatReactionEngine.applyTimedAttributeShred(v, sLvl, 3.0D, 0.0D, 160L);
                                    }
                                    if (sEl % 20 == 0) {
                                        float chasmDmg = (2.70F + sTier * 0.68F) * Math.max(0.5F, sZone.apparatusPotency);
                                        SpellCombatReactionEngine.hurtWithRimeMateriaAdvantage(sLvl, v, sOwner, chasmDmg, false, true);
                                        if (sZone.spell != null && sZone.slot != null) {
                                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                                    sLvl, sOwner, v, Vec3.ZERO,
                                                    sZone.spell, sZone.slot, sZone.fallbackMateria, sZone.apparatusPotency * 0.55F
                                            );
                                        }
                                    }
                                }
                                if (sEl % 5 == 0) {
                                    sLvl.sendParticles(ParticleTypes.SNOWFLAKE, sZone.center.x, sZone.center.y + 0.3D, sZone.center.z, 18, 1.6D, 0.3D, 1.6D, 0.04D);
                                }
                                return true;
                            }
                    ));
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 0.82F);
                    triggersLeft[0]--;
                    rearmCooldown[0] = 95;
                    return triggersLeft[0] > 0;
                }
        ).markDisarmableGlyph();
        SpellLingeringZoneManager.registerZone(level, glyphZone);
        return true;
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 7 — SUB-ZERO BLIZZARDING RIME CONE (CONE / SPRAY)
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
        // Section 6.42 Sub-Zero Blizzarding Rime Cone:
        // Continuous 60° blizzard cone; deals continuous Rime Materia damage, extinguishes all fires and torches,
        // freezes water into packed ice, knocks foes back 3m, and applies Hypothermic Numbness (Slowness IV,
        // -50% mob attack speed, and 25.0s / 500t AI pathfinding disruption)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        Vec3 dirNorm = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();

        SpellWorldVfxManager.broadcastVortexCone(
                level, caster.getEyePosition().subtract(0.0D, 0.2D, 0.0D), dirNorm, coneRange,
                EssenceType.RIME, EssenceType.FROZEN, tier
        );

        float coneDmg = (2.95F + tier * 0.75F) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitTargets) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.clearFire();
            hit.setTicksFrozen(hit.getTicksRequiredToFreeze() + 70);
            hit.setDeltaMovement(dirNorm.x * 0.85D, 0.22D, dirNorm.z * 0.85D); // ~3m knockback!
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 3)); // Slowness IV!
            SpellCombatReactionEngine.applyTimedAttackDebuff(hit, level, 0.0D, 0.50D, 100L);
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 500); // 25.0s AI pathfinding disruption!
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithRimeMateriaAdvantage(level, hit, caster, coneDmg, false, true);
            }
        }

        Vec3 eye = caster.getEyePosition();
        for (double d = 1.5D; d <= coneRange; d += 1.6D) {
            BlockPos step = BlockPos.containing(eye.add(dirNorm.scale(d)));
            for (BlockPos p : BlockPos.betweenClosed(step.offset(-1, -2, -1), step.offset(1, 1, 1))) {
                BlockState bs = level.getBlockState(p);
                if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
                if (bs.is(Blocks.WATER) && level.getFluidState(p).isSource()) {
                    level.setBlockAndUpdate(p, Blocks.PACKED_ICE.defaultBlockState());
                } else if (bs.getBlock() instanceof BaseFireBlock) {
                    level.removeBlock(p, false);
                } else if (bs.getBlock() instanceof CampfireBlock && bs.hasProperty(CampfireBlock.LIT) && bs.getValue(CampfireBlock.LIT)) {
                    level.setBlockAndUpdate(p, bs.setValue(CampfireBlock.LIT, false));
                } else if (bs.is(Blocks.TORCH) || bs.is(Blocks.WALL_TORCH) || bs.is(Blocks.SOUL_TORCH) || bs.is(Blocks.SOUL_WALL_TORCH)) {
                    level.destroyBlock(p, true);
                }
            }
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.90F, 0.72F);
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 8 — GLACIAL RIME CATACLYSMIC BLIZZARD (SKYFALL)
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
        // Section 6.42 Glacial Rime Cataclysmic Blizzard (Extended 18s Duration, Tier 3 Trinity Power):
        // 18.0s (360t) blizzard & hail of 10 monolithic 3D hoarfrost stalactite & cryogenic ice boulder pseudoblock
        // columns across a 12m+ radius + 8.0s (160t) lingering permafrost (520t total); slams mobs downward, shatters
        // 5 armor points, inflicts 6.0s (120t) freezing stasis, slows enemy movement speed by 60% across the entire
        // 26.0s span, and materializes temporary permafrost sheets once the storm finishes before clean restoration!
        int tier = spell.getEffectiveTier();
        double r = Math.max(12.0D, radius);
        BlockPos centerPos = BlockPos.containing(impactCenter);
        double skyH = SpellDeliveryForm.probeCaveSafeSkyfallHeight(level, centerPos, tier);

        SpellWorldVfxManager.broadcastSkyfallPillar(
                level, impactCenter, r, EssenceType.RIME, EssenceType.FROZEN, 365, tier
        );

        final int stalactiteCount = 10;
        final BlockPos[] stalactiteTargets = new BlockPos[stalactiteCount];
        stalactiteTargets[0] = findGroundSurfaceNear(level, centerPos).above();
        for (int i = 1; i < stalactiteCount; i++) {
            double ang = (Math.PI * 2.0D * (i - 1) / (stalactiteCount - 1.0D)) + level.random.nextDouble() * 0.42D;
            double dist = 0.22D * r + level.random.nextDouble() * (0.60D * r);
            BlockPos raw = BlockPos.containing(impactCenter.x + Math.cos(ang) * dist, impactCenter.y, impactCenter.z + Math.sin(ang) * dist);
            stalactiteTargets[i] = findGroundSurfaceNear(level, raw).above();
        }

        final boolean[] landed = new boolean[stalactiteCount];
        Map<BlockPos, BlockState> savedPermafrost = new HashMap<>();

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 520, level.getGameTime(), spell, slot, EssenceType.RIME, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) {
                        for (Map.Entry<BlockPos, BlockState> e : savedPermafrost.entrySet()) {
                            BlockState cur = srv.getBlockState(e.getKey());
                            if (cur.is(Blocks.PACKED_ICE) || cur.is(Blocks.BLUE_ICE) || cur.is(Blocks.SNOW)) {
                                srv.setBlockAndUpdate(e.getKey(), e.getValue());
                            }
                        }
                        return false;
                    }

                    boolean stormActive = elapsed < 360;
                    int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;

                    if (stormActive) {
                        for (int mIdx = 0; mIdx < stalactiteCount; mIdx++) {
                            int impactTick = 24 + mIdx * 34;
                            BlockPos strikePos = stalactiteTargets[mIdx];
                            boolean tallStalactite = (mIdx % 2 == 0);
                            BlockState pseudoState = tallStalactite
                                    ? Blocks.BLUE_ICE.defaultBlockState()
                                    : Blocks.PACKED_ICE.defaultBlockState();
                            int colHeight = tallStalactite ? 2 : 1;

                            if (elapsed == impactTick - 14) {
                                int holdTicks = Math.max(8, 360 - impactTick);
                                SpellWorldVfxManager.broadcastSkyfallPseudoblockColumn(
                                        srv,
                                        new Vec3(strikePos.getX() + 0.5D, strikePos.getY() + skyH, strikePos.getZ() + 0.5D),
                                        strikePos,
                                        pseudoState,
                                        colHeight,
                                        14,
                                        holdTicks,
                                        EssenceType.RIME,
                                        zTier
                                );
                            } else if (elapsed == impactTick) {
                                landed[mIdx] = true;
                                Vec3 hitVec = new Vec3(strikePos.getX() + 0.5D, strikePos.getY(), strikePos.getZ() + 0.5D);
                                LivingEntity owner = zone.resolveCaster(srv);
                                boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                                float boulderDmg = (4.55F + zTier * 1.15F) * Math.max(0.5F, zone.apparatusPotency);

                                SpellWorldVfxManager.broadcastProjectileImpact(
                                        srv, hitVec.add(0.0D, 0.25D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                                        EssenceType.RIME, EssenceType.FROZEN, 2.35F, zTier
                                );

                                AABB blastBox = new AABB(
                                        hitVec.x - 4.8D, hitVec.y - 2.0D, hitVec.z - 4.8D,
                                        hitVec.x + 4.8D, hitVec.y + 4.8D, hitVec.z + 4.8D
                                );
                                for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, blastBox, LivingEntity::isAlive)) {
                                    if (foe == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                                    if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) continue;

                                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 5.0D, 0.0D, 180L);
                                    foe.setTicksFrozen(foe.getTicksRequiredToFreeze() + 140);
                                    foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 120, 0));
                                    foe.setDeltaMovement(foe.getDeltaMovement().x * 0.25D, -0.45D, foe.getDeltaMovement().z * 0.25D);
                                    foe.hasImpulse = true;
                                    SpellCombatReactionEngine.hurtWithRimeMateriaAdvantage(srv, foe, owner, boulderDmg, false, true);
                                }

                                srv.sendParticles(ParticleTypes.SNOWFLAKE, hitVec.x, hitVec.y + 0.8D, hitVec.z, 24, 1.2D, 0.6D, 1.2D, 0.06D);
                                srv.sendParticles(
                                        new BlockParticleOption(ParticleTypes.BLOCK, pseudoState),
                                        hitVec.x, hitVec.y + 0.6D, hitVec.z, 20, 1.0D, 0.5D, 1.0D, 0.09D
                                );
                                srv.playSound(null, strikePos, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.90F, 0.72F);
                            }
                            if (landed[mIdx]) {
                                SpellWorldVfxManager.repelEntitiesFromPseudoblockColumn(srv, strikePos, colHeight, false);
                            }
                        }
                    } else if (elapsed == 360) {
                        // Storm complete: materialize real temporary permafrost sheets & spires for the 8.0s (160t) after-chill!
                        SpellWorldVfxManager.broadcastArcaneSeal(
                                srv, zone.center.add(0.0D, 0.04D, 0.0D), zone.radius,
                                EssenceType.RIME, EssenceType.FROZEN, 160, zTier
                        );
                        for (int mIdx = 0; mIdx < stalactiteCount; mIdx++) {
                            BlockPos strikePos = stalactiteTargets[mIdx];
                            BlockPos ground = strikePos.below();
                            BlockState gs = srv.getBlockState(ground);
                            if ((gs.is(BlockTags.DIRT) || gs.is(Blocks.STONE) || gs.is(Blocks.WATER) || gs.is(Blocks.SAND))
                                    && !savedPermafrost.containsKey(ground)
                                    && !SpellExecutionEngine.isProtectedBlockOrContainer(srv, ground, gs)) {
                                savedPermafrost.put(ground.immutable(), gs);
                                srv.setBlockAndUpdate(ground, (mIdx % 2 == 0 ? Blocks.BLUE_ICE : Blocks.PACKED_ICE).defaultBlockState());
                            }
                            for (BlockPos p : BlockPos.betweenClosed(strikePos.offset(-2, 0, -2), strikePos.offset(2, 0, 2))) {
                                BlockState cur = srv.getBlockState(p);
                                if (cur.isAir()
                                        && Blocks.SNOW.defaultBlockState().canSurvive(srv, p)
                                        && !savedPermafrost.containsKey(p)
                                        && !SpellExecutionEngine.isProtectedBlockOrContainer(srv, p, cur)) {
                                    savedPermafrost.put(p.immutable(), cur);
                                    srv.setBlockAndUpdate(p, Blocks.SNOW.defaultBlockState());
                                }
                            }
                        }
                    }

                    if (elapsed % 6 == 0) {
                        srv.sendParticles(ParticleTypes.SNOWFLAKE, zone.center.x, zone.center.y + (stormActive ? 3.8D : 0.3D), zone.center.z,
                                28, zone.radius * 0.55D, stormActive ? 2.0D : 0.2D, zone.radius * 0.55D, 0.06D);
                    }
                    if (elapsed % 16 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        float pulseDmg = (3.40F + zTier * 0.85F) * Math.max(0.5F, zone.apparatusPotency);
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
                                foe.setTicksFrozen(foe.getTicksRequiredToFreeze() + 120);
                                foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 120, 0));
                                foe.setDeltaMovement(foe.getDeltaMovement().x * 0.3D, -0.38D, foe.getDeltaMovement().z * 0.3D);
                                foe.hasImpulse = true;
                                if (zone.spell != null && zone.slot != null) {
                                    SpellExecutionEngine.deliverSubSpellOnEntity(
                                            srv, owner, foe, new Vec3(0.0D, -1.0D, 0.0D),
                                            zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.45F
                                    );
                                }
                                SpellCombatReactionEngine.hurtWithRimeMateriaAdvantage(srv, foe, owner, pulseDmg, false, true);
                            }
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    // -------------------------------------------------------------------------
    // FORM MUTATION 9 — GLACIAL RIME APEX EIDOLON SUITE (SUMMON / EIDOLON)
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
        tag.putString("SpellEidolonMateria", EssenceType.RIME.name());
        tag.putInt("SpellEidolonTintRGB", HOARFROST_RIME_HEX_COLOR);
        SpellWorldVfxManager.broadcastSummonPillar(
                level, spawnPos, Math.max(1.5D, radius), EssenceType.RIME, EssenceType.FROZEN, spell.getEffectiveTier()
        );
        level.sendParticles(ParticleTypes.SNOWFLAKE, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 28, 0.45D, 0.55D, 0.45D, 0.05D);
    }

    // =========================================================================
    // PART 3: CORE EFFECT MUTATIONS — BLINK, TRANSMUTE & AFFLICTION
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Section 6.42 Permafrost Glacial Slide:
        // Long-distance rapid sub-zero ice slide up to 18m (#6395B1 / #DCF0FF), leaving a trail of packed/blue ice
        // behind the caster, forcefully knocking aside mobs in the trajectory, and granting Frost Walker & +30%
        // movement speed for 6.0s (120t)!
        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.RIME, potencyTier);

        Vec3 diff = arrivalPos.subtract(departurePos);
        int steps = Math.max(4, (int) diff.length());
        Map<BlockPos, BlockState> trail = new HashMap<>();
        float slideDmg = 2.40F + potencyTier * 0.65F;

        for (int i = 0; i <= steps; i++) {
            Vec3 p = departurePos.add(diff.scale((double) i / steps));
            BlockPos below = BlockPos.containing(p).below();
            BlockState bs = level.getBlockState(below);
            if ((bs.is(BlockTags.DIRT) || bs.is(Blocks.STONE) || bs.is(Blocks.SAND) || bs.is(Blocks.WATER))
                    && !trail.containsKey(below)
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, below, bs)) {
                trail.put(below.immutable(), bs);
                level.setBlockAndUpdate(below, (i % 3 == 0 ? Blocks.BLUE_ICE : Blocks.PACKED_ICE).defaultBlockState());
            }
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, new AABB(p, p).inflate(1.65D), e -> e.isAlive() && e != caster)) {
                if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
                Vec3 delta = foe.position().subtract(p);
                Vec3 side = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.18D) : new Vec3(0.0D, 0.30D, 0.0D);
                foe.setDeltaMovement(side.x, 0.28D, side.z);
                foe.hasImpulse = true;
                foe.setTicksFrozen(foe.getTicksRequiredToFreeze() + 60);
                SpellCombatReactionEngine.hurtWithRimeMateriaAdvantage(level, foe, caster, slideDmg, false, true);
            }
        }
        if (!trail.isEmpty()) {
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, arrivalPos, 2.0D, 120, level.getGameTime(), null, null, EssenceType.RIME, 1.0F,
                    (srv, zone, elapsed) -> {
                        if (elapsed < zone.maxDurationTicks) return true;
                        for (Map.Entry<BlockPos, BlockState> e : trail.entrySet()) {
                            BlockState cur = srv.getBlockState(e.getKey());
                            if (cur.is(Blocks.PACKED_ICE) || cur.is(Blocks.BLUE_ICE)) {
                                srv.setBlockAndUpdate(e.getKey(), e.getValue());
                            }
                        }
                        return false;
                    }
            ));
        }

        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_TAIGA_FROST_WALKER_UNTIL, level.getGameTime() + 120L);
        caster.addEffect(new MobEffectInstance(MobEffects.SPEED, 120, 1));
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 0.0D, 0.0D, 0.0D, 0.30D, 120L);
        level.sendParticles(ParticleTypes.SNOWFLAKE, arrivalPos.x, arrivalPos.y + 0.9D, arrivalPos.z, 24, 0.4D, 0.45D, 0.4D, 0.05D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.90F, 1.25F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Section 6.42 Glacial Cryo-Crystallization Transmute:
        // Freezes water & flowing water into Packed Ice or Blue Ice; converts lava into Obsidian or Cryo-Magma Slag
        // (Crying Obsidian); freezes dirt/grass into Permafrost (Packed Ice / Coarse Dirt); turns mud into Clay;
        // crystallizes sand into Frosted Quartz / Calcite; and condenses atmospheric water vapor directly into
        // drinkable water or ice blocks!
        boolean didAny = false;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.5D), ItemEntity::isAlive)) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) {
                didAny = true;
            }
        }
        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            if (didAny) refundBatchDiscount(level, caster, potencyTier);
            return didAny;
        }
        if (state.is(Blocks.WATER)) {
            level.setBlockAndUpdate(pos, level.random.nextBoolean() ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.BLUE_ICE.defaultBlockState());
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.LAVA)) {
            level.setBlockAndUpdate(pos, level.random.nextBoolean() ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.CRYING_OBSIDIAN.defaultBlockState());
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.MUD) || state.is(Blocks.PACKED_MUD)) {
            level.setBlockAndUpdate(pos, Blocks.CLAY.defaultBlockState());
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
            level.setBlockAndUpdate(pos, level.random.nextBoolean() ? Blocks.QUARTZ_BLOCK.defaultBlockState() : Blocks.CALCITE.defaultBlockState());
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(BlockTags.DIRT)) {
            level.setBlockAndUpdate(pos, level.random.nextBoolean() ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.COARSE_DIRT.defaultBlockState());
            refundBatchDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.isAir()) {
            boolean sneaking = caster != null && caster.isShiftKeyDown();
            level.setBlockAndUpdate(pos, sneaking ? Blocks.WATER.defaultBlockState() : Blocks.ICE.defaultBlockState());
            level.sendParticles(ParticleTypes.SNOWFLAKE, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 10, 0.25D, 0.25D, 0.25D, 0.03D);
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
        if (stack.is(Items.GLASS_BOTTLE)) {
            ItemStack waterBottle = new ItemStack(Items.POTION, stack.getCount());
            waterBottle.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER));
            itemEntity.setItem(waterBottle);
            return true;
        }
        if (stack.is(Items.BUCKET) && stack.getCount() == 1) {
            itemEntity.setItem(new ItemStack(Items.WATER_BUCKET));
            return true;
        }
        if (stack.is(Items.WATER_BUCKET)) {
            itemEntity.setItem(new ItemStack(Items.BLUE_ICE, stack.getCount()));
            return true;
        }
        if (stack.is(Items.LAVA_BUCKET)) {
            itemEntity.setItem(new ItemStack(Items.CRYING_OBSIDIAN, stack.getCount()));
            return true;
        }
        if (stack.is(Items.MUD) || stack.is(Items.PACKED_MUD)) {
            itemEntity.setItem(new ItemStack(Items.CLAY, stack.getCount()));
            return true;
        }
        if (stack.is(Items.SAND) || stack.is(Items.RED_SAND)) {
            itemEntity.setItem(new ItemStack(level.random.nextBoolean() ? Items.QUARTZ_BLOCK : Items.CALCITE, stack.getCount()));
            return true;
        }
        return false;
    }

    private static void refundBatchDiscount(ServerLevel level, @Nullable LivingEntity caster, int tier) {
        if (caster instanceof Player player) {
            CompoundTag pData = EntityHelper.getPersistentData(player);
            long now = level.getGameTime();
            if (pData.getLong(TAG_RIME_TRANSMUTE_BATCH_TICK).orElse(-10L) != now) {
                pData.putLong(TAG_RIME_TRANSMUTE_BATCH_TICK, now);
                long refund = Math.max(1L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25D));
                SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.RIME, refund);
            }
        }
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.42 Sub-Zero Cryo-Numbness Hex:
        // Suppresses victim weapon & armor enchantments while afflicted (enforced in SpellCombatReactionEngine),
        // strips 30% of armor toughness, continually stuns the target for 0.5s (10t) every 3.0s with shivering
        // shockwaves, and inflicts continuous sub-zero freezing Rime Materia damage!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        long now = level.getGameTime();
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.80D, 0.30D * mult), 45L);
        target.setTicksFrozen(target.getTicksRequiredToFreeze() + 70);
        if ((now / 20L) % 3L == 0L) {
            target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 10, 0));
            level.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY(0.6D), target.getZ(), 12, 0.45D, 0.25D, 0.45D, 0.04D);
        }

        LivingEntity caster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        float hexDmg = (1.55F + potencyTier * 0.50F) * mult;
        SpellCombatReactionEngine.hurtWithRimeMateriaAdvantage(level, target, caster, hexDmg, false, true);

        level.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY(0.85D), target.getZ(), 6, 0.25D, 0.35D, 0.25D, 0.02D);
        return true;
    }

    private static void placeTemporaryRimeFrostSheets(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 center,
            int halfSpan,
            int durationTicks
    ) {
        BlockPos cp = BlockPos.containing(center);
        Map<BlockPos, BlockState> placed = new HashMap<>();
        for (BlockPos p : BlockPos.betweenClosed(cp.offset(-halfSpan, -1, -halfSpan), cp.offset(halfSpan, 1, halfSpan))) {
            BlockState cur = level.getBlockState(p);
            if (cur.isAir()
                    && Blocks.SNOW.defaultBlockState().canSurvive(level, p)
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, cur)) {
                placed.put(p.immutable(), cur);
                level.setBlockAndUpdate(p, Blocks.SNOW.defaultBlockState());
            }
        }
        if (!placed.isEmpty()) {
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, center, halfSpan + 1.0D, durationTicks, level.getGameTime(), null, null, EssenceType.RIME, 1.0F,
                    (srv, zone, elapsed) -> {
                        if (elapsed < zone.maxDurationTicks) return true;
                        for (Map.Entry<BlockPos, BlockState> e : placed.entrySet()) {
                            if (srv.getBlockState(e.getKey()).is(Blocks.SNOW)) {
                                srv.setBlockAndUpdate(e.getKey(), e.getValue());
                            }
                        }
                        return false;
                    }
            ));
        }
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
