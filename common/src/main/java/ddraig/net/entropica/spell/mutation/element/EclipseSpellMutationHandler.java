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
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Eclipse Materia (Ethereal &amp; Paradox Fusion #25)</b>
 * (Spellcrafting System Guide Section 6.26 — Radiant + Umbral [Leans: Umbral]).
 */
public final class EclipseSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int CORONAL_ECLIPSE_HEX_COLOR = 0x90824C;

    @Override
    public EssenceType materiaType() {
        return EssenceType.ECLIPSE;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Coronal Flare Bifurcation Split Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Penumbra Crescent Scythe";
            case BEAM -> "Umbral Inversion Jet";
            case RADIAL_BURST -> "Total Eclipse Nova";
            case TOUCH_STRIKE -> "Umbral Siphon Palm";
            case AURA -> "Solar Corona Bastion";
            case GLYPH -> "Total Eclipse Singularity Mine";
            case CONE -> "Coronal Flare Deluge";
            case SKYFALL -> "Penumbral Shadow Monsoon";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Coronal Flare Dash";
            case TRANSMUTE, BREAK -> "Umbral Smelting Transmute";
            case AFFLICTION -> "Coronal Blind-Rot Hex";
            default -> "Coronal Eclipse " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations (Coronal Flare Bifurcation Split Flow) ---

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.SMOKE, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.01D);
        if (projectile.tickCount % 2 == 0) {
            level.sendParticles(ParticleTypes.FLAME, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
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
        // Splits into 3 paradox shadow-bolts curving around terrain obstacles to strike secondary enemies!
        float[] angles = new float[]{-26.0F, 0.0F, 26.0F};
        for (float yaw : angles) {
            SpellProjectileEntity child = projectile.spawnSplitChild(level, splitOrigin, yaw, true, 0.60F, 1.35F);
            child.setWallPhasing(true);
        }
        level.sendParticles(ParticleTypes.END_ROD, splitOrigin.x, splitOrigin.y + 0.3D, splitOrigin.z, 12, 0.25D, 0.25D, 0.25D, 0.06D);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, splitOrigin.x, splitOrigin.y + 0.3D, splitOrigin.z, 8, 0.25D, 0.25D, 0.25D, 0.03D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileSpawnedFromForm(ServerLevel level, SpellProjectileEntity projectile, LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        projectile.setHomingGuidance(true);
        projectile.setWallPhasing(true);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.SMOKE, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.01D);
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.005D);
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
        // Penumbra Crescent Scythe: curves around physical shields & cover to strike from behind, silencing abilities for 2s (40t)!
        if (!(hitEntity instanceof LivingEntity target) || !target.isAlive()) return;
        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(target, caster, spell.hasWardFriendlyFireSafeguard())) {
            return;
        }
        if (target.isBlocking()) {
            if (target instanceof Player blockingPlayer && !blockingPlayer.getUseItem().isEmpty()) {
                blockingPlayer.getCooldowns().addCooldown(blockingPlayer.getUseItem(), 60);
            }
            target.stopUsingItem();
            target.invulnerableTime = 0;
            target.hurtServer(level, level.damageSources().magic(), 3.0F + spell.getEffectiveTier());
        }
        Vec3 forwardFromBehind = target.getLookAngle().normalize().scale(0.42D);
        target.setDeltaMovement(target.getDeltaMovement().add(forwardFromBehind.x, 0.16D, forwardFromBehind.z));
        target.hasImpulse = true;
        Vec3 backPos = target.position().subtract(target.getLookAngle().scale(0.65D));
        level.sendParticles(ParticleTypes.END_ROD, backPos.x, target.getY(0.85D), backPos.z, 8, 0.2D, 0.25D, 0.2D, 0.04D);

        SpellLingeringZoneManager.applyArcaneSilence(target, 40);
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 50, 0));
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
        // Umbral Inversion Jet: shrinks mob aggro range to 2 blocks & dissolves enemy armor (-1/s up to -4 armor)!
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            if (hit instanceof Mob mob && mob.getTarget() != null && mob.distanceToSqr(mob.getTarget()) > 4.0D) {
                mob.setTarget(null);
            }
            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("EclipseBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? Math.min(4, data.getInt("EclipseBeamArmorStacks").orElse(0) + 1) : 1;
            data.putLong("EclipseBeamLastTick", now);
            data.putInt("EclipseBeamArmorStacks", stacks);
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, stacks, 0.0D, 100L);
            hit.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 50, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 50, 0));
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
        // Total Eclipse Nova: 6m blast, 4m knockback, simultaneous Blindness & Glowing (4s), snuffs torches/fires, resets mob aggro!
        double r = Math.max(6.0D, radius);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 2.5D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = foe.position().subtract(center).normalize().scale(1.05D);
            foe.setDeltaMovement(away.x, 0.30D, away.z);
            foe.hasImpulse = true;
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
            if (foe instanceof Mob mob) {
                mob.setTarget(null);
            }
        }
        snuffTorchesAndFires(level, BlockPos.containing(center), 5);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, center.x, center.y + 0.8D, center.z, 36, 2.2D, 0.5D, 2.2D, 0.06D);
        level.sendParticles(ParticleTypes.FLAME, center.x, center.y + 0.9D, center.z, 28, 2.4D, 0.4D, 2.4D, 0.05D);
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
        // Umbral Siphon Palm: cuts enemy melee attack damage by 40% for 6s (120t) & siphons positive potion effects to caster!
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyTimedAttackDebuff(hit, level, 0.40D, 0.0D, 120L);
            List<MobEffectInstance> stolen = new ArrayList<>();
            for (MobEffectInstance inst : hit.getActiveEffects()) {
                if (inst.getEffect().value().isBeneficial()) {
                    stolen.add(new MobEffectInstance(inst));
                }
            }
            for (MobEffectInstance inst : stolen) {
                hit.removeEffect(inst.getEffect());
                caster.addEffect(new MobEffectInstance(inst.getEffect(), Math.min(240, inst.getDuration()), inst.getAmplifier()));
            }
            level.sendParticles(ParticleTypes.END_ROD, hit.getX(), hit.getY(0.8D), hit.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.05D);
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
        // Solar Corona Bastion: +6 Armor, immunity to Blindness & Darkness, sets melee attackers ablaze with celestial fire!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_ECLIPSE_BASTION_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 6.0D, 0.0D, 0.0D, 0.0D, dur);
        caster.removeEffect(MobEffects.BLINDNESS);
        caster.removeEffect(MobEffects.DARKNESS);
        level.sendParticles(ParticleTypes.FLAME, caster.getX(), caster.getY(0.9D), caster.getZ(), 20, 0.45D, 0.55D, 0.45D, 0.04D);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, caster.getX(), caster.getY(0.9D), caster.getZ(), 14, 0.4D, 0.5D, 0.4D, 0.02D);
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
        // Total Eclipse Singularity Mine: 4m black hole encircled by coronal ring — pulls to center, roots 3s, purges buffs, blinds 4s, snuffs torches!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 4.0D, 300, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.SMOKE, zone.center.x, zone.center.y + 0.15D, zone.center.z, 6, 1.2D, 0.1D, 1.2D, 0.01D);
                    }
                    if (elapsed < 10) return true;

                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB triggerBox = new AABB(zone.center.x - 2.5D, zone.center.y - 1.0D, zone.center.z - 2.5D,
                            zone.center.x + 2.5D, zone.center.y + 2.0D, zone.center.z + 2.5D);
                    List<LivingEntity> triggers = srv.getEntitiesOfClass(LivingEntity.class, triggerBox, e -> e.isAlive() && e != owner
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard())));
                    if (triggers.isEmpty()) return true;

                    AABB blastBox = new AABB(zone.center.x - 4.0D, zone.center.y - 1.5D, zone.center.z - 4.0D,
                            zone.center.x + 4.0D, zone.center.y + 3.0D, zone.center.z + 4.0D);
                    for (LivingEntity victim : srv.getEntitiesOfClass(LivingEntity.class, blastBox, e -> e.isAlive() && e != owner)) {
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(victim, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                        Vec3 pull = zone.center.subtract(victim.position()).normalize().scale(0.95D);
                        victim.setDeltaMovement(pull.x, 0.15D, pull.z);
                        victim.hasImpulse = true;
                        victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 6));
                        victim.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0));
                        victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                        purgeBeneficialEffects(victim);
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, victim, pull, zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                    }
                    snuffTorchesAndFires(srv, BlockPos.containing(zone.center), 4);
                    srv.sendParticles(ParticleTypes.SONIC_BOOM, zone.center.x, zone.center.y + 0.6D, zone.center.z, 1, 0, 0, 0, 0);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.PLAYERS, 1.0F, 0.85F);
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
        // Coronal Flare Deluge: 6m knockback, simultaneous Blindness & Glowing (4s), snuffs torches & campfires, dissolves 3 armor for 6s!
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.setDeltaMovement(direction.x * 1.45D, 0.35D, direction.z * 1.45D);
            hit.hasImpulse = true;
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            hit.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 120L);
        }
        int steps = Math.min(8, (int) Math.ceil(coneRange));
        for (int s = 1; s <= steps; s++) {
            snuffTorchesAndFires(level, BlockPos.containing(caster.position().add(direction.scale(s))), 2);
        }
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
        // Penumbral Shadow Monsoon: 10s (200t) dark solar radiation stripping active buffs, eroding enchanted armor, & silencing abilities!
        double r = Math.max(8.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 200, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 5 == 0) {
                        srv.sendParticles(ParticleTypes.LARGE_SMOKE, zone.center.x, zone.center.y + 4.5D, zone.center.z, 18, zone.radius * 0.55D, 1.2D, zone.radius * 0.55D, 0.04D);
                        srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 4.0D, zone.center.z, 10, zone.radius * 0.50D, 1.0D, zone.radius * 0.50D, 0.05D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 5.0D, zone.center.z + zone.radius);
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            purgeBeneficialEffects(v);
                            SpellLingeringZoneManager.applyArcaneSilence(v, 45);
                            SpellCombatReactionEngine.applyPercentageArmorShred(v, srv, 0.35D, 0.35D, 45L);
                            if (v instanceof Monster) {
                                for (EquipmentSlot armorSlot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
                                    ItemStack worn = v.getItemBySlot(armorSlot);
                                    if (!worn.isEmpty() && worn.isEnchanted()) {
                                        EnchantmentHelper.updateEnchantments(worn, mutable -> mutable.removeIf(h -> !h.is(EnchantmentTags.CURSE)));
                                    }
                                }
                            }
                            SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.35F);
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
        tag.putString("SpellEidolonMateria", EssenceType.ECLIPSE.name());
        tag.putInt("SpellEidolonTintRGB", CORONAL_ECLIPSE_HEX_COLOR);
        level.sendParticles(ParticleTypes.END_ROD, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 18, 0.4D, 0.5D, 0.4D, 0.05D);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, spawnPos.x, spawnPos.y + 0.8D, spawnPos.z, 16, 0.4D, 0.5D, 0.4D, 0.03D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Coronal Flare Dash: 16m streak of celestial fire knocking aside mobs along the transit path!
        Vec3 delta = arrivalPos.subtract(departurePos);
        int steps = Math.max(2, (int) Math.ceil(delta.length()));
        Vec3 stepVec = delta.scale(1.0D / steps);
        long burnUntil = level.getGameTime() + 80L;
        for (int i = 0; i <= steps; i++) {
            Vec3 p = departurePos.add(stepVec.scale(i));
            level.sendParticles(ParticleTypes.FLAME, p.x, p.y + 0.8D, p.z, 4, 0.2D, 0.3D, 0.2D, 0.02D);
            level.sendParticles(ParticleTypes.SMOKE, p.x, p.y + 0.8D, p.z, 3, 0.2D, 0.3D, 0.2D, 0.01D);
            for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, new AABB(p.x - 1.5D, p.y - 0.5D, p.z - 1.5D, p.x + 1.5D, p.y + 2.0D, p.z + 1.5D))) {
                if (foe == caster || !foe.isAlive() || foe.isAlliedTo(caster) || !(foe instanceof Monster)) continue;
                Vec3 side = foe.position().subtract(p).normalize().scale(0.85D);
                foe.setDeltaMovement(side.x, 0.32D, side.z);
                foe.hasImpulse = true;
                foe.igniteForSeconds(4);
                EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_ECLIPSE_CORONA_BURN_UNTIL, burnUntil);
            }
        }
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Umbral Smelting Transmute: smelts raw ores in complete darkness with +20% bonus yields!
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness < 0.0F || hardness > 2.0F + potencyTier * 5.0F) return false;
        List<ItemStack> drops = Block.getDrops(state, level, pos, null, caster, ItemStack.EMPTY);
        boolean smeltedAny = false;
        boolean inDarkness = level.getMaxLocalRawBrightness(pos) <= 4;
        for (ItemStack d : drops) {
            ItemStack smelted = NetherSpellMutationHandler.trySmeltStack(level, d);
            if (smelted != d) {
                smeltedAny = true;
                if (inDarkness) {
                    int bonus = Math.max(1, Math.round(smelted.getCount() * 0.20F));
                    if (smelted.getCount() >= 5 || level.random.nextFloat() < 0.20F * smelted.getCount()) {
                        smelted.grow(bonus);
                    }
                }
            }
            Block.popResource(level, pos, smelted);
        }
        if (smeltedAny) {
            level.removeBlock(pos, false);
            level.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 8, 0.2D, 0.2D, 0.2D, 0.02D);
            level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 5, 0.2D, 0.2D, 0.2D, 0.03D);
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack in = itemEntity.getItem();
        ItemStack smelted = NetherSpellMutationHandler.trySmeltStack(level, in);
        if (smelted != in) {
            if (level.getMaxLocalRawBrightness(itemEntity.blockPosition()) <= 4) {
                int bonus = (smelted.getCount() / 5) + (level.random.nextFloat() < (smelted.getCount() % 5) * 0.20F ? 1 : 0);
                if (bonus > 0) {
                    smelted.setCount(Math.min(smelted.getMaxStackSize(), smelted.getCount() + bonus));
                }
            }
            itemEntity.setItem(smelted);
            level.sendParticles(ParticleTypes.END_ROD, itemEntity.getX(), itemEntity.getY(0.3D), itemEntity.getZ(), 6, 0.15D, 0.15D, 0.15D, 0.03D);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Coronal Blind-Rot Hex: simultaneous Blindness & Glowing, dissolves 3 armor points, prevents health regeneration!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, true, true));
        SpellCombatReactionEngine.applyTimedAttributeShred(target, level, 3.0D * mult, 0.0D, 35L);
        target.hurtServer(level, level.damageSources().magic(), (1.4F + potencyTier * 0.50F) * mult);
        level.sendParticles(ParticleTypes.SMOKE, target.getX(), target.getY(0.8D), target.getZ(), 4, 0.25D, 0.3D, 0.25D, 0.01D);
        level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.9D), target.getZ(), 2, 0.2D, 0.25D, 0.2D, 0.02D);
        return true;
    }

    public static void purgeBeneficialEffects(LivingEntity target) {
        List<Holder<MobEffect>> toRemove = new ArrayList<>();
        for (MobEffectInstance eff : target.getActiveEffects()) {
            if (eff.getEffect().value().isBeneficial()) {
                toRemove.add(eff.getEffect());
            }
        }
        for (Holder<MobEffect> h : toRemove) {
            target.removeEffect(h);
        }
    }

    public static void snuffTorchesAndFires(ServerLevel level, BlockPos center, int rad) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-rad, -1, -rad), center.offset(rad, 2, rad))) {
            BlockState bs = level.getBlockState(p);
            if (bs.getBlock() instanceof BaseFireBlock) {
                level.removeBlock(p, false);
            } else if (bs.getBlock() instanceof TorchBlock || bs.getBlock() instanceof WallTorchBlock) {
                level.destroyBlock(p, true);
            } else if (bs.getBlock() instanceof CampfireBlock && bs.getValue(CampfireBlock.LIT)) {
                level.setBlockAndUpdate(p, bs.setValue(CampfireBlock.LIT, false));
            }
        }
    }
}
