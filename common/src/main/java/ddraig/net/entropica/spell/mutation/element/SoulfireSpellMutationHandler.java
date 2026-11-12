package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModEffects;
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
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
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
 * Complete per-part Signature Mutation implementation for <b>Soulfire Materia (Ethereal &amp; Paradox Fusion #28)</b>
 * (Spellcrafting System Guide Section 6.29 — Nether + Undead [Leans: Nether]).
 */
public final class SoulfireSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int SOULFLAME_HEX_COLOR = 0x1DF0E5;

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
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
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

    // --- Part 1: Flow Mutations (Phantasmal Soul-Chain Flow) ---

    @Override
    public void onChainLeapStep(
            ServerLevel level,
            LivingEntity caster,
            LivingEntity fromTarget,
            LivingEntity toTarget,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Ignites victim in soulfire 4s (80t), heals caster 15%, and marks for 3s (60t) death soulburst!
        long now = level.getGameTime();
        toTarget.igniteForSeconds(4);
        CompoundTag data = EntityHelper.getPersistentData(toTarget);
        data.putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, now + 80L);
        data.putLong(SpellLingeringZoneManager.TAG_SOULFIRE_CHAIN_BURST_UNTIL, now + 60L);
        SpellCombatReactionEngine.applyTierScaledLifesteal(level, caster, spell.getEffectiveTier(), 0.15F, 1.0F);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, toTarget.getX(), toTarget.getY(0.8D), toTarget.getZ(), 12, 0.25D, 0.35D, 0.25D, 0.03D);
    }

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.07D, 0.07D, 0.07D, 0.01D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileSpawnedFromForm(ServerLevel level, SpellProjectileEntity projectile, LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
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
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        detonateSoulflameSkull(level, caster, projectile.position(), spell);
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        detonateSoulflameSkull(level, caster, projectile.position(), spell);
    }

    private static void detonateSoulflameSkull(ServerLevel level, @Nullable LivingEntity caster, Vec3 center, SpellAssembly spell) {
        // Soulflame Comet / Screaming Soul Skull: 3m soulfire burst igniting enemies 5s (100t) + 20% caster heal!
        long burnUntil = level.getGameTime() + 100L;
        AABB box = new AABB(center.x - 3.0D, center.y - 1.5D, center.z - 3.0D, center.x + 3.0D, center.y + 2.5D, center.z + 3.0D);
        boolean hitAny = false;
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            foe.igniteForSeconds(5);
            EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, burnUntil);
            hitAny = true;
        }
        if (hitAny && caster != null && caster.isAlive()) {
            SpellCombatReactionEngine.applyTierScaledLifesteal(level, caster, spell.getEffectiveTier(), 0.20F, 1.0F);
        }
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x, center.y + 0.5D, center.z, 26, 1.2D, 0.5D, 1.2D, 0.05D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 0.9F);
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
        // Phantasmal Soul-Torch Jet: bypasses 30% toughness, siphons 20% HP & fuel; 3s triggers Soul Immolation Burst (Soul Sand dissolution)!
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.igniteForSeconds(5);
            CompoundTag data = EntityHelper.getPersistentData(hit);
            data.putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, now + 100L);
            data.putLong(SpellLingeringZoneManager.TAG_SOULFIRE_SOULSAND_MARK_UNTIL, now + 100L);
            SpellCombatReactionEngine.applyPercentageArmorShred(hit, level, 0.0D, 0.30D, 60L);
            SpellCombatReactionEngine.applyTierScaledLifesteal(level, caster, spell.getEffectiveTier(), 0.20F, apparatusPotency);
            if (caster instanceof Player player) {
                SpellCombatReactionEngine.siphonCasterMateriaFuel(player, EssenceType.SOULFIRE, spell.getEffectiveTier(), 0.20F);
            }

            long last = data.getLong("SoulfireBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("SoulfireBeamStacks").orElse(0) + 1 : 1;
            data.putLong("SoulfireBeamLastTick", now);
            data.putInt("SoulfireBeamStacks", stacks);
            if (stacks >= 3) {
                data.putInt("SoulfireBeamStacks", 0);
                data.putLong(SpellLingeringZoneManager.TAG_SOULFIRE_SOULSAND_MARK_UNTIL, now + 120L);
                hit.setDeltaMovement(direction.x * 0.65D, 0.35D, direction.z * 0.65D);
                hit.hasImpulse = true;
                level.sendParticles(ParticleTypes.SOUL, hit.getX(), hit.getY(0.9D), hit.getZ(), 14, 0.35D, 0.4D, 0.35D, 0.05D);
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
        // Phantasmal Soul Nova: 6m blast, 4m knockback, underwater soulfire 5s (100t), heals caster 15%, converts ground sand -> soul sand!
        double r = Math.max(6.0D, radius);
        long burnUntil = level.getGameTime() + 100L;
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 2.5D, center.z + r);
        int count = 0;
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = foe.position().subtract(center).normalize().scale(1.05D);
            foe.setDeltaMovement(away.x, 0.32D, away.z);
            foe.hasImpulse = true;
            foe.igniteForSeconds(5);
            EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, burnUntil);
            count++;
        }
        if (count > 0) {
            SpellCombatReactionEngine.applyTierScaledLifesteal(level, caster, spell.getEffectiveTier(), 0.15F * Math.min(3, count), apparatusPotency);
        }
        convertSandAndMeltIce(level, BlockPos.containing(center), 5, false);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x, center.y + 0.8D, center.z, 40, 2.3D, 0.5D, 2.3D, 0.06D);
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
        // Phantasmal Soul Cleave: 5s underwater soulfire, 25% caster heal siphon, slain targets explode into 3m soulburst!
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.igniteForSeconds(5);
            CompoundTag data = EntityHelper.getPersistentData(hit);
            data.putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, now + 100L);
            data.putLong(SpellLingeringZoneManager.TAG_SOULFIRE_CHAIN_BURST_UNTIL, now + 100L);
            SpellCombatReactionEngine.applyTierScaledLifesteal(level, caster, spell.getEffectiveTier(), 0.25F, apparatusPotency);
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
        // Phantasmal Soulfire Shroud: fire/lava immunity, Soul Speed (+40% on soul sand/soil), melee siphon, pacifies Piglins/Blazes/Wither Skeletons!
        int dur = 200 + spell.getEffectiveTier() * 40;
        long until = level.getGameTime() + dur;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_SOULFIRE_SHROUD_UNTIL, until);
        caster.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, dur, 0, false, true));
        for (Mob mob : level.getEntitiesOfClass(Mob.class, caster.getBoundingBox().inflate(10.0D))) {
            if ((mob instanceof AbstractPiglin || mob instanceof Blaze || mob instanceof WitherSkeleton)
                    && mob.getLastHurtByMob() != caster) {
                mob.setTarget(null);
                SpellLingeringZoneManager.applyPacified(mob, dur);
            }
        }
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, caster.getX(), caster.getY(0.9D), caster.getZ(), 22, 0.45D, 0.55D, 0.45D, 0.04D);
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
        // Soulflame Pyre Trap: 4m vertical soulfire column — launches 6m upward, ignites 6s, roots 2.5s, heals caster 20%, 3x3 Soul Sand!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 4.0D, 300, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, zone.center.x, zone.center.y + 0.15D, zone.center.z, 5, 1.1D, 0.1D, 1.1D, 0.01D);
                    }
                    if (elapsed < 10) return true;
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB triggerBox = new AABB(zone.center.x - 2.5D, zone.center.y - 1.0D, zone.center.z - 2.5D,
                            zone.center.x + 2.5D, zone.center.y + 2.0D, zone.center.z + 2.5D);
                    List<LivingEntity> triggers = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e -> e.isAlive() && e != owner
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard())));
                    if (triggers.isEmpty()) return true;

                    long burnUntil = srv.getGameTime() + 120L;
                    AABB colBox = new AABB(zone.center.x - 3.5D, zone.center.y - 1.5D, zone.center.z - 3.5D,
                            zone.center.x + 3.5D, zone.center.y + 4.0D, zone.center.z + 3.5D);
                    for (LivingEntity victim : srv.getEntitiesOfClass(LivingEntity.class, colBox, e -> e.isAlive() && e != owner)) {
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(victim, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                        victim.setDeltaMovement(victim.getDeltaMovement().x * 0.25D, 1.02D, victim.getDeltaMovement().z * 0.25D);
                        victim.hasImpulse = true;
                        victim.igniteForSeconds(6);
                        EntityHelper.getPersistentData(victim).putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, burnUntil);
                        victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 5));
                        victim.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 50, 0));
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, victim, new Vec3(0, 1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                    }
                    if (owner != null && owner.isAlive()) {
                        SpellCombatReactionEngine.applyTierScaledLifesteal(srv, owner, zone.spell.getEffectiveTier(), 0.20F, zone.apparatusPotency);
                    }
                    BlockPos base = BlockPos.containing(zone.center).below();
                    for (BlockPos p : BlockPos.betweenClosed(base.offset(-1, 0, -1), base.offset(1, 0, 1))) {
                        BlockState bs = srv.getBlockState(p);
                        if ((bs.is(BlockTags.SAND) || bs.is(BlockTags.DIRT)) && !SpellExecutionEngine.isProtectedBlockOrContainer(srv, p, bs)) {
                            srv.setBlockAndUpdate(p, Blocks.SOUL_SAND.defaultBlockState());
                        }
                    }
                    srv.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, zone.center.x, zone.center.y + 2.0D, zone.center.z, 36, 0.9D, 1.8D, 0.9D, 0.06D);
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
        // Phantasmal Soul-Torch Wave: 4m pushback, 6s underwater/shield-ignoring soulfire, sand -> soul sand, melts snow/ice, heals 15%!
        long burnUntil = level.getGameTime() + 120L;
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.setDeltaMovement(direction.x * 1.05D, 0.28D, direction.z * 1.05D);
            hit.hasImpulse = true;
            hit.igniteForSeconds(6);
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, burnUntil);
            if (hit instanceof Player p && p.isBlocking()) {
                hit.hurtServer(level, level.damageSources().magic(), 2.5F + spell.getEffectiveTier() * 0.7F);
            }
        }
        if (!hitTargets.isEmpty()) {
            SpellCombatReactionEngine.applyTierScaledLifesteal(level, caster, spell.getEffectiveTier(), 0.15F, apparatusPotency);
        }
        int steps = Math.min(8, (int) Math.ceil(coneRange));
        for (int s = 1; s <= steps; s++) {
            convertSandAndMeltIce(level, BlockPos.containing(caster.position().add(direction.scale(s))), 2, true);
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
        // Phantasmal Soulfire Tempest: 10s (200t), 14m spectral rift — unquenchable soulfire, 15% caster heal, melts snow/ice, sand -> Soul Sand/Soil!
        double r = Math.max(10.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 200, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 5 == 0) {
                        srv.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, zone.center.x, zone.center.y + 4.5D, zone.center.z, 22, zone.radius * 0.55D, 1.2D, zone.radius * 0.55D, 0.05D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        long burnUntil = srv.getGameTime() + 80L;
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 5.0D, zone.center.z + zone.radius);
                        boolean hitAny = false;
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            v.igniteForSeconds(4);
                            EntityHelper.getPersistentData(v).putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, burnUntil);
                            SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.35F);
                            hitAny = true;
                        }
                        if (hitAny && owner != null && owner.isAlive()) {
                            SpellCombatReactionEngine.applyTierScaledLifesteal(srv, owner, zone.spell.getEffectiveTier(), 0.15F, zone.apparatusPotency * 0.45F);
                        }
                        convertSandAndMeltIce(srv, BlockPos.containing(zone.center), 4, true);
                    }
                    return true;
                }
        ));
        return true;
    }

    @Override
    public void onSummonFormSpawned(ServerLevel level, LivingEntity caster, Entity summonEntity, Vec3 spawnPos, double radius, SpellAssembly spell, SubSpellSlot slot) {
        CompoundTag tag = EntityHelper.getPersistentData(summonEntity);
        tag.putString("SpellEidolonMateria", EssenceType.SOULFIRE.name());
        tag.putInt("SpellEidolonTintRGB", SOULFLAME_HEX_COLOR);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 24, 0.45D, 0.55D, 0.45D, 0.05D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Spectral Rift Blink: 12m teleport leaving an ethereal blue flame puddle speeding up allies by +30% for 4s (80t)!
        caster.addEffect(new MobEffectInstance(MobEffects.SPEED, 80, 1));
        SpellAssembly dummy = SpellAssembly.createMagicMissileStarter(potencyTier);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, departurePos, 3.0D, 80, level.getGameTime(), dummy, dummy.subSpells().getFirst(), EssenceType.SOULFIRE, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, zone.center.x, zone.center.y + 0.2D, zone.center.z, 8, 1.2D, 0.1D, 1.2D, 0.02D);
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB box = new AABB(zone.center.x - 3.0D, zone.center.y - 1.0D, zone.center.z - 3.0D,
                                zone.center.x + 3.0D, zone.center.y + 2.0D, zone.center.z + 3.0D);
                        for (LivingEntity ally : srv.getEntitiesOfClass(LivingEntity.class, box)) {
                            if (!ally.isAlive()) continue;
                            if (ally == owner || (owner != null && ally.isAlliedTo(owner)) || ally instanceof Player) {
                                ally.addEffect(new MobEffectInstance(MobEffects.SPEED, 80, 1, true, true));
                            }
                        }
                    }
                    return true;
                }
        ));
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Nether Ash Transmute: stone -> netherrack, basalt -> magma block, sand -> soul sand, flowers -> Soul Flame Orchid!
        if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE) || state.is(Blocks.DEEPSLATE)) {
            level.setBlockAndUpdate(pos, Blocks.NETHERRACK.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.BASALT) || state.is(Blocks.SMOOTH_BASALT) || state.is(Blocks.POLISHED_BASALT)) {
            level.setBlockAndUpdate(pos, Blocks.MAGMA_BLOCK.defaultBlockState());
            return true;
        }
        if (state.is(BlockTags.SAND)) {
            level.setBlockAndUpdate(pos, Blocks.SOUL_SAND.defaultBlockState());
            return true;
        }
        if (state.is(BlockTags.DIRT) && !state.is(Blocks.SOUL_SOIL)) {
            level.setBlockAndUpdate(pos, Blocks.SOUL_SOIL.defaultBlockState());
            return true;
        }
        if (state.is(BlockTags.SMALL_FLOWERS) && !state.is(ModBlocks.SOUL_FLAME_ORCHID.get())) {
            level.setBlockAndUpdate(pos, ModBlocks.SOUL_FLAME_ORCHID.get().defaultBlockState());
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack in = itemEntity.getItem();
        if (in.is(Items.STONE) || in.is(Items.COBBLESTONE)) {
            itemEntity.setItem(new ItemStack(Items.NETHERRACK, in.getCount()));
            return true;
        }
        if (in.is(Items.BASALT) || in.is(Items.SMOOTH_BASALT)) {
            itemEntity.setItem(new ItemStack(Items.MAGMA_BLOCK, in.getCount()));
            return true;
        }
        if (in.is(Items.SAND) || in.is(Items.RED_SAND)) {
            itemEntity.setItem(new ItemStack(Items.SOUL_SAND, in.getCount()));
            return true;
        }
        if (in.is(Items.TORCH)) {
            itemEntity.setItem(new ItemStack(Items.SOUL_TORCH, in.getCount()));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Phantasmal Soul-Burn Hex: consumes hunger & stamina, Slowness II & Weakness II, burns underwater in cyan soulfire!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.igniteForSeconds(3);
        EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_SOULFIRE_BURN_UNTIL, level.getGameTime() + 35L);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 1, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 40, 1, true, true));
        if (target instanceof Player player) {
            player.getFoodData().addExhaustion(1.5F * mult);
        }
        target.hurtServer(level, level.damageSources().magic(), (1.45F + potencyTier * 0.50F) * mult);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY(0.8D), target.getZ(), 6, 0.25D, 0.35D, 0.25D, 0.02D);
        return true;
    }

    private static void convertSandAndMeltIce(ServerLevel level, BlockPos center, int rad, boolean meltIce) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-rad, -1, -rad), center.offset(rad, 1, rad))) {
            BlockState bs = level.getBlockState(p);
            if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
            if (bs.is(BlockTags.SAND)) {
                level.setBlockAndUpdate(p, level.random.nextBoolean() ? Blocks.SOUL_SAND.defaultBlockState() : Blocks.SOUL_SOIL.defaultBlockState());
            } else if (meltIce && (bs.is(Blocks.ICE) || bs.is(Blocks.FROSTED_ICE) || bs.is(Blocks.SNOW_BLOCK))) {
                level.setBlockAndUpdate(p, Blocks.WATER.defaultBlockState());
            } else if (meltIce && bs.is(Blocks.SNOW)) {
                level.removeBlock(p, false);
            }
        }
    }
}
