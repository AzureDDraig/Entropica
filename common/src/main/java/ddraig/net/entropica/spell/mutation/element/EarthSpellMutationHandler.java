package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.network.SpellVfxEventPayload;
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
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.spell.vfx.SpellWorldVfxManager;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Complete per-part Signature Mutation implementation for <b>Earth Materia (Base Type #4)</b>
 * (Spellcrafting System Guide Section 6.5):
 * <ul>
 *     <li><b>Canonical Advantage</b>: Strong against <b>VOID (+1)</b> | Weak against <b>AIR (-1)</b> | Neutral to others.</li>
 *     <li><b>Projectile</b>: <i>Quake Spire</i> — travels beneath the soil as a rumbling fault line, erupting as a sharp
 *     3D stone spike directly beneath the target's feet to launch them into the air.</li>
 *     <li><b>Beam</b>: <i>Magnetite Tractor Beam</i> — emits an inward-pulling magnetic vibrational beam that reels in
 *     metallic items, ores, and armored mobs steadily toward the caster (and fractures exposed ore veins at Tier 2+).</li>
 *     <li><b>Radial Burst</b>: <i>Stone Ring Rampart</i> — instantly raises a defensive circular ring of stone barrier pillars
 *     around the caster for immediate bunker cover while sweeping adjacent foes outside the perimeter.</li>
 *     <li><b>Touch / Strike</b>: <i>Seismic Impact</i> — converts the melee strike into pure Earth Materia damage (+1 vs Void),
 *     shreds armor points, and knocks the target flat onto the ground.</li>
 *     <li><b>Aura / Self</b>: <i>Earth Defensive Attunement</i> — registers defending element as Earth: +1 advantage resistance
 *     vs incoming VOID attacks (50% damage), -1 vulnerability vs incoming AIR attacks (150% damage), +2 Armor Toughness,
 *     and 50% knockback resistance, orbited by 6 hexagonal 3D granite &amp; terracotta bastion shields.</li>
 *     <li><b>Glyph / Trap</b>: <i>Tectonic Impalement Spike</i> — 1.0s arm, 10m (or Tier 5+ indefinite) disarmable trap;
 *     shoots a colossal sharp stone stalagmite up through entering mobs, launching them vertically with Earth Materia damage.</li>
 *     <li><b>Cone / Spray</b>: <i>Sand &amp; Gravel Flurry</i> — blasts a 60-degree cone of high-velocity stone grit and gravel
 *     dealing Earth Materia damage, pushing mobs 6 blocks back, and excavating/breaking loose dirt, sand, and gravel.</li>
 *     <li><b>Rain / Skyfall</b>: <i>Meteorite Shrapnel Barrage</i> — rains dense stone meteor fragments from an overhead
 *     cavern rift (6s+ base, extended for visual admiration), repeatedly battering enemies with Earth Materia damage (+1 vs Void),
 *     staggering grounded foes, and violently knocking down airborne mobs.</li>
 *     <li><b>Summon / Eidolon</b>: <i>Tectonic Eidolon</i> — earthen brown/orange (#773214) triad: ground-punching Agent,
 *     anchored plinth stone-bolt Sentry, and immense-HP melee-taunting Granite Effigy Decoy that detonates into stone shrapnel.</li>
 *     <li><b>Flow (Slow)</b>: <i>Dense Tectonic Mass</i> — grants +0.4 Materia Advantage score (+1.4 vs Void) and guarantees
 *     an armor-shredding impact.</li>
 *     <li><b>Blink / Step</b>: <i>Burrow Step</i> — phases the caster cleanly into stone and soil, emerging safely from a
 *     targeted stone surface up to 16 blocks away.</li>
 *     <li><b>Transmute</b>: <i>Lithic Compaction</i> — compacts Cobblestone into Stone, Stone into Deepslate, and Sand/Gravel
 *     into Sandstone (both world blocks and dropped items).</li>
 *     <li><b>Affliction</b>: <i>Sundering Hex</i> — persistent anti-milk petrifying curse (cleansed by Restoration magic or
 *     caster defeat) permanently shredding 5 points of physical armor and doubling all incoming Earth Materia damage (2.0x).</li>
 * </ul>
 */
public final class EarthSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int TECTONIC_EIDOLON_HEX_COLOR = 0x773214;
    private static final int AMBER_FAULT_HEX_COLOR = 0xFFC278;

    private static final EquipmentSlot[] CHECKED_SLOTS = new EquipmentSlot[]{
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET,
            EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND
    };

    @Override
    public EssenceType materiaType() {
        return EssenceType.EARTH;
    }

    // =========================================================================
    // CANONICAL SIGNATURE TITLES & GUIDE DESCRIPTIONS (SECTION 6.5)
    // =========================================================================

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case SLOW_HEAVY -> "Dense Tectonic Mass";
            default -> "Seismic Tectonic " + flow.getDisplayName();
        };
    }

    @Override
    public @Nullable String resolveFlowDescription(SpellFlowModifier flow) {
        return switch (flow) {
            case SLOW_HEAVY -> "Dense Tectonic Mass: Slow modifier increases projectile mass, granting +0.4 Materia Advantage score (amplifying Earth's advantage vs Void) and guaranteeing an armor-shredding impact.";
            default -> "Seismic Tectonic Flow: Channels heavy lithic momentum along earthen fault lines.";
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Quake Spire";
            case BEAM -> "Magnetite Tractor Beam";
            case RADIAL_BURST -> "Stone Ring Rampart";
            case TOUCH_STRIKE -> "Seismic Impact";
            case AURA -> "Earth Defensive Attunement";
            case GLYPH -> "Tectonic Impalement Spike";
            case CONE -> "Sand & Gravel Flurry";
            case SKYFALL -> "Meteorite Shrapnel Barrage";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public @Nullable String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Quake Spire: Travels beneath the soil as a rumbling fault line, erupting as a sharp stone spike directly beneath the target's feet to launch them into the air.";
            case BEAM -> "Magnetite Tractor Beam: Emits a magnetic vibrational beam that pulls metallic items, ores, and armored mobs steadily toward the caster.";
            case RADIAL_BURST -> "Stone Ring Rampart: Instantly raises a defensive circular ring of stone barrier pillars around the caster for immediate bunker cover.";
            case TOUCH_STRIKE -> "Seismic Impact: Converts the melee strike into pure Earth Materia damage (+1 vs Void), shredding armor points and knocking the target flat.";
            case AURA -> "Earth Defensive Attunement: Defends as Earth (+1 vs Void halving damage, -1 vs Air taking 150% damage) while granting +2 Armor Toughness and 50% knockback resistance.";
            case GLYPH -> "Tectonic Impalement Spike: Senses proximity and shoots a colossal sharp stone stalagmite up through entering mobs, launching them vertically with Earth Materia damage.";
            case CONE -> "Sand & Gravel Flurry: Blasts a 60-degree cone of high-velocity stone grit and gravel that deals Earth Materia damage, pushes mobs 6 blocks back, and breaks loose dirt/gravel.";
            case SKYFALL -> "Meteorite Shrapnel Barrage: Rains dense stone meteor fragments over 6+ seconds, battering foes with Earth Materia damage (+1 vs Void), staggering targets, and knocking down airborne mobs.";
            case SUMMON_AGENT -> "Tectonic Eidolon: Earthen brown/orange (#773214) bruiser delivering heavy ground-punching Earth Materia slams that shred armor and erupt stone spikes.";
            case SUMMON_SENTRY -> "Tectonic Plinth Sentry: Anchored earthen (#773214) carved bastion plinth firing armor-shredding stone projectile bolts.";
            case SUMMON_DECOY -> "Granite Effigy Decoy: Immense-HP carved granite effigy (#773214) that taunts melee attacks before detonating into heavy stone shrapnel.";
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Burrow Step";
            case TRANSMUTE, BREAK -> "Lithic Compaction";
            case AFFLICTION -> "Sundering Hex";
            case CONSTRUCT -> "Tectonic Bastion Wall";
            default -> "Tectonic " + effect.getDisplayName();
        };
    }

    @Override
    public @Nullable String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Burrow Step: Phases the caster cleanly into stone and soil, emerging safely from a targeted stone surface up to 16 blocks away.";
            case TRANSMUTE, BREAK -> "Lithic Compaction: Compacts Cobblestone into Stone, Stone into Deepslate, and Sand/Gravel into Sandstone.";
            case AFFLICTION -> "Sundering Hex: Persistent anti-milk petrifying curse (cleansed by Restoration magic or caster defeat) shredding 5 physical armor and doubling all incoming Earth Materia damage.";
            default -> null;
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATIONS — DENSE TECTONIC MASS (SECTION 6.5)
    // =========================================================================

    @Override
    public float getFlowBonusAdvantageScore(SpellFlowModifier flow, LivingEntity target) {
        return flow == SpellFlowModifier.SLOW_HEAVY ? 0.4F : 0.0F;
    }

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        if (flow == SpellFlowModifier.SLOW_HEAVY) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DEEPSLATE.defaultBlockState()),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.12D, 0.12D, 0.12D, 0.04D);
            if (projectile.tickCount % 2 == 0) {
                level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), AMBER_FAULT_HEX_COLOR, 22, 1.05F, 0.08F),
                        projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.08D, 0.08D, 0.08D, 0.01D);
            }
        }
    }

    @Override
    public void onProjectileHitEntityFromFlow(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            Entity hitEntity,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        if (spell.flowPart().flow() != SpellFlowModifier.SLOW_HEAVY) {
            return;
        }
        if (!(hitEntity instanceof LivingEntity living) || !living.isAlive()) {
            return;
        }
        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard())) {
            return;
        }
        // Dense Tectonic Mass: guaranteed armor-shredding impact on hit!
        shredArmor(level, living, 3.0D + spell.getEffectiveTier() * 0.35D, 160L);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DEEPSLATE.defaultBlockState()),
                living.getX(), living.getY(0.55D), living.getZ(), 14, 0.28D, 0.32D, 0.28D, 0.06D);
        level.playSound(null, living.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.45F, 0.72F);
    }

    // =========================================================================
    // PART 2: DELIVERY FORM MUTATIONS (SECTION 6.5)
    // =========================================================================

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        // Quake Spire: travels beneath/along the soil as a rumbling fault line, stepping cleanly over 1-block terrain ledges!
        BlockPos curPos = projectile.blockPosition();
        BlockPos groundBlock = null;
        for (int dy = 0; dy <= 4; dy++) {
            BlockPos check = curPos.below(dy);
            BlockState bs = level.getBlockState(check);
            if (!bs.getCollisionShape(level, check).isEmpty() && level.getBlockState(check.above()).getCollisionShape(level, check.above()).isEmpty()) {
                groundBlock = check;
                break;
            }
        }

        if (groundBlock != null) {
            double surfaceY = groundBlock.getY() + 1.0D;
            double desiredY = surfaceY + 0.22D;
            double blendedY = projectile.getY() + (desiredY - projectile.getY()) * 0.45D;

            // Step smoothly over a 1-block soil/stone rise directly ahead so the fault line doesn't stub its toe on hills
            Vec3 vel = projectile.getDeltaMovement();
            Vec3 horiz = new Vec3(vel.x, 0.0D, vel.z);
            if (horiz.lengthSqr() > 1.0E-4D) {
                Vec3 lookStep = horiz.normalize().scale(0.85D);
                BlockPos aheadFoot = BlockPos.containing(projectile.getX() + lookStep.x, surfaceY + 0.15D, projectile.getZ() + lookStep.z);
                if (!level.getBlockState(aheadFoot).getCollisionShape(level, aheadFoot).isEmpty()
                        && level.getBlockState(aheadFoot.above()).getCollisionShape(level, aheadFoot.above()).isEmpty()) {
                    blendedY = Math.max(blendedY, aheadFoot.getY() + 1.22D);
                }
            }
            projectile.setPos(projectile.getX(), blendedY, projectile.getZ());
            if (vel.y < -0.04D) {
                projectile.setDeltaMovement(vel.x, -0.02D, vel.z);
            }

            BlockState trailBlock = level.getBlockState(groundBlock);
            if (trailBlock.isAir()) {
                trailBlock = Blocks.STONE.defaultBlockState();
            }
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, trailBlock),
                    projectile.getX(), surfaceY + 0.08D, projectile.getZ(), 4, 0.18D, 0.06D, 0.18D, 0.05D);
            if (projectile.tickCount % 2 == 0) {
                spawnFaultCrystalSparks(level, new Vec3(projectile.getX(), surfaceY + 0.12D, projectile.getZ()), 2, 0.16D);
            }
            if (projectile.tickCount % 4 == 0) {
                level.playSound(null, groundBlock, SoundEvents.DRIPSTONE_BLOCK_STEP, SoundSource.PLAYERS, 0.45F, 0.68F);
            }
        } else {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.TERRACOTTA.defaultBlockState()),
                    projectile.getX(), projectile.getY() - 0.10D, projectile.getZ(), 2, 0.10D, 0.08D, 0.10D, 0.03D);
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
        // Quake Spire: sharp stone spike erupts directly beneath the target's feet, launching them into the air!
        if (!(hitEntity instanceof LivingEntity living) || !living.isAlive()) {
            return;
        }
        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard())) {
            return;
        }

        int tier = spell.getEffectiveTier();
        boolean denseMass = isDenseTectonicMassActive(spell);
        float spikeDmg = slot.hasHarmVerb()
                ? (2.5F + tier * 0.75F)
                : Math.max(3.5F + tier * 0.90F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.55F);
        SpellCombatReactionEngine.hurtWithEarthMateriaAdvantage(level, living, caster, spikeDmg, denseMass, true);
        shredArmor(level, living, 2.5D, 150L);

        if (!SpellCombatReactionEngine.isBossEntity(level, living)) {
            Vec3 v = living.getDeltaMovement();
            SpellExecutionEngine.applySyncedMotion(living, new Vec3(v.x * 0.35D, 0.98D, v.z * 0.35D));
        }

        eruptTemporaryStoneSpike(level, caster, living.blockPosition(), spell, slot, 80);

        // Generous 64-tick (3.2s) 3D stalagmite spire cluster & amber fault crater beneath the launched target!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                living.position().add(0.0D, 0.05D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.EARTH, null, 1.85F, 64, tier
        );
        spawnFaultCrystalSparks(level, living.position().add(0.0D, 0.45D, 0.0D), 14, 0.38D);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.POINTED_DRIPSTONE.defaultBlockState()),
                living.getX(), living.getY() + 0.35D, living.getZ(), 20, 0.28D, 0.55D, 0.28D, 0.09D);
        level.playSound(null, living.blockPosition(), SoundEvents.DRIPSTONE_BLOCK_BREAK, SoundSource.PLAYERS, 0.95F, 0.76F);
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
        // Ground-impact Quake Spire erupts a sharp stone stalagmite that launches adjacent enemies upward!
        int tier = spell.getEffectiveTier();
        boolean denseMass = isDenseTectonicMassActive(spell);
        Vec3 pos = blockHit.getLocation();
        BlockPos aboveHit = blockHit.getBlockPos().above();

        eruptTemporaryStoneSpike(level, caster, aboveHit, spell, slot, 80);
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                pos, new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.EARTH, null, 1.80F, 64, tier
        );
        spawnFaultCrystalSparks(level, pos.add(0.0D, 0.30D, 0.0D), 12, 0.42D);
        level.playSound(null, blockHit.getBlockPos(), SoundEvents.DRIPSTONE_BLOCK_BREAK, SoundSource.PLAYERS, 0.90F, 0.78F);

        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos, pos).inflate(2.25D))) {
            if (!foe.isAlive() || foe == caster) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithEarthMateriaAdvantage(level, foe, caster, 3.0F + tier * 0.75F, denseMass, true);
            }
            shredArmor(level, foe, 2.0D, 140L);
            if (!SpellCombatReactionEngine.isBossEntity(level, foe)) {
                Vec3 v = foe.getDeltaMovement();
                SpellExecutionEngine.applySyncedMotion(foe, new Vec3(v.x * 0.35D, 0.92D, v.z * 0.35D));
            }
        }
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
        // Magnetite Tractor Beam: pulls metallic items, ores, and armored mobs steadily toward the caster!
        int tier = spell.getEffectiveTier();
        long now = level.getGameTime();
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 eye = caster.getEyePosition();
        Vec3 pullTarget = caster.position().add(0.0D, 0.75D, 0.0D);
        double beamLen = Math.min(maxReach, blockHit.getLocation().distanceTo(eye) + 0.75D);

        // Optional Tier 2+ exposed ore-vein fracture at the beam endpoint so ores pop loose and reel right in!
        if (tier >= 2 && now % 15L == 0L) {
            BlockPos hitPos = blockHit.getBlockPos();
            if (hitPos.distToCenterSqr(eye) <= (maxReach + 1.0D) * (maxReach + 1.0D)) {
                BlockState hitState = level.getBlockState(hitPos);
                float destroySpeed = hitState.getDestroySpeed(level, hitPos);
                if (destroySpeed >= 0.0F && destroySpeed < 45.0F
                        && isOreBlock(hitState)
                        && !SpellExecutionEngine.isProtectedBlockOrContainer(level, hitPos, hitState)) {
                    level.destroyBlock(hitPos, true, caster);
                    spawnFaultCrystalSparks(level, Vec3.atCenterOf(hitPos), 10, 0.35D);
                    level.playSound(null, hitPos, SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 0.80F, 1.15F);
                }
            }
        }

        AABB corridor = caster.getBoundingBox().expandTowards(normDir.scale(beamLen)).inflate(2.35D);

        // 1. Steadily reel in armored & metallic-equipped mobs along the magnetic beam corridor
        Set<UUID> pulledMobs = new HashSet<>();
        for (LivingEntity hit : hitEntities) {
            if (!hit.isAlive() || hit == caster) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            pulledMobs.add(hit.getUUID());
            reelMobTowardCaster(level, caster, hit, isArmoredOrMetallicMob(hit));
        }

        for (LivingEntity cand : level.getEntitiesOfClass(LivingEntity.class, corridor)) {
            if (!cand.isAlive() || cand == caster || pulledMobs.contains(cand.getUUID())) continue;
            if (!isArmoredOrMetallicMob(cand)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(cand, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            if (distanceToRaySegment(cand.getBoundingBox().getCenter(), eye, normDir, beamLen) <= 2.15D) {
                reelMobTowardCaster(level, caster, cand, true);
            }
        }

        // 2. Steadily reel in metallic items & ores along the magnetic beam corridor
        boolean pulledAnyMetal = false;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, corridor)) {
            if (!item.isAlive() || item.getItem().isEmpty()) continue;
            if (!isMetallicOrOreItem(item.getItem())) continue;
            if (distanceToRaySegment(item.position(), eye, normDir, beamLen) > 2.45D) continue;

            Vec3 toCaster = pullTarget.subtract(item.position());
            double dist = toCaster.length();
            if (dist < 2.25D) {
                item.setNoPickUpDelay();
            }
            if (dist > 0.25D) {
                double speed = Math.min(1.05D, 0.45D + 0.08D * tier);
                item.setDeltaMovement(toCaster.scale(speed / dist));
                item.hasImpulse = true;
                item.hurtMarked = true;
                pulledAnyMetal = true;
                if (now % 4L == 0L) {
                    level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), AMBER_FAULT_HEX_COLOR, 18, 0.90F, 0.10F),
                            item.getX(), item.getY() + 0.15D, item.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D);
                }
            }
        }

        if (pulledAnyMetal && now % 12L == 0L) {
            level.playSound(null, caster.blockPosition(), SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 0.45F, 1.35F);
        }
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
        // Seismic Impact: converts strike into pure Earth Materia damage (+1 vs Void), shreds armor, and knocks target flat!
        int tier = spell.getEffectiveTier();
        boolean denseMass = isDenseTectonicMassActive(spell);
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();

        // Generous 48-tick (2.4s) 3D chiseled basalt & terracotta cleave!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_CRESCENT_SLASH,
                caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D), normDir,
                EssenceType.EARTH, null, 3.2F, 48, tier
        );

        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (!hit.isAlive() || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            if (!slot.hasHarmVerb()) {
                float slamDmg = Math.max(4.0F + tier * 1.10F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.65F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithEarthMateriaAdvantage(level, hit, caster, slamDmg, denseMass, true);
            }

            shredArmor(level, hit, 4.0D + tier * 0.5D, 200L);

            // Knock the target flat onto the ground!
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 38, 5, false, true));
            hit.addEffect(new MobEffectInstance(
                    BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 38, 0, false, false));
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 38L);
            if (!SpellCombatReactionEngine.isBossEntity(level, hit)) {
                SpellExecutionEngine.applySyncedMotion(hit, new Vec3(normDir.x * 0.62D, -0.58D, normDir.z * 0.62D));
            }

            SpellWorldVfxManager.broadcast(
                    level, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                    hit.position().add(0.0D, 0.05D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                    EssenceType.EARTH, null, 1.45F, 54, tier
            );
            spawnFaultCrystalSparks(level, hit.position().add(0.0D, 0.45D, 0.0D), 12, 0.35D);
        }

        if (!hitEntities.isEmpty()) {
            level.playSound(null, caster.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.60F, 0.74F);
            level.playSound(null, caster.blockPosition(), SoundEvents.DEEPSLATE_BREAK, SoundSource.PLAYERS, 0.90F, 0.78F);
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
        // Stone Ring Rampart: instantly raises a defensive circular ring of stone barrier pillars around the caster for immediate bunker cover!
        int tier = spell.getEffectiveTier();
        double bunkerRadius = 3.25D;
        int rampartDuration = 280; // 14.0s generous bunker lifetime so cover is genuinely useful and admirable!

        // Sweep adjacent enemies outside the bunker perimeter so they aren't trapped inside with the caster
        AABB clearBox = new AABB(
                center.x - (bunkerRadius + 1.4D), center.y - 1.5D, center.z - (bunkerRadius + 1.4D),
                center.x + (bunkerRadius + 1.4D), center.y + 2.8D, center.z + (bunkerRadius + 1.4D)
        );
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, clearBox)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            Vec3 delta = foe.position().subtract(center);
            Vec3 horiz = new Vec3(delta.x, 0.0D, delta.z);
            if (horiz.lengthSqr() <= (bunkerRadius + 1.2D) * (bunkerRadius + 1.2D) && !SpellCombatReactionEngine.isBossEntity(level, foe)) {
                Vec3 out = (horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : new Vec3(1.0D, 0.0D, 0.0D)).scale(1.25D);
                SpellExecutionEngine.applySyncedMotion(foe, new Vec3(out.x, 0.34D, out.z));
            }
        }

        // Generous 72-tick (3.6s) 3D erupting monolith rampart VFX + full-duration 14s bedrock bastion seal!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_RADIAL_NOVA,
                center, new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.EARTH, null, (float) Math.max(bunkerRadius + 0.5D, radius * 0.85D), 72, tier
        );
        SpellWorldVfxManager.broadcastArcaneSeal(level, center, bunkerRadius + 0.35D, EssenceType.EARTH, null, rampartDuration, tier);

        List<BlockPos> placedPillars = new ArrayList<>();
        Set<BlockPos> visitedColumns = new HashSet<>();
        BlockPos cPos = BlockPos.containing(center);

        // 12-pillar crenellated circular stone bunker (alternating 3-block Stone Brick pillars & 2-block Cobblestone battlements)
        for (int idx = 0; idx < 12; idx++) {
            double rad = Math.toRadians(idx * 30.0D);
            int dx = (int) Math.round(Math.cos(rad) * bunkerRadius);
            int dz = (int) Math.round(Math.sin(rad) * bunkerRadius);
            BlockPos colBase = findLocalBunkerFloor(level, cPos.offset(dx, 0, dz));
            if (!visitedColumns.add(colBase)) continue;

            int pillarHeight = (idx % 2 == 0) ? 3 : 2;
            BlockState blockChoice = (idx % 2 == 0)
                    ? Blocks.STONE_BRICKS.defaultBlockState()
                    : Blocks.COBBLESTONE.defaultBlockState();

            for (int dy = 0; dy < pillarHeight; dy++) {
                BlockPos pillarPos = colBase.above(dy);
                BlockState existing = level.getBlockState(pillarPos);
                if ((existing.isAir() || existing.canBeReplaced())
                        && !SpellExecutionEngine.isProtectedBlockOrContainer(level, pillarPos, existing)) {
                    level.setBlockAndUpdate(pillarPos, blockChoice);
                    placedPillars.add(pillarPos.immutable());
                }
            }
        }

        if (!placedPillars.isEmpty()) {
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, center, bunkerRadius, rampartDuration, level.getGameTime(), spell, slot, EssenceType.EARTH, apparatusPotency,
                    (srv, zone, elapsed) -> {
                        if (elapsed < zone.maxDurationTicks) {
                            return true;
                        }
                        for (BlockPos p : placedPillars) {
                            BlockState st = srv.getBlockState(p);
                            if (st.is(Blocks.COBBLESTONE) || st.is(Blocks.STONE_BRICKS)) {
                                srv.removeBlock(p, false);
                                srv.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, st),
                                        p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D, 6, 0.22D, 0.22D, 0.22D, 0.04D);
                            }
                        }
                        srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 0.85F, 0.78F);
                        return false;
                    }
            ));
        }

        spawnFaultCrystalSparks(level, center.add(0.0D, 0.6D, 0.0D), 24, bunkerRadius * 0.65D);
        level.playSound(null, cPos, SoundEvents.STONE_PLACE, SoundSource.PLAYERS, 1.10F, 0.72F);
        level.playSound(null, cPos, SoundEvents.DEEPSLATE_BRICKS_PLACE, SoundSource.PLAYERS, 1.00F, 0.75F);
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
        // Earth Defensive Attunement:
        // +1 vs incoming VOID (50% damage), -1 vs incoming AIR (150% damage), exact +2 Armor Toughness, and 50% Knockback Resistance!
        int tier = spell.getEffectiveTier();
        int dur = 240 + tier * 50;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_EARTH_DEFENSE_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.applyTimedDefenseBuff(caster, level, 2.0D, 0.50D, dur);
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.60F, null);

        SpellWorldVfxManager.broadcastArcaneSeal(level, caster.position(), 1.65D, EssenceType.EARTH, null, 60, tier);
        spawnFaultCrystalSparks(level, caster.position().add(0.0D, 0.95D, 0.0D), 20, 0.55D);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.TERRACOTTA.defaultBlockState()),
                caster.getX(), caster.getY(0.8D), caster.getZ(), 16, 0.45D, 0.45D, 0.45D, 0.04D);
        level.playSound(null, caster.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.65F, 0.78F);
        level.playSound(null, caster.blockPosition(), SoundEvents.DEEPSLATE_TILES_PLACE, SoundSource.PLAYERS, 0.95F, 0.80F);
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
        // Tectonic Impalement Spike: canonical 1.0s (20t) arming delay, 10m (12,000t) or Tier 5+ indefinite persistence, 50% Sneak-Right-Click disarm!
        // Shoots a colossal sharp stone stalagmite up through entering mobs, launching them vertically with Earth Materia damage!
        int tier = spell.getEffectiveTier();
        double senseRadius = Math.max(2.6D, Math.min(4.5D, radius));
        int glyphLifetime = tier >= 5 ? (Integer.MAX_VALUE / 4) : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(level, glyphCenter, senseRadius * 0.85D, EssenceType.EARTH, null, 220, tier);
        level.playSound(null, BlockPos.containing(glyphCenter), SoundEvents.DEEPSLATE_TILES_PLACE, SoundSource.PLAYERS, 0.85F, 0.80F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, senseRadius, glyphLifetime, level.getGameTime(), spell, slot, EssenceType.EARTH, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    // Canonical 1.0s (20-tick) arming delay before proximity sensor activates
                    if (elapsed < 20) return true;
                    if (elapsed % 4 != 0) return true;

                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                    AABB triggerBox = new AABB(
                            zone.center.x - senseRadius * 0.82D, zone.center.y - 1.0D, zone.center.z - senseRadius * 0.82D,
                            zone.center.x + senseRadius * 0.82D, zone.center.y + 2.5D, zone.center.z + senseRadius * 0.82D
                    );
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, triggerBox,
                            e -> e.isAlive() && e != owner && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe)
                                    && !(e instanceof SpellEidolonEntity eid && owner != null && eid.isOwnedBy(owner)));
                    if (stepped.isEmpty()) return true;

                    boolean denseMass = zone.spell != null && isDenseTectonicMassActive(zone.spell);
                    float impaleDmg = Math.max(5.5F + tier * 1.35F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.75F)
                            * Math.max(0.5F, zone.apparatusPotency);

                    for (LivingEntity victim : stepped) {
                        SpellCombatReactionEngine.hurtWithEarthMateriaAdvantage(srv, victim, owner, impaleDmg, denseMass, true);
                        shredArmor(srv, victim, 4.0D, 180L);
                        if (!SpellCombatReactionEngine.isBossEntity(srv, victim)) {
                            Vec3 v = victim.getDeltaMovement();
                            SpellExecutionEngine.applySyncedMotion(victim, new Vec3(v.x * 0.25D, 1.28D, v.z * 0.25D));
                        }
                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, victim, new Vec3(0.0D, 1.0D, 0.0D),
                                    zone.spell, zone.slot, EssenceType.EARTH, zone.apparatusPotency
                            );
                        }
                    }

                    // Colossal 2-block temporary stone stalagmite + generous 88-tick (4.4s) 3D stalagmite eruption!
                    BlockPos baseSpike = BlockPos.containing(zone.center);
                    eruptTemporaryStoneColumn(srv, owner, baseSpike, zone.spell, zone.slot, 88);
                    SpellWorldVfxManager.broadcast(
                            srv, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                            zone.center.add(0.0D, 0.05D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                            EssenceType.EARTH, null, 2.45F, 88, tier
                    );
                    SpellWorldVfxManager.broadcastArcaneSeal(srv, zone.center, senseRadius, EssenceType.EARTH, null, 72, tier);
                    spawnFaultCrystalSparks(srv, zone.center.add(0.0D, 0.9D, 0.0D), 28, senseRadius * 0.55D);
                    srv.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.POINTED_DRIPSTONE.defaultBlockState()),
                            zone.center.x, zone.center.y + 0.8D, zone.center.z, 34, 0.55D, 0.95D, 0.55D, 0.12D);
                    srv.playSound(null, baseSpike, SoundEvents.DRIPSTONE_BLOCK_BREAK, SoundSource.PLAYERS, 1.15F, 0.65F);
                    srv.playSound(null, baseSpike, SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.55F, 0.68F);
                    return false;
                }
        ).markDisarmableGlyph());
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
        // Sand & Gravel Flurry: blasts a 60-degree cone of high-velocity stone grit and gravel dealing Earth Materia damage,
        // pushing mobs 6 blocks back, and excavating/breaking loose dirt, sand, and gravel blocks in its path!
        int tier = spell.getEffectiveTier();
        boolean denseMass = isDenseTectonicMassActive(spell);
        double reach = Math.max(8.0D, coneRange);
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 eye = caster.getEyePosition();

        // Generous 54-tick (2.7s) 3D tumbling gravel & sandstone cone flurry!
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_VORTEX_CONE,
                eye.subtract(0.0D, 0.20D, 0.0D), normDir,
                EssenceType.EARTH, null, (float) reach, 54, tier
        );

        float gritDmg = (slot.hasHarmVerb()
                ? (2.5F + tier * 0.65F)
                : Math.max(3.8F + tier * 0.95F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.55F))
                * Math.max(0.5F, apparatusPotency);

        // True 60-degree cone (30-degree half-angle -> cos(30°) ≈ 0.866, using 0.84 for clean hitbox edge forgiveness)
        Set<UUID> blasted = new HashSet<>();
        for (LivingEntity hit : hitTargets) {
            if (!hit.isAlive() || hit == caster) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            blasted.add(hit.getUUID());
            applyFlurryBlastToTarget(level, caster, hit, normDir, gritDmg, denseMass);
        }

        for (LivingEntity cand : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(reach + 1.0D))) {
            if (!cand.isAlive() || cand == caster || blasted.contains(cand.getUUID())) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(cand, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            Vec3 toCand = cand.getBoundingBox().getCenter().subtract(eye);
            double dist = toCand.length();
            if (dist <= reach && dist > 0.1D && toCand.scale(1.0D / dist).dot(normDir) >= 0.84D) {
                blasted.add(cand.getUUID());
                applyFlurryBlastToTarget(level, caster, cand, normDir, gritDmg, denseMass);
            }
        }

        // Excavate/break loose dirt, sand, and gravel blocks inside the 60-degree cone
        int maxSteps = Math.min(9, (int) Math.ceil(reach));
        int brokenBlocks = 0;
        for (int s = 1; s <= maxSteps && brokenBlocks < 48; s++) {
            Vec3 sliceCenter = eye.add(normDir.scale(s));
            int halfSpan = Math.max(1, (int) Math.ceil(s * 0.55D)); // tan(30°) ≈ 0.577
            BlockPos c = BlockPos.containing(sliceCenter);
            for (BlockPos p : BlockPos.betweenClosed(c.offset(-halfSpan, -1, -halfSpan), c.offset(halfSpan, 1, halfSpan))) {
                if (brokenBlocks >= 48) break;
                Vec3 toBlock = Vec3.atCenterOf(p).subtract(eye);
                double bDist = toBlock.length();
                if (bDist > reach || (bDist > 1.2D && toBlock.scale(1.0D / bDist).dot(normDir) < 0.82D)) {
                    continue;
                }
                BlockState bs = level.getBlockState(p);
                if (isLooseExcavatableSediment(bs)
                        && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) {
                    level.destroyBlock(p.immutable(), true, caster);
                    brokenBlocks++;
                }
            }
        }

        for (double d = 1.5D; d <= reach * 0.85D; d += 2.0D) {
            Vec3 pt = eye.add(normDir.scale(d));
            double sp = 0.25D + d * 0.22D;
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GRAVEL.defaultBlockState()),
                    pt.x, pt.y, pt.z, 10, sp, sp * 0.55D, sp, 0.08D);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()),
                    pt.x, pt.y, pt.z, 8, sp, sp * 0.55D, sp, 0.06D);
        }

        // Section 4.7 Earth Cone Recoil Impulse: crisp backward recoil on the caster
        caster.push(-normDir.x * 0.48D, 0.10D, -normDir.z * 0.48D);
        caster.hasImpulse = true;
        caster.hurtMarked = true;
        level.playSound(null, caster.blockPosition(), SoundEvents.GRAVEL_BREAK, SoundSource.PLAYERS, 1.00F, 0.78F);
        level.playSound(null, caster.blockPosition(), SoundEvents.SAND_BREAK, SoundSource.PLAYERS, 0.90F, 0.85F);
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
        // Meteorite Shrapnel Barrage: rains dense stone meteor fragments over 6+ seconds (140t+ for rich visual admiration),
        // repeatedly battering enemies with Earth Materia damage (+1 vs Void), staggering targets, and knocking down airborne mobs!
        // Note: SpellLingeringZoneManager.registerZone automatically broadcasts our 3D SkyfallPillar & ArcaneSeal for EssenceType.EARTH!
        int tier = spell.getEffectiveTier();
        double r = Math.max(5.5D, radius);
        int barrageDuration = 140 + (tier - 1) * 16; // 7.0s at Tier 1 up to 14.2s at Tier 10!

        level.playSound(null, BlockPos.containing(impactCenter), SoundEvents.DEEPSLATE_BREAK, SoundSource.PLAYERS, 1.15F, 0.65F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, barrageDuration, level.getGameTime(), spell, slot, EssenceType.EARTH, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;

                    if (elapsed % 5 == 0) {
                        srv.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DEEPSLATE.defaultBlockState()),
                                zone.center.x, zone.center.y + 6.5D, zone.center.z,
                                18, zone.radius * 0.52D, 1.2D, zone.radius * 0.52D, 0.22D);
                        srv.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.TERRACOTTA.defaultBlockState()),
                                zone.center.x, zone.center.y + 5.0D, zone.center.z,
                                10, zone.radius * 0.48D, 1.0D, zone.radius * 0.48D, 0.16D);
                        spawnFaultCrystalSparks(srv, zone.center.add(0.0D, 0.35D, 0.0D), 8, zone.radius * 0.55D);
                    }

                    if (elapsed % 15 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        boolean denseMass = zone.spell != null && isDenseTectonicMassActive(zone.spell);
                        float meteorDmg = (2.8F + tier * 0.75F) * Math.max(0.5F, zone.apparatusPotency);

                        // Spawn 2 localized 3D stone meteorite crater bursts inside the barrage footprint each volley!
                        for (int m = 0; m < 2; m++) {
                            double ang = srv.random.nextDouble() * Math.PI * 2.0D;
                            double dist = srv.random.nextDouble() * (zone.radius * 0.78D);
                            Vec3 strikePt = zone.center.add(Math.cos(ang) * dist, 0.05D, Math.sin(ang) * dist);
                            SpellWorldVfxManager.broadcast(
                                    srv, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                                    strikePt, new Vec3(0.0D, 1.0D, 0.0D),
                                    EssenceType.EARTH, null, 1.15F, 34, tier
                            );
                        }
                        srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.DRIPSTONE_BLOCK_BREAK, SoundSource.PLAYERS, 0.85F, 0.72F);

                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 8.5D, zone.center.z + zone.radius
                        );
                        long now = srv.getGameTime();
                        for (LivingEntity victim : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            double dx = victim.getX() - zone.center.x;
                            double dz = victim.getZ() - zone.center.z;
                            if (dx * dx + dz * dz > (zone.radius + 0.6D) * (zone.radius + 0.6D)) continue;
                            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(victim, owner, wardSafe)) {
                                continue;
                            }

                            // Pure Earth Materia meteorite shrapnel (+1 vs Void, -1 vs Air, 2x on Sundering Hex)!
                            SpellCombatReactionEngine.hurtWithEarthMateriaAdvantage(srv, victim, owner, meteorDmg, denseMass, true);

                            // Stagger grounded targets & violently slam airborne mobs straight to the earth!
                            victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 26, 2, false, true));
                            if (!SpellCombatReactionEngine.isBossEntity(srv, victim)) {
                                Vec3 v = victim.getDeltaMovement();
                                if (!victim.onGround()) {
                                    SpellExecutionEngine.applySyncedMotion(victim, new Vec3(v.x * 0.25D, -1.35D, v.z * 0.25D));
                                    EntityHelper.getPersistentData(victim).putLong(
                                            SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 35L);
                                } else {
                                    double jx = (srv.random.nextDouble() - 0.5D) * 0.28D;
                                    double jz = (srv.random.nextDouble() - 0.5D) * 0.28D;
                                    SpellExecutionEngine.applySyncedMotion(victim, new Vec3(jx, -0.18D, jz));
                                }
                            }

                            if (zone.spell != null && zone.slot != null) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(
                                        srv, owner, victim, new Vec3(0.0D, -1.0D, 0.0D),
                                        zone.spell, zone.slot, EssenceType.EARTH, zone.apparatusPotency * 0.35F
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
        tag.putString("SpellEidolonMateria", EssenceType.EARTH.name());
        tag.putInt("SpellEidolonTintRGB", TECTONIC_EIDOLON_HEX_COLOR);

        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                spawnPos.add(0.0D, 0.05D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.EARTH, null, 1.55F, 56, tier
        );
        spawnFaultCrystalSparks(level, spawnPos.add(0.0D, 0.75D, 0.0D), 22, 0.60D);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                spawnPos.x, spawnPos.y + 0.5D, spawnPos.z, 26, 0.5D, 0.5D, 0.5D, 0.10D);
        level.playSound(null, BlockPos.containing(spawnPos), SoundEvents.DEEPSLATE_BRICKS_PLACE, SoundSource.PLAYERS, 1.00F, 0.72F);
    }

    // =========================================================================
    // PART 3: CORE EFFECT MUTATIONS (SECTION 6.5)
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Burrow Step: phases cleanly into stone & soil and erupts safely from the targeted stone surface!
        SpellWorldVfxManager.broadcastArcaneSeal(level, departurePos, 1.45D, EssenceType.EARTH, null, 58, potencyTier);
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                departurePos.add(0.0D, 0.04D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.EARTH, null, 1.25F, 50, potencyTier
        );

        SpellWorldVfxManager.broadcastArcaneSeal(level, arrivalPos, 1.55D, EssenceType.EARTH, null, 62, potencyTier);
        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                arrivalPos.add(0.0D, 0.04D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.EARTH, null, 1.40F, 56, potencyTier
        );

        spawnFaultCrystalSparks(level, departurePos.add(0.0D, 0.45D, 0.0D), 16, 0.40D);
        spawnFaultCrystalSparks(level, arrivalPos.add(0.0D, 0.45D, 0.0D), 18, 0.45D);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                departurePos.x, departurePos.y + 0.35D, departurePos.z, 22, 0.40D, 0.35D, 0.40D, 0.08D);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DEEPSLATE.defaultBlockState()),
                arrivalPos.x, arrivalPos.y + 0.35D, arrivalPos.z, 24, 0.42D, 0.38D, 0.42D, 0.09D);

        level.playSound(null, BlockPos.containing(departurePos), SoundEvents.ROOTED_DIRT_BREAK, SoundSource.PLAYERS, 0.85F, 0.72F);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.DRIPSTONE_BLOCK_BREAK, SoundSource.PLAYERS, 0.95F, 0.80F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Lithic Compaction: Cobblestone -> Stone, Stone -> Deepslate, Sand/Gravel -> Sandstone!
        BlockState compacted = null;
        if (state.is(Blocks.COBBLESTONE) || state.is(Blocks.MOSSY_COBBLESTONE)) {
            compacted = Blocks.STONE.defaultBlockState();
        } else if (state.is(Blocks.STONE) || state.is(Blocks.COBBLED_DEEPSLATE)) {
            compacted = Blocks.DEEPSLATE.defaultBlockState();
        } else if (state.is(Blocks.SAND) || state.is(Blocks.GRAVEL)
                || state.is(Blocks.SUSPICIOUS_SAND) || state.is(Blocks.SUSPICIOUS_GRAVEL)) {
            compacted = Blocks.SANDSTONE.defaultBlockState();
        } else if (state.is(Blocks.RED_SAND)) {
            compacted = Blocks.RED_SANDSTONE.defaultBlockState();
        }

        if (compacted != null) {
            level.setBlockAndUpdate(pos, compacted);
            spawnFaultCrystalSparks(level, Vec3.atCenterOf(pos), 8, 0.32D);
            level.sendParticles(TimedTintableParticleOption.linear(ModParticles.RUNE_URUZ.get(), AMBER_FAULT_HEX_COLOR, 28, 1.05F, 0.04F),
                    pos.getX() + 0.5D, pos.getY() + 0.65D, pos.getZ() + 0.5D, 3, 0.22D, 0.22D, 0.22D, 0.01D);
            level.playSound(null, pos, SoundEvents.BASALT_PLACE, SoundSource.PLAYERS, 0.80F, 0.82F);
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
        ItemStack stack = itemEntity.getItem();
        int count = stack.getCount();
        ItemStack compacted = null;
        if (stack.is(Items.COBBLESTONE) || stack.is(Items.MOSSY_COBBLESTONE)) {
            compacted = new ItemStack(Items.STONE, count);
        } else if (stack.is(Items.STONE) || stack.is(Items.COBBLED_DEEPSLATE)) {
            compacted = new ItemStack(Items.DEEPSLATE, count);
        } else if (stack.is(Items.SAND) || stack.is(Items.GRAVEL)) {
            compacted = new ItemStack(Items.SANDSTONE, count);
        } else if (stack.is(Items.RED_SAND)) {
            compacted = new ItemStack(Items.RED_SANDSTONE, count);
        }

        if (compacted != null) {
            itemEntity.setItem(compacted);
            spawnFaultCrystalSparks(level, itemEntity.position().add(0.0D, 0.20D, 0.0D), 8, 0.25D);
            level.playSound(null, itemEntity.blockPosition(), SoundEvents.BASALT_PLACE, SoundSource.PLAYERS, 0.70F, 0.90F);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Sundering Hex: persistent anti-milk petrifying curse (cleansed by Restoration magic or caster defeat);
        // permanently shreds 5 points of target physical armor (scaled by Hex stack) and doubles all incoming Earth Materia damage!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 5.0D * mult, 0.0D, 50L);
        SpellCombatReactionEngine.hurtWithPureMateria(level, target, level.damageSources().magic(), (1.15F + potencyTier * 0.45F) * mult);

        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                target.getX(), target.getY(0.60D), target.getZ(), 5, 0.25D, 0.30D, 0.25D, 0.04D);
        spawnFaultCrystalSparks(level, target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D), 4, 0.25D);
        return true;
    }

    // =========================================================================
    // TECTONIC & MAGNETITE HELPERS
    // =========================================================================

    private static boolean isDenseTectonicMassActive(SpellAssembly spell) {
        if (spell.flowPart().flow() != SpellFlowModifier.SLOW_HEAVY || !spell.flowPart().mutationEnabled()) {
            return false;
        }
        EssenceType flowMat = spell.flowPart().resolveEffectiveMateria(spell.materiaSpecific(), EssenceType.REGULAR);
        return flowMat == EssenceType.EARTH;
    }

    private static void shredArmor(ServerLevel level, LivingEntity target, double pts, long durationTicks) {
        SpellCombatReactionEngine.applyTimedAttributeShred(target, level, pts, 0.0D, durationTicks);
    }

    private static void applyFlurryBlastToTarget(
            ServerLevel level,
            LivingEntity caster,
            LivingEntity target,
            Vec3 normDir,
            float gritDmg,
            boolean denseMass
    ) {
        SpellCombatReactionEngine.hurtWithEarthMateriaAdvantage(level, target, caster, gritDmg, denseMass, true);
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0, false, true));
        if (!SpellCombatReactionEngine.isBossEntity(level, target)) {
            // Push target ~6 blocks backward with high-velocity stone grit impulse!
            SpellExecutionEngine.applySyncedMotion(target, new Vec3(normDir.x * 1.62D, 0.36D, normDir.z * 1.62D));
        }
    }

    private static void reelMobTowardCaster(ServerLevel level, LivingEntity caster, LivingEntity mob, boolean armoredOrMetallic) {
        if (SpellCombatReactionEngine.isBossEntity(level, mob)) {
            return;
        }
        Vec3 toCaster = caster.position().subtract(mob.position());
        double dist = toCaster.length();
        if (dist < 1.35D) {
            return;
        }
        double pullStrength = armoredOrMetallic
                ? Math.min(0.95D, 0.44D + mob.getArmorValue() * 0.03D)
                : 0.20D;
        Vec3 pull = toCaster.scale(pullStrength / dist);
        SpellExecutionEngine.applySyncedMotion(mob, new Vec3(pull.x, Math.max(0.10D, mob.getDeltaMovement().y * 0.4D + 0.12D), pull.z));
        if (armoredOrMetallic && level.getGameTime() % 5L == 0L) {
            spawnFaultCrystalSparks(level, mob.position().add(0.0D, mob.getBbHeight() * 0.55D, 0.0D), 3, 0.24D);
        }
    }

    private static boolean isArmoredOrMetallicMob(LivingEntity entity) {
        if (entity.getArmorValue() > 0 || entity instanceof IronGolem) {
            return true;
        }
        for (EquipmentSlot slot : CHECKED_SLOTS) {
            ItemStack eq = entity.getItemBySlot(slot);
            if (!eq.isEmpty() && isMetallicOrOreItem(eq)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Identifies metallic items, armor/tools, vanilla ores, and all canonical Entropica ores &amp; metals
     * (Arcanite, Athanorite, Caputite, Eidolite, Euclidite, Mercurite, Resonite, Sanguinite, Viscanite, Entropic Ore).
     */
    private static boolean isMetallicOrOreItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().toLowerCase(Locale.ROOT);

        // Exclude non-metallic wood/stone/leather/turtle equipment even when tagged as tools or armor
        if (path.startsWith("wooden_") || path.startsWith("stone_") || path.startsWith("leather_") || path.startsWith("turtle_")) {
            return false;
        }

        if (stack.is(ItemTags.HEAD_ARMOR) || stack.is(ItemTags.CHEST_ARMOR)
                || stack.is(ItemTags.LEG_ARMOR) || stack.is(ItemTags.FOOT_ARMOR)
                || stack.is(ItemTags.SWORDS) || stack.is(ItemTags.PICKAXES)
                || stack.is(ItemTags.AXES) || stack.is(ItemTags.SHOVELS) || stack.is(ItemTags.HOES)) {
            return true;
        }

        return path.contains("iron")
                || path.contains("gold")
                || path.contains("copper")
                || path.contains("netherite")
                || path.contains("chainmail")
                || path.contains("ingot")
                || path.contains("nugget")
                || path.endsWith("_ore")
                || path.startsWith("raw_")
                || path.contains("entropic_ore")
                || path.contains("ancient_debris")
                || path.contains("anvil")
                || path.contains("bucket")
                || path.contains("minecart")
                || path.contains("hopper")
                || path.contains("cauldron")
                || path.contains("rail")
                || path.contains("compass")
                || path.contains("clock")
                || path.contains("shears")
                || path.contains("shield")
                || path.contains("trident")
                || path.contains("mace")
                || path.contains("lodestone")
                || path.contains("magnetite")
                || path.contains("horse_armor")
                || path.equals("chain")
                // Canonical Entropica Vault Metals:
                || path.contains("arcanite")
                || path.contains("athanorite")
                || path.contains("caputite")
                || path.contains("eidolite")
                || path.contains("euclidite")
                || path.contains("mercurite")
                || path.contains("resonite")
                || path.contains("sanguinite")
                || path.contains("viscanite");
    }

    private static boolean isOreBlock(BlockState state) {
        if (state.isAir()) return false;
        if (state.is(BlockTags.COAL_ORES) || state.is(BlockTags.IRON_ORES)
                || state.is(BlockTags.COPPER_ORES) || state.is(BlockTags.GOLD_ORES)
                || state.is(BlockTags.REDSTONE_ORES) || state.is(BlockTags.LAPIS_ORES)
                || state.is(BlockTags.DIAMOND_ORES) || state.is(BlockTags.EMERALD_ORES)
                || state.is(Blocks.NETHER_QUARTZ_ORE) || state.is(Blocks.GILDED_BLACKSTONE)) {
            return true;
        }
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().toLowerCase(Locale.ROOT);
        return path.endsWith("_ore") || path.startsWith("raw_") && path.endsWith("_block");
    }

    private static boolean isLooseExcavatableSediment(BlockState bs) {
        return bs.is(BlockTags.DIRT)
                || bs.is(BlockTags.SAND)
                || bs.is(Blocks.GRAVEL)
                || bs.is(Blocks.SUSPICIOUS_GRAVEL)
                || bs.is(Blocks.SUSPICIOUS_SAND)
                || bs.is(Blocks.CLAY)
                || bs.is(Blocks.MUD);
    }

    private static double distanceToRaySegment(Vec3 point, Vec3 rayStart, Vec3 rayDirNorm, double rayLen) {
        Vec3 rel = point.subtract(rayStart);
        double proj = Math.clamp(rel.dot(rayDirNorm), 0.0D, rayLen);
        Vec3 closest = rayStart.add(rayDirNorm.scale(proj));
        return point.distanceTo(closest);
    }

    private static BlockPos findLocalBunkerFloor(ServerLevel level, BlockPos approx) {
        for (int dy = 2; dy >= -2; dy--) {
            BlockPos cand = approx.offset(0, dy, 0);
            BlockState at = level.getBlockState(cand);
            BlockState below = level.getBlockState(cand.below());
            if ((at.isAir() || at.canBeReplaced()) && !below.getCollisionShape(level, cand.below()).isEmpty()) {
                return cand;
            }
        }
        return approx;
    }

    private static void eruptTemporaryStoneSpike(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos spikePos,
            SpellAssembly spell,
            SubSpellSlot slot,
            int durationTicks
    ) {
        BlockState at = level.getBlockState(spikePos);
        if ((!at.isAir() && !at.canBeReplaced())
                || level.getBlockState(spikePos.below()).getCollisionShape(level, spikePos.below()).isEmpty()
                || SpellExecutionEngine.isProtectedBlockOrContainer(level, spikePos, at)) {
            return;
        }
        level.setBlockAndUpdate(spikePos, Blocks.POINTED_DRIPSTONE.defaultBlockState());
        final BlockPos cleanupPos = spikePos.immutable();
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, Vec3.atCenterOf(cleanupPos), 1.0D, durationTicks, level.getGameTime(),
                spell, slot, EssenceType.EARTH, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed < zone.maxDurationTicks - 2) return true;
                    if (srv.getBlockState(cleanupPos).is(Blocks.POINTED_DRIPSTONE)) {
                        srv.removeBlock(cleanupPos, false);
                    }
                    return false;
                }
        ));
    }

    private static void eruptTemporaryStoneColumn(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos basePos,
            @Nullable SpellAssembly spell,
            @Nullable SubSpellSlot slot,
            int durationTicks
    ) {
        List<BlockPos> placed = new ArrayList<>();
        if (!level.getBlockState(basePos.below()).getCollisionShape(level, basePos.below()).isEmpty()) {
            for (int dy = 0; dy <= 1; dy++) {
                BlockPos p = basePos.above(dy);
                BlockState at = level.getBlockState(p);
                if ((at.isAir() || at.canBeReplaced()) && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, at)) {
                    level.setBlockAndUpdate(p, Blocks.POINTED_DRIPSTONE.defaultBlockState());
                    placed.add(p.immutable());
                } else {
                    break;
                }
            }
        }
        if (placed.isEmpty()) return;
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, Vec3.atCenterOf(basePos), 1.0D, durationTicks, level.getGameTime(),
                spell, slot, EssenceType.EARTH, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed < zone.maxDurationTicks - 2) return true;
                    for (int i = placed.size() - 1; i >= 0; i--) {
                        BlockPos p = placed.get(i);
                        if (srv.getBlockState(p).is(Blocks.POINTED_DRIPSTONE)) {
                            srv.removeBlock(p, false);
                        }
                    }
                    return false;
                }
        ));
    }

    private static void spawnFaultCrystalSparks(ServerLevel level, Vec3 pos, int count, double spread) {
        level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), TECTONIC_EIDOLON_HEX_COLOR, 30, 1.05F, 0.07F),
                pos.x, pos.y, pos.z, Math.max(1, count / 2), spread, spread * 0.65D, spread, 0.03D);
        level.sendParticles(TimedTintableParticleOption.linear(ModParticles.TINTABLE_CRYSTAL.get(), AMBER_FAULT_HEX_COLOR, 32, 1.00F, 0.08F),
                pos.x, pos.y, pos.z, Math.max(1, (count + 1) / 2), spread, spread * 0.65D, spread, 0.035D);
    }
}
