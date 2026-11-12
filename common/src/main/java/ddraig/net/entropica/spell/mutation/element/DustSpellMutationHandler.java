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
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Complete per-part Signature Mutation implementation for <b>Dust Materia (Tier 1 Fusion #17)</b>
 * (Spellcrafting System Guide Section 6.18).
 */
public final class DustSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int DUST_STALKER_HEX_COLOR = 0xC2B28F;

    @Override
    public EssenceType materiaType() {
        return EssenceType.DUST;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Abrasive Sandblaster Chains";
            default -> "Abrasive Dust " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Choking Dust-Pod";
            case BEAM -> "Abrasive Sandblasting Jet";
            case RADIAL_BURST -> "Whiteout Powder Nova";
            case TOUCH_STRIKE -> "Choking Smother Strike";
            case AURA -> "Obscuring Chalk Veil";
            case GLYPH -> "Choking Powder Pitfall";
            case CONE -> "Abrasive Sandblasting Deluge";
            case SKYFALL -> "Whiteout Dust Storm Downpour";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Chalk-Step";
            case TRANSMUTE, BREAK -> "Sedimentary Abrasion & Polishing";
            case AFFLICTION -> "Choking Powder Smother Hex";
            case KINETIC -> "Pneumatic Dust-Blast";
            default -> "Abrasive " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations ---

    @Override
    public void onChainLeapStep(ServerLevel level, LivingEntity caster, LivingEntity fromTarget, LivingEntity toTarget, SpellAssembly spell, SubSpellSlot slot) {
        // Abrasive Sandblaster Chains: 1.5s Blindness, clears mob aggro, snuffs out torches along chain path!
        toTarget.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0));
        if (toTarget instanceof Mob mob) {
            mob.setTarget(null);
        }
        Vec3 start = fromTarget.position();
        Vec3 end = toTarget.position();
        double dist = start.distanceTo(end);
        Vec3 step = end.subtract(start).normalize();
        for (double d = 0.0D; d <= dist; d += 1.25D) {
            BlockPos bp = BlockPos.containing(start.add(step.scale(d)));
            snuffTorchesAndFires(level, bp, 1);
            level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                    bp.getX() + 0.5D, bp.getY() + 0.8D, bp.getZ() + 0.5D, 3, 0.15D, 0.15D, 0.15D, 0.02D);
        }
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.1D, 0.1D, 0.1D, 0.01D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        // Choking Dust-Pod: 2.5s (50t) Blindness, extinguishes torches/fires in 2m, polishes rough stone!
        if (hitEntity instanceof LivingEntity living && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard()))) {
            living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
        }
        BlockPos impact = BlockPos.containing(projectile.position());
        snuffTorchesAndFires(level, impact, 2);
        polishBlocksInArea(level, impact, 2, false);
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        snuffTorchesAndFires(level, blockHit.getBlockPos(), 2);
        polishBlocksInArea(level, blockHit.getBlockPos(), 2, false);
    }

    @Override
    public void onBeamFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double maxReach, List<LivingEntity> hitEntities, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Abrasive Sandblasting Jet: blinds enemies, shoves back, polishes stone, snuffs torches/fires!
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
            hit.setDeltaMovement(direction.x * 0.85D, 0.20D, direction.z * 0.85D);
            hit.hasImpulse = true;
        }
        int steps = Math.min(16, (int) Math.ceil(maxReach));
        for (int s = 1; s <= steps; s++) {
            BlockPos bp = BlockPos.containing(caster.getEyePosition().add(direction.scale(s)));
            snuffTorchesAndFires(level, bp, 1);
            polishBlocksInArea(level, bp, 1, false);
        }
    }

    @Override
    public void onRadialBurstFormCast(ServerLevel level, LivingEntity caster, Vec3 center, double radius, Vec3 direction, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Whiteout Powder Nova: 6m radius, 4s (80t) Blindness, resets mob aggro, snuffs torches/fires, polishes stone!
        double r = Math.max(6.0D, radius);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 2.5D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            if (foe instanceof Mob mob) {
                mob.setTarget(null);
            }
        }
        BlockPos cPos = BlockPos.containing(center);
        snuffTorchesAndFires(level, cPos, 6);
        polishBlocksInArea(level, cPos, 4, false);
        level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                center.x, center.y + 1.0D, center.z, 50, 2.5D, 0.8D, 2.5D, 0.08D);
    }

    @Override
    public void onTouchStrikeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, List<LivingEntity> hitEntities, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Choking Smother Strike: silences spellcasting & mob abilities for 4s (80t) + suffocation tick damage!
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellLingeringZoneManager.applyArcaneSilence(hit, 80);
            hit.hurtServer(level, level.damageSources().inWall(), 2.0F + spell.getEffectiveTier() * 0.5F);
            final java.util.UUID victimId = hit.getUUID();
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, hit.position(), 1.5D, 80, now, spell, slot, fallbackMateria, apparatusPotency,
                    (srv, zone, elapsed) -> {
                        if (elapsed > zone.maxDurationTicks) return false;
                        if (elapsed % 20 == 0) {
                            Entity e = srv.getEntity(victimId);
                            if (e instanceof LivingEntity lv && lv.isAlive()) {
                                lv.hurtServer(srv, srv.damageSources().inWall(), 1.5F);
                            } else {
                                return false;
                            }
                        }
                        return true;
                    }
            ));
        }
    }

    @Override
    public void onAuraFormCast(ServerLevel level, LivingEntity caster, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Obscuring Chalk Veil
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_DUST_VEIL_UNTIL, level.getGameTime() + dur);
        level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                caster.getX(), caster.getY(0.8D), caster.getZ(), 24, 0.45D, 0.5D, 0.45D, 0.05D);
    }

    @Override
    public boolean onGlyphFormCast(ServerLevel level, LivingEntity caster, Vec3 glyphCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Choking Powder Pitfall: 2-block deep pit of suffocating dust trapping for 4s (80t) + continuous suffocation damage!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 2.5D, 400, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed < 6) return true;
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB trigger = new AABB(zone.center.x - 2.0D, zone.center.y - 1.0D, zone.center.z - 2.0D,
                            zone.center.x + 2.0D, zone.center.y + 2.0D, zone.center.z + 2.0D);
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, trigger, e -> e.isAlive() && e != owner);
                    if (stepped.isEmpty()) return true;

                    Map<BlockPos, BlockState> pitBlocks = new HashMap<>();
                    BlockPos floor = BlockPos.containing(zone.center).below();
                    for (BlockPos p : BlockPos.betweenClosed(floor.offset(-1, -1, -1), floor.offset(1, 0, 1))) {
                        BlockState bs = srv.getBlockState(p);
                        if (!bs.isAir() && !SpellExecutionEngine.isProtectedBlockOrContainer(srv, p, bs) && bs.getDestroySpeed(srv, p) >= 0.0F) {
                            BlockPos imm = p.immutable();
                            pitBlocks.put(imm, bs);
                            srv.removeBlock(imm, false);
                        }
                    }

                    AABB pitArea = new AABB(zone.center.x - 2.0D, zone.center.y - 3.0D, zone.center.z - 2.0D,
                            zone.center.x + 2.0D, zone.center.y + 1.5D, zone.center.z + 2.0D);
                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 2.0D, 80, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (psrv, pzone, pelapsed) -> {
                                if (pelapsed >= pzone.maxDurationTicks) {
                                    pitBlocks.forEach((pos, orig) -> {
                                        if (psrv.getBlockState(pos).isAir()) {
                                            psrv.setBlockAndUpdate(pos, orig);
                                        }
                                    });
                                    return false;
                                }
                                LivingEntity pOwner = pzone.resolveCaster(psrv);
                                for (LivingEntity v : psrv.getEntitiesOfClass(LivingEntity.class, pitArea, e -> e.isAlive() && e != pOwner)) {
                                    if (pOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, pOwner, pzone.spell.hasWardFriendlyFireSafeguard())) continue;
                                    v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 6));
                                    v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 35, 0));
                                    if (pelapsed % 20 == 0) {
                                        v.hurtServer(psrv, psrv.damageSources().inWall(), 2.0F + pzone.spell.getEffectiveTier() * 0.5F);
                                        SpellExecutionEngine.deliverSubSpellOnEntity(psrv, pOwner, v, Vec3.ZERO, pzone.spell, pzone.slot, pzone.fallbackMateria, pzone.apparatusPotency * 0.35F);
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
        // Abrasive Sandblasting Deluge: knockback, 3s Blindness, snuffs torches/fires, polishes stone/deepslate/blackstone/quartz, scrapes copper wax/oxidation, dries mud -> clay!
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            hit.setDeltaMovement(direction.x * 1.15D, 0.30D, direction.z * 1.15D);
            hit.hasImpulse = true;
        }
        int steps = Math.min(8, (int) Math.ceil(coneRange));
        for (int s = 1; s <= steps; s++) {
            BlockPos stepPos = BlockPos.containing(caster.position().add(direction.scale(s)));
            snuffTorchesAndFires(level, stepPos, 2);
            polishBlocksInArea(level, stepPos, 2, true);
        }
    }

    @Override
    public boolean onSkyfallFormCast(ServerLevel level, LivingEntity caster, Vec3 impactCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Whiteout Dust Storm Downpour: 14m chalk dust storm for 15s (300t) blinding, clearing aggro, snuffing torches/fires, polishing stone!
        double r = Math.max(7.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 300, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 5 == 0) {
                        srv.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                                zone.center.x, zone.center.y + 4.5D, zone.center.z, 28, zone.radius * 0.55D, 1.4D, zone.radius * 0.55D, 0.08D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        BlockPos cPos = BlockPos.containing(zone.center);
                        int br = Math.min(7, (int) Math.ceil(zone.radius));
                        snuffTorchesAndFires(srv, cPos, br);
                        polishBlocksInArea(srv, cPos, Math.min(5, br), false);

                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 5.0D, zone.center.z + zone.radius);
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
                            if (v instanceof Mob mob) {
                                mob.setTarget(null);
                            }
                            SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.30F);
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
        tag.putString("SpellEidolonMateria", EssenceType.DUST.name());
        tag.putInt("SpellEidolonTintRGB", DUST_STALKER_HEX_COLOR);
        level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                spawnPos.x, spawnPos.y + 0.8D, spawnPos.z, 26, 0.45D, 0.55D, 0.45D, 0.06D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Chalk-Step: leaves a 3s (60t) blinding chalk decoy cloud at departure that lures hostiles, and arrives silently with zero vibration!
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_SILENT_VIBRATION_UNTIL, level.getGameTime() + 100L);
        AABB depBox = new AABB(departurePos.x - 4.5D, departurePos.y - 2.0D, departurePos.z - 4.5D,
                departurePos.x + 4.5D, departurePos.y + 2.5D, departurePos.z + 4.5D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, depBox)) {
            if (foe == caster || foe.isAlliedTo(caster)) continue;
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            if (foe instanceof Mob mob) {
                mob.setTarget(null);
            }
        }
        SpellAssembly dummy = SpellAssembly.createMagicMissileStarter(potencyTier);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, departurePos, 4.5D, 60, level.getGameTime(), dummy, dummy.subSpells().get(0), EssenceType.DUST, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 5 == 0) {
                        srv.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                                departurePos.x, departurePos.y + 1.0D, departurePos.z, 12, 0.55D, 0.65D, 0.55D, 0.03D);
                    }
                    LivingEntity owner = zone.resolveCaster(srv);
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, depBox)) {
                        if (foe == owner || !foe.isAlive() || (owner != null && foe.isAlliedTo(owner))) continue;
                        foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 45, 0));
                        if (foe instanceof Mob mob) {
                            mob.setTarget(null);
                            if (mob instanceof Monster && elapsed % 10 == 0) {
                                mob.getNavigation().moveTo(departurePos.x, departurePos.y, departurePos.z, 1.1D);
                            }
                        }
                    }
                    return true;
                }
        ));
        level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                departurePos.x, departurePos.y + 0.9D, departurePos.z, 30, 0.7D, 0.6D, 0.7D, 0.05D);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Sedimentary Abrasion & Polishing
        return polishOrAbradeBlock(level, pos, state, true);
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack in = itemEntity.getItem();
        if (in.is(Items.COBBLESTONE)) {
            itemEntity.setItem(new ItemStack(Items.STONE, in.getCount()));
            return true;
        }
        if (in.is(Items.STONE)) {
            itemEntity.setItem(new ItemStack(Items.SMOOTH_STONE, in.getCount()));
            return true;
        }
        if (in.is(Items.COBBLED_DEEPSLATE) || in.is(Items.DEEPSLATE)) {
            itemEntity.setItem(new ItemStack(Items.POLISHED_DEEPSLATE, in.getCount()));
            return true;
        }
        if (in.is(Items.BLACKSTONE)) {
            itemEntity.setItem(new ItemStack(Items.POLISHED_BLACKSTONE, in.getCount()));
            return true;
        }
        if (in.is(Items.QUARTZ_BLOCK)) {
            itemEntity.setItem(new ItemStack(Items.SMOOTH_QUARTZ, in.getCount()));
            return true;
        }
        if (in.is(Items.SANDSTONE)) {
            itemEntity.setItem(new ItemStack(Items.SMOOTH_SANDSTONE, in.getCount()));
            return true;
        }
        if (in.is(Items.RED_SANDSTONE)) {
            itemEntity.setItem(new ItemStack(Items.SMOOTH_RED_SANDSTONE, in.getCount()));
            return true;
        }
        if (in.is(Items.BASALT)) {
            itemEntity.setItem(new ItemStack(Items.SMOOTH_BASALT, in.getCount()));
            return true;
        }
        if (in.is(Items.ANDESITE)) {
            itemEntity.setItem(new ItemStack(Items.POLISHED_ANDESITE, in.getCount()));
            return true;
        }
        if (in.is(Items.DIORITE)) {
            itemEntity.setItem(new ItemStack(Items.POLISHED_DIORITE, in.getCount()));
            return true;
        }
        if (in.is(Items.GRANITE)) {
            itemEntity.setItem(new ItemStack(Items.POLISHED_GRANITE, in.getCount()));
            return true;
        }
        if (in.is(Items.TUFF)) {
            itemEntity.setItem(new ItemStack(Items.POLISHED_TUFF, in.getCount()));
            return true;
        }
        if (in.is(Items.MUD)) {
            itemEntity.setItem(new ItemStack(Items.CLAY, in.getCount()));
            return true;
        }
        if (in.getItem() instanceof BlockItem bi) {
            Block block = bi.getBlock();
            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
            if (key.getPath().startsWith("waxed_")) {
                ResourceLocation unwaxedId = ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getPath().substring(6));
                Block unwaxed = BuiltInRegistries.BLOCK.getValue(unwaxedId);
                Item outItem = unwaxed.asItem();
                if (unwaxed != Blocks.AIR && outItem != Items.AIR) {
                    itemEntity.setItem(new ItemStack(outItem, in.getCount()));
                    return true;
                }
            }
            Optional<BlockState> deoxidized = WeatheringCopper.getPrevious(block.defaultBlockState());
            if (deoxidized.isPresent()) {
                Item outItem = deoxidized.get().getBlock().asItem();
                if (outItem != Items.AIR) {
                    itemEntity.setItem(new ItemStack(outItem, in.getCount()));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Choking Powder Smother Hex: Blindness, silences abilities, suffocation tick damage, steadily erodes physical armor!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0, true, true));
        SpellLingeringZoneManager.applyArcaneSilence(target, 35);
        stackArmorErosion(target, 1, 50);
        target.hurtServer(level, level.damageSources().inWall(), (1.4F + potencyTier * 0.45F) * mult);
        level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                target.getX(), target.getY(0.85D), target.getZ(), 6, 0.25D, 0.3D, 0.25D, 0.02D);
        return true;
    }

    @Override
    public void onEffectAppliedToEntity(ServerLevel level, @Nullable LivingEntity caster, LivingEntity target, Vec3 direction, SpellCoreEffect effect, int potencyTier, SpellAssembly spell) {
        if (effect == SpellCoreEffect.KINETIC && target != caster) {
            // Pneumatic Dust-Blast with Armor Erosion: 10m blast + applies Armor Erosion (-5% per level, up to Level 20 = 100%)!
            int levelsToAdd = Math.max(2, 1 + potencyTier * 2);
            stackArmorErosion(target, levelsToAdd, 200);
            if (!SpellCombatReactionEngine.isBossEntity(level, target)) {
                target.setDeltaMovement(direction.x * 1.65D, 0.38D, direction.z * 1.65D);
                target.hasImpulse = true;
            }
            snuffTorchesAndFires(level, target.blockPosition(), 2);
            level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SANDSTONE.defaultBlockState()),
                    target.getX(), target.getY(0.7D), target.getZ(), 18, 0.4D, 0.4D, 0.4D, 0.06D);
        }
    }

    private static void stackArmorErosion(LivingEntity target, int addLevels, int durationTicks) {
        var holder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.ARMOR_EROSION.get());
        MobEffectInstance cur = target.getEffect(holder);
        int nextAmp = cur == null ? Math.min(19, addLevels - 1) : Math.min(19, cur.getAmplifier() + addLevels);
        int dur = cur == null ? durationTicks : Math.max(cur.getDuration(), durationTicks);
        target.addEffect(new MobEffectInstance(holder, dur, nextAmp, false, true));
    }

    private static void snuffTorchesAndFires(ServerLevel level, BlockPos center, int r) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -1, -r), center.offset(r, 2, r))) {
            BlockState bs = level.getBlockState(p);
            if (bs.getBlock() instanceof BaseFireBlock) {
                level.removeBlock(p, false);
            } else if (bs.getBlock() instanceof CampfireBlock && bs.hasProperty(CampfireBlock.LIT) && bs.getValue(CampfireBlock.LIT)) {
                level.setBlockAndUpdate(p, bs.setValue(CampfireBlock.LIT, false));
            } else if (bs.getBlock() instanceof TorchBlock || bs.getBlock() instanceof WallTorchBlock) {
                level.destroyBlock(p, true);
            }
        }
    }

    private static void polishBlocksInArea(ServerLevel level, BlockPos center, int r, boolean fullAbrasion) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -1, -r), center.offset(r, 1, r))) {
            BlockState bs = level.getBlockState(p);
            if (!bs.isAir() && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) {
                polishOrAbradeBlock(level, p, bs, fullAbrasion);
            }
        }
    }

    public static boolean polishOrAbradeBlock(ServerLevel level, BlockPos pos, BlockState state, boolean fullAbrasion) {
        Block polished = null;
        if (state.is(Blocks.COBBLESTONE)) polished = Blocks.STONE;
        else if (state.is(Blocks.STONE)) polished = Blocks.SMOOTH_STONE;
        else if (state.is(Blocks.COBBLED_DEEPSLATE) || state.is(Blocks.DEEPSLATE)) polished = Blocks.POLISHED_DEEPSLATE;
        else if (state.is(Blocks.BLACKSTONE)) polished = Blocks.POLISHED_BLACKSTONE;
        else if (state.is(Blocks.QUARTZ_BLOCK)) polished = Blocks.SMOOTH_QUARTZ;
        else if (state.is(Blocks.SANDSTONE)) polished = Blocks.SMOOTH_SANDSTONE;
        else if (state.is(Blocks.RED_SANDSTONE)) polished = Blocks.SMOOTH_RED_SANDSTONE;
        else if (state.is(Blocks.BASALT)) polished = Blocks.SMOOTH_BASALT;
        else if (state.is(Blocks.ANDESITE)) polished = Blocks.POLISHED_ANDESITE;
        else if (state.is(Blocks.DIORITE)) polished = Blocks.POLISHED_DIORITE;
        else if (state.is(Blocks.GRANITE)) polished = Blocks.POLISHED_GRANITE;
        else if (state.is(Blocks.TUFF)) polished = Blocks.POLISHED_TUFF;
        else if (fullAbrasion && state.is(Blocks.MUD)) polished = Blocks.CLAY;

        if (polished != null) {
            level.setBlockAndUpdate(pos, polished.defaultBlockState());
            return true;
        }

        if (fullAbrasion) {
            // Scrape wax off waxed copper blocks
            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            if (key.getPath().startsWith("waxed_")) {
                ResourceLocation unwaxedId = ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getPath().substring(6));
                Block unwaxed = BuiltInRegistries.BLOCK.getValue(unwaxedId);
                if (unwaxed != Blocks.AIR) {
                    level.setBlockAndUpdate(pos, unwaxed.withPropertiesOf(state));
                    level.playSound(null, pos, SoundEvents.AXE_WAX_OFF, SoundSource.PLAYERS, 0.7F, 1.1F);
                    return true;
                }
            }
            // Scrape oxidation off copper blocks
            Optional<BlockState> deoxidized = WeatheringCopper.getPrevious(state);
            if (deoxidized.isPresent()) {
                level.setBlockAndUpdate(pos, deoxidized.get());
                level.playSound(null, pos, SoundEvents.AXE_SCRAPE, SoundSource.PLAYERS, 0.7F, 1.1F);
                return true;
            }
        }
        return false;
    }
}
