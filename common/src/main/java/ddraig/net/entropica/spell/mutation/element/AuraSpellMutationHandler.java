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
import ddraig.net.entropica.spell.fuel.SpellFuelService;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Aura Materia (Vital Fusion #23)</b>
 * (Spellcrafting System Guide Section 6.24).
 */
public final class AuraSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int PRANIC_SPIRITUALIST_HEX_COLOR = 0xD47878;

    @Override
    public EssenceType materiaType() {
        return EssenceType.AURA;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Pranic Slipstream Chains";
            default -> "Pranic Slipstream " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Spiritual Gale Lance";
            case BEAM -> "Pranic Jetstream Tether";
            case RADIAL_BURST -> "Spiritual Vacuum Implosion";
            case TOUCH_STRIKE -> "Chakra Disruptor Palm";
            case AURA -> "Chakra Respiration Shroud";
            case GLYPH -> "Chakra Disruption Glyph";
            case CONE -> "Pranic Gale Deluge";
            case SKYFALL -> "Spiritual Gale Barrage";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Pranic Slipstream Blink";
            case TRANSMUTE, BREAK -> "Aerated Purifying Transmute";
            case AFFLICTION -> "Chakra Severance Hex";
            default -> "Pranic " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations ---

    @Override
    public void onChainLeapStep(ServerLevel level, LivingEntity caster, LivingEntity fromTarget, LivingEntity toTarget, SpellAssembly spell, SubSpellSlot slot) {
        // Pranic Slipstream Chains: launches struck victim 3m airborne, silences abilities 1.5s (30t), siphons 15% into caster fuel!
        toTarget.setDeltaMovement(toTarget.getDeltaMovement().x * 0.4D, 0.68D, toTarget.getDeltaMovement().z * 0.4D);
        toTarget.hasImpulse = true;
        SpellLingeringZoneManager.applyArcaneSilence(toTarget, 30);
        siphonAuraFuel(caster, spell.getEffectiveTier(), 0.15F);
        level.sendParticles(ParticleTypes.GUST, toTarget.getX(), toTarget.getY(0.5D), toTarget.getZ(), 2, 0.2D, 0.2D, 0.2D, 0.02D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.END_ROD, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0.08D, 0.08D, 0.08D, 0.01D);
        Entity owner = projectile.getOwner();
        for (AbstractArrow arrow : level.getEntitiesOfClass(AbstractArrow.class, projectile.getBoundingBox().inflate(1.8D))) {
            if (arrow.getOwner() != owner) {
                arrow.setDeltaMovement(projectile.getDeltaMovement().scale(1.15D));
                arrow.hasImpulse = true;
            }
        }
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        // Spiritual Gale Lance: pierces 2, drags struck targets backward along vortex!
        if (hitEntity instanceof LivingEntity living && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard()))) {
            Vec3 drag = projectile.getDeltaMovement().normalize().scale(0.95D);
            living.setDeltaMovement(drag.x, 0.30D, drag.z);
            living.hasImpulse = true;
        }
    }

    @Override
    public void onBeamFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double maxReach, List<LivingEntity> hitEntities, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Pranic Jetstream Tether: repels hostiles, siphons 20% into fuel, vertical jetpack when aimed down, grants Slow Falling & +30% speed to allies!
        int tier = spell.getEffectiveTier();
        for (LivingEntity hit : hitEntities) {
            if (hit == caster || hit.isAlliedTo(caster) || hit instanceof Player) {
                hit.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0));
                hit.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 1));
            } else if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                hit.setDeltaMovement(direction.x * 1.05D, 0.32D, direction.z * 1.05D);
                hit.hasImpulse = true;
                siphonAuraFuel(caster, tier, 0.20F);
            }
        }
        if (direction.y < -0.35D) {
            caster.setDeltaMovement(caster.getDeltaMovement().x, 0.72D, caster.getDeltaMovement().z);
            caster.hasImpulse = true;
            caster.resetFallDistance();
        }
    }

    @Override
    public void onRadialBurstFormCast(ServerLevel level, LivingEntity caster, Vec3 center, double radius, Vec3 direction, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Spiritual Vacuum Implosion: 6m pulls mobs to center, silences abilities 2s (40t), vacuums dropped items directly to caster!
        double r = Math.max(6.0D, radius);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 3.0D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 in = center.subtract(foe.position()).normalize().scale(1.15D);
            foe.setDeltaMovement(in.x, 0.28D, in.z);
            foe.hasImpulse = true;
            SpellLingeringZoneManager.applyArcaneSilence(foe, 40);
        }
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, box)) {
            if (item.isAlive()) {
                item.teleportTo(caster.getX(), caster.getY() + 0.5D, caster.getZ());
                item.setNoPickUpDelay();
            }
        }
        level.playSound(null, BlockPos.containing(center), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.9F, 1.35F);
    }

    @Override
    public void onTouchStrikeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, List<LivingEntity> hitEntities, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Chakra Disruptor Palm: silences abilities & spellcasting for 4s (80t) while siphoning 30% back into caster's Materia fuel!
        int tier = spell.getEffectiveTier();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellLingeringZoneManager.applyArcaneSilence(hit, 80);
            siphonAuraFuel(caster, tier, 0.30F);
        }
    }

    @Override
    public void onAuraFormCast(ServerLevel level, LivingEntity caster, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Chakra Respiration Shroud: -20% spell Materia fuel cost, drowning/suffocation immunity, passive saturation regen!
        int dur = 240 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_AURA_SHROUD_UNTIL, level.getGameTime() + dur);
        level.sendParticles(ParticleTypes.END_ROD, caster.getX(), caster.getY(0.9D), caster.getZ(), 20, 0.45D, 0.5D, 0.45D, 0.04D);
    }

    @Override
    public boolean onGlyphFormCast(ServerLevel level, LivingEntity caster, Vec3 glyphCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Chakra Disruption Glyph: silences abilities & spellcasting for 5s (100t) and strips 25% melee attack power!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 3.5D, 400, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed < 6) return true;
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB box = new AABB(zone.center.x - 3.5D, zone.center.y - 1.0D, zone.center.z - 3.5D,
                            zone.center.x + 3.5D, zone.center.y + 2.0D, zone.center.z + 3.5D);
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != owner);
                    if (stepped.isEmpty()) return true;

                    for (LivingEntity v : stepped) {
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                        SpellLingeringZoneManager.applyArcaneSilence(v, 100);
                        SpellCombatReactionEngine.applyTimedAttackDebuff(v, srv, 0.25D, 0.0D, 100L);
                        v.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, Vec3.ZERO, zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                    }
                    srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 0.6D, zone.center.z, 24, 1.5D, 0.4D, 1.5D, 0.05D);
                    return false;
                }
        ));
        return true;
    }

    @Override
    public void onConeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double coneRange, List<LivingEntity> hitTargets, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Pranic Gale Deluge: 8m knockback, deflects arrows/fireballs, extinguishes fires, silences 2.5s (50t); allies cleansed & +25% speed!
        for (LivingEntity hit : hitTargets) {
            hit.clearFire();
            if (hit == caster || hit.isAlliedTo(caster) || hit instanceof Player) {
                SpellCombatReactionEngine.purifyHexesAndNegativeEffects(hit);
                hit.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 0));
            } else if (!SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) {
                hit.setDeltaMovement(direction.x * 1.70D, 0.42D, direction.z * 1.70D);
                hit.hasImpulse = true;
                SpellLingeringZoneManager.applyArcaneSilence(hit, 50);
            }
        }
        AABB coneBox = caster.getBoundingBox().expandTowards(direction.scale(coneRange)).inflate(2.5D);
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, coneBox)) {
            if (proj.getOwner() != caster) {
                proj.setDeltaMovement(direction.scale(1.5D));
                proj.hasImpulse = true;
            }
        }
        int steps = Math.min(8, (int) Math.ceil(coneRange));
        for (int s = 1; s <= steps; s++) {
            BlockPos bp = BlockPos.containing(caster.position().add(direction.scale(s)));
            for (BlockPos p : BlockPos.betweenClosed(bp.offset(-2, -1, -2), bp.offset(2, 1, 2))) {
                if (level.getBlockState(p).getBlock() instanceof BaseFireBlock) {
                    level.removeBlock(p, false);
                }
            }
        }
    }

    @Override
    public boolean onSkyfallFormCast(ServerLevel level, LivingEntity caster, Vec3 impactCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Spiritual Gale Barrage: 6s (120t) dropping atmospheric torpedoes creating 3m updraft geysers launching enemies 10m upward!
        double r = Math.max(6.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 120, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        double ang = srv.random.nextDouble() * Math.PI * 2.0D;
                        double dist = srv.random.nextDouble() * Math.max(1.0D, zone.radius - 2.0D);
                        Vec3 torpedoPos = zone.center.add(Math.cos(ang) * dist, 0.0D, Math.sin(ang) * dist);
                        srv.sendParticles(ParticleTypes.GUST_EMITTER_LARGE, torpedoPos.x, torpedoPos.y + 0.5D, torpedoPos.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                        srv.playSound(null, BlockPos.containing(torpedoPos), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.85F, 1.15F);

                        AABB geyser = new AABB(torpedoPos.x - 3.0D, torpedoPos.y - 1.5D, torpedoPos.z - 3.0D,
                                torpedoPos.x + 3.0D, torpedoPos.y + 3.5D, torpedoPos.z + 3.0D);
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, geyser, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            v.setDeltaMovement(v.getDeltaMovement().x * 0.25D, 1.28D, v.getDeltaMovement().z * 0.25D); // ~10 blocks upward!
                            v.hasImpulse = true;
                            SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, new Vec3(0, 1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.65F);
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
        tag.putString("SpellEidolonMateria", EssenceType.AURA.name());
        tag.putInt("SpellEidolonTintRGB", PRANIC_SPIRITUALIST_HEX_COLOR);
        level.sendParticles(ParticleTypes.END_ROD, spawnPos.x, spawnPos.y + 0.9D, spawnPos.z, 22, 0.45D, 0.55D, 0.45D, 0.05D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Pranic Slipstream Blink: zero-vibration silent teleport leaving a vacuum pop dragging & disorienting foes + grants caster Slow Falling!
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, level.getGameTime() + 80L);
        AABB depBox = new AABB(departurePos.x - 3.5D, departurePos.y - 1.5D, departurePos.z - 3.5D,
                departurePos.x + 3.5D, departurePos.y + 2.5D, departurePos.z + 3.5D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, depBox)) {
            if (foe == caster || foe.isAlliedTo(caster)) continue;
            Vec3 in = departurePos.subtract(foe.position()).normalize().scale(0.85D);
            foe.setDeltaMovement(in.x, 0.25D, in.z);
            foe.hasImpulse = true;
            foe.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 60, 0));
        }
        caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0));
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Aerated Purifying Transmute: clears lingering potion clouds & cobwebs, dries sponges, purifies mud/tainted soil & wither roses!
        boolean clearedCloud = false;
        for (AreaEffectCloud cloud : level.getEntitiesOfClass(AreaEffectCloud.class, new AABB(pos).inflate(4.0D))) {
            cloud.discard();
            clearedCloud = true;
        }
        if (state.getBlock() instanceof BaseFireBlock) {
            level.removeBlock(pos, false);
            return true;
        }
        if (state.getBlock() instanceof CampfireBlock && state.hasProperty(CampfireBlock.LIT) && state.getValue(CampfireBlock.LIT)) {
            level.setBlockAndUpdate(pos, state.setValue(CampfireBlock.LIT, false));
            return true;
        }
        if (state.is(Blocks.COBWEB)) {
            level.destroyBlock(pos, true, caster);
            return true;
        }
        if (state.is(Blocks.WET_SPONGE)) {
            level.setBlockAndUpdate(pos, Blocks.SPONGE.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.MUD)) {
            level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT)) {
            level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.WITHER_ROSE)) {
            level.setBlockAndUpdate(pos, Blocks.DANDELION.defaultBlockState());
            return true;
        }
        return clearedCloud;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack in = itemEntity.getItem();
        if (in.is(Items.WET_SPONGE)) {
            itemEntity.setItem(new ItemStack(Items.SPONGE, in.getCount()));
            return true;
        }
        if (in.is(Items.POISONOUS_POTATO)) {
            itemEntity.setItem(new ItemStack(Items.POTATO, in.getCount()));
            return true;
        }
        if (in.is(Items.ROTTEN_FLESH)) {
            itemEntity.setItem(new ItemStack(Items.LEATHER, in.getCount()));
            return true;
        }
        if (in.is(Items.WITHER_ROSE)) {
            itemEntity.setItem(new ItemStack(Items.DANDELION, in.getCount()));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Chakra Severance Hex: silences abilities & spellcasting, continuous suffocation tick damage, 2x knockback taken (in interceptWardDamage)
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        SpellLingeringZoneManager.applyArcaneSilence(target, 40);
        target.hurtServer(level, level.damageSources().inWall(), (1.4F + potencyTier * 0.45F) * mult);
        level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.85D), target.getZ(), 5, 0.25D, 0.3D, 0.25D, 0.02D);
        return true;
    }

    private static void siphonAuraFuel(LivingEntity caster, int tier, float costFraction) {
        if (caster instanceof Player player) {
            SpellCombatReactionEngine.siphonCasterMateriaFuel(player, EssenceType.AURA, tier, costFraction);
        }
    }
}
