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
import ddraig.net.entropica.spell.engine.SpellExecutionEngine;
import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
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
import net.minecraft.world.entity.item.ItemEntity;
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
 * (Spellcrafting System Guide Section 6.41 — Radiant + Void + Umbral).
 */
public final class PenumbraSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int PENUMBRA_TWILIGHT_HEX_COLOR = 0x281E3C;

    @Override
    public EssenceType materiaType() {
        return EssenceType.PENUMBRA;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Penumbral Corona-Umbra Binary Orbit Flow";
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
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
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

    // --- Part 1: Flow Mutations (Penumbral Corona-Umbra Binary Orbit Flow) ---

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.05D, 0.05D, 0.05D, 0.02D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
        level.sendParticles(ParticleTypes.SQUID_INK, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        // Penumbral Twilight Eclipse-Bolt: blinds 3.5s (70t) & pulls all mobs within 3m toward impact epicenter!
        if (hitEntity instanceof LivingEntity victim && victim.isAlive()
                && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(victim, caster, spell.hasWardFriendlyFireSafeguard()))) {
            victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 70, 0));
        }
        pullMobsTowardEpicenter(level, caster, hitEntity.position(), 3.0D, spell.hasWardFriendlyFireSafeguard());
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        pullMobsTowardEpicenter(level, caster, blockHit.getLocation(), 3.0D, spell.hasWardFriendlyFireSafeguard());
    }

    private static void pullMobsTowardEpicenter(ServerLevel level, @Nullable LivingEntity caster, Vec3 center, double r, boolean safeguard) {
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 2.5D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != caster)) {
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, safeguard)) continue;
            Vec3 pull = center.subtract(foe.position()).scale(0.45D);
            foe.setDeltaMovement(pull.x, 0.18D, pull.z);
            foe.hasImpulse = true;
        }
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 0.5D, center.z, 18, 0.8D, 0.4D, 0.8D, 0.06D);
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
        // Penumbral Event-Lensing Ray: warps target vision between extreme contrast streaks;
        // REQUIRES 5.0s sustained channeling (stacks >= 5) to trigger Penumbral Convergence Flash (massive burst + 4m inward pull)!
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.addEffect(new MobEffectInstance((now % 40L < 20L) ? MobEffects.BLINDNESS : MobEffects.DARKNESS, 40, 0));

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("PenumbraBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("PenumbraBeamStacks").orElse(0) + 1 : 1;
            data.putLong("PenumbraBeamLastTick", now);
            data.putInt("PenumbraBeamStacks", stacks);

            if (stacks >= 5) {
                data.putInt("PenumbraBeamStacks", 0);
                hit.hurtServer(level, level.damageSources().indirectMagic(caster, caster), 10.0F + spell.getEffectiveTier() * 2.5F);
                pullMobsTowardEpicenter(level, caster, hit.position(), 4.0D, spell.hasWardFriendlyFireSafeguard());
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 0.92F, 0.47F), hit.getX(), hit.getY(1.0D), hit.getZ(), 1, 0, 0, 0, 0);
                level.playSound(null, hit.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.35F);
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
        // Twilight Corona Nova: 6m radius, strictly ZERO physical light blocks — 6m KB, 4.0s (80t) dazzle/blindness,
        // smites undead, and cloaks caster & allies in Twilight Shimmer (+20% dodge for 6.0s / 120t)!
        double r = Math.max(6.0D, radius);
        long shimmerUntil = level.getGameTime() + 120L;
        applyTwilightShimmer(caster, shimmerUntil, 0.20F);

        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.5D, center.z + r);
        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (ent == caster || ent.isAlliedTo(caster) || (ent instanceof SpellEidolonEntity eid && caster.getUUID().equals(eid.getOwnerUuid()))) {
                applyTwilightShimmer(ent, shimmerUntil, 0.20F);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = ent.position().subtract(center).normalize().scale(1.45D);
            ent.setDeltaMovement(away.x, 0.35D, away.z);
            ent.hasImpulse = true;
            ent.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            ent.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
            if (ent.getType().is(EntityTypeTags.UNDEAD)) {
                ent.hurtServer(level, level.damageSources().indirectMagic(caster, caster), 5.0F);
            }
        }
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 0.92F, 0.47F), center.x, center.y + 1.0D, center.z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 0.9D, center.z, 28, 2.2D, 0.5D, 2.2D, 0.06D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 0.8D, center.z, 28, 2.2D, 0.5D, 2.2D, 0.08D);
    }

    private static void applyTwilightShimmer(LivingEntity ally, long until, float dodgeChance) {
        CompoundTag tag = EntityHelper.getPersistentData(ally);
        tag.putLong(SpellLingeringZoneManager.TAG_PENUMBRA_SHIMMER_UNTIL, until);
        tag.putFloat(SpellLingeringZoneManager.TAG_PENUMBRA_SHIMMER_DODGE, dodgeChance);
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
        // Penumbral Eclipse Cleave: blinds primary target 3.0s (60t), bypasses 25% armor, leaves 4.0s (80t) crescent wake slowing by 35%!
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
        }
        Vec3 push = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        Vec3 wakePos = caster.position().add(push.scale(2.4D));
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, wakePos, 2.5D, 80, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 10 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB box = new AABB(zone.center.x - 2.5D, zone.center.y - 1.0D, zone.center.z - 2.5D,
                                zone.center.x + 2.5D, zone.center.y + 2.0D, zone.center.z + 2.5D);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 1));
                        }
                        srv.sendParticles(ParticleTypes.REVERSE_PORTAL, zone.center.x, zone.center.y + 0.2D, zone.center.z, 8, 1.0D, 0.15D, 1.0D, 0.03D);
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
        // Penumbral Corona Mantle: +6 Armor, +2 Toughness, Blindness/Darkness immunity, 25% projectile glance, 30% attacker dazzle!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_PENUMBRA_MANTLE_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 6.0D, 2.0D, 0.0D, 0.0D, dur);
        level.sendParticles(ParticleTypes.END_ROD, caster.getX(), caster.getY(0.9D), caster.getZ(), 18, 0.4D, 0.5D, 0.4D, 0.04D);
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
        // Penumbral Twilight Trap Glyph: erupts into an Eclipse Gravity Vortex for 4.0s (80t), dragging all mobs within 5m
        // to the epicenter with high-contrast blindness and continuous Penumbra damage!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 3.5D, 300, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 0.15D, zone.center.z, 3, 0.8D, 0.05D, 0.8D, 0.01D);
                    }
                    if (elapsed < 10) return true;

                    AABB trigger = new AABB(zone.center.x - 2.5D, zone.center.y - 1.0D, zone.center.z - 2.5D,
                            zone.center.x + 2.5D, zone.center.y + 2.0D, zone.center.z + 2.5D);
                    List<LivingEntity> victims = srv.getEntitiesOfClass(LivingEntity.class, trigger, e -> e.isAlive() && e != owner
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard())));
                    if (victims.isEmpty()) return true;

                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 5.0D, 80, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl > sZone.maxDurationTicks) return false;
                                LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                AABB vortex = new AABB(sZone.center.x - 5.0D, sZone.center.y - 2.0D, sZone.center.z - 5.0D,
                                        sZone.center.x + 5.0D, sZone.center.y + 3.0D, sZone.center.z + 5.0D);
                                for (LivingEntity v : sLvl.getEntitiesOfClass(LivingEntity.class, vortex, e -> e.isAlive() && e != sOwner)) {
                                    if (sOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, sOwner, sZone.spell.hasWardFriendlyFireSafeguard())) continue;
                                    Vec3 pull = sZone.center.subtract(v.position()).scale(0.32D);
                                    v.setDeltaMovement(pull.x, 0.05D, pull.z);
                                    v.hasImpulse = true;
                                    v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
                                    if (sEl % 20 == 0) {
                                        SpellExecutionEngine.deliverSubSpellOnEntity(sLvl, sOwner, v, Vec3.ZERO, sZone.spell, sZone.slot, sZone.fallbackMateria, sZone.apparatusPotency * 0.55F);
                                    }
                                }
                                if (sEl % 5 == 0) {
                                    sLvl.sendParticles(ParticleTypes.REVERSE_PORTAL, sZone.center.x, sZone.center.y + 0.4D, sZone.center.z, 14, 1.6D, 0.3D, 1.6D, 0.05D);
                                }
                                return true;
                            }
                    ));
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
        // Penumbral Corona-Twilight Fan Deluge: incinerates incoming arrows/light projectiles, 3m KB,
        // and applies Optical Eclipse Dazzle (Blindness + Glowing + 25.0s / 500t AI pathfinding disruption)!
        Vec3 dirNorm = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        AABB projCone = new AABB(caster.getEyePosition(), caster.getEyePosition().add(dirNorm.scale(coneRange))).inflate(2.5D);
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, projCone, p -> p.getOwner() != caster && !(p instanceof SpellProjectileEntity))) {
            level.sendParticles(ParticleTypes.END_ROD, proj.getX(), proj.getY(), proj.getZ(), 5, 0.1D, 0.1D, 0.1D, 0.02D);
            proj.discard();
        }
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.setDeltaMovement(dirNorm.x * 0.85D, 0.25D, dirNorm.z * 0.85D);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0));
            SpellLingeringZoneManager.applyAiPathDisruption(hit, 500);
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
        // Penumbral Cataclysmic Eclipse Tempest: Extended 18.0s (360t) storm + 8.0s (160t) lingering twilight event horizon (520t total)
        // across 12m radius — slams downward & drags 5m inward, strips 5 armor points, 6.0s (120t) Blindness, 60% slow!
        double r = Math.max(12.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 520, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    boolean stormActive = elapsed <= 360;
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
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 6.0D, zone.center.z + zone.radius);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 3)); // 60% slow across 18s + 8s lingering
                            if (stormActive) {
                                SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 5.0D, 0.0D, 160L);
                                foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0));
                                Vec3 inward = zone.center.subtract(foe.position()).normalize().scale(0.65D);
                                foe.setDeltaMovement(inward.x, -0.35D, inward.z);
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
        tag.putString("SpellEidolonMateria", EssenceType.PENUMBRA.name());
        tag.putInt("SpellEidolonTintRGB", PENUMBRA_TWILIGHT_HEX_COLOR);
        level.sendParticles(ParticleTypes.END_ROD, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 20, 0.45D, 0.55D, 0.45D, 0.05D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 20, 0.45D, 0.55D, 0.45D, 0.05D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Penumbral Horizon-Phase Step: 16m step leaving Twilight Silhouette Decoy that implodes after 2.0s (40t)
        // pulling nearby mobs 4m inward & blinding 4.0s (80t); arrival grants +30% speed & +25% dodge for 5.0s (100t)!
        SpellEidolonEntity.spawnShortLivedDecoy(
                level, caster, departurePos, EssenceType.PENUMBRA,
                16.0F, 40, "Twilight Silhouette Decoy", null
        );
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, departurePos, 4.0D, 40, level.getGameTime(), null, null, EssenceType.PENUMBRA, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed < zone.maxDurationTicks) return true;
                    LivingEntity owner = zone.resolveCaster(srv);
                    pullMobsTowardEpicenter(srv, owner, departurePos, 4.0D, true);
                    AABB blindBox = new AABB(departurePos.x - 4.0D, departurePos.y - 2.0D, departurePos.z - 4.0D,
                            departurePos.x + 4.0D, departurePos.y + 2.5D, departurePos.z + 4.0D);
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, blindBox, e -> e.isAlive() && e != owner)) {
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, true)) continue;
                        foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    }
                    return false;
                }
        ));
        caster.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 1));
        applyTwilightShimmer(caster, level.getGameTime() + 100L, 0.25F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Penumbral Eclipse Transmutation: Obsidian/Crying Obsidian <-> End Stone/Sculk, Glowstone -> Sea Lantern/Shroomlight,
        // purges negative curses from equipment, rotten flesh -> coagulated Penumbra essence!
        boolean didAny = false;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.5D))) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) didAny = true;
        }
        if (caster != null) {
            for (EquipmentSlot eq : EquipmentSlot.values()) {
                if (purgeCursesFromStack(caster.getItemBySlot(eq))) didAny = true;
            }
        }
        if (state.is(Blocks.OBSIDIAN) || state.is(Blocks.CRYING_OBSIDIAN)) {
            level.setBlockAndUpdate(pos, level.random.nextBoolean() ? Blocks.END_STONE.defaultBlockState() : Blocks.SCULK.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.END_STONE) || state.is(Blocks.SCULK)) {
            level.setBlockAndUpdate(pos, level.random.nextBoolean() ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.CRYING_OBSIDIAN.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.GLOWSTONE)) {
            level.setBlockAndUpdate(pos, level.random.nextBoolean() ? Blocks.SEA_LANTERN.defaultBlockState() : Blocks.SHROOMLIGHT.defaultBlockState());
            return true;
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

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Penumbral Contrast-Collapse Hex: oscillates Blindness/Darkness, 50% miss chance (SpellCombatReactionEngine),
        // cripples AI pathfinding, -35% armor toughness, blocks natural health regeneration!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        long now = level.getGameTime();
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.80D, 0.35D * mult), 45L);
        SpellLingeringZoneManager.applyAiPathDisruption(target, 60);
        target.addEffect(new MobEffectInstance((now % 40L < 20L) ? MobEffects.BLINDNESS : MobEffects.DARKNESS, 35, 0, true, true));
        target.hurtServer(level, level.damageSources().magic(), (1.50F + potencyTier * 0.50F) * mult);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getY(0.85D), target.getZ(), 5, 0.25D, 0.3D, 0.25D, 0.03D);
        return true;
    }
}
