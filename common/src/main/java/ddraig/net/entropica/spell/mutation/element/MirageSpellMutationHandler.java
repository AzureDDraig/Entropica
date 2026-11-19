package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
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
import ddraig.net.entropica.spell.fuel.MateriaStageTable;
import ddraig.net.entropica.spell.fuel.SpellFuelService;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.spell.vfx.SpellWorldVfxManager;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
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
 * (Spellcrafting System Guide Section 6.37 — Dual Radiant {@code #FFEA78} + Arid {@code #C2B28F} [Leans: Arid]).
 */
public final class MirageSpellMutationHandler implements MateriaPartMutationHandler {

    /** Canonical Sunlit Mirage Gold (#FFEA78) primary tint. */
    public static final int PHANTASM_MIRAGE_HEX_COLOR = 0xFFEA78;

    /** Canonical Bleached Desert Sandstone (#C2B28F) secondary tint. */
    public static final int MIRAGE_SANDSTONE_HEX_COLOR = 0xC2B28F;

    private static final String TAG_MIRAGE_BEAM_LAST_TICK = "SpellMirageBeamLastTick";
    private static final String TAG_MIRAGE_BEAM_TICKS = "SpellMirageBeamTicks";

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
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
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

    @Override
    public String resolveFlowDescription(SpellFlowModifier flow) {
        return "Spiraling rings of refractive golden glass mirrors (#FFEA78 / #C2B28F) deflecting 25% of incoming hostile projectiles back at attackers, providing Level-12 illumination, and blinding adjacent foes when struck.";
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Hurls an illusion-wrapped javelin of golden sand and heat haze (#FFEA78 / #C2B28F) that splits into 2 decoy missiles; true impact blinds for 3.0s, scrambles mob aggro for 3.0s, deals Mirage Materia damage, and scatters a 2-block shifting sand patch.";
            case BEAM -> "Channels a shimmering golden tether (#FFEA78 / #C2B28F) redirecting 50% of caster damage onto the blinded victim; 3.0s channeling triggers a Sensory Overload Flash stunning for 1.5s and turning surrounding mobs onto them.";
            case RADIAL_BURST -> "Detonates a 6-block 360-degree eruption of heat distortion and golden sand (#FFEA78 / #C2B28F), wiping mob aggro, inflicting 4s Blindness and Nausea, knocking foes 6 blocks back, and spawning 3 short-lived (5s) illusory clone decoys.";
            case TOUCH_STRIKE -> "Strikes the victim's senses with heat hazes (#FFEA78 / #C2B28F), causing 3.0s frantic air swings and 5.0s friend-foe traitor inversion while treating the caster as invisible and dealing continuous mental Mirage Materia damage.";
            case AURA -> "Projects a 3-block spatial displacement glamour (#FFEA78 / #C2B28F) causing 50% of incoming melee swings and arrows to strike empty air while generating flickering phantom doubles that wander nearby to draw mob aggro.";
            case GLYPH -> "Inscribes a disarmable false oasis rune (#FFEA78 / #C2B28F) that lures mobs within 12 blocks; stepping on it shatters the illusion into a 3-block suffocating quicksand pit for 3.5s with continuous suffocation and Mirage Materia damage.";
            case CONE -> "Sprays a 60-degree cone of shimmering heat waves and golden sand (#FFEA78 / #C2B28F), inflicting 4s Blindness and Nausea, forcing foes to swing at thin air or each other for 3.0s, and dealing Mirage Materia damage.";
            case SKYFALL -> "Summons a 10-second storm of swirling golden sand clouds and refractive light across 8+ blocks (#FFEA78 / #C2B28F); mobs dodge phantom projectiles in panic while suffering suffocating sand damage and Blindness.";
            case SUMMON_AGENT -> "Summons a Mirage-Duelist biped (#FFEA78 / #C2B28F) that leaves 2 phantom afterimages per step and inflicts Paranoia on hit.";
            case SUMMON_SENTRY -> "Anchors a Fata Morgana Obelisk (#FFEA78 / #C2B28F) projecting false illusions across 12 blocks to draw all mob attacks away.";
            case SUMMON_DECOY -> "Manifests a Phantom Mirror Effigy (#FFEA78 / #C2B28F) identical to the caster that explodes into a blinding traitor-frenzy flash when destroyed.";
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Teleports up to 14 blocks instantly, leaving a lifelike illusory clone running along the caster's original trajectory for 3.0s to draw arrows and aggro while turning the true caster invisible for 2.5s.";
            case TRANSMUTE, BREAK -> "Warps physical reality to match the desert illusion: turns stone and cobblestone into sandstone, deepslate into red sand, lava into vitrified smooth glass, colored terracotta into sand, and rotten flesh into dried jerky.";
            case AFFLICTION -> "Persistent milk-immune paranoia curse causing 50% attack misses, erratic phantom-dodging swerves, -40% armor toughness, caster untargetability, and Paranoid Guard-Fumble (+35% mob crit & 40% shield fumble) until cleansed by Vitae/Restoration or Umbral magic.";
            default -> null;
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATION — MIRROR MIRAGE SPIRAL FLOW
    // =========================================================================

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SUNBEAM.get(), PHANTASM_MIRAGE_HEX_COLOR, 14, 0.72F, 0.04F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D
        );
        if ((projectile.tickCount & 1) == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_VORTEX.get(), MIRAGE_SANDSTONE_HEX_COLOR, 16, 0.68F, 0.05F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D
            );
        }
        if (flow == SpellFlowModifier.ORBIT_SATELLITE && projectile.getOwner() instanceof LivingEntity owner && owner.isAlive()) {
            long now = level.getGameTime();
            CompoundTag data = EntityHelper.getPersistentData(owner);
            if (data.getLong(SpellLingeringZoneManager.TAG_MIRAGE_ORBIT_UNTIL).orElse(0L) < now + 30L) {
                data.putLong(SpellLingeringZoneManager.TAG_MIRAGE_ORBIT_UNTIL, now + 35L);
            }
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
        // Section 6.37 Shimmering Mirage Sand-Dart:
        // Splits in mid-air into 2 decoy missiles that mimic the projectile to confuse enemies!
        if (!projectile.isSplitChild()) {
            float spd = (float) Math.max(1.05D, projectile.getDeltaMovement().length());
            projectile.spawnSplitChild(level, projectile.position(), -14.0F, false, 0.30F, spd);
            projectile.spawnSplitChild(level, projectile.position(), 14.0F, false, 0.30F, spd);
        }
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.01D
        );
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
        // Section 6.37 Shimmering Mirage Sand-Dart:
        // On true impact, blinds the target for 3.0s (60t), scrambles mob aggro causing the target to attack nearby
        // allies or swing at thin air for 3.0s (60t), deals Mirage Materia damage (+1 vs Water, +1 vs Void, +1 vs Undead),
        // and scatters a 2-block patch of loose shifting sand!
        if (hitEntity instanceof LivingEntity living && living.isAlive()) {
            boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
            if (caster == null || (!SpellExecutionEngine.isFriendlyAllyForOasis(caster, living)
                    && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, wardSafe))) {
                int tier = spell.getEffectiveTier();
                if (!projectile.isSplitChild()) {
                    living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                    SpellLingeringZoneManager.applyMirageTraitorFrenzy(living, 60);
                    CompoundTag hData = EntityHelper.getPersistentData(living);
                    if (caster != null) {
                        hData.putString(SpellLingeringZoneManager.TAG_MIRAGE_PARANOIA_CASTER_UUID, caster.getUUID().toString());
                    }
                    hData.putLong(SpellLingeringZoneManager.TAG_MIRAGE_PARANOIA_UNTIL, level.getGameTime() + 60L);

                    if (!slot.hasHarmVerb()) {
                        float dartDmg = 2.65F + tier * 0.68F;
                        SpellCombatReactionEngine.hurtWithMirageMateriaAdvantage(level, living, caster, dartDmg, false, true);
                    }
                } else {
                    living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 35, 0));
                    if (!slot.hasHarmVerb()) {
                        float decoySting = 0.85F + tier * 0.22F;
                        SpellCombatReactionEngine.hurtWithMirageMateriaAdvantage(level, living, caster, decoySting, false, true);
                    }
                }

                SpellWorldVfxManager.broadcastProjectileImpact(
                        level, living.position().add(0.0D, living.getBbHeight() * 0.55D, 0.0D),
                        new Vec3(0.0D, 1.0D, 0.0D), EssenceType.MIRAGE, EssenceType.ARID, 1.25F, tier
                );
                level.sendParticles(
                        new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                        living.getX(), living.getY(0.8D), living.getZ(), 18, 0.35D, 0.4D, 0.35D, 0.04D
                );
            }
        }
        if (!projectile.isSplitChild()) {
            scatterShiftingSandPatch(level, caster, hitEntity.position(), spell);
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
        if (!projectile.isSplitChild()) {
            SpellWorldVfxManager.broadcastProjectileImpact(
                    level, blockHit.getLocation(), Vec3.atLowerCornerOf(blockHit.getDirection().getUnitVec3i()),
                    EssenceType.MIRAGE, EssenceType.ARID, 1.15F, spell.getEffectiveTier()
            );
            scatterShiftingSandPatch(level, caster, blockHit.getLocation(), spell);
        }
    }

    private static void scatterShiftingSandPatch(
            ServerLevel level,
            @Nullable LivingEntity caster,
            Vec3 hitPos,
            SpellAssembly spell
    ) {
        BlockPos center = BlockPos.containing(hitPos);
        Map<BlockPos, BlockState> saved = new HashMap<>();
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 0, 1))) {
            if (Math.abs(p.getX() - center.getX()) + Math.abs(p.getZ() - center.getZ()) > 2) continue;
            BlockState ground = level.getBlockState(p);
            if ((ground.is(BlockTags.DIRT) || ground.is(Blocks.STONE) || ground.is(Blocks.COBBLESTONE) || ground.is(Blocks.SANDSTONE))
                    && level.getBlockState(p.above()).isAir()
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, ground)) {
                saved.put(p.immutable(), ground);
                level.setBlock(p, Blocks.SAND.defaultBlockState(), Block.UPDATE_CLIENTS);
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
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        AABB sandBox = new AABB(
                                zone.center.x - 2.0D, zone.center.y - 1.0D, zone.center.z - 2.0D,
                                zone.center.x + 2.0D, zone.center.y + 1.8D, zone.center.z + 2.0D
                        );
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, sandBox, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) continue;
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 1));
                        }
                        srv.sendParticles(
                                new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                                zone.center.x, zone.center.y + 0.2D, zone.center.z, 8, 0.9D, 0.1D, 0.9D, 0.02D
                        );
                    }
                    return true;
                }
        ));
    }

    // =========================================================================
    // FORM MUTATION 2 — PHANTASM MIRAGE TETHER BEAM (BEAM)
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
        // Section 6.37 Phantasm Mirage Tether Beam:
        // Continuously redirects 50% of all damage taken by the caster onto the tethered victim while blinding them;
        // sustained channeling for 3.0 seconds (60 ticks) triggers a Sensory Overload Flash that stuns the target
        // for 1.5s (30t) and causes surrounding mobs to attack them as a decoy!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        long now = level.getGameTime();

        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));

            CompoundTag casterData = EntityHelper.getPersistentData(caster);
            casterData.putString(SpellLingeringZoneManager.TAG_MIRAGE_REDIRECT_UUID, hit.getUUID().toString());
            casterData.putLong(SpellLingeringZoneManager.TAG_MIRAGE_REDIRECT_UNTIL, now + 50L);

            if (!slot.hasHarmVerb()) {
                float tetherDmg = (1.28F + tier * 0.35F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithMirageMateriaAdvantage(level, hit, caster, tetherDmg, false, true);
            }

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong(TAG_MIRAGE_BEAM_LAST_TICK).orElse(-100L);
            int contactTicks = (now - last <= 12L) ? data.getInt(TAG_MIRAGE_BEAM_TICKS).orElse(0) + 4 : 4;
            data.putLong(TAG_MIRAGE_BEAM_LAST_TICK, now);
            data.putInt(TAG_MIRAGE_BEAM_TICKS, contactTicks);

            if (contactTicks >= 60) {
                data.putInt(TAG_MIRAGE_BEAM_TICKS, 0);
                hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
                hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 6));
                hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 70, 0));

                float flashDmg = (4.20F + tier * 1.15F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithMirageMateriaAdvantage(level, hit, caster, flashDmg, false, true);

                for (Mob mob : level.getEntitiesOfClass(Mob.class, hit.getBoundingBox().inflate(10.0D))) {
                    if (mob != hit && mob.isAlive() && mob instanceof Monster
                            && !SpellExecutionEngine.isFriendlyAllyForOasis(caster, mob)) {
                        mob.setTarget(hit);
                        mob.setLastHurtByMob(hit);
                        mob.setAggressive(true);
                    }
                }

                SpellWorldVfxManager.broadcastRadialNova(
                        level, hit.position().add(0.0D, 0.2D, 0.0D), 3.2D, EssenceType.MIRAGE, EssenceType.ARID, tier
                );
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.00F, 0.92F, 0.47F),
                        hit.getX(), hit.getY(1.0D), hit.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
                level.playSound(null, hit.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.05F, 1.42F);
            }
        }
    }

    // =========================================================================
    // FORM MUTATION 3 — SHIMMERING FATA MORGANA SHOCKWAVE (RADIAL BURST)
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
        // Section 6.37 Shimmering Fata Morgana Shockwave:
        // 360° blinding eruption of heat distortion & golden sand (6m+ radius, #FFEA78 / #C2B28F);
        // wipes mob aggro completely, inflicts Blindness & Nausea for 4.0s (80t), knocks enemies 6 blocks backward,
        // and spawns 3 short-lived (5.0s / 100t) illusory clone decoys that distract hostiles!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.0D, radius);
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.MIRAGE, EssenceType.ARID, tier);

        float waveDmg = (2.95F + tier * 0.75F) * Math.max(0.5F, apparatusPotency);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.5D, center.z + r);

        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (foe == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafe)) continue;

            Vec3 delta = foe.position().subtract(center);
            Vec3 away = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.48D) : new Vec3(0.0D, 0.36D, 0.0D);
            foe.setDeltaMovement(away.x, 0.36D, away.z);
            foe.hasImpulse = true;
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            foe.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 80, 0));
            wipeMobAggroCompletely(foe);

            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithMirageMateriaAdvantage(level, foe, caster, waveDmg, false, true);
            }
        }

        for (int i = 0; i < 3; i++) {
            double ang = (Math.PI * 2.0D * i) / 3.0D;
            Vec3 decoyPos = center.add(Math.cos(ang) * 2.5D, 0.0D, Math.sin(ang) * 2.5D);
            SpellEidolonEntity.spawnShortLivedDecoy(
                    level, caster, decoyPos, EssenceType.MIRAGE,
                    12.0F + tier * 2.0F, 100, caster.getName().getString(), null
            );
        }

        level.sendParticles(
                new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                center.x, center.y + 0.9D, center.z, 36, 2.2D, 0.6D, 2.2D, 0.06D
        );
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 0.9D, center.z, 24, 2.0D, 0.5D, 2.0D, 0.06D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.SAND_BREAK, SoundSource.PLAYERS, 1.05F, 1.20F);
    }

    // =========================================================================
    // FORM MUTATION 4 — PHANTASMAL PARANOIA & TRAITOR STRIKE (TOUCH / STRIKE)
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
        // Section 6.37 Phantasmal Paranoia & Traitor Strike:
        // Infects the target's mind with terrifying optical hallucinations: 3.0s (60t) frantic swings at empty air,
        // and 5.0s (100t) friend-foe inversion (forcing the mob to perceive its own allies as hostile monsters while
        // treating the caster as invisible, dealing continuous mental Mirage Materia damage)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        long paranoiaUntil = level.getGameTime() + 100L;

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D), direction, 2.8D,
                EssenceType.MIRAGE, EssenceType.ARID, tier
        );

        float strikeDmg = (2.80F + tier * 0.72F) * Math.max(0.5F, apparatusPotency);

        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

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
            hit.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            hit.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
            hit.swing(InteractionHand.MAIN_HAND);

            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithMirageMateriaAdvantage(level, hit, caster, strikeDmg, true, true);
            }

            level.sendParticles(ParticleTypes.ENCHANT, hit.getX(), hit.getY(1.0D), hit.getZ(), 18, 0.35D, 0.4D, 0.35D, 0.08D);
            level.sendParticles(
                    new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                    hit.getX(), hit.getY(0.85D), hit.getZ(), 14, 0.35D, 0.4D, 0.35D, 0.04D
            );
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 0.90F, 1.25F);
    }

    // =========================================================================
    // FORM MUTATION 5 — SPATIAL DISPLACEMENT GLAMOUR SHROUD (AURA / SELF)
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
        // Section 6.37 Spatial Displacement Glamour Shroud:
        // Enemies perceive the caster's body 3 blocks away from their actual physical position, causing 50% of all
        // incoming melee swings and arrows to strike empty air, while generating flickering phantom doubles that
        // wander nearby to draw mob aggro!
        int tier = spell.getEffectiveTier();
        int dur = 200 + tier * 40;
        EntityHelper.getPersistentData(caster).putLong(
                SpellLingeringZoneManager.TAG_MIRAGE_SHROUD_UNTIL, level.getGameTime() + dur
        );
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.50F, null);

        // Immediately project an initial 3-block displaced wandering phantom double to draw nearby aggro!
        double ang = level.random.nextDouble() * Math.PI * 2.0D;
        Vec3 phantomPos = caster.position().add(Math.cos(ang) * 3.0D, 0.0D, Math.sin(ang) * 3.0D);
        Vec3 wanderVel = new Vec3(-Math.sin(ang) * 0.16D, 0.0D, Math.cos(ang) * 0.16D);
        SpellEidolonEntity.spawnShortLivedDecoy(
                level, caster, phantomPos, EssenceType.MIRAGE,
                10.0F, 70, caster.getName().getString(), wanderVel
        );

        level.sendParticles(
                new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                caster.getX(), caster.getY(0.8D), caster.getZ(), 20, 0.45D, 0.55D, 0.45D, 0.03D
        );
        level.sendParticles(ParticleTypes.END_ROD, caster.getX(), caster.getY(0.9D), caster.getZ(), 14, 0.4D, 0.5D, 0.4D, 0.04D);
        level.playSound(null, caster.blockPosition(), SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 0.85F, 1.35F);
    }

    // =========================================================================
    // FORM MUTATION 6 — FALSE OASIS LURE GLYPH (GLYPH / TRAP)
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
        // Section 6.37 False Oasis Lure Glyph:
        // Disarmable Single/Repeating shimmering rune (#FFEA78 / #C2B28F, 1.0s arming delay) casting a vivid sensory
        // illusion of a sparkling water pool & lush greenery; actively attracts and lures all hostile mobs within
        // 12 blocks toward it; stepping on it shatters the illusion, plunging caught mobs into a 3-block pit of
        // suffocating quicksand for 3.5s (70t) with continuous suffocation & Mirage Materia damage!
        int tier = spell.getEffectiveTier();
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snapped = new Vec3(surface.getX() + 0.5D, surface.above().getY(), surface.getZ() + 0.5D);
        double trapRad = Math.max(3.0D, radius);

        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxTriggers = repeating ? 3 : 1;
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE / 4 : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snapped.add(0.0D, 0.03D, 0.0D), trapRad, EssenceType.MIRAGE, EssenceType.ARID, Math.min(240, maxDuration), tier
        );

        final int[] triggersLeft = {maxTriggers};
        final int[] rearmCooldown = {0};

        SpellLingeringZoneManager.ActiveSpellZone glyphZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snapped, trapRad, maxDuration, level.getGameTime(), spell, slot, EssenceType.MIRAGE, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();

                    if (elapsed > 0 && elapsed % 200 == 0) {
                        int rem = zone.maxDurationTicks >= 1_000_000_000 ? 240 : (int) Math.min(240L, zone.maxDurationTicks - elapsed);
                        SpellWorldVfxManager.broadcastArcaneSeal(
                                srv, zone.center.add(0.0D, 0.03D, 0.0D), zone.radius, EssenceType.MIRAGE, EssenceType.ARID, rem, tier
                        );
                    }

                    if (elapsed % 10 == 0) {
                        // Sparkling false-oasis pool splashes & lush greenery glints!
                        srv.sendParticles(ParticleTypes.SPLASH, zone.center.x, zone.center.y + 0.14D, zone.center.z, 6, 0.85D, 0.08D, 0.85D, 0.03D);
                        srv.sendParticles(ParticleTypes.HAPPY_VILLAGER, zone.center.x, zone.center.y + 0.22D, zone.center.z, 2, 0.75D, 0.12D, 0.75D, 0.01D);
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 0.25D, zone.center.z, 2, 0.65D, 0.10D, 0.65D, 0.01D);

                        AABB lureBox = new AABB(
                                zone.center.x - 12.0D, zone.center.y - 3.0D, zone.center.z - 12.0D,
                                zone.center.x + 12.0D, zone.center.y + 4.0D, zone.center.z + 12.0D
                        );
                        for (Mob mob : srv.getEntitiesOfClass(Mob.class, lureBox, m -> m.isAlive() && m instanceof Monster)) {
                            if (owner != null && SpellExecutionEngine.isFriendlyAllyForOasis(owner, mob)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(mob, owner, wardSafe)) continue;
                            mob.getNavigation().moveTo(zone.center.x, zone.center.y, zone.center.z, 1.18D);
                        }
                    }

                    if (elapsed < 20) return true; // 1.0s arming delay
                    if (rearmCooldown[0] > 0) {
                        rearmCooldown[0]--;
                        return true;
                    }

                    AABB triggerBox = new AABB(
                            zone.center.x - 2.3D, zone.center.y - 1.0D, zone.center.z - 2.3D,
                            zone.center.x + 2.3D, zone.center.y + 2.0D, zone.center.z + 2.3D
                    );
                    List<LivingEntity> triggers = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e ->
                            e.isAlive() && e != owner
                                    && !SpellExecutionEngine.isFriendlyAllyForOasis(owner, e)
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe))
                    );
                    if (triggers.isEmpty()) return true;

                    SpellWorldVfxManager.broadcastRadialNova(
                            srv, zone.center, 3.0D, EssenceType.MIRAGE, EssenceType.ARID, tier
                    );

                    // Temporarily turn surface blocks in the 3m basin into shifting quicksand (restored cleanly on expiry!)
                    BlockPos pitCenter = BlockPos.containing(zone.center).below();
                    Map<BlockPos, BlockState> savedPit = new HashMap<>();
                    for (BlockPos p : BlockPos.betweenClosed(pitCenter.offset(-2, 0, -2), pitCenter.offset(2, 0, 2))) {
                        if (p.distSqr(pitCenter) > 6.5D) continue;
                        BlockState cur = srv.getBlockState(p);
                        if (!cur.isAir() && !cur.is(Blocks.SAND)
                                && (cur.is(BlockTags.DIRT) || cur.is(Blocks.STONE) || cur.is(Blocks.COBBLESTONE) || cur.is(Blocks.SANDSTONE))
                                && !SpellExecutionEngine.isProtectedBlockOrContainer(srv, p, cur)) {
                            savedPit.put(p.immutable(), cur);
                            srv.setBlock(p, Blocks.SAND.defaultBlockState(), Block.UPDATE_CLIENTS);
                        }
                    }

                    // 3.5s (70t) 3m suffocating quicksand pit!
                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 3.0D, 70, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl > sZone.maxDurationTicks) {
                                    for (Map.Entry<BlockPos, BlockState> e : savedPit.entrySet()) {
                                        if (sLvl.getBlockState(e.getKey()).is(Blocks.SAND)) {
                                            sLvl.setBlockAndUpdate(e.getKey(), e.getValue());
                                        }
                                    }
                                    return false;
                                }
                                LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                boolean sWard = sZone.spell != null && sZone.spell.hasWardFriendlyFireSafeguard();
                                int zTier = sZone.spell != null ? sZone.spell.getEffectiveTier() : tier;
                                AABB quicksandBox = new AABB(
                                        sZone.center.x - 3.0D, sZone.center.y - 1.5D, sZone.center.z - 3.0D,
                                        sZone.center.x + 3.0D, sZone.center.y + 2.5D, sZone.center.z + 3.0D
                                );
                                long now = sLvl.getGameTime();
                                for (LivingEntity v : sLvl.getEntitiesOfClass(LivingEntity.class, quicksandBox, e -> e.isAlive() && e != sOwner)) {
                                    if (sOwner != null && SpellExecutionEngine.isFriendlyAllyForOasis(sOwner, v)) continue;
                                    if (sOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, sOwner, sWard)) continue;

                                    Vec3 sink = sZone.center.subtract(v.position()).scale(0.28D);
                                    v.setDeltaMovement(sink.x, Math.min(v.getDeltaMovement().y, -0.16D), sink.z);
                                    v.hasImpulse = true;
                                    v.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 25, 0));
                                    v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 5));
                                    v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
                                    EntityHelper.getPersistentData(v).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 25L);

                                    if (sEl % 20 == 0) {
                                        v.hurtServer(sLvl, sLvl.damageSources().inWall(), 1.20F + zTier * 0.35F);
                                        float mirageTick = (1.35F + zTier * 0.40F) * Math.max(0.5F, sZone.apparatusPotency);
                                        SpellCombatReactionEngine.hurtWithMirageMateriaAdvantage(sLvl, v, sOwner, mirageTick, false, true);
                                        if (sZone.spell != null && sZone.slot != null) {
                                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                                    sLvl, sOwner, v, Vec3.ZERO, sZone.spell, sZone.slot, sZone.fallbackMateria, sZone.apparatusPotency * 0.45F
                                            );
                                        }
                                    }
                                }
                                if (sEl % 6 == 0) {
                                    sLvl.sendParticles(
                                            new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                                            sZone.center.x, sZone.center.y + 0.3D, sZone.center.z, 14, 1.1D, 0.2D, 1.1D, 0.03D
                                    );
                                }
                                return true;
                            }
                    ));

                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.SAND_BREAK, SoundSource.PLAYERS, 1.0F, 0.82F);
                    triggersLeft[0]--;
                    rearmCooldown[0] = 85;
                    return triggersLeft[0] > 0;
                }
        ).markDisarmableGlyph();
        SpellLingeringZoneManager.registerZone(level, glyphZone);
        return true;
    }

    // =========================================================================
    // FORM MUTATION 7 — HALLUCINATORY MIRAGE SHIMMER DELUGE (CONE / SPRAY)
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
        // Section 6.37 Hallucinatory Mirage Shimmer Deluge:
        // Wide 60° cone of shimmering heat waves & golden sand (#FFEA78 / #C2B28F); enemies suffer Blindness & Nausea
        // for 4.0s (80t) and frantically swing at thin air or each other for 3.0s (60t) while taking Mirage Materia damage!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        long paranoiaUntil = level.getGameTime() + 60L;

        SpellWorldVfxManager.broadcastVortexCone(
                level, caster.getEyePosition().subtract(0.0D, 0.2D, 0.0D), direction, coneRange,
                EssenceType.MIRAGE, EssenceType.ARID, tier
        );

        float coneDmg = (2.75F + tier * 0.70F) * Math.max(0.5F, apparatusPotency);

        for (LivingEntity hit : hitTargets) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 80, 0));
            SpellLingeringZoneManager.applyMirageTraitorFrenzy(hit, 60);

            CompoundTag hData = EntityHelper.getPersistentData(hit);
            hData.putString(SpellLingeringZoneManager.TAG_MIRAGE_PARANOIA_CASTER_UUID, caster.getUUID().toString());
            hData.putLong(SpellLingeringZoneManager.TAG_MIRAGE_PARANOIA_UNTIL, paranoiaUntil);
            hit.swing(InteractionHand.MAIN_HAND);

            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithMirageMateriaAdvantage(level, hit, caster, coneDmg, false, true);
            }
        }

        Vec3 mid = caster.getEyePosition().add(direction.scale(coneRange * 0.5D));
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                mid.x, mid.y, mid.z, 26, 1.5D, 0.5D, 1.5D, 0.05D
        );
        level.sendParticles(ParticleTypes.END_ROD, mid.x, mid.y, mid.z, 18, 1.4D, 0.5D, 1.4D, 0.04D);
        level.playSound(null, caster.blockPosition(), SoundEvents.SAND_PLACE, SoundSource.PLAYERS, 0.95F, 1.25F);
    }

    // =========================================================================
    // FORM MUTATION 8 — MIRAGE SAND-STORM TEMPEST (SKYFALL)
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
        // Section 6.37 Mirage Sand-Storm Tempest:
        // 10-second (200t) storm of swirling golden sand clouds & blinding refractive light across an 8-block+ radius
        // (#FFEA78 / #C2B28F); mobs inside hallucinate phantom projectiles, continuously dodging nothing and running
        // in circles in panic, while suffering continuous suffocating sand damage & Blindness!
        int tier = spell.getEffectiveTier();
        double r = Math.max(8.0D, radius);

        SpellWorldVfxManager.broadcastSkyfallPillar(
                level, impactCenter, r, EssenceType.MIRAGE, EssenceType.ARID, 200, tier
        );

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 200, level.getGameTime(), spell, slot, EssenceType.MIRAGE, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 5 == 0) {
                        srv.sendParticles(
                                new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                                zone.center.x, zone.center.y + 2.8D, zone.center.z, 24, zone.radius * 0.55D, 1.5D, zone.radius * 0.55D, 0.05D
                        );
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SUNBEAM.get(), PHANTASM_MIRAGE_HEX_COLOR, 14, 0.78F, 0.06F),
                                zone.center.x, zone.center.y + 3.0D, zone.center.z, 8, zone.radius * 0.48D, 1.2D, zone.radius * 0.48D, 0.03D
                        );
                    }
                    if (elapsed % 10 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;
                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 6.5D, zone.center.z + zone.radius
                        );
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) continue;

                            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
                            // Circular panic-running & phantom-projectile dodging!
                            double circleAng = (elapsed * 0.28D) + (foe.getId() * 1.7D);
                            double dodgeX = Math.cos(circleAng) * 0.48D + (srv.random.nextDouble() - 0.5D) * 0.36D;
                            double dodgeZ = Math.sin(circleAng) * 0.48D + (srv.random.nextDouble() - 0.5D) * 0.36D;
                            foe.setDeltaMovement(dodgeX, Math.max(foe.getDeltaMovement().y, 0.14D), dodgeZ);
                            foe.hasImpulse = true;
                            if (foe instanceof Mob mob) {
                                mob.getNavigation().moveTo(
                                        foe.getX() + Math.cos(circleAng) * 4.0D,
                                        foe.getY(),
                                        foe.getZ() + Math.sin(circleAng) * 4.0D,
                                        1.25D
                                );
                                mob.swing(InteractionHand.MAIN_HAND);
                            }

                            if (elapsed % 20 == 0) {
                                SpellLingeringZoneManager.applyMirageTraitorFrenzy(foe, 40);
                                foe.hurtServer(srv, srv.damageSources().inWall(), 0.90F + zTier * 0.25F);
                                float sandDmg = (1.55F + zTier * 0.42F) * Math.max(0.5F, zone.apparatusPotency);
                                SpellCombatReactionEngine.hurtWithMirageMateriaAdvantage(srv, foe, owner, sandDmg, false, true);
                                if (zone.spell != null && zone.slot != null) {
                                    SpellExecutionEngine.deliverSubSpellOnEntity(
                                            srv, owner, foe, new Vec3(0.0D, -1.0D, 0.0D),
                                            zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.40F
                                    );
                                }
                            }
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    // =========================================================================
    // FORM MUTATION 9 — PHANTASM-MIRAGE AUTOMATON SUITE (SUMMON / EIDOLON)
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
        tag.putString("SpellEidolonMateria", EssenceType.MIRAGE.name());
        tag.putInt("SpellEidolonTintRGB", PHANTASM_MIRAGE_HEX_COLOR);
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 22, 0.45D, 0.55D, 0.45D, 0.04D
        );
        level.sendParticles(ParticleTypes.END_ROD, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 16, 0.4D, 0.5D, 0.4D, 0.05D);
    }

    // =========================================================================
    // PART 3: CORE EFFECT MUTATIONS
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Section 6.37 Fata Morgana Displacement Step:
        // Teleports up to 14 blocks instantly, leaving behind a lifelike illusory clone of the caster that continues
        // running in the caster's original direction for 3.0s (60t) drawing all enemy arrows, spells, and mob aggro,
        // while the true caster becomes invisible for 2.5s (50t)!
        Vec3 look = caster.getLookAngle();
        Vec3 horizRun = new Vec3(look.x, 0.0D, look.z).lengthSqr() > 1.0E-4D
                ? new Vec3(look.x, 0.0D, look.z).normalize().scale(0.32D)
                : new Vec3(0.0D, 0.0D, 0.32D);

        SpellEidolonEntity runner = SpellEidolonEntity.spawnShortLivedDecoy(
                level, caster, departurePos, EssenceType.MIRAGE,
                12.0F + potencyTier * 2.0F, 60, caster.getName().getString(), horizRun
        );
        caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 50, 0, false, false));

        // Redirect mobs locked onto the caster onto the running illusory clone!
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(departurePos, departurePos).inflate(14.0D))) {
            if (mob.isAlive() && mob.getTarget() == caster) {
                mob.setTarget(runner);
            }
        }

        level.sendParticles(
                new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                departurePos.x, departurePos.y + 0.8D, departurePos.z, 16, 0.35D, 0.45D, 0.35D, 0.03D
        );
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Section 6.37 Hallucinatory Glazing Transmute:
        // Converts hard stone, cobblestone, and deepslate into fragile sandstone or loose red sand; transmutes lava
        // into vitrified smooth glass blocks; converts rotten flesh into dried jerky; and un-dyes colored terracotta
        // into natural sunlit sand!
        boolean didAny = false;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.5D))) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) {
                didAny = true;
            }
        }
        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return didAny;
        }

        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE)
                || state.is(Blocks.ANDESITE) || state.is(Blocks.DIORITE) || state.is(Blocks.GRANITE)) {
            level.setBlockAndUpdate(pos, Blocks.SANDSTONE.defaultBlockState());
            maybeGrantMirageBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.DEEPSLATE) || state.is(Blocks.COBBLED_DEEPSLATE) || state.is(Blocks.TUFF)) {
            level.setBlockAndUpdate(pos, Blocks.RED_SAND.defaultBlockState());
            maybeGrantMirageBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.LAVA)) {
            level.setBlock(pos, Blocks.GLASS.defaultBlockState(), Block.UPDATE_CLIENTS);
            maybeGrantMirageBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(BlockTags.TERRACOTTA) || path.endsWith("_glazed_terracotta")) {
            level.setBlockAndUpdate(pos, Blocks.SAND.defaultBlockState());
            maybeGrantMirageBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        return didAny;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        String key = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();

        if (stack.is(Items.ROTTEN_FLESH)) {
            itemEntity.setItem(new ItemStack(Items.COOKED_BEEF, stack.getCount()));
            maybeGrantMirageBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (stack.is(Items.STONE) || stack.is(Items.COBBLESTONE)
                || stack.is(Items.ANDESITE) || stack.is(Items.DIORITE) || stack.is(Items.GRANITE)) {
            itemEntity.setItem(new ItemStack(Items.SANDSTONE, stack.getCount()));
            maybeGrantMirageBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (stack.is(Items.DEEPSLATE) || stack.is(Items.COBBLED_DEEPSLATE) || stack.is(Items.TUFF)) {
            itemEntity.setItem(new ItemStack(Items.RED_SAND, stack.getCount()));
            maybeGrantMirageBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (stack.is(Items.LAVA_BUCKET)) {
            itemEntity.setItem(new ItemStack(Items.GLASS, stack.getCount()));
            maybeGrantMirageBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (stack.is(ItemTags.TERRACOTTA) || key.endsWith("_glazed_terracotta")) {
            itemEntity.setItem(new ItemStack(Items.SAND, stack.getCount()));
            maybeGrantMirageBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.37 Delusional Phantasm Paranoia Hex:
        // Persistent psychological mirage curse (cleansed via Restoration/Vitae magic, Umbral shadow reactions, or caster defeat):
        // 50% melee/projectile miss chance & caster untargetability (SpellCombatReactionEngine), erratic swerves
        // dodging phantom obstacles, -40% active armor toughness, & Paranoid Guard-Fumble (+35% mob crit & 40% shield fumble)!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.80D, 0.40D * mult), 45L);
        target.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 60, 0, true, true));

        LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        if (hexCaster != null && target instanceof Mob mob && mob.getTarget() == hexCaster) {
            mob.setTarget(null);
            mob.setAggressive(false);
        }

        if (target.onGround() && level.random.nextFloat() < 0.55F) {
            double swerveX = (level.random.nextDouble() - 0.5D) * 0.58D;
            double swerveZ = (level.random.nextDouble() - 0.5D) * 0.58D;
            target.setDeltaMovement(target.getDeltaMovement().add(swerveX, 0.08D, swerveZ));
            target.hasImpulse = true;
            if (target instanceof Mob mob) {
                mob.swing(InteractionHand.MAIN_HAND);
            }
        }

        float hexDmg = (1.35F + potencyTier * 0.45F) * mult;
        SpellCombatReactionEngine.hurtWithMirageMateriaAdvantage(level, target, hexCaster, hexDmg, false, true);

        level.sendParticles(
                new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                target.getX(), target.getY(0.85D), target.getZ(), 4, 0.25D, 0.3D, 0.25D, 0.02D
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
        if (effect == SpellCoreEffect.RESTORATION) {
            SpellCombatReactionEngine.purifyHexesAndNegativeEffects(target);
            float heal = 3.0F + potencyTier * 0.90F;
            SpellCombatReactionEngine.notifyMagicalHeal(target, heal);
            target.heal(heal);
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private static void wipeMobAggroCompletely(LivingEntity entity) {
        entity.setLastHurtByMob(null);
        if (entity instanceof Mob mob) {
            mob.setTarget(null);
            mob.setAggressive(false);
        }
        entity.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        entity.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
    }

    private static void maybeGrantMirageBatchTransmuteDiscount(ServerLevel level, @Nullable LivingEntity caster, int tier) {
        if (!(caster instanceof Player player)) return;
        CompoundTag pData = EntityHelper.getPersistentData(player);
        long now = level.getGameTime();
        long prevTick = pData.getLong("SpellMirageBatchTick").orElse(-20L);
        int count = (prevTick == now) ? pData.getInt("SpellMirageBatchCount").orElse(0) + 1 : 1;
        pData.putLong("SpellMirageBatchTick", now);
        pData.putInt("SpellMirageBatchCount", count);
        if (count == 2) {
            long refund = Math.max(1L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25D));
            SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.MIRAGE, refund);
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
