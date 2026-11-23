package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.particle.TimedTintableParticleOption;
import ddraig.net.entropica.entity.SpellEidolonEntity;
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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Complete per-part Signature Mutation implementation for <b>Aegis Materia (Ethereal &amp; Paradox Fusion #34)</b>
 * (Spellcrafting System Guide Section 6.35 — Dual Radiant {@code #FFEA78} + Earth {@code #773214} [Leans: Earth]).
 */
public final class AegisSpellMutationHandler implements MateriaPartMutationHandler {

    /** Canonical Holy Gold (#FFEA78) primary tint. */
    public static final int GILDED_PALADIN_HEX_COLOR = 0xFFEA78;

    /** Canonical Granite Brown (#773214) secondary tint. */
    public static final int GRANITE_BASTION_HEX_COLOR = 0x773214;

    private static final String TAG_AEGIS_BEAM_LAST_TICK = "SpellAegisBeamLastTick";
    private static final String TAG_AEGIS_BEAM_TICKS = "SpellAegisBeamTicks";

    @Override
    public EssenceType materiaType() {
        return EssenceType.AEGIS;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Gilded Bastion Orbit Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Gilded Bastion Shield-Slug";
            case BEAM -> "Citadel Bastion Tether Beam";
            case RADIAL_BURST -> "Gilded Bulwark Rampart Nova";
            case TOUCH_STRIKE -> "Gilded Shield-Bash Strike";
            case AURA -> "Gilded Bastion Citadel Mantle";
            case GLYPH -> "Citadel Rampart Vault Mine";
            case CONE -> "Gilded Granite Shrapnel Deluge";
            case SKYFALL -> "Gilded Citadel Megalith Cataclysm";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY ->
                    SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Bulwark Phalanx Step";
            case TRANSMUTE, BREAK -> "Gilded Petrofaction Transmute";
            case AFFLICTION -> "Petrifying Citadel Sundering Hex";
            default -> "Gilded Bastion " + effect.getDisplayName();
        };
    }

    // =========================================================================
    // PART 1: FLOW MUTATION — GILDED BASTION ORBIT FLOW
    // =========================================================================

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(
                TimedTintableParticleOption.linear(ModParticles.TINTABLE_SIGIL.get(), GILDED_PALADIN_HEX_COLOR, 14, 0.74F, 0.04F),
                projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.04D, 0.04D, 0.04D, 0.01D
        );
        if ((projectile.tickCount & 1) == 0) {
            level.sendParticles(
                    TimedTintableParticleOption.linear(ModParticles.TINTABLE_SHIELD.get(), GRANITE_BASTION_HEX_COLOR, 16, 0.70F, 0.02F),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D
            );
        }
    }

    // =========================================================================
    // FORM MUTATION 1 — GILDED BASTION SHIELD-SLUG (PROJECTILE)
    // =========================================================================

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.WAX_ON, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
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
        // Section 6.35 Gilded Bastion Shield-Slug:
        // Heavy flying pavise shield forged of golden quartz & holy granite (#FFEA78 / #773214); slams targets with
        // blunt kinetic force, knocking them 5 blocks backward, rooting them in golden earth for 2.5s (50t), and
        // raising a temporary 1-block wide golden stone rampart behind them!
        if (!(hitEntity instanceof LivingEntity living) || !living.isAlive()) return;
        if (living == caster || SpellExecutionEngine.isFriendlyAllyForOasis(caster, living)) return;
        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard())) {
            return;
        }

        int tier = spell.getEffectiveTier();
        Vec3 dir = projectile.getDeltaMovement().lengthSqr() > 0.001D
                ? projectile.getDeltaMovement().normalize()
                : (caster != null ? caster.getLookAngle().normalize() : new Vec3(0.0D, 0.0D, 1.0D));

        living.setDeltaMovement(dir.x * 1.28D, 0.30D, dir.z * 1.28D);
        living.hasImpulse = true;
        living.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 50, 0));
        living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 5));
        EntityHelper.getPersistentData(living).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, level.getGameTime() + 50L);

        SpellWorldVfxManager.broadcastProjectileImpact(
                level, living.position().add(0.0D, living.getBbHeight() * 0.5D, 0.0D),
                dir, EssenceType.AEGIS, EssenceType.RADIANT, 0.95F, tier
        );

        if (!slot.hasHarmVerb()) {
            float slugDmg = 3.05F + tier * 0.78F;
            SpellCombatReactionEngine.hurtWithAegisMateriaAdvantage(level, living, caster, slugDmg, false, true);
        }

        BlockPos rampartBase = BlockPos.containing(living.position().add(dir.x * 1.4D, 0.0D, dir.z * 1.4D));
        placeTemporaryStateMap(level, Map.of(
                rampartBase.immutable(), Blocks.POLISHED_GRANITE.defaultBlockState(),
                rampartBase.above().immutable(), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState()
        ), 100);
        level.playSound(null, living.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.75F, 1.35F);
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
        Vec3 hitPos = blockHit.getLocation();
        Vec3 normal = Vec3.atLowerCornerOf(blockHit.getDirection().getUnitVec3i());
        SpellWorldVfxManager.broadcastProjectileImpact(
                level, hitPos, normal, EssenceType.AEGIS, EssenceType.RADIANT, 0.90F, spell.getEffectiveTier()
        );
    }

    // =========================================================================
    // FORM MUTATION 2 — CITADEL BASTION TETHER BEAM (BEAM)
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
        // Section 6.35 Citadel Bastion Tether Beam:
        // Sustained radiant-earthen repellent forcefield beam (#FFEA78 / #773214); creates a repulsion boundary
        // preventing enemies from approaching while shredding 1 physical armor point per second; when channeled on
        // an ally, anchors them with +8 Armor and 50% damage reduction!
        long now = level.getGameTime();
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        for (LivingEntity hit : hitEntities) {
            if (hit != caster && SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) {
                linkAegisBastionTether(level, caster, hit, now);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.setDeltaMovement(direction.x * 0.78D, 0.22D, direction.z * 0.78D);
            hit.hasImpulse = true;

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong(TAG_AEGIS_BEAM_LAST_TICK).orElse(-100L);
            int contactTicks = (now - last <= 12L) ? data.getInt(TAG_AEGIS_BEAM_TICKS).orElse(0) + 4 : 4;
            data.putLong(TAG_AEGIS_BEAM_LAST_TICK, now);
            data.putInt(TAG_AEGIS_BEAM_TICKS, contactTicks);

            double armorShred = Math.min(8.0D, 1.0D + (contactTicks / 20));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, armorShred, 0.0D, 80L);

            if (!slot.hasHarmVerb()) {
                float beamDmg = (1.30F + tier * 0.36F) * Math.max(0.5F, apparatusPotency);
                SpellCombatReactionEngine.hurtWithAegisMateriaAdvantage(level, hit, caster, beamDmg, false, true);
            }
        }

        // Scan beam corridor for friendly allies in case hitEntities only collected offensive targets
        AABB rayBox = caster.getBoundingBox().expandTowards(direction.scale(Math.min(16.0D, maxReach))).inflate(1.0D);
        for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, rayBox,
                e -> e != caster && e.isAlive() && SpellExecutionEngine.isFriendlyAllyForOasis(caster, e))) {
            linkAegisBastionTether(level, caster, ally, now);
        }
    }

    private static void linkAegisBastionTether(ServerLevel level, LivingEntity caster, LivingEntity ally, long now) {
        CompoundTag data = EntityHelper.getPersistentData(ally);
        data.putString(SpellLingeringZoneManager.TAG_AEGIS_TETHER_GUARD_UUID, caster.getUUID().toString());
        data.putLong(SpellLingeringZoneManager.TAG_AEGIS_TETHER_UNTIL, now + 60L);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, level, 8.0D, 2.0D, 0.50D, 0.0D, 60L);
        SpellCombatReactionEngine.syncEntityBarrierState(level, ally, 0.30F, null);
        level.sendParticles(ParticleTypes.WAX_ON, ally.getX(), ally.getY(1.0D), ally.getZ(), 6, 0.3D, 0.4D, 0.3D, 0.03D);
    }

    // =========================================================================
    // FORM MUTATION 3 — GILDED BULWARK RAMPART NOVA (RADIAL BURST)
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
        // Section 6.35 Gilded Bulwark Rampart Nova:
        // Violent 360° radial blast of golden marble pillars & holy shockwaves (6m+ radius, #FFEA78 / #773214);
        // hurls enemies 8 blocks backward flat, smites undead (+50% bonus damage), raises temporary defensive
        // barrier spikes, and grants caught allies +4 Armor and 50% knockback resistance!
        int tier = spell.getEffectiveTier();
        double r = Math.max(6.0D, radius);
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastRadialNova(level, center, r, EssenceType.AEGIS, EssenceType.RADIANT, tier);

        float smiteBonus = 0.50F * MateriaStageTable.getBaseCombatDamageHp(tier) * Math.max(0.5F, apparatusPotency);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r,
                center.x + r, center.y + 3.5D, center.z + r);

        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 4.0D, 0.0D, 0.50D, 0.0D, 140L);

        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (e == caster) continue;
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, e)) {
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(e, level, 4.0D, 0.0D, 0.50D, 0.0D, 140L);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, caster, wardSafe)) continue;

            Vec3 delta = e.position().subtract(center);
            Vec3 away = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.85D) : new Vec3(0.0D, 0.44D, 0.0D);
            e.setDeltaMovement(away.x, 0.44D, away.z);
            e.hasImpulse = true;
            e.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 20, 0));

            boolean undead = e.getType().is(EntityTypeTags.UNDEAD);
            if (undead) {
                e.igniteForSeconds(5);
            }
            if (slot.hasHarmVerb()) {
                if (undead) {
                    SpellCombatReactionEngine.hurtWithAegisMateriaAdvantage(
                            level, e, caster, Math.max(3.5F, smiteBonus), false, true
                    );
                }
            } else {
                float novaDmg = (3.10F + tier * 0.78F) * Math.max(0.5F, apparatusPotency) * (undead ? 1.50F : 1.0F);
                SpellCombatReactionEngine.hurtWithAegisMateriaAdvantage(level, e, caster, novaDmg, false, true);
            }
        }

        BlockPos cPos = BlockPos.containing(center);
        Map<BlockPos, BlockState> spikes = new HashMap<>();
        int[][] offsets = {{3, 0}, {-3, 0}, {0, 3}, {0, -3}, {2, 2}, {-2, 2}, {2, -2}, {-2, -2}};
        for (int i = 0; i < offsets.length; i++) {
            BlockPos b = findGroundSurfaceNear(level, cPos.offset(offsets[i][0], 0, offsets[i][1])).above();
            spikes.put(b.immutable(), (i % 2 == 0) ? Blocks.QUARTZ_PILLAR.defaultBlockState() : Blocks.POLISHED_GRANITE.defaultBlockState());
            spikes.put(b.above().immutable(), Blocks.QUARTZ_PILLAR.defaultBlockState());
        }
        placeTemporaryStateMap(level, spikes, 100);

        level.sendParticles(ParticleTypes.WAX_ON, center.x, center.y + 0.8D, center.z, 32, 2.2D, 0.5D, 2.2D, 0.06D);
        level.playSound(null, cPos, SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.9F, 1.25F);
    }

    // =========================================================================
    // FORM MUTATION 4 — GILDED SHIELD-BASH STRIKE (TOUCH / STRIKE)
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
        // Section 6.35 Gilded Shield-Bash Strike:
        // Slams target with a summoned tower shield of golden granite (#FFEA78 / #773214); deals Aegis Materia damage,
        // knocks the victim 6 blocks backward, stuns them flat for 1.5s (30t), smites undead (+50%), shreds 3 armor
        // points, and bolsters the caster with +4 Armor for 5.0s (100t)!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastCrescentSlash(
                level, caster.getEyePosition().subtract(0.0D, 0.25D, 0.0D), direction, 2.8D,
                EssenceType.AEGIS, EssenceType.RADIANT, tier
        );

        float smiteBonus = 0.50F * MateriaStageTable.getBaseCombatDamageHp(tier) * Math.max(0.5F, apparatusPotency);
        float baseBashDmg = (2.95F + tier * 0.75F) * Math.max(0.5F, apparatusPotency);

        for (LivingEntity hit : hitEntities) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.setDeltaMovement(direction.x * 1.48D, 0.36D, direction.z * 1.48D);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 6));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 120L);

            boolean undead = hit.getType().is(EntityTypeTags.UNDEAD);
            if (undead) {
                hit.igniteForSeconds(5);
            }
            if (slot.hasHarmVerb()) {
                if (undead) {
                    SpellCombatReactionEngine.hurtWithAegisMateriaAdvantage(
                            level, hit, caster, Math.max(3.5F, smiteBonus), true, true
                    );
                }
            } else {
                SpellCombatReactionEngine.hurtWithAegisMateriaAdvantage(
                        level, hit, caster, baseBashDmg * (undead ? 1.50F : 1.0F), true, true
                );
            }
        }

        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 4.0D, 0.0D, 0.25D, 0.0D, 100L);
        level.playSound(null, caster.blockPosition(), SoundEvents.SHIELD_BLOCK.value(), SoundSource.PLAYERS, 1.0F, 1.15F);
    }

    // =========================================================================
    // FORM MUTATION 5 — GILDED BASTION CITADEL MANTLE (AURA / SELF)
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
        // Section 6.35 Gilded Bastion Citadel Mantle:
        // Interlocking golden granite & radiant quartz armor plates (#FFEA78 / #773214); grants +10 Armor,
        // +4 Armor Toughness, 75% knockback resistance, Blindness/Darkness immunity, -25% physical damage reduction,
        // and reflects 40% of melee damage back at attackers with a 2m knockback (with -10% movement speed)!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_AEGIS_MANTLE_UNTIL, level.getGameTime() + dur);
        caster.removeEffect(MobEffects.BLINDNESS);
        caster.removeEffect(MobEffects.DARKNESS);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 10.0D, 4.0D, 0.75D, -0.10D, dur);
        SpellCombatReactionEngine.syncEntityBarrierState(level, caster, 0.55F, null);
        level.sendParticles(ParticleTypes.WAX_ON, caster.getX(), caster.getY(0.9D), caster.getZ(), 22, 0.45D, 0.55D, 0.45D, 0.04D);
    }

    // =========================================================================
    // FORM MUTATION 6 — CITADEL RAMPART VAULT MINE (GLYPH / TRAP)
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
        // Section 6.35 Citadel Rampart Vault Mine:
        // Disarmable Single/Repeating hidden golden rune (1.0s arming delay); when tripped, violently erupts into a
        // 4m circular ring of golden granite spires, trapping caught mobs inside for 4.0s (80t), shredding 4 armor
        // points, and blocking enemy projectile line-of-sight!
        int tier = spell.getEffectiveTier();
        BlockPos surface = findGroundSurfaceNear(level, BlockPos.containing(glyphCenter));
        Vec3 snapped = new Vec3(surface.getX() + 0.5D, surface.above().getY(), surface.getZ() + 0.5D);
        double vaultRad = Math.max(4.0D, radius);

        String subMode = slot.primaryEffect().subMode();
        boolean repeating = subMode != null && (subMode.contains("repeat") || subMode.contains("sentry"));
        int maxTriggers = repeating ? 3 : 1;
        int maxDuration = tier >= 5 ? Integer.MAX_VALUE / 4 : 12_000;

        SpellWorldVfxManager.broadcastArcaneSeal(
                level, snapped.add(0.0D, 0.03D, 0.0D), vaultRad, EssenceType.AEGIS, EssenceType.RADIANT, Math.min(240, maxDuration), tier
        );

        final int[] triggersLeft = {maxTriggers};
        final int[] rearmCooldown = {0};

        SpellLingeringZoneManager.ActiveSpellZone glyphZone = new SpellLingeringZoneManager.ActiveSpellZone(
                caster, snapped, vaultRad, maxDuration, level.getGameTime(), spell, slot, EssenceType.AEGIS, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks || triggersLeft[0] <= 0) return false;
                    if (elapsed > 0 && elapsed % 200 == 0) {
                        int rem = zone.maxDurationTicks >= 1_000_000_000 ? 240 : (int) Math.min(240L, zone.maxDurationTicks - elapsed);
                        SpellWorldVfxManager.broadcastArcaneSeal(
                                srv, zone.center.add(0.0D, 0.03D, 0.0D), zone.radius, EssenceType.AEGIS, EssenceType.RADIANT, rem, tier
                        );
                    }
                    if (elapsed % 12 == 0) {
                        srv.sendParticles(ParticleTypes.WAX_ON,
                                zone.center.x, zone.center.y + 0.15D, zone.center.z, 4, 1.0D, 0.1D, 1.0D, 0.02D);
                    }
                    if (elapsed < 20) return true;
                    if (rearmCooldown[0] > 0) {
                        rearmCooldown[0]--;
                        return true;
                    }

                    LivingEntity owner = zone.resolveCaster(srv);
                    boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                    AABB triggerBox = new AABB(
                            zone.center.x - 2.6D, zone.center.y - 1.0D, zone.center.z - 2.6D,
                            zone.center.x + 2.6D, zone.center.y + 2.2D, zone.center.z + 2.6D
                    );
                    List<LivingEntity> triggers = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e ->
                            e.isAlive() && e != owner && !SpellExecutionEngine.isFriendlyAllyForOasis(owner, e)
                                    && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, wardSafe)));
                    if (triggers.isEmpty()) return true;

                    BlockPos cPos = BlockPos.containing(zone.center);
                    Map<BlockPos, BlockState> ring = new HashMap<>();
                    int[][] ringCoords = {
                            {3, 0}, {-3, 0}, {0, 3}, {0, -3},
                            {3, 1}, {3, -1}, {-3, 1}, {-3, -1},
                            {1, 3}, {-1, 3}, {1, -3}, {-1, -3},
                            {2, 2}, {-2, 2}, {2, -2}, {-2, -2}
                    };
                    for (int i = 0; i < ringCoords.length; i++) {
                        BlockPos baseP = findGroundSurfaceNear(srv, cPos.offset(ringCoords[i][0], 0, ringCoords[i][1])).above();
                        ring.put(baseP.immutable(), Blocks.POLISHED_GRANITE.defaultBlockState());
                        ring.put(baseP.above().immutable(), (i % 2 == 0)
                                ? Blocks.QUARTZ_PILLAR.defaultBlockState()
                                : Blocks.POLISHED_GRANITE.defaultBlockState());
                    }
                    placeTemporaryStateMap(srv, ring, 80);

                    int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;
                    float vaultDmg = (2.80F + zTier * 0.72F) * Math.max(0.5F, zone.apparatusPotency);
                    AABB cageBox = new AABB(
                            zone.center.x - 3.6D, zone.center.y - 1.2D, zone.center.z - 3.6D,
                            zone.center.x + 3.6D, zone.center.y + 3.0D, zone.center.z + 3.6D
                    );
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, cageBox, LivingEntity::isAlive)) {
                        if (foe == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) continue;

                        Vec3 pullIn = zone.center.subtract(foe.position()).scale(0.30D);
                        foe.setDeltaMovement(pullIn.x, 0.12D, pullIn.z);
                        foe.hasImpulse = true;
                        SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 4.0D, 0.0D, 120L);
                        foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
                        EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, srv.getGameTime() + 80L);

                        if (zone.spell != null && zone.slot != null) {
                            SpellExecutionEngine.deliverSubSpellOnEntity(
                                    srv, owner, foe, Vec3.ZERO, zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency
                            );
                        }
                        if (zone.slot == null || !zone.slot.hasHarmVerb()) {
                            SpellCombatReactionEngine.hurtWithAegisMateriaAdvantage(srv, foe, owner, vaultDmg, false, true);
                        }
                    }

                    // Active 4.0s (80t) projectile barrier blocking hostile projectile line-of-sight across the vault!
                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 4.0D, 80, srv.getGameTime(), null, null, EssenceType.AEGIS, 1.0F,
                            (bLvl, bZone, bEl) -> {
                                if (bEl > bZone.maxDurationTicks) return false;
                                LivingEntity bOwner = bZone.resolveCaster(bLvl);
                                AABB pBox = new AABB(
                                        bZone.center.x - 4.0D, bZone.center.y - 0.5D, bZone.center.z - 4.0D,
                                        bZone.center.x + 4.0D, bZone.center.y + 3.2D, bZone.center.z + 4.0D
                                );
                                for (Projectile p : bLvl.getEntitiesOfClass(Projectile.class, pBox)) {
                                    Entity pOwn = p.getOwner();
                                    if (pOwn == bOwner || (pOwn instanceof LivingEntity l && SpellExecutionEngine.isFriendlyAllyForOasis(bOwner, l))) {
                                        continue;
                                    }
                                    p.discard();
                                }
                                return true;
                            }
                    ));

                    srv.playSound(null, cPos, SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.9F, 1.15F);
                    triggersLeft[0]--;
                    rearmCooldown[0] = 95;
                    return triggersLeft[0] > 0;
                }
        ).markDisarmableGlyph();
        SpellLingeringZoneManager.registerZone(level, glyphZone);
        return true;
    }

    // =========================================================================
    // FORM MUTATION 7 — GILDED GRANITE SHRAPNEL DELUGE (CONE / SPRAY)
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
        // Section 6.35 Gilded Granite Shrapnel Deluge:
        // Wide 60° cone of jagged quartz shards & dense granite shrapnel (#FFEA78 / #773214); knocks enemies 6 blocks
        // backward flat for 1.0s (20t), shreds 3 armor points, smites undead (+50%), and deflects incoming arrows
        // and projectiles mid-flight!
        int tier = spell.getEffectiveTier();
        boolean wardSafe = spell.hasWardFriendlyFireSafeguard();

        SpellWorldVfxManager.broadcastVortexCone(
                level, caster.getEyePosition().subtract(0.0D, 0.2D, 0.0D), direction, coneRange,
                EssenceType.AEGIS, EssenceType.RADIANT, tier
        );

        float smiteBonus = 0.50F * MateriaStageTable.getBaseCombatDamageHp(tier) * Math.max(0.5F, apparatusPotency);
        float shrapnelDmg = (2.85F + tier * 0.72F) * Math.max(0.5F, apparatusPotency);

        for (LivingEntity hit : hitTargets) {
            if (SpellExecutionEngine.isFriendlyAllyForOasis(caster, hit)) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, wardSafe)) continue;

            hit.setDeltaMovement(direction.x * 1.48D, 0.34D, direction.z * 1.48D);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 20, 0));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 120L);

            boolean undead = hit.getType().is(EntityTypeTags.UNDEAD);
            if (undead) {
                hit.igniteForSeconds(5);
            }
            if (slot.hasHarmVerb()) {
                if (undead) {
                    SpellCombatReactionEngine.hurtWithAegisMateriaAdvantage(
                            level, hit, caster, Math.max(3.5F, smiteBonus), false, true
                    );
                }
            } else {
                SpellCombatReactionEngine.hurtWithAegisMateriaAdvantage(
                        level, hit, caster, shrapnelDmg * (undead ? 1.50F : 1.0F), false, true
                );
            }
        }

        AABB coneBox = caster.getBoundingBox().expandTowards(direction.scale(coneRange)).inflate(2.5D);
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, coneBox)) {
            Entity owner = proj.getOwner();
            if (owner == caster || (owner instanceof LivingEntity l && SpellExecutionEngine.isFriendlyAllyForOasis(caster, l))) {
                continue;
            }
            proj.setDeltaMovement(direction.scale(Math.max(1.25D, proj.getDeltaMovement().length())));
            proj.setOwner(caster);
            proj.hasImpulse = true;
        }

        Vec3 mid = caster.getEyePosition().add(direction.scale(coneRange * 0.5D));
        level.sendParticles(ParticleTypes.WAX_ON, mid.x, mid.y, mid.z, 24, 1.4D, 0.5D, 1.4D, 0.05D);
    }

    // =========================================================================
    // FORM MUTATION 8 — GILDED CITADEL MEGALITH CATACLYSM (SKYFALL)
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
        // Section 6.35 Gilded Citadel Megalith Cataclysm:
        // Calls down a colossal monolithic fortress pillar of golden granite from above after a 2.0s (40t) telegraph;
        // crushes the impact epicenter with catastrophic Aegis Materia damage, detonates a 14m shockwave knocking mobs
        // flat, smites undead, and leaves a temporary golden granite rampart monument!
        int tier = spell.getEffectiveTier();
        double r = Math.max(14.0D, radius);
        BlockPos centerPos = BlockPos.containing(impactCenter);
        double skyH = SpellDeliveryForm.probeCaveSafeSkyfallHeight(level, centerPos, tier);

        SpellWorldVfxManager.broadcastSkyfallPillar(
                level, impactCenter, r, EssenceType.AEGIS, EssenceType.RADIANT, 160, tier
        );

        int colHeight = Math.clamp(3 + tier / 3, 3, 6);
        int baseDuration = 160;
        int effectiveDuration = SpellWorldVfxManager.resolveEffectiveZoneDurationTicks(spell, EssenceType.AEGIS, baseDuration);
        int[][] cross = {{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        BlockPos[] colBases = new BlockPos[cross.length];
        BlockState[] colStates = new BlockState[cross.length];
        int[] colHeights = new int[cross.length];
        for (int i = 0; i < cross.length; i++) {
            int dx = cross[i][0];
            int dz = cross[i][1];
            colBases[i] = SpellWorldVfxManager.findSkyfallSurfaceBase(level, centerPos.offset(dx, 0, dz));
            colStates[i] = (dx == 0 && dz == 0)
                    ? Blocks.QUARTZ_PILLAR.defaultBlockState()
                    : Blocks.POLISHED_GRANITE.defaultBlockState();
            colHeights[i] = (dx == 0 && dz == 0) ? colHeight : Math.max(2, colHeight - 1);
            SpellWorldVfxManager.broadcastSkyfallPseudoblockColumn(
                    level,
                    new Vec3(colBases[i].getX() + 0.5D, colBases[i].getY() + skyH, colBases[i].getZ() + 0.5D),
                    colBases[i],
                    colStates[i],
                    colHeights[i],
                    40,
                    Math.max(4, effectiveDuration - 40),
                    EssenceType.AEGIS,
                    tier
            );
        }

        final boolean[] materialized = {false};
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, baseDuration, level.getGameTime(), spell, slot, EssenceType.AEGIS, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed >= zone.maxDurationTicks) {
                        if (!materialized[0]) {
                            materialized[0] = true;
                            for (int i = 0; i < cross.length; i++) {
                                SpellWorldVfxManager.materializeSkyfallPseudoblockColumn(srv, colBases[i], colStates[i], colHeights[i]);
                            }
                        }
                        return false;
                    }
                    if (elapsed < 40) {
                        if (elapsed % 4 == 0) {
                            srv.sendParticles(ParticleTypes.WAX_ON,
                                    zone.center.x, zone.center.y + 6.5D - elapsed * 0.14D, zone.center.z,
                                    12, 0.7D, 0.8D, 0.7D, 0.03D);
                            srv.sendParticles(ParticleTypes.END_ROD,
                                    zone.center.x, zone.center.y + 6.5D - elapsed * 0.14D, zone.center.z,
                                    8, 0.6D, 0.6D, 0.6D, 0.02D);
                        }
                        return true;
                    }
                    for (int i = 0; i < cross.length; i++) {
                        SpellWorldVfxManager.repelEntitiesFromPseudoblockColumn(srv, colBases[i], colHeights[i], elapsed == 40);
                    }
                    if (elapsed == 40) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        boolean wardSafe = zone.spell != null && zone.spell.hasWardFriendlyFireSafeguard();
                        int zTier = zone.spell != null ? zone.spell.getEffectiveTier() : tier;

                        SpellWorldVfxManager.broadcastRadialNova(
                                srv, zone.center, zone.radius, EssenceType.AEGIS, EssenceType.RADIANT, zTier
                        );

                        AABB shockBox = new AABB(
                                zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 4.5D, zone.center.z + zone.radius
                        );
                        float megalithDmg = (7.40F + zTier * 1.80F) * Math.max(0.5F, zone.apparatusPotency);

                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, shockBox, LivingEntity::isAlive)) {
                            if (foe == owner || SpellExecutionEngine.isFriendlyAllyForOasis(owner, foe)) continue;
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, wardSafe)) continue;

                            Vec3 delta = foe.position().subtract(zone.center);
                            boolean inEpicenter = delta.horizontalDistanceSqr() <= 14.0D;
                            Vec3 away = delta.lengthSqr() > 1.0E-4D ? delta.normalize().scale(1.62D) : new Vec3(0.0D, 0.44D, 0.0D);
                            foe.setDeltaMovement(away.x, 0.44D, away.z);
                            foe.hasImpulse = true;
                            foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 25, 0));

                            boolean undead = foe.getType().is(EntityTypeTags.UNDEAD);
                            if (undead) {
                                foe.igniteForSeconds(6);
                            }
                            if (zone.spell != null && zone.slot != null) {
                                SpellExecutionEngine.deliverSubSpellOnEntity(
                                        srv, owner, foe, new Vec3(0.0D, -1.0D, 0.0D),
                                        zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * (inEpicenter ? 1.35F : 0.85F)
                                );
                            }
                            float hitDmg = megalithDmg * (inEpicenter ? 1.35F : 0.75F) * (undead ? 1.50F : 1.0F);
                            SpellCombatReactionEngine.hurtWithAegisMateriaAdvantage(srv, foe, owner, hitDmg, false, true);
                        }

                        BlockPos cPos = BlockPos.containing(zone.center);
                        srv.sendParticles(ParticleTypes.WAX_ON, zone.center.x, zone.center.y + 1.2D, zone.center.z,
                                42, 2.2D, 1.2D, 2.2D, 0.08D);
                        srv.playSound(null, cPos, SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.25F, 0.85F);
                    }
                    return true;
                }
        ));
        return true;
    }

    // =========================================================================
    // FORM MUTATION 9 — GILDED PALADIN-COLOSSUS EIDOLON SUITE (SUMMON)
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
        tag.putString("SpellEidolonMateria", EssenceType.AEGIS.name());
        tag.putInt("SpellEidolonTintRGB", GILDED_PALADIN_HEX_COLOR);
        SpellWorldVfxManager.broadcastArcaneSeal(
                level, spawnPos.add(0.0D, 0.03D, 0.0D), 1.45D, EssenceType.AEGIS, EssenceType.RADIANT, 36, spell.getEffectiveTier()
        );
        level.sendParticles(ParticleTypes.WAX_ON, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 24, 0.45D, 0.55D, 0.45D, 0.05D);
    }

    // =========================================================================
    // PART 3: CORE EFFECT MUTATIONS — BLINK, TRANSMUTE & AFFLICTION
    // =========================================================================

    @Override
    public void onBlinkCast(
            ServerLevel level,
            LivingEntity caster,
            Vec3 departurePos,
            Vec3 arrivalPos,
            int potencyTier
    ) {
        // Section 6.35 Bulwark Phalanx Step:
        // Teleports up to 12 blocks with heavy kinetic weight, leaving behind an unbreakable 1-block golden stone
        // tower shield decoy that absorbs up to 15 damage, granting the caster +6 Armor and 50% knockback resistance
        // for 4.0s (80t) upon arrival!
        SpellWorldVfxManager.broadcastBlinkRift(level, departurePos, arrivalPos, EssenceType.AEGIS, potencyTier);
        SpellEidolonEntity.spawnShortLivedDecoy(
                level, caster, departurePos, EssenceType.AEGIS,
                15.0F, 100, "Gilded Tower-Shield Decoy", null
        );
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 6.0D, 1.0D, 0.50D, 0.0D, 80L);
        level.sendParticles(ParticleTypes.WAX_ON, arrivalPos.x, arrivalPos.y + 0.8D, arrivalPos.z, 18, 0.4D, 0.5D, 0.4D, 0.04D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.SHIELD_BLOCK.value(), SoundSource.PLAYERS, 0.9F, 1.2F);
    }

    @Override
    public boolean onTransmuteBlock(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos pos,
            BlockState state,
            int potencyTier
    ) {
        // Section 6.35 Gilded Petrofaction Transmute:
        // Turns cobblestone/deepslate into smooth granite, calcite, or quartz blocks; converts dirt/gravel into clay;
        // petrifies rotten flesh/bones into bone blocks; and cleanses equipment curse enchantments!
        boolean didAny = false;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.5D))) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) {
                didAny = true;
            }
        }
        if (caster != null) {
            ItemStack offhand = caster.getItemInHand(InteractionHand.OFF_HAND);
            if (!offhand.isEmpty() && stripCursesFromStack(offhand)) {
                level.sendParticles(ParticleTypes.WAX_ON, caster.getX(), caster.getY(0.9D), caster.getZ(), 14, 0.3D, 0.3D, 0.3D, 0.05D);
                maybeGrantAegisBatchTransmuteDiscount(level, caster, potencyTier);
                didAny = true;
            }
        }

        if (state.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, pos, state)) {
            return didAny;
        }

        BlockState petrified = null;
        if (state.is(Blocks.COBBLESTONE) || state.is(Blocks.STONE) || state.is(Blocks.GRANITE)) {
            petrified = Blocks.POLISHED_GRANITE.defaultBlockState();
        } else if (state.is(Blocks.DEEPSLATE) || state.is(Blocks.COBBLED_DEEPSLATE)) {
            petrified = Blocks.CALCITE.defaultBlockState();
        } else if (state.is(Blocks.NETHERRACK) || state.is(Blocks.BLACKSTONE) || state.is(Blocks.TUFF)) {
            petrified = Blocks.QUARTZ_BLOCK.defaultBlockState();
        } else if (state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.GRAVEL)) {
            petrified = Blocks.CLAY.defaultBlockState();
        } else if (state.is(BlockTags.SMALL_FLOWERS) && !state.is(ModBlocks.AEGIS_ROSE.get())) {
            petrified = ModBlocks.AEGIS_ROSE.get().defaultBlockState();
        }

        if (petrified != null) {
            level.setBlockAndUpdate(pos, petrified);
            level.sendParticles(ParticleTypes.WAX_ON, pos.getX() + 0.5D, pos.getY() + 0.65D, pos.getZ() + 0.5D,
                    8, 0.25D, 0.25D, 0.25D, 0.03D);
            maybeGrantAegisBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        return didAny;
    }

    @Override
    public boolean onTransmuteDroppedItem(
            ServerLevel level,
            @Nullable LivingEntity caster,
            ItemEntity itemEntity,
            int potencyTier
    ) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;

        ItemStack out = ItemStack.EMPTY;
        if (stack.is(Items.ROTTEN_FLESH) || stack.is(Items.BONE)) {
            out = new ItemStack(Items.BONE_BLOCK, Math.max(1, (stack.getCount() + 1) / 2));
        } else if (stack.is(Items.COBBLESTONE) || stack.is(Items.STONE) || stack.is(Items.GRANITE)) {
            out = new ItemStack(Items.POLISHED_GRANITE, stack.getCount());
        } else if (stack.is(Items.DEEPSLATE) || stack.is(Items.COBBLED_DEEPSLATE)) {
            out = new ItemStack(Items.CALCITE, stack.getCount());
        } else if (stack.is(Items.NETHERRACK) || stack.is(Items.BLACKSTONE) || stack.is(Items.TUFF)) {
            out = new ItemStack(Items.QUARTZ_BLOCK, stack.getCount());
        } else if (stack.is(Items.DIRT) || stack.is(Items.COARSE_DIRT) || stack.is(Items.GRAVEL)) {
            out = new ItemStack(Items.CLAY, stack.getCount());
        } else if (stack.is(ItemTags.SMALL_FLOWERS) && !stack.is(ModBlocks.AEGIS_ROSE.get().asItem())) {
            out = new ItemStack(ModBlocks.AEGIS_ROSE.get().asItem(), stack.getCount());
        }

        if (!out.isEmpty()) {
            itemEntity.setItem(out);
            level.sendParticles(ParticleTypes.WAX_ON, itemEntity.getX(), itemEntity.getY(0.3D), itemEntity.getZ(),
                    8, 0.2D, 0.2D, 0.2D, 0.03D);
            maybeGrantAegisBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }

        if (stripCursesFromStack(stack)) {
            itemEntity.setItem(stack);
            level.sendParticles(ParticleTypes.WAX_ON, itemEntity.getX(), itemEntity.getY(0.3D), itemEntity.getZ(),
                    14, 0.25D, 0.25D, 0.25D, 0.05D);
            maybeGrantAegisBatchTransmuteDiscount(level, caster, potencyTier);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Section 6.35 Petrifying Citadel Sundering Hex:
        // Persistent milk-immune calcifying curse (cleansed via Restoration/Vitae magic, Umbral darkness, or caster
        // defeat): permanently shreds 40% of target armor toughness, cuts movement speed by 40%, blocks jumping,
        // amplifies incoming blunt damage by +35%, and triggers Fractured Bastion (+40% armor-ignoring damage) on Players!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.20D * mult, Math.min(0.85D, 0.40D * mult), 45L);
        SpellCombatReactionEngine.applyTimedMovementSpeedSap(target, level, 0.40D, 45L);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, Math.min(4, Math.max(1, Math.round(1.5F * mult))), true, true));
        EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, level.getGameTime() + 45L);

        LivingEntity hexCaster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        float hexDmg = (1.40F + potencyTier * 0.46F) * mult;
        SpellCombatReactionEngine.hurtWithAegisMateriaAdvantage(level, target, hexCaster, hexDmg, false, true);

        level.sendParticles(ParticleTypes.WAX_ON, target.getX(), target.getY(0.85D), target.getZ(), 4, 0.25D, 0.3D, 0.25D, 0.02D);
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
            float heal = 3.2F + potencyTier * 0.95F;
            SpellCombatReactionEngine.notifyMagicalHeal(target, heal);
            target.heal(heal);
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private static boolean stripCursesFromStack(ItemStack stack) {
        boolean[] stripped = {false};
        EnchantmentHelper.updateEnchantments(stack, mutable ->
                mutable.removeIf(holder -> {
                    if (holder.is(EnchantmentTags.CURSE)) {
                        stripped[0] = true;
                        return true;
                    }
                    return false;
                })
        );
        return stripped[0];
    }

    private static void placeTemporaryStateMap(ServerLevel level, Map<BlockPos, BlockState> desired, int durationTicks) {
        Map<BlockPos, BlockState> saved = new HashMap<>();
        List<BlockState> placedTypes = new ArrayList<>();
        for (Map.Entry<BlockPos, BlockState> entry : desired.entrySet()) {
            BlockPos p = entry.getKey();
            BlockState cur = level.getBlockState(p);
            if ((cur.isAir() || cur.canBeReplaced()) && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, cur)) {
                saved.put(p.immutable(), cur);
                placedTypes.add(entry.getValue());
                level.setBlockAndUpdate(p, entry.getValue());
            }
        }
        if (saved.isEmpty()) return;
        BlockPos anchor = saved.keySet().iterator().next();
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                null, Vec3.atCenterOf(anchor), 4.0D, durationTicks, level.getGameTime(), null, null, EssenceType.AEGIS, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed < zone.maxDurationTicks) return true;
                    for (Map.Entry<BlockPos, BlockState> e : saved.entrySet()) {
                        BlockState cur = srv.getBlockState(e.getKey());
                        boolean matchesPlaced = false;
                        for (BlockState pt : placedTypes) {
                            if (cur.is(pt.getBlock())) {
                                matchesPlaced = true;
                                break;
                            }
                        }
                        if (matchesPlaced) {
                            srv.setBlockAndUpdate(e.getKey(), e.getValue());
                        }
                    }
                    return false;
                }
        ));
    }

    private static void maybeGrantAegisBatchTransmuteDiscount(ServerLevel level, @Nullable LivingEntity caster, int tier) {
        if (!(caster instanceof Player player)) return;
        CompoundTag pData = EntityHelper.getPersistentData(player);
        long now = level.getGameTime();
        long prevTick = pData.getLong("SpellAegisBatchTick").orElse(-20L);
        int count = (prevTick == now) ? pData.getInt("SpellAegisBatchCount").orElse(0) + 1 : 1;
        pData.putLong("SpellAegisBatchTick", now);
        pData.putInt("SpellAegisBatchCount", count);
        if (count == 2) {
            long refund = Math.max(1L, Math.round(MateriaStageTable.getBaseMfumCastCost(tier) * 0.25D));
            SpellFuelService.restorePlayerMateriaFuel(player, EssenceType.AEGIS, refund);
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
