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
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
 * Complete per-part Signature Mutation implementation for <b>Magma Materia (Tier 1 Fusion #13)</b>
 * (Spellcrafting System Guide Section 6.14).
 */
public final class MagmaSpellMutationHandler implements MateriaPartMutationHandler {

    public static final int MAGMA_BRUTE_HEX_COLOR = 0xB83C08;

    @Override
    public EssenceType materiaType() {
        return EssenceType.MAGMA;
    }

    @Override
    public String resolveFlowTitle(SpellFlowModifier flow) {
        return switch (flow) {
            case DOUBLE_BIFURCATE, TRIPLE_TRIFURCATE, QUAD_SPREAD, QUINTUPLE_ARC, SEXTUPLE_RING, SEPTUPLE_CROWN, OCTUPLE_DOME -> "Pyroclastic Split Flow";
            default -> "Pyroclastic " + flow.getDisplayName();
        };
    }

    @Override
    public String resolveFormTitle(SpellDeliveryForm form) {
        return switch (form) {
            case PROJECTILE -> "Molten Magma Boulder";
            case BEAM -> "Pyroclastic Lavastream";
            case RADIAL_BURST -> "Caldera Shockwave Nova";
            case TOUCH_STRIKE -> "Volcanic Slag Cleave";
            case AURA -> "Igneous Armor Mantle";
            case GLYPH -> "Volcanic Vent Geyser Trap";
            case CONE -> "Pyroclastic Surge";
            case SKYFALL -> "Volcanic Meteorite Bombardment";
            case SUMMON_AGENT, SUMMON_SENTRY, SUMMON_DECOY -> ddraig.net.entropica.spell.engine.SpellEidolonSpecs.resolveCanonicalBlueprintTitle(materiaType(), form.getSummonArchetype());
        };
    }

    @Override
    public String resolveEffectTitle(SpellCoreEffect effect) {
        return switch (effect) {
            case BLINK -> "Caldera Step";
            case TRANSMUTE, BREAK -> "Igneous Forging";
            case AFFLICTION -> "Molten Slag Encasement Hex";
            case KINETIC -> "Volcanic Eruption Blast";
            default -> "Volcanic " + effect.getDisplayName();
        };
    }

    // --- Part 1: Flow Mutations ---

    @Override
    public void onFlowMultiCastOrigin(ServerLevel level, LivingEntity caster, SpellFlowModifier flow, int branchCount, SpellAssembly spell) {
        // Pyroclastic Split Flow: when splitting into multiple projectiles, the split origin erupts in a volcanic pop!
        eruptVolcanicPop(level, caster, caster.position().add(0.0D, 0.9D, 0.0D), spell.hasWardFriendlyFireSafeguard());
    }

    @Override
    public void onProjectileImpactOrExpireFromFlow(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Vec3 splitOrigin, SpellAssembly spell, SubSpellSlot slot) {
        SpellFlowModifier flow = spell.flowPart().flow();
        if (flow.getDefaultSubSpellCount() >= 2 || flow == SpellFlowModifier.DELAYED_BLOOM) {
            eruptVolcanicPop(level, caster, splitOrigin, spell.hasWardFriendlyFireSafeguard());
        }
    }

    private static void eruptVolcanicPop(ServerLevel level, @Nullable LivingEntity caster, Vec3 center, boolean wardSafeguard) {
        level.sendParticles(ParticleTypes.LAVA, center.x, center.y, center.z, 14, 0.45D, 0.35D, 0.45D, 0.08D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.75F, 0.85F);
        AABB box = new AABB(center.x - 2.5D, center.y - 1.5D, center.z - 2.5D,
                center.x + 2.5D, center.y + 2.0D, center.z + 2.5D);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, wardSafeguard)) continue;
            foe.igniteForSeconds(4);
        }
    }

    // --- Part 2: Delivery Form Mutations ---

    @Override
    public void onProjectileSpawnedFromForm(ServerLevel level, SpellProjectileEntity projectile, LivingEntity caster, SpellAssembly spell, SubSpellSlot slot) {
        // Molten Magma Boulder: lobbed projectile with slight gravity arc
        projectile.setNoGravity(false);
        Vec3 v = projectile.getDeltaMovement();
        projectile.setDeltaMovement(v.x * 0.92D, v.y + 0.12D, v.z * 0.92D);
    }

    @Override
    public void onProjectileTickFromForm(ServerLevel level, SpellProjectileEntity projectile, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.DRIPPING_LAVA, projectile.getX(), projectile.getY(), projectile.getZ(),
                2, 0.1D, 0.1D, 0.1D, 0.01D);
        level.sendParticles(ParticleTypes.FLAME, projectile.getX(), projectile.getY(), projectile.getZ(),
                2, 0.08D, 0.08D, 0.08D, 0.01D);
    }

    @Override
    public void onProjectileHitEntityFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, Entity hitEntity, SpellAssembly spell, SubSpellSlot slot) {
        splatterMoltenBoulder(level, caster, projectile.position(), BlockPos.containing(hitEntity.position()).below(), spell, slot);
    }

    @Override
    public void onProjectileHitBlockFromForm(ServerLevel level, SpellProjectileEntity projectile, @Nullable LivingEntity caster, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot) {
        splatterMoltenBoulder(level, caster, projectile.position(), blockHit.getBlockPos(), spell, slot);
    }

    private static void splatterMoltenBoulder(ServerLevel level, @Nullable LivingEntity caster, Vec3 center, BlockPos crustPos, SpellAssembly spell, SubSpellSlot slot) {
        level.sendParticles(ParticleTypes.LAVA, center.x, center.y + 0.2D, center.z, 16, 0.45D, 0.35D, 0.45D, 0.08D);
        AABB box = new AABB(center.x - 2.5D, center.y - 1.5D, center.z - 2.5D, center.x + 2.5D, center.y + 2.0D, center.z + 2.5D);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (victim == caster || !victim.isAlive()) continue;
            if (caster != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(victim, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            victim.igniteForSeconds(6);
        }

        // Temporary 1-block magma crust on hit ground (restores cleanly after 80 ticks)
        BlockState prev = level.getBlockState(crustPos);
        if (prev.isSolidRender() && !prev.is(Blocks.MAGMA_BLOCK) && !SpellExecutionEngine.isProtectedBlockOrContainer(level, crustPos, prev)) {
            BlockPos targetPos = crustPos.immutable();
            level.setBlockAndUpdate(targetPos, Blocks.MAGMA_BLOCK.defaultBlockState());
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, Vec3.atCenterOf(targetPos), 1.5D, 80, level.getGameTime(), spell, slot, EssenceType.MAGMA, 1.0F,
                    (srv, zone, elapsed) -> {
                        if (elapsed < zone.maxDurationTicks) return true;
                        if (srv.getBlockState(targetPos).is(Blocks.MAGMA_BLOCK)) {
                            srv.setBlockAndUpdate(targetPos, prev);
                        }
                        return false;
                    }
            ));
        }
    }

    @Override
    public void onBeamFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, double maxReach, List<LivingEntity> hitEntities, BlockHitResult blockHit, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Pyroclastic Lavastream: unquenchable fire, melts ice/snow, bores a 1x1 hole through stone/dirt, auto-smelts mined raw ores!
        long now = level.getGameTime();
        int tier = spell.getEffectiveTier();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.igniteForSeconds(6);
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, now + 120L);
        }

        int boreSteps = Math.min(8 + tier * 2, (int) Math.ceil(maxReach));
        for (int s = 1; s <= boreSteps; s++) {
            BlockPos p = BlockPos.containing(caster.getEyePosition().add(direction.scale(s)));
            BlockState bs = level.getBlockState(p);
            if (bs.isAir() || SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;

            if (bs.is(BlockTags.ICE) || bs.is(Blocks.SNOW) || bs.is(Blocks.SNOW_BLOCK) || bs.is(Blocks.POWDER_SNOW)) {
                level.setBlockAndUpdate(p, bs.is(Blocks.BLUE_ICE) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState());
                level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D, 4, 0.2D, 0.2D, 0.2D, 0.02D);
                continue;
            }

            float hardness = bs.getDestroySpeed(level, p);
            boolean boreable = bs.is(BlockTags.BASE_STONE_OVERWORLD)
                    || bs.is(BlockTags.BASE_STONE_NETHER)
                    || bs.is(BlockTags.DIRT)
                    || bs.is(Blocks.COBBLESTONE)
                    || bs.is(Blocks.GRAVEL)
                    || (hardness >= 0.0F && hardness <= 3.0F + tier * 1.5F);
            if (boreable && hardness >= 0.0F) {
                List<ItemStack> drops = Block.getDrops(bs, level, p, null, caster, ItemStack.EMPTY);
                level.removeBlock(p, false);
                for (ItemStack d : drops) {
                    Block.popResource(level, p, NetherSpellMutationHandler.trySmeltStack(level, d));
                }
                level.sendParticles(ParticleTypes.LAVA, p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D, 3, 0.15D, 0.15D, 0.15D, 0.02D);
            }
        }
    }

    @Override
    public void onRadialBurstFormCast(ServerLevel level, LivingEntity caster, Vec3 center, double radius, Vec3 direction, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Caldera Shockwave Nova: 6m volcanic eruption knocking mobs backward, setting ablaze, turning random floor blocks molten
        double r = Math.max(6.0D, radius);
        AABB box = new AABB(center.x - r, center.y - 2.0D, center.z - r, center.x + r, center.y + 2.5D, center.z + r);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (foe == caster || !foe.isAlive()) continue;
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(foe, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            foe.igniteForSeconds(6);
            Vec3 away = foe.position().subtract(center).normalize().scale(1.25D);
            foe.setDeltaMovement(away.x, 0.42D, away.z);
            foe.hasImpulse = true;
        }

        Map<BlockPos, BlockState> moltenFloor = new HashMap<>();
        BlockPos cPos = BlockPos.containing(center).below();
        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-4, 0, -4), cPos.offset(4, 0, 4))) {
            if (p.distSqr(cPos) > 18.0D) continue;
            BlockState bs = level.getBlockState(p);
            if (bs.isSolidRender() && !bs.is(Blocks.MAGMA_BLOCK) && !bs.is(Blocks.LAVA)
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)
                    && level.random.nextFloat() < 0.25F) {
                BlockPos imm = p.immutable();
                moltenFloor.put(imm, bs);
                // Place contained molten Lava blocks in rimmed floor basins, or Magma Block on exposed edges!
                BlockState moltenState = isContainedLavaCell(level, imm)
                        ? Blocks.LAVA.defaultBlockState()
                        : Blocks.MAGMA_BLOCK.defaultBlockState();
                level.setBlockAndUpdate(imm, moltenState);
            }
        }
        if (!moltenFloor.isEmpty()) {
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, center, r, 100, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                    (srv, zone, elapsed) -> {
                        if (elapsed < zone.maxDurationTicks) return true;
                        moltenFloor.forEach((pos, orig) -> {
                            BlockState cur = srv.getBlockState(pos);
                            if (cur.is(Blocks.MAGMA_BLOCK) || cur.is(Blocks.LAVA) || cur.is(Blocks.OBSIDIAN) || cur.is(Blocks.COBBLESTONE)) {
                                srv.setBlockAndUpdate(pos, orig);
                            }
                        });
                        return false;
                    }
            ));
        }
        level.sendParticles(ParticleTypes.LAVA, center.x, center.y + 0.5D, center.z, 32, 2.0D, 0.5D, 2.0D, 0.1D);
        level.playSound(null, BlockPos.containing(center), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.9F, 0.75F);
    }

    @Override
    public void onTouchStrikeFormCast(ServerLevel level, LivingEntity caster, Vec3 direction, List<LivingEntity> hitEntities, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Volcanic Slag Cleave: 6s fire, -30% armor toughness for 4s (80t), and auto-smelts drops if slain!
        long now = level.getGameTime();
        for (LivingEntity hit : hitEntities) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.igniteForSeconds(6);
            SpellCombatReactionEngine.applyPercentageArmorShred(hit, level, 0.0D, 0.30D, 80L);
            EntityHelper.getPersistentData(hit).putLong(SpellLingeringZoneManager.TAG_NETHER_AUTO_SMELT_UNTIL, now + 60L);
            NetherSpellMutationHandler.autoSmeltDroppedItems(level, hit.getBoundingBox().inflate(2.5D));
        }
    }

    @Override
    public void onAuraFormCast(ServerLevel level, LivingEntity caster, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Igneous Armor Mantle: +4 Armor, 50% knockback resistance, -10% movement speed, 4m snow/ice melt & water steam
        int dur = 200 + spell.getEffectiveTier() * 40;
        EntityHelper.getPersistentData(caster).putLong(SpellLingeringZoneManager.TAG_MAGMA_MANTLE_UNTIL, level.getGameTime() + dur);
        SpellCombatReactionEngine.applyTimedArmorAndToughnessBuff(caster, level, 4.0D, 0.0D, 0.50D, -0.10D, dur);
        level.sendParticles(ParticleTypes.LAVA, caster.getX(), caster.getY(0.8D), caster.getZ(), 20, 0.45D, 0.5D, 0.45D, 0.05D);
    }

    @Override
    public boolean onGlyphFormCast(ServerLevel level, LivingEntity caster, Vec3 glyphCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Volcanic Vent Geyser Trap: 3s vertical lava/steam column launching triggering mob 8m upward + 3x3 molten lava/magma floor!
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

                    Map<BlockPos, BlockState> savedFloor = new HashMap<>();
                    BlockPos floor = BlockPos.containing(zone.center).below();
                    for (BlockPos p : BlockPos.betweenClosed(floor.offset(-1, 0, -1), floor.offset(1, 0, 1))) {
                        BlockState bs = srv.getBlockState(p);
                        if (bs.isSolidRender() && !bs.is(Blocks.MAGMA_BLOCK) && !bs.is(Blocks.LAVA)
                                && !SpellExecutionEngine.isProtectedBlockOrContainer(srv, p, bs)) {
                            BlockPos imm = p.immutable();
                            savedFloor.put(imm, bs);
                            srv.setBlockAndUpdate(imm, Blocks.MAGMA_BLOCK.defaultBlockState());
                        }
                    }
                    // Center cell turns into contained molten Lava once the surrounding 3x3 magma rim is solid!
                    if (savedFloor.containsKey(floor.immutable()) && isContainedLavaCell(srv, floor)) {
                        srv.setBlockAndUpdate(floor, Blocks.LAVA.defaultBlockState());
                    }

                    for (LivingEntity v : stepped) {
                        if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                        v.igniteForSeconds(6);
                        v.setDeltaMovement(v.getDeltaMovement().x * 0.3D, 1.18D, v.getDeltaMovement().z * 0.3D); // ~8 blocks upward!
                        v.hasImpulse = true;
                        SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, new Vec3(0, 1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency);
                    }
                    srv.playSound(null, BlockPos.containing(zone.center), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.85F, 0.85F);

                    // 3-second (60t) active vertical molten lava & steam geyser column!
                    SpellLingeringZoneManager.registerZone(srv, new SpellLingeringZoneManager.ActiveSpellZone(
                            owner, zone.center, 2.0D, 60, srv.getGameTime(), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency,
                            (vsrv, vzone, velapsed) -> {
                                if (velapsed >= vzone.maxDurationTicks) {
                                    savedFloor.forEach((pos, orig) -> {
                                        BlockState cur = vsrv.getBlockState(pos);
                                        if (cur.is(Blocks.MAGMA_BLOCK) || cur.is(Blocks.LAVA) || cur.is(Blocks.OBSIDIAN) || cur.is(Blocks.COBBLESTONE)) {
                                            vsrv.setBlockAndUpdate(pos, orig);
                                        }
                                    });
                                    return false;
                                }
                                if (velapsed % 5 == 0) {
                                    vsrv.sendParticles(ParticleTypes.LAVA, vzone.center.x, vzone.center.y + 1.5D, vzone.center.z, 8, 0.35D, 1.8D, 0.35D, 0.06D);
                                    vsrv.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, vzone.center.x, vzone.center.y + 2.0D, vzone.center.z, 6, 0.4D, 1.5D, 0.4D, 0.04D);
                                }
                                if (velapsed % 12 == 0) {
                                    LivingEntity gOwner = vzone.resolveCaster(vsrv);
                                    AABB column = new AABB(vzone.center.x - 1.75D, vzone.center.y - 1.0D, vzone.center.z - 1.75D,
                                            vzone.center.x + 1.75D, vzone.center.y + 8.0D, vzone.center.z + 1.75D);
                                    for (LivingEntity inCol : vsrv.getEntitiesOfClass(LivingEntity.class, column, e -> e.isAlive() && e != gOwner)) {
                                        if (gOwner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(inCol, gOwner, vzone.spell.hasWardFriendlyFireSafeguard())) continue;
                                        inCol.igniteForSeconds(5);
                                        inCol.hurtServer(vsrv, vsrv.damageSources().onFire(), 2.0F + vzone.spell.getEffectiveTier() * 0.55F);
                                        if (inCol.getDeltaMovement().y < 0.75D) {
                                            inCol.setDeltaMovement(inCol.getDeltaMovement().x * 0.4D, 0.85D, inCol.getDeltaMovement().z * 0.4D);
                                            inCol.hasImpulse = true;
                                        }
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
        // Pyroclastic Surge: pushes enemies back, ignites, turns water into cobblestone/obsidian, melts snow & ice!
        for (LivingEntity hit : hitTargets) {
            if (SpellCombatReactionEngine.isProtectedFromOffensiveSpell(hit, caster, spell.hasWardFriendlyFireSafeguard())) continue;
            hit.igniteForSeconds(6);
            hit.setDeltaMovement(direction.x * 1.15D, 0.35D, direction.z * 1.15D);
            hit.hasImpulse = true;
        }
        int steps = Math.min(8, (int) Math.ceil(coneRange));
        for (int s = 1; s <= steps; s++) {
            BlockPos stepPos = BlockPos.containing(caster.position().add(direction.scale(s)));
            for (BlockPos p : BlockPos.betweenClosed(stepPos.offset(-2, -1, -2), stepPos.offset(2, 1, 2))) {
                BlockState bs = level.getBlockState(p);
                if (bs.is(Blocks.WATER)) {
                    boolean source = bs.getFluidState().isSource();
                    level.setBlockAndUpdate(p, (source ? Blocks.OBSIDIAN : Blocks.COBBLESTONE).defaultBlockState());
                } else if (bs.is(BlockTags.ICE) || bs.is(Blocks.SNOW) || bs.is(Blocks.SNOW_BLOCK) || bs.is(Blocks.POWDER_SNOW)) {
                    level.removeBlock(p, false);
                }
            }
        }
        level.sendParticles(ParticleTypes.LAVA, caster.getX() + direction.x * 2.5D, caster.getEyeY() + direction.y * 2.5D, caster.getZ() + direction.z * 2.5D, 28, 0.8D, 0.4D, 0.8D, 0.08D);
    }

    @Override
    public boolean onSkyfallFormCast(ServerLevel level, LivingEntity caster, Vec3 impactCenter, double radius, SpellAssembly spell, SubSpellSlot slot, @Nullable EssenceType fallbackMateria, float apparatusPotency) {
        // Volcanic Meteorite Bombardment: 6s (120t) across 12m area staggering mobs, igniting, vaporizing water, auto-smelting drops!
        double r = Math.max(6.0D, radius);
        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, impactCenter, r, 120, level.getGameTime(), spell, slot, fallbackMateria, apparatusPotency,
                (srv, zone, elapsed) -> {
                    if (elapsed > zone.maxDurationTicks) return false;
                    if (elapsed % 5 == 0) {
                        srv.sendParticles(ParticleTypes.LAVA, zone.center.x, zone.center.y + 5.5D, zone.center.z, 14, zone.radius * 0.55D, 1.2D, zone.radius * 0.55D, 0.1D);
                    }
                    if (elapsed % 20 == 0) {
                        LivingEntity owner = zone.resolveCaster(srv);
                        long now = srv.getGameTime();
                        BlockPos cPos = BlockPos.containing(zone.center);
                        int br = Math.min(6, (int) Math.ceil(zone.radius));
                        for (BlockPos p : BlockPos.betweenClosed(cPos.offset(-br, -1, -br), cPos.offset(br, 1, br))) {
                            if (srv.getBlockState(p).is(Blocks.WATER)) {
                                srv.removeBlock(p, false);
                                srv.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D, 3, 0.15D, 0.2D, 0.15D, 0.02D);
                            }
                        }
                        AABB area = new AABB(zone.center.x - zone.radius, zone.center.y - 2.0D, zone.center.z - zone.radius,
                                zone.center.x + zone.radius, zone.center.y + 5.0D, zone.center.z + zone.radius);
                        NetherSpellMutationHandler.autoSmeltDroppedItems(srv, area);
                        for (LivingEntity v : srv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner)) {
                            if (owner != null && SpellCombatReactionEngine.isProtectedFromOffensiveSpell(v, owner, zone.spell.hasWardFriendlyFireSafeguard())) continue;
                            v.igniteForSeconds(5);
                            EntityHelper.getPersistentData(v).putLong(SpellLingeringZoneManager.TAG_NETHER_AUTO_SMELT_UNTIL, now + 45L);
                            v.setDeltaMovement(v.getDeltaMovement().x * 0.3D, -0.35D, v.getDeltaMovement().z * 0.3D);
                            v.hasImpulse = true;
                            SpellExecutionEngine.deliverSubSpellOnEntity(srv, owner, v, new Vec3(0, -1, 0), zone.spell, zone.slot, zone.fallbackMateria, zone.apparatusPotency * 0.40F);
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
        tag.putString("SpellEidolonMateria", EssenceType.MAGMA.name());
        tag.putInt("SpellEidolonTintRGB", MAGMA_BRUTE_HEX_COLOR);
        level.sendParticles(ParticleTypes.LAVA, spawnPos.x, spawnPos.y + 0.8D, spawnPos.z, 24, 0.45D, 0.55D, 0.45D, 0.08D);
    }

    // --- Part 3: Core Effect Mutations ---

    @Override
    public void onBlinkCast(ServerLevel level, LivingEntity caster, Vec3 departurePos, Vec3 arrivalPos, int potencyTier) {
        // Caldera Step: leaves a pool of molten lava at departure + arrives with an explosive volcanic fire shockwave!
        BlockPos depFloor = BlockPos.containing(departurePos).below();
        BlockState prev = level.getBlockState(depFloor);
        if (prev.isSolidRender() && !prev.is(Blocks.MAGMA_BLOCK) && !prev.is(Blocks.LAVA)
                && !SpellExecutionEngine.isProtectedBlockOrContainer(level, depFloor, prev)) {
            BlockPos imm = depFloor.immutable();
            BlockState poolState = isContainedLavaCell(level, imm)
                    ? Blocks.LAVA.defaultBlockState()
                    : Blocks.MAGMA_BLOCK.defaultBlockState();
            level.setBlockAndUpdate(imm, poolState);
            SpellAssembly dummy = SpellAssembly.createMagicMissileStarter(potencyTier);
            SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                    caster, departurePos, 1.5D, 80, level.getGameTime(), dummy, dummy.subSpells().get(0), EssenceType.MAGMA, 1.0F,
                    (srv, zone, elapsed) -> {
                        if (elapsed < zone.maxDurationTicks) return true;
                        BlockState cur = srv.getBlockState(imm);
                        if (cur.is(Blocks.MAGMA_BLOCK) || cur.is(Blocks.LAVA) || cur.is(Blocks.OBSIDIAN) || cur.is(Blocks.COBBLESTONE)) {
                            srv.setBlockAndUpdate(imm, prev);
                        }
                        return false;
                    }
            ));
        }
        level.sendParticles(ParticleTypes.LAVA, arrivalPos.x, arrivalPos.y + 0.8D, arrivalPos.z, 26, 0.7D, 0.45D, 0.7D, 0.08D);
        float shockDmg = 3.0F + potencyTier * 1.15F;
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(3.5D))) {
            if (foe == caster || foe.isAlliedTo(caster)) continue;
            foe.igniteForSeconds(5);
            foe.hurtServer(level, level.damageSources().onFire(), shockDmg);
            Vec3 away = foe.position().subtract(arrivalPos).normalize().scale(1.15D);
            foe.setDeltaMovement(away.x, 0.42D, away.z);
            foe.hasImpulse = true;
        }
        level.playSound(null, BlockPos.containing(arrivalPos), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.85F, 0.9F);
    }

    private static boolean isContainedLavaCell(ServerLevel level, BlockPos cell) {
        if (!level.getBlockState(cell.below()).isSolidRender()) return false;
        return level.getBlockState(cell.north()).isSolidRender()
                && level.getBlockState(cell.south()).isSolidRender()
                && level.getBlockState(cell.east()).isSolidRender()
                && level.getBlockState(cell.west()).isSolidRender();
    }

    @Override
    public boolean onTransmuteBlock(ServerLevel level, @Nullable LivingEntity caster, BlockPos pos, BlockState state, int potencyTier) {
        // Igneous Forging: auto-smelts raw ores, turns water into obsidian/magma, and melts stone into magma blocks!
        if (state.is(Blocks.WATER)) {
            boolean isSource = state.hasProperty(LiquidBlock.LEVEL) && state.getValue(LiquidBlock.LEVEL) == 0;
            level.setBlockAndUpdate(pos, (isSource ? Blocks.OBSIDIAN : Blocks.MAGMA_BLOCK).defaultBlockState());
            level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 8, 0.2D, 0.25D, 0.2D, 0.03D);
            return true;
        }
        if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE) || state.is(Blocks.DEEPSLATE) || state.is(Blocks.BASALT)) {
            level.setBlockAndUpdate(pos, Blocks.MAGMA_BLOCK.defaultBlockState());
            level.sendParticles(ParticleTypes.LAVA, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 8, 0.2D, 0.2D, 0.2D, 0.04D);
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
                level.sendParticles(ParticleTypes.LAVA, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 8, 0.2D, 0.2D, 0.2D, 0.04D);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTransmuteDroppedItem(ServerLevel level, @Nullable LivingEntity caster, ItemEntity itemEntity, int potencyTier) {
        ItemStack in = itemEntity.getItem();
        if (in.is(Items.COBBLESTONE) || in.is(Items.STONE)) {
            itemEntity.setItem(new ItemStack(Items.MAGMA_BLOCK, in.getCount()));
            return true;
        }
        ItemStack smelted = NetherSpellMutationHandler.trySmeltStack(level, in);
        if (smelted != in) {
            itemEntity.setItem(smelted);
            level.sendParticles(ParticleTypes.LAVA, itemEntity.getX(), itemEntity.getY(0.3D), itemEntity.getZ(), 6, 0.15D, 0.15D, 0.15D, 0.03D);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPersistentHexTick(ServerLevel level, LivingEntity target, int potencyTier) {
        // Molten Slag Encasement Hex: unquenchable fire even underwater, -40% armor toughness, -30% movement speed!
        float mult = SpellCombatReactionEngine.getHexStackMultiplier(target);
        target.igniteForSeconds(3);
        EntityHelper.getPersistentData(target).putLong(SpellLingeringZoneManager.TAG_UNQUENCHABLE_FIRE_UNTIL, level.getGameTime() + 35L);
        SpellCombatReactionEngine.applyPercentageArmorShred(target, level, 0.0D, 0.40D * mult, 35L);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 35, 1, true, true));
        target.hurtServer(level, level.damageSources().onFire(), (1.6F + potencyTier * 0.55F) * mult);
        level.sendParticles(ParticleTypes.DRIPPING_LAVA, target.getX(), target.getY(0.8D), target.getZ(), 6, 0.25D, 0.35D, 0.25D, 0.02D);
        return true;
    }

    @Override
    public void onEffectAppliedToEntity(ServerLevel level, @Nullable LivingEntity caster, LivingEntity target, Vec3 direction, SpellCoreEffect effect, int potencyTier, SpellAssembly spell) {
        if (effect == SpellCoreEffect.KINETIC && target != caster && !SpellCombatReactionEngine.isBossEntity(level, target)) {
            // Volcanic Eruption Blast: launches mobs 12 blocks airborne!
            target.setDeltaMovement(target.getDeltaMovement().x * 0.35D, 1.42D, target.getDeltaMovement().z * 0.35D);
            target.hasImpulse = true;
            target.igniteForSeconds(4);
            level.sendParticles(ParticleTypes.LAVA, target.getX(), target.getY(0.2D), target.getZ(), 14, 0.35D, 0.3D, 0.35D, 0.08D);
        }
    }
}
