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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Complete per-part Signature Mutation implementation for <b>Lightning Materia (Base Type #12)</b>
 * (Spellcrafting System Guide Section 6.13).
 */
public final class LightningSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int VOLT_SPARK_HEX_COLOR = 0xFFE600;

    @Override
    public EssenceType materiaType() {
        return EssenceType.LIGHTNING;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case CHAIN_LEAP -> "Supercharged Forked Chains";
            default -> "Galvanic Voltaic " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Plasma Bolt (Ball Lightning)";
            case BEAM -> "Continuous Tesla Arc";
            case RADIAL_BURST -> "Static EMP Nova";
            case TOUCH_STRIKE -> "Overcharge Discharge Strike";
            case AURA -> "Static Shroud (Living Lightning Rod)";
            case GLYPH -> "Tesla Arc Mine";
            case CONE -> "Forked Arc Fan (Tesla Shotgun)";
            case SKYFALL -> "Thunderstorm Skyfall (Bolt Barrage)";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Thunderstep";
            case TRANSMUTE, BREAK -> "Galvanic Electrolysis & Smelting";
            case AFFLICTION -> "Galvanic Overcharge Hex";
            case KINETIC -> "Magnetic Railgun";
            default -> "Voltaic " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations ---

    @Override
    public void onChainLeapStep(ServerLevel level, LivingEntity caster, LivingEntity fromTarget, LivingEntity toTarget, SpellAssembly spell, SubSpellSlot slot) {
        // Supercharged Forked Chains: forks into an additional secondary branch at each leap (+50% reach if wet!)
        boolean wetBoost = SpellLingeringZoneManager.isEntityWetOrSoaked(fromTarget)
                || SpellLingeringZoneManager.isEntityWetOrSoaked(toTarget);
        double forkRange = wetBoost ? 9.0D : 6.0D;

        List<LivingEntity> branches = level.getEntitiesOfClass(
                LivingEntity.class,
                toTarget.getBoundingBox().inflate(forkRange),
                e -> e.isAlive() && e != caster && e != fromTarget && e != toTarget
                        && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, caster, spell.hasWardFriendlyFireSafeguard())
        );
        if (!branches.isEmpty()) {
            LivingEntity secondary = branches.get(0);
            Vec3 forkDir = secondary.position().subtract(toTarget.position()).normalize();
            SpellExecutionEngine.deliverSubSpellOnEntity(level, caster, secondary, forkDir, spell, slot, EssenceType.LIGHTNING, 0.70F);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, secondary.getX(), secondary.getY(0.7D), secondary.getZ(),
                    14, 0.25D, 0.35D, 0.25D, 0.1D);
        }
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, toTarget.getX(), toTarget.getY(0.7D), toTarget.getZ(),
                12, 0.25D, 0.35D, 0.25D, 0.1D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileSpawnedFromForm(ServerLevel level, SpellProjectileEntity projectile, LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        // Plasma Bolt (Ball Lightning): near-instant flight speed
        projectile.setDeltaMovement(projectile.getDeltaMovement().scale(1.45D));
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, projectile.getX(), projectile.getY(), projectile.getZ(),
                4, 0.12D, 0.12D, 0.12D, 0.08D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        // Stuns target for 1.0s (20t) and arcs bonus shock sparks to wet or metal-armored mobs within 3 blocks
        if (hitEntity instanceof LivingEntity primary) {
            if (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(primary, caster, spell.hasWardFriendlyFireSafeguard())) {
                applyParalysisStun(primary, 20);
            }
            arcSecondarySparks(level, caster, primary, 3.0D, 2.5F + spell.getEffectiveTier() * 0.5F, true, spell.hasWardFriendlyFireSafeguard());
        }
        level.playSound(null, projectile.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.55F, 1.65F);
    }

    @Override
    public void onBeamFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double maxReach, List<LivingEntity> hitEntities, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Continuous Tesla Arc: staggers target movement & conducts through water pools or wet entities
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            applyParalysisStun(hit, 14);
            if (SpellLingeringZoneManager.isEntityWetOrSoaked(hit)) {
                arcSecondarySparks(level, caster, hit, 4.5D, 2.0F + spell.getEffectiveTier() * 0.5F, false, spell.hasWardFriendlyFireSafeguard());
            }
        }
        Vec3 start = caster.getEyePosition();
        for (double d = 1.0D; d <= Math.min(maxReach, 20.0D); d += 0.8D) {
            Vec3 pt = start.add(direction.scale(d));
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pt.x, pt.y, pt.z, 2, 0.08D, 0.08D, 0.08D, 0.04D);
        }
    }

    @Override
    public void onTouchStrikeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, List<LivingEntity> hitEntities, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Overcharge Discharge Strike: 1.0s stun (20t), arcs within 3 blocks, grants caster 45% attack speed boost (Haste II) for 15s (300t)
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            applyParalysisStun(hit, 20);
            arcSecondarySparks(level, caster, hit, 3.0D, 2.5F + spell.getEffectiveTier() * 0.5F, false, spell.hasWardFriendlyFireSafeguard());
        }
        caster.addEffect(new MobEffectInstance(MobEffects.HASTE, 300, 1, false, true));
        level.playSound(null, caster.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.6F, 1.55F);
    }

    @Override
    public void onRadialBurstFormCast(ServerLevel level, LivingEntity caster, Vec3 center, double radius, Vec3 direction, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Static EMP Nova: 6-block electric burst locking movement for 1.5s (30t) + momentary level-15 pulse to adjacent redstone
        double r = Math.max(6.0D, radius);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 2.5D, center.z + r);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (e == caster || !e.isAlive()) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            applyParalysisStun(e, 30);
        }

        pulseNearbyRedstone(level, BlockPos.containing(center), 5, 20);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y + 0.8D, center.z, 45, r * 0.45D, 0.5D, r * 0.45D, 0.15D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.85F, 1.4F);
    }

    @Override
    public void onAuraFormCast(ServerLevel level, LivingEntity caster, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Static Shroud (Living Lightning Rod): +30% sprint speed, melee shock retaliation, absorbs lightning into full Materia fuel
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_LIGHTNING_SHROUD_UNTIL, level.getGameTime() + dur);
        caster.addEffect(new MobEffectInstance(MobEffects.SPEED, dur, 1, false, true));
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, caster.getX(), caster.getY(0.8D), caster.getZ(), 28, 0.45D, 0.6D, 0.45D, 0.12D);
    }

    @Override
    public boolean onGlyphFormCast(ServerLevel level, LivingEntity caster, Vec3 glyphCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Tesla Arc Mine: paralyzes triggering mob & up to 3 nearby enemies within 4 blocks for 2s (40t), powers redstone for 4s (80t)
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 4.0D, 400, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed < 6) return true;
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB trig = new AABB(zone.center.x - 2.0D, zone.center.y - 1.0D, zone.center.z - 2.0D,
                            zone.center.x + 2.0D, zone.center.y + 2.0D, zone.center.z + 2.0D);
                    if (srv.getEntitiesOfClass(LivingEntity.class, trig, e -> e.isAlive() && e != owner).isEmpty()) return true;

                    int shocked = 0;
                    for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, new AABB(BlockPos.containing(zone.center)).inflate(4.0D))) {
                        if (v == owner || !v.isAlive()) continue;
                        if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                        applyParalysisStun(v, 40);
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, Vec3.ZERO, zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                        if (++shocked >= 4) break;
                    }
                    pulseNearbyRedstone(srv, BlockPos.containing(zone.center), 4, 80);
                    srv.sendParticles(ParticleTypes.ELECTRIC_SPARK, zone.center.x, zone.center.y + 0.5D, zone.center.z, 36, 1.0D, 0.5D, 1.0D, 0.15D);
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.75F, 1.5F);
                    return false;
                }
        ));
        return true;
    }

    @Override
    public void onConeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double coneRange, List<LivingEntity> hitTargets, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Forked Arc Fan (Tesla Shotgun): 1.0s stun (20t), extends cone reach by 50% in rain or over water pools
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            applyParalysisStun(hit, 20);
        }

        if (caster.isInWaterOrRain()) {
            double extRange = coneRange * 1.5D;
            Vec3 eye = caster.getEyePosition();
            for (LivingEntity extra : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(extRange))) {
                if (extra == caster || !extra.isAlive() || hitTargets.contains(extra)) continue;
                if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(extra, caster, spell.hasWardFriendlyFireSafeguard())) continue;
                Vec3 toExtra = extra.getEyePosition().subtract(eye);
                if (toExtra.length() <= extRange && toExtra.normalize().dot(direction) >= 0.5D) {
                    applyParalysisStun(extra, 20);
                    SpellExecutionEngine.deliverSubSpellOnEntity(level, caster, extra, direction, spell, slot, fallbackMateria, apparatusPotency * 0.85F);
                }
            }
        }
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, caster.getX() + direction.x * 2.5D, caster.getEyeY() + direction.y * 2.5D, caster.getZ() + direction.z * 2.5D, 35, 0.8D, 0.4D, 0.8D, 0.18D);
    }

    @Override
    public boolean onSkyfallFormCast(ServerLevel level, LivingEntity caster, Vec3 impactCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Thunderstorm Skyfall (Bolt Barrage): crackling stormcloud striking enemies & electrifying water pools across a 12-block zone over 15s (300t)
        double r = Math.max(6.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 300, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 6 == 0) {
                        srv.sendParticles(ParticleTypes.CLOUD, zone.center.x, zone.center.y + 7.5D, zone.center.z, 10, zone.radius * 0.5D, 0.4D, zone.radius * 0.5D, 0.02D);
                        srv.sendParticles(ParticleTypes.ELECTRIC_SPARK, zone.center.x, zone.center.y + 7.0D, zone.center.z, 12, zone.radius * 0.5D, 0.6D, zone.radius * 0.5D, 0.1D);
                    }
                    if (elapsed % 25 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 6.0D, zone.center.z + zone.radius);
                        List<LivingEntity> candidates = srv.getEntitiesOfClass(LivingEntity.class, area,
                                e -> e.isAlive() && e != owner && !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(e, owner, zone.spell.hasWardFriendlyFireSafeguard()));

                        Vec3 boltPos;
                        LivingEntity victim = null;
                        if (!candidates.isEmpty()) {
                            victim = candidates.get(srv.random.nextInt(candidates.size()));
                            boltPos = victim.position();
                        } else {
                            double ang = srv.random.nextDouble() * Math.PI * 2.0D;
                            double dist = srv.random.nextDouble() * zone.radius;
                            boltPos = zone.center.add(Math.cos(ang) * dist, 0.0D, Math.sin(ang) * dist);
                        }

                        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(srv, EntitySpawnReason.TRIGGERED);
                        if (bolt != null) {
                            bolt.setPos(boltPos.x, boltPos.y, boltPos.z);
                            bolt.setVisualOnly(true);
                            srv.addFreshEntity(bolt);
                        }

                        if (victim != null) {
                            applyParalysisStun(victim, 20);
                            SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, victim, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.65F);
                            if (SpellLingeringZoneManager.isEntityWetOrSoaked(victim)) {
                                arcSecondarySparks(srv, owner, victim, 4.5D, 3.0F, false, zone.spell.hasWardFriendlyFireSafeguard());
                            }
                        }

                        // Electrify connected surface water pools when a bolt hits into or adjacent to water!
                        electrifyNearbyWaterPool(srv, owner, BlockPos.containing(boltPos), 3.0F + zone.spell.getEffectiveTier() * 0.6F, zone.spell.hasWardFriendlyFireSafeguard());
                    }
                    return true;
                }
        ));
        return true;
    }

    private static void electrifyNearbyWaterPool(ServerLevel level, @Nullable LivingEntity caster, BlockPos strikePos, float shockDamage, boolean wardSafeguard) {
        boolean hitWater = false;
        for (BlockPos p : BlockPos.betweenClosed(strikePos.offset(-2, -1, -2), strikePos.offset(2, 1, 2))) {
            if (level.getBlockState(p).is(Blocks.WATER)) {
                hitWater = true;
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.getX() + 0.5D, p.getY() + 1.0D, p.getZ() + 0.5D,
                        4, 0.35D, 0.1D, 0.35D, 0.08D);
            }
        }
        if (!hitWater) return;

        for (LivingEntity submerged : level.getEntitiesOfClass(LivingEntity.class, new AABB(strikePos).inflate(6.0D))) {
            if (submerged == caster || !submerged.isAlive()) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(submerged, caster, wardSafeguard)) continue;
            if (submerged.isInWater() || SpellLingeringZoneManager.isEntityWetOrSoaked(submerged)) {
                submerged.hurtServer(level, level.damageSources().lightningBolt(), shockDamage);
                applyParalysisStun(submerged, 20);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, submerged.getX(), submerged.getY(0.7D), submerged.getZ(),
                        12, 0.3D, 0.35D, 0.3D, 0.1D);
            }
        }
    }

    @Override
    public void onSummonFormSpawned(ServerLevel level, LivingEntity caster, Entity summonEntity, Vec3 spawnPos, double radius, SpellAssembly spell, SubSpellSlot slot) {
        CompoundTag tag = EntityHelper.getPersistentData(summonEntity);
        tag.putString("SpellEidolonMateria", EssenceType.LIGHTNING.name());
        tag.putInt("SpellEidolonTintRGB", VOLT_SPARK_HEX_COLOR);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, spawnPos.x, spawnPos.y + 0.8D, spawnPos.z, 28, 0.45D, 0.6D, 0.45D, 0.14D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Thunderstep: thunderclap teleport leaving an electrified arc line between departure and arrival that shocks entities crossing it
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.8F, 1.45F);
        Vec3 mid = departurePos.add(arrivalPos).scale(0.5D);
        double halfLen = departurePos.distanceTo(arrivalPos) * 0.5D + 1.0D;

        SpellAssembly dummy = SpellAssembly.createMagicMissileStarter(potencyTier);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, mid, halfLen, 60, level.getGameTime(), dummy, dummy.subSpells().get(0), EssenceType.LIGHTNING, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    Vec3 delta = arrivalPos.subtract(departurePos);
                    double len = Math.max(0.5D, delta.length());
                    Vec3 dir = delta.normalize();
                    if (elapsed % 5 == 0) {
                        for (double d = 0.0D; d <= len; d += 1.25D) {
                            Vec3 pt = departurePos.add(dir.scale(d));
                            srv.sendParticles(ParticleTypes.ELECTRIC_SPARK, pt.x, pt.y + 0.8D, pt.z, 2, 0.1D, 0.15D, 0.1D, 0.04D);
                        }
                    }
                    if (elapsed % 10 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        AABB lineBox = new AABB(departurePos, arrivalPos).inflate(1.2D);
                        for (LivingEntity crosser : srv.getEntitiesOfClass(LivingEntity.class, lineBox)) {
                            if (crosser == owner || !crosser.isAlive() || (owner != null && crosser.isAlliedTo(owner))) continue;
                            crosser.hurtServer(srv, srv.damageSources().lightningBolt(), 2.0F + potencyTier * 0.5F);
                            applyParalysisStun(crosser, 15);
                        }
                    }
                    return true;
                }
        ));
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Galvanic Electrolysis & Smelting: fuses sand into fulgurite glass, de-oxidizes copper, auto-smelts metal ores
        if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
            level.setBlockAndUpdate(pos, Blocks.GLASS.defaultBlockState());
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 12, 0.25D, 0.25D, 0.25D, 0.08D);
            return true;
        }
        Optional<BlockState> deoxidized = WeatheringCopper.getPrevious(state);
        if (deoxidized.isPresent()) {
            level.setBlockAndUpdate(pos, deoxidized.get());
            level.sendParticles(ParticleTypes.SCRAPE, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 10, 0.3D, 0.3D, 0.3D, 0.05D);
            return true;
        }
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness >= 0.0F && hardness <= 2.0F + potencyTier * 5.0F) {
            List<ItemStack> drops = Block.getDrops(state, level, pos, null, caster, ItemStack.EMPTY);
            boolean smeltedAny = false;
            for (ItemStack d : drops) {
                ItemStack res = NetherSpellMutationHandler.trySmeltStack(level, d);
                if (res != d) smeltedAny = true;
                Block.popResource(level, pos, res);
            }
            if (smeltedAny) {
                level.removeBlock(pos, false);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 10, 0.25D, 0.25D, 0.25D, 0.08D);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack in = itemEntity.getItem();
        if (in.isEmpty()) return false;
        if (in.is(Items.SAND) || in.is(Items.RED_SAND)) {
            itemEntity.setItem(new ItemStack(Items.GLASS, in.getCount()));
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, itemEntity.getX(), itemEntity.getY(0.3D), itemEntity.getZ(), 8, 0.2D, 0.2D, 0.2D, 0.08D);
            return true;
        }
        ItemStack smelted = NetherSpellMutationHandler.trySmeltStack(level, in);
        if (smelted != in) {
            itemEntity.setItem(smelted);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, itemEntity.getX(), itemEntity.getY(0.3D), itemEntity.getZ(), 8, 0.2D, 0.2D, 0.2D, 0.08D);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Galvanic Overcharge Hex: cuts attack/move speed by ~40%, disables shield blocking, arcs sparks to adjacent foes
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 1, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 35, 1, true, true));

        if (target instanceof Player player && player.isBlocking()) {
            ItemStack shield = player.getUseItem();
            player.stopUsingItem();
            if (!shield.isEmpty()) {
                player.getCooldowns().addCooldown(shield, 100);
            }
        }

        target.hurtServer(level, level.damageSources().lightningBolt(), (1.25F + potencyTier * 0.45F) * mult);
        for (LivingEntity adj : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(3.5D))) {
            if (adj == target || !adj.isAlive()) continue;
            adj.hurtServer(level, level.damageSources().lightningBolt(), (1.0F + potencyTier * 0.35F) * mult);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, adj.getX(), adj.getY(0.7D), adj.getZ(), 6, 0.2D, 0.3D, 0.2D, 0.08D);
        }
        return true;
    }

    @Override
    public void onEffectAppliedToEntity(ServerLevel level, @Nullable LivingEntity caster, LivingEntity target, Vec3 direction, SpellCoreEffect effect, int potencyTier, SpellAssembly spell) {
        if (effect == SpellCoreEffect.KINETIC) {
            // Magnetic Railgun: launches caster horizontally forward or flings metallic-armored entities with extreme velocity!
            Vec3 horiz = new Vec3(direction.x, 0.0D, direction.z);
            if (horiz.lengthSqr() < 1.0E-4D && caster != null) {
                horiz = new Vec3(caster.getLookAngle().x, 0.0D, caster.getLookAngle().z);
            }
            Vec3 launchDir = horiz.normalize();
            if (target == caster) {
                caster.setDeltaMovement(launchDir.x * 2.1D, 0.32D, launchDir.z * 2.1D);
                caster.hasImpulse = true;
            } else if (target.getArmorValue() > 0 || target.getMainHandItem().is(Items.IRON_SWORD) || target.getOffhandItem().is(Items.SHIELD)) {
                target.setDeltaMovement(launchDir.x * 2.35D, 0.45D, launchDir.z * 2.35D);
                target.hasImpulse = true;
            }
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY(0.7D), target.getZ(), 18, 0.3D, 0.35D, 0.3D, 0.15D);
        }
    }

    private static void applyParalysisStun(LivingEntity target, int durationTicks) {
        target.addEffect(new MobEffectInstance(
                BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), durationTicks, 0));
    }

    private static void arcSecondarySparks(
            ServerLevel level,
            @Nullable LivingEntity caster,
            LivingEntity origin,
            double radius,
            float damage,
            boolean requireWetOrMetalArmor,
            boolean wardSafeguard
    ) {
        for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class, origin.getBoundingBox().inflate(radius))) {
            if (other == origin || other == caster || !other.isAlive()) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(other, caster, wardSafeguard)) continue;
            if (requireWetOrMetalArmor && !SpellLingeringZoneManager.isEntityWetOrSoaked(other) && other.getArmorValue() < 6) {
                continue;
            }
            other.hurtServer(level, level.damageSources().lightningBolt(), damage);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, other.getX(), other.getY(0.7D), other.getZ(),
                    10, 0.25D, 0.35D, 0.25D, 0.1D);
        }
    }

    private static void pulseNearbyRedstone(ServerLevel level, BlockPos center, int rad, int durationTicks) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-rad, -1, -rad), center.offset(rad, 2, rad))) {
            BlockState bs = level.getBlockState(p);
            if (bs.is(Blocks.REDSTONE_WIRE) && bs.hasProperty(RedStoneWireBlock.POWER)) {
                level.setBlockAndUpdate(p, bs.setValue(RedStoneWireBlock.POWER, 15));
                level.scheduleTick(p, bs.getBlock(), durationTicks);
                level.updateNeighborsAt(p, bs.getBlock());
            } else if (bs.is(Blocks.REDSTONE_LAMP) && bs.hasProperty(BlockStateProperties.LIT)) {
                level.setBlockAndUpdate(p, bs.setValue(BlockStateProperties.LIT, true));
                level.scheduleTick(p, bs.getBlock(), durationTicks);
            }
        }
    }
}
