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
import ddraig.net.entropica.spell.fuel.SpellFuelService;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.spell.vfx.SpellWorldVfxManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Complete per-part Signature Mutation implementation for <b>Soulfire Materia (Ethereal &amp; Paradox Fusion #28)</b>
 * (Spellcrafting System Guide Section 6.29 — Dual Nether {@code #1DF0E5} + Undead {@code #0D6469} [Leans: Nether]).
 */
public final class SoulfireSpellMutationHandler implements MateriaPartMutationHandler {

    /** Canonical Cyan Soulflame (#1DF0E5) primary tint. */
    public static final int SOULFLAME_HEX_COLOR = 0x1DF0E5;

    /** Canonical Soul-Ember Teal (#0D6469) secondary tint. */
    public static final int SOUL_EMBER_TEAL_HEX_COLOR = 0x0D6469;

    private static final String TAG_SOULFIRE_BEAM_LAST_TICK = "SpellSoulfireBeamLastTick";
    private static final String TAG_SOULFIRE_BEAM_TICKS = "SpellSoulfireBeamTicks";

    @Override
    public EssenceType materiaType() {
        return EssenceType.SOULFIRE;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Phantasmal Soul-Chain Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Soulflame Comet";
            case BEAM -> "Phantasmal Soul-Torch Jet";
            case RADIAL_BURST -> "Phantasmal Soul Nova";
            case TOUCH_STRIKE -> "Phantasmal Soul Cleave";
            case AURA -> "Phantasmal Soulfire Shroud";
            case GLYPH -> "Soulflame Pyre Trap";
            case CONE -> "Phantasmal Soul-Torch Wave";
            case SKYFALL -> "Phantasmal Soulfire Tempest";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Spectral Rift Blink";
            case TRANSMUTE, BREAK -> "Nether Ash Transmute";
            case AFFLICTION -> "Phantasmal Soul-Burn Hex";
            default -> "Phantasmal Soulfire " + effect.getDisplayName();
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATION — PHANTASMAL SOUL-CHAIN FLOW
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
        // Section 6.29 Phantasmal Soul-Chain Flow:
        // Ignites each victim in soulfire for 4.0s (80t), heals caster for 15% of damage dealt,
        // and if any chained target dies within 3.0s (60t), triggers a soulburst damaging all other chained mobs!
        long now = level.getGameTime();
        int tier = spell.getEffectiveTier();

        if (fromTarget != null && fromTarget.isAlive()) {
            EntityHelper.getPersistentData(fromTarget).putLong(SpellLingeringZoneManager.TAG_SOULFIRE_CHAIN_BURST_UNTIL, now + 60L);
        }
        toTarget.igniteForSeconds(4);
        CompoundTag data = EntityHelper.getPersistentData(toTarget);
        data.putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, now + 80L);
        data.putLong(SpellLingeringZoneManager.TAG_SOULFIRE_CHAIN_BURST_UNTIL, now + 60L);

        float hopDmg = 1.25F + tier * 0.38F;
        float dealt = SpellCombatReactionEngine.hurtWithSoulfireMateriaAdvantage(level, toTarget, caster, hopDmg, false, true);
        healCasterFraction(caster, dealt, 0.15F);

        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, toTarget.getX(), toTarget.getY(0.8D), toTarget.getZ(), 12, 0.25D, 0.35D, 0.25D, 0.03D);
    }

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.07D, 0.07D, 0.07D, 0.01D);
    }

    // =========================================================================
    // FORM MUTATION 1 — SOULFLAME COMET / SCREAMING SOUL SKULL (PROJECTILE)
    // =========================================================================

    @Override
    public void onProjectileSpawnedFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Section 6.29 Screaming Soul Skull: homes toward living targets!
        projectile.setHomingGuidance(true);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.01D);
        if (projectile.tickCount % 3 == 0) {
            level.sendParticles(ParticleTypes.SOUL, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
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
        detonateSoulflameSkull(level, caster, hitEntity.position(), spell, slot);
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
        detonateSoulflameSkull(level, caster, blockHit.getLocation(), spell, slot);
    }

    private static void detonateSoulflameSkull(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 center,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Section 6.29 Soulflame Comet / Screaming Soul Skull:
        // Detonates on impact into a 3-block soulfire burst dealing Soulfire Materia damage,
        // igniting enemies in unquenchable cyan soulfire for 5.0s (100t), and siphoning 20% of damage dealt!
        int tier = spell.getEffectiveTier();
        long burnUntil = level.getGameTime() + 100L;
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        float burstDmg = (slot != null && slot.hasHarmVerb())
                ? (1.80F + tier * 0.48F)
                : (3.25F + tier * 0.85F);

        SpellWorldVfxManager.broadcastRadialNova(level, center, 3.0D, EssenceType.SOULFIRE, EssenceType.UNDEAD, tier);
        AABB box = new AABB(center.x - 3.0D, center.y - 1.5D, center.z - 3.0D, center.x + 3.0D, center.y + 2.5D, center.z + 3.0D);
        float totalDealt = 0.0F;

        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive() || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;

            foe.igniteForSeconds(5);
            EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, burnUntil);
            totalDealt += SpellCombatReactionEngine.hurtWithSoulfireMateriaAdvantage(level, foe, caster, burstDmg, false, true);
        }

        healCasterFraction(caster, totalDealt, 0.20F);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x, center.y + 0.5D, center.z, 26, 1.2D, 0.5D, 1.2D, 0.05D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 0.88F);
    }

    // =========================================================================
    // FORM MUTATION 2 — PHANTASMAL SOUL-TORCH JET (BEAM)
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
        // Section 6.29 Phantasmal Soul-Torch Jet:
        // Bypasses 30% armor toughness and siphons 20% of channeled damage into caster health & Materia fuel;
        // channeling for 3.0s (60 contact ticks = 15 pulses) triggers a Soul Immolation Burst that staggers
        // enemies and dissolves slain mobs into Soul Sand or Bone Meal!
        int tier = spell.getEffectiveTier();
        long now = level.getGameTime();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.igniteForSeconds(5);
            CompoundTag data = EntityHelper.getPersistentData(hit);
            data.putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, now + 100L);
            SpellCombatReactionEngine.applyPercentageArmorShred(hit, level, 0.0D, 0.30D, 60L);

            if (!slot.hasHarmVerb()) {
                float jetDmg = (1.20F + tier * 0.35F) * Math.max(0.5F, apparatusPotency);
                float dealt = SpellCombatReactionEngine.hurtWithSoulfireMateriaAdvantage(level, hit, caster, jetDmg, false, true);
                healCasterFraction(caster, dealt, 0.20F);
                if (dealt > 0.0F && caster instanceof Player player && player.isAlive()) {
                    SpellCombatReactionEngine.siphonCasterMateriaFuelFromDamage(player, EssenceType.SOULFIRE, tier, dealt, 0.20F);
                }
            }

            long last = data.getLong(TAG_SOULFIRE_BEAM_LAST_TICK).orElse(-100L);
            int accumTicks = (now - last <= 12L) ? data.getInt(TAG_SOULFIRE_BEAM_TICKS).orElse(0) + 4 : 4;
            data.putLong(TAG_SOULFIRE_BEAM_LAST_TICK, now);

            if (accumTicks >= 60) {
                data.putInt(TAG_SOULFIRE_BEAM_TICKS, 0);
                data.putLong(SpellLingeringZoneManager.TAG_SOULFIRE_SOULSAND_MARK_UNTIL, now + 140L);
                hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 3));
                hit.setDeltaMovement(direction.x * 0.72D, 0.38D, direction.z * 0.72D);
                hit.hasImpulse = true;

                float burstDealt = SpellCombatReactionEngine.hurtWithSoulfireMateriaAdvantage(
                        level, hit, caster, (3.40F + tier * 0.88F) * Math.max(0.5F, apparatusPotency), false, true
                );
                healCasterFraction(caster, burstDealt, 0.20F);
                if (burstDealt > 0.0F && caster instanceof Player player && player.isAlive()) {
                    SpellCombatReactionEngine.siphonCasterMateriaFuelFromDamage(player, EssenceType.SOULFIRE, tier, burstDealt, 0.20F);
                }

                SpellWorldVfxManager.broadcastRadialNova(level, hit.position(), 3.4D, EssenceType.SOULFIRE, EssenceType.UNDEAD, tier);
                level.sendParticles(ParticleTypes.SOUL, hit.getX(), hit.getY(0.9D), hit.getZ(), 16, 0.35D, 0.40D, 0.35D, 0.05D);
                level.playSound(null, hit.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 0.78F);
            } else {
                data.putInt(TAG_SOULFIRE_BEAM_TICKS, accumTicks);
            }
        }
    }

    // =========================================================================
    // FORM MUTATION 3 — PHANTASMAL SOUL NOVA (RADIAL BURST)
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
        // Section 6.29 Phantasmal Soul Nova:
        // 6m+ 360-degree ring of howling cyan soulflames (#1DF0E5 / #0D6469); knocks hostiles back 4 blocks,
        // ignites them in underwater-burning soulfire for 5.0s (100t), heals caster for 15% of all burst damage,
        // and converts ground sand into Soul Sand!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.0D, radius);
        long burnUntil = level.getGameTime() + 100L;
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 2.8D, center.z + r);
        float bonusDealt = 0.0F;

        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive() || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;

            Vec3 delta = foe.position().subtract(center);
            Vec3 horiz = new Vec3(delta.x, 0.0D, delta.z);
            Vec3 away = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize().scale(1.08D) : direction.scale(1.08D);
            foe.setDeltaMovement(away.x, 0.34D, away.z);
            foe.hasImpulse = true;
            foe.igniteForSeconds(5);
            EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, burnUntil);

            if (!slot.hasHarmVerb()) {
                float novaDmg = (3.30F + tier * 0.85F) * Math.max(0.5F, apparatusPotency);
                bonusDealt += SpellCombatReactionEngine.hurtWithSoulfireMateriaAdvantage(level, foe, caster, novaDmg, false, true);
            }
        }

        healCasterFraction(caster, bonusDealt, 0.15F);
        convertSandAndMeltIce(level, BlockPos.containing(center), 5, false, false);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x, center.y + 0.8D, center.z, 38, 2.3D, 0.5D, 2.3D, 0.06D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 0.82F);
    }

    // =========================================================================
    // FORM MUTATION 4 — PHANTASMAL SOUL CLEAVE (TOUCH / STRIKE)
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
        // Section 6.29 Phantasmal Soul Cleave:
        // Deals Soulfire Materia damage, ignites target in unquenchable underwater soulflame for 5.0s (100t),
        // siphons 25% of damage dealt back to caster health, and causes slain targets to explode into a 3m soulburst!
        int tier = spell.getEffectiveTier();
        long now = level.getGameTime();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            CompoundTag data = EntityHelper.getPersistentData(hit);
            data.putLong(SpellLingeringZoneManager.TAG_SOULFIRE_CLEAVE_BURST_UNTIL, now + 100L);
            data.putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, now + 100L);
            hit.igniteForSeconds(5);

            if (!slot.hasHarmVerb()) {
                float cleaveDmg = (2.85F + tier * 0.75F) * Math.max(0.5F, apparatusPotency);
                float dealt = SpellCombatReactionEngine.hurtWithSoulfireMateriaAdvantage(level, hit, caster, cleaveDmg, true, true);
                healCasterFraction(caster, dealt, 0.25F);
            }
        }
    }

    // =========================================================================
    // FORM MUTATION 5 — PHANTASMAL SOULFIRE SHROUD (AURA / SELF)
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
        // Section 6.29 Phantasmal Soulfire Shroud:
        // Cloaks caster in cyan soulflames (#1DF0E5 / #0D6469); total fire/lava immunity, continuous Soul Speed
        // (+40% speed on soul sand/soil), melee soulfire ignition + 20% siphon, and pacifies vanilla Nether mobs!
        int dur = 200 + spell.getEffectiveTier() * 40;
        long until = level.getGameTime() + dur;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_SOULFIRE_SHROUD_UNTIL, until);
        caster.clearFire();
        caster.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, dur, 0, false, true));
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.0F, null);

        for (Mob mob : level.getEntitiesOfClass(Mob.class, caster.getBoundingBox().inflate(16.0D))) {
            if ((mob instanceof AbstractPiglin || mob instanceof Blaze || mob instanceof WitherSkeleton || mob instanceof ZombifiedPiglin)
                    && mob.getLastHurtByMob() != caster) {
                mob.setTarget(null);
                mob.setAggressive(false);
                SpellLingeringZoneManager.applyPacified(mob, dur);
            }
        }

        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, caster.getX(), caster.getY(0.9D), caster.getZ(), 24, 0.45D, 0.55D, 0.45D, 0.04D);
        level.playSound(null, caster.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 0.90F, 1.05F);
    }

    // =========================================================================
    // FORM MUTATION 6 — SOULFLAME PYRE TRAP (GLYPH / TRAP)
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
        // Section 6.29 Soulflame Pyre Trap:
        // Disarmable Single/Repeating cyan rune (1.0s arming delay) erupting into a 4m vertical column of soulfire:
        // launches mobs 6m upward, ignites enemies for 6.0s (120t), roots them with clinging soul hands for 2.5s (50t)
        // as they hit the pyre, heals caster for 20% of damage dealt, and turns a 3x3 ground area into Soul Sand!
        int tier = spell.getEffectiveTier();
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snapped = new Vec3(surface.getX() + 0.5D, surface.above().getY(), surface.getZ() + 0.5D);
        double pyreRad = Math.max(4.0D, radius);

        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxTriggers = repeating ? 3 : 1;
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE / 4 : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snapped.add(0.0D, 0.03D, 0.0D), pyreRad, EssenceType.SOULFIRE, EssenceType.UNDEAD, Math.min(240, maxDuration), tier
        );
        level.playSound(null, surface.above(), SoundEvents.SOUL_SAND_PLACE, SoundSource.PLAYERS, 0.85F, 0.92F);

        final int[] triggersLeft = {maxTriggers};
        final int[] rearmCooldown = {0};

        SpellLingeringZoneManager.ActiveSpellZone glyphZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snapped, pyreRad, maxDuration, level.getGameTime(), spell, slot, EssenceType.SOULFIRE, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    if (elapsed > 0 && elapsed % 200 == 0) {
                        int rem = zone.maxDurationTicks >= 1_000_000_000 ? 240 : (int) Math.min(240L, zone.maxDurationTicks - elapsed);
                        SpellWorldVfxManager.broadcastArcaneSeal(
                                srv, zone.center.add(0.0D, 0.03D, 0.0D), zone.radius, EssenceType.SOULFIRE, EssenceType.UNDEAD, rem, tier
                        );
                    }
                    if (elapsed < 20) return true;
                    if (rearmCooldown[0] > 0) {
                        rearmCooldown[0]--;
                        return true;
                    }

                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                    AABB triggerBox = new AABB(
                            zone.center.x - 2.8D, zone.center.y - 1.0D, zone.center.z - 2.8D,
                            zone.center.x + 2.8D, zone.center.y + 2.2D, zone.center.z + 2.8D
                    );
                    List<LivingEntity> triggers = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e ->
                            e.isAlive() && e != owner && !SpellExecutionEngine.isFriendlyAllyForOasis(owner, e)
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe)));
                    if (triggers.isEmpty()) return true;

                    SpellWorldVfxManager.broadcastGeyserPlume(
                            srv, zone.center, 2.2D, 6.0D, EssenceType.SOULFIRE, EssenceType.UNDEAD, 55, tier
                    );

                    long burnUntil = srv.getGameTime() + 120L;
                    Set<UUID> pendingClutch = new HashSet<>();
                    float bonusDealt = 0.0F;
                    AABB colBox = new AABB(
                            zone.center.x - zone.radius, zone.center.y - 1.5D, zone.center.z - zone.radius,
                            zone.center.x + zone.radius, zone.center.y + 4.2D, zone.center.z + zone.radius
                    );
                    for (LivingEntity victim : srv.getEntitiesOfClass(LivingEntity.class, colBox, LivingEntity::isAlive)) {
                        if (victim == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, victim)) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(victim, owner, wardSafe)) continue;

                        victim.igniteForSeconds(6);
                        EntityHelper.getPersistentData(victim).putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, burnUntil);
                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, victim, new Vec3(0.0D, 1.0D, 0.0D),
                                    zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency
                            );
                        }
                        if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                            float pyreDmg = (3.40F + tier * 0.88F) * Math.max(0.5F, zone.apparatusPotency);
                            bonusDealt += SpellCombatReactionEngine.hurtWithSoulfireMateriaAdvantage(srv, victim, owner, pyreDmg, false, true);
                        }
                        // Launch 6 blocks upward first, then root with clinging soul hands for 2.5s (50t) upon landing!
                        victim.setDeltaMovement(victim.getDeltaMovement().x * 0.25D, 1.04D, victim.getDeltaMovement().z * 0.25D);
                        victim.hasImpulse = true;
                        pendingClutch.add(victim.getUUID());
                    }
                    healCasterFraction(owner, bonusDealt, 0.20F);

                    // Convert 3x3 ground area into Soul Sand safely!
                    BlockPos base = BlockPos.containing(zone.center).below();
                    for (BlockPos p : BlockPos.betweenClosed(base.offset(-1, 0, -1), base.offset(1, 0, 1))) {
                        BlockState bs = srv.getBlockState(p);
                        if ((bs.is(BlockTags.SAND) || bs.is(BlockTags.DIRT)) && !SpellExecutionEngine.isProtectedBlockOrContainer(srv, p, bs)) {
                            srv.setBlockAndUpdate(p, Blocks.SOUL_SAND.defaultBlockState());
                        }
                    }

                    // Clinging soul hands snare: roots launched mobs fast for 2.5s (50t) once they descend back onto the pyre!
                    if (!pendingClutch.isEmpty()) {
                        SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                                owner, zone.center, zone.radius + 1.5D, 90, srv.getGameTime(),
                                null, null, EssenceType.SOULFIRE, 1.0F,
                                (cLvl, cZone, cEl) -> {
                                    if (cEl > cZone.maxDurationTicks || pendingClutch.isEmpty()) return false;
                                    if (cEl < 12) return true;
                                    for (UUID id : new HashSet<>(pendingClutch)) {
                                        Entity ent = cLvl.getEntity(id);
                                        if (!(ent instanceof LivingEntity liv) || !liv.isAlive()) {
                                            pendingClutch.remove(id);
                                            continue;
                                        }
                                        if (liv.onGround() || cEl >= 42) {
                                            applyClingingSoulRoot(cLvl, liv, 50);
                                            cLvl.sendParticles(ParticleTypes.SOUL, liv.getX(), liv.getY(0.25D), liv.getZ(), 8, 0.25D, 0.15D, 0.25D, 0.02D);
                                            pendingClutch.remove(id);
                                        }
                                    }
                                    return !pendingClutch.isEmpty();
                                }
                        ));
                    }

                    srv.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, zone.center.x, zone.center.y + 2.0D, zone.center.z, 34, 0.9D, 1.8D, 0.9D, 0.06D);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 0.78F);

                    triggersLeft[0]--;
                    rearmCooldown[0] = 60;
                    return triggersLeft[0] > 0;
                }
        ).markDisarmableGlyph();
        SpellLingeringZoneManager.registerZone(level, glyphZone);
        return true;
    }

    // =========================================================================
    // FORM MUTATION 7 — PHANTASMAL SOUL-TORCH WAVE (CONE / SPRAY)
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
        // Section 6.29 Phantasmal Soul-Torch Wave:
        // Wide 60° blast of roaring cyan soulflames; pushes enemies 4 blocks back, sets mobs ablaze in
        // unquenchable soulfire for 6.0s (120t, ignoring shields and burning underwater), converts sand into
        // Soul Sand, melts snow/ice, and heals caster for 15% of damage dealt!
        int tier = spell.getEffectiveTier();
        long burnUntil = level.getGameTime() + 120L;
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        float bonusDealt = 0.0F;

        for (LivingEntity hit : hitTargets) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.setDeltaMovement(direction.x * 1.08D, 0.30D, direction.z * 1.08D);
            hit.hasImpulse = true;
            hit.igniteForSeconds(6);
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, burnUntil);

            if (hit.isBlocking()) {
                hit.stopUsingItem();
                bonusDealt += SpellCombatReactionEngine.hurtWithSoulfireMateriaAdvantage(
                        level, hit, caster, (2.60F + tier * 0.70F) * Math.max(0.5F, apparatusPotency), false, true
                );
            } else if (!slot.hasHarmVerb()) {
                bonusDealt += SpellCombatReactionEngine.hurtWithSoulfireMateriaAdvantage(
                        level, hit, caster, (2.75F + tier * 0.72F) * Math.max(0.5F, apparatusPotency), false, true
                );
            }
        }

        healCasterFraction(caster, bonusDealt, 0.15F);
        int steps = Math.min(8, (int) Math.ceil(coneRange));
        for (int s = 1; s <= steps; s++) {
            convertSandAndMeltIce(level, BlockPos.containing(caster.position().add(direction.scale(s))), 2, true, false);
        }
    }

    // =========================================================================
    // FORM MUTATION 8 — PHANTASMAL SOULFIRE TEMPEST (SKYFALL)
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
        // Section 6.29 Phantasmal Soulfire Tempest:
        // Howling spectral rift for 10.0s (200t, 14m+ radius) raining cyan soul-embers and blue fireballs;
        // continuously burns enemies in unquenchable soulfire, heals caster for 15% of all damage dealt,
        // melts snow/ice, and converts sand into Soul Sand and Soul Soil!
        int tier = spell.getEffectiveTier();
        double r = Math.max(14.0D, radius);

        SpellWorldVfxManager.broadcastSkyfallPillar(
                level, impactCenter, r, EssenceType.SOULFIRE, EssenceType.UNDEAD, 200, tier
        );
        level.playSound(null, BlockPos.containing(impactCenter), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.1F, 0.75F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 200, level.getGameTime(), spell, slot, EssenceType.SOULFIRE, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 6 == 0) {
                        srv.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                                zone.center.x, zone.center.y + 4.5D, zone.center.z,
                                20, zone.radius * 0.55D, 1.3D, zone.radius * 0.55D, 0.05D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        long burnUntil = srv.getGameTime() + 80L;
                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 6.0D, zone.center.z + zone.radius
                        );
                        float bonusDealt = 0.0F;
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            if (v == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, v)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, wardSafe)) continue;

                            v.igniteForSeconds(4);
                            EntityHelper.getPersistentData(v).putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, burnUntil);
                            if (zone.spell != null && zone.slot != null) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(
                                        srv, owner, v, new Vec3(0.0D, -1.0D, 0.0D),
                                        zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.35F
                                );
                            }
                            if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                                float tempestDmg = (2.15F + tier * 0.55F) * Math.max(0.5F, zone.apparatusPotency);
                                bonusDealt += SpellCombatReactionEngine.hurtWithSoulfireMateriaAdvantage(srv, v, owner, tempestDmg, false, true);
                            }
                        }
                        healCasterFraction(owner, bonusDealt, 0.15F);
                        convertSandAndMeltIce(srv, BlockPos.containing(zone.center), Math.min(8, (int) Math.ceil(zone.radius * 0.55D)), true, true);
                    }
                    return true;
                }
        ));
        return true;
    }

    // =========================================================================
    // FORM MUTATION 9 — SOULFLAME EIDOLON SUITE (SUMMON)
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
        CompoundTag tag = EntityHelper.getPersistentData(summonEntity);
        tag.putString("SpellEidolonMateria", EssenceType.SOULFIRE.name());
        tag.putInt("SpellEidolonTintRGB", SOULFLAME_HEX_COLOR);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 24, 0.45D, 0.55D, 0.45D, 0.05D);
    }

    // =========================================================================
    // PART 3: CORE EFFECT MUTATIONS — BLINK, TRANSMUTE & AFFLICTION
    // =========================================================================

    @Override
    public void onBlinkCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 departurePos,
            Vec3 arrivalPos,
            int potencyTier
    ) {
        // Section 6.29 Spectral Rift Blink:
        // Teleports 12 blocks, leaving an ethereal blue flame puddle that speeds up allies by +30% for 4.0s (80t)!
        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.SOULFIRE, potencyTier);
        SpellCombatReactionEngine.applyTimedMovementSpeedBoost(caster, level, 0.30D, 80L);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, departurePos, 3.0D, 80, level.getGameTime(), null, null, EssenceType.SOULFIRE, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                                zone.center.x, zone.center.y + 0.16D, zone.center.z, 9, 1.2D, 0.08D, 1.2D, 0.02D);
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB box = new AABB(
                                zone.center.x - 3.0D, zone.center.y - 1.0D, zone.center.z - 3.0D,
                                zone.center.x + 3.0D, zone.center.y + 2.0D, zone.center.z + 3.0D
                        );
                        for (LivingEntity ent : srv.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                            if (ent == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, ent)) {
                                SpellCombatReactionEngine.applyTimedMovementSpeedBoost(ent, srv, 0.30D, 80L);
                                ent.addEffect(new MobEffectInstance(MobEffects.SPEED, 80, 1, true, true));
                            }
                        }
                    }
                    return true;
                }
        ));
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 0.95F, 1.15F);
    }

    @Override
    public boolean onTransmuteBlock(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            BlockState state,
            int potencyTier
    ) {
        // Section 6.29 Nether Ash Transmute:
        // Dissolves stone into Netherrack and basalt into Magma Blocks (+ sand -> Soul Sand, dirt -> Soul Soil,
        // torches -> Soul Torches, campfires -> Soul Campfires, flowers -> Soul Flame Orchid)!
        if (state.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return false;
        }
        if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE)
                || state.is(Blocks.DEEPSLATE) || state.is(Blocks.COBBLED_DEEPSLATE)) {
            level.setBlockAndUpdate(pos, Blocks.NETHERRACK.defaultBlockState());
            maybeGrantSoulfireBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.BASALT) || state.is(Blocks.SMOOTH_BASALT) || state.is(Blocks.POLISHED_BASALT)) {
            level.setBlockAndUpdate(pos, Blocks.MAGMA_BLOCK.defaultBlockState());
            maybeGrantSoulfireBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(BlockTags.SAND)) {
            level.setBlockAndUpdate(pos, Blocks.SOUL_SAND.defaultBlockState());
            maybeGrantSoulfireBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(BlockTags.DIRT) && !state.is(Blocks.SOUL_SOIL)) {
            level.setBlockAndUpdate(pos, Blocks.SOUL_SOIL.defaultBlockState());
            maybeGrantSoulfireBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.TORCH)) {
            level.setBlockAndUpdate(pos, Blocks.SOUL_TORCH.defaultBlockState());
            maybeGrantSoulfireBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.CAMPFIRE)) {
            boolean lit = state.hasProperty(CampfireBlock.LIT) && state.getValue(CampfireBlock.LIT);
            level.setBlockAndUpdate(pos, Blocks.SOUL_CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, lit));
            maybeGrantSoulfireBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(BlockTags.SMALL_FLOWERS) && !state.is(ModBlocks.SOUL_FLAME_ORCHID.get())) {
            level.setBlockAndUpdate(pos, ModBlocks.SOUL_FLAME_ORCHID.get().defaultBlockState());
            maybeGrantSoulfireBatchTransmuteDiscount(level, caster, potencyTier);
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
        ItemStack in = itemEntity.getItem();
        if (in.is(Items.STONE) || in.is(Items.COBBLESTONE) || in.is(Items.DEEPSLATE) || in.is(Items.COBBLED_DEEPSLATE)) {
            itemEntity.setItem(new ItemStack(Items.NETHERRACK, in.getCount()));
            maybeGrantSoulfireBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (in.is(Items.BASALT) || in.is(Items.SMOOTH_BASALT) || in.is(Items.POLISHED_BASALT)) {
            itemEntity.setItem(new ItemStack(Items.MAGMA_BLOCK, in.getCount()));
            maybeGrantSoulfireBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (in.is(Items.SAND) || in.is(Items.RED_SAND)) {
            itemEntity.setItem(new ItemStack(Items.SOUL_SAND, in.getCount()));
            maybeGrantSoulfireBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (in.is(Items.TORCH)) {
            itemEntity.setItem(new ItemStack(Items.SOUL_TORCH, in.getCount()));
            maybeGrantSoulfireBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.29 Phantasmal Soul-Burn Hex:
        // Persistent milk-immune spectral curse (quenched via Water/Restoration magic or caster defeat):
        // permanently consumes hunger & stamina, inflicts Slowness II and Weakness II, and burns underwater
        // with unquenchable cyan soulfire!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.igniteForSeconds(3);
        EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, level.getGameTime() + 45L);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 1, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 45, 1, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 45, 1, true, true));
        if (target instanceof Player player) {
            player.getFoodData().addExhaustion(1.5F * mult);
        }

        LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        float hexDmg = (1.50F + potencyTier * 0.50F) * mult;
        SpellCombatReactionEngine.hurtWithSoulfireMateriaAdvantage(level, target, hexCaster, hexDmg, false, true);

        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY(0.8D), target.getZ(), 6, 0.25D, 0.35D, 0.25D, 0.02D);
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
            target.clearFire();
            EntityHelper.getPersistentData(target).remove(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL);
            float heal = 3.1F + potencyTier * 0.95F;
            SpellCombatReactionEngine.notifyMagicalHeal(target, heal);
            target.heal(heal);
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private static void healCasterFraction(@Nullable LivingEntity caster, float damageDealt, float fraction) {
        if (caster == null || !caster.isAlive() || damageDealt <= 0.0F || fraction <= 0.0F) {
            return;
        }
        float heal = damageDealt * fraction;
        SpellCombatReactionEngine.notifyMagicalHeal(caster, heal);
        caster.heal(heal);
    }

    private static void applyClingingSoulRoot(ServerLevel level, LivingEntity target, int ticks) {
        SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, 0.95D, ticks);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, ticks, 6));
        target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), ticks, 0));
        EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, level.getGameTime() + ticks);
        SpellExecutionEngine.applySyncedMotion(target, new Vec3(0.0D, Math.min(0.0D, target.getDeltaMovement().y), 0.0D));
    }

    private static void convertSandAndMeltIce(
            ServerLevel level,
            BlockPos center,
            int rad,
            boolean meltIce,
            boolean includeSoulSoil
    ) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-rad, -1, -rad), center.offset(rad, 1, rad))) {
            BlockState bs = level.getBlockState(p);
            if (bs.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
            if (bs.is(BlockTags.SAND)) {
                BlockState next = (includeSoulSoil && level.random.nextBoolean())
                        ? Blocks.SOUL_SOIL.defaultBlockState()
                        : Blocks.SOUL_SAND.defaultBlockState();
                level.setBlockAndUpdate(p, next);
            } else if (meltIce && (bs.is(Blocks.ICE) || bs.is(Blocks.PACKED_ICE) || bs.is(Blocks.FROSTED_ICE) || bs.is(Blocks.SNOW_BLOCK))) {
                level.setBlockAndUpdate(p, Blocks.WATER.defaultBlockState());
            } else if (meltIce && bs.is(Blocks.SNOW)) {
                level.removeBlock(p, false);
            }
        }
    }

    private static void maybeGrantSoulfireBatchTransmuteDiscount(ServerLevel level, @Nullable LivingEntity caster, int tier) {
        if (!(caster instanceof Player player)) return;
        CompoundTag pData = EntityHelper.getPersistentData(player);
        long now = level.getGameTime();
        long prevTick = pData.getLong("SpellSoulfireBatchTick").orElse(-20L);
        int count = (prevTick == now) ? pData.getInt("SpellSoulfireBatchCount").orElse(0) + 1 : 1;
        pData.putLong("SpellSoulfireBatchTick", now);
        pData.putInt("SpellSoulfireBatchCount", count);
        if (count == 2) {
            long refund = Math.max(1L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25D));
            SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.SOULFIRE, refund);
        }
    }

    private static BlockPos findGroundSurfaceNear(ServerLevel level, BlockPos start) {
        for (int dy = 2; dy >= -5; dy--) {
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
}
