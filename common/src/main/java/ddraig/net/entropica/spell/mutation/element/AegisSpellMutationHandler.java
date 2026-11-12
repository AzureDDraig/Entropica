package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.SpellEidolonEntity;
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
import ddraig.net.entropica.spell.fuel.MateriaStageTable;
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
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Complete per-part Signature Mutation implementation for <b>Aegis Materia (Ethereal &amp; Paradox Fusion #34)</b>
 * (Spellcrafting System Guide Section 6.35 — Radiant + Earth [Leans: Earth]).
 */
public final class AegisSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int GILDED_PALADIN_HEX_COLOR = 0xE2A83C;

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
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
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

    // --- Part 1: Flow Mutations (Gilded Bastion Orbit Flow) ---

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.WAX_ON, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.04D, 0.04D, 0.04D, 0.01D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.WAX_ON, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.05D, 0.05D, 0.05D, 0.01D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        // Gilded Bastion Shield-Slug: knocks target 5m back, roots in golden earth 2.5s (50t),
        // and raises a temporary 1-block golden stone rampart behind them (restored after 5.0s / 100t)!
        if (!(hitEntity instanceof LivingEntity living) || !living.isAlive()) return;
        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard())) return;

        Vec3 dir = projectile.getDeltaMovement().lengthSqr() > 0.001D
                ? projectile.getDeltaMovement().normalize()
                : (caster != null ? caster.getLookAngle().normalize() : new Vec3(0, 0, 1));
        living.setDeltaMovement(dir.x * 1.25D, 0.30D, dir.z * 1.25D);
        living.hasImpulse = true;
        living.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 50, 0));
        living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 5));
        EntityHelper.getPersistentData(living).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, level.getGameTime() + 50L);

        BlockPos rampartPos = BlockPos.containing(living.position().add(dir.x * 1.3D, 0.0D, dir.z * 1.3D));
        placeTemporaryPillars(level, List.of(rampartPos), Blocks.GILDED_BLACKSTONE.defaultBlockState(), 100);
        level.playSound(null, living.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.75F, 1.35F);
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
        // Citadel Bastion Tether Beam: repels enemies backward & shreds 1 armor/s;
        // when channeled on an ally, anchors them with +8 Armor and 50% damage reduction!
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            boolean isAlly = hit != caster && (hit.isAlliedTo(caster) || (hit instanceof Player && !(hit instanceof Monster)));
            if (isAlly) {
                linkAegisBastionTether(level, caster, hit, now);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.setDeltaMovement(direction.x * 0.70D, 0.20D, direction.z * 0.70D);
            hit.hasImpulse = true;
            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("AegisBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("AegisBeamStacks").orElse(0) + 1 : 1;
            data.putLong("AegisBeamLastTick", now);
            data.putInt("AegisBeamStacks", stacks);
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, Math.min(8.0D, stacks), 0.0D, 80L);
        }

        // Also scan beam corridor for friendly allies in case hitEntities only collected offensive targets
        AABB rayBox = caster.getBoundingBox().expandTowards(direction.scale(Math.min(16.0D, maxReach))).inflate(1.0D);
        for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, rayBox,
                e -> e != caster && e.isAlive() && (e.isAlliedTo(caster) || (e instanceof Player && !(e instanceof Monster))))) {
            linkAegisBastionTether(level, caster, ally, now);
        }
    }

    private static void linkAegisBastionTether(ServerLevel level, LivingEntity caster, LivingEntity ally, long now) {
        CompoundTag data = EntityHelper.getPersistentData(ally);
        data.putString(SpellLingeringZoneManager.TAG_AEGIS_TETHER_GUARD_UUID, caster.getUUID().toString());
        data.putLong(SpellLingeringZoneManager.TAG_AEGIS_TETHER_UNTIL, now + 60L);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(ally, level, 8.0D, 2.0D, 0.50D, 0.0D, 60L);
        level.sendParticles(ParticleTypes.WAX_ON, ally.getX(), ally.getY(1.0D), ally.getZ(), 8, 0.3D, 0.4D, 0.3D, 0.03D);
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
        // Gilded Bulwark Rampart Nova: 6m blast — hurls enemies 8m back, smites undead (+50%),
        // raises temporary barrier spikes, and grants caught allies +4 Armor & 50% knockback resistance!
        double r = Math.max(6.0D, radius);
        float smiteBonus = 0.50F * MateriaStageTable.getBaseCombatDamageHp(spell.getEffectiveTier()) * Math.max(0.5F, apparatusPotency);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.5D, center.z + r);

        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 4.0D, 0.0D, 0.50D, 0.0D, 120L);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (e == caster) continue;
            if (e.isAlliedTo(caster) || (e instanceof Player && !(e instanceof Monster) && spell.hasWardFriendlyFireSafeguard())) {
                SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(e, level, 4.0D, 0.0D, 0.50D, 0.0D, 120L);
                continue;
            }
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = e.position().subtract(center).normalize().scale(1.85D);
            e.setDeltaMovement(away.x, 0.44D, away.z);
            e.hasImpulse = true;
            if (e.getType().is(EntityTypeTags.UNDEAD)) {
                e.igniteForSeconds(5);
                e.invulnerableTime = 0;
                e.hurtServer(level, level.damageSources().indirectMagic(caster, caster), Math.max(3.5F, smiteBonus));
            }
        }

        BlockPos cPos = BlockPos.containing(center);
        placeTemporaryPillars(level, List.of(
                cPos.offset(3, 0, 0), cPos.offset(-3, 0, 0),
                cPos.offset(0, 0, 3), cPos.offset(0, 0, -3)
        ), Blocks.QUARTZ_PILLAR.defaultBlockState(), 100);
        level.sendParticles(ParticleTypes.WAX_ON, center.x, center.y + 0.8D, center.z, 32, 2.2D, 0.5D, 2.2D, 0.06D);
        level.playSound(null, cPos, SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.9F, 1.25F);
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
        // Gilded Shield-Bash Strike: 6m knockback, stuns 1.5s (30t), smites undead (+50%), shreds 3 armor points,
        // and bolsters caster with +4 Armor for 5.0s (100t)!
        float smiteBonus = 0.50F * MateriaStageTable.getBaseCombatDamageHp(spell.getEffectiveTier()) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.setDeltaMovement(direction.x * 1.45D, 0.36D, direction.z * 1.45D);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 120L);
            if (hit.getType().is(EntityTypeTags.UNDEAD)) {
                hit.igniteForSeconds(5);
                hit.invulnerableTime = 0;
                hit.hurtServer(level, level.damageSources().indirectMagic(caster, caster), Math.max(3.5F, smiteBonus));
            }
        }
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 4.0D, 0.0D, 0.25D, 0.0D, 100L);
        level.playSound(null, caster.blockPosition(), SoundEvents.SHIELD_BLOCK.value(), SoundSource.PLAYERS, 1.0F, 1.15F);
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
        // Gilded Bastion Citadel Mantle: +10 Armor, +4 Toughness, 75% knockback resistance, -10% movement speed,
        // Blindness/Darkness immunity, -25% physical damage reduction & 40% melee damage reflection!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_AEGIS_MANTLE_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 10.0D, 4.0D, 0.75D, -0.10D, dur);
        caster.removeEffect(MobEffects.BLINDNESS);
        caster.removeEffect(MobEffects.DARKNESS);
        level.sendParticles(ParticleTypes.WAX_ON, caster.getX(), caster.getY(0.9D), caster.getZ(), 22, 0.45D, 0.55D, 0.45D, 0.04D);
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
        // Citadel Rampart Vault Mine: erupts into a 4m circular ring of temporary golden granite spires (4.0s / 80t),
        // trapping mobs inside, shredding 4 armor points, and blocking projectile LoS!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 4.0D, 300, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.WAX_ON, zone.center.x, zone.center.y + 0.15D, zone.center.z, 5, 1.0D, 0.1D, 1.0D, 0.02D);
                    }
                    if (elapsed < 10) return true;
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB triggerBox = new AABB(zone.center.x - 2.4D, zone.center.y - 1.0D, zone.center.z - 2.4D,
                            zone.center.x + 2.4D, zone.center.y + 2.0D, zone.center.z + 2.4D);
                    List<LivingEntity> triggers = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e -> e.isAlive() && e != owner
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard())));
                    if (triggers.isEmpty()) return true;

                    BlockPos cPos = BlockPos.containing(zone.center);
                    List<BlockPos> ring = List.of(
                            cPos.offset(3, 0, 0), cPos.offset(3, 1, 0),
                            cPos.offset(-3, 0, 0), cPos.offset(-3, 1, 0),
                            cPos.offset(0, 0, 3), cPos.offset(0, 1, 3),
                            cPos.offset(0, 0, -3), cPos.offset(0, 1, -3),
                            cPos.offset(2, 0, 2), cPos.offset(-2, 0, 2),
                            cPos.offset(2, 0, -2), cPos.offset(-2, 0, -2)
                    );
                    placeTemporaryPillars(srv, ring, Blocks.POLISHED_GRANITE.defaultBlockState(), 80);

                    AABB cageBox = new AABB(zone.center.x - 3.5D, zone.center.y - 1.0D, zone.center.z - 3.5D,
                            zone.center.x + 3.5D, zone.center.y + 2.8D, zone.center.z + 3.5D);
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, cageBox, e -> e.isAlive() && e != owner)) {
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                        SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 4.0D, 0.0D, 100L);
                        foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, foe, Vec3.ZERO, zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                    }
                    srv.playSound(null, cPos, SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.9F, 1.15F);
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
        // Gilded Granite Shrapnel Deluge: 6m knockback + 1.0s stun (20t), shreds 3 armor points,
        // smites undead (+50%), and deflects incoming projectiles mid-flight!
        float smiteBonus = 0.50F * MateriaStageTable.getBaseCombatDamageHp(spell.getEffectiveTier()) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.setDeltaMovement(direction.x * 1.45D, 0.34D, direction.z * 1.45D);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 20, 0));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 120L);
            if (hit.getType().is(EntityTypeTags.UNDEAD)) {
                hit.igniteForSeconds(5);
                hit.invulnerableTime = 0;
                hit.hurtServer(level, level.damageSources().indirectMagic(caster, caster), Math.max(3.5F, smiteBonus));
            }
        }
        AABB coneBox = caster.getBoundingBox().expandTowards(direction.scale(coneRange)).inflate(2.5D);
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, coneBox)) {
            if (proj.getOwner() == caster) continue;
            proj.setDeltaMovement(direction.scale(Math.max(1.2D, proj.getDeltaMovement().length())));
            proj.setOwner(caster);
            proj.hasImpulse = true;
        }
        Vec3 mid = caster.getEyePosition().add(direction.scale(coneRange * 0.5D));
        level.sendParticles(ParticleTypes.WAX_ON, mid.x, mid.y, mid.z, 24, 1.4D, 0.5D, 1.4D, 0.05D);
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
        // Gilded Citadel Megalith Cataclysm: 2.0s (40t) telegraph -> monolithic golden granite pillar crush:
        // heavy Aegis damage, 14m shockwave knocking mobs flat, smites undead, leaves temporary golden granite monument (restored after 6.0s)!
        double r = Math.max(10.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 45, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed < 40) {
                        if (elapsed % 4 == 0) {
                            srv.sendParticles(ParticleTypes.WAX_ON, zone.center.x, zone.center.y + 6.5D - elapsed * 0.14D, zone.center.z, 12, 0.7D, 0.8D, 0.7D, 0.03D);
                            srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 6.5D - elapsed * 0.14D, zone.center.z, 8, 0.6D, 0.6D, 0.6D, 0.02D);
                        }
                        return true;
                    }
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB shockBox = new AABB(zone.center.x - 14.0D, zone.center.y - 2.0D, zone.center.z - 14.0D,
                            zone.center.x + 14.0D, zone.center.y + 4.5D, zone.center.z + 14.0D);
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, shockBox, e -> e.isAlive() && e != owner)) {
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                        Vec3 away = foe.position().subtract(zone.center).normalize().scale(1.55D);
                        foe.setDeltaMovement(away.x, 0.42D, away.z);
                        foe.hasImpulse = true;
                        foe.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 25, 0));
                        if (foe.getType().is(EntityTypeTags.UNDEAD)) {
                            foe.igniteForSeconds(6);
                            foe.hurtServer(srv, srv.damageSources().magic(), 5.0F + zone.spell.getEffectiveTier() * 1.4F);
                        }
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, foe, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 1.35F);
                    }

                    BlockPos cPos = BlockPos.containing(zone.center);
                    placeTemporaryPillars(srv, List.of(
                            cPos, cPos.above(), cPos.above(2)
                    ), Blocks.QUARTZ_PILLAR.defaultBlockState(), 120);
                    srv.sendParticles(ParticleTypes.WAX_ON, zone.center.x, zone.center.y + 1.2D, zone.center.z, 40, 2.2D, 1.2D, 2.2D, 0.08D);
                    srv.playSound(null, cPos, SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.25F, 0.85F);
                    return false;
                }
        ));
        return true;
    }

    @Override
    public void onSummonFormSpawned(ServerLevel level, LivingEntity caster, Entity summonEntity, Vec3 spawnPos, double radius, SpellAssembly spell, SubSpellSlot slot) {
        CompoundTag tag = EntityHelper.getPersistentData(summonEntity);
        tag.putString("SpellEidolonMateria", EssenceType.AEGIS.name());
        tag.putInt("SpellEidolonTintRGB", GILDED_PALADIN_HEX_COLOR);
        level.sendParticles(ParticleTypes.WAX_ON, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 24, 0.45D, 0.55D, 0.45D, 0.05D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Bulwark Phalanx Step: 12m heavy step leaving a 15-HP golden stone tower-shield decoy at departure,
        // and granting caster +6 Armor and 50% knockback resistance for 4.0s (80t)!
        SpellEidolonEntity.spawnShortLivedDecoy(
                level, caster, departurePos, EssenceType.AEGIS,
                15.0F, 100, "Gilded Tower-Shield Decoy", null
        );
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 6.0D, 1.0D, 0.50D, 0.0D, 80L);
        level.sendParticles(ParticleTypes.WAX_ON, arrivalPos.x, arrivalPos.y + 0.8D, arrivalPos.z, 18, 0.4D, 0.5D, 0.4D, 0.04D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.SHIELD_BLOCK.value(), SoundSource.PLAYERS, 0.9F, 1.2F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Gilded Petrofaction Transmute: cobble/deepslate -> polished granite / calcite / quartz, dirt/gravel -> clay,
        // flowers -> Aegis Rose, and cleanses curse enchantments on dropped equipment!
        boolean didAny = false;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.5D))) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) {
                didAny = true;
            }
        }
        if (state.is(Blocks.COBBLESTONE)) {
            level.setBlockAndUpdate(pos, Blocks.POLISHED_GRANITE.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.DEEPSLATE) || state.is(Blocks.COBBLED_DEEPSLATE)) {
            level.setBlockAndUpdate(pos, Blocks.CALCITE.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.NETHERRACK) || state.is(Blocks.BLACKSTONE)) {
            level.setBlockAndUpdate(pos, Blocks.QUARTZ_BLOCK.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.GRAVEL)) {
            level.setBlockAndUpdate(pos, Blocks.CLAY.defaultBlockState());
            return true;
        }
        if (state.is(BlockTags.SMALL_FLOWERS) && !state.is(ModBlocks.AEGIS_ROSE.get())) {
            level.setBlockAndUpdate(pos, ModBlocks.AEGIS_ROSE.get().defaultBlockState());
            return true;
        }
        return didAny;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;

        if (stack.is(Items.ROTTEN_FLESH) || stack.is(Items.BONE)) {
            itemEntity.setItem(new ItemStack(Items.BONE_BLOCK, Math.max(1, stack.getCount() / 2)));
            return true;
        }
        if (stack.is(Items.COBBLESTONE)) {
            itemEntity.setItem(new ItemStack(Items.POLISHED_GRANITE, stack.getCount()));
            return true;
        }
        if (stack.is(Items.DEEPSLATE) || stack.is(Items.COBBLED_DEEPSLATE)) {
            itemEntity.setItem(new ItemStack(Items.CALCITE, stack.getCount()));
            return true;
        }
        if (stack.is(Items.DIRT) || stack.is(Items.GRAVEL)) {
            itemEntity.setItem(new ItemStack(Items.CLAY, stack.getCount()));
            return true;
        }
        if (stack.is(ItemTags.SMALL_FLOWERS) && !stack.is(ModBlocks.AEGIS_ROSE.get().asItem())) {
            itemEntity.setItem(new ItemStack(ModBlocks.AEGIS_ROSE.get().asItem(), stack.getCount()));
            return true;
        }

        boolean[] strippedCurse = {false};
        EnchantmentHelper.updateEnchantments(stack, mutable ->
                mutable.removeIf(holder -> {
                    if (holder.is(EnchantmentTags.CURSE)) {
                        strippedCurse[0] = true;
                        return true;
                    }
                    return false;
                })
        );
        if (strippedCurse[0]) {
            itemEntity.setItem(stack);
            level.sendParticles(ParticleTypes.WAX_ON, itemEntity.getX(), itemEntity.getY(0.3D), itemEntity.getZ(), 14, 0.25D, 0.25D, 0.25D, 0.05D);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Petrifying Citadel Sundering Hex: shreds 40% armor toughness, -40% movement speed (Slowness II),
        // blocks jumping, +35% incoming blunt damage, & Fractured Bastion on Players!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.20D * mult, Math.min(0.80D, 0.40D * mult), 45L);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, Math.min(4, Math.max(1, Math.round(1.5F * mult))), true, true));
        EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, level.getGameTime() + 45L);
        target.hurtServer(level, level.damageSources().magic(), (1.40F + potencyTier * 0.45F) * mult);
        level.sendParticles(ParticleTypes.WAX_ON, target.getX(), target.getY(0.85D), target.getZ(), 4, 0.25D, 0.3D, 0.25D, 0.02D);
        return true;
    }

    private static void placeTemporaryPillars(ServerLevel level, List<BlockPos> positions, BlockState pillarState, int durationTicks) {
        Map<BlockPos, BlockState> saved = new HashMap<>();
        for (BlockPos p : positions) {
            BlockState cur = level.getBlockState(p);
            if ((cur.isAir() || cur.canBeReplaced()) && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, cur)) {
                saved.put(p.immutable(), cur);
                level.setBlockAndUpdate(p, pillarState);
            }
        }
        if (saved.isEmpty()) return;
        BlockPos anchor = saved.keySet().iterator().next();
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                null, Vec3.atCenterOf(anchor), 4.0D, durationTicks, level.getGameTime(), null, null, EssenceType.AEGIS, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed < zone.maxDurationTicks) return true;
                    for (Map.Entry<BlockPos, BlockState> e : saved.entrySet()) {
                        if (srv.getBlockState(e.getKey()).is(pillarState.getBlock())) {
                            srv.setBlockAndUpdate(e.getKey(), e.getValue());
                        }
                    }
                    return false;
                }
        ));
    }
}
