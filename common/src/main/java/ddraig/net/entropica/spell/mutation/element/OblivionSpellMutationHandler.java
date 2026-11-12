package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import ddraig.net.entropica.spell.assembly.SubSpellSlot;
import ddraig.net.entropica.spell.engine.SpellCombatReactionEngine;
import ddraig.net.entropica.spell.engine.SpellExecutionEngine;
import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Oblivion Materia (Tier 3 Trinity Fusion #47)</b>
 * (Spellcrafting System Guide Section 6.48 — Nether + Umbral + Undead).
 */
public final class OblivionSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int STYGIAN_OBLIVION_HEX_COLOR = 0xB83C08;

    @Override
    public EssenceType materiaType() {
        return EssenceType.OBLIVION;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Entropic Ash-Chain Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Stygian Hellfire Annihilation Skull";
            case BEAM -> "Stygian Oblivion Disintegration Ray & Void-Flare";
            case RADIAL_BURST -> "Stygian Hellfire Nova";
            case TOUCH_STRIKE -> "Stygian Extinction Palm-Cleave";
            case AURA -> "Oblivion Void-Shroud";
            case GLYPH -> "Oblivion Singularity Mine";
            case CONE -> "Stygian Extinction-Flame Deluge";
            case SKYFALL -> "Cataclysmic Stygian Meteor Downpour";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Stygian Ash-Warp Step";
            case TRANSMUTE, BREAK -> "Stygian Desolation & Ash-Slag Smelting";
            case AFFLICTION -> "Stygian Oblivion-Extinction Hex";
            default -> "Stygian Oblivion " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations (Entropic Ash-Chain Flow) ---

    @Override
    public void onChainLeapStep(ServerLevel level, LivingEntity caster, LivingEntity fromTarget, LivingEntity toTarget, SpellAssembly spell, SubSpellSlot slot) {
        // Entropic Ash-Chain Flow: up to 4 hostiles within 6m (60% initial damage), -25% armor toughness & 5.0s stygian fire!
        SpellCombatReactionEngine.applyPercentageArmorShred(toTarget, level, 0.0D, 0.25D, 100L);
        EntityHelper.getPersistentData(toTarget).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, level.getGameTime() + 100L);
        toTarget.igniteForSeconds(5);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, toTarget.getX(), toTarget.getY(0.8D), toTarget.getZ(), 10, 0.25D, 0.3D, 0.25D, 0.03D);
    }

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.SMOKE, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.01D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.FLAME, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.02D);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        detonateStygianSkull(level, caster, hitEntity.position(), spell);
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        detonateStygianSkull(level, caster, blockHit.getLocation(), spell);
    }

    private static void detonateStygianSkull(ServerLevel level, @Nullable LivingEntity caster, Vec3 center, SpellAssembly spell) {
        // Stygian Hellfire Annihilation Skull: shatters 4 armor points, 4m black fire shockwave burning underwater + 50% slow crater!
        long burnUntil = level.getGameTime() + 100L;
        AABB box = new AABB(center.x - 4.0D, center.y - 1.5D, center.z - 4.0D, center.x + 4.0D, center.y + 2.5D, center.z + 4.0D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != caster)) {
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 4.0D, 0.0D, 160L);
            EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, burnUntil);
            foe.igniteForSeconds(5);
            foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1));
            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2)); // 50% slow
        }
        level.sendParticles(ParticleTypes.LARGE_SMOKE, center.x, center.y + 0.6D, center.z, 26, 1.5D, 0.5D, 1.5D, 0.05D);
        level.sendParticles(ParticleTypes.FLAME, center.x, center.y + 0.6D, center.z, 22, 1.4D, 0.5D, 1.4D, 0.06D);
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
        // Stygian Oblivion Disintegration Ray & Void-Flare: melts 1 armor/s, vaporizes incoming arrows;
        // REQUIRES 5.0s sustained (stacks >= 5) -> Cataclysmic Extinction Burst (8m KB, melts 5 armor, Wither III + Blindness 6.0s / 120t)!
        Vec3 eye = caster.getEyePosition();
        Vec3 dirNorm = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        AABB corridor = new AABB(eye, eye.add(dirNorm.scale(maxReach))).inflate(1.5D);
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, corridor, p -> p.getOwner() != caster && !(p instanceof SpellProjectileEntity))) {
            level.sendParticles(ParticleTypes.ASH, proj.getX(), proj.getY(), proj.getZ(), 5, 0.1D, 0.1D, 0.1D, 0.02D);
            proj.discard();
        }

        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 1.0D, 0.0D, 80L);

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("OblivionBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("OblivionBeamStacks").orElse(0) + 1 : 1;
            data.putLong("OblivionBeamLastTick", now);
            data.putInt("OblivionBeamStacks", stacks);

            if (stacks >= 5) {
                data.putInt("OblivionBeamStacks", 0);
                for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, hit.getBoundingBox().inflate(4.5D), e -> e.isAlive() && e != caster)) {
                    if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
                    Vec3 away = foe.position().subtract(caster.position()).normalize().scale(1.85D);
                    foe.setDeltaMovement(away.x, 0.42D, away.z);
                    foe.hasImpulse = true;
                    SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 5.0D, 0.0D, 200L);
                    foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 2));
                    foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0));
                    foe.hurtServer(level, level.damageSources().indirectMagic(caster, caster), 11.0F + spell.getEffectiveTier() * 2.5F);
                }
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0.72F, 0.24F, 0.03F), hit.getX(), hit.getY(1.0D), hit.getZ(), 1, 0, 0, 0, 0);
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
        // Stygian Hellfire Nova: 6m radius, strictly ZERO physical light blocks — 8m KB, shatters 3 armor,
        // 6.0s black stygian fire, grants caster Obsidian Extinction Armor (+6 Armor, +3 Toughness, Fire/Lava/Wither immunity, 40% thorns 10s)!
        double r = Math.max(6.0D, radius);
        long now = level.getGameTime();
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_OBLIVION_EXTINCTION_ARMOR_UNTIL, now + 200L);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 6.0D, 3.0D, 0.0D, 0.0D, 200L);

        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.5D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != caster)) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = foe.position().subtract(center).normalize().scale(1.85D);
            foe.setDeltaMovement(away.x, 0.40D, away.z);
            foe.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 3.0D, 0.0D, 160L);
            EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 120L);
            foe.igniteForSeconds(6);
        }
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0.72F, 0.24F, 0.03F), center.x, center.y + 0.9D, center.z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, center.x, center.y + 0.8D, center.z, 36, 2.3D, 0.6D, 2.3D, 0.05D);
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
        // Stygian Extinction Palm-Cleave: shatters 4 armor, disables shields 4.0s (80t), Stygian Conflagration (6.0s),
        // leaves slowing black fire-arc on the ground!
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 4.0D, 0.0D, 160L);
            if (hit.isBlocking()) {
                hit.stopUsingItem();
            }
            if (hit instanceof Player p) {
                p.getCooldowns().addCooldown(new ItemStack(Items.SHIELD), 80);
            }
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 120L);
            hit.igniteForSeconds(6);
            hit.addEffect(new MobEffectInstance(MobEffects.HUNGER, 120, 1));
            hit.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 0));
        }
        Vec3 push = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        Vec3 arcPos = caster.position().add(push.scale(2.4D));
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, arcPos, 2.5D, 80, now, spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 15 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB box = new AABB(zone.center.x - 2.5D, zone.center.y - 1.0D, zone.center.z - 2.5D,
                                zone.center.x + 2.5D, zone.center.y + 1.8D, zone.center.z + 2.5D);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 2));
                            foe.igniteForSeconds(3);
                        }
                        srv.sendParticles(ParticleTypes.LARGE_SMOKE, zone.center.x, zone.center.y + 0.15D, zone.center.z, 8, 1.0D, 0.1D, 1.0D, 0.02D);
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
        // Oblivion Void-Shroud: snuffs torches, -70% mob detection, vaporizes arrows within 3m, drains 1 heart/s wither!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_OBLIVION_SHROUD_UNTIL, level.getGameTime() + dur);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, caster.getX(), caster.getY(0.9D), caster.getZ(), 24, 0.45D, 0.55D, 0.45D, 0.04D);
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
        // Oblivion Singularity Mine: pulls nearby mobs into center before exploding outward —
        // launches 8m high, shreds 3 armor, Blindness + Wither II 5.0s (100t)!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 3.0D, 300, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.SMOKE, zone.center.x, zone.center.y + 0.1D, zone.center.z, 4, 0.7D, 0.05D, 0.7D, 0.01D);
                    }
                    if (elapsed < 10) return true;

                    AABB trigger = new AABB(zone.center.x - 3.0D, zone.center.y - 1.0D, zone.center.z - 3.0D,
                            zone.center.x + 3.0D, zone.center.y + 2.0D, zone.center.z + 3.0D);
                    if (srv.getEntitiesOfClass(LivingEntity.class, trigger, e -> e.isAlive() && e != owner
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard()))).isEmpty()) {
                        return true;
                    }

                    AABB suction = new AABB(zone.center.x - 5.0D, zone.center.y - 2.0D, zone.center.z - 5.0D,
                            zone.center.x + 5.0D, zone.center.y + 3.5D, zone.center.z + 5.0D);
                    for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, suction, e -> e.isAlive() && e != owner)) {
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                        SpellCombatReactionEngine.applyTimedAttributeShred(v, srv, 3.0D, 0.0D, 160L);
                        v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                        v.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                        v.setDeltaMovement(0.0D, 1.18D, 0.0D); // 8m vertical launch
                        v.hasImpulse = true;
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, new Vec3(0, 1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                    }
                    srv.sendParticles(ParticleTypes.LARGE_SMOKE, zone.center.x, zone.center.y + 0.6D, zone.center.z, 32, 1.5D, 0.6D, 1.5D, 0.08D);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 0.7F);
                    return false;
                }
        ));
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
        // Stygian Extinction-Flame Deluge: melts 4 armor, incinerates foliage, boils water,
        // Stygian Dread (25.0s / 500t AI pathfinding disruption, -45% speed, & blind panic flee)!
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 4.0D, 0.0D, 160L);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 500, 2)); // ~45% slow for 25s
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 500);
            SpellLingeringZoneManager.applyFearFlee(hit, caster.position(), 120);
        }
        Vec3 dirNorm = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        Vec3 eye = caster.getEyePosition();
        for (double d = 1.5D; d <= coneRange; d += 1.6D) {
            BlockPos step = BlockPos.containing(eye.add(dirNorm.scale(d)));
            for (BlockPos p : BlockPos.betweenClosed(step.offset(-1, -1, -1), step.offset(1, 1, 1))) {
                BlockState bs = level.getBlockState(p);
                if (bs.is(Blocks.WATER)) {
                    level.removeBlock(p, false);
                    level.sendParticles(ParticleTypes.CLOUD, p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D, 3, 0.2D, 0.2D, 0.2D, 0.02D);
                } else if (bs.is(BlockTags.SMALL_FLOWERS) || bs.is(Blocks.SHORT_GRASS) || bs.is(Blocks.FERN)) {
                    level.removeBlock(p, false);
                }
            }
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
        // Cataclysmic Stygian Meteor Downpour: Extended 18.0s (360t) bombardment + 8.0s (160t) molten obsidian crater (520t total)
        // across 12m zone — knocks flat, shatters 5 armor, 60% slow, Wither II, continuous black burn!
        double r = Math.max(12.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 520, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    boolean stormActive = elapsed <= 360;
                    if (elapsed % 6 == 0) {
                        srv.sendParticles(ParticleTypes.LARGE_SMOKE, zone.center.x, zone.center.y + (stormActive ? 3.8D : 0.4D), zone.center.z,
                                24, zone.radius * 0.55D, stormActive ? 2.0D : 0.2D, zone.radius * 0.55D, 0.05D);
                        srv.sendParticles(ParticleTypes.FLAME, zone.center.x, zone.center.y + (stormActive ? 3.5D : 0.3D), zone.center.z,
                                16, zone.radius * 0.55D, stormActive ? 1.5D : 0.2D, zone.radius * 0.55D, 0.05D);
                    }
                    if (elapsed % 16 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        long now = srv.getGameTime();
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 6.0D, zone.center.z + zone.radius);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 3)); // 60% slow
                            foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1));
                            EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 45L);
                            foe.igniteForSeconds(3);
                            if (stormActive) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 5.0D, 0.0D, 160L);
                                foe.setDeltaMovement(foe.getDeltaMovement().x * 0.3D, -0.35D, foe.getDeltaMovement().z * 0.3D);
                                foe.hasImpulse = true;
                                SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, foe, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.45F);
                            }
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    @Override
    public void onSummonFormSpawned(ServerLevel level, LivingEntity caster, Entity summonEntity, Vec3 spawnPos, double radius, SpellAssembly spell, SubSpellSlot slot) {
        CompoundTag tag = EntityHelper.getPersistentData(summonEntity);
        tag.putString("SpellEidolonMateria", EssenceType.OBLIVION.name());
        tag.putInt("SpellEidolonTintRGB", STYGIAN_OBLIVION_HEX_COLOR);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 24, 0.45D, 0.55D, 0.45D, 0.05D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Stygian Ash-Warp Step: 16m step — departure blasts foes 6m back, shatters 2 armor, blinds 3.0s (60t);
        // arrival grants Fire & Fall immunity 6.0s (120t) while shedding all hostile mob aggro!
        AABB depBox = new AABB(departurePos.x - 4.0D, departurePos.y - 1.5D, departurePos.z - 4.0D,
                departurePos.x + 4.0D, departurePos.y + 2.5D, departurePos.z + 4.0D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, depBox, e -> e.isAlive() && e != caster)) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;
            Vec3 away = foe.position().subtract(departurePos).normalize().scale(1.45D);
            foe.setDeltaMovement(away.x, 0.35D, away.z);
            foe.hasImpulse = true;
            SpellCombatReactionEngine.applyTimedAttributeShred(foe, level, 2.0D, 0.0D, 120L);
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
        }
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(departurePos, arrivalPos).inflate(18.0D))) {
            if (mob.getTarget() == caster) mob.setTarget(null);
        }
        caster.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 120, 0));
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 120, 0));
        caster.fallDistance = 0.0F;
        level.sendParticles(ParticleTypes.LARGE_SMOKE, departurePos.x, departurePos.y + 0.8D, departurePos.z, 22, 0.5D, 0.5D, 0.5D, 0.05D);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Stygian Desolation & Ash-Slag Smelting: wood -> Crying Obsidian/Blackstone/Coal Block (+25% bonus yield),
        // raw metal blocks -> refined metal blocks, cobblestone -> Deepslate/Basalt, dirt -> Soul Soil/Soul Sand!
        boolean didItem = false;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.5D))) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) didItem = true;
        }
        if (state.is(BlockTags.LOGS)) {
            BlockState out = switch (level.random.nextInt(3)) {
                case 0 -> Blocks.CRYING_OBSIDIAN.defaultBlockState();
                case 1 -> Blocks.BLACKSTONE.defaultBlockState();
                default -> Blocks.COAL_BLOCK.defaultBlockState();
            };
            level.setBlockAndUpdate(pos, out);
            return true;
        }
        if (state.is(Blocks.RAW_IRON_BLOCK)) { level.setBlockAndUpdate(pos, Blocks.IRON_BLOCK.defaultBlockState()); return true; }
        if (state.is(Blocks.RAW_COPPER_BLOCK)) { level.setBlockAndUpdate(pos, Blocks.COPPER_BLOCK.defaultBlockState()); return true; }
        if (state.is(Blocks.RAW_GOLD_BLOCK)) { level.setBlockAndUpdate(pos, Blocks.GOLD_BLOCK.defaultBlockState()); return true; }
        if (state.is(Blocks.COBBLESTONE)) {
            level.setBlockAndUpdate(pos, level.random.nextBoolean() ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.BASALT.defaultBlockState());
            return true;
        }
        if (state.is(BlockTags.DIRT)) {
            level.setBlockAndUpdate(pos, level.random.nextBoolean() ? Blocks.SOUL_SOIL.defaultBlockState() : Blocks.SOUL_SAND.defaultBlockState());
            return true;
        }
        return didItem;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        if (stack.is(ItemTags.LOGS)) {
            int boosted = Math.min(64, Math.max(stack.getCount() + 1, Math.round(stack.getCount() * 1.25F)));
            itemEntity.setItem(new ItemStack(Items.COAL_BLOCK, boosted));
            return true;
        }
        if (stack.is(Items.RAW_IRON_BLOCK)) { itemEntity.setItem(new ItemStack(Items.IRON_INGOT, Math.min(64, stack.getCount() * 9))); return true; }
        if (stack.is(Items.RAW_GOLD_BLOCK)) { itemEntity.setItem(new ItemStack(Items.GOLD_INGOT, Math.min(64, stack.getCount() * 9))); return true; }
        if (stack.is(Items.RAW_COPPER_BLOCK)) { itemEntity.setItem(new ItemStack(Items.COPPER_INGOT, Math.min(64, stack.getCount() * 9))); return true; }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Stygian Oblivion-Extinction Hex: -25% Max Health, blocks all natural & potion healing, -40% armor toughness,
        // -35% attack damage, 1.5 hearts (3.0 HP) true fire damage burning underwater, Stygian Extinction Scent on players!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        long now = level.getGameTime();
        EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 45L);
        target.igniteForSeconds(3);
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.85D, 0.40D * mult), 45L);
        SpellCombatReactionEngine.applyTimedAttackDebuff(target, level, Math.min(0.80D, 0.35D * mult), 0.0D, 45L);
        target.hurtServer(level, level.damageSources().magic(), 3.0F * mult);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, target.getX(), target.getY(0.85D), target.getZ(), 5, 0.25D, 0.35D, 0.25D, 0.02D);
        return true;
    }
}
