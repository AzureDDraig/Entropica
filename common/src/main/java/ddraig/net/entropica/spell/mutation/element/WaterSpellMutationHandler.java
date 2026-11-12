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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Water Materia (Base Type #2)</b>
 * (Spellcrafting System Guide Section 6.3).
 */
public final class WaterSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int AQUEOUS_EIDOLON_HEX_COLOR = 0x225C7C;

    @Override
    public EssenceType materiaType() {
        return EssenceType.WATER;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case SLOW_HEAVY -> "Viscous Fluid Mass";
            case CHAIN_LEAP -> "Riptide Undertow Chains";
            default -> "Riptide Torrent " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Hydro-Cannon Slug";
            case BEAM -> "High-Pressure Hydro-Cutter";
            case RADIAL_BURST -> "Tidal Nova";
            case TOUCH_STRIKE -> "Crashing Breaker";
            case AURA -> "Oceanic Shroud";
            case GLYPH -> "Whirlpool Geyser";
            case CONE -> "Deluge Torrent";
            case SKYFALL -> "Monsoon Cloudburst";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Fluid Dissolution";
            case TRANSMUTE, BREAK -> "Aqueous Elutriation";
            case AFFLICTION -> "Waterlogged Drowning Hex";
            case HARM -> "Hydrostatic Surge";
            case WARD -> "Tidal Bulwark";
            case RESTORATION -> "Springwater Cleansing";
            default -> "Aqueous " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations ---

    @Override
    public float getFlowBonusAdvantageScore(SpellFlowModifier flow, LivingEntity target) {
        if (flow == SpellFlowModifier.SLOW_HEAVY && (target.isOnFire() || isNetherFireEntity(target))) {
            return 0.6F;
        }
        return 0.0F;
    }

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        if (flow == SpellFlowModifier.SLOW_HEAVY) {
            level.sendParticles(ParticleTypes.SPLASH, projectile.getX(), projectile.getY(), projectile.getZ(),
                    3, 0.12D, 0.12D, 0.12D, 0.02D);
        }
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileSpawnedFromForm(ServerLevel level, SpellProjectileEntity projectile, LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        // Hydro-Cannon Slug: slight gravity arc
        projectile.setNoGravity(false);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.DRIPPING_WATER, projectile.getX(), projectile.getY(), projectile.getZ(),
                2, 0.08D, 0.08D, 0.08D, 0.0D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        triggerHydroSplash(level, projectile.position(), caster, spell, slot, 3.0D);
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        triggerHydroSplash(level, blockHit.getLocation(), caster, spell, slot, 3.0D);
    }

    private void triggerHydroSplash(ServerLevel level, Vec3 pos, @Nullable LivingEntity caster, SpellAssembly spell, SubSpellSlot slot, double rad) {
        douseAndHydrateArea(level, BlockPos.containing(pos), (int) Math.ceil(rad), false);
        AABB box = new AABB(pos.x - rad, pos.y - 1.5D, pos.z - rad, pos.x + rad, pos.y + 2.0D, pos.z + rad);
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (nearby == caster || !nearby.isAlive()) continue;
            SpellLingeringZoneManager.applySoaked(nearby, 160);
        }
        level.sendParticles(ParticleTypes.SPLASH, pos.x, pos.y + 0.3D, pos.z, 26, 0.6D, 0.4D, 0.6D, 0.12D);
        level.playSound(null, BlockPos.containing(pos), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 0.85F, 1.1F);
    }

    @Override
    public void onBeamFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double maxReach, List<LivingEntity> hitEntities, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // High-Pressure Hydro-Cutter: 2x knockback on wet/swimming foes, washes away cobwebs/fire, hydrates farmland
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            boolean alreadyWet = SpellLingeringZoneManager.isEntityWetOrSoaked(hit);
            SpellLingeringZoneManager.applySoaked(hit, 160);
            double kb = (alreadyWet ? 1.50D : 0.75D);
            hit.setDeltaMovement(direction.x * kb, 0.28D, direction.z * kb);
            hit.hasImpulse = true;
        }

        int steps = Math.min(20, (int) Math.ceil(maxReach));
        for (int i = 1; i <= steps; i++) {
            BlockPos p = BlockPos.containing(caster.getEyePosition().add(direction.scale(i)));
            douseAndHydrateArea(level, p, 1, true);
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.BOAT_PADDLE_WATER, SoundSource.PLAYERS, 0.9F, 1.35F);
    }

    @Override
    public void onTouchStrikeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, List<LivingEntity> hitEntities, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Crashing Breaker: 1.0s stun, drenches target, +50% bonus damage if burning or Nether mob
        float bonusDmg = 0.50F * ddraig.net.entropica.spell.fuel.MateriaStageTable.getBaseCombatDamageHp(spell.getEffectiveTier()) * Math.max(0.5F, apparatusPotency);
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            boolean bonusVsFire = hit.isOnFire() || isNetherFireEntity(hit);
            SpellLingeringZoneManager.applySoaked(hit, 180);
            hit.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), 20, 0));
            if (bonusVsFire) {
                hit.invulnerableTime = 0;
                hit.hurtServer(level, level.damageSources().indirectMagic(caster, caster), Math.max(2.0F, bonusDmg));
            }
        }
        if (!hitEntities.isEmpty()) {
            level.playSound(null, caster.blockPosition(), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 0.9F, 0.95F);
        }
    }

    @Override
    public void onRadialBurstFormCast(ServerLevel level, LivingEntity caster, Vec3 center, double radius, Vec3 direction, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Tidal Nova: flushes mobs backward, clears weeds & cools lava crusts, leaves foes soaked
        AABB box = new AABB(center.x - radius, center.y - 2.0D, center.z - radius, center.x + radius, center.y + 2.5D, center.z + radius);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (entity == caster || !entity.isAlive()) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(entity, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellLingeringZoneManager.applySoaked(entity, 200);
            Vec3 away = entity.position().subtract(center).normalize().scale(1.15D);
            entity.setDeltaMovement(away.x, 0.35D, away.z);
            entity.hasImpulse = true;
        }
        int r = Math.min(6, (int) Math.ceil(radius));
        BlockPos cPos = BlockPos.containing(center);
        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-r, -1, -r), cPos.offset(r, 1, r))) {
            BlockState bs = level.getBlockState(p);
            if (bs.is(Blocks.SHORT_GRASS) || bs.is(Blocks.TALL_GRASS) || bs.is(Blocks.DEAD_BUSH)) {
                level.destroyBlock(p, true, caster);
            } else if (bs.is(Blocks.LAVA)) {
                level.setBlockAndUpdate(p, Blocks.OBSIDIAN.defaultBlockState());
            }
        }
        douseAndHydrateArea(level, cPos, r, false);
        level.sendParticles(ParticleTypes.SPLASH, center.x, center.y + 0.5D, center.z, 40, radius * 0.45D, 0.3D, radius * 0.45D, 0.15D);
    }

    @Override
    public void onAuraFormCast(ServerLevel level, LivingEntity caster, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Oceanic Shroud: Water Breathing, Dolphin's Grace, instant fire-extinguishing, 40% explosion dampening
        int dur = 200 + spell.getEffectiveTier() * 40;
        caster.clearFire();
        caster.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, dur, 0, false, true));
        caster.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, dur, 0, false, true));
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_WATER_SHROUD_UNTIL, level.getGameTime() + dur);
        level.sendParticles(ParticleTypes.BUBBLE_POP, caster.getX(), caster.getY(0.8D), caster.getZ(), 20, 0.4D, 0.5D, 0.4D, 0.05D);
    }

    @Override
    public boolean onGlyphFormCast(ServerLevel level, LivingEntity caster, Vec3 glyphCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Whirlpool Geyser: traps entering victims in a spinning water vortex for 3 seconds (60t), immobilizing & drowning
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 3.0D, 400, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed < 6) return true;
                    AABB box = new AABB(zone.center.x - 2.5D, zone.center.y - 1.0D, zone.center.z - 2.5D,
                            zone.center.x + 2.5D, zone.center.y + 3.0D, zone.center.z + 2.5D);
                    LivingEntity owner = zone.resolveCaster(srv);
                    List<LivingEntity> victims = srv.getEntitiesOfClass(LivingEntity.class, box,
                            e -> e.isAlive() && e != owner);
                    if (victims.isEmpty()) {
                        if (elapsed % 10 == 0) {
                            srv.sendParticles(ParticleTypes.BUBBLE, zone.center.x, zone.center.y + 0.1D, zone.center.z, 4, 0.6D, 0.05D, 0.6D, 0.02D);
                        }
                        return true;
                    }
                    // Spawn a 60-tick active whirlpool prison
                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 2.8D, 60, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (wsrv, wzone, welapsed) -> {
                                LivingEntity wOwner = wzone.resolveCaster(wsrv);
                                srv.sendParticles(ParticleTypes.SPLASH, wzone.center.x, wzone.center.y + 0.8D, wzone.center.z, 8, 0.6D, 0.8D, 0.6D, 0.08D);
                                for (LivingEntity v : wsrv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != wOwner)) {
                                    Vec3 pull = wzone.center.subtract(v.position()).scale(0.35D);
                                    v.setDeltaMovement(pull.x, 0.08D, pull.z);
                                    v.hasImpulse = true;
                                    SpellLingeringZoneManager.applySoaked(v, 100);
                                    if (welapsed % 20 == 0) {
                                        v.hurtServer(wsrv, wsrv.damageSources().drown(), 2.0F + wzone.spell.getEffectiveTier() * 0.5F);
                                        SpellExecutionEngine.deliverSubSpellOnEntity(wsrv, wOwner, v, Vec3.ZERO, wzone.spell, wzone.slot, wzone.fallbackMateria, wzone.apparatusPotency * 0.4F);
                                    }
                                }
                                return true;
                            }
                    ));
                    return false;
                }
        ));
        return true;
    }

    @Override
    public void onConeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double coneRange, List<LivingEntity> hitTargets, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Deluge Torrent: 12-block flood wave sweeping mobs back, soaking entities, extinguishing fire & hydrating farmland
        double delugeReach = Math.max(12.0D, coneRange);
        AABB delugeBox = caster.getBoundingBox().expandTowards(direction.scale(delugeReach)).inflate(delugeReach * 0.45D);
        for (LivingEntity cand : level.getEntitiesOfClass(LivingEntity.class, delugeBox)) {
            if (cand == caster || !cand.isAlive()) continue;
            Vec3 toCand = cand.getEyePosition().subtract(caster.getEyePosition()).normalize();
            if (toCand.dot(direction) < 0.55D) continue;
            if (!hitTargets.contains(cand)) {
                SpellExecutionEngine.deliverSubSpellOnEntity(level, caster, cand, direction, spell, slot, fallbackMateria, apparatusPotency);
            }
            SpellLingeringZoneManager.applySoaked(cand, 200);
            if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(cand, caster, spell.hasWardFriendlyFireSafeguard())) {
                cand.setDeltaMovement(direction.x * 1.35D, 0.32D, direction.z * 1.35D);
                cand.hasImpulse = true;
            }
        }
        int steps = Math.max(12, (int) Math.ceil(delugeReach));
        for (int s = 1; s <= steps; s++) {
            douseAndHydrateArea(level, BlockPos.containing(caster.getEyePosition().add(direction.scale(s))), 2, false);
        }
        level.sendParticles(ParticleTypes.SPLASH, caster.getX() + direction.x * 2.5D, caster.getEyeY() + direction.y * 2.5D, caster.getZ() + direction.z * 2.5D, 30, 0.9D, 0.4D, 0.9D, 0.15D);
    }

    @Override
    public boolean onSkyfallFormCast(ServerLevel level, LivingEntity caster, Vec3 impactCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Monsoon Cloudburst: 16m-diameter (8m radius) downpour drenching area, hydrating soil, healing Dolphins/Axolotls/Turtles, drowning Nether/Fire mobs
        double stormRad = Math.max(8.0D, radius);
        douseAndHydrateArea(level, BlockPos.containing(impactCenter), (int) Math.ceil(stormRad), false);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, stormRad, 100, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 4 == 0) {
                        srv.sendParticles(ParticleTypes.FALLING_WATER, zone.center.x, zone.center.y + 6.0D, zone.center.z,
                                28, zone.radius * 0.55D, 1.0D, zone.radius * 0.55D, 0.0D);
                    }
                    if (elapsed % 20 == 0) {
                        douseAndHydrateArea(srv, BlockPos.containing(zone.center), Math.min(8, (int) Math.ceil(zone.radius)), false);
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 5.0D, zone.center.z + zone.radius);
                        for (LivingEntity e : srv.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
                            SpellLingeringZoneManager.applySoaked(e, 160);
                            if (e instanceof Axolotl || e instanceof Turtle || e.getType() == net.minecraft.world.entity.EntityType.DOLPHIN) {
                                SpellCombatReactionEngine.notifyMagicalHeal(e, 3.0F);
                                e.heal(3.0F);
                            } else if (e != owner) {
                                if (isNetherFireEntity(e)) {
                                    e.hurtServer(srv, srv.damageSources().drown(), 4.0F + zone.spell.getEffectiveTier());
                                }
                                SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, e, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.35F);
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
        tag.putString("SpellEidolonMateria", EssenceType.WATER.name());
        tag.putInt("SpellEidolonTintRGB", AQUEOUS_EIDOLON_HEX_COLOR);
        triggerHydroSplash(level, spawnPos, caster, spell, slot, Math.max(3.0D, radius));
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Fluid Dissolution: splash upon reform
        douseAndHydrateArea(level, BlockPos.containing(arrivalPos), 2, false);
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(3.0D))) {
            if (nearby != caster) SpellLingeringZoneManager.applySoaked(nearby, 140);
        }
        level.sendParticles(ParticleTypes.SPLASH, arrivalPos.x, arrivalPos.y + 0.8D, arrivalPos.z, 28, 0.5D, 0.5D, 0.5D, 0.15D);
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 0.85F, 1.2F);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Aqueous Elutriation: washes dirt -> clay, sand -> mud, cleans oxidized copper
        if (state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT)) {
            level.setBlockAndUpdate(pos, Blocks.CLAY.defaultBlockState());
            return true;
        }
        if (state.is(BlockTags.SAND)) {
            level.setBlockAndUpdate(pos, Blocks.MUD.defaultBlockState());
            return true;
        }
        var cleaned = WeatheringCopper.getPrevious(state);
        if (cleaned.isPresent()) {
            level.setBlockAndUpdate(pos, cleaned.get());
            return true;
        }
        if (state.getBlock() instanceof ConcretePowderBlock powder) {
            // Hydrate concrete powder directly into solid concrete!
            String id = BuiltInRegistries.BLOCK.getKey(powder).getPath();
            if (id.endsWith("_concrete_powder")) {
                var solidKey = net.minecraft.resources.ResourceLocation.withDefaultNamespace(id.substring(0, id.length() - "_powder".length()));
                Block solid = BuiltInRegistries.BLOCK.getValue(solidKey);
                if (solid != Blocks.AIR) {
                    level.setBlockAndUpdate(pos, solid.defaultBlockState());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(
            ServerLevel level,
            @Nullable LivingEntity caster,
            net.minecraft.world.entity.item.ItemEntity itemEntity,
            int potencyTier
    ) {
        var stack = itemEntity.getItem();
        if (stack.is(net.minecraft.world.item.Items.DIRT) || stack.is(net.minecraft.world.item.Items.MUD)) {
            itemEntity.setItem(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.CLAY_BALL, Math.min(64, stack.getCount() * 4)));
            level.sendParticles(ParticleTypes.SPLASH, itemEntity.getX(), itemEntity.getY(0.5D), itemEntity.getZ(), 8, 0.2D, 0.2D, 0.2D, 0.05D);
            return true;
        }
        if (stack.is(net.minecraft.world.item.Items.SPONGE)) {
            itemEntity.setItem(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WET_SPONGE, stack.getCount()));
            return true;
        }
        String itemPath = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (itemPath.endsWith("_concrete_powder")) {
            var solidItem = BuiltInRegistries.ITEM.getValue(
                    net.minecraft.resources.ResourceLocation.withDefaultNamespace(itemPath.substring(0, itemPath.length() - "_powder".length())));
            if (solidItem != net.minecraft.world.item.Items.AIR) {
                itemEntity.setItem(new net.minecraft.world.item.ItemStack(solidItem, stack.getCount()));
                level.sendParticles(ParticleTypes.SPLASH, itemEntity.getX(), itemEntity.getY(0.5D), itemEntity.getZ(), 8, 0.2D, 0.2D, 0.2D, 0.05D);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Waterlogged Drowning Hex: keeps target soaked (2x Lightning / 3x Frozen) and deals drowning damage on land
        SpellLingeringZoneManager.applySoaked(target, 45);
        if (!target.isInWater()) {
            float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
            target.hurtServer(level, level.damageSources().drown(), (1.25F + potencyTier * 0.5F) * mult);
            level.sendParticles(ParticleTypes.DRIPPING_WATER, target.getX(), target.getY(0.8D), target.getZ(), 6, 0.25D, 0.3D, 0.25D, 0.0D);
        }
        return true;
    }

    private static boolean isNetherFireEntity(LivingEntity e) {
        return e.fireImmune() || e instanceof Blaze || e instanceof MagmaCube || e instanceof Piglin || e instanceof ZombifiedPiglin;
    }

    private static void douseAndHydrateArea(ServerLevel level, BlockPos center, int rad, boolean clearCobwebs) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-rad, -1, -rad), center.offset(rad, 1, rad))) {
            BlockState bs = level.getBlockState(p);
            if (bs.getBlock() instanceof BaseFireBlock) {
                level.removeBlock(p, false);
            } else if (bs.getBlock() instanceof CampfireBlock && bs.getValue(CampfireBlock.LIT)) {
                level.setBlockAndUpdate(p, bs.setValue(CampfireBlock.LIT, false));
            } else if (bs.getBlock() instanceof FarmBlock && bs.getValue(FarmBlock.MOISTURE) < 7) {
                level.setBlockAndUpdate(p, bs.setValue(FarmBlock.MOISTURE, 7));
            } else if (clearCobwebs && bs.is(Blocks.COBWEB)) {
                level.destroyBlock(p, true);
            }
        }
    }
}
