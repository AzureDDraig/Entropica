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
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Complete per-part Signature Mutation implementation for <b>Taiga Materia (Tier 1 Fusion #19)</b>
 * (Spellcrafting System Guide Section 6.20).
 */
public final class TaigaSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int BOREAL_HUNTER_HEX_COLOR = 0x3E8E7E;

    @Override
    public EssenceType materiaType() {
        return EssenceType.TAIGA;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case DOUBLE_BIFURCATE, TRIPLE_TRIFURCATE, DELAYED_BLOOM -> "Permafrost Fork Split";
            default -> "Boreal Permafrost " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Hardened Frost-Resin Spike";
            case BEAM -> "Glacial Amber Leeching Beam";
            case RADIAL_BURST -> "Boreal Blizzard Nova";
            case TOUCH_STRIKE -> "Heat-Siphon Conifer Touch";
            case AURA -> "Winter Solstice Shroud";
            case GLYPH -> "Frosted Conifer Briar Trap";
            case CONE -> "Frozen Sap Splatter Cone";
            case SKYFALL -> "Falling Glacial Timber Cataclysm";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Boreal Pine-Phasing Blink";
            case TRANSMUTE, BREAK -> "Boreal Timber & Podzol Transmute";
            case AFFLICTION -> "Petrifying Frost-Sap Hex";
            case RESTORATION -> "Evergreen Resilience Restoration";
            default -> "Boreal " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations ---

    @Override
    public void onProjectileImpactOrExpireFromFlow(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Vec3 splitOrigin, SpellAssembly spell, SubSpellSlot slot) {
        // Permafrost Fork Split: splits into 2 sub-zero resin shards upon impact or reaching maximum range!
        projectile.spawnSplitChild(level, splitOrigin, -22.0F, false, 0.70F, 1.35F);
        projectile.spawnSplitChild(level, splitOrigin, 22.0F, false, 0.70F, 1.35F);
        level.sendParticles(ParticleTypes.SNOWFLAKE, splitOrigin.x, splitOrigin.y, splitOrigin.z, 10, 0.25D, 0.25D, 0.25D, 0.05D);
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.SNOWFLAKE, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.01D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        // Hardened Frost-Resin Spike: pierces up to 2 enemies, pinning the SECOND victim to the ground in sticky freezing sap for 3s (60t)!
        if (!(hitEntity instanceof LivingEntity living)) return;
        if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard())) return;

        CompoundTag projData = EntityHelper.getPersistentData(projectile);
        int hitIdx = projData.getInt("TaigaSpikeHitCount").orElse(0) + 1;
        projData.putInt("TaigaSpikeHitCount", hitIdx);

        living.setTicksFrozen(Math.min(living.getTicksRequiredToFreeze() + 40, living.getTicksFrozen() + 60));
        if (hitIdx >= 2) {
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 6));
            living.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 60, 0));
            EntityHelper.getPersistentData(living).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, level.getGameTime() + 60L);
            living.setDeltaMovement(0.0D, Math.min(0.0D, living.getDeltaMovement().y), 0.0D);
            living.hasImpulse = true;
            level.playSound(null, living.blockPosition(), SoundEvents.HONEY_BLOCK_PLACE, SoundSource.PLAYERS, 0.9F, 0.8F);
        } else {
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 2));
        }
    }

    @Override
    public void onBeamFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double maxReach, List<LivingEntity> hitEntities, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Glacial Amber Leeching Beam: siphons enemy speed & toughness into caster + leaves 4s trail of frozen resin & snow!
        boolean drainedAny = false;
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2));
            SpellCombatReactionEngine.applyPercentageArmorShred(hit, level, 0.0D, 0.25D, 80L);
            drainedAny = true;
        }
        if (drainedAny) {
            caster.addEffect(new MobEffectInstance(MobEffects.SPEED, 80, 0));
            SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 0.0D, 2.0D, 0.0D, 0.0D, 80L);
        }
        int steps = Math.min(14, (int) Math.ceil(maxReach));
        List<Vec3> resinTrail = new ArrayList<>();
        for (int s = 1; s <= steps; s++) {
            Vec3 pt = caster.position().add(direction.scale(s));
            BlockPos p = BlockPos.containing(pt);
            if (s % 2 == 0) {
                resinTrail.add(pt);
            }
            if (level.getBlockState(p).isAir() && level.getBlockState(p.below()).isSolidRender() && Blocks.SNOW.defaultBlockState().canSurvive(level, p)) {
                level.setBlockAndUpdate(p, Blocks.SNOW.defaultBlockState());
            }
        }
        if (!resinTrail.isEmpty()) {
            Vec3 mid = caster.position().add(direction.scale(steps * 0.5D));
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, mid, steps * 0.55D, 80, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                    (srv, zone, elapsed) -> {
                        if (elapsed > zone.maxDurationTicks) return false;
                        LivingEntity owner = zone.resolveCaster(srv);
                        long lockUntil = srv.getGameTime() + 15L;
                        for (Vec3 node : resinTrail) {
                            if (elapsed % 8 == 0) {
                                srv.sendParticles(ParticleTypes.DRIPPING_HONEY, node.x, node.y + 0.15D, node.z, 2, 0.3D, 0.05D, 0.3D, 0.01D);
                            }
                            AABB seg = new AABB(node.x - 1.2D, node.y - 1.0D, node.z - 1.2D, node.x + 1.2D, node.y + 1.8D, node.z + 1.2D);
                            for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, seg, e -> e.isAlive() && e != owner)) {
                                if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                                foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 2, false, false));
                                foe.setTicksFrozen(Math.min(foe.getTicksRequiredToFreeze() + 20, foe.getTicksFrozen() + 8));
                                EntityHelper.getPersistentData(foe).putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, lockUntil);
                            }
                        }
                        return true;
                    }
            ));
        }
    }

    @Override
    public void onRadialBurstFormCast(ServerLevel level, LivingEntity caster, Vec3 center, double radius, Vec3 direction, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Boreal Blizzard Nova: 6m knockback, Slowness III & Freezing 4s (80t), snow layers & frost-rimed sweet berry bushes!
        double r = Math.max(6.0D, radius);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 2.5D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            Vec3 away = foe.position().subtract(center).normalize().scale(1.05D);
            foe.setDeltaMovement(away.x, 0.32D, away.z);
            foe.hasImpulse = true;
            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
            foe.setTicksFrozen(foe.getTicksRequiredToFreeze() + 80);
        }
        List<BlockPos> placedBushes = new ArrayList<>();
        BlockPos cPos = BlockPos.containing(center);
        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-4, -1, -4), cPos.offset(4, 1, 4))) {
            if (!level.getBlockState(p).isAir() || !level.getBlockState(p.below()).isSolidRender()) continue;
            if (level.random.nextFloat() < 0.14F && Blocks.SWEET_BERRY_BUSH.defaultBlockState().canSurvive(level, p)) {
                level.setBlockAndUpdate(p, Blocks.SWEET_BERRY_BUSH.defaultBlockState());
                placedBushes.add(p.immutable());
            } else if (level.random.nextFloat() < 0.35F && Blocks.SNOW.defaultBlockState().canSurvive(level, p)) {
                level.setBlockAndUpdate(p, Blocks.SNOW.defaultBlockState());
            }
        }
        if (!placedBushes.isEmpty()) {
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, center, r, 120, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                    (srv, zone, elapsed) -> {
                        if (elapsed < zone.maxDurationTicks) return true;
                        for (BlockPos bp : placedBushes) {
                            if (srv.getBlockState(bp).is(Blocks.SWEET_BERRY_BUSH)) srv.removeBlock(bp, false);
                        }
                        return false;
                    }
            ));
        }
    }

    @Override
    public void onTouchStrikeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, List<LivingEntity> hitEntities, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Heat-Siphon Conifer Touch: Freezing on foe + grants caster +30% speed & temporary Frost Walker for 10s (200t)!
        boolean hitAny = false;
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.setTicksFrozen(hit.getTicksRequiredToFreeze() + 80);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
            hitAny = true;
        }
        if (hitAny) {
            caster.addEffect(new MobEffectInstance(MobEffects.SPEED, 200, 1));
            EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_TAIGA_FROST_WALKER_UNTIL, level.getGameTime() + 200L);
        }
    }

    @Override
    public void onAuraFormCast(ServerLevel level, LivingEntity caster, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Winter Solstice Shroud
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_TAIGA_SHROUD_UNTIL, level.getGameTime() + dur);
        level.sendParticles(ParticleTypes.SNOWFLAKE, caster.getX(), caster.getY(0.8D), caster.getZ(), 24, 0.45D, 0.5D, 0.45D, 0.05D);
    }

    @Override
    public boolean onGlyphFormCast(ServerLevel level, LivingEntity caster, Vec3 glyphCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Frosted Conifer Briar Trap: traps victim for 4s (80t), Freezing + Slowness IV, leaves berry bushes & powder snow!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 3.0D, 400, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed < 6) return true;
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB box = new AABB(zone.center.x - 3.0D, zone.center.y - 1.0D, zone.center.z - 3.0D,
                            zone.center.x + 3.0D, zone.center.y + 2.0D, zone.center.z + 3.0D);
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != owner);
                    if (stepped.isEmpty()) return true;

                    for (LivingEntity v : stepped) {
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                        v.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 6));
                        v.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.INERTIAL_ANCHOR.get()), 80, 0));
                        v.setTicksFrozen(v.getTicksRequiredToFreeze() + 80);
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, Vec3.ZERO, zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                    }
                    List<BlockPos> hazard = new ArrayList<>();
                    BlockPos c = BlockPos.containing(zone.center);
                    for (BlockPos p : BlockPos.betweenClosed(c.offset(-1, 0, -1), c.offset(1, 0, 1))) {
                        if (srv.getBlockState(p).isAir()) {
                            BlockState place = srv.random.nextBoolean() && Blocks.SWEET_BERRY_BUSH.defaultBlockState().canSurvive(srv, p)
                                    ? Blocks.SWEET_BERRY_BUSH.defaultBlockState()
                                    : Blocks.POWDER_SNOW.defaultBlockState();
                            srv.setBlockAndUpdate(p, place);
                            hazard.add(p.immutable());
                        }
                    }
                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 2.0D, 100, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (hsrv, hzone, helapsed) -> {
                                if (helapsed < hzone.maxDurationTicks) return true;
                                for (BlockPos hp : hazard) {
                                    BlockState cur = hsrv.getBlockState(hp);
                                    if (cur.is(Blocks.SWEET_BERRY_BUSH) || cur.is(Blocks.POWDER_SNOW)) {
                                        hsrv.removeBlock(hp, false);
                                    }
                                }
                                return false;
                            }
                    ));
                    return false;
                }
        ));
        return true;
    }

    @Override
    public void onConeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double coneRange, List<LivingEntity> hitTargets, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Frozen Sap Splatter Cone: glues enemies to floor (60% Slowness) & makes them vulnerable (+50% shatter damage), leaving a 4s sticky floor-sap slick!
        long now = level.getGameTime();
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
            CompoundTag data = EntityHelper.getPersistentData(hit);
            data.putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, now + 80L);
            data.putLong(SpellLingeringZoneManager.TAG_TAIGA_BRITTLE_UNTIL, now + 120L);
        }
        Vec3 slickCenter = caster.position().add(direction.scale(Math.max(2.5D, coneRange * 0.5D)));
        double slickR = Math.max(3.5D, coneRange * 0.55D);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, slickCenter, slickR, 80, now, spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 8 == 0) {
                        srv.sendParticles(ParticleTypes.DRIPPING_HONEY, zone.center.x, zone.center.y + 0.2D, zone.center.z, 10, zone.radius * 0.45D, 0.1D, zone.radius * 0.45D, 0.01D);
                        srv.sendParticles(ParticleTypes.SNOWFLAKE, zone.center.x, zone.center.y + 0.3D, zone.center.z, 8, zone.radius * 0.45D, 0.15D, zone.radius * 0.45D, 0.01D);
                    }
                    LivingEntity owner = zone.resolveCaster(srv);
                    long cur = srv.getGameTime();
                    AABB slickBox = new AABB(zone.center.x - zone.radius, zone.center.y - 1.5D, zone.center.z - zone.radius,
                            zone.center.x + zone.radius, zone.center.y + 2.0D, zone.center.z + zone.radius);
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, slickBox, e -> e.isAlive() && e != owner)) {
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                        foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 2, false, false));
                        CompoundTag fd = EntityHelper.getPersistentData(foe);
                        fd.putLong(SpellLingeringZoneManager.TAG_NO_JUMP_UNTIL, cur + 20L);
                        fd.putLong(SpellLingeringZoneManager.TAG_TAIGA_BRITTLE_UNTIL, Math.max(fd.getLong(SpellLingeringZoneManager.TAG_TAIGA_BRITTLE_UNTIL).orElse(0L), cur + 60L));
                    }
                    return true;
                }
        ));
    }

    @Override
    public boolean onSkyfallFormCast(ServerLevel level, LivingEntity caster, Vec3 impactCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Falling Glacial Timber Cataclysm: 6s (120t) barrage of petrified frozen spruce logs & heavy icicles creating obstacle barricades!
        double r = Math.max(6.0D, radius);
        List<BlockPos> barricades = new ArrayList<>();
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 120, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) {
                        for (BlockPos bp : barricades) {
                            BlockState cur = srv.getBlockState(bp);
                            if (cur.is(Blocks.SPRUCE_LOG) || cur.is(Blocks.PACKED_ICE)) {
                                srv.removeBlock(bp, false);
                            }
                        }
                        return false;
                    }
                    if (elapsed % 5 == 0) {
                        srv.sendParticles(ParticleTypes.SNOWFLAKE, zone.center.x, zone.center.y + 5.5D, zone.center.z, 16, zone.radius * 0.5D, 1.2D, zone.radius * 0.5D, 0.06D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        // Drop temporary frozen spruce log & heavy packed-ice icicle barricade pillars in empty air!
                        for (int i = 0; i < 3; i++) {
                            double ang = srv.random.nextDouble() * Math.PI * 2.0D;
                            double dist = 1.5D + srv.random.nextDouble() * (zone.radius - 1.5D);
                            BlockPos logPos = BlockPos.containing(zone.center.add(Math.cos(ang) * dist, 0.0D, Math.sin(ang) * dist));
                            Block pillarMat = (i == 2) ? Blocks.PACKED_ICE : Blocks.SPRUCE_LOG;
                            if (srv.getBlockState(logPos).isAir() && srv.getBlockState(logPos.below()).isSolidRender()) {
                                srv.setBlockAndUpdate(logPos, pillarMat.defaultBlockState());
                                barricades.add(logPos.immutable());
                                if (srv.getBlockState(logPos.above()).isAir()) {
                                    srv.setBlockAndUpdate(logPos.above(), pillarMat.defaultBlockState());
                                    barricades.add(logPos.above().immutable());
                                }
                            }
                        }
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 5.0D, zone.center.z + zone.radius);
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            v.setTicksFrozen(v.getTicksRequiredToFreeze() + 40);
                            v.hurtServer(srv, srv.damageSources().fallingStalactite(owner != null ? owner : v), 2.5F + zone.spell.getEffectiveTier() * 0.6F);
                            SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.45F);
                        }
                        srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.WOOD_PLACE, SoundSource.PLAYERS, 0.85F, 0.75F);
                        srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.70F, 1.15F);
                    }
                    return true;
                }
        ));
        return true;
    }

    @Override
    public void onSummonFormSpawned(ServerLevel level, LivingEntity caster, Entity summonEntity, Vec3 spawnPos, double radius, SpellAssembly spell, SubSpellSlot slot) {
        CompoundTag tag = EntityHelper.getPersistentData(summonEntity);
        tag.putString("SpellEidolonMateria", EssenceType.TAIGA.name());
        tag.putInt("SpellEidolonTintRGB", BOREAL_HUNTER_HEX_COLOR);
        level.sendParticles(ParticleTypes.SNOWFLAKE, spawnPos.x, spawnPos.y + 0.8D, spawnPos.z, 24, 0.45D, 0.55D, 0.45D, 0.05D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Boreal Pine-Phasing Blink: leaves a 3s (60t) frosted decoy bush at departure that lures & chills pursuers!
        BlockPos dep = BlockPos.containing(departurePos).immutable();
        boolean placedBush = false;
        if (level.getBlockState(dep).isAir() && Blocks.SWEET_BERRY_BUSH.defaultBlockState().canSurvive(level, dep)) {
            level.setBlockAndUpdate(dep, Blocks.SWEET_BERRY_BUSH.defaultBlockState());
            placedBush = true;
        }
        final boolean fPlaced = placedBush;
        AABB lureBox = new AABB(departurePos.x - 4.5D, departurePos.y - 2.0D, departurePos.z - 4.5D,
                departurePos.x + 4.5D, departurePos.y + 2.5D, departurePos.z + 4.5D);
        SpellAssembly dummy = SpellAssembly.createMagicMissileStarter(potencyTier);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, departurePos, 4.5D, 60, level.getGameTime(), dummy, dummy.subSpells().get(0), EssenceType.TAIGA, 1.0F,
                (srv, zone, elapsed) -> {
                    if (elapsed >= zone.maxDurationTicks) {
                        if (fPlaced && srv.getBlockState(dep).is(Blocks.SWEET_BERRY_BUSH)) {
                            srv.removeBlock(dep, false);
                        }
                        return false;
                    }
                    LivingEntity owner = zone.resolveCaster(srv);
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, lureBox)) {
                        if (foe == owner || !foe.isAlive() || (owner != null && foe.isAlliedTo(owner))) continue;
                        if (foe instanceof Mob mob && mob instanceof Monster && elapsed % 10 == 0) {
                            mob.getNavigation().moveTo(departurePos.x, departurePos.y, departurePos.z, 1.15D);
                        }
                        if (foe.position().distanceToSqr(departurePos) <= 4.0D) {
                            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 1));
                            foe.setTicksFrozen(Math.min(foe.getTicksRequiredToFreeze() + 30, foe.getTicksFrozen() + 10));
                        }
                    }
                    return true;
                }
        ));
        level.sendParticles(ParticleTypes.SNOWFLAKE, departurePos.x, departurePos.y + 0.9D, departurePos.z, 24, 0.45D, 0.5D, 0.45D, 0.06D);
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Boreal Timber & Podzol Transmute: logs -> spruce_log, leaves -> spruce_leaves, grass/dirt -> podzol, water -> packed_ice!
        if (state.is(BlockTags.OVERWORLD_NATURAL_LOGS) && !state.is(Blocks.SPRUCE_LOG)) {
            level.setBlockAndUpdate(pos, Blocks.SPRUCE_LOG.withPropertiesOf(state));
            return true;
        }
        if (state.is(BlockTags.LEAVES) && !state.is(Blocks.SPRUCE_LEAVES)) {
            level.setBlockAndUpdate(pos, Blocks.SPRUCE_LEAVES.defaultBlockState());
            return true;
        }
        if (state.is(BlockTags.SAPLINGS) && !state.is(Blocks.SPRUCE_SAPLING)) {
            level.setBlockAndUpdate(pos, Blocks.SPRUCE_SAPLING.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT)) {
            level.setBlockAndUpdate(pos, Blocks.PODZOL.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.WATER)) {
            level.setBlockAndUpdate(pos, Blocks.PACKED_ICE.defaultBlockState());
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack in = itemEntity.getItem();
        if (in.is(ItemTags.LOGS_THAT_BURN) && !in.is(Items.SPRUCE_LOG)) {
            itemEntity.setItem(new ItemStack(Items.SPRUCE_LOG, in.getCount()));
            return true;
        }
        if (in.is(ItemTags.LEAVES) && !in.is(Items.SPRUCE_LEAVES)) {
            itemEntity.setItem(new ItemStack(Items.SPRUCE_LEAVES, in.getCount()));
            return true;
        }
        if (in.is(ItemTags.SAPLINGS) && !in.is(Items.SPRUCE_SAPLING)) {
            itemEntity.setItem(new ItemStack(Items.SPRUCE_SAPLING, in.getCount()));
            return true;
        }
        if (in.is(Items.GRASS_BLOCK) || in.is(Items.DIRT)) {
            itemEntity.setItem(new ItemStack(Items.PODZOL, in.getCount()));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Petrifying Frost-Sap Hex: Freezing & Slowness III, prevents sprinting, +50% physical shatter damage (in interceptWardDamage)
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.setTicksFrozen(target.getTicksRequiredToFreeze() + 45);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 2, true, true));
        target.setSprinting(false);
        target.hurtServer(level, level.damageSources().freeze(), (1.3F + potencyTier * 0.45F) * mult);
        level.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY(0.8D), target.getZ(), 6, 0.25D, 0.35D, 0.25D, 0.02D);
        return true;
    }

    @Override
    public void onEffectAppliedToEntity(ServerLevel level, @Nullable LivingEntity caster, LivingEntity target, Vec3 direction, SpellCoreEffect effect, int potencyTier, SpellAssembly spell) {
        if (effect == SpellCoreEffect.RESTORATION) {
            // Evergreen Resilience Restoration: Regeneration II, cures burning & hunger, 30s (600t) complete cold/freeze immunity!
            target.clearFire();
            target.removeEffect(MobEffects.HUNGER);
            target.setTicksFrozen(0);
            target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1));
            EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_TAIGA_COLD_IMMUNE_UNTIL, level.getGameTime() + 600L);
        }
    }
}
