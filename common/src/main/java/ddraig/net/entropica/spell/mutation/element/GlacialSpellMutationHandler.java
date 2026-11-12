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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Complete per-part Signature Mutation implementation for <b>Glacial Materia (Tier 1 Fusion #15)</b>
 * (Spellcrafting System Guide Section 6.16).
 */
public final class GlacialSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int GLACIAL_BRUISER_HEX_COLOR = 0x98DCF2;

    @Override
    public EssenceType materiaType() {
        return EssenceType.GLACIAL;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case SLOW_HEAVY -> "Permafrost Creep";
            default -> "Permafrost " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Glacial Ice-Lance";
            case BEAM -> "Absolute-Zero Cryo-Torrent Beam";
            case RADIAL_BURST -> "Hoarfrost Blizzard Shockwave";
            case TOUCH_STRIKE -> "Brittle Frost Strike";
            case AURA -> "Permafrost Cryo-Shroud";
            case GLYPH -> "Powder Snow Pitfall Trap";
            case CONE -> "Blizzard Shrapnel Fan";
            case SKYFALL -> "Glacial Monolith Cataclysm";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Ice Statue Blink";
            case TRANSMUTE, BREAK -> "Cryo-Solidification & Silk Touch";
            case AFFLICTION -> "Absolute-Zero Cryo-Hex";
            case RESTORATION -> "Cryo-Cocoon Mending";
            default -> "Glacial " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations ---

    @Override
    public float getFlowBonusAdvantageScore(SpellFlowModifier flow, LivingEntity target) {
        return flow == SpellFlowModifier.SLOW_HEAVY ? 0.4F : 0.0F;
    }

    @Override
    public void onProjectileTickFromFlow(ServerLevel level, SpellProjectileEntity projectile, SpellFlowModifier flow) {
        if (flow != SpellFlowModifier.SLOW_HEAVY) return;
        // Permafrost Creep: permanently freezes water beneath its wake into solid blue ice!
        BlockPos bp = projectile.blockPosition();
        for (BlockPos p : List.of(bp, bp.below())) {
            if (level.getBlockState(p).is(Blocks.WATER)) {
                level.setBlockAndUpdate(p, Blocks.BLUE_ICE.defaultBlockState());
                level.sendParticles(ParticleTypes.SNOWFLAKE, p.getX() + 0.5D, p.getY() + 1.0D, p.getZ() + 0.5D, 4, 0.25D, 0.1D, 0.25D, 0.02D);
            }
        }
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.SNOWFLAKE, projectile.getX(), projectile.getY(), projectile.getZ(), 2, 0.08D, 0.08D, 0.08D, 0.01D);
        BlockPos bp = projectile.blockPosition();
        if (level.getBlockState(bp).is(Blocks.WATER)) {
            level.setBlockAndUpdate(bp, Blocks.PACKED_ICE.defaultBlockState());
        }
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        // Glacial Ice-Lance: pierces up to 2 mobs (via SpellMutationRegistry), inflicts 60% Slowness (Slowness III) for 3s (60t)
        if (hitEntity instanceof LivingEntity living && (caster == null || !SpellCombatReactionEngine.isProtectedFromOffensiveSpell(living, caster, spell.hasWardFriendlyFireSafeguard()))) {
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2));
            living.setTicksFrozen(Math.min(living.getTicksRequiredToFreeze() + 40, living.getTicksFrozen() + 70));
        }
        freezeWaterPatch(level, BlockPos.containing(projectile.position()), 1, Blocks.PACKED_ICE);
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        freezeWaterPatch(level, blockHit.getBlockPos(), 2, Blocks.PACKED_ICE);
        level.playSound(null, blockHit.getBlockPos(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.7F, 1.3F);
    }

    @Override
    public void onBeamFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double maxReach, List<LivingEntity> hitEntities, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Absolute-Zero Cryo-Torrent Beam: stacks progressive slowness -> 2s solid freeze stasis, water -> packed ice, lava -> obsidian!
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            CompoundTag data = EntityHelper.getPersistentData(hit);
            long last = data.getLong("GlacialBeamLastTick").orElse(0L);
            int stacks = (now - last <= 40L) ? data.getInt("GlacialBeamStacks").orElse(0) + 1 : 1;
            data.putLong("GlacialBeamLastTick", now);
            data.putInt("GlacialBeamStacks", stacks);

            hit.setTicksFrozen(hit.getTicksRequiredToFreeze() + 40);
            if (stacks >= 3) {
                data.putInt("GlacialBeamStacks", 0);
                applySolidFreezeStasis(hit, 40);
                level.playSound(null, hit.blockPosition(), SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.9F, 1.4F);
            } else {
                hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, Math.min(3, stacks)));
            }
        }
        int steps = Math.min(16, (int) Math.ceil(maxReach));
        for (int s = 1; s <= steps; s++) {
            BlockPos bp = BlockPos.containing(caster.getEyePosition().add(direction.scale(s)));
            for (BlockPos p : BlockPos.betweenClosed(bp.offset(-1, -1, -1), bp.offset(1, 1, 1))) {
                BlockState bs = level.getBlockState(p);
                if (bs.is(Blocks.WATER)) {
                    level.setBlockAndUpdate(p, Blocks.PACKED_ICE.defaultBlockState());
                } else if (bs.is(Blocks.LAVA)) {
                    level.setBlockAndUpdate(p, Blocks.OBSIDIAN.defaultBlockState());
                }
            }
        }
    }

    @Override
    public void onRadialBurstFormCast(ServerLevel level, LivingEntity caster, Vec3 center, double radius, Vec3 direction, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Hoarfrost Blizzard Shockwave: 6m Blindness + 80% Slowness (Slowness IV) for 4s (80t) + snuffs open fires!
        double r = Math.max(6.0D, radius);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 2.5D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            foe.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 3));
            foe.setTicksFrozen(foe.getTicksRequiredToFreeze() + 50);
        }
        BlockPos cPos = BlockPos.containing(center);
        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-6, -1, -6), cPos.offset(6, 2, 6))) {
            if (level.getBlockState(p).getBlock() instanceof BaseFireBlock) {
                level.removeBlock(p, false);
            }
        }
        level.sendParticles(ParticleTypes.SNOWFLAKE, center.x, center.y + 0.8D, center.z, 45, 2.5D, 0.8D, 2.5D, 0.06D);
    }

    @Override
    public void onTouchStrikeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, List<LivingEntity> hitEntities, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Brittle Frost Strike: -30% armor toughness for 5s (100t) and Slowness IV!
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyPercentageArmorShred(hit, level, 0.0D, 0.30D, 100L);
            hit.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 3));
            hit.setTicksFrozen(hit.getTicksRequiredToFreeze() + 40);
        }
    }

    @Override
    public void onAuraFormCast(ServerLevel level, LivingEntity caster, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Permafrost Cryo-Shroud
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_GLACIAL_SHROUD_UNTIL, level.getGameTime() + dur);
        level.sendParticles(ParticleTypes.SNOWFLAKE, caster.getX(), caster.getY(0.8D), caster.getZ(), 24, 0.45D, 0.5D, 0.45D, 0.04D);
    }

    @Override
    public boolean onGlyphFormCast(ServerLevel level, LivingEntity caster, Vec3 glyphCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Powder Snow Pitfall Trap: turns a 3x3 floor area into powdered snow that enemies sink into & freeze rapidly!
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, glyphCenter, 2.5D, 400, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed < 6) return true;
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB box = new AABB(zone.center.x - 2.0D, zone.center.y - 1.0D, zone.center.z - 2.0D,
                            zone.center.x + 2.0D, zone.center.y + 2.0D, zone.center.z + 2.0D);
                    List<LivingEntity> stepped = srv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != owner);
                    if (stepped.isEmpty()) return true;

                    Map<BlockPos, BlockState> saved = new HashMap<>();
                    BlockPos floor = BlockPos.containing(zone.center).below();
                    for (BlockPos p : BlockPos.betweenClosed(floor.offset(-1, -1, -1), floor.offset(1, 0, 1))) {
                        BlockState bs = srv.getBlockState(p);
                        if (!bs.isAir() && !SpellExecutionEngine.isProtectedBlockOrContainer(srv, p, bs)) {
                            BlockPos imm = p.immutable();
                            saved.put(imm, bs);
                            srv.setBlockAndUpdate(imm, Blocks.POWDER_SNOW.defaultBlockState());
                        }
                    }
                    for (LivingEntity v : stepped) {
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                        v.setTicksFrozen(v.getTicksRequiredToFreeze() + 80);
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                    }

                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 2.0D, 120, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (psrv, pzone, pelapsed) -> {
                                if (pelapsed < pzone.maxDurationTicks) return true;
                                saved.forEach((pos, orig) -> {
                                    if (psrv.getBlockState(pos).is(Blocks.POWDER_SNOW)) {
                                        psrv.setBlockAndUpdate(pos, orig);
                                    }
                                });
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
        // Blizzard Shrapnel Fan: shreds enemy armor toughness by 30% and knocks mobs flat for 1.5s (30t)!
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            SpellCombatReactionEngine.applyPercentageArmorShred(hit, level, 0.0D, 0.30D, 100L);
            applySolidFreezeStasis(hit, 30);
            hit.setDeltaMovement(direction.x * 0.75D, -0.60D, direction.z * 0.75D);
            hit.hasImpulse = true;
        }
        level.sendParticles(ParticleTypes.SNOWFLAKE, caster.getX() + direction.x * 2.5D, caster.getEyeY() + direction.y * 2.5D, caster.getZ() + direction.z * 2.5D, 32, 0.8D, 0.4D, 0.8D, 0.1D);
    }

    @Override
    public boolean onSkyfallFormCast(ServerLevel level, LivingEntity caster, Vec3 impactCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Glacial Monolith Cataclysm: 2s (40t) telegraph -> colossal blue ice spire monument + 2s freeze shockwave!
        double r = Math.max(5.5D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 45, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed < 40) {
                        if (elapsed % 5 == 0) {
                            srv.sendParticles(ParticleTypes.SNOWFLAKE, zone.center.x, zone.center.y + 6.0D - (elapsed * 0.12D), zone.center.z,
                                    20, 1.2D, 0.6D, 1.2D, 0.05D);
                        }
                        return true;
                    }
                    LivingEntity owner = zone.resolveCaster(srv);
                    BlockPos base = BlockPos.containing(zone.center);
                    // Build permanent Blue Ice / Packed Ice monolith spire in empty air / water!
                    for (int y = 0; y < 5; y++) {
                        BlockPos core = base.above(y);
                        if (srv.getBlockState(core).canBeReplaced()) {
                            srv.setBlockAndUpdate(core, (y < 3 ? Blocks.BLUE_ICE : Blocks.PACKED_ICE).defaultBlockState());
                        }
                    }
                    for (BlockPos side : List.of(base.north(), base.south(), base.east(), base.west())) {
                        if (srv.getBlockState(side).canBeReplaced()) {
                            srv.setBlockAndUpdate(side, Blocks.PACKED_ICE.defaultBlockState());
                        }
                    }

                    AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                            zone.center.x + zone.radius, zone.center.y + 4.0D, zone.center.z + zone.radius);
                    for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                        if (v.onGround()) {
                            applySolidFreezeStasis(v, 40);
                        }
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 1.25F);
                    }
                    srv.playSound(null, base, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.2F, 0.7F);
                    return false;
                }
        ));
        return true;
    }

    @Override
    public void onSummonFormSpawned(ServerLevel level, LivingEntity caster, Entity summonEntity, Vec3 spawnPos, double radius, SpellAssembly spell, SubSpellSlot slot) {
        CompoundTag tag = EntityHelper.getPersistentData(summonEntity);
        tag.putString("SpellEidolonMateria", EssenceType.GLACIAL.name());
        tag.putInt("SpellEidolonTintRGB", GLACIAL_BRUISER_HEX_COLOR);
        level.sendParticles(ParticleTypes.SNOWFLAKE, spawnPos.x, spawnPos.y + 0.8D, spawnPos.z, 26, 0.45D, 0.55D, 0.45D, 0.05D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Ice Statue Blink: leaves a lifelike frozen ice statue decoy at departure that lures hostiles & shatters into a frost pop!
        BlockPos dep = BlockPos.containing(departurePos);
        boolean placedFeet = false;
        boolean placedHead = false;
        if (level.getBlockState(dep).isAir()) {
            level.setBlockAndUpdate(dep, Blocks.PACKED_ICE.defaultBlockState());
            placedFeet = true;
        }
        if (level.getBlockState(dep.above()).isAir()) {
            level.setBlockAndUpdate(dep.above(), Blocks.PACKED_ICE.defaultBlockState());
            placedHead = true;
        }
        final boolean fFeet = placedFeet;
        final boolean fHead = placedHead;
        final BlockPos immDep = dep.immutable();

        SpellAssembly dummy = SpellAssembly.createMagicMissileStarter(potencyTier);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, departurePos, 5.0D, 60, level.getGameTime(), dummy, dummy.subSpells().get(0), EssenceType.GLACIAL, 1.0F,
                (srv, zone, elapsed) -> {
                    LivingEntity owner = zone.resolveCaster(srv);
                    AABB lureBox = new AABB(departurePos.x - 5.0D, departurePos.y - 2.0D, departurePos.z - 5.0D,
                            departurePos.x + 5.0D, departurePos.y + 3.0D, departurePos.z + 5.0D);
                    if (elapsed < zone.maxDurationTicks) {
                        boolean struckOrBroken = (fFeet && !srv.getBlockState(immDep).is(Blocks.PACKED_ICE))
                                || (fHead && !srv.getBlockState(immDep.above()).is(Blocks.PACKED_ICE));
                        if (!struckOrBroken && elapsed >= 6) {
                            for (Mob mob : srv.getEntitiesOfClass(Mob.class, lureBox)) {
                                if (mob instanceof Monster && mob.isAlive()) {
                                    if (mob.position().distanceToSqr(departurePos) <= 3.25D) {
                                        struckOrBroken = true; // Shatters immediately when a lured hostile strikes the ice statue!
                                        break;
                                    }
                                    if (elapsed % 10 == 0) {
                                        mob.getNavigation().moveTo(departurePos.x, departurePos.y, departurePos.z, 1.15D);
                                    }
                                }
                            }
                        }
                        if (!struckOrBroken) {
                            return true;
                        }
                    }
                    if (fFeet && srv.getBlockState(immDep).is(Blocks.PACKED_ICE)) srv.removeBlock(immDep, false);
                    if (fHead && srv.getBlockState(immDep.above()).is(Blocks.PACKED_ICE)) srv.removeBlock(immDep.above(), false);
                    for (LivingEntity foe : srv.getEntitiesOfClass(LivingEntity.class, lureBox)) {
                        if (foe == owner || !foe.isAlive() || (owner != null && foe.isAlliedTo(owner))) continue;
                        foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 3));
                        foe.setTicksFrozen(foe.getTicksRequiredToFreeze() + 50);
                        foe.hurtServer(srv, srv.damageSources().freeze(), 3.0F + potencyTier * 0.85F);
                    }
                    srv.sendParticles(ParticleTypes.SNOWFLAKE, departurePos.x, departurePos.y + 1.0D, departurePos.z, 28, 1.0D, 0.6D, 1.0D, 0.08D);
                    srv.playSound(null, immDep, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.90F, 1.25F);
                    return false;
                }
        ));
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Cryo-Solidification & Silk Touch: water -> blue/packed ice, lava -> obsidian, guaranteed Silk Touch on brittle/frozen blocks!
        if (state.is(Blocks.WATER)) {
            boolean source = state.hasProperty(LiquidBlock.LEVEL) && state.getValue(LiquidBlock.LEVEL) == 0;
            level.setBlockAndUpdate(pos, (source ? Blocks.BLUE_ICE : Blocks.PACKED_ICE).defaultBlockState());
            return true;
        }
        if (state.is(Blocks.LAVA)) {
            level.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.SNOW_BLOCK)) {
            level.setBlockAndUpdate(pos, Blocks.PACKED_ICE.defaultBlockState());
            return true;
        }
        if (state.is(Blocks.PACKED_ICE)) {
            level.setBlockAndUpdate(pos, Blocks.BLUE_ICE.defaultBlockState());
            return true;
        }
        // Silk Touch harvest on ice, glass, glowstone, sea lanterns, amethyst, ores, etc.
        float hardness = state.getDestroySpeed(level, pos);
        Item blockItem = state.getBlock().asItem();
        if (blockItem != Items.AIR && hardness >= 0.0F && hardness <= 2.0F + potencyTier * 5.0F
                && (state.is(BlockTags.ICE) || state.is(BlockTags.IMPERMEABLE) || state.is(Blocks.GLOWSTONE) || state.is(Blocks.SEA_LANTERN))) {
            level.removeBlock(pos, false);
            Block.popResource(level, pos, new ItemStack(blockItem, 1));
            level.sendParticles(ParticleTypes.SNOWFLAKE, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 8, 0.25D, 0.25D, 0.25D, 0.02D);
            return true;
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack in = itemEntity.getItem();
        if (in.is(Items.SNOW_BLOCK) || in.is(Items.ICE)) {
            itemEntity.setItem(new ItemStack(Items.PACKED_ICE, in.getCount()));
            return true;
        }
        if (in.is(Items.PACKED_ICE)) {
            itemEntity.setItem(new ItemStack(Items.BLUE_ICE, in.getCount()));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Absolute-Zero Cryo-Hex: deep Freezing, -50% speed, -40% armor toughness, sprint/jump freezes solid for 2s!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.setTicksFrozen(target.getTicksRequiredToFreeze() + 45);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 2, true, true));
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, 0.40D * mult, 35L);

        if (target.isSprinting() || (!target.onGround() && target.getDeltaMovement().y > 0.12D)) {
            target.setSprinting(false);
            applySolidFreezeStasis(target, 40);
            level.playSound(null, target.blockPosition(), SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.75F, 1.4F);
        }

        target.hurtServer(level, level.damageSources().freeze(), (1.4F + potencyTier * 0.50F) * mult);
        level.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY(0.8D), target.getZ(), 6, 0.25D, 0.35D, 0.25D, 0.02D);
        return true;
    }

    @Override
    public void onEffectAppliedToEntity(ServerLevel level, @Nullable LivingEntity caster, LivingEntity target, Vec3 direction, SpellCoreEffect effect, int potencyTier, SpellAssembly spell) {
        if (effect == SpellCoreEffect.RESTORATION) {
            // Cryo-Cocoon Mending: invulnerable ice cocoon (100% total damage immunity) for 2s (40t) while rapidly regenerating HP!
            EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_CRYO_COCOON_UNTIL, level.getGameTime() + 40L);
            target.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 40, 4, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 6, false, false));
            target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 2, false, true));
            level.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY(0.9D), target.getZ(), 24, 0.4D, 0.6D, 0.4D, 0.04D);
            level.playSound(null, target.blockPosition(), SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.85F, 1.35F);
        }
    }

    private static void applySolidFreezeStasis(LivingEntity target, int ticks) {
        target.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.PARALYZED.get()), ticks, 0));
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, ticks, 6));
        target.setTicksFrozen(target.getTicksRequiredToFreeze() + ticks);
        target.setDeltaMovement(0.0D, Math.min(0.0D, target.getDeltaMovement().y), 0.0D);
        target.hasImpulse = true;
    }

    private static void freezeWaterPatch(ServerLevel level, BlockPos center, int r, Block iceBlock) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -1, -r), center.offset(r, 1, r))) {
            if (level.getBlockState(p).is(Blocks.WATER)) {
                level.setBlockAndUpdate(p, iceBlock.defaultBlockState());
            }
        }
    }
}
