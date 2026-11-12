package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
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
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Complete per-part Signature Mutation implementation for <b>Dawn Materia (Ethereal &amp; Paradox Fusion #32)</b>
 * (Spellcrafting System Guide Section 6.33 — Radiant + Air [Leans: Radiant]).
 */
public final class DawnSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int DAWN_WARDEN_HEX_COLOR = 0xFFD266;

    @Override
    public EssenceType materiaType() {
        return EssenceType.DAWN;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Convective Horizon Orbit Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Solar Horizon Javelin";
            case BEAM -> "Pranic Solar Lance";
            case RADIAL_BURST -> "Sunrise Horizon Nova";
            case TOUCH_STRIKE -> "Aero-Solar Cleave";
            case AURA -> "Dawn-Bringer Solar Shroud";
            case GLYPH -> "Pranic Sunrise Well";
            case CONE -> "Solar Flare Breath";
            case SKYFALL -> "Sunrise Daybreak Tempest";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Solar Zephyr Blink";
            case TRANSMUTE, BREAK -> "Daybreak Awakening Transmute";
            case AFFLICTION -> "Solar Blind-Sear Hex";
            default -> "Sunrise Horizon " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations (Convective Horizon Orbit Flow) ---

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
        level.sendParticles(ParticleTypes.CLOUD, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.02D);
        level.sendParticles(ParticleTypes.CLOUD, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.01D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        // Solar Horizon Javelin: pierces up to 3 targets, pushes struck mobs backward along slipstream,
        // reveals invisible targets (Glowing 5.0s / 100t), and ignites undead in solar fire!
        if (!(hitEntity instanceof LivingEntity living) || !living.isAlive()) return;
        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard())) return;

        Vec3 push = projectile.getDeltaMovement().normalize().scale(0.95D);
        living.setDeltaMovement(push.x, 0.28D, push.z);
        living.hasImpulse = true;
        living.removeEffect(MobEffects.INVISIBILITY);
        living.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
        if (living.getType().is(EntityTypeTags.UNDEAD)) {
            living.igniteForSeconds(5);
            living.hurtServer(level, level.damageSources().magic(), 3.5F + spell.getEffectiveTier() * 1.2F);
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
        // Pranic Solar Lance: blinds & staggers hostiles while siphoning 20% into caster Materia fuel;
        // 3s continuous channeling triggers Sunrise Flash knocking mobs back 6m!
        long now = level.getGameTime();
        boolean siphoned = false;
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            siphoned = true;
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 1));
            if (hit.getType().is(EntityTypeTags.UNDEAD)) {
                hit.igniteForSeconds(4);
            }

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("DawnBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("DawnBeamStacks").orElse(0) + 1 : 1;
            data.putLong("DawnBeamLastTick", now);
            data.putInt("DawnBeamStacks", stacks);
            if (stacks >= 3) {
                data.putInt("DawnBeamStacks", 0);
                hit.setDeltaMovement(direction.x * 1.50D, 0.42D, direction.z * 1.50D);
                hit.hasImpulse = true;
                hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 0.85F, 0.52F), hit.getX(), hit.getY(1.0D), hit.getZ(), 1, 0, 0, 0, 0);
                level.playSound(null, hit.blockPosition(), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.0F, 1.35F);
            }
        }
        if (siphoned && caster instanceof Player player) {
            SpellCombatReactionEngine.siphonCasterMateriaFuel(player, EssenceType.DAWN, spell.getEffectiveTier(), 0.20F);
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
        // Sunrise Horizon Nova: 6m blast, hurls hostiles 8m backward & 3m airborne, 4.0s (80t) Blindness + Glowing,
        // smites undead with double damage, and clears lingering poison clouds & cobwebs without placing light blocks!
        double r = Math.max(6.0D, radius);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 4.0D, center.z + r);
        float undeadDoubleBonus = MateriaStageTable.getBaseCombatDamageHp(spell.getEffectiveTier()) * Math.max(0.5F, apparatusPotency);

        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = foe.position().subtract(center).normalize().scale(1.85D);
            foe.setDeltaMovement(away.x, 0.72D, away.z);
            foe.hasImpulse = true;
            foe.removeEffect(MobEffects.INVISIBILITY);
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
            if (foe.getType().is(EntityTypeTags.UNDEAD)) {
                foe.igniteForSeconds(6);
                foe.invulnerableTime = 0;
                foe.hurtServer(level, level.damageSources().indirectMagic(caster, caster), Math.max(4.0F, undeadDoubleBonus));
            }
        }

        for (AreaEffectCloud cloud : level.getEntitiesOfClass(AreaEffectCloud.class, box)) {
            cloud.discard();
        }
        BlockPos cPos = BlockPos.containing(center);
        int blockRad = Math.min(6, (int) Math.ceil(r));
        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-blockRad, -1, -blockRad), cPos.offset(blockRad, 3, blockRad))) {
            if (level.getBlockState(p).is(Blocks.COBWEB)) {
                level.destroyBlock(p, true);
            }
        }
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 0.9D, center.z, 36, 2.2D, 0.6D, 2.2D, 0.12D);
        level.sendParticles(ParticleTypes.GUST_EMITTER_SMALL, center.x, center.y + 0.8D, center.z, 4, 1.5D, 0.3D, 1.5D, 0.02D);
        level.playSound(null, cPos, SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.15F, 1.25F);
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
        // Aero-Solar Cleave: hits all enemies in a 3m frontal sweep, deflects incoming arrows during swing,
        // knocks enemies back 4m, and grants caster +30% sprint & attack speed for 5.0s (100t)!
        Set<UUID> alreadyHit = new HashSet<>();
        for (LivingEntity hit : hitEntities) {
            alreadyHit.add(hit.getUUID());
            applyAeroSolarCleaveHit(caster, hit, direction, spell);
        }

        AABB sweepBox = caster.getBoundingBox().expandTowards(direction.scale(3.0D)).inflate(1.5D, 0.8D, 1.5D);
        for (LivingEntity extra : level.getEntitiesOfClass(LivingEntity.class, sweepBox)) {
            if (extra == caster || !extra.isAlive() || alreadyHit.contains(extra.getUUID())) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(extra, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellExecutionEngine.deliverSubSpellOnEntity(level, caster, extra, direction, spell, slot, fallbackMateria, apparatusPotency);
            applyAeroSolarCleaveHit(caster, extra, direction, spell);
        }

        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, sweepBox.inflate(0.8D))) {
            if (proj.getOwner() == caster) continue;
            proj.setDeltaMovement(direction.scale(Math.max(1.2D, proj.getDeltaMovement().length())));
            proj.setOwner(caster);
            proj.hasImpulse = true;
        }

        caster.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 1, false, true));
        SpellCombatReactionEngine.applyTimedAttackSpeedBuff(caster, level, 0.30D, 100L);
        Vec3 arcPos = caster.getEyePosition().add(direction.scale(1.8D));
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, arcPos.x, arcPos.y - 0.3D, arcPos.z, 3, 0.5D, 0.2D, 0.5D, 0.0D);
        level.sendParticles(ParticleTypes.END_ROD, arcPos.x, arcPos.y - 0.2D, arcPos.z, 16, 0.8D, 0.3D, 0.8D, 0.08D);
    }

    private static void applyAeroSolarCleaveHit(LivingEntity caster, LivingEntity target, Vec3 direction, SpellAssembly spell) {
        if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(target, caster, spell.hasWardFriendlyFireSafeguard())) return;
        target.setDeltaMovement(direction.x * 1.15D, 0.35D, direction.z * 1.15D);
        target.hasImpulse = true;
        if (target.getType().is(EntityTypeTags.UNDEAD)) {
            target.igniteForSeconds(5);
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
        // Dawn-Bringer Solar Shroud: feather-fall glide, +1.5m jump, Darkness/Blindness immunity,
        // 40% arrow deflection, and 3m gale pushback + 2s blind on melee attackers!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_DAWN_SHROUD_UNTIL, level.getGameTime() + dur);
        caster.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, dur, 1, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, dur, 0, false, true));
        caster.removeEffect(MobEffects.BLINDNESS);
        caster.removeEffect(MobEffects.DARKNESS);
        level.sendParticles(ParticleTypes.END_ROD, caster.getX(), caster.getY(0.9D), caster.getZ(), 22, 0.45D, 0.5D, 0.45D, 0.06D);
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
        // Pranic Sunrise Well: dual-purpose glyph — restores 4 hearts (8 HP) & cleanses allies stepping on it,
        // OR detonates in a concussive gale blast repelling hostiles 8m backward!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 3.0D, 400, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 0.15D, zone.center.z, 5, 0.9D, 0.1D, 0.9D, 0.02D);
                    }
                    if (elapsed < 12) return true;
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB box = new AABB(zone.center.x - 2.4D, zone.center.y - 1.0D, zone.center.z - 2.4D,
                            zone.center.x + 2.4D, zone.center.y + 2.2D, zone.center.z + 2.4D);
                    List<LivingEntity> entities = srv.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive);
                    if (entities.isEmpty()) return true;

                    for (LivingEntity e : entities) {
                        boolean isAlly = (e == owner && elapsed >= 30)
                                || (e != owner && owner != null && (e.isAlliedTo(owner) || (e instanceof Player && !(e instanceof Monster) && zone.spell.hasWardFriendlyFireSafeguard())));
                        if (isAlly) {
                            SpellCombatReactionEngine.purifyHexesAndNegativeEffects(e);
                            SpellCombatReactionEngine.notifyMagicalHeal(e, 8.0F);
                            e.heal(8.0F);
                            srv.sendParticles(ParticleTypes.HEART, e.getX(), e.getY(1.0D), e.getZ(), 6, 0.3D, 0.3D, 0.3D, 0.03D);
                            srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.5F);
                            return false;
                        }
                    }

                    List<LivingEntity> hostiles = entities.stream()
                            .filter(e -> e != owner && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard())))
                            .toList();
                    if (!hostiles.isEmpty()) {
                        AABB blastBox = new AABB(zone.center.x - 4.5D, zone.center.y - 1.5D, zone.center.z - 4.5D,
                                zone.center.x + 4.5D, zone.center.y + 3.5D, zone.center.z + 4.5D);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, blastBox, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            Vec3 away = foe.position().subtract(zone.center).normalize().scale(1.85D);
                            foe.setDeltaMovement(away.x, 0.52D, away.z);
                            foe.hasImpulse = true;
                            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                            SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, foe, away.normalize(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                        }
                        srv.sendParticles(ParticleTypes.GUST_EMITTER_SMALL, zone.center.x, zone.center.y + 0.6D, zone.center.z, 3, 0.8D, 0.3D, 0.8D, 0.02D);
                        srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.1F, 1.3F);
                        return false;
                    }
                    return true;
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
        // Solar Flare Breath: 5m pushback, sets undead ablaze 6s, strips invisibility & applies Glowing 6s (120t)!
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.setDeltaMovement(direction.x * 1.25D, 0.32D, direction.z * 1.25D);
            hit.hasImpulse = true;
            hit.removeEffect(MobEffects.INVISIBILITY);
            hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0));
            if (hit.getType().is(EntityTypeTags.UNDEAD)) {
                hit.igniteForSeconds(6);
            }
        }
        Vec3 mid = caster.getEyePosition().add(direction.scale(coneRange * 0.5D));
        for (AreaEffectCloud cloud : level.getEntitiesOfClass(AreaEffectCloud.class, new AABB(mid.x - 4.0D, mid.y - 2.0D, mid.z - 4.0D, mid.x + 4.0D, mid.y + 2.0D, mid.z + 4.0D))) {
            cloud.discard();
        }
        level.sendParticles(ParticleTypes.END_ROD, mid.x, mid.y, mid.z, 24, 1.4D, 0.5D, 1.4D, 0.08D);
        level.sendParticles(ParticleTypes.CLOUD, mid.x, mid.y, mid.z, 16, 1.3D, 0.5D, 1.3D, 0.05D);
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
        // Sunrise Daybreak Tempest: 10s (200t), 14m sunrise rift — launches hostiles 4m airborne with convective gusts,
        // smites undead, strips Darkness/Blindness from allies, and heals allies inside for 1.0 HP/s!
        double r = Math.max(10.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 200, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 5 == 0) {
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 5.0D, zone.center.z, 20, zone.radius * 0.55D, 1.3D, zone.radius * 0.55D, 0.07D);
                        srv.sendParticles(ParticleTypes.CLOUD, zone.center.x, zone.center.y + 1.0D, zone.center.z, 12, zone.radius * 0.50D, 0.5D, zone.radius * 0.50D, 0.04D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 7.0D, zone.center.z + zone.radius);
                        for (LivingEntity e : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            boolean isAlly = e == owner || (owner != null && (e.isAlliedTo(owner) || (!(e instanceof Monster) && e instanceof Player)));
                            if (isAlly) {
                                e.removeEffect(MobEffects.DARKNESS);
                                e.removeEffect(MobEffects.BLINDNESS);
                                SpellCombatReactionEngine.notifyMagicalHeal(e, 1.0F);
                                e.heal(1.0F);
                            } else if (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard())) {
                                e.setDeltaMovement(e.getDeltaMovement().x, 0.78D, e.getDeltaMovement().z);
                                e.hasImpulse = true;
                                if (e.getType().is(EntityTypeTags.UNDEAD)) {
                                    e.igniteForSeconds(4);
                                    e.hurtServer(srv, srv.damageSources().magic(), 3.0F + zone.spell.getEffectiveTier() * 0.9F);
                                }
                                SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, e, new Vec3(0, 1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.35F);
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
        tag.putString("SpellEidolonMateria", EssenceType.DAWN.name());
        tag.putInt("SpellEidolonTintRGB", DAWN_WARDEN_HEX_COLOR);
        level.sendParticles(ParticleTypes.END_ROD, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 22, 0.45D, 0.55D, 0.45D, 0.06D);
        level.sendParticles(ParticleTypes.CLOUD, spawnPos.x, spawnPos.y + 0.5D, spawnPos.z, 12, 0.4D, 0.2D, 0.4D, 0.03D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Solar Zephyr Blink: 14m silent thermal blink, cleanses 1 negative debuff on arrival, grants +30% sprint speed for 5.0s (100t)!
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, level.getGameTime() + 50L);
        Holder<MobEffect> negToRemove = null;
        for (MobEffectInstance inst : caster.getActiveEffects()) {
            if (!inst.getEffect().value().isBeneficial()) {
                negToRemove = inst.getEffect();
                break;
            }
        }
        if (negToRemove != null) {
            caster.removeEffect(negToRemove);
        }
        caster.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 1, false, true));
        level.sendParticles(ParticleTypes.END_ROD, arrivalPos.x, arrivalPos.y + 0.8D, arrivalPos.z, 14, 0.35D, 0.45D, 0.35D, 0.05D);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Daybreak Awakening Transmute: cures Zombie Villagers, converts dark stone/wood into warm sandstone/terracotta/birch,
        // and purifies dropped rotten flesh into clean leather!
        boolean didAny = false;
        AABB area = new AABB(pos).inflate(2.5D);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, area)) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) {
                didAny = true;
            }
        }
        for (ZombieVillager zv : level.getEntitiesOfClass(ZombieVillager.class, area)) {
            var villager = zv.convertTo(EntityType.VILLAGER, ConversionParams.single(zv, false, false), v -> {});
            if (villager != null) {
                level.sendParticles(ParticleTypes.END_ROD, villager.getX(), villager.getY(0.9D), villager.getZ(), 20, 0.35D, 0.5D, 0.35D, 0.08D);
                didAny = true;
            }
        }
        if (state.is(Blocks.BLACKSTONE) || state.is(Blocks.POLISHED_BLACKSTONE)) {
            level.setBlockAndUpdate(pos, Blocks.SANDSTONE.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.DEEPSLATE) || state.is(Blocks.COBBLED_DEEPSLATE)) {
            level.setBlockAndUpdate(pos, Blocks.TERRACOTTA.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.DARK_OAK_LOG)) {
            level.setBlockAndUpdate(pos, Blocks.BIRCH_LOG.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.STRIPPED_DARK_OAK_LOG)) {
            level.setBlockAndUpdate(pos, Blocks.STRIPPED_BIRCH_LOG.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.DARK_OAK_PLANKS)) {
            level.setBlockAndUpdate(pos, Blocks.BIRCH_PLANKS.defaultBlockState());
            return true;
        }
        return didAny;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        if (stack.is(Items.ROTTEN_FLESH)) {
            itemEntity.setItem(new ItemStack(Items.LEATHER, stack.getCount()));
            level.sendParticles(ParticleTypes.END_ROD, itemEntity.getX(), itemEntity.getY(0.3D), itemEntity.getZ(), 8, 0.2D, 0.2D, 0.2D, 0.04D);
            return true;
        }
        if (stack.is(Items.BLACKSTONE)) {
            itemEntity.setItem(new ItemStack(Items.SANDSTONE, stack.getCount()));
            return true;
        }
        if (stack.is(Items.DEEPSLATE) || stack.is(Items.COBBLED_DEEPSLATE)) {
            itemEntity.setItem(new ItemStack(Items.TERRACOTTA, stack.getCount()));
            return true;
        }
        if (stack.is(Items.DARK_OAK_LOG)) {
            itemEntity.setItem(new ItemStack(Items.BIRCH_LOG, stack.getCount()));
            return true;
        }
        if (stack.is(Items.DARK_OAK_PLANKS)) {
            itemEntity.setItem(new ItemStack(Items.BIRCH_PLANKS, stack.getCount()));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Solar Blind-Sear Hex: Blindness + Glowing, melts 3 physical armor points, smites undead with continuous fire ticks!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 45, 0, true, true));
        SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 3.0D * mult, 0.0D, 40L);
        if (target.getType().is(EntityTypeTags.UNDEAD)) {
            target.igniteForSeconds(3);
            target.hurtServer(level, level.damageSources().magic(), (2.2F + potencyTier * 0.75F) * mult);
        } else {
            target.hurtServer(level, level.damageSources().magic(), (1.35F + potencyTier * 0.45F) * mult);
        }
        level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.9D), target.getZ(), 4, 0.25D, 0.3D, 0.25D, 0.03D);
        return true;
    }
}
