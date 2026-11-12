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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
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
 * Complete per-part Signature Mutation implementation for <b>Pyre Materia (Tier 3 Trinity Fusion #39)</b>
 * (Spellcrafting System Guide Section 6.40 — Earth + Nether + Nature).
 */
public final class PyreSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int PYRE_HEARTH_HEX_COLOR = 0xFF4500;

    @Override
    public EssenceType materiaType() {
        return EssenceType.PYRE;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return "Cremating Wildfire Orbit Flow";
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Combustion Hearth Orb";
            case BEAM -> "Cremating Wildfire Jetstream Beam";
            case RADIAL_BURST -> "Raging Wildfire Pyre Nova";
            case TOUCH_STRIKE -> "Cremating Timber-Pyre Cleave";
            case AURA -> "Pyre Hearth Hearthstone Shroud";
            case GLYPH -> "Cremation Pyre-Pitfall Glyph";
            case CONE -> "Combustion Hearth Flame-Thrower";
            case SKYFALL -> "Cremating Pyre Cataclysm Downpour";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Wildfire Flashover Step";
            case TRANSMUTE, BREAK -> "Pyre Ash-Charcoal Transmute";
            case AFFLICTION -> "Unquenchable Wildfire Cremation Hex";
            default -> "Cremating Pyre " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations (Cremating Wildfire Orbit Flow) ---

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        level.sendParticles(ParticleTypes.FLAME, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.02D);
        level.sendParticles(ParticleTypes.ASH, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.06D, 0.06D, 0.06D, 0.01D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.LAVA, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.01D);
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.04D, 0.04D, 0.04D, 0.01D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        // Combustion Hearth Orb: 5.0s wildfire ignition, 5m KB, instantly melts nearby ice & snow into steam!
        if (hitEntity instanceof LivingEntity victim && victim.isAlive()
                && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(victim, caster, spell.hasWardFriendlyFireSafeguard()))) {
            victim.igniteForSeconds(5);
            Vec3 kb = projectile.getDeltaMovement().normalize().scale(1.25D);
            victim.setDeltaMovement(kb.x, 0.32D, kb.z);
            victim.hasImpulse = true;
        }
        meltIceAndSnowToSteam(level, BlockPos.containing(hitEntity.position()), 3);
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        meltIceAndSnowToSteam(level, blockHit.getBlockPos(), 3);
    }

    private static void meltIceAndSnowToSteam(ServerLevel level, BlockPos center, int r) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -1, -r), center.offset(r, 2, r))) {
            BlockState bs = level.getBlockState(p);
            if (bs.is(Blocks.ICE) || bs.is(Blocks.FROSTED_ICE) || bs.is(Blocks.PACKED_ICE) || bs.is(Blocks.SNOW) || bs.is(Blocks.SNOW_BLOCK) || bs.is(Blocks.POWDER_SNOW)) {
                level.removeBlock(p, false);
                level.sendParticles(ParticleTypes.CLOUD, p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D, 3, 0.2D, 0.2D, 0.2D, 0.02D);
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
        // Cremating Wildfire Jetstream Beam: melts -1 armor, disintegrates non-flameproof incoming projectiles,
        // 3.0s sustained triggers Cataclysmic Flashover (fiery blast igniting adjacent mobs + 1.5s / 30t stun)!
        Vec3 eye = caster.getEyePosition();
        Vec3 dirNorm = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        AABB corridor = new AABB(eye, eye.add(dirNorm.scale(maxReach))).inflate(1.25D);
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, corridor, p -> p.getOwner() != caster && !(p instanceof SpellProjectileEntity))) {
            if (!proj.fireImmune()) {
                level.sendParticles(ParticleTypes.ASH, proj.getX(), proj.getY(), proj.getZ(), 6, 0.1D, 0.1D, 0.1D, 0.02D);
                proj.discard();
            }
        }

        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.igniteForSeconds(4);
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 1.0D, 0.0D, 80L);

            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("PyreBeamLastTick").orElse(0L);
            int stacks = (now - last <= 45L) ? data.getInt("PyreBeamStacks").orElse(0) + 1 : 1;
            data.putLong("PyreBeamLastTick", now);
            data.putInt("PyreBeamStacks", stacks);

            if (stacks >= 3) {
                data.putInt("PyreBeamStacks", 0);
                hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
                for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, hit.getBoundingBox().inflate(4.0D), e -> e.isAlive() && e != caster)) {
                    if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(adj, caster, spell.hasWardFriendlyFireSafeguard())) continue;
                    adj.igniteForSeconds(6);
                    adj.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 30, 0));
                    adj.hurtServer(level, level.damageSources().onFire(), 6.5F + spell.getEffectiveTier() * 1.8F);
                }
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 0.35F, 0.05F), hit.getX(), hit.getY(1.0D), hit.getZ(), 1, 0, 0, 0, 0);
                level.playSound(null, hit.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.9F, 1.15F);
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
        // Raging Wildfire Pyre Nova: 6m radius — 7m KB, 6.0s fire, incinerates nearby plant matter into charcoal/ash,
        // smothers ice/snow, and bolsters caster melee damage by +25% for 8.0s (160t)!
        double r = Math.max(6.0D, radius);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.5D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != caster)) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = foe.position().subtract(center).normalize().scale(1.65D);
            foe.setDeltaMovement(away.x, 0.38D, away.z);
            foe.hasImpulse = true;
            foe.igniteForSeconds(6);
        }
        BlockPos cp = BlockPos.containing(center);
        meltIceAndSnowToSteam(level, cp, 5);
        for (BlockPos p : BlockPos.betweenClosed(cp.offset(-4, -1, -4), cp.offset(4, 2, 4))) {
            BlockState bs = level.getBlockState(p);
            if (bs.is(BlockTags.SMALL_FLOWERS) || bs.is(Blocks.SHORT_GRASS) || bs.is(Blocks.FERN) || bs.is(Blocks.TALL_GRASS)) {
                level.removeBlock(p, false);
                if (level.random.nextFloat() < 0.25F) {
                    level.addFreshEntity(new ItemEntity(level, p.getX() + 0.5D, p.getY() + 0.3D, p.getZ() + 0.5D, new ItemStack(Items.CHARCOAL)));
                }
            }
        }
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_PYRE_MELEE_BOOST_UNTIL, level.getGameTime() + 160L);
        level.sendParticles(ParticleTypes.FLAME, center.x, center.y + 0.8D, center.z, 44, 2.2D, 0.6D, 2.2D, 0.08D);
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, center.x, center.y + 1.0D, center.z, 20, 1.8D, 0.5D, 1.8D, 0.03D);
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
        // Cremating Timber-Pyre Cleave: 6.0s fire, melts 3 armor points, 5m KB, leaves crescent arc of burning embers!
        Vec3 push = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.igniteForSeconds(6);
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 3.0D, 0.0D, 140L);
            hit.setDeltaMovement(push.x * 1.25D, 0.32D, push.z * 1.25D);
            hit.hasImpulse = true;
        }
        Vec3 arcCenter = caster.position().add(push.scale(2.4D));
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, arcCenter, 2.5D, 80, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 10 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB box = new AABB(zone.center.x - 2.5D, zone.center.y - 1.0D, zone.center.z - 2.5D,
                                zone.center.x + 2.5D, zone.center.y + 1.8D, zone.center.z + 2.5D);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            foe.igniteForSeconds(3);
                            foe.hurtServer(srv, srv.damageSources().onFire(), 2.0F);
                        }
                        srv.sendParticles(ParticleTypes.FLAME, zone.center.x, zone.center.y + 0.15D, zone.center.z, 8, 1.0D, 0.1D, 1.0D, 0.02D);
                    }
                    return true;
                }
        ));
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
        // Pyre Hearth Hearthstone Shroud: +8 Armor, +2 Toughness, -10% move speed, melts ice/snow underfoot, Fire/Lava/Freeze immunity!
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_PYRE_SHROUD_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 8.0D, 2.0D, 0.0D, -0.10D, dur);
        level.sendParticles(ParticleTypes.FLAME, caster.getX(), caster.getY(0.8D), caster.getZ(), 22, 0.45D, 0.55D, 0.45D, 0.04D);
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
        // Cremation Pyre-Pitfall Glyph: erupts into a 4m bonfire pitfall for 4.0s (80t), stripping 3 armor & preventing jumping/escape!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 3.5D, 300, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (elapsed % 10 == 0) {
                        srv.sendParticles(ParticleTypes.SMOKE, zone.center.x, zone.center.y + 0.1D, zone.center.z, 4, 0.8D, 0.1D, 0.8D, 0.01D);
                    }
                    if (elapsed < 10) return true;

                    AABB trigger = new AABB(zone.center.x - 2.4D, zone.center.y - 1.0D, zone.center.z - 2.4D,
                            zone.center.x + 2.4D, zone.center.y + 2.0D, zone.center.z + 2.4D);
                    List<LivingEntity> victims = srv.getEntitiesOfClass(LivingEntity.class, trigger, e -> e.isAlive() && e != owner
                            && (owner == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard())));
                    if (victims.isEmpty()) return true;

                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 4.0D, 80, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (sLvl, sZone, sEl) -> {
                                if (sEl > sZone.maxDurationTicks) return false;
                                LivingEntity sOwner = sZone.resolveCaster(sLvl);
                                AABB pitBox = new AABB(sZone.center.x - 4.0D, sZone.center.y - 1.5D, sZone.center.z - 4.0D,
                                        sZone.center.x + 4.0D, sZone.center.y + 2.5D, sZone.center.z + 4.0D);
                                long now = sLvl.getGameTime();
                                for (LivingEntity v : sLvl.getEntitiesOfClass(LivingEntity.class, pitBox, e -> e.isAlive() && e != sOwner)) {
                                    if (sOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, sOwner, sZone.spell.hasWardFriendlyFireSafeguard())) continue;
                                    v.igniteForSeconds(4);
                                    v.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 25, 0));
                                    EntityHelper.getPersistentData(v).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 25L);
                                    Vec3 inward = sZone.center.subtract(v.position()).scale(0.25D);
                                    v.setDeltaMovement(inward.x, Math.min(-0.12D, v.getDeltaMovement().y), inward.z);
                                    v.hasImpulse = true;
                                    if (sEl == 0) {
                                        SpellCombatReactionEngine.applyTimedAttributeShred(v, sLvl, 3.0D, 0.0D, 160L);
                                    }
                                    if (sEl % 20 == 0) {
                                        SpellExecutionEngine.deliverSubSpellOnEntity(sLvl, sOwner, v, Vec3.ZERO, sZone.spell, sZone.slot, sZone.fallbackMateria, sZone.apparatusPotency * 0.55F);
                                    }
                                }
                                if (sEl % 5 == 0) {
                                    sLvl.sendParticles(ParticleTypes.FLAME, sZone.center.x, sZone.center.y + 0.3D, sZone.center.z, 16, 1.5D, 0.3D, 1.5D, 0.04D);
                                }
                                return true;
                            }
                    ));
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 0.85F);
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
        // Combustion Hearth Flame-Thrower: melts ice, shreds -1 armor, cooks dropped meat & smelts sand into glass!
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.igniteForSeconds(5);
            SpellCombatReactionEngine.applyTimedAttributeShred(hit, level, 1.0D, 0.0D, 100L);
        }
        Vec3 dirNorm = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : caster.getLookAngle();
        Vec3 eye = caster.getEyePosition();
        AABB itemBox = new AABB(eye, eye.add(dirNorm.scale(coneRange))).inflate(2.0D);
        for (ItemEntity ie : level.getEntitiesOfClass(ItemEntity.class, itemBox)) {
            cookOrSmeltDroppedItem(ie);
        }
        for (double d = 1.5D; d <= coneRange; d += 1.8D) {
            BlockPos step = BlockPos.containing(eye.add(dirNorm.scale(d)));
            meltIceAndSnowToSteam(level, step, 1);
            BlockState bs = level.getBlockState(step.below());
            if ((bs.is(Blocks.SAND) || bs.is(Blocks.RED_SAND)) && !SpellExecutionEngine.isProtectedBlockOrContainer(level, step.below(), bs)) {
                level.setBlockAndUpdate(step.below(), Blocks.GLASS.defaultBlockState());
            }
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
        // Cremating Pyre Cataclysm Downpour: 10s (200t), 8m radius — blazing petrified logs & volcanic coal boulders
        // melting -2 armor per impact, igniting in Wildfire, leaving temporary fiery craters!
        double r = Math.max(8.0D, radius);
        Map<BlockPos, BlockState> craters = new HashMap<>();

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 200, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) {
                        for (Map.Entry<BlockPos, BlockState> e : craters.entrySet()) {
                            if (srv.getBlockState(e.getKey()).is(Blocks.MAGMA_BLOCK)) {
                                srv.setBlockAndUpdate(e.getKey(), e.getValue());
                            }
                        }
                        return false;
                    }
                    if (elapsed % 14 == 0) {
                        double ang = srv.random.nextDouble() * Math.PI * 2.0D;
                        double dist = srv.random.nextDouble() * (zone.radius - 1.0D);
                        Vec3 strike = zone.center.add(Math.cos(ang) * dist, 0.0D, Math.sin(ang) * dist);
                        BlockPos gp = BlockPos.containing(strike).below();
                        BlockState gs = srv.getBlockState(gp);
                        if ((gs.is(BlockTags.DIRT) || gs.is(Blocks.STONE)) && craters.size() < 10 && !craters.containsKey(gp)
                                && !SpellExecutionEngine.isProtectedBlockOrContainer(srv, gp, gs)) {
                            craters.put(gp.immutable(), gs);
                            srv.setBlockAndUpdate(gp, Blocks.MAGMA_BLOCK.defaultBlockState());
                        }
                        srv.sendParticles(ParticleTypes.LAVA, strike.x, strike.y + 0.8D, strike.z, 12, 0.9D, 0.5D, 0.9D, 0.06D);
                        srv.sendParticles(ParticleTypes.FLAME, strike.x, strike.y + 1.0D, strike.z, 18, 1.2D, 0.8D, 1.2D, 0.05D);

                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 5.5D, zone.center.z + zone.radius);
                        for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            foe.igniteForSeconds(5);
                            SpellCombatReactionEngine.applyTimedAttributeShred(foe, srv, 2.0D, 0.0D, 120L);
                            SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, foe, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.45F);
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
        tag.putString("SpellEidolonMateria", EssenceType.PYRE.name());
        tag.putInt("SpellEidolonTintRGB", PYRE_HEARTH_HEX_COLOR);
        level.sendParticles(ParticleTypes.FLAME, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 24, 0.45D, 0.55D, 0.45D, 0.05D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Wildfire Flashover Step: 14m step leaving flaming decoy at departure (3.0s / 60t) taunting & burning foes,
        // granting Fire Resistance & +30% sprint speed for 4.0s (80t) on arrival!
        SpellEidolonEntity.spawnShortLivedDecoy(
                level, caster, departurePos, EssenceType.PYRE,
                16.0F, 60, "Smoldering Pyre Decoy", null
        );
        caster.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 80, 0));
        caster.addEffect(new MobEffectInstance(MobEffects.SPEED, 80, 1));
        level.sendParticles(ParticleTypes.FLAME, departurePos.x, departurePos.y + 0.8D, departurePos.z, 20, 0.35D, 0.45D, 0.35D, 0.05D);
        level.sendParticles(ParticleTypes.LAVA, arrivalPos.x, arrivalPos.y + 0.6D, arrivalPos.z, 8, 0.3D, 0.3D, 0.3D, 0.03D);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Pyre Ash-Charcoal Transmute: wood logs -> coal blocks (+20% bonus yield), sand -> glass, raw foods -> cooked,
        // dirt/grass -> fertile podzol/ash-soil, cobblestone -> smooth stone or magma blocks!
        boolean didItem = false;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.5D))) {
            if (onTransmuteDroppedItem(level, caster, item, potencyTier)) didItem = true;
        }
        if (state.is(BlockTags.LOGS)) {
            level.setBlockAndUpdate(pos, Blocks.COAL_BLOCK.defaultBlockState());
            if (level.random.nextFloat() < 0.20F) {
                level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, new ItemStack(Items.CHARCOAL, 2)));
            }
            return true;
        }
        if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
            level.setBlockAndUpdate(pos, Blocks.GLASS.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK)) {
            level.setBlockAndUpdate(pos, Blocks.PODZOL.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.COBBLESTONE)) {
            level.setBlockAndUpdate(pos, level.random.nextBoolean() ? Blocks.SMOOTH_STONE.defaultBlockState() : Blocks.MAGMA_BLOCK.defaultBlockState());
            return true;
        }
        return didItem;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return false;
        if (stack.is(ItemTags.LOGS)) {
            int boosted = Math.min(64, Math.max(stack.getCount() + 1, Math.round(stack.getCount() * 1.20F)));
            itemEntity.setItem(new ItemStack(Items.CHARCOAL, boosted));
            return true;
        }
        if (cookOrSmeltDroppedItem(itemEntity)) {
            return true;
        }
        if (stack.is(Items.COBBLESTONE)) {
            itemEntity.setItem(new ItemStack(Items.SMOOTH_STONE, stack.getCount()));
            return true;
        }
        return false;
    }

    private static boolean cookOrSmeltDroppedItem(ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        if (stack.is(Items.BEEF)) { itemEntity.setItem(new ItemStack(Items.COOKED_BEEF, stack.getCount())); return true; }
        if (stack.is(Items.PORKCHOP)) { itemEntity.setItem(new ItemStack(Items.COOKED_PORKCHOP, stack.getCount())); return true; }
        if (stack.is(Items.CHICKEN)) { itemEntity.setItem(new ItemStack(Items.COOKED_CHICKEN, stack.getCount())); return true; }
        if (stack.is(Items.MUTTON)) { itemEntity.setItem(new ItemStack(Items.COOKED_MUTTON, stack.getCount())); return true; }
        if (stack.is(Items.RABBIT)) { itemEntity.setItem(new ItemStack(Items.COOKED_RABBIT, stack.getCount())); return true; }
        if (stack.is(Items.COD)) { itemEntity.setItem(new ItemStack(Items.COOKED_COD, stack.getCount())); return true; }
        if (stack.is(Items.SALMON)) { itemEntity.setItem(new ItemStack(Items.COOKED_SALMON, stack.getCount())); return true; }
        if (stack.is(Items.POTATO)) { itemEntity.setItem(new ItemStack(Items.BAKED_POTATO, stack.getCount())); return true; }
        if (stack.is(Items.SAND) || stack.is(Items.RED_SAND)) { itemEntity.setItem(new ItemStack(Items.GLASS, stack.getCount())); return true; }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Unquenchable Wildfire Cremation Hex: unquenchable fire even underwater, -40% armor toughness,
        // blocks sprinting & health regen, emits incendiary embers damaging nearby enemies!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        long now = level.getGameTime();
        EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 45L);
        target.igniteForSeconds(3);
        target.setSprinting(false);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, true, true));
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, Math.min(0.85D, 0.40D * mult), 45L);
        target.hurtServer(level, level.damageSources().onFire(), (1.60F + potencyTier * 0.50F) * mult);

        LivingEntity caster = SpellCombatReactionEngine.resolveHexCaster(level, target);
        for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(3.0D), e -> e.isAlive() && e != target && e != caster)) {
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(adj, caster, true)) continue;
            adj.igniteForSeconds(2);
            adj.hurtServer(level, level.damageSources().onFire(), 1.25F * mult);
        }
        level.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY(0.85D), target.getZ(), 6, 0.25D, 0.35D, 0.25D, 0.03D);
        return true;
    }
}
