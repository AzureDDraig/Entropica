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
import ddraig.net.entropica.spell.SpellPrefix;
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
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
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
 * Complete per-part Signature Mutation implementation for <b>Wraith Materia (Ethereal &amp; Paradox Fusion #37)</b>
 * (Spellcrafting System Guide Section 6.38 — Dual Void {@code #41304E} + Undead {@code #4D6453} [Leans: Void]).
 */
public final class WraithSpellMutationHandler implements MateriaPartMutationHandler {

    /** Canonical Abyssal Void Indigo (#41304E) primary tint. */
    public static final int WRAITH_STALKER_HEX_COLOR = 0x41304E;

    /** Canonical Necrotic Moss / Tombstone Ectoplasm (#4D6453) secondary tint. */
    public static final int WRAITH_ECTOPLASM_HEX_COLOR = 0x4D6453;

    private static final String TAG_WRAITH_BEAM_LAST_TICK = "SpellWraithBeamLastTick";
    private static final String TAG_WRAITH_BEAM_TICKS = "SpellWraithBeamTicks";

    @Override
    public EssenceType materiaType() {
        return EssenceType.WRAITH;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Incorporeal Soul-Shield Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Spectral Wraith-Skull Missile";
            case BEAM -> "Incorporeal Soul-Siphon Tether Beam";
            case RADIAL_BURST -> "Incorporeal Ghost-Storm Shockwave";
            case TOUCH_STRIKE -> "Spectral Phasing Palm-Gouge";
            case AURA -> "Specter Ectoplasm Armor";
            case GLYPH -> "Incorporeal Grave-Grip Pitfall Glyph";
            case CONE -> "Banshee Wail Ectoplasm Deluge";
            case SKYFALL -> "Spectral Rift Skull-Vortex Cataclysm";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Grave-Slip Blink";
            case TRANSMUTE, BREAK -> "Ghostly Ash Transmute";
            case AFFLICTION -> "Incorporeal Soul-Hollowing Hex";
            default -> "Incorporeal Wraith " + effect.getDisplayName();
        };
    }

    @Override
    public String resolveFlowDescription(SpellFlowModifier flow) {
        return "Orbiting shields of translucent void ectoplasm (#41304E / #4D6453) granting +4 Armor and converting 20% of incoming physical damage into void hunger recovery.";
    }

    @Override
    public String resolveFormDescription(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Hurls a screaming, wall-phasing skull of void-necrotic ectoplasm (#41304E / #4D6453) that homes in on targets, pierces up to 2 entities, deals Wraith Materia damage, inflicts Wither and Slowness II for 3.0s, and leeches 2 hearts.";
            case BEAM -> "Channels a wall-phasing ethereal tether (#41304E / #4D6453) leeching 1.5 hearts/s to heal the caster while slowing by 50%; 3.0s channeling triggers a Soul Severance Detonation tearing a soul-husk out for burst damage and a 1.5s stun.";
            case RADIAL_BURST -> "Erupts in a 6-block icy spectral shockwave (#41304E / #4D6453) rendering enemies intangible for 3.0s (unable to deal damage or use abilities), stripping 3 armor points, dealing Wraith Materia damage, and healing allied summons.";
            case TOUCH_STRIKE -> "Thrusting ghostly palm strike (#41304E / #4D6453) bypassing shields and armor toughness to deal heavy Wraith Materia damage, steal 2 full hearts, strip 3 armor points, and inflict a 1.5s Incorporeal Stagger stun.";
            case AURA -> "Envelops the caster in translucent void ectoplasm armor (#41304E / #4D6453) granting +8 Armor, 100% knockback immunity, 25% magic damage reflection, suffocation and wither immunity, and 100% Sculk/Warden vibration silence.";
            case GLYPH -> "Inscribes a disarmable shadowy grave sigil (#41304E / #4D6453) that drags caught mobs into 4.0s intangible phase-stasis (unable to move, attack, or cast abilities) with continuous life-siphoning Wraith Materia damage.";
            case CONE -> "Sprays a 60-degree cone of ultrasonic spectral wails and corrosive ectoplasm (#41304E / #4D6453), knocking enemies 6 blocks back, bypassing shields, inflicting 3.5s Blindness and Wither II, stripping 3 armor, and silencing abilities for 3.0s.";
            case SKYFALL -> "Opens a dark ethereal sky rift releasing swirling spectral skulls across a 12-block radius at varying heights (#41304E / #4D6453); targets caught in their paths suffer heavy Wraith Materia damage, Wither II, and Slowness III.";
            case SUMMON_AGENT -> "Summons a wall-phasing Wraith-Stalker biped (#41304E / #4D6453) that phases through solid blocks to strike behind enemies and siphon health.";
            case SUMMON_SENTRY -> "Anchors a Banshee Wail Obelisk (#41304E / #4D6453) shrieking across 8 blocks to silence enemy abilities and slow mobs by 40%.";
            case SUMMON_DECOY -> "Manifests an Ectoplasmic Soul-Husk Effigy (#41304E / #4D6453) that taunts hostiles and bursts into an Incorporeal Ghost-Storm Nova upon defeat.";
        };
    }

    @Override
    public String resolveEffectDescription(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Sinks into the floor into ethereal shadow and emerges up to 12 blocks away silently, dropping mob aggro and granting 2.0s of speed and intangibility upon emergence.";
            case TRANSMUTE, BREAK -> "Spectral decomposition converting dirt and grass into soul soil, foliage and leaves into cobwebs, cobblestone into mossy cobblestone, and bone blocks into soul sand.";
            case AFFLICTION -> "Persistent milk-immune soul-hollowing curse halving damage dealt (-50%), cutting movement speed by 40%, locking natural health regeneration, and transferring life-drain ticks to the nearby caster until cleansed by Vitae/Restoration or Radiant light.";
            default -> null;
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATION — INCORPOREAL SOUL-SHIELD FLOW
    // =========================================================================

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SINGULARITY.get(), WRAITH_STALKER_HEX_COLOR, 14, 0.72F, 0.10F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D
        );
        if ((projectile.tickCount & 1) == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_WISP.get(), WRAITH_ECTOPLASM_HEX_COLOR, 16, 0.68F, 0.04F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D
            );
        }
        if (flow == SpellFlowModifier.ORBIT_SATELLITE && projectile.getOwner() instanceof LivingEntity owner && owner.isAlive()) {
            long now = level.getGameTime();
            CompoundTag data = EntityHelper.getPersistentData(owner);
            if (data.getLong(SpellLingeringZoneManager.TAG_WRAITH_ORBIT_UNTIL).orElse(0L) < now + 30L) {
                data.putLong(SpellLingeringZoneManager.TAG_WRAITH_ORBIT_UNTIL, now + 35L);
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(owner, level, 4.0D, 0.0D, 0.0D, 0.0D, 35L);
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
        // Section 6.38 Spectral Wraith-Skull Missile:
        // Wall-phasing zero-drop ectoplasm skull that passes through solid non-magical blocks to home in on targets!
        projectile.setNoGravity(true);
        if (spell.prefix() != SpellPrefix.UTILITY) {
            projectile.setWallPhasing(true);
        }
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.SCULK_SOUL, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D);
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
        // Section 6.38 Spectral Wraith-Skull Missile:
        // Pierces up to 2 entities, deals Wraith Materia damage (+1 vs Air, +1 vs Umbral, +1 vs Aura, +1 vs Frozen),
        // inflicts Wither and Slowness II for 3.0s (60t), and leeches 2 hearts (4.0 HP) back to the caster!
        if (!(hitEntity instanceof LivingEntity victim) || !victim.isAlive()) return;
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        if (caster != null && SpellExecutionEngine.isFriendlyAllyForOasis(caster, victim)) return;
        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(victim, caster, wardSafe)) return;

        int tier = spell.getEffectiveTier();
        victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
        victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));

        if (!slot.hasHarmVerb()) {
            float skullDmg = 2.75F + tier * 0.70F;
            SpellCombatReactionEngine.hurtWithWraithMateriaAdvantage(level, victim, caster, skullDmg, false, true);
        }

        if (caster != null && caster.isAlive()) {
            SpellCombatReactionEngine.notifyMagicalHeal(caster, 4.0F);
            caster.heal(4.0F);
            level.sendParticles(ParticleTypes.SOUL, caster.getX(), caster.getY(0.9D), caster.getZ(), 6, 0.25D, 0.3D, 0.25D, 0.03D);
        }

        SpellWorldVfxManager.broadcastProjectileImpact(
                level, victim.position().add(0.0D, victim.getBbHeight() * 0.55D, 0.0D),
                new Vec3(0.0D, 1.0D, 0.0D), EssenceType.WRAITH, EssenceType.UNDEAD, 1.25F, tier
        );
        level.sendParticles(ParticleTypes.SCULK_SOUL, victim.getX(), victim.getY(0.8D), victim.getZ(), 12, 0.3D, 0.35D, 0.3D, 0.04D);
    }

    // =========================================================================
    // FORM MUTATION 2 — INCORPOREAL SOUL-SIPHON TETHER BEAM (BEAM)
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
        // Section 6.38 Incorporeal Soul-Siphon Tether Beam:
        // Phases through walls; continually leeches 1.5 hearts/s (3.0 HP/s = 0.60F per 4t pulse) to heal the caster
        // while slowing the victim (-50% speed); sustained channeling for 3.0s (60t) triggers Soul Severance Detonation
        // tearing a spectral soul-husk from the target, dealing massive burst damage & stunning for 1.5s (30t)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        Vec3 eye = caster.getEyePosition();
        Vec3 dirNorm = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();

        List<LivingEntity> phasedTargets = new ArrayList<>(hitEntities);
        AABB corridor = new AABB(eye, eye.add(dirNorm.scale(maxReach))).inflate(1.25D);
        for (LivingEntity cand : level.getEntitiesOfClass(LivingEntity.class, corridor, e -> e.isAlive() && e != caster)) {
            if (phasedTargets.contains(cand)) continue;
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, cand)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(cand, caster, wardSafe)) continue;
            Vec3 toCand = cand.getBoundingBox().getCenter().subtract(eye);
            double proj = toCand.dot(dirNorm);
            if (proj >= 0.0D && proj <= maxReach) {
                Vec3 perp = toCand.subtract(dirNorm.scale(proj));
                if (perp.lengthSqr() <= 1.44D) {
                    phasedTargets.add(cand);
                    SpellExecutionEngine.deliverSubSpellOnEntity(
                            level, caster, cand, dirNorm, spell, slot, fallbackMateria, apparatusPotency * 0.85F
                    );
                }
            }
        }

        long now = level.getGameTime();
        for (LivingEntity hit : phasedTargets) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            SpellCombatReactionEngine.applyTimedMovementSpeedSap(hit, level, 0.50D, 45L);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 2)); // -50% speed!

            if (caster.isAlive()) {
                SpellCombatReactionEngine.notifyMagicalHeal(caster, 0.60F); // 5 pulses/s * 0.60 = 3.0 HP/s (1.5 hearts/s)!
                caster.heal(0.60F);
            }

            if (!slot.hasHarmVerb()) {
                float siphonDmg = (1.30F + tier * 0.36F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithWraithMateriaAdvantage(level, hit, caster, siphonDmg, false, true);
            }

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong(TAG_WRAITH_BEAM_LAST_TICK).orElse(-100L);
            int contactTicks = (now - last <= 12L) ? data.getInt(TAG_WRAITH_BEAM_TICKS).orElse(0) + 4 : 4;
            data.putLong(TAG_WRAITH_BEAM_LAST_TICK, now);
            data.putInt(TAG_WRAITH_BEAM_TICKS, contactTicks);

            if (contactTicks >= 60) {
                data.putInt(TAG_WRAITH_BEAM_TICKS, 0);
                hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
                hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 6));

                // Tear a short-lived spectral soul-husk out of the victim!
                Vec3 huskPos = hit.position().add(-dirNorm.x * 0.9D, 0.0D, -dirNorm.z * 0.9D);
                SpellEidolonEntity.spawnShortLivedDecoy(
                        level, caster, huskPos, EssenceType.WRAITH,
                        10.0F + tier * 2.0F, 50, "Torn Soul-Husk", null
                );

                float severanceDmg = (6.80F + tier * 1.85F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithWraithMateriaAdvantage(level, hit, caster, severanceDmg, false, true);

                SpellWorldVfxManager.broadcastRadialNova(
                        level, hit.position().add(0.0D, 0.2D, 0.0D), 3.0D, EssenceType.WRAITH, EssenceType.UNDEAD, tier
                );
                level.sendParticles(ParticleTypes.SCULK_SOUL, hit.getX(), hit.getY(1.0D), hit.getZ(), 24, 0.45D, 0.55D, 0.45D, 0.08D);
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0.255F, 0.188F, 0.306F),
                        hit.getX(), hit.getY(1.0D), hit.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
                level.playSound(null, hit.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.72F, 1.45F);
            }
        }
    }

    // =========================================================================
    // FORM MUTATION 3 — INCORPOREAL GHOST-STORM SHOCKWAVE (RADIAL BURST)
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
        // Section 6.38 Incorporeal Ghost-Storm Shockwave:
        // 6-block+ icy spectral shockwave of howling void spirits (#41304E / #4D6453); renders caught enemies
        // intangible for 3.0s (60t, unable to deal damage or use abilities), strips 3 armor points, deals Wraith
        // Materia damage, and heals allied summons!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.0D, radius);
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.WRAITH, EssenceType.UNDEAD, tier);

        float stormDmg = (3.00F + tier * 0.76F) * Math.max(0.5F, apparatusPotency);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.5D, center.z + r);

        for (LivingEntity ent : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (ent == caster) continue;
            if (ent instanceof SpellEidolonEntity eid && caster.getUUID().equals(eid.getOwnerUuid())) {
                float summonHeal = 8.0F + tier * 1.2F;
                SpellCombatReactionEngine.notifyMagicalHeal(eid, summonHeal);
                eid.heal(summonHeal);
                level.sendParticles(ParticleTypes.SOUL, eid.getX(), eid.getY(0.9D), eid.getZ(), 8, 0.3D, 0.35D, 0.3D, 0.03D);
                continue;
            }
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, ent)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(ent, caster, wardSafe)) continue;

            SpellLingeringZoneManager.applyIntangiblePhase(ent, 60, false);
            SpellCombatReactionEngine.applyTimedAttributeShred(ent, level, 3.0D, 0.0D, 160L);

            Vec3 delta = ent.position().subtract(center);
            Vec3 away = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(0.95D) : new Vec3(0.0D, 0.28D, 0.0D);
            ent.setDeltaMovement(away.x, 0.28D, away.z);
            ent.hasImpulse = true;

            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithWraithMateriaAdvantage(level, ent, caster, stormDmg, false, true);
            }
        }

        level.sendParticles(ParticleTypes.SCULK_SOUL, center.x, center.y + 0.8D, center.z, 36, 2.2D, 0.6D, 2.2D, 0.06D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.2F, 0.75F);
    }

    // =========================================================================
    // FORM MUTATION 4 — SPECTRAL PHASING PALM-GOUGE (TOUCH / STRIKE)
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
        // Section 6.38 Spectral Phasing Palm-Gouge:
        // Completely bypasses target shields and armor toughness by phasing directly into the target's chest;
        // deals heavy Wraith Materia damage, steals 2 full hearts (4.0 HP) to heal the caster, strips 3 armor points,
        // and inflicts Incorporeal Stagger (1.5s / 30t stun)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D), direction, 2.8D,
                EssenceType.WRAITH, EssenceType.UNDEAD, tier
        );

        float palmDmg = (3.15F + tier * 0.82F) * Math.max(0.5F, apparatusPotency);

        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            if (hit.isBlocking()) {
                hit.stopUsingItem();
            }
            SpellCombatReactionEngine.applyPercentageArmorShred(hit, level, 0.0D, 1.0D, 120L);
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 4.0D, 120L);
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 6));

            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithWraithMateriaAdvantage(level, hit, caster, palmDmg, true, true);
            }

            if (caster.isAlive()) {
                SpellCombatReactionEngine.notifyMagicalHeal(caster, 4.0F);
                caster.heal(4.0F);
            }
            level.sendParticles(ParticleTypes.SCULK_SOUL, hit.getX(), hit.getY(0.9D), hit.getZ(), 16, 0.3D, 0.4D, 0.3D, 0.05D);
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 0.85F);
    }

    // =========================================================================
    // FORM MUTATION 5 — SPECTER ECTOPLASM ARMOR (AURA / SELF)
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
        // Section 6.38 Specter Ectoplasm Armor:
        // +8 Armor, complete knockback immunity (1.0), 25% magic damage reflection, suffocation & wither immunity,
        // and 100% acoustic vibration suppression (immune to Sculk Sensors & Wardens)!
        int dur = 200 + spell.getEffectiveTier() * 40;
        long until = level.getGameTime() + dur;
        CompoundTag data = EntityHelper.getPersistentData(caster);
        data.putLong(SpellLingeringZoneManager.TAG_WRAITH_SHROUD_UNTIL, until);
        data.putLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, until);
        caster.removeEffect(MobEffects.WITHER);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 8.0D, 0.0D, 1.0D, 0.0D, dur);
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.55F, null);
        level.sendParticles(ParticleTypes.SCULK_SOUL, caster.getX(), caster.getY(0.9D), caster.getZ(), 20, 0.45D, 0.55D, 0.45D, 0.04D);
    }

    // =========================================================================
    // FORM MUTATION 6 — INCORPOREAL GRAVE-GRIP PITFALL GLYPH (GLYPH / TRAP)
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
        // Section 6.38 Incorporeal Grave-Grip Pitfall Glyph:
        // Disarmable Single/Repeating shadowy grave sigil (#41304E / #4D6453, 1.0s arming delay); when stepped on,
        // erupts into an ethereal void vortex with ghostly skeletal arms dragging caught mobs partially underground
        // into intangible phase-stasis for 4.0s (80t) unable to move, attack, or cast abilities while taking
        // continuous life-siphoning Wraith Materia damage!
        int tier = spell.getEffectiveTier();
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snapped = new Vec3(surface.getX() + 0.5D, surface.above().getY(), surface.getZ() + 0.5D);
        double trapRad = Math.max(3.2D, radius);

        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxTriggers = repeating ? 3 : 1;
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE / 4 : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snapped.add(0.0D, 0.03D, 0.0D), trapRad, EssenceType.WRAITH, EssenceType.UNDEAD, Math.min(240, maxDuration), tier
        );

        final int[] triggersLeft = {maxTriggers};
        final int[] rearmCooldown = {0};

        SpellLingeringZoneManager.ActiveSpellZone glyphZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snapped, trapRad, maxDuration, level.getGameTime(), spell, slot, EssenceType.WRAITH, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();

                    if (elapsed > 0 && elapsed % 200 == 0) {
                        int rem = zone.maxDurationTicks >= 1_000_000_000 ? 240 : (int) Math.min(240L, zone.maxDurationTicks - elapsed);
                        SpellWorldVfxManager.broadcastArcaneSeal(
                                srv, zone.center.add(0.0D, 0.03D, 0.0D), zone.radius, EssenceType.WRAITH, EssenceType.UNDEAD, rem, tier
                        );
                    }
                    if (elapsed % 12 == 0) {
                        srv.sendParticles(ParticleTypes.SOUL, zone.center.x, zone.center.y + 0.10D, zone.center.z, 3, 0.75D, 0.05D, 0.75D, 0.01D);
                    }

                    if (elapsed < 20) return true; // 1.0s arming delay
                    if (rearmCooldown[0] > 0) {
                        rearmCooldown[0]--;
                        return true;
                    }

                    AABB trigger = new AABB(
                            zone.center.x - 2.4D, zone.center.y - 1.0D, zone.center.z - 2.4D,
                            zone.center.x + 2.4D, zone.center.y + 2.0D, zone.center.z + 2.4D
                    );
                    List<LivingEntity> victims = srv.getEntitiesOfClass(LivingEntity.class, trigger, e ->
                            e.isAlive() && e != owner
                                    && !SpellExecutionEngine.isFriendlyAllyForOasis(owner, e)
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe))
                    );
                    if (victims.isEmpty()) return true;

                    SpellWorldVfxManager.broadcastRadialNova(
                            srv, zone.center, 3.2D, EssenceType.WRAITH, EssenceType.UNDEAD, tier
                    );

                    // 4.0s (80t) intangible phase-stasis grave grip!
                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 3.2D, 80, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl > sZone.maxDurationTicks) return false;
                                LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                boolean sWard = sZone.spell != null && sZone.spell.hasWardFriendlyFireSafeguard();
                                int zTier = sZone.spell != null ? sZone.spell.getEffectiveTier() : tier;
                                AABB gripBox = new AABB(
                                        sZone.center.x - 3.2D, sZone.center.y - 1.5D, sZone.center.z - 3.2D,
                                        sZone.center.x + 3.2D, sZone.center.y + 2.5D, sZone.center.z + 3.2D
                                );
                                long now = sLvl.getGameTime();
                                for (LivingEntity v : sLvl.getEntitiesOfClass(LivingEntity.class, gripBox, e -> e.isAlive() && e != sOwner)) {
                                    if (sOwner != null && SpellExecutionEngine.isFriendlyAllyForOasis(sOwner, v)) continue;
                                    if (sOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, sOwner, sWard)) continue;

                                    SpellLingeringZoneManager.applyIntangiblePhase(v, 25, false);
                                    v.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 25, 0));
                                    v.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 25, 0));
                                    v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 6));
                                    EntityHelper.getPersistentData(v).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 25L);

                                    Vec3 pull = sZone.center.subtract(v.position()).scale(0.26D);
                                    v.setDeltaMovement(pull.x, Math.min(0.0D, v.getDeltaMovement().y - 0.14D), pull.z);
                                    v.hasImpulse = true;

                                    if (sEl % 20 == 0) {
                                        float gripDmg = (1.60F + zTier * 0.45F) * Math.max(0.5F, sZone.apparatusPotency);
                                        SpellCombatReactionEngine.hurtWithWraithMateriaAdvantage(sLvl, v, sOwner, gripDmg, false, true);
                                        if (sZone.spell != null && sZone.slot != null) {
                                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                                    sLvl, sOwner, v, Vec3.ZERO, sZone.spell, sZone.slot, sZone.fallbackMateria, sZone.apparatusPotency * 0.50F
                                            );
                                        }
                                        if (sOwner != null && sOwner.isAlive()) {
                                            SpellCombatReactionEngine.notifyMagicalHeal(sOwner, 1.5F);
                                            sOwner.heal(1.5F);
                                        }
                                    }
                                }
                                if (sEl % 6 == 0) {
                                    sLvl.sendParticles(ParticleTypes.SCULK_SOUL,
                                            sZone.center.x, sZone.center.y + 0.25D, sZone.center.z, 10, 1.1D, 0.25D, 1.1D, 0.03D);
                                }
                                return true;
                            }
                    ));

                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.1F, 0.70F);
                    triggersLeft[0]--;
                    rearmCooldown[0] = 85;
                    return triggersLeft[0] > 0;
                }
        ).markDisarmableGlyph();
        SpellLingeringZoneManager.registerZone(level, glyphZone);
        return true;
    }

    // =========================================================================
    // FORM MUTATION 7 — BANSHEE WAIL ECTOPLASM DELUGE (CONE / SPRAY)
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
        // Section 6.38 Banshee Wail Ectoplasm Deluge:
        // Wide 60° cone of ultrasonic spectral wails & corrosive void ectoplasm (#41304E / #4D6453);
        // knocks enemies 6 blocks back, bypasses physical shields, inflicts Blindness & Wither II for 3.5s (70t),
        // strips 3 armor points, and silences mob abilities for 3.0s (60t)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();
        Vec3 pushDir = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();

        SpellWorldVfxManager.broadcastVortexCone(
                level, caster.getEyePosition().subtract(0.0D, 0.2D, 0.0D), pushDir, coneRange,
                EssenceType.WRAITH, EssenceType.UNDEAD, tier
        );

        float coneDmg = (2.85F + tier * 0.72F) * Math.max(0.5F, apparatusPotency);

        for (LivingEntity hit : hitTargets) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            if (hit.isBlocking()) {
                hit.stopUsingItem();
            }
            hit.setDeltaMovement(pushDir.x * 1.48D, 0.35D, pushDir.z * 1.48D);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 70, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.WITHER, 70, 1));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 140L);
            SpellLingeringZoneManager.applyArcaneSilence(hit, 60);

            if (!slot.hasHarmVerb()) {
                SpellCombatReactionEngine.hurtWithWraithMateriaAdvantage(level, hit, caster, coneDmg, false, true);
            }
        }

        Vec3 mid = caster.getEyePosition().add(pushDir.scale(coneRange * 0.5D));
        level.sendParticles(ParticleTypes.SCULK_SOUL, mid.x, mid.y, mid.z, 26, 1.4D, 0.5D, 1.4D, 0.05D);
        level.playSound(null, caster.blockPosition(), SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 0.75F, 1.55F);
    }

    // =========================================================================
    // FORM MUTATION 8 — SPECTRAL RIFT SKULL-VORTEX CATACLYSM (SKYFALL)
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
        // Section 6.38 Spectral Rift Skull-Vortex Cataclysm:
        // Opens a dark ethereal rift in the ceiling/sky, releasing a swirling collection of trailing spectral skulls
        // spinning around the central casting point in a 12-block radius at varying heights (#41304E / #4D6453);
        // skulls pass freely through blocks and mobs, inflicting heavy Wraith Materia damage, Wither II, & Slowness III!
        int tier = spell.getEffectiveTier();
        double r = Math.max(12.0D, radius);

        SpellWorldVfxManager.broadcastSkyfallPillar(
                level, impactCenter, r, EssenceType.WRAITH, EssenceType.UNDEAD, 180, tier
        );

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 180, level.getGameTime(), spell, slot, EssenceType.WRAITH, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 4 == 0) {
                        for (int s = 0; s < 4; s++) {
                            double ang = (elapsed * 0.22D) + (Math.PI * 0.5D * s);
                            double orbitR = 2.5D + ((s * 2.8D + (elapsed % 24) * 0.25D) % Math.max(2.5D, zone.radius - 2.0D));
                            double h = 0.65D + (s * 1.30D);
                            double sx = zone.center.x + Math.cos(ang) * orbitR;
                            double sy = zone.center.y + h;
                            double sz = zone.center.z + Math.sin(ang) * orbitR;
                            srv.sendParticles(ParticleTypes.SCULK_SOUL, sx, sy, sz, 2, 0.15D, 0.15D, 0.15D, 0.02D);
                            srv.sendParticles(
                                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SINGULARITY.get(), WRAITH_STALKER_HEX_COLOR, 14, 0.75F, 0.08F),
                                    sx, sy, sz, 2, 0.12D, 0.12D, 0.12D, 0.02D
                            );
                        }
                    }
                    if (elapsed % 16 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;
                        float vortexDmg = (2.25F + zTier * 0.62F) * Math.max(0.5F, zone.apparatusPotency);
                        AABB area = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 6.5D, zone.center.z + zone.radius
                        );
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) continue;

                            foe.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1));
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 2));
                            SpellCombatReactionEngine.hurtWithWraithMateriaAdvantage(srv, foe, owner, vortexDmg, false, true);
                            if (zone.spell != null && zone.slot != null) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(
                                        srv, owner, foe, new Vec3(0.0D, -1.0D, 0.0D),
                                        zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.45F
                                );
                            }
                        }
                    }
                    return true;
                }
        ));
        return true;
    }

    // =========================================================================
    // FORM MUTATION 9 — INCORPOREAL WRAITH-STALKER SUITE (SUMMON / EIDOLON)
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
        tag.putString("SpellEidolonMateria", EssenceType.WRAITH.name());
        tag.putInt("SpellEidolonTintRGB", WRAITH_STALKER_HEX_COLOR);
        level.sendParticles(ParticleTypes.SCULK_SOUL, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 22, 0.4D, 0.55D, 0.4D, 0.04D);
    }

    // =========================================================================
    // PART 3: CORE EFFECT MUTATIONS
    // =========================================================================

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Section 6.38 Grave-Slip Blink:
        // Sinks into the floor into ethereal shadow and emerges up to 12 blocks away silently, dropping mob aggro
        // and granting 2.0s (40t) of speed and intangibility upon emergence!
        EntityHelper.getPersistentData(caster).putLong(
                SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, level.getGameTime() + 50L
        );
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(departurePos, arrivalPos).inflate(16.0D))) {
            if (mob.getTarget() == caster) {
                mob.setTarget(null);
                mob.setAggressive(false);
            }
            if (mob.getLastHurtByMob() == caster) {
                mob.setLastHurtByMob(null);
            }
            mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            mob.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
        }
        SpellCombatReactionEngine.applyTimedMovementSpeedBoost(caster, level, 0.35D, 40L);
        caster.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 1, false, false));
        SpellLingeringZoneManager.applyIntangiblePhase(caster, 40, true);
        level.sendParticles(ParticleTypes.SCULK_SOUL, departurePos.x, departurePos.y + 0.25D, departurePos.z, 12, 0.3D, 0.15D, 0.3D, 0.02D);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, arrivalPos.x, arrivalPos.y + 0.35D, arrivalPos.z, 12, 0.3D, 0.25D, 0.3D, 0.02D);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Section 6.38 Ghostly Ash Transmute:
        // Converts dirt and grass into soul soil, transmutes foliage and leaves into cobwebs, converts cobblestone
        // into mossy cobblestone, and turns bone blocks into soul sand!
        boolean didItem = false;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.5D))) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) {
                didItem = true;
            }
        }
        if (SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return didItem;
        }

        if (state.is(BlockTags.DIRT) && !state.is(Blocks.SOUL_SOIL)) {
            level.setBlockAndUpdate(pos, Blocks.SOUL_SOIL.defaultBlockState());
            maybeGrantWraithBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(BlockTags.LEAVES) || state.is(BlockTags.SMALL_FLOWERS)
                || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN) || state.is(Blocks.LARGE_FERN) || state.is(Blocks.VINE)) {
            level.setBlockAndUpdate(pos, Blocks.COBWEB.defaultBlockState());
            maybeGrantWraithBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.COBBLESTONE)) {
            level.setBlockAndUpdate(pos, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
            maybeGrantWraithBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.STONE_BRICKS)) {
            level.setBlockAndUpdate(pos, Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
            maybeGrantWraithBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (state.is(Blocks.BONE_BLOCK)) {
            level.setBlockAndUpdate(pos, Blocks.SOUL_SAND.defaultBlockState());
            maybeGrantWraithBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        return didItem;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        if (stack.is(ItemTags.DIRT) && !stack.is(Items.SOUL_SOIL)) {
            itemEntity.setItem(new ItemStack(Items.SOUL_SOIL, stack.getCount()));
            maybeGrantWraithBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (stack.is(ItemTags.LEAVES) || stack.is(ItemTags.SMALL_FLOWERS)
                || stack.is(Items.SHORT_GRASS) || stack.is(Items.FERN) || stack.is(Items.VINE)) {
            itemEntity.setItem(new ItemStack(Items.COBWEB, stack.getCount()));
            maybeGrantWraithBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (stack.is(Items.COBBLESTONE)) {
            itemEntity.setItem(new ItemStack(Items.MOSSY_COBBLESTONE, stack.getCount()));
            maybeGrantWraithBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (stack.is(Items.STONE_BRICKS)) {
            itemEntity.setItem(new ItemStack(Items.MOSSY_STONE_BRICKS, stack.getCount()));
            maybeGrantWraithBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        if (stack.is(Items.BONE_BLOCK)) {
            itemEntity.setItem(new ItemStack(Items.SOUL_SAND, stack.getCount()));
            maybeGrantWraithBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.38 Incorporeal Soul-Hollowing Hex:
        // Persistent milk-immune spectral curse (cleansed via Restoration/Vitae magic, Radiant light, or caster defeat):
        // permanently halves damage dealt (-50%), cuts movement speed by 40%, disables natural health regeneration,
        // and ticks continuous life-drain Wraith Materia damage transferred directly to the caster if nearby!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellCombatReactionEngine.applyTimedAttackDebuff(target, level, Math.min(0.85D, 0.50D * mult), 0.0D, 45L);
        SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, 0.40D, 45L);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 1, true, true));

        LivingEntity caster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        float drain = (1.50F + potencyTier * 0.50F) * mult;
        float dealt = SpellCombatReactionEngine.hurtWithWraithMateriaAdvantage(level, target, caster, drain, false, true);

        if (dealt > 0.0F && caster != null && caster.isAlive() && caster.distanceToSqr(target) <= 576.0D) {
            SpellCombatReactionEngine.notifyMagicalHeal(caster, dealt);
            caster.heal(dealt);
            level.sendParticles(ParticleTypes.SOUL, caster.getX(), caster.getY(0.9D), caster.getZ(), 3, 0.2D, 0.25D, 0.2D, 0.02D);
        }
        level.sendParticles(ParticleTypes.SCULK_SOUL, target.getX(), target.getY(0.85D), target.getZ(), 4, 0.25D, 0.3D, 0.25D, 0.02D);
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

    private static void maybeGrantWraithBatchTransmuteDiscount(ServerLevel level, @Nullable LivingEntity caster, int tier) {
        if (!(caster instanceof Player player)) return;
        CompoundTag pData = EntityHelper.getPersistentData(player);
        long now = level.getGameTime();
        long prevTick = pData.getLong("SpellWraithBatchTick").orElse(-20L);
        int count = (prevTick == now) ? pData.getInt("SpellWraithBatchCount").orElse(0) + 1 : 1;
        pData.putLong("SpellWraithBatchTick", now);
        pData.putInt("SpellWraithBatchCount", count);
        if (count == 2) {
            long refund = Math.max(1L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25D));
            SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.WRAITH, refund);
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
