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
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Vitae Materia (Vital Fusion #21)</b>
 * (Spellcrafting System Guide Section 6.22):
 * <ul>
 *     <li><b>Canonical Advantage</b>: Strong against <b>UNDEAD (+1)</b> | Weak against <b>NETHER (-1)</b>,
 *     <b>FROZEN (-1)</b>, and <b>UMBRAL (-1)</b> (Dominance {@code vitae_nature_dom}: Strong vs Water &amp; Earth,
 *     Weak vs Nether, Frozen &amp; Umbral; computed dynamically by {@code VisInteractionManager}).</li>
 *     <li><b>Flow (Split)</b>: <i>Cellular Mitosis Split Flow</i> — splits into two smaller pulsating heart-orbs
 *     upon impact or reaching maximum range, with each sub-orb seeking out the lowest-health nearby ally or hostile.</li>
 *     <li><b>Projectile</b>: <i>Spiraling Vitality Helix</i> — intertwined double-helix bolts of vibrant rose life energy
 *     ({@code #FF6B9D}) siphoning positive status buffs from struck enemies directly to the caster.</li>
 *     <li><b>Beam</b>: <i>Cardiovascular Life-Tether</i> — audible heartbeat beam leaching exact 25% of damage dealt back
 *     to heal the caster, cleansing negative debuffs when aimed at allies, and triggering spontaneous breeding on animals.</li>
 *     <li><b>Radial Burst</b>: <i>Cellular Overcharge Bloom</i> — 6m+ biological shockwave ({@code #FF6B9D}) causing struck
 *     enemies to drop temporary collectible life-essence orbs while granting allies Speed II &amp; Jump Boost II for 8.0s.</li>
 *     <li><b>Touch / Strike</b>: <i>Cellular Overcharge Strike</i> — implants volatile vital force into the target, causing
 *     them to emit healing pulses to nearby allies whenever taking damage over the next 5.0s.</li>
 *     <li><b>Aura / Self</b>: <i>Cellular Overdrive Shroud</i> — crackling biological energy ({@code #FF6B9D}) doubling
 *     natural health regeneration rate, cutting food exhaustion rate by 50%, and granting +20% sprint speed while active.</li>
 *     <li><b>Glyph / Trap</b>: <i>Cellular Overcharge Mine</i> — disarmable Single/Repeating biological rune inflicting
 *     35% Slowness and marking the target to detonate into a healing explosion for players if killed within 5.0s.</li>
 *     <li><b>Cone / Spray</b>: <i>Cellular Rejuvenation Spray</i> — 60° sparkling rose mist disorienting enemies (35% miss
 *     chance) and knocking them back 3 blocks while cleansing debuffs and granting +20% movement speed to caught allies.</li>
 *     <li><b>Rain / Skyfall</b>: <i>Font of Vitality Downpour</i> — tier-scaled 16m+ rain of rose life essence for 12.0s
 *     restoring health &amp; hunger to players and pets, clearing ailments every 2.0s, and sprouting vibrant roses &amp; flora.</li>
 *     <li><b>Summon / Eidolon</b>: <i>Vitae Guardian Eidolon Suite</i> — {@code #FF6B9D} Vitality Knight Agent,
 *     6m heartbeat-cleansing Cardiovascular Spire Sentry, and 5m 6-heart-restoring Heart-Husk Effigy Decoy.</li>
 *     <li><b>Blink / Step</b>: <i>Cardiovascular Shock Blink</i> — 14m+ heartbeat boom stunning adjacent hostiles for 1.0s.</li>
 *     <li><b>Transmute</b>: <i>Vitality Synthesis Transmute</i> — cures zombie villagers back into healthy villagers,
 *     converts rotten flesh into edible raw meat, and turns dead bushes into blooming rose bushes.</li>
 *     <li><b>Affliction</b>: <i>Hyper-Metabolic Arrhythmia Hex</i> — milk-immune biological curse permanently disabling
 *     health regeneration, cutting attack damage by 40%, and inflicting self-damaging recoil whenever attempting an attack
 *     until cleansed via Restoration magic or caster defeat.</li>
 * </ul>
 */
public final class VitaeSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int VITAE_GUARDIAN_HEX_COLOR = 0xFF6B9D;
    public static final int VIBRANT_ROSE_HEX_COLOR = 0xFF6B9D;
    public static final int PEARL_BLUSH_HEX_COLOR = 0xFFD1E3;

    @Override
    public EssenceType materiaType() {
        return EssenceType.VITAE;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case DOUBLE_BIFURCATE, TRIPLE_TRIFURCATE, OCTUPLE_DOME -> "Cellular Mitosis Split Flow";
            default -> "Cellular Mitosis Split Flow (" + flow.getDisplayName() + ")";
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Spiraling Vitality Helix";
            case BEAM -> "Cardiovascular Life-Tether";
            case RADIAL_BURST -> "Cellular Overcharge Bloom";
            case TOUCH_STRIKE -> "Cellular Overcharge Strike";
            case AURA -> "Cellular Overdrive Shroud";
            case GLYPH -> "Cellular Overcharge Mine";
            case CONE -> "Cellular Rejuvenation Spray";
            case SKYFALL -> "Font of Vitality Downpour";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Cardiovascular Shock Blink";
            case TRANSMUTE, BREAK -> "Vitality Synthesis Transmute";
            case AFFLICTION -> "Hyper-Metabolic Arrhythmia Hex";
            case RESTORATION -> "Cellular Vitality Restoration";
            default -> "Cellular Vitality " + effect.getDisplayName();
        };
    }

    // =========================================================================
    // FLOW MUTATION — CELLULAR MITOSIS SPLIT FLOW
    // =========================================================================

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        if (projectile.tickCount % 2 == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), VIBRANT_ROSE_HEX_COLOR, 18, 0.85F, 0.05F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D
            );
        }
    }

    @Override
    public void onProjectileImpactOrExpireFromFlow(
            ServerLevel level,
            SpellProjectileEntity projectile,
            @Nullable LivingEntity caster,
            Vec3 splitOrigin,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        // Section 6.22 Cellular Mitosis Split Flow:
        // Splits into two smaller pulsating heart-orbs upon impact or reaching maximum range,
        // with each sub-orb seeking out the lowest-health nearby ally or hostile!
        projectile.spawnSplitChild(level, splitOrigin, -24.0F, true, 0.68F, 1.30F);
        projectile.spawnSplitChild(level, splitOrigin, 24.0F, true, 0.68F, 1.30F);

        level.sendParticles(ParticleTypes.HEART,
                splitOrigin.x, splitOrigin.y, splitOrigin.z, 6, 0.25D, 0.25D, 0.25D, 0.04D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), VIBRANT_ROSE_HEX_COLOR, 24, 1.00F, 0.06F),
                splitOrigin.x, splitOrigin.y, splitOrigin.z, 12, 0.28D, 0.28D, 0.28D, 0.04D
        );
        level.playSound(null, BlockPos.containing(splitOrigin), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.65F, 1.35F);
    }

    // =========================================================================
    // FORM MUTATION 1 — SPIRALING VITALITY HELIX (PROJECTILE)
    // =========================================================================

    @Override
    public void onProjectileSpawnedFromForm(
            ServerLevel level,
            SpellProjectileEntity projectile,
            LivingEntity caster,
            SpellAssembly spell,
            SubSpellSlot slot
    ) {
        projectile.setNoGravity(true);
        Vec3 vel = projectile.getDeltaMovement();
        if (vel.lengthSqr() > 1.0E-4D && vel.length() < 1.80D) {
            projectile.setDeltaMovement(vel.normalize().scale(1.82D));
            projectile.hasImpulse = true;
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.70F, 1.38F);
        level.playSound(null, caster.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.45F, 1.42F);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        double phase = projectile.tickCount * 0.72D;
        double ox = Math.cos(phase) * 0.22D;
        double oz = Math.sin(phase) * 0.22D;
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), VIBRANT_ROSE_HEX_COLOR, 18, 0.85F, 0.05F),
                projectile.getX() + ox, projectile.getY(), projectile.getZ() + oz, 1, 0.02D, 0.02D, 0.02D, 0.0D
        );
        if (projectile.tickCount % 3 == 0) {
            level.sendParticles(ParticleTypes.HEART,
                    projectile.getX() - ox, projectile.getY(), projectile.getZ() - oz, 1, 0.02D, 0.02D, 0.02D, 0.0D);
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
        // Section 6.22 Spiraling Vitality Helix:
        // On top of normal spell damage, siphons positive status buffs from struck enemies directly to the caster!
        if (!(hitEntity instanceof LivingEntity living) || !living.isAlive() || living == caster) return;
        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard())) {
            return;
        }

        if (!slot.hasHarmVerb()) {
            int tier = spell.getEffectiveTier();
            float helixDmg = (2.8F + tier * 0.85F) * spell.flawProfile().getPotencyMultiplier();
            SpellCombatReactionEngine.hurtWithVitaeMateriaAdvantage(level, living, caster, helixDmg, false, true);
        }

        if (caster != null && caster.isAlive()) {
            List<Holder<MobEffect>> toStrip = new ArrayList<>();
            for (MobEffectInstance eff : living.getActiveEffects()) {
                if (eff.getEffect().value().isBeneficial()) {
                    caster.addEffect(new MobEffectInstance(
                            eff.getEffect(),
                            Math.max(60, eff.getDuration()),
                            eff.getAmplifier(),
                            eff.isAmbient(),
                            eff.isVisible(),
                            eff.showIcon()
                    ));
                    toStrip.add(eff.getEffect());
                }
            }
            for (Holder<MobEffect> h : toStrip) {
                living.removeEffect(h);
            }
            if (!toStrip.isEmpty()) {
                level.sendParticles(ParticleTypes.HEART,
                        caster.getX(), caster.getY(0.95D), caster.getZ(), 6, 0.30D, 0.35D, 0.30D, 0.03D);
                level.sendParticles(
                        TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), VIBRANT_ROSE_HEX_COLOR, 22, 1.00F, 0.06F),
                        living.getX(), living.getY(0.85D), living.getZ(), 14, 0.35D, 0.40D, 0.35D, 0.05D
                );
                level.playSound(null, caster.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.80F, 1.32F);
            }
        }
    }

    // =========================================================================
    // FORM MUTATION 2 — CARDIOVASCULAR LIFE-TETHER (BEAM)
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
        // Section 6.22 Cardiovascular Life-Tether:
        // Audible heartbeat beam; leaches exact 25% of damage dealt back to heal the caster,
        // cleanses negative debuffs when aimed at allies, and triggers spontaneous breeding when swept over animals!
        level.playSound(null, caster.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.75F, 1.15F);
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        float potency = Math.max(0.5F, apparatusPotency);
        Player playerCaster = caster instanceof Player p ? p : null;

        for (LivingEntity hit : hitEntities) {
            if (!hit.isAlive() || hit == caster) continue;
            if (hit instanceof Animal animal && !(hit instanceof net.minecraft.world.entity.monster.Enemy)) {
                if (animal.canFallInLove()) {
                    animal.setInLove(playerCaster);
                    level.sendParticles(ParticleTypes.HEART,
                            animal.getX(), animal.getY(0.85D), animal.getZ(), 6, 0.28D, 0.28D, 0.28D, 0.03D);
                }
                continue;
            }
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) {
                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(hit);
                float allyHeal = (2.6F + tier * 0.65F) * potency;
                SpellCombatReactionEngine.notifyMagicalHeal(hit, allyHeal);
                hit.heal(allyHeal);
                level.sendParticles(ParticleTypes.HEART,
                        hit.getX(), hit.getY(0.90D), hit.getZ(), 5, 0.28D, 0.30D, 0.28D, 0.03D);
            } else if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) {
                // Note: When slot.hasHarmVerb() is true, SpellCombatReactionEngine.applyElementalHarm already heals caster
                // for exact 25% of actual damage dealt via vitaeBeamLifesteal!
                if (!slot.hasHarmVerb()) {
                    float beamDmg = (1.25F + tier * 0.36F) * potency;
                    float dealt = SpellCombatReactionEngine.hurtWithVitaeMateriaAdvantage(level, hit, caster, beamDmg, false, true);
                    if (dealt > 0.0F && caster.isAlive()) {
                        float siphon = dealt * 0.25F;
                        SpellCombatReactionEngine.notifyMagicalHeal(caster, siphon);
                        caster.heal(siphon);
                    }
                }
                level.sendParticles(ParticleTypes.HEART,
                        caster.getX(), caster.getY(0.95D), caster.getZ(), 2, 0.22D, 0.22D, 0.22D, 0.02D);
            }
        }

        // Sweep passive animals along the full beam corridor for spontaneous breeding!
        double actualReach = blockHit != null ? caster.getEyePosition().distanceTo(blockHit.getLocation()) : maxReach;
        AABB corridor = caster.getBoundingBox().expandTowards(direction.scale(actualReach)).inflate(1.35D);
        for (Animal animal : level.getEntitiesOfClass(Animal.class, corridor, LivingEntity::isAlive)) {
            if (animal.canFallInLove()) {
                animal.setInLove(playerCaster);
                level.sendParticles(ParticleTypes.HEART,
                        animal.getX(), animal.getY(0.85D), animal.getZ(), 6, 0.28D, 0.28D, 0.28D, 0.03D);
            }
        }
    }

    // =========================================================================
    // FORM MUTATION 3 — CELLULAR OVERCHARGE BLOOM (RADIAL BURST)
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
        // Section 6.22 Cellular Overcharge Bloom:
        // Outward biological shockwave (6-block+ radius, #FF6B9D); struck enemies drop temporary life-essence orbs,
        // while allies in the blast gain Speed II and Jump Boost II for 8.0 seconds (160t)!
        double r = Math.max(6.0D, radius);
        long now = level.getGameTime();
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        float potency = Math.max(0.5F, apparatusPotency);

        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.0D, center.z + r);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            double dx = e.getX() - center.x;
            double dz = e.getZ() - center.z;
            if (dx * dx + dz * dz > (r + 0.6D) * (r + 0.6D)) continue;

            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, e)) {
                // Speed II (amp 1) & Jump Boost II (amp 1) for exact 8.0 seconds (160 ticks)!
                e.addEffect(new MobEffectInstance(MobEffects.SPEED, 160, 1));
                e.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 160, 1));
                level.sendParticles(
                        TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), VIBRANT_ROSE_HEX_COLOR, 22, 0.95F, 0.05F),
                        e.getX(), e.getY(0.85D), e.getZ(), 8, 0.32D, 0.40D, 0.32D, 0.03D
                );
            } else if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, caster, wardSafe)) {
                if (!slot.hasHarmVerb()) {
                    float bloomDmg = (3.3F + tier * 0.90F) * potency;
                    SpellCombatReactionEngine.hurtWithVitaeMateriaAdvantage(level, e, caster, bloomDmg, false, true);
                }

                // Drop a temporary collectible #FF6B9D Life-Essence Orb at each struck enemy's position!
                final Vec3[] orbPos = {e.position().add(0.0D, 0.45D, 0.0D)};
                float orbHeal = (3.5F + tier * 0.90F) * potency;
                SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                        caster, orbPos[0], 2.4D, 120, now, spell, slot, fallbackMateria, apparatusPotency,
                        (srv, zone, elapsed) -> {
                            if (elapsed > zone.maxDurationTicks) return false;
                            if (elapsed % 3 == 0) {
                                srv.sendParticles(
                                        TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), VIBRANT_ROSE_HEX_COLOR, 14, 1.00F, 0.04F),
                                        orbPos[0].x, orbPos[0].y + 0.15D * Math.sin(elapsed * 0.25D), orbPos[0].z,
                                        3, 0.12D, 0.12D, 0.12D, 0.01D
                                );
                                if (elapsed % 9 == 0) {
                                    srv.sendParticles(ParticleTypes.HEART,
                                            orbPos[0].x, orbPos[0].y + 0.25D, orbPos[0].z, 1, 0.08D, 0.08D, 0.08D, 0.01D);
                                }
                            }
                            if (elapsed < 4) return true;

                            LivingEntity owner = zone.resolveCaster(srv);
                            AABB magnetBox = new AABB(
                                    orbPos[0].x - 3.6D, orbPos[0].y - 1.5D, orbPos[0].z - 3.6D,
                                    orbPos[0].x + 3.6D, orbPos[0].y + 2.2D, orbPos[0].z + 3.6D
                            );
                            for (LivingEntity ally : srv.getEntitiesOfClass(LivingEntity.class, magnetBox, LivingEntity::isAlive)) {
                                if (ally instanceof Player || SpellExecutionEngine.isFriendlyAllyForOasis(owner, ally)) {
                                    double distSq = ally.position().add(0.0D, 0.5D, 0.0D).distanceToSqr(orbPos[0]);
                                    if (distSq <= 3.24D) {
                                        SpellCombatReactionEngine.notifyMagicalHeal(ally, orbHeal);
                                        ally.heal(orbHeal);
                                        srv.sendParticles(ParticleTypes.HEART,
                                                ally.getX(), ally.getY(0.95D), ally.getZ(), 6, 0.28D, 0.28D, 0.28D, 0.03D);
                                        srv.playSound(null, ally.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.75F, 1.38F);
                                        return false;
                                    } else {
                                        Vec3 pull = ally.position().add(0.0D, 0.5D, 0.0D).subtract(orbPos[0]).normalize().scale(0.28D);
                                        orbPos[0] = orbPos[0].add(pull);
                                    }
                                }
                            }
                            return true;
                        }
                ));
            }
        }

        level.playSound(null, BlockPos.containing(center), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.0F, 1.05F);
        level.playSound(null, BlockPos.containing(center), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.90F, 1.32F);
    }

    // =========================================================================
    // FORM MUTATION 4 — CELLULAR OVERCHARGE STRIKE (TOUCH / STRIKE)
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
        // Section 6.22 Cellular Overcharge Strike:
        // On top of normal spell damage, implants volatile vital force into the target, causing them to emit
        // healing pulses to nearby allies whenever the afflicted victim takes damage over the next 5.0 seconds (100t)!
        int tier = spell.getEffectiveTier();
        long until = level.getGameTime() + 100L;
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        float potency = Math.max(0.5F, apparatusPotency);

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.position().add(0.0D, caster.getBbHeight() * 0.58D, 0.0D),
                direction, 3.4D, EssenceType.VITAE, EssenceType.NATURE, tier
        );

        for (int i = 0; i < hitEntities.size(); i++) {
            LivingEntity hit = hitEntities.get(i);
            if (!hit.isAlive() || hit == caster) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            float cleaveScale = (i == 0) ? 1.0F : 0.50F;
            CompoundTag data = EntityHelper.getPersistentData(hit);
            data.putLong(SpellLingeringZoneManager.TAG_VITAE_OVERCHARGE_UNTIL, until);
            data.putInt("SpellVitaeOverchargeTier", tier);
            data.putString("SpellVitaeOverchargeCaster", caster.getUUID().toString());

            if (!slot.hasHarmVerb()) {
                float strikeDmg = (3.5F + tier * 1.00F) * cleaveScale * potency;
                SpellCombatReactionEngine.hurtWithVitaeMateriaAdvantage(level, hit, caster, strikeDmg, true, true);
            }

            level.sendParticles(ParticleTypes.HEART,
                    hit.getX(), hit.getY(0.90D), hit.getZ(), 6, 0.28D, 0.32D, 0.28D, 0.04D);
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), VIBRANT_ROSE_HEX_COLOR, 24, 1.00F, 0.06F),
                    hit.getX(), hit.getY(0.85D), hit.getZ(), 12, 0.32D, 0.35D, 0.32D, 0.04D
            );
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.85F, 1.25F);
    }

    // =========================================================================
    // FORM MUTATION 5 — CELLULAR OVERDRIVE SHROUD (AURA / SELF)
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
        // Section 6.22 Cellular Overdrive Shroud:
        // Crackling biological energy (#FF6B9D): doubles natural health regeneration rate,
        // cuts food exhaustion rate by 50%, and grants a permanent +20% sprint speed bonus while active!
        int tier = spell.getEffectiveTier();
        int dur = 240 + tier * 40;
        long until = level.getGameTime() + dur;

        CompoundTag data = EntityHelper.getPersistentData(caster);
        data.putLong(SpellLingeringZoneManager.TAG_VITAE_SHROUD_UNTIL, until);
        data.putInt("SpellVitaeShroudTier", tier);

        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.0F, null);
        level.sendParticles(ParticleTypes.HEART,
                caster.getX(), caster.getY(1.00D), caster.getZ(), 10, 0.42D, 0.48D, 0.42D, 0.04D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), VIBRANT_ROSE_HEX_COLOR, 28, 1.05F, 0.06F),
                caster.getX(), caster.getY(0.85D), caster.getZ(), 20, 0.45D, 0.50D, 0.45D, 0.04D
        );
        level.playSound(null, caster.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.85F, 1.18F);
        level.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.85F, 1.35F);
    }

    // =========================================================================
    // FORM MUTATION 6 — CELLULAR OVERCHARGE MINE (GLYPH / TRAP)
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
        // Section 6.22 Cellular Overcharge Mine:
        // Disarmable (Sneak + Right-Click for 50% refund) biological rune (1s arming delay, Single/Repeating);
        // when triggered, inflicts exact 35% Slowness and marks the target to detonate into a healing explosion
        // for players if killed within 5.0 seconds (100t)!
        int tier = spell.getEffectiveTier();
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snapped = new Vec3(surface.getX() + 0.5D, surface.above().getY(), surface.getZ() + 0.5D);
        double trapRad = Math.max(3.0D, radius);
        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxTriggers = repeating ? 3 : 1;
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snapped.add(0.0D, 0.03D, 0.0D), trapRad, EssenceType.VITAE, EssenceType.NATURE, maxDuration, tier
        );
        level.playSound(null, surface.above(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.75F, 1.22F);

        final int[] triggersLeft = {maxTriggers};
        final int[] rearmCooldown = {0};

        SpellLingeringZoneManager.ActiveSpellZone glyphZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snapped, trapRad, maxDuration, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    // Canonical 1.0s (20t) Glyph arming delay
                    if (elapsed < 20) return true;
                    if (rearmCooldown[0] > 0) {
                        rearmCooldown[0]--;
                        return true;
                    }

                    if (elapsed % 12 == 0) {
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), VIBRANT_ROSE_HEX_COLOR, 18, 0.85F, 0.04F),
                                zone.center.x, zone.center.y + 0.12D, zone.center.z, 4, zone.radius * 0.42D, 0.05D, zone.radius * 0.42D, 0.01D
                        );
                    }

                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                    AABB triggerBox = new AABB(
                            zone.center.x - zone.radius, zone.center.y - 1.0D, zone.center.z - zone.radius,
                            zone.center.x + zone.radius, zone.center.y + 2.2D, zone.center.z + zone.radius
                    );
                    List<LivingEntity> victims = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e ->
                            e.isAlive() && e != owner && !SpellExecutionEngine.isFriendlyAllyForOasis(owner, e)
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe)));
                    if (victims.isEmpty()) return true;

                    long markUntil = srv.getGameTime() + 100L; // Exact 5.0 seconds
                    for (LivingEntity v : victims) {
                        // Stamp 5.0s Death-Bloom mark BEFORE damage so lethal mine kills detonate immediately!
                        CompoundTag vTag = EntityHelper.getPersistentData(v);
                        vTag.putLong(SpellLingeringZoneManager.TAG_VITAE_DEATH_BLOOM_UNTIL, markUntil);
                        vTag.putInt("SpellVitaeDeathBloomTier", tier);

                        // Exact 35% Slowness!
                        SpellCombatReactionEngine.applyTimedMovementSpeedSap(v, srv, 0.35D, 100L);
                        v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1));

                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, v, new Vec3(0.0D, 1.0D, 0.0D),
                                    zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency
                            );
                        }
                        if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                            float mineDmg = (3.4F + tier * 0.90F) * Math.max(0.5F, zone.apparatusPotency);
                            SpellCombatReactionEngine.hurtWithVitaeMateriaAdvantage(srv, v, owner, mineDmg, false, true);
                        }
                    }

                    SpellWorldVfxManager.broadcastRadialNova(srv, zone.center, zone.radius, EssenceType.VITAE, EssenceType.NATURE, tier);
                    srv.sendParticles(ParticleTypes.HEART,
                            zone.center.x, zone.center.y + 0.65D, zone.center.z, 14, 0.9D, 0.4D, 0.9D, 0.05D);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.0F, 1.05F);

                    triggersLeft[0]--;
                    rearmCooldown[0] = 35;
                    return triggersLeft[0] > 0;
                }
        ).markDisarmableGlyph();
        SpellLingeringZoneManager.registerZone(level, glyphZone);
        return true;
    }

    // =========================================================================
    // FORM MUTATION 7 — CELLULAR REJUVENATION SPRAY (CONE / SPRAY)
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
        // Section 6.22 Cellular Rejuvenation Spray:
        // Wide 60-degree cone of sparkling rose biological mist; disorients enemies with reduced attack accuracy (35% miss)
        // and knocks them back 3 blocks, while cleansing all negative debuffs and granting +20% movement speed to caught allies!
        int tier = spell.getEffectiveTier();
        long disorientUntil = level.getGameTime() + 100L;
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        float potency = Math.max(0.5F, apparatusPotency);

        SpellWorldVfxManager.broadcastVortexCone(
                level, caster.getEyePosition().subtract(0.0D, 0.22D, 0.0D),
                direction, coneRange, EssenceType.VITAE, EssenceType.RADIANT, tier
        );

        Vec3 flatPush = new Vec3(direction.x, 0.0D, direction.z);
        Vec3 normPush = flatPush.lengthSqr() > 1.0E-4D ? flatPush.normalize() : new Vec3(0.0D, 0.0D, 1.0D);

        for (LivingEntity hit : hitTargets) {
            if (!hit.isAlive() || hit == caster) continue;
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) {
                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(hit);
                SpellCombatReactionEngine.applyTimedMovementSpeedBoost(hit, level, 0.20D, 160L);
                hit.addEffect(new MobEffectInstance(MobEffects.SPEED, 160, 0));
                float mistHeal = (2.8F + tier * 0.70F) * potency;
                SpellCombatReactionEngine.notifyMagicalHeal(hit, mistHeal);
                hit.heal(mistHeal);
                level.sendParticles(ParticleTypes.HEART,
                        hit.getX(), hit.getY(0.90D), hit.getZ(), 5, 0.28D, 0.30D, 0.28D, 0.03D);
            } else if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) {
                if (!slot.hasHarmVerb()) {
                    float coneDmg = (2.6F + tier * 0.75F) * potency;
                    SpellCombatReactionEngine.hurtWithVitaeMateriaAdvantage(level, hit, caster, coneDmg, false, true);
                }
                hit.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
                EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_VITAE_DISORIENTED_UNTIL, disorientUntil);
                if (!SpellCombatReactionEngine.isBossEntity(level, hit)) {
                    // Exact 3-block knockback!
                    SpellExecutionEngine.applySyncedMotion(hit, new Vec3(normPush.x * 0.88D, 0.26D, normPush.z * 0.88D));
                }
            }
        }

        level.playSound(null, caster.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.90F, 1.35F);
        level.playSound(null, caster.blockPosition(), SoundEvents.BREEZE_SHOOT, SoundSource.PLAYERS, 0.55F, 1.38F);
    }

    // =========================================================================
    // FORM MUTATION 8 — FONT OF VITALITY DOWNPOUR (RAIN / SKYFALL)
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
        // Section 6.22 Font of Vitality Downpour:
        // Shimmering 16-block+ rain of rose life essence for 12.0 seconds (240t); deals Vitae Materia damage to hostiles,
        // continuously restores health & hunger to players and pets, clears negative ailments every 2.0 seconds (40t),
        // and sprouts vibrant roses and flora across the ground!
        int tier = spell.getEffectiveTier();
        double r = Math.max(8.0D, radius);
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(impactCenter));
        BlockPos baseAir = surface.above();
        Vec3 snappedCenter = new Vec3(surface.getX() + 0.5D, baseAir.getY(), surface.getZ() + 0.5D);
        double canopyHeight = SpellDeliveryForm.probeCaveSafeSkyfallHeight(level, baseAir, tier);
        int totalTicks = 240; // Canonical 12.0 seconds

        SpellWorldVfxManager.broadcastSkyfallPillar(
                level, snappedCenter, r, EssenceType.VITAE, EssenceType.NATURE, totalTicks, tier
        );
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snappedCenter.add(0.0D, 0.03D, 0.0D), r, EssenceType.VITAE, EssenceType.NATURE, totalTicks, tier
        );
        level.playSound(null, baseAir, SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.95F, 1.12F);
        level.playSound(null, baseAir, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.95F, 1.28F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snappedCenter, r, totalTicks, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;

                    if (elapsed % 5 == 0) {
                        double rainY = zone.center.y + Math.min(canopyHeight * 0.68D, 11.0D);
                        srv.sendParticles(
                                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), VIBRANT_ROSE_HEX_COLOR, 24, 0.95F, 0.05F),
                                zone.center.x, rainY, zone.center.z, 14 + tier * 2, zone.radius * 0.50D, 1.2D, zone.radius * 0.50D, 0.04D
                        );
                        srv.sendParticles(ParticleTypes.HEART,
                                zone.center.x, rainY * 0.85D, zone.center.z, 4 + tier / 2, zone.radius * 0.48D, 1.0D, zone.radius * 0.48D, 0.02D);
                    }

                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        float potency = Math.max(0.5F, zone.apparatusPotency);

                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + Math.max(5.0D, canopyHeight * 0.60D), zone.center.z + zone.radius
                        );
                        for (LivingEntity e : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            double dx = e.getX() - zone.center.x;
                            double dz = e.getZ() - zone.center.z;
                            if (dx * dx + dz * dz > (zone.radius + 0.6D) * (zone.radius + 0.6D)) continue;

                            if (e instanceof Player || SpellExecutionEngine.isFriendlyAllyForOasis(owner, e)) {
                                float heal = (2.4F + tier * 0.50F) * potency;
                                SpellCombatReactionEngine.notifyMagicalHeal(e, heal);
                                e.heal(heal);
                                if (e instanceof Player p) {
                                    p.getFoodData().eat(1, 0.45F);
                                }
                                // Clear negative ailments & hexes every 2.0 seconds (40 ticks)!
                                if (elapsed % 40 == 0) {
                                    SpellCombatReactionEngine.purifyHexesAndNegativeEffects(e);
                                }
                            } else if (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe)) {
                                if (zone.spell != null && zone.slot != null) {
                                    SpellExecutionEngine.deliverSubSpellOnEntity(
                                            srv, owner, e, new Vec3(0.0D, -1.0D, 0.0D),
                                            zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.35F
                                    );
                                }
                                if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                                    float downpourDmg = (1.90F + tier * 0.55F) * potency;
                                    SpellCombatReactionEngine.hurtWithVitaeMateriaAdvantage(srv, e, owner, downpourDmg, false, true);
                                }
                            }
                        }

                        // Sprout vibrant roses, Vitae Orchids & pink tulips across the ground!
                        sproutVitalityRoses(srv, BlockPos.containing(zone.center), zone.radius, 2 + tier / 3);
                    }
                    return true;
                }
        ));
        return true;
    }

    // =========================================================================
    // FORM MUTATION 9 — VITAE GUARDIAN EIDOLON SUITE (AGENT / SENTRY / DECOY)
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
        int tier = spell.getEffectiveTier();
        CompoundTag tag = EntityHelper.getPersistentData(summonEntity);
        tag.putString("SpellEidolonMateria", EssenceType.VITAE.name());
        tag.putInt("SpellEidolonTintRGB", VITAE_GUARDIAN_HEX_COLOR);

        SpellWorldVfxManager.broadcastSummonPillar(
                level, spawnPos, Math.max(1.65D, radius * 0.35D), EssenceType.VITAE, EssenceType.NATURE, tier
        );
        level.sendParticles(ParticleTypes.HEART,
                spawnPos.x, spawnPos.y + 0.95D, spawnPos.z, 12, 0.42D, 0.55D, 0.42D, 0.04D);
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), VIBRANT_ROSE_HEX_COLOR, 30, 1.05F, 0.06F),
                spawnPos.x, spawnPos.y + 0.90D, spawnPos.z, 20, 0.45D, 0.55D, 0.45D, 0.04D
        );
        level.playSound(null, BlockPos.containing(spawnPos), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.85F, 1.18F);
        level.playSound(null, BlockPos.containing(spawnPos), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.90F, 1.32F);
    }

    // =========================================================================
    // CORE EFFECT SYNERGIES — BLINK, TRANSMUTE, AFFLICTION & RESTORATION
    // =========================================================================

    @Override
    public void onBlinkCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 departurePos,
            Vec3 arrivalPos,
            int potencyTier
    ) {
        // Section 6.22 Cardiovascular Shock Blink:
        // Teleports up to 14+ blocks away with a resounding heartbeat boom, delivering an arrhythmia shock
        // that stuns adjacent hostiles for exact 1.0 second (20t)!
        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.VITAE, potencyTier);
        level.playSound(null, BlockPos.containing(departurePos), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.15F, 0.88F);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.15F, 0.88F);

        var paralyzed = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get());
        var anchor = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get());
        long stunUntil = level.getGameTime() + 20L;
        float shockDmg = 1.80F + potencyTier * 0.45F;

        for (Vec3 shockOrigin : List.of(departurePos, arrivalPos)) {
            SpellWorldVfxManager.broadcastRadialNova(
                    level, shockOrigin.add(0.0D, 0.25D, 0.0D), 3.6D, EssenceType.VITAE, EssenceType.NATURE, potencyTier
            );
            level.sendParticles(ParticleTypes.HEART,
                    shockOrigin.x, shockOrigin.y + 0.90D, shockOrigin.z, 8, 0.45D, 0.40D, 0.45D, 0.04D);
            AABB shockBox = new AABB(
                    shockOrigin.x - 3.8D, shockOrigin.y - 1.5D, shockOrigin.z - 3.8D,
                    shockOrigin.x + 3.8D, shockOrigin.y + 2.6D, shockOrigin.z + 3.8D
            );
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, shockBox, LivingEntity::isAlive)) {
                if (foe == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, foe)) continue;
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, true)) continue;

                SpellCombatReactionEngine.hurtWithVitaeMateriaAdvantage(level, foe, caster, shockDmg, false, true);
                foe.addEffect(new MobEffectInstance(paralyzed, 20, 0));
                foe.addEffect(new MobEffectInstance(anchor, 20, 0));
                foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 6));
                EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, stunUntil);
                SpellExecutionEngine.applySyncedMotion(foe, new Vec3(0.0D, Math.min(0.0D, foe.getDeltaMovement().y), 0.0D));
            }
        }
    }

    @Override
    public boolean onTransmuteBlock(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            BlockState state,
            int potencyTier
    ) {
        // Section 6.22 Vitality Synthesis Transmute:
        // Cures zombie villagers back into healthy villagers, converts rotten flesh into edible raw meat,
        // and turns dead bushes into blooming rose bushes!
        boolean curedAny = cureNearbyZombieVillagers(level, new AABB(pos).inflate(2.8D), caster, potencyTier);
        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return curedAny;
        }

        if (state.is(Blocks.DEAD_BUSH) || state.is(Blocks.WITHER_ROSE)) {
            BlockPos below = pos.below();
            BlockState belowState = level.getBlockState(below);
            // Dead bushes grow on sand/red_sand/terracotta where Rose Bushes cannot survive unless enriched into fertile grass!
            if (!belowState.is(BlockTags.DIRT)
                    && (belowState.is(Blocks.SAND) || belowState.is(Blocks.RED_SAND) || belowState.is(BlockTags.TERRACOTTA))
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, below, belowState)) {
                level.setBlockAndUpdate(below, Blocks.GRASS_BLOCK.defaultBlockState());
            }

            if (level.getBlockState(pos.above()).isAir()
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, pos.above(), level.getBlockState(pos.above()))
                    && Blocks.ROSE_BUSH.defaultBlockState().canSurvive(level, pos)) {
                level.removeBlock(pos, false);
                DoublePlantBlock.placeAt(level, Blocks.ROSE_BUSH.defaultBlockState(), pos, Block.UPDATE_ALL);
            } else {
                level.setBlockAndUpdate(pos, ModBlocks.VITAE_ORCHID.get().defaultBlockState());
            }
            level.sendParticles(ParticleTypes.HEART,
                    pos.getX() + 0.5D, pos.getY() + 0.85D, pos.getZ() + 0.5D, 5, 0.25D, 0.30D, 0.25D, 0.02D);
            maybeGrantVitaeBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        return curedAny;
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
        ItemStack out = ItemStack.EMPTY;
        if (in.is(Items.ROTTEN_FLESH)) {
            Item rawMeat = switch (level.random.nextInt(5)) {
                case 0 -> Items.BEEF;
                case 1 -> Items.PORKCHOP;
                case 2 -> Items.MUTTON;
                case 3 -> Items.CHICKEN;
                default -> Items.RABBIT;
            };
            out = new ItemStack(rawMeat, count);
        } else if (in.is(Items.DEAD_BUSH) || in.is(Items.WITHER_ROSE)) {
            out = new ItemStack(Items.ROSE_BUSH, count);
        }

        if (out.isEmpty()) {
            return false;
        }
        itemEntity.setItem(out);
        level.sendParticles(ParticleTypes.HEART,
                itemEntity.getX(), itemEntity.getY(0.35D), itemEntity.getZ(), 5, 0.18D, 0.18D, 0.18D, 0.02D);
        maybeGrantVitaeBatchTransmuteDiscount(level, caster, potencyTier);
        return true;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.22 Hyper-Metabolic Arrhythmia Hex:
        // Milk-immune biological curse (cleansed strictly via Restoration magic or caster defeat):
        // permanently disables health regeneration, cuts attack damage by 40%, and inflicts severe self-damaging recoil
        // whenever the afflicted enemy attempts an attack!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.removeEffect(MobEffects.REGENERATION);
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 1, true, true));
        SpellCombatReactionEngine.applyTimedAttackDebuff(target, level, Math.min(0.85D, 0.40D * mult), 0.0D, 38L);

        float arrhythmiaDmg = (1.20F + potencyTier * 0.42F) * mult;
        SpellCombatReactionEngine.hurtWithVitaeMateriaAdvantage(level, target, null, arrhythmiaDmg, false, true);

        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), VIBRANT_ROSE_HEX_COLOR, 18, 0.88F, 0.04F),
                target.getX(), target.getY(0.85D), target.getZ(), 4, 0.25D, 0.30D, 0.25D, 0.02D
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
        if ((effect == SpellCoreEffect.TRANSMUTE || effect == SpellCoreEffect.BREAK) && target instanceof ZombieVillager zv) {
            cureSingleZombieVillager(level, zv, caster, potencyTier);
        } else if (effect == SpellCoreEffect.RESTORATION) {
            SpellCombatReactionEngine.purifyHexesAndNegativeEffects(target);
            float bonusHeal = 3.5F + potencyTier * 1.15F;
            SpellCombatReactionEngine.notifyMagicalHeal(target, bonusHeal);
            target.heal(bonusHeal);
            level.sendParticles(ParticleTypes.HEART,
                    target.getX(), target.getY(0.95D), target.getZ(), 8, 0.32D, 0.38D, 0.32D, 0.03D);
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private static boolean cureNearbyZombieVillagers(
            ServerLevel level,
            AABB area,
            @Nullable LivingEntity caster,
            int potencyTier
    ) {
        boolean curedAny = false;
        for (ZombieVillager zv : level.getEntitiesOfClass(ZombieVillager.class, area, LivingEntity::isAlive)) {
            if (cureSingleZombieVillager(level, zv, caster, potencyTier)) {
                curedAny = true;
            }
        }
        return curedAny;
    }

    private static boolean cureSingleZombieVillager(
            ServerLevel level,
            ZombieVillager zv,
            @Nullable LivingEntity caster,
            int potencyTier
    ) {
        Vec3 pos = zv.position();
        var cured = zv.convertTo(EntityType.VILLAGER, ConversionParams.single(zv, false, false), villager -> {
            villager.setVillagerData(zv.getVillagerData());
            villager.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
        });
        if (cured != null) {
            level.sendParticles(ParticleTypes.HEART, pos.x, pos.y + 1.0D, pos.z, 14, 0.40D, 0.50D, 0.40D, 0.04D);
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SPARKLE.get(), VIBRANT_ROSE_HEX_COLOR, 26, 1.05F, 0.06F),
                    pos.x, pos.y + 0.95D, pos.z, 18, 0.42D, 0.50D, 0.42D, 0.04D
            );
            level.playSound(null, BlockPos.containing(pos), SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.PLAYERS, 0.95F, 1.15F);
            maybeGrantVitaeBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        return false;
    }

    private static void sproutVitalityRoses(ServerLevel level, BlockPos center, double radius, int count) {
        for (int i = 0; i < count; i++) {
            double ang = level.random.nextDouble() * Math.PI * 2.0D;
            double dist = Math.sqrt(level.random.nextDouble()) * radius;
            BlockPos sample = BlockPos.containing(
                    center.getX() + 0.5D + Math.cos(ang) * dist,
                    center.getY(),
                    center.getZ() + 0.5D + Math.sin(ang) * dist
            );
            BlockPos surf = findGroundSurfaceNear(level, sample);
            BlockPos up = surf.above();
            if (!level.getBlockState(up).isAir()
                    || SpellExecutionEngine.isProtectedBlockOrContainer(level, up, level.getBlockState(up))) {
                continue;
            }
            if (level.random.nextFloat() < 0.25F
                    && level.getBlockState(up.above()).isAir()
                    && Blocks.ROSE_BUSH.defaultBlockState().canSurvive(level, up)) {
                DoublePlantBlock.placeAt(level, Blocks.ROSE_BUSH.defaultBlockState(), up, Block.UPDATE_ALL);
            } else {
                Block flora = switch (level.random.nextInt(4)) {
                    case 0 -> ModBlocks.VITAE_ORCHID.get();
                    case 1 -> Blocks.POPPY;
                    case 2 -> Blocks.PINK_TULIP;
                    default -> Blocks.ALLIUM;
                };
                if (flora.defaultBlockState().canSurvive(level, up)) {
                    level.setBlockAndUpdate(up, flora.defaultBlockState());
                }
            }
        }
    }

    private static void maybeGrantVitaeBatchTransmuteDiscount(ServerLevel level, @Nullable LivingEntity caster, int tier) {
        if (!(caster instanceof Player player)) return;
        CompoundTag pData = EntityHelper.getPersistentData(player);
        long now = level.getGameTime();
        long prevTick = pData.getLong("SpellVitaeBatchTick").orElse(-20L);
        int count = (prevTick == now) ? pData.getInt("SpellVitaeBatchCount").orElse(0) + 1 : 1;
        pData.putLong("SpellVitaeBatchTick", now);
        pData.putInt("SpellVitaeBatchCount", count);
        if (count == 2) {
            long refund = Math.max(1L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25D));
            SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.VITAE, refund);
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
