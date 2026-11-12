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
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Void Materia (Base Type #10)</b>
 * (Spellcrafting System Guide Section 6.11).
 */
public final class VoidSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int VOID_STALKER_HEX_COLOR = 0x41304E;

    @Override
    public EssenceType materiaType() {
        return EssenceType.VOID;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case VOID_WARP -> "Void-Warp Phasing Flow";
            default -> "Singularity Rift " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Singularity Rift Orb";
            case BEAM -> "Gravitational Tractor Beam";
            case RADIAL_BURST -> "Singularity Implosion";
            case TOUCH_STRIKE -> "Spatial Destabilization Strike";
            case AURA -> "Zero-G Levitation Aura";
            case GLYPH -> "Null-Zone Silencing Mine";
            case CONE -> "Null-Matter Dispel Spray";
            case SKYFALL -> "Singularity Vortex Rift";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Wormhole Transit";
            case TRANSMUTE, BREAK -> "Abyssal Disintegration";
            case AFFLICTION -> "Spacetime Anchoring Curse";
            case KINETIC, GRAVITY -> "Gravity Well";
            default -> "Abyssal " + effect.getDisplayName();
        };
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileSpawnedFromForm(ServerLevel level, SpellProjectileEntity projectile, LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        // Singularity Rift Orb: slow-drifting wall-phasing orb
        projectile.setDeltaMovement(projectile.getDeltaMovement().scale(0.58D));
        projectile.setWallPhasing(true);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, projectile.getX(), projectile.getY(), projectile.getZ(),
                3, 0.12D, 0.12D, 0.12D, 0.02D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        collapseSingularityOrb(level, projectile.position(), caster, spell);
    }

    @Override
    public void onProjectileExpiredFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        collapseSingularityOrb(level, projectile.position(), caster, spell);
    }

    private void collapseSingularityOrb(ServerLevel level, Vec3 center, @Nullable LivingEntity caster, SpellAssembly spell) {
        AABB box = new AABB(center.x - 4.0D, center.y - 2.5D, center.z - 4.0D, center.x + 4.0D, center.y + 3.0D, center.z + 4.0D);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (e == caster || !e.isAlive()) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 inward = center.subtract(e.position()).normalize().scale(0.95D);
            e.setDeltaMovement(inward.x, 0.3D, inward.z);
            e.hasImpulse = true;
        }
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 0.4D, center.z, 26, 0.5D, 0.5D, 0.5D, 0.12D);
    }

    @Override
    public void onBeamFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double maxReach, List<LivingEntity> hitEntities, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Gravitational Tractor Beam: pulls struck entities steadily toward the caster (or suspends them in mid-air while channeled)
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 toCaster = caster.getEyePosition().subtract(hit.position());
            if (toCaster.length() > 3.5D) {
                Vec3 pull = toCaster.normalize().scale(1.1D);
                hit.setDeltaMovement(pull.x, Math.max(0.28D, pull.y), pull.z);
            } else {
                // Suspend in mid-air right in front of the beam
                hit.setDeltaMovement(0.0D, 0.08D, 0.0D);
                hit.resetFallDistance();
            }
            hit.hasImpulse = true;
        }
    }

    @Override
    public void onTouchStrikeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, List<LivingEntity> hitEntities, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Spatial Destabilization Strike: victim teleports randomly for the next 10 seconds (200t)
        long until = level.getGameTime() + 200L;
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_VOID_DESTABILIZED_UNTIL, until);
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.VOID_TEAR.get()), 60, 0));
        }
    }

    @Override
    public void onRadialBurstFormCast(ServerLevel level, LivingEntity caster, Vec3 center, double radius, Vec3 direction, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Singularity Implosion: 360-degree inward gravitational collapse!
        AABB box = new AABB(center.x - radius, center.y - 2.5D, center.z - radius, center.x + radius, center.y + 3.0D, center.z + radius);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (e == caster || !e.isAlive()) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 inward = center.subtract(e.position()).normalize().scale(1.35D);
            e.setDeltaMovement(inward.x, 0.35D, inward.z);
            e.hasImpulse = true;
        }
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 0.8D, center.z, 40, radius * 0.4D, 0.5D, radius * 0.4D, 0.15D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.65F);
    }

    @Override
    public void onAuraFormCast(ServerLevel level, LivingEntity caster, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Zero-G Levitation Aura: weightlessness, high-float jumps, slow-falling glide
        int dur = 200 + spell.getEffectiveTier() * 40;
        caster.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.WEIGHTLESSNESS.get()), dur, 0, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, dur, 2, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, dur, 0, false, true));
        level.sendParticles(ParticleTypes.PORTAL, caster.getX(), caster.getY(0.8D), caster.getZ(), 24, 0.4D, 0.6D, 0.4D, 0.15D);
    }

    @Override
    public boolean onGlyphFormCast(ServerLevel level, LivingEntity caster, Vec3 glyphCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Null-Zone Silencing Mine: silences spellcasting & mob abilities in a 6-block radius for 5 seconds (100t)
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 3.0D, 400, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed < 6) return true;
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB trig = new AABB(zone.center.x - 2.2D, zone.center.y - 1.0D, zone.center.z - 2.2D,
                            zone.center.x + 2.2D, zone.center.y + 2.0D, zone.center.z + 2.2D);
                    if (srv.getEntitiesOfClass(LivingEntity.class, trig, e -> e.isAlive() && e != owner).isEmpty()) return true;

                    long silenceUntil = srv.getGameTime() + 100L;
                    for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, new AABB(BlockPos.containing(zone.center)).inflate(6.0D))) {
                        if (v == owner || !v.isAlive()) continue;
                        EntityHelper.getPersistentData(v).putLong("SpellSilencedUntil", silenceUntil);
                        if (v instanceof Mob mob) mob.setAggressive(false);
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, Vec3.ZERO, zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                    }
                    srv.sendParticles(ParticleTypes.REVERSE_PORTAL, zone.center.x, zone.center.y + 0.8D, zone.center.z, 35, 1.2D, 0.6D, 1.2D, 0.12D);
                    return false;
                }
        ));
        return true;
    }

    @Override
    public void onConeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double coneRange, List<LivingEntity> hitTargets, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Null-Matter Dispel Spray: strips positive potion buffs from struck enemies!
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            List<MobEffectInstance> copy = new ArrayList<>(hit.getActiveEffects());
            for (MobEffectInstance inst : copy) {
                if (inst.getEffect().value().isBeneficial()) {
                    hit.removeEffect(inst.getEffect());
                }
            }
        }
        level.sendParticles(ParticleTypes.PORTAL, caster.getX() + direction.x * 2.5D, caster.getEyeY() + direction.y * 2.5D, caster.getZ() + direction.z * 2.5D, 28, 0.7D, 0.4D, 0.7D, 0.2D);
    }

    @Override
    public boolean onSkyfallFormCast(ServerLevel level, LivingEntity caster, Vec3 impactCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Singularity Vortex Rift: 6s (120t) sky rift pulling mobs upward toward the vortex before dropping them for fall damage!
        double r = Math.max(6.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 120, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    Vec3 skyEye = zone.center.add(0.0D, 6.5D, 0.0D);
                    if (elapsed % 4 == 0) {
                        srv.sendParticles(ParticleTypes.REVERSE_PORTAL, skyEye.x, skyEye.y, skyEye.z, 20, 1.2D, 0.5D, 1.2D, 0.15D);
                    }
                    if (elapsed % 10 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 8.0D, zone.center.z + zone.radius);
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            Vec3 toRift = skyEye.subtract(v.position()).normalize().scale(0.55D);
                            v.setDeltaMovement(toRift.x, Math.max(0.38D, toRift.y), toRift.z);
                            v.hasImpulse = true;
                            if (elapsed % 20 == 0) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, toRift, zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.35F);
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
        tag.putString("SpellEidolonMateria", EssenceType.VOID.name());
        tag.putInt("SpellEidolonTintRGB", VOID_STALKER_HEX_COLOR);
        collapseSingularityOrb(level, spawnPos, caster, spell);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Wormhole Transit: leaves a 5-second (100t) true two-way (departure <-> arrival) wormhole portal for caster & allies!
        long initNow = level.getGameTime();
        EntityHelper.getPersistentData(caster).putLong("SpellWormholeCooldownUntil", initNow + 25L);

        SpellAssembly dummy = SpellAssembly.createMagicMissileStarter(potencyTier);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, departurePos, 1.5D, 100, initNow, dummy, dummy.subSpells().get(0), EssenceType.VOID, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 5 == 0) {
                        srv.sendParticles(ParticleTypes.PORTAL, departurePos.x, departurePos.y + 1.0D, departurePos.z, 10, 0.3D, 0.6D, 0.3D, 0.1D);
                        srv.sendParticles(ParticleTypes.PORTAL, arrivalPos.x, arrivalPos.y + 1.0D, arrivalPos.z, 10, 0.3D, 0.6D, 0.3D, 0.1D);
                    }
                    long now = srv.getGameTime();
                    LivingEntity owner = zone.resolveCaster(srv);

                    AABB depBox = new AABB(departurePos.x - 1.35D, departurePos.y - 0.5D, departurePos.z - 1.35D,
                            departurePos.x + 1.35D, departurePos.y + 2.2D, departurePos.z + 1.35D);
                    for (LivingEntity traveler : srv.getEntitiesOfClass(LivingEntity.class, depBox)) {
                        if (!traveler.isAlive() || (owner != null && traveler != owner && !traveler.isAlliedTo(owner))) continue;
                        CompoundTag tData = EntityHelper.getPersistentData(traveler);
                        if (now < tData.getLong("SpellWormholeCooldownUntil").orElse(0L)) continue;
                        tData.putLong("SpellWormholeCooldownUntil", now + 25L);
                        traveler.teleportTo(arrivalPos.x, arrivalPos.y, arrivalPos.z);
                        traveler.resetFallDistance();
                        srv.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.75F, 1.2F);
                    }

                    AABB arrBox = new AABB(arrivalPos.x - 1.35D, arrivalPos.y - 0.5D, arrivalPos.z - 1.35D,
                            arrivalPos.x + 1.35D, arrivalPos.y + 2.2D, arrivalPos.z + 1.35D);
                    for (LivingEntity traveler : srv.getEntitiesOfClass(LivingEntity.class, arrBox)) {
                        if (!traveler.isAlive() || (owner != null && traveler != owner && !traveler.isAlliedTo(owner))) continue;
                        CompoundTag tData = EntityHelper.getPersistentData(traveler);
                        if (now < tData.getLong("SpellWormholeCooldownUntil").orElse(0L)) continue;
                        tData.putLong("SpellWormholeCooldownUntil", now + 25L);
                        traveler.teleportTo(departurePos.x, departurePos.y, departurePos.z);
                        traveler.resetFallDistance();
                        srv.playSound(null, BlockPos.containing(departurePos), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.75F, 1.2F);
                    }
                    return true;
                }
        ));
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Abyssal Disintegration: dissolves organic blocks into crying obsidian void slag
        if (state.is(BlockTags.LOGS) || state.is(BlockTags.DIRT) || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.PLANKS) || state.is(BlockTags.FLOWERS)) {
            level.setBlockAndUpdate(pos, Blocks.CRYING_OBSIDIAN.defaultBlockState());
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 10, 0.25D, 0.25D, 0.25D, 0.08D);
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        int count = stack.getCount();
        if (stack.is(ItemTags.LOGS) || stack.is(ItemTags.DIRT) || stack.is(ItemTags.LEAVES) || stack.is(ItemTags.PLANKS)) {
            itemEntity.setItem(new ItemStack(Items.CRYING_OBSIDIAN, count));
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, itemEntity.getX(), itemEntity.getY(0.3D), itemEntity.getZ(), 8, 0.2D, 0.2D, 0.2D, 0.06D);
            return true;
        }
        if (stack.is(Items.STONE) || stack.is(Items.COBBLESTONE)) {
            itemEntity.setItem(new ItemStack(Items.END_STONE, count));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Spacetime Anchoring Curse: rubber-band tether yanks the victim back to cursed coordinates every 12s (240t)!
        CompoundTag data = EntityHelper.getPersistentData(target);
        long now = level.getGameTime();
        if (!data.contains(SpellLingeringZoneManager.TAG_VOID_ANCHOR_X)) {
            data.putDouble(SpellLingeringZoneManager.TAG_VOID_ANCHOR_X, target.getX());
            data.putDouble(SpellLingeringZoneManager.TAG_VOID_ANCHOR_Y, target.getY());
            data.putDouble(SpellLingeringZoneManager.TAG_VOID_ANCHOR_Z, target.getZ());
            data.putLong("SpellVoidLastRubberbandTick", now);
        } else {
            double ax = data.getDouble(SpellLingeringZoneManager.TAG_VOID_ANCHOR_X).orElse(target.getX());
            double ay = data.getDouble(SpellLingeringZoneManager.TAG_VOID_ANCHOR_Y).orElse(target.getY());
            double az = data.getDouble(SpellLingeringZoneManager.TAG_VOID_ANCHOR_Z).orElse(target.getZ());
            long lastSnap = data.getLong("SpellVoidLastRubberbandTick").orElse(0L);
            if (now - lastSnap >= 240L && target.distanceToSqr(ax, ay, az) > 2.25D) {
                data.putLong("SpellVoidLastRubberbandTick", now);
                target.teleportTo(ax, ay, az);
                target.setDeltaMovement(Vec3.ZERO);
                level.playSound(null, target.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.85F, 0.7F);
            }
        }
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.hurtServer(level, level.damageSources().magic(), (1.35F + potencyTier * 0.55F) * mult);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getY(0.7D), target.getZ(), 6, 0.25D, 0.35D, 0.25D, 0.06D);
        return true;
    }

    @Override
    public void onEffectAppliedToEntity(ServerLevel level, @Nullable LivingEntity caster, LivingEntity target, Vec3 direction, SpellCoreEffect effect, int potencyTier, SpellAssembly spell) {
        if (effect == SpellCoreEffect.KINETIC || effect == SpellCoreEffect.GRAVITY) {
            // Gravity Well: stationary 3-second (60t) non-destructive black hole pulling in surrounding items AND mobs!
            Vec3 wellCenter = target.position();
            SpellAssembly dummy = SpellAssembly.createMagicMissileStarter(potencyTier);
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, wellCenter, 6.0D, 60, level.getGameTime(), dummy, dummy.subSpells().get(0), EssenceType.VOID, 1.0F,
                    (srv, zone, elapsed) -> {
                        if (elapsed > zone.maxDurationTicks) return false;
                        if (elapsed % 4 == 0) {
                            srv.sendParticles(ParticleTypes.REVERSE_PORTAL, zone.center.x, zone.center.y + 0.6D, zone.center.z,
                                    14, 0.35D, 0.35D, 0.35D, 0.08D);
                        }
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB pullBox = new AABB(zone.center.x - 6.0D, zone.center.y - 2.5D, zone.center.z - 6.0D,
                                zone.center.x + 6.0D, zone.center.y + 3.5D, zone.center.z + 6.0D);
                        for (ItemEntity item : srv.getEntitiesOfClass(ItemEntity.class, pullBox)) {
                            Vec3 pull = zone.center.subtract(item.position());
                            if (pull.lengthSqr() > 0.1D) {
                                item.setDeltaMovement(pull.normalize().scale(0.55D));
                                item.hasImpulse = true;
                            }
                        }
                        for (LivingEntity mob : srv.getEntitiesOfClass(LivingEntity.class, pullBox)) {
                            if (mob == owner || !mob.isAlive() || SpellCombatReactionEngine.isBossEntity(srv, mob)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(mob, owner, spell.hasWardFriendlyFireSafeguard())) continue;
                            Vec3 pull = zone.center.subtract(mob.position());
                            if (pull.lengthSqr() > 0.25D) {
                                Vec3 step = pull.normalize().scale(0.45D);
                                mob.setDeltaMovement(step.x, 0.12D, step.z);
                                mob.hasImpulse = true;
                            }
                        }
                        return true;
                    }
            ));
        }
    }
}
