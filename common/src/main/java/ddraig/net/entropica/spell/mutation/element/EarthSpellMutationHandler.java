package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.config.EntropicaConfig;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
 *     <li><b>Projectile</b>: <i>Quake Spire</i> — travels along and beneath terrain as a rumbling fault line (snapping to
 *     surfaces up to 20 blocks below and terminating cleanly at deep chasm ledges), erupting a temporary 4.0s stone spike
 *     with 2.0–2.5 armor shred and upward launch.</li>
 *     <li><b>Beam</b>: <i>Magnetite Tractor Beam</i> — emits an inward-pulling magnetic vibrational beam that reels in
 *     metallic items, dropped raw/metallic ores, and metal-armored mobs/players steadily toward the caster (and mines
 *     config-allowed exposed ore veins at Tier 2+ every 0.75s).</li>
 *     <li><b>Radial Burst</b>: <i>Stone Ring Rampart</i> — sweeps adjacent foes outside a 4.45m anti-clip perimeter and
 *     raises a circular ring of 14.0s stone barrier pillars around the caster or projectile carrier impact center.</li>
 *     <li><b>Touch / Strike</b>: <i>Seismic Impact</i> — converts the melee strike into pure Earth Materia damage (+1 vs Void),
 *     shredding armor points and knocking the primary struck target flat onto the ground.</li>
 *     <li><b>Aura / Self</b>: <i>Earth Defensive Attunement</i> — registers defending element as Earth: +1 advantage resistance
 *     vs incoming VOID attacks (50% damage), -1 vulnerability vs incoming AIR attacks (150% damage), +2 Armor Toughness,
 *     and 50% knockback resistance (including Sneak-Anchored field support).</li>
 *     <li><b>Glyph / Trap</b>: <i>Tectonic Impalement Spike</i> — 1.0s arm, 10m (or Tier 5+ indefinite) circular disarmable trap;
 *     erupts a 2-block sharp stone stalagmite directly beneath entering foes, shredding 4.0 armor and launching them vertically.</li>
 *     <li><b>Cone / Spray</b>: <i>Sand &amp; Gravel Flurry</i> — blasts an exact 60-degree cone of high-velocity stone grit and gravel
 *     dealing Earth Materia damage, blinding for 1.5s, pushing foes 6 blocks back, and excavating loose dirt, sand, and gravel.</li>
 *     <li><b>Rain / Skyfall</b>: <i>Meteorite Shrapnel Barrage</i> — rains dense stone meteor fragments from an overhead cavern rift
 *     for 6.0s–13.2s across Tiers 1–10 alongside a circular tier-scaled radius, staggering grounded foes and slamming airborne mobs.</li>
 *     <li><b>Summon / Eidolon</b>: <i>Tectonic Eidolon</i> — earthen brown/orange (#773214) reinforced triad: ground-punching Agent,
 *     anchored plinth stone-bolt Sentry, and immense-HP melee-taunting Granite Effigy Decoy that detonates into stone shrapnel.</li>
 *     <li><b>Flow (Slow)</b>: <i>Dense Tectonic Mass</i> — SLOW_HEAVY-exclusive mass boost granting +0.4 Materia Advantage score
 *     and a guaranteed armor-shredding impact.</li>
 *     <li><b>Blink / Step</b>: <i>Burrow Step</i> — phases cleanly across all tiers with a +25% range boost when burrowing through
 *     or emerging onto stone and soil surfaces.</li>
 *     <li><b>Transmute</b>: <i>Lithic Compaction</i> — compacts Cobblestone/Mossy Cobblestone into Stone, Stone/Cobbled Deepslate
 *     into Deepslate, placed Sand/Gravel/Suspicious Sand/Suspicious Gravel into Sandstone, Red Sand into Red Sandstone, and
 *     dropped Sand/Gravel/Red Sand at a 4:1 ratio into Sandstone/Red Sandstone.</li>
 *     <li><b>Affliction</b>: <i>Sundering Hex</i> — persistent anti-milk petrifying curse (cleansed by Restoration magic or
 *     caster defeat) shredding 5.0 / 7.5 / 10.0 physical armor across Levels I–III and doubling incoming Earth Materia damage
 *     with 0 continuous tick damage.</li>
 * </ul>
 */
public final class EarthSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int TECTONIC_EIDOLON_HEX_COLOR = 0x773214;
    private static final int AMBER_FAULT_HEX_COLOR = 0xFFC278;

    /** Exact 60-degree cone half-angle (30 deg): cos(30 deg) = sqrt(3)/2 = 0.8660254. */
    private static final double CONE_HALF_ANGLE_COS = 0.8660254D;
    /** Exact 60-degree cone half-angle tangent: tan(30 deg) = 0.57735027. */
    private static final double CONE_HALF_ANGLE_TAN = 0.57735027D;

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
            default -> null;
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
            case PROJECTILE -> "Quake Spire: Travels along and beneath terrain as a rumbling fault line (terminating at deep chasms), erupting a temporary 4s stone spike beneath foes with 2.0–2.5 armor shred and upward launch.";
            case BEAM -> "Magnetite Tractor Beam: Emits a magnetic vibrational beam that pulls metallic items, raw/metallic ores, and metal-armored foes toward the caster while mining allowed exposed ores at Tier 2+.";
            case RADIAL_BURST -> "Stone Ring Rampart: Sweeps nearby foes outward and raises a circular ring of 14s stone barrier pillars around the caster or projectile impact center.";
            case TOUCH_STRIKE -> "Seismic Impact: Converts the melee strike into pure Earth Materia damage (+1 vs Void), shredding armor points and knocking the primary struck target flat.";
            case AURA -> "Earth Defensive Attunement: Defends as Earth (+1 vs Void halving damage to 50%, -1 vs Air taking 150% damage) while granting +2 Armor Toughness and 50% knockback resistance.";
            case GLYPH -> "Tectonic Impalement Spike: Circular proximity trap that shoots a 2-block sharp stone stalagmite up beneath entering foes, shredding 4 armor and launching them vertically with Earth Materia damage.";
            case CONE -> "Sand & Gravel Flurry: Blasts an exact 60-degree cone of high-velocity stone grit and gravel dealing Earth Materia damage, blinding for 1.5s, pushing foes 6 blocks back, and excavating loose dirt, sand, and gravel.";
            case SKYFALL -> "Meteorite Shrapnel Barrage: Rains dense stone meteor fragments for 6s–13.2s across a tier-scaled circular zone, battering foes with Earth Materia damage (+1 vs Void), staggering targets, and knocking down airborne mobs.";
            case SUMMON_AGENT -> "Tectonic Eidolon: Earthen brown/orange (#773214) reinforced bruiser (+25% HP, +8 Armor, +2 Toughness) delivering heavy ground-punching Earth Materia slams that shred armor.";
            case SUMMON_SENTRY -> "Tectonic Plinth Sentry: Anchored earthen (#773214) reinforced bastion plinth (+25% HP, +8 Armor, +2 Toughness) firing armor-shredding stone projectile bolts.";
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
            case BLINK -> "Burrow Step: Phases cleanly across all tiers with a +25% range boost when burrowing through or emerging onto stone and soil surfaces.";
            case TRANSMUTE, BREAK -> "Lithic Compaction: Compacts Cobblestone/Mossy Cobblestone into Stone, Stone/Cobbled Deepslate into Deepslate, and Sand/Gravel into Sandstone (4:1 on dropped items).";
            case AFFLICTION -> "Sundering Hex: Persistent anti-milk petrifying curse (cleansed by Restoration magic or caster defeat) shredding 5 / 7.5 / 10 physical armor across Levels I–III and doubling incoming Earth Materia damage (0 tick damage).";
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
        if (spell.flowPart().flow() != SpellFlowModifier.SLOW_HEAVY || !spell.flowPart().mutationEnabled()) {
            return;
        }
        if (enforcesZeroEntityHarm(spell)) {
            return;
        }
        if (!(hitEntity instanceof LivingEntity living) || !living.isAlive()) {
            return;
        }
        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard())) {
            return;
        }
        // Dense Tectonic Mass: guaranteed armor-shredding impact on hit (no extra standalone damage hit)!
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
        if (slot != null && slot.formPart().form() != SpellDeliveryForm.PROJECTILE) {
            return;
        }

        // Quake Spire: travels along/beneath terrain as a fault line, snapping to surfaces up to 20 blocks below
        // and terminating cleanly at deep chasm ledges rather than flying freely through open mid-air!
        BlockPos curPos = projectile.blockPosition();
        BlockPos groundBlock = null;
        for (int dy = -1; dy <= 20; dy++) {
            BlockPos check = curPos.below(dy);
            BlockState bs = level.getBlockState(check);
            if (!bs.getCollisionShape(level, check).isEmpty()
                    && level.getBlockState(check.above()).getCollisionShape(level, check.above()).isEmpty()) {
                groundBlock = check;
                break;
            }
        }

        if (groundBlock == null) {
            // Over a deep chasm with no floor within 20 blocks: terminate cleanly at the ledge!
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.POINTED_DRIPSTONE.defaultBlockState()),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 10, 0.22D, 0.18D, 0.22D, 0.06D);
            level.playSound(null, curPos, SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 0.65F, 0.78F);
            projectile.discard();
            return;
        }

        double surfaceY = groundBlock.getY() + 1.0D;
        double desiredY = surfaceY + 0.18D;
        double blendedY = Math.abs(projectile.getY() - desiredY) > 0.65D
                ? desiredY
                : projectile.getY() + (desiredY - projectile.getY()) * 0.65D;

        // Preserve horizontal fault-line travel speed and step smoothly over 1-block soil/stone rises ahead
        Vec3 vel = projectile.getDeltaMovement();
        Vec3 horiz = new Vec3(vel.x, 0.0D, vel.z);
        if (horiz.lengthSqr() > 1.0E-4D) {
            double preservedSpeed = Math.max(horiz.length(), vel.length() * 0.95D);
            Vec3 horizNorm = horiz.normalize();
            Vec3 lookStep = horizNorm.scale(0.85D);
            BlockPos aheadFoot = BlockPos.containing(projectile.getX() + lookStep.x, surfaceY + 0.15D, projectile.getZ() + lookStep.z);
            if (!level.getBlockState(aheadFoot).getCollisionShape(level, aheadFoot).isEmpty()
                    && level.getBlockState(aheadFoot.above()).getCollisionShape(level, aheadFoot.above()).isEmpty()) {
                blendedY = Math.max(blendedY, aheadFoot.getY() + 1.18D);
            }
            Vec3 faultVel = horizNorm.scale(preservedSpeed);
            projectile.setDeltaMovement(faultVel.x, 0.0D, faultVel.z);
            projectile.hasImpulse = true;
        } else if (vel.y < -0.02D) {
            projectile.setDeltaMovement(vel.x, 0.0D, vel.z);
        }

        projectile.setPos(projectile.getX(), blendedY, projectile.getZ());

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
        eruptTemporaryStoneSpike(level, caster, living.blockPosition(), spell, slot, 80);

        if (!enforcesZeroEntityHarm(spell)) {
            boolean denseMass = isDenseTectonicMassActive(spell);
            if (!hasHarmEffect(spell, slot)) {
                float spikeDmg = Math.max(3.5F + tier * 0.90F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.55F);
                SpellCombatReactionEngine.hurtWithEarthMateriaAdvantage(level, living, caster, spikeDmg, denseMass, true);
            }
            shredArmor(level, living, 2.5D, 150L);

            if (!SpellCombatReactionEngine.isBossEntity(level, living)) {
                Vec3 v = living.getDeltaMovement();
                SpellExecutionEngine.applySyncedMotion(living, new Vec3(v.x * 0.35D, 0.98D, v.z * 0.35D));
            }
        }

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
        // Ground-impact Quake Spire erupts a temporary 4.0s (80t) stone stalagmite that launches adjacent enemies upward!
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

        if (enforcesZeroEntityHarm(spell)) {
            return;
        }

        boolean dealStandaloneSpikeDamage = !hasHarmEffect(spell, slot);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos, pos).inflate(2.25D))) {
            if (!foe.isAlive() || foe == caster) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) {
                continue;
            }
            if (dealStandaloneSpikeDamage) {
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
        // Magnetite Tractor Beam: pulls metallic items, dropped raw/metallic ores, and metal-armored mobs/players steadily toward the caster!
        int tier = spell.getEffectiveTier();
        long now = level.getGameTime();
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 eye = caster.getEyePosition();
        Vec3 pullTarget = caster.position().add(0.0D, 0.75D, 0.0D);
        double beamLen = Math.min(maxReach, blockHit.getLocation().distanceTo(eye) + 0.75D);

        // Tier 2+ exposed ore-vein fracture every 0.75s (15t), gated by EntropicaConfig whitelist/blacklist!
        if (tier >= 2 && now % 15L == 0L) {
            BlockPos hitPos = blockHit.getBlockPos();
            if (hitPos.distToCenterSqr(eye) <= (maxReach + 1.0D) * (maxReach + 1.0D)) {
                BlockState hitState = level.getBlockState(hitPos);
                float destroySpeed = hitState.getDestroySpeed(level, hitPos);
                if (destroySpeed >= 0.0F && destroySpeed < 45.0F
                        && EntropicaConfig.isOreAllowedForMagnetiteBeam(hitState)
                        && !SpellExecutionEngine.isProtectedBlockOrContainer(level, hitPos, hitState)) {
                    level.destroyBlock(hitPos, true, caster);
                    spawnFaultCrystalSparks(level, Vec3.atCenterOf(hitPos), 10, 0.35D);
                    level.playSound(null, hitPos, SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 0.80F, 1.15F);
                }
            }
        }

        AABB corridor = caster.getBoundingBox().expandTowards(normDir.scale(beamLen)).inflate(2.35D);

        // 1. Steadily reel in strictly metal-armored mobs/players (Iron Golems or entities wearing metallic armor)
        if (!enforcesZeroEntityHarm(spell)) {
            Set<UUID> pulledMobs = new HashSet<>();
            for (LivingEntity hit : hitEntities) {
                if (!hit.isAlive() || hit == caster) continue;
                if (!isMetalArmoredMob(hit)) continue;
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                    continue;
                }
                pulledMobs.add(hit.getUUID());
                reelMetalArmoredMobTowardCaster(level, caster, hit);
            }

            for (LivingEntity cand : level.getEntitiesOfClass(LivingEntity.class, corridor)) {
                if (!cand.isAlive() || cand == caster || pulledMobs.contains(cand.getUUID())) continue;
                if (!isMetalArmoredMob(cand)) continue;
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(cand, caster, spell.hasWardFriendlyFireSafeguard())) {
                    continue;
                }
                if (distanceToRaySegment(cand.getBoundingBox().getCenter(), eye, normDir, beamLen) <= 2.15D) {
                    pulledMobs.add(cand.getUUID());
                    reelMetalArmoredMobTowardCaster(level, caster, cand);
                }
            }
        }

        // 2. Steadily reel in metallic items & dropped raw/metallic ores along the magnetic beam corridor
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
        // Seismic Impact: SpellCombatReactionEngine converts the melee strike to pure Earth Materia;
        // applies armor shred, 1.9s Slowness/Inertial Anchor, and knockdown strictly to the primary struck target!
        int tier = spell.getEffectiveTier();
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();

        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_CRESCENT_SLASH,
                caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D), normDir,
                EssenceType.EARTH, null, 3.2F, 48, tier
        );

        if (enforcesZeroEntityHarm(spell) || hitEntities.isEmpty()) {
            return;
        }

        LivingEntity primary = hitEntities.get(0);
        if (primary == null || !primary.isAlive() || primary == caster
                || SpellCombatReactionEngine.isProtectedFromOffensiveSpell(primary, caster, spell.hasWardFriendlyFireSafeguard())) {
            return;
        }

        long now = level.getGameTime();
        shredArmor(level, primary, 4.0D + tier * 0.5D, 200L);

        // Knock the primary target flat onto the ground (1.9s = 38 ticks)!
        primary.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 38, 5, false, true));
        if (!SpellCombatReactionEngine.isBossEntity(level, primary)) {
            primary.addEffect(new MobEffectInstance(
                    BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 38, 0, false, false));
            EntityHelper.getPersistentData(primary).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 38L);
            SpellExecutionEngine.applySyncedMotion(primary, new Vec3(normDir.x * 0.62D, -0.58D, normDir.z * 0.62D));
        }

        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                primary.position().add(0.0D, 0.05D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
                EssenceType.EARTH, null, 1.45F, 54, tier
        );
        spawnFaultCrystalSparks(level, primary.position().add(0.0D, 0.45D, 0.0D), 12, 0.35D);
        level.playSound(null, caster.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.60F, 0.74F);
        level.playSound(null, caster.blockPosition(), SoundEvents.DEEPSLATE_BREAK, SoundSource.PLAYERS, 0.90F, 0.78F);
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
        // Stone Ring Rampart: raises a circular ring of 14.0s stone barrier pillars around caster (self-cast) or impact center (carrier)!
        int tier = spell.getEffectiveTier();
        double bunkerRadius = 3.25D;
        int rampartDuration = 280; // 14.0s bunker lifetime

        // Short 4.45m circular anti-clip outward push before raising pillars (respecting Ward & enforcesZeroEntityHarm)
        if (!enforcesZeroEntityHarm(spell)) {
            double pushRadius = bunkerRadius + 1.2D; // 4.45m circular perimeter
            double pushRadiusSq = pushRadius * pushRadius;
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
                if (horiz.lengthSqr() <= pushRadiusSq && !SpellCombatReactionEngine.isBossEntity(level, foe)) {
                    Vec3 out = (horiz.lengthSqr() > 1.0E-4D ? horiz.normalize() : new Vec3(1.0D, 0.0D, 0.0D)).scale(1.25D);
                    SpellExecutionEngine.applySyncedMotion(foe, new Vec3(out.x, 0.34D, out.z));
                }
            }
        }

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
        // Circular horizontal trigger radius; shoots a 2-block sharp stone stalagmite directly beneath entering foes!
        int tier = spell.getEffectiveTier();
        double senseRadius = Math.max(2.6D, Math.min(4.5D, radius));
        int glyphLifetime = tier >= 5 ? (Integer.MAX_VALUE / 4) : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(level, glyphCenter, senseRadius, EssenceType.EARTH, null, 220, tier);
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
                    double triggerRadius = zone.radius;
                    double triggerRadSq = triggerRadius * triggerRadius;
                    AABB triggerBox = new AABB(
                            zone.center.x - triggerRadius, zone.center.y - 1.0D, zone.center.z - triggerRadius,
                            zone.center.x + triggerRadius, zone.center.y + 2.5D, zone.center.z + triggerRadius
                    );
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e -> {
                        double dx = e.getX() - zone.center.x;
                        double dz = e.getZ() - zone.center.z;
                        if (dx * dx + dz * dz > triggerRadSq) return false;
                        return isValidHostileOrPvpTarget(e, owner, wardSafe);
                    });
                    if (stepped.isEmpty()) return true;

                    boolean zeroHarm = enforcesZeroEntityHarm(zone.spell);
                    boolean dealStandaloneImpaleDamage = !zeroHarm && !hasHarmEffect(zone.spell, zone.slot);
                    boolean denseMass = zone.spell != null && isDenseTectonicMassActive(zone.spell);
                    float impaleDmg = Math.max(5.5F + tier * 1.35F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.75F)
                            * Math.max(0.5F, zone.apparatusPotency);

                    Set<BlockPos> eruptedBases = new HashSet<>();
                    for (LivingEntity victim : stepped) {
                        BlockPos victimSpikeBase = findSpikeBaseBeneathEntity(srv, victim);
                        Vec3 victimEruptPos = new Vec3(victim.getX(), victimSpikeBase.getY() + 0.05D, victim.getZ());

                        if (eruptedBases.add(victimSpikeBase)) {
                            eruptTemporaryStoneColumn(srv, owner, victimSpikeBase, zone.spell, zone.slot, 88);
                            SpellWorldVfxManager.broadcast(
                                    srv, SpellVfxEventPayload.VFX_PROJECTILE_IMPACT,
                                    victimEruptPos, new Vec3(0.0D, 1.0D, 0.0D),
                                    EssenceType.EARTH, null, 2.45F, 88, tier
                            );
                            spawnFaultCrystalSparks(srv, victimEruptPos.add(0.0D, 0.85D, 0.0D), 24, 0.55D);
                            srv.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.POINTED_DRIPSTONE.defaultBlockState()),
                                    victimEruptPos.x, victimEruptPos.y + 0.75D, victimEruptPos.z, 28, 0.45D, 0.85D, 0.45D, 0.12D);
                            srv.playSound(null, victimSpikeBase, SoundEvents.DRIPSTONE_BLOCK_BREAK, SoundSource.PLAYERS, 1.15F, 0.65F);
                            srv.playSound(null, victimSpikeBase, SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.55F, 0.68F);
                        }

                        if (!zeroHarm) {
                            if (dealStandaloneImpaleDamage) {
                                SpellCombatReactionEngine.hurtWithEarthMateriaAdvantage(srv, victim, owner, impaleDmg, denseMass, true);
                            }
                            shredArmor(srv, victim, 4.0D, 180L);
                            if (!SpellCombatReactionEngine.isBossEntity(srv, victim)) {
                                Vec3 v = victim.getDeltaMovement();
                                SpellExecutionEngine.applySyncedMotion(victim, new Vec3(v.x * 0.25D, 1.28D, v.z * 0.25D));
                            }
                        }

                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, victim, new Vec3(0.0D, 1.0D, 0.0D),
                                    zone.spell, zone.slot, EssenceType.EARTH, zone.apparatusPotency
                            );
                        }
                    }

                    SpellWorldVfxManager.broadcastArcaneSeal(srv, zone.center, triggerRadius, EssenceType.EARTH, null, 72, tier);
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
        // Sand & Gravel Flurry: blasts an exact 60-degree cone of high-velocity stone grit and gravel dealing Earth Materia damage,
        // blinding for 1.5s (30t), pushing foes 6 blocks back, and excavating loose dirt, sand, and gravel blocks!
        int tier = spell.getEffectiveTier();
        boolean denseMass = isDenseTectonicMassActive(spell);
        double reach = Math.max(8.0D, coneRange);
        Vec3 normDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle().normalize();
        Vec3 eye = caster.getEyePosition();

        SpellWorldVfxManager.broadcast(
                level, SpellVfxEventPayload.VFX_VORTEX_CONE,
                eye.subtract(0.0D, 0.20D, 0.0D), normDir,
                EssenceType.EARTH, null, (float) reach, 54, tier
        );

        if (!enforcesZeroEntityHarm(spell)) {
            boolean dealStandaloneGritDamage = !hasHarmEffect(spell, slot);
            float gritDmg = Math.max(3.8F + tier * 0.95F, MateriaStageTable.getBaseCombatDamageHp(tier) * 0.55F)
                    * Math.max(0.5F, apparatusPotency);

            // Filter strictly to an exact 60-degree cone (half-angle 30 deg -> dot >= 0.8660254D)
            Set<UUID> blasted = new HashSet<>();
            for (LivingEntity hit : hitTargets) {
                if (hit == null || !hit.isAlive() || hit == caster) continue;
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                    continue;
                }
                if (!isInsideSixtyDegreeCone(eye, normDir, reach, hit)) {
                    continue;
                }
                blasted.add(hit.getUUID());
                applyFlurryBlastToTarget(level, caster, hit, normDir, gritDmg, denseMass, dealStandaloneGritDamage);
            }

            double maxConeWidth = reach * CONE_HALF_ANGLE_TAN + 1.25D;
            AABB coneBox = new AABB(eye, eye.add(normDir.scale(reach))).inflate(maxConeWidth);
            for (LivingEntity cand : level.getEntitiesOfClass(LivingEntity.class, coneBox, LivingEntity::isAlive)) {
                if (cand == caster || blasted.contains(cand.getUUID())) continue;
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(cand, caster, spell.hasWardFriendlyFireSafeguard())) {
                    continue;
                }
                if (isInsideSixtyDegreeCone(eye, normDir, reach, cand)) {
                    blasted.add(cand.getUUID());
                    applyFlurryBlastToTarget(level, caster, cand, normDir, gritDmg, denseMass, dealStandaloneGritDamage);
                }
            }
        }

        // Excavate strictly loose Dirt, Sand, and Gravel blocks inside the exact 60-degree cone
        int maxSteps = Math.min(9, (int) Math.ceil(reach));
        int brokenBlocks = 0;
        for (int s = 1; s <= maxSteps && brokenBlocks < 48; s++) {
            Vec3 sliceCenter = eye.add(normDir.scale(s));
            int halfSpan = Math.max(1, (int) Math.ceil(s * CONE_HALF_ANGLE_TAN));
            BlockPos c = BlockPos.containing(sliceCenter);
            for (BlockPos p : BlockPos.betweenClosed(c.offset(-halfSpan, -1, -halfSpan), c.offset(halfSpan, 1, halfSpan))) {
                if (brokenBlocks >= 48) break;
                Vec3 toBlock = Vec3.atCenterOf(p).subtract(eye);
                double bDist = toBlock.length();
                if (bDist > reach || (bDist > 1.2D && toBlock.scale(1.0D / bDist).dot(normDir) < CONE_HALF_ANGLE_COS)) {
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
        // Meteorite Shrapnel Barrage: starts at 6.0s (120t) at Tier 1 and scales upward across Tiers 1–10
        // (120 + (tier - 1) * 16 -> 6.0s at T1 up to 13.2s at T10) alongside circular tier-scaled radius!
        int tier = Math.clamp(spell.getEffectiveTier(), 1, 10);
        double r = resolveMeteoriteBarrageRadius(radius, tier);
        int barrageDuration = resolveMeteoriteBarrageDurationTicks(tier);

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
                        boolean zeroHarm = enforcesZeroEntityHarm(zone.spell);
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

                        double radSq = zone.radius * zone.radius;
                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 8.5D, zone.center.z + zone.radius
                        );
                        long now = srv.getGameTime();
                        for (LivingEntity victim : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            double dx = victim.getX() - zone.center.x;
                            double dz = victim.getZ() - zone.center.z;
                            if (dx * dx + dz * dz > radSq) continue;
                            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(victim, owner, wardSafe)) {
                                continue;
                            }

                            if (!zeroHarm) {
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
        // Burrow Step: phases cleanly into stone & soil and erupts safely at arrival!
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
        // Lithic Compaction: Cobblestone/Mossy Cobblestone -> Stone, Stone/Cobbled Deepslate -> Deepslate,
        // Sand/Gravel/Suspicious Sand/Suspicious Gravel -> Sandstone, Red Sand -> Red Sandstone!
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
        if (stack.isEmpty()) {
            return false;
        }
        int count = stack.getCount();

        if (stack.is(Items.COBBLESTONE) || stack.is(Items.MOSSY_COBBLESTONE)) {
            itemEntity.setItem(new ItemStack(Items.STONE, count));
            emitDroppedCompactionFeedback(level, itemEntity);
            return true;
        }
        if (stack.is(Items.STONE) || stack.is(Items.COBBLED_DEEPSLATE)) {
            itemEntity.setItem(new ItemStack(Items.DEEPSLATE, count));
            emitDroppedCompactionFeedback(level, itemEntity);
            return true;
        }

        // Sand / Gravel -> Sandstone and Red Sand -> Red Sandstone: 4 items per 1 Sandstone block, leaving < 4 remainder untouched!
        Item sandstoneOut = null;
        if (stack.is(Items.SAND) || stack.is(Items.GRAVEL)
                || stack.is(Items.SUSPICIOUS_SAND) || stack.is(Items.SUSPICIOUS_GRAVEL)) {
            sandstoneOut = Items.SANDSTONE;
        } else if (stack.is(Items.RED_SAND)) {
            sandstoneOut = Items.RED_SANDSTONE;
        }

        if (sandstoneOut != null) {
            Item inputItem = stack.getItem();
            if (count >= 4) {
                int blocks = count / 4;
                int remainder = count % 4;
                itemEntity.setItem(new ItemStack(sandstoneOut, blocks));
                if (remainder > 0) {
                    ItemEntity remEntity = new ItemEntity(
                            level, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(),
                            new ItemStack(inputItem, remainder)
                    );
                    remEntity.setDefaultPickUpDelay();
                    level.addFreshEntity(remEntity);
                }
                emitDroppedCompactionFeedback(level, itemEntity);
                return true;
            }

            // Pool unmerged adjacent stacks of the exact same loose sediment within 1.25 blocks if total >= 4;
            // otherwise leave < 4 remainder completely untouched!
            List<ItemEntity> nearbySame = level.getEntitiesOfClass(
                    ItemEntity.class,
                    itemEntity.getBoundingBox().inflate(1.25D),
                    other -> other != itemEntity && other.isAlive() && !other.getItem().isEmpty() && other.getItem().is(inputItem)
            );
            int totalAvailable = count;
            for (ItemEntity other : nearbySame) {
                totalAvailable += other.getItem().getCount();
            }
            if (totalAvailable < 4) {
                return false;
            }

            int blocks = totalAvailable / 4;
            int toConsumeFromOthers = (blocks * 4) - count;
            for (ItemEntity other : nearbySame) {
                if (toConsumeFromOthers <= 0) break;
                ItemStack otherStack = other.getItem();
                int otherCount = otherStack.getCount();
                if (otherCount <= toConsumeFromOthers) {
                    toConsumeFromOthers -= otherCount;
                    other.discard();
                } else {
                    other.setItem(new ItemStack(inputItem, otherCount - toConsumeFromOthers));
                    toConsumeFromOthers = 0;
                }
            }
            itemEntity.setItem(new ItemStack(sandstoneOut, blocks));
            emitDroppedCompactionFeedback(level, itemEntity);
            return true;
        }

        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Sundering Hex: persistent anti-milk petrifying curse (cleansed by Restoration magic or caster defeat);
        // deals 0 continuous HP tick damage, shreds 5.0 / 7.5 / 10.0 physical armor across Levels I–III,
        // and doubles all incoming Earth Materia damage!
        int stack = Math.clamp(SpellCombatReactionEngine.getHexStackLevel(target), 1, 3);
        double armorShred = resolveSunderingHexArmorShred(stack);
        SpellCombatReactionEngine.applyTimedAttributeShred(target, level, armorShred, 0.0D, 50L);

        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                target.getX(), target.getY(0.60D), target.getZ(), 5, 0.25D, 0.30D, 0.25D, 0.04D);
        spawnFaultCrystalSparks(level, target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D), 4, 0.25D);
        return true;
    }

    // =========================================================================
    // PUBLIC & INTERNAL TECTONIC / MAGNETITE HELPERS
    // =========================================================================

    public static int resolveMeteoriteBarrageDurationTicks(int potencyTier) {
        int tier = Math.clamp(potencyTier, 1, 10);
        return 120 + (tier - 1) * 16; // 6.0s (120t) at Tier 1 up to 13.2s (264t) at Tier 10
    }

    public static double resolveMeteoriteBarrageRadius(double baseRadius, int potencyTier) {
        int tier = Math.clamp(potencyTier, 1, 10);
        return Math.max(baseRadius, 5.5D + (tier - 1) * 0.75D);
    }

    public static double resolveSunderingHexArmorShred(int stackLevel) {
        return switch (Math.clamp(stackLevel, 1, 3)) {
            case 2 -> 7.5D;
            case 3 -> 10.0D;
            default -> 5.0D;
        };
    }

    private static boolean enforcesZeroEntityHarm(@Nullable SpellAssembly spell) {
        return spell != null && spell.prefix().enforcesZeroEntityHarm();
    }

    private static boolean hasHarmEffect(@Nullable SpellAssembly spell, @Nullable SubSpellSlot slot) {
        if (slot != null && slot.hasHarmVerb()) {
            return true;
        }
        if (spell != null) {
            for (SubSpellSlot s : spell.subSpells()) {
                if (s.hasHarmVerb()) {
                    return true;
                }
            }
        }
        return false;
    }

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

    private static boolean isInsideSixtyDegreeCone(Vec3 origin, Vec3 normDir, double reach, LivingEntity candidate) {
        double reachSq = reach * reach;
        return isOffsetInsideSixtyDegreeCone(candidate.getBoundingBox().getCenter().subtract(origin), normDir, reachSq)
                || isOffsetInsideSixtyDegreeCone(candidate.getEyePosition().subtract(origin), normDir, reachSq)
                || isOffsetInsideSixtyDegreeCone(candidate.position().subtract(origin), normDir, reachSq);
    }

    private static boolean isOffsetInsideSixtyDegreeCone(Vec3 offset, Vec3 normDir, double reachSq) {
        double lenSq = offset.lengthSqr();
        if (lenSq <= 1.0E-6D || lenSq > reachSq) {
            return false;
        }
        return offset.scale(1.0D / Math.sqrt(lenSq)).dot(normDir) >= CONE_HALF_ANGLE_COS;
    }

    private static void applyFlurryBlastToTarget(
            ServerLevel level,
            LivingEntity caster,
            LivingEntity target,
            Vec3 normDir,
            float gritDmg,
            boolean denseMass,
            boolean dealStandaloneGritDamage
    ) {
        if (dealStandaloneGritDamage) {
            SpellCombatReactionEngine.hurtWithEarthMateriaAdvantage(level, target, caster, gritDmg, denseMass, true);
        }
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0, false, true));
        if (!SpellCombatReactionEngine.isBossEntity(level, target)) {
            // Push target ~6 blocks backward with high-velocity stone grit impulse!
            SpellExecutionEngine.applySyncedMotion(target, new Vec3(normDir.x * 1.62D, 0.36D, normDir.z * 1.62D));
        }
    }

    private static void reelMetalArmoredMobTowardCaster(ServerLevel level, LivingEntity caster, LivingEntity mob) {
        if (SpellCombatReactionEngine.isBossEntity(level, mob)) {
            return;
        }
        Vec3 toCaster = caster.position().subtract(mob.position());
        double dist = toCaster.length();
        if (dist < 1.35D) {
            return;
        }
        double pullStrength = Math.min(0.95D, 0.44D + Math.max(2, mob.getArmorValue()) * 0.03D);
        Vec3 pull = toCaster.scale(pullStrength / dist);
        SpellExecutionEngine.applySyncedMotion(mob, new Vec3(pull.x, Math.max(0.10D, mob.getDeltaMovement().y * 0.4D + 0.12D), pull.z));
        if (level.getGameTime() % 5L == 0L) {
            spawnFaultCrystalSparks(level, mob.position().add(0.0D, mob.getBbHeight() * 0.55D, 0.0D), 3, 0.24D);
        }
    }

    /**
     * Identifies strictly metal-armored mobs/players for {@code Magnetite Tractor Beam}:
     * {@link IronGolem} (or metallic construct golems) OR entities wearing metallic armor
     * (Iron, Chainmail, Gold, Copper, Netherite, Entropica metals, or modded metallic armor).
     * Excludes natural biological {@code armorValue > 0}, leather, turtle scute, wolf scute, and diamond armor.
     */
    public static boolean isMetalArmoredMob(@Nullable LivingEntity entity) {
        if (entity == null || !entity.isAlive()) {
            return false;
        }
        if (entity instanceof IronGolem) {
            return true;
        }
        ResourceLocation entityKey = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (entityKey != null) {
            String entPath = entityKey.getPath().toLowerCase(Locale.ROOT);
            if ("iron_golem".equals(entPath) || "copper_golem".equals(entPath) || "steel_golem".equals(entPath)) {
                return true;
            }
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
                continue;
            }
            ItemStack eq = entity.getItemBySlot(slot);
            if (!eq.isEmpty() && isMetallicArmorItem(eq)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} strictly for worn metallic armor pieces (Iron, Chainmail, Gold, Copper, Netherite,
     * canonical Entropica metals, and modded metallic armor), excluding leather, turtle scute, armadillo/wolf armor,
     * diamond, emerald, bone, wood, stone, elytra, and cosmetic heads/pumpkins.
     */
    public static boolean isMetallicArmorItem(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().toLowerCase(Locale.ROOT);
        if (isExcludedNonMetallicEquipmentPath(path)
                || path.contains("elytra")
                || path.contains("pumpkin")
                || path.endsWith("_head")
                || path.endsWith("_skull")
                || path.contains("banner")) {
            return false;
        }

        boolean taggedArmor = stack.is(ItemTags.HEAD_ARMOR)
                || stack.is(ItemTags.CHEST_ARMOR)
                || stack.is(ItemTags.LEG_ARMOR)
                || stack.is(ItemTags.FOOT_ARMOR);
        boolean armorNameShape = path.endsWith("_helmet")
                || path.endsWith("_chestplate")
                || path.endsWith("_leggings")
                || path.endsWith("_boots")
                || path.endsWith("_horse_armor")
                || path.endsWith("_body_armor")
                || path.endsWith("_cap")
                || path.endsWith("_tunic")
                || path.endsWith("_greaves")
                || path.endsWith("_cuirass")
                || path.endsWith("_sabaton")
                || path.endsWith("_sabatons");

        if (!taggedArmor && !armorNameShape) {
            return false;
        }
        return hasMetallicMaterialToken(path) || taggedArmor;
    }

    /**
     * Identifies metallic items, metallic tools/weapons/armor, dropped raw/metallic ores, and all canonical
     * Entropica metals (Arcanite, Athanorite, Caputite, Eidolite, Euclidite, Mercurite, Resonite, Sanguinite, Viscanite, Entropic Ore),
     * while excluding wooden, stone, leather, turtle, diamond, and non-metallic mineral items/ores.
     */
    public static boolean isMetallicOrOreItem(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().toLowerCase(Locale.ROOT);

        if (isExcludedNonMetallicEquipmentPath(path)
                || path.equals("golden_apple")
                || path.equals("enchanted_golden_apple")
                || path.equals("golden_carrot")
                || path.equals("glistering_melon_slice")) {
            return false;
        }

        if (isMetallicArmorItem(stack)) {
            return true;
        }

        boolean toolOrWeapon = stack.is(ItemTags.SWORDS)
                || stack.is(ItemTags.PICKAXES)
                || stack.is(ItemTags.AXES)
                || stack.is(ItemTags.SHOVELS)
                || stack.is(ItemTags.HOES)
                || path.endsWith("_sword")
                || path.endsWith("_pickaxe")
                || path.endsWith("_axe")
                || path.endsWith("_shovel")
                || path.endsWith("_hoe");
        if (toolOrWeapon) {
            return true;
        }

        if ((path.endsWith("_ore") || path.startsWith("raw_")) && !isNonMetallicMineralPath(path)) {
            return true;
        }

        return hasMetallicMaterialToken(path)
                || path.contains("ingot")
                || path.contains("nugget")
                || path.contains("entropic_ore")
                || path.contains("ancient_debris")
                || path.contains("gilded_blackstone")
                || path.contains("anvil")
                || path.contains("bucket")
                || path.contains("minecart")
                || path.contains("hopper")
                || path.contains("cauldron")
                || path.contains("rail")
                || path.equals("compass")
                || path.equals("recovery_compass")
                || path.equals("clock")
                || path.contains("shears")
                || path.contains("shield")
                || path.contains("trident")
                || path.contains("mace")
                || path.equals("heavy_core")
                || path.contains("lightning_rod")
                || path.equals("bell")
                || path.contains("lodestone")
                || path.contains("magnetite")
                || path.equals("chain");
    }

    private static boolean isExcludedNonMetallicEquipmentPath(String path) {
        return path.startsWith("wooden_")
                || path.startsWith("wood_")
                || path.startsWith("stone_")
                || path.startsWith("leather_")
                || path.contains("turtle")
                || path.contains("scute")
                || path.contains("armadillo")
                || path.equals("wolf_armor")
                || path.contains("diamond")
                || path.contains("emerald")
                || path.contains("bone")
                || path.contains("chitin")
                || path.contains("wool")
                || path.contains("cloth")
                || path.contains("fabric")
                || path.contains("silk")
                || path.contains("robe")
                || path.contains("prismarine")
                || path.contains("obsidian")
                || path.contains("flint")
                || path.contains("bamboo")
                || path.contains("glass")
                || isNonMetallicMineralPath(path);
    }

    private static boolean isNonMetallicMineralPath(String path) {
        return path.contains("coal")
                || path.contains("charcoal")
                || path.contains("diamond")
                || path.contains("emerald")
                || path.contains("lapis")
                || path.contains("quartz")
                || path.contains("redstone")
                || path.contains("amethyst")
                || path.contains("sulfur")
                || path.contains("salt")
                || path.contains("niter")
                || path.contains("apatite")
                || path.contains("ruby")
                || path.contains("sapphire");
    }

    private static boolean hasMetallicMaterialToken(String path) {
        return path.contains("iron")
                || path.contains("chainmail")
                || path.contains("gold")
                || path.contains("copper")
                || path.contains("netherite")
                // Canonical Entropica Vault Metals:
                || path.contains("arcanite")
                || path.contains("athanorite")
                || path.contains("caputite")
                || path.contains("eidolite")
                || path.contains("euclidite")
                || path.contains("mercurite")
                || path.contains("resonite")
                || path.contains("sanguinite")
                || path.contains("viscanite")
                // Common Modded Metals:
                || path.contains("steel")
                || path.contains("bronze")
                || path.contains("silver")
                || path.startsWith("tin_") || path.contains("_tin_") || path.endsWith("_tin")
                || path.startsWith("lead_") || path.contains("_lead_") || path.endsWith("_lead")
                || path.contains("nickel")
                || path.contains("electrum")
                || path.contains("invar")
                || path.contains("constantan")
                || path.contains("platinum")
                || path.contains("aluminum")
                || path.contains("aluminium")
                || path.contains("titanium")
                || path.contains("tungsten")
                || path.contains("zinc")
                || path.contains("brass")
                || path.contains("cobalt")
                || path.contains("manyullyn")
                || path.contains("osmium")
                || path.contains("uranium")
                || path.contains("mithril")
                || path.contains("mythril")
                || path.contains("adamant")
                || path.contains("orichalcum")
                || path.contains("palladium")
                || path.contains("iridium");
    }

    /**
     * Strictly matches loose Dirt ({@code DIRT}, {@code COARSE_DIRT}, {@code ROOTED_DIRT}),
     * Sand ({@code SAND}, {@code RED_SAND}, {@code SUSPICIOUS_SAND}), and Gravel ({@code GRAVEL}, {@code SUSPICIOUS_GRAVEL})
     * for {@code Sand & Gravel Flurry} — never breaking Grass Block, Podzol, Mycelium, Moss, Clay, or Mud.
     */
    public static boolean isLooseExcavatableSediment(@Nullable BlockState bs) {
        if (bs == null || bs.isAir()) {
            return false;
        }
        return bs.is(Blocks.DIRT)
                || bs.is(Blocks.COARSE_DIRT)
                || bs.is(Blocks.ROOTED_DIRT)
                || bs.is(Blocks.SAND)
                || bs.is(Blocks.RED_SAND)
                || bs.is(Blocks.SUSPICIOUS_SAND)
                || bs.is(Blocks.GRAVEL)
                || bs.is(Blocks.SUSPICIOUS_GRAVEL);
    }

    private static boolean isValidHostileOrPvpTarget(
            LivingEntity entity,
            @Nullable LivingEntity caster,
            boolean wardSafe
    ) {
        if (entity == null || !entity.isAlive() || entity == caster) {
            return false;
        }
        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(entity, caster, wardSafe)) {
            return false;
        }
        if (entity instanceof SpellEidolonEntity eid && caster != null && (eid.isOwnedBy(caster) || eid.isFriendlyTo(caster))) {
            return false;
        }
        if (entity instanceof OwnableEntity ownable) {
            LivingEntity petOwner = ownable.getOwner();
            if (petOwner != null) {
                if (caster != null && (petOwner == caster || petOwner.isAlliedTo(caster))) {
                    return false;
                }
                if (wardSafe && petOwner instanceof Player) {
                    return false;
                }
            }
        }
        if (entity instanceof Player player) {
            if (player.isSpectator() || player.isCreative()) {
                return false;
            }
            if (wardSafe) {
                return false;
            }
            return caster == null || !player.isAlliedTo(caster);
        }
        if (entity instanceof Enemy || entity instanceof Monster) {
            return true;
        }
        if (entity instanceof Mob mob && mob.getTarget() != null) {
            LivingEntity mobTarget = mob.getTarget();
            if (mobTarget == caster || (caster != null && mobTarget.isAlliedTo(caster))) {
                return true;
            }
        }
        if (entity instanceof NeutralMob neutral) {
            return neutral.isAngry() || (caster != null && caster.getUUID().equals(neutral.getPersistentAngerTarget()));
        }
        return false;
    }

    private static BlockPos findSpikeBaseBeneathEntity(ServerLevel level, LivingEntity victim) {
        BlockPos feet = BlockPos.containing(victim.getX(), victim.getY() + 0.05D, victim.getZ());
        for (int dy = 0; dy <= 3; dy++) {
            BlockPos cand = feet.below(dy);
            BlockState at = level.getBlockState(cand);
            BlockState below = level.getBlockState(cand.below());
            if ((at.isAir() || at.canBeReplaced()) && !below.getCollisionShape(level, cand.below()).isEmpty()) {
                return cand;
            }
        }
        return feet;
    }

    private static void emitDroppedCompactionFeedback(ServerLevel level, ItemEntity itemEntity) {
        spawnFaultCrystalSparks(level, itemEntity.position().add(0.0D, 0.20D, 0.0D), 8, 0.25D);
        level.playSound(null, itemEntity.blockPosition(), SoundEvents.BASALT_PLACE, SoundSource.PLAYERS, 0.70F, 0.90F);
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
