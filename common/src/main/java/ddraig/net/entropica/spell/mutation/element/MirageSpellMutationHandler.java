package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.SpellEidolonEntity;
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
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ColorParticleOption;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Complete per-part Signature Mutation implementation for <b>Mirage Materia (Ethereal &amp; Paradox Fusion #36)</b>
 * (Spellcrafting System Guide Section 6.37 — Radiant + Arid [Leans: Arid]).
 */
public final class MirageSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int PHANTASM_MIRAGE_HEX_COLOR = 0xE0CE84;

    @Override
    public EssenceType materiaType() {
        return EssenceType.MIRAGE;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Mirror Mirage Spiral Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Shimmering Mirage Sand-Dart";
            case BEAM -> "Phantasm Mirage Tether Beam";
            case RADIAL_BURST -> "Shimmering Fata Morgana Shockwave";
            case TOUCH_STRIKE -> "Phantasmal Paranoia & Traitor Strike";
            case AURA -> "Spatial Displacement Glamour Shroud";
            case GLYPH -> "False Oasis Lure Glyph";
            case CONE -> "Hallucinatory Mirage Shimmer Deluge";
            case SKYFALL -> "Mirage Sand-Storm Tempest";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Fata Morgana Displacement Step";
            case TRANSMUTE, BREAK -> "Hallucinatory Glazing Transmute";
            case AFFLICTION -> "Delusional Phantasm Paranoia Hex";
            default -> "Fata Morgana " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations (Mirror Mirage Spiral Flow) ---

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
        level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.01D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileSpawnedFromForm(ServerLevel level, SpellProjectileEntity projectile, LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        // Shimmering Mirage Sand-Dart: spawns 2 decoy split missiles alongside the real dart to confuse enemies!
        if (!projectile.isSplitChild()) {
            float spd = (float) Math.max(1.0D, projectile.getDeltaMovement().length());
            projectile.spawnSplitChild(level, projectile.position(), -14.0F, false, 0.30F, spd);
            projectile.spawnSplitChild(level, projectile.position(), 14.0F, false, 0.30F, spd);
        }
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.01D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        // Blinds 3.0s (60t), scrambles mob aggro into traitor confusion, & scatters a 2-block loose shifting sand patch!
        if (hitEntity instanceof LivingEntity living && living.isAlive()
                && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard()))) {
            living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            SpellLingeringZoneManager.applyMirageTraitorFrenzy(living, 60);
            level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                    living.getX(), living.getY(0.8D), living.getZ(), 18, 0.35D, 0.4D, 0.35D, 0.04D);
        }
        if (!projectile.isSplitChild()) {
            scatterShiftingSandPatch(level, caster, hitEntity.position(), spell);
        }
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        if (!projectile.isSplitChild()) {
            scatterShiftingSandPatch(level, caster, blockHit.getLocation(), spell);
        }
    }

    private static void scatterShiftingSandPatch(ServerLevel level, @Nullable LivingEntity caster, Vec3 hitPos, SpellAssembly spell) {
        BlockPos center = BlockPos.containing(hitPos);
        Map<BlockPos, BlockState> saved = new HashMap<>();
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 0, 1))) {
            BlockState ground = level.getBlockState(p);
            if ((ground.is(BlockTags.DIRT) || ground.is(Blocks.STONE) || ground.is(Blocks.COBBLESTONE))
                    && level.getBlockState(p.above()).isAir()
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, ground)) {
                saved.put(p.immutable(), ground);
                level.setBlockAndUpdate(p, Blocks.SAND.defaultBlockState());
            }
        }
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, hitPos, 2.0D, 80, level.getGameTime(), spell, null, EssenceType.MIRAGE, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) {
                        for (Map.Entry<BlockPos, BlockState> e : saved.entrySet()) {
                            if (srv.getBlockState(e.getKey()).is(Blocks.SAND)) {
                                srv.setBlockAndUpdate(e.getKey(), e.getValue());
                            }
                        }
                        return false;
                    }
                    if (elapsed % 10 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB sandBox = new AABB(zone.center.x - 2.0D, zone.center.y - 1.0D, zone.center.z - 2.0D,
                                zone.center.x + 2.0D, zone.center.y + 1.8D, zone.center.z + 2.0D);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, sandBox, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 1));
                        }
                        srv.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                                zone.center.x, zone.center.y + 0.2D, zone.center.z, 8, 0.9D, 0.1D, 0.9D, 0.02D);
                    }
                    return true;
                }
        ));
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
        // Phantasm Mirage Tether Beam: redirects 50% of caster's incoming damage onto tethered victim while blinding them;
        // 3.0s continuous channeling triggers Sensory Overload Flash stunning 1.5s (30t) & forcing surrounding mobs to target them!
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));

            CompoundTag casterData = EntityHelper.getPersistentData(caster);
            casterData.putString(SpellLingeringZoneManager.TAG_MIRAGE_REDIRECT_UUID, hit.getUUID().toString());
            casterData.putLong(SpellLingeringZoneManager.TAG_MIRAGE_REDIRECT_UNTIL, now + 50L);

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("MirageBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("MirageBeamStacks").orElse(0) + 1 : 1;
            data.putLong("MirageBeamLastTick", now);
            data.putInt("MirageBeamStacks", stacks);

            if (stacks >= 3) {
                data.putInt("MirageBeamStacks", 0);
                hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
                for (Mob mob : level.getEntitiesOfClass(Mob.class, hit.getBoundingBox().inflate(10.0D))) {
                    if (mob != hit && mob.isAlive() && mob instanceof Monster) {
                        mob.setTarget(hit);
                        mob.setLastHurtByMob(hit);
                        mob.setAggressive(true);
                    }
                }
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0.96F, 0.72F, 0.86F), hit.getX(), hit.getY(1.0D), hit.getZ(), 1, 0, 0, 0, 0);
                level.playSound(null, hit.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.4F);
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
        // Shimmering Fata Morgana Shockwave: 6m eruption — wipes mob aggro, 4.0s (80t) Blindness + Nausea,
        // 6m knockback, and spawns 3 short-lived (5.0s / 100t) illusory clone decoys!
        double r = Math.max(6.0D, radius);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.5D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = foe.position().subtract(center).normalize().scale(1.45D);
            foe.setDeltaMovement(away.x, 0.36D, away.z);
            foe.hasImpulse = true;
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            foe.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 80, 0));
            foe.setLastHurtByMob(null);
            if (foe instanceof Mob mob) {
                mob.setTarget(null);
                mob.setAggressive(false);
            }
        }

        for (int i = 0; i < 3; i++) {
            double ang = (Math.PI * 2.0D * i) / 3.0D;
            Vec3 decoyPos = center.add(Math.cos(ang) * 2.5D, 0.0D, Math.sin(ang) * 2.5D);
            SpellEidolonEntity.spawnShortLivedDecoy(
                    level, caster, decoyPos, EssenceType.MIRAGE,
                    12.0F, 100, caster.getName().getString(), null
            );
        }
        level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                center.x, center.y + 0.9D, center.z, 36, 2.2D, 0.6D, 2.2D, 0.06D);
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 0.9D, center.z, 24, 2.0D, 0.5D, 2.0D, 0.06D);
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
        // Phantasmal Paranoia & Traitor Strike: optical hallucinations (3.0s frantic swings) & friend-foe inversion for 5.0s (100t),
        // treating the caster as invisible while dealing continuous mental Mirage damage!
        long paranoiaUntil = level.getGameTime() + 100L;
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            SpellLingeringZoneManager.applyMirageTraitorFrenzy(hit, 100);
            CompoundTag hData = EntityHelper.getPersistentData(hit);
            hData.putString(SpellLingeringZoneManager.TAG_MIRAGE_PARANOIA_CASTER_UUID, caster.getUUID().toString());
            hData.putLong(SpellLingeringZoneManager.TAG_MIRAGE_PARANOIA_UNTIL, paranoiaUntil);
            if (hit instanceof Mob hMob && hMob.getTarget() == caster) {
                hMob.setTarget(null);
                hMob.setAggressive(false);
            }
            level.sendParticles(ParticleTypes.ENCHANT, hit.getX(), hit.getY(1.0D), hit.getZ(), 18, 0.35D, 0.4D, 0.35D, 0.08D);
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
        // Spatial Displacement Glamour Shroud: 50% melee & projectile miss chance, water-crust stepping, flickering phantom lure!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_MIRAGE_SHROUD_UNTIL, level.getGameTime() + dur);
        level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                caster.getX(), caster.getY(0.8D), caster.getZ(), 20, 0.45D, 0.55D, 0.45D, 0.03D);
        level.sendParticles(ParticleTypes.END_ROD, caster.getX(), caster.getY(0.9D), caster.getZ(), 14, 0.4D, 0.5D, 0.4D, 0.04D);
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
        // False Oasis Lure Glyph: actively lures hostile mobs within 12m toward the shimmering oasis;
        // when stepped on, plunges caught mobs into a 3m suffocating quicksand snare for 3.5s (70t)!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 3.0D, 300, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.SPLASH, zone.center.x, zone.center.y + 0.15D, zone.center.z, 6, 0.8D, 0.1D, 0.8D, 0.03D);
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 0.25D, zone.center.z, 3, 0.6D, 0.1D, 0.6D, 0.01D);

                        AABB lureBox = new AABB(zone.center.x - 12.0D, zone.center.y - 3.0D, zone.center.z - 12.0D,
                                zone.center.x + 12.0D, zone.center.y + 4.0D, zone.center.z + 12.0D);
                        for (Mob mob : srv.getEntitiesOfClass(Mob.class, lureBox, m -> m.isAlive() && m instanceof Monster)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(mob, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            mob.getNavigation().moveTo(zone.center.x, zone.center.y, zone.center.z, 1.15D);
                        }
                    }
                    if (elapsed < 10) return true;

                    AABB triggerBox = new AABB(zone.center.x - 2.2D, zone.center.y - 1.0D, zone.center.z - 2.2D,
                            zone.center.x + 2.2D, zone.center.y + 2.0D, zone.center.z + 2.2D);
                    List<LivingEntity> triggers = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e -> e.isAlive() && e != owner
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard())));
                    if (triggers.isEmpty()) return true;

                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 3.0D, 70, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl > sZone.maxDurationTicks) return false;
                                LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                AABB quicksandBox = new AABB(sZone.center.x - 3.0D, sZone.center.y - 1.5D, sZone.center.z - 3.0D,
                                        sZone.center.x + 3.0D, sZone.center.y + 2.5D, sZone.center.z + 3.0D);
                                long now = sLvl.getGameTime();
                                for (LivingEntity v : sLvl.getEntitiesOfClass(LivingEntity.class, quicksandBox, e -> e.isAlive() && e != sOwner)) {
                                    if (sOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, sOwner, sZone.spell.hasWardFriendlyFireSafeguard())) continue;
                                    Vec3 sink = sZone.center.subtract(v.position()).scale(0.28D);
                                    v.setDeltaMovement(sink.x, Math.min(v.getDeltaMovement().y, -0.15D), sink.z);
                                    v.hasImpulse = true;
                                    v.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 25, 0));
                                    v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 5));
                                    v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
                                    EntityHelper.getPersistentData(v).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 25L);
                                    if (sEl % 20 == 0) {
                                        v.hurtServer(sLvl, sLvl.damageSources().inWall(), 1.5F + sZone.spell.getEffectiveTier() * 0.45F);
                                        SpellExecutionEngine.deliverSubSpellOnEntity(sLvl, sOwner, v, Vec3.ZERO, sZone.spell, sZone.slot, sZone.fallbackMateria, sZone.apparatusPotency * 0.45F);
                                    }
                                }
                                if (sEl % 6 == 0) {
                                    sLvl.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                                            sZone.center.x, sZone.center.y + 0.3D, sZone.center.z, 14, 1.1D, 0.2D, 1.1D, 0.03D);
                                }
                                return true;
                            }
                    ));
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.SAND_BREAK, SoundSource.PLAYERS, 1.0F, 0.85F);
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
        // Hallucinatory Mirage Shimmer Deluge: 4.0s (80t) Blindness + Nausea, & 3.0s (60t) frantic traitor swings!
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 80, 0));
            SpellLingeringZoneManager.applyMirageTraitorFrenzy(hit, 60);
        }
        Vec3 mid = caster.getEyePosition().add(direction.scale(coneRange * 0.5D));
        level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                mid.x, mid.y, mid.z, 26, 1.5D, 0.5D, 1.5D, 0.05D);
        level.sendParticles(ParticleTypes.END_ROD, mid.x, mid.y, mid.z, 18, 1.4D, 0.5D, 1.4D, 0.04D);
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
        // Mirage Sand-Storm Tempest: 10s (200t), 8m swirling golden sand clouds — mobs hallucinate phantom threats,
        // dodging erratically in panic while taking continuous Mirage sand damage + Blindness!
        double r = Math.max(8.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 200, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 5 == 0) {
                        srv.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                                zone.center.x, zone.center.y + 3.0D, zone.center.z, 24, zone.radius * 0.55D, 1.5D, zone.radius * 0.55D, 0.05D);
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 3.0D, zone.center.z, 10, zone.radius * 0.50D, 1.2D, zone.radius * 0.50D, 0.04D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 6.0D, zone.center.z + zone.radius);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
                            SpellLingeringZoneManager.applyMirageTraitorFrenzy(foe, 40);
                            double jx = (srv.random.nextDouble() - 0.5D) * 0.95D;
                            double jz = (srv.random.nextDouble() - 0.5D) * 0.95D;
                            foe.setDeltaMovement(jx, 0.22D, jz);
                            foe.hasImpulse = true;
                            SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, foe, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.35F);
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
        tag.putString("SpellEidolonMateria", EssenceType.MIRAGE.name());
        tag.putInt("SpellEidolonTintRGB", PHANTASM_MIRAGE_HEX_COLOR);
        level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 22, 0.45D, 0.55D, 0.45D, 0.04D);
        level.sendParticles(ParticleTypes.END_ROD, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 16, 0.4D, 0.5D, 0.4D, 0.05D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Fata Morgana Displacement Step: 14m instant step leaving an illusory clone runner moving along caster's
        // original trajectory for 3.0s (60t) drawing enemy arrows & aggro, while granting true caster Invisibility 2.5s (50t)!
        Vec3 look = caster.getLookAngle();
        Vec3 horizRun = new Vec3(look.x, 0.0D, look.z).lengthSqr() > 1.0E-4D
                ? new Vec3(look.x, 0.0D, look.z).normalize().scale(0.32D)
                : new Vec3(0.0D, 0.0D, 0.32D);
        SpellEidolonEntity.spawnShortLivedDecoy(
                level, caster, departurePos, EssenceType.MIRAGE,
                12.0F, 60, caster.getName().getString(), horizRun
        );
        caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 50, 0, false, false));
        level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                departurePos.x, departurePos.y + 0.8D, departurePos.z, 16, 0.35D, 0.45D, 0.35D, 0.03D);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Hallucinatory Glazing Transmute: stone/cobble/deepslate -> sandstone or red sand, lava -> glass,
        // terracotta -> sand, and dropped rotten flesh -> cooked jerky / leather!
        boolean didAny = false;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.5D))) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) {
                didAny = true;
            }
        }
        if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE)) {
            level.setBlockAndUpdate(pos, Blocks.SANDSTONE.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.DEEPSLATE) || state.is(Blocks.COBBLED_DEEPSLATE)) {
            level.setBlockAndUpdate(pos, Blocks.RED_SAND.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.LAVA)) {
            level.setBlockAndUpdate(pos, Blocks.GLASS.defaultBlockState());
            return true;
        }
        if (state.is(BlockTags.TERRACOTTA)) {
            level.setBlockAndUpdate(pos, Blocks.SAND.defaultBlockState());
            return true;
        }
        return didAny;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        if (stack.is(Items.ROTTEN_FLESH)) {
            itemEntity.setItem(new ItemStack(level.random.nextBoolean() ? Items.COOKED_BEEF : Items.LEATHER, stack.getCount()));
            return true;
        }
        if (stack.is(Items.STONE) || stack.is(Items.COBBLESTONE)) {
            itemEntity.setItem(new ItemStack(Items.SANDSTONE, stack.getCount()));
            return true;
        }
        if (stack.is(Items.DEEPSLATE) || stack.is(Items.COBBLED_DEEPSLATE)) {
            itemEntity.setItem(new ItemStack(Items.RED_SAND, stack.getCount()));
            return true;
        }
        if (stack.is(ItemTags.TERRACOTTA)) {
            itemEntity.setItem(new ItemStack(Items.SAND, stack.getCount()));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Delusional Phantasm Paranoia Hex: 50% miss chance & caster untargetability (SpellCombatReactionEngine),
        // erratic swerves, Nausea, -40% armor toughness, & Paranoid Guard-Fumble on Players!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.80D, 0.40D * mult), 45L);
        target.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 60, 0, true, true));
        if (target.onGround() && level.random.nextFloat() < 0.45F) {
            double swerveX = (level.random.nextDouble() - 0.5D) * 0.55D;
            double swerveZ = (level.random.nextDouble() - 0.5D) * 0.55D;
            target.setDeltaMovement(target.getDeltaMovement().add(swerveX, 0.08D, swerveZ));
            target.hasImpulse = true;
        }
        target.hurtServer(level, level.damageSources().magic(), (1.35F + potencyTier * 0.45F) * mult);
        level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                target.getX(), target.getY(0.85D), target.getZ(), 4, 0.25D, 0.3D, 0.25D, 0.02D);
        return true;
    }
}
