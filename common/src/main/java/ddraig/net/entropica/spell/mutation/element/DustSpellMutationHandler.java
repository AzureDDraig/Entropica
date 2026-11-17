package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.ScribedChalkBlock;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
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
import ddraig.net.entropica.spell.fuel.SpellFuelService;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Complete per-part Signature Mutation implementation for <b>Dust Materia (Tier 1 Fusion #17: Air + Earth)</b>
 * (Spellcrafting System Guide Section 6.18).
 * <p>
 * Canonical Palette: Sandstone Dun ({@code #C2B28F}) &amp; Fine Abrasive Chalk Ivory ({@code #F2ECE1}).
 */
public final class DustSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int DUST_STALKER_HEX_COLOR = 0xC2B28F;
    public static final int CHALK_WHITE_HEX_COLOR = 0xF2ECE1;

    @Override
    public EssenceType materiaType() {
        return EssenceType.DUST;
    }

    // =========================================================================
    // CANONICAL SIGNATURE TITLES & DESCRIPTIONS (SECTION 6.18)
    // =========================================================================

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Abrasive Sandblaster Chains";
            default -> "Abrasive Dust " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Choking Dust-Pod";
            case BEAM -> "Abrasive Sandblasting Jet";
            case RADIAL_BURST -> "Whiteout Powder Nova";
            case TOUCH_STRIKE -> "Choking Smother Strike";
            case AURA -> "Obscuring Chalk Veil";
            case GLYPH -> "Choking Powder Pitfall";
            case CONE -> "Abrasive Sandblasting Deluge";
            case SKYFALL -> "Whiteout Dust Storm Downpour";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Chalk-Step";
            case TRANSMUTE, BREAK -> "Sedimentary Abrasion & Polishing";
            case AFFLICTION -> "Choking Powder Smother Hex";
            case KINETIC -> "Pneumatic Dust-Blast";
            default -> "Abrasive " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveFlowDescription(SpellFlowModifier flow) {
        if (flow == SpellFlowModifier.CHAIN_LEAP) {
            return "Leaping chains jump along swirling chalk dust filaments; each leap kicks up a mini-powder cloud "
                    + "that blinds the struck mob for 1.5s, clears active mob aggro, and snuffs out torches along the path.";
        }
        return "Channels the spell carrier through pressurized abrasive mineral dust.";
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Compacted sphere of fine mineral powder (#C2B28F); bursts into a powder cloud on impact, "
                    + "inflicting Blindness for 2.5s, extinguishing torches and fires in 2m, and polishing rough stone into smooth stone.";
            case BEAM -> "Channels a continuous stream of high-velocity mineral dust (#C2B28F); blinds enemies in the ray, "
                    + "shoves them backward with wind pressure, polishes rough stone, and snuffs out wall torches and fires.";
            case RADIAL_BURST -> "Detonates in an explosive 360-degree chalky dust cloud (6-block radius); blinds nearby mobs "
                    + "for 4s, resets mob aggro so enemies lose target lock, extinguishes torches and fires, and polishes rough stone.";
            case TOUCH_STRIKE -> "Coats the victim in suffocating mineral dust, silencing spellcasting and mob abilities "
                    + "for 4 seconds while inflicting suffocation tick damage.";
            case AURA -> "Surrounds caster in a dense swirling dust cloud (#C2B28F); cuts mob detection range by 60%, "
                    + "deflects incoming hostile arrows, instantly extinguishes fires on the caster, and snuffs torches within 3m.";
            case GLYPH -> "Disarmable floor trap that turns the floor beneath the triggering entity into a 2-block deep pit "
                    + "of fine suffocating dust, trapping them for 4s with continuous suffocation damage before restoring the floor.";
            case CONE -> "Sprays a wide 60-degree cone of high-pressure mineral grit; knocks enemies back, inflicts Blindness "
                    + "for 3s, snuffs torches and fires, polishes stone/deepslate/blackstone/quartz, scrapes copper, and dries mud into clay.";
            case SKYFALL -> "Conjures a cave-adaptive swirling 14-block chalk dust storm for 15 seconds; continuously blinds "
                    + "hostiles, clears mob aggro, extinguishes all torches and fires, and polishes rough stone into smooth variants.";
            case SUMMON_AGENT -> "Summons a Dust-Stalker (#C2B28F) that dashes swiftly to deliver blinding abrasive strikes "
                    + "inflicting Armor Erosion.";
            case SUMMON_SENTRY -> "Anchors an Abrasive Dust Pylon (#C2B28F) firing high-pressure dust slugs with pushback and Blindness.";
            case SUMMON_DECOY -> "Projects a Whiteout Chalk Silhouette (#C2B28F) that taunts mobs and detonates into a "
                    + "5-block Whiteout Nova upon destruction.";
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Leaves a blinding chalk decoy at departure that lures pursuers, and arrives silently with zero vibration.";
            case TRANSMUTE, BREAK -> "Polishes stone, deepslate, blackstone, and quartz into smooth variants, scrapes copper "
                    + "oxidation and wax, and dries mud into clay with bulk item Materia fuel efficiency.";
            case AFFLICTION -> "Persistent milk-immune smother curse until cleansed via Water/Restoration magic or caster defeat; "
                    + "permanently blinds, silences abilities, deals suffocation damage, and steadily erodes physical armor.";
            case KINETIC -> "Unleashes a 10-block pneumatic dust blast that applies Armor Erosion (-5% armor and toughness "
                    + "per level, stacking up to Level 20 for 100% total armor reduction).";
            default -> "Infuses the effect with abrasive mineral dust Materia.";
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATION — ABRASIVE SANDBLASTER CHAINS (CHAIN_LEAP)
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
        // Section 6.18 Abrasive Sandblaster Chains:
        // Leaping chains jump along swirling chalk dust filaments; each leap kicks up a mini-powder cloud
        // that blinds the struck mob for 1.5s (30t), clears active mob aggro, and snuffs out torches along the path!
        int tier = spell.getEffectiveTier();
        if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(toTarget, caster, spell.hasWardFriendlyFireSafeguard())) {
            toTarget.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0));
            toTarget.clearFire();
            wipeMobAggroPublic(level, toTarget, 35L);
        }

        Vec3 start = fromTarget.getBoundingBox().getCenter();
        Vec3 end = toTarget.getBoundingBox().getCenter();
        SpellWorldVfxManager.broadcastChainArc(level, start, end, EssenceType.DUST, EssenceType.AIR, tier);

        double dist = start.distanceTo(end);
        Vec3 step = dist > 1.0E-4D ? end.subtract(start).scale(1.0D / dist) : new Vec3(0.0D, 1.0D, 0.0D);
        for (double d = 0.0D; d <= dist; d += 1.15D) {
            Vec3 sample = start.add(step.scale(d));
            BlockPos bp = BlockPos.containing(sample);
            snuffTorchesAndFiresPublic(level, bp, 1);
            level.sendParticles(
                    new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                    sample.x, sample.y, sample.z, 3, 0.16D, 0.16D, 0.16D, 0.02D
            );
        }
        level.sendParticles(ParticleTypes.POOF, toTarget.getX(), toTarget.getY(0.75D), toTarget.getZ(),
                10, 0.35D, 0.35D, 0.35D, 0.04D);
        level.playSound(null, toTarget.blockPosition(), SoundEvents.SAND_BREAK, SoundSource.PLAYERS, 0.65F, 1.25F);
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
        level.playSound(null, projectile.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.55F, 1.38F);
        level.playSound(null, projectile.blockPosition(), SoundEvents.SAND_PLACE, SoundSource.PLAYERS, 0.65F, 1.18F);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        if (projectile.tickCount % 2 == 0) {
            level.sendParticles(
                    new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.09D, 0.09D, 0.09D, 0.01D
            );
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_MIST.get(), CHALK_WHITE_HEX_COLOR, 16, 0.85F, 0.08F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.08D, 0.08D, 0.08D, 0.01D
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
        // Section 6.18 Choking Dust-Pod:
        // Bursts into a powder cloud on impact, inflicting Blindness for 2.5s (50t), extinguishing wall torches
        // and fires in a 2-block radius, and polishing rough stone blocks into smooth stone!
        int tier = spell.getEffectiveTier();
        if (hitEntity instanceof LivingEntity living
                && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                        living, caster, spell.hasWardFriendlyFireSafeguard()))) {
            if (!spell.prefix().enforcesZeroEntityHarm() && !slot.hasHarmVerb()) {
                float podDmg = (3.2F + tier * 0.90F) * spell.flawProfile().getPotencyMultiplier();
                SpellCombatReactionEngine.hurtWithDustMateriaAdvantage(level, living, caster, podDmg, false, true);
            }
            living.clearFire();
            living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
        }
        burstChokingDustPodCloud(level, caster, hitEntity.getBoundingBox().getCenter(), spell);
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
        burstChokingDustPodCloud(level, caster, blockHit.getLocation(), spell);
        polishBlocksInAreaPublic(level, blockHit.getBlockPos(), 2, false);
    }

    @Override
    public void onProjectileExpiredFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        burstChokingDustPodCloud(level, caster, projectile.position(), spell);
    }

    private static void burstChokingDustPodCloud(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 center,
            SpellAssembly spell
    ) {
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        BlockPos impact = BlockPos.containing(center);

        SpellWorldVfxManager.broadcastProjectileImpact(
                level, center, new Vec3(0.0D, 1.0D, 0.0D), EssenceType.DUST, EssenceType.AIR, 1.65F, tier
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(2.25D), LivingEntity::isAlive)) {
            if (foe == caster || (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe))) {
                continue;
            }
            foe.clearFire();
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
        }

        snuffTorchesAndFiresPublic(level, impact, 2);
        polishBlocksInAreaPublic(level, impact, 2, false);

        level.sendParticles(
                new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                center.x, center.y + 0.25D, center.z, 24, 0.85D, 0.45D, 0.85D, 0.06D
        );
        level.sendParticles(ParticleTypes.POOF, center.x, center.y + 0.25D, center.z, 12, 0.65D, 0.35D, 0.65D, 0.05D);
        level.playSound(null, impact, SoundEvents.SAND_BREAK, SoundSource.PLAYERS, 0.85F, 1.08F);
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
        // Section 6.18 Abrasive Sandblasting Jet:
        // Continuous stream of high-velocity mineral dust (#C2B28F); blinds enemies held in the ray,
        // shoves them backward with wind pressure, polishes rough stone into smooth stone, and snuffs wall torches & fires!
        int tier = spell.getEffectiveTier();
        Vec3 eye = caster.getEyePosition();
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();

        double effectiveReach = maxReach;
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            effectiveReach = Math.min(maxReach, eye.distanceTo(blockHit.getLocation()) + 0.65D);
            snuffTorchesAndFiresPublic(level, blockHit.getBlockPos(), 1);
            polishBlocksInAreaPublic(level, blockHit.getBlockPos(), 1, false);
        }

        for (double d = 1.0D; d <= effectiveReach; d += 1.0D) {
            BlockPos bp = BlockPos.containing(eye.add(normDir.scale(d)));
            snuffTorchesAndFiresPublic(level, bp, 1);
            polishBlocksInAreaPublic(level, bp, 1, false);
        }

        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            if (!spell.prefix().enforcesZeroEntityHarm() && !slot.hasHarmVerb()) {
                float rayDmg = (1.30F + tier * 0.40F) * Math.max(0.35F, apparatusPotency) * spell.flawProfile().getPotencyMultiplier();
                SpellCombatReactionEngine.hurtWithDustMateriaAdvantage(level, hit, caster, rayDmg, false, true);
            }
            hit.clearFire();
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
            if (!SpellCombatReactionEngine.isBossEntity(level, hit)) {
                SpellExecutionEngine.applySyncedMotion(hit, new Vec3(
                        normDir.x * 0.92D,
                        Math.max(0.22D, hit.getDeltaMovement().y * 0.4D + 0.18D),
                        normDir.z * 0.92D
                ));
            }
            level.sendParticles(
                    new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                    hit.getX(), hit.getY(0.70D), hit.getZ(), 5, 0.25D, 0.28D, 0.25D, 0.04D
            );
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
        // Section 6.18 Whiteout Powder Nova:
        // Detonates in an explosive 360-degree chalky dust cloud (6-block radius); blinds all nearby mobs
        // for 4 seconds (80t), resets mob aggro so enemies lose target lock, extinguishes torches & fires,
        // and polishes rough stone blocks into smooth stone!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.0D, radius);
        double rSq = (r + 0.5D) * (r + 0.5D);
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        AABB box = new AABB(
                center.x - r, center.y - 2.0D, center.z - r,
                center.x + r, center.y + 3.0D, center.z + r
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (foe == caster || foe.position().distanceToSqr(center) > rSq) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;

            if (!spell.prefix().enforcesZeroEntityHarm() && !slot.hasHarmVerb()) {
                double distFrac = Math.min(1.0D, foe.position().distanceTo(center) / Math.max(1.0D, r));
                float falloff = (float) Math.max(0.33D, 1.0D - 0.67D * distFrac);
                float novaDmg = (3.6F + tier * 1.05F) * falloff * apparatusPotency * spell.flawProfile().getPotencyMultiplier();
                SpellCombatReactionEngine.hurtWithDustMateriaAdvantage(level, foe, caster, novaDmg, false, true);
            }
            foe.clearFire();
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            wipeMobAggroPublic(level, foe, 65L);
        }

        BlockPos cPos = BlockPos.containing(center);
        int blockRad = Math.min(8, (int) Math.ceil(r));
        snuffTorchesAndFiresPublic(level, cPos, blockRad);
        polishBlocksInAreaPublic(level, cPos, Math.min(6, blockRad), false);

        level.sendParticles(
                new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                center.x, center.y + 1.0D, center.z, 60, r * 0.45D, 0.85D, r * 0.45D, 0.08D
        );
        level.sendParticles(ParticleTypes.POOF, center.x, center.y + 0.9D, center.z,
                32, r * 0.40D, 0.70D, r * 0.40D, 0.06D);
        level.playSound(null, cPos, SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.95F, 0.88F);
        level.playSound(null, cPos, SoundEvents.SAND_BREAK, SoundSource.PLAYERS, 0.95F, 0.85F);
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
        // Section 6.18 Choking Smother Strike:
        // Coats the victim in suffocating mineral dust, silencing spellcasting and mob abilities for 4 seconds (80t)
        // while inflicting continuous suffocation tick damage!
        int tier = spell.getEffectiveTier();
        long now = level.getGameTime();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.clearFire();
            SpellLingeringZoneManager.applyArcaneSilence(hit, 80);
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));

            if (!spell.prefix().enforcesZeroEntityHarm() && !slot.hasHarmVerb()) {
                float strikeDmg = (3.0F + tier * 0.85F) * apparatusPotency * spell.flawProfile().getPotencyMultiplier();
                SpellCombatReactionEngine.hurtWithDustMateriaAdvantage(level, hit, caster, strikeDmg, true, true);
            }
            hit.hurtServer(level, level.damageSources().inWall(), 2.0F + tier * 0.50F);

            level.sendParticles(
                    new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                    hit.getX(), hit.getY(0.85D), hit.getZ(), 18, 0.32D, 0.38D, 0.32D, 0.05D
            );
            level.playSound(null, hit.blockPosition(), SoundEvents.SAND_PLACE, SoundSource.PLAYERS, 0.85F, 0.90F);

            final UUID victimId = hit.getUUID();
            final float suffocateTick = 1.6F + tier * 0.35F;
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, hit.position(), 1.5D, 80, now, spell, slot, fallbackMateria, apparatusPotency,
                    (srv, zone, elapsed) -> {
                        if (elapsed > zone.maxDurationTicks) return false;
                        Entity e = srv.getEntity(victimId);
                        if (!(e instanceof LivingEntity lv) || !lv.isAlive()) {
                            return false;
                        }
                        // Washing in water clears the suffocating dust coat early!
                        if (lv.isInWaterOrRain()) {
                            return false;
                        }
                        if (elapsed > 0 && elapsed % 16 == 0) {
                            SpellLingeringZoneManager.applyArcaneSilence(lv, 24);
                            lv.hurtServer(srv, srv.damageSources().inWall(), suffocateTick);
                            srv.sendParticles(
                                    new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                                    lv.getX(), lv.getY(0.90D), lv.getZ(), 8, 0.25D, 0.28D, 0.25D, 0.02D
                            );
                        }
                        return true;
                    }
            ));
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
        // Section 6.18 Obscuring Chalk Veil:
        // Surrounds caster in a dense swirling dust cloud (#C2B28F); cuts mob detection range by 60%,
        // deflects incoming hostile arrows, instantly extinguishes fires on the caster, and snuffs torches within 3m!
        int tier = spell.getEffectiveTier();
        int dur = 240 + tier * 40;
        long now = level.getGameTime();

        caster.clearFire();
        CompoundTag data = EntityHelper.getPersistentData(caster);
        data.putLong(SpellLingeringZoneManager.TAG_DUST_VEIL_UNTIL, now + dur);
        data.putInt("SpellWardTier", tier);

        snuffTorchesAndFiresPublic(level, caster.blockPosition(), 3);
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.72F, null);

        level.sendParticles(
                new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                caster.getX(), caster.getY(0.80D), caster.getZ(), 28, 0.50D, 0.55D, 0.50D, 0.05D
        );
        level.sendParticles(ParticleTypes.POOF, caster.getX(), caster.getY(0.80D), caster.getZ(),
                14, 0.45D, 0.50D, 0.45D, 0.04D);
        level.playSound(null, caster.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.65F, 1.25F);
        level.playSound(null, caster.blockPosition(), SoundEvents.SAND_PLACE, SoundSource.PLAYERS, 0.80F, 1.05F);
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
        // Section 4.6 & 6.18 Choking Powder Pitfall:
        // - Surface-snapped disarmable trap (1.0s arming, Single or Repeating mode, 50% Sneak-Right-Click disarm refund)
        // - Turns the floor beneath the triggering entity into a 2-block deep pit of fine suffocating dust,
        //   trapping them for 4 seconds (80t) while inflicting continuous suffocation damage before restoring the floor!
        int tier = spell.getEffectiveTier();
        double trigRad = Math.max(2.5D, radius);
        int maxWaitTicks = tier >= 5 ? Integer.MAX_VALUE : 12_000;
        BlockPos surfaceFloor = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snappedCenter = new Vec3(surfaceFloor.getX() + 0.5D, surfaceFloor.getY() + 1.02D, surfaceFloor.getZ() + 0.5D);

        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxCharges = repeating ? 3 : 1;
        int[] chargesUsed = {0};
        int[] rearmAtElapsed = {20};
        Map<BlockPos, BlockState> activePitBlocks = new HashMap<>();

        SpellWorldVfxManager.broadcastArcaneSeal(level, snappedCenter, trigRad, EssenceType.DUST, EssenceType.EARTH, 60, tier);
        level.playSound(null, surfaceFloor.above(), SoundEvents.SAND_PLACE, SoundSource.PLAYERS, 0.80F, 0.95F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snappedCenter, trigRad, maxWaitTicks, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) {
                        restorePitBlocksAndLiftEntities(srv, activePitBlocks, surfaceFloor.getY() + 1.05D);
                        return false;
                    }
                    if (elapsed % 24 == 0) {
                        srv.sendParticles(
                                new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                                zone.center.x, zone.center.y + 0.08D, zone.center.z, 4, 0.65D, 0.05D, 0.65D, 0.01D
                        );
                    }
                    if (elapsed < rearmAtElapsed[0]) return true;

                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                    AABB triggerBox = new AABB(
                            zone.center.x - 1.85D, zone.center.y - 1.0D, zone.center.z - 1.85D,
                            zone.center.x + 1.85D, zone.center.y + 2.2D, zone.center.z + 1.85D
                    );
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e ->
                            e.isAlive()
                                    && e != owner
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe))
                    );
                    if (stepped.isEmpty()) return true;

                    // Carve a 2-block deep 3x3 pit of suffocating dust beneath the triggering entity!
                    BlockPos pitTopFloor = BlockPos.containing(stepped.getFirst().getX(), surfaceFloor.getY(), stepped.getFirst().getZ());
                    Map<BlockPos, BlockState> batchPit = new HashMap<>();
                    for (BlockPos p : BlockPos.betweenClosed(pitTopFloor.offset(-1, -1, -1), pitTopFloor.offset(1, 0, 1))) {
                        BlockState bs = srv.getBlockState(p);
                        if (!bs.isAir()
                                && !(bs.getBlock() instanceof ScribedChalkBlock)
                                && !SpellExecutionEngine.isProtectedBlockOrContainer(srv, p, bs)
                                && bs.getDestroySpeed(srv, p) >= 0.0F) {
                            BlockPos imm = p.immutable();
                            // Also protect any ScribedChalkBlock resting directly above the top floor layer!
                            if (p.getY() == pitTopFloor.getY() && srv.getBlockState(imm.above()).getBlock() instanceof ScribedChalkBlock) {
                                continue;
                            }
                            batchPit.put(imm, bs);
                            activePitBlocks.putIfAbsent(imm, bs);
                            srv.removeBlock(imm, false);
                        }
                    }

                    Vec3 pitCenter = new Vec3(pitTopFloor.getX() + 0.5D, pitTopFloor.getY() + 1.02D, pitTopFloor.getZ() + 0.5D);
                    SpellWorldVfxManager.broadcastArcaneSeal(srv, pitCenter, 2.2D, EssenceType.DUST, EssenceType.EARTH, 84, tier);
                    srv.sendParticles(
                            new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                            pitCenter.x, pitCenter.y - 0.5D, pitCenter.z, 40, 1.1D, 0.8D, 1.1D, 0.06D
                    );
                    srv.playSound(null, pitTopFloor, SoundEvents.SAND_BREAK, SoundSource.PLAYERS, 1.0F, 0.78F);

                    float chargeScale = chargesUsed[0] == 0 ? 1.0F : 0.75F;
                    final double liftY = pitTopFloor.getY() + 1.05D;
                    AABB pitArea = new AABB(
                            pitCenter.x - 1.85D, pitTopFloor.getY() - 2.2D, pitCenter.z - 1.85D,
                            pitCenter.x + 1.85D, pitTopFloor.getY() + 1.8D, pitCenter.z + 1.85D
                    );

                    for (LivingEntity v : stepped) {
                        if (!SpellCombatReactionEngine.isBossEntity(srv, v)) {
                            SpellExecutionEngine.applySyncedMotion(v, new Vec3(0.0D, -0.55D, 0.0D));
                        }
                    }

                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, pitCenter, 2.0D, 80, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (psrv, pzone, pelapsed) -> {
                                if (pelapsed >= pzone.maxDurationTicks) {
                                    restorePitBlocksAndLiftEntities(psrv, batchPit, liftY);
                                    for (BlockPos k : batchPit.keySet()) {
                                        activePitBlocks.remove(k);
                                    }
                                    return false;
                                }
                                if (pelapsed % 4 == 0) {
                                    psrv.sendParticles(
                                            new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                                            pitCenter.x, pitTopFloor.getY() - 0.2D, pitCenter.z, 12, 0.95D, 0.65D, 0.95D, 0.03D
                                    );
                                }
                                LivingEntity pOwner = pzone.resolveCaster(psrv);
                                long pNow = psrv.getGameTime();
                                for (LivingEntity v : psrv.getEntitiesOfClass(LivingEntity.class, pitArea, e -> e.isAlive() && e != pOwner)) {
                                    if (pOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, pOwner, wardSafe)) {
                                        continue;
                                    }
                                    v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 6));
                                    v.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 25, 0));
                                    v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 35, 0));
                                    EntityHelper.getPersistentData(v).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, pNow + 25L);

                                    if (pelapsed % 16 == 0) {
                                        float pitSuffocate = (2.0F + tier * 0.55F) * chargeScale;
                                        v.hurtServer(psrv, psrv.damageSources().inWall(), pitSuffocate);
                                        SpellCombatReactionEngine.hurtWithDustMateriaAdvantage(
                                                psrv, v, pOwner, pitSuffocate * 0.75F, false, true
                                        );
                                        if (pzone.spell != null && pzone.slot != null) {
                                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                                    psrv, pOwner, v, Vec3.ZERO,
                                                    pzone.spell, pzone.slot, pzone.fallbackMateria, pzone.apparatusPotency * 0.35F
                                            );
                                        }
                                    }
                                }
                                return true;
                            }
                    ));

                    chargesUsed[0]++;
                    if (chargesUsed[0] >= maxCharges) {
                        return false;
                    }
                    rearmAtElapsed[0] = elapsed + 110;
                    return true;
                }
        ).markDisarmableGlyph()
                .withOnDisarm((srv, z) -> restorePitBlocksAndLiftEntities(srv, activePitBlocks, surfaceFloor.getY() + 1.05D)));
        return true;
    }

    private static void restorePitBlocksAndLiftEntities(
            ServerLevel level,
            Map<BlockPos, BlockState> pitBlocks,
            double safeSurfaceY
    ) {
        if (pitBlocks.isEmpty()) return;
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (Map.Entry<BlockPos, BlockState> entry : pitBlocks.entrySet()) {
            BlockPos pos = entry.getKey();
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
            if (level.getBlockState(pos).isAir()) {
                level.setBlockAndUpdate(pos, entry.getValue());
            }
        }
        AABB liftBox = new AABB(minX - 0.2D, minY - 0.5D, minZ - 0.2D, maxX + 1.2D, maxY + 1.1D, maxZ + 1.2D);
        for (Entity ent : level.getEntities(null, liftBox)) {
            if (ent.isAlive() && ent.getY() < safeSurfaceY) {
                ent.teleportTo(ent.getX(), safeSurfaceY, ent.getZ());
                ent.resetFallDistance();
            }
        }
        pitBlocks.clear();
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
        // Section 6.18 Abrasive Sandblasting Deluge:
        // Sprays a wide 60-degree cone of high-pressure mineral grit; knocks enemies back, inflicts Blindness for 3s (60t),
        // snuffs out torches & fires, polishes stone/deepslate/blackstone/quartz into smooth variants,
        // scrapes oxidation and wax off copper, and dries mud into clay!
        int tier = spell.getEffectiveTier();
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;
            if (!spell.prefix().enforcesZeroEntityHarm() && !slot.hasHarmVerb()) {
                float delugeDmg = (3.2F + tier * 0.90F) * apparatusPotency * spell.flawProfile().getPotencyMultiplier();
                SpellCombatReactionEngine.hurtWithDustMateriaAdvantage(level, hit, caster, delugeDmg, false, true);
            }
            hit.clearFire();
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            if (!SpellCombatReactionEngine.isBossEntity(level, hit)) {
                SpellExecutionEngine.applySyncedMotion(hit, new Vec3(normDir.x * 1.32D, 0.34D, normDir.z * 1.32D));
            }
        }

        // Sweep full 60-degree cone (half-angle 30° -> dot >= 0.866) across terrain blocks!
        Vec3 origin = caster.position().add(0.0D, 0.65D, 0.0D);
        int maxR = Math.min(11, Math.max(5, (int) Math.ceil(coneRange)));
        double maxRSq = maxR * maxR;
        BlockPos originBlock = BlockPos.containing(origin);
        for (BlockPos p : BlockPos.betweenClosed(originBlock.offset(-maxR, -2, -maxR), originBlock.offset(maxR, 3, maxR))) {
            Vec3 toBlock = Vec3.atCenterOf(p).subtract(origin);
            double distSq = toBlock.lengthSqr();
            if (distSq < 0.5D || distSq > maxRSq) continue;
            if (toBlock.normalize().dot(normDir) < 0.84D) continue;

            snuffSingleTorchOrFire(level, p);
            BlockState bs = level.getBlockState(p);
            if (!bs.isAir() && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) {
                polishOrAbradeBlock(level, p, bs, true);
            }
        }

        level.playSound(null, caster.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.90F, 0.95F);
        level.playSound(null, caster.blockPosition(), SoundEvents.SAND_BREAK, SoundSource.PLAYERS, 0.85F, 1.05F);
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
        // Section 6.18 Whiteout Dust Storm Downpour:
        // Conjures a cave-ceiling-adaptive swirling 14-block (7.0D radius) chalk dust storm from the sky for 15 seconds (300t);
        // continuously blinds hostiles, clears active mob aggro, extinguishes all torches and fires,
        // and polishes rough stone blocks into smooth variants across the radius!
        int tier = spell.getEffectiveTier();
        double stormRad = Math.max(7.0D, radius);
        int stormTicks = 300; // Full 15.0 seconds!

        BlockPos baseBlock = BlockPos.containing(impactCenter);
        double canopyHeight = 9.0D;
        for (int dy = 4; dy <= 10; dy++) {
            BlockPos check = baseBlock.above(dy);
            if (!level.getBlockState(check).getCollisionShape(level, check).isEmpty()) {
                canopyHeight = Math.max(3.6D, dy - 0.60D);
                break;
            }
        }
        final double finalCanopyH = canopyHeight;

        SpellWorldVfxManager.broadcastSkyfallPillar(
                level, impactCenter, stormRad, EssenceType.DUST, EssenceType.AIR, 68, tier
        );
        level.playSound(null, baseBlock, SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.95F, 0.82F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, stormRad, stormTicks, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;

                    if (elapsed > 0 && elapsed % 48 == 0) {
                        SpellWorldVfxManager.broadcastSkyfallPillar(
                                srv, zone.center, zone.radius, EssenceType.DUST, EssenceType.AIR, 56, tier
                        );
                    }
                    if (elapsed % 5 == 0) {
                        srv.sendParticles(
                                new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                                zone.center.x, zone.center.y + Math.min(4.2D, finalCanopyH * 0.65D), zone.center.z,
                                26, zone.radius * 0.55D, 1.2D, zone.radius * 0.55D, 0.06D
                        );
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        BlockPos cPos = BlockPos.containing(zone.center);
                        int br = Math.min(8, (int) Math.ceil(zone.radius));
                        snuffTorchesAndFiresPublic(srv, cPos, br);
                        polishBlocksInAreaPublic(srv, cPos, br, false);

                        double rSq = (zone.radius + 0.5D) * (zone.radius + 0.5D);
                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + finalCanopyH, zone.center.z + zone.radius
                        );
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (v.position().distanceToSqr(zone.center) > rSq) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, wardSafe)) {
                                continue;
                            }
                            v.clearFire();
                            v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 55, 0));
                            wipeMobAggroPublic(srv, v, 30L);

                            if (zone.spell != null && zone.slot != null) {
                                if (!zone.spell.prefix().enforcesZeroEntityHarm() && !zone.slot.hasHarmVerb()) {
                                    float stormDmg = (2.2F + tier * 0.60F) * zone.apparatusPotency * 0.45F;
                                    SpellCombatReactionEngine.hurtWithDustMateriaAdvantage(srv, v, owner, stormDmg, false, true);
                                }
                                SpellExecutionEngine.deliverSubSpellOnEntity(
                                        srv, owner, v, new Vec3(0.0D, -1.0D, 0.0D),
                                        zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.30F
                                );
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
        int tier = spell.getEffectiveTier();
        CompoundTag tag = EntityHelper.getPersistentData(summonEntity);
        tag.putString("SpellEidolonMateria", EssenceType.DUST.name());
        tag.putInt("SpellEidolonTintRGB", DUST_STALKER_HEX_COLOR);

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, spawnPos.add(0.0D, 0.04D, 0.0D), 2.1D, EssenceType.DUST, EssenceType.AIR, 48, tier
        );
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                spawnPos.x, spawnPos.y + 0.85D, spawnPos.z, 26, 0.45D, 0.55D, 0.45D, 0.06D
        );
        level.playSound(null, BlockPos.containing(spawnPos), SoundEvents.SAND_PLACE, SoundSource.PLAYERS, 0.90F, 1.05F);
    }

    // =========================================================================
    // PART 3: CORE EFFECT SYNERGIES (BLINK, TRANSMUTE/BREAK, HEX, KINETIC)
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Section 6.18 Chalk-Step:
        // Leaves a blinding chalk decoy at departure and arrives silently with zero vibration!
        EntityHelper.getPersistentData(caster).putLong(
                SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, level.getGameTime() + 100L
        );

        float decoyHp = 10.0F + potencyTier * 3.0F;
        SpellEidolonEntity chalkDecoy = SpellEidolonEntity.spawnShortLivedDecoy(
                level,
                caster,
                departurePos,
                EssenceType.DUST,
                decoyHp,
                80,
                "§fChalk Silhouette",
                null
        );

        AABB depBox = new AABB(
                departurePos.x - 4.5D, departurePos.y - 2.0D, departurePos.z - 4.5D,
                departurePos.x + 4.5D, departurePos.y + 2.5D, departurePos.z + 4.5D
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, depBox, LivingEntity::isAlive)) {
            if (foe == caster || foe == chalkDecoy || foe.isAlliedTo(caster)) continue;
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 65, 0));
            if (foe instanceof Mob mob) {
                mob.setTarget(chalkDecoy);
            }
        }

        SpellWorldVfxManager.broadcastWardShell(
                level, departurePos, 1.65D, EssenceType.DUST, EssenceType.AIR, 60, potencyTier
        );
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                departurePos.x, departurePos.y + 0.90D, departurePos.z, 32, 0.65D, 0.60D, 0.65D, 0.05D
        );
        level.sendParticles(ParticleTypes.POOF, departurePos.x, departurePos.y + 0.90D, departurePos.z,
                16, 0.55D, 0.55D, 0.55D, 0.04D);

        final UUID decoyUuid = chalkDecoy.getUUID();
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, departurePos, 4.5D, 80, level.getGameTime(), null, null, EssenceType.DUST, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 6 == 0) {
                        srv.sendParticles(
                                new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                                departurePos.x, departurePos.y + 1.0D, departurePos.z, 10, 0.55D, 0.60D, 0.55D, 0.03D
                        );
                    }
                    LivingEntity owner = zone.resolveCaster(srv);
                    Entity dEnt = srv.getEntity(decoyUuid);
                    LivingEntity lureTarget = (dEnt instanceof LivingEntity dl && dl.isAlive()) ? dl : null;
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, depBox, LivingEntity::isAlive)) {
                        if (foe == owner || foe == lureTarget || (owner != null && foe.isAlliedTo(owner))) continue;
                        foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0));
                        if (foe instanceof Mob mob && (mob.getTarget() == owner || mob.getTarget() == null)) {
                            if (lureTarget != null) {
                                mob.setTarget(lureTarget);
                            } else {
                                mob.setTarget(null);
                                if (mob instanceof Monster && elapsed % 10 == 0) {
                                    mob.getNavigation().moveTo(departurePos.x, departurePos.y, departurePos.z, 1.1D);
                                }
                            }
                        }
                    }
                    return true;
                }
        ));
    }

    @Override
    public boolean onTransmuteBlock(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            BlockState state,
            int potencyTier
    ) {
        // Section 6.18 Sedimentary Abrasion & Polishing:
        // Polishes stone, deepslate, blackstone, and quartz into smooth variants, scrapes copper oxidation & wax,
        // and dries mud into clay (with full container & ScribedChalkBlock safety)!
        if (state.isAir()
                || state.getBlock() instanceof ScribedChalkBlock
                || SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return false;
        }
        boolean changed = polishOrAbradeBlock(level, pos, state, true);
        if (changed) {
            level.sendParticles(
                    new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                    pos.getX() + 0.5D, pos.getY() + 0.6D, pos.getZ() + 0.5D, 10, 0.30D, 0.30D, 0.30D, 0.04D
            );
            level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.PLAYERS, 0.55F, 1.35F);
        }
        return changed;
    }

    @Override
    public boolean onTransmuteDroppedItem(
            ServerLevel level,
            @Nullable LivingEntity caster,
            ItemEntity itemEntity,
            int potencyTier
    ) {
        ItemStack in = itemEntity.getItem();
        int count = in.getCount();
        Item outItem = null;

        if (in.is(Items.COBBLESTONE)) outItem = Items.STONE;
        else if (in.is(Items.STONE) || in.is(Items.STONE_BRICKS) || in.is(Items.CRACKED_STONE_BRICKS)) outItem = Items.SMOOTH_STONE;
        else if (in.is(Items.COBBLED_DEEPSLATE) || in.is(Items.DEEPSLATE) || in.is(Items.CRACKED_DEEPSLATE_BRICKS)) outItem = Items.POLISHED_DEEPSLATE;
        else if (in.is(Items.BLACKSTONE) || in.is(Items.CRACKED_POLISHED_BLACKSTONE_BRICKS)) outItem = Items.POLISHED_BLACKSTONE;
        else if (in.is(Items.QUARTZ_BLOCK)) outItem = Items.SMOOTH_QUARTZ;
        else if (in.is(Items.SANDSTONE)) outItem = Items.SMOOTH_SANDSTONE;
        else if (in.is(Items.RED_SANDSTONE)) outItem = Items.SMOOTH_RED_SANDSTONE;
        else if (in.is(Items.BASALT)) outItem = Items.SMOOTH_BASALT;
        else if (in.is(Items.ANDESITE)) outItem = Items.POLISHED_ANDESITE;
        else if (in.is(Items.DIORITE)) outItem = Items.POLISHED_DIORITE;
        else if (in.is(Items.GRANITE)) outItem = Items.POLISHED_GRANITE;
        else if (in.is(Items.TUFF)) outItem = Items.POLISHED_TUFF;
        else if (in.is(Items.MUD)) outItem = Items.CLAY;
        else if (in.getItem() instanceof BlockItem bi) {
            Block block = bi.getBlock();
            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
            if (key.getPath().startsWith("waxed_")) {
                ResourceLocation unwaxedId = ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getPath().substring(6));
                Block unwaxed = BuiltInRegistries.BLOCK.getValue(unwaxedId);
                if (unwaxed != Blocks.AIR && unwaxed.asItem() != Items.AIR) {
                    outItem = unwaxed.asItem();
                }
            }
            if (outItem == null) {
                Optional<BlockState> deoxidized = WeatheringCopper.getPrevious(block.defaultBlockState());
                if (deoxidized.isPresent() && deoxidized.get().getBlock().asItem() != Items.AIR) {
                    outItem = deoxidized.get().getBlock().asItem();
                }
            }
        }

        if (outItem == null) {
            return false;
        }

        itemEntity.setItem(new ItemStack(outItem, count));
        // Section 2.2 Bulk Item Batch Efficiency: refund up to 30% Materia fuel when abrading/polishing multi-item stacks!
        if (count > 1 && caster instanceof Player player && !player.isCreative()) {
            long batchRefundMfum = Math.min(24L, (long) (count - 1) * 2L);
            SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.DUST, batchRefundMfum);
        }
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                itemEntity.getX(), itemEntity.getY() + 0.25D, itemEntity.getZ(), 8, 0.20D, 0.20D, 0.20D, 0.03D
        );
        return true;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.18 Choking Powder Smother Hex:
        // Persistent milk-immune chalky smother curse until cleansed via Water/Restoration magic or caster defeat:
        // - Permanently inflicts Blindness
        // - Silences spellcasting and mob special abilities
        // - Deals continuous suffocation tick damage
        // - Steadily erodes physical armor over time (-5% armor & toughness per level up to Level 20 = -100%)!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        int addErosionLevels = Math.max(1, Math.round(mult));

        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0, true, true));
        SpellLingeringZoneManager.applyArcaneSilence(target, 45);
        stackArmorErosionPublic(target, addErosionLevels, 65);

        float suffocateDmg = (1.35F + potencyTier * 0.42F) * mult;
        target.hurtServer(level, level.damageSources().inWall(), suffocateDmg);
        SpellCombatReactionEngine.hurtWithDustMateriaAdvantage(
                level, target, null, suffocateDmg * 0.50F, false, true
        );

        level.sendParticles(
                new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                target.getX(), target.getY(0.85D), target.getZ(), 7, 0.28D, 0.32D, 0.28D, 0.02D
        );
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
        if (effect == SpellCoreEffect.KINETIC && target != caster) {
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(
                    target, caster, spell != null && spell.hasWardFriendlyFireSafeguard())) {
                return;
            }
            // Section 6.18 Pneumatic Dust-Blast with Armor Erosion:
            // Unleashes a 10-block pneumatic dust blast that applies Armor Erosion (-5% armor & toughness per level,
            // stacking up to Level 20 for 100% total armor reduction)!
            Vec3 horiz = new Vec3(direction.x, 0.0D, direction.z);
            Vec3 pushDir = horiz.lengthSqr() > 1.0E-4D
                    ? horiz.normalize()
                    : (caster != null ? new Vec3(caster.getLookAngle().x, 0.0D, caster.getLookAngle().z).normalize() : new Vec3(0.0D, 0.0D, 1.0D));

            int levelsToAdd = Math.max(2, 1 + potencyTier * 2);
            stackArmorErosionPublic(target, levelsToAdd, 240);
            target.clearFire();

            if (!SpellCombatReactionEngine.isBossEntity(level, target)) {
                // Calibrated 10-block horizontal pneumatic dust-blast impulse synced to players & mobs!
                SpellExecutionEngine.applySyncedMotion(target, new Vec3(pushDir.x * 2.15D, 0.44D, pushDir.z * 2.15D));
            }
            snuffTorchesAndFiresPublic(level, target.blockPosition(), 2);

            SpellWorldVfxManager.broadcastProjectileImpact(
                    level, target.getBoundingBox().getCenter(), pushDir, EssenceType.DUST, EssenceType.AIR, 1.65F, potencyTier
            );
            level.sendParticles(
                    new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                    target.getX(), target.getY(0.70D), target.getZ(), 24, 0.45D, 0.45D, 0.45D, 0.08D
            );
            level.playSound(null, target.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.85F, 1.08F);
        }
    }

    /**
     * Terrain-targeted <b>Pneumatic Dust-Blast</b> (Section 6.18):
     * aiming a Dust-mutated Kinetic spell at terrain unleashes a 10-block pneumatic dust blast
     * across nearby targets (including rocket-jump self-propulsion when aimed near the caster's feet)
     * while applying {@code Armor Erosion} to struck foes!
     */
    public static void unleashPneumaticDustBlastAt(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 center,
            Vec3 fallbackDir,
            int potencyTier,
            @Nullable SpellAssembly spell
    ) {
        boolean wardSafe = spell != null && spell.hasWardFriendlyFireSafeguard();
        int levelsToAdd = Math.max(2, 1 + potencyTier * 2);

        SpellWorldVfxManager.broadcastRadialNova(
                level, center.add(0.0D, 0.08D, 0.0D), 4.5D, EssenceType.DUST, EssenceType.AIR, potencyTier
        );
        snuffTorchesAndFiresPublic(level, BlockPos.containing(center), 3);

        AABB blastBox = new AABB(center, center).inflate(4.5D);
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, blastBox, LivingEntity::isAlive)) {
            if (ent == caster) {
                if (ent.position().distanceToSqr(center) <= 20.25D) {
                    Vec3 awaySelf = ent.position().add(0.0D, 0.55D, 0.0D).subtract(center);
                    Vec3 jump = awaySelf.lengthSqr() > 1.0E-4D ? awaySelf.normalize() : new Vec3(0.0D, 1.0D, 0.0D);
                    SpellExecutionEngine.applySyncedMotion(ent, new Vec3(jump.x * 1.35D, Math.max(0.68D, jump.y * 1.15D), jump.z * 1.35D));
                }
                continue;
            }
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, wardSafe)) {
                continue;
            }
            ent.clearFire();
            stackArmorErosionPublic(ent, levelsToAdd, 240);
            if (!SpellCombatReactionEngine.isBossEntity(level, ent)) {
                Vec3 delta = ent.position().subtract(center);
                Vec3 horiz = new Vec3(delta.x, 0.0D, delta.z);
                Vec3 push = horiz.lengthSqr() > 1.0E-4D ? horiz.normalize()
                        : (new Vec3(fallbackDir.x, 0.0D, fallbackDir.z).lengthSqr() > 1.0E-4D
                                ? new Vec3(fallbackDir.x, 0.0D, fallbackDir.z).normalize()
                                : new Vec3(0.0D, 0.0D, 1.0D));
                SpellExecutionEngine.applySyncedMotion(ent, new Vec3(push.x * 2.15D, 0.44D, push.z * 2.15D));
            }
        }

        // Deflect incoming hostile projectiles caught in the pneumatic blast!
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, blastBox)) {
            if (proj.getOwner() == caster) continue;
            Vec3 away = proj.position().subtract(center);
            Vec3 defl = away.lengthSqr() > 1.0E-4D ? away.normalize().scale(1.55D) : proj.getDeltaMovement().scale(-1.2D);
            proj.setDeltaMovement(defl);
            proj.hasImpulse = true;
        }

        level.sendParticles(
                new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                center.x, center.y + 0.65D, center.z, 36, 1.5D, 0.55D, 1.5D, 0.08D
        );
        level.playSound(null, BlockPos.containing(center), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.95F, 0.96F);
    }

    // =========================================================================
    // SHARED PUBLIC ENGINE HELPERS (ARMOR EROSION, AGGRO WIPE, SNUFF & POLISH)
    // =========================================================================

    public static void stackArmorErosionPublic(LivingEntity target, int addLevels, int durationTicks) {
        var holder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.ARMOR_EROSION.get());
        MobEffectInstance cur = target.getEffect(holder);
        int clampedAdd = Math.max(1, addLevels);
        int nextAmp = cur == null ? Math.min(19, clampedAdd - 1) : Math.min(19, cur.getAmplifier() + clampedAdd);
        int dur = cur == null ? durationTicks : Math.max(cur.getDuration(), durationTicks);
        target.addEffect(new MobEffectInstance(holder, dur, nextAmp, false, true));
    }

    public static void wipeMobAggroPublic(ServerLevel level, LivingEntity entity, long amnesiaTicks) {
        if (!(entity instanceof Mob mob)) return;
        LivingEntity prevTarget = mob.getTarget();
        mob.setTarget(null);
        mob.setLastHurtByMob(null);
        mob.setAggressive(false);
        mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        mob.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
        mob.getBrain().eraseMemory(MemoryModuleType.ROAR_TARGET);
        if (mob instanceof NeutralMob neutral) {
            neutral.stopBeingAngry();
            neutral.setPersistentAngerTarget(null);
        }
        if (mob instanceof Warden warden && prevTarget != null) {
            warden.clearAnger(prevTarget);
        }
        if (amnesiaTicks > 0L) {
            EntityHelper.getPersistentData(mob).putLong(
                    SpellLingeringZoneManager.TAG_UMBRAL_OBFUSCATED_UNTIL,
                    Math.max(
                            level.getGameTime() + amnesiaTicks,
                            EntityHelper.getPersistentData(mob).getLong(SpellLingeringZoneManager.TAG_UMBRAL_OBFUSCATED_UNTIL).orElse(0L)
                    )
            );
        }
    }

    public static void snuffTorchesAndFiresPublic(ServerLevel level, BlockPos center, int r) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -1, -r), center.offset(r, 2, r))) {
            snuffSingleTorchOrFire(level, p);
        }
    }

    private static void snuffSingleTorchOrFire(ServerLevel level, BlockPos p) {
        BlockState bs = level.getBlockState(p);
        if (bs.isAir() || bs.getBlock() instanceof ScribedChalkBlock) {
            return;
        }
        if (bs.getBlock() instanceof BaseFireBlock) {
            level.removeBlock(p, false);
        } else if (bs.getBlock() instanceof CampfireBlock && bs.hasProperty(CampfireBlock.LIT) && bs.getValue(CampfireBlock.LIT)) {
            level.setBlockAndUpdate(p, bs.setValue(CampfireBlock.LIT, false));
        } else if (bs.getBlock() instanceof AbstractCandleBlock && bs.hasProperty(AbstractCandleBlock.LIT) && bs.getValue(AbstractCandleBlock.LIT)) {
            AbstractCandleBlock.extinguish(null, bs, level, p);
        } else if ((bs.getBlock() instanceof TorchBlock || bs.getBlock() instanceof WallTorchBlock)
                && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) {
            level.destroyBlock(p, true);
        }
    }

    public static void polishBlocksInAreaPublic(ServerLevel level, BlockPos center, int r, boolean fullAbrasion) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -1, -r), center.offset(r, 1, r))) {
            BlockState bs = level.getBlockState(p);
            if (!bs.isAir()
                    && !(bs.getBlock() instanceof ScribedChalkBlock)
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) {
                polishOrAbradeBlock(level, p, bs, fullAbrasion);
            }
        }
    }

    public static boolean polishOrAbradeBlock(ServerLevel level, BlockPos pos, BlockState state, boolean fullAbrasion) {
        Block polished = null;
        if (state.is(Blocks.COBBLESTONE)) polished = Blocks.STONE;
        else if (state.is(Blocks.STONE) || state.is(Blocks.CRACKED_STONE_BRICKS)) polished = Blocks.SMOOTH_STONE;
        else if (state.is(Blocks.COBBLED_DEEPSLATE) || state.is(Blocks.DEEPSLATE) || state.is(Blocks.CRACKED_DEEPSLATE_BRICKS)) polished = Blocks.POLISHED_DEEPSLATE;
        else if (state.is(Blocks.BLACKSTONE) || state.is(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS)) polished = Blocks.POLISHED_BLACKSTONE;
        else if (state.is(Blocks.QUARTZ_BLOCK)) polished = Blocks.SMOOTH_QUARTZ;
        else if (state.is(Blocks.SANDSTONE)) polished = Blocks.SMOOTH_SANDSTONE;
        else if (state.is(Blocks.RED_SANDSTONE)) polished = Blocks.SMOOTH_RED_SANDSTONE;
        else if (state.is(Blocks.BASALT)) polished = Blocks.SMOOTH_BASALT;
        else if (state.is(Blocks.ANDESITE)) polished = Blocks.POLISHED_ANDESITE;
        else if (state.is(Blocks.DIORITE)) polished = Blocks.POLISHED_DIORITE;
        else if (state.is(Blocks.GRANITE)) polished = Blocks.POLISHED_GRANITE;
        else if (state.is(Blocks.TUFF)) polished = Blocks.POLISHED_TUFF;
        else if (fullAbrasion && state.is(Blocks.MUD)) polished = Blocks.CLAY;

        if (polished != null) {
            level.setBlockAndUpdate(pos, polished.defaultBlockState());
            return true;
        }

        if (fullAbrasion) {
            // Scrape wax off waxed copper blocks
            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            if (key.getPath().startsWith("waxed_")) {
                ResourceLocation unwaxedId = ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getPath().substring(6));
                Block unwaxed = BuiltInRegistries.BLOCK.getValue(unwaxedId);
                if (unwaxed != Blocks.AIR) {
                    level.setBlockAndUpdate(pos, unwaxed.withPropertiesOf(state));
                    level.playSound(null, pos, SoundEvents.AXE_WAX_OFF, SoundSource.PLAYERS, 0.7F, 1.1F);
                    return true;
                }
            }
            // Scrape oxidation off copper blocks
            Optional<BlockState> deoxidized = WeatheringCopper.getPrevious(state);
            if (deoxidized.isPresent()) {
                level.setBlockAndUpdate(pos, deoxidized.get());
                level.playSound(null, pos, SoundEvents.AXE_SCRAPE, SoundSource.PLAYERS, 0.7F, 1.1F);
                return true;
            }
        }
        return false;
    }

    private static BlockPos findGroundSurfaceNear(ServerLevel level, BlockPos approx) {
        for (int dy = 2; dy >= -4; dy--) {
            BlockPos cand = approx.offset(0, dy, 0);
            if (!level.getBlockState(cand).getCollisionShape(level, cand).isEmpty()
                    && level.getBlockState(cand.above()).getCollisionShape(level, cand.above()).isEmpty()) {
                return cand;
            }
        }
        return approx.below();
    }
}
